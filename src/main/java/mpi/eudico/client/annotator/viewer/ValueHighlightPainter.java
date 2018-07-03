package mpi.eudico.client.annotator.viewer;

import java.awt.Color;
import java.util.Map;


/**
 * Extends StyledHighlightPainter by storing a Map of begin indices to colors.
 * This way this painter can be used for multiple highlights.
 *  
 * @author Han Sloetjes
 */
public class ValueHighlightPainter extends StyledHighlightPainter {
	private Map<Integer, Color> colors;
	
	/**
	 * @param c the color, ignored by this painter
	 * @param paintOffset the offset
	 */
	public ValueHighlightPainter(Color c, int paintOffset) {
		super(c, paintOffset, FILLED);
	}

	/**
	 * 
	 * @param c the color, ignored by this painter
	 * @param paintOffset the offset
	 * @param paintMode the mode, filled or stroked
	 */
	public ValueHighlightPainter(Color c, int paintOffset, int paintMode) {
		super(c, paintOffset, paintMode);
	}

	/**
	 * Sets the colors for (some) begin indices.
	 * @param colors the color map
	 */
	public void setColors(Map<Integer, Color> colors) {
		this.colors = colors;
	}
	
	/**
	 * Returns the colors for (some) begin indices.
	 * @return the color map
	 */
	public Map<Integer, Color> getColors() {
		return colors;
	}
	
	/**
	 * Returns the color for the given begin index, or null if not present.
	 * 
	 * @param beginIndex the begin index of the portion to highlight 
	 * @return the color for the given begin index, or null if not present
	 */
	@Override
	public Color getColor(int beginIndex) {
		if (colors != null) {
			return colors.get(beginIndex);
		}
		
		return null;
	}

}
