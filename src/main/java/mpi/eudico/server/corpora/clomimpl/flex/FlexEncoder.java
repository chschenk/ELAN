package mpi.eudico.server.corpora.clomimpl.flex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.util.AnnotationCoreComparator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.util.IoUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class to encode a transcription to flex 
 * XML format.
 * 
 * @author Aarthy Somasundaram
 */
public class FlexEncoder {
	
	private FlexEncoderInfo encoderInfo;
	private AnnotationCoreComparator comparator;	
	private String mediaGUID;	
	
	private HashMap<String, String> guidMap = new HashMap<String, String>();
		
	/**
	 * No-arg constructor.
	 */
	public FlexEncoder() {
		
	}
	
	/**
	 * Set the encoder info
	 * 
	 * @param info
	 */
	public void setEncoderInfo(FlexEncoderInfo info){
		encoderInfo = info;
		//printEncoder();
	}

	/**
	 * Encodes (part of) a transcription into Flex format.
	 * 
	 * @param transcription the transcription	
	 */
	public void encode(TranscriptionImpl transcription) {
		if (transcription == null) {
			return ;
		}		
		
		comparator = new AnnotationCoreComparator();
		
		List<TierImpl> tiers = null;	
		TierImpl tier = null;
		Annotation ann = null;
		
		try{	
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder        db  = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			
			if(doc == null){
			///warning and return
			}
			
			// root element
			Element docEl = createDocElement(doc);
			doc.appendChild(docEl);
						
			// create interlinear text element					
			Element interlinearTextEl = doc.createElement(FlexConstants.IT);		

			List<TierImpl> itTiers = encoderInfo.getMappingForElement(FlexConstants.IT);
			if(itTiers.size() > 0){
				tier = itTiers.get(0);
			}
			
			if(tier != null){	// assuming that interlinear text tier should have only one annotation				
				if(tier.getAnnotations() != null && tier.getAnnotations().size() > 0){
					ann = tier.getAnnotations().get(0);
				}
				
				if(ann != null){													
					// create interlinear item
				//	if(getTypeName(tier.getName()) != null && getLanguage(tier.getName()) != null){
						Element interlinearTextItemEl = createItemElement(doc, tier.getName(), ann);
						interlinearTextEl.appendChild(interlinearTextItemEl);		
				//  }
				} 
			}
			
			interlinearTextEl.setAttribute(FlexConstants.GUID, getGuidValue(ann, FlexConstants.IT));			
			docEl.appendChild(interlinearTextEl);					
	
			// create interlinear text items if any			
			if(tier != null){	
				Element interlinearTextItemEl;
				tiers = encoderInfo.getMappingForItem(FlexConstants.IT);  
				for(int i=0; i< tiers.size(); i++){
					ann = null;
					tier = tiers.get(i);
					if(tier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
						if(tier.getAnnotations() != null && tier.getAnnotations().size() > 0){
							ann = tier.getAnnotations().get(0);
						}						

						// assumes & expects that interlinear text has only one annotation
						if(ann != null){
							interlinearTextItemEl = createItemElement(doc, tier.getName(), ann);
							interlinearTextEl.appendChild(interlinearTextItemEl);
						}												
					}
				}
			}
			
			// create paragraphs element			
			Element paraGraphsEl = doc.createElement("paragraphs");
			interlinearTextEl.appendChild(paraGraphsEl);			
			
			//create paragraph element
			List<TierImpl> paragraphTiers = encoderInfo.getMappingForElement(FlexConstants.PARAGR);
			List<TierImpl> phraseTiers = encoderInfo.getMappingForElement(FlexConstants.PHRASE);
			List<Annotation> paragraphAnnotations = new ArrayList<Annotation>();
			List<Annotation> phraseAnns = null;
			Element paraGraphEl = null;
			Element phrasesEl = null;
			
			for(int i=0; i < paragraphTiers.size(); i++){
				tier = paragraphTiers.get(i);
				paragraphAnnotations.addAll(tier.getAnnotations());
			}
						
			if(paragraphAnnotations.size() > 0){
				Collections.sort(paragraphAnnotations, comparator);				
			
				long beginTime = 0;
				long endTime = 0;
					
				for(int i=0; i < paragraphAnnotations.size(); i++){
					ann = paragraphAnnotations.get(i);
					
					if(i == 0){
						//create paragraph element
						paraGraphEl = createParaGraphElement(doc, ann);	
						paraGraphsEl.appendChild(paraGraphEl);
						
						//create phrases element
						phrasesEl = doc.createElement("phrases");				
						paraGraphEl.appendChild(phrasesEl);	
						
						beginTime = ann.getBeginTimeBoundary();
						endTime = ann.getEndTimeBoundary();
					} else{
						
						if(ann.getBeginTimeBoundary() < endTime){
							if(ann.getEndTimeBoundary() > endTime){
								endTime = ann.getEndTimeBoundary();
							}
						} else{
							//create paragraph element
							paraGraphEl = createParaGraphElement(doc, ann);	
							paraGraphsEl.appendChild(paraGraphEl);
							
							//create phrases element
							phrasesEl = doc.createElement("phrases");				
							paraGraphEl.appendChild(phrasesEl);	
							
							beginTime = ann.getBeginTimeBoundary();
							endTime = ann.getEndTimeBoundary();
						}
					}
						
					phraseAnns = new ArrayList<Annotation>();
					for(int t = 0; t < phraseTiers.size(); t++){
						phraseAnns.addAll(ann.getChildrenOnTier(phraseTiers.get(t)));
					}
					
					Collections.sort(phraseAnns, comparator);
						
					//each paragraph/phrases annotation can have any number of phrase annotation	
					for(int a = 0; a < phraseAnns.size(); a++){
						addPhraseElement(doc, phrasesEl, phraseAnns.get(a));
					}						
					
					// do not write a empty paragraph element
					if (paraGraphEl != null) {// HS maybe unnecessary, superfluous check only useful in error conditions?
						if(phraseAnns.size() == 0 ){
							if (paraGraphEl.getParentNode() == paraGraphsEl) {
								paraGraphsEl.removeChild(paraGraphEl);
							}
						} else {
							paraGraphsEl.appendChild(paraGraphEl);
						}
					}
				}			
			} else {
				phraseAnns = new ArrayList<Annotation>();
					
				for(int i=0; i < phraseTiers.size(); i++){
					phraseAnns.addAll(phraseTiers.get(i).getAnnotations());
				}
				
				Collections.sort(phraseAnns, comparator);
				
				//each paragraph/phrases annotation can have any number of phrase annotation	
				for(int a = 0; a < phraseAnns.size(); a++){
					//create paragraph element
					paraGraphEl = createParaGraphElement(doc, ann);	
					paraGraphsEl.appendChild(paraGraphEl);
					
					//create phrases element
					phrasesEl = doc.createElement("phrases");				
					paraGraphEl.appendChild(phrasesEl);	
					
					addPhraseElement(doc, phrasesEl, phraseAnns.get(a));
				}
			}
			
			Element mediaFileEl = doc.createElement("media-files");	
			mediaFileEl.setAttribute("offset-type", "");
			
			Element mediaEl = doc.createElement(FlexConstants.MEDIA);
			mediaEl.setAttribute(FlexConstants.GUID, mediaGUID);
			// HS question: isn't the set of media descriptors of the transcription not enough for export?
			if(ELANCommandFactory.getViewerManager(transcription) != null){
				if(ELANCommandFactory.getViewerManager(transcription).getMasterMediaPlayer().getMediaDescriptor() != null){
					mediaEl.setAttribute(FlexConstants.LOCATION, ELANCommandFactory.getViewerManager(
							transcription).getMasterMediaPlayer().getMediaDescriptor().mediaURL);
					// export the other media descriptors 
					if (transcription != null) { //this could be the test above instead of via the viewer manager
						List<MediaDescriptor> mediaDescriptors = transcription.getMediaDescriptors();
						MediaDescriptor md;
						
						for (int i = 0; i < mediaDescriptors.size(); i++) {
							md = mediaDescriptors.get(i);
							if (md == ELANCommandFactory.getViewerManager(transcription).getMasterMediaPlayer().getMediaDescriptor()) {
								continue; // change this if the above test is not necessary
							}
							Element otherMediaEl = doc.createElement(FlexConstants.MEDIA);
							otherMediaEl.setAttribute(FlexConstants.GUID, UUID.randomUUID().toString());
							otherMediaEl.setAttribute(FlexConstants.LOCATION, md.mediaURL);
							
							mediaFileEl.appendChild(otherMediaEl);
						}
					}
				} else {
					mediaEl.setAttribute(FlexConstants.LOCATION,"");
				}
			} else {
				String mediaUrl = encoderInfo.getMediaFile();
				if(mediaUrl != null){
					mediaEl.setAttribute(FlexConstants.LOCATION, mediaUrl);
				} else {
					mediaEl.setAttribute(FlexConstants.LOCATION,"");
				}
			}
					
			if (!mediaFileEl.hasChildNodes()) {
				mediaFileEl.appendChild(mediaEl);
			} else {
				mediaFileEl.insertBefore(mediaEl, mediaFileEl.getFirstChild());
			}
			
			interlinearTextEl.appendChild(mediaFileEl);
			
			//create languages element
			
			List<Property> properties = transcription.getDocProperties();
			if(properties != null){
				String languages = null;
				Property prop;
				for(int i = 0; i < properties.size(); i++){
					prop = properties.get(i);
					if(prop.getName().equals(FlexConstants.LANGUAGES)){
						languages = (String) prop.getValue();
						break;
					}
				}
				
				if(languages != null){
					String lang[] = languages.split(" ");
					List<String> langList = new ArrayList<String>();
					
					for (String element : lang) {
						langList.add(element);
					}
					
					if(langList.size() > 0){
						Element languagesEl = doc.createElement(FlexConstants.LANGUAGES);	
						interlinearTextEl.appendChild(languagesEl);
						
						Element languageEl;
						for(int i = 0; i < properties.size(); i++){
							prop = properties.get(i);
							if(langList.contains(prop.getName())){
								languageEl = createLanguageElement(doc, prop);
								languagesEl.appendChild(languageEl);
							}
						}
					}
				}
			}
			
			// finally serialize to a string
			try {
				IoUtil.writeEncodedFile("UTF-8", encoderInfo.getPath(), docEl);
			} catch (Exception ex){
				ex.printStackTrace();
			}			
		} catch (ParserConfigurationException pce) {
			ClientLogger.LOG.warning("Could not create a document: " + pce.getMessage());
		}
	}
	
