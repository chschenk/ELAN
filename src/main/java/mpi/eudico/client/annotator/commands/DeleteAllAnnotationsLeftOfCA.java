package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * An action to delete all annotations, left of the crosshair.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class DeleteAllAnnotationsLeftOfCA extends CommandAction {
    /**
     * Creates a new action.
     *
     * @param viewerManager
     */
    public DeleteAllAnnotationsLeftOfCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_ALL_ANNOS_LEFT_OF);
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
     *         (= 0) and end time (= current media time)
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getArguments()
     */
    @Override
    protected Object[] getArguments() {
        Long bt = Long.valueOf(0);
        Long et = new Long(vm.getMasterMediaPlayer().getMediaTime());

        return new Object[] { null, bt, et };
    }

    /**
     * Returns the transcription.
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
