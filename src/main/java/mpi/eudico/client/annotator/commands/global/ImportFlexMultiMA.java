package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

import mpi.eudico.client.annotator.imports.multiplefiles.MFFlexImportStep1;
import mpi.eudico.client.annotator.imports.multiplefiles.MFFlexImportStep2;
import mpi.eudico.client.annotator.imports.multiplefiles.MFFlexImportStep3;
import mpi.eudico.client.annotator.imports.multiplefiles.MFFlexImportStep4;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;


/**
 * Action that starts an Import Flex for multiple files action
 *
 * @author aarsom, April 2013
 */
public class ImportFlexMultiMA extends FrameMenuAction {
    /**
     * Creates a new ImportFlexMultiMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public ImportFlexMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows an import Flex dialog and creates new transcriptions.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
    	
    	MultiStepPane multipane = new MultiStepPane();
    	multipane.addStep(new MFFlexImportStep1(multipane));
    	multipane.addStep(new MFFlexImportStep2(multipane));
    	multipane.addStep(new MFFlexImportStep3(multipane));
    	multipane.addStep(new MFFlexImportStep4(multipane));
    	

    	JDialog dialog = multipane.createDialog(frame, ElanLocale.getString("MultiFileImport.Flex.Title"), true);
	    dialog.setPreferredSize(new Dimension(600, 600));
	    dialog.pack();
	    dialog.setVisible(true);	
    }
}
