package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that brings up a JDialog with an overview of all linked
 * files and with controls to edit the linked  files collection.
 *
 * @author Han Sloetjes
 */
public class LinkedFilesDlgCA extends CommandAction {
    /**
     * Creates a new LinkedFilesDlgCA.
     *
     * @param viewerManager the viewermanager
     */
    public LinkedFilesDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.LINKED_FILES_DLG);
    }

    /**
     * Creates a new Command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.LINKED_FILES_DLG);
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
