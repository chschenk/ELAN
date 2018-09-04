package nl.mpi.jmmf;

import java.awt.Component;
import java.awt.Dimension;

/**
 * A player that contains methods that mostly wrap a Microsoft Media Foundation
 * function, accessed via JNI.
 * All methods accepting time values expect millisecond values and pass them 
 * to the native function in "reference time", units of 100 nanoseconds.
 * All methods returning time values receive values as "reference time" values 
 * from the native functions and return them as milliseconds.s  
 *  
 * @author Han Sloetjes
 */
public class JMMFPlayer {
	/** the default time format in Direct Show/MMF is Reference Time, units of 100 nanoseconds */
	private static final int MS_TO_REF_TIME = 10000;
    /** The stopped state */
    public final static int STATE_STOP = 0;

    /** The pause state */
    public final static int STATE_PAUSE = 1;

    /** The running state */
    public final static int STATE_RUN = 2;
    /**
     * An enumeration that corresponds to the player states in the native MMFPlayer.
     */
    public enum PlayerState {
    	NO_SESSION (0),
    	READY(1),
    	OPENING(2),
    	STARTED(3),
    	PAUSED(4),
    	SEEKING(5),
    	STOPPED(6),
    	CLOSING(7),
    	CLOSED(8);
    	// index
    	public int value;
    	// constructor with state value
    	PlayerState (int value) {
    		this.value = value;
    	}
    }
    
    private static String initError = null;
    
	static {
		try {
			// don't use SystemReporting here
			if (System.getProperty("os.name").indexOf("Vista") > -1) {
				System.loadLibrary("MMFPlayerVista");// somehow on my machine WIn7 reports as Vista

			} else {
				System.loadLibrary("MMFPlayer");// assume win 7 or >
			}
			String debug = System.getProperty("JMMFDebug");
			if (debug != null && debug.toLowerCase().equals("true")) {
				JMMFPlayer.enableDebugMode(true);
			}
			String correctAtPause = System.getProperty("JMMFCorrectAtPause");
			if (correctAtPause != null) {
				if (correctAtPause.toLowerCase().equals("true")) {
					JMMFPlayer.correctAtPause(true);
				} else if (correctAtPause.toLowerCase().equals("false")) {
					JMMFPlayer.correctAtPause(false);	
				}
				
			}
		} catch (UnsatisfiedLinkError ue) {
			initError = ue.getMessage();
		} catch (Throwable th) {
			initError = th.getMessage();
		}
	}
	
	private String mediaPath;
	//private URL mediaURL;
	private long id = -1;// not inited
	private Component visualComponent;
	// initialize as true in order to be sure it will tried at least once
	private boolean stopTimeSupported = true;
	private boolean allowVideoScaling = true;
	private float videoScaleFactor = 1f;
	private int vx = 0, vy = 0, vw = 0, vh = 0;
	private int vdx = 0, vdy = 0;
	// in synchronous mode all calls that "normally" are performed asynchronous
	// are performed synchronous. These methods only return after the action has 
	// fully been performed
	private boolean synchronousMode = false;// temporarily true
	
	public JMMFPlayer() throws JMMFException {
		super();
		
		if (initError != null) {
			throw new JMMFException(initError);
		}
		//id = initWithFileAndOwner(this.mediaPath, this.visualComponent);
		id = initPlayer(synchronousMode);
		
		//System.out.println("Java Id: " + id);
	}
	
	/**
	 * Constructor with a flag for synchronous or asynchronous mode of operation.
	 * 
	 * @param synchronous if true the player runs in synchronous mode, asynchronous otherwise 
	 * @throws JMMFException
	 */
	public JMMFPlayer(boolean synchronous) throws JMMFException {
		super();
		
		if (initError != null) {
			throw new JMMFException(initError);
		}
		synchronousMode = synchronous;
		//id = initWithFileAndOwner(this.mediaPath, this.visualComponent);
		id = initPlayer(synchronousMode);
		
		//System.out.println("Java Id: " + id);
	}

