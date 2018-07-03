package mpi.eudico.client.annotator.gui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.flex.FlexDecoderInfo;

/**
 * A dialog for configuring the import of FieldWorks Language Explorer, FLEx,
 * format.
 *
 * @author Han Sloetjes
 * @updatedBy Aarthy Somasundaram, Feb 2013
 */
public class ImportFLExDialog extends ClosableDialog implements ActionListener,
    ChangeListener {
    
	//components of file panel
	private JPanel filePanel;
	private JTextField flexFileField;
	private JTextField mediaFileField;
    private JButton selectFlexButton;
    private JButton selectMediaButton;
	    
	//components of options pane    
    private JPanel optionsPanel;
    private JCheckBox includeITCB;
    private JCheckBox includeParagrCB;
    private JCheckBox importParticipantInfoCB;
    private JComboBox unitsCombo;   
    
    //duration   
    private JTextField unitTextField;
    
    //linguistic types
    private JRadioButton typesPerElementRB;
    private JRadioButton typesPerTypeRB;
    private JCheckBox typesPerLanguageCB;    
    
    //component in buttonPanel
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;
    
    private final String[] elements = new String[] {
            FlexConstants.PHRASE, FlexConstants.WORD
        };
    private List<String> tempMediaPaths;
    private FlexDecoderInfo decoderInfo = null;
    
    
    // preferences string    
    public static final String INTERLINEAR_TEXT = "ImportFLExDialog.IncludeInterlinearText";
    public static final String PARAGRAPH = "ImportFLExDialog.IncludeParagraph";
    public static final String PARTICIPANT = "ImportFLExDialog.ImportParticipantInfo";
    public static final String SMALLEST_ALIGNABLE_ELEMENT = "ImportFLExDialog.SmallestAlignableElement";
    public static final String TYPES_PER_ELEMENT = "ImportFLExDialog.TypesPerElement";
    public static final String TYPES_PER_TYPE = "ImportFLExDialog.TypesPerType";
    public static final String TYPES_PER_LANG = "ImportFLExDialog.TypesPerLanguage";
    public static final String DURATION = "ImportFLExDialog.DurationPerPhrase";

    /**
     * Constructor.
     *
     * @param owner
     *
     * @throws HeadlessException
     */
    public ImportFLExDialog(Frame owner) throws HeadlessException {
        super(owner, true);
        initComponents();
        postInit();
    }

    /**
     * Returns the created decoder info object, or null in case the dialog was
     * canceled.
     *
     * @return the decoder info storing the user's selections for the parser
     */
    public FlexDecoderInfo getDecoderInfo() {
        return decoderInfo;
    }

    /**
     * Initialize Components
     */
    private void initComponents() {
        setTitle(ElanLocale.getString("ImportDialog.Title.Flex"));
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gbc;

        // initialize file panel components
        filePanel = new JPanel(new GridBagLayout());
        filePanel.setBorder(new TitledBorder(""));        
        flexFileField = new JTextField("", 20);
        flexFileField.setEditable(false); 
        mediaFileField = new JTextField("", 20);
        mediaFileField.setEditable(false);
        selectFlexButton = new JButton("...");
        selectFlexButton.addActionListener(this);
        selectMediaButton = new JButton("...");
        selectMediaButton.addActionListener(this);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;       
        filePanel.add(new JLabel(ElanLocale.getString(
        		"ImportDialog.Flex.File")), gbc);
        
        gbc.gridy = 1;
        filePanel.add(new JLabel(ElanLocale.getString(
        		"ImportDialog.Label.Media")), gbc);

        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        filePanel.add(flexFileField, gbc);

        gbc.gridy = 1;
        filePanel.add(mediaFileField, gbc);
       
        gbc.gridy = 0;
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        filePanel.add(selectFlexButton, gbc);
       
        gbc.gridy = 1;
        filePanel.add(selectMediaButton, gbc);

        // options panel
        optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ImportDialog.Label.Options")));
        includeITCB = new JCheckBox(ElanLocale.getString(
                    "ImportDialog.Flex.IncludeIT"));
        includeITCB.setSelected(true);
        includeParagrCB = new JCheckBox(ElanLocale.getString(
                    "ImportDialog.Flex.IncludePara"));
        includeParagrCB.setSelected(true);    
        
        importParticipantInfoCB = new JCheckBox(ElanLocale.getString(
                    "ImportDialog.Flex.ImportParticipantInfo"));
        
        typesPerElementRB = new JRadioButton(ElanLocale.getString("ImportDialog.Flex.LinTypeForBasicElement"), true);
        typesPerElementRB.addChangeListener(this);
        typesPerTypeRB = new JRadioButton(ElanLocale.getString("ImportDialog.Flex.LinTypeForTypes"));
        typesPerTypeRB.addChangeListener(this);
        
        ButtonGroup group = new ButtonGroup();
        group.add(typesPerElementRB);
        group.add(typesPerTypeRB);
        
        typesPerLanguageCB = new JCheckBox(ElanLocale.getString("ImportDialog.Flex.LinTypeForLang"));      
        typesPerLanguageCB.setEnabled(false);        
        
        unitsCombo = new JComboBox(elements);
        unitsCombo.setSelectedItem(FlexConstants.PHRASE);      
        unitTextField = new JTextField("", 8);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        gbc.gridy = 0;
        optionsPanel.add(includeITCB, gbc);
        
        gbc.gridy = gbc.gridy +1;
        optionsPanel.add(includeParagrCB, gbc);
        
        gbc.gridy = gbc.gridy +1;
        optionsPanel.add(importParticipantInfoCB, gbc);        
        
        gbc.gridy = gbc.gridy +1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(12, 6, 2, 6);
        optionsPanel.add(new JLabel(ElanLocale.getString(
        		"ImportDialog.Flex.SmallestTimeAlignable")), gbc);
       
        gbc.gridx = 1;       
        gbc.gridwidth = 2;
        optionsPanel.add(unitsCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = gbc.gridy +1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(12, 6, 2, 6);
        optionsPanel.add(new JLabel(ElanLocale.getString("ExportTiersDialog.Tab2")), gbc);
        
        gbc.gridy = gbc.gridy +1;
        gbc.insets = insets;
        optionsPanel.add(typesPerElementRB, gbc);
        
        gbc.gridy = gbc.gridy +1;
        gbc.insets = insets;
        optionsPanel.add(typesPerTypeRB, gbc);
     
        gbc.gridy = gbc.gridy +1;
        gbc.insets = new Insets(2, 26, 2, 6);
        optionsPanel.add(typesPerLanguageCB, gbc);
        
        gbc.gridy = gbc.gridy +1;
        gbc.insets = new Insets(12, 6, 2, 6);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        optionsPanel.add(new JLabel(ElanLocale.getString(
                "ImportDialog.Flex.UnitDuration")), gbc);
        
        gbc.gridx = 1; 
        gbc.gridwidth = 2;       
        optionsPanel.add(unitTextField, gbc);
    
        //initialize components of button panel
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);      
        
        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        getContentPane().add(filePanel, gbc);        
        
        gbc.gridy = 1;        
        getContentPane().add(optionsPanel, gbc);
        
        gbc.gridy = 2;        
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.NONE;
        getContentPane().add(buttonPanel, gbc);
        
        loadPreferences();
    }

    private void postInit() {
        pack();
        setLocationRelativeTo(getParent());
        setVisible(true); //blocks
    }

    /**
     * File chooser to select a flextext file
     * for import
     */
    private void selectFlexFile() {
        FileChooser chooser = new FileChooser(getParent());
        chooser.createAndShowFileDialog(ElanLocale.getString("Button.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		null, FileExtension.FLEX_EXT, false, "LastUsedFlexDir", FileChooser.FILES_ONLY, null);
	    File f = chooser.getSelectedFile();
        if (f != null) {
        	flexFileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * File chooser to select a media files
     * for import
     */
    private void selectMediaFiles() {
    	FileChooser chooser = new FileChooser(getParent());
        chooser.createAndShowMultiFileDialog(ElanLocale.getString("Button.Select"), FileChooser.MEDIA);       
        Object[] files = chooser.getSelectedFiles();
        
        if (files != null) {    
            if (files.length > 0) {
                if (tempMediaPaths == null) {
                    tempMediaPaths = new ArrayList<String>(4);
                } else {
                    tempMediaPaths.clear();
                }

                String filePaths = "";

                for (Object file : files) {
                    tempMediaPaths.add(((File) file).getAbsolutePath());
                    filePaths += (file + ", ");
                }

                mediaFileField.setText(filePaths);
            }
        }
    }

    /**
     * Checks the fields for valid selections and prompts if anything is
     * missing.
     *
     * @return true if enough information has been provided to continue with
     *         the import, false otherwise
     */
    private boolean checkFields() {
        String path = flexFileField.getText();

        if ((path == null) || (path.length() == 0)) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ImportDialog.Flex.Message.NoFlex"),
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

            return false;
        } else {
            File f = new File(path);

            if (!f.exists() || f.isDirectory()) {
                String strMessage = ElanLocale.getString("Menu.Dialog.Message1");
                strMessage += path;
                strMessage += ElanLocale.getString("Menu.Dialog.Message2");

                String strError = ElanLocale.getString("Message.Error");
                JOptionPane.showMessageDialog(this, strMessage, strError,
                    JOptionPane.ERROR_MESSAGE);

                return false;
            }
        }

        // replace all backslashes by forward slashes
        path = path.replace('\\', '/');

        long durationVal = -1;

        try {
        	durationVal = Long.parseLong(unitTextField.getText());
        } catch (NumberFormatException nfe) {
        	JOptionPane.showMessageDialog(this,
        		ElanLocale.getString("ImportDialog.Flex.Message.DurElement"),
        		ElanLocale.getString("Message.Error"),
        		JOptionPane.ERROR_MESSAGE);

        	return false;
        }

        // if we get here create the decoder object 
        decoderInfo = new FlexDecoderInfo(path);
        decoderInfo.smallestWithTimeAlignment = (String) unitsCombo.getSelectedItem();
        decoderInfo.inclITElement = includeITCB.isSelected();
        decoderInfo.inclParagraphElement = includeParagrCB.isSelected();
        decoderInfo.importParticipantInfo = importParticipantInfoCB.isSelected();
        decoderInfo.perPhraseDuration = durationVal;
        if(typesPerTypeRB.isSelected()){
        	decoderInfo.createLingForNewType = typesPerTypeRB.isSelected();
            decoderInfo.createLingForNewLang = typesPerLanguageCB.isSelected();
        }
        
        if ((tempMediaPaths != null) && (tempMediaPaths.size() > 0)) {
            ArrayList<MediaDescriptor> descriptors = new ArrayList<MediaDescriptor>(tempMediaPaths.size());

            for (String medPath : tempMediaPaths) {
                MediaDescriptor descriptor = MediaDescriptorUtil.createMediaDescriptor(medPath);

                if (descriptor != null) {
                    descriptors.add(descriptor);
                }
            }
            decoderInfo.setMediaDescriptors(descriptors);
        }
        
        storePreferences();

        return true;
    }

    /**
     * Handling of action events fired by the buttons.
     *
     * @param e event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectFlexButton) {
            selectFlexFile();
        } else if (e.getSource() == selectMediaButton) {
            selectMediaFiles();
        } else if (e.getSource() == okButton) {
            if (checkFields()) { // creates a decoder info object            	
            	setVisible(false);
            }
        } else if (e.getSource() == cancelButton) {
            decoderInfo = null;
            setVisible(false);
        } 
    }
    
	@Override
	public void stateChanged(ChangeEvent e) {
		typesPerLanguageCB.setEnabled(typesPerTypeRB.isSelected());
	}
	
	private void storePreferences(){	
	    Preferences.set(INTERLINEAR_TEXT, includeITCB.isSelected(), null);
	    Preferences.set(PARAGRAPH, includeParagrCB.isSelected(), null);
	    Preferences.set(PARTICIPANT, importParticipantInfoCB.isSelected(), null);	    	 
	    Preferences.set(SMALLEST_ALIGNABLE_ELEMENT, unitsCombo.getSelectedItem(), null);
	    Preferences.set(TYPES_PER_ELEMENT, typesPerElementRB.isSelected(), null);
	    Preferences.set(TYPES_PER_TYPE, typesPerTypeRB.isSelected(), null);
	    Preferences.set(TYPES_PER_LANG, typesPerLanguageCB.isSelected(), null);
	    Preferences.set(DURATION, unitTextField.getText(), null);
	}
	
	private void loadPreferences() {
		 Boolean boolPref = Preferences.getBool(INTERLINEAR_TEXT, null);
		 if (boolPref != null) {
		   	includeITCB.setSelected(boolPref);
		 }
		 
		 boolPref = Preferences.getBool(PARAGRAPH, null);
		 if (boolPref != null) {
			 includeParagrCB.setSelected(boolPref);
		 }
		 
		 boolPref = Preferences.getBool(PARTICIPANT, null);
		 if (boolPref != null) {
			 importParticipantInfoCB.setSelected(boolPref);
		 }		 
		 
		 String stringPref = Preferences.getString(SMALLEST_ALIGNABLE_ELEMENT, null);
		 if (stringPref != null) {
			 unitsCombo.setSelectedItem(stringPref);
		 }
		 
		 boolPref = Preferences.getBool(TYPES_PER_ELEMENT, null);
		 if (boolPref != null) {
			 typesPerElementRB.setSelected(boolPref);
		 }
		 
		 boolPref = Preferences.getBool(TYPES_PER_TYPE, null);
		 if (boolPref != null) {
			 typesPerTypeRB.setSelected(boolPref);
		 }
		 
		 boolPref = Preferences.getBool(TYPES_PER_LANG, null);
		 if (boolPref != null) {
			 typesPerLanguageCB.setSelected(boolPref);
		 }
		 
		 stringPref = Preferences.getString(DURATION, null);
		 if (stringPref != null) {
			 unitTextField.setText(stringPref.toString());
		 }
	}
}
