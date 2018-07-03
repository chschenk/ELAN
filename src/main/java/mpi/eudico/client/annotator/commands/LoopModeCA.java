package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 *
 */
public class LoopModeCA extends CommandAction {
    /**
     * Creates a new LoopModeCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public LoopModeCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.LOOP_MODE);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.LOOP_MODE);
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
