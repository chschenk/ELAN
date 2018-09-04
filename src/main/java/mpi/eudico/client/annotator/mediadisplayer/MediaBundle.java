package mpi.eudico.client.annotator.mediadisplayer;

/**
 * A class to transfer media data of any kind (so far only a url).
 * @author michahulsbosch
 *
 */
public class MediaBundle {
	private String mediaUrl;

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}
	
}
