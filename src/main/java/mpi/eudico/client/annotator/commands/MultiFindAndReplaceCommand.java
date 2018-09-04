package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.search.model.EAFType;
import mpi.eudico.client.annotator.search.model.ElanSearchEngine;
import mpi.eudico.client.annotator.search.result.model.Replace;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.model.QueryFormulationException;
import mpi.search.content.result.model.ContentResult;


/**
 * A command to perform a find and replace in multiple eaf files. Builds on the
 * single file and replace functionality.
 *
 * @author Han Sloetjes
 */
public class MultiFindAndReplaceCommand implements Command, ProcessReporter {
    private String commandName;
    private ArrayList<ProgressListener> listeners;
    private File[] searchFiles;
    private String[] selectedTiers;
    private String searchPattern;
    private String replPattern;
    private boolean regExp;
    private boolean caseSens;
    private ReplaceThread frThread = null;
    private ProcessReport report;

    /**
     * Creates a new MultiFindAndReplaceCommand instance
     *
     * @param name the name of the command
     */
    public MultiFindAndReplaceCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments: <ul><li>arg[0] = the files to search
     *        (File[])</li> <li>arg[1] the tiers to search, in case of null or
     *        zero length all tiers will be searched (String[])</li>
     *        <li>arg[2] the search pattern (String)</li>
     *        <li>arg[3] the replace pattern (String)</li> <li>arg[4] flag
     *        whether it is a regular expression (Boolean)</li> <li>arg[5]
     *        flag whether to match case sensitive (Boolean)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        searchFiles = (File[]) arguments[0];
        selectedTiers = (String[]) arguments[1];
        searchPattern = (String) arguments[2];
        replPattern = (String) arguments[3];
        regExp = ((Boolean) arguments[4]).booleanValue();
        caseSens = ((Boolean) arguments[5]).booleanValue();

        if ((searchFiles == null) || (searchFiles.length == 0)) {
            report("No files provided to Find and Replace.");
            progressInterrupt("Illegal argument: no files provided");
            return;
        }

        report("Number of files to process:  " + searchFiles.length);

        if (selectedTiers == null || selectedTiers.length == 0) {
        	selectedTiers = new String[]{Constraint.ALL_TIERS};
        	report("Tiers to search: all tiers");
        } else {
        	report("Tiers to search: ");
        	for (String nm : selectedTiers) {
        		report("\t" + nm);
        	}
        }

        
        if ((searchPattern == null) || (searchPattern.length() == 0) ||
                (replPattern == null)) {
            report("Illegal find or replace pattern provided.");
            progressInterrupt("Illegal search or replace pattern provided");
            return;
        }

        report("Search string:  " + searchPattern);
        report("Replace string:  " + replPattern);
        report("Regular expression:  " + regExp);
        report("Case sensitive:  " + caseSens);
        report("\n");
        // checks??
        frThread = new ReplaceThread(MultiFindAndReplaceCommand.this.getName());

