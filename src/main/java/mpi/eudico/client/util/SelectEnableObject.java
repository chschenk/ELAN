package mpi.eudico.client.util;

/**
 * An object with an additional member for the enabled state.
 * 
 * @author Han Sloetjes
 */
public class SelectEnableObject<T> extends SelectableObject<T> {
	private boolean enabled = true;

	/**
	 * Constructor, enabled is the default.
	 */
	public SelectEnableObject() {
		super();
	}

	/**
	 * Constructor, enabled is the default.
	 * 
	 * @param value the value 
	 * @param selected the selected state
	 */
	public SelectEnableObject(T value, boolean selected) {
		super(value, selected);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param value the value
	 * @param selected the selected state
	 * @param enabled the enabled state
	 */
	public SelectEnableObject(T value, boolean selected, boolean enabled) {
		super(value, selected);
		this.enabled = enabled;
	}

	/**
	 * Sets the enabled state.
	 * 
	 * @return the enable state
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Returns the enabled state.
	 * 
	 * @param enabled the enabled state
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
