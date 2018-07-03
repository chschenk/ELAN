package mpi.eudico.client.annotator.tier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ExportOverlapsMultiCommand;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.client.annotator.util.FileExtension;

/**
 * Monitors the progress of the process.
 * 
 * @author Han Sloetjes
 *
 */
public class ExportOverlapsStep2 extends ProgressStepPane {
	
    /** Character Encoding of export file */
    protected String encoding = FileChooser.UTF_8;

	public ExportOverlapsStep2(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();
	}

	/**
	 * Prompts for export location and starts the process.
	 */
	@Override
	public void enterStepForward() {
		doFinish();
	}
	
	/**
	 * After creating the command the window should not be closed
	 */
	@Override
	public boolean doFinish() {
		completed = false;
		multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
		File f = promptForFile(ElanLocale.getString(
			"ExportTabDialog.Title"), null, FileExtension.TEXT_EXT, true, null);
		
		if (f != null) {
			String refTier = (String) multiPane.getStepProperty("Tier-1");
			List<String> selTiers2 = (List<String>) multiPane.getStepProperty("Tiers-2");
			ArrayList<File> files = (ArrayList<File>) multiPane.getStepProperty("files");
			List<String> filePaths = new ArrayList<String>(files.size());
			for (File ff : files) {
				filePaths.add(ff.getAbsolutePath());
			}
			
			ExportOverlapsMultiCommand expCom = new ExportOverlapsMultiCommand("ExportOverlapMulti");
			expCom.addProgressListener(this);
			expCom.execute(null, new Object[]{filePaths, refTier, selTiers2, f.getAbsolutePath(), encoding});
		} else {
			multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
			multiPane.previousStep();
		}
		
		return false;
	}

	/**
	 * Returns the title
	 */
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("ExportOverlapsDialog.Exporting");
	}

	/**
     * Prompts the user for a file name and location.
     *
     * @param chooserTitle the title for the save dialog
     * @param extensions the file extensions (one of the constants of FileExtension)
     * @param filter the FileFilter(s) for the dialog
     * @param showEncodingBox if true, a combobox for selecting the encoding for the output file 
     * @param encodings the list of encodings the user can choose from
     * 
     * @return a file (unique) path
     */
    protected File promptForFile(String chooserTitle, List<String[]> extensions, 
    		String[] mainExt, boolean showEncodingBox, String[] encodings) {
    	
    	FileChooser chooser = new FileChooser(null);
        if (showEncodingBox) {
            chooser.createAndShowFileAndEncodingDialog(chooserTitle, FileChooser.SAVE_DIALOG, extensions, mainExt, "LastUsedExportDir", encodings, null, null);
            encoding = chooser.getSelectedEncoding();    
        } else {
            chooser.createAndShowFileDialog(chooserTitle, FileChooser.SAVE_DIALOG, extensions, mainExt, "LastUsedExportDir", null);
        } 
        return chooser.getSelectedFile();
    }
    
}
