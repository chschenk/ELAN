package mpi.eudico.client.annotator.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.client.util.SelectEnableObject;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.flex.FlexEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * Step pane for element-item configuration
 *  
 * 
 * @author aarsom
 * @version Feb, 2013
 */
@SuppressWarnings("serial")
public class ExportFlexStep2 extends StepPane{	
	
	private TranscriptionImpl transcription;
	
	private final String SELECT_TYPE = ElanLocale.getString("TranscriptionManager.ComboBoxDefaultString");
	private final String DEFAULT_VALUE = ElanLocale.getString("ExportFlexDialog.DefaultValue");
	
	private final String MORPH_TYPE = FlexConstants.MORPH+"-"+FlexConstants.TYPE;

	    
	private FlexEncoderInfo encoderInfo;
	
	private JCheckBox morphTypeCB;
	private JComboBox morphTypeCombo;
	    
	private JTable itemTable;
	private DefaultTableModel itemModel; 
	
	private JTable tierTable;
	private DefaultTableModel tierModel; 
	
	/** a hash map of flexType - list<linguistic types> */
	private HashMap<String, List<String>> itemTypeMap;
	
	/** a hash map of flexType - list<tier> */
	private HashMap<String, List<TierImpl>> itemTiersMap;
	
	/** list<tier> morph type tiers*/
	private List<TierImpl> morphTypeTiersList;
	
	private String morphTypeSelected;
	
	protected Insets globalInset = new Insets(2, 4, 2, 4);
	
	private String DEL = "_";
	
	private ActionHandler actionHandler;
	
