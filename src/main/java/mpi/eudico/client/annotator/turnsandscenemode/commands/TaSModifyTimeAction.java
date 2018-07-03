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
 * An action to modify a time boundary of an existing annotation.
 * Additional limitation is that a boundary can never be moved beyond the 
 * boundaries if the adjacent previous or next annotation.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TaSModifyTimeAction extends AbstractAction {
	private TurnsAndSceneViewer viewer;
	
	/**
	 * @param viewer
	 */
	public TaSModifyTimeAction(TurnsAndSceneViewer viewer) {
		super();
		this.viewer = viewer;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (viewer != null) {
			modifyTime();
		}
	}

	/**
	 * Updates the segmentation time of the source annotation. The new start or end time
	 * will be the current media time, provided the current media time is within the boundaries
	 * of either the previous adjacent annotation or the next adjacent annotation.
	 */
	private void modifyTime() {
		int editingRow = viewer.getAnnotationTable().getEditingRow();
		if (editingRow == -1) {// should not occur
			ClientLogger.LOG.info("A modify time action occurred but it is unknown in which cell.");
			return;
		}
		TaSAnno curAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow, 0);
		Transcription transcription = viewer.getViewerManager().getTranscription();

		if (curAnno.getAnnotation() != null) {
			long curTime = viewer.getMediaTime();
			if (curTime >= curAnno.getBeginTime() && curTime <= curAnno.getEndTime()) {
				ClientLogger.LOG.info("Cannot update the time of the cell; media time within the current boundaries.");
				return;
			}
			if (curTime > curAnno.getEndTime()) {
				if (editingRow < viewer.getAnnotationTable().getRowCount() - 1) {
					TaSAnno nextAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow + 1, 0);
					if (curTime < nextAnno.getEndTime()) {
						// modify time command
						Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MODIFY_ANNOTATION_TIME);
						com.execute(curAnno.getAnnotation(), new Object[]{curAnno.getBeginTime(), curTime});
						// wait for ACMEditEvent
					}
				} else {
					ClientLogger.LOG.info("Cannot update the end time of the last cell");
				}
			} else {// curTime < curAnno.getBeginTime()
				if (editingRow > 0) {
					TaSAnno prevAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow - 1, 0);
					if (curTime > prevAnno.getBeginTime()) {
						// modify time command
						Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MODIFY_ANNOTATION_TIME);
						com.execute(curAnno.getAnnotation(), new Object[]{curTime, curAnno.getEndTime()});
						// wait for ACMEditEvent
					}
				}
			}
		}
	}
}
