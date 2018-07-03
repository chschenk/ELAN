package mpi.eudico.client.mediacontrol;

/**
 * Starts up a Thread with the task to deliver a ControllerEvent to a
 * ControllerListener.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 09-June-1998
 */
public class EventPoster implements Runnable {
    /** Holds value of property DOCUMENT ME! */
    ControllerListener listener;

    /** Holds value of property DOCUMENT ME! */
    ControllerEvent event;

    /**
     * Creates the Thread that tells the listener about the event.
     *
     * @param listener the ControllerListener that wants to receive
     *        ControllerEvents.
     * @param event the ControllerEvent that has to be posted to the listener.
     */
    public EventPoster(ControllerListener listener, ControllerEvent event) {
        this.listener = listener;
        this.event = event;
        new Thread(this, "EventPoster").start();
    }

    /**
     * Tell the listener about the event.
     */
    @Override
	public void run() {
        listener.controllerUpdate(event);
    }
}
