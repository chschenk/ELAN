package mpi.eudico.client.annotator.interlinear.edit.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTNodeRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTTierRenderInfo;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A tier object representing a row in a IGTDataModel.
 * The tier can contain one or more IGTAnnotations. For "root" tiers or tiers
 * symbolically associated with the root the number of annotations is 0 or 1. 
 * 
 * @author Han Sloetjes
 */
public class IGTTier {
    private String tierName;
    private ArrayList<IGTAnnotation> annotations;
    private IGTTier parentTier;
    private List<IGTTier> childTiers;
    
	/**
	 * The type of the IGT tier
	 */
	private IGTTierType type;
	private IGTTierRenderInfo renderInfo;
	/**
	 *  A flag to set if this tier is a "word" level subdivision tier or a descendant tier thereof
	 * This flag should be consistent with the tier type.
	 * (could also define specific tier types for such tiers but this flag is a useful shorthand test).
	 * <p>
	 * If false, we're at a level where there are no subdivisions of the root annotation yet.
	 */
	private boolean isInWordLevelBlock = false;
    
	/**
	 * Constructor.
	 * 
	 * @param tierName the name of the tier
	 * @param type the type of tier
	 */
	public IGTTier(String tierName, IGTTierType type) {
		this(tierName, type, false);
		
		// The tier type must guaranteed not be a type that can be in
		// a word level block, or be one of the special tiers.
		if    (type == IGTTierType.ROOT || 
			   type == IGTTierType.FIRST_LEVEL_ASSOCIATION ||
			   type.isSpecial()) {
			// all ok
		} else {
			throw new IllegalArgumentException("This tier has the wrong type to be outside the word level blocks");
		}
	}
	
	public IGTTier(String tierName, IGTTierType type, boolean inWLBlock) {
		this.tierName = tierName;
		this.type = type;
		this.isInWordLevelBlock = inWLBlock;
		
		renderInfo = new IGTTierRenderInfo();
		annotations = new ArrayList<IGTAnnotation>();
		parentTier = null;
		childTiers = new ArrayList<IGTTier>(8);
	}

	// some standard getters and setters
	public String getTierName() {
		return tierName;
	}

	public void setTierName(String tierName) {
		this.tierName = tierName;
	}

	public ArrayList<IGTAnnotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(ArrayList<IGTAnnotation> annotations) {
		if (annotations != null) {// prevent annotations from being set to null
			this.annotations = annotations;
		}
	}

	public IGTTierType getType() {
		return type;
	}

	public void setType(IGTTierType type) {
		this.type = type;
	}
	
	/**
	 * Return whether this is a tier that has no normal annotations on it:
	 * a speaker label, a time code or a silence duration.
	 */
	public boolean isSpecial() {
		return type.isSpecial();
	}
	
    /**
     * Adds an annotation to this IGTTier.
     *
     * @param ann a <code>IGTAnnotation</code> that is to be added to
     *        this IGTTier
     */
    public void addAnnotation(IGTAnnotation ann) {
        if (ann != null) {
            annotations.add(ann);
            ann.setIGTTier(this);
        }
    }   
    
    /**
     * 
     * 
     * @param ann
     * @param index
     */
    public void insertAnnotation(IGTAnnotation ann, int index) 
    		throws ArrayIndexOutOfBoundsException {
        if (ann != null) {
            annotations.add(index, ann);
            ann.setIGTTier(this);
        }
    }
    
    /**
     * Removes an annotation from the IGTTier.
     * 
     * @param ann 
     */
    public void removeAnnotation(IGTAnnotation ann) {
    	if (ann != null) {
            annotations.remove(ann);
            ann.setIGTTier(null);
        }
    }
    
    /**
     * 
     * @param ann the reference annotation
     * @return the next annotation on this tier, or null
     */
    public IGTAnnotation getNextAnnotation(IGTAnnotation ann) {
    	if (ann == null) {
    		return null;
    	}
    	
    	int index = annotations.indexOf(ann);
    	if (index > -1 && index < annotations.size() - 1) {
    		return annotations.get(index + 1);
    	}
    	
    	// else return null
    	return null;
    }
    
    
    /**
     * 
     * @param ann the reference annotation
     * @return the previous annotation on this tier, or null
     */
    public IGTAnnotation getPreviousAnnotation(IGTAnnotation ann) {
    	if (ann == null) {
    		return null;
    	}
    	
    	int index = annotations.indexOf(ann);
    	if (index > 0) {
    		return annotations.get(index - 1);
    	}
    	
    	// else return null
    	return null;
    }
    
    /**
     * Returns the render information object.
     * 
     * @return the render information object
     */
    public IGTTierRenderInfo getRenderInfo() {
    	return renderInfo;
    }

