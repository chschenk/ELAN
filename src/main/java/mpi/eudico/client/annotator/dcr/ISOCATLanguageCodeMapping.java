package mpi.eudico.client.annotator.dcr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import mpi.eudico.client.annotator.util.ClientLogger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Mapping of ISOCat language codes( 2 letter code)
 * with the Elan language code(3 letter code)
 * 
 * 
 * 
 * @author aarsom
 */
public class ISOCATLanguageCodeMapping {
	//private static final String sourceURL = "http://www.isocat.org/rest/info/languages.xml";
	
	//private static final String cacheFileName = Constants.ELAN_DATA_DIR + File.separator + "languages.xml";
	
	/**
	 * After 100 days (expressed in seconds), fetch a new copy of the language information file.
	 */
	//private static final long REFRESH_TIME = 100L * 24L * 60L * 60L;
	
	private static HashMap<String, String> languageCodes;
			
	
	public static String get2LetterLanguageCode(String lang) {
		
		if(languageCodes == null){
			loadLanguageCodesFromCacheFile();
		}
		
		return languageCodes.get(lang);
		
	}
	
	/**
	 * Parses the language codes from the cache file. If the file is absent or old,
	 * fetch it from the web service.
	 * Unfortunately, as of january 2015, the web service has ended.
	 * Instead we load the codes directly from our own copy in our resources,
	 * and don't bother with a copy of that.
	 */
	private static synchronized void loadLanguageCodesFromCacheFile() {		
		languageCodes = new HashMap<String, String>();
//		File cacheFile = new File(cacheFileName);
		
//		if (!cacheFile.exists() ||
//				(System.currentTimeMillis() / 1000) - cacheFile.lastModified() < REFRESH_TIME) {
//			getLanguageCodesFileFromServer(cacheFileName);
//		}
		
    	InputStream istr = null;
    	try {
    	    XMLReader reader;
	        reader = XMLReaderFactory.createXMLReader(
	        	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", false);
	        reader.setFeature("http://xml.org/sax/features/validation", false);
	        reader.setFeature("http://apache.org/xml/features/validation/schema", false);
	        reader.setFeature("http://apache.org/xml/features/validation/dynamic", false);
	        reader.setContentHandler(new LanguageCodeHandler());
//	    	reader.parse(cacheFileName);
	        URL url = reader.getClass().getResource("/org/isocat/resources/languages.xml");
	        istr = url.openStream();
	        InputSource iso = new InputSource(istr);
	        reader.parse(iso);
    	} catch (SAXException se) {
    		ClientLogger.LOG.warning("Unable to parse the ISOcat languages cache file" + se.getMessage());
    		//se.printStackTrace();
    	} catch (IOException e) {
    		ClientLogger.LOG.warning("Unable to read the ISOcat languages cache file" + e.getMessage());
        } finally {
			try {
				if (istr != null) {
					istr.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Fetch the data from the URL to a temporary file, and then if all
	 * went well, rename that temporary file to the desired file name.
	 * 
	 * Returns true if it seems to have worked.
	 * 
	 * @param cacheFileName
	 */
//	private static boolean getLanguageCodesFileFromServer(String cacheFileName) {
//		return LanguageCollection.getLanguagesFromServer(sourceURL, cacheFileName);
//	}
	
    //#######################
    // Content handler
    //#######################
    static class LanguageCodeHandler extends DefaultHandler{

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if (qName.equals("language")) {
                String lang = atts.getValue("tag");
                String langList = atts.getValue("tags");
                
                String[] languages = langList.split(" ");
                for (String language : languages) {
                	languageCodes.put(language, lang);
                }
            }
		}
    }
}
