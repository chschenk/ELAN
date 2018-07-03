package mpi.eudico.client.annotator.interlinear;

import java.util.ArrayList;
import java.util.List;


/**
 * A printable, interlinear tier that can hold interlinear annotations and
 * typically is part of a InterlinearBlock.
 *
 * @author HS
 * @version 1.0
 */
public class InterlinearTier {
    private String tierName;
    private List<InterlinearAnnotation> annotations;
    private int numLines = 1;
    private int printHeight = 0;
    private int printWidth = 0;
    private int marginWidth = 0;
    private boolean isTimeCode;
    private boolean isSilDuration;

    /**
     * Creates a new InterlinearTier instance.
     *
     * @param tierName the name of the tier
     */
    public InterlinearTier(String tierName) {
        this.tierName = tierName;
        annotations = new ArrayList<InterlinearAnnotation>();
        isTimeCode = false;
    }

    /**
     * Adds an annotation to this InterlinearTier.
     *
     * @param ann a <code>InterlinearAnnotation</code> that is to be added to
     *        this InterlinearTier
     */
    public void addAnnotation(InterlinearAnnotation ann) {
        if (ann != null) {
            annotations.add(ann);
        }
    }

    /**
     * Returns all annotations on this tier.
     *
     * @return a list of all annotations on this tier
     */
    public List<InterlinearAnnotation> getAnnotations() {
        return annotations;
    }

    /**
     * Returns the tier name.
     *
     * @return the tier name
     */
    public String getTierName() {
        return tierName;
    }

    /**
     * Returns the width of the margin (for the tierlabel).
     *
     * @return the width of the margin
     */
    public int getMarginWidth() {
        return marginWidth;
    }

    /**
     * Sets the width of the margin for the tier label.
     *
     * @param marginWidth the new margin width
     */
    public void setMarginWidth(int marginWidth) {
        this.marginWidth = marginWidth;
    }

    /**
     * Returns the print height of this InterlinearTier.
     *
     * @return the print height
     */
    public int getPrintHeight() {
        return printHeight;
    }

    /**
     * Sets the print height of this InterlinearTier.
     *
     * @param printHeight the new print height
     */
    public void setPrintHeight(int printHeight) {
        this.printHeight = printHeight;
    }

    /**
     * Returns the width of the print tier.
     *
     * @return the width of the print tier
     */
    public int getPrintWidth() {
        return printWidth;
    }

    /**
     * Sets the width of this print tier.
     *
     * @param printWidth the new width of the tier
     */
    public void setPrintWidth(int printWidth) {
        this.printWidth = printWidth;
    }

    /**
     * Returns the current total width or advance of the annotations on this
     * tier.
     *
     * @return the current total width
     */
    public int getPrintAdvance() {
        if (annotations.size() == 0) {
            return printWidth;
        } else {
            InterlinearAnnotation last = annotations.get(annotations.size() -
                    1);

            return Math.max(last.x + last.calcWidth, printWidth);
        }
    }

    /**
     * Sets whether or not this is a time code 'tier'.
     *
     * @param isTimeCode if true this is a time code tier
     */
    public void setTimeCode(boolean isTimeCode) {
        this.isTimeCode = isTimeCode;
    }

    /**
     * Returns whether or not this is a silent duration 'tier'.
     *
     * @return true if this is a silDur tier, false otherwise
     */
    public boolean isSilDuration() {
        return isSilDuration;
    }
    
    /**
     * Sets whether or not this is a silent duration 'tier'.
     *
     * @param isSilDuration if true this is a time code tier
     */
    public void setSilDuration(boolean silDur) {
        this.isSilDuration = silDur;
    }

    /**
     * Returns whether or not this is a time code 'tier'.
     *
     * @return true if this is a timecode tier, false otherwise
     */
    public boolean isTimeCode() {
        return isTimeCode;
    }

    /**
     * Returns the max. number of lines of all annotations on this tier.
     *
     * @return the max. number of lines in any annotation on this tier
     */
    public int getNumLines() {
        return numLines;
    }

    /**
     * Set the max. number of lines of any annotation on this tier.
     *
     * @param newNumLines the new number of lines value
     */
    public void setNumLines(int newNumLines) {
        //if (newNumLines > numLines) {
            numLines = newNumLines;
        //}
    }
}
