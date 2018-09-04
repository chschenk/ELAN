package mpi.eudico.client.annotator.recognizer.data;
/**
 * For selections/segments in an audio signal the user typically selects
 * an interval in a signal viewer. For mono signals there is only 
 * one channel so the channel number will always be 1. For stereo
 * signals the channel number can be 1 or 2. The recognizer is 
 * free to treat a stereo file as mono.
 * 
 * For an audio signal the Segment defines the begin time, end time and
 * the category (label) of a pattern in the signal domain.
 * 
 * @author Han Sloetjes
 *
 */
public class AudioSegment extends Segment {
	public int channel;    // channel number, 1 = left or mono, 2 is right

	/**
	 * No-arg constructor.
	 */
	public AudioSegment() {
		super();
		channel = 1;
	}

	/**
	 * Constructor with begin time, end time and label arguments, the channel defaults to 1.
	 * 
	 * @param beginTime the begin time
	 * @param endTime the end time
	 * @param label the label for this segment
	 */
	public AudioSegment(long beginTime, long endTime, String label) {
		super(beginTime, endTime, label);
		channel = 1;
	}
	
	/**
	 * Constructor with begin time, end time, label and channel arguments.
	 * 
	 * @param beginTime the begin time
	 * @param endTime the end time
	 * @param label the label for this segment
	 * @param channel the channel id, 1 or 2
	 */
	public AudioSegment(long beginTime, long endTime, String label, int channel) {
		super(beginTime, endTime, label);
		this.channel = channel;
	}

}
