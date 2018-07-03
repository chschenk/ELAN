package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

import org.w3c.dom.Element;

/**
 * EAF 2.7 extends the previous versions by:
 * - adding a LEXICON_REF element for storing information concerning Lexicon (services) 
 * and lexical entries
 * - adding a LEXICON_REF attribute to Linguistic Types, to link a type to a lexicon (entry)
 * - adding an EXT_REF attribute to Controlled Vocabularies
 * 
 * @see {@link EAFBase}
 * @see {@link EAF26}
 * @version EAF 2.7
 */
public class EAF27 extends EAF26{
	public static final String EAF27_SCHEMA_LOCATION = "http://www.mpi.nl/tools/elan/EAFv2.7.xsd";
	public static final String EAF27_SCHEMA_RESOURCE = "/mpi/eudico/resources/EAFv2.7.xsd";

    /**
	 * Constructor. Sets the format attribute to 2.7 and the schema location to the location 
	 * of the EAF 2.7 xsd.
	 */
    public EAF27() throws Exception {
        super();
        EAF_Format = "2.7";
        EAF_Schema_Location = EAF27.EAF27_SCHEMA_LOCATION;
    }
    
	/**
	 * Adds a LEXICON_REF attribute to a Linguistic Type element as produced by
	 * EAF26.
	 * 
	 * @see {@link EAF26#newLinguisticType(String, boolean, String, String, String)}
	 * @param lexiconQueryBundleName a reference to a lexicon link element 
	 * 
	 * @return a new Element LINGUISTIC_TYPE element
	 */
	public Element newLinguisticType (String id, boolean timeAlignable, 
		String constraint, String controlledVocabularyName, String extRefId, String lexiconQueryBundleName){
		Element result = super.newLinguisticType(id, timeAlignable, constraint,
				controlledVocabularyName, extRefId);
		attributeIfNotEmpty(result, "LEXICON_REF", lexiconQueryBundleName);

		return result;
    }


//    /**
//	 * Creates a new annotation value element with a CVEntryID
//	 * in case there is an external CV linked 
//	 * 
//	 * @param value
//	 * @param valueId
//	 * @return result (Element)
//	 */
//	public Element newAnnotationValue (String value, String valueId) {
//		Element result = newAnnotationValue(value);
//		if (valueId != null) {
//			result.setAttribute("CVEntryID", valueId);
//		}
//		return result;
//	}

	/**
	 * Adds an external reference attribute to a controlled vocabulary element as produced 
	 * by EAFBase 
	 * 
	 * @see {@link EAFBase#newControlledVocabulary(String, String)}
	 * @param conVocId the ID of the Controlled Vocabulary
	 * @param description a description of the vocabulary
	 * @param extRef the external reference attribute
	 * 
	 * @return result a new CONTROLLED_VOCABULARY element
	 */
	public Element newControlledVocabulary (String conVocId, String description, String extRef) {
		Element result = super.newControlledVocabulary(conVocId, description);
		attributeIfNotEmpty(result, "EXT_REF", extRef);
		return result;
	}

	/**
	 * Creates an element for identifying a Lexicon connection. It is based on the assumption that 
	 * the interaction with the lexicon is through is a web service connection.
	 * The reference can also include an identifier for a lexical entry field 
	 * 
	 * TODO could change this method to accept only Strings, like most other methods ??
	 * 
	 * @param lexRef the ID for the new LEXICON_REF element
	 * @param queryBundle the object providing the information for most properties/
	 * attributes of the new element
	 * 
	 * @see #newLexiconLink(String, LexiconLink)
	 * @return a new LEXICON_REF element
	 */
	public Element newLexiconReference(String lexRef, LexiconQueryBundle2 queryBundle) {
		// TODO add the usual null checks? Based on requirements in the xsd?
		if (lexRef == null || lexRef.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (queryBundle == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (queryBundle.getLink() == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (queryBundle.getLink().getName() == null || queryBundle.getLink().getName().isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (queryBundle.getLink().getLexSrvcClntType() == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (queryBundle.getLink().getUrl() == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (queryBundle.getLink().getLexId().getId() == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (queryBundle.getLink().getLexId().getName() == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		
		Element result = this.doc.createElement("LEXICON_REF");
		result.setAttribute("LEX_REF_ID", lexRef);
		result.setAttribute("TYPE", queryBundle.getLink().getLexSrvcClntType());
		result.setAttribute("URL", queryBundle.getLink().getUrl());
		result.setAttribute("LEXICON_ID", queryBundle.getLink().getLexId().getId());
		result.setAttribute("LEXICON_NAME", queryBundle.getLink().getLexId().getName());
		if (queryBundle.getFldId() != null) {
			if (queryBundle.getFldId().getId() != null) {
				result.setAttribute("DATCAT_ID", queryBundle.getFldId().getId());
			}
			if (queryBundle.getFldId().getName() != null) {
				result.setAttribute("DATCAT_NAME", queryBundle.getFldId().getName());
			}
		}
		result.setAttribute("NAME", queryBundle.getLink().getName());
		return result;
	}
	
	/**
	 * Creates a new LEXICON_REF element representing a reference to a Lexicon (which is thought 
	 * of as being accessed via a web service). This variant does not contain an identification of
	 * a single field in the lexical entries.
	 * 
	 * TODO could change this method to accept only Strings, like most other methods ??
	 * 
	 * @param lexRef an ID for this element
	 * @param link an object providing additional information concerning the lexicon
	 * (name, type, URL)
	 * 
	 * @see #newLexiconReference(String, LexiconQueryBundle2)
	 * @return a LEXICON_REF element
	 */
	public Element newLexiconLink(String lexRef, LexiconLink link) {
		if (lexRef == null || lexRef.isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (link == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (link.getName() == null || link.getName().isEmpty()) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (link.getLexSrvcClntType() == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (link.getUrl() == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (link.getLexId() == null || link.getLexId().getId() == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		if (link.getLexId() == null || link.getLexId().getName() == null) {
			throw new RuntimeException("EAF");// should be a specific Exception
		}
		Element result = this.doc.createElement("LEXICON_REF");
		result.setAttribute("LEX_REF_ID", lexRef);
		result.setAttribute("TYPE", link.getLexSrvcClntType());
		result.setAttribute("URL", link.getUrl());
		result.setAttribute("LEXICON_ID", link.getLexId().getId());
		result.setAttribute("LEXICON_NAME", link.getLexId().getName());
		result.setAttribute("NAME", link.getName());
		return result;
	}
}
