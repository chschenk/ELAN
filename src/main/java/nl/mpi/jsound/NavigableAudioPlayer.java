package nl.mpi.jsound;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * An implementation of the Clip interface based on a RandomAccessFile as the 
 * audio input "stream". The JavaSound API's {@link AudioInputStream} is first used 
 * to detect the format and other properties. The size in bytes of the audio file's
 * header is inferred from the total files size in bytes and the number of frames
 * and the frame size detected by {@link AudioInputStream}. The end of the header
 * will be the "virtual" byte index 0 for read operations.
 * <p> 
 * The advantage of this approach is that the audio doesn't have to be read into memory
 * completely, like the default Clip implementation does, and can seek and jump to a random
 * position in the file, unlike the default BufferedInputStream of {@link AudioInputStream}
 * (the skip(long n) method in that class stores all bytes skipped in a buffer, so runs
 * out of memory easily with bigger files).
 * <p>
 * This is still somewhat experimental, improvements might still be needed.
 * 
 * @version 1.0 Dec 2017
 * @author Han Sloetjes
 */
public class NavigableAudioPlayer implements Clip {
	private final Logger LOG = Logger.getLogger("JavaSoundMPI");
	private AudioFormat format;
	private SourceDataLine outputDataLine;
	private RandomAccessFile raAudioFile;
	private long headerSizeBytes;
	private DataLine.Info playerInfo;
	private List<LineListener> lineListeners;
	
	private int frameLengthInt; // is a long in AudioInputStream
	private long frameLength;
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
	
	/**
	 * Constructor. After returning from this constructor, the AudioInputStream is not used
	 * anymore by this class (so can be closed if not otherwise used).
	 * 
	 * @param inputStream the stream providing the format and some other properties to this player 
	 * @param audioFile the audio file to play (the source)
	 * @param outputDataLine the data line receiving the bytes (the sink)
	 * 
	 * @throws LineUnavailableException if the output data line can not be opened
	 * @throws IOException if creating a {@link RandomAccessFile} for the audio file fails
	 */
	public NavigableAudioPlayer(AudioInputStream inputStream, File audioFile,
			SourceDataLine outputDataLine) throws LineUnavailableException, IOException {
		format = inputStream.getFormat();
		frameLength = inputStream.getFrameLength();
		raAudioFile = new RandomAccessFile(audioFile, "r");
		
		this.outputDataLine = outputDataLine;
		if (!outputDataLine.isOpen()) {
			outputDataLine.open();
		}
		initialize();
	}

	/**
	 * Constructor. After returning from this constructor, the AudioInputStream is not used
	 * anymore by this class (so can be closed if not otherwise used).
	 *  
	 * @param inputStream the stream providing the format and some other properties to this player 
	 * @param audioFile the audio file to play (the source)
	 * 
	 * @throws LineUnavailableException if the output data line can not be created or opened
	 * @throws IOException if creating a {@link RandomAccessFile} for the audio file fails
	 */
	public NavigableAudioPlayer(AudioInputStream inputStream, File audioFile) throws 
		LineUnavailableException, IOException {
		super();
		format = inputStream.getFormat();
		frameLength = inputStream.getFrameLength();
		raAudioFile = new RandomAccessFile(audioFile, "r");
		
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(dataLineInfo)) {
			throw new LineUnavailableException("Line not supported: " + dataLineInfo.toString());
		}
		outputDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
		outputDataLine.open(format);
		initialize();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param audioFile the audio file to play (the source)
	 * @param format the {@link AudioFormat} corresponding to the input file
	 * @param dataFrameLength the number of actual frames in the audio file (so that
	 * 		the size of the header can be calculated.
	 * 
	 * @throws LineUnavailableException if the output data line can not be created or opened
	 * @throws IOException if creating a {@link RandomAccessFile} for the audio file fails
	 */
	public NavigableAudioPlayer(File audioFile, AudioFormat format, long dataFrameLength) throws 
		LineUnavailableException, IOException {
		super();
		this.format = format;
		raAudioFile = new RandomAccessFile(audioFile, "r");
		this.frameLength = dataFrameLength;
		
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(dataLineInfo)) {
			throw new LineUnavailableException("Line not supported: " + dataLineInfo.toString());
		}
		outputDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
		outputDataLine.open(format);
		initialize();
	}

