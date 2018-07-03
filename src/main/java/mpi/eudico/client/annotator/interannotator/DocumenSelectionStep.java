package mpi.eudico.client.annotator.interannotator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A AnnotatorCompare object creates both a wizard object and the objects that
 * are the steps the wizard presents. This class, the second step in the wizard
 * dialog, helps in making a choice between the classical way of comparing and
 * comparing by calculating the value of kappa for the matrices generated from
 * two annotated tiers. For additional comments, refer to the first step.
 * 
 * @author keeloo
 */
public class DocumenSelectionStep extends StepPane {

    /*
     * The current document, can be null.
     */
    private Transcription transcription;

    /**
     * ide generated
     */
    private static final long serialVersionUID = 1L;
    
    private JPanel documentPanel;
    private JPanel matchingPanel;
    private JLabel locationLabel;
    private JLabel matchingLabel;
    private TitledBorder border;
    private JRadioButton currentDocumentRB, singleFileRB, multipleFileRB;
    private JRadioButton manualSelectRB, affixBasedRB, sameNameRB;

    /**
     * @param wizard
     *            orchestrating the steps
     * @param transcription
     *            to select the tiers from
     */
    public DocumenSelectionStep(MultiStepPane wizard,
	    Transcription transcription) {

		super(wizard);
		
		this.transcription = transcription;
	
		// enable the wizard to jump to a specific step by looking at step names
		this.setName("CompareAnnotatorsDialog.DocumentSelectionStep");
	
		// create the dialog panel
		createPanel();
    }

    /**
     * set the initial state of the components in the pane
     */
    private void setInitialButtonState() {
	
		if (transcription == null) {
		    // disable selection of the current document
		    currentDocumentRB.setEnabled(false);
		    // default to single file selection
		    singleFileRB.setSelected(true);

		} else {
		    // enable selection of the current document
		    currentDocumentRB.setEnabled(true);
		    currentDocumentRB.setSelected(true);
		}
		sameNameRB.setEnabled(false);
    }

    private void loadPreferences() {
    	String prefTierSource = Preferences.getString(CompareConstants.TIER_SOURCE_KEY, null);
    	String prefTierMatch = Preferences.getString(CompareConstants.TIER_MATCH_KEY, null);
    	
		if (transcription == null) {
		    // restore preference
		    if (prefTierSource != null) {
		    	if (CompareConstants.FILE_MATCHING.CURRENT_DOC.value.equals(prefTierSource)) {
		    		currentDocumentRB.setSelected(true);
		    	} else if (CompareConstants.FILE_MATCHING.ACROSS_FILES.value.equals(prefTierSource)) {
		    		multipleFileRB.setSelected(true);
		    	}// otherwise default, single file
		    }
		} else {
		    if (prefTierSource != null) {
		    	if (CompareConstants.FILE_MATCHING.IN_SAME_FILE.value.equals(prefTierSource)) {
		    		singleFileRB.setSelected(true);
		    	} else if (CompareConstants.FILE_MATCHING.ACROSS_FILES.value.equals(prefTierSource)) {
		    		multipleFileRB.setSelected(true);
		    	}	
		    }
		}
		setStateOnRadioButtonEvent();// update buttons
		// preference
		if (prefTierMatch != null) {
			if (CompareConstants.MATCHING.MANUAL.value.equals(prefTierMatch)) {
				manualSelectRB.setSelected(true);
			} else if (CompareConstants.MATCHING.AFFIX.value.equals(prefTierMatch)) {
				affixBasedRB.setSelected(true);
			} else if (CompareConstants.MATCHING.SAME_NAME.value.equals(prefTierMatch) && sameNameRB.isEnabled()) {
				sameNameRB.setSelected(true);
			}
		}
    }
    
    
    /**
     * adjust the pane's component properties as a result of a RadioButton event
     */
    private void setStateOnRadioButtonEvent() {

		if (currentDocumentRB.isSelected()) {
			sameNameRB.setEnabled(false);
			if (sameNameRB.isSelected()) {
				manualSelectRB.setSelected(true);
			}
		} else if (singleFileRB.isSelected()){
			sameNameRB.setEnabled(false);
			if (sameNameRB.isSelected()) {
				manualSelectRB.setSelected(true);
			}
		} else if (multipleFileRB.isSelected()) {
			sameNameRB.setEnabled(true);
		}
    }

