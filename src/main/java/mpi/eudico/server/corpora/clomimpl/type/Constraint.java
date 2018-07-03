package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.List;


/**
 * DOCUMENT ME!
 * $Id: Constraint.java 45297 2016-05-23 10:39:40Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public interface Constraint extends Cloneable {
    /** Holds value of property DOCUMENT ME! */
    public static final int TIME_SUBDIVISION = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int INCLUDED_IN = 1;

    /** Holds value of property DOCUMENT ME! */
    public static final int NO_GAP_WITHIN_PARENT = 2;

    /** Holds value of property DOCUMENT ME! */
    public static final int SYMBOLIC_SUBDIVISION = 3;

    /** Holds value of property DOCUMENT ME! */
    public static final int SYMBOLIC_ASSOCIATION = 4;

    /** Holds value of property DOCUMENT ME! */
    public static final int MULTIPLE_REFS = 5;

    /** Holds value of property DOCUMENT ME! */
    public static final String[] stereoTypes = {
        "Time Subdivision", "Included In", "No Gap Within Parent",
        "Symbolic Subdivision", "Symbolic Association", "Multiple References"
    };

    /** Holds value of property DOCUMENT ME! */
    public static final String[] publicStereoTypes = {
        "Time Subdivision", "Included In", "Symbolic Subdivision", "Symbolic Association"
    };

    /**
     * DOCUMENT ME!
     *
     * @param segment DOCUMENT ME!
     * @param forTier DOCUMENT ME!
     */
    public void forceTimes(long[] segment, TierImpl forTier);

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot);

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getEndTimeForRefAnnotation(RefAnnotation theAnnot);

    /**
     * DOCUMENT ME!
     *
     * @param begin DOCUMENT ME!
     * @param end DOCUMENT ME!
     * @param forTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<TimeSlot> getTimeSlotsForNewAnnotation(long begin, long end,
        TierImpl forTier);

    /**
     * DOCUMENT ME!
     *
     * @param theTier DOCUMENT ME!
     */
    public void enforceOnWholeTier(TierImpl theTier);

    /**
     * Supposedly returns true when this tier supports insertBefore()
     * and insertAfter().
     */
    public boolean supportsInsertion();

    /**
     * DOCUMENT ME!
     *
     * @param beforeAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Annotation insertBefore(Annotation beforeAnn, TierImpl theTier);

    /**
     * DOCUMENT ME!
     *
     * @param afterAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Annotation insertAfter(Annotation afterAnn, TierImpl theTier);

    /**
     * Detaches annotation theAnn from tier theTier making sure that remaining
     * annotations on tier still meet the Constraint. Assumes that all
     * references and ParentAnnotationListener registrations are already
     * cleaned up.
     */
    public void detachAnnotation(Annotation theAnn, TierImpl theTier);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getStereoType();

    // constraints are nested
    public void addConstraint(Constraint theConstraint);
    
    public Constraint clone() throws CloneNotSupportedException;
}