	/**
	 * Creates the document element
	 * 
	 * @return the document element
	 */
	private Element createDocElement(Document doc) {
		Element root = doc.createElement(FlexConstants.DOC);
		root.setAttribute("version", "2");
		return root;
	}
	
	/**
	 * Creates a "item" element.
	 * 
	 * @param doc, Document
	 * @param tierName
	 * @param ann
	 * @param defaultType
	 * @param defaultLanguage
	 * 
	 * @return element
	 */
	private Element createItemElement(Document doc, String tierName, Annotation ann) {
		Element text = doc.createElement(FlexConstants.ITEM);	
		text.setTextContent(ann.getValue());
		
		List<String> values = encoderInfo.getTypeLangValues(tierName);
		
		String type = null;
		
		// for punct
		if(isPunctType(ann)){
			type = FlexConstants.PUNCT;
		} else {
			type = values.get(0);			
		}
		
		text.setAttribute(FlexConstants.TYPE, type);		
		
		text.setAttribute(FlexConstants.LANG, values.get(1));			
		
		return text;
	}
	
	/**
	 * Creates a "lang" element
	 * @param doc
	 * @param prop
	 * @return
	 */
	private Element createLanguageElement(Document doc, Property prop) {
		Element lang = doc.createElement(FlexConstants.LANGUAGE);	
		lang.setAttribute(FlexConstants.LANG, prop.getName());
		
		String value[] = ((String) prop.getValue()).split("-");
		String font = null;
		String vernacular = null;
		if(value.length == 1){
			font = value[0];
		} else if(value.length == 2){
			font = value[0];
			vernacular = value[1];
		} 
		if(font != null){
			lang.setAttribute(FlexConstants.FONT, font);
		}		
		
		if(vernacular != null){
			lang.setAttribute(FlexConstants.VERNACULAR, vernacular);
		}
		
		return lang;
	}
	
