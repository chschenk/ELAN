package mpi.eudico.client.annotator.player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.logging.Logger;


/**
 * A class to extract the video width and height from an mpeg file.<br>
 * A QuickTime player does not provide access to these fields;  when
 * extracting a frame from the video we need the unscaled image  (for graphic
 * annotations). Unfinished.
 * This class is based on several third party pieces of software (e.g. VideoFile from 
 * XNap).
 * 
 * @author Han Sloetjes
 * @version 1.0 nov 2004
 */
public class MPEGVideoHeader {
    /** Holds value of property DOCUMENT ME! */
    private static final Logger LOG = Logger.getLogger(MPEGVideoHeader.class.getName());

    // constants

    /** Holds value of property pack start code */
    private static final int PACK_START_CODE = 0x000001BA;

    //private static final int   SYSTEM_HEADER_START_CODE      = 0x000001BB;

    /** Holds value of property video header start code */
    private static final int VIDEO_HEADER_START_CODE = 0x000001B3;

    /** Holds value of property mpeg4 video header start code */
    private static final int MPEG4_VIDEO_HEADER_START_CODE = 0x000001B0;

    //private static final int 	 PADDING_BLOCK_START_CODE			 = 0x000001BE;
    //private static final int   PRIVATE_BLOCK_START_CODE      = 0x000001BD;
    //private static final int   PRIVATE2_BLOCK_START_CODE     = 0x000001BF;
    //private static final int   STREAM_END_START_CODE         = 0x000001B9;
    //private static final int   GROUP_START_CODE              = 0x000001B8;

    /** Holds value of property first audio header code */
   // private static final int FIRST_AUDIO_CODE = 0x000001C0;

    /** Holds value of property last audio header code */
   // private static final int LAST_AUDIO_CODE = 0x000001DF;

    /** Holds value of the number of bytes to probe */
    private static final int MAX_FORWARD_READ_LENGTH = 500000;

    //private static final int MAX_BACKWARD_READ_LENGTH = 5000000;

    /** The table of pel aspect ratio's from the MPEG specs  */
    private static final float[] pelAspectRatioTable = {
        0.0f, 1.0f, 0.6735f, 0.7031f, 0.7615f, 0.8055f, 0.8437f, 0.8935f,
        0.9375f, 0.9815f, 1.0255f, 1.0695f, 1.1250f, 1.1575f, 1.2015f, 1.0f
    };

    /** The table of frame rates from the MPEG specs  */
    private static final float[] pictureRateTable = {
        0.0f, 23.976f, 24.0f, 25.0f, 29.97f, 30.0f, 50.0f, 59.94f, 60.0f, -1.0f,
        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f
    };

    /** unknown int value */
    private static final int UNKNOWN_INT = -1;

    /** Hunknoen float value */
    private static final float UNKNOWN_FLOAT = -1.0f;
    private int width = UNKNOWN_INT;
    private int height = UNKNOWN_INT;
    //private double duration = UNKNOWN_FLOAT;
    private float fps = UNKNOWN_FLOAT;
    private float pelAspectRatio = UNKNOWN_FLOAT;
    private boolean firstPackFound = false;
    private int mpegType = UNKNOWN_INT;
    //private boolean audioFound = false;
    private double initialSCR = UNKNOWN_FLOAT;
    //private double lastSCR = UNKNOWN_FLOAT;
    //private int minFileLength = 1024;
    private RandomAccessFile raf;

    /**
     * Constructor.
     *
     * @param path the path of the mpeg file
     */
    public MPEGVideoHeader(String path) {
        readHeader(path);
    }

    /**
     * Main method for standalone use.
     *
     * @param args the path to the mpeg file should be supplied
     */
    public static void main(String[] args) {
        if ((args == null) || (args.length == 0) || (args[0].length() == 0)) {
            usage();

            return;
        }

        new MPEGVideoHeader(args[0]);
    }

    /**
     * Reads the header of the file.
     *
     * @param path the path to the file
     */
    private void readHeader(String path) {
        File mpegFile = new File(path);

        if (!mpegFile.exists()) {
            LOG.warning("Mpeg file not found: " + path);
			// usage();
			
            return;           
        }

        try {
            raf = new RandomAccessFile(mpegFile, "r");
            
			/*
            long fileLength = raf.length();			
            if (fileLength < minFileLength) {
                //do something
            }
            */

            readStart();

            //readEnd();
            //printResults();
        } catch (FileNotFoundException fnfe) {
            LOG.warning("Mpeg file not found.");

            //usage();
        } catch (IOException ioe) {
            LOG.warning("Error reading Mpeg file: " + 
            	ioe.getMessage());
        } catch (Exception e) {
        	// catch any exception
        	LOG.warning("Unknown error reading Mpeg file.");
        } finally {
            try {
                raf.close();
            } catch (IOException ioee) {
            }
        }
    }

