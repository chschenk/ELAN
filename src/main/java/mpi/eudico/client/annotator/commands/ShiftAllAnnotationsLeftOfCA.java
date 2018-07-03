package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Shifts all annotations, on all tiers that are left of the crosshair.
 * 
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
  */
@SuppressWarnings("serial")
public class ShiftAllAnnotationsLeftOfCA extends ShiftAnnotationsLeftOfCA {
    /**
     * Creates a new ShiftAllAnnotationsLeftOfCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ShiftAllAnnotationsLeftOfCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SHIFT_ALL_ANNOS_LEFT_OF);
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
     * Zero and the crosshair time.
     *
     * @return an Object array size = 2, (Long, Long)
     */
    @Override
    protected Object[] getArguments() {
        Long bt = Long.valueOf(0);
        Long et = new Long(vm.getMasterMediaPlayer().getMediaTime());

        return new Object[] { bt, et };
    }
}
