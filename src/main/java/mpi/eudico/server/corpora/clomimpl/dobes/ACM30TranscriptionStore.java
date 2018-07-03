package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.chat.CHATEncoder;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSet;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSetRecord;
import mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxEncoder;

/**
 * A TranscriptionStore that corresponds to EAF v3.0.<br>
 * Version 3.0 adds support cross reference links between annotations
 *  
 * 
 * @see ACM28TranscriptionStore
 *
 * @version October 2016 support for cross-reference links
 */
public class ACM30TranscriptionStore extends ACM28TranscriptionStore {

	public ACM30TranscriptionStore() {
		super();
	}

	/**
	 * @see mpi.eudico.server.corpora.clomimpl.dobes.ACM28TranscriptionStore#storeTranscriptionIn(mpi.eudico.server.corpora.clom.Transcription, mpi.eudico.server.corpora.clom.EncoderInfo, java.util.List, java.lang.String, int)
	 */
	@Override
	public void storeTranscriptionIn(Transcription theTranscription, EncoderInfo encoderInfo, 
			List<TierImpl> tierOrder, String path, int format) throws IOException {

    	/* Get the URN, to make sure one is created if it didn't exist yet. */
    	theTranscription.getURN();
    	

		switch (format) {
			case TranscriptionStore.EAF:
				if (this.fileToWriteXMLinto != null) {
					path = fileToWriteXMLinto.getAbsolutePath();
				}

				new EAF30Encoder().encodeAndSave(theTranscription, null, tierOrder, path);
				break;	
			case TranscriptionStore.EAF_2_8:
				if (this.fileToWriteXMLinto != null) {
					path = fileToWriteXMLinto.getAbsolutePath();
				}

				new EAF28Encoder().encodeAndSave(theTranscription, null, tierOrder, path);
				break;
				
			case TranscriptionStore.EAF_2_7:
				if (this.fileToWriteXMLinto != null) {
					path = fileToWriteXMLinto.getAbsolutePath();
				}
								
				new EAF27Encoder().encodeAndSave(theTranscription, null, tierOrder, path);
				break;
				
			case TranscriptionStore.CHAT:
			
				new CHATEncoder().encodeAndSave(theTranscription, encoderInfo, tierOrder, path);
				break;
				
			case TranscriptionStore.SHOEBOX:
			
				new ShoeboxEncoder(path).encodeAndSave(theTranscription, encoderInfo, tierOrder, path);
			
				break;
				
			default:
				break;
		}
    	
//		super.storeTranscriptionIn(theTranscription, encoderInfo, tierOrder, path,
//				format);
	}

	/**
	 * Uses the EAF30Encoder to save the template.
	 * 
	 * @see mpi.eudico.server.corpora.clomimpl.dobes.ACM28TranscriptionStore#storeTranscriptionAsTemplateIn(mpi.eudico.server.corpora.clom.Transcription, java.util.List, java.lang.String)
	 */
	@Override
	public void storeTranscriptionAsTemplateIn(Transcription theTranscription,
			List<TierImpl> tierOrder, String path) throws IOException {
	    	
	    if (this.fileToWriteXMLinto != null) {
	    	path = fileToWriteXMLinto.getAbsolutePath();	
	    }
	    
	    new EAF30Encoder().encodeAsTemplateAndSave(theTranscription, tierOrder, path);
	}

	/**
	 * There can now be more than one sets of reference links.
	 * 
	 * @see mpi.eudico.server.corpora.clomimpl.dobes.ACM28TranscriptionStore#loadReferenceLinks(mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl, mpi.eudico.server.corpora.clomimpl.abstr.Parser)
	 */
	@Override
	protected void loadReferenceLinks(TranscriptionImpl transcription,
			Parser parser) {	
		List<RefLinkSetRecord> refLinkRecords = parser.getRefLinkSetList(transcription.getPathName());
		
		if (refLinkRecords != null && !refLinkRecords.isEmpty()) {
			for (RefLinkSetRecord rlsRecord : refLinkRecords) {
				 RefLinkSet refLinkSet = rlsRecord.fabricate(transcription, 
						 parser.getExternalReferences(transcription.getPathName()));

		         transcription.addRefLinkSet(refLinkSet);
			}
		}
		
	}

	
}
