package mpi.eudico.util;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;

/**
 * A classloader for loading classes and resources from jars in a specific
 * "extensions" directory.
 *
 * @author Han Sloetjes
 * @version 1.1 include fixes by Martin Schickbichler
 */
public class ExtClassLoader extends URLClassLoader {
    /** the default extensions directory */
    public static final String EXTENSIONS_DIR = System.getProperty("user.dir") +
        File.separator + "extensions";
    private static String extFolder = EXTENSIONS_DIR;
    private static Logger LOG = Logger.getLogger(ExtClassLoader.class.getName());
    private static ExtClassLoader loader;

    //private static URLClassLoader urlLoader;
    // store classes per Jar? or make just one list
    private static Map<String, List<Class<?>>> loadedClasses;
    private static Map<String, List<URL>> resourceURLS;

    /**
     * Creates a new ExtClassLoader instance
     *
     * @param parent the parent class loader
     */
    private ExtClassLoader(ClassLoader parent) {
        super(new URL[0], parent);

        loadedClasses = new HashMap<String, List<Class<?>>>();
        resourceURLS = new HashMap<String, List<URL>>();

        File plDir = new File(extFolder);

        if (plDir.exists() && plDir.isDirectory()) {
            File[] plfs = plDir.listFiles();

            try {
                for (File plf : plfs) {
                    super.addURL(plf.toURI().toURL());

                    //System.out.println("adding URL: "+plfs[i].toURL());
                }
            } catch (MalformedURLException e) {
            }
        }

        loadClasses();
    }

    /**
     * Creates and returns the single instance of this class.
     *
     * @return the single instance of this class
     */
    public static ExtClassLoader getInstance() {
        if (loader == null) {
            loader = new ExtClassLoader(ClassLoader.getSystemClassLoader());
        }

        return loader;
    }

    /**
     * Sets the path to the folder to use to load classes from jar files.
     * Returns silently if the parameter is null or equal to the current path.
     *
     * @param directoryPath the path to the extensions folder
     */
    public static void setExtensionsDirectory(String directoryPath) {
        if ((directoryPath != null) && !directoryPath.equals(extFolder)) {
            File folder = new File(directoryPath);

            if (folder.exists() && folder.isDirectory()) {
                extFolder = directoryPath;
                loader = null; // wait for a call to getInstance to load classes
            } else {
                LOG.warning("The specified folder does not exist: " +
                    directoryPath);
            }
        } else {
            LOG.warning("The folder path is null or equal to current path");
        }
    }

    /**
     * Scans the extensions directory for .jar files and loads all classes
     * found in the jar.  May store url's for all other resources, but for now
     * finding resources is delegated to a URLClassLoader.
     */
    private void loadClasses() {
        try {
            File plDir = new File(extFolder);
            LOG.info("Extensions dir: " + plDir.getAbsolutePath());

            if (plDir.exists() && plDir.isDirectory()) {
                //List jarURLS = new ArrayList(6);
                JarFile jf;
                String jarUrlPref;

                File[] plfs = plDir.listFiles();

                for (File plf : plfs) {
                	// Check if it is a file
                	if (!plf.isFile()) {
                		continue;
                	}
                    // check if it is a jar??
                    //URL url = new URL("jar:file:/" + plf.getAbsolutePath() + "!/");
                    try {
                        jf = new JarFile(plf.getAbsolutePath());
                        jarUrlPref = "jar:file:/" +
                            plf.getAbsolutePath().replace('\\', '/') +
                            "!/";

                        /*
                           try {
                               jarURLS.add(new URL("jar:file:/" +
                                       plf.getAbsolutePath() + "!/"));
                           } catch (MalformedURLException mue) {
                               LOG.warning(mue.getMessage());
                           }
                         */

                        //System.out.println("JF: " + jf.getName());
                    } catch (JarException je) {
                    	// If it is a zip file but not a good jar, I think.
                        LOG.warning("Error loading jar '"+ plf.getAbsolutePath() + "': " + je.getMessage());
                        continue;
                    } catch (ZipException ze) {
                    	// If it isn't a zip file, which is not really unexpected if we
                    	// don't check the file extension.
                        continue;
                    } catch (Throwable ioe) {
                        LOG.warning("Error loading jar '"+ plf.getAbsolutePath() + "': " + ioe.getMessage());
                        continue;
                    }

                    Enumeration<JarEntry> clEnum = jf.entries();

                    ArrayList<Class<?>> foundClasses = new ArrayList<Class<?>>();
                    ArrayList<URL> foundRes = new ArrayList<URL>();

                    while (clEnum.hasMoreElements()) {
                    	JarEntry jae = clEnum.nextElement();

                        //System.out.println("JE: " + jae.getName() + " size: " + jae.getSize() + " com size: " + jae.getCompressedSize());
                        if (jae.getName().endsWith(".class") ||
                                jae.getName().endsWith(".CLASS")) {
//                            try {
//                                InputStream jis = jf.getInputStream(jae);
//                                byte[] cbs = new byte[(int) jae.getSize()];
//                                jis.read(cbs, 0, cbs.length);

                                try {
                                    /*
                                       Class nextClass = defineClass(jae.getName()
                                                                        .replace('/',
                                                   '.').substring(0,
                                                   jae.getName().lastIndexOf('.')),
                                               cbs, 0, cbs.length);
                                     */
                                    Class<?> nextClass = super.loadClass(jae.getName()
                                                                         .replace('/',
                                                '.')
                                                                         .substring(0,
                                                jae.getName().lastIndexOf('.')));

                                    //System.out.println("Class: " + nextClass);
                                    if (nextClass != null) {
                                        foundClasses.add(nextClass);
                                    }
                                } catch (Throwable exception) {
                                	// Usual cases: IndexOutOfBoundsException,
                                	// SecurityException, NoClassDefFoundError.
                                    LOG.warning("Cannot create class from " +
                                    		plf.getAbsolutePath() + " member " +
                                    		jae.getName() +
                                    		": " +
                                    		exception.toString());
                                }

                                // a ClassFormatError can be thrown. Should normally not be caught
                        } else {
                            // store a url
                            try {
                                // jar entry getName returns a path with '/' characters (not '.')
                                foundRes.add(new URL(jarUrlPref +
                                        jae.getName()));
                            } catch (MalformedURLException mue) {
                                LOG.warning("Could not create url for: " +
                                    jae.getName());
                            }
                        }
                    }

                    loadedClasses.put(jf.getName().replace('\\', '/'),
                        foundClasses);
                    resourceURLS.put(jf.getName().replace('\\', '/'), foundRes);
                }

                /*
                   if (jarURLS.size() > 0) {
                       urlLoader = new URLClassLoader((URL[]) jarURLS.toArray(
                                   new URL[] {  }), this);
                   }
                 */
            }
        } catch (Exception ex) {
            LOG.warning("Could not load extension classes: " + ex.getMessage());
        }

        //findResource("a.b.c");
    }

