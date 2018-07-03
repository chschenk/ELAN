package mpi.eudico.client.annotator.recognizer.io;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import mpi.eudico.client.annotator.timeseries.NonContinuousRateTSTrack;
import mpi.eudico.client.annotator.timeseries.TimeSeriesTrack;
import mpi.eudico.client.annotator.timeseries.TimeValue;
import mpi.eudico.client.annotator.timeseries.TimeValueStart;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.TimeFormatter;

/**
 * A class to read AVATecH's timeseries file in CSV format.
 * 
 * @author Han Sloetjes
 */
public class CsvTimeSeriesIO {
	private File csvFile;
	private final String SC = ";";
	private int numColumns = -1;
	
	/**
	 * Constructor
	 */
	public CsvTimeSeriesIO(File csvFile) {
		super();
		this.csvFile = csvFile;
	}
	
	/**
	 * Reads all columns and creates tracks.
	 *  
	 * @param csvFile the timeseries csv file in AVATecH format
	 * @return a list of tracks as read from the file
	 */
	public List<Object> getAllTracks() {
		if (numColumns == -1) {
			int nc = getNumTracks();
			if (nc <= 0) {
				return null;
			}
		}
		
		FileReader fileRead = null;

		try {
			fileRead = new FileReader(csvFile);
			BufferedReader bufRead = new BufferedReader(fileRead);
			String line = null;
			List<String> tracknames = new ArrayList<String>(numColumns);
			int lineNum = 0;
			String[] nextRow;
	        Pattern pat = Pattern.compile(SC);
	        TimeValue tv;
	        
	        float min = Float.MAX_VALUE;
	        float max = Float.MIN_VALUE;
	        List<List<TimeValue>> trackData = new ArrayList<List<TimeValue>>(numColumns);
	        List<float[]> ranges = new ArrayList<float[]>(numColumns);
	        boolean[] prevWasNaN = new boolean[numColumns];
	        Arrays.fill(prevWasNaN, false);
	        
	        for (int i = 0; i < numColumns; i++) {
	        	trackData.add(new ArrayList<TimeValue>(100));
	        	ranges.add(new float[]{min, max});
	        }

			while ((line = bufRead.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				nextRow = pat.split(line);
				
				if (lineNum == 0) {
					for (int i = 1; i < nextRow.length; i++) {
						tracknames.add(nextRow[i]);
					}
					lineNum++;
					continue;
				}
				// read columns, assumes the number of columns is consistent
				for (int i = 1; i < nextRow.length && i <= numColumns; i++) {
					tv = getTimeValue(nextRow[0], nextRow[i]);
					if (tv != null) {
						if (!Float.isNaN(tv.value)) {
							if (prevWasNaN[i - 1]) {
								tv = new TimeValueStart(tv.time, tv.value);
							}
							prevWasNaN[i - 1] = false;
							trackData.get(i - 1).add(tv);
							float[] mm = ranges.get(i - 1);
							if (tv.value < mm[0]) {
								mm[0] = tv.value;
							}
							if (tv.value > mm[1]) {
								mm[1] = tv.value;
							}
						} else {
							prevWasNaN[i - 1] = true;
						}
					}
				}				
			}
			
			try {
				bufRead.close();
			} catch (Exception ex) {
				
			}
			
			List<Object> tracks = new ArrayList<Object>(numColumns);
			
			for (int i = 0; i < trackData.size(); i++) {
				NonContinuousRateTSTrack tsTrack = new NonContinuousRateTSTrack(tracknames.get(i), "");
				tsTrack.setData(trackData.get(i));
				tsTrack.setDerivativeLevel(0);
				tsTrack.setSource(csvFile.getAbsolutePath());
				tsTrack.setType(TimeSeriesTrack.TIME_VALUE_LIST);
				tsTrack.setRange(ranges.get(i));
				tsTrack.setColor(Color.GREEN);
				tracks.add(tsTrack);
			}
			
			return tracks;
		} catch (FileNotFoundException fnfe) {
			ClientLogger.LOG.warning("File not found: " + fnfe.getMessage());
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("File could not be read: " + ioe.getMessage());
        } finally {
			try {
				if (fileRead != null) {
					fileRead.close();
				}
			} catch (IOException e) {
			}
		}
		return null;
	}

	/**
	 * 
	 * @return the number of tracks in the file (the number of columns - 1)
	 */
	public int getNumTracks() {
		if (numColumns > -1) {//make sure the header is read once
			return numColumns;
		}
		
		FileReader fileRead = null;
		
		try {
			fileRead = new FileReader(csvFile);
			BufferedReader bufRead = new BufferedReader(fileRead);
			String line = null;
			String[] nextRow;
	        Pattern pat = Pattern.compile(SC);
	        
			while ((line = bufRead.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				nextRow = pat.split(line);
				numColumns = nextRow.length - 1;
				// check first column name
				if (nextRow.length > 0 && !nextRow[0].equals("")) {
					ClientLogger.LOG.warning("First column name is not \"#timestamp\"");
				}
				break;
			}
			try {
				bufRead.close();
			} catch (Exception ex) {
				
			}
		} catch (FileNotFoundException fnfe) {
			ClientLogger.LOG.warning("File not found: " + fnfe.getMessage());
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("File could not be read: " + ioe.getMessage());
        } finally {
			try {
				if (fileRead != null) {
					fileRead.close();
				}
			} catch (IOException e) {
			}
		}
		
		if (numColumns > -1) {
			return numColumns;
		}
		return 0;
	}
	
	/**
	 * Returns the track in the column at the given index. 
	 * The first column in the file is the time column, the first track column is at index 1 
	 * 
	 * @param column the column index, 1 based
	 * @return a track object
	 */
	public Object getTrack(int column) {
		if (column < 1 || column > numColumns) {
			return null;
		}
		
		FileReader fileRead = null;

		try {
			fileRead = new FileReader(csvFile);
			BufferedReader bufRead = new BufferedReader(fileRead);
			String line = null;
			String trackname = null;
			int lineNum = 0;
			String[] nextRow;
	        Pattern pat = Pattern.compile(SC);
	        TimeValue tv;
	        boolean prevWasNaN = false;
	        float min = Float.MAX_VALUE;
	        float max = Float.MIN_VALUE;
	        List<TimeValue> values = new ArrayList<TimeValue>(100);
	        
			while ((line = bufRead.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				nextRow = pat.split(line);
				
				if (lineNum == 0) {
					trackname = nextRow[column];
					lineNum++;
					continue;
				}
				
				if (nextRow.length > column) {
					tv = getTimeValue(nextRow[0], nextRow[column]);
					
					if (tv != null) {						
						if (!Float.isNaN(tv.value)) {
							if (prevWasNaN) {
								tv = new TimeValueStart(tv.time, tv.value);
							}
							prevWasNaN = false;
							values.add(tv);
							if (tv.value < min) {
								min = tv.value;
							}
							if (tv.value > max) {
								max = tv.value;
							}
						} else {
							prevWasNaN = true;
						}
					} // else log error?
				} else {
					if (nextRow.length > 0) {
						tv = getTimeValue(nextRow[0], null);
						if (tv != null) {							
							if (!Float.isNaN(tv.value)) {
								if (prevWasNaN) {
									tv = new TimeValueStart(tv.time, tv.value);
								}
								prevWasNaN = false;
								values.add(tv);
								if (tv.value < min) {
									min = tv.value;
								}
								if (tv.value > max) {
									max = tv.value;
								}
							}
						}// else log error?
					}// else log error?
				}
			}
			
			try {
				bufRead.close();
			} catch (Exception ex) {
				
			}
			// create a track
			NonContinuousRateTSTrack tsTrack = new NonContinuousRateTSTrack(trackname, "");
			tsTrack.setData(values);
			tsTrack.setDerivativeLevel(0);
			tsTrack.setSource(csvFile.getAbsolutePath());
			tsTrack.setType(TimeSeriesTrack.TIME_VALUE_LIST);
			tsTrack.setRange(new float[]{min, max});
			tsTrack.setColor(Color.GREEN);
			
			return tsTrack;
		} catch (FileNotFoundException fnfe) {
			ClientLogger.LOG.warning("File not found: " + fnfe.getMessage());
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("File could not be read: " + ioe.getMessage());
        } finally {
			try {
				if (fileRead != null) {
					fileRead.close();
				}
			} catch (IOException e) {
			}
		}
		return null;
	}
	
    /**
     * Returns a TimeValue object holding the time in ms. as well as the value
     * of the data sample as a float.
     *
     * @param time the time String
     * @param value the numerical value String
     *
     * @return a TimeValue object or null
     */
    private TimeValue getTimeValue(String time, String value) {
        if (time == null) {
            return null;
        }

        long t = TimeFormatter.toMilliSeconds(time);
        float v = 0;

        if (value != null) {
        	try {
        		v = Float.parseFloat(value);
        	} catch (NumberFormatException nfe) {
        		v = Float.NaN;
        	}
        } else {
        	v = Float.NaN;
        }

        return new TimeValue(t, v);
    }
}
