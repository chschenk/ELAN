//
//  JavaQTMovieView.java
//
//  Created by Albert Russel on 12/30/08.
//   Copyright 2008 MPI - Max Planck Institute for Psycholinguistics, Nijmegen
//

package player;

import java.util.List;
import java.util.ArrayList;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Frame;
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;


public class JavaQTMoviePlayer extends com.apple.eawt.CocoaComponent implements HierarchyListener, ComponentListener {
	private JavaQTMoviePlayerCreatedListener isCreatedListener;
	private long cocoaId;
	private boolean isListener;
	private List<JavaQTMoviePlayer> listeners;
	private boolean playing;
	private boolean isInitialized;
	private String filePath;
	private long initTime;
	private float initRate;
	private float initVolume;
	private long initOffset;
	private int screenHeight = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    
    private float videoScaleFactor = 1f;
	private int vx = 0, vy = 0, vw = 0, vh = 0;
    
	private List<String> drawingElementList;
	private static int drawingElementId = 0;  // drawingElementId MUST BE AN INCREASING NUMBER FOR EACH ELEMENT ADDED, DrawingView.m NEEDS THAT!!!
	
	// Constants for sending messages to the underlying QTMovieView, must be in sync with the constants in JavaQTMovieView.m
	final static int CREATE = 0;
	final static int START = 1;
	final static int STOP = 2;
	final static int FORWARD = 3;
	final static int BACKWARD = 4;
	final static int PLAY_SELECTION = 5;
	final static int SET_MEDIA_TIME = 6;
	final static int SET_RATE = 7;
	final static int SET_VOLUME = 8;
	final static int SET_SCREEN_XY = 9;
	final static int SET_DRAWING_VISIBLE = 10;
	final static int SET_DRAWING_PERIOD = 11;
	final static int ADD_DRAWING_ELEMENT = 12;
	final static int ADD_DRAWING_ELEMENT_LIST = 13;
	final static int REMOVE_DRAWING_ELEMENTS = 14;
	final static int ORGANIZE_DRAWING_ELEMENTS = 15;
	final static int SET_OFFSET = 16;
	final static int CREATE_DRAWING_VIEW = 17;
	final static int SET_MEDIA_TIME_DOUBLE = 18;
    final static int SET_VIDEO_SCALE_FACTOR = 19;
    final static int SET_VIDEO_DESTINATION_BOUNDS = 20;
	final static int CLEAN_UP = 99;
	
	// constants for drawing shapes, must be in sync with values in DrawingView.m
	final static int LINE = 0;
	final static int RECT = 1;
	final static int ELLIPSE = 2;
	final static int STRING = 3;
	
	
	
    static {
    	try {
    		// Ensure native JNI library is loaded
    		System.loadLibrary("JavaQTMovieView");
    	} catch (UnsatisfiedLinkError ule) {
    		System.out.println("Unable to create movie players: " + ule.getMessage());
    	} catch (Throwable t) {
    		System.out.println("Unable to create movie players: " + t.getMessage());
    	}
    }
	public native long createNSViewLongNative();
	// does not exist: public native long createMovie(String path);
	private native boolean isMovieCreated(long cocoaId);
	private native boolean isMovieValid(long cocoaId);
	private native boolean hasVideo(long cocoaId);
	private native boolean isPlaying(long cocoaId);
	private native double getFrameDuration(long cocoaId);
	private native long getMediaDuration(long cocoaId);
	private native long getMediaTime(long cocoaId);
	private native double getMediaTimeDouble(long cocoaId);
	private native float getRate(long cocoaId);
	private native float getVolume(long cocoaId);
	private native float getNaturalWidth(long cocoaId);
	private native float getNaturalHeight(long cocoaId);
	private native byte[] getFrameImage(long cocoaId, long time, int width, int height);
	private native boolean isFileSupported(String path);
    private native float getVideoScaleFactor(long cocoaId);
    private native int[] getVideoDestinationBounds(long cocoaId);// [x, y, w, h]
	
		
	/**
	 * Create a player for a certain file
	 *
	 * The listener gets notified when the player is fuly realized.
	 */
	public JavaQTMoviePlayer(String filePath, JavaQTMoviePlayerCreatedListener listener) throws NoCocoaPlayerException {
		this.filePath = filePath;
		if (!isFileSupported(this.filePath)) {
			throw new NoCocoaPlayerException("Cannot create a player: invalid file or file type");
		}
		isCreatedListener = listener;
		listeners = new ArrayList<JavaQTMoviePlayer>();
		drawingElementList = new ArrayList<String>();
		addHierarchyListener(this);
		addComponentListener(this);
	}
	
