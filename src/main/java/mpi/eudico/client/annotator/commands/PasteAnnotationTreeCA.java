package mpi.eudico.client.annotator.commands;

import javax.swing.Action;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A CoammandAction to paste a complete annotation tree from the system clipboard.
 */
public class PasteAnnotationTreeCA extends PasteAnnotationCA {

    /**
     * @param viewerManager
     */
    public PasteAnnotationTreeCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PASTE_ANNOTATION_TREE);
        putValue(Action.NAME, ELANCommandFactory.PASTE_ANNOTATION_TREE);
        updateLocale();
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.PASTE_ANNOTATION_TREE);
    }
    
}
