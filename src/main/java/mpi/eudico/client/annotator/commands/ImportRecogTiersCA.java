package mpi.eudico.client.annotator.commands;


import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to import tiers from an AVATecH recognizer TIER file,
 * csv or xml.
 *  
 * @author Han Sloetjes
 */
public class ImportRecogTiersCA extends CommandAction {

	/**
	 * Constructor.
	 * @param theVM the viewer manager
	 */
	public ImportRecogTiersCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.IMPORT_RECOG_TIERS);
	}

	@Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.IMPORT_RECOG_TIERS);
	}

    /**
     * Returns the receiver of the command
     *
     * @return the receiver
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }
}
