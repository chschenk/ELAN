package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.ModePanel;
//import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 *
 */
public class LoopModeCommand implements Command {
    private String commandName;
    private ElanMediaPlayerController mediaPlayerController;
    //private ElanMediaPlayer masterMediaPlayer;

    /**
     * Creates a new LoopModeCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public LoopModeCommand(String name) {
        commandName = name;
    }

    /**
     *
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        mediaPlayerController = (ElanMediaPlayerController) arguments[0];
        //masterMediaPlayer = (ElanMediaPlayer) arguments[1];
        
        // HS dec 20006: perform the same action, whether the player is playing or not
        updateLoopMode(!mediaPlayerController.getLoopMode());
        mediaPlayerController.doToggleLoopMode();
        /*
        if (!masterMediaPlayer.isPlaying()) {
            updateLoopMode(!mediaPlayerController.getLoopMode());
            mediaPlayerController.doToggleLoopMode();
        } else {
            //updateLoopMode(mediaPlayerController.getLoopMode()); // reset checkbox
            // HS dec 20006: do the same, it shouldn't matter whether the player is playing
            updateLoopMode(!mediaPlayerController.getLoopMode());
            mediaPlayerController.doToggleLoopMode();
        }
        */
    }

    private void updateLoopMode(boolean onOff) {
        ((ModePanel) mediaPlayerController.getModePanel()).updateLoopMode(onOff);
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
