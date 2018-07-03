package mpi.eudico.client.annotator.timeseries.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import mpi.eudico.client.annotator.timeseries.TimeValue;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.TimeFormatter;


/**
 * A class to read and parse information stored in a .csv or tab-delimited text
 * file. It tries to detect the delimiter and the number of columns (and
 * eventually the sample frequency). It can return a sample of a specified
 * number of rows and can extract the data for a track. Lines starting with a
 * "#" are ignored.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class CSVReader implements ClientLogger {
    private final String TAB = "\t";
    private final String SC = ";";
    private final String COMMA = ",";
    private File sourceFile;
    private FileReader fileRead;
    private BufferedReader bufRead;
    private String delimiter = null;
    private int numColumns = 1;
    private boolean trimLinesBeforeParsing = false;

	/* in case of continuous rate files */
    private float msPerSample = 0.0f; // for filling in gaps
    private float deltaT = 1f; // for derivatives, in seconds
    private int sampleFrequency = 0; // samples per second
                                     /* remember the last used time column, to prevent needless frequency detection */
    private int lastTimeCol = -1;

    /**
     * Creates a new CSVReader instance
     *
     * @param file the source file to read from
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public CSVReader(File file) {
        if (file == null) {
            throw new NullPointerException("The file is null");
        }

        sourceFile = file;
        // detect delimiter, check validity
        detectDelimiter();
    }

    /**
     * Creates a new CSVReader instance
     *
     * @param fileName the name (path) of the source file
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public CSVReader(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("The file name is null");
        }

        if (fileName.startsWith("file:")) {
            fileName = fileName.substring(5);
        }

        sourceFile = new File(fileName);
        // detect delimiter, check validity
        detectDelimiter();
    }
    
    public void setDelimiter(String delimiter) {
    	if (delimiter != null) {
    		this.delimiter = delimiter;
    	}
    }

    private void detectDelimiter() {
        if (sourceFile == null) {
            return;
        }

        if (!sourceFile.exists() || sourceFile.isDirectory()) {
            return;
        }

        try {
            fileRead = new FileReader(sourceFile);
            bufRead = new BufferedReader(fileRead);

            int maxNumLines = 10;
            int numLines = 0;
            int numTabs = 0;
            int numCommas = 0;
            int numSemiCol = 0;
            String line = null;
            Pattern pat;

            while (((line = bufRead.readLine()) != null) &&
                    (numLines < maxNumLines)) {
                if (trimLinesBeforeParsing){
                	line = line.trim();
                } 
                if ((line.length() == 0) || line.startsWith("#")) {
                    continue;
                }

                numLines++;               
                pat = Pattern.compile(TAB);
                numTabs += pat.split(line).length - 1;
                pat = Pattern.compile(SC);
                numSemiCol += pat.split(line).length - 1;
                pat = Pattern.compile(COMMA);
                numCommas += pat.split(line).length - 1;
            }

            try {
                bufRead.close();
            } catch (IOException ioe) {
            }

            String del = null;

            if ((numTabs > numSemiCol) && (numTabs > numCommas)) {
                del = TAB;
            } else if ((numSemiCol > numTabs) && (numSemiCol > numCommas)) {
                del = SC;
            } else if ((numCommas > numTabs) && (numCommas > numSemiCol)) {
                del = COMMA;
            }

            delimiter = del;

            if (delimiter == null) {
                LOG.warning("Could not detect the delimiter");
            }
        } catch (IOException ioe) {
            LOG.warning("Cannot detect delimiter: " + ioe.getMessage());
        }
    }

    /**
     * Returns whether the passed file is a valid separated values file.
     *
     * @return true if the file is a valid separated values file, false
     *         otherwise
     */
    public boolean isValidFile() {
        return delimiter != null;
    }

    /**
     * Returns the number of columns (or the number of cells per row.
     *
     * @return Returns the number of columns.
     */
    public int getNumColumns() {
        if (numColumns == 1) {
            try {
                numColumns = detectNumColumns();
            } catch (IOException ioe) {
                LOG.warning("Could not detect the number of columns: " +
                    ioe.getMessage());
            }
        }

        return numColumns;
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
    private int detectNumColumns() throws IOException {
        if (sourceFile == null) {
            throw new IOException("No source file specified");
        }

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        int maxNumLines = 10;
        int numLines = 0;
        int numCols = 0;
        String line = null;
        Pattern pat = Pattern.compile(delimiter);

        while (((line = bufRead.readLine()) != null) &&
                (numLines < maxNumLines)) {
            if (trimLinesBeforeParsing){
            	line = line.trim();
            }   
            if ((line.length() == 0) || line.startsWith("#")) {
                continue;
            }

            numLines++;
            numCols += pat.split(line).length;
        }

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }

        if ((numLines > 0) && (numCols > 0)) {
            numColumns = Math.round(numCols / (float) numLines);

            return numColumns;
        }

        return 1;
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

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        List<String[]> rows = new ArrayList<String[]>();

        String line = null;
        int count = 0;
        String[] nextRow;
        Pattern pat = Pattern.compile(delimiter);

        while (((line = bufRead.readLine()) != null) && (count < rowCount)) {
            if (trimLinesBeforeParsing){
            	line = line.trim();
            }
            if ((line.length() == 0) || line.startsWith("#")) {
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
     * Returns the sample frequency / sample rate that has been extracted from
     * the  source file.
     *
     * @return the sample frequency
     */
    public int getSampleFrequency() {
        return sampleFrequency;
    }

    /**
     * Detects the sample frequency (and delta, msPerSample) from the values in
     * the specified column.
     *
     * @param column the column containing time samples
     *
     * @throws IOException any io exception
     */
    private void detectSampleFrequency(int column) throws IOException {
        if (sourceFile == null) {
            return;
        }

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        Pattern pat = Pattern.compile(delimiter);
        int numSamplesRead = 0;
        long curDuration = 0;
        long time = 0;
        long firstTime = time;
        String li = null;

        while (((li = bufRead.readLine()) != null) && (numSamplesRead < 20)) {
            if (trimLinesBeforeParsing){
            	li = li.trim();
            } 
            if ((li.length() == 0) || li.trim().startsWith("#")) {
                continue;
            }

            time = getTime(li, column, pat);

            if (time > -1) {
                // check if time > curDuration?
                curDuration = time;

                //System.out.println("Index: " + numSamplesRead + " dur: " + curDuration);
            } else {
                // throw exception?? handle in some way
            }

            if (numSamplesRead == 0) {
                firstTime = curDuration;
            }

            numSamplesRead++;
        }

        if (numSamplesRead > 1) {
            float average = (curDuration - firstTime) / (float) (numSamplesRead -
                1);

            sampleFrequency = Math.round(1000 / average);
            msPerSample = 1000 * (1f / sampleFrequency);
            deltaT = msPerSample / 1000;
            LOG.info("Sec Per Sample: " + average + " - Freq. " +
                sampleFrequency);
            LOG.info("Ms per sample: " + msPerSample + " - dT: " + deltaT);
        }

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }
    }

    /**
     * Returns the data value as a float from the specified line and column.
     *
     * @param line the line to parse
     * @param column the index of the data column
     * @param pattern the pattern containing the delimiter
     *
     * @return the data value or -1
     */
    private float getFloat(String line, int column, Pattern pattern) {
        if ((line == null) || (pattern == null) || (column < 0)) {
            return -1f;
        }

        String[] row = pattern.split(line);

        if (row.length > column) {
            return toFloat(row[column]);
        }

        return -1f;
    }

    /**
     * Returns the time value in milliseconds from the specified line and
     * column.
     *
     * @param line one line from the source file
     * @param timeCol the index of the column containing time information
     * @param pattern the pattern containing the delimiter
     *
     * @return a time value in ms. or -1
     */
    private long getTime(String line, int timeCol, Pattern pattern) {
        if ((line == null) || (line.length() == 0)) {
            return -1L;
        }

        long t = -1;
        String[] row = pattern.split(line);

        if (row.length > timeCol) {
            t = TimeFormatter.toMilliSeconds(row[timeCol]);
        }

        return t;
    }

    /**
     * Returns a TimeValue object holding the time in ms. as well as the  value
     * of the data sample as a float.
     *
     * @param line the line to parse
     * @param timeCol the index of the time column
     * @param dataCol the index of the data column
     * @param pattern the pattern containing the delimiter
     *
     * @return a TimeValue object or null
     */
    private TimeValue getTimeValue(String line, int timeCol, int dataCol,
        Pattern pattern) {
        if ((line == null) || (line.length() == 0)) {
            return null;
        }

        long t = 0;
        float v = 0;
        String[] row = pattern.split(line);

        if (row.length > timeCol) {
            t = TimeFormatter.toMilliSeconds(row[timeCol]);
        }

        if (row.length > dataCol) {
            v = toFloat(row[dataCol]);
        }

        return new TimeValue(t, v);
    }

    private float toFloat(String val) {
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException nfe) {
            LOG.warning("Could not parse float: " + val);
        }

        return Float.NaN;
    }

    /**
     * Reads a track from the specified cell while applying derivation
     * calculations.
     *
     * @param timeCol the column containing the time values, zero based
     * @param dataCol column index in the row, zero based
     * @param derLevel the level of derivation (in time), 0 means the raw
     *        values
     * @param continuousRate if true the track is a continuous rate track (must
     *        match the file)
     *
     * @return either float[] (if continuousRate)
     * 		   or List&lt;TimeValue> (if !continuousRate).
     *
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private Object readTrack(int timeCol, int dataCol, int derLevel,
        boolean continuousRate) throws IOException {
        if (!isValidFile()) {
            return null;
        }

        if ((timeCol < 0) || (dataCol < 0)) {
            throw new IllegalArgumentException(
                "Time column and data column must be greater than or equal to 0");
        }

        if (continuousRate && (timeCol != lastTimeCol)) {
            detectSampleFrequency(timeCol);
        }

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        // List of TimeValue and Float objects
        List<Object> values = new ArrayList<Object>();
        Pattern pat = Pattern.compile(delimiter);

        long curTimeStamp = 0L;
        long firstTimeStamp = 0L;
        float curSample = 0.0f;
        int sampleCount = 0;

        //float[] flArray = null;
        TimeValue tv = null;
        String li = null;

        // two dimensional array for iteration; reused for storage 
        // of previous sample
        float[][] derivArray = null;

        if (derLevel > 0) {
            derivArray = new float[derLevel + 1][2];
        }

        while ((li = bufRead.readLine()) != null) {
            if (trimLinesBeforeParsing){
            	li = li.trim();
            } 
            if ((li.length() == 0) || li.trim().startsWith("#")) {
                continue;
            }

            tv = getTimeValue(li, timeCol, dataCol, pat);

            if (tv != null) {
                curTimeStamp = tv.time;
                curSample = tv.value;

                if (!continuousRate) {
                    if (derLevel == 0) {
                        values.add(tv);
                    } else {
                        // before storing calculate derivatives, does this make sense?
                        if (sampleCount == 0) {
                            derivArray[0][0] = curSample;
                            firstTimeStamp = curTimeStamp;
                        } else if (sampleCount == 1) {
                            derivArray[0][1] = curSample;
                            calculateDerivatives(derivArray);
                            // add the first value twice (instead of adding 0 at index 0
                            values.add(new TimeValue(firstTimeStamp,
                                    derivArray[derivArray.length - 1][1]));
                            values.add(new TimeValue(curTimeStamp,
                                    derivArray[derivArray.length - 1][1]));
                        } else {
                            shiftSamplesInArray(derivArray);
                            derivArray[0][1] = curSample;
                            calculateDerivatives(derivArray);
                            values.add(new TimeValue(curTimeStamp,
                                    derivArray[derivArray.length - 1][1]));
                        }
                    }
                } else { // continuous rate
                         // if the first time value != 0 and != msPerSample, fill with zeros?

                    if ((sampleCount == 0) &&
                            (curTimeStamp > (1.5 * msPerSample))) {
                        float fillSample = msPerSample;

                        while (fillSample < (curTimeStamp - msPerSample)) {
                            values.add(new Float(0L));
                            fillSample += msPerSample;
                            sampleCount++;
                        }
                    }

                    // store sample value, but check if samples have to be filled in  
                    // in order to get a proper fixed rate track
                    // note: this only adds 1 sample max. 
                    if ((curTimeStamp - (sampleCount * msPerSample)) > (msPerSample / 2)) {
                        LOG.info("Adding fill-in at sample: " + sampleCount);

                        if (derLevel == 0) {
                            values.add(new Float(curSample));
                        } else {
                            shiftSamplesInArray(derivArray);
                            derivArray[0][1] = curSample;
                            calculateDerivatives(derivArray);
                            values.add(new Float(
                                    derivArray[derivArray.length - 1][1]));
                        }

                        sampleCount++;
                    }

                    if (derLevel == 0) {
                        values.add(new Float(curSample));
                    } else {
                        // before storing calculate derivatives
                        if (sampleCount == 0) {
                            derivArray[0][0] = curSample;
                        } else if (sampleCount == 1) {
                            derivArray[0][1] = curSample;
                            calculateDerivatives(derivArray);
                            // add the first value twice (instead of adding 0 at index 0
                            values.add(new Float(
                                    derivArray[derivArray.length - 1][1]));
                            values.add(new Float(
                                    derivArray[derivArray.length - 1][1]));
                        } else {
                            shiftSamplesInArray(derivArray);
                            derivArray[0][1] = curSample;
                            calculateDerivatives(derivArray);
                            values.add(new Float(
                                    derivArray[derivArray.length - 1][1]));
                        }
                    }
                }
            }

            sampleCount++;
            curSample = 0.0f;
        }

        LOG.info("Derivative: " + derLevel + " Number of samples: " +
            sampleCount + " Last time value: " + tv.time);

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }

        if (continuousRate) {
            float[] result = new float[sampleCount];

            Float fl;

            for (int i = 0; i < sampleCount; i++) {
                fl = (Float) values.get(i);
                result[i] = fl.floatValue();
            }

            return result;
        }

        return values;
    }
    
    /**
     * Reads a track from the specified cell while applying derivation
     * calculations.
     *
     * @param timeCol the column containing the time values, zero based
     * @param dataCol column index in the row, zero based
     * @param derLevel the level of derivation (in time), 0 means the raw
     *        values
     *
     * @return float[].
     *
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public float[] readContinuousRateTrack(int timeCol, int dataCol, int derLevel)
    		throws IOException {
    	return (float[])readTrack(timeCol, dataCol, derLevel, true);
    }

    /**
     * Reads a track from the specified cell while applying derivation
     * calculations.
     *
     * @param timeCol the column containing the time values, zero based
     * @param dataCol column index in the row, zero based
     * @param derLevel the level of derivation (in time), 0 means the raw
     *        values
     *
     * @return List&lt;TimeValue>.
     *
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
	public List<TimeValue> readNonContinuousRateTrack(int timeCol, int dataCol, int derLevel)
    		throws IOException {
    	return (List<TimeValue>) readTrack(timeCol, dataCol, derLevel, false);
    }

    /**
     * The parameter is a multidimensional array of unknown length (minimal 2),
     * containing arrays of length 2. The first array in the arrays  contains
     * the values read from file, the last value will be at  index 1, the
     * previous at index 0. The derivative of these two values will be placed
     * in the next array at index 1, the previous derivative  of that level
     * has been copied to index 0 before. The desired result is at
     * [<code>derivArray</code>.length -1] [1].
     *
     * @param derivArray multidimensional array, [x][2]
     */
    private void calculateDerivatives(float[][] derivArray) {
        for (int i = 0; i < (derivArray.length - 1); i++) {
            derivArray[i + 1][1] = (derivArray[i][1] - derivArray[i][0]) / deltaT;
        }
    }

    /**
     * An unknown length array containing float arrays of length 2. The values
     * at index 0 in these arrays will be given the value of  the values at
     * index 1. Index 1 will then be filled with the new  (calculated) value
     * of the next sample.
     *
     * @param derivArray multidimensional array, [x][2]
     */
    private void shiftSamplesInArray(float[][] derivArray) {
        for (int i = 0; i < derivArray.length; i++) {
            derivArray[i][0] = derivArray[i][1];
        }
    }

    /**
     * False by default.
	 * @return true if the input lines need to be trimmed before splitting
	 * on the basis of the delimiter, false otherwise. 
	 */
	public boolean isTrimLinesBeforeParsing() {
		return trimLinesBeforeParsing;
	}

	/**
	 * @param trimLinesBeforeParsing the trimLinesBeforeParsing flag to set
	 */
	public void setTrimLinesBeforeParsing(boolean trimLinesBeforeParsing) {
		this.trimLinesBeforeParsing = trimLinesBeforeParsing;
	}

}
