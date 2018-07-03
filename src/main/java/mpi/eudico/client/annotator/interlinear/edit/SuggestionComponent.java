package mpi.eudico.client.annotator.interlinear.edit;

/**
 * Implements a JComponent that displays a single Suggestion.
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.DebugGraphics;
import javax.swing.JComponent;
import javax.swing.RepaintManager;

import nl.mpi.lexan.analyzers.helpers.Suggestion;
import nl.mpi.lexan.analyzers.helpers.SuggestionSet;
import nl.mpi.lexan.analyzers.lexicon.LexAtom;
import nl.mpi.lexan.analyzers.lexicon.LexCont;
import nl.mpi.lexan.analyzers.lexicon.LexEntry;
import nl.mpi.lexan.analyzers.lexicon.LexItem;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionViewerModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTTier;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTBlockRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTNodeRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTSuggestionRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTViewerRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.SuggestionFlowLayout;

@SuppressWarnings("serial")
public class SuggestionComponent extends JComponent
		implements SuggestionFlowLayout.Location, MouseMotionListener {
	private int sugIndex;
	/* the data model for one suggestion */
	private IGTSuggestionModel sugModel;
	private IGTSuggestionRenderInfo sugRenderInfo;
	private IGTViewerRenderInfo viewerRenderInfo;
	private IGTSuggestionViewerModel svModel;
	/* an index in the list of suggestions of the top level tier in the suggestion set,
	 * i.e. an index in the horizontal layout of the suggestions */
	private int fragNr;
	final static int NO_FRAG = -99999;
	/* html string elements for formatted tool tip text*/
	private final static String htmlTTTableOpen = "<html><table>";
	private final static String htmlTTTableClose = "</table></html>";
	private final static String htmlTTRow = "<tr><td>%s</td><td>%s</td></tr>";
	/* in incremental selection mode only part of the suggestion set is rendered */
	private int incrementalModeLevel = -1;
	
	/**
	 * Constructor
	 * @param sugIndex the index of the suggestion this component is displaying
	 * @param viewerRenderInfo rendering info of the viewer
	 * @param svModel the viewer's data model containing the suggestions
	 */
	public SuggestionComponent(int sugIndex, IGTViewerRenderInfo viewerRenderInfo, IGTSuggestionViewerModel svModel) {
		this.sugIndex = sugIndex;
		this.viewerRenderInfo = viewerRenderInfo;
		this.svModel = svModel;
		this.sugModel = svModel.getRowData(sugIndex);
		sugRenderInfo = (IGTSuggestionRenderInfo) sugModel.getRenderInfo();
		this.fragNr = NO_FRAG;
		// initially only the first suggestion shows the tier labels, see setGrid
		sugModel.showTierLabels(sugIndex == 0);
		/*
		if (false) {
			RepaintManager repaintManager = RepaintManager.currentManager(this);
			repaintManager.setDoubleBufferingEnabled(false);
			setDebugGraphicsOptions(DebugGraphics.BUFFERED_OPTION | DebugGraphics.FLASH_OPTION
			        | DebugGraphics.LOG_OPTION);
			setOpaque(false);	 // TESTING: where is all the flashing coming from??
		}
		*/
		//setDebugGraphicsOptions(DebugGraphics.FLASH_OPTION);
		// set silly default size
		this.sugModel.getRenderInfo().height = 75;
		this.sugModel.getRenderInfo().width = 120;
		
		// ToolTips interfere with the MouseListener on the Container^2
		// (the SuggestionWindow). mousePressed() and mouseClicked() don't
		// get called any more when it occurs inside this component...
		// For a workaround, see workAroundToolTipBug().
		String label = sugModel.getSuggestionSet().getLabel();
		if (label != null) {
			setToolTipText(label);
		}
		
		addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0 &&
						e.getChanged() == SuggestionComponent.this) {
	        		calculateLocationAndSize();
	        		removeHierarchyListener(this);
				}
			}
		});
		addMouseMotionListener(this);
	}
	
	/**
	 * Calculates the size and location of the items in the model.
	 * This needs to be delayed until this component is actually add()ed
	 * to a parent, because it uses getGraphics().
	 * 
	 * @param sugModel the suggestion model
	 */
	private void calculateLocationAndSize() {
		Graphics g = getGraphics();
		if (g == null) {
			return;	// leave size (if calculated before) alone
		}

		int h, w;

		final int tierRowCount = sugModel.getRowCount();

		if (tierRowCount > 0) {
			IGTTier rootTier = sugModel.getRootRow();
			
			// Calculate horizontal layout
			w = IGTCalculator.calculateTierAndAnnotationsLAS(g, viewerRenderInfo, 
					rootTier, true);
			
			final IGTBlockRenderInfo sugRenderInfo = sugModel.getRenderInfo();
			sugRenderInfo.width = w;

			// If the tier labels are included in this one, reflect it in the width
			if (sugModel.showTierLabels()) {
				sugRenderInfo.width += svModel.getRenderInfo().rowHeaderWidth;
			}

			/* The Y calculation uses the "natural" height of each line, as determined by
			 * the font size. It includes the ROOT tier which takes up space but is not
			 * actually drawn. In its place is the header, of height 
			 * svModel.getRenderInfo().headerHeight +1 (for the line of the rectangle).
			 * By manipulating the starting Y, we can position the first real tier
			 * just right at the cost of having a silly Y position for the ROOT tier.
			 * 
			 * Additionally, we could set .headerHeight to a value related to the
			 * font size used to draw the header text. Since that is global for the viewer,
			 * all suggestions should use the same font for that (which is reasonable).
			 */
			h = svModel.getRenderInfo().getColumnHeaderHeight(g) -
				viewerRenderInfo.getHeightForTier(g, rootTier.getTierName());
			h += viewerRenderInfo.vertLineMargin;
			h += IGTCalculator.calculateTierYPositionRecursive(g, viewerRenderInfo, rootTier, h);
			
			h += viewerRenderInfo.vertLineMargin;
			sugRenderInfo.height = h;	
		}
	}

	/**
	 * Sets the temporarily "selected" fragment (suggestion) of the set,
	 * used for highlighting/color-coding sets roughly based on same text values 
	 * in that position.
	 *  
	 * @param fragNr the newly selected fragment
	 * @see #getFragOfX(int, boolean)
	 * @see #getHashOfFrag(int)
	 * @see #getRectOfFrag(int)
	 */
	public void setFragNr(int fragNr) {
		if (this.fragNr != fragNr) {
			this.fragNr = fragNr;
			if (this.fragNr > NO_FRAG) {
				int hash = getHashOfFrag(fragNr);
				// Use the hash to fill in 6 of the the lower 7 bits of each colour component,
				// keeping the high bit on and the low bit off, so that we have a fairly light colour
				// which results in better readability of the black text.
				// 0b1xxxxxx0
				int colour = (int)(((long)hash * 31) % 0x03FFFF);
				int cr = 0x80 | ((colour >> 11) & 0x7E);
				int cg = 0x80 | ((colour >>  5) & 0x7E);
				int cb = 0x80 | ((colour <<  1) & 0x7E);
				sugRenderInfo.setHighlightBGColor(new Color(cr, cg, cb));				
			} else {
				sugRenderInfo.setHighlightBGColor(null);
			}
			repaint();
		}
	}
	
	/**
	 * @return the currently selected/highlighted fragment
	 * @see #getFragOfX(int, boolean)
	 * @see #getHashOfFrag(int)
	 * @see #getRectOfFrag(int)
	 */
	public int getFragNr() {
		return fragNr;
	}
	
	/**
	 * 
	 * @param level sets the level of incremental selection, this corresponds to the fragment 
	 * index, counted from the left (zero based)
	 */
	public void setIncrementalModeLevel(int level) {
		incrementalModeLevel = level;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if (incrementalModeLevel > -1) {
			paintComponentIM(g);
			return;
		}
		
		IGTSuggestionRenderer.renderOneSuggestion(g, sugIndex, 
				viewerRenderInfo, svModel);
		
		if (fragNr > NO_FRAG) {
			// Draw a rectangle around the part that determines the colour.
			Rectangle rect = getRectOfFrag(fragNr);
			
			if (rect != null) {
				int height = sugRenderInfo.height;
				int rx = rect.x;
				int ww = rect.width;
				if (fragNr != 0) {
					rx -= 1;
					ww += 2;
				} else {
					ww += 1;
				}
				// count for row 1 because there is an "invisible" root row
				if (fragNr == sugModel.getColumnCountForRow(1) - 1) {
					ww -= 2;
				}
				
				g.setColor(Color.BLUE /*new Color(cr ^ 0x80, cg ^ 0x80, cb ^ 0x80)*/);
				g.drawRect(rx, rect.y - 1, ww, height - rect.y - 2); 
			}
		}
	}
	
	/**
	 * An alternative way to render a suggestion while in incremental mode. 
	 * This might be a temporary workaround until a dedicated component 
	 * and rendering strategy has been implemented for this way of selecting. 
	 * @param g the graphics context
	 */
	private void paintComponentIM(Graphics g) {
		IGTSuggestionRenderer.renderOneSuggestion(g, sugIndex, 
				incrementalModeLevel, viewerRenderInfo, svModel);
		
		Rectangle rect = getRectOfFrag(incrementalModeLevel);
		
		if (rect != null) {
			int height = sugModel.getRenderInfo().height;
			int rx = rect.x;
			int ww = rect.width;
			if (incrementalModeLevel != 0) {
				rx -= 1;
				ww += 2;
			} else {
				ww += 1;
			}
			// count for row 1 because there is an "invisible" root row
			if (incrementalModeLevel == sugModel.getColumnCountForRow(1) - 1) {
				ww -= 2;
			}
			
			g.setColor(Color.BLUE /*new Color(cr ^ 0x80, cg ^ 0x80, cb ^ 0x80)*/);
			g.drawRect(rx, rect.y - 1, ww, height - rect.y - 2); 
			// cover the fragments right of this fragment
			int cvx = rect.x + rect.width + 2;
			int cvw = sugModel.getRenderInfo().width - cvx;
			Color col = svModel.getRenderInfo().blockBackGround;
			if (fragNr > NO_FRAG) {
				if (((IGTSuggestionRenderInfo) sugModel.getRenderInfo()).getHighlightBGColor() != null) { 
					col = ((IGTSuggestionRenderInfo) sugModel.getRenderInfo()).getHighlightBGColor();
				}
			}
			g.setColor(col);
			g.fillRect(cvx, rect.y - 2, cvw, height - rect.y - 1);
		}
	}

	/**
	 * Get the IGTAnnotation that represents the given fragment number.
	 * Negative numbers count from the right:
	 * -1 is the rightmost fragment.
	 * 
	 * @param fragNr in domain [-size, size)
	 * @return the IGTAnnotation, or null if the fragNr is not in the domain.
	 */
	private IGTAnnotation getFrag(int fragNr) {
		IGTSuggestionModel sugg = svModel.getRowData(sugIndex);
		IGTTier firstTier = sugg.getRowData(1);
		final ArrayList<IGTAnnotation> annotations = firstTier.getAnnotations();
		
		final int size = annotations.size();
		if (fragNr < 0) {
			fragNr = fragNr + size;
		}
		if (fragNr >= 0 && fragNr < size) {
			return annotations.get(fragNr);
		}
		
		return null;
	}
	
	/**
	 * @return The hash of the given fragment number. The hash is calculated based on all
	 * constituents of that index (parent suggestion plus possible children)
	 */
	public int getHashOfFrag(int fragNr) {
		IGTAnnotation firstPart = getFrag(fragNr);
		if (firstPart != null) {
			return firstPart.hashCodeOfText();
		}
		
		return 0;
	}
	
	/**
	 * @param fragNr
	 * @return the Rectangle that the given fragment number represents.
	 */
	public Rectangle getRectOfFrag(int fragNr) {
		IGTAnnotation firstPart = getFrag(fragNr);
		if (firstPart != null) {
			IGTNodeRenderInfo ri = firstPart.getRenderInfo();
			int offx = sugModel.showTierLabels() ? svModel.getRenderInfo().rowHeaderWidth : 0;
			
			return new Rectangle(offx + ri.x, ri.y, ri.calcWidth, ri.height);
		}
		
		return null;
	}
	
	/**
	 * Given an X coordinate, return which in which fragment of the suggestion this is.
	 * <p>
	 * If <code>fromRight</code> is true, count the fragments from the right,
	 * where the rightmost fragment is number -1.
	 * 
	 * Otherwise, count from the left and the leftmost fragment is number 0.
	 * 
	 * If no fragment falls at the given location, return NO_FRAG.
	 * <p>
	 * This makes the range [-size, size) U { NO_FRAG } where size is the total number of fragments.
	 * @param x the x coordinate in rendering space
	 * @param fromRight if true the counting is from right to left, otherwise from left to right
	 * 
	 * @see #getHashOfFrag(int)
	 * @see #getRectOfFrag(int)
	 */
	public int getFragOfX(int x, boolean fromRight) {
		IGTSuggestionModel sugg = svModel.getRowData(sugIndex);
		IGTTier firstTier = sugg.getRowData(1);
		final ArrayList<IGTAnnotation> annotations = firstTier.getAnnotations();
		int offx = sugModel.showTierLabels() ? svModel.getRenderInfo().rowHeaderWidth : 0;
		x -= offx;
	
		int fragNr = -1;
		for (IGTAnnotation ann : annotations) {
			IGTNodeRenderInfo ri = ann.getRenderInfo();
			if (ri.x > x) {
				break;
			}
			fragNr++;
		}
		
		if (fragNr < 0) {
			fragNr = NO_FRAG;
		} else if (fromRight) {
			fragNr = fragNr - annotations.size();
		}
		
		return fragNr;
	}
	
	private boolean inColumnHeader(int yPos) {
		return yPos < svModel.renderInfo.getColumnHeaderHeight() + svModel.renderInfo.suggestionMargin;
	}
	
	@Override
	public Dimension getPreferredSize() {
		final IGTBlockRenderInfo renderInfo = sugModel.getRenderInfo();

		return new Dimension(renderInfo.width, renderInfo.height);
	}
	
	/**
	 * This suggestion knows its index in the list of suggestions.
	 * Return that number.
	 */
	public int getIndex() {
		return sugIndex;
	}

	/**
	 * Callback from the SuggestionFlowLayout to notify the component of its
	 * location in the layout.
	 */
	@Override // SuggestionFlowLayout.Location
	public void setGrid(int col, int row) {
		final IGTBlockRenderInfo sugRenderInfo = sugModel.getRenderInfo();

		//System.err.printf("SuggestionComponent: setGrid row=%d col=%d => ", row, col);
		if (col == 0) {
			if (!sugModel.showTierLabels()) {
				sugRenderInfo.width += svModel.getRenderInfo().rowHeaderWidth;
				sugModel.showTierLabels(true);
			}
		} else {
			if (sugModel.showTierLabels()) {
				sugRenderInfo.width -= svModel.getRenderInfo().rowHeaderWidth;
				sugModel.showTierLabels(false);
			}
		}
		//System.err.printf("%d x %d\n", renderInfo.width, renderInfo.height);
		setSize(sugRenderInfo.width, sugRenderInfo.height);
	}

	/**
	 * Get the preferred size of this component, given its location
	 * in the layout.
	 */
	@Override // SuggestionFlowLayout.Location
	public Dimension getPreferredSize(int col, int row) {
		final IGTBlockRenderInfo sugRenderInfo = sugModel.getRenderInfo();
		int width = sugRenderInfo.width;
		
		//System.err.printf("SuggestionComponent: getPreferredSize row=%d col=%d => ", row, col);
		if (col == 0) {
			if (!sugModel.showTierLabels()) {
				width += svModel.getRenderInfo().rowHeaderWidth;
			}
		} else {
			if (sugModel.showTierLabels()) {
				width -= svModel.getRenderInfo().rowHeaderWidth;
			}
		}

		//System.err.printf("%d x %d\n", width, renderInfo.height);
		return new Dimension(width, sugRenderInfo.height);
	}
	
	@Override
	public void setPreferredSize(Dimension dim) {
		// do not call super.setPreferredSize(dim);
		// but this isn't called anyway, so unneeded.
	}

	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (!visible) {
			// reset some rendering related variables
			setFragNr(NO_FRAG);
			incrementalModeLevel = -1;
		}
	}


	/**
	 * Shorthand helper interface name that implements both 
	 * MouseListener and MouseMotionListener.
	 * <p>
	 * Unfortunately the implementer needs to implement THIS interface;
	 * just implementing the two Listeners is not sufficient.
	 */
	public interface MouseAndMotionListener extends MouseListener, MouseMotionListener {};  

	/**
	 * This is needed because a ToolTip installs a MouseListener and
	 * therefore eats the mouse events that our parent containers may
	 * want to see (in particular the SuggestionWindow).
	 * So we work around this by installing extra mouse listeners.
	 */
	public void workAroundToolTipBug(MouseAndMotionListener mml) {
		addMouseListener(mml);
		addMouseMotionListener(mml);
	}
	
	public void removeWorkAroundToolTipBug(MouseAndMotionListener mml) {
		removeMouseListener(mml);
		removeMouseMotionListener(mml);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.isShiftDown() || e.isAltDown()) {
			int x = e.getX();
			int frag = getFragOfX(x, e.isAltDown());
			
			//System.err.printf("SuggestionComponent:mouseMoved: x = %d, frag = %d\n", x, frag);
			if (frag != this.fragNr) {
				// Change the fragment number on all suggestions in
				// the SuggestionWindow, which happen to be our siblings (and us).
				// Probably, this should be done in a cleaner manner, since this method
				// assumes all suggestions are in the same container parent.
				Container parent = getParent();

				for (Component sibling : parent.getComponents()) {
					if (sibling instanceof SuggestionComponent) {
						SuggestionComponent sc = (SuggestionComponent)sibling;
						sc.setFragNr(frag);
					}
				}
			}
		} else if (inColumnHeader(e.getY())) {
			String label = sugModel.getSuggestionSet().getLabel();
			if (label != null) {
				setToolTipText(label);
			}
		} else {
			int frag = getFragOfX(e.getX(), e.isAltDown());
			// don't show tool tips for hidden fragments in incremental mode
			if (incrementalModeLevel > -1 && frag > incrementalModeLevel) {
				setToolTipText(null);
				return;
			}
			
			if (frag > -1) {
				IGTSuggestionModel curModel = svModel.getRowData(sugIndex);
				SuggestionSet suggset = curModel.getSuggestionSet();
				if (frag < suggset.getSuggestions().size()) {
					Suggestion s = suggset.getSuggestions().get(frag);
					
					if (s.getLexEntry() != null) {
						this.setToolTipText(getToolTipText(s.getLexEntry()));
					} else {
						setToolTipText(s.getContent());
					}
				}
			} else {
				setToolTipText(null);
			}
		}
	}

	/**
	 * Creates a tool tip text for the entry as a html table.
	 * The id of the LexEntry could be used to show all information of the entry as it 
	 * is in the lexicon?
	 * 
	 * @param entry the LEXAN entry
	 * @return the formatted tool tip text
	 */
	private String getToolTipText(LexEntry entry) {
		if (entry != null) {
			StringBuilder sb = new StringBuilder(htmlTTTableOpen);
			for (int i = 0; i < entry.getLexItems().size(); i++) {
				LexItem lex = entry.getLexItems().get(i);
				if (lex instanceof LexAtom) {
					sb.append(String.format(htmlTTRow, lex.getType(), ((LexAtom) lex).getLexValue()));
				} else if (lex instanceof LexCont) {
					LexCont lc = (LexCont) lex;
					if (lc.getLexItems() != null) {
						for (LexItem li : lc.getLexItems()) {
							sb.append(String.format(htmlTTRow, li.getType(), ((LexAtom) li).getLexValue()));
						}
					}
				}
				
			}
			sb.append(htmlTTTableClose);
			return sb.toString();		
		}
		
		return null;
	}
	/** for setting a breakpoint */
//	@Override
//	public void update(Graphics g) {
//		super.update(g);
//	}
}
