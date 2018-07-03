package mpi.eudico.client.annotator.recognizer.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.StringTokenizer;
import java.util.regex.Pattern;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * Reads tiers (segmentations) from (AVATecH project specific) 
 * comma separated values files.
 * Note: writing of these files has been moved to RecTierWriter.
 * 
 * @author Han Sloetjes
 */
public class CsvTierIO {
	private final String SC = ";";
	
	/**
	 * Constructor.
	 */
	public CsvTierIO() {
		super();
	}
	
	/**
	 * Reads a csv tier file and extracts segments per tier.
	 * 
	 * @param csvFile the csv tier file
	 * @return a list of Segmentation objects
	 */
	public List<Segmentation> read(File csvFile) {
		if (csvFile == null || !csvFile.exists() || !csvFile.canRead() || csvFile.isDirectory()) {
			return null;// or throw an IOException?
		}
		BufferedReader bufRead = null;
		try {
			//FileReader fileRead = new FileReader(csvFile);
	        InputStreamReader fileRead = new InputStreamReader(new FileInputStream(
	        		csvFile), "UTF-8");
			bufRead = new BufferedReader(fileRead);
			String line = null;
			//StringTokenizer tokenizer;
			String[] nextRow;
	        Pattern pat = Pattern.compile(SC);
	        
			String tok;
			boolean headerRead = false;
			List<String> columnOrder = null;
			int numCols = 0;
			HashMap<String, Segmentation> segmentations = null;
			long bt = -1, et = -1;
			//  15 Apr 2010: specs changed first column always start, second always end?? 
			int btColumn = 0, etColumn = 1;
			Segment segment;
			
			while ((line = bufRead.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				
				if (!headerRead) {
					nextRow = pat.split(line);
					//tokenizer = new StringTokenizer(line, SC);
					// there should be at least 3 columns, otherwise stop parsing
					// comment lines are not supported??
					//if (tokenizer.countTokens() < 3) {
					if (nextRow.length < 3) {
						ClientLogger.LOG.warning("Too few columns in the file; there should at least be a begintime, endtime and one data column.");
						return null;
					}
					
//					boolean btFound = false;
//					boolean etFound = false;
					columnOrder = new ArrayList<String>(nextRow.length);
					segmentations = new HashMap<String, Segmentation>(nextRow.length - 2);
					
					//int i = 0;
					
					for (int i = 0; i < nextRow.length; i++) {
						tok = nextRow[i];
						if (tok.charAt(0) == '"') {
							tok = tok.substring(1);
						}
						if (tok.charAt(tok.length() - 1) == '"') {
							tok = tok.substring(0, tok.length() - 1);
						}
						
//						if (tok.startsWith("#start")) {// #start ?? #starttime
//							btFound = true;
//							btColumn = i;
//						} else if (tok.startsWith("#end")) {// #end ??  #endtime
//							etFound = true;
//							etColumn = i;
//						} else {
						if (i > 1) {
							if (tok.charAt(0) == '#') {
								tok = tok.substring(1);
							}
							
							Segmentation seg = new Segmentation(tok, new ArrayList<RSelection>(), "");
							segmentations.put(tok, seg);
						}
						columnOrder.add(tok);
						//i++;
					}
					
					//if (btFound && etFound) {
						headerRead = true;
						numCols = nextRow.length;
					//}
				} else {
					nextRow = pat.split(line);
					if (nextRow.length < 3) {
						continue;
					}

					// first extract the time values, could be skipped if the order was mandatory
					for (int i = 0; i < nextRow.length; i++ ) {
						tok = nextRow[i];
						if (i == btColumn) {
							bt = parseTime(tok);
						} else if (i == etColumn) {
							et = parseTime(tok);
						}
					}
					
					if (bt > -1 && et >-1) {						
						//tokenizer = new StringTokenizer(line, SC);
						//while (tokenizer.hasMoreTokens()) {
						for (int i = 0; i < nextRow.length; i++ ) {
							tok = nextRow[i];
	//						tok = tokenizer.nextToken();
							if (i != btColumn && i != etColumn) {
								segment = new Segment(bt, et, tok);
								segmentations.get(columnOrder.get(i)).getSegments().add(segment);
							}

							if (i > numCols) {
								break;
							}
						}
					}
				}
			}
			// return the segmentations
			if (segmentations != null) {
				List<Segmentation> segmentList = new ArrayList<Segmentation>(segmentations.values());
				for (Segmentation s : segmentList) {
					s.getMediaDescriptors().clear();
				}
				
				return segmentList;
			} else {
				return null;
			}
		} catch (FileNotFoundException fnfe) {
			ClientLogger.LOG.warning("CSV file not found: " + fnfe.getMessage());
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("Error while readring file: " + ioe.getMessage());
		} finally {
			try {
				if (bufRead != null) {
					bufRead.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	/**
	 * Parses a time value.
	 * 
	 * @param token a time value (in seconds) as a string
	 * @return a time value in milliseconds
	 */
	private long parseTime(String token) {
		if (token != null) {
			try {
				if (token.indexOf('.') > -1) {
					float val = Float.parseFloat(token);
					return (long) (1000 * val);
				} else {
					return Long.parseLong(token);// millisecond values
				}				
			} catch (NumberFormatException nfe) {
				return -1L;
			}
		}
		
		return -1L;
	}

}
