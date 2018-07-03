/*
 * Created on Sep 24, 2004
 *
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mpi.eudico.server.corpora.clom.Annotation;

/**
 * Metrics is a data container for storage and transfer of size and position
 * information. It is used by Interlinearizer during the generation of
 * interlinear views of Transcription objects.
 *
 * @author hennie
 */
public class Metrics {
    private TimeCodedTranscription transcription;
    private Interlinearizer interlinearizer;
    private Map<Annotation, Integer> sizeTable;
    private Map<Annotation, Integer> usedWidthTable;
    private Map<Annotation, Integer> horizontalPositions;
    private Map<Annotation, Integer> verticalPositions;
    private Map<String, Integer> tierHeights;
    private List<Annotation> blockWiseOrdered; // annotations sorted on hor. pos and tier hierarchy
    private List<Annotation> verticallyOrdered; // annotations sorted on vertical position
    private int leftMargin;
    private boolean leftMarginOn = false;

    /**
     * Creates a new Metrics instance
     *
     * @param tr DOCUMENT ME!
     * @param interlinearizer DOCUMENT ME!
     */
    public Metrics(TimeCodedTranscription tr, Interlinearizer interlinearizer) {
        transcription = tr;
        this.interlinearizer = interlinearizer;

        sizeTable = new HashMap<Annotation, Integer>();
        usedWidthTable = new HashMap<Annotation, Integer>();
        horizontalPositions = new HashMap<Annotation, Integer>();
        verticalPositions = new HashMap<Annotation, Integer>();
        tierHeights = new HashMap<String, Integer>();
    }

