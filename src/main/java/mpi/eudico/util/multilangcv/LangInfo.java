package mpi.eudico.util.multilangcv;

import java.util.Comparator;

/**
 * A small collection of information about a language.
 * 
 * @author olasei
 */
public class LangInfo {
	private String id;
	private String longId;
	private String label;
	
	/**
	 * Construct a LangInfo. null strings are not allowed.
	 */

	public LangInfo(String id, String longId, String label) {
		this.id = id;
		this.longId = longId;
		this.label = label;
	}
	
	public LangInfo(LangInfo other) {
		this.id = other.id;
		this.longId = other.longId;
		this.label = other.label;   		
	}

	/**
	 * Id of the language,
	 * for instance "nld"
	 */
	public String getId() {
		return id;
	}

	/**
	 * Persistent Identifier of the language, URL form,
	 * for instance "http://cdb.iso.org/lg/CDB-00138580-001".
	 */
	public String getLongId() {
		return longId;
	}

	/**
	 * A name or description of the language,
	 * for instance "Dutch (nld)".
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label. null is not allowed.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * For use in comboboxes and the like.
	 */
	@Override
	public String toString() {
		return getLabel() + " - " + getId() + " - " + getLongId();	
		
	}
	
	public static Comparator<LangInfo> getIdComparator() {
		return new Comparator<LangInfo>() {
			@Override
			public int compare(LangInfo o1, LangInfo o2) {
				return o1.getId().compareTo(o2.getId());
			}
		};
	}

	public static Comparator<LangInfo> getLabelComparator() {
		return new Comparator<LangInfo>() {
			@Override
			public int compare(LangInfo o1, LangInfo o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		};
	}

	/**
	 * Behaves like an equals() method would, but without calling it that.
	 * Which saves us from having to override hashCode() and changing behaviour in collections.
	 * @param li
	 * @return
	 */
	public boolean valueEquals(LangInfo li) {
		return id.equals(li.id) && 
				longId.equals(li.longId) && 
				label.equals(li.label);
	}

}
