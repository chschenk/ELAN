package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Panel for step 1: File and tier selection
 * 
 * Allows to select multiple files and multiple tiers on which the overlaps or 
 * subtract are to be computed
 * 
 * @author aarsom
 * @version November, 2011
 */
public class OverlapsOrSubtractionStep1 extends AbstractFileAndTierSelectionStepPane{	
	boolean subtractionDialog;
	
	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcription	  
	 */
	public OverlapsOrSubtractionStep1(MultiStepPane mp, TranscriptionImpl transcription){
		this(mp, transcription , false);
	}
	
	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcription
	 * @param subtractionDialog, it true refers to subtraction process
	 */
	public OverlapsOrSubtractionStep1(MultiStepPane mp, TranscriptionImpl transcription, boolean subtractionDialog){
		super(mp, transcription);
		this.subtractionDialog = subtractionDialog;
	}
		
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
	@Override
	public String getStepTitle(){		
		return ElanLocale.getString("OverlapsDialog.Title.Step1Title");
	}	
	
}