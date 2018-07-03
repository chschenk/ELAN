package mpi.eudico.server.corpora.clomimpl.shoebox;

/**
 * Helper class with public access to index values.
 *
 * @author Han Sloetjes
 */
public class ToolboxWord {
    /** the real "x" position of the first character of a word in the Java string */
    public int realX;

    /** the recalculated "x" position of the first character; the recalculation is based
     * on number of non-spacing characters in the string as well as the number of 
     * bytes per character */
    public int calcX;

    /** the real, Java string, width of the word, the number of characters */
    public int realW;

    /** the recalculated width, based on number of non-spacing characters and number
     * of bytes per character */
    public int calcW;

    /** the actual word or unit */
    public String word = "";
    private String marker = "";

    /**
     * Creates a new empty ToolboxWord instance
     */
    public ToolboxWord() {
    }

    /**
     * Creates a new ToolboxWord instance of the specified word
     *
     * @param word the word or unit value
     */
    public ToolboxWord(String word) {
        this.word = word;
    }

    /**
     * Creates a new ToolboxWord instance
     *
     * @param marker the marker this word is part of
     * @param word the word or unit
     */
    public ToolboxWord(String marker, String word) {
        this.marker = marker;
        this.word = word;
    }

    /**
     * Sets the marker name.
     *
     * @param marker the marker to set
     */
    public void setMarkerName(String marker) {
        this.marker = marker;
    }

    /**
     * Returns the marker name.
     *
     * @return the marker name
     */
    public String getMarkerName() {
        return marker;
    }
}
