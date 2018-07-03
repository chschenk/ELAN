package mpi.eudico.client.annotator.export.multiplefiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.flex.FlexEncoder;
import mpi.eudico.server.corpora.clomimpl.flex.FlexEncoderInfo;

@SuppressWarnings("serial")
public class MultipleFileFlexExportStep5 extends AbstractMultiFileExportProgessStepPane{
	
	/** a hash map of flexType - list<linguistic types> */
	private Map<String, List<String>> itemTypeMap;
	
	/** a hash map of flex-item Type - list<linguistic types> */
	private Map<String, String> elementTypeMap;	
	
	/** a hash map of flexType - list<String> (linguistic type) */
	private Map<String, List<String>> linTypeMap;
	
	private String morphType;
	
	private FlexEncoderInfo encoderInfo; 	
	
	private boolean getFromTierName;
		 
	/**
     * Constructor
     *
     * @param multiPane the container pane
     */
    public MultipleFileFlexExportStep5(MultiStepPane multiPane){
    	super(multiPane);
    }
    
    /**
     * Calls doFinish.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {    
    	    	
    	elementTypeMap = (Map<String, String>) multiPane.getStepProperty("ElementTypeMap");	
    	
    	itemTypeMap = (Map<String, List<String>>) multiPane.getStepProperty("ElementItemMap");
		
    	morphType = (String) multiPane.getStepProperty("Morph-Type");
		
    	linTypeMap = (Map<String, List<String>>) multiPane.getStepProperty("TypeLangMap");
    	
    	getFromTierName = (Boolean)multiPane.getStepProperty("GetFromTierName");
    	
        super.enterStepForward();  
    }
    
    /**
     * The actual writing.
     *
     * @param fileName path to the file, not null
     * @param orderedTiers tier names, ordered by the user, min size 1	     
     *
     * @return true if all went well, false otherwise
     */
    @Override
	protected boolean doExport(TranscriptionImpl transcription, final String fileName) {	
    	if (transcription != null && fileName != null) {
    		
        	encoderInfo = getFlexEncoderInfo(transcription);   
        	encoderInfo.setFile(fileName);
        	
        	FlexEncoder encoder = new FlexEncoder();
        	encoder.setEncoderInfo(encoderInfo);
            encoder.encode(transcription);
        	
//            try {
//            	FlexEncoder encoder = new FlexEncoder();
//                encoder.encode(transcription);
//            } catch (IOException ioe) {
//                JOptionPane.showMessageDialog(MultipleFileFlexExportStep5.this,
//                        ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
//                        "(" + ioe.getMessage() + ")",
//                        ElanLocale.getString("Message.Error"),
//                        JOptionPane.ERROR_MESSAGE);   
//            }
            return true;
        }

    	// some message describing that y export failed
    	
        return false;
    }   
    
    private FlexEncoderInfo getFlexEncoderInfo(TranscriptionImpl transcription){	    	
    	    	
    	encoderInfo = new FlexEncoderInfo();
    	
    	updateElementMappingTiers(transcription);    	
    	updateElementItemMappingTiers(transcription);
    	updateTypeLangMap(transcription);
      	
      	
      	return encoderInfo;
        
    }
    
