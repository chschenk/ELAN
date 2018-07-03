package mpi.eudico.client.annotator.tier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.AnnotationSlicer;
import mpi.eudico.client.annotator.util.AnnotationValuesRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.util.SimpleReport;

/**
 * A command that subtracts the annotations on selected
 * tiers and creates the subtracted annotations on a new tier. 
 *
 * @author aarsom
 * @version November, 2011
 */
public class AnnotationFromSubtraction extends AnnotationFromOverlaps{
	
	private String referenceTierName;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AnnotationFromSubtraction(String name) {
        super(name);    
        calculating = ElanLocale.getString("SubtractAnnotationDialog.Calculating");        
    }

    /**
	 * <b>Note: </b>it is assumed the types and order of the arguments are correct.<br>
	 *
	 * @param receiver the TranscriptionImpl
	 * @param arguments the arguments: <ul>
	 * 			<li>arg[0] = the selected tiers (String[])</li> 
	 * 			<li>arg[1] = the new tier name (String)</li> 
	 *        	<li>arg[2] = the linguistic type of the new tier (LinguisticType)</li>
	 *        	<li>arg[3] = the kind of value the new tier should have (Integer)
	 *        					0 = time format (then it uses arg[4])
	 *        					1 = specific value (then it uses arg[5])	
	 *        	<li>arg[4] = the time format to use (String)</li> 
	 *        	<li>arg[5] = the specific value to use (String)</li>	 *        	
	 *        	<li>arg[6] = create overlaps on currently opened transcription (0 = current transcription, 1 = based on files in arg[10]) (Integer)
	 *        	<li>arg[7] = names of the files to create annotations for (String[])
	 *        	<li>arg[8] = boolean value indicating if pal mode should be used (value is true) or NTSC (value is false). Only needed when time format is SMPTE.
	 *        	<li>arg[9] = parent tier name (if tier is selected as parent) or null</li>
	 *          <li>arg[10] = reference tier name(the reference tier from which other tiers are to be subtracted)(String) </li>
	 *        </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {    	
		transcription = (TranscriptionImpl) receiver;
	        
		//copy tierlist in arguments[0] into array sourceTiers
		Object[] objectArray = (Object[])arguments[0]; 		
		sourceTiers = new String[objectArray.length];		
	    for( int i=0; i<objectArray.length; i++ )
	     	sourceTiers[i] = objectArray[i].toString();
	    
	    destTierName = (String) arguments[1];
	    destLingType = (String) arguments[2];
	    annotationValueType = (Integer) arguments[3];
	    timeFormat = (String) arguments[4];
	    annWithValue = (String) arguments[5]; 
	    processMode = (Integer) arguments[6];
	    
	    objectArray= (Object[]) arguments[7];
	    if(objectArray != null){
	    	filenames = new String[objectArray.length];
	    	for( int i=0; i<objectArray.length; i++ )
	    		filenames[i] = objectArray[i].toString();
	    }
	    
	    usePalFormat = (Boolean) arguments[8];
	    parentTierName = (String) arguments[9];   
	    referenceTierName = (String)arguments[10];
	    
	    report = new SimpleReport(ElanLocale.getString(
                "SubtractAnnotationDialog.Report.Title")); 
	    
	    int noOfFiles = 1;
        if(processMode == MULTI_FILE_MODE){
        	noOfFiles = filenames.length;
        }
	    report("Number of files to process:  " + noOfFiles);       
        report("Selected Tiers : ");       
        for(String tierName : sourceTiers){
        	report("\t"+ tierName);
        }          
        report("Selected Linguistic Type Name : " + destLingType);       
        if(parentTierName != null){        	
             report("Parent Tier Name : " + parentTierName);     
        }
	    
	    if(referenceTierName != null){
	    	report("Reference Tier Name : " + referenceTierName);        
	    }
	    
	    execute();
	}
    
    /**
     * Override the work-method of the superclass.
     * 
     * Given a list of tiers, it computes the subtraction of annotations on all selected tiers.
     *  
     * @param tierList the list of tiers where the subtraction is computed for
     * @return a list of subtractions
     */
    @Override
    protected List<AnnotationValuesRecord> getComputedAnnRecord(List<TierImpl> tierList, TranscriptionImpl transcription){
	    if(referenceTierName == null){
	    	return getExclusiveSubtractedList( tierList );
	    } else {
	    	return getSubtractsList( tierList, transcription );
	    }
    }    
    
