package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.Transcription2SubtitleText;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A dialog for subtitle text export, in formats like .srt etc. A minimal
 * duration for each subtitle can be specified.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ExportSubtitleDialog extends AbstractExtTierExportDialog
    implements ChangeListener {
    private JCheckBox minimalDurCB;
    private JTextField minimalDurTF;
    private JCheckBox correctTimesCB;
    private JCheckBox recalculateTimesCB;

    private JCheckBox overrideFrameRateCB;
    private JRadioButton ntscDFTimecodeRB;
    private JRadioButton ntscNDFTimecodeRB;
    private JRadioButton palTimecodeRB;
    
    /**
     * Constructor
     *
     * @param parent parent frame
     * @param modal the modal flag
     * @param transcription the source transcription
     * @param selection the selected time interval
     */
    public ExportSubtitleDialog(Frame parent, boolean modal,
        TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription, selection);
        makeLayout();
        extractTiers();
        postInit();
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
    	
    	List<String> stringsPref = Preferences.getListOfString("ExportSubtitleDialog.TierOrder", transcription);
    	if (stringsPref != null) {
    		setTierOrder(stringsPref);        	
        } else {
        	super.extractTiers(false);
        }
    	
        stringsPref = Preferences.getListOfString("ExportSubtitleDialog.selectedTiers", transcription);
    	if (stringsPref != null) {
        	setSelectedTiers((stringsPref));
         }
        
        String stringPref = Preferences.getString("ExportSubtitleDialog.SelectTiersMode", transcription);
        if (stringPref != null) {
        	//List list = (List) Preferences.get("ExportSubtitleDialog.HiddenTiers", transcription);
        	//setSelectedMode((String)useTyp, list);
        	setSelectionMode(stringPref);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals(stringPref) ) {
            	// call this after! the mode has been set
        		List<String> selItems  = Preferences.getListOfString("ExportSubtitleDialog.LastSelectedItems", transcription);
            	
            	if (selItems != null) {
            		setSelectedItems(selItems);
            	}
        	}
        }
    }

    /**
     * Configures the tier table and adds option elements: minimal duration
     * textfield.
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
        recalculateTimesCB.setEnabled(true);
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

        overrideFrameRateCB = new JCheckBox();
        overrideFrameRateCB.setSelected(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(overrideFrameRateCB, gridBagConstraints);
        
        JPanel timeCodePanel = new JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4,22,4,6);
        optionsPanel.add(timeCodePanel, gridBagConstraints);

        palTimecodeRB = new JRadioButton();
        palTimecodeRB.setEnabled(false);
        palTimecodeRB.setSelected(true);
        ntscDFTimecodeRB = new JRadioButton();
        ntscDFTimecodeRB.setEnabled(false);
        ntscNDFTimecodeRB = new JRadioButton();
        ntscNDFTimecodeRB.setEnabled(false);

        ButtonGroup group = new ButtonGroup();
        group.add(palTimecodeRB);
        group.add(ntscDFTimecodeRB);
        group.add(ntscNDFTimecodeRB);        
               
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        timeCodePanel.add(palTimecodeRB, gridBagConstraints);
   
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        timeCodePanel.add(ntscDFTimecodeRB, gridBagConstraints);
 
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        timeCodePanel.add(ntscNDFTimecodeRB, gridBagConstraints);

        minimalDurCB.addChangeListener(this);
        restrictCheckBox.addChangeListener(this);
        overrideFrameRateCB.addChangeListener(this);
        setPreferredSetting();
        updateLocale();
    }

    /**
     * Checks some fields, creates a file chooser and starts the export.
     *
     * @return true if export took place
     *
     * @throws IOException any io exception
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

        long offset = 0L;

        if (correctTimesCB.isSelected()) {
            List<MediaDescriptor> mediaDescriptors = transcription.getMediaDescriptors();

            if (mediaDescriptors.size() > 0) {
                offset = mediaDescriptors.get(0).timeOrigin;
            }
        }

        double frameRate = -1.0;
        if (overrideFrameRateCB.isSelected()) {
            if (palTimecodeRB.isSelected()) {
                frameRate = 25.0;
            } else if (ntscDFTimecodeRB.isSelected()) {
                frameRate = 29.97;
            } else if (ntscNDFTimecodeRB.isSelected()) {
                frameRate = 30.0;
            }
        }

        // prompt for file name and location
        File exportFile = promptForFile(ElanLocale.getString(
                    "ExportDialog.Subtitles.Title"), null,
                    FileExtension.SUBTITLE_EXT, true);

        if (exportFile == null) {
            return false;
        }

        Transcription2SubtitleText exporter = new Transcription2SubtitleText();
        String[] tierNames = selectedTiers.toArray(new String[0]);
        long b = 0L;
        long e = Long.MAX_VALUE;

        if (restrictCheckBox.isSelected()) {
        	b = selection.getBeginTime();
            e = selection.getEndTime();
        }

        if (exportFile.getName().toLowerCase().endsWith(".stl")) {
            exporter.exportTiersSTL(transcription, tierNames, exportFile,
                encoding, b, e, minimalDur, offset, frameRate, recalculateTimesCB.isSelected());
        }  else if (exportFile.getName().toLowerCase().endsWith(".lrc")) {
            exporter.exportTiersLRC(transcription, tierNames, exportFile,
                    encoding, b, e, minimalDur, offset, recalculateTimesCB.isSelected());
        } else if (exportFile.getName().toLowerCase().endsWith(".xml")) {//ttml, timed text
            exporter.exportTiersTTML(transcription, tierNames, exportFile,
                    encoding, b, e, minimalDur, offset, recalculateTimesCB.isSelected());
        } else {
            exporter.exportTiersSRT(transcription, tierNames, exportFile,
                encoding, b, e, minimalDur, offset, recalculateTimesCB.isSelected());
        }   

        return true;
    }

    /**
     * @see mpi.eudico.client.annotator.export.AbstractTierExportDialog#updateLocale()
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("ExportDialog.Subtitles.Title"));
        titleLabel.setText(ElanLocale.getString("ExportDialog.Subtitles.Title"));
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        recalculateTimesCB.setText(ElanLocale.getString("ExportDialog.RecalculateTimes"));
        minimalDurCB.setText(ElanLocale.getString(
                "ExportDialog.Label.MinimalDur"));

        overrideFrameRateCB.setText(ElanLocale.getString
                ("ExportDialog.OverrideTC"));
        palTimecodeRB.setText(ElanLocale.getString
                ("ExportDialog.OverrideTC.PAL"));
        ntscDFTimecodeRB.setText(ElanLocale.getString
                ("ExportDialog.OverrideTC.NTSCDF"));
        ntscNDFTimecodeRB.setText(ElanLocale.getString
                ("ExportDialog.OverrideTC.NTSCNDF"));
    }

    /**
     * Enables/disables the minimal duration textfield.
     *
     * @param e the change event
     */
    @Override
	public void stateChanged(ChangeEvent e) {
        if (e.getSource() == minimalDurCB) {
            minimalDurTF.setEnabled(minimalDurCB.isSelected());
        } else if (e.getSource() == overrideFrameRateCB) {
            ntscDFTimecodeRB.setEnabled(overrideFrameRateCB.isSelected());
            ntscNDFTimecodeRB.setEnabled(overrideFrameRateCB.isSelected());
            palTimecodeRB.setEnabled(overrideFrameRateCB.isSelected());
        } else if (e.getSource() == restrictCheckBox){
        	recalculateTimesCB.setEnabled(restrictCheckBox.isSelected());
        }
    }
    
    /**
     * Intializes the dialogBox with the last preferred/ used settings 
     *
     */
    private void setPreferredSetting()
    {
    	Boolean boolPref = Preferences.getBool("ExportSubtitleDialog.restrictCheckBox", null);
    
    	if (boolPref != null) {
    		restrictCheckBox.setSelected(boolPref); 
    	}	
     
    	boolPref = Preferences.getBool("ExportSubtitleDialog.minimalDurCB", null);
    	if (boolPref != null) {
    		minimalDurCB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportSubtitleDialog.correctTimesCB", null);
    	if (boolPref != null) {
    		correctTimesCB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportSubtitleDialog.overrideFrameRateCB", null);
    	if (boolPref != null) {
    		overrideFrameRateCB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportSubtitleDialog.ntscDFTimecodeRB", null);
    	if (boolPref != null) {
    		ntscDFTimecodeRB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportSubtitleDialog.ntscNDFTimecodeRB", null);
    	if (boolPref != null) {
    		ntscNDFTimecodeRB.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("ExportSubtitleDialog.palTimecodeRB", null);
    	if (boolPref != null) {
    		palTimecodeRB.setSelected(boolPref); 
    	}
    	
    	String stringPref = Preferences.getString("ExportSubtitleDialog.minimalDurTF", null);
    	if (stringPref != null) {
    		minimalDurTF.setText(stringPref); 
    	}
    	
    	boolPref = Preferences.getBool("ExportSubtitleDialog.recalculateTimesCB", null);
    	if (boolPref != null) {
    		recalculateTimesCB.setSelected(boolPref);    		
    	}
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferences(){
    	Preferences.set("ExportSubtitleDialog.restrictCheckBox", restrictCheckBox.isSelected(), null);    
    	Preferences.set("ExportSubtitleDialog.minimalDurCB", minimalDurCB.isSelected(), null);
    	Preferences.set("ExportSubtitleDialog.correctTimesCB", correctTimesCB.isSelected(), null);
    	Preferences.set("ExportSubtitleDialog.overrideFrameRateCB", overrideFrameRateCB.isSelected(), null);
    	Preferences.set("ExportSubtitleDialog.ntscDFTimecodeRB", ntscDFTimecodeRB.isSelected(), null);
    	Preferences.set("ExportSubtitleDialog.ntscNDFTimecodeRB", ntscNDFTimecodeRB.isSelected(), null);
    	Preferences.set("ExportSubtitleDialog.palTimecodeRB", palTimecodeRB.isSelected(), null);
    	Preferences.set("ExportSubtitleDialog.recalculateTimesCB", recalculateTimesCB.isSelected(), null);
    	if (minimalDurTF.getText() != null){
    		Preferences.set("ExportSubtitleDialog.minimalDurTF", minimalDurTF.getText(), null);
    	}
    	Preferences.set("ExportSubtitleDialog.selectedTiers", getSelectedTiers(), transcription);
    	
    	Preferences.set("ExportSubtitleDialog.SelectTiersMode", getSelectionMode(), transcription);
    	// save the selected list in case on non-tier tab
    	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
    		Preferences.set("ExportSubtitleDialog.LastSelectedItems", getSelectedItems(), transcription);
    	}
    	Preferences.set("ExportSubtitleDialog.HiddenTiers", getHiddenTiers(), transcription);
    	
    	List<String> tierOrder = getTierOrder();
    	Preferences.set("ExportSubtitleDialog.TierOrder", tierOrder, transcription);
    	/*
    	List currentTierOrder = getCurrentTierOrder();    	    	
    	for(int i=0; i< currentTierOrder.size(); i++){
    		if(currentTierOrder.get(i) != tierOrder.get(i)){
    			Preferences.set("ExportSubtitleDialog.TierOrder", currentTierOrder, transcription);
    			break;
    		}
    	} 
    	*/   	
    }
}
