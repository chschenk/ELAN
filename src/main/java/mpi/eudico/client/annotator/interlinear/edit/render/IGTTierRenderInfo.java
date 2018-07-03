package mpi.eudico.client.annotator.interlinear.edit.render;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.interlinear.edit.IGTConstants;


/**
 * Class for storing rendering information on the tier level.
 * Note: maybe this information should be at the renderer/editor level.
 * 
 * @author Han Sloetjes
 */
public class IGTTierRenderInfo extends IGTRenderInfo {
	public Color textColor;// foreground
	public Color editBgColor;//default background
	public Color nonEditBgColor;// or no annotation background
	
	public Font tierFont;// or store this on the viewer component layer
	/* the number of pixels to use for one level of indentation */
	public int indentPerLevel = IGTConstants.INDENTATION_SIZE;
	
	// these fields are used by "word" level tiers, first subdivision tiers under the root
	/** Stores the indices of the annotations at which to wrap to a next block of lines */
	private List<Integer> blockWrapIndices;
	/** in case of block-wise wrapping store the y positions of the top of each wrapped line.
	 * First it seemed that .y was equal to the first of these, but now I see it's the last
	 * (at least in some cases)
	 */ 
	private List<Integer> yPositions;
	
	/**
	 *  is > 1 if this is a wrapped "word" level tier; 
	 *  the child tiers that are wrapped along with it are not marked with numLines > 1.
	 *  
	 *  BUG: This seems the other way around: seems to be > 1 for line-wrapped tiers!
	 */
	private int numLines = 1;
	
	/**
	 * Stores the total height of non-block wrapped but line wrapped tiers.
	 * 
	 * Does not seem to be set for (potentially) block-wrapped tiers!
	 */
	public int renderHeight;
	
	/**
	 * The number of wrapped text lines in the single annotation
	 * on this tier.
	 * <p>
	 * If the number is > 1, this should be only on tiers where there
	 * can be only one annotation! This value is the same as the value
	 * from that one annotation.
	 */
	public int getNumLines() {
		return numLines;
	}

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.render.numLines
	 * @param numLines
	 */
	public void setNumLines(int numLines) {
		this.numLines = numLines;
	}

	/**
	 * Returns the list of indices at which to wrap the blocks of annotations. Can be null.
	 * 
	 * @return the list of annotation indices at which to wrap, can be null
	 */
	public List<Integer> getWrapIndices() {
		return blockWrapIndices;
	}
	
	/**
	 * Returns the list of y positions for this tier.
	 * The y positions are for the various wrapped parts of the tier.
	 * 
	 * @return the list of y positions
	 */
	public List<Integer> getYPositions() {
		return yPositions;
	}
	
	/**
	 * Removes all block wrap indices from the list (if a list exists).
	 */
	public void clearWrapIndices() {
		if (blockWrapIndices != null) {
			blockWrapIndices.clear();
			numLines = 1;
		}
	}
	
	/**
	 * Removes all stored y positions of this tier.
	 * The y positions are for the various wrapped parts of the tier.
	 */
	public void clearYPositions() {
		if (yPositions != null) {
			yPositions.clear();
			yPositions = null;
		}
	}
	
	/**
	 * Adds the index to the list of wrap indices (at the end). 
	 * 
	 * @param index the new wrap index to add to the list
	 */
	public void addWrapIndex(int index) {
		if (blockWrapIndices == null) {
			blockWrapIndices = new ArrayList<Integer>(4);
		}
		
		blockWrapIndices.add(index);
		numLines = blockWrapIndices.size() + 1;
	}
	
	/**
	 * Adds an y position to the list.
	 * The y positions are for the various wrapped parts of the tier.
	 * 
	 * @param yPos an y position
	 */
	public void addYPosition(int yPos) {
		if (yPositions == null) {
			yPositions = new ArrayList<Integer>(4);
		}
		yPositions.add(yPos);
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		//s.append(" IGTTierRenderInfo:[");
		s.append(super.toString());
		s.append(" renderHeight=");
		s.append(String.valueOf(renderHeight));
		s.append(" numLines=");
		s.append(String.valueOf(numLines));
		if (yPositions != null) {
			s.append(" yPositions=");
			s.append(String.valueOf(yPositions));
		}
		if (blockWrapIndices != null) {
			s.append(" blockWrapIndices=");
			s.append(String.valueOf(blockWrapIndices));
		}
		//s.append("]");
		return s.toString();
	}
}
