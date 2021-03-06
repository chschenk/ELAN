/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * A first shot at a class that provides samples given a certain media file. In this version only
 * .wav files are supported. In the future this class should become more general and belongs in a
 * different package than the current one.
 * $Id: WAVSampler.java 44710 2015-11-30 10:27:00Z hasloe $
 *
 * @author $Author$
 * @version $Revision$ *
 */
public class WAVSampler {
	
    private RandomAccessFile soundFile;
    private WAVHeader wavHeader;
    private byte[] buffer;
    private int[] intArrayLeft;
    private int[] intArrayRight;
    private float duration;
    private int headerSize;
    private int maxSampleFirst;
    private int maxSampleSecond;
    private int minSampleFirst;
    private int minSampleSecond;
    private int possibleMaxSample;
    private int possibleMinSample;
    private int sampleFrequency;
    private long nrOfSamples;
    private short bitrate;
    private short compressionCode;
    private short nrOfChannels;
    private short sampleSize;

    /**
     * Construct the Samples object only for .wav files, otherwise an IOException is thrown.
     *
     * @param fileName DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public WAVSampler(String fileName) throws IOException {
        buffer = new byte[4096];

        if (fileName.toLowerCase().endsWith(".wav")) {
            soundFile = new RandomAccessFile(fileName, "r");
            wavHeader = new WAVHeader(soundFile);
            
            if (wavHeader.getHeaderSize() == 0) {
            	soundFile.close();
            	throw new IOException("Invalid wav file format");
            }
            
            sampleFrequency = wavHeader.getFrequency();
            sampleSize = wavHeader.getSampleSize();
            nrOfSamples = wavHeader.getDataLength() / sampleSize;
            nrOfChannels = wavHeader.getNumberOfChannels();
            bitrate = (short) ((sampleSize * 8) / nrOfChannels);
            duration = ((float) 1000 * nrOfSamples) / sampleFrequency;
            possibleMinSample = (int) (-Math.pow(2, bitrate - 1));
            possibleMaxSample = (int) (-1 + Math.pow(2, bitrate - 1));
            headerSize = wavHeader.getHeaderSize();
            compressionCode = wavHeader.getCompressionCode();

            if (compressionCode == WAVHeader.WAVE_FORMAT_ALAW) {
                possibleMinSample *= 64;
                possibleMaxSample *= 64;
            }           		
            		
//            System.out.println("Information from header of wav-file:");
//            System.out.println("NrOfChannels    : " + nrOfChannels);
//            System.out.println("Sample frequency: " + sampleFrequency);
//            System.out.println("Bitrate         : " + bitrate);
//            System.out.println("nrOfSamples     : " + nrOfSamples);
//            System.out.println(
//                " -> Length      : " + TimeFormatter.toString(getTimeAtSample(nrOfSamples)));
//            System.out.println(
//                "WAVE Format     : " +
//                ((compressionCode < WAVHeader.formatDescriptions.length)
//                ? WAVHeader.formatDescriptions[compressionCode] : ("" + compressionCode)));
//
//            if (!wavHeader.getInfo().equals("")) {
//                System.out.println("\nMeta info tail:" + wavHeader.getInfo());
//            }

            soundFile.seek(headerSize);
        }
        else {
            throw new IOException("Unsupported file format");
        }
    }

    /**
     * Returns the total duration of the samples in milli seconds
     *
     * @return DOCUMENT ME!
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Gets the array that stores the data of the first channel of an interval that has been read.
     *
     * @return an array of ints
     *
     * @see #readInterval(int, int)
     */
    public int[] getFirstChannelArray() {
        return intArrayLeft;
    }

