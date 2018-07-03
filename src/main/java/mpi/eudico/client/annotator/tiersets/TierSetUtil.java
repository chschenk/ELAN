package mpi.eudico.client.annotator.tiersets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesListener;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clom.Transcription;

public class TierSetUtil implements PreferencesListener{

	private List<String> tierSetSortOrder;
	
	private List<String> tierSortOrder;
	
	private List<String> visibleTierSetList;
	
	private HashMap<String, TierSet> tierSetMap;
	
	private static TierSetUtil tierSetUtil;
	
	private static String fileURL;
	
	private TierSetIO tierSetIO;
	
	/** A map of tierset listeners, grouped per document */
    private static Map<Transcription, ArrayList<TierSetListener>> listenerGroups = new HashMap<
    		Transcription, ArrayList<TierSetListener>>();
	
	private TierSetUtil(){
		// read file and load tier set
		tierSetMap = new HashMap<String, TierSet>();
		tierSetSortOrder = new ArrayList<String>();
		tierSortOrder = new ArrayList<String>();
		
		visibleTierSetList = new ArrayList<String>();
		
		tierSetIO = new TierSetIO();
		
		Object val = Preferences.get("DefaultTierSetFilePath", null) ;
		if(val != null && val instanceof String){
			fileURL = val.toString();
		} else {
			fileURL = Constants.ELAN_DATA_DIR + File.separator + "TierSet.xml";;
		}
		
		readTierSetsFromFile();
	}
	
	private void readTierSetsFromFile(){
		List<TierSet> tierSetList = null;
		try {
			tierSetList = tierSetIO.read(new File(FileUtility.urlToAbsPath(fileURL)));
		} catch (IOException e) {
			ClientLogger.LOG.warning(ElanLocale.getString("TierSet.Error.FileNotFound"));
		}
		
		if(tierSetList != null){
			for(TierSet tierSet: tierSetList){
				tierSetMap.put(tierSet.getName(), tierSet);
				tierSetSortOrder.add(tierSet.getName());				
			}
			
			updateVisibleTierSetList();
		}
	}
	
