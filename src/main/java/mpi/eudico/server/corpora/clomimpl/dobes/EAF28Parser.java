package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.reflink.AbstractRefLinkRecord;
import mpi.eudico.server.corpora.clomimpl.reflink.CrossRefLinkRecord;
import mpi.eudico.server.corpora.clomimpl.reflink.GroupRefLinkRecord;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSetRecord;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A (SAX2) Parser for Elan Annotation Format (EAF) compliant XML files.
 *
 * @author Hennie Brugman
 * @author Han Sloetjes
 * @version 1-Dec-2003
 * @version jun 2004 addition of ControlledVocabularies
 * @version sep 2005 the constructor is now public giving up the singleton pattern
 * the path parameter of all getter methods can be removed in the next parser version
 * (replace by a public parse(String path) method)
 * @version Feb 2006 support for LinkedFleDescrptors and for stereotype
 * Included In is added. For compatibility reasons the filename parameter to the getters is maintained.
 * @version Dec 2006 element PROPERTY has been added to the HEADER element, attribute
 * ANNOTATOR has been added to element TIER
 * @version Nov 2007 EAF v2.5, added attribute RELATIVE_MEDIA_URL to MEDIA_DESCRIPTOR and
 * RELATIVE_LINK_URL to LINKED_FILE_DESCRIPTOR
 * @version May 2008 added attributes and elements concerning DCR references
 */
public class EAF28Parser extends Parser {
    private boolean verbose = false;
	private XMLReader reader;

	/** stores tiername - tierrecord pairs */
    private final Map<String, TierRecord> tierMap = new HashMap<String, TierRecord>();

    /** a map with tiername - List with Annotation Records pairs */
    private final Map<String, List<AnnotationRecord>> tiers = new HashMap<String, List<AnnotationRecord>>();

    /** Holds value of property DOCUMENT ME! */
    private final List<String> tierNames = new ArrayList<String>();

    /** Holds value of property DOCUMENT ME! */
    private final List<LingTypeRecord> linguisticTypes = new ArrayList<LingTypeRecord>();

    /** Holds value of property DOCUMENT ME! */
    private final List<Locale> locales = new ArrayList<Locale>();

    /** Holds value of property DOCUMENT ME! */
    private final Map<String, String> timeSlots = new HashMap<String, String>();

    /** stores the ControlledVocabulary objects by their ID */
    private final Map<String, CVRecord> controlledVocabularies = new HashMap<String, CVRecord>();

    /** stores the Lexicon Service objects */
    private final Map<String, LexiconServiceRecord> lexiconServices = new HashMap<String, LexiconServiceRecord>();
    
    private final List<Property> docProperties = new ArrayList<Property>();
    
    private final Map<String, ExternalReferenceImpl> extReferences = new HashMap<String, ExternalReferenceImpl>();
    
    private final List<LanguageRecord> languages = new ArrayList<LanguageRecord>();
    
    private final List<LicenseRecord> licenses = new ArrayList<LicenseRecord>();

    /** stores the time slots orderd by id */
    private final List<String> timeOrder = new ArrayList<String>(); // since a HashMap is not ordered, all time_slot_ids have to be stored in order separately.
    private List<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();
    private List<LinkedFileDescriptor> linkedFileDescriptors = new ArrayList<LinkedFileDescriptor>();
    private String author;
    private String currentTierId;
    private String currentAnnotationId;
    private AnnotationRecord currentAnnRecord;
    private String currentCVId;
    private boolean controlledVocabularyIsMultiLanguage;
    private CVEntryRecord currentEntryRecord;
    private CVEntryRecord currentSubEntryRecord;
	private CVDescriptionRecord cvDescriptionRecord;
    private String content = "";
    private String lastParsed = "";
    private String currentFileName;
    private String currentPropertyName;
    private String fileFormat;
    private RefLinkSetRecord refLinkSet;
    private AbstractRefLinkRecord refLinkRecord;

