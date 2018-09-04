package mpi.eudico.client.annotator.interlinear.edit.render;

import mpi.eudico.client.annotator.interlinear.edit.IGTConstants;

/**
 * Rendering information for row headers (the tier names).
 */
public class IGTRowHeaderRenderInfo extends IGTRenderInfo {
	public int leftMargin = IGTConstants.TEXT_MARGIN_LEFT;
	public int rightMargin = IGTConstants.TEXT_MARGIN_RIGHT;

	/**
	 * Constructor.
	 */
	public IGTRowHeaderRenderInfo() {
		super();
	}

	/**
	 * @return the horizontalMargins, the sum of the left margin and right margin.
	 */
	public int getHorizontalMargins() {
		return leftMargin + rightMargin;
	}
	
}
