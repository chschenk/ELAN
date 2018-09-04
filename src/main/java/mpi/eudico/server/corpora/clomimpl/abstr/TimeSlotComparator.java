package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.TimeSlot;

import java.util.Comparator;


/**
 * Compares two TimeSlot objects by first comparing their time and next
 * (if one of the slots is unaligned) their index.
 *
 * @author Han Sloetjes
 */
public class TimeSlotComparator implements Comparator<TimeSlot> {
    /**
     * Compares two TimeSlot objects by first comparing their time and next
     * their index.
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
	public int compare(TimeSlot t1, TimeSlot t2) {
        if (t1.isTimeAligned() && t2.isTimeAligned()) {
            if (t1.getTime() < t2.getTime()) {
                return -1;
            } else if (t1.getTime() == t2.getTime()) {
                return 0;
            }

            return 1;
        } else {
            if (t1.getIndex() < t2.getIndex()) {
                return -1;
            } else if (t1.getIndex() == t2.getIndex()) {
                return 0;
            }

            return 1;
        }
    }
}
