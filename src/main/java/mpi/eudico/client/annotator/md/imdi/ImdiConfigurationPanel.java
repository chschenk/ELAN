package mpi.eudico.client.annotator.md.imdi;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.md.spi.MDConfigurationPanel;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;


/**
 * A panel for configuration / selection of imdi metadata fields.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ImdiConfigurationPanel extends MDConfigurationPanel
    implements ActionListener {
    private ImdiFileServiceProvider provider;
    private TierExportTableModel model;
    private TierExportTable keyTable;
    private static final String SEL_COLUMN = "Select";
    private static final String KEY_COLUMN = "Key";

    /**
     * Creates a new ImdiConfigurationPanel instance
     *
     * @param provider the provider to configure
     */
    public ImdiConfigurationPanel(ImdiFileServiceProvider provider) {
        super();
        this.provider = provider;
        initComponents();
    }

    private void initComponents() {
    	setLayout(new GridBagLayout());
        setBorder(new TitledBorder(ElanLocale.getString("MetadataViewer.SelectKeys")));

        model = new TierExportTableModel();//reuse
        model.setColumnIdentifiers(new String[] { SEL_COLUMN, KEY_COLUMN });
        keyTable = new TierExportTable(model);

        keyTable.getColumn(KEY_COLUMN).setCellRenderer(new ImdiKeyRenderer());
        keyTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);

        // fill model
        if (provider != null) {
        	final List<String> keys = provider.getKeys();
        	if (keys != null) {
        		final List<String> selectedKeys = provider.getSelectedKeys();

        		final int size = keys.size();
        		for (int i = 0; i < size; i++) {
        			String key = keys.get(i);

        			if (selectedKeys.contains(key)) {
        				model.addRow(Boolean.TRUE, key);
        			} else {
        				model.addRow(Boolean.FALSE, key);
        			}
        		}
        	}
        }

        JScrollPane keyScrollPane = new JScrollPane(keyTable);
        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        add(keyScrollPane, gbc);

        //setPreferredSize(new Dimension(300, 460));
    }

    /**
     * Applies changes made to the selection.
     */
    @Override
    public void applyChanges() {
        List<String> nextSelected = new ArrayList<String>();
        nextSelected = model.getSelectedTiers();

        if (provider != null) {
            provider.setSelectedKeys(nextSelected);
        }
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
    }

}
