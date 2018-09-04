package mpi.eudico.client.annotator.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.util.TableSubHeaderObject;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.flex.FlexEncoderInfo;

/**
 * Step pane for element-item configuration
 *  
 * 
 * @author aarsom
 * @version Feb, 2013
 */
@SuppressWarnings("serial")
public class ExportFlexStep3 extends StepPane{	
	
	private TranscriptionImpl transcription;
	
	private final String SELECT_TYPE = ElanLocale.getString("TranscriptionManager.ComboBoxDefaultString");
	private final String SELECT_LANG = ElanLocale.getString("ExportFlexStep3.SelectLang");
	private final String DEFAULT_VALUE = ElanLocale.getString("ExportFlexDialog.DefaultValue");
	    
	private FlexEncoderInfo encoderInfo;

	private JTable table;
	private DefaultTableModel model; 
		
	private JRadioButton linTypeRB;
	private JRadioButton tierRB;

	private JRadioButton typeRB;
	private JRadioButton langRB;
	
	private JTextField addCustomValueTF;
	private JComboBox removeValueCombo;
	
	private JButton addButton;
	private JButton removeButton;
	
	/** a hash map of tiername - type,Lang */
	private HashMap<String, String> typeLangMap;
	
	/** a hash map of flexType - list<tier name> */
	private HashMap<String, List<String>> tierMap;
	
	/** a hash map of flexType - list<String> (linguistic type) */
	private HashMap<String, List<String>> linTypeMap;
	
	/** a hash map of tierName - List<String> */
	private HashMap<String, List<String>> tierTypeLangMap;
	
	protected Insets globalInset = new Insets(2, 4, 2, 4);
	
	private String DEL = "_";
			
	private List<String> typeList;
	private List<String> langList;
	
