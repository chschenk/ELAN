package mpi.eudico.client.mediacontrol;

import java.util.ArrayList;
import java.util.List;


/**
 * Takes care of administrating ControllerListeners and sending events to them.
 * Inspired by EventPostingBase as described in Appendix C of the JMF
 * documentation.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 09-June-1998
 */
public class EventPostingBase {
    private List<ControllerListener> listeners;

    /**
     * Creates a new posting base for Controller events.
     */
    public EventPostingBase() {
        listeners = new ArrayList<ControllerListener>();
    }

    /**
     * Returns the number of connected listeners
     *
     * @return DOCUMENT ME!
     */
    public int getNrOfConnectedListeners() {
        return listeners.size();
    }

    /**
     * Adds a ControllerListener only if it was not added before.
     *
     * @param listener the ControllerListener that wants to receive Controller
     *        events.
     */
    public synchronized void addControllerListener(ControllerListener listener) {
        // Check if the newListener is really new, if so add it to the listeners.
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a ControllerListener.
     *
     * @param listener the ControllerListener that no longer wants to receive
     *        Controller events.
     */
    public synchronized void removeControllerListener(
        ControllerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Posts a ControllerEvent to all registered listeners.
     *
     * @param event the Controller event that has to be posted to all
     *        registered listeners.
     */
    public synchronized void postEvent(ControllerEvent event) {
        for (ControllerListener cl : listeners) {
            new EventPoster(cl, event);
        }
    }
}
