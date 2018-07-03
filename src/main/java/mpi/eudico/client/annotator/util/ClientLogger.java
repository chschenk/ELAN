package mpi.eudico.client.annotator.util;

import java.util.logging.Logger;


/**
 * An interface containing only a logger. Implementing classes can log to this
 * logger and will still be individually recognizable  in the log output.
 *
 * @author Han Sloetjes
 */
public interface ClientLogger {
    /** A logger for client side elan classes */
    public final Logger LOG = Logger.getLogger(ClientLogger.class.getName());
}
