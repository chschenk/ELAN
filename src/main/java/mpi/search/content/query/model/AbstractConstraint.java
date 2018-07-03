package mpi.search.content.query.model;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;


/**
 * Super class for AnchorConstraint and DepedentConstraint
 *
 * @author Alexander Klassmann
 * @version November 2004
 */
public abstract class AbstractConstraint implements Constraint {
    /** Holds TEMPORAL or STRUCTURAL */
    protected String mode = Constraint.TEMPORAL;

    /** Holds ANY or NONE */
    protected String quantifier = Constraint.ANY;

    /** Holds value of property DOCUMENT ME! */
    protected String[] tierNames = new String[0];

    /** holds parent node */
    private Constraint parent = null;

    /** Holds value of property DOCUMENT ME! */
    private Map<String, String> attributes;

    /** Holds value of property DOCUMENT ME! */
    private String patternString = "";

    /** Holds value of property DOCUMENT ME! */
    private String unit;

    /** holds node children of this node */
    private List<Constraint> children = new ArrayList<Constraint>();

    /** Holds value of property DOCUMENT ME! */
    private boolean isCaseSensitive = false;

    /** Holds value of property DOCUMENT ME! */
    private boolean isRegEx = false;

    /** Holds value of property DOCUMENT ME! */
    private long lowerBoundary = Long.MIN_VALUE;

    /** Holds value of property DOCUMENT ME! */
    private long upperBoundary = Long.MAX_VALUE;

