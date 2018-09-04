package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import mpi.eudico.server.corpora.util.ServerLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Reads and tokenizes rows in a comma separated values or tab-delimited text
 * file.
 *
 * @author Han Sloetjes
 * @version 1.0
 * @version 1.1 use utf-8 as the default encoding for the tab files
 */
public class DelimitedTextReader implements ServerLogger {
    private final String TAB = "\t";
    private final String SC = ";";
    private final String COLON = ":";
    private final String COMMA = ",";
    private final String PIPE = "\\|";// escaped pipe character \u007c
    private File sourceFile;
    private String delimiter = TAB;
    private int numColumns = 1;
    private int firstRowIndex = 0;

    /**
     * Creates a new DelimitedTextReader instance
     *
     * @param sourceFile the file to read
     *
     * @throws FileNotFoundException if the file is not found
     */
    public DelimitedTextReader(File sourceFile) throws FileNotFoundException {
        this.sourceFile = sourceFile;

        if (!sourceFile.exists()) {
            throw new FileNotFoundException("Input file does not exist");
        }

        try {
            delimiter = detectDelimiter();
        } catch (IOException ioe) {
            LOG.warning("Unable to detect the delimiter");
        }

        try {
            numColumns = detectNumColumns();
        } catch (IOException ioe) {
            LOG.warning("Unable to detect the delimiter");
        }
    }

    /**
     * Creates a new DelimitedTextReader instance
     *
     * @param fileName the name/path of the file to read
     *
     * @throws FileNotFoundException if the file is not found
     * @throws NullPointerException if the fileName is null
     */
    public DelimitedTextReader(String fileName) throws FileNotFoundException {
        if (fileName == null) {
            throw new NullPointerException("The file name is null");
        }

        if (fileName.startsWith("file:")) {
            fileName = fileName.substring(5);
        }

        sourceFile = new File(fileName);

        if (!sourceFile.exists()) {
            throw new FileNotFoundException("Input file does not exist");
        }

        try {
            delimiter = detectDelimiter();
        } catch (IOException ioe) {
            LOG.warning("Unable to detect the delimiter");
        }

        try {
            numColumns = detectNumColumns();
        } catch (IOException ioe) {
            LOG.warning("Unable to detect the number of columns");
        }
    }

    /**
     * Performs a naive test to discover the delimiter in the file.
     *
     * @return the delimiter
     *
     * @throws IOException io exception
     */
    private String detectDelimiterPr() throws IOException {
        if (sourceFile == null) {
            throw new IOException("No source file specified");
        }

        InputStreamReader fileReader = new InputStreamReader(new FileInputStream(
                    sourceFile), "UTF-8");

        //FileReader fileReader = new FileReader(sourceFile);
        BufferedReader bufRead = new BufferedReader(fileReader);
        int maxNumLines = 10;
        int numLines = 0;
        
        String[] valArray = new String[]{TAB, SC, COLON, COMMA, PIPE};
        int[] counts = new int[valArray.length];
        
        String line = null;
        Pattern pat = null;

        while (((line = bufRead.readLine()) != null) &&
                (numLines < maxNumLines)) {
            if (line.length() == 0 /*|| line.startsWith("#")*/) {
                continue;
            }

            numLines++;
            pat = Pattern.compile(TAB);
            counts[0] += pat.split(line).length - 1;
            
            pat = Pattern.compile(SC);
            counts[1] += pat.split(line).length - 1;
            
            pat = Pattern.compile(COLON);
            counts[2] += pat.split(line).length - 1;
            
            pat = Pattern.compile(COMMA);
            counts[3] += pat.split(line).length - 1;
            
            pat = Pattern.compile(PIPE);
            counts[4] += pat.split(line).length - 1;
        }

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }

        String del = null;
        int maxCount = 0;
        int maxIndex = 0;
        
        for (int i = 0; i < counts.length; i++) {
        	if (counts[i] > maxCount) {
        		maxCount = counts[i];
        		maxIndex = i;
        	}
        }

        del = valArray[maxIndex];

        if (del == null) {
            del = TAB;
        }

        delimiter = del;

