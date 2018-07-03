package mpi.eudico.client.annotator.player;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.PreferencesListener;
import mpi.eudico.client.mediacontrol.Controller;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * Interface for ELAN media players.
 */
public interface ElanMediaPlayer extends Controller, ElanLocaleListener, PreferencesListener {
	public MediaDescriptor getMediaDescriptor();
    /**
     * DOCUMENT ME!
     */
    @Override
	public void start();

    /**
     * DOCUMENT ME!
     */
    @Override
	public void stop();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isPlaying();

    /**
     * DOCUMENT ME!
     *
     * @param startTime DOCUMENT ME!
     * @param stopTime DOCUMENT ME!
     */
    public void playInterval(long startTime, long stopTime);

    @Override
	public void setStopTime(long stopTime);
    
    /**
     * Sets the new point zero or starting point of the media, in milliseconds
     *
     * @param offset the new point 0
     */
    public void setOffset(long offset);

    /**
     * Returns the offset that has been determined after synchronisation with other media; 
     * the new virtual point zero.
     *
     * @return the value of the new starting point, in milliseconds
     */
    public long getOffset();

    public void nextFrame();
    public void previousFrame();
    
    /**
     * Sets the flag that determines whether frame forward/backward jumps with 
     * the number of ms. of the frame duration or jumps to the beginning of
     * the next or previous frame.
     * 
     * @param stepsToFrameBegin if true frame forward/backward jumps to begin of 
     * next or previous frame, otherwise it jumps with a fixed number of ms., 
     * the same as the frame duration
     */
    public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin);
    
    /**
     * DOCUMENT ME!
     *
     * @param time DOCUMENT ME!
     */
    @Override
	public void setMediaTime(long time);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getMediaTime();

    /**
     * Sets the playback rate.
     *
     * @param rate the playback rate
     */
    @Override
	public void setRate(float rate);

    /**
     * Returns the current playback rate.
     *
     * @return the current playback rate
     */
    public float getRate();
    
    /**
     * Returns whether or not the framework has successfully detected the encoded framerate and thus 
     * the duration per frame.
     * 
     * @return true if the framerate has been detected, false otherwise
     */
    public boolean isFrameRateAutoDetected();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getMediaDuration();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public float getVolume();

    /**
     * DOCUMENT ME!
     *
     * @param level DOCUMENT ME!
     */
    public void setVolume(float level);
    
    /**
     * get/setSubVolume() remember the subvolume as selected by the user
     * through the appropriate slider. The VolumeManager will use the
     * remembered value to set an effective volume.
     * @param level
     */
    public void setSubVolume(float level);
    public float getSubVolume(); 
    public void setMute(boolean mute);
    public boolean getMute();

    /**
     * DOCUMENT ME!
     *
     * @param layoutManager DOCUMENT ME!
     */
    public void setLayoutManager(ElanLayoutManager layoutManager);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public java.awt.Component getVisualComponent();
    
    /**
     * Returns the width in pixels of the media, if it has a visual component.
     * 
     * @return the (video) image width, as encoded in the media file, or as interpreted by the 
     * framework
     */
    public int getSourceWidth();
    
    /**
     * Returns the height in pixels of the media, if it has a visual component.
     * 
     * @return the (video) image height, as encoded in the media file, or as interpreted by the 
     * framework
     */
    public int getSourceHeight();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public float getAspectRatio();
    
    /**
     * Enforces an aspect ratio for the media component.
     * 
     * @param aspectRatio the new aspect ratio
     */
    public void setAspectRatio(float aspectRatio);

    /**
     * Returns the frame or sample duration as encoded in the media file (if possible)
     * HS April 2014 the value is now returned as a double
     * 
     * @return the sample duration in milliseconds
     */
    public double getMilliSecondsPerSample();

    /**
     * Sets the frame duration in case the framework has not detected the sample duration. 
     * This value determines the number of ms the playhead moves forward or backward when 
     * the frame forward/backward command has been issued.
     *
     * @param milliSeconds the new frame duration
     */
    public void setMilliSecondsPerSample(long milliSeconds); // temporary, player should do this 

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateLocale();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFrameworkDescription();

    /**
     * DOCUMENT ME!
     *
     * @param controller DOCUMENT ME!
     */
    public void addController(Controller controller);

    /**
     * DOCUMENT ME!
     *
     * @param controller DOCUMENT ME!
     */
    public void removeController(Controller controller);

    /**
     * DOCUMENT ME!
     */
    public void startControllers();

    /**
     * DOCUMENT ME!
     */
    public void stopControllers();

    /**
     * DOCUMENT ME!
     *
     * @param time DOCUMENT ME!
     */
    public void setControllersMediaTime(long time);

    /**
     * DOCUMENT ME!
     *
     * @param rate DOCUMENT ME!
     */
    public void setControllersRate(float rate);
    
    /**
     * Opportunity to dispose of objects, close streams etc. to ensure proper garbage collection.
     */
    public void cleanUpOnClose();
}