	public JMMFPlayer(String mediaPath) throws JMMFException {
		super();
		if (initError != null) {
			throw new JMMFException(initError);
		}
		//check path
		this.mediaPath = mediaPath;
		
		id = initWithFile(this.mediaPath, synchronousMode);
	}

	/**
	 * COnstructor with media path and flag for player mode, synchronous or asynchronous.
	 * 
	 * @param mediaPath the location of the media file
	 * @param synchronous if true the player will operate in synchronous mode, 
	 * asynchronous otherwise
	 * @throws JMMFException
	 */
	public JMMFPlayer(String mediaPath, boolean synchronous) throws JMMFException {
		super();
		if (initError != null) {
			throw new JMMFException(initError);
		}
		//check path
		this.mediaPath = mediaPath;
		synchronousMode = synchronous;
		
		id = initWithFile(this.mediaPath, synchronousMode);

	}
	
//	public JMMFPlayer(URL mediaURL) throws JMMFException {
//		super();
//		this.mediaURL = mediaURL;
//	}
	
	public boolean isSynchronousMode() {
		return synchronousMode;
	}

	public void start() {
		start(id);
	}
	
	/**
	 * In most cases stop means pause
	 */
	public void stop() {
		pause(id);
	}
	
	public void pause() {
		pause(id);
	}
	
	public boolean isPlaying() {
		return isPlaying(id);
	}
	
	public int getState() {
		return getState(id);
	}
	
	public void setRate(float rate) {
		setRate(id, rate);
	}
	
	public float getRate() {
		return getRate(id);
	}
	
	public void setVolume(float volume) {
		setVolume(id, volume);
	}
	
	public float getVolume() {
		return getVolume(id);
	}
	
	public void setMediaTime(long time) {
		setMediaTime(id, MS_TO_REF_TIME * time);
	}
	
	public long getMediaTime() {
		return getMediaTime(id) / MS_TO_REF_TIME;
	}
	
	public long getDuration() {
		return getDuration(id) / MS_TO_REF_TIME;
	}
	
	/**
	 * Move a frame forward, implemented on a lower level.
	 * 
	 * @param atFrameBegin if true try to position the media at the frame start boundary
	 * @return the (natively) calculated media time where the player should jump to,
	 * in milliseconds
	 */
	public double nextFrame(boolean atFrameBegin) {
		return nextFrameInternal(id, atFrameBegin) / MS_TO_REF_TIME;
	}

	/**
	 * Move a frame backward, implemented on a lower level.
	 * 
	 * @param atFrameBegin if true try to position the media at the frame start boundary
	 * @return the (natively) calculated media time where the player should jump to,
	 * in milliseconds
	 */
	public double previousFrame(boolean atFrameBegin) {
		return previousFrameInternal(id, atFrameBegin) / MS_TO_REF_TIME;
	}
	
	public boolean isStopTimeSupported() {
		return stopTimeSupported;
	}
	
	public void setStopTime(long time) {
		if (stopTimeSupported) {
			try {
				setStopTime(id, MS_TO_REF_TIME * time);
			} catch (JMMFException jds) {
				stopTimeSupported = false;
				System.out.println(jds.getMessage());
			}
		}
	}
	
	public long getStopTime() {
		if (stopTimeSupported) {
			return getStopTime(id) / MS_TO_REF_TIME;
		}
		return 0L;
	}
	
	public double getFrameRate() {
		return getFrameRate(id);
	}
	
	/**
	 * Retrieves the player's average time per frame, which is in seconds,
	 * and returns the value in milliseconds.
	 * 
	 * @return the average time per frame in ms.
	 */
	public double getTimePerFrame() {
		return getTimePerFrame(id) * 1000;
	}
	
	public float getAspectRatio() {
		return getAspectRatio(id);
	}
	
	public Dimension getOriginalSize() {
		return getOriginalSize(id);
	}
	
	public boolean isVisualMedia() {
		return isVisualMedia(id);
	}
	
	public int[] getPreferredAspectRatio() {
		return getPreferredAspectRatio(id);
	}
	
	public int getSourceHeight() {
		return getSourceHeight(id);
	}
	
