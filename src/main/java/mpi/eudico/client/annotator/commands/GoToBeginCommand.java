package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: GoToBeginCommand.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class GoToBeginCommand implements Command {
    private String commandName;
    private ElanMediaPlayer player;

    /**
     * Creates a new GoToBeginCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public GoToBeginCommand(String theName) {
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
        // arguments[0] is
        player = (ElanMediaPlayer) receiver;

        if (player == null) {
            return;
        }

        player.setMediaTime(0);
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
