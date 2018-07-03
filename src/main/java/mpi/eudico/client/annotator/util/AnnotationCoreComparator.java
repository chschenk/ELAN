package mpi.eudico.client.annotator.util;

import java.util.Comparator;

import mpi.eudico.server.corpora.clom.AnnotationCore;

public class AnnotationCoreComparator implements Comparator<AnnotationCore> {

	@Override
	public int compare(AnnotationCore o1, AnnotationCore o2) {
        long begin1 = o1.getBeginTimeBoundary();
        long begin2 = o2.getBeginTimeBoundary();

        // Compare begin time
        if (begin1 < begin2) {
            return -1;
        } else if (begin1 > begin2) {
            return 1;
        }

        // Begin time equal, compare end time
        long end1 = o1.getEndTimeBoundary();
        long end2 = o2.getEndTimeBoundary();

        if (end1 < end2) {
            return -1;
        } else if (end1 > end2) {
            return 1;
        }

        return 0;
	}

}
