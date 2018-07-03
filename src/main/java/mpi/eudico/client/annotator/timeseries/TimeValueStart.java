package mpi.eudico.client.annotator.timeseries;

/**
 * Indicates the start or begin of a new segment in a non-continuous rate track.
 * In the visualization this means that no line will be drawn from the previous time-value to this 
 * time-value.
 * This class is just an indicator or flag that the track object can use in the visualization. 
 */
public class TimeValueStart extends TimeValue {

    /**
     * No-arg constructor.
     */
    public TimeValueStart() {
        super();
    }
    
    /**
     * Constructor
     * @param time the time
     * @param value the value
     */
    public TimeValueStart(long time, float value) {
        super(time, value);
    }
}
