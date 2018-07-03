package mpi.eudico.client.annotator.player;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;


/**
 * Describes a Player that can refer to and select from multiple  source files.<br>
 * First (and maybe only) application is in synchronisation of non audio/video
 * sources (e.g. time series data) with an empty media player. The SyncManager
 * can use this type to determine whether or not a selection  combobox should
 * be added to the player in synchronisation mode.
 *
 * @author Han Sloetjes, MPI
 */
public interface MultiSourcePlayer {
    /**
     * Returns the a String representation of the url / id of the currently
     * selected source.
     *
     * @return the id of the current source
     */
    public String getCurrentSource();

    /**
     * Sets the currently selected source by its id as a String.
     *
     * @param currentSource the id of the source that should be selected
     */
    public void setCurrentSource(String currentSource);

    /**
     * Returns the current sources (their id's) as an array of Strings.
     *
     * @return an array of id's
     */
    public String[] getDescriptorStrings();

    /**
     * Returns the current sources (their id's) as an array of
     * MediaDescriptors.
     *
     * @return an array of descriptors
     */
    public MediaDescriptor[] getDescriptors();

    /**
     * Sets the array of sources handled by this player.
     *
     * @param descriptors the array of sources
     */
    public void setDescriptors(MediaDescriptor[] descriptors);
}
