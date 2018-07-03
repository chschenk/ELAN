package mpi.eudico.client.annotator.interlinear;

import java.util.ArrayList;
import java.util.List;


/**
 * Combines a number of (blockwise wrapped) print blocks to a single record
 * block. This enables the application of some Shoebox/Toolbox specific
 * features  (e.g. ordering, grouping, empty lines etc.)
 *
 * @author Han Sloetjes
 */
public class ToolboxRecordBlock {
    private List<InterlinearTier> printTiers;

    /**
     * Creates a new ToolboxRecordBlock instance
     */
    public ToolboxRecordBlock() {
        super();
        printTiers = new ArrayList<InterlinearTier>();
    }

    /**
     * Returns a list of the InterlinearTiers in this block.
     * <p>
     * Some tiers are of class EmptyPrintTier, as added by addEmptyPrintTier() or
     * insertEmptyPrintTier().
     * This is a special case.
     * Don't call any of their methods.
     *
     * @return a list of the InterlinearTiers in this block
     */
    public List<InterlinearTier> getPrintTiers() {
        return printTiers;
    }

    /**
     * Adds a tier to the list.
     *
     * @param pt the tier!
     */
    public void addPrintTier(InterlinearTier pt) {
        printTiers.add(pt);
    }

    /**
     * Inserts a tier at the specified position.
     *
     * @param index the index in the list
     * @param pt the tier
     */
    public void insertPrintTier(int index, InterlinearTier pt) {
        printTiers.add(index, pt);
    }

    /**
     * Removes the tier from the list.
     *
     * @param pt the tier
     *
     * @return true if the tier was in the list and now removed, false
     *         otherwise
     */
    public boolean removePrintTier(InterlinearTier pt) {
        return printTiers.remove(pt);
    }

    /**
     * Returns the index of the last occurrence of the tier with specified
     * name.
     *
     * @param name the name of the tier
     *
     * @return the index, or -1 if not present
     */
    public int lastIndexOfTier(String name) {
        if ((name == null) || (printTiers.size() == 0)) {
            return -1;
        }

        Object tiObj;
        InterlinearTier pt = null;

        for (int i = printTiers.size() - 1; i >= 0; i--) {
            tiObj = printTiers.get(i);

            if (tiObj instanceof EmptyPrintTier) {
                continue;
            }

            pt = (InterlinearTier) tiObj;

            if (pt.getTierName().equals(name)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Adds an indicator (for the renderer) to add or insert an empty line.
     */
    public void addEmptyPrintTier() {
        printTiers.add(new EmptyPrintTier());
    }

    /**
     * Adds an indicator (for the renderer) to add or insert an empty line.
     *
     * @param index DOCUMENT ME!
     */
    public void insertEmptyPrintTier(int index) {
        printTiers.add(index, new EmptyPrintTier());
    }
}
