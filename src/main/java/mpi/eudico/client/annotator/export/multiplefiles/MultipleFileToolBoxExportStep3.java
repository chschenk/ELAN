package mpi.eudico.client.annotator.export.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.FileExtension;

/**
 * Panel for step 3: Export save as settings
 * 
 * Set the 'save as' setting for the files
 * that would be exported
 * 
 * @author aarsom
 * @version Feb, 2012
 */
public class MultipleFileToolBoxExportStep3 extends AbstractMultiFileExportSaveSettingsStepPane{		
	
	/**
	 * Constructor
	 * 
	 * @param multiStepPane
	 */
    public MultipleFileToolBoxExportStep3(MultiStepPane multiStepPane) {
        super(multiStepPane);  
    }
    
    /**
     * Sets the toolbox preference strings
     */
	@Override
	protected void setPreferenceStrings() {
		saveWithOriginalNames = "MultiFileExportToolBoxDialog.saveWithOriginalNames";
		saveInOriginalFolder = "MultiFileExportToolBoxDialog.saveInOriginalFolder";
		saveInRelativeFolder = "MultiFileExportToolBoxDialog.saveInRelativeFolder";
		saveInRelativeFolderName = "MultiFileExportToolBoxDialog.saveInRelativeFolderName";
		saveInSameFolderName = "MultiFileExportToolBoxDialog.saveInSameFolderName";
		dontCreateEmptyFiles = "MultiFileExportToolBoxDialog.dontCreateEmptyFiles";
	}

	@Override
	public String getStepTitle() {	
		return ElanLocale.getString("MultiFileExportToolbox.Title.Step3Title");
	}
	
	@Override
	protected String[] getExportExtensions() {
		return FileExtension.TOOLBOX_TEXT_EXT;
	}
}


