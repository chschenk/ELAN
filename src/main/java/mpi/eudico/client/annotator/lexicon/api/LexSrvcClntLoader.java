package mpi.eudico.client.annotator.lexicon.api;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class LexSrvcClntLoader extends URLClassLoader {
	/** paths to native libraries. Only the libs for this platform are needed */
	private URL[] nativeLibs;
	private Map<String, URL> libMap;

	public LexSrvcClntLoader(URL[] urls, URL[] nativeLibs) {
		super(urls);
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
