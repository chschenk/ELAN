package mpi.eudico.client.annotator.md.spi;

import java.util.Map;
import java.util.ResourceBundle;


/**
 * An interface for a component for visualisation of metadata keys and values.
 * Implementers must extend a java.awt.Component that can be added to a
 * scrollpane.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public interface MDViewerComponent {
    /**
     * Sets the metadata service provider.
     *
     * @param provider the metadata service provider
     */
    public void setProvider(MDServiceProvider provider);

    /**
     * Sets the selected keys and values. May be ignored.
     *
     * @param keysAndValuesMap a map containing key-value pairs
     */
    public abstract void setSelectedKeysAndValues(Map<String, String> keysAndValuesMap);
    
    /**
     * Provides a bundle containing strings for ui elements.
     * 
     * @param bundle locale resource bundle
     */
    public void setResourceBundle(ResourceBundle bundle);
}
