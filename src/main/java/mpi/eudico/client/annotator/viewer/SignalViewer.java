package mpi.eudico.client.annotator.viewer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ClipWaveCommand;
import mpi.eudico.client.annotator.gui.FormattedMessageDlg;
// ALBERT
import mpi.eudico.client.annotator.recognizer.data.Boundary;
import mpi.eudico.client.annotator.recognizer.data.BoundarySegmentation;
// END ALBERT
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.client.util.WAVCuePoint;
import mpi.eudico.client.util.WAVHeader;
import mpi.eudico.client.util.WAVSampler;
import mpi.eudico.util.TimeFormatter;

/**
 * Draws a waveform of each audio channel, a crosshair cursor denoting the
 * current media time and the selection if the user has selected a part of the
 * media file.
 */
@SuppressWarnings("serial")
public class SignalViewer extends TimeScaleBasedViewer
    implements ComponentListener, MouseListener, MouseMotionListener,
    MouseWheelListener, ActionListener, GesturesListener, Scrollable {
    /** property for visualization of mono, one channel files */
    public static final int MONO = 0;

    /** property for separate visualization of stereo channels */
    public static final int STEREO_SEPARATE = 1;

    /** property for visualization of stereo channels combined as a single channel */
    public static final int STEREO_MERGED = 2;

    /** property for colored overlay visualization of stereo channels */
    public static final int STEREO_BLENDED = 3;

    /** The default number of pixels for one second of media time. */
    static final int PIXELS_FOR_SECOND = 100;
    private int channelMode;

    /** An array of zoomlevels. */
    public final int[] VERT_ZOOM = new int[] {
            100, 150, 200, 300, 500, 1000, 2000, 3000
        };
    
    /** a constant for the size of the space between two channels */
    private final int GAP = 4;
    private int rulerHeight;
    private BufferedImage bi;
    private Graphics2D big2d;
    private AlphaComposite alpha04;
    private AlphaComposite alpha07;
    private WavePart currentPart;

    /** a constant for determining how much data should be read/buffered relative to 
     * the width of the image area. The value 1 means no extra buffering, just the 
     * current interval */
    private final int SCREEN_BUFFER = 1;

    private WAVSampler samp;

    /** The initial number of milliseconds per pixel */
    public final int DEFAULT_MS_PER_PIXEL = 10;

    /** The current number of milliseconds per pixel */
    private float msPerPixel;

    /**
     * The number of sound samples per pixel. Is msPerPixel * samples per ms, is
     * msPerPixel * samplefrequency / 1000.
     */
    private float samplesPerPixel;

    /**
     * The resolution in number of pixels for a second. This is not a
     * percentage value. Historically resolution = PIXELS_FOR_SECOND  factor,
     * where factor = 100 / menu_resolution_percentage_value.
     */
    //private int resolution;
    private TimeRuler ruler;
    private int maxAmplitude;
    private int imageWidth;
    private int imageHeight;
    private long crossHairTime;
    private int crossHairPos;
    private long intervalBeginTime;
    private long intervalEndTime;
    private long dragStartTime;
    private long selectionBeginTime;
    private long selectionEndTime;
    private int selectionBeginPos;
    private int selectionEndPos;
    private Point dragStartPoint;
    private Point dragEndPoint;

    /** number of pixels for the distance from the left or right viewer boundary
     * within which a mouse-drag action starts scrolling the interval to left or right.
     * Also used as a margin for certain vizualization aspects. */
    public final int SCROLL_OFFSET = 16;
    private DragScroller scroller;
    private JPopupMenu popup;
    private JMenuItem praatSelMI;
    private JMenuItem clipSelPraatMI;
    private JMenuItem clipSelJavaSoundMI;
	private JMenuItem infoItem;
    private ButtonGroup zoomBG;
    private JRadioButtonMenuItem customZoomMI;
    private JMenuItem zoomSelectionMI;
    private JMenuItem zoomToEntireMediaMI;
    private ButtonGroup vertZoomGroup;
    private JMenu channelMI;
    private JRadioButtonMenuItem separateMI;
    private JRadioButtonMenuItem mergedMI;
    private JRadioButtonMenuItem blendMI;
    private JCheckBoxMenuItem timeRulerVisMI;
    private JCheckBoxMenuItem timeScaleConMI;
    private JMenuItem copyOrigTimeItem;
    private boolean timeScaleConnected;
    private boolean panMode;
    private boolean timeRulerVisible;
    private boolean clearSelOnSingleClick = true;
    private int vertZoom = 100;
    private final ReentrantLock paintLock = new ReentrantLock();
    private int paintLockTimeOut = 20;

    /**
     * an offset in milliseconds into the media file where the new media begin
     * point (0 point) is situated
     */
    private long mediaOffset;
    /** store the path to the media file */
    private String mediaFilePath;

    /** a flag for the scroll thread */
    boolean stopScrolling = true;

    /** a flag for whether or not the viewer allows to connect to another timeline-based viewer */
    boolean allowConnecting = true;
    private Color selectionColor = Constants.SELECTIONCOLOR;

    //  ALBERT
    JMenu segmentationMenu;
    BoundarySegmentation segmentationChannel1;
    boolean showSegmentationChannel1;
    JCheckBoxMenuItem segmentationChannel1Item;
    BoundarySegmentation segmentationChannel2;
    boolean showSegmentationChannel2;
    JCheckBoxMenuItem segmentationChannel2Item;
    // END ALBERT
    // some error feedback
    private String errorKey = null;
    
    
    private boolean recalculateInterval = true;
    private int horScrollSpeed = 10;
    // reads global setting
    private boolean useBufferedImage = false;
    private AffineTransform identityTransform = new AffineTransform();
    
    // can move class up? whether or not the viewer is the view of a scrollpane's viewport
    private boolean viewPortMode = false;
    private JViewport viewPort = null;
    private ViewPortChangeListener viewPortListener = null;
    private final int MAX_READ_NUM_SAMPLES = (int) Math.pow(2, 22); //power 22 = 4194304, 23 = 8388608, 24 = 16777216
        
    /**
     * Create a SignalViewer with some default values but without any data to
     * display.
     */
    public SignalViewer() {
        initViewer();
//        String bufImg = System.getProperty("useBufferedImage");
//        if (bufImg != null && bufImg.toLowerCase().equals("true")) {
//        	useBufferedImage = true;
//        }
        useBufferedImage = SystemReporting.useBufferedPainting;
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        setDoubleBuffered(false);
        setOpaque(true);

        //setVisible(true);
    }
    
    
    /**
     * Constructor used from Corex
     *
     * @param mediaURL
     * @param allowConnecting
     */
    public SignalViewer(URL mediaURL, boolean allowConnecting) {
        this(mediaURL);
        this.allowConnecting = allowConnecting;
        removeMouseMotionListener(this);
    }

    /**
     * Creates a new SignalViewer instance
     *
     * @param mediaUrl DOCUMENT ME!
     */
    public SignalViewer(URL mediaUrl) {
        this(mediaUrl.toExternalForm());
    }
    
    /**
     * Creates a new SignalViewer using the specified path as the media source.
     * 
     * @param mediaPath the path to the media source (WAV file)
     */
    public SignalViewer(String mediaPath) {
    	this();
		setMedia(mediaPath);
		paintBuffer();
		System.out.println("MediaUrl SignalViewer: " + mediaPath);
    }
    
    /**
     * Overrides <code>JComponent</code>'s processKeyBinding by always returning false.
     * Necessary for the proper working of (menu) shortcuts in Elan. 
     */
	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		return false;
	}

    /**
     * Do some initialisation.
     */
    private void initViewer() {
        if (Constants.DEFAULT_LF_LABEL_FONT != null) {
        	ruler = new TimeRuler(Constants.deriveSmallFont(Constants.DEFAULT_LF_LABEL_FONT), 
        			TimeFormatter.toString(0));
        } else {
        	ruler = new TimeRuler(Constants.DEFAULTFONT, TimeFormatter.toString(0));
        }        
        rulerHeight = ruler.getHeight();
        timeRulerVisible = true;
        channelMode = STEREO_MERGED;
        msPerPixel = DEFAULT_MS_PER_PIXEL;
        samplesPerPixel = ((msPerPixel * 44100) / 1000); //default freq
        //resolution = PIXELS_FOR_SECOND;
        maxAmplitude = Short.MAX_VALUE;
        imageWidth = 0;
        imageHeight = 0;
        crossHairTime = 0;
        crossHairPos = 0;
        intervalBeginTime = 0;
        intervalEndTime = 0;
        dragStartTime = 0;
        selectionBeginTime = 0;
        selectionEndTime = 0;
        selectionBeginPos = 0;
        selectionEndPos = 0;
        alpha04 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
        alpha07 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
        currentPart = new WavePart(WavePart.INT_ARRAY_MODE);

        //currentPart = new WavePart(WavePart.GENERAL_PATH_MODE);
        timeScaleConnected = true;
        mediaOffset = 0L;
    }


    /**
     * Sets the source for this viewer.
     * It is synchronized to prevent interference with ControllerUpdates,
     * which run on separate threads and may call various methods which
     * end up looking at the WAVSampler.
     *
     * @param mediaPath the URL of the source as a String
     */
    synchronized public void setMedia(String mediaPath) {
    	// old test; if the file is video file try to find 
    	// a .wav file with the same name
		if (!mediaPath.endsWith("wav")) {
			int i = mediaPath.lastIndexOf('.');
			if (i > 0) {
				mediaPath = mediaPath.substring(0, i) + ".wav";
			} else {
				mediaPath = mediaPath + ".wav";
			}
		}

		if (mediaPath.startsWith("file:")) {
			mediaPath = mediaPath.substring(5);
		} else {
			// check protocol??
		}
		mediaFilePath = mediaPath;
        initLoad(mediaPath);
        paintBuffer();

        //repaint();
    }
    
    /**
     * Returns the path to the .wav file.
     * 
     * @return the path to the .wav file
     */
    public String getMediaPath() {
    	return mediaFilePath;
    }
    
    /**
     * Tries to start the Praat executable and select the specified interval.
     * 
     * @see http://www.fon.hum.uva.nl/praat/
     * @param begin the selection begin time
     * @param end the selection end time
     */
    private void openInPraat(long begin, long end) {
    	PraatConnection.openInPraat(mediaFilePath, begin, end);
    }
    
	/**
	 * Tries to start the Praat executable and select the current selection.
	 * 
	 * @see http://www.fon.hum.uva.nl/praat/
	 */
    private void openSelectionInPraat() {
    	if (getSelectionBeginTime() == getSelectionEndTime()) {
    		openInPraat(0, 0);
    	} else {
			openInPraat(getSelectionBeginTime() + mediaOffset, getSelectionEndTime() + mediaOffset);
    	}
    }
    
    /**
     * Tries to create a clip from the wav file using Praat.
     */
    private void clipSelectionWithPraat() {
    	if (getSelectionBeginTime() == getSelectionEndTime()) {
    		return;
    	} 
		PraatConnection.clipWithPraat(mediaFilePath, getSelectionBeginTime() + mediaOffset, getSelectionEndTime() + mediaOffset);
    }
    
    /**
     * Clip a selection with javax.sound facilities.
     */
    private void clipSelectionWithJavaSound() {
    	if (getSelectionBeginTime() == getSelectionEndTime()) {
    		return;
    	} 
    	ClipWaveCommand command = new ClipWaveCommand("ClipWave");
    	command.execute(getViewerManager().getTranscription(), new Object[]{mediaFilePath, 
    		getSelectionBeginTime() + mediaOffset, getSelectionEndTime() + mediaOffset});
    }
    
    /**
     * Shows a formatted media info message.
     */
    private void showMediaInfo() {
    	String[][] info = new String[3][2];
    	
    	info[0][0] = ElanLocale.getString("LinkedFilesDialog.Label.MediaURL");
    	info[0][1] = mediaFilePath;
    	info[1][0] = ElanLocale.getString("LinkedFilesDialog.Label.MediaOffset");
    	info[1][1] = String.valueOf(mediaOffset);
    	info[2][0] = ElanLocale.getString("Player.duration");
    	info[2][1] = TimeFormatter.toString((long)samp.getDuration());
    	
    	new FormattedMessageDlg(info);
    }

    /**
     * Returns duration calculated from WAV-file (used within Corex)
     *
     * @return long
     */
    public long getSignalDuration() {
        return (long) samp.getDuration();
    }

    /**
     * @param selectionColor the new selection color
     */
    public void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
    }

    /**
     * Initializes a <code>WaveSampler</code> for the given URL.
     * <code>WaveSampler</code> currently only takes a String for the source
     * location.
     *
     * @param sourcePath the URL of the source file as a String
     */
    private void initLoad(String sourcePath) {

        samp = null;
        errorKey = null;

        try {
            samp = new WAVSampler(sourcePath);
        } catch (IOException ioe) {
            System.out.println("Failed to create a WAVSampler");
            errorKey = ElanLocale.getString("SignalViewer.Message.NoReader") + ": " + ioe.getMessage();
        }

        if (samp != null) {
            samplesPerPixel = ((msPerPixel * samp.getSampleFrequency()) / 1000);
            maxAmplitude = Math.max(samp.getPossibleMaxSample(),
                    Math.abs(samp.getPossibleMinSample()));

            if (samp.getWavHeader().getNumberOfChannels() == 1) {
                channelMode = MONO;
            } else if (samp.getWavHeader().getNumberOfChannels() == 2) {
                channelMode = STEREO_SEPARATE;
            }
            updateChannelModePopUpMenu();
            
            short compr = samp.getWavHeader().getCompressionCode();
			if (compr != WAVHeader.WAVE_FORMAT_UNCOMPRESSED && 
            		compr != WAVHeader.WAVE_FORMAT_PCM && compr != WAVHeader.WAVE_FORMAT_ALAW) {
            	errorKey = ElanLocale.getString("SignalViewer.Message.Compression") + ": " + 
            			samp.getWavHeader().getCompressionString(compr);
            }
        }

        int w = Toolkit.getDefaultToolkit().getScreenSize().width;

        //start with loading twice the screenwidth into the WavePart
        //loadData(0L, SCREEN_BUFFER * w * msPerPixel, w);
        loadData(0L, (long) msPerPixel, w);
    }

    /**
     * Loads the wave data for the specified interval into the WavePart object.<br>
     * The WaveSampler is requested to read all bytes for the interval in one
     * turn. This method then takes care of the extraction of the values per
     * pixel.
     *
     * @param fromTime the interval starttime in ms, in the viewer's time space (ignores offset)
     * @param toTime the interval stoptime in ms, in the viewer's time space (ignores offset)
     * @param width the width in number of pixels
     *
     * @return true if loading was successful, false otherwise
     *
     * @see WAVSampler#readInterval(int, int)
     */
    private boolean loadData(long fromTime, long toTime, final int width) {
        //long start = System.currentTimeMillis();
        if (samp == null) {
            return false;
        }
        fromTime += mediaOffset;
        toTime += mediaOffset;

        if ((fromTime > samp.getDuration()) || (fromTime > toTime) ||
                (toTime < 0)) {
            return false;
        }

        long to = (toTime > (long) samp.getDuration())
            ? (long) samp.getDuration() : toTime;
        long from = (fromTime < mediaOffset) ? mediaOffset : fromTime;
        int startPixel = (int) (from / msPerPixel);
        int stopPixel = (int) (to / msPerPixel);
        int extent = (int) (stopPixel - startPixel) + 1;// number of pixels to load
        int size = (extent > width) ? extent : width;// the max. of view area width and the number of pixels to load
        
        boolean roundProb = ((samplesPerPixel * 10) % 10) != 0;
        int numChannelsToLoad = 1;
        if (channelMode == STEREO_BLENDED || channelMode == STEREO_SEPARATE) {
        	numChannelsToLoad = 2;
        }
        
        // start loading, previously performed within a "switch" block
        currentPart.reset();
        currentPart.setInterval(from, to, startPixel, size, extent);
        // loading moved to a separate method        
        loadSamples(startPixel, extent, size, numChannelsToLoad, roundProb);
        
        //System.out.println("Load time: " + (System.currentTimeMillis() - start) + " ms");
        return true;
    }
    
    /**
     * Compares the currently loaded interval with the new interval, shifts the remaining part
     * of the interval and loads a bit of additional data without resetting the current WavePart.
     * It is assumed it is already checked whether it makes sense to try to keep most of the loaded
     * data.
     * 
     * @param from time from which to load data, time value in media time space
     * @param to time to which load data, time value in media time space
     * 
     * @return true if loading was successful
     */
    private boolean shiftAndLoadData(long fromTime, long toTime) {
        if (samp == null) {
            return false;
        }
        
        if ((fromTime > samp.getDuration()) || (fromTime > toTime) ||
                (toTime < 0)) {
            return false;
        }
        if (toTime > samp.getDuration()) {
        	toTime = (long) samp.getDuration();
        }
        long oldStop = currentPart.getStopTime();
        long oldStart = currentPart.getStartTime();
        
        int startPixel = (int) (fromTime / msPerPixel);
        int stopPixel = (int) (toTime / msPerPixel);
        int extent = (int) (stopPixel - startPixel) + 1;
        int size = extent;
        
        boolean roundProb = ((samplesPerPixel * 10) % 10) != 0;
        int oldStartPixel = (int) (oldStart / msPerPixel);
        int newStartPixel = startPixel;
        
    	int distance = oldStartPixel - newStartPixel;
    	if (distance == 0) {
    		//System.out.println("Too little distance to reload...");
    		return true;
    	}
    	if (currentPart.rightOverlap(fromTime, toTime)) {
    		currentPart.shiftInterval(distance);

    		currentPart.setInterval(fromTime, toTime, startPixel, 
    				size, extent);
    		startPixel = (int) (oldStop / msPerPixel);
    		extent = (stopPixel - startPixel) + 1;
    		size = extent;
    	} else if (currentPart.leftOverlap(fromTime, toTime)) {
    		currentPart.shiftInterval(distance);

    		currentPart.setInterval(fromTime, toTime, (int) (fromTime / msPerPixel), 
    				size, extent);
    		stopPixel = (int) (oldStart / msPerPixel);
    		extent = (stopPixel - startPixel) + 1;// or without + 1
    		size = extent;
    	} else {
    		return false;
    	}
       
        int numChannelsToLoad = 1;
        if (channelMode == STEREO_BLENDED || channelMode == STEREO_SEPARATE) {
        	numChannelsToLoad = 2;
        }
        // actual loading in separate method, reuse of code
        loadSamples(startPixel, extent, size, numChannelsToLoad, roundProb);
       
		return true;
    }
    
    /**
     * Actual loading of data after necessary preparation of the current WavePart,
     * such as resetting, setting the interval, shifting of data etc.
     * 
     * @param startPixel the pixel corresponding to the first sample
     * @param numberOfPixels the amount of pixels to load (to "fill" based on loaded samples) (extent)
     * @param imageWidthInPixels the number of pixels in the wave part (size)
     * @param numberOfChannels 1 or 2, mono or stereo channels 
     * @param roundingErrors a flag indicating whether or not to take special measures 
     * to deal with rounding effects in conversion from ms per pixel / samples per pixel
     */
    private void loadSamples(int startPixel, int numberOfPixels, 
    		int imageWidthInPixels, int numberOfChannels, boolean roundingErrors) {
    	// the first sample to be read
    	long startSample = (long) (startPixel * samplesPerPixel);
    	int samplesPerPixelInt = (int) samplesPerPixel;
    	int pixelCounter = startPixel;// renamed from startSampleInt to pixelCounter 
    	
    	if (roundingErrors) {
			samp.seekSample(startSample);
			int totalNumSamplesToRead = (int) Math.min(samp.getNrOfSamples(),
        			(int) (numberOfPixels * samplesPerPixel));
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Rounded: Samples per pixel: %d  Number of pixels: %d  Total samples to read: %d",
						samplesPerPixelInt, imageWidthInPixels, totalNumSamplesToRead));
			}
  
			int i = 0;// total count of loaded pixels
			
			while (totalNumSamplesToRead > 0) {//  > samp.getSampleFrequency()or totalNumSamplesToRead > 0
				// Oct. 2016 support for incremental loading of chunks of data in case of large amount 
        		// of required samples. In most cases one file read action is sufficient.
				int nextSamplesToRead = Math.min(totalNumSamplesToRead, MAX_READ_NUM_SAMPLES);
				nextSamplesToRead = Math.max(nextSamplesToRead, samp.getSampleFrequency());// test
            	int samplesRead = samp.readInterval(nextSamplesToRead, numberOfChannels);
            	int numPixelsInSamples = (int) (samplesRead / samplesPerPixel);
            	int cp = 0;//cur pixel in current cache

				for (; i < imageWidthInPixels && cp < numPixelsInSamples; i++, cp++) {
					int sample;
					int sample2;
					int min = 0;
					int max = 0;
	                int min2 = 0;
	                int max2 = 0;
					// calculate start sample index in the read samples for next pixel
					int index = 0;
					if (cp > 0) {
						long sm = (long) ((startPixel + cp) * samplesPerPixel);
						index = (int) (sm - startSample);
					}
					for (int j = 0; j <= samplesPerPixel
							&& (j + index) < samp.getFirstChannelArray().length; j++) {
						sample = samp.getFirstChannelArray()[j + index];

						if (sample < min) {
							min = sample;
						} else if (sample > max) {
							max = sample;
						}
						
						if (numberOfChannels == 2) {
		                    sample2 = samp.getSecondChannelArray()[j + index];
		                	
		                    if (sample2 < min2) {
		                        min2 = sample2;
		                    } else if (sample2 > max2) {
		                        max2 = sample2;
		                    }
						}
					}
					
					currentPart.addLineToFirstChannel(pixelCounter + i, -max, -min);
					if (numberOfChannels == 2) {
						currentPart.addLineToRightChannel(pixelCounter + i, -max2, -min2);
					}
				}
				long samplesInCurPixels = (long) (cp * samplesPerPixel);
                startPixel += cp;
                // adjust the start sample based on actual used pixels/samples
                startSample = (long) (startPixel * samplesPerPixel);
                if (LOG.isLoggable(Level.FINE)) {
                	LOG.fine(String.format("Samples used: %d  Next start sample: %d  Current position: %d", 
                			samplesInCurPixels, startSample, samp.getSamplePointer()));
                }
                // prevent endless loop if no pixel has been added in the for loop
                if (cp == 0) {
                	break;
                }
                totalNumSamplesToRead -= samplesInCurPixels;
                if (totalNumSamplesToRead > 0){
                	// adjust the read marker position based on actual used samples
                	samp.seekSample(startSample);
                }
			}
          
        } else {// no rounding errors in mapping to pixels
        	samp.seekSample(startSample);//firstSeekSample
        	int totalNumSamplesToRead = (int) Math.min(samp.getNrOfSamples(),
        			(int) (numberOfPixels * samplesPerPixel));
        	if (LOG.isLoggable(Level.FINE)) {
        		LOG.fine(String.format("Samples per Pixel: %d  Number of Pixels: %d  Total samples to read: %d",
        				samplesPerPixelInt, imageWidthInPixels, totalNumSamplesToRead));
        	}           
            
        	while (totalNumSamplesToRead > 0) {// > samp.getSampleFrequency() or totalNumSamplesToRead > 0
        		// Oct. 2016 support for incremental loading of chunks of data in case of large amount 
        		// of required samples. In most cases one file read action is sufficient.
            	int nextSamplesToRead = Math.min(totalNumSamplesToRead, MAX_READ_NUM_SAMPLES);
            	nextSamplesToRead = Math.max(nextSamplesToRead, samp.getSampleFrequency());// test
            	int samplesRead = samp.readInterval(nextSamplesToRead, numberOfChannels);
            	
	            // don't read more than the number of samples returned by readInterval
	            // the existence of two int arrays is guaranteed, even if there is no
	            // second channel in the sound file (0 values will be read in that case).
            	int p = pixelCounter;
	            for (int i = 0;
	                    (i < (samplesRead - samplesPerPixelInt)) &&
	                    (p < (pixelCounter + imageWidthInPixels)); i += samplesPerPixelInt, p++) {
	                int sample;
	                int sample2;
	                int min = 0;
	                int max = 0;
	                int min2 = 0;
	                int max2 = 0;
				  
	                for (int j = 0; j < samplesPerPixelInt; j++) {
	                    sample = samp.getFirstChannelArray()[i + j];
	
	                    if (sample < min) {
	                        min = sample;
	                    } else if (sample > max) {
	                        max = sample;
	                    }
	                    
	                    if (numberOfChannels == 2) {
		                    sample2 = samp.getSecondChannelArray()[i + j];
		
		                    if (sample2 < min2) {
		                        min2 = sample2;
		                    } else if (sample2 > max2) {
		                        max2 = sample2;
		                    }
	                    }
	                }
	                
	                currentPart.addLineToFirstChannel(p, -max, -min);
	                if (numberOfChannels == 2) {
	                	currentPart.addLineToRightChannel(p, -max2, -min2);
	                }
	            }
	            long samplesInCurPixels = (long) ((p - pixelCounter) * samplesPerPixel);
	            if (LOG.isLoggable(Level.FINE)) {
	            	LOG.fine(String.format("Samples read: %d  Samples used from current: ", 
	            			samplesRead, samplesInCurPixels));
	            }
	            // prevent endless loop if no pixel has been added in the for loop
	            if (p == pixelCounter) {
	            	break;
	            }
	            pixelCounter = p;
	            long nextStartSample = (long)(pixelCounter * samplesPerPixel);
	            
            	totalNumSamplesToRead -= samplesInCurPixels;
            	if (totalNumSamplesToRead > 0){
	            	// reposition the sample pointer, place it back to the begin of the next start pixel
	            	samp.seekSample(nextStartSample);
	            }
            }
        }

    }

    /**
     * Paint to the BufferedImage. This is necessary in the following situations:<br>
     * - the viewer has been resized<br>
     * - the timeline cursor has (been) moved out of the current interval<br>
     * - the msPerPixel has been changed<br>
     * - the media source has been changed??<br>
     * - the timeline cursor has (been) moved out of the loaded samples<br>
     * - the MONO - STEREO mode has been changed
     */
    private void paintBuffer() {
    	int curWidth = getViewWidth();
    	int curHeight = getViewHeight();
    	
        if ((curWidth <= 0) || (getHeight() <= 0)) {
            return;
        }

        if ((curWidth != imageWidth) || (curHeight != imageHeight)) {
            imageWidth = curWidth;
            imageHeight = curHeight;

            if (intervalEndTime == 0) {
                intervalEndTime = intervalBeginTime +
                    (int) (imageWidth * msPerPixel);

                if (timeScaleConnected) {
                    setGlobalTimeScaleIntervalEndTime(intervalEndTime);
                }
            }
        }
        // make sure imageWidth and imageHeight are always calculated
    	if (!useBufferedImage) {
    		repaint();
    		return;
    	}
    	
        //synchronized (paintlock) {
    	paintLock.lock();
    	try {
            if ((bi == null) || (bi.getWidth() < imageWidth) ||
                    (bi.getHeight() < imageHeight)) {
                bi = new BufferedImage(imageWidth, imageHeight,
                        BufferedImage.TYPE_INT_RGB);
                big2d = bi.createGraphics();
            }
        //}
        
            if (SystemReporting.antiAliasedText) {
	            big2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            
            paintToConfiguredContext(big2d, imageWidth, imageHeight);
            big2d.setTransform(identityTransform);
    	    if (timeRulerVisible) {
    	    	big2d.setColor(Constants.SELECTIONCOLOR);
    	    	big2d.drawLine(0, rulerHeight, imageWidth, rulerHeight);
    	    }
    	} finally {
    		paintLock.unlock();
    	}

        repaint();

    }



    //  ALBERT
    /**
     * Draws segmentation
     */
    private void drawSegmentation(Graphics2D big2g, int imageTopLeft, int imageBottomLeft, 
    		int imageTopRight, int imageBottomRight, int translateX) {
        // imageWidth, msPerPixel. intervalBeginTime, intervalEndTime are supposed to be correct as a side effect from drawImage
    	//AffineTransform aft = big2g.getTransform();
    	int mediaOffsetX = (int) (mediaOffset / msPerPixel);
        if (segmentationChannel1 != null && showSegmentationChannel1) {
        	java.awt.font.FontRenderContext frc = big2g.getFontRenderContext();
        	// set explicit label font?
	        for (int i = mediaOffsetX; i < imageWidth + mediaOffsetX; i++) {
	        	long from = intervalBeginTime + (int) (i * msPerPixel);
	        	long to = (long) (from + msPerPixel);
	        	Boundary boundary = segmentationChannel1.boundaryBetween(from, to);
	        	if (boundary == null) {
	        		continue;
	        	}
	        	String label = null;
	        	if (boundary != null) {
	        		label = boundary.label;
	        	}
	        	int x = i + translateX;
	        	if (label != null) {
	        		big2g.setColor(Constants.SEGMENTATIONCOLOR);
	        		big2g.drawLine(x, imageTopLeft, x, imageBottomLeft); 
	        		if (!label.equals("")) {
		                java.awt.geom.Rectangle2D rect = big2g.getFont().getStringBounds(label, frc);
		                big2g.setColor(Color.white);
		                big2g.fill3DRect(x + 3, imageBottomLeft - (int)rect.getHeight() - 2, (int)rect.getWidth() + 6, (int)rect.getHeight() + 2, true);
		                big2g.setColor(Color.black); 
		                big2g.drawString(label, x + 5, imageBottomLeft - 3);
	        		}
	        	}
	        }
        }
        if (segmentationChannel2 != null && showSegmentationChannel2) {
        	java.awt.font.FontRenderContext frc = big2g.getFontRenderContext();
        	// set explicit label font?
	        for (int i = 0; i < imageWidth; i++) {
	        	long from = intervalBeginTime + (int) (i * msPerPixel);
	        	long to = (long) (from + msPerPixel);
	        	Boundary boundary = segmentationChannel2.boundaryBetween(from, to);
	        	String label = null;
	        	if (boundary != null) {
	        		label = boundary.label;
	        	}
	        	int x = i + translateX;
	        	if (label != null) {
	        		big2g.setColor(Constants.SEGMENTATIONCOLOR);
	        		big2g.drawLine(x, imageTopRight, x, imageBottomRight); 
	        		if (!label.equals("")) {
		                java.awt.geom.Rectangle2D rect = big2g.getFont().getStringBounds(label, frc);
		                big2g.setColor(Color.white);
		                big2g.fill3DRect(x + 3, imageBottomRight - (int)rect.getHeight() - 2, (int)rect.getWidth() + 6, (int)rect.getHeight() + 2, true);
		                big2g.setColor(Color.black); 
		                big2g.drawString(label, x + 5, imageBottomRight - 3);
	        		}
	        	}
	        }
        }
    }

    /**
     * The left segmentation is also used for mono signals
     */
    public void setSegmentation(BoundarySegmentation segmentation) {
    	segmentationChannel1 = segmentation;
    	if (segmentation != null) {
        	segmentationChannel2 = null;
			segmentationChannel1Item.setSelected(true);
	    	showSegmentationChannel1 = true;
    	} else {
			segmentationChannel1Item.setSelected(false);
	    	showSegmentationChannel1 = false;
    	}
    	enableSegmentationMenu();
    	paintBuffer();
    }

    public void setSegmentationChannel1(BoundarySegmentation segmentation) {
    	segmentationChannel1 = segmentation;
    	if (segmentationChannel1 != null) {
			segmentationChannel1Item.setSelected(true);
			showSegmentationChannel1 = true;
    	} else {
			segmentationChannel1Item.setSelected(false);
			showSegmentationChannel1 = false;
    	}
    	enableSegmentationMenu();
    	paintBuffer();
    }
    
    public void setSegmentationChannel2(BoundarySegmentation segmentation) {
    	segmentationChannel2 = segmentation;
    	if (segmentationChannel2 != null && segmentationChannel2Item != null) {
			segmentationChannel2Item.setSelected(true);
			showSegmentationChannel2 = true;
    	} else if (segmentationChannel2Item != null) {
			segmentationChannel2Item.setSelected(false);
			showSegmentationChannel2 = false;
    	}
    	enableSegmentationMenu();
    	paintBuffer();
    }
    
    public void enableSegmentationMenu() {
    	if (segmentationChannel1 != null) {
    		segmentationMenu.setEnabled(true);
    		segmentationChannel1Item.setEnabled(true);
    	} else {
    		segmentationChannel1Item.setEnabled(false);
    	}
    	if (segmentationChannel2 != null) {
    		segmentationMenu.setEnabled(true);
    		segmentationChannel2Item.setEnabled(true);
    	} else if (getChannelMode() != MONO) {
    		segmentationChannel2Item.setEnabled(false);
    	}
    	if (segmentationChannel1 == null && segmentationChannel2 == null) {
    		segmentationMenu.setEnabled(false);
    	}
    }
    // END ALBERT
    

    /**
     * Draws cue points if there are present in wav file
     *
     * @param big2g
     */
    private void drawCuePoints(Graphics2D big2g) {
    	if (samp == null) {
    		return;
		}
		
        WAVCuePoint[] cuePoints = samp.getWavHeader().getCuePoints();

        if (cuePoints.length > 0) {
            big2g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, new float[] { 4.0f }, 0.0f));

            big2g.setColor(Color.darkGray);

            for (int i = 0; i < cuePoints.length; i++) {
                int time = (int) samp.getTimeAtSample(cuePoints[i].getSampleOffset());
                int x = (int) (time / msPerPixel);

                if ((intervalBeginTime <= time) && (time < intervalEndTime)) {
                    big2g.drawLine(x, rulerHeight, x, imageHeight);

                    String label = cuePoints[i].getLabel();
                    String note = cuePoints[i].getNote();

                    if ((label != null) && (note != null)) {
                        label += " : ";
                    }

                    big2g.drawString(((label != null) ? label
                                                      : Integer.toString(i)) +
                        ((note != null) ? note : ""), x + 1, imageHeight - 1);
                }
            }

            big2g.setStroke(new BasicStroke());
        }
    }

	/**
     * Paints the panel directly, without the use of a BufferedImage.
     * 
     * @param g2d the graphics context of the component
     */
    private void paintUnbuffered(Graphics2D g2d) {
        if ((getViewWidth() <= 0) || (getViewHeight() <= 0)) {
            return;
        }

        paintToConfiguredContext(g2d, getViewWidth(), getViewHeight());

        int intervalX1 = viewPortMode ? 0 : (int) (intervalBeginTime / msPerPixel);
	    if (timeRulerVisible) {
	    	g2d.setColor(Constants.SELECTIONCOLOR);
	    	g2d.drawLine(intervalX1, rulerHeight, intervalX1 + getViewWidth(), rulerHeight);
	    }
        //paint selection
        if (selectionBeginPos != selectionEndPos) {
        	g2d.setColor(selectionColor);
        	g2d.setComposite(alpha04);
        	g2d.fillRect(selectionBeginPos + intervalX1, 0,
                (selectionEndPos - selectionBeginPos), rulerHeight);
        	g2d.setComposite(alpha07);
        	g2d.fillRect(selectionBeginPos + intervalX1, rulerHeight,
                (selectionEndPos - selectionBeginPos), getViewHeight() - rulerHeight);
        	g2d.setComposite(AlphaComposite.Src);
        }

        //draw the cursor
        g2d.setColor(Constants.CROSSHAIRCOLOR);
        g2d.drawLine(crossHairPos + intervalX1, 0, crossHairPos + intervalX1, getViewHeight());
    }
    
    /**
     * The actual painting of the labels etc. to either the graphics context of an image
     * or to that of a component (unbuffered).
     * 
     * @param g2d the graphic context
     */
    private void paintToConfiguredContext(Graphics2D conG2d, int contextWidth, int contextHeight) {
    	int scrollViewBX = 0;
    	if (viewPortMode && !useBufferedImage) {
    		scrollViewBX = (int) (intervalBeginTime / msPerPixel);
    	}
    	if (samp == null) {
            //paint the background color
            conG2d.setComposite(AlphaComposite.Src);
            conG2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
            conG2d.fillRect(scrollViewBX, 0, contextWidth, contextHeight);

            return;
    	}
    	//check whether we have to load a new interval
        // HS March 2011 include media offset in comparison!
        long mediaBT = intervalBeginTime + mediaOffset;
        long mediaET = intervalEndTime + mediaOffset;
        if (mediaET > samp.getDuration()) {
        	mediaET = (long) samp.getDuration();
        }
        
        if (!currentPart.contains(mediaBT, mediaET)) {
//        	System.out.println("Overlap: " + currentPart.amountOfOverlap(mediaBT, mediaET));
//        	System.out.println("Left: " + currentPart.leftOverlap(mediaBT, mediaET));
//        	System.out.println("Right: " + currentPart.rightOverlap(mediaBT, mediaET));
//        	System.out.println("Same length: " + currentPart.sameIntervalLength(mediaBT, mediaET));
            boolean loaded = false;
//        	long begin = System.currentTimeMillis();
        	
        	if (currentPart.amountOfOverlap(mediaBT, mediaET) > 0.8f && 
            		currentPart.sameIntervalLength(mediaBT, mediaET)) {
            	// shift and load additional
        		loaded = shiftAndLoadData(mediaBT, mediaET);
            }
            
        	if (!loaded) {
            	loaded = loadData(intervalBeginTime,
                        intervalBeginTime +
                        (int) (SCREEN_BUFFER * contextWidth * msPerPixel), contextWidth);
        	}
//        	System.out.println("Loadtime: " + (System.currentTimeMillis() - begin));
//        	System.out.println("Wave part BT: " + currentPart.getStartTime());
//        	System.out.println("Wave part ET: " + currentPart.getStopTime());
            // loadData only returns false in case of a severe error

            /*
               if (!loaded) {
                   //return;
                   currentPart.reset();
                   currentPart.setStartTime(0L);
                   currentPart.setStopTime(0L);
               }
             */
        }

        int channelHeight;
        int intervalX1 = (int) (intervalBeginTime / msPerPixel);
        int segmentationX1 = intervalX1;
        //int intervalX2 = (int)(intervalEndTime / msPerPixel);
        // since there can be a media offset (i.e. the global begin time and the media begin
        // time are not the same) we need different translations for the ruler etc
        // and the wave part(s)
        int waveIntervalX1 = (int) ((intervalBeginTime + mediaOffset) / msPerPixel);
        //int waveIntervalX2 = (int) ((intervalEndTime + mediaOffset) / msPerPixel);
        int waveIntervalX2 = (int) (mediaET / msPerPixel);
        int mediaOffsetX = (int) (mediaOffset / msPerPixel);
        if (viewPortMode && !useBufferedImage) {
        	intervalX1 = 0;
        	waveIntervalX1 = mediaOffsetX;
        }
        AffineTransform at = new AffineTransform();

        //paint the background color
        conG2d.setComposite(AlphaComposite.Src);
        conG2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        conG2d.fillRect(scrollViewBX, 0, contextWidth, contextHeight);

        // mark the area beyond the media time
        if (intervalEndTime > getMediaDuration()) {
            int xx = xAt(getMediaDuration());
            if (!SystemReporting.isMacOS()) {
            	conG2d.setColor(UIManager.getColor("Panel.background"));// problems on the mac
            } else {
            	conG2d.setColor(Color.LIGHT_GRAY);
            }
            conG2d.fillRect(xx, 0, contextWidth - xx, contextHeight);
        }

        
        /*paint time ruler */
        if (timeRulerVisible) {
            conG2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
            conG2d.translate(-intervalX1, 0.0);
            ruler.paint(conG2d, intervalBeginTime, contextWidth, msPerPixel,
                SwingConstants.TOP);
            conG2d.translate(intervalX1 - waveIntervalX1, 0.0);
        } else {
        	conG2d.translate(-waveIntervalX1, 0.0);
        }
        
        ///end ruler
        // paint the wave(s)
        switch (channelMode) {
        case MONO:

        //fallthrough
        case STEREO_MERGED:

            //one merged channel for the wave stored in WavePart.firstPath
            channelHeight = contextHeight - rulerHeight;

            int channelMid = rulerHeight + Math.round(channelHeight / 2f);
            conG2d.translate(0.0, channelMid);
            conG2d.setColor(Color.DARK_GRAY);
            conG2d.drawLine(waveIntervalX1, 0, waveIntervalX2, 0);
            conG2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
            at.setToScale(1.0, ((((float) channelHeight) / maxAmplitude) / 2) * (vertZoom / 100f));

            /* Graphics2D.draw(Shape.createTransformedShape(AffineTransform)) seems to be faster */

            //conG2d.transform(at);
            //conG2d.draw(currentPart.getFirstPath().createTransformedShape(at));
            currentPart.paintLeftChannelLimit(conG2d, at, channelHeight / 2);
            conG2d.translate(0.0, -channelMid);

            // ALBERT
            int leftTop = rulerHeight;
            int rightTop = rulerHeight;
            //int channelHeight = (rulerHeight + imageHeight) / 2;
            drawSegmentation(conG2d, leftTop, leftTop + channelHeight, rightTop, 
            		rightTop + channelHeight, segmentationX1);
            // END ALBERT
            
            break;

        case STEREO_SEPARATE:
            channelHeight = (contextHeight - rulerHeight - GAP) / 2;

            int leftChannelMid = rulerHeight +
                (int) (Math.ceil(channelHeight / 2f));
            int rightChannelMid = contextHeight -
                (int) (Math.ceil(channelHeight / 2f));

            // decoration
            conG2d.setColor(Constants.SIGNALCHANNELCOLOR);
            conG2d.fillRect(waveIntervalX1, rulerHeight, contextWidth + 2,
                channelHeight);
            conG2d.fillRect(waveIntervalX1, contextHeight - channelHeight,
            		contextWidth + 2, channelHeight);
            conG2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);

            //
            conG2d.translate(0.0, leftChannelMid);
            conG2d.setColor(Color.DARK_GRAY);
            conG2d.drawLine(waveIntervalX1, 0, waveIntervalX2, 0);
            conG2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
            at.setToScale(1.0, ((((float) channelHeight) / maxAmplitude) / 2) * (vertZoom / 100f));

            //conG2d.draw(currentPart.getFirstPath().createTransformedShape(at));//expensive??
            // conG2d.setClip(0, -leftChannelMid + rulerHeight, imageWidth + 2, channelHeight);
            currentPart.paintLeftChannelLimit(conG2d, at, channelHeight / 2);
            // conG2d.setClip(null);
            conG2d.translate(0.0, rightChannelMid - leftChannelMid);
            conG2d.setColor(Color.DARK_GRAY);
            conG2d.drawLine(waveIntervalX1, 0, waveIntervalX2, 0);
            conG2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);

            //conG2d.draw(currentPart.getSecondPath().createTransformedShape(at));
            // conG2d.setClip(0, -leftChannelMid + rulerHeight, imageWidth + 2, channelHeight);
            currentPart.paintRightChannelLimit(conG2d, at, channelHeight / 2);

            conG2d.translate(0.0, -rightChannelMid);
            // conG2d.setClip(null);
         
            // ALBERT
            leftTop = rulerHeight;
            rightTop = leftTop + channelHeight + GAP;
            //int channelHeight = (rulerHeight + imageHeight) / 2;
            drawSegmentation(conG2d, leftTop, leftTop + channelHeight, rightTop, 
            		rightTop + channelHeight, segmentationX1);
            // END ALBERT
            
            break;

        case STEREO_BLENDED:
            channelHeight = contextHeight - rulerHeight;

            int chMid = rulerHeight + Math.round(channelHeight / 2f);
            conG2d.translate(0.0, chMid);
            conG2d.drawLine(waveIntervalX1, 0, waveIntervalX2, 0);
            at.setToScale(1.0, ((((float) channelHeight) / maxAmplitude) / 2) * (vertZoom / 100f));
            conG2d.setColor(Constants.SIGNALSTEREOBLENDEDCOLOR1);

            //conG2d.draw(currentPart.getFirstPath().createTransformedShape(at));
            currentPart.paintLeftChannelLimit(conG2d, at, channelHeight / 2);

            conG2d.setColor(Constants.SIGNALSTEREOBLENDEDCOLOR2);
            conG2d.setComposite(alpha04);

            //conG2d.draw(currentPart.getSecondPath().createTransformedShape(at));
            currentPart.paintRightChannelLimit(conG2d, at, channelHeight / 2);
            conG2d.setComposite(AlphaComposite.Src);
            conG2d.translate(0.0, -chMid);

