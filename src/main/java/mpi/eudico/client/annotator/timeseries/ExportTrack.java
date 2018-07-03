package mpi.eudico.client.annotator.timeseries;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.timeseries.csv.CSVWriter;
import mpi.eudico.client.annotator.timeseries.xml.XMLWriter;
import mpi.eudico.client.annotator.util.FileExtension;

/**
 * A class for starting the export track process.
 * 
 * @author Han Sloetjes
 */
public class ExportTrack {

	/**
	 * Constructor.
	 */
	public ExportTrack() {
		super();
	}
	
	/**
	 * Shows a save as dialog and starts the export
	 * 
	 * @param track timeseries track
	 */
	public void exportTrack(AbstractTSTrack track) {
		exportTrack(null, track);
	}
	
	/**
	 * Shows a save as dialog and starts the export
	 * 
	 * @param track timeseries track
	 */
	public void exportTrack(Component parent, AbstractTSTrack track) {
		if (track == null) {
			return;
		}
		
		String savePath = promptForTierFile(parent, track.getSource());
		if (savePath != null) {
			File tf = new File(savePath);
			try { 
				if (tf.exists()) {
	                int answer = JOptionPane.showConfirmDialog(parent,
	                        ElanLocale.getString("Message.Overwrite"),
	                        ElanLocale.getString("SaveDialog.Message.Title"),
	                        JOptionPane.YES_NO_OPTION,
	                        JOptionPane.WARNING_MESSAGE);

	                if (answer == JOptionPane.NO_OPTION) {
	                    return;
	                }
				}
			} catch (Exception ex) {// any exception
				return;
			}
			
			String lower = savePath.toLowerCase();
			if (lower.endsWith("csv")) {
				try {
					new CSVWriter().writeTrackToCSV(tf, track);
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(parent, ElanLocale.getString(
						"Recognizer.RecognizerPanel.Warning.SaveFailed")  + ioe.getMessage(), 
						ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
				}
			} else {
				try {
					new XMLWriter().writeTrackToXML(tf, track);
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(parent, ElanLocale.getString(
						"Recognizer.RecognizerPanel.Warning.SaveFailed")  + ioe.getMessage(), 
						ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	/**
	 * Prompts the user to specify a location where to store the timeseries.
	 * 
	 * @param a parent component for the save dialog, can be null
	 * @param track the track
	 * @return the path or null if canceled
	 */
	private String promptForTierFile(Component parent, String source) {
		String prefPath = null;
		if (source != null) {
			source = source.replaceAll("\\", "/");
			int index = source.lastIndexOf('/');
			if (index > -1 ) {
				prefPath = source.substring(index);
			}
		}

		ArrayList<String[]> extensions = new ArrayList<String[]>();
		extensions.add(FileExtension.CSV_EXT);
		
		FileChooser chooser = new FileChooser(parent);
		if (prefPath != null) {
			chooser.setCurrentDirectory(prefPath);
		}
		chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, extensions, FileExtension.XML_EXT, "LinkedFileDir", null);
		File f = chooser.getSelectedFile();
		if (f != null) {
			return f.getAbsolutePath();
		} else {
			return null;
		}		
	}
}
