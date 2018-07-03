package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * DOCUMENT ME! $Id: PlayPauseCA.java,v 1.1.1.1 2004/03/25 16:23:16 wouthuij
 * Exp $
 *
 * @author $Author$
 * @version $Revision$
 */
public class PlayPauseCA extends CommandAction implements ControllerListener {
    private Icon playIcon;
    private Icon pauseIcon;

    /**
     * Creates a new PlayPauseCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public PlayPauseCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.PLAY_PAUSE);

        // ask ViewerManager to connect to player
        vm.connectListener(this);

        playIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/PlayButton.gif"));
        pauseIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/PauseButton.gif"));
        putValue(SMALL_ICON, playIcon);
        putValue(Action.NAME, "");
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PLAY_PAUSE);
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
        Object[] args = new Object[1];
        args[0] = vm.getMediaPlayerController();

        return args;
    }

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public void controllerUpdate(ControllerEvent event) {
        if (event instanceof StopEvent) {
            setPlayPauseButton(true);
        }

        if (event instanceof StartEvent) {
            setPlayPauseButton(false);
        }
    }

    private void setPlayPauseButton(boolean play) {
        if (play) {
            putValue(SMALL_ICON, playIcon);
        } else {
            putValue(SMALL_ICON, pauseIcon);
        }
    }
}
