package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Shift the annotations on all (root) tiers that are completely located to the
 * right of the crosshair.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ShiftAllAnnotationsRightOfCA extends ShiftAnnotationsRightOfCA {
    /**
     * Creates a new ShiftAllAnnotationsRightOfCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ShiftAllAnnotationsRightOfCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SHIFT_ALL_ANNOS_RIGHT_OF);
    }

    /**
     * Creates a new command to shift all tiers, from a certain point.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SHIFT_ANN_ALLTIER_DLG);
    }

    /**
     * Crosshair time and max value.
     *
     * @return an Object array size = 2, (Long, Long)
     */
    @Override
    protected Object[] getArguments() {
        Long bt = new Long(vm.getMasterMediaPlayer().getMediaTime());
        Long et = new Long(Long.MAX_VALUE);

        return new Object[] { bt, et };
    }
}
