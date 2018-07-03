package mpi.eudico.server.corpora.event;

/**
 * Exception that can be thrown whenever an attempt is made to edit an object
 * such that it could lead to an invalid state of the object or effected
 * objects.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
public class IllegalEditException extends Exception {
    /**
     * Creates a new IllegalEditException instance
     */
    public IllegalEditException() {
        super();
    }

    /**
     * Creates a new IllegalEditException instance
     *
     * @param message the message
     */
    public IllegalEditException(String message) {
        super(message);
    }
}
