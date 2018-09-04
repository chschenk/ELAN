package mpi.eudico.client.annotator.export;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;


/**
 * This dialog is used for exporting multiple eaf-files to new eaf-files
 * in which there the tiers to be exported can be chosen as well as the resulting
 * file names.
 * @author Jeffrey Lemein
 * @version March 2010
 */

@SuppressWarnings("serial")
public class ExportTiersDialog extends AbstractExtTierExportDialog{	
	
	private int progressValue;
	private boolean filesOpened = false;
	private boolean exportingStarted = false;	
	
	//utilities
	private ButtonGroup dependentTiersBtnGroup, saveTierBtnGroup, fileNameBtnGroup, suffixBtnGroup;
	
	//components
	private JButton browseBtn;
	private JCheckBox dontExportFilesWithoutTiersCB;
	private JLabel dependentTiersLabel, directoryOptionsLabel, resultingFilenameLabel, otherOptionLabel;
	private JRadioButton includeParentTiersRB, neglectDependentTiersRB, originalDirRB, togetherInSameDirRB, newDirectoryRB;
	private JRadioButton originalFileNameRB, addSuffixRB, originalFileNameWithSuffixRB, useBaseNameWithSuffixRB;	
	private JTextField sameDirectoryTextField, localDirectoryTextField, suffixNameTextField, baseNameTextField;
	private JProgressBar progressBar;
	private JPanel dependentTiersOptionPanel, fileNameOptionsPanel, directoryOptionsPanel, otherOptionsPanel;
	private String browseDirText;

	/**
     * Constructor for multiple files.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param files a list of eaf files
     * @param mode the mode, WORDS or ANNOTATION export
     */
    public ExportTiersDialog(Frame parent, boolean modal, List<File> files) {
        super(parent, modal, files);
        this.files = files;  
        initializeComponents();
        makeLayout();
        loadPreferences();
        extractTiersFromFiles();
        
        postInit();
    }
    
    @Override
	protected void extractTiersFromFiles(){
    	super.extractTiersFromFiles();
    	filesOpened = true;
    	updateExportButtonEnabled();    	
    }

    
    private void  initializeComponents(){    	
		/*
		 * Panel with dependent tier options
		 */
		dependentTiersOptionPanel = new JPanel();
		dependentTiersLabel = new JLabel();
		includeParentTiersRB = new JRadioButton("", true);
		neglectDependentTiersRB = new JRadioButton();
		dependentTiersBtnGroup = new ButtonGroup();
		dependentTiersBtnGroup.add(includeParentTiersRB);
		dependentTiersBtnGroup.add(neglectDependentTiersRB);
		/* */
		
		/*
		 * Panel with file name options
		 */
		fileNameOptionsPanel = new JPanel();
		
		resultingFilenameLabel = new JLabel();
		
		originalFileNameRB = new JRadioButton("", true);
		addSuffixRB = new JRadioButton();
		fileNameBtnGroup = new ButtonGroup();
		fileNameBtnGroup.add(originalFileNameRB);
		fileNameBtnGroup.add(addSuffixRB);
		
		suffixBtnGroup = new ButtonGroup();
		originalFileNameWithSuffixRB = new JRadioButton("", true);
		originalFileNameWithSuffixRB.setEnabled(false);
		suffixNameTextField = new JTextField();
		suffixNameTextField.setEnabled(false);
		
		useBaseNameWithSuffixRB = new JRadioButton();
		useBaseNameWithSuffixRB.setEnabled(false);
		baseNameTextField = new JTextField();
		baseNameTextField.setEnabled(false);		
		
		suffixBtnGroup.add(originalFileNameWithSuffixRB);
		suffixBtnGroup.add(useBaseNameWithSuffixRB);
		/* */
		
		/*
		 * Panel with directory options
		 */
		directoryOptionsPanel = new JPanel();
		
		directoryOptionsLabel = new JLabel();
		
		originalDirRB = new JRadioButton("", true);
		newDirectoryRB = new JRadioButton();
		togetherInSameDirRB = new JRadioButton();
		
		saveTierBtnGroup = new ButtonGroup();
		saveTierBtnGroup.add(originalDirRB);
		saveTierBtnGroup.add(togetherInSameDirRB);
		saveTierBtnGroup.add(newDirectoryRB);
		
		localDirectoryTextField = new JTextField();
		localDirectoryTextField.setEnabled(false);
		
		sameDirectoryTextField = new JTextField();
		sameDirectoryTextField.setEnabled(false);
		sameDirectoryTextField.setEditable(false);
		
		browseBtn = new JButton();
		browseBtn.setEnabled(false);
		/* */
		
		/*
		 * Additional Options Label
		 */
		otherOptionsPanel = new JPanel();
		
		otherOptionLabel = new JLabel();
		
		dontExportFilesWithoutTiersCB = new JCheckBox();
		/* */
		
		//Progress bar
		progressBar = new JProgressBar(0, files.size());
		progressBar.setValue(progressValue);
		progressBar.setStringPainted(true);
		progressValue = 0;				
		
		//event handlers
		ActionListener radioBtnListener = new RadioButtonHandler();
		KeyListener keyListener = new TextFieldHandler();
		
		//event handlers for the radio buttons
		originalDirRB.addActionListener( radioBtnListener );
		newDirectoryRB.addActionListener( radioBtnListener );
		togetherInSameDirRB.addActionListener( radioBtnListener );
		originalFileNameRB.addActionListener( radioBtnListener );
		addSuffixRB.addActionListener( radioBtnListener );
		originalFileNameWithSuffixRB.addActionListener( radioBtnListener );
		useBaseNameWithSuffixRB.addActionListener( radioBtnListener );
		
		//event handlers for the buttons
		browseBtn.addActionListener( this );
		
		//event handlers for the text fields
		suffixNameTextField.addKeyListener( keyListener );
		baseNameTextField.addKeyListener( keyListener );
		localDirectoryTextField.addKeyListener( keyListener );
	}
    
