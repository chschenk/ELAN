package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.multiplefilesedit.statistics.StatisticsAnnotationsMF;
import mpi.eudico.client.annotator.multiplefilesedit.statistics.StatisticsCollectionMF;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;

/**
 * A class for creating simple statistics for multiple files;
 * number of tiers, numbers of annotations, minimal/maximal/total duration etc.
 * 
 */
public class MultipleFileStatisticsCommand implements Command, ProcessReporter {
    private String commandName;
    private ArrayList<ProgressListener> listeners;
    private String[] selectedFiles;
    private String[] selectedTiers;
    private List<String> selectedTierList;
    private boolean loadAll = false;
    private StatisticsCollectionMF statsColl;
    private ProcessReport report;
    private StatisticsThread statsThread;
    
	/**
	 * @param commandName
	 */
	public MultipleFileStatisticsCommand(String commandName) {
		super();
		this.commandName = commandName;
	}

	/**
	 * @param receiver null
	 * @param arguments the arguments: <ul><li>arg[0] = the files to search
     *        (String[])</li> <li>arg[1] = the tiers to search, in case of null or
     *        zero length all tiers will be searched (String[])</li>
     *        <li>arg[2] = flag to indicate whether all types, participants etc. 
     *        have to be processed or only those used by the selected tiers (Boolean)</li>
     *        <li>arg[3] = a statistics collection object (StatisticsCollectionMF)</li></ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		selectedFiles = (String[]) arguments[0];
		selectedTiers = (String[]) arguments[1];
		loadAll = ((Boolean) arguments[2]).booleanValue();
		statsColl = (StatisticsCollectionMF) arguments[3];
		
        if ((selectedFiles == null) || (selectedFiles.length == 0)) {
            report("No files provided for statistics calculation.");
            progressInterrupt("Illegal argument: no files provided");
            return;
        }

        report("Number of files to process:  " + selectedFiles.length);
        
        if (selectedTiers == null || selectedTiers.length == 0) {
        	loadAll = true;
        	selectedTierList = new ArrayList<String>(0);
        	report("Tiers to process: all tiers");
        } else {
        	selectedTierList = new ArrayList<String>(selectedTiers.length);
        	report("Tiers to process: ");
        	for (String nm : selectedTiers) {
        		report("\t" + nm);
        		selectedTierList.add(nm);
        	}      	
        }
        // check null on data object
        if (statsColl == null) {
            report("No statistics results object provided.");
            progressInterrupt("Illegal argument: statistics collection is null");
            return;
        }
        
        statsThread = new StatisticsThread(commandName);
        
        try {
        	statsThread.start();
        } catch(Exception exc) {
            report("An exception occurred while starting the statistics calculation process: " + exc.getMessage());
            progressInterrupt("An exception occurred: " + exc.getMessage());
        }
	}

	@Override
	public String getName() {
		return commandName;
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
    
//############################################################################
    private class StatisticsThread extends Thread {
    	
    	public StatisticsThread() {
    		super();
    	}
    	
    	public StatisticsThread(String name) {
    		super(name);
    	}
    	
    	@Override
		public void run() {
            float perFileProg = 100 / (float) selectedFiles.length;

            // a few counters
            int numInspected = 0;
            int numFailed = 0;
            
            String path;
            File f;
            TranscriptionImpl trans = null;
            TierImpl tier = null;
            AbstractAnnotation aa = null;
            
            for (int i = 0; i < selectedFiles.length; i++) {
            	path = selectedFiles[i];
            	f = new File(path);
            	numInspected++;
            	
                if (!f.exists() || f.isDirectory()) {
                    numFailed++;
                    report("Skipping file: " + f.getAbsolutePath());
                    progressUpdate((int) (i * perFileProg),
                        ("Skipping file: " + f.getAbsolutePath()));

                    continue;
                }
                
                // change path \ to / ?
                try {
                	trans = new TranscriptionImpl(path);
                	
                	// check tiers...
                	List<TierImpl> tiers = trans.getTiers();
                	List<Long> durations = null;
                	// following three only used if everything has to be loaded
                	List<String> typeNames = null;
//                	List<String> partNames = null;
//                	List<String> annotNames = null;
                	int numProcessedTiers = 0;
                	
                	if (loadAll) {
                		typeNames = new ArrayList<String>();
//                		partNames = new ArrayList<String>();
//                		annotNames = new ArrayList<String>();
                	}
                	
                	for (int j = 0; j < tiers.size(); j++) {
                		tier = tiers.get(j);
                		if (loadAll || selectedTierList.contains(tier.getName())) {
                        	long bt, et, curDur;
                        	long minDur = Long.MAX_VALUE; 
                        	long maxDur = 0, totalDur = 0;
                        	long latency = Long.MAX_VALUE;
                        	durations = new ArrayList<Long>();
                        	
                        	List<AbstractAnnotation> annotations = tier.getAnnotations();
                        	
                        	for (int k = 0; k < annotations.size(); k++) {
                        		aa = annotations.get(k);
                        		bt = aa.getBeginTimeBoundary();
                        		et = aa.getEndTimeBoundary();
                        		curDur = et - bt;
                        		if (curDur < minDur) {
                        			minDur = curDur;
                        		}
                        		if (curDur > maxDur) {
                        			maxDur = curDur;
                        		}
                        		if (bt < latency) {
                        			latency = bt;// normally the first annotations should have the lowest begin time
                        		}
                        		totalDur += curDur;
                        		durations.add(curDur);
                        	}
                        	
                        	statsColl.addTier(path, tier, annotations.size(), 
                        			minDur, maxDur, totalDur, latency, durations);
                        	StatisticsAnnotationsMF annMF = statsColl.getAnnotationStats(tier.getName());
                        	if (annMF == null) {
                        		annMF = new StatisticsAnnotationsMF(tier);
                        		statsColl.addAnnotations(path, tier.getName(), annMF);
                        	} else {
                        		annMF.addTier(tier);
                        	}
                        	
                        	if (loadAll) {
                        		String key = tier.getLinguisticType().getLinguisticTypeName();
                        		if (!typeNames.contains(key)) {
                        			typeNames.add(key);
                        		}
//                        		key = tier.getParticipant(); 
//                        		if (key != null && key.length() > 0) {
//                        			if (!partNames.contains(key)) {
//                        				partNames.add(key);
//                        			}
//                        		} else {
//                        			if (!partNames.contains(StatisticsCollectionMF.UNSPECIFIED)) {
//                        				partNames.add(StatisticsCollectionMF.UNSPECIFIED);
//                        			}
//                        		}
//                        		key = tier.getAnnotator();
//                        		if (key != null && key.length() > 0) {
//                        			if (!annotNames.contains(key)) {
//                        				annotNames.add(key);
//                        			}
//                        		} else {
//                        			if (!annotNames.contains(StatisticsCollectionMF.UNSPECIFIED)) {
//                        				annotations.contains(StatisticsCollectionMF.UNSPECIFIED);
//                        			}
//                        		}
                        	}
                        	numProcessedTiers++;
                		}
                	}
                	
                	// after processing tiers check if remaining unused types need to be added
                	if (loadAll) {
                		List<LinguisticType> types = trans.getLinguisticTypes();
                		LinguisticType lt;
                		for (int t = 0; t < types.size(); t++) {
                			lt = types.get(t);
                			if (!typeNames.contains(lt.getLinguisticTypeName())) {
                				statsColl.addEmptyLinguisticType(path, lt.getLinguisticTypeName());
                			}
                		}
                	}
                	report("Processed " + numProcessedTiers + " tiers from file: " + path);
                    progressUpdate((int) ((i + 1) * perFileProg - 1),
                            ("Processed file: " + path));
                } catch (Exception ex) { // any exception 
                    numFailed++;
                    // any exception 
                    report("Can not load file: " + f.getAbsolutePath());
                    report("Cause: " +
                        ((ex.getMessage() != null) ? ex.getMessage()
                                                   : "Unknown file loading or parsing error..."));
                    progressUpdate((int) ((i + 1) * perFileProg - 1),
                        ("Can not load file: " + f.getAbsolutePath()));

                    continue;
                }
            }
            
            report("Processing files completed: ");
            report("Number of files inspected:  " + numInspected);
            report("Number of files failed:  " + numFailed);
            progressComplete("Statistics completed.");
    	}
    }
}
