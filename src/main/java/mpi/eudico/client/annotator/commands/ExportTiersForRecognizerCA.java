package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to create a dialog to export tiers in the AVATecH TIER format,
 * in xml or csv.
 * 
 * @author Han Sloetjes
 */
public class ExportTiersForRecognizerCA extends CommandAction {

	/**
	 * @param theVM the viewermanager
	 * @param name name of the action
	 */
	public ExportTiersForRecognizerCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.EXPORT_RECOG_TIER);
	}

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_RECOG_TIER);
    }
    
    /**
     * Returns the arguments
     *
     * @return the arguments
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getTranscription(), vm.getSelection() };
    }

}
