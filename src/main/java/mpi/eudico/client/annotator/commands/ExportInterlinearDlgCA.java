package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Shows a dialog for export in an interlinear text style.
 *
 * @author Han Sloetjes
 */
public class ExportInterlinearDlgCA extends CommandAction {
    /**
     * Creates a Command Action for the export in an interlinear text style.
     *
     * @param theVM the ViewerManager
     */
    public ExportInterlinearDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_INTERLINEAR);
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_INTERLINEAR);
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
