package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.multiplefilesedit.scrub.ScrubberConfigDialog;
import mpi.eudico.client.annotator.util.ClientLogger;


/**
 * A class that creates a dialog for configuring the clean up process.
 *
 * @author Han Sloetjes
 * @version 2.0, March 2010
 */
@SuppressWarnings("serial")
public class MultiEAFScrubberMA extends AbstractProcessMultiMA
    implements ClientLogger {
    /**
     * Creates a new MultiEAFScrubberMA instance
     *
     * @param name the name
     * @param frame the frame
     */
    public MultiEAFScrubberMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a dialog to configure which characters to remove from annotations.
     * Leading or trailing spaces or all sequences of multiple spaces, 
     * leading, trailing or all new line characters and
     * leading, trailing or all tabs.
     * 
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	// warning
        int option = JOptionPane.showConfirmDialog(frame,
                ElanLocale.getString("MFE.Scrubber.InitWarn"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
	        List<File> files = getMultipleFiles(frame,
	                ElanLocale.getString("ExportDialog.AnnotationList.Title"));
	
	        if ((files == null) || (files.size() == 0)) {
	            LOG.info("No (valid) files supplied.");
	
	            return;
	        }
	        
	        ScrubberConfigDialog scf = new ScrubberConfigDialog(frame, true, files);
	        scf.setVisible(true);
        }
    }
}
