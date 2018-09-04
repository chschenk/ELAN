package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FontSizer;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.gui.ShowHideMoreTiersDlg;
import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTAddToLexiconAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTCreateDependentAnnotationsAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTDeleteAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTEditAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTInterlinearizeAction;
import mpi.eudico.client.annotator.interlinear.edit.actions.IGTSplitAnnotationAction;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTViewerChangeListener;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTViewerModelEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTViewerModelListener;
import mpi.eudico.client.annotator.interlinear.edit.event.ModelEventType;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSelectionEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSetEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSetListener;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSetProvider;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTDefaultModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionViewerModel;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTTier;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTSuggestionRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTViewerRenderInfo;
import mpi.eudico.client.annotator.util.AnnotationCoreComparator;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.util.TimeInterval;
import mpi.eudico.util.TimeRelation;
import nl.mpi.lexan.analyzers.helpers.Position;
import nl.mpi.lexan.analyzers.helpers.SuggestionSet;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * The IGTViewer shows a vertical row of IGTGroups to the user.
 * <p>
 * A group is an annotation on a root tier together with all/any child
 * annotations on depending tiers.
 * The groups are placed in begin time order.
 * <p>
 * The groups are placed in a JTable so that we don't have to bother
 * with scrolling, or finding out in which group the user clicked,
 * and that sort of things. It also handles placing the IGTGroupEditor.
 * <p>
 * Each group is rendered by an IGTGroupRenderer.
 * <p>
 * The data to show is contained in a IGTViewerModel, and rendering details
 * such as coordinates are in a IGTViewerRenderInfo.
 * 
 * <pre>
 * +-IGTViewer-------------------------------------------+
 * |+-ScrollPane----------------------------------------+|
 * ||+-JTable---------------------------------------+ ^ ||
 * |||+-IGTGroup-----------------------------------+| ^ ||
 * ||||                                            || ^ ||
 * ||||                                            || . ||
 * |||+--------------------------------------------+| . ||
 * |||+-IGTGroup-----------------------------------+| . ||
 * ||||                                            || . ||
 * ||||                                            || . ||
 * |||+--------------------------------------------+| v ||
 * |||...                                           | v ||
 * ||+----------------------------------------------+ v ||
 * |+---------------------------------------------------+|
 * +-----------------------------------------------------+
 * </pre>
 */
