package mpi.eudico.client.annotator.player;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.export.ImageExporter;
import mpi.eudico.client.annotator.gui.FormattedMessageDlg;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.display.QTCanvas;
import quicktime.app.time.TaskAllMovies;
import quicktime.app.time.Tasking;
import quicktime.app.view.MoviePlayer;
import quicktime.app.view.QTComponent;
import quicktime.app.view.QTFactory;
import quicktime.app.view.QTImageProducer;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.qd.Pict;
import quicktime.qd.PixMap;
import quicktime.qd.QDDimension;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.clocks.ExtremesCallBack;
import quicktime.std.clocks.TimeBase;
import quicktime.std.clocks.TimeRecord;
import quicktime.std.image.Matrix;
import quicktime.std.movies.Movie;
import quicktime.std.movies.MovieController;
import quicktime.std.movies.TimeInfo;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.DataRef;
import quicktime.std.movies.media.GenericMedia;
import quicktime.std.movies.media.MPEGMedia;
import quicktime.std.movies.media.Media;
import quicktime.std.movies.media.MovieMedia;
import quicktime.std.movies.media.StreamMedia;
import quicktime.std.movies.media.VideoMedia;
import quicktime.util.QTUtils;
import quicktime.util.RawEncodedImage;


/**
 * The QuickTime implementation of an elan media player
 */
