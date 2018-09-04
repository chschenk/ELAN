package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * An action to merge two adjacent annotations on a toplevel tier. 
 * The first annotation is the active annotation.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class MergeAnnotationWithNextCA extends CommandAction
    implements ActiveAnnotationListener {
    /**
     * Creates a new MergeAnnotationWithNextCA instance
     *
     * @param viewerManager the viewer manager
     */
    public MergeAnnotationWithNextCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MERGE_ANNOTATION_WN);

        viewerManager.connectListener(this);
        setEnabled(false);
    }

    /**
     * Creates a new merge annotations command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.MERGE_ANNOTATION_WN);
    }

    /**
     * Returns the receiver of the command, the transcription.
     *
     * @return the receiver of the command
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns an object array of size 2).
     * 
     * argument[0] = active annotation 
     * argument[1] = true( merge with next annotation), if
     * 				false, merge with annotation before
     *     
     * @return object array
     */
    @Override
	protected Object[] getArguments() {
    	Object [] arguments = new Object[2];
    	arguments[0] =  vm.getActiveAnnotation().getAnnotation();
    	arguments[1] = true;
    	
        return arguments;
    }

    /**
     * On a change of ActiveAnnotation perform a check to determine whether
     * this action should be enabled or disabled.<br>
     * If the annotation is on a top level tier the action is enabled
     *
     * @see ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
	public void updateActiveAnnotation() {
        if (vm.getActiveAnnotation().getAnnotation() == null) {
            setEnabled(false);
        } else {
            // only for top level tiers for now
            // could check if there is a next annotation
            setEnabled(!((TierImpl) vm.getActiveAnnotation().getAnnotation()
                                      .getTier()).hasParentTier());
        }
    }
}
