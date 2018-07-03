package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;

public class ShowInBrowserCA extends CommandAction implements ActiveAnnotationListener {

	/**
     * Creates a new ShowInBrowserCA instance
     * @param viewerManager the ViewerManager
     */
    public ShowInBrowserCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SHOW_IN_BROWSER);
        viewerManager.connectListener(this);
    }
	
    /**
     * Constructor to be called by subclasses, otherwise the wrong "commandId" will be set.
     * @param viewerManager the viewer manager
     * @param name the name of the command
     */
    ShowInBrowserCA(ViewerManager2 viewerManager, String name) {
        super(viewerManager, name);
    }
    
	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.SHOW_IN_BROWSER);
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
    	Annotation annotation = vm.getActiveAnnotation().getAnnotation();
        if (annotation == null) {
            return;
        }
        if (!ShowInBrowserCommand.hasBrowserLinkInECV(annotation)) {
            return;
        }
        super.actionPerformed(event);
    }
	
	/**
     * @see mpi.eudico.client.annotator.ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
	public void updateActiveAnnotation() {
    	Annotation annotation = vm.getActiveAnnotation().getAnnotation();
        if (annotation != null) {
        	if(ShowInBrowserCommand.hasBrowserLinkInECV(annotation)) {
        		setEnabled(true);
        	} else {
        		setEnabled(false);
        	}
        } else {
            setEnabled(false);
        }        
    }
}
