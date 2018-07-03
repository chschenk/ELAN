package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * CommandAction to manually set the video standard (of the media file) to NTSC.<br>
 * This only influences the number of milliseconds per frame for Elan.<br>
 * NTSC is interlaced and has 59.94 / 2 frames per second, resulting in 1000 /
 * 29.97 = 33.3667 milliseconds, rounded to 33 ms per frame. See <a
 * href="http://archive.ncsa.uiuc.edu/SCMS/training/general/details/ntsc.html">NCSA
 * web site</a>.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SetNTSCCA extends CommandAction {
    // the number of ms per frame

    /** Holds value of property DOCUMENT ME! */
    private final Object[] args = new Object[] { Long.valueOf(33L) };

    /**
     * Creates a new SetNTSCCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public SetNTSCCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SET_NTSC);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SET_NTSC);
    }

    /**
     * The receiver of this CommandAction is an ElanMediaPlayer.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        return args;
    }
}
