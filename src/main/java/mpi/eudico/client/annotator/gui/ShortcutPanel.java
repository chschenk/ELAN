package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;

/**
 * A panel showing all actions that can have a shortcut in a table an 
 * that allows to select one and change the shortcut.
 * 
 * @author alekoe
 *
 */
@SuppressWarnings("serial")
public class ShortcutPanel extends JPanel implements ActionListener {
	
	private JTabbedPane shortcutPane;	
	private JComboBox sortBox;
	private JButton cancelButton;
	private JButton editButton;
	private JButton reloadButton;
	private JButton reloadAllButton;
	private JButton saveButton;
	public Boolean replaceShortcut = false;			
	public boolean saveChanges = false;

	
	/** shortcut keystrokes for each mode
     *  structure Map< modeName, Map<actionName, keystroke>>*/ 
    private Map<String, Map<String, KeyStroke>> shortcutKeyStrokesMap;
    
    Map< KeyStroke, Map<String, List<String>>> keyStrokeClashMap;
    
    // < KeyStrokeName, list<modeNames>
    private Map<String,List<String>> keyStrokeModeMap;
    
    // <keystrokeName, color>
    private Map<String, Color> colorMap;     
    
    private Map<String,Integer> clashModeMap;
    

	private final ShortcutsUtil scu = ShortcutsUtil.getInstance();
	
	private final String[] columnNames = {ElanLocale.getString("Shortcuts.Table.Description"),
			ElanLocale.getString("Shortcuts.Table.Category"),
			ElanLocale.getString("Shortcuts.Table.Key"),
			"ActionID",
			"Keycode",
			"Modifiercode"};
	
	// index of columns in the table
	private final int desc_col = 2;	
	private final int action_col = 3;	
	private final int keycode_col = 4;
	private final int mod_col = 5;
	
