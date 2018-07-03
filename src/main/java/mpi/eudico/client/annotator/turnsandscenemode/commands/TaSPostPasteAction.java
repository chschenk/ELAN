package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.turnsandscenemode.TaSAnno;
import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;
import mpi.eudico.client.annotator.util.ClientLogger;


/**
 * An action invoked after a "paste" action has occurred. 
 * 
 * The current implementation is that the current text 
 * (after the paste event) is committed to the annotation (which will
 * be created in case of a paste action in a "gap").
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class TaSPostPasteAction extends AbstractAction {
	private TurnsAndSceneViewer viewer;
	
	public TaSPostPasteAction(TurnsAndSceneViewer viewer) {
		super();
		this.viewer = viewer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (viewer != null) {
			pasteAction();
		}
	}
	
	/**
	 * Commits the text of the current cell after a "Paste" action. Commits to the current annotation
	 * or creates a new annotation in case of a gap. 
	 */
	private void pasteAction() {
		int editingRow = viewer.getAnnotationTable().getEditingRow();
		if (editingRow == -1) {// should not occur
			ClientLogger.LOG.info("A paste action occurred but it is unknown in which cell.");
			return;
		}
		TaSAnno curAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow, 0);
		String curText = viewer.getEditor().getTextArea().getText();
		if (curAnno.getAnnotation() != null) {
			// commit the current text
			if (curText != null && !curText.equals(curAnno.getAnnotation().getValue())) {
				Command com = ELANCommandFactory.createCommand(viewer.getViewerManager().getTranscription(), 
						ELANCommandFactory.MODIFY_ANNOTATION);
				com.execute(curAnno.getAnnotation(), new Object[]{curAnno.getAnnotation().getValue(), curText});
			}
		} else {// create an annotation
			// create one or two new annotations
			int caretPos = viewer.getEditor().getTextArea().getCaretPosition();
			long splitTime = -1;
			Command twoCom = ELANCommandFactory.createCommand(viewer.getViewerManager().getTranscription(), 
					ELANCommandFactory.NEW_ANNOTATIONS_IN_GAP);
			twoCom.execute(viewer.getTier(), new Object[]{curAnno.getBeginTime(), curAnno.getEndTime(), 
				splitTime, curText, caretPos});
		}
	}
}