    private void updateElementMappingTiers(TranscriptionImpl transcription){
    	List<TierImpl> itTiers = new ArrayList<TierImpl>();
		List<TierImpl> paraList = new ArrayList<TierImpl>();
		List<TierImpl> phraseList = new ArrayList<TierImpl>();
		List<TierImpl> wordList = new ArrayList<TierImpl>();
		List<TierImpl> morphList = new ArrayList<TierImpl>();
				
		encoderInfo.setMappingForElement(FlexConstants.IT, itTiers);
		encoderInfo.setMappingForElement(FlexConstants.PARAGR, paraList);
		encoderInfo.setMappingForElement(FlexConstants.PHRASE, phraseList);
		encoderInfo.setMappingForElement(FlexConstants.WORD, wordList);
		encoderInfo.setMappingForElement(FlexConstants.MORPH, morphList);
    	
    	List<TierImpl> tierList;
    	TierImpl t;   
    	// load IT tier
    	String type = elementTypeMap.get(FlexConstants.IT);
    	TierImpl selectTextTier = null;
    	if(type != null){
    		tierList = transcription.getTiersWithLinguisticType(type);    		 		
    		for(int i = 0; i < tierList.size(); i++ ){
    			t = tierList.get(i);
    			if(t.getLinguisticType().getConstraints() == null){		
    				if(selectTextTier == null){
    					selectTextTier = t;
    				}
    				
    				if(t.getName().contains(FlexConstants.IT)){
    					selectTextTier = t;
    					break;
    				}
    			}
    		}
    	}   
    	
    	if(selectTextTier != null){
			itTiers.add(selectTextTier);
		}
    	
    	//paragraph tiers
    	type = elementTypeMap.get(FlexConstants.PARAGR);
    	if(type != null){
    		tierList = transcription.getTiersWithLinguisticType(type);
    		if(tierList.size() > 0){
    			//if paragraph is the toplevel tier
        		if(tierList.get(0).getLinguisticType().getConstraints() == null){
        			paraList.addAll(tierList);	
    			} else {
    				// paragraph should be child tier is itTier
    				for(int i = 0; i < tierList.size(); i++ ){
    	    			t = tierList.get(i);
    	    			if(selectTextTier == null || t.hasParentTier() && t.getParentTier().equals(selectTextTier)){
    						if(hasPhraseTier(t)){
    							paraList.add(t);								
    						}
    					} 
    	    		}
    			
    			}
    		}
    	}
    	
    	// phrase tiers
    	type = elementTypeMap.get(FlexConstants.PHRASE);
    	if(type != null){
    		if(paraList.size() > 0){
    			//if phrase is the child tier of paragraph
    			List<TierImpl> tiers;
    			for(int x =0; x < paraList.size(); x++){
					tiers = paraList.get(x).getChildTiers();					
					for(int i = 0; i < tiers.size(); i++){
						t = tiers.get(i);
						if(t.getLinguisticType().getLinguisticTypeName().equals(type)){
							phraseList.add(t);
						}
					}
    			}
    		} else {
    			phraseList.addAll(transcription.getTiersWithLinguisticType(type));
    		}
    	}
    	
    	//word Tiers
    	type = elementTypeMap.get(FlexConstants.WORD);
    	if(type != null){
    		List<TierImpl> tiers;
    		for(int x =0; x < phraseList.size(); x++){
    			tiers = phraseList.get(x).getChildTiers();					
				for(int i = 0; i < tiers.size(); i++){
					t = tiers.get(i);
					if(t.getLinguisticType().getLinguisticTypeName().equals(type)){
						wordList.add(t);
					}
				}
    		} 
    	}
    	
    	//morph tiers
    	type = elementTypeMap.get(FlexConstants.MORPH);
    	if(type != null){
    		List<TierImpl> tiers;
    		for(int x =0; x < wordList.size(); x++){
    			tiers = wordList.get(x).getChildTiers();					
				for(int i = 0; i < tiers.size(); i++){
					t = tiers.get(i);
					if(t.getLinguisticType().getLinguisticTypeName().equals(type)){
						morphList.add(t);
					}
				}
    		} 
    	}
    }
    
    private void updateElementItemMappingTiers(Transcription transcription){				
		encoderInfo.setMappingForItem(FlexConstants.IT, getTiersForItem(FlexConstants.IT));
		encoderInfo.setMappingForItem(FlexConstants.PHRASE, getTiersForItem(FlexConstants.PHRASE));
		encoderInfo.setMappingForItem(FlexConstants.WORD, getTiersForItem(FlexConstants.WORD));
		encoderInfo.setMappingForItem(FlexConstants.MORPH, getTiersForItem(FlexConstants.MORPH));
		
		//morph Type tiers
		List<TierImpl> morphTypeTierList = new ArrayList<TierImpl>();
		encoderInfo.setMorphTypeTiers(morphTypeTierList);
		if(morphType != null){
			List<TierImpl> tierList = encoderInfo.getMappingForElement(FlexConstants.MORPH);
			
			TierImpl t;
			List<TierImpl> childTiers;
			for(int i= 0; i< tierList.size(); i++){
				t = tierList.get(i);
				childTiers = t.getChildTiers();
				for(int j= 0; j< childTiers.size(); j++){
					t = childTiers.get(j);
					if(morphType.equals(t.getLinguisticType().getLinguisticTypeName())){
						morphTypeTierList.add(t);
					}
				}
			}
		}
    }
    
