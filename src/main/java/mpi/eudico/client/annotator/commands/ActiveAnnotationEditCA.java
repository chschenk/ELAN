package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;


/**
 * An action to start editing on a active annotation.
 *
 * @author Han Sloetjes
 */
public class ActiveAnnotationEditCA extends CommandAction {
    /**
     * Constructor.
     *
     * @param theVM the viewermanager
     */
    public ActiveAnnotationEditCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.ACTIVE_ANNOTATION_EDIT);
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.ACTIVE_ANNOTATION_EDIT);
    }

    /**
     * Returns the viewer manager
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }

    /**
     * Finds the active annotation 
     *
     * @return an annotation or null in a 1 element array
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm.getActiveAnnotation().getAnnotation();
        return args;
    }
}

