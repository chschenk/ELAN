package mpi.eudico.client.annotator.interlinear;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * A class that holds the value of an annotation in one or more lines. In
 * addition it stores x position and size of the annotation.
 *
 * @author Han Sloetjes
 */
public class InterlinearAnnotation {
    /** type for a root, top level annotation */
    public static final int ROOT = 0;

    /** annotation of type association */
    public static final int ASSOCIATION = 1;

    /** annotation of type subdivision */
    public static final int SUBDIVISION = 2;

    /** virtual annotation of type timecode */
    public static final int TIMECODE = 3;
    private String value;
    private String[] lines;
    private String tierName;

    /** begin time */
    public long bt;

    /** end time */
    public long et;

    /** the x position */
    public int x;

    /** the y position */

    //public int y;

    /** the real width of the annotation */
    public int realWidth;

    /** the calculated width based on context (child / parent annotations) */
    public int calcWidth;
    
    /** the number of columns this annotation spans in a (html) table */
    public int colSpan = 1;
    
    /** indicates not only that the annotation is empty but that it's just a filler */
    public boolean hidden = false;

    /** the number of lines this annotation needs in case of wrapping */
    public int nrOfLines = 1;

    /** a field for the type of the annotation */
    public int type = ROOT;

    /**
     * Creates a new InterlinearAnnotation instance
     *
     * @param aa the source annotation
     */
    public InterlinearAnnotation(Annotation aa) {
        if (aa != null) {
            value = aa.getValue().replace('\n', ' ').trim();
            bt = aa.getBeginTimeBoundary();
            et = aa.getEndTimeBoundary();

            TierImpl t = (TierImpl) aa.getTier();

            tierName = t.getName();

            if (t.hasParentTier()) {
                LinguisticType lt = t.getLinguisticType();

                if ((lt != null) && (lt.getConstraints() != null)) {
                    if (lt.getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
                        type = ASSOCIATION;
                    } else {
                        type = SUBDIVISION;
                    }
                } else {
                    // something is wrong
                    type = ASSOCIATION;
                }
            }

            // default = root
        }
    }

    /**
     * Creates a new InterlinearAnnotation instance for a timecode value
     *
     * @param timecode the timecode value
     * @param tcTierName the (internal) identifier of the timecode tier
     */
    public InterlinearAnnotation(String timecode, String tcTierName) {
        value = timecode;
        type = TIMECODE;
        tierName = tcTierName;
    }
    
    /**
     * Creates an empty InterlinearAnnotation instance, used for empty slots and empty cells in a table.
     * 
     * @param tierName the name of the tier this empty annotation is on
     * @param type the type of the tier, most likely ASSOCIATION or SUBDIVISION
     */
    public InterlinearAnnotation(String tierName, int type) {
        value = "";
        this.type = type;
        this.tierName = tierName;
    }

    /**
     * Returns the single value of the annotation.
     *
     * @return the single value of the annotation
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Sets the value, to be used for InterlinearAnnotations constructed without 
     * An annotation.
     * 
     * @param value the new value
     */
    public void setValue(String value) {
    	this.value = value;
    }

    /**
     * Returns the lines of the annotation in case of wrapping.
     *
     * @return an array of substrings
     */
    public String[] getLines() {
        return lines;
    }

    /**
     * When the annotation value has to be wrapped, the resulting lines can be
     * passed  to this object. Wrapping is not done by this class itself; it
     * can depend on available number of pixels or characters, font size and
     * font metrics etc.
     *
     * @param lines the calculated substrings
     */
    public void setLines(String[] lines) {
        this.lines = lines;

        if (lines != null) {
            nrOfLines = lines.length;
        } else {
            nrOfLines = 1;
        }
    }

    /**
     * Returns the annotation's tier name.
     *
     * @return the annotation's tier name
     */
    public String getTierName() {
        return tierName;
    }

    /**
     * Sets the name of the tier.
     * 
     * @param name the name
     */
    public void setTierName(String name) {
    	tierName = name;
    }
    
    /**
     * Returns the value of the annotation.
     *
     * @return the value of the annotation
     */
    @Override
	public String toString() {
        return value;
    }
}