	public int getSourceWidth() {
		return getSourceWidth(id);
	}
	
	public void setVisualComponent(Component component) {
		if (this.visualComponent == null) {
			this.visualComponent = component;
			setVisualComponent(id, component);
			// hier... start checking player state
		} else {
			this.visualComponent = component;
			setVisualComponent(id, component);
		}
	}
	
	public Component getVisualComponent() {
		return visualComponent;
	}
	
	public void setVisualComponentSize(int w, int h) {
		//setVisualComponentPos(id, x, y, w, h);
		repositionVideoRect();
		//int dx = Math.max(w, getSourceWidth());
		//int dy = Math.max(h, getSourceHeight());
		//setVideoDestinationPos(id, x, y, w, h);
		//setVideoDestinationPos(id, x, y, dx, dy);
		//float sx = Math.min(1, w / (float) getSourceWidth());
		//float sy = Math.min(1, h / (float) getSourceHeight());
		//setVideoSourcePos(id, 0f, 0f, 1f, 1f);
		//setVideoSourcePos(id, 0f, 0f, sx, sy);
		//setVideoSourceAndDestPos(0f, 0f, 1f, 1f, 0, 0, w, h);
	}
	
	public void setVisible(boolean visible) {
		setVisible(id, visible);
	}
	
	/**
	 * The source rectangle is the part of the video that is displayed on the 
	 * "window", expressed in values between 0 and 1 ({0,0} is the left top
	 * corner, {1,1} the right bottom corner).
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void setVideoSourcePos(float x, float y, float w, float h) {
		setVideoSourcePos(id, x, y, w, h);
	}

	public void setVideoDestinationPos(int x, int y, int w, int h) {
		setVideoDestinationPos(id, x, y, w, h);
	}
	
	public void setVideoSourceAndDestPos(float sx, float sy, float sw, float sh, 
			int x, int y, int w, int h) {
		setVideoSourceAndDestPos(id, sx, sy, sw, sh, x, y, w, h);
	}
	
	public int[] getVideoDestinationPos() {
		return getVideoDestinationPos(id);
	}
	
	public float getVideoScaleFactor() {
		return videoScaleFactor;
	}

	public void setVideoScaleFactor(float videoScaleFactor) {
		this.videoScaleFactor = videoScaleFactor;
		repositionVideoRect();
	}
	
	/**
	 * Moves the video (inside the component, or relative to the component).
	 * 
	 * @param dx distance to move along the x axis
	 * @param dy distance to move along the y axis
	 */
	public void moveVideoPos(int dx, int dy) {
		if (videoScaleFactor == 1 && allowVideoScaling) {
			return;// the video always fills the whole component
		}
		vdx += dx;
		//vdx = vdx > 0 ? 0 : vdx;
		vdy += dy;
		//vdy = vdy > 0 ? 0 : vdy;
		repositionVideoRect();
//		int[] currentDestPos = getVideoDestinationPos();
//		if (currentDestPos != null) {
//			setVideoDestinationPos(currentDestPos[0] + dx, currentDestPos[1] + dy, 
//					currentDestPos[2], currentDestPos[3]);
//			repaintVideo();
//		}
	}
	
	/**
	 * Returns the current video translation.
	 * @return array of size 2, the translation along the x and y axis 
	 */
	public int[] getVideoTranslation() {
		if (!isVisualMedia() || videoScaleFactor == 1) {
			return new int[] {0, 0};
		} else {
			return new int[] {vx, vy};
		}
	}

	/**
	 * Returns the current, scaled video size. This can be different from the component size
	 * on which the video is displayed.
	 * 
	 * @return array of size 2, the scaled video width and scaled video height 
	 */
	public int[] getScaledVideoRect() {
		if (!isVisualMedia() || videoScaleFactor == 1) {
			return new int[] {0, 0};
		} else {
			return new int[] {vw, vh};
		}
	}
	
	public boolean isAllowVideoScaling() {
		return allowVideoScaling;
	}

	public void setAllowVideoScaling(boolean allowVideoScaling) {
		this.allowVideoScaling = allowVideoScaling;
	}
	
