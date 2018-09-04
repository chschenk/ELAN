package mpi.eudico.client.annotator.commands;

import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.CVEntry;


/**
 * A Command to change an entry in a Controlled Vocabulary.
 *
 * @author Han Sloetjes
 */
public class DeleteCVEntryCommand implements Command {
    private String commandName;

    /**
     * Creates a new DeletCVEntryCommand instance
     *
     * @param name the name of the command
     */
    public DeleteCVEntryCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are  correct.<br>
     * When the CV is connected to a Transcription it will handle the
     * notification  of the change.
     *
     * @param receiver the Controlled Vocabulary
     * @param arguments the arguments: <ul><li>arg[0] = an array of entries to
     *        delete (Object[])</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ControlledVocabulary conVoc = (ControlledVocabulary) receiver;
        CVEntry[] entries = (CVEntry[]) arguments[0];

        if ((conVoc != null) && (entries.length > 0)) {
            conVoc.removeEntries(entries);
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