    /**
     * Searches the loaded classes for implementors (or extenders) of the class
     * with the given name. Returns null if the given class cannot be found.
     *
     * @version March 2016 changed return type to List<Class>
     * @param name the fully qualified name of the class
     *
     * @return a list of Class objects
     */
    public List<Class<?>> getImplementingClasses(String name) {
        if (name == null) {
            return null;
        }

        Class<?> superClass = null;

        try {
            superClass = Class.forName(name);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();

            return null;
        }

        return getImplementingClasses(superClass);
    }

    /**
     * Searches the loaded classes for implementors (or extenders) of the class
     * with the given name. Returns null if the given class is null.
     *
     * @version March 2016 changed return type to List<Class>
     * 
     * @param superClass the Class to find implementors/subclasses of
     *
     * @return an array of Class objects of subclasses of the specified class
     */
    public List<Class<?>> getImplementingClasses(Class<?> superClass) {
        if (superClass == null) {
            return null;
        }

        List<Class<?>> cList = new ArrayList<Class<?>>();
        Iterator<String> setIt = loadedClasses.keySet().iterator();

        while (setIt.hasNext()) {
            String name = setIt.next();
            List<Class<?>> li = loadedClasses.get(name);

            if (li == null) {
                continue;
            }

            for (Class<?> cl : li) {

                if (superClass.isAssignableFrom(cl)) {
                    cList.add(cl);
                }
            }
        }

        return cList;
    }

    /**
     * Searches the loaded classes for implementors (or extenders) of the class
     * of the given type. Returns null if the given class is null.
     *
     * @version March 2016
     * 
     * @param superClass the typed Class to find implementors/subclasses of
     *
     * @return an array of Class objects of subclasses of the specified class
     */
    public <T> List<Class<? extends T>> getImplementingTypedClasses(Class<T> superClass) {
        if (superClass == null) {
            return null;
        }

        List<Class<? extends T>> cList = new ArrayList<Class<? extends T>>();
        Iterator<String> setIt = loadedClasses.keySet().iterator();

        while (setIt.hasNext()) {
            String name = setIt.next();
            List<Class<?>> li = loadedClasses.get(name);

            if (li == null) {
                continue;
            }

            for (Class<?> cl : li) {

                if (superClass.isAssignableFrom(cl)) {
                    cList.add(cl.asSubclass(superClass));
                }
            }
        }

        return cList;
    }
    
    /**
     * Creates one new instance of the specified class 
     * 
     * @param implClass the class to instantiate
     * @return on object of the super type
     */
    public <T> T createInstance(Class<? extends T> implClass) {
    	try {
			return implClass.newInstance();
		} catch (InstantiationException ia) {
            LOG.warning("Cannot create new instance of: " + implClass);
            LOG.warning(ia.getClass().getName() + " - " + ia.getMessage());
        } catch (IllegalAccessException iae) {
            LOG.warning("Cannot create new instance of: " + implClass);
            LOG.warning(iae.getClass().getName() + " - " + iae.getMessage());
        } catch (SecurityException se) {
            LOG.warning("Cannot create new instance of: " + implClass);
            LOG.warning(se.getClass().getName() + " - " + se.getMessage());
        } catch (Throwable exc) { // throwable
            LOG.warning("Cannot create new instance of: " + implClass);
            LOG.warning(exc.getClass().getName() + " - " + exc.getMessage());
        }
    	
    	return null;
    }
    
