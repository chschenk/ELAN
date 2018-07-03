package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

public class MFToolboxImportStep3 extends AbstractMFImportStep3{

	public MFToolboxImportStep3(MultiStepPane multiStepPane) {
		super(multiStepPane);		
	}
	
	/**
	 * Set the praat preference strings
	 */
	@Override
	protected void setPreferenceStrings() {	
		saveWithOriginalNames = "MFToolBoxImport.saveWithOriginalNames";
		saveInOriginalFolder = "MFToolBoxImport.saveInOriginalFolder";
		saveInRelativeFolder = "MFToolBoxImport.saveInRelativeFolder";
		saveInRelativeFolderName = "MFToolBoxImport.saveInRelativeFolderName";
		saveInSameFolderName = "MFToolBoxImport.saveInSameFolderName";	
	}	
}