	/**
	 * Constructor
	 */
    public ShortcutPanel() {
        super();
        Map<String, KeyStroke> map;
        shortcutKeyStrokesMap = new HashMap<String,Map<String, KeyStroke>>();
        keyStrokeClashMap = new HashMap< KeyStroke, Map<String, List<String>>>();
        keyStrokeModeMap = new HashMap<String,List<String>>();
        colorMap = new HashMap<String, Color>();
        clashModeMap = new HashMap<String, Integer>();
        
        map = new HashMap<String, KeyStroke>();
        map.putAll(scu.getShortcutKeysOnlyIn(ELANCommandFactory.COMMON_SHORTCUTS));
        shortcutKeyStrokesMap.put(ELANCommandFactory.COMMON_SHORTCUTS, map);
        
        map = new HashMap<String, KeyStroke>();
        map.putAll(scu.getShortcutKeysOnlyIn(ELANCommandFactory.ANNOTATION_MODE));
        shortcutKeyStrokesMap.put(ELANCommandFactory.ANNOTATION_MODE, map);
        
        map = new HashMap<String, KeyStroke>();
        map.putAll(scu.getShortcutKeysOnlyIn(ELANCommandFactory.TRANSCRIPTION_MODE));
        shortcutKeyStrokesMap.put(ELANCommandFactory.TRANSCRIPTION_MODE, map);
        
        map = new HashMap<String, KeyStroke>();
        map.putAll(scu.getShortcutKeysOnlyIn(ELANCommandFactory.SYNC_MODE));
        shortcutKeyStrokesMap.put(ELANCommandFactory.SYNC_MODE, map);
        
        map = new HashMap<String, KeyStroke>();
        map.putAll(scu.getShortcutKeysOnlyIn(ELANCommandFactory.SEGMENTATION_MODE));
        shortcutKeyStrokesMap.put(ELANCommandFactory.SEGMENTATION_MODE, map);
        
        map = new HashMap<String, KeyStroke>();
        map.putAll(scu.getShortcutKeysOnlyIn(ELANCommandFactory.INTERLINEARIZATION_MODE));
        shortcutKeyStrokesMap.put(ELANCommandFactory.INTERLINEARIZATION_MODE, map);
   
        setLayout(new GridBagLayout());
        
        initializeTabPane();  
        
        //Add the scroll pane to this panel.
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(2, 6, 2, 6);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        add(shortcutPane, gbc);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 2, 2));
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        add(buttonPanel, gbc);

        JLabel sortLabel = new JLabel(ElanLocale.getString("Shortcuts.Table.SortBy"));
        sortLabel.setHorizontalAlignment(JLabel.RIGHT);
        sortLabel.setVerticalTextPosition(AbstractButton.BOTTOM);
        sortLabel.setHorizontalTextPosition(AbstractButton.LEADING);        
        buttonPanel.add(sortLabel);
        
        String[] sortOptions = new String[] {ElanLocale.getString("Shortcuts.Table.Description"),ElanLocale.getString("Shortcuts.Table.Category"),ElanLocale.getString("Shortcuts.Table.Key")} ;
        sortBox = new JComboBox(sortOptions);
        sortBox.addActionListener(this);
        buttonPanel.add(sortBox);
        
        editButton = new JButton(ElanLocale.getString("Shortcuts.Button.Edit"));        
        editButton.addActionListener(this);        
        editButton.setVerticalTextPosition(AbstractButton.CENTER);
        editButton.setHorizontalTextPosition(AbstractButton.LEADING);
        buttonPanel.add(editButton);
        
        saveButton = new JButton(ElanLocale.getString("Button.Save"));        
        saveButton.addActionListener(this);        
        saveButton.setVerticalTextPosition(AbstractButton.CENTER);
        saveButton.setHorizontalTextPosition(AbstractButton.LEADING);     
        saveButton.setEnabled(false);
        buttonPanel.add(saveButton);
        
        reloadButton = new JButton(ElanLocale.getString("Shortcuts.Button.Default"));
        reloadButton.addActionListener(this);        
        reloadButton.setVerticalTextPosition(AbstractButton.CENTER);
        reloadButton.setHorizontalTextPosition(AbstractButton.LEADING);
        buttonPanel.add(reloadButton);
        
        reloadAllButton = new JButton(ElanLocale.getString("Shortcuts.Button.RestoreAll"));
        reloadAllButton.addActionListener(this);        
        reloadAllButton.setVerticalTextPosition(AbstractButton.CENTER);
        reloadAllButton.setHorizontalTextPosition(AbstractButton.LEADING);
        buttonPanel.add(reloadAllButton);

        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);        
        cancelButton.setVerticalTextPosition(AbstractButton.CENTER);
        cancelButton.setHorizontalTextPosition(AbstractButton.LEADING);
        buttonPanel.add(cancelButton);
        
        
        // display a message
        if(keyStrokeClashMap.size() > 0){
        	JOptionPane.showMessageDialog(this, ElanLocale.getString("Shortcuts.Warning.Clashes"), ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
        }        
    }
    
    // load the tabs in the tabbed pane
    private void initializeTabPane(){
    	shortcutPane = new JTabbedPane();    	
        JScrollPane listScrollPane = new JScrollPane(getNewTable());   
        shortcutPane.addTab(ElanLocale.getString(ELANCommandFactory.COMMON_SHORTCUTS), listScrollPane);    
        
        listScrollPane = new JScrollPane(getNewTable());   
        shortcutPane.addTab(ElanLocale.getString(ELANCommandFactory.ANNOTATION_MODE), listScrollPane);
        
        listScrollPane = new JScrollPane(getNewTable());   
        shortcutPane.addTab(ElanLocale.getString(ELANCommandFactory.SYNC_MODE), listScrollPane);
        
        listScrollPane = new JScrollPane(getNewTable());   
        shortcutPane.addTab(ElanLocale.getString(ELANCommandFactory.TRANSCRIPTION_MODE), listScrollPane);
        
        listScrollPane = new JScrollPane(getNewTable());   
        shortcutPane.addTab(ElanLocale.getString(ELANCommandFactory.SEGMENTATION_MODE), listScrollPane);      
        
        listScrollPane = new JScrollPane(getNewTable());   
        shortcutPane.addTab(ElanLocale.getString(ELANCommandFactory.INTERLINEARIZATION_MODE), listScrollPane);
        
        JTable table;
        for(int i=0; i< shortcutPane.getTabCount(); i++){
        	String modeName = getConstant(shortcutPane.getTitleAt(i));
        	table = (JTable) ((JScrollPane) shortcutPane.getComponentAt(i)).getViewport().getView(); 
        	table.setName(modeName);
        	
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            loadTableModel(model, modeName);  
        }
        
        computeClashes();
        
    }
    
    /**
     * 
     * @return
     */
    private JTable getNewTable(){
    	 DefaultTableModel model = new DefaultTableModel(0,columnNames.length);
    	 model.setColumnIdentifiers(columnNames);
         JTable table = new JTable(model){
         		// Override isCellEditable
         		@Override
				public boolean isCellEditable(int rowIndex, int colIndex) 
         		{
         			return false;   //Disallow the editing of any cell
         		}
         };
         
         table.getTableHeader().setReorderingAllowed(false);
         
         // hide columns ActionID, Keycode, Modifiercode
         table.getColumn("ActionID").setMinWidth(0);
         table.getColumn("ActionID").setMaxWidth(0);
         table.getColumn("Keycode").setMinWidth(0);
         table.getColumn("Keycode").setMaxWidth(0);
         table.getColumn("Modifiercode").setMinWidth(0);
         table.getColumn("Modifiercode").setMaxWidth(0);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         
         ShortcutClashRenderer renderer = new ShortcutClashRenderer();
         
         table.getColumn(ElanLocale.getString("Shortcuts.Table.Description")).setCellRenderer(renderer);
         table.getColumn(ElanLocale.getString("Shortcuts.Table.Category")).setCellRenderer(renderer);
         table.getColumn(ElanLocale.getString("Shortcuts.Table.Key")).setCellRenderer(renderer);
       
         return table;
    }
    
    /**
     * Loads all the shortcuts in the give tableModel
     * 
     * @param model, model to be loaded
     * @param allShortCuts, the map of all shortcuts to be loaded in the above given model
     */
    private void loadTableModel(DefaultTableModel model, String modeName){     
    	
    	Map<String, KeyStroke> allShortCuts = shortcutKeyStrokesMap.get(modeName);     	
    	List<String> allActions = new ArrayList<String>(); 
    	
    	Iterator<String> it = allShortCuts.keySet().iterator();
    	while (it.hasNext())
    	{     		
     		allActions.add(it.next());     		
     	}
     
    	String[] nameArray = allActions.toArray(new String[0]);
     
    	int numberOfShortCuts = allShortCuts.size();
    	for (int i=0;i<numberOfShortCuts;i++)        
    	{        		
     		String actionName = nameArray[i]; 
     		String description = scu.getDescriptionForAction(actionName);
     		String category = ElanLocale.getString(scu.getCategoryForAction(modeName, actionName));
     		KeyStroke ks = allShortCuts.get(actionName);
     		String keyStrokeName = scu.getDescriptionForKeyStroke(ks);
     		String keyCode = "";
     		String modCode = "";
     		if (ks != null)
     		{
     			keyCode = Integer.toString(ks.getKeyCode());
     			modCode = Integer.toString(ks.getModifiers());
     		}
     		String[] row = {description,category,keyStrokeName,actionName,keyCode,modCode};
     		model.addRow(row);
    	} 
    	
    	convertToKeyBoardClashMap(modeName);
    	
  	  	@SuppressWarnings("unchecked")
		List<Vector<Object>> data = model.getDataVector();
  	    Collections.sort(data, new ColumnSorter(0));
  	    // shouldn't there be some setDataVector() to let the model know that
  	    // the order has changed?
    }
    
    private void convertToKeyBoardClashMap(String modeName){    	
    	HashMap<KeyStroke, List<String>> map = new HashMap<KeyStroke, List<String>>();
    	List<String> actionList;
    	KeyStroke ks;
    	Iterator<Entry<String, KeyStroke>> it = shortcutKeyStrokesMap.get(modeName).entrySet().iterator();
    	while (it.hasNext()) {
        	Entry<String, KeyStroke> pair;
    		pair = it.next();
    		String action = pair.getKey();
    		ks = pair.getValue();
    		if(ks == null){
    			continue;
    		}
    		
    		if(map.containsKey(ks)){
    			actionList = map.get(ks);
    			actionList.add(action);
    		}else {
    			actionList = new ArrayList<String>();
    			actionList.add(action);
    			map.put(ks, actionList);
    		}
    	}
    	
    	if(map.size() > 0){
    		Iterator<Entry<KeyStroke, List<String>>> it2;
        	it2 = map.entrySet().iterator();
    		while( it2.hasNext()){
    			Entry<KeyStroke, List<String>>pair = it2.next();
    			ks = pair.getKey();
    			actionList = pair.getValue();
    			if(actionList != null){
    				if(!keyStrokeClashMap.containsKey(ks)){
    					keyStrokeClashMap.put(ks, new HashMap<String, List<String>>());
    				}
    				keyStrokeClashMap.get(ks).put(modeName, actionList);    				
    			}
    		}
    	}    	
    }
    
    //    keyStrokeClashMap.get(ks).put(modeName, actionList);   
    private void computeClashes(){
    	Map< KeyStroke, Map<String, List<String>>> clashMap =  new  HashMap< KeyStroke, Map<String, List<String>>>();
    	
    	Iterator<Entry<KeyStroke, Map<String, List<String>>>> it = keyStrokeClashMap.entrySet().iterator();
    	Entry<KeyStroke, Map<String, List<String>>> pair;
    	KeyStroke ks;
    	Map<String, List<String>> actionMap;
    	String modeName;
    	List<String> actionList;
    	while (it.hasNext()){
    		pair = it.next();
    		ks = pair.getKey();
    		actionMap = pair.getValue();    		
    		if(actionMap != null){
    			if(actionMap.size() == 1){
    				Iterator<Entry<String, List<String>>> itMap = actionMap.entrySet().iterator();
        			Entry<String, List<String>> pair1;
    				while(itMap.hasNext()){
    					pair1 = itMap.next();
    					modeName = pair1.getKey();
    					actionList = pair1.getValue();
    					if(actionList != null && actionList.size() > 1){
    						clashMap.put(ks, actionMap);
    					}
    				}    				
    			} else if (actionMap.size() > 1 ){
    				actionList = actionMap.get(ELANCommandFactory.COMMON_SHORTCUTS);
    				if(actionList != null && actionList.size() > 0){
    					clashMap.put(ks, actionMap);
    				}
    			}
    		}
    	}    	
    	keyStrokeClashMap = clashMap;
    	
    	
    	if(keyStrokeClashMap.size() > 0){
    		it = keyStrokeClashMap.entrySet().iterator();
    		while(it.hasNext()){
    			pair = it.next();
    			ks = pair.getKey();
    			actionMap = pair.getValue();
    			if(actionMap != null){
    				String ksName = scu.getDescriptionForKeyStroke(ks);
    				if(!colorMap.containsKey(ksName)){
    					colorMap.put(ksName, getNewColor());
    				}
    				Iterator<String> itKey = actionMap.keySet().iterator();
    				while(itKey.hasNext()){
    					modeName = itKey.next();
    					if(!keyStrokeModeMap.containsKey(ksName)){
    						keyStrokeModeMap.put(ksName, new ArrayList<String>());
    					}
    					keyStrokeModeMap.get(ksName).add(modeName);     					
    					
    					
    					if(!clashModeMap.containsKey(modeName)){
    						clashModeMap.put(modeName, 0);
    					}    					
    					clashModeMap.put(modeName, clashModeMap.get(modeName).intValue() +1);
    				}
    			}
    		}    		
    		highLightTabWithClashes();
    	}
    }
        
    /**
	 * Sets a random color for the given Tier
	 * 
	 * @param tierName
	 */
	private Color getNewColor(){		
		while(true){
			int r = (int)(Math.random()*255);
			int g = (int)(Math.random()*255);
			int b = (int)(Math.random()*255);
			
	        double FACTOR = 0.16;

	        Color c =  new Color((int) (255 - ((255 - r) * FACTOR)),
	        		(int) (255 - ((255 - g) * FACTOR)),
	        		(int) (255 - ((255 - b) * FACTOR)));
			
			if(c == Color.BLACK || c == Color.WHITE || colorMap.containsValue(c)){					
				continue;
			}else{
				return c;
			}
		}
	}
    
    private String getConstant(String tabName){
    	String constant = null;
    	
    	if(tabName.equals(ElanLocale.getString(ELANCommandFactory.COMMON_SHORTCUTS))){
    		constant = ELANCommandFactory.COMMON_SHORTCUTS;
    	} else if(tabName.equals(ElanLocale.getString(ELANCommandFactory.ANNOTATION_MODE))){
    		constant = ELANCommandFactory.ANNOTATION_MODE;
    	} else if(tabName.equals(ElanLocale.getString(ELANCommandFactory.TRANSCRIPTION_MODE))){
    		constant = ELANCommandFactory.TRANSCRIPTION_MODE;
    	} else if(tabName.equals(ElanLocale.getString(ELANCommandFactory.SYNC_MODE))){
    		constant = ELANCommandFactory.SYNC_MODE;
    	} else if(tabName.equals(ElanLocale.getString(ELANCommandFactory.SEGMENTATION_MODE))){
    		constant = ELANCommandFactory.SEGMENTATION_MODE;
    	} else if(tabName.equals(ElanLocale.getString(ELANCommandFactory.INTERLINEARIZATION_MODE))){
    		constant = ELANCommandFactory.INTERLINEARIZATION_MODE;
    	}
    	return constant;
    }  
    
    /**
     * actionListener method that catches whether a button is pressed and either
     * closes the window without changing anything (Cancel button)
     * opens an edit window for changing a shortcut (Edit button)
     * saves the current shortcuts to a preferences file (Save button)
     * reloads the default preferences (Reload button) 
     * 
     * if the event comes from the sortBox, sorting is triggered
     */
    @Override
	public void actionPerformed( ActionEvent e )
    {
    	if (e.getSource() == sortBox){
    		JComboBox cb = (JComboBox)e.getSource();
    		int index = cb.getSelectedIndex();	  
    		JTable table = null;
    		for(int i=0; i <shortcutPane.getTabCount(); i++){        		
    			table = (JTable) ((JScrollPane) shortcutPane.getComponentAt(i)).getViewport().getView(); 
    	  	  	DefaultTableModel dtmodel = (DefaultTableModel)table.getModel();
    	  	  	@SuppressWarnings("unchecked")
				List<Vector<Object>> data = dtmodel.getDataVector();
    	  	    Collections.sort(data, new ColumnSorter(index));
    	  	    dtmodel.fireTableStructureChanged();
    	        // rehide columns ActionID, Keycode, Modifiercode
    	        table.getColumn("ActionID").setMinWidth(0);
    	        table.getColumn("ActionID").setMaxWidth(0);
    	        table.getColumn("Keycode").setMinWidth(0);
    	        table.getColumn("Keycode").setMaxWidth(0);
    	        table.getColumn("Modifiercode").setMinWidth(0);
    	        table.getColumn("Modifiercode").setMaxWidth(0);
        	}
    	} 
    	else if (e.getSource() == editButton){
    		JTable table = (JTable) ((JScrollPane) shortcutPane.getComponentAt(shortcutPane.getSelectedIndex())).getViewport().getView(); 
    		int row = table.getSelectedRow();
	    	if (row > -1){	      		
	      		String selectedAction = (String) table.getValueAt(row, 3);
	      		String shortcutKey = (String) table.getValueAt(row, 2);
	      		String keycode = (String) table.getValueAt(row, 4);
      			String modcode = (String) table.getValueAt(row, 5);
      			List<Integer> codes = new ArrayList<Integer>(2);
      			if(shortcutKey == null || shortcutKey.length() <= 0){
      				codes = null;
      			} else {
      				codes.add(Integer.parseInt(keycode));
      				codes.add(Integer.parseInt(modcode));
      			}
      			
	    	  	createEditWindow(selectedAction, codes);
	    	}
    	}
    	else if (e.getSource() == saveButton){
    		if(!saveChanges){
    			return;
    		}    		
    		save();      		
      		SwingUtilities.getWindowAncestor(this).setVisible(false);		  
    	}
    	else if (e.getSource() == reloadButton){
    		String tabName = shortcutPane.getTitleAt(shortcutPane.getSelectedIndex());  
    		scu.restoreDefaultShortcutsForthisMode(getConstant(tabName));
    		restoreTab(tabName);
    		saveChanges = true;
    	}    	
    	else if (e.getSource() == reloadAllButton){    		  		
    		scu.restoreAll();  
    		for(int i=0; i <shortcutPane.getTabCount(); i++){  
        		restoreTab(shortcutPane.getTitleAt(i));
        	}  
    		saveChanges = true;
    	}
    	else if (e.getSource() == cancelButton){
	    	SwingUtilities.getWindowAncestor(this).setVisible(false);
	    	checkForSave();
		  	//JDialog jd = (JDialog) this.getParent().getParent().getParent();
		  	//jd.setVisible(false);
	    }
    	saveButton.setEnabled(saveChanges);
    }  
    
    /**
     * Saves the current shortcuts to the file
     * 
     */
    private void save(){
    	scu.saveCurrentShortcuts(scu.getStorableShortcutMap(shortcutKeyStrokesMap));
  		saveChanges = false;
	  	scu.readCurrentShortcuts();
	  	FrameManager.getInstance().updateShortcuts();	
    }
    
    /**
     * Checks for new changes and warns the user about it     
     */
    void checkForSave(){
    	if(saveChanges){
    		String strMessage = ElanLocale.getString("Shortcuts.Message.AskSave");   
            String strWarning = ElanLocale.getString("Message.Warning");	        	
        	int i = JOptionPane.showOptionDialog(this, strMessage, strWarning, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
       		if(i == JOptionPane.YES_OPTION){
       			save();
       		} else{
       			saveChanges = false;
       			scu.readCurrentShortcuts();
       		} 
    	}
    }
    
    /**
     * a method to ensure that the shortcuts stored in the member variable and 
     * those that are displayed in the table are the same
     */
    private void restoreTab(String tabName){    		  
    	Map<String,KeyStroke> allShortCuts = scu.getShortcutKeysOnlyIn(getConstant(tabName)) ;
    	shortcutKeyStrokesMap.put(getConstant(tabName), allShortCuts);
    	
    	int index = shortcutPane.indexOfTab(tabName);
		JTable table = (JTable) ((JScrollPane) shortcutPane.getComponentAt(index)).getViewport().getView(); 
    		
    	int noOfRows = table.getRowCount();
    	for (int i=0;i<noOfRows;i++)
    	{
    		// Initialise everything with 0
    		int tableKeyCode = 0;
    		int tableModCode = 0;
    		int dataKeyCode = 0;
    		int dataModCode = 0;
   			// get shortcut currently stored in the table
   			String tableKeyCodeAsString = (String) table.getValueAt(i, 4);
   			String tableModCodeAsString = (String) table.getValueAt(i, 5);
   			// if there is any, get the corresponding key codes
   			if (tableKeyCodeAsString != "")
   			{
   				tableKeyCode = Integer.parseInt(tableKeyCodeAsString);
   			}
   			if (tableModCodeAsString != "")
   			{
   				tableModCode = Integer.parseInt(tableModCodeAsString);
   			}
   			// get the key stroke from ShortcutUtils.shortcutKeyStrokes
   			String action = (String) table.getValueAt(i, 3);
   			KeyStroke aKeyStroke = allShortCuts.get(action);
   			// if there is any, get the corresponding key codes
   			if (aKeyStroke != null)
  			{
   				dataKeyCode = aKeyStroke.getKeyCode();
       			dataModCode = aKeyStroke.getModifiers();
   			}
  			
   			// if current keystroke and the one displayed in the table mismatch
   			if ((tableKeyCode != dataKeyCode) || (tableModCode != dataModCode))
   			{
  				// update the table
   				table.setValueAt(Integer.toString(dataKeyCode), i, 4);
				table.setValueAt(Integer.toString(dataModCode), i, 5);
   				String keyStrokeName = scu.getDescriptionForKeyStroke(aKeyStroke);
   				table.setValueAt(keyStrokeName, i, 2);
   			}    			
   		}
    }
    
    /**
     * creates a new window to edit the shortcut for the selected action 
     * @param selectedAction the action selected in the table for which the shortcut should be edited
     */
    private void createEditWindow(String selectedAction, List<Integer> codes)
    {
    	//disableButtons();
    	ShortcutEditPanel.createAndShowGUI(this, selectedAction, codes); // blocks
    	//enableButtons();
    } 
    
    /**
     * changes the shortcut for the currently selected action to the specified keyStroke
     * @param ks the new shortcut keyStroke
     */
    public void changeShortcut(KeyStroke ks, boolean applyInAllModes)
    {
    	JTable table = (JTable) ((JScrollPane) shortcutPane.getComponentAt(shortcutPane.getSelectedIndex())).getViewport().getView();     	
    	final String action = (String) table.getValueAt(table.getSelectedRow(), action_col);
    	
    	if(applyInAllModes){
    		Iterator<Entry<String, List<String>>> it;
    		for(int index=0; index <shortcutPane.getTabCount(); index++){
    			String tabName = shortcutPane.getTitleAt(index);    			   
    			it = scu.getShortcuttableActions(getConstant(tabName)).entrySet().iterator();
    			while(it.hasNext()){    			
    				Map.Entry<String,List<String>> pair = it.next();    		
    				List<String> actions = pair.getValue();
    				if(actions != null){
    					if (actions.contains(action)){
    						changeShortcut(action, ks, tabName);
    					}
    				}
    			}
    		}
    	} else{
    		changeShortcut(action, ks, shortcutPane.getTitleAt(shortcutPane.getSelectedIndex()));
    	}
    }
    
    private List<String> getActionsWithKeyStroke(String modeName, KeyStroke ks){
    	List<String> actionList = new ArrayList<String>();	
    	Iterator<Entry<String, KeyStroke>> it = shortcutKeyStrokesMap.get(modeName).entrySet().iterator();
    	Entry<String, KeyStroke> pair;
    	while(it.hasNext()){
    		pair = it.next();	
    		String action = pair.getKey();
    		if(ks.equals(pair.getValue())){
    			actionList.add(action);
    		}
    	}
    	
    	return actionList;
    }
    
    
    private boolean isKeystrokeUsed(String action, KeyStroke ks, String tabName){
    	boolean replaceDialog = false;
    	Map<String, List<String>> replaceShortcutsMap = new HashMap<String, List<String>>();
    	List<String> actionList;
    	
    	// if the current tab is 'general shortcuts' tab, the new shortcuts 
    	// should be checked if it is used in any one of the modes
		if(tabName.equals(ElanLocale.getString(ELANCommandFactory.COMMON_SHORTCUTS))){	
			
			for(int i=0; i <shortcutPane.getTabCount(); i++){
    			String modeName = getConstant(shortcutPane.getTitleAt(i));
    			// check if the shortcut is used in this mode
    			if(shortcutKeyStrokesMap.get(modeName).containsValue(ks)){
    				// get all the actions in this mode having this shortcut
    				actionList = getActionsWithKeyStroke(modeName, ks);    				
    				if(actionList.contains(action)){
    					actionList.remove(action);
    				}
    				
    				if(actionList.size() > 0){
        				// if a different action uses this shortcut
        				replaceShortcutsMap.put(modeName, actionList);
    				}
    			}
        	} 
    	} else {
    		// if the current tab is 'general shortcuts' tab, the new shortcuts 
        	// should be checked if it is used in any one of the modes
    		String modeName = getConstant(tabName);
			
    		// check if the shortcut is used in this mode
    		if(shortcutKeyStrokesMap.get(modeName).containsValue(ks)){
				// get all the actions in this mode having this shortcut
				actionList = getActionsWithKeyStroke(modeName, ks);    				
				if(actionList.contains(action)){
					actionList.remove(action);
				}
				
				if(actionList.size() > 0){
    				// if a different action uses this shortcut
    				replaceShortcutsMap.put(modeName, actionList);
				}
			}
    		
    		// check if the shortcut is used in this general mode
    		if(shortcutKeyStrokesMap.get(ELANCommandFactory.COMMON_SHORTCUTS).containsValue(ks)){
				// get all the actions in this mode having this shortcut
				actionList = getActionsWithKeyStroke(ELANCommandFactory.COMMON_SHORTCUTS, ks);    				
				if(actionList.contains(action)){
					actionList.remove(action);
				}
				
				if(actionList.size() > 0){
    				// if a different action uses this shortcut
    				replaceShortcutsMap.put(ELANCommandFactory.COMMON_SHORTCUTS, actionList);
				}
			}
    	}		
		
		if(replaceShortcutsMap.size() > 0){
			StringBuilder actionDesc = new StringBuilder();
			String eol = System.getProperty("line.separator"); 
			Iterator<Entry<String, List<String>>> it = replaceShortcutsMap.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, List<String>> pair = it.next();
				String modeName = pair.getKey();				
				actionList = pair.getValue();
				
				if(actionList != null){
					for(int i=0; i< actionList.size(); i++){
						actionDesc.append( actionList.get(i)+ " - " + ElanLocale.getString(modeName) + eol);
					}
				}	
			}
			// show the ShortcutReplace dialog
			ShortcutReplaceDialog srd = new ShortcutReplaceDialog(this, actionDesc.toString(), ks);
			srd.pack();
			srd.setLocationRelativeTo(javax.swing.SwingUtilities.getWindowAncestor(this));
			srd.setModal(true);
			srd.setVisible(true);
			
			// if the user chose replace, do this
			if (replaceShortcut)
			{  
				it = replaceShortcutsMap.entrySet().iterator();
				while(it.hasNext()){
					Entry<String, List<String>> pair = it.next();
					String modeName = pair.getKey();
					JTable table = (JTable) ((JScrollPane) shortcutPane.getComponentAt(shortcutPane.indexOfTab(ElanLocale.getString(modeName)))).getViewport().getView();  
					actionList = pair.getValue();
					
					if(actionList != null){
						for(int i=0; i< actionList.size(); i++){
							int rowIndex = getRowForAction(table, actionList.get(i));
							if(rowIndex < 0){
								continue;
							}						
							// update the table
							table.setValueAt("", rowIndex, desc_col);
							table.setValueAt("", rowIndex, keycode_col);
							table.setValueAt("", rowIndex, mod_col);	
							
							action = actionList.get(i);
							
							updateClashesFor(shortcutKeyStrokesMap.get(modeName).get(action) , modeName, action);
							shortcutKeyStrokesMap.get(modeName).put(action, null);
						}
					}
					table.repaint();
					
				}				
				return false;
			}    
			return true;
		}
    	return false;
    }
    
    private void updateClashesFor(KeyStroke ks, String modeName, String action){
    	if(keyStrokeClashMap.containsKey(ks)){    		
    		Map<String, List<String>> actionMap = keyStrokeClashMap.get(ks);        		
        	if(actionMap != null && actionMap.containsKey(modeName)){
        		List<String> actionList = actionMap.get(modeName);
        		if(actionList != null && actionList.contains(action)){
        			actionList.remove(action);
        			if(actionList.size() <= 0){
        				actionMap.remove(modeName);
        				clashModeMap.put(modeName, clashModeMap.get(modeName).intValue() -1);
        				keyStrokeModeMap.get(scu.getDescriptionForKeyStroke(ks)).remove(modeName);    
        			}
				}
        		
        		if(actionMap.size() == 0){
        			keyStrokeClashMap.remove(ks);        	
        			keyStrokeModeMap.remove(scu.getDescriptionForKeyStroke(ks));      
        			colorMap.remove(scu.getDescriptionForKeyStroke(ks));      
        		} else if(actionMap.size() == 1){
    				Iterator<Entry<String, List<String>>> it = actionMap.entrySet().iterator();
        			Entry<String, List<String>> pair;
    				while(it.hasNext()){
    					pair = it.next();
    					modeName = pair.getKey();
    					actionList = pair.getValue();
    					if(actionList == null || actionList.size() <= 1){
    						clashModeMap.put(modeName, clashModeMap.get(modeName).intValue() -1);
    						keyStrokeClashMap.remove(ks);
    						keyStrokeModeMap.remove(scu.getDescriptionForKeyStroke(ks));      
    	        			colorMap.remove(scu.getDescriptionForKeyStroke(ks));      
    					}
    				} 
        		} else if (actionMap.size() > 1 && modeName.equals(ELANCommandFactory.COMMON_SHORTCUTS)){
        			actionList = actionMap.get(modeName);
        			if(actionList == null || actionList.size() == 0){
        				Iterator<Entry<String, List<String>>> it = actionMap.entrySet().iterator();
            			Entry<String, List<String>> pair;
        				while(it.hasNext()){
        					pair = it.next();
        					modeName = pair.getKey();
        					actionList = pair.getValue();
        					if(actionList == null || actionList.size() <= 1){
        						clashModeMap.put(modeName, clashModeMap.get(modeName).intValue() -1);
        						keyStrokeModeMap.get(scu.getDescriptionForKeyStroke(ks)).remove(modeName);    
        						it.remove();
        					}
        				}
        				
        				if(actionMap.size() == 0){
        					keyStrokeClashMap.remove(ks);
        					keyStrokeModeMap.remove(scu.getDescriptionForKeyStroke(ks));      
                			colorMap.remove(scu.getDescriptionForKeyStroke(ks));     
        				}      
        			}
        		}
        	} 
        	
        	highLightTabWithClashes();
    	}
    }
    
    private void highLightTabWithClashes(){
    	for(int i = 0; i < shortcutPane.getTabCount(); i++){
    		String modeName = getConstant(shortcutPane.getTitleAt(i));
    		if(clashModeMap.containsKey(modeName)){
    			if(clashModeMap.get(modeName).intValue() > 0 ){
    				shortcutPane.setBackgroundAt(i, Color.RED);
    			} else {
    				shortcutPane.setBackgroundAt(i, shortcutPane.getBackground());
    			}
    		} else {
    			//shortcutPane.setBackgroundAt(i, shortcutPane.getBackground());
    		}
    	}
    }
    
    private void changeShortcut(String action, KeyStroke ks, String tabName)
    {   	
    	JTable table = (JTable) ((JScrollPane) shortcutPane.getComponentAt(shortcutPane.indexOfTab(tabName))).getViewport().getView();  
    	int row = getRowForAction(table, action);     	
    	if(row < 0){
    		return;
    	}
    	
    	//String actionDesc = scu.getDescriptionForAction(getActionNameForKeyStroke(allShortCuts, ks));
    	String keydesc = scu.getDescriptionForKeyStroke(ks);
    	String keycodestring = "";
    	String modstring = "";

    	if (ks != null)
    	{    		    		
    		
    		if(isKeystrokeUsed(action, ks, tabName)){
    			return;
    		}
    		
    		keycodestring = Integer.toString(ks.getKeyCode());
    		modstring = Integer.toString(ks.getModifiers());
    	} 
    	
    	table.setValueAt(keydesc, row, desc_col);
    	table.setValueAt(keycodestring, row, keycode_col);
    	table.setValueAt(modstring, row, mod_col);   
    	
    	table.repaint();
    	
    	updateClashesFor(shortcutKeyStrokesMap.get(getConstant(tabName)).get(action) , getConstant(tabName), action);    	
    	
    	shortcutKeyStrokesMap.get(getConstant(tabName)).put(action, ks);
    	        
    	saveChanges = true;
        
    	saveButton.setEnabled(saveChanges);
    }   

	/**
     * searches the table to find the row in which a certain action is displayed
     * @param oldAction the action ID of the action 
     * @return the index of the action's row or -1 if it cannot be found 
     */
    private int getRowForAction(JTable table, String oldAction) 
    {
		// get no of rows
    	int noOfRows = table.getRowCount();
    		
    	// iterate through table to find the correct row no
    	for (int i=0;i<noOfRows;i++)
    	{
    		String tableresult = (String)table.getValueAt(i, action_col);
    		if (tableresult.compareTo(oldAction) == 0)
    		{
    			// return the row no
    			return i;
    		}
    	}
    	
    	// return a negative value if the action couldn't be found in the table
		return -1;
	}

	/**
     * Create the GUI and show it. 
     */
   public static void createAndShowGUI(Window owner) {
        //Create and set up the window.
    	JDialog frame = null;
    	if (owner instanceof Dialog) {
    		frame = new JDialog((Dialog)owner,ElanLocale.getString("Shortcuts.Table.Title"),true);
    	} else if (owner instanceof Frame) {
    		frame = new JDialog((Frame)owner,ElanLocale.getString("Shortcuts.Table.Title"),true);
    	}
    	
    	if (frame == null) {
    		return;
    	}
        //Create and set up the content pane.
        final ShortcutPanel newContentPane = new ShortcutPanel();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(owner);
        frame.setVisible(true);
        
        newContentPane.checkForSave();
    }
   
   class ShortcutClashRenderer extends DefaultTableCellRenderer{

		public ShortcutClashRenderer(){
			super();
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if(!isSelected){
				String modeName = table.getName();
				String keyStrokeName = (String) table.getValueAt(row,2); 			
				if(keyStrokeModeMap.get(keyStrokeName) != null && keyStrokeModeMap.get(keyStrokeName).contains(modeName)){			
					cell.setBackground(colorMap.get(keyStrokeName));
				} else {
					cell.setBackground(table.getBackground());
				}
			}
			return cell;
		}
	}
}