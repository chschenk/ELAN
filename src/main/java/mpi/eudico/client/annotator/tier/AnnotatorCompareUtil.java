package mpi.eudico.client.annotator.tier;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A utility that compares the segmentation on two tiers, calculates overlaps, 
 * total extent (merged) and the quotient of these values for each couple of two matching 
 * annotations and builds a table model.
 * The following algorithm is applied:
 * - iterate over the annotations of the first tier
 * - find the best matching annotation on the second tier
 * - if there are more than one matches use the one with the largest overlap
 * - exclude the matches on tier two from further matching
 * - write zero values for overlap, extent and quotient if there is no matching annotation 
 */
public class AnnotatorCompareUtil {
	
	public AnnotatorCompareUtil() {
		super();
	}

	public TableModel getComparisonTable(TranscriptionImpl transcription, String tier1, String tier2) {
		if (transcription == null || tier1 == null || tier2 == null) {
			return null;
		}
		TierImpl t1 = transcription.getTierWithId(tier1);
		TierImpl t2 = transcription.getTierWithId(tier2);
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
		// 2 x 3 cols (ann, bt, et), overlap, total extent, quotient
		DefaultTableModel model = new DefaultTableModel(0, 9);
		
		AbstractAnnotation aa1 = null;
		AbstractAnnotation aa2 = null;
        long bt1;
        long bt2;
        long et1;
        long et2;
        int i = 0; 
        //int j = 0;
        int lastInserted1 = 0;
        int lastInserted2 = -1;
        List<AbstractAnnotation> overlapList = new ArrayList<AbstractAnnotation>(5);
        List<AbstractAnnotation> addedList2 = new ArrayList<AbstractAnnotation>(numAnn2);
		
        // first loop over the annotations of the first tier, add all aa1 annotations
        for (; i < numAnn1; i++) {
        	overlapList.clear();
            aa1 = annList1.get(i);
            bt1 = aa1.getBeginTimeBoundary();
            et1 = aa1.getEndTimeBoundary();

            // find all overlapping annotations on second tier
            for (int j = lastInserted2 + 1; j < numAnn2; j++) {
            //for (int j = 0; j < numAnn2; j++) {
                aa2 = annList2.get(j);
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
            	model.addRow(new String[]{aa1.getValue(), String.valueOf(bt1), 
                	String.valueOf(et1), "-", "-", "-", "0", "0", "0.00"});
            } else if (overlapList.size() == 1) {
            	aa2 = overlapList.get(0);
                bt2 = aa2.getBeginTimeBoundary();
                et2 = aa2.getEndTimeBoundary();
                long ov = calcOverlap(bt1, et1, bt2, et2);
                long te = calcExtent(bt1, et1, bt2, et2);
            	model.addRow(new String[]{aa1.getValue(), String.valueOf(bt1), 
                    	String.valueOf(et1), 
                    	aa2.getValue(), String.valueOf(bt2), String.valueOf(et2), 
                    	String.valueOf(ov), String.valueOf(te), String.valueOf(ov/(float)te)});
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
            	model.addRow(new String[]{aa1.getValue(), String.valueOf(bt1), 
                    	String.valueOf(et1), 
                    	aa2.getValue(), String.valueOf(bt2), String.valueOf(et2), 
                    	String.valueOf(lov), String.valueOf(te), String.valueOf(lov/(float)te)});
            	addedList2.add(aa2);
            }
        }
        
        // now find the annotations on tier 2 that have not yet been added, insert in table model
        String val;
        for (int j = 0; j < numAnn2; j++) {
            aa2 = annList2.get(j);
            
            if (!addedList2.contains(aa2)) {
            	bt2 = aa2.getBeginTimeBoundary();
            	et2 = aa2.getEndTimeBoundary();
            	int index = 0;
            	// find index to insert
            	for (i = lastInserted1; i < model.getRowCount(); i++) {
            		val = (String) model.getValueAt(i, 4);
            		try {
            			bt1 = Long.parseLong(val);
                        if (bt1 > bt2) {
                        	index = i;
                        	lastInserted1 = i;
                        	break;
                        }
            		} catch (NumberFormatException nfe) {
            			
            		}
            	}
            	model.insertRow(index, new String[]{"-", "-", "-", aa2.getValue(), 
            			String.valueOf(bt2), String.valueOf(et2),  "0", "0", "0.00"});
            }
        }
        // set column headers
        model.setColumnIdentifiers(new String[]{tier1, 
        		ElanLocale.getString("Frame.GridFrame.ColumnBeginTime"),
        		ElanLocale.getString("Frame.GridFrame.ColumnEndTime"),
        		tier2, 
        		ElanLocale.getString("Frame.GridFrame.ColumnBeginTime"),
        		ElanLocale.getString("Frame.GridFrame.ColumnEndTime"),
        		ElanLocale.getString("CompareAnnotatorsDialog.Label.Overlap"),
        		ElanLocale.getString("CompareAnnotatorsDialog.Label.Extent"),
        		ElanLocale.getString("CompareAnnotatorsDialog.Label.Quotient")
        		});
        
		return model;
	}
	
	protected boolean overlaps(long bt1, long et1, long bt2, long et2) {
		return bt1 < et2 && et1 > bt2; // compare excluding et == bt		
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
	protected long calcOverlap(long bt1, long et1, long bt2, long et2) {
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
	protected long calcExtent(long bt1, long et1, long bt2, long et2) {
		return Math.max(et1, et2) - Math.min(bt1, bt2);
	}
}
