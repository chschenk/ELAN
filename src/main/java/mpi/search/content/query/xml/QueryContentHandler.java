package mpi.search.content.query.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.model.DependentConstraint;


/**
 * Created on Sep 30, 2004
 *
 * @author Alexander Klassmann
 * @version Sep 30, 2004
 */
public class QueryContentHandler implements ContentHandler {
    private final List<String> tierNames = new ArrayList<String>();
    private final ContentQuery query;
    private final Map<String, Constraint> constraintHash = new HashMap<String, Constraint>();
    private Constraint constraint;
    private String currentContent;

    protected QueryContentHandler(ContentQuery query) {
        this.query = query;
    }

    /**
     * DOCUMENT ME!
     *
     * @param locator
     */
    @Override
	public void setDocumentLocator(Locator locator) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param ch
     * @param start
     * @param end
     *
     * @exception SAXException
     */
    @Override
	public void characters(char[] ch, int start, int end)
        throws SAXException {
        currentContent += new String(ch, start, end);
    }

    /**
     * DOCUMENT ME!
     *
     * @exception SAXException
     */
    @Override
	public void endDocument() throws SAXException {
        System.out.println("End parsing query file.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param namespaceURI
     * @param elementName
     * @param rawName
     *
     * @exception SAXException
     */
    @Override
	public void endElement(String namespaceURI, String elementName,
        String rawName) throws SAXException {
        if (elementName.equals("tier")) {
            tierNames.add(currentContent);
        } else if (elementName.equals("pattern")) {
            constraint.setPattern(currentContent);
        } else if (elementName.equals("anchorConstraint") ||
                elementName.equals("dependentConstraint")) {
            constraint.setTierNames(tierNames.toArray(new String[0]));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param param1
     *
     * @exception SAXException
     */
    @Override
	public void endPrefixMapping(String param1) throws SAXException {
    }

    /**
     * DOCUMENT ME!
     *
     * @param param1
     * @param param2
     * @param param3
     *
     * @exception SAXException
     */
    @Override
	public void ignorableWhitespace(char[] param1, int param2, int param3)
        throws SAXException {
    }

    /**
     * DOCUMENT ME!
     *
     * @param param1
     * @param param2
     *
     * @exception SAXException
     */
    @Override
	public void processingInstruction(String param1, String param2)
        throws SAXException {
    }

    /**
     * DOCUMENT ME!
     *
     * @param param1
     *
     * @exception SAXException
     */
    @Override
	public void skippedEntity(String param1) throws SAXException {
    }

    /**
     * DOCUMENT ME!
     *
     * @exception SAXException
     */
    @Override
	public void startDocument() throws SAXException {
        System.out.println("Start parsing query file.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param namespaceURI
     * @param elementName
     * @param rawName
     * @param atts
     *
     * @exception SAXException
     */
    @Override
	public void startElement(String namespaceURI, String elementName,
        String rawName, Attributes atts) throws SAXException {
        if (elementName.equals("anchorConstraint") ||
                elementName.equals("dependentConstraint")) {
            if (elementName.equals("anchorConstraint")) {
                constraint = new AnchorConstraint();

                String s;
                s = atts.getValue("id");

                if (s == null) {
                    throw new SAXException("Id is null in element " +
                        elementName);
                }

                query.setAnchorConstraint((AnchorConstraint) constraint);
                constraintHash.put(s, constraint);
                tierNames.clear();
            } else if (elementName.equals("dependentConstraint")) {
                constraint = new DependentConstraint();

                String s;
                s = atts.getValue("id");

                if (s == null) {
                    throw new SAXException("Id is null in element " +
                        elementName);
                }

                constraintHash.put(s, constraint);
                tierNames.clear();

                s = atts.getValue("mode");

                if (s != null) {
                    ((DependentConstraint) constraint).setMode(s);
                }

                s = atts.getValue("quantifier");

                if (s != null) {
                    ((DependentConstraint) constraint).setQuantifier(s);
                }

                s = atts.getValue("id_ref");

                Constraint parent = constraintHash.get(s);
                parent.insert(constraint, parent.getChildCount());
            }

            String s;
            s = atts.getValue("regularExpression");

            if (s != null) {
                constraint.setRegEx(Boolean.valueOf(s).booleanValue());
            }

            s = atts.getValue("caseSensitive");

            if (s != null) {
                constraint.setCaseSensitive(Boolean.valueOf(s).booleanValue());
            }

            s = atts.getValue("unit");

            if (s != null) {
                constraint.setUnit(s);
            }

            s = atts.getValue("from");

            if (s != null) {
                try {
                    constraint.setLowerBoundary(Long.parseLong(s));
                } catch (NumberFormatException ne) {
                    if (!ne.getMessage().equals("null")) {
                        System.out.println(ne.getMessage());
                    }
                }
            }

            s = atts.getValue("to");

            if (s != null) {
                try {
                    constraint.setUpperBoundary(Long.parseLong(s));
                } catch (NumberFormatException ne) {
                    if (!ne.getMessage().equals("null")) {
                        System.out.println(ne.getMessage());
                    }
                }
            }
        }

        currentContent = "";
    }

    /**
     * DOCUMENT ME!
     *
     * @param param1
     * @param param2
     *
     * @exception SAXException
     */
    @Override
	public void startPrefixMapping(String param1, String param2)
        throws SAXException {
    }
}
