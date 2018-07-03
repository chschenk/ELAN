package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.praat.PraatTGEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.praat.PraatTextGridEncoder;


@SuppressWarnings("serial")
public class ExportPraatDialog extends AbstractExtTierExportDialog 
    implements ItemListener{
	
    private JCheckBox selectionCB;
    private JCheckBox correctTimesCB;
    
    // preferences keys

    /** pref key */
    final String prefSelectedTiers = "ExportPraatDialog.selectedTiers";
    
    /** pref key */
    final String prefTierOrder = "ExportPraatDialog.TierOrder";
    
    /** pref key */
    final String prefParentTierOrder = "ExportPraatDialog.ParentTierOrder";
    
    /** pref key */
    final String prefSelectTiersMode = "ExportPraatDialog.SelectTiersMode";
    
    /** pref key */
    final String prefLastSelectedItems = "ExportPraatDialog.LastSelectedItems";

    /** pref key */
    final String prefHiddenTiers = "ExportPraatDialog.HiddenTiers";
    
    /** pref key */
    final String prefRootTiersOnly = "ExportPraatDialog.ShowOnlyRootTiers";
    
    /** pref key */
    final String prefSelectionOnly = "ExportPraatDialog.SelectionOnly";
    
    /** pref key */
    final String prefAddOffsetTime = "ExportPraatDialog.AddOffsetTime";
    
    /**
     * @param parent the parent frame
     * @param modal the modal flag
     * @param transcription the transcription
     */
    public ExportPraatDialog(Frame parent, boolean modal,
            TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription, selection);
//        if (selection != null) {
//            System.out.println("sb: " + selection.getBeginTime() + " se: " + selection.getEndTime());
//        }
        makeLayout();
        postInit();
    }

   /**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    @Override
	public void itemStateChanged(ItemEvent ie) {
        extractTiers();
    } 

	/**
	 * Extract candidate tiers for export based on preferences.
	 */
	protected void extractTiers() {    	
		List<String> tierList = Preferences.getListOfString(prefTierOrder, transcription);
	
		if (tierList != null) {
			setTierOrder(tierList);        	
		} else {
			super.extractTiers(false);
		}
	
		tierList = Preferences.getListOfString(prefSelectedTiers, transcription);
		if (tierList != null) {
			setSelectedTiers(tierList);
		} 
    
		String stringPref = Preferences.getString(prefSelectTiersMode, transcription);
		if (stringPref != null) {
			//List list = (List) Preferences.get(prefHiddenTiers, transcription);
			//setSelectedMode((String)useTyp, null);
			setSelectionMode(stringPref);
			
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals(stringPref) ) {
            	// call this after! the mode has been set
            	List<String> selItems  = Preferences.getListOfString(prefLastSelectedItems, transcription);
            	
            	if (selItems instanceof List) {
            		setSelectedItems(selItems);
            	}
        	}
		}	
        setPreferredSetting();
	}


	/**
     * Initializes UI elements.
     */
    @Override
	protected void makeLayout() {
        super.makeLayout();
        
        selectionCB = new JCheckBox();
        correctTimesCB = new JCheckBox();
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        optionsPanel.add(selectionCB, gridBagConstraints);
        
        gridBagConstraints.gridy = 1;
        optionsPanel.add(correctTimesCB, gridBagConstraints);

        updateLocale();
    }
    
    /**
     * Starts the actual export after performing some checks.
     *
     * @return true if export succeeded, false otherwise
     * @see mpi.eudico.client.annotator.export.AbstractBasicExportDialog#startExport()
     */
    @Override
	protected boolean startExport() throws IOException {
        List<String> selectedTiers = getSelectedTiers();
        savePreferences();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return false;
        }

        String[] encodings = new String[]{
        		FileChooser.UTF_8, FileChooser.UTF_16};
        
        // prompt for file name and location
        File exportFile = promptForFile(ElanLocale.getString("ExportPraatDialog.Title"),null,
        		FileExtension.PRAAT_TEXTGRID_EXT, true, encodings);
                    

        if (exportFile == null) {
            return false;
        }
        long begin = 0l;
        long end = ELANCommandFactory.getViewerManager(transcription).getMasterMediaPlayer().getMediaDuration();
        boolean exportSelection = false;
        if (selectionCB.isSelected()) {
            if (selection != null && selection.getBeginTime() < selection.getEndTime()) {
                begin = selection.getBeginTime();
                end = selection.getEndTime();
                exportSelection = true;
            }
        }
        long mediaOffset = 0L;

        if (correctTimesCB.isSelected()) {
            List<MediaDescriptor> mds = transcription.getMediaDescriptors();

            if ((mds != null) && (mds.size() > 0)) {
                mediaOffset = mds.get(0).timeOrigin;
            }
        }
        
        PraatTGEncoderInfo encInfo = new PraatTGEncoderInfo(begin, end);
        encInfo.setEncoding(encoding);
        encInfo.setOffset(mediaOffset);
        encInfo.setExportSelection(exportSelection);

        List<TierImpl> tiers = transcription.getTiersWithIds(selectedTiers);

        PraatTextGridEncoder encoder = new PraatTextGridEncoder();
        encoder.encodeAndSave(transcription, encInfo, tiers, 
                exportFile.getAbsolutePath());
        
        return true;
    }
    
    /**
     * Applies localized strings to the ui elements. For historic reasons the
     * string identifiers start with "TokenizeDialog"
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("ExportPraatDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportPraatDialog.Title"));
        selectionCB.setText(ElanLocale.getString("ExportDialog.Restrict"));
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
    }
    
    /**
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    private void setPreferredSetting() {  
    	extractTiers();// preferences for tiers
    	
    	Boolean boolPref =  Preferences.getBool(prefRootTiersOnly, null);    
    	if (boolPref == null) {
    		boolPref = Preferences.getBool("ExportPraatDialog.rootTiersCB", null);  
    		Preferences.set("ExportPraatDialog.rootTiersCB", null, null);  
    	} 
    	if(boolPref != null){
    		setRootTiersOnly(boolPref);
    	} 
     
    	boolPref =  Preferences.getBool(prefSelectionOnly, null);    
    	if (boolPref == null) {
    		boolPref = Preferences.getBool("ExportPraatDialog.selectionCB", null);  
    		Preferences.set("ExportPraatDialog.selectionCB", null, null);  
    	}       	
    	if (boolPref != null) {
    		selectionCB.setSelected(boolPref); 
    	}  
    	
    	boolPref =  Preferences.getBool(prefAddOffsetTime, null);    
    	if (boolPref == null) {
    		boolPref = Preferences.getBool("ExportPraatDialog.correctTimesCB", null);  
    		Preferences.set("ExportPraatDialog.correctTimesCB", null, null);  
    	}   
    	if (boolPref != null) {
    		correctTimesCB.setSelected(boolPref);
    	}
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferences(){
    	boolean rootsOnly = tierSelectPanel.isRootTiersOnly();
    	Preferences.set(prefRootTiersOnly, rootsOnly, null); 
    	Preferences.set(prefSelectionOnly, selectionCB.isSelected(), null);
    	Preferences.set(prefAddOffsetTime, correctTimesCB.isSelected(), null);
       	Preferences.set(prefSelectedTiers, getSelectedTiers(), transcription);  
       	Preferences.set(prefSelectTiersMode, getSelectionMode(), transcription);
    	// save the selected list in case on non-tier tab
    	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
    		Preferences.set(prefLastSelectedItems, getSelectedItems(), transcription);
    	}
    	
    	Preferences.set(prefHiddenTiers, getHiddenTiers(), transcription);
    	
    	List<String> tierOrder = getTierOrder();
    	Preferences.set(prefTierOrder, tierOrder, transcription);
    }
}
