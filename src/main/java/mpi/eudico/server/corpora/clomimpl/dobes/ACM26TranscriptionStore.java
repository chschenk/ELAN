 package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.ParserFactory;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeOrderImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotComparator;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.chat.CHATEncoder;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextDecoderInfo;
import mpi.eudico.server.corpora.clomimpl.flex.FlexDecoderInfo;
import mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxEncoder;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo2;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.IncludedIn;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import mpi.eudico.server.corpora.clomimpl.type.TimeSubdivision;
import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A TranscriptionStore that corresponds to EAF v2.6.<br>
 * Version 2.6 extends v2.5 by adding an attribute EXT_REF to annotations, 
 * linguistic type and CV entries as well as the new element EXTERNAL_REF
 *
 * @see EAF24TranscriptionStore
 * 
 * @author Hennie Brugman
 * @author Han Sloetjes
 * @version jun 2004
 * @version Aug 2005 Identity removed
 * @version Feb 2006 LinkedFileDescriptor and Included_In added
 * @version Dec 2006 Annotator added, document level Property added
 * @version Nov 2007 relative url's for media and linked files added
 * @version May 2008 support for external references (such as ISO DCR) added
 */
public class ACM26TranscriptionStore implements TranscriptionStore, ServerLogger {
    // we want the XML to be saved to a file
    // HS 19-11-2002: "private final" changed to "public" to enable automatic
    // backup (if fileToWirteXMLinto is not null, then the transcription will
    // be written to that file

    /** Holds value of property DOCUMENT ME! */
    public java.io.File fileToWriteXMLinto = null; 
	
	public boolean debug = false;
	// after importing/parsing certain file types symbolic associated 
	// annotations need to be concatenated as a postprocessing step
	private boolean concatenateAfterParse = false;
	
    /**
     * Creates a new ACM26TranscriptionStore instance
     */
    public ACM26TranscriptionStore() {
        super();
		//debug = true;
    }
	
    /**
     * Requests to save the specified Transcription to a file.<br>
     * The path to the file is taken from the Transcription.
     *
     * @param theTranscription the Transcription to save
     * @param encoderInfo additional encoder information 
     * @param tierOrder the preferred ordering of the tiers
     * @param format the document / file format
     */
    @Override
	public void storeTranscription(Transcription theTranscription, EncoderInfo encoderInfo,
        List<TierImpl> tierOrder, int format) throws IOException {
        if (theTranscription instanceof TranscriptionImpl) {
            String pathName = ((TranscriptionImpl) theTranscription).getPathName();

            if (!pathName.substring(pathName.length() - 4, pathName.length() -
                        3).equals(".")) {
                pathName += ".eaf";
            } else { //always give it extension eaf
                pathName = pathName.substring(0, pathName.length() - 3);
                pathName = pathName + "eaf";
            }

            storeTranscriptionIn(theTranscription, encoderInfo, tierOrder, 
                pathName, format);
        }
    }

	/**
	 * Requests to save the specified Transcription to a file.<br>
	 * The path to the file is specified by the given pathName.
	 *
	 * @param theTranscription the Transcription to save
	 * @param tierOrder the preferred ordering of the tiers
	 * @param pathName the path to the file to use for storage
	 */
    @Override
	public void storeTranscription(Transcription theTranscription, EncoderInfo encoderInfo,
        List<TierImpl> tierOrder, String pathName, int format) throws IOException{
        if (theTranscription instanceof TranscriptionImpl) {
            //String pathName = ((DobesTranscription) theTranscription).getPathName();
            if (!pathName.substring(pathName.length() - 4, pathName.length() -
                        3).equals(".")) {
                pathName += ".eaf";
            } else { //always give it extension eaf
                pathName = pathName.substring(0, pathName.length() - 3);
                pathName = pathName + "eaf";
            }

            storeTranscriptionIn(theTranscription, encoderInfo, tierOrder, 
                pathName, format);
        }
    }

