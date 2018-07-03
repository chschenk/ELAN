package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingConstants;

import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;

/**
 * Activates the next cell, previous cell, or the cell corresponding to the current
 * media time.
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class TaSActivateCellAction extends AbstractAction {
	/** one of the SwingConstants NEXT or PREVIOUS 
	 *  or CENTER for the cell at the current time */
	private int direction;
	private TurnsAndSceneViewer viewer;

	/**
	 * 
	 * @param name
	 * @param nextOrPrevious
	 */
	public TaSActivateCellAction(TurnsAndSceneViewer viewer, int nextOrPrevious) {
		super();
		this.viewer = viewer;
		direction = nextOrPrevious;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (viewer != null) {
			activateSegment(direction);
		}

	}
	
	/**
	 * A call to activate the previous or next segment/cell in the view.
	 * This method tries to place the caret at the end of the text of the segment that became active
	 * (As with the other responses to (keyboard) actions this implementation should be reconsidered.)
	 * 
	 * @param direction up or down, one of SwingConstants PREVIOUS or NEXT, 
	 * or cell at current time in case of SwingConstants CENTER
	 */
	private void activateSegment(int direction) {
		int editingRow = viewer.getAnnotationTable().getEditingRow();
		
		int nextEditingRow = 0;
		switch (direction) {
		case SwingConstants.CENTER:
			nextEditingRow = viewer.getRowAtTime(viewer.getMediaTime());
			
			if (nextEditingRow == -1) {
				return;
			}
			break;
		case SwingConstants.PREVIOUS:
			if (editingRow == 0) {
				return;
			}
			nextEditingRow = editingRow - 1;
			
			break;
		case SwingConstants.NEXT:
			if (editingRow == viewer.getAnnotationTable().getRowCount() - 1) {
				return;
			}
			nextEditingRow = editingRow + 1;
			break;
		default:
			// nothing	
		}
		
		if (viewer.getAnnotationTable().getCellEditor() != null) {
			viewer.getAnnotationTable().getCellEditor().stopCellEditing();
		}
		viewer.getAnnotationTable().getSelectionModel().setSelectionInterval(nextEditingRow, nextEditingRow);
		viewer.getAnnotationTable().editCellAt(nextEditingRow, 0);
		viewer.getEditor().startEditing();
		viewer.getEditor().getTextArea().setCaretPosition(viewer.getEditor().getTextArea().getText().length());
	}
}
