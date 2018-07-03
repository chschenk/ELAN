package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.search.viewer.ElanSearchFrame;


/**
 * A command for creating a search dialog.
 *
 * @author Han Sloetjes
 */
public class SearchDialogCommand implements Command {
    private String commandName;

    /**
     * Creates a new SearchDialogCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public SearchDialogCommand(String name) {
        commandName = name;
        ;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null, ther is no clear receiver object for this command
     * @param arguments the arguments:  <ul><li>arg[0] = the root frame for the
     *        dailog (Frame)</li> <li>arg[1] = the Transcription to work upon
     *        (Transcription)</li> <li>arg[2] = the Selection object; the
     *        selection can be set from  within the result table and the
     *        result table should reflect selection changes (Selection)</li>
     *        <li>arg[3] = the ActiveAnnotation object, containing the
     *        currently active Annotation (ActiveAnnotation)</li> <li>arg[4] =
     *        the master media player; the player can be stopped and mediatime
     *        can be set from  within the resullt panel (ElanMediaPlayer)</li>
     *        <li>arg[5] = the Identity, can be null (Identity)</li> </ul>
     */

    /*
       public void execute(Object receiver, Object[] arguments) {
           new SearchDialog(
               (Frame)arguments[0],
               (Transcription)arguments[1],
               (Selection)arguments[2],
               (ActiveAnnotation)arguments[3],
               (ElanMediaPlayer)arguments[4],
               (Identity)arguments[5]).setVisible(true);
       }
     */

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null, ther is no clear receiver object for this command
     * @param arguments the arguments:  <ul><li>arg[0] = the ViewerManager for
     *        this document/frame (ViewerManager)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) arguments[0];
        new ElanSearchFrame(vm).setVisible(true);
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
