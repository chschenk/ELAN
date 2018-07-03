package mpi.eudico.client.annotator.tiersets;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.FilesTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.gui.TranscriptionTierSortAndSelectPanel;
import mpi.eudico.client.annotator.tier.DisplayableContentTableModel;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;

@SuppressWarnings("serial")
public class AddTiersDlg extends ClosableDialog implements ActionListener {
	
	private AbstractTierSortAndSelectPanel tierSelectPanel;
	private JButton addButton, cancelButton;
	
	private JPanel fileSelectionPanel;
	private JRadioButton currentlyOpenedFileRB,selectedFilesFromDiskRB, filesFromDomainRB;
	private JButton selectFilesBtn, selectDomainBtn;
	
	private String prefixString = "TierSet.AddTiersDlg";
	
	private List<String> selectedTierList = null;
	
	private ManageTierSetDlg manageTSDlg;
	
	private Insets globalInsets = new Insets(2,4,2,4);
	private JPanel tierSelectionPanel;
	private JTextArea textArea;
	
	private List<String> openedFileNames;
	
	public AddTiersDlg(Dialog owner, ManageTierSetDlg manageTierSetDlg, List<String> tierList){
		super(owner, ElanLocale.getString("TierSet.Title.AddTiers"), true);
		
		manageTSDlg = manageTierSetDlg;
		
		selectedTierList = new ArrayList<String>();
//		if(tierList != null){
//			selectedTierList.addAll(tierList);
//		}
		initComponents();
		WindowLocationAndSizeManager.postInit(this, prefixString);
	}
	
	public void closeDialog(){
		storePreferences();
		WindowLocationAndSizeManager.storeLocationAndSizePreferences(this, prefixString);
		setVisible(false);
		dispose();
	}
	
	/**
	 * Initializes the upper part containing file selection
	 */
	protected void initFileSelectionPanel(){			
		//panel
		fileSelectionPanel = new JPanel(new GridBagLayout());
		fileSelectionPanel.setBorder(new TitledBorder( ElanLocale.getString("FileAndTierSelectionStepPane.Panel.Title.FileSelection")));
		
		//create all radio buttons
		RadioButtonHandler radioButtonListener = new RadioButtonHandler();
		currentlyOpenedFileRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.CurrentlyOpenedFile"), false);
		currentlyOpenedFileRB.addActionListener(radioButtonListener);
		selectedFilesFromDiskRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromFileBrowser"), false);
		selectedFilesFromDiskRB.addActionListener(radioButtonListener);
		filesFromDomainRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromDomain"), false);
		filesFromDomainRB.addActionListener(radioButtonListener);
		
		//add radio buttons to button group
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(currentlyOpenedFileRB);
		buttonGroup.add(selectedFilesFromDiskRB);
		buttonGroup.add(filesFromDomainRB);
		
		//create all buttons		
		ButtonHandler buttonHandler = new ButtonHandler();
		selectFilesBtn = new JButton(ElanLocale.getString("Button.Browse"));
		selectFilesBtn.addActionListener(buttonHandler);
		selectFilesBtn.setEnabled(false);
		
		selectDomainBtn = new JButton(ElanLocale.getString("FileAndTierSelectionStepPane.Button.Domain"));
		selectDomainBtn.addActionListener(buttonHandler);
		selectDomainBtn.setEnabled(false);
		
		//handle multiple file case vs. single file case
		if( manageTSDlg.getTranscription() == null ){
			//MULTIPLE_FILES: disable all radio buttons dealing with single file
			currentlyOpenedFileRB.setEnabled(false);
		} 
		
		//add buttons to panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 30, 0, 10);			
		fileSelectionPanel.add(currentlyOpenedFileRB, gbc);
		
		//files from disk
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		fileSelectionPanel.add(selectedFilesFromDiskRB, gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		fileSelectionPanel.add(selectFilesBtn, gbc);
		
		//files from domain
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		fileSelectionPanel.add(filesFromDomainRB, gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		fileSelectionPanel.add(selectDomainBtn, gbc);
		
		readPreferences();
	}
	
	/**
	 * Initializes tier table pane
	 */
	protected void initTierSelectionPanel(){
		//panel		
		tierSelectionPanel = new JPanel(new GridBagLayout());		
		tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString("MultiFileExport.Panel.Title.TierSelection")));		
		
		textArea = new JTextArea(ElanLocale.getString("FileAndTierSelectionStepPane.Panel.Title.FileSelection"));
		textArea.setEditable(false);
		textArea.setBorder(new LineBorder(Color.LIGHT_GRAY));
		
		//tierSelectPanel = manageTSDlg.getTierSelectPanel();
		if(tierSelectPanel == null){
			if(currentlyOpenedFileRB.isSelected()){
				initializeTierSelectPanel(null);
			} else {
				if(openedFileNames != null && openedFileNames.size() > 0){
					initializeTierSelectPanel(this.getMultipleFiles(openedFileNames));
				}
			}
		} else {
			tierSelectPanel.setSelectedTiers(selectedTierList);
		}
	
		//add table
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = globalInsets;
		
		if(tierSelectPanel != null){
			tierSelectionPanel.add(tierSelectPanel, gbc);		
		} else {
			tierSelectionPanel.add(textArea, gbc);		
		}
	}
	
