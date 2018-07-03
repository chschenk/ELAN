package mpi.eudico.webserviceclient.weblicht;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.util.ServerLogger;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.*;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A parser for tcb content.
 * 
 * @author Han Sloetjes
 */
public class TCFParser implements ContentHandler {
	private XMLReader reader;
	// content
	private String inputContent;
	
	// content handler
	private StringBuilder content = new StringBuilder();
	
	private TCFElement curElement;
	private TCFType curType;
	private String text;
	private Map<TCFType, List<TCFElement>> baseElements;
	
	/**
	 * Constructor
	 */
	public TCFParser(String inputContent) {
		this.inputContent = inputContent;
	}
	
	/**
	 * Returns the parsed text.
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Returns the list of elements of the specific type.
	 * @return the list of tcb elements or null
	 */
	public List<TCFElement> getElementsByType(TCFType type) {
		return baseElements.get(type);
	}
	
	/**
	 * Parses the contents of the string.
	 * 
	 * @throws SAXException
	 * @throws IOException
	 */
	public void parse() throws SAXException, IOException {
		if (inputContent == null) {
			return;// throw exception
		}
		baseElements = new HashMap<TCFType, List<TCFElement>>();
		baseElements.put(TCFType.SENTENCE, new ArrayList<TCFElement>());
		baseElements.put(TCFType.TOKEN, new ArrayList<TCFElement>());
		baseElements.put(TCFType.TAG, new ArrayList<TCFElement>());
		baseElements.put(TCFType.LEMMA, new ArrayList<TCFElement>());
		
        try {
            reader = XMLReaderFactory.createXMLReader(
                    "org.apache.xerces.parsers.SAXParser");
            //reader.setFeature("http://xml.org/sax/features/namespaces", true);
            //reader.setFeature("http://xml.org/sax/features/validation", true);

            reader.setContentHandler(this);
            reader.parse(new InputSource(new StringReader(inputContent)));
        } catch (SAXException se) {
        	//se.printStackTrace();
        	ServerLogger.LOG.warning("Parser exception: " + se.getMessage());
        	throw se;
        } catch (IOException ioe) {
        	//ioe.printStackTrace();
        	ServerLogger.LOG.warning("IO exception: " + ioe.getMessage());
        	throw ioe;
        }
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		//System.out.println("Parse: " + uri + " - " + localName + " - " + name);
		// method stub
		if (SENT.equals(localName)) {
			baseElements.get(TCFType.SENTENCE).add(new TCFElement(atts.getValue(ID), atts.getValue(TOKEN_IDS), null));
			curType = TCFType.SENTENCE;
		} else if (TOKEN.equals(localName)) {
			curElement = new TCFElement(atts.getValue(ID), null, null);
			baseElements.get(TCFType.TOKEN).add(curElement);
			curType = TCFType.TOKEN;
		} else if (TAG.equals(localName)) {
			curElement = new TCFElement(atts.getValue(ID), atts.getValue(TOKEN_IDS), null);
			baseElements.get(TCFType.TAG).add(curElement);
		} else if (POSTAGS.equals(localName)) {
			curType = TCFType.POS_TAG;
		} else if (LEMMA.equals(localName)) {
			curElement = new TCFElement(null, atts.getValue(TOKEN_IDS), null);
			baseElements.get(TCFType.LEMMA).add(curElement);
			curType = TCFType.LEMMA;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		content.append(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (TOKEN.equals(localName) || TAG.equals(localName) || LEMMA.equals(localName)) {
			curElement.setText(content.toString().trim());
		} else if (TEXT.equals(localName)) {
			text = content.toString().trim();
		}
		// reset the content
		content.delete(0, content.length());
	}

	@Override
	public void endDocument() throws SAXException {
		// method stub
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// method stub

	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// method stub
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// method stub
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// method stub
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// method stub
	}

	@Override
	public void startDocument() throws SAXException {
		// method stub
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// method stub
	}

}
