package mpi.eudico.client.annotator.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel.Modes;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A dialog as a host for a tier-sort-and-select panel.
 *  
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class TierSortAndSelectDialog2 extends JDialog implements ActionListener {
	private TranscriptionTierSortAndSelectPanel selectPanel;
	
    /** panel for start and close buttons (bottom component) */
    protected JPanel buttonPanel;
    /** close button */
    private JButton cancelButton;

    /** ok button */
    private JButton okButton;
    
    private List<String> returnedTiers = null;
	
	/**
	 * @param owner
	 */
	public TierSortAndSelectDialog2(Dialog owner,
			TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers) {
		super(owner, true);
		selectPanel = new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers);
		initComponents();
	}

	/**
	 * @param owner
	 */
	public TierSortAndSelectDialog2(Dialog owner,
			TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting) {
		super(owner, true);
		selectPanel = new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers, 
				allowReordering, allowSorting);
		initComponents();
	}
	
	/**
	 * @param owner
	 */
	public TierSortAndSelectDialog2(Dialog owner,
			TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting, 
			Modes tierMode) {
		super(owner, true);
		selectPanel = new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers, 
				allowReordering, allowSorting, tierMode);
		initComponents();
	}

	/**
	 * @param owner
	 */
	public TierSortAndSelectDialog2(Frame owner,
			TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers) {
		super(owner, true);
		selectPanel = new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers);
		initComponents();
	}
	
	/**
	 * @param owner
	 */
	public TierSortAndSelectDialog2(Frame owner,
			TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting) {
		super(owner, true);
		selectPanel = new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers, 
				allowReordering, allowSorting);
		initComponents();
	}
	
	/**
	 * @param owner
	 */
	public TierSortAndSelectDialog2(Frame owner,
			TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting, 
			Modes tierMode) {
		super(owner, true);
		selectPanel = new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers, 
				allowReordering, allowSorting, tierMode);
		initComponents();
	}
	
	private void initComponents() {
		getContentPane().setLayout(new GridBagLayout());
		Insets insets = new Insets(4, 6, 4, 6);
		// add tab pane / panel
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(selectPanel, gridBagConstraints);
		
		// add ok/cancel panel
		cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);
        
        pack();
        int minW = 400;
        int minH = 320;
        setSize(Math.max(minW, getWidth()), Math.max(minH, getHeight()));
	}
	
    /**
     * Returns the selected tiers.
     *
     * @return the selected tiers or null in case of canceling the dialog
     */
    public List<String> getSelectedTiers() {
        return returnedTiers;
    }
    
    /**
     * Sets the selected tiers. Tiers not in the list will be unselected.
     * 
     * @see #setHiddenTiers(List)
     * @param selectedTiers the list of selected tiers
     */
    public void setSelectedTiers(List<String> selectedTiers) {
    	selectPanel.setSelectedTiers(selectedTiers);
    }
    
    /**
     * Returns the hidden, the de-selected tiers.
     * 
     * @see #getSelectedTiers()
     * @return the hidden tiers
     */
    public List<String> getHiddenTiers() {
    	return selectPanel.getHiddenTiers();
    }
    
    /**
     * Sets the unselected, hidden tiers. Tiers not in this list will be selected.
     * 
     * @see #setSelectedTiers()
     * @param hiddenTiers
     */
    public void setHiddenTiers(List<String> hiddenTiers) {
    	selectPanel.setHiddenTiers(hiddenTiers);
    }
    
    /**
     * Returns all tiers in the current order.
     * 
     * @return the list of all tiers
     */
    public List<String> getTierOrder() {
    	selectPanel.applyChanges();
        
    	return selectPanel.getTierOrder();
    }
    
    /**
     * Returns the currently used selection mode
     * 
     * (i.e whether the selection of tiers is based on
     * types / participant/ tier names/ annotators)
     * 
     * @return
     */
    public String getSelectionMode() {   	
    	return selectPanel.getSelectionMode();
    }
    
    /**
     * Sets the last used selection mode.
     * 
     * @param mode tab
     * @param hiddenTiers the tiers that have been deselected before
     */
    public void setSelectionMode(String mode, List<String> hiddenItems) {
    	selectPanel.setSelectionMode(mode, hiddenItems);
    }
    
    /**
     * Returns the current selected items.
     * 
     * @see #getUnselectedItems()
     * @return the list of selected items
     */
    public List<String> getSelectedItems() {
    	return selectPanel.getSelectedItems();
    }
    
    /**
     * Returns the items that are unselected in the current tab.
     * 
     * @see #getSelectedItems()
     * @return the list of unselected items
     */
    public List<String> getUnselectedItems() {
    	return selectPanel.getUnselectedItems();
    }
    
    /**
     * Sets items selected in the current table/tab. 
     * 
     * @see #setUnselectedItems(List)
     * @param items the selected items
     */
    public void setSelectedItems(List<String> items) {
    	selectPanel.setSelectedItems(items);
    }
    
    /**
     * Sets items unselected in the current table/tab.
     * 
     * @see #setSelectedItems(List)
     * @param items the unselected items
     */
    public void setUnselectedItems(List<String> items) {
    	selectPanel.setUnselectedItems(items);
    }

    /**
     * Ok and cancel button actions.
     */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
            setVisible(false);
            dispose();
        } else if (e.getSource() == okButton) {
        	selectPanel.applyChanges();
            returnedTiers = selectPanel.getSelectedTiers();
            setVisible(false);
            dispose();
        }
		
	}
}
