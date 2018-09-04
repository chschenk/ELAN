package mpi.eudico.client.annotator.recognizer.data;

import java.awt.Shape;

/**
 * Segments in the video domain (probably) need more attributes
 * to define an area or trajectory of interest.
 * 
 * Currently one shape is supported per segment
 * 
 * @author Han Sloetjes 
 */
public class VideoSegment extends Segment {
	private Shape shape;
	
	/**
	 * No-arg constructor.
	 */
	public VideoSegment() {
		super();
	}

	/**
	 * Constructor with begin time, end time and label arguments.
	 * 
	 * @param beginTime the begin time
	 * @param endTime the end time
	 * @param label the label for this segment
	 */
	public VideoSegment(long beginTime, long endTime, String label) {
		super(beginTime, endTime, label);
		shape = null;
	}

	/**
	 * Constructor with begin time, end time, label and shape arguments.
	 * 
	 * @param beginTime the begin time
	 * @param endTime the end time
	 * @param label the label for this segment
	 * @param shape the region of interest
	 */
	public VideoSegment(long beginTime, long endTime, String label, Shape shape) {
		super(beginTime, endTime, label);
		this.shape = shape;
	}

	/**
	 * @return the shape
	 */
	public Shape getShape() {
		return shape;
	}

	/**
	 * @param shape the shape to set
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}	
}
