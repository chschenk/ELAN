package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.ChangeCaseDlg;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Creates the change case of annotations dialog.
 */
public class ChangeCaseDlgCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public ChangeCaseDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the change case dialog.
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

        ChangeCaseDlg dialog = new ChangeCaseDlg(trans, ELANCommandFactory.getRootFrame(
                    trans));
        dialog.setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}
