package mpi.eudico.client.annotator.interlinear;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;


/**
 * A class that creates an AnnotationBlock for a given root annotation. The
 * created <code>InterlinearAnnotation</code> objects are stored and returned
 * in a tree structure. Annotations on invisible tiers are omitted, their
 * child annotations on visible tiers are added to first ancestor encountered.
 *
 * @author Han Sloetjes
 */
public class AnnotationBlockCreator {
    /**
     * Creates a new AnnotationBlockCreator instance.
     */
    public AnnotationBlockCreator() {
    }

    /**
     * Creates a tree with the specified annotation as the root, only including
     * block information for visible annotations. Annotations the parent of
     * which is on a invisble tier will be added to the first visible
     * ancestor.  When the tier the annotation 'aa' is on is not included in
     * the visible tiers array  it is checked for separately in the loops to
     * guarantee proper annotation ordering.
     *
     * @param aa the annotation to create a tree for
     * @param visibleTiers a list of the visible tiers (TierImpl objects)
     *
     * @return a DefaultMutableTreeNode; the root node containing the root
     *         annotation print record
     */
    public DefaultMutableTreeNode createBlockForAnnotation(
        AbstractAnnotation aa, List<Tier> visibleTiers) {
        if (aa == null) {
            return null;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new InterlinearAnnotation(
                    aa));

        List<Annotation> children = null;
        AbstractAnnotation next = null;
        AbstractAnnotation parent = null;
        DefaultMutableTreeNode nextNode = null;
        DefaultMutableTreeNode parentNode = root;
        TierImpl tier = null;
        String tierName = null;
        DefaultMutableTreeNode tempNode = null;
        InterlinearAnnotation dataRecord = null;

        children = aa.getParentListeners();

        if (children.size() > 0) {
downloop: 
            for (int i = 0; i < children.size(); i++) {
                next = (AbstractAnnotation) children.get(i);

                // children can come in any order
                tier = (TierImpl) next.getTier();

                if ((visibleTiers == null) || visibleTiers.contains(tier) ||
                        (tier == aa.getTier())) {
                    nextNode = new DefaultMutableTreeNode(new InterlinearAnnotation(
                                next));

                    if (parentNode.getChildCount() == 0) {
                        parentNode.add(nextNode);
                    } else {
                        long bt = next.getBeginTimeBoundary();

                        tierName = next.getTier().getName();

                        boolean inTierGroup = false;
                        int numChildren = parentNode.getChildCount();

                        for (int k = 0; k < numChildren; k++) {
                            tempNode = (DefaultMutableTreeNode) parentNode.getChildAt(k);
                            dataRecord = (InterlinearAnnotation) tempNode.getUserObject();

                            if (dataRecord.getTierName().equals(tierName)) {
                                inTierGroup = true;
                            }

                            if ((dataRecord.bt > bt) && inTierGroup) {
                                parentNode.insert(nextNode, k);

                                break;
                            } else if (inTierGroup &&
                                    !dataRecord.getTierName().equals(tierName)) {
                                // we passed the last ann of the right tier in the group of children
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
                } else {
                    if (next.getParentListeners().size() > 0) {
                        children = next.getParentListeners();

                        //parentNode = nextNode;
                        i = -1;

                        continue downloop;
                    }
                }

                if (i == (children.size() - 1)) {
uploop: 
                    while (true) {
                        parent = (AbstractAnnotation) next.getParentAnnotation();

                        if (parent != null) {
                            if ((visibleTiers == null) ||
                                    visibleTiers.contains(next.getTier()) ||
                                    (next.getTier() == aa.getTier())) {
                                if ((nextNode != null) &&
                                        (nextNode.getParent() != null)) {
                                    parentNode = (DefaultMutableTreeNode) nextNode.getParent();
                                } else {
                                    parentNode = root;
                                }
                            }

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
           //
           System.out.println("Depth First:\n");
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               InterlinearAnnotation rec = (InterlinearAnnotation) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
           System.out.println("Breadth First:\n");
           en = root.breadthFirstEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               InterlinearAnnotation rec = (InterlinearAnnotation) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
           System.out.println("Post Order:\n");
           en = root.postorderEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               InterlinearAnnotation rec = (InterlinearAnnotation) nextnode.getUserObject();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
           //
           System.out.println("Pre Order:\n");
           en = root.preorderEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               InterlinearAnnotation rec = (InterlinearAnnotation) nextnode.getUserObject();
                   System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
           }
           System.out.println("\n");
         */
        return root;
    }

    /**
     * Creates a tree with the specified annotation as the root, only including
     * block information for visible annotations. Annotations the parent of
     * which is on a invisble tier will be added to the first visible
     * ancestor.  When the tier the annotation 'aa' is on is not included in
     * the visible tiers array  it is checked for separately in the loops to
     * guarantee proper annotation ordering. Empty InterlinearAnnotations are added 
     * to dependent tiers where there is no child annotation at a position where there 
     * could be an annotation according to the parent-child tier relationship.
     *
     * @param aa the annotation to create a tree for
     * @param visibleTiers a list of the visible tiers (TierImpl objects)
     *
     * @return a DefaultMutableTreeNode; the root node containing the root
     *         annotation print record
     */
    public DefaultMutableTreeNode createBlockFillEmptyPositions(
            AbstractAnnotation aa, List<Tier> visibleTiers) {
            if (aa == null) {
                return null;
            }

            DefaultMutableTreeNode root = new DefaultMutableTreeNode(new InterlinearAnnotation(
                        aa));

            List<Annotation> children = null;
            AbstractAnnotation next = null;
            AbstractAnnotation parent = null;
            DefaultMutableTreeNode nextNode = null;
            DefaultMutableTreeNode parentNode = root;
            TierImpl tier = null;
            String tierName = null;
            DefaultMutableTreeNode tempNode = null;
            InterlinearAnnotation dataRecord = null;
            
            fillEmptyPositions(root, aa, visibleTiers);
            children = aa.getParentListeners();

            if (children.size() > 0) {
    downloop: 
                for (int i = 0; i < children.size(); i++) {
                    next = (AbstractAnnotation) children.get(i);

                    // children can come in any order
                    tier = (TierImpl) next.getTier();

                    if ((visibleTiers == null) || visibleTiers.contains(tier) ||
                            (tier == aa.getTier())) {
                        nextNode = new DefaultMutableTreeNode(new InterlinearAnnotation(
                                    next));
                        fillEmptyPositions(nextNode, next, visibleTiers);
                        
                        if (parentNode.getChildCount() == 0) {
                            parentNode.add(nextNode);
                        } else {
                            long bt = next.getBeginTimeBoundary();

                            tierName = next.getTier().getName();

                            boolean inTierGroup = false;
                            int numChildren = parentNode.getChildCount();

                            for (int k = 0; k < numChildren; k++) {
                                tempNode = (DefaultMutableTreeNode) parentNode.getChildAt(k);
                                dataRecord = (InterlinearAnnotation) tempNode.getUserObject();

                                if (dataRecord.getTierName().equals(tierName)) {
                                    inTierGroup = true;
                                }

                                if ((dataRecord.bt > bt) && inTierGroup) {
                                    parentNode.insert(nextNode, k);

                                    break;
                                } else if (inTierGroup &&
                                        !dataRecord.getTierName().equals(tierName)) {
                                    // we passed the last ann of the right tier in the group of children
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
                    } else {
                        fillEmptyPositions(root, next, visibleTiers);
                        
                        if (next.getParentListeners().size() > 0) {
                            children = next.getParentListeners();

                            //parentNode = nextNode;
                            i = -1;

                            continue downloop;
                        }
                    }

                    if (i == (children.size() - 1)) {
    uploop: 
                        while (true) {
                            parent = (AbstractAnnotation) next.getParentAnnotation();

                            if (parent != null) {
                                if ((visibleTiers == null) ||
                                        visibleTiers.contains(next.getTier()) ||
                                        (next.getTier() == aa.getTier())) {
                                    if ((nextNode != null) &&
                                            (nextNode.getParent() != null)) {
                                        parentNode = (DefaultMutableTreeNode) nextNode.getParent();
                                    } else {
                                        parentNode = root;
                                    }
                                }

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
            } else {
                // check if there are dependent tiers and recursively add empty int. annotations
            }

            /*
               Enumeration en = root.depthFirstEnumeration();
               //
               System.out.println("Depth First:\n");
               while (en.hasMoreElements()){
                   DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
                   InterlinearAnnotation rec = (InterlinearAnnotation) nextnode.getUserObject();
                   System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
               }
               System.out.println("\n");
               System.out.println("Breadth First:\n");
               en = root.breadthFirstEnumeration();
               while (en.hasMoreElements()){
                   DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
                   InterlinearAnnotation rec = (InterlinearAnnotation) nextnode.getUserObject();
                   System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
               }
               System.out.println("\n");
               System.out.println("Post Order:\n");
               en = root.postorderEnumeration();
               while (en.hasMoreElements()){
                   DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
                   InterlinearAnnotation rec = (InterlinearAnnotation) nextnode.getUserObject();
                   System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
               }
               System.out.println("\n");
               //
               System.out.println("Pre Order:\n");
               en = root.preorderEnumeration();
               while (en.hasMoreElements()){
                   DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
                   InterlinearAnnotation rec = (InterlinearAnnotation) nextnode.getUserObject();
                       System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + rec.getTierName() + " anndata: " + rec.getValue());
               }
               System.out.println("\n");
             */
            return root;
        }
    
    /**
     * Find so called empty slots and add empty InterlinearAnnotations, recursively.
     * 
     * @param root the current node
     * @param aa the Annotation
     * @param visibleTiers the list of visible tiers
     */
    private void fillEmptyPositions(DefaultMutableTreeNode root, Annotation aa, List<Tier> visibleTiers) {
        TierImpl tier = (TierImpl)aa.getTier();
        List<TierImpl> childTiers = tier.getChildTiers();
        int numCh = childTiers.size();
        if (numCh == 0) {
            return;
        }
        
        TierImpl ct;
        DefaultMutableTreeNode nextNode = null;
        for (int i = 0; i < numCh; i++) {
            ct = childTiers.get(i);
            
            if (visibleTiers == null || visibleTiers.contains(ct)) {
                // test if there are child annotations on this tier
	            if (aa.getChildrenOnTier(ct).size() == 0) {
	                int type = InterlinearAnnotation.ASSOCIATION;
	                if (ct.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN || 
	                        ct.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_SUBDIVISION ||
	                        ct.getLinguisticType().getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION) {
	                    type = InterlinearAnnotation.SUBDIVISION;
	                }
	                nextNode = new DefaultMutableTreeNode(new InterlinearAnnotation(ct.getName(), type));
	                root.add(nextNode);
	                // propagate to childtiers of ct
	                fillEmptyPositions(nextNode, ct, visibleTiers);
	            }
            } else {
                // the current tier is invisible but child tiers maybe visible?
                if (aa.getChildrenOnTier(ct).size() == 0) {
                    fillEmptyPositions(root, ct, visibleTiers);    
                }
            }
        }
    }
    
    /**
     * Add an empty InterlinearAnnotation for this tier to this node. It is already known that this position
     * is "empty".
     * 
     * @param root the current node
     * @param tier the tier to add the empty annotation to
     * @param visibleTiers the visible tiers
     */
    private void fillEmptyPositions(DefaultMutableTreeNode root, TierImpl tier, List<Tier> visibleTiers) {
        List<TierImpl> childTiers = tier.getChildTiers();
        int numCh = childTiers.size();
        if (numCh == 0) {
            return;
        }
        
        TierImpl ct;
        DefaultMutableTreeNode nextNode = null;
        for (int i = 0; i < numCh; i++) {
            ct = childTiers.get(i);
            
            if (visibleTiers == null || visibleTiers.contains(ct)) {
                
	            int type = InterlinearAnnotation.ASSOCIATION;
	            if (ct.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN || 
	                    ct.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_SUBDIVISION ||
	                    ct.getLinguisticType().getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION) {
	                type = InterlinearAnnotation.SUBDIVISION;
	            }
	            nextNode = new DefaultMutableTreeNode(new InterlinearAnnotation(ct.getName(), type));
	            root.add(nextNode);
	            // propagate to childtiers of ct
	            fillEmptyPositions(nextNode, ct, visibleTiers);
            } else {
                // the current tier is invisible but child tiers maybe visible?
                fillEmptyPositions(root, ct, visibleTiers);
            }
        }
    }
    
    /**
     * Creates a tree only containing the visible tiers. Of a tier's parent is
     * not visible the tier will be added to first ancestor encountered, going
     * up the tree.
     *
     * @param transcription the transcription
     * @param visibleTiers the visible tiers
     *
     * @return a tree with an 'empty' root.
     */
    public DefaultMutableTreeNode createTierTree(TranscriptionImpl transcription,
        List<Tier> visibleTiers) {
        DefaultMutableTreeNode root;

        if (transcription != null) {
            if (visibleTiers != null) {
                DefaultMutableTreeNode[] nodes;
                Map<TierImpl, DefaultMutableTreeNode> tierNodes = new HashMap<TierImpl, DefaultMutableTreeNode>();

                List<TierImpl> tierVector = transcription.getTiers();
                nodes = new DefaultMutableTreeNode[tierVector.size() + 1];
                nodes[0] = new DefaultMutableTreeNode();

                for (int i = 0; i < tierVector.size(); i++) {
                    TierImpl tier = tierVector.get(i);
                    nodes[i + 1] = new DefaultMutableTreeNode(tier.getName());
                    tierNodes.put(tier, nodes[i + 1]);
                }

                for (int i = 0; i < tierVector.size(); i++) {
                    TierImpl tier = tierVector.get(i);

                    if (visibleTiers.contains(tier)) {
                        if (tier.hasParentTier()) {
                            TierImpl parent = tier;

                            while (true) {
                                parent = parent.getParentTier();

                                if (parent == null) {
                                    nodes[0].add(nodes[i + 1]);

                                    break;
                                }

                                if (visibleTiers.contains(parent)) {
                                    if (tierNodes.get(
                                                parent) != null) {
                                        tierNodes.get(parent).add(nodes[i +
                                            1]);
                                    }

                                    break;
                                }
                            }
                        } else {
                            nodes[0].add(nodes[i + 1]);
                        }
                    }
                }

                root = nodes[0];
                root.setUserObject("Document");
            } else {
                root = new DefaultMutableTreeNode("Document");
            }
        } else {
            root = new DefaultMutableTreeNode("Document");
        }

        /*
           System.out.println("Pre Order Tier enumeration:\n");
           Enumeration en = root.preorderEnumeration();
           while (en.hasMoreElements()){
               DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode)en.nextElement();
               System.out.println("Level: " + nextnode.getLevel() + " -- Tier: " + nextnode.getUserObject());
           }
         */
        return root;
    }
}
