package mpi.eudico.client.annotator.timeseries.praat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.timeseries.TimeValue;
import mpi.eudico.client.annotator.timeseries.TimeValueStart;


/**
 * A class to extract timeseries data from a Praat .PitchTier file. The
 * expected format is roughly like shown below, but the format is only loosely
 * checked. <br>
 * 1 File type = "ooTextFile"<br>
 * 2 Object class = "PitchTier"<br>
 * 3 <br>
 * 4 xmin = 0<br>
 * 5 xmax = 36.59755102040816<br>
 * 6 points: size = 68891 <br>
 * 7 points [1]:<br>
 * 8   time = 0.020010416666655147<br>
 * 9   value = 206.31944559216197 <br>
 * 10 points [2]:<br>
 * 11   time = 0.030010416666655146<br>
 * 12   value = 202.97421414792237<br>
 * <br>
 * short version<br>
 * 1 File type = "ooTextFile"<br>
 * 2 Object class = "PitchTier"<br>
 * 3 <br>
 * 4 0 <br>
 * 5 36.59755102040816<br>
 * 6 68891 <br>
 * 8 0.020010416666655147<br>
 * 9 206.31944559216197<br>
 * 
 * This reader is also used for IntensityTiers 
 * 
 * March 2009: changed the assumption of continuous rate into non-continuous
 */
public class PitchTierFileReader {
    private boolean validFile;
    private String filePath;
    private File sourceFile;
    private FileReader fileRead; // maybe change to InputStreamReader
    private BufferedReader bufRead;

    //private StringTokenizer tokenizer;
    private float msPerSample = 0.0f;
    private int sampleFrequency = 0;
    //private boolean continuous = false;
    private boolean shortNotation = false;
    private int size;
    private double minT;
    private double maxT;
    private float minVal = Float.MAX_VALUE;
    private float maxVal = Float.MIN_VALUE;
    //private boolean startsAtZero = false;
    private final String TIME = "time";
    private final String NUMBER = "number";
    private final String VAL = "value";
    private final char EQ = '=';
    private boolean isPitch = true;
    private boolean isIntensity = false;
    
    /**
     * Creates a new PitchTier File Reader
     *
     * @param fileName
     */
    public PitchTierFileReader(String fileName) {
        super();
        this.filePath = fileName;

        if (filePath.startsWith("file:")) {
            filePath = filePath.substring(5);
        }

        sourceFile = new File(filePath);

        isValidPTFile();
    }
    
    public String getTrackName() {
    	if (isIntensity) {
    		return "IntensityTier";
    	} 
    	
    	return "PitchTier";
    }

