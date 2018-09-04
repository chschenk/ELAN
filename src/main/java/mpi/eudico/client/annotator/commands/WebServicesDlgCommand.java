package mpi.eudico.client.annotator.commands;

import java.awt.Frame;

import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.webserviceclient.tc.TypeCraftfInOutStep;
import mpi.eudico.client.annotator.webserviceclient.tc.TypeCraftStep1;
import mpi.eudico.client.annotator.webserviceclient.tc.TypeCraftStep3;
import mpi.eudico.client.annotator.webserviceclient.tc.TypeCraftStep2;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtStep1;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtStep2;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtStep3;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtStep4;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtTierBasedStep2;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtTierBasedStep3;
import mpi.eudico.client.annotator.webserviceclient.weblicht.WebLichtTierBasedStep4;

/**
 * A command that creates and shows the Web Services window.
 * @author Han Sloetjes
 */
public class WebServicesDlgCommand implements Command {
	private String name;
	
	/**
	 * Constructor.
	 * @param name name of the command
	 */
	public WebServicesDlgCommand(String name) {
		this.name = name;
	}

	/**
	 * @param receiver the viewermanager (or transcription?)
	 * @param arguments null
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		ViewerManager2 vm = (ViewerManager2) receiver;
		/*
		WebServicesDialog dialog = null;
		if (vm != null) {
			dialog = new WebServicesDialog(
				ELANCommandFactory.getRootFrame(vm.getTranscription()), false);
		} else {
			dialog = new WebServicesDialog();
		}
		dialog.setTranscription((TranscriptionImpl) vm.getTranscription());
		dialog.setVisible(true);
		*/
		if (arguments != null && arguments.length > 0) {
			if (arguments[0] == ELANCommandFactory.WEBLICHT_DLG) {
				MultiStepPane pane = new MultiStepPane(
					    ElanLocale.getResourceBundle());
				    StepPane step1 = new WebLichtStep1(pane);
				    pane.addStep(step1);
				    step1.setName("TextOrTierStep1");

				    StepPane step2 = new WebLichtStep2(pane);
				    step2.setName("TextStep2");
				    pane.addStep(step2);
				    
				    StepPane step3 = new WebLichtStep3(pane);
				    step2.setName("TextStep3");
				    pane.addStep(step3);

				    StepPane step4 = new WebLichtStep4(pane);
				    step2.setName("TextStep4");
				    pane.addStep(step4);
				    
					StepPane stTier2 = new WebLichtTierBasedStep2(pane);
					stTier2.setName("TierStep2");
					pane.addStep(stTier2);
					
					StepPane stTier3 = new WebLichtTierBasedStep3(pane);
					stTier3.setName("TierStep3");
					pane.addStep(stTier3);
					
					StepPane stTier4 = new WebLichtTierBasedStep4(pane);
					stTier4.setName("TierStep4");
					pane.addStep(stTier4);
					
					JDialog dialog;
					if (vm != null) {
						pane.putStepProperty("transcription", vm.getTranscription());
						dialog = pane.createDialog(ELANCommandFactory.getRootFrame(vm.getTranscription()), 
								ElanLocale.getString(ELANCommandFactory.WEBLICHT_DLG), true);
					} else {
						dialog = pane.createDialog((Frame) null, 
								ElanLocale.getString(ELANCommandFactory.WEBLICHT_DLG), true);
					}
					dialog.setVisible(true);
			} else if (arguments[0] == ELANCommandFactory.TYPECRAFT_DLG) {
			    // create wizard dialog component
			    MultiStepPane pane = new MultiStepPane(
				    ElanLocale.getResourceBundle());

			    // add the login step as a panel in the component
			    StepPane stepOne = new TypeCraftStep1(pane);
			    pane.addStep(stepOne);

			    // add a panel for the step of chosing between download and upload
			    StepPane choseStep = new TypeCraftfInOutStep(pane);
			    choseStep.setName("updownchoice");
			    pane.addStep(choseStep);

			    // add a panel containing the step to handle download
		 	    StepPane stepTwo = new TypeCraftStep2(pane);
		 	    stepTwo.setName("download");
		 	    stepTwo.setPreferredPreviousStep(choseStep.getName());
		 	    pane.addStep(stepTwo);

			    // add a panel for the upload step
			    StepPane stepThree = new TypeCraftStep3(pane);
			    stepThree.setName("upload");
			    stepThree.setPreferredPreviousStep(choseStep.getName());
			    pane.addStep(stepThree);

				JDialog dialog;
				if (vm != null) {
					pane.putStepProperty("transcription", vm.getTranscription());
					dialog = pane.createDialog(ELANCommandFactory.getRootFrame(vm.getTranscription()), 
							ElanLocale.getString(ELANCommandFactory.TYPECRAFT_DLG), true);
				} else {
					dialog = pane.createDialog((Frame) null, 
							ElanLocale.getString(ELANCommandFactory.TYPECRAFT_DLG), true);
				}
				dialog.setVisible(true);
			}
		}
	}

	/**
	 * Returns the name
	 */
	@Override
	public String getName() {
		return name;
	}

}
