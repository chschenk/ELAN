package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.JComponent;

import mpi.eudico.client.annotator.interlinear.edit.SuggestionComponent.MouseAndMotionListener;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionViewerModel;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTViewerRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.SuggestionFlowLayout;

/**
 * A component that visualizes suggestion sets and allows to select one of the suggestions.
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class SuggestionSetSelector extends JComponent {
	private IGTSuggestionViewerModel viewerModel;
	private IGTViewerRenderInfo viewerRenderInfo;
	
	private int selectorWidth;
	private int selectorHeight;
	private boolean reversedOrder = false;
		
	/**
	 * Constructor accepting suggestions sets in a viewer model as parameter.
	 * 
	 * @param viewerModel the model containing the suggestion sets encapsulated in a igt model
	 * @param viewerRenderInfo contains information for the renderer
	 */
	public SuggestionSetSelector(IGTSuggestionViewerModel viewerModel, IGTViewerRenderInfo viewerRenderInfo) {
		super();
		this.viewerModel = viewerModel;
		this.viewerRenderInfo = viewerRenderInfo;
		
//		initComponents();
	}
	
	/**
	 * Call init component after the component is visible, otherwise somehow it
	 * doesn't know its contents properly... huh?
	 * <p>
	 * @see #initComponents()
	 */
	public void init() {
		initComponents();
	}

	/**
	 * Calculates the total height and width of all suggestion sets.
	 */
	private void initComponents() {
		if (viewerModel != null) {
			selectorWidth = viewerRenderInfo.headerWidth;
			selectorHeight = 0;
			
			final int margin = viewerModel.getRenderInfo().suggestionMargin;
			setLayout(new SuggestionFlowLayout(FlowLayout.LEFT, margin, margin));
			
			//setDebugGraphicsOptions(DebugGraphics.FLASH_OPTION);
			//setOpaque(false);	 // TESTING: where is all the flashing coming from??
			
			final int rowCount = viewerModel.getRowCount();

			for (int i = 0; i < rowCount; i++) {
				SuggestionComponent comp = new SuggestionComponent(i, viewerRenderInfo, viewerModel);
				add(comp);
			}
			validate();
						
		} else {
			selectorWidth = 150;
			selectorHeight = 100;
			setSize(selectorWidth, selectorHeight);
			setPreferredSize(new Dimension(selectorWidth, selectorHeight));
		}
	}

	/**
	 * Returns the suggestion viewer model.
	 * 
	 * @return the suggestion viewer model
	 */
	public IGTSuggestionViewerModel getModel() {
		return viewerModel;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		LayoutManager layout = getLayout();
		if (layout instanceof SuggestionFlowLayout) {
			return ((SuggestionFlowLayout)layout).getSize();
		}
		if (selectorHeight == 0 || selectorWidth == 0) {
			return new Dimension(260, 80);
		}
		
		return new Dimension(selectorWidth, selectorHeight);
	}
	
	/**
	 * Get the height of a single SuggestionComponent
	 * plus the vertical spacing.
	 * Hopefully they are all the same height.
	 */
	public int getUnitHeight() {
		if (getComponentCount() > 0) {
			return getComponent(0).getHeight() + viewerModel.getRenderInfo().suggestionMargin;
		}
		
		return 80;	// some random default
	}
	
	/**
	 * Returns the index of the model displayed at the given location.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the index of the model, or -1 if none
	 */
	public int getSuggestionIndexAtPoint(int x, int y) {
		Component comp = getComponentAt(x, y);
		// Does this handle the JScollPane correctly???
		
		if (comp instanceof SuggestionComponent) {
			SuggestionComponent sugg = (SuggestionComponent)comp;
			
			return sugg.getIndex();
		}
		return -1;
	}
	
	/**
	 * 
	 * @param visibleIndex the position in the list of visible components
	 * @return the SuggestionComponent at the specified index
	 * @see #getVisibleSuggestionCount()
	 * 
	 * @throws IndexOutOfBoundsException if visibleIndex < 0 or >= visible count
	 */
	public SuggestionComponent getSuggestionAtVisibleIndex(int visibleIndex) {
		if (visibleIndex < 0) {
			throw new IndexOutOfBoundsException(String.format("There is no visible suggestion at that index: %d (< 0)", visibleIndex));
		}
		
		int index = 0;
		for (Component c : getComponents()) {
			if (c.isVisible() && c instanceof SuggestionComponent) {
				if (index == visibleIndex) {
					return (SuggestionComponent) c;
				}
				index++;
			}
		}
		
		if (visibleIndex >= index) {
			throw new IndexOutOfBoundsException(String.format("There is no visible suggestion at that index: %d (>= %d)", visibleIndex, index ));
		}
		
		return null;
	}
	
	public void workAroundTooltipBug(MouseAndMotionListener mml) {
		Component[] children = getComponents();
		
		for (Component c : children) {
			if (c instanceof SuggestionComponent) {
				SuggestionComponent sugg = (SuggestionComponent)c;
				
				sugg.workAroundToolTipBug(mml);
			}
		}
	}
	
	public void removeWorkAroundToolTipBug(MouseAndMotionListener mml) {
		Component[] children = getComponents();
		
		for (Component c : children) {
			if (c instanceof SuggestionComponent) {
				SuggestionComponent sugg = (SuggestionComponent)c;
				
				sugg.removeWorkAroundToolTipBug(mml);
			}
		}
	}
	
	/**
	 * Reverses the order of the suggestion components as they are in the
	 * current layout.
	 */
	public void reverseOrderOfSuggestions() {
		if (viewerModel != null) {
			Component[] curComponents = this.getComponents();
			this.removeAll();
			
			for (int i = curComponents.length - 1; i >= 0; i--) {
				add(curComponents[i]);
			}
			reversedOrder = !reversedOrder;
			revalidate();
		}
	}

	/**
	 * 
	 * @return true if the order of the suggestion sets has been reversed 
	 */
	public boolean isReversedOrder() {
		return reversedOrder;
	}
	
	/**
	 * Making suggestions visible or invisible can be done (is done) 
	 * in other classes.
	 * 
	 * @return the number of SuggestionComponents that are visible
	 */
	public int getVisibleSuggestionCount() {
		int numVisible = 0;
		
		for (Component c : getComponents()) {
			if (c.isVisible() && c instanceof SuggestionComponent) {
				numVisible++;
			}
		}
		
		return numVisible;
	}
	
	/**
	 * 
	 * @param visibleIndex the index in the list of visible suggestions
	 * @return the index of the suggestion in the suggestion model
	 * 
	 * @see #getVisibleSuggestionCount()
	 * @see #convertModelIndexToVisible(int)
	 * @throws IndexOutOfBoundsException if visibleIndex is >= visibleSuggestionCount
	 */
	public int convertVisibleIndexToModel(int visibleIndex) {
		if (visibleIndex < 0) {
			throw new IndexOutOfBoundsException(String.format("There is no visible suggestion at that index: %d (< 0)", visibleIndex));
		}
		
		int index = 0;
		for (Component c : getComponents()) {
			if (c.isVisible() && c instanceof SuggestionComponent) {
				if (index == visibleIndex) {
					return ((SuggestionComponent) c).getIndex();
				}
				index++;
			}
		}
		
		if (visibleIndex >= index) {
			throw new IndexOutOfBoundsException(String.format("There is no visible suggestion at that index: %d (>= %d)", visibleIndex, index ));
		}
		// can't happen
		return -1;
	}
	
	/**
	 * Converts the index of a suggestion in the model to the index in the list of visible suggestions.
	 * 
	 * @param modelIndex the index of the suggestion in the viewer model
	 * @return the index in the list of visible suggestions (0-based) or -1 if not visible
	 * 
	 * @see #convertVisibleIndexToModel(int)
	 * @throws IndexOutOfBoundsException if modelIndex < 0 or >= model.getRowCount()
	 */
	public int convertModelIndexToVisible(int modelIndex) {
		if (modelIndex < 0 || modelIndex >= viewerModel.getRowCount()) {
			throw new IndexOutOfBoundsException(String.format(
					"There is no row in the model at that index: %d (valid 0-%d)", modelIndex, viewerModel.getRowCount() - 1));
		}
		
		int index = 0;
		for (Component c : getComponents()) {
			if (c.isVisible() && c instanceof SuggestionComponent) {
				if (((SuggestionComponent) c).getIndex() == modelIndex) {
					return index;
				}
				index++;
			}
		}
		
		return -1;
	}
	
	/** for setting a breakpoint */
//	@Override
//	public void update(Graphics g) {
//		super.update(g);
//	}
}
