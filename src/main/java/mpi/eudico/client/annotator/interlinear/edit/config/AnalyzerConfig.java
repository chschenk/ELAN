package mpi.eudico.client.annotator.interlinear.edit.config;

import java.util.ArrayList;
import java.util.List;

import nl.mpi.lexan.analyzers.helpers.Information;

/**
 * Very preliminary and temporary config class.
 *
 */
public class AnalyzerConfig {
	protected Information annotId;
	protected String src;
	protected List<String> dest;
	
	/**
	 * This type is immutable.
	 * 
	 * @param annotId
	 * @param src
	 * @param dest
	 */
	public AnalyzerConfig(Information annotId, String src, List<String> dest) {
		super();
		this.annotId = annotId;
		this.src = src;
		this.dest = dest;
	}
	
	public Information getAnnotId() {
		return annotId;
	}

	public String getSource() {
		return src;
	}

	public List<String> getDest() {
		return dest;
	}
	
	public boolean isTypeMode() {
		return false;
	}

	/**
	 * This method is mainly of interest in the derived type
	 * AnalyzerTypeConfig. To avoid needing to check isTypeMode()
	 * and casting, this method is provided as a compatibility helper.
	 * 
	 * @return a list with one member: this object itself.
	 */
	public List<AnalyzerConfig> getTierConfigurations() {
		List<AnalyzerConfig> list = new ArrayList<AnalyzerConfig>(1);
		list.add(this);

		return list;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotId == null) ? 0 : annotId.hashCode());
		result = prime * result + ((dest == null) ? 0 : dest.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		return result;
	}

	/**
	 * Equality is based in all (immutable) members, and also on
	 * isTypeMode(). This is to ensure that an AnalyzerConfig object is never
	 * equal to an AnalyzerTypeConfig object.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AnalyzerConfig)) {
			return false;
		}
		AnalyzerConfig other = (AnalyzerConfig) obj;
		// .equals(AnalyzerTypeConfig) = false
		if (isTypeMode() != other.isTypeMode()) {
			return false;
		}
		if (annotId == null) {
			if (other.annotId != null) {
				return false;
			}
		} else if (!annotId.equals(other.annotId)) {
			return false;
		}
		if (dest == null) {
			if (other.dest != null) {
				return false;
			}
		} else if (!dest.equals(other.dest)) {
			return false;
		}
		if (src == null) {
			if (other.src != null) {
				return false;
			}
		} else if (!src.equals(other.src)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		String name = isTypeMode() ? "AnalyzerTypeConfig" : "AnalyzerConfig";
		
		return name + "[" + annotId + ": " + String.valueOf(src) + " -> " + String.valueOf(dest) + "]";
	
	}
}
