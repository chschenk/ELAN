package mpi.eudico.client.annotator.util;

/**
 * A utility class to store old and new begin and end times of an annotation
 * that has  been moved.
 *
 * @author Han Sloetjes
 */
public class TimeShiftRecord {
    /** old begintime value */
    public final long oldBegin;

    /** old endtime value */
    public final long oldEnd;

    /** new begintime value */
    public final long newBegin;

    /** new endtime value */
    public final long newEnd;

    /** the time distance between old and new times */
    public final int shift;

    /**
     * Creates a new TimeShiftRecord instance.
     *
     * @param oldBegin old begin value
     * @param oldEnd old end value
     * @param shiftDistance the displacement in ms
     */
    public TimeShiftRecord(long oldBegin, long oldEnd, int shiftDistance) {
        this.oldBegin = oldBegin;
        this.oldEnd = oldEnd;
        this.shift = shiftDistance;

        if ((oldBegin + shiftDistance) < 0) {
            newBegin = 0;
        } else {
            newBegin = oldBegin + shiftDistance;
        }

        if ((oldEnd + shiftDistance) < 0) {
            newEnd = 0;
        } else {
            newEnd = oldEnd + shiftDistance;
        }
    }
}
