package mpi.eudico.client.annotator.commands;

import java.awt.Dimension;

import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.OverlapsOrSubtractionStep1;
import mpi.eudico.client.annotator.tier.OverlapsOrSubtractionStep3;
import mpi.eudico.client.annotator.tier.OverlapsOrSubtractionStep4;
import mpi.eudico.client.annotator.tier.OverlapsOrSubtractionStep5;
import mpi.eudico.client.annotator.tier.OverlapsStep2;
import mpi.eudico.client.annotator.tier.SubtractionStep2;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Creates the "annotations from overlaps wizard".
 */
public class AnnotationsFromOverlapsDlgCommand implements Command {
    private String commandName;
    private boolean subtraction;
    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AnnotationsFromOverlapsDlgCommand(String name) {
       this(name, false);
    }
       
    
    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AnnotationsFromOverlapsDlgCommand(String name, boolean subtraction) {  
    	commandName = name;
        this.subtraction = subtraction;   
    }

    /**
     * Creates the "annotations from overlaps wizard".
     *
     * @param receiver the transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;       

        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        
        StepPane step1 = new OverlapsOrSubtractionStep1(pane, trans, subtraction);
        StepPane step2 = null;
        if(subtraction){
        	step2 = new SubtractionStep2(pane);
        }else{
        	step2 = new OverlapsStep2(pane, ELANCommandFactory.getRootFrame(trans));
        }
        StepPane step3 = new OverlapsOrSubtractionStep3(pane, trans, subtraction);
        StepPane step4 = new OverlapsOrSubtractionStep4(pane, subtraction);
        StepPane step5 = new OverlapsOrSubtractionStep5(pane, trans, subtraction);
        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);
        pane.addStep(step4);
        pane.addStep(step5);
        
        String title = null;
        if(subtraction){
        	title = ElanLocale.getString("SubtractAnnotationDialog.Title");
        }else{
        	title = ElanLocale.getString("OverlapsDialog.Title");
        }

        JDialog dialog = pane.createDialog(ELANCommandFactory.getRootFrame(trans), title, true);
        dialog.setPreferredSize(new Dimension(600, 680));
        dialog.pack();
        dialog.setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}
