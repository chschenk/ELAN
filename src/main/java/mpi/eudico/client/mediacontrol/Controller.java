package mpi.eudico.client.mediacontrol;



/**
 * Interface that gives the methods to be implemented by a Controller that is
 * connected to an ElanMediaPlayer
 */
public interface Controller {
    /**
     * DOCUMENT ME!
     */
    public void start();

    /**
     * DOCUMENT ME!
     */
    public void stop();

    /**
     * DOCUMENT ME!
     *
     * @param time DOCUMENT ME!
     */
    public void setMediaTime(long time);
    
    public void setStopTime(long time);

    /**
     * DOCUMENT ME!
     *
     * @param rate DOCUMENT ME!
     */
    public void setRate(float rate);

    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     */
    public void addControllerListener(ControllerListener listener);

    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     */
    public void removeControllerListener(ControllerListener listener);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getNrOfConnectedListeners();

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    public void postEvent(ControllerEvent event);
}
