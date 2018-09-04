package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;

/**
 * An action that listen to active annotation updates in order to enable/disable itself.
 * It creates a new {@link ModifyAnnotationTimeDlgCommand} which creates a dialog for 
 * modifying the active annotation's boundaries.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ModifyAnnotationTimeDlgCA extends CommandAction implements ActiveAnnotationListener {
    private Annotation activeAnnotation;
    private Object[] args = new Object[1];
    
    /**
     * Constructor.
     * 
     * @param viewerManager the viewer manager
     */
	public ModifyAnnotationTimeDlgCA(ViewerManager2 viewerManager) {
		super(viewerManager, ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG);
		
        viewerManager.connectListener(this);
        setEnabled(false);
	}

	@Override
	protected void newCommand() {
		command = ELANCommandFactory.createCommand(vm.getTranscription(), 
				ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG);
	}

	/**
	 * @return the viewer manager (for practical reasons)
	 */
	@Override
	protected Object getReceiver() {
		return vm;
	}

	/**
	 * @return an array with the annotation at index 0 
	 */
	@Override
	protected Object[] getArguments() {
		args[0] = activeAnnotation;
		return args;
	}

	/**
	 * Checks if there is an active annotation and if it is alignable
	 */
	@Override
	public void updateActiveAnnotation() {
		activeAnnotation = vm.getActiveAnnotation().getAnnotation();
		
		if (activeAnnotation instanceof AlignableAnnotation) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
	
}
