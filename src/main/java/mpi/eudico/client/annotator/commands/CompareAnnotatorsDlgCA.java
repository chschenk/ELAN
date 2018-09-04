package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Creates a dialog that allows comparison of the annotations on two tiers.
 * @version Jan 2015 this action now uses the ANNOTATOR_COMPARE_MULTI constant
 * to create the command.
 */
@SuppressWarnings("serial")
public class CompareAnnotatorsDlgCA extends CommandAction {

    /**
     * Constructor.
     * 
     * @param viewerManager the ViewerManager
     */
    public CompareAnnotatorsDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.ANNOTATOR_COMPARE_MULTI);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.ANNOTATOR_COMPARE_MULTI);
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
