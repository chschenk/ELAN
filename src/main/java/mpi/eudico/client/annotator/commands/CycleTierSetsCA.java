package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to activate/select the next tier set in a list.
 */
@SuppressWarnings("serial")
public class CycleTierSetsCA extends CommandAction {

	public CycleTierSetsCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.CYCLE_TIER_SETS);
	}
	
	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.CYCLE_TIER_SETS);
	}

	/**
     * @return the multi-tier control panel
     */
    @Override
	protected Object getReceiver() {
        return vm.getMultiTierControlPanel();
    }
}