    @Override
	protected void makeLayout(){
    	super.makeLayout();
    	
		Insets singleTabInset = new Insets(4, 15, 4, 6);    	
		/*
		 * Dependent Tiers options panel
		 */
		dependentTiersOptionPanel.setLayout(new GridBagLayout());		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;	
		gbc.weightx = 1.0;
		dependentTiersOptionPanel.add(dependentTiersLabel, gbc);
				
		gbc.gridy = 1;
		gbc.insets = insets;
		dependentTiersOptionPanel.add(includeParentTiersRB, gbc);
				
		gbc.gridy = 2;
		dependentTiersOptionPanel.add(neglectDependentTiersRB, gbc);
				
		/*
		 * File name option panel
		 */
		fileNameOptionsPanel.setLayout( new GridBagLayout() );
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;		
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		fileNameOptionsPanel.add(resultingFilenameLabel, gbc);
		
		gbc.gridy = 1;		
		gbc.insets = insets;
		fileNameOptionsPanel.add(originalFileNameRB, gbc);
		
		gbc.gridy = 2;		
		fileNameOptionsPanel.add(addSuffixRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.insets = singleTabInset;
		fileNameOptionsPanel.add(originalFileNameWithSuffixRB, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		fileNameOptionsPanel.add(suffixNameTextField, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.insets = singleTabInset;
		gbc.fill = GridBagConstraints.NONE;
		fileNameOptionsPanel.add(useBaseNameWithSuffixRB, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		fileNameOptionsPanel.add(baseNameTextField, gbc);
		/* */
		
		/*
		 * Directory Options Panel
		 */
		directoryOptionsPanel.setLayout( new GridBagLayout() );
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.gridwidth = 3;		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;	
		gbc.weightx = 1.0;
		directoryOptionsPanel.add(directoryOptionsLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = insets;
		directoryOptionsPanel.add(originalDirRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;		
		gbc.gridwidth = 1;				
		directoryOptionsPanel.add(newDirectoryRB, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;		
		gbc.fill = GridBagConstraints.HORIZONTAL;		
		directoryOptionsPanel.add(localDirectoryTextField, gbc);		
		
		
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;		
		directoryOptionsPanel.add(togetherInSameDirRB, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		sameDirectoryTextField.setMinimumSize(new Dimension( 194, sameDirectoryTextField.getMinimumSize().height));
		directoryOptionsPanel.add(sameDirectoryTextField, gbc);		
		

		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		directoryOptionsPanel.add(browseBtn, gbc);	
		
		/* */
		
		/*
		 * Other options panel
		 */
		otherOptionsPanel.setLayout( new GridBagLayout() );
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;		
		gbc.weightx = 1.0;		
		otherOptionsPanel.add(otherOptionLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = insets;
		otherOptionsPanel.add(dontExportFilesWithoutTiersCB, gbc);
		/* */
		
		//Add all panels to the option panel	
         gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 0; 
         gbc.anchor = GridBagConstraints.WEST;
         gbc.fill = GridBagConstraints.NONE;
         gbc.weightx = 1.0;		
         gbc.insets = insets;
         optionsPanel.add(dependentTiersOptionPanel, gbc);       

         gbc.gridy = 1;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         optionsPanel.add(fileNameOptionsPanel, gbc);        

         gbc.gridy = 2;         
         optionsPanel.add(directoryOptionsPanel, gbc);

         gbc.gridy = 3;    
         gbc.fill = GridBagConstraints.NONE;
         optionsPanel.add(otherOptionsPanel, gbc);
		
         gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 3;
         gbc.insets = new Insets(10, 10, 10, 10);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.anchor = GridBagConstraints.LAST_LINE_START;
         getContentPane().add( progressBar, gbc );
		
         updateLocale();
    }
    
	/**
	 * Method that updates the export button according to the state of selected options and opening of files
	 */
	private void updateExportButtonEnabled(){
		if( !filesOpened ){		
			setStartButtonEnabled(false);
			return;
		}
		
		boolean b = true;
		
		if( togetherInSameDirRB.isSelected() && sameDirectoryTextField.getText().equals(browseDirText) ) {
			b = false;
		}
		
		if( newDirectoryRB.isSelected() && localDirectoryTextField.getText().length() <= 0 ) {
			b = false;
		}
		
		if( addSuffixRB.isSelected() ){
			if( originalFileNameWithSuffixRB.isSelected() && suffixNameTextField.getText().length() <= 0 ) {
				b = false;
			} else if( useBaseNameWithSuffixRB.isSelected() && baseNameTextField.getText().length() <= 0 ) {
				b = false;
			}
		}
		
		setStartButtonEnabled(b && !exportingStarted);
	}
	
	private void updateButtonsAndFields() {
		sameDirectoryTextField.setEnabled( togetherInSameDirRB.isSelected() );
		browseBtn.setEnabled( togetherInSameDirRB.isSelected() );
		
		localDirectoryTextField.setEnabled(newDirectoryRB.isSelected());
		baseNameTextField.setEnabled( addSuffixRB.isSelected() && useBaseNameWithSuffixRB.isSelected());
		suffixNameTextField.setEnabled( addSuffixRB.isSelected() && originalFileNameWithSuffixRB.isSelected() );
		useBaseNameWithSuffixRB.setEnabled( addSuffixRB.isSelected() );
		originalFileNameWithSuffixRB.setEnabled( addSuffixRB.isSelected() );
	}
    
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();     
        if( source == browseBtn ){
			String directoryStr = showDirectoryChooser();			
			if( directoryStr != null ){
				sameDirectoryTextField.setText( directoryStr );
			}			
		} else {
			super.actionPerformed(ae);
		}
    }
    
	/**
	 * shows a dialog that lets the user choose a directory
	 * @return string representing the selected directory name or null if no directory has been chosen
	 */
	private String showDirectoryChooser(){
      
		//create a directory chooser and set relevant attributes
        FileChooser dirChooser = new FileChooser(this);
        dirChooser.createAndShowFileDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.OK"), 
        		null, null, false, "LastUsedEAFDir", FileChooser.DIRECTORIES_ONLY, null);

        File selectedDir = dirChooser.getSelectedFile();
        if(selectedDir != null){
            return selectedDir.getAbsolutePath();
        }else{        	
        	return null;
        }
	}
	
	/**
	 * Helper function to check if a given string exists in the list of strings
	 * @param string particular string that needs to be tested if it occurs in the list
	 * @param stringList the list of strings
	 * @return
	 */
	private boolean exists( String string, List<String> stringList ){
		return stringList.indexOf(string) >= 0;
	}
	
	/**
	 * Updates the text and captions of the components
	 */
	@Override
	protected void updateLocale(){
		super.updateLocale();
		setTitle( ElanLocale.getString("ExportTiersDialog.Title") );	
		titleLabel.setText(ElanLocale.getString("ExportTiersDialog.Label.Description"));
		this.neglectDependentTiersRB.setText( ElanLocale.getString("ExportTiersDialog.RadioButton.NeglectDependentTiers") );
		this.includeParentTiersRB.setText( ElanLocale.getString("ExportTiersDialog.RadioButton.IncludeParentTiersRB") );		
		
		this.originalFileNameRB.setText( ElanLocale.getString("ExportTiersDialog.RadioButton.OriginalFileName"));
		this.originalFileNameWithSuffixRB.setText( ElanLocale.getString("ExportTiersDialog.RadioButton.OriginalFileNameWithSuffix"));
		this.useBaseNameWithSuffixRB.setText( ElanLocale.getString("ExportTiersDialog.RadioButton.NewBaseNameWithSuffix"));
		
		this.originalDirRB.setText( ElanLocale.getString("ExportTiersDialog.RadioButton.OriginalDirectory"));
		this.newDirectoryRB.setText( ElanLocale.getString("ExportTiersDialog.RadioButton.NewDirectory"));
		this.togetherInSameDirRB.setText( ElanLocale.getString("ExportTiersDialog.RadioButton.TogetherInSameDirectory"));
		
		this.browseBtn.setText( ElanLocale.getString("ExportTiersDialog.Button.Browse") );
		
		this.baseNameTextField.setText( ElanLocale.getString("ExportTiersDialog.TextField.DefaultBaseName") );
		this.suffixNameTextField.setText( ElanLocale.getString("ExportTiersDialog.TextField.DefaultSuffixName") );
		this.localDirectoryTextField.setText( ElanLocale.getString("ExportTiersDialog.TextField.DefaultLocalDirectoryName") );
		browseDirText = ElanLocale.getString("ExportTiersDialog.TextField.DirectoryNameField");
		this.sameDirectoryTextField.setText( browseDirText );
		
		this.dependentTiersLabel.setText( ElanLocale.getString("ExportTiersDialog.Label.DependentTierOptions"));
		this.directoryOptionsLabel.setText( ElanLocale.getString("ExportTiersDialog.Label.SaveTiersOptions") );
		this.resultingFilenameLabel.setText( ElanLocale.getString("ExportTiersDialog.Label.FileNameOptions") );
		this.otherOptionLabel.setText( ElanLocale.getString("ExportTiersDialog.Label.OtherOptions") );

		
		this.dontExportFilesWithoutTiersCB.setText( ElanLocale.getString("ExportTiersDialog.CheckBox.ExportFilesWithoutTiers"));
		this.addSuffixRB.setText( ElanLocale.getString("ExportTiersDialog.RadioButton.addSuffix"));
	}

	@Override
	protected boolean startExport() throws IOException {
		progressValue = 0;
		progressBar.setValue(progressValue);
		
		savePreferences();
		Thread t = new SaveFileThread(getSelectedTiers());
		t.start();
		
		exportingStarted = true;	
        updateExportButtonEnabled();
		return false;
	}
	
	private void savePreferences() {
		Preferences.set("ExportTiersDialog.rootTiersCB", isRootTiersOnly(), null);
		Preferences.set("ExportTiersDialog.SelectTiersMode", getSelectionMode(), null);    
		Preferences.set("ExportTiersDialog.forceParentExport", includeParentTiersRB.isSelected(), null);
		Preferences.set("ExportTiersDialog.saveWithOriginalNames", originalFileNameRB.isSelected(), null);
		Preferences.set("ExportTiersDialog.saveWithSuffix", originalFileNameWithSuffixRB.isSelected(), null);
		String suffixText = suffixNameTextField.getText();// check length of string?
		if ( !ElanLocale.getString("ExportTiersDialog.TextField.DefaultSuffixName").equals(suffixText) ) {
			Preferences.set("ExportTiersDialog.fileNameSuffix", suffixText, null);
		}
		String baseName = baseNameTextField.getText();
		if ( !ElanLocale.getString("ExportTiersDialog.TextField.DefaultBaseName").equals(baseName) ) {
			Preferences.set("ExportTiersDialog.fileBaseName", baseName, null);
		}
		Preferences.set("ExportTiersDialog.saveInOriginalFolder", originalDirRB.isSelected(), null);
		Preferences.set("ExportTiersDialog.saveInRelativeFolder", newDirectoryRB.isSelected(), null);
		String relFolderName = localDirectoryTextField.getText();
		if ( !ElanLocale.getString("ExportTiersDialog.TextField.DefaultLocalDirectoryName").equals(relFolderName) ) {
			Preferences.set("ExportTiersDialog.saveInRelativeFolderName", relFolderName, null);
		}
		String sameFolder = sameDirectoryTextField.getText();
		if ( !ElanLocale.getString("ExportTiersDialog.TextField.DirectoryNameField").equals(sameFolder) ) {
			Preferences.set("ExportTiersDialog.saveInSameFolderName", sameFolder, null);
		}
		Preferences.set("ExportTiersDialog.dontCreateEmptyFiles", dontExportFilesWithoutTiersCB.isSelected(), null);
	}
	
	private void loadPreferences() {
		String stringPref;
		Boolean boolPref;
		
		boolPref = Preferences.getBool("ExportTiersDialog.rootTiersCB", null);
		if (boolPref != null) {
			setRootTiersOnly(boolPref);
		}
		stringPref = Preferences.getString("ExportTiersDialog.SelectTiersMode", null);
		if (stringPref != null) {
			setSelectionMode(stringPref);
		}
		
		boolPref = Preferences.getBool("ExportTiersDialog.forceParentExport", null);
		if (boolPref != null) {
			boolean forceParent = boolPref;
			if (!forceParent) {
				neglectDependentTiersRB.setSelected(true);
			}
		}
		boolPref = Preferences.getBool("ExportTiersDialog.saveWithOriginalNames", null);
		if (boolPref != null) {
			boolean origNames = boolPref;
			if (!origNames) {
				addSuffixRB.setSelected(true);
				boolPref = Preferences.getBool("ExportTiersDialog.saveWithSuffix", null);
				if (boolPref != null) {
					boolean withSuffix = boolPref;
					if (!withSuffix) {
						useBaseNameWithSuffixRB.setSelected(true);
					}
				}
			}
		}
		stringPref = Preferences.getString("ExportTiersDialog.fileNameSuffix", null);
		if (stringPref != null) {
			suffixNameTextField.setText(stringPref);
		}
		stringPref = Preferences.getString("ExportTiersDialog.fileBaseName", null);
		if (stringPref != null) {
			baseNameTextField.setText(stringPref);
		}
		boolPref = Preferences.getBool("ExportTiersDialog.saveInOriginalFolder", null);
		if (boolPref != null) {
			boolean origFolder = boolPref;
			if (!origFolder) {
				boolPref = Preferences.getBool("ExportTiersDialog.saveInRelativeFolder", null);
				if (boolPref != null) {
					boolean relFolder = boolPref;
					if (relFolder) {
						newDirectoryRB.setSelected(true);
					} else {
						togetherInSameDirRB.setSelected(true);
					}
				}
			}
		}
		stringPref = Preferences.getString("ExportTiersDialog.saveInRelativeFolderName", null);
		if (stringPref != null) {
			localDirectoryTextField.setText((String) stringPref);
		}
		stringPref = Preferences.getString("ExportTiersDialog.saveInSameFolderName", null);
		if (stringPref != null) {
			sameDirectoryTextField.setText((String) stringPref);
		}
		boolPref = Preferences.getBool("ExportTiersDialog.dontCreateEmptyFiles", null);
		if (boolPref != null) {
			dontExportFilesWithoutTiersCB.setSelected((Boolean) boolPref);
		}
		
		updateButtonsAndFields();
	}
	
	/**
	 * KeyListener that handles key released events of the textfield
	 * if this textfield doesn't contain any character, then disable exportButton
	 * @author Jeffrey Lemein
	 * @version March 2010
	 */
	private class TextFieldHandler implements KeyListener{

		@Override
		public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {
			updateExportButtonEnabled();
		}

		@Override
		public void keyTyped(KeyEvent e) {}
		
	}
	
	/**
	 * ActionListener that handles action events of all radio buttons in this dialog
	 * @author Jeffrey Lemein
	 * @version March 2010
	 */
	private class RadioButtonHandler implements ActionListener{
		/**
		 * handles all events of radio button clicks
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			updateButtonsAndFields();
			/*
			sameDirectoryTextField.setEnabled( togetherInSameDirRB.isSelected() );
			browseBtn.setEnabled( togetherInSameDirRB.isSelected() );
			
			localDirectoryTextField.setEnabled(newDirectoryRB.isSelected());
			baseNameTextField.setEnabled( addSuffixRB.isSelected() && useBaseNameWithSuffixRB.isSelected());
			suffixNameTextField.setEnabled( addSuffixRB.isSelected() && originalFileNameWithSuffixRB.isSelected() );
			useBaseNameWithSuffixRB.setEnabled( addSuffixRB.isSelected() );
			originalFileNameWithSuffixRB.setEnabled( addSuffixRB.isSelected() );
			*/
			updateExportButtonEnabled();
		}
	}
	
	/**
	 * Thread used to save files from eafFiles in a separate thread
	 * @author Jeffrey Lemein
	 * @version March 2010
	 */
	private class SaveFileThread extends Thread{
		private List<String> tiersToSave;
		private final int YES = 1, NO = 2, YES_TO_ALL = 0, NO_TO_ALL = 3;
		
		/**
		 * Saves all files with the tier names in the given list
		 * @param tiersToSave the names of the tiers that need to be saved
		 */
		public SaveFileThread( List<String> tiersToSave ){
			this.tiersToSave = tiersToSave;	
		}
		
		/**
		 * Returns the directory where the corresponding file needs to be saved in.
		 * This depends on the selected options for the export
		 * @param file particular file where the directory is computed for
		 * @return the directory where the file needs to be saved
		 */
		private String getDirectoryToSave( File file ){
			String originalDirectory = file.getPath().substring( 0, file.getPath().length() - file.getName().length());
			
			//if original directory is selected
			if( originalDirRB.isSelected() ){
				return originalDirectory;
			}
			
			//if new directory has been selected
			if( newDirectoryRB.isSelected() ) {
				return originalDirectory + localDirectoryTextField.getText().trim();
			}
			
			//if same directory has been chosen
			if( togetherInSameDirRB .isSelected()) {
				return sameDirectoryTextField.getText();
			}
			
			return null;
		}
		
		/**
		 * Gets the file name of the specified transcription and edits it to the format specified for exportation
		 * @param transImpl the transcription implementation of the file
		 * @param nr the number of the saved file.
		 * 		  If all files are saved, the first file has number 0 and each file that follows is incremented by 1
		 * @return the name of the file to be saved or null if no name could be found
		 */
		private String getFileName( TranscriptionImpl transImpl, int nr ){			
			if( originalFileNameRB.isSelected() ) {
				return transImpl.getName();
			}
			
			if( originalFileNameWithSuffixRB.isSelected() ){
				String fileName = transImpl.getName();
				int index = fileName.lastIndexOf('.');
				return fileName.substring(0, index) + suffixNameTextField.getText() + fileName.substring(index);
			}
			
			if( useBaseNameWithSuffixRB.isSelected() ){
				String fileName = transImpl.getName();
				int index = fileName.lastIndexOf('.');
				return baseNameTextField.getText() + (nr+1) + fileName.substring(index);
			}
			
			return null;
		}
		
		/**
		 * Checks if the given directory exists. If it does not exist, it will
		 * try to create one.
		 * @param directoryPath the path to an existing (or nonexisting) directory
		 * @return boolean value indicating if directory exists (and is created succesfully if needed)
		 */
		private boolean createDirectory( String directoryPath ){
			boolean directoryExists = true;
			File directory = new File(directoryPath);
			
			//if directory does not exist, try to make it
			if( !directory.exists() ) {
				directoryExists = (new File(directoryPath)).mkdir();
			}
			
			return directoryExists;
		}
		
		/**
		 * Set the relative paths for the media files
		 * @param pathName pathname where eaf file is saved too
		 * @param tr the transcription implementation that is going to be saved
		 */
		private void setRelativePaths( String pathName, TranscriptionImpl tr ){
			// update relative media paths
            // make sure the eaf path is treated the same way as media files,
            // i.e. it starts with file:/// or file://
			List<LinkedFileDescriptor> linkedFiles = tr.getLinkedFileDescriptors();
			
            String fullEAFURL = FileUtility.pathToURLString(pathName);

            List<MediaDescriptor> mediaDescriptors = tr.getMediaDescriptors();
            MediaDescriptor md;
            String relUrl;

            for (int i = 0; i < mediaDescriptors.size(); i++) {
                md = mediaDescriptors.get(i);
                relUrl = FileUtility.getRelativePath(fullEAFURL, md.mediaURL);
                md.relativeMediaURL = "file:/" + relUrl;
            }

            // linked other files 
            if (linkedFiles.size() > 0) {
                LinkedFileDescriptor lfd;

                for (int i = 0; i < linkedFiles.size(); i++) {
                    lfd = linkedFiles.get(i);
                    relUrl = FileUtility.getRelativePath(fullEAFURL,
                            lfd.linkURL);
                    lfd.relativeLinkURL = "file:/" + relUrl;
                }
            }
		}
		
		/**
		 * Removes tiers from transcription based on the selected tiers in the export dialog 
		 * @param transImpl the transcription of which the tiers could be removed.
		 */
		private void removeTiersFromTranscription( TranscriptionImpl transImpl ){
			//get tiers in new set, so tiers can be removed from transImpl
			List<TierImpl> tiers = new ArrayList<TierImpl>( transImpl.getTiers() );
			
			//for each tier in transcription, check if it exists in the 
			//list of tiers that needs to be saved
			//if not, then remove it from the transcription
			for( int i=0; i<tiers.size(); i++ ){
				TierImpl tier = tiers.get(i);
			
				//if tier is not selected for export, then remove its dependent tiers too
				if( !exists( tier.getName(), tiersToSave ) ){ 
					List<TierImpl> dependentTiers = tier.getDependentTiers();
					
					//if dependent tiers should be neglected, then remove them all
					if( neglectDependentTiersRB.isSelected() ){						
						for( int n=0; n<dependentTiers.size(); n++ ) {
							transImpl.removeTier( dependentTiers.get(n) );
						}
					
						//remove parent tier
						transImpl.removeTier( tier );
					} else{
						//if a dependent tier exists that needs to be exported, don't remove the parent tier
						boolean removeParentTier = true;
						
						for( int n=0; n<dependentTiers.size(); n++ ) {
							if( exists( dependentTiers.get(n).getName(), tiersToSave ) ){
								removeParentTier = false;
								break;
							}
						}
						
						if( removeParentTier ) {
							transImpl.removeTier( tier );
						}
					}
				}
			}
		}		
		
		/**
		 * The start of the save thread. All files will be saved here.
		 */
		@Override
		public void run(){
			int failedExports = 0; //counter to count number of failed exports
			int refusedExports = 0; //nr exports refused because user indicated that files should not overwrite existing files
			int emptyFiles = 0; //nr of files not exported because they were empty
			boolean saveForever = false, skipForever = false;
			
			File file;
			TranscriptionImpl transImpl;
			//walk through all transcriptions
			for( int f=0; f < files.size(); f++ ){
				 file = files.get(f);

		         if (file == null) {
		        	 continue;
		          }

		         try {
		        	 transImpl = new TranscriptionImpl(file.getAbsolutePath());
		        	 removeTiersFromTranscription(transImpl);
		         
		        	 //Try to save the transcription
		        	 String path = getDirectoryToSave( files.get(f) );
		        	 String fileName = getFileName( transImpl, f );
		        	 if (path.charAt(path.length() - 1) != File.separatorChar) {
		        		 path += File.separatorChar;
		        	 }
		        	 String directoryToSave = path + fileName;
		        	
		        	 //if directory does not exist, then create it
		        	 boolean directoryExists = createDirectory(path);
		        	 boolean saveThisFile = true;
		        	 boolean fileExists = new File(directoryToSave).exists();
					
		        	 if( fileExists && skipForever ) {
						saveThisFile = false;
					}
					
		        	 //overwrite files is not selected, then check if file exists and ask for overwriting
		        	 if( !skipForever && !saveForever  && fileExists ){
		        		 //show dialog to ask if existing file should be overwritten
		        		 Object[] possibleValues = { "Yes To All", "Yes", "No", "No To All" };
						
		        		 int choice = JOptionPane.showOptionDialog( null, 
								ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description1") + " " + fileName + " " + ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description2") + "\n\n" +
								ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description3") + "\t " + directoryToSave + "\n\n" +
								ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description4") + "\n", ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Title"), 
										JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, possibleValues, possibleValues[2]);
						
		        		 switch( choice ){
								case YES:
									saveThisFile = true;
									break;
							
								case YES_TO_ALL:
									saveForever = true;
									break;
								
								case NO_TO_ALL:
									skipForever = true;
									saveThisFile = false;
									break;
								
								default: //NO and other
									saveThisFile = false;
		        		 }
							
		        	 }
					
		        	 //set relative paths
		        	 setRelativePaths( directoryToSave, transImpl );
					
		        	 //save files
		        	 if( directoryExists ) {
						//if all files need to be saved OR this file need to be saved
		        		 if( saveForever || saveThisFile ){
		        			 int saveAsType = SaveAs27Preferences.saveAsType(transImpl);
		        			 if( !dontExportFilesWithoutTiersCB.isSelected() ) {
								ACMTranscriptionStore.getCurrentTranscriptionStore().storeTranscriptionIn(transImpl, null, new ArrayList<TierImpl>(), directoryToSave, saveAsType);
							} else{
		        				 if( transImpl.getTiers().size() > 0 ) {
									ACMTranscriptionStore.getCurrentTranscriptionStore().storeTranscriptionIn(transImpl, null, new ArrayList<TierImpl>(), directoryToSave, saveAsType);
								} else {
									emptyFiles++;
								}
		        			 }
		        		 }else{
		        			 refusedExports++;
		        		 }
					} else {
						failedExports++;
					}
		         }catch(IOException e){
		        	 LOG.warning("Can not write transcription to file with directory/filename: " + files.get(f).getAbsolutePath() + "/" + files.get(f).getName() );
		        	 failedExports++;
		         }catch (Exception ex) {
		                // catch any exception that could occur and continue
		                ClientLogger.LOG.warning("Could not handle file: " +
		                    file.getAbsolutePath());
		         }
				
		         //update progress bar
		         progressValue++;
		         progressBar.setValue(progressValue);
		         progressBar.setString( Math.round(100 * (f+1)/(float)files.size()) + "%" );
			}
			
			//exporting finished, so update progress bar
			progressBar.setString( ElanLocale.getString("ExportTiersDialog.ProgressBar.ExportingDone") );
			
			//show information on the export process
			String msg = (files.size()-failedExports-refusedExports-emptyFiles) + " " + ElanLocale.getString("ExportTiersDialog.Message.OutOf") + " " + files.size() + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg1");
			
			//construct additional details if not all files are exported succesfully
			if( emptyFiles + refusedExports + failedExports > 0 ){
				if( emptyFiles == 1 ) {
					msg += "\n\n" + emptyFiles + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg2single");
				} else {
					msg += "\n\n" + emptyFiles + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg2");
				}
				
				if( refusedExports == 1 ) {
					msg += "\n" + refusedExports + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg3single");
				} else {
					msg += "\n" + refusedExports + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg3");
				}
				
				if( failedExports == 1 ) {
					msg += "\n" + failedExports + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg4single");
				} else {
					msg += "\n" + failedExports + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg4");
				}
			}
			
			if( failedExports > 0 ) {
				JOptionPane.showMessageDialog(ExportTiersDialog.this, msg, ElanLocale.getString("ExportTiersDialog.Message.ExportingDoneTitle"), JOptionPane.WARNING_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(ExportTiersDialog.this, msg, ElanLocale.getString("ExportTiersDialog.Message.ExportingDoneTitle"), JOptionPane.INFORMATION_MESSAGE);
			}
			
			if((files.size()-failedExports-refusedExports-emptyFiles) > 0){
				closeDialog(null);
			} else {
				progressValue =0;
				progressBar.setValue(progressValue);
				progressBar.setString(0 + "%" );
				exportingStarted = false;
				updateExportButtonEnabled();
			}
		}
	}
}
		
