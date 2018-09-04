package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;


/**
 * A CommandAction that creates a dialog by which the data category reference
 * of an annotation can be set.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationDatCatCA extends CommandAction
    implements ActiveAnnotationListener {
    private Annotation activeAnnotation;

    /**
     * Constructor.
     *
     * @param viewerManager the viewer manager
     */
    public ModifyAnnotationDatCatCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MODIFY_ANNOTATION_DC);

        viewerManager.connectListener(this);
        setEnabled(false);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.MODIFY_ANNOTATION_DC_DLG);
    }

    /**
     * The receiver of this CommandAction is the Annotation that should be
     * modified.
     *
     * @return the active annotation
     */
    @Override
	protected Object getReceiver() {
        return activeAnnotation;
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#getArguments()
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getTranscription() };
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
     * If there is an active annotation the action is enabled, otherwise
     * disabled.
     */
    protected void checkState() {
        if (activeAnnotation != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }
}