    /**
     * DOCUMENT ME!
     */
    public void reset() {
        sizeTable.clear();
        usedWidthTable.clear();
        horizontalPositions.clear();
        verticalPositions.clear();
        tierHeights.clear();

        blockWiseOrdered = null;
        verticallyOrdered = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param annot DOCUMENT ME!
     * @param size DOCUMENT ME!
     */
    public void setSize(Annotation annot, int size) {
        sizeTable.put(annot, Integer.valueOf(size));
    }

    /**
     * DOCUMENT ME!
     *
     * @param annot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getSize(Annotation annot) {
        int size = 0;

        Integer intSize = sizeTable.get(annot);

        if (intSize != null) {
            size = intSize.intValue();
        }

        return size;
    }

    /**
     * DOCUMENT ME!
     *
     * @param annot DOCUMENT ME!
     * @param width DOCUMENT ME!
     */
    public void setUsedWidth(Annotation annot, int width) {
        usedWidthTable.put(annot, Integer.valueOf(width));
    }

    /**
     * DOCUMENT ME!
     *
     * @param annot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getUsedWidth(Annotation annot) {
        int usedWidth = 0;

        Integer intWidth = usedWidthTable.get(annot);

        if (intWidth != null) {
            usedWidth = intWidth.intValue();
        }

        return usedWidth;
    }

    /**
     * DOCUMENT ME!
     *
     * @param annot DOCUMENT ME!
     * @param hPos DOCUMENT ME!
     */
    public void setHorizontalPosition(Annotation annot, int hPos) {
        horizontalPositions.put(annot, Integer.valueOf(hPos));
    }

    /**
     * DOCUMENT ME!
     *
     * @param annot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getHorizontalPosition(Annotation annot) {
        int hPos = 0;

        Integer intHPos = horizontalPositions.get(annot);

        if (intHPos != null) {
            hPos = intHPos.intValue();
        }

        return hPos;
    }

    /**
     * DOCUMENT ME!
     *
     * @param annot DOCUMENT ME!
     * @param vPos DOCUMENT ME!
     */
    public void setVerticalPosition(Annotation annot, int vPos) {
        verticalPositions.put(annot, Integer.valueOf(vPos));
    }

    /**
     * Sets initial (before any wrapping takes place) position of  annotation
     * on basis of tier heights.
     *
     * @param annot
     */
    public void setVerticalPosition(Annotation annot) {
        int vPos = 0;

        String tierName = annot.getTier().getName();
        String[] visibleTiers = getInterlinearizer().getVisibleTiers();

        for (int i = 0; i < visibleTiers.length; i++) {
            if (tierName.equals(visibleTiers[i])) {
                vPos += getTierHeight(visibleTiers[i]);

                break;
            } else {
                vPos += (getTierHeight(visibleTiers[i]) +
                getInterlinearizer().getLineSpacing());
            }
        }

        verticalPositions.put(annot, Integer.valueOf(vPos));
    }

    /**
     * DOCUMENT ME!
     *
     * @param annot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getVerticalPosition(Annotation annot) {
        int vPos = 0;

        Integer intVPos = verticalPositions.get(annot);

        if (intVPos != null) {
            vPos = intVPos.intValue();
        }

        return vPos;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMaxVerticalPosition() {
        int maxPosition = 0;

        Collection<Integer> c = verticalPositions.values();
        Iterator<Integer> cIter = c.iterator();

        while (cIter.hasNext()) {
            int vPos = cIter.next().intValue();

            if (vPos > maxPosition) {
                maxPosition = vPos;
            }
        }

        return maxPosition;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMaxHorizontallyUsedWidth() {
        int maxHUsed = 0;

        for (Annotation a : horizontalPositions.keySet()) {
            int hpos = horizontalPositions.get(a).intValue();
            int usedWidth = usedWidthTable.get(a).intValue();

            if ((hpos + usedWidth) > maxHUsed) {
                maxHUsed = hpos + usedWidth;
            }
        }

        return maxHUsed + getLeftMargin();
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     * @param tierHeight DOCUMENT ME!
     */
    public void setTierHeight(String tierName, int tierHeight) {
        tierHeights.put(tierName, Integer.valueOf(tierHeight));
    }

    /**
     * DOCUMENT ME!
     *
     * @param forTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getTierHeight(String forTier) {
        Integer i = tierHeights.get(forTier);

        if (i != null) {
            return i.intValue();
        } else {
            return 0;
        }
    }

    /**
     * Calculates total height of all visible tiers, plus potential additional
     * line spacing (total height of 'tier bundle').
     *
     * @return total height
     */
    public int getCumulativeTierHeights() {
        int totalHeight = 0;

        Collection<Integer> heights = tierHeights.values();
        Iterator<Integer> hIter = heights.iterator();

        while (hIter.hasNext()) {
            totalHeight += hIter.next().intValue();
        }

        int numOfVisibleTiers = getInterlinearizer().getVisibleTiers().length;
        totalHeight += (numOfVisibleTiers * getInterlinearizer().getLineSpacing());

        return totalHeight;
    }

    /**
     * Derives list of vertical (Integer) positions on basis of the vertical
     * positions of all annotations (after position and wrapping).
     *
     * @return List with Integers for unique vertical positions of tiers
     */
    public List<Integer> getPositionsOfNonEmptyTiers() {
        Collection<Integer> c = verticalPositions.values();

        return new ArrayList<Integer>(new HashSet<Integer>(c));
    }

    /**
     * DOCUMENT ME!
     *
     * @param position DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getTierLabelAt(int position) {
        String label = null;

        for (Annotation a : verticalPositions.keySet()) {
            if (verticalPositions.get(a).intValue() == position) {
                label = a.getTier().getName();

                break;
            }
        }

        return label;
    }

    /**
     * Returns vertical positions of every visible tier in the 'tier template':
     * all visible tier labels in the correct order, at the proper position.
     * This template can be repeated when blocks are wrapped. In that case
     * empty lines are also labeled.
     *
     * @return Array with vertical positions for every visible tier
     */
    public int[] getVPositionsInTemplate() {
        int[] vPositions = new int[getInterlinearizer().getVisibleTiers().length];
        int lineSpacing = getInterlinearizer().getLineSpacing();

        int positionInTemplate = 0;

        String[] vTierNames = getInterlinearizer().getVisibleTiers();

        for (int index = 0; index < vTierNames.length; index++) {
            int tierHeight = getTierHeight(vTierNames[index]);

            positionInTemplate += tierHeight;
            vPositions[index] = positionInTemplate;
            positionInTemplate += lineSpacing;
        }

        return vPositions;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TimeCodedTranscription getTranscription() {
        return transcription;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Interlinearizer getInterlinearizer() {
        return interlinearizer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public int getLeftMargin() {
        if (leftMarginOn) {
            return leftMargin;
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param i
     */
    public void setLeftMargin(int i) {
        if (leftMarginOn) {
            leftMargin = i;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param show DOCUMENT ME!
     */
    public void showLeftMargin(boolean show) {
        leftMarginOn = show;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean leftMarginShown() {
        return leftMarginOn;
    }

    /**
     * Generates and returns a sorted List of visible annotations. Sorting is
     * done on basis of left to right occurance in interlinear blocks:
     * horizontal position and position in tier hierarchy are used.
     *
     * @return sorted List with annotations
     */
    public List<Annotation> getBlockWiseOrdered() {
        if (blockWiseOrdered == null) { // calculate vector

            // assume that all annotations, visible and invisible, are sized
            blockWiseOrdered = new ArrayList<Annotation>();

            Set<Annotation> allAnnots = sizeTable.keySet();

            String[] visibleTiers = getInterlinearizer().getVisibleTiers();
            List<String> vTierList = Arrays.asList(visibleTiers);

            Iterator<Annotation> annIter = allAnnots.iterator();

            while (annIter.hasNext()) {
                Annotation a = annIter.next();

                if (vTierList.contains(a.getTier().getName())) {
                    blockWiseOrdered.add(a);
                }
            }

            Collections.sort(blockWiseOrdered, new AnnotationComparator());
        }

        return blockWiseOrdered;
    }

    /**
     * Generates and returns a sorted List of visible annotations. Sorting is
     * done on basis of vertical position.
     *
     * @return sorted List with annotations
     */
    public List<Annotation> getVerticallyOrdered() {
        if (verticallyOrdered == null) { // calculate vector

            // assume that all annotations, visible and invisible, are sized
            verticallyOrdered = new ArrayList<Annotation>();

            Set<Annotation> allAnnots = sizeTable.keySet();

            String[] visibleTiers = getInterlinearizer().getVisibleTiers();
            List<String> vTierList = Arrays.asList(visibleTiers);

            Iterator<Annotation> annIter = allAnnots.iterator();

            while (annIter.hasNext()) {
                Annotation a = annIter.next();

                if (vTierList.contains(a.getTier().getName())) {
                    verticallyOrdered.add(a);
                }
            }

            Collections.sort(verticallyOrdered, new AnnotComparatorOnVPos());
        }

        return verticallyOrdered;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pageHeight DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int[] getPageBoundaries(int pageHeight) {
        int[] pageBoundaries = null;
        int lastPageBreak = 0;

        List<Integer> boundaries = new ArrayList<Integer>();
        List<Integer> vPosIntegers = null;
        int[] tierLblVPositions = null;

        // find sorted int[] of vPositions of tier labels
        vPosIntegers = getPositionsOfNonEmptyTiers();
        Collections.sort(vPosIntegers);

        tierLblVPositions = new int[vPosIntegers.size()];

        for (int i = 0; i < vPosIntegers.size(); i++) {
            tierLblVPositions[i] = vPosIntegers.get(i).intValue();
        }

        // loop over tierLabel positions, find each next page break
        for (int k = 0; k < tierLblVPositions.length; k++) {
            if (tierLblVPositions[k] > (lastPageBreak + pageHeight)) { // next break passed

                if (k > 0) {
                    lastPageBreak = tierLblVPositions[k - 1];
                    boundaries.add(Integer.valueOf(lastPageBreak));
                }
            }
        }

        pageBoundaries = new int[boundaries.size()];

        for (int m = 0; m < boundaries.size(); m++) {
            pageBoundaries[m] = boundaries.get(m).intValue();
        }

        return pageBoundaries;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pageIndex DOCUMENT ME!
     * @param pageHeight DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int[] getPageBoundaries(int pageIndex, int pageHeight) {
        int pageCounter = 0;
        int lastPageBreak = 0;

        int[] boundaries = { 0, 0 };

        List<Integer> vPosIntegers = null;
        int[] tierLblVPositions = null;

        // find sorted int[] of vPositions of tier labels
        vPosIntegers = getPositionsOfNonEmptyTiers();

        Collections.sort(vPosIntegers);

        tierLblVPositions = new int[vPosIntegers.size()];

        for (int i = 0; i < vPosIntegers.size(); i++) {
            tierLblVPositions[i] = vPosIntegers.get(i).intValue();
        }

        // loop over tierLabel positions, find each next page break, count pages
        for (int k = 0; k < tierLblVPositions.length; k++) {
            if (tierLblVPositions[k] > (lastPageBreak + pageHeight)) { // next break passed

                if (pageCounter == pageIndex) { // right page found

                    break;
                } else {
                    if (k > 0) {
                        lastPageBreak = tierLblVPositions[k - 1];
                    }

                    pageCounter++;
                }
            }

            boundaries[0] = lastPageBreak;
            boundaries[1] = tierLblVPositions[k];
        }

        if (pageIndex > pageCounter) { // nothing on page pageIndex, terminate
            boundaries[0] = 0;
            boundaries[1] = 0;
        }

        return boundaries;
    }

    /**
     * DOCUMENT ME!
     * $Id: Metrics.java 43915 2015-06-10 09:02:42Z olasei $
     * @author $Author$
     * @version $Revision$
     */
    class AnnotationComparator implements Comparator<Annotation> {
        /**
         * Compares Annotations, first on basis of their horizontal position as
         * stored in horizontalPositions, then on basis of their position in
         * the tier hierarchy.
         *
         * @see java.util.Comparator#compare(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
		public int compare(Annotation a0, Annotation a1) {
            int hpos0 = horizontalPositions.get(a0).intValue();
            int hpos1 = horizontalPositions.get(a1).intValue();

            if (hpos0 < hpos1) {
                return -1;
            }

            if (hpos0 > hpos1) {
                return 1;
            }

            if (hpos0 == hpos1) {
                if (transcription.isAncestorOf(a1.getTier(), a0.getTier())) {
                    //	if (((TierImpl)a0.getTier()).hasAncestor((TierImpl)a1.getTier())) {
                    return 1;
                } else {
                    return -1;
                }
            }

            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     * $Id: Metrics.java 43915 2015-06-10 09:02:42Z olasei $
     * @author $Author$
     * @version $Revision$
     */
    class AnnotComparatorOnVPos implements Comparator<Annotation> {
        /**
         * Compares Annotations, on basis of vertical position
         *
         * @see java.util.Comparator#compare(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
		public int compare(Annotation a0, Annotation a1) {
            int vpos0 = verticalPositions.get(a0).intValue();
            int vpos1 = verticalPositions.get(a1).intValue();

            if (vpos0 < vpos1) {
                return -1;
            }

            if (vpos0 >= vpos1) {
                return 1;
            }

            return 0;
        }
    }
}
