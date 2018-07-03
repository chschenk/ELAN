package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTTier;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTNodeRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTSuggestionRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTTierRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTViewerRenderInfo;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 *  A class for rendering a row of data (a group) into the specified graphics context.
 *  
 * @author Han Sloetjes
 */
public class IGTGroupRenderer {
		
	/**
	 * Renders a row / group into the specified graphics context.
	 * <p>
	 * It is assumed that the background has been cleared to the desired background colour.
	 * <p>
	 * This method is now used for both rendering of tiers and annotations and rendering 
	 * of suggestions produced by an analyzer. The tier rendering differs slightly.
	 * 
	 * @param selectedAnn the selected annotation in the viewer
	 * 
	 * @see IGTSuggestionRenderer#renderOneSuggestion(Graphics, int, boolean, IGTViewerRenderInfo, 
	 * 		mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionViewerModel)
	 */
	public static void renderRow2(Graphics g, IGTDataModel model, IGTViewerRenderInfo vrInfo,
			int height, IGTAnnotation selectedAnn) {
		
		boolean showTierLabels = true;
		boolean showTierIndentation = true;
		int labelWidth = 0;
		int indentPerLevel = 0;
		int leftMargin = 0;
		
		boolean isSuggestion = model instanceof IGTSuggestionModel;
		if (isSuggestion) {
			IGTSuggestionRenderInfo sugRenderInfo = (IGTSuggestionRenderInfo)((IGTSuggestionModel)model).getRenderInfo();
			showTierLabels = sugRenderInfo.tierLabelsVisible;
			showTierIndentation = sugRenderInfo.visualizeIndentation;
			indentPerLevel = sugRenderInfo.indentPerLevel;
			labelWidth = sugRenderInfo.rowHeaderWidth;
			if (sugRenderInfo.getTextInsets() != null) {
				leftMargin = sugRenderInfo.getTextInsets().left;
			}
		} else {
			labelWidth = vrInfo.headerWidth;
			leftMargin = model.getRowHeaderRenderInfo().leftMargin;
		}
		
		g.setColor(vrInfo.foregroundColor);
		
		if (showTierLabels) {
			//labelWidth = vrInfo.headerWidth;
			g.drawLine(labelWidth - 1, 0, labelWidth - 1, height);
		} else {
			labelWidth = 0;
		}
		
		// start rendering tiers, tier labels, annotations
		int defFontBaseline = g.getFontMetrics(vrInfo.defaultFont).getDescent();
				
		List<IGTTier> renderedTier = new ArrayList<IGTTier>();
		final int rowCount = model.getRowCount();
		
		for (int i = 0; i < rowCount; i++) {
			IGTTier tier = model.getRowData(i);
			if (renderedTier.contains(tier)) {
				continue;
			}
			if (isSuggestion && tier.getType() == IGTTierType.ROOT) {
				continue;
			}
			
			final IGTTierRenderInfo tierRenderInfo = tier.getRenderInfo();
			final ArrayList<IGTAnnotation> annotations = tier.getAnnotations();
			final int numAnnos = annotations.size();
			int indent = leftMargin; 
			if (showTierIndentation) {
				if (isSuggestion) {
					indent += (tier.getLevel() * indentPerLevel);
				} else {
					indent += (tier.getLevel() * tierRenderInfo.indentPerLevel);
				}
			}
			
			if (tier.isInWordLevelBlock()) {
				// tier labels
				if (showTierLabels) {
					g.setFont(vrInfo.defaultFont);
					g.setColor(vrInfo.getColorForTier(tier));					
					final List<Integer> yPositions = tierRenderInfo.getYPositions();
					
					// There may be block-wrapping in word-level blocks.
					if (yPositions != null) {
						//final int numYpositions = yPositions.size();
						
						for (int j = 0; j < yPositions.size(); j++) {
							int botY = yPositions.get(j) + tierRenderInfo.height;
							// TODO use the font from the tier render info, maybe as a preference
							// g.setFont(vrInfo.getFontForTier(tier.getTierName()));
							if (j != 0) {
								// draw a little marker or a "line wrap" character for each wrapped tier line
								//"\u21B5"
								g.drawLine(leftMargin, botY - defFontBaseline - g.getFont().getSize() / 2, 
										leftMargin, botY - defFontBaseline - g.getFont().getSize() / 4);
								if (indent > leftMargin) {
									g.drawLine(leftMargin, botY - defFontBaseline - g.getFont().getSize() / 4, 
										tierRenderInfo.indentPerLevel, 
										botY - defFontBaseline - g.getFont().getSize() / 4);
								}
							}
							
							g.drawString(model.getShortTierNameForIndex(i), indent, botY - defFontBaseline);
							
							if (LOG.isLoggable(Level.FINER)) {
								LOG.finer(String.format("string 1T: '%s'",
										model.getShortTierNameForIndex(i)));
							}
						}
					} else {
						int botY = tierRenderInfo.y + tierRenderInfo.height;
						// TODO use the font from the tier render info, maybe as a preference 
						//g.setFont(vrInfo.getFontForTier(tier.getTierName()));
						g.drawString(model.getShortTierNameForIndex(i), indent, botY - defFontBaseline);
						if (LOG.isLoggable(Level.FINER)) {
							LOG.finer(String.format("string 2T: '%s'",
									model.getShortTierNameForIndex(i)));
						}
					}
					
				}
				
				// annotations
				g.setFont(vrInfo.getFontForTier(tier.getTierName()));
				g.setColor(vrInfo.foregroundColor);
				int baseline = vrInfo.getBaselineForTier(g, tier.getTierName());

				for (int j = 0; j < numAnnos; j++) {
					IGTAnnotation igtAnn = annotations.get(j);
					IGTNodeRenderInfo annRender = igtAnn.getRenderInfo();
					int botY = annRender.y + annRender.height;
					
//					if (debugDrawString) System.out.printf("string 3A: '%s'\n", igtAnn.getTextValue());
					drawBox(igtAnn == selectedAnn, 
							labelWidth + annRender.x, annRender.y,
							annRender.calcWidth, annRender.height,
							g, vrInfo, 
							igtAnn.getTextValue(), labelWidth + annRender.x + vrInfo.annBBoxInsets.left, 
							botY - baseline - vrInfo.annBBoxInsets.bottom);
				}
			} else {
				// other types of tiers: a single long annotation;
				// maybe it's text lines are internally line-wrapped.
				int botY = tierRenderInfo.y + tierRenderInfo.height;	// bottom of whole tier
				
				// Tier labels
				if (showTierLabels) {
					// TODO use the font from the tier render info, maybe as a preference 
					//g.setFont(vrInfo.getFontForTier(tier.getTierName()));
					g.setFont(vrInfo.defaultFont);
					g.setColor(vrInfo.getColorForTier(tier));
					g.drawString(model.getShortTierNameForIndex(i), indent, botY - defFontBaseline); // position the baseline of the text correctly
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer(String.format("string 4T: '%s'",
								model.getShortTierNameForIndex(i)));
					}
				}
				
				// Annotation
				if (tierRenderInfo.getNumLines() > 1) {
					if (numAnnos >= 1) {
						IGTAnnotation igtAnn = annotations.get(0);
						final IGTNodeRenderInfo annRender = igtAnn.getRenderInfo();
						List<String> lines = annRender.getWrappedLines();
						
						if (lines != null) {
							g.setFont(vrInfo.getFontForTier(tier.getTierName()));
							g.setColor(vrInfo.foregroundColor);
							int baseline = vrInfo.getBaselineForTier(g, tier.getTierName());

							final int numLines = lines.size();
							for (int a = 0; a < numLines; a++) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.finer(String.format("string 5L: '%s'",
											lines.get(a)));
								}
								drawBox(igtAnn == selectedAnn, 
										labelWidth + annRender.x, botY - tierRenderInfo.height,
										annRender.calcWidth, tierRenderInfo.height,
										g, vrInfo, lines.get(a), 
										labelWidth + annRender.x + vrInfo.annBBoxInsets.left,
										botY - baseline - vrInfo.annBBoxInsets.bottom);
								
								botY += vrInfo.vertLineMargin;
								botY += tierRenderInfo.height;
							}
						} 				
					} 
				} else {
					if (numAnnos >= 1) {
						g.setFont(vrInfo.getFontForTier(tier.getTierName()));
						g.setColor(vrInfo.foregroundColor);
						int baseline = vrInfo.getBaselineForTier(g, tier.getTierName());

						IGTAnnotation igtAnn = annotations.get(0);
						final IGTNodeRenderInfo annRenderInfo = igtAnn.getRenderInfo();
						if (LOG.isLoggable(Level.FINER)) {
							LOG.finer(String.format("string 6A: '%s'",
									igtAnn.getTextValue()));
						}
						drawBox(igtAnn == selectedAnn, 
								labelWidth + annRenderInfo.x, botY - tierRenderInfo.height,
								annRenderInfo.calcWidth, tierRenderInfo.height,
								g, vrInfo, igtAnn.getTextValue(), 
								labelWidth + annRenderInfo.x + vrInfo.annBBoxInsets.left,
								botY - baseline - vrInfo.annBBoxInsets.bottom);
					}
				}
			}
			
			renderedTier.add(tier);
		}
	}

	private static void drawBox(boolean isSelected, 
			int x, int y, int w, int h,
			Graphics g,  IGTViewerRenderInfo vrInfo, 
			String text, int textX, int textY) {
		Color saveColor = g.getColor();

		if (vrInfo.showAnnoBackground) {
			g.setColor(vrInfo.annoBackgroundColor);
			g.fillRect(x, y, w, h);
		}
		if (isSelected) {
			g.setColor(Constants.ACTIVEANNOTATIONCOLOR);
			g.drawRect(x, y, w - 1, h -1);
		} else if (vrInfo.showAnnoBorders) {
			g.setColor(vrInfo.annoBorderColor);
			g.drawRect(x, y, w - 1, h - 1);			
		}
		g.setColor(saveColor);
		
		g.drawString(text, textX, textY);
	}

	/**
	 * For debugging: make the sort of tier clear.
	 * 
	 * @param tier
	 * @return a string indicating the type
	 */
	private static String tierTypeTag(IGTTier tier) {
		String wlb = tier.isInWordLevelBlock() ? " inWLB" : "";
		return " -- " + tier.getType().toString() + wlb;
	}
}
