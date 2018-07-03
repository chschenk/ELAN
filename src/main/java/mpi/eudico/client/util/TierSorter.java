package mpi.eudico.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Utility class for sorting the tiers of a transcription. Note: could be
 * extended by returning a DefaultMutableTreeNode.
 *
 * @author Han Sloetjes
 * @version 1.0 apr 2005
 */
public class TierSorter {
    /** Holds value of the unsorted sorting property */
    public final int UNSORTED = 0;

    /** Holds value of the sort by hierarchy sorting property */
    public final int BY_HIERARCHY = 1;
    public final int BY_NAME = 2;

    /** Holds value of the sort by participant sorting property */
    public final int BY_PARTICIPANT = 3;

    /** Holds value of the sort by linguistic type sorting property */
    public final int BY_LINGUISTIC_TYPE = 4;
    public final int BY_ANNOTATOR = 5;
    public final int BY_LANGUAGE = 6;

    /** A constant for unspecified participant or linguistic type */
    private final String NOT_SPECIFIED = "not specified";
    private TranscriptionImpl transcription;

    /**
     * Creates a new TierSorter instance.
     *
     * @param transcription the transcription containing the tiers to sort
     */
    public TierSorter(TranscriptionImpl transcription) {
        this.transcription = transcription;
    }

    /**
     * Returns a sorted list of the tiers. The sorting algorithm is determined
     * by the specified sorting mode.
     *
     * @param mode the sorting mode, one of BY_HIERARCHY, BY_PARTICIPANT,
     *        BY_LINGUISTIC_TYPE or UNSORTED
     *
     * @return a List&lt;TierImpl>
     *
     * @see #sortTiers(int, List&lt;TierImpl>)
     */
    public List<TierImpl> sortTiers(int mode) {
        return sortTiers(mode, null);
    }

    /**
     * Returns a sorted list of the tiers. The sorting algorithm is determined
     * by the specified sorting mode and is further based on the ordering in
     * the specified tier list.
     *
     * @param mode the sorting mode, one of BY_HIERARCHY, BY_PARTICIPANT,
     *        BY_LINGUISTIC_TYPE, BY_* or UNSORTED
     * @param currentTierOrder a list of the 'current' or default ordering
     *
     * @return a List&lt;TierImpl>
     *
     * @see #sortTiers(int)
     */
    public List<TierImpl> sortTiers(final int mode, final List<TierImpl> currentTierOrder) {
        List<TierImpl> sortedTiers = new ArrayList<TierImpl>();

        // create a list based on the current preferred order
        List<TierImpl> tierList = null;

        if (currentTierOrder == null) {
            tierList = new ArrayList<TierImpl>();
        } else {
            tierList = new ArrayList<TierImpl>(currentTierOrder);
        }

        List<TierImpl> allTiers = transcription.getTiers();

        for (int i = 0; i < allTiers.size(); i++) {
            TierImpl tier = allTiers.get(i);

            if (!tierList.contains(tier)) {
                tierList.add(tier);
            }
        }

        TierImpl.ValueGetter getter = null;
        
        switch (mode) {
        case BY_HIERARCHY:

            Map<TierImpl, DefaultMutableTreeNode> nodes = new HashMap<TierImpl, DefaultMutableTreeNode>();
            DefaultMutableTreeNode sortedRootNode = new DefaultMutableTreeNode(
                    "Root");

            for (TierImpl tier : tierList) {
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(tier);

                nodes.put(tier, n);
            }

            for (TierImpl tier : tierList) {
                if (tier.getParentTier() == null) {
                    sortedRootNode.add(nodes.get(tier));
                } else {
                    nodes.get(tier.getParentTier()).add(nodes.get(tier));
                }
            }

            Enumeration nodeEnum = sortedRootNode.preorderEnumeration();

            // skip root
            nodeEnum.nextElement();

            while (nodeEnum.hasMoreElements()) {
                DefaultMutableTreeNode nextnode = (DefaultMutableTreeNode) nodeEnum.nextElement();
                sortedTiers.add((TierImpl) nextnode.getUserObject());
            }

            break;

        case BY_NAME:        	
        	getter = new TierImpl.NameGetter();
        	break;
        	
        case BY_PARTICIPANT:        	
        	getter = new TierImpl.ParticipantGetter();
            break;

        case BY_LINGUISTIC_TYPE:
        	getter = new TierImpl.LinguisticTypeNameGetter();
            break;

        case BY_ANNOTATOR:
        	getter = new TierImpl.AnnotatorGetter();
        	break;
        	
        case BY_LANGUAGE:
        	getter = new TierImpl.LanguageGetter();
        	break;
        	
        case UNSORTED:
        // fallthrough default order
        default:
            sortedTiers = tierList;
        }

        if (getter != null) {
            Map<String, List<TierImpl>> valueTable = new HashMap<String, List<TierImpl>>();
            List<String> names = new ArrayList<String>();

            for (int i = 0; i < tierList.size(); i++) {
                TierImpl tier = tierList.get(i);

                String value = getter.getSortValue(tier);

                if (value.isEmpty()) {
                    value = NOT_SPECIFIED;
                }

                if (valueTable.get(value) == null) {
                    ArrayList<TierImpl> list = new ArrayList<TierImpl>();
                    list.add(tier);
                    valueTable.put(value, list);
                    names.add(value);
                } else {
                    valueTable.get(value).add(tier);
                }
            }

            if (valueTable.size() > 0) {
            	// Is there any reason to *not* sort by the sort key value?
            	if (mode == BY_NAME) {
            		Collections.sort(names);
            	}
                for (String name : names) {
                    List<TierImpl> pList = valueTable.get(name);

                    for (TierImpl p : pList) {
                        sortedTiers.add(p);
                    }
                }
            }
        }

        /*
           try {
               for (int i = 0; i < sortedTiers.size(); i++) {
                   TierImpl t = (TierImpl) sortedTiers.get(i);
                   System.out.println("Index: " + i + " -- Tier: " + t.getName());
               }
           } catch (Exception e) {
           }
         */
        return sortedTiers;
    }
}
