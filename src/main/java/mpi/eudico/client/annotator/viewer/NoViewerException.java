package mpi.eudico.client.annotator.viewer;

/**
 * Exception that is thrown when it was not possible to create an Elan Viewer
 */
public class NoViewerException extends Exception {
    /**
     * Creates a new NoViewerException instance
     *
     * @param message DOCUMENT ME!
     */
    public NoViewerException(String message) {
        super(message);
    }
}