	/**
	 * Initializes several fields, creates a list for line listeners
	 * @throws LineUnavailableException if the duration of the file can not be determined
	 * @throws IOException if there are problems seeking a position in the source file.
	 */
	private void initialize() throws LineUnavailableException, IOException {
		frameSize = format.getFrameSize();
		frameLengthInt = (int) frameLength;
		if (format.getFrameRate() != AudioSystem.NOT_SPECIFIED) {
			durationMicroSec = (1000 * 1000) * (frameLength / format.getFrameRate());			
			bufferSize = (int)(frameSize * format.getFrameRate() * 2);//2 seconds
		} else {// throw an exception?
			if (format.getSampleRate() != AudioSystem.NOT_SPECIFIED) {
				// this might be wrong
				durationMicroSec = (1000 * 1000) * (frameLength / format.getFrameRate());
				bufferSize = (int)(frameSize * format.getSampleRate() * 2);//2 seconds
			} else {
				//throw new UnsupportedAudioFileException("Cannot determine the duration of the file");
				throw new LineUnavailableException("Cannot determine the duration of the file");
			}
		}
		microsecondLength = (long) durationMicroSec;
		//microSecFrame = durationMicroSec / inputStream.getFrameLength();
		microSecFrame = (1000 * 1000) / format.getFrameRate();
		headerSizeBytes = raAudioFile.length() - (frameLength * frameSize);
		raAudioFile.seek(headerSizeBytes);
		lineListeners = new ArrayList<LineListener>();
	}
	
	/**
	 * @param frames the number of frames (samples) to convert
	 * @return the number of bytes needed for the number of frames
	 */
	private long framesToBytes(long frames) {
		return frames * frameSize;
	}
	
	/**
	 * @param microseconds the time value in microseconds
	 * @return the number of bytes corresponding to the number of microseconds
	 */
	private long microsecondsToBytes(long microseconds) {
		return (long) (microseconds / microSecFrame) * frameSize;
	}
	
	/**
	 * @param frames the number of frames to convert
	 * @return the number of microseconds corresponding to the number of frames (samples)
	 */
	private long framesToMicroseconds(long frames) {
		return (long) (frames * microSecFrame);
	}
	
	/**
	 * @param microseconds the number of microseconds to convert
	 * @return the corresponding number of frames (rounded down if needed)
	 */
	private long microsecondsToFrames(long microseconds) {
		return (long) (microseconds / microSecFrame);
	}

	/**
	 * @see javax.sound.sampled.DataLine#drain()
	 */
	@Override
	public void drain() {
		if (outputDataLine.isRunning()) {
			outputDataLine.stop();// this stops any writing that is going on
		}
		outputDataLine.drain();
	}

	/**
	 * @see javax.sound.sampled.DataLine#flush()
	 */
	@Override
	public void flush() {
		if (outputDataLine.isRunning()) {
			outputDataLine.stop();// this stops any writing that is going on
		}
		outputDataLine.flush();
	}

	/**
	 * The {@link SourceDataLine} simply count the number of frames it has processed
	 * (unaware of where in the file the bytes come from) so its frame or microsecond
	 * position doesn't correspond to the actual time position (after some jumps to 
	 * different positions). So store the position just before starting the play back
	 * and compare to that starting position while the media is playing and the moment
	 * the stream is stopped (for updating the time position in microseconds).
	 * <p>
	 * Stores the position in the output, starts the output line and creates a thread
	 * that reads bytes from the audio file and writes them to the output line and posts 
	 * a START line event.
	 * 
	 * @see javax.sound.sampled.DataLine#start()
	 * @see #stop()
	 */
	@Override
	public void start() {
		if (!outputDataLine.isRunning()) {
			// after last stop the position changed, maybe by the flushing?
			// with a 48000 frame rate source, the position at this point is 
			// always 24000 higher than as recorded in stop()
			lastWriteStartFrame = outputDataLine.getLongFramePosition();
			//System.out.println("Start frame: " + lastWriteStopFrame + " act: " + 
			//		lastWriteStartFrame);
			outputDataLine.start();
			// the following might better be implemented using a single thread and a Lock
			// with one or two Conditions, or similar?
			writeRunner = new WriteRun(bufferSize);
			new Thread(writeRunner).start();
			postLineEvent(new LineEvent(this, LineEvent.Type.START, 
					microsecondsToFrames(microsecondPosition)));
		}
	}

	/**
	 * Stops (pauses) the playback, i.e. stops the output data line to process bytes.
	 * Updates the current media time based on the previous media time and the number of frames
	 * processed by the output since the last start.
	 * <p>
	 * Stops the {@link SourceDataLine}, stops the thread that reads from the audio file and 
	 * writes to the output, updates the media position and posts a STOP event.
	 * @see javax.sound.sampled.DataLine#stop()
	 * @see #start()
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
			postLineEvent(new LineEvent(this, LineEvent.Type.STOP, 
					microsecondsToFrames(microsecondPosition)));
		}
	}

	/**
	 * @return delegates to {@link SourceDataLine#isRunning()}
	 * @see javax.sound.sampled.DataLine#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return outputDataLine.isRunning();
	}

	/**
	 * @return delegates to {@link SourceDataLine#isActive()}
	 * @see javax.sound.sampled.DataLine#isActive()
	 */
	@Override
	public boolean isActive() {
		return outputDataLine.isActive();
	}

