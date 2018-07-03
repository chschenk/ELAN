package mpi.eudico.client.annotator.export.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.FileExtension;

public class MultipleFileFlexExportStep4 extends AbstractMultiFileExportSaveSettingsStepPane{		
	
	/**
	 * Constructor
	 * 
	 * @param multiStepPane
	 */
    public MultipleFileFlexExportStep4(MultiStepPane multiStepPane) {
        super(multiStepPane);  
    }
    
    /**
     * Sets the toolbox preference strings
     */
	@Override
	protected void setPreferenceStrings() {
		saveWithOriginalNames = "MultiFileExportFlexDialog.saveWithOriginalNames";
		saveInOriginalFolder = "MultiFileExportFlexDialog.saveInOriginalFolder";
		saveInRelativeFolder = "MultiFileExportFlexDialog.saveInRelativeFolder";
		saveInRelativeFolderName = "MultiFileExportFlexDialog.saveInRelativeFolderName";
		saveInSameFolderName = "MultiFileExportFlexDialog.saveInSameFolderName";
		dontCreateEmptyFiles = "MultiFileExportFlexDialog.dontCreateEmptyFiles";
	}

	@Override
	public String getStepTitle() {	
		return ElanLocale.getString("MultiFileExportFlex.Step4.Title");
	}
	
	@Override
	protected String[] getExportExtensions() {
		return FileExtension.FLEX_EXT;
	}
}
