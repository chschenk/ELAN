package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.AnnotationTransfer;


/**
 * A CommandAction to paste an annotation (i.e. a transferable AnnotationDataRecord) from the System's 
 * Clipboard to a receiving transcription.
 * 
 * Registration as MenuListener to the Menu for en/disabling the action is not sufficient since there 
 * is an acceleration key. What is needed is to register a Clipboard listener, which is possible 
 * in j2se 1.5 or higher.
 */
public class PasteAnnotationCA extends CommandAction {

    /**
     * Creates a new PasteAnnotationCA instance
     * @param viewerManager the ViewerManager
     */
    public PasteAnnotationCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PASTE_ANNOTATION);
    }
    
    /**
     * Constructor to be called by subclasses; otherwise the coammandId will not be set properly.
     * @param viewerManager the viewer manager
     * @param name the name of the command
     */
    PasteAnnotationCA(ViewerManager2 viewerManager, String name) {
        super(viewerManager, name);
    }
    
    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                ELANCommandFactory.PASTE_ANNOTATION);
    }

    
    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }
 
    
    /**
     * Don't create a command if there is no access to the clipboard.
     * Or if there is no or unusable contents on the clipboard.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (!AnnotationTransfer.validContentsOnClipboard()) {
            return;
        }
        
        super.actionPerformed(event);
    }
    
}
