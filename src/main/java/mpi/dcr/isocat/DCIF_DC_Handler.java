package mpi.dcr.isocat;

import mpi.dcr.DCSmall;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;


/**
 * Minimal parser handler for ISOCat's single Data Category DCIF stream.
 *
 * @author Han Sloetjes
 * @version 1.0
 * @author aarsom
 * @version 2.0
 */
public class DCIF_DC_Handler implements ContentHandler {
    private DCSmall curDC;
    private String curLang;
    private boolean inLanguage = false;
    private boolean recordContent = false;
    private String curContent;
    private List<Profile> curProfs = new ArrayList<Profile>();

    /**
     * The content of an element.
     *
     * @param ch chars
     * @param start
     * @param length
     *
     * @throws SAXException ex
     */
    @Override
	public void characters(char[] ch, int start, int length)
        throws SAXException {
        if (recordContent) {
        	if(curContent == null){
        		curContent = new String(ch, start, length);
        	} else {
        		curContent = curContent.concat(new String(ch, start, length));
        	}
        }
    }

    /**
     * Returns the resulting (small) Data Category object. Note: might be
     * extended in the future if more information is needed
     *
     * @return the DCSmall object
     */
    public DCSmall getDC() {
        return curDC;
    }

    /**
     * End of document
     *
     * @throws SAXException ex
     */
    @Override
	public void endDocument() throws SAXException {
    	if (curDC != null) {
    		curDC.setLoaded(true);
    	}
        curProfs.clear();
    }

    /**
     * Sets selected members of the DCSmall object.
     *
     * @param uri
     * @param localName
     * @param qName
     *
     * @throws SAXException ex
     */
    @Override
	public void endElement(String uri, String localName, String qName)
        throws SAXException {
        if (qName.equals(ISOCatConstants.DC)) {
            if (curProfs.size() > 0) {
                curDC.setProfiles(curProfs.toArray(new Profile[] {  }));
            }
        } else if (qName.equals(ISOCatConstants.ID)) {
            if (curDC != null) {
                curDC.setIdentifier(curContent);
            }
        } else if (qName.equals(ISOCatConstants.LANG)) {
            if (inLanguage && (curContent != null)) {
                curLang = curContent;
            }
        } else if (qName.equals(ISOCatConstants.LANG_SEC)) {
            inLanguage = false;
        } else if (qName.equals(ISOCatConstants.DEF) && curLang != null) {
        	if(curDC != null){
        		curDC.setDesc(curLang, curContent);
        	}
        } else if (qName.equals(ISOCatConstants.NAME) && curLang != null) {
            if (curDC != null) {
            	curDC.setName(curLang, curContent);
            }
        } else if (qName.equals(ISOCatConstants.PROF)) {
        	Profile pf = new Profile(null, curContent);
            if (!curProfs.contains(pf)) {
                curProfs.add(pf);
            }
        }

        recordContent = false;
        curContent = null;
    }

    /**
     * Stub
     *
     * @param prefix
     *
     * @throws SAXException
     */
    @Override
	public void endPrefixMapping(String prefix) throws SAXException {
    }

    /**
     * Stub
     *
     * @param ch
     * @param start
     * @param length
     *
     * @throws SAXException ex!
     */
    @Override
	public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
    }

    /**
     * Stub
     *
     * @param target !
     * @param data
     *
     * @throws SAXException ex
     */
    @Override
	public void processingInstruction(String target, String data)
        throws SAXException {
    }

    /**
     * Stub
     *
     * @param locator 
     */
    @Override
	public void setDocumentLocator(Locator locator) {
    }

    /**
     * Stub
     *
     * @param name
     *
     * @throws SAXException
     */
    @Override
	public void skippedEntity(String name) throws SAXException {
    }

    /**
     * Stub
     *
     * @throws SAXException
     */
    @Override
	public void startDocument() throws SAXException {
        curDC = null;
    }

    /**
     * Extracts selected attibutes and sets some flags.
     *
     * @param uri
     * @param localName
     * @param qName
     * @param atts
     *
     * @throws SAXException ex
     */
    @Override
	public void startElement(String uri, String localName, String qName,
        Attributes atts) throws SAXException {
        if (qName.equals(ISOCatConstants.DC)) {
            curDC = new DCSmall(atts.getValue(ISOCatConstants.PID), null);
        } else if (qName.equals(ISOCatConstants.PROF)) {
            recordContent = true;
        } else if (qName.equals(ISOCatConstants.LANG_SEC)) {
            inLanguage = true;
        } else if (qName.equals(ISOCatConstants.LANG)) {
            if (inLanguage) {
                recordContent = true;
            }
        } else if (qName.equals(ISOCatConstants.DEF)) {
            recordContent = true;
        } else if (qName.equals(ISOCatConstants.NAME)) {
            recordContent = true;
        } else if (qName.equals(ISOCatConstants.ID)) {
            recordContent = true;
        }

        // do something more with conceptual domain?
    }

    /**
     * Stub
     *
     * @param prefix
     * @param uri
     *
     * @throws SAXException
     */
    @Override
	public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
    }
}
