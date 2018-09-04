package mpi.eudico.server.corpora.clomimpl.abstr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSet;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientFactory;
import mpi.eudico.server.corpora.util.ACMEditableObject;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.Pair;
import mpi.eudico.util.TimeFormatter;
/**
 * TranscriptionImpl implements Transcription.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 22-Jun-1999
 * @version Dec 2012 TreeViewable, SharedDataObject removed
 *
 */
// modified Daan Broeder 23-10-2000
// added url attribute + getFullPath() method
// added url parameter to constructor
//
public class TranscriptionImpl implements Transcription {

	private List<ACMEditListener> listeners;
	
	/** the name of a new transcription, before its is saved */
	public static final String UNDEFINED_FILE_NAME = "aishug294879ryshfda9763afo8947a5gf";

	protected String fileName;
	protected String lockFileName;

	/**
	 * The list of Tiers in this Transcription.
	 */
	protected List<TierImpl> tiers;

	/**
	 * Descriptors for the media files or streams associated with this Transcription
	 */
	protected List<MediaDescriptor> mediaDescriptors;
	
	/**
	 * Descriptors for secondary associated files. I.e. non-audio/video files, or files 
	 * representing sources that are not a primary subject of transcription.
	 */
	protected List<LinkedFileDescriptor> linkedFileDescriptors;

	/**
	 * The url of the transcription (if applicable)
	 */
	protected String url;

	/**
	 * Transcription name
	 */
	protected String name;
	protected TimeOrder timeOrder;
	protected List<LinguisticType> linguisticTypes;		// contains id strings for all types
	protected String author;
	protected boolean isLoaded;

	private boolean changed = false;
	
	private int timeChangePropagationMode = Transcription.NORMAL;
	private TimeProposer timeProposer;
	
	/** 
	 * Holds associated ControlledVocabulary objects.
	 * @since jun 04
	 */
	protected List<ControlledVocabulary> controlledVocabularies;
	
	private List<Property> docProperties;
	private Map<String, LexiconServiceClientFactory> lexiconServiceClientFactories;
	private boolean lexiconServicesLoaded = false;
	
	/**
	 * Holds associated Lexicon Link objects
	 * @author Micha Hulsbosch
	 * @since October 2010
	 */
	private HashMap<String, LexiconLink> lexiconLinks;
	
	
	/**
	 * A flag to temporarily turn of unnecessary notification of ACMEditListeners, 
	 * e.g. when a number of modifications is performed in a batch.
	 * @since oct 04
	 */
	protected boolean isNotifying;
	
	/*
	 * URN handling.
	 */
	
	public final static String URN_PROPERTY_NAME = "URN";
	private final static String URN_PREFIX = "urn:nl-mpi-tools-elan-eaf:";
	
	private URI cachedURNValue;
	
	private String currentCVLanguage = "";

	private List<LicenseRecord> licenses;

	private List<RefLinkSet> refLinkSets;
	
	/**
	 * New constructor for unknown file name
	 */
	public TranscriptionImpl() {
		this(UNDEFINED_FILE_NAME);
	}

	/**
	 * New constructor with only the full file path
	 *
	 * @param eafFilePath the (absolute) path to the eaf file
	 */
	public TranscriptionImpl(String eafFilePath) {
		this(FileUtility.fileNameFromPath(eafFilePath),
				FileUtility.pathToURLString(eafFilePath));
				
		initialize(FileUtility.fileNameFromPath(eafFilePath),
				eafFilePath, null);	
	}

	/**
	 * Constructor with the full source file path and an additional info object for the decoder/parser.
	 *
	 * @param sourceFilePath the full path to the source file
	 * @param decoderInfo the info object for the parser
	 */
	public TranscriptionImpl(String sourceFilePath, DecoderInfo decoderInfo) {
		this(FileUtility.fileNameFromPath(sourceFilePath), 
				FileUtility.pathToURLString(sourceFilePath));
				
		initialize(FileUtility.fileNameFromPath(sourceFilePath),
				sourceFilePath, decoderInfo);	
	}
	
	/**
	 * @param theName the name of the path
	 * @param myURL the path to the file or null
	 */
	private TranscriptionImpl(String theName, 
							String myURL) {
		name = theName;
		url = myURL;

		tiers = new ArrayList<TierImpl>();

		//metaTime = new FastMetaTime();
		listeners=new ArrayList<ACMEditListener>();

		timeOrder = new TimeOrderImpl(this);
		linguisticTypes = new ArrayList<LinguisticType>();
		mediaDescriptors = new ArrayList<MediaDescriptor>();
		linkedFileDescriptors = new ArrayList<LinkedFileDescriptor>();
		controlledVocabularies = new ArrayList<ControlledVocabulary>();
		docProperties = new ArrayList<Property>(5);
		isLoaded = false;
		isNotifying = true;
		timeProposer = new TimeProposer();
	}

	/**
	 * @param name the name of the file, including the extension if it is an existing file
	 * @param fileName the absolute path of the XML transcription file.
	 * @param decoderInfo decoder info object for certain file types(Toolbox, Transcriber etc)
	 */
	private void initialize(String name, String fileName, DecoderInfo decoderInfo) {
     
		fileName = FileUtility.urlToAbsPath(fileName);
		
		author = ""; // make sure that it is initialized to empty string

		File fff = new File(fileName);
		if (!fff.exists()) {// this should throw an exception
			isLoaded = true; // prevent loading
			System.out.println("The source file of the transcription could not be found: " + fileName);
		} else {
			isLoaded = false;
		}

		this.fileName = fileName;
		this.lockFileName = fff.getParent() + File.separator + "." + fff.getName() + ".lock";
		
        lexiconLinks = new HashMap<String, LexiconLink>();

        try
        {
    		this.lock();
        }
        catch (IOException e)
        {
        	System.err.println("Could not create lock file:");
        	System.err.println(e.getLocalizedMessage());
        	isLoaded = true; // prevent loading
        }
        
		// HB, 3 dec 03. After this, media descriptors are instantiated, if in the EAF file
		if (!isLoaded()) {
			ACMTranscriptionStore.getCurrentTranscriptionStore().loadTranscription(this, decoderInfo);
			// jul 2005: make sure the proposed times are precalculated
			timeProposer.correctProposedTimes(this, null, 
							ACMEditEvent.CHANGE_ANNOTATIONS, null);
			String errors = checkConsistency();
			if (!errors.isEmpty()) {
				System.err.println("Consistency problems have been detected in the transcription:");
				System.err.print(errors);
			}
		}
	}
	
