package mpi.eudico.client.annotator.md;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.md.imdi.MDTable;
import mpi.eudico.client.annotator.md.spi.MDServiceProvider;
import mpi.eudico.client.annotator.md.spi.MDViewerComponent;


/**
 * A default panel for visualisation of selected metadata values.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class DefaultMDViewerComponent extends JPanel implements MDViewerComponent {
    /** The service provider */
	protected MDServiceProvider provider;
    protected JTable mdTable;
    protected DefaultTableModel model;
    protected JScrollPane scrollPane;
    protected ResourceBundle bundle;

    protected String keyColumn = ElanLocale.getString("MetadataViewer.Key");
    protected String valColumn = ElanLocale.getString("MetadataViewer.Value");
	
    /**
     * Creates a new DefaultMDViewerPanel instance
     */
    public DefaultMDViewerComponent() {
        super();
        
        initComponents();
    }

    /**
     * Creates a new DefaultMDViewerPanel instance
     *
     * @param provider the MD service provider
     */
    public DefaultMDViewerComponent(MDServiceProvider provider) {
        setProvider(provider);

        initComponents();
    }
    
    protected void initComponents() {
        model = new DefaultTableModel(0, 2);
        model.setColumnIdentifiers(new String[] { keyColumn, valColumn});
        mdTable = new JTable(model);
        scrollPane = new JScrollPane(mdTable);
        mdTable.setEnabled(false);
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        add(scrollPane, gbc);        
    }

    /**
     * Sets the metadata provider.
     * 
     * @param provider the metadata provider
     */
    @Override
	public void setProvider(MDServiceProvider provider) {
		this.provider = provider;
	}

	/**
     * Sets the selected key-value pairs for the viewer.
     *
     * @param keysAndValuesMap the map of keys and values for visualisation.
     */
    @Override
	public void setSelectedKeysAndValues(Map<String, String> keysAndValuesMap) {
    	if (keysAndValuesMap == null) {
    		return;
    	}
    	
    	if (model == null) {
            model = new DefaultTableModel(keysAndValuesMap.size(), 2);
            model.setColumnIdentifiers(new String[] { keyColumn, valColumn});
    	}
        int rowCount = model.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
        	model.removeRow(i);
        }
        
        for (Map.Entry<String, String> e : keysAndValuesMap.entrySet()) {
        	String key = e.getKey();
        	String val = e.getValue();
        	//System.out.println("K: " + key + "\n\tV: " + val);
        	model.addRow(new Object[]{key, val});
        }
        
    	if (mdTable instanceof MDTable) {
    		((MDTable) mdTable).calculateRowHeights();
    	}
    }

	/**
	 * @see mpi.eudico.client.annotator.md.spi.MDViewerComponent#setResourceBundle(java.util.ResourceBundle)
	 */
	@Override
	public void setResourceBundle(ResourceBundle bundle) {
		this.bundle = bundle;
		
	    keyColumn = ElanLocale.getString("MetadataViewer.Key");
	    valColumn = ElanLocale.getString("MetadataViewer.Value");
	    if (model != null) {
	    	model.setColumnIdentifiers(new String[]{keyColumn, valColumn});
	    }
	}

/*
	public void mousePressed(MouseEvent e) {
		Point pp = e.getPoint();
		
        if ((SwingUtilities.isRightMouseButton(e) && (e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown())) 
                || e.isPopupTrigger()) {
        	JPopupMenu popup = new JPopupMenu("Metadata Viewer");
        	popup.add(new JMenuItem("Do it!"));
        	popup.show(this, pp.x, pp.y);
        }
		
	}
*/

}
