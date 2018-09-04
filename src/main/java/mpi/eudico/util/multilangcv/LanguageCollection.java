package mpi.eudico.util.multilangcv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;

/**
 * Administer a collection of languages, based on ISO-639-3.
 * Fetched from http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110.
 * Keep the collection as a singleton, softly referenced because it is big.
 *
 * @author olasei
 */
public class LanguageCollection {
	private static final String sourceURL = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110";
	/**
	 * After 100 days (expressed in seconds), fetch a new copy of the language information file.
	 */
	private static final long REFRESH_TIME = 100L * 24L * 60L * 60L;
			
	private static SoftReference<List<LangInfo>> languages;
	
	public static List<LangInfo> getLanguages() {
		if (languages != null) {
			List<LangInfo> langs = languages.get();
			if (langs != null) {
				return langs;
			}
		}
		try {
			return getLanguagesFromCacheFile();
		} catch (ParseException pe) {
			return null;
		}
	}
	
	/**
	 * Get the language's info, given a long or short id.
	 */
	
	public static LangInfo getLanguageInfo(String id){
		List<LangInfo> langs = getLanguages();
		
		LangInfo info = null;
		for(int i = 0; i < langs.size(); i++){
			info = langs.get(i);
			
			if(info.getId().equals(id) ||
				info.getLongId().equals(id)){
				return info;
			}
		}
		return null;
	}
	
	private static synchronized List<LangInfo> getLanguagesFromCacheFile() {
		// If we were locked out by the "synchronized" keyword and
		// someone else has loaded the cache already.
		List<LangInfo> langs;
		if (languages != null) {
			langs = languages.get();
			if (langs != null) {
				return langs;
			}
		}
		
		String cacheFileName = Constants.ELAN_DATA_DIR + File.separator + "ISO-639-3-Languages.xml";	
		File cacheFile = new File(cacheFileName);
		
		if (!cacheFile.exists() ||
				(System.currentTimeMillis() - cacheFile.lastModified()) > REFRESH_TIME * 1000) {
			boolean successFromServer = getLanguagesFromServer(sourceURL, cacheFileName);
			if (!successFromServer && !cacheFile.exists()) {
				// can return, otherwise a ParseException will be thrown
				return null;
			}
		}
		
	    LanguageHandler lh;

    	try {
    	    XMLReader reader;
	        reader = XMLReaderFactory.createXMLReader(
	        	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", false);
	        reader.setFeature("http://xml.org/sax/features/validation", false);
	        reader.setFeature("http://apache.org/xml/features/validation/schema", false);
	        reader.setFeature("http://apache.org/xml/features/validation/dynamic", false);
	        reader.setContentHandler(lh = new LanguageHandler());
	        reader.setErrorHandler(lh);
	    	reader.parse(cacheFileName);
    	} catch (SAXException se) {
    		se.printStackTrace();
    		throw new ParseException(se.getMessage());
    	} catch (IOException e) {
    		throw new ParseException(e.getMessage());
		}
    	
    	langs = lh.getLanguages();
		Collections.sort(langs, LangInfo.getLabelComparator());
		languages = new SoftReference<List<LangInfo>>(langs);
		
		return langs;
	}

	/**
	 * Fetch the data from the URL to a temporary file, and then if all
	 * went well, rename that temporary file to the desired file name.
	 * 
	 * Returns true if it seems to have worked.
	 * 
	 * @param cacheFileName
	 */
	public static boolean getLanguagesFromServer(String sourceURL, String cacheFileName) {
		String newFileName = cacheFileName + ".new";
		File newFile = new File(newFileName);
		boolean ok = false;
				
		HttpURLConnection conn;
		try {
			conn = getConnection(new URL(sourceURL));
	        int respCode = conn.getResponseCode();
            Object cont = conn.getContent();

            if (respCode == 200 && cont instanceof InputStream) {
                @SuppressWarnings("resource") // copy() closes is.
				InputStream is = (InputStream) cont;
    			copy(is, newFile);
    			ok = true;
            }
            conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		// Don't proceed if the result is no good.
		if (!ok || newFile.length() < 100) {
			return false;
		}
		
		// If the new file is substantially shorter than what we had, don't trust it either.
		File file = new File(cacheFileName);
		if (file.exists() && 
				(newFile.length() < file.length() * 2/3)) {
			return false;
		}

		return safeRename(newFile,
						  file,
						  new File(cacheFileName + ".bak"));
	}
	
    private static HttpURLConnection getConnection(URL url) {
        if (url == null) {
            return null;
        }

        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDefaultUseCaches(true);

            conn.setRequestMethod("GET");
            conn.connect();

            return conn;
        } catch (IOException ioe) {
        } catch (Exception e) {
        }
        
        return conn;
    }

	private static void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[4096];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Safely rename the 3 files:
	 * <ol>
	 * <li>bakfile gets removed
	 * <li>curfile becomes bakfile
	 * <li>newfile becomes curfile
	 * </ol>
	 * 
	 * Returns false if this fails at any point.
	 */
	
	private static boolean safeRename(File newFile, File curFile, File bakFile) {
		// file -> file.bak
		if (curFile.exists()) {
			// Be very conservative in performing operations, since they are documented
			// to be rather platform-dependent.
			if (bakFile.exists()) {
				if (!bakFile.delete()) {
					return false;
				}
			}
			// Here, bakfile does not exist any more: rename should really work now!
			if (!curFile.renameTo(bakFile)) {
				return false;
			}
		}
		// file.new -> file
		if (!newFile.renameTo(curFile)) {
			return false;
		}
		return true;
	}
	
    //#######################
    // Content handler
    //#######################
    static class LanguageHandler extends DefaultHandler implements ContentHandler {
    	private String conceptLink, appInfo;
    	private String content;
    	private List<LangInfo> languages = new ArrayList<LangInfo>();

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
            content = "";
            if (qName.equals("item")) {
            	conceptLink = atts.getValue("ConceptLink");
            	appInfo = atts.getValue("AppInfo");
            }
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
            if (qName.equals("item")) {
            	LangInfo li = new LangInfo(content.trim(), conceptLink, appInfo);
            	languages.add(li);
            }			
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
            content += new String(ch, start, length);
		}
		
		public List<LangInfo> getLanguages() {
			return languages;
		}
		
		@Override
		public void error(SAXParseException exception) throws SAXException {
			System.out.println("Error: " + exception.getMessage());
			// system id is the file path
			System.out.println("System id: " + exception.getSystemId());
			System.out.println("Public id: " + exception.getPublicId());
			System.out.println("Line: " + exception.getLineNumber());
			System.out.println("Column: " + exception.getColumnNumber());
			throw exception;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			System.out.println("FatalError: " + exception.getMessage());
			throw exception;
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			System.out.println("Warning: " + exception.getMessage());
		}
    }
    
    

