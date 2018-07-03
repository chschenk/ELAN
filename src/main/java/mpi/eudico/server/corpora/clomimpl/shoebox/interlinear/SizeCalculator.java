/*
 * Created on Sep 24, 2004
 *
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * SizeCalculator contains utility methods to calculate how much horizontal
 * space  each visible annotation in a Transcription needs, both for
 * annotations by themselves, and in the context of interlinearized lines
 * (including additional empty space).
 *
 * @author hennie
 */
public class SizeCalculator {
    /**
     * Calculates how much horizontal space each individual visible annotation
     * occupies. Measured in units specified by Interlinearizer's
     * alignmentUnit parameter, in this case PIXELS
     *
     * @param metrics Stores and transfers (intermediate and final) results of
     *        interlinearizing
     * @param g DOCUMENT ME!
     */
    public static void calculateSizes(Metrics metrics, Graphics g) {
        TimeCodedTranscription tr = metrics.getTranscription();

        //	int alignmentUnit = metrics.getInterlinearizer().getAlignmentUnit();
        String[] visibleTiers = metrics.getInterlinearizer().getVisibleTiers();

        int size = 0;
        int maxTierLabelWidth = 0;

        List<String> vTierList = Arrays.asList(visibleTiers);

        // iterate over visible tiers, over annotations
        Iterator tierIter = tr.getTiers().iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            if (vTierList.contains(t.getName())) { // only visible tiers

                Font font = metrics.getInterlinearizer().getFont(t.getName());
                FontMetrics fontMetrics = g.getFontMetrics(font);

                int tierHeight = fontMetrics.getHeight();
                metrics.setTierHeight(t.getName(), tierHeight);

                for (Annotation a : t.getAnnotations()) {

                    size = fontMetrics.stringWidth(a.getValue().trim());

                    // store size in Metrics
                    metrics.setSize(a, size);
                }

                if (fontMetrics.stringWidth(t.getName()) > maxTierLabelWidth) {
                    maxTierLabelWidth = fontMetrics.stringWidth(t.getName());
                }
            }
        }

