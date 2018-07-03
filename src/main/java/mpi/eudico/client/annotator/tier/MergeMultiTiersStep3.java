package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
* Panel for step 3 : Destination tier specification panel
* 
* This panel allows to specify the attributes like tier name, linguistic type
* for the new destination tier
*
* @author aarsom
* @version Feb, 2014
*/
public class MergeMultiTiersStep3 extends AbstractDestTierAndTypeSpecStepPane{

	/**
	 * Constructor
	 * 
	 * @param mp
	 * @param trans
	 */
	public MergeMultiTiersStep3(MultiStepPane mp, TranscriptionImpl trans) {
		super(mp, trans);
	}

	/**
	 * Title for this panel
	 */
	@Override
	public String getStepTitle(){	
		return ElanLocale.getString("MergeTiers.Title.Step3");
	}
	
	/**
	 * 
	 */
	@Override
	public void enterStepForward(){		
		super.enterStepForward();
		parentTierCB.setEnabled(false);
		childTierRB.setEnabled(false);
	}
}
