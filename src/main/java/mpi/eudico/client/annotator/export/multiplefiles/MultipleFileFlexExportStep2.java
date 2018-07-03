package mpi.eudico.client.annotator.export.multiplefiles;

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
import mpi.eudico.client.annotator.export.ExportFlexStep2;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * Step pane for element-item configuration
 *  
 * 
 * @author aarsom
 * @version Feb, 2013
 */
@SuppressWarnings("serial")
public class MultipleFileFlexExportStep2 extends StepPane{	
	
	private List<TranscriptionImpl> transList;
	
	private final String SELECT_TYPE = ElanLocale.getString("TranscriptionManager.ComboBoxDefaultString");
	private final String DEFAULT_VALUE = ElanLocale.getString("ExportFlexDialog.DefaultValue");
	
	private final String MORPH_TYPE = FlexConstants.MORPH+"-"+FlexConstants.TYPE;	
	
	private JCheckBox morphTypeCB;
	private JComboBox morphTypeCombo;
	
	private JPanel itemMappingPanel;
	    
	private JTable itemTable;
	private DefaultTableModel itemModel; 	
	
	/** a hash map of flexType - list<linguistic types> */
	private HashMap<String, List<String>> itemTypeMap;
	
	private HashMap<String, String> elementTypeMap;	
	
	private String morphTypeSelected;
	
	protected Insets globalInset = new Insets(2, 4, 2, 4);
	
	private String DEL = "_";
	
	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 */
	public MultipleFileFlexExportStep2(MultiStepPane mp){
		super(mp);
				
		itemTypeMap = new HashMap<String, List<String>>();
		itemTypeMap.put(FlexConstants.IT, new ArrayList<String>());
		itemTypeMap.put(FlexConstants.PHRASE, new ArrayList<String>());
		itemTypeMap.put(FlexConstants.WORD, new ArrayList<String>());
		itemTypeMap.put(FlexConstants.MORPH, new ArrayList<String>());			
		
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
		transList = 	(List<TranscriptionImpl>) multiPane.getStepProperty("TransImplList");
		elementTypeMap = (HashMap<String, String>)multiPane.getStepProperty("ElementTypeMap");					
		
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
		List<String> itList = new ArrayList<String>();
		List<String> phraseList = new ArrayList<String>();
		List<String> wordList = new ArrayList<String>();
		List<String> morphList = new ArrayList<String>();
		
		Object obj;
		for(int i=0; i < itemTable.getRowCount(); i++){
			obj = itemTable.getValueAt(i, 0);
			if(obj != null && !obj.toString().equals(DEFAULT_VALUE) && !obj.toString().equals(SELECT_TYPE)){
				itList.add(obj.toString());
			}
			
			obj = itemTable.getValueAt(i, 1);
			if(obj != null && !obj.toString().equals(DEFAULT_VALUE) && !obj.toString().equals(SELECT_TYPE)){
				phraseList.add(obj.toString());
			}
			
			obj = itemTable.getValueAt(i, 2);
			if(obj != null && !obj.toString().equals(DEFAULT_VALUE) && !obj.toString().equals(SELECT_TYPE)){
				wordList.add(obj.toString());
			}
			
			obj = itemTable.getValueAt(i, 3);
			if(obj != null && !obj.toString().equals(DEFAULT_VALUE) && !obj.toString().equals(SELECT_TYPE)){
				morphList.add(obj.toString());
			}
		}
		
		String morphType = (String) morphTypeCombo.getSelectedItem();
		if(morphType != null && 
				!morphType.equals(SELECT_TYPE) &&
				!morphType.equals(DEFAULT_VALUE)){
			multiPane.putStepProperty("Morph-Type", morphType);
			if(morphList.contains(morphType)){
				morphList.remove(morphType);
			}
		}		
		
		HashMap<String, List<String>> elementItemMap = new HashMap<String, List<String>>();
		elementItemMap.put(FlexConstants.IT, itList);
		elementItemMap.put(FlexConstants.PHRASE, phraseList);
		elementItemMap.put(FlexConstants.WORD, wordList);
		elementItemMap.put(FlexConstants.MORPH, morphList);		
		multiPane.putStepProperty("ElementItemMap", elementItemMap);
		
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
		Boolean val = Preferences.getBool(ExportFlexStep2.MORPHTYPE, null);
		if (val != null) {
			morphTypeCB.setSelected(val);
		}
	}
	
