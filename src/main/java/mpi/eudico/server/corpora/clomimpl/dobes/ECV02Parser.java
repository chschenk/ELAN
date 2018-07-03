package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.dobes.EAF28Parser.EAFResolver;
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
 * A (SAX2) Parser for External CV (ECV) compliant XML files.
 * This is version 0.2. It will accept files of version 0.1 too.
 * Old files will typically refer to the EAFv2.7.xsd schema; if they mention the new EAFv2.8.xsd
 * schema, it may not allow the old form and diagnostics will be printed. This is harmless.
 * 
 * @author Micha Hulsbosch
 * @author Olaf Seibert
 * @version jan 2014
 */
public class ECV02Parser {
	/** the sax parser */
    private XMLReader reader;

    /** the url of the external CV */
    private String url;

    /** the expected External ControlledVocabulary objects */
    private List<ExternalCV> cvList;
    /** the External ControlledVocabulary objects that were found but not expected */
    private List<ExternalCV> unexpectedCvList;
    /** stores external references: maps id -> external reference */
	private final Map<String, ExternalReference> extReferences = new HashMap<String, ExternalReference>();
	/** maps CVEntry to the id of the external reference it references */
	private final Map<CVEntry, String> cvEntryExtRef = new HashMap<CVEntry, String>();
    private String currentCVId;
    private ControlledVocabulary currentCV;
    private String currentEntryDesc;
    private String currentEntryExtRef;
    private String content = "";
	//private String author;

	public String currentEntryId;

    /**
     * Creates a ECV02Parser instance
     * 
     * @param url (the url of the external CV)
     * @throws ParseException
     */
    public ECV02Parser(String url) throws ParseException {
    	this(url, false);
    }

