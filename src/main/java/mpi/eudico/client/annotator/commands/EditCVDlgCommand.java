package mpi.eudico.client.annotator.commands;

import java.util.List;

import mpi.eudico.client.annotator.gui.EditCVDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A Command that brings up a JDialog for defining and editing  Controlled
 * Vocabularies.
 *
 * @author Han Sloetjes
 */
public class EditCVDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new edit cv command.
     *
     * @param name the name of the command
     */
    public EditCVDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the edit cv dialog.
     *
     * @param receiver the transcriptionImpl holding the controlled vocabularies
     * @param arguments null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl transcription = (TranscriptionImpl) receiver;
        new EditCVDialog(transcription).setVisible(true);
        
        // check if any of the CV (entries) has changed
        List<ControlledVocabulary> allCvs = transcription.getControlledVocabularies();
        for (int i = 0; i < allCvs.size(); i++) {
        	ControlledVocabulary cv = allCvs.get(i);
        	if (cv.isChanged()) {
        		transcription.setChanged();
        		break;
        	}
        }
        
    }

    /**
     * Returns the name of the command
     *
     * @return the name
     */
    @Override
	public String getName() {
        return null;
    }
}
