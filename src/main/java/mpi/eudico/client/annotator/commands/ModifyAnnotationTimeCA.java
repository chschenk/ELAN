package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;


/**
 * A command action for modifying an annotation's begin and end time.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationTimeCA extends CommandAction {
    /**
     * Creates a new ModifyAnnotationTimeCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public ModifyAnnotationTimeCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MODIFY_ANNOTATION_TIME);
    }

    /**
     * Creates a new ModifyAnnotationTimeCommand. Checks the selection's begin-
     * and endtime as well as the current annotation's begin- and endtime to
     * avoid the  creation of meaningless undoable commands.
     */
    @Override
	protected void newCommand() {
        if (vm.getActiveAnnotation().getAnnotation() instanceof AlignableAnnotation) {
            AlignableAnnotation aa = (AlignableAnnotation) vm.getActiveAnnotation()
                                                             .getAnnotation();

            if ((vm.getSelection().getBeginTime() != vm.getSelection()
                                                           .getEndTime()) &&
                    ((aa.getBeginTimeBoundary() != vm.getSelection()
                                                         .getBeginTime()) ||
                    (aa.getEndTimeBoundary() != vm.getSelection().getEndTime()))) {
                command = ELANCommandFactory.createCommand(vm.getTranscription(),
                        ELANCommandFactory.MODIFY_ANNOTATION_TIME);
            } else {
                command = null;
            }
        } else {
            command = null;
        }
    }

    /**
     * The receiver of this CommandAction is the Annotation that should be
     * modified.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getActiveAnnotation().getAnnotation();
    }

    /**
     * As arguments the new begin and end time are passed.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] {
            new Long(vm.getSelection().getBeginTime()),
            new Long(vm.getSelection().getEndTime())
        };
    }
}
