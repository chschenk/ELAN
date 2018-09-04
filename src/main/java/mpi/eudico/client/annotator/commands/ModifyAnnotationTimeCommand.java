package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.client.annotator.util.TimeShiftRecord;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;


/**
 * A command for modifying an annotation's begin and/or end time.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationTimeCommand implements UndoableCommand {
    private String commandName;
    private AlignableAnnotation annotation;
    private DefaultMutableTreeNode rootNode;
    private TranscriptionImpl transcription;

    /**
     * note: this presumes that tier object references stay the same even if
     * they have been removed from and re-added to the transcription
     */
    private TierImpl tier;
    private TierImpl rootTier;
    private int timePropMode;
    private int leftOffset;
    private int rightOffset;
    private long oldBeginTime;
    private long oldEndTime;
    private long newBeginTime;
    private long newEndTime;
    private ArrayList<DefaultMutableTreeNode> removedAnnotations;
    /**
     * TODO: WARNING: changedAnnotations is used to store TimeShiftRecords and MutableTreeNodes.
     */
    private ArrayList<Object> changedAnnotations;

    /**
     * Creates a new ModifyAnnotationTimeCommand instance.
     *
     * @param name the name of the command
     */
    public ModifyAnnotationTimeCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Annotation
     * @param arguments the arguments:  <ul><li>arg[0] = the new begin time of
     *        the annotation (Long)</li> <li>arg[1] = the new end time of the
     *        annotation (Long)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof AlignableAnnotation) {
            annotation = (AlignableAnnotation) receiver;
        } else {
            return;
        }

        oldBeginTime = annotation.getBeginTimeBoundary();
        oldEndTime = annotation.getEndTimeBoundary();
        newBeginTime = ((Long) arguments[0]).longValue();
        if (newBeginTime < 0) {
        	if (LOG.isLoggable(Level.FINE)) {
        		LOG.fine("New begin time < 0, changing to 0");
        	}
        	newBeginTime = 0;
        }
        newEndTime = ((Long) arguments[1]).longValue();
        if (newEndTime <= newBeginTime) {
        	if (LOG.isLoggable(Level.FINE)) {
        		LOG.fine("Cannot modify annotation times: end time <= begin time");
        	}
        	return;
        }
        
        leftOffset = (int) (newBeginTime - oldBeginTime);
        rightOffset = (int) (newEndTime - oldEndTime);

        // only do something if begin and/or end time has changed
        if ((oldBeginTime == newBeginTime) && (oldEndTime == newEndTime)) {
        	if (LOG.isLoggable(Level.FINE)) {
        		LOG.fine("Cannot modify annotation times: no changes in begin or end time");
        	}
            return;
        }

        tier = (TierImpl) annotation.getTier();

        if (tier.hasParentTier()) {
            rootTier = tier.getRootTier();
        }

        transcription = tier.getTranscription();

        setWaitCursor(true);
        // first check and adjust the new begin and end times
        boolean snapAnnotations = false;
        boolean stickToVideoFrames = false;
    	
    	Boolean boolPref = Preferences.getBool("SnapAnnotations", null);
    	if (boolPref != null) {
    		snapAnnotations = boolPref.booleanValue();
    	} 
    	
    	boolPref = Preferences.getBool("StickAnnotationsWithVideoFrames", null);
    	if (boolPref != null) {
    		stickToVideoFrames = boolPref.booleanValue();
    	}  
    	
    	//videoframes
    	if(stickToVideoFrames  && tier.isTimeAlignable()){
    		if(!tier.hasParentTier() || tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN){
    			newBeginTime = getStartPointOfFrame(newBeginTime);
    			newEndTime = getEndPointOfFrame(newEndTime);
    		}    		
    	}
    	
    	 //snap annotations
    	if(snapAnnotations ){
        	long snapValue = 0L;
            
        	Long longPref = Preferences.getLong("SnapAnnotationsValue", null);
            if (longPref != null) {
            	snapValue = longPref.longValue();
            }
             
        	// HS if snapValue > 0
            if (snapValue > 0) {
            	long firstDiff = Long.MAX_VALUE;
                long secDiff = firstDiff;
                boolean durationChanged = ((oldEndTime - oldBeginTime) != (newEndTime - newBeginTime));
                boolean changeLeft = false;
                boolean changeRight = false;
                
//	            AbstractAnnotation annBefore = (AbstractAnnotation) tier.getAnnotationBefore(annotation) ;
//	            AbstractAnnotation annAfter = (AbstractAnnotation) tier.getAnnotationAfter(annotation);
	            AbstractAnnotation annBefore = (AbstractAnnotation) tier.getAnnotationAtTime(newBeginTime);
	            if (annBefore == null || annBefore == annotation) {
	            	annBefore = (AbstractAnnotation) tier.getAnnotationBefore(newBeginTime);
	            }
	            AbstractAnnotation annAfter = (AbstractAnnotation) tier.getAnnotationAtTime(newEndTime);
	            if (annAfter == null || annAfter == annotation) {
	            	annAfter = (AbstractAnnotation) tier.getAnnotationAfter(newEndTime);
	            }
	            
	            if(annBefore != null){
	            	firstDiff = newBeginTime - annBefore.getEndTimeBoundary();
	            	if (firstDiff != 0 && firstDiff >= -snapValue && firstDiff <= snapValue) {
	            		changeLeft = true;
	            	}
	            }
	            
	            if(annAfter != null) {
	            	secDiff = annAfter.getBeginTimeBoundary() - newEndTime;
	            	if (secDiff != 0 && secDiff >= -snapValue && secDiff <= snapValue) {
	            		changeRight = true;
	            	}
	            } 
	                	
	            if (changeLeft && changeRight) {
	            	// both left and right there is the possibility of snapping, choose one if
	            	// the duration should not change
	            	if (!durationChanged) {
	            		if (Math.abs(firstDiff) <= Math.abs(secDiff)) {
	            			changeRight = false;
	            		} else {
	            			changeLeft = false;
	            		}
	            	}
	            }
	            
	            if (changeLeft) {
	            	newBeginTime = newBeginTime - firstDiff;
	            	if (!durationChanged) {
	            		newEndTime = newEndTime - firstDiff;
	            	}
	            }
	            if (changeRight) {
	            	newEndTime = newEndTime + secDiff;
	            	if (!durationChanged) {
	            		newBeginTime = newBeginTime + secDiff;
	            	}
	            }
	            /*
	            if(firstDiff < secDiff && firstDiff < snapValue ){
	            	newBeginTime = newBeginTime-firstDiff;
	            	newEndTime = newEndTime-firstDiff;    
	            	//annotation.updateTimeInterval(newBeginTime, newEndTime);
	            } else if(secDiff < snapValue){
	            	newBeginTime = newBeginTime+secDiff;
	            	newEndTime = newEndTime+secDiff; 
	            	//annotation.updateTimeInterval(newBeginTime, newEndTime);
	            }
	            */
            }
        } 
    	// then store information for undo / redo
        changedAnnotations = new ArrayList<Object>();
        removedAnnotations = new ArrayList<DefaultMutableTreeNode>();

        // store information on all annotations that will be effected
        timePropMode = transcription.getTimeChangePropagationMode();

        switch (timePropMode) {
        case Transcription.NORMAL:
            storeNormal();

            break;

        case Transcription.BULLDOZER:
            storeBulldozer();

            break;

        case Transcription.SHIFT:
            storeShift();
        }  
        
        if(MonitoringLogger.isInitiated()){        	
        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.CHANGE_ANNOTATION_TIME);        				
        }
    	
    	// finally make the change
        annotation.updateTimeInterval(newBeginTime, newEndTime);
        
        // clear the selection after the update, if a pref is set
        boolPref = Preferences.getBool("ClearSelectionAfterCreation", null);
        
        ViewerManager2 vm = ELANCommandFactory.getViewerManager(transcription);
        if (boolPref != null) {
        	if (boolPref.booleanValue()) {        		
        		if (vm.getMediaPlayerController().getSelectionMode() == true) {
        			vm.getSelection().setSelection(
        					vm.getMediaPlayerController().getMediaTime(), 
        					vm.getMediaPlayerController().getMediaTime());
        		} else {
        			vm.getSelection().setSelection(0, 0);
        		}
        	}
        } else {
        	vm.getSelection().setSelection(newBeginTime, newEndTime);
        }
        
        setWaitCursor(false);
    }
    
    /**
     * 
     * @param time, the time for which the start of that video frame is returned
     * @return the start of that video frame 
     */
    public long getStartPointOfFrame(long time) {
    	ViewerManager2 vm = ELANCommandFactory.getViewerManager(transcription);
    	double milliSecondsPerSample = vm.getMasterMediaPlayer().getMilliSecondsPerSample();
    	long curFrame = (long) (time / milliSecondsPerSample);
    	if (curFrame > 0) {
    		curFrame = (long) (curFrame * milliSecondsPerSample);
    	} else {
    		curFrame = 0;
    	}
    	return curFrame;
	}
    
    /**
     * 
     * 
     * @param time, the time for which the end of that video frame is returned
     * @return the end of that video frame 
     */
    public long getEndPointOfFrame(long time) {
    	ViewerManager2 vm = ELANCommandFactory.getViewerManager(transcription);
    	double milliSecondsPerSample = vm.getMasterMediaPlayer().getMilliSecondsPerSample();
    	long curFrame = (long) (time / milliSecondsPerSample);
    	long newEnd = (long) (curFrame * milliSecondsPerSample);
    	if(newEnd != time){
    		curFrame =(long) ((curFrame + 1) * milliSecondsPerSample);  
    	} else {
    		curFrame = newEnd;
    	}
    	return curFrame;
	}

    /**
     * Undo the changes made by this command.
     */
    @Override
	public void undo() {
        // only do something if begin and/or end time has changed
        if ((oldBeginTime == newBeginTime) && (oldEndTime == newEndTime)) {
            return;
        }
        if (newEndTime <= newBeginTime) {
        	return;
        }

        transcription.setNotifying(false);

        setWaitCursor(true);

        switch (timePropMode) {
        case Transcription.NORMAL:
            restoreNormal();

            break;

        case Transcription.BULLDOZER:
            restoreBulldozer();

            break;

        case Transcription.SHIFT:
            restoreShift();
        }
        
        if(MonitoringLogger.isInitiated()){        	
        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.CHANGE_ANNOTATION_TIME);        				
        }


        transcription.setNotifying(true);

        setWaitCursor(false);
    }

    /**
     * Redo the changes made by this command.
     */
    @Override
	public void redo() {
        // only do something if begin and/or end time has changed
        if ((oldBeginTime == newBeginTime) && (oldEndTime == newEndTime)) {
            return;
        }
        if (newEndTime <= newBeginTime) {
        	return;
        }
        
        setWaitCursor(true);

        AlignableAnnotation annotation = (AlignableAnnotation) tier.getAnnotationAtTime(oldBeginTime);

        if (annotation != null) {
            annotation.updateTimeInterval(newBeginTime, newEndTime);
            if(MonitoringLogger.isInitiated()){        	
            	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.CHANGE_ANNOTATION_TIME);        				
            }

        }

        setWaitCursor(false);
    }

    /**
     * Stores information of all effected annotations in normal time
     * propagation mode. Assumption: no annotations on parenttiers will be
     * effected.
     */
    private void storeNormal() {
        // in case the annotation is on a time-subdivision tier
        // store the whole tree with the parent annotation as root
        if (rootTier != null) {
            AbstractAnnotation aa = (AbstractAnnotation) rootTier.getAnnotationAtTime(oldBeginTime);

            if (aa != null) {
                rootNode = AnnotationRecreator.createTreeForAnnotation(aa);
            }

            return;
        }

        if ((leftOffset > 0) || (rightOffset < 0)) {
            // children could have been destroyed
            rootNode = AnnotationRecreator.createTreeForAnnotation(annotation);
        }

        if (leftOffset < 0) {
            List<Annotation> v = tier.getOverlappingAnnotations(newBeginTime, oldBeginTime);

            if (v.size() > 0) {
                AbstractAnnotation ann = (AbstractAnnotation) v.get(0);

                if (ann.getBeginTimeBoundary() < newBeginTime) {
                    // this one will be changed, children might be deleted
                    // so store the whole tree with this annotation as root						
                    changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            ann));
                } else {
                    removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            ann));
                }

                for (int i = 1; i < v.size(); i++) {
                    removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            (AbstractAnnotation) v.get(i)));
                }
            }
        }

        if (rightOffset > 0) {
            List<Annotation> v = tier.getOverlappingAnnotations(oldEndTime, newEndTime);

            if (v.size() > 0) {
                for (int i = 0; i < v.size(); i++) {
                    if (i != (v.size() - 1)) {
                        removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                (AbstractAnnotation) v.get(i)));
                    } else {
                        //last ann
                        AbstractAnnotation ann = (AbstractAnnotation) v.get(i);

                        if (ann.getEndTimeBoundary() > newEndTime) {
                            changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                    ann));
                        } else {
                            removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                    ann));
                        }
                    }
                }
            }
        }
    }

    /**
     * Restore the situation before the edit action; normal mode.
     */
    private void restoreNormal() {
        if (tier == null) {
            return; //warn??
        }

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        // in case the annotation is on a time-subdivision tier, first remove the root annotation
        // and then restore it.        
        if (rootTier != null) {
            AbstractAnnotation aa = (AbstractAnnotation) rootTier.getAnnotationAtTime(newBeginTime);

            if (aa != null) {
                rootTier.removeAnnotation(aa);

                AnnotationRecreator.createAnnotationFromTree(transcription,
                    rootNode, true);
            }
        } else {
            AlignableAnnotation actAnnotation = (AlignableAnnotation) tier.getAnnotationAtTime(newBeginTime);

            // first delete changed annotations
            DefaultMutableTreeNode node;
            AnnotationDataRecord dataRecord;
            AbstractAnnotation aa;

            if (changedAnnotations.size() > 0) {
                for (int i = 0; i < changedAnnotations.size(); i++) {
                    node = (DefaultMutableTreeNode) changedAnnotations.get(i);
                    dataRecord = (AnnotationDataRecord) node.getUserObject();

                    if (dataRecord.getBeginTime() < oldBeginTime) {
                        aa = (AbstractAnnotation) tier.getAnnotationAtTime(dataRecord.getBeginTime());
                    } else {
                        aa = (AbstractAnnotation) tier.getAnnotationAtTime(dataRecord.getEndTime() -
                                1);
                    }

                    if (aa != null) {
                        tier.removeAnnotation(aa);
                    }
                }
            }

            if ((leftOffset > 0) || (rightOffset < 0)) {
                // children could have been destroyed
                if (actAnnotation != null) {
                    tier.removeAnnotation(actAnnotation);
                }

                AnnotationRecreator.createAnnotationFromTree(transcription,
                    rootNode, true);
            } else {
                if (actAnnotation != null) {
                    actAnnotation.updateTimeInterval(oldBeginTime, oldEndTime);
                }
            }

            // now recreate all annotations that have been deleted
            if (changedAnnotations.size() > 0) {
                for (int i = 0; i < changedAnnotations.size(); i++) {
                    node = (DefaultMutableTreeNode) changedAnnotations.get(i);
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                        node, true);
                }
            }

            if (removedAnnotations.size() > 0) {
                for (int i = 0; i < removedAnnotations.size(); i++) {
                    node = removedAnnotations.get(i);
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                        node, true);
                }
            }
        }

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
    }

    /**
     * Stores information of all effected annotations in bulldozer time
     * propagation mode. Assumption: no annotations on parenttiers will be
     * effected.
     */
    private void storeBulldozer() {
        // in case the annotation is on a time-subdivision tier
        // store the whole tree with the parent annotation as root
        if (rootTier != null) {
            storeNormal();

            return;
        }

        if ((leftOffset > 0) || (rightOffset < 0)) {
            // children could have been destroyed
            rootNode = AnnotationRecreator.createTreeForAnnotation(annotation);
        }

        // annotations will only be shifted; store the times...
        // store annotations in the order they need to be restored
        // check left side....
        List<AbstractAnnotation> annos = tier.getAnnotations();

        if (annos == null) {
            return;
        }

        if (leftOffset < 0) {
            AbstractAnnotation cur = annotation;
            AbstractAnnotation prev;
            int index = annos.indexOf(annotation);
            int gapToBridge = -leftOffset;
            int gapBridged = 0;

            for (int i = index - 1; i >= 0; i--) {
                prev = annos.get(i);

                int dist = (int) (cur.getBeginTimeBoundary() -
                    prev.getEndTimeBoundary());
                gapBridged += dist;

                if (gapBridged < gapToBridge) {
                    // check if the new begintime will be less than zero: 
                	// annotations could be lost ?
                	// store all changed annotations as removed annotations, because they 
                	// will be completely recreated anyway on undo
                    removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            prev));

                    changedAnnotations.add(new TimeShiftRecord(
                            prev.getBeginTimeBoundary(),
                            prev.getEndTimeBoundary(),
                            -(gapToBridge - gapBridged)));

                    cur = prev;
                } else {
                    break;
                }
            }
        }

        // check right side
        if (rightOffset > 0) {
            AbstractAnnotation cur = annotation;
            AbstractAnnotation next;
            int index = annos.indexOf(annotation);
            int gapToBridge = rightOffset;
            int gapBridged = 0;

            for (int i = index + 1; i < annos.size(); i++) {
                next = annos.get(i);

                int dist = (int) (next.getBeginTimeBoundary() -
                    cur.getEndTimeBoundary());
                gapBridged += dist;

                if (gapBridged < gapToBridge) {
                    changedAnnotations.add(new TimeShiftRecord(
                            next.getBeginTimeBoundary(),
                            next.getEndTimeBoundary(), gapToBridge -
                            gapBridged));
                    removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            next));
                    cur = next;
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Restores information of all effected annotations in bulldozer time
     * propagation mode. First restore the annotation that was originally
     * changed by the user,  next shift the annotations to the left and to the
     * right back to their original locations.
     */
    private void restoreBulldozer() {
        if (tier == null) {
            return; //warn??
        }

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        // in case the annotation is on a time-subdivision tier, first remove the root annotation
        // and then restore it.
        if (rootTier != null) {
            restoreNormal();
        } else {
            AlignableAnnotation actAnnotation = (AlignableAnnotation) tier.getAnnotationAtTime(newBeginTime);

            if ((leftOffset > 0) || (rightOffset < 0)) {
                // children could have been destroyed
                if (actAnnotation != null) {
                    tier.removeAnnotation(actAnnotation);
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                        rootNode, true);
                }
            } else {
                // restore the original, edited annotation
                if (actAnnotation != null) {
                    actAnnotation.updateTimeInterval(oldBeginTime, oldEndTime);
                }
            }

            if (changedAnnotations.size() > 0) {
                TimeShiftRecord tsRecord;
                AlignableAnnotation aa;
                DefaultMutableTreeNode node;

                for (int i = 0; i < changedAnnotations.size(); i++) {
                    tsRecord = (TimeShiftRecord) changedAnnotations.get(i);
                    aa = (AlignableAnnotation) tier.getAnnotationAtTime(tsRecord.newBegin);
                    node = removedAnnotations.get(i);

                    if (aa != null) {
                        tier.removeAnnotation(aa);
                        //AnnotationRecreator.createAnnotationFromTree(transcription,
                           // node, true);
                    }
                    // re-create here otherwise annotations that are completely shifted
                    // beyond point 0, are not restored
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                            node, true);
                }
            }
        }

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
    }

    /**
     * Stores information on all effected annotations in shift propagation
     * mode. On a time subdivision tier shift mode is identical to normal
     * mode. On root tiers the changes on the left side are treated the same
     * way as in normal mode, changes on the right side shift all time slots
     * on  the right side of the selection to the right.
     */
    private void storeShift() {
        // in case the annotation is on a time-subdivision tier
        // store the whole tree with the parent annotation as root
        if (rootTier != null) {
            storeNormal();

            return;
        }

        // we are on a root tier
        if ((leftOffset > 0) || (rightOffset < 0)) {
            // children could have been destroyed
            rootNode = AnnotationRecreator.createTreeForAnnotation(annotation);
        }

        // copy from storeNormal for the left side
        if (leftOffset < 0) {
            List<Annotation> v = tier.getOverlappingAnnotations(newBeginTime, oldBeginTime);

            if (v.size() > 0) {
                AbstractAnnotation ann = (AbstractAnnotation) v.get(0);

                if (ann.getBeginTimeBoundary() < newBeginTime) {
                    // this one will be changed, children might be deleted
                    // so store the whole tree with this annotation as root						
                    changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            ann));
                } else {
                    removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            ann));
                }

                for (int i = 1; i < v.size(); i++) {
                    removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            (AbstractAnnotation) v.get(i)));
                }
            }
        }
    }

    /**
     * Restores information of all effected annotations in shift time
     * propagation mode. On root tiers first delete all changed annotations,
     * then  shift the time slots back and finally recreate/restore any
     * removed annotations.
     */
    private void restoreShift() {
        if (tier == null) {
            return; //warn??
        }

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        // in case the annotation is on a time-subdivision tier, first remove the root annotation
        // and then restore it.
        if (rootTier != null) {
            restoreNormal();
        } else {
            // we are on a root tier
            AlignableAnnotation actAnnotation = (AlignableAnnotation) tier.getAnnotationAtTime(newBeginTime);

            // first delete changed annotations on the left side of the current annotation
            DefaultMutableTreeNode node;
            AnnotationDataRecord dataRecord;
            AbstractAnnotation aa;

            if (changedAnnotations.size() > 0) {
                for (int i = 0; i < changedAnnotations.size(); i++) {
                    node = (DefaultMutableTreeNode) changedAnnotations.get(i);
                    dataRecord = (AnnotationDataRecord) node.getUserObject();
                    aa = (AbstractAnnotation) tier.getAnnotationAtTime(dataRecord.getBeginTime());

                    if (aa != null) {
                        tier.removeAnnotation(aa);
                    }
                }
            }

            if ((leftOffset > 0) || (rightOffset < 0)) {
                // children could have been destroyed
                if (actAnnotation != null) {
                    tier.removeAnnotation(actAnnotation);
                }
            }

            if (rightOffset > 0) {
                // shift the slots backward	
                transcription.shiftBackward(oldBeginTime, -rightOffset);
            }

            if ((leftOffset > 0) || (rightOffset < 0)) {
                AnnotationRecreator.createAnnotationFromTree(transcription,
                    rootNode, true);
            } else {
                // restore/update the original, edited annotation
                if (actAnnotation != null) {
                    actAnnotation.updateTimeInterval(oldBeginTime, oldEndTime);
                }
            }

            // now recreate removed and shifted annotations
            if (changedAnnotations.size() > 0) {
                for (int i = 0; i < changedAnnotations.size(); i++) {
                    node = (DefaultMutableTreeNode) changedAnnotations.get(i);
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                        node, true);
                }
            }

            if (removedAnnotations.size() > 0) {
                for (int i = 0; i < removedAnnotations.size(); i++) {
                    node = removedAnnotations.get(i);
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                        node, true);
                }
            }
        }

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
