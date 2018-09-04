package mpi.eudico.client.annotator.player;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FormattedMessageDlg;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.client.mediacontrol.PeriodicUpdateController;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;

import javax.media.EndOfMediaEvent;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.StopAtTimeEvent;
import javax.media.Time;
import javax.media.Control;
//import javax.media.control.*;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;


/**
 * The JMF implementation of an elan media player
 */

//public class JMFMediaPlayer extends ControllerManager implements ElanMediaPlayer, javax.media.ControllerListener {
public class JMFMediaPlayer extends ControllerManager implements ElanMediaPlayer,
    ControllerListener, javax.media.ControllerListener, ActionListener {
    /** Holds value of property DOCUMENT ME! */
    protected Player player; // the JMF player

    /** Holds value of property DOCUMENT ME! */
    protected float shouldBeRate; // used to remember the player rate since JMF sometimes forgets it

    /** Holds value of property DOCUMENT ME! */
    protected long intervalStopTime;

    /** Holds value of property DOCUMENT ME! */
    protected long frozenMediaTime;

    /** Holds value of property DOCUMENT ME! */
    protected long offset;

    /** Holds value of property DOCUMENT ME! */
    protected double milliSecondsPerSample;

    /** Holds value of property DOCUMENT ME! */
    protected boolean playingInterval;

    /** Holds value of property DOCUMENT ME! */
    protected PeriodicUpdateController periodicController;

    /** Holds value of property DOCUMENT ME! */
    protected float systemVolumeLevel;

    /** Holds value of property DOCUMENT ME! */
    protected JPopupMenu popup;

    /** Holds value of property DOCUMENT ME! */
    protected MediaDescriptor mediaDescriptor;

    /** Holds value of property DOCUMENT ME! */
    protected ElanLayoutManager layoutManager;

    /** Holds value of property DOCUMENT ME! */
    protected boolean detached;

    /** Holds value of property DOCUMENT ME! */
    protected JMenuItem durationItem;

    /** Holds value of property DOCUMENT ME! */
    protected JMenuItem detachItem;
    
    /** Holds the value of the "subvolume" property */
	protected float curSubVolume;
	protected boolean mute;

	private JMenuItem infoItem;

	private JMenuItem ratio_4_3_Item;
	private JMenuItem ratio_3_2_Item;
	private JMenuItem ratio_16_9_Item;
	private JMenuItem ratio_185_1_Item;
	private JMenuItem ratio_235_1_Item;
	
    /** Holds value of property DOCUMENT ME! */
    protected NoMouseComponent noMouseComponent;
	/** if true frame forward and frame backward always jump to the begin
	 * of the next/previous frame, otherwise it jumps with the frame duration */
	private boolean frameStepsToFrameBegin = false;
	private boolean pre47FrameStepping = false;
    
	private float aspectRatio = 0f;
    //protected FramePositioningControl fpc;

    //	private FramePositioningControl fpc;
	
	// static block to replace the sound renderer
	static {
		try {
			//FixedJavaSoundRenderer.replaceJavaSoundRenderer();
			final String OFFENDING_RENDERER_PLUGIN_NAME = com.sun.media.renderer.audio.JavaSoundRenderer.class.getName();
	        javax.media.Format[] rendererInputFormats = javax.media.PlugInManager.getSupportedInputFormats( 
	        		OFFENDING_RENDERER_PLUGIN_NAME, javax.media.PlugInManager.RENDERER );
	        javax.media.Format[] rendererOutputFormats = javax.media.PlugInManager.getSupportedOutputFormats( 
	        		OFFENDING_RENDERER_PLUGIN_NAME, javax.media.PlugInManager.RENDERER );
	        //should be only rendererInputFormats
	        if( rendererInputFormats != null || rendererOutputFormats != null ) {
	            final String REPLACEMENT_RENDERER_PLUGIN_NAME = FixedJavaSoundRenderer.class.getName();
	            javax.media.PlugInManager.removePlugIn( OFFENDING_RENDERER_PLUGIN_NAME, javax.media.PlugInManager.RENDERER );
	            javax.media.PlugInManager.addPlugIn( REPLACEMENT_RENDERER_PLUGIN_NAME, 
	            		rendererInputFormats, rendererOutputFormats, javax.media.PlugInManager.RENDERER );
	        } else {
	        	System.out.println("Cannot replace the SoundRenderer, no input and output formats.");
	        }
		} catch (Throwable tr) {
			System.out.println("Cannot replace the SoundRenderer: " + tr.getMessage());
		}
	}

    /**
     * Create a JMFMediaPlayer for a media URL
     *
     * @param mediaDescriptor DOCUMENT ME!
     *
     * @throws NoPlayerException DOCUMENT ME!
     */
    public JMFMediaPlayer(MediaDescriptor mediaDescriptor)
        throws NoPlayerException {
        this.mediaDescriptor = mediaDescriptor;

        // WebStart related initialization, see at the end of this file for details
        // this should maybe throw an exception? Is a loading error always fatal? 
        JMFClassLoader.initJMFJNI();

        // light weight in JMF is unusable because some mpegs can not be rendered in this mode
        //		Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, Boolean.valueOf(true));
        //		Manager.setHint(Manager.PLUGIN_PLAYER, Boolean.valueOf(true));
        String URLString = mediaDescriptor.mediaURL;

        //URLString = "file:///" + URLString.substring(i);
        //URLString = "file://Sun03/Corpora/media-archive/lac_data/senft/StagedEvents/Media/SE-1.mpg";
        //URLString = "rtsp://nt06.mpi.nl/onno/echo2a_6.mp4";
        //URLString = "rtsp://nt06.mpi.nl/SyncTest_hinted.mov";
        //URLString = "rtsp://nt06.mpi.nl:80/De_Eng.mpg";
        player = null;

        try {
            System.out.println("mediaURL = " + URLString);

            if (URLString.startsWith("rtsp")) {
                // do something for streaming
                System.out.println("stream");

                MediaLocator ml = new MediaLocator(URLString);
                player = Manager.createPlayer(ml);
            } else {
                URL mediaURL = new URL(URLString);
                player = Manager.createPlayer(mediaURL);
 //               player = (Player) Manager.createProcessor(mediaURL);
            }
        } catch (javax.media.NoPlayerException e) {
            System.out.println(
                "javax.media.NoPlayerException while creating JMF player");
            e.printStackTrace();
            throw new NoPlayerException("javax.media.NoPlayerException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO exception while creating JMF player");
            e.printStackTrace();
            throw new NoPlayerException(
                "IO exception, problem to connect to data source: " + e.getMessage());
        } catch (Exception e) {
        	System.out.println("Unknown exception while creating JMF player");
            e.printStackTrace();
			throw new NoPlayerException(
				"JMFMediaPlayer Exception: " + e.getMessage());
        } catch (Throwable tr) {
        	throw new NoPlayerException("JMFMediaPlayer Exception: " + tr.getMessage());
        }

        // initialize local parameters
        offset = mediaDescriptor.timeOrigin;
        shouldBeRate = 1.0f;
        frozenMediaTime = -1;
        milliSecondsPerSample = 40;

        // make sure the player is prefetched so the client code is not bothered with it
        // for .mpg a start stop sequence is needed to make setMediaTime possible before
        // the client code called player.start(). This is needed to be able to look for
        // a certain scene before the movie was started. It looks like JMF does not read
        // all the necessary info to do this when it prefetches a player on mpg data.
        // mute the player during start stop
        long timeout = System.currentTimeMillis() + 5000;
        player.realize();

        while (getState() != javax.media.Controller.Realized) {
            try {
                Thread.sleep(50);
                if (System.currentTimeMillis() > timeout) {
                	player.close();
                	throw new NoPlayerException("Could not realize the jmf player");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
 //       printControls(player);
        
 /*       fpc = (FramePositioningControl)player.getControl("javax.media.control.FramePositioningControl");
    	if (fpc == null) {
    	    System.err.println("The player does not support FramePositioningControl.");
    	} else {
    	    System.err.println("OK");
    	}
*/
        GainControl gain = player.getGainControl();
        float level = 1.0f;

        if (gain != null) {
            level = gain.getLevel();
            gain.setLevel(0);
        }

        systemVolumeLevel = level;
        start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stop();
        setMediaTime(0);

        if (gain != null) {
            gain.setLevel(level);
        }

        //		aspectRatio = (float) (getVisualComponent().getPreferredSize().getWidth() /
        //									getVisualComponent().getPreferredSize().getHeight());
        // this takes care of detecting the EndOfMediaEvent
        player.addControllerListener(this);

        if (player.getVisualComponent() != null) {
            player.getVisualComponent().addMouseListener(new MouseHandler());
        }

        //		fpc = (FramePositioningControl) player.getControl("javax.media.control.FramePositioningControl");
        popup = new JPopupMenu();
        durationItem = new JMenuItem(ElanLocale.getString("Player.duration") +
                ":  " + TimeFormatter.toString(getMediaDuration()));
        durationItem.setEnabled(false);
		infoItem = new JMenuItem(ElanLocale.getString("Player.Info"));
		infoItem.addActionListener(this);
		ratio_4_3_Item = new JMenuItem("4:3");
		ratio_4_3_Item.addActionListener(this);
		ratio_3_2_Item = new JMenuItem("3:2");
		ratio_3_2_Item.addActionListener(this);
		ratio_16_9_Item = new JMenuItem("16:9");
		ratio_16_9_Item.addActionListener(this);
		ratio_185_1_Item = new JMenuItem("1.85:1");
		ratio_185_1_Item.addActionListener(this);
		ratio_235_1_Item = new JMenuItem("2.35:1");
		ratio_235_1_Item.addActionListener(this);
		JMenu arMenu = new JMenu(ElanLocale.getString("Player.ForceAspectRatio"));
		arMenu.add(ratio_4_3_Item);
		arMenu.add(ratio_3_2_Item);
		arMenu.add(ratio_16_9_Item);
		arMenu.add(ratio_185_1_Item);
		arMenu.add(ratio_235_1_Item);
        popup.addSeparator();
        popup.add(infoItem);
        popup.add(arMenu);
        popup.add(durationItem);

        //		popup.add("duration : " + ViewerTools.toString(getMediaDuration())).setFont(Constants.SMALLFONT);
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
        return "Java Media Framework " + Manager.getVersion();
    }

    /**
     * Restore the system volume level, JMF destroys it
     */
    @Override
	public void finalize() {
    	if (player.getState() >= Player.Realized) {
    	
	        GainControl gain = player.getGainControl();
	
	        if (gain != null) {
	            gain.setLevel(systemVolumeLevel);
	        }
    	}
    }

    /**
     * JMF controllerUpdate
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public void controllerUpdate(javax.media.ControllerEvent event) {
        if (event instanceof EndOfMediaEvent) {
            // stops all the controllers and sets the media time
            stop();
        } else if (event instanceof StopAtTimeEvent) {
            System.out.println("stopped by jmf itself");
            frozenMediaTime = intervalStopTime;
            stop();
        }
    }

    /**
     * Elan controllerUpdate Used to stop at the stop time in cooperation with
     * the playInterval method
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public synchronized void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent) {
            if (periodicController != null) {
                if (getMediaTime() >= intervalStopTime) {
                    // freeze mediaTime
                    frozenMediaTime = intervalStopTime;

                    // remember the volume
                    float volume = getVolume();

                    // mute the player, makes the acoustic behaviour more deterministic
                    // because stop() does not stop instantaneously
                    setVolume(0);

                    // stop the player
                    stop();

                    //restore the volume
                    setVolume(volume);
                }
            }
        }
    }

    /**
     * play between two times. This method uses the contollerUpdate method to
     * detect if the stop time is passed. The setStopTime method of JMF can
     * not be used because it gives unstable behaviour
     *
     * @param startTime DOCUMENT ME!
     * @param stopTime DOCUMENT ME!
     */
    @Override
	public synchronized void playInterval(long startTime, long stopTime) {
        if ((player == null) || playingInterval || (stopTime <= startTime)) {
            return;
        }

        periodicController = new PeriodicUpdateController(25);
        periodicController.addControllerListener(this);
        addController(periodicController);
        intervalStopTime = stopTime + offset;
        setMediaTime(startTime);
        playingInterval = true;

        //player.setStopTime(new Time(1000000 * stopTime));
        start();
    }
    
    /**
     * Empty implementation for ElanMediaPlayer Interface
     * Only usefull for player that corectly supports setting stop time
     */
    @Override
	public void setStopTime(long stopTime) {
    	
    }
    
    /**
     * Disable all code for interval playing
     */
    private void stopPlayingInterval() {
        if (periodicController != null) {
            periodicController.removeControllerListener(this);
            removeController(periodicController);
            periodicController = null;
        }

        playingInterval = false;
    }

    /**
     * Gets the display Component for this Player.
     *
     * @return DOCUMENT ME!
     */
    @Override
	public java.awt.Component getVisualComponent() {
        if (player == null) {
            return null;
        }

        //		if (noMouseComponent == null) {
        //			noMouseComponent = new NoMouseComponent();
        //			noMouseComponent.add(player.getVisualComponent());
        //		}
        //		return noMouseComponent;
        return player.getVisualComponent();
    }

    /**
     * The only way to get to the dimension of the video image seems to be through the preferred
     * size ofthe visual component.(?)
     * 
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceHeight()
     */
    @Override
	public int getSourceHeight() {
        if (getVisualComponent() != null) {
            return (int) getVisualComponent().getPreferredSize().getHeight();
        }
        
        return 0;
    }
    
    /**
     * The only way to get to the dimension of the video image seems to be through the preferred
     * size ofthe visual component.(?)
     * 
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceWidth()
     */
    @Override
	public int getSourceWidth() {
        if (getVisualComponent() != null) {
            return (int) getVisualComponent().getPreferredSize().getWidth();
        }
        
        return 0;
    }
    
    /**
     * Gets the control Component for this Player. Necessary for CorexViewer.
     * A.K.
     *
     * @return DOCUMENT ME!
     */
    public java.awt.Component getControlPanelComponent() {
        if (player == null) {
            return null;
        }

        return player.getControlPanelComponent();
    }

    /**
     * Gets the ratio between width and height of the video image
     *
     * @return DOCUMENT ME!
     */
    @Override
	public float getAspectRatio() {
        if (player == null || getVisualComponent() == null) {
            return 0;
        }

        if (aspectRatio == 0f) {
        	aspectRatio = (float) (getVisualComponent().getPreferredSize()
                                         .getWidth() / getVisualComponent()
                                                           .getPreferredSize()
                                                           .getHeight());
        }

        return aspectRatio;
    }
    
    /**
     * Enforces an aspect ratio for the media component.
     * 
     * @param aspectRatio the new aspect ratio
     */
    @Override
	public void setAspectRatio(float aspectRatio) {
    	this.aspectRatio = aspectRatio;
    }

    /**
     * Starts the Player as soon as possible. is not synchronized in JMF
     */
    @Override
	public synchronized void start() {
        if ((player == null) || (getState() == javax.media.Controller.Started)) {
            return;
        }

        // do not try to start at the end of the media, the JMF player blocks
        // start playing at the beginning of the media data
        if ((getMediaDuration() - getMediaTime()) < 40) {
            setMediaTime(0);
        }

        // tell JMF to start the player
        player.start();

        // wait for the player to be in the started state
        while (getState() != javax.media.Controller.Started) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // make sure all managed controllers are started
        startControllers();
    }

    /**
     * Stop the media player
     */
    @Override
	public synchronized void stop() {
        if (player == null) {
            return;
        }

        // only freeze media time if it is not frozen yet
        if (frozenMediaTime < 0) {
            frozenMediaTime = getMediaTime();
        }

        // make sure all managed controllers are stopped
        stopControllers();

        // tell JMF to stop the player
        player.stop();

        // wait for the player to stop
        while (player.getState() == javax.media.Controller.Started) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // make sure that all interval playing is finished
        if (playingInterval) {
            stopPlayingInterval();
        }

        // make sure the player is at the correct time after this stop
        setMediaTime(frozenMediaTime);

        // undefine the freeze time
        frozenMediaTime = -1;

        // the following code is needed because JMF sometimes forgets its player rate
        // First an other rate than the current (shouldBe) rate must be set because
        // the JMF player thinks it is playing at the shouldBeRate and ignores calls
        // to setRate for the rate value it thinks it is playing at.
        player.setRate(0.995f * shouldBeRate);
        player.setRate(shouldBeRate);
    }

    /**
     * Tell if this player is playing
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isPlaying() {
        if (player == null) {
            return false;
        }

        return getState() == javax.media.Controller.Started;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the step size for one frame
     */
    @Override
	public double getMilliSecondsPerSample() {
        return milliSecondsPerSample;
    }

    /**
     * DOCUMENT ME!
     *
     * @param milliSeconds the step size for one frame
     */
    @Override
	public void setMilliSecondsPerSample(long milliSeconds) {
        milliSecondsPerSample = milliSeconds;
    }

    /**
     * Return the players state
     *
     * @return DOCUMENT ME!
     */
    private int getState() {
        if (player == null) {
            return javax.media.Controller.Prefetched;
        }

        return player.getState();
    }

    /**
     * Gets the volume as a number between 0 and 1
     *
     * @return DOCUMENT ME!
     */
    @Override
	public float getVolume() {
        if (player.getGainControl() == null) {
            return 0;
        }

        return player.getGainControl().getLevel();
    }

    /**
     * Sets the volume as a number between 0 and 1
     *
     * @param level a number between 0 and 1
     */
    @Override
	public void setVolume(float level) {
        if (player.getGainControl() == null) {
            return;
        }

        try {
        	player.getGainControl().setLevel(level);
        } catch (IllegalArgumentException iae) {
        	System.out.println("Illegal volume level: " + level);
        }
    }

    @Override
    public void setSubVolume(float level) {
    	curSubVolume = level;
    }
    
    @Override
    public float getSubVolume(){
    	return curSubVolume;
    }
    
    @Override
    public void setMute(boolean mute) {
    	this.mute = mute;
    }
    
    @Override
    public boolean getMute() {
    	return mute;
    }
    
    /**
     * Set the offset to be used in get and set media time for this player
     *
     * @param offset the offset in milli seconds
     */
    @Override
	public void setOffset(long offset) {
        this.offset = offset;
        mediaDescriptor.timeOrigin = offset;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the offset used by this player
     */
    @Override
	public long getOffset() {
        return offset;
    }

    /**
     * Sets the Clock's media time in milli seconds. is not synchronized in JMF
     *
     * @param time DOCUMENT ME!
     */
    @Override
	public synchronized void setMediaTime(long time) {
        if (player == null) {
            return;
        }

        // do not set media time on a started player
        if (isPlaying()) {
            stop();
        }

        time = time + offset;
        player.setMediaTime(new Time(time * 1000000));

        // set the media time for the connected controllers
        setControllersMediaTime(time - offset);
    }

    @Override
	public void nextFrame() {
    	if (frameStepsToFrameBegin) {
    		long curFrame = (long) (getMediaTime() / milliSecondsPerSample);
    		setMediaTime((long) Math.ceil((curFrame + 1) * milliSecondsPerSample));
    	} else {
    		setMediaTime((long) Math.ceil(getMediaTime() + getMilliSecondsPerSample()));
    	}
    }
    
    @Override
	public void previousFrame() {
        if (pre47FrameStepping) {
        	nextFramePre47();
        	return;
        }
        
    	if (frameStepsToFrameBegin) {
    		long curFrame = (long) (getMediaTime() / milliSecondsPerSample);
    		if (curFrame > 0) {
    			setMediaTime((long) Math.ceil((curFrame - 1) * milliSecondsPerSample));
    		} else {
    			setMediaTime(0);
    		}
    	} else {
    		setMediaTime((long) Math.ceil(getMediaTime() - getMilliSecondsPerSample()));
    	}
    }
    
    /**
     * The pre 4.7 implementation of next frame.
     */
    private void nextFramePre47() {
		if (pre47FrameStepping) {
			previousFramePre47();
			return;
		}
		
    	long mspsLong = (long) milliSecondsPerSample;
    	if (frameStepsToFrameBegin) {
    		long curFrame = getMediaTime() / mspsLong;
    		setMediaTime((curFrame + 1) * mspsLong);
    	} else {
    		setMediaTime(getMediaTime() + mspsLong);
    	}
    }
    
    /**
     * The previous implementation of previous frame, with (more) rounding effects.
     */
    private void previousFramePre47() {
    	long mspsLong = (long) milliSecondsPerSample;
    	if (frameStepsToFrameBegin) {
    		long curFrame = getMediaTime() / mspsLong;
    		if (curFrame > 0) {
    			setMediaTime((curFrame - 1) * mspsLong);
    		} else {
    			setMediaTime(0);
    		}
    	} else {
    		setMediaTime(getMediaTime() - mspsLong);
    	}
    }

    @Override
	public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
    	frameStepsToFrameBegin = stepsToFrameBegin;
    }
    
    /**
     * Gets this Clock's current media time in milli seconds.
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getMediaTime() {
        if (player == null) {
            return 0;
        }

        // return frozen media time if it is set
        if (frozenMediaTime >= 0) {
            return frozenMediaTime;
        }

        return (long) (1000 * player.getMediaTime().getSeconds()) - offset;
    }

    /**
     * Gets the current temporal scale factor.
     *
     * @return DOCUMENT ME!
     */
    @Override
	public float getRate() {
        if (player == null) {
            return 0;
        }

        return player.getRate();
    }

    /**
     * Sets the temporal scale factor.
     *
     * @param rate DOCUMENT ME!
     */
    @Override
	public synchronized void setRate(float rate) {
        if (player == null) {
            return;
        }

        // do not set rate on a started player
        if (isPlaying()) {
            stop();
        }

        // set the rate for the connected controllers
        setControllersRate(rate);

        shouldBeRate = rate;
        player.setRate(rate);
    }

    
    /**
     * The encoded frame duration is not (yet?) detected by JMF, or it is not yet implemented 
     * in this player.
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#isFrameRateAutoDetected()
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
        if (player == null) {
            return 0;
        }

        return (long) (1000 * player.getDuration().getSeconds()) - offset;
    }

    /**
     * DOCUMENT ME!
     *
     * @param layoutManager DOCUMENT ME!
     */
    @Override
	public void setLayoutManager(ElanLayoutManager layoutManager) {
        if (this.layoutManager == null) {
            detachItem = new JMenuItem(ElanLocale.getString("Detachable.detach"));
            detachItem.addActionListener(this);
            popup.insert(detachItem, 0);
        }

        this.layoutManager = layoutManager;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateLocale() {
		infoItem.setText(ElanLocale.getString("Player.Info"));
        durationItem.setText(ElanLocale.getString("Player.duration") + ":  " +
            TimeFormatter.toString(getMediaDuration()));

        if (detachItem != null) {
            if (detached) {
                detachItem.setText(ElanLocale.getString("Detachable.attach"));
            } else {
                detachItem.setText(ElanLocale.getString("Detachable.detach"));
            }
        }
    }

    /*
     *
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(detachItem) && (layoutManager != null)) {
            if (detached) {
                layoutManager.attach(JMFMediaPlayer.this.getVisualComponent());
                detachItem.setText(ElanLocale.getString("Detachable.detach"));
                detached = false;
            } else {
                layoutManager.detach(JMFMediaPlayer.this.getVisualComponent());
                detachItem.setText(ElanLocale.getString("Detachable.attach"));
                detached = true;
            }
        } else if (e.getSource() == infoItem) {
			new FormattedMessageDlg(this);
		} else if (e.getSource() == ratio_4_3_Item) {
			aspectRatio = 1.33f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
		} else if (e.getSource() == ratio_3_2_Item) {
			aspectRatio = 1.66f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
		} else if (e.getSource() == ratio_16_9_Item) {
			aspectRatio = 1.78f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
		} else if (e.getSource() == ratio_185_1_Item) {
			aspectRatio = 1.85f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());			
		} else if (e.getSource() == ratio_235_1_Item) {
			aspectRatio = 2.35f;
			layoutManager.doLayout();
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
		}
    }
    
    @Override
    public void preferencesChanged() {
    	Boolean val = Preferences.getBool("MediaNavigation.Pre47FrameStepping", null);
    	
    	if (val != null) {
    		pre47FrameStepping = val;
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
    
    // Code coppied from Greg's version of JMFVideoPlayer
    // ALex heeft deze code verbeterd in de client/util package, die code gaan gebruiken zodra de nieywe package structuur er is

 

    /**
     * DOCUMENT ME!
     * $Id: JMFMediaPlayer.java 44069 2015-07-20 08:44:03Z olasei $
     * @author $Author$
     * @version $Revision$
     */
    private class NoMouseComponent extends java.awt.Container {
        /**
         * DOCUMENT ME!
         *
         * @param ml DOCUMENT ME!
         */
        @Override
		public void addMouseListener(java.awt.event.MouseListener ml) {
            // no mouselistener for you!
        }
    }

    /**
     * DOCUMENT ME!
     * $Id: JMFMediaPlayer.java 44069 2015-07-20 08:44:03Z olasei $
     * @author $Author$
     * @version $Revision$
     */
    protected class MouseHandler extends MouseAdapter {
    	private final DecimalFormat format = new DecimalFormat("#.###");
    	
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        @Override
		public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            	if (detachItem != null && layoutManager != null) {
            		if (layoutManager.isAttached(JMFMediaPlayer.this)) {
                		if (detached) {
                			detached = false;
                			detachItem.setText(ElanLocale.getString("Detachable.detach"));
                		}
            		}
            	}
                popup.show(player.getVisualComponent(), e.getPoint().x,
                    e.getPoint().y);
            }
        }
		
		/**
		 * On a double click on the visual component of this player it will become 
		 * the first (largest) player.
		 * @param e the mouse event
		 */
		@Override
		public void mouseClicked(java.awt.event.MouseEvent e) {
			if (e.getClickCount() >= 2) {
				if (layoutManager != null) {
					layoutManager.setFirstPlayer(JMFMediaPlayer.this);
				}
				return;
			}
			if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
				return;
			}
			//System.out.println("X: " + e.getX() + " Y: " + e.getY());
           
            try {
                //System.out.println("OW: " + getVisualComponent().getPreferredSize().getWidth()
                //		+ " OH: " + getVisualComponent().getPreferredSize().getHeight());
                if (e.isAltDown()) {
                	   copyToClipboard(format.format(e.getX() / (float)getVisualComponent().getWidth()) + "," 
                			   + format.format(e.getY() / (float)getVisualComponent().getHeight()));
                }  else if (e.isShiftDown()){
                    copyToClipboard("" + (int)((getVisualComponent().getPreferredSize().getWidth() / (float)getVisualComponent().getWidth()) * e.getX()) 
                		    + "," + (int)((getVisualComponent().getPreferredSize().getHeight() / (float)getVisualComponent().getHeight()) * e.getY()));
                } else {
                    copyToClipboard("" + (int)((getVisualComponent().getPreferredSize().getWidth() / (float)getVisualComponent().getWidth()) * e.getX()) 
                		    + "," + (int)((getVisualComponent().getPreferredSize().getHeight() / (float)getVisualComponent().getHeight()) * e.getY())
                		    + " [" + (int)getVisualComponent().getPreferredSize().getWidth() + "," + (int)getVisualComponent().getPreferredSize().getHeight() + "]");
                }
            } catch (Exception exep) {
            	   exep.printStackTrace();
            }
		}
    }
    
    void printControls(Player player) {
        System.out.println("Player info: " + player);

        Control[] controls = player.getControls();

        for (int i = 0; i < controls.length; i++) {
            System.out.print("\t" + i + ": ");
            System.out.println(controls[i].getClass());
/*
            if (controls[i] instanceof FormatControl) {
                FormatControl fc = (FormatControl) controls[i];
                System.out.println("\tFormatcontrol format: " + fc.getFormat());
                System.out.println("\tControl Component: " +
                    fc.getControlComponent());
            }

            if (controls[i] instanceof FrameGrabbingControl) {
                System.out.println("\t - is framgrabbing control");
            }

            if (controls[i] instanceof FramePositioningControl) {
                System.out.println("\t - is framepositioning control");
            }*/
        }
    }

	@Override
	public void cleanUpOnClose() {
		
		
	}
}
