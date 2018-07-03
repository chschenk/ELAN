package mpi.eudico.client.mediacontrol;


/**
 * Interface that gives the methods to be implemented by a ControllerListener.
 * A ControllerListener will be called from a Controller at specific times and
 * wil receive a ControllerEvent
 */
public interface ControllerListener {
    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    public void controllerUpdate(ControllerEvent event);

    //	public void synchronizedControllerUpdate(ControllerEvent event);
}
