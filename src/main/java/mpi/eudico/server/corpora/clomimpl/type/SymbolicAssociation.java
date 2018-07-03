package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * DOCUMENT ME!
 * $Id: SymbolicAssociation.java 43571 2015-03-23 15:28:01Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class SymbolicAssociation extends ConstraintImpl {
    /**
     * Creates a new SymbolicAssociation instance
     */
    public SymbolicAssociation() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public int getStereoType() {
        return Constraint.SYMBOLIC_ASSOCIATION;
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
        //	System.out.println("begin for ref annot: " + theAnnot.getValue() + " on tier: " + theAnnot.getTier().getName());

        long beginTB = 0;

        if (theAnnot.getReferences().size() > 0) {
            Annotation ref = theAnnot.getReferences().get(0);
            beginTB = ref.getBeginTimeBoundary();
        }

        return beginTB;
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
        long endTB = 0;

        if (theAnnot.getReferences().size() > 0) {
            Annotation ref = theAnnot.getReferences().get(0);
            endTB = ref.getEndTimeBoundary();
        }

        return endTB;
    }
}
