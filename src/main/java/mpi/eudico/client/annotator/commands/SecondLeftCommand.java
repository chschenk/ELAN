package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: SecondLeftCommand.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class SecondLeftCommand implements Command {
    private String commandName;

    /**
     * Creates a new SecondLeftCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public SecondLeftCommand(String theName) {
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
            ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaTime() -
                1000);
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
