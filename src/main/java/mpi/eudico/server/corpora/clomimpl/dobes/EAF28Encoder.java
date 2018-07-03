package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import mpi.eudico.client.annotator.prefs.RecentLanguages;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.RefLink;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.reflink.CrossRefLink;
import mpi.eudico.server.corpora.clomimpl.reflink.GroupRefLink;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSet;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import static mpi.eudico.server.corpora.util.ServerLogger.LOG;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.IoUtil;
import mpi.eudico.util.MutableInt;
import mpi.eudico.util.multilangcv.LangInfo;
import mpi.eudico.util.multilangcv.LanguageCollection;

import org.w3c.dom.Element;

/**
 * Encodes a Transcription to EAF 2.8 format and saves it.
 *
 * @version Aug 2005 Identity removed
 * @version Feb 2006 Constraint Included In added as well as LinkedFileDescriptors
 * @version Dec 2006 attribute Annotator of Tier added and writing of document 
 * level Properties.
 * @version Nov 2007 added support for attribute RELATIVE_MEDIA_URL of MEDIA_DESCRIPTOR
 *  and RELATIVE_LINK_URL of LINKED_FILE_DESCRIPTOR
 * @version May 2008 added attributes and elements concerning DCR references
 * @version Dec 2010 added support for external Controlled Vocabularies and links to 
 * a Lexicon (or Lexicon services)  
 */
public class EAF28Encoder implements AnnotationDocEncoder {
    /** the version string of the format / dtd */
    public String VERSION;
    
    /**
	 * Constructor. Sets the VERSION to 2.8
	 */
	public EAF28Encoder() {
		super();
		VERSION = "2.8";
	}

	/**
     * Creates an EAF 2.8 factory
     * 
     * @return an EAF 2.8 factory
     */
    protected EAFBase getEAFFactory() {
		try {
			return new EAF28();
		} catch (Throwable t) {
			LOG.warning("Could not create an EAF28 factory");
		}
	
    	return null;
    }
    
    /**
     * Creates a DOM and saves.
     *
     * @param theTranscription the Transcription to store
     * @param encoderInfo additional information for encoding
     * @param tierOrder preferred tier ordering; should be removed
     * @param path the output path
     */
    @Override
	public void encodeAndSave(Transcription theTranscription,
        EncoderInfo encoderInfo, List<TierImpl> tierOrder, String path)
    	   throws IOException{
    	Element documentElement = createDOM(theTranscription, tierOrder, path);
        save(documentElement, path);
    }

    /**
     * Saves a template eaf of the Transcription; everything is saved except for
     * the annotations
     *
     * @param theTranscription the Transcription to store
     * @param tierOrder preferred tier ordering; should be removed
     * @param path the output path
     */
    public void encodeAsTemplateAndSave(Transcription theTranscription,
        List<TierImpl> tierOrder, String path) throws IOException{
        Element documentElement = createTemplateDOM(theTranscription, tierOrder, path);
        save(documentElement, path);
    }

