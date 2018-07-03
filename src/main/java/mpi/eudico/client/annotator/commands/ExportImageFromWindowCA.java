package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommanAction for the export of an image of the Elan frame.
 *
 * @author Han Sloetjes
 */
public class ExportImageFromWindowCA extends CommandAction {
    /**
     * Creates a Command Action for the export of an image of the Elan Frame.
     *
     * @param theVM the ViewerManager
     */
    public ExportImageFromWindowCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_IMAGE_FROM_WINDOW);
    }

    /**
     * Creates a new Command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_IMAGE_FROM_WINDOW);
    }

    /**
     * There's no natural receiver for this CommandAction.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return null;
    }

    /**
     * Returns the Transcription as the only argument.
     *
     * @return an array containing the Transcription
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getTranscription() };
    }
}
