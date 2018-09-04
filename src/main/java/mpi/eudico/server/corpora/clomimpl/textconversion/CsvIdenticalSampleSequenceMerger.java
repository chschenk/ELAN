package mpi.eudico.server.corpora.clomimpl.textconversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextReader;
import mpi.eudico.util.TimeFormatter;

/**
 * A class that parses a csv/tab-delimited text file in which each row represents 
 * a sample (per column) with a single time stamp and creates a tab-delimited text file 
 * in which successive rows with the same value are merged into one row with a 
 * start time and an end time. The resulting text file should be suitable for import in ELAN.
 * 
 * Note: this is not (yet) a general purpose utility. It was developed to pre-process
 * specific eye-tracker data at the MPI.
 */
public class CsvIdenticalSampleSequenceMerger {
	private boolean fileCheckPerformed = false;
	private String[] columnsToProcess;
	private String inputFilePath;
	private File inputFile;
	private String outputFilePath;
	private File outputFile;
	private float msPerSample = -1;
	
	private final String TAB = "\t";
	private final String NL = "\n";

	public CsvIdenticalSampleSequenceMerger(String inputFilePath) {
		this(inputFilePath, null);
	}

	public CsvIdenticalSampleSequenceMerger(String inputFilePath, String[] columnsToProcess) {
		this.inputFilePath = inputFilePath;
		this.columnsToProcess = columnsToProcess;
	}
	
	public float getMsPerSample() {
		return msPerSample;
	}

	public void setMsPerSample(float msPerSample) {
		this.msPerSample = msPerSample;
	}
	
	public String getOutputPath() {
		return outputFilePath;
	}
	
	/**
	 * Checks the input file (if it exists and is readable) and creates 
	 * an output file if possible.
	 * @throws IOException if the input file does not exist or if the output file
	 * could not be created.
	 */
	public void checkFiles() throws IOException {
		fileCheckPerformed = true;
		if (inputFilePath == null) {
			throw new IOException("The input file path is null");
		}
		inputFile = new File(inputFilePath);
		if (!inputFile.exists()) {
			throw new IOException("The input file does not exist: " + inputFilePath);
		}
		if (inputFile.isDirectory()) {
			throw new IOException("The input file is a directory, this is not supported.");
		}
		if (!inputFile.canRead()) {
			throw new IOException("The input file can not be read (no permission).");
		}
		
		int lastDot = inputFilePath.lastIndexOf('.');
		if (lastDot > 1 && lastDot < inputFilePath.length() - 1) {
			outputFilePath = inputFilePath.substring(0, lastDot) + "_merged_rows" + inputFilePath.substring(lastDot);
		} else {
			inputFilePath = inputFilePath + "_merged_rows";
		}
		
		outputFile = new File(outputFilePath);
		if (!outputFile.createNewFile()) {// can throw IOException
			System.out.println("Output file already exists and will be overwritten.");
		}
		if (!outputFile.canWrite()) {
			throw new IOException("The output file can not be written to (no permission).");
		}
	}

