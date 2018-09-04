package mpi.eudico.client.annotator.recognizer.data;

/**
 * Container class to hold information regarding media files
 * 
 * For an audio signal the MediaDescriptor consists of a full path
 * to the audio file and a channel number that is 1 for a mono signal
 * and 1 or 2 for a stereo signal. 
 * 
 * For video signals the attribute channel is for the time being undefined. 
 * 
 * @author albertr
 *
 */
public class MediaDescriptor {
	public String mediaFilePath;
	public int channel;

	public MediaDescriptor() {
		this.mediaFilePath = "";
		this.channel = 1;
	}

	public MediaDescriptor(String mediaFilePath) {
		this.mediaFilePath = mediaFilePath;
		this.channel = 1;
	}
	
	public MediaDescriptor(String mediaFilePath, int channel) {
		this.mediaFilePath = mediaFilePath;
		this.channel = channel;
	}
}