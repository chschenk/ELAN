package mpi.eudico.client.annotator.tier;

import java.util.List;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.AnnotationsFromOverlapsUndoableCommand;
import mpi.eudico.client.annotator.commands.AnnotationsFromSubtractionUndoableCommand;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * Panel for Step 5: The final step and actual calculation.  A command is created and this pane
 * is connected as progress listener. The ui is a progress monitor.
 *
 * @author Han Sloetjes , Jeffrey Lemein
 * @author aarsom
 * @version November, 2011
 */
@SuppressWarnings("serial")
public class OverlapsOrSubtractionStep5 extends AbstractProgressStepPane {
	
	protected List<String> files;
	protected List<String> selectedTiers; 
	
	
	protected String destTierName;	
	protected String destLingType;
	
	protected String parentTierName;
	
	protected int annotationValueType;
	
	protected String timeFormat;
	protected boolean usePalFormat;
	protected String annWithValue;
	protected String annFromTier;
	
	protected int processMode;
	protected List<String> tierOrder;
	
	protected int overlapsCriteria;
	protected List<String[]> tierValuePairs;
	
	protected Transcription transcription;
    private boolean subtractionDialog;

    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public OverlapsOrSubtractionStep5(MultiStepPane multiPane, Transcription transcription) {
       this(multiPane, transcription, false);
    }
    
    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public OverlapsOrSubtractionStep5(MultiStepPane multiPane, Transcription transcription, boolean subtractionDialog) {
        super(multiPane);
        this.subtractionDialog = subtractionDialog;
        this.transcription = transcription;
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.Calculating");
    }
    
    /**
     * Calls doFinish.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
	@Override
	public void enterStepForward() {
		files = (List<String>) multiPane.getStepProperty("OpenedFiles");
		selectedTiers = (List<String>) multiPane.getStepProperty("SelectedTiers");               
	    destTierName = (String) multiPane.getStepProperty("DestinationTierName");
	    destLingType = (String) multiPane.getStepProperty("linguisticType");	
	  	parentTierName = (String) multiPane.getStepProperty("ParentTierName");
	  	
	  	Integer avType = (Integer) multiPane.getStepProperty("AnnotationValueType");
	  	if (avType != null) {
	  		annotationValueType = avType;
	  	}
	  	
	  	timeFormat = (String) multiPane.getStepProperty("TimeFormat");
	  	
	  	Boolean usePal = (Boolean) multiPane.getStepProperty("UsePalFormat");
	  	if (usePal != null) {
	  		usePalFormat = usePal;
	  	}
		
		
		annWithValue = (String) multiPane.getStepProperty("AnnotationValue");		
		annFromTier = (String) multiPane.getStepProperty("AnnFromTier");
		
		Integer procMode = (Integer) multiPane.getStepProperty("ProcessMode");
		if (procMode != null) {
			processMode = procMode;
		}
		
		tierOrder = (List<String>) multiPane.getStepProperty("TierOrder");
		
		Integer olCrit = (Integer) multiPane.getStepProperty("overlapsCriteria");
		if (olCrit != null) {
			overlapsCriteria = olCrit;
		}
		tierValuePairs = (List<String[]>) multiPane.getStepProperty("tierValueConstraints");
		
		super.enterStepForward();
    }


	@Override
	public void startProcess() {
		String sourceTiers[] = new String[selectedTiers.size()];
		for( int i=0; i<selectedTiers.size(); i++ ){
			sourceTiers[i] = selectedTiers.get(i);		
		}
				
		Object[] filenames = null;
		if( files != null ){
			filenames = files.toArray();
		}
				
		Object args[] = null;
		AnnotationFromOverlaps com = null;
		if(subtractionDialog){
			//referenceTierName
			String referenceTierName = (String) multiPane.getStepProperty("ReferenceTierName");
					
			args = new Object[]{ sourceTiers, destTierName, destLingType,
					annotationValueType, timeFormat, annWithValue, processMode, filenames,
					usePalFormat, parentTierName, referenceTierName };
					
			// create a command and connect as listener
			if( transcription != null ) {
				com = (AnnotationsFromSubtractionUndoableCommand)ELANCommandFactory.createCommand(transcription, ELANCommandFactory.ANN_FROM_SUBTRACTION);
			} else{
				com = new AnnotationFromSubtraction(ELANCommandFactory.ANN_FROM_OVERLAP);
					
			}			  
		}else{					
			args = new Object[]{ sourceTiers, destTierName, destLingType,
					annotationValueType, timeFormat, annWithValue, annFromTier, tierOrder, 
					overlapsCriteria, tierValuePairs, processMode, filenames,
					usePalFormat, parentTierName };
				
			// create a command and connect as listener
			if( transcription != null ) {
				com = (AnnotationsFromOverlapsUndoableCommand)ELANCommandFactory.createCommand(transcription, ELANCommandFactory.ANN_FROM_OVERLAP);
			} else{
				com = new AnnotationFromOverlaps(ELANCommandFactory.ANN_FROM_OVERLAP);				
			}  
		}
		com.addProgressListener(this);		
		com.execute(transcription, args);
	}
}
