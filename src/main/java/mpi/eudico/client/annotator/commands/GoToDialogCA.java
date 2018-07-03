package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 *
 */
public class GoToDialogCA extends CommandAction {
    /**
     * Creates a new GoToDialogCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public GoToDialogCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.GOTO_DLG);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.GOTO_DLG);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm.getTimePanel();

        return args;
    }
}
