package mpi.eudico.client.annotator.commands;

import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.CVEntry;

/**
 * A Command to change an entry in a Controlled Vocabulary.
 *
 * @author Han Sloetjes
 */
public class ChangeCVEntryCommand implements Command {
	private String commandName;
	
	/**
	 * Creates a new ChangeCVEntryCommand instance
	 *
	 * @param name the name of the command
	 */
	public ChangeCVEntryCommand(String name) {
		commandName = name;
	}

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are 
	 * correct.<br>
	 * When the CV is connected to a Transcription it will handle the notification 
     * of the change.
     * 
	 * @param receiver the Controlled Vocabulary
	 * @param arguments the arguments: <ul><li>arg[0] = the old value
	 *        of the entry (String)</li> <li></li>arg[1] = the new value 
	 *        of the entry (String)<li>arg[2] = the description of
	 *        the entry</li><li>arg[3] = the language index of the value</li></ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {

		ControlledVocabulary conVoc = (ControlledVocabulary) receiver;
		String oldValue = (String) arguments[0];
		String newValue = (String) arguments[1];
		String desc = (String) arguments[2];
		int langIndex;
		
		if (arguments.length > 3) {
			langIndex = (Integer)arguments[3];
		} else {
			langIndex = conVoc.getDefaultLanguageIndex();
		}
			
		CVEntry curEntry = conVoc.getEntryWithValue(langIndex, oldValue);
		if (curEntry != null) {
			conVoc.modifyEntryValue(curEntry, langIndex, newValue);
			curEntry.setDescription(langIndex, desc);
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
