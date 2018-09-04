package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.IllegalEditException;
import mpi.eudico.server.corpora.util.ACMEditableObject;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.Pair;

/**
 * <h2>History</h2
 * <ul>
 * <li>4-May-1999 Hennie Brugman, Albert Russel
 * <li>MK:2002/06/21 commented and using setMetaData()
 * <li>MK:2002/06/21 added getter for Participant and Locale (using hash...)
 * </ul>
 *
 * <h2>Proposed changes</h2>
 * <ul>
 * <li>MK:2002/06/21 participant and locale should get a proper member variable
 * in order to avoid dynamic downcast.
 * </ul>
 *
 * @version Aug 2005 Identity removed
 * @verion Dec 2006 getter and setter for "Annotator" added
 */
public class TierImpl implements Tier {

	@Override
	public void modified(int operation, Object modification) {
   		handleModification(this, operation, modification);
   	}


    @Override
	public void handleModification(ACMEditableObject source, int operation, Object modification) {
		if (transcription != null) {
			transcription.handleModification(source, operation, modification);
		}
	}


	/**
	 * The Tier's name.
	 */
    protected String tierName;

	/**
	 * @version Dec 2012 changed type of parent from DataTreeNode to Transcription
	 * should be renamed
	 * */
	protected TranscriptionImpl transcription;	// back reference, used for deletion of Tiers

	protected TreeSet<Annotation> annotations;
	/**
	 * see method for documentation
	 * */
	protected Map<String, Object> tierMetadata;
	protected TierImpl parentTier;

	private LinguisticType linguisticType;

	private String extRef;

	private String langRef;

	/**
	 * Constructor. It does not add this tier to the transcription that is 
	 * passed as a parameter. The type is set for the tier in the constructor.
	 * 
	 * @param name the name of this tier
	 * @param participant the participant of the tier
	 * @param parent the parent Transcription
	 * @param theType the linguistic type of the tier
	 * */
	public TierImpl(String name, String participant, TranscriptionImpl parent, LinguisticType theType) {
		this(parent);

		tierName = name;

		// init participant
		if (participant == null) {
			this.setParticipant("");
		} else {
			this.setParticipant(participant);
		}

		//MK:02/06/21 init Locale
		//this.setMetadata("DEFAULT_LOCALE", new Locale("EN","US"));
		// HS 2011-07 allow null Locale, use empty string internally
		this.setDefaultLocale(null);

		//set the LT
		this.setLinguisticType(theType);
	}

	/**
	 * Constructor. This tier adds itself to the parent tier.
	 * 
	 * @param parenttier the parent tier
	 * @param name name of this tier
	 * @param participant the participant of 'this' tier
	 * @param parent the parent transcription
	 * @param theType the linguistic type for 'this' tier
	 */
	public TierImpl (TierImpl parenttier, String name, String participant, TranscriptionImpl parent, LinguisticType theType) {
		this(name, participant, parent, theType);

		// register the parent tier.
		this.setParentTier(parenttier);
	}



	/**
	 * Private constructor with the transcription as a parameter
	 * 
	 * @version Dec 2012 made private and removed the name parameter because it was ignored
	 * 
	 * @param theParent the transcription
	 * */
	private TierImpl(TranscriptionImpl theParent) {
		transcription = theParent;

		annotations = new TreeSet<Annotation>(/*new AnnotationOnSameTierComparator()*/);

		tierMetadata = new HashMap<String, Object>();
	}

	/**
	 * Factory method that creates a new annotation of the proper type and meeting the
	 * relevant Constraints, and adds it to the tier. The new annotation will not have
	 * an initial value, this has to be set afterwards.
	 * Arguments: beginTime and endTime can be either different or equal. The latter is
	 * the case for, for example, creation of new RefAnnotations.
	 */
	public Annotation createAnnotation(long beginTime, long endTime) {
		AbstractAnnotation annotation = null;
		TimeOrder timeOrder = transcription.getTimeOrder();

		if (!isTimeAlignable() && (beginTime == endTime)) {	// then contains RefAnnotations
			Annotation referedAnnot = getParentTier().getAnnotationAtTime(beginTime);
			if (referedAnnot != null && getAnnotationAtTime(beginTime) == null) {
				annotation = new RefAnnotation(referedAnnot, this);
			}
		}
		else {					// contains AlignableAnnotations
			if (endTime > beginTime) {
				Constraint c = getLinguisticType().getConstraints();
				if (c != null) {
					List<TimeSlot> slots = c.getTimeSlotsForNewAnnotation(beginTime, endTime, this);
					if (slots.size() == 2) {
						annotation = new AlignableAnnotation(slots.get(0), slots.get(1), this);
					}
				}
				else {	// default.
					TimeSlot bts = new TimeSlotImpl(beginTime, timeOrder);
					timeOrder.insertTimeSlot(bts);

					TimeSlot ets = new TimeSlotImpl(endTime, timeOrder);
					timeOrder.insertTimeSlot(ets);

					annotation = new AlignableAnnotation(bts, ets, this);
				}
			}
		}

		if (annotation != null) {
			addAnnotation(annotation);
			
			modified(ACMEditEvent.ADD_ANNOTATION_HERE, annotation);
		}

		return annotation;
	}

	// Tier interface methods
	@Override
	public String getName() {
		return tierName;
	}

	/**
	 * Returns the transcription this tier is part of.
	 *
	 * @version Dec 2012 changed return type from DataTreeNode to Transcription 
	 * @return	the parent transcription
	 */
	@Override
	public TranscriptionImpl getTranscription() {
		return transcription;
	}

	@Override
	public void setName(String theName) {
		tierName = theName;
		modified(ACMEditEvent.CHANGE_TIER, null);
	}

	/**
	 * Adds an Annotation to the Tier.
	 * <p>
	 * The method signature requires an Annotation, but in practice this TierImpl
	 * requires AbstractAnnotations. Therefore the argument is directly casted
	 * to one.
	 * <p>
	 * The purpose of this method is mainly to satisfy the interface.
	 * The fact that this function is never called proves that all this TierImpls's
	 * Annotations are really AbstractAnnotations.
	 */
	@Override
	public void addAnnotation(Annotation theAnnotation) {
		addAnnotation((AbstractAnnotation)theAnnotation);
	}

	/**
	 * Adds an AbstractAnnotation to the Tier. Where the Annotation is inserted is determined by
	 * the Annotation's compareTo method. The 'compareTo' method uses the TimeOrder that is
	 * associated with this Tier's Transcription to find the correct ordering.
	 * Therefore the Annotation has to be added to the TimeOrder first.
	 * <p>
	 * The method signature requires an Annotation, but in practice this TierImpl
	 * requires AbstractAnnotations.
	 * 
	 * <p>MK:02/06/18<br> A single tier may contain either Alignable- or RefAnnotations.
	 * This property of a tier is set by its LinguisticType.
	 * This method should throw an Exception if one tries to add a RefAnnotation
	 * to a tier that must only contain AlignableAnnotat ion.
	 * Currently, this is not the case.
	 * </p>
	 */
	public void addAnnotation(AbstractAnnotation theAnnotation) {
		// if theAnnotation has TimeSlots, they are supposed to be inserted in TimeOrder.
		// Since annotations is a TreeSet, ordering will be on basis of Annotation.compareTo
		annotations.add(theAnnotation);

		// annotation time segments may now overlap. Since DobesTier (in this version) does
		// not allow overlapping annotations, this should be corrected. In a more generic
		// Tier case this call should be preceded by a querying the Tier if it allows overlapping.

		if (theAnnotation instanceof AlignableAnnotation) {	// assume all Tier's annots are alignable
			AlignableAnnotation a = (AlignableAnnotation) theAnnotation;
			
            if ((transcription.getTimeChangePropagationMode() == Transcription.BULLDOZER) && 
            	(this.getParentTier() == null)) {	// only for root annotations
			
				correctOverlapsByPushing(a, a.getBegin().getTime(), a.getBegin().getTime());
			}
			else if ((transcription.getTimeChangePropagationMode() == Transcription.SHIFT) &&
						(this.getParentTier() == null)) {
				List<TimeSlot> fixedSlots = new ArrayList<TimeSlot>();
				fixedSlots.add(a.getBegin());
				transcription.correctOverlapsByShifting(a, fixedSlots, a.getBegin().getTime(), a.getBegin().getTime());
				correctTimeOverlaps(a);
			}
			else {
				/*
				correctTimeOverlaps(a);
				if (linguisticType.getConstraints() != null && linguisticType.getConstraints().getStereoType() ==
				    Constraint.TIME_SUBDIVISION) {
				    correctDependingOverlaps(a);
				}
				*/
				if (linguisticType.getConstraints() != null && linguisticType.getConstraints().getStereoType() ==
				    Constraint.TIME_SUBDIVISION) {
					// only correct overlaps if necessary
					if (a.getBegin().isTimeAligned() && a.getEnd().isTimeAligned()) {
						List<Annotation> overLaps = getOverlappingAnnotations(a.getBegin().getTime(), a.getEnd().getTime());
						if (overLaps.size() > 1) {// the new annotation itself will always be in the vector
							correctTimeOverlaps(a);
							correctDependingOverlaps(a);
						}
					}
				    //correctDependingOverlaps(a);
				} else {
					correctTimeOverlaps(a);
				}
			}
		}

	}

	/**
	 * Shifts all annotations within certain temporal boundaries plus depending 
	 * annotations, n milliseconds, to the right or to the left. It does so by 
	 * changing the time values of the involved time slots. Only annotations 
	 * that are completely inside the time interval are moved and only if this 
	 * does not lead to changes (like deletion) in other annotations.
	 * Note Nov. 2011: for performance reasons handle modification isn't called anymore
	 * after each change. The caller should make sure that an ACMEditEvent.CHANGE_ANNOTATIONS
	 * notification is issued after a call to this method.
	 *  
	 *  @param numMsToShift the number of milliseconds to shift, can be negative
	 *  @param lowerBoundary only annotations to the right of this timestamp are 
	 *  shifted
	 *  @param upperBoundary only annotations to the right of this timestamp are 
	 *  shifted
	 *  
	 * @return the number of annotations that were actually shifted (on this tier)
	 * 
	 * @throws IllegalEditException thrown when either<br>
	 * - this tier is not a root tier (and/or this is not a time-alignable tier)
	 * - the shift would lead to changes in or deletion of other annotation, so 
	 * there should be an empty space for the shifting annotations
	 * - a shift to the left would lead to a negative time value for any time slot
	 * 
	 * @throws IllegalArgumentException if lowerBoundary >= upperBoundary, or if 
	 * any of the boundaries is negative
	 */
	public int shiftAnnotations(long numMsToShift, long lowerBoundary, long upperBoundary) 
    throws IllegalEditException, IllegalArgumentException {
	if (numMsToShift == 0) {
		return 0;//silently return
	}
	if (lowerBoundary >= upperBoundary) {
		throw new IllegalArgumentException("The lower boundary is greater than or " +
				"the same as the upper boundary.");
	}
	if (lowerBoundary < 0) {
		throw new IllegalArgumentException("The lower boundary has a negative value: " + 
				lowerBoundary);
	}
	if (hasParentTier() || !isTimeAlignable()) {
		throw new IllegalEditException("Shifting annotations is only supported for " +
				"time-alignableroot tiers");
	}
	//long curTime = System.currentTimeMillis();
	//System.out.println("Start shift on: " + getName() + " T: " + curTime);
	// find annotations within the boundaries and any annotation within the new boundaries
	// if numMsToShift > 0 the timespan that should be empty is from upperboundary
	// to (upperboundary + numMsToShift), else from (lowerBoundary - numMsToShift) to
	// lowerBoundary
	ArrayList<AlignableAnnotation> annosToShift = new ArrayList<AlignableAnnotation>();
	
	Iterator<Annotation> annIt = annotations.iterator();
	AlignableAnnotation ann = null;
	while (annIt.hasNext()) {
		ann = (AlignableAnnotation) annIt.next();
		if (ann.getBegin().getTime() >= lowerBoundary && ann.getEnd().getTime() <= upperBoundary) {
			annosToShift.add(ann);
		}
		if (ann.getBegin().getTime() >= upperBoundary) {
			break; 
		}
	}

	if (annosToShift.size() == 0) {
		return 0;//return silently
	}
	// calculate which time interval should be empty
	long[] emptySpace = new long[2];
	if (numMsToShift < 0) {
		ann = annosToShift.get(0);
		emptySpace[0] = ann.getBegin().getTime() + numMsToShift;
		emptySpace[1] = ann.getBegin().getTime();
	} else {
		ann = annosToShift.get(annosToShift.size() - 1);
		emptySpace[0] = ann.getEnd().getTime();
		emptySpace[1] = ann.getEnd().getTime() + numMsToShift;
	}
	
	annIt = annotations.iterator();
	while (annIt.hasNext()) {
		ann = (AlignableAnnotation) annIt.next();
		if (ann.getEnd().getTime() <= emptySpace[0]) {
			continue; // same time does not matter
		}
		if (ann.getBegin().getTime() >= emptySpace[1]) {
			break;
		}
		if (ann.getEnd().getTime() > emptySpace[0] && ann.getBegin().getTime() < emptySpace[1]) {
			if (!annosToShift.contains(ann)) {
				throw new IllegalEditException("There is at least one annotation in the " +
				"time interval where annotations should be moved to. \nMove it out of the way first." +
				"\nB: " + ann.getBegin().getTime() + " E: " + ann.getEnd().getTime());
			}
		}
	}
	// if we reach this point the action can continue, the tricky part are unaligned slots on 
	// time alignable depending tiers. Get all slots in the chain(s) remove them from the time order
	// update the time value of time aligned slots and add them to the time order
	TreeSet<AbstractAnnotation> annots = new TreeSet<AbstractAnnotation>();
	TreeSet<TimeSlotImpl> slots = new TreeSet<TimeSlotImpl>();
	TranscriptionImpl trans = getTranscription();

	if (numMsToShift < 0) { // start with the first one, iterate up
		for (int i = 0; i < annosToShift.size(); i++) {
			ann = annosToShift.get(i);
			//reset
			annots.clear();
			slots.clear();
			trans.getConnectedAnnots(annots, slots, ann);

			// remove the slots
			for (TimeSlot slot : slots) {
				trans.getTimeOrder().removeTimeSlot(slot);
			}

			// update values
			for (TimeSlotImpl slot : slots) {
				if (slot.isTimeAligned()) {
					slot.updateTime(slot.getTime() + numMsToShift);
				} else {
					slot.setProposedTime(slot.getProposedTime() + numMsToShift);
				}
			}
			// add the slots				
			TimeSlot slot1 = slots.first();
			trans.getTimeOrder().insertTimeSlot(slot1);
			TimeSlot slot2 = slots.last();
			trans.getTimeOrder().insertTimeSlot(slot2);
			for (TimeSlot slot : slots) {
				if (slot == slot1) {
					slot1 = slot;
				} else if (slot == slot2) {
					// break;?? is last one
				} else {
					if (slot.isTimeAligned()) {
						trans.getTimeOrder().insertTimeSlot(slot);
					} else {
						trans.getTimeOrder().insertTimeSlot(slot, slot1, slot2);
					}
					slot1 = slot;
				}
			}
			//handleModification(ann, ACMEditEvent.CHANGE_ANNOTATION_TIME, ann);
		}
	} else {// start with the last annotation, iterate down
		for (int i = annosToShift.size() - 1; i >= 0; i--) {
			ann = annosToShift.get(i);
			//reset
			annots.clear();
			slots.clear();
			// repeated code from above, could be a separate method, 
			// but rather not for safety reasons (given the check performed before this)
			trans.getConnectedAnnots(annots, slots, ann);
			// remove the slots
			for (TimeSlotImpl slot : slots) {
				trans.getTimeOrder().removeTimeSlot(slot);
			}
			// update values
			for (TimeSlotImpl slot : slots) {
				if (slot.isTimeAligned()) {
					slot.updateTime(slot.getTime() + numMsToShift);
				} else {
					slot.setProposedTime(slot.getProposedTime() + numMsToShift);
				}
			}
			// add the slots				
			TimeSlot slot1 = slots.first();
			trans.getTimeOrder().insertTimeSlot(slot1);
			TimeSlot slot2 = slots.last();
			trans.getTimeOrder().insertTimeSlot(slot2);
			for (TimeSlot slot : slots) {
				if (slot == slot1) {
					slot1 = slot;
				} else if (slot == slot2) {
					// break;?? is last one
				} else {
					if (slot.isTimeAligned()) {
						trans.getTimeOrder().insertTimeSlot(slot);
					} else {
						trans.getTimeOrder().insertTimeSlot(slot, slot1, slot2);
					}
					slot1 = slot;
				}
			}
			//handleModification(ann, ACMEditEvent.CHANGE_ANNOTATION_TIME, ann);
		}
	}
	return annosToShift.size();
	//handleModification((ACMEditableObject)getParent(), ACMEditEvent.CHANGE_ANNOTATIONS, this);
}
	
