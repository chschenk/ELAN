package mpi.eudico.client.annotator.tiersets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

@SuppressWarnings("serial")
public class ManageTierSetDlg extends ClosableDialog implements ActionListener, ListSelectionListener{	
	
	// tier set button panel
	private JPanel tierSetButtonPanel;	
	private JButton addButton, deleteButton, sortButton, upButton, downButton;
	
	//tier set panel
	private JPanel tierSetPanel;	
	private JTable tierSetTable;
	private DefaultTableModel tierSetModel;	
	
	//tier set attributes panel
	private JPanel tierSetAttriPanel;
	private JTable tierTable;
	private DefaultTableModel tierModel;
	
	private JLabel missingTiersLabel;
	private JButton editButton;
	private JTextArea nameTextArea, descTextArea;
	
	//main panel
	private JButton okButton, cancelButton;
	
	private AbstractTierSortAndSelectPanel tierSelectPanel;
	
	
    /** column id  for tier/ tier set visibility */
    protected final String VISIBLE_COLUMN = ElanLocale.getString(
    		"SignalViewer.Segmentation.Visible");

    /** column id for the name column */
    protected final String NAME_COLUMN = ElanLocale.getString(
    		"AboutDialog.Name");
    
    /** column index  for tier/ tier set visibility */
    protected final int VISIBLE_COL_INDEX = 0;

    /** column index for the name column */
    protected final int NAME_COL_INDEX = 1;
	
	private TierSetUtil tierSetUtil;
	private TranscriptionImpl transcription;
	private List<File> openFilesList;
	
	
	private List<String> missingTiersList;
	
	
	private HashMap<String, HashMap<String, Boolean>> tierSetMap;	
	private Insets globalInsets = new Insets(2,4,2,4);
	
	public ManageTierSetDlg(ElanFrame2 frame){
		super(frame, ElanLocale.getString("TierSet.Title.ManageTierSet"),true);
		
		if(frame.getViewerManager() != null){
			this.transcription = (TranscriptionImpl) frame.getViewerManager().getTranscription();
		}		
		
		tierSetUtil = TierSetUtil.getTierSetUtilInstance();		
		
		missingTiersList = new ArrayList<String>();
		tierSetMap = new HashMap<String, HashMap<String, Boolean>>();
		
		initComponents();
		WindowLocationAndSizeManager.postInit(this, "ManageTierSetDlg");
	}
	
	
	
	/**
	 * Initializes tier set panel
	 */
	private void initTierSetPanel(){
		initTierSetButtonPanel();
		
		tierSetModel = new TierExportTableModel();
        tierSetTable = new JTable(tierSetModel);
        tierSetTable.setDragEnabled(true);
        tierSetTable.setDropMode(DropMode.USE_SELECTION);
        tierSetTable.setTransferHandler(tableDragAndDropHandler());
		
		initTableAndModel(tierSetTable, tierSetModel);
		
		tierSetPanel = new JPanel();
		tierSetPanel.setLayout(new GridBagLayout());
		tierSetPanel.setBorder(new TitledBorder(ElanLocale.getString("TierSet.TierSetList")));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = globalInsets;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
        gbc.weighty = 1.0;
		tierSetPanel.add(new JScrollPane(tierSetTable), gbc);
		
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
		tierSetPanel.add(tierSetButtonPanel, gbc);
	}
	