	/**
	 * @return the parentTier
	 */
	public IGTTier getParentTier() {
		return parentTier;
	}
	
	/**	
	 * @return the root tier 
	 */
	public final IGTTier getRootTier() {
		if (parentTier == null) {
			return this;
		}
		return parentTier.getRootTier();
	}

	/**
	 * @param parentTier the parentTier to set
	 */
	public void setParentTier(IGTTier parentTier) {
		this.parentTier = parentTier;
	}

	/**
	 * This method returns the direct children of this tier. 
	 * This list does not contain deeper level descendants (grand
	 * children).  
	 * 
	 * @return the childTiers
	 */
	public List<IGTTier> getChildTiers() {
		return childTiers;
	}
	
	/**
	 * Returns a list of descendant tiers. The tiers are added recursively, depth first.
	 * 
	 * @return a list of descendant tiers, hierarchically
	 */
	public List<IGTTier> getDescendantTiers() {
		ArrayList<IGTTier> descendants = new ArrayList<IGTTier>();
		addDescendantsToList(descendants);
		
		return descendants;
	}
	
	/**
	 * Returns the level in the tier tree hierarchy, the distance of this tier to 
	 * a top level tier.
	 * 
	 * @return the distance to a "root" tier, 0 for top level tiers and special purpose tiers
	 */
	public int getLevel() {
		if (this.isSpecial()) {
			return 0;
		}
		
		if (getParentTier() == null) {
			return 0;
		}
		
		int level = 0;
		IGTTier t = this;
		
		while (t.getParentTier() != null) {
			level++;
			t = t.getParentTier();
		}
		
		return level;
	}
	
	/**
	 * Adds children to the list, recursively.
	 * 
	 * @param descendants the list to add to
	 */
	private void addDescendantsToList(ArrayList<IGTTier> descendants) {
		if (descendants == null) {
			return;
		}
		IGTTier t;
		for (int i = 0; i < childTiers.size(); i++) {
			t = childTiers.get(i);
			descendants.add(t);
			t.addDescendantsToList(descendants);
		}
	}
    
	/**
	 * Adds a child tier to the list of child tiers.
	 *  
	 * @param childTier the child tier to add
	 */
	public void addChildTier(IGTTier childTier) {
		if (childTier != null && !childTiers.contains(childTier)) {
			childTiers.add(childTier);
		}
	}
	
	/**
	 * Removes the given child tier
	 *  
	 * @param childTier the child tier to remove
	 */
	public void removeChildTier(IGTTier childTier){
		if (childTier != null && childTiers.contains(childTier)) {
			childTiers.remove(childTier);
		}
	}
	
	/**
	 * Returns whether this tier is a descendant of the specified tier, i.e. whether 
	 * the other tier is an ancestor of this tier.
	 * 
	 * @param otherTier the possible ancestor tier
	 * @return true if the other tier is an ancestor of this tier, false otherwise.
	 *         The tier is not considered an ancestor of itself.
	 */
	public boolean hasAncestor(IGTTier otherTier) {
		if (otherTier == null || otherTier == this) {
			return false;
		}
		
		IGTTier curParent = parentTier;
		while (curParent != null) {
			if (curParent == otherTier) {
				return true;
			}
			
			curParent = curParent.getParentTier();
		}
		
		return false;
	}
	
	/**
	 * Returns whether this tier is part of an interlinear block, i.e.
	 * a tier that is a "word-level" subdivision tier or a descendant thereof.
	 * Tiers of Word Level Blocks can have multiple annotations and they can wrap
	 * (blockwise).
	 *  
	 * @return true if this tier is in an interlinear block
	 */
	public boolean isInWordLevelBlock() {
		return isInWordLevelBlock;
	}
	