@SuppressWarnings("serial")
public class IGTViewer extends JComponent
                       implements MouseListener, MouseMotionListener,
				                  ComponentListener,
				                  IGTEditProvider, IGTViewerModelListener,
				                  SuggestionSetListener, FontSizer {

	/**
	 * A local class to adapt the IGTGroupRenderer to be used in a JTable.
	 * 
	 * A similar class existed to create a TableModel from IGTViewerModelImpl
	 * but full functionality required it to be integrated into the
	 * IGTViewerModelImpl itself.
	 * 
	 * @author olasei
	 */
	private class GroupTableCellRenderer implements TableCellRenderer {
		private IGTDataModel rowData;
		private int row;
		private boolean isSelected;
		private Annotation activeAnnotation;
		private IGTAnnotation activeIGTAnnotation = null;
		
		JComponent component = new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				final int thisHeight = getHeight();
				final int thisWidth = getWidth();

				Color bg = (row % 2 == 0) ? viewerRenderInfo.backgroundColor
		                                  : viewerRenderInfo.backgroundColor2;
				if (isSelected) {
					// when editing stopped (e.g. by the ESCAPE key) the row is still selected
				//	bg = bg.darker();
					bg = new Color(bg.getRed(), bg.getGreen(), (int)(bg.getBlue() * 0.7));
				}
				// clear the rectangle for this row
				g.setColor(bg);
				g.fillRect(0, 0, thisWidth, thisHeight);

				IGTGroupRenderer.renderRow2(g, rowData, viewerRenderInfo,
						thisHeight, activeIGTAnnotation);
			}
		};

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			this.row = row;
			this.rowData = (IGTDataModel) value; // same as model.getRowData(row);
			this.isSelected = isSelected;
			// update the active/selected annotation
			updateActiveIGTAnnotation(rowData);
			return component;
		}
			
		/**
		 * Updates the active annotation for the renderer so that the renderer can 
		 * render the active annotation if there is no editor. The active
		 * annotation will usually be in the cell that is edited.
		 *  
		 * @param activeAnnotation the active annotation
		 */
		public void updateActiveAnnotation(Annotation activeAnnotation) {
			this.activeAnnotation = activeAnnotation;
		}
		
		/**
		 * Sets the active/selected IGT annotation for the renderer.
		 * 
		 * @param dataModel
		 */
		private void updateActiveIGTAnnotation(IGTDataModel dataModel) {
			if (activeAnnotation == null) {
				activeIGTAnnotation = null;
			} else {
				IGTTier targetTier = dataModel.getRowDataForTier(activeAnnotation.getTier().getName());
				if (targetTier == null) {
					activeIGTAnnotation = null;
				} else if (!TimeRelation.isInside(activeAnnotation, dataModel.getBeginTime(), dataModel.getEndTime())) {
					activeIGTAnnotation = null;
				} else {
					for (IGTAnnotation igta : targetTier.getAnnotations()) {
						if (igta.getAnnotation() == activeAnnotation) {
							activeIGTAnnotation = igta;
							return;
						}
					}
				}
			}
		}
	}

	/**
	 * A local class to adapt the IGTGroupEditor to be used in a JTable.
	 * <p>
	 * Implementation note: since the editor edits the object in situe, we don't
	 * need (or want!) the listeners to work. The JTable listens to stopCellEditing events
	 * and tries to getCellEditorValue() and put it in the model. All that is unneeded here.
	 * <br/>
	 * It will also remove the editor, and it happens that we want to keep the editor active
	 * on the selected cell as much as possible. So then if we re-activate the editor when
	 * it is removed, we get in all sorts of annoying loops. This way is less bad.
	 */
	private class GroupTableCellEditor implements TableCellEditor {
		private final IGTViewerModel model;

		private GroupTableCellEditor(IGTViewerModel model) {
			this.model = model;
		}

		@Override // CellEditor
		public Object getCellEditorValue() {
			// The editor modifies the object in situ.
			return null;
		}

		@Override // (Abstract)CellEditor
		public boolean isCellEditable(EventObject anEvent) {
			return true;
		}

		@Override // (Abstract)CellEditor
		public boolean shouldSelectCell(EventObject anEvent) {
			return true;
		}

		@Override // (Abstract)CellEditor
		public boolean stopCellEditing() {
			groupEditor.commitEdit();
			model.startEditingRow(-1);
			// We can't return false to prevent the editor from closing;
			// in that case it can't be moved to another cell either.
			return true;
		}

		@Override // AbstractCellEditor
		public void cancelCellEditing() {
			groupEditor.cancelEdit();
			model.startEditingRow(-1);
		}

		@Override // TableCellEditor
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			IGTDataModel rowModel = (IGTDataModel) value; // == model.getRowData(row);
			model.startEditingRow(row); // method should probably be removed
			groupEditor.editIGTGroup(rowModel, viewerRenderInfo);

			return groupEditor;
		}

		public Component getTableCellEditorComponent() {
			return groupEditor;
		}

        /**
         * Not needed; this editor edits the object in situ.
         */
		@Override // (Abstract)CellEditor
		public void addCellEditorListener(CellEditorListener l) {
			// Not wanted: we edit in situ.
		}

		@Override // (Abstract)CellEditor
		public void removeCellEditorListener(CellEditorListener l) {
			// Not wanted: we edit in situ.
			// This happens to be called when the editor is removed from the table.
		}
	}
	private AbstractViewer viewer;
	private IGTViewerModel model;
	private IGTViewerRenderInfo viewerRenderInfo;
	private IGTGroupEditor groupEditor;
	private GroupTableCellRenderer groupCellRenderer;
	private TextAnalyzerHostContext hostContext;
	private TextAnalyzerLexiconHostContext lexHostContext;
	
	private List<IGTViewerChangeListener> viewerChangeListeners;
	private int previousWidth;
	private int previousRowWithActiveAnnotation = -1;
	private JTable table;
	private JScrollPane tableScrollPane;
	private String tierToolTipPattern;
	private JPopupMenu popupMenu;
	private JMenuItem showHideMoreMI;
	private JCheckBoxMenuItem showHideTimeCodesMI, showHideSpeakerMI;
	private boolean tableSelectionIsUpdating = false;
	private Map<KeyStroke, Object> origKSMaps;
			
	/**
	 * Create an IGTViewer.
	 * 
	 * @param model for the data to show
	 * @param hostContext to interact with the host
	 * @param viewer for notifying the host about changes in active annotation and selection
	 */
	public IGTViewer(final IGTViewerModel model, TextAnalyzerHostContext hostContext, 
			TextAnalyzerLexiconHostContext lexHostContext, AbstractViewer viewer) {
		super();
		this.model = model;
		this.hostContext = hostContext;
		this.lexHostContext = lexHostContext;
		this.viewer = viewer;
		
		viewerChangeListeners = new ArrayList<IGTViewerChangeListener>(4);
		viewerRenderInfo = new IGTViewerRenderInfo();
		viewerRenderInfo.headerWidth = 100;
		
		groupEditor = new IGTGroupEditor(this, viewer);
		model.addIGTViewerModelListener(this);
		// TODO maybe the viewer should not add itself as listener?
		// Listener is removed in isClosing().
		if (hostContext instanceof SuggestionSetProvider) {
			((SuggestionSetProvider) hostContext).addSuggestionSetListener(this);
		}
		
		TableModel tableModel = (IGTViewerModelImpl)model; // XXX nasty cast // new GroupTableModel(model);
		groupCellRenderer = new GroupTableCellRenderer();
		GroupTableCellEditor editor = new GroupTableCellEditor(model);
		
		table = new JTable(tableModel);
		table.setDefaultRenderer(IGTDataModel.class, groupCellRenderer);
		table.setDefaultEditor(IGTDataModel.class, editor);
		table.setRowMargin(viewerRenderInfo.vertRowMargin);
		table.setTableHeader(null);
		
		// no longer for resizing, but for right click and hover behavior 
		table.addMouseListener(this);
		table.addMouseMotionListener(this);
		editor.getTableCellEditorComponent().addMouseListener(this);
		
		// To keep the editor where the selection in the table is:
		// (not perfect; typing ESC removes the editor entirely)
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new TableSelectionListener());
		
		addComponentListener(this);
		tableScrollPane = new JScrollPane(table);
		tableScrollPane.addMouseListener(this);

		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		add(tableScrollPane, gbc);
		new InitScrollThread().start();
	}
	
	/**
	 * This is the time to store preferences...
	 * and probably also to remove the listeners.
	 */
	public void isClosing(Transcription transcription) {
		storeGUIPreferences(transcription);

		if (hostContext instanceof SuggestionSetProvider) {
			((SuggestionSetProvider) hostContext).removeSuggestionSetListener(this);
		}
		table.removeMouseListener(this);
		GroupTableCellEditor editor = (GroupTableCellEditor)
				table.getDefaultEditor(IGTDataModel.class);
		editor.getTableCellEditorComponent().removeMouseListener(this);
        removeComponentListener(this);
	}

	/**
	 * Store GUI-related preferences.
	 * <p>
	 * It needs the Transcription to store them in the correct preferences file,
	 * but it does not hold on to it.
	 */
	private void storeGUIPreferences(Transcription transcription) {
		Preferences.set("IGTViewerRenderInfo.headerWidth",
				new Integer(viewerRenderInfo.headerWidth), transcription);
	}

	/**
	 * Read (and apply) GUI-related preferences.
	 * <p>
	 * It needs the Transcription to get them from the correct preferences file,
	 * but it does not hold on to it.
	 */
	public void readGUIPreferences(Transcription transcription) {
		Integer intPref = Preferences.getInt("IGTViewerRenderInfo.headerWidth", transcription);
		if (intPref != null) {
			setHeaderWidth(intPref);
		}
	}
	
	/**
	 * @return the render info object, for direct modification of colors and
	 * sizes
	 */
	public IGTViewerRenderInfo getViewerRenderInfo() {
		return viewerRenderInfo;
	}
	
	/**
	 * Set the per tier color Map
	 */
	public void setTierColorMap(Map<String, Color> tierColorMap){
		viewerRenderInfo.setTierColorMap(tierColorMap);
	}
	
	/**
	 * Set the per tier font Map
	 */
	public void setTierFontMap(Map<String, java.awt.Font> fontMap){
		viewerRenderInfo.setTierFontMap(fontMap);
	}
	
	/**
	 * Passes this flag to the group editor. 
	 * @param deselectCommits whether or not to commit text in the edit box when deselecting it
	 */
	public void setDeselectCommits(boolean deselectCommits) {
		groupEditor.setDeselectCommits(deselectCommits);
	}

	/**
	 * @param fontSize the new base (tier independent) font size
	 */
	@Override
	public void setFontSize(int fontSize) {
		if (fontSize != viewerRenderInfo.getFontSize()) {
			viewerRenderInfo.setFontSize(fontSize);
			recalculateAllRows(true);
		}
	}

	/**
	 * @return the current size of the default, base font 
	 * (obtained from the render info object) 
	 */
	@Override
	public int getFontSize() {
		return viewerRenderInfo.getFontSize();
	}
	
	/**
	 * In the paint component the group renderer is called to render the visible rows/blocks.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	/**
	 * Returns the height of the requested row.
	 *  
	 * @param row row index
	 * @return the height of the row.
	 */
	public int getRowHeight(int row) {
		try {
			return model.getRowData(row).getRenderInfo().height;
		} catch (ArrayIndexOutOfBoundsException ai) {
			// log error
			return 0;
		}			
	}
	
	/**
	 * Returns the number of rows.
	 * 
	 * @return the number of rows in the viewer
	 */
	public int getRowCount() {
		return model.getRowCount();
	}
	
	/**
	 * @return the selected row in the table that is the main part of this viewer
	 */
	public int getSelectedRow() {
		return table.getSelectedRow();
	}
	
	/**
	 * Returns the total height of all the data in the model.
	 * 
	 * @return the total height
	 */
	public int getTotalHeight() {
		return table.getHeight();
	}
	
	/**
	 * The header width is the width of the area available for the tier labels
	 * (to the left of the annotations).
	 * 
	 * @return the width of the area available for tier labels
	 */
	public int getHeaderWidth() {
		return viewerRenderInfo.headerWidth;
	}
	
	/**
	 * Sets the width of the header or tier label area.
	 * 
	 * @param hWidth the new width for tier labels area
	 */
	public void setHeaderWidth(int hWidth) {
		if (hWidth != viewerRenderInfo.headerWidth) {
			viewerRenderInfo.headerWidth = hWidth;
			recalculateAllRows(true);
			repaint();
			notifyViewChanged();//?
		}
	}
	
	/**
	 * Initiates a calculation of the height of one row (i.e. one block of annotations,
	 * a top level annotation with all its depending annotations).
	 * 
	 * @param row the row (== root annotation plus depending annotations) to calculate the height for
	 */
	public void calculateHeightForRow(int row, boolean notifyForChanges) {
		try {
			int width = table.getWidth() - viewerRenderInfo.headerWidth - 1;
			
			if (width > 0) {// introduce a minimum width for the data
				IGTDataModel dModel = model.getRowData(row);
				Graphics g = getGraphics();
				// TODO start calculating width at top level of the group
				g.setFont(viewerRenderInfo.defaultFont);
				IGTCalculator.calculateShortTierLabels(g, dModel, viewerRenderInfo.headerWidth);
				// calculates everything
				IGTTier rootTier = dModel.getRootRow();
				/*int rootWidth =*/ IGTCalculator.calculateTierAndAnnotationsLAS(g, viewerRenderInfo, 
						rootTier, true);
				
				// calculate wrapping information
				int tierCount = dModel.getRowCount();
				IGTTier curTier;
				List<String> procTiers = new ArrayList<String>(); // add already processed tiers to the list
				
				for (int i = 0; i < tierCount; i++) {
					curTier = dModel.getRowData(i);
					if (procTiers.contains(curTier.getTierName())) {
						continue;
					}
					
					IGTCalculator.calculateWrappingInfo(g, viewerRenderInfo, curTier, width);
					
					procTiers.add(curTier.getTierName());
					if (curTier.isInWordLevelBlock()) {
						addChildTiers(procTiers, curTier);
					}
				}

				int origHeight = dModel.getRenderInfo().height;

				// TODO collect information for total height and store it in the render info object
				// of the row
				tierCount = dModel.getRowCount();
				
				int rowHeight = 0;
				procTiers.clear();

				for (int i = 0; i < tierCount; i++) {
					curTier = dModel.getRowData(i);
					if (procTiers.contains(curTier.getTierName())) {
						continue;
					}
					// TODO get the real tier font height
					if (curTier.isInWordLevelBlock()) {
						int subHeight = IGTCalculator.calculateTierYPositionRecursive(g, viewerRenderInfo, 
								curTier, rowHeight);
						rowHeight += subHeight;
						addChildTiers(procTiers, curTier);
					} else {
						int subHeight = IGTCalculator.calculateTierYPosition(g, viewerRenderInfo, 
								curTier, rowHeight);
						rowHeight += subHeight;
					}
					procTiers.add(curTier.getTierName());
				}
				
				dModel.getRenderInfo().height = rowHeight;
				//System.out.println("H1: " +rowHeight + " - H2: " + rowHeight2);
				if (table != null) {
					// setRowHeight()'s height is including the vertical row margin
					table.setRowHeight(row, rowHeight + viewerRenderInfo.vertRowMargin);
				}
				if (notifyForChanges && origHeight != rowHeight) {
					notifyViewChanged();
				}
			}
		} catch (ArrayIndexOutOfBoundsException ai) {
			// log error
		}
	}

	/**
	 * Recalculates the height of all rows (groups).
	 * <p>
	 * Caches the width of this Component so that it doesn't do
	 * all that work when there is no change.
	 * To override that, call recalculateAllRows(true).
	 * <p>
	 * Generates a viewChanged() call to IGTViewerChangeListeners.
	 */
	public void recalculateAllRows(boolean always) {
		int width = table.getWidth();
		if (!always && width == previousWidth) {
			return;
		}
		previousWidth = width;
		
		final int rowCount = getRowCount();
		for (int i = 0; i < rowCount; i++) {
			calculateHeightForRow(i, false);
		}

		notifyViewChanged();
	}

	/**
	 * The IGT group that is being edited has changed under the feet
	 * of the editor. Point it to the new version.
	 * 
	 * @param row
	 * @param rowModel
	 */
	public void updateStaleEditor(int row) {
		groupEditor.cancelEdit();
		IGTDefaultModel rowModel = (IGTDefaultModel) model.getRowData(row);
		table.setRowSelectionInterval(row, row);
		model.startEditingRow(row);
		groupEditor.editIGTGroup(rowModel, viewerRenderInfo);
	}
	
	/**
	 * Adds the child tiers to the specified list, recursively.
	 * 
	 * @param children the list to add to
	 * @param igtTier the tier to retrieve the children from
	 */
	private void addChildTiers(List<String> children, IGTTier igtTier) {
		final List<IGTTier> childTiers = igtTier.getChildTiers();
		if (childTiers != null && !childTiers.isEmpty()) {
			for (int i = 0; i < childTiers.size(); i++) {
				IGTTier child = childTiers.get(i);
				children.add(child.getTierName());
				addChildTiers(children, child);
			}
		}
	}
	
	/**
	 * Makes a (special) tier visible or hidden.
	 * 
	 * @param tierType either TC type or Speaker type
	 * @param newState the new visibility flag
	 */
	private void changeVisibility(IGTTierType tierType, boolean newState) {
		if (model.getSpecialTierVisibility(tierType) != newState) {
			model.setSpecialTierVisibility(tierType, newState);
			recalculateAllRows(true);
		}
	}
	
	/**
	 * Updates the list of hidden tiers (and therefore the list of visible tiers).
	 * It could check if the passed list differs from the current list before 
	 * clearing and re-populating the model.
	 * 
	 * @param hiddenTierNames the new list of hidden tiers
	 * @see {@link InterlinearEditor#initModel}// private method 
	 */
	private void updateHiddenTiersInModel(List<String> hiddenTierNames) {
		if (hiddenTierNames == null) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("The list of hidden tiers is null");
			}
			return;//silently return
		}
		// could check if the list differs from the current one
		boolean empty = model.removeAllRows();
		if (!empty) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Error: the viewer model is not empty after removing all rows");
			}
			// show message and return?
		}
		model.setHiddenTiers(hiddenTierNames);
		// add row data
		// get all annotations from all (top level) tiers, minus the hidden tiers
		List<AlignableAnnotation> allAnns = new ArrayList<AlignableAnnotation>();
		
		for (TierImpl t : ((TranscriptionImpl) hostContext.getTranscription()).getTiers()) {
			if (!t.hasParentTier() && !hiddenTierNames.contains(t.getName())) {
				allAnns.addAll(t.getAlignableAnnotations());
			}
		}
		
		AnnotationCoreComparator comparator = new AnnotationCoreComparator();
		Collections.sort(allAnns, comparator);
		// check the current visibility of special tiers
		boolean speakerVisible = true;
		Boolean speakVisPref = Preferences.getBool("InterlinearEditor.ShowSpeaker", 
				hostContext.getTranscription());
		if (speakVisPref != null) {
			speakerVisible = speakVisPref.booleanValue();
		}
		
		boolean tcVisible = true;
		Boolean tcVisPref = Preferences.getBool("InterlinearEditor.ShowTimeCode", 
				hostContext.getTranscription());
		if (tcVisPref != null) {
			tcVisible = tcVisPref.booleanValue();
		}
		List<IGTTierType> hiddenSpecTiers = null;
		if (!speakerVisible || tcVisible) {
			hiddenSpecTiers = new ArrayList<IGTTierType>(2);
			if (!speakerVisible) {
				hiddenSpecTiers.add(IGTTierType.SPEAKER_LABEL);
			}
			if (!tcVisible) {
				hiddenSpecTiers.add(IGTTierType.TIME_CODE);
			}
		}
		
		for (AlignableAnnotation aa : allAnns) {
			IGTDefaultModel rowModel = new IGTDefaultModel(aa, hiddenTierNames, 
					hiddenSpecTiers);
			model.addRow(rowModel);
		}
		recalculateAllRows(true);
	}
	
	/**
	 * Converts between visible and hidden tiers; when the input are the visible tiers
	 * it returns the hidden tiers and vice-versa.
	 * 
	 * @param transcription containing all the tiers, not null
	 * @param inputSelection the current set of visible or hidden tiers, not null
	 * @return the set difference of input selection
	 */
	private List<String> getSetDifference(Transcription transcription, Collection<String> inputSelection) {
		List<String> setDiff = new ArrayList<String>();
		
		for (Tier t : transcription.getTiers()) {
			if (!inputSelection.contains(t.getName())) {
				setDiff.add(t.getName());
			}
		}
		
		return setDiff;
	}
	
	@Override
	public void mouseClicked(MouseEvent me) {
		// System.out.println("Click: " + me.getPoint() + " source: " + me.getSource());
		// would like to convert table coordinates to coordinates in the editor and
		// programmatically click there (to start editing an annotation with one click) 
		// But mouse events are not received here, as long as the default TableUI mouse listener
		// is there
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
		//System.out.println("Press: " + me.getX() + " source: " + me.getSource());
		// for tier labels show a popup menu
		if (me.getX() < viewerRenderInfo.headerWidth) {
			if (SwingUtilities.isRightMouseButton(me) || me.isPopupTrigger()) {
				JPopupMenu popup = getPopup();
	            popup.show(table, me.getX(), me.getY());
			}
		} else {
			if (SwingUtilities.isRightMouseButton(me) || me.isPopupTrigger()) {
				// treat as single left mouse button
				if (me.getSource() == table) {
					int row = table.rowAtPoint(me.getPoint());
					table.setRowSelectionInterval(row, row);
					table.editCellAt(row, 0);
					// convert table coordinates to coordinates in the editor and
					// programmatically press there
					Rectangle cellRect = table.getCellRect(row, 0, true);
					int yInCell = me.getY() - cellRect.y;
					// the following would be a bit tricky/dirty maybe
					// groupEditor.pressedAt(me.getX(), yInCell);
					
					// this is more strict but...
					TableCellEditor cellEditor = table.getCellEditor(row, 0);
					if (cellEditor instanceof GroupTableCellEditor) {
						Component cc = ((GroupTableCellEditor) cellEditor).getTableCellEditorComponent();
						if (cc instanceof IGTGroupEditor) {
							((IGTGroupEditor) cc).pressedAt(me.getX(), yInCell);
						}
					}
				} // else if source == IGTGroupEditor do nothing
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		//System.out.println("Release: " + me.getX());
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		int row = table.rowAtPoint(me.getPoint());
		if (row == -1) {
			table.setToolTipText(null);
			return;
		}
		
		if (me.getX() < viewerRenderInfo.headerWidth) {
//			System.out.println("Point: " + me.getPoint());
//			System.out.println("Row index: " + row);
//			System.out.println("Row Rect: " + table.getCellRect(row, 0, true));
			
			IGTDefaultModel dataModel = (IGTDefaultModel) table.getValueAt(row, 0);
			int yInCell = me.getY() - table.getCellRect(row, 0, false).y;
			
			for (int i = 0; i < dataModel.getRowCount(); i++) {
				IGTTier igtTier = dataModel.getRowData(i);
				if (igtTier.isAtY(yInCell)) {
					//System.out.println("Tier: " + igtTier.getTierName());
					if (igtTier.isSpecial()) {
						table.setToolTipText(null);
						return; // no tool tip for time code, participant etc
					}
					String tierName = igtTier.getTierName();
					String type = "-";
					String langRef = "-";
					
					Tier tier = hostContext.getTranscription().getTierWithId(tierName);
					if (tier != null) {
						type = tier.getLinguisticType().getLinguisticTypeName();
				        if (tier.getLangRef() != null) {
				            langRef = tier.getLangRef();
				        }
					}
					table.setToolTipText(String.format(getTierToolTipPattern(), 
							tierName, type, langRef));
					return;
				}
			}			
		} else {
			table.setToolTipText(null);
		}
	}
	
	/**
	 * Creates the popup menu if it hasn't been created yet and returns it.
	 * 
	 * @return the right-mouse-button context menu
	 */
	private JPopupMenu getPopup() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu("");
			PopupMenuListener pmListener = new PopupMenuListener();
			showHideMoreMI = new JMenuItem();
	        showHideMoreMI.setText(ElanLocale.getString(
	                "MultiTierControlPanel.Menu.ShowHideMore"));
	        showHideMoreMI.setActionCommand("showHideMore");
	        showHideMoreMI.addActionListener(pmListener);
	        popupMenu.add(showHideMoreMI);
	        popupMenu.addSeparator();
	        
	        showHideSpeakerMI = new JCheckBoxMenuItem(
	        		ElanLocale.getString("InterlinearEditor.Menu.ShowHideSpeaker"));
	        // get initial state from the viewer model
	        showHideSpeakerMI.setSelected(model.getSpecialTierVisibility(IGTTierType.SPEAKER_LABEL));	        	        
	        showHideSpeakerMI.addItemListener(pmListener);
	        popupMenu.add(showHideSpeakerMI);
	        
	        showHideTimeCodesMI = new JCheckBoxMenuItem(
	        		ElanLocale.getString("InterlinearEditor.Menu.ShowHideTimeCode"));
	        // get initial state from the viewer model
	        showHideTimeCodesMI.setSelected(model.getSpecialTierVisibility(IGTTierType.TIME_CODE));	        	        
	        showHideTimeCodesMI.addItemListener(pmListener);
	        popupMenu.add(showHideTimeCodesMI);
		}

		return popupMenu;
	}
	
	/**
	 * 
	 * @return an html formatted tooltip string with a few format specifiers to
	 * allow to pass tier name, type and language as arguments
	 */
	private String getTierToolTipPattern() {
		if (tierToolTipPattern == null) {
	        StringBuilder tooltip = new StringBuilder("<html><table>");

	        // tier name
	        tooltip.append("<tr><td><b>");
	        tooltip.append(ElanLocale.getString("EditTierDialog.Label.TierName"));
	        tooltip.append("</b></td><td>");
	        tooltip.append("%s");
	        tooltip.append("</td></tr>");
	        
	        // tier type
	        tooltip.append("<tr><td><b>");
	        tooltip.append(ElanLocale.getString(
	                "EditTierDialog.Label.LinguisticType"));
	        tooltip.append("</b></td><td>");
	        tooltip.append("%s");
	        tooltip.append("</td></tr>");
	        
	        // tier content language
	        tooltip.append("<tr><td><b>");
	        tooltip.append(ElanLocale.getString("EditTierDialog.Label.ContentLanguage"));
	        tooltip.append("</b></td><td>");
	        tooltip.append("%s");
	        tooltip.append("</td></tr>");
	        tooltip.append("</table></html>");
	        tierToolTipPattern = tooltip.toString();
		}
		
		return tierToolTipPattern;
	}

