package mpi.eudico.util;

/**
 * An int wrapper, a mutable variant of Integer.
 * 
 * @author Han Sloetjes
 */
public class MutableInt {
	/** public access to avoid getter and setter */
	public int intValue;

	/**
	 * Constructor.
	 */
	public MutableInt() {
		super();
		intValue = 0;
	}

	/**
	 * Constructor.
	 * 
	 * @param intValue the new value
	 */
	public MutableInt(int intValue) {
		super();
		this.intValue = intValue;
	}

}
