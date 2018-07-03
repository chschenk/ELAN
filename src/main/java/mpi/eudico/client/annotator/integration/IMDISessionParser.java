package mpi.eudico.client.annotator.integration;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;

import java.util.HashMap;


/**
 * Parses an IMDI session file, only to extract (the first) .eaf file,  (the
 * first) video file and (the first) audio file (url's).  Preliminary
 * implementation...
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
    private HashMap filesMap;

    /**
     * Creates a new IMDISessionParser instance
     */
    public IMDISessionParser() {
        filesMap = new HashMap(3);

        try {
            reader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setContentHandler(new SessionHandler());
        } catch (SAXException se) {
        }
    }

    /**
     * Starts the actual parsing of the imdi file.
     *
     * @param pathToImdiSession the path to the imdi file
     *
     * @throws IOException io exception
     * @throws SAXException parse exceoption
     */
    public void parse(String pathToImdiSession)
        throws IOException, SAXException {
        if (pathToImdiSession == null) {
            return;
        }

        reader.parse(pathToImdiSession);
    }

    /**
     * Returns the HashMap containing the extracted eaf ,video 
     * and or audio file mappings.
     *
     * @return a map containing eaf and media files mappings
     */
    public HashMap getFilesMap() {
    	/*
        Iterator it = filesMap.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            String value = (String) filesMap.get(key);
            System.out.println("K: " + key + " V: " + value);
        }
		*/
        return filesMap;
    }

    /**
     * TEst main method.
     *
     * @param args the args
     */
    public static void main(String[] args) {
        if ((args != null) && (args.length > 0)) {
            try {
                new IMDISessionParser().parse(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*******************************************************************/

    /**
     * the content handler
     */
    class SessionHandler implements ContentHandler {
        /** Holds value of property MediaFile */
        private final String MF = "MediaFile";

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
        private final String VIDEO = "video";

        /** Holds value of property audio */
        private final String AUDIO = "audio";
        private String curResLink;
        private String curContent;
        private String curFormat;
        private String curType;

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
         * @throws SAXException DOparse ex
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
            if (localName.equals(TYPE)) {
                curType = curContent;
            } else if (localName.equals(FORMAT)) {
                curFormat = curContent;
            } else if (localName.equals(LINK)) {
                curResLink = curContent;
            } else if (localName.equals(WRITTEN_RES)) {
                if (curFormat.equalsIgnoreCase(EAF)) {
                    if (!filesMap.containsKey(EAF) && (curResLink != null) &&
                            (curResLink.length() > 0)) {
                        filesMap.put(EAF, curResLink);
                    }
                }

                resetFields();
            } else if (localName.equals(MF)) {
                if (curType.equalsIgnoreCase(VIDEO)) {
                    if (!filesMap.containsKey(VIDEO) && (curResLink != null) &&
                            (curResLink.length() > 0)) {
                        filesMap.put(VIDEO, curResLink);
                    }
                } else if (curType.equalsIgnoreCase(AUDIO)) {
                    if (!filesMap.containsKey(AUDIO) && (curResLink != null) &&
                            (curResLink.length() > 0)) {
                        filesMap.put(AUDIO, curResLink);
                    }
                }

                resetFields();
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
    }
}