    /**
     * Given a list of tiers, it computes the action subtraction of annotations on all selected tiers.
     * 
     * @param tierList, the list of tiers where the subtraction are computed for
     * @param transcription
     * @return a list of subtract
     */
    private List<AnnotationValuesRecord> getSubtractsList(List<TierImpl> tierList, TranscriptionImpl transcription) {
		List<AnnotationValuesRecord> subtractionList = new ArrayList<AnnotationValuesRecord>();
		
		//if there are no tiers to be inspected, then there are no subtracts as well
		if( tierList.size() <= 0 ){    		
			return subtractionList;
		}		
		
		TierImpl currentTier = null;
    	
    	
    	currentTier = (TierImpl) transcription.getTierWithId(referenceTierName);
    	if(currentTier == null){
    		ClientLogger.LOG.severe("The reference tier from which the other tiers to be subtracted is not found in this transcription" + transcription.getName());            	
        	report("Could not find the reference tier in this transcription");            	
        	return subtractionList;
    	}
    	
    	List<AnnotationValuesRecord> resultList = null;
		AbstractAnnotation ann = null;
    	AbstractAnnotation nextAnn = null;
    	AnnotationValuesRecord sub = null;
    	long annBegin, annEnd, nextAnnBegin;
    	long subBegin, subEnd;    	
    	
    	List<AbstractAnnotation> annotations = currentTier.getAnnotations();
    	    	
    	for(int x = 0; x < annotations.size(); x++){
    		ann = (AbstractAnnotation) annotations.get(x);
    		annBegin = ann.getBeginTimeBoundary();
    		annEnd = ann.getEndTimeBoundary();	        		
        	subtractionList.add(new AnnotationValuesRecord(null, ann.getValue(), annBegin, annEnd));
    	}    		
    	
    	// take the number of annotations on the first source tier as the indicator for
        // progress updates
        int annCount = currentTier.getNumberOfAnnotations();
        float perAnn = 60f;
        if(processMode == SINGLE_FILE_MODE){
        	if (annCount > 0) {
            	perAnn = 60 / (float) annCount;
            }
        	progressUpdate(10, calculating);        
        }    	
		
    outerLoop:
	  	for( int i=0; i < tierList.size(); i++ ){
	  		long timeSoFar = 0;
	  		if(processMode == SINGLE_FILE_MODE){
	  			progressUpdate((int) (10 + (i * perAnn)), calculating);
	  		}
	  		
	  		currentTier = tierList.get(i);
	  		if(referenceTierName.equals(currentTier.getName())){
	  			continue;
	  		}
	  		annotations = currentTier.getAnnotations();
	    	resultList = new ArrayList<AnnotationValuesRecord>();	
	    	
	    	if(annotations.size() <= 0){
	    		continue;
	    	} 
	    	
	 	innerLoop:
			for( int subNr = 0; subNr < subtractionList.size(); subNr++ ){ 				
				if(annotations.size() <= 0){
					for(int a = subNr; a < subtractionList.size(); a++)
			    	{
			    		sub = subtractionList.get(a);
			    		resultList.add(new AnnotationValuesRecord(null, sub.getValue(), sub.getBeginTime(), sub.getEndTime()));
			    	}
					subtractionList = resultList;
		    		continue outerLoop;
		    	}
	    		ann = (AbstractAnnotation) annotations.get(0);
	    		annBegin = ann.getBeginTimeBoundary();
	    		annEnd = ann.getEndTimeBoundary();	
	    		
	    		sub = subtractionList.get(subNr);
	    		subBegin = sub.getBeginTime();
	        	subEnd = sub.getEndTime();
	    		
	    		//if(resultList.size() != 0){
	    			if(timeSoFar > annBegin){
	    				annBegin = timeSoFar;
	    			} 
	    			
	    			if(timeSoFar > subBegin){
	    				subBegin = timeSoFar;
	    			} 
	    		//}	    		
	    		
	    			
	    		//if annotation comes before the subtracts, remove annotation and move to the next substract
	    		//       |------|
	    		// |---|	    	
	    		if( annBegin < subBegin  && annEnd < subBegin){	    			
	    			timeSoFar = annEnd;
	    			annotations.remove(0);	  
	    			subNr--;
	    			continue innerLoop;
	    		} 
	    		
	    		//if annotation comes after the subtracts, add this subtract to the resultingSubList and move to the next subtract
	    		// |------|
	   	    	//          |---|	
	    		if(subBegin  < annBegin  &&  subEnd < annBegin ){
	    			resultList.add(new AnnotationValuesRecord(null, sub.getValue(), subBegin, subEnd));	    					
	    			timeSoFar = subEnd;
	    			continue ;
	    		}   
	    		
	    		
	    		// if there is an overlap
	    		
	    		// if the annotation begins before/ after the subtract, create a subtract on the non overlapping area
	    		//    |------|
	    		// |---------|
	    		if( annBegin < subBegin ){	    			
	    			timeSoFar = subBegin;
	    		} 
	    		// |------|
	    		//   |----|
	    		else if(subBegin < annBegin ){
	    			resultList.add(new AnnotationValuesRecord(null, sub.getValue(), subBegin, annBegin));	
	    			timeSoFar = annBegin;
	    		}
	    		
	    		// if the annotation ends before the subtract
	    		// |----------|
	    		// |----|
	    		if( annEnd < subEnd ){	 
	    			// check for the next annotation
	    			if(annotations.size() >= 2){
	    				nextAnn = (AbstractAnnotation) annotations.get(1);
	    				nextAnnBegin = nextAnn.getBeginTimeBoundary();	
	    				
	    				// if the next annotations comes before the end of the subtract
	    				// |----------|
	    	    		// |----|  |--|
	    				if(nextAnnBegin < subEnd){
	    					if(annEnd < nextAnnBegin){
	    						resultList.add(new AnnotationValuesRecord(null, sub.getValue(), annEnd, nextAnnBegin ));
	    					} 
	    					timeSoFar = nextAnnBegin;
	    					subNr--;
		    			} else {
		    				resultList.add(new AnnotationValuesRecord(null, sub.getValue(), annEnd, subEnd ));
		    				timeSoFar = subEnd;
		    			}
	    			} 
	    			// |----------|
	    	    	// |----|  			|--|
	    			else {
	    				resultList.add(new AnnotationValuesRecord(null, sub.getValue(), annEnd, subEnd));
	    				timeSoFar = subEnd;
	    			}
	    			annotations.remove(0);	 
    				continue;
	    		} 
	    		// |-----|
	    		// |----------|
	    		else if(subEnd < annEnd){			    	
	    				timeSoFar = subEnd;	 
	    				continue ;
	    		} 
	    		
	    		//|------|
	    		//|------|
	    		else{
	    			annotations.remove(0);	  
	    		}
			}    
	    	subtractionList = resultList;
	  	}
		return subtractionList;
	}

