package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to change the author attribute of a transcription
 * 
 * @author Han Sloetjes
 */
public class SetAuthorCA extends CommandAction {

	/**
	 * Constructor.
	 * 
	 * @param theVM the viewer manager
	 */
	public SetAuthorCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.SET_AUTHOR);
	}

	/**
	 * Creates a new command.
	 */
	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SET_AUTHOR);
	}

	/**
	 * Returns the transcription.
	 * 
	 * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
	 */
	@Override
	protected Object getReceiver() {
		return vm.getTranscription();
	}

	
}
