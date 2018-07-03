package mpi.eudico.client.annotator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mpi.eudico.client.annotator.lexicon.api.LexSrvcAvailabilityDetector;
import mpi.eudico.client.annotator.recognizer.api.RecogAvailabilityDetector;
import mpi.eudico.util.ExtClassLoader;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A class to detect all the cmdi's available 
 * in the Extensions folder and creates bundles 
 * of its respective cmdi type.
 * 
 * Currently supported CMDI types
 * 		- Recognizer
 * 		- Lexicon service client
 * 
 * Should be updated when new CMDI types are supported 
 * 
 * @updated by aarsom
 * @version Sep 2012
 */
public class AvailabilityDetector {
	
	private static final int LEXICON_CMDI = 0;
	private static final int RECOGNIZER_CMDI = 1;	
	
	private static CMDIParser parser;
	
	private static boolean initialized = false;
	
	/**
	 * Private constructor
	 */
	protected AvailabilityDetector() {
		super();
	}
	
	/**
	 * Tries to find extensions in the designated directory and detects all the
	 * available cmdi files
	 *  
	 */
	public static void loadFilesFromExtensionsFolder(){
		if(initialized){
			return;
		}
		
		File extFile = new File (ExtClassLoader.EXTENSIONS_DIR);
			
		if (!extFile.exists()) {
			ClientLogger.LOG.warning("The extension folder could not be found (" + ExtClassLoader.EXTENSIONS_DIR + ").");
			return;
		}
		if (!extFile.isDirectory()) {
			ClientLogger.LOG.warning("The extension \'folder\' is not a folder (" + ExtClassLoader.EXTENSIONS_DIR + ").");
			return;
		}
		if (!extFile.canRead()) {
			ClientLogger.LOG.warning("The extension folder is not accessible (" + ExtClassLoader.EXTENSIONS_DIR + ").");
			return;
		}
			
		File[] files = extFile.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				detectFromFolder(f);
			} else {
				//load from jar or zip
				String name = f.getName().toLowerCase();
				if (name.endsWith("jar")) {
					detectFromJar(f);
				} else if (name.endsWith("zip")) {
					detectFromZip(f);
				}
			}
		}
		
		initialized = true;		
	}
	
	/**
	 * Detects the CMDI files from the folder and creates a bundle for its type
	 * 
	 * @param file the directory 
	 */
	private static void detectFromFolder(File folder) {
		File[] files = folder.listFiles();	
		List<URL> libs = null;
		List<URL> natLibs = null;
		
		boolean cmdiFound = false;
		boolean libsAdded = false;
		
		for (File f : files) {
			if (f.getName().toLowerCase().endsWith(".cmdi") && !f.getName().startsWith(".")) {				
				try {
					FileInputStream stream;
					int type = getCMDIType(stream = new FileInputStream(f));
					stream.close();
					if(type > -1){
						if(!libsAdded){
							libs = new ArrayList<URL>();
							natLibs = new ArrayList<URL>();
							addLibs(folder, libs, natLibs);
							libsAdded = true;
						}
						boolean bool= createBundle(type, stream = new FileInputStream(f), libs, natLibs, folder);
						stream.close();
						if(!cmdiFound ){
							cmdiFound  = bool;
						}
					}
				} catch (FileNotFoundException fnfe) {
					ClientLogger.LOG.severe("File not found:" + fnfe.getMessage());	
				} catch (IOException e) { // for stream.close()
				}
			} 
		}
		
		if (!cmdiFound) {
			ClientLogger.LOG.severe("No cmdi metadata file found in: " + folder.getName());
		}
	}
	
	/**
	 * Detects the CMDI files from the jar file and creates a bundle for its type
	 * 
	 * @param file the jar file 
	 */
	private static void detectFromJar(File file) {
		try {
			JarFile jFile = new JarFile(file);	
			
			List<URL> libs = null ;
			List<URL> natLibs = null;
			
			boolean cmdiFound = false;		
			boolean libsAdded = false;
			
			Enumeration<JarEntry> entries = jFile.entries();
			while(entries.hasMoreElements()){
				JarEntry je = entries.nextElement();
				if(je.getName().toLowerCase().endsWith(".cmdi") && !je.getName().startsWith(".")){					
					try {
						int type = getCMDIType(jFile.getInputStream(je));
						if(type > -1){
							if(!libsAdded){
								libs = new ArrayList<URL>();
								natLibs = new ArrayList<URL>();
								addLibs(file, libs, natLibs);
								libsAdded = true;
							}
							boolean bool= createBundle(type, jFile.getInputStream(je), libs, natLibs, file.getParentFile());
							if(!cmdiFound ){
								cmdiFound  = bool;
							}
						}
					} catch (MalformedURLException mue) {
						ClientLogger.LOG.severe("Cannot create URL for file: " + file.getName());
					} catch (IOException ioe) {
						ClientLogger.LOG.warning("Cannot read the cmdi file from the jar file: " + file.getName());
					}
				}
			}
			
			if (!cmdiFound) {
				ClientLogger.LOG.warning("No plug-in cmdi metadata file found in " + file.getName());
				return;
			}
			
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("Cannot read the jar file: " + file.getName());
		}
	}	
	
	/**
	 * Detects the CMDI files from the zip file and creates a bundle for its type
	 * 
	 * @param file the zip file 
	 */
	private static void detectFromZip(File file) {
		try {
			ZipFile zFile = new ZipFile(file);	
			
			List<URL> libs = null ;
			List<URL> natLibs = null;
			
			boolean cmdiFound = false;		
			boolean libsAdded = false;
			
			Enumeration<? extends ZipEntry> entries = zFile.entries();
			while(entries.hasMoreElements()){
				ZipEntry ze = entries.nextElement();
				if(ze.getName().toLowerCase().endsWith(".cmdi") && !ze.getName().startsWith(".")){
					try {
						int type = getCMDIType(zFile.getInputStream(ze));
						if(type > -1){
							if(!libsAdded){
								libs = new ArrayList<URL>();
								natLibs = new ArrayList<URL>();
								addLibs(file, libs, natLibs);
								libsAdded = true;
							}
							boolean bool= createBundle(type, zFile.getInputStream(ze), libs, natLibs, file.getParentFile());
							if(!cmdiFound ){
								cmdiFound  = bool;
							}
						}
					} catch (MalformedURLException mue) {
						ClientLogger.LOG.severe("Cannot create URL for file: " + file.getName());							
					} catch (IOException ioe) {
						ClientLogger.LOG.warning("Cannot read the cmdi file from the zip file: " + file.getName());
					}
				}
			}
			
			if (!cmdiFound) {
				ClientLogger.LOG.warning("No plug-in cmdi metadata file found in " + file.getName());
				return;
			}
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("Cannot read the zip file: " + file.getName());
		}
	}
	
	/** Detects the type of CMDI file and return its type
	 * 
	 * @param stream the inputstream for the cmdi file
	 * @return int, the type of the cmdi file
	 */
	private static int getCMDIType(InputStream stream){
		parser = new CMDIParser(stream);
		try {
			parser.parse();
			stream.close();
			return parser.getCMDIType();	
		} catch (SAXException se) {
			ClientLogger.LOG.severe("Cannot parse metadata file: " + stream.toString() +" :" + se.getMessage());	
		} catch (IOException ioe) {
			ClientLogger.LOG.severe(ioe.getMessage());	
		}		
		return -1;
	}	
	
	/**
	 * Identifies the type of CMDI and creates a bundle for it.
	 * 
	 * Should be updated when new CMDI types are supported 
	 * 
	 * @param type the cmdi type indicates what type of bundle should be recreated
	 * @param mdStream the stream representing the "recognizer.cmdi" from a directory or from a jar
	 * @param libs the recognizer's Java libraries
	 * @param natLibs the recognizer's native libraries
	 * @param baseDir the directory the recognizer runs from
	 */
	private static boolean createBundle(int type, InputStream stream, List<URL> libs, List<URL> natLibs, File basedir){	
		if(type > -1){					
			URL[] libUrls = null;
			if (libs.size() > 0) {
				libUrls = libs.toArray(new URL[]{});
			}
			URL[] natLibUrls = null;
			if (natLibs.size() > 0) {
				natLibUrls = natLibs.toArray(new URL[]{});
			}				
			switch(type){
				case LEXICON_CMDI:
					LexSrvcAvailabilityDetector.createBundle(stream, libUrls, natLibUrls, basedir);
					//ClientLogger.LOG.warning("cmdi for lexicon service client found: " + stream.toString());
					break;
				case RECOGNIZER_CMDI:
					RecogAvailabilityDetector.createBundle(stream, libUrls, natLibUrls, basedir);
					//ClientLogger.LOG.warning("cmdi for recognizer found: " + stream.toString());
					break;
			}
			
			return true;
		}
		return false;
	}
	
	/**
	 * Recursively adds jar files and native libraries to the lists.
	 * 
	 * @param file the folder to search for libraries
	 * @param libs the list of Java libraries
	 * @param natLibs the list of native libraries
	 */
	private static void addLibs (File file, List<URL> libs, List<URL> natLibs) {		
		
		String name = file.getName().toLowerCase();
		if (name.endsWith("jar") || name.endsWith("zip")) {
			try {
				libs.add(file.toURI().toURL());
			} catch (MalformedURLException mue) {
				ClientLogger.LOG.severe("Cannot create URL for file: " + file.getName());
			}
		} else if (file.isDirectory()){
			File[] files = file.listFiles();	
			if(files == null){
				return;
			}
			for (File f : files) {
				try {
					if (f.isDirectory()) {
						addLibs(f, libs, natLibs);
					} else {
						name = f.getName().toLowerCase();
						if (name.endsWith("jar") || name.endsWith("zip")) {
							try {
								libs.add(f.toURI().toURL());
							} catch (MalformedURLException mue) {
								ClientLogger.LOG.severe("Cannot create URL for file: " + f.getName());
							}
						} else if (name.endsWith("dll") || name.endsWith("so") || name.endsWith("jnilib")) {
							try {
								natLibs.add(f.toURI().toURL());
							} catch (MalformedURLException mue) {
								ClientLogger.LOG.severe("Cannot create URL for file: " + f.getName());
							}
						}
					}
				} catch (SecurityException se) {
					ClientLogger.LOG.warning("Cannot read file: " + f.getName());
				}
			}
		}
		
		
		
	}	
	
	/**
	 * CMDI parser which used used to identify the
	 * the type of cmdi file
	 * 
	 * Currently detected CMDI file types
	 * 		- Recognizer
	 * 		- Lexicon service client
	 * 
	 * Should be updated when new CMDI types are supported 
	 * 
	 * @author aarsom
	 *
	 */
	private static class CMDIParser implements ContentHandler {
		
		private InputStream inputStream;	
		private int cmdi_type = -1;
		
		public CMDIParser(InputStream inputStream) {
			super();
			this.inputStream = inputStream;			
		}
		
		public void parse()  throws SAXException {
			if (inputStream != null) {
				try {
					XMLReader reader = XMLReaderFactory.createXMLReader(
			    		"org.apache.xerces.parsers.SAXParser");
					reader.setContentHandler(this);
					reader.parse(new InputSource(inputStream));
				} catch (IOException ioe) {
					throw new SAXException(ioe);
				}
			} else {
				throw new SAXException("No input stream specified");
			}
		}
		
		/**
		 * Returns the cmdi type of the current cmdi file
		 * 
		 * @return cmdi_type
		 */
		public int getCMDIType(){
			return cmdi_type;
		}
		
		//############## ContentHandler methods ######################################

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {	
		}

		/**
		 * Should be updated when new CMDI types are supported  
		 */
		@Override
		public void startElement(String nameSpaceURI, String name,
	            String rawName, Attributes attributes) throws SAXException {
			if (name.equals("RECOGNIZER")) {
				if(cmdi_type == -1){
					cmdi_type = RECOGNIZER_CMDI;
				}			
			} else if (name.equals("lexiconserviceclient")) {
				if(cmdi_type == -1){
					cmdi_type = LEXICON_CMDI;
				}	
			}
		}

		@Override
		public void endElement(String nameSpaceURI, String name, String rawName)
		throws SAXException {
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
		}

		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException {
		}

		@Override
		public void processingInstruction(String arg0, String arg1)
				throws SAXException {
		}

		@Override
		public void setDocumentLocator(Locator arg0) {
		}

		@Override
		public void skippedEntity(String arg0) throws SAXException {
		}

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException {
		}
	}
}
