package mpi.eudico.client.annotator.commands;

import java.util.List;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A Command to delete a Controlled Vocabulary from the Transcription.
 *
 * @author Han Sloetjes
 */
public class DeleteCVCommand implements UndoableCommand {
    private String commandName;

    // receiver; the transcription 
    private TranscriptionImpl transcription;

    // store the arguments for undo /redo
    private ControlledVocabulary controlledVocabulary;
    
    //
    private List<LinguisticType> affectedTypes;

    /**
     * Creates a new DeleteCVCommand instance
     *
     * @param name the name of the command
     */
    public DeleteCVCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Adds the removed CV to the Transcription.
     */
    @Override
	public void undo() {
    	if (transcription != null && controlledVocabulary != null) {
			transcription.addControlledVocabulary(controlledVocabulary);
    		// restore the linguistic types that were using this CV before deletion
    		for (LinguisticType type : affectedTypes) {
    			type.setControlledVocabularyName(controlledVocabulary.getName());
    		}   		
    	}
    }

    /**
     * The redo action. Removes the CV from the Transcription.
     */
    @Override
	public void redo() {
    	if (transcription != null) {
    		transcription.removeControlledVocabulary(controlledVocabulary);
    	}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the  Controlled
     *        Vocabulary  (Controlled Vocabulary)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        controlledVocabulary = (ControlledVocabulary) arguments[0];
		
		if (transcription != null) {
			affectedTypes = transcription.getLinguisticTypesWithCV(
				controlledVocabulary.getName());
			transcription.removeControlledVocabulary(controlledVocabulary);
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
