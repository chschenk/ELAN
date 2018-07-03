package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.SelectionListener;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * DOCUMENT ME!
 * $Id: PlaySelectionCA.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class PlaySelectionCA extends CommandAction implements SelectionListener {
    private Icon icon;

    /**
     * Creates a new PlaySelectionCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public PlaySelectionCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.PLAY_SELECTION);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/PlaySelectionButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
        vm.connectListener(this);
    }

    /**
     * Play around selection and play selection use the same command; play
     * selection passes 0 as offset.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PLAY_SELECTION);
    }

    /**
     * DOCUMENT ME!
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
        Object[] args = new Object[3];
        args[0] = vm.getSelection();
        args[1] = vm.getMediaPlayerController();
        args[2] = Integer.valueOf(0);

        return args;
    }

    /**
     * If the selection changes while playing a selection stop playing the (old)
     * selection. Stop the player as well??
     * @see mpi.eudico.client.annotator.SelectionListener#updateSelection()
     */
    @Override
	public void updateSelection() {
        if (vm.getMasterMediaPlayer().isPlaying()) {
            
	        if (vm.getMediaPlayerController().isPlaySelectionMode()) {
	            vm.getMediaPlayerController().setPlaySelectionMode(false);
	            vm.getMasterMediaPlayer().stop();
	            vm.getMasterMediaPlayer().setStopTime(vm.getMasterMediaPlayer().getMediaDuration());
	        }
        }
    }
}