    /**
     * Try to make sense of what the user typed in the edit box.
     * Let's hope they type something of similar form as what we show.
     * @param selection
     * @return
     */
	public static LangInfo tryParse(String selection) {
		String id = null, longId = null, label = "";
		
		String[] parts = selection.split(" - ");
		
		// Trim excess spaces
		for (int i = 0; i < parts.length; i++) {
			parts[i] = parts[i].trim();
		}
		
		if (parts.length == 3) {
			final String idPattern1 = "^[a-z][a-z0-9]{0,4}$";
			final String idPattern2 = "^[a-zA-Z][a-zA-Z0-9]{0,4}$";
			
			// Hope it is something like
			// "'Are'are (alu) - alu - http://cdb.iso.org/lg/CDB-00133314-002"
			if (parts[1].matches(idPattern1)) {
				// Okay, it seems so.
				label = parts[0];
				id = parts[1];
				longId = parts[2];
			} else 	if (parts[1].matches(idPattern2)) {
				label = parts[0];
				id = parts[1].toLowerCase();
				longId = parts[2];
			}
		}
		if (id != null && longId != null) {
			if (label.isEmpty()) {
				label = id;
			}
			return new LangInfo(id, longId, label);
		}
		return null;
	}

	/**
	 * Depending on how one views the importance of the 3-letter names, one can have different policies.
	 * <p>
	 * (1) If the user "invents a new language", then its long and short Ids should not be in the master list.
	 * Or, conversely, if either its long or short Id is in the list, the other one should correspond to it.
	 * <p>
	 * (2) The short id is just a suggestion, the long id is controlling to determine which language it is.
	 * If somebody wants to use a different short id than the one that is usual, that is fine with us.
	 * <p>
	 * Implement policy (1). There are various cases where even the short names in a list must be unique.
	 * One of them is the combined language list from the CVs in an EAF file.
	 * 
	 * @param selectedNewLanguage
	 * @return null if not valid, or the input if valid.
	 */
	public static LangInfo validate(LangInfo selectedNewLanguage) {
		if (selectedNewLanguage == null) {
			return null;
		}

		final String id = selectedNewLanguage.getId();
		final String longId = selectedNewLanguage.getLongId();
		
		for (LangInfo li : getLanguages()) {
			if (li.getId().equals(id)) {
				// Long one should match too
				if (li.getLongId().equals(longId)) {
					return selectedNewLanguage;
				}
				// long id does not match short id; not valid.
				return null;
			} else if (li.getLongId().equals(longId)) {
				// short Id does not match long Id; not valid.
				return null;
			}
		}
		// Neither the short or long ids were found in the list, this is a truly new language.
		return selectedNewLanguage;
	}
}
