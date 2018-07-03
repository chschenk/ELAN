package mpi.eudico.client.annotator.timeseries.spi;

import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;


/**
 * Defines the methods a time series ServiceProvider should implement.
 *
 * @author Han Sloetjes
 */
public interface TSServiceProvider {
    /**
     * The provider can check the source and report whether or not it can
     * handle it.
     *
     * @param filePath the location of the source
     *
     * @return true if the source is compatible with this provider
     */
    public boolean canHandle(String filePath);

    /**
     * Returns whether or not this provider can create a ui for selection and
     * configuration of tracks. If not getConfigPanel should return null and
     * autoCreateTracks should add the track(s) from the source to config.
     *
     * @return true if there is anything to configure (requires ui), false
     *         otherwise
     */
    public boolean isConfigurable();

    /**
     * A ui component enabling the user to select and configure tracks.
     *
     * @param config the config object for the source
     *
     * @return a JComponent that can be added to a dialog/frame
     */
    public TSConfigPanel getConfigPanel(TSSourceConfiguration config);

    /**
     * Creates tracks from the source without any user interaction.
     *
     * @param config the config object for the source
     */
    public void autoCreateTracks(TSSourceConfiguration config);

    /**
     * Creates track using the track information contained in the
     * configuration object.
     *
     * @param config the config object for the source
     */
    public void createTracksFromConfiguration(TSSourceConfiguration config);
}
