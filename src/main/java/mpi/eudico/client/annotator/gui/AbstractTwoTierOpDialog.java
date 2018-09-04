package mpi.eudico.client.annotator.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * An abstract dialog class enabling an operation where  two tiers are
 * involved, typically a source and a destination tier.
 *
 * @author Han Sloetjes
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public abstract class AbstractTwoTierOpDialog extends ClosableDialog
    implements ActionListener, ItemListener {
    /** ui element */
    protected JRadioButton overwriteRB;

    /** ui element */
    protected JLabel titleLabel;

    /** ui element */
    protected JRadioButton preserveRB;

    /** ui element */
    protected JPanel dividePanel;

    /** ui element */
    protected JPanel titlePanel;

    /** ui element */
    protected JLabel destTierLabel;

    /** ui element */
    protected ButtonGroup existButtonGroup;

    /** ui element */
    protected JComboBox sourceTierComboBox;

    /** ui element */
    protected JPanel tierSelectionPanel;

    /** ui element */
    protected JButton startButton;

    /** ui element */
    protected JLabel sourceTierLabel;

    /** ui element */
    protected JPanel buttonPanel;

    /** ui element: combobox filled with Strings */
    protected JComboBox/*<String>*/ destTierComboBox;

    /** ui element */
    protected JButton closeButton;

    /** ui element */
    protected JLabel existingLabel;

    /** ui element */
    protected JButton createTierButton;

    /** ui element */
    //protected JTextArea explanatoryTA;

    /** ui element */
    protected JCheckBox emptyAnnCheckBox;

    /** ui element */
    protected JPanel optionsPanel;

    /** ui element */
    protected TranscriptionImpl transcription;

    /** no selection */
    protected final String EMPTY = "-";

    /**
     * Creates a new tokenizer dialog.
     *
     * @param transcription the transcription
     */
    public AbstractTwoTierOpDialog(Transcription transcription) {
        super(ELANCommandFactory.getRootFrame(transcription), true);
        this.transcription = (TranscriptionImpl) transcription;
        initComponents();
        extractSourceTiers();

        //postInit();
    }

    /**
     * Extract candidate source tiers.
     */
    protected void extractSourceTiers() {
        if (transcription != null) {

            for (Tier tier : transcription.getTiers()) {
                sourceTierComboBox.addItem(tier.getName());

                /*
                   if (tier.getLinguisticType().getConstraints() == null) {
                       sourceTierComboBox.addItem(tier.getName());
                   }
                 */
            }

        } else {
            sourceTierComboBox.addItem(EMPTY);
        }

        extractDestinationTiers();
    }

    /**
     * Extracts the candidate destination tiers for the currently selected
     * source tier.<br>
     * The destination tier must be a direct child of the source  and must be
     * of type tim-subdivision or symbolic-subdivision.
     */
    protected abstract void extractDestinationTiers();

    /**
     * Opens the edit tier dialog to let the user create a new candidate
     * destination tier.
     */
    private void editTierDialog() {
        // store current destination candidates
        String currentSelected = (String) destTierComboBox.getSelectedItem();
        int numCand = destTierComboBox.getItemCount();
        List<String> oldCandidates = new ArrayList<String>(numCand);

        for (int i = 0; i < numCand; i++) {
            oldCandidates.add((String)destTierComboBox.getItemAt(i));
        }

        // this blocks...
        Command command = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.EDIT_TIER);
        Object[] args = new Object[2];
        args[0] = Integer.valueOf(EditTierDialog2.ADD);
        args[1] = null;
        command.execute(transcription, args);

        // we are back from the edit tier dialog
        extractDestinationTiers();

        int newNumCand = destTierComboBox.getItemCount();

        if (newNumCand > numCand) {
            for (int i = 0; i < newNumCand; i++) {
                if (!oldCandidates.contains(destTierComboBox.getItemAt(i))) {
                    // select the first new tier that is encountered
                    destTierComboBox.setSelectedIndex(i);

                    break;
                }
            }
        } else {
            destTierComboBox.setSelectedItem(currentSelected);
        }
    }

    /**
     * Performs some checks and starts the tokenization process.
     */
    protected abstract void startOperation();

    /**
     * Initializes UI elements.
     */
    protected void initComponents() {
        GridBagConstraints gridBagConstraints;

        existButtonGroup = new ButtonGroup();
        titlePanel = new JPanel();
        titleLabel = new JLabel();
        //explanatoryTA = new JTextArea();
        tierSelectionPanel = new JPanel();
        sourceTierLabel = new JLabel();
        sourceTierComboBox = new JComboBox();
        destTierLabel = new JLabel();
        destTierComboBox = new JComboBox();
        createTierButton = new JButton();
        optionsPanel = new JPanel();

        existingLabel = new JLabel();
        overwriteRB = new JRadioButton();
        preserveRB = new JRadioButton();
        emptyAnnCheckBox = new JCheckBox();
        dividePanel = new JPanel();
        buttonPanel = new JPanel();
        startButton = new JButton();
        closeButton = new JButton();

        getContentPane().setLayout(new GridBagLayout());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);
        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                    closeDialog(evt);
                }
            });

        Insets insets = new Insets(2, 6, 2, 6);

        titlePanel.setLayout(new BorderLayout(0, 4));
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titleLabelPanel = new JPanel();
        titleLabelPanel.add(titleLabel);
        titlePanel.add(titleLabelPanel, BorderLayout.NORTH);
		/*
        explanatoryTA.setBackground((Color) UIManager.getDefaults().get("Panel.background"));
        explanatoryTA.setBorder((Border) UIManager.getDefaults().get("TitledBorder.border"));
        explanatoryTA.setMargin(new Insets(3, 3, 3, 3));
        explanatoryTA.setEditable(false);
        explanatoryTA.setLineWrap(true);
        explanatoryTA.setWrapStyleWord(true);
        explanatoryTA.setAlignmentX(0.0F);
        explanatoryTA.setFocusable(false);
        explanatoryTA.setPreferredSize(new Dimension(100, 80));

        titlePanel.add(explanatoryTA, BorderLayout.CENTER);
        */
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(titlePanel, gridBagConstraints);

        tierSelectionPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        tierSelectionPanel.add(sourceTierLabel, gridBagConstraints);

        sourceTierComboBox.addItemListener(this);
        sourceTierComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        tierSelectionPanel.add(sourceTierComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        tierSelectionPanel.add(destTierLabel, gridBagConstraints);

        //destTierComboBox.addItemListener(this);
        destTierComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        tierSelectionPanel.add(destTierComboBox, gridBagConstraints);

        createTierButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;
        tierSelectionPanel.add(createTierButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(tierSelectionPanel, gridBagConstraints);

        optionsPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        optionsPanel.add(dividePanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        optionsPanel.add(existingLabel, gridBagConstraints);

        overwriteRB.setSelected(true);
        existButtonGroup.add(overwriteRB);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(overwriteRB, gridBagConstraints);

        existButtonGroup.add(preserveRB);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(preserveRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        optionsPanel.add(new JPanel(), gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(emptyAnnCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(optionsPanel, gridBagConstraints);

        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));

        startButton.addActionListener(this);
        buttonPanel.add(startButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);
    }

    /**
     * Adds a panel with specific options to the layout.
     *
     * @param opPanel the options panel
     */
    protected void addOptionsPanel(JPanel opPanel) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(2, 6, 2, 6);
        optionsPanel.add(opPanel, gridBagConstraints);
        optionsPanel.revalidate();
    }

    /**
     * Applies localized strings to the ui elements. For historic reasons the
     * string identifiers start with "TokenizeDialog"
     */
    protected void updateLocale() {
        tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "TokenizeDialog.Label.SelectTiers")));
        sourceTierLabel.setText(ElanLocale.getString(
                "TokenizeDialog.Label.SourceTier"));
        destTierLabel.setText(ElanLocale.getString(
                "TokenizeDialog.Label.DestinationTier"));
        createTierButton.setText(ElanLocale.getString(
                "TokenizeDialog.Button.NewTier"));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "TokenizeDialog.Label.Options")));
        existingLabel.setText(ElanLocale.getString(
                "TokenizeDialog.Label.ExistingAnnotations"));
        overwriteRB.setText(ElanLocale.getString(
                "TokenizeDialog.RadioButton.Overwrite"));
        preserveRB.setText(ElanLocale.getString(
                "TokenizeDialog.RadioButton.Preserve"));
        emptyAnnCheckBox.setText(ElanLocale.getString(
                "TokenizeDialog.Checkbox.EmptyAnnotations"));
        startButton.setText(ElanLocale.getString("TokenizeDialog.Button.Start"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }

    /**
     * Pack, size and set location.
     */
    protected void postInit() {
        pack();

        int w = 550;
        int h = 400;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    /**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    protected void closeDialog(WindowEvent evt) {
        setVisible(false);
        dispose();
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == createTierButton) {
            editTierDialog();
        } else if (source == startButton) {
            startOperation();
        } else if (source == closeButton) {
            closeDialog(null);
        }
    }

    /**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    @Override
	public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.DESELECTED) {
            extractDestinationTiers();
        }
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
}
