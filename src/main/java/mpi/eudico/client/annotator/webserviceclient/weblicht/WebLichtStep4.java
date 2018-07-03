package mpi.eudico.client.annotator.webserviceclient.weblicht;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.WebLichtTextBasedCommand;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.webserviceclient.weblicht.WLServiceDescriptor;

/**
 * The final step in uploading text to Weblicht and converting the result to a transcription.
 * 
 * @author Han Sloetjes
 */
public class WebLichtStep4 extends ProgressStepPane {
	private Transcription trans;

	// some fields for the settings resulting from the previous steps
	// necessary because the interaction takes place in a separate thread
	private String inputText;
	private Integer sentenceDuration;
	private WLServiceDescriptor wlDescriptor;

	private WebLichtTextBasedCommand wltCommand;
		
	/**
	 * Constructor
	 * @param multiPane
	 */
	public WebLichtStep4(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();
	}

	
	@Override
	protected void initComponents() {
		super.initComponents();
		progressLabel.setText("");
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.Uploading");
	}

	@Override
	protected void endOfProcess() {
		if (wltCommand != null) {
			wltCommand.removeProgressListener(this);
		}
		
		if (completed) {
			// closes the window
			super.endOfProcess();
		} else {
			progressBar.setValue(0);
			multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
			multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
		}
	}

	@Override
	public void enterStepForward() {
		if (progressLabel != null) {
			progressLabel.setText("");
		}
		progressBar.setValue(0);
		
		doFinish();
	}
	
	/**
	 * Collects information from the pane and creates and executes a command.
	 */
	@Override
	public boolean doFinish() {
		// everything has been set here. Start processing
		multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
		
		// get all info from the properties, upload text in a separate thread
		inputText = (String) multiPane.getStepProperty("InputText");
		sentenceDuration = (Integer) multiPane.getStepProperty("SentenceDuration");
		wlDescriptor = (WLServiceDescriptor) multiPane.getStepProperty("WLTokenizerDescriptor");
		
		Object[] args = new Object[]{inputText, sentenceDuration, wlDescriptor};
		
		trans = (Transcription) multiPane.getStepProperty("transcription");

		wltCommand = new WebLichtTextBasedCommand("WebLichtTextBased");
		wltCommand.addProgressListener(this);
		wltCommand.execute(trans, args);
		
//		multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
		return false;
	}

}
