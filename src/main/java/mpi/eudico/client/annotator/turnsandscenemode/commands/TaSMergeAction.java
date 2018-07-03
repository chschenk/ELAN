package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.turnsandscenemode.TaSAnno;
import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * An action class to initiate a merge action between two cells in the annotation table of the viewer.
 * Since gaps or "virtual annotations" are in the table as well, the merge actions need to be slightly
 * different from the merge actions in the normal annotation mode.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TaSMergeAction extends AbstractAction {
	private TurnsAndSceneViewer viewer;
	public enum MERGE_ROW {
		PREVIOUS,
		NEXT
	}
	private MERGE_ROW mergeWith;

	/**
	 * In the case a cell has been mouse-clicked it is possible to specify which row 
	 * has been clicked and is the source for merging (instead of the selected row)
	 */
	private int sourceRow = -1;
	
	/**
	 * Constructor 
	 * 
	 * @param viewer the viewer 
	 * @param rowToMergeWith merge with PREVIOUS or NEXT row
	 */
	public TaSMergeAction(TurnsAndSceneViewer viewer, MERGE_ROW rowToMergeWith) {
		super();
		this.viewer = viewer;
		mergeWith = rowToMergeWith;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (viewer != null) {
			if (mergeWith == MERGE_ROW.PREVIOUS) {
				mergeWithPrevious(sourceRow);
			} else {
				mergeWithNext(sourceRow);
			}
			// reset 
			sourceRow = -1;
		}
	}

	/**
	 * Sets the row from which to determine which annotation is either the previous or the next.
	 * 
	 * @param sourceRow the source row
	 */
	public void setSourceRow(int sourceRow) {
		this.sourceRow = sourceRow;
	}


	/**
	 * Merge with next action.
	 * 
	 * @param sourceRow the row from which the action originates, -1 in case of no selected row
	 */
	private void mergeWithNext(int sourceRow) {
		//int editingRow = annoTable.getEditingRow();
		int editingRow = viewer.getAnnotationTable().getSelectedRow();
		if (sourceRow > -1) {
			editingRow = sourceRow;
		}
		if (editingRow == -1) {// should not occur
			ClientLogger.LOG.info("Cannot merge with the next cell: there is no cell selected.");
			return;
		}
		if (editingRow == viewer.getAnnotationTable().getRowCount() - 1) {
			ClientLogger.LOG.info("Cannot merge with the next cell: the active cell is the last cell.");
			return;
		}
		//String curText = editorPanel.getTextArea().getText();//?? do something with the text which might have been changed?
		TaSAnno curAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow, 0);
		TaSAnno nextAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow + 1, 0);
		// one or both of the next calls?
		viewer.getEditor().getTextArea().transferFocusUpCycle();
		if (viewer.getAnnotationTable().getCellEditor() != null) {
			viewer.getAnnotationTable().getCellEditor().stopCellEditing();
		}
		
		Transcription transcription = viewer.getViewerManager().getTranscription();
		if (curAnno.getAnnotation() != null) {
			if (nextAnno.getAnnotation() != null) {
				// merge two annotations, reuse existing command
				// only allow merging if there are no dependent annotations for now (mainly because of to generic ACMEditEvent)
				boolean ann1HasDepending = !((AbstractAnnotation) curAnno.getAnnotation()).getParentListeners().isEmpty();
				boolean ann2HasDepending = !((AbstractAnnotation) nextAnno.getAnnotation()).getParentListeners().isEmpty();
				if (!ann1HasDepending && !ann2HasDepending) {
					Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MERGE_ANNOTATION_WN);
					com.execute(transcription, new Object[]{curAnno.getAnnotation(), Boolean.TRUE});
				} else {
					ClientLogger.LOG.info("Cannot merge with the next cell: the annotation has depending annotations.");
					JOptionPane.showMessageDialog(viewer, "Cannot merge with the next annotation: \nthe annotation has depending annotations.",
							"", JOptionPane.WARNING_MESSAGE);
				}
			} else {
				// merge with next gap == modify annotation time to include the next gap's time interval
				Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MODIFY_ANNOTATION_TIME);
				com.execute(curAnno.getAnnotation(), new Object[]{curAnno.getBeginTime(), nextAnno.getEndTime()});
			}
		} else { // the active cell is a gap, the next cell must be an annotation 
			if (nextAnno.getAnnotation() != null) {
				// modify the next annotation's time by adding the time interval of this gap
				Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MODIFY_ANNOTATION_TIME);
				com.execute(nextAnno.getAnnotation(), new Object[]{curAnno.getBeginTime(), nextAnno.getEndTime()});
			} else {
				ClientLogger.LOG.warning("Unexpected state of the sequence of annotations: cannot merge a gap with another gap");
			}
		}
		
		viewer.getAnnotationTable().editCellAt(editingRow, 0);
		viewer.getEditor().startEditing();
	}

	
	/**
	 * Merge with previous action.
	 * 
	 * @param sourceRow the row from which the action originates, -1 in case of no or unknown selected row
	 */
	private void mergeWithPrevious(int sourceRow) {
//		int editingRow = annoTable.getEditingRow();
		int editingRow = viewer.getAnnotationTable().getSelectedRow();
		
		if (sourceRow > -1) {
			editingRow = sourceRow;
		}
		if (editingRow == -1) {// should not occur
			ClientLogger.LOG.info("Cannot merge with the previous cell: there is no cell selected.");
			return;
		}
		if (editingRow == 0) {
			ClientLogger.LOG.info("Cannot merge with the previous cell: the active cell is the first cell.");
			return;
		}
		//String curText = editorPanel.getTextArea().getText();//?? do something with the text which might have been changed?
		TaSAnno curAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow, 0);
		TaSAnno prevAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow - 1, 0);
		// one or both of the next calls?
		viewer.getEditor().getTextArea().transferFocusUpCycle();
		if (viewer.getAnnotationTable().getCellEditor() != null) {
			viewer.getAnnotationTable().getCellEditor().stopCellEditing();
		}
		Transcription transcription = viewer.getViewerManager().getTranscription();

		if (curAnno.getAnnotation() != null) {
			if (prevAnno.getAnnotation() != null) {
				// merge two annotations, reuse existing command
				// only allow merging if there are no dependent annotations for now (mainly because of to generic ACMEditEvent)
				boolean ann1HasDepending = !((AbstractAnnotation) curAnno.getAnnotation()).getParentListeners().isEmpty();
				boolean ann2HasDepending = !((AbstractAnnotation) prevAnno.getAnnotation()).getParentListeners().isEmpty();
				if (!ann1HasDepending && !ann2HasDepending) {
					Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MERGE_ANNOTATION_WB);
					com.execute(transcription, new Object[]{curAnno.getAnnotation(), Boolean.FALSE});
				} else {
					ClientLogger.LOG.info("Cannot merge with the previous cell: the active annotation has depending annotations.");
					// show message
					JOptionPane.showMessageDialog(viewer, "Cannot merge with the previous annotation: \nthe annotation has depending annotations.",
							"", JOptionPane.WARNING_MESSAGE);
									}
			} else {
				// merge with previous gap == modify annotation time to include the previous gap's time interval
				Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MODIFY_ANNOTATION_TIME);
				com.execute(curAnno.getAnnotation(), new Object[]{prevAnno.getBeginTime(), curAnno.getEndTime()});
			}
		} else { // the active cell is a gap, the previous cell must be an annotation 
			if (prevAnno.getAnnotation() != null) {
				// modify the previous annotation's time by adding the time interval of this gap
				Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MODIFY_ANNOTATION_TIME);
				com.execute(prevAnno.getAnnotation(), new Object[]{prevAnno.getBeginTime(), curAnno.getEndTime()});
			} else {
				ClientLogger.LOG.warning("Unexpected state of the sequence of annotations: cannot merge a gap with another gap");
			}
		}
		
		viewer.getAnnotationTable().getSelectionModel().setSelectionInterval(editingRow - 1, editingRow - 1);
		viewer.getAnnotationTable().editCellAt(editingRow - 1, 0);
		viewer.getEditor().startEditing();
	}
	

}
