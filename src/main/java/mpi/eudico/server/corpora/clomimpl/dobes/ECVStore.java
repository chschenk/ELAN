package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.util.ExternalCV;

/**
 * Stores and load an External CV
 * 
 * @author Micha Hulsbosch
 * @version jul 2010
 */
public class ECVStore {
	
	/**
	 * Loads an External CV from a url. This is not optimal for the case where 
	 * an external file contains multiple CV's.
	 * 
	 * @param cv
	 * @param url
	 * @param theTranscription
	 * @throws ParseException
	 */
	public void loadExternalCV(ExternalCV cv, String url) 
		throws ParseException {
		
		List<ExternalCV> ecvList = new ArrayList<ExternalCV>(1);
		ecvList.add(cv);
		loadExternalCVS(ecvList, url);
		
//		ECV02Parser ecvParser = null;
//        try {
//        	ecvParser = new ECV02Parser(url);
//        	ecvParser.parse(null);
//        } catch (ParseException pe) {
//        	System.out.println("Parse failed " + url);
//        	throw(pe);
//        }
//        
//        // get the ext refs mappings
//		Map<String, ExternalReference> extReferences = ecvParser.getExternalReferences();
//        
//        ArrayList<ControlledVocabulary> allCVs = ecvParser.getControlledVocabularies();
//
//        ExternalCV cvFromUrl = null;
//        for (int i = 0; i < allCVs.size(); i++) {
//        	cvFromUrl = (ExternalCV) allCVs.get(i);
//        	if(cvFromUrl.getName().equals(cv.getName())) {
//        		cv.moveAll(cvFromUrl);
//        	}
//        } 
	}
	
	/**
	 * Loads all entries for External Controlled Vocabularies from the specified url.
	 * The ECV objects have been created beforehand (e.g. when parsing an eaf file).
	 * 
	 * Any ECVs that are found at the url will be modified by adding the found entries.
	 * 
	 * @param ecvList the list of ECV objects, should not be null
	 * @param url the url of the file containing the controlled vocabularies
	 * 
	 * @throws ParseException
	 */
	public void loadExternalCVS(List<ExternalCV> ecvList, String url) throws ParseException {
		if (ecvList == null || ecvList.size() == 0) {
			return;// return silently
		}
		ECV02Parser ecvParser = null;
        try {
        	ecvParser = new ECV02Parser(url);
        	ecvParser.parse(ecvList);
        } catch (ParseException pe) {
        	ServerLogger.LOG.severe("Parse failed " + url);
        	throw(pe);
        }
        
        // get the ext refs mappings
//		Map<String, ExternalReference> extReferences = ecvParser.getExternalReferences();
        
//        ArrayList<ControlledVocabulary> allCVs = ecvParser.getControlledVocabularies();
//
//        ExternalCV cvFromUrl = null;
//        ExternalCV cvFromList = null;
//        for (int j = 0; j < ecvList.size(); j++) {
//        	cvFromList = ecvList.get(j);
//        	
//	        for (int i = 0; i < allCVs.size(); i++) {
//	        	cvFromUrl = (ExternalCV) allCVs.get(i);
//	        	// checking equality by name might fail if the name has been changed there where
//	        	// it is used, by the "client"
//	        	if(cvFromUrl.getName().equals(cvFromList.getName())) {
//	        		cvFromList.moveAll(cvFromUrl);
//	        		break;
//	        	}
//	        }
//        }
	}
	
	/**
	 * Stores an External CV (not implemented yet)
	 * 
	 * @see mpi./eudico/server/corpora/clomimpl/dobes/ECV02Encoder
	 *  
	 * @param cv a single external CV
	 * @param cachePath the cache base folder
	 * @param urlString the location of the source file
	 */
	public void storeExternalCV(ExternalCV cv, String cachePath, String urlString) {
		if (cv == null) {
			ServerLogger.LOG.warning("Could not create a cached version: no external CV provided.");
			return;
		}
		List<ExternalCV> list = new ArrayList<ExternalCV>(1);
		storeExternalCVS(list, cachePath, urlString);
	}
	
	/**
	 * Creates a cached version of the controlled vocabularies loaded from the same
	 * external source.
	 * 
	 * @param ecvList the list of controlled vocabularies
	 * @param cachePath the path to the cache base folder
	 * @param urlString the source file
	 */
	public void storeExternalCVS(List<ExternalCV> ecvList, String cachePath, String urlString) {
		if (ecvList == null || ecvList.size() == 0) {
			ServerLogger.LOG.warning("Could not create a cached version: no external CV's provided.");
			return; // return silently
		}
		if (cachePath == null) {
			ServerLogger.LOG.warning("Could not create a cached version: no cache folder specified.");
			return;
		}
		if (urlString == null) {
			ServerLogger.LOG.warning("Could not create a cached version: no source URL specified.");
			return;
		}
		
		ExternalReferenceImpl eri = new ExternalReferenceImpl(urlString, ExternalReference.EXTERNAL_CV);
		try {
			ECV02Encoder encoder = new ECV02Encoder();
			encoder.encodeAndSave(ecvList, cachePath, eri);
		} catch (Throwable thr) {// catch anything that can go wrong, caching is not crucial
			ServerLogger.LOG.severe("Could not create a cached version: " + thr.getMessage());
		}
	}
	
	/**
	 * Performs a quick test on the version of the ECV file and returns it
	 * as a string. 
	 * 
	 * @version Jan 2018
	 * @param path the location of the file to test 
	 * @return the version as a string, currently 0.1 or 0.2
	 * 
	 * @see {@link ACMTranscriptionStore#eafFileFormatTaster(String)}
	 */
	public String ecvFileFormatTest(String path) {
		XMLReader reader;
		FormatTestHandler handler = new FormatTestHandler();
		FileInputStream fis = null;
		InputSource source = null;
		String version = ECV02Encoder.VERSION;
		
		try {
			reader = XMLReaderFactory.createXMLReader(
			    	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", false);
	        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			reader.setContentHandler(handler);
			
			try {
				File f = new File(path);
				fis = new FileInputStream(f);
				source = new InputSource(fis);
			} catch (IOException ioe) {
				// try path as url
				URI pathUri;
				try {
					pathUri = new URI(path);
					source = new InputSource(pathUri.toURL().openStream());
				} catch (URISyntaxException e) {
					throw ioe;
				}
			}
			source.setSystemId(path);
			reader.parse(source);
		} catch (SAXParseException e) {
			// We threw that ourselves, the version is found.
		} catch (SAXException e) {
			if (ServerLogger.LOG.isLoggable(Level.WARNING)) {
				ServerLogger.LOG.warning(e.getMessage());
			}
			return version;
		} catch (IOException e) {
			if (ServerLogger.LOG.isLoggable(Level.WARNING)) {
				ServerLogger.LOG.warning(e.getMessage());
			}
			return version;
        } finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
		}
		
		if (handler.version != null) {
			if (ServerLogger.LOG.isLoggable(Level.FINE)) {
				ServerLogger.LOG.fine("The .ecv file has version: " + handler.version);
			}
			return handler.version;
		}
		
		return version;
	}
	
	/**
	 * Handler class that retrieves the version and then stops (by throwing an exception).
	 * @version Jan 2018
	 */
	class FormatTestHandler extends DefaultHandler {
		String version;
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (localName.isEmpty()) {
				localName = qName;
			}
			if (localName.equals("CV_RESOURCE")) {
				version = attributes.getValue("VERSION");
			}
			// Now we're done... we can stop.
			throw new SAXParseException("Seen enough of the document", null);
		}
	}
}
