package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A Command to actually add a new Controlled Vocabulary to a Transcription.
 *
 * @author Han Sloetjes
 */
public class AddCVCommand implements UndoableCommand {
    private String commandName;

    // receiver
    private TranscriptionImpl transcription;

    // store the arguments for undo /redo
    private String cvName;
    private String description;
    
    private ControlledVocabulary conVoc;

    /**
     * Creates a new AddCVCommand instance
     *
     * @param name the name of the command
     */
    public AddCVCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Deletes the added CV from the Transcription.
     */
    @Override
	public void undo() {
    	if (transcription != null) {
    		transcription.removeControlledVocabulary(conVoc);
    	}
    }

    /**
     * The redo action. Adds the created CV to the Transcription.
     */
    @Override
	public void redo() {
		if (transcription != null) {
			if (transcription.getControlledVocabulary(conVoc.getName()) == null) {
				transcription.addControlledVocabulary(conVoc);
			}
		}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: 
     * 		  either: <ul><li>arg[0] = the name of the
     *        Controlled Vocabulary  (String)</li> <li>arg[1] = the
     *        description of the CV (String)</li> </ul>
     * 		or: <ul><li>arg[0] = the ControlledVocabulary object 
     * 		(Controlled Vocabulary)</li></ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        
        if (arguments.length == 2 && arguments[0] instanceof String) {
	        cvName = (String) arguments[0];
	
	        if (arguments[1] != null) {
	            description = (String) arguments[1];
	        }
	
	        if (transcription.getControlledVocabulary(cvName) == null) {
	            conVoc = new ControlledVocabulary(cvName,
	                    description);
	            transcription.addControlledVocabulary(conVoc);
	        }
        } else if (arguments[0] instanceof ControlledVocabulary) {
			conVoc = (ControlledVocabulary)arguments[0];
        	if (transcription.getControlledVocabulary(conVoc.getName()) == null) {
        		transcription.addControlledVocabulary(conVoc);
        	}
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
