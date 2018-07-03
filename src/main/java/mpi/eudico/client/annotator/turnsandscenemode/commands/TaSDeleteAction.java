package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.turnsandscenemode.TaSAnno;
import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * An action to delete an annotation, a wrapper around the "normal" 
 * undoable command for deleting an annotation.
 */
@SuppressWarnings("serial")
public class TaSDeleteAction extends AbstractAction {
	private TurnsAndSceneViewer viewer;

	/**
	 * @param viewer the viewer
	 */
	public TaSDeleteAction(TurnsAndSceneViewer viewer) {
		super();
		this.viewer = viewer;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (viewer != null) {
			deleteSegment();
		}
	}

	/**
	 * Deletes an annotation, a cell in the table, if this cell represents a real 
	 * annotation.
	 */
	private void deleteSegment() {
		int editingRow = viewer.getAnnotationTable().getEditingRow();
		
		//TODO could delete the selected row if no editing is going on
		//int selRow = viewer.getAnnotationTable().getSelectedRow();
		
		if (editingRow == -1) {// should not occur now?
			ClientLogger.LOG.info("A delete action occurred but it is unknown which cell to delete.");
			return;
		}
		
		TaSAnno curAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow, 0);
		Transcription transcription = viewer.getViewerManager().getTranscription();
		if (curAnno.getAnnotation() != null) {
			Command deleteCommand = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.DELETE_ANNOTATION);
			Object[] args = new Object[2];
			args[0] = ELANCommandFactory.getViewerManager(transcription);
			args[1] = curAnno.getAnnotation();
			deleteCommand.execute(curAnno.getAnnotation().getTier(), args);
		} else {
			ClientLogger.LOG.info("A delete action occurred but the active cell represents a gap.");
		}
	}
}
