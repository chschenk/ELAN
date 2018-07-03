package mpi.eudico.client.annotator.gui.multistep;

/**
 * Step. Defines methods for a single step in a multiple step process.
 *
 * @author Han Sloetjes
 */
public interface Step {
    /**
     * Returns the step title, which can be used for a short description.
     *
     * @return the tile of the step
     */
    public String getStepTitle();

    /**
     * Notification that this step is activated while stepping forward.
     * Some initialization could be done here.
     */
    public void enterStepForward();

    /**
     * Notification that this step is activated while stepping backward.
     */
    public void enterStepBackward();

    /**
     * Notification that this step is de-activated while stepping forward.
     *
     * @return true if this step's condition for stepping forward have been met,
     * false otherwise
     */
    public boolean leaveStepForward();

    /**
     * Notification that this step is de-activated while stepping backward.
     *
     * @return true if this step's condition for stepping backward have been met,
     * false otherwise
     */
    public boolean leaveStepBackward();
    
    /**
     * A step can specify which step should be the next and thus circumvent the 
     * default next step behavior. 
     * 
     * @return the identifier of the preferred next step, can be null
     */
    public String getPreferredNextStep();
    
    /**
     * Sets the step that should be the next, overruling the default next step behavior. 
     * 
     * @param the identifier of the preferred next step
     */
    public void setPreferredNextStep(String nextStepName);
    
    /**
     * A step can specify which step should be the previous, the step back, and thus 
     * circumvent the default previous step behavior. 
     * 
     * @return the identifier of the preferred step back, can be null 
     */
    public String getPreferredPreviousStep();
    
    /**
     * Sets the step that should be the previous step, overruling the default previous step behavior. 
     * 
     * @param the identifier of the preferred previous step
     */
    public void setPreferredPreviousStep(String previousStepName);

    /**
     * Notification that the whole process has been cancelled. 
     * Clean up.
     */
    public void cancelled();

    /**
     * Notification that the whole process has been finished. 
     * Clean up.
     */
    public void finished();

    /**
     * Perform the final action(s) that end(s) this process.  
     *
     * @return true when the action has been finished successfully
     */
    public boolean doFinish();
    
    /**
     * Invitation to show help information about this step or the whole process,
     * if available.
     */
    public void showHelp();
    
    /**
     * Sets the name or identifier for this step. Intended for internal use, the name
     * is not meant to be displayed on screen.
     * 
     * @param name the name or id
     */
    public void setName(String name);
        
    /**
     * Returns the name or identifier of this step.
     * 
     * @return the name or identifier of this step
     */
    public String getName();
}
