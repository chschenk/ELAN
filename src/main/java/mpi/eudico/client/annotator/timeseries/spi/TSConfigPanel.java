package mpi.eudico.client.annotator.timeseries.spi;

import mpi.eudico.client.annotator.timeseries.TimeSeriesChangeListener;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;

import javax.swing.JComponent;


/**
 * A Container class that can be added to a Dialog or Frame and that enables
 * selection and/or configuration of time series track from a source file.
 *
 * @author Han Sloetjes
 */
public abstract class TSConfigPanel extends JComponent {
    /**
     * Registers a listener for TimeSeriesChangeEvents.  After changes in
     * configuration (new track defined, track attributes changed etc)
     * listeners can/should be informed of the changes.
     *
     * @param l a listener for TimeSeriesChangeEvents
     */
    public abstract void addTimeSeriesChangeListener(TimeSeriesChangeListener l);

    /**
     * Sets the configuration object (containing a link to the source) that
     * needs to be configured.
     *
     * @param tssc the source configuration object
     */
    public abstract void setSourceConfiguration(TSSourceConfiguration tssc);
}
