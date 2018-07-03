package mpi.eudico.server.corpora.util;

import java.util.logging.Logger;


/**
 * An interface containing only a logger. Implementing classes can log to this
 * logger and will still be individually recognizable in the log output.
 *
 * @author Han Sloetjes
 */
public interface ServerLogger {
    /** A logger for server side, clom classes */
    public final Logger LOG = Logger.getLogger(ServerLogger.class.getName());
}
