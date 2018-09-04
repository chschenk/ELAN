package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.multiplefilesedit.UpdateTranscriptionsForECVDialog;

/**
 * Creates a dialog to configure and initiate updating of selected 
 * transcriptions after changes in external controlled vocabularies.
 *
 */
@SuppressWarnings("serial")
public class UpdateMultiForECVMA extends FrameMenuAction {
	
	public UpdateMultiForECVMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new UpdateTranscriptionsForECVDialog(frame, true).setVisible(true);	
	}
}
