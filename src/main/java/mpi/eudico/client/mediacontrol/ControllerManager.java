package mpi.eudico.client.mediacontrol;

import java.util.ArrayList;
import java.util.List;




/**
 * A ControllerManager takes care of informing interested Controllers about
 * media related events
 */
public class ControllerManager extends EventPostingBase {
    private List<Controller> controllers;
    private boolean controllersAreStarted;

    /**
     *
     */
    public ControllerManager() {
        controllers = new ArrayList<Controller>();
        controllersAreStarted = false;
    }

    /**
     * Add a Controller that has to be managed
     *
     * @param controller DOCUMENT ME!
     */
    public synchronized void addController(Controller controller) {
        if (!controllers.contains(controller)) {
            controllers.add(controller);
        }
    }

    /**
     * Remove a Controller that no longer has to be managed
     *
     * @param controller DOCUMENT ME!
     */
    public synchronized void removeController(Controller controller) {
        controllers.remove(controller);
    }

    /**
     * Start all managed Controllers
     */
    public void startControllers() {
        if (!controllersAreStarted) {
            final int size = controllers.size();
			for (int i = 0; i < size; i++) {
                controllers.get(i).start();
            }

            controllersAreStarted = true;
        }
    }

    /**
     * Stop all managed Controllers
     */
    public void stopControllers() {
        if (controllersAreStarted) {
            final int size = controllers.size();
			for (int i = 0; i < size; i++) {
                controllers.get(i).stop();
            }

            controllersAreStarted = false;
        }
    }

    /**
     * Set the stop time for all managed Controllers
     *
     * @param time DOCUMENT ME!
     */
    public void setControllersStopTime(long time) {
        final int size = controllers.size();
		for (int i = 0; i < size; i++) {
            controllers.get(i).setStopTime(time); 
        }
    }
    
    /**
     * Set the media time for all managed Controllers
     *
     * @param time DOCUMENT ME!
     */
    public void setControllersMediaTime(long time) {
        final int size = controllers.size();
		for (int i = 0; i < size; i++) {
            controllers.get(i).setMediaTime(time);
        }
    }

    /**
     * Set the rate for all managed Controllers
     *
     * @param rate DOCUMENT ME!
     */
    public void setControllersRate(float rate) {
        final int size = controllers.size();
		for (int i = 0; i < size; i++) {
            controllers.get(i).setRate(rate);
        }
    }
}
