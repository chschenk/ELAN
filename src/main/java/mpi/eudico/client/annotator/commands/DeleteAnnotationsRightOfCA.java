package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * An action to delete all annotations on the active tier, right of the
 * crosshair.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DeleteAnnotationsRightOfCA extends CommandAction {
    /**
     * Creates a new action.
     *
     * @param viewerManager
     */
    public DeleteAnnotationsRightOfCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_ANNOS_RIGHT_OF);
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
     * @return array containing the active tier, selection begin time (=current
     *         media time) and end time (= Long.MAX_VALUE), or null
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getArguments()
     */
    @Override
    protected Object[] getArguments() {
        if (vm.getMultiTierControlPanel().getActiveTier() != null) {
            Long bt = new Long(vm.getMasterMediaPlayer().getMediaTime());
            Long et = new Long(Long.MAX_VALUE);

            return new Object[] {
                vm.getMultiTierControlPanel().getActiveTier(), bt, et
            };
        }

        return null;
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
