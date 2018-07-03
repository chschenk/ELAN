package mpi.eudico.client.annotator.interlinear;

import java.util.List;


/**
 * A class that represents a block of positioned printable, interlinear
 * annotations.  It is in fact a group of interlinear tiers, occupying an area
 * that spans from the left border to the right border of the output area and
 * that optionally includes a margin for tier labels.
 *
 * @author HS
 * @version 1.0
 */
public class InterlinearBlock {
    /**
     * indicates whether this print block is the start of a new annotation
     * block
     */
    private boolean startOfAnnotationBlock = false;
    private List<InterlinearTier> printTiers;
    private int occBlockWidth;

    /**
     * Creates a new InterlinearBlock instance
     *
     * @param printTiers a list of InterlinearTiers that can receive
     *        InterlinearAnnotations
     */
    public InterlinearBlock(List<InterlinearTier> printTiers) {
        this.printTiers = printTiers;
    }

    /**
     * Returns the InterlinearTier of the specified name.
     *
     * @param tierName the name of the tier
     *
     * @return the <code>InterlinearTier</code> for the specified tier
     */
    public InterlinearTier getPrintTier(String tierName) {
        InterlinearTier pt;

        for (int i = 0; i < printTiers.size(); i++) {
            pt = printTiers.get(i);

            if (pt.getTierName().equals(tierName)) {
                return pt;
            }
        }

        return null;
    }

    /**
     * Returns a list of the InterlinearTiers in this block.
     *
     * @return a list of the InterlinearTiers in this block
     */
    public List<InterlinearTier> getPrintTiers() {
        return printTiers;
    }

    /**
     * Returns the total numbers of lines on the tiers in this block.
     *
     * @return the total numbers of lines on the tiers in this block
     */
    public int getNumberOfLines() {
        int lines = 0;
        InterlinearTier pt;

        for (int i = 0; i < printTiers.size(); i++) {
            pt = printTiers.get(i);
            lines += pt.getNumLines();
        }

        return lines;
    }

    /**
     * Invokes the removal of tiers that contain no annotations.
     */
    public void removeEmptyTiers() {
        InterlinearTier pt;

        for (int i = printTiers.size() - 1; i >= 0; i--) {
            pt = printTiers.get(i);

            if (pt.getAnnotations().size() == 0) {
                printTiers.remove(i);
            } else {
                // if all annotations have empty strings as value, remove the tier
                InterlinearAnnotation ia = null;
                int size = pt.getAnnotations().size();
                boolean empty = true;

                for (int j = 0; j < size; j++) {
                    ia = pt.getAnnotations().get(j);

                    if (!ia.getValue().equals("")) {
                        empty = false;

                        break;
                    }
                }

                if (empty) {
                    printTiers.remove(i);
                }
            }
        }
    }

    /**
     * Removes the tiers that only contain empty annotations (value = "").
     */
    public void removeEmptySlotOnlyTiers() {
        InterlinearTier pt;
        InterlinearAnnotation ia = null;

        for (int i = printTiers.size() - 1; i >= 0; i--) {
            pt = printTiers.get(i);

            int size = pt.getAnnotations().size();
            boolean empty = true;

            for (int j = 0; j < size; j++) {
                ia = pt.getAnnotations().get(j);

                if (!ia.getValue().equals("")) {
                    empty = false;

                    break;
                }
            }

            if (empty) {
                printTiers.remove(i);
            }
        }
    }

    /**
     * Returns the width currently occupied by the annotations in the tiers.
     *
     * @return the width
     */
    public int getOccupiedBlockWidth() {
        return occBlockWidth;
    }

    /**
     * The occupied block width can be set explicitely from the outside world,
     * irrespective of the annotations present on the tiers.
     *
     * @param occBlockWidth the new block width
     */
    public void setOccupiedBlockWidth(int occBlockWidth) {
        this.occBlockWidth = occBlockWidth;

        for (int i = 0; i < printTiers.size(); i++) {
            printTiers.get(i).setPrintWidth(occBlockWidth);
        }
    }

    /**
     * Calculates the current max. occupied horizontal space by any annotation
     * on  any tier in the block.
     *
     * @return the current occupied horizontal space
     */
    public int calculateOccupiedBlockWidth() {
        int curWidth = 0;

        for (int i = 0; i < printTiers.size(); i++) {
            int tWidth = printTiers.get(i).getPrintAdvance();

            if (tWidth > curWidth) {
                curWidth = tWidth;
            }
        }

        return curWidth;
    }

    /**
     * Returna whether this print block is the start of a new annotation block.
     * Used for post-processing that might be necessary.
     *
     * @return true if this print block is the start of a new annotation block
     */
    public boolean isStartOfAnnotationBlock() {
        return startOfAnnotationBlock;
    }

    /**
     * Sets whether this print block is the start of a new annotation block.
     * False by default.
     *
     * @param startOfAnnotationBlock the new value for the
     *        startOfAnnotationBlock flag
     */
    public void setStartOfAnnotationBlock(boolean startOfAnnotationBlock) {
        this.startOfAnnotationBlock = startOfAnnotationBlock;
    }
}
