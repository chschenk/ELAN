package mpi.eudico.server.corpora.clomimpl.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;


public class IncludedIn extends ConstraintImpl {

    /**
     * A time subdivision constraint that allows gaps, i.e. empty space before, after and/or 
     * between annotations on a depending tier, within the interval of an annotation on the 
     * parent tier are allowed.
     */
    public IncludedIn() {
        super();
    }

    /**
     * Force begin and end of an annotation {segment}
     * Copied from TimeSubdivision.
     */
    @Override
    public void forceTimes(long[] segment, TierImpl forTier) {
        if (forTier != null) {
            Annotation annAtBegin = forTier.getAnnotationAtTime(segment[0]);
            Annotation annAtEnd = forTier.getAnnotationAtTime(segment[1]);

            if ((annAtBegin != null) && (annAtEnd == null)) {
                segment[1] = annAtBegin.getEndTimeBoundary();
            } else if ((annAtBegin == null) && (annAtEnd != null)) {
                segment[0] = annAtEnd.getBeginTimeBoundary();
            } else if ((annAtBegin != null) && (annAtEnd != null) &&
                    (annAtBegin != annAtEnd)) {
                segment[0] = annAtEnd.getBeginTimeBoundary();
            } else if ((annAtBegin == null) && (annAtEnd == null)) {
                // if annotations in between, constrain to first of them
                List<Annotation> annotsInBetween = forTier.
                		getOverlappingAnnotations(segment[0], segment[1]);

                if (annotsInBetween.size() > 0) {
                    AlignableAnnotation a = (AlignableAnnotation) annotsInBetween.get(0);
                    segment[0] = a.getBegin().getTime();
                    segment[1] = a.getEnd().getTime();
                } else {
                    segment[0] = segment[1];
                }
            }

        }
    }
    
    /**
     * @see mpi.eudico.server.corpora.clomimpl.type.Constraint#getStereoType()
     */
    @Override
	public int getStereoType() {
        return Constraint.INCLUDED_IN;
    }

    /**
     * Don't allow unaligned annotations on this kind of tiers.
     *
     * @return true if annotations can be added with insertBefore and insertAfter, 
     * false otherwise.
     */
    @Override
	public boolean supportsInsertion() {
        return false;
    }
    
    @Override
    public void detachAnnotation(Annotation theAnn, TierImpl theTier) {
        // do nothing??
    }
    
    @Override
    public void enforceOnWholeTier(TierImpl theTier) {
        // empty ??
        System.out.println("IncludedIn: enforce...");
    }
    
    /**
     * Create 2 new time slots or find existing slots for the new annotation.
     * It is assumed that end and begin have been checked: end > begin.
     * It is also assumed that the tier has a parent tier and that the parent 
     * is time alignable
     */
    @Override
    public List<TimeSlot> getTimeSlotsForNewAnnotation(long begin, long end,
            TierImpl forTier) {
        List<TimeSlot> slots = new ArrayList<TimeSlot>(2);
        
        TierImpl parentTier = forTier.getParentTier();
        AlignableAnnotation parentAnn = null;
        List<Annotation> overlappingParentAnns = parentTier.getOverlappingAnnotations(begin, end);
        if (overlappingParentAnns.isEmpty()) {
            return slots;
        }
        // pick the first of the overlapping annotations on the parent
        parentAnn = (AlignableAnnotation) overlappingParentAnns.get(0);
        if (!parentAnn.getBegin().isTimeAligned() || !parentAnn.getEnd().isTimeAligned()) {
            // don't create an annotation if the parent is a time subdivision tier and one 
            // of the slots is unaligned
            return slots;
        }
        if (parentAnn.getBegin().getTime() > begin) {
            begin = parentAnn.getBegin().getTime();
        }
        if (parentAnn.getEnd().getTime() < end) {
            end = parentAnn.getEnd().getTime();
        }
        // we have a candidate parent annotation and a begin and end time within the boundaries
        // get existing annotations on tier
        List<Annotation> overlappingAnnots = forTier.getOverlappingAnnotations(begin, end);
        
        TimeOrder timeOrder = forTier.getTranscription().getTimeOrder();
        TimeSlot bts = new TimeSlotImpl(begin, timeOrder);
        TimeSlot ets = new TimeSlotImpl(end, timeOrder);
        timeOrder.insertTimeSlot(bts);// insertTimeSlot(bts, parentAnn.getBegin(), parentAnn.getEnd())
        timeOrder.insertTimeSlot(ets);
        slots.add(bts);
        slots.add(ets);
        
        Iterator<Annotation> anIt = overlappingAnnots.iterator();
        AlignableAnnotation curAnn = null;
        
        while (anIt.hasNext()) {
            curAnn = (AlignableAnnotation) anIt.next();
            if (curAnn.getBegin().getTime() >= begin) {
                if (curAnn.getEnd().getTime() > end) {
                    curAnn.getBegin().setTime(end);
                } else {
                    curAnn.setBegin(curAnn.getEnd());// will be marked for deletion
                }
            } else {//curann.begin < begin
                if (curAnn.getEnd().getTime() > begin) {
                    curAnn.getEnd().setTime(begin);
                }
            }
        }
        
        return slots;
    }

}
