package mpi.eudico.client.annotator.timeseries.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.TimeFormatter;


/**
 * A timeseries file reader for a continuous rate, time - value pairs text file.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class BasicTSFileReader implements ClientLogger {
    private File sourceFile;
    private boolean validFile = true;
    private FileReader fileRead; // maybe change to InputStreamReader
    private BufferedReader bufRead;
    private StringTokenizer tokenizer;
    private float msPerSample = 0.0f;

    // assumes an integer as the sample frequency
    private int sampleFrequency = 0;
    private float min = Float.MAX_VALUE;
    private float max = Integer.MIN_VALUE;// Float.MIN_VALUE doesn't compare well: data[i] > max always returns false

    /**
     * Creates a new BasicTSFileReader instance
     *
     * @param fileName the path to the source
     */
    public BasicTSFileReader(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("The file name is null");
        }

        if (fileName.startsWith("file:")) {
            fileName = fileName.substring(5);
        }

        sourceFile = new File(fileName);

        validFile = isValidTSFile();
    }

    /**
     * Reads 20 non-empty, non-comment lines to check if all each line contains
     * 2 tokens (time and value).
     *
     * @return true if the file seems to be of the right format
     */
    private boolean isValidTSFile() {
        if (sourceFile == null) {
            return false;
        }

        boolean valid = true;

        try {
            fileRead = new FileReader(sourceFile);
            bufRead = new BufferedReader(fileRead);

            int rowIndex = 0;
            String li = null;

            while (((li = bufRead.readLine()) != null) && (rowIndex < 20)) {
                if (li.length() == 0) {
                    continue; //empty line
                } else {
                    if (li.charAt(0) == '#') {
                        continue; //comment line
                    }

                    tokenizer = new StringTokenizer(li);

                    if (tokenizer.countTokens() != 2) {
                        valid = false;

                        break;
                    }

                    rowIndex++;
                }
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
     * Returns whether the passed file is a valid dataglove log file.
     *
     * @return true if the file is a valid dataglove log file, false otherwise
     */
    public boolean isValidFile() {
        return validFile;
    }

    /**
     * Read a number of samples and calculate the frequency and ms per sample.
     *
     * @return the sample frequency
     *
     * @throws IOException
     */
    public int detectSampleFrequency() throws IOException {
        if (!validFile) {
            return 0;
        }

        sampleFrequency = 0;

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        int numSamplesRead = 0;

        //float curDuration = 0.0f;
        long curTime = 0L;
        String li = null;

        while (((li = bufRead.readLine()) != null) && (numSamplesRead < 20)) {
            if (li.length() == 0) {
                continue;
            } else {
                if (li.charAt(0) == '#') {
                    continue;
                }

                tokenizer = new StringTokenizer(li);

                if (tokenizer.countTokens() == 2) {
                    //curDuration = toFloat(tokenizer.nextToken());
                    curTime = TimeFormatter.toMilliSeconds(tokenizer.nextToken());
                } else {
                    continue;
                }
            }

            numSamplesRead++;
        }

        if (numSamplesRead > 1) {
            // float average = curDuration / (numSamplesRead - 1);
            // sampleFrequency = (int)Math.round(1 / average);
            // msPerSample = 1000 * (1f / sampleFrequency);
            float average = curTime / (numSamplesRead - 1);
            sampleFrequency = Math.round(1000 / average);
            msPerSample = 1000f / sampleFrequency;
            LOG.info("Sec Per Sample: " + average + " - Freq. " +
                sampleFrequency);
            LOG.info("Ms per sample: " + msPerSample);
        }

        try {
            bufRead.close();
        } catch (IOException ioe) {
        }

        return sampleFrequency;
    }

    /**
     * Reads the one track in the file.
     *
     * @return a float array of sample values
     *
     * @throws IOException
     */
    public float[] readTrack() throws IOException {
        if (!validFile) {
            return null;
        }

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        List<Float> values = new ArrayList<Float>();

        //float curTimeStamp = 0.0f;
        long curTimeStamp = 0L;
        float curSample = 0.0f;
        int sampleCount = 0;
        String li = null;

        while ((li = bufRead.readLine()) != null) {
            if ((li.length() == 0) || (li.charAt(0) == '#')) {
                continue; //skip empty lines and comment lines
            } else {
                tokenizer = new StringTokenizer(li);

                if (tokenizer.countTokens() >= 2) {
                    //curTimeStamp = toFloat(tokenizer.nextToken());
                    curTimeStamp = TimeFormatter.toMilliSeconds(tokenizer.nextToken());

                    if ((curTimeStamp - (sampleCount * msPerSample)) > (msPerSample / 2)) {
                        LOG.info("Adding fill-in at sample: " + sampleCount);
                        values.add(new Float(curSample));
                        sampleCount++;
                    }

                    curSample = toFloat(tokenizer.nextToken());

                    if (curSample < min) {
                        min = curSample;
                    }

                    if (curSample > max) {
                        max = curSample;
                    }

                    values.add(new Float(curSample));
                    sampleCount++;
                }
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
     * Return the read minimmum value;
     *
     * @return the minimum
     */
    public float getMin() {
        return min;
    }

    /**
     * Return the read maximum value.
     *
     * @return the maximum
     */
    public float getMax() {
        return max;
    }

    /**
     * Converts a String to a float, with some error checks.
     *
     * @param value the string value
     *
     * @return the float value
     */
    private float toFloat(String value) {
        if ((value == null) || (value.length() == 0)) {
            LOG.severe("No float value: " + value);

            return 0f;
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException nfe) {
            LOG.severe("No float value: " + value);

            return Float.NaN;
        }
    }
}
