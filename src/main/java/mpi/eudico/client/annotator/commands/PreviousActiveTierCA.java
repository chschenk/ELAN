package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Sets the previous (up) tier active in MultiTierControlPanel.
 *
 * @author Han Sloetjes
 */
public class PreviousActiveTierCA extends CommandAction {
    /** Object array of size 1: Boolean.FALSE */
    final Object[] arguments = new Object[] { Boolean.FALSE };

    /**
     * Constructor.
     *
     * @param viewerManager the viewermanager
     */
    public PreviousActiveTierCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PREVIOUS_ACTIVE_TIER);
    }

    /**
     * Creates a new ActiveTierCommand.
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.ACTIVE_TIER);
    }

    /**
     * Returns the receiver of the command.
     *
     * @return the MultiTierControlPanel
     */
    @Override
	protected Object getReceiver() {
        return vm.getMultiTierControlPanel();
    }

    /**
     * Returns the arguments for the command.
     *
     * @return Boolean.TRUE
     */
    @Override
	protected Object[] getArguments() {
        return arguments;
    }
}
