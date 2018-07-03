package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * A class containing one or multiple lines of a Toolbox record that is part of 
 * an interlinearized block of markers.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class InterlinearToolboxLine implements ToolboxLine {
    private static final String blockSep = "\n\t\n";
    private static final int BLOCK_INDEX = 1000000;
    private ToolboxLine parent;
    private String marker = "";
    private String line = "";
    private ArrayList<ToolboxWord> indexedWords;
    private int[] startIndices;
    private boolean correctForMultipleByteChars = true;

    /**
     * Constructor
     *
     * @param marker the marker
     * @param line the (first) line of this marker in a record
     */
    public InterlinearToolboxLine(String marker, String line) {
        super();
        this.marker = marker;
        this.line = line;
    }

    /**
     * Returns the marker name
     *
     * @return the marker name
     */
    @Override
	public String getMarkerName() {
        return marker;
    }

    /**
     * Returns the parent Toolbox line
     *
     * @return the parent Toolbox line
     */
    @Override
	public ToolboxLine getParent() {
        return parent;
    }

    /**
     * Sets the flag whether the indices of individual units (words) should be 
     * corrected based on the number of bytes per character
     *
     * @param correct if false the indices are not corrected for the number of bytes,
     * defaults to true
     */
    public void setCorrectForMultipleByteChars(boolean correct) {
        correctForMultipleByteChars = correct;
    }

    /**
     * Sets the parent of this line
     *
     * @param parent the parent to set
     */
    @Override
	public void setParent(ToolboxLine parent) {
        this.parent = parent;
    }

    /**
     * Appends a line. A Toolbox record can contain multiple lines of the 
     * same marker. A special separator is inserted to accommodate later alignment
     * with other markers.
     *
     * @param appLine the line to append
     */
    @Override
	public void appendLine(String appLine) {
        if (appLine != null) {
            line = line + blockSep + appLine;
        }
    }

    /**
     * Finds the word (starting at) the specified index, or the closest match.
     * The method #createIndices() should have been called first.
     *
     * @param index the index of the first character
     * @param calcLength the calculated/corrected length of the word
     *
     * @return the word or null
     */
    public ToolboxWord getWordAtIndex(int index, int calcLength) {
        ToolboxWord tw = null;
        ToolboxWord countTw = null;

        for (int i = 0; i < indexedWords.size(); i++) {
            countTw = indexedWords.get(i);

            if (countTw.calcX == index) {
                return countTw;
            }

            if (countTw.calcX > index) {
                // the previous is likely the hit
                if (tw != null) {
                    if ((index + calcLength) <= (tw.calcX + tw.calcW)) {
                        return tw;
                    } else {
                        // if there is little overlap with tw try countTw instead
                        int overlap1 = (tw.calcX + tw.calcW) - index;
                        int overlap2 = (index + calcLength) - countTw.calcX;

                        if (overlap1 > overlap2) {
                            return tw;
                        } else if (overlap1 < overlap2) {
                            return countTw;
                        } else {
                            // same amount of overlap
                            if ((index - tw.calcX) < (countTw.calcX - index)) {
                                return countTw;
                            } else if ((index - tw.calcX) > (countTw.calcX -
                                    index)) {
                                return tw;
                            }

                            return tw; // arbitrary choice
                        }
                    }
                } else {
                    return countTw; //??
                }
            } else {
                tw = countTw;
            }
        }

        return tw;
    }

    /**
     * Returns the indices/positions of the first character of individual words
     * or units.
     *
     * @return array of start positions of words
     */
    public int[] getStartIndices() {
        if (startIndices == null) {
            startIndices = new int[1];
            startIndices[0] = 0;
        }

        return startIndices;
    }

    /**
     * Returns the position of a unit in the list of units. Can be used when
     * aligning parent&child units
     *
     * @param tw the word/ unit
     *
     * @return the position in the array list
     */
    public int getPositionOfWord(ToolboxWord tw) {
        if ((indexedWords == null) || (tw == null)) {
            return -1;
        }

        return indexedWords.indexOf(tw);
    }

    /**
     * Returns the number of units/words. This can be used to check whether a
     * symbolic association tier has more elements than its parent.
     *
     * @return the number of units
     */
    public int getNumberOfWords() {
        if (startIndices != null) {
            return startIndices.length;
        }

        // or use indexedWords instead?
        return 0;
    }

    /**
     * After all lines have been added call this method to create real and
     * calculated x and width values
     */
    public void createIndices() {
        String[] lines = line.split(blockSep);
        indexedWords = new ArrayList<ToolboxWord>();

        int b = 0;
        int e = 0;
        int cb = 0;
        int numNS = 0;
        boolean inword = false;

        for (int i = 0; i < lines.length; i++) {
            b = 0;
            numNS = 0; // reset

            char[] chars = lines[i].toCharArray();

            for (int j = 0; j < chars.length; j++) {
                if (!isSpace(chars[j])) {
                    if (!inword) {
                        inword = true;
                        b = j;
                        cb = b - numNS;

                        if ((j > 0) && (indexedWords.size() == 0)) {
                            //the line starts with a number of spaces, insert empty word?
                            ToolboxWord tw = new ToolboxWord(marker,
                                    new String(""));
                            tw.realX = (i * BLOCK_INDEX);
                            tw.calcX = tw.realX;
                            tw.calcW = j - 1;
                            tw.realW = tw.calcW;
                        }
                    }

                    if (isNonSpacing(chars[j])) {
                        numNS++;
                    }
                } else { // is space char

                    if (inword) {
                        // store
                        ToolboxWord tw = new ToolboxWord(marker,
                                new String(chars, b, j - b));
                        tw.realX = b + (i * BLOCK_INDEX);
                        tw.calcX = cb + (i * BLOCK_INDEX);

                        if (indexedWords.size() > 0) {
                            ToolboxWord prev = indexedWords.get(indexedWords.size() -
                                    1);
                            prev.realW = tw.realX - prev.realX /*- 1*/;
                            prev.calcW = prev.realW -
                                (prev.realX - prev.calcX);
                        }

                        indexedWords.add(tw);
                        inword = false;
                    }
                }
            }
        }

        createStartIndices();
    }

    /**
     * After all lines have been added call this method to create real and
     * calculated x and width values
     */
    public void createIndices2() {
        String[] lines = line.split(blockSep);
        StringTokenizer tokenizer = null;
        indexedWords = new ArrayList<ToolboxWord>();

        int b = 0;
        int numNS = 0;

        for (int i = 0; i < lines.length; i++) {
            b = 0;
            numNS = 0; // reset
            tokenizer = new StringTokenizer(lines[i]);

            int numToks = tokenizer.countTokens();

            for (int j = 0; j < numToks; j++) {
                String nextToken = tokenizer.nextToken();
                int index = lines[i].indexOf(nextToken, b);
                b = index;

                // store
                ToolboxWord tw = new ToolboxWord(marker, nextToken);
                tw.realX = b + (i * BLOCK_INDEX);
                tw.calcX = tw.realX - numNS;
                //tw.calcX = tw.realX + numNS;//Aug 2009 in some cases this works better? 

                if (indexedWords.size() > 0) {
                    ToolboxWord prev = indexedWords.get(indexedWords.size() -
                            1);
                    prev.realW = tw.realX - prev.realX /*- 1*/;
                    //prev.calcW = prev.realW - (prev.realX - prev.calcX);
                    prev.calcW = tw.calcX - prev.calcX;
                }

                indexedWords.add(tw);

                char[] chars = nextToken.toCharArray();

                for (int k = 0; k < chars.length; k++) {
                     if (correctForMultipleByteChars) {
                        numNS -= getNumExtraBytes(chars[k]);
                    } else if (isNonSpacing(chars[k])) {
                        //System.out.println("ns: " + chars[k]);
                        numNS++;
                    }
                }

                if ((i == (lines.length - 1)) && (j == (numToks - 1))) {
                    tw.realW = nextToken.length();
                    //tw.calcW = tw.realW - numNS;
                    tw.calcW = tw.realW;
                }

                b += nextToken.length(); // position the begin point to the end of the current word
            }
        }

        createStartIndices();
    }

    /**
     * Inserts an empty Word at the given location if and only if this interval
     * refers to an empty segment in an existing Word (i.e. only containing
     * spaces).
     *
     * @param begin begin index
     * @param end end index
     *
     * @return true if insertion was successful, false otherwise
     */
    public boolean conditionallyInsertWord(int begin, int end) {
        ToolboxWord tw = null;

        for (int i = 0; i < indexedWords.size(); i++) {
            tw = indexedWords.get(i);

            if ((tw.calcX < begin) && (end < (tw.calcX + tw.calcW))) {
                if (begin > (tw.calcX + tw.word.length())) {
                    ToolboxWord nextWord = new ToolboxWord(marker, "");
                    nextWord.calcX = begin;
                    nextWord.calcW = end - begin;
                    nextWord.realX = nextWord.calcX;
                    nextWord.realW = nextWord.calcW;

                    if (i < (indexedWords.size() - 1)) {
                        // set calc width to coincide x of next word, no gap
                        ToolboxWord afterWord = indexedWords.get(i + 1);

                        if ((nextWord.calcX + nextWord.calcW) < afterWord.calcX) {
                            nextWord.calcW = afterWord.calcX - begin;
                        }
                    }

                    tw.calcW = begin - tw.calcX;
                    indexedWords.add(i + 1, nextWord);
                    createStartIndices();

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Creates an array of (calculated) start indices or positions of the start
     * of the words/units.
     */
    private void createStartIndices() {
        if ((indexedWords == null) || (indexedWords.size() == 0)) {
            return;
        }

        startIndices = new int[indexedWords.size()];

        ToolboxWord tw = null;

        for (int i = 0; i < indexedWords.size(); i++) {
            tw = indexedWords.get(i);
            startIndices[i] = tw.calcX;
        }
    }

    private boolean isSpace(char c) {
        return ((c == '\t') || (c == '\n') || (c == ' '));
    }

    /**
     * Check whether the character is a non-spacing character. This is needed
     * to overcome the discrepancy  between the length() of a Java string on
     * the one hand and the interlinear alignment based on the  "rendered"
     * number of positions of words. E.g. "Yo(Combining Ring Below)" (u\0059
     * u\006f u\0325) (if that would be a word)  consists of 3 characters
     * (length=3) bu occupies 2 positions in the interlinear alignment.
     *
     * @param c the character
     *
     * @return true if the character is non-spacing
     */
    private boolean isNonSpacing(char c) {
        int type = Character.getType(c);

        return ((type == Character.NON_SPACING_MARK) ||
        (type == Character.ENCLOSING_MARK) ||
        (type == Character.COMBINING_SPACING_MARK));
    }

    /**
     * Correct for the number of bytes used for a character. The alignment is
     * based on the number of bytes.
     *
     * @param c the character
     *
     * @return 1 (extra) for a 2 bytes character, 2 (extra) for a 3 bytes
     *         character
     */
    private int getNumExtraBytes(char c) {
    	//System.out.println("1b: "+ c + "  " +Integer.toHexString(c));
        if ((c == '\u0000') || ((c >= '\u0080') && (c <= '\u07ff'))) { // 2 bytes
                                                                       //System.out.println("2b: "+ c + "  " +Integer.toHexString(c));

            return 1;
        } else if ((c >= '\u0800') && (c <= '\uffff')) { // 3 bytes
                                                         //System.out.println("3b: "+ c + "  " +Integer.toHexString(c));

            return 2;
        }

        return 0;
    }

    /**
     * Temp: returns marker name plus the line
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return marker + ": " + line;
    }
}
