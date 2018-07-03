package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Selection;


/**
 * DOCUMENT ME!
 * $Id: ClearSelectionCommand.java 43915 2015-06-10 09:02:42Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class ClearSelectionCommand implements UndoableCommand {
    private String commandName;

    // store state
    private long oldBegin;
    private long oldEnd;
    private Selection selection;
    private ElanMediaPlayerController mediaPlayerController;

    /**
     * Creates a new ClearSelectionCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public ClearSelectionCommand(String theName) {
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
        // receiver is Selection
        // arguments[0] is mediaPlayerController
        selection = (Selection) receiver;
        mediaPlayerController = (ElanMediaPlayerController) (arguments[0]);

        oldBegin = selection.getBeginTime();
        oldEnd = selection.getEndTime();

        if (mediaPlayerController.getSelectionMode() == true) {
            selection.setSelection(mediaPlayerController.getMediaTime(),
                mediaPlayerController.getMediaTime());
        } else {
            selection.setSelection(0, 0);
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

    /**
     * DOCUMENT ME!
     */
    @Override
	public void undo() {
        if (selection != null) {
            selection.setSelection(oldBegin, oldEnd);
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void redo() {
        if (selection != null) {
            selection.setSelection(0, 0);
        }
    }
}
