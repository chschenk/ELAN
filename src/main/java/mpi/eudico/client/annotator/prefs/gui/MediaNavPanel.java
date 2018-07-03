package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import mpi.eudico.client.annotator.util.FileUtility;

/**
 * Panel showing option to change the media navigation setting: <br>
 * - frame forward/backward jumps to the begin of next/previous frame (default
 * it jumps with the amount of ms of the duration of a single frame)  - a
 * default location (directory) to search for media files (after the
 * "traditional" location)
 *
 * @author Han Sloetjes
 * @version 2.0, Dec 2008
 */
@SuppressWarnings("serial")
public class MediaNavPanel extends AbstractEditPrefsPanel implements PreferenceEditor, ChangeListener,
    ActionListener {
    private boolean origFrameStepToFrameBegin = false;
    private boolean origPre47FrameStepping = false;// grant access to previous frame stepping behaviour
    private String curMediaLocation = "-";
    private boolean origVideoSameSize = false;
    private boolean videoInCentre = false;
    private boolean origAltMediaLocSetsDirty = true;
    private String origTimeFormat = Constants.MS_STRING;    
    private boolean origPromptForFilename = true;
    private boolean origOnlyClipFirstMediaFile = false;
    private boolean origClipInParallel = true;
    /** 
     * Previous value to show in preferences panel.
     * Defaults to <code>true</code> if no preference was set.
     * See {@link ElanMediaPlayerController#haveSliders()} for default value in actual use.
     */
    private boolean origShowVolumeControls = true;
	private boolean origAutoPlayActivatedAnnotation = false;
	private boolean origAutoPlayKeyCreateAnnotation = false;
    private JCheckBox frameStepCB;
    private JCheckBox pre47FrameSteppingCB;
    private JCheckBox videosSameSizeCB;
    private JCheckBox videosInCentreCB;
    private JLabel setDirLabel;
    private JLabel curDirLabel;
    private JButton defaultDirButton;
    private JButton resetDirButton;
    private JCheckBox changedMediaLocCB;
    private JLabel timeFormatLabel;
    private JComboBox timeFormatComboBox;
    private JCheckBox promptForFilenameCB;
    private JCheckBox onlyClipFirstMediaFileCB;
    private JCheckBox clipInParallelCB; 
	private JCheckBox showVolumeControlsCB;
	private JCheckBox autoPlayActivatedAnnotationCB;
	private JCheckBox autoPlayKeyCreateAnnotationCB;
   
    private String HH_MM_SS_MS = ElanLocale.getString("TimeCodeFormat.Hours");
    private String SS_MS = ElanLocale.getString("TimeCodeFormat.Seconds");
    private String MS = ElanLocale.getString("TimeCodeFormat.MilliSec");   
    private String NTSC = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC");   
    private String PAL = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL");
    private String PAL_50 = ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50");
    
    private Map<String, String> tcMap;
    private int origTimeFormatIndex;
    
    /**
     * Creates a new MediaNavPanel instance
     */
    public MediaNavPanel() {
        super(ElanLocale.getString("PreferencesDialog.Category.Media"));
        tcMap = new HashMap<String, String>(5);
        tcMap.put(HH_MM_SS_MS, Constants.HHMMSSMS_STRING);
        tcMap.put(SS_MS, Constants.SSMS_STRING);
        tcMap.put(MS, Constants.MS_STRING);
        tcMap.put(NTSC, Constants.NTSC_STRING);
        tcMap.put(PAL, Constants.PAL_STRING);
        tcMap.put(PAL_50, Constants.PAL_50_STRING);
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Boolean boolPref = Preferences.getBool("MediaNavigation.FrameStepToFrameBegin",
                null);

        if (boolPref != null) {
            origFrameStepToFrameBegin = boolPref.booleanValue();
        }

        boolPref = Preferences.getBool("MediaNavigation.Pre47FrameStepping",
                null);
        if (boolPref != null) {
        	origPre47FrameStepping = boolPref.booleanValue();
        }
        
        String stringPref = Preferences.getString("DefaultMediaLocation", null);

        if (stringPref != null) {
            curMediaLocation = stringPref;
        }

        boolPref = Preferences.getBool("Media.VideosSameSize", null);

        if (boolPref != null) {
            origVideoSameSize = boolPref.booleanValue();
        }
        
        boolPref = Preferences.getBool("Media.VideosCentre", null);

        if (boolPref != null) {
            videoInCentre = boolPref.booleanValue();
        }
        
        boolPref = Preferences.getBool("MediaLocation.AltLocationSetsChanged", null);
        
        if (boolPref != null) {
        	origAltMediaLocSetsDirty = boolPref.booleanValue();
        }
        
        stringPref = Preferences.getString("CurrentTime.Copy.TimeFormat", null);
        
        if (stringPref != null) {
        	// take into account possible older localized stored preferences
        	if (tcMap.containsKey(stringPref)) {
        		origTimeFormat = tcMap.get(stringPref);
        	} else {
        		if (tcMap.values().contains(stringPref)) {
        			origTimeFormat = stringPref;
        		}
        		// if not, it might be string from yet another language  
        	}
        	//origTimeFormat should now be a non localized string
        }

        boolPref = Preferences.getBool("Media.PromptForFilename", null);

        if (boolPref != null) {
            origPromptForFilename = boolPref.booleanValue();
        }

        boolPref = Preferences.getBool("Media.OnlyClipFirstMediaFile", null); 

        if (boolPref != null) {
            origOnlyClipFirstMediaFile = boolPref.booleanValue();
        }

        boolPref = Preferences.getBool("Media.ClipInParallel", null); 

        if (boolPref != null) {
            origClipInParallel = boolPref.booleanValue();
        }

        boolPref = Preferences.getBool(ElanMediaPlayerController.HAVE_INDIVIDUAL_VOLUME_CONTROLS_PREF, null); 

        if (boolPref != null) {
        	origShowVolumeControls = boolPref.booleanValue();
        }

        boolPref = Preferences.getBool("Media.Autoplay.ActivateAnnotation", null); 

        if (boolPref != null) {
            origAutoPlayActivatedAnnotation = boolPref;
        }

        boolPref = Preferences.getBool("Media.Autoplay.KeyCreateAnnotation", null); 

        if (boolPref != null) {
            origAutoPlayKeyCreateAnnotation = boolPref;
        }
    }

    private void initComponents() {
    	GridBagConstraints gbc;
    	Font plainFont;
    	
    	// create panel for set directory
        setDirLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Media.DefaultLoc"));
        plainFont = setDirLabel.getFont().deriveFont(Font.PLAIN);
        setDirLabel.setFont(plainFont);
    
        JPanel dirPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;        
        gbc.insets = topInset;      
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;        
        dirPanel.add(setDirLabel, gbc);
        
        curDirLabel = new JLabel(curMediaLocation);
        curDirLabel.setFont(new Font(curDirLabel.getFont().getFontName(), Font.PLAIN, 10));
        gbc.gridy = 1;
        dirPanel.add(curDirLabel, gbc);
        
        defaultDirButton = new JButton(ElanLocale.getString("Button.Browse"));        
        gbc.gridy = 0;
        gbc.gridx = 1; 
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;    
        gbc.insets = leftInset;
        dirPanel.add(defaultDirButton, gbc);
        defaultDirButton.addActionListener(this);
        
        resetDirButton = new JButton();
        ImageIcon resetIcon = null;
        // add reset icon
        try {
            resetIcon = new ImageIcon(this.getClass()
                                          .getResource("/mpi/eudico/client/annotator/resources/Remove.gif"));
            resetDirButton.setIcon(resetIcon);
        } catch (Exception ex) {
            resetDirButton.setText("X");            
        }

        resetDirButton.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Reset"));
        resetDirButton.setPreferredSize(new Dimension(
                resetDirButton.getPreferredSize().width,
                defaultDirButton.getPreferredSize().height));
        gbc.gridx = 2;
        dirPanel.add(resetDirButton, gbc);       
        resetDirButton.addActionListener(this);
        
        //time format panel
        timeFormatLabel = new JLabel(ElanLocale.getString("PreferencesDialog.Media.TimeFormat"));
        timeFormatLabel.setFont(plainFont);
        
        timeFormatComboBox = new JComboBox();
        timeFormatComboBox.setFont(plainFont);
        timeFormatComboBox.addItem(HH_MM_SS_MS);   
        timeFormatComboBox.addItem(SS_MS);
        timeFormatComboBox.addItem(MS);    
        timeFormatComboBox.addItem(NTSC);    
        timeFormatComboBox.addItem(PAL);
        timeFormatComboBox.addItem(PAL_50);
        
        boolean prefRestored = false;
        Iterator<String> tcIt = tcMap.keySet().iterator();
        String key;
        String tcConst = null;
        while (tcIt.hasNext()) {
        	key = tcIt.next();
        	tcConst = tcMap.get(key);
        	if (tcConst.equals(origTimeFormat)) {
        		timeFormatComboBox.setSelectedItem(key);
        		prefRestored = true;
        		break;
        	}
        }
        if (!prefRestored) {
        	timeFormatComboBox.setSelectedItem(MS);
        }
        origTimeFormatIndex = timeFormatComboBox.getSelectedIndex();

        JPanel timeFormat = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;  
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.insets = topInset;
        timeFormat.add(timeFormatLabel, gbc);
    	
        gbc.gridx = 1;
        gbc.insets = globalInset;
        timeFormat.add(timeFormatComboBox, gbc);   
       
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.weightx = 1.0;
        timeFormat.add(new JPanel(), gbc);          
        
        // media panel layout  
        int gy = 0;
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;        
        gbc.insets = globalInset;
        gbc.gridy = gy++;
        outerPanel.add(timeFormat, gbc);        
        
        frameStepCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.MediaNav.FrameBegin"),
                origFrameStepToFrameBegin);
        frameStepCB.setFont(plainFont);

        pre47FrameSteppingCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.MediaNav.Pre47FrameStepping"), 
        		origPre47FrameStepping);
        pre47FrameSteppingCB.setFont(plainFont);
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString(
                    "PreferencesDialog.Category.MediaNav")), gbc);

        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(frameStepCB, gbc);
        
        gbc.gridy = gy++;
        outerPanel.add(pre47FrameSteppingCB, gbc);

        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString(
                    "PreferencesDialog.Media.VideoDisplay")), gbc);
       
        videosSameSizeCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.Media.VideoSize"), origVideoSameSize);
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        videosSameSizeCB.setFont(plainFont);
        outerPanel.add(videosSameSizeCB, gbc);
        
        videosInCentreCB = new JCheckBox(ElanLocale.getString(
        	"PreferencesDialog.Media.VideoCentre"), videoInCentre );
        //videosInCentreCB.addChangeListener(this);
        videosInCentreCB.addActionListener(this);
        gbc.gridy = gy++;        
        videosInCentreCB.setFont(plainFont);
        outerPanel.add(videosInCentreCB, gbc);
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Media.Location")),
            gbc);        
        
        gbc.gridy = gy++;
        gbc.insets = catPanelInset;
        outerPanel.add(dirPanel, gbc);      
        
        changedMediaLocCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.SaveAltLocation"));
        changedMediaLocCB.setFont(plainFont);
        changedMediaLocCB.setSelected(origAltMediaLocSetsDirty);

        gbc.gridy = gy++;  
        gbc.insets = globalInset;
        outerPanel.add(changedMediaLocCB, gbc); 
                
        gbc.gridy = gy++;
        gbc.insets = catInset;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        outerPanel.add(new JLabel(ElanLocale.getString(
                    "PreferencesDialog.Media.Clipping")), gbc); 
        
        promptForFilenameCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.PromptForFilename"), 
        	origPromptForFilename);
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        promptForFilenameCB.setFont(plainFont);
        outerPanel.add(promptForFilenameCB, gbc); 
       
        onlyClipFirstMediaFileCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.OnlyClipFirstMediaFile"), 
        	origOnlyClipFirstMediaFile);
        gbc.gridy = gy++;
        onlyClipFirstMediaFileCB.setFont(plainFont);
        outerPanel.add(onlyClipFirstMediaFileCB, gbc); 
       
        clipInParallelCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.ClipInParallel"),
        	origClipInParallel);
        gbc.gridy = gy++;
        clipInParallelCB.setFont(plainFont);
        outerPanel.add(clipInParallelCB, gbc); 
               
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Media.Controls")),
            gbc);        
        
        showVolumeControlsCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.PlayerVolumeControls"),
            	origShowVolumeControls);
        showVolumeControlsCB.addActionListener(this);
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        showVolumeControlsCB.setFont(plainFont);
        outerPanel.add(showVolumeControlsCB, gbc); 

        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Media.AutomaticallyPlayMedia")),
            gbc);        
        
        autoPlayActivatedAnnotationCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.AutoPlayWhenActivated"),
            	origAutoPlayActivatedAnnotation);
        autoPlayActivatedAnnotationCB.addActionListener(this);
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        autoPlayActivatedAnnotationCB.setFont(plainFont);
        outerPanel.add(autoPlayActivatedAnnotationCB, gbc); 
        
        autoPlayKeyCreateAnnotationCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.AutoPlayKeyCreate"),
            	origAutoPlayKeyCreateAnnotation);
        autoPlayKeyCreateAnnotationCB.addActionListener(this);
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        autoPlayKeyCreateAnnotationCB.setFont(plainFont);
        outerPanel.add(autoPlayKeyCreateAnnotationCB, gbc); 
        
        gbc.gridy = gy++;;        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        outerPanel.add(new JPanel(), gbc); // filler
    }

    /**
     * Returns the changed pref.
     *
     * @return a map with the changed pref, or null
     */
    @Override
    public Map<String, Object> getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(10);

            if (frameStepCB.isSelected() != origFrameStepToFrameBegin) {
                chMap.put("MediaNavigation.FrameStepToFrameBegin",
                	Boolean.valueOf(frameStepCB.isSelected()));
            }
            
            if (pre47FrameSteppingCB.isSelected() != origPre47FrameStepping) {
            	chMap.put("MediaNavigation.Pre47FrameStepping", 
            		Boolean.valueOf(pre47FrameSteppingCB.isSelected()));
            }

            if (videosSameSizeCB.isSelected() != origVideoSameSize) {
                chMap.put("Media.VideosSameSize",
                	Boolean.valueOf(videosSameSizeCB.isSelected()));
            }
            
            if (videosInCentreCB.isSelected() != videoInCentre) {
                chMap.put("Media.VideosCentre",
                	Boolean.valueOf(videosInCentreCB.isSelected()));
            }

            if ((curDirLabel.getText() != null) &&
                    !curDirLabel.getText().equals("-")) {
                chMap.put("DefaultMediaLocation", curDirLabel.getText());
            } else {
                chMap.put("DefaultMediaLocation", null);
            }
            
            if (changedMediaLocCB.isSelected() != origAltMediaLocSetsDirty) {
            	chMap.put("MediaLocation.AltLocationSetsChanged", 
            		Boolean.valueOf(changedMediaLocCB.isSelected()));
            }   
            
            //if(!timeFormatComboBox.getSelectedItem().toString().equals(origTimeFormat)){
            if (origTimeFormatIndex != timeFormatComboBox.getSelectedIndex()) {
            	chMap.put("CurrentTime.Copy.TimeFormat", 
            			tcMap.get(timeFormatComboBox.getSelectedItem()));
            }
            
            if (promptForFilenameCB.isSelected() != origPromptForFilename) {
                chMap.put("Media.PromptForFilename",
                	Boolean.valueOf(promptForFilenameCB.isSelected()));
            } 

            if (onlyClipFirstMediaFileCB.isSelected() != origOnlyClipFirstMediaFile) {
                chMap.put("Media.OnlyClipFirstMediaFile",
                	Boolean.valueOf(onlyClipFirstMediaFileCB.isSelected()));
            } 

            if (clipInParallelCB.isSelected() != origClipInParallel) {
                chMap.put("Media.ClipInParallel",
                	Boolean.valueOf(clipInParallelCB.isSelected()));
            } 

            if (showVolumeControlsCB.isSelected() != origShowVolumeControls) {
                chMap.put(ElanMediaPlayerController.HAVE_INDIVIDUAL_VOLUME_CONTROLS_PREF,
                	Boolean.valueOf(showVolumeControlsCB.isSelected()));
            } 

            if (autoPlayActivatedAnnotationCB.isSelected() != origAutoPlayActivatedAnnotation) {
                chMap.put("Media.Autoplay.ActivateAnnotation",
                	Boolean.valueOf(autoPlayActivatedAnnotationCB.isSelected()));
            } 

            if (autoPlayKeyCreateAnnotationCB.isSelected() != origAutoPlayKeyCreateAnnotation) {
                chMap.put("Media.Autoplay.KeyCreateAnnotation",
                	Boolean.valueOf(autoPlayKeyCreateAnnotationCB.isSelected()));
            } 

            return chMap;
        }

        return null;
    }

    /**
     * Returns whether anything has changed.
     *
     * @return whether anything has changed
     */
    @Override
    public boolean isChanged() {
        return ((frameStepCB.isSelected() != origFrameStepToFrameBegin) ||
        pre47FrameSteppingCB.isSelected() != origPre47FrameStepping ||
        !curMediaLocation.equals(curDirLabel.getText()) ||
        (videosSameSizeCB.isSelected() != origVideoSameSize) ||
        (videosInCentreCB.isSelected() != videoInCentre) ||
        (changedMediaLocCB.isSelected() != origAltMediaLocSetsDirty) || 
        //(!origTimeFormat.equals(timeFormatComboBox.getSelectedItem().toString()))) ||
        (origTimeFormatIndex != timeFormatComboBox.getSelectedIndex()) ||
        (promptForFilenameCB.isSelected() != origPromptForFilename) ||
        (onlyClipFirstMediaFileCB.isSelected() != origOnlyClipFirstMediaFile) ||
        (clipInParallelCB.isSelected() != origClipInParallel) ||
        (showVolumeControlsCB.isSelected() != origShowVolumeControls) ||
        (autoPlayActivatedAnnotationCB.isSelected() != origAutoPlayActivatedAnnotation) ||
        (autoPlayKeyCreateAnnotationCB.isSelected() != origAutoPlayKeyCreateAnnotation));
    }

    /**
     * Action event handling
     *
     * @param e the event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == defaultDirButton) {
            // show a folder file chooser, set the current def. location
            FileChooser chooser = new FileChooser(this);
            if (curMediaLocation.length() > 1) {
                File dir = new File(FileUtility.urlToAbsPath(curMediaLocation));

                if (dir.exists() && dir.isDirectory()) {
                	 chooser.setCurrentDirectory(dir.getAbsolutePath());
                }
            }
           chooser.createAndShowFileDialog(ElanLocale.getString("PreferencesDialog.Media.DefaultLoc"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		   null, null, true, null, FileChooser.DIRECTORIES_ONLY, null);
            //chooser.setMultiSelectionEnabled(false);
           
           File selFile = chooser.getSelectedFile();
           if (selFile != null) {
               curDirLabel.setText(selFile.getAbsolutePath());
               curDirLabel.setText(FileUtility.pathToURLString(
                       selFile.getAbsolutePath()));
           } 
            
        } else if (e.getSource() == resetDirButton) {
            curDirLabel.setText("-");
        } else if (e.getSource() == videosInCentreCB) {
        	Preferences.set("Media.VideosCentre.Temporary", videosInCentreCB.isSelected(), null);
        }
    }


    @Override
	public void stateChanged(ChangeEvent e) {// on Windows this is triggered by mouse hover etc. 
		if(e.getSource() == videosInCentreCB ){
			Preferences.set("Media.VideosCentre.Temporary", videosInCentreCB.isSelected(), null);	
		}		
	}
}
