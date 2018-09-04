package mpi.eudico.client.annotator.turnsandscenemode;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FontSizer;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSMergeAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSMergeAction.MERGE_ROW;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;

/**
 * A viewer that (currently) shows the annotations of a single tier and adds "virtual" annotations
 * for gaps, empty area where there are no annotations. These virtual annotations can be edited, 
 * in which case real annotations are created in the corresponding gaps. 
 * Real and virtual annotations can be split (with the Enter key) based on the current media position
 * and the current caret position in the text. 
 * The main unique characteristic of this viewer is that always the full time line, from 0 to the 
 * media duration is covered by either real or virtual annotations because these segments are the
 * main way to access the entire time line for segmentation and labeling. 
 *  
 * @author Han
 *
 */
@SuppressWarnings("serial")
public class TurnsAndSceneViewer extends AbstractViewer implements ACMEditListener, 
ListSelectionListener, FontSizer {
	private TranscriptionImpl transcription;
	private String transTierId = "transcript";
	private TierImpl curTier;
	private List<TaSAnno> segmentList;
	private JScrollPane scrollPane;
	private DefaultTableModel tableModel;
	private TaSTable annoTable;

	private TaSMergeAction mergePreviousMouseAction;
	private TaSMergeAction mergeNextMouseAction;
	private TaSCellPanel editorPanel;
	private TaSCellEditor cellEditor;
	private TaSCellRenderer cellRenderer;
	private JPopupMenu popupMenu;
	/* a field to cache the "current" row at the last media controller update */
	private int rowAtMediaTime = -1;
	private int scrollRowToLocation;
	private int primaryFontSize = 16;
	/* a flag to indicate that hitting Enter to create a new segment 
	 * should not stop the player */
	private boolean continuousPlayMode = false;
	
	/**
	 * No-arg constructor.
	 */
	public TurnsAndSceneViewer() {
		super();
	}
	
	/**
	 * Constructor 
	 * @param transcription the loaded transcription
	 */
	public TurnsAndSceneViewer(Transcription transcription) {
		this();
		this.transcription = (TranscriptionImpl) transcription;
		initComponents();	
	}
	
	/**
	 * Gives access to the editor panel.
	 * 
	 * @return the panel that is used as a cell editor
	 */
	public TaSCellPanel getEditor() {
		return editorPanel;
	}
	
	/**
	 * Gives access to the table the model of which contains real or virtual (gaps)
	 * annotations.
	 * 
	 * @return the annotation table
	 */
	public JTable getAnnotationTable() {
		return annoTable;
	}

	private void initComponents() {
		setLayout(new GridBagLayout());
		tableModel = new DefaultTableModel(0, 1);
		annoTable = new TaSTable(tableModel);
		annoTable.setTableHeader(null);
		
		editorPanel = new TaSCellPanel();
		// popup menu
		popupMenu = new JPopupMenu();
		mergePreviousMouseAction = new TaSMergeAction(this, MERGE_ROW.PREVIOUS);
		mergePreviousMouseAction.putValue(Action.NAME, ElanLocale.getString(ELANCommandFactory.MERGE_ANNOTATION_WB));
		mergeNextMouseAction = new TaSMergeAction(this, MERGE_ROW.NEXT);
		mergeNextMouseAction.putValue(Action.NAME, ElanLocale.getString(ELANCommandFactory.MERGE_ANNOTATION_WN));
		popupMenu.add(mergePreviousMouseAction);
		popupMenu.add(mergeNextMouseAction);

		
		editorPanel.getTextArea().getDocument().addDocumentListener(new CellDocumentListener());
		cellEditor = new TaSCellEditor(this, editorPanel);
		cellRenderer = new TaSCellRenderer(this);
		
		annoTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		annoTable.setShowGrid(false);
		annoTable.setRowSorter(null);
		annoTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		annoTable.getColumnModel().getColumn(0).setCellEditor(cellEditor);
		
		scrollPane = new JScrollPane(annoTable);
		addComponentListener(new ViewerComponentListener());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		this.add(scrollPane, gbc);
		
		scrollRowToLocation = SwingConstants.TOP;
		annoTable.getSelectionModel().addListSelectionListener(this);
		// adding a mouse listener to the table allows to enable/disable actions 
		// depending on the location where clicked and to merge cells without first selecting them
		annoTable.addMouseListener(new TableMouseListener());
	}
	
	private void initTier() {
		if (transcription != null) {
			TierImpl t = transcription.getTierWithId(transTierId);
			if (t == null) {
				// create the tier
				if (transcription.getTiers().isEmpty()) {
                    LinguisticType type = new LinguisticType(transTierId);                     
                    transcription.addLinguisticType(type); 
                    
                    TierImpl tier = new TierImpl(transTierId, "", transcription, type);
                    transcription.addTier(tier);
                    tier.setDefaultLocale(null);
                    t = tier;
				} else {
					// take the first top level tier to work on
					List<TierImpl> topTiers = transcription.getTopTiers();
					if (!topTiers.isEmpty()) {
						t = topTiers.get(0);
					}
				}
			}
			if (t != null) {
				curTier = t;
				// create pseudo-annotations for gaps
				fillAnnotationList(t);
			}
		}
		
		recalculateAllRowHeights();
	}
	
	/**
	 * Creates a table filled with real annotations and pseudo-annotations for 
	 * the gaps.
	 * 
	 * @param t the tier
	 */
	private void fillAnnotationList(Tier t) {
		if (t != null) {
			List<AbstractAnnotation> annos = (List<AbstractAnnotation>) t.getAnnotations();
			segmentList = new ArrayList<TaSAnno>();
			long timeSoFar = 0L;
			long mediaDuration = getMediaDuration();
			if (mediaDuration == 0) {// if the player is not initialized yet?
				mediaDuration = Long.MAX_VALUE;
			}
			
			if (annos.isEmpty()) {
					segmentList.add(new TaSAnno(timeSoFar, 
						mediaDuration));
			} else {
				for (int i = 0; i < annos.size(); i++) {
					AbstractAnnotation aa = annos.get(i);
					if (aa.getBeginTimeBoundary() > timeSoFar) {
						segmentList.add(new TaSAnno(timeSoFar, aa.getBeginTimeBoundary()));
					}
					segmentList.add(new TaSAnno(aa));
					timeSoFar = aa.getEndTimeBoundary();
					
					if (i == annos.size() - 1) {
						if (aa.getEndTimeBoundary() < mediaDuration) {
							segmentList.add(new TaSAnno(aa.getEndTimeBoundary(), mediaDuration));
						}
					}
				}
			}
			
			for (TaSAnno a2d : segmentList) {
				tableModel.addRow(new Object[]{a2d});
			}
		}
	}
	
	/**
	 * Inserts a new TaSAnno object encapsulating an annotation in the table and
	 * modifies or removes "gap" annotations that possibly exist in the time span of
	 * the new annotation. 
	 * 
	 * @param tasAnno a new encapsulated annotation
	 */
	private void insertNewAnnotation(TaSAnno tasAnno) {
		if (tasAnno.getAnnotation() == null) {
			// this is currently not expected to be the case
			ClientLogger.LOG.warning("Cannot insert an empty (gap) annotation.");
			return;
		}
		
		int[] overlRows = getOverlappingRows(tasAnno.getBeginTime(), tasAnno.getEndTime());
		
		if (overlRows[0] == -1 || overlRows[1] == -1) {
			ClientLogger.LOG.warning("Error inserting a new row, there should be at least one "
					+ "time-overlapping row already in the table");
			return;
		}
		
		if (overlRows[0] == overlRows[1]) {
			// should be an exact time match, should be a gap
			TaSAnno curRow = (TaSAnno) tableModel.getValueAt(overlRows[0], 0);
			if (curRow.getBeginTime() == tasAnno.getBeginTime() && 
					curRow.getEndTime() == tasAnno.getEndTime()) {
				tableModel.removeRow(overlRows[0]);
				tableModel.insertRow(overlRows[0], new Object[]{tasAnno});
				calculateRowHeight(overlRows[0]);
			} else if (curRow.getBeginTime() == tasAnno.getBeginTime()) {// && curRow.getEndTime() > tasAnno.getEndTime()
				// insert the new annotation, modify the existing annotation
				curRow.setBeginTime(tasAnno.getEndTime());
				tableModel.insertRow(overlRows[0], new Object[]{tasAnno});
				calculateRowHeight(overlRows[0]);
				calculateRowHeight(overlRows[0] + 1);
			}			
			else {
				ClientLogger.LOG.warning("Cannot replace one single row because the time values "
						+ "of the new annotation are not equal to the existing ones.");
			}
		} else {//error
			ClientLogger.LOG.warning("There are more than one overlapping segments on this tier "
					+ "cannot insert the new annotation");
		}
	}
	
	/**
	 * Returns an int array of size 2, indicating the first and the last index of overlapping
	 * rows. These indexes can be the same. If one of the indexes is -1 there is no overlap 
	 * (which would be an error).
	 * 
	 * @param bt
	 * @param et
	 * @return an array of size 2
	 */
	private int[] getOverlappingRows(long bt, long et) {
		// is there a better way of finding the rows than iterating the rows of the model?
		int startIndex = -1, endIndex = -1;
		for (int i = 0 ; i < tableModel.getRowCount(); i++) {
			TaSAnno rowAnno = (TaSAnno) tableModel.getValueAt(i, 0);
			if (rowAnno.getEndTime() <= bt) {
				continue;
			}
			if (rowAnno.getEndTime() > bt && rowAnno.getBeginTime() < et) {
				if (startIndex == -1) {
					startIndex = i;
				}
				if (i == tableModel.getRowCount() - 1) {
					endIndex = i;
				}
			}
			if (rowAnno.getBeginTime() >= et) {
				if (endIndex == -1) {
					endIndex = i - 1;
				}
				break;
			}
		}
		
		return new int[]{startIndex, endIndex};
	}
	
	/**
	 * Returns the row for the given time value.
	 * 
	 * @param time the time to look up the row for
	 * @return the row index or -1 if not found
	 */
	public int getRowAtTime(long time) {
		for (int i = 0 ; i < tableModel.getRowCount(); i++) {
			TaSAnno rowAnno = (TaSAnno) tableModel.getValueAt(i, 0);
			if (time >= rowAnno.getBeginTime() && time < rowAnno.getEndTime()) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Check whether the annotation is in the table and returns the row index, -1 if not in the table.
	 * 
	 * @param a the annotation
	 * @return the row index or -1
	 */
	private int getRowForAnnotation(Annotation a) {
		for (int i = 0 ; i < tableModel.getRowCount(); i++) {
			TaSAnno rowAnno = (TaSAnno) tableModel.getValueAt(i, 0);
			if (rowAnno.getAnnotation() == a) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * When the times of an annotation changed, neighboring virtual annotations may have to be 
	 * updated or created.
	 *  
	 * @param annotation the annotation hat changed
	 */
	private void updateNeighborSegments(Annotation annotation) {
		if (annotation != null) {
			// find the row for the annotation
			int rowIndex = getRowForAnnotation(annotation);
			
			if (rowIndex == -1) {
				ClientLogger.LOG.warning("Error updating neighboring rows, the annotation was not found in the table");
				return;
			}
			
			// check before and after this annotation if there are gaps
			TaSAnno prevAnno = null;
			TaSAnno nextAnno = null;
			
			if (rowIndex > 0) {
				prevAnno = (TaSAnno) tableModel.getValueAt(rowIndex - 1, 0);
			}
			if (rowIndex < tableModel.getRowCount() - 1) {
				nextAnno = (TaSAnno) tableModel.getValueAt(rowIndex + 1, 0);
			}
			
			if (prevAnno == null) {
				if (annotation.getBeginTimeBoundary() > 0) {// unlikely this can happen
					tableModel.insertRow(0, new Object[]{new TaSAnno(0, 
							annotation.getBeginTimeBoundary())});
					calculateRowHeight(rowIndex);
					rowIndex++;
					//calculateRowHeight(rowIndex);// 1
				}
			} else {// compare with previous annotation
				if (annotation.getBeginTimeBoundary() > prevAnno.getEndTime()) {
					if (prevAnno.getAnnotation() != null) {
						// insert before
						tableModel.insertRow(rowIndex, new Object[]{new TaSAnno(prevAnno.getEndTime(), 
								annotation.getBeginTimeBoundary())});
						calculateRowHeight(rowIndex);
						rowIndex++;
					} else {
						prevAnno.setEndTime(annotation.getBeginTimeBoundary());
					}
				} else if (annotation.getBeginTimeBoundary() < prevAnno.getEndTime()) {// unlikely
					if (prevAnno.getAnnotation() == null) {
						prevAnno.setEndTime(annotation.getBeginTimeBoundary());
						// if the gap duration == 0, remove it
						if (prevAnno.getBeginTime() == prevAnno.getEndTime()) {
							tableModel.removeRow(rowIndex - 1);
						}
					} // the else case would be an error
				}
			}
			
			if (nextAnno == null) {// last annotation in the list
				if (annotation.getEndTimeBoundary() < getMediaDuration()) {
					// add row to the end
					tableModel.addRow(new Object[]{new TaSAnno(annotation.getEndTimeBoundary(), 
							getMediaDuration())});
					calculateRowHeight(tableModel.getRowCount() - 1);
				}
			} else {// compare with next annotation
				if (annotation.getEndTimeBoundary() < nextAnno.getBeginTime()) {
					if (nextAnno.getAnnotation() != null) {
						// insert after
						tableModel.insertRow(rowIndex + 1, new Object[]{new TaSAnno(annotation.getEndTimeBoundary(), 
								nextAnno.getBeginTime())});
						calculateRowHeight(rowIndex + 1); 
					} else {
						nextAnno.setBeginTime(annotation.getEndTimeBoundary());
					}
				} else if (annotation.getEndTimeBoundary() > nextAnno.getBeginTime()) {
					if (nextAnno.getAnnotation() == null) {
						nextAnno.setBeginTime(annotation.getEndTimeBoundary());
						// if the gap duration == 0, remove it
						if (nextAnno.getBeginTime() == nextAnno.getEndTime()) {
							tableModel.removeRow(rowIndex + 1);
						}
					} else { 
						// this can happen when annotations are merged. The remove annotation ACMEvent in that case 
						// is too generic to identify which annotation has been removed
						if (nextAnno.getBeginTime() == nextAnno.getEndTime()) {
							tableModel.removeRow(rowIndex + 1);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Initiates a recalculation of the height of the row containing the specified
	 * annotation.
	 * 
	 * @param annotation
	 */
	private void updateRow(Annotation annotation) {
		if (annotation != null) {
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				TaSAnno rowAnno = (TaSAnno) tableModel.getValueAt(i, 0);
				if (rowAnno.getAnnotation() == annotation) {
					calculateRowHeight(i);
					break;
				}
			}
		}
	}
	
	/**
	 * When an annotation has been removed from the tier, remove the corresponding row from the 
	 * view and replace it by an empty virtual annotation.
	 * 
	 * @param remAnn the removed annotation
	 */
	private void annotationRemoved(Annotation remAnn) {
		if (remAnn != null) {// superfluous?
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				TaSAnno rowAnno = (TaSAnno) tableModel.getValueAt(i, 0);
				if (rowAnno.getAnnotation() == remAnn) {
					//tableModel.removeRow(i);
					
					// check before and after this annotation if there are gaps
					boolean gapBefore = false;
					boolean gapAfter = false;
					TaSAnno prevAnno = null;
					TaSAnno nextAnno = null;
					
					if (i > 0) {
						prevAnno = (TaSAnno) tableModel.getValueAt(i - 1, 0);
						gapBefore = (prevAnno.getAnnotation() == null); 
					}
					if (i < tableModel.getRowCount() - 1) {
						nextAnno = (TaSAnno) tableModel.getValueAt(i + 1, 0);
						gapAfter = (nextAnno.getAnnotation() == null);
					}
					
					if (gapBefore && gapAfter) {
						// remove row at i, remove row at i + 1, update gap row at i - 1
						tableModel.removeRow(i + 1);// first delete the next
						tableModel.removeRow(i);
						prevAnno.setEndTime(nextAnno.getEndTime());
						calculateRowHeight(i - 1);
					} else if (gapBefore) {
						tableModel.removeRow(i);
						prevAnno.setEndTime(remAnn.getEndTimeBoundary());
						calculateRowHeight(i - 1);
					} else if (gapAfter) {
						tableModel.removeRow(i);
						nextAnno.setBeginTime(remAnn.getBeginTimeBoundary());
						calculateRowHeight(i);// nextAnno now in row i
					} else { // no adjacent gaps
						tableModel.removeRow(i);
						tableModel.insertRow(i, new Object[]{new TaSAnno(remAnn.getBeginTimeBoundary(), 
								remAnn.getEndTimeBoundary())});
						calculateRowHeight(i);
					}
					
					break;
				}
			}
		}
	}
	
	/**
	 * After edit actions with an undefined number of modified annotations the table needs to
	 * be reloaded or updated (by comparing the current TaSAnno objects with the current 
	 * Annotations on the tier).
	 * Current implementation: remove all rows from the table model, fill it with the annotations
	 * of the current tier, calculate row heights.
	 * Relatively expensive operation.
	 */
	private synchronized void reloadAnnotationTable() {
		// store current state
		int selRow = annoTable.getSelectedRow();
		long annoBT = 0, annoET = 0;
		if (selRow > -1) {
			TaSAnno curSelAnno = (TaSAnno) annoTable.getValueAt(selRow, 0);
			annoBT = curSelAnno.getBeginTime();
			annoET = curSelAnno.getEndTime();
		}

		// remove all rows
		clearAnnotationTable();
		
		fillAnnotationList(curTier);
		recalculateAllRowHeights();
		// set selected row
		selRow = getRowAtTime((annoBT + annoET) / 2);
		if (selRow > 0) {
			annoTable.getSelectionModel().setSelectionInterval(selRow, selRow);
			// start editing?
		}
	}
	
	/**
	 * Removes all the rows from the table.
	 * Alternatively a new model could be created and set to the table and the table 
	 * newly configured etc.
	 */
	private synchronized void clearAnnotationTable() {
		if (tableModel != null) {
			for (int j = tableModel.getRowCount() - 1; j >=0; j--) {
				tableModel.removeRow(j);
			}
		}
	}
	
	/**
	 * Calculates (and sets) the height of the specified row. Currently relies on
	 * the preferred size of the rendering component.
	 * 
	 * @param row the row
	 * @return the preferred height of the row
	 */
	public int calculateRowHeight(int row) {
		if (row < 0 || row >= annoTable.getRowCount()) {
			throw new IllegalArgumentException("There is no row with index: " + row);
		}
		
		TaSCellPanel renderComp = (TaSCellPanel) cellRenderer.getTableCellRendererComponent(annoTable, 
				tableModel.getValueAt(row, 0), false, false, row, 0);
		int prefHeight = renderComp.getPreferredSize().height;
		annoTable.setRowHeight(row, prefHeight);// or do not set it here?
		if (row == annoTable.getRowCount() - 1) {
			annoTable.setRowHeight(row, 2 * prefHeight);// extra space for the last row
		}
		
		return prefHeight;
	}
	
	public void recalculateAllRowHeights() {
		for (int i = 0; i < annoTable.getRowCount(); i++) {
			calculateRowHeight(i);
		}
		
		annoTable.revalidate();
	}
	
	@Override
	public void controllerUpdate(ControllerEvent event) {
		// could take the current, cached segment and check if the crosshair
		// is still in the same segment. Otherwise find the segment we are now in and highlight it, repaint
		long mediaTime = getMediaTime();
		cellEditor.updatMediaTime(mediaTime);
		cellRenderer.updatMediaTime(mediaTime);
		int editingRow = annoTable.getEditingRow();
		
		if (editingRow > -1) {
			cellEditor.repaint();
			// this cell probably is already in view, but check
			//...
		}
		int medRow = getRowAtTime(mediaTime);
		if(medRow > -1 && medRow != rowAtMediaTime) {
			scrollToRow(medRow);
			rowAtMediaTime = medRow;
		}
		
		repaint();
	}

	@Override
	public void updateSelection() {
		repaint();
	}

	
	/**
	 * Overrides the super implementation in case of the continuous play back mode
	 * 
	 * @see mpi.eudico.client.annotator.viewer.AbstractViewer#setActiveAnnotation(mpi.eudico.server.corpora.clom.Annotation)
	 */
	@Override
	public void setActiveAnnotation(Annotation annotation) {
		if (!continuousPlayMode) {
			super.setActiveAnnotation(annotation);
		} else {
			Command c = ELANCommandFactory.createCommand(transcription,
		        ELANCommandFactory.ACTIVE_ANNOTATION);
		    c.execute(getViewerManager(), new Object[] {annotation, Boolean.FALSE});
		}
	}

	@Override
	public void updateActiveAnnotation() {
		if (annoTable != null) {
			
		}
	}

	@Override
	public void updateLocale() {
		if (mergePreviousMouseAction != null) {
			mergePreviousMouseAction.putValue(Action.NAME, ElanLocale.getString(ELANCommandFactory.MERGE_ANNOTATION_WB));
			mergeNextMouseAction.putValue(Action.NAME, ElanLocale.getString(ELANCommandFactory.MERGE_ANNOTATION_WN));
		}

	}

	@Override
	public void preferencesChanged() {
		// font size is restored via display settings etc.
//		if (annoTable != null && annoTable.getFont() != null) {
//			annoTable.setFont(annoTable.getFont().deriveFont((float) primaryFontSize));
//		}

		if (curTier == null && transcription.getTopTiers().size() > 0) {
			setTier(transcription.getTopTiers().get(0));
		}
	}

	/**
	 * In response to edit actions the table possibly has to be updated in several ways. The height of rows might 
	 * need to be recalculated, new gaps might need to be filled with virtual annotations or virtual annotations
	 * might need to be modified or removed.
	 */
	@Override
	public void ACMEdited(ACMEditEvent e) {
		//System.out.println("ACM Event: " + e.getOperation() + " M: " + e.getModification() + " I: " + e.getInvalidatedObject());
		cellEditor.cancelCellEditing();// more elegant way to stop editing before modifying the table?
		// handle (for now) new annotation, modify annotation, delete annotation, delete tier? (if it is the current tier)
		switch (e.getOperation()) {
		case ACMEditEvent.ADD_ANNOTATION_HERE:
			// add a new TaSAnno object possibly in place of an empty placeholder and remove or update empty placeholder
            if (e.getInvalidatedObject() instanceof TierImpl &&
                    e.getModification() instanceof Annotation) {
            	if (e.getInvalidatedObject() == curTier) {
            		insertNewAnnotation(new TaSAnno( (Annotation) e.getModification()));
            	}
            }
			break;
		
		case ACMEditEvent.CHANGE_ANNOTATION_TIME:
			// find the row, check neighboring annotations/cells to see if they have to be updated
            if (e.getModification() instanceof Annotation) {
            	Annotation modAnno = (Annotation) e.getModification();
            	if (modAnno.getTier() == curTier) {
            		updateNeighborSegments(modAnno);
            	}
            }
			break;
		
		case ACMEditEvent.CHANGE_ANNOTATION_VALUE:
			// find the row, calculate the height, re-validate the table
            if (e.getInvalidatedObject() instanceof Annotation) {
            	updateRow((Annotation) e.getInvalidatedObject());
            }
			break;
			
		case ACMEditEvent.CHANGE_ANNOTATIONS:
			// it would be best if this can be avoided
			reloadAnnotationTable();
			break;
			
		case ACMEditEvent.REMOVE_ANNOTATION:
			// find the row, remove it, update/insert placeholder
			if (e.getModification() instanceof Annotation) {
				Annotation remAnn = (Annotation) e.getModification();
				// check if the tier is in the table, currently only one tier
				if (remAnn.getTier() == curTier) {
					annotationRemoved(remAnn);
				}
			}
			break;
		case ACMEditEvent.REMOVE_TIER:
			// check it the removed tier is (one of) the current tier(s); if so remove it from the table
			if (e.getModification() instanceof TierImpl) {
				if (e.getModification() == curTier) {
					// clear the table
					tableModel.setRowCount(0);
					this.revalidate();// necessary?
				}
			}
			break;
		case ACMEditEvent.CHANGE_TIER:
			// if the changed tier is (one of) the current tier(s) update tier and participant labels, if necessary 
		default:
			break;
		}
	}
	
	/**
	 * List Selection event handling
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		//System.out.println("List Selection Event: F: " + e.getFirstIndex() + " L: " + e.getLastIndex());
		if (!e.getValueIsAdjusting()) {		
			int selRow = annoTable.getSelectedRow();
			//System.out.println("Selected Row: " + selRow);
			if (selRow > -1) {
				TaSAnno selAnno = (TaSAnno) annoTable.getValueAt(selRow, 0);
				setSelection(selAnno.getBeginTime(), selAnno.getEndTime());
				if (!continuousPlayMode || !playerIsPlaying()) {
					setMediaTime(selAnno.getBeginTime());
				}
				setActiveAnnotation(selAnno.getAnnotation()); // can be null				
				// alternatively could override setActiveAnnotation and setMediaTime to check
				// for this flag and for the isPlaying state
//				if (continuousPlayMode) {
//					startPlayer();
//				}
				annoTable.repaint();
			}
		}		
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		int curWidth = getWidth();
		super.setBounds(x, y, width, height);
		if (curWidth != width) {
			recalculateAllRowHeights();
		}
		this.revalidate();
	}

	/**
	 * Sets the main font size for the viewer, i.e. the table
	 * 
	 * @param fontSize the new size of the font
	 */
	@Override
	public void setFontSize(int fontSize) {
		if (this.primaryFontSize != fontSize) {
			this.primaryFontSize = fontSize;
			if (annoTable != null) {
				annoTable.setFont(annoTable.getFont().deriveFont((float) this.primaryFontSize));
				if (annoTable.isEditing()) {
					((TaSCellPanel) cellEditor.getComponent()).setFont(annoTable.getFont());
				}
				recalculateAllRowHeights();
			}
		}
	}
	
	/**
	 * Returns the main font size for this viewer (the size of the text area in the table cells) 
	 * @return the primary font size
	 */
	@Override
	public int getFontSize() {
		return primaryFontSize;
	}
	
	/**
	 * Loads the preferred font for the specified tier.
	 * 
	 * @param tier the tier to load the font for. If null it restores the default font.
	 */
	private void loadTierFont(Tier tier) {
		if (tier != null) {
			Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", getViewerManager().getTranscription());
			if (fonts != null) {
				if (fonts.get(tier.getName()) != null) {
					editorPanel.setMainTierFont(fonts.get(tier.getName()));
					cellRenderer.setMainTierFont(fonts.get(tier.getName()));
				} else {
					editorPanel.setMainTierFont(Constants.DEFAULTFONT);
					cellRenderer.setMainTierFont(Constants.DEFAULTFONT);
				}
			} else {
				editorPanel.setMainTierFont(Constants.DEFAULTFONT);
				cellRenderer.setMainTierFont(Constants.DEFAULTFONT);
			}
		} else {
			editorPanel.setMainTierFont(null);
			cellRenderer.setMainTierFont(null);
		}
	}
	
	/**
	 * Checks if the given row is visible, scrolls the table if necessary.
	 * Based on scrollIfNeeded in AbstractGridViewer.
	 * 
	 * @param row the row to be made visible
	 */
	private void scrollToRow(int row) {
		if (row > -1) {
			synchronized (annoTable) {
				//System.out.println("Scroll 0: ViewSize: " + scrollPane.getViewport().getViewSize());
				Dimension viewSize = scrollPane.getViewport().getViewSize();
				Rectangle viewRect = scrollPane.getViewport().getViewRect();
				Rectangle cellRect = annoTable.getCellRect(row, 0, true);
				//System.out.println("Scroll 1: View: " + viewRect + " Cell: " + cellRect);
				
				if (!viewRect.contains(cellRect)) {
					// reuse the cellRect Rectangle to calculate the new rectangle that should be made visible 
					switch (scrollRowToLocation) {
					case SwingConstants.TOP:
						cellRect.height = (viewRect.height - cellRect.height);
						
						break;
					case SwingConstants.CENTER:
						cellRect.height += (viewRect.height - cellRect.height) / 2;
						
						break;
					case SwingConstants.BOTTOM:
						cellRect.height += 10;
						
						break;
						
						default:
							
					}
					
					if (cellRect.y + cellRect.height > viewSize.height) {
						//System.out.println("Scroll: Adjust Cell Height1: " + cellRect);
						cellRect.height = viewSize.height - cellRect.y;
						//System.out.println("Scroll: Adjust Cell Height2: " + cellRect);
					}
					//cellRect.height = (viewRect.height - cellRect.height) / 2;//??
					//System.out.println("Scroll 2: View: " + viewRect + " Cell: " + cellRect);
					annoTable.scrollRectToVisible(cellRect);
				}
			}	
		}
	}
	
	/**
	 * Triggers an update of the row heights after e.g. resizing of the viewer.
	 */
	private class ViewerComponentListener implements ComponentListener {

		@Override
		public void componentResized(ComponentEvent e) {
			recalculateAllRowHeights();
//			System.out.println("Resized...");
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			
		}

		@Override
		public void componentShown(ComponentEvent e) {
			recalculateAllRowHeights();
//			System.out.println("Shown...");
		}

		@Override
		public void componentHidden(ComponentEvent e) {
			
		}
	}
	
	/**
	 * A listener to document changes so that the height of the editing row in the table can be
	 * recalculated. 
	 */
	private class CellDocumentListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			//System.out.println("Inserted into Document at row: " + annoTable.getEditingRow());
			//System.out.println("Length of insertion: " + e.getLength());
			// could also try to get the location of the last character/line in the view
			/*
			try {
				Rectangle lastRect = editorPanel.getTextArea().modelToView(editorPanel.getTextArea().getText().length());
				curAreaHeight = lastRect.y;
				System.out.println("End rect: " + lastRect.y + " - " + lastRect.height);
			} catch (BadLocationException ble) {
				System.out.println("Bad: " + ble.getMessage());
			}
			*/
			// when a longer text has been pasted, the preferred size is not immediately updated, for some reason
			if (e.getLength() > 1) {
				editorPanel.getTextArea().revalidate();
				editorPanel.revalidate();
			}
			//curEditingRow = annoTable.getEditingRow();
			checkRowHeight(annoTable.getEditingRow());
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			//System.out.println("Removed from Document at row: " + annoTable.getEditingRow());
			checkRowHeight(annoTable.getEditingRow());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			// stub maybe check if font size is changed etc?
			//System.out.println("Change update from Document at row: " + annoTable.getEditingRow());
		}
		
		private void checkRowHeight(int row) {
			if (row > -1) {
				try { // sometimes IllegalArgumentExceptions are thrown for TextHitInfo in TextLayout
					int curHeight = editorPanel.getTextArea().getHeight();
					int curPrefHeight = editorPanel.getTextArea().getPreferredSize().height;
					//System.out.println("Cur H: " + curHeight + "  Pref H: " + curPrefHeight);
					if (curHeight != curPrefHeight) {
						if ( !(row == annoTable.getRowCount() - 1 && curHeight > curPrefHeight)) {
							annoTable.setRowHeight(row, annoTable.getRowHeight(row) + (curPrefHeight - curHeight));
							annoTable.revalidate();
							editorPanel.revalidate();
						}
					}
				} catch (Throwable t) {
					ClientLogger.LOG.warning("Cannot update row height: " + t.getMessage());
					//t.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * A mouse listener for showing and updating the popup menu.
	 *
	 */
	private class TableMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {	
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getSource() == annoTable) {
		        if ((SwingUtilities.isRightMouseButton(e) && (e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown())) 
		                || e.isPopupTrigger()) {
		        	int row = annoTable.rowAtPoint(e.getPoint());
		        	boolean mergePrevEnabled = false;
		        	boolean mergeNextEnabled = false;
		        	//if (row > -1 && row < annoTable.getRowCount()) {
		        	//if (row == annoTable.getSelectedRow()) {
		        		mergePrevEnabled = row > 0;
		        		mergeNextEnabled = row < annoTable.getRowCount() - 1;
		        	//}
		        	mergePreviousMouseAction.setSourceRow(row);
		        	mergePreviousMouseAction.setEnabled(mergePrevEnabled);
		        	mergeNextMouseAction.setSourceRow(row);
		        	mergeNextMouseAction.setEnabled(mergeNextEnabled);
					popupMenu.show(annoTable, e.getX(), e.getY());
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {	
		}

		@Override
		public void mouseEntered(MouseEvent e) {	
		}

		@Override
		public void mouseExited(MouseEvent e) {			
		}
		
	}

	/**
	 * SingleTierViewer methods (although it is not clear if this will remain a single
	 * tier viewer.
	 * Note: this viewer is no longer a SingleTierViewer
	 * @param tier the tier to select 
	 */
//	@Override
	public void setTier(Tier tier) {
		if (tier == curTier) {
			return;
		}
		if (curTier != null) {
			// stop cell editing
			cellEditor.stopCellEditing();
			clearAnnotationTable();
		}
		
		curTier = (TierImpl) tier;

		fillAnnotationList(tier);
		loadTierFont(tier);
		recalculateAllRowHeights();
	}

//	@Override
	public Tier getTier() {
		return curTier;
	}
	
	/**
	 * Sets the tier with the specified name to become the selected tier.
	 *  
	 * @param tierName
	 */
	public void setTierByName(String tierName) {
		TierImpl t = transcription.getTierWithId(tierName);
		if (t.getParentTier() == null) {
			setTier(t);
		}
	}
	
	/**
	 * 
	 * @return the current selected tier or null.
	 */
	public String getTierName() {
		if (curTier != null) {
			return curTier.getName();
		}
		
		return null;
	}

	/**
	 * @return the continuousPlayMode flag
	 */
	public boolean isContinuousPlayMode() {
		return continuousPlayMode;
	}

	/**
	 * @param continuousPlayMode the continuousPlayMode to set
	 */
	public void setContinuousPlayMode(boolean continuousPlayMode) {
		this.continuousPlayMode = continuousPlayMode;
	}
	
	
}
