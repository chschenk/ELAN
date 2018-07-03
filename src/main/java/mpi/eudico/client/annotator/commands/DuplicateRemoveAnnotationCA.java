package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Duplicates an annotation to the active tier and removes the original.
 *  
 * @author Han Sloetjes
 */
public class DuplicateRemoveAnnotationCA extends DuplicateAnnotationCA {
	/**
	 * Constructor.
	 * 
	 * @param viewerManager
	 */
	public DuplicateRemoveAnnotationCA(ViewerManager2 viewerManager) {
		super(viewerManager);
	}

	/**
	 * Create a different command then the duplicate command, one that
	 * also removes the original after duplication.
	 */
	@Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.DUPLICATE_REMOVE_ANNOTATION);
	}
	
}
