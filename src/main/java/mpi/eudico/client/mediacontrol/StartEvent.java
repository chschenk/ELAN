package mpi.eudico.client.mediacontrol;

/**
 * Event that is sent as a paramater to controllerUpdate by the
 * TimeLineController and the PeriodicUpdateController. The new type
 * StartEvent is created to be able to distinguish it from other
 * ControllerEvent in the controllerUpdate methods
 */
public class StartEvent extends ControllerEvent {
    /**
     * Construct the StartEvent for a specific Controller
     *
     * @param controller DOCUMENT ME!
     */
    public StartEvent(Controller controller) {
        super(controller);
    }
}
