package mpi.eudico.server.corpora.clomimpl.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;


/**
 * DOCUMENT ME! $Id: NoTimeGapWithinParent.java,v 1.9 2005/01/18 12:22:55
 * hasloe Exp $
 *
 * @author $Author$
 * @version $Revision$
 */
public class NoTimeGapWithinParent extends ConstraintImpl {
    /**
     * Creates a new NoTimeGapWithinParent instance
     */
    public NoTimeGapWithinParent() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public int getStereoType() {
        return Constraint.NO_GAP_WITHIN_PARENT;
    }

    /**
     * Creates new or finds existing TimeSlots for a new Annotation on a Tier.
     * New TimeSlots will be added to the Transcription's TimeOrder, existing
     * TimeSlots will be adjusted if needed. The Tier object should then add
     * the Annotation to it's list of Annotations,  correct overlaps and mark
     * Annotations for deletion, if necessary.  Note HS aug 2005: the
     * mechanism applied here sometimes conflicts with the  the graph based
     * mechanism applied in TierImpl to solve overlaps etc. This was the cause
     * of the faulty behavior (bug) when an Annotation was  created on a
     * TimeSubdivision Tier, with a begin time equal to that of the  parent
     * Annotation's begin time while there already were one or more child
     * Annotations on that Tier, on that position (the begin time of parent
     * and  new child would be equal to the end time). Existing annotations
     * are marked for deletion based on their begin and end time  (begin >=
     * end). To acclompish this time values of TimeSlots are altered.  But
     * since the new and the existing annotation share the same begin time
     * slot  (the begin slot of the parent), this can not be applied in such
     * case.  Disconnecting the existing annotation from the graph would make
     * the graph  based iteration over a chain of depending annotations fail.
     * To (temporarely) solve this an exisitng annotation is now sometimes
     * marked for deletion here, which looks odd. This whole mechanism 
     * maybe be should be redesigned.
     *
     * @param begin time for new begin time slot
     * @param end time for new end time slot
     * @param forTier the tier to which an annotation is going to be created
     *
     * @return a Vector containing the
     */
    /*
    public Vector getTimeSlotsForNewAnnotation(long begin, long end,
        Tier forTier) {
        Vector slots = new Vector();

        TimeOrder timeOrder = ((TranscriptionImpl) (forTier.getParent())).getTimeOrder();

        TierImpl parentTier = (TierImpl) ((TierImpl) forTier).getParentTier();
        
        if (parentTier == null) {
            // erroneous situation
            return slots;
        }
        
        AlignableAnnotation parentAnn = (AlignableAnnotation) (parentTier.getAnnotationAtTime(begin,
                true));

        if (parentAnn == null) {
            return slots;
        }
        
        // HS june 2006 adjust begin and end time to fall inside the parent's boundary
        if (parentAnn.getBegin().isTimeAligned() && begin < parentAnn.getBegin().getTime()) {
            begin = parentAnn.getBegin().getTime();
        }
        if (parentAnn.getEnd().isTimeAligned() && end > parentAnn.getEnd().getTime()) {
            end = parentAnn.getEnd().getTime();
        }
        boolean insertAtParentBegin = false;

        if (parentAnn.getBegin().getTime() == begin) {
            insertAtParentBegin = true;
        }

        // get next existing TimeSlot on tier
        AlignableAnnotation currAnn = (AlignableAnnotation) ((TierImpl) forTier).getAnnotationAtTime(begin,
                true);

        TimeSlot bts = null;
        TimeSlot ets = null;

        if (currAnn == null) { 
            // no enclosed annotation for this parent yet
            // connect to extremes of parent annotation
            bts = parentAnn.getBegin();
            ets = parentAnn.getEnd();
        } else if (!insertAtParentBegin) {
	            bts = new TimeSlotImpl(begin, timeOrder);
	
	            //	timeOrder.insertTimeSlot(bts);
	            ets = currAnn.getEnd();
	
	            if (ets != parentAnn.getEnd()) {
	                ets.setTime(end);
	            }
	
	            timeOrder.insertTimeSlot(bts, currAnn.getBegin(), currAnn.getEnd());
	
	            // correct end of current annotation
	            //     currAnn.setEnd(bts);               
	            Vector endingAtTs = ((TranscriptionImpl) (forTier.getParent())).getAnnotsEndingAtTimeSlot(currAnn.getEnd(), forTier, true);
	
	            if (endingAtTs.contains(parentAnn)) {
	                endingAtTs.remove(parentAnn);
	            }
	
	            //HS jan 2005 only update annotations on the same tier or on depending tiers
	            Vector depTiers = ((TierImpl) forTier).getDependentTiers();
	
	            for (int i = 0; i < endingAtTs.size(); i++) {
	                AlignableAnnotation nextAA = (AlignableAnnotation) endingAtTs.elementAt(i);
	
	                if ((nextAA.getTier() == forTier) ||
	                        depTiers.contains(nextAA.getTier())) {
	                    nextAA.setEnd(bts);
	                }
	            }
        } else {
            // insert at parent begin, see javadoc comments
            bts = currAnn.getBegin();
            ets = currAnn.getEnd();

            if (end == parentAnn.getEnd().getTime()) {
                // removes any number of existing child annotations
                ets.setTime(end);
                currAnn.markDeleted(true);
            } else {
                AlignableAnnotation currEndAnn = null;
                currEndAnn = (AlignableAnnotation) (((TierImpl) forTier).getAnnotationAtTime(end,
                        true));

                TimeSlotImpl nextEndTs = new TimeSlotImpl(end, timeOrder);

                if ((ets == parentAnn.getEnd()) || (currAnn == currEndAnn)) {
                    // only one child annotation
                    // insert new                	
                    timeOrder.insertTimeSlot(nextEndTs, bts, ets);

                    Vector beginningAtTs = ((TranscriptionImpl) (forTier.getParent())).getAnnotsBeginningAtTimeSlot(bts, forTier, true);

                    if (beginningAtTs.contains(parentAnn)) {
                        beginningAtTs.remove(parentAnn);
                    }

                    // HS jan 2005 only update annotations on the same tier or on depending tiers
                    Vector depTiers = ((TierImpl) forTier).getDependentTiers();

                    for (int i = 0; i < beginningAtTs.size(); i++) {
                        AlignableAnnotation nextAA = (AlignableAnnotation) beginningAtTs.elementAt(i);

                        if ((nextAA.getTier() == forTier) ||
                                depTiers.contains(nextAA.getTier())) {
                            nextAA.setBegin(nextEndTs);
                        }
                    }

                    ets = nextEndTs;
                } else {
                    // more than one child annotations
                    ets.setTime(end);

                    Vector endingAtTs = ((TranscriptionImpl) (forTier.getParent())).getAnnotsEndingAtTimeSlot(ets, forTier, true);

                    if (endingAtTs.contains(parentAnn)) {
                        endingAtTs.remove(parentAnn);
                    }

                    // HS jan 2005 only update annotations on the same tier or on depending tiers
                    Vector depTiers = ((TierImpl) forTier).getDependentTiers();

                    for (int i = 0; i < endingAtTs.size(); i++) {
                        AlignableAnnotation nextAA = (AlignableAnnotation) endingAtTs.elementAt(i);

                        if ((nextAA.getTier() == forTier) ||
                                depTiers.contains(nextAA.getTier())) {
                            nextAA.setBegin(bts);
                        }
                    }
					currAnn.markDeleted(true);// fix for bug see next line 
                }

                //currAnn.markDeleted(true); //bug introduced in 2.5.1
            }
        }

        slots.add(bts);
        slots.add(ets);

        // System.out.println("end of getTimeSlotsForNewAnnotation, returning " + slots.size() + " slots");
        return slots;
    }
    */
    
