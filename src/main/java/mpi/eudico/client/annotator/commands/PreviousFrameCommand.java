package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: PreviousFrameCommand.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class PreviousFrameCommand implements Command {
    private String commandName;

    /**
     * Creates a new PreviousFrameCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public PreviousFrameCommand(String theName) {
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
        if (receiver != null) {
        	// replaced by nextFrame.
        	// getMillisecondsPerSample returns a long so there are rounding errors for ntsc
        	// further some media platforms support direct frame stepping that can handle dropped frames
            /*long msPerFrame = ((ElanMediaPlayer) receiver).getMilliSecondsPerSample();
            long frame = ((ElanMediaPlayer) receiver).getMediaTime() / msPerFrame;

            if (frame > 0) {
                ((ElanMediaPlayer) receiver).setMediaTime((frame - 1) * msPerFrame);
            }*/
        	((ElanMediaPlayer) receiver).previousFrame();
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
