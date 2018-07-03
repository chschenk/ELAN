package mpi.eudico.client.annotator.recognizer.api;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.recognizer.data.NumParam;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.load.RecognizerBundle;
import mpi.eudico.client.annotator.recognizer.load.RecognizerLoader;
import mpi.eudico.client.annotator.recognizer.load.RecognizerParser;
import mpi.eudico.client.annotator.recognizer.silence.SilenceRecognizer;
import mpi.eudico.client.annotator.util.AvailabilityDetector;
import mpi.eudico.client.annotator.util.ClientLogger;

import org.xml.sax.SAXException;

/**
 * Class which creates a template map for all 
 * the bundles created for the available audio
 * and video recognizer
 * 
 * @version Sep 2012
 * @author updated by aarsom
 *
 */
public class RecogAvailabilityDetector {
	/** a template map for audio recognizers */
	private static Map<String, RecognizerBundle> audioRecognizerBundles = new HashMap<String, RecognizerBundle>(6);
	/** a template map for video recognizers */
	private static Map<String, RecognizerBundle> videoRecognizerBundles = new HashMap<String, RecognizerBundle>(6);
	/** a template map for other (audio nor video) recognizers */
	private static Map<String, RecognizerBundle> otherRecognizerBundles = new HashMap<String, RecognizerBundle>(6);
	private static RecognizerLoader recognizerLoader;	
	
	/**
	 * Private constructor
	 */
	private RecogAvailabilityDetector() {
		super();
	}

	private static void addFromBundle(Map<String, Recognizer>map,
			Map<String, RecognizerBundle> bundles, int type) {
		for (String key : bundles.keySet()) {
			RecognizerBundle bundle = bundles.get(key);
			
			if (bundle.getRecExecutionType().equals("local")) {
				LocalRecognizer localRecognizer = new LocalRecognizer(bundle.getRecognizerClass());
				localRecognizer.setParamList(bundle.getParamList());
				localRecognizer.setName(bundle.getName());
				localRecognizer.setId(bundle.getId());
				localRecognizer.setRecognizerType(type);
				localRecognizer.setBaseDir(bundle.getBaseDir());
				map.put(key, localRecognizer);
			} else if (bundle.getRecExecutionType().equals("shared")) {
				SharedRecognizer sharedRecognizer = new SharedRecognizer(bundle.getRecognizerClass());
				sharedRecognizer.setParamList(bundle.getParamList());// returns a copy of the list
				sharedRecognizer.setName(bundle.getName());
				sharedRecognizer.setId(bundle.getId());
				sharedRecognizer.setRecognizerType(type);
				sharedRecognizer.setBaseDir(bundle.getBaseDir());
				map.put(key, sharedRecognizer);
			} else if (bundle.getJavaLibs() != null) {// assume "direct" ?
				
				if (recognizerLoader == null) {
					recognizerLoader = new RecognizerLoader(bundle.getJavaLibs(), bundle.getNativeLibs());
				} else {
					recognizerLoader.addLibs(bundle.getJavaLibs());
					recognizerLoader.addNativeLibs(bundle.getNativeLibs());
				}
				//RecognizerLoader loader = new RecognizerLoader(bundle.getJavaLibs(), bundle.getNativeLibs());
				
				try {
					//loader.loadNativeLibs();
					//rec = (Recognizer) Class.forName(bundle.getRecognizerClass(), true, loader).newInstance();
					Recognizer rec = (Recognizer) Class.forName(bundle.getRecognizerClass(), true, recognizerLoader).newInstance();
					rec.setName(bundle.getName());
					map.put(key, rec);
				} catch (ClassNotFoundException cnfe) {
					ClientLogger.LOG.severe("Cannot load the recognizer class: " + bundle.getRecognizerClass() + " - Class not found");
				} catch (InstantiationException ie) {
					ClientLogger.LOG.severe("Cannot instantiate the recognizer class: " + bundle.getRecognizerClass());
				} catch (IllegalAccessException iae) {
					ClientLogger.LOG.severe("Cannot access the recognizer class: " + bundle.getRecognizerClass());
				} catch (Exception ex) {// any other exception
					ClientLogger.LOG.severe("Cannot load the recognizer: " + bundle.getRecognizerClass() + " - " + ex.getMessage());
				}
			} else {
				ClientLogger.LOG.severe("Cannot load the recognizer: no Java library has been found: " + bundle.getName());
			}
		}
	}