	private void removeTierSelectPanel(){
		if(tierSelectPanel != null){
			tierSelectionPanel.remove(tierSelectPanel);
			tierSelectPanel = null;
		} 
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = globalInsets;
		tierSelectionPanel.add(textArea, gbc);	
		
		updateButtonStates();
		
		validate();
		repaint();
	}
	
	/**
	 * Initialize the advanced tier select panel
	 * 
	 * @param files, the files to be used for export
	 */
	private void initializeTierSelectPanel(ArrayList<File> files){
		if(tierSelectPanel != null){
			tierSelectionPanel.remove(tierSelectPanel);
		}else {
			tierSelectionPanel.remove(textArea);
		}
		
		if(files == null){
			tierSelectPanel = new TranscriptionTierSortAndSelectPanel(manageTSDlg.getTranscription(), null, selectedTierList,false, true){
				@Override
				protected void initTables(){
					super.initTables();
					
					TableModelListener listener = new ModelClickedHandler();
					model.addTableModelListener(listener);
					typeModel.addTableModelListener(listener);
					partModel.addTableModelListener(listener);
					annotModel.addTableModelListener(listener);
					langModel.addTableModelListener(listener);
				}
			};
		} else {
			tierSelectPanel = new ExtentedFilesTierSortAndSelectPanel(files, null, selectedTierList, false, true);
			
		}
		
		//add table
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = globalInsets;
		tierSelectionPanel.add(tierSelectPanel, gbc);	
		
		updateButtonStates();
		
		validate();
		repaint();
	}
	