    /**
     * Create the DOM tree and returns the document element.
     *
     * @param theTranscription the Transcription to save (not null)
     * @param tierOrder the preferred ordering of the tiers
     *
     * @return the document element
     */
    public Element createDOM(Transcription theTranscription, List<TierImpl> tierOrder, String path) {
    	long beginTime = System.currentTimeMillis();
    	if (LOG.isLoggable(Level.FINE)) {
    		LOG.fine("Encoder start creating DOM");
    	}
        Map<String, Element> tierElements = new HashMap<String, Element>(); // for temporary storage of created tier Elements
        Map<TimeSlot, String> timeSlotIds = new HashMap<TimeSlot, String>(); // for temporary storage of generated tsIds
        // may 2008 create and store ids and elements for external reference objects. 
        // if different references to the same external object are found it is stored only once
        GetExtRefIdParams getExtRefIdParams = new GetExtRefIdParams();
        
        // For saving Lexicon Query Bundles:
        Map<String, LexiconQueryBundle2> lexRefs = new HashMap<String, LexiconQueryBundle2>();
        MutableInt lexRefIndexMut = new MutableInt(1);
        
        List<Locale> usedLocales = new ArrayList<Locale>(); // for storage of used locales

        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        if (attisTr == null) {
        	if (LOG.isLoggable(Level.WARNING)) {
	            LOG.warning(
	                "[[ASSERTION FAILED]] TranscriptionStore/storeTranscription: theTranscription is null");
        	}
        }

        EAFBase eafFactory = getEAFFactory();
        
        // ANNOTATION_DOCUMENT
        Element annotDocument = addAnnotationDocument(eafFactory, attisTr);
        
        // LICENSEs - new in 2.8
        addLicenses(eafFactory, annotDocument, attisTr);

        // HEADER
        Element headerElement = addHeader(eafFactory, annotDocument);

        // MEDIA_DESCRIPTOR (in HEADER)
        addMediaDescriptors(eafFactory, headerElement, attisTr);
      
        // LINKED_FILE_DESCRIPTOR ((in HEADER)
        addLinkedFilesDescriptors(eafFactory, headerElement, attisTr);

        // PROPERTY (in HEADER)
        // jan 2007 add document properties
        Property lastUsedAnnIdProp = addDocProperties(eafFactory, headerElement, attisTr);
        
        // Make sure the property exists (really unlikely that we need to do this)
        if (lastUsedAnnIdProp == null) {
        	lastUsedAnnIdProp = new PropertyImpl("lastUsedAnnotationId", Integer.valueOf(0));
        	attisTr.addDocProperty(lastUsedAnnIdProp);
        }

        if (LOG.isLoggable(Level.FINE)) {
        	LOG.fine(String.format("Header creation took: %d ms", (System.currentTimeMillis() - beginTime)));
        	beginTime = System.currentTimeMillis();
        }
        // TIME_ORDER
        timeSlotIds = addTimeOrderAndSlots(eafFactory, annotDocument, attisTr);
        
        if (LOG.isLoggable(Level.FINE)) {
        	LOG.fine(String.format("TimeSlots creation took: %d ms", (System.currentTimeMillis() - beginTime)));
        }
        // TIERS
        tierElements = addTiers(eafFactory, annotDocument, attisTr, usedLocales);
        
        // ANNOTATIONS       
        addAnnotations(eafFactory, attisTr, tierElements, timeSlotIds, getExtRefIdParams);
        // all annotations have an id now
        headerElement.appendChild(eafFactory.newProperty("lastUsedAnnotationId", 
        		lastUsedAnnIdProp.getValue().toString()));
        
        if (LOG.isLoggable(Level.FINE)) {
        	LOG.fine(String.format("Tiers and Annotations creation took: %d ms", (System.currentTimeMillis() - beginTime)));
        }
        
        // LINGUISTIC_TYPES
        addTypes(eafFactory, annotDocument, attisTr, getExtRefIdParams, lexRefs, lexRefIndexMut);
        
        if (LOG.isLoggable(Level.FINE)) {
        	LOG.fine(String.format("Linguistic Types creation took: %d ms", 
        			(System.currentTimeMillis() - beginTime)));
        }
        // LOCALES
        addLocales(eafFactory, annotDocument, usedLocales);

        // <LANGUAGE>s
        List<ControlledVocabulary> conVocs = attisTr.getControlledVocabularies();
        addLanguages(new ArrayList<Tier>(attisTr.getTiers()), eafFactory, annotDocument, conVocs);

        // CONSTRAINTS
        addConstraints(eafFactory, annotDocument);
       
        // <CONTROLLED_VOCABULARY>s
        addControlledVocabularies(eafFactory, annotDocument, conVocs, getExtRefIdParams);
        
        if (LOG.isLoggable(Level.FINE)) {
        	LOG.fine(String.format("Constraints and CV's creation took: %d ms", 
        			(System.currentTimeMillis() - beginTime)));
        }
        
        // LEXICON SERVICES
        addLexiconRefs(eafFactory, annotDocument, attisTr, lexRefs, lexRefIndexMut);
        
        // REF_LINK_SET
        addReferenceLinks(eafFactory, annotDocument, attisTr, getExtRefIdParams);
        
        // EXTERNAL REFERENCES (arbitrary order)
        addExternalRefs(eafFactory, annotDocument, attisTr, getExtRefIdParams);
        
        return eafFactory.getDocumentElement();
    }

    /**
     * Create the DOM tree containing only elements that need to be stored in
     * the  template; i.e. everything except timeorder, time slots,
     * annotations and media descriptors.
     *
     * @param theTranscription the Transcription to save (not null)
     * @param tierOrder the preferred ordering of the tiers
     *
     * @return the document element
     */
    public Element createTemplateDOM(Transcription theTranscription,
        List<TierImpl> tierOrder, String path) {
        GetExtRefIdParams getExtRefIdParams = new GetExtRefIdParams();
        
        // For saving Lexicon Query Bundles:
        HashMap<String, LexiconQueryBundle2> lexRefs = new HashMap<String, LexiconQueryBundle2>();
        MutableInt lexRefIndexMut = new MutableInt(1);
        
        List<Locale> usedLocales = new ArrayList<Locale>(); // for storage of used locales

        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        if (attisTr == null) {
        	if (LOG.isLoggable(Level.WARNING)) {
        		LOG.warning(
        			"[[ASSERTION FAILED]] TranscriptionStore/storeTranscription: theTranscription is null");
        	}
        }

        EAFBase eafFactory = getEAFFactory();

        // ANNOTATION_DOCUMENT // NOV 2016 author is now stored like in an eaf
        Element annotDocument = addAnnotationDocument(eafFactory, attisTr);
        
        // LICENSEs - new in 2.8
        addLicenses(eafFactory, annotDocument, attisTr);

        // HEADER
        //always set header to empty in template
        addHeader(eafFactory, annotDocument);
        
        // TIME_ORDER element, empty. To satisfy the xsd. 
        Element timeOrderElement = eafFactory.newTimeOrder();
        annotDocument.appendChild(timeOrderElement);
        
        // TIERS
        addTiers(eafFactory, annotDocument, attisTr, usedLocales);
        
        // LINGUISTIC_TYPES
        addTypes(eafFactory, annotDocument, attisTr, getExtRefIdParams, lexRefs, lexRefIndexMut);
        
        // LOCALES
        addLocales(eafFactory, annotDocument, usedLocales);

        // <LANGUAGE>
        // Collect all used languages from the Controlled Vocabularies
        List<ControlledVocabulary> conVocs = attisTr.getControlledVocabularies();
        addLanguages(new ArrayList<Tier>(attisTr.getTiers()), eafFactory, annotDocument, conVocs);
        
        // CONSTRAINTS
        addConstraints(eafFactory, annotDocument);

        //CONTROLLED VOCABULARIES
        addControlledVocabularies(eafFactory, annotDocument, conVocs, getExtRefIdParams);
        
        // LEXICON SERVICES
        addLexiconRefs(eafFactory, annotDocument, attisTr, lexRefs, lexRefIndexMut);

        // EXTERNAL REFERENCES
        addExternalRefs(eafFactory, annotDocument, attisTr, getExtRefIdParams);
        
        return eafFactory.getDocumentElement();
    }
    
//##### per element methods that can be overridden in future versions of the encoder #####//
    /**
     * Create and add the ANNOTATION_DOCUMENT element
     * @param eafFactory 
     * @param transcription
     * 
     * @return the document element (it is not necessary to return the document element, other
     * methods could retrieve it from the EAFFactory, via e.g. {@link EAF28#getDocumentElement()}
     */
    protected Element addAnnotationDocument(EAFBase eafFactory, Transcription transcription) {
        String dateString = getDate();

        String author = transcription.getAuthor();

        if (author == null) {
            author = "unspecified";
        }

        Element annotDocument = eafFactory.newAnnotationDocument(dateString,
                author, VERSION);
        eafFactory.appendChild(annotDocument);
        return annotDocument;
    }
    
