package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * CommandAction to manually set the video standard (of the media file) to PAL.<br>
 * This only influences the number of milliseconds per frame for Elan.<br>
 * PAL usually has 25 frames per second, resulting in 1000 / 25 = 40 milliseconds per
 * frame. See <a
 * href="http://archive.ncsa.uiuc.edu/SCMS/training/general/details/pal.html">NCSA
 * web site</a>.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SetPALCA extends CommandAction {

    /** the number of ms per frame */
    private final Object[] args = new Object[] { Long.valueOf(40L) };

    /**
     * Creates a new SetPALCA instance
     *
     * @param viewerManager
     */
    public SetPALCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SET_PAL);
    }

    /**
     * 
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SET_PAL);
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
     *
     *
     * @return the ms per frame value
     */
    @Override
	protected Object[] getArguments() {
        return args;
    }
}
