package mpi.eudico.client.annotator.commands;

import java.util.List;

import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * An undoable command that builds on the new annotation command.
 * It takes the active annotation as input, creates a new annotation and sets the value for the new annotation
 * to be the same as the input annotation. This is then one undoable command in the undo/redo list.
 */
public class DuplicateAnnotationCommand extends NewAnnotationCommand {
    /* 
     * TODO maybe this command should do the same as the paste command 
     * and use a AnnotationDataRecord? 
     */
	private String value;
    private ExternalReference extRef;
    private String cvEntryId;
    
    /**
     * Creates a new DuplicateAnnotationCommand instance
     * 
     * @param name the name of the command
     */
    public DuplicateAnnotationCommand(String name) {
        super(name);
    }

    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the ViewerManager
     * @param arguments the arguments:  <ul><li>arg[0] = the tier
     *        (TierImpl)</li> <li>arg[1] = the annotation to duplicate
     *        (Annotation)</li>  </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        final ViewerManager2 vm = (ViewerManager2) receiver;
        TierImpl t = (TierImpl) arguments[0];
        AbstractAnnotation ann = (AbstractAnnotation) arguments[1];
        value = ann.getValue();
        cvEntryId = ann.getCVEntryId();
        // adjust bt and et if necesary
        long[] times = adjustTimes(t, ann.getBeginTimeBoundary(), ann.getEndTimeBoundary());
        
        if (times != null) { 
        	// prevent that an edit box (drop down list in case of a controlled vocabulary) is shown
        	// in case of a CV the first value of the list would be applied to the duplicate
        	((TranscriptionImpl)vm.getTranscription()).setNotifying(false);
        	
	        super.execute(t, new Object[]{new Long(times[0]), new Long(times[1])});
	        
	        // set value
	        if (newAnnotation != null) {
	            newAnnotation.setValue(value);
	            // if the original annotation has external references, copy them as well
	            if (ann.getExtRef() != null) {
	            	try {
	            		extRef = ann.getExtRef().clone();
	            		((AbstractAnnotation) newAnnotation).setExtRef(extRef);
	            	} catch (CloneNotSupportedException cnse) {
	            		// ignore
	            	}
	            }
	            newAnnotation.setCVEntryId(cvEntryId);
	        }

	        transcription.setNotifying(true);
	        
	        Command c = ELANCommandFactory.createCommand(vm.getTranscription(),
	                ELANCommandFactory.ACTIVE_ANNOTATION);
	        c.execute(vm, new Object[] { ann });
	        
	        // HS July 2008: after the actions above the focus is completely lost, 
	        // none of the keyboard shortcuts seem to function
	        // try to correct this by giving the frame the focus
            SwingUtilities.invokeLater(new Runnable(){
                @Override
				public void run() {
                    ELANCommandFactory.getRootFrame(vm.getTranscription()).requestFocus();
                }
            });
        }
    }
    
    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
    	transcription.setNotifying(false);
    	
        super.redo();
        
        
        if (newAnnotation != null) {
            newAnnotation.setValue(value);
            
            if (extRef != null) {
            	((AbstractAnnotation) newAnnotation).setExtRef(extRef);
            }
            
            newAnnotation.setCVEntryId(cvEntryId);
        }
        
        transcription.setNotifying(true);
    }
    
    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
        super.undo();     
    }
    
    /**
     * Adjust the begin and end time for the new annotation based on tier type and parent tier 
     * and parent annotation.
     *  
     * @param tier the destination tier
     * @param begin the begin time of the original annotation
     * @param end the end time of the original annotation
     * @return the adjusted begin and end time in an array
     */
    private long[] adjustTimes(TierImpl tier, long begin, long end) {
        long[] result = new long[]{begin, end};
        
        Constraint c = tier.getLinguisticType().getConstraints();
        if (c == null) {
            return result;
        }
        if (c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION || c.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) {
           
            TierImpl par = tier.getParentTier();
            long mid = (begin + end) / 2;
            if (tier.getAnnotationAtTime(mid) == null && par.getAnnotationAtTime(mid) != null) {
                result[0] = mid;
                result[1] = mid;
            } else if (tier.getAnnotationAtTime(begin) == null && par.getAnnotationAtTime(begin) != null) {
                result[1] = begin;
            } else if (tier.getAnnotationAtTime(end) == null && par.getAnnotationAtTime(end) != null) {
                result[0] = end;
            } else {
                // find any other overlapping annotation?
                return null;
            }
        } else if (c.getStereoType() == Constraint.INCLUDED_IN || c.getStereoType() == Constraint.TIME_SUBDIVISION) {
            TierImpl par = tier.getParentTier();
            Annotation ann1 = par.getAnnotationAtTime(begin);
            Annotation ann2 = par.getAnnotationAtTime(end); 
            
            if (ann1 != null && ann2 != null && ann1 != ann2) {
                result[1] = ann1.getEndTimeBoundary();
            } else if (ann1 != null && ann2 == null) {
                result[1] = ann1.getEndTimeBoundary();
            } else if (ann1 == null && ann2 != null) {
                result[0] = ann2.getBeginTimeBoundary();
            } else {
                List<Annotation> v = par.getOverlappingAnnotations(begin, end);
                
                if (v.size() == 0) {
                    return null;
                } else {
                    ann1 = v.get(0);// take the first one
                    result[0] = ann1.getBeginTimeBoundary();
                    result[1] = ann1.getEndTimeBoundary();
                }
            }
        }
        
        return result;
    }
}
