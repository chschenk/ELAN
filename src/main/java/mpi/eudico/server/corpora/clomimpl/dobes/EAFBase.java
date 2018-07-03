package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A utility class for creating specific EAF elements in the process of building a DOM Document
 * for the purpose of serialization.  
 * <p>
 * The ANNOTATION_DOCUMENT element needs to be added to the EAF DOM document explicitly:
 * <code>
 * Element e = newAnnotationDocument(new Date()+"", "author", "version 1.0");
 * doc.appendChild(e);
 * </code>
 *
 * <p>
 * To write the DOM to file, use getDocumentElement(), which returns all including Elements.
 * 
 * The arguments of the newELEMENT() methods must not be null, if not otherwise
 * stated. A RuntimeException will be thrown if an argument is null.
 * This is much better than to silently write incomplete data.
 * 
 * @version jun 2004 Support for Controlled Vocabulary elements added
 * @version Feb 2006 support for LinkedFileDescriptors and stereotype Included_In added
 * @version Jan 2007 support for Annotator attribute added to the tier element
 *                   added element PROPERTY within the HEADER element
 * @version Nov 2007 added support for attribute RELATIVE_MEDIA_URL of MEDIA_DESCRIPTOR and
 *                   RELATIVE_LINK_URL of LINKED_FILE_DESCRIPTOR
 * @version May 2008: added support for references to concepts in the ISO Data Category Registry.
 * This applies to annotations (alignable and ref), CV entries and Linguistic Types. 
 * The methods are no longer final so that the class can be extended.
 * @version Sept 2016 this newly introduced base class more or less replaces the EAF20 through 
 * EAF25 classes. For most elements this class provides the basic implementation while subclasses
 * can extend or replace methods where needed.  
 */
public abstract class EAFBase {
	// to be set by subclasses
	protected String EAF_Format;
	protected String EAF_Schema_Location;
	
	/**
	 * Three Time Units
	 */
	public final static String TIME_UNIT_MILLISEC = "milliseconds";
	public final static String TIME_UNIT_NTSC     = "NTSC-frames";
	public final static String TIME_UNIT_PAL      = "PAL-frames";

	/**
	 * The DOM document variable doc is protected so that subclasses can access it.
	 * The newXXElement methods call the createElement(String) on the Document instance.
	 */
	protected Document doc;
	
