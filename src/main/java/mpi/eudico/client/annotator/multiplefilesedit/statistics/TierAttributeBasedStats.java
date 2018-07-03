package mpi.eudico.client.annotator.multiplefilesedit.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder for information based on an attribute of tiers for multiple  files.
 * To be used for annotator, participant, linguistic type, content language
 *
 * @author Han Sloetjes
 */
public 	class TierAttributeBasedStats {
	private String attributeValue;
	public List<Long> durations;// should every individual duration be in the list or only the unique durations?
	//public int numFiles;
	public int numTiers;
	public int numAnnotations;
	public long minDur;
	public long maxDur;
	public long totalDur;
	public long latency;
	
	private List<String> fileNames;
	private List<String> tierNames;
	/**
	 * @param attributeValue the value of the attribute
	 */
	public TierAttributeBasedStats(String attributeValue) {
		super();
		this.attributeValue = attributeValue;
		fileNames = new ArrayList<String>();
		tierNames = new ArrayList<String>();
		durations = new ArrayList<Long>();
	}
	
	public String getAttributeValue() {
		return attributeValue;
	}
	
	public void addFileName(String fileName) {
		if (!fileNames.contains(fileName)) {
			fileNames.add(fileName);
		}
	}
	
	public void addTierName(String name) {
		if (!tierNames.contains(name)) {
			tierNames.add(name);
		}
	}
	
	public int getNumFiles() {
		return fileNames.size();
	}
	
	public int getNumUniqueTiers() {
		return tierNames.size();
	}
}
