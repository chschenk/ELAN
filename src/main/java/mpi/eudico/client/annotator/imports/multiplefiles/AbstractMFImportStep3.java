package mpi.eudico.client.annotator.imports.multiplefiles;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * Step 3: Abstract Step pane to set the 
 * 'save as' settings for the output files. 
 * 
 * @author aarsom
 * @version May, 2012
 */
public abstract class AbstractMFImportStep3 extends StepPane implements ActionListener {	
	
	// preference strings
	protected String saveWithOriginalNames;
	protected String saveInOriginalFolder;
	protected String saveInRelativeFolder;
	protected String saveInRelativeFolderName;
	protected String saveInSameFolderName;
	
	//components
	protected JButton browseBtn;	
	protected JRadioButton  originalDirRB, togetherInSameDirRB, newDirectoryRB;
	protected JRadioButton originalFileNameRB, addSuffixRB;
	
	protected JTextField sameDirectoryTextField, localDirectoryTextField;

	protected JPanel fileNameOptionsPanel, directoryOptionsPanel;		
	protected Insets insets = new Insets(2, 4, 2, 4);		
	
	 
	protected JScrollPane outerScrollPane;
	protected String browseDirText = ElanLocale.getString("MultiFileImport.Step3.DefaultLocalDirName");	

	/**
	 * Constructor
	 * 
	 * @param multiStepPane
	 */
	public AbstractMFImportStep3(MultiStepPane multiStepPane) {
        super(multiStepPane);
        setPreferenceStrings();     
        initComponents();
    }
    
	/**
	 * Method to set the preference string which 
	 * will be used to restore the last used 
	 * setting.
	 */
    protected abstract void setPreferenceStrings();
    
	/**
	 * Initializes the ui components
	 */
    @Override
	protected void initComponents(){
    	initFileNameOptionsPanel();
    	initDirectoryOptionsPanel();    
    	
    	JPanel outerPanel = new JPanel();
	    outerPanel.setLayout(new GridBagLayout());
	   
	    outerScrollPane = new JScrollPane(outerPanel);
	    outerScrollPane.setBorder(null);
	    
	    setLayout(new GridBagLayout());
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = insets;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		outerPanel.add(fileNameOptionsPanel, gbc);		
	
		gbc.gridy = 1;
		outerPanel.add(directoryOptionsPanel, gbc);	
		
		gbc.gridy = 2;		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weighty = 1.0;
		outerPanel.add(new JPanel(), gbc);	
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
        add(outerScrollPane, gbc);
		    	
		//event handlers
		KeyListener keyListener = new TextFieldHandler();
		
		//event handlers for the radio buttons
		originalDirRB.addActionListener( this );
		newDirectoryRB.addActionListener( this );
		togetherInSameDirRB.addActionListener( this );
		originalFileNameRB.addActionListener( this );
		addSuffixRB.addActionListener( this );
		
		//event handlers for the buttons
		browseBtn.addActionListener( this );
		
		//event handlers for the text fields
		localDirectoryTextField.addKeyListener( keyListener );
		
		loadPreferences();
	}
    
    @Override
	public String getStepTitle(){
    	return ElanLocale.getString("MultiFileExportToolbox.Title.Step3Title");
    }
    
    /**
	 * Calls the next step
	 *
	 * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
	@Override
	public boolean doFinish() {
		savePreferences();
		multiPane.nextStep();		
		return false;
	}
	
	@Override
	public void enterStepForward(){	
		updateButtonStates();
	}	

	@Override
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	/**
     * Check and store properties, if all conditions are met.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
    	
    	multiPane.putStepProperty("UseOriginalDir", originalDirRB.isSelected());
    	multiPane.putStepProperty("NewDirectory", newDirectoryRB.isSelected());
    	multiPane.putStepProperty("NewDirName", localDirectoryTextField.getText().trim());
    	multiPane.putStepProperty("TogetherInSameDir", togetherInSameDirRB.isSelected());
    	multiPane.putStepProperty("SameDirectoryName", sameDirectoryTextField.getText());    
    	
    	multiPane.putStepProperty("UseOriginalFileName", originalFileNameRB.isSelected() );
    	multiPane.putStepProperty("UseOriginalFileNameWithSuffix", addSuffixRB.isSelected());  
    	
		return true;    	
    
    }
	
	/**
	 * Set the button states appropriately, according to constraints
	 */
	public void updateButtonStates(){
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);	
			
