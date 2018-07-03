package mpi.eudico.client.mediacontrol;

/**
 * Event that is sent as a paramater to controllerUpdate by the
 * TimeLineController and the PeriodicUpdateController. The new type StopEvent
 * is created to be able to distinguish it from other ControllerEvent in the
 * controllerUpdate methods
 */
public class StopEvent extends ControllerEvent {
    /**
     * Construct the StopEvent for a specific Controller
     *
     * @param controller DOCUMENT ME!
     */
    public StopEvent(Controller controller) {
        super(controller);
    }
}
