package mpi.eudico.client.annotator.commands;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ngramstats.NgramStatsResult;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.client.annotator.util.SquelchOutput;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.SimpleReport;

/**
 * Executes the N-gram analysis
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class NgramStatsCommand implements Command {
    private String commandName;
    private ProgressListener listener;
    private List<String> selectedFiles;
    private NgramStatsResult ngramsResult;
    private NgramsThread ngramsThread;
    private SimpleReport report = new SimpleReport(ElanLocale.getString("ProcessReport"));

	public NgramStatsCommand(String commandName) {
		super();
		this.commandName = commandName;
	}

	@Override
	public void execute(Object receiver, Object[] arguments) {
		selectedFiles = (List<String>) arguments[0];
		ngramsResult = (NgramStatsResult) arguments[1];
        
        ngramsThread = new NgramsThread(commandName);
        
        try {
        	ngramsThread.start();
        } catch(Exception ex) {
        	report.append("Error in executing analysis: " + ex.getMessage());
        }
	}

	@Override
	public String getName() {
		return commandName;
	}

    public synchronized void addProgressListener(ProgressListener pl) {
    	listener = pl;
    }

    public synchronized void removeProgressListener(ProgressListener pl) {
        listener = null;
    }

    /**
     * Notifies any listeners of a progress update.
     * @param percent the new progress percentage, [0 - 100]
     * @param message a descriptive message
     */
    private void progressUpdate(int percent, String message) {
    	if (listener != null) {
    		listener.progressUpdated(this, percent, message);
    	}
    }

    /**
     * Notifies any listeners that the process has completed.
     * @param message a descriptive message
     */
    private void progressComplete(String message) {
    	if (listener != null) {
    		listener.progressCompleted(this, message);
    	}
    }
    
    /**
     * The report generated during parsing
     * @return ProcessReport the report
     */
    public ProcessReport getProcessReport() {
    	return report;
    }
    
//############################################################################
    private class NgramsThread extends Thread {
    	public NgramsThread(String name) {
    		super(name);
    	}

    	@Override
    	public void run() {
    		// how much to increment the progress counter per file
            float perFileProg = 100 / (float)selectedFiles.size();
            
            // some counters
            int numFailed = 0;
            
            // It's super-ANNOYING to see gazillions of lines emitted by the EAFParser encountering errors
            // Seems like they don't affect our usage of ELAN but... sample lines:
            // [Error] _984_small.eaf:812:23: cvc-id.1: There is no ID/IDREF binding for IDREF 'EN'.
            // [Error] 640_slave2.eaf:1562:92: cvc-id.2: There are multiple occurrences of ID value 'a10'.
            // [Error] 640_slave2.eaf:1562:92: cvc-attribute.3: The value 'a10' of attribute 'ANNOTATION_ID' on element 'ALIGNABLE_ANNOTATION' is not valid with respect to its type, 'ID'.
            
            // the solution is to... shut up the parser's capability to emit lines while parsing :)
            SquelchOutput squelcher = new SquelchOutput();
            try {
            	squelcher.squelchOutput();
            } catch (Exception e) {
            	report.append("Error squelching parser: " + e.getMessage());
            	progressComplete("ERROR");
            	return;
            }
            
            for (int i = 0; i < selectedFiles.size(); i++) {
                try {
                	TranscriptionImpl trans = new TranscriptionImpl( selectedFiles.get(i) );
                	TierImpl tier = trans.getTierWithId(ngramsResult.getTier());
                	
                	if (tier != null) {
	                	// Loop through the annotations and add them to the result
	                	List<AbstractAnnotation> annotations = tier.getAnnotations();
	                	if ( annotations.size() > 0 ) {
	                		ngramsResult.startFile(selectedFiles.get(i));
	                    	for (int ann = 0; ann < annotations.size(); ann++) {
	                    		ngramsResult.addAnnotation(annotations.get(ann));
	                    	}
	                    	ngramsResult.endFile();
	                	}
                	} else {
                		report.append("Selected tier(" + ngramsResult.getTier() + ") not present in file: " + selectedFiles.get(i));
                	}

                    progressUpdate( (int)((i + 1) * perFileProg - 1), "Processed file: " + selectedFiles.get(i));
                } catch (Exception ex) {
                	report.append("Error parsing file(" + selectedFiles.get(i) + "): " + ex.getMessage());
                	
                	numFailed++;
                    progressUpdate( (int)((i + 1) * perFileProg - 1), "Unable to load file(" + selectedFiles.get(i) + "): " + ex.getMessage());
                    continue;
                }
            }
            
            // restore the output handlers
            try {
            	squelcher.restoreOutput();
            } catch (Exception e) {
            	report.append("Error unsquelching parser: " + e.getMessage());
            	progressComplete("ERROR");
            	return;
            }
            
            progressUpdate(99, "Calculating Statistics...");
            try {
            	ngramsResult.calculateStatistics();
            } catch (Exception ex) {
            	// add useful info from the exception
            	report.append("Error calculating statistics: " + ex.getMessage());
            	final Writer rv = new StringWriter();
            	final PrintWriter rvWriter = new PrintWriter(rv);
            	ex.printStackTrace(rvWriter);
            	report.append("Stack Trace: " + rv.toString());            	
            	
            	progressComplete("ERROR");
            	return;
            }

            // prepend the header to the report with misc stats
            report.prepend("----------\n Parsing run done in " + ngramsResult.getSearchTime() + " secs" +
            		"\n Selected Domain: " + ngramsResult.getDomain() +
            		"\n Selected Tier: " + ngramsResult.getTier() +
            		"\n N-gram Size: " + ngramsResult.getNgramSize() +
            		"\n Files Inspected: " + ngramsResult.getNumFiles() +
            		"\n Files Failed: " + numFailed +
            		"\n Total Annotations: " + ngramsResult.getNumAnnotations() +
            		"\n Total N-grams: " + ngramsResult.getNumNgrams() +
            		"\n Collated N-grams: " + ngramsResult.getNumCollectedNgrams() +
            "\n----------\n");
            
            progressComplete("Statistics completed.");
    	}
    }
}
