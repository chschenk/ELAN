package mpi.eudico.client.mediacontrol;

/**
 * Event that is sent as a parameter to controllerUpdate by the
 * TimeLineController and the PeriodicUpdateController. The new type TimeEvent
 * is created to be able to distinguish it from other controller events in the
 * controllerUpdate methods
 */
public class TimeEvent extends ControllerEvent {
    /**
     * Construct the TimeEVent for a specific Controller
     *
     * @param controller DOCUMENT ME!
     */
    public TimeEvent(Controller controller) {
        super(controller);
    }
}
