package nl.mpi.jds;

import java.awt.Component;
import java.awt.Dimension;

/**
 * A player that contains methods that mostly wrap a DirectShow 
 * function, accessed via JNI.
 * All methods accepting time values expect millisecond values and pass them 
 * to the native function in "reference time", units of 100 nanoseconds.
 * All methods returning time values receive values as "reference time" values 
 * from the native functions and return them as milliseconds.s  
 *  
 * @author Han Sloetjes
 */
public class JDSPlayer {
	/** the default time format in Direct Show is Reference Time, units of 100 nanoseconds */
	private static final int MS_TO_REF_TIME = 10000;
    /** The stopped state */
    public final static int STATE_STOP = 0;

    /** The pause state */
    public final static int STATE_PAUSE = 1;

    /** The running state */
    public final static int STATE_RUN = 2;
    
	static {
		System.loadLibrary("DSPlayer");
    	String debug = System.getProperty("JDSDebug");
    	if (debug != null && debug.toLowerCase().equals("true")) {
    		JDSPlayer.enableDebugMode(true);
    	}
	}
	
	private String mediaPath;
	//private URL mediaURL;
	private long id = -1;// not inited
	private Component visualComponent;
	// initialize as true in order to be sure it will tried at least once
	private boolean stopTimeSupported = true;
	private float videoScaleFactor = 1f;
	private int vx = 0, vy = 0, vw = 0, vh = 0;
	
	public JDSPlayer(String mediaPath) throws JDSException {
		super();
		//check path
		this.mediaPath = mediaPath;
		 
		id = initWithFile(this.mediaPath);
		// 			
//		String codec = "Elecard MPEG Demultiplexer";
//		boolean b = isCodecInstalled(codec);
//		System.out.println("Codec installed: " + codec + " : " + b);
//		b = isCodecInstalled("boe");
//		System.out.println("Codec installed: boe" + " : " + b);
//		String type = getFileType(mediaPath);
//		System.out.println("File type: " + type);
	}
	
	public JDSPlayer(String mediaPath, String preferredCodec) throws JDSException {
		super();
		//check path
		this.mediaPath = mediaPath;
		 
		id = initWithFileAndCodec(this.mediaPath, preferredCodec);
	}
	
	public String[] getFiltersInGraph () {
		return getFiltersInGraph(id);
	}
	
//	public JDSPlayer(URL mediaURL) throws JDSException {
//		super();
//		this.mediaURL = mediaURL;
//	}

	public void start() {
		start(id);
	}
	
	public void stop() {
		stop(id);
	}
	
	public void pause() {
		pause(id);
	}
	
	public boolean isPlaying() {
		return isPlaying(id);
	}
	
	public void stopWhenReady() {
		stopWhenReady(id);
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
			} catch (JDSException jds) {
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
	 * Retrieves the Basic Video's average time per frame, which is in seconds,
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
		this.visualComponent = component;
		setVisualComponent(id, component);
		repositionVideoRect();
	}
	
	public Component getVisualComponent() {
		return visualComponent;
	}
	
	public void setVisualComponentPos(int x, int y, int w, int h) {
		setVisualComponentPos(id, x, y, w, h);
		repositionVideoRect();
	}
	
	public void setVisible(boolean visible) {
		setVisible(id, visible);
	}
	
	public void setVideoSourcePos(int x, int y, int w, int h) {
		setVideoSourcePos(id, x, y, w, h);
	}

	public void setVideoDestinationPos(int x, int y, int w, int h) {
		vx = x;
		vy = y;
		setVideoDestinationPos(id, x, y, w, h);
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

	public byte[] getCurrentImageData() {
		return getCurrentImage(id);
	}
	
	public byte[] getImageDataAtTime(long time) {
		return getImageAtTime(id, MS_TO_REF_TIME * time);
	}
	
	public void cleanUpOnClose() {
		clean(id);
	}
	
	public long getID() {
		return id;
	}
	
	private void repositionVideoRect() {
		if (visualComponent != null) {
			vw = (int) (visualComponent.getWidth() * videoScaleFactor);
			vh = (int) (visualComponent.getHeight() * videoScaleFactor);
			if (vx + vw < visualComponent.getWidth()) {
				vx = visualComponent.getWidth() - vw;
			}
			if (vx > 0) {
				vx = 0;
			}
			if (vy + vh < visualComponent.getHeight()) {
				vy = visualComponent.getHeight() - vh;
			}
			if (vy > 0) {
				vy = 0;
			}
			setVideoDestinationPos(id, vx, vy, vw, vh);
		}
	}
	// internal 	
	/**
	 * Calculates and sets the new media position with the highest possible accuracy.
	 * Calculations are performed using the native time units of Direct Show, 
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
		nextMediaPosition += 100;
		//System.out.println("Next Frame in Nano: " + nextMediaPosition);
		setMediaTime(id, (long) Math.ceil(nextMediaPosition));
		
		return nextMediaPosition;
	}
	
	/**
	 * Calculates and sets the new media position with the highest possible accuracy.
	 * Calculations are performed using the native time units of Direct Show, 
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
	private native void start(long id);
	private native void stop(long id);
	private native void pause(long id);
	private native boolean isPlaying(long id);
	private native void stopWhenReady(long id);
	private native int getState(long id);
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
	private native void setVisualComponentPos(long id, int x, int y, int w, int h);
	private native void setVisible(long id, boolean visible);
	private native void setVideoSourcePos(long id, int x, int y, int w, int h);
	private native void setVideoDestinationPos(long id, int x, int y, int w, int h);
	private native int[] getVideoDestinationPos(long id);
	
	private native long initWithFile(String mediaPath) throws JDSException;
	/** Use friendly name of the filter here, not the GUID */
	private native long initWithFileAndCodec(String mediaPath, String preferredCodec) throws JDSException;
	/** Use friendly name of the filter here, not the GUID */
	public native boolean isCodecInstalled(String codec);// can do without id
	public native String[] getRegisteredFilters();// can do without id
	private native String[] getFiltersInGraph(long id);
	/** Returns the GUID of the media subtype */
	public native String getFileType(String mediaPath);// can do without id
	private native boolean isVisualMedia(long id);
	private native void setStopTime(long id, long time) throws JDSException;
	private native long getStopTime(long id);
	private native int getSourceHeight(long id);
	private native int getSourceWidth(long id);
	private native int[] getPreferredAspectRatio(long id);
	private native byte[] getCurrentImage(long id);
	private native byte[] getImageAtTime(long id, long time);
	/** Enables or disables debugging messages in the native player */
	public static native void enableDebugMode(boolean enable);
	private native void clean(long id);
}
