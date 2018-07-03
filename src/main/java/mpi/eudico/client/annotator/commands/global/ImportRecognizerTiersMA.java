package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.recognizer.io.CsvTierIO;
import mpi.eudico.client.annotator.recognizer.io.XmlTierIO;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * A menu action to create a new document from a a csv or xml TIER file (AVATecH).
 * @author Han Sloetjes
 */
public class ImportRecognizerTiersMA extends FrameMenuAction {

	/**
	 * Constructor
	 * @param name name of the action
	 * @param frame the parent frame
	 */
	public ImportRecognizerTiersMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {		
        
        String filePath = promptForTierFile();
        if (filePath == null) {
        	return;
        }
        
        File f = new File(filePath);
        if (f.exists() && f.canRead()) {
        	TranscriptionImpl transcription = new TranscriptionImpl();
            LinguisticType type = null;
            // create a default LinguisticType for the tiers
            if (transcription.getLinguisticTypes().size() == 0) {
                // time-alignable, no constraint type
                type = new LinguisticType("recognizer");
                transcription.addLinguisticType(type);
                transcription.setChanged();
            }
        	
        	List<Segmentation> segm = null;
        	
        	if (filePath.endsWith("csv")) {
				CsvTierIO cio = new CsvTierIO();
				segm = cio.read(f);
        	} else {
				XmlTierIO xio = new XmlTierIO(f);
				try {
					segm = xio.parse();
				} catch (Exception exe){
					JOptionPane.showMessageDialog(frame, 
	            			exe.getMessage(), ElanLocale.getString("Message.Error"), 
	            			JOptionPane.ERROR_MESSAGE);
				}
        	}
        	
        	if (segm != null && segm.size() > 0) {
        		int numTiersCreated = 0;
        		transcription.setNotifying(false);
        		AlignableAnnotation aa;
        		
        		for (Segmentation s : segm) {
        			if (s == null) {
        				continue;
        			}
        			TierImpl t = new TierImpl(s.getName(), null, transcription, type);
        			transcription.addTier(t);
        			numTiersCreated++;
        			
        			ArrayList<RSelection> segments = s.getSegments();
        			for (RSelection sel : segments) {
        				if (sel != null) {
        					aa = (AlignableAnnotation) t.createAnnotation(sel.beginTime, sel.endTime);
        					if (aa != null && sel instanceof Segment && ((Segment) sel).label != null) {
        						aa.setValue(((Segment) sel).label);
        						// TODO: set CvEntryId?
        					}
        				}
        			}			
        		}
        		
        		transcription.setNotifying(true);
        		
        		if (numTiersCreated > 0) {
        			// create new window
        	        // open window
        	        FrameManager.getInstance().createFrame(transcription);
        		} else {
        			// warn no tiers
                	JOptionPane.showMessageDialog(frame, 
                			ElanLocale.getString("Recognizer.RecognizerPanel.Warning.NoTiers"), 
                			ElanLocale.getString("Message.Error"), JOptionPane.WARNING_MESSAGE);
        		}
        	} else {
        		// warning no tiers 
            	JOptionPane.showMessageDialog(frame, 
            			ElanLocale.getString("Recognizer.RecognizerPanel.Warning.NoTiers"), 
            			ElanLocale.getString("Message.Error"), JOptionPane.WARNING_MESSAGE);
        	}
        } else {
        	// warning file read failed
        	JOptionPane.showMessageDialog(frame, 
        			ElanLocale.getString("Recognizer.RecognizerPanel.Warning.LoadFailed"), 
        			ElanLocale.getString("Message.Error"), JOptionPane.WARNING_MESSAGE);
        }
        
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
		
		FileChooser chooser = new FileChooser(frame);
		chooser.createAndShowFileDialog(null, FileChooser.OPEN_DIALOG, FileExtension.XML_EXT, "Recognizer.Path");
		File f = chooser.getSelectedFile();
	
		if (f !=null) {				
			//AS : the new Filechooser forces the selected file should be one of the 
			// supported formats specifed so the following code is been commented out
			
//			if (chooser.getFileFilter() == ff) {
//				String lower = f.getAbsolutePath().toLowerCase();
//				boolean valid = false;
//				for (int i = 0; i < FileExtension.CSV_EXT.length; i++) {
//					if (lower.endsWith(FileExtension.CSV_EXT[i])) {
//						valid = true;
//						break;
//					}
//				}
//				if (!valid) {
//					f = new File(f.getAbsolutePath() + "." + FileExtension.CSV_EXT[0]);
//				}
//			} else {
//				if (!f.getAbsolutePath().toLowerCase().endsWith("xml")) {
//					f = new File(f.getAbsolutePath() + ".xml");
//				}
//			}
			return f.getAbsolutePath();
		} 
		
		return null;			
	}
}
