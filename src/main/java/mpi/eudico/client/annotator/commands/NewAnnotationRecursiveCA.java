package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A CommandAction for the recursive creation of new annotations on a tier.
 *
 */
@SuppressWarnings("serial")
public class NewAnnotationRecursiveCA extends NewAnnotationCA {
    /**
     * Creates a new NewAnnotationRecursiveCA instance
     *
     * @param viewerManager
     */
    public NewAnnotationRecursiveCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.NEW_ANNOTATION_REC);
    }

    /**
     * Before just creating a command check if it is possible to create a new
     * annotation and if so, on which tier. If receiver is <code>null</code>
     * no command is created (since the command should be undoable we don't
     * want to check in the command itself).
     */
    @Override
	protected void newCommand() {
        command = null;        

        if (checkState() && getReceiver() != null) {
    		 command = ELANCommandFactory.createCommand(vm.getTranscription(),
                     ELANCommandFactory.NEW_ANNOTATION_REC);  
        }
    }
}
