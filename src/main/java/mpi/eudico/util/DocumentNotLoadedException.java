package mpi.eudico.util;

/**
 * Indicates that a document could not be loaded, for whatever reason.
 * Wrapper.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DocumentNotLoadedException extends Exception {
    /**
     * Creates a new DocumentNotLoadedException instance
     */
    public DocumentNotLoadedException() {
    }

    /**
     * Creates a new DocumentNotLoadedException instance
     *
     * @param message the message
     */
    public DocumentNotLoadedException(String message) {
        super(message);
    }

    /**
     * Creates a new DocumentNotLoadedException instance
     *
     * @param cause the cause
     */
    public DocumentNotLoadedException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new DocumentNotLoadedException instance
     *
     * @param message the message
     * @param cause the cause
     */
    public DocumentNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}
