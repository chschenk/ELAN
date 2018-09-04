package mpi.eudico.client.annotator.commands;

import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.CVEntry;


/**
 * A Command to replace the existing CVEntries in a Controlled Vocabulary  by a
 * new array of CVEntries.
 *
 * @author Han Sloetjes
 */
public class ReplaceCVEntriesCommand implements Command {
    private String commandName;
    private CVEntry[] entries;
    private ControlledVocabulary conVoc;

    /**
     * Creates a new ReplaceCVEntriesCommand instance.
     *
     * @param name the name of the command
     */
    public ReplaceCVEntriesCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are  correct.<br>
     * When the CV is connected to a Transcription it will handle the
     * notification  of the change.
     *
     * @param receiver the Controlled Vocabulary
     * @param arguments the arguments: <ul><li>arg[0] = an array of entries to
     *        replace the exisitng entries (CVEntry[])</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        conVoc = (ControlledVocabulary) receiver;

        entries = (CVEntry[]) arguments[0];

        if ((conVoc != null) && (entries != null)) {
            conVoc.replaceAll(entries);
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
