package mpi.eudico.client.annotator.interannotator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.tier.AnnotatorCompareUtil;
import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * Extends the AnnotatorCompareUtil class with a method that calculates an average 
 * for two lists (corresponding to two tiers) of AnnotationCore objects.
 *
 */
public class AnnotatorCompareUtil2 extends AnnotatorCompareUtil {

	public AnnotatorCompareUtil2() {
		super();
	}
	
	/**
	 * Calculates the average overlap / extent ratio for the entire combination of the 
	 * segments of two tiers. Doesn't store and preserve the ratio of individual combinations. 
	 * 
	 * @param compareCombi represents the annotations of two tiers
	 * @return the average quotient value
	 */
	public double getAverageRatio(CompareCombi compareCombi) {
		if (compareCombi == null) {
			return 0;
		}
		CompareUnit cu1 = compareCombi.getFirstUnit();
		CompareUnit cu2 = compareCombi.getSecondUnit();
		int numAnn1 = cu1.annotations.size();
		int numAnn2 = cu2.annotations.size();
		double totalRatio = 0.0d;
		int itemCount = 0;
		
		AnnotationCore aa1 = null;
		AnnotationCore aa2 = null;
        long bt1;
        long bt2;
        long et1;
        long et2;
        int i = 0; 

        int lastInserted2 = -1;
        List<AnnotationCore> overlapList = new ArrayList<AnnotationCore>(5);
        List<AnnotationCore> addedList2 = new ArrayList<AnnotationCore>(numAnn2);
        
        // first loop over the annotations of the first tier, process all aa1 annotations
        for (; i < numAnn1; i++) {
        	overlapList.clear();
            aa1 = cu1.annotations.get(i);
            bt1 = aa1.getBeginTimeBoundary();
            et1 = aa1.getEndTimeBoundary(); 
            
            // find all overlapping annotations on second tier
            for (int j = lastInserted2 + 1; j < numAnn2; j++) {
                aa2 = cu2.annotations.get(j);
                bt2 = aa2.getBeginTimeBoundary();
                et2 = aa2.getEndTimeBoundary();
                
                if (overlaps(bt1, et1, bt2, et2)) {
                	if (!addedList2.contains(aa2)) {
                		overlapList.add(aa2);
                	}
                } else if (bt2 > et1) {
                	lastInserted2 = j - 1 - overlapList.size();
                	break;
                }
            }
            
            if (overlapList.size() == 0) {
            	//totalRatio += 0;
            } else if (overlapList.size() == 1) {
            	aa2 = overlapList.get(0);
                bt2 = aa2.getBeginTimeBoundary();
                et2 = aa2.getEndTimeBoundary();
                long ov = calcOverlap(bt1, et1, bt2, et2);
                long te = calcExtent(bt1, et1, bt2, et2);
                totalRatio += ov/(double)te;
            	addedList2.add(aa2);
            } else {// more than 1, find the largest overlap
            	long lov = 0;
            	int indexLov = 0;
            	for (int j = 0; j < overlapList.size(); j++) {
            		aa2 = overlapList.get(j);
            		long ov = calcOverlap(bt1, et1, aa2.getBeginTimeBoundary(), aa2.getEndTimeBoundary());
            		if (ov > lov) {// with same overlap, pick the first one
            			lov = ov;
            			indexLov = j;
            		}
            	}
            	aa2 = overlapList.get(indexLov);
                bt2 = aa2.getBeginTimeBoundary();
                et2 = aa2.getEndTimeBoundary();
            	long te = calcExtent(bt1, et1, bt2, et2);
            	totalRatio += lov/(double)te;
            	addedList2.add(aa2);
            }
            
            itemCount++;
        }
        
        // now find the annotations on tier 2 that have not yet been added, process them
        for (int j = 0; j < numAnn2; j++) {
            aa2 = cu2.annotations.get(j);
            
            if (!addedList2.contains(aa2)) {
            	//totalRatio += 0;
            	itemCount++;
            }
        }
        
        if (itemCount != 0) {
        	return totalRatio / itemCount;
        }
        
		return 0;
	}
	
