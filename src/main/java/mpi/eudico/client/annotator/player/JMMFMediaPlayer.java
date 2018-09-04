package mpi.eudico.client.annotator.player;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;

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
import mpi.eudico.client.annotator.PreferencesListener;
import mpi.eudico.client.annotator.export.ImageExporter;
import mpi.eudico.client.annotator.gui.FormattedMessageDlg;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;
import nl.mpi.jmmf.DIBInfoHeader;
import nl.mpi.jmmf.JMMFException;
import nl.mpi.jmmf.JMMFPanel;
import nl.mpi.jmmf.JMMFPlayer;

/**
 * An ELAN player that wraps a JMMFPlayer, a Java based player that uses
 * the Microsoft Media Foundation for media playback. Only available on
 * Vista and Windows 7, mp4 support on Windows 7 only.
 * 
 * @author Han Sloetjes
 */
public class JMMFMediaPlayer extends ControllerManager implements
		ElanMediaPlayer, ControllerListener, VideoFrameGrabber, HasAsynchronousNativePlayer,
		ActionListener, PreferencesListener {
	private JMMFPlayer jmmfPlayer;
	private JMMFPanel jmmfPanel;
//	private JMMFCanvas jmmfPanel;
	private MediaDescriptor mediaDescriptor;
	private long offset = 0L;
	private long stopTime;
	private long duration;// media duration minus offset
	private long origDuration;// the original media duration
	// end of media buffer, don't set stop time or media time to 
	// the end of the media because then the media jumps to 0
	private long eomBuffer = 0;
	private int numFramesEOMBuffer = 5;
	private float origAspectRatio = 0;
	private float aspectRatio = 0;
	private double millisPerSample;
	//private boolean playing;
	private PlayerStateWatcher stopThread = null;
	private EndOfTimeWatcher endTimeWatcher = null;
	
	private boolean isInited = false;
	private boolean initWaitThreadStarted = false;
	private float cachedVolume = 1.0f;
	private float cachedRate = 1.0f;
	private float curSubVolume;
	private boolean mute;
	
    private boolean frameRateAutoDetected = true;
	/** if true frame forward and frame backward always jump to the begin
	 * of the next/previous frame, otherwise it jumps with the frame duration */
	private boolean frameStepsToFrameBegin = false;
	private boolean pre47FrameStepping = false;
	// gui
	private ElanLayoutManager layoutManager;
    private JPopupMenu popup;
    private JMenuItem durationItem;
    protected JMenuItem detachItem;
    private JMenuItem infoItem;
	private JMenuItem saveItem;
	private JMenu arMenu;
	private JRadioButtonMenuItem origRatioItem;
	private JRadioButtonMenuItem ratio_4_3_Item;
	private JRadioButtonMenuItem ratio_3_2_Item;
	private JRadioButtonMenuItem ratio_16_9_Item;
	private JRadioButtonMenuItem ratio_185_1_Item;
	private JRadioButtonMenuItem ratio_235_1_Item;
	private JMenuItem copyOrigTimeItem;
	private boolean detached;
	private JMenu zoomMenu;
	private JRadioButtonMenuItem zoom100;
	private JRadioButtonMenuItem zoom150;
	private JRadioButtonMenuItem zoom200;
	private JRadioButtonMenuItem zoom300;
	private JRadioButtonMenuItem zoom400;
	//private boolean allowVideoScaling = true;
	private float videoScaleFactor = 1f;
	//private int vx = 0, vy = 0, vw = 0, vh = 0;
	private int dragX = 0, dragY = 0;
	private int SET_MT_TIMEOUT = 1000;
	
	private final ReentrantLock syncLock = new ReentrantLock();
	/** a flag for recording and checking if the player is currently playing
	 *  an interval. The asynchronous native player does not reach the paused
	 *  state immediately after it was paused. */
	private boolean isPlayingInterval = false;
	
	/**
	 * Constructor.
	 * 
	 * @param mediaDescriptor
	 * @throws NoPlayerException
	 */
	public JMMFMediaPlayer(MediaDescriptor mediaDescriptor) throws NoPlayerException {
		this.mediaDescriptor = mediaDescriptor;
		offset = mediaDescriptor.timeOrigin;
		
        String urlString = mediaDescriptor.mediaURL;
        if (urlString.startsWith("file:") &&
                !urlString.startsWith("file:///")) {
            urlString = urlString.substring(5);
        }
        // check the EndOfMedia buffer property
        String eomFramesProp = System.getProperty("JMMFPlayer.EndOfMedia.NumberOfFramesBuffer");
        if (eomFramesProp != null) {
        	try {
        		numFramesEOMBuffer = Integer.parseInt(eomFramesProp);
        	} catch (NumberFormatException nfe) {
        		ClientLogger.LOG.warning("There is a 'EndOfMedia.NumberOfFramesBuffer' property but its value can not be parsed");
        	}
        }
        try {
        	boolean synchronousMode = false;
        	Boolean synVal = Preferences.getBool("Windows.JMMFPlayer.SynchronousMode", null);
        	if (synVal != null) {
        		synchronousMode = synVal;
        	}
        	
        	jmmfPlayer = new JMMFPlayer(urlString, synchronousMode);
        	if (jmmfPlayer.isVisualMedia()) {
        		jmmfPanel = new JMMFPanel(jmmfPlayer);
//        		jmmfPanel = new JMMFCanvas(jmmfPlayer);
        		initPopupMenu();
        		MouseHandler mh = new MouseHandler();
        		jmmfPanel.addMouseListener(mh);
        		jmmfPanel.addMouseMotionListener(mh);
        		
        		Boolean boolPref = Preferences.getBool("Windows.JMMFPlayer.CorrectAtPause", null);
        		
        		if (boolPref != null) {
        			JMMFPlayer.correctAtPause(boolPref);
        		}
        	}        	
        	// cannot get info from the player yet
        } catch (JMMFException je) {
        	throw new NoPlayerException("JMMFPlayer cannot handle the file: " + je.getMessage());
        } catch (Throwable tr) {
        	throw new NoPlayerException("JMMFPlayer cannot handle the file: " + tr.getMessage());
        }
	}
	
	@Override
	public void cleanUpOnClose() {
		if (jmmfPlayer != null) {
			if (endTimeWatcher != null) {
				endTimeWatcher.close();
			}
			if (jmmfPanel != null) {
				jmmfPanel.setPlayer(null);
			}
			
			jmmfPlayer.cleanUpOnClose();
			jmmfPlayer = null;//make sure no more calls are made to this player
		}

	}

	/**
	 * Returns the aspect ratio.
	 */
	@Override
	public float getAspectRatio() {
		if (aspectRatio != 0) {
			return aspectRatio;
		}
		if (jmmfPlayer != null) {
			if (origAspectRatio == 0) {
				origAspectRatio = jmmfPlayer.getAspectRatio();
			}
			aspectRatio = origAspectRatio;
			
			// initialization problem?
			if (jmmfPlayer.isVisualMedia() && jmmfPlayer.getAspectRatio() == 0) {
				ClientLogger.LOG.warning("The aspect ratio is not initialized yet");
				return 1;
			}
		}
		return aspectRatio;
	}

	@Override
	public String getFrameworkDescription() {
		if (jmmfPlayer != null && jmmfPlayer.isSynchronousMode()) {
			return "JMMF - Java with Microsoft Media Foundation Player (Synchronous Mode)";
		}
		return "JMMF - Java with Microsoft Media Foundation Player";
	}

	@Override
	public MediaDescriptor getMediaDescriptor() {
		return mediaDescriptor;
	}

	/**
	 * Gets the duration from the player (and stores it locally).
	 * @return the media duration in ms
	 */
	@Override
	public long getMediaDuration() {
		if (duration <= 0) {
			if (jmmfPlayer != null) {
				if (origDuration == 0) {
					origDuration = jmmfPlayer.getDuration();					
				}
				duration = origDuration - offset;
			}
		}
		return duration;
	}

	/**
	 * Returns the current media time, in ms and corrected for the offset.
	 */
	@Override
	public long getMediaTime() {
		if (jmmfPlayer != null) {
			return jmmfPlayer.getMediaTime() - offset;
		}
		return 0;
	}

	/**
	 * Retrieves the duration per sample (and caches it locally).
	 */
	@Override
	public double getMilliSecondsPerSample() {
		if (millisPerSample == 0.0) {
			if (jmmfPlayer != null) {
				millisPerSample = jmmfPlayer.getTimePerFrame();
				if (millisPerSample == 0.0) {
					millisPerSample = 40.0;
					frameRateAutoDetected = false;
				}
			}
		}
		return millisPerSample;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public float getRate() {
		if (jmmfPlayer != null) {
			return jmmfPlayer.getRate();
		}
		return 1;
	}

	@Override
	public int getSourceHeight() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isVisualMedia() && jmmfPlayer.getSourceHeight() <= 1) {
				ClientLogger.LOG.warning("Height not initialized yet");
				return 100;
			}
			return jmmfPlayer.getSourceHeight();
		}
		return 0;
	}

	@Override
	public int getSourceWidth() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isVisualMedia() && jmmfPlayer.getSourceWidth() <= 1) {
				ClientLogger.LOG.warning("Width not initialized yet");
				return 100;
			}
			return jmmfPlayer.getSourceWidth();
		}
		return 0;
	}

	/**
	 * After the first time this is called the panel will be added to a window,
	 * upon which the player will be initialized fully.
	 */
	@Override
	public Component getVisualComponent() {
		if (!isInited && !initWaitThreadStarted) {
			initWaitThreadStarted = true;
			new InitWaitThread().start();
		}
		return jmmfPanel;
	}

	@Override
	public float getVolume() {
		if (jmmfPlayer != null) {
			return jmmfPlayer.getVolume();
		}
		return 0f;
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
    
    @Override
	public boolean isFrameRateAutoDetected() {
		return frameRateAutoDetected;
	}

	@Override
	public boolean isPlaying() {
		if (jmmfPlayer != null) {
			return jmmfPlayer.isPlaying();
			//return playing;
			//return jmmfPlayer.getState() == JMMFPlayer.PlayerState.STARTED.value;
		}
		return false;
	}

	@Override
	public void nextFrame() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			
	        if (pre47FrameStepping) {
	        	nextFramePre47();
	        	return;
	        }
	        
			double nextTime = jmmfPlayer.nextFrame(frameStepsToFrameBegin);
			
			if (!jmmfPlayer.isSynchronousMode()) {
	        	long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}

					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
			}
			
			setControllersMediaTime((long) Math.ceil(nextTime) - offset);
			/*
	        if (frameStepsToFrameBegin) {
	        	long curFrame = (long)(getMediaTime() / millisPerSample);
	    		setMediaTime((long) Math.ceil((curFrame + 1) * millisPerSample));
	        } else {
	        	long curTime = jmmfPlayer.getMediaTime();
	        	//System.out.println("Current time: " + curTime);
	        	//curTime += millisPerSample;
	        	curTime = (long) Math.ceil(curTime + millisPerSample);
	        	//System.out.println("Current time 2: " + curTime);
	        	jmmfPlayer.setMediaTime(curTime);
	        	if (!jmmfPlayer.isSynchronousMode()) {
		        	long sysTime = System.currentTimeMillis();
					while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
						try {
							Thread.sleep(5);
						} catch (InterruptedException ie){}
	
						if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
							break;
						}
					}
	        	}
				//jmmfPlayer.repaintVideo();
	        	setControllersMediaTime(curTime - offset);
	        }
	        */
		}
	}
	
    /**
     * The pre 4.7 implementation of next frame.
     */
    private void nextFramePre47() {
    	// assumes a check for jmmfPlayer != null and player == paused has been performed 
        if (frameStepsToFrameBegin) {
        	long curFrame = (long)(getMediaTime() / millisPerSample);
    		setMediaTime((long)((curFrame + 1) * millisPerSample));
        } else {
        	long curTime = jmmfPlayer.getMediaTime();
        	//System.out.println("Current time: " + curTime);
        	curTime += millisPerSample;
        	//System.out.println("Current time 2: " + curTime);
        	jmmfPlayer.setMediaTime(curTime);
        	if (!jmmfPlayer.isSynchronousMode()) {
	        	long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}
	
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
        	}
			//jmmfPlayer.repaintVideo();
        	setControllersMediaTime(curTime - offset);
        }
    }

	@Override
	public void playInterval(long startTime, long stopTime) {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			syncLock.lock();
			try {
				setStopTime(stopTime);
				if (getMediaTime() != startTime + offset) {
					setMediaTimeAndWait(startTime);
				}
				
				startInterval();
			} finally {
				syncLock.unlock();
			}
		}

	}
	
	void startInterval() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isPlaying()) {
				return;
			}

	        // create a PlayerEndWatcher thread
	        if (stopThread != null && stopThread.isAlive()) {
	        	stopThread.setStopped();
	        }
	        int sleepTime = 200;	        
	        if (jmmfPlayer.isSynchronousMode()) {
	        	sleepTime = 20;
	        }
	        
	        stopThread = new PlayerStateWatcher(sleepTime);
	        isPlayingInterval = true;
	        if (jmmfPlayer.isSynchronousMode()) {
	        	jmmfPlayer.start();
	        	startControllers();
	        	stopThread.start();
	        } else {
		        stopThread.start();		        
		        jmmfPlayer.start();
		        startControllers();
	        }
		}
	}
	
	/**
	 * @return the value of the isPlayingInterval flag, which is set to false at the 
	 * very end of a play interval action, taking into account the asynchronous behavior
	 * of the native media player.
	 * 
	 * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#isPlayingInterval()
	 */
	@Override
	public boolean isPlayingInterval() {
		return isPlayingInterval;
	}

	@Override
	public void previousFrame() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			
			if (pre47FrameStepping) {
				previousFramePre47();
				return;
			}
			
			double prevTime = jmmfPlayer.previousFrame(frameStepsToFrameBegin);
			
			if (!jmmfPlayer.isSynchronousMode()) {
	        	long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}

					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
			}
			
			setControllersMediaTime((long) Math.ceil(prevTime) - offset);
		}

	}
	
    /**
     * The previous implementation of previous frame, with (more) rounding effects.
     */
    private void previousFramePre47() {
    	// assumes this is checked: player != null and is paused
        if (frameStepsToFrameBegin) {
        	long curFrame = (long)(getMediaTime() / millisPerSample);
        	if (curFrame > 0) {
        		setMediaTime((long)((curFrame - 1) * millisPerSample));
        	} else {
        		setMediaTime(0);
        	}
        } else {
        	long curTime = jmmfPlayer.getMediaTime();
        	curTime -= millisPerSample;
        	
	        if (curTime < 0) {
	        	curTime = 0;
	        }
	
	        jmmfPlayer.setMediaTime(curTime);
	        if (!jmmfPlayer.isSynchronousMode()) {
		        long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}
					
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
	        }
        	setControllersMediaTime(curTime - offset);
        }	
    }

	@Override
	public void setAspectRatio(float aspectRatio) {
		this.aspectRatio = aspectRatio;
		// hier update the visual component
	}

	@Override
	public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
		this.frameStepsToFrameBegin = stepsToFrameBegin;
	}

	@Override
	public void setLayoutManager(ElanLayoutManager layoutManager) {
		this.layoutManager = layoutManager;
		if (this.layoutManager != null) {
			detached = !(this.layoutManager.isAttached(this));
		}
	}

	@Override
	public void setMediaTime(long time) {
		if (jmmfPlayer != null) {
			// works a bit better than just setting the position
//			if (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value){
//				return;
//			}
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			if (time < 0) {
				time = 0;
			}
			if (time > duration - eomBuffer) {
				time = duration - eomBuffer;
			}

			// blocking
			jmmfPlayer.setMediaTime(time + offset);
			if (!jmmfPlayer.isSynchronousMode()) {
				long curTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}
					if (System.currentTimeMillis() - curTime > SET_MT_TIMEOUT) {
	//					System.out.println("Set MT: time out");
						break;
					}
				}
			}
			//System.out.println("Set MT: " + (System.currentTimeMillis() - curTime));
			setControllersMediaTime(time);

		}
	}

	private void setMediaTimeAndWait(long time) {
		//System.out.println("T: " + time);
		if (jmmfPlayer != null) {
			// works a bit better than just setting the position
//			if (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value){
//				return;
//			}
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			if (time < 0) {
				time = 0;
			}
			// don't check for the margin at the end of media
			if (time > duration /* - eomBuffer*/) {
				time = duration /* - eomBuffer*/;
			}
			
			jmmfPlayer.setMediaTime(time + offset);
			if (!jmmfPlayer.isSynchronousMode()) {
				long sysTime = System.currentTimeMillis();
				
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}
					
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
			}
			setControllersMediaTime(time);
		}
	}

	@Override
	public void setMilliSecondsPerSample(long milliSeconds) {
		if (!frameRateAutoDetected) {
			this.millisPerSample = milliSeconds;
		}
	}

	@Override
	public void setOffset(long offset) {
		long diff = this.offset - offset;
        this.offset = offset;
        mediaDescriptor.timeOrigin = offset;
        if (jmmfPlayer != null) {
			if (origDuration == 0) {
				origDuration = jmmfPlayer.getDuration();
			}
        	duration = origDuration - offset;
        }
        stopTime += diff;
        setStopTime(stopTime);//??
	}

	@Override
	public void setRate(float rate) {
		if (!isInited) {
			cachedRate = rate;
			return;
		}
		if (jmmfPlayer != null) {
			jmmfPlayer.setRate(rate);
		}
		setControllersRate(rate);
	}

	@Override
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
        // see if the stop time must be increased to ensure correct frame rendering at a frame boundary
		double msps = getMilliSecondsPerSample();
		if (msps != 0.0) {
	        long nFrames = (long) ((stopTime + offset) / msps);
	
	        if ((long) Math.ceil(nFrames * msps) == (stopTime + offset)) { // on a frame boundary
	            this.stopTime += 1;
	        }
		}
		if (jmmfPlayer != null) {
			jmmfPlayer.setStopTime(this.stopTime + offset);
		}
        setControllersStopTime(this.stopTime);
	}

	@Override
	public void setVolume(float level) {
		//System.out.println("Set volume: " + level);
		if (!isInited) {
			cachedVolume = level;
			return;
		}
		if (jmmfPlayer != null) {
			jmmfPlayer.setVolume(level);
		}
	}

	@Override
	public void start() {
		//System.out.println("start");
		if (jmmfPlayer != null) {
//			if (playing) {
//				return;
//			}
	        if (jmmfPlayer.isPlaying()) {
	        	return;
	        }
	        // play at start of media if at end of media
	        if ((getMediaDuration() - getMediaTime()) < 40) {
	            setMediaTime(0);
	        }

	        //playing = true;
	        jmmfPlayer.start();
	        if (!jmmfPlayer.isSynchronousMode()) {
				long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() != JMMFPlayer.PlayerState.STARTED.value) {
					//System.out.println("Poll: " + count + " " + getMediaTime());
					try {
						Thread.sleep(4);
					} catch (InterruptedException ie) {
						
					}
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
	        }
	        
	        startControllers();
	        
	        if (endTimeWatcher == null) {
	        	endTimeWatcher = new EndOfTimeWatcher(250);
	        	endTimeWatcher.setNormalPlayback(true);
	        	endTimeWatcher.setPlaying(true);
	        	endTimeWatcher.start();
	        } else {
	        	endTimeWatcher.setNormalPlayback(true);
	        	endTimeWatcher.setPlaying(true);
	        }

		}

	}

	@Override
	public void stop() {
		//System.out.println("stop");
		if (jmmfPlayer != null) {
//			if (!playing) {
//				return;
//			}
	        if (!jmmfPlayer.isPlaying()) {
	        	return;
	        }

			// stop a stop listening thread
			if (stopThread != null) {
				stopThread.setStopped();
			}
			
			//playing = false;
			jmmfPlayer.pause();
			// stop controller immediately without waiting until the player is actually stopped
			stopControllers();
			if (endTimeWatcher != null) {
				endTimeWatcher.setPlaying(false);
			}
			
			if (!jmmfPlayer.isSynchronousMode()) {
				// wait until the player is in the paused state, but not indefinitely
				long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() != JMMFPlayer.PlayerState.PAUSED.value) {
					//System.out.println("Poll: " + count + " " + getMediaTime());
					try {
						Thread.sleep(4);
					} catch (InterruptedException ie) {
						
					}
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
			}
			// to late to stop the controllers here?
//				stopControllers();
			//System.out.println("Paused at " + getMediaTime());
			setControllersMediaTime(getMediaTime());
			jmmfPlayer.repaintVideo();
			// stop a bit before the end of media because the player jumps to 0 when reaching the end
			// canceling the stop timer would be better
			if (jmmfPlayer.getStopTime() != duration - eomBuffer) {
				setStopTime(duration - eomBuffer);
			}

		}

	}

	@Override
	public void updateLocale() {
		if (popup != null) {
			if(detached) {
				detachItem.setText(ElanLocale.getString("Detachable.attach"));
			} else {
				detachItem.setText(ElanLocale.getString("Detachable.detach"));
			}			
			infoItem.setText(ElanLocale.getString("Player.Info"));
			durationItem.setText(ElanLocale.getString("Player.duration") +
	                ":  " + TimeFormatter.toString(getMediaDuration()));
			saveItem.setText(ElanLocale.getString("Player.SaveFrame"));
			origRatioItem.setText(ElanLocale.getString("Player.ResetAspectRatio"));
			arMenu.setText(ElanLocale.getString("Player.ForceAspectRatio"));
			//zoomMenu.setText(ElanLocale.getString("Menu.Zoom"));
			//graphItem.setText(ElanLocale.getString("Player.FilterGraph"));
			//allFiltersItem.setText(ElanLocale.getString("Player.AllFilters"));
	        if (copyOrigTimeItem != null) {
	        	copyOrigTimeItem.setText(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
	        }
		}
	}

	@Override
	public void controllerUpdate(ControllerEvent event) {
	}

	/**
	 * Returns the current image; it is retrieved from the renderer, 
	 * so the size might not be the original size.
	 */
	@Override
	public Image getCurrentFrameImage() {
		return getFrameImageForTime(getMediaTime());
	}

	/**
	 * Currently returns the current image.
	 */
	@Override
	public Image getFrameImageForTime(long time) {
		if (jmmfPlayer == null) {
			return null;
		}

		if (jmmfPlayer.isPlaying()) {
			stop();
		}
		
        if (time != getMediaTime()) {
            setMediaTime(time);
        }

        // pass a header object as argument, it will be filled by the JNI code.
        // the image data array, without header, is returned
        BufferedImage image = null;
        DIBInfoHeader dih = new DIBInfoHeader();
        byte[] data = jmmfPlayer.getCurrentImageData(dih);
        image = DIBToImage.DIBDataToBufferedImage(dih, data);
		return image;
	}

	private void initPopupMenu() {
		if (jmmfPanel == null) {
			return;
		}
		popup = new JPopupMenu();
        detachItem = new JMenuItem(ElanLocale.getString("Detachable.detach"));
        detachItem.addActionListener(this);
		infoItem = new JMenuItem(ElanLocale.getString("Player.Info"));
        infoItem.addActionListener(this);
        durationItem = new JMenuItem(ElanLocale.getString("Player.duration") +
                ":  " + TimeFormatter.toString(duration));
        durationItem.setEnabled(false);
        saveItem = new JMenuItem(ElanLocale.getString("Player.SaveFrame"));
        saveItem.addActionListener(this);
        origRatioItem = new JRadioButtonMenuItem(ElanLocale.getString("Player.ResetAspectRatio"), true);
        origRatioItem.setActionCommand("ratio_orig");
        origRatioItem.addActionListener(this);
		ratio_4_3_Item = new JRadioButtonMenuItem("4:3");
		ratio_4_3_Item.setActionCommand("ratio_4_3");
		ratio_4_3_Item.addActionListener(this);
		ratio_3_2_Item = new JRadioButtonMenuItem("3:2");
		ratio_3_2_Item.setActionCommand("ratio_3_2");
		ratio_3_2_Item.addActionListener(this);
		ratio_16_9_Item = new JRadioButtonMenuItem("16:9");
		ratio_16_9_Item.setActionCommand("ratio_16_9");
		ratio_16_9_Item.addActionListener(this);
		ratio_185_1_Item = new JRadioButtonMenuItem("1.85:1");
		ratio_185_1_Item.setActionCommand("ratio_185_1");
		ratio_185_1_Item.addActionListener(this);
		ratio_235_1_Item = new JRadioButtonMenuItem("2.35:1");
		ratio_235_1_Item.setActionCommand("ratio_235_1");
		ratio_235_1_Item.addActionListener(this);
		arMenu = new JMenu(ElanLocale.getString("Player.ForceAspectRatio"));
		ButtonGroup arbg = new ButtonGroup();
		arbg.add(origRatioItem);
		arbg.add(ratio_4_3_Item);
		arbg.add(ratio_3_2_Item);
		arbg.add(ratio_16_9_Item);
		arbg.add(ratio_185_1_Item);
		arbg.add(ratio_235_1_Item);
		arMenu.add(origRatioItem);
		arMenu.addSeparator();
		arMenu.add(ratio_4_3_Item);
		arMenu.add(ratio_3_2_Item);
		arMenu.add(ratio_16_9_Item);
		arMenu.add(ratio_185_1_Item);
		arMenu.add(ratio_235_1_Item);
		copyOrigTimeItem = new JMenuItem(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
		copyOrigTimeItem.addActionListener(this);
		zoomMenu = new JMenu(ElanLocale.getString("Menu.Zoom"));
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
		zbg.add(zoom100);
		zbg.add(zoom150);
		zbg.add(zoom200);
		zbg.add(zoom300);
		zbg.add(zoom400);
		zoomMenu.add(zoom100);
		zoomMenu.add(zoom150);
		zoomMenu.add(zoom200);
		zoomMenu.add(zoom300);
		zoomMenu.add(zoom400);
//		graphItem = new JMenuItem(ElanLocale.getString("Player.FilterGraph"));
//		graphItem.addActionListener(this);
//		allFiltersItem = new JMenuItem(ElanLocale.getString("Player.AllFilters"));
//		allFiltersItem.addActionListener(this);
		popup.add(detachItem);
        popup.addSeparator();
        popup.add(saveItem);
        popup.add(infoItem);
        //popup.add(graphItem);
        //popup.add(allFiltersItem);
        popup.add(arMenu);
        popup.add(zoomMenu);
        popup.add(durationItem);
        popup.add(copyOrigTimeItem);
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
    /*
    private void repositionVideoRect() {
		if (jmmfPanel != null) {
			if (!allowVideoScaling) {
				int compW = jmmfPanel.getWidth();
				int compH = jmmfPanel.getHeight();
				int origW = getSourceWidth();
				int origH = getSourceHeight();
				float sx = 0f;
				float sy = 0f;
				float sw = 1f;
				float sh = 1f;
				float factW = compW / (float) origW;
				float ar = origW / (float) origH;
				if (factW > 1) {// component bigger than video
					
				} else {
					
				}
			} else {
				vw = (int) (jmmfPanel.getWidth() * videoScaleFactor);
				vh = (int) (jmmfPanel.getHeight() * videoScaleFactor);
				if (vx + vw < jmmfPanel.getWidth()) {
					vx = jmmfPanel.getWidth() - vw;
				}
				if (vx > 0) {
					vx = 0;
				}
				if (vy + vh < jmmfPanel.getHeight()) {
					vy = jmmfPanel.getHeight() - vh;
				}
				if (vy > 0) {
					vy = 0;
				}
				jmmfPlayer.setVideoDestinationPos(vx, vy, vw, vh);
			}
		}
	}
    */

    /**
     * Check the setting for correct-at-pause behavior.
     */
	@Override
	public void preferencesChanged() {
		Boolean val = Preferences.getBool("Windows.JMMFPlayer.CorrectAtPause", null);
		
		if (val != null) {
			JMMFPlayer.correctAtPause(val.booleanValue());
		}
    	
    	val = Preferences.getBool("MediaNavigation.Pre47FrameStepping", null);
    	
    	if (val != null) {
    		pre47FrameStepping = val;
    	} 
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
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

            getVisualComponent().addNotify();
        } else if (e.getSource() == infoItem) {
            new FormattedMessageDlg(this);
        } else if (e.getSource() == saveItem) {
        	ImageExporter export = new ImageExporter();
        	export.exportImage(getCurrentFrameImage());
        } else if (e.getActionCommand().startsWith("ratio")) {
	        if (e.getSource() == origRatioItem) {
				aspectRatio = origAspectRatio;
			} else if (e.getSource() == ratio_4_3_Item) {
				aspectRatio = 1.33f;
			} else if (e.getSource() == ratio_3_2_Item) {
				aspectRatio = 1.66f;
			} else if (e.getSource() == ratio_16_9_Item) {
				aspectRatio = 1.78f;
			} else if (e.getSource() == ratio_185_1_Item) {
				aspectRatio = 1.85f;			
			} else if (e.getSource() == ratio_235_1_Item) {
				aspectRatio = 2.35f;
			} 
			layoutManager.doLayout();// will lead to a call to setVisualComponentSize of JMMFPlayer
			layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
					new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
        } else if (e.getActionCommand().startsWith("zoom")) {
			if (e.getSource() == zoom100) {
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
			jmmfPlayer.setVideoScaleFactor(videoScaleFactor);
			layoutManager.setPreference(("VideoZoom(" + mediaDescriptor.mediaURL + ")"), 
					new Float(videoScaleFactor), layoutManager.getViewerManager().getTranscription());
        } else if (e.getSource() == copyOrigTimeItem) {
			long t = getMediaTime() + offset;
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

	}
//##############
	
	// hier test thread for setting media time ?
	/**
	 * Sets the media position of the player, waits till the operation is finished 
	 * and then updates the controllers.
	 */
	/*
	private class SetMediaPositionThread extends Thread {
		long time;
		long offset = 0;
		
		public SetMediaPositionThread(long time, long offset) {
			super();
			this.time = time;
			this.offset = offset;
		}

		public void run() {
			//jmmfPlayer.setMediaTime(time + offset);
			while (jmmfPlayer.getMediaTime() != time + offset) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException ie){
					return;
				}
			}
			setControllersMediaTime(time);
			//jmmfPlayer.repaintVideo();
		}
	}
	*/
	/*
	private class SetMediaPosQueued extends Thread {
		private ArrayDeque<long[]> queue;
		private final Object LOCK = new Object();
		
		public SetMediaPosQueued() {
			queue = new ArrayDeque<long[]>(10);
		}
		
		public void add(long[] timepair) {
			synchronized (LOCK) {
				queue.add(timepair);
			}
		}
		
		public void run () {
			while (true) {
				while (queue.isEmpty()) {
					try {
					    sleep(40);
					} catch (InterruptedException ie) {
						System.out.println("Interrupted while waiting...");
					}
				}
				
				long[] next;
				synchronized (LOCK) {
					next = queue.poll();
				}
				
				if (next != null) {
					System.out.println("Setting pos: " + next[0]);
					jmmfPlayer.setMediaTime(next[0] + next[1]);
					while (jmmfPlayer.getMediaTime() != next[0] + next[1]) {
						try {
							Thread.sleep(5);
						} catch (InterruptedException ie){
							
						}
					}
					jmmfPlayer.repaintVideo();
					setControllersMediaTime(next[0]);
				}
			}
		}
	}
	*/
	/**
	 * TODO Revise, add a simple isInited to JMMFPlayer?
	 * Waits until the player is initiated and then stores, caches 
	 * some properties of the media.
	 */
	private class InitWaitThread extends Thread {
		final  int MAX_TRIES = 30;
		int count = 0;
		
		@Override
		public void run() {
			int state = 0;
			do {
				state = jmmfPlayer.getState();
				count++;
				if (state >= JMMFPlayer.PlayerState.STARTED.value && 
						state < JMMFPlayer.PlayerState.CLOSING.value) {
					isInited = true;
					ClientLogger.LOG.info("JMMFMediaPlayer: Init Session");
					ClientLogger.LOG.info("Aspect Ratio: " + jmmfPlayer.getAspectRatio());
					ClientLogger.LOG.info("Duration: " + jmmfPlayer.getDuration());
					ClientLogger.LOG.info("Time per frame: " + jmmfPlayer.getTimePerFrame());
					origDuration = jmmfPlayer.getDuration();
					//origAspectRatio = jmmfPlayer.getAspectRatio();
					int [] ar = jmmfPlayer.getPreferredAspectRatio();
					if (ar != null && ar.length == 2) {
						origAspectRatio = ar[0] / (float) ar[1];
						if (origAspectRatio != jmmfPlayer.getAspectRatio()) {
							ClientLogger.LOG.info("Preferred Aspect Ratio: " + origAspectRatio);
						}
					}
					millisPerSample = jmmfPlayer.getTimePerFrame();
					eomBuffer = (long) (numFramesEOMBuffer * millisPerSample);
					if (durationItem != null) {
						durationItem .setText(ElanLocale.getString("Player.duration") +
				                ":  " + TimeFormatter.toString(getMediaDuration()));
					}
					setVolume(cachedVolume);
					setRate(cachedRate);
					if (layoutManager != null) {
						layoutManager.doLayout();
					} else {
						ClientLogger.LOG.info("No LayoutManager set yet");
					}
					break;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException ie) {
					
				}
				if (count > MAX_TRIES) {
					break;
				}
			} while (true);
		}
	}
	
	 /**
     * Class to take care of state changes after the player finished
     * playing an interval or reached end of media  Active
     * callback does not seem possible due to threading issues in JNI and MMF?
     */
    private class PlayerStateWatcher extends Thread {
    	// default sleep time of 250 ms
    	private int sleepInterval = 250;
		private boolean stopped = false;
		private final int MAX_SLEEP = 3; // break the waiting if sleep time is less than this
		
    	/**
    	 * Constructor.
    	 * 
    	 * @param sleepInterval the number of ms to sleep in between tests
    	 */
    	public PlayerStateWatcher(int sleepInterval) {
			super();
			if (sleepInterval > 0) {
				this.sleepInterval = sleepInterval;
			}
		}
    	
    	public void setStopped() {
    		stopped = true;
    	}
    	
        /**
         * DOCUMENT ME!
         */
        @Override
		public void run() {
        	long refTime = stopTime;
        	long curTime;
        	
            while (!stopped /*&& (getMediaTime() < refTime)*/) {
            	curTime = getMediaTime();
            	if (curTime >= refTime) {
            		break;
            	} else if (refTime - curTime <= sleepInterval) {
            		sleepInterval = (int) Math.max((refTime - curTime) / 2 - MAX_SLEEP, MAX_SLEEP);
            	}
//            	System.out.println("M time: " + getMediaTime() + " (" + refTime + ")");
//            	System.out.println("Sleep: " + sleepInterval);
                try {
                    Thread.sleep(sleepInterval);
                } catch (InterruptedException ie) {
                    //ie.printStackTrace();
                	return;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                if (!jmmfPlayer.isPlaying()) {
                	break;
                }
            }
            
            if (stopped) {
            	isPlayingInterval = false;
            	return;
            }
            syncLock.lock();
            try {
	            if (jmmfPlayer.isPlaying()) {// in case pausing in the native player didn't succeed
	            	JMMFMediaPlayer.this.stop();
	                stopControllers();
	                jmmfPlayer.setMediaTime(refTime + offset);
	                setControllersMediaTime(getMediaTime());
	                setStopTime(duration - eomBuffer);
	                //playing = false;
	            } else
	            /*if (playing)*/ { //if at stop time (i.e. not stopped by hand) do some extra stuff
	            	//System.out.println("Player at stop time");
	                stopControllers();
	                jmmfPlayer.setMediaTime(refTime + offset);	                
	                setControllersMediaTime(getMediaTime());
	                setStopTime(duration - eomBuffer);
	                //playing = false;
	            }
	            // or check the paused state here
	            isPlayingInterval = false;
            } finally {
            	syncLock.unlock();
            }
        }
    }
    
    /**
     * A thread that tries to detect whether the media player already reached the end of media
     * and stops connected controllers if so. 
     * The native media player tries to stop playback a few hundred ms before the end of
     * media because when the media foundation player reaches the end the player is stopped 
     * (and as a result jumps back to 0 without "scrubbing" the first frame).
     * 
     * @author Han Sloetjes
     *
     */
    private class EndOfTimeWatcher extends Thread {
    	// default sleep time of 250 ms
    	private int sleepInterval = 250;
    	/** only detect end of file in case of normal playback */
    	private volatile boolean normalPlayback = true;
    	private volatile boolean isPlaying = false;
    	private boolean closed = false;
		/**
		 * Constructor that sets the sleep duration.
		 * 
		 * @param sleepInterval the sleep interval
		 */
		EndOfTimeWatcher(int sleepInterval) {
			super();
			if (sleepInterval > 0) {
				this.sleepInterval = sleepInterval;
			}
		}
    	
		/**
		 * Sets whether the player is in normal playback mode, i.e. whether it 
		 * is not playing a selection but plays until the end of the file.
		 * 
		 * @param normalPlayback a flag to indicate whether the player is in 
		 * normal playback mode
		 */
		public synchronized void setNormalPlayback(boolean normalPlayback) {
			this.normalPlayback = normalPlayback;
		}
		
		/**
		 * Sets the playing state.
		 * 
		 * @param playing
		 */
		public synchronized void setPlaying(boolean isPlaying) {
			this.isPlaying = isPlaying;
			if (isPlaying) {
				notify();
			}
		}
		
		/**
		 * Closes this thread, stops execution.
		 */
		public void close() {
			closed = true;
		}
		
		/**
		 * When active check if the player is at (or close to) the end of the media.
		 */
		@Override
		public void run() {
			while (!closed) {
				try {
					Thread.sleep(sleepInterval);
					
					synchronized(this) {
						while (!isPlaying || !normalPlayback) {
							//System.out.println("Waiting...");
							wait();
						}
					}
				} catch (InterruptedException ie) {
					
				}
				// test for end of media, stop controllers etc.
				long curMediaTime = getMediaTime();
				if (curMediaTime >= getMediaDuration() - eomBuffer) {
					//System.out.println("At end: " + curMediaTime);
					if (jmmfPlayer.isPlaying()) {
						//System.out.println("At end: " + curMediaTime + " player still playing");
		            	JMMFMediaPlayer.this.stop();
		                stopControllers();

		                isPlaying = false;
		            } else {
		            	//System.out.println("At end: " + curMediaTime + " player already stopped.");
		            	// the player reached end of media and rewinded back to the beginning
		                stopControllers();

		                isPlaying = false;
		            }
				} else if (curMediaTime == 0){// maybe the media player isn't playing anymore, time = 0??
					if (jmmfPlayer.isPlaying()) {
						//System.out.println("Rewinded to: " + curMediaTime + " player is playing.");
		            	JMMFMediaPlayer.this.stop();
		                stopControllers();

		                isPlaying = false;
					} else {
						//System.out.println("Rewinded to: " + curMediaTime + " player stopped.");
		                stopControllers();

		                isPlaying = false;
					}
				} else if (jmmfPlayer.getState() == JMMFPlayer.PlayerState.PAUSED.value ||
						jmmfPlayer.getState() == JMMFPlayer.PlayerState.STOPPED.value) {
					if (isPlaying) {
		            	JMMFMediaPlayer.this.stop();
		                stopControllers();
		                
						isPlaying = false;
					}
				}
			}
		}
    }
	
	private class MouseHandler implements MouseListener, MouseMotionListener {
		private final DecimalFormat format = new DecimalFormat("#.###");
		
		@Override
		public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
                if (layoutManager != null) {
                    layoutManager.setFirstPlayer(JMMFMediaPlayer.this);
                }

                return;
            }
            if (SwingUtilities.isRightMouseButton(e)) {
            	return;
            }
            try {
            	int[] vidDest = jmmfPlayer.getVideoDestinationPos();
            	
            	if (vidDest != null) {
            		int nx = e.getX() - vidDest[0];
            		int ny = e.getY() - vidDest[1];
                	// include scale factor and translation 
                	if (videoScaleFactor > 1) {
                		int[] vidCoords = jmmfPlayer.getVideoTranslation();
                		int[] vidSize = jmmfPlayer.getScaledVideoRect();
                		nx = e.getX() - vidCoords[0];// coordinates in the scaled image
                		ny = e.getY() - vidCoords[1];

                		if (vidSize[0] != 0 && vidSize[1] != 0) {
    	            		nx = (int)(vidDest[2] * (nx / (float) vidSize[0]));// recalculate
    	            		ny = (int)(vidDest[3] * (ny / (float) vidSize[1]));
                		}
                	}
                	
	                if (e.isAltDown()) {
	                	copyToClipboard(format.format(nx / (float)vidDest[2]) + "," 
	             			   + format.format(ny / (float)vidDest[3]));
	                }  else if (e.isShiftDown()){
	                    copyToClipboard("" + (int)((jmmfPlayer.getSourceWidth() / (float)vidDest[2]) * nx) 
	                		    + "," + (int)((jmmfPlayer.getSourceHeight() / (float)vidDest[3]) * ny));
	                } else {
	                    copyToClipboard("" + (int)((jmmfPlayer.getSourceWidth() / (float)vidDest[2]) * nx) 
	                		    + "," + (int)((jmmfPlayer.getSourceHeight() / (float)vidDest[3]) * ny)
	                		    + " [" + jmmfPlayer.getSourceWidth() + "," + jmmfPlayer.getSourceHeight() + "]");
	                }
//	                if (e.isAltDown()) {
//	                	copyToClipboard(format.format((e.getX() - vidDest[0]) / (float)vidDest[2]) + "," 
//	             			   + format.format((e.getY() - vidDest[1]) / (float)vidDest[3]));
//	                }  else if (e.isShiftDown()){
//	                    copyToClipboard("" + (int)((jmmfPlayer.getSourceWidth() / (float)vidDest[2]) * (e.getX() - vidDest[0])) 
//	                		    + "," + (int)((jmmfPlayer.getSourceHeight() / (float)vidDest[3]) * (e.getY() - vidDest[1])));
//	                } else {
//	                    copyToClipboard("" + (int)((jmmfPlayer.getSourceWidth() / (float)vidDest[2]) * (e.getX() - vidDest[0])) 
//	                		    + "," + (int)((jmmfPlayer.getSourceHeight() / (float)vidDest[3]) * (e.getY() - vidDest[1]))
//	                		    + " [" + jmmfPlayer.getSourceWidth() + "," + jmmfPlayer.getSourceHeight() + "]");
//	                } 
                }
            } catch (Exception exep) {
            	   exep.printStackTrace();
            }	
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			//System.out.println("Corner: " + getVisualComponent().getLocationOnScreen());
            //JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        	// on jre 1.6 (and higher?) the coordinates are not correct, or at least different?
			Point cl = e.getPoint();
//        	if (java_version >= 16) {
//        		cl = adjustCoords(e.getComponent(), cl);
//        	}
        	
            if (SwingUtilities.isRightMouseButton(e)) {
            	// check the detached state, attaching can be done independently of the menu
            	if (layoutManager.isAttached(JMMFMediaPlayer.this)) {
            		if (detached) {
            			detached = false;
            			detachItem.setText(ElanLocale.getString("Detachable.detach"));
            		}
            	}
            	durationItem.setText(ElanLocale.getString("Player.duration") +
                        ":  " + TimeFormatter.toString(duration));
            	//System.out.println("S: " + e.getSource() + " X: " + e.getX() +  " Y: " + e.getY());
                popup.show(getVisualComponent(), (int) cl.getX(), (int) cl.getY());
                return;
            }
			dragX = (int) cl.getX();
			dragY = (int) cl.getY();
//			vw = (int) (jmmfPanel.getWidth() * videoScaleFactor);
//			vh = (int) (jmmfPanel.getHeight() * videoScaleFactor);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int dx = dragX - e.getX();
			int dy = dragY - e.getY();
//			vx -= dx;
//			vy -= dy;
			dragX = e.getX();
			dragY = e.getY();
			jmmfPlayer.moveVideoPos(-dx, -dy);
			// check video position relative to video window
//			if (vx + vw < jmmfPanel.getWidth()) {
//				vx = jmmfPanel.getWidth() - vw;
//			}
//			if (vx > 0) {
//				vx = 0;
//			}
//			if (vy + vh < jmmfPanel.getHeight()) {
//				vy = jmmfPanel.getHeight() - vh;
//			}
//			if (vy > 0) {
//				vy = 0;
//			}
//			jmmfPlayer.setVideoDestinationPos(vx, vy, vw, vh);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		private Point adjustCoords(Component comp, Point org) {
			try {
				Point p = comp.getLocationOnScreen();
				return new Point((int)(p.getX() + org.getX()), (int) (p.getY() + org.getY()));
			} catch (Exception ex){}// catch any exception
			
			return org;
		}
	}

}
