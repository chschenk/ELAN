package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A Command that actually changes the playback volume of the master media
 * player and that updates the rate slider.
 *
 * @author Han Sloetjes
 */
public class PlaybackVolumeToggleCommand implements Command {
    private String commandName;

    /**
     * Creates a new PlaybackVolumeToggleCommand instance
     *
     * @param theName the name of the command
     */
    public PlaybackVolumeToggleCommand(String theName) {
        commandName = theName;
    }

    /**
     * Changes the volume of the master media player and updates the user
     * interface. <b>Note: </b>it is assumed the types and order of the
     * arguments are correct.
     *
     * @param receiver the viewer manager
     * @param arguments the arguments: <ul><li>arg[0] = the new value for the
     *        playback rate toggle (Float)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 viewerManager = (ViewerManager2) receiver;
        float volume = ((Float) arguments[0]).floatValue();

        viewerManager.getMediaPlayerController().setVolume(volume);
    }

    /**
     * Returns the name of the command
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }
}
