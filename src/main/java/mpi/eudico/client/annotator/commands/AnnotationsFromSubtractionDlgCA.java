package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Creates the "annotations from overlaps wizard".
 */
public class AnnotationsFromSubtractionDlgCA extends CommandAction {

    /**
     * Constructor.
     * 
     * @param viewerManager the ViewerManager
     */
    public AnnotationsFromSubtractionDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.ANN_FROM_SUBTRACTION);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.ANN_FROM_SUBTRACTION_COM);
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