	private void initComponents(){		
		
		
		addButton = new JButton(ElanLocale.getString("Button.Add"));
		addButton.addActionListener(this);
		
		cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
		cancelButton.addActionListener(this);
		
		initFileSelectionPanel();		
		initTierSelectionPanel();		
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,2));
		buttonPanel.add(addButton);
		buttonPanel.add(cancelButton);		
		
		getContentPane().setLayout(new GridBagLayout());
		
		int y = 0; 
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.insets = globalInsets;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1.0;
		getContentPane().add(fileSelectionPanel, gbc);			
		
		gbc.gridy = ++y;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		getContentPane().add(tierSelectionPanel, gbc);
		
		gbc.gridy = ++y;
		gbc.anchor = GridBagConstraints.SOUTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		getContentPane().add(buttonPanel, gbc);
		
		updateButtonStates();
	}
	
	public void readPreferences(){
    	
    	boolean currentFile = false;
    	boolean domain = false;
    	boolean disk = false;
    	
    	Object val = Preferences.get("TierSetDlgFileSelection.CurrentTranscription", null);
    	if(val instanceof Boolean){
    		currentFile = ((Boolean)val).booleanValue();
    	} 
    	
    	val = Preferences.get("TierSetDlgFileSelection.Filesfromdomain", null);
    	if(val instanceof Boolean){
    		domain = ((Boolean)val).booleanValue();
    	}
    	
    	val = Preferences.get("TierSetDlgFileSelection.FilesfromDisk", null);
    	if(val instanceof Boolean){
    		disk = ((Boolean)val).booleanValue();
    	}
    	
    	if(domain || disk){
    		filesFromDomainRB.setSelected(domain);
    		selectDomainBtn.setEnabled(domain);
    		selectFilesBtn.setEnabled(disk);
    		selectedFilesFromDiskRB.setSelected(disk);
    	} else if(manageTSDlg.getTranscription() != null){
    		if(currentFile){
    			currentlyOpenedFileRB.setSelected(currentFile);
    		}
    	}
    	
    	val = Preferences.get("TierSetDlg.SelectedFiles" ,null);
		if(val instanceof List){
			openedFileNames = (List<String>)val;
		}
    }
	
	public void storePreferences(){
		Preferences.set("TierSetDlgFileSelection.Filesfromdomain", filesFromDomainRB.isSelected(), null);
		Preferences.set("TierSetDlgFileSelection.FilesfromDisk", selectedFilesFromDiskRB.isSelected(), null);
		Preferences.set("TierSetDlgFileSelection.CurrentTranscription", currentlyOpenedFileRB.isSelected(), null);
		
		Preferences.set("TierSetDlg.SelectedFiles" ,openedFileNames, null);
	}
	
	
	public List<String> getSelectedTierNames(){
		return selectedTierList;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		if(source == addButton){
			selectedTierList = tierSelectPanel.getSelectedTiers();
			closeDialog();
		} else if(source == cancelButton){
			selectedTierList.clear();
			closeDialog();
		} 
	}
	
	protected ArrayList<File> getMultipleFiles(List<String> fileNames){
		if(fileNames == null){
			return null;
		}
		
		
		if (fileNames.size() > 0) {
			ArrayList<File> files = new ArrayList<File>();
	        File f;
	        for (int i = 0; i < fileNames.size(); i++) {
	        	f = new File(fileNames.get(i));
	        	if (f.isFile() && f.canRead()) {
	        		files.add(f);
	        	} else if (f.isDirectory() && f.canRead()) {
	        		addFiles(f, files);
	        	}
	        }
	        
	        if(files.size() > 0){
	        	return files;
	        }
		}		
		return null;
	}
	
	
    /**
     * Scans the folders for eaf files and adds them to files list,
     * recursively.
     *
     * @param dir the  or folder
     * @param files the list to add the files to
     */
    protected void addFiles(File dir, ArrayList<File> files) {
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
                    	
                    	if(!files.contains(allSubs[i])) {
							files.add(allSubs[i]);
						}
                    }
                }
            }
        }
    }
    
    /**
     * Scans the folders for eaf files and adds them to files list,
     * recursively.
     *
     * @param dir the  or folder
     * @param files the list to add the files to
     */
    protected void addFileNames(File dir, List<String> files) {
        if ((dir == null) && (files == null)) {
            return;
        }

        File[] allSubs = dir.listFiles();

        for (int i = 0; i < allSubs.length; i++) {
            if (allSubs[i].isDirectory() && allSubs[i].canRead()) {
            	addFileNames(allSubs[i], files);
            } else {
                if (allSubs[i].canRead()) {
                    if (allSubs[i].getName().toLowerCase()
                                      .endsWith(FileExtension.EAF_EXT[0])) {
                        // test if the file is already there??
                    	
                    	if(!files.contains(allSubs[i].getAbsolutePath())) {
							files.add(allSubs[i].getAbsolutePath());
						}
                    }
                }
            }
        }
    }
    
    /**
	 * Shows a multiple file chooser dialog, checks if every selected file exists
	 * and stores the selected files in private variable eafFiles
	 * 
	 * @return 
	 */
    private List<String> showMultiFileChooser(){
		List<String> fileNames = null;	
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowMultiFileDialog(ElanLocale.getString("ExportDialog.Multi"), FileChooser.GENERIC,
        		FileExtension.EAF_EXT, "LastUsedEAFDir");
        
        Object[] objects = chooser.getSelectedFiles();     

		if (objects != null) {	  
			if (objects.length > 0) {           
				fileNames = new ArrayList<String>();
                for (Object object : objects) {
                    if (fileNames.contains(object) == false) {
                        fileNames.add("" + object);
                    }
                }
            }
		}		
		return fileNames;
	}
	
    /**
	 * Updates the button states according to some constraints 
	 */
	public void updateButtonStates(){		
		if(tierSelectPanel != null && tierSelectPanel.getSelectedTiers().size() > 0){
			addButton.setEnabled(true);
		} else{
			addButton.setEnabled(false);
		}
	}
	
	private class ModelClickedHandler implements TableModelListener {
		@Override
		public void tableChanged(TableModelEvent e){
			updateButtonStates();
		}
	}

	private class ButtonHandler implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			updateButtonStates();
			if( e != null ){
				JButton button = (JButton) e.getSource();
				
				if( button == selectFilesBtn ){
					List<String> filenames = showMultiFileChooser();
					if( filenames != null && !filenames.isEmpty() ){
						initializeTierSelectPanel(getMultipleFiles(filenames));
						openedFileNames = filenames;
					}
				}else if( button == selectDomainBtn ){
					//create domain dialog
					MFDomainDialog domainDialog = new MFDomainDialog(ELANCommandFactory.getRootFrame(null), ElanLocale.getString("ExportDialog.Multi"), true);
					domainDialog.setVisible(true);
					
					//when domain is selected, get the search paths
					List<String> searchPaths = domainDialog.getSearchPaths();
					List<String> searchDirs = domainDialog.getSearchDirs();
					
					
					List<String> filenames = new ArrayList<String>();
					ArrayList<File> files = null;
					
					//check if domain contains files
					if( !searchPaths.isEmpty() ){
						files = getMultipleFiles(searchPaths);						
						filenames.addAll(searchPaths);
					}
				
					if( !searchDirs.isEmpty() ){
						//load the files in the selected domain		
						if(files == null){
							files = getMultipleFiles(searchDirs);
						}else {
							files.addAll(getMultipleFiles(searchDirs));
						}
						
					    File f;
					    for (int i = 0; i < searchDirs.size(); i++) {
					      	f = new File(searchDirs.get(i));
					       	if (f.isFile() && f.canRead()) {
					       		if(!filenames.contains(searchDirs.get(i))){
					       			filenames.add(searchDirs.get(i));	
					       		}
					       	} else if (f.isDirectory() && f.canRead()) {
					       		addFileNames(f, filenames);
					       	}
					    }	
					}
					
					if(files != null){
						initializeTierSelectPanel(files);	
						openedFileNames = filenames;
					}
				}
			}				
		}		
	}
	
	private class RadioButtonHandler implements ActionListener{
		private JRadioButton previouslySelectedRadioButton;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton rb = (JRadioButton) e.getSource();
			
			if( rb == currentlyOpenedFileRB ){		
				if( previouslySelectedRadioButton != rb ){
					previouslySelectedRadioButton = rb;
					openedFileNames = null;
					initializeTierSelectPanel(null);
				}
				
				// update transcription sort panel
				selectFilesBtn.setEnabled(false);
				selectDomainBtn.setEnabled(false);
			}
			
			else if( rb == selectedFilesFromDiskRB ){
				if( previouslySelectedRadioButton != rb ){
					previouslySelectedRadioButton = rb;
					openedFileNames = null;
					removeTierSelectPanel();
				}
				
				selectFilesBtn.setEnabled(true);
				selectDomainBtn.setEnabled(false);
			}else if( rb == filesFromDomainRB ){
				if( previouslySelectedRadioButton != rb ){
					previouslySelectedRadioButton = rb;
					openedFileNames = null;
					removeTierSelectPanel();
				}
				selectFilesBtn.setEnabled(false);
				selectDomainBtn.setEnabled(true);
			}
		}
	}
	
	private class ExtentedFilesTierSortAndSelectPanel extends FilesTierSortAndSelectPanel{

		public ExtentedFilesTierSortAndSelectPanel(ArrayList<File> files,
				List<String> tierOrder, List<String> selectedTiers, boolean allowReOrdering, boolean allowSorting) {
			super(files, tierOrder, selectedTiers, allowReOrdering, allowSorting);
		}
		
		@Override
		protected void initTables(){
			 if (allTierNames == null) {
				 allTierNames = new ArrayList<String>();
			 }
		        
			 LoadTableThread lt = new LoadTableThread(files, tierTable);
		     lt.start();
			
			TableModelListener listener = new ModelClickedHandler();
			model.addTableModelListener(listener);
			typeModel.addTableModelListener(listener);
			partModel.addTableModelListener(listener);
			annotModel.addTableModelListener(listener);
			langModel.addTableModelListener(listener);
		}
		
		private class LoadTableThread extends Thread{	
			protected List<File> files;
			protected JTable tierTable;
			private DisplayableContentTableModel displaymodel;
			
			public LoadTableThread(List<File> files, JTable tierTable){
				this.files = files;
				this.tierTable = tierTable;
				String[] msg;
				
				if(files == null || files.size() == 0){
					msg =  new String[]{ElanLocale.getString("FileAndTierSelectionStepPane.Panel.Title.FileSelection")};
				} else {
					
					msg = new String[]{ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part1"), "0 " + 
							ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part2") + " " + files.size() + " " + 
							ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part3") + " (0%)" };
				}
				displaymodel = new DisplayableContentTableModel(msg);
				displaymodel.connectTable(tierTable);
				tierTable.setModel(displaymodel);
			}
			
			@Override
			public void run(){
				tierTypeMap.clear();
		    	tierParticipantMap.clear();
		    	tierAnnotatorMap.clear();
		    	tierLanguageMap.clear();
		    	
		    	if(files != null){
		    		List<TierImpl> pts = null;
			        EAFSkeletonParser parser = null;
			        File file;
			        String path;

			        for (int i = 0; i < files.size(); i++) {
			        	file = files.get(i);
			        	if (file == null) {
			        		continue;
			        	}
			        	path = file.getAbsolutePath();

			            try {
			            	parser = new EAFSkeletonParser(path);
			                parser.parse();
			                pts = parser.getTiers();

			                TierImpl tier;
			                String value;
			                List<String> list;
			                String tierName;

			                for (int j = 0; j < pts.size(); j++) {
			                	tier = (TierImpl) pts.get(j);
			                	tierName = tier.getName();

			                    if (!allTierNames.contains(tierName)) {
			                    	allTierNames.add(tierName);
			                    }
			                    // store the root tiers separately
			                    if (tier.getParentTier() == null) {
			                    	if (!rootTiers.contains(tierName)) {
			                    		rootTiers.add(tierName);
			                    	}
			                    }
			                    value = tier.getParticipant();
			            		if (value.isEmpty()) {
			            			value = NOT_SPECIFIED;
			            		}
			                    
			            		if (tierParticipantMap.get(value) == null) {
			            			tierParticipantMap.put(value, new ArrayList<String>());
			            		}
			        			
			            		list = tierParticipantMap.get(value);
			            		if (!list.contains(tierName)) {
			            			list.add(tierName);
			            		}
			            		
			            		value = tier.getAnnotator();
			        			if (value.isEmpty()) {
			        				value = NOT_SPECIFIED;
			        			}
			        			
			        			if (tierAnnotatorMap.get(value) == null) {
			            			tierAnnotatorMap.put(value, new ArrayList<String>());
			            		}
			        			
			        			list = tierAnnotatorMap.get(value);
			            		if (!list.contains(tierName)) {
			            			list.add(tierName);
			            		}    		
			           			
			           			value = tier.getLinguisticType().getLinguisticTypeName();
			           			if (tierTypeMap.get(value) == null) {
			           				tierTypeMap.put(value, new ArrayList<String>());
			            		}
			           			
			           			list = tierTypeMap.get(value);
			            		if (!list.contains(tierName)) {
			            			list.add(tierName);
			            		}
			            		// store the types for root tiers separately
			            		if (tier.getParentTier() == null) {
			            			if (!rootTypes.contains(value)) {
			            				rootTypes.add(value);
			            			}
			            		}

			            		value = tier.getLangRef();
			            		if (value == null || value.isEmpty()) {
			            			value = NOT_SPECIFIED;
			            		}
			                    
			            		if (tierLanguageMap.get(value) == null) {
			            			tierLanguageMap.put(value, new ArrayList<String>());
			            		}
			        			
			            		list = tierLanguageMap.get(value);
			            		if (!list.contains(tierName)) {
			            			list.add(tierName);
			            		}
			            		
			                }
			            } catch (ParseException pe) {
			                ClientLogger.LOG.warning(pe.getMessage());
			                    //pe.printStackTrace();
			            } catch (Exception ex) {
			                ClientLogger.LOG.warning("Could not load file: " + path);
			            }
			            
			            final String msg = (i+1) + " " + ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part2") + " " + files.size() + " " + 
								ElanLocale.getString("FileAndTierSelectionStepPane.Message2.Part3") + " (" + Math.round((i+1)/((float)files.size())*100.0f) + "%)";
						
			            SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								displaymodel.updateMessage(1, msg);
							}
						});
			        }
			   }
		        
		        try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							//update table
							if( allTierNames.isEmpty() ){															
								displaymodel.setValueAt( ElanLocale.getString("FileAndTierSelectionStepPane.Message1"), 0, 0);
							}else{
								tierTable.setModel(model);
								if(tierTable instanceof TierExportTable) {
									((TierExportTable) tierTable).init(ListSelectionModel.SINGLE_INTERVAL_SELECTION, false);
								} else {
									ClientLogger.LOG.warning("tierTable is not of class TierExportTable");
								}
								
								for (int i = model.getRowCount() - 1; i >= 0; i--) {
								   	model.removeRow(i);
								}
							
								for (int i = 0; i < allTierNames.size(); i++) {
							    	if(selectedTierNames.size() > 0){
							    		if(selectedTierNames.contains(allTierNames.get(i))){
							    			model.addRow(Boolean.TRUE, allTierNames.get(i));
							    		} else {
							    			model.addRow(Boolean.FALSE, allTierNames.get(i));
							    		}
							    	} else {
							    		model.addRow(Boolean.FALSE, allTierNames.get(i));
							    	}
							    }
							    
							    if (model.getRowCount() > 0) {
							    	tierTable.setRowSelectionInterval(0, 0);
							    }
							    
							    fillModel(typeModel, tierTypeMap.keySet());
							    fillModel(partModel, tierParticipantMap.keySet());
							    fillModel(annotModel, tierAnnotatorMap.keySet());
							    fillModel(langModel, tierLanguageMap.keySet());
							}
							
							updateButtonStates();
							
						}});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
}
