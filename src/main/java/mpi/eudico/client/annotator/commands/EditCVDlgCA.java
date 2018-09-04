package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that brings up a JDialog for defining and editing
 * Controlled Vocabularies.
 *
 * @author Han Sloetjes
 */
public class EditCVDlgCA extends CommandAction {
    /**
     * Creates a new EditCVDlgCA.
     *
     * @param viewerManager the viewermanager
     */
    public EditCVDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EDIT_CV_DLG);
    }

    /**
     * Creates a new Command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EDIT_CV_DLG);
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
