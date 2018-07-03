package mpi.eudico.client.annotator.timeseries.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * Default implementation of TSConfiguration.
 *
 * @author Han Sloetjes
 */
public class TSConfigurationImpl implements TSConfiguration {
    /** the properties */
    protected Properties properties;

    /** a map for configuration or track objects */
    protected Map<Object, Object> objectMap;

    /**
     * Creates a new TSConfigurationImpl instance
     */
    public TSConfigurationImpl() {
        properties = new Properties();
        objectMap = new HashMap<Object, Object>();
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#setProperty(java.lang.String,
     *      java.lang.String)
     */
    @Override
	public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#getProperty(java.lang.String)
     */
    @Override
	public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#removeProperty(java.lang.String)
     */
    @Override
	public Object removeProperty(String key) {
        return properties.remove(key);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#propertyNames()
     */
    @Override
	public Enumeration<?> propertyNames() {
        return properties.propertyNames();
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#putObject(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
	public void putObject(Object key, Object value) {
        objectMap.put(key, value);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#getObject(java.lang.Object)
     */
    @Override
	public Object getObject(Object key) {
        return objectMap.get(key);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#removeObject(java.lang.Object)
     */
    @Override
	public Object removeObject(Object key) {
        return objectMap.remove(key);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#objectKeys()
     */
    @Override
	public Set<Object> objectKeySet() {
        return objectMap.keySet();
    }
}
