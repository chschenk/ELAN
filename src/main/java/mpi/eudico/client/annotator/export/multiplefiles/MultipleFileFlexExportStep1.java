package mpi.eudico.client.annotator.export.multiplefiles;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.export.ExportFlexStep1;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * Step 1: Step pane for selecting multiple
 * files and element mapping
 * 
 * @author aarsom
 * @version April, 2013
 */
@SuppressWarnings("serial")
public class MultipleFileFlexExportStep1 extends StepPane implements ActionListener{	
	
	private JRadioButton selectedFilesFromDiskRB, filesFromDomainRB;
	private JPanel fileSelectionPanel, elementMappingPanel;
	private ButtonGroup buttonGroup;
	private JButton selectFilesBtn, selectDomainBtn;	
	
	private JTable typeTable;
	private DefaultTableModel typeModel; 

	private JCheckBox textCB, paraCB;
	
	private List<TranscriptionImpl> transList;
	private List<String> openedFileList;	
	
	private final String ELEMENT_NAME = "Element Name";    
	private final String ELEMENT_TYPE = "Element type";    
	private final String SELECT_TYPE = ElanLocale.getString("TranscriptionManager.ComboBoxDefaultString");	
	private final String DEFAULT_VALUE = ElanLocale.getString("ExportFlexDialog.DefaultValue");	  	
	
	private Insets globalInset = new Insets(2, 4, 2, 4);  
	
	/** a hash map of flexType - list<linguistic types> */
	private HashMap<String, List<String>> elementTypeMap;
	
	private final int IT_INDEX = 0;
	private final int PARA_INDEX = 1;
	private final int PHRASE_INDEX = 2;
	private final int WORD_INDEX = 3;
	private final int MORPH_INDEX = 4;

	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * 
	 */
	public MultipleFileFlexExportStep1(MultiStepPane mp){
		super(mp);		
		
		elementTypeMap = new HashMap<String, List<String>>();
		elementTypeMap.put(FlexConstants.IT, new ArrayList<String>());
		elementTypeMap.put(FlexConstants.PARAGR, new ArrayList<String>());
		elementTypeMap.put(FlexConstants.PHRASE, new ArrayList<String>());
		elementTypeMap.put(FlexConstants.WORD, new ArrayList<String>());
		elementTypeMap.put(FlexConstants.MORPH, new ArrayList<String>());
		
		initComponents();
	}
		
