package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Creates the "annotations from overlaps wizard".
 */
public class AnnotationsFromOverlapsDlgCA extends CommandAction {

    /**
     * Constructor.
     * 
     * @param viewerManager the ViewerManager
     */
    public AnnotationsFromOverlapsDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.ANN_FROM_OVERLAP);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.ANN_FROM_OVERLAP_COM);
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
