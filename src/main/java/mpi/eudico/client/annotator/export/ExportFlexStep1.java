package mpi.eudico.client.annotator.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.client.util.SelectEnableObject;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.flex.FlexEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * Step pane for element mapping
 *  
 * 
 * @author aarsom
 * @version Feb, 2013
 */
@SuppressWarnings("serial")
public class ExportFlexStep1 extends StepPane{	
	
	private TranscriptionImpl transcription;
	
	private final String ELEMENT_NAME = ElanLocale.getString("ExportFlexStep1.Table.ElementName");    
	private final String ELEMENT_TYPE = ElanLocale.getString("ExportFlexStep1.Table.Type");    
	private final String SELECT_TYPE = ElanLocale.getString("TranscriptionManager.ComboBoxDefaultString");
	private final String SELECT_TIER = "<"+ElanLocale.getString("TranscriptionManager.SelectTierDlg.DefaultValue")+">";   
	private final String DEFAULT_VALUE = ElanLocale.getString("ExportFlexDialog.DefaultValue");	    
	 
	private JTable typeTable;
	private DefaultTableModel typeModel; 
	
	private JTable tierTable;
	private DefaultTableModel tierModel; 
	
	private JComboBox textCombo;	
	private JCheckBox textCB;
	private JCheckBox paraCB;
	
	/** a hash map of flexType - list<linguistic types> */
	private HashMap<String, List<String>> elementTypeMap;
	
	/** a hash map of flexType - list<linguistic types> */
	private HashMap<String, List<TierImpl>> elementTiersMap;
	
	private String interLinearTextTierName;

	protected Insets globalInset = new Insets(2, 4, 2, 4);
	
