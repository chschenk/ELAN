package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.AddParticipantDlg;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Creates the create empty annotations on dependent tiers dialog.
 */
public class AddParticipantDlgCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AddParticipantDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the add participant dialog.
     *
     * @param receiver the transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
    	TranscriptionImpl trans = (TranscriptionImpl) receiver;
    	new AddParticipantDlg(trans, ELANCommandFactory.getRootFrame(trans));    	
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}

