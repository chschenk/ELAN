package mpi.eudico.client.annotator.export;


import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel.Modes;
import mpi.eudico.client.annotator.gui.FilesTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.TranscriptionTierSortAndSelectPanel;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

@SuppressWarnings("serial")
public abstract class AbstractExtTierExportDialog extends AbstractBasicExportDialog {   
    
    /** A constant for unspecified participant or linguistic type */
    protected final String NOT_SPECIFIED = "not specified";
		  
    /** Tier selection panel and its components */
    protected final JPanel tierSelectionPanel = new JPanel(); 

	
	/** restrict export to selection ? */
    protected final JCheckBox restrictCheckBox = new JCheckBox();
	
	/** selection */
    protected final Selection selection;	
    protected List<File> files;	
	protected boolean multipleFileExport = false;
	
	protected List<String> allTierNames;		   

	protected AbstractTierSortAndSelectPanel tierSelectPanel;
	// pane and scrollpane in case the contents is too big to fit in the window/on the screen
    protected JPanel outerPanel;
    protected JScrollPane outerScrollPane;

    /**
     * Creates a new AbstractTierExportDialog instance.
     *
     * @param parent the parent frame
     * @param modal whether this dialog should be modal
     * @param transcription the transcription
	 * @param selection the selection
     */
    public AbstractExtTierExportDialog(Frame parent, boolean modal, TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription);
        this.selection = selection;
        
