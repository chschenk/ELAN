package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * An action to delete all annotations, right of the crosshair.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DeleteAllAnnotationsRightOfCA extends CommandAction {
    /**
     * Creates a new action.
     *
     * @param viewerManager
     */
    public DeleteAllAnnotationsRightOfCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_ALL_ANNOS_RIGHT_OF);
    }

    /**
     * Creates a new delete annotations command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.DELETE_ANNOS_IN_SELECTION);
    }

    /**
     * Returns the arguments.
     *
     * @return array containing "null" (i.e. all tiers), selection begin time
     *         (=current media time) and end time (= Long.MAX_VALUE)
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getArguments()
     */
    @Override
    protected Object[] getArguments() {
        Long bt = new Long(vm.getMasterMediaPlayer().getMediaTime());
        Long et = new Long(Long.MAX_VALUE);

        return new Object[] { null, bt, et };
    }

    /**
     * The transcription.
     *
     * @return the transcription
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }
}
