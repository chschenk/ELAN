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
import mpi.eudico.client.annotator.export.ImageExporter;
import mpi.eudico.client.annotator.gui.FormattedMessageDlg;
import mpi.eudico.client.annotator.gui.TextAreaMessageDlg;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;
import nl.mpi.jds.JDSException;
import nl.mpi.jds.JDSPanel;
import nl.mpi.jds.JDSPlayer;

/**
 * Implementation of an ElanMediaPlayer that encapsulates a JDSPlayer,
 * a Java Native Interface to Direct Show player. 
 * 
 * @author han
 *
 */
public class JDSMediaPlayer extends ControllerManager
    implements ElanMediaPlayer, ControllerListener, VideoFrameGrabber, ActionListener {
	private static boolean regFiltersPrinted = false;
	private JDSPlayer jdsPlayer;
	private JDSPanel jdsPanel;
	private MediaDescriptor mediaDescriptor;
	private long offset = 0L;
	private long stopTime;
	private long duration;// media duration minus offset
	private float origAspectRatio;
	private float aspectRatio;
	private double millisPerSample;
	private boolean playing;
	private PlayerStateWatcher stopThread = null;
	private PlayerEndWatcher endThread = null;
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
	private JRadioButtonMenuItem origRatioItem;
	private JRadioButtonMenuItem ratio_4_3_Item;
	private JRadioButtonMenuItem ratio_3_2_Item;
	private JRadioButtonMenuItem ratio_16_9_Item;
	private JRadioButtonMenuItem ratio_185_1_Item;
	private JRadioButtonMenuItem ratio_235_1_Item;
	private JMenuItem copyOrigTimeItem;
	private JMenuItem graphItem;
	private JMenuItem allFiltersItem;
	private boolean detached;
	private JMenu arMenu;
	private JMenu zoomMenu;
	private JRadioButtonMenuItem zoom100;
	private JRadioButtonMenuItem zoom150;
	private JRadioButtonMenuItem zoom200;
	private JRadioButtonMenuItem zoom300;
	private JRadioButtonMenuItem zoom400;
	private float videoScaleFactor = 1f;
	private int vx = 0, vy = 0, vw = 0, vh = 0;
	private int dragX = 0, dragY = 0;
	
	private final ReentrantLock syncLock = new ReentrantLock();
	
	private static int java_version = 15; // 1.5
	
	static {
		String version = System.getProperty("java.version");
		if (version.length() >= 3) {
			if (version.charAt(1) == '.') {
				char[] ch = new char[2];
				ch[0] = version.charAt(0);
				ch[1] = version.charAt(2);
				try {
					java_version = Integer.parseInt(new String(ch));
				} catch (NumberFormatException nfe) {
					// ignore
				}
			}
		}
	}
	
	/**
	 * 
	 * @param mediaDescriptor
	 * @throws NoPlayerException
	 */
	public JDSMediaPlayer(MediaDescriptor mediaDescriptor) throws NoPlayerException {
		this.mediaDescriptor = mediaDescriptor;
		offset = mediaDescriptor.timeOrigin;
		
        String URLString = mediaDescriptor.mediaURL;

        if (URLString.startsWith("file:") &&
                !URLString.startsWith("file:///")) {
            URLString = URLString.substring(5);
        }
        // check preferred codec/splitter
         String prefSplitter = System.getProperty("JDSPreferredSplitter");
        try {
        	if (prefSplitter == null || prefSplitter.length() == 0) {
        		jdsPlayer = new JDSPlayer(URLString);
        	} else {
        		jdsPlayer = new JDSPlayer(URLString, prefSplitter);
        	}
        	duration = jdsPlayer.getDuration();
        	// after this call it is known whether setStopTome is supported or not
        	setMediaTime(0L);
        	jdsPlayer.setStopTime(duration);
        	duration -= offset;
        	origAspectRatio = jdsPlayer.getAspectRatio();
        	aspectRatio = origAspectRatio;
        	millisPerSample = jdsPlayer.getTimePerFrame();
        	if (millisPerSample == 0.0) {
        		millisPerSample = 40;// default 40 ms per frame, 25 frames per second
        		frameRateAutoDetected = false;
        	}
        	if (jdsPlayer.isVisualMedia()) {
        		jdsPanel = new JDSPanel(jdsPlayer);
        		initPopupMenu();
        		MouseHandler mh = new MouseHandler();
        		jdsPanel.addMouseListener(mh);
        		jdsPanel.addMouseMotionListener(mh);
        	} else {
        		millisPerSample = 40;// for audio default to 40
        		frameRateAutoDetected = false;
        		aspectRatio = 0.0f;
        	}
        	if (!regFiltersPrinted) {
        		printRegisteredFilters();
        	}
        	printFiltersInChain();
        } catch (JDSException jdse) {
        	throw new NoPlayerException(jdse.getMessage());
        } catch (UnsatisfiedLinkError ue) {
        	// although an error should normally not be caught, catching this error when a native library
        	// is not found, allows the program to try alternative media solutions
        	throw new NoPlayerException(ue.getMessage());
        } catch (Throwable tr) {
        	throw new NoPlayerException(tr.getMessage());
        }
	}

	private void printRegisteredFilters() {
		if (jdsPlayer != null) {
			String[] allFilters = jdsPlayer.getRegisteredFilters();
			System.out.println("Registered Filters:");
			for (int i = 0; i < allFilters.length; i++) {
				System.out.println(i + ": " + allFilters[i]);
			}
			regFiltersPrinted = true;
			System.out.println();
		}
	}
	
	private void printFiltersInChain() {
		if (jdsPlayer != null) {
			String[] filters = jdsPlayer.getFiltersInGraph();
			System.out.println("Filters in the filter chain: " + mediaDescriptor.mediaURL);
			for (String filter : filters) {
				System.out.println(filter);
			}
		}
	}
	
	@Override
	public void cleanUpOnClose() {
		if (jdsPlayer != null) {
			jdsPlayer.cleanUpOnClose();
			jdsPlayer = null;
		}
	}

	@Override
	public float getAspectRatio() {
		return aspectRatio;
	}

	@Override
	public String getFrameworkDescription() {
		return "JDS - Java DirectShow Player";
	}

	@Override
	public MediaDescriptor getMediaDescriptor() {
		return mediaDescriptor;
	}

	@Override
	public long getMediaDuration() {
		return duration;
	}

	@Override
	public long getMediaTime() {
		if (jdsPlayer != null) {
			return jdsPlayer.getMediaTime() - offset;
		}
		return 0;
	}

	@Override
	public double getMilliSecondsPerSample() {
		return millisPerSample;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public float getRate() {
		if (jdsPlayer != null) {
			return jdsPlayer.getRate();
		}
		return 0;
	}

	@Override
	public int getSourceHeight() {
		if (jdsPlayer != null) {
			return jdsPlayer.getSourceHeight();
		}
		return 0;
	}

	@Override
	public int getSourceWidth() {
		if (jdsPlayer != null) {
			return jdsPlayer.getSourceWidth();
		}
		return 0;
	}

	@Override
	public Component getVisualComponent() {
		return jdsPanel;
	}

	@Override
	public float getVolume() {
		if (jdsPlayer != null) {
			return jdsPlayer.getVolume();
		}
		return 0;
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
		if (jdsPlayer != null) {
			return jdsPlayer.isPlaying();
		}
		return false;
	}

	@Override
	public void nextFrame() {
		if (jdsPlayer != null) {
			//if (jdsPlayer.getState() != JDSPlayer.STATE_PAUSE) {
			if (jdsPlayer.isPlaying()) {
				//jdsPlayer.pause();
				stop();
			}
			                       // quick temporary? fix for audio only players
	        if (pre47FrameStepping || !jdsPlayer.isVisualMedia()) {
	        	nextFramePre47();
	        	return;
	        }
	        
			double nextTime = jdsPlayer.nextFrame(frameStepsToFrameBegin);

			setControllersMediaTime((long) Math.ceil(nextTime) - offset);
		}

	}
	
    /**
     * The pre 4.7 implementation of next frame.
     */
    private void nextFramePre47() {
    	// assumes player != null and is paused
        if (frameStepsToFrameBegin) {
        	long curFrame = (long)(getMediaTime() / millisPerSample);
    		setMediaTime((long) Math.ceil((curFrame + 1) * millisPerSample));
        } else {
        	long curTime = jdsPlayer.getMediaTime();
        	curTime = (long) Math.ceil(curTime + millisPerSample);
        	jdsPlayer.setMediaTime(curTime);
        	setControllersMediaTime(curTime - offset);
        }
    }

	@Override
	public void playInterval(long startTime, long stopTime) {
		if (jdsPlayer != null) {
			if (jdsPlayer.isPlaying()) {
				//jdsPlayer.stop();
				stop();
			}
			setStopTime(stopTime);
			setMediaTime(startTime);
			startInterval();
		}
		
	}
	
	@Override
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
        // see if the stop time must be increased to ensure correct frame rendering at a frame boundary
        long nFrames = (long) ((stopTime + offset) / getMilliSecondsPerSample());

        if ((long) Math.ceil(nFrames * getMilliSecondsPerSample()) == (stopTime + offset)) { // on a frame boundary
            this.stopTime += 1;
        }
        jdsPlayer.setStopTime(this.stopTime + offset);
        setControllersStopTime(this.stopTime);
	}

	@Override
	public void previousFrame() {
		if (jdsPlayer != null) {
			//if (jdsPlayer.getState() != JDSPlayer.STATE_PAUSE) {
			if (jdsPlayer.isPlaying()) {
				//jdsPlayer.pause();
				stop();
			}
			                       // quick temporary? fix for audio only players
			if (pre47FrameStepping || !jdsPlayer.isVisualMedia()) {
				previousFramePre47();
				return;
			}
			
			double prevTime = jdsPlayer.previousFrame(frameStepsToFrameBegin);
			setControllersMediaTime((long) Math.ceil(prevTime) - offset);
		}

	}

    /**
     * The previous implementation of previous frame, with (more) rounding effects.
     */
    private void previousFramePre47() {
    	// assumes player != null and is paused
        if (frameStepsToFrameBegin) {
        	long curFrame = (long)(getMediaTime() / millisPerSample);
        	if (curFrame > 0) {
        		setMediaTime((long) Math.ceil(((curFrame - 1) * millisPerSample)));
        	} else {
        		setMediaTime(0);
        	}
        } else {
        	long curTime = jdsPlayer.getMediaTime();
        	curTime = (long) Math.ceil(curTime - millisPerSample);
        	
	        if (curTime < 0) {
	        	curTime = 0;
	        }
	
        	jdsPlayer.setMediaTime(curTime);
        	setControllersMediaTime(curTime - offset);
        }
    }
    
	@Override
	public void setAspectRatio(float aspectRatio) {
		this.aspectRatio = aspectRatio;
		// update popup??
	}

	@Override
	public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
		frameStepsToFrameBegin = stepsToFrameBegin;
	}
	
	public void setZoomFactor(float zoom) {
		videoScaleFactor = zoom;
		// update popup menu??
		if (videoScaleFactor == 1) {
			vx = 0;
			vy = 0;
		}
		vw = (int) (videoScaleFactor * jdsPanel.getWidth());
		vh = (int) (videoScaleFactor * jdsPanel.getHeight());
		jdsPlayer.setVideoScaleFactor(videoScaleFactor);
		//jdsPlayer.setVideoDestinationPos(vx, vy, vw, vh);
		if (popup != null) {
			int zf = (int) (100 * videoScaleFactor);
			switch(zf) {
			case 100:
				zoom100.setSelected(true);
				break;
			case 150:
				zoom150.setSelected(true);
				break;
			case 200:
				zoom200.setSelected(true);
				break;
			case 300:
				zoom300.setSelected(true);
				break;
			case 400:
				zoom400.setSelected(true);
				default:
			}
		}
	}
	/*
	public void setVideoRectOrigin(int x, int y) {
		if (videoScaleFactor > 0) {
			vx = x;
			vy = y;
		}
	}
	*/
	@Override
	public void setLayoutManager(ElanLayoutManager layoutManager) {
        if (this.layoutManager == null) {
//            detachItem = new JMenuItem(ElanLocale.getString("Detachable.detach"));
//            detachItem.addActionListener(this);
//            popup.insert(detachItem, 0);
        }
		this.layoutManager = layoutManager;
	}

	@Override
	public void setMediaTime(long time) {
		if (jdsPlayer != null) {			
			//if (jdsPlayer.getState() != JDSPlayer.STATE_PAUSE) {
			if (jdsPlayer.isPlaying()) {
				//jdsPlayer.pause();
				stop();
			}
	        if (time < 0) {
	            time = 0;
	        }
	        if (time > duration) {
	        	time = duration;
	        }
	        if (jdsPlayer.getState() != JDSPlayer.STATE_PAUSE) {
	        	jdsPlayer.pause();
	        }
	        jdsPlayer.setMediaTime(time + offset);
	        setControllersMediaTime(time);
		}

	}

	@Override
	public void setMilliSecondsPerSample(long milliSeconds) {
        if (!frameRateAutoDetected) {
            millisPerSample = milliSeconds;
        }
	}

	@Override
	public void setOffset(long offset) {
		long curTime = getMediaTime();
		long diff = /*this.offset - */offset - this.offset;
        this.offset = offset;
        mediaDescriptor.timeOrigin = offset;
        if (jdsPlayer != null) {
        	duration = jdsPlayer.getDuration() - offset;
        }
        stopTime += diff;
        if (stopTime != diff && stopTime != duration) {
        	setStopTime(stopTime);//??
        }
         
        curTime += diff;
        setMediaTime(curTime < 0 ? 0 : curTime);
	}

	@Override
	public void setRate(float rate) {
		if (jdsPlayer != null) {
			jdsPlayer.setRate(rate);
		}
		setControllersRate(rate);
	}



	@Override
	public void setVolume(float level) {
		if (jdsPlayer != null) {
			jdsPlayer.setVolume(level);
		}
	}
	
	void startInterval() {
		if (jdsPlayer != null) {
			if (playing) {
				return;
			}
	        // play at start of media if at end of media
//	        if ((getMediaDuration() - getMediaTime()) < 40) {
//	            setMediaTime(0);
//	        }
			syncLock.lock();
			try {
		        playing = true;
		        jdsPlayer.start();
		        startControllers();
		        // create a PlayerStateWatcher thread
		        if (stopThread != null && stopThread.isAlive()) {
		        	stopThread.setStopped();
		        }
		        stopThread = new PlayerStateWatcher();
		        stopThread.start();
			} finally {
	        	syncLock.unlock();
	        }
		}
	}

	/**
	 * Only to be called if not playing an interval.
	 */
	@Override
	public void start() {
		if (jdsPlayer != null) {
			if (playing) {
				return;
			}
	        // play at start of media if at end of media
	        if ((getMediaDuration() - getMediaTime()) < 40) {
	            setMediaTime(0);
	        }

	        playing = true;
	        jdsPlayer.start();
	        startControllers();
	        if (endThread != null && endThread.isAlive()) {
	        	endThread.setStopped();
	        }
	        // create a PlayerEndWatcher thread
	        endThread = new PlayerEndWatcher();
	        endThread.start();
		}
	}

	@Override
	public void stop() {
		if (jdsPlayer != null) {
			if (!playing) {
				return;
			}

			if (stopThread != null) {
				stopThread.setStopped();
			}
			jdsPlayer.pause();
	        stopControllers();
	        setControllersMediaTime(getMediaTime());
	        
	        playing = false;
	        
			// reset stoptime
			if (jdsPlayer.getStopTime() != duration) {
				setStopTime(duration);
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
			zoomMenu.setText(ElanLocale.getString("Menu.Zoom"));
			graphItem.setText(ElanLocale.getString("Player.FilterGraph"));
			allFiltersItem.setText(ElanLocale.getString("Player.AllFilters"));
	        if (copyOrigTimeItem != null) {
	        	copyOrigTimeItem.setText(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
	        }
		}
	}

	@Override
	public void controllerUpdate(ControllerEvent event) {
	}

	@Override
	public Image getCurrentFrameImage() {
		return getFrameImageForTime(getMediaTime());
	}

	@Override
	public Image getFrameImageForTime(long time) {
		if (jdsPlayer == null) {
			return null;
		}
		//if (jdsPlayer.getState() != JDSPlayer.STATE_PAUSE) {
		if (jdsPlayer.isPlaying()) {
			stop();
			//jdsPlayer.pause();
		}
		
        if (time != getMediaTime()) {
            setMediaTime(time);
        }
        BufferedImage image = null;
        
        byte[] data = jdsPlayer.getCurrentImageData();
        image = DIBToImage.DIBDataToBufferedImage(data);
		return image;
	}
	
	public static final byte[] getBytes(int i) {
		return new byte[] { (byte)(i>>24), (byte)(i>>16), (byte)(i>>8), (byte)i };
	}

	private void initPopupMenu() {
		if (jdsPanel == null) {
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
		graphItem = new JMenuItem(ElanLocale.getString("Player.FilterGraph"));
		graphItem.addActionListener(this);
		allFiltersItem = new JMenuItem(ElanLocale.getString("Player.AllFilters"));
		allFiltersItem.addActionListener(this);
		popup.add(detachItem);
        popup.addSeparator();
        popup.add(saveItem);
        popup.add(infoItem);
        popup.add(graphItem);
        popup.add(allFiltersItem);
        popup.add(arMenu);
        popup.add(zoomMenu);
        popup.add(durationItem);
        popup.add(copyOrigTimeItem);

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
        } else if (e.getSource() == graphItem) {
        	String[] graphText = jdsPlayer.getFiltersInGraph();
        	if (graphText != null) {
        		new TextAreaMessageDlg(jdsPanel, graphText, 
        				ElanLocale.getString("Player.FilterGraph.Title"));
        	} else {
        		new TextAreaMessageDlg(jdsPanel, ElanLocale.getString("Player.Message.NoGraph"), 
        				ElanLocale.getString("Player.FilterGraph.Title"));
        	}
        } else if (e.getSource() == allFiltersItem) {
        	String[] graphText = jdsPlayer.getRegisteredFilters();
        	if (graphText != null) {
        		new TextAreaMessageDlg(jdsPanel, graphText, ElanLocale.getString("Player.AllFilters.Title"));
        	} else {
        		new TextAreaMessageDlg(jdsPanel, ElanLocale.getString("Player.Message.NoFilters"), 
        				ElanLocale.getString("Player.AllFilters.Title"));
        	}
        } else if (e.getSource() == saveItem) {
        	ImageExporter export = new ImageExporter();
        	export.exportImage(getCurrentFrameImage());
        } else if (e.getActionCommand().startsWith("ratio")) {
	        if (e.getSource() == origRatioItem) {
				aspectRatio = origAspectRatio;
				//layoutManager.doLayout();
				//layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
				//		new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
			} else if (e.getSource() == ratio_4_3_Item) {
				aspectRatio = 1.33f;
				//layoutManager.doLayout();
				//layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
				//		new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
			} else if (e.getSource() == ratio_3_2_Item) {
				aspectRatio = 1.66f;
				//layoutManager.doLayout();
				//layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
				//		new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
			} else if (e.getSource() == ratio_16_9_Item) {
				aspectRatio = 1.78f;
				//layoutManager.doLayout();
				//layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
				//		new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
			} else if (e.getSource() == ratio_185_1_Item) {
				aspectRatio = 1.85f;
				//layoutManager.doLayout();
				//layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
				//		new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());			
			} else if (e.getSource() == ratio_235_1_Item) {
				aspectRatio = 2.35f;
				//layoutManager.doLayout();
				//layoutManager.setPreference(("AspectRatio(" + mediaDescriptor.mediaURL + ")"), 
				//		new Float(aspectRatio), layoutManager.getViewerManager().getTranscription());
			} 
			layoutManager.doLayout();
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
			jdsPlayer.setVideoScaleFactor(videoScaleFactor);
			layoutManager.setPreference(("VideoZoom(" + mediaDescriptor.mediaURL + ")"), 
					new Float(videoScaleFactor), layoutManager.getViewerManager().getTranscription());
        } else if (e.getSource() == copyOrigTimeItem) {
			long t = getMediaTime() + offset;
			String timeFormat = Preferences.getString("CurrentTime.Copy.TimeFormat", null);
			String currentTime = null;
			
	        if (timeFormat != null) {
	        	if(timeFormat.equals(Constants.HHMMSSMS_STRING)){
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
    	    System.out.println(text);
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

	private class MouseHandler implements MouseListener, MouseMotionListener {
		private final DecimalFormat format = new DecimalFormat("#.###");
		
		@Override
		public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
                if (layoutManager != null) {
                    layoutManager.setFirstPlayer(JDSMediaPlayer.this);
                }

                return;
            }
            if (SwingUtilities.isRightMouseButton(e)) {
            	return;
            }
            try {
            	int[] vidDest = jdsPlayer.getVideoDestinationPos();
            	if (vidDest != null) {
            		int nx = e.getX() - vidDest[0];
            		int ny = e.getY() - vidDest[1];
                	// include scale factor and translation 
                	if (videoScaleFactor > 1) {
                		nx = e.getX() - vx;// coordinates in the scaled image
                		ny = e.getY() - vy;

                		if (vw != 0 && vh != 0) {
    	            		nx = (int)(vidDest[2] * (nx / (float) vw));// recalculate
    	            		ny = (int)(vidDest[3] * (ny / (float) vh));
                		}
                	}
            		
	                if (e.isAltDown()) {
	                	copyToClipboard(format.format(nx / (float)vidDest[2]) + "," 
	             			   + format.format(ny / (float)vidDest[3]));
	                }  else if (e.isShiftDown()){
	                    copyToClipboard("" + (int)((jdsPlayer.getSourceWidth() / (float)vidDest[2]) * nx) 
	                		    + "," + (int)((jdsPlayer.getSourceHeight() / (float)vidDest[3]) * ny));
	                } else {
	                    copyToClipboard("" + (int)((jdsPlayer.getSourceWidth() / (float)vidDest[2]) * nx) 
	                		    + "," + (int)((jdsPlayer.getSourceHeight() / (float)vidDest[3]) * ny)
	                		    + " [" + jdsPlayer.getSourceWidth() + "," + jdsPlayer.getSourceHeight() + "]");
	                }
	                
//	                if (e.isAltDown()) {
//	                	copyToClipboard(format.format((e.getX() - vidDest[0]) / (float)vidDest[2]) + "," 
//	             			   + format.format((e.getY() - vidDest[1]) / (float)vidDest[3]));
//	                }  else if (e.isShiftDown()){
//	                    copyToClipboard("" + (int)((jdsPlayer.getSourceWidth() / (float)vidDest[2]) * (e.getX() - vidDest[0])) 
//	                		    + "," + (int)((jdsPlayer.getSourceHeight() / (float)vidDest[3]) * (e.getY() - vidDest[1])));
//	                } else {
//	                    copyToClipboard("" + (int)((jdsPlayer.getSourceWidth() / (float)vidDest[2]) * (e.getX() - vidDest[0])) 
//	                		    + "," + (int)((jdsPlayer.getSourceHeight() / (float)vidDest[3]) * (e.getY() - vidDest[1]))
//	                		    + " [" + jdsPlayer.getSourceWidth() + "," + jdsPlayer.getSourceHeight() + "]");
//	                } 
                }
            } catch (Exception exep) {
            	   exep.printStackTrace();
            }	
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// stub
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
        	if (java_version >= 16) {
        		cl = adjustCoords(e.getComponent(), cl);
        	}
        	
            if (SwingUtilities.isRightMouseButton(e)) {
            	// check the detached state, attaching can be done independently of the menu
            	if (layoutManager.isAttached(JDSMediaPlayer.this)) {
            		if (detached) {
            			detached = false;
            			detachItem.setText(ElanLocale.getString("Detachable.detach"));
            		}
            	}
            	//System.out.println("S: " + e.getSource() + " X: " + e.getX() +  " Y: " + e.getY());
                popup.show(getVisualComponent(), (int) cl.getX(), (int) cl.getY());
                return;
            }
			dragX = (int) cl.getX();
			dragY = (int) cl.getY();
			int[] curBounds = jdsPlayer.getVideoDestinationPos();
			if (curBounds != null) {
				vx = curBounds[0];
				vy = curBounds[1];
				if (curBounds[2] != 0 && curBounds[3] != 0) {
					vw = curBounds[2];
					vh = curBounds[3];
				} else {
					vw = (int) (jdsPanel.getWidth() * videoScaleFactor);
					vh = (int) (jdsPanel.getHeight() * videoScaleFactor);
				}
			} else {
				vw = (int) (jdsPanel.getWidth() * videoScaleFactor);
				vh = (int) (jdsPanel.getHeight() * videoScaleFactor);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int dx = dragX - e.getX();
			int dy = dragY - e.getY();
			vx -= dx;
			vy -= dy;
			dragX = e.getX();
			dragY = e.getY();
			// check video position relative to video window
			if (vx + vw < jdsPanel.getWidth()) {
				vx = jdsPanel.getWidth() - vw;
			}
			if (vx > 0) {
				vx = 0;
			}
			if (vy + vh < jdsPanel.getHeight()) {
				vy = jdsPanel.getHeight() - vh;
			}
			if (vy > 0) {
				vy = 0;
			}
			jdsPlayer.setVideoDestinationPos(vx, vy, vw, vh);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// stub
		}
		
		private Point adjustCoords(Component comp, Point org) {
			try {
				Point p = comp.getLocationOnScreen();
				return new Point((int)(p.getX() + org.getX()), (int) (p.getY() + org.getY()));
			} catch (Exception ex){}// catch any exception
			
			return org;
		}
	}
	
    /**
     * Temporary class to take care of state changes  after the player finished
     * playing an interval  or reached end of media  As soon as active
     * callback can be handled this class will become obsolete.
     * This method can only be used in combination with codecs that support setStopTime.
     */
    private class PlayerStateWatcher extends Thread {
    	private boolean stopped = false;
    	
    	public void setStopped() {
    		stopped = true;
    	}
    	
        /**
         * DOCUMENT ME!
         */
        @Override
		public void run() {
        	long refTime = stopTime + offset;
        	// depending on file format or codec, the stop time that was set on the native
        	// player is not exactly the same as the passed stop time. E.g. when the value 6820
        	// is set on the native player, it might return a stop time of 6817
        	if (jdsPlayer.getStopTime() < refTime) {
        		refTime = jdsPlayer.getStopTime();
        	}

            while (playing && !stopped && (jdsPlayer.getMediaTime() < refTime)) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                	return;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            if (stopped) {
            	return;
            }
            syncLock.lock();
            try {
	            // if at stop time (i.e. not stopped by hand) do some extra stuff
	            if (playing) {
	                //jdsPlayer.stopWhenReady();// needed?
	            	jdsPlayer.pause();
	                stopControllers();
	                
	                // some mpeg2 codecs need an extra set media time to render the correct frame
	                // if needed undo stop time correction, see setStopTime for details
	                /*
	                if (!stopped) {// check again?
		                if (getMediaTime() == (stopTime + 1)) {
		                	System.out.println("State watch ST1: " + stopTime);
		                    setMediaTime(stopTime); // sometimes needed for mpeg2
		                } else {
		                	System.out.println("State watch MT: " + getMediaTime());
		                    setMediaTime(getMediaTime()); // sometimes needed for mpeg2
		                }
	                }
	                */
	                setControllersMediaTime(getMediaTime());
	                //jdsPlayer.pause();
	                setStopTime(duration);
	                playing = false;
	            }
            } finally {
            	syncLock.unlock();
            }
        }
    }
    
    /**
     * Thread waiting for the player to reach end of media in order to stop controllers. 
     * As soon as active
     * callback can be handled this class will become obsolete.
     */
    private class PlayerEndWatcher extends Thread {
    	private boolean stopped = false;
    	
    	public void setStopped() {
    		stopped = true;
    	}
    	
        /**
         * Waits for the end of media being reached
         */
        @Override
		public void run() {
            while (playing && !stopped && (getMediaTime() < duration)) {
                try {
                    Thread.sleep(300);
                }  catch (InterruptedException ie) {
                    ie.printStackTrace();
                	return;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
           
            if (stopped) {
            	return;
            }
            // if at stop time (i.e. not stopped by hand) do some extra stuff
            if (playing) {
                //jdsPlayer.stopWhenReady();// needed?
                jdsPlayer.pause();//??
                stopControllers();
                setControllersMediaTime(getMediaTime());
                //System.out.println("Pausing at end of media: " + mediaDescriptor.mediaURL + 
                //		" Time: " + getMediaTime());
                playing = false;
            }
        }
    }

}
