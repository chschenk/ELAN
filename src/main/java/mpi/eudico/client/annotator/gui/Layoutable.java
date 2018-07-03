package mpi.eudico.client.annotator.gui;

/**
 * Interface that defines the Layout behaviour of elements in the Elan
 * universe. To be implemented by all viewers and other components that are to
 * be managed by the LayoutManager
 */
public interface Layoutable {
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean wantsAllAvailableSpace(); // uses all free space in horizontal direction

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isOptional(); // can beshown/hidden. If hidden, dimensions are (0,0), position in layout is kept

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isDetachable(); // can be detached, re-attached from main document window

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isWidthChangeable();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isHeightChangeable();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMinimalWidth();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMinimalHeight();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getImageOffset(); // position of image wrt Layoutable's origin, to be used for spatial alignment
}
