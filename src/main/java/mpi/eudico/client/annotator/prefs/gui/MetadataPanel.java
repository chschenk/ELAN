package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.border.LineBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.md.imdi.ImdiFileServiceProvider;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;


/**
 * A preference editor for visualization of IMDI metadata values.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class MetadataPanel extends AbstractEditPrefsPanel implements PreferenceEditor {
    private ImdiFileServiceProvider provider;
    private List<String> origKeys = null;
    private List<String> afterKeys = null;
    private TierExportTableModel model;
    private JTable keyTable;
    private final String SEL_COLUMN = "Select";
    private final String KEY_COLUMN = "Key";

    /**
     * Constructor.
     */
    public MetadataPanel() {
        super(ElanLocale.getString("PreferencesDialog.Metadata.IMDI"));
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
    	List<String> val = Preferences.getListOfString("Metadata.IMDI.Defaults", null);

        if (val != null) {
            origKeys = val;
        }
    }

    private void initComponents() {      	
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = globalInset;

        provider = new ImdiFileServiceProvider();

        URL imdiUrl = this.getClass()
                          .getResource("/mpi/eudico/client/annotator/resources/Session.imdi");
        
        provider.setMetadataFile(imdiUrl.toString()); // uses an empty imdi session file
        provider.initialize(); // should all be safe
        model = new TierExportTableModel(); //reuse
        model.setColumnIdentifiers(new String[] { SEL_COLUMN, KEY_COLUMN });
        keyTable = new TierExportTable(model);

        keyTable.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        keyTable.setBorder(new LineBorder(Color.LIGHT_GRAY));
        // fill model
        if ((provider != null) && (provider.getKeys() != null)) {
            String key;

            for (int i = 0; i < provider.getKeys().size(); i++) {
                key = provider.getKeys().get(i);

                if ((origKeys != null) && origKeys.contains(key)) {
                    model.addRow(Boolean.TRUE, key );
                } else {
                    model.addRow(Boolean.FALSE, key);
                }
            }
        }

        outerPanel.add(keyTable, gbc);
    }

    /**
     * Returns a map of (changed) preferences. The method isChanged() is called 
     * first by the enclosing dialog, this is crucial!
     *
     * @return a map of (changed) preferences
     */
    @Override
	public Map<String, Object> getChangedPreferences() {
        // rely on the fact that isChanged has been called
        if (afterKeys != null) {
            Map<String, Object /*List<String>*/> prefs = new HashMap<String, Object>(1);
            prefs.put("Metadata.IMDI.Defaults", afterKeys);

            return prefs;
        }

        return null;
    }

    /**
     * Compares the current selected items with the initial selected items.
     *
     * @return true if anything changed
     */
    @Override
	public boolean isChanged() {
        afterKeys = new ArrayList<String>();

        int includeCol = model.findColumn(SEL_COLUMN);
        int nameCol = model.findColumn(KEY_COLUMN);

        // add selected keys in the right order
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean include = (Boolean) model.getValueAt(i, includeCol);

            if (include.booleanValue()) {
                afterKeys.add((String) model.getValueAt(i, nameCol));
            }
        }

        if ((origKeys == null) && (afterKeys.size() > 0)) {
            return true;
        }

        if ((origKeys != null) && (afterKeys.size() != origKeys.size())) {
            return true;
        }

        // check whether there is at least one key not present in the both lists
        if (origKeys != null) {
            for (Object key : origKeys) {
                if (!afterKeys.contains(key)) {
                    return true;
                }
            }

            for (Object key : afterKeys) {
                if (!origKeys.contains(key)) {
                    return true;
                }
            }
        }

        afterKeys = null;

        return false;
    }
}
