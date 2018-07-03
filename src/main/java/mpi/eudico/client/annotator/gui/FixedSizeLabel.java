package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;

import javax.swing.JLabel;


/**
 * DOCUMENT ME!
 * $Id: FixedSizeLabel.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class FixedSizeLabel extends JLabel {
    private Dimension dimension;

    /**
     * Creates a new FixedSizeLabel instance
     *
     * @param label DOCUMENT ME!
     * @param width DOCUMENT ME!
     * @param height DOCUMENT ME!
     */
    public FixedSizeLabel(String label, int width, int height) {
        super(label, JLabel.CENTER);
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
