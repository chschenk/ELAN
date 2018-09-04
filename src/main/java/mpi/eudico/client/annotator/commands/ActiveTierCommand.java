package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.viewer.MultiTierControlPanel;

/**
 * Sets the next (down) or previous (up) tier active in the multitier control
 * panel  and thus in the TimeLineViewer.
 *
 * @author Han Sloetjes
 */
public class ActiveTierCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param commandName the name of the command
     */
    public ActiveTierCommand(String commandName) {
        this.commandName = commandName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the MultiTierControlPanel
     * @param arguments the arguments:  <ul><li>arg[0] = the direction of
     *        activation,  next tier (vertically down) or previous tier
     *        (vertically up). True means next (down), false means previous
     *        (down).  (Boolean)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
		MultiTierControlPanel controlPanel = (MultiTierControlPanel) receiver;
        boolean next = ((Boolean) arguments[0]).booleanValue();
        controlPanel.setNextActiveTier(next);
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }
}
