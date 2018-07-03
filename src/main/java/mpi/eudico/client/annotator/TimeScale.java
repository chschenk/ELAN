package mpi.eudico.client.annotator;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class TimeScale {
    private List<TimeScaleListener> listeners;
    private long timeScaleBeginTime;
    private long timeScaleEndTime;
    private float timeScaleMsPerPixel;

    /**
     * Creates an empty TimeScale (begin time == end time).
     */
    public TimeScale() {
        listeners = new ArrayList<TimeScaleListener>();

        timeScaleBeginTime = 0;
        timeScaleEndTime = 0;
        timeScaleMsPerPixel = 10f;
    }

    /**
     * Returns the begin time of the time scale in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    public long getBeginTime() {
        return timeScaleBeginTime;
    }

    /**
     * Sets the beginTime of the time scale in milli seconds.
     *
     * @param beginTime DOCUMENT ME!
     */
    public void setBeginTime(long beginTime) {
        // Only update if needed.
        if (timeScaleBeginTime != beginTime) {
            timeScaleBeginTime = beginTime;

            // Tell all the interested TimeScalelisteners about the change
            notifyListeners();
        }
    }

    /**
     * Returns the end time of the time scale in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    public long getEndTime() {
        return timeScaleEndTime;
    }

    /**
     * Sets the endTime of the time scale in milli seconds.
     *
     * @param endTime DOCUMENT ME!
     */
    public void setEndTime(long endTime) {
        // Only update if needed.
        if (timeScaleEndTime != endTime) {
            timeScaleEndTime = endTime;

            // Tell all the interested TimeScalelisteners about the change
            notifyListeners();
        }
    }

    /**
     * Returns the duration of the visible interval in the time scale in milli
     * seconds.
     *
     * @return DOCUMENT ME!
     */
    public long getIntervalDuration() {
        return timeScaleEndTime - timeScaleBeginTime;
    }

    /**
     * Returns the step size of the time scale in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    public float getMsPerPixel() {
        return timeScaleMsPerPixel;
    }

    /**
     * Sets the step size of the time scale in milli seconds.
     *
     * @param msPerPixel DOCUMENT ME!
     */
    public void setMsPerPixel(float msPerPixel) {
        // Only update if needed.
        if (timeScaleMsPerPixel != msPerPixel) {
            timeScaleMsPerPixel = msPerPixel;

            // Tell all the interested TimeScalelisteners about the change
            notifyListeners();
        }
    }

    /**
     * Add a listener for TimeScale events.
     *
     * @param listener the listener that wants to be notified for DisplayState
     *        events.
     */
    public void addTimeScaleListener(TimeScaleListener listener) {
        listeners.add(listener);
        listener.updateTimeScale();
    }

    /**
     * Remove a listener for TimeScale events.
     *
     * @param listener the listener that no longer wants to be notified for
     *        TimeScale events.
     */
    public void removeTimeScaleListener(TimeScaleListener listener) {
        listeners.remove(listener);
    }

    /**
     * Tell all the listeners what the current TimeScale is.
     */
    public void notifyListeners() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).updateTimeScale();
        }
    }
}
