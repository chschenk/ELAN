package mpi.eudico.client.annotator.interannotator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class AnnotatorCompareTable {
	private float defOverlapThreshold = 0.60f;
	// no corresponding segment
	final private String NCS = "n.c.s.";
	
	public AnnotatorCompareTable() {
		super();
	}


	public TableModel getComparisonTable(TranscriptionImpl transcription, String tier1, String tier2, 
			float overlapThreshold) {
		if (transcription == null || tier1 == null || tier2 == null) {
			return null;
		}
		if (overlapThreshold > 0.5f && overlapThreshold <= 1.0f) {
			defOverlapThreshold = overlapThreshold;
		}
		
		TierImpl t1 = (TierImpl) transcription.getTierWithId(tier1);
		TierImpl t2 = (TierImpl) transcription.getTierWithId(tier2);
		if (!t1.isTimeAlignable()) {
			throw new IllegalArgumentException("First tier is not time alignable: " + tier1);
		}
		if (!t2.isTimeAlignable()) {
			throw new IllegalArgumentException("Second tier is not time alignable: " + tier2);
		}
		List<AbstractAnnotation> annList1 = new ArrayList<AbstractAnnotation>(t1.getAnnotations());
		List<AbstractAnnotation> annList2 = new ArrayList<AbstractAnnotation>(t2.getAnnotations());
		int numAnn1 = annList1.size();
		int numAnn2 = annList2.size();
		// extract all annotation values
		List<String> annValues = new ArrayList<String>(20);
		String value;
		AbstractAnnotation aa1 = null;
		AbstractAnnotation aa2 = null;
		
		for (int i = 0; i < numAnn1; i++) {
			aa1 = annList1.get(i);
			value = aa1.getValue();
			if (value != null && value.length() != 0) {
				if (!annValues.contains(value)) {
					annValues.add(value);
				}
			}
		}
		
		for (int i = 0; i < numAnn2; i++) {
			aa1 = annList2.get(i);
			value = aa1.getValue();
			if (value != null && value.length() != 0) {
				if (!annValues.contains(value)) {
					annValues.add(value);
				}
			}
		}
		annValues.add(NCS);
		int numCols = annValues.size();
		// columns are R1, rows are R2
		DefaultTableModel model = new DefaultTableModel(numCols, numCols + 1);// add one column for "row labels"
		int[][] combiArray = new int[numCols][numCols];// [rows][cols]
		
        long bt1;
        long bt2;
        long et1;
        long et2;
        int i = 0; 
        //int j = 0;
        //int lastInserted1 = 0;
        int lastInserted2 = -1;
        List<AbstractAnnotation> overlapList = new ArrayList<AbstractAnnotation>(5);
        List<AbstractAnnotation> addedList2 = new ArrayList<AbstractAnnotation>(numAnn2);
		
        // first loop over the annotations of the first tier, add all aa1 annotations
        for (; i < numAnn1; i++) {
        	overlapList.clear();
            aa1 = (AlignableAnnotation) annList1.get(i);
            bt1 = aa1.getBeginTimeBoundary();
            et1 = aa1.getEndTimeBoundary();

            // find all overlapping annotations on second tier
            for (int j = lastInserted2 + 1; j < numAnn2; j++) {
            //for (int j = 0; j < numAnn2; j++) {
                aa2 = (AlignableAnnotation) annList2.get(j);
                bt2 = aa2.getBeginTimeBoundary();
                et2 = aa2.getEndTimeBoundary();
                
                if (overlaps(bt1, et1, bt2, et2)) {
                	if (!addedList2.contains(aa2)) {
                		overlapList.add(aa2);
                	}
                }
                if (bt2 > et1) {
                	lastInserted2 = j - 1 - overlapList.size();
                	break;
                }
            }
            // TODO add a value to the segmentation only column
            if (overlapList.size() == 0) {// add ann1 to the table with the ncs value for r2
            	int col = annValues.indexOf(aa1.getValue());
            	if (col > -1) {
            		combiArray[numCols - 1][col]++; 
            	}
            } else if (overlapList.size() == 1) {
            	aa2 = (AlignableAnnotation) overlapList.get(0);
            	if (overlapGTThreshold(bt1, et1, aa2.getBeginTimeBoundary(), aa2.getEndTimeBoundary())) {
	            	int col = annValues.indexOf(aa1.getValue());
	            	int row = annValues.indexOf(aa2.getValue());
	            	if (col > -1 && row > -1) {
	            		combiArray[row][col]++;  
	            	}
	            	addedList2.add(aa2);
            	} else {// the overlap was too little, add ann1 with the ncs value for r2
                	int col = annValues.indexOf(aa1.getValue());
                	if (col > -1) {
                		combiArray[numCols - 1][col]++; 
                	}
            	}
            } else {// more than 1, find the largest overlap. There can only be one with overlap > threshold
            	long lov = 0;
            	int indexLov = 0;
            	for (int j = 0; j < overlapList.size(); j++) {
            		aa2 = (AlignableAnnotation) overlapList.get(j);
            		long ov = calcOverlap(bt1, et1, aa2.getBeginTimeBoundary(), aa2.getEndTimeBoundary());
            		if (ov > lov) {// with same overlap, pick the first one
            			lov = ov;
            			indexLov = j;
            		}
            	}
            	aa2 = (AlignableAnnotation) overlapList.get(indexLov);
                bt2 = aa2.getBeginTimeBoundary();
                et2 = aa2.getEndTimeBoundary();
                if (overlapGTThreshold(bt1, et1, bt2, et2)) {
	            	int col = annValues.indexOf(aa1.getValue());
	            	int row = annValues.indexOf(aa2.getValue());
	            	if (col > -1 && row > -1) {
	            		combiArray[row][col]++;
	            	}
                	addedList2.add(aa2);
                } else {// the overlap was too little, add ann1 with the ncs value for r2
                	int col = annValues.indexOf(aa1.getValue());
                	if (col > -1) {
                		combiArray[numCols - 1][col]++; //combiArray[col][numCols] = combiArray[col][numCols] + 1; 
                	}              	
                }
            }
        }
        
        // now find the annotations on tier 2 that have not yet been added, update the combination table
        String val;
        for (int j = 0; j < numAnn2; j++) {
            aa2 = (AlignableAnnotation) annList2.get(j);
            
            if (!addedList2.contains(aa2)) {
            	val = aa2.getValue();
            	int row = annValues.indexOf(val);
            	if (row > -1) {
            		combiArray[row][numCols - 1]++;
            	}
            }
        }
        // set column headers
//        model.setColumnIdentifiers(new String[]{tier1, 
//        		ElanLocale.getString("Frame.GridFrame.ColumnBeginTime"),
//        		ElanLocale.getString("Frame.GridFrame.ColumnEndTime"),
//        		tier2, 
//        		ElanLocale.getString("Frame.GridFrame.ColumnBeginTime"),
//        		ElanLocale.getString("Frame.GridFrame.ColumnEndTime"),
//        		ElanLocale.getString("CompareAnnotatorsDialog.Label.Overlap"),
//        		ElanLocale.getString("CompareAnnotatorsDialog.Label.Extent"),
//        		ElanLocale.getString("CompareAnnotatorsDialog.Label.Quotient")
//        		});
        List<String> headersList = new ArrayList<String>(numCols + 1);
        headersList.add("R1/R2");
        headersList.addAll(annValues);
        model.setColumnIdentifiers(headersList.toArray(new String[]{}));
        // add rows, including a "row header"
        for (int k = 0; k < numCols; k++) {
        	model.setValueAt(annValues.get(k), k, 0);// row header
        	for (int n = 0; n < numCols; n++) {
        		model.setValueAt(String.valueOf(combiArray[k][n]), k, n + 1);
        	}
        }
        
		return model;
	}
	
	private boolean overlaps(long bt1, long et1, long bt2, long et2) {
		return bt1 < et2 && et1 > bt2; // compare excluding et == bt		
	}
	
	private boolean overlapGTThreshold(long bt1, long et1, long bt2, long et2) {
		long dur1 = et1 - bt1;
		long dur2 = et2 - bt2;
		if (dur1 == 0 || dur2 == 0) {
			return false;
		}
		
		if (dur1 >= dur2) {
			return (dur2 / (float) dur1) >= defOverlapThreshold;
		} else {
			return (dur1 / (float) dur2) >= defOverlapThreshold;
		}		
	}
	
	/**
	 * Returns the overlap (logical AND) of 2 segments, i.e.
	 * smallest end time - largest begin time.
	 * @param bt1 first begin time 
	 * @param et1 first end time 
	 * @param bt2 second begin time
	 * @param et2 second end time
	 * @return the overlap (logical AND) of 2 segments
	 */
	private long calcOverlap(long bt1, long et1, long bt2, long et2) {
		return Math.min(et1, et2) - Math.max(bt1, bt2);
	}
	
	/**
	 * Returns the total extent (logical OR) of 2 segments, i.e.
	 * largest end time - smallest begin time.
	 * @param bt1 first begin time 
	 * @param et1 first end time 
	 * @param bt2 second begin time
	 * @param et2 second end time
	 * @return the total extent (logical OR) of 2 segments
	 */
	private long calcExtent(long bt1, long et1, long bt2, long et2) {
		return Math.max(et1, et2) - Math.min(bt1, bt2);
	}

	
}
