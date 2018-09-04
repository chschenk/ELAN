package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that shows a merge transcriptions dialog.
 *
 * @author Han Sloetjes
 */
public class MergeTranscriptionDlgCA extends CommandAction {
    /**
     * Constructor.
     *
     * @param viewerManager the viewer manager
     */
    public MergeTranscriptionDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MERGE_TRANSCRIPTIONS);
    }

    /**
     * Creates a new Command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.MERGE_TRANSCRIPTIONS);
    }

    /**
     * Returns the receiver of the command.
     *
     * @return the receiver of the command
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * There are no arguments for the command that creates a dialog.
     *
     * @return null
     */
    @Override
	protected Object[] getArguments() {
        return null;
    }
}
