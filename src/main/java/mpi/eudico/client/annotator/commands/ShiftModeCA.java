package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 *
 */
public class ShiftModeCA extends CommandAction {
    /**
     * Creates a new ShiftModeCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public ShiftModeCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SHIFT_MODE);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SHIFT_MODE);
    }

    /**
     *
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns null, no arguments need to be passed.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        return null;
    }
}
