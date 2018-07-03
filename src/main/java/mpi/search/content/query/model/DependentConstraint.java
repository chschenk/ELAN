package mpi.search.content.query.model;

import java.util.HashMap;


/**
 * $Id: DependentConstraint.java 8407 2007-03-21 08:08:34Z klasal $
 *
 * Constraint that depends on another constraint (i.e. is child node)
 */
public class DependentConstraint extends AbstractConstraint {
    /**
     * Creates a new DependentConstraint object.
     */
    public DependentConstraint() {
        mode = Constraint.STRUCTURAL;
    }

    /**
     * Creates a new DependentConstraint object.
     *
     * @param tierNames DOCUMENT ME!
     */
    public DependentConstraint(String[] tierNames) {
        this.tierNames = tierNames;
        mode = Constraint.STRUCTURAL;
    }

    /**
     * sets quantifier to ANY and mode to STRUCTURAL
     *
     * @param tierName name of tier
     * @param patternString string/regular expression to be searched
     * @param lowerBoundary negative number (of units) (e.g. 0, -1, -2, ... -X)
     * @param upperBoundary positive number (of units) (e.g. 0, 1, 2 ... +X)
     * @param unit search unit in which should be searched (in respect to
     *        referential constraint)
     * @param isRegEx string or regular expression ?
     * @param isCaseSensitive case sensitive string search ?
     * @param attributes should contain (as strings) attribute names (key) and
     *        values (value)
     */
    public DependentConstraint(String tierName, String patternString,
        long lowerBoundary, long upperBoundary, String unit, boolean isRegEx,
        boolean isCaseSensitive, HashMap attributes) {
        this(AnchorConstraint.ANY, tierName, patternString,
            Constraint.STRUCTURAL, lowerBoundary, upperBoundary, unit, isRegEx,
            isCaseSensitive, attributes);
    }

    /**
     * 
     *
     * @param quantifier (ANY or NONE)
     * @param tierName name of tier
     * @param patternString string/regular expression to be searched
     * @param mode temporal or structural
     * @param lowerBoundary negative number (of units) (e.g. 0, -1, -2, ... -X)
     * @param upperBoundary positive number (of units) (e.g. 0, 1, 2 ... +X)
     * @param unit search unit in which should be searched (in respect to
     *        referential constraint)
     * @param isRegEx string or regular expression ?
     * @param isCaseSensitive case sensitive string search ?
     * @param attributes should contain (as strings) attribute names (key) and
     *        values (value)
     */
    public DependentConstraint(String quantifier, String tierName,
        String patternString, String mode, long lowerBoundary,
        long upperBoundary, String unit, boolean isRegEx,
        boolean isCaseSensitive, HashMap attributes) {
        super(new String[] { tierName }, patternString, lowerBoundary,
            upperBoundary, unit, isRegEx, isCaseSensitive, attributes);
        this.mode = mode;
        this.quantifier = quantifier;
    }

    /**
     * sets kind of dependency
     *
     * @param s TEMPORAL or STRUCTURAL
     */
    public void setMode(String s) {
        mode = s;
    }

    /**
     * sets constraint quantifier
     *
     * @param s ANY or NO
     */
    public void setQuantifier(String s) {
        quantifier = s;
    }

    /**
     * clones itself and copies values
     *
     * @return copy of this class
     */
    @Override
	public Object clone() {
        DependentConstraint newConstraint = null;
        newConstraint = (DependentConstraint) super.clone();
        newConstraint.setMode(getMode());
        newConstraint.setQuantifier(getQuantifier());

        return newConstraint;
    }

    /**
     *
     *
     * @param object object to be compared
     *
     * @return true if constraints are the same
     */
    @Override
	public boolean equals(Object object) {
        if (!(object instanceof DependentConstraint)) {
            return false;
        }

        DependentConstraint constraint = (DependentConstraint) object;

        if (!constraint.getQuantifier().equals(getQuantifier())) {
            return false;
        }

        if (!constraint.getMode().equals(getMode())) {
            return false;
        }

        if (!constraint.getTierName().equals(getTierName())) {
            return false;
        }

        return super.equals(object);
    }
}