	/**
	 * Returns the current image; it is retrieved from the renderer, 
	 * so the size might not be the original size.
	 */
	public byte[] getCurrentImageData(DIBInfoHeader dih) {
		return getCurrentImage(id, dih);
	}
	
	/**
	 * Currently returns the current image.
	 */
	public byte[] getImageDataAtTime(long time, DIBInfoHeader dih) {
		return getImageAtTime(id, dih, MS_TO_REF_TIME * time);
	}
	
	/**
	 * First make sure the player is stopped, then close the session
	 * then delete the player.
	 */
	public void cleanUpOnClose() {
		//clean(id);
		final int STOP_TO = 7000;
		final int CLOSE_TO = 10000;
		boolean stopped = false;
		boolean closed = false;
		if (getState(id) != PlayerState.STOPPED.value) {
			stop(id);
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < STOP_TO) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ie) {
					System.out.println("Interrupted while waiting for player to stop.");
				}
				if (getState(id) == PlayerState.STOPPED.value) {
					System.out.println("Player succesfully stopped.");
					stopped = true;
					break;
				}
			}
		}
		if (!stopped) {
			// is this fatal?
		}
		
		if (getState(id) != PlayerState.CLOSING.value) {
			closeSession(id);
			
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < CLOSE_TO) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ie) {
					System.out.println("Interrupted while waiting for player to close session.");
				}
				if (getState(id) == PlayerState.CLOSED.value) {
					System.out.println("Player session succesfully closed.");
					closed = true;
					break;
				}
			}
		}
		
		if (!closed) {
			// if the session is not closed don't delete the player in order to avoid a crash
			System.out.println("Error: failed to close the player gracefully.");
		} else {
			deletePlayer(id);
		}
	}
	
	public long getID() {
		return id;
	}
	
	private void repositionVideoRect() {
		if (visualComponent != null) {
			if (!allowVideoScaling) {
//				int compW = visualComponent.getWidth();
//				int compH = visualComponent.getHeight();
//				int origW = getSourceWidth();
//				int origH = getSourceHeight();
//				float sx = 0f;
//				float sy = 0f;
//				float sw = 1f;
//				float sh = 1f;
//				float factW = compW / (float) origW;
//				float ar = origW / (float) origH;
//				if (factW > 1) {// component bigger than video
//					
//				} else {
//					
//				}
			} else {
				int compW = visualComponent.getWidth();
				int compH = visualComponent.getHeight();
				if (videoScaleFactor == 1) {
					setVideoSourceAndDestPos(0, 0, 1, 1, 0, 0, compW, compH);
				} else {
					vw = (int) (compW * videoScaleFactor);
					vh = (int) (compH * videoScaleFactor);
					vx = vdx;
					vy = vdy;
					if (vx + vw < compW) {
						vx = compW - vw;
					}
					if (vx > 0) {
						vx = 0;
					}
					if (vy + vh < compH) {
						vy = compH - vh;
					}
					if (vy > 0) {
						vy = 0;
					}
					//vx = -vx;
					//vy = -vy;
					float sx1 = (float) -vx / vw;
					float sy1 = (float) -vy / vh;
					float sx2 = sx1 + ((float) compW / vw);
					float sy2 = sy1 + ((float) compH / vh);
					//setVideoDestinationPos(id, vx, vy, vw, vh);
					setVideoSourceAndDestPos(sx1, sy1, sx2, sy2, 0, 0, compW, compH);
				}
			}
		}
	}
	
	public void repaintVideo() {
		if (visualComponent != null) {
			repaintVideo(id);
		}
	}
	// internal 	
	/**
	 * Calculates and sets the new media position with the highest possible accuracy.
	 * Calculations are performed using the native time units of the Media Foundation, 
	 * units of 100 nanoseconds.
	 * 
	 * @param id the id of the native player
	 * @param atFrameBegin if true the player cursor should be placed at the beginning 
	 * of the next frame
	 * 
	 * @return the next calculated media position as a double, in units of 100 nanoseconds
	 */
	private double nextFrameInternal(long id, boolean atFrameBegin) {
		long curTime = getMediaTime(id); // in units of 100 nanoseconds
		double perFrame = getTimePerFrame(id); // in seconds
		double perFrameNano = perFrame * 1000 * MS_TO_REF_TIME;
		double nextMediaPosition;
		
		if (atFrameBegin) {
			long curFrame = (long) (curTime / perFrameNano);
			nextMediaPosition = (curFrame + 1) * perFrameNano;
		} else {
			nextMediaPosition = curTime + perFrameNano;
		}
		//System.out.println("Next Frame in Nano: " + nextMediaPosition);
		setMediaTime(id, (long) Math.ceil(nextMediaPosition));
		
		return nextMediaPosition;
	}
	
	/**
	 * Calculates and sets the new media position with the highest possible accuracy.
	 * Calculations are performed using the native time units of the Media Foundation, 
	 * units of 100 nanoseconds.
	 * 
	 * @param id the id of the native player
	 * @param atFrameBegin if true the player cursor should be placed at the beginning 
	 * of the previous frame
	 * 
	 * @return the calculated media position for the previous frame as a double, 
	 * in units of 100 nanoseconds
	 */
	private double previousFrameInternal(long id, boolean atFrameBegin) {
		long curTime = getMediaTime(id); // in units of 100 nanoseconds
		double perFrame = getTimePerFrame(id); // in seconds
		double perFrameNano = perFrame * 1000 * MS_TO_REF_TIME;
		double nextMediaPosition;
		
		if (atFrameBegin) {
			long curFrame = (long) (curTime / perFrameNano);
			if (curFrame == 0) {
				nextMediaPosition = 0;
			} else {
				nextMediaPosition = (curFrame - 1) * perFrameNano;
			}
		} else {
			nextMediaPosition = curTime - perFrameNano;
			if (nextMediaPosition < 0) {
				nextMediaPosition = 0;
			}
		}
		setMediaTime(id, (long) Math.ceil(nextMediaPosition));
		
		return nextMediaPosition;
	}
	
	//// native methods  /////
	
	private native long initPlayer(boolean synchronous);
	private native void start(long id);;
	private native void stop(long id);
	private native void pause(long id);
	private native boolean isPlaying(long id);
	private native int getState(long id); // get player state
	private native void setRate(long id, float rate);
	private native float getRate(long id);
	private native void setVolume(long id, float volume);
	private native float getVolume(long id);
	private native void setMediaTime(long id, long time);
	private native long getMediaTime(long id);
	private native long getDuration(long id);
	private native double getFrameRate(long id);
	private native double getTimePerFrame(long id);
	private native float getAspectRatio(long id);
	private native Dimension getOriginalSize(long id);
	private native void setVisualComponent(long id, Component component);
	private native void setVisible(long id, boolean visible);
	private native void setVideoSourcePos(long id, float x, float y, float w, float h);
	private native void setVideoDestinationPos(long id, int x, int y, int w, int h);
	private native void setVideoSourceAndDestPos(long id, float sx, float sy, float sw, float sh, 
			int x, int y, int w, int h);
	private native int[] getVideoDestinationPos(long id);
	
	private native long initWithFile(String mediaPath, boolean synchronous) throws JMMFException;
	/** Returns the GUID of the media subtype */
	public native String getFileType(String mediaPath);// can do without id
	private native boolean isVisualMedia(long id);
	private native void setStopTime(long id, long time) throws JMMFException;
	private native long getStopTime(long id);
	private native int getSourceHeight(long id);
	private native int getSourceWidth(long id);
	private native int[] getPreferredAspectRatio(long id);
	private native byte[] getCurrentImage(long id, DIBInfoHeader dih);
	private native byte[] getImageAtTime(long id, DIBInfoHeader dih, long time);
	private native void repaintVideo(long ids);
	/** Enables or disables debugging messages in the native player */
	public static native void enableDebugMode(boolean enable);
	public static native void correctAtPause(boolean correct);
	private native void clean(long id);
	private native void closeSession(long id);
	private native void deletePlayer(long id);
}
