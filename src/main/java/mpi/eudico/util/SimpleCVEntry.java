package mpi.eudico.util;

import mpi.eudico.server.corpora.clom.ExternalReference;

/**
 * A simple, one-language version of a CVEntry.
 * <p>
 * This is useful to put into the InlineEditBox or other GUI elements.
 * At creation time you can choose which language should be used.
 * 
 * @author olasei
 */
public class SimpleCVEntry {
	private CVEntry ref;
	private String value;
	private String description;
	
	public SimpleCVEntry(CVEntry e, int langIndex) {
		ref = e;
		value = e.getValue(langIndex);
		description = e.getDescription(langIndex);
	}

	/**
	 * @return the value
	 */
	@Override
	public String toString() {
		return value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return ref.getId();
	}

	/**
	 * @return the extRef
	 */
	public ExternalReference getExternalRef() {
		return ref.getExternalRef();
	}

	/**
	 * @return the shortcutKeyCode
	 */
	public int getShortcutKeyCode() {
		return ref.getShortcutKeyCode();
	}
}