//          ALBERT
            leftTop = rulerHeight;
            rightTop = rulerHeight;
            //int channelHeight = (rulerHeight + imageHeight) / 2;
            drawSegmentation(conG2d, leftTop, leftTop + channelHeight, rightTop, 
            		rightTop + channelHeight, segmentationX1);
            // END ALBERT
            
            break;

        default:
            ;
        }
        
        drawCuePoints(conG2d);
        
        conG2d.translate(mediaOffsetX, 0.0);
    }

    /**
     * Override <code>JComponent</code>'s paintComponent to paint:<br>
     * - a BufferedImage with a ruler and the waveform<br>
     * - the current selection<br>
     * - the cursor / crosshair
     *
     * @param g the graphics object
     */
    @Override
	public void paintComponent(Graphics g) {
    	try {
    		if (!paintLock.tryLock(paintLockTimeOut, TimeUnit.MILLISECONDS)){
    			//System.out.println("SV: Image Buffer locked, no repaint.");
    			return;
    		}
    	} catch (InterruptedException ie) {
    		// ignore and let repainting happen
    	}
    	
    	try {//test
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        if (SystemReporting.antiAliasedText) {
	        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        int h = getViewHeight();

        if (useBufferedImage && bi != null) {
            //synchronized (paintlock) {
        	int scrollViewBX = 0;
        	if (viewPortMode) {
        		scrollViewBX = (int) (intervalBeginTime / msPerPixel);
        	}
            g2d.drawImage(bi, scrollViewBX, 0, this);

            int intervalX1 = (int) (mediaOffset / msPerPixel);
            intervalX1 = 0;
            //paint selection
            if (selectionBeginPos != selectionEndPos) {
                g2d.setColor(selectionColor);
                g2d.setComposite(alpha04);
                g2d.fillRect(selectionBeginPos - intervalX1, 0,
                    (selectionEndPos - selectionBeginPos), rulerHeight);
                g2d.setComposite(alpha07);
                g2d.fillRect(selectionBeginPos - intervalX1, rulerHeight,
                    (selectionEndPos - selectionBeginPos), h - rulerHeight);
                g2d.setComposite(AlphaComposite.Src);
            }

            //draw the cursor
            g2d.setColor(Constants.CROSSHAIRCOLOR);
            g2d.drawLine(crossHairPos - intervalX1, 0, crossHairPos - intervalX1, h);
            
            //}		
        } else {
        	paintUnbuffered(g2d);
        	g2d.setTransform(identityTransform);
        }

        if (errorKey != null) {
        	g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
        	g2d.drawString(errorKey, 10, 20 + rulerHeight);
        }
    	} finally {// test
    		paintLock.unlock();
    	}
    }

    /**
     * Implements updateTimeScale from TimeScaleBasedViewer to adjust the
     * TimeScale if needed and when in TimeScale connected mode.<br>
     * Checks the GlobalTimeScaleIntervalBeginTime and
     * GlobalTimeScaleMsPerPixel and adjusts the interval and resolution of
     * this viewer when they differ from the global values.<br>
     * For the time being assume that the viewer is notified only once when
     * the resolution or the interval begintime has changed.
     * <p>
     * This is synchronized to prevent interference with setMedia(), since
     * it is called in a separate thread from a ControllerUpdate.
     */
    @Override
	synchronized public void updateTimeScale() {
        if (timeScaleConnected) {
            // if the resolution is changed recalculate the begin time
            if (getGlobalTimeScaleMsPerPixel() != msPerPixel) {
                setLocalTimeScaleMsPerPixel(getGlobalTimeScaleMsPerPixel());
            } else if (getGlobalTimeScaleIntervalBeginTime() != intervalBeginTime) {
                //assume the resolution has not been changed
                setLocalTimeScaleIntervalBeginTime(getGlobalTimeScaleIntervalBeginTime());

                //System.out.println("update begin time in SignalViewer called");
            }
        }
    }

    /**
     * Sets whether or not this viewer listens to global time scale updates.
     *
     * @param connected the new timescale connected value
     */
    public void setTimeScaleConnected(boolean connected) {
        timeScaleConnected = connected;

        if (timeScaleConnected) {
            if (msPerPixel != getGlobalTimeScaleMsPerPixel()) {
                setLocalTimeScaleMsPerPixel(getGlobalTimeScaleMsPerPixel());
            }

            if (intervalBeginTime != getGlobalTimeScaleIntervalBeginTime()) {
                setLocalTimeScaleIntervalBeginTime(getGlobalTimeScaleIntervalBeginTime());
            }
        }
    }

    /**
     * Gets whether this viewer listens to time scale updates from other
     * viewers.
     *
     * @return true when connected to global time scale values, false otherwise
     */
    public boolean getTimeScaleConnected() {
        return timeScaleConnected;
    }

    /**
     * Checks whether this viewer is TimeScale connected and changes the
     * milliseconds per pixel value globally or locally.<br>
     * On setting the global MsPerPixel {@link #updateTimeScale()} will be
     * called and the local msPerPixel value will be updated also.
     *
     * @param mspp the new milliseconds per pixel value
     */
    public void setMsPerPixel(float mspp) {
        if (timeScaleConnected) {
            setGlobalTimeScaleMsPerPixel(mspp);
            setGlobalTimeScaleIntervalBeginTime(intervalBeginTime);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        } else {
            setLocalTimeScaleMsPerPixel(mspp);
        }
    }

    /**
     * Change the horizontal resolution or zoomlevel locally. The msPerPixel
     * denotes the number of milliseconds of which the samples should be
     * merged to one value. It corresponds to one pixel in image space (a
     * pixel is the smallest unit in image space).<br>
     * The position on the screen of crosshair cursor should change as little
     * as possible.<br>
     * This is calculated as follows:<br>
     * The absolute x coordinate in image space is the current media time
     * divided by the new msPerPixel.<br>
     * <pre>
     * |----------|----------|-------x--|-- <br>
     * |imagesize |                  | absolute x coordinate of media time<br>
     * |    1     |    2     |    3     |
     * </pre>
     * Calculate the number of screen images that fit within the absolute x
     * coordinate. The new position on the screen would then be the absolute x
     * coordinate minus the number of screen images multiplied by the image
     * width. The difference between the old x value and the new x value is
     * then used to calculate the new interval start time.<br>
     * The new start time = (number of screen images  image width -
     * difference)  msPerPixel.
     *
     * @param step the new horizontal zoomlevel
     */
    private void setLocalTimeScaleMsPerPixel(float step) {
        if (msPerPixel == step) {
            return;
        }
        if (step >= TimeScaleBasedViewer.MIN_MSPP) {
            msPerPixel = step;
        } else {
            msPerPixel = TimeScaleBasedViewer.MIN_MSPP;
        }

        if (currentPart != null) {
        	currentPart.setDrawExtremesContour(msPerPixel <= 0.5f);
        }
        
        //resolution = (int) (1000f / msPerPixel);

        /*stop the player if necessary*/
        boolean playing = playerIsPlaying();

        if (playing) {
            stopPlayer();
        }

        long mediaTime = getMediaTime();
        int oldScreenPos = crossHairPos;
        int newMediaX = (int) (mediaTime / msPerPixel);
        int numScreens = (int) (mediaTime / (imageWidth * msPerPixel));
        int newScreenPos = newMediaX - (numScreens * imageWidth);
        int diff = oldScreenPos - newScreenPos;

        //new values
        intervalBeginTime = (long) (((numScreens * imageWidth) - diff) * msPerPixel);

        if (intervalBeginTime < 0) {
            intervalBeginTime = 0;
        }

        intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);
        crossHairPos = xAt(mediaTime);
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());
        
        if (viewPortMode) {
        	revalidate();// make sure the preferred size is updated
        	//viewPort.removeChangeListener(viewPortListener);
        	viewPortListener.setEnabled(false);
        	scrollRectToVisible(new Rectangle(xAt(intervalBeginTime) ,0, getViewWidth(), getViewHeight()));
        	//viewPort.addChangeListener(viewPortListener);
        	viewPortListener.setEnabled(true);
        }
        
        if (samp != null) {
            samplesPerPixel = ((msPerPixel * samp.getSampleFrequency()) / 1000);
            //System.out.println("Samples per pixel int: " + samplesPerPixel);
            //System.out.println("Samples per pixel float: " + ((msPerPixel * samp.getSampleFrequency()) / 1000));
        }

        //force to reload data
        currentPart.setStartTime(0L);
        currentPart.setStopTime(0L);
        paintBuffer();

        if (playing) {
            startPlayer();
        }
        float zoomFl = 100f * (10f / msPerPixel);