	/**
	 * Starts the conversion process.
	 * @throws IOException if a file check has not been performed yet it is done here and now 
	 */
	public void processFile() throws IOException {
		if (!fileCheckPerformed) {
			checkFiles();
		}
		
		DelimitedTextReader deltReader = new DelimitedTextReader(inputFile);
		
		int numColumns = deltReader.getNumColumns();
		int[] columnsIndexes;
		int firstRow = 0;
		List<String> outputColumnNames = new ArrayList<String>();
		
		if (columnsToProcess != null) {
			List<String[]> headerLine = deltReader.getSamples(1);
			if (headerLine.isEmpty()) {
				throw new IOException("Could not read first row, the header line.");
			}
			String[] header = headerLine.get(0);
			List<Integer> incColumns = new ArrayList<Integer>();
			for (String colName : columnsToProcess) {
				for (int i = 0; i < header.length; i++) {
					if (colName.equals(header[i])) {
						incColumns.add(i);
						outputColumnNames.add(colName);
						break;
					}
				}
			}
			
			if (incColumns.isEmpty()) {
				throw new IOException("None of the specified columns were found in the header.");
			}
			// assume the first column is the sample time column, in case no msPerSample has been set
			if (msPerSample == -1 && !incColumns.contains(0)) {
				incColumns.add(0, 0);
			}
			
			columnsIndexes = new int[incColumns.size()];
			for (int i = 0; i < columnsIndexes.length; i++) {
				columnsIndexes[i] = incColumns.get(i);
			}
			firstRow = 1;
		} else {
			columnsIndexes = new int[numColumns];
			for (int i = 0; i < columnsIndexes.length; i++) {
				columnsIndexes[i] = i;
				outputColumnNames.add("T" + i);
			}
		}
		
		List<String[]> allLines = deltReader.getRowDataForColumns(firstRow, columnsIndexes);
		
		if (allLines.isEmpty()) {
			throw new IOException("No actual row data found");
		}
		// create a writer
        BufferedWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(outputFile);
            OutputStreamWriter osw = null;

            try {
                osw = new OutputStreamWriter(out, "UTF-8");
            } catch (UnsupportedCharsetException uce) {
                // 
            }

            writer = new BufferedWriter(osw);
            writeHeader(writer, outputColumnNames);
            // create two maps for each column to store current start time and current value (or use AnnotationCore or Pair<> 
            Map<Integer, MFloat> curTimeMap = null;
            Map<Integer, String> curValueMap = null;
            float curRowTime = 0f;
            boolean[] changedCols = new boolean[columnsIndexes.length];
            String lastSampleTime = "";
            
            for (String[] rowData : allLines) {
            	if (msPerSample < 0) {
	            	if (rowData[0].equals(lastSampleTime)) {
	            		// skip lines with the same sample time as the previous line
	            		continue;
	            	} else {
	            		lastSampleTime = rowData[0];
	            	}
            	}
            	if (curValueMap == null) {// first row
                    curTimeMap = new HashMap<Integer, MFloat>(columnsIndexes.length);
                    curValueMap = new HashMap<Integer, String>(columnsIndexes.length);
                    if (msPerSample < 0) {
                    	// extract time from first column
                    	curRowTime = getTime(rowData[0]);
                    } // else curRowTime starts at 0
                    for (int i = 0; i < columnsIndexes.length; i++) {
                    	curTimeMap.put(i, new MFloat(curRowTime));
                    }
                    int j = msPerSample < 0 ? 1 : 0; 
                    
                    for (int i = j; i < rowData.length; i++) {
                    	curValueMap.put(i - j, rowData[i]);
                    }
                    
                    continue;// don't write anything yet
            	}
            	// for other rows compare to stored values
            	if (msPerSample < 0) {
            		curRowTime = getTime(rowData[0]);
            	} else {
            		curRowTime += msPerSample;
            	}
            	Arrays.fill(changedCols, false);// reset
            	//boolean anyChange = false;
            	int j = msPerSample < 0 ? 1 : 0;
            	
            	for (int i = j; i < rowData.length; i++) {
            		// check values
            		if (!curValueMap.get(i - j).equals(rowData[i])){
            			//anyChange = true;
            			changedCols[i - j] = true;
            			// hmm if not immediately written both the old and the new values 
            			// will have to be stored etc.
            			long bt = Math.round(curTimeMap.get(i - j).floatValue);
            			long et = Math.round(curRowTime);
            			writeRow(writer, bt, et, curValueMap.get(i - j), changedCols);
            			
            			curValueMap.put(i - j, rowData[i]);
            			curTimeMap.get(i - j).floatValue = curRowTime;
            			changedCols[i - j] = false;
            		}
            	}
            	
            	if (rowData == allLines.get(allLines.size() - 1)) {
            		//write the last annotations
            		for (int i = j; i < rowData.length; i++) {
            			long bt = Math.round(curTimeMap.get(i - j).floatValue);
            			long et = Math.round(curRowTime);
            			if (bt != et) {
            				changedCols[i - j] = true;
            				writeRow(writer, bt, et, curValueMap.get(i - j), changedCols);
            				changedCols[i - j] = false;
            			}
            		}
            	}
            }// end of all lines iteration
            
        } catch (IOException ex) {
        	throw ex;
        } catch (Throwable t) {
            // FileNotFound, Security or UnsupportedEncoding exceptions
            throw new IOException("Cannot write to file: " + t.getMessage());
        } finally {
	        try {
	            if (writer != null) {
	            	writer.close();
	            }
	        } catch (IOException iioo) {
	            //iioo.printStackTrace();
	        }
        }
	}
	
	/**
	 * First tries to parse a float value from the String (which is assumed to be in ss.ms format).
	 * Rounding to a millisecond time value is done later.
	 * 
	 * @param floatFormatTime the string to parse
	 * @return a time as a float
	 */
	private float getTime(String floatFormatTime) {
		try {
			return 1000 * Float.parseFloat(floatFormatTime);
		} catch (NumberFormatException nfe) {
			return TimeFormatter.toMilliSeconds(floatFormatTime);
		}
	}
	
	/**
	 * Writes a header line.
	 * @param writer writer
	 * @param headers the column headers
	 * @throws IOException
	 */
	private void writeHeader(BufferedWriter writer, List<String> headers) throws IOException {
		writer.write("Start Time");
		writer.write(TAB);
		writer.write("End Time");
		for (String s : headers) {
			writer.write(TAB);
			writer.write(s);
		}
		writer.write(NL);
	}
	
	private void writeRow(BufferedWriter writer, long beginTime, long endTime, String rowCellData, boolean[] inColumn) throws IOException {
		writer.write(String.valueOf(beginTime));
		writer.write(TAB);
		writer.write(String.valueOf(endTime));
		writer.write(TAB);
		
		for (boolean b : inColumn) {
			if (b) {
				writer.write(rowCellData);
			}
			writer.write(TAB);
		}
		writer.write(NL);
	}
	
	/*
	 * A "mutable" Float object
	 */
	private class MFloat {
		public float floatValue;

		/**
		 * @param floatValue
		 */
		public MFloat(float floatValue) {
			super();
			this.floatValue = floatValue;
		}
		
	}
}
