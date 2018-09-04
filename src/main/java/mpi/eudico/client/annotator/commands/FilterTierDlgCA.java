package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Creates a Filter Tier dialog.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class FilterTierDlgCA extends CommandAction {
    /**
     * Constructor.
     *
     * @param theVM the ViewerManager
     */
    public FilterTierDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.FILTER_TIER);
    }

    /**
     * Creates a new Tokenize dialog command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.FILTER_TIER_DLG);
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
