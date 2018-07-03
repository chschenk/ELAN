package mpi.eudico.client.util;

/**
 * A subtitle unit holds a begin time, real end time, calculated end time  and
 * one or more lines of text.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class SubtitleUnit implements Comparable<SubtitleUnit> {
    private long begin;
    private long realEnd;
    private long calcEnd;
    private String[] values;
    private int lineIndex;

    /**
     * No-arg constructor.
     */
    public SubtitleUnit() {
        super();
    }

    /**
     * Constructor taking the begin time, end time and single line value.
     *
     * @param begin begin time
     * @param realEnd real end time
     * @param value subtitle value
     */
    public SubtitleUnit(long begin, long realEnd, String value) {
        this.begin = begin;
        this.realEnd = realEnd;
        calcEnd = realEnd;
        this.setValue(value);
    }
    
    /**
     * Constructor taking the begin time, end time, a line index and single line value.
     *
     * @param begin begin time
     * @param realEnd real end time
     * @param lineIndex the index in the collection of lines (or tiers)
     * @param value subtitle value
     */
    public SubtitleUnit(long begin, long realEnd, int lineIndex, String value) {
        this.begin = begin;
        this.realEnd = realEnd;
        calcEnd = realEnd;
        this.lineIndex = lineIndex;
        this.setValue(value);
    }

    /**
     * Returns the begin time
     *
     * @return the begin time
     */
    public long getBegin() {
        return begin;
    }

    /**
     * Sets the begin time
     *
     * @param begin the begin time
     */
    public void setBegin(long begin) {
        this.begin = begin;
    }

    /**
     * Returns the calculated end time, i.e. the largest of real end time amd
     * begin time + minimal duration. Or something in between if the next unit
     * starts before begin time + minimal duration.
     *
     * @return the calculated end time
     */
    public long getCalcEnd() {
        return calcEnd;
    }

    /**
     * Sets the calculated end time.
     *
     * @param calcEnd calculated end time
     *
     * @see #getCalcEnd()
     */
    public void setCalcEnd(long calcEnd) {
        this.calcEnd = calcEnd;
    }

    /**
     * Returns the real end time of the unit
     *
     * @return the real end time
     */
    public long getRealEnd() {
        return realEnd;
    }

    /**
     * Sets the real end time
     *
     * @param realEnd the real end time
     */
    public void setRealEnd(long realEnd) {
        this.realEnd = realEnd;
        
        if (realEnd > calcEnd) {
        	calcEnd = realEnd;
        }
    }

    /**
     * Returns the index in the collection of subtitle lines.
     * 
     * @return the index in the collection of subtitle lines
     */
    public int getLineIndex() {
		return lineIndex;
	}

    /**
     * Sets the index in the collection of subtitle lines.
     * 
     * @param lineIndex the index in the collection of lines
     */
	public void setLineIndex(int lineIndex) {
		this.lineIndex = lineIndex;
	}

    /**
     * Sets the single line of subtitle text
     *
     * @param value the subtitle text
     */
    public void setValue(String value) {
        this.values = new String[] { value };
    }

    /**
     * Returns the multiple lines of subtitle text
     *
     * @return the multiple lines of subtitle text
     */
    public String[] getValues() {
        return values;
    }

    /**
     * Sets the multiple lines of subtitle text
     *
     * @param values the multiple lines of subtitle text
     */
    public void setValues(String[] values) {
        this.values = values;
    }

    /**
     * First begin times are compared, then end times.
     *
     * @param unit the other unit to compare with
     *
     * @return -1 if this unit is less than the other, 1 if the other unit is
     *         less,  0 if both begin and end time are equal.
     *
     * @throws ClassCastException if unit is not a SubtitleUnit
     */
    @Override
	public int compareTo(SubtitleUnit u2) {
         if (begin < u2.getBegin()) {
            return -1;
        }

        if (begin > u2.getBegin()) {
            return 1;
        }

        if (realEnd < u2.getRealEnd()) {
            return -1;
        }

        if (realEnd > u2.getRealEnd()) {
            return 1;
        }

        // compare calculated ends??
        return 0;
    }
}
