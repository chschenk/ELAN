package mpi.eudico.server.corpora.util;

/**
 * Interface for a process reporting. For the time being supports one report to
 * report to.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
public interface ProcessReporter {
    /**
     * Sets the report object to append report messages to.
     *
     * @param report the report object
     */
    public void setProcessReport(ProcessReport report);

    /**
     * Returns the report object, containing the report messages.
     *
     * @return the report, or null if no report was set
     */
    public ProcessReport getProcessReport();

    /**
     * Adds a message to the report.
     *
     * @param message the message
     */
    public void report(String message);
}
