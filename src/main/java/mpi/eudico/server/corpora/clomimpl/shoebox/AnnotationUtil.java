package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;

/**
 * DOCUMENT ME! $Id: AnnotationUtil.java 43500 2015-03-05 15:35:15Z olasei $
 *
 * @author $Author$
 * @version $Revision$
 */
public class AnnotationUtil {
//    private static Logger logger = Logger.getLogger(AnnotationUtil.class.toString());

    /**
     * MK:02/06/28<br> An Annotation has children, possibly on many tiers.
     *
     * @param tthis DOCUMENT ME!
     * @param tier restrict direct childs to this tier
     *
     * @return all direct child RefAnnotations on given tier, or empty Vector.
     */
    public static final List<RefAnnotation> getKids(Annotation tthis, Tier tier) {
 //       logger.log(Level.INFO,
 //           "-- getKids (" + tthis.getValue() + ", " + tier.getName());

        List<RefAnnotation> result = new ArrayList<RefAnnotation>();

        if (!(tthis instanceof Annotation)) {
            return result;
        }

        List<Annotation> v = new ArrayList<Annotation>(((AbstractAnnotation) tthis).getParentListeners());

        for (Iterator<Annotation> e = v.iterator(); e.hasNext();) {
        	Annotation o = e.next();

            if (!(o instanceof RefAnnotation)) {
                continue;
            }

            RefAnnotation r = (RefAnnotation) o;

            if (r.getTier() != tier) {
                continue;
            }

 //           logger.log(Level.INFO, " >> " + r.getValue());
            result.add(r);
        }

//        logger.log(Level.INFO, "");

        return result;
    }

    /**
     * MK:02/06/28<br> An Annotation has children, possibly on many tiers.  If
     * there are kids are on the same tier, they are chained according the
     * next member of a RefAnno. 'this' may not have any childs, signaled by
     * returning null.
     *
     * @param tthis DOCUMENT ME!
     * @param tier restrict direct childs to this tier
     *
     * @return last direct child Annotations or null!
     */
    public static final RefAnnotation getLastKid(Annotation tthis, Tier tier) {
 //       logger.log(Level.INFO,
 //           "-- getLastKid (" + tthis.getValue() + ", " + tier.getName());

        List<RefAnnotation> kids = AnnotationUtil.getKids(tthis, tier);

        if (kids.size() == 0) {
            return null;
        } else {
            return AnnotationUtil.getLast(kids.get(0));
        }
    }

    /**
     * MK:02/06/28<br>The next-chain of RefAnnos lead to the last brother. If
     * there is no next-chain, return yourself
     *
     * @param tthis DOCUMENT ME!
     *
     * @return last Annotations from next-chain
     */
    public static final RefAnnotation getLast(RefAnnotation tthis) {
        //System.out.println("------- looking for last of " + tthis.getValue());
        if (tthis.getNext() == null) {
            return tthis;
        }

        // looking for a stack overflow
        if (tthis.getNext() == tthis) {
            return tthis; // who knows...
        }

        return AnnotationUtil.getLast(tthis.getNext());
    }
}
