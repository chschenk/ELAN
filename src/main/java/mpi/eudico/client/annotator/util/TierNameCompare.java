package mpi.eudico.client.annotator.util;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Utility class that can (1) be used to detect a common prefix or suffix of two tier names
 * (2) update tier names in AnnotationDataRecords, based on a given delimiter and left or right
 * correspondence (part left of the delimiter is the same, or part right of the delimiter).  
 * 
 * @author Han Sloetjes
 */
public class TierNameCompare {
	/** The part after (the first occurrence of) a delimiter is equal.
	 *  Corresponds to KEEP_LEFT_PART in some operations. **/
	public static final int PREFIX_MODE = -1;
	/** The part before (the last occurrence of) a delimiter is equal 
	 *  Corresponds to KEEP_RIGHT_PART in some operations. **/
	public static final int SUFFIX_MODE = 1;
	/** determines that the left part (before a delimiter) should be used **/
	public static final int KEEP_LEFT_PART = -1;
	/** determines that the right part (after a delimiter) should be used **/
	public static final int KEEP_RIGHT_PART = 1;
	
	/**
	 * Constructor.
	 */
	public TierNameCompare() {
		super();
	}

    /**
     * Checks similarities between two tier names. 
     * @param tierName1 first tier name
     * @param tierName2 second tier name
     * @return an array of size 2: index 0 is the index of a delimiter character in the first name,
     * index 1 determines whether the begin (part before the delimiter) is the same (PREFIX_MODE), 
     * or the end (SUFFIX_MODE) (part after the delimiter)
     */
    public int[] findCorrespondingAffix(String tierName1, String tierName2) {
    	if (tierName1 == null || tierName2 == null) {
    		return null;
    	}
    	int[] indices = new int[]{-1, PREFIX_MODE};
    	char[] ch1 = tierName1.toCharArray();
    	char[] ch2 = tierName2.toCharArray();
    	int bi = -1, ei = -1;
    	int bi2 = -1, ei2 = -1;
    	for (int i = 0; i < ch1.length && i < ch2.length; i++) {
    		if (ch1[i] == ch2[i]) {
    			if (bi == -1) {
    				bi = i;
    				ei = i;
    			} else {
    				ei = i;
    			}
    		} else {
    			break;
    		}
    	}
    	
    	for (int i = ch1.length - 1, j = ch2.length - 1; i > -1 && j > -1; i--, j--) {
    		if (ch1[i] == ch2[j]) {
    			if (ei2 == -1) {
    				bi2 = i;
    				ei2 = j;
    			} else {
    				bi2 = i;
    			}
    		} else {
				break;
			}
    	}
    	// decide what is the prefix or suffix. If both seem to be present, prefer the longest,
    	// if equally long use prefix?
    	if (bi == 0 && ei > 0) {
    		if (bi2 > -1 && ch1.length - 1 - bi2 > ei -bi) {
        		indices[0] = bi2;// index of delimiter in first name
        		indices[1] = SUFFIX_MODE;// suffix equal
    		} else {
	    		indices[0] = ei;// index of delimiter
	    		indices[1] = PREFIX_MODE;// prefix equal
    		}
    	} else if (bi2 > -1 && ei2 > -1) {
    		indices[0] = bi2;
    		indices[1] = SUFFIX_MODE;// suffix equal
    	}
    	return indices;
    }

    /**
     * Replaces a prefix/suffix by a new prefix/suffix for all tiers in the tree. 
     * Assumes that already has been checked that "from" exists and is not the first or last character.
     * 
     * @param root the root of the annotation tree
     * @param newAffix the new prefix or suffix
     * @param from the last char before the suffix
     * @param leftOrRight if <= KEEP_LEFT_PART, the part left of the last "from" character should be maintained, 
     * otherwise the part right of the first "from" character should be maintained
     */
    public void adjustTierNames (DefaultMutableTreeNode root, String newAffix, char from, int leftOrRight) {
        if (root == null) {
            return;
        }
        DefaultMutableTreeNode n = null;
        AnnotationDataRecord adr;
        int index;
        Enumeration nodeIt = root.breadthFirstEnumeration();
        while (nodeIt.hasMoreElements()) {
            n = (DefaultMutableTreeNode) nodeIt.nextElement();
            adr = (AnnotationDataRecord) n.getUserObject();
            if (leftOrRight <= KEEP_LEFT_PART) {
	            index = adr.getTierName().lastIndexOf(from);
	            if (index > -1) {
	            	adr.setTierName(adr.getTierName().substring(0, index) + newAffix);
	            }
            } else {
            	index = adr.getTierName().indexOf(from);
	            if (index > -1) {
	            	adr.setTierName(newAffix + adr.getTierName().substring(index));
	            }
            }
        }
    }

    /**
     * Replaces a prefix/suffix by a new prefix/suffix for all tiers in the tree. 
     * Assumes that already has been checked that "from" exists and is not the first or last character.
     * 
     * @param root the root of the annotation tree
     * @param newAffix the new prefix or suffix
     * @param leftOrRight if <= KEEP_LEFT_PART, the affix is a suffix, otherwise it is a prefix
     */
    public void addAffixToTierNames (DefaultMutableTreeNode root, String newAffix, int leftOrRight) {
        if (root == null) {
            return;
        }
        DefaultMutableTreeNode n = null;
        AnnotationDataRecord adr;

        Enumeration nodeIt = root.breadthFirstEnumeration();
        while (nodeIt.hasMoreElements()) {
            n = (DefaultMutableTreeNode) nodeIt.nextElement();
            adr = (AnnotationDataRecord) n.getUserObject();
            if (leftOrRight <= KEEP_LEFT_PART) {
	            adr.setTierName(adr.getTierName() + newAffix);
            } else {
	            adr.setTierName(newAffix + adr.getTierName());
            }
        }
    }
}
