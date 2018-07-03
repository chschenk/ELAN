package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.Set;

import mpi.eudico.server.corpora.clomimpl.reflink.CrossRefLink.Directionality;

import org.w3c.dom.Element;

/**
 * EAF 3.0 extends the previous versions by adding:
 * - creation of Reference Links (cross references and grouping references). 
 * In EAF28 methods have already been added for creating these Reference Links,
 * as a preparation for EAF 3.0. Some additional attributes are now added here.
 * 
 * @see {@link EAFBase}
 * @see {@link EAF26}
 * @see {@link EAF27}
 * @see {@link EAF28}
 * @version EAF 3.0
 */
public class EAF30 extends EAF28{
	public static final String EAF30_SCHEMA_LOCATION = "http://www.mpi.nl/tools/elan/EAFv3.0.xsd";
	public static final String EAF30_SCHEMA_RESOURCE = "/mpi/eudico/resources/EAFv3.0.xsd";
   
    /**
	 * Constructor. Sets the Format variable to 2.8 and sets the schema location to the 
	 * location of the EAF 2.8 xsd.
	 */
    public EAF30() throws Exception {
        super();
    	EAF_Format = "3.0";
    	EAF_Schema_Location = EAF30.EAF30_SCHEMA_LOCATION;
    }
    
	/**
	 * Creates a new REF_LINK_SET element, a set containing reference links
	 *   
	 * @param linksID an ID for the new set
	 * @param linksName the name of the set
	 * @param extRefId a reference to an external object
	 * @param langRef a reference to a LANGUAGE element
	 * @param cvRef a reference to a controlled vocabulary
	 * 
	 * @return a new REF_LINK_SET element
	 */
	public Element newRefLinkSet(String linksID, String linksName, String extRefId,
			String langRef, String cvRef) {
		if (linksID == null || linksID.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		
		Element result = this.doc.createElement("REF_LINK_SET");
		result.setAttribute("LINK_SET_ID", linksID);// change in final version of 3.0 xsd
		attributeIfNotEmpty(result, "LINK_SET_NAME", linksName);
		attributeIfNotEmpty(result, "EXT_REF", extRefId);
		attributeIfNotEmpty(result, "LANG_REF", langRef);
		attributeIfNotEmpty(result, "CV_REF", cvRef);	
		
		return result;
	}
	
	/**
	 * Creates a new cross reference link between annotations, the link can 
	 * have several attributes. 
	 * Adds a REF_LINK_NAME attribute in addition to what EAF28 added.
	 * 
	 * @param id the ID of the reference link
	 * @param refName the name of the reference link 
	 * @param extRefId a reference to an external object
	 * @param langRef a reference to a LANGUAGE element
	 * @param cveRef a reference to a controlled vocabulary entry
	 * @param refType the type of reference link 
	 * @param content a textual content of the link (e.g. description)
	 * @param ref1 the reference to first object
	 * @param ref2 the reference to the second object
	 * @param dir an indication of the directionality of the link
	 * 
	 * @return a new CROSS_REF_LINK element
	 */
	public Element newCrossRefLink(String id, String refName, String extRefId, String langRef,
			String cveRef, String refType, String content,
			String ref1, String ref2, Directionality dir) {
		if (id == null || id.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (ref1 == null || ref1.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (ref2 == null || ref2.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		
		Element result = super.newCrossRefLink(id, extRefId, langRef, cveRef, 
				refType, content, ref1, ref2, dir);
		attributeIfNotEmpty(result, "REF_LINK_NAME", refName);
		
		return result;
	}

	/**
	 * Create a new grouping reference link.
	 * Adds a REF_LINK_NAME attribute in addition to what EAF28 added.
	 * 
	 * @param id the ID of the reference link
	 * @param refName the name of the reference link
	 * @param extRefId a reference to an external object
	 * @param langRef a reference to a LANGUAGE element
	 * @param cveRef a reference to a controlled vocabulary entry
	 * @param refType the type of reference link
	 * @param content a textual content of the link (e.g. description)
	 * @param refs a collection of references to elements that are part of this group
	 * 
	 * @return a new GROUP_REF_LINK
	 */
	public Element newGroupRefLink(String id, String refName, String extRefId, String langRef,
			String cveRef, String refType, String content, 
			Set<String> refs) {
		if (id == null || id.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (refs == null || refs.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}

		Element result = super.newGroupRefLink(id, extRefId, langRef, cveRef, 
				refType, content, refs);
		attributeIfNotEmpty(result, "REF_LINK_NAME", refName);
		
		return result;
	}
	
}
