package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTNode;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTTier;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTNodeRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTSuggestionViewerRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTViewerRenderInfo;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * A class containing methods for producing the layout, calculating sizes, wrapping lines etc.
 * for an interlinear block.
 * 
 * @author Han Sloetjes
 */
public class IGTCalculator implements ClientLogger {
	private static final int MIN_ANNOTATION_PIXEL_WIDTH = 10;
	/**
	 * Private constructor, only static methods. 
	 */
	private IGTCalculator() {
	}

	/**
	 * Calculates the y positions of tiers and the total height for the tier in case of wrapping.
	 * 
	 * This method is for tiers that are not part of a subdivision group of tiers.
	 * 
	 * @param viewerRenderInfo the object containing rendering information (fonts, margins, sizes)
	 * @param tier the tier to calculate y positions and height
	 * @param yPosStart the y coordinate (of the top) at which the tier starts
	 * 
	 * @return the total height of the tier
	 */
	public static int calculateTierYPosition(Graphics g2d, IGTViewerRenderInfo viewerRenderInfo, IGTTier tier, int yPosStart) {
		int lineHeight = viewerRenderInfo.getHeightForTier(g2d, tier.getTierName());
		tier.getRenderInfo().y = yPosStart;
		
		int totHeight = 0;
		final int numLines = tier.getRenderInfo().getNumLines();
		totHeight += (numLines * lineHeight);
		totHeight += ((numLines - 1) * viewerRenderInfo.vertLineMargin);
		tier.getRenderInfo().height = lineHeight; // or add the margin to the height?
		tier.getRenderInfo().renderHeight = totHeight;
		
		for (int i = 0; i < tier.getAnnotations().size(); i++) { // should be one annotation
			IGTAnnotation igtAnn = tier.getAnnotations().get(i);
			igtAnn.getRenderInfo().y = yPosStart;
			igtAnn.getRenderInfo().height = totHeight;
		}
		
		totHeight += viewerRenderInfo.vertLineMargin;
		return totHeight;
	}
	
	/**
	 * Calculates the y positions of a tier and its child tiers and the total height 
	 * for the group of tiers (including possible wrapping).
	 * <p>
	 * This method is for tiers that are the root of a subdivision group of tiers
	 * (WORD_LEVEL_ROOT).
	 * 
	 * @param viewerRenderInfo the object containing rendering information (fonts, margins, sizes)
	 * @param tier the root tier to calculate y positions and height
	 * @param yPosStart the y coordinate (of the top) at which the tier starts
	 * 
	 * @return the total height of the tier group (not the bottom Y position)
	 */
	public static int calculateTierYPositionRecursive(Graphics g2d, IGTViewerRenderInfo viewerRenderInfo, IGTTier tier, int yPosStart) {
		int curHeight = viewerRenderInfo.getHeightForTier(g2d, tier.getTierName());
		tier.getRenderInfo().clearYPositions();
		tier.getRenderInfo().y = yPosStart;
		tier.getRenderInfo().height = curHeight; // or add the margin to the height?
		int totHeight = 0;
		int numBlocks = 1;
		List<Integer> wrapIndices = tier.getRenderInfo().getWrapIndices();
		if (wrapIndices != null) {
			numBlocks = wrapIndices.size() + 1;
		}
		
		int tempY = yPosStart;
		List<IGTTier> descendants = tier.getDescendantTiers();
		
		for (int j = 0; j < numBlocks; j++) {
			tier.getRenderInfo().addYPosition(tempY);

			int minA = 0;
			final ArrayList<IGTAnnotation> annotations = tier.getAnnotations();
			final int numAnnotations = annotations.size();
			int maxA = numAnnotations - 1;
			if (maxA < 0) {// no annotations
				//continue; 
			}
			
			if (j > 0) {
				minA = wrapIndices.get(j - 1);
			} 
			if (j < numBlocks - 1) {
				maxA = wrapIndices.get(j) - 1;
			}
			
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("Y: calculateTierYPositionRecursive: minA=%d, maxA=%d",
						minA, maxA));
			}
			
