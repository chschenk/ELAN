package mpi.eudico.client.annotator.tiersets;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author aarsom
 *
 */
public class TierSet {
	private String name;
	private String desc;
	private List<String> tierList;
	private List<String> visibleTierList;
	private boolean visible = true;
	
	protected TierSet(String name, List<String> tierList){
		this.name = name;
		this.tierList = tierList;
		visibleTierList = new ArrayList<String>();
	}
	
	protected void setName(String name){
		this.name = name;
	}
	
	protected void setDescription(String description){
		this.desc = description;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDescription(){
		return desc;
	}
	
	/**
	 * Sets the list of tiers for this Tier set
	 * 
	 * @param tierList
	 */
	protected void setTierList(List<String> tierList){
		if(tierList != null){
			this.tierList = tierList;
			
			List<String> visibleTiersList = new ArrayList<String>();
			visibleTiersList.addAll(visibleTierList);
			visibleTierList.clear();
			
			for(String tier : visibleTiersList){
				if(tierList.contains(tier)){
					this.visibleTierList.add(tier);
				}
			}
		}
	}
	
	public List<String> getTierList(){
		return tierList;
	}
	
	public void setVisible(boolean visible){
		this.visible = visible;
	}
	
	public boolean isVisible(){
		return visible;
	}
	
	/**
	 * Checks if the tier is in this tier set
	 * 
	 * @param tierName, name of the tier to be checked
	 * @return boolean, true if yes, else false
	 */
	public boolean containsTier(String tierName){
		return tierList.contains(tierName);
	}
	
	/**
	 *Sets a list of tiers that are visible in this TierSet
	 *
	 * @param visibleTiersList
	 */
	public void setVisibleTiers(List<String> visibleTiersList){
		if(visibleTiersList != null){
			this.visibleTierList.clear();
			// extra check if the tier is in this tierSet
			for(String tier : visibleTiersList){
				if(tierList.contains(tier)){
					this.visibleTierList.add(tier);
				}
			}
		}
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public List<String> getVisibleTierList(){
		return visibleTierList;
	}
	
	/**
	 * 
	 * @param tierName
	 * @param visible
	 */
	public void setTierVisiblity(String tierName, boolean visible){
		if(tierName != null && tierList.contains(tierName)){
			if(visible){
				if(!visibleTierList.contains(tierName)){
					visibleTierList.add(tierName);
				}
			} else {
				if(visibleTierList.contains(tierName)){
					visibleTierList.remove(tierName);
				}
			}
		}
	}
}
