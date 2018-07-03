/*
 * Created on Sep 24, 2004
 *
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;


/**
 * Positioner contains utility methods to calculate horizontal and vertical
 * positions to be used to render annotations on a page. It deals with line
 * and blockwise wrapping, and takes empty slots (absent annotations) into
 * account.
 *
 * @author hennie
 */
public class Positioner {
    /**
     * DOCUMENT ME!
     *
     * @param metrics DOCUMENT ME!
     */
    public static void calcHorizontalPositions(Metrics metrics) {
        // Find all visible root annotations.
        // Sort them according to time order.
        // Iterate over them.
        // Position all children, taking empty slots into account.
        TimeCodedTranscription tr = metrics.getTranscription();
        String[] visibleTiers = metrics.getInterlinearizer().getVisibleTiers();
        List<String> vTierList = Arrays.asList(visibleTiers);

        List<Annotation> rootAnnotations = new ArrayList<Annotation>();
        Map<Tier, Integer> positionPerTier = new HashMap<Tier, Integer>();

        int hBlockOffset = 0; // horizontal offset per rootAnnotation

        List<TierImpl> topTiers = ((TranscriptionImpl)tr.getTranscription()).getTopTiers();

        for (TierImpl t : topTiers) {
            rootAnnotations.addAll(t.getAnnotations());
        }

        Collections.sort(rootAnnotations);

        Iterator<Annotation> annIter = rootAnnotations.iterator();

        while (annIter.hasNext()) {
            Annotation a = annIter.next();
            positionAnnotation(a, hBlockOffset, vTierList, metrics,
                positionPerTier);

            hBlockOffset += (metrics.getUsedWidth(a) +
            metrics.getInterlinearizer().getEmptySpace());
            positionPerTier.clear(); // reset	
        }
    }

