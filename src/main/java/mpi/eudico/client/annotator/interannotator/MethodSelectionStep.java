package mpi.eudico.client.annotator.interannotator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A AnnotatorCompare object creates both a wizard object and the objects that
 * are the steps the wizard presents. This class, the first step in the wizard
 * dialog, helps in making a choice between the classical way of comparing and
 * comparing by calculating the value of kappa for the matrices generated from
 * two annotated tiers.
 * 
 * Because the class extends LocalizableStepPane, it needs to conform to the
 * limitations imposed by that class. This means that methods need to invoke
 * corresponding methods from the superclass first. Unless such a method is kind
 * of abstract of course.
 * 
 * @author keeloo
 */
public class MethodSelectionStep extends StepPane {

    /*
     * Because it influences state changes, remember the current document from
     * the messages interpreted by the constructor. If no document has been
     * selected, transcription equals null.
     */
    private Transcription transcription;

    /**
     * ide generated
     */
    private static final long serialVersionUID = 1L;
    

    private JPanel methodPanel = null;
    private TitledBorder border;
    private JRadioButton classicCompareRB, kappaCompareRB, staccatoCompareRB;

    /**
     * @param wizard
     *            orchestrating the steps
     * @param transcription
     *            to select the tiers from
     */
    public MethodSelectionStep(MultiStepPane wizard,
	    Transcription transcription) {
		super(wizard);
	
		this.transcription = transcription;
		
		// enable the wizard to jump to a specific step by looking at step names
		this.setName("CompareAnnotatorsDialog.MethodSelectionStep");
	
		// create the dialog panel
		createPanel();
    }

    /**
     * This method will
     * be invoked on a change in language preference. The text associated with
     * the components will retrieved by means of an ElanLocale object.
     */
    public void updateLocale() {

		// update the border text
		border.setTitle(ElanLocale
			.getString("CompareAnnotatorsDialog.MethodSelectionStep.Hint"));
	
		// update the radio button's text
		classicCompareRB.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.MethodSelectionStep.ClassicRB"));
		kappaCompareRB.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.MethodSelectionStep.KappaRB"));
		staccatoCompareRB.setText(ElanLocale.getString(
				"CompareAnnotatorsDialog.MethodSelectionStep.StaccatoRB"));
    }

    /**
     * Create the panel representing the first step of the wizard dialog. The
     * panel will be created by using a GridBagLayout and a GridBagConstraints
     * object.
     */
    public void createPanel() {
		// create a border for the panel
		border = new TitledBorder("");
	
		// create radio buttons
		classicCompareRB = new JRadioButton();
		kappaCompareRB = new JRadioButton(); 		
		kappaCompareRB.setSelected(true);
		staccatoCompareRB = new JRadioButton();
		//staccatoCompareRB.setEnabled(false);
	
		/*
		 * After all language sensitive components have been created, add text
		 * to them.
		 */
		updateLocale();
	
		// group the buttons
		ButtonGroup methodButtonGroup = new ButtonGroup();
		methodButtonGroup.add(classicCompareRB);
		methodButtonGroup.add(kappaCompareRB);
		methodButtonGroup.add(staccatoCompareRB);
	
		// create the panel
		methodPanel = new JPanel(new GridBagLayout());
		methodPanel.setBorder(border);
	
		// prepare to add components to the panel
		Insets globalInset, singleTabInset;
	
		globalInset = new Insets(5, 10, 5, 10);
		singleTabInset = new Insets(0, 30, 0, 10);// TODO reconsider the left inset (similar for some other wizards)
	
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
	
		// add the radio buttons
		gbc.insets = singleTabInset;
		methodPanel.add(kappaCompareRB, gbc);
	
		gbc.gridy = 1;
		methodPanel.add(classicCompareRB, gbc);
		
		gbc.gridy = 2;
		methodPanel.add(staccatoCompareRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		methodPanel.add(new JPanel(), gbc);
	
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
		
		String prefMethod = Preferences.getString(CompareConstants.METHOD_KEY, null);
		if (prefMethod != null) {
			if (CompareConstants.METHOD.CLASSIC.value.equals(prefMethod)) {
				classicCompareRB.setSelected(true);
			} else if (CompareConstants.METHOD.MOD_KAPPA.value.equals(prefMethod)) {
				kappaCompareRB.setEnabled(true);
			} else if (CompareConstants.METHOD.STACCATO.value.equals(prefMethod)) {
				staccatoCompareRB.setSelected(true);
			}
		}
		// add the panel to the wizard pane
		add(methodPanel, gbc);
    }
    
    /**
     * Answer the wizard when it asks for the title of this step.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
		// no need to invoke the superclass method
		return (ElanLocale
			.getString("CompareAnnotatorsDialog.MethodSelectionStep.Title"));
    }

    /**
     * Answer the wizard when it asks for the preferred next step.
     * 
     * @return the identifier of the preferred next step; when null the wizard
     *         will follow the steps in the order of declaration.
     */
    @Override
	public String getPreferredNextStep() {
    	// TODO hier... the classic method might need to be changed such that it also performs
    	// on tiers in different files?
    	/*
		if (multiPane.getStepProperty(CompareConstants.METHOD_KEY) == CompareConstants.METHOD.CLASSIC) {
		    return "CompareAnnotatorsDialog.TierSelectionStep";
		} else {
		    // otherwise, stick to the predefined step order
		    return null;
		}
		*/
    	return null;
    }

    /**
     * Act on message from the wizard when entering this step for the first time.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
     @Override
	public void enterStepForward() {
 		// accordingly, enable the 'next' wizard button
 		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
    }
 
    /**
     * Act on message from the wizard when entering this step after choosing
     * 'previous' in the succeeding step.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
		// on entering back, the 'next' button will not be enabled, re-enable it
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		// disable the 'finish' button
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
    }

    /**
     * Store the method that is selected.
     * 
     * @return true, can always move to the next step
     */
	@Override
	public boolean leaveStepForward() {
		if (classicCompareRB.isSelected()) {
		    // remember the method selected
		    multiPane.putStepProperty(CompareConstants.METHOD_KEY, CompareConstants.METHOD.CLASSIC);
		    Preferences.set(CompareConstants.METHOD_KEY, CompareConstants.METHOD.CLASSIC.value, null);
		} else if (kappaCompareRB.isSelected()){
		    multiPane.putStepProperty(CompareConstants.METHOD_KEY, CompareConstants.METHOD.MOD_KAPPA);
		    Preferences.set(CompareConstants.METHOD_KEY, CompareConstants.METHOD.MOD_KAPPA.value, null);
		} else if (staccatoCompareRB.isSelected()) {
		    multiPane.putStepProperty(CompareConstants.METHOD_KEY, CompareConstants.METHOD.STACCATO);
		    Preferences.set(CompareConstants.METHOD_KEY, CompareConstants.METHOD.STACCATO.value, null);
		} else {
			return false;
		}

		return true;
	}
    
    
}
