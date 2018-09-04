package mpi.eudico.util;

import java.io.Serializable;

/**
 * Represents a time interval identified by a begin time and an end time.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class TimeInterval implements Serializable {
    protected long beginTime;
    protected long endTime;

    /**
     * Creates a new TimeInterval instance.
     * No checks are performed on the parameters!
     *
     * @param theBeginTime the begin time
     * @param theEndTime the end time
     */
    public TimeInterval(long theBeginTime, long theEndTime) {
        beginTime = theBeginTime;
        endTime = theEndTime;
    }

    /**
     * Returns the begin time
     *
     * @return the begin time
     */
    public long getBeginTime() {
        return beginTime;
    }

    /**
     * Returns the end time
     *
     * @return the end time
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Returns the duration, is end time - begin time
     *
     * @return the duration of the interval (can be negative because the begin and end time are not checked).
     */
    public long getDuration() {
        return endTime - beginTime;
    }
}
