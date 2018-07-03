package mpi.eudico.client.annotator.transcriptionMode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.HTMLViewer;
import mpi.eudico.client.annotator.layout.TranscriptionManager;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Dialog to select the linguistic types for the columns 
 * in the transcription table
 * 
 * @author Aarthy Somasundaram
 *
 */
@SuppressWarnings("serial")
public class TranscriptionModeSettingsDlg extends ClosableDialog implements ActionListener, ChangeListener, ItemListener{
	
    private final String COLUMN_NO = ElanLocale.getString("TranscriptionModeSettingsDlg.Column.No");    
    private final String COLUMN_TYPE = ElanLocale.getString("TranscriptionModeSettingsDlg.Column.SelectType");    
    private final String DEFAULT_VALUE = ElanLocale.getString("TranscriptionModeSettingsDlg.DefaultValue");    
    
	private Transcription transcription ;
    private ElanLayoutManager layoutManager;    
    
    private JComboBox fontSizeComboBox;    
    private JSpinner spinner;    
    private JTable table;
    private DefaultTableModel model; 
    private JButton butSelectTiers;
	private JButton helpButton;
	private JButton applyButton;
    private JButton cancelButton;
    private JButton addColButton;
    private JButton deleteColButton;
    
    private List<String> columnTypeList;
    private List<String> hiddenTiersList;
    
    PossibleTypesExtractor computeTypes;   
 	
 	/** structure : HashMap< top level parent tiers, 
     * 				, List<tiers linked to the parent tier>>
     */
 	private Map<TierImpl, List<TierImpl>> tierMap; 	
    
 	// current transcription table settings
    private final List<String> currentColumnTypeList;   
    
    private final List<String> currentHiddenTiersList;
    
    private final Map<TierImpl, List<TierImpl>> currentTierMap; 	     	
 	
    private SelectChildTiersDlg dialog;
    private int fontSize;
 	
 	private boolean valueChanged = false;	
 	
 	private final int TYPE_COL_INDEX = 1;  	
 	
 	/**
 	 * Creates a TranscriptionModeSettingsDlg instance
 	 * 
 	 * @param layoutManager, ElanLayoutManager
 	 * @param columnTypes,   list of selected types
 	 * @param hashMap, tierMap
 	 * @param hiddenTiers, hiddenTiers list
 	 * @param size,   font size of the transcription table
 	 */
    public TranscriptionModeSettingsDlg(ElanLayoutManager layoutManager, List<String> columnTypes, Map<TierImpl, List<TierImpl>> hashMap, List<String> hiddenTiers, Integer size){
        super(layoutManager.getElanFrame(), ElanLocale.getString("TranscriptionModeSettingsDlg.Title"), true);        
       
        transcription = layoutManager.getViewerManager().getTranscription();
        this.layoutManager = layoutManager;   
        computeTypes = new PossibleTypesExtractor((TranscriptionImpl)transcription);
        currentHiddenTiersList = new ArrayList<String>();
	 	currentColumnTypeList = new ArrayList<String>();
	 	currentTierMap = new HashMap<TierImpl, List<TierImpl>>();	       
	 	if(hashMap != null){
	 		currentTierMap.putAll(hashMap);
	 	}
	 	
	 	if(hiddenTiers != null){
	 		currentHiddenTiersList.addAll(hiddenTiers);		
	 	}
	 	
		if(columnTypes != null){
			currentColumnTypeList.addAll(columnTypes);
		}
		
		initComponents();		
		if(size != null){			
			fontSizeComboBox.setSelectedItem(size);
			fontSize = size;
		}
		
		if(currentColumnTypeList != null){
			List<String> possibleTypes;
			if(currentColumnTypeList.size() >= 1){
				possibleTypes =  computeTypes.getPossibleTypesForColumn(1, columnTypeList); // all the types in the transcription
				if(possibleTypes.contains(currentColumnTypeList.get(0))){
					columnTypeList.add(currentColumnTypeList.get(0));
					table.setValueAt(currentColumnTypeList.get(0), 0,TYPE_COL_INDEX );
				
					for(int i=1 ; i< currentColumnTypeList.size(); i++){
						int column = i+1;
						possibleTypes = computeTypes.getPossibleTypesForColumn(column, columnTypeList);
						if(possibleTypes.contains(currentColumnTypeList.get(i))){
							columnTypeList.add(currentColumnTypeList.get(i));
							model.addRow(new Object[]{column ,currentColumnTypeList.get(i)});
						} 
					}					
					spinner.setValue(table.getRowCount());
				}
			}
		}
		
		applyButton.setEnabled(false);
		enableOrDisableSeletTierButton();
		postInit();
    }
    
