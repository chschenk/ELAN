package mpi.eudico.client.annotator.interlinear.edit.config;

import java.util.ArrayList;
import java.util.List;

import nl.mpi.lexan.analyzers.helpers.Information;

/**
 * This class extends AnalyzerConfig by encapsulating tier-type-based 
 * configurations. This means that instead of individual tier names 
 * tier types are stored as the source or one of the targets of an 
 * analyzer. By the time this information is passed to the analyzers
 * each type is resolved to a set of tiers that are of that type.
 * Analyzers and the LEXAN API are unaware of the concept of tier types,
 * they just work with tier names, embedded in a Position object.
 * 
 * @see AnalyzerConfig
 */
public class AnalyzerTypeConfig extends AnalyzerConfig {
	/**
	 * The tiers that are implied by the types. 
	 * <p>
	 * Derived data, excluded from hashCode and equals.
	 */
	protected List<AnalyzerConfig> tierConfList;
	/**
	 * Derived data, excluded from hashCode and equals.
	 */
	private List<String> excludedTiersList;
	
	/**
	 * @param annotId
	 * @param src
	 * @param dest
	 */
	public AnalyzerTypeConfig(Information annotId, String src, List<String> dest) {
		super(annotId, src, dest);	
		
		tierConfList = new ArrayList<AnalyzerConfig>();
		excludedTiersList = new ArrayList<String>();
	}
	
	@Override
	public boolean isTypeMode() {
		return true;
	}

	/**
	 * Return the actual tiers that are implied by the configured tier types.
	 * <p>
	 * These are not considered for equality of Annot(Type)Configs.
	 * However, an AnalyzerConfig can never be equal to an AnalyzerTypeConfig.
	 */
	@Override
	public List<AnalyzerConfig> getTierConfigurations() {
		return tierConfList;
	}
	
	public List<String> getExcludedTierList() {
		return excludedTiersList;
	}
	
	public void addTierConf(AnalyzerConfig ac) {
		if (!tierConfList.contains(ac)) {
			tierConfList.add(ac);
		}
		
		excludedTiersList.remove(ac.getSource());
	}
	
	public boolean removeTierConf(AnalyzerConfig ac) {
		boolean removed = tierConfList.remove(ac);
		
		if (removed && !excludedTiersList.contains(ac.getSource())) {
			excludedTiersList.add(ac.getSource());
		}
		
		return removed;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result += prime;	// because isTypeMode() == true
//		result = prime
//				* result
//				+ ((excludedTiersList == null) ? 0 : excludedTiersList
//						.hashCode());
//		result = prime * result
//				+ ((tierConfList == null) ? 0 : tierConfList.hashCode());
		return result;
	}
	
	/**
	 * Equality does not take into consideration the derived fields
	 * tierConfList and excludedTiersList. Therefore it is safe if they get
	 * modified while the object is in a container.
	 * <p>
	 * Therefore the only fields participating in equality are those of
	 * the immutable super type AnalyzerConfig.
	 * However, an AnalyzerConfig can never be equal to an AnalyzerTypeConfig.
	 */

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof AnalyzerTypeConfig)) {
			return false;
		}
//		AnalyzerTypeConfig other = (AnalyzerTypeConfig) obj;
//		if (excludedTiersList == null) {
//			if (other.excludedTiersList != null) {
//				return false;
//			}
//		} else if (!excludedTiersList.equals(other.excludedTiersList)) {
//			return false;
//		}
//		if (tierConfList == null) {
//			if (other.tierConfList != null) {
//				return false;
//			}
//		} else if (!tierConfList.equals(other.tierConfList)) {
//			return false;
//		}
		return true;
	}
}