			for (int a = minA; a <= maxA; a++) {
				IGTAnnotation igtAnn = annotations.get(a);
				igtAnn.getRenderInfo().y = tempY;
				igtAnn.getRenderInfo().height = curHeight;
				
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("Y: calculateTierYPositionRecursive: a=%d, y=%d\n%s",
							a, tempY, igtAnn.toString()));
				}
			}
			
			tempY += (curHeight + viewerRenderInfo.vertLineMargin);
			totHeight += (curHeight + viewerRenderInfo.vertLineMargin);
			
			long chminX = 0;
			long chMaxX = 0;
			boolean useChXVal = true;
			
			if (minA >= 0 && minA < numAnnotations) {
				final IGTAnnotation igtMinAnnotation = annotations.get(minA);
				if (igtMinAnnotation.getAnnotation() == null) {
					chminX = igtMinAnnotation.getRenderInfo().x;
				} else {
					chminX = igtMinAnnotation.getAnnotation().getBeginTimeBoundary();
					useChXVal = false;
				}
				
			}
			if (maxA >= 0 && maxA < numAnnotations) {
				final IGTAnnotation igtMaxAnnotation = annotations.get(maxA);
				if (useChXVal) {
					chMaxX = igtMaxAnnotation.getRenderInfo().x + igtMaxAnnotation.getRenderInfo().calcWidth;
				} else {
					chMaxX = igtMaxAnnotation.getAnnotation().getEndTimeBoundary();
				}
			}			
			
			// don't just take children, but all descendants
			for (int k = 0; k < descendants.size(); k++) {
				IGTTier depTier = descendants.get(k);
				int deptHeight = g2d == null ? depTier.getRenderInfo().height
						                     : viewerRenderInfo.getHeightForTier(g2d, depTier.getTierName());
				totHeight += (deptHeight + viewerRenderInfo.vertLineMargin);
				if (j == 0) {
					depTier.getRenderInfo().clearYPositions();
				}
				depTier.getRenderInfo().height = deptHeight;
				depTier.getRenderInfo().y = tempY;
				depTier.getRenderInfo().addYPosition(tempY);
				
				// update annotations coordinates
				// on the dep. tier
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("Y: calculateTierYPositionRecursive: start on %d descendant annotations, useChXVal=%b, minA=%d chminX=%d maxA=%d chmaxX=%d",
							depTier.getAnnotations().size(), useChXVal, minA, chminX, maxA, chMaxX));
				}
				for (int a = 0; a < depTier.getAnnotations().size(); a++) {
					IGTAnnotation igtAnn = depTier.getAnnotations().get(a);
					IGTNodeRenderInfo annRender = igtAnn.getRenderInfo();
					
					if (useChXVal) {
						if (annRender.x < chminX) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.finer(String.format("Y: calculateTierYPositionRecursive: skip anno %d: annRender.x < chminX (%d < %d)",
										a, annRender.x, chminX));
							}
							continue;
						}
						if (annRender.x > chMaxX) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.finer(String.format("Y: calculateTierYPositionRecursive: stop after anno %d: annRender.x > chMaxX (%d > %d)",
										a, annRender.x, chMaxX));
							}
							break;
						}
					} else {
						long beginDep = igtAnn.getAnnotation().getBeginTimeBoundary();
						long endDep = igtAnn.getAnnotation().getBeginTimeBoundary();
						
						if (beginDep < chminX) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.finer(String.format("Y: calculateTierYPositionRecursive: skip anno %d: beginDep < chminX (%d < %d)",
										a, beginDep, chminX));
							}
							continue;
						}
						
						if (endDep > chMaxX) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.finer(String.format("Y: calculateTierYPositionRecursive: stop after anno %d: endDep > chMaxX (%d > %d)",
										a, endDep, chMaxX));
							}
							break;
						}
					}
					
					annRender.y = tempY;
					annRender.height = deptHeight;
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer(String.format("calculateTierYPositionRecursive: anno=%d y=%d\n%s",
								a, tempY, igtAnn.toString()));
					}
				}
				tempY += (deptHeight + viewerRenderInfo.vertLineMargin);
			}
			
			//tempY += curHeight;
		}
					
		totHeight += viewerRenderInfo.vertLineMargin;
		return totHeight;
	}
	
	/**
	 * Calculates the location (x) and size of the annotations of this tier and depending tiers.
	 * Returns the maximal width the (group of) tier(s) occupies.
	 * 
	 * @param g2d the graphics context
	 * @param viewerRenderInfo the object containing rendering information (fonts, margins, sizes)
	 * @param tier the tier to calculate
	 * @param recursive if true the calculations are performed recursively based on the hierarchy
	 * of the annotations
	 * 
	 * @return the total width (advance) of the tier
	 */
	public static int calculateTierAndAnnotationsLAS(Graphics g2d, IGTViewerRenderInfo viewerRenderInfo, 
			IGTTier tier, boolean recursive) {
		if (tier == null) {
			return 0;
		}
		
		List<IGTAnnotation> anns = tier.getAnnotations();
		int curXAdvance = 0;
		int curXStart = 0;
		FontMetrics fm = g2d.getFontMetrics(viewerRenderInfo.getFontForTier(tier.getTierName()));
		
		for (IGTAnnotation ia : anns) {
			final IGTNodeRenderInfo renderInfo = ia.getRenderInfo();
			int rw = annotationWidth(fm, ia.getTextValue()) + viewerRenderInfo.getHorizontalBBoxInsets();
			renderInfo.realWidth = rw;
			renderInfo.calcWidth = rw;
			renderInfo.x = curXStart; //curXAdvance + viewerRenderInfo.whitespaceWidth;
			curXAdvance += rw;
			// iterate over children. Iterate down and up again.
			// calculate the max width of all children, based on the hierarchy
			if (recursive && ia.getChildCount() > 0) {
				int maxChildrenWidth = 0;
				int curXChildStart = curXStart;
				Map<IGTTier, List<IGTNode>> groupedChildren = ia.getChildrenPerTier();
				for (List<IGTNode> children : groupedChildren.values()) {
					int curWidth = calculateChildrenLAS(g2d, viewerRenderInfo, children, 
							curXChildStart /*curXAdvance + viewerRenderInfo.whitespaceWidth*/);
					
					if (curWidth > maxChildrenWidth) {
						maxChildrenWidth = curWidth;
					}
				}
				
				if (maxChildrenWidth > renderInfo.calcWidth) {
					renderInfo.calcWidth = maxChildrenWidth;
				}
				// update last child per group to match the "word" level calculated width				
				IGTCalculator.updateLastChildSize(ia);

				curXAdvance = renderInfo.x + renderInfo.calcWidth;
				// end of this group of word level annotation + depending annotations
			}
			
			curXStart += (renderInfo.calcWidth + viewerRenderInfo.whitespaceWidth);
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("X: calculateTierAndAnnotationsLAS %s",
						ia.toString()));
			}
		}
		// the total advance of this tiers' annotations
		return curXAdvance;
	}

	/**
	 * Calculate the width of an annotation.
	 * Use a minimum with so that there is something to see for the user.
	 */
	private static int annotationWidth(FontMetrics fm, String text) {
		return Math.max(MIN_ANNOTATION_PIXEL_WIDTH, fm.stringWidth(text));
	}
	
	/**
	 * Method to be called recursively for calculating the width of all children of a particular annotation.
	 * This is always recursive (currently).
	 * 
	 * @param g2d the Graphics context
	 * @param viewerRenderInfo the object containing rendering information (fonts, margins, sizes)
	 * @param children the list of children
	 * @param startX the x coordinate for the x location of the first child per tier
	 * 
	 * @return the total width (advance) of the children
	 */
	public static int calculateChildrenLAS(Graphics g2d, IGTViewerRenderInfo viewerRenderInfo, List<IGTNode> children, int startX) {
		if (children == null || children.size() == 0) {
			return 0;
		}
		int curXAdvance = startX;
		int totalChildWidth = 0;
		
		for (int i = 0; i < children.size(); i++) {
			IGTAnnotation ia = (IGTAnnotation) children.get(i);
			FontMetrics fm = g2d.getFontMetrics(viewerRenderInfo.getFontForTier(ia.getIGTTier().getTierName()));
			int width = annotationWidth(fm, ia.getTextValue()) + viewerRenderInfo.getHorizontalBBoxInsets();
			
			ia.getRenderInfo().x = curXAdvance;

			ia.getRenderInfo().realWidth = width;
			ia.getRenderInfo().calcWidth = width;
			
			if (ia.getChildCount() > 0) {
				int maxChildrenWidth = 0;
				Map<IGTTier, List<IGTNode>> groupedChildren = ia.getChildrenPerTier();
				for (List<IGTNode> grchildren : groupedChildren.values()) {
					int curWidth = calculateChildrenLAS(g2d, viewerRenderInfo,
							grchildren,
							ia.getRenderInfo().x);
					
					if (curWidth > maxChildrenWidth) {
						maxChildrenWidth = curWidth;
					}
				}
				if (maxChildrenWidth > width) {
					width = maxChildrenWidth;
					ia.getRenderInfo().calcWidth = maxChildrenWidth;
				}
				
				// update the last child on child tiers to match the total width
				updateLastChildSize(ia);
			}
			
			curXAdvance += width;
			totalChildWidth += width;
			if (i < children.size() - 1) {// was i <= children.size() - 1
				curXAdvance += viewerRenderInfo.whitespaceWidth;
				totalChildWidth += viewerRenderInfo.whitespaceWidth;
			}
		}
		
		return totalChildWidth;
	}
	
	/**
	 * Updates the width of the last child of a node, such that it spans the area up
	 * to the width of this node. Recursive.
	 * <p>
	 * On wrapped blocks, it should only be as wide as the rhs of the word level root
	 * (which has been wrapped with this one).
	 * 
	 * @param lastNode the last child node on a particular depending tier
	 */
	public static void updateLastChildSize(IGTNode currentNode) {
		if (currentNode != null && currentNode.getChildCount() > 0) {
			Map<IGTTier, List<IGTNode>> groupedChildren = currentNode.getChildrenPerTier();

			for (Map.Entry<IGTTier, List<IGTNode>> e : groupedChildren.entrySet()) {
				List<IGTNode> children = e.getValue();
				IGTTier tier = e.getKey();
				
//				final List<Integer> wrapIndices = tier.getRenderInfo().getWrapIndices();
//				if (wrapIndices != null && !wrapIndices.isEmpty()) {
//					// On block-wrapped tiers, the last annotation is not anywhere
//					// near the rhs of the root annotation.
//					// Don't bother adjusting it.
//					// TODO: maybe do it on all full lines (the not-last ones)?
//					// XXX: this is called before calculateWrappingInfo(...) so anything
//					// we think we know about wrapping is outdated by now.
//					continue;
//				}

				if (children.size() > 0) {// (un)necessary check?
					IGTNode lastNode = children.get(children.size() - 1);
					
					if (LOG.isLoggable(Level.FINER)) {
						if (lastNode instanceof IGTAnnotation) {
							IGTAnnotation igtAnno = (IGTAnnotation)lastNode;
							AbstractAnnotation anno = igtAnno.getAnnotation();
							final String value = anno == null ? "null"
									                          : anno.getValue();
							LOG.finer(String.format("X: updateLastChildSize: anno='%s'",
									value));
						}
					}
					int rhs = currentNode.getRenderInfo().x + currentNode.getRenderInfo().calcWidth;  

					// it shouldn't be possible that the child's calc width > the parent's 
					// calc width, so maybe the next "if" test is not needed 
					final IGTNodeRenderInfo lastNodeRenderInfo = lastNode.getRenderInfo();
					final int lastNodeRhs = lastNodeRenderInfo.x + lastNodeRenderInfo.calcWidth;
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer(String.format("X: updateLastChildSize: rhs=%d lastNodeRhs=%d .x=%d .calcWidth=%d, .realWidth=%d",
								rhs, lastNodeRhs, lastNodeRenderInfo.x, lastNodeRenderInfo.calcWidth, lastNodeRenderInfo.realWidth));
					}
					if (lastNodeRhs < rhs) { // was < 
						lastNodeRenderInfo.calcWidth = rhs - lastNodeRenderInfo.x;

						// update the last children of this last node (so recursive)
						if (LOG.isLoggable(Level.FINER)) {
							LOG.finer(String.format("X: updateLastChildSize: .calcWidth:=%d",
									lastNodeRenderInfo.calcWidth));
						}
						
						IGTCalculator.updateLastChildSize(lastNode);
					}
				}
			}
		}
	}
	
	/**
	 * Calculates the wrapping info for this tier given the width.
	 * 
	 * @param g2d the Graphics context
	 * @param curTier the tier to wrap
	 * @param width the available width
	 */
	public static void calculateWrappingInfo(Graphics g2d, IGTViewerRenderInfo viewerRenderInfo, IGTTier curTier, int width) {
		if (curTier == null || width == 0) {
			return;
		}
		
		// Can we be called with subtiers of WORD_LEVEL_ROOT?
		// Those should probably be left alone, being processed already by their WLROOT.
		if (curTier.isInWordLevelBlock()
			//curTier.getType() == IGTTierType.SUBDIVISION ||
			//curTier.getType() == IGTTierType.WORD_LEVEL_ROOT
				) {
			IGTCalculator.calculateBlockWrapping(curTier, width);
		} else //if (curTier.getType() == IGTTierType.ROOT ||
				//   curTier.getType() == IGTTierType.FIRST_LEVEL_ASSOCIATION || 
				//   curTier.getType() == IGTTierType.ASSOCIATION || 
				//   curTier.getType() == IGTTierType.TIME_CODE)
		{ // TODO check if already processed and/or make this test more fine grained
			List<IGTAnnotation> anns = curTier.getAnnotations();
			if (anns.size() > 0) {
				IGTAnnotation ann = curTier.getAnnotations().get(0);
				FontMetrics fm = g2d.getFontMetrics(viewerRenderInfo.getFontForTier(curTier.getTierName()));
				//calculateLineWrapping(g2d, fm, ann, width);
				IGTCalculator.calculateLineWrappingSimple(g2d, fm, ann, width);
			}
		}
	}
	
	/**
	 * Calculates the wrapping information for a single line of text.
	 * The approach in this method is to just start at the beginning add words to test until a 
	 * wrapping position is found.
	 * 
	 * @param g2d the graphics context
	 * @param fm the font metrics
	 * @param ann the annotation to calculate wrapping for
	 * @param width the maximum width for a line 
	 */