    /**
     * Note: instead of passing the document element it could be obtained from the factory by getDocumentElement().
     * 
     * Adds licenses if any. 
     * @param eafFactory the factory, should be EAF28 or higher
     * @param annotDocument
     * @param transcription
     */
    protected void addLicenses (EAFBase eafFactory, Element annotDocument, Transcription transcription) {
    	if (!(eafFactory instanceof EAF28)) {
    		if (LOG.isLoggable(Level.WARNING)) {
    			LOG.warning("Cannot add license information, EAF factory version too low");
    		}
    		return;
    	}
    	EAF28 eaf28Fact = (EAF28) eafFactory;
        List<LicenseRecord> licenses = transcription.getLicenses();
        if (licenses != null) {
            for (LicenseRecord lr : licenses) {
                Element license = eaf28Fact.newLicense(lr);
                annotDocument.appendChild(license);
            }
        }
    }
    
    /**
     * Add and return the header element.
     * 
     * @param eafFactory
     * @param annotDocument
     * @return the HEADER element
     */
    protected Element addHeader(EAFBase eafFactory, Element annotDocument) {
        Element header = eafFactory.newHeader(""); // mediaFile maintained for compat with 1.4.1
        annotDocument.appendChild(header);
        
        return header;
    }
    
    /**
     * Add media descriptors to the header
     * 
     * @param eafFactory
     * @param headerElement
     * @param transcription
     */
    protected void addMediaDescriptors(EAFBase eafFactory, Element headerElement, 
    		Transcription transcription) {
    	Iterator<MediaDescriptor> mdIter = transcription.getMediaDescriptors().iterator();

        while (mdIter.hasNext()) {
            MediaDescriptor md = mdIter.next();

            String origin = null;

            if (md.timeOrigin != 0) {
                origin = String.valueOf(md.timeOrigin);
            }

            String extrFrom = null;

            if ((md.extractedFrom != null) && (md.extractedFrom != "")) {
                extrFrom = md.extractedFrom;
            }

            Element mdElement = eafFactory.newMediaDescriptor(md.mediaURL,
                    md.relativeMediaURL, md.mimeType, origin, extrFrom);

            headerElement.appendChild(mdElement);
        }
    }
    
    /**
     * Add linked file descriptors to the header.
     * @param eafFactory
     * @param headerElement
     * @param transcription
     */
    protected void addLinkedFilesDescriptors(EAFBase eafFactory, Element headerElement, 
    		Transcription transcription) {
    	Iterator<LinkedFileDescriptor> lfdIt = transcription.getLinkedFileDescriptors().iterator();
        LinkedFileDescriptor lfd;

        while (lfdIt.hasNext()) {
            lfd = lfdIt.next();

            String origin = null;

            if (lfd.timeOrigin != 0) {
                origin = String.valueOf(lfd.timeOrigin);
            }

            Element lfdElement = eafFactory.newLinkedFileDescriptor(lfd.linkURL,
                    lfd.relativeLinkURL, lfd.mimeType, origin, lfd.associatedWith);
            headerElement.appendChild(lfdElement);
        }
    }
    
