package mpi.eudico.client.annotator.interannotator;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * An AnnotatorCompare object holds a wizard dialog for configuring and carrying
 * out rater agreement analysis.
 * 
 * Review comments
 * 
 * @author keeloo
 */

public class AnnotatorCompare {
    
    /*
     * remember the frame from the message send to the constructors , because it
     * is referenced in the classic dialog
     */
    private JFrame frame;

    // if not null, transcription holds the associated transcription

    private TranscriptionImpl transcription;
    
    JDialog dialog;

    /**
     * Create wizard dialog for comparing raters after receiving a message from
     * a CompareAnnotatorsDlgCommand object.
     * 
     * @param transcription
     *            transcription currently associated with a frame object
     * @param frame
     *            JFrame object that is parent to the dialog
     * @param modal
     *            if and only if true, the dialog will be modal
     */
    public AnnotatorCompare(TranscriptionImpl transcription, JFrame frame,
	    Boolean modal) {
	
		this.transcription = transcription;
		this.frame = frame;
	
		/* Create the wizard object, use the locale that is associated with the
		 * frame.
		 */
	
		MultiStepPane wizard = new MultiStepPane(ElanLocale.getResourceBundle());
		
		// create a dialog object for the wizard to use
		dialog = wizard.createDialog(frame,
			ElanLocale.getString("CompareAnnotatorsDialog.Title"), modal);
	
		wizard = addSteps(wizard);
	
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setSize(dialog.getWidth() + 60, dialog.getHeight());
		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);
	
    }

    /**
     * Create wizard dialog for comparing raters after receiving a message from
     * a CompareAnnotatorsMA object.
     *
     * @param frame
     *            ElanFrame2 object that is parent to the dialog
     * @param modal
     *            if and only if true, the dialog will be modal
     */
    public AnnotatorCompare(ElanFrame2 frame, Boolean modal) {	
		this.frame = frame;
		this.transcription = null;
	
		// create the wizard, use the locale that is associated with the frame
		MultiStepPane wizard = new MultiStepPane(ElanLocale.getResourceBundle());
	
		// create the wizard dialog
		dialog = wizard.createDialog(frame,
			ElanLocale.getString("CompareAnnotatorsDialog.Title"), modal);
	
		wizard = addSteps(wizard);
	
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		dialog.pack();
		dialog.setSize(dialog.getWidth() + 60, dialog.getHeight() + 60);
		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);
    }

    /**
     * Characterize the wizard by added steps to it.
     * 
     * Notes on the wizard. Forward steps are disabled by default, backward steps
     * are enabled by default. In the dialog belonging to a step, use
     * putStepProperty to convey the information obtained in the step between
     * the steps. At any step, the choices can be inspected and the appropriate
     * actions can be taken. 
     * 
     * The path taken by the steps is controlled by the wizard buttons: previous,
     * next, cancel, finish. Next to this, the path can be altered by skipping to
     * a certain step. 
     * 
     * @param wizard
     * 
     * @return wizard
     */
    private MultiStepPane addSteps(MultiStepPane wizard) {

		StepPane selectMethod = new MethodSelectionStep(wizard, transcription);
		//selectMethod.setName("Method");
		wizard.addStep(selectMethod);
		
		StepPane customizeStep = new CustomizationStep(wizard);
		//customizeStep.setName("Customize");
		wizard.addStep(customizeStep);
	
		StepPane selectDocument = new DocumenSelectionStep(wizard, transcription);
		//selectDocument.setName("Document");
		wizard.addStep(selectDocument);
	
	    StepPane filesSelection = new FilesSelectionStep(wizard, transcription);
	    //filesSelection.setName("Files");
		wizard.addStep(filesSelection);
		    
		StepPane tierSelection = new TiersSelectionStep(wizard, transcription);
		//tierSelection.setName("Tiers");
		wizard.addStep(tierSelection);
		
		StepPane progressStep = new CompareProgressStep(wizard, transcription);
		//progressStep.setName("Progress");
		wizard.addStep(progressStep);

		return wizard;
    }
}
