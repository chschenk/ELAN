package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.IncludedIn;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import mpi.eudico.server.corpora.clomimpl.type.TimeSubdivision;
import mpi.eudico.server.corpora.lexicon.LexicalEntryFieldIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Parses an eaf file, creating objects of ControlledVocabularies,
 * LinguisticTypes and Tiers only. The rest is skipped.
 *
 * @author Han Sloetjes
 * @version 1.0 jan 2006: reflects EAFv2.2.xsd
 * @version 2.0 jan 2007: reflects EAFv2.4.xsd, attribute "ANNOTATOR" added to 
 * element "TIER"
 * @version 3.0 may 2008: reflects EAFv2.6.xsd, external references (DCR) added 
 * to Linguistic Type and CV entry
 */
public class EAFSkeletonParser {
    /** the sax parser */
    //private final SAXParser saxParser;
    private XMLReader reader;

    /** the currently supported eaf version */
    private final String version = "2.8";
    private String fileName;

    /** stores tiername - tierrecord pairs */
    private final Map<String, TierRecord> tierMap = new HashMap<String, TierRecord>();
    private List<TierImpl> tiers;
    private List<String> tierOrder = new ArrayList<String>();

    /** stores linguistic types records! */
    private final List<LingTypeRecord> lingTypeRecords = new ArrayList<LingTypeRecord>();
    private List<LinguisticType> linguisticTypes;

    /** stores the Locales */
    private final List<Locale> locales = new ArrayList<Locale>();

    /** stores the ControlledVocabulary objects */
    private final List<ControlledVocabulary> cvList = new ArrayList<ControlledVocabulary>();
    /** stores the list of loaded lexicon links */
    private final List<LexiconLink> lexiconLinks = new ArrayList<LexiconLink>();
    
    /** stores the ControlledVocabulary record objects by their ID */
    private final Map<String, CVRecord> controlledVocabularies = new LinkedHashMap<String, CVRecord>();
    /** stores the Lexicon Service objects with the ID as the key */
    private final Map<String, LexiconServiceRecord> lexiconServices = new LinkedHashMap<String, LexiconServiceRecord>();
    /** Map to temporarily store links from lexicon id to linguistic type in order to couple a Lexicon Query Bundle
     to a Linguistic Type */
    private final Map<String, LinguisticType> lexRefs = new HashMap<String, LinguisticType>();
    /** stores external references: maps id -> extref */
	private final Map<String, ExternalReferenceImpl> extReferences = new LinkedHashMap<String, ExternalReferenceImpl>();
	/** maps CV entries to the ext ref id they reference */
	private final Map<CVEntry, String> cvEntryExtRef = new HashMap<CVEntry, String>();
    private final List<LanguageRecord> languages = new ArrayList<LanguageRecord>();
    private final List<LicenseRecord> licenses = new ArrayList<LicenseRecord>();

    private String fileFormat;

    /**
     * Creates a new EAFSkeletonParser instance
     *
     * @param fileName the file to be parsed
     *
     * @throws ParseException any exception that can occur when creating 
     * a parser
     * @throws NullPointerException thrown when the filename is null
     */
    public EAFSkeletonParser(String fileName) throws ParseException {
    	this(fileName, false);
    	
    	/*
    	if (fileName == null) {
            throw new NullPointerException();
        }

        this.fileName = fileName;
        saxParser = new SAXParser();

        try {
            saxParser.setFeature("http://xml.org/sax/features/validation", true);
            saxParser.setFeature("http://apache.org/xml/features/validation/dynamic",
                true);
            saxParser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                "http://www.mpi.nl/tools/elan/EAFv2.6.xsd");
            saxParser.setContentHandler(new EAFSkeletonHandler());
        } catch (SAXNotRecognizedException e) {
            e.printStackTrace();
            throw new ParseException(e.getMessage());
        } catch (SAXNotSupportedException e) {
            e.printStackTrace();
            throw new ParseException(e.getMessage());
        }
    	*/
    }

