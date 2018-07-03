package nl.mpi.jsound;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * A subclass of and a wrapper around {@link AudioInputStream} which can be used for extracting
 * a segment using {@link AudioSystem.#write(AudioInputStream, javax.sound.sampled.AudioFileFormat.Type, java.io.OutputStream)}.
 * For that it sets framePos and frameLength, the 'virtual' start and end point.
 * 
 * @author Han Sloetjes
 */
public class SegmentAudioInputStream extends AudioInputStream {
	private long startSampleFrame;

	/**
	 * Constructor.
	 * @param stream the original AudioSourceStream
	 * @param startTimeMicro the segment's start time in microseconds
	 * @param endTimeMicro the segment's end time in microseconds
	 */
	public SegmentAudioInputStream(AudioInputStream aiStream, long startTimeMicro, 
			long endTimeMicro) throws IOException {
		super(aiStream, aiStream.getFormat(), aiStream.getFrameLength());
		
		// calculate the sample boundaries, rounding down the begin, rounding up the end
		float durationMicroSec = (1000 * 1000) * (aiStream.getFrameLength() / format.getFrameRate());
		float microSecFrame = durationMicroSec / aiStream.getFrameLength();
		
		// calculate the start position in bytes to skip to
		framePos = (long) (startTimeMicro / microSecFrame);//frame number, rounded down
		startSampleFrame = framePos;
		
		// calculate the end position in frames to use as the length
		long endSampleFrame = (long) (endTimeMicro / microSecFrame);
		if (endTimeMicro % microSecFrame != 0) {
			// add one frame
			endSampleFrame++;
		}
		
		// the segment is endSampleFrame - startSampleFrame frames long, set frameLength 
		// to endSampleFrame @see getFrameLength
		frameLength = endSampleFrame;
		// jump to the start position, startSampleFrame
		aiStream.reset();
		long startSampleByte = startSampleFrame * aiStream.getFormat().getFrameSize();
		// skipping doesn't always move with the correct number of bytes, so try several times
		long skipped = aiStream.skip(startSampleByte);
		while (skipped < startSampleByte) {
			long nextReq = startSampleByte - skipped;
			long nextSkip = aiStream.skip(nextReq);
			skipped += nextSkip;
		}
	}

	/**
	 * @return the number of frames in this file i.e. in the file after clipping,
	 * the difference between the set length and the start sample of the segment/clip.
	 * This is important so that the correct data size can be written in the header of
	 * the .wav file.
	 * 
	 * @return the length in frames of the "selected" fragment and of the new file when 
	 * this input stream is saved via 
	 * 
	 * {@link AudioSystem#write(AudioInputStream, javax.sound.sampled.AudioFileFormat.Type, java.io.File)}
	 */
	@Override
	public long getFrameLength() {
		return frameLength - startSampleFrame;
	}	
}
