package mpi.eudico.client.annotator.dcr;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFrame;

import mpi.dcr.DCSmall;
import mpi.dcr.ILATDCRConnector;
import mpi.dcr.LocalDCSelectPanel;


/**
 * A panel for data category selection from a local cache.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class LocalDCSPanel extends LocalDCSelectPanel {
    /**
     * Creates a new LocalDCSPanel instance
     *
     * @param connector the local connector
     */
    public LocalDCSPanel(ILATDCRConnector connector) {
        super(connector);
    }

    /**
     * Creates a new LocalDCSPanel instance
     *
     * @param connector the connector
     * @param resBundle a resource bundle with localized strings
     */
    public LocalDCSPanel(ILATDCRConnector connector, ResourceBundle resBundle) {
        super(connector, resBundle);
    }

    /**
     * Creates a new LocalDCSPanel instance
     */
    public LocalDCSPanel() {
        super();
    }

    /**
     * @see mpi.dcr.LocalDCSelectPanel#selectAndAddCategories()
     */
    @Override
	protected void selectAndAddCategories() {
        ELANDCRDialog dialog = null;

        if (this.getTopLevelAncestor() instanceof JDialog) {
            dialog = new ELANDCRDialog((JDialog) this.getTopLevelAncestor(),
                    true, ELANDCRDialog.REMOTE_MODE);
        } else {
            dialog = new ELANDCRDialog((JFrame) this.getTopLevelAncestor(),
                    true, ELANDCRDialog.REMOTE_MODE);
        }

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true); // blocks
        

        List<DCSmall> val = dialog.getValue();

        if (val != null) {
            addCategories(val);
        }
    }
}