    /**
     * Returns the maximal value of the read samples.
     *
     * @return DOCUMENT ME!
     */
    public int getMaxSampleFirst() {
        return maxSampleFirst;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the mamimal value of the read samples of the second channel
     */
    public int getMaxSampleSecond() {
        return maxSampleSecond;
    }

    /**
     * Returns the minimal value of the read samples.
     *
     * @return DOCUMENT ME!
     */
    public int getMinSampleFirst() {
        return minSampleFirst;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the minimal value of the read samples of the second channel
     */
    public int getMinSampleSecond() {
        return minSampleSecond;
    }

    /**
             *
             */
    public long getNrOfSamples() {
        return nrOfSamples;
    }

    /**
     * Returns the maximal possible value of the samples.
     *
     * @return DOCUMENT ME!
     */
    public int getPossibleMaxSample() {
        return possibleMaxSample;
    }

    /**
     * Returns the minimal possible value of the samples.
     *
     * @return DOCUMENT ME!
     */
    public int getPossibleMinSample() {
        return possibleMinSample;
    }

    /**
     * Returns the sample frequency
     *
     * @return DOCUMENT ME!
     */
    public int getSampleFrequency() {
        return sampleFrequency;
    }

    /**
     * Get the current sample nr
     *
     * @return DOCUMENT ME!
     */
    public long getSamplePointer() {
        long pointer = 0;

        try {
            pointer = (soundFile.getFilePointer() - headerSize) / sampleSize;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return pointer;
    }

    /**
     * Gets the array that stores the data of the second channel of an interval that has been read.
     *
     * @return an array of ints
     *
     * @see #readInterval(int, int)
     */
    public int[] getSecondChannelArray() {
        return intArrayRight;
    }

    /**
     * returns time corresponding to a given sample
     *
     * @param sample
     *
     * @return long time in ms
     */
    public long getTimeAtSample(long sample) {
        return (long) (((sample * 1000f) / sampleFrequency) + .5);
    }

    /**
     * Get the time in milli seconds that corresponds with the current sample
     *
     * @return DOCUMENT ME!
     */
    public float getTimePointer() {
        return ((float) 1000 * getSamplePointer()) / sampleFrequency;
    }

    /**
     * Gets the <code>WavHeader</code>.
     *
     * @return the <code>WavHeader</code> object of this <code>WavSampler</code>
     */
    public WAVHeader getWavHeader() {
        return wavHeader;
    }

    /**
     * Close the RandomAccessFile.
     */
    public void close() {
        wavHeader = null;

        if (soundFile != null) {
            try {
                soundFile.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        soundFile = null;
    }

    /**
     * Reads the requested number of samples in one run and stores the extracted values  in one
     * (mono or merged) or two (stereo) arrays of ints. These arrays can then  be accessed to
     * further process the samples.<br>
     * <b>Note:</b> if the loading of two channels is requested, a second array of the proper size
     * will always be created, even if the .wav file does not actually contain more than one
     * channel.
     *
     * @param requestedNrOfSamplesToRead the number of samples to read
     * @param nrOfChannelsToLoad the number of channels to get
     *
     * @return the number of <b>samples</b> that have been read, less than or equal to
     *         requestedNrOfSamplesToRead
     *
     * @see #getFirstChannelArray()
     * @see #getSecondChannelArray()
     */
    public int readInterval(int requestedNrOfSamplesToRead, int nrOfChannelsToLoad) {
        int actualRead = 0;
        boolean stereoOutput = false;

        int actualNrOfSamplesToRead = requestedNrOfSamplesToRead;

        int samplesAvailable = (int) (getNrOfSamples() - getSamplePointer());

        if (requestedNrOfSamplesToRead > samplesAvailable) {
            actualNrOfSamplesToRead = samplesAvailable;
        }

        if ((intArrayLeft == null) || (intArrayLeft.length < actualNrOfSamplesToRead)) {
            intArrayLeft = new int[actualNrOfSamplesToRead];
        }

        Arrays.fill(intArrayLeft,  0);

        if (nrOfChannelsToLoad == 2) {
            stereoOutput = true;

            if ((intArrayRight == null) || (intArrayRight.length < actualNrOfSamplesToRead)) {
                intArrayRight = new int[actualNrOfSamplesToRead];
            }

            Arrays.fill(intArrayRight, 0);
        }
        else {
            nrOfChannelsToLoad = 1;
            intArrayRight = null;
        }

        if (buffer.length < (actualNrOfSamplesToRead * sampleSize)) {
            buffer = new byte[actualNrOfSamplesToRead * sampleSize];
        }

        // actual reading
        try {
            actualRead = soundFile.read(buffer, 0, actualNrOfSamplesToRead * sampleSize);

            //System.out.println("Requested (samples): " + requestedNrOfSamplesToRead);
            //System.out.println("Reading (samples):   " + actualNrOfSamplesToRead);
            //System.out.println("Reading (bytes):     " + actualNrOfSamplesToRead * sampleSize);
            //System.out.println("Bytes read:          " + actualRead);
        }
        catch (IOException ioe) {
//            System.out.println("IO Error while reading samples.");

            return actualRead; //=0
        }

        // 8 bit mono
        if (sampleSize == 1) {
            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                intArrayLeft[s] = convert(buffer[s] & 0xFF);
            }
        }
        else if (sampleSize == 2) {
        	// 16 bit mono
            if (nrOfChannels == 1) {
                int b = 0;
                int b1;
                int b2;

                for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                    b1 = buffer[b] & 0xFF;
                    b2 = buffer[b + 1];
                    intArrayLeft[s] = convert(b1 | (b2 << 8));
                    b += 2;
                }
            }
            // 8 bit stereo
            else {
                int b = 0;
                int b1;

                for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                    // channel 1
                    b1 = buffer[b] & 0xFF;
                    intArrayLeft[s] = convert(b1);

                    // channel 2
                    b1 = buffer[b + 1] & 0xFF;

                    if (stereoOutput) {
                        intArrayRight[s] = convert(b1);
                    }
                    else {
                        intArrayLeft[s] = (intArrayLeft[s] + convert(b1)) / 2;
                    }

                    b += 2;
                }
            }
        }
        // 24 bit mono
        else if ((sampleSize == 3) && (nrOfChannels == 1)) {
            int b = 0;
            int b1;
            int b2;
            int b3;

            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2];

                intArrayLeft[s] = convert(b1 | (b2 << 8) | (b3 << 16));
                b += 3;
            }
        }
        // 16 bit stereo
        else if ((sampleSize == 4) && (nrOfChannels == 2)) {
            int b = 0;
            int b1;
            int b2;

            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                // channel 1
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1];
                intArrayLeft[s] = convert(b1 | (b2 << 8));

                // channel 2
                b1 = buffer[b + 2] & 0xFF;
                b2 = buffer[b + 3];

                if (stereoOutput) {
                    intArrayRight[s] = convert(b1 | (b2 << 8));
                }
                else {
                    intArrayLeft[s] = (intArrayLeft[s] + (convert(b1 | (b2 << 8)))) / 2;
                }

                b += 4;
            }
        }
        //24 bit stereo
        else if ((sampleSize == 6) && (nrOfChannels == 2)) {
            int b = 0;
            int b1;
            int b2;
            int b3;

            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                // channel 1
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2];

                intArrayLeft[s] = convert(b1 | (b2 << 8) | (b3 << 16));

                // channel 2
                b1 = buffer[b + 3] & 0xFF;
                b2 = buffer[b + 4] & 0xFF;
                b3 = buffer[b + 5];

                if (stereoOutput) {
                    intArrayRight[s] = convert(b1 | (b1 << 8) | (b3 << 16));
                }
                else {
                    intArrayLeft[s] = (intArrayLeft[s] + convert(b1 | (b2 << 8) | (b3 << 16))) / 2;
                }

                b += 6;
            }
        }
        
        //System.out.println("Returning (samples):     " + actualRead / sampleSize);
        //System.out.println("IntArrayLeft (samples):  " + intArrayLeft.length + " thread: " +Thread.currentThread());
        //System.out.println("IntArrayRight (samples): " + intArrayRight.length + " WavSampler: " + this);
        return actualRead / sampleSize;
    }

    /**
     * Seek the n-th sample
     *
     * @param n DOCUMENT ME!
     */
    public void seekSample(long n) {
        if ((n < 0) || (n > nrOfSamples)) {
//            System.out.println("Cannot seek sample " + n + ".");
        }
        else {
            try {
                soundFile.seek(headerSize + (n * sampleSize));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Seek the sample that corresponds with the time in milli seconds
     *
     * @param time DOCUMENT ME!
     */
    public void seekTime(float time) {
        if (time < 0) {
//            System.out.println("Cannot seek sample for time `" + time + "' !");
        }
        else {
            seekSample((long) ((time * sampleFrequency) / 1000));
        }
    }

    private int convert(int orig) {
        switch (compressionCode) {
        case WAVHeader.WAVE_FORMAT_ALAW: //alaw

            byte val = toUnsigned((byte) orig);
            int t;
            int seg;

            val ^= 0x55;
            t = (val & 0xf) << 4;
            seg = (toUnsigned(val) & 0x70) >> 4;

            switch (seg) {
            case 0:
                t += 8;

                break;

            case 1:
                t += 0x108;

                break;

            default:
                t += 0x108;
                t <<= (seg - 1);
            }

            return (((val & 0x80) != 0) ? t : (-t));

        default:
            return orig;
        }
    }

    private byte toUnsigned(byte signed) {
        return (byte) (signed + 128);
    }
}
