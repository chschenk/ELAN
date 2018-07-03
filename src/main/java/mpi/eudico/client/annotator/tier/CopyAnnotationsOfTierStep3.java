package mpi.eudico.client.annotator.tier;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * Third step in the copy annotations process, which allows to specify
 * which annotations to copy, all annotations or only those with a specific value,
 * and whether or not existing annotations on the target tier may be overwritten.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CopyAnnotationsOfTierStep3 extends StepPane implements ChangeListener {
	private JPanel optionsPanel;
	private JRadioButton allAnnotationsRB;
	private JRadioButton withValueRB;
	private ButtonGroup buttonGroup;
	private JTextField valuePatternTF;
	private JCheckBox regexCB;
	private JCheckBox overwriteCB;
	
	/**
	 * Constructor
	 * @param multiPane the parent panel
	 */
	public CopyAnnotationsOfTierStep3(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();
	}

	/**
	 * Adds radio buttons, textfields and checkboxes to the layout.
	 */
	@Override
	protected void initComponents() {
		setBorder(new EmptyBorder(5, 10, 5, 10));
		setLayout(new BorderLayout());
		
		optionsPanel = new JPanel(new GridBagLayout());
		allAnnotationsRB = new JRadioButton(ElanLocale.getString(
				"RemoveAnnotationsOrValuesDlg.RadioButton.AllAnnotations"), true);// All annotations
		withValueRB = new JRadioButton(ElanLocale.getString(
				"RemoveAnnotationsOrValuesDlg.RadioButton.AnnotationsWithValues"));//annotations with this value
		buttonGroup = new ButtonGroup();
		buttonGroup.add(allAnnotationsRB);
		buttonGroup.add(withValueRB);
		valuePatternTF = new JTextField();
		regexCB = new JCheckBox(ElanLocale.getString(
				"CopyAnnotationsDialog.CopyOptions.RegularExpression"), true);// treat as regular expression
		overwriteCB = new JCheckBox(ElanLocale.getString(
				"MergeTranscriptionDialog.Label.Overwrite"));// overwrite existing
		// lay out
		optionsPanel.setBorder(new TitledBorder(ElanLocale.getString("Menu.Options")));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		Insets globalInsets = new Insets(10, 10, 5, 10);
		gbc.insets = globalInsets;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		optionsPanel.add(allAnnotationsRB, gbc);
		
		gbc.gridy++;
		optionsPanel.add(withValueRB, gbc);
		gbc.gridy++;
		gbc.insets = new Insets(5, 35, 5, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		optionsPanel.add(valuePatternTF, gbc);
		gbc.gridy++;
		optionsPanel.add(regexCB, gbc);
		gbc.gridy++;
		gbc.insets = new Insets(10, 10, 5, 10);
		optionsPanel.add(overwriteCB, gbc);
		
		// add a filler panel
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridy++;
		optionsPanel.add(new JPanel(), gbc);
		add(optionsPanel, BorderLayout.CENTER);
		// listeners
		allAnnotationsRB.addChangeListener(this);
		withValueRB.addChangeListener(this);
		// read preferences
		loadPreferences();
		updateUIState();
	}

	/**
	 * Loads the stored configuration settings.
	 */
	private void loadPreferences() {
		Boolean allAnnPref = Preferences.getBool("CopyAnnotationsFromTier.All", null);
		if (allAnnPref != null && allAnnPref) {
			allAnnotationsRB.setSelected(true);
		}
		Boolean withValuePref = Preferences.getBool("CopyAnnotationsFromTier.WithValue", null);
		if (withValuePref != null && withValuePref) {
			withValueRB.setSelected(true);
		}
		Boolean regexPref = Preferences.getBool("CopyAnnotationsFromTier.Regex", null);
		if (regexPref != null) {
			regexCB.setSelected(regexPref);
		}
		Boolean overwritePref = Preferences.getBool("CopyAnnotationsFromTier.Overwrite", null);
		if (overwritePref != null) {
			overwriteCB.setSelected(overwritePref);
		}
	}
	
	/**
	 * Stores the configuration settings.
	 */
	private void storePreferences() {
		Preferences.set("CopyAnnotationsFromTier.All", Boolean.valueOf(allAnnotationsRB.isSelected()), null);
		Preferences.set("CopyAnnotationsFromTier.WithValue", Boolean.valueOf(withValueRB.isSelected()), null);
		Preferences.set("CopyAnnotationsFromTier.Regex", Boolean.valueOf(regexCB.isSelected()), null);
		Preferences.set("CopyAnnotationsFromTier.Overwrite", Boolean.valueOf(overwriteCB.isSelected()), null);
	}
	
	/**
	 * Some UI items are enabled or disabled depending on selected states of
	 * the "all" or "some" annotations radio buttons. 
	 */
	private void updateUIState() {
		boolean allAnns = allAnnotationsRB.isSelected();
		valuePatternTF.setEnabled(!allAnns);
		valuePatternTF.setEditable(!allAnns);
		regexCB.setEnabled(!allAnns);
	}
	
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("CopyAnnotationsDialog.SetCriteria");
	}

	@Override
	public void enterStepForward() {
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
	}

	/**
	 * Stores the settings for the copy operation as step properties.
	 */
	@Override
	public boolean leaveStepForward() {
		String copyMode = "ALL";
		if (withValueRB.isSelected()) {
			copyMode = "WithValue";
			multiPane.putStepProperty("QueryValue", valuePatternTF.getText().trim());// can be the empty string
			multiPane.putStepProperty("UseRegex", Boolean.valueOf(regexCB.isSelected()));
		}
		multiPane.putStepProperty("CopyMode", copyMode);
		multiPane.putStepProperty("Overwrite", Boolean.valueOf(overwriteCB.isSelected()));
		
		storePreferences();
		return true;
	}

	/**
	 * The next step starts and monitors the progress of the actual
	 * process.
	 */
	@Override
	public boolean doFinish() {
		multiPane.nextStep();
		return false;
	}

	/**
	 * Update UI elements regardless of the source of the event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		updateUIState();
	}

}
