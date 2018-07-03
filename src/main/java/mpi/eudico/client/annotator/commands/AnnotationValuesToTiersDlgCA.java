package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A class to create the dialog for configuring the conversion of 
 * annotation values to tiers.
 * 
 * @author Han Sloetjes
 */
public class AnnotationValuesToTiersDlgCA extends CommandAction {

	/**
	 * Constructor.
	 * @param theVM
	 * @param name
	 */
	public AnnotationValuesToTiersDlgCA(ViewerManager2 viewerManager) {
		super(viewerManager, ELANCommandFactory.ANNOTATIONS_TO_TIERS);
	}

	/**
	 * Creates a new command to create the window.
	 */
	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.ANNOTATIONS_TO_TIERS_DLG);
	}

	/**
	 * Returns the transcription.
	 * 
	 * @return the transcription
	 */
	@Override
	protected Object getReceiver() {
		return vm.getTranscription();
	}	

}
