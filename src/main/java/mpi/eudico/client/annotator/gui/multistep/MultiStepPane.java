package mpi.eudico.client.annotator.gui.multistep;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import mpi.eudico.client.annotator.gui.ClosableDialog;


/**
 * The main panel holding ui elements of a multiple step process.
 * Uses a CardLayout to manage a number of subpanels each representing one 
 * of the steps in the process. 
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class MultiStepPane extends JComponent implements MultiStepControl,
    ActionListener {
    /** Constant for a 'help' button */
    public static final int HELP_BUTTON = 0;

    /** Constant for the 'next' button */
    public static final int NEXT_BUTTON = 1;

    /** Constant for the 'previous' button */
    public static final int PREVIOUS_BUTTON = 2;

    /** Constant for the 'finish' button */
    public static final int FINISH_BUTTON = 3;

    /** Constant for the 'cancel' button */
    public static final int CANCEL_BUTTON = 4;

    /** Constant for all buttons */
    public static final int ALL_BUTTONS = 8;
    private ResourceBundle bundle;
    private JButton helpButton;
    private JButton nextButton;
    private JButton prevButton;
    private JButton finishButton;
    private JButton cancelButton;

    /** Constant for the prreferred panel dimension */
    private final Dimension prefSize = new Dimension(500, 400);

    /** Constant for the preferred height of the top panel or 
     * the title panel */
    private final int prefTitlePanelHeight = 70;

    /** Constant for the preferred height of the control buttons panel */
    private final int prefButtonPanelHeight = 60;
    private Insets insets;
    private StepTitlePanel titlePanel;
    private JPanel stepContainer;
    private CardLayout stepLayout;
    private JPanel buttonPanel;
    private JDialog dialog;
    private HashMap<Integer, StepPane> stepMap;
    private HashMap stepProperties;
    private int currentStepIndex = 0;
    private StepPane currentStep;

    /**
     * Creates a new MultiStepPane instance.
     */
    public MultiStepPane() {
        initComponents();
    }

    /**
     * Creates a new MultiStepPane instance, taking strings from the resource 
     * bundle for labels and buttons etc.
     *
     * @param bundle a resource bundle holding (localized) string for ui elements
     */
    public MultiStepPane(ResourceBundle bundle) {
        this.bundle = bundle;
        initComponents();
    }

    /**
     * Initializes the panel and its components.
     */
    protected void initComponents() {
        stepMap = new HashMap<Integer, StepPane>();
        stepProperties = new HashMap();
        setLayout(new GridBagLayout());
        setPreferredSize(prefSize);
        setMinimumSize(prefSize);
        insets = new Insets(0, 0, 0, 0);
        titlePanel = new StepTitlePanel();
        titlePanel.setPreferredSize(new Dimension(getPreferredSize().width,
                prefTitlePanelHeight));
        titlePanel.setMinimumSize(new Dimension(getPreferredSize().width,
                prefTitlePanelHeight));
        titlePanel.setBackground(Color.white);
        titlePanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        add(titlePanel, gridBagConstraints);

        stepLayout = new CardLayout();
        stepContainer = new JPanel(stepLayout);
        stepContainer.setPreferredSize(prefSize);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        add(stepContainer, gridBagConstraints);

        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Color.DARK_GRAY),
                new EmptyBorder(6, 6, 6, 6)));
        helpButton = new JButton();
        nextButton = new JButton();
        prevButton = new JButton();
        finishButton = new JButton();
        cancelButton = new JButton();
        helpButton.addActionListener(this);
        nextButton.addActionListener(this);
        prevButton.addActionListener(this);
        finishButton.addActionListener(this);
        cancelButton.addActionListener(this);

        Insets buttonInsets = new Insets(4, 4, 4, 4);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.insets = buttonInsets;
        buttonPanel.add(helpButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = buttonInsets;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(new JPanel(), gridBagConstraints);

        JPanel buttonGroupPanel = new JPanel(new GridLayout(1, 4, buttonInsets.left,
		buttonInsets.top));
        buttonGroupPanel.add(prevButton);
        buttonGroupPanel.add(nextButton);
        buttonGroupPanel.add(finishButton);
        buttonGroupPanel.add(cancelButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.insets = buttonInsets;
        buttonPanel.add(buttonGroupPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        add(buttonPanel, gridBagConstraints);

        helpButton.setEnabled(false);
        helpButton.setVisible(false);
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
        finishButton.setEnabled(false);

        updateLocale();
    }

    /**
     * Applies localized strings from a bundle or uses default values if no 
     * bundle is specified.
     */
    public void updateLocale() {
        if (bundle != null) {
            helpButton.setText(bundle.getString("MultiStep.Help"));
            prevButton.setText(bundle.getString("MultiStep.Previous"));
            nextButton.setText(bundle.getString("MultiStep.Next"));
            finishButton.setText(bundle.getString("MultiStep.Finish"));
            cancelButton.setText(bundle.getString("MultiStep.Cancel"));
        } else {
            helpButton.setText("Help");
            prevButton.setText("Previous");
            nextButton.setText("Next");
            finishButton.setText("Finish");
            cancelButton.setText("Cancel");
        }
    }

    /**
     * Adds a step panel to the end of the list of panels.  
     *
     * @param step the step panel to add
     */
    public void addStep(StepPane step) {
        if (step == null) {
            return; // NullPointerException?
        }

        int curSize = stepMap.size();
        stepContainer.add(step, "" + curSize);
        stepMap.put(Integer.valueOf(curSize), step);

        if (curSize == 0) {
            currentStepIndex = 0;
            currentStep = step;
            stepLayout.show(stepContainer, "" + currentStepIndex);

            if (titlePanel != null) {
                titlePanel.setTitleText(currentStep.getStepTitle());
            }

            currentStep.enterStepForward();
        }
    }

    /**
     * Called when the "Next" button is clicked. Brings the next step panel 
     * to front in the cardlayout. If the current step specifies a different
     * step to jump to, this takes precedence.
     */
    @Override
	public void nextStep() {
    	if (currentStep.getPreferredNextStep() != null) {
    		int tmpIndex = getIndexForStepName(currentStep.getPreferredNextStep());
    		if (tmpIndex > -1) {
    			goToStep(tmpIndex, true);
    			return;
    		}
    	}
    	
        if (currentStepIndex < (stepMap.size() - 1)) {
            if (currentStep.leaveStepForward()) {
                stepLayout.next(stepContainer);
                currentStepIndex++;

                prevButton.setEnabled(true);
                nextButton.setEnabled(false);
                currentStep = stepMap.get(Integer.valueOf(
                            currentStepIndex));

                if (titlePanel != null) {
                    titlePanel.setTitleText(currentStep.getStepTitle());
                }

                currentStep.enterStepForward();
            }
        } else if (currentStepIndex == stepMap.size()) {
            finish();
        }
    }

    /**
     * Called when the "Previous" button is clicked. Brings the previous step panel 
     * to front in the cardlayout. If the current step specifies a different
     * step to jump back to, this takes precedence.
     */
    @Override
	public void previousStep() {
    	if (currentStep.getPreferredPreviousStep() != null) {
    		int tmpIndex = getIndexForStepName(currentStep.getPreferredPreviousStep());
    		if (tmpIndex > -1) {
    			goToStep(tmpIndex, false);
    			return;
    		}
    	}
    	
        if ((currentStepIndex > 0) && (stepMap.size() > 0)) {
            if (currentStep.leaveStepBackward()) {
                stepLayout.previous(stepContainer);
                currentStepIndex--;

                if (currentStepIndex == 0) {
                    prevButton.setEnabled(false);
                }

                nextButton.setEnabled(false);
                currentStep = stepMap.get(Integer.valueOf(
                            currentStepIndex));

                if (titlePanel != null) {
                    titlePanel.setTitleText(currentStep.getStepTitle());
                }

                currentStep.enterStepBackward();
            }
        }
    }

    /**
     * Called when the "Finish" button has been clicked. Invokes the doFinish method on 
     * the current step panel. The current Step returns true when the action of that step 
     * has successfully been performed.It returns false when an error occurred or when a 
     * process is still going on (and the step wants to monitor it). In that case the step 
     * should explicitely call the close() method of this class.
     * By default the finished() method on all steps will then be invoked, allowing each 
     * step to clean up.
     *  
     * @see Step#doFinish()
     */
    @Override
	public void finish() {
        if (currentStep.doFinish()) {
            // notify all steps that the process has finished
        		finishNotify();
             if (dialog != null) {
                 dialog.setVisible(false);
                 //dialog.hide();
             }
        }
    }

    /**
     * Called when the "Cancel" button has been clicked. 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.MultiStepControl#cancel()
     */
    @Override
	public void cancel() {
    		// notify all steps that the process has been canceled / finished
    		finishNotify();
        if (dialog != null) {
            dialog.setVisible(false);
            //dialog.hide();
        }
    }
    
    /**
     * Can be called by any step to close the dialog (if any).
     * When the last step has monitored the process (and doFinish() returned false) the
     * step can call this method at the end of the process.
     */
    public void close() {
    		cancel();
    }

    /**
     * Called when the "Help" button has been clicked. Invokes showHelp on the current 
     * step. It is up to the step to show some kind of help or to ignore the call.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.MultiStepControl#showHelp()
     */
    @Override
	public void showHelp() {
        currentStep.showHelp();
    }

    /**
     * Adds a key-value pair to the list of properties.
     * 
     * @param key the key
     * @param value the value
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.MultiStepControl#putStepProperty(java.lang.String, java.lang.Object)
     */
    @Override
	public void putStepProperty(Object key, Object value) {
        stepProperties.put(key, value);
    }

    /**
     * Returns the value to which the key is mapped.
     * 
     * @param key the key
     * @return the associated value
     */
    @Override
	public Object getStepProperty(Object key) {
        return stepProperties.get(key);
    }

    /**
     * Returns a keyset of the properties map.
     * 
     * @return a keyset of the properties map
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.MultiStepControl#getPropertyKeys()
     */
    @Override
	public Set<Object> getPropertyKeys() {
        return stepProperties.keySet();
    }

    /**
     * Returns the index of the current step.
     * 
     * @return the index of the current step
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.MultiStepControl#getCurrentStepIndex()
     */
    @Override
	public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    /**
     * Returns the current step.
     *
     * @return the current step
     */
    @Override
	public StepPane getCurrentStep() {
        return currentStep;
    }

    /**
     * Jumps to the step at the specified index.
     * 
     * @param stepIndex the index of the step to jump to
     * @param forward if true the current step's leaveStepForward will be called etc.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.MultiStepControl#goToStep(int)
     */
    @Override
	public void goToStep(int stepIndex, boolean forward) {
        if ((stepIndex >= 0) && (stepIndex < stepMap.size())) {
        	if (forward) {
	            if (currentStep.leaveStepForward()) {
	                stepLayout.show(stepContainer, String.valueOf(stepIndex));
	                currentStepIndex = stepIndex;
	
	                if (currentStepIndex == 0) {
	                    prevButton.setEnabled(false);
	                } else {
	                	prevButton.setEnabled(true);
	                }
	                nextButton.setEnabled(false);
	                currentStep = stepMap.get(currentStepIndex);
	
	                if (titlePanel != null) {
	                    titlePanel.setTitleText(currentStep.getStepTitle());
	                }
	
	                currentStep.enterStepForward();
	            }
        	} else {
	            if (currentStep.leaveStepBackward()) {
	                stepLayout.show(stepContainer, String.valueOf(stepIndex));
	                currentStepIndex = stepIndex;
	
	                if (currentStepIndex == 0) {
	                    prevButton.setEnabled(false);
	                } else {
	                	prevButton.setEnabled(true);
	                }
	                nextButton.setEnabled(false);
	                currentStep = stepMap.get(currentStepIndex);
	
	                if (titlePanel != null) {
	                    titlePanel.setTitleText(currentStep.getStepTitle());
	                }
	
	                currentStep.enterStepBackward();
	            }
        	}
        }
    }

    /**
     * Jumps to the step identified by name.
     *
     * @param name identifier of the step
     * @param forward if true the current step's leaveStepForward will be called etc.
     */
    @Override
	public void goToStep(String name, boolean forward) {
        String reqIndex = null;
        int tempIndex = getIndexForStepName(name);
        
        if (tempIndex > -1) {
        	reqIndex = String.valueOf(tempIndex);
        }

        if (reqIndex != null) {  
        	if (forward) {
	            if (currentStep.leaveStepForward()) {
	            	stepLayout.show(stepContainer, reqIndex);
	
	                if (currentStepIndex == 0) {
	                    prevButton.setEnabled(false);
	                } else {
	                	prevButton.setEnabled(true);
	                }
	                nextButton.setEnabled(false);
	                currentStepIndex = tempIndex;
	                currentStep = stepMap.get(currentStepIndex);
	
	                if (titlePanel != null) {
	                    titlePanel.setTitleText(currentStep.getStepTitle());
	                }
	
	                currentStep.enterStepForward();
	            }
        	} else {
        		 if (currentStep.leaveStepBackward()) {
 	            	stepLayout.show(stepContainer, reqIndex);
 	
 	                if (currentStepIndex == 0) {
 	                    prevButton.setEnabled(false);
 	                } else {
 	                	prevButton.setEnabled(true);
 	                }
 	                nextButton.setEnabled(false);
 	                currentStepIndex = tempIndex;
 	                currentStep = stepMap.get(currentStepIndex);
 	
 	                if (titlePanel != null) {
 	                    titlePanel.setTitleText(currentStep.getStepTitle());
 	                }
 	
 	                currentStep.enterStepBackward();
 	            }
        	}
        }
    }
    
    /**
     * Returns the index of the step with the given name/id.
     * 
     * @param name the name/id of the requested step 
     * @return the index of the step or -1 if there is no step with that name/id
     */
    private int getIndexForStepName(String name) {
        Set<Integer> keys = stepMap.keySet();
        Iterator<Integer> it = keys.iterator();
        int tempIndex = -1;
        
        while (it.hasNext()) {
            Integer nextInt = it.next();
            StepPane val = stepMap.get(nextInt);

            if ((val != null) && val.getName() != null && val.getName().equals(name)) {
            	tempIndex = nextInt.intValue();
                break;
            }
        }
        
        return tempIndex;
    }
    
    /**
     * Notify all steps the process has been finished.
     */
    private void finishNotify() {
    		Set<Integer> keys = stepMap.keySet();
        Iterator<Integer> it = keys.iterator();

        while (it.hasNext()) {
            Integer nextInt = it.next();
            StepPane val = stepMap.get(nextInt);

            if (val != null) {
            		val.finished();
            }
        }
        
        
    }

    /**
     * Invokes setVisible(visible) on the button identified by buttonType.
     *
     * @param buttonType one of the button constants
     * @param visible the visibility property
     */
    public void setButtonVisible(int buttonType, boolean visible) {
        switch (buttonType) {
        case HELP_BUTTON:
            helpButton.setVisible(visible);

            break;

        case NEXT_BUTTON:
            nextButton.setVisible(visible);

            break;

        case PREVIOUS_BUTTON:
            prevButton.setVisible(visible);

            break;

        case FINISH_BUTTON:
            finishButton.setVisible(visible);

            break;

        case CANCEL_BUTTON:
            cancelButton.setVisible(visible);

            break;

        case ALL_BUTTONS:
            helpButton.setVisible(visible);
            nextButton.setVisible(visible);
            prevButton.setVisible(visible);
            finishButton.setVisible(visible);
            cancelButton.setVisible(visible);

            break;

        default:}
    }

    /**
     * Returns the visibility state of the specified button.
     *
     * @param buttonType one of the button constants
     *
     * @return the visibility property of the button
     */
    public boolean isButtonVisible(int buttonType) {
        switch (buttonType) {
        case HELP_BUTTON:
            return helpButton.isVisible();

        case NEXT_BUTTON:
            return nextButton.isVisible();

        case PREVIOUS_BUTTON:
            return prevButton.isVisible();

        case FINISH_BUTTON:
            return finishButton.isVisible();

        case CANCEL_BUTTON:
            return cancelButton.isVisible();

        case ALL_BUTTONS:
            return helpButton.isVisible() && nextButton.isVisible() &&
            prevButton.isVisible() && finishButton.isVisible() &&
            cancelButton.isVisible();

        default:
            return false;
        }
    }

    /**
     * Enables / disables the specified button.
     *
     * @param buttonType one of the button constants
     * @param enable true to enable, false to disable the button
     */
    public void setButtonEnabled(int buttonType, boolean enable) {
        switch (buttonType) {
        case HELP_BUTTON:
            helpButton.setEnabled(enable);

            break;

        case NEXT_BUTTON:
            nextButton.setEnabled(enable);

            break;

        case PREVIOUS_BUTTON:
            prevButton.setEnabled(enable);

            break;

        case FINISH_BUTTON:
            finishButton.setEnabled(enable);

            break;

        case CANCEL_BUTTON:
            cancelButton.setEnabled(enable);

            break;

        case ALL_BUTTONS:
            helpButton.setEnabled(enable);
            nextButton.setEnabled(enable);
            prevButton.setEnabled(enable);
            finishButton.setEnabled(enable);
            cancelButton.setEnabled(enable);

            break;

        default:}
    }
    
//    /**
//     * Enables / disables the specified button.
//     *
//     * @param buttonType one of the button constants
//     * @param enable true to enable, false to disable the button
//     */
//    public void setButtonToolTipText(int buttonType, String toolTip) {
//        switch (buttonType) {
//        case HELP_BUTTON:
//            helpButton.setToolTipText(toolTip);
//            break;
//
//        case NEXT_BUTTON:
//        	nextButton.setToolTipText(toolTip);
//            break;
//
//        case PREVIOUS_BUTTON:
//        	prevButton.setToolTipText(toolTip); 
//            break;
//
//        case FINISH_BUTTON:
//        	finishButton.setToolTipText(toolTip); 
//            break;
//
//        case CANCEL_BUTTON:
//        	cancelButton.setToolTipText(toolTip);
//            break;
//        }
//    }

    /**
     * Returns whether the specified button is enabled.
     *
     * @param buttonType one of the button constants
     *
     * @return true if the button is enabled, false otherwise
     */
    public boolean isButtonEnabled(int buttonType) {
        switch (buttonType) {
        case HELP_BUTTON:
            return helpButton.isEnabled();

        case NEXT_BUTTON:
            return nextButton.isEnabled();

        case PREVIOUS_BUTTON:
            return prevButton.isEnabled();

        case FINISH_BUTTON:
            return finishButton.isEnabled();

        case CANCEL_BUTTON:
            return cancelButton.isEnabled();

        case ALL_BUTTONS:
            return helpButton.isEnabled() && nextButton.isEnabled() &&
            prevButton.isEnabled() && finishButton.isEnabled() &&
            cancelButton.isEnabled();

        default:
            return false;
        }
    }

    /**
     * Sets the labeltext of the specified button.
     *
     * @param buttonType one of the button constants
     * @param text the new text for the button
     */
    public void setButtonText(int buttonType, String text) {
        switch (buttonType) {
        case HELP_BUTTON:
            helpButton.setText(text);

            break;

        case NEXT_BUTTON:
            nextButton.setText(text);

            break;

        case PREVIOUS_BUTTON:
            prevButton.setText(text);

            break;

        case FINISH_BUTTON:
            finishButton.setText(text);

            break;

        case CANCEL_BUTTON:
            cancelButton.setText(text);

            break;

        default:}
    }

    /**
     * Sets the Icon for the specifeid button.
     *
     * @param buttonType one of the button constants
     * @param icon the new icon
     */
    public void setButtonIcon(int buttonType, Icon icon) {
        switch (buttonType) {
        case HELP_BUTTON:
            helpButton.setIcon(icon);

            break;

        case NEXT_BUTTON:
            nextButton.setIcon(icon);

            break;

        case PREVIOUS_BUTTON:
            prevButton.setIcon(icon);

            break;

        case FINISH_BUTTON:
            finishButton.setIcon(icon);

            break;

        case CANCEL_BUTTON:
            cancelButton.setIcon(icon);

            break;

        default:}
    }

    /**
     * Sets the color of the title panel background.
     *
     * @param background the new background color
     */
    public void setTitlePanelBackground(Color background) {
        if (titlePanel != null) {
            titlePanel.setBackground(background);
        }
    }

    /**
     * Replaces the current title or top panel by the specified panel.
     *
     * @param newTitlePanel the new title panel
     */
    public void setTitlePanel(StepTitlePanel newTitlePanel) {
        GridBagConstraints c = ((GridBagLayout) getLayout()).getConstraints(titlePanel);
        getLayout().removeLayoutComponent(titlePanel);
        titlePanel = newTitlePanel;

        if (titlePanel != null) {
            add(titlePanel, c);
        }
    }

    /**
     * Returns the current title panel.
     *
     * @return the current title panel
     */
    public StepTitlePanel getTitlePanel() {
        return titlePanel;
    }

    /**
     * Sets the color for the button panel.
     *
     * @param background the color for the button panel background
     */
    public void setButtonPanelBackground(Color background) {
        if (buttonPanel != null) {
            buttonPanel.setBackground(background);
        }
    }

    /**
     * Creates and returns a dialog for the multistep process.
     *
     * @param parent the parent for the dialog
     * @param title the dialog title
     * @param modal whether or not the dialog should be modal
     *
     * @return the dialog
     */
    public JDialog createDialog(JDialog parent, String title, boolean modal) {
        dialog = new ClosableDialog(parent, title, modal);
        dialog.setContentPane(this);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        
        return dialog;
    }

    /**
     * Creates and returns a dialog for the multistep process.
     *
     * @param parent the parent for the dialog
     * @param title the dialog title
     * @param modal whether or not the dialog should be modal
     *
     * @return the dialog
     */
    public JDialog createDialog(Frame parent, String title, boolean modal) {
        dialog = new ClosableDialog(parent, title, modal);
        dialog.setContentPane(this);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        return dialog;
    }
    
    public JDialog getDialog(){
    	return dialog;
    }
    
    /**
     * The ActionListener implementation.
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == helpButton) {
        } else if (e.getSource() == nextButton) {
            nextStep();
        } else if (e.getSource() == prevButton) {
            previousStep();
        } else if (e.getSource() == finishButton) {
            finish();
        } else if (e.getSource() == cancelButton) {
            cancel();
        }
    }
}