    /**
     * Change component associated text on a change of language preference
     */
    public void updateLocale() {
	
		// update the border text
		border.setTitle(ElanLocale.getString(
				"CompareAnnotatorsDialog.DocumentSelectionStep.Hint"));
		locationLabel.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.DocumentSelectionStep.TierLocation"));
		// update the radio button's text
		currentDocumentRB.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.DocumentSelectionStep.CurrentDocumentRB"));
		singleFileRB.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.DocumentSelectionStep.SingleFileRB"));
		multipleFileRB.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.DocumentSelectionStep.MultipleFileRB"));
		
		matchingLabel.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.DocumentSelectionStep.TierMatching"));
		manualSelectRB.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.DocumentSelectionStep.ManualMatching"));
		affixBasedRB.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.DocumentSelectionStep.AffixBased"));
		sameNameRB.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.DocumentSelectionStep.SameName"));
    }

    /**
     * Create the panel representing the second step of the wizard dialog. The
     * panel will be created by using a GridBagLayout and a GridBagConstraints
     * object.
     */
    public void createPanel() {

		// create a border for the panel
		border = new TitledBorder("");
	
		locationLabel = new JLabel();
		/*
		 * Create the radio buttons and an object listening to changes in their
		 * state.
		 */
		currentDocumentRB = new JRadioButton();
		singleFileRB = new JRadioButton();
		multipleFileRB = new JRadioButton();
		
		matchingLabel = new JLabel();
		manualSelectRB = new JRadioButton();
		manualSelectRB.setSelected(true);
		affixBasedRB = new JRadioButton();
		sameNameRB = new JRadioButton();
	
		/*
		 * After all language sensitive components have been created, add text
		 * to them.
		 */
		updateLocale();
	
		setInitialButtonState();
		// document or tier location panel
		// associate radio buttons with listener
		RadioButtonListener radioButtonListener = new RadioButtonListener();
		currentDocumentRB.addActionListener(radioButtonListener);
		singleFileRB.addActionListener(radioButtonListener);
		multipleFileRB.addActionListener(radioButtonListener);
	
		// group the buttons
		ButtonGroup documentButtonGroup = new ButtonGroup();
		documentButtonGroup.add(currentDocumentRB);
		documentButtonGroup.add(singleFileRB);
		documentButtonGroup.add(multipleFileRB);
	
		// create the panel
		documentPanel = new JPanel(new GridBagLayout());
		documentPanel.setBorder(border);
	
		// prepare to add components to the panel
		Insets globalInset, singleTabInset;
	
		globalInset = new Insets(5, 10, 5, 10);
		singleTabInset = new Insets(0, 30, 0, 10);// TODO reconsider the left inset
	
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;;
	
		// add label and the buttons
		gbc.insets = globalInset;
		documentPanel.add(locationLabel, gbc);
		
		gbc.gridy = 1;
		gbc.insets = singleTabInset;
		documentPanel.add(currentDocumentRB, gbc);
	
		gbc.gridy = 2;
		documentPanel.add(singleFileRB, gbc);
	
		gbc.gridy = 3;
		documentPanel.add(multipleFileRB, gbc);
		
		// fill the panel
		setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = globalInset;
		gbc.fill = GridBagConstraints.BOTH;
	
		// add the panel to the wizard pane
		add(documentPanel, gbc);
		
		// tier matching panel
		matchingPanel = new JPanel(new GridBagLayout());
		ButtonGroup matchGroup = new ButtonGroup();
		matchGroup.add(manualSelectRB);
		matchGroup.add(affixBasedRB);
		matchGroup.add(sameNameRB);
		// add listeners
		
		GridBagConstraints mgbc = new GridBagConstraints();
		Insets mgbcInsets = new Insets(5, 0, 5, 10);
		mgbc.anchor = GridBagConstraints.NORTHWEST;
		mgbc.fill = GridBagConstraints.HORIZONTAL;
		mgbc.insets = mgbcInsets;
		mgbc.weightx = 1.0;
		
		matchingPanel.add(matchingLabel, mgbc);
		
		mgbc.gridy = 1;
		mgbc.insets = new Insets(0, 20, 0, 10);
		matchingPanel.add(manualSelectRB, mgbc);
		
		mgbc.gridy = 2;
		matchingPanel.add(affixBasedRB, mgbc);
		
		mgbc.gridy = 3;
		matchingPanel.add(sameNameRB, mgbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(15, 10, 5, 10);
		documentPanel.add(matchingPanel, gbc);
		
		gbc.gridy = 5;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		documentPanel.add(new JPanel(), gbc);
		
		loadPreferences();
    }

    /**
     * An object in this class that listens to changes in the state of the radio
     * buttons. After a button is pushed, the other button's state will set
     * accordingly.
     */
    private class RadioButtonListener implements ActionListener {
    	/**
    	 * Currently doesn't check which button has been clicked, the same checks are performed
    	 * on any click.
    	 */
		@Override
		public void actionPerformed(ActionEvent e) {	
		    setStateOnRadioButtonEvent();
		}
    }

    /**
     * Act on the message send when entering this step after choosing 'next' in
     * the  step.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepForward() {
    	/*
    	 * Always have the next button enabled
    	 */
    	 multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    }

