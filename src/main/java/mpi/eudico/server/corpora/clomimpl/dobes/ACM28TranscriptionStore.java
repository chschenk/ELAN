 package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.prefs.RecentLanguages;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.ExternalReference;
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
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSet;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSetRecord;
import mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxEncoder;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo2;
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
import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.multilangcv.LangInfo;


/**
 * A TranscriptionStore that corresponds to EAF v2.8.<br>
 * Version 2.8 has multi-language CVs and some extra attributes.
 * Version 2.7 extends v2.6 by adding several elements and attributes 
 * related to working with external controlled vocabularies and with
 * lexicons.
 *
 * @see EAF27TranscriptionStore
 * @see EAF26TranscriptionStore
 * 
 * @author Hennie Brugman
 * @author Han Sloetjes
 * @author Olaf Seibert
 * @version jun 2004
 * @version Aug 2005 Identity removed
 * @version Feb 2006 LinkedFileDescriptor and Included_In added
 * @version Dec 2006 Annotator added, document level Property added
 * @version Nov 2007 relative url's for media and linked files added
 * @version May 2008 support for external references (such as ISO DCR) added
 * @version Dec 2010 support for external CV's and for LEXICON references added
 * @version May 2014 support for multi-lingual CVs added, and prefs format conversion
 */
