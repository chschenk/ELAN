package mpi.eudico.client.annotator.commands.global;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ClipMediaCommand;
import mpi.eudico.client.annotator.commands.ClipMediaMultiCommand;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.util.ClipWithScriptUtil;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextReader;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.SimpleReport;
import mpi.eudico.util.TimeFormatter;

/**
 * A menu action to initiate clipping of media fragments based on a tab-delimited
 * text file and using the clip-with-script facility.
 * 
 * @author Han Sloetjes
 * @version May 2013
 */
public class ClipMediaMultiMA extends FrameMenuAction {
	private final String scriptFileName = "clip-media.txt";
	
	/**
	 * 
	 * @param name name of the action
	 * @param frame the parent frame
	 */
	public ClipMediaMultiMA(String name, ElanFrame2 frame) {
		super(name, frame);
		
	}

	/**
	 * Prompts for a tab-delimited text file or csv file and starts the parsing and clipping process.
	 * The action returns immediately in the case of an erroneous situation occurring. 
	 * 
	 * @param the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		ClipWithScriptUtil scriptUtil = new ClipWithScriptUtil();
		SimpleReport report = new SimpleReport();
		report.append("Starting multiple file clipping...");
		// check if executable and param line can be extracted
		File scriptFile = scriptUtil.getScriptFile(scriptFileName);
		if (scriptFile == null) {
			showWarningMessage("The script file (clip-media.txt) containing the configuration for clipping is not found");
			return;
		}
		report.append("Script file: " + scriptFile.getAbsolutePath());
		String[] scriptLine = scriptUtil.parseScriptLine(scriptFile);
		
		if (scriptLine == null || scriptLine.length < 2) {
			showWarningMessage(ElanLocale.getString("ClipMedia.Error.Message") + " " + 
					ElanLocale.getString("ClipMedia.Error.Message.NoParameters"));
			return;
		}
		// the executable part of the script
		String executable = scriptLine[0];
		// the parameter part of the script
		String paramLine = scriptLine[1];
		report.append("Executable: " + executable);
		report.append("Parameter part: " + paramLine);
		
		FileChooser chooser = new FileChooser(frame);
		chooser.createAndShowFileDialog(ElanLocale.getString(
        	"Frame.ElanFrame.OpenDialog.Title"), FileChooser.OPEN_DIALOG,
        	FileExtension.CSV_EXT, "LastUsedCSVDir");// or use txt extension?
		
		File tabDelFile = chooser.getSelectedFile();
		if (tabDelFile != null) {
			report.append("Input file: " + tabDelFile.getAbsolutePath());
			try {
				DelimitedTextReader reader = new DelimitedTextReader(tabDelFile);
				reader.detectDelimiter();
				int numCol = reader.detectNumColumns();
				if (numCol < 3) {
					showWarningMessage("Too few columns in the tab-delimited text/csv file to perform clipping.");
					return;
				}
				int[] columns = new int[3];
				columns[0] = 0;
				columns[1] = 1;
				columns[2] = numCol - 1;
				
				List<String[]> rowDataList = reader.getRowDataForColumns(reader.getFirstRowIndex(), columns);
				
				if (rowDataList == null || rowDataList.size() == 0) {
					showWarningMessage("No row data could be extracted from the file.");
					return;
				}
				
				// prompt for output folder
				File outputFolder = null;
				FileChooser foldChooser = new FileChooser(frame);
				foldChooser.createAndShowFileDialog(ElanLocale.getString("ActivityMonitoringDialog.OpenDialog.Title"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.OK"), 
		        		null, null, false, "LastUsedExportDir", FileChooser.DIRECTORIES_ONLY, null);

		        outputFolder = foldChooser.getSelectedFile();
		        if (outputFolder == null) {
		        	// report
		        	showWarningMessage("No output folder provided.");
		        	return;
		        } else if (!outputFolder.exists()) {
		        	// report
		        	showWarningMessage("The provided output folder does not exist.");
		        	return;
		        }
				report.append("Output folder is: " + outputFolder.getAbsolutePath());
				
				String[] rowData;
				long bt, et;
				//String curEafPath;
				int numSegments = 0;
				
				// store the segments per file before continuing
				Map<String, List<long[]>> groupedSegments = new HashMap <String, List<long[]>>();
				// read each line, extract bt, et, filepath
				for (int i = 0; i < rowDataList.size(); i++) {
					rowData = rowDataList.get(i);
					
					if (rowData == null || rowData.length < 3) {
						// message or log or report error, then continue.
						report.append("Encountered a line holding no or invalid information for clipping: " + (i + 1));
						continue;
					}
					bt = extractTime(rowData[0]);
					if (bt < 0) {
						report.append("Invalid begin time on line: " + (i + 1));
						continue;
					}
					et = extractTime(rowData[1]);
					if (et <= bt) {
						report.append("Invalid end time on line: " + (i + 1));
						continue;
					}
					
					if (groupedSegments.containsKey(rowData[2])) {
						groupedSegments.get(rowData[2]).add(new long[]{bt, et});						
					} else {
						List<long[]> segList = new ArrayList<long[]>();
						groupedSegments.put(rowData[2], segList);
						segList.add(new long[]{bt, et});
						report.append("Encountered file " + rowData[2] + ". Linked media files will be clipped.");
					}
					numSegments++;
				}
				
				if (groupedSegments.size() == 0) {
					// warn
					showWarningMessage("No information for clipping could be extracted from the text file.");
					return;
				}
				
				ClippingProgressThread progressMonitor = new ClippingProgressThread(executable, paramLine, 
						outputFolder.getAbsolutePath(), groupedSegments, report);
				progressMonitor.start();
				
			} catch (FileNotFoundException fnfe) {
				showWarningMessage("The selected file is not found.");
			} catch (IOException ioe) { 
				showWarningMessage("Error while reading the file.");
			}
		}
	}
	
	/**
	 * Converts a string to a long value in milliseconds.
	 * 
	 * @param time the time string
	 * @return the value in milliseconds or -1 if it couldn't be converted
	 */
	private long extractTime(String time) {
		if (time != null) {
			return TimeFormatter.toMilliSeconds(time);
		}
		
		return -1;
	}

