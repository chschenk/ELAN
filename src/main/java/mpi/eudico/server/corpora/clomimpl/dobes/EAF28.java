package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.Locale;
import java.util.Set;

import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import mpi.eudico.server.corpora.clomimpl.reflink.CrossRefLink.Directionality;

import org.w3c.dom.Element;

/**
 * EAF 2.8 extends the previous versions by adding:
 * - adding an External Reference attribute to tiers //??
 * - adding a Language reference attribute to tiers and annotations
 * - adding a LANGUAGE element
 * - adding a DESCRIPTION element for Controlled Vocabularies
 * - adding support for multi-lingual controlled vocabularies which means a change in the structure of 
 * CV entry and adds a new CVE Value element
 * - adding a License element
 * 
 * As a preparation for EAF 3.0 methods have been added for creating Reference Links (cross references
 * and grouping references). (Even though usage in combination with the 2.8 schema results in 
 * invalid xml.) 
 * 
 * @see {@link EAFBase}
 * @see {@link EAF26}
 * @see {@link EAF27}
 * @version EAF 2.8
 */
public class EAF28 extends EAF27{
	public static final String EAF28_SCHEMA_LOCATION = "http://www.mpi.nl/tools/elan/EAFv2.8.xsd";
	public static final String EAF28_SCHEMA_RESOURCE = "/mpi/eudico/resources/EAFv2.8.xsd";
   
    /**
	 * Constructor. Sets the Format variable to 2.8 and sets the schema location to the 
	 * location of the EAF 2.8 xsd.
	 */
    public EAF28() throws Exception {
        super();
    	EAF_Format = "2.8";
    	EAF_Schema_Location = EAF28.EAF28_SCHEMA_LOCATION;
    }
    
	/**
	 * Adds a Language reference attribute to a tier with the structure as in EAFBase.
	 * @see {@link EAFBase#newTier(String, String, String, String, Locale, String)}
	 * @param extRef an EXT_REF attribute  //?? TODO check this out, what is this used for
	 * @param langRef LANG_REF attribute 
	 * 
	 * @return a new TIER element 
	 */
	public Element newTier
		(String id,
		 String participant,
		 String annotator,
		 String typeRef,
		 Locale language,
		 String parent,
		 String extRef,
		 String langRef) {
		Element result = super.newTier(id, participant, annotator, 
				typeRef, language, parent);
		attributeIfNotEmpty(result, "EXT_REF", extRef);
		attributeIfNotEmpty(result, "LANG_REF", langRef);
		return result;
    }
	
	/**
	 * Adds a reference attribute to an entry in a CV, based on the structure 
	 * of ALIGNABLE_ANNOTATION in eaf 2.6
	 * 
	 * @see {@link EAF26#newAlignableAnnotation(String, String, String)}
	 * @param cveId a reference to an entry in a multi-language controlled vocabulary
	 * 
	 * @return a new ALIGNABLE_ANNOTATION element
	 */
	public Element newAlignableAnnotation
		(String id,
		 String beginTimeSlot,
		 String endTimeSlot,
		 String extRefId,
		 String cveId) {
		Element result = super.newAlignableAnnotation(id, beginTimeSlot, 
				endTimeSlot, extRefId);
		attributeIfNotEmpty(result, "CVE_REF", cveId);
		return result;
    }
	
	/**
	 * Adds a reference attribute to an entry in a CV, based on the structure 
	 * of ALIGNABLE_ANNOTATION in eaf 2.6
	 * 
	 * @see {@link EAF26#newRefAnnotation(String, String, String)}
	 * @param cveId a reference to an entry in a multi-language controlled vocabulary
	 * 
	 * @return a new REF_ANNOTATION element
	 */
	public Element newRefAnnotation
		(String id,
		 String annotationRef,
		 String previousAnnotation,
		 String extRefId,
		 String cveId) {
		Element result = super.newRefAnnotation(id, annotationRef, 
				previousAnnotation, extRefId);
		attributeIfNotEmpty(result, "CVE_REF", cveId);
		return result;
    }

