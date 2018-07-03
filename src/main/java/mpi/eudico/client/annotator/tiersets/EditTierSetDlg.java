package mpi.eudico.client.annotator.tiersets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;

/**
 * A dialog to create new or change existing tier sets
 *
 * @author Aarthy Somasundaram
 */
@SuppressWarnings("serial")
public class EditTierSetDlg extends ClosableDialog implements ActionListener{	
	
	private JTextArea nameTextArea, descTextArea;
	
	private JButton showOrHideButton, addButton, removeButton, upButton, downButton;
	
	private JTable tierTable;
	private DefaultTableModel tierModel;
	
	private JButton changeButton;
	private JButton cancelButton;
	
	private TierSetUtil tierSetUtil;	
	private TierSet tierSet = null;
	private ManageTierSetDlg manageTierSetDlg;
	
	private final int ADD = 0;
	private final int EDIT = 1;
	
	
	
	private int mode;
	
	private Insets globalInsets = new Insets(2,4,2,4);
	
	
	/**
	 * Constructor used to add new tier set
	 * 
	 * @param owner
	 * @param trans	 
	 */
	public EditTierSetDlg(ManageTierSetDlg owner, String title){
		this(owner, null ,title);
	}
	
	/**
	 * Constructor used to modify existing tier set
	 * 
	 * @param owner
	 * @param trans
	 * @param tierSet
	 */
	public EditTierSetDlg(ManageTierSetDlg owner, TierSet tierSet, String title){
		super(owner, title, true);
		manageTierSetDlg = owner;
		this.tierSet = tierSet;
		if(tierSet == null){
			mode = ADD;
		} else {
			mode = EDIT;
		}
		
		tierSetUtil = TierSetUtil.getTierSetUtilInstance();
		initComponents();
		WindowLocationAndSizeManager.postInit(this, "EditTierSetDlg");
	}
	
	
	
	/**
	 * Initializes and returns tier panel
	 * @return
	 */
	private JPanel getTierPanel(){		
		showOrHideButton = new JButton(ElanLocale.getString("Button.Hide")); 
		showOrHideButton.addActionListener(this); 				
		
		tierModel = new TierExportTableModel();
		tierTable = new JTable(tierModel);
		manageTierSetDlg.initTableAndModel(tierTable, tierModel);
		tierTable.setDragEnabled(true);
		tierTable.setDropMode(DropMode.USE_SELECTION);
		tierTable.setTransferHandler(ManageTierSetDlg.tableDragAndDropHandler());
		
		// tier panel
		JPanel tierPanel = new JPanel();
		tierPanel.setLayout(new GridBagLayout());
		tierPanel.setBorder(new TitledBorder(ElanLocale.getString("TierSet.TierList")));
		int y = 0;
				
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.insets = globalInsets;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		tierPanel.add(new JScrollPane(tierTable), gbc);		
				
		gbc.gridy = ++y;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.0;
		tierPanel.add(getTierButtonPanel(), gbc);	
		
		return tierPanel;
	}	
	
	/**
	 * Initialize and return a tier button panel
	 * 
	 * @return panel
	 */
	private JPanel getTierButtonPanel(){
		addButton = new JButton();
		addButton.setToolTipText(ElanLocale.getString("Button.Add"));
		addButton.addActionListener(this);
		
		removeButton = new JButton();
		removeButton.setToolTipText(ElanLocale.getString("Button.Delete"));
		removeButton.addActionListener(this);
		
		 try {
			 ImageIcon addIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Plus16.gif"));
			 ImageIcon removeIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Minus16.gif"));
			 addButton.setIcon(addIcon);
			 removeButton.setIcon(removeIcon);
	     } catch (Exception ex) {
	    	 addButton.setText("+");
	    	 removeButton.setText("-");
	     }
		
		upButton = new JButton();
		upButton.setToolTipText(ElanLocale.getString("EditCVDialog.Button.Up"));
		 
		downButton = new JButton();
		downButton.setToolTipText(ElanLocale.getString("EditCVDialog.Button.Down"));

		upButton.addActionListener(this);
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
		
		// tier Button panel
		JPanel tierButtonPanel = new JPanel();
		tierButtonPanel.setLayout(new GridBagLayout());
				
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = globalInsets;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		tierButtonPanel.add(upButton, gbc);
				
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		tierButtonPanel.add(downButton, gbc);
				
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		tierButtonPanel.add(addButton);
				
		gbc.gridx = 3;
		tierButtonPanel.add(removeButton);
		
		return tierButtonPanel;
	}
	
