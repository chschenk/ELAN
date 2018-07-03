package mpi.eudico.client.annotator.gui.multistep;

import java.util.Set;


/**
 * MultiStepControl. Methods controlling navigation and actions 
 * in a multiple step process (wizard).
 *
 * @author Han Sloetjes
 */
public interface MultiStepControl {
    /**
     * Called when the "Next" button is clicked.
     */
    public void nextStep();

    /**
     * Called when the "Back" button is clicked.
     */
    public void previousStep();

    /**
     * Called when the "Finish" button is clicked, or "Next" on the last panel.
     */
    public void finish();

    /**
     * Called when the "Cancel" button is clicked.
     */
    public void cancel();

    /**
     * Called when the "Help" button is clicked.
     */
    public void showHelp();

    /**
     * Returns the index of the current step in a multiple step process.
     *
     * @return the index of the current step in a multiple step process
     */
    public int getCurrentStepIndex();

    /**
     * Returns the current step. 
     *
     * @return the current step.
     */
    public StepPane getCurrentStep();

    /**
     * Initiates a jump to the step at the specified index.
     *
     * @param stepIndex the index of the step to activate
     * @param forward if true this is considered a step forward, otherwise backward
     */
    public void goToStep(int stepIndex, boolean forward);

    /**
     * Initiates a jump to the step identified by the specified name.
     *
     * @param name the identifier of the step to activate
     * @param forward if true this is considered a step forward, otherwise backward
     */
    public void goToStep(String name, boolean forward);

    /**
     * Each step can store objects in a HashMap for use by other steps.
     *
     * @param key the key
     * @param value the value
     */
    public void putStepProperty(Object key, Object value);

    /**
     * Each step can retrieve objects from a HashMap, stored by other steps.
     *
     * @param key the key
     *
     * @return the objet
     */
    public Object getStepProperty(Object key);

    /**
     * Returns an enumeration of all step keys.
     *
     * @return an enumeration of all stored step keys
     */
    public Set getPropertyKeys();
}
