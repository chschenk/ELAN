package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;


/**
 * Action to shift annotations on the active tier that are located to the left
 * of the media crosshair.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
@SuppressWarnings("serial")
public class ShiftAnnotationsLeftOfCA extends CommandAction {
    /**
     * Creates a new ShiftAnnotationsLeftOfCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ShiftAnnotationsLeftOfCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SHIFT_ANNOS_LEFT_OF);
    }

    /**
     * Creates a new ShiftAnnotationsLeftOfCA instance
     *
     * @param viewerManager the viewer manager
     * @param name the name
     */
    public ShiftAnnotationsLeftOfCA(ViewerManager2 viewerManager, String name) {
        super(viewerManager, name);
    }

    /**
     * Creates a new shift annotations command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SHIFT_ANN_DLG);
    }

    /**
     * The active tier, zero and the crosshair time.
     *
     * @return an Object array size = 3, Tier, Long, Long
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
     * Returns the receiver of the command.
     *
     * @return the receiver of the command
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * If the media is playing, first stop the player.
     *
     * @param event action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (vm.getMasterMediaPlayer().isPlaying()) {
            vm.getMasterMediaPlayer().stop();
        }

        super.actionPerformed(event);
    }
}
