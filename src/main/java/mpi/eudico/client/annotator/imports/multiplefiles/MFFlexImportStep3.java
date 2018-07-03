package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

/**
 * Step 3: Abstract Step pane to set the 
 * 'save as' settings for the output files. 
 * 
 * @author aarsom
 * @version April, 2013
 */
public class MFFlexImportStep3 extends AbstractMFImportStep3{

	public MFFlexImportStep3(MultiStepPane multiStepPane) {
		super(multiStepPane);		
	}
	
	/**
	 * Set the praat preference strings
	 */
	@Override
	protected void setPreferenceStrings() {	
		saveWithOriginalNames = "MFFlexImport.saveWithOriginalNames";
		saveInOriginalFolder = "MFFlexImport.saveInOriginalFolder";
		saveInRelativeFolder = "MFFlexImport.saveInRelativeFolder";
		saveInRelativeFolderName = "MFFlexImport.saveInRelativeFolderName";
		saveInSameFolderName = "MFFlexImport.saveInSameFolderName";	
	}
}