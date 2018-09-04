package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.export.ExportWordListDialog;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Creates a dialog to select tiers for a word list export.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class ExportWordsDialogCommand implements Command {
    private String commandName;

    /**
     * Creates a new ExportWordsDialogCommand instance
     *
     * @param name the name of the command
     */
    public ExportWordsDialogCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription
     *        object(Transcription)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl transcription = (TranscriptionImpl) arguments[0];
        new ExportWordListDialog(ELANCommandFactory.getRootFrame(transcription),
            true, transcription).setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}
