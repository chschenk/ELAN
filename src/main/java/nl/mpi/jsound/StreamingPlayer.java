/**
 * 
 */
package nl.mpi.jsound;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author Han Sloetjes
 *
 */
public class StreamingPlayer implements Clip {
	private AudioInputStream inputStream;
	private AudioFormat format;
	private SourceDataLine outputDataLine;
	
	private int frameLengthInt; // is a long in AudioInputStream
	private long microsecondLength;
	private float durationMicroSec = 0;
	private float microSecFrame = 1;
	
	private int bufferSize = 96000; // 2 * 48000, 2 seconds
	private int frameSize = 1;
	private long microsecondPosition;
	//private long framePos;
	//private Thread writeThread;
	private WriteRun writeRunner;
	private long lastWriteStopFrame;
	private long lastWriteStartFrame;
	
	public StreamingPlayer(AudioInputStream inputStream,
			SourceDataLine outputDataLine) throws LineUnavailableException {
		this.inputStream = inputStream;
		format = inputStream.getFormat();
		this.outputDataLine = outputDataLine;
		if (!outputDataLine.isOpen()) {
			outputDataLine.open();
		}
		initialize();
	}

	
	public StreamingPlayer(AudioInputStream inputStream) throws LineUnavailableException {
		super();
		this.inputStream = inputStream;
		format = inputStream.getFormat();
		
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(dataLineInfo)) {
			throw new LineUnavailableException("Line not supported: " + dataLineInfo.toString());
		}
		outputDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
		outputDataLine.open(format);
		initialize();
	}

	private void initialize() throws LineUnavailableException {
		frameSize = format.getFrameSize();
		frameLengthInt = (int) inputStream.getFrameLength();
		if (format.getFrameRate() != AudioSystem.NOT_SPECIFIED) {
			durationMicroSec = (1000 * 1000) * (inputStream.getFrameLength() / format.getFrameRate());			
			bufferSize = (int)(frameSize * format.getFrameRate() * 2);//2 seconds
		} else {// throw an exception?
			if (format.getSampleRate() != AudioSystem.NOT_SPECIFIED) {
				// this might be wrong
				durationMicroSec = (1000 * 1000) * (inputStream.getFrameLength() / format.getFrameRate());
				bufferSize = (int)(frameSize * format.getSampleRate() * 2);//2 seconds
			} else {
				//throw new UnsupportedAudioFileException("Cannot determine the duration of the file");
				throw new LineUnavailableException("Cannot determine the duration of the file");
			}
		}
		microsecondLength = (long) durationMicroSec;
		//microSecFrame = durationMicroSec / inputStream.getFrameLength();
		microSecFrame = (1000 * 1000) / format.getFrameRate();
		inputStream.mark(frameLengthInt * frameSize);// mark at position 0 to be able to reset()
	}
	
	private long framesToBytes(long frames) {
		return frames * frameSize;
	}
	
	private long microsecondsToBytes(long microseconds) {
		return (long) (microseconds / microSecFrame) * frameSize;
	}
	
	private long framesToMicroseconds(long frames) {
		return (long) (frames * microSecFrame);
	}
	
	private long microsecondsToFrames(long microseconds) {
		return (long) (microseconds / microSecFrame);
	}

	/**
	 * @see javax.sound.sampled.DataLine#drain()
	 */
	@Override
	public void drain() {
		if (outputDataLine.isRunning()) {
			outputDataLine.stop();
		}
		outputDataLine.drain();
		// TODO stop writing, empty buffer?
	}

	/**
	 * @see javax.sound.sampled.DataLine#flush()
	 */
	@Override
	public void flush() {
		if (outputDataLine.isRunning()) {
			outputDataLine.stop();
		}
		outputDataLine.flush();
	}

	/**
	 * @see javax.sound.sampled.DataLine#start()
	 */
	@Override
	public void start() {
		if (!outputDataLine.isRunning()) {
			// lastWriteStartFrame = lastWriteStopFrame;
			// after last stop the position changed, maybe by the flushing?
			// with a 48000 frame rate source, the position at this point is 
			// always 24000 higher than as recorded in stop()
			lastWriteStartFrame = outputDataLine.getLongFramePosition();
			//System.out.println("Start frame: " + lastWriteStopFrame + " act: " + 
			//		lastWriteStartFrame);
			outputDataLine.start();
			
			writeRunner = new WriteRun(bufferSize);
			new Thread(writeRunner).start();
		}
	}

	/**
	 * @see javax.sound.sampled.DataLine#stop()
	 */
	@Override
	public void stop() {
		if (outputDataLine.isRunning()) {
			// store the current frame position, calculate time compared to the last stored position
			// determine the corresponding reader byte position		
//			lastWriteStopFrame = outputDataLine.getLongFramePosition();
//			System.out.println("Stop at pos: " + lastWriteStopFrame + 
//					" Av: " + outputDataLine.available());
			if (writeRunner != null) {
				writeRunner.stopWriting();
			}
			outputDataLine.stop();
			// the frame position just before and after stopping the data line 
			// can differ by 1000 to 2000 frames
			// after stop() apparently a blocked write operation still returns the number of bytes written
			lastWriteStopFrame = outputDataLine.getLongFramePosition();
			//System.out.println("Stopped at pos: " + outputDataLine.getFramePosition()
			//		+ " Av: " + outputDataLine.available());
			setMicrosecondPosition(microsecondPosition + framesToMicroseconds(
					lastWriteStopFrame - lastWriteStartFrame));
		}
	}

	/**
	 * @see javax.sound.sampled.DataLine#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return outputDataLine.isRunning();
	}

	/**
	 * @see javax.sound.sampled.DataLine#isActive()
	 */
	@Override
	public boolean isActive() {
		return outputDataLine.isActive();
	}

	/**
	 * @see javax.sound.sampled.DataLine#getFormat()
	 */
	@Override
	public AudioFormat getFormat() {
		return format;
	}

	/**
	 * @see javax.sound.sampled.DataLine#getBufferSize()
	 */
	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @see javax.sound.sampled.DataLine#available()
	 */
	@Override
	public int available() {
		return 0;
	}

	/**
	 * @see javax.sound.sampled.DataLine#getFramePosition()
	 */
	@Override
	public int getFramePosition() {
		return (int) microsecondsToFrames(getMicrosecondPosition());
	}

	/**
	 * @see javax.sound.sampled.DataLine#getLongFramePosition()
	 */
	@Override
	public long getLongFramePosition() {
		return microsecondsToFrames(getMicrosecondPosition());
	}

	/**
	 * @see javax.sound.sampled.DataLine#getMicrosecondPosition()
	 */
	@Override
	public long getMicrosecondPosition() {
		if (outputDataLine.isRunning()) {
			long deltaF = outputDataLine.getLongFramePosition() - lastWriteStartFrame;
			return microsecondPosition + framesToMicroseconds(deltaF);
		}
		return microsecondPosition;
	}

	/**
	 * @see javax.sound.sampled.DataLine#getLevel()
	 */
	@Override
	public float getLevel() {
		return 1.0f;
	}

	/**
	 * @see javax.sound.sampled.Line#getLineInfo()
	 */
	@Override
	public javax.sound.sampled.Line.Info getLineInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.sound.sampled.Line#open()
	 */
	@Override
	public void open() throws LineUnavailableException {
		if (!outputDataLine.isOpen()) {
			outputDataLine.open();
		}
	}

	/**
	 * @see javax.sound.sampled.Line#close()
	 */
	@Override
	public void close() {
		if (outputDataLine.isRunning()) {
			outputDataLine.stop();
		}
		if (outputDataLine.isOpen()) {
			outputDataLine.close();
		}
		//inputStream.close();
	}

	/**
	 * @see javax.sound.sampled.Line#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return outputDataLine.isOpen();
	}

	/**
	 * @see javax.sound.sampled.Line#getControls()
	 */
	@Override
	public Control[] getControls() {
		return outputDataLine.getControls();
	}

	/**
	 * @see javax.sound.sampled.Line#isControlSupported(javax.sound.sampled.Control.Type)
	 */
	@Override
	public boolean isControlSupported(Type control) {
		return outputDataLine.isControlSupported(control);
	}

	/**
	 * @see javax.sound.sampled.Line#getControl(javax.sound.sampled.Control.Type)
	 */
	@Override
	public Control getControl(Type control) {
		return outputDataLine.getControl(control);
	}

	/**
	 * @see javax.sound.sampled.Line#addLineListener(javax.sound.sampled.LineListener)
	 */
	@Override
	public void addLineListener(LineListener listener) {
		outputDataLine.addLineListener(listener);
	}

	/**
	 * @see javax.sound.sampled.Line#removeLineListener(javax.sound.sampled.LineListener)
	 */
	@Override
	public void removeLineListener(LineListener listener) {
		outputDataLine.removeLineListener(listener);
	}

	/**
	 * @see javax.sound.sampled.Clip#open(javax.sound.sampled.AudioFormat, byte[], int, int)
	 */
	@Override
	public void open(AudioFormat format, byte[] data, int offset, int bufferSize)
			throws LineUnavailableException {
		throw new LineUnavailableException("Unsupported");

	}

	/**
	 * @see javax.sound.sampled.Clip#open(javax.sound.sampled.AudioInputStream)
	 */
	@Override
	public void open(AudioInputStream stream) throws LineUnavailableException,
			IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.sound.sampled.Clip#getFrameLength()
	 */
	@Override
	public int getFrameLength() {
		return frameLengthInt;
	}

	public long getFrameLengthLong() {
		return inputStream.getFrameLength();
	}
	
	/**
	 * @see javax.sound.sampled.Clip#getMicrosecondLength()
	 */
	@Override
	public long getMicrosecondLength() {
		return microsecondLength;
	}

	/**
	 * @see javax.sound.sampled.Clip#setFramePosition(int)
	 */
	@Override
	public void setFramePosition(int frames) {
		setPosition(framesToBytes(frames));
		microsecondPosition = framesToMicroseconds(frames);
	}

	/**
	 * @see javax.sound.sampled.Clip#setMicrosecondPosition(long)
	 */
	@Override
	public void setMicrosecondPosition(long microseconds) {
		this.microsecondPosition = microseconds;
		setPosition(microsecondsToBytes(microseconds));
	}
	
	private void setPosition(long bytePosition) {
		// stop the output
		if (outputDataLine.isRunning()) {
			outputDataLine.stop();			
		}
		outputDataLine.flush();//superfluous if stop has been called?
		// reset and skip the input to the correct position
		if (bytePosition % frameSize != 0) {
			bytePosition -= (bytePosition % frameSize);
		}
		try {
			//System.out.println("Av b: " + inputStream.available());// bytes from current position to end
			inputStream.reset();
			//System.out.println("Av a: " + inputStream.available());// total amount of bytes
			long startSampleByte = bytePosition;
			// skipping doesn't always move with the correct number of bytes, so try several times
			long skipped = inputStream.skip(startSampleByte);
			while (skipped < startSampleByte) {
				long nextReq = startSampleByte - skipped;
				long nextSkip = inputStream.skip(nextReq);
				skipped += nextSkip;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}

	/**
	 * @see javax.sound.sampled.Clip#setLoopPoints(int, int)
	 */
	@Override
	public void setLoopPoints(int start, int end) {
		// stub, not supported
	}

	/**
	 * @see javax.sound.sampled.Clip#loop(int)
	 */
	@Override
	public void loop(int count) {
		// stub not supported
	}

	
	private class WriteRun implements Runnable {
		byte[] buffer;
		boolean stopWritingRequested = false;
		
		public WriteRun(int bufferSize) {
			super();
			buffer = new byte[bufferSize];
		}

		void stopWriting() {
			stopWritingRequested = true;
		}
		
		@Override
		public void run() {
			while (!stopWritingRequested) {
				try {
					int readBytes = inputStream.read(buffer, 0, buffer.length);
					
					if (readBytes < frameSize) {
						// generate stop event end of media
						break;
					}
					// blocks if the buffer is filled
					if (!stopWritingRequested) {
						int written = outputDataLine.write(buffer, 0, readBytes);
						//System.out.println("P: " + outputDataLine.getLongFramePosition() + "  W: " + 
						//		written + " + R: " + readBytes);
						if (written <= 0) {
							break;
						}
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
					break;
				}
			}
			
		}		
		
	}
}
