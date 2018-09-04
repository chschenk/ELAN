package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.util.ProgressListener;

/**
 * An abstract command class for commands that do their work in a separate thread and 
 * report their progress to one or more listeners.
 * 
 */
public abstract class AbstractProgressCommand implements Command {
    private List<ProgressListener> listeners;
    private String commandName;
    /* a flag to store the cancelled or interrupted state, for cancellation from the "outside" (e.g. the user) */
    protected boolean cancelled = false;
    /* a flag to store some erroneous state that leads to programmatic interruption */ 
    protected boolean errorOccurred = false;
    /* a current progress field, counting from 0 to 100 */
	protected float curProgress = 0.0f;
   
    /**
     * 
     * @param theName name of the command
     */
	public AbstractProgressCommand(String theName) {
		commandName = theName;
	}

	@Override
	public void execute(Object receiver, Object[] arguments) {

	}

	@Override
	public String getName() {
		return commandName;
	}

    /**
     * Adds a ProgressListener to the list of ProgressListeners.
     *
     * @param pl the new ProgressListener
     */
    public synchronized void addProgressListener(ProgressListener pl) {
        if (listeners == null) {
            listeners = new ArrayList<ProgressListener>(2);
        }

        listeners.add(pl);
    }

    /**
     * Removes the specified ProgressListener from the list of listeners.
     *
     * @param pl the ProgressListener to remove
     */
    public synchronized void removeProgressListener(ProgressListener pl) {
        if ((pl != null) && (listeners != null)) {
            listeners.remove(pl);
        }
    }
    
    /**
     * Request to stop the ongoing process. This method just sets a flag. 
     * 
     * In case of success this might result in a progressInterrupted call.
     */
    public void cancelProcess() {
    	cancelled = true;
    }

    /**
     * Notifies any listeners of a progress update.
     *
     * @param percent the new progress percentage, [0 - 100]
     * @param message a descriptive message
     */
    protected void progressUpdate(int percent, String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressUpdated(this,
                    percent, message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has completed.
     *
     * @param message a descriptive message
     */
    protected void progressComplete(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressCompleted(this,
                    message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has been interrupted.
     *
     * @param message a descriptive message
     */
    protected void progressInterrupt(String message) {
    	errorOccurred = true;
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressInterrupted(this,
                    message);
            }
        }
    }
}