    /**
     * Creates a new AbstractConstraint object.
     */
    public AbstractConstraint() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierNames constraint number within a query
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
    public AbstractConstraint(String[] tierNames, String patternString,
        long lowerBoundary, long upperBoundary, String unit, boolean isRegEx,
        boolean isCaseSensitive, Map<String, String> attributes) {
        this.tierNames = tierNames;
        this.patternString = patternString;
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
        this.unit = unit;
        this.isRegEx = isRegEx;
        this.isCaseSensitive = isCaseSensitive;
        this.attributes = attributes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean getAllowsChildren() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param h DOCUMENT ME!
     */
    @Override
	public void setAttributes(Map<String, String> h) {
        attributes = h;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getAttributes()
     */
    @Override
	public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    @Override
	public void setCaseSensitive(boolean b) {
        isCaseSensitive = b;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#isCaseSensitive()
     */
    @Override
	public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Constraint getChildAt(int i) {
        return children.get(i);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public int getChildCount() {
        return children.size();
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isEditable() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getId() {
        return (parent != null) ? (parent.getId() + "." +
        parent.getIndex(this)) : "C";
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isLeaf() {
        return (getChildCount() == 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    @Override
	public void setLowerBoundary(long l) {
        lowerBoundary = l;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getLowerBoundary()
     */
    @Override
	public long getLowerBoundary() {
        return lowerBoundary;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getLowerBoundaryAsString()
     */
    @Override
	public String getLowerBoundaryAsString() {
        return (lowerBoundary == Long.MIN_VALUE) ? "-X" : ("" + lowerBoundary);
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getMode()
     */
    @Override
	public String getMode() {
        return mode;
    }

    /**
     * This method is here to satisfy the MutableTreeNode interface.
     * <p>
     * In practice all nodes in the tree are Constraints.
     * <p>
     * This method may still be called from the java libraries,
     * or with parent == null.
     */
    @Override
	public void setParent(MutableTreeNode parent) {
        this.setParent((Constraint) parent);
    }

    @Override
	public void setParent(Constraint parent) {
        this.parent = parent;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Constraint getParent() {
        return parent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     */
    @Override
	public void setPattern(String s) {
        patternString = s;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getPattern()
     */
    @Override
	public String getPattern() {
        return patternString;
    }

    /**
     * returns Quantifier like ("ANY" or "NONE")
     *
     * @return String
     */
    @Override
	public String getQuantifier() {
        return quantifier;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    @Override
	public void setRegEx(boolean b) {
        isRegEx = b;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#isRegEx()
     */
    @Override
	public boolean isRegEx() {
        return isRegEx;
    }

    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     */
    public void setTierName(String s) {
        tierNames = new String[] { s };
    }

    /**
     * for Corex compatibility
     *
     * @return first element of tierNames[]
     */
    @Override
	public String getTierName() {
        return (tierNames.length > 0) ? tierNames[0] : null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     */
    @Override
	public void setTierNames(String[] s) {
        tierNames = s;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String[] getTierNames() {
        return tierNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     */
    @Override
	public void setUnit(String s) {
        unit = s;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getUnit()
     */
    @Override
	public String getUnit() {
        return unit;
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    @Override
	public void setUpperBoundary(long l) {
        upperBoundary = l;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getUpperBoundary()
     */
    @Override
	public long getUpperBoundary() {
        return upperBoundary;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getUpperBoundaryAsString()
     */
    @Override
	public String getUpperBoundaryAsString() {
        return (upperBoundary == Long.MAX_VALUE) ? "+X" : ("" + upperBoundary);
    }

    /**
     * dummy function; DefaultTreeModel uses it; has further no implication
     *
     * @param object DOCUMENT ME!
     */
    @Override
	public void setUserObject(Object object) {
    }

    /**
     * @see mpi.search.content.query.model.Constraint#addAttribute(String,
     *      String)
     */
    @Override
	public void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    /**
     * Implements javax.swing.tree.TreeNode.children().
     * Since this uses the old-style Enumeration, use the
     * collection-to-enumeration adapter.
     * <p>
     * 
     * {@inheritDoc}
     */
    @Override
	public Enumeration<Constraint> children() {
    	return Collections.enumeration(children);
    }

    /**
     * Overridden to make clone public.  Returns a shallow copy of this node;
     * the new node has no parent or children and has a reference to the same
     * user object, if any.
     *
     * @return a copy of this node
     */
    @Override
	public Object clone() {
        AbstractConstraint newConstraint = null;

        try {
            newConstraint = (AbstractConstraint) super.clone();

            newConstraint.setTierNames(getTierNames());
            newConstraint.setPattern(getPattern());
            newConstraint.setCaseSensitive(isCaseSensitive());
            newConstraint.setRegEx(isRegEx());
            newConstraint.setUnit(getUnit());
            newConstraint.setLowerBoundary(getLowerBoundary());
            newConstraint.setUpperBoundary(getUpperBoundary());
            newConstraint.setAttributes(getAttributes());
            newConstraint.children = new ArrayList<Constraint>();
            newConstraint.parent = null;
        } catch (CloneNotSupportedException e) {
        }

        return newConstraint;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
	public boolean equals(Object object) {
        if (!(object instanceof AbstractConstraint)) {
            return false;
        }

        AbstractConstraint constraint = (AbstractConstraint) object;

        if (constraint.isCaseSensitive() != isCaseSensitive()) {
            return false;
        }

        if (constraint.isRegEx() != isRegEx()) {
            return false;
        }

        if (!constraint.getPattern().equals(getPattern())) {
            return false;
        }

        if (constraint.getLowerBoundary() != getLowerBoundary()) {
            return false;
        }

        if (constraint.getUpperBoundary() != getUpperBoundary()) {
            return false;
        }

        if (((constraint.getUnit() == null) && (getUnit() != null)) ||
                ((constraint.getUnit() != null) &&
                !constraint.getUnit().equals(getUnit()))) {
            return false;
        }

        if (((constraint.getAttributes() == null) &&
                (constraint.getAttributes() != null)) ||
                ((constraint.getAttributes() != null) &&
                !constraint.getAttributes().equals(getAttributes()))) {
            return false;
        }

        return true;
    }

    /**
     * This method is here to satisfy the MutableTreeNode interface.
     * <p>
     * In practice all nodes in the tree are Constraints.
     * <p>
     * This method may still be called from the java libraries.
     *
     * @param child DOCUMENT ME!
     * @param index DOCUMENT ME!
     */
    @Override // MutableTreeNode
	public void insert(MutableTreeNode child, int index) {
    	insert((Constraint)child, index);
    }

    @Override // Constraint
	public void insert(Constraint child, int index) {
   		children.add(index, child);    	
        child.setParent(this);
    }
	
    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     */
    @Override
	public void remove(int index) {
        Constraint child = children.get(index);
        children.remove(index);
        child.setParent(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    @Override
	public void remove(MutableTreeNode node) {
        children.remove(node);
        node.setParent(null);
    }

    /**
             *
             */
    @Override
	public void removeFromParent() {
        if (parent != null) {
            parent.remove(this);
        }
    }

    /**
     * only for debugging
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Quantifier:\t" + quantifier + "\n");

        for (String tierName : tierNames) {
            sb.append("Tier name:\t" + tierName + "\n");
        }

        sb.append("Pattern:\t" + patternString + "\n");
        sb.append("Unit:\t" + unit + "\n");
        sb.append("Lower boundary:\t" + lowerBoundary + "\n");
        sb.append("Upper boundary:\t" + upperBoundary + "\n");

        return sb.toString();
    }
}
