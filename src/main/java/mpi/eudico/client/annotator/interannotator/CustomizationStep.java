package mpi.eudico.client.annotator.interannotator;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * This step provides ui elements for method-specific customization;
 * depending on the selected method in the first step different parameters can be set.  
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CustomizationStep extends StepPane {
	private CompareConstants.METHOD curMethod = null;
	
	private JPanel customPanel;
	// modified kappa items
	private JSlider overlapSlider;
	private SliderListener sliderListener;
	private JTextField overlapTF;
	private Insets globalInsets;
	private int defPercentage = 60;
	private JCheckBox verboseOutCB;
	
	// staccato items
	private JFormattedTextField monteCarloTF;
	private JFormattedTextField numNominaTF;
	private JFormattedTextField nullHypTF;
	
	/**
	 * Constructor initializes only the basic, empty panel with a titled border.
	 * 
	 * @param multiPane the parent multiple step pane.
	 */
	public CustomizationStep(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}
	
    /*
     * Creates all basic ui elements.
     */
    @Override
	protected void initComponents() {
    	setLayout(new GridBagLayout());
    	customPanel = new JPanel(new GridBagLayout());
    	
    	globalInsets = new Insets(5, 10, 5, 10);
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1.0;
    	gbc.weighty = 1.0;
    	gbc.insets = globalInsets;
    	add(customPanel, gbc);
    	updateLocale();
    }
    
    public void updateLocale() {
    	customPanel.setBorder(new TitledBorder(ElanLocale.getString("CompareAnnotatorsDialog.CustomizeStep.TitleBorder")));
    }
    
    /**
     * Called when this panel becomes visible after leaving the first step (the method selection step)  
     * and when the new method differs from a possible current or previous method.
     * Removes the current gui elements, if any at all, and populates the panel with appropriate elements.
     * 
     * @param method the (newly) selected comparison method
     */
    private void updatePanel(CompareConstants.METHOD method) {
    	// clean up
    	customPanel.removeAll();
		if (overlapSlider != null) {
			overlapSlider.removeChangeListener(sliderListener);
		}
		
    	if (method == CompareConstants.METHOD.MOD_KAPPA) {
    		// add label + slider + basic or extensive output
    		JLabel percentageLabel = new JLabel(ElanLocale.getString(
    				"CompareAnnotatorsDialog.CustomizeStep.PercentageLabel"));
    		overlapTF = new JTextField(4);
    		overlapTF.setEditable(false);
    		overlapTF.setText(String.valueOf(defPercentage));
    		overlapSlider = new JSlider(51, 100, defPercentage);
    		Dictionary<Integer, JComponent> labels = new Hashtable<Integer, JComponent>(10);
    		labels.put(51, new JLabel("51"));
    		labels.put(60, new JLabel("60"));
    		labels.put(70, new JLabel("70"));
    		labels.put(80, new JLabel("80"));
    		labels.put(90, new JLabel("90"));
    		labels.put(100, new JLabel("100"));
    		overlapSlider.setLabelTable(labels); // requires a Dictionary (Hashtable)
    		//overlapSlider.setLabelTable(overlapSlider.createStandardLabels(20));
    		overlapSlider.setPaintLabels(true);
    		//overlapSlider.setMajorTickSpacing(10);
    		//overlapSlider.setPaintTicks(true);
    		sliderListener = new SliderListener();
    		overlapSlider.addChangeListener(sliderListener);    		
    		overlapSlider.createStandardLabels(20);
    		verboseOutCB = new JCheckBox(ElanLocale.getString(
    				"CompareAnnotatorsDialog.CustomizeStep.VerboseOutput"), false);
    		
    		
    		GridBagConstraints gbc = new GridBagConstraints();
    		gbc.anchor = GridBagConstraints.NORTHWEST;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.weightx = 1.0;
    		gbc.insets = globalInsets;
    		gbc.gridwidth = 2;
    		customPanel.add(percentageLabel, gbc);
    		
    		gbc.gridy = 1;
    		gbc.gridwidth = 1;
    		gbc.fill = GridBagConstraints.NONE;
    		gbc.weightx = 0.0;
    		gbc.anchor = GridBagConstraints.NORTHWEST;
    		customPanel.add(overlapTF, gbc);
    		gbc.gridx = 1;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.weightx = 1.0;
    		gbc.anchor = GridBagConstraints.NORTHWEST;
    		customPanel.add(overlapSlider, gbc);
    		
    		gbc.gridx = 0;
    		gbc.gridwidth = 2;
    		gbc.gridy = 2;
    		gbc.insets =  new Insets(5, 0, 5, 10);
    		customPanel.add(verboseOutCB, gbc);
    		// filler
    		gbc.gridy = 3;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weightx = 1.0;
    		gbc.weighty = 1.0;
    		customPanel.add(new JPanel(), gbc);
    		// read preferences
    		Integer sliderPref = Preferences.getInt(CompareConstants.OVERLAP_PERCENTAGE, null);
    		if (sliderPref != null) {
    			int percentage = sliderPref;
    			if (percentage >= 51 && percentage <= 100) {
    				overlapSlider.setValue(percentage);
    			}
    		}
    		Boolean verbosePref = Preferences.getBool(CompareConstants.OUTPUT_PER_TIER_PAIR, null);
    		if (verbosePref != null) {
    			verboseOutCB.setSelected((Boolean) verbosePref);
    		}
    	} else if (method == CompareConstants.METHOD.STACCATO) {
    		// add text fields for number of Monte Carlo Simulations, number of slots for nomination-lengths,
    		// and for null hypothesis value
    		GridBagConstraints gbc = new GridBagConstraints();
    		gbc.insets = globalInsets;
    		gbc.anchor = GridBagConstraints.WEST;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.weightx = 1.0;
    		
    		gbc.gridwidth = 1;
    		customPanel.add(new JLabel(ElanLocale.getString("CompareAnnotatorsDialog.CustomizeStep.MCSIterations")), gbc);
    		gbc.gridy = 1;
    		customPanel.add(new JLabel(ElanLocale.getString("CompareAnnotatorsDialog.CustomizeStep.NominationLength")), gbc);
    		gbc.gridy = 2;
    		customPanel.add(new JLabel(ElanLocale.getString("CompareAnnotatorsDialog.CustomizeStep.NullHypothesis")), gbc);
    		
    		gbc.gridx = 1;
    		gbc.gridy = 0;
    		
    		monteCarloTF = new JFormattedTextField(new DecimalFormat("#####"));
    		monteCarloTF.setValue(1000);
    		monteCarloTF.setPreferredSize(new Dimension (60, monteCarloTF.getPreferredSize().height));
    		customPanel.add(monteCarloTF, gbc);
    		
    		numNominaTF = new JFormattedTextField(NumberFormat.getIntegerInstance());
    		numNominaTF.setValue(10);
    		gbc.gridy = 1;
    		customPanel.add(numNominaTF, gbc);
    		
    		nullHypTF = new JFormattedTextField(new DecimalFormat("0.0#"));
    		nullHypTF.setValue(0.05d);
    		gbc.gridy = 2;
    		customPanel.add(nullHypTF, gbc);
    		// filler
    		gbc.gridy = 3;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weightx = 1.0;
    		gbc.weighty = 1.0;
    		customPanel.add(new JPanel(), gbc);
    		
    		// read preferences
    		Integer mcsIterObj = Preferences.getInt(CompareConstants.MONTE_CARLO_SIM, null);
    		if (mcsIterObj != null) {
    			monteCarloTF.setValue(mcsIterObj);
    		}
    		Integer numNomObj = Preferences.getInt(CompareConstants.NUM_NOMINATIONS, null);
    		if (numNomObj != null) {
    			numNominaTF.setValue(numNomObj);
    		}
    		Double nullHypObj = Preferences.getDouble(CompareConstants.NULL_HYPOTHESIS, null);
    		if (nullHypObj != null) {
    			nullHypTF.setValue(nullHypObj);
    		}
    	}
    	//curMethod = method;
    	//updateLocale();
    }

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("CompareAnnotatorsDialog.CustomizeStep.Title");
	}

	/**
	 * Checks what the newly selection method is and updates the panel if needed.
	 */
	@Override
	public void enterStepForward() {
		CompareConstants.METHOD method = (CompareConstants.METHOD) 
				multiPane.getStepProperty(CompareConstants.METHOD_KEY);
		if (method == CompareConstants.METHOD.CLASSIC) {
			curMethod = method;
			leaveStepForward();
			multiPane.nextStep();
			return;
		}
		if (method != curMethod) {
			updatePanel(method);			
		}
		curMethod = method;
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}

	/**
	 * Stores current settings in the overall properties map and as a preference
	 */
	@Override
	public boolean leaveStepForward() {
		if (curMethod == CompareConstants.METHOD.MOD_KAPPA) {
			// store percentage; store flag for verbose output
			if (overlapSlider != null) {//should be superfluous
				multiPane.putStepProperty(CompareConstants.OVERLAP_PERCENTAGE, Integer.valueOf(overlapSlider.getValue()));
				Preferences.set(CompareConstants.OVERLAP_PERCENTAGE, Integer.valueOf(overlapSlider.getValue()), null);
			}
			if (verboseOutCB != null) {
				multiPane.putStepProperty(CompareConstants.OUTPUT_PER_TIER_PAIR, Boolean.valueOf(verboseOutCB.isSelected()));
				Preferences.set(CompareConstants.OUTPUT_PER_TIER_PAIR, Boolean.valueOf(verboseOutCB.isSelected()), null);
			}
		} else if (curMethod == CompareConstants.METHOD.STACCATO) {
			if (monteCarloTF != null) {//should be superfluous. If this one is not null the other text fields are neither
				try {
					monteCarloTF.commitEdit();
					//Object mcObj = monteCarloTF.getValue();// returns a Long, it seems
					Integer mcsInteger = Integer.valueOf( monteCarloTF.getValue().toString());
					multiPane.putStepProperty(CompareConstants.MONTE_CARLO_SIM, mcsInteger);
					Preferences.set(CompareConstants.MONTE_CARLO_SIM, mcsInteger, null);
				} catch (ParseException pe) {
					// log?
					monteCarloTF.grabFocus();
					return false;
				}
				try {
					numNominaTF.commitEdit();
					Integer nnInteger = Integer.valueOf( numNominaTF.getValue().toString());
					multiPane.putStepProperty(CompareConstants.NUM_NOMINATIONS, nnInteger);
					Preferences.set(CompareConstants.NUM_NOMINATIONS, nnInteger, null);
				} catch (ParseException pe) {
					// log?
					numNominaTF.grabFocus();
					return false;
				}
				try {
					nullHypTF.commitEdit();
					//Object nhObj = nullHypTF.getValue();// returns a Double, it seems
					Double nhDouble = Double.valueOf(nullHypTF.getValue().toString());
					multiPane.putStepProperty(CompareConstants.NULL_HYPOTHESIS, nhDouble);
					Preferences.set(CompareConstants.NULL_HYPOTHESIS, nhDouble, null);
				} catch (ParseException pe) {
					// log?
					nullHypTF.grabFocus();
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void enterStepBackward() {
		if (curMethod == CompareConstants.METHOD.CLASSIC) {
			leaveStepBackward();
			multiPane.previousStep();
		} else {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
			multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
		}
	}
    
	/**
	 * A listener for the percentage slider (currently only for the modified kappa method).
	 */
    private class SliderListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent ce) {
			// check the source if it can be ambiguous. Now assume the one slider fired the event.
			if (overlapSlider != null && overlapTF != null) {
				overlapTF.setText(String.valueOf(overlapSlider.getValue()));
			}
		}
    	
    }
}
