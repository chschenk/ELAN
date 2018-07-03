package mpi.eudico.client.annotator.recognizer.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Parses tiers (segmentations) from (AVATecH project specific) XML files.
 * 
 * @author Han Sloetjes
 */
public class XmlTierIO implements ContentHandler {
	private final String TIERS = "TIERS";
	private final String TIER = "TIER";
	private final String SPAN = "span";
	private final String START = "start";
	private final String END = "end";
	private final String VAL = "v";
	
	private long bt = 0, et = 0;
	private String curContent = "";
	private List<String> vals;
	private HashMap<Integer, Segmentation> segmentations = null;
	private File xmlFile;
	
	//true if the file allows multiple TIER elements
	private boolean newXSDVersion = false;
	
	/**
	 * 
	 * @param xmlFile the XML tier file
	 */
	public XmlTierIO(File xmlFile) {
		super();
		this.xmlFile = xmlFile;
	}

	/**
	 * Parses the xml tier file and extracts segments per tier.
	 * 
	 * @return a list of Segmentation objects
	 */
	public List<Segmentation> parse() throws Exception {
		if (xmlFile == null || !xmlFile.exists() || !xmlFile.canRead() || xmlFile.isDirectory()) {
			return null;// or throw an IOException?
		}
		
    	FileInputStream fis = null;
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader(
					"org.apache.xerces.parsers.SAXParser");
			reader.setContentHandler(this);
			fis = new FileInputStream(xmlFile);
			InputSource source = new InputSource(fis);
			source.setSystemId(xmlFile.getPath());
			reader.parse(source);
			
			// return the segmentations
			if (segmentations != null && segmentations.size() > 0) {
				return new ArrayList<Segmentation>(segmentations.values());
			} else {
				throw new Exception("No tiers found in the TIER file");
			}
		} catch (IOException ioe) {
			//ClientLogger.LOG.warning("Parsing failed: " + ioe.getMessage());
			throw new Exception("Parsing failed: " + ioe.getMessage());
		} catch (SAXException sax) {
			//ClientLogger.LOG.warning("Parsing failed: " + sax.getMessage());
			throw new Exception("Parsing failed: " + sax.getMessage());
        } finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
}
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
		if(name.equals(TIERS)){
			newXSDVersion = true;
			segmentations = new HashMap<Integer, Segmentation>();
		}		
		if (name.equals(TIER)) {
			String cols = attributes.getValue("columns");
			if (cols != null && cols.length() > 0) {
				StringTokenizer tokenizer = new StringTokenizer(cols);
				int numTiers = tokenizer.countTokens();
				vals = new ArrayList<String>(numTiers);				
				String tok;
				int i = 0;
				if(!newXSDVersion){
					segmentations = new HashMap<Integer, Segmentation>(numTiers);
				} else{
					i = segmentations.size();
				}				
				
				while (tokenizer.hasMoreTokens()) {
					tok = tokenizer.nextToken();
					if (tok.charAt(0) == '#') {
						tok = tok.substring(1);
					}

					Segmentation segm = new Segmentation(tok, new ArrayList<RSelection>(), "");
					segm.getMediaDescriptors().clear();
					segmentations.put(i, segm);
					i++;
				}
			} else {
				throw new SAXException("No tiernames found, cannot create tiers.");
			}
		} else if (name.equals(SPAN)) {
			bt = parseTime(attributes.getValue(START));
			et = parseTime(attributes.getValue(END));
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
		} else if (name.equals(SPAN)) {
			if (bt > -1 && et > -1) {
				// create segments
				for (int i = 0; i < vals.size(); i++) {
					Segment segment = new Segment(bt, et, vals.get(i));
					int segIndex =  i;
					if(newXSDVersion){
						segIndex =  segmentations.size()-vals.size()+i;
					}
					
					if (segIndex < segmentations.size()  ) {
						segmentations.get(segIndex).getSegments().add(segment);
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
