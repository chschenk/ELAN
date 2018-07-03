package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.turnsandscenemode.TaSAnno;
import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;
import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * An action invoked after a "cut" action has occurred. 
 * 
 * The current implementation notifies the viewer so that the current text 
 * can be committed to the annotation (otherwise a check should be made 
 * when a cell is de-activated to see if anything changed).
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class TaSPostCutAction extends AbstractAction {
	private TurnsAndSceneViewer viewer;

	public TaSPostCutAction(TurnsAndSceneViewer viewer) {
		super();
		this.viewer = viewer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (viewer != null) {
			cutAction();// in the current row/cell	
		}
	}

	/**
	 * Commits the text of the current cell (in case this is already an existing annotation) after 
	 * a "Cut" action occurred. Otherwise the text would still be in the annotation if the cell 
	 * is de-activated without e.g. hitting Enter. 
	 */
	private void cutAction() {
		int editingRow = viewer.getAnnotationTable().getEditingRow();
		if (editingRow == -1) {// should not occur
			ClientLogger.LOG.info("A cut action occurred but it is unknown in which cell.");
			return;
		}
		TaSAnno curAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow, 0);
		if (curAnno.getAnnotation() != null) {
			// commit the current text
			String curText = viewer.getEditor().getTextArea().getText();
			if (curText != null && !curText.equals(curAnno.getAnnotation().getValue())) {
				Command com = ELANCommandFactory.createCommand(viewer.getViewerManager().getTranscription(), 
						ELANCommandFactory.MODIFY_ANNOTATION);
				com.execute(curAnno.getAnnotation(), new Object[]{curAnno.getAnnotation().getValue(), 
					curText});
				// wait for the ACM event to update the TaSAnno object in the table, 
				// this deactivates the cell, not sure if this is always the desired behavior
			}
		}
	}
}
