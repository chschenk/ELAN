package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to create a dialog  for the copy annotations process.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CopyAnnotationsOfTierDlgCA extends CommandAction {

	/**
	 * Constructor.
	 * 
	 * @param theVM the viewer manager
	 */
	public CopyAnnotationsOfTierDlgCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.COPY_ANN_OF_TIER);
	}

	/**
	 * Creates the command
	 */
	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(), 
				ELANCommandFactory.COPY_ANN_OF_TIER_DLG);
	}

    /**
     * Returns the receiver of the command, the transcription.
     *
     * @return the receiver of the command
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }
}
