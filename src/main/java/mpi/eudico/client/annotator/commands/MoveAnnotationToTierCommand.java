package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.client.annotator.util.TierNameCompare;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A class to move a (group of) annotation(s) to a different tier.
 * Roughly equivalent to a copy, paste and delete original sequence.
 *  
 * @author Han Sloetjes
 *
 */
public class MoveAnnotationToTierCommand extends PasteAnnotationTreeCommand {
	private DefaultMutableTreeNode movedNode = null;
	private String origTierName = null;
	private boolean origAnnotationDeleted = false; 
	
	/**
	 * Constructor.
	 * @param name
	 */
	public MoveAnnotationToTierCommand(String name) {
		super(name);
	}

	/**
	 * Creates the new annotation again and deletes the original annotation.
	 */
	@Override
	public void redo() {
        if (transcription != null) {
            setWaitCursor(true);
            transcription.setNotifying(false);
            
            newAnnotation();
            
            AbstractAnnotation aa = null;
            if (record != null && origTierName != null && origAnnotationDeleted) {
            	// record can not be null
            	TierImpl t = (TierImpl) transcription.getTierWithId(origTierName);
            	if (t != null) {
            		aa = (AbstractAnnotation) t.getAnnotationAtTime((record.getBeginTime() + record.getEndTime()) / 2);
            		if (aa != null) {
            			t.removeAnnotation(aa);
            		}
            	}
            }
            transcription.setNotifying(true);
            setWaitCursor(false);
        }
	}

