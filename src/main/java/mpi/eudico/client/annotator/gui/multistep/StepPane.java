package mpi.eudico.client.annotator.gui.multistep;

import javax.swing.JPanel;


/**
 * StepPane. A Panel representing one of the steps in a multiple step 
 * process.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class StepPane extends JPanel implements Step {
    /** The MultiStepPane this step is added to */
    protected MultiStepPane multiPane;
    /** The internal name or id of a pane. Can be used to refer to a pane of which the index is not known. */
    protected String name;
    /** The internal name or id of the "next" pane */
    protected String nextStep;
    /** The internal name or id of the "previous" pane */
    protected String previousStep;

    /**
     * Creates a new StepPane instance.
     *
     * @param multiPane the 'parent' pane this step is added to
     */
    public StepPane(MultiStepPane multiPane) {
        super();
        this.multiPane = multiPane;
    }

    /**
     * Initializes the components of the step ui.
     */
    protected void initComponents() {
    }

    /**
     * Returns the title of the step.
     * 
     * @return the title of the step
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return "";
    }

    /**
     * Notification that this step will become the active step, moving up.
     * The step can perform some preparations.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
    }

    /**
     * Notification that this step will become the active step, moving down.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
    }

    /** 
     * Notification that this step will no longer be the active step, moving up.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        return true;
    }

    /**
     * Notification that this step will no longer be the active step, moving down.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#leaveStepBackward()
     */
    @Override
	public boolean leaveStepBackward() {
        return true;
    }

    /**
     * A step can specify which step should be the next and thus circumvent the 
     * default next step behavior. 
     * 
     * @return the identifier of the preferred next step, can be null
     */
	@Override
	public String getPreferredNextStep() {
		return nextStep;
	}

    /**
     * Sets the step that should be the next, overruling the default next step behavior. 
     * 
     * @param the identifier of the preferred next step
     */
	@Override
	public void setPreferredNextStep(String nextStepName) {
		nextStep = nextStepName;
	}
	
    /**
     * A step can specify which step should be the previous, the step back, and thus 
     * circumvent the default previous step behavior. 
     * 
     * @return the identifier of the preferred step back, can be null 
     */
	@Override
	public String getPreferredPreviousStep() {
		return previousStep;
	}
	
    /**
     * Sets the step that should be the previous step, overruling the default previous step behavior. 
     * 
     * @param the identifier of the preferred previous step
     */
	@Override
	public void setPreferredPreviousStep(String previousStepName) {
		previousStep = previousStepName;
	}

	/**
     * Called when the process has been cancelled. 
     * The step can clean up resources etc.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#cancelled()
     */
    @Override
	public void cancelled() {
        
    }

    /**
     * Called when to process has finished. 
     * The step can clean up etc.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#finished()
     */
    @Override
	public void finished() {
        
    }

    /**
     * Called when this step should perform the finishing action. The step 
     * can delegate the action to one of the other steps.
     * 
     * @return true if the process has finished successfully, false otherwise
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
        return true;
    }
    
    /**
     * Show help information of some form. Or ignore.
     */
    @Override
	public void showHelp() {
    	
    }
    
    /**
     * Sets the name or identifier for this step. Intended for internal use, the name
     * is not meant to be displayed on screen.
     * 
     * @param name the name or id
     */
    @Override
	public void setName(String name) {
    	this.name = name;
    }
        
    /**
     * Returns the name or identifier of this step.
     * 
     * @return the name or identifier of this step, can be null
     */
    @Override
	public String getName() {
    	return name;
    }
}
