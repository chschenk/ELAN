package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

import mpi.eudico.client.annotator.imports.multiplefiles.MFToolboxImportStep1;
import mpi.eudico.client.annotator.imports.multiplefiles.MFToolboxImportStep2;
import mpi.eudico.client.annotator.imports.multiplefiles.MFToolboxImportStep3;
import mpi.eudico.client.annotator.imports.multiplefiles.MFToolboxImportStep4;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;


/**
 * Action that starts an Import Toolbox for multiple files action
 *
 * @author aarsom, April 2012
 */
public class ImportToolboxMultiMA extends FrameMenuAction {
    /**
     * Creates a new ImportToolboxMultiMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public ImportToolboxMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows an import Toolbox dialog and creates new transcriptions.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
    	
    	MultiStepPane multipane = new MultiStepPane();
    	multipane.addStep(new MFToolboxImportStep1(multipane));
    	multipane.addStep(new MFToolboxImportStep2(multipane));
    	multipane.addStep(new MFToolboxImportStep3(multipane));
    	multipane.addStep(new MFToolboxImportStep4(multipane));
    	

    	JDialog dialog = multipane.createDialog(frame, ElanLocale.getString("MultiFileImport.Toolbox.Title"), true);
	    dialog.setPreferredSize(new Dimension(600, 600));
	    dialog.pack();
	    dialog.setVisible(true);	
    }
}
