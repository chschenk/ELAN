package mpi.eudico.client.annotator.interannotator;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.AbstractCompareCommand;
import mpi.eudico.client.annotator.commands.CompareAnnotationModKappaCommand;
import mpi.eudico.client.annotator.commands.CompareAnnotationRatioMultiCommand;
import mpi.eudico.client.annotator.commands.CompareAnnotationStaccatoCommand;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Transcription;

@SuppressWarnings("serial")
public class CompareProgressStep extends ProgressStepPane implements ActionListener {
    private Transcription transcription;
    private Map<Object, Object> allStepProperties;
	// a command implementing one of the available method commands
	private AbstractCompareCommand comCommand;
	private JButton cancelProcessButton;

    /**
     * 
     * @param multiPane the wizard
     * @param transcription the transcription in case of single document processing
     */
	public CompareProgressStep(MultiStepPane multiPane, Transcription transcription) {
		super(multiPane);
		// can be null
		this.transcription = transcription;
		// enable the wizard to jump to a specific step by looking at step names
		this.setName("CompareAnnotatorsDialog.CompareProgressStep");
		allStepProperties = new HashMap<Object, Object>();
		
		initComponents();
	}

	@Override
	protected void initComponents() {
		super.initComponents();
		progressLabel.setText(ElanLocale.getString("CompareAnnotatorsDialog.ProgressStep.Title"));
		// add a cancel button that cancels the current process but that doesn't close the dialog?
		cancelProcessButton = new JButton(ElanLocale.getString("MultiStep.Cancel"));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		add(cancelProcessButton, gbc);
		cancelProcessButton.addActionListener(this);
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("CompareAnnotatorsDialog.ProgressStep.Title");
	}

	/**
	 * Retrieves all step properties assuming all necessary sanity checks have been performed before.
	 * Starts the actual processing.  
	 */
	@Override
	public void enterStepForward() {		
		doFinish();
	}

	/**
	 * This method is currently not used by the host MultiPane. finished is called when the cancel button is pressed.
	 */
	@Override
	public void cancelled() {
	}

	@Override
	public boolean doFinish() {
		// to do make sure the dialog is not closed by frame close buttons??
		// disable buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
        // don't use the default cancel button it disposes of the dialog
        //multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
		
        // get step properties
		allStepProperties.clear();
		// copy the step properties, assume the necessary checks have been performed in earlier steps  
		Iterator<Object> propIter = multiPane.getPropertyKeys().iterator();
		while (propIter.hasNext()) {
			Object key = propIter.next();
			allStepProperties.put(key, multiPane.getStepProperty(key));
		}
		completed = false;
		cancelProcessButton.setEnabled(true);
		// retrieve compare method
		CompareConstants.METHOD compMethod = (CompareConstants.METHOD) allStepProperties.get(CompareConstants.METHOD_KEY);
		
		if (compMethod == CompareConstants.METHOD.MOD_KAPPA) {
			comCommand = new CompareAnnotationModKappaCommand("CompareModifiedKappa");
	        comCommand.addProgressListener(this);
	        comCommand.execute(transcription, new Object[]{allStepProperties});
		} else if (compMethod == CompareConstants.METHOD.STACCATO) {
			comCommand = new CompareAnnotationStaccatoCommand("CompareStaccato");
	        comCommand.addProgressListener(this);
	        comCommand.execute(transcription, new Object[]{allStepProperties});
		} else if (compMethod == CompareConstants.METHOD.CLASSIC) {
	        comCommand = new CompareAnnotationRatioMultiCommand("CompareMultiRatio");
	        comCommand.addProgressListener(this);
	        comCommand.execute(transcription, new Object[]{allStepProperties});
		}	
   
		return false;
	}

	/**
	 * This is called when the Cancel button has been pressed. Closes the dialog.
	 */
	@Override
	public void finished() {
		if (comCommand != null) {
			comCommand.cancelProcess();
		}
	}

	@Override
	public String getName() {
		return super.getName();
	}	

	@Override
	protected void endOfProcess() {
		// pop up results and or report or a save as dialog
		if (!completed) {
			ClientLogger.LOG.warning("The calculation process was interrupted.");
			// a message that there was an interrupt should have been shown already here
			//showMessageDialog(ElanLocale.getString("CompareAnnotatorsDialog.Message.Interrupted"));
			// ask if the dialog should be closed??
			setCancelOrPreviousState();
			return;
		}
		
		// comCommand != null
		List<CompareCombi> resultList = comCommand.getCompareSegments();
		if (resultList != null) {
			//printSegments(resultList);
			// prompt for file save to text
			FileChooser chooser = new FileChooser(this);		
			chooser.createAndShowFileAndEncodingDialog(null,  FileChooser.SAVE_DIALOG, 
					FileExtension.TEXT_EXT, "LastUsedExportDir", FileChooser.UTF_8);
			File exportFile = chooser.getSelectedFile();
			String encoding = chooser.getSelectedEncoding();
			if (exportFile != null) {

				try {
					comCommand.writeResultsAsText(exportFile, encoding);
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("Output error: " +ioe.getMessage());
					showMessageDialog(ElanLocale.getString("CompareAnnotatorsDialog.Message.OutputError") + " " +
							ioe.getMessage());
				} catch (Throwable t) {
					ClientLogger.LOG.warning("Output error: " + t.getMessage());
					showMessageDialog(ElanLocale.getString("CompareAnnotatorsDialog.Message.OutputError") + " " +
							t.getMessage());
				}
			} else {
				ClientLogger.LOG.info("Saving of the results was cancelled");
				// cancelled
				setCancelOrPreviousState();
			}
		} else {
			ClientLogger.LOG.warning("No comparison results available.");
			showMessageDialog(ElanLocale.getString("CompareAnnotatorsDialog.Message.NoResults"));
		}
		// super closes the dialog
		super.endOfProcess();
	}
	
	/**
	 * Enables and disables buttons such that the dialog can be cancelled or
	 * the user can go back to the previous step(s) to modify settings.
	 */
	private void setCancelOrPreviousState() {
		multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
		cancelProcessButton.setEnabled(false);
		// reset the progress bar
        if (progressLabel != null) {
            progressLabel.setText("");
        }

        progressBar.setValue(0);
	}
	
	private void printSegments(List<CompareCombi> compareSegments) {
		for (int i = 0; i < compareSegments.size(); i++) {
			System.out.println("Compare index: " + (i + 1));
			CompareCombi cc = compareSegments.get(i);
			System.out.println("F1: " + cc.getFirstUnit().fileName + " T1: " + cc.getFirstUnit().tierName);
			System.out.println("\tA1: " + cc.getFirstUnit().annotations.size());
			System.out.println("F2: " + cc.getSecondUnit().fileName + " T2: " + cc.getSecondUnit().tierName);
			System.out.println("\tA2: " + cc.getSecondUnit().annotations.size());
			System.out.println("Agreement: " + cc.getOverallAgreement());
			System.out.println("#####");
		}
	}
	
	@Override
	public void progressCompleted(Object source, String message) {
        if (progressLabel != null) {
            progressLabel.setText(message);
        }

        progressBar.setValue(100);
        if (!completed) {
        	completed = true;
	        endOfProcess();        
        }
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == cancelProcessButton) {
			if (comCommand != null) {
				comCommand.cancelProcess();
			}
			setCancelOrPreviousState();
		}
		
	}
	
}
