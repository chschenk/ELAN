package mpi.eudico.server.corpora.clomimpl.flex;


import java.util.HashMap;
import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * A encoderinfo object for FLEx files.
 * 
 * @author Aarthy Somasundaram
 */
public class FlexEncoderInfo implements EncoderInfo {

	/** a map containing element name - List<TierImpl>*/
  	private HashMap<String, List<TierImpl>> elementMapping;
  	
	/** a map containing item name - List<TierImpl> */
  	private HashMap<String, List<TierImpl>> itemMapping;
  	
  	/** a map containing tier name - List<String> */
  	private HashMap<String, List<String>> typeLangMap;
  	
  	/** a list of tiers - List<TierImpl> */
  	private List<TierImpl> morphTypeList;
  	
  	private String filePath;
  	
  	private String mediaURL;
	 
    /**
     * Creates a encoder info instance with default values.
     */
    public FlexEncoderInfo() {
        super();
        elementMapping = new HashMap<String, List<TierImpl>>();
        itemMapping = new HashMap<String, List<TierImpl>>();
    }
    
    /**
     * Set the mapping for each element
     * 
     * @param element, the element
     * @param value , the value to be mapped for the element
     */
    public void setMappingForElement(String element, List<TierImpl> value){
    	if(element != null){
    		elementMapping.put(element, value);
    	}
    }  
    
    /**
     * Set the mapping for each element-item
     * 
     * @param item, the item
     * @param value , the value to be mapped for the element
     */
    public void setMappingForItem(String item, List<TierImpl> value){
    	if(item != null){
    		itemMapping.put(item, value);
    	}
    }  
    
    /**
     * Set the type-lang value for tiers
     * 
     * @param item, the item
     * @param value , the value to be mapped for the element
     */
    public void setTypeLangMap(HashMap<String, List<String>> map){
    	typeLangMap = map;
    }  
    
    /**
     * Set the morphType tiers
     *      
     * @param list , the list morph-type tiers
     */
    public void setMorphTypeTiers(List<TierImpl> list){
    	morphTypeList = list;
    }  
    
    /**
     * Path of the export file
     * 
     * @param path
     */
    public void setFile(String path){
    	filePath = path;
    }
    
    /**
     * Path of the export file
     * 
     * @param path
     */
    public void setMediaFile(String path){
    	mediaURL= path;
    }
    
    /**
     * Returns the export file path
     * @return
     */
    public String getMediaFile(){
    	return  mediaURL;
    }
    
    /**
     * Returns the export file path
     * @return
     */
    public String getPath(){
    	return  filePath;
    }
    
    
    /**
     * Get the type-lang value for tiers
     * 
     * @param tierName, the name of the tier
     * @param return List<String> [0] = type; [1] = lang
     */
    public List<String> getTypeLangValues(String tierName){
    	return typeLangMap.get(tierName);
    }  
    
    /**
     * Returns  the linguistic type mapped for 
     * the give element
     * 
     * @param element name
     * @return List<TierImpl>, list of tier impl objects
     */
    public List<TierImpl> getMappingForElement(String element){
    	return elementMapping.get(element);
    }
    
    /**
     * Returns  the list of morph-type tiers
   
     * @return List<TierImpl>, list of tier impl objects
     */
    public List<TierImpl> getMorphTypeTiers(){
    	return morphTypeList;
    }
    
    /**
     * Returns the List of tier impl objects mapped for 
     * the give element-item
     * 
     * @param element name
     * @return List<TierImpl>, list of tier impl objects
     */
    public List<TierImpl> getMappingForItem(String item){
    	return itemMapping.get(item);
    }
}
    
   
