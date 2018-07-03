package mpi.eudico.client.annotator.tier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.AnnotationValuesRecord;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Command that merges the annotations on selected tiers and creates new
 * annotations on a third tier. Overlapping annotations are merged into one
 * new annotation. Non overlapping annotations are copied as is. The content
 * depends  on the user's choice
 *
 * @author aarsom
 * @version 1.0 Feb, 2014
 */


/**
 * A command that merges the annotations selected tiers and creates new
 * annotations on a third tier. Overlapping annotations are merged into one
 * new annotation. Non overlapping annotations are copied as is. The content
 * depends  on the user's choice
 * 
 * @author aarsom aarsom
 * @version Feb, 2014
 */
public class MergeTiers extends AnnotationFromOverlaps {
	private boolean overlapsOnly;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public MergeTiers(String name) {
    	super(name);
    }
    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are correct.<br>
     *
     * @param receiver the TranscriptionImpl
     * @param arguments the arguments: <ul>
     * 			<li>arg[0] = the selected tiers (String[])</li> 
     * 			<li>arg[1] = the new tier name (String)</li> 
     *        	<li>arg[2] = the linguistic type of the new tier (String)</li>
     *        	<li>arg[3] = the kind of value the new tier should have (Integer)<ul>
     *        					<li>0 = time format (then it uses arg[4])
     *        					<li>1 = specific value (then it uses arg[5])
     *        					<li>2 = value from another tier (then it used arg[6]) 
     *        					<li>3 = concat values from tiers based on the annotation time
     *        					<li>4 = concat values from tiers based on the preferred tier order (then it uses arg[7]) </li></ul></li>
     *        	<li>arg[4] = the time format to use (String)</li> 
     *        	<li>arg[5] = the specific value to use (String)</li>
     *        	<li>arg[6] = the specific value from a tier to use (String)</li>
     *         	<li>arg[7] = tier order in which the values will be concatenated (String)</li>
     *        	<li>arg[8] = the tier name of which the values should be taken (String)</li>
     *        	<li>arg[9] = the overlaps criteria (0 = regardless of the value, 1 = same value, 2 = different value, 3 = based on constraints) (List<String[]>)
     *        	<li>arg[10] = create overlaps on currently opened transcription (0 = current transcription, 1 = based on files in arg[10]) (Integer)
     *        	<li>arg[11] = names of the files to create annotations for (String[])
     *        	<li>arg[12] = boolean value indicating if pal mode should be used (value is true) or NTSC (value is false). Only needed when time format is SMPTE.
     *        	<li>arg[13] = boolean value indicating if only the overlapping annotation has to be processed</li>
     *        </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
    	report.setName(ElanLocale.getString("MergeTiers.Title.Report"));
    	overlapsOnly = (Boolean) arguments[13];
    	
    	arguments[13] = null;
    	
