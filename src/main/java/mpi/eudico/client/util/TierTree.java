package mpi.eudico.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Created on Apr 2, 2004
 * 
 * Create a tree from DefaultMutableTreeNodes which have
 * Tier names as String userObjects.
 *
 * @author Alexander Klassmann
 * @version Apr 2, 2004
 * @version Aug 2005 Identity removed
 */
public class TierTree {
    private DefaultMutableTreeNode[] nodes;
    List<TierImpl> tierVector;

    /**
     * Creates a new TierTree instance
     *
     * @param transcription DOCUMENT ME!
     * @param identity DOCUMENT ME!
     */
    public TierTree(TranscriptionImpl transcription) {
        Map<TierImpl, DefaultMutableTreeNode> tierNodes = new HashMap<TierImpl, DefaultMutableTreeNode>();

        tierVector = new ArrayList<TierImpl>(transcription.getTiers());
        nodes = new DefaultMutableTreeNode[tierVector.size() + 1];
        nodes[0] = new DefaultMutableTreeNode();

        for (int i = 0; i < tierVector.size(); i++) {
            TierImpl tier = tierVector.get(i);
            nodes[i + 1] = new DefaultMutableTreeNode(tier.getName());
            tierNodes.put(tier, nodes[i + 1]);
        }

        for (int i = 0; i < tierVector.size(); i++) {
            TierImpl tier = tierVector.get(i);

            if (tier.hasParentTier()) {
                DefaultMutableTreeNode parent = tierNodes.get(tier.getParentTier());
				if (parent != null) {
                    parent.add(nodes[i + 1]);
                }
            } else {
                nodes[0].add(nodes[i + 1]);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public DefaultMutableTreeNode getTree() {
        return nodes[0];
    }
    public int getNumberOfNodes() {
        return tierVector.size();
    }
    
    public DefaultMutableTreeNode sortAlphabetically() {
    	return performSorting(false);
    }

    public DefaultMutableTreeNode sortReverseAlphabetically() {
    	return performSorting(true);
    }
    
    private DefaultMutableTreeNode performSorting(Boolean reverse) {
    	if (tierVector.size() < 2) {
            return nodes[0];
    	}
        Boolean stillSorting = true;

        Map<TierImpl, DefaultMutableTreeNode> tierNodes = new HashMap<TierImpl, DefaultMutableTreeNode>();
        
        while (stillSorting) {
        
          for (int i = 0; i < tierVector.size() - 1; i++) {
            String tierName1 = tierVector.get(i).getName();
            String tierName2 = tierVector.get(i+1).getName();
            if (doSwap(tierName1, tierName2, reverse)) {
                // swap
                TierImpl tempTier = tierVector.get(i);
                DefaultMutableTreeNode tempTreeNode = nodes[i+1];
                tierVector.set(i,tierVector.get(i+1));
                tierVector.set(i+1,tempTier);
                nodes[i+1] = nodes[i+2];
                nodes[i+2] = tempTreeNode;
                stillSorting = true;
            break;
            } else {
                stillSorting = false;
            }
          }
          
        }
        
        // sorted
        for (int i = 0; i < tierVector.size() - 1; i++) {
            tierNodes.put(tierVector.get(i), nodes[i + 1]);
        }
                    
        nodes[0].removeAllChildren();
        
        for (int i = 0; i < tierVector.size(); i++) {
            TierImpl tier = tierVector.get(i);

            if (tier.hasParentTier()) {
                DefaultMutableTreeNode parent = tierNodes.get(tier.getParentTier());   // removed final
                if (parent != null) {
                    parent.add(nodes[i + 1]);
                }
            } else {
                nodes[0].add(nodes[i + 1]);
            }
        }
        return nodes[0];
    }
    
    private Boolean doSwap(String first, String second, Boolean reverse) {
    	if(reverse) {
    		if (first.compareTo(second) < 0) {
    			return true;
    		}
    	} else {
    		if (first.compareTo(second) > 0) {
    			return true;
    		}
    	}
    	return false;
    }
}
