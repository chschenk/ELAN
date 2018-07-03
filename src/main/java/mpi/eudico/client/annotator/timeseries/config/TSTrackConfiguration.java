package mpi.eudico.client.annotator.timeseries.config;

import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;


/**
 * A configuration object that serves as a wrapper for one TimeSeriesTrack object 
 * and that adds some fields/properties necessary for storage and retrieval of
 * settings (and that don't belong in a track object).
 * Above that any property can be added and any object that might be neede or useful.
 * The track object is stored in the object map with the track name as key.
 * 
 * @author Han Sloetjes
 */
public class TSTrackConfiguration extends TSConfigurationImpl {
    private String trackName;

    /**
     * Creates a new TSTrackConfiguration instance
     *
     * @param name the name of the track
     */
    public TSTrackConfiguration(String name) {
        trackName = name;
    }

    /**
     * Creates a new TSTrackConfiguration instance
     *
     * @param name the name of the track
     * @param track the track object; it is added to the object map
     */
    public TSTrackConfiguration(String name, Object track) {
        this(name);
        putObject(name, track);
    }

    /**
     * Get the sample position(s) the data of this track is based upon.
     * 
     * @return the sample position(s) or null
     */
    public SamplePosition getSamplePos() {
        return (SamplePosition) getObject(TimeSeriesConstants.SAMPLE_POS);
    }

    /**
     * Returns the name of the track
     *
     * @return the name of the track
     */
    public String getTrackName() {
        return trackName;
    }

    /**
     * Sets the sample position(s) this track is based upon.
     *
     * @param position the sample position(s)
     */
    public void setSamplePos(SamplePosition position) {
    	if (position != null) {
    		putObject(TimeSeriesConstants.SAMPLE_POS, position);
    	}
    }

    /**
     * Sets the name of the track. If a track object already had been
     * registered  with an old name, it will be removed from the map and
     * registered again with the new name.
     *
     * @param string the name of the track
     */
    public void setTrackName(String string) {
        Object track = getObject(trackName);
        trackName = string;

        if (track != null) {
            putObject(trackName, track);
        }
    }
}
