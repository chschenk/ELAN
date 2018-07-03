package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.TimePanel;


/**
 *
 */
public class GoToDialogCommand implements Command {
    private String commandName;
    private TimePanel timepanel;

    /**
     * Creates a new GoToDialogCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public GoToDialogCommand(String name) {
        commandName = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        timepanel = (TimePanel) (arguments[0]);
        timepanel.showCrosshairTimeInputBox();
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