// ############## IGTEditProvider implementation  ###############################
	@Override
	public List<IGTEditAction> actionsForAnnotation(IGTAnnotation igtAnnotation) {
		if (igtAnnotation != null) {
			// TODO check type of annotation, based on that create a list of actions
			ShortcutsUtil su = ShortcutsUtil.getInstance();
			List<IGTEditAction> actions = new ArrayList<IGTEditAction>(4);
			IGTEditAction act = null;
			
			if (hostContext.isAnalyzerSource(igtAnnotation.getIGTTier().getTierName())) {
				act = new IGTInterlinearizeAction(igtAnnotation, hostContext,
						ElanLocale.getString("InterlinearEditor.Button.Interlinearize"));
				act.putValue(Action.ACCELERATOR_KEY, su.getKeyStrokeForAction(ELANCommandFactory.ANALYZE_ANNOTATION, 
						ELANCommandFactory.INTERLINEARIZATION_MODE));
				actions.add(act);
			}
			
			AbstractAnnotation ann = igtAnnotation.getAnnotation();
			Tier tier = ann.getTier();
			LexiconQueryBundle2 lexBundle = tier.getLinguisticType().getLexiconQueryBundle();
			Constraint constraints = tier.getLinguisticType().getConstraints();
			
			if(lexBundle != null && !igtAnnotation.getAnnotation().getValue().isEmpty()) {
				act = new IGTAddToLexiconAction(igtAnnotation, lexHostContext,
						ElanLocale.getString("InterlinearEditor.Button.AddToLexicon"));
				act.putValue(Action.ACCELERATOR_KEY, su.getKeyStrokeForAction(ELANCommandFactory.ADD_TO_LEXICON, 
						ELANCommandFactory.INTERLINEARIZATION_MODE));
				actions.add(act);
			}
			
			act = new IGTDeleteAction(igtAnnotation,
					hostContext, ElanLocale.getString("Menu.Annotation.DeleteAnnotation"));
			act.putValue(Action.ACCELERATOR_KEY, su.getKeyStrokeForAction(ELANCommandFactory.DELETE_ANNOTATION, 
					ELANCommandFactory.INTERLINEARIZATION_MODE));
			actions.add(act);
			

			boolean isInsertionSupported = constraints != null && constraints.supportsInsertion();
			
			// An annotation can be split if it is not on a top-level tier,
			// and if it has no children or only 1-to-1 associated child annotations.
			// this test should be moved to a single place. a single class
			if (tier.getParentTier() == null) {
				boolean canSplit = true;
				List<TierImpl> childTiers = ((TierImpl)tier).getChildTiers();
				if (!childTiers.isEmpty()) {
					for (TierImpl tt : childTiers) {
						if (tt.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
							continue;
						}
						if (!ann.getChildrenOnTier(tt).isEmpty()) {
							canSplit = false;
							break;
						}
					}
				}
				
				if (canSplit) {
					act = new IGTSplitAnnotationAction(igtAnnotation,
							hostContext, IGTSplitAnnotationAction.SPLIT,
							ElanLocale.getString("Menu.Annotation.SplitAnnotation"));
					act.putValue(Action.ACCELERATOR_KEY, su.getKeyStrokeForAction(ELANCommandFactory.SPLIT_ANNOTATION, 
							ELANCommandFactory.INTERLINEARIZATION_MODE));
					actions.add(act);
				}
			}
			
			if (isInsertionSupported) {
				// It would be nicer to push the decision for 
				// "Split" or "New" into the Action. But at that point we can't
				// decide whether we need one or two ("Before" and "After").
				act = new IGTSplitAnnotationAction(igtAnnotation,
						hostContext, IGTSplitAnnotationAction.NEW_BEFORE,
						ElanLocale.getString("Menu.Annotation.NewAnnotationBefore"));
				act.putValue(Action.ACCELERATOR_KEY, su.getKeyStrokeForAction(ELANCommandFactory.NEW_ANNOTATION_BEFORE, 
						ELANCommandFactory.INTERLINEARIZATION_MODE));
				actions.add(act);

				act = new IGTSplitAnnotationAction(igtAnnotation,
						hostContext, IGTSplitAnnotationAction.NEW_AFTER,
						ElanLocale.getString("Menu.Annotation.NewAnnotationAfter"));
				act.putValue(Action.ACCELERATOR_KEY, su.getKeyStrokeForAction(ELANCommandFactory.NEW_ANNOTATION_AFTER, 
						ELANCommandFactory.INTERLINEARIZATION_MODE));
				actions.add(act);
			}

			// Only offer to add children if there are child tiers.
			List<? extends Tier> childTiers = tier.getChildTiers();
			
			if (!childTiers.isEmpty()) {
				act = new IGTCreateDependentAnnotationsAction(igtAnnotation,
						hostContext, 
						ElanLocale.getString("Menu.Annotation.CreateDependingAnnotations"));
				act.putValue(Action.ACCELERATOR_KEY, su.getKeyStrokeForAction(ELANCommandFactory.CREATE_DEPEND_ANN, 
						ELANCommandFactory.INTERLINEARIZATION_MODE));
				actions.add(act);
			}
			
			// Merge with next and previous annotations would be nice, but it
			// claims to work only for root tiers. It might actually work for
			// AlignableAnnotations generally, since that is what it checks for.
			
//			act = new IGTMergeAnnotationAction(igtAnnotation,
//					hostContext, false, "Merge Previous Annotation");
//			actions.add(act);
//			
//			act = new IGTMergeAnnotationAction(igtAnnotation,
//					hostContext, true, "Merge Next Annotation");
//			actions.add(act);

			return actions;
		}
		return null;
	}

	@Override
	public List<IGTEditAction> actionsForEmptySpace(IGTAnnotation parent) {
		if (parent != null) {
			List<IGTEditAction> actions = new ArrayList<IGTEditAction>(1);
			IGTEditAction act = new IGTCreateDependentAnnotationsAction(parent,
					hostContext, ElanLocale.getString("Menu.Annotation.CreateDependingAnnotations"));
			actions.add(act);
			
			return actions;
		}
		
		return null;
	}

