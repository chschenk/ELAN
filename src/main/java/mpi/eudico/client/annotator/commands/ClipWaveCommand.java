package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

import nl.mpi.jsound.WaveClipper;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * A command to create an audio clip from a segment of a wave file
 * using javax.sound.sampled classes.
 * 
 * @author Han Sloetjes
 */
public class ClipWaveCommand implements Command {
	private String name;
	private Transcription transcription;
	
	/**
	 * Constructor.
	 * @param name the name of the command
	 */
	public ClipWaveCommand(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @param receiver the transcription
	 * @param arguments the arguments:
	 * <ul>
	 * <li>args[0]: the wave source path (String or MediaDescriptor)</li>
	 * <li>args[1]: begin time of the segment to clip (Long)</li>
	 * <li>args[2]: end time of that segment (Long)</li>
	 * </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		transcription = (Transcription) receiver;
		
		Object sourceObj = arguments[0];
		Long beginTime = (Long) arguments[1];
		Long endTime = (Long) arguments[2];
		
		String sourcePath = null;
		if (sourceObj instanceof String) {
			sourcePath = (String) sourceObj;
		} else if (sourceObj instanceof MediaDescriptor) {
			sourcePath = ((MediaDescriptor) sourceObj).mediaURL;
		}
		
		if (sourcePath == null) {
			// log, show message
			return;
		}
		if (sourcePath.startsWith("file:")) {
			sourcePath = sourcePath.substring(5);
		}
		// prompt for file name or construct a file name
		String outputPath = getOutputFile(sourcePath, beginTime, endTime);
		
		if (outputPath == null) {
			return;
		}
		
		clipFile(sourcePath, outputPath, beginTime, endTime);
	}
	
	/**
	 * Creates a file path for the clip, either by prompting the user
	 * or by constructing it from the source name and the begin and end time.
	 * 
	 * @param sourcePath the path of the source file
	 * @param begin begin time of the interval
	 * @param end end time of the interval
	 * @return the path for the new wave file
	 */
	private String getOutputFile(String sourcePath, long begin, long end) {
		boolean promptForFile = true;
		Boolean promptObj = Preferences.getBool("Media.PromptForFilename", 
				transcription);
		if (promptObj != null) {
			promptForFile = promptObj;
		}
		
		if (promptForFile) {
            FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(transcription));
            chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, null, "MediaClipDir");

            File selFile = chooser.getSelectedFile();
            if (selFile != null) {
            	return selFile.toString();
            } else {
            	return null;
            }
		} else {// construct a name
			int si = sourcePath.lastIndexOf(".");
			if (si > -1) {
				return sourcePath.substring(0, si) + "_" + begin + "_" + end + 
						sourcePath.substring(si);
			} else {
				return sourcePath + "_" + begin + "_" + end;
			}	

		}
	}
	
	/**
	 * Creates a WaveClipper and exports the audio clip
	 * @param inputFile the source file
	 * @param outputFile the target file
	 * @param beginTime the begin of the interval
	 * @param endTime the end of the interval
	 */
	private void clipFile(String inputFile, String outputFile, long beginTime, long endTime) {
		WaveClipper clipper = new WaveClipper();
		
		try {
			boolean clipSuccess = clipper.exportClip(inputFile, outputFile, 
					beginTime * 1000, endTime * 1000);
			
			if (clipSuccess) {
				// success message, unless in unattended mode
				showMessage(String.format(ElanLocale.getString("ClipMedia.Message.Saved"), outputFile),
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				// warning
				showMessage(ElanLocale.getString("ClipMedia.Error.Message.Unknown"), 
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException ioe) {
			showMessage(ElanLocale.getString("ClipMedia.Error.Message.IOError"), 
					JOptionPane.ERROR_MESSAGE);
		} catch (UnsupportedAudioFileException uafe) {
			showMessage(ElanLocale.getString("ClipMedia.Error.Message.NotSupported"), JOptionPane.ERROR_MESSAGE);
		} catch (Throwable t) {
			showMessage("An unknown error occured while creating the audio clip", 
					JOptionPane.ERROR_MESSAGE);
		}
	}

	
	private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription),
        		message, "", type);
	}
	
	@Override
	public String getName() {
		return name;
	}

}
