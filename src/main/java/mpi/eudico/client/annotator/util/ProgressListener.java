package mpi.eudico.client.annotator.util;

/**
 * Listener for progress update messages.
 *
 * @author Han Sloetjes
 */
public interface ProgressListener {
    /**
     * Progress update notification.
     *
     * @param source the source object
     * @param percent progress percent complete, 0 - 100
     * @param message a status message
     */
    public void progressUpdated(Object source, int percent, String message);

    /**
     * Progress completed notification.
     *
     * @param source the source object
     * @param message a status message
     */
    public void progressCompleted(Object source, String message);

    /**
     * Progress interrupted notification.
     *
     * @param source the source object
     * @param message a status message
     */
    public void progressInterrupted(Object source, String message);
}