    /**
     * Adds the document properties to the header. 
     * Special treatment for the last used annotation id property, which is returned.
     * @param eafFactory
     * @param headerElement
     * @param transcription
     * @return the "lastUsedAnnotationId" Property, it will be added after the annotations have been added or null
     */
    protected Property addDocProperties(EAFBase eafFactory, Element headerElement, 
    		Transcription transcription) {
    	Property lastUsedAnnIdProp = null;
    	TranscriptionImpl transImpl = (TranscriptionImpl) transcription; // have to cast to TranscriptionImpl
    	List<Property> props = transImpl.getDocProperties();
        if (props.size() > 0) {
        	for (Property prop : props) {
        		final Object value = prop.getValue();
				final String name = prop.getName();
				if (name != null || value != null) {
        			// special treatment of last used ann id
        			if ("lastUsedAnnotationId".equals(name)) {
        				if (value != null) {
	        				try {
	        					lastUsedAnnIdProp = prop;
	        				} catch (ClassCastException nfe) {
	        					if (LOG.isLoggable(Level.INFO)) {
	        						LOG.info("Could not retrieve the last used annotation id: " + nfe.getMessage());
	        					}
	        				}
        				}
        				continue;// will be added later
        			}
        			
        			if (value != null) {
        				headerElement.appendChild(eafFactory.newProperty(
            					name, value.toString())); 				
        			} else {
        				headerElement.appendChild(eafFactory.newProperty(
            					name, null));
        			}       			
        		}
        	}
        }
        
        return lastUsedAnnIdProp;
    }
    
    /**
     * Creates and adds a time order element with time slots.
     * @param eafFactory
     * @param annotDocument
     * @param transcription
     * 
     * @return a map containing time slot objects to id strings
     */
    protected Map<TimeSlot, String> addTimeOrderAndSlots(EAFBase eafFactory, Element annotDocument, 
    		Transcription transcription) {
    	Map<TimeSlot, String> timeSlotIds = new HashMap<TimeSlot, String>();
    	TimeOrder timeOrder = transcription.getTimeOrder();

        // HB, July 19, 2001: cleanup unused TimeSlots first
        timeOrder.pruneTimeSlots();

        Element timeOrderElement = eafFactory.newTimeOrder();
        annotDocument.appendChild(timeOrderElement);

        int index = 1;

        Iterator<TimeSlot> tsElements = timeOrder.iterator();

        while (tsElements.hasNext()) {
            TimeSlot ts = tsElements.next();

            Element tsElement = null;
            String tsId = "ts" + index;

            // store ts with it's id temporarily
            timeSlotIds.put(ts, tsId);

            if (ts.getTime() != TimeSlot.TIME_UNALIGNED) {
                tsElement = eafFactory.newTimeSlot(tsId, ts.getTime());
            } else {
                tsElement = eafFactory.newTimeSlot(tsId);
            }

            timeOrderElement.appendChild(tsElement);

            index++;
        }
        return timeSlotIds;
    }
    
    /**
     * Creates and add tier elements and returns a map of Tier-names to Elements.
     * @param eafFactory requires EAF28 or higher
     * @param annotDocument
     * @param transcription
     * @param usedLocales a non-null List to which Locale objects used by tiers are added
     * 
     * @return a map containing Tier name to Tier Element mapping 
     */
    protected Map<String, Element> addTiers(EAFBase eafFactory, Element annotDocument, 
    		Transcription transcription, List<Locale> usedLocales) {
    	EAF28 eaf28Fact = (EAF28) eafFactory;
    	Map<String, Element> tierElements = new HashMap<String, Element>();
    	// HS Nov 2009 always store tiers in the order they are in the transcription,
    	// for new files this means creation order
    	List<Tier> storeOrder = new ArrayList<Tier>(transcription.getTiers());
        Iterator<Tier> tierIter = storeOrder.iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            String id = t.getName();
            String participant = t.getParticipant();
            String annotator = t.getAnnotator();
            String lingType = t.getLinguisticType().getLinguisticTypeName();

            if (lingType == null) {
                lingType = "not specified";
            }

            Locale lang = t.getDefaultLocale();

            // TreeSet would do this but compareTo causes ClassCastException?
            // Locale does not implement Comparable so a Comparator would have to be provided to the TreeSet
            if (lang != null && !usedLocales.contains(lang)) {
                usedLocales.add(lang);
            }

            String parentName = null;

            if (t.getParentTier() != null) {
                parentName = t.getParentTier().getName();
            }
            
            String extRef = t.getExtRef();		// new in 2.8
            String langRef = t.getLangRef();	// new in 2.8

            Element tierElement = eaf28Fact.newTier(id, participant, annotator, 
            		lingType, lang, parentName, extRef, langRef);
            annotDocument.appendChild(tierElement);

            tierElements.put(t.getName(), tierElement); // store for later use

           // Make sure all annotations have an id, by using getId() on each one.
           // This may change the value of lastUsedAnnIdProp.getValue().
           // moved to addAnnotations()
//           for (Annotation ann : t.getAnnotations()) {
//               @SuppressWarnings("unused")
//			   String unused = ann.getId();     	   
//           }
        }
        
