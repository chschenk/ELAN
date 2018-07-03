package mpi.eudico.client.annotator.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JComponent;


/**
 * DOCUMENT ME!
 * $Id: LayoutableTestComponent.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class LayoutableTestComponent extends JComponent implements Layoutable {
    private boolean bWantsAllAvailableSpace = false;
    private boolean bIsOptional = false;
    private boolean bIsDetachable = false;
    private boolean bIsHorizontallyResizable = false;
    private boolean bIsVerticallyResizable = false;
    private int imageOffset = 0;
    private int minimalWidth = 0;
    private int minimalHeight = 0;
    private int nr;
    private Color color;

    /**
     * Creates a new LayoutableTestComponent instance
     *
     * @param nr DOCUMENT ME!
     * @param color DOCUMENT ME!
     */
    LayoutableTestComponent(int nr, Color color) {
        this.nr = nr;
        this.color = color;

        setBackground(color);

        JButton but = new JButton("" + nr);
        but.setBackground(color);
        but.setSize(getMinimalWidth(), getMinimalHeight());

        setLayout(new BorderLayout());
        add(but, BorderLayout.CENTER);
    }

    // uses all free space in horizontal direction
    @Override
	public boolean wantsAllAvailableSpace() {
        if ((nr == 7) || (nr == 8)) {
            return true;
        } else {
            return false;
        }
    }

    // can be shown/hidden. If hidden, dimensions are (0,0), position in layout is kept
    @Override
	public boolean isOptional() {
        return false;
    }

    // can be detached, re-attached from main document window
    @Override
	public boolean isDetachable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isWidthChangeable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isHeightChangeable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public int getMinimalWidth() {
        return 50;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public int getMinimalHeight() {
        return 50;
    }

    // position of image wrt Layoutable's origin, to be used for spatial alignment
    @Override
	public int getImageOffset() {
        return 10;
    }
}
