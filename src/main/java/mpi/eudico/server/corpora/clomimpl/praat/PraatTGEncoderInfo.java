package mpi.eudico.server.corpora.clomimpl.praat;

import mpi.eudico.server.corpora.clom.EncoderInfo;


/**
 * An Encoder Info class for the Praat TextGrid Encoder. Stores the
 * selection/interval begin and end time.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class PraatTGEncoderInfo implements EncoderInfo {
    /**
     * the 'xmin' value of the TextGrid file: the begin value of the selection
     * or 0
     */
    private long beginTime;

    /**
     * the 'xmax' value of the TextGrid file: the end of the selection or the
     * duration of the media file
     */
    private long endTime;

    /** the encoding for the Praat file */
    private String encoding;
    
    /**
     * in case of a media offset, add this value to all time values in the file
     */
    private long offset;
    
	/**
	 * for correct handling of media offsets it is necessary to explicitly 
	 * know if a selection is exported or the whole file
	 */
	private boolean exportSelection = false;

    /**
     * Constructor.
     */
    public PraatTGEncoderInfo() {
        super();
    }

    /**
     * Constructor.
     *
     * @param bt the (selection) begin time
     * @param et the (selection) end time
     */
    public PraatTGEncoderInfo(long bt, long et) {
        super();
        beginTime = bt;
        endTime = et;
    }

    /**
     * Returns the (selection) begin time.
     *
     * @return Returns the begin time.
     */
    public long getBeginTime() {
        return beginTime;
    }

    /**
     * Sets the (selection) begin time.
     *
     * @param beginTime The begin time to set.
     */
    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    /**
     * Returns the (selection) end time.
     *
     * @return Returns the end time.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Sets the (selection) end time.
     *
     * @param endTime The end time to set.
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the character encoding.
     *
     * @return the character encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding
     *
     * @param encoding the character encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns the media offset.
     * 
     * @return the media offset in ms
     */
	public long getOffset() {
		return offset;
	}

	/**
	 * Sets the media offset for recalculating annotation times.
	 * 
	 * @param offset the offset
	 */
	public void setOffset(long offset) {
		this.offset = offset;
	}
	
	/**
	 * Returns whether only a selection is exported or the whole file.
	 * 
	 * @return true if only a selection is exported
	 */
	public boolean isExportSelection() {
		return exportSelection;
	}

	/**
	 * Sets whether a selection is exported or the whole file.
	 * 
	 * @param exportSelection flag
	 */
	public void setExportSelection(boolean exportSelection) {
		this.exportSelection = exportSelection;
	}
}
