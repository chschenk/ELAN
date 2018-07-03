package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.ModePanel;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 *
 */
public class SelectionModeCommand implements Command {
    private String commandName;
    private ElanMediaPlayerController mediaPlayerController;
    private ElanMediaPlayer masterMediaPlayer;

    /**
     * Creates a new SelectionModeCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public SelectionModeCommand(String name) {
        commandName = name;
    }

    /**
     *
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        mediaPlayerController = (ElanMediaPlayerController) arguments[0];
        masterMediaPlayer = (ElanMediaPlayer) arguments[1];
        
        if (!masterMediaPlayer.isPlaying()) {
            updateSelectionMode(!mediaPlayerController.getSelectionMode());
        	   mediaPlayerController.doToggleSelectionMode();

            if ((mediaPlayerController.getSelectionMode() == true) &&
                    (mediaPlayerController.getSelectionBeginTime() == mediaPlayerController.getSelectionEndTime())) {
                mediaPlayerController.setSelection(mediaPlayerController.getMediaTime(),
                    mediaPlayerController.getMediaTime());
            }
        } else {
            updateSelectionMode(mediaPlayerController.getSelectionMode()); // reset checkbox
        }
    }

    private void updateSelectionMode(boolean onOff) {
        ((ModePanel) mediaPlayerController.getModePanel()).updateSelectionMode(onOff);
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
