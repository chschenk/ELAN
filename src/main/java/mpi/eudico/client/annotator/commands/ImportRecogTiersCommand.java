package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.recognizer.io.CsvTierIO;
import mpi.eudico.client.annotator.recognizer.io.XmlTierIO;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A Command to load tiers from a recognizer tier file. 
 * @author Han Sloetjes
 *
 */
public class ImportRecogTiersCommand implements Command {
	private String name;
	private Transcription trans;
	
	/**
	 * Constructor.
	 * @param name name of the command
	 */
	public ImportRecogTiersCommand(String name) {
		this.name = name;
	}

	/**
	 * @param receiver the transcription
	 * @param arguments null
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		trans = (Transcription) receiver;

		String filePath = promptForTierFile();
		if (filePath == null) {
			return;
		}
		
		File f = new File(filePath);

        if (f.exists() && f.canRead()) {
        	List<Segmentation> segm = null;
        	
        	if (filePath.endsWith("csv")) {
				CsvTierIO cio = new CsvTierIO();
				segm = cio.read(f);
        	} else {
				XmlTierIO xio = new XmlTierIO(f);
				try {
					segm = xio.parse();
				} catch (Exception exe){
					JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(trans), 
	            			exe.getMessage(), ElanLocale.getString("Message.Error"), 
	            			JOptionPane.ERROR_MESSAGE);
				}
        	}
        	
        	if (segm != null && segm.size() > 0) {
                Command cc = ELANCommandFactory.createCommand(trans,
                        ELANCommandFactory.SEGMENTS_2_TIER_DLG);
                cc.execute(trans,
                    new Object[] { segm });
        	} else {
        		// warning no tiers 
            	JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(trans), 
            			ElanLocale.getString("Recognizer.RecognizerPanel.Warning.NoTiers"), 
            			ElanLocale.getString("Message.Error"), JOptionPane.WARNING_MESSAGE);
        	}
        } else {
        	// warning file read failed
        	JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(trans), 
        			ElanLocale.getString("Recognizer.RecognizerPanel.Warning.LoadFailed"), 
        			ElanLocale.getString("Message.Error"), JOptionPane.WARNING_MESSAGE);
        }
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Prompts the user to specify a location from where to load tiers.
	 * The tiers can be in a csv file or in an xml file
	 * 
	 * @return the path or null if canceled
	 */
	private String promptForTierFile() {
		ArrayList<String[]> extensions = new ArrayList<String[]>();
		extensions.add(FileExtension.CSV_EXT);
		
		FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(trans));
		chooser.createAndShowFileDialog(null, FileChooser.OPEN_DIALOG, extensions, FileExtension.XML_EXT, "Recognizer.Path", null);

		File f = chooser.getSelectedFile();
		
		if (f != null) {	
			return f.getAbsolutePath();
		} 
		
		return null;
	}
}
