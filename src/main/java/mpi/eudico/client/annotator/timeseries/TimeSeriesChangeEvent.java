package mpi.eudico.client.annotator.timeseries;

import java.util.EventObject;


/**
 * An event used to notify that (the configuration of) a track or 
 * (the configuration of) a timeseries source has changed.
 *
 * @author Han Sloetjes
 */
public class TimeSeriesChangeEvent extends EventObject {
    // constants

    /** edit type add: an object has been added */
    public static final int ADD = 0;

    /** edit type change: an object has been changed */
    public static final int CHANGE = 1;

    /** edit type delete: an object has been deleted */
    public static final int DELETE = 2;

    // subtypes for changes

    /** identifies a change in a timeseries source (or its configuration) */
    public static final int TS_SOURCE = 100;

    /** a change in one or more tracks */
    public static final int TRACK = 101;
    
    /** a change in one or more tracks */
    public static final int TRACK_AND_PANEL = 102;    
    
    private int editType = CHANGE;
    private int editSourceType = TS_SOURCE;

    /**
     * Constructor
     *
     * @param source the source object of the event
     */
    public TimeSeriesChangeEvent(Object source) {
        super(source);
    }

    /**
     * Creates a new TimeSeriesChangeEvent instance
     *
     * @param source the source
     * @param editType the edit type, ADD, CHANGE or DELETE
     */
    public TimeSeriesChangeEvent(Object source, int editType) {
        this(source);
        this.editType = editType;
    }

    /**
     * Creates a new TimeSeriesChangeEvent instance
     *
     * @param source the source
     * @param editType the edit type, ADD, CHANGE or DELETE
     * @param editSourceType the (sub)type of source that has been changed, TRACK or TS_SOURCE
     */
    public TimeSeriesChangeEvent(Object source, int editType, int editSourceType) {
        this(source, editType);
        this.editSourceType = editSourceType;
    }

    /**
     * Returns the type of source that has been changed, either TRACK or
     * TS_SOURCE. When a TimeSeries source has changed extracted tracks might
     * also have changed.
     *
     * @return the (sub)type of source that has been changed, TRACK or TS_SOURCE
     */
    public int getEditSourceType() {
        return editSourceType;
    }

    /**
     * Returns the type of edit.
     *
     * @return the type of edit, ADD, CHANGE or DELETE
     */
    public int getEditType() {
        return editType;
    }
}
