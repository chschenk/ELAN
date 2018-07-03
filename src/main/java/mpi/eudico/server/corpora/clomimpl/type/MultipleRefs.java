package mpi.eudico.server.corpora.clomimpl.type;

import java.util.Iterator;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;


/**
 * DOCUMENT ME!
 * $Id: MultipleRefs.java 43798 2015-05-07 11:39:46Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class MultipleRefs extends ConstraintImpl {
    /**
     * Creates a new MultipleRefs instance
     */
    public MultipleRefs() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public int getStereoType() {
        return Constraint.MULTIPLE_REFS;
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
        long beginTimeBoundary = Long.MAX_VALUE;
        long beginB = 0;

        Iterator<Annotation> refIter = theAnnot.getReferences().iterator();

        while (refIter.hasNext()) {
            beginB = refIter.next().getBeginTimeBoundary();

            beginTimeBoundary = Math.min(beginTimeBoundary, beginB);
        }

        return beginTimeBoundary;
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
        long endTimeBoundary = 0;
        long endB = 0;

        Iterator<Annotation> refIter = theAnnot.getReferences().iterator();

        while (refIter.hasNext()) {
            endB = refIter.next().getEndTimeBoundary();

            endTimeBoundary = Math.max(endTimeBoundary, endB);
        }

        return endTimeBoundary;
    }
}
