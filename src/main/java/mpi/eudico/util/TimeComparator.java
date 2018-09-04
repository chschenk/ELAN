package mpi.eudico.util;

import java.util.Comparator;

import javax.media.Time;


/**
 * Class to compare JMF Time objects. With the help of this Comparator it is
 * easy to build an ordered set of Times that belong to Tag/Tuple begin and
 * end times. This ordered set can be used to make the Time[] for the
 * TimeLineController.
 */
public class TimeComparator implements Comparator {
    /**
     * Two times are equal if they differ less than one millisecond
     *
     * @see java.util.Comparator#compare(Object, Object)
     */
    @Override
	public int compare(Object obj1, Object obj2) {
        return (int) ((1000 * ((Time) obj1).getSeconds()) -
        (1000 * ((Time) obj2).getSeconds()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param obj DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean equals(Object obj) {
        return false;
    }
}
