package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;


/**
 *
 */
public class NormalTimePropCommand implements Command {
    private String commandName;

    /**
     * Creates a new NormalTimePropCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public NormalTimePropCommand(String name) {
        commandName = name;
    }

    /**
     *
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ((Transcription) receiver).setTimeChangePropagationMode(Transcription.NORMAL);
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
