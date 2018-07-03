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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import staccato.data.StaccatoData;
import staccato.data.StaccatoNomination;
import staccato.model.StaccatoAnalyzer;
import staccato.model.StaccatoListener;
import staccato.model.StaccatoListener.StaccatoEvent;

import mpi.eudico.client.annotator.interannotator.CompareCombi;
import mpi.eudico.client.annotator.interannotator.CompareConstants;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * A command that calculates a value indicating the Degree of Organization of the segmentation
 * of two tiers.   
 * The calculation is based on / performed by the Staccato software.
 * Reference:
 * Luecking, A., Ptock, S., & Bergmann, K. (2011). Staccato: Segmentation Agreement Calculator according to Thomann. 
 * In E. Efthimiou G. & Kouroupetroglou (Eds.), Proceedings of the 9th International Gesture Workshop: 
 * Gestures in Embodied Communication and Human-Computer Interaction (pp. 50-53).
 * 
 * Thomann, B.: Oberservation and judgment in psychology: Assessing agreement among markings of behavioral events. 
 * BRM 33(3), 339-248 (2001)
 * 
 * @author Han Sloetjes
 */
public class CompareAnnotationStaccatoCommand extends AbstractCompareCommand {
	// set some defaults
	private int monteCarloIterations = 1000;
	private int nominationsGranularity = 10;
	private double nullHypothesis = 0.05d;
	
	private Map<CompareCombi, StaccatoData> resultMap;

	/**
	 * Constructor.
	 * @param theName the name of the command
	 */
	public CompareAnnotationStaccatoCommand(String theName) {
		super(theName);
	}

	/**
	 * Most of the preparation is performed in the super class.
	 * 
	 * {@link AbstractCompareCommand#execute(Object, Object[])}
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		resultMap = new HashMap<CompareCombi, StaccatoData>();
		super.execute(receiver, arguments);
	}

	/**
	 * Based on the tier combinations created bin the super class based on user input,
	 * Staccato objects are produced and the StaccatoAnalyzer is called to do the 
	 * actual calculations.  
	 */
	@Override
	protected void calculateAgreement() {
		// in the super class checks have been performed and combinations of tiers have been
		// created. Start the calculations right away.
		if (compareSegments.size() == 0) {
			logErrorAndInterrupt("There are no tier pairs, nothing to calculate.");
			return;
		}
		// retrieve a few settings from the arguments (obtained from the multi step pane).
		// check for reasonable minimum and maximum values
		Object userObj = compareProperties.get(CompareConstants.MONTE_CARLO_SIM);
		if (userObj instanceof Integer) {
			monteCarloIterations = (Integer) userObj;
			if (monteCarloIterations < 10) {
				monteCarloIterations = 10;
			} else if (monteCarloIterations > 25000) {
				monteCarloIterations = 25000;
			}
		}
		userObj = compareProperties.get(CompareConstants.NUM_NOMINATIONS);
		if (userObj instanceof Integer) {
			nominationsGranularity = (Integer) userObj;
			if (nominationsGranularity < 5) {
				nominationsGranularity = 5;
			} else if (nominationsGranularity > 100) {
				nominationsGranularity = 100;//??
			}
		}
		userObj = compareProperties.get(CompareConstants.NULL_HYPOTHESIS);
		if (userObj instanceof Double) {
			nullHypothesis = (Double) userObj;
			if (nullHypothesis < 0.01) {
				nullHypothesis = 0.01;
			} else if (nullHypothesis > 1) { //??
				nullHypothesis = 1;
			}
		}
		
		int combiCount = 0;
		// starting at an arbitrary 30%
		float perCombi = 50f / compareSegments.size();
		progressUpdate((int) curProgress, "Calculating the degree of organization per tier pair...");
		
		for (CompareCombi cc : compareSegments) {
			// create a Vector of StaccatoNominations and add all annotations of the two tiers
			// see StaccatoController how to initialize a StaccatoAnalyzer and a StaccatoData object
			StaccatoData sd = createStaccatoData(cc);
			
			// add to the result map even if the processing failed
			resultMap.put(cc, sd);
			
			if(sd != null) {
				cc.setOverallAgreement(sd.getOg());
			}
			combiCount++;
			
			curProgress += perCombi;
			progressUpdate((int) curProgress, null);
		}
		
		progressComplete(String.format("Completed calculations of %d pairs of tiers.", combiCount));
	}
	
