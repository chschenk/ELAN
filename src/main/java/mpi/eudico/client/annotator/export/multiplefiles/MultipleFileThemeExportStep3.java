package mpi.eudico.client.annotator.export.multiplefiles;

import java.io.IOException;
import java.util.List;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.theme.ThemeEncoder;
import mpi.eudico.server.corpora.clomimpl.theme.ThemeEncoderInfo;

/**
 * The export progress step. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class MultipleFileThemeExportStep3 extends
		AbstractMultiFileExportProgessStepPane {
	private ThemeEncoder themeEncoder;
	private boolean useCVForVVT;
	private boolean useTierNameAsActor;
	
	
	/**
	 * Constructor.
	 * @param multiPane
	 */
	public MultipleFileThemeExportStep3(MultiStepPane multiPane) {
		super(multiPane);
	}

	@Override
	protected boolean doExport(TranscriptionImpl transImpl, String fileName) {
		ThemeEncoderInfo tei = new ThemeEncoderInfo();
		tei.setTierNameAsActor(useTierNameAsActor);
		tei.setUseCVforVVT(useCVForVVT);
		
        List<TierImpl> selectedTiersInThisTrans = transImpl.getTiersWithIds(selectedTiers);
		
        try {
        	themeEncoder.encodeAndSave(transImpl, tei, selectedTiersInThisTrans, fileName);
        } catch (IOException ioe) {
        	return false;
        } catch (Throwable t) {
        	return false;
        }
        
		return true;
	}

	/**
	 * Retrieves some settings from previous steps.
	 */
	@Override
	public void enterStepForward() {
		if (themeEncoder == null) {
			themeEncoder = new ThemeEncoder();
		}
		
		Object stepProp = multiPane.getStepProperty("UseCVForVVT");
		if (stepProp instanceof Boolean) {
			useCVForVVT = (Boolean) stepProp;
		} else {
			useCVForVVT = false;
		}
		
		stepProp = multiPane.getStepProperty("TierNameAsActor");
		if (stepProp instanceof Boolean) {
			useTierNameAsActor = (Boolean) stepProp;
		} else {
			useTierNameAsActor = true;
		}
		
		super.enterStepForward();
	}
	
	

}