    /**
     * Writes to the file specified by given path, unless the field 
     * <code>fileToWriteXMLinto</code> is not null.
     *
     * @param theTranscription the Transcription to save (not null)
     * @param tierOrder the preferred ordering of the tiers
     * @param path the path to the file to use for storage
     */
    @Override
	public void storeTranscriptionIn(Transcription theTranscription, EncoderInfo encoderInfo,
        List<TierImpl> tierOrder, String path, int format) throws IOException{

		switch (format) {
			case TranscriptionStore.EAF:
				if (this.fileToWriteXMLinto != null) {
					path = fileToWriteXMLinto.getAbsolutePath();
				}
								
				new EAF26Encoder().encodeAndSave(theTranscription, null, tierOrder, path);			
				break;
				
			case TranscriptionStore.CHAT:
			
				new CHATEncoder().encodeAndSave(theTranscription, encoderInfo, tierOrder, path);
				break;
				
			case TranscriptionStore.SHOEBOX:
			
				new ShoeboxEncoder(path).encodeAndSave(theTranscription, encoderInfo, tierOrder, path);
			
				break;
				
			default:
				break;
		}
    }



	/**
	 * Creates a template file using the given path, unless the field 
	 * <code>fileToWriteXMLinto</code> is not null.
	 *
	 * @param theTranscription the Transcription to use for the template (not null)
	 * @param tierOrder the preferred ordering of the tiers
	 * @param path the path to the file to use for storage
	 */
    @Override
	public void storeTranscriptionAsTemplateIn(Transcription theTranscription,
        List tierOrder, String path) throws IOException{
        	
        if (this.fileToWriteXMLinto != null) {
        	path = fileToWriteXMLinto.getAbsolutePath();	
        }
        
        new EAF26Encoder().encodeAsTemplateAndSave(theTranscription, tierOrder, path);
    }

    /**
     * Loads the Transcription from an eaf file.
     *
     * @param theTranscription the Transcription to load
     */
    @Override
	public void loadTranscription(Transcription theTranscription) {
        loadTranscription(theTranscription, null);
    }
    
