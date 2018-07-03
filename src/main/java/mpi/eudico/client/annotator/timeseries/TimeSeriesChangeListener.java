package mpi.eudico.client.annotator.timeseries;

import java.util.EventListener;


/**
 * Defines a listener to TimeSeriesChangeEvents.
 *
 * @author Han Sloetjes
 */
public interface TimeSeriesChangeListener extends EventListener {
    /**
     * A time series object has changed. Can be a track or configuration object.
     *
     * @param event the change event
     */
    public void timeSeriesChanged(TimeSeriesChangeEvent event);
}