	/**
	 * Check if the given annotation is of type punct
	 * 
	 * @param ann
	 * 
	 * @return boolean
	 */
	private boolean isPunctType(Annotation ann){
		String value = ((AbstractAnnotation)ann).getExtRefValue(ExternalReference.ISO12620_DC_ID);
		if(value != null && value.equals(FlexConstants.PUNCT_ISOCAT_URL)){
			return true;
		}
		
		String regex = "\\p{P}";
		if(ann.getValue().matches(regex)){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Creates a "paragraph" element.
	 * 
	 * @param doc
	 * @param ann
	 * 
	 * @return  element
	 */
	private Element createParaGraphElement(Document doc, Annotation ann){
		Element paraGr = doc.createElement(FlexConstants.PARAGR);	
		paraGr.setAttribute(FlexConstants.GUID, getGuidValue(ann,FlexConstants.PARAGR));
		
		return paraGr;
	}
	
//	/**
//	 * Retracts the type information from the tier name
//	 * Expected tier name format: <tierName-typeName-language>
//	 * 
//	 *
//	 * @param tierName from which the type has to extracted
//	 * @return type
//	 */
//	private String getTypeName(String tierName){		
//		String type = null;
//		
//		if(tierName.startsWith(FlexConstants.IT)){
//			tierName = tierName.substring(FlexConstants.IT.length());
//		}
//		
//		int index = tierName.indexOf('-');
//		int nextIndex = tierName.lastIndexOf('-');		
//		
//		if(nextIndex > -1 && index+1 < nextIndex){
//			type = tierName.substring(index+1, nextIndex);
//		}
//		
//		return type;
//		
//	}
	
//	/**
//	 * Retracts the language information from the tier name
//	 * Expected tier name format: <tierName-typeName-language>
//	 * 
//	 * @param tierName
//	 * @return lang
//	 */
//	private String getLanguage(String tierName){
//		String lang = null;		
//		int index = tierName.lastIndexOf('-');	
//		if( index+1 < tierName.length()-1){
//			lang = tierName.substring(index+1);
//		}
//		return lang;
//	}
	
	/**
	 * Get the GUID value from the given annotation
	 * 
	 * @param ann
	 * 
	 * @return String
	 */
	private String getGuidValue(Annotation ann, String elementType){
		String guid;
		if(ann != null){
			guid = ann.getId();		
			if(guid != null){
				int index = guid.indexOf(FlexConstants.FLEX_GUID_ANN_PREFIX);
				if(index > 0){
					guid = guid.substring(index + FlexConstants.FLEX_GUID_ANN_PREFIX.length());
				} else {
					guid = null;
				}
			}			
			
			if(elementType != null &&
					(elementType.equals(FlexConstants.WORD) || elementType.equals(FlexConstants.MORPH))){
				String annValue = ann.getValue();
				
				if(guid == null){
					guid = guidMap.get(annValue);
				}
				
				if(guid == null){
					guid = UUID.randomUUID().toString();
				}
				
				if(!guidMap.containsKey(annValue)){
					guidMap.put(annValue, guid);
				}
			}
			
			if(guid != null){
				return guid;
			}
		}	
	
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Adds a phrase element, including all sub elements.
	 * 
	 * @param doc
	 * @param phrasesEI
	 * @param phrAnn
	 */
	private void addPhraseElement(Document doc, Element phrasesEI, Annotation phraseAnn) {
		if (doc == null ||  phrasesEI == null || phraseAnn == null) {
			// TODO LOG
			return;
		}
		
		if(mediaGUID == null){
			mediaGUID = UUID.randomUUID().toString();
		}
		
		TierImpl phraseTier = (TierImpl)phraseAnn.getTier();
		
		Element phrEl = doc.createElement(FlexConstants.PHRASE);
		phrasesEI.appendChild(phrEl);
		
		phrEl.setAttribute(FlexConstants.GUID, getGuidValue(phraseAnn, FlexConstants.PHRASE));	
		phrEl.setAttribute(FlexConstants.SPEAKER, phraseTier.getParticipant());
		phrEl.setAttribute(FlexConstants.BEGIN_TIME, Long.toString(phraseAnn.getBeginTimeBoundary()));
		phrEl.setAttribute(FlexConstants.END_TIME, Long.toString(phraseAnn.getEndTimeBoundary()));		
		phrEl.setAttribute("media-file", mediaGUID);		
		
		Element phrItemEl ;
		//if(getTypeName(phraseTier.getName()) != null && getLanguage(phraseTier.getName()) != null){
			//TO DO: set default values for type and lang
			phrItemEl = createItemElement(doc, phraseTier.getName(), phraseAnn);
			phrEl.appendChild(phrItemEl);
		//}
		
		List<TierImpl> selectedItemTiers = encoderInfo.getMappingForItem(FlexConstants.PHRASE); 
		List<TierImpl> wordTiers = encoderInfo.getMappingForElement(FlexConstants.WORD);
		List<TierImpl> childTiers = phraseTier.getChildTiers();
		TierImpl childTier;
		List<Annotation> childAnns;
		if(childTiers != null){
			boolean wordTierAdded = false;
			for(int i= 0; i < childTiers.size(); i++){
				childTier = childTiers.get(i);
				if(selectedItemTiers.contains(childTier)){
				//if(childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
					childAnns = phraseAnn.getChildrenOnTier(childTier);
					//add phrase item
					if(childAnns != null && childAnns.size() > 0){
						//TO DO: set default values for type and lang
						phrItemEl = createItemElement(doc, childTier.getName(), childAnns.get(0));
						phrEl.appendChild(phrItemEl);
					}
				} else if(wordTiers.contains(childTier) && !wordTierAdded){
					//create words element
					Element wordsEl= doc.createElement("words");
					phrEl.appendChild(wordsEl);		
					
					childAnns = phraseAnn.getChildrenOnTier(childTier);
										
					if(childAnns != null){
						Collections.sort(childAnns, comparator);
						for(int w = 0; w < childAnns.size(); w++){
							addWordElement(doc, wordsEl, childAnns.get(w));
						}
					}
					wordTierAdded = true;
					
				}
			}
		}
	}

	/**
	 * Adds a word element to a phrase element.
	 * 
	 * @param doc
	 * @param wordsEl
	 * @param wordAnn	
	 */
	private void addWordElement(Document doc, Element wordsEl, Annotation wordAnn) {
		if (doc == null || wordsEl == null || wordAnn == null) {
			// TODO LOG
			return;
		}
		
		TierImpl wordTier = (TierImpl)wordAnn.getTier();
		
		Element wordEl = doc.createElement(FlexConstants.WORD);
		wordsEl.appendChild(wordEl);	
		
		Element wordItemEl = null;
		
		//if(getTypeName(wordTier.getName()) != null && getLanguage(wordTier.getName()) != null){
			//TO DO: set default values for type and lang
			wordItemEl = createItemElement(doc, wordTier.getName(), wordAnn);
			wordEl.appendChild(wordItemEl);
		//}
		
		
		if(wordItemEl == null || !wordItemEl.getAttribute(FlexConstants.TYPE).equals(FlexConstants.PUNCT)){
			wordEl.setAttribute(FlexConstants.GUID, getGuidValue(wordAnn, FlexConstants.WORD));	
		}
		
		List<TierImpl> selectedItemTiers = encoderInfo.getMappingForItem(FlexConstants.WORD); 
		List<TierImpl> morphTiers = encoderInfo.getMappingForElement(FlexConstants.MORPH);
		List<TierImpl> childTiers = wordTier.getChildTiers();
		TierImpl childTier;
		List<Annotation> childAnns;
		if(childTiers != null){
			boolean morphTierAdded = false;
			for(int i= 0; i < childTiers.size(); i++){
				childTier = childTiers.get(i);
				if(selectedItemTiers.contains(childTier)){
				//if(childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
					childAnns = wordAnn.getChildrenOnTier(childTier);
					
					//add word item
					if(childAnns != null && childAnns.size() > 0){
						//TO DO: set default values for type and lang
						wordItemEl = createItemElement(doc, childTier.getName(), childAnns.get(0));
						wordEl.appendChild(wordItemEl);
					}
				} else if(morphTiers.contains(childTier) && !morphTierAdded){				
					childAnns = wordAnn.getChildrenOnTier(childTier);
										
					if(childAnns != null && childAnns.size() > 0){
						//create morphemes element
						Element morphsEl= doc.createElement("morphemes");
						wordEl.appendChild(morphsEl);		
						
						Collections.sort(childAnns, comparator);
						for(int w = 0; w < childAnns.size(); w++){
							addMorphElement(doc, morphsEl, childAnns.get(w));
						}
					}
					morphTierAdded = true;
					
				}
			}
		}
	}

	/**
	 * Adds a morpheme element to a word element 
	 *  
	 * @param doc
	 * @param morphsEl
	 * @param morphAnn 
	 */
	private void addMorphElement(Document doc, Element morphsEl, Annotation morphAnn) {
		if (doc == null ||  morphsEl == null || morphAnn == null) {
			// TODO LOG
			return;
		}
		
		TierImpl morphTier = (TierImpl)morphAnn.getTier();
			
		Element morphEl = doc.createElement(FlexConstants.MORPH);
		morphsEl.appendChild(morphEl);		
		
		Element morphItemEl;	
		boolean morphTypeFound = false;
		
		morphEl.setAttribute(FlexConstants.GUID, getGuidValue(null, FlexConstants.MORPH));	// random value
		morphEl.setAttribute(FlexConstants.TYPE, "root"); // default value;
		
		List<TierImpl> morphTypeTiersList = encoderInfo.getMorphTypeTiers();
		List<TierImpl> selectedItemTiers = encoderInfo.getMappingForItem(FlexConstants.MORPH); 
		
		morphItemEl = createItemElement(doc, morphAnn.getTier().getName(), morphAnn);
		morphEl.appendChild(morphItemEl);
			
		List<TierImpl> childTiers = morphTier.getChildTiers();
		TierImpl childTier;
		List<Annotation> childAnns;
		if(childTiers != null){			
			for(int i= 0; i < childTiers.size(); i++){
				childTier = childTiers.get(i);
				if(selectedItemTiers.contains(childTier)){
				//if(childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
					childAnns = morphAnn.getChildrenOnTier(childTier);
					
					//add morph item
					if(childAnns != null && childAnns.size() > 0){						
						morphItemEl = createItemElement(doc, childTier.getName(), childAnns.get(0));
						morphEl.appendChild(morphItemEl);						
					}
				} else if(!morphTypeFound && morphTypeTiersList.contains(childTier)) {
					childAnns = morphAnn.getChildrenOnTier(childTier);
					if(childAnns != null && childAnns.size() > 0){
						morphEl.setAttribute(FlexConstants.GUID, getGuidValue(childAnns.get(0), FlexConstants.MORPH));	
						morphEl.setAttribute(FlexConstants.TYPE, childAnns.get(0).getValue());
						morphTypeFound = true;
					} 
				}
			}
		}
	}
	
	//######### print some encoder info ##########
	private void printEncoder(){
		if (encoderInfo != null) {
			System.out.println("FLEx encoder information:");
			
			System.out.println("Tiers for interlinear text:");
			List<TierImpl> tiers = encoderInfo.getMappingForElement(FlexConstants.IT);
			printTier(tiers);
			System.out.println("Tiers for interlinear text items:");
			tiers = encoderInfo.getMappingForItem(FlexConstants.IT);
			printTier(tiers);
			
			System.out.println("Tiers for paragraph:");
			tiers = encoderInfo.getMappingForElement(FlexConstants.PARAGR);
			printTier(tiers);
			System.out.println("Tiers for paragraph items:");
			tiers = encoderInfo.getMappingForItem(FlexConstants.PARAGR);
			printTier(tiers);
			
			System.out.println("Tiers for phrase:");
			tiers = encoderInfo.getMappingForElement(FlexConstants.PHRASE);
			printTier(tiers);
			System.out.println("Tiers for phrase items:");
			tiers = encoderInfo.getMappingForItem(FlexConstants.PHRASE);
			printTier(tiers);
			
			System.out.println("Tiers for words:");
			tiers = encoderInfo.getMappingForElement(FlexConstants.WORD);
			printTier(tiers);
			System.out.println("Tiers for words items:");
			tiers = encoderInfo.getMappingForItem(FlexConstants.WORD);
			printTier(tiers);
			
			System.out.println("Tiers for morphemes:");
			tiers = encoderInfo.getMappingForElement(FlexConstants.MORPH);
			printTier(tiers);
			System.out.println("Tiers for morphemes item:");
			tiers = encoderInfo.getMappingForItem(FlexConstants.MORPH);
			printTier(tiers);
			
		} else {
			System.out.println("No encoder information available.");
		}
	}
	
	private void printTier(List<TierImpl> tiers) {
		if (tiers == null || tiers.size() == 0) {
			System.out.println("None.");
		} else {
			for (TierImpl t : tiers) {
				System.out.println(t.getName());
			}
		}
	}
}


