package mpi.eudico.client.annotator.lexicon.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import mpi.eudico.client.annotator.lexicon.api.Param;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Class to parse the lexiconServiceClient.cmdi
 * @author Micha Hulsbosch
 *
 */
public class LexSrvcClntParser implements ContentHandler {

	
	private InputStream inputStream;
	public ArrayList<Param> paramList;
	public String type;
	public boolean curOsSupported;
	public String name;
	public String implementor;
	public String description;

	public LexSrvcClntParser(InputStream inputStream) {
		super();
		this.inputStream = inputStream;
		paramList = new ArrayList<Param>(10);
	}
	
	public void parse()  throws SAXException {
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
		if (name.equals("lexiconserviceclient")) {
			//type = attributes.getValue("type");
			description = attributes.getValue("info");
			implementor = attributes.getValue("spiClass");
			curOsSupported = true;
			/*
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
			*/
		} else if (name.equals("param")) {
			curParam = new Param();
			curParam.setType(attributes.getValue("type"));
		}
	}

	@Override
	public void endElement(String nameSpaceURI, String name, String rawName)
	throws SAXException {
		if (curContent != null && curContent.length() > 0) {
			if (name.equals("lexiconserviceclient")) {
				this.name = curContent.trim();
			} else if (name.equals("param")) {
				curParam.setContent(curContent.trim());
				paramList.add(curParam);
			}
		}
		
		curContent = "";
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDocumentLocator(Locator arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}

}
