package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 *
 */
public class SelectionModeCA extends CommandAction {
    /**
     * Creates a new SelectionModeCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public SelectionModeCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SELECTION_MODE);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SELECTION_MODE);
    }

    /**
     *
     */
    @Override
	protected Object getReceiver() {
        return null;
    }

    /**
     * Returns null, no arguments need to be passed.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm.getMediaPlayerController();
        args[1] = vm.getMasterMediaPlayer();

        return args;
    }
}
