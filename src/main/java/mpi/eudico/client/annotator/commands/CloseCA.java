package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A Command Action to close a single ELAN window.
 *
 * @author Han Sloetjes, MPI
 */
public class CloseCA extends CommandAction {
    /**
     * Creates a new Close command action.
     *
     * @param viewerManager the viewer manager
     */
    public CloseCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.CLOSE);
        updateLocale();
    }

    /**
     * Creates a new Close command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.CLOSE);
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
