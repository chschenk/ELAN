package mpi.eudico.client.annotator.recognizer.data;

import java.util.Comparator;

/**
 * Compares 2 selection objects, based on begin and end time.
 * 
 * @author Han Sloetjes
 */
public class SelectionComparator implements Comparator<RSelection> {
	/**
	 * Constructor.
	 */
	public SelectionComparator() {
		super();
	}

	/**
	 * First compares the begin times and then the end times.
	 * 
	 * @return -1 if the first object's begin time is less than the second's
	 */
	@Override
	public int compare(RSelection o1, RSelection o2) {
		if (o1.beginTime < o2.beginTime || 
				(o1.beginTime == o2.beginTime && o1.endTime < o2.endTime)) {
			return -1;
		} else if (o1.beginTime == o2.beginTime && o1.endTime == o2.endTime) {
			return 0;
		} else {
			return 1;
		}
	}

}
