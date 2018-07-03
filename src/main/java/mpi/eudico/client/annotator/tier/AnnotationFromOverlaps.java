package mpi.eudico.client.annotator.tier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.commands.AddTypeCommand;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationValuesRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.IncludedIn;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.server.corpora.util.SimpleReport;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.TimeFormatter;

/**
 * A command that calculates overlaps of annotations on selected
 * tiers and creates annotations for each overlap on a third tier. The
 * duration of each  new annotation is the duration of the overlap.<br>
 *
 * @author Han Sloetjes
 * @author Jeffrey Lemein
 * @updatedBy aarsom
 * @version November, 2011
 */
public class AnnotationFromOverlaps implements ClientLogger, ProcessReporter {
	
	public static final int OVERLAP_CRITERIA_OVERLAP = 0;
	public static final int OVERLAP_CRITERIA_VALUES_EQUAL = 1;
	public static final int OVERLAP_CRITERIA_VALUES_NOT_EQUAL = 2;
	public static final int OVERLAP_CRITERIA_USE_SPECIFIED_CONSTRAINTS = 3;
	 
	public static final int SINGLE_FILE_MODE = 0;
	public static final int MULTI_FILE_MODE= 1;
	
	protected String commandName;
	protected TranscriptionImpl transcription;
	protected String[] sourceTiers;
    protected String destTierName;
    protected String destLingType;
    protected int annotationValueType;
    protected String timeFormat;
    protected String annWithValue;
    protected String annFromTier;
    // single file or multifile
    protected int processMode;
    protected String[] filenames;
    protected boolean usePalFormat;
    protected String parentTierName;
   	    
    protected boolean destTierCreated = false;
    protected List<AnnotationDataRecord> createdAnnos;    
    
    protected String createAnnotations = ElanLocale.getString("OverlapsDialog.CreatingAnn");
    protected String calculating = ElanLocale.getString("OverlapsDialog.Calculating");
    
    protected int overlapsCriteria;
    protected List<String[]> tierValueConstraints;
    
    private List<ProgressListener> listeners;
    protected ProcessReport report;
    
    protected  int numInspected = 0;
    protected int numFailed = 0;
    protected int numChanged = 0;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AnnotationFromOverlaps(String name) {
        commandName = name;
        report = new SimpleReport(ElanLocale.getString("OverlapsDialog.Report.Title"));
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are correct.<br>
     *
     * @param receiver the TranscriptionImpl
     * @param arguments the arguments: <ul>
     * 			<li>arg[0] = the selected tiers (String[])</li> 
     * 			<li>arg[1] = the new tier name (String)</li> 
     *        	<li>arg[2] = the linguistic type of the new tier (String)</li>
     *        	<li>arg[3] = the kind of value the new tier should have (Integer)
     *        					0 = time format (then it uses arg[4])
     *        					1 = specific value (then it uses arg[5])
     *        					2 = value from another tier (then it used arg[6]) 
     *        					3 = concat values from tiers based on the annotation time
     *        					4 = concat values from tiers based on the preferred tier order (then it uses arg[7]) </li>
     *        	<li>arg[4] = the time format to use (String)</li> 
     *        	<li>arg[5] = the specific value to use (String)</li>
     *        	<li>arg[6] = the specific value from a tier to use (String)</li>
     *         	<li>arg[7] = tier order in which the values will be concatenated (List of String)</li>
     *        	<li>arg[8] = the tier name of which the values should be taken (String)</li>
     *        	<li>arg[9] = the overlaps criteria (0 = regardless of the value, 1 = same value, 2 = different value, 3 = based on constraints) (List<String[]>)
     *        	<li>arg[10] = create overlaps on currently opened transcription (0 = current transcription, 1 = based on files in arg[10]) (Integer)
     *        	<li>arg[11] = names of the files to create annotations for (String[])
     *        	<li>arg[12] = boolean value indicating if pal mode should be used (value is true) or NTSC (value is false). Only needed when time format is SMPTE.
     *        	<li>arg[13] = parent tier name (if tier is selected as parent) or null</li>
     *        </ul>
     */
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
        annFromTier = (String) arguments[6];
        
        List<String> tierOrder = (List<String>)arguments[7];
        if(tierOrder != null && annotationValueType == 4 ){
        	sourceTiers = new String[tierOrder.size()];
        	for( int i=0; i< tierOrder.size(); i++ )
        		sourceTiers[i] = tierOrder.get(i).toString();
        }
        
        overlapsCriteria = (Integer) arguments[8];
        tierValueConstraints = (List<String[]>) arguments[9];
        processMode = (Integer) arguments[10];
        usePalFormat = (Boolean) arguments[12];
        objectArray= (Object[]) arguments[11];
        if(objectArray != null){
        	filenames = new String[objectArray.length];
        	for( int i=0; i<objectArray.length; i++ )
        		filenames[i] = objectArray[i].toString();   
        }
        
        parentTierName = (String) arguments[13];        
                
        int noOfFiles = 1;
        if(processMode == MULTI_FILE_MODE){
        	noOfFiles = filenames.length;
        }
        
        report("Number of files to process:  " + noOfFiles);       
        report("Selected Tiers : ");       
        for(String tierName : sourceTiers){
        	report("\t" + tierName);
        }          
        report("Selected Linguistic Type Name : " + destLingType);       
        if(parentTierName != null){        	
             report("Parent Tier Name : " + parentTierName);     
        }        
        execute();
    }
    
