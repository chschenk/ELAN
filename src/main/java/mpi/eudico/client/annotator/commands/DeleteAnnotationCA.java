package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;


/**
 * A command action for deleting an annotation.
 *
 * @author Han Sloetjes
 */
public class DeleteAnnotationCA extends CommandAction
    implements ActiveAnnotationListener {
    private Annotation activeAnnotation;

    /**
     * Creates a new DeleteAnnotationCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public DeleteAnnotationCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_ANNOTATION);
        viewerManager.connectListener(this);
        setEnabled(false);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.DELETE_ANNOTATION);
    }

    /**
     * The receiver of this CommandAction is the TierImpl object from which the
     * annotation should be removed.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return activeAnnotation.getTier();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm;
        args[1] = activeAnnotation;

        return args;
    }

    /**
     * On a change of ActiveAnnotation perform a check to determine whether
     * this action should be enabled or disabled.<br>
     *
     * @see ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
	public void updateActiveAnnotation() {
        activeAnnotation = vm.getActiveAnnotation().getAnnotation();
        checkState();
    }

    /**
     * DOCUMENT ME!
     */
    protected void checkState() {
        if (activeAnnotation != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }
}