	/**
	 * @return the format of the audio file
	 * @see javax.sound.sampled.DataLine#getFormat()
	 */
	@Override
	public AudioFormat getFormat() {
		return format;
	}

	/**
	 * @return the size of the buffer used for reading and writing bytes, the
	 * equivalent of 2 seconds of audio
	 * @see javax.sound.sampled.DataLine#getBufferSize()
	 */
	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @return 0 at the moment (could return total bytes - current position in bytes?)
	 * @see javax.sound.sampled.DataLine#available()
	 */
	@Override
	public int available() {
		return 0;
	}

	/**
	 * @return the current media position in frames
	 * @see javax.sound.sampled.DataLine#getFramePosition()
	 */
	@Override
	public int getFramePosition() {
		return (int) microsecondsToFrames(getMicrosecondPosition());
	}

	/**
	 * @return the current media position in frames
	 * @see javax.sound.sampled.DataLine#getLongFramePosition()
	 */
	@Override
	public long getLongFramePosition() {
		return microsecondsToFrames(getMicrosecondPosition());
	}

	/**
	 * @return the current media position in microseconds
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
	 * @return 1, the base level of the volume of this player, does not consider
	 * settings in the Gain Control
	 * @see javax.sound.sampled.DataLine#getLevel()
	 */
	@Override
	public float getLevel() {
		return 1.0f;
	}

	/**
	 * @return a minimal Info object containing the audio format and this as Line class
	 * @see javax.sound.sampled.Line#getLineInfo()
	 */
	@Override
	public javax.sound.sampled.Line.Info getLineInfo() {
		if (playerInfo == null) {
			playerInfo = new DataLine.Info(getClass(), format);
		}
		return playerInfo;
	}

	/**
	 * Opens the output line and posts an OPEN line event
	 * @see javax.sound.sampled.Line#open()
	 */
	@Override
	public void open() throws LineUnavailableException {
		if (!outputDataLine.isOpen()) {
			outputDataLine.open();
		}
		postLineEvent(new LineEvent(this, LineEvent.Type.OPEN, 0));
	}

	/**
	 * Stops and closes the output line ({@link SourceDataLine}, closes
	 * the {@link RandomAccessFile} and posts a CLOSE line event
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
		try {
			raAudioFile.close();
		} catch (IOException ioe) {}
		
		postLineEvent(new LineEvent(this, LineEvent.Type.CLOSE, 0));
	}

	/**
	 * @return {@link SourceDataLine#isOpen()}
	 * @see javax.sound.sampled.Line#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return outputDataLine.isOpen();
	}

	/**
	 * @return {@link SourceDataLine#getControls()}
	 * @see javax.sound.sampled.Line#getControls()
	 */
	@Override
	public Control[] getControls() {
		return outputDataLine.getControls();
	}

	/**
	 * @return {@link SourceDataLine#isControlSupported(Type)}
	 * @see javax.sound.sampled.Line#isControlSupported(javax.sound.sampled.Control.Type)
	 */
	@Override
	public boolean isControlSupported(Type control) {
		return outputDataLine.isControlSupported(control);
	}

	/**
	 * @return {@link SourceDataLine#getControl(Type)}
	 * @see javax.sound.sampled.Line#getControl(javax.sound.sampled.Control.Type)
	 */
	@Override
	public Control getControl(Type control) {
		return outputDataLine.getControl(control);
	}

	/**
	 * Adds the listener to the list of listeners
	 * @see javax.sound.sampled.Line#addLineListener(javax.sound.sampled.LineListener)
	 */
	@Override
	public synchronized void addLineListener(LineListener listener) {
		//outputDataLine.addLineListener(listener);
		if (!lineListeners.contains(listener)) {
			lineListeners.add(listener);
		}
	}

	/**
	 * Removes the listener from the list
	 * @see javax.sound.sampled.Line#removeLineListener(javax.sound.sampled.LineListener)
	 */
	@Override
	public synchronized void removeLineListener(LineListener listener) {
		//outputDataLine.removeLineListener(listener);
		lineListeners.remove(listener);
	}
	
	/**
	 * Calls {@link LineListener#update(LineEvent)} of all registered listeners
	 * @param event the event to deliver to all line listeners
	 */
	private void postLineEvent(LineEvent event) {
		for (LineListener listener : lineListeners) {
			listener.update(event);
		}
	}

	/**
	 * Not implemented, not supported!
	 * @see javax.sound.sampled.Clip#open(javax.sound.sampled.AudioFormat, byte[], int, int)
	 * @throws LineUnavailableException 
	 */
	@Override
	public void open(AudioFormat format, byte[] data, int offset, int bufferSize)
			throws LineUnavailableException {
		throw new LineUnavailableException("Method not supported");
	}