	/**
	 * Tries to create a Transcription from a file path.
	 * 
	 * @param path the path to a file, probably an eaf file
	 * 
	 * @return a transcription or null
	 */
	private Transcription loadEafFile(String path) {
		if (path != null) {
			path = FileUtility.urlToAbsPath(path);
			// currently the constructor of TranscriptionImpl does not throw an exception
			// therefore check here if the file exists
			File transFile = new File(path);
			if (!transFile.exists()) {
				return null;
			}
			return new TranscriptionImpl(new File(path).getAbsolutePath());
		}
		return null;
	}
	
	/**
	 * Show a (gui) warning message.
	 * 
	 * @param message
	 */
	private void showWarningMessage(String message) {
		JOptionPane.showMessageDialog(frame, message, 
				ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * A thread for monitored progress of the clipping.
	 * 
	 * @author Han Sloetjes
	 */
	class ClippingProgressThread extends Thread {
		private ProgressMonitor monitor;
		private Map<String, List<long[]>> groupedSegments;
		private ProcessReport report;
		private String executable;
		private String paramLine;
		private String outputPath;
		private final int MULT = 10; 
		
		/**
		 * Constructor
		 */
		ClippingProgressThread(String executable, String paramLine, String outputPath,
				Map<String, List<long[]>> groupedSegments, ProcessReport report) {
			this.executable = executable;
			this.paramLine = paramLine;
			this.outputPath = outputPath;
			this.groupedSegments = groupedSegments;
			this.report = report;
		}
		
		/**
		 * Processes the clipping by calling the ClipMediaMultiCommand for the segments
		 * of every eaf file in the map.
		 */
		@Override
		public void run() {
			Transcription trans;
			Iterator<String> groupIter = groupedSegments.keySet().iterator();
			int max = groupedSegments.size() * MULT + 2;
			int prog = 0;
			int fileCount = 0;
			String curEafPath;
			
			JLabel message = new JLabel("Clipping progress");
			message.setMinimumSize(new Dimension(400, message.getMinimumSize().height));
			message.setPreferredSize(new Dimension(400, message.getMinimumSize().height));
			monitor = new ProgressMonitor(frame, message, "Preparing clipping...", 0, max);
			monitor.setMillisToDecideToPopup(10);
			monitor.setMillisToPopup(50);
			// allow the monitor to pop up
			for (int i = 0; i < 2; i++) {
				prog = i;
				monitor.setProgress(i);
				try {
					Thread.sleep(100);
				} catch(InterruptedException ie){}
			}
			
			while (groupIter.hasNext()) {
				if (monitor.isCanceled()) {
					report.append("The process has been canceled after: " + fileCount + " transcription files.");
					monitor.close();
					break;
				}
				curEafPath = groupIter.next();
				prog += MULT;
				monitor.setNote("Processing clips based on eaf file: " + FileUtility.fileNameFromPath(curEafPath));

				// parse eaf
				trans = loadEafFile(curEafPath);
				fileCount++;
				
				if (trans == null) {
					report.append("Unable to load the .eaf file: " + curEafPath + ". The linked media files cannot be retrieved and are skipped.");
					monitor.setProgress(prog);
					continue;
				}
				// clip all segments of the media of this transcription (take preferences into account)
				// create command and execute
				ClipMediaCommand command = new ClipMediaMultiCommand("ClipMulti");// localize?
				List<long[]> curSegs = groupedSegments.get(curEafPath);

				command.execute(trans, new Object[]{executable, paramLine, curSegs, outputPath, report});
				// in principle execute should return after all clipping has finished, but this depends on the 
				// application called to do the clipping, otherwise the system can run out of memory
				report.append("Processing of " + curEafPath + " done. (But the actual clipping might still continue.)");				
				monitor.setProgress(prog);
			}
			monitor.setProgress(monitor.getMaximum());// ensure the monitor disappears
			
			// show report
			ReportDialog repDialog = new ReportDialog(frame, report);
			repDialog.setVisible(true);
		}
	}
}