    /**
     * Act on the message send when entering this step after choosing 'previous' in
     * the succeeding step.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
    	/*
    	 * Always have the next button enabled
    	 */
    	 multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	
		/*
		 * because the wizard dialog cannot be finished from this step, the
		 * 'finish' button needs to be disabled.
		 */
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
    }
    
    /**
     * Store choices and selected files for the next step.
     * 
     */
	@Override
	public boolean leaveStepForward() {
		if (transcription != null) {
			if (currentDocumentRB.isSelected()) {
				multiPane.putStepProperty(CompareConstants.TIER_SOURCE_KEY, CompareConstants.FILE_MATCHING.CURRENT_DOC);
			} else if (singleFileRB.isSelected()) {
				multiPane.putStepProperty(CompareConstants.TIER_SOURCE_KEY, CompareConstants.FILE_MATCHING.IN_SAME_FILE);
			} else if (multipleFileRB.isSelected()) {
				multiPane.putStepProperty(CompareConstants.TIER_SOURCE_KEY, CompareConstants.FILE_MATCHING.ACROSS_FILES);
			}
		} else {
			if (currentDocumentRB.isSelected()) {// error condition, shouldn't happen
				ClientLogger.LOG.warning("Cannot proceed to the next step when \"current\" is selected and there's no transcription loaded.");
				return false;
			}
			if (singleFileRB.isSelected()) {
				multiPane.putStepProperty(CompareConstants.TIER_SOURCE_KEY, CompareConstants.FILE_MATCHING.IN_SAME_FILE);
			} else if (multipleFileRB.isSelected()) {
				multiPane.putStepProperty(CompareConstants.TIER_SOURCE_KEY, CompareConstants.FILE_MATCHING.ACROSS_FILES);
			}
		}
		
		if (manualSelectRB.isSelected()) {
			multiPane.putStepProperty(CompareConstants.TIER_MATCH_KEY, CompareConstants.MATCHING.MANUAL);
		} else if (affixBasedRB.isSelected()) {
			multiPane.putStepProperty(CompareConstants.TIER_MATCH_KEY, CompareConstants.MATCHING.AFFIX);
		} else if (sameNameRB.isSelected()) {
			multiPane.putStepProperty(CompareConstants.TIER_MATCH_KEY, CompareConstants.MATCHING.SAME_NAME);// only in combination with inMultipleFiles
		}
		// preferences
		CompareConstants.FILE_MATCHING curTierSource = (CompareConstants.FILE_MATCHING) multiPane.getStepProperty(CompareConstants.TIER_SOURCE_KEY);
		if (curTierSource != null) {
			Preferences.set(CompareConstants.TIER_SOURCE_KEY, curTierSource.value, null);
		}
		CompareConstants.MATCHING curTierMatch = (CompareConstants.MATCHING) multiPane.getStepProperty(CompareConstants.TIER_MATCH_KEY);
		if (curTierMatch != null) {
			Preferences.set(CompareConstants.TIER_MATCH_KEY, curTierMatch.value, null);
		}
		
		return true;
	}

	/**
	 * Depending on the selected radio buttons either jump to the tier selection step
	 * or to the files selection step.
	 */
	@Override
	/* tier matching can be configured in the same step as file selection so always go to the next step
	public String getPreferredNextStep() {
		if (currentDocumentRB.isSelected()) {
			return "Tiers";
		} else {
			return "Files";
		}
	}
	*/
	/**
     * Reply to the wizard's question for the title of this step.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    public String getStepTitle() {
		// no need to invoke the superclass method
		return (ElanLocale
			.getString("CompareAnnotatorsDialog.DocumentSelectionStep.Title"));
    }
}
