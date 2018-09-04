package mpi.eudico.client.annotator.player;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.export.ImageExporter;
import mpi.eudico.client.annotator.gui.FormattedMessageDlg;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;
import uk.co.caprica.vlcj.Info;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

/**
 * The VLCJ based implementation of an elan media player
 */
public class VLCJMediaPlayer extends ControllerManager implements
        ElanMediaPlayer, NeedsCreateNewVisualComponent,
        ControllerListener, ActionListener {

    private static final Logger logger = Logger.getLogger(VLCJMediaPlayer.class.
            getName());
    /**
     * Player reference
     */
    private EmbeddedMediaPlayer player;
    /**
     * Display component
     */
    private EmbeddedMediaPlayerComponent playerComponent;
    /**
     * Where to find the media file
     */
    private final MediaDescriptor mediaDescriptor;
    /**
     * The temporal offset between external and media time-stamps.
     */
    private long timeOffset;

    private static enum PlayMode {
        ToEnd, Interval
    }

    private final ReentrantLock modeLock = new ReentrantLock();
    /**
     * Current play mode
     */
    private PlayMode mode = PlayMode.ToEnd;
    /**
     * Current playing interval. stopTime is adjusted for the timeOffset.
     */
    private long startTime, stopTime;
    /**
     */
    private Dimension videoSize;
	private static final Dimension fallbackVideoSize = new Dimension(352, 288);
	private float aspectRatio, origAspectRatio;
	private double msPerFrame = 0.0d;
	private boolean isVisual;
	private ElanLayoutManager layoutManager;
	/**
	 * Setting the volume "too early", before it really starts playing, doesn't
	 * (always) work. Save the volume here, so that it can be applied later 
	 * when the movie starts playing.
	 */
	private int savedVolume;
	
	// Menu-related fields
    private JPopupMenu popup;
    private JMenuItem durationItem;
    protected JMenuItem detachItem;
    private JMenuItem infoItem;
	private JMenuItem saveItem;
	private JRadioButtonMenuItem origRatioItem;
	private JRadioButtonMenuItem ratio_1_1_Item;
    private JRadioButtonMenuItem ratio_5_4_Item;
	private JRadioButtonMenuItem ratio_4_3_Item;
	private JRadioButtonMenuItem ratio_16_10_Item;
	private JRadioButtonMenuItem ratio_16_9_Item;
	private JRadioButtonMenuItem ratio_235_1_Item;
	private JRadioButtonMenuItem ratio_221_1_Item;
	private JRadioButtonMenuItem ratio_239_1_Item;
	private JMenuItem copyOrigTimeItem;
	private boolean detached;
	private JMenu arMenu;
	private JMenu zoomMenu;
	private JRadioButtonMenuItem zoomOriginal;
	private JRadioButtonMenuItem zoom100;
	private JRadioButtonMenuItem zoom150;
	private JRadioButtonMenuItem zoom200;
	private JRadioButtonMenuItem zoom300;
	private JRadioButtonMenuItem zoom400;
	private float videoScaleFactor = 0;

    static {
    	// A settable property: -Dnl.mpi.elan.vlcj=/Applications/VLC.app/Contents/MacOS/lib
    	String path = System.getProperty("nl.mpi.elan.vlcj");
    	if (path != null) {
	        NativeLibrary.addSearchPath(
	                RuntimeUtil.getLibVlcLibraryName(), path);
    	}
    	// A reasonable default location to install VLC
        String home = SystemReporting.USER_HOME;
        if (home != null && !home.isEmpty()) {
	        NativeLibrary.addSearchPath(
	                RuntimeUtil.getLibVlcLibraryName(), home + "/Applications/VLC.app/Contents/MacOS/lib");
        }
    	// Another reasonable default location to install VLC
        NativeLibrary.addSearchPath(
                RuntimeUtil.getLibVlcLibraryName(), "/Applications/VLC.app/Contents/MacOS/lib");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        System.loadLibrary("jawt");
        
        // Safely try to initialise LibX11 to reduce the opportunity for native
        // crashes - this will silently throw an Error on Windows (and maybe MacOS)
        // that can safely be ignored.
        // From https://github.com/caprica/vlcj/issues/293 .
        //
        // If this is not done early enough (XInitThreads(3) says it must be called
        // before the first Xlib call, but so far that hasn't been an issue) then you get
        // error messages like these:
        // ../../../include/vlc_xlib.h:46:vlc_xlib_init: Xlib not initialized for threads.
        // This process is probably using LibVLC incorrectly.
        // Pass "--no-xlib" to libvlc_new() to fix this.
        // [00007f31acc6f1d8] vdpau_avcodec generic error: Xlib is required for VDPAU
        //
        // If it turns out that calling this here is too late, move the call earlier,
        // but try to make sure it doesn't create an unneeded dependency on 
        // vlcj or X.
        LibXUtil.initialise();
    }

    /**
     * Create a VLCJMediaPlayer2 for a media URL
     *
     * @param mediaDescriptor DOCUMENT ME!
     *
     * @throws NoPlayerException DOCUMENT ME!
     */
	public VLCJMediaPlayer(MediaDescriptor mediaDescriptor)
            throws NoPlayerException {
        this.mediaDescriptor = mediaDescriptor;
        isVisual = ! mediaDescriptor.mimeType.startsWith("audio/");

        internalCreateNewVisualComponent(null);

    	if (isVisual) {
            // At this point, no usable info is actually available yet.
            // seekable, playable, #video outputs, video size... all false or 0.
            // Therefore, we play a small bit of the media in a second media player,
            // which doesn't display anything on the screen, to find out the video size. 
    		tryHiddenPlayer(mediaDescriptor.mediaURL);     

    		Dimension dim = (videoSize != null) ? videoSize
                                                : fallbackVideoSize;
			origAspectRatio = (float)dim.getWidth() / (float)dim.getHeight();
			aspectRatio = origAspectRatio;
    	} else {
			aspectRatio = origAspectRatio = -1;
    	}
    }
    
	private void internalCreateNewVisualComponent(String [] prepareOptions) {
        playerComponent = new EmbeddedMediaPlayerComponent();
        player = playerComponent.getMediaPlayer();
        player.addMediaPlayerEventListener(infoListener);
        
        playerComponent.addHierarchyListener(new HierarchyListener() {
			// This makes sure that when the player window becomes displayable
			// (which is a subset of visible) the player shows some frame in it. 
			// This can't be done before that time.
            @Override
			public void hierarchyChanged(HierarchyEvent e) {
            	long flags = e.getChangeFlags() & (HierarchyEvent.PARENT_CHANGED);
                if ((flags != 0) &&	e.getComponent().isDisplayable()) {
                    player.start();
                   	player.pause();
            	}
            }
        });

        // configure the player with the given media file
        player.prepareMedia(mediaDescriptor.mediaURL, prepareOptions);
        player.parseMedia();
        setOffset(mediaDescriptor.timeOrigin);
        
    	if (isVisual) {            
            player.setEnableMouseInputHandling(false);
            player.setEnableKeyInputHandling(false);
            MouseHandler mh = new MouseHandler();
    		Canvas c = playerComponent.getVideoSurface();
    		c.addMouseListener(mh);
    		c.addMouseMotionListener(mh);
    	}
	}
	
    private DirectMediaPlayer hiddenMediaPlayer;
    private Semaphore semaphore;
    
    /*
     * Play media invisibly, to find out the size.
     */
    private void tryHiddenPlayer(String media) {
	    	MediaPlayerFactory factory = new MediaPlayerFactory();
	    	TestRenderCallback render = new TestRenderCallback();
	    	hiddenMediaPlayer = factory.newDirectMediaPlayer(new TestBufferFormatCallback(), render);
	        hiddenMediaPlayer.setVolume(0);
	        semaphore = new Semaphore(0);	        
	        hiddenMediaPlayer.playMedia(media);
	        try {
	        	logger.log(Level.FINE, "tryHiddenPlayer: waiting for semaphore...");
	        	semaphore.tryAcquire(1000L, TimeUnit.MILLISECONDS);
	        } catch(InterruptedException ex) {	        		
	        }
        	hiddenMediaPlayer.stop();
	        hiddenMediaPlayer.release();
	        hiddenMediaPlayer = null;
	        semaphore = null;
    }

    private final class TestRenderCallback implements RenderCallback {
    	
        public TestRenderCallback() {
	        logger.log(Level.FINER, "TestRenderCallback()");
        }

        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffer, BufferFormat bufferFormat) {
	        logger.log(Level.FINER, "TestRenderCallback.display()");
        }
    }

    private final class TestBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
        	logger.log(Level.FINE, "getBufferFormat: sourceWidth={0} sourceHeight={1}", new Object[]{
        			sourceWidth,  sourceHeight
        	});
        	videoSize = new Dimension(sourceWidth, sourceHeight);
        	// As soon as we're here, we might as well stop. We know what we wanted.
        	logger.log(Level.FINE, "tryHiddenPlayer: TestBufferFormatCallback: releasing semaphore");
        	semaphore.release();
            return new RV32BufferFormat(352, 288);
        }
    }

    private final MediaPlayerEventListener intervalListener =
            new MediaPlayerEventAdapter() {
                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    modeLock.lock();
                    try {
                        if (newTime > stopTime && mode == PlayMode.Interval) {
                        	stop();
                            // internalSetMediaTime(startTime); Other code will go back to the start, if in loop mode
                        }
                    } finally {
                        modeLock.unlock();
                    }
                }
            };

    @Override // NeedsCreateNewVisualComponent
    public java.awt.Component createNewVisualComponent() {
        logger.log(Level.FINE, "createNewVisualComponent");
        if (isVisual && playerComponent != null && player != null) {
        	// Preserve the time, volume, play rate, aspect ratio.
        	long time = player.getTime();
        	int volume = player.getVolume();
        	float rate = player.getRate();
        	float ar = getAspectRatio();
        	
   			playerComponent.release(true);
   			playerComponent = null;
   			String opt1 = ":start-time=" + Float.toString(time / 1000f);
   			String[] opts = { opt1 };
            internalCreateNewVisualComponent(opts);
			
            player.setRate(rate);
            player.setVolume(volume);
            setAspectRatio(ar);
            
            stopControllers();
            
            return playerComponent;
        } else {
        	// This method should not have been called in this case.
            return null;
        }        
    }
    
    @Override
    public MediaDescriptor getMediaDescriptor() {
        return mediaDescriptor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public String getFrameworkDescription() {
        return "VLCJMediaPlayer-" + Info.getInstance().version();
    }

    /**
     * Elan controllerUpdate Used to stop at the stop time in cooperation with
     * the playInterval method
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public void controllerUpdate(ControllerEvent event) {
        logger.log(Level.FINE, "controller update {0}", event);
    }

    /**
     * play between two times. This method uses the contollerUpdate method to
     * detect if the stop time is passed.
     *
     * @param startTime DOCUMENT ME!
     * @param stopTime DOCUMENT ME!
     */
    @Override
    public void playInterval(long startTime, long stopTime) {
        logger.log(Level.FINE, "play interval {0}-{1}", new Object[]{startTime,
                    stopTime});
        modeLock.lock();
        try {
            this.startTime = Math.max(0, startTime);
            this.stopTime = Math.min(stopTime + timeOffset, player.getLength());
            internalSetMediaTime(this.startTime);
            switch (mode) {
                case ToEnd:
                    mode = PlayMode.Interval;
                    player.addMediaPlayerEventListener(intervalListener);
            }
            start();
        } finally {
            modeLock.unlock();
        }
    }

    /**
     * Empty implementation for ElanMediaPlayer Interface Only useful for
     * player that correctly supports setting stop time
     */
    @Override
	public void setStopTime(long stopTime) {
    }

    /**
     * Disable all code for interval playing.
     */
    private void stopPlayingInterval() {
        modeLock.lock();
        try {
            player.removeMediaPlayerEventListener(intervalListener);
            mode = PlayMode.ToEnd;
        } finally {
            modeLock.unlock();
        }
    }

    /**
     * Gets the display Component for this Player.
     * Unfortunately, the VLC player insists on its Component to be displayed,
     * even for audio-only media.
     */
    @Override
    public java.awt.Component getVisualComponent() {
    	return playerComponent;
    }

    /**
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceHeight()
     */
    @Override
    public int getSourceHeight() {
        logger.log(Level.FINE, "getSourceHeight; size is {0}known",
        		videoSize == null ? "not " : "");
        if (videoSize != null) {
        	return videoSize.height;
        } else {
        	return fallbackVideoSize.height;
        }
    }

    /**
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceWidth()
     */
    @Override
    public int getSourceWidth() {
        logger.log(Level.FINE, "getSourceWidth; size is {0}known",
        		videoSize == null ? "not " : "");
        if (videoSize != null) {
        	return videoSize.width;
        } else {
        	return fallbackVideoSize.width;
        }
    }

    /**
     * Gets the control Component for this Player. Necessary for CorexViewer.
     * A.K.
     *
     * @return DOCUMENT ME!
     */
    public java.awt.Component getControlPanelComponent() {
        return null;
    }

    /**
     * Gets the ratio between width and height of the video image
     *
     * @return DOCUMENT ME!
     */
    @Override
    public float getAspectRatio() {
        logger.log(Level.FINE, "getAspectRatio: {0}", aspectRatio);
    	if (isVisual) {
    		return aspectRatio;
    	} else {
    		return -1; // Hack in ElanLayoutManager2.addMediaPlayer() will move it out of the way.
    	}
    }

    /**
     * Enforces an aspect ratio for the media component.
     * Only these 8 exact strings are understood by VLC.
     * Other strings result in the default aspect ratio.
     *
     * @param aspectRatio the new aspect ratio
     */
    @Override
	public void setAspectRatio(float aspectRatio) {
        String aspect = "";
        switch ((int)(100 * aspectRatio + 0.5)) {
	        case 100: aspect =   "1:1"; break;
	        case 125: aspect =   "5:4"; break;
	        case 133: aspect =   "4:3"; break;
	        case 160: aspect =  "16:10"; break;
	        case 177:
	        case 178: aspect =  "16:9"; break;
	        case 221: aspect = "221:100"; break;
	        case 234: /* bad floating point rounding! */
	        case 235: aspect = "235:100"; break;
	        case 239: aspect = "239:100"; break;
	        default: aspect  = ""; break;
        }
        logger.log(Level.FINE, "player.setAspectRatio({0})", aspect);
    	player.setAspectRatio(aspect);
    	this.aspectRatio = aspectRatio;
    }

    /**
     * Starts the Player as soon as possible.
     * Also set the volume again (try to cause no delay to the starting of all other media).
     */
    @Override
    public void start() {
       	player.play();	// async!
        startControllers();
       	player.setVolume(savedVolume);
    }

    /**
     * Stop the media player
     */
    @Override
    public void stop() {
        if (player.isPlaying()) {
            if (player.canPause()) {
                player.pause();
            } else {
                player.stop();
            }
        }
        stopControllers();
        setControllersMediaTime(getMediaTime());
        stopPlayingInterval();
    }

    /**
     * Tell if this player is playing
     *
     * @return true if the player is playing, false otherwise
     */
    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    /**
     * TODO We don't know always how many frames per second the media is...
     * That hopefully becomes known while playing, but even that
     * depends in the format of the media. It seems to work for MPEG4
     * but not for MPEG(2).
     *
     * @return the step size for one frame
     */
    @Override
    public double getMilliSecondsPerSample() {
    	if (msPerFrame > 0) {
    		return msPerFrame;
    	}
    	// Return an arbitrary value: 25 fps.
        return 1000 / 25;
    }

    /**
     * DOCUMENT ME!
     *
     * @param milliSeconds the step size for one frame
     */
    @Override
    public void setMilliSecondsPerSample(long milliSeconds) {
        logger.log(Level.INFO, "Setting ms/sample not supported, requested {0}",
                milliSeconds);
    }

    /**
     * Gets the volume as a number between 0 and 1
     *
     * @return DOCUMENT ME!
     */
    @Override
    public float getVolume() {
        return player.getVolume() / 100f;
    }

    /**
     * Sets the volume as a number between 0 and 1.
     * Also remember it for possible later use.
     *
     * @param level a number between 0 and 1
     */
    @Override
    public void setVolume(float level) {
        int value = (int) (level * 100);
        player.setVolume(value);
        savedVolume = value;
    }

    /**
     * Set the offset to be used in get and set media time for this player
     *
     * @param offset the offset in milli seconds
     */
    @Override
    public void setOffset(long offset) {
        logger.log(Level.FINE, "set offset {0}", offset);
        this.timeOffset = offset;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the offset used by this player
     */
    @Override
    public long getOffset() {
        return timeOffset;
    }

    /**
     * Gets this Clock's current media time in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    @Override
    public long getMediaTime() {
		return player.getTime() - timeOffset;
    }

    /**
     * Sets the media time in milliseconds.
     * This means that the player is set to that time + the time offset.
     * Also sets the time for all controlled media players.
     *
     * @param time in msec.
     */
    @Override
    public void setMediaTime(long time) {
    	if (logger.isLoggable(Level.FINE)) {
    		logger.log(Level.FINE, "set media time {0} + offset {1}", new Object[] {
    				time, timeOffset });
    	}
        internalSetMediaTime(time);
    }

    private void internalSetMediaTime(long time) {
        player.setTime(time + timeOffset);
        setControllersMediaTime(time);
    }
    
    /**
     * nextFrame() doesn't work properly on all media files.
     * And even when it works, the media time is very imprecise.
     */
    @Override
    public void nextFrame() {
		if (player.isPlaying()) {
			stop();
		}
        player.nextFrame();
        setControllersMediaTime(getMediaTime());
        logger.log(Level.FINE, "time now {0}", player.getTime());
    }

    /** 
     * if true frame forward and frame backward always jump to the begin
	 * of the next/previous frame, otherwise it jumps with the frame duration.
	 */
	private boolean frameStepsToFrameBegin = false;

	/**
	 * Since time registration is very imprecise, stepping a frame back is not likely to work properly.
	 * But we can try...
	 */
    @Override
    public void previousFrame() {
        logger.log(Level.INFO, "Prev frame not supported natively.");
		if (player != null) {
			if (player.isPlaying()) {
				stop();
			}
	        
			double msecPerSample = getMilliSecondsPerSample();
			long curTime = getMediaTime();
			
	        if (frameStepsToFrameBegin) {
	        	long curFrame = (long)(curTime / msecPerSample);
	        	if (curFrame > 0) {
	        		internalSetMediaTime((long) Math.ceil((curFrame - 1) * msecPerSample));
	        	} else {
	        		internalSetMediaTime(0);
	        	}
	        } else {
	        	curTime = (long) Math.ceil(curTime - msecPerSample);
	        	
		        if (curTime < 0) {
		        	curTime = 0;
		        }
		
		        internalSetMediaTime(curTime);
	        }
		}
    }

    @Override
	public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
        frameStepsToFrameBegin = stepsToFrameBegin;
    }

    /**
     * Gets the current temporal scale factor.
     *
     * @return DOCUMENT ME!
     */
    @Override
    public float getRate() {
        return player.getRate();
    }

    /**
     * Sets the temporal scale factor.
     *
     * @param rate DOCUMENT ME!
     */
    @Override
    public void setRate(float rate) {
        player.setRate(rate);
        setControllersRate(rate);
    }

    /**
     * @see
     * mpi.eudico.client.annotator.player.ElanMediaPlayer#isFrameRateAutoDetected()
     */
    @Override
    public boolean isFrameRateAutoDetected() {
        return false;
    }

    /**
     * Get the duration of the media represented by this object in milli
     * seconds.
     *
     * @return DOCUMENT ME!
     */
    @Override
    public long getMediaDuration() {
        return player.getLength() - timeOffset;
    }

    /**
     * DOCUMENT ME!
     *
     * @param layoutManager DOCUMENT ME!
     */
    @Override
	public void setLayoutManager(ElanLayoutManager layoutManager) {
		this.layoutManager = layoutManager;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateLocale() {
    	if (popup != null) {
    		initPopupMenu();
    	}
    }

    /*
     * Popup menu stuff.
     * Thepopup menu is created as late as possible, since at the start
     * the media duration isn't known yet.
     */

    private void initPopupMenu() {
		if (player == null) {
			return;
		}
		popup = new JPopupMenu();
        detachItem = new JMenuItem(ElanLocale.getString("Detachable.detach"));
        detachItem.addActionListener(this);
		infoItem = new JMenuItem(ElanLocale.getString("Player.Info"));
        infoItem.addActionListener(this);
        durationItem = new JMenuItem(ElanLocale.getString("Player.duration") +
                ":  " + TimeFormatter.toString(getMediaDuration()));
        durationItem.setEnabled(false);
        saveItem = new JMenuItem(ElanLocale.getString("Player.SaveFrame"));
        saveItem.addActionListener(this);
        origRatioItem = new JRadioButtonMenuItem(ElanLocale.getString("Player.ResetAspectRatio"), true);
        origRatioItem.setActionCommand("ratio_orig");
        origRatioItem.addActionListener(this);
		ratio_1_1_Item = new JRadioButtonMenuItem("1:1");
		ratio_1_1_Item.setActionCommand("ratio_1_1");
		ratio_1_1_Item.addActionListener(this);
		ratio_5_4_Item = new JRadioButtonMenuItem("5:4");
		ratio_5_4_Item.setActionCommand("ratio_5_4");
		ratio_5_4_Item.addActionListener(this);
		ratio_4_3_Item = new JRadioButtonMenuItem("4:3");
		ratio_4_3_Item.setActionCommand("ratio_4_3");
		ratio_4_3_Item.addActionListener(this);
		ratio_16_10_Item = new JRadioButtonMenuItem("16:10");
		ratio_16_10_Item.setActionCommand("ratio_16_10");
		ratio_16_10_Item.addActionListener(this);
		ratio_16_9_Item = new JRadioButtonMenuItem("16:9");
		ratio_16_9_Item.setActionCommand("ratio_16_9");
		ratio_16_9_Item.addActionListener(this);
		ratio_221_1_Item = new JRadioButtonMenuItem("2.21:1");
		ratio_221_1_Item.setActionCommand("ratio_221_1");
		ratio_221_1_Item.addActionListener(this);
		ratio_235_1_Item = new JRadioButtonMenuItem("2.35:1");
		ratio_235_1_Item.setActionCommand("ratio_235_1");
		ratio_235_1_Item.addActionListener(this);
		ratio_239_1_Item = new JRadioButtonMenuItem("2.39:1");
		ratio_239_1_Item.setActionCommand("ratio_239_1");
		ratio_239_1_Item.addActionListener(this);
		arMenu = new JMenu(ElanLocale.getString("Player.ForceAspectRatio"));
		ButtonGroup arbg = new ButtonGroup();
		arbg.add(origRatioItem);
		arbg.add(ratio_1_1_Item);
		arbg.add(ratio_5_4_Item);
		arbg.add(ratio_4_3_Item);
		arbg.add(ratio_16_10_Item);
		arbg.add(ratio_16_9_Item);
		arbg.add(ratio_221_1_Item);
		arbg.add(ratio_235_1_Item);
		arbg.add(ratio_239_1_Item);
		arMenu.add(origRatioItem);
		arMenu.addSeparator();
		arMenu.add(ratio_1_1_Item);
		arMenu.add(ratio_5_4_Item);
		arMenu.add(ratio_4_3_Item);
		arMenu.add(ratio_16_10_Item);
		arMenu.add(ratio_16_9_Item);
		arMenu.add(ratio_221_1_Item);
		arMenu.add(ratio_235_1_Item);
		arMenu.add(ratio_239_1_Item);
		copyOrigTimeItem = new JMenuItem(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
		copyOrigTimeItem.addActionListener(this);

		zoomMenu = new JMenu(ElanLocale.getString("Menu.Zoom"));
		zoomOriginal = new JRadioButtonMenuItem("Reset", (videoScaleFactor == 0));
		zoomOriginal.setActionCommand("zoom0");
		zoomOriginal.addActionListener(this);
		zoom100 = new JRadioButtonMenuItem("100%", (videoScaleFactor == 1));
		zoom100.setActionCommand("zoom100");
		zoom100.addActionListener(this);
		zoom150 = new JRadioButtonMenuItem("150%", (videoScaleFactor == 1.5));
		zoom150.setActionCommand("zoom150");
		zoom150.addActionListener(this);
		zoom200 = new JRadioButtonMenuItem("200%", (videoScaleFactor == 2));
		zoom200.setActionCommand("zoom200");
		zoom200.addActionListener(this);
		zoom300 = new JRadioButtonMenuItem("300%", (videoScaleFactor == 3));
		zoom300.setActionCommand("zoom300");
		zoom300.addActionListener(this);
		zoom400 = new JRadioButtonMenuItem("400%", (videoScaleFactor == 4));
		zoom400.setActionCommand("zoom400");
		zoom400.addActionListener(this);

		ButtonGroup zbg = new ButtonGroup();
		zbg.add(zoomOriginal);
		zbg.add(zoom100);
		zbg.add(zoom150);
		zbg.add(zoom200);
		zbg.add(zoom300);
		zbg.add(zoom400);

		zoomMenu.add(zoomOriginal);
		zoomMenu.addSeparator();
		zoomMenu.add(zoom100);
		zoomMenu.add(zoom150);
		zoomMenu.add(zoom200);
		zoomMenu.add(zoom300);
		zoomMenu.add(zoom400);

		popup.add(detachItem);
        popup.addSeparator();
        popup.add(saveItem);
        popup.add(infoItem);
        popup.add(arMenu);
        popup.add(zoomMenu);
        popup.add(durationItem);
        popup.add(copyOrigTimeItem);
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
        logger.log(Level.INFO, "action performed {0}", e);
		if (e.getSource().equals(detachItem) && (layoutManager != null)) {
            if (detached) {
                layoutManager.attach(getVisualComponent());
                detachItem.setText(ElanLocale.getString("Detachable.detach"));
                detached = false;
            } else {
                layoutManager.detach(getVisualComponent());
                detachItem.setText(ElanLocale.getString("Detachable.attach"));
                detached = true;
            }
        } else if (e.getSource() == infoItem) {
            new FormattedMessageDlg(this);
        } else if (e.getSource() == saveItem) {
        	ImageExporter export = new ImageExporter();
        	export.exportImage(player.getVideoSurfaceContents());
        	// VLC has a built-in method to save a snapshot, where it chooses its now name.
//        	player.saveSnapshot();
        } else if (e.getActionCommand().startsWith("ratio")) {
	        if (e.getSource() == origRatioItem) {
				aspectRatio = origAspectRatio;
			} else if (e.getSource() == ratio_1_1_Item) {
				aspectRatio = 1.00f;
			} else if (e.getSource() == ratio_5_4_Item) {
				aspectRatio = 1.25f;
			} else if (e.getSource() == ratio_4_3_Item) {
				aspectRatio = 1.33f;
			} else if (e.getSource() == ratio_16_10_Item) {
				aspectRatio = 1.60f;
			} else if (e.getSource() == ratio_16_9_Item) {
				aspectRatio = 1.77f;
			} else if (e.getSource() == ratio_221_1_Item) {
				aspectRatio = 2.21f;
			} else if (e.getSource() == ratio_235_1_Item) {
				aspectRatio = 2.35f;
			} else if (e.getSource() == ratio_239_1_Item) {
				aspectRatio = 2.39f;
			}
	        setAspectRatio(aspectRatio);
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
        } else if (e.getActionCommand().startsWith("zoom")) {
			if (e.getSource() == zoomOriginal) {
				videoScaleFactor = 0f;
			} else if (e.getSource() == zoom100) {
				videoScaleFactor = 1f;
			} else if (e.getSource() == zoom150) {
				videoScaleFactor = 1.5f;
			} else if (e.getSource() == zoom200) {
				videoScaleFactor = 2f;
			} else if (e.getSource() == zoom300) {
				videoScaleFactor = 3f;
			} else if (e.getSource() == zoom400) {
				videoScaleFactor = 4f;
			}
			player.setScale(videoScaleFactor);
			layoutManager.setPreference(("VideoZoom(" + mediaDescriptor.mediaURL + ")"), 
					new Float(videoScaleFactor), layoutManager.getViewerManager().getTranscription());
        } else if (e.getSource() == copyOrigTimeItem) {
			long t = getMediaTime() + timeOffset;
			String timeFormat = Preferences.getString("CurrentTime.Copy.TimeFormat", null);
			String currentTime = null;
			
	        if (timeFormat != null) {
	        	if (timeFormat.equals(Constants.HHMMSSMS_STRING)) {
	            	currentTime = TimeFormatter.toString(t);
	            } else if (timeFormat.equals(Constants.SSMS_STRING)) {
	            	currentTime = TimeFormatter.toSSMSString(t);
	            } else if (timeFormat.equals(Constants.NTSC_STRING)) {
	            	currentTime = TimeFormatter.toTimecodeNTSC(t);
	            } else if (timeFormat.equals(Constants.PAL_STRING)) {
	            	currentTime = TimeFormatter.toTimecodePAL(t);
	            } else if (timeFormat.equals(Constants.PAL_50_STRING)) {
	            	currentTime = TimeFormatter.toTimecodePAL50(t);
	            } else {
	            	currentTime = Long.toString(t);
	            }
	        } else {
	        	currentTime = Long.toString(t);
	        }
	        copyToClipboard(currentTime);
		}
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

    protected class MouseHandler extends MouseAdapter {
    	@Override
        public void mousePressed(MouseEvent e) {
    		logger.log(Level.FINE, "mousePressed {0}", e);
        	
           	maybeShowPopup(e);
        }

        @Override
		public void mouseReleased(MouseEvent e) {
    		logger.log(Level.FINE, "mouseReleased {0}", e);
        	
           	maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            	// check the detached state, attaching can be done independently of the menu
	        	if (layoutManager.isAttached(VLCJMediaPlayer.this)) {
	        		if (detached) {
	        			detached = false;
	        			detachItem.setText(ElanLocale.getString("Detachable.detach"));
	        		}
	        	}
	        	//System.out.println("S: " + e.getSource() + " X: " + e.getX() +  " Y: " + e.getY());
	        	if (popup == null) {
	        		initPopupMenu();
	        	}
	            popup.show(playerComponent.getVideoSurface(), e.getX(), e.getY());
            }
        }
       
        /**
         * On a double click on the visual component of this player it will
         * become the first (largest) player.
         *
         * @param e the mouse event
         */
    	@Override
        public void mouseClicked(MouseEvent e) {
    		logger.log(Level.FINE, "mouseClicked {0}", e);
            if (e.getClickCount() >= 2) {
                if (layoutManager != null) {
                    layoutManager.setFirstPlayer(VLCJMediaPlayer.this);
                }

                return;
            }
            // Try to get mousePressed events; documentation suggests that this may be necessary.
            e.getComponent().requestFocus();
        }
    }

//    void printControls(Player player) {
//    }

    @Override
    public void cleanUpOnClose() {
        player.stop();
        playerComponent.release(true);
        player.release();
    }


    MediaPlayerEventListener infoListener = new MediaPlayerEventListener() {
            @Override
			public void mediaChanged(MediaPlayer mediaPlayer,
                    libvlc_media_t media, String mrl) {
                logger.log(Level.FINE, "media changed {0}", media);
            }

            @Override
			public void opening(MediaPlayer mediaPlayer) {
                logger.log(Level.FINE, "opening");
            }

            @Override
			public void buffering(MediaPlayer mediaPlayer,
                    float newCache) {
                logger.log(Level.FINER, "buffer");
            }

            @Override
			public void playing(MediaPlayer mediaPlayer) {
                logger.log(Level.FINE, "playing");
                if (msPerFrame <= 0) {
                    float fps = mediaPlayer.getFps();
                    if (fps > 1) {
                    	msPerFrame = (1000d / fps);
                        logger.log(Level.FINE, "player fps: {0}", fps);   
                    }
                }
            }

            @Override
			public void paused(MediaPlayer mediaPlayer) {
                logger.log(Level.FINE, "paused");
            }

            @Override
			public void stopped(MediaPlayer mediaPlayer) {
                logger.log(Level.FINE, "stopped");
            }

            @Override
			public void forward(MediaPlayer mediaPlayer) {
                logger.log(Level.FINE, "forward");
            }

            @Override
			public void backward(MediaPlayer mediaPlayer) {
                logger.log(Level.FINE, "backward");
            }

            @Override
			public void finished(MediaPlayer mediaPlayer) {
                logger.log(Level.FINE, "finished");
                // Make sure that a StopEvent gets sent (by the PeriodicUpdateController).
                VLCJMediaPlayer.this.stop();
                // It seems that after ending the media, it can't be played anymore.
                // Re-preparing the media seems rather a sledgehammer approach.
                VLCJMediaPlayer.this.player.stop();
                //VLCJMediaPlayer2.this.player.prepareMedia(VLCJMediaPlayer2.this.getMediaDescriptor().mediaURL);
            }

            @Override
			public void timeChanged(MediaPlayer mediaPlayer,
                    long newTime) {
                logger.log(Level.FINER, "time changed {0}", newTime);
                if (msPerFrame <= 0) {
                    float fps = mediaPlayer.getFps();
                    if (fps > 1) {
                    	msPerFrame = (1000d / fps);
                        logger.log(Level.INFO, "player fps: {0}", fps);   
                    }
                }
            }

            @Override
			public void positionChanged(MediaPlayer mediaPlayer,
                    float newPosition) {
                logger.log(Level.FINER, "position changed {0}", newPosition);
            }

            @Override
			public void seekableChanged(MediaPlayer mediaPlayer,
                    int newSeekable) {
                logger.log(Level.FINE, "seekable changed {0}", newSeekable);
            }

            @Override
			public void pausableChanged(MediaPlayer mediaPlayer,
                    int newSeekable) {
                logger.log(Level.FINE, "pausable changed {0}", newSeekable);
            }

            @Override
			public void titleChanged(MediaPlayer mediaPlayer,
                    int newTitle) {
                logger.log(Level.FINE, "title changed {0}", newTitle);
            }

            @Override
			public void snapshotTaken(MediaPlayer mediaPlayer,
                    String filename) {
                logger.log(Level.FINE, "snapshot taken {0}", filename);
            }

            @Override
			public void lengthChanged(MediaPlayer mediaPlayer,
                    long newLength) {
                logger.log(Level.FINE, "length changed {0}", newLength);
            }

            @Override
			public void videoOutput(MediaPlayer mediaPlayer,
                    int newCount) {
                logger.log(Level.FINE, "video output changed {0}", newCount);
            }

            @Override
			public void error(MediaPlayer mediaPlayer) {
                logger.log(Level.WARNING, "error");
            }

            @Override
			public void mediaMetaChanged(MediaPlayer mediaPlayer,
                    int metaType) {
                logger.log(Level.FINE, "media meta changed {0}", metaType);
            }

            @Override
			public void mediaSubItemAdded(MediaPlayer mediaPlayer,
                    libvlc_media_t subItem) {
                logger.log(Level.FINE, "media sub item added {0}", subItem);
            }

            @Override
			public void mediaDurationChanged(MediaPlayer mediaPlayer,
                    long newDuration) {
                logger.log(Level.FINE, "media duration changed {0}", newDuration);
            }

            @Override
			public void mediaParsedChanged(MediaPlayer mediaPlayer,
                    int newStatus) {
                logger.log(Level.FINE, "media parsed changed {0}", newStatus);
            }

            @Override
			public void mediaFreed(MediaPlayer mediaPlayer) {
                logger.log(Level.FINE, "media freed");
            }

            @Override
			public void mediaStateChanged(MediaPlayer mediaPlayer,
                    int newState) {
                logger.log(Level.FINE, "media state changed {0}", newState);
            }

            @Override
			public void newMedia(MediaPlayer mediaPlayer) {
                logger.log(Level.FINE, "new media");
            }

            @Override
			public void subItemPlayed(MediaPlayer mediaPlayer,
                    int subItemIndex) {
            }

            @Override
			public void subItemFinished(MediaPlayer mediaPlayer,
                    int subItemIndex) {
            }

            @Override
			public void endOfSubItems(MediaPlayer mediaPlayer) {
            }
            // For vlcj 3.x (which we don't use yet, because it requires
            // a vlc version that is too new):
            //@Override
            public void elementaryStreamSelected(MediaPlayer mp,int i1,int i2) {
            }
            //@Override
            public void elementaryStreamDeleted(MediaPlayer mp,int i1,int i2) {
            }
            //@Override
            public void elementaryStreamAdded(MediaPlayer mp,int i1,int i2) {
            }
            //@Override
            public void scrambledChanged(MediaPlayer mp,int i1) {
            }
        };

    private float subVolume;
    private boolean mute;
        
	@Override
	public void setSubVolume(float level) {
		subVolume = level;
	}

	@Override
	public float getSubVolume() {
		return subVolume;
	}

	@Override
	public void setMute(boolean mute) {
		this.mute = mute;
	}

	@Override
	public boolean getMute() {
		return mute;
	}

	@Override
	public void preferencesChanged() {
		// stub
	}
}