    /**
     * Constructor. Instantiates a DocumentBuilder and creates a new Document. 
	 */
    public EAFBase() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder        db  = dbf.newDocumentBuilder();
		this.doc = db.newDocument();
    }

	/**
	 * EAFBase does not itself implement org.w3c.dom.Document, it sort of encapsulates 
	 * a Document which is created by a DocumentBuilder. Two methods delegate their
	 * working to methods of the same name of Document.
	 * 
	 * @see {@link Document#getDocumentElement()}
	 */	
	public Element getDocumentElement() { 
		return this.doc.getDocumentElement();
	}
	/**
	 * Adds a Node to the Document
	 * @see {@link Document#appendChild(Node)} 
	 * @param e the Node to add to the Document
	 * 
	 * @return the appended Node
	 */
	public Node appendChild(Node e) { 
		return this.doc.appendChild(e);
	}

	/*
	 *  All methods "newELEMENT()" are returning an object of type org.w3c.dom.Element
	 * with name ELEMENT.
	 * Technically speaking, they encapsulate
	 * the two methods Element.setAttribute() and Element.createTextNode() and perform some
	 * checks.
	 */

	/**
	 * Creates the root node, use result in <your doc variable>.appendChild();
	 * 
	 * @param creationDate Creation date of this annotation document
	 * @param author Author of this document
	 * @param version the version of the document (in practice the same as EAF Format
	 * 
	 * @return a new Element ANNOTATION_DOCUMENT.
	 */
	public Element newAnnotationDocument
		(String creationDate, String author, String version) {
		if (creationDate == null) throw new RuntimeException("EAF");
		if (author == null) throw new RuntimeException("EAF");
		if (version == null) throw new RuntimeException("EAF");

		Element result = this.doc.createElement("ANNOTATION_DOCUMENT");
		result.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		result.setAttribute("xsi:noNamespaceSchemaLocation", EAF_Schema_Location);
		result.setAttribute("DATE",   creationDate);
		result.setAttribute("AUTHOR", author);
		result.setAttribute("VERSION", version);
		result.setAttribute("FORMAT", EAF_Format);
		return result;
    }


	/**
	 * Creates a new Header element, use result in annotationDocument.appendChild();
	 * 
	 * @param mediaFile the name of one media file, deprecated, not used anymore
	 * @param timeUnits one of the TIME_UNIT constants, in practice milliseconds
	 * 
	 * @return a new Element HEADER.
	 */
	public Element newHeader (String mediaFile, String timeUnits) {
		if (mediaFile == null) throw new RuntimeException("EAF");
		if (timeUnits == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("HEADER");
		result.setAttribute("MEDIA_FILE",   mediaFile);
		result.setAttribute("TIME_UNITS", timeUnits);
		return result;
    }


	/**
	 * @see #newHeader(String, String)
	 * @param mediaFile same meaning as above
	 * 
	 * @return a new Element HEADER with a default time unit of milliseconds.
	 */
	public Element newHeader (String mediaFile) {
		if (mediaFile == null) throw new RuntimeException("EAF");
		return newHeader(mediaFile, EAFBase.TIME_UNIT_MILLISEC);
    }


    /**
     * Since eaf 2.1: support MediaDescriptors. For compatibility with
     * ELAN 1.4.1 still maintain mediaFile for some time
     * @param mediaURL the full path to a media file
     * @param relMediaURL the relative URL of the media file, relative to this document
     * @param mimeType A MIME Type String
     * @param timeOrigin a time offset determining the modified start time of the media
     * @param extractedFrom used for .wav files to specify from which media file (video)
     * the the audio has been extracted
     * @return a new MEDIA_DESCRIPTOR ELement  
     */
    public Element newMediaDescriptor(String mediaURL, String relMediaURL,
    				String mimeType, String timeOrigin, String extractedFrom) {

		if (mediaURL == null) throw new RuntimeException("EAF");
		if (mimeType == null) throw new RuntimeException("EAF");

		Element mdElement = this.doc.createElement("MEDIA_DESCRIPTOR");
		mdElement.setAttribute("MEDIA_URL", mediaURL);
		mdElement.setAttribute("MIME_TYPE", mimeType);

		attributeIfNotEmpty(mdElement, "RELATIVE_MEDIA_URL", relMediaURL);
		if (timeOrigin != null) {
			mdElement.setAttribute("TIME_ORIGIN", String.valueOf(timeOrigin));
		}
		if (extractedFrom != null) {
			mdElement.setAttribute("EXTRACTED_FROM", extractedFrom);
		}

		return mdElement;
	}
    
    /**
     * Introduced in eaf 2.3, descriptor of (non a/v) linked files.
     * 
     * @param linkURL the url of the file
     * @param relLinkURL an optional relative file url
     * @param mimeType the mimetype of the file
     * @param origin the time origin or offset
     * @param associatedWith the file this link is associated with
     * @return a new LINKED_FILE_DESCRIPTOR element
     */
    public Element newLinkedFileDescriptor(String linkURL, String relLinkURL,
            String mimeType, String origin, String associatedWith) {
        if (linkURL == null) throw new RuntimeException("EAF");
        
        Element lfdElement = this.doc.createElement("LINKED_FILE_DESCRIPTOR");
        lfdElement.setAttribute("LINK_URL", linkURL);
        
        if (mimeType == null) {
            lfdElement.setAttribute("MIME_TYPE", "unknown");
        } else {
            lfdElement.setAttribute("MIME_TYPE", mimeType);
        }
        
		attributeIfNotEmpty(lfdElement, "RELATIVE_LINK_URL", relLinkURL);
        
        if (origin != null) {
            lfdElement.setAttribute("TIME_ORIGIN", origin);
        }
        
        if (associatedWith != null) {
            lfdElement.setAttribute("ASSOCIATED_WITH", associatedWith);
        }
        
        return lfdElement;
    }

    /**
     * Introduced in EAF version 2.5. Document level property with an optional 
     * name attribute and string contents.
     * 
     * @param name the name attribute
     * @param value the content
     * @return a new PROPERTY element
     */
    public Element newProperty(String name, String value) {
    	Element propElement = this.doc.createElement("PROPERTY");
    	
		attributeIfNotEmpty(propElement, "NAME", name);
    	if (value != null && value.length() > 0) {
    		propElement.appendChild(doc.createTextNode(value));
    	}
    	return propElement;
    }
    
	/**
	 * Use result in annotationDocument.appendChild();
	 * 
	 * @return a new TIME_ORDER element
	 */
	public Element newTimeOrder () {
		Element result = this.doc.createElement("TIME_ORDER");
		return result;
    }
	
	/**
	 * Use result in time_order.appendChild();
	 * 
	 * @see #newTimeSlot(String)
	 * @param id the TIME_SLOT_ID of the TIME_SLOT
	 * @param time a time slot has a precise time, the TIME_VALUE attribute
	 * @return a new TIME_SLOT element with a time
	 */
	public Element newTimeSlot (String id, long time) {
		if (id == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("TIME_SLOT");
		result.setAttribute("TIME_SLOT_ID", id);
		result.setAttribute("TIME_VALUE", time + "");
		return result;
    }
	
	/**
	 * Use result in time_order.appendChild();
	 * 
	 * @see #newTimeSlot(String, long)
	 * @param id the TIME_SLOT_ID of the TIME_SLOT
	 * @return a new TIME_SLOT element without a time value
	 */
	public Element newTimeSlot (String id) {
		if (id == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("TIME_SLOT");
		result.setAttribute("TIME_SLOT_ID", id);
		return result;
    }
	
	/**
	 * Use result in annotationDocument.appendChild();
	 * 
	 * @param id the TIER_ID of the tier, the name of the tier 
	 * @param participant the PARTICIPANT attribute, can be null
	 * @param annotator the ANNOTATOR attribute, can be null
	 * @param typeRef the LINGUISTIC_TYPE_REF attribute, not null
	 * @param language the DEFAULT_LOCALE attribute (not the same as Language!), can be null,
	 * used for input methods
	 * @param parent the PARENT_REF attribute, can be null
	 * @return a new TIER element
	 */
	public Element newTier
		(String id,
		 String participant,
		 String annotator,
		 String typeRef,
		 Locale language,
		 String parent) {
		if (id == null) throw new RuntimeException("EAF");
		if (typeRef == null) throw new RuntimeException("EAF");
		//if (language == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("TIER");
		result.setAttribute("TIER_ID", id);
		attributeIfNotEmpty(result, "PARTICIPANT", participant);
		attributeIfNotEmpty(result, "ANNOTATOR", annotator);
		result.setAttribute("LINGUISTIC_TYPE_REF", typeRef);
		if (language != null) {
			result.setAttribute("DEFAULT_LOCALE", language.getLanguage());
		}
		attributeIfNotEmpty(result, "PARENT_REF", parent);
		return result;
    }
	
	/**
	 * Use result in tier.appendChild();
	 * 
	 * @return a new ANNOTATION element
	 */
	public Element newAnnotation () {
		Element result = this.doc.createElement("ANNOTATION");
		return result;
    }
	
	/**
	 * Use result in annotation.appendChild();
	 * 
	 * @param id the ANNOTATION_ID attribute
	 * @param beginTimeSlot the TIME_SLOT_REF1 reference attribute
	 * @param endTimeSlot the TIME_SLOT_REF2 reference attribute
	 * 
	 * @return a new ALIGNABLE_ANNOTATION element
	 */
	public Element newAlignableAnnotation
		(String id,
		 String beginTimeSlot,
		 String endTimeSlot) {
		if (id == null) throw new RuntimeException("EAF");

		Element result = this.doc.createElement("ALIGNABLE_ANNOTATION");
		result.setAttribute("ANNOTATION_ID", id);
		result.setAttribute("TIME_SLOT_REF1", beginTimeSlot);
		result.setAttribute("TIME_SLOT_REF2", endTimeSlot);
		return result;
    }
	
	/**
	 * Use result in annotation.appendChild();
	 * 
	 * @param id the ANNOTATION_ID attribute
	 * @param annotationRef the ANNOTATION_REF attribute, a reference to a parent 
	 * annotation, not null
	 * @param previousAnnotation the PREVIOUS_ANNOTATION attribute, can be null
	 * 
	 * @return a new REF_ANNOTATION element
	 */
	public Element newRefAnnotation
		(String id,
		 String annotationRef,
		 String previousAnnotation) {
		if (id == null) throw new RuntimeException("EAF");
		if (annotationRef == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("REF_ANNOTATION");
		result.setAttribute("ANNOTATION_ID", id);
		result.setAttribute("ANNOTATION_REF", annotationRef);
		attributeIfNotEmpty(result, "PREVIOUS_ANNOTATION", previousAnnotation);
		return result;
    }
	
	/**
	 * Use result in refAnnotation.appendChild() or in alignableAnnotation.appendChild()
	 * 
	 * 
	 * @param value the text of an annotation, the contents of ANNOTATION_VALUE
	 * 
	 * @return a new ANNOTATION_VALUE element
	 */
	public Element newAnnotationValue (String value) {
		if (value == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("ANNOTATION_VALUE");
		// Just to be safe, even though current serializers don't seem to add line breaks:
		//result.setAttribute("xml:space", "preserve");
		/* for filtering out illegal xml characters
		StringBuilder b = new StringBuilder(value.length());
		char[] ch = value.toCharArray();
		for (char c : ch) {
			if (c >= '\u0020') {
				b.append(c);
			} else {
				System.out.println("illegal char...");
			}
		}
		result.appendChild(doc.createTextNode(b.toString()));
		*/
		result.appendChild(doc.createTextNode(value));
		return result;
    }
	
	/**
	 * Use result in annotationDocument.appendChild();
	 *
	 * @param id the LINGUISTIC_TYPE_ID attribute, the name of the type
	 * @param timeAlignable the TIME_ALIGNABLE attribute
	 * @param constraint a reference to a CONSTRAINT element, the CONSTRAINTS attribute
`	 * @param controlledVocabularyName the name of the CV reference, the 
	 * CONTROLLED_VOCABULARY_REF attribute, can be null
	 * 
	 * @return a new LINGUISTIC_TYPE element
	 */
	public Element newLinguisticType (String id, boolean timeAlignable, 
			String constraint, String controlledVocabularyName){
		if (id == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("LINGUISTIC_TYPE");
		result.setAttribute("LINGUISTIC_TYPE_ID", id);
		result.setAttribute("TIME_ALIGNABLE", timeAlignable ? "true" : "false");
		result.setAttribute("GRAPHIC_REFERENCES", "false");
		if (constraint != null) result.setAttribute("CONSTRAINTS", constraint);
		attributeIfNotEmpty(result, "CONTROLLED_VOCABULARY_REF", controlledVocabularyName);

		return result;
    }

    /**
     * Use result in annotationDocument.appendChild();
     * 
     * @param stereotype the STEREOTYPE attribute
     * @param description the DESCRIPTION attribute, can be null
     * 
     * @return a new CONSTRAINT element
     */
    public Element newConstraint(String stereotype, String description) {
    	if (stereotype == null) throw new RuntimeException("EAF");
    	Element result = this.doc.createElement("CONSTRAINT");
    	result.setAttribute("STEREOTYPE", stereotype);
    	if (description != null) {
    		result.setAttribute("DESCRIPTION", description);
    	}
    	return result;
    }

	/**
	 * Use result in annotationDocument.appendChild();
	 * 
	 * @param locale a Locale object, not null, containing a LANGUAGE_CODE, a COUNTRY_CODE (or null)
	 * and a VARIANT
	 * 
	 * @return a new LOCALE element
	 */
	public Element newLocale (Locale l){
		if (l == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("LOCALE");
		result.setAttribute("LANGUAGE_CODE", l.getLanguage());
		if (!l.getCountry().equals("")) result.setAttribute("COUNTRY_CODE", l.getCountry());
		if (!l.getVariant().equals("")) result.setAttribute("VARIANT", l.getVariant());
		return result;
    }
    
    /**
	 * Use result in annotationDocument.appendChild();
	 * 
     * @param conVocId the CV_ID (name) of the CV
     * @param description the DESCRIPTION of the CV, can be null
     * 
     * @return a new CONTROLLED_VOCABULARY element
     */
    public Element newControlledVocabulary (String conVocId, String description) {
    	if (conVocId == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("CONTROLLED_VOCABULARY");
		result.setAttribute("CV_ID", conVocId);
		if (description != null) {
			result.setAttribute("DESCRIPTION", description);
		}
    	return result;
    }
    
	/**
	 * Use result in controlledVocabulary.appendChild();
	 * 
	 * @param value the value of the CVEntry
	 * @param description the DESCRIPTION of the entry, can be null
	 * 
	 * @return a new CV_ENTRY element
	 */
    public Element newCVEntry(String value, String description) {
    	if (value == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("CV_ENTRY");
		result.appendChild(doc.createTextNode(value));
		if (description != null) {
			result.setAttribute("DESCRIPTION", description);
		}
		
		return result;
    }

    /**
     * Adds an attribute with its value to an element, if the value is not null and
     * not empty
     * 
     * @param e the element to add an attribute to
     * @param name the name of the attribute
     * @param val the value of the attribute
     */
    protected void attributeIfNotEmpty(Element e, String name, String val) {
		if (val != null && !val.isEmpty()) {
			e.setAttribute(name, val);
		}
	}
}
