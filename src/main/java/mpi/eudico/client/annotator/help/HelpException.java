package mpi.eudico.client.annotator.help;

/**
 * An exception thrown when the help window can not be created, for whatever
 * reason.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class HelpException extends Exception {
    /**
     * Creates a new HelpException instance
     */
    public HelpException() {
        super();
    }

    /**
     * Creates a new HelpException instance
     *
     * @param message exception message
     */
    public HelpException(String message) {
        super(message);
    }

    /**
     * Creates a new HelpException instance
     *
     * @param message exception message
     * @param cause the cause
     */
    public HelpException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new HelpException instance
     *
     * @param cause the cause
     */
    public HelpException(Throwable cause) {
        super(cause);
    }
}
