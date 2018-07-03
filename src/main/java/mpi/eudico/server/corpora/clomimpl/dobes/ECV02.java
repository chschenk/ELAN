package mpi.eudico.server.corpora.clomimpl.dobes;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Creates an ECV document
 * 
 * @author Micha Hulsbosch, Olaf Seibert
 * @version january 2015
 */
public class ECV02 {

	public static final String ECV_SCHEMA_LOCATION = "http://www.mpi.nl/tools/elan/EAFv2.8.xsd";
	public static final String ECV_SCHEMA_RESOURCE = "/mpi/eudico/resources/EAFv2.8.xsd";

	protected Document doc;
	
    /**
     * Creates an empty document
     * 
     * @throws Exception
     */
    public ECV02() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder        db  = dbf.newDocumentBuilder();
		this.doc = db.newDocument();
    }

	/** see org.w3c.dom.Element.getDocumentElement() */
	public Element getDocumentElement() { return this.doc.getDocumentElement(); }

	/** see org.w3c.dom.Element.appendChild() */
	public Node appendChild(Node e) { return this.doc.appendChild(e); }

	/**
	 * Creates a new external CV document element
	 * 
	 * @param creationDate
	 * @param author
	 * @param version
	 * @return result (Element)
	 */
	public Element newExternalCVDocument
	(String creationDate, String author, String version) {
		// these attributes are all optional
		//if (creationDate == null) throw new RuntimeException("ECV");
		//if (author == null) throw new RuntimeException("ECV");
		//if (version == null) throw new RuntimeException("ECV");
		Element result = this.doc.createElement("CV_RESOURCE");
		result.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		result.setAttribute("xsi:noNamespaceSchemaLocation", ECV_SCHEMA_LOCATION);
		if (creationDate != null) {
			result.setAttribute("DATE",   creationDate);
		}
		if (author != null) {
			result.setAttribute("AUTHOR", author);
		}
		if (version != null) {
			result.setAttribute("VERSION", version);
		} else {
			result.setAttribute("VERSION", "0.2");// default
		}
		//result.setAttribute("FORMAT", "0.2");
		return result;
	}

	/**
	 * Creates a new CV element
	 * 
	 * @param conVocId
	 * @param description
	 * @return result (Element)
	 */
	public Element newControlledVocabulary (String conVocId) {
		if (conVocId == null) throw new RuntimeException("ECV");
		Element result = this.doc.createElement("CONTROLLED_VOCABULARY");
		result.setAttribute("CV_ID", conVocId);
		return result;
	}
	
	public Element newLanguage(String id, String def, String label) {
		Element result = this.doc.createElement("LANGUAGE");
		result.setAttribute("LANG_ID", id);
		result.setAttribute("LANG_DEF", def);
		result.setAttribute("LANG_LABEL", label);
		return result;
	}

	public Element newDescription(String languageId, String description) {
		Element result = this.doc.createElement("DESCRIPTION");
		result.setAttribute("LANG_REF", languageId);
		result.appendChild(doc.createTextNode(description));
		return result;
	}

	public Element newCVEntryValue(String langRef, String value, String description) {
		Element result = this.doc.createElement("CVE_VALUE");
		if (description == null) {
			description = "";
		}
		result.setAttribute("DESCRIPTION", description);
		result.setAttribute("LANG_REF", langRef);
		result.appendChild(doc.createTextNode(value));
		return result;
	}

	public Element newCVEntryML(String id, String extRefId) {
		Element result = this.doc.createElement("CV_ENTRY_ML");
		result.setAttribute("CVE_ID", id);
		if (extRefId != null && !extRefId.isEmpty()) {
			result.setAttribute("EXT_REF", extRefId);
		}
		return result;
	}
	
	/**
	 * Creates a new External Reference element
	 * 
	 * @param id
	 * @param type
	 * @param value
	 * @return result (Element)
	 */
	public Element newExternalReference(String id, String type, String value) {
    	if (id == null || type == null || value == null) throw new RuntimeException("ECV");
    	
    	Element result = this.doc.createElement("EXTERNAL_REF");
    	result.setAttribute("EXT_REF_ID", id);
    	result.setAttribute("TYPE", type);
    	result.setAttribute("VALUE", value);
    	
    	return result;
    }
}
