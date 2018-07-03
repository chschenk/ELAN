package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileExportPraatStep1;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileExportPraatStep2;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileExportPraatStep3;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;

/**
 * Creates a dialog to select tiers from multiple files for praat
 * export.
 *
 * @author Aarthy Somasundaram
 * @version 1.0
 */
public class ExportPraatMultiMA extends AbstractProcessMultiMA {
    /**
     * Creates a new ExportPraatMultiMA instance
     *
     * @param name name of the action
     * @param frame the containing frame
     */
    public ExportPraatMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a multistep dialog for praat export.
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
      
    	MultiStepPane multipane = new MultiStepPane();
    	multipane.addStep(new MultipleFileExportPraatStep1(multipane, null));
    	multipane.addStep(new MultipleFileExportPraatStep2(multipane));
    	multipane.addStep(new MultipleFileExportPraatStep3(multipane));
    	

    	JDialog dialog = multipane.createDialog(frame, ElanLocale.getString("ExportPraatDialog.Title"), true);
	    dialog.setPreferredSize(new Dimension(600, 600));
	    dialog.pack();
	    dialog.setVisible(true);	
    }
}

