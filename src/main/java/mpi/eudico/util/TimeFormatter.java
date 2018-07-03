package mpi.eudico.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.Locale;


/**
 * A class with some viewer tool methods
 */
public class TimeFormatter {
    private static String hourString;
    private static String minuteString;
    private static String secondString;
    private static String milliString;

    /** a two-digit formatter */
    private static final DecimalFormat twoDigits = new DecimalFormat("00");

    /** a three-digit formatter */
    private static final DecimalFormat threeDigits = new DecimalFormat("000");

    // use the US Locale to make sure a '.' is used and not ','

    /** a n-digit.three-digit formatter */
    private static final DecimalFormat secondsMillis = new DecimalFormat("#0.000",
            new DecimalFormatSymbols(Locale.US));

    /**
     * Converts a time definition in the format hh:mm:ss.sss into a long that
     * contains the time in milli seconds.
     * Jan 2011: now also handles negative time values with minutes and hour values 
     * greater than 0. Negative values with only seconds and milliseconds were 
     * already just parsed, there was no check for a minus sign. 
     *
     * @param timeString the string that contains the time in the format
     *        hh:mm:ss.sss
     *
     * @return the time in seconds, -1.0 if the time string has an illegal
     *         format. Since negative values are also handled, the return value of -1 
     *         to indicate an error was and is quite useless.
     */
    public static long toMilliSeconds(String timeString) {
        try {
            String hourString = new String("0.0");
            String minuteString = new String("0.0");
            String secondString = new String("0.0");
            
            boolean negative = timeString.charAt(0) == '-';
            if (negative) {
            	timeString = timeString.substring(1);
            }
            
            int mark1 = timeString.indexOf(':', 0);

            if (mark1 == -1) { // no :, so interpret string as sss.ss or ms
            	// HS apr-2006: added millisecond support; doesn't this break
            	// anything??
            	if (timeString.indexOf('.') < 0) {
            		// no ':' nor '.', so interpret as bare milliseconds??
            		if (negative) {
            			return -Long.parseLong(timeString);
            		} else {
            			return Long.parseLong(timeString);
            		}
            	} else {
            		// no :, so interpret string as sss.ss
            		secondString = timeString;
            	}

            } else {
                int mark2 = timeString.indexOf(':', mark1 + 1);

                if (mark2 == -1) { // only one :, so interpret string as mm:ss.sss
                    minuteString = timeString.substring(0, mark1);
                    secondString = timeString.substring(mark1 + 1,
                            timeString.length());
                } else { // two :, so interpret string as hh:mm:ss.sss
                    hourString = timeString.substring(0, mark1);
                    minuteString = timeString.substring(mark1 + 1, mark2);
                    secondString = timeString.substring(mark2 + 1,
                            timeString.length());
                }
            }

            double hours = Double.parseDouble(hourString);
            double minutes = Double.parseDouble(minuteString);
            double seconds = Double.parseDouble(secondString);

            if (negative) {
                return (long) -(1000 * ((hours * 3600.0) + (minutes * 60.0) +
                        seconds));
            } else {
	            return (long) (1000 * ((hours * 3600.0) + (minutes * 60.0) +
	            seconds));
            }
        } catch (Exception e) { // the timeString was not parseable

            return -1;
        }
    }

    /**
     * Converts a time in seconds to the following string representation:
     * hh:mm:ss.sss
     *
     * @param time a long containing the time in milli seconds
     *
     * @return the string representation of the time
     */
    public static String toString(long time) {
        long hours = time / 3600000;
        hourString = twoDigits.format(hours);

        long minutes = (time - (3600000 * hours)) / 60000;
        minuteString = twoDigits.format(minutes);

        long seconds = (time - (3600000 * hours) - (60000 * minutes)) / 1000;
        secondString = twoDigits.format(seconds);

        long millis = time - (3600000 * hours) - (60000 * minutes) -
            (1000 * seconds);
        milliString = threeDigits.format(millis);

        return hourString + ":" + minuteString + ":" + secondString + "." +
        milliString;
    }

    /**
     * Converts a time in ms to a ss.mmm formatted String
     *
     * @param time the time value to convert
     *
     * @return a String in the ss.mmm format
     */
    public static String toSSMSString(long time) {
        double dd = time / 1000.0;

        return secondsMillis.format(dd);
    }

