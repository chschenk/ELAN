package mpi.eudico.server.corpora.clom;

import java.util.Iterator;
import java.util.List;


/**
 * TimeOrder encapsulates the ordering of TimeSlots in a Transcription. It is
 * considered to be part of the Transcription.  The TimeOrder is used when
 * comparing TimeSlots in the TimeSlot's compareTo method. Given a constructed
 * TimeOrder, it is then sufficient to add TimeSlots to a TreeSet, they will
 * be ordered according to the TimeOrder automatically.
 *
 * @author Hennie Brugman
 */
public interface TimeOrder {
    /**
     * Adds a TimeSlot to the TimeOrder at current position. The TimeSlot can
     * be either time-aligned or not time-aligned.
     *
     * @param theTimeSlot the TimeSlot to be inserted.
     */
    public void insertTimeSlot(TimeSlot theTimeSlot);

    /**
     * Adds a TimeSlot to the TimeOrder at current position. The TimeSlot can
     * be either time-aligned or not time-aligned. The TimeSlot is inserted
     * after 'afterSlot' and before 'beforeSlot'.
     *
     * @param theTimeSlot the TimeSlot to be inserted.
     */
    public void insertTimeSlot(TimeSlot theTimeSlot, TimeSlot afterSlot,
        TimeSlot beforeSlot);

    /**
     * DOCUMENT ME!
     *
     * @param theSlot DOCUMENT ME!
     */
    public void removeTimeSlot(TimeSlot theSlot);

    /**
     * A utility method to print the current state of TimeOrder to standard
     * output.
     */
    public void printTimeOrder();

    /**
     * Returns true if timeSlot1 starts before timeSlot2, according to the
     * order specified by the TimeOrder. Each TimeSlot can be either
     * time-aligned or  not time-aligned.
     *
     * @param timeSlot1 first TimeSlot to be compared.
     * @param timeSlot2 second TimeSlot to be compared.
     *
     * @return true if timeSlot1 starts before timeSlot2.
     */
    public boolean isBefore(TimeSlot timeSlot1, TimeSlot timeSlot2);

    /**
     * Get the TimeSlot before the given one.
     */
	TimeSlot getPredecessorOf(TimeSlot timeSlot);

	/**
     * Returns the number of elements in TimeOrder.
     *
     * @return DOCUMENT ME!
     */
    public int size();

    /**
     * Remove all time slots that are not referenced by any Annotation. If this
     * is not done the TimeOrder will grow continuously from generation to
     * generation of the XML document.
     */
    public void pruneTimeSlots();

    /**
     * Returns either time, or, in case of unaligned slots, a proposal for a
     * time.
     *
     * @return DOCUMENT ME!
     */
    public long proposeTimeFor(TimeSlot theSlot);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Iterator<TimeSlot> iterator();

    /**
     * DOCUMENT ME!
     *
     * @param ts DOCUMENT ME!
     * @param newTime DOCUMENT ME!
     */
    public void modifyTimeSlot(TimeSlot ts, long newTime);

	/**
	 * Increase the time value of part of the time slots with a certain 
	 * amount of ms.
	 *  
	 * @param fromTime only slots with time values greater than this value will be changed 
	 * @param shift the amount of ms to add to the time value
	 * @param lastFixedSlot the last slot that should not be changed 
	 * (the end time slot of a source annotation)
	 * @param otherFixedSlots optional other slots that should not be changed
	 *  
	 */
    public void shift(long fromTime, long shift, TimeSlot lastFixedSlot, 
		List<TimeSlot> otherFixedSlots);
    
    /**
     * Adds the specified amount of ms to the time value of aligned slots.
     * 
     * @param shiftValue the amount of ms to add to the time values
     * @throws IllegalArgumentException when any slot would get a negative time value
     */ 
    public void shiftAll(long shiftValue) throws IllegalArgumentException;

}