	/**	
	 * Return the list of available recognizers:
	 * first the audio recognizers, then video, then others.	 
	 * Maybe add parameters for needed capabilities like file formats.
	 * 
	 * @return a Map with the available recognizers
	 */
	public static Map<String, Recognizer> getAudioRecognizers() {
		AvailabilityDetector.loadFilesFromExtensionsFolder();
		
		Map<String, Recognizer> audioRecs = new HashMap<String, Recognizer>(6);
		SilenceRecognizer sr = new SilenceRecognizer();
		audioRecs.put(sr.getName(), sr);
//		Recognizer demoRec = new DemoRecognizer();
//		audioRecs.put(demoRec.getName(), demoRec);
//		Recognizer testRec = new TestRecognizer();
//		audioRecs.put(testRec.getName(), testRec);
		
		addFromBundle(audioRecs, audioRecognizerBundles, Recognizer.AUDIO_TYPE);

		return audioRecs;
	}
	
	/**	
	 * Return the list of available recognizers:
	 * first the audio recognizers, then video, then others.	 
	 * Maybe add parameters for needed capabilities like file formats.
	 * 
	 * @return a Map with the available recognizers
	 */
	public static Map<String, Recognizer> getVideoRecognizers() {
		AvailabilityDetector.loadFilesFromExtensionsFolder();
		
		Map<String, Recognizer> videoRecs = new HashMap<String, Recognizer>(5);
		
		addFromBundle(videoRecs, videoRecognizerBundles, Recognizer.VIDEO_TYPE);

		return videoRecs;
	}
	
	/**	
	 * Return the list of available recognizers:
	 * first the audio recognizers, then video, then others.	 
	 * Maybe add parameters for needed capabilities like file formats.
	 * 
	 * @return a Map with the available recognizers
	 */
	public static Map<String, Recognizer> getOtherRecognizers() {
		AvailabilityDetector.loadFilesFromExtensionsFolder();
		Map<String, Recognizer> otherRecs = new HashMap<String, Recognizer>(3);
		addFromBundle(otherRecs, otherRecognizerBundles, Recognizer.OTHER_TYPE);
		
		return otherRecs;
	}
	
	/**
	 * Returns the parameter list of a recognizer.
	 * 
	 * @param recognizerName the name of the recognizer
	 * 
	 * @return a List containing the parameters
	 */
	public static List<Param> getParamList(String recognizerName) {
		if (recognizerName != null) {
			RecognizerBundle bundle = getBundle(recognizerName);
			List<Param> params = null;
			if (bundle != null) {
				params = bundle.getParamList();
			}
			
			if (params != null) {
				List<Param> copyList = new ArrayList<Param>(params.size());
				for (Param p : params) {
					if (p == null) {
						continue;
					}
					try {
						copyList.add((Param) p.clone());
					} catch (CloneNotSupportedException cnse) {
						ClientLogger.LOG.warning("Cannot clone a parameter: " + p.id);
					}
				}
				
				return copyList;
			}
		}
		
		return null;
	}

	private static RecognizerBundle getBundle(String name) {
		RecognizerBundle result = null;
		if (result == null && audioRecognizerBundles != null) {
			result = audioRecognizerBundles.get(name);
		}
		if (result == null && videoRecognizerBundles != null) {
			result = videoRecognizerBundles.get(name);
		}
		if (result == null && otherRecognizerBundles != null) {
			result = otherRecognizerBundles.get(name);
		}
		return result;
	}
	
	/**
	 * Returns the help file of a recognizer.
	 * 
	 * @param recognizerName the name of the recognizer
	 * 
	 * @return a string specifying the filename, can be null
	 */
	public static String getHelpFile(String recognizerName){
		if (recognizerName != null) {
			RecognizerBundle bundle = getBundle(recognizerName);
			String helpFile = null;
			if (bundle != null) {
				helpFile = bundle.getHelpFile();
			}
			// special case for the Silence Recognizer, temporary solution
			// either create a bundle for the recognizer or distribute the recognizer as an extension
			if (helpFile == null && recognizerName.equals(SilenceRecognizer.NAME)) {
				return "/mpi/eudico/client/annotator/resources/silence_recognizer.html";
			}
			return helpFile;
		}		
		return null;
	}
	
	/**
	 * Returns the icon URL of a recognizer.
	 * 
	 * @param recognizerName the name of the recognizer
	 * 
	 * @return a string specifying the filename, can be null
	 */
	public static URL getIconURL(String recognizerName){
		if (recognizerName != null) {
			RecognizerBundle bundle = getBundle(recognizerName);
			URL icon = null;
			if (bundle != null) {
				icon = bundle.getIconURL();
			}
			// Special case for built-in recognizers, such as the Silence Recognizer, which don't
			// have a bundle. Use this icon to show the built-in property.
			// Either create a bundle for the recognizer or distribute the recognizer as an extension
			if (bundle == null) {
				return RecogAvailabilityDetector.class.getResource("/mpi/eudico/client/annotator/resources/ELAN16.png");
			}
			return icon;
		}		
		return null;
	}
	
