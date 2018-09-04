package mpi.eudico.client.annotator.util;

import java.util.Comparator;

/**
 * Compares both begin and end time of 2 annotation data
 * records. The times may be interpolated times.
 */
public class AnnotationDataRecordComparator implements Comparator<AnnotationDataRecord> {
    /**
     * Note: this comparator imposes orderings that are inconsistent with
     * equals.
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
	public int compare(AnnotationDataRecord o1, AnnotationDataRecord o2) {
        long begin1 = o1.getBeginTime();
        long begin2 = o2.getBeginTime();

        // Then compare begin time
        if (begin1 < begin2) {
            return -1;
        } else if (begin1 > begin2) {
            return 1;
        }

        // Begin time equal, compare end time
        long end1 = o1.getEndTime();
        long end2 = o2.getEndTime();

        if (end1 < end2) {
            return -1;
        } else if (end1 > end2) {
            return 1;
        }

        return 0;
    }    
}
