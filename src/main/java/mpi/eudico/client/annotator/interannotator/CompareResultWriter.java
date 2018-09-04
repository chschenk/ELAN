package mpi.eudico.client.annotator.interannotator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Writes the results of agreement calculations to a text file.
 * Note: maybe this class can be deleted if there is too little in common in the way different 
 * algorithms are applied and exported.
 */
public class CompareResultWriter {
    final String TAB = "\t";
    final String NL = "\n";
    final String NL2 = "\n\n";
    
	public CompareResultWriter() {
		super();
	}

	/**
	 * Writes the results of agreement calculations to file. 
	 * 
	 * @param resultList the list of tier pairs and the calculated agreement
	 * @param outputFile the file to write to
	 * @param encoding the preferred encoding, defaults to UTF-8
	 * 
	 * @throws IOException any io exception that can occur
	 */
	public void writeResults(List<CompareCombi> resultList, File outputFile, String encoding) throws IOException {
        if (resultList == null) {
        	throw new NullPointerException("There are no results to save.");
        }
        
        if (outputFile == null) {
        	throw new IOException("There is no file location specified.");
        }
		
        if (encoding == null) {
        	encoding = "UTF-8";
        }
        
		BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(outputFile);
            OutputStreamWriter osw = null;

            try {
                osw = new OutputStreamWriter(out, encoding);
            } catch (UnsupportedCharsetException uce) {
                osw = new OutputStreamWriter(out, "UTF-8");
            }

            writer = new BufferedWriter(osw);
            // write BOM?
            
            DecimalFormat decFormat = new DecimalFormat("#0.0000",
                    new DecimalFormatSymbols(Locale.US));
            
            // write "header" date and time
            writer.write(String.format("Output created: %tD %<tT",  Calendar.getInstance()));
            writer.write(NL2);
            writer.write("Number of pairs of tiers in the comparison: " + resultList.size());
            writer.write(NL2);
            // write agreement output
            int totalCount = 0;
            double totalAgr = 0.0d;
            
            for (CompareCombi cc : resultList) {
            	if (cc.getPerValueAgreement() == null || cc.getPerValueAgreement().size() == 0) {
            		// output the overall agreement
            		writer.write("File 1: " + cc.getFirstUnit().fileName + " Tier 1: " + cc.getFirstUnit().tierName);
            		writer.write(NL);
            		writer.write("Number of annotations 1: " + cc.getFirstUnit().annotations.size());
            		writer.write(NL);
            		writer.write("File 2: " + cc.getSecondUnit().fileName + " Tier 2: " + cc.getSecondUnit().tierName);
            		writer.write(NL);
            		writer.write("Number of annotations 2: " + cc.getSecondUnit().annotations.size());
            		writer.write(NL);
            		writer.write("Agreement: " + decFormat.format(cc.getOverallAgreement()));
                    writer.write(NL2);
            	} else {
            		
            	}
            	
            	totalCount++;
            	totalAgr += cc.getOverallAgreement();
            }
            
            if (totalCount > 0) {
            	writer.write("Average agreement: " + decFormat.format(totalAgr / totalCount));
            } else {
            	writer.write("There is no overall average agreement avaialable: no tier combinations found.");
            }
        } catch (Exception ex) {
            // FileNotFound, Security or UnsupportedEncoding exceptions
            throw new IOException("Cannot write to file: " + ex.getMessage());
        } finally {
        	if (writer != null) {
	        	try {
	        		writer.close();
	        	} catch (Throwable t){
	        		
	        	}
        	}
        }
	}
}
