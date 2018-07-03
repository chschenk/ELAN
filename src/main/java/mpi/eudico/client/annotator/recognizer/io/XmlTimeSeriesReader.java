package mpi.eudico.client.annotator.recognizer.io;

import java.awt.Color;
import java.io.File;
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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Parses timeseries data from (AVATecH project specific) XML files.
 * 
 * @author Han Sloetjes
 */
public class XmlTimeSeriesReader implements ContentHandler {
	private final String TS = "TIMESERIES";
	private final String ITEM = "i";
	private final String TIME = "t";
	private final String VAL = "v";
	
	private int numColumns;
	private long bt = 0;
	private String curContent = "";
	private File xmlFile;
	
	private List<String> vals;
	private List<String> tracknames = new ArrayList<String>();

    TimeValue tv;
    
    float min = Float.MAX_VALUE;
    float max = Float.MIN_VALUE;
    private List<List<TimeValue>> trackData;
    private List<float[]> ranges;
    private boolean[] prevWasNaN;

	
	/**
	 * 
	 * @param xmlFile the XML tier file
	 */
	public XmlTimeSeriesReader(File xmlFile) {
		super();
		this.xmlFile = xmlFile;		
	}

	/**
	 * Parses the xml timeseries file and extracts non-continuous timeseries tracks.
	 * 
	 * @return a list of track (TimeSeriesTrack) objects
	 */
	public List<Object> parse() throws IOException, SAXException {
		if (xmlFile == null || !xmlFile.exists() || !xmlFile.canRead() || xmlFile.isDirectory()) {
			//return null;// or throw an IOException?
			throw new IOException("Cannot parse the TIMESERIES file.");
		}

		
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader(
	    		"org.apache.xerces.parsers.SAXParser");
			reader.setContentHandler(this);

			reader.parse(xmlFile.getAbsolutePath());
			
			// return the tracks
			if (trackData != null && trackData.size() >= 0) {
				List<Object> tracks = new ArrayList<Object>(numColumns);
				
				for (int i = 0; i < trackData.size(); i++) {
					NonContinuousRateTSTrack tsTrack = new NonContinuousRateTSTrack(tracknames.get(i), "");
					tsTrack.setData(trackData.get(i));
					tsTrack.setDerivativeLevel(0);
					tsTrack.setSource(xmlFile.getAbsolutePath());
					tsTrack.setType(TimeSeriesTrack.TIME_VALUE_LIST);
					tsTrack.setRange(ranges.get(i));
					tsTrack.setColor(Color.GREEN);
					tracks.add(tsTrack);
				}
				
				return tracks;
			} else {
				ClientLogger.LOG.warning("No tracks found in the TIMESERIES file");
			}
		} catch (IOException ioe) {
			ClientLogger.LOG.warning("Parsing failed: " + ioe.getMessage());
			throw ioe;
		} catch (SAXException sax) {
			ClientLogger.LOG.warning("Parsing failed: " + sax.getMessage());
			throw sax;
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
	
	/**
	 * Parses a string value to float. If this fails NaN is returned.
	 *  
	 * @param value the string value
	 * @return a float
	 */
	private float getValue(String value) {
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
        
        return v;
	}
	
	/**
	 * Initializes  some data lists and arrays, based on the number of "columns" attribute in the 
	 * document element TIMESERIES. 
	 */
	private void initDataColls() {
		vals = new ArrayList<String>(10);
		trackData = new ArrayList<List<TimeValue>>(numColumns);
		ranges = new ArrayList<float[]>(numColumns);
		prevWasNaN = new boolean[numColumns];
	    Arrays.fill(prevWasNaN, false);
	    
	    for (int i = 0; i < numColumns; i++) {
	    	trackData.add(new ArrayList<TimeValue>(100));
	    	ranges.add(new float[]{min, max});
	    }
	}
	
	// ################# ContentHandler methods ##############
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		curContent += new String(ch, start, length);
	}

	@Override
	public void startDocument() throws SAXException {
		
	}

	@Override
	public void startElement(String nameSpaceURI, String name,
            String rawName, Attributes attributes) throws SAXException {
		if (name.equals(TS)) {
			String cols = attributes.getValue("columns");
			if (cols != null && cols.length() > 0) {
				Pattern pat = Pattern.compile(" ");
				String[] columns = pat.split(cols);
				
				for (int i = 0; i < columns.length; i++) {
					tracknames.add(columns[i]);
				}
				numColumns = columns.length;
				initDataColls();
			} else {
				throw new SAXException("No timeseries columns found, cannot create timeseries tracks.");
			}
		} else if (name.equals(ITEM)) {
			bt = parseTime(attributes.getValue(TIME));
			vals.clear();
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		
	}

	@Override
	public void endElement(String nameSpaceURI, String name, String rawName)
			throws SAXException {
		if (name.equals(VAL)) {
			vals.add(curContent.trim());
			curContent = "";
		} else if (name.equals(ITEM)) {
			if (bt > -1) {
				// create time-value pairs
				TimeValue tv;
				for (int i = 0; i < vals.size() && i < numColumns; i++) {// the values correspond to the columns
					tv = new TimeValue(bt, getValue(vals.get(i)));
					if (!Float.isNaN(tv.value)) {
						if (prevWasNaN[i]) {
							tv = new TimeValueStart(tv.time, tv.value);
						}
						prevWasNaN[i] = false;
						trackData.get(i).add(tv);
						float[] mm = ranges.get(i);
						if (tv.value < mm[0]) {
							mm[0] = tv.value;
						}
						if (tv.value > mm[1]) {
							mm[1] = tv.value;
						}
					} else {
						prevWasNaN[i] = true;
					}
				}
			}
		}
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// method stub
	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// method stub
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// method stub
	}

	@Override
	public void setDocumentLocator(Locator arg0) {
		// method stub
	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// method stub
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// method stub
	}

}
