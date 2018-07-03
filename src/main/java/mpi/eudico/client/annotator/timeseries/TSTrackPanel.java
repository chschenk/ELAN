package mpi.eudico.client.annotator.timeseries;

import java.awt.Insets;

import java.util.List;


/**
 * Defines a panel that can visualize multiple timeseries tracks, with vertical
 * ruler etc.
 */
public interface TSTrackPanel {
    /**
     * Adds a track to the panel.
     *
     * @param track the track to add to the panel
     */
    public void addTrack(AbstractTSTrack track);

    /**
     * Removes a track from the panel.
     *
     * @param track the track to remove from the panel
     *
     * @return true if the track was in the list of tracks and has been
     *         removed, false otherwise
     */
    public boolean removeTrack(AbstractTSTrack track);

    /**
     * Removes a track identified by trackID from the panel.
     *
     * @param trackID the name or id of the track to remove from the panel
     *
     * @return true if the track was in the list of tracks and has been
     *         removed, false otherwise
     */
    public boolean removeTrack(String trackID);

    /**
     * Returns the track identified by trackID.
     *
     * @param trackID the name or id of the track
     *
     * @return the track, or null if not present in the list
     */
    public AbstractTSTrack getTrack(String trackID);

    /**
     * Returns the list of tracks in this panel.
     *
     * @return the list of tracks
     */
    public List<AbstractTSTrack> getTracks();

    /**
     * Sets the display height for this panel.
     *
     * @param height the new height
     */
    public void setHeight(int height);

    /**
     * Returns the height of this panel.
     *
     * @return the height of this panel
     */
    public int getHeight();

    /**
     * Sets the display width of this panel.
     *
     * @param width the new width
     */
    public void setWidth(int width);

    /**
     * Returns the width of the panel.
     *
     * @return the width of the panel
     */
    public int getWidth();

    /**
     * Sets the margin of this panel.
     *
     * @param insets an Insets object containing the margin
     */
    public void setMargin(Insets insets);

    /**
     * Returns the margin in an Insets object.
     *
     * @return the margin in an Insets object
     */
    public Insets getMargin();
}
