package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.TimeScale;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: PixelLeftCommand.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class PixelLeftCommand implements Command {
    private String commandName;

    /**
     * Creates a new PixelLeftCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public PixelLeftCommand(String theName) {
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
        // arguments[0] is TimeScale
        TimeScale ts = (TimeScale) arguments[0];

        if (receiver != null) {
            ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaTime() -
                (int) ts.getMsPerPixel());
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
