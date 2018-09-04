package mpi.eudico.client.annotator.commands;

import java.util.List;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A Command to replace an existing Controlled Vocabulary in a Transcription
 * by a new Controlled Vocabulary.
 *
 * @author Han Sloetjes
 */
public class ReplaceCVCommand implements UndoableCommand {
    private String commandName;

    // receiver
    private TranscriptionImpl transcription;
    private ControlledVocabulary conVoc;
    private ControlledVocabulary newConVoc;

    /**
     * Creates a new ReplaceCVCommand instance
     *
     * @param name the name of the command
     */
    public ReplaceCVCommand(String name) {
        commandName = name;
    }

    /**
     * Removes the new CV and adds the old CV again, updates linguistic types.
     */
    @Override
	public void undo() {
    	if (transcription != null) {
    		List<LinguisticType> typesWithThisCV = transcription.getLinguisticTypesWithCV(newConVoc.getName());
    		
			transcription.removeControlledVocabulary(newConVoc);

			transcription.addControlledVocabulary(conVoc);
			
            if (typesWithThisCV != null && !typesWithThisCV.isEmpty()) {
            	LinguisticType lt;
            	for (int i = 0; i < typesWithThisCV.size(); i++) {
            		lt = typesWithThisCV.get(i);
            		lt.setControlledVocabularyName(conVoc.getName());
            	}
            }
    	}
    }

    /**
     * Removes the old CV and adds the new CV again, updates linguistic types.
     */
    @Override
	public void redo() {
		if (transcription != null) {
			List<LinguisticType> typesWithThisCV = transcription.getLinguisticTypesWithCV(conVoc.getName());
			
			transcription.removeControlledVocabulary(conVoc);

			transcription.addControlledVocabulary(newConVoc);
			
            if (typesWithThisCV != null && !typesWithThisCV.isEmpty()) {
            	LinguisticType lt;
            	for (int i = 0; i < typesWithThisCV.size(); i++) {
            		lt = typesWithThisCV.get(i);
            		lt.setControlledVocabularyName(newConVoc.getName());
            	}
            }
		}
    }

    /**
     * Replacing a ControlledVocabulary comes down to deleting the old one  and
     * adding the new one in one (action)command.<br>
     * May 2013: linguistic types linked to the replaced CV are now updated to use
     * the new CV.<br>
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the old Controlled
     *        Vocabulary (Controlled Vocabulary)</li> <li>arg[1] = the the new
     *        Controlled Vocabulary (Controlled Vocabulary)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        conVoc = (ControlledVocabulary) arguments[0];
        newConVoc = (ControlledVocabulary) arguments[1];

        if ((transcription.getControlledVocabulary(conVoc.getName()) != null) &&
                (newConVoc != null)) {
        	List<LinguisticType> typesWithThisCV = transcription.getLinguisticTypesWithCV(conVoc.getName());
            
        	transcription.removeControlledVocabulary(conVoc);

            transcription.addControlledVocabulary(newConVoc);
            
            if (typesWithThisCV != null && !typesWithThisCV.isEmpty()) {
            	LinguisticType lt;
            	for (int i = 0; i < typesWithThisCV.size(); i++) {
            		lt = typesWithThisCV.get(i);
            		lt.setControlledVocabularyName(newConVoc.getName());
            	}
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