	/**
	 * Creates and returns a map of annotations with the largest overlap.
	 * Annotations not in the map don't have a counterpart in the other group
	 * 
	 * @param compareCombi the two lists of annotations
	 * @return a map of annotations with the largest overlap
	 */
	public Map<AnnotationCore, AnnotationCore> matchAnnotations(CompareCombi compareCombi) {
		if (compareCombi == null) {
			return null;
		}
		
		return matchAnnotations(compareCombi, 0.0);
	}
	
	/**
	 * Creates and returns a map of annotations with the largest overlap.
	 * Annotations not in the map don't have a counterpart in the other group
	 * 
	 * @param compareCombi the two lists of annotations
	 * @param minimalOverlapPercentage the minimal required overlap, the overlap as a percentage 
	 * of the largest interval 
	 * @return a map of annotations with the largest overlap
	 */
	public Map<AnnotationCore, AnnotationCore> matchAnnotations(CompareCombi compareCombi, 
			double minimalOverlapPercentage) {
		if (compareCombi == null) {
			return null;
		}

		CompareUnit cu1 = compareCombi.getFirstUnit();
		CompareUnit cu2 = compareCombi.getSecondUnit();
		int numAnn1 = cu1.annotations.size();
		int numAnn2 = cu2.annotations.size();
		
		AnnotationCore aa1 = null;
		AnnotationCore aa2 = null;
        long bt1;
        long bt2;
        long et1;
        long et2;
        
        int lastInserted2 = 0;
        List<OverlapPair> allOverlaps = new ArrayList<OverlapPair>();
        List<AnnotationCore> overlapList = new ArrayList<AnnotationCore>(5);
        
        // first loop over the annotations of the first tier, process all aa1 annotations
        for (int i = 0; i < numAnn1; i++) {
        	overlapList.clear();
            aa1 = cu1.annotations.get(i);
            bt1 = aa1.getBeginTimeBoundary();
            et1 = aa1.getEndTimeBoundary(); 

            // find all overlapping annotations on second tier
            for (int j = lastInserted2; j < numAnn2; j++) {
                aa2 = cu2.annotations.get(j);
                bt2 = aa2.getBeginTimeBoundary();
                et2 = aa2.getEndTimeBoundary();
                
                // the calculation of the overlaps boolean, the percentage and the raw overlap can 
                // probably be done more effectively
                if (overlaps(bt1, et1, bt2, et2)) { 
                	double percentage = calculateOverlapPercentage(bt1, et1, bt2, et2);
	                if (percentage >= minimalOverlapPercentage) {
	                	long overlap = this.calcOverlap(bt1, et1, bt2, et2);
	                	allOverlaps.add(new OverlapPair(aa1, aa2, percentage, overlap));
	                	overlapList.add(aa2);// to keep track of
                	}
                } else if (bt2 >= et1) {
                	lastInserted2 = j - 1 - overlapList.size();// too conservative maybe?
                	if (lastInserted2 < 0) {// make sure the index in the second list will not be -1
                		lastInserted2 = 0;
                	}
                	break;
                }
            }
        }
        
        // found all overlaps, find the optimal combination of overlapping annotations in the two lists,
        // based on:
        // 1. largest percentage overlap 
        // 2. in case an annotation overlaps two annotations with the same percentage, check the raw overlap
        // prefer the greatest
        // 3. process "left to right", if 1 and 2 are the same take the first
        
        Map<AnnotationCore, AnnotationCore> matchedAnnotations = new HashMap<AnnotationCore, AnnotationCore>();
        List<OverlapPair> curGroup = new ArrayList<OverlapPair>();
        
        // iterate back and forth to find the ideal match for each annotation while applying second best match to 
        // annotation already seen
        OverlapPair op1;
        OverlapPair op2;
        // or sort on percentage and move from bigger to the smaller overlaps
        Collections.sort(allOverlaps);
        // iterate and remove combinations one by one from the list??
        while (allOverlaps.size() > 0) {
        	op1 = allOverlaps.get(allOverlaps.size() - 1);
        	
        	if (op1.percentage == 1.0) {
        		// fully aligned
        		matchedAnnotations.put(op1.ac1, op1.ac2);
        		allOverlaps.remove(op1);
        		continue;
        	}
        	
        	if (matchedAnnotations.containsKey(op1.ac1) || matchedAnnotations.containsValue(op1.ac2)) {
        		// one of the annotations is already matched
        		allOverlaps.remove(op1);
        		continue;
        	}
        	// get all overlaps involving op.ac1 and op1.ac2 with the same overlap percentage
        	curGroup.clear();
        	curGroup.add(op1);
        	for (int i = allOverlaps.size() - 2; i >= 0; i--) {
        		op2 = allOverlaps.get(i);
        		if (op2.percentage == op1.percentage) {
        			if (!matchedAnnotations.containsKey(op2.ac1) && !matchedAnnotations.containsValue(op2.ac2)) {
	        			if (op2.ac1 == op1.ac1 || op2.ac2 == op1.ac2) {
	        				curGroup.add(op2);
	        			}
        			}
        		} else {
        			break;
        		}
        	}
        	// no ambiguities if there are no other combinations with the same percentage
        	if (curGroup.size() == 1) {
        		// only the current overlap is in the group
        		matchedAnnotations.put(op1.ac1, op1.ac2);
        		allOverlaps.remove(op1);
        		continue;
        	}
        	
        	OverlapPair bestCandidate = op1;
        	OverlapPair iterOP = null;
        	for (int k = curGroup.size() - 1; k >= 0; k--) {
        		iterOP = curGroup.get(k);
        		if (iterOP.rawOverlap > bestCandidate.rawOverlap) {
        			bestCandidate = iterOP;
        		} else if (iterOP.rawOverlap == bestCandidate.rawOverlap) {
        			// everything the same, check earliest start time
        			long minST = Math.min(iterOP.ac1.getBeginTimeBoundary(), iterOP.ac2.getBeginTimeBoundary());
        			if (minST < bestCandidate.ac1.getBeginTimeBoundary() || minST < bestCandidate.ac2.getBeginTimeBoundary()) {
        				bestCandidate = iterOP;
        			}
        		}
        	}
        	// only add the best candidate if it is op1? or add anyway?
        	//if (bestCandidate == op1) {
        		matchedAnnotations.put(op1.ac1, op1.ac2);
        	//}
        	
        	allOverlaps.remove(op1);
        }
       
		return matchedAnnotations;
	}
	
	
	/**
	 * Calculates the percentage of the overlap of two intervals of the longest of the two 
	 * intervals.  
	 * 
	 * @param bt1 begin of first interval
	 * @param et1 end of first interval
	 * @param bt2 begin of second interval
	 * @param et2 end of second interval
	 * 
	 * @return the value of the overlap as a percentage of the longest interval
	 */
	public double calculateOverlapPercentage(long bt1, long et1, long bt2, long et2) {
		return (Math.min(et1, et2) - Math.max(bt1, bt2)) / 
				(double) Math.max(et1 - bt1, et2 - bt2);
	}
	
	/**
	 * A class to hold two annotations/segments, the percentage of their overlap and 
	 * the raw overlap in milliseconds.
	 */
	private class OverlapPair implements Comparable<OverlapPair> {
		AnnotationCore ac1;
		AnnotationCore ac2;
		double percentage;
		long rawOverlap;
		
		public OverlapPair(AnnotationCore ac1, AnnotationCore ac2,
				double percentage, long rawOverlap) {
			super();
			this.ac1 = ac1;
			this.ac2 = ac2;
			this.percentage = percentage;
			this.rawOverlap = rawOverlap;
		}

		@Override
		public int compareTo(OverlapPair op) {
			if (op.percentage > percentage) {
				return -1;
			} 
			if (op.percentage < percentage) {
				return 1;
			} 
			if (op.rawOverlap > rawOverlap) {
				return -1;
			}
			if (op.rawOverlap < rawOverlap) {
				return 1;
			}
			
			return 0;
		}		
	}
}