//	private static void calculateLineWrappingSimpleOld(Graphics g2d, FontMetrics fm, IGTAnnotation ann, int width) {
//		if (ann == null || ann.getTextValue() == null || ann.getTextValue().isEmpty()) {
//			return;
//		}
//		
//		final IGTNodeRenderInfo annRenderInfo = ann.getRenderInfo();
//		
//		if (annRenderInfo.realWidth > width) {
//			annRenderInfo.clearWrappedLines();
//			String total = ann.getTextValue().replace("\n", " ");// text value can't be null here
//			int[] indices = getWordEndIndices(total);
//			
//			if (indices.length == 0) {
//				annRenderInfo.setNumLines(1);
//				ann.getIGTTier().getRenderInfo().setNumLines(1);
//			} else {
//				int bIndex = 0;
//				int eIndex = 0;
//				
//				//String substring = null;
//				String prevSubstring = null;
//				//int subWidth;
//				int numLines = 0;
//				
//				for (int i = 0; i < indices.length; i++) {
//					eIndex = indices[i];
//					String substring = total.substring(bIndex, eIndex);
//					int subWidth = fm.stringWidth(substring);
//					
//					if (subWidth > width) {// crossed the boundary, use previous substring
//						if (i == 0) {
//							annRenderInfo.addWrappedLine(substring);
//							numLines++;
//							bIndex = eIndex + 1;
//						} else if (i == indices.length - 1) {
//							if (prevSubstring != null) {
//								annRenderInfo.addWrappedLine(prevSubstring);
//								numLines++;
//							}
//							bIndex = indices[i - 1] + 1;
//							substring = total.substring(bIndex);
//							subWidth = fm.stringWidth(substring);
//							if (subWidth < width) {
//								annRenderInfo.addWrappedLine(substring);
//								numLines++;
//							} else {// add two lines
//								annRenderInfo.addWrappedLine(total.substring(bIndex, eIndex));
//								numLines++;
//								annRenderInfo.addWrappedLine(total.substring(eIndex + 1));
//								numLines++;
//							}
//							break;
//						} else {// default
//							if (prevSubstring != null) {
//								annRenderInfo.addWrappedLine(prevSubstring);
//								numLines++;
//								bIndex = indices[i - 1] + 1;
//							} else {
//								annRenderInfo.addWrappedLine(substring);
//								numLines++;
//								bIndex = eIndex + 1;
//							}							
//						}
//						
//					} else {// else continue with the same begin index
//						if (i == indices.length - 1) {// last index
//							prevSubstring = substring;
//							substring = total.substring(bIndex);// try the rest of the string
//							subWidth = fm.stringWidth(substring);
//							if (subWidth < width) {
//								annRenderInfo.addWrappedLine(substring);
//								numLines++;
//							} else {
//								annRenderInfo.addWrappedLine(prevSubstring);
//								numLines++;
//								annRenderInfo.addWrappedLine(total.substring(eIndex + 1));
//								numLines++;
//							}
//						} else {
//							prevSubstring = substring;
//							continue;
//						}						
//					}
//					prevSubstring = null;
//				}
//				
//				annRenderInfo.calcWidth = width;
//				annRenderInfo.setNumLines(numLines);
//				ann.getIGTTier().getRenderInfo().setNumLines(numLines);
//			}
//		} else {
//			annRenderInfo.setNumLines(1);
//			annRenderInfo.clearWrappedLines();
//			ann.getIGTTier().getRenderInfo().setNumLines(1);
//		}
//	}
	
	/**
	 * Calculates the wrapping information for a single line of text.
	 * The approach in this method is to just start at the beginning add words to test until a 
	 * wrapping position is found.
	 * 
	 * @param g2d the graphics context
	 * @param fm the font metrics
	 * @param ann the annotation to calculate wrapping for
	 * @param width the maximum width for a line 
	 */
	private static void calculateLineWrappingSimple(Graphics g2d, FontMetrics fm, IGTAnnotation ann, int width) {
		if (ann == null || ann.getTextValue() == null || ann.getTextValue().isEmpty()) {
			return;
		}
		
		int numLines = 1;
		final IGTNodeRenderInfo annRenderInfo = ann.getRenderInfo();
		annRenderInfo.clearWrappedLines();
		
		if (annRenderInfo.realWidth > width) {
			String total = ann.getTextValue().replace("\n", " ");// text value can't be null here
			int[] indices = getWordEndIndices(total);
			
			if (indices.length > 0) {
				int bIndex = 0;
				String prevSubstring = null;
				int prevEIndex = -1;
				
				for (int i = 0; i < indices.length; i++) {
					int eIndex = indices[i];
					String substring = total.substring(bIndex, eIndex);
					int subWidth = annotationWidth(fm, substring);
					
					if (subWidth > width) {
						// The collected substring is too wide:
						// use the previous substring (which still fit).
						if (prevSubstring == null) {
							// Oops! There is no previous substring. That must mean that even the
							// single word is already too wide. We'll have to use it anyway.
							annRenderInfo.addWrappedLine(substring);
							bIndex = eIndex + 1;
							// Continue just after this word, with the next end position.
						} else {
							annRenderInfo.addWrappedLine(prevSubstring);
							bIndex = prevEIndex + 1;
							prevSubstring = null;
							i--; // Compensate for i++: look at the same end position again.
						}
					} else {
						// substring fit: make it longer, i.e. continue with the same begin index.
						prevSubstring = substring;
						prevEIndex = eIndex;
					}
				}
				
				// Add the final part which was not too wide.
				if (prevSubstring != null) {
					annRenderInfo.addWrappedLine(prevSubstring);
				}
				
				annRenderInfo.calcWidth = width;
				numLines = annRenderInfo.getWrappedLines().size();
			}
		} else if (annRenderInfo.calcWidth > width) {
			// Lines that don't wrap but are still too wide due to children
			// (which themselves get wrapped and therefore fit within width).
			annRenderInfo.calcWidth = width;
		}
		
		annRenderInfo.setNumLines(numLines);
		ann.getIGTTier().getRenderInfo().setNumLines(numLines);
	}
	
	/**
	 * Calculates the annotation indices at which wrapping should occur. The index of the annotation is 
	 * that of the first annotation that should move to a new line.
	 * Individual words are currently not wrapped.
	 * 
	 * @param tier the tier which is parent on the level of subdivision, e.g. on the word level
	 * 
	 * @param width the width of display area
	 */
	public static void calculateBlockWrapping(IGTTier tier, int width) {
		// The check should maybe be "inWordLevelBlock()" ?
		if (tier != null &&
				tier.isInWordLevelBlock()
				/*was (tier.getType() == IGTTierType.SUBDIVISION || tier.getType() == IGTTierType.WORD_LEVEL_ROOT)*/) {
			tier.getRenderInfo().clearWrapIndices();

			//int numBlocks = 1;
			int xShift = 0;
			
			//boolean firstOnLine = true;
			
			final int numAnnotations = tier.getAnnotations().size();
			for (int i = 0; i < numAnnotations; i++ ) {
				IGTAnnotation ann = tier.getAnnotations().get(i);
				int calcW = ann.getRenderInfo().calcWidth;
				
				// add a special check for the last annotation?
				// this block might need to be removed.
				// Is it doing something useful? It finds the largest realWidth of its children,
				// but is that a useful value? It should perhaps sum the widths or something?
				if (i == numAnnotations - 1) {
					// find the largest realWidth of this annotation and its (in)direct children.
					int realW = ann.getRenderInfo().realWidth;
					List<IGTNode> children = ann.getChildren();
					
					if (children != null && !children.isEmpty()) {
						int size = children.size();
						
						for (int j = 0; j < size; j++) {
							IGTAnnotation ca = (IGTAnnotation) children.get(j);
							realW = Math.max(realW, ca.getRenderInfo().realWidth);
						}
					}
					if (LOG.isLoggable(Level.FINER) && realW != calcW) {
						LOG.finer(String.format("X: i=%d calcW := realW: %d <- %d",
								i, calcW, realW));
					}
					// For the last annotation, which may be artificially widened,
					// consider it to fit on the line as long as the real width fits.
					calcW = realW;
					//testje: ann.getRenderInfo().calcWidth = width - ann.getRenderInfo().x - xShift;
				}
				
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("X: i=%d: %d + %d + %d = %d >= %d?",
							i,
							ann.getRenderInfo().x, calcW, xShift,
							ann.getRenderInfo().x + calcW + xShift,
							width));
				}

				// If the position + the width of the annotation doesn't fit
				// in the available space, wrap it.
				// However, don't do that for the first one, since we would have an empty first line!
				// After the first one, it doesn't matter since we only consider each annotation once.
				if (i > 0 && ann.getRenderInfo().x + calcW + xShift > width) {
					// wrap: put this one on the next line.
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer(String.format("break before wrapIndex %d",
								i));
					}
					tier.getRenderInfo().addWrapIndex(i);
					xShift = -ann.getRenderInfo().x;
					//firstOnLine = true; // actually, for the next one.
				} else {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer(String.format("X: no break before wrapIndex %d",
								i));
						//firstOnLine = false; // actually, for the next one.
					}
				}

				// update annotation and its children
				if (xShift != 0) {
					shiftAnnotationAndChildren(ann, xShift);
				}
			}
		}
	}
	
	/**
	 * I'm not sure is this function is really needed, but as it is,
	 * if a group of tiers is wrapped, only the root of it gets
	 * updated with the wrap index and the numLines.
	 * The children do get the yPositions somewhere.
	 * <p>
	 * The big question is how to find the wrap indexes on child tiers.
	 * Not every annotation on this root tier has children, or the same number.
	 * <p>
	 * Maybe:<ul>
	 *  <li>find the first sibling of this wrapped annotation that has a child
	 *      (can be ann itself)     
	 *  <li>that child will then be the first wrapped child of the subtier
	 *  <li>so find which index it has on that tier
	 *  <li>and that is the wrapIndex.
	 *  </ul>
	 * Rather a lot of work.
	 * 
	 * @param tier
	 * @param wrapIndex
	 */
	
	@SuppressWarnings("unused")
	private static void wrapTierAndChildren(IGTTier tier, int wrapIndex) {
		if (wrapIndex >= 0) {
			tier.getRenderInfo().addWrapIndex(wrapIndex);
		}
		// Not sure if the rest, including the recursion is needed,
		// but it seems dirty if only some tiers have this info.
		
		tier.getRenderInfo().setNumLines(tier.getRenderInfo().getNumLines() + 1); // should probably not be here but in the Y calculation, if needed at all

		List<IGTTier> children = tier.getChildTiers();
		if (children != null && children.size() > 0) {
			for (IGTTier childTier : children) {
				/// TODO what should be the wrapIndex here ????
				wrapTierAndChildren(childTier, -1);
			}
		}
	}
	
	/**
	 * Shift an annotation and all its children;
	 * usually the shift is to the left (negative xShift)
	 * due to a wrapping operation.
	 * 
	 * @param ann
	 * @param xShift
	 */
	private static void shiftAnnotationAndChildren(IGTNode ann, int xShift) {
		ann.getRenderInfo().x += xShift;
		List<IGTNode> children = ann.getChildren();
		
		if (children != null && !children.isEmpty()) {
			for (IGTNode child : children) {
				shiftAnnotationAndChildren(child, xShift);
			}
		}
	}

	/**
	 * Returns an array of indices of whitespace characters in a string.
	 * Adds an extra index for the end of the string.
	 * 
	 * @param s the string
	 * @return the array, if there are no whitespace characters the array is empty
	 */
	static int[] getWordEndIndices(String s) {
		if (s == null) {
			return new int[]{};
		}
		
		List<Integer> inds = new ArrayList<Integer>();
		final int length = s.length();
		
		for (int i = 0; i < length; i++) {
			if (Character.isWhitespace(s.charAt(i))) {
				inds.add(i);
			}
		}
		inds.add(length);
		
		if (inds.size() > 0) {
			int[] indices = new int[inds.size()];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = inds.get(i);
			}
			return indices;
		} else {
			return new int[]{};
		}
	}
	
	/**
	 * Calculates the shortened label for a the tiers in the model.
	 * 
	 * @param g2d the graphics context for the rendering
	 * @param dModel the data model for one block of annotations
	 * @param availWidth the available width for the labels. Margins have to be taken into account
	 */
	public static void calculateShortTierLabels(Graphics g2d, IGTDataModel dModel, int availWidth) {
		if (dModel == null || availWidth <= 0) {// the width can have a greater minimum
			return;// silently return or throw exception?
		}
		FontMetrics fm = g2d.getFontMetrics();
		int netWidth = availWidth - dModel.getRowHeaderRenderInfo().getHorizontalMargins();
		
		for (int i = 0; i < dModel.getRowCount(); i++) {
			IGTTier igtTier = dModel.getRowData(i);
			
			int tierWidth = netWidth - igtTier.getLevel() * igtTier.getRenderInfo().indentPerLevel;
			int totalWidth = fm.stringWidth(igtTier.getTierName());
			
			if (totalWidth >= tierWidth) {
				String shortName = igtTier.getTierName().substring(0, igtTier.getTierName().length() - 1);
				while (fm.stringWidth(shortName) > tierWidth && shortName.length() > 1) {
					shortName = shortName.substring(0, shortName.length() - 1);
				}
				
				dModel.setShortTierNameForIndex(i, shortName);
			} else {
				// set the short width to ?
				dModel.setShortTierNameForIndex(i, igtTier.getTierName());
			}
		}
	}
	
	/**
	 * Calculates the minimally required width to render the tier names of a suggestion
	 * taking into account margins and (optionally) indentation.
	 * @param g2d the graphics context
	 * @param sugModel the suggestion data model
	 * @param renderInfo the suggestion rendering information
	 * @return the minimal width required to render all tier names
	 */
	public static int calcSuggestionRowHeaderWidth(Graphics g2d, IGTSuggestionModel sugModel, 
			IGTSuggestionViewerRenderInfo renderInfo) {
		int leftMargin = 0;
		int rightMargin = 0;
		if (renderInfo.getTextInsets() != null) {
			leftMargin = renderInfo.getTextInsets().left;
			rightMargin = renderInfo.getTextInsets().right;
		}
		FontMetrics fm = g2d.getFontMetrics(renderInfo.getHeaderFont());
		int minWidth = 0;
		for (int i = 0; i < sugModel.getRowCount(); i++) {
			IGTTier igtTier = sugModel.getRowData(i);
			if (igtTier.getType() == IGTTierType.ROOT) {
				continue;
			}
			int tierNameWidth = fm.stringWidth(igtTier.getTierName()) + leftMargin + rightMargin;
			if (renderInfo.visualizeIndentation) {
				tierNameWidth += (igtTier.getLevel() * renderInfo.indentPerLevel);
			}
			if (tierNameWidth > minWidth) {
				minWidth = tierNameWidth;
			}
		}
		
		return minWidth;
	}
	
	/**
	 * Updates part of an igt group starting with a node that has to be updated and then
	 * propagating changes to descending annotations and then up the tree again.
	 * <p>
	 * <b>TODO</b> make sure annotations are not shifted out of the view. This can happen as
	 *      we type into the IGTGroupEditor.editBox.
	 * 
	 * @param igtAnn the annotation from which the update starts
	 * @param calcWidth new calculated width for the node
	 * @param availWidth try to keep annotations within this visible width
	 * 
	 * @return true if it seems some annotation shifted out of view
	 */
	public static boolean updateLASRecursiveFromNode(IGTAnnotation igtAnn, int calcWidth, int availWidth) {
		if (igtAnn == null) {
			return false;
		}
		int widthDiff = calcWidth - igtAnn.getRenderInfo().calcWidth;
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer(String.format("X: updateLASRecursiveFromNode: %d",
					widthDiff));
		}
		// if widthDiff > 0 the node needs more space, subsequent annotations are 
		// shifted to the right, 
		// TODO the widthDiff < 0, following annotations are shifted left, is not implemented yet.  
		// A check on the real width should be included in that case.
