package mpi.eudico.client.annotator.export.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * The first step in multiple file export to Theme format
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class MultipleFileThemeExportStep1 extends
		AbstractFilesAndTierSelectionStepPane {

	public MultipleFileThemeExportStep1(MultiStepPane mp,
			TranscriptionImpl transcription) {
		super(mp, transcription);
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("MultiFileExportTheme.Step1.Title");
	}

}
