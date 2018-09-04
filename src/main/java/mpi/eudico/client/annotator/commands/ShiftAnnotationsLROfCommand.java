package mpi.eudico.client.annotator.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.util.TimeInterval;
/**
 * Iterates over top level tiers and shifts annotations within specified interval
 * taking into account limits based on existing annotations.
 * 
 * @version Mar 2018 no longer uses one, overall available empty interval for
 * all tiers anymore, but instead an interval per root tier, to prevent errors in
 * the undo action
 *   
 * @see ShiftAnnotationsCommand
 */
public class ShiftAnnotationsLROfCommand extends ShiftAnnotationsCommand {
    // store empty space per root tier
	Map<String, TimeInterval> spacePerRootTier;
    
	public ShiftAnnotationsLROfCommand(String name) {
		super(name);
		availBT = 0;
		availET = Long.MAX_VALUE;
	}

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the begin of the time interval the
     *        annotations in which are  to be shifted (Long)</li> <li>arg[1] =
     *        the end time of that interval (Long)</li> <li>arg[2] = the shift
     *        value (Long)</li> </ul>
     */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		transcription = (TranscriptionImpl) receiver;
		
        if (arguments != null) {
        	spacePerRootTier = new HashMap<String, TimeInterval>();
            bt = (Long) arguments[0];
            et = (Long) arguments[1];
            shiftValue = (Long) arguments[2];

            transcription.setNotifying(false);
            int numShifted = 0;
            
            List<TierImpl> rootTiers = transcription.getTopTiers();
            // calculate overall available space first 
            for (int i = 0; i < rootTiers.size(); i++) {
            	TierImpl tier = rootTiers.get(i);
            	
//            	availBT = Math.max(availBT, getBoundaryBefore(tier, bt));
//            	availET = Math.min(availET, getBoundaryAfter(tier, et));
            	spacePerRootTier.put(tier.getName(), new TimeInterval(getBoundaryBefore(tier, bt),
            			getBoundaryAfter(tier, et)));
            }            
            
            for (int i = 0; i < rootTiers.size(); i++) {
            	TierImpl tier = rootTiers.get(i);
            	
            	if (tier != null && tier.isTimeAlignable()) {
            		numShifted += shift(tier, shiftValue, bt, et);
            	}
            }
            
            if (numShifted > 0) {
            	transcription.handleModification(transcription, ACMEditEvent.CHANGE_ANNOTATIONS, 
            			transcription);
            }
            
            transcription.setNotifying(true);
        }
	}

    /**
     * Shift the annotations again.
     */
	@Override
	public void redo() {
		if (transcription != null) {
			transcription.setNotifying(false);
			int numShifted = 0;
			
            List<TierImpl> rootTiers = transcription.getTopTiers();
            for (int i = 0; i < rootTiers.size(); i++) {
            	TierImpl tier = rootTiers.get(i);
            	
            	if (tier != null && tier.isTimeAlignable()) {
            		numShifted += shift(tier, shiftValue, bt, et);
            	}
            }
            
            if (numShifted > 0) {
            	transcription.handleModification(transcription, ACMEditEvent.CHANGE_ANNOTATIONS, 
            			transcription);
            }
            
            transcription.setNotifying(true);
		}
	}

    /**
     * Shift the annotations back.
     */
	@Override
	public void undo() {
		if (transcription != null) {
			transcription.setNotifying(false);
			int numShifted = 0;
			
            List<TierImpl> rootTiers = transcription.getTopTiers();
            for (int i = 0; i < rootTiers.size(); i++) {
            	TierImpl tier = rootTiers.get(i);
            	
            	if (tier != null && tier.isTimeAlignable()) {
            		TimeInterval ti = spacePerRootTier.get(tier.getName());
            		if (ti != null && ti.getBeginTime() != ti.getEndTime()) {
            			numShifted += shift(tier, -shiftValue, ti.getBeginTime(), ti.getEndTime());
            		}
            	}
            }
            
            if (numShifted > 0) {
            	transcription.handleModification(transcription, ACMEditEvent.CHANGE_ANNOTATIONS, 
            			transcription);
            }
            
            transcription.setNotifying(true);
		}
	}

	/**
	 * @return if there is an overlapping annotation at the specified time
	 * return the end time of the annotation before the overlapping
	 * annotation, or 0 if there is none 
	 */
	@Override
	long getBoundaryBefore(TierImpl tier, long bt) {
		if (bt == 0) {
			return bt;
		}
		// take overlapping annotations into account
    	Annotation a = tier.getAnnotationAtTime(bt);
    	if (a != null && a.getBeginTimeBoundary() != bt) {
    		return a.getEndTimeBoundary();
    	} else {
    		a = tier.getAnnotationBefore(bt);
    		if (a != null) {
        		return a.getEndTimeBoundary();
        	}
    	}
    	
		return 0;
	}

	/**
	 * @return if there is an overlapping annotation at the specified time
	 * return the begin time of the annotation after the overlapping
	 * annotation, or {@code Long.MAX_VALUE} if there is none 
	 */
	@Override
	long getBoundaryAfter(TierImpl tier, long et) {
    	if (et == Long.MAX_VALUE) {
    		return et;
    	}
    	// take overlapping annotations into account
    	Annotation a = tier.getAnnotationAtTime(et);
    	if (a != null) {
    		return a.getBeginTimeBoundary();
    	} else {
	    	a = tier.getAnnotationAfter(et);
	    	if (a != null) {
	    		return a.getBeginTimeBoundary();
	    	}
    	}
    	
    	return Long.MAX_VALUE;
	}
    
    
}
