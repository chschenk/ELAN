package mpi.eudico.client.annotator;

/**
 * Interface that gives the methods to be implemented by a TimeScaleUser
 */
public interface TimeScaleUser extends TimeScaleListener {
    /**
     * DOCUMENT ME!
     *
     * @param timeScale DOCUMENT ME!
     */
    public void setGlobalTimeScale(TimeScale timeScale);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getGlobalTimeScaleIntervalBeginTime();

    /**
     * DOCUMENT ME!
     *
     * @param time DOCUMENT ME!
     */
    public void setGlobalTimeScaleIntervalBeginTime(long time);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getGlobalTimeScaleIntervalEndTime();

    /**
     * DOCUMENT ME!
     *
     * @param time DOCUMENT ME!
     */
    public void setGlobalTimeScaleIntervalEndTime(long time);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getGlobalTimeScaleIntervalDuration();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public float getGlobalTimeScaleMsPerPixel();

    /**
     * DOCUMENT ME!
     *
     * @param step DOCUMENT ME!
     */
    public void setGlobalTimeScaleMsPerPixel(float step);

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateTimeScale();
}
