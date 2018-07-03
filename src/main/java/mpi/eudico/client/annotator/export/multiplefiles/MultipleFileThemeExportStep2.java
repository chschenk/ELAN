package mpi.eudico.client.annotator.export.multiplefiles;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.FileExtension;

/**
 * Step for specifying settings for export.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class MultipleFileThemeExportStep2 extends
		AbstractMultiFileExportSaveSettingsStepPane {
	private JCheckBox useCVForVvtCB;
	private JCheckBox useTierNameAsActorCB;
	private String useCVForVVT;
	private String useTierNameAsActor;
	
	/**
	 * Constructor
	 * @param multiStepPane
	 */
	public MultipleFileThemeExportStep2(MultiStepPane multiStepPane) {
		super(multiStepPane);
	}

	@Override
	protected void setPreferenceStrings() {
		// TODO modify
		saveWithOriginalNames = "MultiFileExportThemeDialog.saveWithOriginalNames";
		saveInOriginalFolder = "MultiFileExportThemeDialog.saveInOriginalFolder";
		saveInRelativeFolder = "MultiFileExportThemeDialog.saveInRelativeFolder";
		saveInRelativeFolderName = "MultiFileExportThemeDialog.saveInRelativeFolderName";
		saveInSameFolderName = "MultiFileExportThemeDialog.saveInSameFolderName";
		dontCreateEmptyFiles = null;
		useCVForVVT = "MultiFileExportThemeDialog.useCVForVVT";
		useTierNameAsActor = "MultiFileExportThemeDialog.tierNameAsActor";
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("MultiFileExportPraat.Title.Step2Title");// change or reuse?
	}

	@Override
	protected String[] getExportExtensions() {
		return FileExtension.TEXT_EXT;
	}

	@Override
	public boolean leaveStepForward() {
		// store other options properties
		multiPane.putStepProperty("UseCVForVVT", Boolean.valueOf(useCVForVvtCB.isSelected()));
		multiPane.putStepProperty("TierNameAsActor", Boolean.valueOf(useTierNameAsActorCB.isSelected()));
		
		return super.leaveStepForward();
	}

	@Override
	protected void initFileNameOptionsPanel() {
		super.initFileNameOptionsPanel();
		fileExtComboBox.setEnabled(false);
	}

	@Override
	protected void initOtherOptionsPanel() {
		otherOptionsPanel = new JPanel(new GridBagLayout());
		otherOptionsPanel.setBorder(new TitledBorder(
				ElanLocale.getString("ExportTiersDialog.Label.OtherOptions")));
		
		useCVForVvtCB = new JCheckBox(
			ElanLocale.getString("MultiFileExportTheme.Label.UseCV"), false);
		useTierNameAsActorCB = new JCheckBox(
				ElanLocale.getString("MultiFileExportTheme.Label.TierAsActor"), true);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = insets;
		gbc.weightx = 1.0;
		otherOptionsPanel.add(useCVForVvtCB, gbc);
		gbc.gridy = 1;
		otherOptionsPanel.add(useTierNameAsActorCB, gbc);
	}

	@Override
	protected void savePreferences() {
		super.savePreferences();
		
		if (useCVForVVT != null) {
			Preferences.set(useCVForVVT, useCVForVvtCB.isSelected(), null);
		}
		if (useTierNameAsActor != null) {
			Preferences.set(useTierNameAsActor, useTierNameAsActorCB.isSelected(), null);
		}
	}

	@Override
	protected void loadPreferences() {
		super.loadPreferences();
		
		Boolean prefValue;
		if (useCVForVVT != null) {
			prefValue = Preferences.getBool(useCVForVVT, null);
			if (prefValue != null) {
				useCVForVvtCB.setSelected(prefValue);
			}
		}
		
		if (useTierNameAsActor != null) {
			prefValue = Preferences.getBool(useTierNameAsActor, null);
			if (prefValue != null) {
				useTierNameAsActorCB.setSelected(prefValue);
			}
		}
	}

	
	
}
