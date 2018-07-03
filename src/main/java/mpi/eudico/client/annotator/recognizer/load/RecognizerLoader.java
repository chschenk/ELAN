package mpi.eudico.client.annotator.recognizer.load;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mpi.eudico.client.annotator.util.ClientLogger;

public class RecognizerLoader extends URLClassLoader {
	/** paths to native libraries. Only the libs for this platform are needed */
	private URL[] nativeLibs;
	private Map<String, URL> libMap;
	
	/**
	 * 
	 * @param urls the absolute paths to Java jar (or zip) files
	 * @param nativeLibs the absolute path to native libraries, can be null
	 */
	public RecognizerLoader(URL[] urls, URL[] nativeLibs) {
		super(urls);
		this.nativeLibs = nativeLibs;
		
		extractLibNames();
	}

	/**
	 * 
	 * @param urls the absolute paths to Java jar (or zip) files
	 * @param nativeLibs the absolute path to native libraries, can be null
	 * @param parent the parent classloader
	 */
	public RecognizerLoader(URL[] urls, URL[] nativeLibs, ClassLoader parent) {
		super(urls, parent);
		this.nativeLibs = nativeLibs;
		
		extractLibNames();
	}
	
	/**
	 * Extracts the native library names from the URL's. Only the libraries that conform to the 
	 * naming conventions of the current OS are stored in the library map.<br>
	 * 
	 * On Windows: names have extension .dll<br>
	 * On Mac: names start with lib and have extension .jnilib<br>
	 * On Linux/Unix: names start with lib and have extension .so 
	 */
	private void extractLibNames() {
		if (nativeLibs != null) {
			libMap = new HashMap<String, URL>(nativeLibs.length);

			URL url;
			for (int i = 0; i < nativeLibs.length; i++) {
				url = nativeLibs[i];
				if (url != null) {
					addNativeLib(url);
				}
//				String exForm = url.toExternalForm();
//				String fileName = exForm;
//				int index = exForm.lastIndexOf('/');
//				if (index > -1 && index < exForm.length() - 1) {
//					fileName = exForm.substring(index + 1);
//				}
//				// fileName should now be a file name
//				index = fileName.lastIndexOf('.');
//				int beginIndex = 0;
//				if (fileName.startsWith("lib")) {
//					beginIndex = 3;
//				}
//				if (index > -1) {
//					exForm = fileName.substring(beginIndex, index);
//				}
//				String mapped = System.mapLibraryName(exForm);
//				if (fileName.equals(mapped)) {
//					libMap.put(exForm, url);
//				}
				
			}
		}
	}
	
	/**
	 * Extracts the native library names from the URL's. Only the libraries that conform to the 
	 * naming conventions of the current OS are stored in the library map.<br>
	 * 
	 * On Windows: names have extension .dll<br>
	 * On Mac: names start with lib and have extension .jnilib<br>
	 * On Linux/Unix: names start with lib and have extension .so 
	 * 
	 * @param url the URL of the library
	 */
	private void addNativeLib(URL url) {
		// libMap is not null when we get here.
		String exForm = url.toExternalForm();
		String fileName = exForm;
		int index = exForm.lastIndexOf('/');
		if (index > -1 && index < exForm.length() - 1) {
			fileName = exForm.substring(index + 1);
		}
		// fileName should now be a file name
		index = fileName.lastIndexOf('.');
		int beginIndex = 0;
		if (fileName.startsWith("lib")) {
			beginIndex = 3;
		}
		if (index > -1) {
			exForm = fileName.substring(beginIndex, index);
		}
		String mapped = System.mapLibraryName(exForm);
		if (fileName.equals(mapped) && !libMap.containsKey(exForm)) {
			libMap.put(exForm, url);
		}
	}

	/**
	 * Return the absolute path to one of the native libraries.
	 * 
	 * @param libname the name of the library
	 */
	@Override
	protected String findLibrary(String libname) {
		if (libname == null || libMap == null) {
			return null;
		}
		//String osLibName = System.mapLibraryName(libname);// url.toExternalForm always contains forward slashes
		//System.out.println("Mapped: " + osLibName);
		URL url = libMap.get(libname);
		if (url != null) {
//			System.out.println("Path: " + url.getPath());
//			System.out.println("External: " + url.toExternalForm());
//			System.out.println("Protocol: " + url.getProtocol());
			if (url.getProtocol() != null && url.getProtocol().equals("file")) {
				return url.getPath();
			} else {
				return url.toExternalForm();
			}
		}
		
		return super.findLibrary(libname);// returns null
	}

	/**
	 * Loads the native libraries, to be called just before the first call to a recognizer.
	 */
	public void loadNativeLibs() {
		if (libMap != null) {
			Iterator<URL> mapIt = libMap.values().iterator();
			URL url;
			while (mapIt.hasNext()) {
				url = mapIt.next();
				try {
					// hier... This doesn't guarantee that the native methods are found in the loaded classes
					// java.lang.UnsatisfiedLinkError occurs?
					System.load(url.getPath());
				} catch (SecurityException se) {
					ClientLogger.LOG.warning("Cannot load library: " + url.getFile() + ". " + se.getMessage());
				} catch (UnsatisfiedLinkError ue) {
					ClientLogger.LOG.warning("Cannot load library: " + url.getFile() + ". " + ue.getMessage());
				}
			}
		}
	}
	
	/**
	 * Allows adding native libraries url's at a later stage, after the loader has been created.
	 * @param nativeLibs the URL's of native libraries
	 */
	public void addNativeLibs(URL[] nativeLibs) {
		if (nativeLibs == null) {
			return;
		}
		
		if (libMap == null) {
			libMap = new HashMap<String, URL>(nativeLibs.length);
		}
		
		for (URL url : nativeLibs) {
			if (url != null) {
				addNativeLib(url);
			}
		}
	}

	/**
	 * Adds Java library URL's.
	 * 
	 * @param urls the urls to add
	 */
	public void addLibs(URL[] urls) {
		if (urls == null) {
			return;
		}
		
		for (URL u : urls) {
			addURL(u);
		}
	}	
	

}
