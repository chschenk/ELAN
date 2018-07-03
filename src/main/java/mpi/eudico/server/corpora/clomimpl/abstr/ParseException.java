package mpi.eudico.server.corpora.clomimpl.abstr;

/**
 * A generic exception that can be used by any of the clom parsers.
 *
 * @author Han Sloetjes
 */
public class ParseException extends RuntimeException {
    /**
     * No-arg constructor
     */
    public ParseException() {
        super();
    }

    /**
     * Message only constructor
     *
     * @param message a message
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Cause only constructor
     *
     * @param cause the cause
     */
    public ParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Message and cause constructor
     *
     * @param message a message
     * @param cause the cause
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
