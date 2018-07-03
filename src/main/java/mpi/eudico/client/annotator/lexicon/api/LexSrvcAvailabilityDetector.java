package mpi.eudico.client.annotator.lexicon.api;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.util.AvailabilityDetector;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientFactory;
import org.xml.sax.SAXException;

/**
 * This class creates a template map for the bundles created for the lexicon
 * based cmdi files 
 *  
 * @author Micha Hulsbosch
 * @updated by Aarthy Somasundaram
 * @version Sep 2012
 *
 */
public class LexSrvcAvailabilityDetector {
	private static Map<String, LexSrvcClntBundle> lexSrvcClntBundles = new HashMap<String, LexSrvcClntBundle>();
	private static LexSrvcClntLoader lexSrvcClntLoader;

	
	/**
	 * Private constructor
	 */
	private LexSrvcAvailabilityDetector() {
		super();
	}
	
	/**
	 * Creates and returns the Lexicon Service Client Factories that are found in the designated extensions
	 * directory.
	 * @return a HashMap containing the Lexicon Service Client Factories identified by their type (name).
	 */
	public static HashMap<String, LexiconServiceClientFactory> getLexiconServiceClientFactories() {
		
		AvailabilityDetector.loadFilesFromExtensionsFolder();
		
		HashMap<String, LexiconServiceClientFactory> lexSrvcClnts = new HashMap<String, LexiconServiceClientFactory>(6);
		
		Iterator<String> keyIt = lexSrvcClntBundles.keySet().iterator();
		String key;
		LexSrvcClntBundle bundle;
		LexiconServiceClientFactory clientFactory;
		while (keyIt.hasNext()) {
			key = keyIt.next();
			bundle = lexSrvcClntBundles.get(key);
			
			if (bundle.getJavaLibs() != null) {
				
				if (lexSrvcClntLoader == null) {
					lexSrvcClntLoader = new LexSrvcClntLoader(bundle.getJavaLibs(), bundle.getNativeLibs());
				} else {
					lexSrvcClntLoader.addLibs(bundle.getJavaLibs());
					lexSrvcClntLoader.addNativeLibs(bundle.getNativeLibs());
				}
				
				try {
					clientFactory = (LexiconServiceClientFactory) Class.forName(bundle.getLexSrvcClntClass(), true, lexSrvcClntLoader).newInstance();
					clientFactory.setType(bundle.getName());
					clientFactory.setDescription(bundle.getDescription());
					for(Param p : bundle.getParamList()) {
						if(p.getType().equals("defaultUrl")) {
							clientFactory.setDefaultUrl(p.getContent());
						} else if(p.getType().equals("searchConstraint")) {
							clientFactory.addSearchConstraint(p.getContent());
						}
					}
					lexSrvcClnts.put(key, clientFactory);
				} catch (ClassNotFoundException cnfe) {
					ClientLogger.LOG.severe("Cannot load the lexicon service client class: " + bundle.getLexSrvcClntClass() + " - Class not found");
				} catch (InstantiationException ie) {
					ClientLogger.LOG.severe("Cannot instantiate the lexicon service client class: " + bundle.getLexSrvcClntClass());
				} catch (IllegalAccessException iae) {
					ClientLogger.LOG.severe("Cannot access the lexicon service client class: " + bundle.getLexSrvcClntClass());
				} catch (Exception ex) {// any other exception
					ClientLogger.LOG.severe("Cannot load the lexicon service client: " + bundle.getLexSrvcClntClass() + " - " + ex.getMessage());
					ex.printStackTrace();
				}
			} else {
				ClientLogger.LOG.severe("Cannot load the lexicon service client: no Java library has been found ");
			}
		}
		
		return lexSrvcClnts;
		
	}
	
	/**
	 *  Returns the number of detected service client factories.
	 *  
	 * @return the number of detected service client factories
	 */
	public static int getNumberOfFactories() {
		return lexSrvcClntBundles.size();
	}
	
	/**
	 * Returns a list of names of detected lexicon service client factories.
	 * 
	 * @return a list of names of detected lexicon service client factories
	 */
	public static List<String> getFactoryNames() {
		if (lexSrvcClntBundles.size() > 0) {
			ArrayList<String> names = new ArrayList<String>(lexSrvcClntBundles.size());
			names.addAll(lexSrvcClntBundles.keySet());
			return names;
		}
		
		return null;
	}
	