    /**
     * Constructor, creates a new XMLReader
     *
     */
    public EAF28Parser() {
    	try {
    		boolean validate = Boolean.parseBoolean(System.getProperty("ELAN.EAF.Validate", "true"));
    				
	        reader = XMLReaderFactory.createXMLReader(
	        	"org.apache.xerces.parsers.SAXParser");
	        reader.setFeature("http://xml.org/sax/features/namespaces", true);
	        reader.setFeature("http://xml.org/sax/features/validation", validate);
	        reader.setFeature("http://apache.org/xml/features/validation/schema", validate);
	        reader.setFeature("http://apache.org/xml/features/validation/dynamic", validate);
	        EAFContentHandler handler;
	        reader.setContentHandler(handler = new EAFContentHandler());
	        reader.setErrorHandler(handler);
	        // This works to make sure the schema isn't fetched from the web but from here:
	        reader.setEntityResolver(new EAFResolver()); // see http://www.saxproject.org/apidoc/org/xml/sax/EntityResolver.html

    	} catch (SAXException se) {
    		se.printStackTrace();
    	}
    }

    /**
     * For backward compatibility; not used anymore
     *
     * @param fileName the eaf filename, parameter also for historic reasons
     *
     * @return media file name
     */
    @Override
	public String getMediaFile(String fileName) {
        parse(fileName);

        return null;
    }

    /**
     * Returns the media descriptors
     *
     * @param fileName the eaf filename, parameter also for historic reasons
     *
     * @return the media descriptors
     */
    @Override
	public List<MediaDescriptor> getMediaDescriptors(String fileName) {
        parse(fileName);

        return mediaDescriptors;
    }

