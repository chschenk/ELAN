package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.viewer.TimeLineViewer;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;


/**
 * A command that activates a annotation and starts editing
 * at once.
 *
 * @author Han Sloetjes
 * @version Aug 2008
 */
public class ActiveAnnotationEditCommand implements Command {
	 private String name;
  
    /**
     * Creates a new ActiveAnnotationEditCommand instance
     *
     * @param name the name
     */
    public ActiveAnnotationEditCommand(String name) {
    	this.name = name;
    }

    /**
     * <b>Note: </b>it is assumed that the types and order of the arguments are
     * correct.
     *
     * @param receiver the ViewerManager
     * @param arguments the arguments:  <ul><li>arg[0] = the annotation to
     *        activate and edit (Annotation)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) receiver;
        Annotation annot = (Annotation) arguments[0];

        if (annot == null) {
            return;
        }
        
        // if the selected annotation is not the active one
        if(vm.getActiveAnnotation().getAnnotation() != annot){
        	vm.getActiveAnnotation().setAnnotation(annot);
        }
        
        // center the editing annotation in the timeline viewer
        //super.execute(receiver, arguments);

        // should the selection be set??
        if (annot instanceof AlignableAnnotation) {
            vm.getSelection()
              .setSelection(annot.getBeginTimeBoundary(),
                annot.getEndTimeBoundary());

            if (!vm.getMediaPlayerController().isBeginBoundaryActive()) {
                vm.getMediaPlayerController().toggleActiveSelectionBoundary();
            }
        } else if (annot instanceof RefAnnotation) {
            Annotation parent = annot;

            while (true) {
                parent = parent.getParentAnnotation();

                if ((parent == null) || parent instanceof AlignableAnnotation) {
                    break;
                }
            }

            if (parent instanceof AlignableAnnotation) {
                AlignableAnnotation aa = (AlignableAnnotation) parent;
                vm.getSelection()
                  .setSelection(aa.getBeginTimeBoundary(),
                    aa.getEndTimeBoundary());
            }
        }

        final ElanLayoutManager layoutManager = ELANCommandFactory.getLayoutManager(vm.getTranscription());
		int mode = layoutManager.getMode();

		if (mode == ElanLayoutManager.NORMAL_MODE) {
            if (layoutManager.getVisibleMultiTierViewer() instanceof TimeLineViewer) {
                layoutManager.getTimeLineViewer()
                             .showEditBoxForAnnotation(annot);
            } else { // only 2 options now
                layoutManager.getInterlinearViewer()
                             .showEditBoxForAnnotation(annot);
            }
        }
    }
    
    /**
     * Returns the name
     *
     * @return name
     */
    @Override
	public String getName() {
        return name;
    }
}
