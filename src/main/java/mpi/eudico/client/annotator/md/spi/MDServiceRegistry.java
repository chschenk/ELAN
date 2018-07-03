package mpi.eudico.client.annotator.md.spi;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import mpi.eudico.client.annotator.md.cmdi.CMDIServiceProvider;
import mpi.eudico.client.annotator.md.imdi.ImdiFileServiceProvider;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.ExtClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A service registry for Metadata providers. Singleton. Has a slightly
 * different approach to class loading. The found implementing  providers are
 * stored in a List as Class objects (instead of Provider objects).
 * Instantiation is delayed till a request for a provider is received.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class MDServiceRegistry {
    private static MDServiceRegistry registry;

    /** the fully qualified class name of the metadata spi */
    private final String spiClassName = "mpi.eudico.client.annotator.md.spi.MDServiceProvider";
    private Class<?> prefProviderClass;
    private List<Class<?>> providers;

    /**
     * Creates a new MDServiceRegistry instance
     */
    private MDServiceRegistry() {
        providers = new ArrayList<Class<?>>();

        // register providers
        registerPropertySpi();
        registerStandardSpis();
        registerClasspathSpis();
    }

    /**
     * Returns the single instance of the registry.
     *
     * @return the single instance of the registry
     */
    public static MDServiceRegistry getInstance() {
        if (registry == null) {
            registry = new MDServiceRegistry();
        }

        return registry;
    }

    /**
     * Returns a provider instance of the specified class.
     *
     * @param className the name of the class
     *
     * @return the instantiated provider, or null
     */
    public MDServiceProvider getProviderByClassName(String className) {
        if (className == null) {
            return null;
        }

        if ((prefProviderClass != null) &&
                prefProviderClass.getName().equals(className)) {
            // create instance
            return createProviderInstance(prefProviderClass);
        }

        for (int i = 0; i < providers.size(); i++) {
        	Class<?> provClass = providers.get(i);

            if (provClass.getName().equals(className)) {
                return createProviderInstance(provClass);
            }
        }

        return null;
    }

    /**
     * Finds a provider for the specified metadata file. First the preferred
     * provider (a system property) is tried, if this one cannot handle the
     * file  the other providers are checked. The first one that can handle
     * the file is returned.
     *
     * @param filePath the file path
     *
     * @return an MDServiceProvider capable of handling the file or null
     */
    public MDServiceProvider getProviderForMDFile(String filePath) {
        if (filePath == null) {
            LOG.warning("No file specified for TSServiceProvider");

            return null;
        }
        File f = new File(filePath);
        
        if (!f.exists()) {
        	ClientLogger.LOG.warning("The metadata file does not exist: " + filePath);
        	return null;
        }
        if (!f.canRead()) {
        	ClientLogger.LOG.warning("The metadata file cannot be read: " + filePath);
        	return null;
        }
        if (f.length() == 0) {
        	ClientLogger.LOG.warning("The metadata file has zero length: " + filePath);
        	return null;
        }
        
        MDServiceProvider provider = null;

        // first try the preferred, property provider class
        if (prefProviderClass != null) {
            provider = createProviderInstance(prefProviderClass);

            if (provider != null) {
                if (provider.setMetadataFile(filePath)) {
                	provider.initialize();
                    return provider;
                }
            }
        }

        // try the others

        for (int i = 0; i < providers.size(); i++) {
        	Class<?> cl = providers.get(i);
            provider = createProviderInstance(cl);

            if ((provider != null) && provider.setMetadataFile(filePath)) {
            	provider.initialize();
                return provider;
            }
        }

        return null;
    }

    /**
     * A preferred Service Provider can be specified by setting the property
     * mpi.eudico.client.annotator.md.spi.MDServiceProvider to the fully
     * qualified service provider classname e.g.
     * -Dmpi.eudico.client.annotator.md.spi.MDServiceProvider=com.bar.md.aprovider
     */
    private void registerPropertySpi() {
        String name = System.getProperty(spiClassName);

        if (name == null) {
            return;
        }

        try {
            Class<?> c = Class.forName(name, true,
                    ClassLoader.getSystemClassLoader());
            prefProviderClass = c;
        } catch (ClassNotFoundException cnfe) {
            LOG.warning(cnfe.getMessage());
        } catch (SecurityException se) {
            LOG.warning(se.getMessage());
        }

        if (prefProviderClass == null) {
            // search extensions
            try {
                Class<?> propImpl = ExtClassLoader.getInstance().loadClass(name);

                if (propImpl != null) {
                    try {
                        prefProviderClass = propImpl;
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
        providers.add(ImdiFileServiceProvider.class);
        providers.add(CMDIServiceProvider.class);
    }

    /**
     * A new implementation based on the new class loader that loads classes
     * and resources from a  specified "extension" directory. The
     * META-INF/services is simply ignored. It tries to  find any implementor
     * of MDServiceProvider.
     */
    private void registerClasspathSpis() {
        // add the external providers from the extension directory
        List<Class<?>> implementors = ExtClassLoader.getInstance()
                                             .getImplementingClasses(MDServiceProvider.class);

        if (implementors != null && implementors.size() > 0) {
            for (Class<?> cl : implementors) {
                if (!providers.contains(cl)) {
                    providers.add(cl);
                }
            }
        }
    }

    /**
     * Creates a new instance of the specified class. Exceptions are handles
     * internally, null will be returned if an exception occurs.
     *
     * @param cl the class to instantiate
     *
     * @return the created MDServiceProvider, or null
     */
    private MDServiceProvider createProviderInstance(Class<?> cl) {
        if (cl == null) {
            LOG.warning("Cannot create a new instance: the class is null");

            return null;
        }

        try {
            return (MDServiceProvider) cl.newInstance();
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

        return null;
    }
}
