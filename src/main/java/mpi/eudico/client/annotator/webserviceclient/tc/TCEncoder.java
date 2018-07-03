package mpi.eudico.client.annotator.webserviceclient.tc;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mpi.eudico.client.annotator.util.AnnotationCoreComparator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.webserviceclient.typecraft.TCtoTranscription;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * A class to encode (parts of) a transcription to TypeCraft 
 * XML format.
 * 
 * @author Han Sloetjes
 */
public class TCEncoder {
	
	
	/**
	 * No-arg constructor.
	 */
	public TCEncoder() {
		
	}

	/**
	 * Encodes (part of) a transcription into TC xml format.
	 * 
	 * @param transcription the transcription
	 * @param phraseTiers a list of phrase level tiers
	 * 
	 * @return xml as a string object
	 */
	public String encodeTCTierBased(Transcription transcription, List<String> phraseTiers) {
		if (transcription == null || phraseTiers == null) {
			return null;
		}
		
		return null;
	}
	
	/**
	 * Encodes (part of) a transcription into TC xml format.
	 * 
	 * @param transcription the transcription
	 * @param tierMap a mapping of tc "tier" levels to lists of tiers
	 * 
	 * @return xml as a string
	 */
	public String encodeTCTierBased(Transcription transcription, Map<String, List<String>> tierMap) {
		if (transcription == null || tierMap == null) {
			return null;
		}
		
		return null;
	}


	/**
	 * Encodes (part of) a transcription into TC xml format.
	 * 
	 * @param transcription the transcription
	 * @param phraseType the name of the phrase level
	 * 
	 * @return xml as a string
	 */
	public String encodeTCTypeBased(TranscriptionImpl transcription, String phraseType) {
		if (transcription == null || phraseType == null) {
			return null;
		}
		Document doc = null;
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder        db  = dbf.newDocumentBuilder();
			doc = db.newDocument();
			// root element
			Element docEl = createDocElement(doc);
			if (docEl != null) {
				doc.appendChild(docEl);
			}
			// create text element
			Element textEl = createTextElement(doc, transcription); 
			if (textEl != null) {
				docEl.appendChild(textEl);
				
				// add phrases
				List<TierImpl> phrTiers = transcription.getTiersWithLinguisticType(phraseType);
				List<AbstractAnnotation> annos = null;
				if (phrTiers != null && phrTiers.size() > 1) {
					annos = sortAnnotations(phrTiers);
				} else if (phrTiers != null && phrTiers.size() == 1) {
					annos = phrTiers.get(0).getAnnotations();
				}
				
				if (annos != null && annos.size() > 0) {
					AbstractAnnotation aa;
					for (int i = 0; i < annos.size(); i++) {
						aa = annos.get(i);
						addPhraseElement(doc, transcription, textEl, aa);
					}
				}
				
				// finally serialize to a string
				try {
					//IoUtil.writeEncodedFile("UTF-8", "/Users/Shared/temp/tc.xml", doc.getDocumentElement());
					DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
					DOMImplementation domImpl = registry.getDOMImplementation("XML LS");
					if (domImpl instanceof DOMImplementationLS) {
						DOMImplementationLS domLS = (DOMImplementationLS) domImpl;
						LSSerializer serializer = domLS.createLSSerializer();
						LSOutput lsOut = domLS.createLSOutput();
						lsOut.setEncoding("UTF-8");
						StringWriter writer = new StringWriter();
						lsOut.setCharacterStream(writer);
						serializer.write(doc.getDocumentElement(), lsOut);
						//String result = serializer.writeToString(doc.getDocumentElement());// on Mac this defaults to writing UTF-16
						//String result = writer.toString();
						//System.out.println(result);
						//return result;
						return writer.toString();
					}
				} catch (Exception ex){
					ex.printStackTrace();
				}
			}
			
		} catch (ParserConfigurationException pce) {
			ClientLogger.LOG.warning("Could not create a document: " + pce.getMessage());
		}
		 
