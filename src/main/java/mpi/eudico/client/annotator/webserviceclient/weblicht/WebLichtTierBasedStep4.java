package mpi.eudico.client.annotator.webserviceclient.weblicht;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.AbstractProgressCommand;
import mpi.eudico.client.annotator.commands.WebLichtTierBasedCommand;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.webserviceclient.weblicht.WLServiceDescriptor;

/**
 * The final step in uploading tiers to Weblicht and converting the result to a transcription.
 * THe actual work is delegated to a command.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class WebLichtTierBasedStep4 extends ProgressStepPane {
	private String tierName;
	private String contentType;
	private Transcription trans;
	private WLServiceDescriptor wlDescriptor;
	private AbstractProgressCommand wlCommand;
	
	/**
	 * Constructor
	 * @param multiPane
	 */
	public WebLichtTierBasedStep4(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}

	/**
	 * Show a progress bar.
	 */
	@Override
	protected void initComponents() {
		super.initComponents();
		
		progressLabel.setText("");
	}

	@Override
	public void enterStepForward() {
		if (progressLabel != null) {
			progressLabel.setText("");
		}
		progressBar.setValue(0);
		
		doFinish();
	}
	
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.Uploading");
	}
	
	@Override
	protected void endOfProcess() {

		if (wlCommand != null) {
			wlCommand.removeProgressListener(this);
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
	public boolean doFinish() {		
		// everything has been set here. Start processing
		multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
		
		tierName = (String) multiPane.getStepProperty("Tier");
		contentType = (String) multiPane.getStepProperty("ContentType");
		trans = (Transcription) multiPane.getStepProperty("transcription");
		// get the service descriptor
		wlDescriptor = (WLServiceDescriptor) multiPane.getStepProperty("WLServiceDescriptor");
		// check null...
		if (wlDescriptor == null) {
			String manualURL = (String) multiPane.getStepProperty("ManualServiceURL");
			if (manualURL != null) {
				wlDescriptor = new WLServiceDescriptor("Custom Service");
				wlDescriptor.fullURL = manualURL;
			}
		}
		
		if (wlDescriptor == null) {
			// warn
			showWarningDialog(ElanLocale.getString("WebServicesDialog.WebLicht.Warning4"));
			
			multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
			multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
			return false;
		}
		// can store /retrieve a selected web service
		// can allow an id for the content language 
		completed = false;
		// does this make sense at all? cancel is not supported nicely in the ongoing process situation of 
		// the multiple step functions

		// create a command and register as listener
		wlCommand = new WebLichtTierBasedCommand("WebLicht.TierBased.Command");
		wlCommand.addProgressListener(this);
		wlCommand.execute(trans, new Object[]{wlDescriptor, tierName, contentType});
				
		return false;
	}



}
