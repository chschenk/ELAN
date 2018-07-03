package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;


/**
 * Panel showing options for:<br>
 * - flag to set that deselecting the edit box commits changes (default is cancel)<br>
 * - flag to set that Enter commits the changes (default is adding a new line
 * char)<br>
 * - flag to set that the selection should be cleared after creation of a new annotation,
 * or modifying the time alignment etc.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class EditingPanel extends AbstractEditPrefsPanel implements PreferenceEditor, ChangeListener {
	/** deselect commits preference, default is false */
    private JCheckBox deselectCB;

    /** enter commits (in addition to Ctrl + Enter), default false */
    private JCheckBox enterCommitsCB;
    /** clear the selection after creation or modification of a new annotation */
    private JCheckBox clearSelectionCB;
    private JCheckBox clearSelectionOnSingleClickCB;
    
    private JCheckBox createDependAnnCB;
    private JCheckBox snapAnnCB;
    private JLabel snapAnnLabel;
    private JCheckBox stickToFramesCB;
    private JTextField snapAnnTextField;
    private JCheckBox editInCenterCB;
    private JLabel copyOptionLabel;
    private JComboBox copyOptionComboBox;
    private JCheckBox useCopyCurrentTimeFormatCB;
	private JLabel languageLabel;
	private RecentLanguagesBox cvLanguageBox;
    
    private boolean origDeselectFlag = true;// May 2013 changed default to true //if this default value is changed- also change in TranscriptionTableEditBox  
    private boolean origEnterFlag = true; // May 2013 changed default to true
    private boolean origClearSelFlag = false;
    private boolean origClearSelOnSingleClickFlag = true; 
    private boolean oriCreateDependAnnFlag = false;
    
    private boolean oriSnapAnnotationsFlag = false;
    private boolean oriStickToFramesFlag = false;
    private long oriSnapValue = 100L;
    private long newSnapValue; // initialized on reading preferences
    private boolean oriAnnInCenterFlag = true;
     
    private static String TEXTANDTIME = ElanLocale.getString("PreferencesDialog.Edit.CopyAll");  
    private static String TEXT = ElanLocale.getString("PreferencesDialog.Edit.CopyTextOnly");
    private static String TEXT_CITE = ElanLocale.getString("PreferencesDialog.Edit.CopyCite");
    
    /* to be implemented later on
     * 
     * private static String URL = ElanLocale.getString("PreferencesDialog.Edit.CopyHyperlink");*/
    private Map<String, String> tcMap;
    // tcMod is to be a map with locale strings as key and English strings from Constants as values
    private int origCopyOptionIndex;
    private String oriCopyOption = Constants.TEXTANDTIME_STRING;
    private boolean origUseCopyCurrentTimeFormat = false;
    
	private String oriLanguageValue;
	private String newLanguageValue;
   
    /**
     * Creates a new EditingPanel instance
     */
      public EditingPanel( ) {
    	super(ElanLocale.getString("PreferencesDialog.Category.Edit"));
        tcMap = new HashMap<String, String>(4);
        tcMap.put(TEXTANDTIME, Constants.TEXTANDTIME_STRING);
        tcMap.put(TEXT, Constants.TEXT_STRING);
        tcMap.put(TEXT_CITE, Constants.CITE_STRING);
        /* to be implemented later on
         *
         * tcMap.put(URL, Constants.URL_STRING); to be implemented later on
         */
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Boolean boolPref = Preferences.getBool("InlineEdit.DeselectCommits", null);

        if (boolPref != null) {
            origDeselectFlag = boolPref.booleanValue();
        }

        boolPref = Preferences.getBool("InlineEdit.EnterCommits", null);

        if (boolPref != null) {
            origEnterFlag = boolPref.booleanValue();
        }
        
        boolPref = Preferences.getBool("ClearSelectionAfterCreation", null);
        
        if (boolPref != null) {
        	origClearSelFlag = boolPref.booleanValue();
        }
        
        boolPref = Preferences.getBool("ClearSelectionOnSingleClick", null); 
        
        if (boolPref != null) {
        	origClearSelOnSingleClickFlag = boolPref.booleanValue();
        }
        
        boolPref = Preferences.getBool("CreateDependingAnnotations", null);
        
        if (boolPref != null) {
        	oriCreateDependAnnFlag = boolPref.booleanValue();
        }
        
        boolPref = Preferences.getBool("SnapAnnotations", null);
        
        if (boolPref != null) {
        	oriSnapAnnotationsFlag = boolPref.booleanValue();
        }
        
        Long longPref = Preferences.getLong("SnapAnnotationsValue", null);
        
        if (longPref != null) {
        	oriSnapValue = longPref.longValue();
        }
        newSnapValue = oriSnapValue;
        
        String stringPref = Preferences.getString(Preferences.PREF_ML_LANGUAGE, null);
        
        if (stringPref != null) {
        	oriLanguageValue = stringPref;
        }
        newLanguageValue = oriLanguageValue;
                
        boolPref = Preferences.getBool("StickAnnotationsWithVideoFrames", null);
        
        if (boolPref != null) {
        	oriStickToFramesFlag = boolPref.booleanValue();
        }
        
        boolPref = Preferences.getBool("EditingPanel.ActiveAnnotationInCenter", null);
        
        if (boolPref != null) {
        	oriAnnInCenterFlag = boolPref.booleanValue();
        }
        
        stringPref = Preferences.getString("EditingPanel.CopyOption", null);
        
        if (stringPref != null) {
        	// take into account possible older localized stored preferences
        	if (tcMap.containsKey(stringPref)) {
        		oriCopyOption = tcMap.get(stringPref);
        	} else {
        		if (tcMap.values().contains(stringPref)) {
        			oriCopyOption = stringPref;
        		}
        		// if not, it might be string from yet another language
        	}
        	// oriCopyOption should now be a non localized string
        }
       
        boolPref = Preferences.getBool("CopyAnnotation.UseCopyCurrentTimeFormat", null);
        
        if (boolPref != null) {
        	origUseCopyCurrentTimeFormat = boolPref.booleanValue();
        }
    }  

    private void initComponents() {
        deselectCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.Edit.Deselect"), origDeselectFlag);
        enterCommitsCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.Edit.EnterCommits"), origEnterFlag);
        clearSelectionCB = new JCheckBox(ElanLocale.getString(
        		"PreferencesDialog.Edit.ClearSelection"), origClearSelFlag);
        clearSelectionOnSingleClickCB = new JCheckBox(ElanLocale.getString(
		"PreferencesDialog.Edit.ClearSelectionOnSingleClick"), origClearSelOnSingleClickFlag);
        createDependAnnCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.CreateDependAnn"),oriCreateDependAnnFlag);
        snapAnnCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.SnapAnnotations"),oriSnapAnnotationsFlag);
        snapAnnCB.addChangeListener(this);
        snapAnnLabel = new JLabel(ElanLocale.getString(
				"PreferencesDialog.Edit.SnapAnnotations.Label"));
        snapAnnTextField = new JTextField(Long.toString(oriSnapValue), 8);  
        snapAnnTextField.setEnabled(oriSnapAnnotationsFlag);
        stickToFramesCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.StickToVideoFrames"),oriStickToFramesFlag);
        editInCenterCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.ActiveAnnotationInCenter"), oriAnnInCenterFlag);
        languageLabel = new JLabel(ElanLocale.getString(
				"PreferencesDialog.Edit.DefaultCVLanguage.Label"));
        cvLanguageBox = new RecentLanguagesBox(oriLanguageValue);
        
        deselectCB.setFont(deselectCB.getFont().deriveFont(Font.PLAIN));
        enterCommitsCB.setFont(deselectCB.getFont());
        clearSelectionCB.setFont(deselectCB.getFont());
        clearSelectionOnSingleClickCB.setFont(deselectCB.getFont());
        createDependAnnCB.setFont(deselectCB.getFont());
        snapAnnCB.setFont(deselectCB.getFont());
        snapAnnLabel.setFont(deselectCB.getFont());
        snapAnnTextField.setFont(deselectCB.getFont());
        stickToFramesCB.setFont(deselectCB.getFont());
        editInCenterCB.setFont(deselectCB.getFont());
        copyOptionLabel = new JLabel(ElanLocale.getString(
				"PreferencesDialog.Edit.CopyOptionLabel"));
        copyOptionLabel.setFont(deselectCB.getFont());
	
        copyOptionComboBox = new JComboBox();
        copyOptionComboBox.addItem(TEXTANDTIME);        
        copyOptionComboBox.addItem(TEXT);
        copyOptionComboBox.addItem(TEXT_CITE);
        /* to be implemented later on
         * 
         * copyOptionComboBox.addItem(URL); */   
                
        boolean prefRestored = false;
        Iterator<String> tcIt = tcMap.keySet().iterator();
        String key;
        String tcConst = null;
        while (tcIt.hasNext()) {
        	key = tcIt.next();
        	tcConst = tcMap.get(key);
        	if (tcConst.equals(oriCopyOption)) {
        		copyOptionComboBox.setSelectedItem(key);
        		prefRestored = true;
        		break;
        	}
        }
        if (!prefRestored) {
        	copyOptionComboBox.setSelectedItem(TEXTANDTIME); // default
        }
        origCopyOptionIndex = copyOptionComboBox.getSelectedIndex();
        
        copyOptionComboBox.setSelectedItem(oriCopyOption);
        copyOptionComboBox.setFont(deselectCB.getFont());
        useCopyCurrentTimeFormatCB = new JCheckBox(String.format(ElanLocale.getString(
        		"PreferencesDialog.Edit.CopyTimeFormatLabel"), 
        		ElanLocale.getString("CommandActions.CopyCurrentTime")), origUseCopyCurrentTimeFormat);
        
        GridBagConstraints gbc = new GridBagConstraints();
	
        // create panel for max snap value entry		
        JPanel snapPanel = new JPanel(new GridBagLayout());	
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;  
        gbc.insets = topInset;
        snapPanel.add(snapAnnLabel, gbc);
    	
        gbc.gridx = 1;
        //gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.weightx = 1.0;
        gbc.insets = leftInset;
        snapPanel.add(snapAnnTextField, gbc);   
        
        //copy annotations panel
        JPanel copyPanel = new JPanel(new GridBagLayout());	
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;  
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.insets = topInset;
        copyPanel.add(copyOptionLabel, gbc);
    	
        gbc.gridx = 1;
        gbc.insets = leftInset;
        copyPanel.add(copyOptionComboBox, gbc);   
       
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.weightx = 1.0;
        copyPanel.add(new JPanel(), gbc);
        
        gbc. gridx = 0;
        gbc.gridy = 1;
        gbc.insets = globalPanelInset;
        gbc.gridwidth = 2;
        copyPanel.add(useCopyCurrentTimeFormatCB, gbc);
        
        // default controlled vocabulary language panel
        JPanel languagePanel  = new JPanel(new GridBagLayout());	
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;  
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.insets = topInset;
        languagePanel.add(languageLabel, gbc);

        gbc.gridx = 1;
        gbc.insets = leftInset;
        languagePanel.add(cvLanguageBox, gbc);   
       
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.weightx = 1.0;
        languagePanel.add(new JPanel(), gbc);         
        
        //editing options panel	    
        int gy = 0;
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.insets = globalInset;
        gbc.gridy = gy++;
        outerPanel.add(deselectCB, gbc);

        gbc.gridy = gy++;
        outerPanel.add(enterCommitsCB, gbc);
        
        gbc.gridy = gy++;
        outerPanel.add(clearSelectionCB, gbc);
        
        gbc.gridy = gy++;       
        outerPanel.add(clearSelectionOnSingleClickCB, gbc);
       
        gbc.gridy = gy++;       
        outerPanel.add(createDependAnnCB, gbc);
        
        gbc.gridy = gy++;
        outerPanel.add(stickToFramesCB, gbc);
        
        gbc.gridy = gy++;        
        outerPanel.add(snapAnnCB, gbc);       
        
        gbc.gridy = gy++;
        gbc.insets = singleTabInset;
        outerPanel.add(snapPanel, gbc);   
       
        gbc.gridy = gy++; 
        gbc.insets = globalInset;                 
        outerPanel.add(editInCenterCB, gbc);   

        gbc.gridy = gy++; 
        gbc.insets =  globalPanelInset;
        outerPanel.add(copyPanel, gbc);
        
        gbc.gridy = gy++; 
        gbc.insets =  globalPanelInset;
        outerPanel.add(languagePanel, gbc);       
        
        gbc.gridy = gy++;  
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        outerPanel.add(new JPanel(), gbc); 
    }
    
    /**
     * Returns the changed prefs.
     *
     * @return a map containing the changed preferences, key-value pairs, or null
     */
    @Override
	public Map getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(3);

            if (deselectCB.isSelected() != origDeselectFlag) {
                chMap.put("InlineEdit.DeselectCommits",
                	Boolean.valueOf(deselectCB.isSelected()));
            }

            if (enterCommitsCB.isSelected() != origEnterFlag) {
                chMap.put("InlineEdit.EnterCommits",
                	Boolean.valueOf(enterCommitsCB.isSelected()));
            }

            if (clearSelectionCB.isSelected() != origClearSelFlag) {
            	chMap.put("ClearSelectionAfterCreation", 
            		Boolean.valueOf(clearSelectionCB.isSelected()));
            }
            
            if (clearSelectionOnSingleClickCB.isSelected() != origClearSelOnSingleClickFlag) {
            	chMap.put("ClearSelectionOnSingleClick", 
            		Boolean.valueOf(clearSelectionOnSingleClickCB.isSelected()));
            }
            
            if (createDependAnnCB.isSelected() != oriCreateDependAnnFlag) {
            	chMap.put("CreateDependingAnnotations", 
            		Boolean.valueOf(createDependAnnCB.isSelected()));
            }
            
            if (snapAnnCB.isSelected() != oriSnapAnnotationsFlag) {
            	chMap.put("SnapAnnotations", 
            		Boolean.valueOf(snapAnnCB.isSelected()));
            }
            
            if (snapAnnCB.isSelected()){ 
            	chMap.put("SnapAnnotationsValue", Long.valueOf(newSnapValue));
            }
            
            if (! cvLanguageBox.getLongId().equals(oriLanguageValue)) {
            	newLanguageValue = cvLanguageBox.getLongId();
            	chMap.put(Preferences.PREF_ML_LANGUAGE, newLanguageValue);
            	// Tell the Transcriptions about it
            	Preferences.updateAllCVLanguages(newLanguageValue, false);
            }
            
            if (stickToFramesCB.isSelected() != oriStickToFramesFlag) {
            	chMap.put("StickAnnotationsWithVideoFrames", 
            		Boolean.valueOf(stickToFramesCB.isSelected()));
            }
           
            if (editInCenterCB.isSelected() != oriAnnInCenterFlag) {
            	chMap.put("EditingPanel.ActiveAnnotationInCenter", 
            		Boolean.valueOf(editInCenterCB.isSelected()));
            }
            
            String string = copyOptionComboBox.getSelectedItem().toString(), 
        	    nonLocaleString = Constants.TEXTANDTIME_STRING;

	    // Use the non-locale interpretation of the string in the comboBox. 
           
            nonLocaleString = tcMap.get(string);
                        
            if(!nonLocaleString.equals(oriCopyOption)){
            	chMap.put("EditingPanel.CopyOption", nonLocaleString);
            }
            
            if (!useCopyCurrentTimeFormatCB.isSelected() == origUseCopyCurrentTimeFormat) {
            	chMap.put("CopyAnnotation.UseCopyCurrentTimeFormat", 
            			Boolean.valueOf(useCopyCurrentTimeFormatCB.isSelected()));
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
        if (deselectCB.isSelected() != origDeselectFlag ||
                enterCommitsCB.isSelected() != origEnterFlag ||
                clearSelectionCB.isSelected() != origClearSelFlag||
                clearSelectionOnSingleClickCB.isSelected() != origClearSelOnSingleClickFlag|| 
                createDependAnnCB.isSelected() != oriCreateDependAnnFlag ||
                snapAnnCB.isSelected() != oriSnapAnnotationsFlag || 
                newSnapValue != oriSnapValue || 
                stickToFramesCB.isSelected() != oriStickToFramesFlag ||
                ! cvLanguageBox.getLongId().equals(oriLanguageValue) ||
                editInCenterCB.isSelected() != oriAnnInCenterFlag ||
                origCopyOptionIndex != copyOptionComboBox.getSelectedIndex() ||
                useCopyCurrentTimeFormatCB.isSelected() != origUseCopyCurrentTimeFormat){
            return true;
        }

        return false;
    }
    
    /**
     * Validates all the inputs in this panel
     * 
     * @return true, if all the inputs are valid, if not false;
     */
    public boolean validateInputs(){
    	if(snapAnnCB.isSelected()){
    		if (snapAnnTextField.getText() !=null){           	
        		try{
        			newSnapValue = Long.parseLong(snapAnnTextField.getText().trim());    
        		} catch (NumberFormatException e){        			
        			JOptionPane.showMessageDialog(this,
                            ElanLocale.getString("PreferencesDialog.Edit.InvalidSnapValue"),
                            ElanLocale.getString("Message.Warning"),
                            JOptionPane.WARNING_MESSAGE);
            		focusValue(snapAnnTextField);
            		return false;  			
        		}
        	}
    	}
    	return true;
    } 
    
    public void focusValue(JTextField tf){
    	tf.requestFocus();
    }

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == snapAnnCB){
			snapAnnTextField.setEnabled(snapAnnCB.isSelected());
			if(snapAnnCB.isSelected()){
				snapAnnTextField.requestFocus();
			}		
		}
	}  
}