    /**
     * Returns the linked file descriptors
     *
     * @param fileName the eaf file name, for historic reasons
     *
     * @return a list of linked file descriptors
     */
    @Override
	public List<LinkedFileDescriptor> getLinkedFileDescriptors(String fileName) {
        parse(fileName);

        return linkedFileDescriptors;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getAuthor(String fileName) {
        parse(fileName);

        return author;
    }

    /**
     * Returns a list of Property(Impl) objects that have been retrieved from the eaf.
     *
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getTranscriptionProperties(java.lang.String)
	 */
	@Override
	public List<Property> getTranscriptionProperties(String fileName) {
		parse(fileName);

		return docProperties;
	}

	/**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<LingTypeRecord> getLinguisticTypes(String fileName) {
        parse(fileName);

        return linguisticTypes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<String> getTimeOrder(String fileName) {
        parse(fileName);

        return timeOrder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Map<String, String> getTimeSlots(String fileName) {
        parse(fileName);

        return timeSlots;
    }

	/**
	 * Returns a Map of CVRecords with the cv ids as keys.<br>
	 *
	 * @param fileName the eaf filename
	 *
	 * @return a Hashtable of ArrayLists with the cv id's as keys
	 */
    @Override
    public Map<String, CVRecord> getControlledVocabularies(String fileName) {
    	parse(fileName);

    	return controlledVocabularies;
    }

    /**
     * Returns a HashMap of ArrayLists with the lexicon names as keys<br/>
     * Each ArrayList can contain
     */
    @Override
	public Map<String, LexiconServiceRecord> getLexiconServices(String fileName) {
    	parse(fileName);
    	
    	return lexiconServices;
    }
    
    /**
     * Returns the names of the Tiers that are present in the Transcription
     * file
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<String> getTierNames(String fileName) {
        parse(fileName);

        return tierNames;
    }

    /**
     * Returns participant attribute of a tier.
     * The tier record is not used in TranscriptionStore yet.
     *
     * @param tierName name of tier
     * @param fileName the eaf
     *
     * @return the participant
     */
    @Override
	public String getParticipantOf(String tierName, String fileName) {
        parse(fileName);

        if (tierMap.get(tierName) != null) {
        	if (tierMap.get(tierName).getParticipant() != null) {
        		return tierMap.get(tierName).getParticipant();
        	}
        }

        return "";
    }

    /**
     * Returns the annotator attribute of a tier.
     * The tier record is not used in TranscriptionStore yet.
     *
     * @param tierName name of tier
     * @param fileName the eaf
     *
     * @return the annotator of the tier
     */
    @Override
	public String getAnnotatorOf(String tierName, String fileName) {
    	parse(fileName);

        if (tierMap.get(tierName) != null) {
        	if (tierMap.get(tierName).getAnnotator() != null) {
        		return tierMap.get(tierName).getAnnotator();
        	}
        }

        return "";
	}


	/**
     * Returns the name of the linguistic type of a tier.
     * The tier record is not used in TranscriptionStore yet.
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return name of the type
     */
    @Override
	public String getLinguisticTypeIDOf(String tierName, String fileName) {

        parse(fileName);

        if (tierMap.get(tierName) != null) {
        	if (tierMap.get(tierName).getLinguisticType() != null) {
        		return tierMap.get(tierName).getLinguisticType();
        	}
        }

        return "";
    }

    /**
     * Returns the Locale object for a tier.
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return the default Locale object
     */
    @Override
	public Locale getDefaultLanguageOf(String tierName, String fileName) {
        parse(fileName);

        Locale resultLoc = null;

        String localeId = null;
        if (tierMap.get(tierName) != null) {
            localeId = tierMap.get(tierName).getDefaultLocale();
        }

        Iterator<Locale> locIter = locales.iterator();

        while (locIter.hasNext()) {
            Locale l = locIter.next();

            if (l.getLanguage().equals(localeId)) {
                resultLoc = l;
            }
        }

        return resultLoc;
    }

    /**
     * Returns the name of the parent tier, if any.
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return the name of the parent tier, or null
     */
    @Override
    public String getParentNameOf(String tierName, String fileName) {
        parse(fileName);

        if (tierMap.get(tierName) != null) {
            return tierMap.get(tierName).getParentTier();
        }

        return null;
    }

    /**
     * Returns EXT_REF attribute of a tier.
     * The tier record is not used in TranscriptionStore yet.
     *
     * @param tierName name of tier
     * @param fileName the eaf
     *
     * @return the EXT_REF
     */
    @Override
	public String getExtRefOf(String tierName, String fileName) {
        parse(fileName);

        if (tierMap.get(tierName) != null) {
        	return tierMap.get(tierName).getExtRef();
        }

        return null;
    }

    /**
     * Returns LANG_REF attribute of a tier.
     * The tier record is not used in TranscriptionStore yet.
     *
     * @param tierName name of tier
     * @param fileName the eaf
     *
     * @return the EXT_REF
     */
    @Override
	public String getLangRefOf(String tierName, String fileName) {
        parse(fileName);

        if (tierMap.get(tierName) != null) {
        	return tierMap.get(tierName).getLangRef();
        }

        return null;
    }

    /**
     * Returns a List with the Annotations for this Tier. Each
     * AnnotationRecord contains begin time, end time and text values
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return List of AnnotationRecord objects for the tier
     */
    @Override
    public List<AnnotationRecord> getAnnotationsOf(String tierName, String fileName) {
        // make sure that the correct file has been parsed
        parse(fileName);

        return tiers.get(tierName);
    }

    @Override
    public Map<String, ExternalReferenceImpl> getExternalReferences (String fileName) {
    	parse(fileName); //historic reasons
    	
    	return extReferences;
    }
    
    @Override
    public List<LanguageRecord> getLanguages(String fileName) {
    	parse(fileName); //historic reasons
    	
    	return languages;
    }
    
    /**
     * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getFileFormat()
     */
    @Override
    public int getFileFormat() {
    	int result = 0;
    	if (fileFormat != null) {
	    	// split string in parts separated by dot.
	    	String parts[] = fileFormat.split("\\.");
	    	result = Integer.valueOf(parts[0]) * 1000 * 1000;
	    	
	    	if (parts.length >= 2) {
	    		result += Integer.valueOf(parts[1]) * 1000;
	        	if (parts.length >= 3) {
	        		result += Integer.valueOf(parts[2]);
	        	}
	    	}
    	}
    	
    	return result;
    }

    @Override
    public List<LicenseRecord> getLicenses(String fileName) {
    	parse(fileName); //historic reasons
    	
    	return licenses;
    }
    
    @Override
    public List<RefLinkSetRecord> getRefLinkSetList(String fileName) {
    	//parse(fileName); //historic reasons
    	if (refLinkSet != null) {
    		List<RefLinkSetRecord> refLinkSetList = new ArrayList<RefLinkSetRecord>(1);
    		refLinkSetList.add(refLinkSet);
    		
    		return refLinkSetList;
    	}
    	return null;
    }
    
    /**
     * Reset data for a fresh parse.
     */
    private void clear() {
        // (re)set everything to null for each parse
        tiers.clear();
        tierNames.clear(); // HB, 2-1-02, to store name IN ORDER
        //tierAttributes.clear();
        linguisticTypes.clear();
        locales.clear();
        timeSlots.clear();
        timeOrder.clear();
        mediaDescriptors.clear();
        linkedFileDescriptors.clear();
        controlledVocabularies.clear();
    }
    
   /**
     * Parses a EAF v2.8 xml file.
     *
     * @param fileName the EAF v2.8 xml file that must be parsed.
     */
    private void parse(String fileName) {
        //long start = System.currentTimeMillis();

        //		System.out.println("Parse : " + fileName);
        //		System.out.println("Free memory : " + Runtime.getRuntime().freeMemory());
        // only parse the same file once
        if (lastParsed.equals(fileName)) {
            return;
        }

        // parse the file
        lastParsed = fileName;
        currentFileName = fileName;

        clear();
        
		// the SAX parser can have difficulties with certain characters in
		// the filepath: instead create an InputSource with FileInputStream for the parser.
        // HS Mar 2007: depending on Xerces version a SAXException or an IOException
        // is thrown in such case.
        File f = new File(fileName);
        
        if (f.exists()) {
        	FileInputStream fis = null;
	        try {
				fis = new FileInputStream(f);
				InputSource source = new InputSource(fis);
				source.setSystemId(fileName);
				reader.parse(source);
	        } catch (SAXException e) {
	            System.out.println("Parsing error: " + e.getMessage());
	        } catch (IOException e) {
	            System.out.println("IO error: " + e.getMessage());
	        } catch (Exception e) {
	            printErrorLocationInfo("Fatal(?) Error! " + e.getMessage() + " " + e.toString());
	        } finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException e) {
				}
	        }
        }

