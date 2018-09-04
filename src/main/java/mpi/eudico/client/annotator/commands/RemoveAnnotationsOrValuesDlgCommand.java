package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.RemoveAnnotationsOrValuesDlg;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Creates the RemoveAnnotationsOrValues dialog.
 * 
 * @author Aarthy Somasundaram
 * @version Oct 20, 2010
  */
public class RemoveAnnotationsOrValuesDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new RemoveAnnotationsOrValuesDlgCommand instance
     *
     * @param name the name of the command
     */
    public RemoveAnnotationsOrValuesDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates a (re-)label dialog.
     *
     * @param receiver the Transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;
        new RemoveAnnotationsOrValuesDlg(trans, ELANCommandFactory.getRootFrame(trans)).setVisible(true);
    }

    /**
     * Returns the name of the command
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }
}