public class ACM28TranscriptionStore implements TranscriptionStore, ServerLogger {
	
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
     * Creates a new ACM28TranscriptionStore instance
     */
    public ACM28TranscriptionStore() {
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
    	
    	/* Get the URN, to make sure one is created if it didn't exist yet. */
    	theTranscription.getURN();

		switch (format) {
			case TranscriptionStore.EAF:
				if (this.fileToWriteXMLinto != null) {
					path = fileToWriteXMLinto.getAbsolutePath();
				}

				new EAF28Encoder().encodeAndSave(theTranscription, null, tierOrder, path);
				break;
				
			case TranscriptionStore.EAF_2_7:
				if (this.fileToWriteXMLinto != null) {
					path = fileToWriteXMLinto.getAbsolutePath();
				}
								
				new EAF27Encoder().encodeAndSave(theTranscription, null, tierOrder, path);
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
        List<TierImpl> tierOrder, String path) throws IOException{
        	
        if (this.fileToWriteXMLinto != null) {
        	path = fileToWriteXMLinto.getAbsolutePath();	
        }
        
        new EAF28Encoder().encodeAsTemplateAndSave(theTranscription, tierOrder, path);
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
		int format = -1;
		if (lowerPathName.endsWith("cha")) {
			parser = ParserFactory.getParser(ParserFactory.CHAT);
			//concatenateAfterParse = true;//??
			format = TranscriptionStore.CHAT;
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
			format = TranscriptionStore.SHOEBOX;
		}
		else if (lowerPathName.endsWith("trs")) {	// Transcriber
			parser = ParserFactory.getParser(ParserFactory.TRANSCRIBER);
			parser.setDecoderInfo(decoderInfo);
			format = TranscriptionStore.TRANSCRIBER;
		}
		else if (decoderInfo instanceof FlexDecoderInfo) {	// FLEx
			parser = ParserFactory.getParser(ParserFactory.FLEX);
			parser.setDecoderInfo(decoderInfo);
		}
		else if (lowerPathName.endsWith("imdi")) {	// CGN
			parser = ParserFactory.getParser(ParserFactory.CGN);
		}
		else {
			// Have a quick look inside the file to check the version
			int version = ACMTranscriptionStore.eafFileFormatTaster(trPathName);
			parser = ParserFactory.getParser(version);
			
			if (version <= ParserFactory.EAF26) {
				format = TranscriptionStore.EAF_2_7;
			} else if (version == ParserFactory.EAF27) {
				format = TranscriptionStore.EAF_2_7;
			} else {
				format = TranscriptionStore.EAF;
			}
		}
		
		long beginTime = System.currentTimeMillis();
		if (debug) {
			System.out.println("Parsing eaf took: " + (System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();
		}

        // set license and url
        attisTr.setLicenses(parser.getLicenses(trPathName));

        // make media descriptors available in transcription
        List<MediaDescriptor> mediaDescriptors = parser.getMediaDescriptors(trPathName);
        attisTr.setMediaDescriptors(new ArrayList<MediaDescriptor>(mediaDescriptors));

        // add linked file descriptors
        List<LinkedFileDescriptor> linkedFileDescriptors = parser.getLinkedFileDescriptors(trPathName);
        if (linkedFileDescriptors != null) {
            attisTr.setLinkedFileDescriptors(new ArrayList<LinkedFileDescriptor>(linkedFileDescriptors));
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

        if (attisTr.getAuthor() == null || attisTr.getAuthor().isEmpty()) {
            attisTr.setAuthor(author);
        }

		if (debug) {
			System.out.println("Extracting header took: " + (System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		// get the ext refs mappings
		Map<String, ExternalReferenceImpl> extReferences = parser.getExternalReferences(trPathName);
        // make linguistic types available in transcription
        List<LingTypeRecord> linguisticTypes = parser.getLinguisticTypes(trPathName);
        
        // Map to temporarily store links to lexica in order to couple a Lexicon Query Bundle
        // to a Linguistic Type
        Map<String, LinguisticType> lexRefs = new HashMap<String, LinguisticType>();

        List<LinguisticType> typesCopy = new ArrayList<LinguisticType>(linguisticTypes.size());

        for (int i = 0; i < linguisticTypes.size(); i++) {
 //           typesCopy.add(i, linguisticTypes.get(i));
 
 			LingTypeRecord ltr = linguisticTypes.get(i);
 
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
			lexRefs.put(ltr.getLexiconReference(), lt);
			// check ext ref (dcr), in Linguistic Type this is a string
			if (extReferences != null && ltr.getExtRefId() != null) {
				ExternalReferenceImpl eri = extReferences.get(ltr.getExtRefId());
				if (eri != null) {
					lt.setDataCategory(eri.getValue());
				}
			}
			
			typesCopy.add(lt); 
        }

        attisTr.setLinguisticTypes(new ArrayList<LinguisticType>(typesCopy));

		if (debug) {
			System.out.println("Creating linguistic types took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
        //attisTr.setLinguisticTypes(linguisticTypes);
        TimeOrder timeOrder = attisTr.getTimeOrder();

        // populate TimeOrder with TimeSlots
        List<String> order = parser.getTimeOrder(trPathName);
        Map<String, String> slots = parser.getTimeSlots(trPathName);

        Map<String, TimeSlot> timeSlothash = new HashMap<String, TimeSlot>(); // temporarily stores map from id to TimeSlot object

        Iterator<String> orderedIter = order.iterator();
		TimeSlot ts = null;
		String tsKey = null;
		long time;
		List<TimeSlot> tempSlots = new ArrayList<TimeSlot>(order.size());
		int index = 0;
		// jan 2006: sort the timeslots before adding them all to the TimeOrder object
		// (for performance reasons)
        while (orderedIter.hasNext()) {

            tsKey = orderedIter.next();
            time = Long.parseLong(slots.get(tsKey));

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

        /*
         * This sorting is sometimes based on the index value which was given just above.
         * In effect, it assumes that unaligned timeslots are given in the correct order
         * with respect to each other and their aligned neighbours.
         * Within each tier, the timeslots are ordered, but between tiers this isn't
         * always true for unaligned timeslots. Apparently this causes no harm.
         */
		Collections.sort(tempSlots, new TimeSlotComparator());
		((TimeOrderImpl)timeOrder).insertOrderedSlots(tempSlots);
		
		if (debug) {
			System.out.println("Creating time slots and time order took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
        Map<TierImpl, String> parentHash = new HashMap<TierImpl, String>();

        if (!attisTr.isLoaded()) {
            Iterator<String> iter = parser
                         		.getTierNames(trPathName)
                                .iterator();

            // HB, 27 aug 03, moved earlier
            // HS 09-2011 this causes many edit events to be generated and that the
            // changed flag is set
            //attisTr.setLoaded(true); // else endless recursion !!!!!

            while (iter.hasNext()) {
                String tierName = iter.next();
				
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
				
                Locale defaultLocale = parser.getDefaultLanguageOf(tierName,
						trPathName);

                tier.setParticipant( participant);
                tier.setAnnotator(annotator);
                tier.setLinguisticType(linguisticType);
                

                if (defaultLocale != null) { // HB, 29 oct 02: added condition, since DEFAULT_LOCALE is IMPLIED
                    tier.setDefaultLocale(defaultLocale);
                }

                // potentially, set tier's parent
                String parentId = parser.getParentNameOf(tierName,
						trPathName);

                if (parentId != null) {
                    // store tier-parent_id pair until all Tiers instantiated
                    parentHash.put(tier, parentId);
                }
                
                tier.setLangRef(parser.getLangRefOf(tierName, trPathName));
                tier.setExtRef(parser.getExtRefOf(tierName, trPathName));

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
        List<TierImpl> tiers = attisTr.getTiers();

        // create Annotations. Algorithm:
        // 1. loop over annotationRecords List. Instantiate right Annotations. Store
        //    references to annotations in intermediate data structures
        // 2. loop over intermediate structure. Realize references to Annotations by object
        //    references, iso using annotation_id's
        Map<String, Annotation> idToAnnotation = new HashMap<String, Annotation>();
        Map<String, String> references = new HashMap<String, String>();
        Map<String, String> referenceChains = new HashMap<String, String>();

        // HB, 2-1-02: temporarily store annotations, before adding them to tiers.
        // Reason: object reference have to be in place to add annotations in correct order.
        Map<Tier, List<Annotation>> tempAnnotationsForTiers = new HashMap<Tier, List<Annotation>>();

        // create Annotations, either AlignableAnnotations or RefAnnotations

        for (Tier tier : tiers) {
            List<AnnotationRecord> annotationRecords = parser.getAnnotationsOf(tier.getName(),
					trPathName);
            
            // Check if this tier has an External Controlled Vocabulary.
            boolean tierHasExternalCV = false;
            
            String cvName = ((TierImpl)tier).getLinguisticType().getControlledVocabularyName();
            Map<String, CVRecord> cvTable = parser.getControlledVocabularies(trPathName);
            
            if (cvTable != null && cvName != null) {
            	CVRecord cvRecord = cvTable.get(cvName);

            	tierHasExternalCV = cvRecord != null &&
				            		cvRecord.getExtRefId() != null &&
				            		!cvRecord.getExtRefId().isEmpty();
            }

            // HB, 2-1-02
            List<Annotation> tempAnnotations = new ArrayList<Annotation>();

            Iterator<AnnotationRecord> it1 = annotationRecords.iterator();

            while (it1.hasNext()) {
                Annotation annotation = null;

                AnnotationRecord annotationRecord = it1.next();

                if (annotationRecord.getAnnotationType().equals(AnnotationRecord.ALIGNABLE)) {
                    annotation = new AlignableAnnotation(timeSlothash.get(
                                annotationRecord.getBeginTimeSlotId()),
                            timeSlothash.get(annotationRecord.getEndTimeSlotId()), tier);

                } else /*if (annotationRecord.getAnnotationType().equals(AnnotationRecord.REFERENCE))*/ {
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
                       
                // By Micha: the entries of an (External) CV have IDs that should be used
        		// for consistent annotation values
        		if(annotationRecord.getCvEntryId() != null && !annotationRecord.getCvEntryId().isEmpty()) {
        			annotation.setCVEntryId(annotationRecord.getCvEntryId());
        		}        		
                
                idToAnnotation.put(annotationRecord.getAnnotationId(), annotation);
                // check value of id, may not be necessary if the highest id value is stored in the xml file
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
                	String[] extRefIds = annotationRecord.getExtRefId().split(" ");
                	for (String extRefId : extRefIds) {
						ExternalReferenceImpl eri = extReferences
								.get(extRefId);
						
						if (eri != null) {
							if (tierHasExternalCV &&
									eri.getReferenceType() == ExternalReference.CVE_ID &&
									annotation.getCVEntryId() == null) {
								// This may happen in old EAF files before 2.8.
								// Convert the EXT_REF of type CVE_ID to a direct CVE_REF.
								String cvEntryId = eri.getValue();
								annotation.setCVEntryId(cvEntryId);
							} else 
							if (eri.getReferenceType() == ExternalReference.ISO12620_DC_ID 
								|| eri.getReferenceType() == ExternalReference.CVE_ID) {
								// create a clone to ensure that every object get its own ext ref instance
								try {
									((AbstractAnnotation) annotation).addExtRef(eri.clone());
								} catch (CloneNotSupportedException cnse) {
									LOG.severe("Could not set the external reference: " + cnse.getMessage());
								}
							}
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

            List<Annotation> annots = tempAnnotationsForTiers.get(t);
            Iterator<Annotation> aIter = annots.iterator();

            while (aIter.hasNext()) {
                // HB, 14 aug 02, changed from addAnnotation
                t.insertAnnotation(aIter.next());
            }
        }

        // HB, 4-7-02: with all annotations on the proper tiers, register implicit
        // parent-child relations between alignable annotations explicitly

        for (TierImpl t : tiers) {
            if (t.isTimeAlignable() && t.hasParentTier()) {
                Iterator<? extends Annotation> alannIter = t.getAnnotations().iterator();

                while (alannIter.hasNext()) {
                    Annotation a = alannIter.next();

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
        
        Map<String, CVRecord> cvTable = parser.getControlledVocabularies(trPathName);
        List<LanguageRecord> languages = parser.getLanguages(trPathName);
        
        // Seed the "recent languages" list with the listed languages.
        // They may be used as a Tier language without being in a CV.
        if (languages != null) {
	        for (LanguageRecord lr : languages) {
	        	RecentLanguages.getInstance().addRecentLanguage(
	        			new LangInfo(lr.getId(), lr.getDef(), lr.getLabel()));
	        }
        }
        
        if ((cvTable != null) && (cvTable.size() > 0)) {
        	ControlledVocabulary cv = null;
        	Set<String> cvIt = cvTable.keySet();
        	for (String cvName : cvIt) {
        		if (cvName == null) {
        			continue;
        		}
        		// a CVRecord can contain one description String
        		// and many CVEntryRecords
        		CVRecord contents = cvTable.get(cvName);
        		
        		// If there is an external reference, we are dealing with an 
        		// external CV
        		if(contents.getExtRefId() != null && contents.getExtRefId() != ""
        			&& extReferences != null) {
        			cv = new ExternalCV(cvName);
        			ExternalReferenceImpl eri =  extReferences.get(contents.getExtRefId());
        			if (eri != null) {
        				try {
        					((ExternalCV) cv).setExternalRef(eri.clone());
        				} catch (CloneNotSupportedException cnse) {
        					LOG.severe("Could not set the external reference: " + cnse.getMessage());
        				}
        			}
        		} else {
        			cv = new ControlledVocabulary(cvName);
        			//HS 09-2011 make sure loading of the CV does not set the changed flag.
        			cv.setInitMode(true);
        		}
        		
        		List<CVDescriptionRecord> descriptions = contents.getDescriptions();
        		// HS DESCRIPTION is an optional element, so this can't be the correct test for multi-lingual CV's?
        		// Revise
        		boolean isMultiLanguage = descriptions != null && !descriptions.isEmpty();
        		
        		if (isMultiLanguage) {
        			/*
        			 * Add all languages from <DESCRIPTION> tags to the vocabulary.
        			 */
        			for (CVDescriptionRecord d : descriptions) {
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

        		if (contents.hasContents()) { 
        			if(contents.getDescription() != null && !contents.getDescription().isEmpty()) {
        				// Non-multi-language case
        				cv.setDescription(0, contents.getDescription());
        			}
        			// load the externals in a post processing step
        			if(! (cv instanceof ExternalCV)) {
        				CVEntry entry;
        				List<CVEntryRecord> entriesInRecord = contents.getEntries();
        				for (int i = 0; i < entriesInRecord.size(); i++) {
        					CVEntryRecord cveRecord = entriesInRecord.get(i);
							entry = new CVEntry(cv);
        					if (cveRecord.getId() != null) {
        						entry.setId(cveRecord.getId());
        					}
    						if (isMultiLanguage) {
    							if (cveRecord.getSubEntries() != null) {
    								// A CVEntry without words should not occur, but has been seen in the wild.
	    							for (CVEntryRecord subRecord : cveRecord.getSubEntries()) {
	    								String langId = subRecord.getSubEntryLangRef();
	    								int langIndex = cv.getIndexOfLanguage(langId);
	    								if (langIndex >= 0) {
		    								entry.setDescription(langIndex, subRecord.getDescription());
		    								entry.setValue(langIndex, subRecord.getValue());
	    								} else {
	    									LOG.warning("<CVE_VALUE> element with attribute LANG_REF=\"" + langId + 
	    											"\" for which there was no <DESCRIPTION>. The element was ignored.");
	    								}
	    							}
    							}
    						} else {
								entry.setDescription(cveRecord.getDescription());
								entry.setValue(cveRecord.getValue());
        					}

    						// check external reference
    						if (extReferences != null
    								&& cveRecord.getExtRefId() != null) {
    							ExternalReferenceImpl eri = extReferences.get(cveRecord.getExtRefId());
    							if (eri != null) {
    								try {
    									entry.setExternalRef(eri.clone());
    								} catch (CloneNotSupportedException cnse) {
    									LOG.severe("Could not set the external reference: "
    											+ cnse.getMessage());
    								}
    							}
    						}
    						cv.addEntry(entry);
    					}
        			}
        		}
        		
        		//cv.setACMEditableObject(attisTr);
        		cv.setInitMode(false);
				attisTr.addControlledVocabulary(cv);
        	}
        }

		if (debug) {
			System.out.println("Creating CVs took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}
		
		// Now that we have the CVs, we can find the CVEntryIDs where they were not given.
		// If this is an old file format, from before the time that CvEntryIDs were habitually
		// used to connect Annotations to CVEntries, that is probably the case.
		// We don't need to do this for 2.008.000 and higher.
		
		int fileFormat = parser.getFileFormat();
		if (fileFormat > 0 && fileFormat < 2008000) { // if (format == EAF_2_7)
			for (Tier t : tiers) {
	            // When reading in old files without IDs from Annotation -> CVEntry,
	            // we need to create them.
				TierImpl ti =  (TierImpl)t;
	            String cvName = ti.getLinguisticType().getControlledVocabularyName();
	            ControlledVocabulary controlledVocabulary = ((TranscriptionImpl)theTranscription).
	            														getControlledVocabulary(cvName);
	            // If no CV associated with the tier, we can skip it.
	            if (controlledVocabulary == null) {
	            	continue;	// 
	            }
	
	        	List<? extends Annotation> va = ti.getAnnotations();
	        	for (Annotation a : va) {
	        		String id = a.getCVEntryId();
	        		// If there is an ID already, don't change it.
	        		if (id != null && !id.isEmpty()) {
	        			continue;
	        		}
					// If there is no CvEntryId yet (for old files) then we need to find it here.
					String word = a.getValue();
					final int langIndex = 0; // Old files have only 1 language in their CVs.
					CVEntry entry = controlledVocabulary.getEntryWithValue(langIndex, word);
					if (entry != null) {
						a.setCVEntryId(entry.getId());
					}
	        	}
			}
			
			if (debug) {
				System.out.println("Looking up Annotation values in their CVs took: " + 
					(System.currentTimeMillis() - beginTime) + " ms");
				beginTime = System.currentTimeMillis();	
			}
		}
		
		// Related to the above conversion of Annotations,
		// convert the preferences format: needed for the change in EAF 2.8 where
		// the preferences for CV entries are keyed by ID rather than Value.
		// Since preferences are auto-loaded when needed this will just load them
		// a bit earlier.
		SaveAs27Preferences.adjustPreferencesAfterLoadingFormat(theTranscription, format);

		// Lexicon Services (Micha Hulsbosch, 2010)
		// Add the Lexicon Query Bundles and Lexicon Links to the transcription
		Map<String, LexiconServiceRecord> lexSrvcTable = parser.getLexiconServices(trPathName);
        if ((lexSrvcTable != null) && (lexSrvcTable.size() > 0)) {
        	LexiconQueryBundle2 bundle = null;
        	Iterator<String> lexIt = lexSrvcTable.keySet().iterator();
        	while (lexIt.hasNext()) {
        		String lexRef = lexIt.next();
        		if (lexRef == null) {
        			continue;
        		}
        		LexiconServiceRecord contents = lexSrvcTable.get(lexRef);
        		
        		// First try to create a client
        		//HashMap<String, LexiconServiceClientFactory> factories = attisTr.getLexiconServiceClientFactories();
        		//LexiconServiceClientFactory clientFactory = factories.get(contents.getType());
        		LexiconIdentification lexiconIdentification = new LexiconIdentification();
        		lexiconIdentification.setId(contents.getLexiconId());
        		lexiconIdentification.setName(contents.getLexiconName());
//        		LexiconServiceClient client = null;
//        		if(clientFactory == null) {
//        			client = null;
//        		} else {
//        			client = clientFactory.createClient(contents.getUrl());
//        		}
        		
        		// Second create a LexiconLink
        		LexiconLink link = new LexiconLink(contents.getName(), contents.getType(), contents.getUrl(), null, lexiconIdentification);
        		attisTr.addLexiconLink(link);
        		
        		// Third make a bundle
        		if(lexRefs.containsKey(lexRef)) {
        			bundle = new LexiconQueryBundle2(link, new LexicalEntryFieldIdentification(contents.getDatcatId(), contents.getDatcatName()));
        			lexRefs.get(lexRef).setLexiconQueryBundle(bundle);
        		}
        	}
        }
        
        // Realise Reference Links, if any
        loadReferenceLinks(attisTr, parser);
        /*
        RefLinkSetRecord refLinkSetRecord = parser.getRefLinkSet(trPathName);
        if (refLinkSetRecord != null) {
            RefLinkSet refLinkSet = refLinkSetRecord.fabricate(attisTr, extReferences);

            attisTr.addRefLinkSet(refLinkSet);
        }
        */
		
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
        // HS set the loaded flag
		attisTr.setLoaded(true);
		
		if (debug) {
			System.out.println("Post-processing took: " + 
				(System.currentTimeMillis() - beginTime) + " ms");
			beginTime = System.currentTimeMillis();	
		}      
        //System.out.println("getName: " + attisTr.getName());
        //System.out.println("fullpath: " + attisTr.getFullPath());
        //System.out.println("pathname: " + attisTr.getPathName());
		
		// By Micha: An external CV could be newer than the one originally used.
		// This means that entry values could be changed. The following changes annotation value
		// to the ones in the entries of the external CV
		// TODO check whether the external CV is actually newer!
		// HS: moved to the "client" side, this is only necessary when the transcription is loaded
		// in an editor (ELAN)
		
    }
    
	public void concatenateSymbolicAssociations(TranscriptionImpl transcription) {
		Annotation lastParent = null;
		RefAnnotation lastAnnot = null;
		
		List<RefAnnotation> annotsToRemove = new ArrayList<RefAnnotation>();
		
    	for (TierImpl t : transcription.getTiers()) {
			lastParent = null;
			lastAnnot = null;
			annotsToRemove.clear();
			
			LinguisticType lt = t.getLinguisticType();
			Constraint c = null;
			if (lt != null) {
				c = lt.getConstraints();
			}
			if (c != null && c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
				// iterate over annots, take annots with same parent together
				for (RefAnnotation a : t.getRefAnnotations()) {
					if (a.getParentAnnotation() == lastParent && lastAnnot != null) {
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
	
	/**
	 * First bit moved out of {@link #loadTranscription(Transcription, DecoderInfo)} to facilitate
	 * new versions to extend existing older versions (in this case of a TranscriptionStore but the 
	 * same is true and ongoing for encoder and parser).
	 * 
	 * @param transcription
	 * @param parser
	 */
	protected void loadReferenceLinks(TranscriptionImpl transcription, Parser parser) {
		
		List<RefLinkSetRecord> refLinkRecords = parser.getRefLinkSetList(transcription.getPathName());
		
		if (refLinkRecords != null && !refLinkRecords.isEmpty()) {
			RefLinkSetRecord refLinkSetRecord = refLinkRecords.get(0);
	        if (refLinkSetRecord != null) {
	            RefLinkSet refLinkSet = refLinkSetRecord.fabricate(transcription, 
	            		parser.getExternalReferences(transcription.getPathName()));

	            transcription.addRefLinkSet(refLinkSet);
	        }
		}
	}
}
