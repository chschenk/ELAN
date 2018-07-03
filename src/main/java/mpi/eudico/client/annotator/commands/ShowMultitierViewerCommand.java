package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;


/**
 * Tells the ViewerManager which Multitier Viewer should be visible.
 *
 * @author Han Sloetjes
 */
public class ShowMultitierViewerCommand implements Command {
    private String commandName;

    /**
     * Creates a new ShowMultitierViewerCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public ShowMultitierViewerCommand(String name) {
        commandName = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver the ViewerManager
     * @param arguments the arguments:  <ul><li>arg[0] = the multitier viewer
     *        to set visible (String)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        String com = (String) arguments[0];

        if (com == ELANCommandFactory.SHOW_TIMELINE) {
            ((ElanLayoutManager) receiver).showTimeLineViewer();
        } else if (com == ELANCommandFactory.SHOW_INTERLINEAR) {
            ((ElanLayoutManager) receiver).showInterlinearViewer();
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