	/**
	 * Returns a transfer handler for drag and drop within a table
	 */
	protected static final TransferHandler tableDragAndDropHandler() {
		return new TransferHandler() {
        	// Dragging and dropping rows in the tierSetTable is
        	// done by transferring a list of indexes of selected rows
        	
        	// Custom dataflavor for transferring an int array
        	DataFlavor intArrayDataFlavor = new DataFlavor(int[].class, "Integer Array");
        	        	
        	// The class for transferring an int array
        	class TransferableIntArray implements Transferable {
        		
        		// These are the supported data flavors
	    		protected DataFlavor[] supportedFlavors = {
	    			intArrayDataFlavor   // Transfer as a int array
	    		};
        		  
	    		int[] intArray;
	    		
	    		public TransferableIntArray(int[] intArray) {this.intArray = intArray;}
	    		
				@Override
				public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
					if(flavor.equals(intArrayDataFlavor)) {
						return intArray;
					} else {
						throw(new UnsupportedFlavorException(flavor));
					}
				}

				@Override
				/** Return a list of DataFlavors we can support */
				public DataFlavor[] getTransferDataFlavors() { return supportedFlavors; }

				@Override
				public boolean isDataFlavorSupported(DataFlavor flavor) {
					if(flavor.equals(intArrayDataFlavor)) {
						return true;
					}
					return false;
				}
        		
        	}
        	
        	@Override
			public int getSourceActions(JComponent c) {
                return DnDConstants.ACTION_COPY_OR_MOVE;
            }
        	
            @Override
			public Transferable createTransferable(JComponent comp)
            {
                JTable table=(JTable)comp;
                int[] rows=table.getSelectedRows();
                
                TransferableIntArray transferable = new TransferableIntArray(rows);
                return transferable;
            }
            @Override
			public boolean canImport(TransferHandler.TransferSupport info){
                if (!info.isDataFlavorSupported(intArrayDataFlavor)){
                    return false;
                }
  
                return true;
            }
  
            @Override
			public boolean importData(TransferSupport support) {
  
                if (!support.isDrop()) {
                    return false;
                }
  
                if (!canImport(support)) {
                    return false;
                }
  
                JTable table=(JTable)support.getComponent();
                DefaultTableModel tableModel=(DefaultTableModel)table.getModel();
                 
               JTable.DropLocation dl = (JTable.DropLocation)support.getDropLocation();
  
                int dropRow = dl.getRow();
  
                int[] rows;
                try {
                    rows = (int[])support.getTransferable().getTransferData(intArrayDataFlavor);
                } catch (UnsupportedFlavorException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
  
                // Put the data of all selected rows in a temporary list of some sort
                List<Object[]> tierSetList = new ArrayList<Object[]>();
                int selectedRowsAboveDropRow = 0;
                for(int i = rows.length-1; i >= 0; i--) {
                	if(rows[i] < dropRow) {
                		selectedRowsAboveDropRow++;
                	}
                	Boolean isVisible = (Boolean) table.getModel().getValueAt(rows[i], 0);
                	String tierName = (String) table.getModel().getValueAt(rows[i], 1);
                	System.out.println("TIERNAME: " + tierName);
                	tierSetList.add(new Object[]{isVisible,tierName});
                	// Remove the selected rows from the table
                	tableModel.removeRow(rows[i]);
                }
                               
                // Put the data from the temporary list back in the table
                int insertRow = dropRow - selectedRowsAboveDropRow;
                for(int i = 0; i < tierSetList.size(); i++) {
                	tableModel.insertRow(insertRow, tierSetList.get(i));
                }
                
                // Set the row selection 
                table.setRowSelectionInterval(insertRow, (insertRow + rows.length - 1));
  
                return true;
            }
        };
	}
	