	/**
	 * Creates a parser, a classloader, loads the class of the recognizer, creates a bundle 
	 * and adds the bundle to the proper recognizer map(s).
	 * 
	 * @param mdStream the stream representing the "recognizer.cmdi" from a directory or from a jar
	 * @param libs the recognizer's Java libraries
	 * @param natLibs the recognizer's native libraries
	 * @param baseDir the directory the recognizer runs from
	 */
	public static void createBundle(InputStream mdStream, URL[] libs, URL[] natLibs, File baseDir) {
		boolean isDetector = false;
		String binaryName = null;
		RecognizerBundle bundle = null;
		RecognizerParser parser = null;
		
		try {
			parser = new RecognizerParser(mdStream);
			parser.parse();
			if (parser.getRecognizerType() == null || 
					(!parser.getRecognizerType().equals("direct") &&
					 !parser.getRecognizerType().equals("local") &&
					 !parser.getRecognizerType().equals("shared"))) {
				ClientLogger.LOG.warning("Unsupported recognizer type, should be 'direct', 'local' or 'shared': " + parser.getRecognizerType());
				return;
			}
			if (!parser.isCurOsSupported()) {
				ClientLogger.LOG.warning("Recognizer does not support this Operating System: " + parser.getRecognizerName());
				return;
			}
			if (parser.getImplementor() != null) {
				isDetector = true;
				binaryName = parser.getImplementor();
			} else {
				ClientLogger.LOG.warning("The implementing class name has not been specified.");
				return;
			}
			
		} catch (SAXException sax) {
			ClientLogger.LOG.severe("Cannot parse metadata file: " + sax.getMessage());
		}
		
		if (isDetector) {
			boolean audio = false;
			boolean video = false;
			
			List<Param> paramList = parser.getParamList();
			
			if (paramList != null) {
				validateParamList(paramList);
				
				for (Param par : paramList) {
					if (par instanceof FileParam) {
						if (((FileParam) par).ioType == FileParam.IN) {
							if (((FileParam) par).contentType == FileParam.AUDIO) {
								audio = true;
							} else if (((FileParam) par).contentType == FileParam.VIDEO) {
								video = true;
							}
						}
					}
				}
			}// else exception?
			
			if (parser.getRecognizerType().equals("direct")) {
				if (libs == null) {
					return;
				}
				// create a classloader, bundle			
				RecognizerLoader loader = new RecognizerLoader(libs, natLibs);
	
				if (binaryName != null) {
					try {
						Class<?> c = loader.loadClass(binaryName);
						
						// if the above works, assume everything is all right
						bundle = new RecognizerBundle();
						bundle.setRecognizerClass(binaryName);
						bundle.setJavaLibs(libs);
						bundle.setNativeLibs(natLibs);
					} catch (ClassNotFoundException cne) {
						ClientLogger.LOG.severe("Cannot load the recognizer class: " + binaryName + " - Class not found");
					} 
				}// else throw exception?
				else {
					ClientLogger.LOG.warning("Cannot load the recognizer class: Class not found");
				}
			} else if (parser.getRecognizerType().equals("local") ||
					   parser.getRecognizerType().equals("shared")) {
				
				bundle = new RecognizerBundle();
				bundle.setRecognizerClass(binaryName);// reuse the class name for the run command
			}
			// Common initialization
			if (bundle != null) {
				bundle.setId(parser.getRecognizerName());
				bundle.setName(parser.getDescription());// friendly name
				bundle.setParamList(paramList);				
				bundle.setRecExecutionType(parser.getRecognizerType());
				bundle.setBaseDir(baseDir);
				bundle.setHelpFile(parser.getHelpFile());
				bundle.setIconRef(parser.getIconRef());
				if (video) { // may also have audio inputs
					videoRecognizerBundles.put(bundle.getName(), bundle);
				} else if (audio) {
					audioRecognizerBundles.put(bundle.getName(), bundle);
				} else {	// has only inputs that are audio nor video.
					otherRecognizerBundles.put(bundle.getName(), bundle);
				}
			}
		}
	}
	
	/**
	 * Validates the parameters in the list
	 * 
	 * @param paramList, list of parameters to validate
	 */
	private static void validateParamList(List<Param> paramList){	
		int n = 0;
		Param par;
		while(n < paramList.size()){
			par = paramList.get(n);
			if(par.info == null){
				paramList.remove(par);
				continue;
			}
			
			if(par instanceof NumParam){
				if(!(((NumParam)par).min < ((NumParam)par).max)){
					paramList.remove(par);
					continue;
				}
			} else	if (par instanceof FileParam) {
				if (((FileParam) par).contentType < 0) {
					paramList.remove(par);
					continue;
				}
			}			
			n++;
		}
	}
}

