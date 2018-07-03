package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;


/**
 * <p>
 * MK:02/06/19<br>Warning: all constructor are incomplete, you have to add TS
 * to a TO
 * </p>
 */
public class TimeSlotImpl implements TimeSlot {
    /** Holds value of property DOCUMENT ME! */
    long time;

    /** Holds value of property DOCUMENT ME! */
    boolean isAligned;

    /** Holds value of property DOCUMENT ME! */
    int index;

    /** Holds value of property DOCUMENT ME! */
    TimeOrder timeOrder;
    
	// temp
	long proposedTime;

    /**
     * Creates a new TimeSlotImpl instance
     *
     * @param theTO DOCUMENT ME!
     */
    public TimeSlotImpl(TimeOrder theTO) {
        time = TIME_UNALIGNED;
        isAligned = false;
        index = NOT_INDEXED;
        timeOrder = theTO;
		proposedTime = TIME_UNALIGNED;
    }

    /**
     * Creates a new TimeSlotImpl instance
     *
     * @param theTime DOCUMENT ME!
     * @param theTO DOCUMENT ME!
     */
    public TimeSlotImpl(long theTime, TimeOrder theTO) {
        time = theTime;
        isAligned = true;
        index = NOT_INDEXED;
        timeOrder = theTO;
		proposedTime = TIME_UNALIGNED;
    }

    /*    public TimeSlotImpl(long theTime, int theIndex) {
       time = theTime;
       isAligned = true;
       index = theIndex;
       }
     */
    @Override
	public int getIndex() {
        return index;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theIndex DOCUMENT ME!
     */
    @Override
	public void setIndex(int theIndex) {
        index = theIndex;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getTime() {
        return time;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTime DOCUMENT ME!
     */
    @Override
	public void setTime(long theTime) {
        // time = theTime;

        timeOrder.modifyTimeSlot(this, theTime);

        if (theTime >= 0) {
            isAligned = true;
        } else {
            isAligned = false;
        }
    }

    /**
     * Only to be called by TimeOrder !!
     *
     * @param theTime DOCUMENT ME!
     */
    @Override
	public void updateTime(long theTime) {
        time = theTime;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isTimeAligned() {
        return isAligned;
    }

    /**
     * Returns true if this timeSlot comes after the parameter timeSlot
     *
     * @param timeSlot the timeSlot against which is to be checked if this
     *        timeSlot comes after it.
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isAfter(TimeSlot timeSlot) {
        /*        if (isTimeAligned() && timeSlot.isTimeAligned() && time > timeSlot.getTime()) {
           return true;
           }
           if (isTimeAligned() && timeSlot.isTimeAligned() && time == timeSlot.getTime()) {
               if (index > timeSlot.getIndex()) {
                   return true;
               }
           }*/
        if (index > timeSlot.getIndex()) {
            return true;
        }

        return false;
    }
    
    /**
     * Returns the precalculated time for unaligned timeslots.
     * To disable this feature let this method always return -1.
     * @return the pre-calculated proposed time
     */
	public long getProposedTime() {
		//return -1;
		return proposedTime;
	}
	
	/**
	 * Sets the new proposed time after a change in related aligned timeslots.
	 * @param proposedTime the new proposed time
	 */   
	public void setProposedTime(long proposedTime) {
		this.proposedTime = proposedTime;
	}

    // here compareTo uses TimeSlot.getIndex, which is not a remote
    // method invocation, in contrast with timeOrder.startsBefore.
    // This works substantially faster.
    @Override
	public int compareTo(TimeSlot obj) {
        int ret = 1;

        if (this.getIndex() > obj.getIndex()) {
            ret = 1;
        }
        // NOTE: 0 case necessary, because TreeSet.remove uses compareTo
        // to test equality (?!)
        else if (this.getIndex() == obj.getIndex()) {
            ret = 0;
        } else {
            ret = -1;
        }

        return ret;
    }
}
