package mpi.eudico.client.annotator.interlinear.edit.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.interlinear.edit.IGTConstants;

/**
 * This rendering information class holds properties for rendering sets of suggestions, 
 * on the level of a viewer showing multiple suggestions.
 *  
 * @author Han Sloetjes
 */
public class IGTSuggestionViewerRenderInfo extends IGTSuggestionRenderInfo {
	private Font headerFont = Constants.DEFAULTFONT;;
	private int headerHeight = 14;// the height of the column header
	public int suggestionMargin = IGTConstants.SUGGESTION_MARGIN;
	public Color headerBackGround = new Color(200, 200, 240);
	public Color blockBackGround = new Color(225, 225, 255);	
	
	/**
	 * @return the font for the column header
	 */
	public Font getHeaderFont() {
		return headerFont;
	}

	/**
	 * @param headerFont the font for the column header, not null
	 */
	public void setHeaderFont(Font headerFont) {
		if (headerFont == null) {
			return;
		}
		this.headerFont = headerFont;
		calcColumnHeaderHeight();
	}

	private void calcColumnHeaderHeight() {
		int h = headerFont.getSize();
		if (textBBoxInsets != null) {
			h += textBBoxInsets.top;
			h += textBBoxInsets.bottom;
		} else {
			h += 2;
		}
		headerHeight = h;
	}
	
	/**
	 * Returns a more accurate height for the column header, based on the font metrics
	 * of the graphics object.
	 *  
	 * @param g2d the rendering context
	 * @return the height for the column header
	 */
	public int getColumnHeaderHeight(Graphics g2d) {
		int h = g2d.getFontMetrics(headerFont).getHeight();
		if (textBBoxInsets != null) {
			h += textBBoxInsets.top;
			h += textBBoxInsets.bottom;
		} else {
			h += 2;
		}
		return h;
	}
	
	/**
	 * @return an estimated height based on font and insets
	 */
	public int getColumnHeaderHeight() {
		return headerHeight;
	}
	
	/**
	 * 
	 * @param textInsets the new insets for headers and annotations
	 */
	@Override
	public void setTextInsets(Insets textInsets) {
		textBBoxInsets = textInsets;
		calcColumnHeaderHeight();
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		buf.append(super.toString());
		buf.append(" headerHeight=");
		buf.append(String.valueOf(headerHeight));
		buf.append(" suggestionMargin=");
		buf.append(String.valueOf(suggestionMargin));
		buf.append(" headerBackGround=");
		buf.append(String.valueOf(headerBackGround));
		buf.append(" blockBackGround=");
		buf.append(String.valueOf(blockBackGround));
		
		return buf.toString();
	}
}