    /**
     * Extracts and calculates the System Clock Reference value for MPEG1.
     *
     * @param b the bytes read from the file
     *
     * @return the scr value
     */
    protected double getMPEGSCR(byte[] b) {
        //	int highbit = (b[0] >> 3) & 0x01;
        long low4Bytes = ((((b[0] & 0xff) >> 1) & 0x03) << 30) |
            ((b[1] & 0xff) << 22) | (((b[2] & 0xff) >> 1) << 15) |
            ((b[3] & 0xff) << 7) | ((b[4] & 0xff) >> 1);

        double scr = (double) low4Bytes / 90000;

        return scr;
    }

    /**
     * Extracts and calculates the System Clock Reference value for MPEG2.
     * Unfinished
     *
     * @param b the bytes read from the file
     *
     * @return the scr value
     */
    protected double getMPEG2SCR(byte[] b) {
        double scr;

        //	int highbit = (b[0] & 0x20) >> 5;
        long low4Bytes = (((b[0] & 0x18) >> 3) << 30) | ((b[0] & 0x03) << 28) |
            ((b[1] & 0xff) << 20) | (((b[2] & 0xF8) >> 1) << 15) |
            ((b[2] & 0x03) << 13) | ((b[3] & 0xff) << 5) |
            ((b[4] & 0xff) >> 3);

        int sys_clock_extension = ((b[4] & 0x3) << 7) | ((b[5] & 0xff) >> 1);

        if (sys_clock_extension == 0) {
            scr = (double) low4Bytes / 90000;

            return scr;
        } else {
            //???
            return 0.0;
        }
    }

    /**
     * Print usage.
     */
    private static void usage() {
        System.out.println("Enter a path to a MPEG file!");
    }
	
	/**
	 * Reads from the beginning of the file and checks for the 0x000001 part of the 
	 * header start codes. When such a code is found checkHeaderCode() is called to 
	 * handle (or ignore) the start code.
	 * Note: a stupid way to find header codes, there should be a better way.
	 * 
	 * @throws IOException io exception
	 */
    private void readStart() throws IOException {
        int forward = 1024;
        byte[] b = new byte[forward];
        int available;
        long curPointer = 0;

        for (int i = 0; i < MAX_FORWARD_READ_LENGTH; i += (available - 2)) {
            available = raf.read(b);
            curPointer = raf.getFilePointer();

            if (available > 0) {
                for (int offset = 0; offset < (available - 2); offset++) {
                    if ((b[offset] == 0) && (b[offset + 1] == 0) &&
                            (b[offset + 2] == 1)) {
                        // set the pointer back
                        raf.seek(curPointer - (available - offset));
                        checkHeaderCode();

                        //stop if we found mpeg type and video width/height
                        if ((mpegType != UNKNOWN_INT) &&
                                (width != UNKNOWN_INT)) {
                            return;
                        }
                    }
                }
            } else {
            	// Reached end of file... stop.
            	break;
            }
            // Go back a bit to re-read the last 2 unexamined bytes
            raf.seek(curPointer - 2);
        }
    }

	/**
	 * Looks for the last PACK_START_CODE in the file.
	 * @throws IOException io exception
	 */
	/*
    private void readEnd() throws IOException {
        int bufSize = 8024;

        if (bufSize < raf.length()) {
            bufSize = (int) (raf.length() / 2);
        }

        byte[] b = new byte[bufSize];
        raf.seek(raf.length());

        for (int i = 0; i < (raf.length() - b.length); i += b.length) {
            long fp = raf.getFilePointer() - b.length;

            if (i != 0) {
                fp += 3;
            }

            if (fp < 0) {
                if (fp <= b.length) {
                    break;
                }

                fp = 0;
            }

            raf.seek(fp);
            raf.readFully(b);

            boolean packFound = false;

            // look for 0x000001BA, then break
            for (int offset = b.length - 1; offset > 2; offset--) {
                if ((b[offset - 3] == 0) && (b[offset - 2] == 0) &&
                        (b[offset - 1] == 1)) {
                    if ((b[offset] & 0xff) == 0xBA) {
                        raf.seek(raf.getFilePointer() -
                            (b.length - offset - 1));
                        packFound = true;

                        break;
                    }
                }
            }

            if (packFound) {
                // we are at the position right after the last pack start code
                readLastPackHeader();

                return;
            } else {
                raf.seek(raf.getFilePointer() - b.length);
            }
        }
    }
	*/
	