        try {
            frThread.start();
        } catch (Exception exc) {
            // any exception
            report("An exception occurred: " + exc.getMessage());
            progressInterrupt("An exception occurred: " + exc.getMessage());
        }
    }

    /**
     * Requests the running find and replace thread to stop. It depends on the
     * current ongoing operation how long it will take before execution stops.
     */
    public void interrupt() {
        if (frThread != null) {
            frThread.interrupt();
        }
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * Adds a ProgressListener to the list of ProgressListeners.
     *
     * @param pl the new ProgressListener
     */
    public synchronized void addProgressListener(ProgressListener pl) {
        if (listeners == null) {
            listeners = new ArrayList<ProgressListener>(2);
        }

        listeners.add(pl);
    }

    /**
     * Removes the specified ProgressListener from the list of listeners.
     *
     * @param pl the ProgressListener to remove
     */
    public synchronized void removeProgressListener(ProgressListener pl) {
        if ((pl != null) && (listeners != null)) {
            listeners.remove(pl);
        }
    }

    /**
     * Returns the proces report.
     *
     * @return the process report, or null
     */
    @Override
	public ProcessReport getProcessReport() {
        return report;
    }

    /**
     * Sets the process report.
     *
     * @param report the new report to append messages to
     */
    @Override
	public void setProcessReport(ProcessReport report) {
        this.report = report;
    }

    /**
     * Adds a message to the report.
     *
     * @param message the message
     */
    @Override
	public void report(String message) {
        if (report != null) {
            report.append(message);
        }
    }

    /**
     * Notifies any listeners of a progress update.
     *
     * @param percent the new progress percentage, [0 - 100]
     * @param message a descriptive message
     */
    private void progressUpdate(int percent, String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressUpdated(this,
                    percent, message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has completed.
     *
     * @param message a descriptive message
     */
    private void progressComplete(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressCompleted(this,
                    message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has been interrupted.
     *
     * @param message a descriptive message
     */
    private void progressInterrupt(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressInterrupted(this,
                    message);
            }
        }
    }

    /**
     * The actual find and replace is performed in a separate thread.
     *
     * @author Han Sloetjes
     */
    private class ReplaceThread extends Thread {
        private boolean interruptRequested = false;

        /**
         * Creates a new thread to find and replace in multiple files.
         */
        public ReplaceThread() {
            super();
        }

        /**
         * Creates a new thread to find and replace in multiple files.
         *
         * @param name the name of the thread
         */
        public ReplaceThread(String name) {
            super(name);
        }

        /**
         * Sets the flag that an interrupt request was received. The current
         * find  and replace action will stop if it is not just writing a
         * changed transcription. This methods returns immediately.
         */
        @Override
		public void interrupt() {
            interruptRequested = true;
        }

        /**
         * The actual action of this thread.
         *
         * @see java.lang.Runnable#run()
         */
        @Override
		public void run() {
            float perFileProg = 100 / (float) searchFiles.length;

            // a few counters
            int numInspected = 0;
            int numFailed = 0;
            int numChanged = 0;

            File f = null;
            String path;
            TranscriptionImpl trans = null;
            ElanSearchEngine engine;
            ContentQuery query;
            ContentResult result;
            AnchorConstraint constraint = new AnchorConstraint();
            constraint.setCaseSensitive(caseSens);
            constraint.setRegEx(regExp);
            constraint.setLowerBoundary(Long.MIN_VALUE);
            constraint.setUpperBoundary(Long.MAX_VALUE);
            constraint.setPattern(searchPattern);
            //constraint.setTierNames(new String[] { Constraint.ALL_TIERS });
            constraint.setTierNames(selectedTiers);
            constraint.setUnit(Constraint.IS_INSIDE);

            EAFType type = new EAFType();

            for (int i = 0; i < searchFiles.length; i++) {
                // stop is there the operation is interrupted
                if (interruptRequested) {
                    // update report
                    //super.interrupt();//just break the loop
                    report("Search loop interrupted at index:  " + i);
                    finalReport(numInspected, numChanged, numFailed);
                    progressInterrupt("Operation interrupted...");

                    break;
                }

                f = searchFiles[i];

                if (!f.exists() || f.isDirectory()) {
                    numFailed++;
                    report("Skipping file: " + f.getAbsolutePath());
                    progressUpdate((int) (i * perFileProg),
                        ("Skipping file: " + f.getAbsolutePath()));

                    continue;
                }

                path = f.getAbsolutePath();
                path = path.replace('\\', '/');

                try {
                    trans = new TranscriptionImpl(new File(path).getAbsolutePath());
                    trans.setUnchanged();
                    trans.setNotifying(false);
                } catch (Exception ex) {
                    numFailed++;
                    // any exception 
                    report("Can not load file: " + f.getAbsolutePath());
                    report("Cause: " +
                        ((ex.getMessage() != null) ? ex.getMessage()
                                                   : "Unknown file loading or parsing error..."));
                    progressUpdate((int) (i * perFileProg),
                        ("Can not load file: " + f.getAbsolutePath()));

                    continue;
                }

                if (interruptRequested) {
                    // update report
                    //super.interrupt();//just break the loop
                    report("Search loop interrupted at index:  " + i);
                    finalReport(numInspected, numChanged, numFailed);
                    progressInterrupt("Operation interrupted...");

                    break;
                }

                engine = new ElanSearchEngine(null, trans); // or pass a listener??
                query = new ContentQuery(constraint, type);
                // HS 16 March 2010 adjust the Query object such, that only the names of tiers
                // that are present in the transcription are in the tier name array
                if (selectedTiers[0] != Constraint.ALL_TIERS) {
                	ArrayList<String> validNames = new ArrayList<String>(selectedTiers.length);
                	for (String n : selectedTiers) {
                		if (trans.getTierWithId(n) != null) {
                			validNames.add(n);
                		}
                	}
                	if (validNames.size() == 0) {
                		// none of the tiers are in the file
                		numInspected++;
                        report("None of the selected tiers found " + 
                        		"\nin file: " + f.getAbsolutePath());
                        //finalReport(numInspected, numChanged, numFailed);
                        progressUpdate((int) (i * perFileProg),
                            ("None of the tiers in file: " +
                            f.getAbsolutePath()));
                		continue;// next file
                	}
                	constraint.setTierNames(validNames.toArray(new String[]{}));
                }
                
                try {
                    engine.performSearch(query);
                    result = (ContentResult) query.getResult();

                    int numHits = result.getRealSize(); //log or report
                    numInspected++;
                    report("Number of hits in " + f.getAbsolutePath() + " :  " +
                        numHits);

                    if (numHits == 0) {
                        progressUpdate((int) (i * perFileProg),
                            ("No hits in file: " + f.getAbsolutePath()));

                        continue;
                    }

                    // create a backup of the file??
                    Replace.execute(result, replPattern, trans);

                    // save the file
                    if (!interruptRequested) {
                        // save
        				int saveAsType = SaveAs27Preferences.saveAsType(trans);

                        try {
                            ACMTranscriptionStore.getCurrentTranscriptionStore()
                                                 .storeTranscription(trans,
                                null, new ArrayList<TierImpl>(0), saveAsType);
                            numChanged++;
                            progressUpdate((int) (i * perFileProg),
                                ("Processed succesfully: " +
                                f.getAbsolutePath()));
                            report("Saved file successfully: " + f.getAbsolutePath());
                        } catch (IOException ioe) {
                            numFailed++;
                            report("Could not save the file: " +
                                f.getAbsolutePath());
                            report("because: " +
                                ((ioe.getMessage() != null) ? ioe.getMessage()
                                                            : "unknown cause..."));
                        }

                        // progress update
                    } else {
                        // update report
                        report("Search loop interrupted at index:  " + i);
                        finalReport(numInspected, numChanged, numFailed);
                        progressInterrupt("Operation interrupted...");

                        break;
                    }
                } catch (PatternSyntaxException pse) {
                    numFailed++;
                    numInspected++;
                    report("Pattern exception: " + pse.getDescription());
                    // we can stop here because the same pattern is used in each iteration
                    finalReport(numInspected, numChanged, numFailed);
                    progressUpdate((int) (i * perFileProg),
                        ("Cannot perform search in file: " +
                        f.getAbsolutePath()));
                    progressInterrupt("Illegal search pattern, exiting...");

                    break;
                } catch (QueryFormulationException qfe) {
                    numFailed++;
                    numInspected++;
                    report("Query formulation exception: " + qfe.getMessage() + 
                    		"\nin file: " + f.getAbsolutePath());
                    //finalReport(numInspected, numChanged, numFailed);
                    progressUpdate((int) (i * perFileProg),
                        ("Cannot perform search in file: " +
                        f.getAbsolutePath()));
                    //progressInterrupt("Wrong query formulation, exiting...");

                    //break;
                    // do not stop: a query formulation exception is thrown when (amongst
                    // other situations) a specified tier cannot not be found, i.e. a tier 
                    // is not present in a certain file 
                } catch (Exception ex) {
                    numFailed++;
                    numInspected++;
                    report("Exception while executing query: " +
                        ex.getMessage() + "\nin file: " + f.getAbsolutePath());
                    progressUpdate((int) (i * perFileProg),
                        ("Cannot perform search in file: " +
                        f.getAbsolutePath()));
                }
            }

            // report...
            report("Find and replace completed.");
            finalReport(numInspected, numChanged, numFailed);
            progressComplete("Operation completed, view report");
        }

        private void finalReport(int numIns, int numChan, int numFail) {
            report("\nSummary: ");
            report("Number of files in domain:  " + searchFiles.length);
            report("Number of files inspected:  " + numIns);
            report("Number of files changed:  " + numChan);
            report("Number of files failed:  " + numFail);
        }
    }
}
