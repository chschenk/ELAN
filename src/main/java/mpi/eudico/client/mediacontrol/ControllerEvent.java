package mpi.eudico.client.mediacontrol;

/**
 * Event that is sent as a paramater to controllerUpdate by the
 * TimeLineController and the PeriodicUpdateController. The generic type
 * ControllerEvent is extended by more specific event types like StartEvent,
 * StopEvent and TimeEvent
 */
public class ControllerEvent {
    private Controller controller;

    /**
     * Construct the event for a Controller
     *
     * @param controller DOCUMENT ME!
     */
    public ControllerEvent(Controller controller) {
        this.controller = controller;
    }

    /**
     * Tell where the event came from
     *
     * @return DOCUMENT ME!
     */
    public Controller getSource() {
        return controller;
    }
}
