package mpi.eudico.client.annotator.timeseries.glove;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import mpi.eudico.client.annotator.util.ClientLogger;


/**
 * Reader for the MPI CyberGlove file format (.log). Each sample consists of
 * either  15 or 35 rows. Each row has a fixed number of up to 6 values
 * (columns). Samples are separated by a white line. Comment lines start with
 * "#". Each sample has a time value and it is assumed that it is a continuous
 * rate file.
 *
 * @author Han Sloetjes
 */
public class DataGloveFileReader implements DataGloveConstants, ClientLogger {
    private boolean validLogFile = true;
    private File sourceFile;
    private FileReader fileRead; // maybe change to InputStreamReader
    private BufferedReader bufRead;
    private StringTokenizer tokenizer;
    private float msPerSample = 0.0f;
    private float deltaT = 1f;
    private int sampleFrequency = 0;
    private int numRowsPerSample = 0;

    /**
     * Creates a new DataGloveFileReader instance
     *
     * @param file the time series source file
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public DataGloveFileReader(File file) {
        if (file == null) {
            throw new NullPointerException("The file is null");
        }

        sourceFile = file;
        validLogFile = isValidDataFile();
        System.out.println("Valid log file: " + validLogFile);
    }

    /**
     * Creates a new DataGloveFileReader instance
     *
     * @param fileName the path to the timeseries source file
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public DataGloveFileReader(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("The file name is null");
        }

        if (fileName.startsWith("file:")) {
            fileName = fileName.substring(5);
        }

        sourceFile = new File(fileName);
        validLogFile = isValidDataFile();
        LOG.info("Valid Data Glove log file: " + validLogFile);
    }

    /* not yet implemented
       public DataGloveFileReader(URL url) {
       }
     */
    /**
     * Reads a sample and checks its format.
     *
     * @return true if it seems to be a cyber glove log file
     */
    private boolean isValidDataFile() {
        if (sourceFile == null) {
            return false;
        }

        boolean valid = true;

        try {
            fileRead = new FileReader(sourceFile);
            bufRead = new BufferedReader(fileRead);

            int rowIndex = 0;
            String li = null;

            while (((li = bufRead.readLine()) != null) && (rowIndex < 40)) {
                if (li.length() == 0) {
                    // end of sample
                    break;
                } else {
                    if (li.charAt(0) == '#') {
                        continue;
                    }

                    tokenizer = new StringTokenizer(li);

                    if ((rowIndex < COLS_PER_ROW.length) &&
                            (tokenizer.countTokens() < COLS_PER_ROW[rowIndex])) {
                        valid = false;

                        break;
                    }

                    rowIndex++;
                }
            }

            numRowsPerSample = rowIndex;

            if (rowIndex < (COLS_PER_ROW.length - 1)) {
                valid = false; // too few rows
            }

            try {
                bufRead.close();
            } catch (IOException ie) {
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();

            return false;
        }

        return valid;
    }

    /**
     * Test read.
     *
     * @throws IOException io exception
     */
    public void read() throws IOException {
        sampleFrequency = detectSampleFrequency();
        msPerSample = 1000 * (1f / sampleFrequency);
        LOG.info("Ms per sample detected: " + msPerSample);

        //readTrack(10, 5);
        /*
           fileRead = new FileReader(file);
           //System.out.println("Encoding: " + fr.getEncoding());
           // the log is encoded in Cp1252 encoding
         */
    }

    /**
     * Main method for testing.
     *
     * @param args args
     */
    public static void main(String[] args) {
        String fileName = "D:\\MPI\\ELAN docs\\dataglove\\glove.log";
        DataGloveFileReader reader = new DataGloveFileReader(fileName);

        try {
            reader.read();
        } catch (IOException ioe) {
            System.out.println("Cannot read file " + fileName + " " +
                ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    /**
     * Converts the tokens in the string to a float array.
     *
     * @param line the line
     *
     * @return the float array or null
     */
    private float[] toFloatArray(String line) {
        if ((line == null) || (line.length() == 0)) {
            return null;
        }

        tokenizer = new StringTokenizer(line);

        float[] result = new float[tokenizer.countTokens()];
        String token = null;
        int i = 0;

        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();

            try {
                result[i] = Float.parseFloat(token);
            } catch (NumberFormatException nfe) {
                LOG.severe("No float value: " + token);
                result[i] = Float.NaN;
            }

            i++;
        }

        return result;
    }

    /**
     * Reads 10 samples and calculates the sample frequency. The frequency  is
     * not stored in the file itself.
     *
     * @return the sample frequency
     *
     * @throws IOException any io exception
     */
    public int detectSampleFrequency() throws IOException {
        if (!validLogFile) {
            return 0;
        }

        sampleFrequency = 0;

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        int numSamplesRead = 0;
        int indexInSample = 0;
        float curDuration = 0.0f;
        float[] flArray = null;
        String li = null;

        while (((li = bufRead.readLine()) != null) && (numSamplesRead < 10)) {
            if (li.length() == 0) {
                numSamplesRead++;

                if (numSamplesRead == 1) {
                    numRowsPerSample = indexInSample;
                    LOG.info("Number of  rows per sample: " + numRowsPerSample);
                }

                indexInSample = 0;
            } else {
                if (li.charAt(0) == '#') {
                    continue;
                }

                if (indexInSample == 0) {
                    flArray = toFloatArray(li);

                    if (flArray.length >= 2) { //should be of length 3
                        curDuration = flArray[1];

                        //System.out.println("Index: " + numSamplesRead + " dur: " + curDuration);
                    }
                }

                indexInSample++;
            }
        }

        if (numSamplesRead > 1) {
            float average = curDuration / (numSamplesRead - 1);

            sampleFrequency = (int) Math.round(1 / average);
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

        return sampleFrequency;
    }

    /**
     * Reads a track from the specified 'cell', given by rwo and column
     *
     * @param row row index in sample, zero based
     * @param column column index in the row, zero based
     *
     * @return a float array
     *
     * @throws IOException
     * @throws IllegalArgumentException if the specified cell does not exist
     */
    public Object readTrack(int row, int column) throws IOException {
        if (!validLogFile) {
            return null;
        }

        if ((row < 0) || (column < 0)) {
            throw new IllegalArgumentException(
                "Row and column must be greater than or equal to 0");
        }

        if (row >= numRowsPerSample) {
            throw new IllegalArgumentException("Row " + row +
                " does not exist. There are " + numRowsPerSample +
                " rows per sample in the file.");
        }

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        List<Float> values = new ArrayList<Float>();
        int indexInSample = 0;
        float curTimeStamp = 0.0f;
        float curSample = 0.0f;
        int sampleCount = 0;
        float[] flArray = null;
        String li = null;

        while ((li = bufRead.readLine()) != null) {
            if (li.length() == 0) {
                // store sample value, but check if samples have to be filled in  
                // in order to get a proper fixed rate track
                if ((curTimeStamp - (sampleCount * msPerSample)) > (msPerSample / 2)) {
                    LOG.info("Adding fill-in at sample: " + sampleCount);
                    values.add(new Float(curSample));
                    sampleCount++;
                }

                values.add(new Float(curSample));
                // begin of new sample reset...
                indexInSample = 0;
                sampleCount++;
                curSample = 0.0f;
            } else {
                if (li.charAt(0) == '#') {
                    continue;
                }

                if (indexInSample == 0) {
                    flArray = toFloatArray(li);

                    if (flArray.length >= 2) { //should be of length 3
                        curTimeStamp = flArray[1];
                    }
                    if (row == 0) {
                        curSample = flArray[column];
                    }
                } else if (indexInSample == row) {
                    flArray = toFloatArray(li);

                    if (flArray.length > column) {
                        curSample = flArray[column];
                    }
                }

                indexInSample++;
            }
        }

        LOG.info("Number of samples: " + sampleCount);

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }

        float[] result = new float[sampleCount];

        Float fl;

        for (int i = 0; i < sampleCount; i++) {
            fl = values.get(i);
            result[i] = fl.floatValue();

            //if (i < 20) {
            //	System.out.println("i: " + i + " v: " + fl.floatValue());
            //}
        }

        return result;
    }

    /**
     * Reads a track from the specified cell while applying derivation
     * calculations.
     *
     * @param row row index in sample, zero based
     * @param column column index in the row, zero based
     * @param derivative the level of derivation (in time), 0 means the raw
     *        values
     *
     * @return a float array
     *
     * @throws IOException
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public Object readTrack(int row, int column, int derivative)
        throws IOException {
        if (!validLogFile) {
            return null;
        }

        if (derivative == 0) {
            return readTrack(row, column);
        }

        if ((row < 0) || (column < 0)) {
            throw new IllegalArgumentException(
                "Row and column must be greater than or equal to 0");
        }

        if (row >= numRowsPerSample) {
            throw new IllegalArgumentException("Row " + row +
                " does not exist. There are " + numRowsPerSample +
                " rows per sample in the file.");
        }

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        List<Float> values = new ArrayList<Float>();
        int indexInSample = 0;
        float curTimeStamp = 0.0f;
        float curSample = 0.0f;
        int sampleCount = 0;
        float[] flArray = null;
        String li = null;

        // two dimensional array for iteration; reused for storage 
        // of previous sample
        float[][] derivArray = new float[derivative + 1][2];

        while ((li = bufRead.readLine()) != null) {
            if (li.length() == 0) {
                // store sample value, but check if samples have to be filled in  
                // in order to get a proper fixed rate track
                if ((curTimeStamp - (sampleCount * msPerSample)) > (msPerSample / 2)) {
                    LOG.info("Adding fill-in at sample: " + sampleCount);
                    shiftSamplesInArray(derivArray);
                    derivArray[0][1] = curSample;
                    calculateDerivatives(derivArray);
                    values.add(new Float(derivArray[derivArray.length - 1][1]));
                    sampleCount++;
                }

                // before storing calculate derivatives
                if (sampleCount == 0) {
                    derivArray[0][0] = curSample;
                } else if (sampleCount == 1) {
                    derivArray[0][1] = curSample;
                    calculateDerivatives(derivArray);
                    // add the first value twice (instead of adding 0 at index 0
                    values.add(new Float(derivArray[derivArray.length - 1][1]));
                    values.add(new Float(derivArray[derivArray.length - 1][1]));
                } else {
                    shiftSamplesInArray(derivArray);
                    derivArray[0][1] = curSample;
                    calculateDerivatives(derivArray);
                    values.add(new Float(derivArray[derivArray.length - 1][1]));
                }

                // begin of new sample reset...
                indexInSample = 0;
                sampleCount++;
                curSample = 0.0f;
            } else {
                if (li.charAt(0) == '#') {
                    continue;
                }

                if (indexInSample == 0) {
                    flArray = toFloatArray(li);

                    if (flArray.length >= 2) { //should be of length 3
                        curTimeStamp = flArray[1];
                    }
                } else if (indexInSample == row) {
                    flArray = toFloatArray(li);

                    if (flArray.length > column) {
                        curSample = flArray[column];
                    }
                }

                indexInSample++;
            }
        }

        LOG.info("Derivative: " + derivative + " Number of samples: " +
            sampleCount);

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }

        float[] result = new float[sampleCount];

        Float fl;

        for (int i = 0; i < sampleCount; i++) {
            fl = values.get(i);
            result[i] = fl.floatValue();

            //if (i < 20) {
            //	System.out.println("i: " + i + " v: " + fl.floatValue());
            //}
        }

        return result;
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
     * Returns the milliseconds per sample value that has been extracted from
     * the  source file Returns whether the value at the specified row and
     * column represents an angle in degrees. If true values between 0 and
     * -180 can be converted to a value between 180 and 360.
     *
     * @param row the row
     * @param column the column
     *
     * @return true if the cell represent an angle in degrees, false otherwise.
     */
    private boolean shouldConvertAngle(int row, int column) {
        switch (row) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
            return true;

        case 8:
            return ((column == 0) || (column == 1));

        case 10:
        case 12:
        case 14:
            return ((column >= 3) && (column < 5));

        default:
            return false;
        }
    }

    /**
     * Returns the milliseconds per sample value that has been extracted from
     * the  source file
     *
     * @return the milliseconds per sample value
     */
    public float getMsPerSample() {
        return msPerSample;
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
     * Returns whether the passed file is a valid dataglove log file.
     *
     * @return true if the file is a valid dataglove log file, false otherwise
     */
    public boolean isValidFile() {
        return validLogFile;
    }
}