    private List<TierImpl> getTiersForItem(String itemType){
		List<TierImpl> tierList = encoderInfo.getMappingForElement(itemType);;
		List<String> typeList = itemTypeMap.get(itemType);
				
		List<TierImpl> itemTierList = new ArrayList<TierImpl>();
		TierImpl t;
		List<TierImpl> childTiers;
		for(int i= 0; i< tierList.size(); i++){
			t = tierList.get(i);
			childTiers = t.getChildTiers();
			for(int j= 0; j< childTiers.size(); j++){
				t = childTiers.get(j);
				if(typeList.contains(t.getLinguisticType().getLinguisticTypeName())){
					itemTierList.add(t);
				}
			}
		}		
		return itemTierList;
	}
    
    /**
	 * Checks if the given tier has any phrase type tiers
	 * 
	 * @return boolean
	 */
	private boolean hasPhraseTier(TierImpl tier){
		if(tier != null){
			List<TierImpl> childTiers = tier.getChildTiers();
			for(int i=0; i < childTiers.size(); i++){
				tier = childTiers.get(i);				
				if(tier.getLinguisticType().getLinguisticTypeName().equals(elementTypeMap.get(FlexConstants.PHRASE))){
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void updateTypeLangMap(TranscriptionImpl transcription){
		
		List<TierImpl> tierList = new ArrayList<TierImpl>();
		tierList.addAll(encoderInfo.getMappingForElement(FlexConstants.IT));
		tierList.addAll(encoderInfo.getMappingForElement(FlexConstants.PHRASE));
		tierList.addAll(encoderInfo.getMappingForElement(FlexConstants.WORD));
		tierList.addAll(encoderInfo.getMappingForElement(FlexConstants.MORPH));
		
		tierList.addAll(encoderInfo.getMappingForItem(FlexConstants.IT));
		tierList.addAll(encoderInfo.getMappingForItem(FlexConstants.PHRASE));
		tierList.addAll(encoderInfo.getMappingForItem(FlexConstants.WORD));
		tierList.addAll(encoderInfo.getMappingForItem(FlexConstants.MORPH));
		
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		
		String type;
		String lang;
		
		for (Entry<String, List<String>> entry : linTypeMap.entrySet()) {
			String lingType = entry.getKey();
			List<String> valueList = entry.getValue();
			
			List<TierImpl> tiers = transcription.getTiersWithLinguisticType(lingType);
			
			for (TierImpl t : tiers) {
				if(tierList.contains(t)){
					type = null;
					lang = null;
					
					if(getFromTierName){
						type = getTypeName(t.getName());
						lang = getLanguage(t.getName());
						
						if(type != null){
							valueList.set(0, type);
						}
						
						if(lang != null){
							valueList.set(1, lang);
						}
					}
					map.put(t.getName(), valueList);
				}
			}
			
		}
		
		encoderInfo.setTypeLangMap(map);
	}
	
	/**
	 * Retracts the type information from the tier/linType name
	 * Expected  tier/linType name format: < tier/linType-typeName-language>
	 * 
	 *
	 * @param  name from which the type has to extracted
	 * @return type
	 */
	private String getTypeName(String name){
		String type = null;
		
		if(name.startsWith(FlexConstants.IT)){
			name = name.substring(FlexConstants.IT.length());
		}
		
		int index = name.indexOf('-');
		int nextIndex = -1;
		
		if(index > -1){
			if(index+1 < name.length()){
				nextIndex = name.indexOf('-', index+1);		
			}
					
			if(nextIndex > -1 && index+1 < nextIndex){
				type = name.substring(index+1, nextIndex);
			} else {
				type = name.substring(index+1);
			}
		}
		
		if(type == null || type.equals(FlexConstants.ITEM)){
			type = null;
		}
		
		return type;
		
	}
	
	/**
	 * Retracts the language information from the tier/linType name
	 * Expected tier/linType name format: <tier/linType-typeName-language>
	 * 
	 * @param name
	 * @return lang
	 */
	private String getLanguage(String name){		
		String lang = null;		
		
		if(name.startsWith(FlexConstants.IT)){
			name = name.substring(FlexConstants.IT.length());
		}
		
		int firstIndex = name.indexOf('-');	
		int nextIndex = -1;
		if(firstIndex+1 < name.length()){
			nextIndex = name.indexOf('-', firstIndex+1);		
		}
		
		if(nextIndex > -1 && firstIndex+1 < nextIndex){
			lang = name.substring(nextIndex+1);
		} 
		
		return lang;
	}
}
