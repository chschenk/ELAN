package mpi.eudico.client.annotator.timeseries;

/**
 * A class containing a field for a time in ms (long) and a field for an amplitude value (float).
 * The fields are public, no getters and setters provided.<br>
 * When there is a number of TimeValue objects in a non-continuous rate track, a line will be drawn between 
 * two subsequent time-value points.
 * The class implements Comparable; the compareTo method compares the time of the objects.  
 */
public class TimeValue implements Comparable<TimeValue> {
    public long time;
    public float value;
    
    /**
     * No arg constructor, fields are initialised to their default value, 0;
     */
    public TimeValue() {
        time = 0l;
        value = 0f;
    }
    
    /**
     * Constructor, sets the fields to the value of the arguments.
     * @param time the time
     * @param value the value or amplitude
     */
    public TimeValue(long time, float value) {
        this.time = time;
        this.value = value;
    }
    
    /**
     * Returns -1 if this object's time is less than the other object's time, 0 if the times are 
     * equal, 1 if this object's time is greater than the object's time.
     * @param o the TimeValue object to compare with
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
	public int compareTo(TimeValue o) {
        if (o.time > this.time) {
            return -1;
        } else if (o.time < this.time) {
            return 1;
        }
        
        return 0;
    }

}
