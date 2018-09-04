package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import java.util.List;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * EncoderInfo for export of a TranscriptionImpl object. 
 * Contains a reference to the transcription, media header lines including the duration
 * of the files (obtained from the players) and shared parameters.
 */
public class DelimitedTextEncoderInfoTrans extends DelimitedTextEncoderInfo {
	/** the transcription to export */
	private TranscriptionImpl transcription;
	/** "media header" lines containing information about the media files, including duration and fps. etc. */
	private List<String> mediaHeaderLines;
	
	/**
	 * Constructor with the transcription as parameter (not null).
	 * 
	 * @param transcription the transcription
	 */
	public DelimitedTextEncoderInfoTrans(TranscriptionImpl transcription) {
		super();
		if (transcription == null) throw new NullPointerException("The Transcription is null.");
		this.transcription = transcription;
	}

	public TranscriptionImpl getTranscription() {
		return transcription;
	}
	
	public List<String> getMediaHeaderLines() {
		return mediaHeaderLines;
	}
	public void setMediaHeaderLines(List<String> headerLines) {
		this.mediaHeaderLines = headerLines;
	}
	
}