	/**
	 * Calls the super implementation to remove the moved annotation from its new tier
	 * and recreates the annotation (with depending annotations) on the original tier.
	 * 
	 */
	@Override
	public void undo() {
		boolean setNotifyingOff = false;

        // copied from New AnnotationCommand
        if ((tier != null) && (newAnnotation != null)) {
            setWaitCursor(true);

            Annotation aa = tier.getAnnotationAtTime((newAnnBegin + newAnnEnd) / 2);

            if (aa != null) {
                tier.removeAnnotation(aa);
                if(MonitoringLogger.isInitiated()){
                	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.NEW_ANNOTATION);
                }
            }

            if (tier.isTimeAlignable()) {
                transcription.setNotifying(false);
                setNotifyingOff = true;

                restoreInUndo();
                
//                transcription.setNotifying(true);
            }
            
            if (movedNode != null && origAnnotationDeleted) {
            	//transcription.setNotifying(false);
            	if (timePropMode != Transcription.NORMAL) {
            		transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            	}
            	
            	AnnotationRecreator.createAnnotationFromTree(transcription, movedNode, true);
            	
            	if (timePropMode != Transcription.NORMAL) {
            		transcription.setTimeChangePropagationMode(timePropMode);
            	}
            	//transcription.setNotifying(true);
            } else if (record != null && origAnnotationDeleted) {
            	if (timePropMode != Transcription.NORMAL) {
            		transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            	}
            	TierImpl t = (TierImpl) transcription.getTierWithId(origTierName);
            	AbstractAnnotation oa = (AbstractAnnotation) t.createAnnotation(record.getBeginTime(), record.getEndTime());
            	
            	if (oa != null) {
                    AnnotationRecreator.restoreValueEtc((AbstractAnnotation) oa, record, false);
            	}
            	if (timePropMode != Transcription.NORMAL) {
            		transcription.setTimeChangePropagationMode(timePropMode);
            	}
            }
            
            if (setNotifyingOff) {
            	transcription.setNotifying(true);
            }
            
            setWaitCursor(false);
        }
	}

	/**
	 * @param receiver the destination tier
	 * @param arguments the arguments
	 * <ul> <li>arg[0] the annotation to move</li>
	 * 		<li>arg[1] = the begin time of the
     *        annotation (Long) (if present)</li> <li>arg[2] = the end time of the
     *        annotation (Long) (if present)</li> </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		// some initialization that normally occurs in NewAnnotationCommand
		tier = (TierImpl) receiver;

		destTierName = tier.getName();
		transcription = (TranscriptionImpl) tier.getTranscription();
        changedAnnotations = new ArrayList();
        removedAnnotations = new ArrayList<DefaultMutableTreeNode>();
		
		AbstractAnnotation aa = (AbstractAnnotation) arguments[0];
		TierImpl srcTier = (TierImpl) aa.getTier();
		origTierName = srcTier.getName();
		// hier check tier type compatibility 
		if (tier == null || srcTier == null || (tier.getParentTier() != null)) {
			return;
		}
		
		int numDescendants = 0;
		if (aa.getParentListeners().size() == 0) {
			// create data record
			record = new AnnotationDataRecord(aa);
		} else {
			// create tree 
			node = AnnotationRecreator.createTreeForAnnotation(aa);
			record = (AnnotationDataRecord) node.getUserObject();
			numDescendants = getDescendantCount(node);
		}
		timePropMode = transcription.getTimeChangePropagationMode();
		// store existing annotations on dest tier
		begin = aa.getBeginTimeBoundary();
		end = aa.getEndTimeBoundary();
		
		if (arguments.length > 2) {
			begin = (Long) arguments[1];
			end = (Long) arguments[2];
		}
		newAnnBegin = begin;
		newAnnEnd = end;
		
		if (node != null) {
			adjustTierNames(node, tier.getName());
		} else if (record != null) {
			record.setTierName(tier.getName());
		}
		
		//Object[] nextArgs = new Object[]{new Long(aa.getBeginTimeBoundary()), new Long(aa.getEndTimeBoundary())};
		//super.execute(receiver, nextArgs);
		storeForUndo();
		newAnnotation();
		
		// finally delete original annotation
		if (newAnnotation != null) {
			// check the number of descendants of the "copy"
			movedNode = AnnotationRecreator.createTreeForAnnotation(aa);
			if (numDescendants > 0) {
				DefaultMutableTreeNode checkNode = AnnotationRecreator.createTreeForAnnotation((AbstractAnnotation) newAnnotation);
				if (checkNode != null) {
					int numMovedDescendants = getDescendantCount(checkNode);
					if (numDescendants == numMovedDescendants) {
						// remove the original
						((TierImpl) aa.getTier()).removeAnnotation(aa);
						origAnnotationDeleted = true;
					} else {
						// generate a warning. Since there is no systematic way yet to pass messages to the user interface 
						// level from a command, just log a message here for now
						ClientLogger.LOG.warning("Not all descendent annotations could be moved, therefore the original annotation is copied not moved.");
					}
				}
			} else {
				((TierImpl) aa.getTier()).removeAnnotation(aa);
				origAnnotationDeleted = true;				
			}

		}
		
        // HS July 2012: after the actions above the focus is completely lost, 
        // none of the keyboard shortcuts seem to function
        // try to correct this by giving the frame the focus
		// see also DuplicateAnnotation, PasteAnnotation
        SwingUtilities.invokeLater(new Runnable(){
            @Override
			public void run() {
                ELANCommandFactory.getRootFrame(transcription).requestFocus();
            }
        });
	}

	
	/**
	 * Do not adjust the begin and end times
	 */
	@Override
	protected void adjustTimes() {

	}
	
	/**
	 * Updates names of tiers in the tree, based on shared suffixes.
	 * @param inNode the root node
	 * @param destTierName the new root tier name
	 */
	protected void adjustTierNames(DefaultMutableTreeNode inNode, String destTierName) {
		if (inNode != null && destTierName != null) {
			TierNameCompare tnc = new TierNameCompare();
			AnnotationDataRecord aRecord = (AnnotationDataRecord) inNode.getUserObject();
			
        	int[] indices = tnc.findCorrespondingAffix(aRecord.getTierName(), destTierName);
        	if (indices != null && indices[0] > -1) {
        		char del = aRecord.getTierName().charAt(indices[0]);
        		String affix = "";
        		if (indices[1] <= TierNameCompare.PREFIX_MODE) {
        			int di = destTierName.lastIndexOf(del);
        			if (di > -1) {
        				affix = destTierName.substring(di);
        			}
        		} else {
        			int di = destTierName.indexOf(del);
        			if (di > -1) {
        				affix = destTierName.substring(0, di);
        			}
        		}
        		tnc.adjustTierNames(inNode, affix, del, indices[1]);
        	}
		}
		
	}

	/**
	 * Returns the name.
	 */
	@Override
	public String getName() {
		return super.getName();
	}
	
	/**
	 * Counts the number of descendants.
	 *  
	 * @param node the parent node
	 * @return the number of descendant nodes, 0 by default
	 */
	private int getDescendantCount(TreeNode node) {
		int count = 0;
		if (node != null) {
			count = node.getChildCount();
			if (count > 0) {
				TreeNode tn = null;
				for (int i = 0; i < node.getChildCount(); i++) {
					tn = node.getChildAt(i);
					count += getDescendantCount(tn);
				}
			}
		}
		return count;
	}

}
