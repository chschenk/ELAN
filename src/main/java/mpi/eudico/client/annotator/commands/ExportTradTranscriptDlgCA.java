package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Show a dialog for export in a traditional transcript style.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ExportTradTranscriptDlgCA extends CommandAction {
    /**
     * Creates a Command Action for the export of text in a traditional transcript style.
     *
     * @param theVM the ViewerManager
     */
    public ExportTradTranscriptDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_TRAD_TRANSCRIPT);
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_TRAD_TRANSCRIPT);
    }

    /**
     * There's no natural receiver for this CommandAction.
     *
     * @return null
     */
    @Override
	protected Object getReceiver() {
        return null;
    }

    /**
     * Returns the Transcription and the Selection object.
     *
     * @return an array containing the Transcription and the Selection
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getTranscription(), vm.getSelection() };
    }
}
