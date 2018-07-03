package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

public class MFPraatImportStep3 extends AbstractMFImportStep3{

	public MFPraatImportStep3(MultiStepPane multiStepPane) {
		super(multiStepPane);		
	}
	
	/**
	 * Set the praat preference strings
	 */
	@Override
	protected void setPreferenceStrings() {	
		saveWithOriginalNames = "MFPraatImport.saveWithOriginalNames";
		saveInOriginalFolder = "MFPraatImport.saveInOriginalFolder";
		saveInRelativeFolder = "MFPraatImport.saveInRelativeFolder";
		saveInRelativeFolderName = "MFPraatImport.saveInRelativeFolderName";
		saveInSameFolderName = "MFPraatImport.saveInSameFolderName";	
	}
}