	/**
	 * Creates a StaccatoData object containing the StaccatoNominations (equivalent to annotations)
	 * and the parameters for the calculation (based on user input).
	 * 
	 * @param cc the combination of annotations of two tiers
	 * 
	 * @return a StaccatoData object
	 */
	private StaccatoData createStaccatoData(CompareCombi cc) {
		if (cc == null) {
			return null;
		}
		int idx = 0;
		long minT = Long.MAX_VALUE;// currently not used by Staccato
		long maxT = 0;
		Vector<StaccatoNomination> inputNominations = new Vector<StaccatoNomination>();
		
		for (AnnotationCore ac : cc.getFirstUnit().annotations) {
			// int start, int end, int id, String label, String annotator
			StaccatoNomination sn = new StaccatoNomination(
					(int) ac.getBeginTimeBoundary(), (int) ac.getEndTimeBoundary(), 
					idx++, ac.getValue(), cc.getFirstUnit().annotator);
			inputNominations.add(sn);
			// check minimum and maximum time values in the input data
			// in principle the first annotation will have the minimal time and the last
			// annotation the maximum time value, maybe this check is superfluous
			if (ac.getBeginTimeBoundary() < minT) {
				minT = ac.getBeginTimeBoundary();
			}
			if (ac.getEndTimeBoundary() > maxT) {
				maxT = ac.getEndTimeBoundary();
			}
		}
		
		for (AnnotationCore ac : cc.getSecondUnit().annotations) {
			// int start, int end, int id, String label, String annotator
			StaccatoNomination sn = new StaccatoNomination(
					(int) ac.getBeginTimeBoundary(), (int) ac.getEndTimeBoundary(), 
					idx++, ac.getValue(), cc.getSecondUnit().annotator);
			inputNominations.add(sn);
			// check minimum and maximum time values in the input data
			// see above
			if (ac.getBeginTimeBoundary() < minT) {
				minT = ac.getBeginTimeBoundary();
			}
			if (ac.getEndTimeBoundary() > maxT) {
				maxT = ac.getEndTimeBoundary();
			}
		}
		
		StaccatoData stacdata = new StaccatoData();
		stacdata.setMci(monteCarloIterations);
		stacdata.setNomL(nominationsGranularity);
		stacdata.setAlpha(nullHypothesis);
		stacdata.setMatL((int) maxT);
		stacdata.setNominations(inputNominations);// maybe not necessary?
		
		//long startTime = System.currentTimeMillis();
		//System.out.println("Start at: " + startTime);
		StaccatoAnalyzer analyzer = new StaccatoAnalyzer();
		StacProcListener spListener = new StacProcListener();
		analyzer.initNominations(inputNominations);
		analyzer.process(spListener, stacdata);
		// try catch
		try {
			analyzer.join();
		} catch (InterruptedException ie) {
			ClientLogger.LOG.severe("The processing of a pair of tiers failed: " + ie.getMessage());// TO DO add tier and or file name?			
			return null;
		}
		//System.out.println("Waited for results: " + (System.currentTimeMillis() - startTime));
		
		// check return status, get the results
		if (spListener.lastEvent == StaccatoEvent.FINISHED) {
			return analyzer.getResult();
		}

		return null;
	}

	/**
	 * Writes the results to a text file. Per tier pair some information of the files and the tiers is
	 * exported and the information provided by Staccato: Degree of Organization, Fields, NucleusNominations,
	 * Nuclei. An average DO is calculated at the end. 
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
            writer.write(NL2);
            writer.write("The Degree of Organization (DO) value as calculated by the Staccato software");
            writer.write(NL);
            writer.write("which is based on the Thomann algorithm.");
            // add references
            writer.write("The DO is calculated with two tiers as input.");
            writer.write(NL2);
            writer.write("Number of pairs of tiers in the comparison: " + compareSegments.size());
            writer.write(NL);
            writer.write("Settings for computation:");
            writer.write(NL);
            writer.write("\tNumber of Monte Carlo Simulations:\t" + monteCarloIterations);
            writer.write(NL);
            writer.write("\tNumber of Nomination slots:\t" + nominationsGranularity);
            writer.write(NL);
            writer.write("\tNull Hypothesis:\t" + nullHypothesis);
            writer.write(NL2);
            writer.write("Per tier combination output follows:");
            writer.write(NL);
    		writer.write("==========");
            writer.write(NL2);
            // write agreement output
            int totalCount = 0;
            double totalAgr = 0.0d;
            
            StaccatoData stacData;
            
            for (CompareCombi cc : compareSegments) {
        		// output the overall agreement
        		writer.write("File 1: " + cc.getFirstUnit().fileName + " Tier 1: " + cc.getFirstUnit().tierName);
        		writer.write(NL);
        		writer.write("Number of annotations 1: " + cc.getFirstUnit().annotations.size());
        		writer.write(NL);
        		writer.write("File 2: " + cc.getSecondUnit().fileName + " Tier 2: " + cc.getSecondUnit().tierName);
        		writer.write(NL);
        		writer.write("Number of annotations 2: " + cc.getSecondUnit().annotations.size());
        		stacData = resultMap.get(cc);
        		if (stacData != null) {
        			writer.write(NL);
            		writer.write(stacData.getResultString());
        		} else {
        			writer.write(NL);
            		writer.write("Degree of Organization: " + decFormat.format(cc.getOverallAgreement()));
            		writer.write(NL);
        		}
        		writer.write("==========");
                writer.write(NL2);
            	
            	totalCount++;
            	totalAgr += cc.getOverallAgreement();
            }
            
            if (totalCount > 0) {
            	writer.write("Overall average Degree of Organization: " + decFormat.format(totalAgr / totalCount));
            } else {
            	writer.write("There is no overall average Degree of Organization avaialable: no tier combinations found.");
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

	/**
	 * Listens to events generated by Staccato.
	 * 
	 * While running the event reports progress from 0 to 100, an error is -1,
	 * finished is 101. 
	 */
	private class StacProcListener implements StaccatoListener {
		int lastEvent;
		
		@Override
		public void reactOnStaccatoEvent(StaccatoEvent se) {
			lastEvent = se.getEvent();
			//System.out.println("Staccato event: " + se.getEvent());
		}
		
	}
}
