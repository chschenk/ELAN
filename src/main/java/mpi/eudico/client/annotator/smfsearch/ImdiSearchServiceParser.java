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
 * A non-reusable, one-file-per-parser class.
 *
 * @author Han Sloetjes
 */
public class ImdiSearchServiceParser {
    private List<String> sessionFiles;
    private XMLReader reader;
    private String corpusFilePath;

    // can be used to read information from special Description elements
    private boolean isSearchService = false;

    /**
     * Creates a reader and starts parsing immediately.
     *
     * @param corpusFilePath the path to a corpus file
     *
     * @throws IOException i/o exception
     * @throws SAXException if there problems creating the parser or 
     * parsing the file.
     */
    public ImdiSearchServiceParser(String corpusFilePath)
        throws IOException, SAXException {
        super();

        if (corpusFilePath == null) {
            throw new IOException("Cannot parse file <null>");
        }
        this.corpusFilePath = corpusFilePath.replace('\\', '/');
        
        sessionFiles = new ArrayList<String>();
        reader = XMLReaderFactory.createXMLReader(
                "org.apache.xerces.parsers.SAXParser");
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setContentHandler(new CorpusHandler());
        reader.parse(corpusFilePath);
    }

    /**
     * Returns the extracted links to session files.
     *
     * @return a list containing the extracted session files
     */
    public List<String> getSessionFiles() {
        return sessionFiles;
    }
    
    /**
     * Converts the session url's "file:/... " to file paths.
     * Removes "/../" relative path parts from the url.
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
    
    /**
     * Mini content handler for Corpus files, extracts corpus links.
     * 
     * @author Han Sloetjes
     * @version 1.0
      */
    class CorpusHandler implements ContentHandler {
        /** Holds value of property Description */
        private final String DESC = "Description";

        /** Holds value of property CorpusLink */
        private final String LINK = "CorpusLink";

        /** Holds value of property Corpus */
        private final String CORPUS = "Corpus";
        private String curCorLink;
        private boolean inCorpLink = false;
        private String curContent;

        /**
         * Start of an element, only a few elements are of interest.
         *
         * @param uri 
         * @param localName 
         * @param qName element
         * @param atts attributes
         *
         * @throws SAXException 
         */
        @Override
		public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
            if (CORPUS.equals(qName)) {
                if (atts.getValue("SearchService") != null) {
                    isSearchService = true;
                }
            } else if (DESC.equals(qName)) {
                // needed if we are going to do something with the query/result data 
                // in the description element(s) 
            } else if (LINK.equals(qName)) {
                inCorpLink = true;
            }
        }

        /**
         * the content of the elements, currently only the CorpuLinks are extracted.
         *
         * @param ch content
         * @param start start index
         * @param length num chars
         *
         * @throws SAXException 
         */
        @Override
		public void characters(char[] ch, int start, int length)
            throws SAXException {
            if (inCorpLink) {
                curContent = new String(ch, start, length);
                curCorLink = urlToPath(curContent);
                
                if (curCorLink.startsWith("../") || curCorLink.startsWith("./")) {
                	curCorLink = FileUtility.getAbsolutePath(corpusFilePath, curCorLink);
                }
                if (curCorLink != null) {
                    sessionFiles.add(curCorLink);
                }
            }
        }

        /**
         * End of the element
         *
         * @param uri 
         * @param localName 
         * @param qName element name
         *
         * @throws SAXException
         */
        @Override
		public void endElement(String uri, String localName, String qName)
            throws SAXException {
            if (LINK.equals(qName)) {
                inCorpLink = false;
            }
        }

        /**
         * 
         *
         * @throws SAXException 
         */
        @Override
		public void startDocument() throws SAXException {
            // stub
        }

        /**
         * 
         *
         * @throws SAXException 
         */
        @Override
		public void endDocument() throws SAXException {
            // stub
        }

        /**
         * 
         *
         * @param prefix 
         *
         * @throws SAXException 
         */
        @Override
		public void endPrefixMapping(String prefix) throws SAXException {
            // stub			
        }

        /**
         * 
         *
         * @param ch 
         * @param start 
         * @param length 
         *
         * @throws SAXException 
         */
        @Override
		public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
            // stub		
        }

        /**
         * 
         *
         * @param target 
         * @param data 
         *
         * @throws SAXException 
         */
        @Override
		public void processingInstruction(String target, String data)
            throws SAXException {
            // stub			
        }

        /**
         * 
         *
         * @param locator 
         */
        @Override
		public void setDocumentLocator(Locator locator) {
            // stub			
        }

        /**
         * 
         *
         * @param name 
         *
         * @throws SAXException 
         */
        @Override
		public void skippedEntity(String name) throws SAXException {
            // stub		
        }

        /**
         * 
         *
         * @param prefix 
         * @param uri 
         *
         * @throws SAXException 
         */
        @Override
		public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
            // stub			
        }
    }
}
