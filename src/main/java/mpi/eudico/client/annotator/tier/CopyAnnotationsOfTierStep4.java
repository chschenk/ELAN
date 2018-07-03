package mpi.eudico.client.annotator.tier;

import java.util.List;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.CopyAnnotationsOfTierCommand;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Final step where the command to do the actual copying is created and started.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CopyAnnotationsOfTierStep4 extends AbstractProgressStepPane {
	private TranscriptionImpl transcription;
	private List<String> openFiles;
	private String sourceTierName;
	private String targetTierName;
	private String copyMode;// all or with specific value
	private String queryValue;// the value to match
	private Boolean useRegex;
	private Boolean overwrite;
	
	/**
	 * Constructor
	 * @param multiPane the parent pane
	 * @param transcription the transcription or null in case of multiple files 
	 */
	public CopyAnnotationsOfTierStep4(MultiStepPane multiPane, 
			TranscriptionImpl transcription) {
		super(multiPane);
		this.transcription = transcription;
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("CopyAnnotationsDialog.ProgressStep.Title");
	}

	
	/**
	 * Used property keys in previous steps (the relevant ones):
	 * <ul>
	 * <li>SelectedTiers  - List<Object> with tier names, should only contain one element</li>
	 * <li>OpenedFiles  - List<String> a list of selected files to process (in case of multiple file variant)</li>
	 * <li>TargetTier  - the destination tier name</li>
	 * <li>CopyMode  - either ALL or WithValue</li>
	 * <li>QueryValue  - the value of the annotations to copy, can be a regex, can be empty</li>
	 * <li>UseRegex  - treat the specified value as exact match or as regular expression</li>
	 * <li>Overwrite  - whether or not to overwrite existing annotations</li>
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void enterStepForward() {
		// collect settings, or do that in startProcess?
		List<Object> selTiers = (List<Object>)multiPane.getStepProperty("SelectedTiers");
		if (selTiers != null && selTiers.size() > 0) {
			sourceTierName = (String) selTiers.get(0);
		} else {
			showWarningDialog("The source tier is undefined, cannot copy.");
			return;
		}
		targetTierName = (String) multiPane.getStepProperty("TargetTier");
		if (targetTierName == null) {
			showWarningDialog("The destination tier is undefined, cannot copy.");
			return;
		}
		copyMode = (String) multiPane.getStepProperty("CopyMode");
		queryValue = (String) multiPane.getStepProperty("QueryValue");// will be null in case of copy all
		useRegex = (Boolean) multiPane.getStepProperty("UseRegex");
		overwrite = (Boolean) multiPane.getStepProperty("Overwrite");
		
		if (transcription == null) {
			openFiles = (List<String>) multiPane.getStepProperty("OpenedFiles");
		}
		// super.enterStepForward() disables buttons and calls doFinish, which calls startProcess
		super.enterStepForward();
	}

	@Override
	public void startProcess() {
		// start a process
		// construct a command, register as listener, execute the command
		CopyAnnotationsOfTierCommand com = (CopyAnnotationsOfTierCommand)ELANCommandFactory.createCommand(
				transcription, ELANCommandFactory.COPY_ANN_OF_TIER);
		Object[] args = new Object[]{sourceTierName, targetTierName, copyMode, queryValue, useRegex, overwrite};
		com.addProgressListener(this);
		com.execute(transcription, args);
	}

}