        metrics.setLeftMargin(maxTierLabelWidth + 10);
    }

    /**
     * Calculates how much horizontal space each individual visible annotation
     * occupies. Measured in units specified by Interlinearizer's
     * alignmentUnit parameter, in this case BYTES
     *
     * @param metrics Stores and transfers (intermediate and final) results of
     *        interlinearizing
     */
    public static void calculateSizes(Metrics metrics) {
        TimeCodedTranscription tr = metrics.getTranscription();
        String[] visibleTiers = metrics.getInterlinearizer().getVisibleTiers();

        int size = 0;
        //int maxTierLabelWidth = 0;

        List<String> vTierList = Arrays.asList(visibleTiers);

        // iterate over visible tiers, over annotations
        Iterator tierIter = tr.getTiers().iterator();

        while (tierIter.hasNext()) {
            TierImpl t = (TierImpl) tierIter.next();

            if (vTierList.contains(t.getName())) { // only visible tiers

                int charEncoding = metrics.getInterlinearizer().getCharEncoding(t.getName());
                metrics.setTierHeight(t.getName(), 1); // for bytes case, just to index lines

                for (Annotation a : t.getAnnotations()) {

                    if (charEncoding == Interlinearizer.UTF8) {
                        size = getNumOfBytes(a.getValue());
                    } else {
                        size = a.getValue().length(); // default 1 bytes per char
                    }

                    // store size in Metrics
                    metrics.setSize(a, size);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param utf8String DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static int getNumOfBytes(String utf8String) {
        int numOfBytes = 0;

        char[] chars = new char[utf8String.length()];
        utf8String.getChars(0, utf8String.length(), chars, 0);

        for (char ch : chars) {
            if ((ch == '\u0000') || ((ch >= '\u0080') && (ch <= '\u07ff'))) { // 2 bytes
                numOfBytes += 2;
            } else if ((ch >= '\u0800') && (ch <= '\uffff')) { // 3 bytes
                numOfBytes += 3;
            } else {
                numOfBytes += 1;
            }
        }

        return numOfBytes;
    }

    /**
     * Calculates occupied horizontal space for each visible annotation,
     * including empty space needed for interlinearization.
     *
     * @param metrics Stores and transfers results of interlinearizing
     */
    public static void calculateUsedWidths(Metrics metrics) {
        // algoritm: recursively calls getUsedWidth(annot).
        //
        // - get list of root annotations (==with no parent annot).
        // - iterate over these annots
        // - for each annot, 
        //     - get 'size' from Metrics
        //     - per immediate child tier, get child annots
        //     - recursively determine total usedWidth for those, including some empty space
        //     - find max of 'size' and total widths of child tiers, only taking visible
        //       tiers into account.
        //     - store each determined usedWidth in Metrics.
        List<Annotation> rootAnnotations = new ArrayList<Annotation>();

        TimeCodedTranscription tr = metrics.getTranscription();
        //int alignmentUnit = metrics.getInterlinearizer().getAlignmentUnit();

        // iterate over top tiers, over annotations
        List<TierImpl> topTiers = ((TranscriptionImpl)tr.getTranscription()).getTopTiers();

        for (TierImpl t : topTiers) {
            rootAnnotations.addAll(t.getAnnotations());
        }

        Iterator<Annotation> annotIter = rootAnnotations.iterator();

        while (annotIter.hasNext()) {
            // result is stored in Metrics as a side effect
            Annotation ann = annotIter.next();
            determineUsedWidth(ann, metrics);
        }
    }

    /**
     * Recursive method that calculates and stores used width for an annotation
     * in an interlinear layout. Result includes necessary empty space for
     * alignment.
     *
     * @param ann
     * @param metrics used to pass maximum width down the recursion tree in
     *        case parents are wider
     *
     * @return used width for annotation in number of alignmentUnits (pixels,
     *         bytes,...)
     */
    private static int determineUsedWidth(Annotation ann, Metrics metrics) {
        // - get 'size' from Metrics
        // - per immediate child tier, get child annots
        // - recursively determine total usedWidth for those, including some empty space
        // - find max of 'size' and total widths of child tiers, only taking visible
        //   tiers into account.
        // - store each determined usedWidth in Metrics.
        TimeCodedTranscription tr = metrics.getTranscription();

        int maxUsedWidth = metrics.getSize(ann); // if invisible, size == 0
        int annWidth = maxUsedWidth;
        int usedWidth = 0;
        Map<Tier, Integer> usedPerTier = new HashMap<Tier, Integer>();
        Map<Tier, Annotation> lastChildPerTier = new HashMap<Tier, Annotation>();

        List<? extends Annotation> childAnnots = tr.getChildAnnotationsOf(ann);

        for (Annotation child : childAnnots) {
            usedWidth = determineUsedWidth(child, metrics);

            Integer currWidthForTier = usedPerTier.get(child.getTier());

            if (currWidthForTier != null) {
                // TODO: substitute 10 with proper amount of empty space!!!
                usedPerTier.put(child.getTier(),
                	Integer.valueOf(currWidthForTier.intValue() + usedWidth +
                        metrics.getInterlinearizer().getEmptySpace()));
            } else {
                usedPerTier.put(child.getTier(), Integer.valueOf(usedWidth));
            }

            // store last child on each tier
            Annotation lastOnTier = lastChildPerTier.get(child.getTier());

            if (lastOnTier != null) {
                if (child.compareTo(lastOnTier) > 0) {
                    lastChildPerTier.put(child.getTier(), child);
                }
            } else { // lastOnTier not yet set
                lastChildPerTier.put(child.getTier(), child);
            }
        }

        Collection<Integer> sizes = usedPerTier.values();
        Iterator<Integer> sizePerTierIter = sizes.iterator();

        while (sizePerTierIter.hasNext()) {
            int sizePerTier = sizePerTierIter.next().intValue();

            if (sizePerTier > maxUsedWidth) {
                maxUsedWidth = sizePerTier;
            }
        }

        // if maxUsedWidth determined by ann itself, propagate this down the tree again.
        // algoritm: add extra width to last of ann's children on each tier.
        if (maxUsedWidth == annWidth) {
           	for (Tier t : usedPerTier.keySet()) {
                int widthToBeAdded = maxUsedWidth -
                    usedPerTier.get(t).intValue();

                if (widthToBeAdded > 0) {
                    Annotation lastAnn = lastChildPerTier.get(t);
                    metrics.setUsedWidth(lastAnn,
                        metrics.getUsedWidth(lastAnn) + widthToBeAdded);
                }
            }
        }

        // store maxUsedWidth in Metrics
        metrics.setUsedWidth(ann, maxUsedWidth);

        return maxUsedWidth;
    }
}
