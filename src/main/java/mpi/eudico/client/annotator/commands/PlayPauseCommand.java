package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: PlayPauseCommand.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class PlayPauseCommand implements Command {
    private String commandName;
    private ElanMediaPlayer player;
    private ElanMediaPlayerController mediaPlayerController;

    /**
     * Creates a new PlayPauseCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public PlayPauseCommand(String theName) {
        commandName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is ElanMediaPlayerController
        player = (ElanMediaPlayer) receiver;
        mediaPlayerController = (ElanMediaPlayerController) arguments[0];

        if (player == null) {
            return;
        }

        boolean playSel = mediaPlayerController.isPlaySelectionMode();
        mediaPlayerController.setPlaySelectionMode(false);
        mediaPlayerController.stopLoop();
        
        if (player.isPlaying() == true) {
            player.stop();
            
            if (playSel) {           
	            player.setStopTime(player.getMediaDuration());
            }
        } else {
            player.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}
