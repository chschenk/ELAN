package mpi.eudico.client.annotator.recognizer.data;

/**
 * Generic container class for segment data. 
 * It adds a label to the segment.
 * 
 * @author albertr
 * @version Jan 2010 HS: Segment now extends RSelection
 */
public class Segment extends RSelection {
	public String label;   // segment content label
	
	/**
	 * No-arg constructor
	 */
	public Segment() {
		super(0, 0);
		//label = "";
	}
	
	/**
	 * Constructor with a begin time, end time and a label as arguments.
	 * 
	 * @param beginTime begin time of the segment
	 * @param endTime the end time of the segment
	 * @param label the (optional) label for this segment
	 */
	public Segment(long beginTime, long endTime, String label) {
		super(beginTime, endTime);
		this.label = label;
	}
}