	@Override
	public void addACMEditListener(ACMEditListener l){
		if(!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	@Override
	public void removeACMEditListener(ACMEditListener l){
		listeners.remove(l);
	}

	@Override
	public void notifyListeners(ACMEditableObject source, int operation, Object modification){
		Iterator<ACMEditListener> i=listeners.iterator();
		ACMEditEvent event = new ACMEditEvent(source, operation, modification);
		while(i.hasNext()){
			i.next().ACMEdited(event);
		}
	}

   	@Override
	public void modified(int operation, Object modification) {
   		handleModification(this, operation, modification);
   	}


    @Override
	public void handleModification(ACMEditableObject source, int operation, Object modification) {
		setChanged();
		
		timeProposer.correctProposedTimes(this, source, operation, modification); 
		
		if (isNotifying) {
			notifyListeners(source, operation, modification);
		}		
	}
	
	/**
	 * Sets the notification flag.
	 * When set to false ACMEditListeners are no longer notified of modification.
	 * When set to true listeners are notified of an CHANGE_ANNOTATIONS 
	 * ACMEditEvent. Every modification will then be followed by a notification.
	 * 
	 * @param notify the new notification flag  
	 */
	public void setNotifying(boolean notify) {
		isNotifying = notify;

		// If the transcription is known to be unchanged, don't send the event
		// (which sets changed to true as a side effect).
		// It would be even better to try to detect if there was a modification since
		// the call to setNotifying(false).
		if (isNotifying && changed) {
			modified(ACMEditEvent.CHANGE_ANNOTATIONS, null);		
		}
	}
	
	/**
	 * Returns the notifying flag.
	 * @return true when ACMEditListeners are notified of every modification, 
	 * 	false otherwise
	 */
	public boolean isNotifying() {
		return isNotifying;
	}

	/**
	 * Returns the name of the Transcription
	 *
	 * @return	name of Transcription
	 */
	@Override
	public String getName() {
		return name;
	}


	@Override
	public void setName(String theName) {
		name = theName;
	}

	/**
	 * Returns the url of the Transcription
	 *
	 * @return	url string of Transcription
	 */
	@Override
	public String getFullPath() {
		return url;
	}

	@Override
	public List<MediaDescriptor> getMediaDescriptors() {
		return mediaDescriptors;
	}

	@Override
	public void setMediaDescriptors(List<MediaDescriptor> theMediaDescriptors) {
		mediaDescriptors = theMediaDescriptors;
	}

	/**
	 * Returns the collection of linked file descriptors
	 * @return the linked file descriptors
	 */
	@Override
	public List<LinkedFileDescriptor> getLinkedFileDescriptors() {
		return linkedFileDescriptors;
	}
	
	/**
	 * Sets the collection of linked files descriptors.
	 * NB: could check for null here
	 * @param descriptors the new descriptors
	 */
	@Override
	public void setLinkedFileDescriptors(List<LinkedFileDescriptor> descriptors) {
		linkedFileDescriptors = descriptors;
	}

	public boolean isLoaded() {
		return isLoaded;
	}


	public void setLoaded(boolean loaded) {
		isLoaded = loaded;
	}

	@Override
	public void addTier(Tier theTier) {
		addTier((TierImpl)theTier);
	}
	
	public void addTier(TierImpl theTier) {
		// HS Dec 2012 TODO throw an exception if a tier is added with a name that is 
		// already in use by a tier in the list of tiers
		if (theTier == null || getTierWithId(theTier.getName()) != null) {
			// tier (name) already there, return (should throw exception
			return;
		}
		tiers.add(theTier);

		if (isLoaded()) {
			modified(ACMEditEvent.ADD_TIER, theTier);
		}
	}


	/**
	 * Satisfy the interface, by accepting a Tier,
	 * but in practice it should always be a TierImpl.
	 * 
	 * Since it is never called, this shows that the usage is correct.
	 */
	@Override
	public void removeTier(Tier theTier) {
		removeTier((TierImpl) theTier);
	}
	
	public void removeTier(TierImpl theTier) {
		theTier.removeAllAnnotations();

		List<Tier> deletedTiers = new ArrayList<Tier>();
		deletedTiers.add(theTier);

		// loop over tiers, remove all tiers where:
		// - number of annotations is 0
		// - and tier.hasAncestor(theTier)

		for (TierImpl t : tiers) {

			if (	(t.getNumberOfAnnotations() == 0) &&
				(t.hasAncestor(theTier)) ){

				deletedTiers.add(t);
			}
		}
		tiers.removeAll(deletedTiers);

		modified(ACMEditEvent.REMOVE_TIER, theTier);
	}


	@Override
	public TimeOrder getTimeOrder() {
		return timeOrder;
	}


	public void pruneAnnotations() {
		boolean anythingChanged = false;
		// remove all annotations that are marked deleted
		for (TierImpl t : tiers) {
			anythingChanged |= t.pruneAnnotations();
		}

		timeOrder.pruneTimeSlots();
		// HB, 9 aug 02: moved from tier to transcription to because delete
		// usually concerns more than one tier.
		if (anythingChanged) {
			modified(ACMEditEvent.REMOVE_ANNOTATION, null);
		}

	}

	/**
	 * Refined prune method; only the 'source' tier and its dependent tiers will 
	 * be asked to prune their annotations.
	 * 
	 * @param fromTier the tier that might have been changed
	 */
	public void pruneAnnotations(Tier fromTier) {
		pruneAnnotations(fromTier, null);
//		if (fromTier instanceof TierImpl) {
//			final TierImpl fromTierImpl = (TierImpl) fromTier;
//			List<TierImpl> depTiers = fromTierImpl.getDependentTiers();
//			depTiers.add(0, fromTierImpl);
//			for (TierImpl t : depTiers) {
//				t.pruneAnnotations();
//			}
//			timeOrder.pruneTimeSlots();
//			modified(ACMEditEvent.REMOVE_ANNOTATION, null);
//		}
	}
	
	/**
	 * Refined prune method; only the 'source' tier and its dependent tiers will 
	 * be asked to prune their annotations.
	 * 
	 * Note: HS 01-2013 change in order to have the deleted annotation in the ACMEditEvent
	 * 
	 * @param fromTier the tier that has been changed
	 * @param removedAnnotation the annotation that was removed
	 */
	public void pruneAnnotations(Tier fromTier, Annotation removedAnnotation) {
		if (fromTier instanceof TierImpl) {
			final TierImpl fromTierImpl = (TierImpl) fromTier;
			List<TierImpl> depTiers = fromTierImpl.getDependentTiers();
			depTiers.add(0, fromTierImpl);	// prune parent tier first
			boolean somethingChanged = false;
			for (TierImpl t : depTiers) {
				somethingChanged |= t.pruneAnnotations();
			}
			timeOrder.pruneTimeSlots();
			if (somethingChanged) {
				// Note: this way, there is no notification of removal of the child annotations. 
				modified(ACMEditEvent.REMOVE_ANNOTATION, removedAnnotation);
			}
		}
	}
	

	@Override
	public void setAuthor(String theAuthor) {
		author = theAuthor;
	}

	@Override
	public String getAuthor() {
		return author;
	}


	@Override
	public void setLinguisticTypes(List<LinguisticType> theTypes) {
		linguisticTypes = theTypes;
	}


	@Override
	public List<LinguisticType> getLinguisticTypes() {
		return linguisticTypes;
	}

	@Override
	public LinguisticType getLinguisticTypeByName(String name) {
		if (linguisticTypes != null) {
			for (LinguisticType ct : linguisticTypes) {
				if (ct.getLinguisticTypeName().equals(name)) {
				    return ct;
				}
			}
		}
		
		return null;
	}

	@Override
	public void addLinguisticType(LinguisticType theType) {
		linguisticTypes.add(theType);

		modified(ACMEditEvent.ADD_LINGUISTIC_TYPE, theType);
	}


	@Override
	public void removeLinguisticType(LinguisticType theType) {
		linguisticTypes.remove(theType);

		modified(ACMEditEvent.REMOVE_LINGUISTIC_TYPE, theType);
	}


	@Override
	public void changeLinguisticType(LinguisticType linType,
									String newTypeName,
									List<Constraint> constraints,
									String cvName,
									boolean newTimeAlignable,
									String dcId,
									LexiconQueryBundle2 queryBundle) {

		linType.setLinguisticTypeName(newTypeName);
		linType.removeConstraints();
		if (constraints != null) {
			for (Constraint constraint : constraints) {
				linType.addConstraint(constraint);
			}
		}
		linType.setControlledVocabularyName(cvName);
		linType.setTimeAlignable(newTimeAlignable);
		linType.setDataCategory(dcId);
		linType.setLexiconQueryBundle(queryBundle);

		modified(ACMEditEvent.CHANGE_LINGUISTIC_TYPE, linType);
		
		// Notify Tiers, so that they may change the Annotations.
		// Tiers are not listeners to these events.
		for (TierImpl ti : tiers) {
			LinguisticType tlt = ti.getLinguisticType();
			if (tlt == linType) {
				ti.setLinguisticType(tlt);	// also triggers GUI updates
			}
		}
	}

	@Override
	public List<TierImpl> getTiersWithLinguisticType(String typeID) {
		List<TierImpl> matchingTiers = new ArrayList<TierImpl>();

		for (TierImpl t : tiers) {
			if (t.getLinguisticType().getLinguisticTypeName().equals(typeID)) {
				matchingTiers.add(t);
			}
		}

		return matchingTiers;
	}


	/**
	 * <p>MK:02/06/12<br>
	 * Implementing method from interface Transription.
	 * <p>
	 * @param theTierId see there!
	 * @return see there!
	 */
	@Override
	public TierImpl getTierWithId(String theTierId) {
		TierImpl result = null;
	
		for (TierImpl t : tiers) {
	
			if (t.getName().equals(theTierId)) {
				result = t;
				break;
			}
	
		}
	
		return result;
	}
	
	/**
	 * Get a list of TierImpls from a list of TierNames.
	 * 
	 * @param names
	 * @return
	 */
	public List<TierImpl> getTiersWithIds(List<String> names) {
        List<TierImpl> tiers = new ArrayList<TierImpl>(names.size());
        for (String tierName : names) {
        	TierImpl ti = getTierWithId(tierName);
        	if (ti != null) {
        		tiers.add(ti);
        	}
        }
        return tiers;
	}

	/**
	 * <p>MK:02/06/12<br>
	 * Where the name of all tiers are unique for a transcription, this method
	 * returns the tier with the given name.
	 * If no tier matches the given name, null is returned.<br>
	 * Unless tier IDs are introduced, this method and getTierWithId() are identical.
	 * <br>
	 * Non-unique tiernames must be introduced.
	 * </p>
	 * @param name name of tier, as in tier.getName()
	 * @return first tier in transription with given name, or null.
	 */
//	protected final Tier getTierByUniqueName(String name) {
//		if (tiers == null) {
//			return null;
//		}
//		for (Tier t : tiers) {
//			String n = t.getName();
//			if (n.equals(name)) {
//				return t;
//			}
//		}
//		return null;
//	}

	@Override
	public List<Annotation> getAnnotationsUsingTimeSlot(TimeSlot theSlot) {
		List<Annotation> resultAnnots = new ArrayList<Annotation>();

		for (TierImpl t : tiers) {
			resultAnnots.addAll(t.getAnnotationsUsingTimeSlot(theSlot));
		}

		return resultAnnots;
	}


	
	public List<Annotation> getAnnotsBeginningAtTimeSlot(TimeSlot theSlot) {
		List<Annotation> resultAnnots = new ArrayList<Annotation>();

		for (TierImpl t : tiers) {
			resultAnnots.addAll(t.getAnnotsBeginningAtTimeSlot(theSlot));
		}

		return resultAnnots;
	}

	/**
	 * Refined version of getAnnotsBeginningAtTimeSlot(TimeSlot); here only the specified tier 
	 * (optional) and its depending tiers are polled.
	 * @param theSlot the TimeSlot
	 * @param forTier the tier
	 * @param includeThisTier if true annotations on this tier will also be included
	 * @return a Vector containing annotations
	 */
	public List<Annotation> getAnnotsBeginningAtTimeSlot(TimeSlot theSlot, Tier forTier, boolean includeThisTier) {
		List<Annotation> resultAnnots = new ArrayList<Annotation>();

		final TierImpl forTierImpl = (TierImpl) forTier;
		List<TierImpl> depTiers = forTierImpl.getDependentTiers();
		if (includeThisTier) {
			depTiers.add(0, forTierImpl);
		}		
		Iterator<TierImpl> tierIter = depTiers.iterator();

		while (tierIter.hasNext()) {
			TierImpl t = tierIter.next();

			resultAnnots.addAll(t.getAnnotsBeginningAtTimeSlot(theSlot));
		}

		return resultAnnots;
	}

	public List<Annotation> getAnnotsEndingAtTimeSlot(TimeSlot theSlot) {
		List<Annotation> resultAnnots = new ArrayList<Annotation>();
		
		for (TierImpl t : tiers) {
			resultAnnots.addAll(t.getAnnotsEndingAtTimeSlot(theSlot));
		}

		return resultAnnots;
	}
		
	/**
	 * Refined version of getAnnotsEndingAtTimeSlot(TimeSlot); here only the specified tier 
	 * (optional) and its depending tiers are polled.
	 * @param theSlot the TimeSlot
	 * @param forTier the tier
	 * @param includeThisTier if true annotations on this tier will also be included
	 * @return a Vector containing annotations
	 */
	public List<Annotation> getAnnotsEndingAtTimeSlot(TimeSlot theSlot, TierImpl forTier, boolean includeThisTier) {
		List<Annotation>resultAnnots = new ArrayList<Annotation>();
		
		List<TierImpl> depTiers = forTier.getDependentTiers();
		if (includeThisTier) {
			depTiers.add(0, forTier);
		}
		
		for (TierImpl t : depTiers) {

			resultAnnots.addAll(t.getAnnotsEndingAtTimeSlot(theSlot));
		}

		return resultAnnots;
	}

	/**
	 * Iterates over all annotations of time alignable tiers and adds each 
	 * referenced TimeSlot to a HashSet.
	 * 
	 * @return a set of referenced TimeSlots
	 */
	public HashSet<TimeSlot> getTimeSlotsInUse() {
		// HashSet, because it is based on equals() and hashCode(), not compareTo().
		HashSet<TimeSlot> usedSlots = new HashSet<TimeSlot>(getTimeOrder().size());
		
		for (TierImpl t : tiers) {
			if (t.isTimeAlignable()) {
		        for (Annotation annObj : t.getAnnotations()) {
					if (annObj instanceof AlignableAnnotation) {
						AlignableAnnotation aa = (AlignableAnnotation) annObj;
						usedSlots.add(aa.getBegin());
						usedSlots.add(aa.getEnd());
					}
				}
			}
		}
		
		return usedSlots;
	}
	
	@Override
	public List<String> getAnnotationIdsAtTime(long time){
		List<String> resultAnnots = new ArrayList<String>();
		
		for (TierImpl t : tiers) {
			Annotation ann = t.getAnnotationAtTime(time);
			if(ann != null){
				resultAnnots.add(ann.getId());
			}
		}
		
		return resultAnnots;
	}

	/**
	 * This implementation is very inefficient.
	 * If you need more than a single lookup, consider using
	 * getAnnotationsByIdMap().
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Annotation getAnnotationById(String id){
		if(id == null) {
			return null;
		}
		for (TierImpl t : tiers) {
			Annotation a = t.getAnnotationById(id);
			if(a != null) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * Get a Map from annotation Ids to Annotations.
	 * This is useful if you need to look up more than a handful of ids,
	 * since each call to getAnnotationById() looks at half the annotations
	 * on average anyway.
	 * <p>
	 * Implementation note: since HashSet and TreeSet are implemented with
	 * a backing HashMap and TreeMap anyway, there is not much point in
	 * creating just a Set, even for cases where a Set is sufficient.
	 * 
	 * @return
	 */
	public Map<String, Annotation> getAnnotationsByIdMap() {
		Map<String, Annotation> map = new HashMap<String, Annotation>();
		
		for (TierImpl t : tiers) {
			t.getAnnotationsByIdMap(map);
		}
		
		return map;
	}
	
	@Override
	public long getLatestTime() {
		long latestTime = 0;
		
		Iterator<TimeSlot> elmts = getTimeOrder().iterator();
		while (elmts.hasNext()) {
			long t = elmts.next().getTime();
			if (t > latestTime) {
				latestTime = t;
			}
		}		
		return latestTime;
	}
	
	
	
	/**
	 * This method returns all child annotations for a given annotation,
	 * irrespective of which tier it is on. There exists an alternative method
	 * Annotation.getChildrenOnTier(). The main difference is, that getChildAnnotationsOf
	 * does not base itself on ParentAnnotationListeners for the case of AlignableAnnotations.
	 * This is essential during deletion of annotations.
	 * THEREFORE: DO NOT REPLACE THIS METHOD WITH getChildrenOnTier. DELETION OF ANNOTATIONS
	 * WILL THEN FAIL !!!
	 */
	@Override
	public List<Annotation> getChildAnnotationsOf(Annotation theAnnot) {
		List<Annotation> children = new ArrayList<Annotation>();
		if (theAnnot instanceof RefAnnotation) {
			children.addAll(((RefAnnotation) theAnnot).getParentListeners());
		}
		else {	// theAnnot is AlignableAnnotation

			// HB, 6-5-03, to take closed annotation graphs into account

			TreeSet<AlignableAnnotation> connectedAnnots = new TreeSet<AlignableAnnotation>();
			//TreeSet connectedTimeSlots = new TreeSet();
		//	getConnectedAnnots(connectedAnnots, connectedTimeSlots, ((AlignableAnnotation) theAnnot).getBegin());
			// HS mar 06: pass the top tier as well, to prevent iterations over unrelated tiers
			final Tier theAnnotsTier = theAnnot.getTier();
			getConnectedSubtree(connectedAnnots, 
								((AlignableAnnotation) theAnnot).getBegin(),
								((AlignableAnnotation) theAnnot).getEnd(),
								theAnnotsTier);

			List<AlignableAnnotation> connAnnotVector = new ArrayList<AlignableAnnotation>(connectedAnnots);	// 'contains' on TreeSet seems to go wrong in rare cases
																	// I don't understand this, but it works - HB
			
			// <<< end of HB, 6-5-03

			// first collect direct descendant tiers of theAnnot's tier
			List<TierImpl> descTiers = new ArrayList<TierImpl>();

			for (TierImpl t : tiers) {
				//if (t.getParentTier() == theAnnot.getTier()) {
				if (t.hasAncestor(theAnnotsTier)) {
					descTiers.add(t);
				}
			}

			// on these descendant tiers, check all annots if they have theAnnot as parent
			for (TierImpl descT : descTiers) {

				for (Annotation a : descT.getAnnotations()) {
					if (a instanceof RefAnnotation) {
						// HS 29 jul 04: added test on the size of the Vector to prevent 
						// NoSuchElementException
						if (((RefAnnotation) a).getReferences().size() > 0 && 
							((RefAnnotation) a).getReferences().get(0) == theAnnot) {
							children.add(a);
						}
					}
					else if (a instanceof AlignableAnnotation) {												
						if (connAnnotVector.contains(a)) {
							children.add(a);
						} else if (descT.getLinguisticType().getConstraints().getStereoType() == 
						    Constraint.INCLUDED_IN) {
						    // feb 2006: special case for Included_In tier, child annotations are not 
						    // found in the graph tree, test overlap, or more precise inclusion
						    /*
						    if (a.getBeginTimeBoundary() >= theAnnot.getBeginTimeBoundary() &&  
						            a.getEndTimeBoundary() <= theAnnot.getEndTimeBoundary()) {
						        children.add(a);
						    }
						    */
						    // return overlapping instead of included annotations
						    if (a.getBeginTimeBoundary() < theAnnot.getEndTimeBoundary() &&  
						            a.getEndTimeBoundary() > theAnnot.getBeginTimeBoundary()) {
						        children.add(a);
						    }
						    if (a.getBeginTimeBoundary() > theAnnot.getEndTimeBoundary()) {
						        break;
						    }
						}
					}
				}
			}
		}

		return children;
	}

	/**
	 * Annotation based variant of the graph based getConnectedAnnots(TreeSet, TreeSet, TimeSlot).
	 * Annotations on "Included_In" tiers are not discovered in the graph based way.
	 * <p>
	 * Implementation node: the TreeSet is currently untyped but should probably have
	 * <TimeSlotImpl> as type parameter, but that requires a lot of casts in many places,
	 * since many methods such as ann.getBegin() result in a TimeSlot.
	 * 
	 * @param connectedAnnots storage for annotations found
	 * @param connectedTimeSlots storage for slots found: this set is modified
	 * @param fromAnn the parent annotation
	 */	
	public void getConnectedAnnots(TreeSet<AbstractAnnotation> connectedAnnots, TreeSet/*<TimeSlotImpl>*/ connectedTimeSlots, 
	        AlignableAnnotation fromAnn) {
	    // first get the annotations and slots, graph based
	    getConnectedAnnots(connectedAnnots, connectedTimeSlots, fromAnn.getBegin());
	    // then find Included_In dependent tiers
	    TierImpl t = (TierImpl) fromAnn.getTier();
	    List<TierImpl> depTiers = t.getDependentTiers();
	    Iterator<TierImpl> dtIt = depTiers.iterator();
	    TierImpl tt;
	    while (dtIt.hasNext()) {
	        tt = dtIt.next();
	        if (tt.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN) {
	            //anns = tt.getOverlappingAnnotations(fromAnn.getBeginTimeBoundary(), fromAnn.getEndTimeBoundary());
	            for (AlignableAnnotation aa : tt.getAlignableAnnotations()) { // iterations over all annotations on the tier...
	                if (fromAnn.isAncestorOf(aa)) {
	                    getConnectedAnnots(connectedAnnots, connectedTimeSlots, 
	        	                       aa);
	                }
	                //getConnectedAnnots(connectedAnnots, connectedTimeSlots, 
	                //        ((AlignableAnnotation) anIt.next()).getBegin());
	            }
	        }
	        
	    }
	}
	
	public void getConnectedAnnots(TreeSet<AbstractAnnotation> connectedAnnots, TreeSet<TimeSlot/*Impl*/> connectedTimeSlots, TimeSlot/*Impl*/ startingFromTimeSlot) {

		List<Annotation> annots = null;
		connectedTimeSlots.add(startingFromTimeSlot);

		// annots = getAnnotsBeginningAtTimeSlot(startingFromTimeSlot);
		annots = getAnnotationsUsingTimeSlot(startingFromTimeSlot);

		if (annots != null) {
			//connectedAnnots.addAll(annots);
			
			Iterator<Annotation> aIter = annots.iterator();
			while (aIter.hasNext()) {
				AlignableAnnotation aa = (AlignableAnnotation) aIter.next();
				boolean added = connectedAnnots.add(aa);
				if (!added) {
				    continue;
				}

				if (!connectedTimeSlots.contains(aa.getBegin())) {
					getConnectedAnnots(connectedAnnots, connectedTimeSlots, aa.getBegin());
				}
				if (!connectedTimeSlots.contains(aa.getEnd())) {
					getConnectedAnnots(connectedAnnots, connectedTimeSlots, aa.getEnd());
				}
			}
		}
	}

	/**
	 * Find all annotations part of the (sub) graph, time slot based.
	 * HS mar 06: the top level tier for the search can be specified to prevent 
	 * iterations over unrelated tiers.
	 * 
	 * @param connectedAnnots TreeSet to add found annotations to (in/out)
	 * @param startingFromTimeSlot begin time slot of the subtree
	 * @param stopTimeSlot end time slot of the sub tree
	 * @param topTier the top level tier for the subtree search (can be a dependent tier though)
	 * @return true when the end time slot has been reached, false otherwise
	 */
	public boolean getConnectedSubtree(TreeSet<AlignableAnnotation> connectedAnnots, 
									TimeSlot startingFromTimeSlot,
									TimeSlot stopTimeSlot,
									Tier topTier) {
		List<Annotation> annots = null;
		boolean endFound = false;
		
		if (topTier == null) {
		    annots = getAnnotsBeginningAtTimeSlot(startingFromTimeSlot); 
		} else {
		    annots = getAnnotsBeginningAtTimeSlot(startingFromTimeSlot, topTier, true);
		}
		

		if (annots != null) {
			Iterator<Annotation> aIter = annots.iterator();
			while (aIter.hasNext()) {
				AlignableAnnotation aa = (AlignableAnnotation) aIter.next();

				if (aa.getEnd() != stopTimeSlot) {
					//endFound = getConnectedSubtree(connectedAnnots, aa.getEnd(), stopTimeSlot, topTier);
				    endFound = getConnectedSubtree(connectedAnnots, aa.getEnd(), stopTimeSlot, aa.getTier());
					//if (endFound) {// HS Feb 2016: removing this check seems to resolve some editing problems in case of subdivision of subdivision tiers
						connectedAnnots.add(aa);
					//}
				}
				else {
					endFound = true;
					connectedAnnots.add(aa);
				}
			}
		}
		
		return endFound;
	}


	public boolean eachRootHasSubdiv() {
		boolean eachRootHasSubdiv = true;

			List<TierImpl> topTiers = getTopTiers();
			Iterator<TierImpl> tierIter = topTiers.iterator();

			while (tierIter.hasNext()) {
				TierImpl t = tierIter.next();

				for (AbstractAnnotation a : t.getAnnotations()) {

					List<Annotation> children = a.getParentListeners();
					if ((children == null) || (children.size() == 0)) {
						return false;
					}
					else {
						boolean subdivFound = false;
						Iterator<Annotation> childIter = children.iterator();
						while (childIter.hasNext()) {
							Annotation ch = childIter.next();
							Constraint constraint = ((TierImpl) ch.getTier()).getLinguisticType().getConstraints();

							if (	(constraint.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) ||
								(constraint.getStereoType() == Constraint.TIME_SUBDIVISION)) {

								subdivFound = true;
								break;
							}
						}

						if (!subdivFound) {
							return false;
						}
					}

				}
			}


		return eachRootHasSubdiv;
	}


	/**
	 * Returns whether any modifications are made since the last reset (when saving)
	 */
	@Override
	public boolean isChanged() {
		return changed;
	}


	/**
	 * Resets 'changed' status to unchanged.
	 * Dec 2009 HS also set the Controlled Vocabularies to unchanged
	 */
	@Override
	public void setUnchanged() {
		changed = false;
		
		if (controlledVocabularies != null) {
			ControlledVocabulary cv;
			for (int i = 0; i < controlledVocabularies.size(); i++) {
				cv = controlledVocabularies.get(i);
				cv.setChanged(false);
			}
		}
//		updateWindowChangedMarker();
	}


	/**
	 * Sets 'changed' status to changed
	 */
	@Override
	public void setChanged() {
//		if (!changed) {
			changed = true;
//			updateWindowChangedMarker();
//		}
	}

	/**
	 * This MacOS specific function will show the red close button with
	 * a black dot inside if the document is modified.
	 * It will also grey out the document proxy icon (also MacOS specific).
	 * TODO: change this in some way that the this 'server' side does not
	 * need to call the 'client' side. Maybe create an UNCHANGED ACMEdited 
	 * event and add the ElanFrame2 as a listener?
	 */
//	private void updateWindowChangedMarker() {
//		if (SystemReporting.isMacOS()) {
//			final JFrame rootFrame = ELANCommandFactory.getRootFrame(this);
//			if (rootFrame != null) {
//				rootFrame.getRootPane().
//					putClientProperty("Window.documentModified", Boolean.valueOf(changed));
//			}
//		}
//	}
	
	/**
	 * Returns time Change Propagation Mode (normal, bulldozer or shift)
	 * 
	 * @author hennie
	 */
	@Override
	public int getTimeChangePropagationMode() {
		return timeChangePropagationMode;
	}
	
	/**
	 * Set Time Change Propagation Mode (normal, bulldozer or shift)
	 * @author hennie
	 */
	@Override
	public void setTimeChangePropagationMode(int theMode) {
		timeChangePropagationMode = theMode;
	}
	
	/**
	 * Propagate time changes by shifting all next annotations to later times,
	 * maintaining gaps. Algorithm: shift all time slots after end of fixedAnnotation
	 * over distance newEnd minus oldEnd.
	 * 
	 * @param fixedAnnotation the source annotation 
	 * @param fixedSlots slots connected to the source annotation that should not be shifted
	 * @param oldBegin the old begin time of the annotation
	 * @param oldEnd the old end time of the annotation
	 */
	public void correctOverlapsByShifting(AlignableAnnotation fixedAnnotation, List<TimeSlot> fixedSlots, 
		long oldBegin, long oldEnd) {
		long newEnd = fixedAnnotation.getEnd().getTime();
		
		// right shift
		if (newEnd > oldEnd) {	// implies that end is time aligned
			long shift = newEnd - oldEnd;
			
			List<TimeSlot> otherFixedSLots = getOtherFixedSlots(oldBegin, fixedAnnotation.getTier().getName());
			if(otherFixedSLots != null){
				for(int i=0; i <otherFixedSLots.size(); i++){
					if(!fixedSlots.contains(otherFixedSLots.get(i))){
						fixedSlots.add(otherFixedSLots.get(i));
					}
				}
			}
			
			getTimeOrder().shift(oldBegin, shift, fixedAnnotation.getEnd(), fixedSlots);
		}
	}	
	
	/**
	 * Returns the a list of extra time slots which should 
	 * not be shifted. (Time slots of overlapping annotations
	 * which should not be changed)
	 * 
	 * @param from, starting point for shifting
	 * @param excludeTimeSlotsFromTier, current tier on which the 
	 * @return vector of other fixed time slots
	 */
	private List<TimeSlot> getOtherFixedSlots(long from, String excludeTimeSlotsFromTier){
		List<TierImpl> tiers = this.getTiers();
		
		List<TimeSlot> fixedSlots = new ArrayList<TimeSlot>();
		
		for(int t=0; t<tiers.size(); t++){
			TierImpl tier = tiers.get(t);
			if(tier.getLinguisticType().getConstraints() == null){
				if(excludeTimeSlotsFromTier != null && tier.getName().equals(excludeTimeSlotsFromTier)){
					continue;
				}
				
				for (AlignableAnnotation ann : tier.getAlignableAnnotations()) {
					
					if(ann.getBeginTimeBoundary() > from){
						break;
					}
					
					if(ann.getBeginTimeBoundary() < from && ann.getEndTimeBoundary() > from){
						if(!fixedSlots.contains(ann.endTime)){
							fixedSlots.add(ann.endTime);
						}
						List<TierImpl> dependingdTiers = tier.getDependentTiers();
						if(dependingdTiers != null){
							for(int i= 0; i< dependingdTiers.size(); i++){
								TierImpl dependTier = dependingdTiers.get(i);
								if(dependTier.getLinguisticType().getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION || 
										dependTier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN){
									List<Annotation> childAnnotations = ann.getChildrenOnTier(dependTier);
									
									if(childAnnotations != null){
										for(int a = 0; a < childAnnotations.size(); a++){
											AlignableAnnotation childAnn = (AlignableAnnotation) childAnnotations.get(a);
											if(childAnn.getBeginTimeBoundary() > from){
												if(!fixedSlots.contains(childAnn.beginTime)){
													fixedSlots.add(childAnn.beginTime);
												}
											}
												
											if(childAnn.getEndTimeBoundary() > from){
												if(!fixedSlots.contains(childAnn.endTime)){
													fixedSlots.add(childAnn.endTime);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}		
		return fixedSlots;
	}
	
	/**
	 * Propagate time changes by shifting time slots.<br> 
	 * <b>Note: </b> this method is intended only to be used to 
	 * reverse the effects of an earlier call to <code>correctOverlapsByShifting
	 * </code>, as in an undo operation!
	 * 
	 * @see #correctOverlapsByShifting(AlignableAnnotation, long, long)
	 * @param from starting point for shifting
	 * @param amount the distance to shift the timeslots
	 */
	public void shiftBackward(long from, long amount) {
		if (amount < 0) {	
			getTimeOrder().shift(from, amount, null, getOtherFixedSlots(from, null));
			// let listeners know the whole transcription 
			// could be changed
			modified(ACMEditEvent.CHANGE_ANNOTATIONS, null);
		}		
	}
	
	/**
	 * Shifts all aligned timeslots with the specified amount of ms.<br>
	 * When an attempt is made to shift slots such that one or more slots 
	 * would have a negative time value an exception is thrown. 
	 * Slots (and annotations) will never implicitely be deleted.
	 * The shift operation is delegated to th TimeOrder object.
	 * 
	 * @param shiftValue the number of ms to add to the time value of aligned timeslots, 
	 *    can be less than zero
	 * @throws IllegalArgumentException when the shift value is such that any aligned 
	 *    slot would get a negative time value 
	 */
	public void shiftAllAnnotations(long shiftValue) throws IllegalArgumentException {
		timeOrder.shiftAll(shiftValue);
		// notify
		modified(ACMEditEvent.CHANGE_ANNOTATIONS, null); 
	}

	/**
	     * Returns all Tiers from this Transcription.
	     *
	     * @return returns the list of tiers
	     */
	@Override
	public List<TierImpl> getTiers() {

	    return tiers;
	}


	/**
	 * Check if an Object is equal to this Transcription. For DOBES equality is
	 * true if the names of two Transcriptions are the same.
	 *
	 * @param obj the object to compare with
	 *
	 * @return true if the name of the two transcriptions are identical
	 */
	@Override
	public boolean equals(Object obj) {

	        if (obj instanceof TranscriptionImpl &&
	                (((TranscriptionImpl) obj).getName() == name)) {
	            return true;
	        }
	
	    return false;
	}

	/**
     * Returns the file path.
     *
     * @return the file path
     */
	public String getPathName() {
	    return fileName;
	}

	/**
     * Sets the file path.
     *
     * @param theFileName the file path
     */
	public void setPathName(String theFileName) {
	    theFileName = FileUtility.urlToAbsPath(theFileName);
	    fileName = theFileName;
	    url = FileUtility.pathToURLString(fileName);
	    name = FileUtility.fileNameFromPath(theFileName);
	}

	/**
     * Obsolete. Sets the main media file.
     *
     * @param pathName the path to the main media file
     */
//	@Override
//	public void setMainMediaFile(String pathName) {
//	}

	/******** support for controlled vocabularies  ******/
	
	/**
	 * Sets the collection of ControlledVocabularies known to this Transcription.
	 * 
	 * @param controlledVocabs the CV's for this transcription
	 */
	@Override
	public void setControlledVocabularies(List<ControlledVocabulary> controlledVocabs) {
		if (controlledVocabs != null) {
			controlledVocabularies = controlledVocabs;
		}
		//called at parse/construction time, don't call modified 		
	}
	
	/**
	 * Returns the collection of controlled vocabularies known to this Transcription.
	 * If there are no CV's an empty List is returned.
	 * 
	 * @return the list of associated cv's
	 */
	@Override
	public List<ControlledVocabulary> getControlledVocabularies() {
		return controlledVocabularies;
	}
	
	/**
	 * Returns the ControlledVocabulary with the specified name if it exists
	 * in the Transcription or null otherwise.
	 * 
	 * @param name the name of the cv
	 * 
	 * @return the CV with the specified name or <code>null</code>
	 */
	@Override
	public ControlledVocabulary getControlledVocabulary(String name) {
		if (name == null) {
			return null;	
		}
		ControlledVocabulary conVoc = null;
		for (int i = 0; i < controlledVocabularies.size(); i++) {
			conVoc = controlledVocabularies.get(i);
			if (conVoc.getName().equalsIgnoreCase(name)) {
				break;
			} else {
				conVoc = null;
			}
		}
		return conVoc;
	}
	
	/**
	 * Adds the specified CV to the list of cv's if it is not already in the list 
	 * and if there is not already a cv with the same name in the list.
	 * 
	 * @param cv the ControlledVocabulary to add
	 */
	public void addControlledVocabulary(ControlledVocabulary cv) {
		if (cv == null) {
			return;
		}
		ControlledVocabulary conVoc;
		for (int i = 0; i < controlledVocabularies.size(); i++) {
			conVoc = controlledVocabularies.get(i);
			if ( conVoc == cv || conVoc.getName().equalsIgnoreCase(cv.getName())) {
				return;
			}
		}
		controlledVocabularies.add(cv);
		// register as a listener for modifications
		// A.K. was never used -> removed 
		cv.setACMEditableObject(this);
		
		if (isLoaded) {		
			modified(ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY, cv);
		}		
	}
	
	/**
	 * Removes the specified CV from the list.<br>
	 * Any LinguisticTypes that reference the specified ControlledVocabulary will 
	 * have their reference set to <code>null</code>.
	 * 
	 * @param cv the CV to remove from the list
	 */
	public void removeControlledVocabulary(ControlledVocabulary cv) {
		if (cv == null) {
			return;
		}
		List<LinguisticType> types = getLinguisticTypesWithCV(cv.getName());
		for (LinguisticType lt : types) {
			lt.setControlledVocabularyName(null);
		}
		
		cv.removeACMEditableObject();
		controlledVocabularies.remove(cv);

		modified(ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY, cv);
	}
	
	/**
	 * Replaces the specified CV with a new one with the same name.<br>
	 * This is used for undo/redo of big changes to a CV.
	 * 
	 * @param cv the CV to replace from the list
	 */
	public void replaceControlledVocabulary(ControlledVocabulary cv) {
		if (cv == null) {
			return;
		}
		ControlledVocabulary oldCV = getControlledVocabulary(cv.getName());
		if (oldCV == null) {
			return;
		}
		
		oldCV.removeACMEditableObject();
		controlledVocabularies.remove(oldCV);
		controlledVocabularies.add(cv);
		cv.setACMEditableObject(this);

		modified(ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY, cv);
	}
	
	/**
	 * Updates the name and description of the specified CV and updates the Linguistic 
	 * Types referencing this CV, if any.<br> The contents of a ControlledVocabulary is changed 
	 * directly in an editor; the CV class insures it's integrity. The editor class 
	 * should call setChanged on the Transcription object.<br><br>
	 * Pending: The moment updating existing annotation values is offered as an option, 
	 * this implementation has to been changed.
	 * 
	 * @param cv the cv that has been changed or is to be changed.
	 * @param name the new name of the cv
	 * @param description the description for the cv
	 */
	public void changeControlledVocabulary(ControlledVocabulary cv , String name, 
		int langIndex, String description/*, List entries*/) {
		boolean newChange = false;
		String oldName = cv.getName();
		String oldDescription = cv.getDescription(langIndex);
		// doublecheck on the name
		ControlledVocabulary conVoc;
		for (int i = 0; i < controlledVocabularies.size(); i++) {
			conVoc = controlledVocabularies.get(i);
			if ( conVoc != cv && conVoc.getName().equalsIgnoreCase(name)) {
				return;
			}
		}
		if (!oldName.equals(name)) {			
			newChange = true;
			List<LinguisticType> types = getLinguisticTypesWithCV(oldName);
			for (int i = 0; i < types.size(); i++) {
				types.get(i).setControlledVocabularyName(name);
			}
			cv.setName(name);
		}
		if (oldDescription == null || !oldDescription.equals(description)) {
			cv.setDescription(langIndex, description);
			newChange = true;
		}
		
		if (newChange) {
			modified(ACMEditEvent.CHANGE_CONTROLLED_VOCABULARY, cv);
		}
	}
	
	/**
	 * To be called after externally defined Controlled Vocabularies have been 
	 * loaded. Checks whether annotations that link to an entry in such a CV 
	 * have to be updated as a result of a change in the CV.
	 */
	public void checkAnnotECVConsistency() {
		for (TierImpl currentTier : tiers) {
			Pair<ControlledVocabulary, Integer> pair = currentTier.getEffectiveLanguage();
			
			if (pair == null) {
				continue;
			}
			
			ControlledVocabulary cv = pair.getFirst();
			int langIndex = pair.getSecond();
			
			if (cv instanceof ExternalCV && langIndex >= 0) {
				ExternalCV ecv = (ExternalCV) cv;
				
				if (!ecv.isLoadedFromURL() && !ecv.isLoadedFromCache()) {
					continue;
				}
				
				for (AbstractAnnotation currentAnn : currentTier.getAnnotations()) {
					String cvEntryRefId = currentAnn.getCVEntryId();
					if (cvEntryRefId == null) {
						continue;
					}
					
					CVEntry entry = ecv.getEntrybyId(cvEntryRefId);
					String value;
					if (entry != null && 
							(value = entry.getValue(langIndex)) != null &&
							!value.equals(currentAnn.getValue())) {
						currentAnn.setValue(value);
						setChanged();
					} else if (entry == null) {
						// The entry the External Ref. refers to is no longer
						// there, so remove the CV Entry ID from the annotation
						currentAnn.setCVEntryId(null);
						setChanged();
					}
				}
			}
		}
	} 
	
	/**
	 * Finds the LinguisticTypes that hold a reference to the CV with the
	 * specified name and returns them in a List.
	 * 
	 * @param name
	 *            the identifier of the ControlledVocabulary
	 * @return a list of linguistic types
	 */
	@Override
	public List<LinguisticType> getLinguisticTypesWithCV(String name) {
		List<LinguisticType> matchingTypes = new ArrayList<LinguisticType>();

		for (LinguisticType lt : linguisticTypes) {
			String cvName = lt.getControlledVocabularyName();
			if (cvName != null && cvName.equalsIgnoreCase(name)) {
				matchingTypes.add(lt);
			}
		}
		
		return matchingTypes;
	}
	
	/**
	 * Finds the tiers with a linguistic type that references the Controlled Vocabulary
	 * with the specified name.
	 * 
	 * @see #getLinguisticTypes(String)
	 * 
	 * @param name the identifier of the ControlledVocabulary
	 * 
	 * @return a list of all tiers using the specified ControlledVocabulary
	 */
	public List<TierImpl> getTiersWithCV(String name) {
		List<TierImpl> matchingTiers = new ArrayList<TierImpl>();
		if (name == null || name.length() == 0) {
			return matchingTiers;
		}
		
		List<LinguisticType> types = getLinguisticTypesWithCV(name);
		if (types.size() > 0) {
			for (LinguisticType type : types) {
				List<TierImpl> tv = getTiersWithLinguisticType(type.getLinguisticTypeName());
				if (tv.size() > 0) {
					matchingTiers.addAll(tv);
				}					
			}
		}
		return matchingTiers;
	}
	
	public boolean allRootAnnotsUnaligned() {
		// used when importing unaligned files from for example Shoebox or CHAT		
		
			List<TierImpl> topTiers = getTopTiers();
			Iterator<TierImpl> tierIter = topTiers.iterator();
	
			while (tierIter.hasNext()) {
				TierImpl t = tierIter.next();
	
				for (AlignableAnnotation a : t.getAlignableAnnotations()) {
					if (a.getBegin().isTimeAligned() || a.getEnd().isTimeAligned()) {
						return false;
					}
				}
			}	
		
		return true;
	}
	
	public void alignRootAnnots() {
		List<TimeSlot> rootTimeSlots = new ArrayList<TimeSlot>();
		
		// collect timeslots of root annotations
	
			List<TierImpl> topTiers = getTopTiers();
			Iterator<TierImpl> tierIter = topTiers.iterator();
	
			while (tierIter.hasNext()) {
				TierImpl t = tierIter.next();
	
				for (AlignableAnnotation a : t.getAlignableAnnotations()) {
					
					rootTimeSlots.add(a.getBegin());
					rootTimeSlots.add(a.getEnd());					
				}
			}		
		
		// align at regular intervals. Assume that slots belong to one
		// annotation pairwise		
		int cnt = 0;
		
		Object[] tsArray = rootTimeSlots.toArray();
		Arrays.sort(tsArray);
		
		for (int i = 0; i < tsArray.length; i++) {
			TimeSlot ts = (TimeSlot) tsArray[i];
			if (i%2 == 0) {
				ts.setTime(1000*cnt++);
			}
			else {
				ts.setTime(1000*cnt);
			}
		}
	}

	/**
	 * MK:02/06/27<br>
	 * A transcription contain tiers without parent-tiers. 
	 * @return List of TierImpl without parent-tier
	 */
	public final List<TierImpl> getTopTiers() {
		List<TierImpl> result = new ArrayList<TierImpl>();
		for (TierImpl t : getTiers()) {
			// the parent tier
			Tier dad = t.getParentTier();
			if (dad == null) {
				result.add(t);
			} else {
				// System.out.println(" -- tierpa " + dad.getName());
			}
		}
		return result;
	}

	/*
	protected void finalize() throws Throwable {
	    System.out.println("Finalize Transcription...");
	    super.finalize();
	}
	*/
	
	/**
	 * Adds all Property objects in the given list to the list of document properties
	 * if they are not already in that list.
	 * 
	 * @param props a list of property objects to add
	 */
	@Override
	public void addDocProperties(List<Property> props) {
		if (props != null) {
			Property prop;
			for (int i = 0; i < props.size(); i++) {
				prop = props.get(i);
				if (prop instanceof Property && !docProperties.contains(prop)) {
					docProperties.add(prop);
				}
			}
		}
	}
	
	/**
	 * Adds a single document property object to the list.
	 * 
	 * @param prop the property
	 */
	@Override
	public void addDocProperty(Property prop) {
		if (prop != null && !docProperties.contains(prop)) {
			docProperties.add(prop);
		}
	}
	
	/**
	 * Removes the specified object from the list.
	 * 
	 * @param prop the Property to remove
	 * @return true if it was in the list, false otherwise
	 */
	@Override
	public boolean removeDocProperty(Property prop) {
		if (prop != null) {
			return docProperties.remove(prop);
		}
		
		return false;
	}
	
	/**
	 * Returns the ArrayList containing the document properties.
	 * Note: it is not a copy of the list.
	 * 
	 * @return the list of document properties
	 */
	@Override
	public List<Property> getDocProperties() {
		return docProperties;
	}
	
	/**
	 * Empties the document properties list.
	 */
	public void clearDocProperties() {
		docProperties.clear();
	}
	
	public Property getDocProperty(String name) {
		List<Property> props = getDocProperties();
		for (Property p : props) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	/******** support for lexicon links ******/
	
	/**
	 * Return the LexiconServiceClientFactories of this transcription
	 * 
	 * @return the lexiconServiceClientFactories of this transcription, can be null!
	 * @author Micha Hulsbosch
	 */
	public Map<String, LexiconServiceClientFactory> getLexiconServiceClientFactories() {
		return lexiconServiceClientFactories;
	}
	
	/**
	 * Adds a client factory to the map.
	 * 
	 * @param name name of the service factory
	 * @param fact the service client factory
	 */
	public void addLexiconServiceClientFactory(String name, LexiconServiceClientFactory fact) {
		if (lexiconServiceClientFactories == null) {
			lexiconServiceClientFactories = new HashMap<String, LexiconServiceClientFactory>(6);
		}
		lexiconServiceClientFactories.put(name, fact);
		lexiconServicesLoaded = true; // adding a factory means loading has been performed
	}

	/**
	 * 
	 * @param name the name of the service factory to remove
	 */
	public void removeLexiconServiceClientFactory(String name) {
		if (lexiconServiceClientFactories != null) {
			lexiconServiceClientFactories.remove(name);
		}
	}
	
	/** 
	 * Returns whether client factories for lexicon services have been loaded,
	 * or at least have been attempted to be loaded.
	 * Allows for "lazy" loading of the factories.
	 * 
	 * @return true if loading has been tried
	 */
	public boolean isLexiconServicesLoaded() {
		return lexiconServicesLoaded;
	}
	
	/**
	 * After loading has been performed  (or has been attempted), 
	 * this flag can be set to true.
	 * 
	 * @param lexiconServicesLoaded
	 */
	public void setLexiconServicesLoaded(boolean lexiconServicesLoaded) {
		this.lexiconServicesLoaded = lexiconServicesLoaded;
	}

	/**
	 * Return the Lexicon Links of this transcription
	 * @return the Lexicon Links of this transcription
	 * @author Micha Hulsbosch
	 */
	@Override
	public HashMap<String, LexiconLink> getLexiconLinks() {
		return lexiconLinks;
	}

	/**
	 * Adds a Lexicon Link to this transcription
	 * @param the Lexicon Link to add
	 * @author Micha Hulsbosch
	 */
	@Override
	public void addLexiconLink(LexiconLink link) {
		lexiconLinks.put(link.getName(), link);
		if (isLoaded) {		
			modified(ACMEditEvent.ADD_LEXICON_LINK, link);
		}
	}


		/**
		 * Remove a Lexicon Link from this transcription
		 * @param the Lexicon Link
		 * @author Micha Hulsbosch
		 */
		@Override
		public void removeLexiconLink(LexiconLink link) {
			for(LinguisticType type : linguisticTypes) {
				if(type.isUsingLexiconQueryBundle() && 
						type.getLexiconQueryBundle().getLink().getName().equals(link.getName())) {
					type.setLexiconQueryBundle(null);
				}
			}
			
			lexiconLinks.remove(link.getName());
			if (isLoaded) {		
				modified(ACMEditEvent.DELETE_LEXICON_LINK, link);
			}
		}

	/**
	 * Returns the Lexicon Link that has a certain name or null if does not exist
	 * @param the name of the Lexicon Link
	 * @return the Lexicon Link that has a certain name or null if does not exist
	 * @author Micha Hulsbosch
	 */
	@Override
	public LexiconLink getLexiconLink(String linkName) {
		return lexiconLinks.get(linkName);
	}

	/**
	 * Returns the Linguistic Types that use a certain Lexicon Link
	 * @param the name of the Lexicon Link
	 * @return the Linguistic Types that use a certain Lexicon Link
	 * @author Micha Hulsbosch
	 */
	public List<LinguisticType> getLinguisticTypesWithLexLink(String name) {
		List<LinguisticType> types = new ArrayList<LinguisticType>();
		for(LinguisticType type : linguisticTypes) {
			if(type.isUsingLexiconQueryBundle() && 
					type.getLexiconQueryBundle().getLink().getName().equals(name)) {
				types.add(type);
			}
		}
		return types;
	}
	
	/**
	 * Gets a unique name for this transcription.
	 * If there isn't one yet, invent one.
	 * This function should preferably be called before saving a transcription,
	 * to ensure that each file has one.
	 * @return
	 */
	@Override
	public URI getURN() {
		if (cachedURNValue == null) {
			List<Property> props = getDocProperties();
			for (Property p : props) {
				if (p.getName().equals(URN_PROPERTY_NAME)) {
					try {
						cachedURNValue = new URI((String)p.getValue());
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
			/* If it wasn't in the document's properties, invent one */
			if (cachedURNValue == null) {
				createNewURN(null);
			}
		}
		
		return cachedURNValue;
	}
	
	/**
	 * Although this method it public, it should be called only in very limited
	 * circumstances.
	 * Its use-case is for "Create Transcription Files For Media Files" which
	 * stores a single Transcription multiple times, modifying it a bit each time.
	 */
	public void createNewURN() {
		List<Property> props = getDocProperties();
		for (Property p : props) {
			if (p.getName().equals(URN_PROPERTY_NAME)) {
				createNewURN(p);
				return;
			}
		}
		
		/* If it wasn't in the document's properties, invent one */
		createNewURN(null);
	}
	
	private void createNewURN(Property overwrite) {
		try {
			cachedURNValue = new URI(URN_PREFIX + UUID.randomUUID());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if (overwrite != null) {
			overwrite.setValue(cachedURNValue);
		} else {
			Property p = new PropertyImpl(URN_PROPERTY_NAME, cachedURNValue);
			addDocProperty(p);
		}
	}
	
	/**
	 * Change the CV language in all tiers of this transcription.
	 * Checks if the new language is the same as the old one,
	 * unless it is empty or null.
	 * <p>
	 * Also change the default language of all the CVs in this transcription.
	 * 
	 * @param newLanguage the new language to use.
	 * @param force if true, don't skip the work if the language is the same as before.
	 */
	public void updateCVLanguage(String newLanguage, boolean force) {
		if (newLanguage == null || newLanguage.isEmpty()) {
			return;
		}
		
		if (!force && currentCVLanguage.equals(newLanguage)) {
			return;
		}
	
		currentCVLanguage = newLanguage;
		
		for (ControlledVocabulary cv : getControlledVocabularies()) {
			cv.setPreferenceLanguage(newLanguage);
		}
		
		setNotifying(false);
		for (Tier t : tiers) {
			((TierImpl) t).updateCVLanguage();
		}
		setNotifying(true);
	}
	
	/**
	 * The preferences keep us informed of the preferred language.
	 * <p>
	 * @see TranscriptionImpl#updateCVLanguage(String, boolean) 
	 * @return the current preferred language for multi-lingual content
	 */
	public String getCVLanguage() {
		return currentCVLanguage;
	}

	@Override
	public List<LicenseRecord> getLicenses() {
		return licenses;
	}
	
	@Override
	public void setLicenses(List<LicenseRecord> licenses) {
		this.licenses = licenses;		
	}
	
	/**
	 * Check the tiers and the annotations for being consistent.
	 * Currently, the following checks are performed:
	 * <ul>
	 * <li>TimeSlots are not shared where they should not be
	 * <li>start time <= end time
	 * <li>comments don't start with diacritics without some letter to combine them with.
	 * </ul>
	 * 
	 * TODO: decide how to show the results to the user.
	 * 
	 * @return a String which lists all complaints. May be empty but not null.
	 */
	public String checkConsistency() {
		StringBuilder complaints = new StringBuilder();
		FixTimeslots fixTS = new FixTimeslots();
		
		List<TierImpl> tiers = getTiers();
		for (TierImpl t : tiers) {
			if (t.isTimeAlignable()) {
				fixTierTimeSlots(t, fixTS);
				checkTierTimes(t, complaints);
			}
			checkTierAnnotations(t, complaints);
		}
		return complaints.toString();
	}
	
	/**
	 * Make sure that different tier hierarchies don't share TimeSlots.
	 * Shared TimeSlots mean that two times are <b>by definition of the constraint</b>
	 * the same, not that they are so by coincidence.
	 * <p>
	 * However this can easily be fixed by creating some duplicate timeslots.
	 * 
	 * @param t the TierImpl to check
	 * @param fixTS keeps track of timeslots and replacements
	 */
	private void fixTierTimeSlots(TierImpl t, FixTimeslots fixTS) {
		TierImpl localRootTier = getLocalRoot(t);
		
        for (Annotation annObj : t.getAnnotations()) {
			if (annObj instanceof AlignableAnnotation) {
				AlignableAnnotation aa = (AlignableAnnotation) annObj;
				
				aa.setBegin(fixTS.fixTimeSlot(localRootTier, aa.getBegin()));
				aa.setEnd(fixTS.fixTimeSlot(localRootTier, aa.getEnd()));
			}
		}
	}
	
	/**
	 * Find the "local root tier" for a given tier.
	 * <p>
	 * The local root tier is the topmost parent of all child tiers that may share
	 * TimeSlots (of course there may be subtrees in there that are separate
	 * sharing trees, if any of the children qualify for being a local root themselves).
	 * @param t
	 * @return
	 */
	private TierImpl getLocalRoot(TierImpl t) {
		Constraint s = t.getLinguisticType().getConstraints();
		if (s != null && s.getStereoType() == Constraint.INCLUDED_IN) {
			// an Included In tier does not share timeslots with its parent.
			return t;
		}
		// If a Tier has no Constraint it is likely a root Tier. Use this invariant???
		TierImpl parent = t.getParentTier();
		if (parent == null) {
			return t;
		}
		return getLocalRoot(parent);
	}

	/**
	 * Local helper class to encapsulate the information that is used
	 * when fixing over-shared TimeSlots.
	 * <p>
	 * First, there is timeslotsInUse which maps the TimeSlots to the root tier
	 * where they belong, i.e. were first seen. If the same TimeSlot is later
	 * seen with a different root tier, it needs to be replaced there.
	 * <p>
	 * That is where the second map, replacementTimeSlot, comes in.
	 * For each subsequent root tier where the same TimeSlot is seen, a new
	 * duplicate TimeSlot needs to be made. This map records which duplicate
	 * goes with which <TimeSlot, root tier> combination.
	 */
	private class FixTimeslots {
		Map<TimeSlot, TierImpl> timeslotsInUse;
		Map<Pair<TimeSlot, TierImpl>, TimeSlot> replacementTimeSlot;
		TimeOrder timeOrder;

		FixTimeslots() {
			timeslotsInUse = new HashMap<TimeSlot, TierImpl>();
			replacementTimeSlot = new HashMap<Pair<TimeSlot, TierImpl>, TimeSlot>();
			timeOrder = getTimeOrder();
		}
		
		/**
		 * Check if the given TimeSlot ts has already been used on an unrelated Tier.
		 * If there is improper sharing, create a new TimeSlot and return it.
		 * Otherwise, if there is no problem, return ts unchanged.
		 * <p>
		 * Remember any mapping that is made so it can be used again later when appropriate.
		 * @param rootTier the root tier under which ts is used
		 * @param ts the timeslot to check/fix
		 */
		private TimeSlot fixTimeSlot(TierImpl rootTier, TimeSlot ts) {
			
			TierImpl rootTierUsedBefore = timeslotsInUse.get(ts);
			
			if (rootTierUsedBefore == null) {
				// This timeslot has not been seen yet.
				// Remember for later in which tier tree it belongs.
				timeslotsInUse.put(ts, rootTier);
				// No replacement necessary.
				return ts;
			}
			
			if (rootTierUsedBefore == rootTier) {
				// Used in the same tier tree, that's ok.
				return ts;
			}
			
			// Check if this timeslot was replaced before (in this tier tree),
			// and if so with what.
			Pair<TimeSlot, TierImpl> p = Pair.makePair(ts, rootTier);
			TimeSlot newTS = replacementTimeSlot.get(p);
			
			if (newTS != null) {
				// Ok, so we created a replacement for this timeslot when used in
				// this tier tree already. Use it again to keep proper
				// sharing in place.
				return newTS;
			}
			
			// Since no replacement was made before, do it now.
			
			if (ts.isTimeAligned()) {
				newTS = new TimeSlotImpl(ts.getTime(), timeOrder);
				timeOrder.insertTimeSlot(newTS);
			} else {
				newTS = new TimeSlotImpl(timeOrder);
			    // The clone timeslot should be after the clone of the original's predecessor.
				TimeSlot pred = timeOrder.getPredecessorOf(ts);
				TimeSlot predClone = replacementTimeSlot.get(Pair.makePair(pred, rootTier));
				if (predClone == null) { // unlikely but might happen.
					predClone = pred;	 // when each tier tree gets its own TimeOrder, this is wrong.
				}
				// Put new timeslot after predClone
				timeOrder.insertTimeSlot(newTS, predClone, null);
			}
			
			// and remember the replacement.
			replacementTimeSlot.put(p, newTS);

			System.err.printf("Duplicated timeslot at time %d ms: seen on not-sharing tier trees '%s' and '%s'.\n",
						ts.getTime(), rootTier.getName(), rootTierUsedBefore.getName());
			
			return newTS;
		}
	}

	/**
	 * Check if the start and end time of annotations are consistent,
	 * i.e. if the start is before (or equal to) the end.
	 * 
	 * @param t
	 * @param complaints
	 */
	private void checkTierTimes(TierImpl t, StringBuilder complaints) {
		 for (Annotation a : t.annotations) {
			 /*if (a instanceof AlignableAnnotation)*/ {
				 long startTime = a.getBeginTimeBoundary();
				 long endTime = a.getEndTimeBoundary();
				 if (startTime > endTime) {
					 complaints.append("Error: end time before start time.");
					 complaints.append(" Tier: ");
					 complaints.append(t.getName());
					 complaints.append(" Start time: ");
					 complaints.append(TimeFormatter.toString(startTime));
					 complaints.append(" End time: ");
					 complaints.append(TimeFormatter.toString(endTime));
					 complaints.append('\n');
				 }
			 }
		 }
	}

	/**
	 * Check if any text of an annotation starts with a combining mark, so
	 * without any character before it to combine with.
	 * 
	 * @param t
	 * @param complaints to report any errors
	 */
	private void checkTierAnnotations(TierImpl t, StringBuilder complaints) {
		 for (Annotation a : t.annotations) {
			 String text = a.getValue();
			 if (text.length() > 0) {
				 int codePoint = text.codePointAt(0);
				 int type = Character.getType(codePoint);
				 if (type == Character.NON_SPACING_MARK ||       // Mn
					 type == Character.COMBINING_SPACING_MARK || // Mc
					 type == Character.ENCLOSING_MARK) {         // Me
					 long startTime = a.getBeginTimeBoundary();
					 complaints.append("Error: Unicode combining or enclosing mark at the start of the annotation. A space character was prepended.");
					 complaints.append(" Tier: ");
					 complaints.append(t.getName());
					 complaints.append(" Start time: ");
					 complaints.append(TimeFormatter.toString(startTime));
					 complaints.append('\n');
					 // "Fix" the annotation by prepending a space.
					 a.setValue(" " + text);
				 }
			 }
		 }
	}

	/**
	 * Access functions for the references between annotations.
	 * 
	 * @param refLinkSet a set to add to the list of sets
	 */
	@Override
	public void addRefLinkSet(RefLinkSet refLinkSet) {
		if (refLinkSet == null) {
			return;
		}
		if (refLinkSets == null) {
			refLinkSets = new ArrayList<RefLinkSet>();
		}
		if (!refLinkSets.contains(refLinkSet)) {
			refLinkSets.add(refLinkSet);
			addACMEditListener(refLinkSet);
			if (isLoaded) {
				modified(ACMEditEvent.ADD_REFERENCE_LINK_SET, refLinkSet);
			}
		}
		
	}
	
	/**
	 * Access functions for the references between annotations.
	 * 
	 * @param refLinkSet a set to remove from the list of sets
	 */
	@Override
	public void removeRefLinkSet(RefLinkSet refLinkSet) {
		if (refLinkSet == null) {
			return;
		}
		if (refLinkSets != null) {
			if(refLinkSets.remove(refLinkSet)) {
				removeACMEditListener(refLinkSet);
				
				if (isLoaded) {
					modified(ACMEditEvent.REMOVE_REFERENCE_LINK_SET, refLinkSet);
				}
			}
			
		}
		
	}
	/**
	 * 
	 * @param id the LINK_SET_NAME of the set
	 * @return the RefLinkSet with the specified name or null
	 */
	@Override
	public RefLinkSet getRefLinkSetByName(String name) {
		if (refLinkSets != null) {
			for (RefLinkSet rls : refLinkSets) {
				if (rls.getLinksName() != null && rls.getLinksName().equals(name)) {
					return rls;
				}
			}
		}
		return null;
	}
	
	/**
	 * Note: the id may only be generated by the time the transcription is saved
	 * 
	 * @param id the LINK_SET_ID of the set
	 * @return the RefLinkSet with the specified id or null
	 */
	@Override
	public RefLinkSet getRefLinkSetById(String id) {
		if (refLinkSets != null) {
			for (RefLinkSet rls : refLinkSets) {
				if (rls.getLinksID().equals(id)) {
					return rls;
				}
			}
		}
		return null;
	}
	
	/**
	 * Sets the list of reference link sets e.g. after loading from file
	 * @param refLinksSets
	 */
	public void setRefLinkSets(List<RefLinkSet> refLinksSets) {
		this.refLinkSets = refLinksSets;
	}
	
	/**
	 * @return the list of reference link sets or null
	 */
	@Override
	public List<RefLinkSet> getRefLinkSets() {
		return refLinkSets;
	}

	//###### below undo on the "server" side, currently only for undo in dealing with reference links ######
	/**
	 * A helper class to indicate the end of the undo's in one transaction.
	 * Needed basically because ArrayDeque can't contain nulls.
	 * This class can be static since it needs nothing from TranscriptionImpl.
	 * It can be a singleton since it is immutable.
	 * 
	 * @author olasei
	 */
	static class TerminatingUndoTransaction extends UndoTransaction {
		@Override
		public void undo() {
		}
		@Override
		public void setNext(UndoTransaction u) {
			throw new UnsupportedOperationException("Must not change this object");
		}
	}
	
	private final Deque<UndoTransaction> undoStack = new ArrayDeque<UndoTransaction>();
	static private final UndoTransaction terminatingUndoTransaction = new TerminatingUndoTransaction();
	
	/**
	 * Every time a new UndoableCommand is put on the Undo stack of the CommandHistory,
	 * the server side should start a new UndoTransaction. This will record
	 * any side-effects of the Command that it doesn't know how to undo, or doesn't
	 * even know that it has to undo.
	 */
	public void pushNewUndoTransaction() {
		// ArrayDeque can't contain nulls, so use a dummy object.
		// It won't be changed anyway.
		undoStack.push(terminatingUndoTransaction);
	}

	/**
	 * Every time an UndoableCommand is taken from the Undo stack of the CommandHistory,
	 * the server side should pick up the corresponding UndoTransaction and use it to
	 * roll back its side-effects too.
	 * <p>
	 * Each undo-transaction may consist of multiple objects, linked through getNext(). 
	 */
	public void popAndUndoTransaction() {
		UndoTransaction undo = undoStack.pollFirst();
		
		while (undo != null) {
			undo.undo();
			undo = undo.getNext();
		}
	}
	
	/**
	 * Take the most recent UndoTransaction from the stack and forget it.
	 * Do not roll it back.
	 * <p>
	 * This should be used for not undoing what an undo did.
	 */
	public void popAndForgetTransaction() {
		undoStack.pollFirst();
	}
	
	/**
	 * Remove old transactions that are more than keep items ago.
	 * @param keep must be >= 0
	 */
	public void forgetOldUndoTransactions(int keep) {
		while (undoStack.size() > keep) {
			undoStack.pollLast();
		}
	}
	
	/**
	 * If multiple kinds of actions need to be done to undo a transaction,
	 * this method links together those undo objects.
	 * <p>
	 * pushNewUndoTransaction() must have been called first.
	 * <p>
	 * Should be safe to call if the server-side undo system
	 * never was initialised.
	 */
	public void addToCurrentUndoTransaction(UndoTransaction u) {
		if (!undoStack.isEmpty()) {
			UndoTransaction uPrev = undoStack.pop();
			u.setNext(uPrev);
			undoStack.push(u);
		}
	}

	/**
	 * Allow a potential adder of undo objects to check if the current
	 * one may be suitable for re-use by changing it.
	 * <p>
	 * pushNewUndoTransaction() must have been called first,
	 * otherwise this function returns null.
	 */
	public UndoTransaction getCurrentUndoTransaction() {
		return undoStack.peek();
	}

	@Override
	public void unlock() {
		System.out.println(String.format("Removing lock with filename %s", this.lockFileName));
		File lockFile = new File(this.lockFileName);
		lockFile.delete();
	}
	
	private void lock() throws IOException
	{
		System.out.println(String.format("Creating lock with filename %s", this.lockFileName));
		Path lockFilePath = Paths.get(this.lockFileName);
		java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
		List<String> lockInfo = new ArrayList<String>();
		lockInfo.add(Long.toString(System.currentTimeMillis() / 1000L));
		lockInfo.add(System.getProperty("user.name"));
		lockInfo.add(localMachine.getHostName());
		Files.write(lockFilePath, lockInfo, Charset.forName("ASCII"), StandardOpenOption.CREATE_NEW);
	}
}
