package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

/**
 * Panel for Step 4: Specify the value of the 
 * resulting merged annotations 
 * 
 * @author aarsom
 * @version Feb, 2014
 */
public class MergeMultiTiersStep4 extends AbstractDestTierAnnValueSpecStepPane{

	/**
	 * Constructor
	 * 
	 * @param mp
	 */
	public MergeMultiTiersStep4(MultiStepPane mp) {
		super(mp);
	}
	
	/**
	 * Title for this panle
	 */
	@Override
	public String getStepTitle(){	
		return ElanLocale.getString("MergeTiers.Title.Step4");
	}
	
	/**     
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
	@Override
	public void enterStepForward(){		
		boolean overlapOnly = (Boolean)multiPane.getStepProperty("OnlyProcessOverlapingAnnotations");
		tierValueRadioButton.setEnabled(overlapOnly);
		tierSelectBox.setEnabled(overlapOnly);
				
		super.enterStepForward();
	}	
	
	
}
