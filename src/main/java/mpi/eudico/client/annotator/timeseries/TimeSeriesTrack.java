package mpi.eudico.client.annotator.timeseries;

/**
 * An interface describing a single time series data track.
 */
public interface TimeSeriesTrack extends Comparable<TimeSeriesTrack> {
    /** the values are stored in an int array */
    public static final int VALUES_INT_ARRAY = 0;

    /** the values are stored in an float array */
    public static final int VALUES_FLOAT_ARRAY = 1;

    /** the values are stored in an double array */
    public static final int VALUES_DOUBLE_ARRAY = 2;

    /** the values are stored in time-value pairs in a Map */
    public static final int TIME_VALUES_MAP = 10;

    /** the values are stored in TimeValue objects in a List */
    public static final int TIME_VALUE_LIST = 11;

    /**
     * Returns the name /id of the track.
     *
     * @return the name /id of the track
     */
    public String getName();

    /**
     * Sets the name /id of the track
     *
     * @param name the name /id of the track
     */
    public void setName(String name);

    /**
     * Returns the description of this track.
     *
     * @return the description of this track
     */
    public String getDescription();

    /**
     * Sets the description of this track
     *
     * @param description the description of this track
     */
    public void setDescription(String description);

    /**
     * Returns the range that has been set for this track. Might be an
     * estimated, expected range that does not necessarely represent the
     * actual minimum and maximum values in the track.
     *
     * @return the range that has been set for this track
     */
    public float[] getRange();

    /**
     * Sets the range that has been set for this track
     *
     * @param range the range that has been set for this track
     *
     * @see #getRange()
     */
    public void setRange(float[] range);

    /**
     * A String representation of the source this track has been extracted
     * from.
     *
     * @return the source of the track
     */
    public String getSource();

    /**
     * Sets the source for the track data.
     *
     * @param source the source
     */
    public void setSource(String source);

    /**
     * Returns the level of a derivation in case the data in this track is   a
     * derivative of the data in the samples.
     *
     * @return the level of derivation, 0 means no derivation (the raw data
     *         from the file)
     */
    public int getDerivativeLevel();

    /**
     * Sets the level of a derivation for the data in this track.
     *
     * @param level the level of derivation, 0 means no derivation
     */
    public void setDerivativeLevel(int level);

    /**
     * The sample rate of the track, always interpreted as number of samples
     * per second.  Presumes a fixed rate, i.e. a value for every x ms is
     * present.
     *
     * @param rate sample rate of the track
     */
    public void setSampleRate(float rate);

    /**
     * Returns the sample rate, i.e. number of samples per seconds.
     *
     * @return the sample rate
     *
     * @see #getSampleRate()
     */
    public float getSampleRate();

    /**
     * Retunrs the number of samples in this track.
     *
     * @return the number of samples
     */
    public int getSampleCount();

    /**
     * Returns the actual data of the track.
     * Probably a float[] or List&lt;TimeValue>?
     *
     * @return the actual data of the track
     */
    public Object getData();

    /**
     * Sets the actual data of the track.
     *
     * @param data the actual data of the track
     */
    public void setData(Object data);

    /**
     * Returns a String representation of the ruler's units, like m/s.
     *
     * @return a String representation of the ruler's units
     */
    public String getUnitString();

    /**
     * Sets the String representation of the ruler's units.
     *
     * @param unitString the String representation of the ruler's units
     */
    public void setUnitString(String unitString);

    /**
     * Finds and returns the minimum value within the given time interval.
     *
     * @param begin begin time of the interval (inclusive)
     * @param end end time of the interval (inclusive)
     *
     * @return the minimum value
     */
    public float getMinimum(long begin, long end);

    /**
     * Finds and returns the maximum value within the given time interval.
     *
     * @param begin begin time of the interval (inclusive)
     * @param end end time of the interval (inclusive)
     *
     * @return the maximum value
     */
    public float getMaximum(long begin, long end);

    /**
     * Finds and returns the average (arithmetic mean) of the values 
     * within the given time interval.
     *
     * @param begin begin time of the interval (inclusive)
     * @param end end time of the interval (inclusive)
     *
     * @return the average (arithmetic mean) of the values
     */
    public float getAverage(long begin, long end);
    
    /**
     * Finds and returns the sum of the values within the given time
     * interval.
     *
     * @param begin begin time of the interval (inclusive)
     * @param end end time of the interval (inclusive)
     *
     * @return the sum of the values
     */
    public float getSum(long begin, long end);
    
    /**
     * Finds and returns the value at the beginning of the given time
     * interval.
     *
     * @param begin begin time of the interval (inclusive)
     * @param end end time of the interval (inclusive)
     *
     * @return the value at the beginning of the interval
     */
    public float getValueAtBegin(long begin, long end);
    
    /**
     * Finds and returns the value at the end of the given time
     * interval.
     *
     * @param begin begin time of the interval (inclusive)
     * @param end end time of the interval (inclusive)
     *
     * @return the value at the end of the interval
     */
    public float getValueAtEnd(long begin, long end);
    
    /**
     * Finds and returns the median (the "middle" value) of the values within 
     * the given time interval.
     *
     * @param begin begin time of the interval (inclusive)
     * @param end end time of the interval (inclusive)
     *
     * @return the median of the values
     */
    public float getMedian(long begin, long end);
    
    /**
     * Finds and returns the range (difference between maximum and minimum) 
     * of the values within the given time interval.
     *
     * @param begin begin time of the interval (inclusive)
     * @param end end time of the interval (inclusive)
     *
     * @return the range (maximum - minimum) of the values
     */
    public float getRange(long begin, long end);
}