//        int zoom = (int) (100f * (10f / msPerPixel));

//        if (zoom <= 0) {
//            zoom = 100;
//        }
        
        updateZoomPopup(zoomFl);
        
        if (getViewerManager() != null) {
        	setPreference("SignalViewer.ZoomLevel", new Float(zoomFl), 
        		getViewerManager().getTranscription());
        }

        //repaint();
    }

    /**
     * Returns the current msPerPixel.
     *
     * @return msPerPixel
     */
    public float getMsPerPixel() {
        return msPerPixel;
    }

    /**
     * Calls #setMsPerPixel with the appropriate value. In setMsPerPixel the
     * value of this.resolution is actually set. msPerPixel = 1000 / resolution<br>
     * resolution = 1000 / msPerPixel
     *
     * @param resolution the new resolution
     */
    /*
    public void setResolution(int resolution) {
        this.resolution = resolution;

        int mspp = (int) (1000f / resolution);
        setMsPerPixel(mspp);
    }
	*/
    /**
     * Sets the resolution by providing a factor the default PIXELS_FOR_SECOND
     * should be multiplied with.<br>
     * resolution = factor  PIXELS_FOR_SECOND.<br>
     * <b>Note:</b><br>
     * The factor = 100 / resolution_menu_percentage !
     *
     * @param factor the multiplication factor
     */
    /*
    public void setResolutionFactor(float factor) {
        int res = (int) (PIXELS_FOR_SECOND * factor);
        setResolution(res);
    }
	*/
    /**
     * Gets the current resolution
     *
     * @return the current resolution
     */
    /*
    public int getResolution() {
        return resolution;
    }
    */

    /**
     * Gets the current channel display mode.
     *
     * @return the current channel mode, one of MONO, STEREO_SEPARATE,
     *         STEREO_MERGED or STEREO_BLENDED
     */
    public int getChannelMode() {
        return channelMode;
    }

    /**
     * Sets the channel display mode.
     *
     * @param mode the new channel mode
     */
    public void setChannelMode(int mode, boolean storePref) {
        if (mode == channelMode) {
            return;
        }

        if ((mode <= MONO) || (mode > STEREO_BLENDED)) {
            channelMode = MONO;
        } else {
            if ((samp != null) &&
                    (samp.getWavHeader().getNumberOfChannels() != 2)) {
            	channelMode = MONO;
                return;
            }

            channelMode = mode;
        }
        if (storePref) {
	        setPreference("SignalViewer.StereoMode", Integer.valueOf(channelMode), 
	        		getViewerManager().getTranscription());
        }
        paintBuffer();
    }

    
    /**
	 * @return the timeRulerVisible
	 */
	public boolean isTimeRulerVisible() {
		return timeRulerVisible;
	}


	/**
	 * Sets the visibility of the time ruler. Preferences are not updated.
	 * 
	 * @param timeRulerVisible the timeRulerVisible to set
	 */
	public void setTimeRulerVisible(boolean timeRulerVisible) {
		this.timeRulerVisible = timeRulerVisible;
		if (timeRulerVisMI != null && timeRulerVisMI.isSelected() != timeRulerVisible) {
			timeRulerVisMI.setSelected(timeRulerVisible);
		}

		if (timeRulerVisible) {
			rulerHeight = ruler.getHeight();
		} else {
			rulerHeight = 0;
		}
		paintBuffer();
	}


	/**
     * Returns the x-coordinate for a specific time. The coordinate is in the
     * component's coordinate system.
     *
     * @param t time
     *
     * @return int the x-coordinate for the specified time
     */
    public int xAt(long t) {
    	if (!viewPortMode) {
        	return (int) ((t - intervalBeginTime) / msPerPixel);
    	} else {
    		return (int) (t / msPerPixel);
    	}
    }

	/**
     * Returns the x-coordinate for a specific time, a time in the media's time space, 
     * i.e. including the media offset. 
     * The coordinate is in the component's coordinate system.
     *
     * @param t time
     *
     * @return int the x-coordinate for the specified time
     */
    public int xAtMediaTime(long t) {
    	if (!viewPortMode) {
    		return (int) (((t + mediaOffset) / msPerPixel) - (intervalBeginTime / msPerPixel));
    	} else {
    		return (int) (((t + mediaOffset) / msPerPixel));
    	}
    }
    
    /**
     * Returns the time in ms at a given position in the current image. The
     * given x coordinate is in the component's ("this") coordinate system.
     * The interval begin time is included in the calculation of the time at
     * the given coordinate.
     *
     * @param x x-coordinate
     *
     * @return the mediatime corresponding to the specified position
     */
    public long timeAt(int x) {
    	if (!viewPortMode) {
    		return intervalBeginTime + (long) (x * msPerPixel);
    	} else {
    		return (long) (x * msPerPixel);
    	}
    }

    /**
     * Returns the time in ms, corrected for the media offset, at a given position in the current image. 
     * The given x coordinate is in the component's ("this") coordinate system.
     * The interval begin time is included in the calculation of the time at
     * the given coordinate.
     *
     * @param x x-coordinate
     *
     * @return the mediatime corresponding to the specified position, corrected for the media offset
     */
    public long mediaTimeAt(int x) {
    	if (!viewPortMode) {
    		return intervalBeginTime + mediaOffset + (long) (x * msPerPixel);
    	} else {
    		return (long) (x * msPerPixel) + mediaOffset;
    	}
    }
    
    /**
     * @return the current interval begin time
     */
    public long getIntervalBeginTime() {
        return intervalBeginTime;
    }

    /**
     * @return the current interval end time
     */
    public long getIntervalEndTime() {
        return intervalEndTime;
    }

    /**
     * Checks whether this viewer is TimeScale connected and changes the
     * interval begin time globally or locally.<br>
     * On setting the global interval begin time {@link #updateTimeScale()}
     * will be called and the local intervalBeginTime will be updated also.
     *
     * @param begin the new interval begin time
     */
    public void setIntervalBeginTime(long begin) {
        if (timeScaleConnected) {
            setGlobalTimeScaleIntervalBeginTime(begin);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        } else {
            setLocalTimeScaleIntervalBeginTime(begin);
        }
    }
    
    public void setRecalculateInterval(boolean recalculate){
    	recalculateInterval = recalculate;
    }

    /**
     * Calculates the new interval begin and/or end time.<br>
     * There are two special cases taken into account:<br>
     * 
     * <ul>
     * <li>
     * when the player is playing attempts are made to shift the interval
     * <i>n</i> times the interval size to the left or to the right, until the
     * new interval contains the new mediatime.
     * </li>
     * <li>
     * when the player is not playing and the new interval begin time coincides
     * with the selection begin time, the interval is shifted a certain offset
     * away from the image edge. Same thing when the interval end time
     * coincides with the selection end time.
     * </li>
     * </ul>
     * 
     *
     * @param mediaTime
     */
    private void recalculateInterval(final long mediaTime) {    	
    	if(!recalculateInterval){
    		return;
    	}
        long newBeginTime = intervalBeginTime;
        long newEndTime = intervalEndTime;

        if (playerIsPlaying()) {
            // we might be in a selection outside the new interval
            // shift the interval n * intervalsize to the left or right
            if (mediaTime > intervalEndTime) {
                newBeginTime = intervalEndTime;
                newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);

                while ((newEndTime += (imageWidth + msPerPixel)) < mediaTime) {
                    newBeginTime += (imageWidth * msPerPixel);
                }
            } else if (mediaTime < intervalBeginTime) {
                newEndTime = intervalBeginTime;
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                while ((newEndTime -= (imageWidth * msPerPixel)) > mediaTime) {
                    newBeginTime -= (imageWidth * msPerPixel);
                }

                if (newBeginTime < 0) {
                    newBeginTime = 0;
                    newEndTime = (long) (imageWidth * msPerPixel);
                }
            } else {
                // the new time appears to be in the current interval after all
                return;
            }
        } else { //player is not playing

            // is the new media time to the left or to the right of the current interval
            if (mediaTime < intervalBeginTime) {
                newBeginTime = mediaTime - (int) (SCROLL_OFFSET * msPerPixel);

                if (newBeginTime < 0) {
                    newBeginTime = 0;
                }

                newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
            } else if (mediaTime > intervalEndTime){
                newEndTime = mediaTime + (int) (SCROLL_OFFSET * msPerPixel);
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                if (newBeginTime < 0) { // something would be wrong??
                    newBeginTime = 0;
                    newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
                }
            }

            if ((newBeginTime == getSelectionBeginTime()) &&
                    (newBeginTime > (SCROLL_OFFSET * msPerPixel))) {
                newBeginTime -= (SCROLL_OFFSET * msPerPixel);
                newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
            }

            if (newEndTime == getSelectionEndTime()) {
                newEndTime += (SCROLL_OFFSET * msPerPixel);
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                if (newBeginTime < 0) { // something would be wrong??
                    newBeginTime = 0;
                    newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
                }
            }
            
        }

        if (timeScaleConnected) {
            //System.out.println("SV new begin time: " + newBeginTime);
            //System.out.println("SV new end time: " + newEndTime);
            setGlobalTimeScaleIntervalBeginTime(newBeginTime);
            setGlobalTimeScaleIntervalEndTime(newEndTime);
        } else {
            setLocalTimeScaleIntervalBeginTime(newBeginTime);
        }
    }

    /**
     * Changes the interval begin time locally.
     *
     * @param begin the new local interval begin time
     */
    private void setLocalTimeScaleIntervalBeginTime(long begin) {
        if (begin == intervalBeginTime) {
            return;
        }
        paintLock.lock();
        try {
        intervalBeginTime = begin;
        intervalEndTime = intervalBeginTime + (int) (getViewWidth() * msPerPixel);

        crossHairPos = xAt(crossHairTime);
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());

        if (viewPortMode) {
        	scrollRectToVisible(new Rectangle(xAt(intervalBeginTime), 0, getViewWidth(), getViewHeight()));
        }
        } finally {
        	paintLock.unlock();
        }
        //System.out.println("SV begin:" + intervalBeginTime);
        paintBuffer();
    }

    /**
     * Create a popup menu to enable the manipulation of some settings for this
     * viewer.
     */
    private void createPopupMenu() {
        popup = new JPopupMenu("Signal Viewer");

        JMenu zoomMI = new JMenu(ElanLocale.getString("TimeScaleBasedViewer.Zoom"));
        zoomBG = new ButtonGroup();
        zoomSelectionMI = new JMenuItem(
        		ElanLocale.getString("TimeScaleBasedViewer.Zoom.Selection"));
        zoomSelectionMI.addActionListener(this);
        zoomMI.add(zoomSelectionMI);
        // add zoom to show entire media file item
        zoomToEntireMediaMI = new JMenuItem(
        		ElanLocale.getString("TimeScaleBasedViewer.Zoom.EntireMedia"));
        zoomToEntireMediaMI.addActionListener(this);
        zoomMI.add(zoomToEntireMediaMI);
        customZoomMI = new JRadioButtonMenuItem(
        		ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom"));
        customZoomMI.setEnabled(false);
        zoomBG.add(customZoomMI);
        zoomMI.add(customZoomMI);
        zoomMI.addSeparator();
        //
        JRadioButtonMenuItem zoomRB;

        for (int element : ZOOMLEVELS) {
            zoomRB = new JRadioButtonMenuItem(element + "%");
            zoomRB.setActionCommand(String.valueOf(element));
            zoomRB.addActionListener(this);
            zoomBG.add(zoomRB);
            zoomMI.add(zoomRB);

            if (element == 100) {
                zoomRB.setSelected(true);
            }
        }

        popup.add(zoomMI);
        
        JMenu vertZoomMenu = new JMenu(ElanLocale.getString("SignalViewer.VertZoom"));
        vertZoomGroup = new ButtonGroup();
        
        for (int element : VERT_ZOOM) {
            zoomRB = new JRadioButtonMenuItem(element + "%");
            zoomRB.setActionCommand("vz-" + element);
            zoomRB.addActionListener(this);
            vertZoomGroup.add(zoomRB);
            vertZoomMenu.add(zoomRB);
            if (vertZoom == element) {
                zoomRB.setSelected(true);
            }
        }
        popup.add(vertZoomMenu);
        
        ButtonGroup chanGroup = new ButtonGroup();
        channelMI = new JMenu(ElanLocale.getString(
                "SignalViewer.Stereo"));
        separateMI = new JRadioButtonMenuItem(ElanLocale.getString(
                "SignalViewer.Stereo.Separate"));
        separateMI.setActionCommand("sep");
        separateMI.addActionListener(this);
        chanGroup.add(separateMI);
        channelMI.add(separateMI);
        
        mergedMI = new JRadioButtonMenuItem(ElanLocale.getString(
                "SignalViewer.Stereo.Merged"));
        mergedMI.setActionCommand("merge");
        mergedMI.addActionListener(this);
        chanGroup.add(mergedMI);
        channelMI.add(mergedMI);

        blendMI = new JRadioButtonMenuItem(ElanLocale.getString(
                "SignalViewer.Stereo.Blended"));
        blendMI.setActionCommand("blend");
        blendMI.addActionListener(this);
        chanGroup.add(blendMI);
        channelMI.add(blendMI);
        popup.add(channelMI);

        updateChannelModePopUpMenu();
        
        timeRulerVisMI = new JCheckBoxMenuItem(ElanLocale.getString(
			"TimeScaleBasedViewer.TimeRuler.Visible"));
        timeRulerVisMI.setSelected(timeRulerVisible);
        timeRulerVisMI.addActionListener(this);
        popup.add(timeRulerVisMI);
        
        if (allowConnecting) {
            timeScaleConMI = new JCheckBoxMenuItem(ElanLocale.getString(
                        "TimeScaleBasedViewer.Connected"), timeScaleConnected);
            timeScaleConMI.setActionCommand("connect");
            timeScaleConMI.addActionListener(this);
            popup.add(timeScaleConMI);
        }

        popup.addSeparator();
        
        JMenuItem praatMI = new JMenuItem(ElanLocale.getString("SignalViewer.Praat.File"));
		praatMI.setActionCommand("praat");
		praatMI.addActionListener(this);
		popup.add(praatMI);
		
		praatSelMI = new JMenuItem(ElanLocale.getString("SignalViewer.Praat.Selection"));
		praatSelMI.setActionCommand("praatSel");
		praatSelMI.addActionListener(this);
		popup.add(praatSelMI);
		
		clipSelPraatMI = new JMenuItem(ElanLocale.getString("SignalViewer.Praat.Clip"));
		clipSelPraatMI.setActionCommand("clipSel");
		clipSelPraatMI.addActionListener(this);
		popup.add(clipSelPraatMI);
		
		clipSelJavaSoundMI = new JMenuItem(ElanLocale.getString("SignalViewer.JavaSound.Clip"));
		clipSelJavaSoundMI.setActionCommand("clipSelJS");
		clipSelJavaSoundMI.addActionListener(this);
		popup.add(clipSelJavaSoundMI);
		
		//ALBERT
		if (samp != null) {
	        popup.addSeparator();
	        segmentationMenu = new JMenu(ElanLocale.getString("SignalViewer.Segmentation"));
	        
			if (samp.getWavHeader().getNumberOfChannels() == 2) {
		        segmentationChannel1Item = new JCheckBoxMenuItem(ElanLocale.getString("SignalViewer.Segmentation.LeftVisible"), showSegmentationChannel1); 
		        segmentationChannel1Item.addActionListener(this);
		        segmentationMenu.add(segmentationChannel1Item);
		        segmentationChannel2Item = new JCheckBoxMenuItem(ElanLocale.getString("SignalViewer.Segmentation.RightVisible"), showSegmentationChannel2); 
		        segmentationChannel2Item.addActionListener(this);
		        segmentationMenu.add(segmentationChannel2Item);
		        popup.add(segmentationMenu);
				
				segmentationMenu.setEnabled(false);
				segmentationChannel1Item.setEnabled(false);
				segmentationChannel2Item.setEnabled(false);
			} else {
		        segmentationChannel1Item = new JCheckBoxMenuItem(ElanLocale.getString("SignalViewer.Segmentation.Visible"), showSegmentationChannel1); 
		        segmentationChannel1Item.addActionListener(this);
		        segmentationMenu.add(segmentationChannel1Item);
		        popup.add(segmentationMenu);
				
				segmentationMenu.setEnabled(false);
				segmentationChannel1Item.setEnabled(false);
			}
		}
        
		if (allowConnecting) {
			popup.addSeparator();
			infoItem = new JMenuItem(ElanLocale.getString("Player.Info"));
			infoItem.setActionCommand("info");
			infoItem.addActionListener(this);
			popup.add(infoItem);
		}
		copyOrigTimeItem = new JMenuItem(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
		copyOrigTimeItem.addActionListener(this);
		popup.add(copyOrigTimeItem);
		
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        float zoomFl = 100f * (10f / msPerPixel);
//        int zoom = (int) (100f * (10f / msPerPixel));

//        if (zoom <= 0) {
//            zoom = 100;
//        }

        updateZoomPopup(zoomFl);
    }
    
    /**
     * Update the enabled state of selection dependent menu items
     */
    private void updatePopupMenu() {
    	if (popup != null) {
    		boolean enable = (getSelectionBeginTime() != getSelectionEndTime());
    		praatSelMI.setEnabled(enable);
    		clipSelPraatMI.setEnabled(enable);
    		zoomSelectionMI.setEnabled(enable);
    		clipSelJavaSoundMI.setEnabled(enable);
    	}
    }
    
    /**
     * Updates the channel mode popup menu for the current wav file
     */
    private void updateChannelModePopUpMenu(){
    	if(popup == null){
    		createPopupMenu();
    	} else {
    		if (channelMode == MONO) {
            	channelMI.setEnabled(false);
            } else {
            	channelMI.setEnabled(true);
            	
            	if(getViewerManager() != null && getViewerManager().getTranscription() != null){
            		Integer stereoMode = Preferences.getInt("SignalViewer.StereoMode", 
            				getViewerManager().getTranscription());
            		if (stereoMode != null) {
            			if(stereoMode.intValue() > MONO && stereoMode.intValue() <= STEREO_BLENDED) {
							channelMode = stereoMode.intValue();
						}
            		}
            	}        		
        		switch(channelMode) {
				case STEREO_MERGED:
					mergedMI.setSelected(true);
					break;
				case STEREO_BLENDED:
					blendMI.setSelected(true);
					break;
				case STEREO_SEPARATE:
					separateMI.setSelected(true);
				}
			}
    	}
    }

    /**
     * Updates the "zoom" menu item. Needed, when timeScaleConnected, after a
     * change of the zoomlevel in some other connected viewer.
     * Also after zoom to selection the custom zoom level item has to be updated.
     *
     * @param zoom the zoom level
     */
    private void updateZoomPopup(float zoom) {
    	if (popup == null) {
    		return;
    	}
    	// rounding issues 74.999 == 75%, 149.99 == 150%
        if (zoom > 74.99f && zoom < 75f) {
        	zoom = 75f;
        } else if (zoom > 149.99 && zoom < 150f) {
        	zoom = 150f;
        }
        int zoomMenuIndex = -1;

        for (int i = 0; i < ZOOMLEVELS.length; i++) {
        	if (zoom == ZOOMLEVELS[i]) {
        		zoomMenuIndex = i;
        		break;
        	}
        }

        Enumeration<AbstractButton> en = zoomBG.getElements();
        int counter = 0;

        while (en.hasMoreElements()) {
            JRadioButtonMenuItem rbmi = (JRadioButtonMenuItem) en.nextElement();

            if (counter == zoomMenuIndex + 1) {
                rbmi.setSelected(true);

                break;
            } else {
                rbmi.setSelected(false);
            }

            counter++;
        }
        if (zoomMenuIndex == -1) {
        	customZoomMI.setSelected(true);
        	customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom") + " - " + zoom + "%");
        } else {
        	customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom"));
        }
        
    }

    /**
     * Zooms in to the next level of predefined zoomlevels.
     * Note: has to be adapted once custom zoomlevels are implemented.
     */
    private void zoomIn() {
    	float zoom = 100 / (msPerPixel / 10);
    	// HS 08-2012 updated to match the implementation of the TimeLineViewer
    	float nz = zoom + 10;
    	float nm = ((100f / nz) * 10);
    	setMsPerPixel(nm);
    	/*
        // first find the closest match (there can be rounding issues)
        int zoomMenuIndex = -1;
        int diff = Integer.MAX_VALUE;

        for (int i = 0; i < ZOOMLEVELS.length; i++) {
            int d = Math.abs(ZOOMLEVELS[i] - (int) zoom);

            if (d < diff) {
                diff = d;
                zoomMenuIndex = i;
            }
        }
    	
    	if (zoomMenuIndex > -1 && zoomMenuIndex < ZOOMLEVELS.length - 1) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex + 1];
    		float nextMsPerPixel = ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}
    	*/
    }
   
    /**
     * Zooms in to the next level of predefined zoomlevels.
     * Note: has to be adapted once custom zoomlevels are implemented.
     */
    private void zoomOut() {
    	float zoom = 100 / (msPerPixel / 10);
    	// HS 08-2012 updated to match the implementation in the TimeLineViewer
    	float nz = zoom - 10;
    	if (nz < ZOOMLEVELS[0]) {
    		nz = ZOOMLEVELS[0];
    	}
    	float nm = ((100f / nz) * 10);
    	setMsPerPixel(nm);
    	/*
        // first find the closest match (there can be rounding issues)
        int zoomMenuIndex = -1;
        int diff = Integer.MAX_VALUE;

        for (int i = 0; i < ZOOMLEVELS.length; i++) {
            int d = Math.abs(ZOOMLEVELS[i] - (int) zoom);

            if (d < diff) {
                diff = d;
                zoomMenuIndex = i;
            }
        }
    	
    	if (zoomMenuIndex > 0) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex - 1];
    		float nextMsPerPixel = (int) ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}
    	*/
    }
    
    private void zoomToSelection() {
    	long selInterval = getSelectionEndTime() - getSelectionBeginTime();
    	if (selInterval == 0) {
    		return;
    	}
    	//if (selInterval < 150) {
    	//	selInterval = 150;
    	//}
    	int sw = imageWidth != 0 ? imageWidth - (2 * SCROLL_OFFSET) : getViewWidth() - (2 * SCROLL_OFFSET);
    	float nextMsPP = selInterval / (float) sw;
    	// set a limit of zoom = 5% or mspp = 200
//    	if (nextMsPP > 200) {
//    		nextMsPP = 200;
//    	}
    	if (nextMsPP < TimeScaleBasedViewer.MIN_MSPP) {
    		nextMsPP = TimeScaleBasedViewer.MIN_MSPP;
    	}
    	setMsPerPixel(nextMsPP);
    	//customZoomMI.setSelected(true);
    	//customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom") + " - " + (int)(100 / ((float) msPerPixel / 10)) + "%");
    	if (!playerIsPlaying()) {
    		long ibt = getSelectionBeginTime() - (long)(SCROLL_OFFSET * msPerPixel);
    		if (ibt < 0) {
    			ibt = 0;
    		}
    		setIntervalBeginTime(ibt);
    	}
    	if (getViewerManager() != null) {
    		setPreference("SignalViewer.ZoomLevel", new Float(100f * (10f / msPerPixel)), 
        		getViewerManager().getTranscription());
    	}
    }
    
    /**
     * Zoom out (or in) to such a level that the entire media file is displayed in 
     * the viewer area (leaving a small margin on the right side of the viewer).
     */
    private void zoomToShowEntireMedia() {
    	int areaWidth = getViewWidth() - SCROLL_OFFSET;
    	float nextMsPP = samp.getDuration() / (float) areaWidth;
    	if (nextMsPP != msPerPixel) {
	    	setMsPerPixel(nextMsPP);
	    	setIntervalBeginTime(0);
	    	if (getViewerManager() != null) {
	    		setPreference("SignalViewer.ZoomLevel", new Float(100f * (10f / msPerPixel)), 
	        		getViewerManager().getTranscription());
	    	}
    	}
    }

    /**
     * Set the specified menuitem selected.
     * 
     * @param vertZoom the current vertical zoom level
     */
    private void updateVertZoomPopup( int vertZoom) {
        if (popup != null) {
            java.util.Enumeration en = vertZoomGroup.getElements();
            JRadioButtonMenuItem rbmi;
            while (en.hasMoreElements()) {
                rbmi = (JRadioButtonMenuItem) en.nextElement();

                if (rbmi.getText().indexOf("" + vertZoom) > -1) {
                    rbmi.setSelected(true);
                    
                    break;
                } else {
                    rbmi.setSelected(false);
                }
            }
        }	
    }
    
    /**
     * Updates the value of the media offset field, i.e. the new media start
     * point in the .wav file.
     *
     * @param offset the new offset in ms
     */
    public void setOffset(long offset) {
        //System.out.println("SignalViewer: new offset: " + offset);
        if (offset != mediaOffset) {
            mediaOffset = offset;

            //force a reload of data
            currentPart.setStartTime(0);
            currentPart.setStopTime(0);

            crossHairPos = xAt(crossHairTime);
            selectionBeginPos = xAt(getSelectionBeginTime());
            selectionEndPos = xAt(getSelectionEndTime());
            //currentPart.reset();
            paintBuffer();
        }
    }

    /**
     * When a TimeEvent or StopEvent is received the crosshair position is
     * updated. If necessary a new interval is loaded and the new position of
     * the selection in image space is calculated.
     * <p>
     * This is synchronized to prevent interference with setMedia().
     * 
     * @see ControllerListener
     */
    @Override
	synchronized public void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent || event instanceof StopEvent) {
            crossHairTime = getMediaTime();

            if ((crossHairTime == intervalEndTime) && !playerIsPlaying()) {
                recalculateInterval(crossHairTime);
            } else if ((crossHairTime < intervalBeginTime) ||
                    (crossHairTime > intervalEndTime)) {
                recalculateInterval(crossHairTime);
            } else {
                //repaint a part of the viewer
                int oldCrossHairPos = crossHairPos;
                crossHairPos = xAt(crossHairTime);
                
                if (!useBufferedImage) {
                	repaint();
                } else {
	                if (crossHairPos >= oldCrossHairPos) {
	                	//if ()
	                    repaint(oldCrossHairPos - 1, 0,
	                        crossHairPos - oldCrossHairPos + 2, getViewHeight());
	
	                    //repaint();
	                } else {
	                    repaint(crossHairPos - 1, 0,
	                        oldCrossHairPos - crossHairPos + 2, getViewHeight());

	                    //repaint();
	                }
                }
            }
        }
    }

    /*
     * Implement SelectionUser
     * @see mpi.eudico.client.annotator.SelectionUser#updateSelection()
     */
    @Override
	public void updateSelection() {
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());
        repaint();
    }
    
    /**
     * zooms (wave form)the signal viewer for the given time interval
     * (similar to zoomTo selection, mainly used of signal viewer
     *  in transcription mode)
     * 
     * @param begin, the begin time of the interval
     * @param end, the end time of the interval
     */
    public void updateInterval(long begin, long end) {
    	
    	long selInterval = end - begin;     	
     	int sw = imageWidth != 0 ? imageWidth - (2 * SCROLL_OFFSET) : getViewWidth() - (2 * SCROLL_OFFSET);
     	float nextMsPP = selInterval / (float) sw;
     	// set a limit of zoom = 5% or mspp = 200
     	if (nextMsPP > 200) {
     		nextMsPP = 200;
     	}
     	if (nextMsPP < TimeScaleBasedViewer.MIN_MSPP) {
     		nextMsPP = TimeScaleBasedViewer.MIN_MSPP;
     	}
     	setMsPerPixel(nextMsPP);     	
     	if (!playerIsPlaying()) {
     		long ibt = begin - (long)(SCROLL_OFFSET * msPerPixel);
     		if (ibt < 0) {
     			ibt = 0;
     		}
     		setIntervalBeginTime(ibt);
     	}      
        repaint();
    }

    /**
     * AR heeft dit hier neergezet, zie abstract viewer voor get en set
     * methodes van ActiveAnnotation. Update method from ActiveAnnotationUser
     */
    @Override
	public void updateActiveAnnotation() {
    }

    /**
     * method from ElanLocaleListener not implemented in AbstractViewer
     */
    @Override
	public void updateLocale() {
        if (popup != null) {
            createPopupMenu();
        }
    }

    /**
     * Layout information, gives the nr of pixels at the left of the viewer
     * panel that contains no time line information
     *
     * @return the nr of pixels at the left that contain no time line related
     *         data
     */
    public int getLeftMargin() {
        return 0;
    }

    /**
     * Layout information, gives the nr of pixels at the right of the viewer
     * panel that contains no time line information
     *
     * @return the nr of pixels at the right that contain no time line related
     *         data
     */
    public int getRightMargin() {
        return 0;
    }
    
    /**
     * Alternative to getWidth(), returns the width of the viewport if the viewer is 
     * the view in a scrollpane, or the width of the component in case it is not.
     * 
     * @return the width of the scroll pane's viewport or the width of the component
     */
    protected int getViewWidth() {
    	if (viewPortMode) {
    		return viewPort.getExtentSize().width;
    	}
    	return super.getWidth();
    }

    /**
     * Alternative to getHeight(), returns the height of the viewport if the viewer is 
     * the view in a scrollpane, or the height of the component in case it is not.
     * 
     * @return the height of the scroll pane's viewport or the height of the component
     */
    protected int getViewHeight() {
    	if (viewPortMode) {
    		return viewPort.getExtentSize().height;
    	}
    	return super.getHeight();
    }
    
    private Point toPixelCoordinates(Point p) {
    	if (viewPortMode) {
    		return new Point(p.x - viewPort.getViewPosition().x, 
    				p.y - viewPort.getViewPosition().y);
    	}
    	
    	return p;
    }

    /*
     * For testing purposes
     */
    private void printMem(String message) {
        System.out.println(message);

        Runtime r = Runtime.getRuntime();
        System.out.println("Total memory: " + (r.totalMemory() / 1024) + " Kb");
        System.out.println("Free memory: " + (r.freeMemory() / 1024) + " Kb");
    }
    
	/**
     * Puts the specified text on the clipboard.
     * 
     * @param text the text to copy
     */
    private void copyToClipboard(String text) {
    	    if (text == null) {
    		    return;
    	    }
    	    //System.out.println(text);
    	    if (System.getSecurityManager() != null) {
            try {
                System.getSecurityManager().checkSystemClipboardAccess();
                StringSelection ssVal = new StringSelection(text);
                
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ssVal, null);
            } catch (SecurityException se) {
                //LOG.warning("Cannot copy, cannot access the clipboard.");
            } catch (IllegalStateException ise) {
            	   // LOG.warning("");
            }
        } else {
            try {
                StringSelection ssVal = new StringSelection(text);
                
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ssVal, null);
            } catch (IllegalStateException ise) {
            	   // LOG.warning("");
            }
        }
    }

    //*************************************************************************************//

    /* implement ComponentListener */
    /*
     * Calculate a new BufferedImage taken the new size of the Component
     */
    @Override
	public void componentResized(ComponentEvent e) {
    	long curEndTime = intervalEndTime;
        intervalEndTime = intervalBeginTime + (int) (getViewWidth() * msPerPixel);

        if (timeScaleConnected && curEndTime != intervalEndTime) {
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        }

        paintBuffer();

        //repaint();
    }

    /**
     * @param e ignored
     */
    @Override
	public void componentMoved(ComponentEvent e) {
    }

    /**
     * @param e ignored
     */
    @Override
	public void componentShown(ComponentEvent e) {
    }

    /**
     * @param e ignore
     */
    @Override
	public void componentHidden(ComponentEvent e) {
    }

    //***********************************************************************************

    /* implement MouseListener and MouseMotionListener
       /*
     * A mouse click in the SignalViewer updates the media time
     * to the time corresponding to the x-position.
     */
    @Override
	public void mouseClicked(MouseEvent e) { 
    	if(!this.isEnabled()){
    		return;
    	}
        Point pp = e.getPoint();
        
        // ALBERT
        if ((e.getClickCount() == 2)) {
    		long clickTime = timeAt(pp.x) + mediaOffset;
        	switch (channelMode) {
            	case MONO: 
            		if (segmentationChannel1 != null && showSegmentationChannel1) {
		            	long beforeTime = segmentationChannel1.boundaryTimeBefore(clickTime) - mediaOffset;
		            	long afterTime = segmentationChannel1.boundaryTimeAfter(clickTime) - mediaOffset;
		            	setSelection(beforeTime, afterTime);
		            	setMediaTime(beforeTime);
            		}
            		break;
            	case STEREO_SEPARATE: 
            		if (pp.y < ((SignalViewer) e.getSource()).getViewHeight() / 2) {
                		if (segmentationChannel1 != null && showSegmentationChannel1) {
			            	long beforeTime = segmentationChannel1.boundaryTimeBefore(clickTime) - mediaOffset;
			            	long afterTime = segmentationChannel1.boundaryTimeAfter(clickTime) - mediaOffset;
			            	setSelection(beforeTime, afterTime);
			            	setMediaTime(beforeTime);
                		}
            		} else {
                		if (segmentationChannel2 != null && showSegmentationChannel2) {
			            	long beforeTime = segmentationChannel2.boundaryTimeBefore(clickTime) - mediaOffset;
			            	long afterTime = segmentationChannel2.boundaryTimeAfter(clickTime) - mediaOffset;
			            	setSelection(beforeTime, afterTime);
			            	setMediaTime(beforeTime);
                		}
            		}
            		break;
            	case STEREO_MERGED:
                case STEREO_BLENDED:
                	if (segmentationChannel1 != null && showSegmentationChannel1 && !showSegmentationChannel2) {
		            	long beforeTime = segmentationChannel1.boundaryTimeBefore(clickTime) - mediaOffset;
		            	long afterTime = segmentationChannel1.boundaryTimeAfter(clickTime) - mediaOffset;
		            	setSelection(beforeTime, afterTime);
		            	setMediaTime(beforeTime);
                	} else if (segmentationChannel2 != null && showSegmentationChannel2 && !showSegmentationChannel1) {
		            	long beforeTime = segmentationChannel2.boundaryTimeBefore(clickTime) - mediaOffset;
		            	long afterTime = segmentationChannel2.boundaryTimeAfter(clickTime) - mediaOffset;
		            	setSelection(beforeTime, afterTime);
		            	setMediaTime(beforeTime);
                	}	
        	}
        } // END ALBERT
        else if ((e.getClickCount() == 1) && e.isShiftDown()) {
            // change the selection interval
            if (getSelectionBeginTime() != getSelectionEndTime()) {
                long clickTime = timeAt(pp.x);

                if (clickTime > getSelectionEndTime()) {
                    // expand to the right
                    setSelection(getSelectionBeginTime(), clickTime);
                } else if (clickTime < getSelectionBeginTime()) {
                    // expand to the left
                    setSelection(clickTime, getSelectionEndTime());
                } else {
                    // reduce from left or right, whichever boundary is closest
                    // to the click time
                    if ((clickTime - getSelectionBeginTime()) < (getSelectionEndTime() -
                            clickTime)) {
                        setSelection(clickTime, getSelectionEndTime());
                    } else {
                        setSelection(getSelectionBeginTime(), clickTime);
                    }
                }
            } else {
            	// create a selection from media time to click time
            	long clickTime = timeAt(pp.x);
            	long medTime = getMediaTime();
            	if (clickTime > medTime) {
            		setSelection(medTime, clickTime);
            	} else if (clickTime < medTime) {
            		setSelection(clickTime, medTime);
		}
	    }
	} else if (SwingUtilities.isLeftMouseButton(e)) {
	    setMediaTime(timeAt(pp.x));
	}

	
	    // Selection clearing
	    // Skip clearing the selection if shift is down, i.e. if the selection 
	    // is being changed, or if a popup has been created by clicking right.
	    // Because popup.isShowing() on these clicks, the selection will
	    // not be cleared.

	    if (clearSelOnSingleClick && e.getClickCount() == 1
		    && !e.isShiftDown() && !popup.isShowing()
		    && !getViewerManager().getMediaPlayerController().getSelectionMode()) {
	    	setSelection(0, 0);
	    	// For clicks that close the popup not popup.isShowing()
	    	// therefore, we cannot avoid these clicks clearing the 
	    	// selection as well.
	    }
	
    }

    /*
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
	public void mousePressed(MouseEvent e) {
	
    	if(!this.isEnabled()){
    		return;
    	}
    	    	
        if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
			Point pp = e.getPoint();
			
			if (popup == null) {
				createPopupMenu();
			}
			updatePopupMenu();
			
			if ((popup.getWidth() == 0) || (popup.getHeight() == 0)) {
				popup.show(this, pp.x, pp.y);
			} else {
				popup.show(this, pp.x, pp.y);
				SwingUtilities.convertPointToScreen(pp, this);

				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
				Window w = SwingUtilities.windowForComponent(this);

				if ((pp.x + popup.getWidth()) > d.width) {
					pp.x -= popup.getWidth();
				}

				//this does not account for a desktop taskbar
				if ((pp.y + popup.getHeight()) > d.height) {
					pp.y -= popup.getHeight();
				}

				//keep it in the window then
				if ((pp.y + popup.getHeight()) > (w.getLocationOnScreen().y +
						w.getHeight())) {
					pp.y -= popup.getHeight();
				}

				popup.setLocation(pp);
			}
            return;
        }

        if (playerIsPlaying()) {
            stopPlayer();
        }

        dragStartPoint = e.getPoint();
        dragStartTime = timeAt(dragStartPoint.x);

        if (e.isAltDown() && (dragStartPoint.y < rulerHeight)) {
            panMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            //System.out.println("alt down");
        } else {
            panMode = false;

            /* just to be sure a running scroll thread can be stopped */
            stopScroll();
        }
    }

    /*
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseReleased(MouseEvent e) {
    	if(!this.isEnabled()){
    		return;
    	}
        //stop scrolling thread
        stopScroll();

        // changing the selection might have changed the intervalBeginTime
        if (timeScaleConnected) {
            setGlobalTimeScaleIntervalBeginTime(intervalBeginTime);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        }

        if (panMode) {
            panMode = false;
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /*
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseEntered(MouseEvent e) {
    }

    /*
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseExited(MouseEvent e) {
    	if(!this.isEnabled()){
    		return;
    	}
        stopScroll();
        repaint();
    }

    /*
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseDragged(MouseEvent e) {
    	if(!this.isEnabled()){
    		return;
    	}
        if (SwingUtilities.isRightMouseButton(e)) {
            return;
        }

        dragEndPoint = e.getPoint();

        //panning
        if (panMode) {
            int scrolldiff = dragEndPoint.x - dragStartPoint.x;
            long newTime = intervalBeginTime - (int) (scrolldiff * msPerPixel);
            setIntervalBeginTime((newTime < -mediaOffset) ? (-mediaOffset)
                                                          : newTime);
            dragStartPoint = dragEndPoint;

            return;
        }
        
        Point viewDragEndPoint = dragEndPoint;
        if (viewPortMode) {
        	viewDragEndPoint = toPixelCoordinates(dragEndPoint);
        }
        /*e.getPoint can be outside the image size*/
        if ((viewDragEndPoint.x <= 0) || (viewDragEndPoint.x >= getViewWidth())) {
            stopScroll();

            return;
        }

        //auto scroll first
        if ((viewDragEndPoint.x < SCROLL_OFFSET) && (viewDragEndPoint.x > 0)) {

            if (scroller == null) {
                // if the dragging starts close to the edge call setSelection
                if ((viewDragEndPoint.x < SCROLL_OFFSET) &&
                        (viewDragEndPoint.x > 0)) {
                    if (getSelectionBeginTime() == getSelectionEndTime()) {
                    	setSelection(dragStartTime, dragStartTime);
                    }
                }

                stopScrolling = false;
                scroller = new DragScroller(-SCROLL_OFFSET / 4, dragScrollSleepTime);
                scroller.start();
            }

            return;
        } else if ((viewDragEndPoint.x > (getViewWidth() - SCROLL_OFFSET)) &&
                (viewDragEndPoint.x < getViewWidth())) {
            /*
               long begin = intervalBeginTime + SCROLL_OFFSET * msPerPixel;
               setIntervalBeginTime(begin);
               paintBuffer();
             */
            if (scroller == null) {
                // if the dragging starts close to the edge call setSelection
                if ((viewDragEndPoint.x > (getViewWidth() - SCROLL_OFFSET)) &&
                        (viewDragEndPoint.x < getViewWidth())) {
                    if (getSelectionBeginTime() == getSelectionEndTime()) {
                    	setSelection(dragStartTime, dragStartTime);
                    }
                }

                stopScrolling = false;
                scroller = new DragScroller(SCROLL_OFFSET / 4, dragScrollSleepTime);
                scroller.start();
            }

            return;
        } else {
            stopScroll();

            /* selection and media time, prevent from selecting beyond the media length*/
            if (timeAt(dragEndPoint.x) > dragStartTime) { //left to right
                selectionEndTime = timeAt(dragEndPoint.x);

                if ((samp != null) && (selectionEndTime > samp.getDuration())) {
                    selectionEndTime = (long) samp.getDuration();
                }

                selectionBeginTime = dragStartTime;

                if (selectionBeginTime < 0) {
                    selectionBeginTime = 0L;
                }

                if (selectionEndTime < 0) {
                    selectionEndTime = 0L;
                }

                setMediaTime(selectionEndTime);
            } else { //right to left
                selectionBeginTime = timeAt(dragEndPoint.x);

                if ((samp != null) &&
                        (selectionBeginTime > samp.getDuration())) {
                    selectionBeginTime = (long) samp.getDuration();
                }

                selectionEndTime = dragStartTime;

                if ((samp != null) && (selectionEndTime > samp.getDuration())) {
                    selectionEndTime = (long) samp.getDuration();
                }

                if (selectionBeginTime < 0) {
                    selectionBeginTime = 0L;
                }

                if (selectionEndTime < 0) {
                    selectionEndTime = 0L;
                }

                setMediaTime(selectionBeginTime);
            }

            setSelection(selectionBeginTime, selectionEndTime);
            repaint();
        }
    }

    /*
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseMoved(MouseEvent e) {
        if (e.getPoint().y <= rulerHeight) {
            //setToolTipText(Long.toString(timeAt(e.getPoint().x)));
            setToolTipText(TimeFormatter.toString(timeAt(e.getPoint().x)));
        } else if ((e.getPoint().x >= selectionBeginPos) &&
                (e.getPoint().x <= selectionEndPos)) {
            //setToolTipText("Selection: " +
            //     TimeFormatter.toString(getSelectionBeginTime()) + " - " +
            //    TimeFormatter.toString(getSelectionEndTime()));
            StringBuilder sb = new StringBuilder("<html><table><tr><td>");
            sb.append(ElanLocale.getString("MediaPlayerControlPanel.Selectionpanel.Name"));
            sb.append(":</td><td>").append(TimeFormatter.toString(getSelectionBeginTime())).append(" - ");
            sb.append(TimeFormatter.toString(getSelectionEndTime())).append("</td></tr><tr><td>");
            sb.append(ElanLocale.getString("Frame.GridFrame.ColumnDuration")).append(":</td><td>");
            sb.append(TimeFormatter.toString(getSelectionEndTime() - getSelectionBeginTime()));
            sb.append("</td></tr></table></html>");
            setToolTipText(sb.toString());
        } else {
            setToolTipText(null);
        }
    }

    /**
     * The use of a mousewheel needs Java 1.4!<br>
     * Zoom in or out (CTRL), or scroll left or right (SHIFT).
     *
     * @param e the mousewheel event
     */
    @Override
	public void mouseWheelMoved(MouseWheelEvent e) {
    	if (e.getWheelRotation() == 0) {
    		return;
    	}
    	if (e.isControlDown()) {
    		if (e.getUnitsToScroll() > 0) {
    			zoomOut();
    		} else {
    			zoomIn();
    		}
    		return;
    	} else if (e.isShiftDown()) {// on Mac this is the same as hor. scroll with two fingers on the trackpad
	        if (e.getWheelRotation() != 0) {
	        	int timeDiff = (int) (horScrollSpeed * e.getWheelRotation() * msPerPixel);// 2: arbitrary acceleration of the gesture
	        	long newTime = intervalBeginTime + timeDiff;
	        	if (newTime != intervalBeginTime && !(intervalBeginTime < 0 && newTime < intervalBeginTime)) {
	        		setIntervalBeginTime(newTime);
	        	}
	        }
	        return;
        }
    }
    
    /**
     * Zooming in or out as the "pinch" behaviour.
     */
	@Override
	public void magnify(double zoom) {
		if (zoom > 0) {
			zoomIn();
		} else if (zoom < 0) {
			zoomOut();
		}
	}

	/**
	 * Horizontal swiping behaviour.
	 */
	@Override
	public void swipe(int x, int y) {
		if (x != 0) {
			long newTime = intervalBeginTime + (long) (x * msPerPixel);
			if (newTime != intervalBeginTime && !(newTime < 0 && newTime < intervalBeginTime)) {
				setIntervalBeginTime(newTime);
			}
		}		
	}
    
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == timeRulerVisMI) {
    		timeRulerVisible = timeRulerVisMI.isSelected();
    		if (timeRulerVisible) {
    			rulerHeight = ruler.getHeight();
    		} else {
    			rulerHeight = 0;
    		}
    		paintBuffer();
    		if (getViewerManager() != null) {
    			setPreference("SignalViewer.TimeRulerVisible", Boolean.valueOf(timeRulerVisible),
    				getViewerManager().getTranscription());
    		}
    	}
    	else if (e.getActionCommand().equals("connect")) {
            boolean connected = ((JCheckBoxMenuItem) e.getSource()).getState();
            setTimeScaleConnected(connected);
            if (getViewerManager() != null) {
            	setPreference("SignalViewer.TimeScaleConnected", Boolean.valueOf(connected), 
            		getViewerManager().getTranscription());
            }
        } else if (e.getActionCommand().equals("sep")) {
            setChannelMode(STEREO_SEPARATE, true);
        } else if (e.getActionCommand().equals("merge")) {
            setChannelMode(STEREO_MERGED, true);
        } else if (e.getActionCommand().equals("blend")) {
            setChannelMode(STEREO_BLENDED, true);
        } else if (e.getActionCommand().equals("praat")) {
            openInPraat(0, 0);
        } else if (e.getActionCommand().equals("praatSel")) {
            openSelectionInPraat();
        } else if (e.getActionCommand().equals("clipSel")) {
            clipSelectionWithPraat();
        } else if (e.getActionCommand().equals("clipSelJS")) {
            clipSelectionWithJavaSound();
        } else if (e.getActionCommand().equals("info")) {
            showMediaInfo();
        }/*else if (e.getActionCommand().indexOf("res") > -1) {
           String com = e.getActionCommand();
           String resString = com.substring(com.indexOf(" ") + 1);
           float factor = 100 / Float.parseFloat(resString);
           int newMsPerPixel = (int)(1000f / (PIXELS_FOR_SECOND * factor));
           setMsPerPixel(newMsPerPixel);
           }*/
        else if (e.getActionCommand().startsWith("vz-")) {
            // vertical zoom or amplification
            int nvz = 100;
            String vzVal = e.getActionCommand().substring(3);
            try {
                nvz = Integer.parseInt(vzVal);
            } catch (NumberFormatException nfe) {
                System.out.println("Error parsing the vertical zoom level");
            }
            vertZoom = nvz;
            paintBuffer();
            if (getViewerManager() != null) {
            	setPreference("SignalViewer.VerticalZoomLevel", Integer.valueOf(vertZoom), 
            		getViewerManager().getTranscription());
            }
        }
        else if (e.getSource() == zoomSelectionMI) {
    		zoomToSelection();
    	}
        else if (e.getSource() == zoomToEntireMediaMI) {
        	zoomToShowEntireMedia();
        }
        // ALBERT
        else if (e.getSource() == segmentationChannel1Item) {
        	showSegmentationChannel1 = segmentationChannel1Item.getState();
            paintBuffer();
        } 
        else if (e.getSource() == segmentationChannel2Item) {
        	showSegmentationChannel2 = segmentationChannel2Item.getState();
            paintBuffer();
        } else if (e.getSource() == copyOrigTimeItem) {
			long t = getMediaTime() + mediaOffset;
			String timeFormat = Preferences.getString("CurrentTime.Copy.TimeFormat", null);
			String currentTime = null;
			
	        if (timeFormat != null) {
	        	if (timeFormat.equals(Constants.HHMMSSMS_STRING)){
	            	currentTime = TimeFormatter.toString(t);
	            } else if(timeFormat.equals(Constants.SSMS_STRING)){
	            	currentTime = TimeFormatter.toSSMSString(t);
	            } else if(timeFormat.equals(Constants.NTSC_STRING)){
	            	currentTime = TimeFormatter.toTimecodeNTSC(t);
	            } else if(timeFormat.equals(Constants.PAL_STRING)){
	            	currentTime = TimeFormatter.toTimecodePAL(t);
	            } else if(timeFormat.equals(Constants.PAL_50_STRING)){
	            	currentTime = TimeFormatter.toTimecodePAL50(t);
	            } else {
	            	currentTime = Long.toString(t);
	            }
	        } else {
	        	currentTime = Long.toString(t);
	        }
	        copyToClipboard(currentTime);
		}
        // END ALBERT
        else {
            /* the rest are zoom menu items*/
            String zoomString = e.getActionCommand();
            int zoom = 100;

            try {
                zoom = Integer.parseInt(zoomString);
            } catch (NumberFormatException nfe) {
                System.err.println("Error parsing the zoom level");
            }

            float newMsPerPixel = ((100f / zoom) * 10);
            setMsPerPixel(newMsPerPixel);            
        }
    }

    /**
     * Updates the viewer after a change in or loading of preferences.
     */
	@Override
	public void preferencesChanged() {
	    // super.preferencesChanged (); 
		Float zoomLevel = Preferences.getFloat("SignalViewer.ZoomLevel", 
				getViewerManager().getTranscription());
		if (zoomLevel != null) {
			float zl = zoomLevel.floatValue();
			if(zl > 5000.0){
				zl = 5000;
			}
			float newMsPerPixel = (100f / zl) * 10;
            setMsPerPixel(newMsPerPixel);
			//updateZoomPopup((int)zl);
		}
		Boolean tsConnect = Preferences.getBool("SignalViewer.TimeScaleConnected", 
				getViewerManager().getTranscription());
		if (tsConnect != null) {
			if (timeScaleConMI != null) {
				timeScaleConMI.setSelected(tsConnect.booleanValue());
			}
			setTimeScaleConnected(tsConnect.booleanValue());
		}
		
		updateChannelModePopUpMenu();
		
		Integer vzLevel = Preferences.getInt("SignalViewer.VerticalZoomLevel",
				getViewerManager().getTranscription());
		if (vzLevel != null) {
			vertZoom = vzLevel.intValue();
			updateVertZoomPopup(vertZoom);
		}
		Boolean rulerVis = Preferences.getBool("SignalViewer.TimeRulerVisible", 
				getViewerManager().getTranscription());
		if (rulerVis != null) {
			timeRulerVisible = rulerVis.booleanValue();
			if (timeRulerVisMI != null) {
				timeRulerVisMI.setSelected(timeRulerVisible);
			}
			if (timeRulerVisible) {
				rulerHeight = ruler.getHeight();
			} else {
				rulerHeight = 0;
			}
		}
		Boolean boolPref = Preferences.getBool("ClearSelectionOnSingleClick",null);
		if (boolPref != null) {
		    clearSelOnSingleClick = boolPref.booleanValue();
		}
		
		Integer intPref = Preferences.getInt("Preferences.TimeLine.HorScrollSpeed", null);
		if (intPref != null) {
			horScrollSpeed = intPref;
		}
		
		boolPref = Preferences.getBool("UI.UseBufferedPainting", null);
		if (!SystemReporting.isBufferedPaintingPropertySet && boolPref != null) {
			useBufferedImage = boolPref;
		}
		
		paintBuffer();
	}
	
