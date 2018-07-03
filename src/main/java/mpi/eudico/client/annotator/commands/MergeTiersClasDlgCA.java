package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

public class MergeTiersClasDlgCA extends CommandAction{
	/**
     * Constructor.
     *
     * @param viewerManager the ViewerManager
     */
    public MergeTiersClasDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MERGE_TIERS_CLAS);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.MERGE_TIERS_DLG_CLAS);
    }

    /**
     * Returns the transcription
     *
     * @return the transcription
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }
}
