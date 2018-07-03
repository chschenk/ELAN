package mpi.eudico.client.annotator.export.multiplefiles;

import java.io.IOException;
import java.util.List;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.praat.PraatTGEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.praat.PraatTextGridEncoder;

/**
 * Final Step 3 
 * Actual export id done here.
 * The ui is progess monitor
 * 
 * @author aarsom
 * @version Feb,2012
 *
 */
@SuppressWarnings("serial")
public class MultipleFileExportPraatStep3 extends AbstractMultiFileExportProgessStepPane{

	private boolean correctTimes;
	private String encoding;

	/**
	 * Constructor
	 * 
	 * @param multiPane
	 */
	public MultipleFileExportPraatStep3(MultiStepPane multiPane) {
		super(multiPane);	
	}

	/**
	 * Actual export
	 * 
	 */
	@Override
	protected boolean doExport(TranscriptionImpl transImpl, String fileName) {      
        long begin = 0l;
        long end = transImpl.getLatestTime();
        
        long mediaOffset = 0L;

        if (correctTimes) {
            List<MediaDescriptor> mds = transImpl.getMediaDescriptors();

            if ((mds != null) && (mds.size() > 0)) {
                mediaOffset = mds.get(0).timeOrigin;
            }
        }
        
        PraatTGEncoderInfo encInfo = new PraatTGEncoderInfo(begin, end);
        encInfo.setEncoding(encoding);
        encInfo.setOffset(mediaOffset);
        encInfo.setExportSelection(false);
        
        PraatTextGridEncoder encoder = new PraatTextGridEncoder();
        try {
			encoder.encodeAndSave(transImpl, encInfo, 
					transImpl.getTiersWithIds(selectedTiers),
					fileName);
		} catch (IOException e) {			
			e.printStackTrace();
			return false;
		}
        
        return true;
	}
	
	 /**
     * Calls doFinish.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
    	 correctTimes = (Boolean) multiPane.getStepProperty("CorrectTimes");
    	 encoding = (String)multiPane.getStepProperty("Encoding");    	
       super.enterStepForward();
    }
}
