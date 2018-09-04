package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * An undoable command that builds on the create dependent annotation command.
 * It takes the active annotation as input, and creates new annotation recursively
 * on all the dependent tiers
 * This is then one undoable command in the undo/redo list.
 */
public class CreateDependentAnnotationsCommand	implements UndoableCommand{  
	
    private String commandName;
    private TierImpl tier;    
    TranscriptionImpl transcription;   
    private long begin;
    private long end;
    List<String> annotationTiers;    
    int numberOfannotationsCreated = 0;
    
    /**
     * Creates a new CreateDependentAnnotationsCommand instance
     * 
     * @param name the name of the command
     */
    public CreateDependentAnnotationsCommand(String name) {
    	commandName = name;
    }
    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.<br>     
     *
     * @param receiver the TierImpl
     * @param arguments the arguments: 
     * 		 <ul> 
     * 		<li>arg[0] = the begin time of the annotation (Long)</li> 
     * 		<li>arg[1] = the end time of the annotation (Long)</li> 
     * 		</ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {     	
    	tier = (TierImpl) receiver; 
    	begin = ((Long) arguments[0]).longValue();
        end = ((Long) arguments[1]).longValue();
        transcription = (tier.getTranscription());
        
        annotationTiers = new ArrayList<String>();        
        createDependingAnnotations();
        
        if (numberOfannotationsCreated > 0) {
        	if (MonitoringLogger.isInitiated()) {
	         	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.CREATE_DEPENDING_ANNOTATIONS, tier.getName(), tier.getAnnotationAtTime(begin).getValue(), Long.toString(begin), Long.toString(end), 
	         			"number of annotations created : " + numberOfannotationsCreated);
	        }
        }
    }
    
    public void createDependingAnnotations() {
    	annotationTiers.clear();
    	int curPropMode = 0;
        curPropMode = transcription.getTimeChangePropagationMode();
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }
        // setWaitCursor(true);
                
        List<TierImpl> dependentTiers = tier.getDependentTiers();
        
 	    if (dependentTiers != null) {
 	    	final int numTiers = dependentTiers.size();
			for (int i = 0; i < numTiers; i++) {	    		
 	        	TierImpl currentChildTier = dependentTiers.get(i);
 	        	if (currentChildTier.getAnnotationAtTime(begin) == null) {
 	        		annotationTiers.add(currentChildTier.getName());

 	        		if (currentChildTier.isTimeAlignable()) {
 	        			currentChildTier.createAnnotation(begin, end);
 	        		} else {
 	        			long time = (begin + end) / 2;
 	        			currentChildTier.createAnnotation(time, time);	            		
 	        		}
 	        		
 	        		numberOfannotationsCreated++;
 	        	}
 	        }
 	    	
 	     }   
 	    
 	    //setWaitCursor(false);
         // restore the time propagation mode
         transcription.setTimeChangePropagationMode(curPropMode);       
    }

    /**
     * The redo action.
     */
    @Override
	public void redo() {  
    	createDependingAnnotations();
    	if (numberOfannotationsCreated > 0) {
        	if (MonitoringLogger.isInitiated()) {
	         	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.CREATE_DEPENDING_ANNOTATIONS);
	        }
        }
    }
    
    /**
     * The undo action.
     */
    @Override
	public void undo() {    	
		 int curPropMode = 0;
         curPropMode = transcription.getTimeChangePropagationMode();

         if (curPropMode != Transcription.NORMAL) {
             transcription.setTimeChangePropagationMode(Transcription.NORMAL);
         }
        // setWaitCursor(true);
         
        final int numTiers = annotationTiers.size();

        for (int i = 0; i < numTiers; i++) {	    		
        	TierImpl currentChildTier = transcription.getTierWithId(annotationTiers.get(i));
 	        if (currentChildTier.isTimeAlignable()) {
 	        	Annotation ann = currentChildTier.getAnnotationAtTime(begin);
 	        	if (ann != null) {
 	        		currentChildTier.removeAnnotation(ann);
 	        	} 	        	
 	        } else {
 	        	long time = (begin + end) / 2;
 	        	Annotation ann = currentChildTier.getAnnotationAtTime(time);
 	        	if (ann != null) {
 	        		currentChildTier.removeAnnotation(ann);
 	        	} 	
 	        }
 	     } 
        
        numberOfannotationsCreated = 0 ;
        if (MonitoringLogger.isInitiated()) {
         	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.CREATE_DEPENDING_ANNOTATIONS);
        }
         
        // setWaitCursor(false);

         // restore the time propagation mode
         transcription.setTimeChangePropagationMode(curPropMode);
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
}