    /**
     * Pack, size and set location.
     */
    private void postInit() {
    	pack();       
        setResizable(true);
        setLocationRelativeTo(getParent());
    }
	
	/**
     * Initializes the components for ui.
     */
    private void initComponents() {    
    	// initialize all the variables
		hiddenTiersList = new ArrayList<String>();
	 	columnTypeList = new ArrayList<String>();
	 	tierMap = new HashMap<TierImpl, List<TierImpl>>();	 
       
	 	// DefaultTableModel
	 	model = new DefaultTableModel(){ 
	 		@Override
			public boolean isCellEditable(int row, int column) {        	
	 			if(getColumnName(column).equals(COLUMN_NO)){
	 				return false;
	 			}    
	 			
	 			if(table.getValueAt(row, column) == null){
	 				return false;
	 			}
	 			
	 			if(table.getValueAt(row, column).toString().equals(DEFAULT_VALUE)){
	 				return false;
	 			}
	 		
	 			return true;
       	 	}
        };        
        model.setColumnIdentifiers(new String[] {COLUMN_NO, COLUMN_TYPE});  
        
        //DefaultTableCellRenderer
        DefaultTableCellRenderer render = new DefaultTableCellRenderer(){
       	 	@Override
			public Component getTableCellRendererComponent(JTable table,
       			 Object value, boolean isSelected, boolean hasFocus, int row,
       			 	int column){
       		 
       	 		Component cell = super.getTableCellRendererComponent(table, value, 
       				 isSelected, hasFocus, row, column);
       		 
       	 		if(value == null){
       	 			return cell;
       	 		}       		 
       	 		if(value.equals(TranscriptionManager.COMBOBOX_DEFAULT_STRING)){    			   
       	 			cell.setForeground(Color.GRAY);
       	 		} else{
       	 			cell.setForeground(Color.BLACK);
       	 		}       		 
       	 		return cell;    		   
       	 	}
        };
        
        // table settings
        table = new JTable(model);
       // table.setFont(Constants.DEFAULTFONT);
        table.setCellSelectionEnabled(true);	
        table.setDefaultEditor(Object.class, new TableCellEditor());  
        table.setDefaultRenderer(Object.class, render);      
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);      
        table.setSelectionBackground(Color.WHITE);        
        table.setRowHeight(table.getRowHeight()+ 5);        
        table.getColumnModel().getColumn(0).setMinWidth(50);
        //table.getColumnModel().getColumn(0).setPreferredWidth(75);
        table.getColumnModel().getColumn(0).setMaxWidth(50);   
        
        fontSizeComboBox = new JComboBox();
		for (int element : Constants.FONT_SIZES) {
			fontSizeComboBox.addItem(element);
		}
		fontSizeComboBox.addItemListener(this);			
		
