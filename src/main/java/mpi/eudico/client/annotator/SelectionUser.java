package mpi.eudico.client.annotator;

/**
 * The interface that defines methods from a Selection user
 */
public interface SelectionUser extends SelectionListener {
    /**
     * DOCUMENT ME!
     *
     * @param selection DOCUMENT ME!
     */
    public void setSelectionObject(Selection selection);

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateSelection();

    /**
     * DOCUMENT ME!
     *
     * @param begin DOCUMENT ME!
     * @param end DOCUMENT ME!
     */
    public void setSelection(long begin, long end);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getSelectionBeginTime();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getSelectionEndTime();
}