	/**
	 * Checks which header start code has been found.
	 * 
	 * @throws IOException io exception
	 */
    private void checkHeaderCode() throws IOException {
        int code = raf.readInt();

        switch (code) {
        case PACK_START_CODE:

            if (!firstPackFound) {
                readPackHeader();
            }

            break;

        case VIDEO_HEADER_START_CODE:

            if ((width == UNKNOWN_INT) || (height == UNKNOWN_INT)) {
                readVideoHeader();
            }

            break;

        case MPEG4_VIDEO_HEADER_START_CODE:
            mpegType = 4;

            // read the mp4 header, to be implemented
            break;
        }
		
		// this is not reliable...
		/*
        if ((code >= FIRST_AUDIO_CODE) && (code <= LAST_AUDIO_CODE)) {
            audioFound = true;
        }
        */
    }

	/**
	 * Reads some bytes following the last PACK_START_CODE in the file.<br>
	 * The System Clock Reference can be extracted for the calculation of the 
	 * duration, which does not function properly.
	 *  
	 * @throws IOException io exception
	 */
	/*
    private void readLastPackHeader() throws IOException {
        if (lastSCR != UNKNOWN_FLOAT) {
            return;
        }

        byte[] b = new byte[6];

        //raf.readFully(b);
        int available = raf.read(b);

        if (available == b.length) {
            if ((b[0] & 0xF0) == 0x20) {
                lastSCR = getMPEGSCR(b);
            } else if ((b[0] & 0xC0) == 0x40) {
                lastSCR = getMPEG2SCR(b);
            }

            // ?? can these values be 0
            if ((initialSCR >= 0) && (lastSCR >= 0)) {
                duration = lastSCR - initialSCR;
            }
        }
    }
    */

	/**
	 * Reads some bytes following a PACK_START_CODE.<br>
	 * Here it can be determined what type of Mpeg file it is
	 * (inclomplete...) and the System Clock Reference can be extracted.
	 * Now used for the calculation of the duration, which does not 
	 * function properly.
	 *  
	 * @throws IOException io exception
	 */
    private void readPackHeader() throws IOException {
        if (initialSCR != UNKNOWN_FLOAT) {
            return;
        }

        byte[] b = new byte[6];

        raf.readFully(b);

        if ((b[0] & 0xF0) == 0x20) {
            mpegType = 1;
            initialSCR = getMPEGSCR(b);
        } else if ((b[0] & 0xC0) == 0x40) {
            mpegType = 2;
            initialSCR = getMPEG2SCR(b);
        }
    }

	/**
	 * Reads some bytes following a VIDEO_HEADER_START_CODE.<br>
	 * The image width and height as well as the pel aspect ratio index 
	 * and the frame rate index are extracted.
	 * @throws IOException io exception
	 */
    private void readVideoHeader() throws IOException {
        if ((width != UNKNOWN_INT) && (height != UNKNOWN_INT)) {
            return;
        }

        byte[] b = new byte[3];
        raf.readFully(b);

        width = (((b[0] & 0xff) << 4) | (b[1] & 0xf0));
        height = (((b[1] & 0x0f) << 8) | (b[2] & 0xff));

        byte nb = raf.readByte();
        int ratioIndex = (nb & 0xff) >> 4;

        if ((ratioIndex > 0) && (ratioIndex < pelAspectRatioTable.length)) {
            pelAspectRatio = pelAspectRatioTable[ratioIndex];
        }

        int framerateIndex = (nb & 0x0f);

        if ((framerateIndex > 0) && (framerateIndex < pictureRateTable.length)) {
            fps = pictureRateTable[framerateIndex];
        }
    }

    /**
     * Returns the number of frames per second.
     *
     * @return the number of frames per second
     */
    public float getFps() {
        return fps;
    }

    /**
     * Returns the height of the video images.
     *
     * @return the height of the video images
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the width of the video images.
     *
     * @return the width of the video images
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the duration of the mediafile.
     *
     * @return the duration 
     */
    /*
    public double getDuration() {
        return duration;
    }
    */

    /**
     * Print some results extracted from the media file.
     */
    public void printResults() {
        boolean isPal = (fps == pictureRateTable[3]);
        boolean isNTSC = (fps == pictureRateTable[4]);
        
        String results = "MPEG Header Info:\n" + "MPEG type: " + mpegType +
            "\nWidth: " + width + "\nHeight: " + height + "\nAspect Ratio: " +
            pelAspectRatio + "\nFrame Rate: " + fps + "\nIs PAL: " + isPal +
            "\nIs NTSC: " + isNTSC;
        System.out.println(results);
    }
}