        return tierElements;
    }
    
    /**
     * Adds annotation elements to existing tier elements.
     * @param eafFactory
     * @param transcription
     * @param tierElements the tier elements created earlier
     * @param timeSlotIds the time slot elements created earlier
     * @param getExtRefIdParams external references id's
     */
    protected void addAnnotations(EAFBase eafFactory, Transcription transcription, 
    		Map<String, Element> tierElements, Map<TimeSlot, String> timeSlotIds, 
    		GetExtRefIdParams getExtRefIdParams) {
    	EAF28 eaf28Fact = (EAF28) eafFactory;
    	// second pass. Actually creates and adds Annotation Elements
    	List<Tier> storeOrder = new ArrayList<Tier>(transcription.getTiers());
        Iterator<Tier> tierIter2 = storeOrder.iterator();

        while (tierIter2.hasNext()) {
            TierImpl t = (TierImpl) tierIter2.next();

            for (Annotation ann : t.getAnnotations()) {
                // Make sure all annotations have an id, by using getId() on each one.
                // This may change the value of lastUsedAnnIdProp.getValue().
                @SuppressWarnings("unused")
 			    String unused = ann.getId();  
                // may 2008 store and add external reference id refs
                String extRefId = null;
                
				if (ann instanceof AbstractAnnotation) {
					final List<ExternalReference> extRefs = ((AbstractAnnotation) ann).getExtRefs();
					
					if (extRefs != null) {
	                	for (ExternalReference thisExtRef : extRefs) {
	                		String tmpExtRefId = getExtRefId(getExtRefIdParams, thisExtRef);
	                		if (extRefId != null && !extRefId.isEmpty()) {
	                			extRefId += " " + tmpExtRefId;
	                		} else {
	                			extRefId = tmpExtRefId;
	                		}
						}
	                }
				}
                
                Element annElement = eaf28Fact.newAnnotation();
                tierElements.get(t.getName()).appendChild(annElement);

                Element annSubElement = null;

                String annId = ann.getId();

                if (ann instanceof AlignableAnnotation) {
                    String beginTsId = timeSlotIds.get(((AlignableAnnotation) ann).getBegin());
                    String endTsId = timeSlotIds.get(((AlignableAnnotation) ann).getEnd());
                    // TODO in the following cases an exception should be thrown
                    if (beginTsId == null) {
                    	if (LOG.isLoggable(Level.WARNING)) {
                    		LOG.warning(String.format("The alignable annotation with id \"%s\" has no reference to a begin time slot.", ann.getId()));
                    	}
                    }
                    if (endTsId == null) {
                    	if (LOG.isLoggable(Level.WARNING)) {
                    		LOG.warning(String.format("The alignable annotation with id \"%s\" has no reference to an end time slot.", ann.getId()));
                    	}
                    }

                   annSubElement = eaf28Fact.newAlignableAnnotation(annId,
                            beginTsId, endTsId, extRefId, ann.getCVEntryId());
                } else if (ann instanceof RefAnnotation) {
                    String refId = null;
                    String prevId = null;
                    List<Annotation> refs = ((RefAnnotation) ann).getReferences();
                    RefAnnotation prev = ((RefAnnotation) ann).getPrevious();

                    // for the moment, take the first, if it exists
                    if (refs.size() > 0) {
                        refId = refs.get(0).getId();
                    }

                    if (prev != null) {
                        prevId = prev.getId();
                    }

                    annSubElement = eaf28Fact.newRefAnnotation(annId, refId,
                            prevId, extRefId, ann.getCVEntryId());
                }

                annElement.appendChild(annSubElement);

                // ANNOTATION_VALUE
                Element valueElement;
        		valueElement = eaf28Fact.newAnnotationValue(ann.getValue());

        		annSubElement.appendChild(valueElement);
            }
        }
    }
    
    /**
     * Adds (Linguistic) Type elements to the document
     * @param eafFactory
     * @param annotDocument
     * @param transcription
     * @param getExtRefIdParams
     * @param lexRefs a map of strings to lexicon bundles
     * @param lexRefIndexMut a counter for lexicon references indices
     */
    protected void addTypes(EAFBase eafFactory, Element annotDocument, Transcription transcription, 
    		GetExtRefIdParams getExtRefIdParams, Map<String, LexiconQueryBundle2> lexRefs,
    		MutableInt lexRefIndexMut) {
    	EAF28 eaf28Fact = (EAF28) eafFactory;
        List<LinguisticType> lTypes = transcription.getLinguisticTypes();

        if (lTypes != null) {
            Iterator<LinguisticType> typeIter = lTypes.iterator();

            while (typeIter.hasNext()) {
                // HB, april 24, 2002: for the moment, just store lt name
                LinguisticType lt = typeIter.next();
                // may 2008 store and add external reference id ref
                String extRefId = null;
                if (lt.getDataCategory() != null && lt.getDataCategory().length() > 0) {
                	ExternalReferenceImpl eri = new ExternalReferenceImpl(lt.getDataCategory(), ExternalReference.ISO12620_DC_ID);
                	extRefId = getExtRefId(getExtRefIdParams, eri);
                }
                
                String stereotype = null;

                if (lt.hasConstraints()) {
                    stereotype = Constraint.stereoTypes[lt.getConstraints()
                                                          .getStereoType()];
                    stereotype = stereotype.replace(' ', '_');
                }

                // Create a LEXICON_REF if there is a Lexicon Query Bundle used.
                String lexRef = null;
                LexiconQueryBundle2 queryBundle = lt.getLexiconQueryBundle();
                if(queryBundle != null) {
                	//lexRef = "lr" + lexRefIndex++;
                	lexRef = "lr" + lexRefIndexMut.intValue++;
                	lexRefs.put(lexRef, queryBundle);
                }
                
                Element typeElement = eaf28Fact.newLinguisticType(lt.getLinguisticTypeName(),
                        lt.isTimeAlignable(),
                        stereotype, lt.getControlledVocabularyName(), extRefId, 
                        lexRef);
               
                annotDocument.appendChild(typeElement);
            }
        }
    }
    
    /**
     * Adds the Locale elements based on the Locale references collected from the tiers.
     * @param eafFactory
     * @param annotDocument
     * @param usedLocales
     */
    protected void addLocales(EAFBase eafFactory, Element annotDocument, List<Locale> usedLocales) {
    	Iterator<Locale> locIter = usedLocales.iterator();

        while (locIter.hasNext()) {
            Locale l = locIter.next();
            Element locElement = eafFactory.newLocale(l);
            annotDocument.appendChild(locElement);
        }
    }
    
    /**
     * Common code for the DOM and the TemplateDOM to add the list of &lt;LANGUAGE>s
     * that are used in the collections of CVs and Tiers.
     * 
     * Since the tiers only store the short Ids, we need to find the corresponding
     * long Ids. They are supposed to be in the recent languages list though.
     * If that fails (it shouldn't) we fall back to the full official list.
     * 
     * @param storeOrder
     * @param eafFactory EAF28 or higher
     * @param annotDocument
     * @param conVocs
     */
    protected void addLanguages(List<Tier> storeOrder,
			EAFBase eafFactory, Element annotDocument,
			List<ControlledVocabulary> conVocs) {
    	if (!(eafFactory instanceof EAF28)) {
    		return;
    	}
    	EAF28 eaf28Fact = (EAF28) eafFactory; 
		Map<String, LanguageRecord> defToLang = new HashMap<String, LanguageRecord>();
        Map<String, LanguageRecord> idToLang = new HashMap<String, LanguageRecord>();

        // Get languages from CVs
        for (ControlledVocabulary cv: conVocs) {
        	int nLangs = cv.getNumberOfLanguages();
        	for (int i = 0; i < nLangs; i++) {
        		String id = cv.getLanguageId(i);
        		String def = cv.getLongLanguageId(i);
        		String label = cv.getLanguageLabel(i);
        		final LanguageRecord lr = new LanguageRecord(id, def, label);
				defToLang.put(def, lr);
				idToLang.put(id, lr);
        	}
        }
        
        // Get languages from Tiers; stored only as (short) id.
        // We need to find the full record for the language.
        for (Tier tier : storeOrder) {
        	String id = tier.getLangRef(); // short Id
        	if (id != null && !id.isEmpty()) {
	        	// Try to look up in the languages that were collected already from the CVs
	        	LanguageRecord lr =  idToLang.get(id);
	        	if (lr == null) {
	        		// try to look up long Id just in case
	        		lr = defToLang.get(id);
	        	}
	        	// Otherwise, try the recent languages list
	        	if (lr == null) {
	        		LangInfo li = RecentLanguages.getInstance().getLanguageInfo(id);
	        		if (li != null) {
	        			lr = new LanguageRecord(li.getId(), li.getLongId(), li.getLabel());        			
	        		}
	        	}   
	        	// Otherwise, try the full official languages list
	        	if (lr == null) {
	        		LangInfo li = LanguageCollection.getLanguageInfo(id);
	        		if (li != null) {
	        			lr = new LanguageRecord(li.getId(), li.getLongId(), li.getLabel());        			
	        		}
	        	}   
	        	if (lr != null) {
	        		defToLang.put(lr.getDef(), lr);
					idToLang.put(lr.getId(), lr);
	        	}
        	}
        }

        // Now create <LANGUAGE> elements for all collected languages
        for (LanguageRecord lr : defToLang.values()) {
        	Element languageElement = eaf28Fact.newLanguage(lr.getId(), lr.getDef(), lr.getLabel());
        	annotDocument.appendChild(languageElement);
        }
	}
    
    /**
     * Adds the Constraint elements, these are predefined, sort of constants. 
     * @param eafFactory
     * @param annotDocument
     */
    protected void addConstraints(EAFBase eafFactory, Element annotDocument) {
        Element timeSubdivision = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.TIME_SUBDIVISION].replace(
                	' ', '_'),
        		"Time subdivision of parent annotation's time interval, no time gaps allowed within this interval");
	    Element symbSubdivision = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION].replace(
	                ' ', '_'),
	            "Symbolic subdivision of a parent annotation. Annotations refering to the same parent are ordered");
	    Element symbAssociation = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION].replace(
	                ' ', '_'), "1-1 association with a parent annotation");
	    Element includedIn = eafFactory.newConstraint(Constraint.stereoTypes[Constraint.INCLUDED_IN].replace(
	                ' ', '_'),
	            "Time alignable annotations within the parent annotation's time interval, gaps are allowed");
	
	    annotDocument.appendChild(timeSubdivision);
	    annotDocument.appendChild(symbSubdivision);
	    annotDocument.appendChild(symbAssociation);
	    annotDocument.appendChild(includedIn);
    }

    /**
     * Adds Controlled Vocabulary elements to the document. 
     * @param eafFactory EAF28 or higher
     * @param annotDocument
     * @param conVocs the list of CV's in the transcription
     * @param getExtRefIdParams collected external references 
     */
    protected void addControlledVocabularies(EAFBase eafFactory, Element annotDocument, 
    		List<ControlledVocabulary> conVocs, GetExtRefIdParams getExtRefIdParams) {
    	if (!(eafFactory instanceof EAF28)) {
    		return;// log message?
    	}
    	EAF28 eaf28Fact = (EAF28) eafFactory;
    	for (ControlledVocabulary cv : conVocs) {
            Element cvElement;

            if (cv instanceof ExternalCV) {
                String extRefId = null;
                
    			// If cv is an external CV, save the cv with a EXT_REF and no entries in the EAF
    			// and save an ECV for caching
    			final ExternalReference externalRef = ((ExternalCV) cv).getExternalRef();
               	extRefId = getExtRefId(getExtRefIdParams, externalRef);
    			cvElement = eaf28Fact.newControlledVocabulary(cv.getName(),
    					null/*cv.getDescription()*/, extRefId);
    		} else {
    			cvElement = eafFactory.newControlledVocabulary(cv.getName(),
    					null/*cv.getDescription()*/);
    						
    			// <DESCRIPTION>s inside <CONTROLLED_VOCABULARY>
    			int nLangs = cv.getNumberOfLanguages();
    			
    			for (int i = 0; i < nLangs; i++) {
    				String languageId = cv.getLanguageId(i);
    				String description = cv.getDescription(i);
    				// HS if the descriptions are empty it should be possible to not add an element but currently
    				// the number of languages and the question whether it is a multiple language CV is based on
    				// the DESCRIPTION elements
//    				if (description != null && !description.isEmpty()) {
	    				Element descriptionElement = eaf28Fact.newDescription(languageId, description);
	    				cvElement.appendChild(descriptionElement);
//    				}
    			}
    			
    			// <CV_ENTRY_ML>s

    			for (CVEntry entry : cv) {
                    String extRefId = null;

                    final ExternalReference externalRef = entry.getExternalRef();
                	extRefId = getExtRefId(getExtRefIdParams, externalRef);

    				Element entryElement = eaf28Fact.newCVEntryML(entry.getId(), extRefId);
    				
    				// <CVE_VALUE>s inside <CV_ENTRY_ML>
        			for (int i = 0; i < nLangs; i++) {
        				String languageId = cv.getLanguageId(i);
        				String description = entry.getDescription(i);
        				String value = entry.getValue(i);
        				if (!value.isEmpty() || 
        					(description != null && !description.isEmpty())) {
        					Element valueElement = eaf28Fact.newCVEntryValue(languageId, value, description);            				
        					entryElement.appendChild(valueElement);
        				}
        			}
        			
        			// Check if there were any children attached under the CVEntryML
        			// which is required by the schema.
        			if (!entryElement.hasChildNodes()) {
        				// This should not happen, but insert a dummy anyway.
        				String languageId = cv.getLanguageId(0);
    					Element valueElement = eaf28Fact.newCVEntryValue(languageId, "", "");            				
    					entryElement.appendChild(valueElement);
        			}
    				      				
    				cvElement.appendChild(entryElement);
    			}
    		}
            annotDocument.appendChild(cvElement);
        }
    }
    
    /**
     * Adds Lexicon reference elements to the document.
     * @param eafFactory EAF28 or higher
     * @param annotDocument
     * @param transcription
     * @param lexRefs the collected lexicon bundles
     * @param lexRefIndexMut the lexicon references index counter for the ID
     */
    protected void addLexiconRefs(EAFBase eafFactory, Element annotDocument, Transcription transcription, 
    		Map<String, LexiconQueryBundle2> lexRefs, MutableInt lexRefIndexMut) {
    	EAF28 eaf28Fact = (EAF28) eafFactory;
    	// First save the Lexicon Query Bundles
        Map<String, LexiconLink> savedLexiconLinks = new HashMap<String, LexiconLink>();
        for(String lexRef : lexRefs.keySet()) {
        	LexiconQueryBundle2 queryBundle = lexRefs.get(lexRef);
        	Element lexSrvcElement = eaf28Fact.newLexiconReference(lexRef, queryBundle);
        	savedLexiconLinks.put(queryBundle.getLinkName(), queryBundle.getLink());
        	annotDocument.appendChild(lexSrvcElement);
        }
        
        // Then save all Lexicon Links that were not in a Lexicon Query Bundle
        Map<String, LexiconLink> lexiconLinks = transcription.getLexiconLinks();
        for(String linkName : lexiconLinks.keySet()) {
        	if(!savedLexiconLinks.containsKey(linkName)) {
        		Element lexSrvcElement = eaf28Fact.newLexiconLink("lr" + lexRefIndexMut.intValue++, 
        				lexiconLinks.get(linkName));
        		annotDocument.appendChild(lexSrvcElement);
        	}
        }            	
    }
    
    /**
     * Adds Reference links, links between annotations.
     * 
     * @param eafFactory EAF28 or higher. <REF_LINK_SET> were actually introduced in EAF 3.0.
     * @param annotDocument
     * @param transcription
     * @param getExtRefIdParams the collected external references and ID's
     */
    protected void addReferenceLinks(EAFBase eafFactory, Element annotDocument, Transcription transcription,
    		GetExtRefIdParams getExtRefIdParams) {
    	EAF28 eaf28Fact = (EAF28) eafFactory;
    	List<RefLinkSet> rlSetList = ((TranscriptionImpl) transcription).getRefLinkSets();
    	RefLinkSet rlset = null;
    	if (rlSetList != null && !rlSetList.isEmpty()) {
    		rlset = rlSetList.get(0);
    	}
        if (rlset != null) {
           	String extRefId = getExtRefId(getExtRefIdParams, rlset.getExtRef());
        	Element rlsElement = eaf28Fact.newRefLinkSet(
        			rlset.getLinksID(), extRefId,
        			rlset.getLangRef(), rlset.getCvRef());
        	
        	annotDocument.appendChild(rlsElement);
        	
        	for (RefLink rl : rlset.getRefs()) {
        		Element rlElement = null;
               	extRefId = getExtRefId(getExtRefIdParams, rl.getExtRef());

               	if (rl instanceof CrossRefLink) {
        			CrossRefLink crl = (CrossRefLink)rl;
        			rlElement = eaf28Fact.newCrossRefLink(
        					rl.getId(), extRefId, rl.getLangRef(), rl.getCveRef(), rl.getRefType(), rl.getContent(),
        					crl.getRef1(), crl.getRef2(), crl.getDirectionality());
        		} else if (rl instanceof GroupRefLink) {
        			GroupRefLink grl = (GroupRefLink)rl;
        			rlElement = eaf28Fact.newGroupRefLink(
        					rl.getId(), extRefId, rl.getLangRef(), rl.getCveRef(), rl.getRefType(), rl.getContent(),
        					grl.getRefs());
        		}
        		rlsElement.appendChild(rlElement);
        	}
        }
    }
    
    /**
     * 
     * @param eafFactory EAF 26 or higher (cast to EAF28 as the minimum for this implementation)
     * @param annotDocument
     * @param transcription
     * @param getExtRefIdParams the collected external references and ID's
     */
    protected void addExternalRefs(EAFBase eafFactory, Element annotDocument, Transcription transcription,
    		GetExtRefIdParams getExtRefIdParams) {
    	EAF28 eaf28Fact = (EAF28) eafFactory;// in fact EAF26 or higher
    	// EXTERNAL REFERENCES (arbitrary order)    
        for (Entry<ExternalReference, String> entry : getExtRefIdParams.map.entrySet()) {
            ExternalReference er = entry.getKey();
            String id = entry.getValue();

            if (id != null && er != null) {
            	Element erElement = eaf28Fact.newExternalReference(id, er.getTypeString(), er.getValue());
        		annotDocument.appendChild(erElement);
        	}
        }
    }
    
  //##### end per element methods that can be overridden in future versions of the encoder #####//
    
	/**
	 * A bit of state information for mapping ExternalReferences to ids for use in the
	 * saved file.
	 * <p>
	 * The Map could be a LinkedHashMap, if we care about printing the ExternalReferences
	 * in numerical order.
	 * 
	 * @author olasei
	 */
	protected class GetExtRefIdParams {
		private int extRefIndex;
		Map<ExternalReference, String> map;
		
		GetExtRefIdParams() {
			extRefIndex = 1;
			map = new HashMap<ExternalReference, String>();
		}
	}
	
	/**
	 * Gets an ID for a given ExternalReference. If identical ExternalReferences
	 * are passed more than once, it will produce the same ID every time.
	 * If passed null, it will return null.
	 * 
	 * @param params state object that keeps the context
	 * @param extRef
	 * @return
	 */
	protected String getExtRefId(GetExtRefIdParams params, ExternalReference extRef) {
		if (extRef == null || params == null) {
			return null;
		}
		
		String id = params.map.get(extRef);
		if (id == null) {
			id = "er" + params.extRefIndex++;
			params.map.put(extRef, id);
		}
		return id;
	}
   
    /**
     * Get a date of the form "2014-03-05T15:06:29+01:00".
     * @return
     */
    protected String getDate() {
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        final Calendar calendar = Calendar.getInstance();
		String dateString = dateFmt.format(calendar.getTime());

		return addTimezone(calendar, dateString);
    }
    
    protected String addTimezone(Calendar calendar, String dateString) {
        int GMTOffsetInMinutes = calendar.getTimeZone().getRawOffset() / (60 * 1000);

        char sign = '+';

        if (GMTOffsetInMinutes < 0) {
            sign = '-';
            GMTOffsetInMinutes = -GMTOffsetInMinutes;
        }

        final int hours = GMTOffsetInMinutes / 60;
		final int minutes = GMTOffsetInMinutes % 60;

		String strOffset = String.format("%02d:%02d", hours, minutes);
        
        return dateString + sign + strOffset;
    }
    
    // java 1.8?
    /*
    private String zonedDateTime() {
    	return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
	*/
    protected void save(Element documentElement, String path) throws IOException{
    	if (LOG.isLoggable(Level.INFO)) {
    		LOG.info(path + " <----XML output\n");
    	}

        try {
            // test for errors
            if (("" + documentElement).length() == 0) {
                throw new IOException("Unable to save this file (zero length).");
            }
            long beginTime = System.currentTimeMillis();
            //IoUtil.writeEncodedFile("UTF-8", path, documentElement);
            IoUtil.writeEncodedEAFFile("UTF-8", path, documentElement);
            
            if (LOG.isLoggable(Level.FINE)) {
            	LOG.fine(String.format("Saving file took: %d ms", (System.currentTimeMillis() - beginTime)));
            }
        } catch (Exception eee) {
            throw new IOException("Unable to save this file: " + eee.getMessage());
        }
    }

}
