package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.LabelAndNumberDlg;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Creates the LabelAndNumber dialog.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class LabelAndNumberDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new LabelAndNumberDlgCommand instance
     *
     * @param name the name of the command
     */
    public LabelAndNumberDlgCommand(String name) {
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
        new LabelAndNumberDlg(trans, ELANCommandFactory.getRootFrame(trans)).setVisible(true);
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
