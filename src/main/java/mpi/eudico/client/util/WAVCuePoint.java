package mpi.eudico.client.util;

/**
 * This class contains the information of one Cue Point in the tail of a
 * WAV-file, mainly the position (i.e. time) and a possible label (null, if
 * not specified in the file)  Created on Mar 16, 2004
 *
 * @author Alexander Klassmann
 * @version Mar 16, 2004
 */
public class WAVCuePoint {
    /** Holds value of property DOCUMENT ME! */
    private final int ID;

    /** Holds value of property DOCUMENT ME! */
    private final int position;

    /** Holds value of property DOCUMENT ME! */
    private final int chunkStart;

    /** Holds value of property DOCUMENT ME! */
    private final int blockStart;

    /** Holds value of property DOCUMENT ME! */
    private final int sampleOffset;
    private String label = null;
    private String note = null;

    /**
     * Creates a new WAVCuePoint instance
     *
     * @param ID DOCUMENT ME!
     * @param position DOCUMENT ME!
     * @param chunkStart DOCUMENT ME!
     * @param blockStart DOCUMENT ME!
     * @param sampleOffset DOCUMENT ME!
     */
    public WAVCuePoint(int ID, int position, int chunkStart, int blockStart,
        int sampleOffset) {
        this.ID = ID;
        this.position = position;
        this.chunkStart = chunkStart;
        this.blockStart = blockStart;
        this.sampleOffset = sampleOffset;
    }

    /**
     * DOCUMENT ME!
     *
     * @param label DOCUMENT ME!
     */
    protected void setLabel(String label) {
        this.label = label;
    }

    /**
     * DOCUMENT ME!
     *
     * @param note DOCUMENT ME!
     */
    protected void setNote(String note) {
        this.note = note;
    }

    /**
     * returns unique identification value (int!)
     *
     * @return int
     */
    public int getID() {
        return ID;
    }

    /**
     * returns play order position
     *
     * @return int
     */
    public int getPosition() {
        return position;
    }

    /**
     * returns byte Offset of DataChunk
     *
     * @return int
     */
    public int getChunkStart() {
        return chunkStart;
    }

    /**
     * returns Byte Offset to sample of First Channel
     *
     * @return int
     */
    public int getBlockStart() {
        return blockStart;
    }

    /**
     * returns Byte Offset to sample byte of First Channel
     *
     * @return int
     */
    public int getSampleOffset() {
        return sampleOffset;
    }

    /**
     * returns the label
     *
     * @return String
     */
    public String getLabel() {
        return label;
    }

    /**
     * returns the note
     *
     * @return String
     */
    public String getNote() {
        return note;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String toString() {
        return "ID            : " + ID + "\nPosition      : " + position +
        "\nChunk Start   : " + chunkStart + "\nBlock Start   : " + blockStart +
        "\nSample Offset : " + sampleOffset +
        ((label != null) ? ("\nLabel         : " + label) : "") +
        ((note != null) ? ("\nNote          : " + note) : "") + "\n";
    }
}
