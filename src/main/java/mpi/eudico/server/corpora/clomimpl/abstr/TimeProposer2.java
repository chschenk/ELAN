package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeSlot;


/**
 * Calculates interpolated times for unaligned time slots without applying them
 * to the slots. Finds an Annotation for a given time based on these
 * interpolated times, so applies to unaligned annotations on a time-alignable
 * tier  (i.e. a Time Subdivision tier). Builds a tree of annotations, based
 * on annotation/time slot chains and recursively calculates interpolated time
 * values.
 *
 * @author Han Sloetjes
 */
public class TimeProposer2 {
    /**
     * Constructor
     */
    public TimeProposer2() {
        super();
    }

    /**
     * Finds the annotation for the specifed time on the specified tier. The
     * tier tree  only contains the tiers from the root tier to the target
     * tier. The root annotation is the root of the annotation tree that is
     * created for recursive  interpolation of unaligned annotations.
     *
     * @param tierTree the tier hierarchy, from root to target tier
     * @param rootAnn the root annotation, the annotation at the specified time
     *        on the root tier
     * @param targetTier the tier to find an annotation on at the specified
     *        time
     * @param time the time valeu to find an annotation for
     *
     * @return the annotation at the specified time (if necessary based on
     *         interpolated time values) or null
     */
    public Annotation getAnnotationAtTime(List<TierImpl> tierTree,
        AlignableAnnotation rootAnn, Tier targetTier, long time) {
        if ((tierTree == null) || (rootAnn == null) || (targetTier == null)) {
            return null;
        }

        // create an annotation tree based on time slot chains
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootAnn);
        addChildren(rootNode, rootAnn, tierTree);

        // calculate times for unaligned slots
        Map<TimeSlot, Long> slotsAndTimes = new HashMap<TimeSlot, Long>(10);
        calculateInterpolatedTimes(rootNode, slotsAndTimes);

        // finally find the right annotation
        Enumeration<DefaultMutableTreeNode> nodeEn = rootNode.postorderEnumeration();
        DefaultMutableTreeNode nn;
        AlignableAnnotation aa;
        long bb = 0;
        long ee = 0;

        while (nodeEn.hasMoreElements()) {
            nn = nodeEn.nextElement();
            aa = (AlignableAnnotation) nn.getUserObject();

            if (aa.getTier() != targetTier) {
                continue;
            }

            if (aa.getBegin().isTimeAligned()) {
                bb = aa.getBegin().getTime();
            } else {
                Long bL = slotsAndTimes.get(aa.getBegin());

                if (bL != null) {
                    bb = bL.longValue();
                } // else would be erroneous
            }

            if (aa.getEnd().isTimeAligned()) {
                ee = aa.getEnd().getTime();
            } else {
                Long eL = slotsAndTimes.get(aa.getEnd());

                if (eL != null) {
                    ee = eL.longValue();
                } // else would be erroneous
            }

            if ((bb <= time) && (time < ee)) {
                return aa;
            }
        }

