package nl.mpi.jsound;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A class that uses javax.sound classes for writing segments or clips 
 * of a wave file to a new wave file with the same encoding settings etc.
 * 
 * @author Han Sloetjes
 */
public class WaveClipper {
	private final Logger LOG = Logger.getLogger("JavaSoundMPI");
	private String filePath;
	private AudioInputStream audioIn;
	
	/**
	 * A no-arg constructor to be used in combination with 
	 * {@link #exportClip(String, String, long, long)}, in the case
	 * where only one segment of an input file is going to be saved,
	 * or one or more segments from different input files.
	 */
	public WaveClipper() {
		super();
	}
	
	/**
	 * Constructor with an input wave file as parameter.
	 * Can be used in combination with {@link #exportClip(String, long, long)} in 
	 * the case where multiple segments of the same input file have to be saved.
	 * 
	 * @param filePath the path to the source wave file
	 * 
	 * @throws IOException when a file read or write error occurs
	 * @throws UnsupportedAudioFileException when the format is not supported

	 */
	public WaveClipper(String filePath) throws IOException, 
		UnsupportedAudioFileException {
		super();
		this.filePath = filePath;
		
		initReader();
	}

	/**
	 * Initializes an audio input stream for the wave file.
	 */
	private void initReader() throws IOException, 
		UnsupportedAudioFileException {
		try {
			File f = new File (filePath);
			InputStream is = new BufferedInputStream(new FileInputStream(f));
			audioIn = AudioSystem.getAudioInputStream(is);
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot create audio reader stream: " + ioe.getMessage());
			}
			throw ioe;
		} catch (UnsupportedAudioFileException uafe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Unsupported audio file: " + uafe.getMessage());
			}
			throw uafe;
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot create audio reader: " + t.getMessage());
			}
		}
	}
	
	/**
	 * Exports a clip or segment to a new file. 
	 * This method can be used if an input stream is already there, i.e. if the 
	 * constructor with the source file as parameter has been called.
	 * 
	 * @param targetPath the file name and path to write to
	 * @param t1 begin time in microseconds
	 * @param t2 end time in microseconds
	 * @return true if the file was written successful
	 * 
	 * @throws IOException when a file read or write error occurs
	 * @throws UnsupportedAudioFileException when the format is not supported
	 */
	public synchronized boolean exportClip(String targetPath, long t1, long t2) 
			throws IOException, UnsupportedAudioFileException {
		if (audioIn == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot export a clip, there is no audio input stream");
			}
			return false;
		}
		OutputStream out = null;
		try {
			File fileOut = new File (targetPath);
			out = new BufferedOutputStream(new FileOutputStream(fileOut));
			
			SegmentAudioInputStream saiStream = new SegmentAudioInputStream(audioIn, 
					t1, t2);
			
			int numWr = AudioSystem.write(saiStream, AudioFileFormat.Type.WAVE, out);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Wrote %d bytes to file %s", numWr, targetPath));
			}
			return numWr > 0;
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("An error ocurred while writing the file: " + ioe.getMessage());
			}
			throw ioe;
		}  catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot create an audio file reader: " + t.getMessage());
			}
			throw new IOException("Cannot write the file because of an error: "  + t.getMessage());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Throwable thr){}
			}
			// closing saiStream would close audioIn as well
		}
	}
	
	/**
	 * Exports a clip or segment to a new file. 
	 * This method can be used for a single, one time clip action. A new AudioInputStream
	 * is created on every invocation, even if the sourceFile parameter is the same
	 * during successive calls.
	 * 
	 * @param sourceFile the source wave file from which to export a fragment
	 * @param targetPath the file name and path to write to
	 * @param t1 begin time in microseconds
	 * @param t2 end time in microseconds
	 * @return true if the file was written successful
	 * 
	 * @throws IOException when a file read or write error occurs
	 * @throws UnsupportedAudioFileException when the format is not supported
	 */
	public boolean exportClip(String sourceFile, String targetPath, long t1, long t2) 
			throws IOException, UnsupportedAudioFileException {
		AudioInputStream audioSourceStream = null;
		OutputStream out = null;
		AudioInputStream saiStream = null;
		
		try {
			File f = new File (sourceFile);
			InputStream is = new BufferedInputStream(new FileInputStream(f));
			audioSourceStream = AudioSystem.getAudioInputStream(is);
			
			File fileOut = new File (targetPath);
			out = new BufferedOutputStream(new FileOutputStream(fileOut));
			
			saiStream = new SegmentAudioInputStream(audioSourceStream, 
					t1, t2);
			int numWr = AudioSystem.write(saiStream, AudioFileFormat.Type.WAVE, out);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Wrote %d bytes to file %s", numWr, targetPath));
			}
			return numWr > 0;
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot create an audio reader stream: " + ioe.getMessage());
			}
			throw ioe;
		} catch (UnsupportedAudioFileException uafe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Unsupported audio file: " + uafe.getMessage());
			}
			throw uafe;
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot create an audio file reader: " + t.getMessage());
			}
			throw new IOException("Cannot write the file because of an error: "  + t.getMessage());
		} finally {
			try {
				if (saiStream != null) {
					saiStream.close();
				}
				if (audioSourceStream != null) {
					audioSourceStream.close();
				}
				if (out != null) {
					out.close();
				}
			} catch(Throwable thr) {
				thr.printStackTrace();
			}
		}

	}

	/**
	 * Close the audio input stream, if not null.
	 */
	@Override
	protected void finalize() throws Throwable {
		if (audioIn != null) {
			try {
				audioIn.close();
			} catch (Throwable t) {}
		}
	}
	
	
}
