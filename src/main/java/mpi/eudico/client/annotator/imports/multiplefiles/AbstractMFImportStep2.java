package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;


/**
 * Step 2: Abstract Step pane for setting the import options
 * 
 * @author aarsom
 * @version May, 2012
 */
public abstract class AbstractMFImportStep2 extends StepPane{

	public AbstractMFImportStep2(MultiStepPane multiPane) {
		super(multiPane);
	}
	
	/**
	  * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
	  */
	@Override
	public void enterStepBackward() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	    multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
	}
	
	@Override
	public void enterStepForward(){	
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	    multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
	}	
	
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("MultiFileImport.Step2.Title");
	}

}