	/**
	 * Creates a new LANGUAGE element.
	 * 
	 * @param id the ID of the new element, a 3 letter ISO 639-3 language code
	 * @param def the language definition, is expected to be a  
	 * URL (e.g. http://cdb.iso.org/lg/CDB-00130975-001) in case of an ISO 639-3 language
	 * @param label the full name of the language 
	 * 
	 * @return a new LANGUAGE element
	 */
	public Element newLanguage(String id, String def, String label) {
		if (id == null || id.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		Element result = this.doc.createElement("LANGUAGE");
		result.setAttribute("LANG_ID", id);
		attributeIfNotEmpty(result, "LANG_DEF", def);
		attributeIfNotEmpty(result, "LANG_LABEL", label);
//		result.setAttribute("LANG_DEF", def);
//		result.setAttribute("LANG_LABEL", label);
		return result;
	}

	/**
	 * Creates a new DESCRIPTION element
	 * 
	 * @param languageId a reference to a LANGUAGE element
	 * @param description a description of the Controlled Vocabulary this
	 * element is added to
	 * 
	 * @return a new DESCRIPTION element
	 */
	public Element newDescription(String languageId, String description) {
		if (languageId == null || languageId.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		Element result = this.doc.createElement("DESCRIPTION");
		result.setAttribute("LANG_REF", languageId);
		result.appendChild(doc.createTextNode(description));
		return result;
	}

	/**
	 * Creates a new CVE_VALUE element, a child of a CV_ENTRY_ML element
	 * 
	 * @param langRef a reference to a LANGUAGE element
	 * @param value the value of the entry (in the language of langRef
	 * @param description a description of this value 
	 * 
	 * @return a new CVE_VALUE element
	 */
	public Element newCVEntryValue(String langRef, String value, String description) {
		if (langRef == null || langRef.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		Element result = this.doc.createElement("CVE_VALUE");
		attributeIfNotEmpty(result, "DESCRIPTION", description);
		result.setAttribute("LANG_REF", langRef);
		result.appendChild(doc.createTextNode(value));
		return result;
	}

	/**
	 * Creates a new CV_ENTRY_ML element
	 * 
	 * @param id the ID attribute of the new element 
	 * @param extRefId an external reference
	 * 
	 * @return a new CV_ENTRY_ML element
	 */
	public Element newCVEntryML(String id, String extRefId) {
		if (id == null || id.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		Element result = this.doc.createElement("CV_ENTRY_ML");
		result.setAttribute("CVE_ID", id);
		attributeIfNotEmpty(result, "EXT_REF", extRefId);
		return result;
	}

	/**
	 * Create a new LICENSE element
	 * 
	 * @param licenseRecord a license record containing the license text and possibly an URL 
	 * TODO could change this method to accept two Strings, like most other methods ??
	 * 
	 * @return a new LICENSE element
	 */
	public Element newLicense(LicenseRecord licenseRecord) {
		if (licenseRecord == null) {
			return doc.createElement("LICENSE");
		}
		String licenseURL = licenseRecord.getUrl();
		String license = licenseRecord.getText();
		Element result;
		
		result = doc.createElement("LICENSE");
		result.setAttribute("LICENSE_URL", licenseURL);				
		result.appendChild(doc.createTextNode(license));			

		return result;
	}

	/**
	 * NOTE: this is not part of the 2.8 schema, thought of as a preparation of upcoming
	 * changes in eaf 3.0
	 * 
	 * Creates a new REF_LINK_SET element, a set containing reference links
	 *   
	 * @param linksID an ID for the new set
	 * @param extRefId a reference to an external object
	 * @param langRef a reference to a LANGUAGE element
	 * @param cvRef a reference to a controlled vocabulary
	 * 
	 * @return a new REF_LINK_SET element
	 */
	public Element newRefLinkSet(String linksID, String extRefId,
			String langRef, String cvRef) {
		// TODO add the usual null checks? Based on requirements in the xsd?
		Element result;
		
		result = this.doc.createElement("REF_LINK_SET");
		result.setAttribute("LINKS_ID", linksID);
		attributeIfNotEmpty(result, "EXT_REF", extRefId);
		attributeIfNotEmpty(result, "LANG_REF", langRef);
		attributeIfNotEmpty(result, "CV_REF", cvRef);

		return result;
	}
	
	/**
	 * NOTE: this is not part of the 2.8 schema, thought of as a preparation of upcoming
	 * changes in eaf 3.0
	 * 
	 * Creates a new cross reference link between annotations, the link can 
	 * have several attributes. 
	 * 
	 * @param id the ID of the reference link
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
	public Element newCrossRefLink(String id, String extRefId, String langRef,
			String cveRef, String refType, String content,
			String ref1, String ref2, Directionality dir) {
		// TODO add the usual null checks? Based on requirements in the xsd?
		Element result;
		
		result = this.doc.createElement("CROSS_REF_LINK");
		result.setAttribute("REF_LINK_ID", id);
		attributeIfNotEmpty(result, "EXT_REF", extRefId);
		attributeIfNotEmpty(result, "LANG_REF", langRef);
		attributeIfNotEmpty(result, "CVE_REF", cveRef);
		attributeIfNotEmpty(result, "REF_TYPE", refType);
		if (content != null && !content.isEmpty()) {
			result.appendChild(doc.createTextNode(content));			
		}
		
		result.setAttribute("REF1", ref1);
		result.setAttribute("REF2", ref2);
		result.setAttribute("DIRECTIONALITY", dir.toString());
		
		return result;
	}

	/**
	 * NOTE: this is not part of the 2.8 schema, thought of as a preparation of upcoming
	 * changes in eaf 3.0
	 * 
	 * Create a new grouping reference link.
	 * 
	 * @param id the ID of the reference link 
	 * @param extRefId a reference to an external object
	 * @param langRef a reference to a LANGUAGE element
	 * @param cveRef a reference to a controlled vocabulary entry
	 * @param refType the type of reference link
	 * @param content a textual content of the link (e.g. description)
	 * @param refs a collection of references to elements that are part of this group
	 * 
	 * @return a new GROUP_REF_LINK
	 */
	public Element newGroupRefLink(String id, String extRefId, String langRef,
			String cveRef, String refType, String content, 
			Set<String> refs) {
		// TODO add the usual null checks? Based on requirements in the xsd?
		Element result;
		
		result = this.doc.createElement("GROUP_REF_LINK");
		result.setAttribute("REF_LINK_ID", id);
		attributeIfNotEmpty(result, "EXT_REF", extRefId);
		attributeIfNotEmpty(result, "LANG_REF", langRef);
		attributeIfNotEmpty(result, "CVE_REF", cveRef);
		attributeIfNotEmpty(result, "REF_TYPE", refType);
		if (content != null && !content.isEmpty()) {
			result.appendChild(doc.createTextNode(content));			
		}

		StringBuffer sb = new StringBuffer();
		for (String ref : refs) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(ref);
		}
		result.setAttribute("REFS", sb.toString());
		
		return result;
	}
	
}
