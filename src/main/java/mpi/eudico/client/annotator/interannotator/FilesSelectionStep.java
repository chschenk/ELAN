package mpi.eudico.client.annotator.interannotator;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.export.multiplefiles.AbstractFilesAndTierSelectionStepPane;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
/**
 * Only reuses and extends the file selection part of the super class.
 * The extension is that, depending on choices in a previous step, the user can specify 
 * how to match 2 files, based on prefix or suffix. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class FilesSelectionStep extends AbstractFilesAndTierSelectionStepPane 
implements ChangeListener {
	protected JPanel matchingOptionsPanel;
	protected JLabel matchLabel;
	protected JRadioButton prefixMatchRB;
	protected JRadioButton suffixMatchRB;
	protected JCheckBox customSepCB;
	protected JTextField customSepTF;
	protected ArrayList<File> selFiles;
	// items for tier name matching
    private JPanel tierMatchingOptionsPanel;
    private JRadioButton tierPrefixMatchRB;
    private JRadioButton tierSuffixMatchRB;
    private JCheckBox tierCustomSepCB;
    private JTextField tierCustomSepTF;
    
    private final String prefFileSelection = "Compare.FileSelection";
    private final String prefTierAffixType = "Compare.Matching.TierAffix.Type";
    private final String prefFileAffixType = "Compare.Matching.FileAffix.Type";
    private final String prefFileCustomSep = "Compare.Matching.FileCustomSeparator";
    private final String prefTierCustomSep = "Compare.Matching.TierCustomSeparator";
    
	/**
	 * Constructor.
	 * 
	 * @param mp
	 * @param transcription
	 */
	public FilesSelectionStep(MultiStepPane mp,
			TranscriptionImpl transcription) {
		super(mp, transcription);
		initComponents2();
	}
	
	@Override
	protected void initComponents() {
		// method stub
	}

	protected void initComponents2() {
		initFileSelectionPanel();	
		initTierSelectionPanel();		
		initOptionsPanel();
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = globalInset;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		add(fileSelectionPanel, gbc);

		gbc.gridy = 1;
		add(tierMatchingOptionsPanel, gbc);
		loadPreferences();
	}
	
	@Override
	protected void initFileSelectionPanel() {
		super.initFileSelectionPanel();
		fileSelectionPanel.remove(currentlyOpenedFileRB);
		
		// add additional options if appropriate
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		fileSelectionPanel.add(new JPanel(), gbc);
	}

	@Override
	protected void initTierSelectionPanel() {
		tierSelectionPanel = new JPanel();
		textArea = new JTextArea();

		tierMatchingOptionsPanel = new JPanel(new GridBagLayout());
		JPanel innerPanel = new JPanel(new GridBagLayout());
		
		tierSuffixMatchRB = new JRadioButton();
		tierSuffixMatchRB.setSelected(true);
		tierPrefixMatchRB = new JRadioButton();
		ButtonGroup matchGroup = new ButtonGroup();
		matchGroup.add(tierSuffixMatchRB);
		matchGroup.add(tierPrefixMatchRB);
		tierCustomSepCB = new JCheckBox();
		tierCustomSepTF = new JTextField(6);
		tierCustomSepTF.setEnabled(false);
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		//gbc.insets = globalInset;//new Insets(2, 4, 2, 4)
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 30, 0, 10);
		innerPanel.add(tierSuffixMatchRB, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		innerPanel.add(tierPrefixMatchRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 3;
		innerPanel.add(tierCustomSepCB, gbc);
		
		gbc.gridy = 3;
		gbc.insets = new Insets(0, 60, 0, 10);// TODO revise, have some constants with indentation values
		gbc.fill = GridBagConstraints.NONE;
		innerPanel.add(tierCustomSepTF, gbc);
		
		gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		tierMatchingOptionsPanel.add(innerPanel, gbc);
		
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		tierMatchingOptionsPanel.add(new JPanel(), gbc);
		
		tierSuffixMatchRB.setText(ElanLocale.getString("CreateMultiEAFDialog.Label.Suffix"));
		tierPrefixMatchRB.setText(ElanLocale.getString("CreateMultiEAFDialog.Label.Prefix"));
		tierCustomSepCB.setText(ElanLocale.getString("CreateMultiEAFDialog.Button.Separator"));
		tierCustomSepCB.addChangeListener(this);
		tierMatchingOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString("CompareAnnotatorsDialog.FilesSelectionStep.CombineTiers")));
		
	}

	protected void initOptionsPanel() {
		matchingOptionsPanel = new JPanel(new GridBagLayout());
		// give this panel a titled border, or just add it to the tier selection panel?
		matchLabel = new JLabel();
		//matchLabel.setAlignmentX(0f);
		suffixMatchRB = new JRadioButton();
		suffixMatchRB.setSelected(true);
		prefixMatchRB = new JRadioButton();
		ButtonGroup matchGroup = new ButtonGroup();
		matchGroup.add(suffixMatchRB);
		matchGroup.add(prefixMatchRB);
		customSepCB = new JCheckBox();
		customSepTF = new JTextField(6);
		customSepTF.setEnabled(false);
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = globalInset;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		matchingOptionsPanel.add(matchLabel, gbc);
		
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 30, 0, 10);
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		matchingOptionsPanel.add(suffixMatchRB, gbc);
		
		gbc.gridx = 1;
		matchingOptionsPanel.add(prefixMatchRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		matchingOptionsPanel.add(customSepCB, gbc);
		
		gbc.gridy = 3;
		gbc.insets = new Insets(0, 60, 0, 10);// TODO revise, have some constants with indentation values
		gbc.fill = GridBagConstraints.NONE;
		matchingOptionsPanel.add(customSepTF, gbc);
		
		// constraints for the options panel
		gbc = new GridBagConstraints();		
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.insets = new Insets(15, 10, 5, 10);
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		// insert the panel after the last real component, "before" the filler panel
		GridBagConstraints fillConstraints = null;
		GridBagConstraints lastConstraints = null;
		try {
			Component lastRelComp = fileSelectionPanel.getComponent(fileSelectionPanel.getComponentCount() - 2);
			lastConstraints = ((GridBagLayout) fileSelectionPanel.getLayout()).getConstraints(lastRelComp);
			Component filler = fileSelectionPanel.getComponent(fileSelectionPanel.getComponentCount() - 1);
			fillConstraints = ((GridBagLayout) fileSelectionPanel.getLayout()).getConstraints(filler);
			
			if (lastConstraints != null) {
				gbc.gridy = lastConstraints.gridy + 1;
				if (fillConstraints != null) {// remove the filler panel and add it again
					fileSelectionPanel.remove(filler);
					fillConstraints.gridy = lastConstraints.gridy + 2;
					fileSelectionPanel.add(filler, fillConstraints);
				}
			}
		} catch (Throwable t) {
			// just catch any possible exception
			// results in adding the options panel at the end
		}

		fileSelectionPanel.add(matchingOptionsPanel, gbc);

		matchLabel.setText(ElanLocale.getString("CompareAnnotatorsDialog.FilesSelectionStep.CombineFiles"));
		suffixMatchRB.setText(ElanLocale.getString("CreateMultiEAFDialog.Label.Suffix"));
		prefixMatchRB.setText(ElanLocale.getString("CreateMultiEAFDialog.Label.Prefix"));
		customSepCB.setText(ElanLocale.getString("CreateMultiEAFDialog.Button.Separator"));
		customSepCB.addChangeListener(this);
	}
	
	private void showWarning(String message) {
		JOptionPane.showMessageDialog(this, message, ElanLocale.getString("Message.Warning"), 
				JOptionPane.WARNING_MESSAGE);
	}

	@Override
	public void updateButtonStates() {
		if (selFiles == null || selFiles.size() == 0) {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		} else {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		}
	}

	@Override
	protected void initializeTierSelectPanel(ArrayList<File> files) {
		if (files != null) {
			if (selFiles == null) {
				selFiles = new ArrayList<File>();
			}
			selFiles.clear();
			selFiles.addAll(files);
		}
		updateButtonStates();
	}
	
	/**
	 * Overrides this method by doing nothing. Since the files are not going to be changed
	 * a warning concerning open files is either not necessary or has to be different 
	 * (unsaved changes in the files will not be part of the results).
	 */
	@Override
	protected void checkForOpenedFiles(List<String> fileNames) {
		// method stub
	}

	@Override
	public void enterStepForward() {
		Object tierSource = multiPane.getStepProperty(CompareConstants.TIER_SOURCE_KEY);
		matchingOptionsPanel.setVisible(tierSource == CompareConstants.FILE_MATCHING.ACROSS_FILES);

		Object tierMatching = multiPane.getStepProperty(CompareConstants.TIER_MATCH_KEY);
		tierMatchingOptionsPanel.setVisible(tierMatching == CompareConstants.MATCHING.AFFIX);

 		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
 		boolean nextStep = (selFiles != null && selFiles.size() > 0) || transcription != null;
 		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, nextStep);
 		
 		if (transcription != null && tierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC && 
 				tierMatching != CompareConstants.MATCHING.AFFIX) {
 			// nothing to do in this step so move on
 			leaveStepForward();
 			multiPane.nextStep();
 		}
 		
 		// check enabling/disabling of file selection components
 		if (transcription != null && tierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC) {
 			// disable the file selection panel
 			selectedFilesFromDiskRB.setEnabled(false); 
 			filesFromDomainRB.setEnabled(false);
 			selectFilesBtn.setEnabled(false); 
 			selectDomainBtn.setEnabled(false);
 		} else {
 			selectedFilesFromDiskRB.setEnabled(true);
 			filesFromDomainRB.setEnabled(true);
 			if (selectedFilesFromDiskRB.isSelected()) {
 				selectFilesBtn.setEnabled(true);
 			} else {
 				selectDomainBtn.setEnabled(true);
 			}
 		}
	}

	@Override
	public void enterStepBackward() {
 		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
 		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
 		
 		Object tierSource = multiPane.getStepProperty(CompareConstants.TIER_SOURCE_KEY);
 		Object tierMatching = multiPane.getStepProperty(CompareConstants.TIER_MATCH_KEY);
 		if (transcription != null && tierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC && 
 				tierMatching != CompareConstants.MATCHING.SUFFIX && tierMatching != CompareConstants.MATCHING.PREFIX) {
 			// nothing to do in this step so move on
 			leaveStepBackward();
 			multiPane.previousStep();
 		}
	}


	@Override
	public boolean leaveStepForward() {
		if ((selFiles == null || selFiles.size() == 0) && transcription == null) {
			// warn the user? This shouldn't be possible actually
			ClientLogger.LOG.warning("No files selected, cannot proceed to next step.");
			return false;
		}
		
		if (matchingOptionsPanel.isVisible() && customSepCB.isSelected()) {
			String separator = customSepTF.getText();
			if (separator == null || separator.length() == 0) {
				// warn the user
				ClientLogger.LOG.warning("No custom separator specified for file matching, cannot proceed to next step.");
				showWarning(ElanLocale.getString("CompareAnnotatorsDialog.DocumentSelectionStep.Warning.NoSeparator"));
				customSepTF.requestFocus();
				return false;
			}
		}
		if (tierMatchingOptionsPanel.isVisible() && tierCustomSepCB.isSelected()) {
			String separator = tierCustomSepTF.getText();
			if (separator == null || separator.length() == 0) {
				// warn the user
				ClientLogger.LOG.warning("No custom separator specified for tier matching, cannot proceed to next step.");
				showWarning(ElanLocale.getString("CompareAnnotatorsDialog.DocumentSelectionStep.Warning.NoTierSeparator"));
				tierCustomSepTF.requestFocus();
				return false;
			}
		}
		
		multiPane.putStepProperty(CompareConstants.SEL_FILES_KEY, selFiles);
		
		if (matchingOptionsPanel.isVisible()) {
			if (suffixMatchRB.isSelected()) {
				multiPane.putStepProperty(CompareConstants.FILE_MATCH_KEY, CompareConstants.MATCHING.SUFFIX);
			} else {
				multiPane.putStepProperty(CompareConstants.FILE_MATCH_KEY, CompareConstants.MATCHING.PREFIX);
			}
			if (customSepCB.isSelected()) {
				multiPane.putStepProperty(CompareConstants.FILE_SEPARATOR_KEY, customSepTF.getText());
			} else {
				multiPane.putStepProperty(CompareConstants.FILE_SEPARATOR_KEY, null);//reset to be sure
			}
		} else {//reset some properties?
			multiPane.putStepProperty(CompareConstants.FILE_MATCH_KEY, null);// or set suffix as default
			multiPane.putStepProperty(CompareConstants.FILE_SEPARATOR_KEY, null);
		}
		
		if (tierMatchingOptionsPanel.isVisible()) {
			if (tierSuffixMatchRB.isSelected()) {
				multiPane.putStepProperty(CompareConstants.TIER_MATCH_KEY, CompareConstants.MATCHING.SUFFIX);// replaces AFFIX
			} else {
				multiPane.putStepProperty(CompareConstants.TIER_MATCH_KEY, CompareConstants.MATCHING.PREFIX);// replaces AFFIX
			}
			if (tierCustomSepCB.isSelected()) {
				multiPane.putStepProperty(CompareConstants.TIER_SEPARATOR_KEY, tierCustomSepTF.getText());
			} else {
				multiPane.putStepProperty(CompareConstants.TIER_SEPARATOR_KEY, null);// reset 
			}
		} else {//reset?
			multiPane.putStepProperty(CompareConstants.TIER_SEPARATOR_KEY, null);
		}
		
		storePreferences();
		return true;
	}

	private void storePreferences() {	
		if (filesFromDomainRB.isSelected()) {
			Preferences.set(prefFileSelection, "Domain", null);
		} else if (selectedFilesFromDiskRB.isSelected()) {
			Preferences.set(prefFileSelection, "Browse", null);
		}// current document dealt with separately
		
		if (matchingOptionsPanel.isVisible()) {
			CompareConstants.MATCHING curMatching = (CompareConstants.MATCHING) multiPane.getStepProperty(CompareConstants.FILE_MATCH_KEY);
			if ( curMatching != null) {
				Preferences.set(prefFileAffixType, curMatching.value, null);
			}
			Preferences.set(prefFileCustomSep, Boolean.valueOf(customSepCB.isSelected()), null);
			Preferences.set(CompareConstants.FILE_SEPARATOR_KEY, customSepTF.getText(), null);
		}
		
		if (tierMatchingOptionsPanel.isVisible()) {
			if (tierSuffixMatchRB.isSelected()) {
				Preferences.set(prefTierAffixType, CompareConstants.MATCHING.SUFFIX.value, null);
			} else {
				Preferences.set(prefTierAffixType, CompareConstants.MATCHING.PREFIX.value, null);
			}
			Preferences.set(prefTierCustomSep, Boolean.valueOf(tierCustomSepCB.isSelected()), null);
			Preferences.set(CompareConstants.TIER_SEPARATOR_KEY, tierCustomSepTF.getText(), null);
		}
		
	}
	
	private void loadPreferences() {
		String fileSelPref = Preferences.getString(prefFileSelection, null);
		if ("Domain".equals(fileSelPref)) {// Browse is default
			filesFromDomainRB.setSelected(true);
			selectDomainBtn.setEnabled(true);
			selectFilesBtn.setEnabled(false);
		}
		String fileMatchType = Preferences.getString(prefFileAffixType, null);
		// suffix is default
		if (CompareConstants.MATCHING.PREFIX.value.equals(fileMatchType)) {
			prefixMatchRB.setSelected(true);
		}
		Boolean customFileSep = Preferences.getBool(prefFileCustomSep, null);
		if (customFileSep != null) {
			customSepCB.setSelected(customFileSep);// will this take care of enabling/disabling of the text field
		}
		String stringPref = Preferences.getString(CompareConstants.FILE_SEPARATOR_KEY, null);
		if (stringPref != null) {
			customSepTF.setText(stringPref);
		}
		// tier preferences
		String tierPref = Preferences.getString(prefTierAffixType, null);
		if (CompareConstants.MATCHING.PREFIX.value.equals(tierPref)) {// suffix default
			tierPrefixMatchRB.setSelected(true);
		}
		Boolean boolPref = Preferences.getBool(prefTierCustomSep, null);
		if (boolPref != null) {
			tierCustomSepCB.setSelected(boolPref);
		}
		stringPref = Preferences.getString(CompareConstants.TIER_SEPARATOR_KEY, null);
		if (stringPref != null) {
			tierCustomSepTF.setText(stringPref);
		}
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("CompareAnnotatorsDialog.FilesSelectionStep.Title");
	}

	@Override
	public String getPreferredNextStep() {
		return "Tiers";
	}

	@Override
	public String getPreferredPreviousStep() {
		return "Document";
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == customSepCB) {
			customSepTF.setEnabled(customSepCB.isSelected());
		} else if (e.getSource() == tierCustomSepCB) {
			tierCustomSepTF.setEnabled(tierCustomSepCB.isSelected());
		}
	}

}
