package mpi.eudico.client.annotator.timeseries.config;

import java.util.Enumeration;
import java.util.Set;


/**
 * Interface for TimeSeries objects configuration.
 *
 * @author Han Sloetjes
 */
public interface TSConfiguration {
    /**
     * Sets a property.
     *
     * @param key a key
     * @param value the value
     */
    public void setProperty(String key, String value);

    /**
     * Returns a property.
     *
     * @param key the key
     *
     * @return the value or null
     */
    public String getProperty(String key);

    /**
     * Removes a property (key and value).
     *
     * @param key the key
     *
     * @return the removed value or null
     */
    public Object removeProperty(String key);

    /**
     * Returns an enumeration of the keys in the properties.
     *
     * @return a key enumeration
     */
    public Enumeration propertyNames();

    /**
     * Adds an object, a configuration or a track e.g., to a map of objects.
     *
     * @param key an identifier
     * @param value a timeseries object
     */
    public void putObject(Object key, Object value);

    /**
     * Returns an object for the specified key.
     *
     * @param key an identifier
     *
     * @return an object or null
     */
    public Object getObject(Object key);

    /**
     * Removes the key and value from the properties map.
     *
     * @param key an identifier
     *
     * @return the removed object or null
     */
    public Object removeObject(Object key);

    /**
     * Returns the keys contained in the object map
     *
     * @return the keys in the object map
     */
    public Set objectKeySet();
}
