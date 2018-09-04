package mpi.eudico.client.annotator.commands;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * A command action for duplicating (copy and paste in a single operation) an annotation.
 * Based on the active annotation and the active tier in TimeLineViewer or InterlinearViewer.
 *
 * @author Han Sloetjes
 */
public class DuplicateAnnotationCA extends CommandAction 
	implements ActiveAnnotationListener {

    /**
     * Creates a new DuplicateAnnotationCA instance 
     * 
     * @param viewerManager the Viewer Manager
     */
    public DuplicateAnnotationCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DUPLICATE_ANNOTATION);

        viewerManager.connectListener(this);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
                       ELANCommandFactory.DUPLICATE_ANNOTATION);
    }

    /**
     * The receiver of this CommandAction is the viewer manager.
     *
     * @return the viewer manager
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }

    /**
     * The active tier and the active annotation. 
     * 
     * @return an Object array size = 2
     */
    @Override
	protected Object[] getArguments() {
        return new Object[]{vm.getMultiTierControlPanel().getActiveTier(), 
                vm.getActiveAnnotation().getAnnotation()};
    }
    
    /**
     * Override ationPerformed to perform some initial checks before calling super.actionPerformed, 
     * where a new Command is created etc.
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (vm.getActiveAnnotation().getAnnotation() == null) {
            return;
        }
        if (vm.getMultiTierControlPanel() == null || vm.getMultiTierControlPanel().getActiveTier() == null
                || vm.getMultiTierControlPanel().getActiveTier() == vm.getActiveAnnotation().getAnnotation().getTier()) {
            return;
        }
       TierImpl t = (TierImpl) vm.getMultiTierControlPanel().getActiveTier() ;
       Constraint c = t.getLinguisticType().getConstraints();
       
       if (c != null && (c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION || c.getStereoType() == 
           Constraint.SYMBOLIC_SUBDIVISION)) {
           long mid = (vm.getActiveAnnotation().getAnnotation().getBeginTimeBoundary() + 
           		vm.getActiveAnnotation().getAnnotation().getEndTimeBoundary()) / 2;
           if (t.getAnnotationAtTime(mid) != null) {
               return;
           } else {
               TierImpl par = (TierImpl) t.getParentTier();
               
               if (par.getOverlappingAnnotations(
                       vm.getActiveAnnotation().getAnnotation().getBeginTimeBoundary(),
                       vm.getActiveAnnotation().getAnnotation().getEndTimeBoundary()).size() == 0) {
                   return;
               }
           }
       } else if (c != null && (c.getStereoType() == Constraint.TIME_SUBDIVISION || c.getStereoType() == 
           Constraint.INCLUDED_IN)) {
           TierImpl par = (TierImpl) t.getParentTier();
           Annotation ann1 = par.getAnnotationAtTime(vm.getActiveAnnotation().getAnnotation().getBeginTimeBoundary());
           Annotation ann2 = par.getAnnotationAtTime(vm.getActiveAnnotation().getAnnotation().getEndTimeBoundary());
           
           if (ann1 != null && ann2 != null && ann1 != ann2) {
               //return; // don't choose?
           } else if (ann1 == null && ann2 == null && par.getOverlappingAnnotations(
                   vm.getActiveAnnotation().getAnnotation().getBeginTimeBoundary(), 
                   vm.getActiveAnnotation().getAnnotation().getEndTimeBoundary()).size() == 0) {
               return;
           }
       }
           
        // let a command be created
        super.actionPerformed(event);
    }
    
    /**
     * @see mpi.eudico.client.annotator.ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
	public void updateActiveAnnotation() {
        if (vm.getActiveAnnotation().getAnnotation() != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }        
    }
}
