package mpi.eudico.client.annotator.recognizer.load;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * A class that collects information and resources concerning recognizers
 * that have been detected in the extensions folder.
 * 
 * @author Han Sloetjes
 */
public class RecognizerBundle {
	private String id;
	/** a friendly name */
	private String name;
	/** the loader for this recognizer */ //?? needed?
	private ClassLoader loader;
	private String recognizerClassName;// binary name or fully qualified class name
	private String recExecutionType; // direct, local, shared.
	
	private List<Param> paramList;
	private String helpFile;
	private URL[] javaLibs;
	private URL[] nativeLibs;
	private File baseDir;
	private String iconRef;
	
	/**
	 * No-arg constructor.
	 */
	public RecognizerBundle() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param name the name of the recognizer
	 */
	public RecognizerBundle(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ClassLoader getLoader() {
		return loader;
	}

	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}

	public List<Param> getParamList() {
		if (paramList == null) {
			return null;
		}
		//make a copy of the list
		ArrayList<Param> params = new ArrayList<Param>(paramList.size());
		for (Param p : paramList) {
			try {
			params.add((Param) p.clone());
			} catch (CloneNotSupportedException cnse) {
				
			}
		}
		
		return params;
	} 
	
	public void setParamList(List<Param> paramList) {
		this.paramList = paramList;
	}
	
	public void setHelpFile(String file) {		
		this.helpFile = file;		
	}
	
	/**
	 * Get an URL for the help file (properly quoted if it contains "weird" characters).
	 * If relative, it is resolved to the base directory.
	 */
	public String getHelpFile() {
		URL url = getURL(helpFile);
		if (url != null) {
			return url.toString();
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRecognizerClass() {
		return recognizerClassName;
	}

	public void setRecognizerClass(String recognizerClassName) {
		this.recognizerClassName = recognizerClassName;
	}

	public URL[] getJavaLibs() {
		return javaLibs;
	}

	public void setJavaLibs(URL[] javaLibs) {
		this.javaLibs = javaLibs;
	}

	public URL[] getNativeLibs() {
		return nativeLibs;
	}

	public void setNativeLibs(URL[] nativeLibs) {
		this.nativeLibs = nativeLibs;
	}

	public String getRecExecutionType() {
		return recExecutionType;
	}

	public void setRecExecutionType(String recExecutionType) {
		this.recExecutionType = recExecutionType;
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public void setIconRef(String iconRef) {
		this.iconRef = iconRef;
	}
	
	public String getIconRef() {
		return this.iconRef;
	}

	/**
	 * Get an URL for the icon (properly quoted if it contains "weird" characters).
	 * If relative, it is resolved to the base directory.
	 */
	public URL getIconURL() {
		return getURL(iconRef);
	}

	/**
	 * Takes a potentially relative URL and resolves it relative
	 * to the directory of the .cmdi file. If the given URL is
	 * absolute, it can be of a different scheme, such as http:. 
	 * 
	 * @param relative
	 * @return Resolved URL, could be file: or some other such as http:.
	 *         The URL properly quotes "weird" characters in the path.
	 */
	private URL getURL(String relative) {
		if (relative != null) {
			try {
				String path = getBaseDir().getAbsolutePath();
				// on Windows the path contains back slashes 
				// replace by forward slashes to prevent a URISyntaxException 
				path = path.replace('\\', '/');
				if (!path.endsWith("/")) {
					path = path + '/';
				}
				// to prevent a URISyntaxException. If the path does not start with '/' it is considered to be relative
				if (path.charAt(0) != '/') {
					path = '/' + path;
				}
				// This constructor actually properly quotes the path!
				//                  scheme, host, path, fragment
				URI base = new URI("file", null,  path, null);
				// Note that new URL(protocol, host, port, path) does not quote the path.
				URI icon = base.resolve(relative);
				return icon.toURL();
			} catch (URISyntaxException e) {
				ClientLogger.LOG.warning("URISyntaxException: " + e.getMessage());
			} catch (MalformedURLException e) {
				ClientLogger.LOG.warning("MalformedURLException: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				ClientLogger.LOG.warning("IllegalArgumentException: " + e.getMessage());
			}
		}
		return null;
	}
}
