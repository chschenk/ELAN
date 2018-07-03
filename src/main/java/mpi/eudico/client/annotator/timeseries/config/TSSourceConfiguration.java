package mpi.eudico.client.annotator.timeseries.config;

import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;


/**
 * A configuration class for timeseries data files. This class adds a few
 * fields to the generic option  of setting and getting properties.  The
 * source file has to be passed to the constructor. A time offset for all
 * tracks in the file can be specified, as well as  a default or preferred
 * reader classname and the sample type. Configuration objects of individual
 * tracks can be added to this configuration object.
 *
 * @author Han Sloetjes
 */
public class TSSourceConfiguration extends TSConfigurationImpl {
    private String source;
    private String providerClassName;
    private int timeOrigin = 0;
    private int timeColumn = -1;
    private String sampleType = TimeSeriesConstants.UNKNOWN_RATE_TYPE;

    /**
     * Creates a new TSSourceConfiguration instance
     *
     * @param source the source file path as a string
     */
    public TSSourceConfiguration(String source) {
        super();
        this.source = source;
    }

    /**
     * Returns the service provider classname, if specified.
     *
     * @return the fully qualified service provider classname
     */
    public String getProviderClassName() {
        return providerClassName;
    }

    /**
     * Returns the sample type string.
     *
     * @return a string representation of the sample type, fixed rate, variable
     *         rate etc
     */
    public String getSampleType() {
        return sampleType;
    }

    /**
     * Returns the source file path.
     *
     * @return the path to the source file
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the source file path.
     *
     * @param source the source file path
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Returns the time offset for tracks in this file.
     *
     * @return the time offset for tracks in this file
     */
    public int getTimeOrigin() {
        return timeOrigin;
    }

    /**
     * Sets the fully qualified class name of the service provider for  this
     * source file.
     *
     * @param string the fully qualified class name of the service provider
     */
    public void setProviderClassName(String string) {
        providerClassName = string;
    }

    /**
     * Sets the sample type.
     *
     * @param string the sample type as string
     *
     * @see TimeSeriesConstants
     */
    public void setSampleType(String string) {
        sampleType = string;
    }

    /**
     * Sets the time offset for the tracks in this file.
     *
     * @param offset the time offset in ms
     */
    public void setTimeOrigin(int offset) {
        timeOrigin = offset;
    }

    /**
     * Returns the index of the column containing time values in multi column
     * types of files.
     *
     * @return the index of the column or -1
     */
    public int getTimeColumn() {
        return timeColumn;
    }

    /**
     * Sets the index of the column containing time values in multi column
     * types of files.
     *
     * @param timeColumn the index of the column
     */
    public void setTimeColumn(int timeColumn) {
        this.timeColumn = timeColumn;
    }
}
