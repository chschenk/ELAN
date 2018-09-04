package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Sets the next (down) tier active in MultiTierControlPanel.
 *
 * @author Han Sloetjes
 */
public class NextActiveTierCA extends CommandAction {
    /** Object array of size 1: Boolean.TRUE */
    final Object[] arguments = new Object[] { Boolean.TRUE };

    /**
     * Constructor.
     *
     * @param viewerManager the viewermanager
     */
    public NextActiveTierCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.NEXT_ACTIVE_TIER);
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