//		if (widthDiff <= 0) {
//			return false;
//		}
		igtAnn.getRenderInfo().calcWidth = calcWidth;
		// or move up first??
		updateLastChildSize(igtAnn);
		// find the parent that is on the "word level" tier 
		IGTTier wlTier = null;
		IGTAnnotation curAnn = igtAnn;
		IGTTier curTier = curAnn.getIGTTier();
		
		if (curTier.isInWordLevelBlock()) {
			IGTTier parentTier;
			while (true) {
				parentTier = curTier.getParentTier();
				if (parentTier == null || !parentTier.isInWordLevelBlock()) {
					wlTier = curTier;
					break;
				} else {
					curTier = parentTier;
				}
			}
		}
		
		if (wlTier != null && wlTier != igtAnn.getIGTTier()) {
			IGTAnnotation parentAnn = wlTier.getAnnotationAtPoint(new Point(curAnn.getRenderInfo().x, 
					wlTier.getRenderInfo().y));
			if (parentAnn != null) {// it shouldn't be, otherwise the way to get to the ancestor should be changed 
				parentAnn.getRenderInfo().calcWidth += widthDiff;
				curAnn = parentAnn;
			}
		}
		
		// shift next annotations
		boolean shiftedOutOfView = false;
		IGTAnnotation nextAnn = curAnn.getIGTTier().getNextAnnotation(curAnn);
		while (nextAnn != null) {
			shiftAnnotationAndChildren(nextAnn, widthDiff);
			shiftedOutOfView = shiftedOutOfView || 
					(nextAnn.getRenderInfo().x +  
					 nextAnn.getRenderInfo().calcWidth > availWidth);
			
			nextAnn = curAnn.getIGTTier().getNextAnnotation(nextAnn);
		}

		// update top level tiers and 
		IGTTier topTier = igtAnn.getIGTTier();
		while (topTier != null) {
			if (topTier.getParentTier() != null) {
				topTier = topTier.getParentTier();
			} else {
				break;
			}
		}
		
		if (topTier != null && topTier != igtAnn.getIGTTier()) {
			nextAnn = topTier.getAnnotations().get(0);
			if (nextAnn != null) {
				nextAnn.getRenderInfo().calcWidth += widthDiff;
				updateLastChildSize(nextAnn);
			}
		}
		
		return shiftedOutOfView;
	}
}