		if( togetherInSameDirRB.isSelected() && sameDirectoryTextField.getText().equals(browseDirText ) ){
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
			return;
		}		
			
		if( newDirectoryRB.isSelected() && localDirectoryTextField.getText().length() <= 0 ){
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
			return;
		}		
	}
    
    /*
	 * File name option panel
	 */
	protected void initFileNameOptionsPanel(){	        	
		fileNameOptionsPanel = new JPanel(new GridBagLayout());
		fileNameOptionsPanel.setBorder(new TitledBorder(
				ElanLocale.getString("ExportTiersDialog.Label.FileNameOptions")));					
		
		originalFileNameRB = new JRadioButton(ElanLocale.getString("MultiFileExport.SaveSettingsPane.RB.OriginalFileName"), true);
		addSuffixRB = new JRadioButton(ElanLocale.getString("MultiFileExport.SaveSettingsPane.RB.OriginalFileNameWithSuffix"));
		
		ButtonGroup fileNameBtnGroup = new ButtonGroup();
		fileNameBtnGroup.add(originalFileNameRB);
		fileNameBtnGroup.add(addSuffixRB);		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;				
		gbc.insets = insets;
		gbc.weightx = 1.0;
		fileNameOptionsPanel.add(originalFileNameRB, gbc);
		
		gbc.gridy = 1;		
		fileNameOptionsPanel.add(addSuffixRB, gbc);	

    }
		  
    /*
	 * Panel with directory options
	 */
	protected void initDirectoryOptionsPanel(){      
    	
		directoryOptionsPanel = new JPanel(new GridBagLayout());
		directoryOptionsPanel.setBorder(new TitledBorder(
				ElanLocale.getString("MultiFileExportToolbox.Label.SaveDirOptions")));
		
		originalDirRB = new JRadioButton(ElanLocale.getString("ExportTiersDialog.RadioButton.OriginalDirectory"), true);
		newDirectoryRB = new JRadioButton(ElanLocale.getString("ExportTiersDialog.RadioButton.NewDirectory"));
		togetherInSameDirRB = new JRadioButton(ElanLocale.getString("ExportTiersDialog.RadioButton.TogetherInSameDirectory"));
		
		ButtonGroup saveTierBtnGroup = new ButtonGroup();
		saveTierBtnGroup.add(originalDirRB);
		saveTierBtnGroup.add(togetherInSameDirRB);
		saveTierBtnGroup.add(newDirectoryRB);
		
		localDirectoryTextField = new JTextField(browseDirText);
		localDirectoryTextField.setEnabled(false);
		
		sameDirectoryTextField = new JTextField(ElanLocale.getString("ExportTiersDialog.TextField.DirectoryNameField"));
		sameDirectoryTextField.setEnabled(false);
		sameDirectoryTextField.setEditable(false);
		sameDirectoryTextField.setMinimumSize(new Dimension( 194, sameDirectoryTextField.getMinimumSize().height));
		
		browseBtn = new JButton(ElanLocale.getString("ExportTiersDialog.Button.Browse"));
		browseBtn.setEnabled(false);			
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.gridwidth = 3;		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;	
		gbc.insets = insets;
		directoryOptionsPanel.add(originalDirRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;		
		gbc.gridwidth = 1;				
		directoryOptionsPanel.add(newDirectoryRB, gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 2;		
		gbc.fill = GridBagConstraints.HORIZONTAL;		
		gbc.weightx = 1.0;
		directoryOptionsPanel.add(localDirectoryTextField, gbc);		
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;		
		gbc.weightx = 0.0;
		directoryOptionsPanel.add(togetherInSameDirRB, gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;		
		gbc.weightx = 1.0;
		directoryOptionsPanel.add(sameDirectoryTextField, gbc);			

		gbc.gridx = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		directoryOptionsPanel.add(browseBtn, gbc);	    
    }  
		
	/**
	 * Updates the state of the buttons
	 * depending on the selection
	 */
	protected void updateButtonsAndFields() {
		sameDirectoryTextField.setEnabled( togetherInSameDirRB.isSelected() );
		browseBtn.setEnabled( togetherInSameDirRB.isSelected() );		
		localDirectoryTextField.setEnabled(newDirectoryRB.isSelected());		
	}
    
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();     
        if( source == browseBtn ){
			String directoryStr = showDirectoryChooser();			
			if( directoryStr != null ){
				sameDirectoryTextField.setText( directoryStr );
			}			
		}
        
        updateButtonsAndFields();
		updateButtonStates();
    }
    
	/**
	 * shows a dialog that lets the user choose a directory
	 * @return string representing the selected directory name or null if no directory has been chosen
	 */
    protected String showDirectoryChooser(){      
		//create a directory chooser and set relevant attributes
        FileChooser dirChooser = new FileChooser(this);
        dirChooser.createAndShowFileDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.OK"), 
        		null, null, false, null, FileChooser.DIRECTORIES_ONLY, null);

        File selectedDir = dirChooser.getSelectedFile();
        if(selectedDir != null){
            return selectedDir.getAbsolutePath();
        }else{        	
        	return null;
        }
	}
	
    protected void savePreferences() {		
		if(saveWithOriginalNames != null)
			Preferences.set(saveWithOriginalNames, originalFileNameRB.isSelected(), null);		
		
		if(saveInOriginalFolder != null)
			Preferences.set(saveInOriginalFolder, originalDirRB.isSelected(), null);
		
		if(saveInRelativeFolder != null)
			Preferences.set(saveInRelativeFolder, newDirectoryRB.isSelected(), null);
		
		if(saveInRelativeFolderName != null){
			String relFolderName = localDirectoryTextField.getText();
			if ( !ElanLocale.getString("ExportTiersDialog.TextField.DefaultLocalDirectoryName").equals(relFolderName) ) {
				Preferences.set(saveInRelativeFolderName, relFolderName, null);
			}
		}
		
		if(saveInSameFolderName != null){
			String sameFolder = sameDirectoryTextField.getText();
			if ( !ElanLocale.getString("ExportTiersDialog.TextField.DirectoryNameField").equals(sameFolder) ) {
				Preferences.set(saveInSameFolderName, sameFolder, null);
			}
		}
	}
	
    protected void loadPreferences() {
		Boolean boolPref;
		String stringPref;
		
		if(saveWithOriginalNames != null){
			boolPref = Preferences.getBool(saveWithOriginalNames, null);
			if (boolPref != null) {
				originalFileNameRB.setSelected((Boolean)boolPref);
				addSuffixRB.setSelected(!boolPref);
			}
		}	
		
		if(saveInOriginalFolder != null){
			boolPref = Preferences.getBool(saveInOriginalFolder, null);
			if (boolPref != null) {
				boolean origFolder = (Boolean) boolPref;
				if (!origFolder) {
					boolPref = Preferences.getBool(saveInRelativeFolder, null);
					if (boolPref != null) {
						boolean relFolder = (Boolean) boolPref;
						if (relFolder) {
							newDirectoryRB.setSelected(true);
						} else {
							togetherInSameDirRB.setSelected(true);
						}
					}
				}
			}
		}
		
		if(saveInRelativeFolderName != null){
			stringPref = Preferences.getString(saveInRelativeFolderName, null);
			if (stringPref != null) {
				localDirectoryTextField.setText((String) stringPref);
			}
		}
		
		if(saveInSameFolderName != null){
			stringPref = Preferences.getString(saveInSameFolderName, null);
			if (stringPref != null) {
				sameDirectoryTextField.setText((String) stringPref);
			}
		}
		updateButtonsAndFields();
	}
	
    protected class TextFieldHandler implements KeyListener{
		@Override
		public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {
			updateButtonStates();
		}

		@Override
		public void keyTyped(KeyEvent e) {}
		
	}
}