public class QTMediaPlayer extends ControllerManager implements ElanMediaPlayer, ControllerListener, 
    VideoFrameGrabber, ActionListener, NeedsCreateNewVisualComponent {
	public static final int STOP_WITH_STOP_TIME = 0;
	public static final int STOP_WITH_PREVIEW = 1;
	//  private QTJComponent canvas;
    private QTComponent canvas;
    private float aspectRatio;
    protected long offset;
    private double milliSecondsPerSample;
	// private long intervalStopTime;
    boolean playingInterval;
    boolean streaming;
    //private PeriodicUpdateController periodicController;
    ExtremesCallBack endOfMediaCallback;
    private JPopupMenu popup;
    private boolean isWavPlayer;
    private MediaDescriptor mediaDescriptor;
    private ElanLayoutManager layoutManager;
    private boolean detached;
    private JMenuItem durationItem;
    private JMenuItem detachItem;
	private JMenuItem infoItem;
	private JMenuItem saveItem;
	private JMenuItem ratio_4_3_Item;
	private JMenuItem ratio_3_2_Item;
	private JMenuItem ratio_16_9_Item;
	private JMenuItem ratio_185_1_Item;
	private JMenuItem ratio_235_1_Item;
	private JMenuItem copyOrigTimeItem;
   // private VisualComponent visualComponent;    
	//private Panel visualComponent;
	private Component visualComponent;
    int stopMode;  
	TimeBase timeBase;
	long exactStopTime;
	protected Movie movie; //private Movie movie;
	private MovieController controller;
	
	private QTFile scratchFile;
	private String scratchName = "tmpMedia";
	static int scratchCount = 0;
	private boolean isEditSave = false;
	private int mpegImageWidth = 0;
	private int mpegImageHeight = 0;
	private boolean frameRateAutoDetected = false;
	private boolean rateDetectionAttempted = false;
	/** if true frame forward and frame backward always jump to the begin
	 * of the next/previous frame, otherwise it jumps with the frame duration */
	private boolean frameStepsToFrameBegin = false;
	private boolean pre47FrameStepping = false;
	private float curSubVolume;
	private boolean mute;
	/** stores a possible initialisation error */
	private static String initError = null;
	
    /**
     * Create a QTMediaPlayer for a media URL
     *
     * @param mediaDescriptor DOCUMENT ME!
     *
     * @throws NoPlayerException DOCUMENT ME!
     */
    public QTMediaPlayer(MediaDescriptor mediaDescriptor)
        throws NoPlayerException {
        this.mediaDescriptor = mediaDescriptor;

        // WebStart related initialization, see at the end of this file for details
        initQTJNI();

        if (initError != null) {
        	throw new NoPlayerException(initError);
        }
        
        try {
            
            QTSession.open();
            System.out.println("QuickTime version: " + QTSession.getQTMajorVersion() + "." + QTSession.getMinorVersion()
                    + "." + QTSession.getBugFixVersion());
            String URLString = mediaDescriptor.mediaURL;

            System.out.println("mediaURL = " + URLString);
            // test to see if this improves performance
            String qtNoSleep = System.getProperty("QTNoSleepTime");
            if (qtNoSleep != null && qtNoSleep.toLowerCase().equals("true")) {
            	try {
            		Tasking.tasker.setSleepTime(1);
            		System.out.println("QT setting sleep time off...");
            	} catch (Exception exc){}
            }

            //URLString = "rtsp://nt06.mpi.nl:80/De_Eng.mp4";
            //DataRef dataRef = new DataRef(rtspURL);
            //Movie mov = Movie.fromDataRef(dataRef, StdQTConstants.newMovieActive | StdQTConstants4.newMovieAsyncOK);
            // remove the file: part of the URL
            String fileString = URLString;

            if (URLString.startsWith("file:")) {
                streaming = false;
                fileString = URLString.substring(5);
	            QTFile qtFile = new QTFile(fileString);
	            movie = Movie.fromFile(OpenMovieFile.asRead(qtFile));
	            movie.setTimeScale(1000);
	            movie.update();
	
				Movie editMovie = createScratchMovie();
				if (editMovie != null) {
					movie = editMovie;
					isEditSave = true;
				}
            } else { // rtsp stuff
                streaming = true;
            	   DataRef dataRef = new DataRef(URLString);
                movie = Movie.fromDataRef(dataRef, StdQTConstants.newMovieActive);
            }
            
			quicktime.app.view.MoviePlayer player = new quicktime.app.view.MoviePlayer(movie);
			controller = new MovieController(movie);
			controller.activate();
			controller.setAttached(true);
			
			// this might help to let the video be scaled correctly in the ELAN window after loading??
            Matrix matrix = new Matrix();
            matrix.scale(1.1f, 1.1f, 0f, 0f);
            movie.setMatrix(matrix);
            
            if (URLString.endsWith("wav") || 
            	mediaDescriptor.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE)) {
                isWavPlayer = true; // ask api??
                canvas = null;
                visualComponent = null;
            } else {
            	if (URLString.startsWith("file")&& 
            		(URLString.endsWith("mpg") || URLString.endsWith("mpeg")) ) {
            		MPEGVideoHeader mpegHeader = new MPEGVideoHeader(fileString);
            		mpegImageWidth = mpegHeader.getWidth();
            		mpegImageHeight = mpegHeader.getHeight();
            		// System.out.println("MPEG w: " + mpegImageWidth + " - h: " + mpegImageHeight);
            	}
                //canvas = QTFactory.makeQTJComponent(player);
                //canvas.asJComponent().addMouseListener(new MouseHandler());
                
                canvas = QTFactory.makeQTComponent(movie);
            	   // canvas = QTFactory.makeQTComponent(controller);
                visualComponent = canvas.asComponent();
                visualComponent.addMouseListener(new MouseHandler());
                
		//		visualComponent = new VisualComponent(canvas.asComponent());
		//		visualComponent = new Panel();
		//		visualComponent.setLayout(null);
				
                popup = new JPopupMenu();
                durationItem = new JMenuItem(ElanLocale.getString(
                            "Player.duration") + ":  " +
                        TimeFormatter.toString(getMediaDuration()));
                durationItem.setEnabled(false);
				infoItem = new JMenuItem(ElanLocale.getString("Player.Info"));
				infoItem.addActionListener(this);
				saveItem = new JMenuItem(ElanLocale.getString("Player.SaveFrame"));
				saveItem.addActionListener(this);
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
				copyOrigTimeItem = new JMenuItem(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
				copyOrigTimeItem.addActionListener(this);
                popup.addSeparator();
                popup.add(saveItem);
                popup.add(infoItem);
                popup.add(arMenu);
                popup.add(durationItem);
                popup.add(copyOrigTimeItem);
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            }
			
	//		for (int i = 1; i <= movie.getTrackCount(); i++) {
	//			quicktime.std.movies.Track t = movie.getIndTrack(i);
	//			System.out.println("media " + t.getMedia());
	//			System.out.println("sample2 " + t.getMedia().sampleNumToMediaTime(1));
	//		}
			
			float boundsW = player.getOriginalSize().getWidth(); // 352     352
			float boundsH = player.getOriginalSize().getHeight();  // 288   240
			// System.out.println("w: " + boundsW + "     h: " + boundsH);
			aspectRatio = boundsW / boundsH;			
			System.out.println("Aspect ratio: " + aspectRatio);
			
			offset = mediaDescriptor.timeOrigin;
			
			// ask api
            milliSecondsPerSample = 40;
            // when called here, this causes a video to appear in the upper left corner of the screen,
		   // on top of the menubar
            //detectFrameRate();

            // callback for end of media
            timeBase = movie.getTimeBase();
            endOfMediaCallback = new TimeBaseExtremesCallBack(timeBase, StdQTConstants.triggerAtStop);
            endOfMediaCallback.callMeWhen();
			
            //stopMode = STOP_WITH_PREVIEW;//STOP_WITH_STOP_TIME;//
            // STOP_WITH_PREVIEW sometimes results in strange behavior after playing an interval:
            // the mediatime cannot be set to a value before the interval begintime
		   stopMode = STOP_WITH_STOP_TIME;
			
			movie.goToBeginning();
			
			setMediaTime(0L);
			
			if (isWavPlayer) {
			    // this causes a video to appear in the upper left corner of the screen,
			    // on top of the menubar
			    TaskAllMovies.addMovieAndStart();
			    //stopMode = STOP_WITH_STOP_TIME;//??
			}
		} catch (QTException e) {
            System.out.println("QTException while creating QT player ");
            e.printStackTrace();
            QTSession.close();
            throw new NoPlayerException("QTException while creating QT player.");
        } catch (UnsatisfiedLinkError ule) {
        	System.out.println("Unable to load the QuickTime libraries: " + ule.getMessage());
        	throw new NoPlayerException("Unable to load the QuickTime libraries: " + ule.getMessage());
        }
    }
    
    /**
     * Tries to detect the frame rate, and from there calculate the ms per frame value.
     * Not straightforward, especially for MPEG1 and MPEG2.
     */
    private void detectFrameRate() {
        try {
            rateDetectionAttempted = true;
            int scale = movie.getTimeScale();
            boolean isVisual = false;
            Track track = null;
            for (int i = 0; i < movie.getTrackCount(); i++) {
                track = movie.getTrack(i + 1);
                System.out.println("Media: " + track.getMedia().getClass().getName());
                if (track.getMedia() instanceof MovieMedia ||
                        track.getMedia() instanceof MPEGMedia ||
                        track.getMedia() instanceof VideoMedia ||
                        track.getMedia() instanceof StreamMedia ||
                        track.getMedia() instanceof GenericMedia) {// mpeg-2 identifies as generic media
                    isVisual = true;
                    break;
                }
            }

            if (!isVisual) {
                System.out.println("Media: non-video...");
                return;
            }
            int numTestFrames = 30;
            System.out.println("duration="+ movie.getDuration() + " timescale=" + movie.getTimeScale());
            movie.task(0);
            int flags = StdQTConstants.nextTimeStep;
            int[] modes = new int[]{StdQTConstants.visualMediaCharacteristic};
            TimeInfo info = movie.getNextInterestingTime(flags, modes, 0, 1.0f);
            System.out.println(info);
            int numframes = 0;
            int lastTime = info.time;
            int numDuplicateTimes = 0;
            while (info.time>=0 && numframes <= numTestFrames) {
                ++numframes;
                info = movie.getNextInterestingTime(flags, modes, info.time, 1.0f);
                
                if (info.time == lastTime) {
                    // something wrong??
                    numDuplicateTimes++;
                    System.out.println("T: same time...");
                    if (numDuplicateTimes == 3) {
                        break;    
                    }
                } else {
                    lastTime = info.time;
                }
                //System.out.println("T: " + info.time);
            }
            System.out.println("frames = "+ numframes + " cur time: " + info.time);
            float frameRate = 0f;
            if (numframes > numTestFrames) {
                frameRate = ((numframes + 1) * scale) / (float) info.time; 
            } else if (numframes > 0) {
                frameRate = (numframes * scale) / (float) info.time;
            } else {
                // ms per frame can not be calculated
                System.out.println("framerate could not be calculated");
                return;
            }
            System.out.println("framerate: " + frameRate);
            System.out.println("Ms per frame: " + Math.round(1000 / frameRate));
            if (frameRate > 0) {
                frameRateAutoDetected = true;
                milliSecondsPerSample = Math.round(1000 / frameRate);
            }
        } catch (QTException qte) {
            qte.printStackTrace();
        }
        
    }
    
    private class VisualComponent extends Panel implements ComponentListener, HierarchyListener {	
		boolean doNotify;
		
    	public VisualComponent(Component component) {
			doNotify = true;
			add(component);
    		addComponentListener(this);
    		addHierarchyListener(this);
    	}
		
		@Override
		public void componentResized(ComponentEvent e) {System.out.println("resized");

		}

		
		@Override
		public void hierarchyChanged(HierarchyEvent e) {System.out.println("hier");
		
		}
		
		@Override
		public void addNotify() {System.out.println("addNotify");
			if (doNotify) {
				super.addNotify();
				doNotify = false;
			}
/*			try {
				super.addNotify();
				add(canvas.asComponent());
	//			quicktime.qd.NativeGraphics.getContext(canvas).unlock();
			} catch (Exception e) {
				e.printStackTrace();
			}*/
		}
		
		@Override
		public void removeNotify() {System.out.println("removeNotify");
				super.removeNotify();
/*			try {
//				quicktime.qd.NativeGraphics.getContext(canvas).unlock();
				remove(canvas.asComponent());
				super.removeNotify();
			} catch (Exception e) {
				e.printStackTrace();
			}*/
		}

		@Override
		public void componentShown(ComponentEvent e) {System.out.println("show");
			
		}

		@Override
		public void componentHidden(ComponentEvent e) {System.out.println("hide");
			
		}

		@Override
		public void componentMoved(ComponentEvent e) {System.out.println("move");
		
		}
		
		@Override
		protected void finalize() throws Throwable {
			System.out.println("Finalize visual component");
			super.finalize();
		}

    }
	
    @Override
    public MediaDescriptor getMediaDescriptor() {
    	return mediaDescriptor;
    }
    
	public Movie getMovie() {
		if (isEditSave) {
			return movie;
		}
		return null;
	}
	
	/**
	 * Returns the (mpeg) media width and height as read from the mpegfile. 
	 * @return the dimension according to fields in the stream headers
	 */
	public Dimension getMediaFileDimension() {
		if (mpegImageWidth > 0 && mpegImageHeight > 0) {
			return new Dimension(mpegImageWidth, mpegImageHeight);
		}
		return null;
	}
	
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public String getFrameworkDescription() {
        return "Quicktime For Java Media Player";
    }

    /**
     * Elan controllerUpdate Used to stop at the stop time in cooperation with
     * the playInterval method
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public synchronized void controllerUpdate(ControllerEvent event) {

	}

    /**
     * play between two times. This method uses the contollerUpdate method to
     * detect if the stop time is passed. The setintervalStopTime method of
     * JMF can not be used because it gives unstable behaviour
     *
     * @param startTime DOCUMENT ME!
     * @param stopTime DOCUMENT ME!
     */
    @Override
    public void playInterval(long startTime, long stopTime) {
        if ((movie == null) || playingInterval || (stopTime <= startTime)) {
            return;
        }
	    long startOff = startTime + offset;
	    long stopOff = stopTime + offset;
        try {
			// load small intervals in memory 
			// time scale??
			if (stopTime - startTime < 5000) {
				int loadBeginTime = startOff - 5000 > 0 ? (int)(startOff - 5000) : 0;
				int loadDuration = (int)(stopOff - loadBeginTime + 1000); 
				//System.out.println("Loading " + loadDuration + " milliseconds in ram");
				movie.loadIntoRam(loadBeginTime, loadDuration, StdQTConstants.unkeepInRam);
		//		movie.loadIntoRam(loadBeginTime, loadDuration, 0);//StdQTConstants.unkeepInRam);
			}
	        setMediaTime(startTime);
	        Thread.sleep(100);
		/*						
        	movie.prePreroll((int)startOff, movie.getPreferredRate());
        	if (!streaming) {
        	    movie.preroll((int)startOff, movie.getPreferredRate());
        	}
        	*/
			/*
	        controller.setSelectionBegin(new TimeRecord (1000, startTime));
		    controller.setSelectionDuration(new TimeRecord (1000, stopTime - startTime));
		    controller.setLooping(true);
		    controller.setPlaySelection(true);
		    startControllers();
		    controller.play(1f);
		    */
			// correct stoptime for frame boundary?
			
			if (stopMode == STOP_WITH_STOP_TIME) {
				setStopTime(stopOff);
			} else {	
				movie.setPreviewTime((int)startOff, (int)(stopTime - startTime));
				movie.setPreviewMode(true);	
			}
			
			exactStopTime = stopTime;
			start();
			 
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}

    /**
     * Empty implementation for ElanMediaPlayer Interface
     * Only usefull for player that correctly supports setting stop time
     */
    @Override
    public void setStopTime(long stopTime) {
		try {
			timeBase.setStopTime(new TimeRecord(movie.getTimeScale(), stopTime));
		} catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the display Component for this Player.
     *
     * @return DOCUMENT ME!
     */
    @Override
    public java.awt.Component getVisualComponent() {
    	return visualComponent;
    	/*
        if (isWavPlayer) {
            return null;
        } else {
            //return canvas.asJComponent();

     //       return canvas.asComponent();
			return visualComponent;
        }
        */
        
    }
    
    @Override // NeedsCreateNewVisualComponent
    public java.awt.Component createNewVisualComponent() {
        if (isWavPlayer) {
            return null;
        } else {
            try {
                canvas = QTFactory.makeQTComponent(movie);
                visualComponent = canvas.asComponent();
                visualComponent.addMouseListener(new MouseHandler());
            } catch (QTException qte) {
                System.out.println("Could not create a new visual component. ");
                qte.printStackTrace();
            }
            
			return visualComponent;
        }        
    }

    /**
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceHeight()
     */
    @Override
    public int getSourceHeight() {
        if (movie != null) {
            try {
                return movie.getNaturalBoundsRect().getHeight();    
            } catch (QTException qte) {
                return 0;
            }
        }
        
        return 0;
    }
    
    /**
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#getSourceWidth()
     */
    @Override
    public int getSourceWidth() {
        if (movie != null) {
            try {
                return movie.getNaturalBoundsRect().getWidth();    
            } catch (QTException qte) {
                return 0;
            }
        }
        
        return 0;
    }
    
    /**
     * Gets the ratio between width and height of the video image
     *
     * @return DOCUMENT ME!
     */
    @Override
    public float getAspectRatio() {
        if (movie == null) {
            return 0;
        }

        //float aspectRatio = (float) (canvas.asJComponent().getPreferredSize()
        //                                   .getWidth() / canvas.asJComponent().getPreferredSize().getHeight());

        //float aspectRatio = (float) (canvas.asComponent().getPreferredSize().getWidth() /
        //									canvas.asComponent().getPreferredSize().getHeight());
        
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
        if (movie == null) {
            return;
        }

        // do not try to start at the end of the media, the JMF player blocks
        // start playing at the beginning of the media data
        if ((getMediaDuration() - getMediaTime()) < 40) {
            setMediaTime(0);
        }

        try {
            // make sure all managed controllers are started
            startControllers();
        	//System.out.println(" " + System.currentTimeMillis());
			movie.start();
        	//System.out.println(" " + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop the media player
     */
    @Override
    public synchronized void stop() {
        if (movie == null) {
            return;
        }

        try {
			movie.stop();

            // make sure all managed controllers are stopped
            stopControllers();
            setControllersMediaTime(getMediaTime());
		} catch (QTException qt) {
            qt.printStackTrace();
        }
	}

    /**
     * Tell if this player is playing
     *
     * @return DOCUMENT ME!
     */
    @Override
    public boolean isPlaying() {
        if (movie == null) {
            return false;
        }

        float rate = 0;

        try {
            rate = movie.getRate();
        } catch (Exception qt) {
            qt.printStackTrace();
        }

        return (rate != 0);
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
        if (movie == null) {
            return;
        }

        // do not set media time on a started player
        if (isPlaying()) {
            stop();
        }

        try {
            movie.setTime(new TimeRecord(movie.getTimeScale(), time + offset));
			
            // set the media time for the connected controllers
            setControllersMediaTime(time);
        } catch (Exception qt) {
            qt.printStackTrace();
        }
        
    }

    @Override
    public void nextFrame() {
        if (pre47FrameStepping) {
        	nextFramePre47();
        	return;
        }
        
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
			previousFramePre47();
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
        if (movie == null) {
            return 0;
        }
		
        long time = 0;

        try {
            time = movie.getTime();
        } catch (Exception qt) {
            qt.printStackTrace();
        }

        //System.out.println("get mt : " + (time - offset));
        return time - offset;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the step size for one frame
     */
    @Override
    public double getMilliSecondsPerSample() {
        if (!rateDetectionAttempted) {
            detectFrameRate();
        }
        return milliSecondsPerSample;
    }

    /**
     * DOCUMENT ME!
     *
     * @param milliSeconds the step size for one frame
     */
    @Override
    public void setMilliSecondsPerSample(long milliSeconds) {
        if (!frameRateAutoDetected) {
            milliSecondsPerSample = milliSeconds;
        }
        //milliSecondsPerSample = milliSeconds;
    }

    /**
     * Sets the temporal scale factor.
     *
     * @param rate DOCUMENT ME!
     */
    @Override
    public synchronized void setRate(float rate) {
        if (movie == null) {
            return;
        }

        // do not set rate on a started player
        if (isPlaying()) {
            stop();
        }

		try {
			movie.setPreferredRate(rate);
			setControllersRate(rate);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Gets the current temporal scale factor.
     *
     * @return DOCUMENT ME!
     */
    @Override
    public float getRate() {
        if (movie == null) {
            return 0;
        }

		float rate = 0;
		try {
			rate = movie.getPreferredRate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rate;
    }

    /**
     * @see mpi.eudico.client.annotator.player.ElanMediaPlayer#isFrameRateAutoDetected()
     */
    @Override
    public boolean isFrameRateAutoDetected() {
        if (!rateDetectionAttempted) {
            detectFrameRate();
        }
        return frameRateAutoDetected;
    }
    
    /**
     * Get the duration of the media represented by this object in milli
     * seconds.
     *
     * @return DOCUMENT ME!
     */
    @Override
    public long getMediaDuration() {
        if (movie == null) {
            return 0;
        }

        long duration = 0;

        try {
            duration = movie.getDuration();
        } catch (Exception qt) {
            qt.printStackTrace();
        }

        //System.out.println("dur " + duration);
        return duration - offset;
    }

    /**
     * Sets the volume as a number between 0 and 1
     *
     * @param volume DOCUMENT ME!
     */
    @Override
    public void setVolume(float volume) {
        if (movie == null) {
            return;
        }

        try {
            movie.setVolume(volume);
        } catch (Exception qt) {
            qt.printStackTrace();
        }
    }

    /**
     * Gets the volume as a number between 0 and 1
     *
     * @return DOCUMENT ME!
     */
    @Override
    public float getVolume() {
        if (movie == null) {
            return 0;
        }

        float volume = 0;

        try {
            volume = movie.getVolume();
        } catch (Exception qt) {
            qt.printStackTrace();
        }

        return volume;
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
     * Sets the way an interval is being played by QT.
     * @param mode the new stop mode, one of <code>STOP_WITH_PREVIEW</code> 
     * or <code>STOP_WITH_STOP_TIME</code>
     */
    public void setStopMode(int mode) {
    	if (mode == STOP_WITH_PREVIEW || mode == STOP_WITH_STOP_TIME) {
    		stopMode = mode;
    	}
    }
    
    /**
     * Attempts to create a scratch movie file for save editing.
     * This is to avoid modifications to an mpeg file when using 2d
     * annotations (i.e. adding tracks). 
     * 
     * @return the temp scratch movie, or null
     */
    private Movie createScratchMovie() {
		if (movie == null) {
			return null;
		}
		try{
			Media media = null;
			if (movie.getTrackCount() >= 1) {
				Track origTrack = movie.getTrack(1);
				media = origTrack.getMedia();
				if (media instanceof MPEGMedia) {
					isWavPlayer = false;
					// create a new editable movie object to prevent modification of 
					// mpg files when using svg/graphic annotations	
					File tempFile = new File(Constants.ELAN_DATA_DIR, (scratchName + scratchCount++));
					// if deletion did fail on last exit...
					if (tempFile.exists()) {
						// if it is more than 24 hours old
						long modified = tempFile.lastModified();
						long age = System.currentTimeMillis() - modified;
						if (age >= 24 * 60 * 60 * 1000) {
							tempFile.delete();
						}
					}
					
					scratchFile = new QTFile(tempFile);
					
					scratchFile.createMovieFile(QTUtils.toOSType("TOVD"),
						StdQTConstants.newMovieActive 
						| StdQTConstants.createMovieFileDeleteCurFile );
	
					scratchFile.deleteOnExit();
					File resourceFile = new File(scratchFile.getAbsolutePath() + ".#res");

					if (resourceFile.exists()) {
						resourceFile.deleteOnExit();						
					}
						
					//Movie editMovie = Movie.createMovieFile(scratchFile, QTUtils.toOSType("TOVD"),
					//	StdQTConstants.newMovieActive | StdQTConstants.createMovieFileDeleteCurFile |
					//	StdQTConstants.createMovieFileDontCreateResFile);
					Movie editMovie = new Movie();
					int dataRefCount = media.getDataRefCount();
					DataRef mediaDataRef = media.getDataRef(dataRefCount);
					QDDimension origSize = origTrack.getSize();
					Track editTrack = editMovie.newTrack(origSize.getWidthF(), origSize.getHeightF(), 
						origTrack.getVolume());
					int ts = movie.getTimeScale();
					//Media editMedia = new MPEGMedia(editTrack, ts, mediaDataRef);
					/*Media editMedia = */Media.newFromType(StdQTConstants.MPEGMediaType,
						editTrack, ts, mediaDataRef);
					int duration = origTrack.getDuration();
	
					origTrack.insertSegment(editTrack, 0, duration, 0);
					origTrack.copySettings(editTrack);
					OpenMovieFile omf = OpenMovieFile.asWrite(scratchFile);
	
					editMovie.addResource(omf, 0, scratchFile.getName());
					omf.close();
						
					Movie nextMovie = Movie.fromFile(OpenMovieFile.asRead(scratchFile));
					if (nextMovie != null) {
						nextMovie.setTimeScale(ts);
						nextMovie.update();
						/*
						System.out.println("Edit Movie: duration: " + nextMovie.getDuration());
						System.out.println("Edit Movie: num tracks: " + nextMovie.getTrackCount());
						System.out.println("Edit Movie: num samples: " + nextMovie.getTrack(1).getMedia().getSampleCount());
						*/
						QTUtils.reclaimMemory();
						
						return nextMovie;
					}	
				}
			}
		} catch (QTException qte) {
			qte.printStackTrace();
		}
    	return null;
    }

	/**
	 * Grabs the current video frame and converts it to an Image object.
	 * 
	 * @return the current video frame
	 */
    @Override
	public Image getCurrentFrameImage() {
		return getFrameImageForTime(getMediaTime());
	}
	
	/**
	 * Grabs the frame for the given time and converts it to a BufferedImage.<br>
	 * The size of the image is determined by the size read from the header of 
	 * the mediafile; QT often uses another size for MPEG-1 files.
	 * QT on Windows uses another default pixel format than QT on the Mac; the current 
	 * implementation seems to work on both platforms.
	 * 
	 * @param time the media time for the frame 
	 * @return the frame image or null
	 */	
    @Override
	public Image getFrameImageForTime(long time) {
		if (1 + 1 == 0) {// switch for testing alternatives
			return getFrameImageForTime2(time);
		}
		try {
			// by default mpeg movies seem to be sized to 320 x 240 by qt
			// this is not right...
			int w = 352;
			int h = 288;
			
			if (mpegImageWidth > 0 && mpegImageHeight > 0) {
				w = mpegImageWidth;
				h = mpegImageHeight;
			} else {
				//w = (int)(movie.getNaturalBoundsRect().getWidth() * movie.getMatrix().getSx());
				//h = (int)(movie.getNaturalBoundsRect().getHeight() * movie.getMatrix().getSy());
				w = movie.getNaturalBoundsRect().getWidth();
				h = movie.getNaturalBoundsRect().getHeight();
			}

			Pict pict = movie.getTrack(1).getPict((int)time);
			//Pict pict = movie.getPict((int)(time / milliSecondsPerSample));
			//System.out.println("Def pixel format: " + QDGraphics.kDefaultPixelFormat);
			// Windows: 1111970369 == k32BGRAPixelFormat
			// MACOS: 32 == k32ARGBPixelFormat
			QDGraphics offScr = new QDGraphics(QDGraphics.kDefaultPixelFormat, new QDRect(0, 0, w, h));
			pict.draw(offScr, offScr.getBounds());

			PixMap pixmap = offScr.getPixMap();
			RawEncodedImage raw = RawEncodedImage.fromPixMap(pixmap);
			// copy bytes to an array
			int intsPerRow = pixmap.getRowBytes()/4;
			int[] pixels = new int [intsPerRow * h];
			raw.copyToArray(0, pixels, 0, pixels.length);

			DirectColorModel model = new DirectColorModel(
				32, // bits/sample
				0x00ff0000, // R
				0x0000ff00, // G
				0x000000ff, // B
				0x00000000); // ignore alpha

			Image image = Toolkit.getDefaultToolkit().createImage(
				new MemoryImageSource(w, h, model, pixels, 0, intsPerRow));
				
			//QTUtils.reclaimMemory();
			
			return image;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Image getFrameImageForTime2(long time) {
		try {
			// by default mpeg movies seem to be sized to 320 x 240 by qt
			// this is not right...
			int w = 352;
			int h = 288;
			
			if (mpegImageWidth > 0 && mpegImageHeight > 0) {
				w = mpegImageWidth;
				h = mpegImageHeight;
			} else {
				//w = (int)(movie.getNaturalBoundsRect().getWidth() * movie.getMatrix().getSx());
				//h = (int)(movie.getNaturalBoundsRect().getHeight() * movie.getMatrix().getSy());
				w = movie.getNaturalBoundsRect().getWidth();
				h = movie.getNaturalBoundsRect().getHeight();
			}
			MoviePlayer pl = new MoviePlayer(movie);
			pl.setTime((int) time);
			
			QTImageProducer ip = new QTImageProducer(pl, new Dimension(w, h));
			Image img = Toolkit.getDefaultToolkit().createImage(ip);
			return img;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * If a scratch file has been created, delete it.
	 */
        @Override
		protected void finalize() throws Throwable {
	    System.out.println("Finalize QT player...");
		if (scratchFile != null && scratchFile.exists()) {
			File resourceFile = new File(scratchFile.getAbsolutePath() + ".#res");

			if (resourceFile.exists()) {
				resourceFile.delete();						
			}
			scratchFile.delete();
		}
		if (isWavPlayer) {
		    TaskAllMovies.removeMovie();
		}
		QTSession.close();
		QTUtils.reclaimMemory();
		super.finalize();
	}

    // Greg's code
    // for WIN32 under JNLP load QT JNI librarys manualy. OS-X does not require this
    private void initQTJNI() {
        if (System.getProperty("os.name").regionMatches(false, 0, "Win", 0, 3)) {
        	try {
        		System.loadLibrary("QTJava");
        	} catch (UnsatisfiedLinkError ule) {
        		System.out.println(ule.getMessage());
        		//initError = ule.getMessage();
        	} catch (Throwable tr) {
        		System.out.println(tr.getMessage());
        		//initError = tr.getMessage();
        	}
        	try {
        		System.loadLibrary("QTJavaNative");
        	} catch (UnsatisfiedLinkError ule) {
        		System.out.println(ule.getMessage());
        		// is this fatal?
        	} catch (Throwable tr) {
        		System.out.println(tr.getMessage());
        		//initError = tr.getMessage();
        	}
        	try {
        		System.loadLibrary("QTJNative");
        	} catch (UnsatisfiedLinkError ule) {
        		System.out.println(ule.getMessage());
        		// is this fatal?
        	} catch (Throwable tr) {
        		System.out.println(tr.getMessage());
        		//initError = tr.getMessage();
        	}
        }
        
        try {
        	Class c = Class.forName("quicktime.QTException");
        	System.out.println("QTJava found...");
        } catch (ClassNotFoundException cnfe) {
        	initError = cnfe.getMessage();
        } catch (Throwable tr) {
        	initError = "QTJava was not found or could not be loaded";
        }
    }

    /**
     * Private class that extends a QTCanvas and keeps the mouse events away
     * from QT otherwise the media rendering will be stopped by a mouse click
     * in the visual component panel
     *
     * @param layoutManager DOCUMENT ME!
     */

		private class QTCanvasNoMouse extends QTCanvas {
    		@Override
			public void addMouseListener(java.awt.event.MouseListener ml) {
    			if (ml instanceof MouseHandler) {
    				super.addMouseListener(ml);
    			}
    		}
    	}
        
    @Override
    public void setLayoutManager(ElanLayoutManager layoutManager) {
        if (this.layoutManager == null  && !isWavPlayer) {
            			detachItem = new JMenuItem(ElanLocale.getString("Detachable.detach"));
            			detachItem.addActionListener(this);
            			popup.insert(detachItem, 0);
        }

        this.layoutManager = layoutManager;
    }

    /*
     *
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(detachItem) && (layoutManager != null)) {
            if (detached) {
                layoutManager.attach(QTMediaPlayer.this.getVisualComponent());
                detachItem.setText(ElanLocale.getString("Detachable.detach"));
                detached = false;
            } else {
                layoutManager.detach(QTMediaPlayer.this.getVisualComponent());
                detachItem.setText(ElanLocale.getString("Detachable.attach"));
                detached = true;
            }
        } else if (e.getSource() == infoItem) {
			new FormattedMessageDlg(this);
		} else if (e.getSource() == saveItem) {
			ImageExporter export = new ImageExporter(layoutManager.getElanFrame());
			export.exportImage(getCurrentFrameImage());
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

    /**
     * DOCUMENT ME!
     */
    @Override
    public void updateLocale() {
    	if (infoItem != null) {
			infoItem.setText(ElanLocale.getString("Player.Info"));
    	}
        if (durationItem != null) {
            durationItem.setText(ElanLocale.getString("Player.duration") +
                ":  " + TimeFormatter.toString(getMediaDuration()));
        }
        if (saveItem != null) {
			saveItem.setText(ElanLocale.getString("Player.SaveFrame"));
        }		

        if (detachItem != null) {
            if (detached) {
                detachItem.setText(ElanLocale.getString("Detachable.attach"));
            } else {
                detachItem.setText(ElanLocale.getString("Detachable.detach"));
            }
        }
        if (copyOrigTimeItem != null) {
        	copyOrigTimeItem.setText(ElanLocale.getString("Player.CopyTimeIgnoringOffset"));
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

    /**
     * DOCUMENT ME!
     * $Id: QTMediaPlayer.java 44863 2016-01-14 15:44:21Z hasloe $
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
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				if (layoutManager != null && layoutManager.isAttached(QTMediaPlayer.this)) {
					if (detached) {
						detached = false;
						detachItem.setText(ElanLocale.getString("Detachable.detach"));
					}
				}
                popup.show(getVisualComponent(), e.getPoint().x, e.getPoint().y);
            }
        }
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2) {
				if (layoutManager != null) {
					layoutManager.setFirstPlayer(QTMediaPlayer.this);
				}
				return;
			}
            if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				if (layoutManager != null && layoutManager.isAttached(QTMediaPlayer.this)) {
					if (detached) {
						detached = false;
						detachItem.setText(ElanLocale.getString("Detachable.detach"));
					}
				}
                popup.show(getVisualComponent(), e.getPoint().x, e.getPoint().y);
                return;
            }
            //System.out.println("X: " + e.getX() + " Y: " + e.getY());
            //System.out.println("CW: " + visualComponent.getWidth() + " CH: " + visualComponent.getHeight());
            try {
                //System.out.println("OW: " + movie.getNaturalBoundsRect().getWidth() + " OH: " + 
            	   //    movie.getNaturalBoundsRect().getHeight());
                if (e.isAltDown()) {
                	   copyToClipboard(format.format(e.getX() / (float)visualComponent.getWidth()) + "," 
                			   + format.format(e.getY() / (float)visualComponent.getHeight()));
                }  else if (e.isShiftDown()){
                    copyToClipboard("" + (int)((movie.getNaturalBoundsRect().getWidth() / (float)visualComponent.getWidth()) * e.getX()) 
                		    + "," + (int)((movie.getNaturalBoundsRect().getHeight() / (float)visualComponent.getHeight()) * e.getY()));
                } else {
                    copyToClipboard("" + (int)((movie.getNaturalBoundsRect().getWidth() / (float)visualComponent.getWidth()) * e.getX()) 
                		    + "," + (int)((movie.getNaturalBoundsRect().getHeight() / (float)visualComponent.getHeight()) * e.getY())
                		    + " [" + movie.getNaturalBoundsRect().getWidth() + "," + movie.getNaturalBoundsRect().getHeight() + "]");
                }
            } catch (Exception exep) {
            	   exep.printStackTrace();
            }
        }
    }

    /**
     * This class implements a method that is called when the movie stops
     */
    private class TimeBaseExtremesCallBack
        extends quicktime.std.clocks.ExtremesCallBack {
        /**
         * Super class constructor
         *
         * @param tb DOCUMENT ME!
         * @param flag DOCUMENT ME!
         *
         * @throws QTException DOCUMENT ME!
         */
        public TimeBaseExtremesCallBack(TimeBase tb, int flag)
            throws QTException {
            super(tb, flag);
        }

        /**
         * Make sure all the connected controllers are stopped at end of media
         */
        @Override
		public void execute() {
			//System.out.println("extremes callback");

			try {	
				movie.stop();
				stopControllers();
				setMediaTime(exactStopTime);  // de troep stopt vaak enkele milli seconden te vroeg.
				if (stopMode == STOP_WITH_STOP_TIME) {
					setStopTime(movie.getDuration());	
				} else {
					movie.setPreviewMode(false);
				}
				exactStopTime = movie.getDuration();
                endOfMediaCallback.callMeWhen();
            } catch (Exception e) {
				e.printStackTrace();
			}
			
        }
    }

    /**
     * Release resources to be ready for the garbage collector...?
     */
    @Override
	public void cleanUpOnClose() {
	    System.out.println("Clean up QT media player...");
		visualComponent = null;
		layoutManager = null;
		mediaDescriptor = null;
		try {
			timeBase.disposeQTObject();
			endOfMediaCallback.cancelAndCleanup();
			controller.deactivate();
			controller.disposeQTObject();
			canvas = null;
			movie = null;
			controller = null;
		} catch (QTException qte) {
		    qte.printStackTrace();
		}
		
	}


}
