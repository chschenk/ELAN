package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * DOCUMENT ME!
 * $Id: ActiveSelectionBoundaryCommand.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class ActiveSelectionBoundaryCommand implements Command {
    private String commandName;

    /**
     * Creates a new ActiveSelectionBoundaryCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public ActiveSelectionBoundaryCommand(String theName) {
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
        // receiver is master ElanMediaPlayerController
        // arguments[0] is ElanMediaPlayer
        // arguments[1] is Selection
        // arguments[2] is ActiveSelectionBoundaryCA
        ElanMediaPlayerController mediaPlayerController = (ElanMediaPlayerController) receiver;
        ElanMediaPlayer player = (ElanMediaPlayer) arguments[0];
        Selection selection = (Selection) arguments[1];
        ActiveSelectionBoundaryCA ca = (ActiveSelectionBoundaryCA) arguments[2];

        if (player == null) {
            return;
        }

        if (player.isPlaying()) {
            return;
        }

        long beginTime = selection.getBeginTime();
        long endTime = selection.getEndTime();

        if (beginTime == endTime) {
            return;
        }

        mediaPlayerController.toggleActiveSelectionBoundary();

        if (mediaPlayerController.isBeginBoundaryActive()) {
            //		ca.setLeftIcon(false);
            player.setMediaTime(beginTime);
        } else {
            //		ca.setLeftIcon(true);
            player.setMediaTime(endTime);
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
