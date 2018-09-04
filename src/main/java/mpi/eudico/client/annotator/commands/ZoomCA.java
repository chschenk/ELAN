package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action for zooming in or out or zooming to a specific value,
 * specifically by means of a keyboard shortcut.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public abstract class ZoomCA extends CommandAction {
	// zoom in = 1, zoom out = -1, zoom "default" time scale = 0, 
	// values > 10 are specific scale percentages, e.g. 50% 
	protected Object[] arg = null;
	// the commandId should be / could be in the properties map of the action, e.g. ACTION_COMMAND_KEY
	protected String actionName;
	
	/**
	 * Constructor.
	 * 
	 * @param theVM the viewer manager
	 * @param name the name of the command
	 */
	public ZoomCA(ViewerManager2 theVM, String name) {
		super(theVM, name);
		actionName = name;
	}
	
	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(), actionName);
	}

	/**
	 * @return the layout manager.
	 */
	@Override
	protected Object getReceiver() {
		return ELANCommandFactory.getLayoutManager(vm.getTranscription());
	}
	
	/**
	 * @return the argument that indicates whether to zoom in, out or to a specific value.
	 */
	@Override
	protected Object[] getArguments() {
		return arg;
	}

}
