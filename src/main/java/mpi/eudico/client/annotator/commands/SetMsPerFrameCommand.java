package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * Command to manually set the video standard (of the media file) to PAL.<br>
 *
 * @author Han Sloetjes
 *
 * @see SetPAlCA
 */
public class SetMsPerFrameCommand implements Command {
    private String commandName;

    /**
     * Creates a new SetMsPerFrameCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public SetMsPerFrameCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the ElanMediaPlayer
     * @param arguments the arguments:  <ul><li>arg[0] = the new number of ms
     *        per frame (Long)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        long newValue = ((Long) arguments[0]).longValue();

        if (receiver != null) {
            ((ElanMediaPlayer) receiver).setMilliSecondsPerSample(newValue);
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