	/**
	 * Initialize tier set button panel components
	 */
	private void initTierSetButtonPanel(){
		addButton = new JButton();
		addButton.setToolTipText(ElanLocale.getString("Button.Add"));
		addButton.addActionListener(this);

		deleteButton = new JButton();
		deleteButton.setToolTipText(ElanLocale.getString("Button.Delete"));
		deleteButton.addActionListener(this);
		
		sortButton = new JButton(ElanLocale.getString("MultiTierControlPanel.Menu.Button.Sort"));
		sortButton.addActionListener(this);
		
		upButton = new JButton();
		upButton.setToolTipText(ElanLocale.getString("EditCVDialog.Button.Up"));
		upButton.addActionListener(this);
		 
		downButton = new JButton();
		downButton.setToolTipText(ElanLocale.getString("EditCVDialog.Button.Down"));
		downButton.addActionListener(this);
		
		try {
			ImageIcon upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
			ImageIcon downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));
			upButton.setIcon(upIcon);
			downButton.setIcon(downIcon);
		} catch (Exception ex) {
			upButton.setText("Up");
			downButton.setText("Down");
		}
		
		try {
			ImageIcon addIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Plus16.gif"));
			ImageIcon removeIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Minus16.gif"));
			addButton.setIcon(addIcon);
			deleteButton.setIcon(removeIcon);
		} catch (Exception ex) {
			addButton.setText("+");
			deleteButton.setText("-");
		}
		
		tierSetButtonPanel = new JPanel();
		tierSetButtonPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = globalInsets;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;		
		tierSetButtonPanel.add(upButton, gbc);
		
		gbc.gridx = 1;
		tierSetButtonPanel.add(downButton);
	
		gbc.gridx = 2;
		gbc.weightx = 1.0;
		tierSetButtonPanel.add(sortButton);
		
		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.EAST;		
		tierSetButtonPanel.add(addButton);
		
		gbc.gridx = 4;
		tierSetButtonPanel.add(deleteButton);
	}
	
	/**
	 * Initializes tier set attributes panel components
	 */
	private void initTierSetAttributePanel(){
		tierModel = new TierExportTableModel();
		tierTable = new JTable(tierModel);
		tierTable.setRowSelectionAllowed(false);
		
		if(transcription != null){
			tierTable.setDefaultRenderer(Object.class, new MyTableCellRender());
        }
        
			
		initTableAndModel(tierTable, tierModel);
		
		editButton = new JButton(ElanLocale.getString("Menu.Edit"));
		editButton.setEnabled(false);
		editButton.addActionListener(this);	
		    
		nameTextArea = new JTextArea(1,1);
		nameTextArea.setEditable(false);
			
		descTextArea = new JTextArea(5,1);
		descTextArea.setWrapStyleWord(true);
		descTextArea.setEditable(false);
		
		missingTiersLabel= new JLabel();
		missingTiersLabel.setFont(new Font(missingTiersLabel.getFont().getFontName(), Font.PLAIN, 10));
		missingTiersLabel.setForeground(Color.RED);
		    
		tierSetAttriPanel = new JPanel();
		tierSetAttriPanel.setLayout(new GridBagLayout());
		tierSetAttriPanel.setBorder(new TitledBorder(ElanLocale.getString("TierSet.Attributes")));
					
		GridBagConstraints	gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(4,2,4,2);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		tierSetAttriPanel.add(new JLabel(ElanLocale.getString("TierSet.Name")), gbc);
			
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		tierSetAttriPanel.add(nameTextArea, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		tierSetAttriPanel.add(new JLabel(ElanLocale.getString("TierSet.Description")), gbc);
			
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		tierSetAttriPanel.add(descTextArea, gbc);
			
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		tierSetAttriPanel.add(new JLabel(ElanLocale.getString("TierSet.TierList")), gbc);
			
		gbc.gridy = 3;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		tierSetAttriPanel.add(new JScrollPane(tierTable), gbc);
		
		gbc.gridy = 4;
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		tierSetAttriPanel.add(missingTiersLabel, gbc);	
			
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.CENTER;
		tierSetAttriPanel.add(editButton, gbc);		
	}
	
	/**
	 * Initialize components and make layout
	 */
	private void initComponents(){
		getContentPane().setLayout(new GridBagLayout());		
		
		//tier set panel
		initTierSetPanel();
		
		// tier set desc panel
		initTierSetAttributePanel();	
		
		//main panel components
		JLabel editTierLabel = new JLabel(ElanLocale.getString("TierSet.Title.EditTierSet"));
		editTierLabel.setFont(editTierLabel.getFont().deriveFont((float) 14));
		editTierLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel filePanelLabel = new JLabel("FileSelectionPannel");
		filePanelLabel.setFont(editTierLabel.getFont().deriveFont((float) 14));
		filePanelLabel.setHorizontalAlignment(SwingConstants.CENTER);	       
		
		// button panel
		okButton = new JButton(ElanLocale.getString("Button.Apply"));
	    cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
	    
	    okButton.addActionListener(this);
	    cancelButton.addActionListener(this);
	    
	    JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
	    buttonPanel.add(okButton);
	    buttonPanel.add(cancelButton);
	    
	    int y = 0;
        
	    GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.insets = globalInsets;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 1.0;
		gbc.weightx = 0.0;
		getContentPane().add(tierSetPanel, gbc);
		
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		getContentPane().add(tierSetAttriPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = ++y;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 0.0;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		getContentPane().add(buttonPanel, gbc);
		
		List<String> list = tierSetUtil.getTierSetList();
		TierSet tierSet;
		for(String name: list){
			tierSet = tierSetUtil.getTierSet(name);
			tierSetModel.addRow( new Object[]{tierSet.isVisible(), name});
		}
		tierSetTable.getSelectionModel().addListSelectionListener(this);
		if(tierSetTable.getRowCount() > 0){
			tierSetTable.setRowSelectionInterval(0, 0);
		}
		
		updateMissingTiers();
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
        	@Override
			public void windowClosing(WindowEvent evt) {
        		closeDialog();
        	}
        });
	}
	
	/**
     * Closes this dialog
     */
    private void closeDialog() {
    	WindowLocationAndSizeManager.storeLocationAndSizePreferences(this, "ManageTierSetDlg");
    	setVisible(false);
		dispose();
	}
	
	/**
	 * Set up a table and model with the proper columns and other settings.
	 * The columns are called  VISIBLE_COLUMN and NAME_COLUMN even for
	 * tables that contain other field values.
	 */
	protected void initTableAndModel(JTable table, DefaultTableModel model) {
        model.setColumnIdentifiers(new String[] { VISIBLE_COLUMN, NAME_COLUMN });
        table.getColumn(VISIBLE_COLUMN)
                 .setCellEditor(new DefaultCellEditor(new JCheckBox()));
        table.getColumn(VISIBLE_COLUMN)
                 .setCellRenderer(new CheckBoxTableCellRenderer());
        table.getColumn(VISIBLE_COLUMN).setMaxWidth(60);
	}    
	
   /**
    * Opens a dialog to create new tier set
    */
	private void addTierSet(){
    	EditTierSetDlg dlg = new EditTierSetDlg(this, ElanLocale.getString("TierSet.Title.NewTierSet"));
		dlg.setVisible(true);
		TierSet tierSet = dlg.getTierSet();
		if(tierSet != null){
			tierSetModel.addRow(new Object[]{tierSet.isVisible(), tierSet.getName()});
			tierSetTable.setRowSelectionInterval(tierSetModel.getRowCount()-1,
					tierSetModel.getRowCount()-1);
		}
		
		updateMissingTiers();
    }
	
	private void updateMissingTiers(){
		if(transcription == null){
			return;
		}
		
		TierSet ts;
		List<String> tierList;
		
		missingTiersList.clear();
		
		for(int i=0; i < tierSetTable.getRowCount(); i++){
			ts = tierSetUtil.getTierSet((String) tierSetTable.getValueAt(i, NAME_COL_INDEX));
			tierList = ts.getTierList();
			for(String tier: tierList){
				if(transcription.getTierWithId(tier) == null){
					if(!missingTiersList.contains(tier)){
						missingTiersList.add(tier);
					}
				}
			}
		}
		
		if(missingTiersList.size() > 0){
			missingTiersLabel.setText("* tiers marked red are missing in this transcription");
		}
	}
    
	/**
	 * Opens a dialog to edit the current tier set
	 */
    private void editTierSet(){
    	String tierSetName = nameTextArea.getText();
    	TierSet tierSet = TierSetUtil.getTierSetUtilInstance().getTierSet(tierSetName);    	
    	
    	EditTierSetDlg dlg = new EditTierSetDlg(this, tierSet, ElanLocale.getString("TierSet.Title.EditTierSet"));
		dlg.setVisible(true);
		
		tierSet = dlg.getTierSet();
		if(tierSet != null){
			tierSetModel.setValueAt(tierSet.getName(), tierSetTable.getSelectedRow(), NAME_COL_INDEX);
			loadTierSetDescPanel(tierSet);
		}
		
		updateMissingTiers();
    }
    
   /**
    * Deletes a tier set
    */
    private void deleteTierSet(){
    	int selectedRowIndex = tierSetTable.getSelectedRow();
		tierSetModel.removeRow(selectedRowIndex);		
		tierSetUtil.deleteTierSet(nameTextArea.getText());
		
		if(tierSetModel.getRowCount() > 0){
			if(selectedRowIndex > tierSetTable.getRowCount()-1){
				selectedRowIndex = tierSetTable.getRowCount()-1;
			}
			tierSetTable.setRowSelectionInterval(selectedRowIndex, selectedRowIndex);
		} else {
			loadTierSetDescPanel(null);
		}
		
		updateMissingTiers();
    }
    
    /**
     * Moves selected tiers up in the list of tiers.
     */
    private void moveUp() {
        if ((tierSetTable == null) || (tierSetModel == null) ||
                (tierSetModel.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierSetTable.getSelectedRows();

        for (int i = 0; i < selected.length; i++) {
            int row = selected[i];

            if ((row > 0) && !tierSetTable.isRowSelected(row - 1)) {
            	tierSetModel.moveRow(row, row, row - 1);
            	tierSetTable.changeSelection(row, 0, true, false);
            	tierSetTable.changeSelection(row - 1, 0, true, false);
            }
        }
    }
    
    /**
     * Moves selected tiers up in the list of tiers.
     */
    private void moveDown() {
        if ((tierSetTable == null) || (tierSetModel == null) ||
                (tierSetModel.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierSetTable.getSelectedRows();

        for (int i = selected.length - 1; i >= 0; i--) {
            int row = selected[i];

            if ((row < (tierSetModel.getRowCount() - 1)) &&
                    !tierSetTable.isRowSelected(row + 1)) {
            	tierSetModel.moveRow(row, row, row + 1);
            	tierSetTable.changeSelection(row, 0, true, false);
            	tierSetTable.changeSelection(row + 1, 0, true, false);
            }
        }
    }
    
    /**
     * Sorts the current table model alphabetically.
     */
    private void sortAZ() {		
		if (tierSetModel.getRowCount() < 2) {
			return;
		}
				
				
		String curSelectedTS = "";
		if(tierSetTable.getSelectedRow() > -1){
			curSelectedTS = (String) tierSetTable.getValueAt(tierSetTable.getSelectedRow(), NAME_COL_INDEX);
		}
		
		HashMap<String, Boolean> tierSetVisibleMap  = new HashMap<String, Boolean>();
				
		List<String> values = new ArrayList<String>(tierSetModel.getRowCount());	   
		String tierSetName;
		for (int i = 0; i < tierSetModel.getRowCount(); i++) {
			tierSetName = (String)tierSetModel.getValueAt(i, NAME_COL_INDEX);
			values.add(tierSetName);
			tierSetVisibleMap.put(tierSetName, 
					(Boolean)tierSetModel.getValueAt(i, VISIBLE_COL_INDEX));			
		}
		
		Collections.sort(values);
		
		tierSetTable.getSelectionModel().removeListSelectionListener(this);
		
		for (int i = 0; i < tierSetModel.getRowCount(); i++) {
			tierSetName = values.get(i);
			tierSetModel.setValueAt(tierSetName, i, NAME_COL_INDEX);
			tierSetModel.setValueAt(tierSetVisibleMap.get(tierSetName), i, VISIBLE_COL_INDEX);
			if(tierSetName.equals(curSelectedTS)){
				tierSetTable.setRowSelectionInterval(i, i);
			}
		}
		
		tierSetTable.getSelectionModel().addListSelectionListener(this);
    }
    
    
    /**
     * Store changes of the current tier set temporally in a map
     */
    private void storeCurrentTierSetChanges(){  				
    	String tierSetName = nameTextArea.getText().trim();
    	HashMap<String, Boolean> tierMap = null;
    				
    				
    	if(tierSetMap.containsKey(tierSetName)){
    		tierMap = tierSetMap.get(tierSetName);
    		tierMap.clear();
    	}
    				
    	if(tierMap == null){
    		tierMap = new HashMap<String, Boolean>();
    		tierSetMap.put(tierSetName, tierMap);
    	}
    				
    	String tierName;
    	boolean isVisible;
    				
    	for(int i = 0; i < tierTable.getRowCount(); i ++){
    		tierName = (String) tierTable.getValueAt(i,NAME_COL_INDEX);
    		isVisible = (Boolean) tierTable.getValueAt(i, VISIBLE_COL_INDEX);
    		tierMap.put(tierName, isVisible);
    	}
    }
    
	/**
	 * Loads the tier set attribute panel
	 * 
	 * @param tierSet, tier set to be loaded
	 */
    private void loadTierSetDescPanel(TierSet tierSet){		
		// clear all inputs
		nameTextArea.setText("");
		descTextArea.setText("");
		while(tierModel.getRowCount() > 0){
			tierModel.removeRow(tierModel.getRowCount()-1);			
		}
		
		if(tierSet != null){
			nameTextArea.setText(tierSet.getName());
			descTextArea.setText(tierSet.getDescription());
			
			if(tierSetMap.containsKey(tierSet.getName())){
	    		HashMap <String, Boolean> tierMap = tierSetMap.get(tierSet.getName());
	    		List<String> visibleTiers = tierSet.getVisibleTierList();
				for(String tierName : tierSet.getTierList()){
	    			if(tierMap.containsKey(tierName)) {
	    				tierModel.addRow(new Object[]{tierMap.get(tierName), tierName});
	    			} else {
	    				tierModel.addRow(new Object[]{visibleTiers.contains(tierName), tierName});
	    			}
				}
	    	} else {
	    		List<String> visibleTiers = tierSet.getVisibleTierList();
				for(String tierName : tierSet.getTierList()){
					tierModel.addRow(new Object[]{visibleTiers.contains(tierName), tierName});
				}
	    	}
			editButton.setEnabled(true);
		} else {
			editButton.setEnabled(false);
		}
	}	
	
	private List<String> getFileNamesList(){
		List<String> fileList = new ArrayList<String>();
		if(openFilesList != null){
			for(File f: openFilesList){
				fileList.add(f.getAbsolutePath());
			}
			return fileList;
		} else {
			return null;
		}
	}
	
	public TranscriptionImpl getTranscription(){
		return transcription;
	}
	
	public void setTierSelectPanel(AbstractTierSortAndSelectPanel tierSelectPanel){
		this.tierSelectPanel = tierSelectPanel;
	}
	
	public AbstractTierSortAndSelectPanel getTierSelectPanel(){
		return tierSelectPanel;
	}
	@Override
	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		if(source == addButton){
			addTierSet();
		} 
		else if(source == editButton){
			editTierSet();
		}
		else if(source == deleteButton){
			deleteTierSet();
		}
		else if (source == sortButton) {
			sortAZ();
        } 	
		else if(source == upButton){
			moveUp();
		}
		else if(source == downButton){
			moveDown();
		}
		else if(source == okButton){
			storeCurrentTierSetChanges();
			
			//check for changes
			String tierSetName;
			TierSet tierSet;
			List<String> tierSetSortOrder = new ArrayList<String>();
			for(int i=0; i < tierSetTable.getRowCount(); i++){
				tierSetName = (String) tierSetTable.getValueAt(i, NAME_COL_INDEX);
				tierSet = tierSetUtil.getTierSet(tierSetName);
				tierSetSortOrder.add(tierSetName);
				
				tierSet.setVisible((Boolean)tierSetTable.getValueAt(i, VISIBLE_COL_INDEX));
				
				HashMap<String, Boolean> tierMap = tierSetMap.get(tierSetName);
				if(tierMap != null){					
					for(String tierName: tierMap.keySet()){
						tierSet.setTierVisiblity(tierName, tierMap.get(tierName));
					}
				}
			}
			tierSetUtil.updateTierSetSortOrder(tierSetSortOrder);
	    	tierSetUtil.writeTierSetsToFile();
	    	
	    	//notify all listeners
	    	tierSetUtil.notifyAllListeners();
	    	closeDialog();
		}
		else if(ae.getSource() == cancelButton){
			closeDialog();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if(tierSetTable.getSelectedRow() > -1){
			storeCurrentTierSetChanges();
			loadTierSetDescPanel(tierSetUtil.getTierSet((String)tierSetTable.getValueAt(
					tierSetTable.getSelectedRow(), NAME_COL_INDEX)));
		}
	}
	
	private class MyTableCellRender extends DefaultTableCellRenderer{
		
		public MyTableCellRender(){
			super();
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
			       Object value, boolean isSelected, boolean hasFocus, int row,
			       int column){
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if(column == NAME_COL_INDEX){
				if(missingTiersList.contains(value.toString())){
					cell.setForeground(Color.RED);
				} else {
					cell.setForeground(table.getForeground());
				}
			} else {
				cell.setForeground(table.getForeground());
			}
			
			
			return cell;
		  
			
		}
		
	}
	
	
}
