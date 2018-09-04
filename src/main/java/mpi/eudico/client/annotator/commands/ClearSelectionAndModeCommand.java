package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.ModePanel;
import mpi.eudico.client.annotator.Selection;


/**
 * Clears the selection and sets the selection mode to false.
 *
 * @author Han Sloetjes
 */
public class ClearSelectionAndModeCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param name name of the command
     */
    public ClearSelectionAndModeCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Selection
     * @param arguments the arguments:  <ul><li>arg[0] = the media player
     *        controller  (ElanMediaPlayerController)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Selection selection = (Selection) receiver;
        ElanMediaPlayerController mediaPlayerController = (ElanMediaPlayerController) (arguments[0]);

        if (mediaPlayerController.getSelectionMode() == true) {
            ((ModePanel) mediaPlayerController.getModePanel()).updateSelectionMode(false);
            mediaPlayerController.doToggleSelectionMode();
            selection.setSelection(mediaPlayerController.getMediaTime(),
                mediaPlayerController.getMediaTime());
        } else {
            selection.setSelection(0, 0);
        }
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
