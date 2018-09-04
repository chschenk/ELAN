package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.CorpusECVUpdater;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.server.corpora.util.SimpleReport;

/**
 * A command that creates annotation documents for all media files in a folder 
 * and optionally its sub-folders.
 * 
 * @author Han Sloetjes
 */
public class UpdateTranscriptionsForECVCommand implements Command, ProcessReporter {
	private String name;
	private ProcessReport report;
	private int count = 0;
	
	private Boolean canceled = false;
	private CorpusECVUpdater corpusECVUpdater;
	
	/**
	 * Constructor.
	 * 
	 * @param name the name of the command
	 */
	public UpdateTranscriptionsForECVCommand(String name) {
		this.name = name;
	}

	/**
	 * @param receiver a ProgressListener
	 * @param arguments the arguments:
	 * <ul>
	 * <li>arg[0] = the path of the folder where source transcription files are (String)</li>
	 * <li>arg[1] = the path to an output folder, if null the source folder is the destination folder (String)</li>
	 * <li>arg[2] = flag for recursive processing (Boolean)</li>
	 * <li>arg[3] = language</li>
	 * <li>arg[4] = flag for letting the annotation values take precedence over a CVEntry reference (Boolean, optional)</li>
	 * </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		// TODO Use the CorpusECVUpdater class for this
		
		if (report == null) {
			report = new SimpleReport();
		}
		
		report("Start processing...");
		
		corpusECVUpdater = new CorpusECVUpdater(this, (ProgressListener) receiver);
		corpusECVUpdater.setRecursive((Boolean) arguments[2]);
		if (arguments.length >= 5) {
			corpusECVUpdater.setAnnotationValuePrecedence((Boolean) arguments[4]);
		}
		int success = corpusECVUpdater.updateFiles(new String[]{(String) arguments[0], (String) arguments[1]}, (String) arguments[3]);
		
		String status = "No errors occurred.";
		if(success > 0) {
			status = "Some errors occurred";
		}
		report("\nFinished processing. " + status + "\n");
	}

	/**
	 * Returns the name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns the report, can be null.
	 */
	@Override
	public ProcessReport getProcessReport() {
		return report;
	}
	
	/**
	 * Adds a string to the report if it exists.
	 */
	@Override
	public void report(String message) {
		if (report != null) {
			report.append(message);
		}		
	}

	/**
	 * Sets the report object.
	 * @param report the new report, can be null
	 */
	@Override
	public void setProcessReport(ProcessReport report) {
		this.report = report;
	}
	
	public void cancel() {
		canceled = true;
		corpusECVUpdater.cancel();
	}
	public Boolean isCanceled() {
		return canceled;
	}
}
