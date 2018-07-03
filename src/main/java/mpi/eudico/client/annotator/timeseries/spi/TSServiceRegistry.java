package mpi.eudico.client.annotator.timeseries.spi;

import mpi.eudico.client.annotator.timeseries.csv.CSVServiceProvider;
import mpi.eudico.client.annotator.timeseries.glove.DataGloveServiceProvider;
import mpi.eudico.client.annotator.timeseries.praat.PitchTierServiceProvider;
import mpi.eudico.client.annotator.timeseries.xml.XmlTsServiceProvider;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.ExtClassLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * A Service Provider Registry for TimeSeries Service Providers.
 *
 * @author Han Sloetjes
 */
public final class TSServiceRegistry implements ClientLogger {
    private static TSServiceRegistry registry;

    /** the fully qualified class name of the time series spi */
    private final String spiClassName = "mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider";
    private TSServiceProvider prefProvider;
    private List<TSServiceProvider> providers;

    /**
     * Creates a new TSServiceRegistry instance
     */
    private TSServiceRegistry() {
        providers = new ArrayList<TSServiceProvider>();

        // register providers
        registerPropertySpi();
        registerStandardSpis();
        registerClasspathSpis2();
    }

    /**
     * Return the one instance of the registry.
     *
     * @return the registry
     */
    public static TSServiceRegistry getInstance() {
        if (registry == null) {
            registry = new TSServiceRegistry();
        }

        return registry;
    }

    /**
     * Returns the service provider of the given class name.
     *
     * @param className the fully qualified class name of the provider
     *
     * @return the service provider of the given class name, or null
     */
    public TSServiceProvider getProviderByClassName(String className) {
        if (className == null) {
            return null;
        }

        if ((prefProvider != null) &&
                prefProvider.getClass().getName().equals(className)) {
            return prefProvider;
        }

        Object prov;

        for (int i = 0; i < providers.size(); i++) {
            prov = providers.get(i);

            if ((prov != null) && prov.getClass().getName().equals(className)) {
                return (TSServiceProvider) prov;
            }
        }

        return null;
    }

    /**
     * Tries to find a registered service provider for the specified file.
     * The first provider claiming to be able to handle the file is returned.
     *
     * @param filePath the path to the file
     *
     * @return the first provider claiming to be able to handle the file
     */
    public TSServiceProvider getProviderForFile(String filePath) {
        if (filePath == null) {
            LOG.warning("No file specified for TSServiceProvider");

            return null;
        }

        // first try property spis, next standard spis and finally 
        // spis from the classpath
        if (prefProvider != null) {
            if (prefProvider.canHandle(filePath)) {
                return prefProvider;
            }
        }

        Object prov;

        for (int i = 0; i < providers.size(); i++) {
            prov = providers.get(i);

            if (prov != null) {
                if (((TSServiceProvider) prov).canHandle(filePath)) {
                    return (TSServiceProvider) prov;
                }
            }
        }

        return null;
    }

	/**
	 * A preferred Service Provider can be specified by setting the property 
	 * mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider to the fully 
	 * qualified service provider classname e.g.
	 * -Dmpi.eudico.client.annotator.timeseries.spi.TSServiceProvider=com.bar.ts.aprovider
	 *
	 */
    private void registerPropertySpi() {
        String name = System.getProperty(spiClassName);

        if (name == null) {
            return;
        }

        try {
            Class<?> c = Class.forName(name, true,
                    ClassLoader.getSystemClassLoader());
            prefProvider = (TSServiceProvider) c.newInstance();
        } catch (ClassNotFoundException cnfe) {
            LOG.warning(cnfe.getMessage());
        } catch (IllegalAccessException iae) {
            LOG.warning(iae.getMessage());
        } catch (InstantiationException ia) {
            LOG.warning(ia.getMessage());
        } catch (SecurityException se) {
            LOG.warning(se.getMessage());
        }
        
        if (prefProvider == null) {
        	// search extensions
        	try {
        		Class<?> propImpl = ExtClassLoader.getInstance().loadClass(name);
        		if (propImpl != null) {
        			try {
        			    prefProvider = (TSServiceProvider) propImpl.newInstance();
        			} catch (IllegalAccessException iae) {
        	            LOG.warning(iae.getMessage());
        	        } catch (InstantiationException ia) {
        	            LOG.warning(ia.getMessage());
        	        } catch (SecurityException se) {
        	            LOG.warning(se.getMessage());
        	        }
        		}
        	} catch (ClassNotFoundException cne) {
        		LOG.warning(cne.getMessage());
        	}
        	
        }
    }

