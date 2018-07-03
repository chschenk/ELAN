package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.ExternalCVEntry;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A (SAX2) Parser for External CV (ECV) compliant XML files
 * 
 * @author Micha Hulsbosch
 * @version jul 2010
 */
public class ECV01Parser {
	/** the sax parser */
    private XMLReader reader;

    /** the url of the external CV */
    private String url;

    /** stores the Locales */
//    private final ArrayList locales = new ArrayList();
    
    /** stores the ControlledVocabulary objects */
    private final ArrayList<ControlledVocabulary> cvList = new ArrayList<ControlledVocabulary>();
    /** stores external references */
	private final HashMap<String, ExternalReference> extReferences = new HashMap<String, ExternalReference>();
	private final HashMap<CVEntry, String> cvEntryExtRef = new HashMap<CVEntry, String>();
    private String currentCVId;
    private ControlledVocabulary currentCV;
    private String currentEntryDesc;
    private String currentEntryExtRef;
    private String content = "";
	//private boolean parseError;
	//private String author;

	public String currentEntryId;

    /**
     * Creates a ECV01Parser instance
     * 
     * @param url (the url of the external CV)
     * @throws ParseException
     */
    public ECV01Parser(String url) throws ParseException {
    	this(url, false);
    }

	/**
	 * Creates a ECV01Parser instance
	 * 
	 * @param url (the url of the external CV)
	 * @param strict (whether the error handler should be used)
	 */
	public ECV01Parser(String url, boolean strict) throws ParseException {
		if (url == null) {
	        throw new NullPointerException();
	    }
	
	    this.url = url;
	
		try {
	        reader = XMLReaderFactory.createXMLReader(
	        	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", true);
	        reader.setFeature("http://xml.org/sax/features/validation", true);
	        reader.setFeature("http://apache.org/xml/features/validation/schema", true);
	        reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
	        reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",
	        		this.getClass().getResource(ACMTranscriptionStore.getCurrentEAFSchemaLocal()).openStream());
//	        reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",
//	        		this.getClass().getResource("/mpi/eudico/resources/ECVv0.1.xsd").openStream());//http://www.mpi.nl/tools/elan/EAFv2.7.xsd
	        reader.setContentHandler(new ECV01Handler());
	        if (strict) {
	        	reader.setErrorHandler(new ECV01ErrorHandler());
	        }
		} catch (SAXException se) {
			se.printStackTrace();
			//throw new ParseException(se.getMessage());
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new ParseException(ioe.getMessage());
		}
	}

