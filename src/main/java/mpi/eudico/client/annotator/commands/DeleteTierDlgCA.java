package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.EditTierDialog2;


/**
 * Brings up a JDialog for deleting a Tier.
 *
 * @author Han Sloetjes
 */
public class DeleteTierDlgCA extends CommandAction {
    /**
     * Creates a new DeleteTierDlgCA instance
     *
     * @param viewerManager the viewermanager
     */
    public DeleteTierDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_TIER);
    }

    /**
     * Creates a new edit tier dialog command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EDIT_TIER);
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

    /**
     * Returns the arguments, here the Delete mode constant
     *
     * @return the args for the command
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { Integer.valueOf(EditTierDialog2.DELETE), null };
    }
}
