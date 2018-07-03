package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;


/**
 * Panel showing options for:<br>
 * - flag to set that deselecting the edit box commits changes (default is cancel)<br>
 * - flag to set that Enter commits the changes (default is adding a new line
 * char)<br>
 * - flag to set that the selection should be cleared after creation of a new annotation,
 * or modifying the time alignement etc.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CVPanel extends AbstractEditPrefsPanel implements PreferenceEditor, ChangeListener {
    
    private JCheckBox suggestEntryContainsCB;
    private JCheckBox suggestSearchDescCB;
    private JCheckBox suggestIgnoreCaseCB;
    
    private JCheckBox showDescriptionCB;
    
    private JTextField inlineWidthTF;
    private JTextField cvWidthPercentageTF;
    
    private int oriInlineBoxWidth = 0;
    private int oriCVWidthPercentage = 30;
    
    private boolean oriSuggestSearchMethodFlag = false;
    private boolean oriSuggestSearchInDescFlag = false;
    private boolean oriSuggestIgnoreCaseFlag = false;
    
    private boolean oriShowDescriptionFlag = true;
    
    private int newInlineBoxWidth; // initialized on reading preferences
    private int newCVWidthPercentage; // initialized on reading preferences   
	
   
    /**
     * Creates a new EditingPanel instance
     */
    public CVPanel() {
    	super(ElanLocale.getString("EditCVDialog.Label.CV"));
       
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Boolean boolPref = Preferences.getBool("SuggestPanel.EntryContains", null);
        
        if (boolPref != null) {
        	oriSuggestSearchMethodFlag = boolPref.booleanValue();
        }
        
        boolPref = Preferences.getBool("SuggestPanel.SearchDescription", null);
        
        if (boolPref != null) {
        	oriSuggestSearchInDescFlag = boolPref.booleanValue();
        }

        boolPref = Preferences.getBool("SuggestPanel.IgnoreCase", null);
        
        if (boolPref != null) {
        	oriSuggestIgnoreCaseFlag = boolPref.booleanValue();
        } 
        
        boolPref = Preferences.getBool("InlineEditBox.ShowCVDescription", null);
        
        if (boolPref != null) {
        	oriShowDescriptionFlag = boolPref.booleanValue();
        } 
        
        Integer intPref = Preferences.getInt("InlineEditBoxWidth", null);
        
        if (intPref != null) {
        	oriInlineBoxWidth = intPref.intValue();
        }
        newInlineBoxWidth = oriInlineBoxWidth;
        
        intPref = Preferences.getInt("InlineEditBoxCvWidthPercentage", null);
        
        if (intPref != null) {
        	oriCVWidthPercentage = intPref.intValue();
        }
        newCVWidthPercentage = oriCVWidthPercentage;
       
    }  

    private void initComponents() {   
        suggestEntryContainsCB = new JCheckBox(ElanLocale.getString(
        		"PreferencesDialog.Edit.SuggestEntryContains"), oriSuggestSearchMethodFlag);
        suggestSearchDescCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.SuggestSearchDesc"), oriSuggestSearchInDescFlag);
        suggestIgnoreCaseCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.SuggestIgnoreCase"), oriSuggestIgnoreCaseFlag);
        
        showDescriptionCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.CV.ShowCVDescripiton"), oriShowDescriptionFlag);
        
        showDescriptionCB.addChangeListener(this);
        
        inlineWidthTF = new JTextField("", 5);
        if(oriInlineBoxWidth > 0){
        	inlineWidthTF.setText(Long.toString(oriInlineBoxWidth));
        }
        cvWidthPercentageTF = new JTextField(Integer.toString(oriCVWidthPercentage), 5);
        
        
        suggestEntryContainsCB.setFont(suggestEntryContainsCB.getFont().deriveFont(Font.PLAIN));
        suggestSearchDescCB.setFont(suggestEntryContainsCB.getFont());
        suggestIgnoreCaseCB.setFont(suggestEntryContainsCB.getFont());  
        inlineWidthTF.setFont(suggestEntryContainsCB.getFont());
        cvWidthPercentageTF.setFont(suggestEntryContainsCB.getFont());
      
        
        GridBagConstraints gbc = new GridBagConstraints();       
        
        // default inline edit box panel
        JPanel inlineEditPanel  = new JPanel(new GridBagLayout());	
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE; 
        gbc.gridx = 0;
        gbc.gridy = 0;
        //gbc.insets = globalInset;// uncomment?
        //gbc.insets = leftInset;
        inlineEditPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Edit.InlineEditBoxWidth")), gbc);
        
        gbc.gridx = 1;
        //gbc.insets = leftInset;// uncomment?
        inlineEditPanel.add(inlineWidthTF, gbc);   
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        //gbc.insets = globalInset;// uncom
        //gbc.insets = leftInset;
        inlineEditPanel.add(showDescriptionCB, gbc);
       
        gbc.gridy = 2;    
        gbc.gridwidth = 1;
        gbc.insets = singleTabInset;
        inlineEditPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Edit.InlineEditBoxCVWidthPercentage")), gbc); 
        
        gbc.gridx = 1;
        gbc.insets = leftInset;
        inlineEditPanel.add(cvWidthPercentageTF, gbc);
        cvWidthPercentageTF.setEnabled(showDescriptionCB.isSelected());
        
        //editing options panel	    
        int gy = 0;
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.gridy = gy++; 
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Edit.InlineEditBox")), gbc);
        
        
        gbc.gridy = gy++; 
        gbc.insets =  globalPanelInset;// comment?
        gbc.fill = GridBagConstraints.NONE;
        outerPanel.add(inlineEditPanel, gbc);  
        
        gbc.gridy = gy++;  
        gbc.insets = catInset; 
        gbc.fill = GridBagConstraints.HORIZONTAL;   
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Edit.SuggestPanel")), gbc);

        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(suggestEntryContainsCB, gbc);
        
        gbc.gridy = gy++;
        outerPanel.add(suggestSearchDescCB, gbc);
        
        gbc.gridy = gy++;
        outerPanel.add(suggestIgnoreCaseCB, gbc);
        
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
	public Map<String, Object> getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(3);

            if (suggestEntryContainsCB.isSelected() != oriSuggestSearchMethodFlag) {
            	chMap.put("SuggestPanel.EntryContains", 
            		Boolean.valueOf(suggestEntryContainsCB.isSelected()));
            }
            if (suggestSearchDescCB.isSelected() != oriSuggestSearchInDescFlag) {
            	chMap.put("SuggestPanel.SearchDescription", 
            		Boolean.valueOf(suggestSearchDescCB.isSelected()));
            }
            if (suggestIgnoreCaseCB.isSelected() != oriSuggestIgnoreCaseFlag) {
            	chMap.put("SuggestPanel.IgnoreCase", 
            		Boolean.valueOf(suggestIgnoreCaseCB.isSelected()));
            }
            if (showDescriptionCB.isSelected() != oriShowDescriptionFlag) {
            	chMap.put("InlineEditBox.ShowCVDescription", 
            		Boolean.valueOf(showDescriptionCB.isSelected()));
            }
            
            chMap.put("InlineEditBoxWidth", newInlineBoxWidth);
            chMap.put("InlineEditBoxCvWidthPercentage", newCVWidthPercentage);
                     
            /* kj: instead of this, look at the index
             * language can be changed in the meantime
             */

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
        if (suggestEntryContainsCB.isSelected() != oriSuggestSearchMethodFlag ||
                suggestSearchDescCB.isSelected() != oriSuggestSearchInDescFlag ||
                suggestIgnoreCaseCB.isSelected() != oriSuggestIgnoreCaseFlag ||
                showDescriptionCB.isSelected() != oriShowDescriptionFlag ||
                newInlineBoxWidth != oriInlineBoxWidth ||
                newCVWidthPercentage != oriCVWidthPercentage 
                ) {
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
    	if (inlineWidthTF.getText() != null){          
    		String widthText = inlineWidthTF.getText().trim();
    		if (widthText.length() > 0) {
	    		try{
	        		newInlineBoxWidth = Integer.parseInt(inlineWidthTF.getText().trim()); 
	        	} catch (NumberFormatException e){        			
	        		JOptionPane.showMessageDialog(this,
	                       ElanLocale.getString("PreferencesDialog.Edit.InvalidInlineWidthValue"),
	                       ElanLocale.getString("Message.Warning"),
	                       JOptionPane.WARNING_MESSAGE);
		            	focusValue(inlineWidthTF);
	            	return false;  			    			
	        	}
        	} else {
        		newInlineBoxWidth = 0;
        	}
    	}
    	
    	if (showDescriptionCB.isSelected() && cvWidthPercentageTF.getText() != null){           	
    		try{
    			newCVWidthPercentage = Integer.parseInt(cvWidthPercentageTF.getText().trim()); 
    		} catch (NumberFormatException e){        			
    			JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("PreferencesDialog.Edit.InvalidInlineCVWidthValue"),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);
        		focusValue(cvWidthPercentageTF);
        		return false;  			    			
    		}
    	}
    	return true;
    } 
    	
    public void focusValue(JTextField tf){
    	tf.requestFocus();
    } 
    
    @Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == showDescriptionCB){
			cvWidthPercentageTF.setEnabled(showDescriptionCB.isSelected());
			if(showDescriptionCB.isSelected()){
				cvWidthPercentageTF.requestFocus();
			}		
		}
	}
}
