package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.player.*;

/**
 * Interface that describes MediaPlayerUser methods
 */
public interface MediaPlayerUser {
    /**
     * DOCUMENT ME!
     *
     * @param player DOCUMENT ME!
     */
    public void setPlayer(ElanMediaPlayer player);

    /**
     * DOCUMENT ME!
     */
    public void startPlayer();

    /**
     * DOCUMENT ME!
     */
    public void stopPlayer();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean playerIsPlaying();

    /**
     * DOCUMENT ME!
     *
     * @param startTime DOCUMENT ME!
     * @param stopTime DOCUMENT ME!
     */
    public void playInterval(long startTime, long stopTime);

    /**
     * DOCUMENT ME!
     *
     * @param milliSeconds DOCUMENT ME!
     */
    public void setMediaTime(long milliSeconds);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getMediaTime();

    /**
     * DOCUMENT ME!
     *
     * @param rate DOCUMENT ME!
     */
    public void setRate(float rate);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public float getRate();

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
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getMediaDuration();
    
    /**
     * Notification of the fact that the offset (and therefore the duration) of the media player
     * changed.
     */
    public void mediaOffsetChanged();
}
