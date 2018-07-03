package mpi.eudico.client.annotator.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;

/**
 * This class provides methods for storing annotations' state and recreation of
 * (deleted) annotations.<br>
 * It can: <br>
 * - create a dependency tree for annotations and store relevant data from
 * each annotation involved<br>
 * - recreate annotations (including dependent annotations) given the stored
 * information - handle conversion of annotations after a modification of
 * attributes of  a Linguistic Type  Note: preliminary
 *
 * @author Han Sloetjes
 * @version july 2004
 */
public class AnnotationRecreator {
    /** a logger */
    private static final Logger LOG = Logger.getLogger(AnnotationRecreator.class.getName());

    /**
     * Recreates annotations on Tiers with the specified LinguisticType.<br>
     * This operation can be neccessary when an other kind of Annotation type
     * is needed after a modification in the type, e.g. conversion of
     * AlignableAnnotations  into SVGAlignableAnnotations when the
     * LinguisticType has been changed  to allow graphic references.<br>
     * Be sure to lock the whole transcription while this operation is going
     * on!
     * <p>
     * Since SVGAlignableAnnotations don't exist any more,
     * this method is probably unused now.
     *
     * @param trans the Transcription
     * @param type the modified LinguisticType
     */
    private static void convertAnnotations(TranscriptionImpl trans,
        LinguisticType type) {
        if ((trans == null) || (type == null)) {
            return;
        }

        List<TierImpl> convertedTiers = new ArrayList<TierImpl>();
        List<TierImpl> tiersToConvert = null;

        tiersToConvert = trans.getTiersWithLinguisticType(type.getLinguisticTypeName());

        List<TierImpl> rootTiers = new ArrayList<TierImpl>();

        // put all root tiers at the beginning of the vector
        for (TierImpl t : tiersToConvert) {

            if (!t.hasParentTier()) {
                rootTiers.add(t);
            }
        }

        for (TierImpl t : tiersToConvert) {
            if (!rootTiers.contains(t)) {
                rootTiers.add(t);
            }
        }

        tiersToConvert = rootTiers;

        // convert all annotations on these tiers
        for (TierImpl curTier : tiersToConvert) {

            if (!convertedTiers.contains(curTier)) {
                AnnotationRecreator.convertAnnotations(trans, curTier);

                convertedTiers.add(curTier);

                List<TierImpl> depTiers = curTier.getDependentTiers();

                for (TierImpl depTier : depTiers) {
                    if (!convertedTiers.contains(depTier)) {
                        convertedTiers.add(depTier);
                    }
                }
            }
        }
    }

    /**
     * Recreates annotations on the specified Tier.<br>
     * This operation can be necessary when an other kind of Annotation type
     * is needed after a modification in the type, e.g. conversion of
     * AlignableAnnotations  into SVGAlignableAnnotations when the
     * LinguisticType has been changed  to allow graphic references.<br>
     * Be sure to lock the whole transcription while this operation is going
     * on!
     * <p>
     * Since SVGAlignableAnnotations don't exist any more,
     * this method is probably unused now.
     *
     * @param trans the Transcription
     * @param tier the modified LinguisticType
     */
    private static void convertAnnotations(Transcription trans, TierImpl tier) {
        if ((trans == null) || (tier == null)) {
            return;
        }

        List<AbstractAnnotation> annotations = tier.getAnnotations();

        List<DefaultMutableTreeNode> annTreeList = new ArrayList<DefaultMutableTreeNode>(annotations.size());

        // step 1: create data structures
        for (AbstractAnnotation absAnn : annotations) {
        	DefaultMutableTreeNode root = createTreeForAnnotation(absAnn);
            annTreeList.add(root);
        }

        // step 2: delete annotations
        for (AbstractAnnotation absAnn : annotations) {
            tier.removeAnnotation(absAnn);
        }

        // step 3: recreate annotations
        for (DefaultMutableTreeNode root : annTreeList) {
            AnnotationRecreator.createAnnotationFromTree(trans, root);
        }
    }

