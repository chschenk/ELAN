package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to save a transcription produced in Simple-ELAN
 * with separated tiers for translation and speakers (in case
 * there are special markers for that within annotations).
 */
@SuppressWarnings("serial")
public class ExportRegularMultitierEafCA extends CommandAction {

	public ExportRegularMultitierEafCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.EXPORT_REGULAR_MULTITIER_EAF);
	}

	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_REGULAR_MULTITIER_EAF);
	}

	/**
	 * @return the transcription to convert, save as
	 * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
	 */
	@Override
	protected Object getReceiver() {
		return vm.getTranscription();
	}
	
}