        return null;
    }

    /**
     * Adds the children of the specified annotation that are part of the next
     * tier in the tree and that form a chain from the begin time slot to the
     * end time slot of the specified annotation. Recursive.
     *
     * @param node the node to add child nodes to
     * @param ann the annotation to find child annotations for
     * @param tierTree the tier tree
     */
    protected void addChildren(DefaultMutableTreeNode node,
        AlignableAnnotation ann, List<TierImpl> tierTree) {
        int index = tierTree.indexOf(ann.getTier());

        if (index < (tierTree.size() - 1)) {
            TierImpl nextTier = tierTree.get(index + 1);

            Iterator<Annotation> annIt = nextTier.annotations.iterator();
            AlignableAnnotation loopAnn;
            boolean beginFound = false;

            while (annIt.hasNext()) {
                loopAnn = (AlignableAnnotation) annIt.next();

                if (loopAnn.getBegin() == ann.getBegin()) {
                    beginFound = true;

                    DefaultMutableTreeNode nextNode = new DefaultMutableTreeNode(loopAnn);
                    node.add(nextNode);
                    addChildren(nextNode, loopAnn, tierTree);

                    continue;
                }

                if (beginFound) {
                    DefaultMutableTreeNode nextNode = new DefaultMutableTreeNode(loopAnn);
                    node.add(nextNode);
                    addChildren(nextNode, loopAnn, tierTree);

                    if (loopAnn.getEnd() == ann.getEnd()) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Calculates interpolated time values for time slots, starting at the top
     * level. Loops over all direct children of the specified node and
     * calculates interpolated times for each individual unaligned slot or
     * sequence of unaligned slots. Relies on the fact/assumption that for the
     * specified node/annotation there are either time aligned begin and/or
     * end slots or a mapping to an interpolated time value in the map for
     * begin and/or end slot.
     *
     * @param node the current node
     * @param slotsAndTimes the map containing the time slot - time value pairs
     */
    protected void calculateInterpolatedTimes(DefaultMutableTreeNode node,
        Map<TimeSlot, Long> slotsAndTimes) {
        int numCh = node.getChildCount();
        DefaultMutableTreeNode nn;
        AlignableAnnotation aa;
        TimeSlot ts;
        List<TimeSlot> uSlots = new ArrayList<TimeSlot>(5);

        long lastRealTime = 0;
        long curRealTime = 0;
        boolean inUnalignedChain = false;

        for (int i = 0; i < numCh; i++) {
            nn = (DefaultMutableTreeNode) node.getChildAt(i);
            aa = (AlignableAnnotation) nn.getUserObject();

            ts = aa.getBegin();

            if (ts.isTimeAligned()) {
                if (inUnalignedChain) {
                    // loop over unaligned ann's and calculate times
                    curRealTime = ts.getTime();

                    long span = curRealTime - lastRealTime;
                    long perSlot = span / (uSlots.size() + 1);

                    for (int j = 0; j < uSlots.size(); j++) {
                        slotsAndTimes.put(uSlots.get(j),
                            new Long(lastRealTime + ((j + 1) * perSlot)));

                        // System.out.println("aa1: " + aa.getValue() + " tt: " + (lastRealTime + ((j + 1) * perSlot)));
                    }

                    uSlots.clear();
                }

                inUnalignedChain = false;
                lastRealTime = ts.getTime();
            } else {
                // first try if there is a mapping in the map
                Long bt = slotsAndTimes.get(ts);

                if (bt != null) {
                    if (inUnalignedChain) {
                        // loop over unaligned ann's and calculate times can this occur for a begin slot?
                        curRealTime = bt.longValue();

                        long span = curRealTime - lastRealTime;
                        long perSlot = span / (uSlots.size() + 1);

                        for (int j = 0; j < uSlots.size(); j++) {
                            slotsAndTimes.put(uSlots.get(j),
                                new Long(lastRealTime + ((j + 1) * perSlot)));

                            // System.out.println("aa2: " + aa.getValue() + " tt: " + (lastRealTime + ((j + 1) * perSlot)));
                        }

                        uSlots.clear();
                    }

                    inUnalignedChain = false;
                    lastRealTime = bt.longValue();

                    //continue;
                } else {
                    if (!uSlots.contains(ts)) {
                        uSlots.add(ts);
                    }

                    if (!inUnalignedChain) { //??
                        inUnalignedChain = true;
                    }
                }
            }

            // inspect the end slot
            ts = (TimeSlotImpl) aa.getEnd();

            if (ts.isTimeAligned()) {
                if (inUnalignedChain) {
                    // loop over unaligned slots and calculate times
                    curRealTime = ts.getTime();

                    long span = curRealTime - lastRealTime;
                    long perSlot = span / (uSlots.size() + 1);

                    for (int j = 0; j < uSlots.size(); j++) {
                        slotsAndTimes.put(uSlots.get(j),
                            new Long(lastRealTime + ((j + 1) * perSlot)));

                        // System.out.println("aa3: " + aa.getValue() + " tt: " + (lastRealTime + ((j + 1) * perSlot)));
                    }

                    uSlots.clear();
                }

                inUnalignedChain = false;
                lastRealTime = ts.getTime();
            } else {
                // first try if there is a mapping in the map
                Long bt = slotsAndTimes.get(ts);

                if (bt != null) {
                    if (inUnalignedChain) {
                        // loop over unaligned ann's and calculate times
                        curRealTime = bt.longValue();

                        long span = curRealTime - lastRealTime;
                        long perSlot = span / (uSlots.size() + 1);

                        for (int j = 0; j < uSlots.size(); j++) {
                            slotsAndTimes.put(uSlots.get(j),
                                new Long(lastRealTime + ((j + 1) * perSlot)));

                            // System.out.println("aa4: " + aa.getValue() + " tt: " + (lastRealTime + ((j + 1) * perSlot)));
                        }

                        uSlots.clear();
                    }

                    inUnalignedChain = false;
                    lastRealTime = bt.longValue();
                } else {
                    if (!uSlots.contains(ts)) {
                        uSlots.add(ts);
                    }

                    if (!inUnalignedChain) { //??
                        inUnalignedChain = true;
                    }
                }
            }
        }

        // interpolate the children's children
        for (int i = 0; i < numCh; i++) {
            nn = (DefaultMutableTreeNode) node.getChildAt(i);
            calculateInterpolatedTimes(nn, slotsAndTimes);
        }
    }
}