    /**
     * Loads the Transcription from an eaf file.
     *
     * @param theTranscription the transcription to load
     * @param decoderInfo the info object for the decoder or parser
     */
    @Override
	public void loadTranscription(Transcription theTranscription, DecoderInfo decoderInfo) {
        //	System.out.println("EAFTranscriptionStore.loadTranscription called");
        TranscriptionImpl attisTr = (TranscriptionImpl) theTranscription;
        
        String trPathName = attisTr.getPathName();
        String lowerPathName = trPathName.toLowerCase();
		Parser parser = null;
		if (lowerPathName.endsWith("cha")) {
			parser = ParserFactory.getParser(ParserFactory.CHAT);
			//concatenateAfterParse = true;//??
		}
		else if (decoderInfo instanceof DelimitedTextDecoderInfo) {
			parser = ParserFactory.getParser(ParserFactory.CSV);
			parser.setDecoderInfo(decoderInfo);
		}
		else if ((lowerPathName.endsWith("txt") || lowerPathName.endsWith("sht") 
				|| lowerPathName.endsWith("tbt")) && decoderInfo instanceof ToolboxDecoderInfo2) {	// Toolbox
			parser = ParserFactory.getParser(ParserFactory.TOOLBOX);
			parser.setDecoderInfo(decoderInfo);
			concatenateAfterParse = true;//??
		}
		else if (lowerPathName.endsWith("txt") || lowerPathName.endsWith("sht") 
				|| lowerPathName.endsWith("tbt")) {	// Shoebox
			parser = ParserFactory.getParser(ParserFactory.SHOEBOX);
			parser.setDecoderInfo(decoderInfo);
			concatenateAfterParse = true;
		}
		else if (lowerPathName.endsWith("trs")) {	// Transcriber
			parser = ParserFactory.getParser(ParserFactory.TRANSCRIBER);
			parser.setDecoderInfo(decoderInfo);
		}
		else if (decoderInfo instanceof FlexDecoderInfo) {	// FLEx
			parser = ParserFactory.getParser(ParserFactory.FLEX);
			parser.setDecoderInfo(decoderInfo);
		}
		else if (lowerPathName.endsWith("imdi")) {	// CGN
			parser = ParserFactory.getParser(ParserFactory.CGN);
		}
		else {
			parser = ParserFactory.getParser(ParserFactory.EAF26);
		}
		long beginTime = System.currentTimeMillis();
		if (debug) {
			System.out.println("Parsing eaf took: " + (System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();
		}

        // make media descriptors available in transcription
        List<MediaDescriptor> mediaDescriptors = parser.getMediaDescriptors(trPathName);
        attisTr.setMediaDescriptors(new Vector<MediaDescriptor>(mediaDescriptors));

        // add linked file descriptors
        List<LinkedFileDescriptor> linkedFileDescriptors = parser.getLinkedFileDescriptors(trPathName);
        if (linkedFileDescriptors != null) {
            attisTr.setLinkedFileDescriptors(new Vector<LinkedFileDescriptor>(linkedFileDescriptors));
        }
        
        // add transcription or document properties;
        // convert "lastUsedAnnotationId" to an integer and remove it for now.
        List<Property> props = parser.getTranscriptionProperties(trPathName);
        int lastUsedAnnotationId = 0;
        if (props != null) {
        		PropertyImpl pimpl;
        		for (int i = 0; i < props.size(); i++) {
        			pimpl = (PropertyImpl) props.get(i);
        			if ("lastUsedAnnotationId".equals(pimpl.getName())) {
        				String val = (String) pimpl.getValue();
        				if (val != null) {
        					try {
        						lastUsedAnnotationId = Integer.parseInt(val);
            				} catch (NumberFormatException nfe) {
            					
            				}
        				}
        				props.remove(i);// or trust that it is correct??
        				break;
        			}
        		}
        		attisTr.addDocProperties(props);
        }
        // set author
        String author = parser.getAuthor(trPathName);

        if (attisTr.getAuthor().equals("")) {
            attisTr.setAuthor(author);
        }

		if (debug) {
			System.out.println("Extracting header took: " + (System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		// get the ext refs mappings
		Map extReferences = parser.getExternalReferences(trPathName);
        // make linguistic types available in transcription
        List linguisticTypes = parser.getLinguisticTypes(trPathName);

        ArrayList<LinguisticType> typesCopy = new ArrayList<LinguisticType>(linguisticTypes.size());

        for (int i = 0; i < linguisticTypes.size(); i++) {
 //           typesCopy.add(i, linguisticTypes.get(i));
 
 			LingTypeRecord ltr = (LingTypeRecord) linguisticTypes.get(i);
 
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
			// check ext ref (dcr), in Linguistic Type this is a string
			if (extReferences != null && ltr.getExtRefId() != null) {
				ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(ltr.getExtRefId());
				if (eri != null) {
					lt.setDataCategory(eri.getValue());
				}
			}
			
			typesCopy.add(lt); 
        }

        attisTr.setLinguisticTypes(new Vector<LinguisticType>(typesCopy));

		if (debug) {
			System.out.println("Creating linguistic types took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
        //attisTr.setLinguisticTypes(linguisticTypes);
        TimeOrder timeOrder = attisTr.getTimeOrder();

        // populate TimeOrder with TimeSlots
        List order = parser.getTimeOrder(trPathName);
        Map slots = parser.getTimeSlots(trPathName);

        HashMap<String, TimeSlot> timeSlothash = new HashMap<String, TimeSlot>(); // temporarily stores map from id to TimeSlot object

        Iterator orderedIter = order.iterator();
		TimeSlot ts = null;
		String tsKey = null;
		long time;
		ArrayList<TimeSlot> tempSlots = new ArrayList<TimeSlot>(order.size());
		int index = 0;
		// jan 2006: sort the timeslots before adding them all to the TimeOrder object
		// (for performance reasons)
        while (orderedIter.hasNext()) {

            tsKey = (String) orderedIter.next();
            time = Long.parseLong((String) slots.get(tsKey));

            if (time != TimeSlot.TIME_UNALIGNED) {
                ts = new TimeSlotImpl(time, timeOrder);
            } else {
                ts = new TimeSlotImpl(timeOrder);
            }

			ts.setIndex(index++);
            //timeOrder.insertTimeSlot(ts);
			tempSlots.add(ts);
            timeSlothash.put(tsKey, ts);
        }

		Collections.sort(tempSlots, new TimeSlotComparator());
		((TimeOrderImpl)timeOrder).insertOrderedSlots(tempSlots);
		
		if (debug) {
			System.out.println("Creating time slots and time order took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
        HashMap<TierImpl, String> parentHash = new HashMap<TierImpl, String>();

        if (!attisTr.isLoaded()) {
            Iterator iter = parser
                         		.getTierNames(trPathName)
                                .iterator();

            // HB, 27 aug 03, moved earlier
            attisTr.setLoaded(true); // else endless recursion !!!!!

            while (iter.hasNext()) {
                String tierName = (String) iter.next();
				
				TierImpl tier = new TierImpl(null, tierName, null, attisTr, null);

                // set tier's metadata
                String participant = parser.getParticipantOf(tierName,
						trPathName);
                String linguisticTypeID = parser
                                              .getLinguisticTypeIDOf(tierName,
						trPathName);
                String annotator = parser.getAnnotatorOf(tierName, trPathName);
				
				LinguisticType linguisticType = null;
				Iterator<LinguisticType> typeIter = typesCopy.iterator();
				while (typeIter.hasNext()) {
					LinguisticType lt = typeIter.next();
					if (lt.getLinguisticTypeName().equals(linguisticTypeID)) {
						linguisticType = lt;
						break;
					}
				}
				
                Locale defaultLanguage = parser.getDefaultLanguageOf(tierName,
						trPathName);

                tier.setParticipant(participant);
                tier.setAnnotator(annotator);
                tier.setLinguisticType(linguisticType);
                

                if (defaultLanguage != null) { // HB, 29 oct 02: added condition, since DEFAULT_LOCALE is IMPLIED
                	tier.setDefaultLocale(defaultLanguage);
                }

                // potentially, set tier's parent
                String parentId = parser.getParentNameOf(tierName,
						trPathName);

                if (parentId != null) {
                    // store tier-parent_id pair until all Tiers instantiated
                    parentHash.put(tier, parentId);
                }

                attisTr.addTier(tier);
            }
        }

        // all Tiers are created. Now set all parent tiers
        Iterator<TierImpl> parentIter = parentHash.keySet().iterator();

        while (parentIter.hasNext()) {
            TierImpl t = parentIter.next();
            t.setParentTier(attisTr.getTierWithId(parentHash.get(t)));
        }

		if (debug) {
			System.out.println("Creating tiers took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
        //	attisTr.setLoaded(true);	// else endless recursion !!!!!
        List tiers = attisTr.getTiers();

        // create Annotations. Algorithm:
        // 1. loop over annotationRecords Vector. Instantiate right Annotations. Store
        //    references to annotations in intermediate data structures
        // 2. loop over intermediate structure. Realize references to Annotations by object
        //    references, iso using annotation_id's
        HashMap<String, Annotation> idToAnnotation = new HashMap<String, Annotation>();
        HashMap<String, String> references = new HashMap<String, String>();
        HashMap<String, String> referenceChains = new HashMap<String, String>();

        // HB, 2-1-02: temporarily store annotations, before adding them to tiers.
        // Reason: object reference have to be in place to add annotations in correct order.
        HashMap<Tier, ArrayList<Annotation>> tempAnnotationsForTiers = new HashMap<Tier, ArrayList<Annotation>>();

        // create Annotations, either AlignableAnnotations or RefAnnotations
        Iterator tierIter = tiers.iterator();

        while (tierIter.hasNext()) {
            Tier tier = (Tier) tierIter.next();
            List annotationRecords = parser.getAnnotationsOf(tier.getName(),
					trPathName);

            // HB, 2-1-02
            ArrayList<Annotation> tempAnnotations = new ArrayList<Annotation>();

            Iterator it1 = annotationRecords.iterator();

            while (it1.hasNext()) {
                Annotation annotation = null;

                AnnotationRecord annotationRecord = (AnnotationRecord) it1.next();

                if (annotationRecord.getAnnotationType().equals(AnnotationRecord.ALIGNABLE)) {
                    annotation = new AlignableAnnotation(timeSlothash.get(
                                annotationRecord.getBeginTimeSlotId()),
                            timeSlothash.get(annotationRecord.getEndTimeSlotId()), tier);
                } else if (annotationRecord.getAnnotationType().equals(AnnotationRecord.REFERENCE)) {
                    annotation = new RefAnnotation(null, tier);

                    references.put(annotationRecord.getAnnotationId(), annotationRecord.getReferredAnnotId());

                    if (annotationRecord.getPreviousAnnotId() != null) {
                        referenceChains.put(annotationRecord.getAnnotationId(), annotationRecord.getPreviousAnnotId());
                    }
                }

                if (annotationRecord.getValue() != null) {
                    annotation.setValue(annotationRecord.getValue());
                }

                annotation.setId(annotationRecord.getAnnotationId());
                                
                idToAnnotation.put(annotationRecord.getAnnotationId(), annotation);
                // check value of id, may not be neccesary if the highest id value is stored in the xml file
                if (annotationRecord.getAnnotationId() != null && annotationRecord.getAnnotationId().charAt(0) == 'a') {
                		try {
                			int aid = Integer.parseInt(annotationRecord.getAnnotationId().substring(1));
                			if (aid > lastUsedAnnotationId) {
                				lastUsedAnnotationId = aid;
                			}
                		} catch (NumberFormatException nfe){
                			
                		} catch (Exception ex) {
                			// catch any
                		}
                }
                // check ext. reference
                if (extReferences != null && annotationRecord.getExtRefId() != null) {
                	ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(annotationRecord.getExtRefId());
                	if (eri != null) {
                		// create a clone to ensure that every object get its own ext ref instance
                		try {
                			((AbstractAnnotation) annotation).setExtRef(eri.clone());
                		} catch (CloneNotSupportedException cnse) {
                			LOG.severe("Could not set the external reference: " + cnse.getMessage());
                		}
                	}
                }
                //	tier.addAnnotation(annotation);
                // HB, 2-1-02
                tempAnnotations.add(annotation);
            }

            // end of loop over annotation records
            // HB, 2-1-02
            tempAnnotationsForTiers.put(tier, tempAnnotations);
        }
        // add lastUsedAnnotationIdProperty by converting it to Integer
        PropertyImpl luaid = new PropertyImpl("lastUsedAnnotationId",
        		Integer.valueOf(lastUsedAnnotationId));
        attisTr.addDocProperty(luaid);
        
        // end of loop over tierIter
        // realize object references
        Iterator<String> refIter = references.keySet().iterator();

        while (refIter.hasNext()) {
            String key = refIter.next();
            Annotation referedAnnotation = idToAnnotation.get(references.get(
                        key));
            RefAnnotation refAnnotation = null;

            try {
                refAnnotation = (RefAnnotation) idToAnnotation.get(key);
                refAnnotation.addReference(referedAnnotation);
            } catch (Exception ex) {
                //MK:02/09/17 adding exception handler
                Object o = idToAnnotation.get(key);
                LOG.warning("failed to add a refanno to  (" +
                    referedAnnotation.getTier().getName() + ", " +
                    referedAnnotation.getBeginTimeBoundary() + ", " +
                    referedAnnotation.getEndTimeBoundary() + ") " +
                    referedAnnotation.getValue());

                if (o instanceof AlignableAnnotation) {
                    AlignableAnnotation a = (AlignableAnnotation) o;
					LOG.warning("  found AlignableAnnotation (" +
                        a.getTier().getName() + ", " +
                        a.getBeginTimeBoundary() + ", " +
                        a.getEndTimeBoundary() + ") " + a.getValue());
                } else {
					LOG.warning("  found " + o);
                }
            }
        }

        // realize reference chains (== within tiers)
        Iterator<String> rIter = referenceChains.keySet().iterator();

        while (rIter.hasNext()) {
            String key = rIter.next();
            RefAnnotation previous = (RefAnnotation) idToAnnotation.get(referenceChains.get(
                        key));
            RefAnnotation a = (RefAnnotation) idToAnnotation.get(key);

            if (previous != null) {
                previous.setNext(a);
            }
        }

        // HB, 2-1-01: with object references in place, add annotations to the correct tiers.
        // This is now done in the correct order (RefAnnotation.compareTo delegates comparison
        // to it's parent annotation.
        Iterator<Tier> tIter = tempAnnotationsForTiers.keySet().iterator();

        while (tIter.hasNext()) {
            TierImpl t = (TierImpl) tIter.next();

            ArrayList annots = tempAnnotationsForTiers.get(t);
            Iterator aIter = annots.iterator();

            while (aIter.hasNext()) {
                // HB, 14 aug 02, changed from addAnnotation
                t.insertAnnotation((Annotation) aIter.next());
            }
        }

        // HB, 4-7-02: with all annotations on the proper tiers, register implicit
        // parent-child relations between alignable annotations explicitly
        Iterator tierIter2 = tiers.iterator();

        while (tierIter2.hasNext()) {
            TierImpl t = (TierImpl) tierIter2.next();

            if (t.isTimeAlignable() && t.hasParentTier()) {
                Iterator alannIter = t.getAnnotations().iterator();

                while (alannIter.hasNext()) {
                    Annotation a = (Annotation) alannIter.next();

                    if (a instanceof AlignableAnnotation) {
                        ((AlignableAnnotation) a).registerWithParent();
                    }
                }
            }
        }
        
		if (debug) {
			System.out.println("Creating and connecting annotations took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
        // HS jun 2004 create the ControlledVocabularies, if any
        
        Map cvTable = parser.getControlledVocabularies(trPathName);
        if ((cvTable != null) && (cvTable.size() > 0)) {
        	ControlledVocabulary cv = null;
        	Iterator cvIt = cvTable.keySet().iterator();
        	while (cvIt.hasNext()) {
        		String cvName = (String)cvIt.next();
        		if (cvName == null) {
        			continue;
        		}
        		cv = new ControlledVocabulary(cvName);
        		// the contents vector can contain one description String
        		// and many CVEntryRecords
        		ArrayList contents = (ArrayList)cvTable.get(cvName);

        		if (contents.size() > 0) {
					Object next;
					CVEntry entry;
        			for (int i = 0; i < contents.size(); i++) {
        				next = contents.get(i);
        				if (next instanceof String) {
        					cv.setDescription((String)next);
        				} else if (next instanceof CVEntryRecord) {        					
        					entry = new CVEntry(cv, ((CVEntryRecord)next).getValue(),	
								((CVEntryRecord)next).getDescription());
							if (entry != null) {
								// check external reference
								if (extReferences != null && ((CVEntryRecord)next).getExtRefId() != null) {
									ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(
											((CVEntryRecord)next).getExtRefId());
									if (eri != null) {
										try {
											entry.setExternalRef(eri.clone());
										} catch (CloneNotSupportedException cnse) {
											LOG.severe("Could not set the external reference: " + cnse.getMessage());
										}
									}
								}
								cv.addEntry(entry);
							}
        				}
        			}
        		}
        		
        		//cv.setACMEditableObject(attisTr);
				attisTr.addControlledVocabulary(cv);
        	}
        }

		if (debug) {
			System.out.println("Creating CV's took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
		// if all root annotations unaligned (in case of shoebox or chat import)
		// align them at 1 second intervals
		if (attisTr.allRootAnnotsUnaligned()) {
			attisTr.alignRootAnnots();
		}
		
		// hb, 23-9-04
		// There are cases where more than one symbolically associated annotation refers
		// to the same parent annotation (e.g. shoebox files with interlinearized tiers
		// with 'tokens' separated by spaces, as for Advanced Glossing stuff.
		// Fix this by concatenating the values of those annotations in one RefAnnotation
		if (concatenateAfterParse) {
			// May 2009 only perform this postprocessing in the appropriate cases
			concatenateSymbolicAssociations(attisTr);
		}
      
		if (debug) {
			System.out.println("Post-processing took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}      
        //System.out.println("getName: " + attisTr.getName());
        //System.out.println("fullpath: " + attisTr.getFullPath());
        //System.out.println("pathname: " + attisTr.getPathName());
    }
    
    public void concatenateSymbolicAssociations(TranscriptionImpl transcription) {
		Annotation lastParent = null;
		RefAnnotation lastAnnot = null;
		
		Vector<RefAnnotation> annotsToRemove = new Vector<RefAnnotation>();
		
		List tiers = transcription.getTiers();
			
			Iterator tierIter = tiers.iterator();
			while (tierIter.hasNext()) {
				lastParent = null;
				lastAnnot = null;
				annotsToRemove.clear();
				
				TierImpl t = (TierImpl) tierIter.next();
				LinguisticType lt = t.getLinguisticType();
				Constraint c = null;
				if (lt != null) {
					c = lt.getConstraints();
				}
				if (c != null && c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
					// iterate over annots, take annots with same parent together
					Iterator annIter = t.getAnnotations().iterator();
					while (annIter.hasNext()) {
						RefAnnotation a = (RefAnnotation) annIter.next();
						if (a.getParentAnnotation() == lastParent) {
							lastAnnot.setValue(lastAnnot.getValue() + " " + a.getValue());
							annotsToRemove.add(a);
						} 
						else {
							lastParent = a.getParentAnnotation();
							lastAnnot = a;
						}
					}
					
					// remove concatenated annots
					Iterator<RefAnnotation> rIter = annotsToRemove.iterator();
					while (rIter.hasNext()) {
						t.removeAnnotation(rIter.next());
					}
				}
			}

    }
}
