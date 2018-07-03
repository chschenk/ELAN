package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;

/**
 * The final step for a step pane dialog and where the actual process starts. 
 * Shows the progress information
 * 
 * @author aarsom
 * @version Feb 2014
 *
 */
public abstract class AbstractProgressStepPane extends ProgressStepPane {	
	
	/**
    * Constructor
    *
    * @param multiPane the container pane
    */
	public AbstractProgressStepPane(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();
	}

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	abstract public String getStepTitle();
    
    /**
     * Calls doFinish.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
	@Override
	public void enterStepForward() {		
		// disable buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
        doFinish();
    }
	
	  /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
    	completed = false;
        
    	startProcess();
		
		// the action is performed on a separate thread, don't close
        return false;
    }
    
    abstract public void startProcess();
}
