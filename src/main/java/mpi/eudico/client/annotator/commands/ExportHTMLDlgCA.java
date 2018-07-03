package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Creates a Command that creates an export as HTML preview dialog.
 */
public class ExportHTMLDlgCA extends CommandAction {

    /**
     * Constructor.
     * 
     * @param theVM
     */
    public ExportHTMLDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_HTML);
        
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.EXPORT_HTML);
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
