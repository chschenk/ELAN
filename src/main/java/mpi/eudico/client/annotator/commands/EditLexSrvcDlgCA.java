package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

public class EditLexSrvcDlgCA extends CommandAction {

	public EditLexSrvcDlgCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.EDIT_LEX_SRVC_DLG);
	}

	/**
     * Creates a new Command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EDIT_LEX_SRVC_DLG);
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
