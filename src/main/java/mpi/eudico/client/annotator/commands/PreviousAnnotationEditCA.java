package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import javax.swing.Action;


/**
 * Activates the previous annotation and starts editing it.
 * 
 * @author HS
 * @version 1.0
  */
public class PreviousAnnotationEditCA extends CommandAction {
    /**
     * Creates a new PreviousAnnotationEditCA instance
     *
     * @param theVM the viewermanager
     */
    public PreviousAnnotationEditCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.PREVIOUS_ANNOTATION_EDIT);

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
     * Returns the previous annotation in an array of size one.
     *
     * @return the previous annotation in an array of size one
     */
    @Override
	protected Object[] getArguments() {
        Annotation currentActiveAnnot = vm.getActiveAnnotation().getAnnotation();
        Annotation newActiveAnnot = null;

        if (currentActiveAnnot != null) {
            newActiveAnnot = ((TierImpl) (currentActiveAnnot.getTier())).getAnnotationBefore(currentActiveAnnot);

            //if (newActiveAnnot == null) {
            //    newActiveAnnot = currentActiveAnnot;
            //}
        } else { // try on basis of current time and active tier

            Tier activeTier = vm.getMultiTierControlPanel().getActiveTier();

            if (activeTier != null) {
                newActiveAnnot = ((TierImpl) activeTier).getAnnotationBefore(vm.getMasterMediaPlayer()
                                                                               .getMediaTime());
            }
        }

        Object[] args = new Object[1];
        args[0] = newActiveAnnot;

        return args;
    }
}
