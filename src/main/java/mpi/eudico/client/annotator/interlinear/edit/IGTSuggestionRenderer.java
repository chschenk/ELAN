package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.Color;
import java.awt.Graphics;

import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionViewerModel;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTSuggestionRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTSuggestionViewerRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTViewerRenderInfo;

/**
 * A renderer for suggestion sets. The rendering is similar to that of igt groups,
 * but adds numbering of the sets and horizontal or vertical orientation of the display.  
 * 
 * @author Han Sloetjes
 */
public class IGTSuggestionRenderer {

	/**
	 * Returns the width of what was rendered, including a margin
	 */
	public static int renderOneSuggestion(Graphics g2d, 
			int sugIndex,
			IGTViewerRenderInfo vrInfo,
			IGTSuggestionViewerModel svModel) {
		
		IGTSuggestionModel sugModel = svModel.getRowData(sugIndex);
		IGTSuggestionRenderInfo sugModelRenderInfo = (IGTSuggestionRenderInfo) sugModel.getRenderInfo();
		IGTSuggestionViewerRenderInfo svModelRenderInfo = svModel.getRenderInfo();
		int height = sugModelRenderInfo.height;

		// draw header
		String slabel = "S" + (sugIndex + 1);
		String label = sugModel.getSuggestionSet().getLabel();
		if (label != null) {
			slabel = slabel + " " + label;
		}
		
		g2d.setColor(svModelRenderInfo.headerBackGround);
		g2d.fillRect(0, 0, sugModelRenderInfo.width, svModelRenderInfo.getColumnHeaderHeight(g2d));
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, sugModelRenderInfo.width - 1, svModelRenderInfo.getColumnHeaderHeight(g2d));
		// render the label with the right font, respect margins, use a substring to prevent painting
		// up to (over) the boundary of the suggestion component
		g2d.setFont(svModelRenderInfo.getHeaderFont());
		int textX = 0;
		if (sugModelRenderInfo.tierLabelsVisible) {
			textX += svModelRenderInfo.rowHeaderWidth;
		}
		int textW = sugModelRenderInfo.width;
		int textY = svModelRenderInfo.getColumnHeaderHeight(g2d) - g2d.getFontMetrics().getDescent();
		if (svModelRenderInfo.getTextInsets() != null) {
			textX += svModelRenderInfo.getTextInsets().left;
			textY -= svModelRenderInfo.getTextInsets().bottom;
			textW -= svModelRenderInfo.getTextInsets().right;
		}
		textW -= textX;
		String dispLabel = getDisplayLabel(g2d, textW, slabel); 
		g2d.drawString(dispLabel, textX, textY);
		
		// draw block background + line around it
		final int blockY = svModelRenderInfo.getColumnHeaderHeight(g2d);
		if (sugModelRenderInfo.getHighlightBGColor() != null) {
			g2d.setColor(sugModelRenderInfo.getHighlightBGColor());
		} else {
			g2d.setColor(svModelRenderInfo.blockBackGround);
		}
		g2d.fillRect(0, blockY,
				sugModelRenderInfo.width, sugModelRenderInfo.height - blockY);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawRect(0, blockY, 
				sugModelRenderInfo.width - 1, sugModelRenderInfo.height - blockY - 1);

		IGTGroupRenderer.renderRow2(g2d, sugModel, vrInfo, height, null);
		// Note: includeTierLabels is missing from this call, but
		// the fact that sugModel is a IGTSuggestionModel is (mis)used.
		// As long as svModelRenderInfo.headerHeight is the same height as
		// the omitted ROOT tier, layout looks ok. This is arranged in
		// SuggestionComponent.calculateLocationAndSize(...) by manipulating
		// the initial Y position and thereby shifting all the tiers.
		
		int xOff = sugModelRenderInfo.width;
		xOff += svModelRenderInfo.suggestionMargin;
		
		return xOff;
	}
	
	/**
	 * Returns the width of what was rendered, including a margin
	 */
	public static int renderOneSuggestion(Graphics g2d, 
			int sugIndex,
			int incrementalIndex,
			IGTViewerRenderInfo vrInfo,
			IGTSuggestionViewerModel svModel) {
		
		IGTSuggestionModel sugModel = svModel.getRowData(sugIndex);
		IGTSuggestionRenderInfo sugModelRenderInfo = (IGTSuggestionRenderInfo) sugModel.getRenderInfo();
		IGTSuggestionViewerRenderInfo svModelRenderInfo = svModel.getRenderInfo();
		int height = sugModelRenderInfo.height;

		// omit header
		
		g2d.setColor(svModelRenderInfo.headerBackGround);
		g2d.fillRect(0, 0, sugModelRenderInfo.width, svModelRenderInfo.getColumnHeaderHeight(g2d));
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, sugModelRenderInfo.width - 1, svModelRenderInfo.getColumnHeaderHeight(g2d));
		// don't render the label 
		// draw block background + line around it
		final int blockY = svModelRenderInfo.getColumnHeaderHeight(g2d);
		if (sugModelRenderInfo.getHighlightBGColor() != null) {
			g2d.setColor(sugModelRenderInfo.getHighlightBGColor());
		} else {
			g2d.setColor(svModelRenderInfo.blockBackGround);
		}
		g2d.fillRect(0, blockY,
				sugModelRenderInfo.width, sugModelRenderInfo.height - blockY);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawRect(0, blockY, 
				sugModelRenderInfo.width - 1, sugModelRenderInfo.height - blockY - 1);

		IGTGroupRenderer.renderRow2(g2d, sugModel, vrInfo, height, null);
		// Note: includeTierLabels is missing from this call, but
		// the fact that sugModel is a IGTSuggestionModel is (mis)used.
		// As long as svModelRenderInfo.headerHeight is the same height as
		// the omitted ROOT tier, layout looks ok. This is arranged in
		// SuggestionComponent.calculateLocationAndSize(...) by manipulating
		// the initial Y position and thereby shifting all the tiers.
		
		int xOff = sugModelRenderInfo.width;
		xOff += svModelRenderInfo.suggestionMargin;
		
		return xOff;
	}
	
	private static String getDisplayLabel(Graphics g, int textW, String slabel) {
		if (g.getFontMetrics().stringWidth(slabel) > textW) {
			String dispLabel = slabel.substring(0, slabel.length() - 1);
			while (dispLabel.length() > 0 && g.getFontMetrics().stringWidth(dispLabel) > textW) {
				dispLabel = dispLabel.substring(0, dispLabel.length() - 1);
			}
			return dispLabel;
		}
		return slabel;
	}
}