    /**
     * Starts the process in a seperate thread
     */
    protected void execute(){
    	Thread calcThread = new CalcOverLapsThread(AnnotationFromOverlaps.class.getName());

        try {
            calcThread.start();
        } catch (Exception exc) {
            LOG.severe("Exception in calculation: " +  exc.getMessage());
            progressInterrupt("An exception occurred: " + exc.getMessage());
            report("An exception occurred: " + exc.getMessage());
        }
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return commandName;
    }

    /**
     * Iterates over the annotations of the source tier and creates annotations
     * 
     */
    private boolean createAnnotations(TranscriptionImpl transcription) {    	
    	//retrieve original tier required for computation (the tiers with gaps)    	
    	ArrayList<TierImpl> tierList = new ArrayList<TierImpl>();
    	
    	TierImpl parentTier = null; 	
    	
    	// check for the parent tier 
    	if(parentTierName != null){
    		parentTier = (TierImpl)transcription.getTierWithId(parentTierName);
    		//check if parent tiers exists
            if (parentTier == null ) { 
            	ClientLogger.LOG.severe("The parent tier selected is not found in this transcription" + transcription.getName());            	
            	report("File skipped.Could not find the parent tier in this transcription");            	
            	numFailed++;
                return false;
            }
    	} 
    	
    	LinguisticType typeToUse = transcription.getLinguisticTypeByName(destLingType);
    	
    	// check for the linguistic type
    	if( processMode == MULTI_FILE_MODE ){
    		// if linguistic type name is not present, create a new type
    		if(typeToUse == null){
    			//create new type    			     
       		 	Constraint c;    
       		 	if(parentTierName == null){
       		 		c = null;
       		 	}else {
       		 		c = new IncludedIn();
       		 	} 
               //create and execute a command
               Command com = new AddTypeCommand(ELANCommandFactory.ADD_TYPE);               
               Object[] args = new Object[5];
               args[0] = destLingType;
               args[1] = c;
               args[2] = null;
               args[3] = Boolean.TRUE;
               args[4] = Boolean.FALSE;
               com.execute(transcription, args);
               typeToUse = transcription.getLinguisticTypeByName(destLingType);     
               report("Selected Linguistic type is created in this transcription.");
    		} else {
    			boolean validType = true;
    			if(parentTierName == null){
    				// if a top level tier is to be created
    				if (typeToUse.getConstraints() != null) {                		
    					validType =false;
                	}
       		 	} else {
       		 		// if a dependent tier is to be created
       		 		if (typeToUse.getConstraints() == null || typeToUse.getConstraints().getStereoType() != Constraint.INCLUDED_IN) {                		
       		 			validType =false;
       		 		}
       		 	}
    			
    			if(!validType){
    				ClientLogger.LOG.severe("Linguistic type for the tier to be created in this transcription is not compatible with the selected type: " + transcription.getName());
    				report("Skipping file : " + transcription.getName() + ".Cannot create the new tier.Linguistic types in this transcription is not compatible with the selected type");    
    				numFailed++;
    				return false;
    			}
    		}
    	}    	
    	
    	// get the selected tiers in transcription 
    	for( int i=0; i < sourceTiers.length; i++ ){
    		TierImpl tier = (TierImpl) transcription.getTierWithId(sourceTiers[i]);
    		
    		//check if source(selected) tier is present
    		if (tier == null)
                continue;
    		
    		tierList.add(tier);
    	} 
    	
    	// create a new tier for the new annotations    	
    	String participant = null;
		Locale locale = null;
		String langRef = null;
    	if(parentTier != null){
    		participant = parentTier.getParticipant();
    		locale = parentTier.getDefaultLocale();
    		langRef = parentTier.getLangRef();
    	} else {    		
    		// Check participants, locales and language references.
    		// Collect either a value which is equal in all tiers,
    		// or null if they are not consistent in all tiers.
    		boolean checkParticpant = true;
    		boolean checkLocale = true;
    		boolean checkLangRef = true;
    		// Get value from first tier
    		if (tierList.size() > 0) {
				participant = tierList.get(0).getParticipant();
				locale = tierList.get(0).getDefaultLocale();
				langRef= tierList.get(0).getLangRef();
    		}
    		// Check if other tiers have the same value
    		for(int i = 1 ; i < tierList.size(); i++ ){
    				// check for the participant
    				if(checkParticpant){
    					if(tierList.get(i).getParticipant() != null){
        					if( participant == null || !participant.equals(tierList.get(i).getParticipant())){
        						participant = null;
        						checkParticpant = false;
        	    			}
        				} else{
        					if( participant != null){
        						participant = null;
        						checkParticpant = false;
        	    			}
        				}
    				}    				
    				
    				// check for locale
    				if(checkLocale){
    					if(tierList.get(i).getDefaultLocale() != null){
        					if( locale == null || !locale.equals(tierList.get(i).getDefaultLocale())){
        						locale = null;
        						checkLocale= false;
        	    			}
        				} else{
        					if( locale != null){
        						locale = null;
        						checkLocale = false;
        	    			}
        				}
    				}
    			
					// check for language reference
					if(checkLangRef){
						if(tierList.get(i).getLangRef() != null){
	    					if( langRef == null || !langRef.equals(tierList.get(i).getLangRef())){
	    						langRef = null;
	    						checkLangRef= false;
	    	    			}
	    				} else{
	    					if( langRef != null){
	    						langRef = null;
	    						checkLangRef = false;
	    	    			}
	    				}
					}
			
    			if(!checkLocale && !checkParticpant && !checkLangRef){
    				break;
    			}
			}
    	}
    	
    	// TO DO : check if a tier with the destTierName already occurs
    	
    	TierImpl dTier = new TierImpl(parentTier, destTierName, participant, transcription, typeToUse);
		dTier.setDefaultLocale(locale);
		dTier.setLangRef(langRef);
		//dTier.setAnnotator(parentTier.getAnnotator());
    	transcription.addTier(dTier);
    	String cvName = typeToUse.getControlledVocabularyName();
    	ControlledVocabulary cv = transcription.getControlledVocabulary(cvName);
    	 
    	report("Destination tier created.");

        //flag for the undo command that the destination tier is created so that
        //when undo takes place, the command knows that we can remove the tier as a whole
        destTierCreated = true;

        //Stage 2: Computing overlaps
        //         dTier contains name of tier where the gaps should be placed
        //         tierList contains the tiers from which the gaps should be computed
        int curPropMode = transcription.getTimeChangePropagationMode();
        transcription.setNotifying(false);

        if (curPropMode != Transcription.NORMAL) {
        	transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        //get the annotations from the source tier
        List<AnnotationValuesRecord> annValRecordList = getComputedAnnRecord(tierList, transcription );
        
        if(tierList.size() <=0){
        	report("Source tiers not found in this transcription");
        } 
        	
        AlignableAnnotation aa = null;
        int numAnnCreated = 0;
        
        float perAnn = 25f;
        if(processMode == AnnotationFromOverlaps.SINGLE_FILE_MODE){
        	if (annValRecordList.size() > 0) {
            	perAnn = 25 / (float) annValRecordList.size();        	
            }        	
            progressUpdate(70, createAnnotations);
        }
        
        long beginTime;
        long endTime;
        createdAnnos = new ArrayList<AnnotationDataRecord>();
        for( int i=0; i<annValRecordList.size(); i++ ){
        	AnnotationValuesRecord overlap = annValRecordList.get(i);
        	beginTime = overlap.getBeginTime();
            endTime = overlap.getEndTime();
        	if (endTime <= beginTime) {
        		ClientLogger.LOG.warning("Cannot create annotation, begin time >= end time: " + beginTime + " - " + endTime);
        		continue;
        	}
        	aa = (AlignableAnnotation) dTier.createAnnotation(beginTime, endTime);
        	
        	if (aa != null) {
        		switch( annotationValueType ){
	        		//time format
        			case AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_TIME_FORMAT:
        				aa.setValue(getTimeString(endTime - beginTime));
	        			break;
	        		
	        		//specific value
	        		case AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_SPECIFIC_VALUE:
	        			aa.setValue(annWithValue);
	        			break;
	        		
	        		default:
	        			String value = overlap.getNewLabelValue();
	        			if(value == null){
	        				value = overlap.getValue();
	        			}
	        			aa.setValue(value);
	        			break;
        		}
        		numAnnCreated++;
        		// check if we need to set the CvEntryId...
        		if (cv != null) {
        			CVEntry e = cv.getEntryWithValue(aa.getValue());
        			if (e != null) {
        				aa.setCVEntryId(e.getId());
        			}
        		}
        		
        		if(processMode == SINGLE_FILE_MODE){
        			createdAnnos.add(new AnnotationDataRecord(aa));  
        			progressUpdate((int) (70 + (i * perAnn)), createAnnotations);
        		} 
                
            } else {
                ClientLogger.LOG.warning("Annotation could not be created " +
                		beginTime + "-" + endTime);
            }
        }
        report("Number of annotations created: " + numAnnCreated);       
        
        if(processMode == SINGLE_FILE_MODE){
        	ClientLogger.LOG.info("Number of annotations created: " + numAnnCreated );   
        }        
        
        if (transcription.getTimeChangePropagationMode() != curPropMode) {
        	transcription.setTimeChangePropagationMode(curPropMode);
        }

        transcription.setNotifying(true);
        
        return true;
    }

    /**
     * Converts a numeric time value to a (formatted) string.
     *
     * @param time
     *
     * @return a string representation
     */
    private String getTimeString(long time) {
        if (Constants.HHMMSSMS_STRING.equals(timeFormat)) {
            return TimeFormatter.toString(time);
        } else if (Constants.SSMS_STRING.equals(timeFormat)) {
            return TimeFormatter.toSSMSString(time);
        } else if (Constants.HHMMSSFF_STRING.equals(timeFormat)) {// Note: superfluous after introduction of PAL 50fps
        	if( usePalFormat )
        		return TimeFormatter.toTimecodePAL(time);
        	else
        		return TimeFormatter.toTimecodeNTSC(time);
        } else if (Constants.PAL_STRING.equals(timeFormat)) {
        	return TimeFormatter.toTimecodePAL(time);
        } else if (Constants.PAL_50_STRING.equals(timeFormat)) {
        	return TimeFormatter.toTimecodePAL50(time);
        } else if (Constants.NTSC_STRING.equals(timeFormat)) {
        	return TimeFormatter.toTimecodeNTSC(time);
        }
        else {
            return String.valueOf(time);
        }
    }
    
    /**
     * Given a list of tiers, it computes the overlaps of annotations on all selected tiers.
     *  
     * @param tierList the list of tiers where the overlaps are computed for
     * @return a list of overlaps
     */
    protected List<AnnotationValuesRecord> getComputedAnnRecord( List<TierImpl> tierList, TranscriptionImpl transcription){
    	List<AnnotationValuesRecord> overlapList = new ArrayList<AnnotationValuesRecord>();    	
    	
    	//if there are no tiers to be inspected, then there are no overlaps as well
    	if( tierList.size() <= 0 ){
    		//JOptionPane.showMessageDialog(null, "tierList.size() <= 0");
    		return overlapList;
    	}
    	
    	String constraint;
    	TierImpl currentTier;
    	long timeSoFar = 0, beginTime;
    	
    	AnnotationValuesRecord record;
    	
    	/**
    	 * Stage 1: Get overlaps from first tier
    	 */
    	currentTier = tierList.get(0);
    	List<AbstractAnnotation> annotations = currentTier.getAnnotations();
    	
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
    	
    	//JOptionPane.showMessageDialog(null, "Tier with tier name: " + currentTier.getName() + " has " + annotations.size() + " annotations");
    	
    	//if there are no annotations on a tier, then there can be no overlaps
    	if( annotations.size() <= 0 ){
    		//JOptionPane.showMessageDialog(null, "annotations.size() <= 0");
    		return overlapList;
    	}
    	    	
    	constraint = getConstraint(currentTier.getName());
    	
    	//loop over all annotations and store the overlaps
    	for( int i=0; i<annotations.size(); i++ ){
    		AbstractAnnotation abstrAnnotation = annotations.get(i);
    		beginTime = abstrAnnotation.getBeginTimeBoundary();
    		long endTime = abstrAnnotation.getEndTimeBoundary();
    		
    		//begin by viewing annotations as overlaps when the overlaps criteria is different than 
    		//obeying user constraints. If they should obey user constraints, then add them when they
    		//are equal to the constraint or when there is no constraint specified for the tier name.
    		//JOptionPane.showMessageDialog(null, "annotation value = " + abstrAnnotation.getValue() + ", constraint = " + constraint + "\nOVERLAP_CRITERIA_USE_SPECIFIED_CONSTRAINTS = " + OVERLAP_CRITERIA_USE_SPECIFIED_CONSTRAINTS + "\nOverlapsCriteria = " + overlapsCriteria);
    		if( overlapsCriteria != OVERLAP_CRITERIA_USE_SPECIFIED_CONSTRAINTS || constraint == null || 
    				(constraint != null && constraint.equals(abstrAnnotation.getValue())) ){   
    			record = new AnnotationValuesRecord(null, null, beginTime, endTime);
    			record.setNewLabelValue(abstrAnnotation.getValue());
    			overlapList.add(record);   			
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
	    	if( overlapList.size() <= 0 ){
	    		//JOptionPane.showMessageDialog(null, "stage 2: overlapList.size() < 0");
	    		break;
	    	}
	    	
    		currentTier = tierList.get(i);
	    	annotations = currentTier.getAnnotations();
	    	
	    	//JOptionPane.showMessageDialog(null, "Tier with tier name: " + currentTier.getName() + " has " + annotations.size() + " annotations");
	    		
	    	constraint = getConstraint(currentTier.getName());
	    	boolean valueFromCurrentTier = (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_FROM_TIER && currentTier.getName().equals(annFromTier));
	    	
	    	for( int overlapNr = 0; overlapNr < overlapList.size(); overlapNr++ ){
	    		if( annotations.size() <= 0 ){
	    			for( int j=overlapList.size()-1; j>=overlapNr; j-- )
	    				overlapList.remove(j);
	    			//JOptionPane.showMessageDialog(null, "stage 2: break because annotations.size() < 0");
	    			break;
	    		}
	    		
	    		AbstractAnnotation abstrAnnotation = (AbstractAnnotation) annotations.get(0);
	    		beginTime = abstrAnnotation.getBeginTimeBoundary();
	    		long endTime = abstrAnnotation.getEndTimeBoundary();
	    		record = overlapList.get(overlapNr);
	    		
	    		//decide to go on to compute overlaps for this annotation
	    		constraint = getConstraint(currentTier.getName());
	    		
	    		//System.out.print("checkConstraint");
	    		boolean matchConstraint = checkConstraint(record.getNewLabelValue(), abstrAnnotation.getValue(), overlapsCriteria, constraint);
	    		//System.out.println(" - finished (matchConstraint = " + matchConstraint + ")");
	    		
	    		//String value1 = overlapList.get(overlapNr).value;
	    		//String value2 = abstrAnnotation.getValue();
	    		
	    		if( !matchConstraint ){
	    			//JOptionPane.showMessageDialog(null, value1 + " and " + value2 + " does not match constraint");
	    			
	    			if( endTime < record.getEndTime() ){
	    				annotations.remove(0);	    				
	    			}else{
	    				overlapList.remove(overlapNr);	    				
	    			}
	    			overlapNr--;
	    			continue;
	    		}
	    	
	    		//System.out.println("!matchConstraint");
	    		
	    		//if annotation comes before the calculated overlaps
	    		//      |---|
	    		//|---|
	    		if( endTime < record.getBeginTime() ){
	    			//JOptionPane.showMessageDialog(null, "annotation " + value2 + " comes before first annotation " + value1 + ", so remove annotation");
	    			annotations.remove(0);	    			
	    			overlapNr--;
	    			continue;
	    		}
	    		
	    		//  |---|
	    		//|---|
	    		if( beginTime < record.getBeginTime() && endTime <= record.getEndTime()){
	    			//JOptionPane.showMessageDialog(null, "annotation " + value2 + " starts before and ends during first annotation " + value1 + ", update overlap and remove annotation");
	    			if( endTime < record.getEndTime() ){	
	    				record = new AnnotationValuesRecord(null, null, record.getBeginTime(), endTime);
	    				record.setNewLabelValue(overlapList.get(overlapNr).getNewLabelValue());
	    				overlapList.add(overlapNr, record );
	    				overlapList.get(overlapNr+1).setBeginTime(endTime); 
	    			}else{
	    				record.setEndTime(endTime);
	    			}
	    			
	    			String newValue = null;
	    			if (valueFromCurrentTier) {// should be a constant, value from specific tier
	    				newValue = abstrAnnotation.getValue();    					
    				} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME) {
    					newValue = abstrAnnotation.getValue() + " " + record.getNewLabelValue();
    				} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER){
    					newValue = record.getNewLabelValue() + " " + abstrAnnotation.getValue();
    				} else {
    					newValue = record.getNewLabelValue();
    				}
	    			
	    			if(!newValue.equals(record.getNewLabelValue())){
	    				record.setNewLabelValue(newValue);
	    				//overlapList.add(overlapNr, record);
	    			}
	    			
	    			annotations.remove(0);	    			
	    			continue;
	    		}
	    		
	    		//|-------|
	    		//  |---|
	    		if( beginTime >= record.getBeginTime() && endTime <= record.getEndTime()){
	    			//JOptionPane.showMessageDialog(null, "annotation " + value2 + " starts and ends during first annotation " + value1 + ", update overlap and remove annotation");
	    			if( endTime < record.getEndTime() ){	
	    				record = new AnnotationValuesRecord(null, null, beginTime, endTime);
	    				record.setNewLabelValue(overlapList.get(overlapNr).getNewLabelValue());	    				
	    				overlapList.add(overlapNr, record);	    				
	    				overlapList.get(overlapNr+1).setBeginTime(endTime);
	    			}else{	    				
	    				record.setBeginTime(beginTime);	    								
	    			}
	    			
	    			String newValue = null;
	    			if (valueFromCurrentTier) {
	    				newValue = abstrAnnotation.getValue();   
    				} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME || annotationValueType ==AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER ) {
    					newValue = record.getNewLabelValue() +" "+ abstrAnnotation.getValue();
    				} else {
    					newValue = record.getNewLabelValue();
    				}
	    			
	    			if(!newValue.equals(record.getNewLabelValue())){
	    				record.setNewLabelValue(newValue);
	    				//overlapList.add(overlapNr,record);
	    			}
	    			
	    			annotations.remove(0);
	    			continue;
	    		}
	    		
	    		//|----|
	    		//      |--|
	    		if( beginTime >= record.getEndTime() ){
	    			//JOptionPane.showMessageDialog(null, "annotation " + value2 + " starts after first annotation " + value1 + " , remove overlap (ERROR probably)");
	    			overlapList.remove(overlapNr);
	    			overlapNr--;
	    			continue;
	    		}
	    		
	    		//|----|
	    		//  |------|
	    		if( beginTime >= record.getBeginTime() && endTime > record.getEndTime()){
	    			//JOptionPane.showMessageDialog(null, "annotation " + value2 + " starts during and ends after first annotation " + value1 + ", update overlap");
	    			record.setBeginTime(beginTime);
	    			//overlapList.get(overlapNr).value = overlapList.get(overlapNr).value +" "+ abstrAnnotation.getValue();
	    			
	    			String newValue;
	    			if (valueFromCurrentTier) {
	    				newValue =  abstrAnnotation.getValue();
	    			} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME || annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER) {
	    				newValue = record.getNewLabelValue() +" "+ abstrAnnotation.getValue();
	    			} else {
    					newValue = record.getNewLabelValue();
    				}
	    			
	    			if(!newValue.equals(record.getNewLabelValue())){
	    				record.setNewLabelValue(newValue);
	    				//overlapList.add(overlapNr, record);
	    			}
	    			
	    			continue;
	    		}
	    		
	    		//   |----|
	    		//|----------|
	    		if( beginTime < record.getBeginTime() && endTime > record.getEndTime() ){
	    			//JOptionPane.showMessageDialog(null, "annotation " + value2 + " starts before and ends after first annotation " + value1 + ", so do nothing");
	    			String newValue;
 					if (valueFromCurrentTier) {
 						newValue = abstrAnnotation.getValue();
	    			} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIME) {
	    				newValue = abstrAnnotation.getValue() +" "+ record.getNewLabelValue() ;
	    			} else if (annotationValueType == AbstractDestTierAnnValueSpecStepPane.ANN_VALUE_CONCAT_BASED_ON_TIERORDER) {
	    				newValue = record.getNewLabelValue() + " " + abstrAnnotation.getValue();
	    			} else {
	    				newValue = record.getNewLabelValue();
    				}
 					
 					if(!newValue.equals(record.getNewLabelValue())){
 						record.setNewLabelValue(newValue);
 						//overlapList.add(overlapNr, record);
	    			}
	    			continue;
	    		}
	    		
	    		
	    	}
    	}
    	
    	return overlapList;
    }
    
    /**
     * Checks if the overlap value match the specified constraint
     * @param overlapValue the value of the overlap to be checked
     * @param newValue the value of the annotation for which the constraint check is performed
     * @param overlapsCriteria the criteria used to check if constraints are obeyed (overlap, equal value, inequal value, or user specified constraints.
     * @param constraint the value that newValue should have to obey the constraint.
     * @return
     */
    protected boolean checkConstraint(String overlapValue, String newValue, int overlapsCriteria, String constraint){
    	//the only constraint is that it should be an overlap, so return true because values are not relevant
    	if( overlapsCriteria == OVERLAP_CRITERIA_OVERLAP )
    		return true;
    	
    	if( overlapsCriteria == OVERLAP_CRITERIA_VALUES_EQUAL )
    		return overlapValue.equals(newValue);
    	
    	if( overlapsCriteria == OVERLAP_CRITERIA_VALUES_NOT_EQUAL )
    		return !overlapValue.equals(newValue);    	
    	
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
    protected String getConstraint(String tierName) {
    	//System.out.println("Checking if tier " + tierName + " has a constraint");
    	for( int i=0; i<tierValueConstraints.size(); i++ ){
    		//System.out.println("tiervalueConstraints[0] = " + tierValueConstraints.get(i)[0]);
    		
			if( tierValueConstraints.get(i)[0].equals(tierName) ){
				//System.out.println("READY: return value");
				return tierValueConstraints.get(i)[1];
			}
    	}
    	
		//System.out.println("READY: return null");
		return null;
	}
    
    /**
     * Adds a ProgressListener to the list of ProgressListeners.
     *
     * @param pl the new ProgressListener
     */
    public synchronized void addProgressListener(ProgressListener pl) {
        if (listeners == null) {
            listeners = new ArrayList<ProgressListener>(2);
        }

        listeners.add(pl);
    }

    /**
     * Removes the specified ProgressListener from the list of listeners.
     *
     * @param pl the ProgressListener to remove
     */
    public synchronized void removeProgressListener(ProgressListener pl) {
        if ((pl != null) && (listeners != null)) {
            listeners.remove(pl);
        }
    }

    /**
     * Notifies any listeners of a progress update.
     *
     * @param percent the new progress percentage, [0 - 100]
     * @param message a descriptive message
     */
    protected void progressUpdate(int percent, String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressUpdated(this,
                    percent, message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has completed.
     *
     * @param message a descriptive message
     */
    protected void progressComplete(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressCompleted(this,
                    message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has been interrupted.
     *
     * @param message a descriptive message
     */
    protected void progressInterrupt(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressInterrupted(this,
                    message);
            }
        }
    }
    
    /**
     * Returns the proces report.
     *
     * @return the process report, or null
     */
    @Override
	public ProcessReport getProcessReport() {
        return report;
    }

    /**
     * Sets the process report.
     *
     * @param report the new report to append messages to
     */
    @Override
	public void setProcessReport(ProcessReport report) {
        this.report = report;
    }

    /**
     * Adds a message to the report.
     *
     * @param message the message
     */
    @Override
	public void report(String message) {
        if (report != null) {
            report.append(message);
        }
    }
    
    /**
     * A thread class that compares the annotations on all the tiers, detects the
     * overlaps and  creates a new annotation on a new tier, with the
     * duration of the overlap.
     */
    private class CalcOverLapsThread extends Thread {
        /**
         * Creates a new thread to calculate the overlaps.
         */
        public CalcOverLapsThread() {
            super();
        }

        /**
         * Creates a new thread to calculate the overlaps.
         *
         * @param name the name of the thread
         */
        public CalcOverLapsThread(String name) {
            super(name);
            
        }

        /**
         * Interrupts the current calculation process.
         */
        @Override
		public void interrupt() {
            super.interrupt();
            progressInterrupt("Operation interrupted...");           
            report("Operation interrupted...");
        }

        /**
         * The actual action of this thread.
         *
         * @see java.lang.Runnable#run()
         */
        @Override
		public void run() {
        	//create annotations
            if( processMode == SINGLE_FILE_MODE ){           	
            	
        		//use currently opened transcription
        		createAnnotations(transcription);
        		progressComplete("Operation complete...");
            }
        	else if( processMode == MULTI_FILE_MODE ){
        		//transcription based on files
        		
        		float perFile = 25f;
                if (filenames.length > 0) {
               	 	perFile = 25 / (float) filenames.length;        	
                }                     
                
                String total = "Total no of files : " + filenames.length;
            	String processed = "Processed files : ";
            	String processing = "processing file : ";
            	TranscriptionImpl t;
            	String buffer = null;
            	boolean annotationsCreated;
        		for( int i = 0; i< filenames.length; i++){
        			t = new TranscriptionImpl(filenames[i]);
        			report("\nFile " + i + " : " + t.getName());        			
        			buffer = total+ "\n" + processed +i+ "\n" + processing + t.getName();
        			progressUpdate((int) (i * perFile), buffer);       		
        			annotationsCreated = createAnnotations(t);
        			String directoryToSave = t.getPathName();

        			try{
        				if(annotationsCreated){
            				int saveAsType = SaveAs27Preferences.saveAsType(t);

        					ACMTranscriptionStore.getCurrentTranscriptionStore().storeTranscriptionIn(
        							t, null, new ArrayList<TierImpl>(), directoryToSave, saveAsType);
        					numChanged++;
        				}
        			
        			}catch(IOException e){
    					LOG.warning("Can not write transcription to file with directory/filename: " + directoryToSave );
    					if(annotationsCreated){
    						numFailed++;        					}
    										
    					report("Can not write transcription ' " + t.getName() +"' to file with directory/filename: " + directoryToSave );
        			}
        			buffer = total+ "\n" + processed +(i+1);
        		}
        		
        		progressComplete(buffer + '\n' + "Operation complete...");
        		
        		 report("\nSummary: ");
        	        report("Number of files in domain:  " + filenames.length);
        	        //report("Number of files inspected:  " + numInspected);
        	        report("Number of files changed:  " + numChanged);
        	        report("Number of files failed:  " + numFailed);
        	}  
            
            new ReportDialog(report).setVisible(true);
        }
    }
}

    

    
    
    