    /**
     * Creates a tree structure from one annotation.<br>
     * The specified annotation will be the root of the tree.  UNFINISHED!
     *
     * @param aa the annotation
     *
     * @return the root of the created tree
     */
    public static DefaultMutableTreeNode createTreeForAnnotation(
        AbstractAnnotation aa) {
        DefaultMutableTreeNode root = null;

        root = new DefaultMutableTreeNode(new AnnotationDataRecord(aa));

        List<Annotation> children = null;
        AbstractAnnotation next = null;
        AbstractAnnotation parent = null;
        DefaultMutableTreeNode nextNode = null;
        DefaultMutableTreeNode parentNode = root;
        String tierName = null;
        DefaultMutableTreeNode tempNode = null;
        AnnotationDataRecord dataRecord = null;

        children = aa.getParentListeners();

        if (children.size() > 0) {
downloop: 
            for (int i = 0; i < children.size(); i++) {
                next = (AbstractAnnotation) children.get(i);

                nextNode = createNodeForAnnotation(next);

                // children can come in any order

                if (parentNode.getChildCount() == 0) {
                    parentNode.add(nextNode);
                } else {
                    long bt = next.getBeginTimeBoundary();

                    for (int k = 0; k < parentNode.getChildCount(); k++) {
                        tempNode = (DefaultMutableTreeNode) parentNode.getChildAt(k);
                        dataRecord = (AnnotationDataRecord) tempNode.getUserObject();

                        tierName = next.getTier().getName();

                        if ((dataRecord.getBeginTime() > bt) &&
                                (tierName != null) &&
                                dataRecord.getTierName().equals(tierName)) {
                            parentNode.insert(nextNode, k);

                            break;
                        } else if (k == (parentNode.getChildCount() - 1)) {
                            parentNode.add(nextNode);
                        }
                    }
                }

                if (next.getParentListeners().size() > 0) {
                    children = next.getParentListeners();
                    parentNode = nextNode;
                    i = -1;

                    continue downloop;
                }

                if (i == (children.size() - 1)) {
uploop: 
                    while (true) {
                        parent = (AbstractAnnotation) next.getParentAnnotation();

                        if (parent != null) {
                            parentNode = (DefaultMutableTreeNode) nextNode.getParent();
                            children = parent.getParentListeners();

                            int j = children.indexOf(next);

                            if (j == (children.size() - 1)) {
                                if (parent == aa) {
                                    break downloop;
                                }

                                next = parent;
                                nextNode = parentNode;

                                continue uploop;
                            } else {
                                i = j;

                                continue downloop;
                            }
                        } else {
                            break downloop;
                        }
                    }
                }
            }
        }

        /*
           Enumeration en = root.depthFirstEnumeration();
           System.out.println("Depth First:\n");
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               AnnotationDataRecord rec = (AnnotationDataRecord) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
           System.out.println("Breadth First:\n");
           en = root.breadthFirstEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               AnnotationDataRecord rec = (AnnotationDataRecord) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
           System.out.println("Post Order:\n");
           en = root.postorderEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               AnnotationDataRecord rec = (AnnotationDataRecord) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
           System.out.println("Pre Order:\n");
           en = root.preorderEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               AnnotationDataRecord rec = (AnnotationDataRecord) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
         */
        return root;
    }

    /**
     * Creates a treenode without children from one annotation.<br>
     *
     * @param aa the annotation
     *
     * @return the root of the created treenode
     */
    public static DefaultMutableTreeNode createNodeForAnnotation(
        AbstractAnnotation aa) {
        DefaultMutableTreeNode node = null;

        node = new DefaultMutableTreeNode(new AnnotationDataRecord(aa));

        return node;
    }

