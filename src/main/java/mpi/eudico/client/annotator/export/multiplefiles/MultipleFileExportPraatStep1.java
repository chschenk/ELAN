package mpi.eudico.client.annotator.export.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Panel for step 1: File and tier selection
 * 
 * Select multiple files and tiers that are to be exported as 
 * praat file
 * 
 * @author aarsom
 * @version Feb, 2012
 */
@SuppressWarnings("serial")
public class MultipleFileExportPraatStep1 extends AbstractFilesAndTierSelectionStepPane{
	
	//private JCheckBox correctTimesCB;

	/**
	 * Constructor
	 * 
	 * @param mp
	 * @param transcription
	 */
	public MultipleFileExportPraatStep1(MultiStepPane mp,
			TranscriptionImpl transcription) {
		super(mp, transcription);
	}

	@Override
	public String getStepTitle() {		
		return ElanLocale.getString("MultiFileExportPraat.Title.Step1Title");
	}

}
