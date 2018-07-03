package mpi.eudico.client.annotator.export;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.ShoeboxMarkerDialog;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.shoebox.MarkerRecord;
import mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxTypFile;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.shoebox.interlinear.Interlinearizer;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;


/**
 * An export dialog for exporting tiers to a Shoebox/Toolbox file.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ExportShoebox extends AbstractExtTierExportDialog
    implements  ItemListener {
    
	private JButton fieldSpecButton;
    private JButton typButton;
    private JCheckBox allUnicodeCB;
    private JCheckBox correctTimesCB;
    private JCheckBox generateMarkersCB;
    private JCheckBox wrapLinesCB;

    /** ui elements */
    private JLabel charPerLineLabel;
    private JLabel dbTypeLabel;
    private JLabel tierNamesLabel;
    private JLabel timeFormatLabel;
    private JLabel typeLabel;
    private JPanel markerPanel;
    private JRadioButton hhMMSSMSFormatRB;
    private JRadioButton specRB;
    private JRadioButton ssMSFormatRB;
    private JRadioButton tierNamesRB;
    private JRadioButton typeRB;
    private JTextField dbTypField;
    private JTextField numCharTF;
    private JTextField typField;

    // some strings
    // not visible in the table header
    /** default line width */
    private final int NUM_CHARS = 80;
    private List<MarkerRecord> markers;

    // fields for the encoder
    private String databaseType;
    private String exportFileName;   

    /**
     * Constructor.
     *
     * @param parent parent frame
     * @param modal the modal/blocking attribute
     * @param transcription the transcription to export from
     */
    public ExportShoebox(Frame parent, boolean modal,
        TranscriptionImpl transcription) {
        super(parent, modal, transcription, null);
        makeLayout();
        extractTiers();
        postInit();
        typField.requestFocus();
    }

    /**
     * DOCUMENT ME!
     *
     * @param tier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<TierImpl> getTierTree(TierImpl tier) {
    	List<TierImpl> tierTree = new ArrayList<TierImpl>();
    	List<List<TierImpl>> tierTrees = new ArrayList<List<TierImpl>>();

    	List<TierImpl> children = tier.getChildTiers();

        tierTree.add(tier);

        for (int j = 0; j < children.size(); j++) {
            TierImpl child = children.get(j);
            tierTrees.add(getTierTree(child));
        }

        Collections.sort(tierTrees, new ListComparator());

        for (int j = 0; j < tierTrees.size(); j++) {
            tierTree.addAll(tierTrees.get(j));
        }

        return tierTree;
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == typButton) {
            chooseTyp();
            typeRB.setSelected(true);
        } else if (source == fieldSpecButton) {
            specifyFieldSpecs();
            specRB.setSelected(true);
        } else {
            super.actionPerformed(ae);
        }
    }

    /**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    @Override
	public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == wrapLinesCB) {
            if (wrapLinesCB.isSelected()) {
                setDefaultNumOfChars();
                numCharTF.requestFocus();
            } else {
                numCharTF.setEnabled(false);
                numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            }
        } else if (ie.getSource() == typeRB) {
            setEnabledAutoGenerate(false);
            setEnabledAllUnicode(true);
        } else if (ie.getSource() == specRB) {
            setEnabledAutoGenerate(false);
            setEnabledAllUnicode(false);
        } else if (ie.getSource() == tierNamesRB) {
            setEnabledAutoGenerate(true);
            setEnabledAllUnicode(false);
        }
    }   

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
    	
    	
    	List<String> listPref = Preferences.getListOfString("ExportShoebox.TierOrder", transcription);
    	if (listPref != null) {
    		setTierOrder(listPref);        	
        } else {
        	super.extractTiers(false);
        }
    	
    	listPref = Preferences.getListOfString("ExportShoebox.selectedTiers", transcription);
        if (listPref != null) {
        	//loadTierPreferences((List)useTyp);
        	setSelectedTiers(listPref);
        } else {
        	//selectAll(true);
        }
        
        String stringPref = Preferences.getString("ExportShoebox.SelectTiersMode", transcription);
        if (stringPref != null) {
        	//List list = (List) Preferences.get("ExportShoebox.HiddenTiers", transcription);
        	setSelectionMode(stringPref);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals(stringPref) ) {
            	// call this after! the mode has been set
        		List<String> selItems  = Preferences.getListOfString("ExportShoebox.LastSelectedItems", transcription);
            	
            	if (selItems != null) {
            		setSelectedItems(selItems);
            	}
        	}
         }
    }

    /**
     * Initializes UI elements.
     */
    @Override
	protected void makeLayout() {
        super.makeLayout();
        charPerLineLabel = new JLabel();
        wrapLinesCB = new JCheckBox();
        wrapLinesCB.setSelected(true);
        numCharTF = new JTextField(4);
        timeFormatLabel = new JLabel();
        ssMSFormatRB = new JRadioButton();
        hhMMSSMSFormatRB = new JRadioButton();
        correctTimesCB = new JCheckBox();

        typField = new JTextField("", 23);
        typButton = new JButton("...");
        typeLabel = new JLabel();
        allUnicodeCB = new JCheckBox();
        fieldSpecButton = new JButton();
        dbTypeLabel = new JLabel();
        dbTypField = new JTextField("", 14);

        ButtonGroup buttonGroup = new ButtonGroup();
        typeRB = new JRadioButton();
        typeRB.setSelected(true);
        typeRB.addItemListener(this);
        specRB = new JRadioButton();
        specRB.addItemListener(this);
        tierNamesRB = new JRadioButton();
        tierNamesRB.addItemListener(this);
        buttonGroup.add(typeRB);
        buttonGroup.add(specRB);
        buttonGroup.add(tierNamesRB);
        tierNamesLabel = new JLabel();
        generateMarkersCB = new JCheckBox();

        GridBagConstraints gridBagConstraints;

        optionsPanel.setLayout(new GridBagLayout());

        wrapLinesCB.addItemListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(wrapLinesCB, gridBagConstraints);

        JPanel fill = new JPanel();
        Dimension fillDim = new Dimension(30, 10);
        fill.setPreferredSize(fillDim);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(fill, gridBagConstraints);

        numCharTF.setEnabled(false);
        numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(numCharTF, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(charPerLineLabel, gridBagConstraints);

        ButtonGroup group = new ButtonGroup();
        group.add(ssMSFormatRB);
        ssMSFormatRB.setSelected(true);
        group.add(hhMMSSMSFormatRB);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(timeFormatLabel, gridBagConstraints);

        fill = new JPanel();
        fillDim = new Dimension(30, 10);
        fill.setPreferredSize(fillDim);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(fill, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(hhMMSSMSFormatRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(ssMSFormatRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(correctTimesCB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(optionsPanel, gridBagConstraints);

        markerPanel = new JPanel();
        markerPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(typeRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(typeLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        markerPanel.add(typField, gridBagConstraints);

        typButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(typButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(allUnicodeCB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(specRB, gridBagConstraints);

        fieldSpecButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(fieldSpecButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        markerPanel.add(dbTypeLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(dbTypField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(tierNamesRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(tierNamesLabel, gridBagConstraints);

        generateMarkersCB.addItemListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        markerPanel.add(generateMarkersCB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(markerPanel, gridBagConstraints);

        //move buttonPanel from 3rd to 4th component
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        ((GridBagLayout) getContentPane().getLayout()).setConstraints(buttonPanel,
            gridBagConstraints);

        setDefaultNumOfChars();
        setEnabledAutoGenerate(false);
 
        setShoeboxMarkerRB();
        loadPreferences();
        
        updateLocale();
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Starts the actual export after performing some checks.
     *
     * @return true if export succeeded, false oherwise
     */
    @Override
	protected boolean startExport() {
    	savePreferences();
    	
        if (!checkMarkerFields()) {
            return false;
        }

        List<String> selectedTiers = getSelectedTiers();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return false;
        }

        // check the chars per line value
        int charsPerLine = Integer.MAX_VALUE;

        if (wrapLinesCB.isSelected()) {
            String textValue = numCharTF.getText().trim();

            try {
                charsPerLine = Integer.parseInt(textValue);
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString(
                        "ExportShoebox.Message.InvalidNumber"));
                numCharTF.selectAll();
                numCharTF.requestFocus();

                return false;
            }
        }

        int timeFormat = Interlinearizer.SSMS;

        if (hhMMSSMSFormatRB.isSelected()) {
            timeFormat = Interlinearizer.HHMMSSMS;
        }

        // prompt for file name and location
        File exportFile = promptForFile(ElanLocale.getString("ExportShoeboxDialog.Title"), null, FileExtension.SHOEBOX_TEXT_EXT, false);

        if (exportFile == null) {
            return false;
        }

        exportFileName = exportFile.getPath();

        // export....
        boolean success = doExport(exportFileName, selectedTiers, charsPerLine,
                timeFormat, correctTimesCB.isSelected());

        if (success) {
            if (generateMarkersCB.isSelected()) {
                autoGenerateMarkerFile();
            }
        }

        return success;
    }

    /**
     * Applies localized strings to the ui elements. For historic reasons the
     * string identifiers start with "TokenizeDialog"
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("ExportShoebox.Title"));
        titleLabel.setText(ElanLocale.getString("ExportShoebox.Title"));
        markerPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportShoebox.Label.Markers")));
        wrapLinesCB.setText(ElanLocale.getString(
                "ExportShoebox.Label.WrapBlocks"));
        charPerLineLabel.setText(ElanLocale.getString(
                "ExportShoebox.Label.NumberChars"));
        timeFormatLabel.setText(ElanLocale.getString(
                "ExportShoebox.Label.Format"));
        hhMMSSMSFormatRB.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.TimeCodeFormat.TimeCode"));
        ssMSFormatRB.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.TimeCodeFormat.Seconds"));
        correctTimesCB.setText(ElanLocale.getString("ExportDialog.CorrectTimes"));
        typeLabel.setText(ElanLocale.getString("ExportShoebox.Label.Type"));
        allUnicodeCB.setText(ElanLocale.getString(
                "ExportShoebox.CheckBox.AllUnicode"));
        fieldSpecButton.setText(ElanLocale.getString(
                "ExportShoebox.Button.FieldSpec"));
        dbTypeLabel.setText(ElanLocale.getString(
                "ExportShoebox.Label.SpecifyType"));
        tierNamesLabel.setText(ElanLocale.getString(
                "ExportShoebox.Label.UseTierNames"));
        generateMarkersCB.setText(ElanLocale.getString(
                "ExportShoebox.CheckBox.AutoGenerateMarkers"));
    }
    
    private void loadPreferences(){
    	Boolean boolPref = Preferences.getBool("ExportShoebox.wrapLinesCB", null);
    	if (boolPref != null) {
			wrapLinesCB.setSelected(boolPref);
		}
    	
    	String stringPref = Preferences.getString("ExportShoebox.numCharTF", null);
    	if (stringPref != null) {
			numCharTF.setText(stringPref);
		}
    	
    	boolPref = Preferences.getBool("ExportShoebox.hhMMSSMSFormatRB",null);
    	if (boolPref != null) {
			hhMMSSMSFormatRB.setSelected(boolPref);
		}
    	
    	boolPref = Preferences.getBool("ExportShoebox.ssMSFormatRB", null);
    	if (boolPref != null) {
			ssMSFormatRB.setSelected(boolPref);
		}
    	
    	boolPref = Preferences.getBool("ExportShoebox.allUnicodeCB", null);
    	if (boolPref != null) {
			allUnicodeCB.setSelected(boolPref);
		}
    	
    	boolPref = Preferences.getBool("ExportShoebox.generateMarkersCB", null);
    	if (boolPref != null) {
			generateMarkersCB.setSelected(boolPref);
		}
    	
    	boolPref = Preferences.getBool("ExportShoebox.correctTimesCB", null);
    	if (boolPref != null) {
			correctTimesCB.setSelected(boolPref);
		}
    }
    
    private void savePreferences(){    	    	
    	Preferences.set("ExportShoebox.selectedTiers", getSelectedTiers(), transcription);
    	Preferences.set("ExportShoebox.wrapLinesCB", wrapLinesCB.isSelected(), null);
    	Preferences.set("ExportShoebox.numCharTF", numCharTF.getText(), null);
    	Preferences.set("ExportShoebox.hhMMSSMSFormatRB", hhMMSSMSFormatRB.isSelected(),null);
    	Preferences.set("ExportShoebox.ssMSFormatRB", ssMSFormatRB.isSelected(), null);
    	Preferences.set("ExportShoebox.allUnicodeCB",allUnicodeCB.isSelected(), null);
    	Preferences.set("ExportShoebox.generateMarkersCB", generateMarkersCB.isSelected(), null);    
    	Preferences.set("ExportShoebox.correctTimesCB", correctTimesCB.isSelected(), null);
    	
    	Preferences.set("ExportShoebox.SelectTiersMode", getSelectionMode(), transcription); 
    	// save the selected list in case on non-tier tab
    	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
    		Preferences.set("ExportShoebox.LastSelectedItems", getSelectedItems(), transcription);
    	}
    	Preferences.set("ExportShoebox.HiddenTiers", getHiddenTiers(), transcription);
    	
    	List<String> tierOrder = getTierOrder();
    	Preferences.set("ExportShoebox.TierOrder", tierOrder, transcription);
    	/*
    	List currentTierOrder = getCurrentTierOrder();    	    	
    	for(int i=0; i< currentTierOrder.size(); i++){
    		if(currentTierOrder.get(i) != tierOrder.get(i)){
    			Preferences.set("ExportShoebox.TierOrder", currentTierOrder, transcription);
    			break;
    		}
    	} 
    	*/   	
    }

    private void setDefaultNumOfChars() {
        numCharTF.setEnabled(true);
        numCharTF.setBackground(Constants.SHAREDCOLOR4);

        if ((numCharTF.getText() != null) ||
                (numCharTF.getText().length() == 0)) {
            numCharTF.setText("" + NUM_CHARS);
        }
    }

    private void setEnabledAllUnicode(boolean enable) {
        allUnicodeCB.setSelected(false);
        allUnicodeCB.setEnabled(enable);
    }

    private void setEnabledAutoGenerate(boolean enable) {
        generateMarkersCB.setSelected(false);
        generateMarkersCB.setEnabled(enable);
    }

    private List<TierImpl> getHierarchicallySortedTiers(TranscriptionImpl transcription) {
        // for each root tier, find dependency tree.
        // store in a List with Lists, one for each root.
        // take the largest tier tree first, this is likely to be the interlinear tree
        List<List<TierImpl>> tierTrees = new ArrayList<List<TierImpl>>();
        List<TierImpl> sortedTiers = new ArrayList<TierImpl>();

        List<TierImpl> topTiers = transcription.getTopTiers();

        for (int i = 0; i < topTiers.size(); i++) {
            TierImpl topTier = topTiers.get(i);
            tierTrees.add(getTierTree(topTier));
        }

        Collections.sort(tierTrees, new ListComparator());

        for (int j = 0; j < tierTrees.size(); j++) {
            sortedTiers.addAll(tierTrees.get(j));
        }

        return sortedTiers;
    }

    private void setShoeboxMarkerRB() {
        String stringPref = Preferences.getString("LastUsedShoeboxExport", null);

        if (stringPref == null) {
            tierNamesRB.setSelected(true);
        } else if (stringPref.equalsIgnoreCase("markers")) {
            specRB.setSelected(true);

            //typButton.setEnabled(false);
            fieldSpecButton.setEnabled(true);

            List<?> mo = Preferences.getList("LastUsedShoeboxMarkers", null);

            if (mo != null) {
                markers = (List<MarkerRecord>) mo;
            }
        } else if (stringPref.equalsIgnoreCase("typ")) {
            typeRB.setSelected(true);

            String luTypFile = Preferences.getString("LastUsedShoeboxTypFile", null);

            if (luTypFile != null) {
                typField.setText(luTypFile);
            }

            //typButton.setEnabled(true);
        } else {
            tierNamesRB.setSelected(true);
        }
    }

    private void autoGenerateMarkerFile() {
        // generate marker records for each tier.
        // only marker, parent marker and stereotype have to be set, rest is default
        List<MarkerRecord> markerRecords = new ArrayList<MarkerRecord>();

        try {
            List<TierImpl> tiers = transcription.getTiers();

            for (int i = 0; i < tiers.size(); i++) {
                TierImpl t = tiers.get(i);

                MarkerRecord mkrRecord = new MarkerRecord();
                mkrRecord.setMarker(t.getName());

                if (t.hasParentTier()) {
                    mkrRecord.setParentMarker(t.getParentTier().getName());

                    if (t.getLinguisticType() != null) {
                        int stereotype = t.getLinguisticType().getConstraints()
                                          .getStereoType();

                        if ((stereotype == Constraint.SYMBOLIC_SUBDIVISION) ||
                                (stereotype == Constraint.TIME_SUBDIVISION) ||
                                (stereotype == Constraint.INCLUDED_IN)) {
                            //mkrRecord.setStereoType(Constraint.publicStereoTypes[2]);
                            mkrRecord.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION]);
                        } else if (stereotype == Constraint.SYMBOLIC_ASSOCIATION) {
                            //mkrRecord.setStereoType(Constraint.publicStereoTypes[3]);
                            mkrRecord.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);
                        }
                    }
                }

                mkrRecord.setCharset(MarkerRecord.UNICODESTRING);
                mkrRecord.setParticipantMarker(false);
                mkrRecord.setExcluded(false);

                markerRecords.add(mkrRecord);
            }

            // store in mkr file with name of transcription, next to eaf
            // dec 2006 HS: by default the .mkr file will now be saved next to the export file
            String fileName = transcription.getPathName();

            if (exportFileName != null) {
                fileName = exportFileName.substring(0,
                        exportFileName.lastIndexOf("."));
            } else if (fileName.toLowerCase().endsWith(".eaf")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }

            fileName += ".mkr";

            final File newSaveFile = new File(fileName);

            if (newSaveFile != null) {
                if (newSaveFile.exists()) {
                    int answer = JOptionPane.showConfirmDialog(null,
                            ElanLocale.getString("Message.Overwrite") + "\n" +
                            fileName,
                            ElanLocale.getString("SaveDialog.Message.Title"),
                            JOptionPane.YES_NO_OPTION);

                    if (answer == JOptionPane.NO_OPTION) {
                        return;
                    }
                }

                FileOutputStream out = new FileOutputStream(newSaveFile);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                            out, "UTF-8"));

                Iterator<MarkerRecord> markerIter = markerRecords.iterator();

                while (markerIter.hasNext()) {
                    writer.write(markerIter.next().toString());
                }

                writer.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Checks the contents of marker input fields and next the existence of the
     * designated files.
     *
     * @return true if the files exist, false otherwise
     */
    private boolean checkMarkerFields() {
        if (typeRB.isSelected() &&
                ((typField.getText() == null) ||
                (typField.getText().length() == 0))) {
            showError(ElanLocale.getString("ImportDialog.Message.SpecifyType"));

            return false;
        }

        if (typeRB.isSelected()) {
            File tf = new File(typField.getText());

            if (!tf.exists()) {
                showError(ElanLocale.getString("ImportDialog.Message.NoType"));

                return false;
            } else {
                try {
                    ShoeboxTypFile typFile = new ShoeboxTypFile(tf);
                    databaseType = typFile.getDatabaseType();
                } catch (Exception e) {
                }
            }
        } else {
            databaseType = dbTypField.getText();
        }

        return true;
    }

    private void chooseTyp() {
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("ImportDialog.Title.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("ImportDialog.Approve"), 
        		null, FileExtension.SHOEBOX_TYP_EXT, false, "LastUsedShoeboxTypDir", FileChooser.FILES_ONLY, null);

        File f = chooser.getSelectedFile();
        if (f != null) {
        	typField.setText(f.getAbsolutePath());
        }
    }

    //******************************
    // actual export methods from here, for the time being
    //******************************
    /**
     * The actual writing.
     *
     * @param fileName path to the file, not null
     * @param orderedTiers tier names, ordered by the user, min size 1
     * @param charsPerLine num of chars per line if linewrap is selected
     * @param timeFormat DOCUMENT ME!
     * @param correctTimes DOCUMENT ME!
     *
     * @return true if all went well, false otherwise
     */
    private boolean doExport(final String fileName, final List<String> orderedTiers,
        final int charsPerLine, final int timeFormat, final boolean correctTimes) {
        int markerSource = ToolboxEncoderInfo.TIERNAMES; // default

        if (typeRB.isSelected()) {
            markerSource = ToolboxEncoderInfo.TYPFILE;
            Preferences.set("LastUsedShoeboxExport", "typ", null);
            Preferences.set("LastUsedShoeboxTypFile", typField.getText(), null);
        } else if (specRB.isSelected()) {
            markerSource = ToolboxEncoderInfo.DEFINED_MARKERS;
            Preferences.set("LastUsedShoeboxExport", "markers", null);

            if (markers != null) {
                Preferences.set("LastUsedShoeboxMarkers", markers, null);
            }
        } else {
            Preferences.set("LastUsedShoeboxExport", "", null);
        }

        ToolboxEncoderInfo tbEncoderInfo = new ToolboxEncoderInfo(charsPerLine,
                markerSource, timeFormat);
        tbEncoderInfo.setCorrectAnnotationTimes(correctTimes);

        if (databaseType != null) {
            tbEncoderInfo.setDatabaseType(databaseType);
        }

        if (typeRB.isSelected()) {
            //   tbEncoderInfo.setDatabaseType(databaseType);
            if (allUnicodeCB.isSelected()) {
                tbEncoderInfo.setAllUnicode(true);
            }
        } else if (specRB.isSelected()) {
            tbEncoderInfo.setMarkers(markers);
        }

        if (fileName != null) {
        	List<TierImpl> tiers = transcription.getTiersWithIds(orderedTiers);
        	
            try {
                ACMTranscriptionStore.getCurrentTranscriptionStore()
                                     .storeTranscriptionIn(transcription,
                    tbEncoderInfo, tiers, fileName,
                    TranscriptionStore.SHOEBOX);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                    "(" + ioe.getMessage() + ")",
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        return true;
    }

    /**
     * Shows an error dialog.
     *
     * @param message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
    }

    private void specifyFieldSpecs() {
        ShoeboxMarkerDialog smd = new ShoeboxMarkerDialog(null, true);
        smd.setVisible(true);
        markers = smd.getMarkers();
    }

    //***********************

    class ListComparator implements Comparator<List<?>> {
        /**
         * Compares Lists, on basis of their size. The largest one comes
         * first
         *
         * @see java.util.Comparator#compare(java.lang.Object,
         *      java.lang.Object)
         */
		@Override
		public int compare(List<?> l0, List<?> l1) {
            if (l0.size() < l1.size()) {
                return 1;
            }

            if (l0.size() > l1.size()) {
                return -1;
            }
            return 0;
        }
    }
}
