package mpi.eudico.util;

import java.util.Comparator;

/**
 * Compares two TimeInterval objects.<br>
 * Note: this comparator imposes orderings that are inconsistent with
 * equals.
 *
 * @author Han Sloetjes
 */
public class TimeIntervalComparator implements Comparator<TimeInterval> {
    /**
     * Compares two TimeInterval objects. First the begin times are
     * compared. If they are the same the end times  are compared.  Note:
     * this comparator imposes orderings that are inconsistent with
     * equals.
     *
     * @param o1 the first interval
     * @param o2 the second interval
     *
     * @return -1 if the first interval is before the second, 0 if begin and end times 
     * are equal, 1 if the second interval is before the first
     *
     * @throws NullPointerException when either of the arguments is null
     * @throws ClassCastException when either object is not a TimeInterval
     *
     * @see java.util.Comparator#compare(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
	public int compare(TimeInterval o1, TimeInterval o2) throws NullPointerException, ClassCastException {
    	if (o1 == null) {
    		throw new NullPointerException("The first TimeInterval is null");
    	}
    	if (o2 == null) {
    		throw new NullPointerException("The second TimeInterval is null");
    	}
    	
        if (o1.getBeginTime() < o2.getBeginTime()) {
            return -1;
        }

        if ((o1.getBeginTime() == o2.getBeginTime()) &&
                (o1.getEndTime() < o2.getEndTime())) {
            return -1;
        }

        if ((o1.getBeginTime() == o2.getBeginTime()) &&
                (o1.getEndTime() == o2.getEndTime())) {
            return 0;
        }

        return 1;
    }
}