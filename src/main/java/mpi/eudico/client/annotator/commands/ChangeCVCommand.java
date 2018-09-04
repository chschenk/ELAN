package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A Command to change a Controlled Vocabulary (name and or description).
 *
 * @author Han Sloetjes
 */
public class ChangeCVCommand implements UndoableCommand {
    private String commandName;

    // receiver; the transcription handles the change of the name or description 
    // of a CV
    private TranscriptionImpl transcription;

    // store the arguments for undo /redo
    private String oldCVName;
    private String oldCVDesc;
    private String cvName;
    private int langIndex;
    private String description;
    private ControlledVocabulary conVoc;

    /**
     * Creates a new ChangeCVCommand instance
     *
     * @param name the name of the command
     */
    public ChangeCVCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Restores the old values of the CV.
     */
    @Override
	public void undo() {
    	if (transcription != null && conVoc != null) {
    		transcription.changeControlledVocabulary(conVoc, oldCVName, langIndex, oldCVDesc);
    	}
    }

    /**
     * The redo action. 
     */
    @Override
	public void redo() {
		if (transcription != null && conVoc != null) {
			transcription.changeControlledVocabulary(conVoc, cvName, langIndex, description);
		}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the old name of the
     *        Controlled Vocabulary  (String)</li> <li>arg[1] = the old
     *        description of the CV (String)</li> <li>arg[2] = the new name of
     *        the Controlled Vocabulary  (String)</li> <li> arg[3] =  the new
     *        description of the CV (String)</li>
     *        <li>arg[4] the langIndex of the description</li>
     *        </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        oldCVName = (String) arguments[0];

        if (arguments[1] != null) {
            oldCVDesc = (String) arguments[1];
        }

        cvName = (String) arguments[2];

        if (arguments[3] != null) {
            description = (String) arguments[3];
        }
        
        langIndex = 0;
        if (arguments.length >= 4 && arguments[4] != null) {
        	langIndex = (Integer)arguments[4];
        }

        conVoc = transcription.getControlledVocabulary(oldCVName);

        if (conVoc != null) {
            transcription.changeControlledVocabulary(conVoc, cvName, langIndex, description);
        }
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }
}