// #####     Zoomable interface     #####
    // this can be removed once SignalViewer extends  DefaultTimeScaleBasedViewer
	@Override
	public void zoomInStep() {
		float zoom = 100 / (msPerPixel / 10);
    	// if zoom is already larger than the max predefined level either
    	// return or add a fixed percentage?
    	if (zoom >= ZOOMLEVELS[ZOOMLEVELS.length - 1]) {
    		return;
    	}
        int zoomMenuIndex = -1;
    	// find between which levels the current value is, go to the larger one
    	for (int i = 0; i < ZOOMLEVELS.length; i++) {
    		if (zoom < ZOOMLEVELS[i]) {
    			zoomMenuIndex = i;
    			break;
    		} else if (zoom == ZOOMLEVELS[i]) {
    			zoomMenuIndex = i + 1;
    		}
    	}
    	if (zoomMenuIndex >= 0 && zoomMenuIndex < ZOOMLEVELS.length) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex];
    		float nextMsPerPixel = ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}	
	}

	@Override
	public void zoomOutStep() {
		float zoom = 100 / (msPerPixel / 10);
    	// if zoom is already smaller than the min predefined level return
    	if (zoom <= ZOOMLEVELS[0]) {
    		return;
    	}
        int zoomMenuIndex = -1;
    	// find between which levels the current value is, go to the larger one
    	for (int i = ZOOMLEVELS.length -1; i >= 0 ; i--) {
    		if (zoom > ZOOMLEVELS[i]) {
    			zoomMenuIndex = i;
    			break;
    		} else if (zoom == ZOOMLEVELS[i]) {
    			zoomMenuIndex = i - 1;
    		}
    	}
    	if (zoomMenuIndex >= 0 && zoomMenuIndex < ZOOMLEVELS.length) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex];
    		float nextMsPerPixel = ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}
	}


	@Override
	public void zoomToDefault() {
		if ((int) msPerPixel != DEFAULT_MS_PER_PIXEL) {
    		int nextZoom = 100; // 100%
    		float nextMsPerPixel = DEFAULT_MS_PER_PIXEL;
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
		}		
	}

