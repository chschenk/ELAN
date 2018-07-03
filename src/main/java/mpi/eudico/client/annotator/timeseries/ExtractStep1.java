package mpi.eudico.client.annotator.timeseries;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.TierTableModel;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;


/**
 * First step of a wizard to extract certain data from a time series track
 * based on time intervals (annotations) on a time-alignable tier. The extracted
 * value for the intervals are stored in ref annotations on a symbolically 
 * associated depending tier.
 * 
 * @author Han Sloetjes
 * @version 1.0 March 2006
  */
@SuppressWarnings("serial")
public class ExtractStep1 extends StepPane implements ActionListener,
    ListSelectionListener {
    private TranscriptionImpl transcription;
    private JTable sourceTable;
    private TierTableModel tierModel;
    private JList dependList;
    private DefaultListModel depModel;
    private JButton newTierButton;

    /**
     * Creates a new ExtractStep1 instance
     *
     * @param multiPane the container multistep pane
     * @param transcription the transcription to take source and destination tier from
     */
    public ExtractStep1(MultiStepPane multiPane, TranscriptionImpl transcription) {
        super(multiPane);
        this.transcription = transcription;
        initComponents();
    }

    /**
     * Initialize ui components etc.
     */
    @Override
	public void initComponents() {
        // setPreferredSize
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane tableScroll;
        JScrollPane listScroll;

        // Table and List
        if (transcription != null) {
            List<TierImpl> allTiers = transcription.getTiers();
            List<TierImpl> alignables = new ArrayList<TierImpl>(allTiers.size());
            TierImpl tier;

            for (int i = 0; i < allTiers.size(); i++) {
                tier = allTiers.get(i);

                if (tier.isTimeAlignable() && tier.getNumberOfAnnotations() > 0) {
                    alignables.add(tier);
                }
            }

            tierModel = new TierTableModel(alignables,
                    new String[] {
                        TierTableModel.NAME, TierTableModel.PARENT,
                        TierTableModel.TYPE
                    });
            sourceTable = new JTable(tierModel);
        } else {
            tierModel = new TierTableModel();
            sourceTable = new JTable(tierModel);
        }

        sourceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableScroll = new JScrollPane(sourceTable);
        depModel = new DefaultListModel();
        dependList = new JList(depModel);
        dependList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listScroll = new JScrollPane(dependList);

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(new JLabel("<html>" +
                ElanLocale.getString("TimeSeriesViewer.Extract.SourceTier") +
                "</html>"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(tableScroll, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        add(new JLabel("<html>" +
                ElanLocale.getString("TimeSeriesViewer.Extract.DestTier") +
                "</html>"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(listScroll, gbc);

        newTierButton = new JButton(ElanLocale.getString(
                    "Menu.Tier.AddNewTier"));
        newTierButton.addActionListener(this);
        newTierButton.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        add(newTierButton, gbc);

        sourceTable.getSelectionModel().addListSelectionListener(this);
        dependList.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Refreshes the list of dependent symbolically associated tiers for the
     * selected source tier.
     */
    private void updateDependentTierList() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        depModel.removeAllElements();
        newTierButton.setEnabled(false);

        int row = sourceTable.getSelectedRow();

        if (row < 0) {
            return;
        }

        String tierName = (String) tierModel.getValueAt(row,
                tierModel.findColumn(TierTableModel.NAME));
        TierImpl tier = transcription.getTierWithId(tierName);

        if (tier != null) {

            for (TierImpl loopTier : tier.getChildTiers()) {

                if (loopTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
                    depModel.addElement(loopTier.getName());
                }
            }

            if (depModel.getSize() > 0) {
                dependList.setSelectedIndex(0);
                multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
            }

            newTierButton.setEnabled(true);
        }
    }

    /**
     * If there is a selected tier, prompt for a tier name and create a
     * dependent tier of type Symbolic Association. The type can be selected
     * from a list or can be created if there is no linguistic type of the
     * proper stereotype in the transcription.
     */
    private void createTier() {
        int row = sourceTable.getSelectedRow();

        if (row < 0) {
            return;
        }

        String tierName = (String) tierModel.getValueAt(row,
                tierModel.findColumn(TierTableModel.NAME));
        TierImpl tier = transcription.getTierWithId(tierName);

        if (tier != null) {
            // prompt for tier name
            String name = JOptionPane.showInputDialog(this,
                    ElanLocale.getString("EditTierDialog.Message.TierName"),
                    ElanLocale.getString("Menu.Tier.AddNewTier"),
                    JOptionPane.QUESTION_MESSAGE);

            if ((name == null) || (name.length() == 0)) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("EditTierDialog.Message.TierName"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);

                return;
            }

            if (transcription.getTierWithId(name) != null) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("EditTierDialog.Message.Exists"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.ERROR_MESSAGE);

                return;
            }

            // prompt for Linguistic Type
            List<LinguisticType> types = transcription.getLinguisticTypes();
            List<String> symbList = new ArrayList<String>(types.size());

            for (LinguisticType lt : types) {
                if ((lt.getConstraints() != null) &&
                        (lt.getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)) {
                    symbList.add(lt.getLinguisticTypeName());
                }
            }

            if (symbList.size() == 0) {
                // prompt for name for symbolic association type
                String typeName = JOptionPane.showInputDialog(this,
                        ElanLocale.getString("EditTypeDialog.Message.TypeName"),
                        ElanLocale.getString("Menu.Type.AddNewType"),
                        JOptionPane.QUESTION_MESSAGE);

                if ((typeName == null) || (typeName.length() == 0)) {
                    JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("EditTypeDialog.Message.TypeName"),
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);

                    return;
                }

                if (transcription.getLinguisticTypeByName(typeName) != null) {
                    JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("EditTypeDialog.Message.Exists"),
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);

                    return;
                }

                Command c = ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.ADD_TYPE);
                c.execute(transcription,
                    new Object[] {
                        typeName, new SymbolicAssociation(), null, Boolean.FALSE,
                        Boolean.FALSE
                    });
                LinguisticType lt = transcription.getLinguisticTypeByName(typeName);

                if (lt != null) {
                    symbList.add(typeName);
                } else {
                    return;
                }
            }

            String selTypeName = null;

            if (symbList.size() == 1) {
                selTypeName = symbList.get(0);
            } else {
                selTypeName = (String) JOptionPane.showInputDialog(this,
                        ElanLocale.getString(
                            "TimeSeriesViewer.Extract.SelectType"),
                        ElanLocale.getString("EditTypeDialog.ChangeType"),
                        JOptionPane.QUESTION_MESSAGE, null, symbList.toArray(),
                        symbList.get(0));
            }

            if (selTypeName != null) {
                Command c = ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.ADD_TIER);
                c.execute(transcription,
                    new Object[] {
                        name, tier, selTypeName, tier.getParticipant(), tier.getAnnotator(),
                        tier.getDefaultLocale(), tier.getLangRef()
                    });
                updateDependentTierList();
            }
        }
    }

    /**
     * Return the title of this step.
     *
     * @return the title
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("TimeSeriesViewer.Extract.SelectTiers");
    }

    /**
     * Enable or disable the Next button depending on destination tier selection.
     */
    @Override
	public void enterStepBackward() {
        if (dependList.getSelectedIndex() < 0) {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        }
    }

    /**
     * Store the names of the selected source and destination tier as step 
     * properties.
     *
     * @return true if a source and destination tier have been selected, 
     * false otherwise
     */
    @Override
	public boolean leaveStepForward() {
        if ((sourceTable.getSelectedRow() >= 0) &&
                (dependList.getSelectedIndex() >= 0)) {
            String sourceName = (String) tierModel.getValueAt(sourceTable.getSelectedRow(),
                    tierModel.findColumn(TierTableModel.NAME));
            multiPane.putStepProperty("SourceTier", sourceName);

            String destName = (String) dependList.getSelectedValue();
            multiPane.putStepProperty("DestTier", destName);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Button action.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        // only the add tier button is there
        createTier();
    }

    /**
     * Update ui elements after a change in selected tiers.
     *
     * @param e the event
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            if (e.getSource() == sourceTable.getSelectionModel()) {
                updateDependentTierList();
            } else if (e.getSource() == dependList.getSelectionModel()) {
                if (dependList.getSelectedIndex() < 0) {
                    multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
                } else {
                    multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
                }
            }
        }
    }
}
