package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Creates the change case of annotations dialog.
 */
public class ChangeCaseDlgCA extends CommandAction {
    /**
     * Constructor.
     *
     * @param viewerManager the ViewerManager
     */
    public ChangeCaseDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.CHANGE_CASE);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.CHANGE_CASE_COM);
    }

    /**
     * Returns the transcription
     *
     * @return the transcription
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }
}
