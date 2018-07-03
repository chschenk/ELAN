package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * An action to delete all annotations on the active tier, left of the
 * crosshair.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DeleteAnnotationsLeftOfCA extends CommandAction {
    /**
     * Creates a new action.
     *
     * @param viewerManager
     */
    public DeleteAnnotationsLeftOfCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_ANNOS_LEFT_OF);
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
     * @return array containing the active tier, selection begin time (= 0) and end
     *         time (= current media time), or null
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getArguments()
     */
    @Override
    protected Object[] getArguments() {
        if (vm.getMultiTierControlPanel().getActiveTier() != null) {
            Long bt = Long.valueOf(0);
            Long et = new Long(vm.getMasterMediaPlayer().getMediaTime());

            return new Object[] {
                vm.getMultiTierControlPanel().getActiveTier(), bt, et
            };
        }

        return null;
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
