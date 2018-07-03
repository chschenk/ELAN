package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ngramstats.NgramStatsResult;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * Exports the N-gram analysis result to a CSV file delimited by tabs (\t)
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class ExportNgramRawData extends AbstractBasicExportDialog {
	private static final long serialVersionUID = -8050510198504976188L;
	private NgramStatsResult result;

	// constants for exporting
	final static public String DELIM = "\t";
    final static private String NEWLINE = "\n";

	public ExportNgramRawData(Frame parent, boolean modal, Transcription transcription, NgramStatsResult res) {
		super(parent, modal, null);
		
		// store the result
		result = res;
		
		try {
            startExport();
        } catch (Exception ee) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                "(" + ee.getMessage() + ")",
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
        }
	}

	@Override
	protected boolean startExport() throws IOException {
		// prompt for file name and location
		File file = promptForFile(ElanLocale.getString("ExportTabDialog.Title"), null, FileExtension.TEXT_EXT, true);
        
		// did we get a file to export to?
		if (file == null) {
			return false;
		}
        
		// setup the writer
		FileOutputStream out = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(out, encoding);
		BufferedWriter writer = new BufferedWriter(osw);
		
		// write out the header
		writer.write("# RAW DATA Export of N-gram Analysis done on " + new Date().toString() + NEWLINE);
		writer.write("# Selected Domain: " + result.getDomain() + NEWLINE);
		writer.write("# Selected Tier: " + result.getTier() + NEWLINE);
		writer.write("# N-gram Size: " + result.getNgramSize() + NEWLINE);
		writer.write("# Search Time: " + result.getSearchTime() + "s" + NEWLINE);
		writer.write("# Files Inspected: " + result.getNumFiles() + NEWLINE);
		writer.write("# Total Annotations: " + result.getNumAnnotations() + NEWLINE);
		writer.write("# Total N-grams: " + result.getNumNgrams() + NEWLINE);
		writer.write("#" + NEWLINE);

		// write out the columns
		List<String> columns = result.getNgramAt(0).toCSVColumns();
		Iterator itr = columns.iterator();
		while ( itr.hasNext() ) {
			writer.write( (String)itr.next() );
			if ( ! itr.hasNext() ) {
				// last column :)
				writer.write( NEWLINE );
			} else {
				writer.write( DELIM );
			}
		}

		// write out the ngrams :)
		for (int i=0; i < result.getNumNgrams(); i++) {
			writer.write(result.getNgramAt(i).toCSV(DELIM) + NEWLINE);
		}
		writer.close();

		return true;
	}
}