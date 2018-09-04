package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.List;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Command that splits the active annotation into two annotations
 * if invoked from the context menu, then split is done at the point where
 * its invoked, otherwise it is split equally
 *
 * @author Aarthy Somasundaram
 * @version 1.0
 * @version 2.0 HS August 2015: added two more, optional, arguments; text, which can be different from
 * the original text of the annotation, and the index where the text should be split into a part for the
 * current annotation and a part for the new annotation
 */
public class SplitAnnotationCommand implements UndoableCommand {
    private String commandName;
    private AnnotationDataRecord annotationRecord;
    private Transcription transcription;
    long splitTime;
    private String[] splitText; // if null, take the current text of the annotation
    
    private boolean annotationsSplit = false;
    private boolean annHasChildren = false;

    
    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public SplitAnnotationCommand(String name) {
        commandName = name;
    }    
    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  (can be null)
     * 			<ul><li>arg[0] = the annotation (Annotation)
     * 				<li>arg[1] = splitTime, 
     * 					time to split the annotation (long)</li> 
     * 				<li>arg[2] = text, possibly new (String, optional)</li>
     * 				<li>arg[3] = the position within the new text where the text should be split (Integer, optional)</li></ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {    	
    	transcription = (Transcription) receiver;
    	
    	AlignableAnnotation annotation = null;
		if (arguments[0] instanceof AlignableAnnotation) {
			annotation = (AlignableAnnotation) arguments[0];
		}
		if (annotation != null) {
			long begin = annotation.getBeginTimeBoundary();
			long end = annotation.getEndTimeBoundary();
			if (arguments.length > 1 && arguments[1] != null) {
				splitTime = (Long) arguments[1];
			} else {
				splitTime = (begin + end) / 2;
			}
			annHasChildren = !annotation.getParentListeners().isEmpty();
		}
        
        if (arguments.length >= 4) {
        	String textToSplit = (String) arguments[2];
        	if (textToSplit == null) {
        		textToSplit = annotation.getValue();
        	}
        	int splitIndex = (Integer) arguments[3];
        	splitText = splitTextInTwo(textToSplit, splitIndex);
        }
    	
    	splitAnnotation(annotation);    
    	if(annotationsSplit && MonitoringLogger.isInitiated()){
			   MonitoringLogger.getLogger(transcription).log(MonitoringLogger.SPLIT_ANNOTATION);
		}
    }    
    
    protected void splitAnnotation(AlignableAnnotation annotation){
    	int curPropMode = 0;
        curPropMode = transcription.getTimeChangePropagationMode();
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }
        setWaitCursor(true);
        if (annHasChildren) {// make sure the ACM events are more specific in case of no depending annotations
        	((TranscriptionImpl) transcription).setNotifying(false);
        }
        
    	if(annotation != null){
    	   long begin = annotation.getBeginTimeBoundary();
    	   long end = annotation.getEndTimeBoundary();  
    	   
    	   TierImpl tier = (TierImpl) annotation.getTier();
    	   
   		   String value = annotation.getValue();
   		   annotationRecord = new AnnotationDataRecord(annotation);        	
   		   if(tier.isTimeAlignable()){
   			   if(!tier.hasParentTier()){
   				   List<Annotation> childAnnotations = annotation.getParentListeners();
   				   annotation.updateTimeInterval(begin, splitTime);  	
   				   Annotation ann = tier.createAnnotation(splitTime, end);   				   
   				   annotationsSplit = true;
   				   
   				   if(ann != null){
   					   ann.setValue(value);
   				   }
   				   // HS added update of (split) text values
   				   if (splitText != null) {
   					   annotation.setValue(splitText[0]);
   					   ann.setValue(splitText[1]);
   				   }
   				   // end set values
   				   
   				   if(childAnnotations != null){
    				   for(int j=0; j< childAnnotations.size();j++){
    					   value = childAnnotations.get(j).getValue();    	
    					   long time = (splitTime+end)/2;
    					   ann = ((TierImpl)childAnnotations.get(j).getTier()).createAnnotation(time, time);
    					   if(ann != null){
    						   ann.setValue(value);
    					   	}
    				   }
   				   }
   			   }   			   
   		   }
    	}

		setWaitCursor(false);
        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
        if (annHasChildren) {
        	((TranscriptionImpl) transcription).setNotifying(true);
        }
    }
    
    /**
     * Splits a text into 2 parts at the given index.
     * 
     * @param inputText not null
     * @param splitIndex the position to split the text
     * @return a String array of size 2
     */
    String[] splitTextInTwo(String inputText, int splitIndex) {
    	String[] split = new String[2];
    	
    	if (splitIndex <= 0) {
    		split[0] = "";
    		split[1] = inputText;
    	} else if (splitIndex >= inputText.length()) {
    		split[0] = inputText;
    		split[1] = "";
    	} else {
    		split[0] = inputText.substring(0, splitIndex);
    		split[1] = inputText.substring(splitIndex);
    	}
    	
    	return split;
    }

	@Override
	public String getName() {
		return commandName;
	}
	
	@Override
	public void undo() {
		 int curPropMode = 0;
         curPropMode = transcription.getTimeChangePropagationMode();
         if (curPropMode != Transcription.NORMAL) {
             transcription.setTimeChangePropagationMode(Transcription.NORMAL);
         }
         setWaitCursor(true);
         
         AlignableAnnotation ann = null;
         if ((annotationRecord != null)) {
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());          
            if(tier != null){            
            	tier.removeAnnotation(tier.getAnnotationAtTime(splitTime));
            	ann = (AlignableAnnotation) tier.getAnnotationAtTime(annotationRecord.getBeginTime());
            	if(ann != null){
            		ann.updateTimeInterval(annotationRecord.getBeginTime(), annotationRecord.getEndTime());
            		if (splitText != null) {
            			ann.setValue(annotationRecord.getValue());
            		}
            	}
            } 
            
            annotationsSplit = false;
            if(MonitoringLogger.isInitiated()){
            	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.SPLIT_ANNOTATION);
            }
		}  
         
         setWaitCursor(false);

         // restore the time propagation mode
         transcription.setTimeChangePropagationMode(curPropMode);
	}
	
	@Override
	public void redo() {			
		if ((annotationRecord != null)) {
			TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
	        if(tier != null){
	        	AlignableAnnotation ann = (AlignableAnnotation) tier.getAnnotationAtTime(annotationRecord.getBeginTime());
	        	splitAnnotation(ann);
	        }
	        
	        if(annotationsSplit && MonitoringLogger.isInitiated()){
	        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.SPLIT_ANNOTATION);
	        }
		}		
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
