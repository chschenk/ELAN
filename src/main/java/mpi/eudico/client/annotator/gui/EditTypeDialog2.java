package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
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
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
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

import mpi.dcr.DCSmall;
import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.dcr.ELANDCRDialog;
import mpi.eudico.client.annotator.dcr.ELANLocalDCRConnector;
import mpi.eudico.client.annotator.lexicon.LexiconQueryBundleDialog;
import mpi.eudico.client.annotator.type.LinguisticTypeTableModel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.IncludedIn;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import mpi.eudico.server.corpora.clomimpl.type.TimeSubdivision;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.EmptyStringComparator;

/**
 * A dialog for adding, changing and removing Linguistic Types.
 * @version 2.0
 */
@SuppressWarnings("serial")
public class EditTypeDialog2 extends ClosableDialog implements ActionListener,
	ItemListener, ChangeListener, ListSelectionListener {
    /** add linguistic type mode */
    public static final int ADD = 0;

    /** change linguistic type mode! */
    public static final int CHANGE = 1;

    /** delete linguistic type mode! */
    public static final int DELETE = 2;
    
    /** import linguistic type mode */
    public static final int IMPORT = 3;
    
    /** value for no stereotype! */
    public final String none = "None";
    
    private TranscriptionImpl transcription;
    private List<LinguisticType> types;
    private String oldConstraint;
    
    private JLabel titleLabel;
    private JPanel typePanel;
    private JTable typeTable;
    private LinguisticTypeTableModel model;
    private JTabbedPane tabPane;
    private JPanel editPanel;
    private JLabel currentTypesLabel;
    private JLabel typeLabel;
    private JLabel timeAlignableLabel;
    private JLabel constraintsLabel;
    private JLabel cvLabel;
    private JTextField typeTextField;
    private JCheckBox timeAlignableCheckbox;
    private JComboBox constraintsComboBox;
    private JComboBox currentTypesComboBox;
    private JComboBox cvComboBox;
    // dcr 
    protected JPanel dcrPanel;
    protected JLabel dcrLabel;
    protected JTextField dcrField;
    protected JTextField dcIdField;
    protected JButton dcrButton;
    protected JButton removeDcrButton;
    
	// lexicon
	private JLabel lexiconLabel;
	private JPanel lexiconPanel;

	private JTextField lexiconLinkField;
	private JTextField lexiconFieldField;

	private JButton lexiconButton;
	private JButton removeLexiconButton;
	private LexiconQueryBundle2 oldLexiconQueryBundle;
	private LexiconQueryBundle2 newLexiconQueryBundle;
    
    // import panel
    private JPanel importPanel;
    private JLabel importSourceLabel;
    private JTextField importSourceTF;
    private JButton importSourceButton;
    
    private JPanel buttonPanel;
    private JButton changeButton;
    private JButton cancelButton;
    private int mode = ADD;    

    /**
     * A general purpose constructor for adding, changing, deleting or importing 
     * LinguisticTypes.<br>
     *
     * @param theFrame the parent frame
     * @param modal whether the dialog should be modal or not
     * @param theTranscription the Transcription containing the types
     * @param editMode the mode to start with, ADD, CHANGE, DELETE or IMPORT
     */
    public EditTypeDialog2(Frame theFrame, boolean modal,
        Transcription theTranscription, int editMode) {
        super(theFrame, modal);
        transcription = (TranscriptionImpl) theTranscription;

        if ((editMode >= ADD) && (editMode <= IMPORT)) {
            mode = editMode;
        }

        initComponents();
        extractCurrentTypes();
        extractControlledVocabularies();
        updateUIForType((String) currentTypesComboBox.getSelectedItem());
        updateLocale();
        updateForMode();
        postInit() ;

        if (mode == ADD) {
            typeTextField.requestFocus();
        } else if (mode == IMPORT){
            importSourceButton.requestFocus();
        } else {
            currentTypesComboBox.requestFocus();
        }
    }
    
    /**
     * Initializes the ui components.
     */
    private void initComponents() {
        titleLabel = new JLabel();
        currentTypesLabel = new JLabel();
        typeLabel = new JLabel();
        timeAlignableLabel = new JLabel();
        constraintsLabel = new JLabel();
        cvLabel = new JLabel();
        typeTextField = new JTextField(30);
        changeButton = new JButton();
        cancelButton = new JButton();
        timeAlignableCheckbox = new JCheckBox("", true);
        timeAlignableCheckbox.setEnabled(false);
        constraintsComboBox = new JComboBox();
        currentTypesComboBox = new JComboBox();
        currentTypesComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        cvComboBox = new JComboBox();
        
		// lexicon
        lexiconLabel = new JLabel();
        lexiconPanel = new JPanel(new GridBagLayout());
        lexiconLinkField = new JTextField();
        lexiconLinkField.setEditable(false);
        lexiconLinkField.setEnabled(false);
        lexiconFieldField = new JTextField();
        lexiconFieldField.setEditable(false);
        lexiconFieldField.setEnabled(false);
        lexiconButton = new JButton();
        lexiconButton.addActionListener(this);
        removeLexiconButton = new JButton();
        removeLexiconButton.addActionListener(this);
        removeLexiconButton.setEnabled(false);
        try {
        	ImageIcon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Plus16.gif"));
        	lexiconButton.setIcon(icon);
        } catch (Throwable t) {
        	lexiconButton.setText("+");
        }
        try {
        	ImageIcon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Minus16.gif"));
        	removeLexiconButton.setIcon(icon);
        } catch (Throwable t) {
        	removeLexiconButton.setText("X");
        }
        // dcr
        dcrPanel = new JPanel(new GridBagLayout());
        dcrLabel = new JLabel();
        dcrField = new JTextField();
        dcIdField = new JTextField();
        dcIdField.setEditable(false);
        dcrField.setEditable(false);
        dcrField.setEnabled(false);
        dcIdField.setEnabled(false);
        dcrButton = new JButton();
        dcrButton.addActionListener(this);
        try {
        	ImageIcon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Plus16.gif"));
        	dcrButton.setIcon(icon);
        } catch (Throwable t) {
        	dcrButton.setText("+");
        }
        removeDcrButton = new JButton();
        removeDcrButton.addActionListener(this);
        removeDcrButton.setEnabled(false);
        try {
        	ImageIcon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Minus16.gif"));
        	removeDcrButton.setIcon(icon);
        } catch (Throwable t) {
        	removeDcrButton.setText("X");
        }
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        // add stereotypes
        constraintsComboBox.addItem(none);
        //get all stereotypes and add them to the choice menu
        String[] publicStereoTypes = Constraint.publicStereoTypes;

        for (String publicStereoType : publicStereoTypes) {
            constraintsComboBox.addItem(publicStereoType);
        }
        constraintsComboBox.addItemListener(this);
        typePanel = new JPanel();
        typePanel.setLayout(new GridBagLayout());
        ImageIcon tickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Tick16.gif"));
        ImageIcon untickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Untick16.gif"));
        CheckBoxTableCellRenderer cbRenderer = new CheckBoxTableCellRenderer();
        cbRenderer.setIcon(untickIcon);
        cbRenderer.setSelectedIcon(tickIcon);
        cbRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        String[] columns = new String[] {
                LinguisticTypeTableModel.NAME,
                LinguisticTypeTableModel.STEREOTYPE,
                LinguisticTypeTableModel.CV_NAME,
                LinguisticTypeTableModel.DC_ID,
                LinguisticTypeTableModel.TIME_ALIGNABLE,
            };
        model = new LinguisticTypeTableModel(transcription.getLinguisticTypes(), columns);
        typeTable = new JTable(model);
        typeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        typeTable.getSelectionModel().addListSelectionListener(this);
        
        for (int i = 0; i < typeTable.getColumnCount(); i++) {
            if (typeTable.getModel().getColumnClass(i) != String.class) {
                typeTable.getColumn(typeTable.getModel().getColumnName(i))
                         .setPreferredWidth(35);
            }

            if (typeTable.getModel().getColumnClass(i) == Boolean.class) {
                typeTable.getColumn(typeTable.getModel().getColumnName(i))
                         .setCellRenderer(cbRenderer);
            }
        }
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
        EmptyStringComparator emptyComp = new EmptyStringComparator();
        for (int i = 1; i < 4; i++) {
        	rowSorter.setComparator(i, emptyComp);
        }
        typeTable.setRowSorter(rowSorter);
        
        JScrollPane typeScrollPane = new JScrollPane(typeTable);
        Dimension size = new Dimension(300, 120);
        typeScrollPane.setMinimumSize(size);
        typeScrollPane.setPreferredSize(size);

        tabPane = new JTabbedPane();
        
        getContentPane().setLayout(new GridBagLayout());
        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
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
        typePanel.add(typeScrollPane, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(typePanel, gbc);
        
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
        
        GridBagConstraints lgbc = new GridBagConstraints();
        lgbc.anchor = GridBagConstraints.WEST;
        lgbc.insets = insets;
        editPanel.add(currentTypesLabel, lgbc);

        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.gridx = 1;
        rgbc.fill = GridBagConstraints.HORIZONTAL;
        rgbc.anchor = GridBagConstraints.WEST;
        rgbc.insets = insets;
        rgbc.weightx = 1.0;
        editPanel.add(currentTypesComboBox, rgbc);
        lgbc.gridy = 1;
        editPanel.add(typeLabel, lgbc);
        rgbc.gridy = 1;
        editPanel.add(typeTextField, rgbc);
        lgbc.gridy = 2;
        editPanel.add(constraintsLabel, lgbc);
        rgbc.gridy = 2;
        editPanel.add(constraintsComboBox, rgbc);
        lgbc.gridy = 3;
        editPanel.add(cvLabel, lgbc);
        rgbc.gridy = 3;
        editPanel.add(cvComboBox, rgbc);
        
        // lexicon
        lgbc.gridy = 4;
        editPanel.add(lexiconLabel, lgbc);
        GridBagConstraints lexgbc = new GridBagConstraints();
        lexgbc.fill = GridBagConstraints.HORIZONTAL;
        lexgbc.anchor = GridBagConstraints.WEST;
        lexgbc.weightx = 1.0;
        lexiconPanel.add(lexiconLinkField,lexgbc);
        lexgbc.gridx = 1;
        lexgbc.weightx = 0.5;
        lexgbc.insets = new Insets(0, 4, 0, 0);
        lexiconPanel.add(lexiconFieldField, lexgbc);
        lexgbc.fill = GridBagConstraints.NONE;
        lexgbc.weightx = 0.0;
        lexgbc.gridx = 2;
        lexiconPanel.add(lexiconButton, lexgbc);
        lexgbc.gridx = 3;
        lexiconPanel.add(removeLexiconButton, lexgbc);
        
        rgbc.gridy = 4;
        editPanel.add(lexiconPanel, rgbc);
        // end lexicon
        lgbc.gridy = 5;
        editPanel.add(dcrLabel, lgbc);
        // construct the dcr panel
        GridBagConstraints dcgbc = new GridBagConstraints();
        dcgbc.fill = GridBagConstraints.HORIZONTAL;
        dcgbc.anchor = GridBagConstraints.WEST;
        dcgbc.weightx = 1.0;
        dcrPanel.add(dcrField, dcgbc);
        dcgbc.gridx = 1;
        dcgbc.weightx = 0.5;
        dcgbc.insets = new Insets(0, 4, 0, 0);
        dcrPanel.add(dcIdField, dcgbc);
        dcgbc.gridx = 2;
        dcgbc.fill = GridBagConstraints.NONE;
        dcgbc.weightx = 0.0;
        dcrPanel.add(dcrButton, dcgbc);
        dcgbc.gridx = 3;
        dcrPanel.add(removeDcrButton, dcgbc);
        
        rgbc.gridy = 5;
        editPanel.add(dcrPanel, rgbc);
        lgbc.gridy = 6;
        editPanel.add(timeAlignableLabel, lgbc);
        rgbc.gridy = 6;
        editPanel.add(timeAlignableCheckbox, rgbc);

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

        // buttons
        changeButton.addActionListener(this);
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
    }
    
    private void updateLocale() {
        typeLabel.setText(ElanLocale.getString("EditTypeDialog.Label.Type"));
        typePanel.setBorder(new TitledBorder(ElanLocale.getString(
        	"EditTypeDialog.CurrentTypes")));
        timeAlignableLabel.setText(ElanLocale.getString(
                "EditTypeDialog.Label.TimeAlignable"));
        constraintsLabel.setText(ElanLocale.getString(
                "EditTypeDialog.Label.Stereotype"));
        cvLabel.setText(ElanLocale.getString("EditTypeDialog.Label.CV"));
        dcrLabel.setText(ElanLocale.getString("DCR.Label.ISOCategory"));
//        dcrButton.setText(ElanLocale.getString("Button.Browse"));
        dcrButton.setToolTipText(ElanLocale.getString("DCR.Label.SelectCategory"));
        removeDcrButton.setToolTipText(ElanLocale.getString("DCR.Label.RemoveCategory"));
        cancelButton.setText(ElanLocale.getString("Button.Close"));
        importSourceLabel.setText("<html>" +
                ElanLocale.getString("EditTypeDialog.Label.ImportSource") +
                "</html>");
        importSourceButton.setText(ElanLocale.getString("Button.Browse"));
		lexiconLabel.setText(ElanLocale.getString("EditTypeDialog.Label.Lexicon"));
        lexiconButton.setToolTipText(ElanLocale.getString("EditTypeDialog.Button.Lexicon"));
        removeLexiconButton.setToolTipText(ElanLocale.getString("EditTypeDialog.Button.RemoveLexicon"));
    }
    
    /**
     * Updates texts and enables/disables components for the current  edit
     * mode.
     */
    private void updateForMode() {
        switch (mode) {
        case ADD:
            setTitle(ElanLocale.getString("EditTypeDialog.Title.Add"));
            currentTypesLabel.setText("");
            currentTypesComboBox.setEnabled(false);
            typeTextField.setEnabled(true);
            typeTextField.setEditable(true);
            typeTextField.setText("");
            constraintsComboBox.setEnabled(true);
            constraintsComboBox.setSelectedItem(none);
            timeAlignableCheckbox.setSelected(true);
            cvComboBox.setEnabled(true);
            cvComboBox.setSelectedItem(none);
            changeButton.setText(ElanLocale.getString(
                    "Button.Add"));
            changeButton.setEnabled(true);
            dcrField.setText("");
            dcIdField.setText("");
            dcrButton.setEnabled(true);
            removeDcrButton.setEnabled(false);
            lexiconFieldField.setText("");
            lexiconLinkField.setText("");
            lexiconButton.setEnabled(true);
            removeLexiconButton.setEnabled(false);
            break;

        case CHANGE:
            setTitle(ElanLocale.getString("EditTypeDialog.Title.Change"));
            currentTypesLabel.setText(ElanLocale.getString(
                    "EditTypeDialog.ChangeType"));
            changeButton.setText(ElanLocale.getString(
                    "Button.Change"));
            currentTypesComboBox.setEnabled(true);
            typeTextField.setEnabled(true);
            typeTextField.setEditable(true);
            constraintsComboBox.setEnabled(true);
            cvComboBox.setEnabled(true);
            
            if (currentTypesComboBox.getModel().getSize() > 0) {
                updateUIForType((String) currentTypesComboBox.getItemAt(0));
                currentTypesComboBox.addItemListener(this);
            } else {
                changeButton.setEnabled(false);
            }
            dcrButton.setEnabled(true);
            removeDcrButton.setEnabled(false);
            //oldConstraint = (String)constraints.getSelectedItem();
            lexiconButton.setEnabled(true);
            removeLexiconButton.setEnabled(false);
            break;

        case DELETE:
            setTitle(ElanLocale.getString("EditTypeDialog.Title.Delete"));
            currentTypesLabel.setText(ElanLocale.getString(
                    "EditTypeDialog.DeleteType"));
            changeButton.setText(ElanLocale.getString(
                    "Button.Delete"));
            currentTypesComboBox.setEnabled(true);
            typeTextField.setEnabled(false);
            typeTextField.setEditable(false);
            constraintsComboBox.setEnabled(false);
            cvComboBox.setEnabled(false);
            
            if (currentTypesComboBox.getModel().getSize() > 0) {
                updateUIForType((String) currentTypesComboBox.getItemAt(0));
                currentTypesComboBox.addItemListener(this);

                //typeTextField.setText((String)currentTypes.getItemAt(0));
            } else {
                changeButton.setEnabled(false);
            }

            typeTextField.setEditable(false);
            constraintsComboBox.setEnabled(false);
            cvComboBox.setEnabled(false);
            dcrButton.setEnabled(false);
            removeDcrButton.setEnabled(false);
            lexiconButton.setEnabled(false);
            removeLexiconButton.setEnabled(false);

            break;
        case IMPORT:
            setTitle(ElanLocale.getString("EditTypeDialog.Title.Import"));
            changeButton.setText(ElanLocale.getString(
            "Button.Import"));

            break;
        }

        titleLabel.setText(getTitle());
    }

    /**
     * Extract the linguistic types already present in the transcription.
     */
    private void extractCurrentTypes() {
        currentTypesComboBox.removeItemListener(this);
        currentTypesComboBox.removeAllItems();
        types = transcription.getLinguisticTypes();

        if (types == null) {
            types = new ArrayList<LinguisticType>();

            return;
        }

        for (LinguisticType lt : types) {
            currentTypesComboBox.addItem(lt.getLinguisticTypeName());
        }

        currentTypesComboBox.addItemListener(this);
    }

    /**
     * Again extract the types from the transcription after an add, change or
     * delete operation.
     */
    private void reextractTypes() {
        extractCurrentTypes();

        if (currentTypesComboBox.getItemCount() > 0) {
            currentTypesComboBox.setSelectedIndex(0);

            String name = (String) currentTypesComboBox.getSelectedItem();

            if (name != null) {
                updateUIForType(name);
            }
        } else {
            typeTextField.setText("");
            dcIdField.setText("");
            dcrField.setText("");
        }

        if (mode == ADD) {
            typeTextField.setText("");
            dcIdField.setText("");
            dcrField.setText("");
        }
        
        if (model != null) {
            typeTable.getSelectionModel().removeListSelectionListener(this);
            model.removeAllRows();

            Iterator typeIt = types.iterator();
            LinguisticType lt;
            while (typeIt.hasNext()) {
                lt = (LinguisticType) typeIt.next();
                model.addRow(lt);
            }
            typeTable.getSelectionModel().addListSelectionListener(this);
        }
    }

	/**
     * Fills the cv combo box with the Controlled Vocabularies present  in the
     * Transcription.
     */
    private void extractControlledVocabularies() {
        List<ControlledVocabulary> cvs = transcription.getControlledVocabularies();
        cvComboBox.addItem(none);

        for (ControlledVocabulary cv : cvs) {
            cvComboBox.addItem(cv.getName());
        }
    }
    
    /**
     * Initialize UI elements with the attributes from the first element in the
     * types list.
     *
     * @param typeName the name of the LinguisticType
     */
    private void updateUIForType(String typeName) {
        if (typeName != null) {
            typeTextField.setText(typeName);

            Iterator typeIt = types.iterator();
            LinguisticType lt;

            while (typeIt.hasNext()) {
                lt = (LinguisticType) typeIt.next();

                if ((lt != null) &&
                        lt.getLinguisticTypeName().equals(typeName)) {
                		if (currentTypesComboBox.getSelectedItem() != 
                		    typeName) {
                		    currentTypesComboBox.setSelectedItem(typeName);
                		}
                    constraintsComboBox.removeItemListener(this);

                    Constraint oldC = lt.getConstraints();

                    if (oldC != null) {
                        String stereoType = Constraint.stereoTypes[oldC.getStereoType()];
                        oldConstraint = stereoType;
                        constraintsComboBox.setSelectedItem(stereoType);
                    } else {
                        oldConstraint = none;
                        constraintsComboBox.setSelectedItem(none);
                    }

                    timeAlignableCheckbox.setSelected(lt.isTimeAlignable());

                    if (lt.isUsingControlledVocabulary()) {
                        String cvName = lt.getControlledVocabularyName();
                        cvComboBox.getModel().setSelectedItem(cvName);
                    } else {
                        cvComboBox.getModel().setSelectedItem(none);
                    }

                    if (lt.getDataCategory() != null && !lt.getDataCategory().isEmpty()) {
                    	dcIdField.setText(lt.getDataCategory());
                    	// retrieve the textual identifier from the local cache
                    	DCSmall small = ELANLocalDCRConnector.getInstance().getDCSmall(lt.getDataCategory());
                    	if (small != null && small.getIdentifier() != null) {
                    		dcrField.setText(small.getIdentifier());
                    	} else {
                    		dcrField.setText("");
                    	}
                    	if (mode == CHANGE) {
                    		removeDcrButton.setEnabled(true);
                    	}
                    } else {
                    	dcIdField.setText("");
                    	dcrField.setText("");
                    	removeDcrButton.setEnabled(false);
                    }
                    
                    if(lt.isUsingLexiconQueryBundle()) {
                    	LexiconQueryBundle2 queryBundle = lt.getLexiconQueryBundle();
                    	String linkName = queryBundle.getLinkName();
                    	String fieldName = queryBundle.getFldId().getName();
                    	lexiconLinkField.setText(linkName);
                    	lexiconFieldField.setText(fieldName);
                    	oldLexiconQueryBundle = queryBundle;
                    	if (mode == CHANGE) {
                    		removeLexiconButton.setEnabled(true);
                    	}
                    } else {
                    	lexiconLinkField.setText("");
                    	lexiconFieldField.setText("");
                    	oldLexiconQueryBundle = null;
                    	removeLexiconButton.setEnabled(false);
                    }
                    newLexiconQueryBundle = oldLexiconQueryBundle;
                    
                    if (mode == CHANGE) {
                        List<TierImpl> tiers = transcription.getTiersWithLinguisticType(typeName);

                        if (tiers.size() > 0) {
                            constraintsComboBox.setEnabled(false);
                        } else {
                            constraintsComboBox.setEnabled(true);
                        }
                        dcrButton.setEnabled(true);
                        lexiconButton.setEnabled(true);
                    }
                    
                    constraintsComboBox.addItemListener(this);
                    
                    // update table
                    if (model != null) {
                    	typeTable.getSelectionModel().removeListSelectionListener(this);
                        int col = model.findColumn(LinguisticTypeTableModel.NAME);

                        for (int i = 0; i < model.getRowCount(); i++) {
                            if (typeName.equals(model.getValueAt(i, col))) {
                                typeTable.getSelectionModel().setLeadSelectionIndex(
                                		typeTable.convertRowIndexToView(i));
                                typeTable.scrollRectToVisible(typeTable.getCellRect(
                                		typeTable.convertRowIndexToView(i), col, true));
                                
                                break;
                            }
                        }
                        typeTable.getSelectionModel().addListSelectionListener(this);
                    }
                    break;
                }
            }
        } else {
            oldConstraint = none;
            constraintsComboBox.setSelectedItem(none);
            cvComboBox.getModel().setSelectedItem(none);
            timeAlignableCheckbox.setSelected(true);
            lexiconLinkField.setText("");
            lexiconFieldField.setText("");         
            removeLexiconButton.setEnabled(false);
            dcIdField.setText("");
            dcrField.setText("");
            
            removeDcrButton.setEnabled(false);
            if (mode == ADD) {
            	dcrButton.setEnabled(true);
            	lexiconButton.setEnabled(true);
            } else {
            	lexiconButton.setEnabled(false);
            	dcrButton.setEnabled(false);
            }
        }
    }
    
    /**
     * Utility method for creating a Constraint for a given name.
     *
     * @param name the name of the constraint
     *
     * @return a Constraint or <code>null</code>
     */
    private Constraint getConstraintForName(String name) {
        Constraint c = null;

        if (name.equals(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION])) {
            c = new TimeSubdivision();
        } else if (name.equals(
                    Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION])) {
            c = new SymbolicSubdivision();
        } else if (name.equals(
                    Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION])) {
            c = new SymbolicAssociation();
        } else if (name.equals(
                Constraint.stereoTypes[Constraint.INCLUDED_IN])) {
            c = new IncludedIn();
        }

        return c;
    }
    
    private void doAdd(String name) {
        // check existence
        LinguisticType lt = null;
        Iterator tIter = types.iterator();

        while (tIter.hasNext()) {
            lt = (LinguisticType) tIter.next();

            if (lt.getLinguisticTypeName().equals(name)) {
                String errorMessage = ElanLocale.getString(
                        "EditTypeDialog.Message.Exists");
                typeTextField.requestFocus();
                JOptionPane.showMessageDialog(this, errorMessage,
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        //create new type
        String cons = (String) constraintsComboBox.getSelectedItem();
        Constraint c = getConstraintForName(cons);
        boolean alignable = timeAlignableCheckbox.isSelected();
        String cvName = (String) cvComboBox.getSelectedItem();
        String dcId = dcIdField.getText();
        if (dcId != null && dcId.length() == 0) {
        	dcId = null;
        }
        if (cvName.equals(none)) {
            cvName = null;
        }

        //create and execute a command
        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.ADD_TYPE);
        Object[] args = new Object[7];
        args[0] = name;
        args[1] = c;
        args[2] = cvName;
        args[3] = Boolean.valueOf(alignable);
        args[4] = Boolean.valueOf(false);
        args[5] = dcId;
        args[6] = newLexiconQueryBundle;
        com.execute(transcription, args);
        
        reextractTypes();
        // select the newly added one
        if (currentTypesComboBox.getItemCount() > 0) {
        	currentTypesComboBox.setSelectedIndex(currentTypesComboBox.getItemCount() - 1);
        }
        //dispose();
    }

    private void doChange(String name) {
        String oldName = (String) currentTypesComboBox.getSelectedItem();
        LinguisticType lt = null;
        LinguisticType iterType = null;
        Iterator tIter = types.iterator();

        while (tIter.hasNext()) {
            iterType = (LinguisticType) tIter.next();

            if (iterType.getLinguisticTypeName().equals(oldName)) {
                lt = iterType;
            }

            if (iterType.getLinguisticTypeName().equals(name) &&
                    (iterType != lt)) {
                // name already exists
                String errorMessage = ElanLocale.getString(
                        "EditTypeDialog.Message.Exists");
                typeTextField.requestFocus();
                JOptionPane.showMessageDialog(this, errorMessage,
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        if (lt == null) {
            // something is wrong
            String errorMessage = ElanLocale.getString(
                    "EditTypeDialog.Message.UnknownError");
            JOptionPane.showMessageDialog(this, errorMessage,
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

            //dispose();
            return;
        }

        String oldCV = null;

        if (lt.isUsingControlledVocabulary()) {
            oldCV = lt.getControlledVocabularyName();
        }

        String cons = (String) constraintsComboBox.getSelectedItem();
        Constraint c = getConstraintForName(cons);

        String cvName = (String) cvComboBox.getSelectedItem();

        if (cvName.equals(none)) {
            cvName = null;
        }

        boolean alignable = timeAlignableCheckbox.isSelected();
        
        String oldDcId = lt.getDataCategory();
        String dcId = dcIdField.getText();

        // if nothing has changed do nothing
        if (name.equals(oldName) &&
                (((c == null) && (lt.getConstraints() == null)) ||
                (c != null && c.equals(lt.getConstraints()))) &&
                (((oldCV == null) && (cvName == null)) ||
                ((oldCV != null) && oldCV.equals(cvName))) &&
                (lt.isTimeAlignable() == alignable) &&
                ((oldDcId == null && dcId == null) || 
                		(oldDcId != null && oldDcId.equals(dcId))) &&
                (((oldLexiconQueryBundle == newLexiconQueryBundle)))) {
            return;
        }

        //create and execute a command
        Object[] args = new Object[8];
        args[0] = name;
        args[1] = c;
        args[2] = cvName;
        args[3] = Boolean.valueOf(alignable);
        args[4] = Boolean.valueOf(false);
        args[5] = lt;
        args[6] = dcId;
        args[7] = newLexiconQueryBundle;

        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.CHANGE_TYPE);
        com.execute(transcription, args);

        int curRow = currentTypesComboBox.getSelectedIndex();
        reextractTypes();

        if (curRow > -1) {
        	currentTypesComboBox.setSelectedIndex(curRow);
        }
        //dispose();
    }

    private void doDelete() {
        String oldName = (String) currentTypesComboBox.getSelectedItem();
        LinguisticType lt = null;
        Iterator tIter = types.iterator();

        while (tIter.hasNext()) {
            lt = (LinguisticType) tIter.next();

            if (lt.getLinguisticTypeName().equals(oldName)) {
                break;
            }
        }

        if (lt == null) {
            // something is wrong
            String errorMessage = ElanLocale.getString(
                    "EditTypeDialog.Message.UnknownError");
            JOptionPane.showMessageDialog(this, errorMessage,
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

            //dispose();
            return;
        } else {
            //warn
        	List<TierImpl> clientTiers = transcription.getTiersWithLinguisticType(oldName);

            if (clientTiers.size() > 0) {
                StringBuilder errorBuffer = new StringBuilder(ElanLocale.getString(
                            "EditTypeDialog.Message.TypeInUse"));
                errorBuffer.append(":\n");

                Iterator clIter = clientTiers.iterator();

                while (clIter.hasNext()) {
                    errorBuffer.append("- ");
                    errorBuffer.append(((Tier) clIter.next()).getName());
                    errorBuffer.append("\n");
                }

                errorBuffer.append(ElanLocale.getString(
                        "EditTypeDialog.Message.Reassign"));
                JOptionPane.showMessageDialog(this, errorBuffer.toString(),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        //create and execute a command
        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.DELETE_TYPE);
        com.execute(transcription, new Object[] { lt });

        reextractTypes();

        //dispose();
    }
    
    /**
     * Imports tiers (without annotations) from an eaf or etf.
     */
    private void doImport() {
        String fileName = importSourceTF.getText();

        if (!isValidFile(fileName)) {
            // reuse message from import tiers...
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("EditTierDialog.Message.SelectValid"),
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.IMPORT_TYPES);
        c.execute(transcription, new Object[] { fileName });

        reextractTypes();
        updateUIForType(null);    
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
     * Creates a dialog for selection of a data category from the local cache. From that dialog
     * a new dialog can be created for connection to the global dcr.
     */
    private void selectDataCategory() {
    	ELANDCRDialog dialog = new ELANDCRDialog(this, true, ELANDCRDialog.LOCAL_MODE);
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		Object selValue = dialog.getValue();
		if (selValue instanceof List) {
			List vals = (List) selValue;
			if (vals.size() > 0) {
				Object valueObj = vals.get(0);
				if (valueObj instanceof DCSmall) {
					DCSmall dcs = (DCSmall) valueObj;
					
					dcrField.setText(dcs.getIdentifier());
					dcIdField.setText(dcs.getId());
					removeDcrButton.setEnabled(true);
				}
			}
		}
    }
    
    /**
     * Removes the data category information from the dcr text fields. 
     */
    private void removeDataCategory() {
    	dcrField.setText("");
    	dcIdField.setText("");
    	removeDcrButton.setEnabled(false);
    }
    
	/**
	 * Shows the Lexicon Query Bundle dialog to select a Lexicon Link and Lexicon Entry Field ID
	 * @author Micha Hulsbosch
	 */
	private void selectLexiconService() {
		String typeName = (String) currentTypesComboBox.getSelectedItem();
		Iterator it = types.iterator();
		while(it.hasNext()) {
			LinguisticType lt = (LinguisticType) it.next();
			if ((lt != null) &&
					lt.getLinguisticTypeName().equals(typeName)) {
				if(newLexiconQueryBundle != null) {
					LexiconLink link = newLexiconQueryBundle.getLink();
					LexiconQueryBundleDialog bundleDialog = new LexiconQueryBundleDialog(this, true, 
								transcription, newLexiconQueryBundle);
					bundleDialog.pack();
					bundleDialog.setVisible(true);
					if(!bundleDialog.isCanceled()) {
						newLexiconQueryBundle = bundleDialog.getBundle();
					}
				} else {
					LexiconQueryBundleDialog bundleDialog = new LexiconQueryBundleDialog(this, true, transcription);
					bundleDialog.pack();
					bundleDialog.setVisible(true);
					if(!bundleDialog.isCanceled()) {
						newLexiconQueryBundle = bundleDialog.getBundle();
					}
				}
				break;
			}
		} 
		if(newLexiconQueryBundle != null) {
			lexiconLinkField.setText(newLexiconQueryBundle.getLinkName());
			lexiconFieldField.setText(newLexiconQueryBundle.getFldId().getName());
			removeLexiconButton.setEnabled(true);
		} else {
			lexiconLinkField.setText("");
			lexiconFieldField.setText("");
			removeLexiconButton.setEnabled(false);
		}
	}
	
	/**
	 * Removes the link with a lexicon service.
	 */
	private void removeLexiconService() {
		lexiconFieldField.setText("");
		lexiconLinkField.setText("");
		removeLexiconButton.setEnabled(false);
		newLexiconQueryBundle = null;
	}
	
    @Override
	public void actionPerformed(ActionEvent event) {
        if (event.getSource() == changeButton) {
            if (mode == DELETE) {
                doDelete();
            } else if (mode == IMPORT) {
                doImport();
            } else {
                String typeName = typeTextField.getText();
                typeName.replace('\n', ' ');
                typeName.trim();

                if (typeName.length() == 0) {
                    String errorMessage = ElanLocale.getString(
                            "EditTypeDialog.Message.TypeName");
                    typeTextField.requestFocus();
                    JOptionPane.showMessageDialog(this, errorMessage,
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);

                    return;
                } else {
                    switch (mode) {
                    case ADD:
                        doAdd(typeName);

                        break;

                    case CHANGE:
                        doChange(typeName);

                        break;

                    default:
                        return;
                    }
                }
            }
        } else if (event.getSource() == importSourceButton) {
            promptForImportFile();
        } else if (event.getSource() == dcrButton) {
            selectDataCategory();
        } else if (event.getSource() == removeDcrButton) {
            removeDataCategory();
        } else if (event.getSource() == lexiconButton) {
        	selectLexiconService();
        } else if (event.getSource() == removeLexiconButton) {
        	removeLexiconService();
        } else {
            dispose();
        }    
    }
    
    @Override
	public void itemStateChanged(ItemEvent e) {
        if ((e.getSource() == currentTypesComboBox) &&
                (e.getStateChange() == ItemEvent.SELECTED)) {
            String name = (String) currentTypesComboBox.getSelectedItem();

            if (name != null) {
                updateUIForType(name);
            }
        } else if ((e.getSource() == constraintsComboBox) &&
                (e.getStateChange() == ItemEvent.SELECTED)) {
            String constraint = (String) constraintsComboBox.getSelectedItem();

            if ((constraint == "Symbolic Subdivision") ||
                    (constraint == "Symbolic Association")) {
                timeAlignableCheckbox.setSelected(false);
            } else {
                timeAlignableCheckbox.setSelected(true);
            }

            //if ((e.getSource() == constraints) && (oldType != null)) {	// warn if tiers use the type
            if ((mode == CHANGE) && (oldConstraint != constraint)) {
                String typeName = (String) currentTypesComboBox.getSelectedItem();
                List<TierImpl> tiers = transcription.getTiersWithLinguisticType(typeName);

                if (tiers.size() > 0) {
                    StringBuilder mesBuf = new StringBuilder(ElanLocale.getString(
                                "EditTypeDialog.Message.TypeInUse"));
                    mesBuf.append("\n");
                    mesBuf.append(ElanLocale.getString(
                            "EditTypeDialog.Message.Corrupt"));
                    JOptionPane.showMessageDialog(this, mesBuf.toString(),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);

                    /*
                       // HB, 9 may 03, restore old stereotype
                       Constraint oldC = oldType.getConstraints();
                       String oldStereoType = none;
                       if (oldC != null) {
                           oldStereoType = Constraint.stereoTypes[oldC.getStereoType()];
                       }
                       constraints.setSelectedItem(oldStereoType);
                     */
                    updateUIForType(typeName);
                }
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
            if (currentTypesComboBox.getItemCount() > 0) {
                String name = (String) currentTypesComboBox.getSelectedItem();
                updateUIForType(name);
            }
        } 
    }
    
    /**
     * Update the ui after selection of a type in the table.
     * 
     * @param e the list selection event
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
		if (mode == ADD) {
			return;
		}
		int row = typeTable.getSelectedRow();
		if (row > -1) {
			row = typeTable.convertRowIndexToModel(row);
			int column = model.findColumn(LinguisticTypeTableModel.NAME);
			updateUIForType((String) model.getValueAt(row, column));
		}
    }
}
