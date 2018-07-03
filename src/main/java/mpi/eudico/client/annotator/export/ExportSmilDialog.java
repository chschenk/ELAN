package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.TransformerException;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.EAF2SMIL;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


@SuppressWarnings("serial")
public class ExportSmilDialog extends AbstractExtTierExportDialog 
	implements ChangeListener {
   
	private JCheckBox minimalDurCB;
    private JTextField minimalDurTF;
    private JCheckBox correctTimesCB;
    private JCheckBox recalculateTimesCB;
	
	private JButton fontSettingsButton;
	private Map<String, Object> fontSettingHashMap;
	
     /**
     * 
     * @param parent
     * @param modal
     * @param transcription
     * @param selection
     */
    public ExportSmilDialog (Frame parent, boolean modal,
        TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription, selection);
        this.makeLayout();
        extractTiers();
        postInit();
    }

    /**
     * Initializes UI elements.
     */
    @Override
	protected void makeLayout() {
        super.makeLayout();       
        // options
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(restrictCheckBox, gridBagConstraints);
        
        recalculateTimesCB = new JCheckBox();  
        recalculateTimesCB.setEnabled(false);
        gridBagConstraints.gridy = 1;     
        gridBagConstraints.insets = new Insets(4,22,4,6);
        optionsPanel.add(recalculateTimesCB, gridBagConstraints);

        correctTimesCB = new JCheckBox();
        correctTimesCB.setSelected(true);
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        optionsPanel.add(correctTimesCB, gridBagConstraints);

        minimalDurCB = new JCheckBox();
        minimalDurCB.setSelected(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(minimalDurCB, gridBagConstraints);
        
        minimalDurTF = new JTextField(6);
        minimalDurTF.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(minimalDurTF, gridBagConstraints);
        
        fontSettingsButton = new JButton();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(fontSettingsButton, gridBagConstraints);
        fontSettingsButton.addActionListener(this);
        
        restrictCheckBox.addChangeListener(this);
        minimalDurCB.addChangeListener(this);
        
        setPreferredSetting();
        updateLocale();
    }
    
    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
    	
    	List<String> stringsPref = Preferences.getListOfString("ExportSmilDialog.TierOrder", transcription);
    	if (stringsPref != null) {
    		setTierOrder(stringsPref);
        } else {
        	super.extractTiers(false);	
        }
    	
        stringsPref = Preferences.getListOfString("ExportSmilDialog.selectedTiers", transcription);
        if (stringsPref != null) {
        	setSelectedTiers(stringsPref);
        }
        
        String stringPref = Preferences.getString("ExportSmilDialog.SelectTiersMode", transcription);
        if (stringPref != null) {
//        	List list = (List) Preferences.get("ExportSmilDialog.HiddenTiers", transcription);
//        	setSelectedMode((String)useTyp, list);
        	setSelectionMode(stringPref);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals(stringPref) ) {
            	// call this after! the mode has been set
        		List<String> selItems  = Preferences.getListOfString("ExportSmilDialog.LastSelectedItems", transcription);
            	
            	if (selItems instanceof List) {
            		setSelectedItems(selItems);
            	}
        	}
         }
    }

    
    /**
     * @see mpi.eudico.client.annotator.export.AbstractTierExportDialog#updateLocale()
     */
    @Override
	protected void updateLocale() {
    		super.updateLocale();
        setTitle(ElanLocale.getString("ExportSmilDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportSmilDialog.TitleLabel"));
        fontSettingsButton.setText(ElanLocale.getString("ExportQtSubtitleDialog.Button.FontSetting"));
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        minimalDurCB.setText(ElanLocale.getString(
                "ExportDialog.Label.MinimalDur"));        
        recalculateTimesCB.setText(ElanLocale.getString("ExportDialog.RecalculateTimes"));
     }

    /**
     * @see mpi.eudico.client.annotator.export.AbstractTierExportDialog#startExport()
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
        
        int minimalDur = 0;

        if (minimalDurCB.isSelected()) {
            String dur = minimalDurTF.getText();

            if ((dur == null) || (dur.length() == 0)) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportDialog.Message.InvalidNumber"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

                minimalDurTF.requestFocus();

                return false;
            }

            try {
                minimalDur = Integer.parseInt(dur);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportDialog.Message.InvalidNumber"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

                minimalDurTF.requestFocus();

                return false;
            }
        }

 
        // prompt for file name and location
        File exportFile = promptForFile(ElanLocale.getString(
                "Export.TigerDialog.title"), null, FileExtension.SMIL_EXT, false);
        
        if (exportFile == null) {
            return false;
        }
        
        long offset = 0L;

        if (correctTimesCB.isSelected()) {
            List<MediaDescriptor> mediaDescriptors = transcription.getMediaDescriptors();

            if (mediaDescriptors.size() > 0) {
                offset = mediaDescriptors.get(0).timeOrigin;
            }
        }

        // export....
        String[] tierNames = selectedTiers.toArray(new String[] {  });
        String mediaURL = "";
        if ( transcription.getMediaDescriptors().size() > 0) {
            mediaURL = transcription.getMediaDescriptors().
            get(0).mediaURL;
        }
        try {	        
		    if (selection != null && restrictCheckBox.isSelected()) {
				EAF2SMIL.export2SMIL(
					 transcription,
					exportFile,
					tierNames,
					mediaURL,
					selection.getBeginTime(),
					selection.getEndTime(), offset,  minimalDur,recalculateTimesCB.isSelected(), fontSettingHashMap);
		    } else {
				EAF2SMIL.export2SMIL(
					new File(transcription.getPathName()), 
					exportFile, tierNames, mediaURL,offset,  minimalDur, fontSettingHashMap);
		    }
        } catch (TransformerException te) {
            // this is ugly
            throw new IOException("TransformerException: " + te.getMessage());
        }
        
        
        return true;
    }
       
    /**
     * Enables/disables the recalculate Time interval Check box
     *
     * @param e the change event
     */
    @Override
	public void stateChanged(ChangeEvent e) {
    	if (e.getSource() == minimalDurCB) {
            minimalDurTF.setEnabled(minimalDurCB.isSelected());
        }else if (e.getSource() == restrictCheckBox){
        	recalculateTimesCB.setEnabled(restrictCheckBox.isSelected());
        }
    }
    
    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
    	super.actionPerformed(ae);
    	if(ae.getSource() == fontSettingsButton){
    		this.setName("realPlayer");
    		setNewFontSetting(DisplaySettingsPane.getNewFontSetting(this, ElanLocale.getString("DisplaySettingsPane.Title"))); 
    		}
    	}
    
    
	private void setNewFontSetting(Map<String, Object> newSetting){
		if (newSetting != null){
			fontSettingHashMap = new HashMap<String, Object>(); 
			fontSettingHashMap = newSetting;
			}
	}
    
    /**
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    private void setPreferredSetting()
    {
    	Boolean boolPref = Preferences.getBool("ExportSmilDialog.restrictCheckBox", null);
    
    	if(boolPref != null){
    		restrictCheckBox.setSelected(boolPref); 
    	}	
    	
    	boolPref = Preferences.getBool("ExportSmilDialog.minimalDurCB", null);
    	if(boolPref != null){
    		minimalDurCB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportSmilDialog.correctTimesCB", null);
    	if(boolPref != null){
    		correctTimesCB.setSelected(boolPref); 
    	}  
    	
    	String stringPref = Preferences.getString("ExportSmilDialog.minimalDurTF", null);
    	if (stringPref != null){
    		minimalDurTF.setText(stringPref); 
    	}
    	
    	boolPref = Preferences.getBool("ExportSmilDialog.recalculateTimesCB", null);
    	if(boolPref != null){
    		recalculateTimesCB.setSelected(boolPref);    		
    	}

    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferences(){
    	Preferences.set("ExportSmilDialog.restrictCheckBox", restrictCheckBox.isSelected(), null);
    	Preferences.set("ExportSmilDialog.selectedTiers", getSelectedTiers(), transcription);
    	Preferences.set("ExportSmilDialog.minimalDurCB", minimalDurCB.isSelected(), null);
    	Preferences.set("ExportSmilDialog.correctTimesCB", correctTimesCB.isSelected(), null);    	
    	Preferences.set("ExportSmilDialog.recalculateTimesCB", recalculateTimesCB.isSelected(), null);
    	if (minimalDurTF.getText() != null){
    		Preferences.set("ExportSmilDialog.minimalDurTF", minimalDurTF.getText(), null);  
    	}
    	
    	Preferences.set("ExportSmilDialog.SelectTiersMode", getSelectionMode(), transcription);
    	// save the selected list in case on non-tier tab
    	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
    		Preferences.set("ExportSmilDialog.LastSelectedItems", getSelectedItems(), transcription);
    	}
    	Preferences.set("ExportSmilDialog.HiddenTiers", getHiddenTiers(), transcription);
    	
    	List<String> tierOrder = getTierOrder();
    	Preferences.set("ExportSmilDialog.TierOrder", tierOrder, transcription);
    	/*
    	List currentTierOrder = getCurrentTierOrder();    	    	
    	for(int i=0; i< currentTierOrder.size(); i++){
    		if(currentTierOrder.get(i) != tierOrder.get(i)){
    			Preferences.set("ExportSmilDialog.TierOrder", currentTierOrder, transcription);
    			break;
    		}
    	}    	
    	*/
    }
    
}
