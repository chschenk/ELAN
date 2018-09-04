package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
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


/**
 * A Command for the creation of new annotations on a tier.<br>
 * Should become undoable eventually. Because existing annotations  can be
 * destroyed when creating a new annotation undo /redo is not  yet
 * implemented.
 *
 * @author Han Sloetjes
 */
public class NewAnnotationCommand implements UndoableCommand {
    private String commandName;
    TierImpl tier;
    TierImpl rootTier;
    int timePropMode;
    TranscriptionImpl transcription;
    Annotation newAnnotation;
    String annotationValue;
    long begin;
    long end;
    protected long newAnnBegin;
    protected long newAnnEnd;
    ArrayList<DefaultMutableTreeNode> removedAnnotations;
    /**
     * TODO: WARNING: changedAnnotations is used to store TimeShiftRecords and MutableTreeNodes.
     */
    List<Object> changedAnnotations;
    private int leftOffset;
    private int rightOffset;

    /**
     * Creates a new NewAnnotationCommand instance
     *
     * @param name the name of the command
     */
    public NewAnnotationCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action.
     */
    @Override
	public void undo() {
        if ((tier != null) && (newAnnotation != null)) {
            setWaitCursor(true);

            Annotation aa = tier.getAnnotationAtTime((newAnnBegin + newAnnEnd) / 2);

            if (aa != null) {
                tier.removeAnnotation(aa);
                if(MonitoringLogger.isInitiated()){
                	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.NEW_ANNOTATION);
                }
                ViewerManager2 vm = ELANCommandFactory.getViewerManager(transcription);
                if (vm.getActiveAnnotation().getAnnotation() == aa) {
                	// deactivate annotation
                    Command c = ELANCommandFactory.createCommand(transcription,
                            ELANCommandFactory.ACTIVE_ANNOTATION);
                    c.execute(vm, new Object[] { null });
                }
            }

            if (tier.isTimeAlignable()) {
                transcription.setNotifying(false);

                restoreInUndo();
                /*
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
				*/
                transcription.setNotifying(true);
            }
            setWaitCursor(false);
        }
    }

    /**
     * The redo action.
     */
    @Override
	public void redo() {
        if (tier != null) {
            setWaitCursor(true);
            
            transcription.setNotifying(false);
            
            newAnnotation = tier.createAnnotation(begin, end);
            
            if (annotationValue != null) {
            	newAnnotation.setValue(annotationValue);
            }
            
            if(MonitoringLogger.isInitiated()){
            	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.NEW_ANNOTATION);
            }
            
            transcription.setNotifying(true);

            setWaitCursor(false);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.<br>
     * July 2006: removed the ViewerManager as one of the objects in the arguments array
     *
     * @param receiver the TierImpl
     * @param arguments the arguments:  <ul> <li>arg[0] = the begin time of the
     *        annotation (Long)</li> <li>arg[1] = the end time of the
     *        annotation (Long)</li> 
     *        <li>arg[2] = the textual value of the annotation (String, optional)</li></ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        tier = (TierImpl) receiver;
        begin = ((Long) arguments[0]).longValue();
        end = ((Long) arguments[1]).longValue();
        if (arguments.length >= 3) {
        	annotationValue = (String) arguments[2];
        }

        transcription = tier.getTranscription();
        ViewerManager2 vm = ELANCommandFactory.getViewerManager(transcription);
        
        // stop the player if playing
        if(vm.getMasterMediaPlayer().isPlaying() && 
        		ELANCommandFactory.getLayoutManager(transcription).getMode() == ElanLayoutManager.NORMAL_MODE){
        	vm.getMasterMediaPlayer().stop();
        }

        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.ACTIVE_ANNOTATION);
        c.execute(vm, new Object[] { null });
        setWaitCursor(true);

        if (!tier.isTimeAlignable()) {
            // symbolic subdivision or symbolic association
            // nothing gets lost
            //newAnnotation = tier.createAnnotation(begin, end);
            newAnnotation();
        } else {
        	adjustTimes();
            changedAnnotations = new ArrayList<Object>();
            removedAnnotations = new ArrayList<DefaultMutableTreeNode>();

            if (tier.hasParentTier()) {
                rootTier = tier.getRootTier();
            }

            timePropMode = transcription.getTimeChangePropagationMode();

            storeForUndo();
//            switch (timePropMode) {
//            case Transcription.NORMAL:
//                storeNormal();
//
//                break;
//
//            case Transcription.BULLDOZER:
//                storeBulldozer();
//
//                break;
//
//            case Transcription.SHIFT:
//                storeShift();
//            }

            // finally create the annotation 
            //newAnnotation = tier.createAnnotation(begin, end);
            newAnnotation();
        }
        /*
        if (newAnnotation != null) {
            newAnnBegin = newAnnotation.getBeginTimeBoundary();
            newAnnEnd = newAnnotation.getEndTimeBoundary();
        }
        */
        
        Boolean boolPref = Preferences.getBool("ClearSelectionAfterCreation", null);
        
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
        }
        
        setWaitCursor(false);
    }
    
    /**
     * Check the settings for snapping to existing annotations and for adjusting to video frames.
     */
    protected void adjustTimes() {
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
    	if(stickToVideoFrames && tier.isTimeAlignable()){
    		if(!tier.hasParentTier() || tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN){
    			begin = getStartPointOfFrame(begin);
    			end = getEndPointOfFrame(end);
    		}    		
    	}
    	
    	 //snap annotations, this overrides the stick to frames adjustments
    	if(snapAnnotations && tier.isTimeAlignable()){
        	if(!tier.hasParentTier() || tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN){
        		long snapValue = 0L;
        		Long longPref = Preferences.getLong("SnapAnnotationsValue", null);
            	if (longPref != null){
            		snapValue = longPref.longValue();
            		if (snapValue == 0) {
            			return;
            		}
            	}    	
        		
//        		AbstractAnnotation annBefore = (AbstractAnnotation) tier.getAnnotationBefore(begin) ;
//                AbstractAnnotation annAfter = (AbstractAnnotation) tier.getAnnotationAfter(end);
                
                long firstDiff = Long.MAX_VALUE;
                long secDiff = firstDiff;
                
	            AbstractAnnotation annBefore = (AbstractAnnotation) tier.getAnnotationAtTime(begin);
	            if (annBefore == null) {
	            	annBefore = (AbstractAnnotation) tier.getAnnotationBefore(begin);
	            }
	            
	            AbstractAnnotation annAfter = (AbstractAnnotation) tier.getAnnotationAtTime(end);
	            if (annAfter == null) {
	            	annAfter = (AbstractAnnotation) tier.getAnnotationAfter(end);
	            }
	            
                if(annBefore != null){
                	firstDiff = begin - annBefore.getEndTimeBoundary();
	            	if (firstDiff != 0 && firstDiff >= -snapValue && firstDiff <= snapValue) {
	            		begin = begin - firstDiff;
	            	}
                }
                	
                if(annAfter != null) {
                	secDiff = annAfter.getBeginTimeBoundary() - end;
	            	if (secDiff != 0 && secDiff >= -snapValue && secDiff <= snapValue) {
	            		end = end + secDiff;
	            	}
                } 
                /*
                if(firstDiff < secDiff && firstDiff < snapValue){
                	begin = begin-firstDiff;
                	end = end-firstDiff;                			
                } else if(secDiff < snapValue){
                	begin = begin+secDiff;
                	end = end+secDiff;                			
               	} 
               	*/               	
            }
        } 
    }
    
    /**
     * A method for storing information for the undo operation. 
     * Can be called or overridden by sub-classes. 
     */
    protected void storeForUndo() {
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
    }
    
    /**
     * A method for restoring the original situation in the undo operation.
     * Can be called or overridden by subclasses.
     */
    protected void restoreInUndo() {
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
    }
    
    /**
     * The creation of the new annotation in a separate method to allow overriding.
     */
    void newAnnotation() {  	
        newAnnotation = tier.createAnnotation(begin, end);        
        
        if (newAnnotation != null) {
            newAnnBegin = newAnnotation.getBeginTimeBoundary();
            newAnnEnd = newAnnotation.getEndTimeBoundary();
            if (annotationValue != null) {
            	newAnnotation.setValue(annotationValue);
            }
            
            if(MonitoringLogger.isInitiated()){
            	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.NEW_ANNOTATION, tier.getName(), Long.toString(begin), Long.toString(end));
            }
            
        }// check some possible causes and popup a message
        else if (tier.isTimeAlignable() && tier.getParentTier() != null) {
        	AlignableAnnotation aa = (AlignableAnnotation) tier.getParentTier().getAnnotationAtTime(
        			(begin + end) / 2);
        	// popup a message if there is no parent annotation?
        	if (aa == null) {
        		return;
        	}
        	if (!aa.getBegin().isTimeAligned() || !aa.getEnd().isTimeAligned()) {
        		JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription), 
        				ElanLocale.getString("Message.UnalignedParentAnnotation"),
        				ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
        	}
        } else if (tier.getLinguisticType().getConstraints() != null && 
        		tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
        	// if there is already an annotation at that location a new one cannot be created
    		JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription), 
    				ElanLocale.getString("Message.ExistingAnnotation"),
    				ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
        }
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
    		curFrame =(long) (curFrame * milliSecondsPerSample);
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
    		curFrame = (long) ((curFrame + 1) * milliSecondsPerSample);  
    	} else {
    		curFrame = newEnd;
    	}
    	return curFrame;
	}

    /**
     * Stores information of all effected annotations in normal time
     * propagation mode. Assumption: no annotations on parenttiers will be
     * effected.
     */
    private void storeNormal() {
        if (rootTier != null) {
            List<Annotation> possiblyEffectedAnn = rootTier.getOverlappingAnnotations(begin,
                    end);
            AbstractAnnotation aa;

            // use the changedAnnotations arraylist
            for (int i = 0; i < possiblyEffectedAnn.size(); i++) {
                aa = (AbstractAnnotation) possiblyEffectedAnn.get(i);
                changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                        aa));
            }
        } else {
        	List<Annotation> effectedAnn = tier.getOverlappingAnnotations(begin, end);
            AbstractAnnotation aa;

            for (int i = 0; i < effectedAnn.size(); i++) {
                aa = (AbstractAnnotation) effectedAnn.get(i);

                if (aa.getBeginTimeBoundary() < begin) {
                    changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            aa));
                } else if (aa.getEndTimeBoundary() > end) {
                    changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            aa));
                } else {
                    removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            aa));
                }
            }
        }
    }

    /**
     * Restore the situation before the edit action; normal mode.
     */
    private void restoreNormal() {
        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        if (rootTier != null) {
            long mid = (newAnnBegin + newAnnEnd) / 2;
            DefaultMutableTreeNode node = null;
            AnnotationDataRecord annRecord = null;

            for (int i = 0; i < changedAnnotations.size(); i++) {
                node = (DefaultMutableTreeNode) changedAnnotations.get(i);
                annRecord = (AnnotationDataRecord) node.getUserObject();

                if ((annRecord.getBeginTime() <= mid) &&
                        (annRecord.getEndTime() >= mid)) {
                    break;
                }
            }

            if (node == null) {
                return;
            }

            Annotation rootAnn = rootTier.getAnnotationAtTime(mid);

            if (rootAnn != null) {
                rootTier.removeAnnotation(rootAnn);
                AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
            }
        } else {
            // first delete changed annotations
            DefaultMutableTreeNode node;
            AnnotationDataRecord dataRecord;
            AbstractAnnotation aa;

            if (changedAnnotations.size() > 0) {
                for (int i = 0; i < changedAnnotations.size(); i++) {
                    node = (DefaultMutableTreeNode) changedAnnotations.get(i);
                    dataRecord = (AnnotationDataRecord) node.getUserObject();

                    if (dataRecord.getBeginTime() < begin) {
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

            // now recreate all annotations that have been deleted
            if (changedAnnotations.size() > 0) {
                for (int i = 0; i < changedAnnotations.size(); i++) {
                    node = (DefaultMutableTreeNode) changedAnnotations.get(i);
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                        node);
                }
            }

            if (removedAnnotations.size() > 0) {
                for (int i = 0; i < removedAnnotations.size(); i++) {
                    node = removedAnnotations.get(i);
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                        node);
                }
            }
        }

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
    }

    /**
     * Stores information of all effected annotations in bulldozer time
     * propagation mode. Assumption: no annotations on parent tiers will be effected.<br>
     * On root tiers: <br>
     * if the selection begin is within the boundaries of an existing
     * annotation this annotation will be shifted to the left; all other
     * overlapped  annotations will be shifted to the right. Information on
     * effected  annotations is stored in this order: first the annotation
     * that is shifted  to the left, then all other annotations on the left
     * side that will be  bulldozered (descending), then all annotations that
     * will be shifted to  the right in ascending order.
     */
    private void storeBulldozer() {
        if (rootTier != null) {
            // creation of an annotation on a time-subdivision tier
            // same as normal mode
            storeNormal();
        } else {
            // creation of an annotation on a root tier
            List<Annotation> affectedAnn = tier.getOverlappingAnnotations(begin, end);
            List<AbstractAnnotation> allAnn = tier.getAnnotations();
            int index;

            if (affectedAnn.size() > 0) {
                Annotation aa;

                // check for bulldozer shift on the left + right side
                aa = affectedAnn.get(0);
                index = allAnn.indexOf(aa);

                if ((aa.getBeginTimeBoundary() <= begin) &&
                        (aa.getEndTimeBoundary() > begin)) {
                    leftOffset = (int) (begin - aa.getEndTimeBoundary());

                    if (end <= aa.getEndTimeBoundary()) {
                        rightOffset = 0;
                    } else {
                        if (affectedAnn.size() >= 2) {
                            aa = affectedAnn.get(1);
                            rightOffset = (int) (end -
                                aa.getBeginTimeBoundary());
                        }
                    }
                } else {
                    leftOffset = 0;
                    rightOffset = (int) (end - aa.getBeginTimeBoundary());
                }

                // store info, start on the left
                if (leftOffset < 0) {
                    AbstractAnnotation cur = allAnn.get(index);
                    AbstractAnnotation prev;
                    int gapToBridge = -leftOffset;
                    int gapBridged = 0;

                    changedAnnotations.add(new TimeShiftRecord(
                            cur.getBeginTimeBoundary(),
                            cur.getEndTimeBoundary(), leftOffset));
                    removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            cur));

                    for (int i = index - 1; i >= 0; i--) {
                        prev = allAnn.get(i);

                        int dist = (int) (cur.getBeginTimeBoundary() -
                            prev.getEndTimeBoundary());
                        gapBridged += dist;

                        if (gapBridged < gapToBridge) {
                            changedAnnotations.add(new TimeShiftRecord(
                                    prev.getBeginTimeBoundary(),
                                    prev.getEndTimeBoundary(),
                                    -(gapToBridge - gapBridged)));
                            removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                    prev));
                            cur = prev;
                        } else {
                            break;
                        }
                    }
                }

                if (rightOffset > 0) {
                    if (leftOffset < 0) {
                        index++;
                    }

                    AbstractAnnotation cur = allAnn.get(index);
                    AbstractAnnotation next;
                    int gapToBridge = rightOffset;
                    int gapBridged = 0;

                    changedAnnotations.add(new TimeShiftRecord(
                            cur.getBeginTimeBoundary(),
                            cur.getEndTimeBoundary(), rightOffset));
                    removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            cur));

                    for (int i = index + 1; i < allAnn.size(); i++) {
                        next = allAnn.get(i);

                        int dist = (int) (next.getBeginTimeBoundary() -
                            cur.getEndTimeBoundary());
                        gapBridged += dist;

                        if (gapBridged < gapToBridge) {
                            changedAnnotations.add(new TimeShiftRecord(
                                    next.getBeginTimeBoundary(),
                                    next.getEndTimeBoundary(),
                                    gapToBridge - gapBridged));
                            removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                    next));
                            cur = next;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Restores information of all effected annotations in bulldozer time
     * propagation mode. First deletes the annotation that was originally
     * created by the user,  next deletes the shifted annotations and then
     * recreates the original annotations.
     */
    private void restoreBulldozer() {
        if (rootTier != null) {
            // creation of an annotation on a time-subdivision tier
            // same as normal mode
            restoreNormal();
        } else {
            // creation of an annotation on a root tier
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            // work
            // delete the created annotation
            Annotation createdAnn = tier.getAnnotationAtTime(begin);

            if (createdAnn != null) {
                tier.removeAnnotation(createdAnn);
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
                        AnnotationRecreator.createAnnotationFromTree(transcription,
                            node, true);
                    }
                }
            }

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
        }
    }

    /**
     * Stores information on all effected annotations in shift propagation
     * mode.
     */
    private void storeShift() {
        if (rootTier != null) {
            // time subdivision
            storeNormal();
        } else {
        	List<Annotation> affectedAnn = tier.getOverlappingAnnotations(begin, end);
            AbstractAnnotation aa;

            if (affectedAnn.size() > 0) {
                aa = (AbstractAnnotation) affectedAnn.get(0);

                if ((aa.getBeginTimeBoundary() < begin) &&
                        (aa.getEndTimeBoundary() > begin)) {
                    changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                            aa));
                }
            }
        }
    }

    /**
     * Restores information of all effected annotations in shift time-
     * propagation mode.
     */
    private void restoreShift() {
        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        if (rootTier != null) {
            // shift the slots backward	
            transcription.shiftBackward(begin, -(end - begin));
            restoreNormal();
        } else {
            DefaultMutableTreeNode node = null;

            if (changedAnnotations.size() > 0) {
                Annotation aa;
                node = (DefaultMutableTreeNode) changedAnnotations.get(0);

                AnnotationDataRecord annRecord = (AnnotationDataRecord) node.getUserObject();
                long begin = annRecord.getBeginTime();

                aa = tier.getAnnotationAtTime(begin);

                if (aa != null) {
                    tier.removeAnnotation(aa);
                }
            }

            // shift the slots backward	
            transcription.shiftBackward(begin, -(end - begin));

            if (node != null) {
                AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
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
    void setWaitCursor(boolean showWaitCursor) {
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
