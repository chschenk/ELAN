package mpi.eudico.client.annotator.recognizer.load;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import mpi.eudico.client.annotator.recognizer.data.FileParam;
import mpi.eudico.client.annotator.recognizer.data.NumParam;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.data.TextParam;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class RecognizerParser implements ContentHandler {
	private String recognizerName;
	private String description;
	private String implementor;
	private String recognizerType;// should be "direct", "local", "shared" ("remote")
	private boolean curOsSupported = false;
	private List<Param> paramList;
	private String helpFile;
	private String iconRef;

	/**
	 * @return the recognizerName
	 */
	public String getRecognizerName() {
		return recognizerName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the implementor
	 */
	public String getImplementor() {
		return implementor;
	}

	/**
	 * @return the recognizerType
	 */
	public String getRecognizerType() {
		return recognizerType;
	}

	/**
	 * @return the curOsSupported
	 */
	public boolean isCurOsSupported() {
		return curOsSupported;
	}

	/**
	 * @return the paramList
	 */
	public List<Param> getParamList() {
		return paramList;
	}

	/**
	 * @return the helpFile
	 */
	public String getHelpFile() {
		return helpFile;
	}

	/**
	 * @return the iconRef
	 */
	public String getIconRef() {
		return iconRef;
	}

	private InputStream inputStream;
	
	/**
	 * Creates a new parser for the specified input stream.
	 * TODO could throw some SAX exceptions when required information is not there?
	 *  
	 * @param inputStream the source as an input stream
	 */
	public RecognizerParser(InputStream inputStream) {
		super();
		this.inputStream = inputStream;
		paramList = new ArrayList<Param>(10);
	}
	
	/**
	 * Starts the parsing process.
	 * 
	 * @throws SAXException any SAX exception or wrapped IO exception
	 */
	public void parse() throws SAXException {
		if (inputStream != null) {
			try {
				XMLReader reader = XMLReaderFactory.createXMLReader(
		    		"org.apache.xerces.parsers.SAXParser");
				reader.setContentHandler(this);
				reader.parse(new InputSource(inputStream));
			} catch (IOException ioe) {
				throw new SAXException(ioe);
			}
		} else {
			throw new SAXException("No input stream specified");
		}
	}

	//############## ContentHandler methods ######################################
	String curContent = "";
	Param curParam;
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		curContent += new String(ch, start, length);
	}

	@Override
	public void startElement(String nameSpaceURI, String name,
            String rawName, Attributes attributes) throws SAXException {
		if (name.equals("recognizer")) {
			recognizerType = attributes.getValue("recognizerType");
			description = attributes.getValue("info");
			
			String os = System.getProperty("os.name").toLowerCase();
			if (os.indexOf("win") > -1) {
				implementor = attributes.getValue("runWin");
				curOsSupported = (implementor != null);
//				if (implementor == null) {
//					curOsSupported = false;
//					//hier... throw exception to stop parsing?
//				}
			} else if (os.indexOf("mac") > -1) {
				implementor = attributes.getValue("runMac");
				curOsSupported = (implementor != null);
//				if (implementor == null) {
//					curOsSupported = false;
//				}
			} else if (os.indexOf("linux") > -1) {
				implementor = attributes.getValue("runLinux");
				curOsSupported = (implementor != null);
//				if (implementor == null) {
//					curOsSupported = false;
//				}
			}
		} else if (name.equals("numparam")) {
			NumParam np = new NumParam();
			int minPrec = 1, maxPrec = 1;
			String value = attributes.getValue("min");
			if (value != null) {
				try {
					float min = Float.parseFloat(value);
					np.min = min;
					int index = value.indexOf(".");
					if (index > -1 && index < value.length() - 1) {
						minPrec = value.length() - index;
					}
				} catch (NumberFormatException nfe) {
					np.min = 0f;
				}
			}
			value = attributes.getValue("max");
			if (value != null) {
				try {
					float max = Float.parseFloat(value);
					np.max = max;
					int index = value.indexOf(".");
					if (index > -1 && index < value.length() - 1) {
						maxPrec = value.length() - index;
					}
				} catch (NumberFormatException nfe) {
					np.max = 100f;
				}
			}
			value = attributes.getValue("default");
			if (value != null) {
				try {
					float def = Float.parseFloat(value);
					np.def = def;
				} catch (NumberFormatException nfe) {
					np.def = 50f;
				}
			}
			value = attributes.getValue("info");
			if (value != null) {
				np.info = value;
			}// else??
			
			value = attributes.getValue("level");
			if (value != null) {
				np.level = value;
			}
			
			value = attributes.getValue("type");
			if (value != null) {
				np.type = value;
			}
			
			np.precision = minPrec > maxPrec ?  minPrec : maxPrec;
			if (np.max - np.min < 1) {
				np.precision++;
			}
			paramList.add(np);
			curParam = np;
		} else if (name.equals("textparam")) {
			TextParam tp = new TextParam();
			String value = attributes.getValue("default");
			if (value != null) {
				tp.defValue = value;
			}
			value = attributes.getValue("info");
			if (value != null) {
				tp.info = value;
			}
			value = attributes.getValue("level");
			if (value != null) {
				tp.level = value;
			}
			value = attributes.getValue("convoc");
			if (value != null) {
				StringTokenizer tokenizer = new StringTokenizer(value);
				List<String> cvList = new ArrayList<String>(tokenizer.countTokens());
				while (tokenizer.hasMoreTokens()) {
					cvList.add(tokenizer.nextToken());
				}
				tp.conVoc = cvList;
			}
			paramList.add(tp);
			curParam = tp;
		} else if (name.equals("input") || name.equals("output")) {
			FileParam fp = new FileParam();
			if (name.equals("input")) {
				fp.ioType = FileParam.IN;
			} else {
				fp.ioType = FileParam.OUT;
			}
			String value = attributes.getValue("type");
			if (value != null) {
				if (value.equals("audio")) {
					fp.contentType = FileParam.AUDIO;
				} else if (value.equals("video")) {
					fp.contentType = FileParam.VIDEO;
				} else if (value.equals("tier")) {
					fp.contentType = FileParam.TIER;
				} else if (value.equals("multitier")) {
					fp.contentType = FileParam.MULTITIER;
				} else if (value.equals("csvtier")) {
					fp.contentType = FileParam.CSV_TIER;
				} else if (value.equals("timeseries")) {
					fp.contentType = FileParam.TIMESERIES;
				} else if (value.equals("csvtimeseries")) {
					fp.contentType = FileParam.CSV_TS;
				} else if (value.equals("auxiliary")) {
					fp.contentType = FileParam.AUX;
				}
			}
			value = attributes.getValue("info");
			if (value != null) {
				fp.info = value;
			}
			value = attributes.getValue("level");
			if (value != null) {
				fp.level = value;
			}
			value = attributes.getValue("optional");
			if (value != null) {
				fp.optional = Boolean.valueOf(value);
			}
			value = attributes.getValue("mimetypes");
			if (value != null) {
				StringTokenizer tokenizer = new StringTokenizer(value);
				List<String> mts = new ArrayList<String>(tokenizer.countTokens());
				while (tokenizer.hasMoreTokens()) {
					mts.add(tokenizer.nextToken());
				}
				fp.mimeTypes = mts;
			}
			
			paramList.add(fp);
			curParam = fp;
		} else if(name.equals("documentation")) {
			iconRef = attributes.getValue("icon16");
		}
	}

	@Override
	public void endElement(String nameSpaceURI, String name, String rawName)
		throws SAXException {
		if (curContent != null && curContent.length() > 0) {
			if (name.equals("recognizer")) {
				recognizerName = curContent.trim();
			} else if (name.equals("numparam")) {
				((NumParam) curParam).id = curContent.trim();
			} else if (name.equals("textparam")) {
				((TextParam) curParam).id = curContent.trim();
			} else if (name.equals("input") || name.equals("output")) {
				((FileParam) curParam).id = curContent.trim();
			} else if(name.equals("documentation")){
				helpFile = curContent.trim();
			}
		}
		
		curContent = "";
	}
	
	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
	}

	@Override
	public void setDocumentLocator(Locator arg0) {
	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
	}

}
