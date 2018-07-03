package mpi.eudico.client.annotator.util;

import java.awt.Color;

import mpi.eudico.server.corpora.clom.Annotation;



/**
 * Stores an Annotation and some additional display specific information.<br>
 *
 * @author Han Sloetjes
 * @version 0.2 17/9/2003
 * @version 0.3 13/9/2004
 * @version 0.4 Dec 2009 added a boolean member for truncation and a color member
 */
public class Tag2D {
    private Annotation annotation;

    /* the Tier2D this Tag2D belongs to */
    private Tier2D tier2d;
    private String truncatedValue;
    private int x;
    private int width;
    private boolean isTruncated = false;
    private Color color;


    /**
     * Constructor; a Tag2D does not exist without a annotation object.<br>
     *
     * @param annotation the Annotation
     */
    public Tag2D(Annotation annotation) {
        this.annotation = annotation;
    }

    /**
     * Returns the Annotation.
     *
     * @return the Annotation
     */
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Sets the Tier2D.
     *
     * @param tier2d the Tier2D
     */
    public void setTier2D(Tier2D tier2d) {
        this.tier2d = tier2d;
    }

    /**
     * Returns the Tier2D.
     *
     * @param tier2d the Tier2D
     */
    public Tier2D getTier2D() {
        return tier2d;
    }
	
    /**
     * Returns the unmodified value of the enclosed Annotation.<br>
     *
     * @return the value of the Annotation
     *
     * @see #getTruncatedValue
     */
    public String getValue() {
    	if (annotation != null) {
			return annotation.getValue();
    	} else  {
    		return null;
    	}        
    }

    /**
     * Returns the truncated value of the enclosed Tag.<br>
     * The length of the truncated string depends on the width  that is
     * available for this Tag2D.
     *
     * @return the truncated value
     */
    public String getTruncatedValue() {
    	if (truncatedValue != null) {
			return truncatedValue;
    	} else {
    		return "";
    	}        
    }

    /**
     * Sets the truncated value.
     *
     * @param truncatedValue the truncated value
     */
    public void setTruncatedValue(String truncatedValue) {
        this.truncatedValue = truncatedValue;
        
    	if (annotation == null || truncatedValue == null || annotation.getValue() == null) {
    		isTruncated = false;
    	} else {
    		isTruncated = (truncatedValue.length() < annotation.getValue().length());
    	}
    }

    /**
     * Returns the begintime of the Annotation.
     *
     * @return the begintime of the Annotation
     */
    public long getBeginTime() {
    	if (annotation != null) {
			return annotation.getBeginTimeBoundary();
    	} else {
    		return 0;
    	}        
    }

    /**
     * Returns the endtime of the Annotation.
     *
     * @return the endtime of the Annotation
     */
    public long getEndTime() {
    	if (annotation != null) {
			return annotation.getEndTimeBoundary();
    	} else {
    		return 0;
    	}
        
    }

    /**
     * Sets the current width in pixels.
     *
     * @param width the current width in pixels
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the current width in pixels.
     *
     * @return the current width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the x coordinate for this annotation.
     *
     * @param x the x coordinate for this annotation
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Returns the x coordinate of this annotation.
     *
     * @return the x coordinate of this annotation
     */
    public int getX() {
        return x;
    }
    
    public boolean isTruncated() {
    	return isTruncated;
    }
    
    /**
     * Returns the preferred display color, or null
     * 
     * @return the color
     */
    public Color getColor() {
    	return color;
    }
    
    /**
     * Sets the preferred color for this annotation.
     * 
     * @param color
     */
    public void setColor (Color color) {
    	this.color = color;
    }
}
