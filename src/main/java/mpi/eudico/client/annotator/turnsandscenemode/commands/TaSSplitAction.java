package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.turnsandscenemode.TaSAnno;
import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * An action that initiates a "split" action if a specific keystroke occurred.
 * Splitting always involves the creation of a new annotation and sometimes also
 * modification of an existing annotation.
 */
@SuppressWarnings("serial")
public class TaSSplitAction extends AbstractAction {
	private TurnsAndSceneViewer viewer;
	
	public TaSSplitAction(TurnsAndSceneViewer viewer) {
		super();
		this.viewer = viewer;
	}

	/**
	 * Performs the split action, interacts with the viewer and table etc.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (viewer != null) {
			splitAnnotation();
		}
	}

	/**
	 * Called by the special split/commit action that has been added to the text area.
	 * 
	 * 1) If there was not an annotation yet in the editing row, an annotation will be created
	 * with the text in the cell and with the media time as end time. If the caret is not at the
	 * end of the text 2 annotations will be created in the current segment.
	 * 
	 * 2) If there was already an annotation in the row it will be split at the text position and 
	 * at the current media position. 
	 * Some trade-off decisions have to be made when the media time is not within the current
	 * segment (if that can happen at all).
	 */
	private void splitAnnotation() {
		//System.out.println("Split Event Occurred");
		// get the editor panel, get access to the text area, get text and caret location
		// get current time, split if possible, warn if not
//		System.out.println("Split: Text: " + viewer.getEditor().getTextArea().getText());
//		System.out.println("Split: Text Length: " + viewer.getEditor().getTextArea().getText().length());
//		System.out.println("Split: Caret: " + viewer.getEditor().getTextArea().getCaretPosition());
//		System.out.println("Split: Time: " + viewer.getMediaTime());
		String curText = viewer.getEditor().getTextArea().getText();
		int caretPos = viewer.getEditor().getTextArea().getCaretPosition();
		long curMediaTime = viewer.getMediaTime();
		int editingRow = viewer.getAnnotationTable().getEditingRow();
		// add a test for -1, should not happen
		if (editingRow < 0) {
			// log a message, return and do nothing
			return;
		}
		TaSAnno curAnno = (TaSAnno) viewer.getAnnotationTable().getValueAt(editingRow, 0);
		// one or both of the next calls?
		viewer.getEditor().getTextArea().transferFocusUpCycle();
		if (viewer.getAnnotationTable().getCellEditor() != null) {// this should be superfluous here
			viewer.getAnnotationTable().getCellEditor().stopCellEditing();
		}
		
		// now create editing action(s) 
		// if the media time is not within the current segment, no splitting can be done
		boolean shouldSplit = curMediaTime > curAnno.getBeginTime() && curMediaTime < curAnno.getEndTime();
		Transcription transcription = viewer.getViewerManager().getTranscription();
		if (curAnno.getAnnotation() != null) {
			// edit an existing annotation and possibly create a new one
			// only allow splitting of an annotation when it has no dependent annotations in this mode
			boolean annHasDepending = !((AbstractAnnotation) curAnno.getAnnotation()).getParentListeners().isEmpty();
			
			if (!shouldSplit || (shouldSplit && annHasDepending)) {// do the same in both cases, modify the text
				if (shouldSplit && annHasDepending) {
					ClientLogger.LOG.warning("Splitting of annotations that have depending annotations is not supported.");
					// show a message dialog
				}
				// no way to split, ignore the caret position, check if the text changed 
				if (curText != null && !curText.equals(curAnno.getAnnotation().getValue())) {
					Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MODIFY_ANNOTATION);
					com.execute(curAnno.getAnnotation(), new Object[]{curAnno.getAnnotation().getValue(), curText});
					// the viewer will receive an ACM event and should update the TaSAnno object in the table
				}
			} else /*if (shouldSplit && !annHasDepending)*/ {	
				Command splitCom = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.SPLIT_ANNOTATION);
				Object[] args = new Object[4];
				args[0] = curAnno.getAnnotation();
				args[1] = curMediaTime;
				args[2] = curText;
				args[3] = caretPos;
				splitCom.execute(transcription, args);
			}
		} else { // a gap
			// create one or two new annotations
			long splitTime = shouldSplit ? curMediaTime : -1;
			Command twoCom = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.NEW_ANNOTATIONS_IN_GAP);
			twoCom.execute(viewer.getTier(), new Object[]{curAnno.getBeginTime(), curAnno.getEndTime(), splitTime, curText, caretPos});
		}
		
		/*
		* Sometimes there is no new annotation created, so the editing cannot move to the next one.
		* If this happens in the middle of the annotations, the test below might not be the correct one.
		* Maybe the increment to editingRow should be calculated individually in each of the above cases.
		* (But what I've seen so far doesn't seem bad; hitting enter in an annotation without splitting
		* it moves to the next one.)
		*/
		int newEditingRow = Math.min(editingRow + 1, viewer.getAnnotationTable().getRowCount() - 1);
		viewer.getAnnotationTable().getSelectionModel().setSelectionInterval(newEditingRow, newEditingRow);
		viewer.getAnnotationTable().editCellAt(newEditingRow, 0);
		viewer.getEditor().startEditing();
	}
}
