package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import javax.swing.Action;


/**
 * An action to activate the next annotation and start editing immediately.
 *
 * @author Han Sloetjes
 */
public class NextAnnotationEditCA extends CommandAction {
    /**
     * Constructor.
     *
     * @param theVM the viewermanager
     */
    public NextAnnotationEditCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.NEXT_ANNOTATION_EDIT);

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.ACTIVE_ANNOTATION_EDIT);
    }

    /**
     * Returns the viewer manager
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }

    /**
     * Finds the next annotatin to activate.
     *
     * @return an annotation or null in a 1 element array
     */
    @Override
	protected Object[] getArguments() {
        Annotation currentActiveAnnot = vm.getActiveAnnotation().getAnnotation();
        Annotation newActiveAnnot = null;

        if (currentActiveAnnot != null) {
            newActiveAnnot = ((TierImpl) (currentActiveAnnot.getTier())).getAnnotationAfter(currentActiveAnnot);

            //if (newActiveAnnot == null) {
            //    newActiveAnnot = currentActiveAnnot;
            //}
        } else { // try on basis of current time and active tier

            Tier activeTier = vm.getMultiTierControlPanel().getActiveTier();

            if (activeTier != null) {
                newActiveAnnot = ((TierImpl) activeTier).getAnnotationAfter(vm.getMasterMediaPlayer()
                                                                              .getMediaTime());
            }
        }

        Object[] args = new Object[1];
        args[0] = newActiveAnnot;

        return args;
    }
}