	public void initComponents(){		    
	    changeButton = new JButton();
	    changeButton.addActionListener(this);
	    
	    cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
	    cancelButton.addActionListener(this);
	    
		if(mode == ADD){
			changeButton.setText(ElanLocale.getString("SegmentsToTierDialog.Button.Create"));
		} else if(mode == EDIT) {
			changeButton.setText(ElanLocale.getString("Button.Change"));
		}			
		KeyAdapter keyListener = new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_TAB){
					if(e.getSource() == nameTextArea){
						descTextArea.requestFocus();
					} else {
						nameTextArea.requestFocus();
					}
				}
			}
	    };
		
	    nameTextArea = new JTextArea(1,1);
	    nameTextArea.addKeyListener(keyListener);
	    
	    descTextArea = new JTextArea(5,1);
	    descTextArea.setWrapStyleWord(true);	
	    descTextArea.addKeyListener(keyListener);
	    
        // button panel
	    JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
	    buttonPanel.add(changeButton);
	    buttonPanel.add(cancelButton);
	    
	    
	    // main layout
	    setLayout(new GridBagLayout());
	    
	    int y = 0;
	    
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.anchor = GridBagConstraints.NORTH;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.gridx = 0;
	    gbc.gridy = y;
	    gbc.insets = new Insets(4, 6, 4, 6);
	    add(new JLabel(ElanLocale.getString("TierSet.Name")), gbc);
	    
	    gbc.gridx = 1;
	    gbc.weightx = 1.0;
	    add(nameTextArea, gbc);
	    
	    gbc.gridx = 0;
	    gbc.gridy = ++y;
	    gbc.weightx = 0.0;
	    add(new JLabel(ElanLocale.getString("TierSet.Description")), gbc);
	    
	    gbc.gridx = 1;
	    gbc.weightx = 1.0;
	    add(descTextArea, gbc);
	    
	    gbc.gridx = 0;
		gbc.gridy = ++y;
		gbc.gridwidth = 2;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(getTierPanel(), gbc);
		
	    gbc.gridy = ++y;
	    gbc.weighty = 0.0;
	    gbc.fill = GridBagConstraints.NONE;
	    add(buttonPanel, gbc);
	    
	    if(mode == EDIT){
	    	nameTextArea.setText(tierSet.getName());
	    	descTextArea.setText(tierSet.getDescription());
	    	
	    	List<String> visibleTiersList = tierSet.getVisibleTierList();
		    for(String tier : tierSet.getTierList()){
		    	tierModel.addRow(new Object[]{visibleTiersList.contains(tier), tier});
		    }
	    }
	    
	    
	    
	    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                	if(mode == ADD){
                		tierSet = null;
                	}
                    closeDialog();
                }
        });
	}
	
	
	/**
     * Closes this dialog
     */
    private void closeDialog() {
    	WindowLocationAndSizeManager.storeLocationAndSizePreferences(this, "EditTierSetDlg");
		setVisible(false);
		dispose();
	}
    
    public TierSet getTierSet(){
    	return tierSet;
    }
    
    private void createTierSet(){
    	if(nameTextArea.getText() != null && nameTextArea.getText().trim().length() > 0){
    		String tierSetName = nameTextArea.getText().trim();
    		if(!tierSetUtil.checkIfTierSetExists(tierSetName)){
    			List<String> tierList = new ArrayList<String>();
    			for(int i =0; i < tierTable.getRowCount(); i++){
    				tierList.add((String)tierTable.getValueAt(i, 1));
    			}
//    			if(tierList.size() == 0){
//    				JOptionPane.showMessageDialog(this, "A tier set should have at least one tier. Please add tiers to tier set.", ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
//            		tierTable.requestFocusInWindow();
//            		return;
//    			}
    			
    			tierSet = tierSetUtil.createTierSet(tierSetName, tierList);
    			
    			for(int i =0; i < tierTable.getRowCount(); i++){
    				tierSet.setTierVisiblity(((String)tierTable.getValueAt(i, 1)),
    						((Boolean)tierTable.getValueAt(i, 0)));
    			}
    			
    			if(descTextArea.getText() != null){
    				tierSet.setDescription(descTextArea.getText());
    			}
    			
        			
        			closeDialog();
        		
    		} else {
    			JOptionPane.showMessageDialog(this, "Tier Set name already exist!", ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
        		nameTextArea.requestFocusInWindow();
        		return;
    		}
    	} else {
    		JOptionPane.showMessageDialog(this, "Please enter a valid name for the tier set", ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
    		nameTextArea.requestFocusInWindow();
    		return;
    	}
    }
    
    private void updateTierSet(){
    	// tier list
		List<String> tierList = new ArrayList<String>();
		for(int i =0; i < tierTable.getRowCount(); i++){
			tierList.add((String)tierTable.getValueAt(i, 1));
		}
    	
    	if(tierList.size() == 0){
			JOptionPane.showMessageDialog(this, "A tier set should have at least one tier. Please add tiers to tier set.", ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
    		tierTable.requestFocusInWindow();
    		return;
		}
    	
    	if(nameTextArea.getText() != null && nameTextArea.getText().trim().length() > 0){
    		String tierSetName = nameTextArea.getText().trim();
    		String oldName = tierSet.getName();
    		//check tier set name
    		if(!tierSet.getName().equals(tierSetName)){
    			if(!tierSetUtil.checkIfTierSetExists(tierSetName)){
    				tierSet.setName(tierSetName);
    				tierSetUtil.updateTierSet(oldName, tierSet);
    			} else {
        			JOptionPane.showMessageDialog(this, "Tier Set name already exist!", ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
            		nameTextArea.requestFocusInWindow();
            		return;
    			}
    		}
    		
    		//set desc
    		if(descTextArea.getText() != null){
				tierSet.setDescription(descTextArea.getText());
			}    			
    		tierSet.setTierList(tierList);
    			
    		for(int i =0; i < tierTable.getRowCount(); i++){
    			tierSet.setTierVisiblity(((String)tierTable.getValueAt(i, 1)),
    				((Boolean)tierTable.getValueAt(i, 0)));
    		}
    		
    		closeDialog();    		
    	} else {
    		JOptionPane.showMessageDialog(this, "Please enter a valid name for the tier set", ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
    		nameTextArea.requestFocusInWindow();
    	}
    }
    
    /**
     * Moves selected tiers up in the list of tiers.
     */
    protected void moveUp() {
        if ((tierTable == null) || (tierModel == null) ||
                (tierModel.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int i = 0; i < selected.length; i++) {
            int row = selected[i];

            if ((row > 0) && !tierTable.isRowSelected(row - 1)) {
            	tierModel.moveRow(row, row, row - 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row - 1, 0, true, false);
            }
        }
    }
    
    /**
     * Moves selected tiers up in the list of tiers.
     */
    protected void moveDown() {
        if ((tierTable == null) || (tierModel == null) ||
                (tierModel.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int i = selected.length - 1; i >= 0; i--) {
            int row = selected[i];

            if ((row < (tierModel.getRowCount() - 1)) &&
                    !tierTable.isRowSelected(row + 1)) {
            	tierModel.moveRow(row, row, row + 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row + 1, 0, true, false);
            }
        }
    }
    
    /**
     * 
     */
    private void showAddTiersDlg(){
    	List<String> tierList = new ArrayList<String>();
		for(int i =0; i < tierTable.getRowCount(); i++){
			tierList.add((String)tierTable.getValueAt(i, 1));
		}
    	    	
    	AddTiersDlg dlg = new AddTiersDlg(this, manageTierSetDlg, tierList);
    	dlg.setVisible(true);
    		
    	List<String> selectedTiers = dlg.getSelectedTierNames();
    		
    	List<String> curTierList = new ArrayList<String>();
    	for(int i = 0; i < tierTable.getRowCount(); i++){
    		curTierList.add((String) tierTable.getValueAt(i, 1));
    	}
    		
    	//add tiers
    	for(String tier : selectedTiers){
    	  	if(!curTierList.contains(tier)){
        		tierModel.addRow(new Object[]{Boolean.TRUE, tier});
        	}
        }
    }
    
	@Override
	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		
		if(source == addButton){
			showAddTiersDlg();
		}
		else if(source == removeButton){
			int[] rows = tierTable.getSelectedRows();
			
			while(rows.length > 0)
			{
				tierModel.removeRow(tierTable.convertRowIndexToModel(rows[0]));

				rows = tierTable.getSelectedRows();
			}
		}
		else if(source == upButton){
			moveUp();
		}
		else if(source == downButton){
			moveDown();
		}
		if(ae.getSource() == changeButton){
			if(mode ==ADD){
				createTierSet();
			} else {
				updateTierSet();	
			}	
		} else if (ae.getSource() == cancelButton){
			tierSet = null;
			closeDialog();
		}
	}
}
