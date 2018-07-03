package mpi.eudico.client.annotator.md.spi;

import javax.swing.JPanel;


/**
 * Abstract class for configuration (selection) of metadata keys.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public abstract class MDConfigurationPanel extends JPanel {
    /**
     * Creates a new MDConfigurationPanel instance
     */
    public MDConfigurationPanel() {
        super();
    }

    /**
     * Applies the changes made to the selection.
     */
    public abstract void applyChanges();
}
