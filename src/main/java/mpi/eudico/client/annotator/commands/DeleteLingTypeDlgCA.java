package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.EditTypeDialog2;


/**
 * Brings up a JDialog for deleting a Linguistic Type.
 *
 * @author Han Sloetjes
 */
public class DeleteLingTypeDlgCA extends CommandAction {
    /**
     * Creates a new DeleteLingTypeDlgCA instance
     *
     * @param viewerManager the viewer manager
     */
    public DeleteLingTypeDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_TYPE);
    }

    /**
     * Creates a new edit type dialog command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EDIT_TYPE);
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
        return new Object[] { Integer.valueOf(EditTypeDialog2.DELETE) };
    }
}