    /**
     * Timecode has the format hh:mm:ss:ff. PAL has 25 frames per second.
     *
     * @param time the time to convert
     * @return a PAL timecode string
     */
    public static String toTimecodePAL(long time) {
        long hours = time / 3600000;
        hourString = twoDigits.format(hours);

        long minutes = (time - (3600000 * hours)) / 60000;
        minuteString = twoDigits.format(minutes);

        long seconds = (time - (3600000 * hours) - (60000 * minutes)) / 1000;
        secondString = twoDigits.format(seconds);

        long frames = (time - (3600000 * hours) - (60000 * minutes) -
            (1000 * seconds)) / 40;
        milliString = twoDigits.format(frames);

        return hourString + ":" + minuteString + ":" + secondString + ":" +
        milliString;
    }
    
    /**
     * Time code has the format hh:mm:ss:ff. This PAL variant has 50 frames per second.
     *
     * @param time the time to convert
     * @return a PAL-50 time code string
     */
    public static String toTimecodePAL50(long time) {
        long hours = time / 3600000;
        hourString = twoDigits.format(hours);

        long minutes = (time - (3600000 * hours)) / 60000;
        minuteString = twoDigits.format(minutes);

        long seconds = (time - (3600000 * hours) - (60000 * minutes)) / 1000;
        secondString = twoDigits.format(seconds);

        long frames = (time - (3600000 * hours) - (60000 * minutes) -
            (1000 * seconds)) / 20;
        milliString = twoDigits.format(frames);

        return hourString + ":" + minuteString + ":" + secondString + ":" +
        milliString;
    }

    /**
     * Returns the frame number for a given time in a PAL encoded media file.
     * First frame is index 0.
     *
     * @param time the time in milliseconds
     * @return a string containing the framenumber in PAL
     */
    public static String toFrameNumberPAL(long time) {
    	return String.valueOf((long) (time / 40));
    }
    
    /**
     * Returns the frame number for a given time in a PAL-50 encoded media file.
     * First frame is index 0.
     *
     * @param time the time in milliseconds
     * @return a string containing the frame number in PAL - 50fps
     */
    public static String toFrameNumberPAL50(long time) {
    	return String.valueOf((long) (time / 20));
    }

    /**
     * Timecode has the format hh:mm:ss:ff. NTSC has approx. 29.97 frames per second.
     * The 'standard' SMPTE drop frames mechanism is used for frame number calculation,
     * i.e. drop the first two frames from every minute except every tenth minute.
     *
     * @param time the time to convert
     * @return a NTSC timecode string
     */
    public static String toTimecodeNTSC(long time) {
        // this is already off by a frame in the Premiere testmovie
        int frameNumber = (int)((time / 1000f) * 29.97);
        //int frameNumber = (int)time;
        // every block of ten minutes hasan exact number of frames, 17982. Calculate the
        // number of 10-minute-blocks, can also be used for calculation of hours + minutes
        int numTenMin = frameNumber / 17982;
        int hours = numTenMin / 6;
        numTenMin = numTenMin - (6 * hours);
        // the rest is used to calculate minutes (less than 10), seconds and frames
        // calculate number of complete minutes from remaining frames
        int numMin = frameNumber % 17982;
        // complete minutes
        int min = numMin / 1800;
        // remainder for calc of seconds
        int rest = numMin - (min * 1800);
        int sec = rest / 30;
        // remainig frames
        int fr = rest - (sec * 30);
        // adjust, add 2 frames for each minute
        fr += (min * 2);
        // if frames > 29 add extra second and eventually minute
        if (fr > 29) {
            fr -= 30;
            sec += 1;
            if (sec > 59) {
                sec = 0;
                min += 1;
                fr += 2;
            }
        }
        // convert to string
        return twoDigits.format(hours) + ":" + twoDigits.format((numTenMin * 10) + min) + ":"
        + twoDigits.format(sec) + ":" + twoDigits.format(fr);
    }

    /**
     * Returns the frame number for a given time in a NTSC encoded media file.
     * First frame is index 0.
     *
     * @param time the time in milliseconds
     * @return a string containing the framenumber in NTSC
     */
    public static String toFrameNumberNTSC(long time) {
        // this is already off by a frame in the Premiere testmovie
        int frameNumber = (int)((time / 1000f) * 29.97);

    	return String.valueOf(frameNumber);
    }
}
