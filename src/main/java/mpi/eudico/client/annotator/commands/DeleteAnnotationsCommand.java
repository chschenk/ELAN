package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Deletes multiple annotations, on the active tier in a given time interval.
 *
 * @version 2.0 Added support for deletion of annotations on all tiers 
 * @author Han Sloetjes
 * @version 1.0
 */
public class DeleteAnnotationsCommand implements UndoableCommand {
    private String commandName;
    private TranscriptionImpl transcription;
    private String tierName;
    private List<String> rootTiers;
    private Long bt;
    private Long et;
    private List<DefaultMutableTreeNode> delAnnRecords;

    /**
     * Creates a new DeleteAnnotationCommand instance.
     *
     * @param name the name of the command
     */
    public DeleteAnnotationsCommand(String name) {
        commandName = name;
    }

    /**
     * Deletes the annotations again
     */
    @Override
	public void redo() {
        deleteAnnotations();
    }

    /**
     * Restores the annotations that have been deleted
     */
    @Override
	public void undo() {
        if ((transcription != null) && (delAnnRecords != null)) {
        	if (tierName != null) {
	            TierImpl tier = transcription.getTierWithId(tierName);
	
	            if (tier == null) {
	                ClientLogger.LOG.warning("The tier could not be found: " +
	                    tierName);
	
	                return;
	            }
	
	            int curPropMode = 0;
	
	            curPropMode = transcription.getTimeChangePropagationMode();
	
	            if (curPropMode != Transcription.NORMAL) {
	                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
	            }
	            transcription.setNotifying(false);
	            setWaitCursor(true);
	
	            if (!tier.hasParentTier()) {
	                for (DefaultMutableTreeNode n : delAnnRecords) {
	                    AnnotationRecreator.createAnnotationFromTree(transcription,
	                        n, true);
	                }
	            } else {
	                AnnotationRecreator.createAnnotationsSequentially(transcription,
	                    delAnnRecords, true);
	            }
	
	            setWaitCursor(false);
	            
	            // restore the time propagation mode
	            transcription.setTimeChangePropagationMode(curPropMode);
	            transcription.setNotifying(true);
	        } else if (rootTiers != null && rootTiers.size() > 0) {
	        	
	            int curPropMode = 0;
	
	            curPropMode = transcription.getTimeChangePropagationMode();
	
	            if (curPropMode != Transcription.NORMAL) {
	                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
	            }
	            transcription.setNotifying(false);
	            setWaitCursor(true);
	            
                for (DefaultMutableTreeNode n : delAnnRecords) {
                    AnnotationRecreator.createAnnotationFromTree(transcription,
                        n, true);
                }
                
	            setWaitCursor(false);
	        	
	            // restore the time propagation mode
	            transcription.setTimeChangePropagationMode(curPropMode);
	            transcription.setNotifying(true);
	        }
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct. If arg[0] is null, all tiers are assumed.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the Tier
     *        (TierImpl)</li> <li>arg[1] = the begin time of the interval
     *        (Long)</li> <li>arg[2] = the end time of the interval
     *        (Long)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        if (arguments != null) {
            TierImpl tier = (TierImpl) arguments[0];
            if (tier != null) {
            	tierName = tier.getName();	
            } else {
            	rootTiers = new ArrayList<String>();
            	List<TierImpl> allTiers = transcription.getTiers();
                TierImpl tier2 = null;
                for (int i = 0; i < allTiers.size(); i++) {
                	tier2 = allTiers.get(i);
                	if (tier2.getParentTier() == null && tier2.isTimeAlignable()) {
                		rootTiers.add(tier2.getName());
                	}
                }
            }
            //tierName = tier.getName();
            bt = (Long) arguments[1];
            et = (Long) arguments[2];
            delAnnRecords = new ArrayList<DefaultMutableTreeNode>();

            setWaitCursor(true);
            storeAnnotationTrees(tier, bt, et);

            deleteAnnotations();
            setWaitCursor(false);
        }
    }

    private void storeAnnotationTrees(TierImpl tier, long begin, long end) {
        AbstractAnnotation annotation;
        DefaultMutableTreeNode node;
        if (tier != null) {
	        List<AbstractAnnotation> annos = tier.getAnnotations();
	
	        for (int i = 0; i < annos.size(); i++) {
	            annotation = annos.get(i);
	
	            if (annotation.getEndTimeBoundary() < begin) {
	                continue;
	            }
	
	            if (annotation.getBeginTimeBoundary() >= end) {
	                break;
	            }
	
	            if ((annotation.getBeginTimeBoundary() >= begin) &&
	                    (annotation.getEndTimeBoundary() <= end)) {
	                node = AnnotationRecreator.createTreeForAnnotation(annotation);
	
	                if (node != null) {
	                    delAnnRecords.add(node);
	                }
	            }
	        }
        } else {
        	if (rootTiers != null && rootTiers.size() > 0) {
        		TierImpl tier2 = null;
        		for (String tName : rootTiers) {
        			tier2 = transcription.getTierWithId(tName);
        			if (tier2 != null) {
        		        List<AbstractAnnotation> annos = tier2.getAnnotations();

        		        for (int i = 0; i < annos.size(); i++) {
        		            annotation = annos.get(i);
        		
        		            if (annotation.getEndTimeBoundary() < begin) {
        		                continue;
        		            }
        		
        		            if (annotation.getBeginTimeBoundary() >= end) {
        		                break;
        		            }
        		
        		            if ((annotation.getBeginTimeBoundary() >= begin) &&
        		                    (annotation.getEndTimeBoundary() <= end)) {
        		                node = AnnotationRecreator.createTreeForAnnotation(annotation);
        		
        		                if (node != null) {
        		                    delAnnRecords.add(node);
        		                }
        		            }
        		        }
        			}
        		}
        	}
        }
    }

    private void deleteAnnotations() {
        if ((delAnnRecords != null) && (delAnnRecords.size() > 0)) {
        	List<Annotation> toDelete = new ArrayList<Annotation>(delAnnRecords.size());
        	if (tierName != null) {
	            TierImpl tier = transcription.getTierWithId(tierName);
	
	            if (tier == null) {
	                ClientLogger.LOG.warning("The tier could not be found: " +
	                    tierName);
	
	                return;
	            }
	
	            AbstractAnnotation annotation;
	            AnnotationDataRecord record;
	
	            for (DefaultMutableTreeNode n : delAnnRecords) {
	                record = (AnnotationDataRecord) n.getUserObject();
	                annotation = (AbstractAnnotation) tier.getAnnotationAtTime((record.getBeginTime() +
	                        record.getEndTime()) / 2);
	
	                if (annotation != null) {
	                    //tier.removeAnnotation(annotation);
	                    toDelete.add(annotation);
	                }
	            }
        	} else {
        		TierImpl tier = null;
        		AbstractAnnotation annotation;
   	            AnnotationDataRecord record;
   	            
	            for (DefaultMutableTreeNode n : delAnnRecords) {
	                record = (AnnotationDataRecord) n.getUserObject();
	                tier = (transcription.getTierWithId(record.getTierName()));
	                
	                if (tier == null) {
	                	 ClientLogger.LOG.warning("The tier could not be found: " +
	                			 record.getTierName());
	                	 continue;
	                }
	                
	                annotation = (AbstractAnnotation) tier.getAnnotationAtTime((record.getBeginTime() +
	                        record.getEndTime()) / 2);
	
	                if (annotation != null) {
	                    //tier.removeAnnotation(annotation);
	                    toDelete.add(annotation);
	                }
	            }
        	}
            transcription.setNotifying(false);
            for (Annotation ann : toDelete) {
            	((TierImpl) ann.getTier()).removeAnnotation(ann);
            }
            transcription.setNotifying(true);
        }
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