// ################# IGTViewerModelListener implementation ################################
	@Override
	public void viewerModelChanged(IGTViewerModelEvent event) {
		if (event != null) {
			if (event.getRow() > -1) {
				if (event.getType() == ModelEventType.CHANGE) {
					calculateHeightForRow(event.getRow(), true);// triggers an event
				} 
			}
		}		
	}

	/**
	 * If an analyzer/recognizer produces a list of suggestions, some user interaction is 
	 * needed to select one of the provided suggestions.
	 * 
	 * For now the event of new suggestion becoming available will start editing the group of
	 * annotations (the row) the suggestions concern.
	 * 
	 * This usually comes in from the HostContext and is passed on to the IGTGroupEditor.
	 * 
	 * @param event the event holding the list of suggestions
	 */
	@Override // SuggestionSetListener
	public void suggestionSetDelivered(SuggestionSetEvent event) {
		if (event != null && event.getSuggestionSets() != null) {
			List<SuggestionSet> sugSets = event.getSuggestionSets();
			
			int recursionLevel = event.getRecursionLevel();
			Position sourcePos = null;

			for (SuggestionSet sugSet : sugSets) {
				sourcePos = sugSet.getSource();
				// Find the row to edit based on tier name and time information.
				// Note that the position is that of a source, so it is guaranteed to exist.
				int rowIndex = getRowIndexForTierAndTime(sourcePos.getTierId(), (sourcePos.getBeginTime() + sourcePos.getEndTime()) / 2);
				
				if (rowIndex > -1) {
					// Start editing the row, if it isn't already the editing row, and inform the 
					// editor. In most cases all suggestion sets will pertain to one row (i.e. group of annotations)
					// Also bring this row into view by scrolling (if possible).
					
					// Scroll to make this row visible.
					scrollToVisible(rowIndex);
					table.editCellAt(rowIndex, 0);
					break;
				}
			}
			
			// create a suggestion model
			IGTSuggestionViewerModel svModel = new IGTSuggestionViewerModel(recursionLevel);
			svModel.renderInfo.setHeaderFont(viewerRenderInfo.defaultFont);
			svModel.renderInfo.setTextInsets(viewerRenderInfo.annBBoxInsets);
			int optRowHeaderWidth = viewerRenderInfo.headerWidth;
			boolean rhwCalculated = false;
			
			for (SuggestionSet sugSet : sugSets) {
				IGTSuggestionModel sugModel = new IGTSuggestionModel(sugSet);
				svModel.addRow(sugModel);
				
				if (!rhwCalculated) {
					optRowHeaderWidth = IGTCalculator.calcSuggestionRowHeaderWidth(getGraphics(), sugModel, 
							svModel.renderInfo);
					if (optRowHeaderWidth > 0) {
						svModel.renderInfo.rowHeaderWidth = optRowHeaderWidth;
					}
					rhwCalculated = true;
				}
				// copy some settings from viewer level to suggestion level
				IGTSuggestionRenderInfo sugRenderInfo = (IGTSuggestionRenderInfo)sugModel.getRenderInfo();
				sugRenderInfo.rowHeaderWidth = svModel.renderInfo.rowHeaderWidth;
				sugRenderInfo.setTextInsets(svModel.renderInfo.getTextInsets());
				
			}
			
			// Let the group editor create a window.
			groupEditor.suggestionSetDelivered(svModel, sourcePos, hostContext);
		}
	}
	
	/**
	 * Cancel any currently displayed suggestion set, since the user has
	 * initiated some action to create new ones.
	 */
	@Override // SuggestionSetListener
	public void cancelSuggestionSet() {
		groupEditor.cancelSuggestionSet();
	}

	/**
	 * Make sure that the row specified is scrolled into view.
	 * @param row
	 */
	void scrollToVisible(int row) {
		table.scrollRectToVisible(table.getCellRect(row, 0, true));
	}
	
	/**
	 * Returns the index of the row of data containing the specified tier and 
	 * with the specified time within its time boundaries.  
	 * 
	 * @param tierName the tier name to look for
	 * @param time the time to find
	 * @return the row index or -1
	 */
	public int getRowIndexForTierAndTime(String tierName, long time) {
		if (tierName != null /*&& time > -1*/) {
			IGTDataModel rowModel;
			
			for (int i = 0; i < model.getRowCount(); i++) {
				rowModel = model.getRowData(i);
				
				if (rowModel.getBeginTime() <= time && 
						rowModel.getEndTime() >= time) {
					if (rowModel.getRowIndexForTier(tierName) > -1) {
						return i;
					}
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Returns the index of the first row of data containing the specified 
	 * time within its time boundaries. If the time is equal to the end time
	 * of a row, the next row is checked for its begin time: if that also equals 
	 * the specified time the next row will be returned. 
	 * 
	 * @param time the time to find
	 * @return the row index or -1
	 */
	public int getRowIndexForTime(long time) {
		if (time > -1) {
			
			for (int i = 0; i < model.getRowCount(); i++) {
				IGTDataModel rowModel = model.getRowData(i);
				
				if (rowModel.getBeginTime() <= time && 
						rowModel.getEndTime() >= time) {
					if (rowModel.getEndTime() == time) {
						// check begin time of next row, use that row if the same
						if (i < model.getRowCount() - 1) {
							IGTDataModel nextModel = model.getRowData(i + 1);
							if (nextModel.getBeginTime() == time) {
								return i + 1;
							}
						}
					}
					
					return i;					
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Returns the time interval represented by the annotations in the specified row.
	 * 
	 * @param row row index
	 * @return array of length 2, the begin time and end time, 
	 *     or null of there is no row at the specified index
	 */
	public long[] getTimeIntervalForRow(int row) {
		if (row >= 0 && row < model.getRowCount()) {
			IGTDataModel rowModel = model.getRowData(row);
			return new long[]{rowModel.getBeginTime(), rowModel.getEndTime()};
		}
		
		return null;
	}

	/**
	 * Returns whether the specified tier is present (and visible) in the row data
	 * of the specified row.
	 * 
	 * @param tierName the name of the tier to find
	 * @param row the index of the row to query
	 * @return true if the tier is visible in the specified row, false otherwise
	 */
	public boolean isTierInRow(String tierName, int row) {
		if (row >= 0 && row < model.getRowCount()) {
			IGTDataModel rowModel = model.getRowData(row);
			
			return rowModel.getRowIndexForTier(tierName) > -1;
		}
		
		return false;
	}
	
	/**
	 * Called from an AbstractViewer's updateActiveAnnotation(). Also bring it
	 * into view, if possible.  (Scrolling to the correct cell fails when switching
	 * modes; this method may be called before the table etc. is fully initialized.
	 * That's why a separate thread is launched that waits for the initialization 
	 * to complete and then scrolls.) 
	 * <p>
	 * Note that if we change the active annotation and notify the world, we get
	 * notified back here (because we're listeners too). So before doing any
	 * work, check if the notification changes anything.
	 * 
	 * @param newActiveAnnotation the new active annotation
	 * @see InitScrollThread
	 */
	public void updateActiveAnnotation(Annotation newActiveAnnotation) {
		if (newActiveAnnotation != groupEditor.getActiveAnnotation()) {
			if (newActiveAnnotation != null) {
				String tierName = newActiveAnnotation.getTier().getName();
				long time = (newActiveAnnotation.getBeginTimeBoundary() + newActiveAnnotation.getEndTimeBoundary()) / 2;
				final int rowIndex = getRowIndexForTierAndTime(tierName, time);
				if (rowIndex >= 0) {
					updateStaleEditor(rowIndex);
					scrollToVisible(rowIndex);
					table.setRowSelectionInterval(rowIndex, rowIndex);
					table.editCellAt(rowIndex, 0); // hm, doesn't always have effect???
				}
			}
			groupEditor.updateActiveAnnotation(newActiveAnnotation);
		}
		int oldActiveIndex = previousRowWithActiveAnnotation;
		// a table row might need to be repainted if the previous active annotation
		// was in that row and if that row is in the view port
		groupCellRenderer.updateActiveAnnotation(newActiveAnnotation);
		previousRowWithActiveAnnotation = table.getSelectedRow();
		if (oldActiveIndex != previousRowWithActiveAnnotation && oldActiveIndex != -1) {
			Rectangle rowRect = table.getCellRect(oldActiveIndex, 0, false);
			if (tableScrollPane.getViewport().getViewRect().intersects(rowRect)) {
				table.repaint(rowRect);
			}
		}
	}
	
	/**
	 * Triggers the group editor to open a text edit box for the specified annotation.
	 * When null is passed (no active annotation) the current group editor can start editing 
	 * the last edited or selected annotation or just the first annotation. If there is no group editor, 
	 * first a row is selected (startEditingCell) and then the first
	 * annotation is set active and editing starts.
	 * 
	 * @param annotationToEdit the annotation to edit or null
	 */
	public void startEditAnnotation(Annotation annotationToEdit) {
		if (annotationToEdit == null) {				
			int row = table.getSelectedRow();
			if (row < 0 && table.getRowCount() >= 1) {
				row = 0;
			}
			table.setRowSelectionInterval(row, row);
			groupEditor.startEditAnnotation(null);			
		} else {
			int selRow = table.getSelectedRow();
			int annRow = getRowIndexForTierAndTime(annotationToEdit.getTier().getName(), 
					(annotationToEdit.getBeginTimeBoundary() + annotationToEdit.getEndTimeBoundary()) / 2);
			if (selRow == annRow) {
				groupEditor.startEditAnnotation(annotationToEdit);
			} else {
				table.setRowSelectionInterval(annRow, annRow);
				model.startEditingRow(annRow);
				groupEditor.startEditAnnotation(annotationToEdit);
			}
		}		
	}

	/**
	 * Select the row corresponding to the specified time.
	 * 
	 * Note: there is a potential interference with event handling of a change in 
	 * row selection e.g. by a mouse click. This sets the selection interval which
	 * will trigger an event which causes the InterlinearEditor to call this method,
	 * which will try to select the first row for the selection time. A flag is used
	 * to prevent this kind of loops.  
	 * 
	 * @param time the time to find in the table
	 * @see InitScrollThread
	 */
	public void selectRowForTime(long time) {
		if (tableSelectionIsUpdating) {
			return;
		}
		int row = getRowIndexForTime(time);
		
		if (getSelectedRow() != row) {
			table.setRowSelectionInterval(row, row);
			scrollToVisible(row);
		}
	}
	
	/**
	 * Adds a viewer change listener.
	 * 
	 * @param listener the listener
	 */
	public void addViewerChangeListener(IGTViewerChangeListener listener) {
		if (!viewerChangeListeners.contains(listener)) {
			viewerChangeListeners.add(listener);
		}
	}

	/**
	 * Removes a viewer change listener.
	 * 
	 * @param listener the listener
	 */
	public void removeViewerChangeListener(IGTViewerChangeListener listener) {
		viewerChangeListeners.remove(listener);
	}
	
	/**
	 * Notifies listeners of a change in the view.
	 */
	private void notifyViewChanged() {
		for (IGTViewerChangeListener listener : viewerChangeListeners) {
			listener.viewChanged();
		}
	}

	@Override // ComponentListener
	public void componentResized(ComponentEvent e) {
		recalculateAllRows(false);
	}

	@Override // ComponentListener
	public void componentMoved(ComponentEvent e) {
		// stub
	}

	@Override // ComponentListener
	public void componentShown(ComponentEvent e) {
		// stub
	}

	@Override // ComponentListener
	public void componentHidden(ComponentEvent e) {
		// stub
	}
	
	/**
	 * The current implementation stores original mappings so that they can be restored
	 * when the list of "key strokes not to be consumed" changes. 
	 * The current assumption is that the input maps of a JTable always map a KeyStroke
	 * to the same command (if a keystroke is in multiple maps).
	 * 
	 * @param ksNotToBeConsumed a List of KeyStrokes that have been set on a higher level
	 */
	public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksNotToBeConsumed) {
		if (origKSMaps == null) {
			origKSMaps = new HashMap<KeyStroke, Object>();
		}
		// restore original key bindings
		if (!origKSMaps.isEmpty()) {
			Iterator<KeyStroke> keyIt = origKSMaps.keySet().iterator();
			while (keyIt.hasNext()) {
				KeyStroke ks = keyIt.next();
				Object command = origKSMaps.get(ks);
				
				InputMap im = table.getInputMap();
				if (im.get(ks) != null) {
					im.put(ks, command);
				}
				im = table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				if (im.get(ks) != null) {
					im.put(ks, command);
				}
				im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				if (im.get(ks) != null) {
					im.put(ks, command);
				}
			}
			origKSMaps.clear();
		}
		
		final String NONE = "none"; 
		for (KeyStroke ks : ksNotToBeConsumed) {
			InputMap im = table.getInputMap();
			Object command = im.get(ks);
			
			if (command != null) {
				origKSMaps.put(ks, command);
				im.put(ks, NONE);
			}
			
			im = table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);			
			if (im.get(ks) != null) {
				origKSMaps.put(ks, command);
				im.put(ks, NONE);
			}
			
			im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			if (im.get(ks) != null) {
				origKSMaps.put(ks, command);
				im.put(ks, NONE);
			}			
		}
	}
	
	/**
	 * Listens to popup menu actions.
	 * 
	 * Consideration: instead of implementing this in this class it could also be done in 
	 * InterlinearEditor?
	 */
	private class PopupMenuListener implements ActionListener, ItemListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("showHideMore")) {
				
				Collection<String> hiddenTiers = model.getHiddenTiers();
				if (hiddenTiers == null) {
					hiddenTiers = new ArrayList<String>();
				}

				List<String> visibleTiers = getSetDifference(hostContext.getTranscription(), hiddenTiers);
				
				ShowHideMoreTiersDlg dialog = new ShowHideMoreTiersDlg(hostContext.getTranscription(), 
						visibleTiers, IGTViewer.this);
				
				String selectionMode = Preferences.getString("InterlinearEditor.SelectTiersMode", 
						hostContext.getTranscription());
				if (selectionMode != null) {
					dialog.setSelectionMode(selectionMode, (List<String>) hiddenTiers);
				}
				dialog.setVisible(true);

	        	if(dialog.isValueChanged()){
	        		List<String> selVisibleTiers = dialog.getVisibleTierNames();
	        		List<String> nextHiddenTiers = getSetDifference(hostContext.getTranscription(), 
	        				selVisibleTiers);
	        		updateHiddenTiersInModel(nextHiddenTiers);
	        		Preferences.set("InterlinearEditor.SelectTiersMode", dialog.getSelectionMode(), 
	        				hostContext.getTranscription(), false, false);
	        		Preferences.set("InterlinearEditor.HiddenTiers", nextHiddenTiers, 
	        				hostContext.getTranscription(), false, false);
	        	}
	        	
	        }
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getItem() == showHideTimeCodesMI) {   	
	        	boolean newVisibility = (e.getStateChange() == ItemEvent.SELECTED);
	        	changeVisibility(IGTTierType.TIME_CODE, newVisibility);
	        	Preferences.set("InterlinearEditor.ShowTimeCode", Boolean.valueOf(newVisibility), 
	        			hostContext.getTranscription(), false, false);
	        } else if (e.getItem() == showHideSpeakerMI) {
	        	boolean speakerVis = (e.getStateChange() == ItemEvent.SELECTED);
	        	changeVisibility(IGTTierType.SPEAKER_LABEL, speakerVis);
	        	Preferences.set("InterlinearEditor.ShowSpeaker", Boolean.valueOf(speakerVis), 
	        			hostContext.getTranscription(), false, false);
	        }
		}		
	}// end Listener class
	
	private class TableSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				final int selectedRow = table.getSelectedRow();
				// set selection, if there will be an active annotation set later
				// the selection will be updated
				if (viewer != null) {
					tableSelectionIsUpdating = true;
					long[] timeInterval = getTimeIntervalForRow(selectedRow);
					if (timeInterval != null) {
						viewer.setSelection(timeInterval[0], timeInterval[1]);
						viewer.setMediaTime(timeInterval[0]);
					} else {
						viewer.setSelection(0, 0);
					}
					tableSelectionIsUpdating = false;
				}
				
				if (table.getEditingRow() != selectedRow && selectedRow >= 0) {
					table.editCellAt(selectedRow, 0);
				}				
			}
		}
	}
	
	/**
	 * A thread that waits until the table is valid/validated (in the AWT sense)
	 * and then scrolls the table to the selected cell. 
	 * updateActiveAnnotation and selectRowForTime try to scroll the selected row
	 * to become visible but this fails if the table isn't visible and or resized yet.
	 * 
	 * @see IGTViewer#updateActiveAnnotation(Annotation)
	 * @see IGTViewer#selectRowForTime(long)
	 */
	private class InitScrollThread extends Thread {
		@Override
		public void run() {
			while (!table.isValid()) {
				try {
					Thread.sleep(500);
				} catch (Throwable t) {
					
				}
			}

			scrollToVisible(table.getSelectedRow());
		}
	}
	
}
