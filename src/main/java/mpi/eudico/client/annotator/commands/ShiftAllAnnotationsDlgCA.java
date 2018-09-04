package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that shows a 'shift all annotations' dialog.
 *
 * @author Han Sloetjes
 */
public class ShiftAllAnnotationsDlgCA extends CommandAction {
    /**
     * Creates a new ShiftAllAnnotationsDlgCA.
     *
     * @param viewerManager the viewermanager
     */
    public ShiftAllAnnotationsDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SHIFT_ALL_ANNOTATIONS);
    }

    /**
     * Creates a new Command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SHIFT_ALL_DLG);
    }

    /**
     * Returns the receiver of the command.
     *
     * @return the receiver of the command
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * There are no arguments for the command that creates a dialog.
     *
     * @return null
     */
    @Override
	protected Object[] getArguments() {
        return null;
    }
}