	/**
	 * Tries to parse the ECV file using the url
	 * 
	 * @throws ParseException
	 */
	public void parse() throws ParseException {
    	FileInputStream fis = null;
        try {
    		if (url.toLowerCase().startsWith("file:")) {
    			String fileName = FileUtility.urlToAbsPath(url); // removes "file:" protocol  			
    			File f = new File(fileName);
    			try {
    				// may throw FileNotFoundException, e.g. in case of local path with white spaces (%20)
					fis = new FileInputStream(f);	 
					InputSource source = new InputSource(fis);
					reader.parse(source);
    			} catch (FileNotFoundException fnfe) {
    				URL localURL = new URL(url);
    				InputSource sourceU = new InputSource(localURL.openStream());
    				reader.parse(sourceU);
    			}
    		} else {        	
    			reader.parse(url);
    		}
            createObjects();
        } catch (SAXException sax) {
            System.out.println("Parsing error: " + sax.getMessage());
            throw new ParseException("Parsing error: " + sax.getMessage(), sax.getCause());
        } catch (IOException ioe) {
            System.out.println("IO error: " + ioe.getMessage());
            throw new ParseException("IO error: " + ioe.getMessage(), ioe.getCause());
        } catch (Exception e) {
        	throw new ParseException(e.getMessage(), e.getCause());
        } finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
        }
    }

	private void createObjects() {
		// post-processing of ext_ref's of CV entries
        if (cvEntryExtRef.size() > 0) {
        	CVEntry entry;
        	String erId;
        	Iterator<CVEntry> entIter = cvEntryExtRef.keySet().iterator();
        	while (entIter.hasNext()) {
        		entry = entIter.next();
        		erId = cvEntryExtRef.get(entry);
        		
        		ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(erId);
        		if (eri != null) {
        			try {
        				entry.setExternalRef(eri.clone());
        			} catch (CloneNotSupportedException cnse) {
        				System.out.println("Could not set the external reference: " + cnse.getMessage());
        			}
        		}
        	}
        	
        }
	}
	
    /**
     * @return cvList (ArrayList containing CVs)
     */
    public ArrayList<ControlledVocabulary> getControlledVocabularies() {
    	return cvList;
    }

    /**
     * @return extReferences (the external reference)
     */
    public Map<String, ExternalReference> getExternalReferences() {
		return extReferences;
	}

	/** 
     * ECV01ErrorHandler
     * @author Micha Hulsbosch
     *
     */  
    class ECV01ErrorHandler implements ErrorHandler {

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
     * ECV01Handler
     * 
     * The content handler for the SAX parser
     * 
     * @author Micha Hulsbosch
     *
     */
	public class ECV01Handler implements ContentHandler {
		//private Locator locator;
        
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int end)
				throws SAXException {
			content += new String(ch, start, end);
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#endDocument()
		 */
		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub
	
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String nameSpaceURI, String name, String rawName)
        throws SAXException {
	        if (name.equals("CV_ENTRY")) {
	        	ExternalCVEntry entry = new ExternalCVEntry(currentCV, content, currentEntryDesc, currentEntryId);
	            currentCV.addEntry(entry);
	            if (currentEntryExtRef != null) {
	            	cvEntryExtRef.put(entry, currentEntryExtRef);
	            }
	        }
	    }
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
		 */
		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
			// TODO Auto-generated method stub
	
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
		 */
		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException {
			// TODO Auto-generated method stub
	
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
		 */
		@Override
		public void processingInstruction(String arg0, String arg1)
				throws SAXException {
			// TODO Auto-generated method stub
	
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
		 */
		@Override
		public void setDocumentLocator(Locator locator) {
			//this.locator = locator;
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
		 */
		@Override
		public void skippedEntity(String arg0) throws SAXException {
			// TODO Auto-generated method stub
	
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#startDocument()
		 */
		@Override
		public void startDocument() throws SAXException {
			//parseError = false;
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String nameSpaceURI, String name,
	            String rawName, Attributes attributes) throws SAXException {
			content = "";
			
			if (name.equals("CV_RESOURCE")) {
				//author = attributes.getValue("AUTHOR");
            } 
//			else if (name.equals("LOCALE")) {
//                String langCode = attributes.getValue("LANGUAGE_CODE");
//                String countryCode = attributes.getValue("COUNTRY_CODE");
//
//                if (countryCode == null) {
//                    countryCode = "";
//                }
//
//                String variant = attributes.getValue("VARIANT");
//
//                if (variant == null) {
//                    variant = "";
//                }
//
//                Locale l = new Locale(langCode, countryCode, variant);
//                locales.add(l);
//            } 
			else if (name.equals("CONTROLLED_VOCABULARY")) {
            	currentCVId = attributes.getValue("CV_ID");
                currentCV = new ExternalCV(currentCVId);

                String desc = attributes.getValue("DESCRIPTION");
                if (desc != null) {
                    currentCV.setDescription(desc);
                }
                
                // To be discussed whether there is both
                // a CV_ID and a NAME
//                String cvName = attributes.getValue("NAME");
//                if (cvName != null) {
//                    currentCV.setName(cvName);
//                }
                
                // To be discussed whether a CV in an ECV can have an EXT_REF
            	String extRefId = attributes.getValue("EXT_REF");
            	if (extRefId != null) {
            		ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(
        					(extRefId));
        			if (eri != null) {
        				try {
        					((ExternalCV) currentCV).setExternalRef(eri.clone());
        				} catch (CloneNotSupportedException cnse) {
        					//LOG.severe("Could not set the external reference: " + cnse.getMessage());
        				}
        			}
            	}
                
                cvList.add(currentCV);
            } else if (name.equals("CV_ENTRY")) {
                currentEntryDesc = attributes.getValue("DESCRIPTION");
                currentEntryExtRef = attributes.getValue("EXT_REF");
                currentEntryId = attributes.getValue("CVE_ID");
            } else if (name.equals("EXTERNAL_REF")) {
    			String value = attributes.getValue("VALUE");
    			String type = attributes.getValue("TYPE");
    			String refId = attributes.getValue("EXT_REF_ID");
    			if (value != null) {
    				ExternalReferenceImpl eri = new ExternalReferenceImpl(value, type);
    				extReferences.put(refId, eri);
    			}
    			
    		}
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
		 */
		@Override
		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException {
			// TODO Auto-generated method stub
	
		}
	
	}
}
