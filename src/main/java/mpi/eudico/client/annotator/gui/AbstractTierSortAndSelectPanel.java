package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;

@SuppressWarnings("serial")
public abstract class AbstractTierSortAndSelectPanel extends JPanel implements ActionListener,
	ChangeListener, ItemListener, TableModelListener {
    public enum Modes {
    	ALL_TIERS, ROOT_TIERS, ALIGNABLE_TIERS, ROOT_W_INCLUDED 
    }
    /** A constant for unspecified participant or linguistic type */
    protected final String NOT_SPECIFIED = "not specified";
    /** these tab labels are final and used as id's so they do not change when the ui language changes */
    public static final String BY_TIER = "Tier";//ElanLocale.getString("ExportDialog.Tab.Tier") ;
    public static final String BY_TYPE = "Type";//ElanLocale.getString("ExportDialog.Tab.Type");
    public static final String BY_PART = "Participant";//ElanLocale.getString("ExportDialog.Tab.Participant");
    public static final String BY_ANN = "Annotators";//ElanLocale.getString("ExportDialog.Tab.Annotators") ;
    public static final String BY_LANG = "ContentLanguage";
    protected final int TIER_INDEX = 0;
    protected final int TYPE_INDEX = 1;
    protected final int PART_INDEX = 2;
    protected final int ANN_INDEX = 3;
    protected final int LANG_INDEX = 4;
	protected JPanel tierButtonPanel;
	protected JButton downButton;
	protected JButton upButton;
	protected JButton allButton;
	protected JButton noneButton;
	protected JButton sortButton;
	protected JButton unsortButton;
	protected JCheckBox rootTiersOnlyCB;
    /** table for tiers */
    protected TierExportTableModel model;
    /** panel for a tier table */
    protected JPanel tierSelectionPanel;
    
    protected final JTabbedPane selectTiersTabPane = new JTabbedPane();  

    /** table ui */
    protected JTable tierTable;
    /** column id for the include in export checkbox column, invisible */
    protected final String SELECT_COLUMN = "select";

    /** column id for the tier name column, invisible */
    protected final String TIER_NAME_COLUMN = "tier";
    
    protected List<String> returnedTiers = null;

    protected int currentTabIndex = 0;
    protected List<String> allTierNames;		   
    protected List<String> oldTierOrder;
	
    protected List<String> selectedTypeNames;
	protected List<String> selectedTierNames;
	protected List<String> selectedParts;
	protected List<String> selectedAnns;
	protected List<String> selectedLangs;
	
	//protected Map<String, Boolean> preRootOnlySelected;
	
	//private Transcription transcription;
	protected List<String> hiddenTiers;
	protected boolean allowReordering;
	protected boolean allowSorting;
	
	/** table for types */
    protected TierExportTableModel typeModel;
	/** table for participants */
    protected TierExportTableModel partModel;
	/** table for annotators */
    protected TierExportTableModel annotModel;
	/** table for languages */
    protected TierExportTableModel langModel;
    protected Modes mode = Modes.ALL_TIERS;
    protected Map<Integer, String> tabIndices;
    protected boolean pendingChanges = false;
    /** two maps for storing state while switching between "roots only" and "all" tiers */
    protected LinkedHashMap<String, Boolean> unfilteredTiers;
    protected LinkedHashMap<String, Boolean> unfilteredTypes;
    protected boolean initialSelectedTiersProvided = false;
	
	/**
	 * Initializes panes, buttons and tier table 
	 */
	protected void initComponents() {	
        selectedTypeNames = new ArrayList<String>();        
        selectedParts = new ArrayList<String>();
        selectedAnns = new ArrayList<String>(); 
        selectedLangs = new ArrayList<String>(); 
    	hiddenTiers = new ArrayList<String>();
    	
    	setLayout(new GridBagLayout());
		Insets insets = new Insets(4, 6, 4, 6);
		
		model = getNewModel();
		tierTable = new TierExportTable(model);
		
        typeModel = getNewModel();
        JTable typeTable = new TierExportTable(typeModel);
        
        partModel = getNewModel();
        JTable partTable = new TierExportTable(partModel);
        
        annotModel = getNewModel();
        JTable annotTable = new TierExportTable(annotModel);
        
		langModel = getNewModel();
        JTable langTable = new TierExportTable(langModel);
        
        initTables();
        
        Dimension tableDim = new Dimension(450, 160);
        JScrollPane tierScrollPane = new JScrollPane(tierTable);        
        selectTiersTabPane.setPreferredSize(tableDim);         
        selectTiersTabPane.addTab(ElanLocale.getString("ExportDialog.Tab.Tier"), tierScrollPane);
        
        selectTiersTabPane.addTab(ElanLocale.getString("ExportDialog.Tab.Type") ,new JScrollPane(typeTable));
        selectTiersTabPane.addTab(ElanLocale.getString("ExportDialog.Tab.Participant") , new JScrollPane(partTable));
        selectTiersTabPane.addTab(ElanLocale.getString("ExportDialog.Tab.Annotators"), new JScrollPane(annotTable));
        selectTiersTabPane.addTab(ElanLocale.getString("ExportDialog.Tab.Languages"), new JScrollPane(langTable));
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        //gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        //gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(selectTiersTabPane, gridBagConstraints);
        selectTiersTabPane.addChangeListener(this);
        
        // add button panel
        tierButtonPanel = new JPanel(new GridBagLayout());
        int gridx = 0;
        int gridy = 0;
        if (mode != Modes.ROOT_TIERS) {
        	// add the option to switch between root tiers and current-mode-tiers
        	rootTiersOnlyCB = new JCheckBox(ElanLocale.getString("ExportTradTranscript.Label.RootTiers"));
        	rootTiersOnlyCB.addItemListener(this);
	        gridBagConstraints = new GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = gridy++;
	        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
	        gridBagConstraints.anchor = GridBagConstraints.WEST;
	        gridBagConstraints.insets = new Insets(0, 6, 0, 6);
	        tierButtonPanel.add(rootTiersOnlyCB, gridBagConstraints);
        }
        
        if (allowReordering) {
	        upButton = new JButton();
	        downButton = new JButton();

	        try {
	            ImageIcon upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
	            ImageIcon downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));
	            upButton.setIcon(upIcon);
	            downButton.setIcon(downIcon);
	        } catch (Exception ex) {
	            upButton.setText("Up");
	            downButton.setText("Down");
	        }
	        upButton.addActionListener(this);
	        downButton.addActionListener(this);
	
			
	        gridBagConstraints = new GridBagConstraints();
	        gridBagConstraints.gridx = gridx++;
	        gridBagConstraints.gridy = gridy;
	        gridBagConstraints.anchor = GridBagConstraints.WEST;
	        gridBagConstraints.insets = insets;
	        tierButtonPanel.add(upButton, gridBagConstraints);
	
	        gridBagConstraints = new GridBagConstraints();
	        gridBagConstraints.gridx = gridx++;
	        gridBagConstraints.gridy = gridy;
	        gridBagConstraints.anchor = GridBagConstraints.WEST;
	        gridBagConstraints.insets = new Insets(4, 0, 4, 6);
	        tierButtonPanel.add(downButton, gridBagConstraints);
        }
        
        if (allowSorting) {
        	sortButton = new JButton(ElanLocale.getString("MultiTierControlPanel.Menu.Button.Sort"));
	        gridBagConstraints = new GridBagConstraints();
	        gridBagConstraints.gridx = gridx++;
	        gridBagConstraints.gridy = gridy;
	        gridBagConstraints.anchor = GridBagConstraints.WEST;
	        gridBagConstraints.insets = insets;
	        tierButtonPanel.add(sortButton, gridBagConstraints);
	               	
        	unsortButton = new JButton(ElanLocale.getString("MultiTierControlPanel.Menu.Button.Default"));
        	unsortButton.setEnabled(false);
	        gridBagConstraints = new GridBagConstraints();
	        gridBagConstraints.gridx = gridx++;
	        gridBagConstraints.gridy = gridy;
	        gridBagConstraints.anchor = GridBagConstraints.WEST;
	        gridBagConstraints.insets = new Insets(4, 0, 4, 6);
	        tierButtonPanel.add(unsortButton, gridBagConstraints);
	        
        	sortButton.addActionListener(this);
        	unsortButton.addActionListener(this);
        }
        
        allButton = new JButton(ElanLocale.getString("Button.SelectAll"));
        noneButton = new JButton(ElanLocale.getString("Button.SelectNone"));
        allButton.addActionListener(this);
        noneButton.addActionListener(this);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = gridx++;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(4, 20, 4, 2);
        tierButtonPanel.add(allButton, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = gridx++;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;
        tierButtonPanel.add(noneButton, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(tierButtonPanel, gridBagConstraints);
        
        model.addTableModelListener(this);
        typeModel.addTableModelListener(this);
        partModel.addTableModelListener(this);
        annotModel.addTableModelListener(this);
        langModel.addTableModelListener(this);
	}
	
	/**
	 * Set up a table model with the proper columns identifiers.
	 * The columns are called  SELECT_COLUMN and TIER_NAME_COLUMN even for
	 * tables that contain other field values.
	 */
	
	TierExportTableModel getNewModel() {
		TierExportTableModel model = new TierExportTableModel();
        model.setColumnIdentifiers(new String[] { SELECT_COLUMN, TIER_NAME_COLUMN });
        return model;
	}
	
	/**
	 * Populates the tables with data from the transcription
	 */
	protected abstract void initTables();
	
    /**
     * Returns the selected tiers.
     *
     * @return the selected tiers
     */
    public List<String> getSelectedTiers() {
    	if (pendingChanges) {
    		updateChanges(currentTabIndex);
    	}
        returnedTiers = this.selectedTierNames;
        return returnedTiers;
    }
    
    /**
     * Sets the selected tiers, the other tiers will be unselected.
     * @param selectedTiers
     */
    public void setSelectedTiers(List<String> selectedTiers) {
    	if (selectedTiers == null) {
    		return;
    	}
    	
    	model.removeTableModelListener(this);
    	
	   	int includeCol = model.findColumn(SELECT_COLUMN);
	    int nameCol = model.findColumn(TIER_NAME_COLUMN);
	     
		for (int i = 0; i < model.getRowCount(); i++) {
			Object value = model.getValueAt(i, nameCol);
			if(selectedTiers.contains(value.toString())){
				model.setValueAt(Boolean.TRUE, i, includeCol);
			} else {
				model.setValueAt(Boolean.FALSE, i, includeCol);
			}
		}
		model.addTableModelListener(this);
    }
    
    /**
     * Returns all tiers in the current order.
     * 
     * @return the list of all tiers
     */
    public List<String> getTierOrder() {
    	List<String> orderedTiers = new ArrayList<String>();
    	
    	if (rootTiersOnlyCB == null || !rootTiersOnlyCB.isSelected() || unfilteredTiers == null) {
	    	int nameCol = model.findColumn(TIER_NAME_COLUMN);	    	
	    	
	        for (int i = 0; i < model.getRowCount(); i++) {
	        	orderedTiers.add((String) model.getValueAt(i, nameCol));
	        }
    	} else {
    		// retrieve the order from the stored list
    		orderedTiers.addAll(unfilteredTiers.keySet());
    	}
        
    	return orderedTiers;
    }
    
    /**
     * Sets the order for the tiers in the tier table. 
     * This does not set the list of available tiers. They are extracted from the transcription.
     * 
     * @param tierOrder the new tier order
     */
    public void setTierOrder(List<String> tierOrder) {
    	if(tierOrder == null){
    		return;
    	}
    	
    	model.removeTableModelListener(this);
    	
	   	int includeCol = model.findColumn(SELECT_COLUMN);
	    int nameCol = model.findColumn(TIER_NAME_COLUMN);
	    
	    Map<String, Boolean> currentSelected = new HashMap<String, Boolean>(model.getRowCount());
	    List<String> curOrder = new ArrayList<String> (model.getRowCount());
	    String name;
	    Boolean sel;

		for (int i = 0; i < model.getRowCount(); i++) {
			name = (String) model.getValueAt(i, nameCol);
			sel = (Boolean) model.getValueAt(i, includeCol);
			curOrder.add(name);
			currentSelected.put(name, sel);
		}
			
//		int lastRowChanged = -1;
//		for (int i = 0; i < tierOrder.size() && i < model.getRowCount(); i++) {
//			name = tierOrder.get(i);
//			if (allTierNames.contains(name)) {
//				model.setValueAt(name, i, nameCol);
//				if (curOrder.contains(name)) {
//					model.setValueAt(currentSelected.get(name), i, includeCol);
//				} else {
//					model.setValueAt(Boolean.FALSE, i, includeCol);
//				}
//				lastRowChanged = i;
//			}
//		}
//		// add tiers that were in the list before but not in the new ordered list
//		for(String s : curOrder) {
//			if (!tierOrder.contains(s)) {
//				lastRowChanged++;
//				if (lastRowChanged < model.getRowCount()) {
//					model.setValueAt(s, lastRowChanged, nameCol);
//					model.setValueAt(currentSelected.get(s), lastRowChanged, includeCol);
//				}
//			}
//		}
		
		while(model.getRowCount() > 0){
			model.removeRow(model.getRowCount()-1);
		}
		
		//first validate the tierOrder list for duplicate values
		  List<String> newTierOrder = new ArrayList<String> (model.getRowCount());
		  for(String s: tierOrder){
			  if(!newTierOrder.contains(s)){
				  newTierOrder.add(s);
			  }
		  }
	
		// add tiers from the  new ordered list
		for (int i = 0; i < newTierOrder.size(); i++) {
			name = newTierOrder.get(i);
			if (allTierNames.contains(name)) {			
				if (curOrder.contains(name)) {
					model.addRow(currentSelected.get(name), name);
				} else {
					model.addRow(Boolean.FALSE, name);
				}
			}
		}
		
		// add tiers that were in the list before but not in the new ordered list
		for(String s : curOrder) {
			if (!newTierOrder.contains(s)) {
				model.addRow(currentSelected.get(s), s);				
			}
		}
		
		pendingChanges = true;
		
		model.addTableModelListener(this);
    }
    
    /**
     * Returns the list of hidden tiers of a 
     * certain group(type /participants/ annotators)
     * @version Dec 2011 only store tiers that have explicitly been unselected in the tiers tab
     * @return a list of tiers that have been unselected in the tier tab.
     */
    public List<String> getHiddenTiers() {
    	if (pendingChanges) {
    		updateChanges(currentTabIndex);
    	}
    	return hiddenTiers;
    	
    }
    
    /** 
     *Sets and removes the hidden tiers from selection. 
     *Does not set the changed flag.
     * 
     * @param hiddenTiers the list of hidden tier names
     */
    public void setHiddenTiers(List<String> hiddenTiers){  
    	if(hiddenTiers == null){
    		return;
    	}
    	
    	this.hiddenTiers = hiddenTiers;
    	model.removeTableModelListener(this);
    	
	   	int includeCol = model.findColumn(SELECT_COLUMN);
	    int nameCol = model.findColumn(TIER_NAME_COLUMN);
	     
		for (int i = 0; i < model.getRowCount(); i++) {
			Object value = model.getValueAt(i, nameCol);
			if(hiddenTiers.contains(value.toString())){
				model.setValueAt(Boolean.FALSE, i, includeCol);
			} else {
				model.setValueAt(Boolean.TRUE, i, includeCol);
			}
		}
		model.addTableModelListener(this);
    	
    }

    /**
     * Sets the selection mode and the items that are hidden in that mode
     * 
     * @param setSelectType
     */
    public void setSelectionMode(String selectionMode, List<String> hiddenItems){     
    	if(selectionMode == null){
    		return;
    	}
    	
    	Iterator<Integer> it = tabIndices.keySet().iterator();
    	Integer curInt;
    	while (it.hasNext()) {
    		curInt = it.next();
    		if (tabIndices.get(curInt).equals(selectionMode)) {
    			currentTabIndex = curInt;
    			break;
    		}
    	}
    	 	
    	selectTiersTabPane.setSelectedIndex(currentTabIndex);
    	// update the items
    	if (hiddenItems != null) {
    		setUnselectedItems(hiddenItems);
    	}
    }
    
    /**
     * Returns the currently used selection mode
     * 
     * (i.e whether the selection of tiers is based on
     * types / participant/ tier names/ annotators)
     * 
     * @return
     */
    public String getSelectionMode(){
    	int index = selectTiersTabPane.getSelectedIndex();// use currentTabIndex?
    	return tabIndices.get(index);
    }
    
    /**
     * Returns a list of the currently selected items, regardless of selected tab.
     * @return a list of selected items
     */
    public List<String> getSelectedItems() {
    	int tab = selectTiersTabPane.getSelectedIndex();
		String selectionMode = tabIndices.get(tab);
		
		DefaultTableModel iterModel = getModel(selectionMode);
	   	int includeCol = iterModel.findColumn(SELECT_COLUMN);
	    int nameCol = iterModel.findColumn(TIER_NAME_COLUMN);

	    List<String> selected = new ArrayList<String>();
		Object value;
		for (int i = 0; i < iterModel.getRowCount(); i++) {
			value = iterModel.getValueAt(i, includeCol);
			if ((Boolean) value) {
				selected.add((String) iterModel.getValueAt(i, nameCol));
			}
		}

    	return selected;
    }
    
    /**
     * Returns a list of the currently unselected items in the current tab.
     * @return a list of unselected items
     */
    public List<String> getUnselectedItems() {
    	int tab = selectTiersTabPane.getSelectedIndex();
		String selectionMode = tabIndices.get(tab);
		
		DefaultTableModel iterModel = getModel(selectionMode);
	   	int includeCol = iterModel.findColumn(SELECT_COLUMN);
	    int nameCol = iterModel.findColumn(TIER_NAME_COLUMN);

	    List<String> unselected = new ArrayList<String>();
		Object value;
		for (int i = 0; i < iterModel.getRowCount(); i++) {
			value = iterModel.getValueAt(i, includeCol);
			if (! (Boolean) value) {
				unselected.add((String) iterModel.getValueAt(i, nameCol));
			}
		}

    	return unselected;
    }
    
    /**
     * After the selected tab has been set, this method can be used to set the selected items.
     * 
     * @param items
     */
    public void setSelectedItems(List<String> items) {
    	if (items == null) {
    		return;
    	}
    	int tab = selectTiersTabPane.getSelectedIndex();
		String selectionMode = tabIndices.get(tab);
		
		DefaultTableModel iterModel = getModel(selectionMode);
	   	int includeCol = iterModel.findColumn(SELECT_COLUMN);
	    int nameCol = iterModel.findColumn(TIER_NAME_COLUMN);
	    iterModel.removeTableModelListener(this);
	    
		Object value;
		for (int i = 0; i < iterModel.getRowCount(); i++) {
			value = iterModel.getValueAt(i, nameCol);
			iterModel.setValueAt(items.contains(value), i, includeCol);
		}
		
		iterModel.addTableModelListener(this);
    	// update tier list
		pendingChanges = true;
    }
    
    /**
     * After the selected tab has been set, this method can be used to set the unselected items.
     * 
     * @param items the items to deselect
     */
    public void setUnselectedItems(List<String> items) {
    	if (items == null) {
    		return;
    	}
    	int tab = selectTiersTabPane.getSelectedIndex();
		String selectionMode = tabIndices.get(tab);
		
		DefaultTableModel iterModel = getModel(selectionMode);
	   	int includeCol = iterModel.findColumn(SELECT_COLUMN);
	    int nameCol = iterModel.findColumn(TIER_NAME_COLUMN);
	    iterModel.removeTableModelListener(this);
	    
		final int rowCount = iterModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			Object value = iterModel.getValueAt(i, nameCol);
			iterModel.setValueAt(!items.contains(value), i, includeCol);
		}
		
		iterModel.addTableModelListener(this);
    	// update tier list
		pendingChanges = true;
    }
    
    /**
     * Sets the selected state of the root tiers only checkbox. 
     * Only applicable in Modes other than ROOT_TIERS.
     * 
     * @param rootsOnly the flag
     */
    public void setRootTiersOnly(boolean rootsOnly) {
    	if (mode == Modes.ROOT_TIERS) {
    		return;
    	}
    	rootTiersOnlyCB.setSelected(rootsOnly);// does this trigger an event?
    	//toggleRootsOnly(rootsOnly);
    }
    
    /**
     * Returns whether the root tiers filtering is selected. 
     * Only applicable in Modes other than ROOT_TIERS.
     * 
     * @return true if filtering of root tiers only is selected.
     */
    public boolean isRootTiersOnly() {
    	if (mode == Modes.ROOT_TIERS) {
    		return true;
    	}
    	return rootTiersOnlyCB.isSelected();
    }
    
    /**
     * If this panel is in a dialog this method can be called in order to ensure 
     * that the list of tiers is updated according to the current selection
     * in the current tab.
     */
    public void applyChanges() {
    	int tab = selectTiersTabPane.getSelectedIndex();
    	//updateTabAtIndex(tab);
    	updateChanges(tab);
    }
	
    /**
     * The action event handling.
     */
	@Override
	public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == allButton) {
        	selectAllOrNone(Boolean.TRUE);
        } else if (ae.getSource() == noneButton) {
        	selectAllOrNone(Boolean.FALSE);
        } else if (ae.getSource() == upButton) {
            moveUp();
        } else if (ae.getSource() == downButton) {
            moveDown();
        } else if (ae.getSource() == sortButton) {
            sortAZ();
        } else if (ae.getSource() == unsortButton) {
        	undoSort();
        }
	}
	
	/**
	 * Event after a tab has been selected in the tabpane.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		// tab pane
		if (pendingChanges) {
			updateChanges(currentTabIndex);
		}
		currentTabIndex = selectTiersTabPane.getSelectedIndex();
		updateTabAtIndex(currentTabIndex);
		updateButtons(currentTabIndex);
		
	}
	
	/**
	 * Responds to change in selection of the root tiers only checkbox.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == rootTiersOnlyCB) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				toggleRootsOnly(false);
			} else {
				toggleRootsOnly(true);
			}
		}
		
	}

	
	/**
	 * Adds or removes a tier to the list of hidden tiers after a tier model event.
	 * The list selection doesn't fire an event if the checkbox in the first column is clicked.
	 * 
	 * The listener should only be added to the model when the tier tab is active, after
	 * changes are made as a result of selections in the other tabs. (????)
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		pendingChanges = true;
	}
	
	/**
	 * Switches dynamically between a view where only root tier are shown
	 * and the list of tiers for the mode of the panel.
	 *  
	 * @param rootsOnly
	 */
	protected void toggleRootsOnly(boolean rootsOnly) {
		
	}

	
	private void updateButtons(int selIndex) {
		String tabTitle = tabIndices.get(selIndex);
		// select all, select none, sort AZ always enabled?
		boolean up = false;
		boolean down = false;
		boolean unsort = false;
		
		if (BY_TIER.equals(tabTitle)) {
			boolean enable = model.getRowCount() > 1;
			up = enable;
			down = enable;
			unsort = (oldTierOrder != null);// a sort has been done
		} /*else if (BY_TYPE.equals(tabTitle)) {
			
		}*/
		
		if (upButton != null) {
			upButton.setEnabled(up);
		}
		if (downButton != null) {
			downButton.setEnabled(down);
		}
		if (unsortButton != null) {
			unsortButton.setEnabled(unsort);
		}
	}
	
	protected DefaultTableModel getModel(String selectionMode) {
		if (BY_ANN.equals(selectionMode)) {
			return annotModel;
		} else if (BY_PART.equals(selectionMode)) {
			return partModel;
		} else if (BY_TYPE.equals(selectionMode)) {
			return typeModel;
		} else if (BY_LANG.equals(selectionMode)) {
			return langModel;
		} else {
			return model;
		}
	}
	
	/**
	 * Selects or deselects all values in the table in the active tab 
	 * @param select 
	 */
	protected void selectAllOrNone(Boolean select) {
		String selectionMode = tabIndices.get(currentTabIndex);
		DefaultTableModel iterModel = getModel(selectionMode);
		
		int includeCol = iterModel.findColumn(SELECT_COLUMN);
		
		for (int i = 0; i < iterModel.getRowCount(); i++) {
			iterModel.setValueAt(select, i, includeCol);
		}
		// if the tier tab is active, reset the hidden tiers list
//		if (iterModel == model) {
//			if (select) {// all tiers selected
//				hiddenTiers.clear();
//			} else {
//				hiddenTiers.clear();
//				hiddenTiers.addAll(allTierNames);
//			}
//		}
	}
	
    /**
     * Moves selected tiers up in the list of tiers.
     */
    protected void moveDown() {
        if ((tierTable == null) || (model == null) ||
                (model.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int i = selected.length - 1; i >= 0; i--) {
            int row = selected[i];

            if ((row < (model.getRowCount() - 1)) &&
                    !tierTable.isRowSelected(row + 1)) {
                model.moveRow(row, row, row + 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row + 1, 0, true, false);
            }
        }
    }

    /**
     * Moves selected tiers up in the list of tiers.
     */
    protected void moveUp() {
        if ((tierTable == null) || (model == null) ||
                (model.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int element : selected) {
            int row = element;

            if ((row > 0) && !tierTable.isRowSelected(row - 1)) {
                model.moveRow(row, row, row - 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row - 1, 0, true, false);
            }
        }
    }
    
    /**
     * Sorts the current table model alphabetically.
     */
    private void sortAZ() {
		String selectionMode = tabIndices.get(currentTabIndex);
		DefaultTableModel iterModel = getModel(selectionMode);
		
		if (iterModel.getRowCount() < 2) {
			return;
		}
		// store for undo if needed
		if (iterModel == model) {
			oldTierOrder = new ArrayList<String>(allTierNames.size());
		} else {
			iterModel.removeTableModelListener(this);
		}
				
		Map<String, Boolean> curSelected = new HashMap<String, Boolean>(iterModel.getRowCount());
		List<String> values = new ArrayList<String>(iterModel.getRowCount());
		
	   	int includeCol = iterModel.findColumn(SELECT_COLUMN);
	    int nameCol = iterModel.findColumn(TIER_NAME_COLUMN);
	    String value;
		for (int i = 0; i < iterModel.getRowCount(); i++) {
			value = (String) iterModel.getValueAt(i, nameCol);
			values.add(value);
			curSelected.put(value, (Boolean)iterModel.getValueAt(i, includeCol));
			if (iterModel == model) {
				oldTierOrder.add(value);
			}
		}
		Collections.sort(values);
		for (int i = 0; i < iterModel.getRowCount() && i < values.size(); i++) {
			value = values.get(i);
			iterModel.setValueAt(value, i, nameCol);
			iterModel.setValueAt(curSelected.get(value), i, includeCol);
		}
		if (iterModel == model) {
			unsortButton.setEnabled(true);
		}
		if (iterModel != model) {
			iterModel.addTableModelListener(this);
		}
    }
    
    /**
     * Undoing the last A-Z sort is only supported for the tiers table
     */
    private void undoSort() {
    	if (oldTierOrder != null) {
    		Map<String, Boolean> curSelected = new HashMap<String, Boolean>(model.getRowCount());
    		String value;
    	   	int includeCol = model.findColumn(SELECT_COLUMN);
    	    int nameCol = model.findColumn(TIER_NAME_COLUMN);
    		
    		for (int i = 0; i < model.getRowCount(); i++) {
    			value = (String) model.getValueAt(i, nameCol);
    			curSelected.put(value, (Boolean)model.getValueAt(i, includeCol));
    		}
    		
    		for (int i = 0; i < model.getRowCount() && i < oldTierOrder.size(); i++) {
    			value = oldTierOrder.get(i);
    			model.setValueAt(value, i, nameCol);
    			model.setValueAt(curSelected.get(value), i, includeCol);
    		}
    		unsortButton.setEnabled(false);
    	}
    }
    
	 /**
     * 
     */
    protected void showTiersTab(){     	
    	int includeCol = model.findColumn(SELECT_COLUMN);
        int nameCol = model.findColumn(TIER_NAME_COLUMN);
        model.removeTableModelListener(this);
        
    	for (int i = 0; i < model.getRowCount(); i++) {
    		Object value = model.getValueAt(i, nameCol);
    		if(selectedTierNames.contains(value.toString())){
    			model.setValueAt(Boolean.TRUE, i, includeCol);
    		} else {
    			model.setValueAt(Boolean.FALSE, i, includeCol);
    		}
    	}

    	model.addTableModelListener(this);
    }
    
    /**
     * Update the tier tab changes
     */
    protected void updateTiers(){
    	if (pendingChanges) {
    		int includeCol = model.findColumn(SELECT_COLUMN);
            int nameCol = model.findColumn(TIER_NAME_COLUMN);
            selectedTierNames.clear();
            hiddenTiers.clear();
            
        	String tierName; 
            for (int i = 0; i < model.getRowCount(); i++) {
            	Boolean include = (Boolean) model.getValueAt(i, includeCol);
            	tierName =  (String) model.getValueAt(i, nameCol) ;
            	if (include) {
            		selectedTierNames.add(tierName);
            	} else {
            		hiddenTiers.add(tierName);
            	}
            }
    	}

    }
    
    /**
     * Update the types tab changes
     */
    protected abstract void updateLinguisticTypes();
    
    /**
     * Update the participants tab changes
     */
    protected abstract void updateParticipants();
    
    /**
     * Update the annotator tab changes
     */
    protected abstract void updateAnnotators();   
    
    /**
     * Update the language tab changes
     */
    protected abstract void updateLanguages();   
   
    /**
     * Updates the list of tiers based on the changes in the current tab
     * 
     * @param index the index of the current active tab
     */
    protected void updateChanges(int index) {
    	switch (index) {
    	case TIER_INDEX:
    		updateTiers();
    		break;
    	case TYPE_INDEX:
    		updateLinguisticTypes();
    		break;
    	case PART_INDEX:
    		updateParticipants();
    		break;
    	case ANN_INDEX:
    		updateAnnotators();
    		break;
    	case LANG_INDEX:
    		updateLanguages();
    		break;
    	default:
    	}
    	
    	pendingChanges = false;
    }
    
    /**
     * Updates the table at the specified index; based on the current tier selection 
     * the types, participant's or annotator's tab is updated. 
     * 
     * @param index
     */
    protected void updateTabAtIndex(int index) {
    	if (index==TIER_INDEX) {
    		showTiersTab();
    	}
    }
}
