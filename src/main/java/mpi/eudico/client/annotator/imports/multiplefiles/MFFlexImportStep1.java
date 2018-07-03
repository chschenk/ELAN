package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.FileExtension;

/**
 * Step 1: Step pane for selecting multiple 
 * flextext files that are to be imported 
 * 
 * @author aarsom
 * @version April, 2013
 */
@SuppressWarnings("serial")
public class MFFlexImportStep1 extends AbstractMFImportStep1 {

	public MFFlexImportStep1(MultiStepPane mp) {
		super(mp);
	}
	
	@Override
	protected Object[] getMultipleFiles() {
    	Object[] files = getMultipleFiles(ElanLocale.getString("MultiFileImport.Flex.Select"),
       			FileExtension.FLEX_EXT, "LastUsedFlexDir", FileChooser.FILES_AND_DIRECTORIES);    	

    	if ((files == null) || (files.length == 0)) {
    		return null;
    	}
    	
    	return getFilesFromFilesAndFolders(files, FileExtension.FLEX_EXT);  
	}
}
