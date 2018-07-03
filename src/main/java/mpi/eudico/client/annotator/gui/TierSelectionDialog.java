package mpi.eudico.client.annotator.gui;

import java.awt.Dialog;
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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;


/**
 * A dialog to select tier names from a list (table) of tier names.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class TierSelectionDialog extends ClosableDialog
    implements ActionListener {
    private List<String> allTier;
    private List<String> selectedTier;
    private List<String> returnedTiers = null;
    private TierExportTableModel model;
    private TierExportTable tierTable;
    private JButton selAllButton;
    private JButton deselAllButton;
    private JButton okButton;
    private JButton cancelButton;

    /** column id for the include in export checkbox column, invisible */
    protected final String EXPORT_COLUMN = "export";

    /** column id for the tier name column, invisible */
    protected final String TIER_NAME_COLUMN = "tier";

    /**
     * Creates a new TierSelectionDialog instance
     *
     * @param owner parent dialog
     * @param allTier the list of tiers
     * @param selectedTier the list of selected tiers
     *
     * @throws HeadlessException he
     */
    public TierSelectionDialog(Dialog owner, List<String> allTier,
        List<String> selectedTier) throws HeadlessException {
        super(owner, true);
        this.allTier = allTier;
        this.selectedTier = selectedTier;
        returnedTiers = selectedTier;
        initComponents();
    }

    /**
     * Creates a new TierSelectionDialog instance
     *
     * @param owner parent dialog
     * @param allTier the list of tiers
     * @param selectedTier the list of selected tiers
     *
     * @throws HeadlessException
     */
    public TierSelectionDialog(Frame owner, List<String> allTier,
        List<String> selectedTier) throws HeadlessException {
        super(owner, true);
        this.allTier = allTier;
        this.selectedTier = selectedTier;
        returnedTiers = selectedTier;
        initComponents();
    }

    private void initComponents() {
        JPanel cp = new JPanel(new GridBagLayout());
        cp.setBorder(new TitledBorder(ElanLocale.getString(
                    "ExportDialog.Label.SelectTiers")));

        model = new TierExportTableModel();
        model.setColumnIdentifiers(new String[] { EXPORT_COLUMN, TIER_NAME_COLUMN });
        tierTable = new TierExportTable(model);

        if (allTier != null) {
            for (String name : allTier) {
                if (selectedTier != null) {
                    if (selectedTier.contains(name)) {
                        model.addRow(Boolean.TRUE, name);
                    } else {
                        model.addRow(Boolean.FALSE, name);
                    }
                } else {
                    model.addRow(Boolean.TRUE, name);
                }
            }
        }

        Insets insets = new Insets(4, 6, 4, 6);
        Dimension tableDim = new Dimension(120, 200);

        JScrollPane tierScrollPane = new JScrollPane(tierTable);
        tierScrollPane.setPreferredSize(tableDim);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        cp.add(tierScrollPane, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 6, 4));
        selAllButton = new JButton(ElanLocale.getString(
                    "ExportDialog.Label.SelectAll"));
        selAllButton.addActionListener(this);
        buttonPanel.add(selAllButton);

        deselAllButton = new JButton(ElanLocale.getString(
                    "ExportDialog.Label.DeselectAll"));
        deselAllButton.addActionListener(this);
        buttonPanel.add(deselAllButton);

        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        cp.add(buttonPanel, gbc);
        getContentPane().add(cp);

        pack();

        if (getParent() != null) {
            setLocationRelativeTo(getParent());
        }
    }

    private List<String> getSelectedTiers() {
    	return model.getSelectedTiers();
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == selAllButton) {
            if (model != null) {
                for (int i = 0; i < tierTable.getRowCount(); i++) {
                    model.setValueAt(Boolean.TRUE, i, 0);
                }
            }
        } else if (ae.getSource() == deselAllButton) {
            if (model != null) {
                for (int i = 0; i < tierTable.getRowCount(); i++) {
                    model.setValueAt(Boolean.FALSE, i, 0);
                }
            }
        } else if (ae.getSource() == cancelButton) {
            setVisible(false);
            dispose();
        } else if (ae.getSource() == okButton) {
            returnedTiers = getSelectedTiers();
            setVisible(false);
            dispose();
        }
    }

    /**
     * Returns the selected tiers.
     *
     * @return the selected tiers
     */
    public List<String> getValue() {
        return returnedTiers;
    }
}
