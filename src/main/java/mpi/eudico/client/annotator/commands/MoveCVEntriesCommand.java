package mpi.eudico.client.annotator.commands;

import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.CVEntry;


/**
 * A Command to change the order of entries in a Controlled Vocabulary.
 *
 * @author Han Sloetjes
 */
public class MoveCVEntriesCommand implements Command {
    private String commandName;
    private int editType;
    private CVEntry[] entries;
    private ControlledVocabulary conVoc;

    /**
     * Creates a new MoveCVEntriesCommand instance
     *
     * @param name the name of the command
     */
    public MoveCVEntriesCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are  correct.<br>
     * When the CV is connected to a Transcription it will handle the
     * notification  of the change.
     *
     * @param receiver the Controlled Vocabulary
     * @param arguments the arguments: <ul><li>arg[0] = an array of entries
     *        to move (CVEntry[])</li> <li>arg[1] =  the type of move
     *        operation (Integer)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        conVoc = (ControlledVocabulary) receiver;

        entries = (CVEntry[]) arguments[0];
        editType = ((Integer) arguments[1]).intValue();

        if ((conVoc != null) && (entries.length > 0)) {
            conVoc.moveEntries(entries, editType);
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