	// preferences string    
    public static final String INTERLINEAR_TEXT = "ExportFLExDialog.IncludeInterlinearText";
    public static final String PARAGRAPH = "ExportFLExDialog.IncludeParagraph";

	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcription
	 */
	public ExportFlexStep1(MultiStepPane mp, TranscriptionImpl transcription){
		super(mp);
		this.transcription = transcription;		
		
		elementTypeMap = new HashMap<String, List<String>>();
		elementTypeMap.put(FlexConstants.PARAGR, new ArrayList<String>());
		elementTypeMap.put(FlexConstants.PHRASE, new ArrayList<String>());
		elementTypeMap.put(FlexConstants.WORD, new ArrayList<String>());
		elementTypeMap.put(FlexConstants.MORPH, new ArrayList<String>());
		    
		elementTiersMap = new HashMap<String, List<TierImpl>>();
		elementTiersMap.put(FlexConstants.PARAGR, new ArrayList<TierImpl>());
		elementTiersMap.put(FlexConstants.PHRASE, new ArrayList<TierImpl>());
		elementTiersMap.put(FlexConstants.WORD, new ArrayList<TierImpl>());
		elementTiersMap.put(FlexConstants.MORPH, new ArrayList<TierImpl>());		    
		
		initComponents();
	}
	
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("ExportFlexStep1.Title");
	}

	/**
	 * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
	 */
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
		FlexEncoderInfo encoderInfo = new FlexEncoderInfo();
		
		List<TierImpl> itTiers = new ArrayList<TierImpl>();
				
		if(textCB.isSelected()){
			String tierName = (String) textCombo.getSelectedItem();
			TierImpl itTier = transcription.getTierWithId(tierName);
			if(itTier != null){
				itTiers.add(itTier);
			}
		} 
		
		List<TierImpl> paraList = new ArrayList<TierImpl>();
		List<TierImpl> phraseList = new ArrayList<TierImpl>();
		List<TierImpl> wordList = new ArrayList<TierImpl>();
		List<TierImpl> morphList = new ArrayList<TierImpl>();
		
		Object obj;
		for(int i=0; i < tierTable.getRowCount(); i++){
			obj = tierTable.getValueAt(i, 0);
			if(obj != null && ((SelectEnableObject)obj).isEnabled() && ((SelectEnableObject)obj).isSelected()){
				paraList.add(transcription.getTierWithId(((SelectEnableObject)obj).toString()));
			}
			
			obj = tierTable.getValueAt(i, 1);
			if(obj != null && ((SelectEnableObject)obj).isEnabled() && ((SelectEnableObject)obj).isSelected()){
				phraseList.add(transcription.getTierWithId(((SelectEnableObject)obj).toString()));
			}
			
			obj = tierTable.getValueAt(i, 2);
			if(obj != null && ((SelectEnableObject)obj).isEnabled() && ((SelectEnableObject)obj).isSelected()){
				wordList.add(transcription.getTierWithId(((SelectEnableObject)obj).toString()));
			}
			
			obj = tierTable.getValueAt(i, 3);
			if(obj != null && ((SelectEnableObject)obj).isEnabled() && ((SelectEnableObject)obj).isSelected()){
				morphList.add(transcription.getTierWithId(((SelectEnableObject)obj).toString()));
			}
		}
		
		encoderInfo.setMappingForElement(FlexConstants.IT, itTiers);
		encoderInfo.setMappingForElement(FlexConstants.PARAGR, paraList);
		encoderInfo.setMappingForElement(FlexConstants.PHRASE, phraseList);
		encoderInfo.setMappingForElement(FlexConstants.WORD, wordList);
		encoderInfo.setMappingForElement(FlexConstants.MORPH, morphList);
				
		multiPane.putStepProperty("EncoderInfo", encoderInfo);
		
		return true;
	}	
    
	/**
	 * Updates the button states according to some constraints 
	 * (like everything has to be filled in, consistently)
	 */
	public void updateButtonStates(){		
		
		boolean phraseTier = false;
		SelectEnableObject obj;
		
		for(int i=0; i< tierTable.getRowCount(); i++){
			obj = (SelectEnableObject) tierTable.getValueAt(i,1);
			if(obj != null){
				if(obj.isEnabled() && obj.isSelected()){
					phraseTier = true;
				}
			}
		}

		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, phraseTier);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
	}    
			
	/**
	 * Initializes the components for ui.
	 */
	@Override
	protected void initComponents() {   
		
		textCombo = new JComboBox();		
		textCombo.addItem(SELECT_TIER);		
		
		List<TierImpl> tiers = transcription.getTiers();
		TierImpl t;
		String selectTextTier = null;
		for(int i = 0; i < tiers.size(); i++ ){
			t = tiers.get(i);
			if(t.getLinguisticType().getConstraints() == null){			
				textCombo.addItem(t.getName());
				
				if(selectTextTier == null && t.getName().contains(FlexConstants.IT)){
					selectTextTier = t.getName();
				}
			}
		}		
		
		textCombo.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String item = (String) e.getItem();
					if(item != null){
						updateTable();
					}
			     }
			}
		});	
		
		ChangeListener listener = new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				textCombo.setEnabled(textCB.isSelected());
				updateTable();
			}
		};
		
		textCB = new JCheckBox(ElanLocale.getString("ExportFlexStep1.InterLinearText"));
		textCB.setSelected(true);
		textCB.addChangeListener(listener);		
		
		paraCB = new JCheckBox(ElanLocale.getString("ExportFlexStep1.Paragraph"));
		paraCB.setSelected(true);
		paraCB.addChangeListener(listener);	    	
	    	
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
	       	 	
	       	 	if(row == 0){
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
	    JPanel mappingPanel = new JPanel();
	    mappingPanel.setLayout(new GridBagLayout());
	    mappingPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportFlexStep1.ElementMapping")));
	        
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = globalInset;
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.anchor = GridBagConstraints.NORTHWEST;	
	    mappingPanel.add(textCB, gbc);
			
	    gbc.gridx = 1;
	    mappingPanel.add(textCombo, gbc);
			
	    gbc.gridx = 0;
	    gbc.gridy = 1;	
	    gbc.gridwidth = 2;
	    mappingPanel.add(paraCB, gbc);	
			
	    gbc.gridx = 0;
	    gbc.gridy = 2;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
	    mappingPanel.add(new JScrollPane(typeTable), gbc);
	    
	    //item mapping panel
	    tierModel = new DefaultTableModel(){
	    	@Override
			public boolean isCellEditable(int row, int column) {     	
	    		Object value = getValueAt(row, column);
	   			if(value instanceof SelectEnableObject){	   				
		    		return ((SelectEnableObject)value).isEnabled();
		    	}
		    	
		    	return false;
	   		} 
	    };
	   		
		tierModel.setColumnIdentifiers(new String[] {FlexConstants.PARAGR,  
	   			FlexConstants.PHRASE, FlexConstants.WORD, FlexConstants.MORPH});  
		tierModel.addRow(new Object[]{null, null, null, null});
		
		tierTable = new JTable(tierModel);
		tierTable.setDefaultRenderer(Object.class, new CheckBoxTableCellRenderer());  
		tierTable.setDefaultEditor(Object.class, new TierTableCellEditor());
		tierTable.setShowGrid(true);
		tierTable.setGridColor(Color.BLACK);      
		tierTable.setSelectionBackground(Color.WHITE);        
		tierTable.setRowHeight(typeTable.getRowHeight()+ 5);  
	    
	    
	    JPanel itemPanel = new JPanel();
	    itemPanel.setLayout(new GridBagLayout());
	    itemPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportFlexStep1.TierMapping")));
	        
	    gbc = new GridBagConstraints();
	    gbc.anchor = GridBagConstraints.NORTHWEST;	
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
	    itemPanel.add(new JScrollPane(tierTable), gbc);
				        
	    // main layout
	    setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;		
		gbc.weightx = 1.0;	
		gbc.weighty = 1.0;			
		add(mappingPanel, gbc);	
		
		gbc.gridy = 1;
		add(itemPanel, gbc);
		
		loadPreferences();
	} 
	
	/**
	 * Removes all the Element type values
	 * and disables the table
	 */
	private void intializeElementTypeColum(){
		typeTable.setEnabled(false);
		for(int i=0; i < typeModel.getRowCount(); i++){
			typeModel.setValueAt(null, i, 1);
		}
	}
	
	/**
	 * Update table according to the selected tier options
	 */
	private void updateTable(){
		if(typeTable.isEditing()){
			typeTable.editingCanceled(null);
		}
		
		interLinearTextTierName = null;
		
		String textTier = (String) textCombo.getSelectedItem();		
		
		if(textCB.isSelected()){
			if(textTier == null || textTier.equals(SELECT_TIER)){
				intializeElementTypeColum();				
			} else{
				interLinearTextTierName = textTier;
				typeTable.setEnabled(true);
				
				if(paraCB.isSelected()){	
					updateTypesForParagraph();
				} else {
					typeModel.setValueAt(null, 0, 1);
					updateTypesForPhrase();
				}
			}
		} else {
			typeTable.setEnabled(true);
			if(paraCB.isSelected()){	
				updateTypesForParagraph();
			} else {
				typeModel.setValueAt(null, 0, 1);
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
		
		String itType = null;
		
		TierImpl t;
		if(textCB.isSelected()){
			// paragraph is time_subdiv or sym_subdiv or top level
			t = transcription.getTierWithId(interLinearTextTierName);
			itType = t.getLinguisticType().getLinguisticTypeName();
		}
				
		List<TierImpl> tiers = transcription.getTiers();
		for(int i=0; i < tiers.size(); i++){
			t = tiers.get(i);
			if(itType != null && t.getLinguisticType().getLinguisticTypeName().equals(itType)){
				continue;
			}
			
			if(t.getLinguisticType().getConstraints() != null){
				int stereotype = t.getLinguisticType().getConstraints().getStereoType();
				if(stereotype == Constraint.TIME_SUBDIVISION || stereotype == Constraint.SYMBOLIC_SUBDIVISION ||
						stereotype == Constraint.INCLUDED_IN){
					if(!typeList.contains(t.getLinguisticType().getLinguisticTypeName())){
						typeList.add(t.getLinguisticType().getLinguisticTypeName());
					}
				}
			} else {
				if(!typeList.contains(t.getLinguisticType().getLinguisticTypeName())){
					typeList.add(t.getLinguisticType().getLinguisticTypeName());
				}
			}
		}
		
		if(typeList.size() == 0){
			typeModel.setValueAt(DEFAULT_VALUE, 0, 1);			
			typeModel.setValueAt(DEFAULT_VALUE, 1, 1);
			typeModel.setValueAt(DEFAULT_VALUE, 2, 1);
			typeModel.setValueAt(DEFAULT_VALUE, 3, 1);
			updateTiersForParagraph();
		} else {
			typeModel.setValueAt(typeList.get(0), 0, 1);
			for(int i=0 ; i < typeList.size(); i++){
				if(typeList.get(i).startsWith(FlexConstants.PARAGR)){
					typeModel.setValueAt(typeList.get(i), 0, 1);
					break;
				}
			}
			updateTiersForParagraph();
			updateTypesForPhrase();
		}
	}
	
	/**
	 * Checks if the given tier has any phrase type tiers
	 * 
	 * @return boolean
	 */
	private boolean hasPhraseTier(TierImpl tier){
		if(tier != null){
			List<TierImpl> childTiers = tier.getChildTiers();
			for(int i=0; i < childTiers.size(); i++){
				tier = childTiers.get(i);
				int sterotype = tier.getLinguisticType().getConstraints().getStereoType();
				if(sterotype == Constraint.TIME_SUBDIVISION ||
					sterotype == Constraint.SYMBOLIC_SUBDIVISION ||
					sterotype == Constraint.INCLUDED_IN){
					return true;
				}
			}
		}
		
		return false;
	}	

	/**
	 * Updates tiers for paragraph element
	 */
	private void updateTiersForParagraph(){	
		List<TierImpl> tierList = elementTiersMap.get(FlexConstants.PARAGR);		
		tierList.clear();	
		
		String paraType = (String) typeTable.getValueAt(0, 1);
		if(!paraType.equals(DEFAULT_VALUE) && !paraType.equals(SELECT_TYPE)){			
			List<TierImpl> tiers = transcription.getTiersWithLinguisticType(paraType);

			if((tiers.get(0)).getLinguisticType().getConstraints() == null){
				tierList.addAll(tiers);	
			} else {
				// if it is a child tier				
				TierImpl tier;
				for(int i = 0; i < tiers.size(); i++){
					tier = tiers.get(i);
					if(interLinearTextTierName == null || tier.hasParentTier() && tier.getParentTier().getName().equals(interLinearTextTierName)){
						if(hasPhraseTier(tier)){
							tierList.add(tier);								
						}
					} 		
				}
			}
		}
		
		updateTierTableForElement(FlexConstants.PARAGR);
	}
	
	/**
	 * Updates the possible linguistic types for the phrase
	 * and makes a default selection
	 */
	private void updateTypesForPhrase(){
		List<String> typeList = elementTypeMap.get(FlexConstants.PHRASE);
		typeList.clear();
		
		TierImpl tier;		
		
		if(paraCB.isSelected()){
			// phrase is time_subdiv or sym_subdiv or includedIn			
			String type = (String) typeTable.getValueAt(0, 1);
			List<TierImpl> tiers = transcription.getTiersWithLinguisticType(type);
			
			for (int t = 0; t < tiers.size(); t++){
				tier = tiers.get(t);			
				List<TierImpl> childTiers = tier.getChildTiers();
				for(int i=0; i < childTiers.size(); i++){
					tier = childTiers.get(i);
					int sterotype = tier.getLinguisticType().getConstraints().getStereoType();
					if(sterotype == Constraint.INCLUDED_IN ||
						sterotype == Constraint.TIME_SUBDIVISION ||
						sterotype == Constraint.SYMBOLIC_SUBDIVISION){
						if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
							typeList.add(tier.getLinguisticType().getLinguisticTypeName());
						}
					}
				}
			}
		} else {
			// phrase is time_subdiv or sym_subdiv or included_in or top level			
			String itType = null;
			if(textCB.isSelected()){
				tier = transcription.getTierWithId((String)textCombo.getSelectedItem());
				itType = tier.getLinguisticType().getLinguisticTypeName();
			}
				
			List<TierImpl> tiers = transcription.getTiers();
			for(int i=0; i < tiers.size(); i++){
				tier = tiers.get(i);
				if(itType != null && tier.getLinguisticType().getLinguisticTypeName().equals(itType)){
					continue;
				}
				
				if(tier.getLinguisticType().getConstraints() != null){
					int stereotype = tier.getLinguisticType().getConstraints().getStereoType();
					if(stereotype == Constraint.TIME_SUBDIVISION || 
							stereotype == Constraint.SYMBOLIC_SUBDIVISION || 
							stereotype == Constraint.INCLUDED_IN) {
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
			typeModel.setValueAt(DEFAULT_VALUE, 1, 1);
			typeModel.setValueAt(DEFAULT_VALUE, 2, 1);
			typeModel.setValueAt(DEFAULT_VALUE, 3, 1);
			updateTiersForPhrase();
		} else {
			typeModel.setValueAt(typeList.get(0), 1, 1);
			for(int i=0 ; i < typeList.size(); i++){
				if(typeList.get(i).startsWith(FlexConstants.PHRASE)){
					typeModel.setValueAt(typeList.get(i), 1, 1);
					break;
				}
			}
			updateTiersForPhrase();
			updateTypesForWord();
		} 
	}
	
	/**
	 * Updates the tiers for the phrase element
	 */
	private void updateTiersForPhrase(){	
		List<TierImpl> paraGrapghTierList = elementTiersMap.get(FlexConstants.PARAGR);		
				
		List<TierImpl> tierList = elementTiersMap.get(FlexConstants.PHRASE);
		tierList.clear();
		
		String phraseType = (String) typeTable.getValueAt(1, 1);
		if(!phraseType.equals(DEFAULT_VALUE) && !phraseType.equals(SELECT_TYPE)){
			TierImpl tier;			
			
			if(paraCB.isSelected()){
				List<TierImpl> tiers;
				for(int x =0; x < paraGrapghTierList.size(); x++){
					tiers = paraGrapghTierList.get(x).getChildTiers();
					boolean phraseTierFound = false;
					for(int i = 0; i < tiers.size(); i++){
						tier = tiers.get(i);
						if(tier.getLinguisticType().getLinguisticTypeName().equals(phraseType)){
							tierList.add(tier);
							phraseTierFound = true;
						}
					}
					
					if(!phraseTierFound){
						tierList.add(null);
					}
				}
			}else {
				paraGrapghTierList.clear();
				tierList.addAll(transcription.getTiersWithLinguisticType(phraseType));	
			}
		}		
		
		updateTierTableForElement(FlexConstants.PHRASE);
	}
	
	/**
	 * Updates the possible linguistic types for the word
	 * and makes a default selection
	 */
	private void updateTypesForWord(){
		List<String> typeList = elementTypeMap.get(FlexConstants.WORD);
		typeList.clear();
		
		String type = (String) typeTable.getValueAt(1, 1);
		List<TierImpl> tiers = transcription.getTiersWithLinguisticType(type);
		TierImpl tier;
		for (int t = 0; t < tiers.size(); t++){
			tier = tiers.get(t);			
			List<TierImpl> childTiers = tier.getChildTiers();
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
		
		if(typeList.size() == 0){
			typeModel.setValueAt(DEFAULT_VALUE, 2, 1);
			typeModel.setValueAt(DEFAULT_VALUE, 3, 1);
		} else {
			typeModel.setValueAt(typeList.get(0), 2, 1);
			for(int i=0 ; i < typeList.size(); i++){
				if(typeList.get(i).startsWith(FlexConstants.WORD)){
					typeModel.setValueAt(typeList.get(i), 2, 1);
					break;
				}
			}
		}
		
		updateTiersForWord();
		updateTypesForMorph();
	}
	
	/**
	 * Updates tiers for word element
	 */
	private void updateTiersForWord(){	
		List<TierImpl> phraseTierList = elementTiersMap.get(FlexConstants.PHRASE);	
		
		List<TierImpl> tierList = elementTiersMap.get(FlexConstants.WORD);
		tierList.clear();
		
		String wordType = (String) typeTable.getValueAt(2, 1);
		if(!wordType.equals(DEFAULT_VALUE) && !wordType.equals(SELECT_TYPE)){
			List<TierImpl> tiers;
			TierImpl tier;		
			for(int x =0; x < phraseTierList.size(); x++){				
				boolean tierFound = false;
				tiers = phraseTierList.get(x).getChildTiers();
				for(int i = 0; i < tiers.size(); i++){
					tier = tiers.get(i);
					if(tier.getLinguisticType().getLinguisticTypeName().equals(wordType)){
						tierList.add(tier);
						tierFound = true;
					}
				}				
				
				if(!tierFound){
					tierList.add(null);
				}
			}
		}
		updateTierTableForElement(FlexConstants.WORD);
	}
	
	/**
	 * Updates the possible linguistic types for the morph
	 * and makes a default selection
	 */
	private void updateTypesForMorph(){
		List<String> typeList = elementTypeMap.get(FlexConstants.MORPH);
		typeList.clear();			
		
		String type = (String) typeTable.getValueAt(2, 1);		
		List<TierImpl> tiers = transcription.getTiersWithLinguisticType(type);
		TierImpl tier;
		for (int t = 0; t < tiers.size(); t++){
			tier = tiers.get(t);			
			List<TierImpl> childTiers = tier.getChildTiers();
			for(int i=0; i < childTiers.size(); i++){
				tier = childTiers.get(i);
				int stereotype = tier.getLinguisticType().getConstraints().getStereoType();
				// HS Oct 2013 though not recommended, also accept time subdivision. The time info will be lost.
				if(stereotype == Constraint.SYMBOLIC_SUBDIVISION || stereotype == Constraint.TIME_SUBDIVISION
						|| stereotype == Constraint.INCLUDED_IN){
					if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
						typeList.add(tier.getLinguisticType().getLinguisticTypeName());
					}
				}
			}
		}
		
		if(typeList.size() == 0){
			typeModel.setValueAt(DEFAULT_VALUE, 3, 1);
		} else {
			typeModel.setValueAt(typeList.get(0), 3, 1);
			for(int i=0 ; i < typeList.size(); i++){
				if(typeList.get(i).startsWith(FlexConstants.MORPH)){
					typeModel.setValueAt(typeList.get(i), 3, 1);
					break;
				}
			}
		}

		updateTiersForMorph();
	}
	
	/**
	 * Updates tiers for word element
	 */
	private void updateTiersForMorph(){	
		List<TierImpl> wordTierList = elementTiersMap.get(FlexConstants.WORD);	
		
		List<TierImpl> tierList = elementTiersMap.get(FlexConstants.MORPH);
		tierList.clear();
		
		String morphType = (String) typeTable.getValueAt(3, 1);
		if(!morphType.equals(DEFAULT_VALUE) && !morphType.equals(SELECT_TYPE)){
			for (TierImpl tier : wordTierList) {
				boolean tierFound = false;
				if(tier != null){
					for (TierImpl ctier : tier.getChildTiers()) {
						if(ctier.getLinguisticType().getLinguisticTypeName().equals(morphType)){
							tierList.add(ctier);
							tierFound = true;
						}
					}
				}
				
				if(!tierFound){
					tierList.add(null);
				}
			}
		}
		
		updateTierTableForElement(FlexConstants.MORPH);
	}
	
	private void updateTierTableForElement(String itemType){
		List<TierImpl> tierList = elementTiersMap.get(itemType);
					
		int column = 0;
		if(itemType.equals(FlexConstants.PARAGR)){
			column = 0;
			clearTierTable();
		} else if(itemType.equals(FlexConstants.PHRASE)){
			column = 1;			
			if(!paraCB.isSelected()){
				clearTierTable();
			}
		} else if(itemType.equals(FlexConstants.WORD)){
			column = 2;
		} else if(itemType.equals(FlexConstants.MORPH)){
			column = 3;
		}
		
		if(tierList.size() > 0){
			int row = tierList.size();
						
			for(int i= tierTable.getRowCount(); i < row; i++){
				tierModel.addRow(new Object[]{null, null, null, null});
			}
			
			Tier t;
			for(int i= 0; i< tierList.size(); i++){
				t = tierList.get(i);
				if(t != null){
					tierModel.setValueAt(new SelectEnableObject(tierList.get(i).getName(), true), i, column);
				} else {
					tierModel.setValueAt(null, i, column);
				}
			}
		} else {
			for(int i= 0; i < tierTable.getRowCount(); i++){
				for(int c = column; c <= 3; c++){
					tierModel.setValueAt(null, i, c);
				}
			}
		}
		
		updateButtonStates();
	}
	
	/**
	 * Deletes all the Element type values
	 * from the given row
	 * 
	 * @param row, row after which the element 
	 *             values has to be deleted
	 */
	private void clearTierTable(){
		while(tierTable.getRowCount() > 0){
			tierModel.removeRow(tierTable.getRowCount() -1);
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
		case 0:
			updateTypesForParagraph();
			break;
		case 1:
			updateTypesForPhrase();
			break;
		case 2:
			updateTypesForWord();
			break;
		case 3:
			updateTypesForMorph();
			break;
		}
	}
	
	/**
	 * Update the tiers for the given row
	 * 
	 * @param row
	 */
	private void updateTiersForRow(int row){
		switch(row) {	
		case 0:
			updateTiersForParagraph();
			break;
		case 1:
			updateTiersForPhrase();
			break;
		case 2:
			updateTiersForWord();
			break;
		case 3:
			updateTiersForMorph();
			break;
		}
	}
	
	private void loadPreferences(){
		Boolean val = Preferences.getBool(INTERLINEAR_TEXT, null);
		if (val != null) {
			textCB.setSelected(val);
		}
		
		val = Preferences.getBool(PARAGRAPH, null);
		if (val != null) {
			paraCB.setSelected(val);
		}
				
	}
	
	private void storePreferences(){
		Preferences.set(INTERLINEAR_TEXT, textCB.isSelected(), null);
		Preferences.set(PARAGRAPH, paraCB.isSelected(), null);		
	}
	
	/**
	  * Cell Editor for the tier mapping JTable
	  * 
	  * @author aarsom
	  */
	private class TierTableCellEditor extends DefaultCellEditor implements ActionListener{
		private int startEditInOneClick = 1;
	  	private JCheckBox checkBox;	  
	  	private SelectEnableObject selObject;
	  	private int column;
	  	private int row;
	  	
		public TierTableCellEditor() {
	   		super(new JCheckBox());
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
	   		this.column = column;
	   		
	   		if(value instanceof SelectEnableObject){
	   			selObject = (SelectEnableObject)value;
	   			checkBox = new JCheckBox();
	   			checkBox.setSelected(selObject.isSelected());
	   			checkBox.setText(selObject.getValue().toString());
	   			checkBox.addActionListener(this);
		   		return checkBox;
	   		}

	   		return null;
	   	}
	   	
	   	
	  	@Override
		public Object getCellEditorValue() { 
	   		return selObject;
	   	}
	  	
	  	@Override
		public void actionPerformed(ActionEvent e) {
	  		selObject.setSelected(checkBox.isSelected());
	  		validateOtherTiers();
	  		updateButtonStates();
	  	}
	  	
	  	private void validateOtherTiers(){
	  		SelectEnableObject obj;
	  		if(column < 3){
	  			for(int i = column+1; i <= 3; i++){
	  				obj = (SelectEnableObject) tierModel.getValueAt(row, i);
	  				if(obj != null){
	  					obj.setEnabled(selObject.isSelected());
	  				}
	  			}
	  			
	  			tierTable.repaint();
	  		}
	  	}
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
				updateTiersForRow(row);
				deleteRowValuesAfter(row);
				if(typeTable.getRowCount()-1 > row){
					updateTypesForRow(row+1);
				} 
			} else{
				if(val.equals(SELECT_TYPE)){
					deleteRowValuesAfter(row);
				}
				updateTiersForRow(row);
			}
		}
	 }
}