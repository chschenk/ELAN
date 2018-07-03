package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.util.MutableInt;


/**
 * An undoable command that merges two adjacent annotations on a top level
 * tier. Depending annotations are also merged, it depends on the type of tier
 * how annotations are added.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class MergeAnnotationsCommand implements UndoableCommand {
    private String commandName;
    private Transcription transcription;
    private AnnotationDataRecord activeAnnRecord;
    private AnnotationDataRecord mergeAnnRecord;
    private DefaultMutableTreeNode activeNode;
    private DefaultMutableTreeNode mergeAnnNode;
    private DefaultMutableTreeNode mergeAnnNodeComp;
    private boolean simpleMerge = false;
    
    private boolean mergeWithNext = false;
    
    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public MergeAnnotationsCommand(String name) {
        commandName = name;
    }

    /**
     * Repeat the operation from execute.
     */
    @Override
	public void redo() {
        if (simpleMerge) {
            mergeSimple();
        } else {
            mergeComplex();
        }
        
        if(MonitoringLogger.isInitiated()){
        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.MERGE_ANNOTATION);
        }
    }

    /**
     * In the simple case revert the changes to the first annotation and
     * recreate the second. In the complex case delete the modified first
     * annotation and recreate both original annotations.
     */
    @Override
	public void undo() {
        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();
//        ((TranscriptionImpl) transcription).setNotifying(false);
        
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        if (simpleMerge) {
            TierImpl tier = (TierImpl) transcription.getTierWithId(activeAnnRecord.getTierName());

            if (tier != null) {
                AlignableAnnotation aa = (AlignableAnnotation) tier.getAnnotationAtTime(activeAnnRecord.getBeginTime());

                if (aa != null) {                	
                		aa.updateTimeInterval(aa.getBeginTimeBoundary(),
                					activeAnnRecord.getEndTime());
                		aa.setValue(activeAnnRecord.getValue());
                }

                AlignableAnnotation aa2 = (AlignableAnnotation) tier.createAnnotation(mergeAnnRecord.getBeginTime(),
                		mergeAnnRecord.getEndTime());

                if (aa2 != null) {
                    AnnotationRecreator.restoreValueEtc(aa2, mergeAnnRecord, true);   
                }
            } else {
                ClientLogger.LOG.warning(
                    "Cannot undo annotation merge: tier not found");
            }
        } else {
            // complex
        	((TranscriptionImpl) transcription).setNotifying(false);
            AnnotationDataRecord annData = (AnnotationDataRecord) activeNode.getUserObject();
            TierImpl rootTier = (TierImpl) transcription.getTierWithId(annData.getTierName());

            if (rootTier != null) {
                AlignableAnnotation aaAct = (AlignableAnnotation) rootTier.getAnnotationAtTime(annData.getBeginTime());

                if (aaAct != null) {
                    rootTier.removeAnnotation(aaAct);
                }

                AnnotationRecreator.createAnnotationFromTree(transcription,
                    activeNode, true);
                AnnotationRecreator.createAnnotationFromTree(transcription,
                		mergeAnnNode, true);
            }
            ((TranscriptionImpl) transcription).setNotifying(true);
        }
        
        if(MonitoringLogger.isInitiated()){
        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.MERGE_ANNOTATION);
        }
        