    /**
     * Given a list of tiers, it computes the subtraction(based on exclusiveOR) of annotations on all selected tiers.
     * 
     * @param tierList, the list of tiers where the subtraction are computed for    
     * @return a list of subtract
     */
	private List<AnnotationValuesRecord> getExclusiveSubtractedList(List<TierImpl> tierList) {
		List<AnnotationValuesRecord> subtractionList = new ArrayList<AnnotationValuesRecord>();
		
		//if there are no tiers to be inspected, then there are no subtracts as well
		if( tierList.size() <= 0 ){    		
			return subtractionList;
		}
		
		List<Long> timeValues = AnnotationSlicer.getTimeValues(tierList);
		Map<Long, List<Annotation>> map = AnnotationSlicer.getAnnotationMap(timeValues, tierList);		
		
		List<Annotation> annList;				
		long currTimeValue;		
		
		for(int i = 0; i < timeValues.size(); i++){
			currTimeValue = timeValues.get(i);
			annList  = map.get(currTimeValue);
			if(annList.size()%2 != 0){
				if(annList.size() == 1){
					subtractionList.add(new AnnotationValuesRecord(null, annList.get(0).getValue(), currTimeValue, timeValues.get(i+1)));
				} else
					subtractionList.add(new AnnotationValuesRecord(null, "", currTimeValue, timeValues.get(i+1)));
			}
		}
	    return subtractionList;
	}
}

    

    
  