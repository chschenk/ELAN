package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.Transcription2QtSubtitle;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * DOCUMENT ME! $Id: ExportQtSubtitleDialog.java 44089 2015-07-27 12:41:08Z olasei $
 *
 * @author $Author: ericauer $
 * @version $Revision: 1.3 $
 */
@SuppressWarnings("serial")
public class ExportQtSubtitleDialog extends AbstractExtTierExportDialog
    implements  ChangeListener {
    private JCheckBox minimalDurCB;
    private JTextField minimalDurTF;
    private JCheckBox correctTimesCB;
    private JCheckBox mergeTiersCB;
    private JCheckBox recalculateTimesCB;
    
    private JButton fontSettingsButton;
    private Map<String, Object> fontSettingHashMap;
    private JCheckBox reuseSettingsCB;
    
    private boolean smilExport;
    
    // preferences keys
    
    final String prefSmilPrefix = "ExportQtSMILDialog";
    
    final String prefSubtitlePrefix = "ExportQtSubtitleDialog";
    
    String prefStringPrefix = prefSubtitlePrefix;
    
    /** pref key */
    final String prefSelectionOnly = prefStringPrefix + ".SelectionOnly";
    /** pref key */
    final String prefSelectedTiers = prefStringPrefix + ".selectedTiers";    
    /** pref key */
    final String prefTierOrder = prefStringPrefix + ".TierOrder";    
    /** pref key */
    final String prefSelectTiersMode = prefStringPrefix + ".SelectTiersMode";
    /** pref key */
    final String prefLastSelectedItems = prefStringPrefix + ".LastSelectedItems";
    /** pref key */
    final String prefHiddenTiers = prefStringPrefix + ".HiddenTiers";   
    /** pref key */
    final String prefAddOffsetTime = prefStringPrefix + ".AddOffsetTime";
    /** pref key */
    final String prefMergeTiers = prefStringPrefix + ".MergeTiers";
    /** pref key */
    final String prefRecalculateTime = prefStringPrefix + ".RecalculateTimeFromZero";
    /** pref key */
    final String prefMinDur = prefStringPrefix + ".MinimumDuration";
    /** pref key */
    final String prefMinDurValue = prefStringPrefix + ".MinimumDurationValue";   
    /** pref key */
    final String prefReuseLastDisplaySet = prefStringPrefix + ".ReuseLastCustomDisplaySettings"; 

    /**
     * DOCUMENT ME!
     *
     * @param parent
     * @param modal
     * @param transcription
     * @param selection
     */
    public ExportQtSubtitleDialog(Frame parent, boolean modal,
        TranscriptionImpl transcription, Selection selection) {
    	this(parent,modal,transcription, selection, false);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param parent
     * @param modal
     * @param transcription
     * @param selection
     * @param smilExport
     */
    public ExportQtSubtitleDialog(Frame parent, boolean modal,
        TranscriptionImpl transcription, Selection selection, boolean smilExport) {    	
    	super(parent, modal, transcription, selection);
    	this.smilExport = smilExport;  
    	if(smilExport){
    		prefStringPrefix = prefSmilPrefix;
    	}else{
    		prefStringPrefix = prefSubtitlePrefix;
    	}
        makeLayout();
        extractTiers();
        postInit();
        restrictCheckBox.requestFocus();        
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
    	
    	
    	List<String> stringsPref = Preferences.getListOfString(prefTierOrder, transcription);
    	if (stringsPref != null) {
    		setTierOrder(stringsPref);        	
        } else {
        	super.extractTiers(false);
        }
    	
    	stringsPref = Preferences.getListOfString(prefSelectedTiers, transcription);
        if (stringsPref != null) {
        	setSelectedTiers(stringsPref);
        }
       
        String stringPref = Preferences.getString(prefSelectTiersMode, transcription);
        if (stringPref != null) {
        	setSelectionMode(stringPref);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals(stringPref) ) {
            	// call this after! the mode has been set
        		List<String> selItems  = Preferences.getListOfString(prefLastSelectedItems, transcription);
            	
            	if (selItems != null) {
            		setSelectedItems(selItems);
            	}
        	}
         } 
    }

    /**
     * Initializes UI elements. Note: (for the time being) the checkbox column
     * indicating the selected state has been removed. It isn't that useful in
     * a single selection mode. The table could be replaced by a JList. When
     * the checkbox column would  be added again one of two things should
     * happen: either a custom tablecelleditor should be written for the
     * checkbox column or the valueChanged method should iterate over all rows
     * and uncheck all boxes, except the one for the selected roe.
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

        mergeTiersCB = new JCheckBox();
        mergeTiersCB.setSelected(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(mergeTiersCB, gridBagConstraints);        
        
        fontSettingsButton = new JButton();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(fontSettingsButton, gridBagConstraints);
        fontSettingsButton.addActionListener(this); 
        
        reuseSettingsCB = new JCheckBox();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(reuseSettingsCB, gridBagConstraints);
        
        minimalDurCB.addChangeListener(this);
        restrictCheckBox.addChangeListener(this);
        setPreferredSetting();

        updateLocale();
    }

    /**
     * @see mpi.eudico.client.annotator.export.AbstractExtTierExportDialog#startExport()
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
        File exportFile;
        if(smilExport){        	
            exportFile = promptForFile(ElanLocale.getString(
                    "ExportQtSmilDialog.Title"), null, FileExtension.SMIL_EXT, false);
            
            if (exportFile == null) {
                return false;
            }
        
        } else{
          List<String[]> extensions = new ArrayList<String[]>();
          extensions.add(FileExtension.TEXT_EXT);
          extensions.add(FileExtension.XML_EXT);
          exportFile = promptForFile(ElanLocale.getString(
                    "ExportQtSubtitleDialog.Title"), extensions, FileExtension.TEXT_EXT, false);
        }

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

        boolean merge = mergeTiersCB.isSelected();
        long mediaDur = ELANCommandFactory.getViewerManager(transcription)
                                          .getMasterMediaPlayer()
                                          .getMediaDuration();

        String[] tierNames = selectedTiers.toArray(new String[0]);
        long b = 0L;
        long e = Long.MAX_VALUE;

        if (restrictCheckBox.isSelected()) {
            b = selection.getBeginTime();
            e = selection.getEndTime();
        } 
        
        String mediaURL = "";        
        if ( transcription.getMediaDescriptors().size() > 0) {
            mediaURL = transcription.getMediaDescriptors().
            get(0).mediaURL;
            
        }
        
        if (fontSettingHashMap == null) {// the display settings dialog has not been used
        	if (reuseSettingsCB.isEnabled() && reuseSettingsCB.isSelected()) {
        		fontSettingHashMap = DisplaySettingsPane.getLastUsedSetting();
        	}
        }

        int vidWidth = -1;
        if (ELANCommandFactory.getViewerManager(transcription).getMasterMediaPlayer()
        		.getVisualComponent() != null) {
        	vidWidth = ELANCommandFactory.getViewerManager(transcription)
        			.getMasterMediaPlayer().getSourceWidth();
        }
        
        if (vidWidth > 0) {
        	if (fontSettingHashMap == null) {
        		fontSettingHashMap = new HashMap<String, Object>(1);
        	}
        	fontSettingHashMap.put("width", vidWidth);
        }
        
        int vidHeight = -1;
        if (ELANCommandFactory.getViewerManager(transcription).getMasterMediaPlayer()
        		.getVisualComponent() != null) {
        	vidHeight = ELANCommandFactory.getViewerManager(transcription)
        			.getMasterMediaPlayer().getSourceHeight();
        }
        
        if (vidHeight > 0) {
        	if (fontSettingHashMap == null) {// likely unnecessary check
        		fontSettingHashMap = new HashMap<String, Object>(1);
        	}
        	fontSettingHashMap.put("videoHeight", vidHeight);
        }
        
        if (!merge) {
             Transcription2QtSubtitle.exportTiers(transcription, tierNames,
                exportFile, b, e, offset, minimalDur, mediaDur, recalculateTimesCB.isSelected(), fontSettingHashMap);            
        } else {
            Transcription2QtSubtitle.exportTiersMerged(transcription,
                tierNames, exportFile, b, e, offset, minimalDur, mediaDur, recalculateTimesCB.isSelected(), fontSettingHashMap);
        }  
        
        // .sml output for Smil Quicktime Export
        if(smilExport){
        	
        	 String smilFile = exportFile.getAbsolutePath();
             int index = smilFile.lastIndexOf('.');
             if (index > 0) {
             	smilFile = smilFile.substring(0, index);
             }             
             smilFile += "." + FileExtension.SMIL_EXT[1];
             
             String mediaPath ="";
             if(mediaURL.length() > 0){ 
             	index = mediaURL.lastIndexOf("/");
             	mediaPath = mediaURL.substring(index+1);       	
             }
        
             try {	        
            	 if (selection != null && restrictCheckBox.isSelected()) {            		 
            		 ExportQtSmilDialog.export2SMILQt(	new File(transcription.getPathName()),
            					 new File(smilFile), tierNames,	mediaPath, b+offset, e+offset, recalculateTimesCB.isSelected(), merge, fontSettingHashMap);
            	 } else {
            		 ExportQtSmilDialog.export2SMILQt(	new File(transcription.getPathName()), 
						new File(smilFile), tierNames, mediaPath, mediaDur, merge, fontSettingHashMap);
            	 }
            	 return true;
             }
             catch (TransformerException te) {
            	 // this is ugly
            	 throw new IOException("TransformerException: " + te.getMessage());        	
             }
        	}
        
        	return true;
    }

    /**
     * @see mpi.eudico.client.annotator.export.AbstractTierExtExportDialog#updateLocale()
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        if(smilExport){
     	   setTitle(ElanLocale.getString("ExportQtSmilDialog.Title"));
            titleLabel.setText(ElanLocale.getString(
                    "ExportQtSmilDialog.TitleLabel"));   	   
     	   
        } else {
        	 setTitle(ElanLocale.getString("ExportQtSubtitleDialog.Title"));
             titleLabel.setText(ElanLocale.getString(
                     "ExportQtSubtitleDialog.TitleLabel"));
        }
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        minimalDurCB.setText(ElanLocale.getString(
                "ExportDialog.Label.MinimalDur"));
        mergeTiersCB.setText(ElanLocale.getString(
                "ExportQtSubtitleDialog.Label.Merge"));
        fontSettingsButton.setText(ElanLocale.getString(
        			"ExportQtSubtitleDialog.Button.FontSetting"));  
        recalculateTimesCB.setText(ElanLocale.getString("ExportDialog.RecalculateTimes"));
        reuseSettingsCB.setText(ElanLocale.getString("ExportQtSubtitleDialog.Button.ReuseSetting"));
    }
    

    /**
     * Enables/disables the minimal duration textfield and the recalculate time check box
     *
     * @param e the change event
     */
    @Override
	public void stateChanged(ChangeEvent e) {
        if (e.getSource() == minimalDurCB) {
            minimalDurTF.setEnabled(minimalDurCB.isSelected());
        } else if (e.getSource() == restrictCheckBox){
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
    		setNewFontSetting(DisplaySettingsPane.getNewFontSetting(this, ElanLocale.getString("DisplaySettingsPane.Title")));    		
    	}
    }
    
    
	protected void setNewFontSetting(Map<String, Object> newSetting){
		if (newSetting != null){
			//fontSettingHashMap = new HashMap(); //??
			fontSettingHashMap = newSetting;
		}
	}
    /**
     * 
     * Initializes the dialog with the last preferred/used settings 
     *
     */
    protected void setPreferredSetting()
    {
    	Boolean boolPref = Preferences.getBool(prefSelectionOnly, null);    	
    	if(boolPref == null){
    		boolPref = Preferences.getBool(prefStringPrefix+".restrictCheckBox", null); 
    		Preferences.set(prefStringPrefix+".restrictCheckBox", null, null);   
    	}
    	if(boolPref != null){
    		restrictCheckBox.setSelected((Boolean)boolPref); 
    	}	
     
    	boolPref = Preferences.getBool(prefMinDur, null);
    	if(boolPref == null){
    		boolPref = Preferences.getBool(prefStringPrefix+".minimalDurCB", null); 
    		Preferences.set(prefStringPrefix+".minimalDurCB", null, null);   
    	}
    	if(boolPref != null){
    		minimalDurCB.setSelected((Boolean)boolPref); 
    	}
     
    	boolPref = Preferences.getBool(prefAddOffsetTime, null);
    	if(boolPref == null){
    		boolPref = Preferences.getBool(prefStringPrefix+".correctTimesCB", null); 
    		Preferences.set(prefStringPrefix+".correctTimesCB", null, null);   
    	}
    	if(boolPref != null){
    		correctTimesCB.setSelected((Boolean)boolPref); 
    	}
     
    	boolPref = Preferences.getBool(prefMergeTiers, null);
    	if(boolPref == null){
    		boolPref = Preferences.getBool(prefStringPrefix+".mergeTiersCB", null); 
    		Preferences.set(prefStringPrefix+".mergeTiersCB", null, null);   
    	}
    	if(boolPref != null){
    		mergeTiersCB.setSelected((Boolean)boolPref); 
    	}
    	
    	String stringPref = Preferences.getString(prefMinDurValue, null);
    	if(stringPref == null){
    		stringPref = Preferences.getString(prefStringPrefix+".minimalDurTF", null); 
    		Preferences.set(prefStringPrefix+".minimalDurTF", null, null);   
    	}
    	if(stringPref != null){
    		minimalDurTF.setText(stringPref); 
    	}
    	
    	boolPref = Preferences.getBool(prefRecalculateTime, null);
    	if(boolPref == null){
    		boolPref = Preferences.getBool(prefStringPrefix+".recalculateTimesCB", null); 
    		Preferences.set(prefStringPrefix+".recalculateTimesCB", null, null);   
    	}
    	if(boolPref != null){
    		recalculateTimesCB.setSelected((Boolean)boolPref);    		
    	}
    	boolPref = Preferences.getBool(prefReuseLastDisplaySet, null);
    	if (boolPref instanceof Boolean) {
    		reuseSettingsCB.setSelected((Boolean) boolPref);
    	}
    	// check if there are previous settings, if not disable the check box
    	Object setMap = DisplaySettingsPane.getLastUsedSetting();
    	if (setMap == null) {
    		reuseSettingsCB.setEnabled(false);
    	}
    }
    
    /**
     * Saves the preferred/last used settings. 
     *
     */
    protected void savePreferences(){
    	Preferences.set(prefSelectionOnly, restrictCheckBox.isSelected(), null);    
    	Preferences.set(prefMinDur, minimalDurCB.isSelected(), null);
    	Preferences.set(prefAddOffsetTime, correctTimesCB.isSelected(), null);
    	Preferences.set(prefMergeTiers, mergeTiersCB.isSelected(), null);   
    	Preferences.set(prefRecalculateTime, recalculateTimesCB.isSelected(), null);
    	if (minimalDurTF.getText() != null){
    		Preferences.set(prefMinDurValue, minimalDurTF.getText(), null);    	
    	}
    	Preferences.set(prefSelectedTiers, getSelectedTiers(), transcription);    	
    	Preferences.set(prefSelectTiersMode, getSelectionMode(), transcription);
    	// save the selected list in case on non-tier tab
    	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
    		Preferences.set(prefLastSelectedItems, getSelectedItems(), transcription);
    	}
    	Preferences.set(prefHiddenTiers, getHiddenTiers(), transcription);
    	
    	List<String> tierOrder = getTierOrder();
    	Preferences.set(prefTierOrder, tierOrder, transcription);
    	/*
    	List<String> currentTierOrder = getCurrentTierOrder();    	    	
    	for(int i=0; i< currentTierOrder.size(); i++){
    		if(currentTierOrder.get(i) != tierOrder.get(i)){
    			Preferences.set(prefTierOrder, currentTierOrder, transcription);
    			break;
    		}
    	} 
    	*/
    	Preferences.set(prefReuseLastDisplaySet, reuseSettingsCB.isSelected(), null);
    }
}