    	super.execute(receiver, arguments);
    }
    
    /**
     * Given a list of tiers, it merges annotations on all selected tiers.
     *  
     * @param tierList the list of tiers to be merged
     * @return a list of merged annotations
     */
    @Override
	protected ArrayList<AnnotationValuesRecord> getComputedAnnRecord(List<TierImpl> tierList, TranscriptionImpl transcription){
    	
    	ArrayList<AnnotationValuesRecord> mergedAnnList = new ArrayList<AnnotationValuesRecord>();
    	
    	//if there are no tiers to be inspected, then nothing to merge
    	if( tierList.size() <= 0 ){
    		return mergedAnnList;
    	}
    	    	
    	String constraint;
    	TierImpl currentTier;
    	long beginTime, endTime;
    	AnnotationValuesRecord mergeRecord;
    	AbstractAnnotation abstrAnnotation;
    	List<AbstractAnnotation> annotations = new ArrayList<AbstractAnnotation>();
    	
    	if(overlapsOnly){    	
    		AnnotationValuesRecord annRecord;
        	List<AnnotationValuesRecord> annotationsRecord;        	
    		/**
        	 * Stage 1: Get overlaps from first tier
        	 */
        	currentTier = tierList.get(0);
        	
        	// take the number of annotations on the first source tier as the indicator for
            // progress updates
            int annCount = currentTier.getNumberOfAnnotations();
            float perAnn = 60f;
            if(processMode == AnnotationFromOverlaps.SINGLE_FILE_MODE){
            	if (annCount > 0) {
                	perAnn = 60 / (float) annCount;
                }
            	progressUpdate(10, calculating);
            }
            		
        	//if there are no annotations on a tier, then there can be no overlaps
        	if( annCount <= 0 ){
        		return mergedAnnList;
        	}
        	    	
        	boolean valueFromCurrentTier = (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_FROM_TIER && currentTier.getName().equals(annFromTier));
        	
        	constraint = getConstraint(currentTier.getName());
        	annotations = currentTier.getAnnotations();
        	//loop over all annotations and store the overlaps
        	for( int i=0; i< annotations.size(); i++ ){
        		beginTime = annotations.get(i).getBeginTimeBoundary();
        		endTime = annotations.get(i).getEndTimeBoundary();
        		
        		//begin by viewing annotations as overlaps when the overlaps criteria is different than 
        		//obeying user constraints. If they should obey user constraints, then add them when they
        		//are equal to the constraint or when there is no constraint specified for the tier name.        		
        		if( overlapsCriteria != OVERLAP_CRITERIA_USE_SPECIFIED_CONSTRAINTS || constraint == null || 
        				(constraint != null && constraint.equals(annotations.get(i).getValue())) ){   
        			mergeRecord = new AnnotationValuesRecord(currentTier.getName(), annotations.get(i).getValue(), beginTime, endTime);
        			if(valueFromCurrentTier || annotationValueType != AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_FROM_TIER){
        				mergeRecord.setNewLabelValue(annotations.get(i).getValue());
        			} else {
        				mergeRecord.setNewLabelValue("");
        			}
        			mergedAnnList.add(mergeRecord);   			
        		}
        	}  
        	
        	       	
        	/**
        	 * Stage 2: Loop over the remaining tiers and remove overlaps
        	 */
        	for( int i=1; i<tierList.size(); i++ ){   
        		 if(processMode == AnnotationFromOverlaps.SINGLE_FILE_MODE){
        			 progressUpdate((int) (10 + (i * perAnn)), calculating);
        		 }
        		 
        		//if there are no overlaps, then no overlaps will be created
    	    	if( mergedAnnList.size() <= 0 ){
    	    		break;
    	    	}
    	    	
    	    	
    	    	
        		currentTier = tierList.get(i);
    	    	annotations = currentTier.getAnnotations();
    	    	
    	    	valueFromCurrentTier = (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_FROM_TIER && currentTier.getName().equals(annFromTier));
    	    	
    	    	annotationsRecord = new ArrayList<AnnotationValuesRecord>();
            	for( int a=0; a<annotations.size(); a++ ){
            		abstrAnnotation = annotations.get(a);
            		beginTime = abstrAnnotation.getBeginTimeBoundary();
            		endTime = abstrAnnotation.getEndTimeBoundary();
            		
            		annRecord = new AnnotationValuesRecord(currentTier.getName(), abstrAnnotation.getValue(), beginTime, endTime);
            		if(valueFromCurrentTier || annotationValueType != AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_FROM_TIER){
            			annRecord.setNewLabelValue(abstrAnnotation.getValue());
        			} else {
        				annRecord.setNewLabelValue("");
        			}
            		annotationsRecord.add(annRecord);   			
            		
            	}   
    	    	
    	    	constraint = getConstraint(currentTier.getName());
    	    	outerLoop:
    	    	for( int overlapNr = 0; overlapNr < mergedAnnList.size(); overlapNr++ ){
    	    		if( annotationsRecord.size() <= 0 ){
    	    			for( int j=mergedAnnList.size()-1; j>=overlapNr; j-- )
    	    				mergedAnnList.remove(j);
    	    			break;
    	    		}
    	    		
    	    		annRecord = annotationsRecord.get(0);
    	    		beginTime = annRecord.getBeginTime();
    	    		endTime = annRecord.getEndTime();
    	    		mergeRecord = mergedAnnList.get(overlapNr);
    	    		
    	    		valueFromCurrentTier = (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_FROM_TIER 
    	    				&& annRecord.getTierName().equals(annFromTier));
    	    		
    	    		//decide to go on to compute overlaps for this annotation
    	    		constraint = getConstraint(currentTier.getName());    	    		
    	    		boolean matchConstraint = checkConstraint(mergeRecord.getValue(), annRecord.getValue(), overlapsCriteria, constraint);    	    		
    	    		if( !matchConstraint ){    	    			
    	    			if( endTime < mergeRecord.getEndTime() ){
    	    				annotationsRecord.remove(0);	    				
    	    			}else{
    	    				mergedAnnList.remove(overlapNr);	    				
    	    			}
    	    			overlapNr--;
    	    			continue;
    	    		}
    	    		
    	    		//if annotation comes before the mergedAnn
    	    		//      |---|
    	    		//|---|
    	    		if( endTime < mergeRecord.getBeginTime() ){    	    			
    	    			annotationsRecord.remove(0);	    			
    	    			overlapNr--;
    	    			continue;
    	    		}
    	    		
    	    		//|----|
    	    		//      |--|
    	    		if( beginTime >= mergeRecord.getEndTime() ){
    	    			mergedAnnList.remove(overlapNr);
    	    			overlapNr--;
    	    			continue;
    	    		} 
    	    		
    	    		//  |---|
    	    		//|---|
    	    		if( beginTime < mergeRecord.getBeginTime() && endTime <= mergeRecord.getEndTime()){  
    	    			mergeRecord.setBeginTime(beginTime); 
    	    			if(valueFromCurrentTier){
    	    				if(mergeRecord.getTierName().equals(annFromTier)){
    	    					if(mergeRecord.getNewLabelValue().trim().length() > 0){
    	    						mergeRecord.setNewLabelValue(annRecord.getNewLabelValue() + " " +
    	    								mergeRecord.getNewLabelValue() );
    	    					} else{
    	    						mergeRecord.setNewLabelValue(annRecord.getNewLabelValue());
    	    					}
    	    				}
    	    			} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME) {
          					mergeRecord.setNewLabelValue(annRecord.getNewLabelValue() + " " + mergeRecord.getNewLabelValue());
          				} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER){
          					mergeRecord.setNewLabelValue(mergeRecord.getNewLabelValue() + " " + annRecord.getNewLabelValue());
          				}
    	    			    	    			
    	    			// check if it is the last annotation
    	    			if(annotationsRecord.size() >= 2){
    	    				//    	 |-------------------------------------------|
        					//    |------------|   |-------------| |-----| |---------|
    	    				while(annotationsRecord.get(1).getBeginTime() < mergeRecord.getEndTime()){
	    						matchConstraint = checkConstraint(mergeRecord.getValue(), annotationsRecord.get(1).getValue(), overlapsCriteria, constraint);
	    						if(matchConstraint){
        	    	    			overlapNr--;
        	    	    			break;
        	    				} else {
        	    					// |----------------|
                					//|-----| |-----|
        	    					if(annotationsRecord.get(1).getEndTime() <= mergeRecord.getEndTime()){
        	    						annotationsRecord.remove(1);  
        	    						if(annotationsRecord.size() < 2){
        	    							break;
        	    						}
        	    					} else {
        	    						// 		|--------------|   
                    					// |-------------|  |--------|
        	    						annotationsRecord.get(1).setBeginTime(mergeRecord.getEndTime());
        	    						break;
        	    					}
        	    				}
	    					}
    	    			}
    	    			
    	    			annotationsRecord.remove(0);	    			
    	    			continue;	
    	    		}	
    	    		
    	    		//|-------|
    	    		//  |---|
    	    		if( beginTime >= mergeRecord.getBeginTime() && endTime <= mergeRecord.getEndTime()){   
    	    			if(valueFromCurrentTier){
    	    				if(mergeRecord.getNewLabelValue().trim().length() > 0){
	    						mergeRecord.setNewLabelValue(mergeRecord.getNewLabelValue() + " " + annRecord.getNewLabelValue());
	    					} else{
	    						mergeRecord.setNewLabelValue(annRecord.getNewLabelValue());
	    					}
    	    			} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME || 
    	    					annotationValueType ==AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER ) {
        					mergeRecord.setNewLabelValue(mergeRecord.getNewLabelValue() + " " + annRecord.getNewLabelValue());
        				} 
    	    			
    	    			// check if it is the last annotation
    	    			if(annotationsRecord.size() >= 2){
    	    				//   |-------------------------------------------|
        					//    |--------|   |-------------| |-----| |---------|
    	    				while(annotationsRecord.get(1).getBeginTime() < mergeRecord.getEndTime()){
	    						matchConstraint = checkConstraint(mergeRecord.getValue(), annotationsRecord.get(1).getValue(), overlapsCriteria, constraint);
	    						if(matchConstraint){
        	    	    			overlapNr--;
        	    	    			break;
        	    				} else {
        	    					// |----------------|
                					//|-----| |-----|
        	    					if(annotationsRecord.get(1).getEndTime() <= mergeRecord.getEndTime()){
        	    						annotationsRecord.remove(1);  
        	    						if(annotationsRecord.size() < 2){
        	    							break;
        	    						}
        	    					} else {
        	    						// 		|--------------|   
                    					// |-------------|  |--------|
        	    						annotationsRecord.get(1).setBeginTime(mergeRecord.getEndTime());
        	    						break;
        	    					}
        	    				}
	    					}
    	    			}
    	    			annotationsRecord.remove(0);
    	    			continue;
    	    		}    	    		
    	    		
    	    		//|----|
    	    		//  |------|
    	    		if( beginTime >= mergeRecord.getBeginTime() && endTime > mergeRecord.getEndTime()){ 
    					// check if  it is the last merge Record
    	    			if(overlapNr+1 < mergedAnnList.size()){
    	    				// |------|    |--------|  |--------|
        					//    |-------------------------------|
        	    			while(mergedAnnList.get(overlapNr+1).getBeginTime() < endTime){
        	    				// |------|    |--------|  |--------|
            					//    |-------------------------------|
        	    				matchConstraint = checkConstraint(mergedAnnList.get(overlapNr+1).getValue(), annRecord.getValue(), overlapsCriteria, constraint);
    	    					if(matchConstraint){
    	    						if(valueFromCurrentTier){
    	    							if(mergeRecord.getNewLabelValue().trim().length() > 0){
    	    								annRecord.setNewLabelValue(mergeRecord.getNewLabelValue() + " " + annRecord.getNewLabelValue());
    	    	    					} else{
    	    	    						annRecord.setNewLabelValue(annRecord.getNewLabelValue());
    	    	    					}
    	        	    			} else	if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME || 
        	    	    					annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER) {
        	    						annRecord.setNewLabelValue(mergeRecord.getNewLabelValue() + " " + annRecord.getNewLabelValue());
        	    	    			} else {
        	    	    				annRecord.setNewLabelValue(mergeRecord.getNewLabelValue());
        	    	    				annRecord.setTierName(mergeRecord.getTierName());
        	    	    			}
        	    					
        	    					annRecord.setBeginTime(mergeRecord.getBeginTime());
        	    					mergedAnnList.remove(overlapNr);
        	    	    			overlapNr--;
        	    	    			continue outerLoop;
            	    			} else {
            	    				// |------| |--------|  |--------|  
                					//    |-----------------------|
            	    				if(mergedAnnList.get(overlapNr+1).getEndTime() <= endTime){
            	    					mergedAnnList.remove(overlapNr+1); 
            	    					if(overlapNr+1 >= mergedAnnList.size()){
            	    						break;
            	    					}
            	    				} else {
            	    					// |--------| 		|--------------|   
                        				//       |-------------| 
            	    					mergedAnnList.get(overlapNr+1).setBeginTime(endTime);
            	    					break;
            	    				}
            	    			}
    	    				}
        	    		} 
        	    		
    	    			if(valueFromCurrentTier){
    	    				if(mergeRecord.getNewLabelValue().trim().length() > 0){
    	    					mergeRecord.setNewLabelValue(mergeRecord.getNewLabelValue() + " " + annRecord.getNewLabelValue());
	    					} else{
	    						mergeRecord.setNewLabelValue(annRecord.getNewLabelValue());
	    					}
        	    		} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME || 
        	    				annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER) {
        	    			mergeRecord.setNewLabelValue(mergeRecord.getNewLabelValue() + " " + annRecord.getNewLabelValue());
        	    		}     					
        	    		mergeRecord.setEndTime(endTime);
        	    		annotationsRecord.remove(0);        	    		
        	    		continue;        	    		
    	    		} 
    	    		
    	    		//   |----|
    	    		//|----------|
    	    		if( beginTime < mergeRecord.getBeginTime() && endTime > mergeRecord.getEndTime() ){
    	    			// check if  it is the last merge Record
    	    			if(overlapNr+1 < mergedAnnList.size()){
    	    				//     |--------|  |--------|
        					//    |-------------------------------|
        	    			while(mergedAnnList.get(overlapNr+1).getBeginTime() < endTime){
        	    				//      |---|    |--------|  |--------|
            					//    |-------------------------------|
        	    				matchConstraint = checkConstraint(mergedAnnList.get(overlapNr+1).getValue(), annRecord.getValue(), overlapsCriteria, constraint);
    	    					if(matchConstraint){
    	    						if(valueFromCurrentTier){
    	    							if(mergeRecord.getNewLabelValue().trim().length() > 0){
    	    								annRecord.setNewLabelValue(annRecord.getNewLabelValue() + " " +
    	    	    								mergeRecord.getNewLabelValue() );
    	    	    					} else{
    	    	    						annRecord.setNewLabelValue(annRecord.getNewLabelValue());
    	    	    					}
    	        	    			} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME){
        	    						annRecord.setNewLabelValue(annRecord.getNewLabelValue() + " " + mergeRecord.getNewLabelValue());
        	    					} else if(annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER) {
        	    						annRecord.setNewLabelValue(mergeRecord.getNewLabelValue() + " " + annRecord.getNewLabelValue());
        	    	    			} else {
        	    	    				annRecord.setNewLabelValue(mergeRecord.getNewLabelValue());
        	    	    				annRecord.setTierName(mergeRecord.getTierName());
        	    	    			}
    	    						
        	    					mergedAnnList.remove(overlapNr);
        	    	    			overlapNr--;
        	    	    			continue outerLoop;
            	    			} else {
            	    				//      |---| |--------|  |--------|  
                					//    |-----------------------|
            	    				if(mergedAnnList.get(overlapNr+1).getEndTime() <= endTime){
            	    					mergedAnnList.remove(overlapNr+1); 
            	    					if(overlapNr+1 >= mergedAnnList.size()){
            	    						break;
            	    					}
            	    				} else {
            	    					//     |--| 	|--------------|   
                        				//   |-------------| 
            	    					mergedAnnList.get(overlapNr+1).setBeginTime(endTime);
            	    					break;
            	    				}
            	    			}
    	    				}
        	    		} 
        	    		
    	    			if(valueFromCurrentTier){
    	    				if(mergeRecord.getTierName().equals(annFromTier)){
    	    					mergeRecord.setNewLabelValue(annRecord.getNewLabelValue() +
    	    							" " + mergeRecord.getNewLabelValue() );
    	    				} else {
    	    					mergeRecord.setNewLabelValue(annRecord.getNewLabelValue());
        	    				mergeRecord.setTierName(annRecord.getTierName());
    	    				}
        	    		}else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME){
        	    			mergeRecord.setNewLabelValue(annRecord.getNewLabelValue() + " " + mergeRecord.getNewLabelValue());
    					} else if(annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER) {
    						mergeRecord.setNewLabelValue(mergeRecord.getNewLabelValue() + " " + annRecord.getNewLabelValue());
    	    			}
    	    			
        	    		mergeRecord.setBeginTime(beginTime);
        	    		mergeRecord.setEndTime(endTime);
        	    		annotationsRecord.remove(0);        	    		
        	    		continue;
    	    		}
    	    	}
        	} 
    	} else {
    		/**
        	 * Stage 1: Get annotations from all the tiers
        	 */
        	for( int i=0; i< tierList.size(); i++ ){  
        		annotations.addAll(tierList.get(i).getAnnotations());
        	}	
        	
        	if(annotations.size() == 0){    		
        		return mergedAnnList;
        	}
        	
        	AnnotationComparator annComparator = new AnnotationComparator();
    		Collections.sort(annotations, annComparator);
        	   	
        	// take the number of annotations as the indicator for
            // progress updates
            int annCount = annotations.size();
            float perAnn = 60f;
            if(processMode == AnnotationFromOverlaps.SINGLE_FILE_MODE){
            	if (annCount > 0) {
            		perAnn = 60 / (float) annCount;
                }
            	progressUpdate(10, calculating);
            }
            
            // sort the mergeANN LIST
            
            /**
        	 * Stage 2: Loop over the annotation and merge them
        	 */
            for( int i= 0; i < annotations.size(); i++ ){   
        		if(processMode == AnnotationFromOverlaps.SINGLE_FILE_MODE){
        			progressUpdate((int) (10 + (i * perAnn)), calculating);
        		} 
        		
        		
            	abstrAnnotation = (AbstractAnnotation) annotations.get(i);
        		beginTime = abstrAnnotation.getBeginTimeBoundary();
        		endTime = abstrAnnotation.getEndTimeBoundary();
        		
        		constraint = getConstraint(abstrAnnotation.getTier().getName());
    	    	
    	    	// if list is empty add the annotation
        		if(mergedAnnList.size() == 0){    			
        			if( overlapsCriteria != OVERLAP_CRITERIA_USE_SPECIFIED_CONSTRAINTS || constraint == null || 
            				(constraint != null && constraint.equals(abstrAnnotation.getValue())) ){ 
        				mergeRecord = new AnnotationValuesRecord(abstrAnnotation.getTier().getName(), null, beginTime, endTime);
        				mergeRecord.setNewLabelValue(abstrAnnotation.getValue());
        				mergedAnnList.add(mergeRecord); 
            		}
        			continue;
        		}
        		
        		mergeRecord = mergedAnnList.get(mergedAnnList.size()-1);
        		
        		//decide to go on to merge this annotation
        		boolean matchConstraint = checkConstraint(mergeRecord.getNewLabelValue(), abstrAnnotation.getValue(), overlapsCriteria, constraint);
        		if( !matchConstraint ){    			
        			continue;
        		}
        		
        		// if the annotation doesn't overlap with the merge record
        		//|----|
        		//      |--|
        		if( beginTime >= mergeRecord.getEndTime() ){
        			mergeRecord = new AnnotationValuesRecord(abstrAnnotation.getTier().getName(), null, beginTime, endTime);
        			mergeRecord.setNewLabelValue(abstrAnnotation.getValue());
        			mergedAnnList.add(mergeRecord);	 
        			continue;
        		}
        		
        		// if the annotation overlaps with the merge record
        		//|-------|
        		//  |---|
        		if( beginTime >= mergeRecord.getBeginTime() && endTime <= mergeRecord.getEndTime()){    			
        			   			
        			String newValue = null;
        			if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME ){    				
    					newValue = mergeRecord.getNewLabelValue() +" "+ abstrAnnotation.getValue();
    				} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER){
    					// check the tier order
    					if(tierList.indexOf(transcription.getTierWithId(mergeRecord.getTierName())) <= 
    						tierList.indexOf(abstrAnnotation.getTier())){
    						newValue = mergeRecord.getNewLabelValue() +" "+ abstrAnnotation.getValue();
    					} else {
    						newValue = abstrAnnotation.getValue() + " " + mergeRecord.getNewLabelValue();
    					}
    				} else {
    					newValue = mergeRecord.getNewLabelValue();
    				}
        			
        			mergeRecord.setNewLabelValue(newValue);
        			continue;
        		}
        		
        		//|----|
        		//  |------|
        		if( beginTime >= mergeRecord.getBeginTime() && endTime > mergeRecord.getEndTime()){
        			mergeRecord.setEndTime(endTime);
        			
        			String newValue = null;
        			if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME ){    				
    					newValue = mergeRecord.getNewLabelValue() +" "+ abstrAnnotation.getValue();
    				} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER){
    					// check the tier order
    					if(tierList.indexOf(transcription.getTierWithId(mergeRecord.getTierName())) <= 
    						tierList.indexOf(abstrAnnotation.getTier())){
    						newValue = mergeRecord.getNewLabelValue() +" "+ abstrAnnotation.getValue();
    					} else {
    						newValue = abstrAnnotation.getValue() + " " + mergeRecord.getNewLabelValue();
    					}
    				} else {
    					newValue = mergeRecord.getNewLabelValue();
    				}
        			
        			mergeRecord.setNewLabelValue(newValue);
        			continue;
        		}
        	}
    	}
    	return mergedAnnList;
    }
    
    /**
     * Checks if the annotation value match the specified constraint
     * @param mergeValue the value of the annotation to be checked
     * @param newValue the value of the annotation for which the constraint check is performed
     * @param criteria the criteria used to check if constraints are obeyed (merge, equal value, inequal value, or user specified constraints.
     * @param constraint the value that newValue should have to obey the constraint.
     * @return
     */
    @Override
	protected boolean checkConstraint(String mergeValue, String newValue, int criteria, String constraint){
    	//the only constraint is that it should be an overlap, so return true because values are not relevant
    	if(overlapsCriteria == OVERLAP_CRITERIA_OVERLAP )
    		return true;
    	
    	if(overlapsCriteria == OVERLAP_CRITERIA_VALUES_EQUAL )
        	return mergeValue.equals(newValue);
    	
    	if( overlapsCriteria == OVERLAP_CRITERIA_VALUES_NOT_EQUAL )
    		return !mergeValue.equals(newValue);    	
    	
    	//use the specified constraints by the user
    	if( overlapsCriteria == OVERLAP_CRITERIA_USE_SPECIFIED_CONSTRAINTS )
    		return constraint == null || newValue.equals(constraint);
    	
    	return true;
    }
    
    /**
     * Returns the specified constraint for the given tier name.
     * @param tierName the name of the tier for which the constraint should be given.
     * @return the value (constraint) that the given tier name should have.
     * or NULL if no constraints are specified for this tier name.
     */
    @Override
	protected String getConstraint(String tierName) {    	
    	if(overlapsCriteria == OVERLAP_CRITERIA_VALUES_EQUAL){
    		if (tierValueConstraints.size() == 1){
    			return tierValueConstraints.get(0)[0];
    		}  		
    	} else {
    		for( int i=0; i<tierValueConstraints.size(); i++ ){
    			if( tierValueConstraints.get(i)[0].equals(tierName) ){
    				//System.out.println("READY: return value");
    				return tierValueConstraints.get(i)[1];
    			}
        	}
    	}
		
		return null;
	}
    
    
    /**
	 * Class to compare or sort the annotations
	 * according to the start time
	 * 
	 * @author aarsom
	 *
	 */
	private class AnnotationComparator implements Comparator<Annotation>{			
		@Override
		public int compare(Annotation o1, Annotation o2) {
			Long bt1 = o1.getBeginTimeBoundary();
			Long bt2 = o2.getBeginTimeBoundary();
			if(bt1 != bt2){
				return bt1.compareTo(bt2);
			} else {
				Long et1 = o1.getEndTimeBoundary();
				Long et2 = o2.getEndTimeBoundary();
				return et1.compareTo(et2);	
			}
		}				
	}
}


        
        
        
        
        
        
        
       
        
       

        

        
        
        

    

    
    
    
