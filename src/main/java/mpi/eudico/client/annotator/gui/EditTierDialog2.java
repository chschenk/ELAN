package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.prefs.gui.RecentLanguagesBox;
import mpi.eudico.client.annotator.tier.TierTableModel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.im.ImUtil;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.EmptyStringComparator;


/**
 * A dialog to create, change, delete or import tiers. This is an extended
 * version of EditTierDialog. An information table has been added showing info
 * on the current tiers. Tabs have been introduced to switch between add,
 * change, delete or import  mode. The import mode is also new: it enables the
 * import of tiers (with associated  linguistic types and cv's, but without
 * annotations) from an .eaf or .etf.
 *
 * @author Han Sloetjes
 * @version 2.0
 */
@SuppressWarnings("serial")
public class EditTierDialog2 extends ClosableDialog implements ActionListener,
    ItemListener, ChangeListener, ListSelectionListener {
    /** the add mode */
    public static final int ADD = 0;

    /** the change mode */
    public static final int CHANGE = 1;

    /** the delete mode */
    public static final int DELETE = 2;

    /** the import mode */
    public static final int IMPORT = 3;

    /** value for no parent */
    final private String none = "none";

    //private Frame frame;
    private TranscriptionImpl transcription;
    private TierImpl tier = null;
    private Tier oldParentTier;
    private String oldTierName;
    private String oldParentTierName;
    private String oldParticipant;
    private String oldAnnotator;
    private LinguisticType oldLingType;
    private Locale oldLocale;
    private Locale[] langs;
    private int mode = ADD;
    private boolean singleEditMode = false;
    private List<TierImpl> tiers;

    // ui
    private JLabel titleLabel;
    private JPanel tablePanel;
    private JTable tierTable;
    private TierTableModel model;
    private JTabbedPane tabPane;

    // ui elements for edit panel
    private JPanel editPanel;
    private JLabel selectTierLabel;
    private JLabel tierNameLabel;
    private JComboBox currentTiersComboBox;
    private JTextField tierNameTextField;
    private JLabel participantLabel;
    private JTextField participantTextField;
    private JLabel annotatorLabel;
    private JTextField annotatorTextField;
    private JLabel lingTypeLabel;
    private JComboBox lingTypeComboBox;
    private JLabel parentLabel;
    private JComboBox parentComboBox;
    private JLabel languageLabel;
    private JComboBox languageComboBox;
    private JLabel mlLanguageLabel;
    private RecentLanguagesBox mlLanguageBox;
    private JButton advancedButton;
    private HashMap<String, Object> currentProps = new HashMap<String, Object>(10);

    // import panel
    private JPanel importPanel;
    private JLabel importSourceLabel;
    private JTextField importSourceTF;
    private JButton importSourceButton;
    private JButton changeButton;
    private JButton cancelButton;
    private JPanel buttonPanel;

    /**
     * Creates a new EditTierDialog2 instance
     *
     * @param parentFrame the parent ELAN frame
     * @param modal the modal flag: true
     * @param theTranscription the transcription to work on
     * @param editMode the edit mode: ADD, CHANGE, DELETE or IMPORT
     * @param tier the tier to select in the ui initially
     */
    public EditTierDialog2(Frame parentFrame, boolean modal,
        Transcription theTranscription, int editMode, TierImpl tier) {
        super(parentFrame, modal);

        //frame = parentFrame;
        transcription = (TranscriptionImpl) theTranscription;

        if ((editMode >= ADD) && (editMode <= IMPORT)) {
            mode = editMode;
        }

        initComponents();
        extractCurrentTiers();

        if (tier != null) {
            this.tier = tier;

            String name = tier.getName();
            singleEditMode = true;

            if (currentTiersComboBox != null) {
                currentTiersComboBox.setSelectedItem(name);
            }
        }

        updateLanguageComboBox();
        updateLocale();
        updateForMode();
        updateUIForTier((String) currentTiersComboBox.getSelectedItem());
        postInit();

        if (editMode == ADD) {
            tierNameTextField.requestFocus();
        } else if (editMode == IMPORT) {
        	// this forces proper rendering of the import panel, 
        	// otherwise a textfield of the editPanel is visible on top 
        	// of the importPanel ??
			editPanel.setVisible(false);
			importSourceButton.requestFocus();
        } else {
            currentTiersComboBox.requestFocus();
        }
        
    }
    
    /**
     * Initializes the ui components.
     */
    private void initComponents() {
        langs = ImUtil.getLanguages(this);

        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        tablePanel = new JPanel();
        tablePanel.setLayout(new GridBagLayout());
        model = new TierTableModel(transcription.getTiers());
        tierTable = new JTable(model);
        tierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
        EmptyStringComparator emptyComp = new EmptyStringComparator();
        for (int i = 1; i < tierTable.getColumnCount(); i++) {
        	rowSorter.setComparator(i, emptyComp);
        }
        tierTable.setRowSorter(rowSorter);

        JScrollPane tableScrollPane = new JScrollPane(tierTable);
        Dimension size = new Dimension(300, 120);
        tableScrollPane.setMinimumSize(size);
        tableScrollPane.setPreferredSize(size);

        tabPane = new JTabbedPane();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        getContentPane().add(titleLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        tablePanel.add(tableScrollPane, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(tablePanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 6, 6, 6);
        gbc.weightx = 1.0;

        //gbc.weighty = 1.0;
        getContentPane().add(tabPane, gbc);

        // edit panel
        editPanel = new JPanel(new GridBagLayout());
        selectTierLabel = new JLabel();
        currentTiersComboBox = new JComboBox();
        currentTiersComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        tierNameLabel = new JLabel();
        tierNameTextField = new JTextField();
        participantLabel = new JLabel();
        participantTextField = new JTextField();
        annotatorLabel = new JLabel();
        annotatorTextField = new JTextField();
        lingTypeLabel = new JLabel();
        lingTypeComboBox = new JComboBox();
        parentLabel = new JLabel();
        parentComboBox = new JComboBox();
        languageLabel = new JLabel();
        languageComboBox = new JComboBox();
        mlLanguageLabel = new JLabel();
        mlLanguageBox = new RecentLanguagesBox(null);
        mlLanguageBox.addNoLanguageItem();
        advancedButton = new JButton();

        GridBagConstraints lgbc = new GridBagConstraints();
        lgbc.anchor = GridBagConstraints.WEST;
        lgbc.insets = insets;
        editPanel.add(selectTierLabel, lgbc);

        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.gridx = 1;
        rgbc.fill = GridBagConstraints.HORIZONTAL;
        rgbc.anchor = GridBagConstraints.WEST;
        rgbc.insets = insets;
        rgbc.weightx = 1.0;
        editPanel.add(currentTiersComboBox, rgbc);
        lgbc.gridy = 1;
        editPanel.add(tierNameLabel, lgbc);
        rgbc.gridy = 1;
        editPanel.add(tierNameTextField, rgbc);
        lgbc.gridy = 2;
        editPanel.add(participantLabel, lgbc);
        rgbc.gridy = 2;
        editPanel.add(participantTextField, rgbc);
        lgbc.gridy = 3;
        editPanel.add(annotatorLabel, lgbc);
        rgbc.gridy = 3;
        editPanel.add(annotatorTextField, rgbc);
        lgbc.gridy = 4;
        editPanel.add(parentLabel, lgbc);
        rgbc.gridy = 4;
        editPanel.add(parentComboBox, rgbc);
        lgbc.gridy = 5;
        editPanel.add(lingTypeLabel, lgbc);
        rgbc.gridy = 5;
        editPanel.add(lingTypeComboBox, rgbc);
        lgbc.gridy = 6;
        editPanel.add(languageLabel, lgbc);
        rgbc.gridy = 6;
        editPanel.add(languageComboBox, rgbc);
        lgbc.gridy = 7;
        editPanel.add(mlLanguageLabel, lgbc);
        rgbc.gridy = 7;
        editPanel.add(mlLanguageBox, rgbc);
        lgbc.gridy = 8;
        editPanel.add(advancedButton, lgbc);

        // import panel
        importPanel = new JPanel(new GridBagLayout());
        importSourceLabel = new JLabel();
        importSourceTF = new JTextField();
        importSourceTF.setEditable(false);
        importSourceButton = new JButton();
        importSourceButton.addActionListener(this);

        gbc = new GridBagConstraints();
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        importPanel.add(importSourceLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        importPanel.add(importSourceTF, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        importPanel.add(importSourceButton, gbc);

        tabPane.addTab(ElanLocale.getString("Button.Add"), null);
        tabPane.addTab(ElanLocale.getString("Button.Change"),
            null);
        tabPane.addTab(ElanLocale.getString("Button.Delete"),
            null);
        tabPane.addTab(ElanLocale.getString("Button.Import"),
            importPanel);

        if (mode < IMPORT) {
            tabPane.setComponentAt(mode, editPanel);
        } else {
            tabPane.setComponentAt(0, editPanel);
        }

        tabPane.setSelectedIndex(mode);
        tabPane.addChangeListener(this);
        advancedButton.addActionListener(this);

        // buttons
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        changeButton = new JButton();
        changeButton.addActionListener(this);
        cancelButton = new JButton();
        cancelButton.addActionListener(this);
        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);    

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        getContentPane().add(buttonPanel, gbc);
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();

        int w = 550;
        int h = 400;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        setLocationRelativeTo(getParent());

        //setResizable(false);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    private void updateLocale() {
        tablePanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "EditTierDialog.Label.CurrentTiers")));
        tierNameLabel.setText(ElanLocale.getString(
                "EditTierDialog.Label.TierName"));
        participantLabel.setText(ElanLocale.getString(
                "EditTierDialog.Label.Participant"));
        annotatorLabel.setText(ElanLocale.getString("EditTierDialog.Label.Annotator"));
        lingTypeLabel.setText(ElanLocale.getString(
                "EditTierDialog.Label.LinguisticType"));
        parentLabel.setText(ElanLocale.getString("EditTierDialog.Label.Parent"));
        languageLabel.setText(ElanLocale.getString(
                "EditTierDialog.Label.Language"));
        mlLanguageLabel.setText(ElanLocale.getString(
                "EditTierDialog.Label.ContentLanguage"));
        cancelButton.setText(ElanLocale.getString("Button.Close"));
        importSourceLabel.setText("<html>" +
            ElanLocale.getString("EditTierDialog.Label.ImportSource") +
            "</html>");
        importSourceButton.setText(ElanLocale.getString("Button.Browse"));
        advancedButton.setText(ElanLocale.getString("EditTierDialog.Label.Advanced"));
    }

    /**
     * Updates texts and enables/disables components for the current  edit
     * mode.
     */
    private void updateForMode() {
        switch (mode) {
        case ADD:
        	tierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setTitle(ElanLocale.getString("EditTierDialog.Title.Add"));
            selectTierLabel.setText("");

            currentTiersComboBox.setEnabled(false);

            //currentTiersComboBox.setVisible(false);
            changeButton.setText(ElanLocale.getString(
                    "Button.Add"));
            parentComboBox.setEnabled(true);
            tierNameTextField.setEditable(true);
            participantTextField.setEditable(true);
            annotatorTextField.setEditable(true);
            lingTypeComboBox.setEnabled(true);
            languageComboBox.setEnabled(true);
            mlLanguageBox.setEnabled(true);
            tierNameTextField.setText("");
            participantTextField.setText("");
            annotatorTextField.setText("");
            advancedButton.setEnabled(true);

            break;

        case CHANGE:
        	tierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setTitle(ElanLocale.getString("EditTierDialog.Title.Change"));
            selectTierLabel.setText(ElanLocale.getString(
                    "EditTierDialog.Label.ChangeTier"));
            selectTierLabel.setVisible(true);
            currentTiersComboBox.setEnabled(true);
            currentTiersComboBox.setVisible(true);
            currentTiersComboBox.requestFocus();
            changeButton.setText(ElanLocale.getString(
                    "Button.Change"));
            parentComboBox.setEnabled(false);
            tierNameTextField.setEditable(true);
            participantTextField.setEditable(true);
            annotatorTextField.setEditable(true);
            lingTypeComboBox.setEnabled(true);
            languageComboBox.setEnabled(true);
            mlLanguageBox.setEnabled(true);
            advancedButton.setEnabled(true);

            break;

        case DELETE:
        	tierTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            setTitle(ElanLocale.getString("EditTierDialog.Title.Delete"));
            selectTierLabel.setText(ElanLocale.getString(
                    "EditTierDialog.Label.DeleteTier"));
            changeButton.setText(ElanLocale.getString(
                    "Button.Delete"));
            tierNameTextField.setEditable(false);
            participantTextField.setEditable(false);
            annotatorTextField.setEditable(false);
            parentComboBox.setEnabled(false);
            lingTypeComboBox.setEnabled(false);
            languageComboBox.setEnabled(false);
            mlLanguageBox.setEnabled(false);
            selectTierLabel.setVisible(true);
            currentTiersComboBox.setEnabled(true);
            currentTiersComboBox.setVisible(true);
            currentTiersComboBox.requestFocus();
            advancedButton.setEnabled(false);

            break;

        case IMPORT:
        	tierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setTitle(ElanLocale.getString("EditTierDialog.Title.Import"));
            changeButton.setText(ElanLocale.getString(
                    "Button.Import"));
            changeButton.requestFocus();

            break;
        }

        titleLabel.setText(getTitle());
    }

    /**
     * Fill the tiers combobox with the currently present tiers.
     */
    private void extractCurrentTiers() {
        currentTiersComboBox.removeItemListener(this);
        currentTiersComboBox.removeAllItems();
        tiers = transcription.getTiers();

        if (tiers == null) {
            tiers = new ArrayList<TierImpl>();

            return;
        }

        Iterator<TierImpl> tierIt = tiers.iterator();

        while (tierIt.hasNext()) {
            TierImpl t = tierIt.next();
            currentTiersComboBox.addItem(t.getName());
        }

        if (currentTiersComboBox.getItemCount() > 0) {
            currentTiersComboBox.setSelectedIndex(0);
            tier = tiers.get(0);
        }

        currentTiersComboBox.addItemListener(this);
    }

    /**
     * Again extract the tiers from the transcription after an add, change or
     * delete operation.
     */
    private void reextractTiers() {
        extractCurrentTiers();

        if (currentTiersComboBox.getItemCount() > 0) {
            currentTiersComboBox.setSelectedIndex(0);
            String name = (String) currentTiersComboBox.getSelectedItem();
            updateUIForTier(name);
        } else {
            tierNameTextField.setText("");
            participantTextField.setText("");
            annotatorTextField.setText("");
        }

        if (mode == ADD) {
            tierNameTextField.setText("");
            participantTextField.setText("");
            annotatorTextField.setText("");
        }

        if (model != null) {
        	tierTable.getSelectionModel().removeListSelectionListener(this);
            model.removeAllRows();

            Iterator<TierImpl> tierIt = tiers.iterator();

            while (tierIt.hasNext()) {
                TierImpl t = tierIt.next();
                model.addRow(t);
            }
            tierTable.getSelectionModel().addListSelectionListener(this);
        }
    }

    /**
     * Empties and refills the Linguistic Type menu with types that are not
     * excluded by the current parent tier choice.
     */
    private void fillLingTypeMenu() {
        lingTypeComboBox.removeItemListener(this);
        lingTypeComboBox.removeAllItems();
        // Aug 2006: in Change mode only add Ling. Types:
        // - with the same stereotype as the current stereotype if there are already annotations on the tier
        // - else if there are dependent tiers and the current tier is time alignable check if any of the 
        // dependent tiers is time-alignable: if so only add time-alignable types else allow all (if there is a parent tier)
        // - if there are no dependent tiers allow all (if there is a parent tier)
        int curStereoType = -1;
        boolean onlySameStereo = false;
        boolean onlyTimeAlignable = false;
        boolean excludeTimeAlignable = false;
        if (mode == CHANGE) {
            if (tier != null && tier.getNumberOfAnnotations() > 0) {
                onlySameStereo = true;
            }
            if (tier != null && tier.getLinguisticType().getConstraints() != null) {
                curStereoType = tier.getLinguisticType().getConstraints().getStereoType();
            }
            if (!onlySameStereo && curStereoType >= 0) {
                final int size = tier.getDependentTiers().size();
				if (size > 0) {

                    // if there is at least one time alignable child only add time alignable
                    TierImpl ch;
                    for (int i = 0; i < size; i++) {
                        ch = tier.getDependentTiers().get(i);
                        if (ch.isTimeAlignable()) {
                            onlyTimeAlignable = true;
                            break;
                         }
                    }
                }
            }
        }
        
        TierImpl parentTier = (transcription.getTierWithId((String) parentComboBox.getSelectedItem()));

        Constraint parentConstraint = null;

        if (parentTier != null) {
            parentConstraint = parentTier.getLinguisticType().getConstraints();

            if (parentConstraint != null) {
                if ((parentConstraint.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) ||
                        (parentConstraint.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)) {
                    excludeTimeAlignable = true;
                }
            }
        }

        for (LinguisticType lt : transcription.getLinguisticTypes()) {
            String ltName = lt.getLinguisticTypeName();

            if (parentTier == null) { // only unconstrained types

                if (lt.getConstraints() != null) {
                    continue;
                }
            }
            
            if (excludeTimeAlignable && (lt.getConstraints() != null) &&
                    ((lt.getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION) || 
                            lt.getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {
                continue;
            }

            if (parentTier != null) { // only constrained types

                if (lt.getConstraints() == null) {
                    continue;
                }
            }
            // special cases change mode
            if (onlySameStereo) {
                if (lt.getConstraints() != null && lt.getConstraints().getStereoType() != curStereoType) {
                    continue;
                }
            }
            
            if (onlyTimeAlignable && (lt.getConstraints() != null) &&
                    ((lt.getConstraints().getStereoType() != Constraint.TIME_SUBDIVISION) && 
                            lt.getConstraints().getStereoType() != Constraint.INCLUDED_IN)) {
                continue;
            }   
            
            lingTypeComboBox.addItem(ltName);
        }

        // set selected the current type of the selected tier
        String tierName = (String) currentTiersComboBox.getSelectedItem();

        if (tierName != null) {
            TierImpl t2 = transcription.getTierWithId(tierName);

            if (t2 != null) {
                LinguisticType type = t2.getLinguisticType();

                if (type != null) {
                    lingTypeComboBox.setSelectedItem(type.getLinguisticTypeName());
                }
            }
        }

        lingTypeComboBox.addItemListener(this);

        if (lingTypeComboBox.getModel().getSize() <= 0) {
            changeButton.setEnabled(false);
        } else {
            changeButton.setEnabled(true);
        }
    }

    /**
     * Fills the parent tier combobox with the potential parent tiers for the
     * specified tier.
     */
    private void fillParentComboBox() {
        parentComboBox.removeItemListener(this);
        parentComboBox.removeAllItems();
        parentComboBox.addItem(none);

        if ((tier != null) && (mode != ADD)) {
        	// HS 31-07-2012
            // List candidateParents = transcription.getCandidateParentTiers(tier);
        	List<Tier> candidateParents = getCandidateParentTiers(tier);

            for (Tier t : candidateParents) {
                parentComboBox.addItem(t.getName());            	
            }

            if (tier.hasParentTier()) {
                parentComboBox.setSelectedItem(tier.getParentTier().getName());
            }
        } else if (mode == ADD) {

            for (TierImpl t : tiers) {
                parentComboBox.addItem(t.getName());
            }

            parentComboBox.setSelectedItem(none);
        }

        parentComboBox.addItemListener(this);
    }
    
    /**
     * Returns a list of possible parent tiers from the transcription.
     * 
     * @param child the candidate child
     * @return a list of possible parents
     */
    private List<Tier> getCandidateParentTiers(TierImpl child) {
    	if (child == null) {
    		return null;
    	}
    	
    	List<Tier> parents = new ArrayList<Tier>();
    	Constraint c = child.getLinguisticType().getConstraints();
    	if (c == null) {
    		return parents;//empty list
    	} else {
    		int stereoType = c.getStereoType();
    		if (stereoType == Constraint.SYMBOLIC_ASSOCIATION || stereoType == Constraint.SYMBOLIC_SUBDIVISION) {
                Iterator<TierImpl> tierIt = tiers.iterator();

                while (tierIt.hasNext()) {
                    TierImpl t = tierIt.next();
                    if (t != child) {
                    	parents.add(t);
                    }
                }
    		} else if (stereoType == Constraint.TIME_SUBDIVISION || stereoType == Constraint.INCLUDED_IN){
                Iterator<TierImpl> tierIt = tiers.iterator();

                while (tierIt.hasNext()) {
                    TierImpl t = tierIt.next();
                    if (t != child) {
                    	if (t.getLinguisticType().getConstraints() == null || 
                    			t.getLinguisticType().getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION ||
                    			t.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN) {
                    		parents.add(t);
                    	}
                    }
                }
    		}
    	}
    	
    	return parents;
    }

    /**
     * Gets the Locale of the currently selected tier and tries to set this
     * Locale as the selected item in the language combo box.<br>
     * 
     * @version 10-2016 unclear why this box would have to be emptied and 
     * filled again on every update, this is now changed
     */
    private void updateLanguageComboBox() { 
    	if (languageComboBox.getItemCount() == 0) {
	        if (langs != null) {
	            for (int i = 0; i < langs.length; i++) {
	                if (i == 0 && langs[i] == Locale.getDefault()) {
	                    languageComboBox.addItem(langs[i].getDisplayName() + " (System default)");
	                } else {
		                languageComboBox.addItem(langs[i].getDisplayName());
		            }
		        }
	        }
	    	
	        String none = ElanLocale.getString("EditTierDialog.Label.None");
	        languageComboBox.insertItemAt(none, 0);
    	}
    	
        if (tier != null) {
            Locale l = tier.getDefaultLocale();

            if (l != null) {
                /*
                   List al = Arrays.asList(langs);
                   if (!al.contains(l)) {
                       languageComboBox.addItem(l.getDisplayName());
                   }
                 */
            	if (l.equals(Locale.getDefault()) && 
            			((String) languageComboBox.getItemAt(1)).startsWith(l.getDisplayName())) {
            		languageComboBox.setSelectedIndex(1);
            	} else {
            		languageComboBox.setSelectedItem(l.getDisplayName());
            	}
            } else {
                languageComboBox.setSelectedIndex(0);
            }
        }
    }

    void updateMultiLingualComboBox() {
        mlLanguageBox.setSelectedItem(tier.getLangRef());
    }
    
    /**
     * Updates ui elements for a certain selected tier, e.g. after committing a
     * change to the set of tiers.
     *
     * @param name the name of the selected tier
     */
    private void updateUIForTier(String name) {
        if (name != null) {
            tier = transcription.getTierWithId(name);

            if (tier != null) {
            	if (currentTiersComboBox.getSelectedItem() != 
            		tier.getName()) {
            		currentTiersComboBox.setSelectedItem(name);
            	}
                oldParentTier = tier.getParentTier();

                if (oldParentTier != null) {
                    oldParentTierName = tier.getParentTier().getName();
                } else {
                    oldParentTierName = none;
                }

                oldLingType = tier.getLinguisticType();
                oldLocale = tier.getDefaultLocale();
                oldTierName = tier.getName();
                oldParticipant = tier.getParticipant();
                oldAnnotator = tier.getAnnotator();

                if (mode != ADD) {
                    tierNameTextField.setText(oldTierName);
                    participantTextField.setText(oldParticipant);
                    annotatorTextField.setText(oldAnnotator);

                    if (mode == CHANGE) {
                        if (tier.getNumberOfAnnotations() == 0) {
                            parentComboBox.setEnabled(true);
                        }
                    }
                }
            }

            fillParentComboBox();
            fillLingTypeMenu();
            updateLanguageComboBox();
            updateMultiLingualComboBox();
            
            // update table
            if (model != null) {
            	tierTable.getSelectionModel().removeListSelectionListener(this);
                int col = model.findColumn(TierTableModel.NAME);

                for (int i = 0; i < model.getRowCount(); i++) {
                    if (name.equals(model.getValueAt(i, col))) {
                    	if (tierTable.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
                    		tierTable.getSelectionModel().setLeadSelectionIndex(tierTable.convertRowIndexToView(i));
                    	} else {
                    		tierTable.addRowSelectionInterval(tierTable.convertRowIndexToView(i),
                    				tierTable.convertRowIndexToView(i));
                    		
                    	}

                        break;
                    }
                }
                tierTable.getSelectionModel().addListSelectionListener(this);
            }
        } else {
            fillParentComboBox();
            fillLingTypeMenu();
        }
    }

    /**
     * Adds a new tier to the transcription.
     *
     * @param tierName the name of the new tier
     * @param parentTier the parent tier, can be null
     * @param lingType the Linguistic Type name
     * @param participant the participant value for the tier
     * @param annotator the annotator of the tier
     * @param locale the default language for the tier
     * @param langRef the langRef (URL) for the desired language
     */
    private void doAdd(String tierName, Tier parentTier, String lingType,
        String participant, String annotator, Locale locale, String langRef) {
    	
        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.ADD_TIER);

        Object receiver = transcription;
        Object[] args = new Object[7];
        args[0] = tierName;
        args[1] = parentTier;
        args[2] = lingType;
        args[3] = participant;
        args[4] = annotator;
        args[5] = locale;
        args[6] = langRef;

        c.execute(receiver, args);
        
        // store some preferences
        applyAttributeSettings(); 

        // update the dialog ui
        reextractTiers();
        updateUIForTier(null);
    }

    /**
     * Changes properties of a tier.
     *
     * @param tierName new name of the tier
     * @param parentTier the parent tier
     * @param lingType the linguistic type
     * @param participant the participant
     * @param annotator the annotator of the tier
     * @param locale the locale
     */
    private void doChange(String tierName, Tier parentTier, String lingType,
        String participant, String annotator, Locale locale, String langRef) {
    	
    	applyAttributeSettings();
    	
        // double check on parent and type
        if (tier.getNumberOfAnnotations() > 0) {
            if (((parentTier != null) && (oldParentTier == null)) ||
                    ((parentTier == null) && (oldParentTier != null)) ||
                    (parentTier != oldParentTier)) {
                parentTier = oldParentTier;
            }

            if (getStereoTypeForTypeName(lingType) != getStereoTypeForType(
                        oldLingType)) {
                lingType = oldLingType.getLinguisticTypeName();
            }
        }
        
        String oldLangRef = tier.getLangRef();

        // check whether something has changed
        if (!tierName.equals(oldTierName) ||
                ((parentTier != null) && (oldParentTier != null) &&
                (parentTier != oldParentTier)) ||
                !lingType.equals(oldLingType.getLinguisticTypeName()) ||
                !participant.equals(oldParticipant) ||
                !annotator.equals(oldAnnotator) ||
                ( (locale == null && oldLocale != null) || ((locale != null) && (locale != oldLocale)) )  ||
                (langRef == null && oldLangRef != null) || (langRef != null && !langRef.equals(oldLangRef))
                ) {
            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.CHANGE_TIER);

            Object receiver = tier;
            Object[] args = new Object[7];
            args[0] = tierName;
            args[1] = parentTier;
            args[2] = lingType;
            args[3] = participant;
            args[4] = annotator;
            args[5] = locale;
            args[6] = langRef;

            c.execute(receiver, args);

            if (singleEditMode) {
                // dispose
                dispose();
            } else {
                // update the dialog ui
                reextractTiers();
            }
        } else {
            //System.out.println("no change");
        }
    }

    /**
     * Actually deletes the tier.
     */
    private void doDelete() {
        if (tier != null) {
        	List<TierImpl> selectedTiers = new ArrayList<TierImpl>();   
    
        	int[] selectedRows = tierTable.getSelectedRows();
        	if(selectedRows.length > 0) {
        		for (int i = 0; i < selectedRows.length; i++) {
        			selectedRows[i] = tierTable.convertRowIndexToModel(selectedRows[i]);
        		}
        		int column = model.findColumn(TierTableModel.NAME);
        		for (int selectedRow : selectedRows) {
        			String tierName = (String) model.getValueAt(selectedRow, column);
        			TierImpl selectedTier = transcription.getTierWithId(tierName);
        			selectedTiers.add(selectedTier);
        		}
        	} else {
        		selectedTiers.add(tier);
        	}
        	
        	StringBuilder mesBuf = new StringBuilder();       	
        	Iterator<TierImpl> selIt = selectedTiers.iterator();
        	while(selIt.hasNext()) {
        		TierImpl selT = selIt.next();
        		mesBuf.append(selT.getName() + "\n");
        		List<TierImpl> depTiers = selT.getDependentTiers();
    			if ((depTiers != null) && (depTiers.size() > 0)) {
    				StringBuilder tmpBuf = new StringBuilder();
                    
                    Iterator<TierImpl> depIt = depTiers.iterator();
                    while (depIt.hasNext()) {
                    	Tier t = depIt.next();
                    	if(!selectedTiers.contains(t)) {
                            tmpBuf.append("-   ");

                            tmpBuf.append(t.getName() + "\n");
                    	}
                    }
                    
                    if(tmpBuf.length() > 0) {
                    	mesBuf.append("\n"+ElanLocale.getString(
                    			"EditTierDialog.Message.AlsoDeleted") + "\n\n");
                    	mesBuf.append(tmpBuf);
                    }
                }
        	}
        	
        	JPanel panel = new JPanel(new GridBagLayout());
        	
        	JTextArea tierList = new JTextArea(mesBuf.toString());
        	tierList.setEditable(false);
        	tierList.setBackground(panel.getBackground());        	       	        	
        	
        	JScrollPane scrollPane = new JScrollPane(tierList);
        	JLabel label = new JLabel(ElanLocale.getString(
                    "EditTierDialog.Message.ConfirmDelete"));
        	
        	GridBagConstraints gbc = new GridBagConstraints();
        	gbc.anchor =  GridBagConstraints.NORTHWEST;
        	panel.add(label, gbc);
        	
        	gbc.gridy = 1;
        	gbc.insets = new Insets(6,10,6,6);
        	panel.add(scrollPane, gbc);
        	
        	
        	int prefheight = scrollPane.getViewport().getPreferredSize().height;
        	int prefwidth = label.getPreferredSize().width;
        	
        	if(prefheight > this.getPreferredSize().height/2){
        		prefheight = this.getPreferredSize().height/2;
        	}
//        	
//        	if(prefwidth > this.getPreferredSize().width/2){
//        		prefwidth = this.getPreferredSize().width/2;
//        	}
        	
        	scrollPane.getViewport().setPreferredSize(new Dimension(prefwidth, prefheight));        	
        	
            int option = JOptionPane.showConfirmDialog(this, panel,
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.YES_NO_OPTION);        	

            if (option == JOptionPane.YES_OPTION) {
                Object[] args = selectedTiers.toArray();
                Command c = ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.DELETE_TIERS);
                c.execute(transcription, args);

                if (singleEditMode) {
                    // dispose
                    dispose();
                } else {
                    // update the dialog ui
                    reextractTiers();
                }
            }
        }
    }

    /**
     * Imports tiers (without annotations) from an eaf or etf.
     */
    private void doImport() {
        String fileName = importSourceTF.getText();

        if (!isValidFile(fileName)) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("EditTierDialog.Message.SelectValid"),
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.IMPORT_TIERS);
        c.execute(transcription, new Object[] { fileName });

        reextractTiers();
        updateUIForTier(null);
    }

    /**
     * Prompts the user to browse to an eaf or etf file, checks a little  and
     * updates the ui.
     */
    private void promptForImportFile() {
    	ArrayList<String[]> extensions = new ArrayList<String[]>();
    	extensions.add(FileExtension.EAF_EXT);
    	extensions.add(FileExtension.TEMPLATE_EXT);

        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("EditTierDialog.Title.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		extensions, FileExtension.EAF_EXT, false, "LastUsedEAFDir", FileChooser.FILES_ONLY, null);
        
        File eafFile = chooser.getSelectedFile();
        if (eafFile != null) {
        	importSourceTF.setText(eafFile.getAbsolutePath());
        }
    }

    /**
     * Creates a dialog with more tier options, initially user preferences 
     * for the tier.
     */
    private void showAdvancedOptionsDialog() {
        currentProps.clear();
        
        Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
		if (colors == null) {
			colors = new HashMap<String, Color>();
			Preferences.set("TierColors", colors, transcription);
		}
        /* Mod by Mark */
		Map<String, Color> highlightColors = Preferences.getMapOfColor("TierHighlightColors", transcription);
        if(highlightColors == null) {
        	highlightColors = new HashMap<String, Color>();
        	Preferences.set("TierHighlightColors", highlightColors, transcription);
        }
        /* --- END --- */

		Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
		if (fonts == null) {
			fonts = new HashMap<String, Font>();
			Preferences.set("TierFonts", fonts, transcription);
		}
		
		currentProps.put("TierColor", Color.WHITE);
        /* Mod by Mark */
        currentProps.put("TierHighlightColor", Color.WHITE);
        /* --- END --- */
		currentProps.put("TierFont", null);
		
        if (mode == ADD) {
        	String name = tierNameTextField.getText();
        	if (name == null || name.length() == 0) {
        		currentProps.put("TierName", "");
        	} else {
        		currentProps.put("TierName", name);
        	}
        	if (parentComboBox.getSelectedItem() != none) {
        		String pName = (String) parentComboBox.getSelectedItem();
        		Color col = colors.get(pName);
    			if (col != null) {
    				// use the parents color
            		currentProps.put("TierColor", col);
        		}
                /* Mod by Mark */
                col = highlightColors.get(pName);

                if (col != null) {
                    // use the parents color
                    currentProps.put("TierHighlightColor", col);
                }
                /* --- END --- */
            }
        } else {// mode is CHANGE
        	currentProps.put("TierName", currentTiersComboBox.getSelectedItem());

			Color col = colors.get(currentTiersComboBox.getSelectedItem());
			if (col != null) {
        		currentProps.put("TierColor", col);
			} else {
				String pName = (String) parentComboBox.getSelectedItem();
				Color colP = colors.get(pName);
    			if (colP != null) {
    				currentProps.put("TierColor", colP);
    			}				
			}
            /* Mod by Mark */
            if (highlightColors instanceof Map) {
                col = highlightColors.get(currentTiersComboBox.getSelectedItem());

                if (col != null) {
                    currentProps.put("TierHighlightColor", col);
                } else {
                    String pName = (String) parentComboBox.getSelectedItem();
                    Color colP = highlightColors.get(pName);

                    if (colP != null) {
                        currentProps.put("TierHighlightColor", colP);
                    }
                }
            }
			/* --- END --- */
			Font fo = fonts.get(currentTiersComboBox.getSelectedItem());
			if (fo != null) {
				currentProps.put("TierFont", fo);
			}
        }
        
        AdvancedTierOptionsDialog dialog = new AdvancedTierOptionsDialog(this, 
        		ElanLocale.getString("EditTierDialog.Title.Change"), true, 
        		currentProps);
        dialog.setVisible(true); // blocks
        // check the returned map
       	if (dialog.getTierProperties() != null) {// dialog not canceled
           	currentProps.putAll(dialog.getTierProperties());  
        }
    }
    
    private void applyAttributeSettings(){   
    	if(currentProps == null || currentProps.size() == 0){
    		return;
    	}
    	List<TierImpl> tierList = new ArrayList<TierImpl>();    	
    	
    	String participant ;
        String annotator;
        String lingType;
    	
    	//if change
    	if(mode != ADD){
    		participant = tier.getParticipant();
            annotator = tier.getAnnotator();
            lingType = tier.getLinguisticType().getLinguisticTypeName();
    	}else {// add tier
    		 participant = participantTextField.getText();
             annotator = annotatorTextField.getText();
             lingType = (String) lingTypeComboBox.getSelectedItem();
    	}
    	
    	Boolean sameType = (Boolean)currentProps.get("SameType");
    	if(sameType == null){
    		sameType = false;
    	}
		
    	if(sameType){
			if(transcription.getTiersWithLinguisticType(lingType) !=null){
				tierList.addAll(transcription.getTiersWithLinguisticType(lingType));
			}
		}
    	
    	Boolean dependingTiers = (Boolean)currentProps.get("DependingTiers");
    	if(dependingTiers == null){
    		dependingTiers = false;
    	}
		
		if(dependingTiers){
			if(tier != null && tier.getDependentTiers() != null){
				tierList.addAll(tier.getDependentTiers());
			}
		}  
		
		Boolean samePart = (Boolean)currentProps.get("SameParticipants");
    	if(samePart == null){
    		samePart = false;
    	}
    	
		if(samePart){
			if(participant != null){
				List<TierImpl> allTiers = transcription.getTiers();
				for(int i= 0; i<allTiers.size(); i++ ){
					TierImpl t = allTiers.get(i);
					if(t.getParticipant()!= null){
						if(t.getParticipant().equals(participant)){
							if(!tierList.contains(t)){
								tierList.add(t);
							}
						}
					}
				}
			}				
		}
		
		Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
		if (colors == null) {
			colors = new HashMap<String, Color>();
			Preferences.set("TierColors", colors, transcription);
		}
		
		Map<String, Color> highlightColors = Preferences.getMapOfColor("TierHighlightColors", transcription);
        if(highlightColors == null) {
        	highlightColors = new HashMap<String, Color>();
        	Preferences.set("TierHighlightColors", highlightColors, transcription);
        }  
        
        Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
		if (fonts == null) {
			fonts = new HashMap<String, Font>();
			Preferences.set("TierFonts", fonts, transcription);
		}
		
		Color nextColor = (Color) currentProps.get("TierColor");
		Color nextHighlightColor = (Color) currentProps.get("TierHighlightColor");       
		Font fo = (Font) currentProps.get("TierFont");
		
		//add tier
		String tierName;
		if(mode == ADD){
			tierName = tierNameTextField.getText();
            tierName.replace('\n', ' ');
            tierName.trim();
		} else {
			tierName = tier.getName();
		}
		
		if (nextColor != null) {
			if (!nextColor.equals(Color.WHITE)) {
				colors.put(tierName, nextColor);
			} else {
				colors.remove(tierName);
			}
    	} 
        
        if (nextHighlightColor != null) {
        	if (!nextHighlightColor.equals(Color.WHITE)) {
        		highlightColors.put(tierName, nextHighlightColor);
        	} else {
        		highlightColors.remove(tierName);
        	}
        } 
        
        if (fo != null) {
			fonts.put(tierName, fo);
    	} else {
    		fonts.remove(tierName);
    	}
		
		boolean applyFont = false;
		boolean applyColor = false;
		boolean applyHighlight = false;
		
		for(int i=0; i< tierList.size(); i++){
			if(i == 0){
				applyFont = (Boolean)currentProps.get("Font");
				applyColor = (Boolean)currentProps.get("Color");
				applyHighlight = (Boolean)currentProps.get("HighLightColor");
			}
			
			TierImpl t = tierList.get(i);
			
			if (applyColor) {
				if(nextColor != null) {
					if (!nextColor.equals(Color.WHITE)) {
						colors.put(t.getName(), nextColor);
					} else {
						colors.remove(t.getName());
					}
				}
        	}
			
			if (applyHighlight){
				if(nextHighlightColor != null) {
					if (!nextHighlightColor.equals(Color.WHITE)) {
						highlightColors.put(t.getName(), nextHighlightColor);
					} else {
						highlightColors.remove(t.getName());
					}
				} 
			}
		
			if (applyFont){
				if( fo != null) {
					((Map<String, Font>) fonts).put(t.getName(), fo);
				} else {
					((Map<String, Font>) fonts).remove(t.getName());
				}
			}
		}
		
		Preferences.set("TierColors", colors, transcription);
		Preferences.set("TierHighlightColors", highlightColors, transcription);
		Preferences.set("TierFonts", fonts, transcription, true);
	}
    
    /**
     * Checks if a filename points to an exisitng .eaf or .etf file.
     *
     * @param fileName a String representation of a file
     *
     * @return true if the file exists and is an .eaf or .rtf, false otherwise
     */
    private boolean isValidFile(String fileName) {
        if (fileName == null) {
            return false;
        }

        File f = new File(fileName);

        if (!f.exists()) {
            return false;
        }

        String lowerPathName = fileName.toLowerCase();

        String[] exts = FileExtension.EAF_EXT;

        for (String ext : exts) {
            if (lowerPathName.endsWith("." + ext)) {
                return true;
            }
        }

        exts = FileExtension.TEMPLATE_EXT;

        for (String ext : exts) {
            if (lowerPathName.endsWith("." + ext)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the stereotype for a linguistic type. When the linguistic type
     * has no constraints -1 is returned.
     *
     * @param name type name
     *
     * @return the stereotype or -1
     */
    private int getStereoTypeForTypeName(String name) {
        LinguisticType type = null;

        for (LinguisticType tempType : transcription.getLinguisticTypes()) {

            if (tempType.getLinguisticTypeName().equals(name)) {
                type = tempType;

                break;
            }
        }

        return getStereoTypeForType(type);
    }

    /**
     * Returns the stereotype for a linguistic type. When the linguistic type
     * has no constraints -1 is returned.
     *
     * @param type type
     *
     * @return the stereotype or -1
     */
    private int getStereoTypeForType(LinguisticType type) {
        if ((type == null) || (type.getConstraints() == null)) {
            return -1;
        } else {
            return type.getConstraints().getStereoType();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (event.getSource() == changeButton) {
            if (mode == DELETE) {
                doDelete();

                return;
            } else if (mode == IMPORT) {
                doImport();

                return;
            } else {
                String tierName = tierNameTextField.getText();
                tierName = tierName.replace('\n', ' ');
                tierName = tierName.trim();

                if (tierName.length() == 0) {
                    tierNameTextField.requestFocus();
                    JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("EditTierDialog.Message.TierName"),
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);

                    return;
                }

                if (transcription.getTierWithId(tierName) != null) {
                    if ((mode == ADD) ||
                            ((mode == CHANGE) && !tierName.equals(oldTierName))) {
                        tierNameTextField.requestFocus();
                        JOptionPane.showMessageDialog(this,
                            ElanLocale.getString(
                                "EditTierDialog.Message.Exists"),
                            ElanLocale.getString("Message.Error"),
                            JOptionPane.ERROR_MESSAGE);

                        return;
                    }
                }

                String participant = participantTextField.getText();
                String annotator = annotatorTextField.getText();
                String lingType = (String) lingTypeComboBox.getSelectedItem();
                Tier parentTier = transcription.getTierWithId((String) parentComboBox.getSelectedItem());

                String localeName = (String) languageComboBox.getSelectedItem();
                Locale locale = null;
                
                if (languageComboBox.getSelectedIndex() == 0) {
                	// locale = null
                } else if (languageComboBox.getSelectedIndex() == 1 && localeName.indexOf("(System default)") > -1) {
                	locale = Locale.getDefault();
                } else {               
	                if (langs != null) {
	                    for (Locale lang : langs) {
	                        if (lang.getDisplayName().equals(localeName)) {
	                            locale = lang;
	
	                            break;
	                        }
	                    }
	                }
                }
                // allow null
//                if (locale == null) {
//                    locale = oldLocale;
//                }

                String mlLanguage = mlLanguageBox.getId();
                
                switch (mode) {
                case ADD:
                    doAdd(tierName, parentTier, lingType, participant, annotator, locale, mlLanguage);

                    break;

                case CHANGE:
                    doChange(tierName, parentTier, lingType, participant, annotator, locale, mlLanguage);

                    break;
                }
            }
            
            currentProps.clear();
            //dispose();
        } else if (event.getSource() == importSourceButton) {
            promptForImportFile();
        } else if (event.getSource() == advancedButton) {
        	showAdvancedOptionsDialog();
        } else {
            dispose();
        }
    }

    /**
     * ComboBox selection changes.
     *
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource() == currentTiersComboBox) {
            	// When selecting a tier from the combo box, multiple selection
            	// in the table is always unexpected.
            	// Temporarily disable it here, careful not to lose the selection.
            	final ListSelectionModel selectionModel = tierTable.getSelectionModel();
				int oldMode = selectionModel.getSelectionMode();
                String name = (String) currentTiersComboBox.getSelectedItem();
            	selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                updateUIForTier(name);
               	selectionModel.setSelectionMode(oldMode);
            } else if ((e.getSource() == lingTypeComboBox) && (mode == CHANGE)) {
                if ((tier != null) && (tier.getNumberOfAnnotations() > 0)) {
                    // warn if more than 0 annotations and stereotype is different
                    String newTypeName = (String) e.getItem();
                    boolean stereoTypeChanged = false;
                    int newStereoType = getStereoTypeForTypeName(newTypeName);
                    int oldStereoType = getStereoTypeForType(oldLingType);

                    if (newStereoType != oldStereoType) {
                        stereoTypeChanged = true;
                    }

                    if (!oldLingType.getLinguisticTypeName().equals(newTypeName) &&
                            stereoTypeChanged) {
                        StringBuilder buf = new StringBuilder(ElanLocale.getString(
                                    "EditTierDialog.Message.RecommendType"));
                        buf.append("\n");
                        buf.append(ElanLocale.getString(
                                "EditTierDialog.Message.Corrupt"));

                        JOptionPane.showMessageDialog(this, buf.toString(),
                            ElanLocale.getString("Message.Warning"),
                            JOptionPane.WARNING_MESSAGE);

                        // HS sep-04 prevent changing the lin. type when there are annotations   
                        lingTypeComboBox.setSelectedItem(oldLingType.getLinguisticTypeName());
                    }
                }
            } else if (e.getSource() == parentComboBox) {
                if ((mode == CHANGE) && (tier != null) &&
                        (tier.getNumberOfAnnotations() > 0)) {
                    if (!(oldParentTierName.equals(e.getItem()))) {
                        StringBuilder buf = new StringBuilder(ElanLocale.getString(
                                    "EditTierDialog.Message.RecommendParent"));
                        buf.append("\n");
                        buf.append(ElanLocale.getString(
                                "EditTierDialog.Message.Corrupt"));
                        JOptionPane.showMessageDialog(this, buf.toString(),
                            ElanLocale.getString("Message.Warning"),
                            JOptionPane.WARNING_MESSAGE);

                        // HS sep-04 prevent changing the parent when there are annotations    
                        parentComboBox.setSelectedItem(oldParentTierName);
                    }
                } else {
                    // suggest the participant name from the parent...
                    String partiName = participantTextField.getText();

                    if ((partiName == null) ||
                            (partiName.trim().length() == 0)) {
                        TierImpl parent = transcription.getTierWithId((String) e.getItem());

                        if (parent != null) {
                            participantTextField.setText(parent.getParticipant());
                        }
                    }
                }

                fillLingTypeMenu();
            }
        }
    }

    /**
     * Add the editpanel to the selected tab
     *
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
	public void stateChanged(ChangeEvent e) {
        tabPane.removeChangeListener(this);
        mode = tabPane.getSelectedIndex();
        tabPane.removeAll();
        tabPane.addTab(ElanLocale.getString("Button.Add"), null);
        tabPane.addTab(ElanLocale.getString("Button.Change"),
            null);
        tabPane.addTab(ElanLocale.getString("Button.Delete"),
            null);
        tabPane.addTab(ElanLocale.getString("Button.Import"),
            importPanel);

        if (mode < IMPORT) {
            tabPane.setComponentAt(tabPane.getSelectedIndex(), editPanel);
        } else {
            tabPane.setComponentAt(1, editPanel);
        }

        tabPane.setSelectedIndex(mode);
        updateForMode();

        //editPanel.revalidate();
        tabPane.revalidate();
        tabPane.addChangeListener(this);

        if ((mode == CHANGE) || (mode == DELETE)) {
            if (currentTiersComboBox.getItemCount() > 0) {
                String name = (String) currentTiersComboBox.getSelectedItem();
                updateUIForTier(name);
            }
        } else if (mode == ADD) {
        	updateUIForTier(null);
            if (parentComboBox.getItemCount() > 0) {
                parentComboBox.setSelectedIndex(0);
            }
        }
    }
    
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (mode == ADD) {
			return;
		}
		int row = tierTable.getSelectedRow();
		if (row > -1) {
			row = tierTable.convertRowIndexToModel(row);
			int column = model.findColumn(TierTableModel.NAME);
			updateUIForTier((String) model.getValueAt(row, column));
		}
	}
}