// #####     Scrollable interface and related, viewport mode or not     #####
	
	/**
	 * Make a distinction between the size when in a scrollpane and when used "standalone"
	 * 
	 * @return when in a scrollpane returns the required size for the entire sound file given the
	 * current zoom level (plus some extra)
	 */
	@Override
	public Dimension getPreferredSize() {
		if (viewPortMode) {
			return new Dimension((int) (
					(getMediaDuration() - mediaOffset) / msPerPixel) + (getViewWidth() / 2), 
					getViewHeight());
		}
		return super.getPreferredSize();
	}
	
	/**
	 * @return same as getPreferredSize
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}


	/**
	 * Currently returns 10 but it might be better to make it dependent on the zoom level?
	 *  
	 * @param visibleRect
	 * @param orientation
	 * @param direction
	 * @return currently 10 pixels
	 */
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			return 0;
		} else {
			return 10;
		}
	}

	/**
	 * The horizontal block increment is the width of the view.
	 * @param visibleRect
	 * @param orientation only horizontal scrolling is allowed
	 * @param direction ignored, the same for left and right
	 * @return the number of pixels to scroll
	 */
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			return 0;
		} else {
			if (viewPort != null) {
				return viewPort.getExtentSize().width;
				// or return visibleRect.width;
			}
		}
		return 0;
	}

	/**
	 * There should be a horizontal scroll pane.
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	/**
	 * No vertical scrollbar for this viewer. 
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return true;
	}

	/**
	 * If this component is added to a parent component, this checks
	 * if the parent is a scrollpane or any other container.
	 */
	@Override
	public void addNotify() {
		super.addNotify();
		if (getParent() instanceof JViewport) {
			viewPort = (JViewport) getParent();
			viewPortListener = new ViewPortChangeListener();
			viewPort.addChangeListener(viewPortListener);
			viewPortMode = true;
			revalidate();
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		if (viewPort != null) {
			viewPort.removeChangeListener(viewPortListener);
		}
	}

	/**
	 * Inner class to listen to changes in the scroll position of 
	 * the view port in case the viewer is in a scroll pane. 
	 *
	 */
	private class ViewPortChangeListener implements ChangeListener {
		/* a flag to activate or deactivate the listener. This is a way to prevent
		 * an endless loop (scoll event -> update interval begin time -> 
		 * scrollRectToVisible -> scroll event */
		private boolean enabled = true;
		
		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		/**
		 * Updates the visible interval and redraws the waveform etc.
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			if (!enabled) {
				return;
			}
			if (e.getSource() == viewPort) {
				imageWidth = viewPort.getExtentSize().width;
				imageHeight = viewPort.getExtentSize().height;
				//viewPort.removeChangeListener(this);
				enabled = false;
				setIntervalBeginTime(timeAt(viewPort.getViewPosition().x));
				//viewPort.addChangeListener(this);
				enabled = true;
				repaint();
			}
		}		
	}
	
	// #####	
    /**
     * Try to close the RandomAccessFile of the WAVSampler.
     */
    @Override
	public void finalize() {
        if (samp != null) {
            samp.close();
        }
    }

    /**
     * Scrolls the image while dragging to the left or right with the specified
     * number  of pixels.<br>
     * This method is called from a separate Thread.
     *
     * @param numPixels the number of pixels to scroll the interval
     *
     * @see DragScroller
     */
    synchronized void scroll(int numPixels) {
        long begin = intervalBeginTime + (int) (numPixels * msPerPixel);

        if (numPixels > 0) {
            // left to right
            setIntervalBeginTime(begin);
            selectionEndTime = getSelectionEndTime() +
            		(int) (numPixels * msPerPixel);

            if ((samp != null) && (selectionEndTime > samp.getDuration())) {
                selectionEndTime = (long) samp.getDuration();
            }

            setMediaTime(selectionEndTime);
            setSelection(getSelectionBeginTime(), selectionEndTime);
        } else {
            // right to left
            if (begin < 0) {
                begin = 0;
            }

            setIntervalBeginTime(begin);
            selectionBeginTime = getSelectionBeginTime() +
            (int) (numPixels * msPerPixel);

            if (selectionBeginTime < 0) {
                selectionBeginTime = 0;
            }

            setMediaTime(selectionBeginTime);
            setSelection(selectionBeginTime, getSelectionEndTime());
        }

        //repaint();
    }

    /**
     * DOCUMENT ME!
     */
    void stopScroll() {
        /*
           if (scroller != null) {
               try {
                   scroller.interrupt();
               } catch (SecurityException se) {
                   System.out.println("SignalViewer: could not stop scroll thread");
               } finally {
               }
               scroller = null;
           }
         */
        stopScrolling = true;
        scroller = null;
    }

    /**
     * $Id: SignalViewer.java 46351 2017-12-20 10:39:45Z hasloe $
     */
    class DragScroller extends Thread {
        /** the number of pixels to scroll */
        int numPixels;

        /** the sleep time determining the scroll speed */
        long sleepTime;

        /**
         * Creates a new DragScroller instance
         *
         * @param numPixels the number of pixels to scroll
         * @param sleepTime the sleep time determining the scroll speed
         */
        DragScroller(int numPixels, long sleepTime) {
            this.numPixels = numPixels;
            this.sleepTime = sleepTime;
        }

        /**
         * Starts the scrolling.
         */
        @Override
		public void run() {
            while (!stopScrolling) {
                SignalViewer.this.scroll(numPixels);

                try {
                    sleep(sleepTime);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

}
