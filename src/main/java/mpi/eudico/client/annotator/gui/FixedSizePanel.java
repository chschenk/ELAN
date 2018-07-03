package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;

import javax.swing.JPanel;


/**
 * DOCUMENT ME!
 * $Id: FixedSizePanel.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class FixedSizePanel extends JPanel {
    private Dimension dimension;

    /**
     * Creates a new FixedSizePanel instance
     *
     * @param width DOCUMENT ME!
     * @param height DOCUMENT ME!
     */
    public FixedSizePanel(int width, int height) {
        dimension = new Dimension(width, height);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Dimension getMinimumSize() {
        return dimension;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Dimension getPreferredSize() {
        return dimension;
    }
}