	/**
	 * Add some predefined MPI service providers to the list.
	 */
    private void registerStandardSpis() {
        providers.add(new DataGloveServiceProvider());
        providers.add(new BasicTSServiceProvider());
        providers.add(new CSVServiceProvider());
        providers.add(new PitchTierServiceProvider());
        providers.add(new XmlTsServiceProvider());
    }

    /**
     * Discover providers by checking the provider-configuration file named
     * <b>mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider</b> in
     * the  resource directory <b>META-INF/services</b> of jars in the
     * classpath. The first line in that file will be taken as the fully
     * qualified name of a service provider class.
     */
    private void registerClasspathSpis() {
        String line = null;

        try {
            Enumeration<URL> resEn = ClassLoader.getSystemClassLoader().getResources("META-INF/services/" +
                    spiClassName);

            while (resEn.hasMoreElements()) {
                URL url = resEn.nextElement();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                            url.openStream()));

                while ((line = in.readLine()) != null) {
                    line = line.trim();

                    if ((line.length() != 0) && (line.charAt(0) != '#')) {
                        // method 1
                        Class<?> cl = ClassLoader.getSystemClassLoader().loadClass(line);

                        if (TSServiceProvider.class.isAssignableFrom(cl)) {
                            providers.add((TSServiceProvider) cl.newInstance());
                        }

                        // method 2
                        //Class c = Class.forName(line, true,
                        //			ClassLoader.getSystemClassLoader());
                        //providers.add((TSServiceProvider) c.newInstance());
                    }
                }

                in.close();
            }
        } catch (IOException ioe) {
            LOG.warning("Cannot create class: " + line);
            LOG.warning(ioe.getMessage());
        } catch (ClassNotFoundException cnfe) {
            LOG.warning("Cannot create class: " + line);
            LOG.warning(cnfe.getMessage());
        } catch (InstantiationException ie) {
            LOG.warning("Cannot create class: " + line);
            LOG.warning(ie.getMessage());
        } catch (IllegalAccessException iae) {
            LOG.warning("Cannot create class: " + line);
            LOG.warning(iae.getMessage());
        }
    }
    
    /**
     * A new implementation based on the new class loader that loads classes and resources from a 
     * specified "extension" directory. The META-INF/services is simply ignored. It tries to 
     * find any implementor of TSServiceProvider.
     */
    private void registerClasspathSpis2() { 
    	// add the external providers from the extension directory
		List<Class<?>> implementors = ExtClassLoader.getInstance().getImplementingClasses(TSServiceProvider.class);
		
		if (implementors != null && implementors.size() > 0) {
			for (Class<?> cl : implementors) {
				try {
					//if (TSServiceProvider.class.isAssignableFrom(cl)) {// check already performed by the class loader
                        providers.add((TSServiceProvider) cl.newInstance());
                    //}
				} catch (IllegalAccessException iae) {
					LOG.warning("Cannot create new instance of: " + cl);
					LOG.warning(iae.getMessage());
    	        } catch (InstantiationException ia) {
    	        	LOG.warning("Cannot create new instance of: " + cl);
    	        	LOG.warning(ia.getMessage());
    	        } catch (SecurityException se) {
    	        	LOG.warning("Cannot create new instance of: " + cl);
    	        	LOG.warning(se.getMessage());
    	        } catch (Exception exc) { // any exception
    	        	LOG.warning("Cannot create new instance of: " + cl);
    	        	LOG.warning(exc.getMessage());
				}
			}
		}
    }
}
