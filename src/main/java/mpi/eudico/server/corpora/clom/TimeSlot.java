package mpi.eudico.server.corpora.clom;

/**
 * DOCUMENT ME!
 * $Id: TimeSlot.java 43483 2015-03-05 10:11:50Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public interface TimeSlot extends Comparable<TimeSlot> {
    /** Holds value of property DOCUMENT ME! */
    public static final int TIME_UNALIGNED = -1;

    /** Holds value of property DOCUMENT ME! */
    public static final int NOT_INDEXED = -1;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getIndex();

    /**
     * DOCUMENT ME!
     *
     * @param theIndex DOCUMENT ME!
     */
    public void setIndex(int theIndex);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getTime();

    /**
     * DOCUMENT ME!
     *
     * @param theTime DOCUMENT ME!
     */
    public void setTime(long theTime);

    /**
     * Only to be called by TimeOrder !!
     */
    public void updateTime(long theTime);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isTimeAligned();

    /**
     * DOCUMENT ME!
     *
     * @param timeSlot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isAfter(TimeSlot timeSlot);
}