    /**
     * Creates a typed list of instances for the list of implementing classes.
     *  
     * @param interfaceClass the typed class of the interface
     * @param implementingClasses the list of untyped classes that are assumed to implement the interface 
     * @return a list of instances of the implementing classes
     */
    public <T> List<T> getInstanceList(Class<T> interfaceClass, List<Class<?>> implementingClasses) {
    	if (interfaceClass == null) {
    		LOG.warning("Cannot create instances, interface Class is null");
    		return null;
    	}
    	if (implementingClasses == null || implementingClasses.isEmpty()) {
    		LOG.warning("Cannot create instances, the list of implementing classes is null or empty");
    		return null;
    	}
    	
    	List<T> instanceList = new ArrayList<T>(implementingClasses.size());
    	
    	for (Class<?> cl : implementingClasses) {
    		try {
    			int modifiers = cl.getModifiers();
    			if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers)) {
    				instanceList.add(interfaceClass.cast(cl.newInstance()));
    			} else {
    				if (LOG.isLoggable(Level.INFO)) {
    					if (Modifier.isInterface(modifiers)) {
    						LOG.info("Cannot instantiate interface: " + cl.getName());
    					} else if (Modifier.isAbstract(modifiers)) {
    						LOG.info("Cannot instantiate abstract class: " + cl.getName());
    					}
    				}
    			}
			} catch (InstantiationException ia) {
	            LOG.warning("Cannot create new instance of: " + cl);
	            LOG.warning(ia.getClass().getName() + " - " + ia.getMessage());
	        } catch (IllegalAccessException iae) {
	            LOG.warning("Cannot create new instance of: " + cl);
	            LOG.warning(iae.getClass().getName() + " - " + iae.getMessage());
	        } catch (SecurityException se) {
	            LOG.warning("Cannot create new instance of: " + cl);
	            LOG.warning(se.getClass().getName() + " - " + se.getMessage());
	        } catch (Throwable exc) { // throwable
	            LOG.warning("Cannot create new instance of: " + cl);
	            LOG.warning(exc.getClass().getName() + " - " + exc.getMessage());
	        }
    	}
    	
    	return instanceList;
    }
    
    /**
     * Creates a typed list of instances for all detected implementing classes in the extensions folder.
     * This is a shorthand for {@link #getImplementingClasses(Class)} followed by 
     * {@link #getInstanceList(Class, List)}
     *  
     * @param interfaceClass the typed class of the interface
     *  
     * @return a list of instances of the implementing classes
     */
    public <T> List<T> getInstanceList(Class<T> interfaceClass) {
    	if (interfaceClass == null) {
    		LOG.warning("Cannot create instances, interface Class is null");
    		return null;
    	}
    	List<Class<?>> implClassList = getImplementingClasses(interfaceClass);
    	
    	if (implClassList == null || implClassList.isEmpty()) {
    		LOG.warning("Cannot create instances, the list of implementing classes is null or empty");
    		return null;
    	}
    	
    	return getInstanceList(interfaceClass, implClassList);
    }
    
    /**
     * Finds the class.
     *
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    @Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name == null) {
            throw new ClassNotFoundException("No class found for null");
        }

        return super.findClass(name);

        /*
           Class cl;
           List li;
           Object key;
           Iterator setIt = loadedClasses.keySet().iterator();
           while (setIt.hasNext()) {
               key = setIt.next();
               li = (List) loadedClasses.get(key);
               if (li == null) {
                   continue;
               }
               for (int i = 0; i < li.size(); i++) {
                   cl = (Class) li.get(i);
                   if (cl.getName().equals(name)) {
                       return cl;
                   }
               }
           }
           throw new ClassNotFoundException("No class found for name: " + name);
         */
    }

    /**
     * Finds the stored resource url. If not found returns null; the parent
     * classloader  and bootstrap classloader have  already been tried before
     * this method is called.
     *
     * @see java.lang.ClassLoader#findResource(java.lang.String)
     */
    @Override
	public URL findResource(String name) {
        if (name == null) {
            return null;
        }

        if (!name.startsWith("/")) {
            name = name.replace('.', '/');
        } else {
            name = name.substring(1);
        }

        Iterator<String> setIt = resourceURLS.keySet().iterator();

        while (setIt.hasNext()) {
        	String key = setIt.next();
        	List<URL> li = resourceURLS.get(key);

            if (li == null) {
                continue;
            }

            for (URL url : li) {

                if (url != null) {
                    String res = url.toString();

                    int index = res.indexOf("!/");

                    if ((index > -1) && (index < (res.length() - 2))) {
                        res = res.substring(index + 2);

                        if (name.equals(res)) {
                            return url;
                        }
                    }
                }
            }
        }

        return null;

        /*
           if (urlLoader != null) {
               return urlLoader.findResource(name);
           }
         */
    }
}
