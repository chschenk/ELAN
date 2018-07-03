package mpi.eudico.client.annotator.timeseries;

import java.awt.Color;

/**
 * Abstract time series track.
 *
 * @author Han Sloetjes
 */
public abstract class AbstractTSTrack implements TimeSeriesTrack {
    protected Color color;
	protected float[] range;
	protected int datatype;
	protected String units;
	protected String name;
	protected String description;
	protected String source;
	protected int timeOffset;
	protected int derivativeLevel;

    /**
     * Creates a new AbstractTSTrack instance
     */
    public AbstractTSTrack() {
    	this ("nk", "-");
    }

    /**
     * Creates a new AbstractTSTrack instance
     *
     * @param name the name of the track
     * @param description the description
     */
    public AbstractTSTrack(String name, String description) {
    	this(name, description, TimeSeriesTrack.VALUES_FLOAT_ARRAY);
    }

    /**
     * Creates a new AbstractTSTrack instance
     *
     * @param name the name of the track
     * @param description the description
     * @param type the type of the track data
     */
    public AbstractTSTrack(String name, String description, int type) {
    	this.name = name;
    	this.description = description;
    	this.datatype = type;
    }

    /**
     * lefvert - May 9 2006
     * Compares two AbstracktTSTracks based on track name.
     *
     * @param object the track to compare this track to
     * @returns -1 if object is smaller than this track, 0 if they are equal, and 1 if object is greater.
     */
    @Override
	public int compareTo(TimeSeriesTrack object){ 
    	return name.compareTo(object.getName()); 
    } 
    
    /**
     * Returns the color used for rendering.
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color.
     *
     * @param color the color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Returns the name /id of the track.
     *
     * @return the name /id of the track
     */
    @Override
	public String getName() {
        return name;
    }

    /**
     * Sets the name /id of the track
     *
     * @param name the name /id of the track
     */
    @Override
	public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this track.
     *
     * @return the description of this track
     */
    @Override
	public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this track
     *
     * @param description the description of this track
     */
    @Override
	public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the (estimated or calculated) range of this track.
     *
     * @return the range, float array of size 2; min, max
     */
    @Override
	public float[] getRange() {
        return range;
    }

    /**
     * Sets the (estimated or calculated) range of this track.
     *
     * @param range the new range, float array of size 2
     */
    @Override
	public void setRange(float[] range) {
        this.range = range;
    }

	/**
	 * A String representation of the source this track has been extracted
	 * from.
	 *
	 * @return the source of the track
	 */
	@Override
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source for the track data.
	 *
	 * @param source the source
	 */
	@Override
	public void setSource(String source) {
		this.source = source;
	}

    /**
     * Returns the level of a derivation in case the data in this track is  
     * a derivative of the data in the samples.
     *
     * @return the level of derivation, 0 means no derivation (the raw data from the file)
     */
	@Override
	public int getDerivativeLevel() {
		return derivativeLevel;
	}

    /**
     * Sets the level of a derivation for the data in this track.
     *
     * @param int the level of derivation, 0 means no derivation
     */
	@Override
	public void setDerivativeLevel(int level) {
		derivativeLevel = level;
	}

	/**
	 * Returns a String representation of the ruler's units, like m/s.
	 *
	 * @return a String representation of the ruler's units
	 */
	@Override
	public String getUnitString() {
		return units;
	}

	/**
	 * Sets the String representation of the ruler's units.
	 *
	 * @param unitString the String representation of the ruler's units
	 */
	@Override
	public void setUnitString(String unitString) {
		units = unitString;
	}
			
    /**
     * Returns the type, one of the constants in TimeSeriesTrack.
     *
     * @return the type of track data
     */
    public int getType() {
        return datatype;
    }

    /**
     * Sets the data type of this track, one of the constants in TimeSeriesTrack.
     *
     * @param type the type
     */
    public void setType(int type) {
        datatype = type;
    }

    /**
     * Returns the time offset into the data stream (point zero).
     *
     * @return the offset
     */
    public int getTimeOffset() {
        return timeOffset;
    }

    /**
     * Set the time offset.
     *
     * @param timeOffset the time offset
     */
    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

	/**
	 * Returns the index of the value for the specified time, rounded down.
	 * 
	 * @param time the time
	 * @return the index of the value for the specified time, rounded down
	 */
	public abstract int getIndexForTime(long time);

	/**
	 * Returns the begin time of the sample at the given sample index.
	 * The value is rounded down.
	 * As a result time - sample comparisons are begin time inclusive, end 
	 * time exclusive. e.g. if each sample has a duration of 40 ms sample 0 
	 * is from 0 - 39 ms, sample 1 from 40 - 79 ms etc.
	 * 
	 * @param index the sample index
	 * @return the time value for the sample, i.e. the begin time of the sample
	 * interval
	 */	
	public abstract long getTimeForIndex(int index);
	
	/**
	 * Returns the time of the last sample or the largest time value in the data. 
	 * 
	 * @return the time stamp or time value of the last sample in the data, returns -1
	 * in case there's no data or the "duration" could not be retrieved
	 */
	public long getDataDuration() {
		return -1;
	}
}