        //long duration = System.currentTimeMillis() - start;

        //	System.out.println("Parsing took " + duration + " milli seconds");
    }

    private void println(String s) {
        if (verbose) {
            System.out.println(s);
        }
    }

    private void printErrorLocationInfo(String message) {
        System.out.println(message);
        System.out.println("Exception for " + currentFileName);
        System.out.println("Tier id " + currentTierId);
        System.out.println("Annotation id " + currentAnnotationId);
    }

    /**
     * EAF 2.8 content handler and error handler.
     */
    class EAFContentHandler extends DefaultHandler
    						implements ContentHandler, ErrorHandler {

        /**
         * ContentHandler method
         *
         * @throws SAXException DOCUMENT ME!
         */
    	@Override
        public void startDocument() throws SAXException {
        }

        /**
         * ContentHandler method
         *
         * @param nameSpaceURI DOCUMENT ME!
         * @param name DOCUMENT ME!
         * @param rawName DOCUMENT ME!
         * @param attributes DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
    	@Override
        public void startElement(String nameSpaceURI, String name,
            String rawName, Attributes attributes) throws SAXException {
            //	System.out.println("startElement called for name:" + name);
            content = "";

            if (name.equals("ANNOTATION_DOCUMENT")) {
                author = attributes.getValue("AUTHOR");
                fileFormat = attributes.getValue("FORMAT");
            } else if (name.equals("LICENSE")) {
                String licenseURL = attributes.getValue("LICENSE_URL");
                LicenseRecord lr = new LicenseRecord();
                lr.setUrl(licenseURL);
                licenses.add(lr);
            } else if (name.equals("HEADER")) {
            } else if (name.equals("MEDIA_DESCRIPTOR")) {
                String mediaURL = attributes.getValue("MEDIA_URL");
                String mimeType = attributes.getValue("MIME_TYPE");

                MediaDescriptor md = new MediaDescriptor(mediaURL, mimeType);

                long timeOrigin = 0;

                if (attributes.getValue("TIME_ORIGIN") != null) {
                    timeOrigin = Long.parseLong(attributes.getValue(
                                "TIME_ORIGIN"));
                    md.timeOrigin = timeOrigin;
                }

                String extractedFrom = "";

                if (attributes.getValue("EXTRACTED_FROM") != null) {
                    extractedFrom = attributes.getValue("EXTRACTED_FROM");
                    md.extractedFrom = extractedFrom;
                }
                // eaf 2.5 addition
                String relURL = attributes.getValue("RELATIVE_MEDIA_URL");
                if (relURL != null) {
                	md.relativeMediaURL = relURL;
                }

                mediaDescriptors.add(md);
            } else if (name.equals("LINKED_FILE_DESCRIPTOR")) {
                String linkURL = attributes.getValue("LINK_URL");
                String mime = attributes.getValue("MIME_TYPE");
                LinkedFileDescriptor lfd = new LinkedFileDescriptor(linkURL, mime);

                if (attributes.getValue("TIME_ORIGIN") != null) {
                    try {
                        long origin = Long.parseLong(attributes.getValue("TIME_ORIGIN"));
                        lfd.timeOrigin = origin;
                    } catch (NumberFormatException nfe) {
                        System.out.println("Could not parse the time origin: " + nfe.getMessage());
                    }
                }

                String assoc = attributes.getValue("ASSOCIATED_WITH");
                if (assoc != null) {
                    lfd.associatedWith = assoc;
                }

                // eaf 2.5 addition
                String relURL = attributes.getValue("RELATIVE_LINK_URL");
                if (relURL != null) {
                	lfd.relativeLinkURL = relURL;
                }

                linkedFileDescriptors.add(lfd);
            } else if (name.equals("PROPERTY")) {
                // transcription properties
            	currentPropertyName = attributes.getValue("NAME");
            } else if (name.equals("TIME_ORDER")) {
                // nothing to be done, tierOrder List already created
            } else if (name.equals("TIME_SLOT")) {
                String timeValue = String.valueOf(TimeSlot.TIME_UNALIGNED);

                if (attributes.getValue("TIME_VALUE") != null) {
                    timeValue = attributes.getValue("TIME_VALUE");
                }

                timeSlots.put(attributes.getValue("TIME_SLOT_ID"), timeValue);
                timeOrder.add(attributes.getValue("TIME_SLOT_ID"));
            } else if (name.equals("TIER")) {
                currentTierId = attributes.getValue("TIER_ID");

                // First check whether this tier already exists
                if (!tiers.containsKey(currentTierId)) {
                    // create a record
                    TierRecord tr = new TierRecord();
                    tr.setName(currentTierId);
                    tierMap.put(currentTierId, tr);

                    tr.setParticipant(attributes.getValue("PARTICIPANT"));
                    tr.setAnnotator(attributes.getValue("ANNOTATOR"));
                    tr.setLinguisticType(attributes.getValue(
                            "LINGUISTIC_TYPE_REF"));
                    tr.setDefaultLocale(attributes.getValue("DEFAULT_LOCALE"));
                    tr.setParentTier(attributes.getValue("PARENT_REF"));
                    tr.setExtRef(attributes.getValue("EXT_REF"));
                    tr.setLangRef(attributes.getValue("LANG_REF"));

                    // create entries in the tiers and tierAttributes HashMaps for annotations and attributes resp.
                    tiers.put(currentTierId, new ArrayList<AnnotationRecord>());

                    tierNames.add(currentTierId);
                }
            } else if (name.equals("ALIGNABLE_ANNOTATION")) {
                currentAnnotationId = attributes.getValue("ANNOTATION_ID");

                // create new "AnnotationRecord" and add to annotations HashMap for current tier
                ////
                currentAnnRecord = new AnnotationRecord();
                currentAnnRecord.setAnnotationId(currentAnnotationId);
                // ignore any attribute SVG_REF, it is not supported any more
			    currentAnnRecord.setAnnotationType(AnnotationRecord.ALIGNABLE);
				currentAnnRecord.setBeginTimeSlotId(attributes.getValue("TIME_SLOT_REF1"));
				currentAnnRecord.setEndTimeSlotId(attributes.getValue("TIME_SLOT_REF2"));
				currentAnnRecord.setExtRefId(attributes.getValue("EXT_REF"));
    			currentAnnRecord.setCvEntryId(attributes.getValue("CVE_REF"));
				
				tiers.get(currentTierId).add(currentAnnRecord);

            } else if (name.equals("REF_ANNOTATION")) {
                currentAnnotationId = attributes.getValue("ANNOTATION_ID");

                // create new "AnnotationRecord" and add to annotations HashMap for current tier
                ////
                 currentAnnRecord = new AnnotationRecord();
                 currentAnnRecord.setAnnotationId(currentAnnotationId);
                 currentAnnRecord.setAnnotationType(AnnotationRecord.REFERENCE);
                 currentAnnRecord.setReferredAnnotId(attributes.getValue("ANNOTATION_REF"));
				if (attributes.getValue("PREVIOUS_ANNOTATION") != null) {
				    currentAnnRecord.setPreviousAnnotId(attributes.getValue("PREVIOUS_ANNOTATION"));
				} else {
				    currentAnnRecord.setPreviousAnnotId("");
				}
				currentAnnRecord.setExtRefId(attributes.getValue("EXT_REF"));
    			currentAnnRecord.setCvEntryId(attributes.getValue("CVE_REF"));
				
				tiers.get(currentTierId).add(currentAnnRecord);

            } else if (name.equals("ANNOTATION_VALUE")) {
            	// No need to do anything here.
    			// currentAnnRecord.setCvEntryId(attributes.getValue("CVE_REF"));
    		} else if (name.equals("LINGUISTIC_TYPE")) {
            	LingTypeRecord ltr = new LingTypeRecord();

				ltr.setLingTypeId(attributes.getValue(
						"LINGUISTIC_TYPE_ID"));

                String timeAlignable = "true";

                if ((attributes.getValue("TIME_ALIGNABLE") != null) &&
                        (attributes.getValue("TIME_ALIGNABLE").equals("false"))) {
                    timeAlignable = "false";
                }

                ltr.setTimeAlignable(timeAlignable);

                // ignore any attribute GRAPHIC_REFERENCES, it is not supported any more
                String stereotype = attributes.getValue("CONSTRAINTS");
                ltr.setStereoType(stereotype);
                
                if(stereotype != null && stereotype.startsWith("Symbolic")) {
                    ltr.setTimeAlignable("false");
                }

				ltr.setControlledVocabulary(
					attributes.getValue("CONTROLLED_VOCABULARY_REF"));
				
				ltr.setLexiconReference(attributes.getValue("LEXICON_REF"));
				
				ltr.setExtRefId(attributes.getValue("EXT_REF"));

                linguisticTypes.add(ltr);
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
    		}

    		else if (name.equals("LOCALE")) {
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

    			String desc = attributes.getValue("DESCRIPTION");	// Deprecated in 2.8
    			if (desc != null) {
    				cv.setDescription(desc);
    			}
    			
    			// by Micha: if it is an external CV it has an external reference
    			String extRefId = attributes.getValue("EXT_REF");
    			if (extRefId != null) {
    				cv.setExtRefId(extRefId);
    			}
    			
    			controlledVocabularies.put(currentCVId, cv);
            } else if (name.equals("DESCRIPTION")) {				// New in 2.8
            	cvDescriptionRecord = new CVDescriptionRecord();
    			controlledVocabularyIsMultiLanguage = true;

            	String langRef = attributes.getValue("LANG_REF");
    			if (langRef != null) {
    				cvDescriptionRecord.setLangRef(langRef);
    			}
    			controlledVocabularies.get(currentCVId).addDescription(cvDescriptionRecord);
    		} else if (name.equals("CV_ENTRY")) {				// Deprecated in 2.8
    			assert(!controlledVocabularyIsMultiLanguage);
    			currentEntryRecord = new CVEntryRecord();

    			currentEntryRecord.setDescription(
    				attributes.getValue("DESCRIPTION"));
    			currentEntryRecord.setExtRefId(attributes.getValue("EXT_REF"));
    			currentEntryRecord.setId(attributes.getValue("ID"));

    			controlledVocabularies.get(currentCVId).addEntry(currentEntryRecord);
            } else if (name.equals("CV_ENTRY_ML")) {	// New for 2.8
    			assert(controlledVocabularyIsMultiLanguage);
    			currentEntryRecord = new CVEntryRecord();
    			
    			String cveID = attributes.getValue("CVE_ID");
    			String extRef = attributes.getValue("EXT_REF");
    			
    			currentEntryRecord.setExtRefId(extRef);
    			currentEntryRecord.setId(cveID);
    			
    			controlledVocabularies.get(currentCVId).addEntry(currentEntryRecord);
            } else if (name.equals("CVE_VALUE")) {		// New for 2.8
    			assert(controlledVocabularyIsMultiLanguage);
    			String description = attributes.getValue("DESCRIPTION");
    			String langRef = attributes.getValue("LANG_REF");
    			
    			currentSubEntryRecord = new CVEntryRecord();
    			currentSubEntryRecord.setDescription(description);
    			currentSubEntryRecord.setSubEntryLangRef(langRef);
    			
    			currentEntryRecord.addSubEntry(currentSubEntryRecord);
            	
    		} else if (name.equals("REF_LINK_SET")) {	// new for 3.0
    			refLinkSet = new RefLinkSetRecord();

    			String linksID = attributes.getValue("LINKS_ID");
    			String extRef = attributes.getValue("EXT_REF");
    			String langRef = attributes.getValue("LANG_REF");
    			String cvRef = attributes.getValue("CV_REF");
    			
    			refLinkSet.setLinksID(linksID);    			
    			refLinkSet.setExtRefID(extRef);
    			refLinkSet.setLangRef(langRef);    			
    			refLinkSet.setCvRef(cvRef);
    		} else if (name.equals("CROSS_REF_LINK")) {	// new for 3.0
    			CrossRefLinkRecord rec = new CrossRefLinkRecord();
    			refLinkSet.getRefLinks().add(rec);
    			refLinkRecord = rec;

    			String refLinkID = attributes.getValue("REF_LINK_ID");
    			String extRef = attributes.getValue("EXT_REF");
    			String langRef = attributes.getValue("LANG_REF");
    			String cveRef = attributes.getValue("CVE_REF");
    			
    			rec.setId(refLinkID);
    			rec.setExtRefID(extRef);
    			rec.setLangRef(langRef);    			
    			rec.setCveRef(cveRef);
    			
    			String ref1 = attributes.getValue("REF1");
    			String ref2 = attributes.getValue("REF2");
    			String dir = attributes.getValue("DIRECTIONALITY");
    			
    			rec.setRef1(ref1);
    			rec.setRef2(ref2);
    			rec.setDirectionality(dir);
    		} else if (name.equals("GROUP_REF_LINK")) {	// new for 3.0
    			GroupRefLinkRecord rec = new GroupRefLinkRecord();
    			refLinkSet.getRefLinks().add(rec);
    			refLinkRecord = rec;

    			String refLinkID = attributes.getValue("REF_LINK_ID");
    			String extRef = attributes.getValue("EXT_REF");
    			String langRef = attributes.getValue("LANG_REF");
    			String cveRef = attributes.getValue("CVE_REF");

    			rec.setId(refLinkID);
    			rec.setExtRefID(extRef);
    			rec.setLangRef(langRef);    			
    			rec.setCveRef(cveRef);
    			
    			String refs = attributes.getValue("REFS");
    			
    			rec.setRefs(refs);
    		} else if (name.equals("EXTERNAL_REF")) {
    			String value = attributes.getValue("VALUE");
    			String type = attributes.getValue("TYPE");
    			String dcId = attributes.getValue("EXT_REF_ID");
    			if (value != null && value.length() > 0) {
    				ExternalReferenceImpl eri = new ExternalReferenceImpl(value, type);
    				extReferences.put(dcId, eri);
    			}
    		}
        }
         //startElement

        /**
         * ContentHandler method
         *
         * @param nameSpaceURI DOCUMENT ME!
         * @param name DOCUMENT ME!
         * @param rawName DOCUMENT ME!
         *
         * @throws SAXException DOCUMENT ME!
         */
    	@Override
        public void endElement(String nameSpaceURI, String name, String rawName)
            throws SAXException {
            if (name.equals("ANNOTATION_VALUE")) {
                currentAnnRecord.setValue(content);
            } else if (name.equals("CV_ENTRY")) {
            	currentEntryRecord.setValue(content);
            } else if (name.equals("PROPERTY")) {
            	if (content.length() > 0 && currentPropertyName != null) {
                	PropertyImpl prop = new PropertyImpl(currentPropertyName, content);
                	docProperties.add(prop);
            	}
            } else if (name.equals("DESCRIPTION")) {	// New for 2.8
            	if (content.length() > 0 && cvDescriptionRecord != null) {
            		cvDescriptionRecord.setDescription(content);
            	}
            	cvDescriptionRecord = null;
            } else if (name.equals("CVE_VALUE")) {	// New for 2.8
                currentSubEntryRecord.setValue(content);
            } else if (name.equals("LICENSE")) {	// New for 2.8
            	String licenseText = content;
            	LicenseRecord lr = licenses.get(licenses.size() - 1);
            	lr.setText(licenseText);
    		} else if (name.equals("CROSS_REF_LINK") ||
    				   name.equals("GROUP_REF_LINK")) {	// new for 3.0
    			String c = content.trim();
    			if (!c.isEmpty()) {
    				refLinkRecord.setContent(c);
    			}
            }
        }

    	@Override
        public void characters(char[] ch, int start, int end)
            throws SAXException {
            content += new String(ch, start, end);
        }
        
        /**
         * An error handler for the eaf parser.<br>
         * The exception thrown (by Xerces 2.6.2) contains apart from file name,
         * line and column number, only a description of the problem in it's message.
         * To really deal with a problem a handler would need to parse the message
         * for certain strings (defined in a Xerces resource .properties file) and/or
         * read the file to the specified problem line.
         * Problematic...
         *
         * @author Han Sloetjes, MPI
         */
    	@Override
		public void error(SAXParseException exception) throws SAXException {
			System.out.println("Error:     " + exception.getMessage());
			// system id is the file path
			System.out.println("System id: " + exception.getSystemId());
			System.out.println("Public id: " + exception.getPublicId());
			System.out.println("Line:      " + exception.getLineNumber());
			System.out.println("Column:    " + exception.getColumnNumber());
		}

    	@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			System.out.println("FatalError: " + exception.getMessage());

		}

    	@Override
		public void warning(SAXParseException exception) throws SAXException {
			System.out.println("Warning: " + exception.getMessage());

		}
    }

    /**
     * @see {@link http://www.saxproject.org/apidoc/org/xml/sax/EntityResolver.html}
     * @author olasei
     */
    public static class EAFResolver implements EntityResolver {
        /**
         * Caller must close() the returned InputStreamReader which will close() the stream.
         */
    	@SuppressWarnings("resource")
		@Override
		public InputSource resolveEntity (String publicId, String systemId)
    	{
    		InputStream stream = null;
    		String resource = null;
    		
    		if (systemId.equals(EAF28.EAF28_SCHEMA_LOCATION)) {
    			resource = EAF28.EAF28_SCHEMA_RESOURCE;
    		} else if (systemId.equals(ECV02.ECV_SCHEMA_LOCATION)) {
        		resource = ECV02.ECV_SCHEMA_RESOURCE;
    		} else if (systemId.equals(EAF27.EAF27_SCHEMA_LOCATION)) {
        		resource = EAF27.EAF27_SCHEMA_RESOURCE;
        	} else {
        		// Use a fallback - should be compatible.
        		resource = EAF27.EAF27_SCHEMA_RESOURCE;        		
        	}
    		if (resource != null) {
    			// return a special input source
    			try {
    				stream = this.getClass().getResource(resource).openStream();
        			Reader reader = new InputStreamReader(stream);
        			return new InputSource(reader);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		// use the default behaviour
    		return null;
    	}
    }

}
