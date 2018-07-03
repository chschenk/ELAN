package mpi.eudico.client.annotator.interlinear.edit.render;

import java.awt.Color;
import java.awt.Insets;

import mpi.eudico.client.annotator.interlinear.edit.IGTConstants;

/**
 * A rendering information class holding properties for rendering a set of suggestions.
 * The properties concern tier labels and annotations.
 * 
 * @see IGTSuggestionViewerRenderInfo
 * @author Han Sloetjes
 */
public class IGTSuggestionRenderInfo extends IGTBlockRenderInfo {
	protected Insets textBBoxInsets = null;
	public int rowHeaderWidth = 100;
	/* the number of pixels to use for one level of indentation */
	public int indentPerLevel = IGTConstants.INDENTATION_SIZE;
	public boolean visualizeIndentation = false;
	public boolean tierLabelsVisible = true;
	protected Color highlightBGColor = null;
	
	/** 
	 * @param textInsets the margins around labels and annotation values, the 
	 * bounding box insets for rendered text
	 */
	public void setTextInsets(Insets textInsets) {
		textBBoxInsets = textInsets;
	}
	
	/**
	 * @return the bounding box insets for rendered text
	 */
	public Insets getTextInsets() {
		return textBBoxInsets;
	}

	/**
	 * @return the color that has been set for highlighting or color coding
	 * a suggestion set. If null a default color will be used.
	 */
	public Color getHighlightBGColor() {
		return highlightBGColor;
	}

	/**
	 * @param highlightColor the new color for highlighting this set of suggestions
	 */
	public void setHighlightBGColor(Color highlightBGColor) {
		this.highlightBGColor = highlightBGColor;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		buf.append(super.toString());
		buf.append(" rowHeaderWidth=");
		buf.append(String.valueOf(rowHeaderWidth));
		buf.append(" indentationPerLevel=");
		buf.append(String.valueOf(indentPerLevel));
		buf.append(" showIndentation=");
		buf.append(String.valueOf(visualizeIndentation));
		if (textBBoxInsets != null) {
			buf.append(" textInsets=");
			buf.append(textBBoxInsets.toString());
		}
		if (highlightBGColor != null) {
			buf.append(" hlColor=");
			buf.append(highlightBGColor.toString());
		}
		
		return buf.toString();
	}
}
