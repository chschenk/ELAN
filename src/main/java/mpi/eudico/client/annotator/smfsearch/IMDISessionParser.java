package mpi.eudico.client.annotator.smfsearch;

import mpi.eudico.client.annotator.util.FileUtility;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;


/**
 * Parses an IMDI session file, to extract any .eaf file.
 * Can be reused for multiple session files.
 *
 * @author Han Sloetjes
 */
public class IMDISessionParser {
	/** Holds value of property eaf 
	 * Dec 2005: the format string for eaf files in imdi files has been changed
	 * to "text/x-eaf+xml".
	 */
	public static final String EAF = "text/x-eaf+xml";
	//private final String EAF = "eaf";
	
    private XMLReader reader;
    private List<String> files;

    /**
     * Creates a new IMDISessionParser instance
     */
    public IMDISessionParser() throws SAXException {
        files = new ArrayList<String>(5);
        
        reader = XMLReaderFactory.createXMLReader(
                "org.apache.xerces.parsers.SAXParser");
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setContentHandler(new SessionHandler());
    }

    /**
     * Starts the actual parsing of the imdi file.
     *
     * @param pathToImdiSession the path to the imdi file
     * @return a List containing all eaf files
     * 
     * @throws IOException io exception
     * @throws SAXException parse exceoption
     */
    public synchronized List<String> parse(String pathToImdiSession)
        throws IOException, SAXException {
        if (pathToImdiSession == null) {
            return null;
        }
        files.clear();
        reader.parse(pathToImdiSession);
        // convert relative paths to absolute paths
        pathToImdiSession = pathToImdiSession.replace('\\', '/');
        ArrayList<String> retList = new ArrayList<String>(files.size());
        String path = null;
        for (int i = 0; i < files.size(); i++) {
        	path = files.get(i);
        	if (path.startsWith("../") || path.startsWith("./")) {
        		retList.add(FileUtility.getAbsolutePath(pathToImdiSession, path));
        	} else {
        		retList.add(path);
        	}
        }
        return retList;
    }

    /*******************************************************************/

    /**
     * the content handler
     */
    class SessionHandler implements ContentHandler {
        /** Holds value of property MediaFile */
        //private final String MF = "MediaFile";

        /** Holds value of property AnnotationUnit */
        //private final String AU = "AnnotationUnit";
        
		/** Holds value of property WrittenResource */
        private final String WRITTEN_RES ="WrittenResource";

        /** Holds value of property ResourceLink */
        private final String LINK = "ResourceLink";

        /** Holds value of property Type */
        private final String TYPE = "Type";

        /** Holds value of property Format */
        private final String FORMAT = "Format";
		
        /** Holds value of property video */
        //private final String VIDEO = "video";

        /** Holds value of property audio */
        //private final String AUDIO = "audio";
        private String curResLink;
        private String curContent;
        private String curFormat;
        private String curType;
        private boolean inWritten = false;

        /**
         * End of document
         *
         * @throws SAXException parse ex
         */
        @Override
		public void endDocument() throws SAXException {
        }

        /**
         * Start of document
         *
         * @throws SAXException parse ex
         */
        @Override
		public void startDocument() throws SAXException {
        }

        /**
         * The contents of an element
         *
         * @param ch the characters
         * @param start start index
         * @param length number of characters
         *
         * @throws SAXException parse ex
         */
        @Override
		public void characters(char[] ch, int start, int length)
            throws SAXException {
            curContent = new String(ch, start, length);
        }

        /**
         * Ignorable white spaces.
         *
         * @param ch the characters
         * @param start start index
         * @param length number of characters
         *
         * @throws SAXException parse ex
         */
        @Override
		public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        }

        /**
         * End of prefix mapping.
         *
         * @param prefix the prefix
         *
         * @throws SAXException parse ex
         */
        @Override
		public void endPrefixMapping(String prefix) throws SAXException {
        }

        /**
         * Skipped entity.
         *
         * @param name name of entity
         *
         * @throws SAXException parse ex
         */
        @Override
		public void skippedEntity(String name) throws SAXException {
        }

        /**
         * Sets the document locator.
         *
         * @param locator the locator
         */
        @Override
		public void setDocumentLocator(Locator locator) {
        }

        /**
         * Instruction.
         *
         * @param target the target
         * @param data the data
         *
         * @throws SAXException parse ex
         */
        @Override
		public void processingInstruction(String target, String data)
            throws SAXException {
        }

        /**
         * Start of prefix mapping.
         *
         * @param prefix the prefix
         * @param uri prefix uri
         *
         * @throws SAXException parse ex
         */
        @Override
		public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        }

        /**
         * End of element. Handles the elements we are interested in.
         *
         * @param namespaceURI namespace
         * @param localName name of thwe element
         * @param qName raw name?
         *
         * @throws SAXException parse ex
         */
        @Override
		public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
        	if (inWritten) {
	            if (localName.equals(TYPE)) {
	                curType = curContent;
	            } else if (localName.equals(FORMAT)) {
	                curFormat = curContent;
	            } else if (localName.equals(LINK)) {
	                curResLink = curContent;
	            } else if (localName.equals(WRITTEN_RES)) {
	                if (curFormat != null && curFormat.equalsIgnoreCase(EAF)) {
	                	if (curResLink != null) {
	                		files.add(urlToPath(curResLink));
	                	}
	                } else if (curResLink != null && curResLink.toLowerCase().endsWith(".eaf")) {
	                	files.add((curResLink));
	                }
	
	                resetFields();
	                inWritten = false;
	            } 
        	}
        }

        /**
         * Start of an element. Ignored.
         *
         * @param namespaceURI namespace
         * @param localName name of element
         * @param qName raw name?
         * @param atts the attributes of the element
         *
         * @throws SAXException parse ex
         */
        @Override
		public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        	if (qName.equals(WRITTEN_RES)) {
        		inWritten = true;
        	}
        }

        /**
         * Resets the fields at the end of a relevant element.
         */
        void resetFields() {
            curResLink = null;
            curContent = null;
            curFormat = null;
            curType = null;
        }
        
        /**
         * Converts the session url's "file:/... " to file paths.
         * Removes "/../" relative path parts, converts a relative path to absolute path.
         * 
         * @param url the url
         * @return the path or null
         */
        private String urlToPath(String url) {
            if (url == null) {
                return url;
            }

            try {
                URL u = new URL(url);
                String prot = u.getProtocol();

                if (prot != null) {
                    if (prot.equals("file")) {
                    	// remove xxx/../yyy structures from the path
                    	String path = u.getPath();
                    	int index = path.indexOf("/../");
                    	if (index > 0) {
                    		int prevSl = path.lastIndexOf('/', index - 1); 
                    		if (prevSl > -1) {
                    			path = path.substring(0, prevSl) + path.substring(index + 3);
                    		}
                    	}
                    	
                        return path;
                    } else {
                        // just return the url?
                        return url;
                    }
                } else {
                	// remove xxx/../yyy structures from the path
                	String path = url;
                	int index = path.indexOf("/../");
                	if (index > 0) {
                		int prevSl = path.lastIndexOf('/', index - 1); 
                		if (prevSl > -1) {
                			path = path.substring(0, prevSl) + path.substring(index + 3);
                		}
                	}
                	
                    return path;
                    //return url;
                }
            } catch (MalformedURLException mue) {
                return url;
            }
        }
    }
}
