package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;

/**
 * An action to cancel an edit action and to deactivate the current
 * segment. This action can be added to (the text area) of the cell editor. 
 */
@SuppressWarnings("serial")
public class CancelEditAction extends AbstractAction {
	private TurnsAndSceneViewer viewer;
	
	/**
	 * Constructor. 
	 * @param viewer notification of the cancel edit is currently via
	 * the viewer (an edit listener or similar would be better).
	 */
	public CancelEditAction(TurnsAndSceneViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (viewer != null) {
			if (viewer.getAnnotationTable().getEditingRow() > -1) {
				viewer.getAnnotationTable().getCellEditor().cancelCellEditing();
			}
		}
	}
}
