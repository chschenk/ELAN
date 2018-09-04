package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.gui.InlineEditBox;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTEditAction;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelListener;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSelectionListener;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAbstractDataModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTDefaultModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionViewerModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTTier;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTNodeRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTTierRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTViewerRenderInfo;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import nl.mpi.lexan.analyzers.helpers.Position;

/**
 * A class to edit a group of annotations, in an interlinear glossed text style.
 * 
 * Why does this not use the IGTGroupRenderer?
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class IGTGroupEditor extends JComponent
	implements MouseListener, KeyListener, IGTDataModelListener, ClientLogger {
	private final static Color EDIT_BG_COLOR = new Color(200, 200, 255);
	
	private IGTEditProvider editProvider;
	private IGTAbstractDataModel model;
	private IGTViewerRenderInfo vrInfo;
	
	private IGTAnnotation selectedAnn = null;
    // editing, temporary?
    private InlineEditBox editBox;
    private boolean forceOpenControlledVocabulary = false;
	private AbstractViewer viewer;
	private boolean deselectCommits = true;

	private WeakReference<SuggestionWindow> weakSuggestionWindow;
    	
	/**
	 * Constructor.
	 */
	public IGTGroupEditor(IGTEditProvider editProvider, AbstractViewer viewer) {
		super();
		this.editProvider = editProvider;
		this.viewer = viewer;
		this.setLayout(null);

		editBox = new InlineEditBox(true);
        editBox.addKeyListener(this);
        
        editBox.setFont(getFont());
        editBox.setVisible(false);
        editBox.setBorder(null);
        editBox.setMargin(new Insets(0, 0, 0, 0));
        this.add(editBox);
		setBackground(EDIT_BG_COLOR);
		addMouseListener(this);
		setFocusable(true);
		setOpaque(false);	// don't erase to background when repainting (but alas, doesn't work)
	}

	/**
	 * Sets the content for the editor, the data of one row in the viewer model.
	 * A row in the model consists of a root annotation and its dependent annotations.
	 * 
	 * @param rowModel the row data model
	 * @param viewerRenderInfo the rendering properties of the viewer level
	 * @param xOff a horizontal offset in the coordinate space of editor component 
	 * @param yOff a vertical offset in the coordinate space of editor component
	 * @param verticalScrollValue a vertical offset of the editor as a whole (negative)
	 */
	public void editIGTGroup(IGTDataModel rowModel, IGTViewerRenderInfo viewerRenderInfo) {
		if (deselectCommits) {
			commitEdit();
		} else {
			cancelEdit();
		}

		this.model = (IGTAbstractDataModel) rowModel;// safe cast
		this.vrInfo = viewerRenderInfo;
		
		if (this.model != null) {
			this.model.addIGTDataModelListener(this);
		}

		repaint();
	}
	
	public void commitEdit() {
		if (this.model != null) {
			this.model.removeIGTDataModelListener(this);
			if (editBox.isVisible()) {
				editBox.commitEdit();
			}
		}
	}
	
	public void cancelEdit() {
		if (this.model != null) {
			this.model.removeIGTDataModelListener(this);
			if (editBox.isVisible()) {
				editBox.cancelEdit();
			}
		}
	}

	/**
	 * Sets whether deselecting the edit box commits or discards changes. This is a user preference
	 * and the viewer listens to changes in preferences.
	 * 
	 * @param deselectCommits the commit on deselect flag
	 */
	public void setDeselectCommits(boolean deselectCommits) {
		this.deselectCommits = deselectCommits;
	}

	/**
	 * The editor is informed of the delivery of suggestion sets by the IGTViewer.
	 * It will cause a window to open, hopefully just below the annotations to be replaced.
	 * <p>
	 * When a suggestion is chosen, the listener is notified.
	 * 
	 * @param svModel the viewer model containing the suggestions
	 * @param pos the position of the source annotation, i.e. the tier name and time interval
	 * identifying the source annotation
	 * @param listener a listener that wants to be informed of selection of a suggestion (the host context) 
	 */
	public void suggestionSetDelivered(IGTSuggestionViewerModel svModel, Position pos, SuggestionSelectionListener listener) {
		if (pos != null) {
			int row = model.getRowIndexForTier(pos.getTierId());
			if (row > -1) {
				IGTTier igtTier = model.getRowData(row);
				IGTAnnotation igtAnn = null;
				boolean found = false;
				final ArrayList<IGTAnnotation> annotations = igtTier.getAnnotations();
				final int size = annotations.size();
				for (int i = 0; i < size; i++) {
					igtAnn = annotations.get(i);
					// Find an IGTAnnotation which matches the source of the suggestions,
					// so it seems we should show it there.
					final AbstractAnnotation annotation = igtAnn.getAnnotation();
					if (annotation != null) {
						final long beginTime = pos.getBeginTime();
						if (LOG.isLoggable(Level.FINER)) {
							LOG.finer(String.format("find matching ann: %d-%d in %d-%d?",
								beginTime, pos.getEndTime(),
								annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary()));
						}
						if (annotation.getBeginTimeBoundary() <= beginTime &&
							annotation.getEndTimeBoundary()   >  beginTime) {
							found = true;
							break;
						}
					}
					
				}

				int annY = 0;
				int annX = 0;
				int annCalcWidth = 0;
				int annHeight = 0;

				if (igtAnn == null || !found) {
					// Try to align with the label stating the name of the tier;
					// TODO since not x correct yet, and which yPosition should we take for wrapped tiers?
					final IGTTierRenderInfo tierRenderInfo = igtTier.getRenderInfo();
					annY = tierRenderInfo.y;
					annX = 0; // vrInfo.headerWidth; //  tierRenderInfo.x;
					annCalcWidth = tierRenderInfo.width; // is always 0
					annHeight = tierRenderInfo.height;
				} else {
					final IGTNodeRenderInfo annRenderInfo = igtAnn.getRenderInfo();
					annY = annRenderInfo.y;
					annX = annRenderInfo.x;
					annCalcWidth = annRenderInfo.calcWidth;
					annHeight = annRenderInfo.height;
				}
				
				// found, show the suggestions below the annotation
				// XXX If there are multiple rows in the suggestion, it places the window suitably
				// for the first rows, but subsequent rows would now be covered...
				SuggestionSetSelector selector = new SuggestionSetSelector(svModel, vrInfo);
				
				SuggestionWindow window = new SuggestionWindow(SwingUtilities.getWindowAncestor(this), selector);
				window.setSuggestionSelectionListener(listener);
				//window.setSize(300, 100);
				//window.setSize(window.getPreferredSize()); // not much better: hasn't calculated the size of the contents yet
				// not setting the size keeps it at 1x1 which is almost invisible,
				// which is nicer than seeing a resizing window.
				window.setVisible(true);

				try {
					Point gePos = this.getLocationOnScreen();
					Dimension windowSize = window.getPreferredSize();
					// Using windowSize is useless since it is a fixed size now and will
					// get resized to a better size later.
					// Using preferredSize is not much better since it doesn't know it yet and is defaulted.
					window.setLocation(gePos.x + vrInfo.headerWidth + annX + (annCalcWidth / 2) - (windowSize.width / 2), 
							gePos.y + annY + annHeight + 2 * vrInfo.vertLineMargin);
					// calculate max available width and height based on location on screen
					Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
					Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
					Point windowLocation = window.getLocation();
					int availableWidthOnScreen = screenDim.width - screenInsets.right - windowLocation.x;
					int availableHeightOnScreen = screenDim.height - screenInsets.bottom - windowLocation.y;
					window.setMaximumSize(new Dimension(availableWidthOnScreen, availableHeightOnScreen));
					// even here the actual preferred dimension hasn't been calculated yet. This is done in the window's
					// componentShown method, which is only called after this method has returned.
					// Could call it (or more likely some other init. method) here and then relocate the window
					// if more space is required and more space is available above the source annotation
				} catch (IllegalComponentStateException ex) {
					// Potential problem: "component must be showing on the screen to determine its location"
					ex.printStackTrace();
				}
				
				// Swing must keep the (strong) reference to the displayed window.
				// After all displayed windows don't just disappear...
				weakSuggestionWindow = new WeakReference<SuggestionWindow>(window);
			}
		}
	}

	/**
	 * For when the suggestion window needs to be closed programmatically,
	 * we have a weak reference to it. It is weak so that having the reference
	 * does not hold on to the window's resources when it is closed by the user.
	 * On the other hand, when the window closes, the weak reference gets
	 * cleared more "eagerly" than a soft reference.
	 */
	public void cancelSuggestionSet() {
		if (weakSuggestionWindow != null) {
			SuggestionWindow window = weakSuggestionWindow.get();

			if (window != null) {
				window.setVisible(false);
				window.dispose();
				weakSuggestionWindow.clear();
			}
			
			weakSuggestionWindow = null;
		}
	}

	/**
	 * Get the currently active (selected) AbstractAnnotation.
	 */
	public AbstractAnnotation getActiveAnnotation() {
		if (selectedAnn == null) {
			return null;
		} else {
			return selectedAnn.getAnnotation();
		}
	}

	/**
	 * The outside notifies us that the active annotation has changed.
	 * Update out internal state and drawing.
	 * We must not notify back.
	 * 
	 * @param ann
	 */
	public void updateActiveAnnotation(Annotation activeAnnotation) {
		if (model == null) {
			return;
		}
		if (activeAnnotation == null) {
			internalSetSelectedAnnotation((IGTAnnotation)null);
		} else {
			String tierName = activeAnnotation.getTier().getName();
			IGTTier tier = model.getRowDataForTier(tierName);
			
			if (tier != null) {
				for (IGTAnnotation ann : tier.getAnnotations()) {
					if (ann.getAnnotation() == activeAnnotation) {
						internalSetSelectedAnnotation(ann);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Opens the edit box for the specified annotation or the first editable 
	 * annotation found.
	 * 
	 * @param annotationToEdit the annotation to edit or null
	 */
	public void startEditAnnotation(Annotation annotationToEdit) {
		if (model == null) {
			return;
		}
		
		if (selectedAnn != null && (
				selectedAnn.getAnnotation() == annotationToEdit || annotationToEdit == null)) {
			textEditAnnotation(selectedAnn);
		} else if (annotationToEdit != null) {
			updateActiveAnnotation(annotationToEdit);// only sets the "active annotation" internally
			textEditAnnotation(selectedAnn);			
		} else {
			// find first editable annotation
			for (int i = 0; i < model.getRowCount(); i++) {
				IGTTier t = model.getRowData(i);
				if (!t.isSpecial() && !t.getAnnotations().isEmpty()) {
					IGTAnnotation annToEdit = t.getAnnotations().get(0);
					// set the active annotation
					internalSetSelectedAnnotationAndNotify(annToEdit);
					textEditAnnotation(selectedAnn);
					break;
				}
			}
		}
	}

	/**
	 * Update the internal idea of what the currently active annotation is,
	 * including repainting both the old and the new one.
	 * <p>
	 * Note: Should we prevent null? AbstractViewer.setActiveAnnotation() does that too.
	 * 
	 * @param newSelectedAnn
	 */
	private void internalSetSelectedAnnotation(IGTAnnotation newSelectedAnn) {
		IGTAnnotation prev = selectedAnn;
		selectedAnn = newSelectedAnn;
		repaint(selectedAnn);
		repaint(prev);
	}
	
	/**
	 * Call this when a user action changes the active annotation.
	 * Because the user did it, we should let the rest of ELAN know about it.
	 * 
	 * @param newSelectedAnn
	 */
	private void internalSetSelectedAnnotationAndNotify(IGTAnnotation newSelectedAnn) {
		internalSetSelectedAnnotation(newSelectedAnn);
		notifyActiveAnnotation();
	}
	
	/**
	 * Notify the outside that the active annotation has changed.
	 * It also sets the selection to the annotation's time period.
	 * <p>
	 * It looks at the current active annotation, so call this after updating that.
	 * 
	 * @param newSelectedAnn
	 */
	private void notifyActiveAnnotation() {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Notify the outside that the active annotation and selection have changed");
		}
		if (viewer != null) {
			Annotation ann = getActiveAnnotation();
			viewer.setActiveAnnotation(ann);
			// The above sets the selection to the nearest parent annotation that is aligned.
			// Here set it to the boundaries of the annotation itself.
			viewer.setSelection(ann);
		}
	}

	/**
	 * Try to be efficient about repainting, and indicate to Swing that only
	 * this one annotation needs to be repainted.
	 * 
	 * @param ann
	 */
	private void repaint(IGTAnnotation ann) {
		if (ann != null) {
			IGTNodeRenderInfo annRender = ann.getRenderInfo();
			repaint(vrInfo.headerWidth + annRender.x, annRender.y, 
					annRender.calcWidth, annRender.height);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Color bg = EDIT_BG_COLOR;
		
		g.setColor(bg);
		g.fillRect(0, 0, width, height);

		IGTGroupRenderer.renderRow2(g, model, vrInfo, height, selectedAnn);
	}
	
	/**
	 * Finds the annotation at the given location in the editor's coordinate space.
	 *  
	 * @param p the location (in the editor's coordinate: top/left of this row is (0,0))
	 * @return the IGTAnnotation or null
	 */
	private IGTAnnotation getAnnotationAtPoint(Point p) {
		IGTTier igtTier = getTierAtY(p.y);
		if (igtTier != null) {
			return igtTier.getAnnotationAtPoint(p);
		}
		
		return null;
	}
	
	private IGTTier getTierAtY(int y) {
		if (model != null) {
			final int rowCount = model.getRowCount();

			for (int i = 0; i < rowCount; i++) {
				IGTTier igtTier = model.getRowData(i);

				if (igtTier.isAtY(y)) { //isAtY should be a quick test whether the tier has a line at the y location
					return igtTier;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * This is used when clicking in empty space.
	 * Since there is no annotation there, one cannot directly say which annotation
	 * "should have been" there. But one can find a parent tier with an annotation
	 * at the same horizontal location.
	 */
	private IGTAnnotation getParentAnnotationOfPoint(Point p, boolean grandParentsToo) {
		IGTTier igtTier = getTierAtY(p.y);
		
		while (igtTier != null) {
			IGTTier parentTier = igtTier.getParentTier();
			
			if (parentTier != null) {
				int y = parentTier.getY(p.y);

				p.y = y;

				IGTAnnotation parentAnn = parentTier.getAnnotationAtPoint(p);
				
				if (parentAnn != null || !grandParentsToo) {
					return parentAnn;
				}
			}
			igtTier = parentTier;
		}
		
		return null;
	}
	
	/**
	 * Starts editing the specified annotation, currently by showing a popup menu.
	 * 
	 * @param igtAnn the annotation to edit
	 */
	private void editAnnotation(IGTAnnotation igtAnn) {
		if (igtAnn == null) {
			return;
		}
		if (igtAnn.getIGTTier().isSpecial()) {
			return;
		}
		
		List<IGTEditAction> actions = editProvider.actionsForAnnotation(igtAnn);
		
		if (actions != null) {
			JPopupMenu popup = new JPopupMenu();

			for (IGTEditAction igtAction : actions) {
				popup.add(igtAction);
			}

			popup.show(this, igtAnn.getRenderInfo().x + vrInfo.headerWidth, igtAnn.getRenderInfo().y + igtAnn.getRenderInfo().height);
			
			repaint();
		}
	}
	
	private void editEmptySpace(IGTAnnotation parent, int x, int y) {
		if (parent == null) {
			return;
		}
		
		List<IGTEditAction> actions = editProvider.actionsForEmptySpace(parent);
		
		if (actions != null) {
			JPopupMenu popup = new JPopupMenu();

			for (IGTEditAction igtAction : actions) {
				popup.add(igtAction);
			}

			popup.show(this, x, y);
			
			repaint();
		}
	}

	/**
	 * Starts editing the specified annotation in a text edit component.
	 * 
	 * @param igtAnn the annotation to text edit
	 */
	private void textEditAnnotation(IGTAnnotation igtAnn) {
		if (igtAnn == null) {
			return;
		}
		if (igtAnn.getIGTTier().isSpecial()) {
			return;
		}
		
        editBox.setAnnotation(igtAnn.getAnnotation(),
                forceOpenControlledVocabulary);

        editBox.setFont(vrInfo.getFontForTier(igtAnn.getIGTTier().getTierName()));
       	final IGTNodeRenderInfo renderInfo = igtAnn.getRenderInfo();
       	int x = vrInfo.headerWidth + renderInfo.x;
       	int y = renderInfo.y;
       	int w = Math.max(50, renderInfo.calcWidth + 2);
       	int h = renderInfo.height;

       	// Box too wide to fit?
		final int availWidth = getWidth() - 1; // a 1-pixel of the background green remains on top of the outer blue line
		if (x + w > availWidth) {
			w = Math.max(50, availWidth - x);
			x = availWidth - w;
		}

       	Dimension dim = new Dimension(w, h); 
		editBox.setLocation(x, y);
        editBox.configureEditor(JPanel.class, null, dim);
        // call the override minimum version of set size
        editBox.setSizeIgnoreMinimum(dim);
        editBox.setVisible(true);
        editBox.startEdit();

        forceOpenControlledVocabulary = false;
        
        if (LOG.isLoggable(Level.FINER)) {
        	LOG.finer("Ann: x=" + (renderInfo.x + vrInfo.headerWidth) + " y=" + renderInfo.y 
        		+ " w=" + (renderInfo.calcWidth + 2) + " h=" + renderInfo.height);
        	LOG.finer("TF: " + editBox.getBounds());
        }
	}
	
	/**
	 * Searches the current group for the next editable (and therefore visible) annotation.
	 * By default the search is left to right, top to bottom, but this 
	 * might need to be customizable. 
	 * 
	 * @param igtAnn the current annotation, the annotation from which the search starts.
	 */
	private void selectEditNextAnnotation(IGTAnnotation igtAnn) {
		if (igtAnn == null) {
			return;
		}
		IGTAnnotation nextAnn = igtAnn.getIGTTier().getNextAnnotation(igtAnn);
		if (nextAnn != null) {
			internalSetSelectedAnnotationAndNotify(nextAnn);
			textEditAnnotation(nextAnn);
		} else {
			// find next visible tier
			int curRow = model.getRowIndexForTier(igtAnn.getIGTTier().getTierName());
			// loop over following tiers/rows depending on the tab direction
			int nextRow = curRow + 1;
			IGTTier nextTier;
			
			while (true) {
				if (nextRow == model.getRowCount()) {
					 nextRow = 0;
				}
				
				if (nextRow == curRow) {
					// select the first annotation unless it is the same as the current annotation
					nextAnn = igtAnn.getIGTTier().getAnnotations().get(0);// should always be safe
					break;
				}
				nextTier = model.getRowData(nextRow);
				
				if (nextTier.isSpecial()) {
					nextRow++;
					continue;
				}
				if (nextTier.getAnnotations().isEmpty()) {
					nextRow++;
				} else {
					nextAnn = nextTier.getAnnotations().get(0);
					break;
				}
			}
			
			if (nextAnn == null || nextAnn == igtAnn) {
				internalSetSelectedAnnotationAndNotify(null);
			} else {
				internalSetSelectedAnnotationAndNotify(nextAnn);
				textEditAnnotation(nextAnn);
			}
		}
	}

	/**
	 * Searches the current group for the previous editable (and therefore visible) annotation.
	 * By default the search for the previous annotation is right to left, bottom to top (but this 
	 * might need to be customizable). 
	 * 
	 * @param igtAnn the current annotation, the annotation from which the search starts.
	 */
	private void selectEditPreviousAnnotation(IGTAnnotation igtAnn) {
		if (igtAnn == null) {
			return;
		}
		IGTAnnotation prevAnn = igtAnn.getIGTTier().getPreviousAnnotation(igtAnn);
		if (prevAnn != null) {
			internalSetSelectedAnnotationAndNotify(prevAnn);
			textEditAnnotation(prevAnn);
		} else {
			// find previous visible tier
			int curRow = model.getRowIndexForTier(igtAnn.getIGTTier().getTierName());
			// loop over preceding tiers/rows depending on the shift+tab direction
			int prevRow = curRow - 1;
			IGTTier nextTier;
			
			while (true) {
				if (prevRow < 0) {
					 prevRow = model.getRowCount() - 1;
				}
				
				if (prevRow == curRow) {
					// select the last annotation unless it is the same as the current annotation
					int numAnn = igtAnn.getIGTTier().getAnnotations().size();
					if (numAnn > 1) {
						prevAnn = igtAnn.getIGTTier().getAnnotations().get(numAnn - 1);
					}
					break;
				}
				nextTier = model.getRowData(prevRow);
				
				if (nextTier.isSpecial()) {
					prevRow--;
					continue;
				}
				if (nextTier.getAnnotations().isEmpty()) {
					prevRow--;
				} else {
					int numAnn = nextTier.getAnnotations().size();
					prevAnn = nextTier.getAnnotations().get(numAnn - 1);
					break;
				}
			}
			
			if (prevAnn == null || prevAnn == igtAnn) {
				internalSetSelectedAnnotationAndNotify(null);
			} else {
				internalSetSelectedAnnotationAndNotify(prevAnn);
				textEditAnnotation(prevAnn);
			}
		}
	}
	/**
	 * Indirect mouse clicked event, called when a row is activated, starts editing the annotation
	 * at that point.
	 * 
	 * @param x the x position (inside this row)
	 * @param y the y position (inside this row)
	 * @param nclicks 
	 */
	void clickedAt(int x, int y, int nclicks) {
		Point p = new Point(x - vrInfo.headerWidth, y);

		if (nclicks == 1) {
			IGTAnnotation actAnnotation = getAnnotationAtPoint(p);
			// Only change the active annotation if you click in one.
			// Clicking outside doesn't do anything in this regard.
			// (Should this be some kind of preference?)
			if (actAnnotation != null) {
				internalSetSelectedAnnotationAndNotify(actAnnotation);
				textEditAnnotation(selectedAnn);
			} else {
				// dismiss the edit box, if there is one
				if (editBox.isVisible()) {
					if (deselectCommits) {
						editBox.commitEdit();
					} else {
						editBox.cancelEdit();
					}
				} else {
					internalSetSelectedAnnotationAndNotify(null);
				}
			}
		}
	}
	
	/**
	 * Indirect mouse pressed event, called when a row is activated,
	 * using the menu button.
	 * 
	 * @param x the x position (inside this row)
	 * @param y the y position (inside this row)
	 */
	void pressedAt(int x, int y) {
		if (editBox.isVisible()) {
			if (deselectCommits) {
				editBox.commitEdit();
			} else {
				editBox.cancelEdit();
			}
		}
		
		Point p = new Point(x - vrInfo.headerWidth, y);
		IGTAnnotation actAnnotation = getAnnotationAtPoint(p);
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer(String.format("pressedAt x=%d y=%d ann=%s",
					x, y, String.valueOf(actAnnotation)));
		}
		
		internalSetSelectedAnnotationAndNotify(actAnnotation);
	
		if (selectedAnn != null) { 
			editAnnotation(selectedAnn);
		} else {
			IGTAnnotation parentAnnotation = getParentAnnotationOfPoint(p, true);
			editEmptySpace(parentAnnotation, x, y);
		}
	}
	

	@Override // IGTDataModelListener
	public void dataModelChanged(IGTDataModelEvent event) {
		repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent me) {
        if ((SwingUtilities.isRightMouseButton(me) && (me.getButton() == MouseEvent.BUTTON1 ^ me.isMetaDown())) 
                || me.isPopupTrigger()) {
        	return;
        }
		int nx = me.getX();
		int ny = me.getY();
		int nclicks = me.getClickCount();
		clickedAt(nx, ny, nclicks);	// nx, ny relative to top left of this row

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// stub
		
	}

	@Override
	public void mousePressed(MouseEvent me) {
        if ((SwingUtilities.isRightMouseButton(me) && (me.getButton() == MouseEvent.BUTTON1 ^ me.isMetaDown())) 
                || me.isPopupTrigger()) {
    		int nx = me.getX();
    		int ny = me.getY();
    		pressedAt(nx, ny);	//nx, ny relative to top left of this row
        }
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// stub
		
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		/**
		 * For now assumes the key event came from the text edit box.
		 */
		if (ke.getKeyCode() == KeyEvent.VK_TAB) {
			editBox.commitEdit();
			if (ke.isShiftDown()) {
				selectEditPreviousAnnotation(selectedAnn);
			} else {
				selectEditNextAnnotation(selectedAnn);
			}
			ke.consume();
		}
		
	}

	@Override
	public void keyReleased(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_TAB) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Tab release: consuming event");
			}
			ke.consume();
		}
		
		if (selectedAnn == null) {
			return;
		}
		//if (ke.getSource() == editBox) {
			int curWidth = editBox.getWidth();
			int textWidth = editBox.getCurrentTextWidth();
			int availWidth = getWidth() - vrInfo.headerWidth;
			//textWidth += 0;// correct for some margin space, turn into a constant
			if (textWidth != curWidth && textWidth > selectedAnn.getRenderInfo().calcWidth) {
				Dimension dim = editBox.getSize();
				dim.width = textWidth;
				if (editBox.getX() + dim.width < getWidth()) {
					editBox.setSizeIgnoreMinimum(dim);
					editBox.revalidate();
					// update the calc width of the annotation block and positions of annotations to the right?
					boolean hasToWrap = IGTCalculator.updateLASRecursiveFromNode(selectedAnn, textWidth, availWidth);
					if (false && hasToWrap && this.model instanceof IGTDefaultModel) {
						AbstractAnnotation aa = selectedAnn.getAnnotation();
						/* This eventually calls IGTViewer.calculateHeightForRow(...) which does pretty much
						 * what we want, EXCEPT that it re-calculates all the widths from the annotations.
						 * And since the annotation isn't changed yet at this point (only in the editor),
						 * this doesn't help us.
						 * This might be fixed by completely separating the size calculations from the positioning.
						 * But that is a lot of work.
						 * It would mostly come automatically with making every annotation into a Component,
						 * and making some LayoutManager, which is also a lot of work.
						 * I tried hacking some omissions of recalculating but it happened anyway.
						 */
						((IGTDefaultModel)this.model).annotationValueChanged(aa);
					}
				}
				// check position of edit box and size of the editor as a whole
				
				repaint();
			}
		//}
	}

	/**
	 * Resizing upon a key typed work well because the typed character hasn't been added
	 * to the text document yet.
	 */
	@Override
	public void keyTyped(KeyEvent ke) {
		//System.out.println("Key typed: " + editBox.getCurrentText());
	}

}
