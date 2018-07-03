package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * Deletes a Linguistic Type from a Transcription.
 *
 * @author Han Sloetjes
 */
public class DeleteTypeCommand implements UndoableCommand {
    private String commandName;

    // receiver
    private Transcription transcription;

    // the LinguisticType to remove
    private LinguisticType linType;

    /**
     * Creates a new DeleteTypeCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public DeleteTypeCommand(String name) {
        commandName = name;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void undo() {
        if (linType != null) {
            transcription.addLinguisticType(linType);
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void redo() {
        if (linType != null) {
            transcription.removeLinguisticType(linType);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the Linguistic Type
     *        (LinguisticType)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (Transcription) receiver;
        linType = (LinguisticType) arguments[0];

        transcription.removeLinguisticType(linType);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}