    /**
     * July 2006. New implementation that tries to correctly handle as much situations and special cases on Time Subdivision 
     * tiers as possible. <br>
     * Some special cases are: <br>
     * - begin and/or end time are the same as that/those of an existing annotation and/or the parent annotation<br>
     * - begin and/or end time are the same as that/those of an annotation on a dependent tier<br>
     * - end time is out of the bounds of the parent annotation, end time will be adjusted, the annotation on the parent tier 
     * at begin time is always the parent annotation<br>
     * - begin and end time are within the boundaries of the parent annotation; the end time will be adjusted to be the
     * same as the end time of the parent annotation (otherwise two annotations would have to be created.
     * 
     * Annotations are no longer marked for deletion here. The whole operation depends on proper deletion of overlapping 
     * annotations in TierImpl.
     * @param begin the (requested) begin time for the new annotation
     * @param end the (requested) end time for the new annotation
     * @param forTier the tier the new annotation should be placed on
     * @return a List containing two time slots, either new ones or existing slots
     */
    @Override
	public List<TimeSlot> getTimeSlotsForNewAnnotation(long begin, long end,
        TierImpl forTier) {
    	List<TimeSlot> slots = new ArrayList<TimeSlot>();

        TierImpl parentTier = forTier.getParentTier();
            
        if (parentTier == null) {
             // erroneous situation
            return slots;
        }
            
        AlignableAnnotation parentAnn = (AlignableAnnotation) (parentTier.getAnnotationAtTime(begin,
                    true));

        if (parentAnn == null) {
            // can't create a new child annotation here
            return slots;
        }
            
        // HS june 2006 adjust begin and end time to fall inside the parent's boundary
        // maybe the test should be begin < parentAnn.getBeginTimeBoundary()
        if (parentAnn.getBegin().isTimeAligned() && begin < parentAnn.getBegin().getTime()) {
            begin = parentAnn.getBegin().getTime();
        }
        if (parentAnn.getEnd().isTimeAligned() && end > parentAnn.getEnd().getTime()) {
            end = parentAnn.getEnd().getTime();
        }
        
        boolean insertAtParentEnd = false;

        if (parentAnn.getEnd().getTime() == end) {
            insertAtParentEnd = true;
        }
        
        // get existing annotation on this tier at begin time
        AlignableAnnotation currAnn = (AlignableAnnotation) forTier.getAnnotationAtTime(begin,
                true);
        
        if (currAnn == null) {
            // no enclosed annotation for this parent yet
            // connect to extremes of parent annotation
            slots.add(parentAnn.getBegin());
            slots.add(parentAnn.getEnd());
            return slots;
        } else {
        	// HS June 2008: if there is already at least 1 child annotation, don't create a new annotation
        	// if the parent annotation's begin or end is unaligned
        	if (!parentAnn.getBegin().isTimeAligned() || !parentAnn.getEnd().isTimeAligned()) {
        		return slots;
        	}
        }
        boolean multiOverlappingAnns = false;
        // get existing annotation on this tier at end time
        AlignableAnnotation currEndAnn = null;
        if (insertAtParentEnd) {
            // getAnnotationAtTime compares annotation end time exclusive...
            currEndAnn = (AlignableAnnotation) forTier.getAnnotationAtTime(end - 1,
                    true);
        } else {
            currEndAnn = (AlignableAnnotation) forTier.getAnnotationAtTime(end,
                    true);
        }
        if (currEndAnn != null && currEndAnn != currAnn && end != currAnn.getEnd().getTime()) {
            multiOverlappingAnns = true;
        }
        if (!multiOverlappingAnns && begin != currAnn.getBegin().getTime()  && end < currAnn.getEnd().getTime()) {
            // special case: adjust end time to be the same as the end time of the current annotation
            // we do not split the current annotation; this would mean that more than one annotation would be created, implicitly
            // so the current annotation is adjusted and the new annotation is inserted with the old end time
            end = currAnn.getEnd().getTime();
        }
        
        TimeOrder timeOrder = (forTier.getTranscription()).getTimeOrder();
        List<TierImpl> childTiers = forTier.getChildTiers();
        TimeSlot bts = null;
        TimeSlot ets = null;
        boolean coincidingBegin = false;
        boolean coincidingEnd = false;
        
        if (begin == currAnn.getBegin().getTime()) {
            coincidingBegin = true;
        }
        if (currEndAnn != null && 
                (currEndAnn.getBegin().getTime() == end || currEndAnn.getEnd().getTime() == end)) {
            coincidingEnd = true; // coincides with some existing time slot
        }
        
        // different scenarios depending on the situation....
        // 1 begin and end coincide with the current annotation. either at parent begin or end or both
        // 2 begin coincides, end doesn't, a. one overlapping ann., b. multiple overlapping anns, either or not at parents end
        // 3 end coincides, begin doesn't, a. one overlapping ann., b. multiple overlapping anns, either or not at parents end
        // 4 begin and end in new positions, always multiple overlapping annotations
        // with 2, 3 and 4 begin or end might coincide with a timeslot on a depending tier
        bts = currAnn.getBegin();
        ets = currAnn.getEnd();
        // case 1
        if (coincidingBegin && end == currAnn.getEnd().getTime()) {
            //currAnn.setEnd(bts);
        } else if (coincidingBegin) {
            // case 2, begin is the same as an existing ann. end is within that ann's boundaries
            if (!multiOverlappingAnns) {
                ets = new TimeSlotImpl(end, timeOrder);
                timeOrder.insertTimeSlot(ets, bts, currAnn.getEnd());
                //currAnn.setBegin(ets); later...

                //  propagate changes to depending tiers, the begin
                TierImpl nextT;
                for (int i = 0; i < childTiers.size(); i++) {
                    nextT = childTiers.get(i);
                    
                    if (nextT.isTimeAlignable()) { 
                        // hier... testen op stereotype en Included In apart behandelen, of hier alleen Time Sub 
                        // en Included In overlaten aan TierImpl correctOverlaps etc.
                        setAlignableBeginSlot(nextT, currAnn, ets, end); 
                    }
                }
                currAnn.setBegin(ets);
            } else {
                // multiple overlapping annotations, end is in the boundaries of one of the following anno's
                if (!insertAtParentEnd) {
                    TimeSlot oldBeginTS;
                    TierImpl nextT;
                    
                    ets = new TimeSlotImpl(end, timeOrder);
                    oldBeginTS = currEndAnn.getBegin();
                    
                    if (!oldBeginTS.isTimeAligned()) {
                        AlignableAnnotation naa = getFirstAlignedAnnotation(forTier, currEndAnn);
                        if (naa != null) {
                            currEndAnn = naa;
                            oldBeginTS = currEndAnn.getBegin();
                        }
                    }
                    
                    timeOrder.insertTimeSlot(ets, currEndAnn.getBegin(), currEndAnn.getEnd());
                    
	                // propagate changes to depending tiers, the end
	                for (int i = 0; i < childTiers.size(); i++) {
	                    nextT = childTiers.get(i);
	                    
	                    if (nextT.isTimeAlignable()) {
	                        setAlignableBeginSlot(nextT, currEndAnn, ets, end); 
	                    }
	                }
	                currEndAnn.setBegin(ets);

                } else {
                    // end is the same as the end of one of the following annotations
                    ets = parentAnn.getEnd();
                }
            }
        } else {          
            // case 3 and 4: begin is not the same as an exisitng annotation,create a new timeSlot
            AlignableAnnotation nextAA;
            TimeSlot oldEndTS;
            TierImpl nextT;
            
            bts = new TimeSlotImpl(begin, timeOrder);
            oldEndTS = currAnn.getEnd();
            timeOrder.insertTimeSlot(bts, currAnn.getBegin(), ets);
            //currAnn.setEnd(bts);
            
            // reconnect next annotation
            
            if (multiOverlappingAnns) {
	            List<Annotation> nx  = forTier.getAnnotsBeginningAtTimeSlot(oldEndTS);
	            for (int j = 0; j < nx.size(); j++) {
	                nextAA = (AlignableAnnotation) nx.get(j);

	                if (parentAnn.getParentListeners().contains(nextAA)) {
		                for (int i = 0; i < childTiers.size(); i++) {
		                    nextT = childTiers.get(i);
		                    
		                    if (nextT.isTimeAlignable()) { 
		                        //setAlignableBeginSlot(nextT, nextAA, bts, end);//hier begin?? of alle annots met oldEndTS als begin
		                        //setAlignableBeginSlot(nextT, nextAA, bts, begin);//hier begin?? timealignable?
		                        // nieuwe methode setBeginSlot(nextT, nextAA, bts, oldEndTS)??
		                    }
		                }
	                    nextAA.setBegin(bts);
	                }
	            }
            }
            
            // propagate changes to depending tiers
            for (int i = 0; i < childTiers.size(); i++) {
                nextT = childTiers.get(i);
                
                if (nextT.isTimeAlignable()) {
                    setAlignableEndSlot(nextT, currAnn, bts, ets, begin, end);
                }
            }
            currAnn.setEnd(bts);
            // if !multiOverlappingAnns end has been set to be the same as currAnn's end time
            if (multiOverlappingAnns) {
                // end is in the boundaries of a following annotation
                if (coincidingEnd) {
                    // case 3: coinciding begin time of currEndAnn or end time of currEndAnn in case it is the parents end
                    if (insertAtParentEnd) {
                        ets = parentAnn.getEnd();
                    } else {
                        ets = currEndAnn.getBegin();
                    }
                } else {
                    //case 4:  create a new end time slot too
                    TimeSlot oldBeginTS;
                    
                    ets = new TimeSlotImpl(end, timeOrder);
                    oldBeginTS = currEndAnn.getBegin();
                    
                    if (!oldBeginTS.isTimeAligned()) {
                        AlignableAnnotation naa = getFirstAlignedAnnotation(forTier, currEndAnn);
                        if (naa != null) {
                            currEndAnn = naa;
                            oldBeginTS = currEndAnn.getBegin();
                        }
                    }
                    
                    timeOrder.insertTimeSlot(ets, currEndAnn.getBegin(), currEndAnn.getEnd());
                    //currEndAnn.setBegin(ets); later...
                    
	                // propagate changes to depending tiers, the end
	                for (int i = 0; i < childTiers.size(); i++) {
	                    nextT = childTiers.get(i);
	                    
	                    if (nextT.isTimeAlignable()) {
	                        setAlignableBeginSlot(nextT, currEndAnn, ets, end); 
	                    }
	                }
	                currEndAnn.setBegin(ets);
                }
            }
        }
        
        slots.add(bts);
        slots.add(ets);
        return slots;
    }
    
    
    /**
     * DOCUMENT ME!
     *
     * @param theTier DOCUMENT ME!
     */
    @Override
	public void enforceOnWholeTier(TierImpl theTier) {
        // close all gaps within parents by reconnecting time slots
        // iterate over parent's annotations.
        // for each parent, find enclosed annotations
        // iterate over enclosed annotations
        // for each, connect begin timeslot to either parent's begin or previous annots end
        // connect end of last to parent's end
        Tier parentTier = theTier.getParentTier();

        if (parentTier != null) {
            for (AlignableAnnotation parent : ((TierImpl) parentTier).getAlignableAnnotations()) {

                //Vector enclosedAnnots = ((TierImpl) theTier).getOverlappingAnnotations(
                //	parent.getBegin().getTime(), parent.getEnd().getTime(), true);
                List<AlignableAnnotation> enclosedAnnots = 
                		theTier.getOverlappingAnnotations(parent.getBegin(),
                        parent.getEnd());

                Iterator annIter = enclosedAnnots.iterator();
                AlignableAnnotation previousAnn = null;

                while (annIter.hasNext()) {
                    AlignableAnnotation a = (AlignableAnnotation) annIter.next();

                    if (previousAnn == null) { // connect to parent's begin
                        a.setBegin(parent.getBegin());
                    } else { // connect end of previous to ann begin
                        previousAnn.setEnd(a.getBegin());
                    }

                    previousAnn = a;
                }

                // connect last ann to end of parent
                if (previousAnn != null) {
                    previousAnn.setEnd(parent.getEnd());
                }
            }

            TimeOrder timeOrder = (theTier.getTranscription()).getTimeOrder();

            timeOrder.pruneTimeSlots();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean supportsInsertion() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param beforeAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Annotation insertBefore(Annotation beforeAnn, TierImpl theTier) {
        AlignableAnnotation a = null;
        AlignableAnnotation beforeA = (AlignableAnnotation) beforeAnn;

        TimeOrder timeOrder = (theTier.getTranscription()).getTimeOrder();

        TimeSlot newTs = new TimeSlotImpl(timeOrder);
        timeOrder.insertTimeSlot(newTs, beforeA.getBegin(), beforeA.getEnd());

        a = new AlignableAnnotation(beforeA.getBegin(), newTs, theTier);

        //    beforeA.setBegin(newTs);
        Annotation parentAnn = beforeAnn.getParentAnnotation();

        List<Annotation> beginningAtTs = (theTier.getTranscription()).getAnnotsBeginningAtTimeSlot(beforeA.getBegin(), theTier, true);

        if (beginningAtTs.contains(parentAnn)) {
            beginningAtTs.remove(parentAnn);
        }

        // HS jan 2005 only update annotations on the same tier or on depending tiers
        List<TierImpl> depTiers = theTier.getDependentTiers();

        for (int i = 0; i < beginningAtTs.size(); i++) {
            AlignableAnnotation other = (AlignableAnnotation) beginningAtTs.get(i);

            if ((other.getTier() == theTier) ||
                    depTiers.contains(other.getTier())) {
                other.setBegin(newTs);
            }
        }

        theTier.addAnnotation(a);

        return a;
    }

    /**
     * DOCUMENT ME!
     *
     * @param afterAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Annotation insertAfter(Annotation afterAnn, TierImpl theTier) {
        AlignableAnnotation a = null;
        AlignableAnnotation afterA = (AlignableAnnotation) afterAnn;

        TimeOrder timeOrder = (theTier.getTranscription()).getTimeOrder();

        TimeSlot newTs = new TimeSlotImpl(timeOrder);
        // timeOrder.insertTimeSlot(newTs, afterA.getBegin(), afterA.getEnd());
        
        // HS July 2006: insert after the highest aligned time slot on any of the dependent chikdren
        TimeSlot highestATS = null;
        Annotation loopA;
        TimeSlot ts;
        List<Annotation> pls = afterA.getParentListeners();
        for (int i = 0; i < pls.size(); i++) {
            loopA = pls.get(i);
            if (loopA instanceof AlignableAnnotation) {
                ts = ((AlignableAnnotation) loopA).getBegin();
                if (ts.isTimeAligned()) {
                    if (highestATS == null || ts.getTime() > highestATS.getTime()) {
                        highestATS = ts;
                    }
                }
            }
        }
        if (highestATS == null) {
            timeOrder.insertTimeSlot(newTs, afterA.getBegin(), afterA.getEnd());
        } else {
            timeOrder.insertTimeSlot(newTs, highestATS, afterA.getEnd());
        }
        
        a = new AlignableAnnotation(newTs, afterA.getEnd(), theTier);

        //      afterA.setEnd(newTs);
        Annotation parentAnn = afterAnn.getParentAnnotation();

        List<Annotation> endingAtTs = (theTier.getTranscription()).getAnnotsEndingAtTimeSlot(afterA.getEnd(), theTier, true);

        if (endingAtTs.contains(parentAnn)) {
            endingAtTs.remove(parentAnn);
        }

        // HS jan 2005 only update annotations on the same tier or on depending tiers
        List<TierImpl> depTiers = theTier.getDependentTiers();

        for (int i = 0; i < endingAtTs.size(); i++) {
            AlignableAnnotation other = (AlignableAnnotation) endingAtTs.get(i);

            if ((other.getTier() == theTier) ||
                    depTiers.contains(other.getTier())) {
                other.setEnd(newTs);
            }
        }

        theTier.addAnnotation(a);
        a.registerWithParent(); //HB, 18-5-04, only works after addAnnotation, newTS not used on any tier during construction

        return a;
    }

    /**
     * Detach annotation theAnn from tier theTier by reconnecting remaining
     * Annotations on the tier. Assumes that all references and
     * ParentAnnotationListener registrations are already cleaned up.
     *
     * @param theAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     */
    @Override
	public void detachAnnotation(Annotation theAnn, TierImpl theTier) {
        // find all dependents on this tier for theAnn's parent.
        // within this set, find theAnn's previous and next annotion, if they exist.
        // finally, reconnect
        AlignableAnnotation a = (AlignableAnnotation) theAnn; // cast is safe for case of NoTimeGapWithinParent
        Annotation parent = a.getParentAnnotation();

        //System.out.println("\ndetach: " + a.getValue());
        List<Annotation> enclosedAnnots = new ArrayList<Annotation>();
        List<Annotation> childAnnots = new ArrayList<Annotation>();

        if (parent != null) {
            //	enclosedAnnots = parent.getChildrenOnTier(theTier);
            childAnnots = (theTier.getTranscription()).getChildAnnotationsOf(parent);
        }

        Iterator childIter = childAnnots.iterator();

        while (childIter.hasNext()) {
            Annotation ann = (Annotation) childIter.next();

            if (ann.getTier() == theTier) {
                enclosedAnnots.add(ann);
            }
        }

        if (enclosedAnnots.size() > 0) {
            AlignableAnnotation prev = null;
            AlignableAnnotation next = null;

            int index = enclosedAnnots.indexOf(a);

            if (index > 0) {
                prev = (AlignableAnnotation) (enclosedAnnots.get(index - 1));
            }
            // july 2006: added a test (index > -1); if "a" is not in the vector
            // it is incorrect to just take the first child annotation as "next"            
            if (index > -1 && index < (enclosedAnnots.size() - 1)) {
                next = (AlignableAnnotation) (enclosedAnnots.get(index + 1));
            }
            
            // reconnect
            if (prev != null) {
                //    prev.setEnd(a.getEnd());
				if (prev.isMarkedDeleted()) {
					// don't bother to reconnect an annotation that has been marked deleted
					return; 
				}

                List<Annotation> endingAtTs = (theTier.getTranscription()).getAnnotsEndingAtTimeSlot(prev.getEnd(), theTier, true);

                for (int i = 0; i < endingAtTs.size(); i++) {
                    ((AlignableAnnotation) endingAtTs.get(i)).setEnd(a.getEnd());
                }
            } else if (next != null) {
                //    next.setBegin(a.getBegin());
                if (next.isMarkedDeleted()) {
					//don't bother to reconnect an annotation that has been marked deleted??
                    // sometimes this is needed? revise..
                	//return;
                }

                List<Annotation> beginningAtTs = (theTier.getTranscription()).getAnnotsBeginningAtTimeSlot(next.getBegin(), theTier, true);

                for (int i = 0; i < beginningAtTs.size(); i++) {
                    ((AlignableAnnotation) beginningAtTs.get(i)).setBegin(a.getBegin());
                }
            }
        }
    }
    
    /**
     * Recursively sets the begin time slot of the proper annotation on the specified tier; if the annotation is not time aligned,
     * it finds the first preceding annotation on that tier with a time aligned begin slot and changes that slot, if it is 
     * a chile of the specified parent annotation.
     * @param nextT the tier
     * @param parentAA the parent annotation on the parent tier
     * @param ets the new begin slot
     * @param the time to start searching for an annotation 
     */
    private void setAlignableBeginSlot(TierImpl nextT, AlignableAnnotation parentAA, TimeSlot ets, long end) {
        if (parentAA == null) {
            return;
        }
        if (nextT.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN) {
            return;// test
        }
        AlignableAnnotation nextAA = (AlignableAnnotation) nextT.getAnnotationAtTime(end);
        List<TierImpl> childT = nextT.getChildTiers();
        TierImpl ch;
        if (nextAA != null) {
            if (nextAA.getBegin().isTimeAligned()) {
                if (parentAA.getParentListeners().contains(nextAA)) {               
	                for (int i = 0; i < childT.size(); i++) {
	                    ch = childT.get(i);
	                    if (ch.isTimeAlignable()) {
	                        setAlignableBeginSlot(ch, nextAA, ets, end);
	                    }
	                }
	                
	                nextAA.setBegin(ets);
                }
            } else {
                // find the first preceding aligned begin time slot/annotation, but don't pass the parent's begin slot
                boolean found = false;
                List<Annotation> annos;
                while (!found) {
                    annos = nextT.getAnnotsEndingAtTimeSlot(nextAA.getBegin());
                    for (int j = 0; j < annos.size(); j++) {
                        nextAA = (AlignableAnnotation) annos.get(j);
                        if ((nextAA.getBegin().isTimeAligned() || parentAA.getBegin() == nextAA.getBegin()) && 
                                parentAA.getParentListeners().contains(nextAA)) {
                            for (int i = 0; i < childT.size(); i++) {
                                ch = childT.get(i);
                                if (ch.isTimeAlignable()) {
                                    setAlignableBeginSlot(ch, nextAA, ets, end);
                                }
                            } 
                            nextAA.setBegin(ets);
                            found = true;
                        }
                    }
                    if (annos.size() == 0) {
                        break; // error
                    }
                }
            }
        } else {
            // find a child of parent
            List<Annotation> ba = nextT.getAnnotsBeginningAtTimeSlot(parentAA.getBegin());
            for (int j = 0; j < ba.size();  j++) {
                nextAA = (AlignableAnnotation) ba.get(j);
                for (int i = 0; i < childT.size(); i++) {
                    ch = childT.get(i);
                    if (ch.isTimeAlignable()) {
                        setAlignableBeginSlot(ch, nextAA, ets, end);
                    }
                }
                nextAA.setBegin(ets);
            }
        }
    }
    
    /**
     * Recursively sets the end time slot of the specified annotation if it is time aligned and reconnects the next annotation 
     * to that new slot (if it has the same parent). If it is not time aligned it finds the first following annotation 
     * that has a time aligned end slot.
     * @param nextT the tier
     * @param parentAA the parent annotation on the parent tier
     * @param bts the (new) begin time slot
     * @param ets the (new) end time slot
     * @param begin the begin time for a new annotation
     * @param end the end time for a new annotation
     */
    private void setAlignableEndSlot(TierImpl nextT, AlignableAnnotation parentAA, TimeSlot bts,
            TimeSlot ets, long begin, long end) {
        if (parentAA == null) {
            return;
        }
        if (nextT.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN) {
            return;// test
        }
        AlignableAnnotation nextAA = (AlignableAnnotation) nextT.getAnnotationAtTime(begin);
        List<TierImpl> childT = nextT.getChildTiers();
        TierImpl ch;
        
        if (nextAA != null) {
            TimeSlot oldEndTS;
            if (nextAA.getBegin().getTime() != begin) {// true for any unaligned begin slot
                oldEndTS = nextAA.getEnd();
                if (nextAA.getEnd().isTimeAligned() || nextAA.getEnd() == parentAA.getEnd()) {
                    for (int i = 0; i < childT.size(); i++) {
                        ch = childT.get(i);
                        if (ch.isTimeAlignable()) {
                            setAlignableEndSlot(ch, nextAA, bts, ets, begin, end);
                        }
                    }
                    nextAA.setEnd(bts);
                    // reconnect the following annotation
                    if (oldEndTS != ets || oldEndTS.getTime() < end) { // && ??
                        List<Annotation> vv = nextT.getAnnotsBeginningAtTimeSlot(oldEndTS);
                        for (int j = 0; j < vv.size(); j++) {
                            nextAA = (AlignableAnnotation) vv.get(j);
                           // if (parentAA.getParentListeners().contains(nextAA)) {
                                nextAA.setBegin(bts);    
                            //}
                        }
                    }
                } else {
                    // find first following aligned annotation/slot
                    boolean found = false;
                    List<Annotation> annos;
                    while (!found) {
                        annos = nextT.getAnnotsBeginningAtTimeSlot(nextAA.getEnd());
                        for (int j = 0; j < annos.size(); j++) {
                            nextAA = (AlignableAnnotation) annos.get(j);
                            oldEndTS = nextAA.getEnd();
                            if (nextAA.getEnd().isTimeAligned()) {
                                for (int i = 0; i < childT.size(); i++) {
                                    ch = childT.get(i);
                                    if (ch.isTimeAlignable()) {
                                        setAlignableEndSlot(ch, nextAA, bts, ets, begin, end);
                                    }
                                }
                                nextAA.setEnd(bts);
                                found = true;
                                
                                //reconnect the following annotation
                                if (oldEndTS != ets || oldEndTS.getTime() < end) {// && ??
                                    List<Annotation> vv = nextT.getAnnotsBeginningAtTimeSlot(oldEndTS);
                                    for (int k = 0; k < vv.size(); k++) {
                                        nextAA = (AlignableAnnotation) vv.get(k);
                                        //if (parentAA.getParentListeners().contains(nextAA)) {
                                            nextAA.setBegin(bts);    
                                       // }
                                    }
                                }
                            }
                        }
                        if (annos.size() == 0) {// end of (sub) chain
                            break; // error
                        }
                    }
                }

            } else {
                // if the begin time happens to coincide with an existing dep. begin timeslot, replace begin
                TimeSlot oldBeginTS = nextAA.getBegin();
                for (int i = 0; i < childT.size(); i++) {
                    ch = childT.get(i);
                    if (ch.isTimeAlignable()) {
                        setAlignableEndSlot(ch, nextAA, bts, ets, begin, end);
                    }
                }
                nextAA.setBegin(bts);
                // reconnect the previous annotation..
                List<Annotation> vv = nextT.getAnnotsEndingAtTimeSlot(oldBeginTS);
                for (int k = 0; k < vv.size(); k++) {
                    nextAA = (AlignableAnnotation) vv.get(k);
                    nextAA.setEnd(bts);
                }
            }                            
        }
        /* else {
            // find annotation beginning at parent's end, reconnect

            Vector ba = nextT.getAnnotsBeginningAtTimeSlot(parentAA.getEnd());
            for (int j = 0; j < ba.size();  j++) {
                nextAA = (AlignableAnnotation) ba.get(j);
                for (int i = 0; i < childT.size(); i++) {
                    ch = (TierImpl) childT.get(i);
                    if (ch.isTimeAlignable()) {
                        setAlignableEndSlot(ch, nextAA, bts, ets, begin, end);
                    }
                }
                nextAA.setBegin(bts);
            }
        }
        */
    }
    
    /**
     * Finds the next annotation with an alignable end slot, under the same parent annotation.
     * @param tier the tier the annotations are on
     * @param fromAnn the starting point for the search to the right
     * @return the first aligned annotation
     */
    /*
    private AlignableAnnotation getNextAlignedAnnotation(TierImpl tier, AlignableAnnotation fromAnn) {
        if (tier == null || fromAnn == null) {
            return null;
        }
        AlignableAnnotation resAnn = null;
        AlignableAnnotation loopAnn = fromAnn;
        Vector anns = null;
        outerloop:
        while (true) {
            anns = tier.getAnnotsBeginningAtTimeSlot(loopAnn.getEnd());
            for (int i = 0; i < anns.size(); i++) {
                loopAnn = (AlignableAnnotation) anns.get(i);
                if (loopAnn.getParentAnnotation() != fromAnn.getParentAnnotation()) {
                    break outerloop;
                }
                if (loopAnn.getEnd().isTimeAligned()) {
                    resAnn = loopAnn;
                    break outerloop;
                }
            }
            resAnn = loopAnn;// save last ref in this vector
            if (anns.size() == 0) {
                break;
            }
        }
        return resAnn;
    }
    */
    
    /**
     * Finds the first annotation in the chain with an alignable end slot, under the same parent annotation.
     * @param tier the tier the annotations are on
     * @param fromAnn the starting point for the search to the left
     * @return the first aligned annotation
     */
    private AlignableAnnotation getFirstAlignedAnnotation(TierImpl tier, AlignableAnnotation fromAnn) {
        if (tier == null || fromAnn == null) {
            return null;
        }
        AlignableAnnotation resAnn = null;
        AlignableAnnotation loopAnn = fromAnn;
        List<Annotation> anns = null;
        outerloop:
        while (true) {
            anns = tier.getAnnotsEndingAtTimeSlot(loopAnn.getBegin());
            for (int i = 0; i < anns.size(); i++) {
                loopAnn = (AlignableAnnotation) anns.get(i);
                if (loopAnn.getParentAnnotation() != fromAnn.getParentAnnotation()) {
                    break outerloop;
                }
                if (loopAnn.getBegin().isTimeAligned()) {
                    resAnn = loopAnn;
                    break outerloop;
                }
            }
            resAnn = loopAnn;// save last ref in this vector
            if (anns.size() == 0) {
                break;
            }
        }
        return resAnn;
    }
}
