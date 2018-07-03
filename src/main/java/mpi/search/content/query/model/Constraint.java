package mpi.search.content.query.model;

import java.util.Enumeration;
import java.util.Map;

import javax.swing.tree.MutableTreeNode;

/**
 * Created on Aug 18, 2004
 *
 * @author Alexander Klassmann
 * @version Aug 18, 2004
 */
public interface Constraint extends MutableTreeNode, Cloneable{
	public static final String ALL_TIERS = "Search.Constraint.AllTiers";
	/** a constant for a custom set of tiers as defined by the user */
	public static final String CUSTOM_TIER_SET = "Search.Constraint.CustomTiers";
    /**
     * 
     */
    public static final String TEMPORAL = "Search.Constraint.Temporal";

    /**
     * 
     */
    public static final String STRUCTURAL = "Search.Constraint.Structural";

    /**
     * 
     */
    public static final String[] MODES = { STRUCTURAL, TEMPORAL };

    /**
     * 
     */
    public static final String ANY = "Search.Constraint.Any";

    /**
     * 
     */
    public static final String NONE = "Search.Constraint.None";

    /**
     * 
     */
    public static final String[] QUANTIFIERS = new String[] { ANY, NONE };

    /**
     * 
     */
    public static final String IS_INSIDE = "Search.Constraint.Inside";

    /**
     * 
     */
    public static final String OVERLAP = "Search.Constraint.Overlap";

    /**
     * 
     */
    public static final String NOT_INSIDE = "Search.Constraint.NotInside";

    /**
     * 
     */
    public static final String NO_OVERLAP = "Search.Constraint.NoOverlap";

    /**
     * 
     */
    public static final String LEFT_OVERLAP = "Search.Constraint.LeftOverlap";

    /**
     * 
     */
    public static final String RIGHT_OVERLAP = "Search.Constraint.RightOverlap";

    /**
     * 
     */
    public static final String WITHIN_OVERALL_DISTANCE = "Search.Constraint.WithinOverallDistance";

    /**
     * 
     */
    public static final String WITHIN_DISTANCE_TO_LEFT_BOUNDARY = "Search.Constraint.WithinLeftDistance";

    /**
     * 
     */
    public static final String WITHIN_DISTANCE_TO_RIGHT_BOUNDARY = "Search.Constraint.WithinRightDistance";

    /**
     * 
     */
    public static final String BEFORE_LEFT_DISTANCE = "Search.Constraint.BeforeLeftDistance";

    /**
     * 
     */
    public static final String AFTER_RIGHT_DISTANCE = "Search.Constraint.AfterRightDistance";

    /**
     * 
     */
    public static final String[] ANCHOR_CONSTRAINT_TIME_RELATIONS = {
            IS_INSIDE, OVERLAP, NOT_INSIDE, NO_OVERLAP
        };

    /**
     * 
     */
    public static final String[] DEPENDENT_CONSTRAINT_TIME_RELATIONS = {
            IS_INSIDE, OVERLAP, LEFT_OVERLAP, RIGHT_OVERLAP,
            WITHIN_OVERALL_DISTANCE, WITHIN_DISTANCE_TO_LEFT_BOUNDARY,
            WITHIN_DISTANCE_TO_RIGHT_BOUNDARY, BEFORE_LEFT_DISTANCE,
            AFTER_RIGHT_DISTANCE
        };

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Map<String, String> getAttributes();

    /**
     *
     *
     * @param b DOCUMENT ME!
     */
    public void setCaseSensitive(boolean b);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isCaseSensitive();

    /**
     *
     *
     * @param l DOCUMENT ME!
     */
    public void setLowerBoundary(long l);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getLowerBoundary();

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public String getLowerBoundaryAsString();

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public String getMode();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getId();

    /**
     *
     *
     * @param s DOCUMENT ME!
     */
    public void setPattern(String s);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getPattern();

    /**
     * returns Quantifier like ("ANY" or "NONE")
     *
     * @return String
     */
    public String getQuantifier();

    /**
     *
     *
     * @param b DOCUMENT ME!
     */
    public void setRegEx(boolean b);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isRegEx();

    /**
     *
     *
     * @param s DOCUMENT ME!
     */
    public void setTierNames(String[] s);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getTierName();

    public String[] getTierNames();
    /**
     *
     *
     * @param s DOCUMENT ME!
     */
    public void setUnit(String s);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getUnit();

    /**
     *
     *
     * @param l DOCUMENT ME!
     */
    public void setUpperBoundary(long l);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getUpperBoundary();

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public String getUpperBoundaryAsString();
    
    public void setAttributes(Map<String, String> h);

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    public void addAttribute(String name, String value);
    
    public Object clone();
    
    public boolean isEditable();
    
    // specialize from MutableTreeNode
    @Override
	public Enumeration<Constraint> children();
	public void insert(Constraint child, int index);
	public void setParent(Constraint parent);
    @Override
	public Constraint getChildAt(int i);
}