	//Preference string
	public static final String MORPHTYPE = "ExportFlex.MorphTypeSelected";

	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcripton
	 */
	public ExportFlexStep2(MultiStepPane mp, TranscriptionImpl transcription){
		super(mp);
		this.transcription = transcription;		
		
		itemTypeMap = new HashMap<String, List<String>>();
		itemTypeMap.put(FlexConstants.IT, new ArrayList<String>());
		itemTypeMap.put(FlexConstants.PHRASE, new ArrayList<String>());
		itemTypeMap.put(FlexConstants.WORD, new ArrayList<String>());
		itemTypeMap.put(FlexConstants.MORPH, new ArrayList<String>());
		    
		itemTiersMap = new HashMap<String, List<TierImpl>>();
		itemTiersMap.put(FlexConstants.IT, new ArrayList<TierImpl>());
		itemTiersMap.put(FlexConstants.PHRASE, new ArrayList<TierImpl>());
		itemTiersMap.put(FlexConstants.WORD, new ArrayList<TierImpl>());
		itemTiersMap.put(FlexConstants.MORPH, new ArrayList<TierImpl>());
		
		morphTypeTiersList = new ArrayList<TierImpl>();
		
		actionHandler = new ActionHandler();
		
		initComponents();
	}
	
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("ExportFlexStep2.Title");
	}

	/**
	 * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
	 */
	@Override
	public void enterStepForward(){
		encoderInfo = (FlexEncoderInfo) multiPane.getStepProperty("EncoderInfo");	
		
		updateItemTypesFor(FlexConstants.IT);
		
		updateItemTypesFor(FlexConstants.PHRASE);

		updateItemTypesFor(FlexConstants.WORD);

		updateItemTypesFor(FlexConstants.MORPH);
		
		updateTypesForMorphType();
		
		updateButtonStates();
	}
	
	
	
	@Override
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	@Override
	public boolean leaveStepForward(){	
		storePreferences();
		// the morph types has to be checked
		
		List<TierImpl> itTiers = new ArrayList<TierImpl>();
		List<TierImpl> phraseList = new ArrayList<TierImpl>();
		List<TierImpl> wordList = new ArrayList<TierImpl>();
		List<TierImpl> morphList = new ArrayList<TierImpl>();
		morphTypeTiersList.clear();
		
		Object obj;
		for(int i=0; i < tierTable.getRowCount(); i++){
			obj = tierTable.getValueAt(i, 0);
			if(obj != null && ((SelectEnableObject)obj).isEnabled() && ((SelectEnableObject)obj).isSelected()){
				itTiers.add(transcription.getTierWithId(((SelectEnableObject)obj).toString()));
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
			
			obj = tierTable.getValueAt(i, 4);
			if(obj != null && ((SelectEnableObject)obj).isEnabled() && ((SelectEnableObject)obj).isSelected()){
				morphTypeTiersList.add(transcription.getTierWithId(((SelectEnableObject)obj).toString()));
			}
		}
		
		encoderInfo.setMappingForItem(FlexConstants.IT, itTiers);
		encoderInfo.setMappingForItem(FlexConstants.PHRASE, phraseList);
		encoderInfo.setMappingForItem(FlexConstants.WORD, wordList);
		encoderInfo.setMappingForItem(FlexConstants.MORPH, morphList);
		
		encoderInfo.setMorphTypeTiers(morphTypeTiersList);
		
		multiPane.putStepProperty("EncoderInfo", encoderInfo);
		return true;
	}
    
	/**
	 * Updates the button states according to some constraints 
	 * (like everything has to be filled in, consistently)
	 */
	public void updateButtonStates(){		
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}	 
	
	private void loadPreferences(){
		Boolean val = Preferences.getBool(MORPHTYPE, null);
		if (val != null) {
			morphTypeCB.setSelected((Boolean)val);
		}
	}
	
	private void storePreferences(){
		Preferences.set(MORPHTYPE, morphTypeCB.isSelected(), null);
	}
	
		
	/**
	 * Initializes the components for ui.
	 */
	@Override
	protected void initComponents() {   
		morphTypeCB = new JCheckBox(ElanLocale.getString("ExportFlexStep2.SelectMorphType"));
		morphTypeCB.setSelected(true);
		morphTypeCB.addActionListener(actionHandler);
		
		morphTypeCombo = new JComboBox();
		morphTypeCombo.addActionListener(actionHandler);		
		
		// Item Model DefaultTableModel
   		itemModel = new DefaultTableModel(){ 
   			@Override
			public boolean isCellEditable(int row, int column) {     				
   				if(itemModel.getValueAt(row, column) == null){
   					return false;
   				}
   			 			
   				if(itemModel.getValueAt(row, column).toString().equals(DEFAULT_VALUE)){
   					return false;
   				}
   			 		
   				return true;
   			}
   		};        
   		
   		itemModel.setColumnIdentifiers(new String[] {
   				FlexConstants.IT+DEL+FlexConstants.ITEM, 
   				FlexConstants.PHRASE+DEL+FlexConstants.ITEM, 
   				FlexConstants.WORD+DEL+FlexConstants.ITEM,
   				FlexConstants.MORPH+DEL+FlexConstants.ITEM});  
   		itemModel.addRow(new Object[]{null, null, null, null});
		
    	//DefaultTableCellRenderer
   		DefaultTableCellRenderer itemTableRenderer = new DefaultTableCellRenderer(){
   	       	 @Override
			public Component getTableCellRendererComponent(JTable table,
   	       				Object value, boolean isSelected, boolean hasFocus, int row,
   	       			 	int column){
   	       		 
   	       		Component cell = super.getTableCellRendererComponent(table, value, 
   	       				 isSelected, hasFocus, row, column);
   	       		 
   	       	 	if(value == null){
   	       	 		return cell;
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
   		itemTable = new JTable(itemModel);	       
   		itemTable.setCellSelectionEnabled(true);	
   		itemTable.setDefaultEditor(Object.class, new ItemTableCellEditor());  
   		itemTable.setDefaultRenderer(Object.class, itemTableRenderer);      
   		itemTable.setShowGrid(true);
   		itemTable.setGridColor(Color.BLACK);      
   		itemTable.setSelectionBackground(Color.WHITE);        
   		itemTable.setRowHeight(itemTable.getRowHeight()+ 5);  
		
		// element mapping panel
	    JPanel mappingPanel = new JPanel();
	    mappingPanel.setLayout(new GridBagLayout());
	    mappingPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportFlexStep2.ItemMapping")));
	        
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = globalInset;	   
	    gbc.anchor = GridBagConstraints.NORTHWEST;		   
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
	    mappingPanel.add(new JScrollPane(itemTable), gbc);
	    
	    //item mapping panel
	    tierModel = new DefaultTableModel(){
	    	@Override
			public boolean isCellEditable(int row, int column) {     	
	    		if(column == 4 && !morphTypeCB.isSelected()){
	    			return false;
	    		}
	    		
	    		
	    		Object value = getValueAt(row, column);
	   			if(value instanceof SelectEnableObject){	   				
		    		return ((SelectEnableObject)value).isEnabled();
		    	}
		    	
		    	return false;
	   		} 
	    };
	   		
		tierModel.setColumnIdentifiers(new String[] { 
				FlexConstants.IT+DEL+FlexConstants.ITEM, 
   				FlexConstants.PHRASE+DEL+FlexConstants.ITEM, 
   				FlexConstants.WORD+DEL+FlexConstants.ITEM,
   				FlexConstants.MORPH+DEL+FlexConstants.ITEM,
   				MORPH_TYPE});  
		tierModel.addRow(new Object[]{null, null, null, null, null});
		
		tierTable = new JTable(tierModel);
		tierTable.setDefaultRenderer(Object.class, new CheckBoxTableCellRenderer());  
		tierTable.setDefaultEditor(Object.class, new TierTableCellEditor());
		tierTable.setShowGrid(true);
		tierTable.setGridColor(Color.BLACK);      
		tierTable.setSelectionBackground(Color.WHITE);  
	    
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
		gbc.insets = globalInset;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;	
		add(morphTypeCB, gbc);		
		
		gbc.gridx = 1;		
		add(morphTypeCombo, gbc);			
	
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;		
		gbc.weightx = 1.0;	
		gbc.weighty = 1.0;			
		add(mappingPanel, gbc);	
		
		gbc.gridy = 2;
		add(itemPanel, gbc);
		
		loadPreferences();
	} 	
	
	private void updateTypesForMorphType(){
		List<String> typeList = itemTypeMap.get(FlexConstants.MORPH);		
		morphTypeCombo.removeActionListener(actionHandler);
		
		morphTypeCombo.removeAllItems();
		
		if(typeList.size() <= 0){
			morphTypeCombo.setEnabled(false);
			morphTypeCB.setEnabled(false);	
			
			morphTypeCombo.addItem(DEFAULT_VALUE);
			updateTiersForMorphType();
		} else {
			morphTypeCombo.setEnabled(true);
			morphTypeCB.setEnabled(true);	
			
			morphTypeCombo.addItem(SELECT_TYPE);
			for(int i=0; i < typeList.size(); i++){
				morphTypeCombo.addItem(typeList.get(i));
			}
		}
		
		morphTypeCombo.addActionListener(actionHandler);
		if(morphTypeSelected != null){
			if(typeList.contains(morphTypeSelected)){
				morphTypeCombo.setSelectedItem(morphTypeSelected);
			}
		} else {
			for(int i=0; i < typeList.size(); i++){
				if(typeList.get(i).contains(MORPH_TYPE)){
					morphTypeCombo.setSelectedIndex(i+1);
				}
			}
		}
	}
	
	private void updateTiersForMorphType(){
		int column = 4;	
		
		//first remove all the values from the column		
		for(int i= 0; i < tierTable.getRowCount(); i++){
			tierModel.setValueAt(null, i, column);
		}
		
		if(morphTypeCB.isSelected()){
			morphTypeSelected = morphTypeCombo.getSelectedItem().toString();	
		} else {
			morphTypeSelected = null;
		}
		
		if(morphTypeSelected == null ||
				morphTypeSelected.equals(DEFAULT_VALUE) ||
				morphTypeSelected.equals(SELECT_TYPE)){
			return;
		}
		
		List<TierImpl> tierList = encoderInfo.getMappingForElement(FlexConstants.MORPH);
		
		List<String> itemTierList = new ArrayList<String>();
		for (TierImpl t : tierList) {
			for (TierImpl t2 : t.getChildTiers()) {
				if(morphTypeSelected.equals(t2.getLinguisticType().getLinguisticTypeName())){
					itemTierList.add(t.getName());
				}
			}
		}
		
		if(itemTierList.size() > 0){
			int row = itemTierList.size();
						
			for(int i= tierTable.getRowCount(); i < row; i++){
				tierModel.addRow(new Object[]{null, null, null, null, null});
			}			
			
			for(int i= 0; i< itemTierList.size(); i++){
				tierModel.setValueAt(new SelectEnableObject<String>(itemTierList.get(i), itemTierList.get(i).contains(MORPH_TYPE)), i, column);			
			}
			
		} else {
			for(int i= 0; i < tierTable.getRowCount(); i++){
				tierModel.setValueAt(null, i, column);
			}
		}
		checkAndRemoveEmptyRows(tierModel);
	}
	
	private void updateItemTypesFor(String itemType){
		List<String> typeList = itemTypeMap.get(itemType);
		typeList.clear();
		
		List<TierImpl> tiers = null;
		int column = 0;
		if(itemType.equals(FlexConstants.IT)){			
			tiers = encoderInfo.getMappingForElement(FlexConstants.IT);	
			column = 0;			
		} else if(itemType.equals(FlexConstants.PHRASE)){
			tiers = encoderInfo.getMappingForElement(FlexConstants.PHRASE);	
			column = 1;			
		} else if(itemType.equals(FlexConstants.WORD)){
			tiers = encoderInfo.getMappingForElement(FlexConstants.WORD);	
			column = 2;
		} else if(itemType.equals(FlexConstants.MORPH)){
			tiers = encoderInfo.getMappingForElement(FlexConstants.MORPH);	
			column = 3;
		}
		
		if(tiers != null){
			for (TierImpl tier : tiers) {
				for (TierImpl tier2 : tier.getChildTiers()) {
					int sterotype = tier2.getLinguisticType().getConstraints().getStereoType();
					if(sterotype == Constraint.SYMBOLIC_ASSOCIATION){
						if(!typeList.contains(tier2.getLinguisticType().getLinguisticTypeName())){
							typeList.add(tier2.getLinguisticType().getLinguisticTypeName());
						}
					}
				}
			}
		}	
		
		if(typeList.size() > 0){
			int row = typeList.size();
						
			for(int i= itemTable.getRowCount(); i <= row; i++){
				itemModel.addRow(new Object[]{null, null, null, null});
			}
			
			boolean addDefaultValue = true;
			for(int i= 0; i< typeList.size(); i++){
				if(column == 3){
					if(typeList.get(i).contains(MORPH_TYPE)){
						addDefaultValue = false;
						continue;
					}
				}
				itemModel.setValueAt(typeList.get(i), i, column);
			}
			
			if(addDefaultValue){
				itemModel.setValueAt(DEFAULT_VALUE, row, column);
			} else {
				// applied only for morph type
				itemModel.setValueAt(SELECT_TYPE, row-1, column);
			}
			
			deleteColumnValuesAfter(row, column);
		} else {
			if(itemTable.getRowCount() <= 0){
				itemModel.addRow(new Object[]{null, null, null, null});
			}
			itemModel.setValueAt(DEFAULT_VALUE, 0, column);
			deleteColumnValuesAfter(1, column);
	   	}
		checkAndRemoveEmptyRows(itemModel);
		
		updateTierTableForItem(itemType);
	}
	
	private void updateTierTableForItem(String itemType){
		List<TierImpl> tierList = encoderInfo.getMappingForElement(itemType);
		List<String> typeList = new ArrayList<String>();
							
		int column = 0;
		if(itemType.equals(FlexConstants.IT)){
			column = 0;	
		} else if(itemType.equals(FlexConstants.PHRASE)){
			column = 1;	
		} else if(itemType.equals(FlexConstants.WORD)){
			column = 2;
		} else if(itemType.equals(FlexConstants.MORPH)){
			column = 3;
		}
		
		for(int i = 0; i < itemTable.getRowCount(); i++){
			String type = (String) itemTable.getValueAt(i, column);
			if(type != null && !type.equals(SELECT_TYPE) && !type.equals(DEFAULT_VALUE)){
				if(!typeList.contains(type)){
					typeList.add(type);
				}
			}
		}
		
		List<String> itemTierList = new ArrayList<String>();
		for (TierImpl t : tierList) {
			for (TierImpl t2 : t.getChildTiers()) {
				if(typeList.contains(t2.getLinguisticType().getLinguisticTypeName())){
					itemTierList.add(t2.getName());
				}
			}
		}
		
		//first remove all the values from the column		
		for(int i= 0; i < tierTable.getRowCount(); i++){
			tierModel.setValueAt(null, i, column);
		}
		
		if(itemTierList.size() > 0){
			int row = itemTierList.size();
						
			for(int i= tierTable.getRowCount(); i < row; i++){
				tierModel.addRow(new Object[]{null, null, null, null, null});
			}
			
			for(int i= 0; i< itemTierList.size(); i++){
				if(column == 3 && itemTierList.get(i).contains(MORPH_TYPE)){
					tierModel.setValueAt(new SelectEnableObject<String>(itemTierList.get(i), false), i, column);
				} else{
					tierModel.setValueAt(new SelectEnableObject<String>(itemTierList.get(i), true), i, column);
				}
			}
		} else {
			for(int i= 0; i < tierTable.getRowCount(); i++){
				tierModel.setValueAt(null, i, column);
			}
		}
		checkAndRemoveEmptyRows(tierModel);
	}
	
	private void checkAndRemoveEmptyRows(DefaultTableModel model){
		int rowIndex = 0;
		
		int n = model.getColumnCount();
		
		while(rowIndex < model.getRowCount()){
			if(n == 4){
				if(model.getValueAt(rowIndex,0) == null && 
						model.getValueAt(rowIndex,1) == null &&
						model.getValueAt(rowIndex,2) == null &&
						model.getValueAt(rowIndex,3) == null){
					model.removeRow(rowIndex);
				} else {
					rowIndex++ ;
				}
			} else if (n == 5){
				if(model.getValueAt(rowIndex,0) == null && 
						model.getValueAt(rowIndex,1) == null &&
						model.getValueAt(rowIndex,2) == null &&
						model.getValueAt(rowIndex,3) == null &&
						model.getValueAt(rowIndex,4) == null){
					model.removeRow(rowIndex);
				} else {
					rowIndex++ ;
				}
			}
			
		}
	}
	
	/**
	 * Update the types for the given row
	 * 
	 * @param row
	 */
	private void updateTiersForColumn(int column){
		switch(column) {	
		case 0:
			updateTierTableForItem(FlexConstants.IT);
			break;
		case 1:
			updateTierTableForItem(FlexConstants.PHRASE);
			break;
		case 2:
			updateTierTableForItem(FlexConstants.WORD);
			break;
		case 3:
			updateTierTableForItem(FlexConstants.MORPH);
			break;
	
		}
	}
	
	/**
	 * Deletes all the item type values
	 * on the given column from the given row
	 * 
	 * @param row, row after which the element 
	 *             values has to be deleted
	 * @param column, column on which the values to be deleted
	 */
	private void deleteColumnValuesAfter(int row, int column){
		for(int i= row+1 ; i< itemTable.getRowCount(); i++){
			itemTable.setValueAt(null, i, column);
	   	}
	}
	
	private class ActionHandler implements ActionListener{

		public ActionHandler() {	   		  		
	   	}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			updateTiersForMorphType();	
		}
	}
	
	/**
	  * Cell Editor for the tier mapping JTable
	  * 
	  * @author aarsom
	  */
	private class TierTableCellEditor extends DefaultCellEditor implements ActionListener{
		private int startEditInOneClick = 1;
	  	private JCheckBox checkBox;	  
	  	private SelectEnableObject<String> selObject;
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
	   			selObject = (SelectEnableObject<String>)value;
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
	  		
	  		if(column == 3 || column == 4){
	  			
	  		}
	  		
	  		
	  		// check the morph- item & morphtype
	  	}
	}
	
	
	 /**
	  * Cell Editor for the selection JTable
	  * 
	  * @author aarsom
	  *
	  */
	 private class ItemTableCellEditor extends DefaultCellEditor implements ActionListener {    	
	   	private int startEditInOneClick = 1;
	   	private JComboBox comboBox;	  
	   	
	   	private int row;
	   	private int column;
	   	private String value;
	   	private String type;
	   	
	   	public ItemTableCellEditor() {
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
	   		this.column = column;
	   		this.value = value.toString();
	   		
	   		switch(column){
	   		case 0:
	   			type = FlexConstants.IT;
	   			break;
	   		case 1:
	   			type = FlexConstants.PHRASE;
	   			break;
	   		case 2:
	   			type = FlexConstants.WORD;
	   			break;
	   		case 3:
	   			type = FlexConstants.MORPH;
	   			break;
	   		}
	   		
	   		List<String> types = itemTypeMap.get(type);
	  		comboBox = new JComboBox();  
	   	
	  		List<String> usedTypes = new ArrayList<String>();
	  		for(int i = 0; i < row; i++){
	  			usedTypes.add((String) table.getValueAt(i, column));
	  		}
	   		
	  		comboBox.addItem(SELECT_TYPE);
	   		for(int i=0; i< types.size(); i++){	
	   			if(!usedTypes.contains(types.get(i))){
	   				comboBox.addItem(types.get(i));
	   			}
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
	  		itemTable.editingStopped(new ChangeEvent(this));
			if(value.equals(comboBox.getSelectedItem()) ){
				return;
			}
			
			Object val = comboBox.getSelectedItem();
			
			if(!val.equals(DEFAULT_VALUE) && !val.equals(SELECT_TYPE)){
				deleteColumnValuesAfter(row, column);
				
				if(row < itemTypeMap.get(type).size()-1){
					if(itemTable.getRowCount()-1 == row){
						itemModel.addRow(new Object[]{null, null, null, null, null});
					} 
					itemTable.setValueAt(SELECT_TYPE, row+1, column);
				} else {
					if(itemTable.getRowCount()-1 == row){
						itemModel.addRow(new Object[]{null, null, null, null, null});
					} 
					itemTable.setValueAt(DEFAULT_VALUE, row+1, column);
				}
				
			} else{
				if(val.equals(SELECT_TYPE)){
					deleteColumnValuesAfter(row, column);
				}
			}
			checkAndRemoveEmptyRows(itemModel);
			updateTiersForColumn(column);
		}
	 }
}