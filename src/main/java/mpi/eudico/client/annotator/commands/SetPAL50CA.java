package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * CommandAction to manually set the video frame rate to PAL(?) 50 fps.<br>
 * This only influences the number of milliseconds per frame for Elan.<br>
 * 50 frames per second results in 1000 / 50 = 20 milliseconds per
 * frame.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SetPAL50CA extends CommandAction {

    /** the number of ms per frame */
    private final Object[] args = new Object[] { Long.valueOf(20L) };

    /**
     * Creates a new SetPAL50CA instance
     *
     * @param viewerManager
     */
    public SetPAL50CA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SET_PAL_50);
    }

    /**
     * 
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SET_PAL_50);
    }

    /**
     * The receiver of this CommandAction is an ElanMediaPlayer.
     *
     * @return the master media player
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * @return the ms per frame value, 20 ms
     */
    @Override
	protected Object[] getArguments() {
        return args;
    }
}
