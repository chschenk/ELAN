package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;


/**
 * Action that shifts all annotations on the active tier that are located to
 * the right of the current media time.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
public class ShiftAnnotationsRightOfCA extends CommandAction {
    /**
     * Creates a new ShiftAnnotationsRightOfCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ShiftAnnotationsRightOfCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SHIFT_ANNOS_RIGHT_OF);
    }

    /**
     * Creates a new ShiftAnnotationsRightOfCA instance
     *
     * @param viewerManager the viewer manager
     * @param name the name
     */
    public ShiftAnnotationsRightOfCA(ViewerManager2 viewerManager, String name) {
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
     * The active tier, crosshair time and long max value.
     *
     * @return an Object array size = 3, Tier, Long, Long
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
