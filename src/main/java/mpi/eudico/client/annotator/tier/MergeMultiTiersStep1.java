package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Panel for step 1: File and tier selection
 * 
 * Allows to select multiple files and tiers which has to be merged
 * 
 * @author aarsom
 * @version Feb, 2014
 */
public class MergeMultiTiersStep1 extends AbstractFileAndTierSelectionStepPane{	
		
	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcription	  
	 */
	public MergeMultiTiersStep1(MultiStepPane mp, TranscriptionImpl transcription){
		super(mp, transcription);
	}
		
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
	@Override
	public String getStepTitle(){	
		return ElanLocale.getString("MergeTiers.Title.Step1");
	}	
	
}