        return del;
    }
    
    /**
     * Calls the private implementation and strips the escape character of the 
     * delimiter in case it is the vertical bar or pipe character.
     * 
     * @return the detected delimiter, TAB by default
     * @throws IOException any io exception
     */
    public String detectDelimiter() throws IOException {
    	String del = detectDelimiterPr();
    	if (PIPE.equals(del)) {
    		return "|";
    	}
    	
    	return del;
    }

    /**
     * Tries to detect the number of columns in the delimited file, by reading
     * a number of rows. Call detectDelimiter first, the default delimiter is
     * a tab.
     *
     * @return the number of columns
     *
     * @throws IOException io exception
     */
    public int detectNumColumns() throws IOException {
        if (sourceFile == null) {
            throw new IOException("No source file specified");
        }
        InputStreamReader fileReader = new InputStreamReader(new FileInputStream(
                sourceFile), "UTF-8");
        //FileReader fileReader = new FileReader(sourceFile);
        BufferedReader bufRead = new BufferedReader(fileReader);
        int maxNumLines = 10;
        int numLines = 0;
        int numCols = 0;
        String line = null;
        Pattern pat = Pattern.compile(delimiter);
        String[] nextRow;

        while (((line = bufRead.readLine()) != null) &&
                (numLines < maxNumLines)) {
            if (line.length() == 0 /*|| line.startsWith("#")*/) {
                continue;
            }

            numLines++;
            // HS 10-2012 : specifying a negative limit ensures that empty strings are in the array
            nextRow = pat.split(line, -1);
            numCols += nextRow.length;
        }

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }

        if ((numLines > 0) && (numCols > 0)) {
            numColumns = (int) Math.round(numCols / (float) numLines);

            return numColumns;
        }

        return 1;
    }

    /**
     * Returns the delimiter.
     *
     * @return the delimiter
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the delimiter. Trusts the caller.
     *
     * @param delimiter the new delimiter
     */
    public void setDelimiter(String delimiter) {
        if (delimiter != null) {
            this.delimiter = delimiter;
            if (delimiter.equals("|")) {
            	this.delimiter = PIPE;
            }
        }
    }

    /**
     * Returns the number of columns.
     *
     * @return the number of columns
     */
    public int getNumColumns() {
        return numColumns;
    }

    /**
     * Sets the number of columns. Trusts the caller.
     *
     * @param numColumns the number of columns
     */
    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    /**
     * Returns a List of String array objects. The arrays only contain the
     * values of the columns  specified by the columns parameter. Zero based.
     *
     * @param columns the column indices
     *
     * @return a List of String array objects
     *
     * @throws IOException any io exception
     */
    public List<String[]> getRowDataForColumns(int[] columns) throws IOException {
        return getRowDataForColumns(firstRowIndex, columns);
    }

    /**
     * Returns a List of String array objects. The arrays only contain the
     * values of the columns  specified by the columns parameter. Zero based.
     *
     * @param firstRow the first row to include in the result
     * @param columns the column indices
     *
     * @return a List of String array objects
     *
     * @throws IOException io exception e.g. when no valid source file has been
     *         specified
     */
    public List<String[]> getRowDataForColumns(int firstRow, int[] columns)
        throws IOException {
        if ((sourceFile == null) || (columns == null)) {
            throw new IOException("No source file specified");
        }

        InputStreamReader fileReader = new InputStreamReader(new FileInputStream(
                sourceFile), "UTF-8");
        //FileReader fileReader = new FileReader(sourceFile);
        BufferedReader bufRead = new BufferedReader(fileReader);
        List<String[]> rows = new ArrayList<String[]>();

        String line = null;
        int rowCount = 0;
        String[] nextRow;

        //StringTokenizer tokenizer;
        Pattern pat = Pattern.compile(delimiter);

        while ((line = bufRead.readLine()) != null) {
            if ((line.length() == 0 /*|| line.startsWith("#")*/) ||
                    (rowCount < firstRow)) {
                rowCount++;

                continue;
            }

            nextRow = pat.split(line);

            String[] row = new String[columns.length];

            for (int i = 0; i < columns.length; i++) {
                if (columns[i] < nextRow.length) {
                    row[i] = nextRow[columns[i]];
                } else {
                    row[i] = "";
                }
            }

            rows.add(row);
            rowCount++;
        }

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }

        return rows;
    }

    /**
     * Returns a List of String array objects.
     *
     * @param rowCount the number of lines to read and split
     *
     * @return a List of String array objects
     *
     * @throws IOException io exception e.g. when no valid source file has been
     *         specified
     */
    public List<String[]> getSamples(int rowCount) throws IOException {
        if (sourceFile == null) {
            throw new IOException("No source file specified");
        }

        InputStreamReader fileReader = new InputStreamReader(new FileInputStream(
                sourceFile), "UTF-8");
        //FileReader fileReader = new FileReader(sourceFile);
        BufferedReader bufRead = new BufferedReader(fileReader);
        List<String[]> rows = new ArrayList<String[]>();

        String line = null;
        int count = 0;
        String[] nextRow;
        Pattern pat = Pattern.compile(delimiter);

        while (((line = bufRead.readLine()) != null) && (count < rowCount)) {
            if (line.length() == 0) {
                continue;
            }

            nextRow = pat.split(line);
            rows.add(nextRow);
            count++;
        }

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }

        return rows;
    }

    /**
     * Returns the line number of the first row of data, so after any header
     * lines Zero based.
     *
     * @return the line number of the first row of data
     */
    public int getFirstRowIndex() {
        return firstRowIndex;
    }

    /**
     * Sets the line number of the first row of data after any header lines.
     * Believes the caller. Zero based.
     *
     * @param firstRow the first row index
     */
    public void setFirstRowIndex(int firstRow) {
        this.firstRowIndex = firstRow;
    }
}