	/**
	 * Create a player for a certain file with an initial value for media time, player rate and audio volume.
	 *
	 * The listener gets notified when the player is fuly realized.
	 */
	public JavaQTMoviePlayer(String filePath, long time, float rate, float volume, JavaQTMoviePlayerCreatedListener listener) 
	throws NoCocoaPlayerException {
        this(filePath, listener);
		initTime = time;
		initRate = rate;
		initVolume = volume;
		initOffset = 0;
	}
	
	
	/**
	 * HierarchyListener method only for internal usage.
     * The actual native movie is only created once this Component is added
     * to a parent.
	 */
	public void hierarchyChanged(HierarchyEvent e) {//System.out.println("hierarchy changed " + cocoaId);
        //System.out.println("hierarchyChanged: isInitialized=" + isInitialized);
        //System.out.println("hierarchyChanged: PARENT_CHANGED=" + (e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED));
        //System.out.println("hierarchyChanged: DISPLAYABILITY_CHANGED=" + (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) + ": " + isDisplayable());
        //System.out.println("hierarchyChanged: isVisible():"  + isVisible());
        //System.out.println("hierarchyChanged: SHOWING_CHANGED=" + (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) + ": " + isShowing());
        //System.out.println("hierarchyChanged: isInFrameOrWindow()=" + isInFrameOrWindow(e.getChangedParent()));
		if (!isInitialized) {
			if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
				if (isInFrameOrWindow(e.getChangedParent())) {
                    //System.out.println("hierarchyChanged: *** starting to create movie ***");
					createQTMovie(filePath);
					
					//System.out.println("JavaQTMoviePlayer: waiting for the native Movie object to be created...");
					long waitTimeOut = 5000;
					long atStart = System.currentTimeMillis();
					// Wait until the movie is created.
                    // Blocking the Event Dispatching Thread is probably not a good idea though...
                    // On the other hand, ELAN can't handle it if the player is truely created
                    // asynchronously.
					try {
						while (!isMovieCreated()) {//System.out.println("wait for : " + cocoaId);
							Thread.sleep(100);
                            // test... prevent an eternal loop
                            if (System.currentTimeMillis() > atStart + waitTimeOut) {
                                // break out of the loop
                                System.out.println("JavaQTMoviePlayer: Movie object not created, waited for " + waitTimeOut + " milliseconds.");
                                // maybe show visible warning?
                                return;
                            }
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					isInitialized = true;
					//System.out.println("JavaQTMoviePlayer: the native Movie object was created.");
					
					if (initTime != 0 || initRate != 0 || initVolume != 0 || initOffset != 0) {
						setVolume(initVolume);
						setRate(initRate);
						setOffset(initOffset);
						setMediaTime(initTime);
					}
					
					if (isCreatedListener != null) {
						isCreatedListener.playerCreated(this);
					}
				}
			} 
		}
	}
	
	/**
	 * Utility method only for internal usage
	 */
	private boolean isInFrameOrWindow(Container c) {//System.out.println("check frame");
		while (c != null) {//System.out.println(c.getClass().getName());
			if (c instanceof Frame || c instanceof Window) {
				((Component) c).setVisible(true);
				((Component) c).addComponentListener(this); // needed for positioning the overlay drawing window
				return true;
			}
			c = c.getParent();
		}
		
		return false;
	}
	
	
	/**
	 * createNSViewLong must be implemented in com.apple.eawt.CocoaComponent
	 */
	public long createNSViewLong() {
		cocoaId = createNSViewLongNative();
		
		return cocoaId;
	}
	
	public boolean isMovieCreated() {
		if (cocoaId == 0) return false;
		return isMovieCreated(cocoaId);
	}
	
	public boolean isMovieValid() {
		if (cocoaId == 0) return false;
		return isMovieValid(cocoaId);
	}
	
	public boolean hasVideo() {
		if (cocoaId == 0) return false;
		
		return hasVideo(cocoaId);
	}
	
	public boolean isPlaying() {
		if (cocoaId == 0) return false;
		
		return isPlaying(cocoaId);
	}
	
	public double getFrameDuration() {
		if (cocoaId == 0) return 0;
		
		return getFrameDuration(cocoaId);
	}
	
	public long getMediaDuration() {
		if (cocoaId == 0) return 0;
		
		return getMediaDuration(cocoaId);
	}
	
	public long getMediaTime() {
		if (cocoaId == 0) return 0;
		return getMediaTime(cocoaId);
	}
	
	public double getMediaTimeDouble() {
		if (cocoaId == 0) return 0d;
		return getMediaTimeDouble(cocoaId);
	}
	
	public float getRate() {
		if (cocoaId == 0) return 0;
		return getRate(cocoaId);
	}
	
	public float getVolume() {
		if (cocoaId == 0) return 0;
		
		return getVolume(cocoaId);
	}
	
	public float getNaturalWidth() {
		if (cocoaId == 0) return 0;
		
		return getNaturalWidth(cocoaId);
	}
	
	public float getNaturalHeight() {
		if (cocoaId == 0) return 0;
		
		return getNaturalHeight(cocoaId);
	}
    
    public float getVideoScaleFactor() {
        if (cocoaId == 0) return 1;
        
        return getVideoScaleFactor(cocoaId);
    }
    
    public int[] getVideoDestinationBounds() {
        if (cocoaId == 0) return null;
        
        return getVideoDestinationBounds(cocoaId);
    }
    
	/**
	 * Add a slave player to this player
	 */
	public void addListener(JavaQTMoviePlayer player) {
		player.setListener(true);
		player.setVolume(0);
		if (!listeners.contains(player)) {
			listeners.add(player);
		}
	}		
	
	/**
	 * Remove a slave player from this player
	 */
	public void removeListener(JavaQTMoviePlayer player) {
		player.setListener(false);
		player.setVolume(getVolume(cocoaId));
		listeners.remove(player);
	}
	
	/**
	 * Remove all slave players
	 */
	public void removeAllListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			JavaQTMoviePlayer player = listeners.get(i);
			player.setListener(false);
			player.setVolume(getVolume(cocoaId));
		}
		listeners.clear();
	}
	
	/**
	 * Marks this player as a listener, is taken care of by the addListener and removeListener methods
	 */
	private void setListener(boolean isListener) {
		this.isListener = isListener;
	}
	
	public long getId() {
		return cocoaId;
	}
	
	/**
	 * Only used internaly to create the native player as soon as the view hierarchy is ready
	 */
    private void createQTMovie(String path) {
		if (cocoaId == 0) {
			return;
		}
		
		sendMessage(CREATE, path);
	}
	
	/**
	 * Start the player and all its listeners
	 */
	public void start() {
		if (isListener || cocoaId == 0) {
			return;
		}
		
		long[] players = new long[listeners.size() + 2];
		players[0] = getMediaTime(cocoaId);
		players[1] = cocoaId;
		for (int i = 0; i < listeners.size(); i++) {
			players[i + 2] = listeners.get(i).getId();
		}
		
		sendMessage(START, players);
		playing = true;
	}
	
	/**
	 * Stop the player and all its listeners
	 */
	public void stop() {
		if (isListener || cocoaId == 0) {
			return;
		}
		
		long[] players = new long[listeners.size() + 1];
		players[0] = cocoaId;
		for (int i = 0; i < listeners.size(); i++) {
			players[i + 1] = ((JavaQTMoviePlayer) listeners.get(i)).getId();
		}
		
		sendMessage(STOP, players);
		playing = false;
	}
	
	/**
	 * Let the player and all its listeners play an interval
	 */
	public void playInterval(long startTime, long stopTime) {
		if (isListener || cocoaId == 0) {
			return;
		}
		
		long[] players = new long[listeners.size() + 3];
		players[0] = startTime;
		players[1] = stopTime;
		players[2] = cocoaId;
		for (int i = 0; i < listeners.size(); i++) {
			players[i + 3] = ((JavaQTMoviePlayer) listeners.get(i)).getId();
		}
		
		sendMessage(PLAY_SELECTION, players);
	}
	
	/**
	 * Step this player and its listeners one frame forward
	 */
	public void nextFrame() {
		if (isListener || cocoaId == 0) {
			return;
		}
		
		long[] players = new long[listeners.size() + 1];
		players[0] = cocoaId;
		for (int i = 0; i < listeners.size(); i++) {
			players[i + 1] = ((JavaQTMoviePlayer) listeners.get(i)).getId();
		}
		
		sendMessage(FORWARD, players);
	}
	
	/**
	 * Step this player and its listeners one frame backward
	 */
	public void previousFrame() {
		if (isListener || cocoaId == 0) {
			return;
		}
		
		long[] players = new long[listeners.size() + 1];
		players[0] = cocoaId;
		for (int i = 0; i < listeners.size(); i++) {
			players[i + 1] = ((JavaQTMoviePlayer) listeners.get(i)).getId();
		}
		
		sendMessage(BACKWARD, players);
	}
	
	/**
	 * Set the media time for this player and its listeners
	 */
	public void setMediaTime(long time) {	
		//System.out.println("set media time 1: " + time + " id: " + cocoaId);
		if (isListener || cocoaId == 0) return;
		
		long[] players = new long[listeners.size() + 2];	
		players[0] = time;
		players[1] = cocoaId;
		for (int i = 0; i < listeners.size(); i++) {
			players[i + 2] = ((JavaQTMoviePlayer) listeners.get(i)).getId();
		}
		
		if (isInitialized) {
			if (isListener) {
				return;
			}		
			sendMessage(SET_MEDIA_TIME, players);
		} else {
			initTime = time;
		}
	}
	
	/**
	 * Set the media time for this player and its listeners, as precise as possible
	 * @param time the time value in milliseconds
	 */
	public void setMediaTimeDouble(double time) {	
		//System.out.println("set media time 1: " + time + " id: " + cocoaId);
		if (isListener || cocoaId == 0) return;
		
		long[] players = new long[listeners.size() + 2];

		players[0] = (long) Math.ceil(time * 1000);// (long) (time * 1000);
		players[1] = cocoaId;
		for (int i = 0; i < listeners.size(); i++) {
			players[i + 2] = ((JavaQTMoviePlayer) listeners.get(i)).getId();
		}
		
		if (isInitialized) {
			if (isListener) {
				return;
			}		
			sendMessage(SET_MEDIA_TIME_DOUBLE, players);
		} else {
			initTime = (long) time;
		}
	}
	
	/**
	 * Set the player rate for this player and its listeners
	 */
	public void setRate(float rate) {	
		if (isListener || cocoaId == 0) {
			return;
		}
		
		long[] players = new long[listeners.size() + 2];
		// must use a long because otherwise cocoaId gets messed up, is converted to a float on the other side
		players[0] = (long) (100000 / rate); 
		players[1] = cocoaId;
		for (int i = 0; i < listeners.size(); i++) {
			players[i + 2] = ((JavaQTMoviePlayer) listeners.get(i)).getId();
		}
		
		if (isInitialized) {
			if (isListener) {
				return;
			}			
			sendMessage(SET_RATE, players);
		} else {
			initRate = rate;
		}
	}
	
	/**
	 * Set the volume for this player (listeners have volume 0)
	 */
	public void setVolume(float volume) {
		if (cocoaId == 0) {
			return;
		}
		
		if (isInitialized) {
			float[] parameters = new float[1];
			parameters[0] = volume;
			sendMessage(SET_VOLUME, parameters);
		} else {
			initVolume = volume;
		}
	}
	
	/**
	 * Sets the time offset for this player. Is used to synchronize multiple players, 
	 * the offset is added to all media time values in e.g. start(), setMediaTime() etc.
	 */
	public void setOffset(long offset) {
		if (cocoaId == 0) {
			return;
		}
		
		if (isInitialized) {
			//System.out.println("J setOffset: " + offset + " id: " + cocoaId);
			long[] parameters = new long[1];
			parameters[0] = offset;
			sendMessage(SET_OFFSET, parameters);			
		} else {
			initOffset = offset;
		}
	}
    
    /**
     * Sets the scale factor for the video.
     * @param scaleFactor the scale factor for the video
     */
    public void setVideoScaleFactor(float scaleFactor) {
        if (cocoaId == 0) {
			return;
		}
        
        if (isInitialized) {//??
			//System.out.println("J setVideoScaleFactor: " + scaleFactor + " id: " + cocoaId);
			float[] parameters = new float[1];
			parameters[0] = scaleFactor;
            this.videoScaleFactor = scaleFactor;
			sendMessage(SET_VIDEO_SCALE_FACTOR, parameters);
            // recalculation of location and size handled by the native player code
            // repositionVideoRect();
		}
    }
    
    public void setVideoDestinationBounds(int x, int y, int w, int h) {
        if (cocoaId == 0) {
			return;
		}
        
        if (isInitialized) {//??
            int[] bounds = new int[4];
            bounds[0] = x;
            bounds[1] = y;
            bounds[2] = w;
            bounds[3] = h;
            vx = x;
            vy = y;
            vw = w;
            vh = h;
            sendMessage(SET_VIDEO_DESTINATION_BOUNDS, bounds);
        }
    }
    
    /**
     * Recalculates the location and the size of the video frame.
     * Now implemented in the native code.
     */
    /*
    private void repositionVideoRect() {
        if (isInitialized) {//??
			vw = (int) (getWidth() * videoScaleFactor);
			vh = (int) (getHeight() * videoScaleFactor);
			if (vx + vw < getWidth()) {
				vx = getWidth() - vw;
			}
			if (vx > 0) {
				vx = 0;
			}
			if (vy + vh < getHeight()) {
				vy = getHeight() - vh;
			}
			if (vy > 0) {
				vy = 0;
			}
			setVideoDestinationBounds(vx, vy, vw, vh);
		}
    }
     */
	
	/**
	 * Gives a PNG image with the requested width and height of the frame at the requested time.
	 * ONLY WORKS ON A STOPPED PLAYER!
	 */
	public byte[] getFrame(long time, int width, int height) {
		if (cocoaId == 0 || playing) return null;
		if (isPlaying(cocoaId)) return null;
		
		return getFrameImage(cocoaId, time, width, height);
	}
	
	/**
	 * Clean up the mess in the native world
	 */
	public void cleanUpOnClose() {
		if (cocoaId == 0) {
			return;
		}
		
        isCreatedListener = null;   // no more callbacks

		cocoaId = 0; // prevents java calls calling the native world
		sendMessage(CLEAN_UP, null);
	}
	
	
	
	
	
	//
	// drawing code 
	//
	/**
	 * Explicit call to create a drawing view. Creating a drawing view is no longer done automatically
	 * when the QTMovie is created. This led to crashes on some systems and it is unnecessary overhead
	 * when not used.
	 * Should, ideally throw an exception if creation fails.
	 */
	public void createDrawingView() {
		sendMessage(CREATE_DRAWING_VIEW, null);
	}
	
	/**
	 * Turn the visibility of the drawing elemnts on or off
	 */
	public void setDrawingVisible(boolean isVisible) {
		boolean[] parameters = new boolean[1];
		parameters[0] = isVisible;
		sendMessage(SET_DRAWING_VISIBLE, parameters);
	}
	
	/**
	 * Only draw elements for every period frame, so if period = 4 only the elements for every fourth frame is drawn
	 */
	public void setDrawingPeriod(long period) {
		long[] parameters = new long[1];
		parameters[0] = period;
		sendMessage(SET_DRAWING_PERIOD, parameters);
	}
	
	/**
	 * Remove a specific element
	 */
	public void removeElement(int id) {
		String elementString = "ID " + id;
		sendMessage(REMOVE_DRAWING_ELEMENTS, elementString);
	}
	
	/**
	 * Remove all elements
	 */
	public void removeAllElements() {
		String elementString = "ALL";
		sendMessage(REMOVE_DRAWING_ELEMENTS, elementString);
	}
	
	/**
	 * Remove all elements that are visible between beginTime and endTime
	 *
	 * If the element is only partialy visible in the time interval it is still completely removed!
	 */
	public void removeAllElementsBetween(long beginTime, long endTime) {
		String elementString = "INTERVAL " + beginTime + " " + endTime;
		sendMessage(REMOVE_DRAWING_ELEMENTS, elementString);
	}
	
	/**
	 * Add a Line to the to the native drawing elements
	 */
	public long addLine(long beginTime, long endTime, float x, float y, float x2, float y2, 
						int red, int green, int blue, float alpha, float lineWidth) {
		if (cocoaId == 0) return -1;
		
		String elementString = makeLineString(beginTime, endTime, x, y, x2, y2, red, green, blue, alpha, lineWidth);
		if (elementString != null) {
			int id = drawingElementId++;
			elementString = id + " " + elementString;
			sendMessage(ADD_DRAWING_ELEMENT, elementString);
			return id;
		} else {
			return -1;
		}
	}
	
	/**
	 * Add a Rectangle to the native drawing elements
	 */
	public long addRectangle(long beginTime, long endTime, float x, float y, float width, float height, 
							 int red, int green, int blue, float alpha, float lineWidth, boolean filled) {
		if (cocoaId == 0) return -1;
		
		String elementString = makeRectangleString(beginTime, endTime, x, y, width, height, red, green, blue, alpha, lineWidth, filled);
		if (elementString != null) {
			int id = drawingElementId++;
			elementString = id + " " + elementString;
			sendMessage(ADD_DRAWING_ELEMENT, elementString);
			return id;
		} else {
			return -1;
		}
	}
	
	/**
	 * Add anEllipse to the native drawing elements
	 */
	public long addEllipse(long beginTime, long endTime, float cx, float cy, float rx, float ry, 
						   int red, int green, int blue, float alpha, float lineWidth, boolean filled) {
		if (cocoaId == 0) return -1;
		
		String elementString = makeEllipseString(beginTime, endTime, cx, cy, rx, ry, red, green, blue, alpha, lineWidth, filled);
		if (elementString != null) {
			int id = drawingElementId++;
			elementString = id + " " + elementString;
			sendMessage(ADD_DRAWING_ELEMENT, elementString);
			return id;
		} else {
			return -1;
		}
	}
	
	/**
	 * Add a String to the native drawing elements
	 */
	public long addString(String text, long beginTime, long endTime, float x, float y, 
						  int red, int green, int blue, float alpha, String fontName, int fontSize) {
		if (cocoaId == 0) return -1;
		
		String elementString = makeStringString(text, beginTime, endTime, x, y, red, green, blue, alpha, fontName, fontSize);
		if (elementString != null) {
			int id = drawingElementId++;
			elementString = id + " " + elementString;
			sendMessage(ADD_DRAWING_ELEMENT, elementString);
			return id;
		} else {
			return -1;
		}
	}
	
	
	// LOCAL DRAWING ELEMENT LIST CAN BE FASTER IF MANY ELEMENTS MUST BE ADDED
	
	/**
	 * Clear the local list of drawing elements
	 */
	public void clearDrawingElementList() {
		drawingElementList.clear();
	}
	
	/**
	 * Send the local list of drawing elements to the native drawing world
	 */
	public void sendDrawingElementList() {
		Object[] elements = drawingElementList.toArray(); // String[]
		sendMessage(ADD_DRAWING_ELEMENT_LIST, elements);
	}
	
	/**
	 * Add a Line to the local list of drawing elements
	 * 
	 * Do not forget to call sendDrawingElementList() to send the local list to the native drawing code
	 */
	public long addLineToDrawingElementList(long beginTime, long endTime, float x, float y, float x2, float y2, 
						int red, int green, int blue, float alpha, float lineWidth) {
		if (cocoaId == 0) return -1;
		
		String elementString = makeLineString(beginTime, endTime, x, y, x2, y2, red, green, blue, alpha, lineWidth);
		if (elementString != null) {
			int id = drawingElementId++;
			elementString = id + " " + elementString;
			drawingElementList.add(elementString);
			return id;
		} else {
			return -1;
		}
	}
	
	/**
	 * Add a Rectangle to the local list of drawing elements
	 *
	 * Do not forget to call sendDrawingElementList() to send the local list to the native drawing code
	 */
	public long addRectangleToDrawingElementList(long beginTime, long endTime, float x, float y, float width, float height, 
												 int red, int green, int blue, float alpha, float lineWidth, boolean filled) {
		if (cocoaId == 0) return -1;
		
		String elementString = makeRectangleString(beginTime, endTime, x, y, width, height, red, green, blue, alpha, lineWidth, filled);
		if (elementString != null) {
			int id = drawingElementId++;
			elementString = id + " " + elementString;
			drawingElementList.add(elementString);
			return id;
		} else {
			return -1;
		}
	}
	
	
	/**
	 * Add anEllipse to the local list of drawing elements
	 *
	 * Do not forget to call sendDrawingElementList() to send the local list to the native drawing code
	 */
	public long addEllipseToDrawingElementList(long beginTime, long endTime, float cx, float cy, float rx, float ry, 
											   int red, int green, int blue, float alpha, float lineWidth, boolean filled) {
		if (cocoaId == 0) return -1;
		
		String elementString = makeEllipseString(beginTime, endTime, cx, cy, rx, ry, red, green, blue, alpha, lineWidth, filled);
		if (elementString != null) {
			int id = drawingElementId++;
			elementString = id + " " + elementString;
			drawingElementList.add(elementString);
			return id;
		} else {
			return -1;
		}
	}
	
	
	/**
	 * Add a String to the local list of drawing elements
	 *
	 * Do not forget to call sendDrawingElementList() to send the local list to the native drawing code
	 */
	public long addStringToDrawingElementList(String text, long beginTime, long endTime, float x, float y, 
											  int red, int green, int blue, float alpha, String fontName, int fontSize) {
		if (cocoaId == 0) return -1;
		
		String elementString = makeStringString(text, beginTime, endTime, x, y, red, green, blue, alpha, fontName, fontSize);
		if (elementString != null) {
			int id = drawingElementId++;
			elementString = id + " " + elementString;
			drawingElementList.add(elementString);
			return id;
		} else {
			return -1;
		}
	}
	
	// END OF LOCAL DRAWING ELEMENT LIST METHODS
	
	
	
	
	/**
	 * Construct a String that encodes a Line for the native drawing code
	 */
	private String makeLineString(long beginTime, long endTime, float x, float y, float x2, float y2, 
							 int red, int green, int blue, float alpha, float lineWidth) {
		
		String elementString = "" + LINE;
		
		if (beginTime >= 0) elementString += " " + beginTime; else return null; 
		if (endTime >= 0) elementString += " " + endTime; else return null;
		if (x >= 0 && x <= 1) elementString += " " + x; else return null; 
		if (y >= 0 && y <= 1) elementString += " " + y; else return null;
		if (x2 >= 0 && x2 <= 1) elementString += " " + x2; else return null; 
		if (y2 >= 0 && y2 <= 1) elementString += " " + y2; else return null; 
		if (red >= 0 && red <= 255) elementString += " " + red; else return null; 
		if (green >= 0 && green <= 255) elementString += " " + green; else return null; 
		if (blue >= 0 && blue <= 255) elementString += " " + blue; else return null; 
		if (alpha >= 0 && alpha <= 1) elementString += " " + alpha; else return null; 
		if (lineWidth > 0) elementString += " " + lineWidth;
		
		return elementString;
	}
	
	/**
	 * Construct a String that encodes a Rectangle for the native drawing code
	 */
	private String makeRectangleString(long beginTime, long endTime, float x, float y, float width, float height, 
							 int red, int green, int blue, float alpha, float lineWidth, boolean filled) {
		
		String elementString = "" + RECT;
		
		if (beginTime >= 0) elementString += " " + beginTime; else return null; 
		if (endTime >= 0) elementString += " " + endTime; else return null;
		if (x >= 0 && x <= 1) elementString += " " + x; else return null; 
		if (y >= 0 && y <= 1) elementString += " " + y; else return null;
		if (width >= 0 && width <= 1) elementString += " " + width; else return null; 
		if (height >= 0 && height <= 1) elementString += " " + height; else return null; 
		if (red >= 0 && red <= 255) elementString += " " + red; else return null; 
		if (green >= 0 && green <= 255) elementString += " " + green; else return null; 
		if (blue >= 0 && blue <= 255) elementString += " " + blue; else return null; 
		if (alpha >= 0 && alpha <= 1) elementString += " " + alpha; else return null; 
		if (lineWidth > 0) elementString += " " + lineWidth;
		if (filled) elementString += " 1"; else elementString += " 0"; // all values must be a number that is interpretable as a float
		
		return elementString;
	}
	
	
	/**
	 * Construct a String that encodes an Ellipse for the native drawing code
	 */
	private String makeEllipseString(long beginTime, long endTime, float cx, float cy, float rx, float ry, 
						   int red, int green, int blue, float alpha, float lineWidth, boolean filled) {
		
		String elementString = "" + ELLIPSE;
		
		if (beginTime >= 0) elementString += " " + beginTime; else return null; 
		if (endTime >= 0) elementString += " " + endTime; else return null;
		if (cx >= 0 && cx <= 1) elementString += " " + cx; else return null; 
		if (cy >= 0 && cy <= 1) elementString += " " + cy; else return null;
		if (rx >= 0 && rx <= 1) elementString += " " + rx; else return null; 
		if (ry >= 0 && ry <= 1) elementString += " " + ry; else return null; 
		if (red >= 0 && red <= 255) elementString += " " + red; else return null; 
		if (green >= 0 && green <= 255) elementString += " " + green; else return null; 
		if (blue >= 0 && blue <= 255) elementString += " " + blue; else return null; 
		if (alpha >= 0 && alpha <= 1) elementString += " " + alpha; else return null; 
		if (lineWidth > 0) elementString += " " + lineWidth;
		if (filled) elementString += " 1"; else elementString += " 0"; // all values must be a number that is interpretable as a float
		
		return elementString;
	}
	
		
	/**
	 * Construct a String that encodes a String for the native drawing code
	 */
	private String makeStringString(String text, long beginTime, long endTime, float x, float y, 
						  int red, int green, int blue, float alpha, String fontName, int fontSize) {
		String delimiter = "AxQzyQRcD";
		String elementString = STRING + " " + delimiter + " " + delimiter + text; // must be exactly like this!
		
		if (beginTime >= 0) elementString += delimiter + beginTime; else return null; 
		if (endTime >= 0) elementString += delimiter + endTime; else return null;
		if (x >= 0 && x <= 1) elementString += delimiter + x; else return null; 
		if (y >= 0 && y <= 1) elementString += delimiter + y; else return null;
		if (red >= 0 && red <= 255) elementString += delimiter + red; else return null; 
		if (green >= 0 && green <= 255) elementString += delimiter + green; else return null; 
		if (blue >= 0 && blue <= 255) elementString += delimiter + blue; else return null; 
		if (alpha >= 0 && alpha <= 1) elementString += delimiter + alpha; else return null;
		if (fontName != null) elementString += delimiter + fontName; else elementString += delimiter + "Monaco";
		if (fontSize > 0) elementString += delimiter + fontSize; else return null;
		
		return elementString;
	}
	
	
	/**
	 * Not used now but could be used if internal datastructures like segment a tree must be build for efficiency reasons
	 * First tests suggest that one needs not to worry about performance problems when there are less than a million drawing elements.
	 */
	public void organizeDrawingElements() {
		sendMessage(ORGANIZE_DRAWING_ELEMENTS, null);
	}
	
	//
	// end drawing code
	//
	
	
	
	
	// ComponentListener methods, they take care of giving all native components the right size.
		// the cocoa world needs this info because it is not available there, i.e. the methods there give always (0, 0)
	public void componentMoved(ComponentEvent e) {
		if (cocoaId == 0) return;
		
		if (isShowing()) {
            // when drawing elements are used, the video scale factor has to be taken into account as well
			setFrameXY((int)getLocationOnScreen().getX(), screenHeight - getHeight() - (int)getLocationOnScreen().getY());
		}
	}
	
	public void componentResized(ComponentEvent e) {
		if (cocoaId == 0) return;
		if (isShowing()) {
            // when drawing elements are used, the video scale factor has to be taken into account as well
			setFrameXY((int)getLocationOnScreen().getX(), screenHeight - getHeight() - (int)getLocationOnScreen().getY());
            // this is done by the code of the native player
//            if (videoScaleFactor > 1) {
//                repositionVideoRect();
//            }
		}
	}
	
	public void componentShown(ComponentEvent e) {
		
	}
	
	public void componentHidden(ComponentEvent e) {
		
	}
	
	private void setFrameXY(int x, int y) {
		long[] parameters = new long[2];
		parameters[0] = x;
		parameters[1] = y;
		sendMessage(SET_SCREEN_XY, parameters);
	}
	
	
	
	
	
	
	
	/*
	 *  Default implementation of com.apple.eawt.CocoaComponent abstract methods
	 */
	final static Dimension MIN_SIZE		= new Dimension(200, 200);
	final static Dimension PREF_SIZE	= new Dimension(400, 400);
	final static Dimension MAX_SIZE		= new Dimension(600, 600);
	
	public Dimension getPreferredSize() {
		return PREF_SIZE;
	}
	
	public Dimension getMinimumSize () {
		return MIN_SIZE;
	}
	
	public Dimension getMaximumSize() {
		return MAX_SIZE;
	}
	
	// Deprecated; just cast the correct createNSViewLong implementation
	public int createNSView() {
		return (int)createNSViewLong();
	}
	
	
	
}
