package mpi.eudico.server.corpora.clomimpl.type;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * DOCUMENT ME!
 * $Id: ConstraintImpl.java 46477 2018-07-25 12:55:45Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public abstract class ConstraintImpl implements Constraint {
    /** Holds value of property DOCUMENT ME! */
    protected List<Constraint> nestedConstraints;

    /**
     * Creates a new ConstraintImpl instance
     */
    public ConstraintImpl() {
        nestedConstraints = new ArrayList<Constraint>();
    }

    /**
     * DOCUMENT ME!
     *
     * @param segment DOCUMENT ME!
     * @param forTier DOCUMENT ME!
     */
    @Override
	public void forceTimes(long[] segment, TierImpl forTier) {
        for (Constraint c : nestedConstraints) {
            c.forceTimes(segment, forTier);
        }
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
        long t = 0;

        for (Constraint c : nestedConstraints) {
            t = c.getBeginTimeForRefAnnotation(theAnnot);
        }

        return t;
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
        long t = 0;

        for (Constraint c : nestedConstraints) {
            t = c.getEndTimeForRefAnnotation(theAnnot);
        }

        return t; // default
    }

    /**
     * DOCUMENT ME!
     *
     * @param begin DOCUMENT ME!
     * @param end DOCUMENT ME!
     * @param forTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<TimeSlot> getTimeSlotsForNewAnnotation(long begin, long end,
        TierImpl forTier) {
        List<TimeSlot> slots = new ArrayList<TimeSlot>();

        for (Constraint c : nestedConstraints) {
            slots = c.getTimeSlotsForNewAnnotation(begin, end, forTier);
        }

        return slots;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTier DOCUMENT ME!
     */
    @Override
	public void enforceOnWholeTier(TierImpl theTier) {
        //	Iterator cIter = nestedConstraints.iterator();
        //	while (cIter.hasNext()) {
        //		((Constraint) cIter.next()).enforceOnWholeTier(theTier);
        //	}	
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean supportsInsertion() {
        return false;
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
        return null;
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
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnn DOCUMENT ME!
     * @param theTier DOCUMENT ME!
     */
    @Override
	public void detachAnnotation(Annotation theAnn, TierImpl theTier) {
        // default: do nothing
    }

    /**
     * DOCUMENT ME!
     *
     * @param theConstraint DOCUMENT ME!
     */
    @Override
	public void addConstraint(Constraint theConstraint) {
        nestedConstraints.add(theConstraint);
    }
	
	/**
	 * Overrides <code>Object</code>'s equals method by checking number and type
	 * of the nested Constraints of the other object to be equal to the number 
	 * and type of the nested Constraints in this object.
	 * 
	 * @param obj the reference object with which to compare
	 * @return true if this object is the same as the obj argument; false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			// null is never equal
			return false;
		}
		if (obj == this) {
			// same object reference 
			return true;
		}
		if (!(obj instanceof ConstraintImpl)) {
			// it should be a ConstraintImpl object
			return false;
		}
		
		ConstraintImpl other = (ConstraintImpl) obj;
		
		if (other.getStereoType() != this.getStereoType()) {
			return false;
		}
		
		if (nestedConstraints.size() != other.nestedConstraints.size()) {
			return false;
		}
		
		boolean allConstraintsEqual = true;
		
		loop:
		for (int i = 0; i < nestedConstraints.size(); i++) {
			ConstraintImpl ci = (ConstraintImpl) nestedConstraints.get(i);
			for (int j = 0; j < other.nestedConstraints.size(); j++) {
				if (ci.equals(other.nestedConstraints.get(j))) {
					continue loop;	
				}
			}
			// if we get here constraints are unequal
			allConstraintsEqual = false;
			break;
		}
		
		return allConstraintsEqual;
	}
	
	@Override
	public ConstraintImpl clone() throws CloneNotSupportedException {
		ConstraintImpl copy = (ConstraintImpl)super.clone();
		// deep-copy List
		ArrayList<Constraint> newList = new ArrayList<Constraint>();
		for (Constraint c : copy.nestedConstraints) {
			newList.add(c.clone());
		}
		copy.nestedConstraints = newList;
		
		return copy;
	}
	
    /**
     * @param typeConstant one of the stereotype constants, TIME_SUBDIVISION etc.
     * @return a String representation of the stereotype
     */
    public static String getStereoTypeName(int typeConstant) {
    	switch(typeConstant) {
    	case Constraint.TIME_SUBDIVISION:
    		return Constraint.publicStereoTypes[0];
    	case Constraint.INCLUDED_IN:
    		return Constraint.publicStereoTypes[1];
    	case Constraint.SYMBOLIC_SUBDIVISION:
    		return Constraint.publicStereoTypes[2];
    	case Constraint.SYMBOLIC_ASSOCIATION:
    		return Constraint.publicStereoTypes[3];
    	default:
    		return "No Constraint";
    	}
    }
}
