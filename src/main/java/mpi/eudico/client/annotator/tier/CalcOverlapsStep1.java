package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * The first panel of a wizard that calculates overlaps of annotations on two
 * tiers  and creates annotations for each overlap on a third tier. The
 * duration of each  new annotation is the duration of the overlap.<br>
 * This panel shows two tables with the time-alignable tiers, in both one can
 * be selected.
 * 
 * @vesrion 1.0 Jan 2007
 * @author Han Sloetjes
 */
public class CalcOverlapsStep1 extends StepPane implements ListSelectionListener {
    protected TranscriptionImpl transcription;
    protected TierTableModel model1;
    protected TierTableModel model2;
    private JTable table1;
    private JTable table2;
    JLabel firstLabel;
    JLabel secLabel;

    /**
     * Constructor.
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public CalcOverlapsStep1(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane);
        this.transcription = transcription;

        initComponents();
    }

    /**
     * Initialize ui components etc.
     */
    @Override
	public void initComponents() {
        // get the alignable tiers
        model1 = new TierTableModel(null,
                new String[] { TierTableModel.NAME, TierTableModel.TYPE });
        model2 = new TierTableModel(null,
                new String[] { TierTableModel.NAME, TierTableModel.TYPE });

        TierImpl ti;

        for (int i = 0; i < transcription.getTiers().size(); i++) {
            ti = (TierImpl) transcription.getTiers().get(i);

            if (ti.isTimeAlignable() || ti.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
                model1.addRow(ti);
                model2.addRow(ti);
            }
        }

        table1 = new JTable(model1);
        table1.getSelectionModel()
              .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table1.getSelectionModel().addListSelectionListener(this);
        table2 = new JTable(model2);
        table2.getSelectionModel()
              .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table2.getSelectionModel().addListSelectionListener(this);

        Dimension prdim = new Dimension(120, 80);
        JScrollPane p1 = new JScrollPane(table1);
        p1.setPreferredSize(prdim);

        JScrollPane p2 = new JScrollPane(table2);
        p2.setPreferredSize(prdim);

        firstLabel = new JLabel(ElanLocale.getString(
                    "OverlapsDialog.Label.First"));
        secLabel = new JLabel(ElanLocale.getString(
                    "OverlapsDialog.Label.Second"));

        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;

        add(firstLabel, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(p1, gbc);
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        add(secLabel, gbc);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(p2, gbc);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.SelectTiers");
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        if ((table1.getSelectedRow() > -1) && (table2.getSelectedRow() > -1) &&
                (table1.getSelectedRow() != table2.getSelectedRow())) {
            int row = table1.getSelectedRow();
            Object sel = table1.getValueAt(row,
                    model1.findColumn(TierTableModel.NAME));

            if (sel != null) {
                multiPane.putStepProperty("Source-1", (String) sel);
            }

            row = table2.getSelectedRow();
            sel = table2.getValueAt(row, model2.findColumn(TierTableModel.NAME));

            if (sel != null) {
                multiPane.putStepProperty("Source-2", (String) sel);
            }

            return true;
        }

        return false;
    }

    /**
     * Checks if in both table one tier is selected and that they are not the
     * same.
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
        if ((table1.getSelectedRow() > -1) && (table2.getSelectedRow() > -1) &&
                (table1.getSelectedRow() != table2.getSelectedRow())) {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        }
    }
}