	public void insertAnnotation(Annotation theAnnotation) {
		// if theAnnotation has TimeSlots, they are supposed to be inserted in TimeOrder.
		// Since annotations is a TreeSet, ordering will be on basis of Annotation.compareTo
		annotations.add(theAnnotation);
	}
	
	/**
	 * Checks whether the order of the annotations in the TreeSet is still 
	 * correct. When one annotation has been moved beyond another (by updating
	 * its begin and end times) the order of the annotations is incorrect and 
	 * has to be corrected.
	 * 
	 * @see AlignableAnnotation.updateTimeInterval(long, long)
	 * @see #resortAnnotations()
	 * 
	 * @return true if all annotations are in the correct position, false otherwise
	 */
	public boolean checkAnnotationOrderConsistency() {
		List<Annotation> v = new ArrayList<Annotation>(annotations);
		Annotation a1, a2;
		for (int i = 0; i < v.size() - 1; i++) {
			a1 = v.get(i);
			a2 = v.get(i + 1);
			if (a1.compareTo(a2) > 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * When an annotation has been moved beyond another annotation on the same 
	 * tier their order in the TreeSet is incorrect and has to be changed.
	 * Since a TreeSet uses compareTo() to find and remove an object, removing
	 * a single object can fail when the ordering is inconsistent. 
	 * That is why in that case all annotations are removed from the TreeSet 
	 * and added again.<br>
	 * Call checkAnnotationOrderConsistency() before calling this method.<br>
	 * This method has package access, so can not be called from client code.  
	 * 
	 * @see #checkAnnotationOrderConsistency()
	 * @see AlignableAnnotation.updateTimeInterval(long, long)
	 */
	void resortAnnotations() {
		List<Annotation> v = new ArrayList<Annotation>(annotations);
		annotations.clear();
		annotations.addAll(v);
	}

	public Annotation createAnnotationBefore(Annotation beforeAnn) {
		Annotation a = null;

		Constraint c = linguisticType.getConstraints();
		if ((c != null) && (c.supportsInsertion())) {
			a = c.insertBefore(beforeAnn, this);
			modified(ACMEditEvent.ADD_ANNOTATION_BEFORE, a);
		}
		return a;
	}


	public Annotation createAnnotationAfter(Annotation afterAnn) {
		Annotation a = null;

		Constraint c = linguisticType.getConstraints();
		if ((c != null) && (c.supportsInsertion())) {
			a = c.insertAfter(afterAnn, this);
			modified(ACMEditEvent.ADD_ANNOTATION_AFTER, a);
		}

		return a;
	}


	@Override
	public void removeAnnotation(Annotation theAnnotation) {
		//boolean removed = annotations.remove(theAnnotation);
		theAnnotation.markDeleted(true);

		// HB, 4-7-02: outcommented, seems unnecessary since markDeleted
		// already propagates deletion to ParentAnnotationListeners.
	//	((AbstractAnnotation) theAnnotation).notifyListeners();
        // HS 01-2013 change in order to have the deleted annotation in the ACMEditEvent  
		transcription.pruneAnnotations(this, theAnnotation); 	// prunes all tiers that might have been changed
	}


	@Override
	public void removeAllAnnotations() {
		Iterator<Annotation> annIter = annotations.iterator();
		while (annIter.hasNext()) {
			Annotation annot = annIter.next();
			annot.markDeleted(true);

			// HB, 16 jul 02: outcommented, since markDeleted propagates deletion
			// to ParentAnnotationListeners.
		//	((AbstractAnnotation) annot).notifyListeners(ACMEditEvent.REMOVE_ANNOTATION, null);
		}

		transcription.pruneAnnotations(this); 	// prunes all tiers that might have been changed
	}

	/**
	 * @return List of AbstractAnnotation Objects.
	 * A new List is created from the annotation TreeSet!
	 * <p>
	 * If you don't care about the elements being anything more specific than
	 * Annotation, consider calling this method as
	 * <pre> List&lt;? extends Annotation> annotations  = tier.getAnnotations();</pre>
	 * or
	 * <pre> for (Annotation ann : t.getAnnotations()) {</pre>
	 * instead of (variations of)
	 * <pre> List&lt;AbstractAnnotation> annotations  = tier.getAnnotations();
     * Iterator&lt;AbstractAnnotation> annotIter = annotations.iterator();
	 * while (annotIter.hasNext()) {
	 *     Annotation ann = annotIter.next();</pre>      
	 */
	@Override
	public List<AbstractAnnotation> getAnnotations() {
		// We believe that all Annotations are AbstractAnnotations without checking.
		@SuppressWarnings("unchecked")
		List<AbstractAnnotation> annList = new ArrayList(annotations);
		return annList;
	}
	
	/**
	 * Get the current AlignableAnnotations, checking that they
	 * are indeed that.
	 */
	public List<AlignableAnnotation> getAlignableAnnotations() {
		if (isTimeAlignable()) {
			@SuppressWarnings("unchecked")
			List<AlignableAnnotation> annList = new ArrayList(annotations);
			return annList;
		}
		System.err.println("Error: getAlignableAnnotations called on not-time-alignable Tier " + getName());
		return new ArrayList<AlignableAnnotation>(0);
	}
	
	/**
	 * Get the current RefAnnotations, checking that they
	 * are indeed that.
	 */
	public List<RefAnnotation> getRefAnnotations() {
		if (!isTimeAlignable()) {
			@SuppressWarnings("unchecked")
			List<RefAnnotation> annList = new ArrayList(annotations);
			return annList;
		}
		System.err.println("Error: getRefAnnotations called on time-alignable Tier " + getName());
		return new ArrayList<RefAnnotation>(0);
	}
	
	/**
	 * Returns the annotation with the corresponding id;
	 * null if no annotation has a corresponding id;
	 * @param id
	 * @return
	 */
	public Annotation getAnnotationById(String id){
		if(id == null) {
			return null;
		}
		for (Annotation a : annotations) {
			if (id.equals(a.getId())) {
				return a;
			}
		}

		return null;
	}

	public void getAnnotationsByIdMap(Map<String, Annotation> map) {
		for (Annotation a : annotations) {
			map.put(a.getId(), a);
		}
	}
	
	@Override
	public int getNumberOfAnnotations() {
		return annotations.size();
	}


	public Annotation getAnnotationBefore(Annotation theAnnotation) {
		Annotation previousAnnotation = null;
		Annotation currentAnnotation = null;

		Iterator<Annotation> iter = annotations.iterator();
		while (iter.hasNext()) {
			previousAnnotation = currentAnnotation;
			currentAnnotation = iter.next();

			if (currentAnnotation.equals(theAnnotation)) {
				break;
			}
		}

		return previousAnnotation;
	}


	public Annotation getAnnotationBefore(long time) {
		Annotation previousAnnotation = null;
		Annotation currentAnnotation = null;
		Annotation foundAnnotation = null;
		long lastEndTime = -1;

		Iterator<Annotation> iter = annotations.iterator();
		while (iter.hasNext()) {
			previousAnnotation = currentAnnotation;
			currentAnnotation = iter.next();
			lastEndTime = currentAnnotation.getEndTimeBoundary();

			if (lastEndTime > time) {
				foundAnnotation = previousAnnotation;
				break;
			}
		}
		if (foundAnnotation != null) {
			return foundAnnotation;
		}
		else {
			if (lastEndTime > -1 && lastEndTime < time) {
				return currentAnnotation;
			}
			//return currentAnnotation;
			return null;
		}
	}


	public Annotation getAnnotationAfter(Annotation theAnnotation) {
		Annotation nextAnnotation = null;
		Annotation currentAnnotation = null;

		Iterator<Annotation> iter = annotations.iterator();
		while (iter.hasNext()) {
			currentAnnotation = iter.next();

			if (currentAnnotation.equals(theAnnotation)) {
				if (iter.hasNext()) {
					nextAnnotation = iter.next();
				}
				break;
			}
		}

		return nextAnnotation;

	}


	public Annotation getAnnotationAfter(long time) {
		Annotation currentAnnotation = null;
		Annotation resultAnnotation = null;

		Iterator<Annotation> iter = annotations.iterator();
		while (iter.hasNext()) {
			currentAnnotation = iter.next();

			if (currentAnnotation.getEndTimeBoundary() > time) {
				resultAnnotation = currentAnnotation;
				break;
			}
		}

		return resultAnnotation;

	}


	/**
	 * Corrects overlapping time intervals using the following algorithm:
	 * - get all begin and end TimeSlots in order of the Tier's Annotations
	 * - get fixed TimeSlots from fixedAnnotation
	 * - get first and second time value
	 * - compare: if first smaller than second make second first
	 * - if first bigger than second correct by making them equal, keeping the fixed
	 *   one fixed. Assumed is that exactly one of the pair is fixed !!!!!
	 * - go on to the next second value
	 * - not aligned TimeSlots are ignored
	 */
/*	public void correctTimeOverlaps(AlignableAnnotation fixedAnnotation) {
System.out.println("");
System.out.println("before correction:");

	((TranscriptionImpl) parent).getTimeOrder().printTimeOrder();

		List tiersTimeSlots = new List();

		// add all aligned TimeSlots in Annotation order
		Iterator annotIter = annotations.iterator();
		while (annotIter.hasNext()) {
			AlignableAnnotation ann = (AlignableAnnotation) annotIter.next();
			if (ann.getBegin().isTimeAligned()) {
				tiersTimeSlots.add(ann.getBegin());
			}
			if (ann.getEnd().isTimeAligned()) {
				tiersTimeSlots.add(ann.getEnd());
			}
		}

		Iterator slotIter = tiersTimeSlots.iterator();
		TimeSlot slot1 = null;
		TimeSlot slot2 = null;

		if (slotIter.hasNext()) {
			slot1 = (TimeSlot) slotIter.next();
		}

		while (slotIter.hasNext()) {
			slot2 = (TimeSlot) slotIter.next();

			if (slot1.getTime() <= slot2.getTime()) {	// correct order
				slot1 = slot2;
			}
			else {		// incorrect order
				if (	(slot1.equals(fixedAnnotation.getBegin()))	||
					(slot1.equals(fixedAnnotation.getEnd()))   ) {	// slot1 fixed

					// adapt slot2
					slot2.setTime(slot1.getTime());
				}
				else {		// slot2 fixed
					slot1.setTime(slot2.getTime());
				}
			}
		}

		// delete any completely overlapped Annotations. Tests: for aligned timeslots,
		// check if begintime is equal to end time, for unaligned slots, check if there is
		// a non zero time gap left (getTimeBegin/EndBoundary not equal).
System.out.println("");
System.out.println("after correction:");

	((TranscriptionImpl) parent).getTimeOrder().printTimeOrder();

		markAnnotationsForDeletion();
		//MK:02/06/12 quick fix
		//MK:02/06/17 removed guard in order to find the cause of the problem
//		if (parent instanceof TranscriptionImpl) {
		((TranscriptionImpl) parent).pruneAnnotations(); 	// prunes all tiers
//		}
	}
*/

	public void correctTimeOverlaps(AlignableAnnotation fixedAnnotation) {
		TimeSlot fixedSlot1 = fixedAnnotation.getBegin();
		TimeSlot fixedSlot2 = fixedAnnotation.getEnd();
		if (!fixedSlot1.isTimeAligned()) {
			return;
		}
		if (!fixedSlot2.isTimeAligned()) {
			return;
		}

		// get annotations that are connected to fixedAnnotation by one graph
		TreeSet<AbstractAnnotation> connectedAnnots = new TreeSet<AbstractAnnotation>();
		TreeSet<TimeSlotImpl> connectedTimeSlots = new TreeSet<TimeSlotImpl>();
		
		//((TranscriptionImpl) parent).getConnectedAnnots(connectedAnnots,
		//					connectedTimeSlots, fixedSlot1);
		transcription.getConnectedAnnots(connectedAnnots,
				connectedTimeSlots, fixedAnnotation);
						
		List<AbstractAnnotation> connectedAnnotVector = new ArrayList<AbstractAnnotation>(connectedAnnots);
		//List connectedTimeSlotVector = new ArrayList(connectedTimeSlots);
		// the annotation TreeSet sometimes contains duplicate elements (although it is a Set), probably 
		// due to oddities in the compareTo method in AbstractAnnotation
		ArrayList<AlignableAnnotation> correctedAnnos = new ArrayList<AlignableAnnotation>(connectedAnnotVector.size()); 
		// find 'begin' and 'end' timeslot of connected graph
		TimeSlot[] graphEndpoints = getGraphEndpoints(connectedAnnotVector);
		
		// get all overlapping annotations on this tier and their alignable children
		TreeSet<AlignableAnnotation> overlappingAnnots = new TreeSet<AlignableAnnotation>();
		
		// oct 04 old implementation
		//Iterator annotIter = getOverlappingAnnotations(fixedSlot1.getTime(), fixedSlot2.getTime()).iterator();
		// oct 04 old 2 implementation
		//Iterator annotIter = getOverlappingAnnotations(fixedSlot1.getTime(), 
		//	fixedSlot2.getTime(), true).iterator();
		// aug 05 include extremes, to solve problems on time subdivision tiers (new annotation 
		// over existing annotations, starting at begin time of parent annotation 
		Iterator<AlignableAnnotation> annotIter = getOverlappingAnnotationsIncludeExtremes(fixedSlot1, fixedSlot2).iterator();
			
		while (annotIter.hasNext()) {
			AlignableAnnotation ann = annotIter.next();

			if (ann != fixedAnnotation) {
				overlappingAnnots.add(ann);
			}
			
			// then add ann's alignable children.
			// Note HS July 2006: in the process of propagation of changes as a result of the addition of a new 
			// annotation, getChildAnnotationsOf returns (graph based) overlapping annotations rather then child annotations 
			List<Annotation> children = transcription.getChildAnnotationsOf(ann);

			if (children.size() > 0) {
				Iterator<Annotation> childIter = children.iterator();
				while (childIter.hasNext()) {
					Annotation child = childIter.next();		
					if (child instanceof AlignableAnnotation) {
						//AlignableAnnotation a = (AlignableAnnotation) child;									
						overlappingAnnots.add((AlignableAnnotation) child);								
					}
				}
			} 
		}
		
		List<AbstractAnnotation> overlappingAnnotVector = new ArrayList<AbstractAnnotation>(overlappingAnnots);
		
		// put overlappingAnnots in List, in order given by graph, starting at graphBegin
		// then add remaining annots to result, in original order
		
		overlappingAnnotVector = inGraphOrder(overlappingAnnotVector, graphEndpoints[0]);
		boolean forcedInside = false;
		// now correct each overlapping annotation
		Iterator<AbstractAnnotation> overlappingAnnotIter = overlappingAnnotVector.iterator();
		while (overlappingAnnotIter.hasNext()) {
			AlignableAnnotation aa = (AlignableAnnotation) overlappingAnnotIter.next();
			if (correctedAnnos.contains(aa)) {
			    continue;
			} else {
			    correctedAnnos.add(aa);
			}
		 	Constraint cc = ((TierImpl) aa.getTier()).getLinguisticType().getConstraints();
		 	// 3 different cases:
		 	// - an annotation not connected to aa's graph overlaps aa
		 	// - the fixedAnnotation is the root annotation of aa's graph
		 	// - the fixedAnnotation is a dependent annotation in aa's graph
		 	
			if (cc != null && cc.getStereoType() == Constraint.INCLUDED_IN) {
			    if (fixedAnnotation.isAncestorOf(aa)) {
			        forceAnnotationInInterval(aa, fixedAnnotation);
			    } else {
			        forceOutOfFixedInterval(aa, fixedAnnotation);
			    }
			}
			else if (!connectedAnnotVector.contains(aa)) {	// other annotation overlaps aa
			    if (aa.getTier() == fixedAnnotation.getTier()) {
			        forceOutOfFixedInterval(aa, fixedAnnotation);
			        // update children first
			        forceChildAnnotationsOfAlignable(aa);
			    }				
			}// special case for included in?

			else if (!fixedAnnotation.hasParentAnnotation()){		// fixed annot is root of graph
			    // this only needs to be called once, works only on this tier??
			    if (!forcedInside) {
			        forceInsideFixedInterval(graphEndpoints, fixedAnnotation);
			        forcedInside = true;
			    }
			    if (cc != null && cc.getStereoType() == Constraint.TIME_SUBDIVISION) {
			        forceTSInsideFixedInterval(aa, graphEndpoints, fixedAnnotation);
			    }
			}
			else {	// fixedAnnotation is dependent member of graph
				// algorithm:
				// following graph, starting at graphBegin, force all timeslots after fixed2 
				// that have times before fixed2 to time of fixed2
				// HS feb 05: force all (connected) slots between fixedSlots1 and 2 to the time
				// of fixedSlot1
			
				// forceAnnotChainOutOfFixedInterval only works on this tier...
				if (aa.getTier() == fixedAnnotation.getTier()) {
				    forceAnnotChainOutOfFixedInterval(graphEndpoints, fixedSlot1, fixedSlot2);
				    
				    if (aa.getBegin().isTimeAligned() && aa.getBegin().getTime() < aa.getEnd().getTime()) {
				        // update children				        
				        if (aa.getBegin().getTime() < fixedSlot1.getTime() && aa.getEnd().getTime() > fixedSlot1.getTime()) {
				            forceChildChainOutOfInterval(aa, graphEndpoints, fixedSlot1, fixedSlot2, true);
				        } else if (aa.getBegin().getTime() <= fixedSlot2.getTime() && aa.getEnd().getTime() > 
				                fixedSlot2.getTime()) {
				            forceChildChainOutOfInterval(aa, graphEndpoints, fixedSlot1, fixedSlot2, false);
				        }
				        
				    }
				}
			}
		}
		// special iteration for connected Include_In descendant annotations
		Iterator<AbstractAnnotation> connAnnotIter = connectedAnnotVector.iterator();
		while (connAnnotIter.hasNext()) {
			AlignableAnnotation aa = (AlignableAnnotation) connAnnotIter.next();
			if (correctedAnnos.contains(aa)) {
			    continue;
			} else {
			    correctedAnnos.add(aa);
			}
			if (overlappingAnnotVector.contains(aa) || aa == fixedAnnotation) {
			    continue;
			}
			if (!((TierImpl)aa.getTier()).hasAncestor(this)) {
			    continue;
			}
		 	Constraint cc = ((TierImpl) aa.getTier()).getLinguisticType().getConstraints();
		 	if (cc != null && cc.getStereoType() == Constraint.INCLUDED_IN) {
			    if (fixedAnnotation.isAncestorOf(aa)) {
			        forceAnnotationInInterval(aa, fixedAnnotation);
			    } else {
			        forceOutOfFixedInterval(aa, fixedAnnotation);
			    }
			    overlappingAnnotVector.add(aa);// eventually mark for deletion
			} else if (cc != null && cc.getStereoType() == Constraint.TIME_SUBDIVISION) {
			    // special case to handle the event of a time subdivision annotation's time has been modified
			    if (!fixedAnnotation.isAncestorOf(aa)) {
			        forceOutOfFixedInterval(aa, fixedAnnotation);
			        overlappingAnnotVector.add(aa);// eventually mark for deletion
			    }
			}
		}
		
		// HB, 12-8-04, reposition unaligned slots
		// start at graph begin, find next annot on this tier following the graph, until graph end.
		// correct order for each (partially) unaligned annotation
		repositionUnalignedSlots(graphEndpoints[0]);
		
		if (overlappingAnnotVector.size() > 0) {
			// delete any completely overlapped Annotations. Tests: for aligned timeslots,
			// check if begintime is equal to end time, for unaligned slots, check if there is
			// a non zero time gap left (getTimeBegin/EndBoundary not equal).

			//markAnnotationsForDeletion(new Vector(overlappingAnnots));
		    markAnnotationsForDeletion(overlappingAnnotVector);
			transcription.pruneAnnotations(this); 	// prunes tiers that possibly are changed
		}	
		
	}
	
	public TimeSlot[] getGraphEndpoints(List<AbstractAnnotation> connectedAnnotList) {
		TimeSlot[] endpoints = new TimeSlot[2];
				
		Iterator<AbstractAnnotation> annIter = connectedAnnotList.iterator();
		while (annIter.hasNext()) {
			AlignableAnnotation ann = (AlignableAnnotation) annIter.next();
			if (!ann.hasParentAnnotation()) {	// assume there is only one root annot in a graph
				endpoints[0] = ann.getBegin();
				endpoints[1] = ann.getEnd();
				
				break;
			}
		}
								
		return endpoints;
	}

	/**
	 * Returns the timeslots where the 'subchain' containing theSlot connects to
	 * either an aligned timeslot or an unaligned slot shared with a parent annotation.
	 * 
	 * @param theSlot
	 * @return
	 */
	public TimeSlot[] getEndpointsOfSubchain(TimeSlot theSlot, boolean stopAtAlignedSlot) {
		TimeSlot[] endpoints = new TimeSlot[2];
		
		endpoints[0] = getBeginSlotOfChain(theSlot, stopAtAlignedSlot);
		endpoints[1] = getEndSlotOfChain(theSlot, stopAtAlignedSlot);
		
		return endpoints;
	}

	/**
	 * Starting at theSlot, find the last time slot of annotations connected
	 * to it in the same 'chain' on this tier.
	 * 
	 * @param theSlot
	 * @return
	 */
	public TimeSlot getEndSlotOfChain(TimeSlot theSlot, boolean stopAtAlignedSlot) {
		TimeSlot endSlot = theSlot;
		
			if ((getParentTier() != null && 
					getParentTier()
						.getAnnotationsUsingTimeSlot(endSlot).size() > 0)) {
				return endSlot;			
			}

			List<Annotation> annotsFromSlot = getAnnotsBeginningAtTimeSlot(theSlot);
			if (annotsFromSlot.size() > 0) {
				AlignableAnnotation nextAnnot = (AlignableAnnotation) annotsFromSlot.get(0);
				TimeSlot nextSlot = nextAnnot.getEnd();
				if 	((nextSlot.isTimeAligned() && stopAtAlignedSlot) ||
					(getParentTier() != null && 
						getParentTier()
							.getAnnotationsUsingTimeSlot(nextSlot).size() > 0)) {	// end found, stop
					endSlot = nextSlot;
				}
				else {
					endSlot = getEndSlotOfChain(nextSlot, stopAtAlignedSlot);
				}	
			}
		
		return endSlot;
	}

	/**
	 * Starting at theSlot, find the first time slot of annotations connected
	 * to it in the same 'chain' on this tier.
	 * 
	 * @param theSlot
	 * @return
	 */	
	public TimeSlot getBeginSlotOfChain(TimeSlot theSlot, boolean stopAtAlignedSlot) {
		TimeSlot beginSlot = theSlot;
		
			if ((getParentTier() != null && 
					getParentTier()
						.getAnnotationsUsingTimeSlot(beginSlot).size() > 0)) {
				return beginSlot;			
			}

			List<Annotation> annotsToSlot = getAnnotsEndingAtTimeSlot(theSlot);
			if (annotsToSlot.size() > 0) {
				AlignableAnnotation prevAnnot = (AlignableAnnotation) annotsToSlot.get(0);
				TimeSlot prevSlot = prevAnnot.getBegin();
				if 	((prevSlot.isTimeAligned() && stopAtAlignedSlot) ||
					(getParentTier() != null && 
						getParentTier()
							.getAnnotationsUsingTimeSlot(prevSlot).size() > 0)) {	// end found, stop
					beginSlot = prevSlot;
				}
				else {
					beginSlot = getBeginSlotOfChain(prevSlot, stopAtAlignedSlot);
				}	
			}
		
		return beginSlot;
	}


	private List<AbstractAnnotation> inGraphOrder(List<AbstractAnnotation> overlappingAnnots, TimeSlot graphBegin) {
		TimeSlot aBegin = graphBegin;
		TimeSlot aEnd = graphBegin;
		
		List<Annotation> annsForTS = null;
		AlignableAnnotation currentA = null;
		
		List<AbstractAnnotation> result = new ArrayList<AbstractAnnotation>();
		
		while (aEnd != null) {

			annsForTS = this.getAnnotsBeginningAtTimeSlot(aBegin);	
			
			if (annsForTS != null && annsForTS.size() > 0) {
				currentA = (AlignableAnnotation) annsForTS.get(0);
				aEnd = currentA.getEnd();	
				
				if (overlappingAnnots.contains(currentA)) {
					result.add(currentA);
				}
				
				aBegin = aEnd;
			}
			else {
				aEnd = null;
			}			
		}
		
		// and add remaining to result
		Iterator<AbstractAnnotation> remainingIter = overlappingAnnots.iterator();
		while (remainingIter.hasNext()) {
			AlignableAnnotation remA = (AlignableAnnotation) remainingIter.next();
			if (!result.contains(remA))	{
				result.add(remA);
			}
		}
		
		return result;		
	}
	
	private void forceOutOfFixedInterval(AlignableAnnotation aa, AlignableAnnotation fixedAnnotation) {
		TimeSlot begin = aa.getBegin();
		TimeSlot end = aa.getEnd();

		TimeSlot fixedSlot1 = fixedAnnotation.getBegin();
		TimeSlot fixedSlot2 = fixedAnnotation.getEnd();

		if (aa.getTier() == this) {
			boolean beginInFixedInterval = false;
			boolean endInFixedInterval = false;
					
			TreeSet<AbstractAnnotation> connAnnots = new TreeSet<AbstractAnnotation>();
			TreeSet<TimeSlot> connTimeSlots = new TreeSet<TimeSlot>();
					
			transcription.getConnectedAnnots(connAnnots,
					connTimeSlots, begin);
					
			if ((begin.isAfter(fixedSlot1) && fixedSlot2.isAfter(begin)) || 
					(begin.getTime() == fixedSlot1.getTime())) {	
				beginInFixedInterval = true;
			}
			if ((end.isAfter(fixedSlot1) && fixedSlot2.isAfter(end)) || 
					(end.getTime() == fixedSlot1.getTime())) {	
				endInFixedInterval = true;
			}
					
			if (beginInFixedInterval && !endInFixedInterval) {	// only begin in fixed interval
				// force all connected timeslot to after fixedSlot2
				shiftAfterTimeSlot(fixedSlot2, connTimeSlots);
			}
			else {	// all other cases
				// force all connected timeslots to before fixedSlot1
				shiftBeforeTimeSlot(fixedSlot1, connTimeSlots);
			}
		} else {
		    // handle annotations on Included_In tiers here
		    
		    Constraint cc = ((TierImpl) aa.getTier()).getLinguisticType().getConstraints();
		    if (cc != null && cc.getStereoType() == Constraint.INCLUDED_IN) {
		        if (begin.getTime() >= fixedAnnotation.getBeginTimeBoundary() && 
		                begin.getTime() < fixedAnnotation.getEndTimeBoundary()) {
		            begin.setTime(fixedAnnotation.getEndTimeBoundary());

		            if (end.getTime() < fixedAnnotation.getEndTimeBoundary()) {
		                end.setTime(fixedAnnotation.getEndTimeBoundary());
		            }
		        } else if (begin.getTime() < fixedAnnotation.getBeginTimeBoundary() && 
		                end.getTime() > fixedAnnotation.getBeginTimeBoundary()) {
		            end.setTime(fixedAnnotation.getBeginTimeBoundary());
		        } else if (begin.getTime() < fixedAnnotation.getBeginTimeBoundary() && 
		                end.getTime() >= fixedAnnotation.getEndTimeBoundary()) {
		            // shift to front
		            end.setTime(fixedAnnotation.getBeginTimeBoundary());
		        }
	            // update existing Time_Subdivision and Included_In child tiers 
		        forceChildAnnotationsOfAlignable(aa);
		    }  else if (cc != null && cc.getStereoType() == Constraint.TIME_SUBDIVISION) {
		        if (aa.getBegin().isTimeAligned() && aa.getEnd().isTimeAligned() && aa.getBegin().getTime() > 
		                fixedAnnotation.getBegin().getTime() && aa.getEnd().getTime() < fixedAnnotation.getEnd().getTime()) {
		            aa.markDeleted(true);
		        } else if (aa.getBegin().isTimeAligned() && aa.getBegin().getTime() <= fixedAnnotation.getEnd().getTime() &&
		                aa.getBegin().getTime() > fixedAnnotation.getBegin().getTime()  && 
		                aa.getEnd().getTime() >= fixedAnnotation.getEnd().getTime()) {
		            // reconnect at the right side, the counterpart on the left side is handled elsewhere?
		            aa.setBegin(fixedAnnotation.getEnd());
		            forceChildAnnotationsOfAlignable(aa);
		        } 
		    }
		}
	}


	private void shiftAfterTimeSlot(TimeSlot fixedSlot, TreeSet<TimeSlot> connTimeSlots) {
		TimeSlot lastShiftedSlot = null;
		
		Iterator<TimeSlot> tsIter = connTimeSlots.iterator();
		while (tsIter.hasNext()) {
			TimeSlot ts = tsIter.next();
			if (fixedSlot.isAfter(ts)) {
				if (ts.isTimeAligned()) {
					ts.setTime(fixedSlot.getTime());
					lastShiftedSlot = ts;
				}
				else {	// keep with last shifted time slot
					transcription.getTimeOrder().removeTimeSlot(ts);
					transcription.getTimeOrder().insertTimeSlot(ts, lastShiftedSlot, null);
					// July 2006: keep unaligned slot in the right order
					lastShiftedSlot = ts;
				}
			}
		}
	}

	private void shiftBeforeTimeSlot(TimeSlot fixedSlot, TreeSet<TimeSlot> connTimeSlots) {
		TimeSlot lastShiftedSlot = null;
		TimeSlot afterThisSlot = null;
		
		ArrayList<TimeSlot> pending = new ArrayList<TimeSlot>();
		Iterator<TimeSlot> tsIter = connTimeSlots.iterator();
		while (tsIter.hasNext()) {
			TimeSlot ts = tsIter.next();
			if (ts.isAfter(fixedSlot)) {
				if (ts.isTimeAligned()) {
					ts.setTime(fixedSlot.getTime());
					lastShiftedSlot = ts;
					if (pending.size() > 0) {
						TimeSlot uts;
						for (int i = 0; i < pending.size(); i++) {
							uts = pending.get(i);
							transcription.getTimeOrder().removeTimeSlot(uts);
							transcription.getTimeOrder().insertTimeSlot(uts, afterThisSlot, ts);
						}
						pending.clear();
					}
				}
				else {	// keep with last shifted time slot
//					if (lastShiftedSlot != null) {
//						((TranscriptionImpl) parent).getTimeOrder().removeTimeSlot(ts);
//						((TranscriptionImpl) parent).getTimeOrder().insertTimeSlot(ts, lastShiftedSlot, null);						
//					}
					pending.add(ts);
				}
			} else {
				afterThisSlot = ts;
			}
		}	
		if (pending.size() > 0) {
			TimeSlot uts;
			for (int i = 0; i < pending.size(); i++) {
				uts = pending.get(i);
				transcription.getTimeOrder().removeTimeSlot(uts);
				transcription.getTimeOrder().insertTimeSlot(uts, afterThisSlot, fixedSlot);
			}
			pending.clear();
		}
	}

/*	private void forceInsideFixedInterval(AlignableAnnotation aa, AlignableAnnotation fixedAnnotation) {
		TimeSlot begin = aa.getBegin();
		TimeSlot end = aa.getEnd();

		TimeSlot fixedSlot1 = fixedAnnotation.getBegin();
		TimeSlot fixedSlot2 = fixedAnnotation.getEnd();

		if (fixedSlot1.isAfter(begin)) {
			if (begin.isTimeAligned() && (begin != fixedSlot1)) {
				begin.setTime(fixedSlot1.getTime());
			}
			else {
				((TranscriptionImpl) parent).getTimeOrder().removeTimeSlot(begin);
				((TranscriptionImpl) parent).getTimeOrder().insertTimeSlot(begin, fixedSlot1, null);						
			}
		}
		if (fixedSlot1.isAfter(end)) {
			if (end.isTimeAligned() && (end != fixedSlot2)) {
				end.setTime(fixedSlot1.getTime());
			}
			else {
				((TranscriptionImpl) parent).getTimeOrder().removeTimeSlot(end);
				((TranscriptionImpl) parent).getTimeOrder().insertTimeSlot(end, begin, null);
			}
		}
		if (begin.isAfter(fixedSlot2)) {
			if (begin.isTimeAligned() && (begin != fixedSlot1)) {
				begin.setTime(fixedSlot2.getTime());
			}
			else {
				((TranscriptionImpl) parent).getTimeOrder().removeTimeSlot(begin);
				((TranscriptionImpl) parent).getTimeOrder().insertTimeSlot(begin, null, fixedSlot2);
			}
		}
		if (end.isAfter(fixedSlot2)) {
			if (end.isTimeAligned() && (end != fixedSlot2)) {
				end.setTime(fixedSlot2.getTime());
			}
			else {
				((TranscriptionImpl) parent).getTimeOrder().removeTimeSlot(end);
				((TranscriptionImpl) parent).getTimeOrder().insertTimeSlot(end, begin, null);
			}
		}			
	}
*/	
	private void forceInsideFixedInterval(TimeSlot[] graphEndpoints, AlignableAnnotation fixedAnnotation) {
		TimeSlot annBegin = graphEndpoints[0];
		TimeSlot annEnd = graphEndpoints[0];
		
		List<Annotation> annotsForTS = null;
		AlignableAnnotation currentAnn = null;
		
		while (annEnd != null) {
			annotsForTS = this.getAnnotsBeginningAtTimeSlot(annBegin);	
			
			if (annotsForTS != null && annotsForTS.size() > 0) {
				currentAnn = (AlignableAnnotation) annotsForTS.get(0);
				annEnd = currentAnn.getEnd();	
						
				if (annEnd.isTimeAligned() && annEnd.getTime() < fixedAnnotation.getBegin().getTime()) {
					annEnd.setTime(fixedAnnotation.getBegin().getTime());
				}
				
				if (!annEnd.isTimeAligned() && fixedAnnotation.getBegin().isAfter(annEnd)) {
					transcription.getTimeOrder().removeTimeSlot(annEnd);
					transcription.getTimeOrder().insertTimeSlot(annEnd, fixedAnnotation.getBegin(), null);					
				}
				
				annBegin = annEnd;
			}
			else {
				annEnd = null;
			}			
		}			
	}

	/**
	 * Special case for Included_In annotations.
	 * @param aa the annotation to force within the interval of fixedAnnotation
	 * @param fixedAnnotation the leading annotation
	 */
	private void forceAnnotationInInterval(AlignableAnnotation aa, AlignableAnnotation fixedAnnotation) {
		TimeSlot begin = aa.getBegin();
		TimeSlot end = aa.getEnd();
		
	    Constraint cc = ((TierImpl) aa.getTier()).getLinguisticType().getConstraints();
	    if (cc != null && cc.getStereoType() == Constraint.INCLUDED_IN) {
	        if (begin.getTime() >= fixedAnnotation.getBeginTimeBoundary() && 
	                end.getTime() <= fixedAnnotation.getEndTimeBoundary()) {
	            return;
	        } else if (begin.getTime() < fixedAnnotation.getBeginTimeBoundary() && 
	                end.getTime() > fixedAnnotation.getBeginTimeBoundary()) {
	            begin.setTime(fixedAnnotation.getBeginTimeBoundary());
	            if (end.getTime() > fixedAnnotation.getEndTimeBoundary()) {
	                end.setTime(fixedAnnotation.getEndTimeBoundary());
	            }
	            // update existing Time_Subdivision and Included_In child tiers 
	            forceChildAnnotationsOfAlignable(aa);
	        } else if (end.getTime() > fixedAnnotation.getEndTimeBoundary() && 
	                begin.getTime() < fixedAnnotation.getEndTimeBoundary()) {
	            end.setTime(fixedAnnotation.getEndTimeBoundary());
	            if (begin.getTime() < fixedAnnotation.getBeginTimeBoundary()) {
	                begin.setTime(fixedAnnotation.getBeginTimeBoundary());
	            }
	            // update existing Time_Subdivision and Included_In child tiers
	            forceChildAnnotationsOfAlignable(aa);
	        } else {
	            // delete
	            end.setTime(begin.getTime());
	            //aa.markDeleted(true);
	        }   
	    }  
	}
	
	/**
	 * Special treatment for Time Subdivision child annotations.
	 * @param aa the child annotation
	 * @param graphEndpoints he graph endpoints
	 * @param fixedAnnotation the ancestor in which interval the children should be forced
	 */
	private void forceTSInsideFixedInterval(AlignableAnnotation aa, TimeSlot[] graphEndpoints, AlignableAnnotation fixedAnnotation) {
	    Constraint cc = ((TierImpl) aa.getTier()).getLinguisticType().getConstraints();
	    if (cc == null || cc.getStereoType() != Constraint.TIME_SUBDIVISION) {
	        return;
	    }
	    if (!aa.getBegin().isTimeAligned() || !aa.getEnd().isTimeAligned()) {
	        return;
	    }
		TimeSlot begin = aa.getBegin();
		TimeSlot end = aa.getEnd();
		
		if (begin.getTime() < end.getTime() && end.getTime() < fixedAnnotation.getBegin().getTime() && 
		        begin != fixedAnnotation.getBegin() && begin != graphEndpoints[0]) {
		    begin.setTime(fixedAnnotation.getBegin().getTime());
		} else if (begin.getTime() < end.getTime() && begin.getTime() > fixedAnnotation.getEnd().getTime() &&
		        end != fixedAnnotation.getEnd() && end != graphEndpoints[1]) {
		    end.setTime(fixedAnnotation.getEnd().getTime());
		}
	}
	
	/**
	 * Propagate changes in existing Incuded_In annotations or toplevel annotations to 
	 * child annotations (parent listeners).
	 * Special case because Included In annotations are not part of the time slot based annotation chain.
	 * This should be done recursively.
	 * 
	 * @param aa the time alignable annotation that has been changed
	 */
	private void forceChildAnnotationsOfAlignable(AlignableAnnotation aa) {
	    Constraint cc = ((TierImpl) aa.getTier()).getLinguisticType().getConstraints();
	    if (cc == null) {
	        //return;
	    }
	    else if (cc.getStereoType() != Constraint.INCLUDED_IN && 
	            cc.getStereoType() != Constraint.TIME_SUBDIVISION) {
	        return;
	    }
	    List<Annotation> chan = aa.getParentListeners();
	    Object next;
	    AlignableAnnotation ann;
	    Constraint sc = null;
	    HashMap<Tier, ArrayList<AlignableAnnotation>> tsChildren = null;
	    for (int i = 0; i < chan.size(); i++) {
	        next = chan.get(i);
	        if (! (next instanceof AlignableAnnotation)) {
	            continue;
	        }
	        ann = (AlignableAnnotation) next;
	        sc = ((TierImpl) ann.getTier()).getLinguisticType().getConstraints();
	        if (sc.getStereoType() == Constraint.INCLUDED_IN) {
	            forceAnnotationInInterval(ann, aa);
	            if (ann.getBegin().getTime() == ann.getEnd().getTime()) {
	                ann.markDeleted(true);
	            }
	        } else if (sc.getStereoType() == Constraint.TIME_SUBDIVISION) {
	           if (tsChildren == null) {
	               tsChildren = new HashMap<Tier, ArrayList<AlignableAnnotation>>(3);
	           }
	           if (tsChildren.containsKey(ann.getTier())) {
	               tsChildren.get(ann.getTier()).add(ann);
	           } else {
	               ArrayList<AlignableAnnotation> al = new ArrayList<AlignableAnnotation>(5);
	               al.add(ann);
	               tsChildren.put(ann.getTier(), al);
	           }
	        }
	    }
	    if (tsChildren != null) {
	        Iterator<Tier> keyIt = tsChildren.keySet().iterator();
	        Object key;
	        while (keyIt.hasNext()) {
	            key = keyIt.next();
	            updateTSChildren(aa, (TierImpl) key, tsChildren.get(key));
	        }
	    }
	}
	
	/**
	 * Adjusts time of Time_Subdivision children of Included_In or toplevel annotations. This should be done recursively.
	 * @param parent the parent annotation, should be an Included_In annotation or toplevel annotation
	 * @param tier the tier the child annotations are part of, should be a Time_Subdivision tier
	 * @param tsAnnos the child annotations
	 */
	private void updateTSChildren(AlignableAnnotation parAnn, TierImpl tier, ArrayList<AlignableAnnotation> tsAnnos) {
	    if (tier.getLinguisticType().getConstraints() == null || 
	            tier.getLinguisticType().getConstraints().getStereoType() != Constraint.TIME_SUBDIVISION) {
	        return;
	    }
	    Collections.sort(tsAnnos);
	    AlignableAnnotation ann;
	    boolean shouldPropagate = false;
	    for (int i = 0; i < tsAnnos.size(); i++) {
	        ann = tsAnnos.get(i);
	        shouldPropagate = false;
	        if (ann.getBegin() == parAnn.getBegin()) {
	            // the first child
	            if (ann.getEnd().isTimeAligned() && ann.getEnd().getTime() < parAnn.getBegin().getTime()) {
	                ann.markDeleted(true);// or set Begin = End
	            } else {
	                shouldPropagate = true;
	            }
	        } else {
	            if (!ann.getBegin().isTimeAligned() && parAnn.getBegin().isAfter(ann.getEnd())) {
					transcription.getTimeOrder().removeTimeSlot(ann.getBegin());
					transcription.getTimeOrder().insertTimeSlot(ann.getBegin(), parAnn.getBegin(), null);
					shouldPropagate = true;
	            }
	            if ((ann.getEnd().isTimeAligned() && parAnn.getBegin().getTime() >= ann.getEnd().getTime()) ||
	                    (ann.getBegin().isTimeAligned() && parAnn.getEnd().getTime() <= ann.getBegin().getTime())) {
	                ann.markDeleted(true);
	            }
	            if (ann.getBegin().isTimeAligned() && parAnn.getBegin().getTime() >= ann.getBegin().getTime() &&
	                    ann.getBegin() != parAnn.getBegin()) {
	                ann.setBegin(parAnn.getBegin());
	                if (!ann.getEnd().isTimeAligned() && ann.getBegin().isAfter(ann.getEnd())) {
						transcription.getTimeOrder().removeTimeSlot(ann.getEnd());
						transcription.getTimeOrder().insertTimeSlot(ann.getEnd(), ann.getBegin(), parAnn.getEnd());
	                }
	                shouldPropagate = true;
	            }
	            if (ann.getEnd().isTimeAligned() && parAnn.getEnd().getTime() <= ann.getEnd().getTime() && 
	                    ann.getEnd() != parAnn.getEnd()) {
	                ann.setEnd(parAnn.getEnd());
	                shouldPropagate = true;
	            }
	            if (!ann.getEnd().isTimeAligned() && ann.getEnd().isAfter(parAnn.getEnd())) {
					transcription.getTimeOrder().removeTimeSlot(ann.getEnd());
					transcription.getTimeOrder().insertTimeSlot(ann.getEnd(), ann.getBegin(), parAnn.getEnd());
					shouldPropagate = true;
	            }
	            if (!ann.getBegin().isTimeAligned() && !ann.getEnd().isTimeAligned() && 
	                    ann.getBegin().isAfter(ann.getEnd())) {
					transcription.getTimeOrder().removeTimeSlot(ann.getEnd());
					transcription.getTimeOrder().insertTimeSlot(ann.getEnd(), ann.getBegin(), parAnn.getEnd());
					shouldPropagate = true;
	            }
	        }
	        if (shouldPropagate) {
	            forceChildAnnotationsOfAlignable(ann);
	        }
	    }
	}
	
	/**
	 * Only makes changes to annotations on this tier. 
	 * @param graphEndpoints
	 * @param fixedSlot1
	 * @param fixedSlot2
	 */
	private void forceAnnotChainOutOfFixedInterval(TimeSlot[] graphEndpoints, TimeSlot fixedSlot1, 
		TimeSlot fixedSlot2) {
		TimeSlot annBegin = graphEndpoints[0];
		TimeSlot annEnd = graphEndpoints[0];
		List<Annotation> annotsForTS = null;
		AlignableAnnotation currentAnn = null;
		
		boolean passedFixedSlot1 = false;
		boolean passedFixedSlot2 = false;

		if (annBegin == fixedSlot1 || annBegin.getTime() >= fixedSlot1.getTime()) {
		    passedFixedSlot1 = true;
		}
		
		while (annEnd != null) {
			annotsForTS = this.getAnnotsBeginningAtTimeSlot(annBegin);	
			
			if (annotsForTS != null && annotsForTS.size() > 0) {
				currentAnn = (AlignableAnnotation) annotsForTS.get(0);
				annEnd = currentAnn.getEnd();	
				
				if (passedFixedSlot1 && !passedFixedSlot2) {
					 if (annEnd != fixedSlot2 && annEnd.isTimeAligned() && 
					 	annEnd.getTime() > fixedSlot1.getTime()) {
					 		annEnd.setTime(fixedSlot1.getTime());
					 	}
				}
				
				if (passedFixedSlot2 && 
					annEnd.isTimeAligned() && 
					annEnd.getTime() < fixedSlot2.getTime()) {
							
					annEnd.setTime(fixedSlot2.getTime());
				}
				
				if (annEnd.getTime() >= fixedSlot1.getTime() /*|| annEnd == fixedSlot1*/) {
					passedFixedSlot1 = true;
				}
				if (annEnd == fixedSlot2) {
					passedFixedSlot2 = true;
				}
				
				annBegin = annEnd;
				if (annEnd == graphEndpoints[1]) {
				    break;
				}
			}
			else {
				annEnd = null;
			}			
		}		
	}
	
	/**
	 * Propagate changes in annotations on this tier to dependent tiers. Relies on NoTimeGapWithinParent's
	 * detachAnnotation() method to restore the chain.
	 * @param aa the parent annotation
	 * @param graphEndpoints the endpoints of the graph
	 * @param fixedSlot1 fixed begin slot
	 * @param fixedSlot2 fixed end slot
	 * @param left if true force to the left
	 */
	private void forceChildChainOutOfInterval(AlignableAnnotation aa, TimeSlot[] graphEndpoints, 
	        TimeSlot fixedSlot1, TimeSlot fixedSlot2, boolean left) {
	   List<TierImpl> ct = this.getDependentTiers(); 
	   TierImpl t;
	   for (int i = 0; i < ct.size(); i++) {
	       t = ct.get(i);
	       if (t.getLinguisticType().getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION) {
	           if (left) {
	             t.forceAnnotChainOutOfFixedInterval(new TimeSlot[] {aa.getBegin(), aa.getEnd()}, fixedSlot1, fixedSlot2);
	             continue;
	           }
	           
	        TimeSlot annBegin = aa.getBegin();
	   		TimeSlot annEnd = aa.getBegin();
	   		List<Annotation> annotsForTS = null;
	   		AlignableAnnotation currentAnn = null;
	   		
	   		boolean passedFixedSlot1 = false;
	   		boolean passedFixedSlot2 = false;

	   		if (annBegin == fixedSlot1 || annBegin.getTime() >= fixedSlot1.getTime()) {
	   		    passedFixedSlot1 = true;
	   		}
	   		if (annBegin == fixedSlot2 || annBegin.getTime() >= fixedSlot2.getTime()) {
	   		    passedFixedSlot2 = true;
	   		}
	   		while (annEnd != null) {
	   			annotsForTS = t.getAnnotsBeginningAtTimeSlot(annBegin);	
	   			
	   			if (annotsForTS != null && annotsForTS.size() > 0) {
	   				currentAnn = (AlignableAnnotation) annotsForTS.get(0);
	   				annEnd = currentAnn.getEnd();	
	   				
	   				if (passedFixedSlot1 && !passedFixedSlot2) {
	   					 if (annEnd != fixedSlot2 && annEnd.isTimeAligned() &&
	   					         annEnd != graphEndpoints[1] &&
	   					 	annEnd.getTime() > fixedSlot1.getTime()) {
	   					 		annEnd.setTime(fixedSlot1.getTime());
	   					 	}
	   				}
	   				
	   				if (passedFixedSlot2 && annEnd != graphEndpoints[1] &&
	   					annEnd.isTimeAligned() && 
	   					annEnd.getTime() < fixedSlot2.getTime()) {
	   							
	   					annEnd.setTime(fixedSlot2.getTime());
	   				}
	   				
	   				if (annEnd.getTime() >= fixedSlot1.getTime() /*|| annEnd == fixedSlot1*/) {
	   					passedFixedSlot1 = true;
	   				}
	   				if (annEnd == fixedSlot2) {
	   					passedFixedSlot2 = true;
	   				}
	   				
	   				annBegin = annEnd;
	   				if (annEnd == graphEndpoints[1]) {
	   				    break;
	   				}
	   			}
	   			else {
	   				annEnd = null;
	   			}			
	   		}
	       }
	   }
	}
	
	private void repositionUnalignedSlots(TimeSlot graphBegin) {
		TimeSlot annBegin = graphBegin;
		TimeSlot annEnd = graphBegin;
		
		List<Annotation> annotsForTS = null;
		AlignableAnnotation currentAnn = null;
		
		while (annEnd != null) {
			annotsForTS = this.getAnnotsBeginningAtTimeSlot(annBegin);	

			if (annotsForTS != null && annotsForTS.size() > 0) {
				currentAnn = (AlignableAnnotation) annotsForTS.get(0);
				annEnd = currentAnn.getEnd();	
				
				if (	(annBegin.isAfter(annEnd) && annBegin.isTimeAligned() && !annEnd.isTimeAligned()) ||
						(annBegin.isAfter(annEnd) && !annBegin.isTimeAligned() && !annEnd.isTimeAligned())){

					transcription.getTimeOrder().removeTimeSlot(annEnd);
					transcription.getTimeOrder().insertTimeSlot(annEnd, annBegin, null);					
				}
				if (annBegin.isAfter(annEnd) && !annBegin.isTimeAligned() && annEnd.isTimeAligned()){
				    
					transcription.getTimeOrder().removeTimeSlot(annBegin);
					// this means inserting at the end.....
					transcription.getTimeOrder().insertTimeSlot(annBegin, null , annEnd);

					// the operation becomes more and more expensive...
					/*
					TimeSlot prevSlot = graphBegin;
					TimeSlot loopSlot;
					java.util.Enumeration slotEn = ((TranscriptionImpl) parent).getTimeOrder().elements();
					while (slotEn.hasMoreElements()) {
					    loopSlot = (TimeSlot) slotEn.nextElement();
					    if (loopSlot == annEnd) {
					        break;
					    }
					    prevSlot = loopSlot;
					}
					slotEn = null;
					((TranscriptionImpl) parent).getTimeOrder().insertTimeSlot(annBegin, prevSlot , annEnd);
					// start all over again
					//repositionUnalignedSlots(graphBegin);
					//return;	
					 */			 
				}
				annBegin = annEnd;
			}
			else {
				annEnd = null;
			}			
		}		
	}
	
	
	/**
	 * Alternative way to correct overlapping annotation time intervals by shifting other
	 * annotations to the left and right (like a "bulldozer".
	 * Algorithm used:
	 * - check if a right shift is necessary, if yes:
	 * - determine shift
	 * - Make an iterator point at fixedAnnotation
	 * - iterate to the next, and check if there is a gap between the two
	 * - if yes, subtract it from the shift value
	 * - shift begin and end times
	 * - repeat all steps for left shift
	 */
	public void correctOverlapsByPushing(AlignableAnnotation fixedAnnotation, long oldBegin, long oldEnd) {
		long newBegin = fixedAnnotation.getBegin().getTime();
		long newEnd = fixedAnnotation.getEnd().getTime();

		long storeOldEnd = oldEnd;

	//	System.out.println("");
	//	System.out.println("fixedAnnotation: " + fixedAnnotation.getValue());
	//	System.out.println("oldBegin: " + oldBegin + " , oldEnd: " + oldEnd);

		// right shift
		if (newEnd > oldEnd) {	// implies that end is time aligned
			AlignableAnnotation a = null;
			long timeGap = 0;

			long rightShift = newEnd - oldEnd;

			Iterator<Annotation> annIter = annotations.iterator();

			// iterate until fixedAnnotation
			while (annIter.hasNext()) {
				a = (AlignableAnnotation) annIter.next();

				if (a == fixedAnnotation) {
					break;
				}
			}


			while (annIter.hasNext()) {
				a = (AlignableAnnotation) annIter.next();

				timeGap = a.getBegin().getTime() - oldEnd;
				if (timeGap > 0) {
					rightShift -= timeGap;
				}

				// actual shift
				if (rightShift > 0) {
				//	System.out.println("rightShift ann: " + a.getValue() + " over distance: " + rightShift);

					oldEnd = a.getEnd().getTime();

					//a.getBegin().setTime(a.getBegin().getTime() + rightShift);
					//a.getEnd().setTime(a.getEnd().getTime() + rightShift);
					shiftConnectedAnnots(a, rightShift);
				}
				if (rightShift <= 0) {
					break;
				}
			}
			/*
			// finally reposition time subdivision and included in dependent annotations
			List<Annotation> parentListeners = new ArrayList<Annotation>(fixedAnnotation.getParentListeners());
			long beginShift = newBegin - oldBegin;
			for (Annotation dann : parentListeners) {
				if (dann instanceof AlignableAnnotation) {
					AlignableAnnotation depAA = (AlignableAnnotation) dann;
					long depAAnewBT = Math.min(depAA.getBegin().getTime() + beginShift, newEnd);
					if (depAA.getBegin() == fixedAnnotation.getBegin()) {
						depAAnewBT = depAA.getBegin().getTime();
					}
					long depAAnewET = Math.min(depAA.getEnd().getTime() + beginShift, newEnd);
					if (depAA.getEnd() == fixedAnnotation.getEnd()) {
						depAAnewET = depAA.getEnd().getTime();
					}
					if (depAAnewBT >= newBegin && depAAnewET <= newEnd 
							 ) {
						if (depAA.getBegin() != fixedAnnotation.getBegin()) {
							depAA.getBegin().setTime(depAAnewBT);
						}
						if (depAA.getEnd() != fixedAnnotation.getEnd()) {
							depAA.getEnd().setTime(depAAnewET);
						}
//						forceChildAnnotationsOfAlignable(depAA);
					}
				}
			}
			*/
		}

		// left shift
		if (	((newBegin < oldBegin) && (fixedAnnotation.getBegin().isTimeAligned()))   ||
			(oldBegin == storeOldEnd)) {	// new annotation added


		//	System.out.println("starting left shift");
			AlignableAnnotation a = null;
			long timeGap = 0;

			long leftShift = oldBegin - newBegin;

			boolean leftShiftNotYetSpecified = false;
			if (oldBegin == storeOldEnd) {	// in case of inserting a new annotation
				leftShiftNotYetSpecified = true;
			}

			List<Annotation> annVector = new ArrayList<Annotation>(annotations);
			ListIterator<Annotation> annIter2 = annVector.listIterator();

			// iterate until fixedAnnotation
			while (annIter2.hasNext()) {
				a = (AlignableAnnotation) annIter2.next();

				if (a == fixedAnnotation) {
					break;
				}
			}

			boolean firstIteration = true;
			while (annIter2.hasPrevious()) {
				a = (AlignableAnnotation) annIter2.previous();

				if (a == fixedAnnotation) {
					firstIteration = false;
					continue;
				}
				timeGap = oldBegin - a.getEnd().getTime();
				if (timeGap > 0) {
					leftShift -= timeGap;
				}

				// actual shift
				if ((leftShift > 0) || leftShiftNotYetSpecified) {


					// first iteration is fixedAnnotation again, since
					// ListIterator gives same ref twice when switching from next to previous.
					if (!firstIteration) {
						if (leftShiftNotYetSpecified) {
							leftShift = a.getEnd().getTime() - storeOldEnd;
							if (leftShift < 0) { leftShift = 0; }

							leftShiftNotYetSpecified = false;
						}
						oldBegin = a.getBegin().getTime();
					//	System.out.println("leftShift ann: " + a.getValue() + " over distance: " + leftShift);
						//a.getBegin().setTime(a.getBegin().getTime() - leftShift);
						//a.getEnd().setTime(a.getEnd().getTime() - leftShift);
						shiftConnectedAnnots(a, -leftShift);
					}
				}

				if (leftShift <= 0) {
					break;
				}
				firstIteration = false;
			}
		}
	}	
	
	private void shiftConnectedAnnots(AlignableAnnotation ann, long shift) {	
		TreeSet<AbstractAnnotation> connectedAnnots = new TreeSet<AbstractAnnotation>();
		TreeSet<TimeSlotImpl> connectedTimeSlots = new TreeSet<TimeSlotImpl>();
		
		//((TranscriptionImpl) getParent()).getConnectedAnnots(connectedAnnots, connectedTimeSlots, ann.getBegin());
		getTranscription().getConnectedAnnots(connectedAnnots, connectedTimeSlots, ann);
		TimeSlot prevSlot = null;
		Iterator<TimeSlotImpl> tsIter = connectedTimeSlots.iterator();
		while (tsIter.hasNext()) {
			TimeSlot ts = tsIter.next();

			if (ts.isTimeAligned()) {
				if ((ts.getTime() + shift) < 0) {
					ts.setTime(0);
				}
				else {
					if (prevSlot != null && !prevSlot.isTimeAligned()) {
						// first make sure it is after the previous unaligned slot

						transcription.getTimeOrder().removeTimeSlot(ts);
						transcription.getTimeOrder().insertTimeSlot(ts, prevSlot, null);

						// then update the time
						ts.setTime(ts.getTime() + shift);
					} else {
						ts.setTime(ts.getTime() + shift);
					}
						
				}
			} else {
				if (prevSlot != null) {
					transcription.getTimeOrder().removeTimeSlot(ts);
					transcription.getTimeOrder().insertTimeSlot(ts, prevSlot, null);

				}
			}
			prevSlot = ts;
		}
		
		if (connectedAnnots.size() > 0) {
			markAnnotationsForDeletion(connectedAnnots);
			transcription.pruneAnnotations(this); 	// prunes all tiers that might have been changed
		}	
	}
	
	private void markAnnotationsForDeletion(Collection<? extends Annotation> annots) {
		for (Annotation ann : annots) {

			if (ann instanceof AlignableAnnotation) {
				// mark deleted if time interval of an aligned ann has zero duration, or if there
				// is no non-zero gap left for an unaligned annotation
				//if (	((AlignableAnnotation) ann).getBeginTimeBoundary() >=
				//	((AlignableAnnotation) ann).getEndTimeBoundary()	) {
				if (((AlignableAnnotation) ann).calculateBeginTime() >=
					((AlignableAnnotation) ann).calculateEndTime()	) {
 				//	 System.out.println("begin: " + ((AlignableAnnotation) ann).getBeginTimeBoundary());
 				//	 System.out.println("end: " + ((AlignableAnnotation) ann).getEndTimeBoundary());
 				//	 System.out.println("mark deleted: " + ann.getValue());

					ann.markDeleted(true);
				}
			}
		//	else if (ann instanceof RefAnnotation) {
		// 		mark deleted if refcount is zero, implemented via ACMEditListener
		//	}
		}
	}

	/**
	 * 01 May 06: pruning of timeslots has been moved to the level of TranscriptionImpl.
	 * This method is mainly called from there and pruning of timeslots can be done 
	 * once after the pruning of annotations on all tiers.
	 * 
	 * @return true if something was changed.
	 */
	public boolean pruneAnnotations() {
		// removes all annotations that are marked deleted
		boolean somethingChanged = false;

		TreeSet<Annotation> copiedAnnotations = new TreeSet<Annotation>(annotations); //is this copying necessary??
		Iterator<Annotation> annIter = copiedAnnotations.iterator();
		while (annIter.hasNext()) {
			Annotation ann = annIter.next();

			if (ann.isMarkedDeleted()) {
				Constraint c = linguisticType.getConstraints();
				if (c != null) {
					c.detachAnnotation(ann, this);
				}
				annotations.remove(ann);	// don't call removeAnnotation, to prevent multiple notifications
				somethingChanged = true;
			}
		}

		//if(somethingChanged) {
		//	((TranscriptionImpl) parent).getTimeOrder().pruneTimeSlots();
		//	notifyListeners(ACMEditEvent.REMOVE_ANNOTATION, null);
		//}
		
		return somethingChanged;
	}


	/**
	 * <p>MK:02/06/21<br>Dynamic retrieval of tier properties, which can be of any type.
	 * Elan requires two of them.
	 * </p>
	 * @param element well-defined values are: PARTICIPANT, DEFAULT_LOCALE, ANNOTATOR
	 * */
	private void setMetadata(String  element, Object value) {
	    if (element == null || value == null) {
	        return;
	    }
		tierMetadata.put(element, value);

		modified(ACMEditEvent.CHANGE_TIER, null);
	}

	/**
	 * <p>MK:02/06/21<br>see setter</p>
	 */
	private Object getMetadataValue(String element) {
	    if (element == null) {
	        return null;
	    }
		return tierMetadata .get(element);
	}


	/**
	 * Set the linguistic type of the tier.
	 * If the tier already contains annotations, then
	 * if possible, add the tier to the Transcription before calling this,
	 * because it also tries to update CV Entry Ids, which requires the Transcription.
	 * See {@link #updateCVEntryIds()}.
	 */
	@Override
	public void setLinguisticType(LinguisticType theType) {
		linguisticType = theType;
		updateCVEntryIds();

		modified(ACMEditEvent.CHANGE_TIER, null);
	}

	
	@Override
	public LinguisticType getLinguisticType() {
	//	return (String) getMetadataValue("LINGUISTIC_TYPE");
		return linguisticType;
	}


	/**
	 * <p>MK:02/06/21 Starting.</p>
	 * @return	the participant of 'this' tier or default
	 */
	@Override
	public String getParticipant() {
		String participant = "not specified";
		if (getMetadataValue("PARTICIPANT") != null) {
			participant = (String) getMetadataValue("PARTICIPANT");
		}

		return participant;
	}


	@Override
	public void setParticipant(String theParticipant) {
		setMetadata("PARTICIPANT", theParticipant);

		// notifyListeners implicitly called via setMetadata
	}

	/**
	 * Returns the "Annotator" of this tier, if specified.
	 * @return the annotator or an empty String
	 */
	@Override
	public String getAnnotator() {
		String annotator = (String) getMetadataValue("ANNOTATOR");
		
		return annotator != null ? annotator : "";
	}
	
	/**
	 * Sets the annotator of this tier, the person who creates/d the 
	 * annotations of this tier.
	 * 
	 * @param annotator the annotator id/name
	 */
	@Override
	public void setAnnotator(String annotator) {
		setMetadata("ANNOTATOR", annotator);
	}
	
	/**
	 * <p>MK:02/06/21 Starting.</p>
	 * @return	the locale of 'this' tier or default
	 */
	public Locale getDefaultLocale() {
		//Locale locale = new Locale("not specified", "", "");
		// HS Feb 2010 allow null for Locale, use empty string internally
		Locale locale = null;
		Object val = getMetadataValue("DEFAULT_LOCALE");
		if (val instanceof Locale) {
			locale = (Locale) val;
		}

		return locale;
	}


	public void setDefaultLocale(Locale l) {
		// HS Feb 2010 allow null for Locale, use empty string internally
		if (l == null) {
			setMetadata("DEFAULT_LOCALE", "");
		} else {
			setMetadata("DEFAULT_LOCALE", l);
		}

		// notifyListeners implicitly called via setMetadata
	}


	/**
	 * Don't use this method when there are already annotations;
	 * it's unsafe.
	 * HS Aug. 2011: removed obsolete constraint checks and updating of annotations (they didn't work)
	 * Now only the parent is set and an ACMEditEvent is generated. 
	 * 
	 * @param newParent may or may not be null.
	 */
	public void setParentTier(TierImpl newParent) {
		if (annotations.size() > 0) {
			// don't change the parent tier. An exception should be thrown.
			return; 
		}
		
		parentTier = newParent;

		modified(ACMEditEvent.CHANGE_TIER, null);
	}

	/**
	 * Stub method to satisfy the interface.
	 * If at some point it will be allowed to have other kinds of
	 * Tiers in a TranscriptionImpl, that needs to be checked here
	 * (among other places).
	 * <p>
	 * Currently this method is never called (nor should it be).
	 */
	@Override
	public void setParentTier(Tier newParent) {
		setParentTier((TierImpl)newParent);
	}

	private void enforceConstraints() {
		if (linguisticType.hasConstraints()) {
			Constraint c = linguisticType.getConstraints();
			c.enforceOnWholeTier(this);
		}
	}



	private Annotation selectParentCandidate(TierImpl parent, Annotation forAnnotation) {
		Annotation selectedCandidate = null;

		long begin = forAnnotation.getBeginTimeBoundary();
		long end = forAnnotation.getEndTimeBoundary();

		List<Annotation> candidates = parent.getOverlappingAnnotations(begin, end);

		// A candidate is only available if it does not already have a dependent annotation
		// on this tier. Select the first available candidate.

		Iterator<Annotation> candIter = candidates.iterator();
		while (candIter.hasNext()) {
			boolean candAvailable = true;

			Annotation cand = candIter.next();
		//	System.out.println("Ann: " + cand.getValue() + " is candidate parent for ann: " + forAnnotation.getValue());

			Iterator<Annotation> listenerIter = ((AbstractAnnotation) cand).getParentListeners().iterator();
			while (listenerIter.hasNext()) {
				Annotation listener = listenerIter.next();
				if (listener.getTier() == this) {
					candAvailable = false;
					break;
				}
			}

			if (candAvailable) {
				selectedCandidate = cand;
				break;
			}
		}

		return selectedCandidate;
	}


	/**
	 * @return the parent tier of 'this' tier in the current transcription.
	 * </p>
	 */
	@Override
	public TierImpl getParentTier() {
		return parentTier;
	}

	/**
	 * @return if 'this' tier has a parent in the current transcription.
	 * </p>
	 */
	@Override
	public boolean hasParentTier() {
		return parentTier != null;
	}

	/**
	 * @return the root tier in the current transcription.
	 * </p>
	 * */
	@Override
	public final TierImpl getRootTier() {
		if (parentTier == null) {
			return this;
		}
		return parentTier.getRootTier();
	}

	/**
	 * AK: 11/07/02
	 * @return itself or nearest ancestorTier with Linguistic Type TIME_SUBDIVISION
	 *
	 */
	public final TierImpl getTimeSubDivAncestor(){
		Constraint constraint = getLinguisticType().getConstraints();
		if(constraint!=null) {
			if(constraint.getStereoType()==Constraint.TIME_SUBDIVISION) {
				return this;
			}
		}
		if(parentTier!= null) {
			return parentTier.getTimeSubDivAncestor();
		}
		return null;
	}

	/**
	 * Returns whether the parameter is a (strict) ancestor of this tier.
	 * So a tier is not an ancestor of itself.
	 */
	@Override
	public boolean hasAncestor(Tier ancestor) {
		if (parentTier == null) {
			return false;
		}

		if (parentTier == ancestor) {
			return true;
		} else {
			return parentTier.hasAncestor(ancestor);
		}
	}


	/**
	 * Returns all direct and indirect children of this tier.
	 */
	@Override
	public List<TierImpl> getDependentTiers() {
		List<TierImpl> depTiers = new ArrayList<TierImpl>();

		for (TierImpl t : transcription.getTiers()) {
			if (t.hasAncestor(this)) {
				depTiers.add(t);
			}
		}


		return depTiers;
	}

	/**
	 * Returns all direct children of this tier.
	 */
	@Override
	public List<TierImpl> getChildTiers() {
		List<TierImpl> childTiers = new ArrayList<TierImpl>();

		for (TierImpl t : transcription.getTiers()) {
			if (t.getParentTier() == this) {
				childTiers.add(t);
			}
		}

		return childTiers;
	}
	
	/**
	 * We may want to have a method which tells us if this tier
	 * has any child tiers, without looking all of them up.
	 */
	
	/**
	 * Returns the annotation at the specified time, if any. Called at edit time.
	 * While editing the virtual time of unaligned time slots has to be recalculated.
	 * The comparison of times is begintime inclusive, end time exclusive!
	 * 
	 * Note oct 04: addition related to performance of unaligned slots.. temporary?
	 * 
	 * @param theTime the time
	 * @param forceRecalculation if true the precalculated proposed time for unaligned
	 * time slots is ignored
	 * @return tne annotation at the specified time, or null
	 */
	public Annotation getAnnotationAtTime(long theTime, boolean forceRecalculation) {
		Annotation result = null;

			if (!isTimeAlignable() || !forceRecalculation) {
				Iterator<Annotation> annIter = annotations.iterator();
				while (annIter.hasNext()) {
					Annotation ann = annIter.next();
					if (	ann.getBeginTimeBoundary() <= theTime &&
							//ann.getEndTimeBoundary() >= theTime	) {
							ann.getEndTimeBoundary() > theTime	) {

						result = ann;				
						break;
					}
				}
			} else if (getParentTier() == null || (linguisticType.getConstraints() != null && 
					linguisticType.getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {// aligned annotations
				Iterator<Annotation> annIter = annotations.iterator();
				while (annIter.hasNext()) {
					AlignableAnnotation ann = (AlignableAnnotation) annIter.next();
					long b = ann.calculateBeginTime();
					long e = ann.calculateEndTime();

					if ( b <= theTime && e > theTime ) {

						result = ann;				
						break;
					}
				}
			} else { // alignable dependent tier, time subdivision
				// first try to find a fully time-aligned annotation
				Iterator<Annotation> annIter = annotations.iterator();
				while (annIter.hasNext()) {
					AlignableAnnotation ann = (AlignableAnnotation) annIter.next();
					if (ann.getBegin().isTimeAligned() && ann.getEnd().isTimeAligned()) {
						if (ann.getBegin().getTime() <= theTime && ann.getEnd().getTime() > theTime) {
							result = ann;
							break;
						}
						if (ann.getBegin().getTime() > theTime) {
							break;
						}
					}
				}
				// if no fully aligned annotation found
				if (result == null) {
				    TierImpl root = getRootTier();
				    AlignableAnnotation rootAnn = (AlignableAnnotation) root.getAnnotationAtTime(theTime);
				    if (rootAnn != null) {
				    	// create a tier hierarchy
				    	ArrayList<TierImpl> relTiers = new ArrayList<TierImpl>();
				    	relTiers.add(this);
				    	TierImpl pt = getParentTier();
				    	while (pt != null) {
				    		relTiers.add(0, pt);
				    		pt = pt.getParentTier();
				    	}
				    	
				    	// hier... new time proposals
				    	TimeProposer2 tp2 = new TimeProposer2();
				    	result = tp2.getAnnotationAtTime(relTiers, rootAnn, this, theTime);
				    }
				}
			}
	
		return result;
	}

	// Assumes that overlapping annotations on the same tier are not allowed !!!!!
	public Annotation getAnnotationAtTime(long theTime) {
		return getAnnotationAtTime(theTime, false);
		/*//oct 04 old implementation
		Annotation result = null;

		Iterator annIter = annotations.iterator();
		while (annIter.hasNext()) {
			Annotation ann = (Annotation) annIter.next();
		*/ // end old implementation
		/*
			if (ann instanceof AlignableAnnotation) {	// IGNORE REFS TO REFANNOTATIONS FOR THE MOMENT!!
				if ( 	((AlignableAnnotation) ann).getBegin().isTimeAligned() &&
					((AlignableAnnotation) ann).getEnd().isTimeAligned() &&
					((AlignableAnnotation) ann).getBegin().getTime() <= theTime &&
					((AlignableAnnotation) ann).getEnd().getTime() >= theTime	) {

					result = ann;
					break;
				}
			}
		*/

		/*	if (ann instanceof AlignableAnnotation) {
				if (	((AlignableAnnotation) ann).getBeginTimeBoundary() <= theTime &&
					((AlignableAnnotation) ann).getEndTimeBoundary() >= theTime	) {

					result = ann;
					break;
				}
			}
		*/
		/* oct 04 old implementation
			if (	ann.getBeginTimeBoundary() <= theTime &&
//				ann.getEndTimeBoundary() >= theTime	) {
				ann.getEndTimeBoundary() > theTime	) {

				result = ann;				
				break;
			}
		}

		return result;
		// end old implementation
		*/
	}

	// KEEP until after testing of setParentTier method
/*	public Vector getOverlappingAnnotations(long t1, long t2) {
		Vector annots = new Vector();

		// For the moment, return empty vector in case of RefAnnotations
		// For the moment, ignore partially aligned AlignableAnnotations
		Iterator annIter = annotations.iterator();
		while (annIter.hasNext()) {
			Annotation ann = (Annotation) annIter.next();

			if (ann instanceof AlignableAnnotation) {
				if ( 	((AlignableAnnotation) ann).getBegin().isTimeAligned() &&
					((AlignableAnnotation) ann).getEnd().isTimeAligned() ) {

					long b = ((AlignableAnnotation) ann).getBegin().getTime();
					long e = ((AlignableAnnotation) ann).getEnd().getTime();

					if (	((t1 <= b) && (b < t2)) ||
						((t1 < e) && (e <= t2)) ||
						((b <= t1) && (t1 < e)) ||
						((b < t2) && (t2 <= e)) ) {

						annots.add(ann);
					}
				}
			}
		}

		return annots;
	}
*/

	// HB, 1 aug 02: replacement, using time boundaries.
	// Since this method was used only for setParent, we may want to keep
	// the old implementation until setParentTier is revised, tested and found OK.
	public List<Annotation> getOverlappingAnnotations(long t1, long t2) {
		//return getOverlappingAnnotations(t1, t2, false);
		//  old implementation... 10-04
		List<Annotation> annots = new ArrayList<Annotation>();

		for (Annotation ann : annotations) {

			long b = ann.getBeginTimeBoundary();
			long e = ann.getEndTimeBoundary();

			if (	((t1 <= b) && (b < t2)) ||
				((t1 < e) && (e <= t2)) ||
				((b <= t1) && (t1 < e)) ||
				((b < t2) && (t2 <= e)) ) {

				annots.add(ann);
			}
			// if the treeset is consistent...
			//if (b > t2) {
			//    break;
			//}
		}

		return annots;
		// end old implementation...
		// 
	}
	
	/**
	 * To be called at edit time. This method does not use the precalculated proposed,
	 * virtual time for unaligned time slots.
	 * 
	 * Note oct 04: addition related to performance of unaligned slots.. temporary?
	 * 
	 * @see #getOverlappingAnnotations(long, long)
	 * @param t1 begin time
	 * @param t2 end time
	 * @return a Vector containing overlapping annotations
	 */
	/*
	public Vector getOverlappingAnnotations(long t1, long t2, boolean forceRecalculation) {
		Vector annots = new Vector();
			if (!isTimeAlignable() || !forceRecalculation) {
			
				Iterator annIter = annotations.iterator();
				while (annIter.hasNext()) {
					Annotation ann = (Annotation) annIter.next();
					
					long b = ann.getBeginTimeBoundary();
					long e = ann.getEndTimeBoundary();
			
					if (	((t1 <= b) && (b < t2)) ||
						((t1 < e) && (e <= t2)) ||
						((b <= t1) && (t1 < e)) ||
						((b < t2) && (t2 <= e)) ) {
			
						annots.add(ann);
					}
				}
			} else {
				Iterator annIter = annotations.iterator();
				while (annIter.hasNext()) {
					AlignableAnnotation ann = (AlignableAnnotation) annIter.next();
					
					long b = ann.calculateBeginTime();
					long e = ann.calculateEndTime();
			
					if ( ((t1 <= b) && (b < t2)) ||
						((t1 < e) && (e <= t2)) ||
						((b <= t1) && (t1 < e)) ||
						((b < t2) && (t2 <= e)) ) {
			
						annots.add(ann);
					}
				}
			}

		return annots;
	}
	*/
	
	/**
	 * A method to get the overlapping annotations based on the timeslots order.<br>
	 * <b>Note: </b>Currently annotations using the specified timeslots are 
	 * not included in the result.
	 * @param t1 the begin time slot
	 * @param t2 the end time slot
	 * @return a List of annotations 
	 */
	public List<AlignableAnnotation> getOverlappingAnnotations(TimeSlot t1, TimeSlot t2) {
		List<AlignableAnnotation> v = new ArrayList<AlignableAnnotation>();
		
		// HS dec 05: since only AlignableAnnotations refer to a TimeSlot directly
		// return if the tier is not alignable.
		if (!this.isTimeAlignable()) {
			return v;
		}
		
		for (Annotation a : annotations) {
			AlignableAnnotation ann = (AlignableAnnotation) a;
			TimeSlot bt =  ann.getBegin();
			TimeSlot et =  ann.getEnd();
			if ( (et.getIndex() > t1.getIndex() && et.getIndex() < t2.getIndex()) ||
				 (bt.getIndex() > t1.getIndex() && bt.getIndex() < t2.getIndex()) ||			 
				 (bt.getIndex() < t1.getIndex() && et.getIndex() > t2.getIndex())) {
				v.add(ann);
			}
		}
		return v;
	}

	/**
	 * A method to get the overlapping annotations based on the timeslots order.<br>
	 * <b>Note: </b>Annotations using the specified timeslots are included in the result.
	 * 
	 * @param t1 the begin time slot
	 * @param t2 the end time slot
	 * @return a List of annotations 
	 */
	public List<AlignableAnnotation> getOverlappingAnnotationsIncludeExtremes(TimeSlot t1, TimeSlot t2) {
		List<AlignableAnnotation> v = new ArrayList<AlignableAnnotation>();
		
		// HS dec 05: since only AlignableAnnotations refer to a TimeSlot directly
		// return if the tier is not alignable.
		if (!this.isTimeAlignable()) {
			return v;
		}
		
		for (Annotation a : annotations) {
			AlignableAnnotation ann = (AlignableAnnotation) a;
			TimeSlot bt =  ann.getBegin();
			TimeSlot et =  ann.getEnd();
			if ( (et.getIndex() > t1.getIndex() && et.getIndex() <= t2.getIndex()) ||
				 (bt.getIndex() >= t1.getIndex() && bt.getIndex() < t2.getIndex()) ||			 
				 (bt.getIndex() < t1.getIndex() && et.getIndex() > t2.getIndex())) {
				v.add(ann);
			}
		}
		return v;
	}

	public List<Annotation> getAnnotationsUsingTimeSlot(TimeSlot theSlot) {
		List<Annotation> resultAnnots = new ArrayList<Annotation>();
		
		// HS dec 05: since only AlignableAnnotations refer to a TimeSlot directly
		// return if the tier is not alignable.
		if (!this.isTimeAlignable()) {
			return resultAnnots;
		}
		
		Iterator<Annotation> annIter = annotations.iterator();
		while (annIter.hasNext()) {
			Annotation ann = annIter.next();

			if (ann instanceof AlignableAnnotation) {
				if ((((AlignableAnnotation) ann).getBegin() == theSlot) ||
					(((AlignableAnnotation) ann).getEnd() == theSlot)) {

					resultAnnots.add(ann);
				}
			}
		}

		return resultAnnots;
	}


	public List<Annotation> getAnnotsBeginningAtTimeSlot(TimeSlot theSlot) {
		List<Annotation> resultAnnots = new ArrayList<Annotation>();

		// HS dec 05: since only AlignableAnnotations refer to a TimeSlot directly
		// return if the tier is not alignable.
		if (!this.isTimeAlignable()) {
			return resultAnnots;
		}
		
		boolean foundOne = false;//if there are more than one they will follow eachother 
		for (Annotation ann : annotations) {

			if (ann instanceof AlignableAnnotation) {
				if (((AlignableAnnotation) ann).getBegin() == theSlot) {
				    	foundOne = true;
					resultAnnots.add(ann);
					// for most tier types there will only be one annotation beginning at a 
					// particular time slot, we can break here
					if (linguisticType != null && linguisticType.getConstraints() != null &&
						(linguisticType.getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION ||
						        linguisticType.getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {
						//break;
					}
				} else if (foundOne) {
				    if (linguisticType != null && linguisticType.getConstraints() != null &&
							(linguisticType.getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION ||
							        linguisticType.getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {
							break;
						}
				}
			}
		}

		return resultAnnots;
	}

	public List<Annotation> getAnnotsEndingAtTimeSlot(TimeSlot theSlot) {
		List<Annotation> resultAnnots = new ArrayList<Annotation>();
		
		// HS dec 05: since only AlignableAnnotations refer to a TimeSlot directly
		// return if the tier is not alignable.
		if (!this.isTimeAlignable()) {
			return resultAnnots;
		}
		boolean foundOne = false;//if there are more than one they will follow eachother
		Iterator<Annotation> annIter = annotations.iterator();
		while (annIter.hasNext()) {
			Annotation ann = annIter.next();
	
			if (ann instanceof AlignableAnnotation) {
				if (((AlignableAnnotation) ann).getEnd() == theSlot) {
				    foundOne = true;
					resultAnnots.add(ann);
					// for most tier types there will only be one annotation beginning at a 
					// particular time slot, we can break here
					if (linguisticType != null && linguisticType.getConstraints() != null &&
						(linguisticType.getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION ||
						        linguisticType.getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {
						//break;
					}
				} else if (foundOne) {
				    if (linguisticType != null && linguisticType.getConstraints() != null &&
							(linguisticType.getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION ||
							        linguisticType.getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {
							break;
						}
				}
			}
		}

		return resultAnnots;
	}

	public boolean isTimeAlignable() {
		boolean timeAlignable = true;
		if (linguisticType != null) {
			timeAlignable = linguisticType.isTimeAlignable();
		}
		return timeAlignable;
	}
	
	/**
	 * Proposes a time value for an unaligned time slot on basis of subdivision in equal
	 * parts of available time interval between previous and next aligned slot of annotations
	 * ON THIS TIER.
	 *
	 * @return proposed end time
	 */
	
	public long proposeTimeFor(TimeSlot theSlot) {
		long proposedTime = 0;
					
			if (!this.hasParentTier()) {
				return transcription.getTimeOrder().proposeTimeFor(theSlot);
			}

			//TreeSet connectedAnnots = new TreeSet();
			//TreeSet connectedTimeSlots = new TreeSet();
			
			//((TranscriptionImpl) getParent()).getConnectedAnnots(connectedAnnots,
				//				connectedTimeSlots, theSlot);
								
			//List connectedAnnotVector = new List(connectedAnnots);
			
			// find 'begin' and 'end' timeslot of connected graph
		//	TimeSlot[] graphEndpoints = ((TierImpl) getTier()).getGraphEndpoints(connectedAnnotVector);
			TimeSlot[] graphEndpoints = getEndpointsOfSubchain(theSlot, true);
	
			TimeSlot aBegin = graphEndpoints[0];
			TimeSlot aEnd = graphEndpoints[0];
			
			List<Annotation> annsForTS = null;
			AlignableAnnotation currentA = null;
			boolean beginFound = false;
			int unalignedCounter = 1;
			int foundAtPosition = 0;
					
			TimeSlot segBegin = aBegin;
			TimeSlot segEnd = graphEndpoints[1];
			
			if (theSlot == aEnd) {	// unaligned begin slot
				aEnd = null;  // skip following while loop
			}
			
			while (aEnd != null) {
				annsForTS = getAnnotsBeginningAtTimeSlot(aBegin);	
				
				if (annsForTS != null && annsForTS.size() > 0) {
					currentA = (AlignableAnnotation) annsForTS.get(0);
					aEnd = currentA.getEnd();	
					
					if (aEnd.isTimeAligned() && !beginFound) { // reset counters whenever aligned slot found
						segBegin = aEnd;
						unalignedCounter = 1;	// reset
						foundAtPosition = 0; // reset, 8 sep 04
					}
					if (!aEnd.isTimeAligned()) {	// for each unaligned slot, increase counters
						if (!(aEnd == segEnd)) {
							unalignedCounter++;
							if (!beginFound) {
								foundAtPosition++;
							}
						}
					}
					if (aEnd.isTimeAligned() && beginFound) {
						segEnd = aEnd;
						break;
					}
							
					if (aEnd == theSlot) {
						beginFound = true;
					}
					
					aBegin = aEnd;
				}
				else {
					aEnd = null;
				}			
			}
				
			// hb, 5 oct 04. If segBegin or segEnd unaligned, get proposedTime from parent annotation
			long segB = 0;
			long segE = 0;
				
			if (getParentTier() != null) {
				if (!segBegin.isTimeAligned()) {
					segB = getParentTier().proposeTimeFor(segBegin);
				}
				else {
					segB = segBegin.getTime();
				}
				if (!segEnd.isTimeAligned()) {
					segE = getParentTier().proposeTimeFor(segEnd);
				}
				else {
					segE = segEnd.getTime();
				}
			}
				
	
			proposedTime = segB + foundAtPosition * ((segE - segB)/unalignedCounter);
			
			// correct if unaligned segBegin
			if ((theSlot == segBegin) && !(segBegin.isTimeAligned())) {
				proposedTime = segB;
			}
			// correct if unaligned segEnd
			if ((theSlot == segEnd) && !(segEnd.isTimeAligned())) {
				proposedTime = segE;
			}
		
		return proposedTime;		
	}
	
	//////////////////////
	/**
	 * Propagate changes to depending tiers after creation of a new annotation on a time subdivision tier.
	 */
	private void correctDependingOverlaps(AlignableAnnotation a) {
	    List<TierImpl> depTiers = getDependentTiers();

	    TierImpl tier;
	    depTiers.add(0, this);
	    ArrayList<AlignableAnnotation> annots = new ArrayList<AlignableAnnotation>();
	    TimeSlot bts = a.getBegin();// assumed to be time aligned
	    TimeSlot ets = a.getEnd();// assumed to be time aligned
	    
	    for (int i = 0; i < depTiers.size(); i++) {
	        tier = depTiers.get(i);
	        if (tier.isTimeAlignable()) {
	            getAnnotsChainStartingWithSlot(annots, tier, bts, ets);
	            getAnnotsChainEndingWithSlot(annots, tier, ets, bts);
	        }	        
	    }
	    
	    boolean pruneNeeded = false;
	    AlignableAnnotation ann;
	    for (int i = 0; i < annots.size(); i++) {
	        ann = annots.get(i);
	        if (ann != a && !ann.isMarkedDeleted()) {
	            ann.markDeleted(true);
	            pruneNeeded = true;
	        }
	    }

	    for (int i = 0; i < depTiers.size(); i++) {
	        tier = depTiers.get(i);
	        if (tier != this && tier.isTimeAlignable()) {
	            //tier.repositionUnalignedSlots(a.getBegin());
	            tier.repositionUnalignedSlots(a.getEnd());
	        }	        
	    }
	    
	    if (pruneNeeded) {
	       // ((TranscriptionImpl) parent).pruneAnnotations(this);  
		    for (int i = 0; i < depTiers.size(); i++) {
		        tier = depTiers.get(i);
		        tier.pruneAnnotationsWithoutDetach();
		    }
	    }
	}
	
	/**
	 * Should only be called for time alignable tiers.
	 * @see #getAnnotsChainEndingWithSlot(ArrayList, TierImpl, TimeSlot)
	 * @param annots the list where found annotations are stored
	 * @param bts begin time slot
	 * @param ets the end slot of the chain
	 */
	private void getAnnotsChainStartingWithSlot(ArrayList<AlignableAnnotation> annots, TierImpl tier, TimeSlot bts, TimeSlot ets) {
	    List<Annotation> v = tier.getAnnotsBeginningAtTimeSlot(bts);
	    AlignableAnnotation ann;
	    for (int i = 0; i < v.size(); i++) {
	        ann = (AlignableAnnotation) v.get(i);
	        if (!annots.contains(ann)) {
	           annots.add(ann);
	           if (ann.getEnd() != ets) {
	               getAnnotsChainStartingWithSlot(annots, tier, ann.getEnd(), ets);    
	           }
	        }
	    }
	}
	
	/**
	 * Should only be called for time alignable tiers.
	 * Counterpart of #getAnnotsChainStartingWithSlot but now starting with the end slot working to
	 * the beginning. In case the chain is temporarily broken during an edit action
	 * @see #getAnnotsChainStartingWithSlot(ArrayList, TierImpl, TimeSlot)
	 * @param annots the list where found annotations are stored
	 * @param ets the end time slot to start with
	 * @param bts begin time slot of the chain
	 */
	private void getAnnotsChainEndingWithSlot(ArrayList<AlignableAnnotation> annots, TierImpl tier, TimeSlot ets, TimeSlot bts) {
	    List<Annotation> v = tier.getAnnotsEndingAtTimeSlot(ets);
	    AlignableAnnotation ann;
	    for (int i = 0; i < v.size(); i++) {
	        ann = (AlignableAnnotation) v.get(i);
	        if (!annots.contains(ann)) {
	           annots.add(ann);
	           if (ann.getBegin() != bts) {
	               getAnnotsChainEndingWithSlot(annots, tier, ann.getBegin(), bts);    
	           }
	        }
	    }
	}
	
	/**
	 * Removes annotations that have been marked deleted without calling the Constraint's detachAnnotation method.
	 * Should only be used when the chain of remaining annotations has already been 'repaired'.
	 */
	public void pruneAnnotationsWithoutDetach() {
		Iterator<Annotation> annIter = annotations.iterator();
		while (annIter.hasNext()) {
			Annotation ann = annIter.next();

			if (ann.isMarkedDeleted()) {
			    annIter.remove();
			    transcription.modified(ACMEditEvent.REMOVE_ANNOTATION, ann);
			}
		}
	}	
	
	/**
	 * Find which language is effective (due to global and local preference)
	 * and the CV that it should be in.
	 * 
	 * @return a pair. Returns null if there is a problem looking it up.
	 * If there is clearly "no CV" set, returns a Pair with the cv element equal to null.
	 * If the desired language cannot be found in the CV, langIndex < 0.
	 */
	public Pair<ControlledVocabulary, Integer> getEffectiveLanguage() {
		// Do we currently have a parent to help us find CVs?
		if (transcription == null || !(transcription instanceof TranscriptionImpl)) {
			return null;
		}

		// Check if this tier is associated with a CV.
		// If not, we are done quickly.
		
		LinguisticType lt = getLinguisticType();
		String cvname = lt.getControlledVocabularyName();
		if (cvname == null || cvname.isEmpty()) {
			return new Pair<ControlledVocabulary, Integer>(null, 0);
		}
		// Can we find that CV?
		ControlledVocabulary cv = transcription.getControlledVocabulary(cvname);
		if (cv == null) {
			return null;
		}
		
		// First check if the tier language is available in the CV
		int langIndex = -1;
		String tierLanguage = getLangRef();
		if (tierLanguage != null) {
			langIndex = cv.getIndexOfLanguage(tierLanguage);			
		}
		// Then alternatively, if the global language is available.
		if (langIndex < 0) {
			String transcriptionLanguage = transcription.getCVLanguage();
			langIndex = cv.getIndexOfLanguage(transcriptionLanguage);
		}

		// langIndex may still be < 0 at this point.
		return new Pair<ControlledVocabulary, Integer>(cv, langIndex);
	}
	
	/**
	 * Update all Annotations on this tier, which are associated with Controlled Vocabulary Entries,
	 * to the new global language.
	 * However, this can be overridden by the Tier-local language.
	 * To be used when the globally preferred language or the tier-local language changes.
	 * 
	 * TODO: should this be undo-able??? In theory changing the language back and
	 * calling this method again should do the trick. (Could some cases be missed??)
	 * 
	 */
	public void updateCVLanguage() {
		// Short-circuit empty Tiers.
		if (annotations.isEmpty()) {
			return;
		}
		
		Pair<ControlledVocabulary, Integer> pair = getEffectiveLanguage();
		if (pair == null) {
			return;
		}
		
		ControlledVocabulary cv = pair.getFirst();
		int langIndex = pair.getSecond();
		
		// If there is any kind of problem finding the right language,
		// just leave the annotations alone.
		if (cv == null || langIndex < 0) {
			return;
		}
		// july 2018 if an external CV has not been loaded yet it has no entries and 
		// all links from annotations to cv entries would be removed in the
		// annotation loop following this test
		if (cv instanceof ExternalCV) {
			if ( !((ExternalCV) cv).isLoadedFromURL() &&
					!((ExternalCV) cv).isLoadedFromCache()) {
				return;
			}
		}
		
		// Ok, finally we can go and work on all annotations of this tier.
		for (Annotation a : annotations) {
			String cveId = a.getCVEntryId();
			CVEntry cve = cv.getEntrybyId(cveId);
			if (cve == null) {
				// The CV does not contain this ID any more?
				a.setCVEntryId(null);
			} else {
				String newWord = cve.getValue(langIndex);
				if (!newWord.isEmpty()) {
					a.setValue(newWord);
				}
			}
		}
	}
	
	/**
	 * Update all CVEntryIds of all Annotations to match with their values.
	 * To be used when (1) the Linguistic Type associated with a Tier gets a different CV,
	 * or when (2) the Tier gets a different Linguistic Type.
	 * <p>
	 * If the tier is currently not added to a transcription, it can't find CVs,
	 * so it can't do anything; so if possible, do that first.
	 */
	public void updateCVEntryIds() {
		// Short-circuit empty Tiers.
		if (annotations.isEmpty()) {
			return;
		}
		
		Pair<ControlledVocabulary, Integer> pair = getEffectiveLanguage();
		if (pair == null) {
			return;
		}
		
		ControlledVocabulary cv = pair.getFirst();
		int langIndex = pair.getSecond();

		if (cv == null) {
			// If "no CV" explicitly set, we should erase all Ids.
			for (Annotation a : annotations) {
				a.setCVEntryId(null);
			}		
			return;
		}
		if (langIndex < 0) {
			// Language not available: leave the annotations alone.
			return;
		}
		// july 2018 if an external CV has not been loaded yet it has no entries and 
		// all links from annotations to cv entries would be removed in the
		// annotation loop following this test
		if (cv instanceof ExternalCV) {
			if ( !((ExternalCV) cv).isLoadedFromURL() &&
					!((ExternalCV) cv).isLoadedFromCache()) {
				return;
			}
		}
		// Ok, finally we can go and work on all annotations of this tier.
		for (Annotation a : annotations) {
			CVEntry cve = cv.getEntryWithValue(langIndex, a.getValue());
			if (cve != null) {
				a.setCVEntryId(cve.getId());
			} else {
				a.setCVEntryId(null);				
			}
		}
	}

	@Override
	public String getExtRef() {
		return this.extRef;		
	}	
	
	@Override
	public void setExtRef(String extRef) {
		this.extRef = extRef;		
	}	
	
	@Override
	public String getLangRef() {
		return this.langRef;		
	}	
	
	/**
	 * Set the language reference (short id) of a Tier.
	 * It is assumed that this language is on the "recent language" list already.
	 * We can't add it anyway if it isn't, because we just get a partial identification.
	 */
	@Override
	public void setLangRef(String newLangRef) {
		if (langRef == newLangRef || 
				(langRef != null && langRef.equals(newLangRef))) {
			return;
		}
		langRef = newLangRef;
		updateCVLanguage();
	}

	/**
	 * An interface to get one of several field values from a TierImpl.
	 * This is used to generalise sorting or filtering tiers
	 * based on their annotator, linguistic type name, etc.
	 * 
	 * @author olasei
	 */
	public interface ValueGetter {
		/**
		 * Gets the appropriate sort key value.
		 */
		String getSortValue(TierImpl t);
	}

	public static class AnnotatorGetter implements ValueGetter {
		@Override
		public String getSortValue(TierImpl t) {
			return t.getAnnotator();
		};
	}

	public static class ParticipantGetter implements ValueGetter {
		@Override
		public String getSortValue(TierImpl t) {
			return t.getParticipant();
		};
	}

	public static class LinguisticTypeNameGetter implements ValueGetter {
		@Override
		public String getSortValue(TierImpl t) {
			return t.getLinguisticType().getLinguisticTypeName();
		};
	}

	public static class LanguageGetter implements ValueGetter {
		@Override
		public String getSortValue(TierImpl t) {
			String l = t.getLangRef();
			return l == null ? "" : l;
		};
	}

	public static class NameGetter implements ValueGetter {
		@Override
		public String getSortValue(TierImpl t) {
			return t.getName();
		};
	}
	
	public static class ParentTierNameGetter implements ValueGetter {
		@Override
		public String getSortValue(TierImpl t) {
			return t.getParentTier() == null ? "" : t.getParentTier().getName(); 
		};
	}
	
	public static class LocaleGetter implements ValueGetter {
		@Override
		public String getSortValue(TierImpl t) {
			return t.getDefaultLocale() == null ? "" : t.getDefaultLocale().toString(); 
		};
	}
}