	/**
	 * Sets the flag whether this tier is part of an interlinear block.
	 * This should be consistent with the type of this tier.
	 * (Note: Oh? If this flag depends purely on the tier type,
	 *  why should one need to store the flag? 
	 *  Conversely, type DEEPER_LEVEL_ASSOCIATION doesn't seem to fit in this pattern.)
	 * 
	 * @param inBlock the new value of the flag
	 */
	public void setInWordLevelBlock(boolean inBlock) {
//		if (inBlock) {
//			if (type != IGTTierType.ROOT && type != IGTTierType.PHRASE_LEVEL_ROOT && type != IGTTierType.FIRST_LEVEL_ASSOCIATION && 
//					type != IGTTierType.SILENCE_DURATION && type != IGTTierType.SPEAKER_LABEL && 
//					type != IGTTierType.TIME_CODE) {
//				isInWordLevelBlock = inBlock;
//			} else {
//				// don't set the flag, throw an exception?
//				System.err.printf("setInWordLevelBlock(true) for tier type %s\n", type);
//			}
//		} else {
//			if (type == IGTTierType.ROOT || type == IGTTierType.PHRASE_LEVEL_ROOT || type == IGTTierType.FIRST_LEVEL_ASSOCIATION || 
//					type == IGTTierType.SILENCE_DURATION || type == IGTTierType.SPEAKER_LABEL || 
//					type == IGTTierType.TIME_CODE) {// allow
//				isInWordLevelBlock = inBlock;
//			} else {
//				// don't set the flag, throw an exception?
//				// The default for the flag is false anyway...
//				if (isInWordLevelBlock != inBlock) {
//					System.err.printf("setInWordLevelBlock(false) for tier type %s\n", type);
//					isInWordLevelBlock = inBlock;
//				}
//			}
//		}
		switch (type) {
		case ROOT:
		case PHRASE_LEVEL_ROOT:
		case FIRST_LEVEL_ASSOCIATION:
		case SILENCE_DURATION:
		case SPEAKER_LABEL:
		case TIME_CODE:
			// Can set it to FALSE on these types... but that is the default anyway.
			if (inBlock) {
				// don't set the flag, throw an exception?
				if (isInWordLevelBlock != inBlock) {
					LOG.warning(String.format("setInWordLevelBlock(true) for tier type %s",
							type));
				}
			}
			isInWordLevelBlock = inBlock;
			break;
		case NONE:
		case SUBDIVISION:
		case WORD_LEVEL_ROOT:
			// Can set it to TRUE on these types...
			if (!inBlock) {
				// don't clear the flag, throw an exception?
				if (isInWordLevelBlock != inBlock) {
					LOG.warning(String.format("setInWordLevelBlock(true) for tier type %s",
							type));
				}
			}
			isInWordLevelBlock = inBlock;
			break;
		case ASSOCIATION:	// can this case happen?
		case DEEPER_LEVEL_ASSOCIATION:
			// These types can be either, depending on the tier hierarchy.
			isInWordLevelBlock = inBlock;
		}
	}
	
	// #### methods for locating annotations etc #############