	/**
	 * Initialize the ui components
	 */
	@Override
	protected void initComponents(){			
		setLayout(new GridBagLayout());
			
		initFileSelectionPanel();
		
		initElementMappingPanel();	
		
        GridBagConstraints gbc = new GridBagConstraints();	
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.insets = globalInset;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		add(fileSelectionPanel, gbc);
		
		gbc.gridy = 1;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		add(elementMappingPanel, gbc);
	}
	
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("MultiFileExportFlex.Step1.Title");
	}
	
	@Override
	public void enterStepForward(){
		updateButtonStates();
	}
	
	@Override
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	@Override
	public boolean leaveStepForward(){	
		storePreferences();
		
		HashMap<String, String> elementMap = new HashMap<String, String>();
		elementMap.put(FlexConstants.IT, (String) typeTable.getValueAt(IT_INDEX,1));
		elementMap.put(FlexConstants.PARAGR, (String) typeTable.getValueAt(PARA_INDEX,1));
		elementMap.put(FlexConstants.PHRASE, (String) typeTable.getValueAt(PHRASE_INDEX,1));
		
		String type = (String) typeTable.getValueAt(WORD_INDEX,1);
		if(type != null &&
				!type.equals(DEFAULT_VALUE) &&
				!type.equals(SELECT_TYPE)){
			elementMap.put(FlexConstants.WORD, type);
		}
		
		type = (String) typeTable.getValueAt(MORPH_INDEX,1);
		if(type != null &&
				!type.equals(DEFAULT_VALUE) &&
				!type.equals(SELECT_TYPE)){
			elementMap.put(FlexConstants.MORPH, type);
		}		
		
		multiPane.putStepProperty("ElementTypeMap", elementMap);	
		multiPane.putStepProperty("TransImplList", transList);
		multiPane.putStepProperty("OpenedFiles", openedFileList);
		
		return true;
	}
	
	/**
	 * Initializes the upper part containing file selection
	 */
	private void initFileSelectionPanel(){			
		//panel
		fileSelectionPanel = new JPanel(new GridBagLayout());
		fileSelectionPanel.setBorder(new TitledBorder( ElanLocale.getString("MultiFileExport.Panel.Title.FileSelection")));
		
		//create all radio buttons		
		selectedFilesFromDiskRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromFileBrowser"));
		selectedFilesFromDiskRB.addActionListener(this);
		selectedFilesFromDiskRB.setSelected(true);
		
		filesFromDomainRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromDomain"));;
		filesFromDomainRB.addActionListener(this);
		
		//add radio buttons to button group
		buttonGroup = new ButtonGroup();
		buttonGroup.add(selectedFilesFromDiskRB);
		buttonGroup.add(filesFromDomainRB);
		
		//create all buttons		
		selectFilesBtn = new JButton(ElanLocale.getString("Button.Browse"));
		selectFilesBtn.addActionListener(this);
		
		selectDomainBtn = new JButton(ElanLocale.getString("FileAndTierSelectionStepPane.Button.Domain"));
		selectDomainBtn.addActionListener(this);		
		selectDomainBtn.setEnabled(false);
		
		//add buttons to panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = globalInset;		
		fileSelectionPanel.add(selectedFilesFromDiskRB, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		fileSelectionPanel.add(selectFilesBtn, gbc);
		
		//files from domain
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		fileSelectionPanel.add(filesFromDomainRB, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		fileSelectionPanel.add(selectDomainBtn, gbc);
	}
	
	private void initElementMappingPanel(){
		ChangeListener listener = new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				if(typeTable.isEditing()){
					typeTable.editingCanceled(null);
				}
				
				if(e.getSource() == textCB){
					if(textCB.isSelected()){
						updateTypesForIT();
					} else {
						typeTable.setValueAt(null, IT_INDEX, 1);
						if(paraCB.isSelected()){
							updateTypesForParagraph();
						} else {
							updateTypesForPhrase();
						}
					}
				} else if(e.getSource() == paraCB){
					if(paraCB.isSelected()){
						updateTypesForParagraph();
					} else {
						typeTable.setValueAt(null, PARA_INDEX, 1);
						updateTypesForPhrase();
					}
				}
				
				updateButtonStates();
			}
		};
		
		textCB = new JCheckBox(ElanLocale.getString("ExportFlexStep1.InterLinearText"));
		textCB.setSelected(true);		
		
		paraCB = new JCheckBox(ElanLocale.getString("ExportFlexStep1.Paragraph"));
		paraCB.setSelected(true);
	    	
		// Element Model DefaultTableModel
		typeModel = new DefaultTableModel(){ 
			@Override
			public boolean isCellEditable(int row, int column) {        	
				if(getColumnName(column).equals(ELEMENT_NAME)){
					return false;
				}  
						 			
				if(typeTable.getValueAt(row, column) == null){
					return false;
		 		}
		 			
		 		if(typeTable.getValueAt(row, column).toString().equals(DEFAULT_VALUE)){
		 			return false;
		 		}
		 		
		 		if(typeTable.isEditing()){
					typeTable.editingCanceled(null);
				}

		 		
		 		return true;
			}
		};        
		typeModel.setColumnIdentifiers(new String[] {ELEMENT_NAME, ELEMENT_TYPE}); 
		typeModel.addRow(new Object[]{FlexConstants.IT, null});
		typeModel.addRow(new Object[]{FlexConstants.PARAGR, null});
		typeModel.addRow(new Object[]{FlexConstants.PHRASE, null});
       	typeModel.addRow(new Object[]{FlexConstants.WORD, null});
   		typeModel.addRow(new Object[]{FlexConstants.MORPH, null});
   	        
		//DefaultTableCellRenderer
		DefaultTableCellRenderer typeTableRenderer = new DefaultTableCellRenderer(){
	       	 @Override
			public Component getTableCellRendererComponent(JTable table,
	       				Object value, boolean isSelected, boolean hasFocus, int row,
	       			 	int column){
	       		 
	       		Component cell = super.getTableCellRendererComponent(table, value, 
	       				 isSelected, hasFocus, row, column);
	       		 
	       	 	if(value == null){
	       	 		return cell;
	       	 	}      
	       	 	
	       	 	if(!table.isEnabled()){
	       	 		table.setGridColor(Color.GRAY);
	       	 		cell.setForeground(Color.GRAY);
	       	 		return cell;
	       	 	} else {
	       	 		table.setGridColor(Color.BLACK);
	       	 	}
	       	 	
	       	 	if(row == IT_INDEX){
	       	 		if(textCB.isSelected()){
	       	 			cell.setForeground(Color.BLACK);
	       	 		} else {
	       	 			cell.setForeground(Color.GRAY);
	       	 			return cell;
	       	 		}
	       	 	}	 
	       	 	
	       	 	if(row == PARA_INDEX){
	       	 		if(paraCB.isSelected()){
	       	 			cell.setForeground(Color.BLACK);
	       	 		} else {
	       	 			cell.setForeground(Color.GRAY);
	       	 			return cell;
	       	 		}
	       	 	}	  
	       	 		
	       	 	if(value.equals(SELECT_TYPE)){    			   
	       	 		cell.setForeground(Color.GRAY);
	       	 	} else{
	       	 		cell.setForeground(Color.BLACK);
	       	 	}       		 
	       	 	return cell;    		   
	       	 }
		};
	        
		// table settings
		typeTable = new JTable(typeModel);	       
		typeTable.setCellSelectionEnabled(true);	
		typeTable.setDefaultEditor(Object.class, new TypeTableCellEditor());  
		typeTable.setDefaultRenderer(Object.class, typeTableRenderer);      
		typeTable.setShowGrid(true);
		typeTable.setGridColor(Color.BLACK);      
		typeTable.setSelectionBackground(Color.WHITE);        
		typeTable.setRowHeight(typeTable.getRowHeight()+ 5);  		
	        
		// element mapping panel
		elementMappingPanel = new JPanel(new GridBagLayout());	   
	    elementMappingPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportFlexStep1.ElementMapping")));
	        
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = globalInset;
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.anchor = GridBagConstraints.NORTHWEST;	
	    elementMappingPanel.add(textCB, gbc);	   
			
	    gbc.gridy = 1;		   
	    elementMappingPanel.add(paraCB, gbc);	
		
	    gbc.gridy = 2;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
	    elementMappingPanel.add(new JScrollPane(typeTable), gbc);	
	    
	    loadPreferences();
	    
	    textCB.addChangeListener(listener);	
	    paraCB.addChangeListener(listener);	    	
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if( e != null ){
			Object source = e.getSource();
			if( source == selectFilesBtn ){
				List<String>selectedFiles = new ArrayList<String>();
				FileChooser chooser = new FileChooser(this);
		        chooser.createAndShowMultiFileDialog(ElanLocale.getString("MultiFileImport.Flex.Select"), FileChooser.GENERIC, null, null, FileExtension.EAF_EXT, false, "LastUsedEAFDir", FileChooser.FILES_ONLY, null);           
		       	Object[] array = chooser.getSelectedFiles();
				if(array != null){
					File f;
					for (Object element : array) {
						f = (File) element;
						if( f.canRead()){
							selectedFiles.add(f.getAbsolutePath());
						}
					}
					loadTranscriptionList(selectedFiles);
				}
			} else if( source == selectDomainBtn ){
				//create domain dialog
				MFDomainDialog domainDialog = new MFDomainDialog(ELANCommandFactory.getRootFrame(null), ElanLocale.getString("ExportDialog.Multi"), true);
				domainDialog.setVisible(true);
				
				//when domain is selected, get the search paths
				List<String> searchPaths = domainDialog.getSearchPaths();
				List<String> searchDirs = domainDialog.getSearchDirs();
									
				List<String> selectedFiles = new ArrayList<String>();
				
				//check if domain contains files
				if( !searchPaths.isEmpty() ){					
					File f;
				    for (int i = 0; i < searchPaths.size(); i++) {
				      	f = new File(searchPaths.get(i));
				       	if (f.canRead()) {
				       		if(!selectedFiles.contains(f.getAbsolutePath())){
				       			selectedFiles.add(f.getAbsolutePath());	
				       		}
				       	} 
				    }	
				}
				
				//check if domain contains files
				if( !searchDirs.isEmpty() ){					
				    File f;
				    for (int i = 0; i < searchDirs.size(); i++) {
				      	f = new File(searchDirs.get(i));
				       	if (f.isFile() && f.canRead()) {
				       		if(!selectedFiles.contains(f.getAbsolutePath())){
				       			selectedFiles.add(f.getAbsolutePath());	
				       		}
				       	} else if (f.isDirectory() && f.canRead()) {
				       		addFiles(f, selectedFiles);
				       	}
				    }	
				}
				
				loadTranscriptionList(selectedFiles);
			} else if( source == selectedFilesFromDiskRB ){				
				selectFilesBtn.setEnabled(true);
				selectDomainBtn.setEnabled(false);
			} else if( source == filesFromDomainRB ){				
				selectFilesBtn.setEnabled(false);
				selectDomainBtn.setEnabled(true);
			}
		}		
		updateButtonStates();
	}
		
	/**
	 * Scans the folders for eaf files and adds them to files list,
	 * recursively.
	 *
	 * @param dir the  or folder
	 * @param files the list to add the files to
	 */
	 private void addFiles(File dir, List<String> files) {
		 if ((dir == null) && (files == null)) {
			 return;
		 }

		 File[] allSubs = dir.listFiles();

		 for (int i = 0; i < allSubs.length; i++) {
			 if (allSubs[i].isDirectory() && allSubs[i].canRead()) {
				 addFiles(allSubs[i], files);
			 } else {
				 if (allSubs[i].canRead()) {
					 if (allSubs[i].getName().toLowerCase()
							 .endsWith(FileExtension.EAF_EXT[0])) {
						 // test if the file is already there??
	                    	
						 if(!files.contains(allSubs[i].getAbsolutePath())){
							 files.add(allSubs[i].getAbsolutePath());
						 }
					 }
				 }
	         }
	     }
	 }   

	/**
	 * Updates the button states according to some constraints 
	 * (like everything has to be filled in, consistently)
	 */
	public void updateButtonStates(){		
		boolean phraseTier = true;
		
		String pharseType = (String) typeTable.getValueAt(PHRASE_INDEX, 1);
		if(pharseType == null ||
				pharseType.equals(SELECT_TYPE) ||
				pharseType.equals(DEFAULT_VALUE)){
			phraseTier = false;
		}
		
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, phraseTier);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
	}
	
	
	/**
	 * Initialize the selected file list
	 */
	private void loadTranscriptionList(List<String> files){
		transList = new ArrayList<TranscriptionImpl>();
		openedFileList = new ArrayList<String>();
		TranscriptionImpl transImpl;
		for(int i = 0; i < files.size(); i++){
			transImpl = new TranscriptionImpl( files.get(i) );
			if(transImpl != null){				
				transList.add(transImpl);
				openedFileList.add(files.get(i));
			}
		}		
		loadTypesForIT();
		
		if(textCB.isSelected()){
			updateTypesForIT();
		} else {
			typeTable.setValueAt(null, IT_INDEX, 1);
			if(paraCB.isSelected()){
				updateTypesForParagraph();
			} else {
				updateTypesForPhrase();
			}
		}
		updateButtonStates();
	}  
	
	/**
	 * Updates the possible linguistic types for the interlinear text	
	 */
	private void loadTypesForIT(){	
		List<String> typeList = elementTypeMap.get(FlexConstants.IT);	
		typeList.clear();
		
		List<TierImpl> tiers;
		TierImpl tier;
		for(int tr=0; tr < transList.size(); tr++){
			tiers = transList.get(tr).getTiers();
			for(int i=0; i < tiers.size(); i++){
				tier = tiers.get(i);
				
				if(tier.getLinguisticType().getConstraints() == null){
					if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
						typeList.add(tier.getLinguisticType().getLinguisticTypeName());
					}
				}
			}
		}
	}
	
	/**
	 * Updates the possible linguistic types for the interlinear text
	 * and makes a default selection 
	 */
	private void updateTypesForIT(){	
		List<String> typeList = elementTypeMap.get(FlexConstants.IT);	
		
		if(typeList.size() == 0){
			typeModel.setValueAt(DEFAULT_VALUE, IT_INDEX, 1);			
			typeModel.setValueAt(DEFAULT_VALUE, PARA_INDEX, 1);
			typeModel.setValueAt(DEFAULT_VALUE, WORD_INDEX, 1);
			typeModel.setValueAt(DEFAULT_VALUE, MORPH_INDEX, 1);			
		} else {
			typeModel.setValueAt(typeList.get(0), IT_INDEX, 1);
			for(int i=0 ; i < typeList.size(); i++){
				if(typeList.get(i).startsWith(FlexConstants.IT) ||
						typeList.get(i).startsWith(FlexConstants.TXT)){
					typeModel.setValueAt(typeList.get(i), IT_INDEX, 1);
					break;
				}
			}
			
			if(paraCB.isSelected()){
				updateTypesForParagraph();
			} else {
				updateTypesForPhrase();
			}
			
		}
	}
	
	/**
	 * Updates the possible linguistic types for the paragraph
	 * and makes a default selection
	 */
	private void updateTypesForParagraph(){	
		List<String> typeList = elementTypeMap.get(FlexConstants.PARAGR);		
		typeList.clear();	
		
		String itType = (String) typeTable.getValueAt(IT_INDEX, 1);
		
		// paragraph cane be time_subdiv or sym_subdiv or top level
		TierImpl tier;
		List<TierImpl> tiers;
		for(int t=0; t < transList.size(); t++){
			tiers = transList.get(t).getTiers();
			for(int i=0; i < tiers.size(); i++){
				tier = tiers.get(i);
				if(itType != null && tier.getLinguisticType().getLinguisticTypeName().equals(itType)){
					continue;
				}
				
				if(tier.getLinguisticType().getConstraints() != null){
					int stereotype = tier.getLinguisticType().getConstraints().getStereoType();
					if(stereotype == Constraint.TIME_SUBDIVISION || stereotype == Constraint.SYMBOLIC_SUBDIVISION ||
							stereotype == Constraint.INCLUDED_IN){
						if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
							typeList.add(tier.getLinguisticType().getLinguisticTypeName());
						}
					}
				} else {
					if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
						typeList.add(tier.getLinguisticType().getLinguisticTypeName());
					}
				}
			}
		}		
		
		if(typeList.size() == 0){
			typeModel.setValueAt(DEFAULT_VALUE, PARA_INDEX, 1);			
			typeModel.setValueAt(DEFAULT_VALUE, PHRASE_INDEX, 1);
			typeModel.setValueAt(DEFAULT_VALUE, WORD_INDEX, 1);
			typeModel.setValueAt(DEFAULT_VALUE, MORPH_INDEX, 1);			
		} else {
			typeModel.setValueAt(typeList.get(0), PARA_INDEX, 1);
			for(int i=0 ; i < typeList.size(); i++){
				if(typeList.get(i).startsWith(FlexConstants.PARAGR)){
					typeModel.setValueAt(typeList.get(i), PARA_INDEX, 1);
					break;
				}
			}			
			updateTypesForPhrase();
		}
	}	

	/**
	 * Updates the possible linguistic types for the phrase
	 * and makes a default selection
	 */
	private void updateTypesForPhrase(){
		List<String> typeList = elementTypeMap.get(FlexConstants.PHRASE);
		typeList.clear();
		
		TierImpl tier;
		List<TierImpl> tiers;
		
		if(paraCB.isSelected()){
			String paraType = (String) typeTable.getValueAt(PARA_INDEX, 1);
			List<TierImpl> childTiers;
			// phrase is time_subdiv or sym_subdiv or includedIn
			for(int tr=0; tr < transList.size(); tr++){
				tiers = transList.get(tr).getTiersWithLinguisticType(paraType);
				for (int t = 0; t < tiers.size(); t++){
					tier = tiers.get(t);			
					childTiers = tier.getChildTiers();
					for(int i=0; i < childTiers.size(); i++){
						tier = childTiers.get(i);
						int stereotype = tier.getLinguisticType().getConstraints().getStereoType();
						if(stereotype == Constraint.INCLUDED_IN ||
								stereotype == Constraint.TIME_SUBDIVISION ||
										stereotype == Constraint.SYMBOLIC_SUBDIVISION){
							if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
								typeList.add(tier.getLinguisticType().getLinguisticTypeName());
							}
						}
					}
				}
			}
		} else {
			// phrase is time_subdiv or sym_subdiv or top level		
			String itType = (String) typeTable.getValueAt(IT_INDEX, 1);
			
			for(int tr=0; tr < transList.size(); tr++){
				tiers = transList.get(tr).getTiers();
				for(int i=0; i < tiers.size(); i++){
					tier = tiers.get(i);
					
					if(itType != null && tier.getLinguisticType().getLinguisticTypeName().equals(itType)){
						continue;
					}
					
					if(tier.getLinguisticType().getConstraints() != null){
						int sterotype = tier.getLinguisticType().getConstraints().getStereoType();
						if(sterotype == Constraint.INCLUDED_IN ||
							   sterotype == Constraint.TIME_SUBDIVISION || 
							   sterotype == Constraint.SYMBOLIC_SUBDIVISION ){
							if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
								typeList.add(tier.getLinguisticType().getLinguisticTypeName());
							}
						}
					} else {
						if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
							typeList.add(tier.getLinguisticType().getLinguisticTypeName());
						}
					}
				}
			}
		}
		
		if(typeList.size() == 0){
			typeModel.setValueAt(DEFAULT_VALUE, PHRASE_INDEX, 1);
			typeModel.setValueAt(DEFAULT_VALUE, WORD_INDEX, 1);
			typeModel.setValueAt(DEFAULT_VALUE, MORPH_INDEX, 1);
		} else {
			typeModel.setValueAt(typeList.get(0), PHRASE_INDEX, 1);
			for(int i=0 ; i < typeList.size(); i++){
				if(typeList.get(i).startsWith(FlexConstants.PHRASE)){
					typeModel.setValueAt(typeList.get(i), PHRASE_INDEX, 1);
					break;
				}
			}
			updateTypesForWord();
		} 
	}
	
	/**
	 * Updates the possible linguistic types for the word
	 * and makes a default selection
	 */
	private void updateTypesForWord(){
		List<String> typeList = elementTypeMap.get(FlexConstants.WORD);
		typeList.clear();
		
		String phraseType = (String) typeTable.getValueAt(PHRASE_INDEX, 1);
		
		List<TierImpl> tiers;
		List<TierImpl> childTiers;
		TierImpl tier;
		
		// word can be time_subdiv or sym_subdiv 
		for(int tr=0; tr < transList.size(); tr++){
			tiers = transList.get(tr).getTiersWithLinguisticType(phraseType);
			for (int t = 0; t < tiers.size(); t++){
				tier = tiers.get(t);			
				childTiers = tier.getChildTiers();
				for(int i=0; i < childTiers.size(); i++){
					tier = childTiers.get(i);
					int stereotype = tier.getLinguisticType().getConstraints().getStereoType();
					if(stereotype == Constraint.TIME_SUBDIVISION ||
						stereotype == Constraint.SYMBOLIC_SUBDIVISION ||
						stereotype == Constraint.INCLUDED_IN){
						if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
							typeList.add(tier.getLinguisticType().getLinguisticTypeName());
						}
					}
				}
			}
		}
		
		
		if(typeList.size() == 0){
			typeModel.setValueAt(DEFAULT_VALUE, WORD_INDEX, 1);
			typeModel.setValueAt(DEFAULT_VALUE, MORPH_INDEX, 1);
		} else {
			typeModel.setValueAt(typeList.get(0), WORD_INDEX, 1);
			for(int i=0 ; i < typeList.size(); i++){
				if(typeList.get(i).startsWith(FlexConstants.WORD)){
					typeModel.setValueAt(typeList.get(i), WORD_INDEX, 1);
					break;
				}
			}
		}		
		updateTypesForMorph();
	}	
	
	/**
	 * Updates the possible linguistic types for the morph
	 * and makes a default selection
	 */
	private void updateTypesForMorph(){
		List<String> typeList = elementTypeMap.get(FlexConstants.MORPH);
		typeList.clear();	
		
		String wordType = (String) typeTable.getValueAt(WORD_INDEX, 1);
		
		List<TierImpl> tiers;
		List<TierImpl> childTiers;
		TierImpl tier;
		
		// morph can be sym_subdiv 
		for(int tr=0; tr < transList.size(); tr++){
			tiers = transList.get(tr).getTiersWithLinguisticType(wordType);
			for (int t = 0; t < tiers.size(); t++){
				tier = tiers.get(t);			
				childTiers = tier.getChildTiers();
				for(int i=0; i < childTiers.size(); i++){
					tier = childTiers.get(i);
					int stereotype = tier.getLinguisticType().getConstraints().getStereoType();
					if(stereotype == Constraint.SYMBOLIC_SUBDIVISION ||
							stereotype == Constraint.TIME_SUBDIVISION ||
							stereotype == Constraint.INCLUDED_IN){
						if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
							typeList.add(tier.getLinguisticType().getLinguisticTypeName());
						}
					}
				}
			}
		}
		
		if(typeList.size() == 0){
			typeModel.setValueAt(DEFAULT_VALUE, MORPH_INDEX, 1);
		} else {
			typeModel.setValueAt(typeList.get(0), MORPH_INDEX, 1);	
			for(int i=0 ; i < typeList.size(); i++){
				if(typeList.get(i).startsWith(FlexConstants.MORPH)){
					typeModel.setValueAt(typeList.get(i), MORPH_INDEX, 1);
					break;
				}
			}
		}
	}
	
	/**
	 * Deletes all the Element type values
	 * from the given row
	 * 
	 * @param row, row after which the element 
	 *             values has to be deleted
	 */
	private void deleteRowValuesAfter(int row){
		for(int i= row+1 ; i< typeTable.getRowCount(); i++){
			typeTable.setValueAt(null, i, 1);
	   	}
	}
	
	
	/**
	 * Update the types for the given row
	 * 
	 * @param row
	 */
	private void updateTypesForRow(int row){
		switch(row) {	
		case PARA_INDEX:
			updateTypesForParagraph();
			break;
		case PHRASE_INDEX:
			updateTypesForPhrase();
			break;
		case WORD_INDEX:
			updateTypesForWord();
			break;
		case MORPH_INDEX:
			updateTypesForMorph();
			break;
		}
	}
	
	private void loadPreferences(){
		Boolean boolPref = Preferences.getBool(ExportFlexStep1.INTERLINEAR_TEXT, null);
		if (boolPref != null) {
			textCB.setSelected(boolPref);
		}
		
		boolPref = Preferences.getBool(ExportFlexStep1.PARAGRAPH, null);
		if (boolPref != null) {
			paraCB.setSelected(boolPref);
		}
		
		boolPref = Preferences.getBool("MultipleFileFlexExport.Filesfromdomain", null);
		if (boolPref != null) {
			filesFromDomainRB.setSelected(boolPref);
			selectedFilesFromDiskRB.setSelected(!boolPref);
		}	
	}
	
	private void storePreferences(){
		Preferences.set(ExportFlexStep1.INTERLINEAR_TEXT, textCB.isSelected(), null);
		Preferences.set(ExportFlexStep1.PARAGRAPH, paraCB.isSelected(), null);
		Preferences.set("MultipleFileFlexExport.Filesfromdomain", filesFromDomainRB.isSelected(), null);
	}
	
	/**
	  * Cell Editor for the type mapping JTable
	  * 
	  * @author aarsom
	  */
	private class TypeTableCellEditor extends DefaultCellEditor implements ActionListener {    	
	  	private int startEditInOneClick = 1;
	  	private JComboBox comboBox;	  
	  	
	  	private int row;
	  	private String value;
	  	
	   	public TypeTableCellEditor() {
	   		super(new JComboBox());
	   		setClickCountToStart(startEditInOneClick);      		
	   	}
	   	
	   	@Override
		public Component getTableCellEditorComponent(
	   			JTable table,
	   			Object value,
	   			boolean isSelected,
	   			int row,
	   			int column) { 
	   		
	   		this.row = row;
	   		this.value = value.toString();
	   		
	   		List<String> types = elementTypeMap.get(table.getValueAt(row, 0));
	  		comboBox = new JComboBox();  
	   		
	  		comboBox.addItem(SELECT_TYPE);
	   		for(int i=0; i< types.size(); i++){	
				comboBox.addItem(types.get(i));
	   		}
	   		
	  		comboBox.addActionListener(this);

	   		return comboBox;
	   	}
	   	
	   	
	  	@Override
		public Object getCellEditorValue() {  
	   		return comboBox.getSelectedItem();
	   	}

	  	@Override
		public void actionPerformed(ActionEvent e) {
	  		typeTable.editingStopped(new ChangeEvent(this));
			if(value.equals(comboBox.getSelectedItem()) ){
				return;
			}
			
			Object val = comboBox.getSelectedItem();
			if(!val.equals(DEFAULT_VALUE) && !val.equals(SELECT_TYPE)){
				deleteRowValuesAfter(row);
				if(typeTable.getRowCount()-1 > row){
					updateTypesForRow(row+1);
				} 
			} else{
				if(val.equals(SELECT_TYPE)){
					deleteRowValuesAfter(row);
				}
			}
		}
	 }
}