		spinner = new JSpinner( new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));	
		model.addRow(new Object[]{1, TranscriptionManager.COMBOBOX_DEFAULT_STRING});
		spinner.addChangeListener(this);	
		
		butSelectTiers = new JButton(ElanLocale.getString("TranscriptionModeSettingsDlg.SelectTier"));		
		butSelectTiers.setEnabled(false);
		
		helpButton = new JButton(new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Help24.gif")));
		helpButton.setToolTipText(ElanLocale.getString("Button.Help.ToolTip"));
		helpButton.setPreferredSize(new Dimension(24,24));
        
        addColButton = new JButton(new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Plus16.gif")));               
        addColButton.setPreferredSize(new Dimension(20,20));
        
        deleteColButton = new JButton(new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Minus16.gif")));
        deleteColButton.setPreferredSize(new Dimension(20,20));
        
        applyButton = new JButton();
        applyButton.setText(ElanLocale.getString("Button.Apply"));
        applyButton.setEnabled(false);          
        
        cancelButton = new JButton();
        cancelButton.setText(ElanLocale.getString("Button.Cancel"));
        
        cancelButton.addActionListener(this);
	    applyButton.addActionListener(this);
	    butSelectTiers.addActionListener(this);
	    helpButton.addActionListener(this);	    
	    addColButton.addActionListener(this);
	    deleteColButton.addActionListener(this);
        
        JLabel fontSizeLabel   = new JLabel(ElanLocale.getString("TranscriptionModeSettingsDlg.FontSize"));
        JLabel columnNoLabel   = new JLabel(ElanLocale.getString("TranscriptionModeSettingsDlg.SelectColumns"));
        JScrollPane scroller = new JScrollPane(table);       
        
        Insets inset = new Insets(5, 10, 5, 10);       
        
        //button panel
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.anchor = GridBagConstraints.WEST;
	    gbc.insets = inset;
	    buttonPanel.add(applyButton, gbc);

	    gbc.gridx = 1;		   
	    buttonPanel.add(cancelButton, gbc);
	    
	    //column selection panel
	    JPanel columnPanel = new JPanel(new GridBagLayout());	    
	    gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		columnPanel.add(spinner,gbc);	   
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;		
		columnPanel.add(addColButton,gbc);	 
		
		gbc.gridx = 2;		
		columnPanel.add(deleteColButton,gbc);	             
		
		// main layout
		getContentPane().setLayout(new GridBagLayout());
      
	    gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.insets = inset;
		getContentPane().add(helpButton,gbc);	   
		
		gbc.gridx = 0;
		gbc.gridy = 1;		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(fontSizeLabel,gbc);		
		
		gbc.gridx = 1;			
		getContentPane().add(fontSizeComboBox,gbc);	
		
		gbc.gridx = 0;
		gbc.gridy = 2;			
		getContentPane().add(columnNoLabel,gbc);	
		
		gbc.gridx = 1; 
		getContentPane().add(columnPanel,gbc);	
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;	  
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;	   
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		getContentPane().add(scroller,gbc);	 
		
		gbc.gridy = 4;
	    gbc.gridwidth = 1;	   
	    gbc.anchor = GridBagConstraints.WEST;	 
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weighty = 0.0;
	    getContentPane().add(butSelectTiers,gbc);
		
	    gbc.gridx = 1;		
		gbc.anchor = GridBagConstraints.EAST;		
		getContentPane().add(buttonPanel, gbc);	
	    
 	    addWindowListener(new WindowAdapter() {
 	    	@Override
			public void windowClosing(WindowEvent we) {
 	    		doClose();
            }
        });
    }   
  
    
	
	/**
	 * Enables or disables the option to select the tiers button
	 * according to the types selected
	 * 
	 * @param type, the selected type
	 */
	private void enableOrDisableSeletTierButton(){		
		butSelectTiers.setEnabled(false);
		if(columnTypeList.size() >=1){
			butSelectTiers.setEnabled(true);
		}
	}
	
	/**
	 * Returns the list of all select types
	 * 
	 * @return
	 */
	public List<String> getColumnTypeList() {		
		return columnTypeList;
	}	
	
	/**
	 * Returns the tierMap 
	 * 
	 * @return tierMap
	 */
	public Map<TierImpl, List<TierImpl>> getTierMap(){
	   	return tierMap;
	}
	
	/**
	 * Returns the list of all hidden tiers
	 * 
	 * @return
	 */
	public List<String> getHiddenTiersList() {		
		return hiddenTiersList;
	}
	
	/**
	 * Returns the font size for the table
	 * 
	 * @return the font size
	 */
	public Integer getFontSize(){
		return (Integer)fontSizeComboBox.getSelectedItem();
	}
    
    /**
     * Closes this dialog
     */
    private void doClose() {
		setVisible(false);
		dispose();
	} 
    
    private void doApply(){
    	valueChanged = false;
    	
    	if(currentColumnTypeList != null){
        	valueChanged = !currentColumnTypeList.equals(columnTypeList);
        } else if(columnTypeList.size() > 0){
        	valueChanged = true;
        }
    	
    	if(dialog != null){    		
    		if(valueChanged){
    			if(!columnTypeList.equals(dialog.getColumnTypes())){
    				tierMap = null;
    				hiddenTiersList = null;	
    			}
        	} else if(dialog.isValueChanged()){
        		valueChanged = true;  		
        	} else if(tierMap != null && 
        			(tierMap.size() != currentTierMap.size() ||
        			tierMap.equals(currentTierMap))){
        		valueChanged = true;  		        		
        	} else if(hiddenTiersList != null && 
        			(hiddenTiersList.size() != currentHiddenTiersList.size()  ||
        			hiddenTiersList.equals(currentHiddenTiersList))){
        		valueChanged = true;  		        		
        	}
    	}     	
    	doClose();
    }
    
    private void showSelectTiersDialog(){      	
    	
    	if(dialog != null){
    		if(!columnTypeList.equals(dialog.getColumnTypes())){
        		if(currentColumnTypeList != null && currentColumnTypeList.equals(columnTypeList)){
            		dialog = new SelectChildTiersDlg(layoutManager, currentTierMap, currentHiddenTiersList, currentColumnTypeList);            		
            		tierMap = new HashMap<TierImpl, List<TierImpl>>();
        			tierMap.putAll(currentTierMap);
        			hiddenTiersList = new ArrayList<String>();
        			hiddenTiersList.addAll(currentHiddenTiersList);
        			columnTypeList.clear();
        			columnTypeList.addAll(currentColumnTypeList);        			
            	} else {
            		tierMap = null;
            		hiddenTiersList = null;	
            	}
    		}    		
    		dialog = new SelectChildTiersDlg(layoutManager, tierMap, hiddenTiersList, columnTypeList);
    		
    	} else {   
    		boolean valueChanged = false;
    		if(currentColumnTypeList != null){
            	valueChanged = !currentColumnTypeList.equals(columnTypeList);
            } else if(columnTypeList.size() > 0){
            	valueChanged = true;
            }
    		
    		if(!valueChanged){
    			tierMap.clear();
    			tierMap.putAll(currentTierMap);
    			hiddenTiersList.clear();
    			hiddenTiersList.addAll(currentHiddenTiersList);
    			columnTypeList.clear();
    			columnTypeList.addAll(currentColumnTypeList);
    		}
    			
    		dialog = new SelectChildTiersDlg(layoutManager, tierMap, hiddenTiersList, columnTypeList);
    		
    	}
   
    	
		dialog.setVisible(true);
		
		if(dialog.isValueChanged()){
			tierMap = dialog.getTierMap();
			hiddenTiersList = dialog.getHiddenTiers();	
			applyButton.setEnabled(true);
		}	
    }
    
    @Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == applyButton){
			doApply();		
		} else if (e.getSource() == cancelButton){	
			columnTypeList = null;
			hiddenTiersList = null;	
			tierMap = null;
			valueChanged = false;
			doClose();
		} else if(e.getSource() == butSelectTiers){			
			showSelectTiersDialog();
		} else if(e.getSource() == helpButton){			
			showHelpDialog();
		} else if(e.getSource() == addColButton){	
			int n = ((Integer) spinner.getValue()).intValue();	
			n= n+1;
			spinner.setValue(n);			
		} else if(e.getSource() == deleteColButton){			
			int n = ((Integer) spinner.getValue()).intValue();	
			n= n-1;
			spinner.setValue(n);
		}
	}
    
    /**
     * Add rows to the table
     */
    private void addRows(){
    	int n = ((Integer) spinner.getValue()).intValue();
    	Object value = null;
    	while(table.getRowCount() < n){		
    		if(n == 1){
    			model.addRow(new Object[]{table.getRowCount()+1 , TranscriptionManager.COMBOBOX_DEFAULT_STRING});
    			continue;
    		}
    		if(table.getRowCount() >= 1){
    			value = table.getValueAt(table.getRowCount()-1, TYPE_COL_INDEX);
    		}			
			if( value == null || 
					value.equals(TranscriptionManager.COMBOBOX_DEFAULT_STRING) || 
					value.equals(DEFAULT_VALUE)){
				if(table.getRowCount() <= 0){
					model.addRow(new Object[]{table.getRowCount()+1 , TranscriptionManager.COMBOBOX_DEFAULT_STRING});
				}else{
					model.addRow(new Object[]{table.getRowCount()+1 , null});
				}				
			} else {
				model.addRow(new Object[]{table.getRowCount()+1 , TranscriptionManager.COMBOBOX_DEFAULT_STRING});
			}			
		}
    }
    
    /**
     * Deletes rows from the table
     */
    private void deleteRows(){
    	int n = ((Integer) spinner.getValue()).intValue();  
    	Object value;
    	while(table.getRowCount() > n){ 
    		model.removeRow(table.getRowCount()-1);
		}
    	
    	for(int i= columnTypeList.size(); i > table.getRowCount(); i--){
    		columnTypeList.remove(i-1);
    	}
    }
    
    /**
     * Deletes all the values after the given row
     * 
     * @param row, reference row after which all the
     * 			   values in the table are deleted
     */
    private void deleteValuesAfter(int row){
    	for(int i= row+1 ; i< table.getRowCount(); i++){
    		if(i == row+1){
    			table.setValueAt(TranscriptionManager.COMBOBOX_DEFAULT_STRING, i, TYPE_COL_INDEX);
    		} else {
    			table.setValueAt(null, i, TYPE_COL_INDEX);
    		}
    	}
    	
    	while (columnTypeList.size() > row) {
			columnTypeList.remove(columnTypeList.size()-1);
		}
    }   
    
    @Override
	public void stateChanged(ChangeEvent e) {	
    	int n = ((Integer) spinner.getValue()).intValue();			
    	
    	if(n == 0){
    		deleteColButton.setEnabled(false);
    	} else {
    		deleteColButton.setEnabled(true);
    	}    	
    	
		if(e.getSource() == spinner){		
			if(table.getRowCount() < n){				
				addRows();
			} else 	if(table.getRowCount() > n){
				deleteRows();
			}
		}		
		applyButton.setEnabled(true);	
    }
    
    private void showHelpDialog() {
    	try {
    		HTMLViewer helpViewer = new HTMLViewer("/mpi/eudico/client/annotator/resources/transcriptionModeHelpDoc.html", 
    			false, ElanLocale.getString("TranscriptionModeSettingsDlg.Help"));
    		JDialog dialog = helpViewer.createHTMLDialog(this);
    		dialog.pack();
    		dialog.setSize(500, 600);
    		dialog.setLocationRelativeTo(this);
    		dialog.setVisible(true);
    	} catch (IOException ioe) {
    		// message box
    		JOptionPane.showMessageDialog(this, (ElanLocale.getString("Message.LoadHelpFile") + " " + ioe.getMessage()), 
    				ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE, null);
    	}
    }    
        
    public boolean isValueChanged(){
    	return valueChanged;
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
    	private String value; 

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
    		this.value = value.toString();
    		
    		List<String> possibleTypes = computeTypes.getPossibleTypesForColumn(row+1, columnTypeList);
    		comboBox = new JComboBox();  
    		
    		if(possibleTypes.size() == 0){
    			table.setValueAt(DEFAULT_VALUE, row, column);    			
    			return null;
    		}
    		
    		for(int i=0; i< possibleTypes.size(); i++){							
				comboBox.addItem(possibleTypes.get(i));
    		}
    		
    		if(value.equals(TranscriptionManager.COMBOBOX_DEFAULT_STRING)){
    			comboBox.setSelectedItem(null);
    		} else if(possibleTypes.contains(value)){
    			comboBox.setSelectedItem(value);
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
			if(e.getSource() == comboBox ){				
				if (comboBox .getSelectedItem() != null){	
					table.editingStopped(new ChangeEvent(this));
					if(value.equals(comboBox.getSelectedItem())){
						return;
					}
					
					applyButton.setEnabled(true);	
					
					if(table.getRowCount()-1 > row){
						if(table.getValueAt(row+1, 1) == null || table.getValueAt(row+1, 1).toString().equals(DEFAULT_VALUE)){
							table.setValueAt(TranscriptionManager.COMBOBOX_DEFAULT_STRING, row+1, 1);
						} else {
							Object value = table.getValueAt(row+1, 1);
							if(!value.equals(DEFAULT_VALUE) && !value.equals(TranscriptionManager.COMBOBOX_DEFAULT_STRING)){
								deleteValuesAfter(row);
							}
						}
					} else 	if(row == (table.getRowCount()-1)){							
						((DefaultTableModel)table.getModel()).addRow( new Object[]{table.getRowCount()+1, TranscriptionManager.COMBOBOX_DEFAULT_STRING});	
						spinner.setValue(table.getRowCount());						
					}	
					
					
					if(row <= columnTypeList.size()-1){
						columnTypeList.set( row , comboBox.getSelectedItem().toString()); 	
					} else {
						columnTypeList.add(comboBox.getSelectedItem().toString()); 	
					}
				} 
				enableOrDisableSeletTierButton();
			}
		}
    }
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == this.fontSizeComboBox){
			if(fontSize != (Integer)fontSizeComboBox.getSelectedItem()){
				fontSize = (Integer)fontSizeComboBox.getSelectedItem();
				applyButton.setEnabled(true);
			}
		}
	}
	
	