        allTierNames = new ArrayList<String>();
    }
    
    /**
     * Creates a new AbstractTierExportDialog instance.
     *
     * @param parent the parent frame
     * @param modal whether this dialog should be modal
     * @param files the list of all files
     */
    public AbstractExtTierExportDialog(Frame parent, boolean modal, List<File> files) {    	
    	this(parent, modal, null, null);
        this.files = files;
        multipleFileExport = true;
    }
	
	/**
     * Initializes UI elements.
     */
    @Override
	protected void makeLayout() {
        super.makeLayout();
        outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());
        outerScrollPane = new JScrollPane(outerPanel);
        outerScrollPane.setBorder(null);
        
        getContentPane().setLayout(new GridBagLayout());
        if (!multipleFileExport) {
        	tierSelectPanel = new TranscriptionTierSortAndSelectPanel(transcription, getModeForExport());
        } else {
        	tierSelectPanel = new FilesTierSortAndSelectPanel(files, getModeForExport());
        }
        optionsPanel.setLayout(new GridBagLayout());

        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        // tierSelection panel
        tierSelectionPanel.setLayout(new GridBagLayout()); 
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tierSelectionPanel.add(tierSelectPanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        
        //main panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(titleLabel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        //getContentPane().add(tierSelectionPanel, gridBagConstraints);
        outerPanel.add(tierSelectionPanel, gridBagConstraints);
       
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        //getContentPane().add(optionsPanel, gridBagConstraints);
        outerPanel.add(optionsPanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(outerScrollPane, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);
        
        addComponentListener(new SizeListener());
    }  
    
    /**
     * To be overridden by the actual export classes.
     *  
     * @return one of the tier modes, the default is Modes.ALL_TIERS
     */
    protected Modes getModeForExport() {
    	return Modes.ALL_TIERS;
    }
    
    /**
     * extract tiers from the transcription
     * 
     * @param selectOnlyRootTiers - if true selects all the root tiers
     */
    
    protected void extractTiers(boolean selectOnlyRootTiers){
    	multipleFileExport = false;   
    	allTierNames.clear();  

    	// if no preferred order is stored, take the order of the document global tier order
    	allTierNames.addAll(ELANCommandFactory.getViewerManager(transcription).getTierOrder().getTierOrder());
    	tierSelectPanel.setTierOrder(allTierNames);
    	
    }
    
    /**
     * Extracts all unique tiers from multiple files.
     */   
    protected void extractTiersFromFiles() {
    	multipleFileExport = true;
    }    

    /**
     * Set the localized text on ui elements
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportDialog.Label.SelectTiers")));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportDialog.Label.Options")));
        restrictCheckBox.setText(ElanLocale.getString("ExportDialog.Restrict"));
    }
    
    /**
     * Returns the selected tier names
     * 
     * @return
     */
    protected List<String> getSelectedTiers(){     	
    	return tierSelectPanel.getSelectedTiers();
    }
    
    /**
     * Returns the list of hidden ties of a 
     * certain group(type /participants/ annotators)
     * 
     * @return
     */
    protected List<String> getHiddenTiers(){
    	return tierSelectPanel.getHiddenTiers();
    }
    
    /**
     * Returns the current tierOrder of this export
     * 
     * @ return  tierOrder
     */
    /*
    protected List<String> getCurrentTierOrder() {  
    	List<String> tierOrder = new ArrayList<String>(); // <String>?
      
        for (int i = 0; i < model.getRowCount(); i++) { 
        	tierOrder.add(model.getValueAt(i, nameCol));            
        }
        
        return tierOrder;
    }*/
    
    /**
     * Returns the currently used selection mode
     * 
     * (i.e whether the selection of tiers is based on
     * types / participant/ tier names/ annotators)
     * 
     * @return
     */
    protected String getSelectionMode(){
    	return tierSelectPanel.getSelectionMode();
    }
    
    /**
     * Returns a list of the currently selected items, regardless of selected tab.
     * @return a list of selected items
     */
    protected List<String> getSelectedItems() {
    	return tierSelectPanel.getSelectedItems();
    }
    
    /**
     * Returns a list of the currently unselected items in the current tab.
     * @return a list of unselected items
     */
    protected List<String> getUnselectedItems() {
    	return tierSelectPanel.getUnselectedItems();
    }
    
    /**
     * Returns the initially used tier order for this export
     * 
     * @return
     */
    protected List<String> getTierOrder(){
    	return tierSelectPanel.getTierOrder();
    }    

    /** 
     *Sets and removes the hidden tiers from selection
     * 
     * @param hiddenTiers
     */
    private void setHiddenTiers(List<String> hiddenTiers){   
    	if(hiddenTiers == null){
    		return;
    	}
    	tierSelectPanel.setHiddenTiers(hiddenTiers);
    	
    }
    
    /**
     * Sets the selected tiers.
     * 
     * @param selectedTiers
     */
    protected void setSelectedTiers(List<String> selectedTiers) {
    	tierSelectPanel.setSelectedTiers(selectedTiers);
    }
    
    /**
     * Sets the selection mode (tier, type etc) one of the constants of the AbstractTierSortAndSelectPanel
     * @param mode the mode
     */
    protected void setSelectionMode(String mode) {
    	tierSelectPanel.setSelectionMode(mode, null);
    }
    
    /**
     * After the selected tab has been set, this method can be used to set the selected items.
     * 
     * @param items
     */
    protected void setSelectedItems(List<String> items) {
    	tierSelectPanel.setSelectedItems(items);
    }
    
    /**
     * After the selected tab has been set, this method can be used to set the unselected items.
     * 
     * @param items the items to deselect
     */
    protected void setUnselectedItems(List<String> items) {
    	tierSelectPanel.setUnselectedItems(items);
    }
    
    /**
     * Sets the selection mode and also the tiers hidden in that mode
     * 
     * @param setSelectType
     */
    protected void setSelectedMode(String selectionMode, List<String> hiddenTiers){      	
    	if(selectionMode == null){
    		return;
    	}
    	tierSelectPanel.setSelectionMode(selectionMode, hiddenTiers);
    }
    
    /**
     * Sets the tier order
     * 
     * @param storedTierOrder
     */
    protected void setTierOrder(List<String> storedTierOrder){
    	tierSelectPanel.setTierOrder(storedTierOrder);
    }
    
    /**
     * Sets the flag for filtering root tiers only or showing all tiers. 
     * 
     * @param rootsOnly if true only top level tier are shown, otherwise all tiers 
     * (of the particular mode of the selection panel) will be shown
     */
    protected void setRootTiersOnly(boolean rootsOnly) {
    	tierSelectPanel.setRootTiersOnly(rootsOnly);
    }
    
    /**
     * Returns whether the selection panel is in root tiers only mode.
     * 
     * @return whether root mode is on or of
     */
    protected boolean isRootTiersOnly() {
    	return tierSelectPanel.isRootTiersOnly();
    }
          

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
    	super.actionPerformed(ae);
    } 
    
    /**
     * A class that adds or removes a border to the outer scrollpane,
     * depending on whether one or both the of scrollbars are visible or not.
     * 
     * @author Han Sloetjes
     */
    class SizeListener implements ComponentListener {

		@Override
		public void componentHidden(ComponentEvent e) {
		}

		@Override
		public void componentMoved(ComponentEvent e) {	
		}

		@Override
		public void componentResized(ComponentEvent e) {
			if (outerScrollPane != null) {
				if (outerScrollPane.getHorizontalScrollBar().isVisible() || 
						outerScrollPane.getVerticalScrollBar().isVisible()) {
					if (outerScrollPane.getBorder() == null) {
						outerScrollPane.setBorder(new LineBorder(Color.GRAY, 1));
					}
				} else {
					if (outerScrollPane.getBorder() != null) {
						outerScrollPane.setBorder(null);
					}
				}
			}
		}

		@Override
		public void componentShown(ComponentEvent e) {
			componentResized(e);
		}   	
    }
    
}

