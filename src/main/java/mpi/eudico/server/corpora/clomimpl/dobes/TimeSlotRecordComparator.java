package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.Comparator;

/**
 * A comparator class that compares 2 timeslot records by first comparing the time value 
 * and next the id.
 * 
 * @author Han Sloetjes
 */
public class TimeSlotRecordComparator implements Comparator<TimeSlotRecord> {

	/**
	 * Constructor
	 */
	public TimeSlotRecordComparator() {
		super();
	}
	
	/**
	 * Compares 2 time slot records, by first comparing their time value and then their id or index.
	 * 
	 * @param tsr1 the first record
	 * @param tsr2 the second record
	 * 
	 * @return -1 if the first record is before the second, 1 if the first record is after the second, 0 otherwise 
	 */
	@Override
	public int compare(TimeSlotRecord tsr1, TimeSlotRecord tsr2) {
		
		if (tsr1.getValue() < tsr2.getValue()) {
			return -1;
		}
		if (tsr1.getValue() > tsr2.getValue()) {
			return 1;
		}
		if (tsr1.getValue() == tsr2.getValue()) {
			if (tsr1.getId() < tsr2.getId()) {
				return -1;
			}
			if (tsr1.getId() > tsr2.getId()) {
				return 1;
			}
		}
		
		return 0;
	}

}
