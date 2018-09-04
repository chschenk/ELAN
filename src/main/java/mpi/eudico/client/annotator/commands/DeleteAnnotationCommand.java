package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A command for deleting an annotation.
 *
 * @author Han Sloetjes
 */
public class DeleteAnnotationCommand implements UndoableCommand {
    private String commandName;
    private AbstractAnnotation annotation;
    private TierImpl tier;
    private DefaultMutableTreeNode annRootNode;
    private TranscriptionImpl transcription;
    private boolean annHasChildren = false;

    /**
     * Creates a new DeleteAnnotationCommand instance.
     *
     * @param name the name of the command
     */
    public DeleteAnnotationCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action.
     */
    @Override
	public void undo() {
        if ((tier != null) && (annRootNode != null)) {
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            setWaitCursor(true);
            if (annHasChildren) {
            	transcription.setNotifying(false);
            }

            AnnotationRecreator.createAnnotationFromTree(tier.getTranscription(),
                annRootNode, true);
            
            if(MonitoringLogger.isInitiated()){        	
            	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.DELETE_ANNOTATION);        				
            }

            setWaitCursor(false);
            if (annHasChildren) {
            	transcription.setNotifying(true);
            }

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
        }
    }

    /**
     * The redo action.
     */
    @Override
	public void redo() {
        if ((tier != null) && (annRootNode != null)) {
            setWaitCursor(true);

            AnnotationDataRecord annRecord = (AnnotationDataRecord) annRootNode.getUserObject();
            long begin = annRecord.getBeginTime();
            long end = annRecord.getEndTime();
            long mid = (begin + end) / 2;
            Annotation aa = tier.getAnnotationAtTime(mid);

            if (aa != null) {
                tier.removeAnnotation(aa);
            }
            
            if(MonitoringLogger.isInitiated()){        	
            	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.CHANGE_ANNOTATION_TIME);        				
            }

            setWaitCursor(false);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the TierImpl
     * @param arguments the arguments:  <ul><li>arg[0] = the Viewer Manager
     *        (ViewerManager2)</li> <li>arg[1] = the annotation
     *        (Annotation)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) arguments[0];
        transcription = (TranscriptionImpl) vm.getTranscription();
        tier = (TierImpl) receiver;

        setWaitCursor(true);

        annotation = (AbstractAnnotation) arguments[1];

        if (vm.getActiveAnnotation().getAnnotation() == annotation) {
            vm.getActiveAnnotation().setAnnotation(null);
        }
        
		annHasChildren = !annotation.getParentListeners().isEmpty();

        annRootNode = AnnotationRecreator.createTreeForAnnotation(annotation);

        tier.removeAnnotation(annotation);
        
        if(MonitoringLogger.isInitiated()){        	
        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.DELETE_ANNOTATION);        				
        }

        setWaitCursor(false);
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getDefaultCursor());
        }
    }
}
