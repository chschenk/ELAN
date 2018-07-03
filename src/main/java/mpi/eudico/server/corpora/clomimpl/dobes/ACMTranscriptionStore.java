package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.ParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A class with a single method that returns the current ACM TranscriptionStore.
 * To be used when no specific version is required, it returns the latest version.
 * <br>
 * This way there will be a single location to be changed when a new version of 
 * the transcription store becomes available.
 *  
 * @author Han Sloetjes
 * @version 1.0
  */
public class ACMTranscriptionStore {
    /**
     * Creates a new ACMTranscriptionStore instance
     */
    private ACMTranscriptionStore() {
        // not to be instantiated
    }

    /**
     * Returns the current version of ACM Transcription Store.
     * Note: this methods creates a new instance of the transcription store 
     * for each call
     *
     * @return the current version of the ACM Transcription Store
     */
    public static final TranscriptionStore getCurrentTranscriptionStore() {
        return new ACM30TranscriptionStore();
    }
    
    /**
     * Returns the current (latest) parser for .eaf files.
     * 
     * @return the current (latest) parser for .eaf files
     */
    public static final Parser getCurrentEAFParser() {
    	return new EAF30Parser();
    }
    
    /**
     * The entity resolver is used to determine which local xsd to use for parsing
     * 
     * @return the current (latest) entity resolver
     */
    public static final EntityResolver getCurrentEAFResolver() {
    	return new EAF30Parser.EAFResolver();
    }
    
    /**
     * Returns the path to the current (latest) local version of the EAF schema.
     * Local means the location in the source tree.
     * 
     * @return the path to the current EAF schema
     */
    public static final String getCurrentEAFSchemaLocal() {
    	return EAF30.EAF30_SCHEMA_RESOURCE;
    }
    
    /**
     * Returns the path to the current (latest) remote version of the EAF schema.
     * Remote means the (official) URL of the EAF schema.
     * 
     * @return the URL (as a string) to the current EAF schema
     */
    public static final String getCurrentEAFSchemaRemote() {
    	return EAF30.EAF30_SCHEMA_LOCATION;
    }

	/**
	 * Check the barest minimum from the file in order to
	 * find the format version number.
	 * 
	 * @return ParserFactory.EAF27, or .EAF28, or newer.
	 */
    public static int eafFileFormatTaster(String trPathName) {
    	XMLReader reader;
		TasterContentHandler handler = new TasterContentHandler();

		int version = ParserFactory.EAF30;
    	FileInputStream fis = null;
		try {
			reader = XMLReaderFactory.createXMLReader(
			    	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", false);
	        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			reader.setContentHandler(handler);
	        // This works to make sure the schema isn't fetched from the web but from here:
	        reader.setEntityResolver(getCurrentEAFResolver()); // see http://www.saxproject.org/apidoc/org/xml/sax/EntityResolver.html
			// the SAX parser can have difficulties with certain characters in
			// the trPathName: instead create an InputSource with FileInputStream for the parser.
	        File f = new File(trPathName);	
			fis = new FileInputStream(f);
			InputSource source = new InputSource(fis);
			source.setSystemId(trPathName);
			reader.parse(source);
		} catch (SAXParseException e) {
			// It's okay, we threw that ourselves.
		} catch (SAXException e) {
			e.printStackTrace();
			return version;
		} catch (IOException e) {
			e.printStackTrace();
			return version;
        } finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
		}

		if (handler.format != null) {
			// Okay, parse version numbers such as 2.7, 3.14, etc.
			Scanner scanner = new Scanner(handler.format).useDelimiter("\\.");
			int majorVersion = scanner.nextInt();
			int minorVersion = scanner.nextInt();
			
			int toolow = ParserFactory.EAF26;
			int toohigh = ParserFactory.EAF30;
			
			// Don't check all versions, use some defaults.
			if (majorVersion < 2) {
				version = toolow;
			} else if (majorVersion == 2) {
				if (minorVersion < 7) {
					version = toolow;
				} else if (minorVersion == 7) {
					version = ParserFactory.EAF27;
				} else if (minorVersion == 8) {
					version = ParserFactory.EAF28;
				} else if (minorVersion > 8) {
					version = toohigh;
				}
			} else if (majorVersion > 2) {
				version = toohigh;
			}			
		}
		
		return version;
	}
    
	static class TasterContentHandler implements ContentHandler {
		String format;

		@Override
		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException {
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
		public void startElement(String uri, String localname, String qName,
				Attributes atts) throws SAXException {
			if (localname.isEmpty()) {
				localname = qName;
			}
			if (localname.equals("ANNOTATION_DOCUMENT")) {
				format = atts.getValue("FORMAT");
			}
			// Now we're done... we can stop.
			throw new SAXParseException("Seen enough of the document", null);
		}

		@Override
		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException {
		}
	}

}