	/**
	 * Creates and returns a service factory of a given name.
	 * 
	 * @param name the name of the factory
	 * @return a new instance of the factory or null
	 */
	public static LexiconServiceClientFactory getFactoryByName(String name) {

		LexSrvcClntBundle bundle = lexSrvcClntBundles.get(name);
		if (bundle == null) {
			return null;
		}
		
		if (bundle.getJavaLibs() != null) {
			
			if (lexSrvcClntLoader == null) {
				lexSrvcClntLoader = new LexSrvcClntLoader(bundle.getJavaLibs(), bundle.getNativeLibs());
			} else {
				lexSrvcClntLoader.addLibs(bundle.getJavaLibs());
				lexSrvcClntLoader.addNativeLibs(bundle.getNativeLibs());
			}
			
			try {
				LexiconServiceClientFactory clientFactory = (LexiconServiceClientFactory) Class.forName(bundle.getLexSrvcClntClass(), 
						true, lexSrvcClntLoader).newInstance();
				clientFactory.setType(bundle.getName());
				clientFactory.setDescription(bundle.getDescription());
				for(Param p : bundle.getParamList()) {
					if(p.getType().equals("defaultUrl")) {
						clientFactory.setDefaultUrl(p.getContent());
					} else if(p.getType().equals("searchConstraint")) {
						clientFactory.addSearchConstraint(p.getContent());
					}
				}
				return clientFactory;
			} catch (ClassNotFoundException cnfe) {
				ClientLogger.LOG.severe("Cannot load the lexicon service client class: " + bundle.getLexSrvcClntClass() + " - Class not found");
			} catch (InstantiationException ie) {
				ClientLogger.LOG.severe("Cannot instantiate the lexicon service client class: " + bundle.getLexSrvcClntClass());
			} catch (IllegalAccessException iae) {
				ClientLogger.LOG.severe("Cannot access the lexicon service client class: " + bundle.getLexSrvcClntClass());
			} catch (Exception ex) {// any other exception
				ClientLogger.LOG.severe("Cannot load the lexicon service client: " + bundle.getLexSrvcClntClass() + " - " + ex.getMessage());
				ex.printStackTrace();
			}
		} else {
			ClientLogger.LOG.severe("Cannot load the lexicon service client: no Java library has been found ");
		}

		return null;
	}

	/**
	 * Creates a bundle containing information that can be used to create a Lexicon Service Client Factory.
	 * @param the lexicon stream
	 * @param the libraries
	 * @param the native libraries
	 * @param the baseDir
	 */
	public static void createBundle(InputStream lexStream, URL[] libs, URL[] natLibs, File baseDir) {
		boolean isLexClient = false;
		String binaryName = null;
		LexSrvcClntBundle bundle = null;
		LexSrvcClntParser parser = null;
		
		try {
			parser = new LexSrvcClntParser(lexStream);
			parser.parse();
			/*
			if (parser.type == null || (!parser.type.equals("direct") && !parser.type.equals("local"))) {
				ClientLogger.LOG.warning("Unsupported lexicon service client type, should be 'direct' or 'local': " + parser.type);
				return;
			}
			*/
			if (!parser.curOsSupported) {
				ClientLogger.LOG.warning("Lexicon service client does not support this Operating System: " + parser.name);
				return;
			}
			if (parser.implementor != null) {
				isLexClient = true;
				binaryName = parser.implementor;
			} else {
				ClientLogger.LOG.warning("The implementing class name has not been specified.");
				return;
			}
			
		} catch (SAXException sax) {
			ClientLogger.LOG.severe("Cannot parse metadata file: " + sax.getMessage());
		}
		
		if (isLexClient) {
			//if (parser.type.equals("direct")) {
				// create a classloader, bundle			
				LexSrvcClntLoader loader = new LexSrvcClntLoader(libs, natLibs);
	
				if (binaryName != null) {
					try {
						Class<?> c = loader.loadClass(binaryName);
						
						// if the above works, assume everything is all right
						bundle = new LexSrvcClntBundle();
						bundle.setName(parser.name);
						bundle.setDescription(parser.description);
						bundle.setParamList(parser.paramList);
						bundle.setLexSrvcClntClass(binaryName);
						//bundle.setLexExecutionType(parser.type);
						bundle.setJavaLibs(libs);
						bundle.setNativeLibs(natLibs);
						bundle.setBaseDir(baseDir);
							
						lexSrvcClntBundles.put(bundle.getName(), bundle);
					} catch (ClassNotFoundException cne) {
						ClientLogger.LOG.severe("Cannot load the lexicon service client class: " + binaryName + " - Class not found");
					} 
				}
				else {
					ClientLogger.LOG.warning("Cannot load the lexicon service client class: Class not found");
				}
			/*
			} else if (parser.type.equals("local")) {
				
				bundle = new LexSrvcClntBundle();
				bundle.setName(parser.name);
				bundle.setName(parser.description);// friendly name
				bundle.setParamList(parser.paramList);
				bundle.setLexSrvcClntClass(binaryName);// reuse the class name for the run command
				bundle.setLexExecutionType(parser.type);
				bundle.setBaseDir(baseDir);
				
				lexSrvcClntBundles.put(bundle.getName(), bundle);
			}
			*/			
		}
	}

}
