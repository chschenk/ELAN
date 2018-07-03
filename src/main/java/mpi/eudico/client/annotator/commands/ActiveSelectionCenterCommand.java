package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;

/**
 * DOCUMENT ME!
 * 
 * A command action for moving the crosshair to the center of the selection.
 * 
 * @author $Aarthy Somsundaram$
 * @version $Dec 2010$
 */

public class ActiveSelectionCenterCommand implements Command {
    private String commandName;

    /**
     * Creates a new ActiveSelectionCenterCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public ActiveSelectionCenterCommand(String theName) {
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
        
        ElanMediaPlayerController mediaPlayerController = (ElanMediaPlayerController) receiver;
        ElanMediaPlayer player = (ElanMediaPlayer) arguments[0];
        Selection selection = (Selection) arguments[1];        

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
        
        player.setMediaTime((beginTime+endTime)/2);
        
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
