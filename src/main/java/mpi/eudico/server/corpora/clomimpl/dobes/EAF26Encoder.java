package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.IoUtil;

import org.w3c.dom.Element;


/**
 * Encodes a Transcription to EAF 2.6 format and saves it.
 *
 * @version Aug 2005 Identity removed
 * @version Feb 2006 Constraint Included In added as well as LinkedFileDescriptors
 * @version Dec 2006 attribute Annotator of Tier added and writing of document 
 * level Properties.
 * @version Nov 2007 added support for attribute RELATIVE_MEDIA_URL of MEDIA_DESCRIPTOR
 *  and RELATIVE_LINK_URL of LINKED_FILE_DESCRIPTOR
 * @version May 2008 added attributes and elements concerning DCR references
 */
public class EAF26Encoder implements AnnotationDocEncoder, ServerLogger {
    /** the version string of the format / dtd */
    public static final String VERSION = "2.6";
    public static boolean debug = false;
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
        EncoderInfo encoderInfo, List tierOrder, String path)
    	   throws IOException{
        Element documentElement = createDOM(theTranscription, tierOrder);
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
        List tierOrder, String path) throws IOException{
        Element documentElement = createTemplateDOM(theTranscription, tierOrder);
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
    public static Element createDOM(Transcription theTranscription,
        List tierOrder) {
    	long beginTime = System.currentTimeMillis();
    	if (debug) {
    		System.out.println("Encoder creating DOM...");
    	}
        Hashtable<String, Element> tierElements = new Hashtable<String, Element>(); // for temporary storage of created tier Elements
        Hashtable<TimeSlot, String> timeSlotIds = new Hashtable<TimeSlot, String>(); // for temporary storage of generated tsIds
        Hashtable<Annotation, String> annotationIds = new Hashtable<Annotation, String>(); // for temporary storage of generated annIds
        // may 2008 create and store ids and elements for external reference objects. 
        // if different references to the same external object are found it is stored only once
        HashMap<String, ExternalReference> extRefIds = new HashMap<String, ExternalReference>();
        ArrayList<String> extRefList = new ArrayList<String>();
        
        Vector<Locale> usedLocales = new Vector<Locale>(); // for storage of used locales

        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        if (attisTr == null) {
            LOG.warning(
                "[[ASSERTION FAILED]] TranscriptionStore/storeTranscription: theTranscription is null");
        }

        /* the media object is now deprecated; MediaDescriptors are used instead
           if (attisTr.getMediaObject() == null) {
               System.out.println(
                   "[[ASSERTION FAILED]] ACM22TranscriptionStore/storeTranscription: theTranscription.getMediaObject() is null");
           }
         */
        EAF26 eafFactory = null;

        try {
            eafFactory = new EAF26();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // ANNOTATION_DOCUMENT
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String dateString = dateFmt.format(Calendar.getInstance().getTime());
        dateString = correctDate(dateString);

        String author = attisTr.getAuthor();

        if (author == null) {
            author = "unspecified";
        }

        Element annotDocument = eafFactory.newAnnotationDocument(dateString,
                author, VERSION);
        eafFactory.appendChild(annotDocument);

        // HEADER
        Element header = eafFactory.newHeader(""); // mediaFile maintained for compat with 1.4.1
        annotDocument.appendChild(header);

        Iterator mdIter = attisTr.getMediaDescriptors().iterator();

        while (mdIter.hasNext()) {
            MediaDescriptor md = (MediaDescriptor) mdIter.next();

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

            header.appendChild(mdElement);
        }

        Iterator lfdIt = attisTr.getLinkedFileDescriptors().iterator();
        LinkedFileDescriptor lfd;

        while (lfdIt.hasNext()) {
            lfd = (LinkedFileDescriptor) lfdIt.next();

            String origin = null;

            if (lfd.timeOrigin != 0) {
                origin = String.valueOf(lfd.timeOrigin);
            }

            Element lfdElement = eafFactory.newLinkedFileDescriptor(lfd.linkURL,
                    lfd.relativeLinkURL, lfd.mimeType, origin, lfd.associatedWith);
            header.appendChild(lfdElement);
        }
        // jan 2007 add document properties
        Property lastUsedAnnIdProp = null;
        List<Property> props = attisTr.getDocProperties();
        if (props.size() > 0) {
        	Property prop;
        	for (int i = 0; i < props.size(); i++) {
        		prop = props.get(i);
        		final Object value = prop.getValue();
				final String name = prop.getName();
				if (name != null || value != null) {
        			// special treatment of last used ann id
        			if ("lastUsedAnnotationId".equals(name)) {
        				if (value != null) {
	        				try {
	        					lastUsedAnnIdProp = prop;
	        				} catch (NumberFormatException nfe) {
	        					System.out.println("Could not retrieve the last used annotation id.");
	        				}
        				}
        				continue;
        			}
        			
        			if (value != null) {
        				header.appendChild(eafFactory.newProperty(
            					name, value.toString())); 				
        			} else {
        				header.appendChild(eafFactory.newProperty(
            					name, null));
        			}
        			
        		}
        	}
        }
        
		// Make sure the property exists (really unlikely that we need to do this)
		if (lastUsedAnnIdProp == null) {
		    lastUsedAnnIdProp = new PropertyImpl("lastUsedAnnotationId", Integer.valueOf(0));
		    attisTr.addDocProperty(lastUsedAnnIdProp);
		}

		if (debug) {
        	System.out.println("Header creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
        }
        // TIME_ORDER
        TimeOrder timeOrder = attisTr.getTimeOrder();

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
        if (debug) {
        	System.out.println("TimeSlots creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
        }
        // TIERS
        //Vector tiers = attisTr.getTiers();
        // HS Nov 2009 always store tiers in the order they are in the transcription, for new files this means
        // creation order
        Vector storeOrder = new Vector(attisTr.getTiers());
        /*
        Vector storeOrder = new Vector(tierOrder); // start with tiers in specified order
        Iterator tIter = tiers.iterator();

        while (tIter.hasNext()) { // add other tiers in document order

            Tier t = (Tier) tIter.next();

            if (!storeOrder.contains(t)) {
                storeOrder.add(t);
            }
        }
		*/
        //int annIndex = 0;
        int extRefIndex = 1;
        int annIndex = (Integer) lastUsedAnnIdProp.getValue();

        Iterator tierIter = storeOrder.iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            String id = t.getName();
            String participant = t.getParticipant();
            String annotator = t.getParticipant();
            String lingType = t.getLinguisticType().getLinguisticTypeName();

            if (lingType == null) {
                lingType = "not specified";
            }

            //Locale lang = (Locale) t.getMetadataValue("DEFAULT_LOCALE");
            Locale lang = t.getDefaultLocale();
            // HS Feb 2010 allow null for Locale, use empty string internally
//            if (lang == null) {
//                lang = new Locale("not specified", "", "");
//            }

            // check is quick solution, TreeSet would do this but compareTo causes ClassCastException
            if (lang != null && !usedLocales.contains(lang)) {
                usedLocales.add(lang);
            }

            String parentName = null;

            if (t.getParentTier() != null) {
                parentName = t.getParentTier().getName();
            }

            Element tierElement = eafFactory.newTier(id, participant, annotator, 
            		lingType, lang, parentName);
            annotDocument.appendChild(tierElement);

            tierElements.put(t.getName(), tierElement); // store for later use

            /*
            List<Annotation> annotations = t.getAnnotations();

            Iterator<Annotation> annotIter = annotations.iterator();
            
            while (annotIter.hasNext()) {
                Annotation ann = (Annotation) annotIter.next();
                annotationIds.put(ann, "a" + annIndex);

                annIndex++;
            }
            */
            /*
            while (annotIter.hasNext()) {
                Annotation ann = (Annotation) annotIter.next();

                //annotation has already an id
                if ((ann.getId() != null) && !ann.getId().isEmpty()) {
                    annotationIds.put(ann, ann.getId());
                } else {
                    //create an id that isn't yet in the transcription
                    do {
                        annIndex++;
                    } while (attisTr.getAnnotation("a" + annIndex) != null);

                    annotationIds.put(ann, "a" + annIndex);
                }
            }
            */
            for (Annotation ann : t.getAnnotations()) {

                //annotation has already an id
                if ((ann.getId() != null) && !ann.getId().equals("")) {
                    annotationIds.put(ann, ann.getId());
                   	annIndex = (Integer)lastUsedAnnIdProp.getValue();
                } else {
                    //create an id that isn't yet in the transcription
                	   annIndex++;
                    annotationIds.put(ann, "a" + annIndex);
                }
            }

        }
        header.appendChild(eafFactory.newProperty("lastUsedAnnotationId", String.valueOf(annIndex)));

        // ANNOTATIONS
        // second pass. Actually creates and adds Annotation Elements
        Iterator tierIter2 = storeOrder.iterator();

        while (tierIter2.hasNext()) {
            TierImpl t = (TierImpl) tierIter2.next();

            for (Annotation ann : t.getAnnotations()) {
                // may 2008 store and add external reference id refs
                String extRefId = null;
                
                if (ann instanceof AbstractAnnotation && ((AbstractAnnotation) ann).getExtRef() != null) {
                	if (!extRefIds.containsValue(((AbstractAnnotation) ann).getExtRef())) {
                		extRefId = "er" + extRefIndex++;
                		extRefIds.put(extRefId, ((AbstractAnnotation) ann).getExtRef());
                		extRefList.add(extRefId);
                	} else {
                		for (int i = 0; i < extRefList.size(); i++) {
                			if (((AbstractAnnotation) ann).getExtRef().equals(extRefIds.get(extRefList.get(i)))) {
                				extRefId = extRefList.get(i);
                				break;
                			}
                		}
                		
                	}
                }
                
                Element annElement = eafFactory.newAnnotation();
                tierElements.get(t.getName()).appendChild(annElement);

                Element annSubElement = null;

                String annId = annotationIds.get(ann);

                if (ann instanceof AlignableAnnotation) {
                    String beginTsId = timeSlotIds.get(((AlignableAnnotation) ann).getBegin());
                    String endTsId = timeSlotIds.get(((AlignableAnnotation) ann).getEnd());
                  

                    annSubElement = eafFactory.newAlignableAnnotation(annId,
                            beginTsId, endTsId, extRefId);
                } else if (ann instanceof RefAnnotation) {
                    String refId = null;
                    String prevId = null;
                    List<Annotation> refs = ((RefAnnotation) ann).getReferences();
                    RefAnnotation prev = ((RefAnnotation) ann).getPrevious();

                    // for the moment, take the first, if it exists
                    if (refs.size() > 0) {
                        refId = annotationIds.get(refs.get(0));
                    }

                    if (prev != null) {
                        prevId = annotationIds.get(prev);
                    }

                    annSubElement = eafFactory.newRefAnnotation(annId, refId,
                            prevId, extRefId);
                }

                annElement.appendChild(annSubElement);

                // ANNOTATION_VALUE
                Element valueElement = eafFactory.newAnnotationValue(ann.getValue());
                annSubElement.appendChild(valueElement);
            }
        }
        if (debug) {
        	System.out.println("Tiers and Annotations creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
        }
        // LINGUISTIC_TYPES
        List<LinguisticType> lTypes = attisTr.getLinguisticTypes();

        if (lTypes != null) {
            Iterator typeIter = lTypes.iterator();

            while (typeIter.hasNext()) {
                // HB, april 24, 2002: for the moment, just store lt name
                LinguisticType lt = (LinguisticType) typeIter.next();
                // may 2008 store and add external reference id ref
                String extRefId = null;
                if (lt.getDataCategory() != null && lt.getDataCategory().length() > 0) {
                	ExternalReferenceImpl eri = new ExternalReferenceImpl(lt.getDataCategory(), ExternalReference.ISO12620_DC_ID);
                	if (!extRefIds.containsValue(eri)) {
                		extRefId = "er" + extRefIndex++;
                		extRefIds.put(extRefId, eri);
                		extRefList.add(extRefId);
                	} else {
                		for (int i = 0; i < extRefList.size(); i++) {
                			if (eri.equals(extRefIds.get(extRefList.get(i)))) {
                				extRefId = extRefList.get(i);
                				break;
                			}
                		}
                	}
                }
                
                String stereotype = null;

                if (lt.hasConstraints()) {
                    stereotype = Constraint.stereoTypes[lt.getConstraints()
                                                          .getStereoType()];
                    stereotype = stereotype.replace(' ', '_');
                }

                Element typeElement = eafFactory.newLinguisticType(lt.getLinguisticTypeName(),
                        lt.isTimeAlignable(),
                        stereotype, lt.getControlledVocabularyName(), extRefId);

                annotDocument.appendChild(typeElement);
            }
        }

        if (debug) {
        	System.out.println("Linguistic Types creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
        }
        
        // LOCALES
        Iterator<Locale> locIter = usedLocales.iterator();

        while (locIter.hasNext()) {
            Locale l = locIter.next();
            Element locElement = eafFactory.newLocale(l);
            annotDocument.appendChild(locElement);
        }

        // HB, 18 jul 02: for the moment manually add relevant Constraints
        // CONSTRAINTS
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

        //CONTROLLED VOCABULARIES
        List<ControlledVocabulary> conVocs = attisTr.getControlledVocabularies();

        if (conVocs.size() > 0) {
            ControlledVocabulary cv;
            Element cvElement;
            Element entryElement;
            String extRefId = null;

            for (int i = 0; i < conVocs.size(); i++) {
                cv = conVocs.get(i);
                cvElement = eafFactory.newControlledVocabulary(cv.getName(),
                        cv.getDescription());

                CVEntry[] entries = cv.getEntries();
                
                for (CVEntry entry : entries) {
                    if (entry.getExternalRef() != null) {
                    	if (!extRefIds.containsValue(entry.getExternalRef())) {
                    		extRefId = "er" + extRefIndex++;
                    		extRefIds.put(extRefId, entry.getExternalRef());
                    		extRefList.add(extRefId);
                    	} else {
                    		for (int k = 0; k < extRefList.size(); k++) {
                    			if (entry.getExternalRef().equals(extRefIds.get(extRefList.get(k)))) {
                    				extRefId = extRefList.get(k);
                    				break;
                    			}
                    		}
                    	}
                    } else {
                    	extRefId = null;
                    }
                    entryElement = eafFactory.newCVEntry(entry.getValue(),
                            entry.getDescription(), extRefId);
                    cvElement.appendChild(entryElement);
                }

                annotDocument.appendChild(cvElement);
            }
        }
        if (debug) {
        	System.out.println("Constraints and CV's creation took: " + (System.currentTimeMillis() - beginTime) + " ms");
        	beginTime = System.currentTimeMillis();
        }
        // EXTERNAL REFERENCES
        Element erElement;
        ExternalReferenceImpl eri;
        String id;
        String type = "iso12620";// only supported type so far
        
        for (int i = 0; i < extRefList.size(); i++) {
        	id = extRefList.get(i);
        	eri = (ExternalReferenceImpl) extRefIds.get(id);
        	if (id != null && eri != null) {
        		erElement = eafFactory.newExternalReference(id, type, eri.getValue());
        		annotDocument.appendChild(erElement);
        	}
        }
        
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
    public static Element createTemplateDOM(Transcription theTranscription,
        List tierOrder) {
        Hashtable<String, Element> tierElements = new Hashtable<String, Element>(); // for temporary storage of created tier Elements
                                                  //Hashtable annotationIds = new Hashtable(); // for temporary storage of generated annIds
        HashMap<String, ExternalReference> extRefIds = new HashMap<String, ExternalReference>();
        ArrayList<String> extRefList = new ArrayList<String>();
        int extRefIndex = 1;
        
        Vector<Locale> usedLocales = new Vector<Locale>(); // for storage of used locales

        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;

        if (attisTr == null) {
            LOG.warning(
                "[[ASSERTION FAILED]] TranscriptionStore/storeTranscription: theTranscription is null");
        }

        /* the media object is deprecated
           if (attisTr.getMediaObject() == null) {
               System.out.println(
                   "[[ASSERTION FAILED]] ACM22TranscriptionStore/storeTranscription: theTranscription.getMediaObject() is null");
           }
         */
        EAF26 eafFactory = null;

        try {
            eafFactory = new EAF26();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // ANNOTATION_DOCUMENT
        //SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm z");

        //String dateString = dateFmt.format(Calendar.getInstance().getTime());
        
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String dateString = dateFmt.format(Calendar.getInstance().getTime());
        dateString = correctDate(dateString);

        String author = attisTr.getAuthor();

        //always set author to empty in template
        author = "";

        Element annotDocument = eafFactory.newAnnotationDocument(dateString,
                author, VERSION);
        eafFactory.appendChild(annotDocument);

        // HEADER
        //always set header to empty in template
        //Element header = eafFactory.newHeader(attisTr.getMediaObject().getMediaURL().toString());
        Element header = eafFactory.newHeader("");
        annotDocument.appendChild(header);

        // TIME_ORDER element, empty. To satisfy the xsd. 
        Element timeOrderElement = eafFactory.newTimeOrder();
        annotDocument.appendChild(timeOrderElement);
        
        // TIERS
        //Vector tiers = attisTr.getTiers();
        // HS Nov 2009 always store in the order the tiers are in the transcription
        // for new files this means creation order
        Vector storeOrder = new Vector(attisTr.getTiers());
        /*
        Vector storeOrder = new Vector(tierOrder); // start with tiers in specified order
        Iterator tIter = tiers.iterator();

        while (tIter.hasNext()) { // add other tiers in document order

            Tier t = (Tier) tIter.next();

            if (!storeOrder.contains(t)) {
                storeOrder.add(t);
            }
        }
		*/
        Iterator tierIter = storeOrder.iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            String id = t.getName();
            String participant = t.getParticipant();
            String annotator = t.getParticipant();
            String lingType = t.getLinguisticType().getLinguisticTypeName();

            if (lingType == null) {
                lingType = "not specified";
            }

            //Locale lang = (Locale) t.getMetadataValue("DEFAULT_LOCALE");
            Locale lang = t.getDefaultLocale();
            //  HS Feb 2010 allow null for Locale, use empty string internally
//            if (lang == null) {
//                lang = new Locale("not specified", "", "");
//            }

            // check is quick solution, TreeSet would do this but compareTo causes ClassCastException
            if (lang != null && !usedLocales.contains(lang)) {
                usedLocales.add(lang);
            }

            String parentName = null;

            if (t.getParentTier() != null) {
                parentName = t.getParentTier().getName();
            }

            Element tierElement = eafFactory.newTier(id, participant, annotator,
            		lingType, lang, parentName);
            annotDocument.appendChild(tierElement);

            tierElements.put(t.getName(), tierElement); // store for later use
                                                        /*
               Vector annotations = t.getAnnotations();
               Iterator annotIter = annotations.iterator();
               while (annotIter.hasNext()) {
                   Annotation ann = (Annotation) annotIter.next();
                   annotationIds.put(ann, "a" + annIndex);
                   annIndex++;
               }
             */
        }

        // LINGUISTIC_TYPES
        List<LinguisticType> lTypes = attisTr.getLinguisticTypes();

        if (lTypes != null) {
            Iterator typeIter = lTypes.iterator();

            while (typeIter.hasNext()) {
                // HB, april 24, 2002: for the moment, just store lt name
                LinguisticType lt = (LinguisticType) typeIter.next();
                // may 2008 store and add external reference id ref
                String extRefId = null;
                if (lt.getDataCategory() != null && lt.getDataCategory().length() > 0) {
                	ExternalReferenceImpl eri = new ExternalReferenceImpl(lt.getDataCategory(), ExternalReference.ISO12620_DC_ID);
                	if (!extRefIds.containsValue(eri)) {
                		extRefId = "er" + extRefIndex++;
                		extRefIds.put(extRefId, eri);
                		extRefList.add(extRefId);
                	} else {
                		for (int i = 0; i < extRefList.size(); i++) {
                			if (eri.equals(extRefIds.get(extRefList.get(i)))) {
                				extRefId = extRefList.get(i);
                				break;
                			}
                		}
                	}
                }
                
                String stereotype = null;

                if (lt.hasConstraints()) {
                    stereotype = Constraint.stereoTypes[lt.getConstraints()
                                                          .getStereoType()];
                    stereotype = stereotype.replace(' ', '_');
                }

                Element typeElement = eafFactory.newLinguisticType(lt.getLinguisticTypeName(),
                        lt.isTimeAlignable(),
                        stereotype, lt.getControlledVocabularyName(), extRefId);

                annotDocument.appendChild(typeElement);
            }
        }

        // LOCALES
        Iterator<Locale> locIter = usedLocales.iterator();

        while (locIter.hasNext()) {
            Locale l = locIter.next();
            Element locElement = eafFactory.newLocale(l);
            annotDocument.appendChild(locElement);
        }

        // HB, 18 jul 02: for the moment manually add relevant Constraints
        // CONSTRAINTS
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

        // CONTROLLED VOCABULARIES
        List<ControlledVocabulary> conVocs = attisTr.getControlledVocabularies();

        if (conVocs.size() > 0) {
            ControlledVocabulary cv;
            Element cvElement;
            Element entryElement;
            String extRefId = null;

            for (int i = 0; i < conVocs.size(); i++) {
                cv = conVocs.get(i);
                cvElement = eafFactory.newControlledVocabulary(cv.getName(),
                        cv.getDescription());

                CVEntry[] entries = cv.getEntries();

                for (CVEntry entry : entries) {
                    if (entry.getExternalRef() != null) {
                    	if (!extRefIds.containsValue(entry.getExternalRef())) {
                    		extRefId = "er" + extRefIndex++;
                    		extRefIds.put(extRefId, entry.getExternalRef());
                    		extRefList.add(extRefId);
                    	} else {
                    		for (int k = 0; k < extRefList.size(); k++) {
                    			if (entry.getExternalRef().equals(extRefIds.get(extRefList.get(k)))) {
                    				extRefId = extRefList.get(k);
                    				break;
                    			}
                    		}
                    	}
                    } else {
                    	extRefId = null;
                    }
                    entryElement = eafFactory.newCVEntry(entry.getValue(),
                            entry.getDescription(), extRefId);
                    cvElement.appendChild(entryElement);
                }

                annotDocument.appendChild(cvElement);
            }
        }

        // EXTERNAL REFERENCES
        Element erElement;
        ExternalReferenceImpl eri;
        String id;
        String type = "iso12620";// only supported type so far
        
        for (int i = 0; i < extRefList.size(); i++) {
        	id = extRefList.get(i);
        	eri = (ExternalReferenceImpl) extRefIds.get(id);
        	if (id != null && eri != null) {
        		erElement = eafFactory.newExternalReference(id, type, eri.getValue());
        		annotDocument.appendChild(erElement);
        	}
        }

        
        return eafFactory.getDocumentElement();
    }

    /**
     * Creates a validating date string.
     *
     * @param strIn the date string to correct
     *
     * @return a validating date string
     */
    private static String correctDate(String strIn) {
        String strResult = new String(strIn);

        try {
            int offsetGMT = Calendar.getInstance().getTimeZone().getRawOffset() / (60 * 60 * 1000);

            String strOffset = "+";

            if (offsetGMT < 0) {
                strOffset = "-";
            }

            offsetGMT = Math.abs(offsetGMT);

            if (offsetGMT < 10) {
                strOffset += "0";
            }

            strOffset += (offsetGMT + ":00");

            strResult += strOffset;

            int indexSpace = strResult.indexOf(" ");

            if (indexSpace != -1) {
                String strEnd = strResult.substring(indexSpace + 1);
                strResult = strResult.substring(0, indexSpace);
                strResult += "T";
                strResult += strEnd;
            }

            strResult = strResult.replace('.', '-');
        } catch (Exception ex) {
            return strIn;
        }

        return strResult;
    }

    private static void save(Element documentElement, String path) throws IOException{
        LOG.info(path + " <----XML output\n");

        try {
            // test for errors
            if (("" + documentElement).length() == 0) {
                throw new IOException("Unable to save this file (zero length).");
                /*
                String txt = "Sorry: unable to save this file (zero length).";
                JOptionPane.showMessageDialog(null, txt, txt,
                    JOptionPane.ERROR_MESSAGE);

                //bla
                return;
                */
            }
            long beginTime = System.currentTimeMillis();
            //IoUtil.writeEncodedFile("UTF-8", path, documentElement);
            IoUtil.writeEncodedEAFFile("UTF-8", path, documentElement);
            
            if (debug) {
            	System.out.println("Saving file took: " + 
            			(System.currentTimeMillis() - beginTime) + " ms");	
            }
        } catch (Exception eee) {
            throw new IOException("Unable to save this file: " + eee.getMessage());
            /*
            String txt = "Sorry: unable to save this file. (" +
                eee.getMessage() + ")";
            JOptionPane.showMessageDialog(null, txt, txt,
                JOptionPane.ERROR_MESSAGE);
                */
        }
    }
}