//	class ComputePossibleLinguisticTypes{
//		private List<String> linkedTypes;   
//	    private List<TierImpl> parentTierList;  
//	    /** structure : HashMap< top level parent tiers, 
//	     * 				map< typeName, List< number of tiers of this type under this toplevel tier>>
//	     */
//	 	private HashMap<TierImpl, HashMap<String, Integer>> typeTierMap;
//	 	
//	 	private String refType;	
//	   
//		
//		public ComputePossibleLinguisticTypes(){
//			// initialize all the variables
//	        typeTierMap = new HashMap<TierImpl, HashMap<String, Integer>>();	
//			parentTierList = new ArrayList<TierImpl>();   
//			linkedTypes = new ArrayList<String>();
//		}
//		
//		public void setReferenceType(String typeName){
//			refType = typeName;
//		}
//		
//		/**
//	     * Returns a hashmap with the available typeNames
//	     * and the number of tiers(as a value) of each type related to the given
//	     * reference tier
//	     * 
//	     * @param tier, the top level parent tier
//	     * @return tierTypeMap, a map of the available types and the number of tiers of that types
//	     */
//	    private HashMap<String, Integer> getTierTypeMap(TierImpl tier){    	   	
//	    	HashMap<String, Integer> tierTypeMap = new HashMap<String, Integer>();	    	
//	    	List childTiers = tier.getChildTiers();    	
//	    		
//	    	for(int x=0; x < childTiers.size(); x++){
//				TierImpl childTier = (TierImpl) childTiers.get(x);
//				String typeName = null ;
//				if(childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
//					// get all the types of the tiers depending on this child tier
//					List dependentTiers = childTier.getDependentTiers();
//					for(int y=0; y < dependentTiers.size(); y++) {
//						TierImpl dependantTier = (TierImpl) dependentTiers.get(y);	
//						typeName = dependantTier.getLinguisticType().getLinguisticTypeName();
//						if(dependantTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
//							if(tierTypeMap.containsKey(typeName)){
//								tierTypeMap.put(typeName, tierTypeMap.get(typeName)+1);
//							} else {
//								tierTypeMap.put(typeName, 1);
//							}
//							
//							if(!linkedTypes.contains(dependantTier.getLinguisticType().getLinguisticTypeName())){
//								linkedTypes.add(dependantTier.getLinguisticType().getLinguisticTypeName());
//							}
//						}
//					}						
//					typeName = childTier.getLinguisticType().getLinguisticTypeName();
//					
//					if(tierTypeMap.containsKey(typeName)){
//						tierTypeMap.put(typeName, tierTypeMap.get(typeName)+1);
//					} else {
//						tierTypeMap.put(typeName, 1);
//					}
//					
//					if(!linkedTypes.contains(childTier.getLinguisticType().getLinguisticTypeName())){
//						linkedTypes.add(childTier.getLinguisticType().getLinguisticTypeName());
//					}
//				}
//			}    	
//	    	return tierTypeMap;   	
//	    }
//	    
//	    /**
//	     * Get all the types linked with the first selected type
//	     */
//	    private void getLinkedTypes(){ 
//	    	linkedTypes.clear();
//	    	parentTierList.clear();
//	    	
//	    	List tierList = transcription.getTiersWithLinguisticType(refType);	
//	    	List<TierImpl> parentTiers = new ArrayList<TierImpl>();
//	    	
//			for(int i= 0 ; i < tierList.size(); i++){
//				TierImpl tier = (TierImpl) tierList.get(i);		
//				
//				//if type selected for the first column is not symbolic associated type
//				if(tier.getLinguisticType().getConstraints() == null || tier.getLinguisticType().getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION ){	
//					if(parentTierList.contains(tier)){
//			    		continue;
//			    	} 
//			    	parentTierList.add(tier);
//			    	typeTierMap.put(tier, getTierTypeMap(tier));				
//				} else {				
//					TierImpl parentTier = (TierImpl) tier.getParentTier();
//					while(parentTier != null && (parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)){
//						parentTier = (TierImpl) parentTier.getParentTier();
//					}	
//					
//					if(parentTierList.contains(parentTier)){
//			    		continue;
//			    	} 
//					parentTierList.add(parentTier);				
//					
//					typeTierMap.put(parentTier, getTierTypeMap(parentTier));	
//				}
//			}
//	    }
//	    
//	    /**
//	     * Returns the list of all possible types for the next column
//	     * 
//	     * @param column, the column number
//	     * @return possibleTypes, a list of all possible types for this column
//	     */
//	    private List<String> getPossibleTypesForColumn(int column){
//	    	
//	    	List<String> possibleTypes = new ArrayList<String>();
//	    	
//	    	if(column == 1){ 
//	    		// for first column any linguistic type can be selected
//	    		List types = transcription.getLinguisticTypes();		    
//	    		for(int i = 0; i < types.size(); i++){
//	    			LinguisticType type = (LinguisticType)types.get(i);	
//	    			Vector tierList = transcription.getTiersWithLinguisticType(type.getLinguisticTypeName());
//	    			if( tierList!= null && tierList.size() > 0){
//	    				possibleTypes.add(type.getLinguisticTypeName());
//	    			}
//	    		}
//	    		return possibleTypes;
//	    	}
//	    	
//	    	if(column == 2 ){
//	    		// for other columns 
//	    		getLinkedTypes();
//	    	}
//	    	
//	    	for(int x=0; x < parentTierList.size(); x++){
//	    		HashMap <String,Integer> _map = typeTierMap.get(parentTierList.get(x));
//	    		for(int i=0; i < linkedTypes.size(); i++){
//	    			String typeName = linkedTypes.get(i);
//	    			if (_map.containsKey(typeName)){    				
//	    				int noOfTiers = _map.get(typeName).intValue();
//	    				if(noOfTiers > 0){
//	    					for(int c=0; c < columnTypeList.size(); c++){    						
//	    						if(c == column-1){
//	    							break;
//	    						}
//	    						if(typeName.equals(columnTypeList.get(c))){
//	    							noOfTiers = noOfTiers -1;
//	    							if(noOfTiers <= 0){
//	    								noOfTiers = 0;
//	    								break;
//	    							}
//	    						}
//	    					}    					
//	    				}
//	    				
//	    				if(noOfTiers > 0){
//	    					if(!possibleTypes.contains(typeName)){
//	        					possibleTypes.add(typeName);
//	        				}
//	    				}
//	    			}
//	    		}
//	    	}    	
//	    	return possibleTypes;	
//	    }  
//	}
}



