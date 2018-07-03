package mpi.dcr.isocat;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.ArrayList;

/**
 * The workspace parser handler, extracts profiles from the workspace
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class WSHandler implements ContentHandler {
    private ArrayList<Profile> profiles = new ArrayList<Profile>(12);
    private final String PROF = "cat:profile";

    /**
     * Returns a list with Profile objects, containing name and id
     *
     * @return a list with Profile objects
     */
    public ArrayList<Profile> getProfiles() {
        return new ArrayList<Profile>(profiles);
    }

    /**
     * Stub
     *
     * @param ch 
     * @param start 
     * @param length 
     *
     * @throws SAXException 
     */
    @Override
	public void characters(char[] ch, int start, int length)
        throws SAXException {
    }

    /**
     * Stub
     *
     * @throws SAXException 
     */
    @Override
	public void endDocument() throws SAXException {
    }

    /**
     * Stub
     *
     * @param uri 
     * @param localName 
     * @param qName 
     *
     * @throws SAXException 
     */
    @Override
	public void endElement(String uri, String localName, String qName)
        throws SAXException {
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
     * @throws SAXException 
     */
    @Override
	public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
    }

    /**
     * Stub
     *
     * @param target 
     * @param data 
     *
     * @throws SAXException 
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
     * Clear the current profiles map
     *
     * @throws SAXException 
     */
    @Override
	public void startDocument() throws SAXException {
        profiles.clear();
    }

    /**
     * Extracts id and name from a profile element
     *
     * @param uri 
     * @param localName 
     * @param qName 
     * @param atts 
     *
     * @throws SAXException 
     */
    @Override
	public void startElement(String uri, String localName, String qName,
        Attributes atts) throws SAXException {
        if (qName.equals(PROF)) {
        	profiles.add(new Profile(atts.getValue("id"), atts.getValue("name")));
        }
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
