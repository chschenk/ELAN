package mpi.eudico.client.annotator.tier;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A dialog for customizing the annotations from gaps action.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class AnnotationsFromGapsDlg extends ClosableDialog
    implements ActionListener, ListSelectionListener, ChangeListener {
    private TranscriptionImpl transcription;
    private long mediaDuration;

    // ui elements
    //private JPanel titlePanel;
    private JPanel tierPanel;
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JButton selectAllButton;
    private JButton selectNoneButton;
    private JButton closeButton;
    private JButton startButton;
    private JComboBox onTierCB;
    private JLabel titleLabel;
    private JList tierList;
    private DefaultListModel model;
    private JRadioButton onSameTierRB;
    private JRadioButton onNewTierRB;
    private JLabel tierNameLabel;
    private JTextField tierNameTF;
    private JRadioButton emptyRB;
    private JRadioButton durationRB;
    private JRadioButton valueRB;
    private JRadioButton msRB;
    private JRadioButton secRB;
    private JRadioButton hourRB;
    private JTextField annValueTF;

    /**
     * Creates a new AnnotationsFromGapsDlg instance
     *
     * @param owner the parent frame
     * @param transcription the transcription
     * @param mediaDuration the master media duration
     *
     * @throws HeadlessException
     */
    public AnnotationsFromGapsDlg(Frame owner, TranscriptionImpl transcription,
        long mediaDuration) throws HeadlessException {
        super(owner);
        this.transcription = transcription;
        this.mediaDuration = mediaDuration;
        initComponents();
        extractTiers();
        postInit();
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
       
        setLocationRelativeTo(getParent());

        //setResizable(false);
    }

    private void initComponents() {
        //titlePanel = new JPanel();
        tierPanel = new JPanel();
        optionsPanel = new JPanel();
        buttonPanel = new JPanel();
        startButton = new JButton();
        closeButton = new JButton();

        selectAllButton = new JButton();
        selectAllButton.addActionListener(this);
        selectNoneButton = new JButton();
        selectNoneButton.addActionListener(this);

        model = new DefaultListModel();
        tierList = new JList(model);
        tierList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tierList.addListSelectionListener(this);

        onTierCB = new JComboBox();

        onSameTierRB = new JRadioButton();
        onSameTierRB.setSelected(true);
        onSameTierRB.addChangeListener(this);
        onNewTierRB = new JRadioButton();
        onNewTierRB.addChangeListener(this);

        tierNameLabel = new JLabel();
        tierNameTF = new JTextField();
        tierNameTF.setEnabled(false);
        emptyRB = new JRadioButton();
        emptyRB.setSelected(true);
        emptyRB.addChangeListener(this);
        durationRB = new JRadioButton();
        durationRB.addChangeListener(this);
        valueRB = new JRadioButton();
        valueRB.addChangeListener(this);
        msRB = new JRadioButton(ElanLocale.getString("TimeCodeFormat.MilliSec"));
        msRB.setSelected(true);
        msRB.setEnabled(false);
        secRB = new JRadioButton(ElanLocale.getString("TimeCodeFormat.Seconds"));
        secRB.setEnabled(false);
        hourRB = new JRadioButton(ElanLocale.getString(
                    "TimeCodeFormat.TimeCode"));
        hourRB.setEnabled(false);
        annValueTF = new JTextField(); // should enable input methods!?
        annValueTF.setEnabled(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(msRB);
        bg.add(secRB);
        bg.add(hourRB);

        ButtonGroup bg2 = new ButtonGroup();
        bg2.add(onSameTierRB);
        bg2.add(onNewTierRB);

        ButtonGroup bg3 = new ButtonGroup();
        bg3.add(emptyRB);
        bg3.add(durationRB);
        bg3.add(valueRB);

        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gbc;

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        getContentPane().add(titleLabel, gbc);
        
     // add more elements to this panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        getContentPane().add(tierPanel, gbc); 
        
        // add elements to the optionspanel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;       
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        getContentPane().add(optionsPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = insets;
        getContentPane().add(buttonPanel, gbc); 
        
        
        //tier Panel elements
       
        tierPanel.setLayout(new GridBagLayout());
        Dimension tableDim = new Dimension(450, 100);
        JScrollPane tierScrollPane = new JScrollPane(tierList);
        tierScrollPane.setPreferredSize(tableDim);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;        
        tierPanel.add(tierScrollPane, gbc);        
      

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = insets;
        //gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        tierPanel.add(selectAllButton, gbc);

        gbc = new GridBagConstraints();       
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        //gbc.weighty = 1.0;
        gbc.gridy = 1;
        gbc.gridy = 1;
        tierPanel.add(selectNoneButton, gbc);

        //option panel elements
        optionsPanel.setLayout(new GridBagLayout());
        insets.bottom = 3;
        JLabel destLabel = new JLabel(ElanLocale.getString(
                    "FillGapsDialog.Label.Destination"));

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        optionsPanel.add(destLabel, gbc);

        gbc.insets = new Insets(2, 16, 2, 6);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0;
        optionsPanel.add(onSameTierRB, gbc);

        //gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        optionsPanel.add(onTierCB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0;
        optionsPanel.add(onNewTierRB, gbc);

        //gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        optionsPanel.add(tierNameTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(24, 6, 2, 6);
        optionsPanel.add(new JLabel(ElanLocale.getString(
                    "FillGapsDialog.Label.AnnotationValue")), gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(2, 16, 2, 6);
        optionsPanel.add(valueRB, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        //gbc.insets = insets;
        optionsPanel.add(annValueTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;

        //gbc.insets = insets;
        optionsPanel.add(durationRB, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(2, 32, 2, 6);
        optionsPanel.add(msRB, gbc);
        gbc.gridy = 8;
        optionsPanel.add(secRB, gbc);
        gbc.gridy = 9;
        optionsPanel.add(hourRB, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(2, 16, 2, 6);
        optionsPanel.add(emptyRB, gbc);

        //button panel
        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));
        startButton.addActionListener(this);
        buttonPanel.add(startButton);
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);        

        updateLocale();
    }

    private void updateLocale() {
        setTitle(ElanLocale.getString("FillGapsDialog.Title"));
        titleLabel.setText(ElanLocale.getString("FillGapsDialog.Title"));
        tierPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "FillGapsDialog.Label.SelectTiers")));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "LabelAndNumberDialog.Label.Options")));
        onSameTierRB.setText(ElanLocale.getString(
                "FillGapsDialog.Label.SameTier"));
        onNewTierRB.setText(ElanLocale.getString("FillGapsDialog.Label.NewTier"));
        tierNameLabel.setText(ElanLocale.getString(
                "FillGapsDialog.Label.NewTierName"));
        emptyRB.setText(ElanLocale.getString("FillGapsDialog.Label.Empty"));
        durationRB.setText(ElanLocale.getString("FillGapsDialog.Label.Duration"));
        valueRB.setText(ElanLocale.getString(
                "FillGapsDialog.Label.SpecifyValue"));
        startButton.setText(ElanLocale.getString("Button.OK"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
        selectAllButton.setText(ElanLocale.getString("Button.SelectAll"));
        selectNoneButton.setText(ElanLocale.getString("Button.SelectNone"));
    }

    /**
     * Extract all top-level tiers and fill the list.
     */
    private void extractTiers() {
        if (transcription != null) {
            List<TierImpl> v = transcription.getTiers();
            TierImpl t;

            for (int i = 0; i < v.size(); i++) {
                t = v.get(i);

                if ((t.getParentTier() == null) && t.isTimeAlignable()) { //currently superfluous check
                    model.addElement(t.getName());
                    onTierCB.addItem(t.getName());
                }
            }

            if (model.getSize() > 0) {
                tierList.setSelectedIndex(0);
            } else {
                startButton.setEnabled(false);
            }
        }
    }

    /**
     * The action performed handling.
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectAllButton) {
            //select all tiers from tierList
            tierList.setSelectionInterval(0, model.getSize() - 1);
        } else if (e.getSource() == selectNoneButton) {
            //select nothing from tierList
            tierList.clearSelection();
        } else if (e.getSource() == closeButton) {
            setVisible(false);
            dispose();
        } else if (e.getSource() == startButton) {
            //get selected tier names from list
            Object[] tierNamesTemp = tierList.getSelectedValues();
            String[] tierNames = new String[tierNamesTemp.length];

            for (int i = 0; i < tierNames.length; i++) {
				tierNames[i] = (String) tierNamesTemp[i];
			}

            //if no tier has been selected, then warn the user
            if ((tierNames == null) || (tierNames.length <= 0)) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("LabelAndNumberDialog.Warning.NoTier"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);

                return;
            }

            String nTierName;

            //check if "on new tier" has been selected
            if (onNewTierRB.isSelected()) {
                //retrieve the name of the new tier
                nTierName = tierNameTF.getText();

                //check if the tier name is valid
                if ((nTierName == null) || (nTierName.length() == 0)) {
                    JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("EditTierDialog.Message.TierName"),
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.WARNING_MESSAGE);

                    return;
                } else {
                    //the new tier name is valid
                    //now check if this given new tier name is already present
                    //in the set of existing tiers. If yes, then warn user.
                    if (transcription.getTierWithId(nTierName) != null) {
                        JOptionPane.showMessageDialog(this,
                            ElanLocale.getString(
                                "EditTierDialog.Message.Exists"),
                            ElanLocale.getString("Message.Error"),
                            JOptionPane.WARNING_MESSAGE);

                        return;
                    }
                }
            } else {
                //the gaps should be placed on the tier itself
                nTierName = (String) onTierCB.getSelectedItem();
            }

            String annValue = null; //annotated value
            String format = null;

            if (valueRB.isSelected()) {
                annValue = annValueTF.getText();
            } else if (durationRB.isSelected()) {
                if (msRB.isSelected()) {
                    format = Constants.MS_STRING;
                } else if (secRB.isSelected()) {
                    format = Constants.SSMS_STRING;
                } else if (hourRB.isSelected()) {
                    format = Constants.HHMMSSMS_STRING;
                }
            }

            //construct array with arguments for the command
            Object[] arg = new Object[] {
                    tierNames, nTierName, annValue, format,
                    new Long(mediaDuration)
                };

            Command com = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.ANN_FROM_GAPS);
            com.execute(transcription, arg);
        }
    }

    /**
     * The handling of changes in radio button selections
     *
     * @param e the event
     */
    @Override
	public void stateChanged(ChangeEvent e) {
        if ((e.getSource() == onSameTierRB) || (e.getSource() == onNewTierRB)) {
            onTierCB.setEnabled(onSameTierRB.isSelected() &&
                onSameTierRB.isEnabled());
            tierNameTF.setEnabled(onNewTierRB.isSelected());
        } else if (e.getSource() == valueRB) {
            annValueTF.setEnabled(valueRB.isSelected());
        } else if (e.getSource() == durationRB) {
            boolean b = durationRB.isSelected();
            //annValueTF.setEnabled(!b);
            msRB.setEnabled(b);
            secRB.setEnabled(b);
            hourRB.setEnabled(b);
        }

        //emptyRB not listed, because it's not needed
    }

    /**
     * Event Listener for List components
     *
     * @param event selection event
     */
    @Override
	public void valueChanged(ListSelectionEvent event) {
        JList list = (JList) event.getSource();
        int nrSelectedItems = list.getSelectedIndices().length;
        int firstSelectedIndex = list.getSelectedIndex();

        switch (nrSelectedItems) {
        //in case no items are selected
        case 0:
            startButton.setEnabled(false);
            onSameTierRB.setEnabled(false);
            onSameTierRB.setSelected(true);
            onTierCB.setEnabled(false);
            onNewTierRB.setEnabled(false);
            tierNameTF.setEnabled(false);
            emptyRB.setEnabled(false);
            durationRB.setEnabled(false);
            valueRB.setEnabled(false);
            msRB.setEnabled(false);
            secRB.setEnabled(false);
            hourRB.setEnabled(false);
            annValueTF.setEnabled(false);

            break;

        case 1:
            onTierCB.removeAllItems();
            onTierCB.addItem(tierList.getSelectedValue());

            if (firstSelectedIndex >= 0) {
                onTierCB.setSelectedIndex(0);
            }

            startButton.setEnabled(true);
            onSameTierRB.setEnabled(true);
            onNewTierRB.setEnabled(true);
            tierNameTF.setEnabled(onNewTierRB.isSelected());

            emptyRB.setEnabled(true);
            durationRB.setEnabled(true);
            valueRB.setEnabled(true);
            annValueTF.setEnabled(valueRB.isSelected());

            break;

        default:
            onTierCB.removeAllItems();

            for (int i = 0; i < nrSelectedItems; i++) {
				onTierCB.addItem(tierList.getSelectedValues()[i]);
			}

            if (firstSelectedIndex >= 0) {
                onTierCB.setSelectedIndex(0);
            }

            startButton.setEnabled(true);
            onSameTierRB.setEnabled(true);
            onNewTierRB.setEnabled(true);
            tierNameTF.setEnabled(onNewTierRB.isSelected());

            emptyRB.setEnabled(true);

            durationRB.setEnabled(true);
            valueRB.setEnabled(true);
            annValueTF.setEnabled(valueRB.isSelected());

            break;
        }
    }
}
