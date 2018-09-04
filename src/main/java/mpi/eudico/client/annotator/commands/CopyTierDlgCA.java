package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that shows a 'wizard' dialog to copy a tier.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CopyTierDlgCA extends CommandAction {
    /**
     * Creates a new CopyTierDlgCA instance
     *
     * @param viewerManager the viewer manager
     */
    public CopyTierDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.COPY_TIER);
    }

    /**
     * Creates a new Command.
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.COPY_TIER_DLG);
    }

    /**
     * Returns the receiver of the command, the transcription.
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
