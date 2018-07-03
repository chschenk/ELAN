package mpi.eudico.client.annotator.commands;

import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * Action that finds the annotation to activate on the tier visually below the tier 
 * the current active annotation is on (if there is an active annotation). Searches recursively.
 */
@SuppressWarnings("serial")
public class AnnotationDownCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new AnnotationDownCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public AnnotationDownCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.ANNOTATION_DOWN);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/GoToLowerAnnotation.gif"));
        putValue(SMALL_ICON, icon);

        putValue(Action.NAME, "");

        //setEnabled(true);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.ACTIVE_ANNOTATION);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Annotation currentActiveAnnot = vm.getActiveAnnotation().getAnnotation();
        Annotation newActiveAnnot = null;
        List<TierImpl> visibleTiers = null;
        if (vm.getMultiTierControlPanel() != null) {
        	visibleTiers = vm.getMultiTierControlPanel().getVisibleTiers();
        }

        if (visibleTiers == null || visibleTiers.size() == 0) {
            return new Object[] { currentActiveAnnot }; // do nothing
        }

        if (currentActiveAnnot != null) {
            long refTime;
            long mediaTime = vm.getMasterMediaPlayer().getMediaTime();

            if ((mediaTime >= currentActiveAnnot.getBeginTimeBoundary()) &&
                    (mediaTime <= currentActiveAnnot.getEndTimeBoundary())) {
                if (mediaTime == currentActiveAnnot.getBeginTimeBoundary()) {
                    // +1 because TierImpl.getAnnotationAtTime compares inclusive on both sides
                    refTime = mediaTime + 1;
                } else {
                    refTime = mediaTime;
                }
            } else {
                refTime = currentActiveAnnot.getBeginTimeBoundary() + 1;
            }

            TierImpl currentTier = (TierImpl) currentActiveAnnot.getTier();

            int index = visibleTiers.indexOf(currentTier);

            if ((index > -1) && (index < (visibleTiers.size() - 1))) {
                // current tier is visible                
                for(int i = index; i < (visibleTiers.size()-1); i++){
            		TierImpl downTier = getNextNonEmptyTier(i + 1);

            		if (downTier == null) {
            			newActiveAnnot = currentActiveAnnot; // do nothing
            		} else {
            			// get annotation before this time
            			newActiveAnnot = getClosestAnnotation(downTier, currentActiveAnnot);
            		}
            		if(newActiveAnnot !=null){
            			long newBeginTime = newActiveAnnot.getBeginTimeBoundary();
            			long newEndTime = newActiveAnnot.getEndTimeBoundary();
            			long beginTime  = currentActiveAnnot.getBeginTimeBoundary();
            			long endTime = currentActiveAnnot.getEndTimeBoundary();
            			// checks if the annotation is within the active annotation interval or
            			// within a time interval of +/- 1 sec from  the active annotation, 
            			// then alt+down moves to this new annotation
            			if((newBeginTime >= beginTime && newBeginTime <= endTime) || 
            					(newBeginTime <= beginTime && newBeginTime >= (beginTime-1000L)) ||
            					(newBeginTime >= endTime && newBeginTime <= (endTime+1000L))) {
            				break;	
            			} // if the annotation interval is larger than the active annotation but if it overlaps the active annotation, 
            			// and if it is within the time interval of +/- 5 sec and the duration of the annotation should be less than 10 sec,
            			//then jumps to that annotation
            			else if(newBeginTime <= beginTime && newEndTime >= endTime){
            				if((beginTime-5000L) > newBeginTime || (endTime+5000L) > newEndTime){
            					if((newEndTime-newBeginTime) < 10000L){
            						break;
            					}
            				}
            			}
            		}
            	}
            } else if (index < 0) {
                // start with the top tier or the active tier, or do nothing??
                TierImpl firstTier = getNextNonEmptyTier(0);

                if (firstTier == null) {
                    newActiveAnnot = currentActiveAnnot;
                } else {
                    newActiveAnnot = getAnnotationAtOrBefore(firstTier, refTime);
                }
            } else if (index == (visibleTiers.size() - 1)) {
                // the active annotation is on the last visible tier, do nothing
                newActiveAnnot = currentActiveAnnot;
            }
        } else { // try on basis of current time and active tier

            Tier activeTier = vm.getMultiTierControlPanel().getActiveTier();

            if (activeTier != null) {
                newActiveAnnot = getAnnotationAtOrBefore((TierImpl) activeTier,
                        vm.getMasterMediaPlayer().getMediaTime());
            } else {
                // use first visible tier
                TierImpl firstTier = getNextNonEmptyTier(0);

                if (firstTier == null) {
                    newActiveAnnot = currentActiveAnnot;
                } else {
                    newActiveAnnot = getAnnotationAtOrBefore(firstTier,
                            vm.getMasterMediaPlayer().getMediaTime());
                }
            }
        }

        Object[] args = new Object[1];
        args[0] = newActiveAnnot;

        return args;
    }

    /**
     * Finds the next tier (descending) containing annotations.
     *
     * @param fromIndex the index to start searching from (inclusive)
     *
     * @return the next tier or null
     */
    private TierImpl getNextNonEmptyTier(int fromIndex) {
        List<TierImpl> vis = null;
        if (vm.getMultiTierControlPanel() != null) {
        	vis = vm.getMultiTierControlPanel().getVisibleTiers();
        }

        if (vis == null || vis.size() == 0) {
            return null;
        }

        if ((fromIndex < 0) || (fromIndex >= vis.size())) {
            return null;
        }

        for (int i = fromIndex; i < vis.size(); i++) {
            if (vis.get(i).getNumberOfAnnotations() > 0) {
                return vis.get(i);
            }
        }

        return null;
    }

    /**
     * If there is an annotation at the specified time return it, otherwise
     * return the closest annotation before this time
     *
     * @param tier the tier to search
     * @param time the reference time
     *
     * @return the annotation at or before the specified time
     */
    private Annotation getAnnotationAtOrBefore(TierImpl tier, long time) {
        if (tier.getAnnotationAtTime(time) != null) {
            return tier.getAnnotationAtTime(time);
        } else {
            return tier.getAnnotationBefore(time);
        }
    }
    
    /**
     * Returns the annotation closest to the reference annotation
     *
     * @param tier the tier to search
     * @param ann the reference annotation
     *
     * @return the annotation closest to the ref ann
     */
    private Annotation getClosestAnnotation(final TierImpl tier,
       Annotation ann) {
    	long beginTime = ann.getBeginTimeBoundary();
    	long endTime = ann.getEndTimeBoundary();
        
    	Annotation annBefore = tier.getAnnotationBefore(beginTime);
        Annotation annAfter = tier.getAnnotationAfter(beginTime);
        if(annAfter!= null && annBefore !=null){
        	// returns the first annotation before, if there is an overlap with the current annotation
        	if(annBefore.getEndTimeBoundary() > beginTime && annBefore.getEndTimeBoundary() < endTime){
        		return annBefore;
        	}
        	// returns the first annotation after, if there is an overlap with the current annotation
        	if(annAfter.getBeginTimeBoundary() > beginTime && annAfter.getBeginTimeBoundary() < endTime){
        		return annAfter;
        	} 
        	// if no overlap, returns the closest annotation
        	if((beginTime - annBefore.getEndTimeBoundary()) > (annAfter.getBeginTimeBoundary() - endTime)){
        		return annAfter;
        	} else {       		
        		return annBefore;
        	}
        }
        
        if(annAfter != null){
        	return annAfter;
        }
        
        if(annBefore !=null){
        	return annBefore;
        }
        return null;
    }
}
