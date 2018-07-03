package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.TimeScale;
import mpi.eudico.client.annotator.TimeScaleUser;
import mpi.eudico.client.annotator.Zoomable;

/**
 * A viewer that has a time scale ruler in its view
 */
@SuppressWarnings("serial")
public abstract class TimeScaleBasedViewer extends AbstractViewer
    implements TimeScaleUser, Zoomable {
    private TimeScale timeScale;

    /** An array of zoomlevels. */
    public final int[] ZOOMLEVELS = new int[] {
            1, 5, 10, 25, 50, 75, 100, 150, 200, 300, 400, 500, 750, 1000
        };
    /** minimum value of milliseconds per pixel */
    final static float MIN_MSPP = 0.025f;
    
    public int dragScrollSleepTime = 10; 
    
    /**
     * Sets the TimeScale object for this viewer
     *
     * @param timeScale DOCUMENT ME!
     */
    @Override
	public void setGlobalTimeScale(TimeScale timeScale) {
        if (timeScale == null) {
            return;
        }

        this.timeScale = timeScale;
    }

    /**
     * Returns the first visible time of the global scale in milli seconds
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getGlobalTimeScaleIntervalBeginTime() {
        if (timeScale == null) {
            return 0;
        }

        return timeScale.getBeginTime();
    }

    /**
     * Sets the first visible time of the global scale in milli seconds
     *
     * @param time DOCUMENT ME!
     */
    @Override
	public void setGlobalTimeScaleIntervalBeginTime(long time) {
        if (timeScale == null) {
            return;
        }

        timeScale.setBeginTime(time);
    }

    /**
     * Returns the last visible time of the global scale in milli seconds
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getGlobalTimeScaleIntervalEndTime() {
        if (timeScale == null) {
            return 0;
        }

        return timeScale.getEndTime();
    }

    /**
     * Sets the last visible time of the global scale in milli seconds
     *
     * @param time DOCUMENT ME!
     */
    @Override
	public void setGlobalTimeScaleIntervalEndTime(long time) {
        if (timeScale == null) {
            return;
        }

        timeScale.setEndTime(time);
    }

    /**
     * Returns the duration of the visible interval of the global scale in
     * milli seconds
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getGlobalTimeScaleIntervalDuration() {
        if (timeScale == null) {
            return 0;
        }

        return timeScale.getIntervalDuration();
    }

    /**
     * Returns the step size of the global scale in milli seconds
     *
     * @return DOCUMENT ME!
     */
    @Override
	public float getGlobalTimeScaleMsPerPixel() {
        if (timeScale == null) {
            return 10f;
        }

        return timeScale.getMsPerPixel();
    }

    /**
     * Sets the step size of the global scale in milli seconds
     *
     * @param step DOCUMENT ME!
     */
    @Override
	public void setGlobalTimeScaleMsPerPixel(float step) {
        if (timeScale == null) {
            return;
        }

        timeScale.setMsPerPixel(step);
    }

    /**
     * Is called to notify the viewer that time scale related data is changed
     * the viewer can use the get methods to ask for the new values
     */
    @Override
	public abstract void updateTimeScale();

    /**
     * Returns the milli seconds per pixel for the time scale
     */

    //	public abstract int getMsPerPixel();

    /**
     * Returns the first visible time for the time scale
     */

    //	public abstract long getIntervalBeginTime();

    /**
     * Returns the last visible time for the time scale
     */

    //	public abstract long getIntervalEndTime();
    
}