    /**
     * Restore the common fields of an annotation from the AnnotationDataRecord.
     * These include the Value, the ExtRef and the CvEntryId.
     * @param aa the annotation to restore into (may be null)
     * @param annData
     * @param includeID also restore the annotation id?
     * @return whether the Annotation was != null. If it wasn't, a severe message is logged.
     */
    public static boolean restoreValueEtc(
    		AbstractAnnotation aa, 
    		AnnotationDataRecord annData,
    		boolean includeID) {
    	
        if (aa != null) {  
        	// Actually, I think these checks for null should not be done.
            if (annData.getExtRef() != null) {
            	aa.setExtRef(annData.getExtRef());
            } else if (aa.getExtRef() != null) {
            	if (LOG.isLoggable(Level.FINE)) {
            		LOG.fine(String.format("External reference not removed for annotation: Tier: %s, BT: %d, ET: %d", 
            				aa.getTier().getName(), aa.getBeginTimeBoundary(), aa.getEndTimeBoundary()));
            	}
            }
            
            if (annData.getCvEntryId() != null) {
            	// Dec 2015 don't set the cv entry id if the annotation (i.e. the tier it is on) is not 
            	// linked to a CV at all or if the CV does not contain an entry with the cv entry id
            	String cvName = aa.getTier().getLinguisticType().getControlledVocabularyName();
            	// March 2017 Check if the current CV is the same as the "original" CV, 
            	// if not, prevent setting the cv entry reference
            	if (cvName != null) {
	            	if (!aa.getTier().getName().equals(annData.getTierName())) {
	            		Tier otherTier = aa.getTier().getTranscription().getTierWithId(annData.getTierName());
	            		if (otherTier != null) {
	            			String otherCVName = otherTier.getLinguisticType().getControlledVocabularyName();
	            			if (!cvName.equals(otherCVName)) {
	            				cvName = null;// prevents setting the cv entry id ref
	            			}
	            		}
	            	}
            	}
            	
				if (cvName != null) {
					ControlledVocabulary cv = aa.getTier().getTranscription().getControlledVocabulary(cvName);
            		if (cv != null && cv.getEntrybyId(annData.getCvEntryId()) != null) {
            			aa.setCVEntryId(annData.getCvEntryId()); 
            		}          		
            	}                       	
            }
            
            if (includeID) {
            	aa.setId(annData.getId());
            }
            
            // Set the Value last, since it may trigger update events.
            aa.setValue(annData.getValue());

            return true;
        } else {
        	String type;
        	if (aa instanceof RefAnnotation) {
        		type = "Reference annotation";
        	} else if (aa instanceof AlignableAnnotation) {
        		type = "Alignable annotation";
        	} else {
        		type = "Annotation";
        	}
            LOG.severe(
                type + " could not be recreated: " +
                annData.getValue() + " bt: " +
                annData.getBeginTime() + " et: " +
                annData.getEndTime());
            
            return false;
        }
    }
    
   /**
     * (Re)creates an annotation without reproducing the annotation ID.
     * @param trans the transcription
     * @param root the root node
     * 
     * @return the created root annotation or null
     * @see #createAnnotationFromTree(Transcription, DefaultMutableTreeNode, boolean)
     */
    public static AbstractAnnotation createAnnotationFromTree(
            Transcription trans, DefaultMutableTreeNode root) {
    		return createAnnotationFromTree(trans, root, false);
    }
    
    /**
     * (Re)creates an annotation with all depending annotations from the
     * information  contained in the specified Node. <br>
     * Suitable for annotations on a root tier or any other tier where only
     * one  annotation has to be recreated.
     * The implementation has been combined with {@link mpi.eudico.client.annotator.tier.TierCopier#createRootAnnotationFromTree(Transcription, DefaultMutableTreeNode, Map)}
     * 
     * @param trans the Transcription to work on
     * @param root the rootnode containing the data objects for the annotations
     * @param includeID whether or not to restore the annotation id from the record,
     * the default is false
     *
     * @return the created (root) annotation or null
     */
    public static AbstractAnnotation createAnnotationFromTree(
            Transcription trans, DefaultMutableTreeNode root, boolean includeID) {
    	return createAnnotationFromTree(trans, root, includeID, null);
    }

