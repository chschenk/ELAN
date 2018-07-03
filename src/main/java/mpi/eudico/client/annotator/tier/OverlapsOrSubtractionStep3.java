package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Panel for step 3 : Destination tier specification panel
 * 
 * This panel allows to specify the attributes like tier name, linguistic type
 * and the parent tier for the new destination tier
 *
 * @author Jeffrey Lemein
 * @author aarsom
 * @version November, 2011
 */
public class OverlapsOrSubtractionStep3 extends AbstractDestTierAndTypeSpecStepPane{	
	boolean subtractionDialog;
	
	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcription
	 */
	public OverlapsOrSubtractionStep3(MultiStepPane mp, TranscriptionImpl transcription){
		this(mp, transcription , false);
	}
	
	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcription
	 * @param subtractionDialog, if true specify the subtraction process
	 */
	public OverlapsOrSubtractionStep3(MultiStepPane mp, TranscriptionImpl transcription, boolean subtractionDialog){
		super(mp, transcription);
		this.subtractionDialog = subtractionDialog;
	}
	
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("OverlapsDialog.Title.Step3Title");
	}
	
	@Override
	public void enterStepForward(){		
		super.enterStepForward();
		if(subtractionDialog){
			parentTierCB.setEnabled(false);
			String parentTierName = (String) multiPane.getStepProperty("ReferenceTierName");					
			if(parentTierName != null){			
				parentTierCB.setSelectedItem(parentTierName);
				childTierRB.setEnabled(true);
			}else{
				childTierRB.setEnabled(false);
			}
		}    	
	}
	
	/**
	 * Updates the button states according to some constraints (like everything has to be filled in, consistently)
	 */
	@Override
	public void updateButtonStates(){		
		super.updateButtonStates();
		if(subtractionDialog){
			parentTierCB.setEnabled(false);
		}
	}
}