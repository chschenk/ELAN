package mpi.eudico.client.annotator.multiplefilesedit.scrub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.server.corpora.util.SimpleReport;


/**
 * A class that removes 
 * leading and/or trailing space characters and/or sequences of multiple characters,
 * leading, trailing or all new line characters,
 * leading, trailing or all tabs characters
 * from all annotations in one or more transcriptions.
 *
 * @author Han Sloetjes
 * @version 2.0, March 2010
 */
public class TranscriptionScrubber implements ClientLogger, ProcessReporter {
    private ArrayList<ProgressListener> listeners;
    private ProcessReport report;
    private boolean isCanceled = false;
    /** the space character! */
    public final char SP = '\u0020';
    /** the tab character */
    public final char TAB = '\t';
    /** the new line character */
    public final char NL = '\n';

    /**
     * Creates a new TranscriptionScrubber instance
     */
    public TranscriptionScrubber() {
        super();
    }

    /**
     * Iterates over the list of files scrubs the transcription and (if
     * anything changed)  saves the file. Overwrites.
     *
     * @param files the eaf files
     * @param filters the characters to remove and their positions (leading, trailing, all)
     */
    public void scrubAndSave(List<File> files, Map<Character, boolean[]> filters) {
        if ((files == null) || (files.size() == 0)) {
            LOG.info("No (valid) files supplied.");

            return;
        }

        new MultiFileScrubber(files, filters).start();
    }
    
