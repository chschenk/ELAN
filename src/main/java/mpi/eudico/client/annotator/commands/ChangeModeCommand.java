package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;


/**
 * Switches between different modes.
 *
 * @author Aarthy Somasundaram
 */
public class ChangeModeCommand implements Command {
    private String commandName;

    /**
     * Creates a new AnnotationModeCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public ChangeModeCommand(String name) {
        commandName = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver the ViewerManager
     * @param arguments the arguments:
     * 
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
    	
    	// receiver is master ElanMediaPlayerController
        // arguments[0] is current mode - integer
    	
        ((ElanLayoutManager) receiver).changeMode((Integer)arguments[0]);
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
