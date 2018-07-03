package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to open the Web Services dialog.
 * 
 * @author Han Sloetjes
 */
public class WebLichtDlgCA extends CommandAction {

	/**
	 * Constructor
	 * @param theVM viewer manager
	 * @param name the name of the command and action
	 */
	public WebLichtDlgCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.WEBLICHT_DLG);
	}

	/**
	 * Creates a WebServicesDlgCommand.
	 */
	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(), 
				ELANCommandFactory.WEBSERVICES_DLG);
	}

	/**
	 * Returns the viewer manager
	 */
	@Override
	protected Object getReceiver() {
		return vm;
	}

	/**
	 * Returns the constant for WebLicht to create a dialog for interacting with that service. 
	 */
	@Override
	protected Object[] getArguments() {
		return new String[]{ELANCommandFactory.WEBLICHT_DLG};
	}

	
}
