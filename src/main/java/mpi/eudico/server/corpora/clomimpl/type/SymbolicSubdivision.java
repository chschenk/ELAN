package mpi.eudico.server.corpora.clomimpl.type;

import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * DOCUMENT ME!
 * $Id: SymbolicSubdivision.java 43571 2015-03-23 15:28:01Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class SymbolicSubdivision extends ConstraintImpl {
    /**
     * Creates a new SymbolicSubdivision instance
     */
    public SymbolicSubdivision() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public int getStereoType() {
        return Constraint.SYMBOLIC_SUBDIVISION;
    }

    /**
     * DOCUMENT ME!
     *
     * @param segment DOCUMENT ME!
     * @param forTier DOCUMENT ME!
     */
    @Override
	public void forceTimes(long[] segment, TierImpl forTier) {
        //		if (forTier != null) {
        //			segment[1] = segment[0];
        //		}
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot) {
        long[] segment = { 0, 0 };
        int[] elmtsLeftAndRight = { 0, 0 };

        getSegmentForChainOf(theAnnot, segment, elmtsLeftAndRight);

        long duration = segment[1] - segment[0];
        double durationPerAnnot = (double) duration / (double) (elmtsLeftAndRight[0] +
            elmtsLeftAndRight[1] + 1);

        return (segment[0] + (long) (elmtsLeftAndRight[0] * durationPerAnnot));
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getEndTimeForRefAnnotation(RefAnnotation theAnnot) {
        long[] segment = { 0, 0 };
        int[] elmtsLeftAndRight = { 0, 0 };

        getSegmentForChainOf(theAnnot, segment, elmtsLeftAndRight);

        long duration = segment[1] - segment[0];
        double durationPerAnnot = (double) duration / (double) (elmtsLeftAndRight[0] +
            elmtsLeftAndRight[1] + 1);

        return (segment[0] +
        (long) ((elmtsLeftAndRight[0] + 1) * durationPerAnnot));
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
        Annotation parentAnn = ((RefAnnotation) beforeAnn).getReferences()
                                             .get(0);
        RefAnnotation newAnn = new RefAnnotation(parentAnn, theTier);

        if (((RefAnnotation) beforeAnn).hasPrevious()) {
            RefAnnotation prevAnn = ((RefAnnotation) beforeAnn).getPrevious();

            prevAnn.setNext(newAnn);
        }

        newAnn.setNext((RefAnnotation) beforeAnn);

        theTier.addAnnotation(newAnn);

        return newAnn;
    }

    /**
     * <p>
     * MK:02/06/24<br> Method inserts a new RefAnnotation after a given RefAnnotation.<br>
     * Note that you can create the new Annotation on a different tier than
     * the given Annotation
     * </p>
     *
     * @param afterAnn WRONG TYPE: must be RefAnnotation. Tier member ignored.
     *        Not nullable.
     * @param theTier tier of the newly created RefAnnotation, not nullable.
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Annotation insertAfter(Annotation afterAnn, TierImpl theTier) {
        final RefAnnotation afterRefAnn = (RefAnnotation) afterAnn;
		Annotation parentAnn = afterRefAnn.getReferences().get(0);
        RefAnnotation newAnn = new RefAnnotation(parentAnn, theTier);

        //MK:02/06/24 insert into "next" chain 
        if (afterRefAnn.hasNext()) {
            RefAnnotation nextAnn = afterRefAnn.getNext();

            newAnn.setNext(nextAnn);
        }

        afterRefAnn.setNext(newAnn);

        theTier.addAnnotation(newAnn);

        return newAnn;
    }

    private void getSegmentForChainOf(RefAnnotation theAnnot, long[] segment,
        int[] elmtsLeftAndRight) {
        RefAnnotation firstOfChain = getFirstOfChain(theAnnot, elmtsLeftAndRight);
        RefAnnotation lastOfChain = getLastOfChain(theAnnot, elmtsLeftAndRight);

        List<Annotation> refsOfFirst = firstOfChain.getReferences();

        if (refsOfFirst.size() > 0) {
            Annotation beginRef = refsOfFirst.get(0);
            segment[0] = beginRef.getBeginTimeBoundary();
        }

        List<Annotation> refsOfLast = lastOfChain.getReferences();

        if (refsOfLast.size() > 0) {
            Annotation endRef = lastOfChain.getReferences().get(0);
            segment[1] = endRef.getEndTimeBoundary();
        }
    }

    private RefAnnotation getFirstOfChain(RefAnnotation theAnnot,
        int[] elmtsLeftAndRight) {
        RefAnnotation first = theAnnot;

        int leftElementCount = 0;

        while (first.hasPrevious()) {
            first = first.getPrevious();
            leftElementCount++;
        }

        elmtsLeftAndRight[0] = leftElementCount;

        return first;
    }

    private RefAnnotation getLastOfChain(RefAnnotation theAnnot,
        int[] elmtsLeftAndRight) {
        RefAnnotation last = theAnnot;

        int rightElementCount = 0;

        while (last.hasNext()) {
            last = last.getNext();
            rightElementCount++;
        }

        elmtsLeftAndRight[1] = rightElementCount;

        return last;
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
        RefAnnotation a = (RefAnnotation) theAnn; // cast is safe for case of SymbolicSubdivision

        RefAnnotation prev = a.getPrevious();
        RefAnnotation next = a.getNext();

        // reconnect
        if (prev != null) {
            prev.setNext(next);
        } else if (next != null) {
            next.setPrevious(null);
        }
    }
}