	//preference string
	private final String TIERS = "ExportFlex.LangTypeValue.BasedOnTiers";
	private final String TYPE_LANG = "ExportFlex.AddRemove.LangOrType";
	
	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcripton
	 */
	public ExportFlexStep3(MultiStepPane mp, TranscriptionImpl transcription){
		super(mp);
		this.transcription = transcription;		
		
		typeLangMap = new HashMap<String, String>();
		
		tierTypeLangMap = new HashMap<String, List<String>>();		
		
		tierMap = new HashMap<String, List<String>>();		
		linTypeMap = new HashMap<String, List<String>>();
		
		typeList = new ArrayList<String>();
		for (String element : FlexConstants.DEFINED_TYPES) {
			typeList.add(element);
		}
		
		langList = new ArrayList<String>();
		
		initComponents();
	}
	
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("ExportFlexStep3.Title");
	}

	/**
	 * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
	 */
	@Override
	public void enterStepForward(){
		encoderInfo = (FlexEncoderInfo) multiPane.getStepProperty("EncoderInfo");	
		
		updateMapsForType(FlexConstants.IT);		
		updateMapsForType(FlexConstants.PHRASE);		
		updateMapsForType(FlexConstants.WORD);		
		updateMapsForType(FlexConstants.MORPH);
		
		updateTable();
		
		Collections.sort(typeList);
		Collections.sort(langList);
		
		updateRemoveValuesComboBoxItems();
		
		updateButtonStates();
	}
	
	private void updateMapsForType(String flextype){
		
		List<TierImpl> tierImplList = new ArrayList<TierImpl>();
		
		tierImplList.addAll(encoderInfo.getMappingForElement(flextype));
		tierImplList.addAll(encoderInfo.getMappingForItem(flextype));
				
		List<String> typeList = new ArrayList<String>();
		List<String> tierList = new ArrayList<String>();
		
		String typeName;
		String tierName;
		for(int i =0; i < tierImplList.size(); i++){
			typeName = tierImplList.get(i).getLinguisticType().getLinguisticTypeName();
			
			if(!typeList.contains(typeName)){
				typeList.add(typeName);
			}
			
			tierName = tierImplList.get(i).getName();
			tierList.add(tierName);
		}
		
		linTypeMap.put(flextype, typeList);
		tierMap.put(flextype, tierList);
	}
	
	@Override
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	@Override
	public boolean leaveStepForward(){	
		// store the tier -type, lang
		storePreferences();
		
		tierTypeLangMap.clear();
		
		List<String> valueList;
		for(int i =0; i < table.getRowCount(); i++){
			if(table.getValueAt(i,1) instanceof TableSubHeaderObject){
				continue;
			}
			
			String name = (String) table.getValueAt(i, 0);
			
			valueList = new ArrayList<String>();			
			valueList.add((String) table.getValueAt(i, 1));
			valueList.add((String) table.getValueAt(i, 2));
			
			if(linTypeRB.isSelected()){
				List<TierImpl> tiers = transcription.getTiersWithLinguisticType(name);
				for(int t=0; t < tiers.size(); t++){
					tierTypeLangMap.put(tiers.get(t).getName(), valueList);
				}
			} else {
				tierTypeLangMap.put(name, valueList);
			}
		}
		
		encoderInfo.setTypeLangMap(tierTypeLangMap);
				
		multiPane.putStepProperty("EncoderInfo", encoderInfo);
		return true;

	}
    
	/**
	 * Updates the button states according to some constraints 
	 * (like everything has to be filled in, consistently)
	 */
	public void updateButtonStates(){	
		
		boolean next_Button = true;
		Object val;
		for(int i =0; i < table.getRowCount(); i++){
			if(table.getValueAt(i,1) instanceof TableSubHeaderObject){
				continue;
			}
			
			val = table.getValueAt(i, 1);
			if(val == null || val.equals(SELECT_TYPE)){
				next_Button = false;
				break;
			}
			
			val = table.getValueAt(i, 2);
			if(val == null || val.equals(SELECT_LANG)){
				next_Button = false;
				break;
			}
		}
		
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, next_Button);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}	    
	
	private void loadPreferences(){
		Boolean boolPref = Preferences.getBool(TIERS, null);
		if (boolPref != null) {
			tierRB.setSelected((Boolean)boolPref);
			linTypeRB.setSelected(!(Boolean)boolPref);
		}
		String stringPref = Preferences.getString(TYPE_LANG, null);
		if (boolPref != null) {
			if ("language".equals(stringPref)) {
				langRB.setSelected(true);
			}
		}
	}
	
	private void storePreferences(){
		Preferences.set(TIERS, tierRB.isSelected(), null);
		Preferences.set(TYPE_LANG, (typeRB.isSelected() ? "type" : "language"), null);
	}
		
	/**
	 * Initializes the components for ui.
	 */
	@Override
	protected void initComponents() {   
		
		ActionHandler actionHandler = new ActionHandler();
	 
	    //tier mapping 
	    model = new DefaultTableModel(){
	    	@Override
			public boolean isCellEditable(int row, int column) {     
	    		if(column == 0){
	    			return false;
	    		}
	    		
	   			if(getValueAt(row, column) instanceof String){	   				
		    		return true;
		    	}
		    	
		    	return false;
	   		} 
	    };
	    
	    //DefaultTableCellRenderer
   		DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer(){
   			private Color DEF_LABEL_BG = new Color (ExportFlexStep3.this.getBackground().getRed(), ExportFlexStep3.this.getBackground().getGreen(), 
   					ExportFlexStep3.this.getBackground().getBlue());
   	       	 @Override
			public Component getTableCellRendererComponent(JTable table,
   	       				Object value, boolean isSelected, boolean hasFocus, int row,
   	       			 	int column){
   	       		 
   	       		Component cell = super.getTableCellRendererComponent(table, value, 
   	       				 isSelected, hasFocus, row, column);
   	       		 
   	       	 	if(value == null){
   	       	 		return cell;
   	       	 	}  
   	       	 	
   	       	 	if(value instanceof TableSubHeaderObject){
   	       	 		cell.setFont(new Font(table.getFont().getName(), Font.BOLD, table.getFont().getSize()+1));
   	       	 		cell.setBackground(DEF_LABEL_BG);
   	       	 	    cell.setForeground(Color.BLACK);
   	       	 	} else if(value.equals(SELECT_TYPE) || value.equals(SELECT_LANG)){    			   
   	       	 		cell.setForeground(Color.GRAY);
   	       	 		cell.setBackground(Color.WHITE);
   	       	 	} else{
   	       	 		cell.setForeground(Color.BLACK);
   	       	 	    cell.setBackground(Color.WHITE);
   	       	 	}       		 
   	       	 	return cell;    		   
   	       	 }
   		};
	   				
		table = new JTable(model);
		table.setDefaultRenderer(Object.class, tableRenderer);  
		table.setDefaultEditor(Object.class, new TableCellEditor());
		table.setShowGrid(true);
		table.setGridColor(Color.BLACK);      
		table.setSelectionBackground(Color.WHITE);  
		
		linTypeRB = new JRadioButton(ElanLocale.getString("ExportFlexStep3.LingType"));
		linTypeRB.addActionListener(actionHandler);
		
		tierRB = new JRadioButton(ElanLocale.getString("ExportFlexStep3.Tier"));
		tierRB.addActionListener(actionHandler);
		
		tierRB.setSelected(true);
		
		ButtonGroup group = new ButtonGroup();
		group.add(tierRB);
		group.add(linTypeRB);	
		
		typeRB = new JRadioButton("type");
		typeRB.addActionListener(actionHandler);
		
		langRB = new JRadioButton("language");
		langRB.addActionListener(actionHandler);
		
		typeRB.setSelected(true);
		
		ButtonGroup group1 = new ButtonGroup();
		group1.add(typeRB);
		group1.add(langRB);	
		
		addCustomValueTF = new JTextField();
		removeValueCombo = new JComboBox();		
		
		addButton = new JButton(ElanLocale.getString("Button.Add"));
		removeButton = new JButton(ElanLocale.getString("FileChooser.Button.Remove"));
		
		addButton.addActionListener(actionHandler);
		removeButton.addActionListener(actionHandler);
		
		//type-lang configuration panel
		JPanel radioPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;		   
		gbc.fill = GridBagConstraints.NONE;	  
		radioPanel.add(typeRB, gbc);
	    
	    gbc.gridx = 1;
	    radioPanel.add(langRB, gbc);		
		
		JPanel configPanel = new JPanel();
	    configPanel.setLayout(new GridBagLayout());
	    configPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportFlexStep3.Title.Configuration")));
	    	    
	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = globalInset;	   
	    gbc.anchor = GridBagConstraints.NORTHWEST;		   
	    gbc.fill = GridBagConstraints.NONE;	  
	    configPanel.add(new JLabel(ElanLocale.getString("ExportFlexStep3.Label.AddRemove")), gbc);
	    
	    gbc.gridx = 1;
	    gbc.gridwidth = 2;
	    configPanel.add(radioPanel, gbc);
	    
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    gbc.gridwidth = 1;
	    configPanel.add(new JLabel(ElanLocale.getString("ExportFlexStep3.Label.AddCustomVal")), gbc);
	    
	    gbc.gridx = 1;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 1.0;
	    configPanel.add(addCustomValueTF, gbc);
	    
	    gbc.gridx = 2;
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weightx = 0.0;
	    configPanel.add(addButton, gbc);
	    
	    gbc.gridx = 0;
	    gbc.gridy = 2;
	    configPanel.add(new JLabel(ElanLocale.getString("ExportFlexStep3.Label.RemoveVal")), gbc);
	    
	    gbc.gridx = 1;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 1.0;
	    configPanel.add(removeValueCombo, gbc);
	    
	    gbc.gridx = 2;
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weightx = 0.0;
	    configPanel.add(removeButton, gbc);
	    
	    
	    // Define types and languages panel
	    JPanel panel = new JPanel();
	    panel.setLayout(new GridBagLayout());
	    panel.setBorder(new TitledBorder(ElanLocale.getString("ExportFlexStep3.Title.Specification")));
	        
	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.anchor = GridBagConstraints.NORTHWEST;		   
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.insets = new Insets(2,20,2,4);
	    panel.add(tierRB, gbc);
	    
	    gbc.gridx = 1;
	    gbc.insets = globalInset;	   
	    panel.add(linTypeRB, gbc);
		
	    gbc.gridx = 0;
	    gbc.gridy = 1;	
	    gbc.gridwidth = 2;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.gridwidth = 2;
	    gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
	    panel.add(new JScrollPane(table), gbc);	
		
		 // main layout
	    setLayout(new GridBagLayout());
	    gbc = new GridBagConstraints(); 
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = globalInset;	   
	    gbc.anchor = GridBagConstraints.NORTHWEST;		   
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
	    gbc.gridheight = 1;
	    add(panel, gbc);
	    
	    gbc.gridy = 1;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 0.0;
	    gbc.weighty = 0.0;
	    add(configPanel, gbc);
	    
	    loadPreferences();
	}	
	
	private void updateTable(){
		while(table.getRowCount() > 0){
			model.removeRow(0);
		}
		
		HashMap<String, List<String>> map;
		if(tierRB.isSelected()){
			model.setColumnIdentifiers(new String[] { 
		   			"TierName", FlexConstants.TYPE, FlexConstants.LANGUAGE});  
			
			map = tierMap;
		} else {
			model.setColumnIdentifiers(new String[] { 
		   			"Linguistic Type Name", FlexConstants.TYPE, FlexConstants.LANGUAGE});  
			map = linTypeMap;
		}
		
		addRowsFor(FlexConstants.IT, map.get(FlexConstants.IT));
		addRowsFor(FlexConstants.PHRASE, map.get(FlexConstants.PHRASE));
		addRowsFor(FlexConstants.WORD, map.get(FlexConstants.WORD));
		addRowsFor(FlexConstants.MORPH, map.get(FlexConstants.MORPH));
	}
	
	private void addRowsFor(String flexType, List<String> valList){
		model.addRow(new Object[]{new TableSubHeaderObject(flexType), new TableSubHeaderObject(""), new TableSubHeaderObject("")});
		for(int i = 0; i < valList.size(); i++){	
			String type =  getTypeName(valList.get(i));
			String lang = getLanguage(valList.get(i));
			if(i==0 && !flexType.equals(FlexConstants.IT)){
				if(type.equals(SELECT_TYPE)){
					type = FlexConstants.TXT;
				}
				
				if(lang == null){
					lang = SELECT_LANG;
				}
				
			} else {
				if(type.equals(SELECT_TYPE)){
					lang = null;
				} else {
					if(lang == null){
						lang = SELECT_LANG;
					}
				}
			}
			
			if(!type.equals(SELECT_TYPE)){
				if(!typeList.contains(type)){
					typeList.add(type);
				}
			}
			
			if(lang != null && !lang.equals(SELECT_LANG)){
				if(!langList.contains(lang)){
					langList.add(lang);
				}
			}
			
			model.addRow(new String[]{valList.get(i), type, lang});
		}
	}
	
	/**
	 * Extracts or reconstructs the (FLEx) type information from the tier/linType name
	 * Expected tier/linType name format: <(tier/linType)-typeName-language>,
	 * so three components of which two can have a '-' hyphen (the delimiter used
	 * by ELAN to construct tier and tier type names) in it.
	 * The tier or tier type part can start with "interlinear-text", this is treated
	 * separately. Other legal values might be added in the future?
	 * The flex-item-type name part can be "title-abbreviation" or "text-is-translation"
	 * or a custom name containing a '-', as allowed in FieldWorks 9 and higher. 
	 * The language part probably won't contain a '-' in its part.
	 *
	 * @param  name from which the type has to extracted
	 * @return type the extracted item type
	 */
	private String getTypeName(String name){
		String type = null;
		// could match more FLEx elements containing a '-'
		if(name.startsWith(FlexConstants.IT)){
			name = name.substring(FlexConstants.IT.length());// name now starts with a "-"
		}
		
		String[] compsArray = name.split("-");
		if (compsArray.length == 3) {
			// select second component
			type = compsArray[1];
		} else if (compsArray.length > 3) {
			// glue all components except the first and the last together
			/*
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < compsArray.length - 1; i++) {
				sb.append(compsArray[i]);
				if (i < compsArray.length - 2) {
					sb.append("-");
				}
			}
			type = sb.toString();
			*/
			// equivalent to
			type = name.substring(name.indexOf("-") + 1, name.lastIndexOf("-"));
		} else if (compsArray.length == 2) {
			type = compsArray[0];//??
		}
		/*
		int index = name.indexOf('-');
		int nextIndex = -1;
		
		if(index > -1){
			if(index+1 < name.length()){
				nextIndex = name.indexOf('-', index+1);		
			}
					
			if(nextIndex > -1 && index+1 < nextIndex){
				type = name.substring(index+1, nextIndex);
			} else {
				type = name.substring(index+1);
			}
		}
		*/
		if(type == null || type.equals(FlexConstants.ITEM)){
			type = SELECT_TYPE;
		}
		
		return type;
		
	}
	
	/**
	 * Extracts the language information from the tier/linType name
	 * Expected tier/linType name format: <tier/linType-typeName-language>
	 * 
	 * @param name the composed tier name or tier type name
	 * @return lang the extracted language label
	 * @see #getTypeName(String)
	 */
	private String getLanguage(String name){		
		String lang = null;		
		// could match more FLEx elements containing a '-'
		if(name.startsWith(FlexConstants.IT)){
			name = name.substring(FlexConstants.IT.length());
		}
		
		int lindex = name.lastIndexOf("-");
		if (lindex > -1 && lindex < name.length() - 2) {
			lang = name.substring(lindex + 1);
		}
		/*
		int firstIndex = name.indexOf('-');	
		int nextIndex = -1;
		if(firstIndex+1 < name.length()){
			nextIndex = name.indexOf('-', firstIndex+1);		
		}
		
		if(nextIndex > -1 && firstIndex+1 < nextIndex){
			lang = name.substring(nextIndex+1);
		} 
		*/
		return lang;
	}
	
	private void validateRowValues(){		
		String type;
		String language;
		for(int i =0; i < table.getRowCount(); i++){
			if(table.getValueAt(i,1) instanceof TableSubHeaderObject){
				continue;
			}
			
			type = (String) table.getValueAt(i, 1);
			language = (String) table.getValueAt(i, 2);
			
			if(!typeList.contains(type)){
				table.setValueAt(SELECT_TYPE, i, 1);
				table.setValueAt(null, i, 2);				
			} else {
				if(!langList.contains(language)){
					table.setValueAt(SELECT_LANG, i, 2);		
				}
			}
		}
	}
	
	private void updateRemoveValuesComboBoxItems(){
		removeValueCombo.removeAllItems();
		removeValueCombo.addItem("<select>");		
		
		if(typeRB.isSelected()){
			for(int i=0; i < typeList.size(); i++){
				removeValueCombo.addItem(typeList.get(i));
			}				
		} else {
			for(int i=0; i < langList.size(); i++){
				removeValueCombo.addItem(langList.get(i));
			}
		}
	}
	
	private class ActionHandler implements ActionListener{		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if(source.equals(linTypeRB) || source.equals(tierRB)){
				updateTable();
			} else if(source instanceof JRadioButton){
				List<String> values ;
				if(source.equals(typeRB)){
					values = typeList;
				} else {
					values = langList;	
				}
				
				removeValueCombo.removeAllItems();
				removeValueCombo.addItem("<select>");
				
				for(int i=0; i < values.size(); i++){
					removeValueCombo.addItem(values.get(i));
				}
			} else if (source.equals(addButton)){
				String value = addCustomValueTF.getText().trim();
				if(typeRB.isSelected()){					
					if(!typeList.contains(value)){
						typeList.add(value);
						Collections.sort(typeList);	
						updateRemoveValuesComboBoxItems();
						if (table.getCellEditor() != null && table.getEditingColumn() == 1) {// hardcoded column index for type
							table.getCellEditor().cancelCellEditing();
						}
					}
				} else {
					if(!langList.contains(value)){
						langList.add(value);
						Collections.sort(langList);
						updateRemoveValuesComboBoxItems();
						if (table.getCellEditor() != null && table.getEditingColumn() == 2) {// hardcoded column index for language
							table.getCellEditor().cancelCellEditing();
						}
					}	
				}
				
				addCustomValueTF.setText("");
			} else if(source.equals(removeButton)){
				String value = (String) removeValueCombo.getSelectedItem();
				if(value != null){
					if(typeRB.isSelected()){					
						typeList.remove(value);						
					} else {
						langList.remove(value);
					}
					
					removeValueCombo.removeItem(value);
					removeValueCombo.setSelectedIndex(0);
					
					validateRowValues();
				}
			}
		}
	}
	
	/**
	 * Cell Editor for the selection JTable
	 * 
	 * @author aarsom
	 *
	 */
	private class TableCellEditor extends DefaultCellEditor implements ActionListener {    	
	 	private int startEditInOneClick = 1;
	   	private JComboBox comboBox;
	   	private int row;
	   	private int column;
	   	private Object value;
	   	private String participant;
  	
	   	
	   	public TableCellEditor() {
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
		   	this.value = value;
	   		
		   	List<String> usedValues = new ArrayList<String>();
	   		List<String> values = null;
	   		
	   		
	   		if(column == 1){
	   			values = typeList;	   			
	   		} else if (column == 2){
	   			values = langList;	   
	   			
	   			if(tierRB.isSelected()){
	   				//participant = ((TierImpl)transcription.getTierWithId(table.getValueAt(row, 0).toString())).getParticipant();
	   				participant =  getParticipantFor((transcription.getTierWithId(table.getValueAt(row, 0).toString())));
	   			} else {
	   				participant = null;
	   			}
	   			
	   			
	   			int index  = row-1;
	   			String typeVal = (String) table.getValueAt(row, 1);	   			
	   				
	   			Object val;
	   			String type;
	   			String par;
	   			while(index > -1){
	   				val = table.getValueAt(index, 2);
	   				
	   				if(val instanceof TableSubHeaderObject){
	   					break;
	   				}	   					
	   					
	   				if(tierRB.isSelected()){
	   					//par = ((TierImpl)transcription.getTierWithId(table.getValueAt(index, 0).toString())).getParticipant();
	   					par = getParticipantFor((transcription.getTierWithId(table.getValueAt(index, 0).toString())));
		   			} else {
		   				par = null;
		   			}	   				
	   				
	   				type = (String) table.getValueAt(index, 1);
	   				
	   				if(type.equals(typeVal)){
		   				if(!val.toString().equals(SELECT_LANG)){
		   					if (participant == null || participant.equals(par)) {
		   						usedValues.add(val.toString());
		   					}
			   			}	
	   				}
	   				
	   				index--;
	   			}
	   		}	  
	   		
	   		comboBox = new JComboBox();
	   		String selectItem = null;
	   		for(int i = 0; i < values.size(); i++){
	   			if(!usedValues.contains(values.get(i))){
	   				if(value != null && values.get(i).equals(value)){
	   					selectItem = value.toString();
	   				}	   				
	   			}
	   			comboBox.addItem(values.get(i));
	   		}
	   		
	   		comboBox.setSelectedItem(selectItem);
	   		
	   		comboBox.addActionListener(this);
	   		return comboBox;
	   	}
	   	
	   	private String getParticipantFor(TierImpl tier)
	   	{
	   		String par = tier.getParticipant();
	   		if(par == null || par.trim().length() == 0){
	   			par = tier.getRootTier().getParticipant();
	   		}
	   		
	   		return par;
	   	}
	   	
	  	@Override
		public Object getCellEditorValue() {  
	  		if(comboBox.getSelectedIndex() == -1){
	  			return value;
	  		}
	   		return comboBox.getSelectedItem();
	   	}

		@Override
		public void actionPerformed(ActionEvent e) {
			table.editingStopped(new ChangeEvent(this));
			
			if(value.equals(comboBox.getSelectedItem()) ){
				return;
			}
			
			String typeVal;
			Object langVal;
			
			if(column == 1){
				typeVal = (String) comboBox.getSelectedItem();
				langVal = table.getValueAt(row, 2);
				
				
				if(langVal == null || langVal.toString().equals(SELECT_LANG)){
	   				table.setValueAt(SELECT_LANG, row, 2);
	   			} else {
	   				//validate lang value	   				
	   				int index  = row-1;
		   			while (index > -1){
		   				if(table.getValueAt(index, 0) instanceof TableSubHeaderObject){	 
		   					break;
		   				}
		   				index--;
		   			}
	   				
		   			// first item index from TableSubHeaderObject
		   			index = index +1;
	   				
		   			if(row == index){
		   				index++;
		   			}
		   			Object obj = table.getValueAt(index, 2);
		   			String type;
		   			String par;
		   			while(obj!= null && !(obj instanceof TableSubHeaderObject)){
		   				type = (String) table.getValueAt(index, 1);
		   				
		   				if(tierRB.isSelected()){
		   					par = transcription.getTierWithId(table.getValueAt(index, 0).toString()).getParticipant();
			   			} else {
			   				par = null;
			   			}	
		   				
			   			if(type.equals(typeVal) && (participant == null || par.equals(participant))){			   							   				
			   				if(langVal.toString().equals(obj.toString())){
			   					if(row > index){
			   						table.setValueAt(SELECT_LANG, row, 2);
			   					} else {
			   						table.setValueAt(SELECT_LANG, index, 2);
			   					}
				   			}	   		
			   			}		
			   			index = index+1;
			   			if(row == index){
			   				index++;
			   			}
			   			if(index < table.getRowCount()){
			   				obj = table.getValueAt(index, 2);
			   			} else {
			   				obj = null;
			   			}
			   		}
	   			}
			} else if (column == 2){
				langVal = comboBox.getSelectedItem();
				typeVal = (String) table.getValueAt(row, 1);
				
				int index = row+1;
	   			Object obj;
	   			Object lang;
	   			if(index <table.getRowCount()){
	   				while(true){
		   				obj = table.getValueAt(index, 1);
		   				lang = table.getValueAt(index, 2);
		   				if(obj instanceof TableSubHeaderObject){
		   					break;
		   				}
		   				
			   			if(obj.toString().equals(typeVal)){
			   				if(lang != null && lang.equals(langVal)) {
								table.setValueAt(SELECT_LANG, index, 2);
							}
			   			}		
			   			index = index+1;
			   			
			   			if(index == table.getRowCount()){
			   				break;
			   			}
		   			}
	   			}
			}			
			updateButtonStates();
		}
	}
}

