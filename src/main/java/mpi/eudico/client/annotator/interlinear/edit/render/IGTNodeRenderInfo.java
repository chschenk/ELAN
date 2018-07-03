package mpi.eudico.client.annotator.interlinear.edit.render;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


/**
 * A class for storing rendering information for an annotation node.
 * Coordinates, sizes, number of lines, colors
 *  
 * @author Han Sloetjes
 */
public class IGTNodeRenderInfo extends IGTRenderInfo {
	// TODO add members, might all be public?
	/** The width that the node really takes for its own contents */
	public int realWidth;
	/** The width that may be increased due to parents or children being wider,
	 * or decreased because of text wrapping. */
	public int calcWidth;
	
	/**
	 * The number of wrapped text lines in this annotation.
	 * <p>
	 * If the number is > 1, this should be only on tiers where there
	 * can be only one annotation!
	 */
	private int numLines = 1;
	
	// info about Color and Font on the IGTTier level?
	private List<String> wrappedLines;
	
	/**
	 * Returns the list of wrapped lines. Can be null.
	 * 
	 * @return the list of wrapped lines, can be null
	 */
	public List<String> getWrappedLines() {
		return wrappedLines;
	}
	
	/**
	 * Clears the list of wrapped lines.
	 */
	public void clearWrappedLines() {
		if (wrappedLines != null) {
			wrappedLines.clear();
		}
	}
	
	/**
	 * Adds a line to the list of wrapped lines.
	 * 
	 * @param line the line to add
	 */
	public void addWrappedLine(String line) {
		if (line == null) {
			return;
		}
		
		if (wrappedLines == null) {
			wrappedLines = new ArrayList<String>(4);
		}
		
		wrappedLines.add(line);
	}

	/**
	 * The number of wrapped text lines in this annotation.
	 * <p>
	 * If the number is > 1, this should be only on tiers where there
	 * can be only one annotation!
	 */
	public int getNumLines() {
		return numLines;
	}

	public void setNumLines(int numLines) {
		this.numLines = numLines;
	}

	/**
	 * The test is performed on the extended, calculated width of the node/annotation.
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.render.IGTRenderInfo#isPointInRenderArea(java.awt.Point)
	 */
	@Override
	public boolean isPointInRenderArea(Point p) {
		if (p == null) {
			return false;
		}
		return p.x >= x && p.x <= x + calcWidth && 
				p.y >= y && p.y <= y + height;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		//buf.append("IGTNodeRenderInfo:[");
		buf.append(" x=");
		buf.append(String.valueOf(x));
		buf.append(" y=");
		buf.append(String.valueOf(y));
		buf.append(" width=");
		buf.append(String.valueOf(width));
		buf.append(" height=");
		buf.append(String.valueOf(height));
		buf.append(" realW=");
		buf.append(String.valueOf(realWidth));
		buf.append(" calcW=");
		buf.append(String.valueOf(calcWidth));
		//buf.append("]");
		
		return buf.toString();
	}
	
}
