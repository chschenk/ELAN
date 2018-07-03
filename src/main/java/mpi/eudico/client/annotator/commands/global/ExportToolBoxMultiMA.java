package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileToolBoxExportStep1;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileToolBoxExportStep2;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileToolBoxExportStep3;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileToolBoxExportStep4;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;


/**
 * Creates a dialog to select tiers from multiple files for tool box
 * export.
 *
 * @author Aarthy Somsasundaram
 * @version 1.0
 */
public class ExportToolBoxMultiMA extends AbstractProcessMultiMA {
    /**
     * Creates a new ExportToolBoxMultiMA instance
     *
     * @param name name of the action
     * @param frame the containing frame
     */
    public ExportToolBoxMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a multistep dialog for toolbox export
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
      
    	final MultiStepPane multipane = new MultiStepPane();
    	multipane.addStep(new MultipleFileToolBoxExportStep1(multipane));
    	multipane.addStep(new MultipleFileToolBoxExportStep2(multipane));
    	multipane.addStep(new MultipleFileToolBoxExportStep3(multipane));
    	multipane.addStep(new MultipleFileToolBoxExportStep4(multipane));
    	

        EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
            	JDialog dialog = multipane.createDialog(frame, ElanLocale.getString("ExportShoebox.Title.Toolbox"), true);
        	    dialog.setPreferredSize(new Dimension(600, 600));
        	    dialog.pack();
        	    dialog.setVisible(true);
            }
        });
	
    }
}