	public void writeTierSetsToFile(){
		List<TierSet> tierSetList = new ArrayList<TierSet>();
		for(String name : tierSetSortOrder){
			tierSetList.add(tierSetMap.get(name));
		}
		
		try {
			tierSetIO.write(new File(FileUtility.urlToAbsPath(fileURL)), tierSetList);
			updateVisibleTierSetList();
		} catch (IOException e) {
			ClientLogger.LOG.warning("Error while writing the tier set file: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	public static TierSetUtil getTierSetUtilInstance(){
		if(tierSetUtil == null){
			tierSetUtil = new TierSetUtil();
		} else {
			checkTierSetFile();
		}
		return tierSetUtil;
	}
	
	private static void checkTierSetFile(){
		String f = null;
		Object val = Preferences.get("DefaultTierSetFilePath", null) ;
		if(val != null && val instanceof String){
			f = val.toString();
		}
		
		if(f != null && !f.equals(fileURL)){
			fileURL = f;
			tierSetUtil = new TierSetUtil();
		}
	}
	
	public List<String> getTierOrder(Transcription transcription){
		List<String>  tierOrder = new ArrayList<String>();
		for(String tier : tierSortOrder){
			if(transcription.getTierWithId(tier) != null){
				tierOrder.add(tier);
			}
		}
		return tierOrder;
	}
	
	private void updateTierOrder(){
		tierSortOrder.clear();
		for (int i = 0; i < tierSetSortOrder.size(); i++) {
            TierSet tierSet = tierSetMap.get(tierSetSortOrder.get(i));
            if(tierSet.isVisible()){
            	for(String tierName: tierSet.getTierList()){
            		if(!tierSortOrder.contains(tierName)){
            			tierSortOrder.add(tierName);
            		}
            	}
            }
        }    
	}
	
	public List<String> getTierSetList(){
		return tierSetSortOrder;
	}
	
	public TierSet getTierSet(String tierSetName){
		return tierSetMap.get(tierSetName);
	}
	
	private void updateVisibleTierSetList(){
		visibleTierSetList.clear();
        for (int i = 0; i < tierSetSortOrder.size(); i++) {
            TierSet tierSet = tierSetMap.get(tierSetSortOrder.get(i));
            
            if(tierSet.isVisible()){
            	visibleTierSetList.add(tierSet.getName());
            }
        } 
        
        updateTierOrder();
	}
	
	public List<String> getVisibleTierSets(){
		return visibleTierSetList;
        
	}
	
	public boolean checkIfTierSetExists(String tierSetName){
		// To Do : should the names be case sensitive????????
		// Should the tier set name be unique including the tier names
		
		if(tierSetSortOrder.contains(tierSetName)){
			return true;
		} else {
			return false;
		}
	}
	
	public TierSet createTierSet(String name, List<String> tierList){
		if(name == null || tierList == null){
			// TO DO error Message;
			return null;
		}
		
		//Tier set name should be unique, check should be implemented
		
		if(!checkIfTierSetExists(name)){
			TierSet tierSet = new TierSet(name, tierList);
			tierSetSortOrder.add(name);
			tierSetMap.put(name, tierSet);
			
			updateVisibleTierSetList();
				
			return tierSet;
		} else {
			
			// TO DO : error message ????????
			return null;
		}
	}
	
	public void updateTierSet(String oldName, TierSet tierSet){
		tierSetSortOrder.set(tierSetSortOrder.indexOf(oldName), tierSet.getName());
		
		if(visibleTierSetList.contains(oldName)){
			visibleTierSetList.remove(oldName);
			visibleTierSetList.add( tierSet.getName());
		}
		
		tierSetMap.remove(oldName);
		tierSetMap.put(tierSet.getName(), tierSet);
	}
	
	public void deleteTierSet(String name){
		if(tierSetSortOrder.contains(name)){
			tierSetSortOrder.remove(name);
		}
		
		if(tierSetMap.containsKey(name)){
			tierSetMap.remove(name);
		}
		
		updateVisibleTierSetList();
	}
	
	public void updateTierSetSortOrder(List<String> tierSetOrder){
		if(tierSetOrder != null){
			tierSetSortOrder = tierSetOrder;
		}
	}
	
	public void updateTierSet(String name){
		// TO  DO : update tier naem, tier list, visible tiers etc
	}
	
	 /**
     * Adds a TierSetListener to the listener list of the specified document.
     *  
     * @param document the document in which changes the listener is interested, 
     *       the key to the group of listeners per document
     * @param listener the listener to changes in the tierset for the specified document
     */
    public void addTierSetListener(Transcription document, TierSetListener listener) {
		if (listenerGroups.containsKey(document)) {
			// check whether it is already in the list
			ArrayList<TierSetListener> listeners = listenerGroups.get(document);
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		} else {
			ArrayList<TierSetListener> list = new ArrayList<TierSetListener>();
			list.add(listener);
			
			listenerGroups.put(document, list);
		}
    }
    
    /**
     * Adds a TierSetListener to the listener list of the specified document.
     *  
     * @param document the document in which changes the listener is interested, 
     *       the key to the group of listeners per document
     * @param listener the listener to changes in the tierset for the specified document
     */
    public void removeTierSetListener(Transcription document, TierSetListener listener) {
		if (listenerGroups.containsKey(document)) {
			// check whether it is already in the list
			ArrayList<TierSetListener> listeners = listenerGroups.get(document);
			if (listeners.contains(listener)) {
				listeners.remove(listener);
			}
		} 
    }
    
    /**
     * Method for notifying all listeners of document independent, application wide
     * tierset changes.
     */
    public void notifyAllListeners(TierSet tierSet) {
    	updateVisibleTierSetList();
    	ArrayList<TierSetListener> listeners = null;
    	Iterator<ArrayList<TierSetListener>> listIt = listenerGroups.values().iterator();
    	    
    	while (listIt.hasNext()) {
    		listeners = listIt.next();
    		if (listeners != null) {
    			for (int i = 0; i < listeners.size(); i++) {
    				listeners.get(i).tierSetVisibilityChanged(tierSet);
    			}
    		}
    	}
    }
    
    /**
     * Method for notifying all listeners of document independent, application wide
     * tierset changes.
     */
    public void notifyAllListeners(String tierName, boolean isVisible) {
    	ArrayList<TierSetListener> listeners = null;
    	Iterator<ArrayList<TierSetListener>> listIt = listenerGroups.values().iterator();
    	    
    	while (listIt.hasNext()) {
    		listeners = listIt.next();
    		if (listeners != null) {
    			for (int i = 0; i < listeners.size(); i++) {
    				listeners.get(i).tierVisibilityChanged(tierName, isVisible);
    			}
    		}
    	}
    }

    /**
     * Method for notifying all listeners of document independent, application wide
     * tierset changes.
     */
    public void notifyAllListeners() {
    	updateVisibleTierSetList();
    	ArrayList<TierSetListener> listeners = null;
    	Iterator<ArrayList<TierSetListener>> listIt = listenerGroups.values().iterator();
    	    
    	while (listIt.hasNext()) {
    		listeners = listIt.next();
    		if (listeners != null) {
    			for (int i = 0; i < listeners.size(); i++) {
    				listeners.get(i).tierSetChanged();
    			}
    		}
    	}
    }

	@Override
	public void preferencesChanged() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @author Micha Hulsbosch
	 * @param name
	 * @return 
	 */
	public Boolean isVisibleTier(String name) {
		return getVisibleTiers().contains(name);
	}
	
	/**
	 * @author Micha Hulsbosch
	 * @return A list of tier names that are visible
	 *         in the visible tier sets
	 */
	public List<String> getVisibleTiers() {
		List<String> tierList = new ArrayList<String>();
		for(String tierSetName : getVisibleTierSets()){
			for(String tierName : getTierSet(tierSetName).getVisibleTierList()){
				if(!tierList.contains(tierName)){
					tierList.add(tierName);
				}
			}
		}
		return tierList;
	}
	
	public Boolean isTierInVisibleTierSets(String name) {
		return getTiersInVisibleTierSets().contains(name);
	}
	
	/**
	 * @author Micha Hulsbosch
	 * @return A list of tier names that are in
	 *         the visible tier sets
	 */
	public List<String> getTiersInVisibleTierSets() {
		List<String> tierList = new ArrayList<String>();
		for(String tierSetName : getVisibleTierSets()){
			for(String tierName : getTierSet(tierSetName).getTierList()){
				if(!tierList.contains(tierName)){
					tierList.add(tierName);
				}
			}
		}
		return tierList;
	}
}