    private static void positionAnnotation(Annotation a, int blockOffset,
        List<String> vTierList, Metrics metrics, Map<Tier, Integer> posPerTier) {
        int hPosition = 0;

        boolean annVisible = true;

        annVisible = vTierList.contains(a.getTier().getName());

        // set vertical position
        if (annVisible) {
            metrics.setVerticalPosition(a);
        }

        // set horizontal position.		
        Integer hPosInteger = posPerTier.get(a.getTier());

        if (hPosInteger != null) {
            hPosition = hPosInteger.intValue();
        }

        // To take empty slots into account:
        //   if a has parent, make sure posPerTier is after parent hPosition
        Annotation parentAnn = a.getParentAnnotation();

        if (parentAnn != null) {
            int parentHPos = metrics.getHorizontalPosition(parentAnn);

            if (parentHPos > (hPosition + blockOffset)) {
                hPosition = parentHPos - blockOffset;
            }
        }

        // set also for invisible annots, to pass alignment on to visible children
        metrics.setHorizontalPosition(a, blockOffset + hPosition);

        // calculate and store new horizontal position
        hPosition += (metrics.getUsedWidth(a) +
        metrics.getInterlinearizer().getEmptySpace());
        posPerTier.put(a.getTier(), Integer.valueOf(hPosition));

        // position children and empty spaces
        TimeCodedTranscription tr = metrics.getTranscription();
        List<? extends Annotation> childAnnots = null;

        childAnnots = tr.getChildAnnotationsOf(a);
        Collections.sort(childAnnots);

        for (Annotation child : childAnnots) {

            positionAnnotation(child, blockOffset, vTierList, metrics,
                posPerTier);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param metrics DOCUMENT ME!
     */
    public static void wrap(Metrics metrics) {
        int horWrap = 0; // horizontal component of wrap vector
        int vertWrap = 0; // vertical component of wrap vector

        int lastBlockStart = 0;
        int lastBlockIndex = 0;

        List<TierImpl> topTiers = ((TranscriptionImpl)metrics.getTranscription().getTranscription()).getTopTiers();

        boolean wrap = false;

        List<Annotation> orderedAnnots = metrics.getBlockWiseOrdered();

        for (int i = 0; i < orderedAnnots.size(); i++) {
            Annotation a = orderedAnnots.get(i);
            TierImpl t = (TierImpl) a.getTier();
            wrap = false;

            // only wrap on subdivision tiers, exclude top tiers...			
            if (topTiers.contains(t)) {
                lastBlockStart = metrics.getHorizontalPosition(a);
                lastBlockIndex = i;

                if ((metrics.getInterlinearizer().getBlockWrapStyle() == Interlinearizer.EACH_BLOCK) &&
                        (i > 0)) { // not first block
                    wrap = true;
                } else if ((metrics.getInterlinearizer().getBlockWrapStyle() == Interlinearizer.BLOCK_BOUNDARY) &&
                        ((metrics.getHorizontalPosition(a) +
                        metrics.getUsedWidth(a)) > (metrics.getInterlinearizer()
                                                               .getWidth() -
                        metrics.getLeftMargin()))) {
                    if (i > 0) {
                        wrap = true; // not first block		
                    }
                } else if ((metrics.getHorizontalPosition(a) // if page width not across a's value string
                         +metrics.getSize(a)) <= (metrics.getInterlinearizer()
                                                             .getWidth() -
                        metrics.getLeftMargin())) {
                    continue;
                }
            }

            // ...and symbolic associations of top tiers
            if (t.hasParentTier() && topTiers.contains(t.getParentTier()) &&
                    (t.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)) {
                continue;

                // if page width not across a's value string

                /*    if (    metrics.getHorizontalPosition(a)
                   + metrics.getSize(a) <=
                   metrics.getInterlinearizer().getWidth()
                   - metrics.getLeftMargin()) {
                
                       continue;
                   }    */
            }

            if (crossesPageWidth(a, metrics) || wrap) {
                // adjust wrap vector
                horWrap = -(metrics.getHorizontalPosition(a));
                vertWrap = metrics.getCumulativeTierHeights() +
                    metrics.getInterlinearizer().getBlockSpacing();

                // wrap annots starting right annotation
                // (keep root annots and their symb assocations together)
                int startAt = i;

                if (metrics.getHorizontalPosition(a) == lastBlockStart) {
                    startAt = lastBlockIndex;
                }

                wrap(metrics, horWrap, vertWrap, startAt);
                wrap = false;
            }
        }
    }

    private static boolean crossesPageWidth(Annotation a, Metrics metrics) {
        return (metrics.getHorizontalPosition(a) + metrics.getUsedWidth(a)) > (metrics.getInterlinearizer()
                                                                                      .getWidth() -
        metrics.getLeftMargin());
    }

    private static void wrap(Metrics metrics, int hWrap, int vWrap,
        int startingIndex) {
        List<Annotation> orderedAnnots = metrics.getBlockWiseOrdered();

        for (int i = startingIndex; i < orderedAnnots.size(); i++) {
            Annotation a = orderedAnnots.get(i);
            metrics.setHorizontalPosition(a,
                metrics.getHorizontalPosition(a) + hWrap);
            metrics.setVerticalPosition(a,
                metrics.getVerticalPosition(a) + vWrap);
        }
    }

    /**
     * Hides lines were no annotations are drawn by vertical repositioning. So
     * also be applicable after wrapping.
     *
     * @param metrics
     */
    public static void hideEmptyLines(Metrics metrics) {
        int currentVPos = 0;
        int currentVBlockBegin = 0;

        List<Integer> emptyLines = new ArrayList<Integer>();
        List<Integer> emptyLineHeights = new ArrayList<Integer>(); // parallel with 'emptyLines'

        int[] vPositionsInTemplate = metrics.getVPositionsInTemplate();
        int maxVerticalPosition = metrics.getMaxVerticalPosition();

        // get 'Set' with all annotation positions.
        // repeatedly go over vPositionsInTemplate until past maxVerticalPosition.
        // if position not in set of annotation positions, line is empty.
        // delete lines by subtracting tier's space from verticalPositions > position.
        List<Integer> annotPositions = metrics.getPositionsOfNonEmptyTiers();

        while (currentVPos < maxVerticalPosition) {
            for (int i = 0; i < vPositionsInTemplate.length; i++) {
                currentVPos = currentVBlockBegin + vPositionsInTemplate[i];

                Integer currentVPosInt = Integer.valueOf(currentVPos);

                if (!annotPositions.contains(currentVPosInt)) {
                    emptyLines.add(currentVPosInt);

                    int previousPos = 0;

                    if (i > 0) {
                        previousPos = vPositionsInTemplate[i - 1];
                    }

                    emptyLineHeights.add(Integer.valueOf(vPositionsInTemplate[i] -
                            previousPos));
                }
            }

            currentVBlockBegin = currentVPos +
                metrics.getInterlinearizer().getBlockSpacing() +
                metrics.getInterlinearizer().getLineSpacing();
        }

        // now delete empty lines
        // - get list of annots, sorted on vertical position
        // - iterate over them
        // - if 'next empty line' passed increase correction
        // - subtract correction from annots vertical position
        if (emptyLines.size() == 0) { // no empty lines, ready

            return;
        }

        Iterator<Integer> emptyLineIter = emptyLines.iterator();
        Iterator<Integer> lineHeightIter = emptyLineHeights.iterator();

        int nextEmptyLine = 0;

        if (emptyLineIter.hasNext()) {
            nextEmptyLine = emptyLineIter.next().intValue();
        }

        int nextLineHeight = 0;

        if (lineHeightIter.hasNext()) {
            nextLineHeight = lineHeightIter.next().intValue();
        }

        int correction = 0;

        List<Annotation> sortedAnnots = metrics.getVerticallyOrdered();

        for (Annotation a : sortedAnnots) {
            int vPos = metrics.getVerticalPosition(a);

            if (vPos > nextEmptyLine) {
                while (emptyLineIter.hasNext()) {
                    correction += nextLineHeight;

                    nextEmptyLine = emptyLineIter.next().intValue();
                    nextLineHeight = lineHeightIter.next().intValue();

                    if (vPos < nextEmptyLine) { // until all empty lines are 'eaten'

                        break;
                    }
                }
            }

            // apply correction
            if (correction > 0) {
                metrics.setVerticalPosition(a, vPos - correction);
            }
        }
    }
}
