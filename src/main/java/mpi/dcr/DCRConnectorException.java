package mpi.dcr;

/**
 * This class is an exception wrapper for any excepions that get thrown in the
 * DCR connection proces
 *
 * @author markem
 * @version 1.0
 */
public class DCRConnectorException extends Exception {
    /** serialVersionUID */
    private static final long serialVersionUID = -303440770049705699L;
    private Throwable m_throwable;

    /**
     * Creates a new DCRConnectorException instance
     *
     * @param a_message the message
     */
    public DCRConnectorException(String a_message) {
        super(a_message);
    }

    /**
     * Creates a new DCRConnectorException instance
     *
     * @param a_message the message
     * @param a_throwable the cause
     */
    public DCRConnectorException(String a_message, Throwable a_throwable) {
        super(a_message);
        this.setThrowable(a_throwable);
    }

    /**
     * Creates a new DCRConnectorException instance
     *
     * @param a_throwable the cause!
     */
    public DCRConnectorException(Throwable a_throwable) {
        super();
        this.setThrowable(a_throwable);
    }

    /**
     * Returns the cause
     *
     * @return the cause
     */
    public Throwable getThrowable() {
        return m_throwable;
    }

    /**
     * Sets the cause of the exception.
     *
     * @param a_throwable the cause
     */
    protected void setThrowable(Throwable a_throwable) {
        m_throwable = a_throwable;
    }
}
