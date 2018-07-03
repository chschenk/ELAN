package mpi.eudico.client.annotator.commands;

import java.util.List;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * A command that creates one or two annotations in a gap on a particular (top level) tier.
 * If an attempt is made to create annotations where there is not a gap, nothing is actually changed
 * (an error message should be shown probably).  
 * This command is intended for use within a restricted, simplified turns-and-scene level 
 * transcription mode. It ignores, e.g., the time change propagation mode and several preference
 * settings.
 * 
 * @author Han Sloetjes
 * @version 1.0 August 2015
 */
public class NewAnnotationsInGap implements UndoableCommand {
	private String commandName;
	Transcription transcription;
	TierImpl tier;
	long beginTime;
	long endTime;
	long splitTime;
	String[] splitText;
	
	AnnotationDataRecord annRecord1;
	AnnotationDataRecord annRecord2;

	/**
	 * Constructor.
	 * @param name
	 */
	public NewAnnotationsInGap(String name) {
		commandName = name;
	}

	/**
	 * It is assumed the types and number of arguments are correct.
	 * 
	 * @param receiver the tier the annotations are to be created on 
	 * 			(assumed to be a top level tier for now) (TierImpl)
	 * @param arguments 
	 * <ul>
	 * <li>arg[0] = begin time of the total time interval (Long)</li>
	 * <li>arg[1] = end time of the total time interval (Long)</li>
	 * <li>arg[2] = the time at which to split the interval; if outside the range of the first 2 arguments this is ignored (Long)</li>
	 * <li>arg[3] = the text for new annotation(s) (String)</li>
	 * <li>arg[4] = the position at which to split the text in case 2 annotations are made (Integer)</li>
	 * </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		tier = (TierImpl) receiver;
		if (tier.hasParentTier()) {
			ClientLogger.LOG.warning("This operation is not (yet) supported on dependent tiers.");
			return;
		}
		transcription = tier.getTranscription();
		beginTime = (Long) arguments[0];
		endTime = (Long) arguments[1];
		
		List<Annotation> overlapping = tier.getOverlappingAnnotations(beginTime,  endTime);
		if (!overlapping.isEmpty()) {
			ClientLogger.LOG.warning("This operation is only allowed in sections where there is a gap (no annotations yet).");
			return;
		}
		
		splitTime = (Long) arguments[2];
		if (splitTime <= beginTime || splitTime >= endTime) {
			splitTime = -1;// only one annotation
		}
		String inputText = (String) arguments[3];
		if (inputText == null) {
			inputText = "";
		}
		
		int splitPos = (Integer) arguments[4];
		if (splitTime == -1) {
			splitPos = Integer.MAX_VALUE;
		}
		
		splitText = splitTextInTwo(inputText, splitPos);
		
		createAnnotations();
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
    
    /**
     * Creates one or two annotations in a gap, a time span not containing annotations. 
     * it is assumed that checks have been performed before this method is called.
     */
    protected void createAnnotations() {
    	int curPropMode = 0;
        curPropMode = transcription.getTimeChangePropagationMode();
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }
        //((TranscriptionImpl) transcription).setNotifying(false);// max 4 events
        
        long et = splitTime != -1 ? splitTime : endTime;
        Annotation nextAnn = tier.createAnnotation(beginTime, et);
        
        if (nextAnn != null) {
        	nextAnn.setValue(splitText[0]);
        	annRecord1 = new AnnotationDataRecord(nextAnn); 
        }
        
        // create a second annotation if there is a split time and if there is text for the second annotation
        if (splitTime > -1 && splitText[1].length() > 0) {
        	Annotation secAnn = tier.createAnnotation(splitTime, endTime);
        	
        	if (secAnn != null) {
        		secAnn.setValue(splitText[1]);
        		annRecord2 = new AnnotationDataRecord(secAnn);
        	}
        }
        // restore the time propagation mode, although it is not expected to be anything else than NORMAL
        transcription.setTimeChangePropagationMode(curPropMode);
        //((TranscriptionImpl) transcription).setNotifying(true);
    }

	@Override
	public String getName() {
		return commandName;
	}

	@Override
	public void undo() {
		if (annRecord1 != null) {
	    	int curPropMode = 0;
	        curPropMode = transcription.getTimeChangePropagationMode();
	        if (curPropMode != Transcription.NORMAL) {
	            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
	        }
	        
	        TierImpl topTier = (TierImpl) transcription.getTierWithId(annRecord1.getTierName());
	        
	        if (topTier != null) {
	        	long mid = (annRecord1.getBeginTime() + annRecord1.getEndTime()) / 2;
	        	Annotation remAnn = topTier.getAnnotationAtTime(mid);
	        	if (remAnn != null) {
	        		topTier.removeAnnotation(remAnn);
	        	}
	        	
		        if (annRecord2 != null) {
		        	long mid2 = (annRecord2.getBeginTime() + annRecord2.getEndTime()) / 2;
		        	Annotation remAnn2 = topTier.getAnnotationAtTime(mid2);
		        	if (remAnn2 != null) {
		        		topTier.removeAnnotation(remAnn2);
		        	}
		        }
	        }

	         // restore the time propagation mode
	         transcription.setTimeChangePropagationMode(curPropMode);
		}

	}

	@Override
	public void redo() {
		if (annRecord1 != null) {
	    	int curPropMode = 0;
	        curPropMode = transcription.getTimeChangePropagationMode();
	        if (curPropMode != Transcription.NORMAL) {
	            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
	        }
	        
	        TierImpl topTier = (TierImpl) transcription.getTierWithId(annRecord1.getTierName());
	        
	        if (topTier != null) {
	            Annotation nextAnn = tier.createAnnotation(annRecord1.getBeginTime(), annRecord1.getEndTime());
	            
	            if (nextAnn != null) {
	            	nextAnn.setValue(annRecord1.getValue());
	            }
	        	
	        	
		        if (annRecord2 != null) {
		        	Annotation secAnn = tier.createAnnotation(annRecord2.getBeginTime(), annRecord2.getEndTime());
		        	
		        	if (secAnn != null) {
		        		secAnn.setValue(annRecord2.getValue());
		        	}
		        }
	        }
	        // restore the time propagation mode
	        transcription.setTimeChangePropagationMode(curPropMode);
		}
	}

}
