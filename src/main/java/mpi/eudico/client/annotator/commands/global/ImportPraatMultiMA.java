package mpi.eudico.client.annotator.commands.global;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.imports.multiplefiles.MFPraatImportStep1;
import mpi.eudico.client.annotator.imports.multiplefiles.MFPraatImportStep2;
import mpi.eudico.client.annotator.imports.multiplefiles.MFPraatImportStep3;
import mpi.eudico.client.annotator.imports.multiplefiles.MFPraatImportStep4;

public class ImportPraatMultiMA extends FrameMenuAction{
    /**
     * Creates a new ImportPraatMultiMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public ImportPraatMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows an import praat dialog and creates new transcriptions.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
    	MultiStepPane multipane = new MultiStepPane();
    	multipane.addStep(new MFPraatImportStep1(multipane));
    	multipane.addStep(new MFPraatImportStep2(multipane));
    	multipane.addStep(new MFPraatImportStep3(multipane));
    	multipane.addStep(new MFPraatImportStep4(multipane));
    	

    	JDialog dialog = multipane.createDialog(frame, ElanLocale.getString("MultiFileImport.Praat.Title"), true);
	    dialog.setPreferredSize(new Dimension(600, 600));
	    dialog.pack();
	    dialog.setVisible(true);	
    }
}
