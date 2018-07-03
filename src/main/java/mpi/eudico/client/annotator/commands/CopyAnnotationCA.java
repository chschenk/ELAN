package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A CommandAction to copy an annotation (i.e. a transferable AnnotationDataRecord) to the System's 
 * Clipboard.
 */
public class CopyAnnotationCA extends CommandAction implements ActiveAnnotationListener {

    /**
     * Creates a new CopyAnnotationCA instance
     * @param viewerManager the ViewerManager
     */
    public CopyAnnotationCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.COPY_ANNOTATION);
        viewerManager.connectListener(this);
    }
    
    /**
     * Constructor to be called by subclasses, otherwise the wrong "commandId" will be set.
     * @param viewerManager the viewer manager
     * @param name the name of the command
     */
    CopyAnnotationCA(ViewerManager2 viewerManager, String name) {
        super(viewerManager, name);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.COPY_ANNOTATION);
    }

    /**
     * The active annotation. 
     * 
     * @return an Object array size = 1
     */
    @Override
	protected Object[] getArguments() {
        return new Object[]{ vm.getActiveAnnotation().getAnnotation() };
    }
    
    
    /**
     * Don't create a command when there is no active annotation or if there is no clipbaord access.
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (vm.getActiveAnnotation().getAnnotation() == null) {
            return;
        }
        if (!canAccessSystemClipboard()) {
            return;
        }
        super.actionPerformed(event);
    }
    
    /**
     * Performs a check on the accessibility of the system clipboard.
     *
     * @return true if the system clipboard is accessible, false otherwise
     */
    protected boolean canAccessSystemClipboard() {

        if (System.getSecurityManager() != null) {
            try {
                System.getSecurityManager().checkSystemClipboardAccess();

                return true;
            } catch (SecurityException se) {
                se.printStackTrace();

                return false;
            }
        }

        return true;
    }

    /**
     * @see mpi.eudico.client.annotator.ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
	public void updateActiveAnnotation() {
        if (vm.getActiveAnnotation().getAnnotation() != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }        
    }
}