    /**
     * Combined version of 
     * {@link #createAnnotationFromTree(Transcription, DefaultMutableTreeNode, boolean)}
     * and
     * {@link mpi.eudico.client.annotator.tier.TierCopier#createRootAnnotationFromTree(Transcription, DefaultMutableTreeNode, Map)}.
     */
    public static AbstractAnnotation createAnnotationFromTree(
        Transcription trans, DefaultMutableTreeNode root, boolean includeID, Map<String, String> tierMapping) {
        if ((trans == null) || (root == null)) {
            return null;
        }

        AbstractAnnotation annotation = null;
        DefaultMutableTreeNode node;
        AlignableAnnotation aa = null;
        RefAnnotation ra = null;
        Annotation an = null;

        /* create new annotations
         * iterate twice over the depending annotations: on the first
         * pass only Annotations with a time alignable begin time are
         * created, on the second pass the rest is done.
         */
        Enumeration/*<DefaultMutableTreeNode>*/ en = root.breadthFirstEnumeration();

        long begin;
        long end;
        AnnotationDataRecord annData = null;
        TierImpl tier = null;
        int linStereoType = -1;

        if (tierMapping != null) {
	        annData = (AnnotationDataRecord) root.getUserObject();
	        String tierName = tierMapping.get(annData.getTierName());
	        tier = (TierImpl) trans.getTierWithId(tierName);
	
	        if (tier == null) {
	            LOG.warning(
	                "Cannot recreate annotations: tier copy does not exist: " +
	                tierName);
	        }
        }

        while (en.hasMoreElements()) {
            aa = null; //reset
            node = (DefaultMutableTreeNode) en.nextElement();
            annData = (AnnotationDataRecord) node.getUserObject();

            if (tierMapping == null) {
            	tier = (TierImpl) trans.getTierWithId(annData.getTierName());
            } else {
                String tierName = tierMapping.get(annData.getTierName());
                tier = (TierImpl) trans.getTierWithId(tierName);            	
            }

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");
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
                    // if the annotation to create was the first in a series of unaligned annotations 
                    // on a time alignable tier, use createAnnotationBefore
                    if (tierMapping != null) {
                    	// The only reason we test explicitly for tierMapping is that the original
                    	// function just did this then-part. Its documentation says "assumes that 
                    	// the root annotation is created on a toplevel tier (no parent)".
                    	// So it is perfectly possible that for all uses of the original,
                    	// the test below is false and control would go to the final else-part,
                    	// which is identical to this then-part.
                    	aa = (AlignableAnnotation) tier.createAnnotation(begin, end);
                    } else
                    if (!annData.isEndTimeAligned() && tier.getLinguisticType().getConstraints() != null && 
                    		tier.getLinguisticType().getConstraints().supportsInsertion()) {
                    	AlignableAnnotation curAnn = (AlignableAnnotation) tier.getAnnotationAtTime(annData.getBeginTime());
                    	if (curAnn != null && curAnn.getBeginTimeBoundary() == annData.getBeginTime()) {
                    		aa = (AlignableAnnotation) tier.createAnnotationBefore(curAnn);
                    	} else {
                    		aa = (AlignableAnnotation) tier.createAnnotation(begin, end);
                    	}
                    } else {
                    	aa = (AlignableAnnotation) tier.createAnnotation(begin, end);
                    }

                    if (node == root) {
                        annotation = aa;
                    }

                    restoreValueEtc(aa, annData, includeID);   
                }
            } else {
                // non-alignable in second run
            }
        }

        // second run
        en = root.breadthFirstEnumeration();

        // for re-creation of unaligned annotation on Alignable (Time-Subdivision) tiers
        Annotation prevAnn = null;

        while (en.hasMoreElements()) {
            aa = null; //reset
            an = null;
            ra = null;
            node = (DefaultMutableTreeNode) en.nextElement();

            annData = (AnnotationDataRecord) node.getUserObject();

            if (tierMapping == null) {
            	tier = (TierImpl) trans.getTierWithId(annData.getTierName());
            } else {
                String tierName = tierMapping.get(annData.getTierName());
                tier = (TierImpl) trans.getTierWithId(tierName);            	
            }

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");
                continue;
            }

            if (tier.isTimeAlignable()) {
                if (!annData.isBeginTimeAligned()) {
                	String tierName = annData.getTierName();
                	if (tierMapping != null) {
                		tierName = tierMapping.get(tierName);
                	}
                    if ((prevAnn != null) &&
                            (!prevAnn.getTier().getName().equals(tierName) ||
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

                    if (node == root) {
                        annotation = aa;
                    }

                    restoreValueEtc(aa, annData, includeID);
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

                    //an = tier.getAnnotationAtTime(begin);
                	String tierName = annData.getTierName();
                	if (tierMapping != null) {
                		tierName = tierMapping.get(tierName);
                	}
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals(tierName)) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    if ((prevAnn != null) &&
                            (prevAnn.getEndTimeBoundary() < (begin + 1))) {
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

                    if (node == root) {
                        annotation = ra;
                    }

                    restoreValueEtc(ra, annData, includeID);
                } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;
                    an = tier.getAnnotationAtTime(begin);

                    if (an == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                    }

                    if (node == root) {
                        annotation = ra;
                    }

                    restoreValueEtc(ra, annData, includeID);
                }
            }
        }

        // end second run
        return annotation;
    }

	/**
	 * @see #createAnnotationsSequentially(Transcription, List&lt;DefaultMutableTreeNode>, boolean)
	 * @param trans the Transcription
	 * @param annotationsNodes an List containing a DefaultMutableTreeNode
	 *        for each annotation to recreate
	 */
    public static void createAnnotationsSequentially(Transcription trans,
        List<DefaultMutableTreeNode> annotationsNodes) {
    		createAnnotationsSequentially(trans, annotationsNodes, false);
    }
    
    /**
     * Creates a number of annotations with child-annotations in a sequence. <br>
     * Should handle the recreation of sequences of unaligned annotations on
     * either Time-Subdivision or Symbolic Subdivision tiers correctly.<br>
     * When annotations are only to be recreated on one tier (one level, no
     * child annotations) createAnnotationsSequentiallyDepthless can be used
     * instead.
     *
     * @param trans the Transcription to work on
     * @param annotationsNodes an List containing a DefaultMutableTreeNode
     *        for each annotation to recreate
     *
     * @see #createAnnotationsSequentiallyDepthless(Transcription, List)
     */
    public static void createAnnotationsSequentially(Transcription trans,
        List<DefaultMutableTreeNode> annotationsNodes, boolean includeId) {
        if ((trans == null) || (annotationsNodes == null) ||
                (annotationsNodes.size() == 0)) {
            return;
        }

        DefaultMutableTreeNode node;
        DefaultMutableTreeNode parentNode;
        AnnotationDataRecord annData = null;
        TierImpl tier = null;
        LinguisticType linType = null;

        // first recreate aligned annotations
        for (int i = 0; i < annotationsNodes.size(); i++) {
            node = annotationsNodes.get(i);
            annData = (AnnotationDataRecord) node.getUserObject();

            if (annData.isBeginTimeAligned()) {
                AnnotationRecreator.createAnnotationFromTree(trans, node, includeId);
            }
        }

        // next recreate the rest
        for (int i = 0; i < annotationsNodes.size(); i++) {
            node = annotationsNodes.get(i);
            annData = (AnnotationDataRecord) node.getUserObject();

            if (annData.isBeginTimeAligned()) {
                // we already had this one
                continue;
            }

            tier = (TierImpl) trans.getTierWithId(annData.getTierName());

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            linType = tier.getLinguisticType();

            if ((linType.getConstraints() != null) &&
                    (linType.getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)) {
                AnnotationRecreator.createAnnotationFromTree(trans, node, includeId);

                continue;
            }

            // tier is of a subdivision type...
            // count the number of unaligned annotations we have to create under a 
            // single parent annotation, horizontally....
            TierImpl parentTier = tier.getParentTier();
            AbstractAnnotation parentAnn = (AbstractAnnotation) parentTier.getAnnotationAtTime(annData.getBeginTime());

            if (parentAnn == null) {
                LOG.severe(
                    "Cannot recreate annotations: parent annotation does not exist.");

                continue;
            }

            parentNode = new DefaultMutableTreeNode("parent");
            parentNode.add(node);

            for (; i < annotationsNodes.size(); i++) {
                node = annotationsNodes.get(i);
                annData = (AnnotationDataRecord) node.getUserObject();

                if ((parentTier.getAnnotationAtTime(annData.getBeginTime()) == parentAnn) &&
                        !annData.isBeginTimeAligned()) {
                    parentNode.add(node);

                    if (i == (annotationsNodes.size() - 1)) {
                        AnnotationRecreator.createChildAnnotationsSkipRoot(trans,
                            parentNode, includeId);
                    }
                } else {
                    AnnotationRecreator.createChildAnnotationsSkipRoot(trans,
                        parentNode, includeId);
                    i--;

                    break;
                }
            }
        }
    }

    /**
     * Creates a number of unaligned child annotations that share the same
     * parent  annotation. Does not set the id of the annotations.
     *
     * @param trans the transcription
     * @param parentNode the parent node containing the nodes with the
     *        information necessary to recreate the child annotations
     *        @see #createChildAnnotationsSkipRoot(Transcription, DefaultMutableTreeNode, boolean)
     */
    static void createChildAnnotationsSkipRoot(Transcription trans,
        DefaultMutableTreeNode parentNode) {
    		createChildAnnotationsSkipRoot(trans, parentNode, false);
    }
    
    /**
     * Creates a number of unaligned child annotations that share the same
     * parent  annotation.
     * TODO: combine this with {@link mpi.eudico.client.annotator.tier.TierCopier#createTimeSubAnnotationsSkipRoot(Transcription, DefaultMutableTreeNode, Map)}
     *
     * @param trans the transcription
     * @param parentNode the parent node containing the nodes with the
     *        information necessary to recreate the child annotations
     * @param includeId whether or not to (re)store the annotation id
     */
    static void createChildAnnotationsSkipRoot(Transcription trans,
        DefaultMutableTreeNode parentNode, boolean includeId) {
        if ((trans == null) || (parentNode == null) ||
                (parentNode.getChildCount() == 0)) {
            return;
        }

        Annotation prevAnn = null;
        DefaultMutableTreeNode node;
        AlignableAnnotation aa = null;
        RefAnnotation ra = null;
        Annotation an = null;

        AnnotationDataRecord annData = null;
        TierImpl tier = null;

        long begin;
        int linStereoType = -1;

        Enumeration en = parentNode.breadthFirstEnumeration();

        // skip the empty root
        en.nextElement();

        while (en.hasMoreElements()) {
            aa = null; //reset
            an = null;
            ra = null;
            node = (DefaultMutableTreeNode) en.nextElement();

            annData = (AnnotationDataRecord) node.getUserObject();

            tier = (TierImpl) trans.getTierWithId(annData.getTierName());

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");

                continue;
            }

            if (tier.isTimeAlignable()) {
                if (!annData.isBeginTimeAligned()) {
                    if ((prevAnn != null) &&
                            (!prevAnn.getTier().getName().equals(annData.getTierName()) ||
                            (prevAnn.getEndTimeBoundary() <= annData.getBeginTime()))) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    if (prevAnn == null) {
                        begin = annData.getBeginTime();
                        an = tier.getAnnotationAtTime( /*annData.getEndTime() - 1*/
                                begin /*< end ? begin + 1 : begin*/);

                        if (an != null) {
                            aa = (AlignableAnnotation) tier.createAnnotationAfter(an);
                            prevAnn = aa;
                        } else {
                            // time subdivision of a time subdivision...
                            aa = (AlignableAnnotation) tier.createAnnotation(begin,
                                    annData.getEndTime());
                            prevAnn = aa;
                        }
                    } else {
                        aa = (AlignableAnnotation) tier.createAnnotationAfter(prevAnn);

                        prevAnn = aa;
                    }

                  	restoreValueEtc(aa, annData, includeId);
                } else {
                    //should not happen; all annotations should be unaligned in this case
                    prevAnn = null;
                }
            } else {
                // ref annotations
                linStereoType = tier.getLinguisticType().getConstraints()
                                    .getStereoType();

                if (linStereoType == Constraint.SYMBOLIC_SUBDIVISION) {
                    begin = annData.getBeginTime() /*+ 1*/;

                    //an = tier.getAnnotationAtTime(begin);
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals(annData.getTierName())) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    // should not be necessary here
                    if ((prevAnn != null) &&
                            (prevAnn.getEndTimeBoundary() < (begin + 1))) {
                        prevAnn = null;
                    }

                    if (prevAnn == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                        prevAnn = ra;
                    } else {
                        ra = (RefAnnotation) tier.createAnnotationAfter(prevAnn);
                        prevAnn = ra;
                    }

                    restoreValueEtc(ra, annData, includeId);
                } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;
                    an = tier.getAnnotationAtTime(begin);

                    if (an == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                    }

                    restoreValueEtc(ra, annData, includeId);
                }
            }
        }
    }
    
    /**
     * @see #createAnnotationsSequentiallyDepthless(Transcription, List, boolean)
     * @param trans the transcription
     * @param annotationsRecords the annotation records
     */
    public static void createAnnotationsSequentiallyDepthless(
        Transcription trans, List<List<AnnotationDataRecord>> annotationsRecords) {
    		createAnnotationsSequentiallyDepthless(trans, annotationsRecords, false);
    }

    /**
     * Creates a number of annotations in a sequence without creating child
     * annotations. <br>
     * Should handle the recreation of sequences of unaligned annotations on
     * either Time-Subdivision or Symbolic Subdivision tiers correctly.<br>
     * Depthless means that annotations are only created on one level, no
     * information on child annotations is to be expected (like a tree without
     * branches). The List does not contain DefaultMutableTreeNode
     * objects but Lists of AnnotationDataRecord objects instead; this
     * prevents the unnecessary creation  of DefaultMutableTreeNode objects,
     * as well as unnecessary checks on the existence of child annotations.
     * Useful for tokenizations.
     *
     * @param trans the Transcription to work on
     * @param annotationsRecords an List containing Lists of
     *        AnnotationDataRecord objects for each annotation to recreate,
     *        all 'siblings' grouped in one list
     * @param includeId whether or not to restore the annotation id. 
     * 
     * @see #createAnnotationsSequentially(Transcription, List)
     */
    public static void createAnnotationsSequentiallyDepthless(
	        Transcription trans,
	        List<List<AnnotationDataRecord>> annotationsRecords,
	        boolean includeId) {
        if ((trans == null) || (annotationsRecords == null) ||
                (annotationsRecords.size() == 0)) {
            return;
        }

        List<AnnotationDataRecord> siblingList = null;

        for (int i = 0; i < annotationsRecords.size(); i++) {
            siblingList = annotationsRecords.get(i);
            createSiblingAnnotations(trans, siblingList, includeId);
        }
    }

    /**
     * Creates a number of unaligned child annotations that share the same
     * parent annotation and without further child annotations (one level).
     *
     * @param trans the transcription
     * @param siblings the list containing the information necessary to
     *        recreate the child annotations
     * @param includeId whether to restore the annotation id
     */
    static void createSiblingAnnotations(
    		Transcription trans,
    		List<AnnotationDataRecord> siblings,
    		boolean includeId) {
        if ((trans == null) || (siblings == null) || (siblings.size() == 0)) {
            return;
        }

        Annotation prevAnn = null;
        AlignableAnnotation aa = null;
        RefAnnotation ra = null;
        Annotation an = null;

        AnnotationDataRecord annData = null;
        TierImpl tier = null;
        long begin;
        int linStereoType = -1;

        for (int i = 0; i < siblings.size(); i++) {
            aa = null; //reset
            an = null;
            ra = null;
            annData = siblings.get(i);

            // only get the tier once...
            if (tier == null) {
                tier = (TierImpl) trans.getTierWithId(annData.getTierName());
            }

            if (tier == null) {
                LOG.severe("Cannot recreate annotations: tier does not exist.");

                return;
            }

            if (tier.isTimeAlignable()) {
                if (annData.isBeginTimeAligned()) {
                    // only the first annotation
                    aa = (AlignableAnnotation) tier.createAnnotation(annData.getBeginTime(),
                            annData.getEndTime());
                    prevAnn = aa;
                } else {
                    if (prevAnn == null) {
                        begin = annData.getBeginTime();
                        an = tier.getAnnotationAtTime( /*annData.getEndTime() - 1*/
                                begin /*< end ? begin + 1 : begin*/);

                        if (an != null) {
                            aa = (AlignableAnnotation) tier.createAnnotationAfter(an);
                            prevAnn = aa;
                        }
                    } else {
                        aa = (AlignableAnnotation) tier.createAnnotationAfter(prevAnn);

                        prevAnn = aa;
                    }
                }

                restoreValueEtc(aa, annData, includeId);
            } else {
                // ref annotations					
                linStereoType = tier.getLinguisticType().getConstraints()
                                    .getStereoType();

                if (linStereoType == Constraint.SYMBOLIC_SUBDIVISION) {
                    begin = annData.getBeginTime() /*+ 1*/;

                    //an = tier.getAnnotationAtTime(begin);
                    if ((prevAnn != null) &&
                            !prevAnn.getTier().getName().equals(annData.getTierName())) {
                        // reset previous annotation field
                        prevAnn = null;
                    }

                    if ((prevAnn != null) &&
                            (prevAnn.getEndTimeBoundary() < (begin + 1))) {
                        prevAnn = null;
                    }

                    if (prevAnn == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                        prevAnn = ra;
                    } else {
                        ra = (RefAnnotation) tier.createAnnotationAfter(prevAnn);
                        prevAnn = ra;
                    }

                    restoreValueEtc(ra, annData, includeId);
                } else if (linStereoType == Constraint.SYMBOLIC_ASSOCIATION) {
                    begin = annData.getBeginTime() /*+ 1*/;
                    an = tier.getAnnotationAtTime(begin);

                    if (an == null) {
                        ra = (RefAnnotation) tier.createAnnotation(begin, begin);
                    }

                    restoreValueEtc(ra, annData, includeId);
                }
            }
        }
    }
}
