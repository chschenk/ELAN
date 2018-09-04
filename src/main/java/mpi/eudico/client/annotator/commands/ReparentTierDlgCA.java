package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that shows a 'wizard' dialog to reparent a tier.
 *
 * @author Han Sloetjes
 */
public class ReparentTierDlgCA extends CommandAction {
    /**
     * Creates a new ReparentTierDlgCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ReparentTierDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.REPARENT_TIER);
    }

    /**
     * Creates a new Command.
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.REPARENT_TIER_DLG);
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