//        ((TranscriptionImpl) transcription).setNotifying(true);
        transcription.setTimeChangePropagationMode(curPropMode);
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the active annotation
     *        (Annotation)</li> 
     *        li>arg[0] = merge with next annotation,(Boolean)</li> 
     *        </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (Transcription) receiver;
        mergeWithNext = (Boolean) arguments[1];

        if ((arguments.length > 0) &&
                arguments[0] instanceof AlignableAnnotation) {
            AlignableAnnotation aa = (AlignableAnnotation) arguments[0];
            
            AlignableAnnotation mergeAnn = null;
            
            if(mergeWithNext){
            	// check if there is a next
            	mergeAnn = (AlignableAnnotation) ((TierImpl) aa.getTier()).getAnnotationAfter(aa);
            } else {
            	// Annotations are swapped to re-use the same code for merge with next annotation
            	mergeAnn = aa;
            	aa = (AlignableAnnotation) ((TierImpl) aa.getTier()).getAnnotationBefore(mergeAnn);
            }

            if (mergeAnn != null  && aa !=null) {
                if ((aa.getParentListeners().size() == 0) &&
                        (mergeAnn.getParentListeners().size() == 0)) {
                    activeAnnRecord = new AnnotationDataRecord(aa);

                	mergeAnnRecord = new AnnotationDataRecord(mergeAnn);

                    simpleMerge = true;
                    mergeSimple();
                } else {
                    activeNode = AnnotationRecreator.createTreeForAnnotation(aa);
                    mergeAnnNode = AnnotationRecreator.createTreeForAnnotation(mergeAnn);// unmodified for undo
                    mergeAnnNodeComp = AnnotationRecreator.createTreeForAnnotation(mergeAnn);// modified for symbolic subdivisions
                    recalculateSymbolicSubdivisions(activeNode, mergeAnnNodeComp, aa.getBeginTimeBoundary(), 
                    		mergeAnn.getEndTimeBoundary());
                    
                    mergeComplex();
                }
            }
        }
        
        if(MonitoringLogger.isInitiated()){
        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.MERGE_ANNOTATION);
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
     * Merge action in case there are no child annotations
     */
    private void mergeSimple() {
        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();
        //((TranscriptionImpl) transcription).setNotifying(false);
        
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        TierImpl tier = (TierImpl) transcription.getTierWithId(activeAnnRecord.getTierName());

        if (tier != null) {
            AlignableAnnotation aa = (AlignableAnnotation) tier.getAnnotationAtTime(activeAnnRecord.getBeginTime());

            if (aa != null ) {            	
            		aa.updateTimeInterval(aa.getBeginTimeBoundary(),
            				mergeAnnRecord.getEndTime());
            		aa.setValue(aa.getValue() + " " + mergeAnnRecord.getValue());
            		// By changing the Value, extRefs and cvEntryIds become meaningless.

            }
        }

        //((TranscriptionImpl) transcription).setNotifying(true);
        transcription.setTimeChangePropagationMode(curPropMode);
    }

    /**
     * Merge operation with child annotations. First the first root
     * annotation's time interval is updated,  then the information of the
     * next annotation tree is processed; either new annotations are created
     * (subdivision and included in) or the values of existing annotations are
     * updated (root and sym associated children of the root).
     */
    private void mergeComplex() {
        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }
        ((TranscriptionImpl) transcription).setNotifying(false);
        
        AnnotationDataRecord annData = (AnnotationDataRecord) activeNode.getUserObject();
        DefaultMutableTreeNode root = mergeAnnNodeComp;
        TierImpl rootTier = (TierImpl) transcription.getTierWithId(annData.getTierName());

        if (rootTier != null) {
        	
            AlignableAnnotation aaAct = (AlignableAnnotation) rootTier.getAnnotationAtTime(annData.getBeginTime());

            if (aaAct != null) {            	
            	aaAct.updateTimeInterval(aaAct.getBeginTimeBoundary(),
                            ((AnnotationDataRecord) root.getUserObject()).getEndTime());

                // now update and / or recreate the child annotations of the next annotation
                // this is a slightly modified version of AnnotationRecreator.createAnnotationFromTree(
                // Transcription trans, DefaultMutableTreeNode root)
                boolean includeID = false; // other root annotation don't copy id's ??
                DefaultMutableTreeNode node;
                AlignableAnnotation aa = null;
                RefAnnotation ra = null;
                Annotation an = null;
                AlignableAnnotation curLastChild;

                /* create new annotations
                 * iterate twice over the depending annotations: on the first
                 * pass only Annotations with a time alignable begin time are
                 * created, on the second pass the rest is done.
                 */
                Enumeration en = root.breadthFirstEnumeration();
                TierImpl tier = null;
                long begin;
                long end;
                int linStereoType = -1;

                while (en.hasMoreElements()) {
                    aa = null; //reset
                    node = (DefaultMutableTreeNode) en.nextElement();
                    annData = (AnnotationDataRecord) node.getUserObject();

                    if (node.isRoot()) {                    	
                    		aaAct.setValue(aaAct.getValue() + " " +
                                    annData.getValue());
                        continue;
                    }

                    tier = (TierImpl) transcription.getTierWithId(annData.getTierName());

                    if (tier == null) {
                        ClientLogger.LOG.severe(
                            "Cannot recreate annotations: tier does not exist.");

                        continue;
                    }

                    if (tier.isTimeAlignable()) {
                        if (annData.isBeginTimeAligned()) {
                            begin = annData.getBeginTime();
                            end = annData.getEndTime();

                            // this sucks... sometimes an annotation can have the same begin and 'virtual'
                            // end time on a time-subdivision tier
                            if (!annData.isEndTimeAligned() && (end == begin)) {
                                end++;
                            }
                            // if there is/are already one or more unaligned annotations on this spot, make sure
                            // these are shifted to the time before this new annotation
                            curLastChild = (AlignableAnnotation) tier.getAnnotationAtTime(aaAct.getEndTimeBoundary() - 1);
                            if (curLastChild != null && !curLastChild.getBegin().isTimeAligned()) {
                            	AlignableAnnotation nlChild = (AlignableAnnotation) tier.createAnnotationAfter(curLastChild);
                            	if (nlChild != null) {
                            		nlChild.updateTimeInterval(begin, end);
                            		aa = nlChild;
                            	}
                            } else {                               
                                aa = (AlignableAnnotation) tier.createAnnotation(begin,
                                        end);
                            }                           

                            AnnotationRecreator.restoreValueEtc(aa, annData, includeID);   
                        }
                    } else {
                        // non-alignable in second run
                    }
                }

                // second run
                en = root.breadthFirstEnumeration();
                en.nextElement(); //skip root
                                  // for re-creation of unaligned annotation on Alignable (Time-Subdivision) tiers

                Annotation prevAnn = null;

                while (en.hasMoreElements()) {
                    aa = null; //reset
                    an = null;
                    ra = null;
                    node = (DefaultMutableTreeNode) en.nextElement();

                    annData = (AnnotationDataRecord) node.getUserObject();

                    tier = (TierImpl) transcription.getTierWithId(annData.getTierName());

                    if (tier == null) {
                        ClientLogger.LOG.severe(
                            "Cannot recreate annotations: tier does not exist.");

                        continue;
                    }

                    if (tier.isTimeAlignable()) {
                        if (!annData.isBeginTimeAligned()) {
                            if ((prevAnn != null) &&
                                    (!prevAnn.getTier().getName()
                                                 .equals(annData.getTierName()) ||
                                    (prevAnn.getEndTimeBoundary() <= annData.getBeginTime()))) {
                                // reset previous annotation field
                                prevAnn = null;
                            }

                            if (prevAnn == null) {
                                begin = annData.getBeginTime();
                                end = annData.getEndTime();
                                an = tier.getAnnotationAtTime( /*annData.getEndTime() - 1*/
                                        begin /*< end ? begin + 1 : begin*/);

                                if (an != null) {
                                    aa = (AlignableAnnotation) tier.createAnnotationAfter(an);
                                    prevAnn = aa;
                                } else {
                                    // time subdivision of a time subdivision...
                                    aa = (AlignableAnnotation) tier.createAnnotation(begin,
                                            end);
                                    prevAnn = aa;
                                }
                            } else {
                                aa = (AlignableAnnotation) tier.createAnnotationAfter(prevAnn);

                                prevAnn = aa;
                            }

                            AnnotationRecreator.restoreValueEtc(aa, annData, includeID); 
                        } else {
                            //reset the prevAnn object when an aligned annotation is encountered
                            prevAnn = null;
                        }
                    } else {
                        // ref annotations
                        linStereoType = tier.getLinguisticType().getConstraints()
                                            .getStereoType();

                        if (linStereoType == Constraint.SYMBOLIC_SUBDIVISION) {
                            begin = annData.getBeginTime() /*+ 1*/;
                            
                            if (node.getParent() == root) {
                            	begin = ((AnnotationDataRecord) root.getUserObject()).getEndTime() - 1; 
                            } else {
                            	if (((TierImpl)tier.getParentTier()).getLinguisticType().getConstraints().getStereoType()
                            			== Constraint.SYMBOLIC_ASSOCIATION) {
                            		begin = ((AnnotationDataRecord) ((DefaultMutableTreeNode)node.getParent()).getUserObject()).getEndTime() - 1;
                            	}
                            }

                            //an = tier.getAnnotationAtTime(begin);
                            if ((prevAnn != null) &&
                                    !prevAnn.getTier().getName()
                                                .equals(annData.getTierName())) {
                                // reset previous annotation field
                                prevAnn = null;
                            }

                            if ((prevAnn != null) &&
                                    (prevAnn.getEndTimeBoundary() < begin)) {
                                prevAnn = null;
                            }

                            if (prevAnn == null) {
                                an = tier.getAnnotationAtTime(begin);

                                if (an != null) {
                                    if (an.getBeginTimeBoundary() == begin) {
                                        // the first annotation
                                        ra = (RefAnnotation) tier.createAnnotationBefore(an);
                                    } else {
                                        ra = (RefAnnotation) tier.createAnnotationAfter(an);
                                    }
                                } else {
                                    ra = (RefAnnotation) tier.createAnnotation(begin,
                                            begin);
                                }

                                prevAnn = ra;
                            } else {
                                ra = (RefAnnotation) tier.createAnnotationAfter(prevAnn);
                                prevAnn = ra;
                            }

                            AnnotationRecreator.restoreValueEtc(ra, annData, includeID);  
                        } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                            begin = annData.getBeginTime() /*+ 1*/;
                            an = tier.getAnnotationAtTime(begin);

                            if (tier.getParentTier() == rootTier) {
                                // direct child, update the value
                                if (an != null) {
                                    an.setValue(an.getValue() + " " +
                                        annData.getValue());
                                } else {
                                    ra = (RefAnnotation) tier.createAnnotation(begin,
                                            begin);

                                    if (ra != null) {
                                        AnnotationRecreator.restoreValueEtc(ra, annData, false);  
                                    }
                                }

                                continue;
                            }

                            if (an == null) {
                                ra = (RefAnnotation) tier.createAnnotation(begin,
                                        begin);
                            }

                            AnnotationRecreator.restoreValueEtc(ra, annData, includeID);  
                        }
                    }
                }
            }
        }

        ((TranscriptionImpl) transcription).setNotifying(true);
        transcription.setTimeChangePropagationMode(curPropMode);
    }
    
    /**
     * Recalculates virtual begin and end times of sym. subdivision annotations of the second node,
     * based on the (new) total of subdivision annotations and the new interval. Recursive.
     *  
     * @param node1 the node containing the first (toplevel) annotation
     * @param node2 the node containing the second (toplevel) annotation
     * @param begin the begin time of the merged annotations
     * @param end the end time of the merged annotations
     */
    private void recalculateSymbolicSubdivisions(DefaultMutableTreeNode node1, 
    		DefaultMutableTreeNode node2, long begin, long end) {
    	DefaultMutableTreeNode node = null;
    	AnnotationDataRecord annData = null;
    	TierImpl tier = null;
    	Map<String, MutableInt> changedTiers = new HashMap<String, MutableInt>();
    	// find sym. subdiv in the second node
    	for (int i = 0; i < node2.getChildCount(); i++) {
    		node = (DefaultMutableTreeNode) node2.getChildAt(i);
    		annData = (AnnotationDataRecord) node.getUserObject();
    		tier = (TierImpl) transcription.getTierWithId(annData.getTierName());
    		
    		if (tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) {
    			if (!changedTiers.containsKey(annData.getTierName())) {
    				changedTiers.put(annData.getTierName(), new MutableInt(1));
    			} else {
    				changedTiers.get(annData.getTierName()).intValue++;
    			}
    		} else {
    			// check the children
    			checkChildNodes(node, changedTiers, true);
    		}
    	}
    	// now find sym. sub in the first node, add if they are also in the second node
    	for (int i = 0; i < node1.getChildCount(); i++) {
    		node = (DefaultMutableTreeNode) node1.getChildAt(i);
    		annData = (AnnotationDataRecord) node.getUserObject();
    		tier = (TierImpl) transcription.getTierWithId(annData.getTierName());
    		
    		if (tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) {
    			if (changedTiers.containsKey(annData.getTierName())) {
    				changedTiers.get(annData.getTierName()).intValue++;
    			}
    		} else {
    			// check the children
    			checkChildNodes(node, changedTiers, false);
    		}
    	}
    	
    	if (changedTiers.size() == 0) {
    		return;
    	}
    	
    	Iterator<String> tierIt = changedTiers.keySet().iterator();
    	List<DefaultMutableTreeNode> curData = new ArrayList<DefaultMutableTreeNode>(10);
    	String tierName = null;
    	
    	while (tierIt.hasNext()) {
    		curData.clear();
    		tierName = tierIt.next();
    		int totalCount = changedTiers.get(tierName).intValue;
    		int perAnn = (int) Math.ceil((end - begin) / (float)totalCount);
    		
    		Enumeration nodeEnum = node2.breadthFirstEnumeration();
        	while (nodeEnum.hasMoreElements()) {
        		node = (DefaultMutableTreeNode) nodeEnum.nextElement();
        		annData = (AnnotationDataRecord) node.getUserObject();
        		if (annData.getTierName().equals(tierName)) {
        			curData.add(node);
        		}
        	}
        	int start = totalCount - curData.size();
        	for (int i = 0; i < curData.size(); i++) {
        		node = curData.get(i);
        		annData = (AnnotationDataRecord) node.getUserObject();
        		annData.setBeginTime(begin + (start * perAnn));
        		
        		if (i != curData.size() - 1) {
        			annData.setEndTime(begin + ((start + 1) * perAnn));
        		}
        		// adjust child nodes
        		updateChildren(node);
        		start++;
        	}
    	}   	
    }
    
    private void checkChildNodes (DefaultMutableTreeNode node2, 
    		Map<String, MutableInt> changedTiers, boolean isInSecondNode) {
    	DefaultMutableTreeNode node = null;
    	AnnotationDataRecord annData = null;
    	TierImpl tier = null;
    	
    	for (int i = 0; i < node2.getChildCount(); i++) {
    		node = (DefaultMutableTreeNode) node2.getChildAt(i);
    		annData = (AnnotationDataRecord) node.getUserObject();
    		tier = (TierImpl) transcription.getTierWithId(annData.getTierName());
    		
    		if (tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) {
    			if (!changedTiers.containsKey(annData.getTierName())) {
    				if (isInSecondNode) {
    					changedTiers.put(annData.getTierName(), new MutableInt(1));
    				}
    			} else {
    				changedTiers.get(annData.getTierName()).intValue++;
    			}
    		} else {
    			// check the children
    			checkChildNodes(node, changedTiers, isInSecondNode);
    		}
    	}
    }
    
    
    /**
     * Updates child annotations of symbolic subdivision annotations, both symbolic association and
     * symbolic subdivisions.
     * 
     * @param node the node containing the symbolic subdivision annotation
     */
    private void updateChildren(DefaultMutableTreeNode node1) {
    	AnnotationDataRecord annData = (AnnotationDataRecord) node1.getUserObject();
    	long begin = annData.getBeginTime();
    	long end = annData.getEndTime();
    	DefaultMutableTreeNode node;
    	TierImpl tier = null;
    	Map<String, MutableInt> changedTiers = new HashMap<String, MutableInt>();
    	
    	for (int i = 0; i < node1.getChildCount(); i++) {
    		node = (DefaultMutableTreeNode) node1.getChildAt(i);
    		annData = (AnnotationDataRecord) node.getUserObject();
    		tier = (TierImpl) transcription.getTierWithId(annData.getTierName());
    		
    		if (tier.getLinguisticType().getConstraints() != null && 
    				tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) {
    			if (!changedTiers.containsKey(annData.getTierName())) {
    				changedTiers.put(annData.getTierName(), new MutableInt(1));
    			} else {
    				changedTiers.get(annData.getTierName()).intValue++;
    			}
    		} else if (tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
    			annData.setBeginTime(begin);
    			annData.setEndTime(end);
    		}
    	}
    	
    	if (changedTiers.size() == 0) {
    		return;
    	}
    	//HashSet set;
    	Iterator<String> tierIt = changedTiers.keySet().iterator();
    	List<DefaultMutableTreeNode> curData = new ArrayList<DefaultMutableTreeNode>(10);
    	String tierName = null;
    	
    	while (tierIt.hasNext()) {
    		curData.clear();
    		tierName = tierIt.next();
    		int totalCount = changedTiers.get(tierName).intValue;
    		int perAnn = (int) Math.ceil((end - begin) / (float)totalCount);
    		
        	for (int i = 0; i < node1.getChildCount(); i++) {
        		node = (DefaultMutableTreeNode) node1.getChildAt(i);
        		annData = (AnnotationDataRecord) node.getUserObject();
        		if (annData.getTierName().equals(tierName)) {
        			curData.add(node);
        		}
        	}
        	int start = totalCount - curData.size();
        	for (int i = 0; i < curData.size(); i++) {
        		node = curData.get(i);
        		annData = (AnnotationDataRecord) node.getUserObject();
        		annData.setBeginTime(begin + (start * perAnn));
        		
        		if (i != curData.size() - 1) {
        			annData.setEndTime(begin + ((start + 1) * perAnn));
        		} else {
        			annData.setEndTime(end);
        		}
        		// adjust child nodes
        		updateChildren(node);
        		start++;
        	}
    	} 
    }
}