	/**
	 * Not implemented, not supported! Use one of the constructors instead.
	 * @see javax.sound.sampled.Clip#open(javax.sound.sampled.AudioInputStream)
	 * @throws LineUnavailableException
	 */
	@Override
	public void open(AudioInputStream stream) throws LineUnavailableException,
			IOException {
		throw new LineUnavailableException("Method not supported, use one of the constructors to open this player");
	}

	/**
	 * @return the number of frames in this media file as an <code>int</code>
	 * @see javax.sound.sampled.Clip#getFrameLength()
	 */
	@Override
	public int getFrameLength() {
		return frameLengthInt;
	}

	/**
	 * 
	 * @return the number of frames in this media file as a <code>long</code>
	 */
	public long getFrameLengthLong() {
		return frameLength;
	}
	
	/**
	 * @return the duration of this media file  in microseconds
	 * @see javax.sound.sampled.Clip#getMicrosecondLength()
	 */
	@Override
	public long getMicrosecondLength() {
		return microsecondLength;
	}

	/**
	 * Requests the player to jump to the specified frame or sample position.
	 * Calculates the corresponding position in microseconds and seeks the 
	 * corresponding byte position in the {@link RandomAccessFile}.
	 * 
	 * @param frames the frame position to jump to
	 * @see javax.sound.sampled.Clip#setFramePosition(int)
	 */
	@Override
	public void setFramePosition(int frames) {
		setPosition(framesToBytes(frames));
		microsecondPosition = framesToMicroseconds(frames);
	}

	/**
	 * Requests the player to jump to the specified microseconds position.
	 * Seeks the corresponding byte position in the {@link RandomAccessFile}.
	 * 
	 * @param microseconds the time value to jump to
	 * @see javax.sound.sampled.Clip#setMicrosecondPosition(long)
	 */
	@Override
	public void setMicrosecondPosition(long microseconds) {
		this.microsecondPosition = microseconds;
		setPosition(microsecondsToBytes(microseconds));
	}
	
	/**
	 * Stops the media play back, if necessary, flushes the buffer of the output 
	 * and seeks the corresponding position in the audio file (taking into account 
	 * the size of the header of the audio file)
	 * 
	 * @param bytePosition the byte position in the actual audio data, must 
	 * correspond to the exact start position of a frame or sample
	 */
	private void setPosition(long bytePosition) {
		// stop the output
		if (outputDataLine.isRunning()) {
			outputDataLine.stop();			
		}
		outputDataLine.flush();//superfluous if stop has been called?
		// seek to the correct position in the random access file
		if (bytePosition % frameSize != 0) {
			bytePosition -= (bytePosition % frameSize);
		}
		try {
			raAudioFile.seek(headerSizeBytes + bytePosition);
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot seek position: " + ioe.getMessage());
			}
		}
		
	}

	/**
	 * Stub, not supported.
	 * @see javax.sound.sampled.Clip#setLoopPoints(int, int)
	 */
	@Override
	public void setLoopPoints(int start, int end) {
		// stub, not supported
	}

	/**
	 * Stub, not supported
	 * @see javax.sound.sampled.Clip#loop(int)
	 */
	@Override
	public void loop(int count) {
		// stub not supported
	}

	/**
	 * Checks for the end-of-media situation, stops the output line and posts
	 * an end-of-media stop event
	 */
	private void endOfMedia() {
		// the output line continues to run at the end of its buffer (will loop),
		//stop it when the output is at or close to the last frame 
		long baseFrame = microsecondsToFrames(microsecondPosition);
		while (outputDataLine.isRunning()) {
			if (baseFrame + (outputDataLine.getLongFramePosition() - lastWriteStartFrame) >= 
					frameLength - 1) {
				stop();
				break;
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException ie){}
		}
		// post an end-of-media stop event, passing the total frame length as the position
		// otherwise frame positions are zero based, so the last frame is frameLength - 1
		postLineEvent(new LineEvent(this, LineEvent.Type.STOP, frameLength));
	}
	
	/**
	 * A Runnable that reads bytes form the input file and writes them to the output
	 * data line. 
	 * <p>
	 * At the moment for every start event a new instance is created (including a new 
	 * byte buffer), this is probably not the most efficient solution.
	 * 
	 * @author Han Sloetjes
	 *
	 */
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
					int readBytes = raAudioFile.read(buffer, 0, buffer.length);
					
					if (readBytes < frameSize) {
						// generate stop event end of media
						endOfMedia();
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
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.warning("Unable to read or write audio data: " + ioe.getMessage());
					}
					break;
				}
			}			
		}				
	} // end class WriteRun
}
