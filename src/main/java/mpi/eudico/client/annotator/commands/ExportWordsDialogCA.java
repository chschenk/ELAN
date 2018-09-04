package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Creates a dialog to select tiers for a word list export.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class ExportWordsDialogCA extends CommandAction {
    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public ExportWordsDialogCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_WORDS);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_WORDS);
    }

    /**
     * Returns the Transcription.
     *
     * @return an array containing the Transcription
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getTranscription() };
    }
}