    /**
     * Stops the scrub process.
     */
    public void interrupt() {
    	isCanceled = true;
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

    /**
     * Returns the report or null.
     * 
     * @return the report or null
     */
	@Override
	public ProcessReport getProcessReport() {
		return report;
	}

	/**
	 * Adds a message to the report.
	 * 
	 * @param message the message to add to the report.
	 */
	@Override
	public void report(String message) {
		if (report == null) {
			report = new SimpleReport();
		}
		report.append(message);
	}

	/**
	 * Sets the report object.
	 * 
	 * @param report the report object
	 */
	@Override
	public void setProcessReport(ProcessReport report) {
		this.report = report;
	}
	
    //###############################################
    /**
     * The actual scrubbing in a separate thread. Customization options could
     * be added, like save to a new file instead of overwriting, only scrub at
     * the end, also remove newline characters within annotations etc.
     *
     * @author Han Sloetjes
     * @version 1.0
     */
    class MultiFileScrubber extends Thread {
        private List<File> files;
        private Map<Character, boolean[]> filters;

        /**
         * Creates a new MultifileScrubber instance
         *
         * @param files the eaf files
         * @param filters the characters to remove and their positions (leading, trailing, all)
         */
        public MultiFileScrubber(List<File> files, Map<Character, boolean[]> filters) {
            super();
            this.files = files;
            this.filters = filters;
        }

        /**
         * Creates a new MultifileScrubber instance
         *
         * @param name the thread name
         * @param files the eaf files
         * @param filters the characters to remove and their positions (leading, trailing, all)
         */
        public MultiFileScrubber(String name, List<File> files, Map<Character, boolean[]> filters) {
            super(name);
            this.files = files;
            this.filters = filters;
        }

        /**
         * The actual clean up work
         */
        @Override
		public void run() {
            if ((files == null) || (files.size() == 0)) {
                LOG.info("No files supplied to the thread.");
                report("No files supplied to the thread.");
                return;
            }
            char[] chars;
            boolean[][] flags;
            
            // report which characters to remove
            if (filters != null) {
            	chars = new char[filters.size()];
            	flags = new boolean[chars.length][3];
            	int count = 0;
            	report("Characters to remove:");
            	Iterator<Character> keys = filters.keySet().iterator();
            	Character key;
            	boolean[] vals;
            	while (keys.hasNext()) {
            		key = keys.next();
            		chars[count] = key.charValue();
            		vals = filters.get(key);
            		switch (key.charValue()) {
            		case '\n':
            			report("Key: NEW_LINE");
            			break;
            		case '\t':
            			report("Key: TAB");
            			break;
            		case '\u0020':
            			report("Key: WHITESPACE");
            			break;
            			default:
            				report("Key: " + key.toString());
            		}
            		
            		report("\tleading " + vals[0] + "\ttrailing " + vals[1] + "\tall " + vals[2]);
            		flags[count] = vals;
            		count++;
            	}
            	report("\n");
            } else {
            	report("No characters to remove, stopping clean up.");
            	progressInterrupt("No characters to remove");
            	return;
            }
            
            int numFiles = files.size();
            report("Number of files to process: " + numFiles);

            TranscriptionStore eafTranscriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();
            File file;
            TranscriptionImpl trans;
            TierImpl tier;
            Annotation ann;
            String val;
            StringBuilder builder = null;
            char[] annChars;
            
            float perFile = 95 / (float) numFiles;
            // stats
            int numProcessed = 0;
            int numFailed = 0;
            int numChanged = 0;

            for (int i = 0; i < numFiles; i++) {
                file = files.get(i);

                if (file == null) {
                    LOG.warning("File is null (index = " + i + ")");
                    report("File is null (index = " + i + ")");
                    numFailed++;
                    continue;
                }

                try {
                    trans = new TranscriptionImpl(file.getAbsolutePath());
                    trans.setNotifying(false);

                    boolean changed = false;// this transcription
                    boolean annChanged = false;// this annotation
                    List<TierImpl> tiers = trans.getTiers();

                    for (int j = 0; j < tiers.size(); j++) {
                        tier = tiers.get(j);

                        if (tier == null) {
                            LOG.warning("Tier is null (index = " + j + ")");

                            continue;
                        }

                        List<AbstractAnnotation> annotations = tier.getAnnotations();

                        int numAnnos = tier.getNumberOfAnnotations();

                        for (int k = 0; k < numAnnos; k++) {
                            ann = annotations.get(k);
                            val = ann.getValue();
                            annChanged = false;

                            if ((val != null) && (val.length() > 0)) {
                            	boolean nonSpaceChanged = false;
                            	// start the processing, first replace \t and \n by spaces
                            	for (int z = 0; z < chars.length; z++) {
                            		if (chars[z] != SP) {
                            			// optimize; if all tab and/or newline flags are false, skip the copying
                            			if (!flags[z][0] && !flags[z][1] && !flags[z][2]) {
                            				continue;
                            			}
                                    	builder = new StringBuilder(val.length());
                                    	annChars = val.toCharArray();
                                    	char lastCopied = SP;
                                    	nonSpaceChanged = false;
                                    	
                            			if (flags[z][2]) {// remove all, replace if prev and next are not spaces
                            				for (int y = 0; y < annChars.length; y++) {
                            					if (annChars[y] != chars[z]) {
                            						builder.append(annChars[y]);
                            						lastCopied = annChars[y];
                            					} else {
                            						annChanged = true;
                            						nonSpaceChanged = true;
                            						// check prev, next
                            						if ((y > 0 && lastCopied != SP) && 
                            								(y < annChars.length - 1 && annChars[y + 1] != SP)) {
                                						builder.append(SP);
                                						lastCopied = SP;
                            						} // don't copy 
                            					}
                            				}
                            			} else { // remove begin and/or end
                            				int bi = 0;
                            				int ei = annChars.length - 1;
                            				if (flags[z][0]) {// remove leading
	                            				for (char annChar : annChars) {
	                            					if (annChar == chars[z]) {
	                            						bi++;
	                            					} else {
	                            						break;
	                            					}
	                            				}
                            				}
                            				if (flags[z][1]) {// remove trailing
                            					for (int y = annChars.length - 1; y >= 0; y--) {
                            						if (annChars[y] == chars[z]) {
                            							ei--;
                            						} else {
                            							break;
                            						}
                            					}
                            				}
                            				if (bi != 0 || ei != annChars.length - 1) {
                            					annChanged = true;
                            					nonSpaceChanged = true;
                            					builder.append(annChars, bi, (ei - bi + 1));
                            				}
                            			}
                            			if (nonSpaceChanged) {
                            				val = builder.toString();
                            			}
                            		}
                            		
                            	}
                            	char lastCopied = SP;
                            	boolean spaceChanged = false;
                            	// now check spaces
                            	for (int z = 0; z < chars.length; z++) {
                            		if (chars[z] == SP) {
                            			// optimize; if all space flags are false, skip the copying
                            			if (!flags[z][0] && !flags[z][1] && !flags[z][2]) {
                            				break;
                            			}
                                    	builder = new StringBuilder(val.length());
                                    	annChars = val.toCharArray();
                                    	spaceChanged = false;
                                    	
                                    	if (flags[z][2]) {// remove all, replace multiple spaces by one
                            				for (int y = 0; y < annChars.length; y++) {
                            					if (annChars[y] != SP) {
                            						builder.append(annChars[y]);
                            						lastCopied = annChars[y];
                            					} else {
                            						// check prev, next
                            						if ((y > 0 && lastCopied != SP) && 
                            								(y < annChars.length - 1 && annChars[y + 1] != SP)) {
                                						builder.append(SP);
                                						lastCopied = SP;
                            						} else {// don't copy
                            							spaceChanged = true;
                            							annChanged = true;
                            						}
                            					}
                            				}
                                    	} else {// remove leading and or trailing
                            				int bi = 0;
                            				int ei = annChars.length - 1;
                            				if (flags[z][0]) {// remove leading
	                            				for (char annChar : annChars) {
	                            					if (annChar == SP) {
	                            						bi++;
	                            					} else {
	                            						break;
	                            					}
	                            				}
                            				}
                            				if (flags[z][1]) {// remove trailing
                            					for (int y = annChars.length - 1; y >= 0; y--) {
                            						if (annChars[y] == SP) {
                            							ei--;
                            						} else {
                            							break;
                            						}
                            					}
                            				}
                            				if (bi != 0 || ei != annChars.length - 1) {
                            					annChanged = true;
                            					spaceChanged = true;
                            					builder.append(annChars, bi, (ei - bi + 1));
                            				}
                                    	}
                                    	if (spaceChanged) {
                                    		val = builder.toString();
                                    	}
                            		}
                            	}

                            }
                            if (annChanged) {
                            	changed = true;
                            	ann.setValue(val);
                            }
                        }// end annotations
                    }// end tiers

                    if (changed) {
                    	numChanged++;
        				int saveAsType = SaveAs27Preferences.saveAsType(trans);
                        // store
                        try {
                            eafTranscriptionStore.storeTranscription(trans,
                                null, trans.getTiers(), saveAsType);
                            LOG.info("Scrubbed and saved file: " +
                                file.getName());
                            report("Scrubbed and saved file: " +
                                    file.getName());
                        } catch (IOException ioe) {
                            LOG.severe(
                                "Error while saving eaf file! The file could have been damaged!");
                            report("Error while saving eaf file! The file could have been damaged: " + 
                            		file.getAbsolutePath());
                        }
                    }
                } catch (Exception ex) {
                	numFailed++;
                    // catch any exception that could occur and continue
                    LOG.warning("Could not handle file: " +
                        file.getAbsolutePath());
                    report("Could not handle file: " +
                            file.getAbsolutePath());
                }

                numProcessed++;
                progressUpdate((int) (i * perFile), null);//file.getName()

                if (isCanceled) {
                    LOG.warning("Scrubbing canceled after " + i + " files.");
                    report.append("Scrubbing canceled after " + i + " files.");
                    
                    progressInterrupt("Scrubbing interrupted");
                    break;
                }
            }
            
            report("Number of files processed: \t" + numProcessed);
            report("Number of files changed: \t" + numChanged);
            report("Number of files failed: \t" + numFailed);
            progressComplete("Scrubbing completed...");
        }
    }
}
