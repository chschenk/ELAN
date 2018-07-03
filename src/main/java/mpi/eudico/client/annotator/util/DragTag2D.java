package mpi.eudico.client.annotator.util;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

public class DragTag2D extends Tag2D {
    private int y;
    private int origX;
    private int origY = 0;
    private int dx;
    private int dy;

    // only support moving for top level tiers 
    private boolean moveAcrossTiersAllowed = false;
    /** a flag that can be used for highlighting when the dragged annotation is over a
     * drop target */
    public boolean isOverTargetTier = false;

	/**
	 * @param annotation
	 */
	public DragTag2D(Annotation annotation) {
		super(annotation);
		if (annotation != null) {
			if (annotation.getTier() != null && ((TierImpl) annotation.getTier()).getParentTier() == null) {
				moveAcrossTiersAllowed = true;
			}
		}
	}
	
	/**
	 * Copies properties from the other tag 2d.
	 * @param tag2d
	 */
	public void copyFrom(Tag2D tag2d) {
		if (tag2d != null) {
			this.setX(tag2d.getX());
			origX = tag2d.getX();
			this.setWidth(tag2d.getWidth());
			this.setTier2D(tag2d.getTier2D());
		}
	}

	/**
	 * Returns whether the drag annotation can be moved to another tier.
	 * Only true if the annotation's tier is a top level tier.
	 * 
	 * @return  true if the annotation's tier is a top level tier
	 */
	public boolean isMoveAcrossTiersAllowed() {
		return moveAcrossTiersAllowed;
	}
    
    /**
     * Returns the y coordinate of the annotation (for drag and drop).
     * 
     * @return the y coordinate
     */
    public int getY() {
		return y;
	}
    
    /**
     * Returns the original y coordinate.
     * @return the original y coordinate
     */
    public int getOrigY() {
    	return origY;
    }
    
    public void resetY() {
    	this.y = origY;
    }
    
    /**
     * Returns the starting point x coordinate.
     * @return the original x value
     */
    public int getOrigX() {
    	return origX;
    }

    /**
     * Sets the y coordinate of the annotation (for drag and drop).
     * 
     * @param y the y coordinate
     */
	public void setY(int y) {
		this.y = y;
	}
	
	public void resetX() {
		setX(origX);
	}
	
	public void move(int x, int y) {
		dx += x;
		dy += y;
	}

	public int getDx() {
		return dx;
	}

	public int getDy() {
		return dy;
	}
	
}