		return null;
	}

	/**
	 * Encodes (part of) a transcription into TC xml format.
	 * 
	 * @param transcription the transcription
	 * @param phraseType a mapping of tc "tier" levels to lists of types
	 * 
	 * @return xml as a string
	 */
	public String encodeTCTypeBased(Transcription transcription, Map<String, List<String>> typeMapping) {
		if (transcription == null || typeMapping == null) {
			return null;
		}
		
		return null;
	}
	
	/**
	 * Adds the annotations of all tiers in the list to one list and sorts them based on time alignment.
	 * 
	 * @param phrTiers the list of tiers
	 * @return a sorted list of annotations
	 */
	private List<AbstractAnnotation> sortAnnotations(List<TierImpl> phrTiers) {
		if (phrTiers == null || phrTiers.size() == 0) {
			return null;
		}
		
		List<AbstractAnnotation> sortedAnnos = new ArrayList<AbstractAnnotation>();
		TierImpl t;
		
		for (int i = 0; i < phrTiers.size(); i++) {
			t = phrTiers.get(i);
			sortedAnnos.addAll(t.getAnnotations());
		}
		
		Collections.sort(sortedAnnos, new AnnotationCoreComparator());
		return sortedAnnos;
	}
	
	/**
	 * Creates the document element
	 * 
	 * @return the document element
	 */
	private Element createDocElement(Document doc) {
		if (doc != null) {
			Element root = doc.createElement("typecraft");
			root.setAttribute("xsi:schemaLocation", "http://typecraft.org/typecraft.xsd");
			root.setAttribute("xmlns", "http://typecraft.org/typecraft");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			return root;
		}
		return null;
	}
	
	/**
	 * Creates a "text" element.
	 * @param doc
	 * @param transcription
	 * @return 
	 */
	private Element createTextElement(Document doc,
			TranscriptionImpl transcription) {
		if (doc != null && transcription != null) {
			Element text = doc.createElement("text");
			List<Property> props = transcription.getDocProperties();
			Property prop;
			
			for (int i = 0; i < props.size(); i++) {
				prop = props.get(i);
				if (prop.getName() != null && prop.getName().equals("id") && prop.getValue() != null) {
					text.setAttribute(prop.getName(), (String) prop.getValue());
				} else if (prop.getName() != null && prop.getName().equals("lang") && prop.getValue() != null) {
					text.setAttribute(prop.getName(), (String) prop.getValue());
				} else if (prop.getName() != null && prop.getName().equals("title") && prop.getValue() != null) {
					Element tiEl = doc.createElement(prop.getName());
					tiEl.appendChild(doc.createTextNode((String) prop.getValue()));
					text.appendChild(tiEl);
				} else if (prop.getName() != null && prop.getName().equals("titleTranslation") && prop.getValue() != null) {
					Element trEl = doc.createElement(prop.getName());
					trEl.appendChild(doc.createTextNode((String) prop.getValue()));
					text.appendChild(trEl);
				} else if (prop.getName() != null && prop.getName().equals("body") && prop.getValue() != null) {
					Element boEl = doc.createElement(prop.getName());
					boEl.appendChild(doc.createTextNode((String) prop.getValue()));
					text.appendChild(boEl);
				}
			}
			
			return text;
		}
		return null;
	}
	
	/**
	 * Adds a phrase element, including all sub elements.
	 * 
	 * @param doc
	 * @param transcription
	 * @param textEl
	 * @param phrAnn
	 */
	private void addPhraseElement(Document doc,
			Transcription transcription, Element textEl, AbstractAnnotation phrAnn) {
		if (doc == null || transcription == null || textEl == null || phrAnn == null) {
			// TODO LOG
			return;
		}
		
		Element phrEl = doc.createElement(TCtoTranscription.PHRASE);
		textEl.appendChild(phrEl);
		long bt = phrAnn.getBeginTimeBoundary();
		long dur = phrAnn.getEndTimeBoundary() - bt;
		if (dur > 0) {
			phrEl.setAttribute("offset", String.valueOf(bt));
			phrEl.setAttribute("duration", String.valueOf(dur));
		}
		String speak = ((TierImpl) phrAnn.getTier()).getParticipant();
		if (speak != null) {
			phrEl.setAttribute("speaker", speak);
		}
		// hier check valid tier and check id
		String id = phrAnn.getId();
		if (id != null && id.startsWith(TCtoTranscription.TC_ID_PREFIX)) {
			phrEl.setAttribute("id", id.substring(2));
		}
		// the annotation text itself
		Element origEl = doc.createElement("original");
		origEl.appendChild(doc.createTextNode(phrAnn.getValue()));
		phrEl.appendChild(origEl);
		// translation and description, valid
		List<TierImpl> depTiers = ((TierImpl) phrAnn.getTier()).getDependentTiers();
		TierImpl valTier;
		for (int i = 0; i < depTiers.size(); i++) {
			valTier = depTiers.get(i);
			if (valTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.VALIDITY)) {//attribute
				List<Annotation> depAnnos = phrAnn.getChildrenOnTier(valTier);// inefficient??
				if (depAnnos != null && depAnnos.size() > 0) {
					AbstractAnnotation vdAnn = (AbstractAnnotation) depAnnos.get(0);
					phrEl.setAttribute("valid", vdAnn.getValue());
				}
				//break;
			} else if (valTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.TRANSLATION)) {//element
				List<Annotation> depAnnos = phrAnn.getChildrenOnTier(valTier);// inefficient??
				if (depAnnos != null && depAnnos.size() > 0) {
					AbstractAnnotation trAnn = (AbstractAnnotation) depAnnos.get(0);
					Element tranEl = doc.createElement(TCtoTranscription.TRANSLATION);
					tranEl.appendChild(doc.createTextNode(trAnn.getValue()));
					phrEl.appendChild(tranEl);
				}
			} else if (valTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.DESCRIPTION)) {//element
				List<Annotation> depAnnos = phrAnn.getChildrenOnTier(valTier);// inefficient??
				if (depAnnos != null && depAnnos.size() > 0) {
					AbstractAnnotation descAnn = (AbstractAnnotation) depAnnos.get(0);
					Element descEl = doc.createElement(TCtoTranscription.DESCRIPTION);
					descEl.appendChild(doc.createTextNode(descAnn.getValue()));
					phrEl.appendChild(descEl);
				}
			}
		}
			
		// words
		for (int i = 0; i < depTiers.size(); i++) {
			valTier = depTiers.get(i);
			if (valTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.WORDS)) {//words
				List<Annotation> depAnnos = phrAnn.getChildrenOnTier(valTier);// inefficient??
				if (depAnnos != null && depAnnos.size() > 0) {
					AbstractAnnotation woAnn = null;
					for (int j = 0; j < depAnnos.size(); j++) {
						woAnn = (AbstractAnnotation) depAnnos.get(j);
						addWordElement(doc, transcription, phrEl, woAnn);
					}
				}
				break;
			}
		}
	}

	/**
	 * Adds a word element to a phrase element.
	 * 
	 * @param doc
	 * @param transcription
	 * @param phrEl
	 * @param woAnn the annotation to convert to a word element
	 */
	private void addWordElement(Document doc, Transcription transcription,
			Element phrEl, AbstractAnnotation woAnn) {
		if (doc == null || transcription == null || phrEl == null || woAnn == null) {
			// TODO LOG
			return;
		}
		
		Element woEl = doc.createElement("word");
		woEl.setAttribute("text", woAnn.getValue());
		phrEl.appendChild(woEl);
		
		TierImpl wTier = (TierImpl) woAnn.getTier();
		List<TierImpl> depTiers = wTier.getDependentTiers();
		TierImpl depTier;
		// head attribute and pos element
		for (int i = 0; i < depTiers.size(); i++) {
			depTier = depTiers.get(i);
			
			if (depTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.HEAD)) {
				List<Annotation> headAnnos = woAnn.getChildrenOnTier(depTier);// inefficient??
				if (headAnnos != null && headAnnos.size() > 0) {
					AbstractAnnotation heAnn = (AbstractAnnotation) headAnnos.get(0);
					woEl.setAttribute(TCtoTranscription.HEAD, heAnn.getValue());
				}
			} else if (depTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.POS)) {
				List<Annotation> posAnnos = woAnn.getChildrenOnTier(depTier);// inefficient??
				if (posAnnos != null && posAnnos.size() > 0) {
					AbstractAnnotation pAnn = (AbstractAnnotation) posAnnos.get(0);
					Element posEl = doc.createElement(TCtoTranscription.POS);
					posEl.appendChild(doc.createTextNode(pAnn.getValue()));
					woEl.appendChild(posEl);
				}
			}
		}
		
		// morphs
		for (int i = 0; i < depTiers.size(); i++) {
			depTier = depTiers.get(i);
			if (depTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.MORPH)) {
				List<Annotation> morAnnos = woAnn.getChildrenOnTier(depTier);// inefficient??
				
				if (morAnnos != null && morAnnos.size() > 0) {
					AbstractAnnotation morAnn = null;
					for (int j = 0; j < morAnnos.size(); j++) {
						morAnn = (AbstractAnnotation) morAnnos.get(j);
						
						addMorphElement(doc, transcription, woEl, morAnn);
					}
				}
				break;
			}
		}
	}

	/**
	 * Adds a morpheme element to a word element 
	 *  
	 * @param doc
	 * @param transcription
	 * @param woEl
	 * @param morAnn the annotation to convert to a morpheme element
	 */
	private void addMorphElement(Document doc, Transcription transcription,
			Element woEl, AbstractAnnotation morAnn) {
		if (doc == null || transcription == null || woEl == null || morAnn == null) {
			// TODO LOG
			return;
		}
		
		Element morEl = doc.createElement("morpheme");
		morEl.setAttribute("text", morAnn.getValue());
		woEl.appendChild(morEl);
		
		TierImpl morTier = (TierImpl) morAnn.getTier();
		List<TierImpl> depTiers = morTier.getDependentTiers();
		TierImpl depTier;
		
		for (int i = 0; i < depTiers.size(); i++) {
			depTier = depTiers.get(i);
			
			if (depTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.MEANING)) {
				List<Annotation> meanAnns = morAnn.getChildrenOnTier(depTier);// inefficient?
				
				if (meanAnns != null && meanAnns.size() > 0) {
					AbstractAnnotation meanAn = (AbstractAnnotation) meanAnns.get(0);
					morEl.setAttribute(TCtoTranscription.MEANING, meanAn.getValue());
				} 
			} else if (depTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.BASE)) {
				List<Annotation> baseAnns = morAnn.getChildrenOnTier(depTier);
				
				if (baseAnns != null && baseAnns.size() > 0) {
					AbstractAnnotation baseAn = (AbstractAnnotation) baseAnns.get(0);
					morEl.setAttribute(TCtoTranscription.BASE, baseAn.getValue());
				}
			} else if (depTier.getLinguisticType().getLinguisticTypeName().equals(TCtoTranscription.GLOSS)) {
				List<Annotation> glossAnns = morAnn.getChildrenOnTier(depTier);
				
				if (glossAnns != null && glossAnns.size() > 0) {
					AbstractAnnotation glossAn = (AbstractAnnotation) glossAnns.get(0);
					String concatValue = glossAn.getValue();
					
					if (concatValue.indexOf(TCtoTranscription.GLOSS_DELIMITER) > 0) {
						String[] glosses = concatValue.split(TCtoTranscription.GLOSS_DELIMITER);
						
						for (String gloss : glosses) {
							Element glEl = doc.createElement(TCtoTranscription.GLOSS);
							glEl.appendChild(doc.createTextNode(gloss));
							morEl.appendChild(glEl);
						}
					} else {
						Element glEl = doc.createElement(TCtoTranscription.GLOSS);
						glEl.appendChild(doc.createTextNode(concatValue));
						morEl.appendChild(glEl);
					}					
				}
			}
		}
	}
}