	/**
	 * Returns whether this tier has any, possibly wrapped, line at the specified y location. 
	 * 
	 * @param y the y location (in row editor coordinates)
	 * @return true if this tier has a line at the y coordinate, false otherwise
	 */
	public boolean isAtY(int y) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("isAtY %d? numLines=%d isInWordLevelBlock=%b",
					y, renderInfo.getNumLines(), isInWordLevelBlock));
		}
		
		// TODO check the grid of wrapped lines / blocks
		if (isInWordLevelBlock) {
			if (renderInfo.getYPositions() == null || renderInfo.getYPositions().size() <= 1) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine(String.format("isAtY %d? .y=%d .height=%d => %b",
							y, renderInfo.y, renderInfo.height, (y >= renderInfo.y && y <= renderInfo.y + renderInfo.height)));
				}
				
				return y >= renderInfo.y && (y <= renderInfo.y + renderInfo.height /*||
											 y <= renderInfo.y + renderInfo.renderHeight*/);
			}
			
			List<Integer> yPos = renderInfo.getYPositions(); 
			if (yPos != null) {
				int nextY;
				if (LOG.isLoggable(Level.FINE)) {
					LOG.fine(String.format("isAtY %d? %d Y positions",
							y, yPos.size()));
				}
				
				for (int i = 0; i < yPos.size(); i++) {
					nextY = yPos.get(i);
					
					if (LOG.isLoggable(Level.FINE)) {
						LOG.fine(String.format("isAtY %d? nextY=%d .height=%d => %b",
								y, nextY, renderInfo.height, (y >= nextY && y <= nextY + renderInfo.height)));
					}

					if (y >= nextY && y <= nextY + renderInfo.height) {
						return true;
					}
					
					if (nextY > y) {
						return false;
					}
				}
			}
		} else {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("isAtY %d? not inWLB. .y=%d .height=%d => %b",
						y, renderInfo.y, renderInfo.height, (y >= renderInfo.y && y <= renderInfo.y + renderInfo.height)));
			}
			
			return y >= renderInfo.y && y <= renderInfo.y + renderInfo.renderHeight;
		}
		return false;
	}
	
	/**
	 * Returns an Y coordinate for this tier. 
	 * <p>
	 * If the tier is wrapped, give the largest Y coordinate of the wrapped parts
	 * that is still smaller than the given Y.
	 * <p>
	 * Therefore, assuming the given Y is not in the tier, it returns where this tier
	 * is (just) above the Y, for cases where this is a parent tier (of the tier that
	 * does correspond to the Y coordinate). 
	 * 
	 * @param lessThanY the y location (in row editor coordinates)
	 * @return true if this tier has a line at the y coordinate, false otherwise
	 */
	public int getY(int lessThanY) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("getY %d? numLines=%d isInWordLevelBlock=%b",
					lessThanY, renderInfo.getNumLines(), isInWordLevelBlock));
		}
		
		// TODO check the grid of wrapped lines / blocks
		if (isInWordLevelBlock) {
			if (renderInfo.getYPositions() == null || renderInfo.getYPositions().size() <= 1) {
				return renderInfo.y;
			}

			List<Integer> yPos = renderInfo.getYPositions(); 
			if (yPos != null) {
				int prevY = renderInfo.y;
				
				for (int i = 0; i < yPos.size(); i++) {
					int nextY = yPos.get(i);

					if (nextY >= lessThanY) {
						return prevY;
					}
					
					prevY = nextY;
				}
				
				return prevY;
			}
		} else {
			return renderInfo.y;
		}
		
		return 0;
	}
	
	/**
	 * Returns the annotation that is located at the specified point given the current rendering settings.
	 * 
	 * @param p the point to query  (in renderInfo coordinates)
	 * @return the annotation at the point or null
	 */
	public IGTAnnotation getAnnotationAtPoint(Point p) {
		
		final int size = annotations.size();
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer(String.format("IGTTier.getAnnotationAtPoint(%s) isInWordLevelBlock=%b %s",
					String.valueOf(p), isInWordLevelBlock, String.valueOf(renderInfo)));
		}

		if (!isInWordLevelBlock) {
			// exclude special tiers, like time code, speaker, silence tier?
			if (size > 0) {
				IGTAnnotation igtAnn = annotations.get(0);
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("IGTTier.getAnnotationAtPoint(%s) !isInWordLevelBlock, 1st annotation: %s",
							String.valueOf(p), String.valueOf(igtAnn.getRenderInfo())));
				}
				
				if (igtAnn.getRenderInfo().isPointInRenderArea(p)) {
					LOG.finer("IGTTier.getAnnotationAtPoint() found!");
					return igtAnn;
				}

				LOG.finer("IGTTier.getAnnotationAtPoint() not isPointInRenderArea()");
			}
		} else {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("IGTTier.getAnnotationAtPoint() isInWordLevelBlock=true %s",
						String.valueOf(getRenderInfo())));
			}
			
			int i = 0;
			final List<Integer> wrapIndices = getRenderInfo().getWrapIndices();
			if (wrapIndices != null && !wrapIndices.isEmpty()) {
				// Child tiers that are wrapped may not have their wrap indices set (nor numLines > 1)... is that intentional??? XXX
				final List<Integer> yPositions = getRenderInfo().getYPositions();
				
				// Shortcut: if the Y we're looking for is further down,
				// skip to the next wrapped line.
				// Example: yPositions=[32, 80, 128] blockWrapIndices=[4, 11]]
				// if y is between ... and  80, start at i = 0
				// if y is between  80 and 128, start at i = 4
				// if y is between 128 and ..., start at i = 11
				int wrapIndex = 1;
				int yPos = yPositions.get(wrapIndex);
				while (p.y > yPos) {
					wrapIndex++;
					if (wrapIndex >= yPositions.size()) {
						break;
					}
					yPos = yPositions.get(wrapIndex);
				}
				if (wrapIndex > 1) {
					i = wrapIndices.get(wrapIndex - 2);
				}
			}
				
			for (; i < size; i++) {
				IGTAnnotation igtAnn = annotations.get(i);
				final IGTNodeRenderInfo renderInfo2 = igtAnn.getRenderInfo();
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("IGTTier.getAnnotationAtPoint(%s) in %s?",
							String.valueOf(p), String.valueOf(renderInfo2)));
				}
				
				// Shortcut: if the row may be wrapped, the annotation may be further down than our click
				if (renderInfo2.y > p.y) {
					break;
				}
				
				if (renderInfo2.isPointInRenderArea(p)) {
					LOG.finer("IGTTier.getAnnotationAtPoint() found!\n");
					return igtAnn;
				}
			}
		}
		
		LOG.finer("IGTTier.getAnnotationAtPoint() NULL...\n");
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("IGTTier:[");
		s.append(" tierName=");
		s.append(String.valueOf(tierName));
		s.append(" type=");
		s.append(String.valueOf(type));
		s.append(" isInWordLevelBlock=");
		s.append(String.valueOf(isInWordLevelBlock));
		s.append(String.valueOf(renderInfo));
		s.append("]");
		return s.toString();
	}
}
