package mpi.dcr.isocat;

import mpi.dcr.DCSmall;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal parser handler for ISOCat's Data Category Selection DCIF stream.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class DCIF_DCS_Handler implements ContentHandler {
    private DCSelection selection;
    private List<DCSmall> catList;
    private DCSmall curDC;
    private Profile curProfile = null;
    private boolean inProfile = false;
    private List<Profile> curProfs = new ArrayList<Profile>();

    /**
     * Extracts profiles names
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
        if (inProfile) {
            String p = new String(ch, start, length);
            Profile pp = new Profile(null, p);
            if (!curProfs.contains(pp)) {
                curProfs.add(pp);
            }
        }
    }

    /**
     * Returns the selection
     *
     * @return the list of data categories
     */
    public DCSelection getDCSelection() {
        return selection;
    }

    /**
     * Clears the profiles list
     *
     * @throws SAXException 
     */
    @Override
	public void endDocument() throws SAXException {
        curProfs.clear();
    }

    /**
     * Sets selected members of the current DCSmall object.
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
        if (qName.equals(ISOCatConstants.DC)) {
            if (curProfs.size() > 0) {
                if ((curProfile != null) && !curProfs.contains(curProfile)) {
                    curProfs.add(0, curProfile);
                }

                curDC.setProfiles(curProfs.toArray(new Profile[] {  }));
                curProfs.clear();
            }

            //curDC.setLoaded(true);
        } else if (qName.equals(ISOCatConstants.PROF)) {
            inProfile = false;
        }
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
     * Stub
     *
     * @throws SAXException 
     */
    @Override
	public void startDocument() throws SAXException {
        selection = new DCSelection();
        catList = new ArrayList<DCSmall>();
        selection.setDataCategories(catList);
    }

    /**
     * Extracts selected attibutes and sets some flags. 
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
        if (qName.equals(ISOCatConstants.DCS)) {
            String name = atts.getValue(ISOCatConstants.NAME_ATT);
            selection.setName(name);

            String profNameKey = "profile:";

            if ((name != null) && name.startsWith(profNameKey) &&
                    (name.length() > profNameKey.length())) {
                curProfile = new Profile(null, 
                		name.substring(profNameKey.length()).trim());
            }
        } else if (qName.equals(ISOCatConstants.DC)) {
            if (curProfile != null) {
                curDC = new DCSmall(curProfile, atts.getValue(ISOCatConstants.PID),
                        atts.getValue(ISOCatConstants.ID_ATT));
            } else {
                curDC = new DCSmall(atts.getValue(ISOCatConstants.PID), 
                		atts.getValue(ISOCatConstants.ID_ATT));
            }

            curDC.setName(atts.getValue(ISOCatConstants.NAME_ATT));
            curDC.setDesc(atts.getValue(ISOCatConstants.DEF_ATT));
            catList.add(curDC);
        } else if (qName.equals(ISOCatConstants.IS_A)) {
            DCSmall broader = null;

            if (curProfile != null) {
                broader = new DCSmall(curProfile, atts.getValue(ISOCatConstants.PID),
                        atts.getValue(ISOCatConstants.ID_ATT));
            } else {
                broader = new DCSmall(atts.getValue(ISOCatConstants.PID), 
                		atts.getValue(ISOCatConstants.ID_ATT));
            }

            broader.setName(atts.getValue(ISOCatConstants.NAME_ATT));
            broader.setDesc(atts.getValue(ISOCatConstants.DEF_ATT));
            curDC.setBroaderDC(broader);
            curDC.setBroaderDCId(broader.getId());
        } else if (qName.equals(ISOCatConstants.PROF)) {
            inProfile = true;
        }

        // do something with conceptual domain?
        // in the inconceptual domains of a DC element several profiles may be present
        // extract them and add them to the dc?
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
