package mpi.eudico.client.annotator.tier;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel.Modes;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.TranscriptionTierSortAndSelectPanel;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A dialog to select tiers the annotations of which need to be converted to
 * upper- or lowercase.
 * <p>
 * Implementation note: the code could be reduced further by extending
 * the AbstractTierSortAndSelectPanel. The layout is perfectly compatible
 * and it contains everything but the contents of the options panel.
 * But this dialog is not about export.
 * 
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ChangeCaseDlg extends ClosableDialog implements ActionListener {
    private TranscriptionImpl transcription;

    // ui elements
    private JPanel titlePanel;
    private JPanel tierPanel;
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JButton startButton;
    private JLabel titleLabel;
    
    private AbstractTierSortAndSelectPanel tierSelectPanel;
    
    private JRadioButton upperCaseRB;
    private JRadioButton lowerCaseRB;
    private JCheckBox beginCapCheckBox;
	private JCheckBox onlyBeginCapCheckBox;

    /**
     * Creates a new ChangeCaseDlg instance
     *
     * @param transcription the transcription that hold the tiers
     */
    public ChangeCaseDlg(TranscriptionImpl transcription, Frame frame) {
        super(frame);
        this.transcription = transcription;
        initComponents();
        postInit();
    }

    private void initComponents() {
        titlePanel = new JPanel();
        tierPanel = new JPanel();
        optionsPanel = new JPanel();
        buttonPanel = new JPanel();
        startButton = new JButton();
        closeButton = new JButton();
        titleLabel = new JLabel();

    	tierSelectPanel = new TranscriptionTierSortAndSelectPanel(transcription,
    			null, // List<String> tierOrder, i.e. ALL_TIERS
    			null, // List<String> selectedTiers,
    			true, // boolean allowReordering,
    			true, // boolean allowSorting,
    			Modes.ALL_TIERS);

        upperCaseRB = new JRadioButton();
        upperCaseRB.setSelected(true);
        onlyBeginCapCheckBox = new JCheckBox();
        onlyBeginCapCheckBox.setEnabled(true);
        lowerCaseRB = new JRadioButton();
        beginCapCheckBox = new JCheckBox();
        beginCapCheckBox.setEnabled(false);
        
        ButtonGroup group = new ButtonGroup();
        group.add(upperCaseRB);
        group.add(lowerCaseRB);

        setModal(true);
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gridBagConstraints;

        titlePanel.setLayout(new BorderLayout(0, 4));
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titleLabelPanel = new JPanel();
        titleLabelPanel.add(titleLabel);
        titlePanel.add(titleLabelPanel, BorderLayout.NORTH);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(titlePanel, gridBagConstraints);

        tierPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        tierPanel.add(tierSelectPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(tierPanel, gridBagConstraints);

        optionsPanel.setLayout(new GridBagLayout());
        //insets.bottom = 3;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(upperCaseRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 20, 2, 6);;
        optionsPanel.add(onlyBeginCapCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(lowerCaseRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2, 20, 2, 6);;
        optionsPanel.add(beginCapCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(12, 6, 2, 6);
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
        updateLocale();

        // addItemListener etc
        upperCaseRB.addActionListener(this);
        lowerCaseRB.addActionListener(this);
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        int minimalWidth = 500;
        int minimalHeight = 400;
        setSize((getSize().width < minimalWidth) ? minimalWidth : getSize().width,
            (getSize().height < minimalHeight) ? minimalHeight : getSize().height);
        setLocationRelativeTo(getParent());
        //setResizable(false);
    }

    private void updateLocale() {
        setTitle(ElanLocale.getString("ChangeCaseDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ChangeCaseDialog.Title"));
        tierPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "LabelAndNumberDialog.Label.Tier")));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "LabelAndNumberDialog.Label.Options")));
        upperCaseRB.setText(ElanLocale.getString(
                "ChangeCaseDialog.UpperCase"));
        lowerCaseRB.setText(ElanLocale.getString(
                "ChangeCaseDialog.LowerCase"));
        onlyBeginCapCheckBox.setText(ElanLocale.getString(
                "ChangeCaseDialog.InitialCapitalize"));
        beginCapCheckBox.setText(ElanLocale.getString(
                "ChangeCaseDialog.Capital"));
        startButton.setText(ElanLocale.getString("Button.OK"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }

    /**
     * Checks the current settings and creates a Command.
     */
    private void startOperation() {
        List<String> tierNames = null;

        tierNames = getSelectedTiers();

        if (tierNames.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("LabelAndNumberDialog.Warning.NoTier"),
                ElanLocale.getString("Message.Error"),
                JOptionPane.WARNING_MESSAGE);

            return;
        }
       
        //closeDialog(); // to give the command the possibility of showing a monitor??
        boolean initial = // look at the relevant checkbox only
        		(upperCaseRB.isSelected() && onlyBeginCapCheckBox.isSelected()) ||
        		(lowerCaseRB.isSelected() && beginCapCheckBox.isSelected());
        Object[] args = new Object[] {
                tierNames,
                Boolean.valueOf(upperCaseRB.isSelected()), 
                Boolean.valueOf(initial)
            };
        Command command = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.CHANGE_CASE);

        command.execute(transcription, args);
    }

    /**
     * Returns the tiers that have been selected in the table.
     *
     * @return a list of the selected tiers
     */
    private List<String> getSelectedTiers() {
    	return tierSelectPanel.getSelectedTiers();
    }

    /**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    private void closeDialog() {
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

        if (source == startButton) {
            startOperation();
        } else if (source == closeButton) {
            closeDialog();
        } else if (source == upperCaseRB || source == lowerCaseRB) {
        	onlyBeginCapCheckBox.setEnabled(upperCaseRB.isSelected());
        	beginCapCheckBox.setEnabled(lowerCaseRB.isSelected());
        }
    }
}
