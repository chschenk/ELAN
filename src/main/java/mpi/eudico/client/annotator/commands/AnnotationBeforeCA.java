package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;


/**
 * A command action for creating a new annotation before a given annotation.
 *
 * @author Han Sloetjes
 */
public class AnnotationBeforeCA extends CommandAction
    implements ActiveAnnotationListener {
    private Annotation activeAnnotation;

    /**
     * Creates a new AnnotationBeforeCA instance
     *
     * @param viewerManager the ViewerManager
     */
    public AnnotationBeforeCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.NEW_ANNOTATION_BEFORE);
                
        viewerManager.connectListener(this);
        setEnabled(false);
    }

    /**
     * Creates a new <code>Command</code>.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.NEW_ANNOTATION_BEFORE);
    }

    /**
     * The receiver of this CommandAction is the TierImpl object on which the
     * new annotation should be created.
     *
     * @return the receiver
     */
    @Override
	protected Object getReceiver() {
        //return viewerManager.getActiveAnnotation().getAnnotation().getTier;
        return activeAnnotation.getTier();
    }

    /**
     * Returns the arguments for the related Command.
     *
     * @return the arguments for the related Command
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = activeAnnotation;

        return args;
    }

    /**
     * On a change of ActiveAnnotation perform a check to determine whether
     * this action should be enabled or disabled.<br>
     * This depends on the type of the annotation and the type of the Tier it
     * belongs to.
     *
     * @see ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
	public void updateActiveAnnotation() {
        activeAnnotation = vm.getActiveAnnotation().getAnnotation();
        checkState();
    }

    /**
     * Enables or disables this <code>Action</code> depending on the caracteristics
     * of the active annotation (and therefore the tier it is on), if any.
     */
    protected void checkState() {
        setEnabled(false);

        if (activeAnnotation == null) {
            return;
        }

        TierImpl tier = (TierImpl) activeAnnotation.getTier();


        Constraint c = tier.getLinguisticType().getConstraints();

        if (c != null && c.supportsInsertion()) {
        	setEnabled(true);
        }
    }
    
}
