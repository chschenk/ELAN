package mpi.eudico.client.annotator.md.spi;

import java.util.List;
import java.util.Map;


/**
 * An interface for a provider of metadata information that a transcription is
 * associated with.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public interface MDServiceProvider {
    /**
     * Sets the Metadata file for the provider. This method should be as
     * lightweight as possible. Preferably only a check on the file type
     * should be performed.
     *
     * @param filePath the path to the metadata
     *
     * @return true if the file can be handled (i.e. is of the right type),
     *         false otherwise
     */
    public boolean setMetadataFile(String filePath);

    /**
     * Returns the file path of the metadata file.
     *
     * @return the file path
     */
    public String getMetadataFile();

    /**
     * Returns a description of the format of the metadata.
     *
     * @return a description of the format of the metadata
     */
    public String getMDFormatDescription();

    /**
     * Returns the value for the specified key. If there are multiple values
     * for the key it is undefined which value will be returned.
     *
     * @param key the metadata key
     *
     * @return the value for the key, can be null
     */
    public String getValue(String key);

    /**
     * Returns a list of values for the specified key.
     *
     * @param key the key
     *
     * @return a list of values, can be null
     */
    public List<String> getValues(String key);

    /**
     * Returns all available metadata keys as a list.
     *
     * @return the keys
     */
    public List<String> getKeys();

    /**
     * Returns all selected metadata keys as a list.
     *
     * @return the selected keys
     */
    public List<String> getSelectedKeys();

    /**
     * Sets the selected keys.
     *
     * @param selectedKeys the new selection of metadata keys
     */
    public void setSelectedKeys(List<String> selectedKeys);

    /**
     * Returns a map of the selected keys and their values. This will be
     * converted to a table model for visualisation.
     *
     * @return the selected keys and values
     */
    public Map<String, String> getSelectedKeysAndValues();

    /**
     * A flag to specify whether the provider allows selection / configuration
     * of metadata visualization.
     *
     * @return true, if selection of metadata is supported
     */
    public boolean isConfigurable();

    /**
     * Creates and returns a panel for configuration of metadata visualisation.
     *
     * @return a panel that will be displayed in a modal dialog.
     */
    public MDConfigurationPanel getConfigurationPanel();

    /**
     * The provider can provide a custom viewer for selected metadata. If no
     * custom component is provided, the metadata will be shown in a table
     * with 2 columns, for  the keys and the values.
     *
     * @return a custom metadata viewer component, can be null
     */
    public MDViewerComponent getMDViewerComponent();

    /**
     * When {@link #setMetadataFile(String)} returns true, this method is
     * called to initialize the provider. The provider is free to defer
     * initialization.
     */
    public void initialize();
}
