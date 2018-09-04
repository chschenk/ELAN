package mpi.eudico.client.annotator.player;

import javax.swing.JComponent;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.view.QTFactory;
import quicktime.app.view.QTJComponent;
import quicktime.std.StdQTConstants;
import quicktime.std.clocks.TimeBase;
import quicktime.std.clocks.TimeRecord;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.DataRef;


/**
 * A QuickTime based rtsp streaming media player for Windows.
 * 
 * Oct 2006 HS: added override of playInterval(long, long) to prevent 
 * QTExceptions when playing a selection. Also added setStopTime, but the change 
 * there might be made in the superclass.
 * Added an alternative for the Extremes callback class.
 * 
 * @author wwj $sidgrid$
 * @version 1.0
  */
public class QTStreamingPlayer extends QTMediaPlayer {
    private QTJComponent canvas;
    private JComponent visualComponent;
    private quicktime.app.view.MoviePlayer player;

    /**
     * Creates a new QTStreamingPlayer instance
     *
     * @param mediaDescriptor the (rtsp) media descriptor
     *
     * @throws NoPlayerException when the player could not be created.
     * Note that when the rtsp url is invalid (non-existing file) no 
     * exception is thrown by QT
     */
    public QTStreamingPlayer(MediaDescriptor mediaDescriptor)
        throws NoPlayerException {
        super(mediaDescriptor);

        String URLString = mediaDescriptor.mediaURL;

        System.out.println("mediaURL = " + URLString);

        try {
            if (URLString.startsWith("rtsp")) {
                DataRef dataRef = new DataRef(URLString);
                movie = Movie.fromDataRef(dataRef, StdQTConstants.newMovieActive);
                // this would cause the movie not to be created properly 
                //movie.setTimeScale(1000);
                //System.out.println("mts: " + movie.getTimeScale());// default = 600
            }

            player = new quicktime.app.view.MoviePlayer(movie);

            quicktime.std.image.Matrix matrix = new quicktime.std.image.Matrix();
            matrix.scale(4f, 4f, 0f, 0f);
            player.setMatrix(matrix);

            canvas = QTFactory.makeQTJComponent(player);
            visualComponent = canvas.asJComponent();
            visualComponent.addMouseListener(new MouseHandler());
            
            // callback for end of media
            timeBase = movie.getTimeBase();
            endOfMediaCallback = new PlayerExtremesCallBack(timeBase, StdQTConstants.triggerAtStop);
            endOfMediaCallback.callMeWhen();
            
            //stopMode = STOP_WITH_STOP_TIME;
        } catch (QTException e) {
            System.out.println("QTException while creating QT Streaming player ");
            e.printStackTrace();
            QTSession.close();
            throw new NoPlayerException("QTException while creating QT Streaming player.");
        }
    }

    /**
     * A slightly different framework description than the superclass.
     *
     * @return framework description
     */
    @Override
	public String getFrameworkDescription() {
        return "Streaming Quicktime For Java Media Player";
    }
    
    /**
     * @see QTMediaPlayer#createNewVisualComponent()
     */
    @Override
	public java.awt.Component createNewVisualComponent() {
        try {
            canvas = QTFactory.makeQTJComponent(player);
            visualComponent = canvas.asJComponent();
            visualComponent.addMouseListener(new MouseHandler());
        } catch (QTException qte) {
            System.out.println("Could not create a new visual component. ");
            qte.printStackTrace();
        }

        return visualComponent;
    }

    /**
     * @see ElanMediaPlayer#getVisualComponent()
     */
    @Override
	public java.awt.Component getVisualComponent() {
        return visualComponent;
    }

    /**
     * Play between two time values. This method uses a ExtremesCallBack 
     * to detect that the stop time is reached.
     *
     * @param startTime begin of selection
     * @param stopTime end of selection
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
			// time scale??, does this make sense whith streaming?
        	
			if (stopTime - startTime < 5000) {
				int loadBeginTime = startOff - 5000 > 0 ? (int)(startOff - 5000) : 0;
				int loadDuration = (int)(stopOff - loadBeginTime + 1000); 
				//System.out.println("Loading " + loadDuration + " milliseconds in ram");
				movie.loadIntoRam(loadBeginTime, loadDuration, StdQTConstants.unkeepInRam);
		//		movie.loadIntoRam(loadBeginTime, loadDuration, 0);//StdQTConstants.unkeepInRam);
			}
			
	        setMediaTime(startTime);
	        Thread.sleep(100);
								
        	movie.prePreroll((int)startOff, movie.getPreferredRate());
        	//movie.preroll((int)startOff, movie.getPreferredRate());// exception when streaming
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
     * Starts the Player as soon as possible. is not synchronized in JMF
     */
    @Override
	public synchronized void start() {
        if (movie == null) {
            return;
        }

        // do not try to start at the end of the media, the player blocks
        // start playing at the beginning of the media data
        if ((getMediaDuration() - getMediaTime()) < 40) {
            setMediaTime(0);
        }

        try {
            // make sure all managed controllers are started
            startControllers();
            // playback at another rate doesn't seem to be supported, 
            // but no exception is thrown, instead a "not supported" image is shown
            player.setRate(movie.getPreferredRate());
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
            player.setRate(0);
            // make sure all managed controllers are stopped
            stopControllers();
            setControllersMediaTime(getMediaTime());
        } catch (QTException qt) {
            qt.printStackTrace();
        }
    }

    /**
     * @see ElanMediaPlayer#setMediaTime(long)
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
            player.setTime((int)(time + offset));
            // set the media time for the connected controllers
            setControllersMediaTime(time);
        } catch (Exception qt) {
            qt.printStackTrace();
        }
    }

    /**
     * Some cleaning up to help garbage collection.
     */
    @Override
	public void cleanUpOnClose() {
        super.cleanUpOnClose();
        this.visualComponent = null;
        this.canvas = null;
        //dispose the player
        this.player = null;
    }
    
    /**
     * This class implements a method that is called when the movie/player reaches 
     * the defined stoptime.
     */
    private class PlayerExtremesCallBack
        extends quicktime.std.clocks.ExtremesCallBack {
        /**
         * Super class constructor
         *
         * @param tb the time base
         * @param flag callback flags
         *
         * @throws QTException any
         */
        public PlayerExtremesCallBack(TimeBase tb, int flag)
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
				//System.out.println("mt: " + getMediaTime() + " st: " + exactStopTime);
				player.setRate(0);
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
}
