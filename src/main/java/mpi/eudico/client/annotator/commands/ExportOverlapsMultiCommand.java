package mpi.eudico.client.annotator.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.util.ProgressListener;

/**
 * A command that exports overlap (related) information for multiple files 
 * to a tab-delimited text file.
 * 
 * @author Han Sloetjes
 *
 */
public class ExportOverlapsMultiCommand implements Command {
	private String name;
	private ArrayList<ProgressListener> listeners;
	
	final String NL = "\n";
	final String TAB = "\t";
	
	/**
	 * Constructor.
	 * @param name
	 */
	public ExportOverlapsMultiCommand(String name) {
		this.name = name;
	}

	/**
	 * @param receiver null
	 * @param arguments 
	 * <ul><li>arg[0] = the files to process (List[String])</li>
	 * <li>arg[1] = the reference tier (String)</li>
	 * <li>arg[2] = the tiers to compare against the reference tier (List[String])</li>
	 * <li>arg[3] = the file path to write the result to (String) </li>
	 * <li>arg[4] = the encoding for the export file (String)</li></ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		List<String> fileList = (List<String>) arguments[0];
		if (fileList.size() == 0) {
			progressComplete("No files specified");
			return;
		}
		String refTier = (String) arguments[1];
		List<String> compTiers = (List<String>) arguments[2];
		String filePath = (String) arguments[3];
		String encoding = (String) arguments[4];
		
		if (encoding == null) {
			encoding = "UTF-8";
		}
		
		new ExportRunner(fileList, refTier, compTiers, filePath, encoding).start();
		
	}

	/**
	 * Returns the name.
	 */
	@Override
	public String getName() {
		return name;
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
                ((ProgressListener) listeners.get(i)).progressUpdated(this,
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
                ((ProgressListener) listeners.get(i)).progressCompleted(this,
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
                ((ProgressListener) listeners.get(i)).progressInterrupted(this,
                    message);
            }
        }
    }
    
    /**
     * A thread for the execution of the command.
     * @author Han Sloetjes
     *
     */
    private class ExportRunner extends Thread {
		private List<String> fileList;
		private String refTier;
		private List<String> compTiers;
		private String filePath;
		private String encoding;
		/**
		 * @param fileList
		 * @param refTier
		 * @param compTiers
		 * @param filePath
		 * @param encoding
		 */
		public ExportRunner(List<String> fileList, String refTier,
				List<String> compTiers, String filePath, String encoding) {
			super();
			this.fileList = fileList;
			this.refTier = refTier;
			this.compTiers = compTiers;
			this.filePath = filePath;
			this.encoding = encoding;
		}
		
		@Override
		public void run() {
			File outputFile = new File(filePath);
			try {
				BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputFile), encoding));
				// prepare table
				// output layout
				// bt > et > dur > refTier > ct overlap > ct samevalue > ct ov duration > ct number of overlaps > ct value(s) > b-b firstAfter > e - b firstAfter > e -e firstAfter
				outWriter.write("Begin time");
				outWriter.write(TAB);
				outWriter.write("End time");
				outWriter.write(TAB);
				outWriter.write("Duration");
				outWriter.write(TAB);
				outWriter.write(refTier);
				outWriter.write(TAB);
				for (int i = 0; i < compTiers.size(); i++) {
					String nm = compTiers.get(i);
					outWriter.write(nm + "-ov");
					outWriter.write(TAB);
					outWriter.write(nm + "-same");
					outWriter.write(TAB);
					outWriter.write(nm + "-ov-dur");
					outWriter.write(TAB);
					outWriter.write(nm + "-no-ann");
					outWriter.write(TAB);
					outWriter.write(nm + "-value");
					outWriter.write(TAB);
					outWriter.write(nm + "-bt-to-bt-After");
					outWriter.write(TAB);
					outWriter.write(nm + "-et-to-bt-After");
					outWriter.write(TAB);
					outWriter.write(nm + "-et-to-et-After");
					outWriter.write(TAB);
					// new columns 20-12-2011
					outWriter.write(nm + "-bt-After");
					outWriter.write(TAB);
					outWriter.write(nm + "-et-After");
					outWriter.write(TAB);
					outWriter.write(nm + "-value-After");
					outWriter.write(TAB);
				}
				outWriter.write(NL);
				String fileName;
				float perFile = 100f / fileList.size();
				for (int i = 0; i < fileList.size(); i++) {
					fileName = fileList.get(i);
					ExportOverlapsCommand com = new ExportOverlapsCommand("ExportOverlaps");
					com.execute(outWriter, new Object[]{fileName, refTier, compTiers});
					// errors cannot really be detected this way...
					progressUpdate((int) ((i + 1) * perFile), "Processed file " + fileName);
				}
				outWriter.flush();
				outWriter.close();
				progressComplete("Export complete.");
			} catch (FileNotFoundException fnfe) {
				progressInterrupt("Export failed: " + fnfe.getMessage());
			} catch (UnsupportedEncodingException uee) {
				progressInterrupt("Export failed: " + uee.getMessage());
			} catch (IOException ioe) {
				progressInterrupt("Export failed: " + ioe.getMessage());
			}
		}		
		
    }
}