	private void storePreferences(){
		Preferences.set(ExportFlexStep2.MORPHTYPE, morphTypeCB.isSelected(), null);
	}
		
	/**
	 * Initializes the components for ui.
	 */
	@Override
	protected void initComponents() {   
		morphTypeCB = new JCheckBox(ElanLocale.getString("ExportFlexStep2.SelectMorphType"));
		morphTypeCB.setSelected(true);
		
		morphTypeCombo = new JComboBox();		
		
		initializeItemMappingPanel();
	    
	    JPanel itemPanel = new JPanel();
	    itemPanel.setLayout(new GridBagLayout());
	    itemPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportFlexStep1.TierMapping")));
	        				        
	    // main layout
	    setLayout(new GridBagLayout());
	    GridBagConstraints gbc = new GridBagConstraints();	
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
		add(itemMappingPanel, gbc);	
		
		loadPreferences();
	} 	
	
	private void initializeItemMappingPanel(){
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
   		
   		
	    itemMappingPanel = new JPanel(new GridBagLayout());	    
	    itemMappingPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportFlexStep2.ItemMapping")));
	        
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = globalInset;	   
	    gbc.anchor = GridBagConstraints.NORTHWEST;		   
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
	    itemMappingPanel.add(new JScrollPane(itemTable), gbc);
	}
	
	private void updateTypesForMorphType(){
		List<String> typeList = itemTypeMap.get(FlexConstants.MORPH);		
			
		morphTypeCombo.removeAllItems();
		
		if(typeList.size() <= 0){
			morphTypeCombo.setEnabled(false);
			morphTypeCB.setEnabled(false);	
			
			morphTypeCombo.addItem(DEFAULT_VALUE);
		} else {
			morphTypeCombo.setEnabled(true);
			morphTypeCB.setEnabled(true);	
			
			morphTypeCombo.addItem(SELECT_TYPE);
			for(int i=0; i < typeList.size(); i++){
				morphTypeCombo.addItem(typeList.get(i));
			}
		}
		
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
	
	private void updateItemTypesFor(String itemType){
		List<String> typeList = itemTypeMap.get(itemType);
		typeList.clear();
		
		int column = 0;
		String type = null;
		if(itemType.equals(FlexConstants.IT)){			
			type = elementTypeMap.get(FlexConstants.IT);	
			column = 0;			
		} else if(itemType.equals(FlexConstants.PHRASE)){
			type = elementTypeMap.get(FlexConstants.PHRASE);	
			column = 1;			
		} else if(itemType.equals(FlexConstants.WORD)){
			type = elementTypeMap.get(FlexConstants.WORD);	
			column = 2;
		} else if(itemType.equals(FlexConstants.MORPH)){
			type = elementTypeMap.get(FlexConstants.MORPH);	
			column = 3;
		}
		
		List<TierImpl> tiers;
		List<TierImpl> childTiers;
		TierImpl tier;
		if(type != null){
			for(int tr= 0; tr < transList.size(); tr++){
				tiers = transList.get(tr).getTiersWithLinguisticType(type);
				for (int t = 0; t < tiers.size(); t++){
					tier = tiers.get(t);			
					childTiers = tier.getChildTiers();
					for(int i=0; i < childTiers.size(); i++){
						tier = childTiers.get(i);
						int sterotype = tier.getLinguisticType().getConstraints().getStereoType();
						if(sterotype == Constraint.SYMBOLIC_ASSOCIATION){
							if(!typeList.contains(tier.getLinguisticType().getLinguisticTypeName())){
								typeList.add(tier.getLinguisticType().getLinguisticTypeName());
							}
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
				itemModel.setValueAt(SELECT_TYPE, row, column);
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
	}
	
	private void checkAndRemoveEmptyRows(DefaultTableModel model){
		int rowIndex = 0;		
		while(rowIndex < model.getRowCount()){
			if(model.getValueAt(rowIndex,0) == null && 
					model.getValueAt(rowIndex,1) == null &&
					model.getValueAt(rowIndex,2) == null &&
					model.getValueAt(rowIndex,3) == null){
				model.removeRow(rowIndex);
			} else {
				rowIndex++ ;
			} 
			
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
		}
	 }
}