    /**
     * Only checks for some standard id lines and tries to detect short notation.
     */
    private void isValidPTFile() {
        if (sourceFile == null) {
            validFile = false;

            return;
        }

        try {
            fileRead = new FileReader(sourceFile);
            bufRead = new BufferedReader(fileRead);

            boolean textFile = false;
            boolean pitchFile = false;

            int rowIndex = 0;
            String li = null;
            long firstTimeStamp = -1L;

            double d = 0.0;
            List<Long> times = new ArrayList<Long>(11);

            while (((li = bufRead.readLine()) != null) && (rowIndex < 36)) { // 10 * 3 + 6

                if (li.length() == 0) { //third line is empty, index 2
                    rowIndex++;

                    continue;
                } else {
                    if (li.contains("ooTextFile")) {
                        textFile = true;
                    } else if (li.contains("PitchTier")) {
                        pitchFile = true;
                        validFile = true;
                        isPitch = true;
                        isIntensity = false;
                    } else if (li.contains("IntensityTier")) {// use the same for Intensity tiers
                        pitchFile = true;
                        validFile = true;
                        isPitch = false;
                        isIntensity = true;
                    } else if ((rowIndex == 3) || li.contains("xmin")) {
                        try {
                            if (li.contains("=")) {
                                minT = Double.parseDouble(li.substring(li.indexOf(
                                                EQ) + 1).trim()) * 1000;
                            } else {
                                minT = Double.parseDouble(li.trim()) * 1000;
                                shortNotation = true;
                            }
                        } catch (NumberFormatException nfe) {
                            minT = 0.0;
                        }

                        //if (minT == 0.0) {
                        //    startsAtZero = true;
                        //}
                    } else if ((rowIndex == 4) || li.contains("xmax")) {
                        try {
                            if (li.contains("=")) {
                                maxT = Double.parseDouble(li.substring(li.indexOf(
                                                EQ) + 1).trim()) * 1000;
                            } else {
                                maxT = Double.parseDouble(li.trim()) * 1000;
                                shortNotation = true;
                            }
                        } catch (NumberFormatException nfe) {
                            maxT = 1000.0; //error
                        }
                    } else if ((rowIndex == 5) || li.contains("size")) {
                        try {
                            if (li.contains("=")) {
                                size = Integer.parseInt(li.substring(li.indexOf(
                                                EQ) + 1).trim());
                            } else {
                                size = Integer.parseInt(li.trim());
                            }
                        } catch (NumberFormatException nfe) {
                            maxT = 1000; //error
                        }
                    } else if ((rowIndex > 5) && pitchFile) {
                        try {
                            if (li.contains("=") && ((rowIndex % 3) == 1)) {
                                d = Double.parseDouble(li.substring(li.indexOf(
                                                EQ) + 1).trim());
                                times.add((long) (d * 1000));
                                if (firstTimeStamp < 0) {
                                	firstTimeStamp = (long) (d * 1000);
                                }
                            } else if (shortNotation && ((rowIndex % 2) == 0)) {
                                d = Double.parseDouble(li.trim());
                                times.add((long) (d * 1000));
                                if (firstTimeStamp < 0) {
                                	firstTimeStamp = (long) (d * 1000);
                                }
                            }
                        } catch (NumberFormatException nfe) {
                            break; //error
                        }

                        
                    }

                    rowIndex++;
                }
            }

            // calculate sample frequency
            // not really relevant for non continuous rate tracks, used to detect segments/gaps
            if (times.size() > 2) {
                long bt = times.get(0);
                long et = times.get(times.size() - 1);
                long dur = et - bt;
                msPerSample = ((float) dur / (times.size() - 1));
                sampleFrequency = Math.round(1000f / msPerSample);
            } else {
                validFile = false;
            }
              /*
            if (((long) minT) != firstTimeStamp) {
            	ServerLogger.LOG.warning("First time value not equal to xmin: " + firstTimeStamp + " - " + minT);
            	minT = firstTimeStamp;
            }
            */
            try {
                bufRead.close();
            } catch (IOException ie) {
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Returns whether the provided file is recognized as a valid PithTier file. 
     *
     * @return true if the file is a PitchTier file
     */
    public boolean isValidFile() {
        return validFile;
    }

    /**
     * Reads the file and returns an array of TimeValue objects. It is assumed that it is
     * a non-continuous rate track.
     * 
     * @return an List of TimeValue objects
     *
     * @throws IOException 
     */
    public List<TimeValue> readTrack() throws IOException {
        if (!validFile) {
            return null;
        }

        fileRead = new FileReader(sourceFile);
        bufRead = new BufferedReader(fileRead);

        List<TimeValue> values = new ArrayList<TimeValue>(size);
        int rowIndex = 0;

        long curTimeStamp = 0L;
        long prevTimeStamp = -1L;
        //float curSample = 0.0f;
        float nextSample = 0.0f;

        //int sampleCount = 0;
        String li = null;
        int eqIndex = -1;

        while (((li = bufRead.readLine()) != null)) {
            if ((li.length() == 0) || (rowIndex < 6)) { //third line is empty, index 2
                rowIndex++;

                continue;
            } else {
                if (shortNotation) {
                    if ((rowIndex % 2) == 0) {
                        try {
                        	curTimeStamp = (long) (1000 * Double.parseDouble(li.trim()));
                        } catch (NumberFormatException nfe) {
                        	curTimeStamp = curTimeStamp + (long) msPerSample;
                        }
                    } else {
                        try {
                            nextSample = Float.parseFloat(li.trim());

                            if (nextSample > maxVal) {
                                maxVal = nextSample;
                            }

                            if (nextSample < minVal) {
                                minVal = nextSample;
                            }
                        } catch (NumberFormatException nfe) {
                            nextSample = Float.NaN;
                        }
                        
                        if (prevTimeStamp < 0 || curTimeStamp - prevTimeStamp > 1.2 * msPerSample) {
                        	values.add(new TimeValueStart(curTimeStamp, nextSample));
                        } else {
                        	values.add(new TimeValue(curTimeStamp, nextSample));
                        }
                        // update prev time stamp
                        prevTimeStamp = curTimeStamp;
                    }
                } else { // long notation

                    if (li.indexOf(TIME) > -1 || li.indexOf(NUMBER) > -1) {
                        eqIndex = li.indexOf(EQ);

                        if (eqIndex > 0) {
                            try {
                            	curTimeStamp = (long) (1000 * Double.parseDouble(li.substring(eqIndex +
                                            1).trim()));
                            } catch (NumberFormatException nfe) {
                            	curTimeStamp = curTimeStamp +
                                    (long) msPerSample;
                            }
                        }
                    } else if (li.indexOf(VAL) > -1) {
                        eqIndex = li.indexOf(EQ);

                        if (eqIndex > 0) {
                            try {
                                nextSample = Float.parseFloat(li.substring(eqIndex +
                                            1).trim());

                                if (nextSample > maxVal) {
                                    maxVal = nextSample;
                                }

                                if (nextSample < minVal) {
                                    minVal = nextSample;
                                }
                            } catch (NumberFormatException nfe) {
                                nextSample = Float.NaN;
                            }

                            if (prevTimeStamp < 0 || curTimeStamp - prevTimeStamp > 1.2 * msPerSample) {
                            	values.add(new TimeValueStart(curTimeStamp, nextSample));
                            } else {
                            	values.add(new TimeValue(curTimeStamp, nextSample));
                            }
                            // update prev time stamp
                            prevTimeStamp = curTimeStamp;
                        }
                    } // could count the number of points: points [1]: etc
                }

                rowIndex++;

            }
        }

        // insert zeros if the min_time > 0
        /*
        if (!startsAtZero && (values.size() > 0)) {
            int numToInsert = (int) (minT / msPerSample);
            ArrayList<Float> ins = new ArrayList<Float>(numToInsert);
            values.ensureCapacity(values.size() + numToInsert + 1);
            // fill with the minimum value from file 
            for (int i = 0; i < numToInsert; i++) {
                ins.add(minVal);
            }

            values.addAll(0, ins);
        }
		*/
        /*
        sampleCount = values.size();

        float[] result = new float[sampleCount];

        for (int i = 0; i < sampleCount; i++) {
            result[i] = values.get(i);
        }
		*/
        return values;
    }

    /**
     * Returns the milliseconds per sample value that has been extracted from
     * the  source file. Not relevant for non-continuous rate tracks.
     *
     * @return the milliseconds per sample value
     */
    public float getMsPerSample() {
        return msPerSample;
    }

    /**
     * Returns the sample frequency / sample rate that has been extracted from
     * the  source file. Not relevant for non-continuous rate tracks.
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
        return minVal;
    }

    /**
     * Return the read maximum value.
     *
     * @return the maximum
     */
    public float getMax() {
        return maxVal;
    }
}