	/**
	 * Creates a ECV02Parser instance
	 * 
	 * @param url (the url of the external CV)
	 * @param strict (whether the error handler should be used)
	 */
	public ECV02Parser(String url, boolean strict) throws ParseException {
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
	        // This works to make sure the schema isn't fetched from the web but from here:
	        reader.setEntityResolver(new EAFResolver()); // see http://www.saxproject.org/apidoc/org/xml/sax/EntityResolver.html
	        reader.setContentHandler(new ECV02Handler());
	        if (strict) {
	        	reader.setErrorHandler(new ECV02ErrorHandler());
	        }
		} catch (SAXException se) {
			se.printStackTrace();
			//throw new ParseException(se.getMessage());
		}
	}

	/**
	 * Tries to parse the ECV file using the url.
	 * <p>
	 * You can pass a list of ECVs that you expect to find, and when found, they are modified
	 * and filled with the entries that are found.
	 * Any extra ECVs can be retrieved via {@link #getExtraControlledVocabularies()}.
	 * {@link #getControlledVocabularies()} delivers the list of expected ECVs.
	 * <p>
	 * If you pass null, you simply get all ECVs that are found.
	 * In that case, they are all returned via {@link #getControlledVocabularies()}.
	 * {@link #getExtraControlledVocabularies()} delivers the same list.
	 * 
	 * @throws ParseException
	 */
	public void parse(List<ExternalCV> ecvList) throws ParseException {
		if (ecvList != null) {
			cvList = ecvList;
			unexpectedCvList = null;
		} else {
			cvList = new ArrayList<ExternalCV>();
			unexpectedCvList = cvList;
		}
		
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

	/**
	 * Search if the given ECV name occurs on the list of expected ECVs.
	 * If so, return that ECV.
	 * <br/>
	 * Otherwise, create a new ECV and put it on the 'unexpected' list. 

	 * @param name
	 */
	private ExternalCV findOrCreate(String name) {
        for (int j = 0; j < cvList.size(); j++) {
        	ExternalCV cvFromList = cvList.get(j);
     
        	if (name.equals(cvFromList.getName())) {
        		return cvFromList;
        	}
        }
        if (unexpectedCvList == null) {
        	unexpectedCvList = new ArrayList<ExternalCV>();
        }
        ExternalCV ecv = new ExternalCV(name);
        unexpectedCvList.add(ecv);
        return ecv;
	}

	private void createObjects() {
		// post-processing of ext_ref's of CV entries
        if (cvEntryExtRef.size() > 0) {
        	for (Entry<CVEntry, String> mapentry : cvEntryExtRef.entrySet()) {
        		CVEntry entry = mapentry.getKey();
        		String erId = mapentry.getValue();
        		
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
     * @return cvList (List containing CVs)
     */
    public List<ControlledVocabulary> getControlledVocabularies() {
    	return Collections.<ControlledVocabulary>unmodifiableList(cvList);
    }

    /**
     * @return cvList (List containing unexpected CVs)
     */
    public List<ControlledVocabulary> getExtraControlledVocabularies() {
    	return Collections.<ControlledVocabulary>unmodifiableList(unexpectedCvList);
    }

    /**
     * @return extReferences (the external reference)
     */
    public Map<String, ExternalReference> getExternalReferences() {
		return extReferences;
	}

	/** 
     * ECV02ErrorHandler
     * @author Micha Hulsbosch
     *
     */
    
    class ECV02ErrorHandler implements ErrorHandler {

		@Override
		public void error(SAXParseException exception) throws SAXException {
			System.out.println("Error:     " + exception.getMessage());
			// system id is the file path
			System.out.println("System id: " + exception.getSystemId());
			System.out.println("Public id: " + exception.getPublicId());
			System.out.println("Line:      " + exception.getLineNumber());
			System.out.println("Column:    " + exception.getColumnNumber());
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
     * ECV02Handler
     * 
     * The content handler for the SAX parser
     * 
     * @author Micha Hulsbosch
     *
     */
	public class ECV02Handler implements ContentHandler {
		//private Locator locator;
		private ExternalCVEntry currentEntry;
		private boolean controlledVocabularyIsMultiLanguage;
		private String currentEntryLangRef;
//	    private final Map<String, String> languages = new HashMap<String, String>();
        private List<LanguageRecord> languages = new ArrayList<LanguageRecord>();

       
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
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String nameSpaceURI, String name, String rawName)
        throws SAXException {
            if (name.equals("CV_ENTRY")) {				// Deprecated in EAF 2.8 / ECV 0.2
            	if (!controlledVocabularyIsMultiLanguage) {
	            	CVEntry entry = new ExternalCVEntry(currentCV, content, currentEntryDesc, currentEntryId);
	                currentCV.addEntry(entry);
	                if (currentEntryExtRef != null) {
	                	cvEntryExtRef.put(entry, currentEntryExtRef);
	                }
            	}
            } else if (name.equals("DESCRIPTION")) {	// New for 2.8 / 0.2
            	if (content.length() > 0) {
            		int index = currentCV.getNumberOfLanguages() - 1;
            		currentCV.setDescription(index, content);
            	}
            } else if (name.equals("CVE_VALUE")) {		// New for 2.8 / 0.2
        		int index = currentCV.getIndexOfLanguage(currentEntryLangRef);
        		if (index >= 0) {
	            	currentEntry.setDescription(index, currentEntryDesc);
	                currentEntry.setValue(index, content);
        		}
            }
	    }
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
		 */
		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
		 */
		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException {
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
		 */
		@Override
		public void processingInstruction(String arg0, String arg1)
				throws SAXException {
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
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#startDocument()
		 */
		@Override
		public void startDocument() throws SAXException {
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
            } else if (name.equals("LANGUAGE")) {					// New in 2.8
    			String id = attributes.getValue("LANG_ID");
    			String def = attributes.getValue("LANG_DEF");
    			String label = attributes.getValue("LANG_LABEL");
    			if (id != null && def != null) {
    				languages.add(new LanguageRecord(id, def, label));
    			}
            } else if (name.equals("CONTROLLED_VOCABULARY")) {
            	currentCVId = attributes.getValue("CV_ID");
                currentCV = findOrCreate(currentCVId);
    			controlledVocabularyIsMultiLanguage = false;		// initial value

                String desc = attributes.getValue("DESCRIPTION");	// Deprecated in 2.8
                if (desc != null) {
                    currentCV.setDescription(desc);
                }
    			controlledVocabularyIsMultiLanguage = false;		// initial value
  
                // To be discussed whether a CV in an ECV can have an EXT_REF
    			// However, at this point we haven't seen the <EXTERNAL_REF> elements yet
    			// so we can't look them up either.
            	String extRefId = attributes.getValue("EXT_REF");
            	if (extRefId != null) {
            		ExternalReference eri = extReferences.get(extRefId);
        			if (eri != null) {
        				try {
        					((ExternalCV) currentCV).setExternalRef(eri.clone());
        				} catch (CloneNotSupportedException cnse) {
        					//LOG.severe("Could not set the external reference: " + cnse.getMessage());
        				}
        			}
            	}
                
            } else if (name.equals("DESCRIPTION")) {				// New in 2.8
    			controlledVocabularyIsMultiLanguage = true;

            	String shortId = attributes.getValue("LANG_REF");
    			if (shortId != null) {
    				for (LanguageRecord lr : languages) {
    					if (shortId.equals(lr.getId())) {
    						String longId = lr.getDef();
    						String label = lr.getLabel();
    	    				int index =	currentCV.addLanguage(shortId, longId, label);
    	    				break;
    					}
    				}
    			}

            } else if (name.equals("CV_ENTRY")) {			// Deprecated in 2.8
                currentEntryDesc = attributes.getValue("DESCRIPTION");
                currentEntryExtRef = attributes.getValue("EXT_REF");
                currentEntryId = attributes.getValue("CVE_ID");
            } else if (name.equals("CV_ENTRY_ML")) {	// New for 2.8
    			assert(controlledVocabularyIsMultiLanguage);
    			currentEntry = new ExternalCVEntry(currentCV);
    			
    			String cveID = attributes.getValue("CVE_ID");
    			String extRefID = attributes.getValue("EXT_REF");
    			
    			currentEntry.setId(cveID);
    			
    			currentCV.addEntry(currentEntry);
                if (extRefID != null && !extRefID.isEmpty()) {
                	cvEntryExtRef.put(currentEntry, extRefID);
                }
            } else if (name.equals("CVE_VALUE")) {		// New for 2.8
    			assert(controlledVocabularyIsMultiLanguage);
    			currentEntryDesc = attributes.getValue("DESCRIPTION");
    			currentEntryLangRef = attributes.getValue("LANG_REF");          	
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
		}
	
	}
}
