package mpi.eudico.client.annotator.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import mpi.eudico.client.annotator.interannotator.AnnotatorCompareUtil2;
import mpi.eudico.client.annotator.interannotator.CompareCombi;

/**
 * A command that calculates some kind of agreement value based on the ratio of the 
 * amount of overlap of two annotation and the total extent of the two annotations when merged.
 */
public class CompareAnnotationRatioMultiCommand extends AbstractCompareCommand {

	/**
	 * Constructor.
	 * 
	 * @param theName
	 */
	public CompareAnnotationRatioMultiCommand(String theName) {
		super(theName);
	}

	/**
	 * Just calls the super implementation {@link AbstractCompareCommand#execute(Object, Object[])} 
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		super.execute(receiver, arguments);
	}

	/**
	 * Compares the segmentation of two tiers by calculating the quotient of the time overlap of
	 * two annotations and the total extent of the two (merged) annotations.
	 * This is similar to the Compare Annotators function that has been part of ELAN for some time,
	 * but here only the average for two tiers is calculated and stored (no overview containing all annotations).
	 * 
	 * @version October 2014
	 */
	@Override
	protected void calculateAgreement() {
		// in the super class checks have been performed and combinations of tiers have been
		// created. Start the calculations right away.
		if (compareSegments.size() == 0) {
			logErrorAndInterrupt("There are no tier pairs, nothing to calculate.");
			return;
		}

		int combiCount = 0;
		// starting at an arbitrary 30%
		float perCombi = 60f / compareSegments.size();
		AnnotatorCompareUtil2 compareUtil = new AnnotatorCompareUtil2();
		progressUpdate((int) curProgress, "Starting calculations...");
		for (CompareCombi cc : compareSegments) {
			double average = compareUtil.getAverageRatio(cc);
			cc.setOverallAgreement(average);
			combiCount++;
			
			curProgress += perCombi;
			progressUpdate((int) curProgress, null);
		}
		
		progressComplete(String.format("Completed calculations of %d pairs of tiers.", combiCount));
	}

	/**
	 * Writes the comparisons per tier combination to a file and outputs an average value at the end.
	 * 
	 * @param toFile the file to write to
	 * @param encoding the encoding for the file
	 * 
	 * @throws any IO related IOException 
	 */
	@Override
	public void writeResultsAsText(File toFile, String encoding)
			throws IOException {
        if (compareSegments == null) {
        	throw new NullPointerException("There are no results to save.");
        }
        
        if (toFile == null) {
        	throw new IOException("There is no file location specified.");
        }
		
        if (encoding == null) {
        	encoding = "UTF-8";
        }
        
		BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(toFile);
            OutputStreamWriter osw = null;

            try {
                osw = new OutputStreamWriter(out, encoding);
            } catch (UnsupportedCharsetException uce) {
                osw = new OutputStreamWriter(out, "UTF-8");
            }

            writer = new BufferedWriter(osw);
            // write BOM?
            final String NL = "\n";
            final String NL2 = "\n\n";
            DecimalFormat decFormat = new DecimalFormat("#0.0000",
                    new DecimalFormatSymbols(Locale.US));
            
            // write "header" date and time
            writer.write(String.format("Output created: %tD %<tT",  Calendar.getInstance()));
            writer.write("The overlap/extent value is the amount of overlap of two matching annotations divided by the total extent of those two annotations.");
            writer.write(NL);
            writer.write("Unmatched annotations (annotations without a counterpart on the other tier) add 1 to the number of comparisons, but 0 to the value.");
            writer.write(NL2);
            writer.write("Number of pairs of tiers in the comparison: " + compareSegments.size());
            writer.write(NL2);
            // write agreement output
            int totalCount = 0;
            double totalAgr = 0.0d;
            
            for (CompareCombi cc : compareSegments) {
        		// output the overall agreement
        		writer.write("File 1: " + cc.getFirstUnit().fileName + " Tier 1: " + cc.getFirstUnit().tierName);
        		writer.write(NL);
        		writer.write("Number of annotations 1: " + cc.getFirstUnit().annotations.size());
        		writer.write(NL);
        		writer.write("File 2: " + cc.getSecondUnit().fileName + " Tier 2: " + cc.getSecondUnit().tierName);
        		writer.write(NL);
        		writer.write("Number of annotations 2: " + cc.getSecondUnit().annotations.size());
        		writer.write(NL);
        		writer.write("Average overlap/extent ratio: " + decFormat.format(cc.getOverallAgreement()));
                writer.write(NL2);
            	
            	totalCount++;
            	totalAgr += cc.getOverallAgreement();
            }
            
            if (totalCount > 0) {
            	writer.write("Overall average overlap/extent ratio: " + decFormat.format(totalAgr / totalCount));
            } else {
            	writer.write("There is no overall average overlap/extent ratio avaialable: no tier combinations found.");
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