    /**
     * Creates a new EAFSkeletonParser instance
     *
     * @param fileName the file to be parsed
     *
     * @throws ParseException any exception that can occur when creating 
     * a parser
     * @throws NullPointerException thrown when the filename is null
     */
    public EAFSkeletonParser(String fileName, boolean strict) throws ParseException {
        if (fileName == null) {
            throw new NullPointerException();
        }

        this.fileName = fileName;

    	try {
    		boolean validate = Boolean.parseBoolean(System.getProperty("ELAN.EAF.Validate", "true"));
    		
	        reader = XMLReaderFactory.createXMLReader(
	        	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", true);
	        reader.setFeature("http://xml.org/sax/features/validation", validate);
	        reader.setFeature("http://apache.org/xml/features/validation/schema", true);
	        reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
	        reader.setContentHandler(new EAFSkeletonHandler());
	        // This works to make sure the schema isn't fetched from the web but from here:
        	reader.setEntityResolver(ACMTranscriptionStore.getCurrentEAFResolver());
	        if (strict) {
	        	reader.setErrorHandler(new EAFErrorHandler());
	        }
    	} catch (SAXException se) {
    		se.printStackTrace();
    		throw new ParseException(se.getMessage());
    	}
    }
    /*
       public ArrayList getMediaDescriptors() {
           return null;
       }
     */

    /**
     * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getLinguisticTypes(java.lang.String)
     */
    public List<LinguisticType> getLinguisticTypes() {
        return linguisticTypes;
    }

    /**
     * Returns a list of tier objects.
     *
     * @return a list of tiers
     */
    public List<TierImpl> getTiers() {
        return tiers;
    }

    /**
     * Returns a list of the tiernames in the same order as in the file.
     *  
     * @return a list of the tiernames in the same order as in the .eaf file
     */
    public List<String> getTierOrder() {
    	return tierOrder;
    }
    
    /**
     * Returns a list of CVs.
     *
     * @return a list of Controlled Vocabularies
     */
    public List<ControlledVocabulary> getControlledVocabularies() {
        return cvList;
    }

    /**
     * Returns the current version of the skeleton parser.
     *
     * @return the current version
     */
    public String getVersion() {
        return version;
    }

    /**
	 * @return the fileFormat
	 */
	public String getFileFormat() {
		return fileFormat;
	}

	/**
	 * @param fileFormat the fileFormat to set
	 */
	void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	/**
	 * @return a list of LicenseRecords
	 */
    public List<LicenseRecord> getLicenseRecords() {
    	return licenses;
    }
    
    /**
     * @return a list of LanguageRecords
     */
    public List<LanguageRecord> getLanguageRecords() {
    	return languages;
    }
    
    /**
     * 
     * @return a list of encountered lexicon links
     */
    public List<LexiconLink> getLexiconLinks() {
    	return lexiconLinks;
    }
   
    
    /**
     * Starts the actual parsing.
     *
     * @throws ParseException any parse exception
     */
    public void parse() throws ParseException {
        // init maps and lists
    	FileInputStream fis = null;
        try {
        	
        	String fileName2;       	
        	if (fileName.toLowerCase().startsWith("file:")) {
        		fileName2 = FileUtility.urlToAbsPath(fileName); // remove "file:" etc.
        	} else {
        		fileName2 = fileName;
        	}
        	
        	try {
    			File f = new File(fileName2);
				fis = new FileInputStream(f);	// may throw FileNotFoundException
				InputSource source = new InputSource(fis);
				source.setSystemId(fileName2);
				reader.parse(source);
        	} catch (FileNotFoundException fnfe) {
        		try {
	        		// try as (local) url
					URL localURL = new URL(fileName2);
					InputSource sourceU = new InputSource(localURL.openStream());
					reader.parse(sourceU);
        		} catch (MalformedURLException mue) {
        			// still try as a file first
        			reader.parse(fileName);
        		}
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
     * After parsing create objects from the records; tiers and linguistic
     * types, CV's, CVEntries, Languages and Lexicon services. 
     * Locale objects have already been made.
     */
    private void createObjects() {
    	// convert Lexicon service records to LexiconLinks and LexiconQueryBundles
    	// here is a lot of duplication of code from EAFnnParser and ACMnnTranscriptionStore, should be changed 
    	Map<String, LexiconQueryBundle2> lqBundleMap = new HashMap<String, LexiconQueryBundle2>();
    	Iterator<String> recordIter = lexiconServices.keySet().iterator();
    	while (recordIter.hasNext()) {
    		String recId = recordIter.next();
    		LexiconServiceRecord record = lexiconServices.get(recId);
    		// for bundle
    		LexiconIdentification lexiconIdentification = new LexiconIdentification(
    				record.getLexiconId(), record.getLexiconName());
    		// for lexicon link
    		LexiconLink link = new LexiconLink(record.getName(), record.getType(), 
    				record.getUrl(), null, lexiconIdentification);
    		lexiconLinks.add(link);
    		
    		LexiconQueryBundle2 bundle = new LexiconQueryBundle2(link, 
    				new LexicalEntryFieldIdentification(record.getDatcatId(), record.getDatcatName()));
    		lqBundleMap.put(recId, bundle);//record.getLexiconId(), bundle
    	}
    	
    	// External References have been created while parsing
    	// create Controlled Vocabularies 
    	Map<String, ControlledVocabulary> cvMap = new HashMap<String, ControlledVocabulary>();
    	Iterator<String> cvIter = controlledVocabularies.keySet().iterator();
    	
    	while (cvIter.hasNext()) {
    		ControlledVocabulary cv = null;
    		String cvKey = cvIter.next();
    		CVRecord cvRec = controlledVocabularies.get(cvKey);
    		
    		String extRefId = cvRec.getExtRefId();
    		if (extRefId != null && !extRefId.isEmpty()) {
    			ExternalCV ecv = new ExternalCV(cvRec.getCv_id());
    			ExternalReferenceImpl eri =  extReferences.get(extRefId);
    			if (eri != null) {
    				try {
    					ecv.setExternalRef(eri.clone());
    				} catch (CloneNotSupportedException cnse) {
    					//LOG.severe("Could not set the external reference: " + cnse.getMessage());
    				}
    			}
    			cv = ecv;

    			// external CV entries are not loaded here
    		} else {
    			cv = new ControlledVocabulary(cvRec.getCv_id());
    			cv.setInitMode(true);
    			// load entries
    		}   		
			if (cvRec.getDescription() != null && !cvRec.getDescription().isEmpty()) {
				cv.setDescription(0, cvRec.getDescription());// the source is an old monolingual CV
			}
			List<CVDescriptionRecord> cvDescriptions = cvRec.getDescriptions();
			if (cvDescriptions != null) {
				for (CVDescriptionRecord d : cvDescriptions) {
    				// get long description from <LANGUAGE> tags outside <CONTROLLED_VOCABULARY>
    				String langId = d.getLangRef();
    				String longLanguageId = "";
    				String langLabel = "";
    				if (languages != null) {
    					for (LanguageRecord lr : languages) {
    						if (langId.equals(lr.getId())) {
    							longLanguageId = lr.getDef();
    							langLabel = lr.getLabel();
    							break;
    						}
    					}
    				}
    				int langIndex = cv.addLanguage(langId, longLanguageId, langLabel);
    				langIndex = Math.max(0, langIndex); // "correct" incorrect Ids
    				cv.setDescription(langIndex, d.getDescription());
				}
			}
			if(! (cv instanceof ExternalCV)) {
				CVEntry entry;
				List<CVEntryRecord> entriesInRecord = cvRec.getEntries();
				for (int i = 0; i < entriesInRecord.size(); i++) {
					CVEntryRecord cveRecord = entriesInRecord.get(i);
					entry = new CVEntry(cv);
					if (cveRecord.getId() != null) {
						entry.setId(cveRecord.getId());
					}
					
					if (cveRecord.getValue() != null || cveRecord.getDescription() != null) {
						entry.setDescription(cveRecord.getDescription());
						entry.setValue(cveRecord.getValue());
					} else {
						if (cveRecord.getSubEntries() != null) {
							// A CVEntry without words should not occur, but has been seen in the wild.
							for (CVEntryRecord subRecord : cveRecord.getSubEntries()) {
								String langId = subRecord.getSubEntryLangRef();
								int langIndex = cv.getIndexOfLanguage(langId);
								if (langIndex >= 0) {
    								entry.setDescription(langIndex, subRecord.getDescription());
    								entry.setValue(langIndex, subRecord.getValue());
								} else {
//									LOG.warning("<CVE_VALUE> element with attribute LANG_REF=\"" + langId + 
//											"\" for which there was no <DESCRIPTION>. The element was ignored.");
								}
							}
						}
					}

					// check external reference
					if (extReferences != null
							&& cveRecord.getExtRefId() != null) {
						ExternalReferenceImpl eri = extReferences.get(cveRecord.getExtRefId());
						if (eri != null) {
							try {
								entry.setExternalRef(eri.clone());
							} catch (CloneNotSupportedException cnse) {
//								LOG.severe("Could not set the external reference: "
//										+ cnse.getMessage());
							}
						}
					}
					cv.addEntry(entry);
				}
			}
			cv.setInitMode(false);
			cvList.add(cv);
			cvMap.put(cvKey, cv);
    	}
    	//
        linguisticTypes = new ArrayList<LinguisticType>(lingTypeRecords.size());

        for (int i = 0; i < lingTypeRecords.size(); i++) {
            LingTypeRecord ltr = lingTypeRecords.get(i);

            LinguisticType lt = new LinguisticType(ltr.getLingTypeId());

            boolean timeAlignable = true;

            if (ltr.getTimeAlignable().equals("false")) {
                timeAlignable = false;
            }

            lt.setTimeAlignable(timeAlignable);

            String stereotype = ltr.getStereoType();
            Constraint c = null;

            if (stereotype != null) {
                stereotype = stereotype.replace('_', ' '); // for backwards compatibility

                if (stereotype.equals(
                            Constraint.stereoTypes[Constraint.TIME_SUBDIVISION])) {
                    c = new TimeSubdivision();
                } else if (stereotype.equals(
                            Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION])) {
                    c = new SymbolicSubdivision();
                } else if (stereotype.equals(
                            Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION])) {
                    c = new SymbolicAssociation();
                } else if (stereotype.equals(
                            Constraint.stereoTypes[Constraint.INCLUDED_IN])) {
                    c = new IncludedIn();
                }
            }

            if (c != null) {
                lt.addConstraint(c);
            }

            lt.setControlledVocabularyName(ltr.getControlledVocabulary());
            if (ltr.getLexiconReference() != null) {
            	lt.setLexiconQueryBundle(lqBundleMap.get(ltr.getLexiconReference()));
            }

			// check ext ref (dcr), in Linguistic Type this is a string
			if (ltr.getExtRefId() != null) {
				ExternalReferenceImpl eri = extReferences.get(ltr.getExtRefId());
				if (eri != null) {
					lt.setDataCategory(eri.getValue());
				}
			}
			
            linguisticTypes.add(lt);
        }

        tiers = new ArrayList<TierImpl>(tierMap.size());

        HashMap<TierImpl, String> parentHash = new HashMap<TierImpl, String>();

        Iterator<TierRecord> tierIt = tierMap.values().iterator();
        TierRecord rec;
        TierImpl tier;
        LinguisticType type;
        Locale loc;

        while (tierIt.hasNext()) {
            tier = null;
            type = null;

            rec = tierIt.next();
            tier = new TierImpl(null, rec.getName(), rec.getParticipant(),
                    null, null);

            Iterator<LinguisticType> typeIter = linguisticTypes.iterator();

            while (typeIter.hasNext()) {
                LinguisticType lt = typeIter.next();

                if (lt.getLinguisticTypeName().equals(rec.getLinguisticType())) {
                    type = lt;

                    break;
                }
            }

            if (type == null) {
                // don't add the tier, something's wrong
                continue;
            }

            tier.setLinguisticType(type);

            if (rec.getDefaultLocale() == null) {
                // default, en
                tier.setDefaultLocale(new Locale("en", "", ""));
            } else {
                Iterator<Locale> locIt = locales.iterator();

                while (locIt.hasNext()) {
                    loc = locIt.next();

                    if (loc.getLanguage().equals(rec.getDefaultLocale())) {
                        tier.setDefaultLocale(loc);

                        break;
                    }
                }
            }

            if (rec.getParentTier() != null) {
                parentHash.put(tier, rec.getParentTier());
            }
            
            if (rec.getAnnotator() != null) {
            	tier.setAnnotator(rec.getAnnotator());
            }
            
            tier.setLangRef(rec.getLangRef());
            tier.setExtRef(rec.getExtRef());

            tiers.add(tier);
        }

        // all Tiers are created. Now set all parent tiers
        Iterator<TierImpl> parentIter = parentHash.keySet().iterator();

        while (parentIter.hasNext()) {
            TierImpl t = parentIter.next();
            String parent = parentHash.get(t);

            Iterator<TierImpl> secIt = tiers.iterator();

            while (secIt.hasNext()) {
                TierImpl pt = secIt.next();

                if (pt.getName().equals(parent)) {
                    t.setParentTier(pt);

                    break;
                }
            }
        }
        
        // post-processing of ext_ref's of CV entries
        if (cvEntryExtRef.size() > 0) {
        	for (Entry<CVEntry, String> mapentry : cvEntryExtRef.entrySet()) {
        		CVEntry entry = mapentry.getKey();
        		String erId = mapentry.getValue();
        		
        		ExternalReferenceImpl eri = extReferences.get(erId);
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
     * An error handler for the eaf parser.<br>
     * The exception thrown (by Xerces 2.6.2) contains apart from file name,
     * line and column number, only a description of the problem in it's message.
     * To really deal with a problem a handler would need to parse the message
     * for certain strings (defined in a Xerces resource .properties file) and/or
     * read the file to the specified problem line.
     *
     * @author Han Sloetjes, MPI
     */
    class EAFErrorHandler implements ErrorHandler {

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

    //#######################
    // Content handler
    //#######################
    class EAFSkeletonHandler extends DefaultHandler implements ContentHandler {
        private String content = "";
        private CVEntryRecord currentEntryRecord;
        private CVEntryRecord currentSubEntryRecord;
    	private CVDescriptionRecord cvDescriptionRecord;
    	
        private String currentTierId;
        private String currentCVId;
        private boolean controlledVocabularyIsMultiLanguage;
        
        /**
         * ContentHandler method
         *
         * @param ch the characters
         * @param start start index
         * @param length length
         *
         * @throws SAXException sax exception
         */
        @Override
		public void characters(char[] ch, int start, int length)
            throws SAXException {
            content += new String(ch, start, length);
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String nameSpaceURI, String name,
            String rawName, Attributes attributes) throws SAXException {
            content = "";

            if (name.equals("ANNOTATION_DOCUMENT")) {
                setFileFormat(attributes.getValue("FORMAT"));
            } else if (name.equals("LICENSE")) {		// New for 2.8
                String licenseURL = attributes.getValue("LICENSE_URL");
                LicenseRecord lr = new LicenseRecord();
                lr.setUrl(licenseURL);
                licenses.add(lr);
           } else if (name.equals("TIER")) {
                currentTierId = attributes.getValue("TIER_ID");

                // First check whether this tier already exists, prevent duplicates
                if (!tierMap.containsKey(currentTierId)) {
                    // create a record
                    TierRecord tr = new TierRecord();
                    tr.setName(currentTierId);
                    tierMap.put(currentTierId, tr);
                    tierOrder.add(currentTierId);

                    tr.setParticipant(attributes.getValue("PARTICIPANT"));
                    tr.setAnnotator(attributes.getValue("ANNOTATOR"));
                    tr.setLinguisticType(attributes.getValue(
                            "LINGUISTIC_TYPE_REF"));
                    tr.setDefaultLocale(attributes.getValue("DEFAULT_LOCALE"));
                    tr.setParentTier(attributes.getValue("PARENT_REF"));
                    tr.setExtRef(attributes.getValue("EXT_REF"));
                    tr.setLangRef(attributes.getValue("LANG_REF"));
                }
            } else if (name.equals("LINGUISTIC_TYPE")) {
                LingTypeRecord ltr = new LingTypeRecord();

                ltr.setLingTypeId(attributes.getValue("LINGUISTIC_TYPE_ID"));

                String timeAlignable = "true";

                if ((attributes.getValue("TIME_ALIGNABLE") != null) &&
                        (attributes.getValue("TIME_ALIGNABLE").equals("false"))) {
                    timeAlignable = "false";
                }

                ltr.setTimeAlignable(timeAlignable);

                String stereotype = attributes.getValue("CONSTRAINTS");
                ltr.setStereoType(stereotype);
                
        		if(stereotype != null && stereotype.startsWith("Symbolic")) {
        			ltr.setTimeAlignable("false");
        		}
        		
                ltr.setControlledVocabulary(attributes.getValue(
                        "CONTROLLED_VOCABULARY_REF"));

                ltr.setExtRefId(attributes.getValue("EXT_REF"));
                ltr.setLexiconReference(attributes.getValue("LEXICON_REF"));
                
                lingTypeRecords.add(ltr);
            } 
    		// Load the Lexicon Query Bundle or Lexicon Link
    		else if (name.equals("LEXICON_REF")) {
    			String lexiconSrvcRef = attributes.getValue("LEX_REF_ID");	// Ref of LexiconQueryBundle
    			String lexiconClientName = attributes.getValue("NAME");		// Name of LexiconClientService
    			String lexiconSrvcType = attributes.getValue("TYPE");		// Type of LexiconClientService
    			String lexiconSrvcUrl = attributes.getValue("URL");			// URL of LexiconClientService
    			String lexiconSrvcId = attributes.getValue("LEXICON_ID");	// ID of Lexicon
    			String lexiconSrvcName = attributes.getValue("LEXICON_NAME");	// Name of Lexicon
    			String dataCategory = attributes.getValue("DATCAT_NAME");	// Name of LexicalEntryField
    			String dataCategoryId = attributes.getValue("DATCAT_ID");	// ID of LexicalEntryField
    			
    			LexiconServiceRecord lsr = new LexiconServiceRecord();
    			lsr.setName(lexiconClientName);
    			lsr.setType(lexiconSrvcType);
    			lsr.setUrl(lexiconSrvcUrl);
    			lsr.setLexiconId(lexiconSrvcId);
    			lsr.setLexiconName(lexiconSrvcName);
    			lsr.setDatcatName(dataCategory);
    			lsr.setDatcatId(dataCategoryId);
    			
    			lexiconServices.put(lexiconSrvcRef, lsr);
    		} else if (name.equals("LOCALE")) {
                String langCode = attributes.getValue("LANGUAGE_CODE");
                String countryCode = attributes.getValue("COUNTRY_CODE");

                if (countryCode == null) {
                    countryCode = "";
                }

                String variant = attributes.getValue("VARIANT");

                if (variant == null) {
                    variant = "";
                }

                Locale l = new Locale(langCode, countryCode, variant);
                locales.add(l);
            } else if (name.equals("LANGUAGE")) {					// New in 2.8
    			String id = attributes.getValue("LANG_ID");
    			String def = attributes.getValue("LANG_DEF");
    			String label = attributes.getValue("LANG_LABEL");
    			if (id != null && def != null) {
    				languages.add(new LanguageRecord(id, def, label));
    			}
            } else if (name.equals("CONTROLLED_VOCABULARY")) {
    			currentCVId = attributes.getValue("CV_ID");

    			controlledVocabularyIsMultiLanguage = false;		// initial value
    			CVRecord cv = new CVRecord(currentCVId);

    			String desc = attributes.getValue("DESCRIPTION");
    			if (desc != null) {
    				cv.setDescription(desc);
    			}
    			/* this fails because external references have not been loaded yet
    			// by Micha: if a CV has an external reference
                // it is an external CV
                //currentCV = new ControlledVocabulary(currentCVId);
                String extRefId = attributes.getValue("EXT_REF");
    			if (extRefId != null) {
    				currentCV = new ExternalCV(currentCV);
    				ExternalReferenceImpl eri = extReferences.get(
    						(extRefId));
    				if (eri != null) {
    					try {
    						((ExternalCV) currentCV).setExternalRef(eri.clone());
    					} catch (CloneNotSupportedException cnse) {
    						//LOG.severe("Could not set the external reference: " + cnse.getMessage());
    					}
    				}
    			}
    			//cvList.add(currentCV);
    			*/
    			// if it is an external CV it has an external reference
    			String extRefId = attributes.getValue("EXT_REF");
    			if (extRefId != null) {
    				cv.setExtRefId(extRefId);
    			}
    			
    			controlledVocabularies.put(currentCVId, cv);
            } else if (name.equals("DESCRIPTION")) {			// New in 2.8
            	cvDescriptionRecord = new CVDescriptionRecord();
    			controlledVocabularyIsMultiLanguage = true;

            	String langRef = attributes.getValue("LANG_REF");
    			if (langRef != null) {
    				cvDescriptionRecord.setLangRef(langRef);
    			}
    			controlledVocabularies.get(currentCVId).addDescription(cvDescriptionRecord);
    		} else if (name.equals("CV_ENTRY")) {				// Removed in 2.8
    			assert(!controlledVocabularyIsMultiLanguage);
    			currentEntryRecord = new CVEntryRecord();

    			currentEntryRecord.setDescription(
    				attributes.getValue("DESCRIPTION"));
    			currentEntryRecord.setExtRefId(attributes.getValue("EXT_REF"));
    			currentEntryRecord.setId(attributes.getValue("ID"));

    			controlledVocabularies.get(currentCVId).addEntry(currentEntryRecord);
            } else if (name.equals("CV_ENTRY_ML")) {			// New in 2.8
    			assert(controlledVocabularyIsMultiLanguage);
    			currentEntryRecord = new CVEntryRecord();
    			
    			String cveID = attributes.getValue("CVE_ID");
    			String extRef = attributes.getValue("EXT_REF");
    			
    			currentEntryRecord.setExtRefId(extRef);
    			currentEntryRecord.setId(cveID);
    			
    			controlledVocabularies.get(currentCVId).addEntry(currentEntryRecord);   			
            } else if (name.equals("CVE_VALUE")) {				// New in 2.8
    			assert(controlledVocabularyIsMultiLanguage);
    			String description = attributes.getValue("DESCRIPTION");
    			String langRef = attributes.getValue("LANG_REF");
    			
    			currentSubEntryRecord = new CVEntryRecord();
    			currentSubEntryRecord.setDescription(description);
    			currentSubEntryRecord.setSubEntryLangRef(langRef);
    			
    			currentEntryRecord.addSubEntry(currentSubEntryRecord);          	
            } else if (name.equals("EXTERNAL_REF")) {
            	String value = attributes.getValue("VALUE");
            	String type = attributes.getValue("TYPE");
            	String erId = attributes.getValue("EXT_REF_ID");
        		if (value != null && value.length() > 0) {
        			ExternalReferenceImpl eri = new ExternalReferenceImpl(value, type);
        			extReferences.put(erId, eri);
        		}
            	
            }
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String nameSpaceURI, String name, String rawName)
            throws SAXException {
            if (name.equals("CV_ENTRY")) {
            	currentEntryRecord.setValue(content);
            } else if (name.equals("DESCRIPTION")) {	// New for 2.8
            	if (content.length() > 0 && cvDescriptionRecord != null) {
            		cvDescriptionRecord.setDescription(content);
            	}
            	cvDescriptionRecord = null;
            } else if (name.equals("CVE_VALUE")) {		// New for 2.8
                currentSubEntryRecord.setValue(content);
            } else if (name.equals("LICENSE")) {
            	String licenseText = content;
            	LicenseRecord lr = licenses.get(licenses.size() - 1);
            	lr.setText(licenseText);
            }
        }
    }  
}
