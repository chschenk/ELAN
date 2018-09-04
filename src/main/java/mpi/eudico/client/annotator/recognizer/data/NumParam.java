package mpi.eudico.client.annotator.recognizer.data;

/**
 * A class for numerical parameters. 
 * 
 * @author Han Sloetjes
 */
public class NumParam extends Param {
	/** the minimum value */
	public float min;
	/** the maximum value */
	public float max;
	/** the default value */
	public float def;//default
	/** the current, user provided or stored value */
	public float current = Float.MIN_VALUE;
	/** the number of decimal positions */
	public int precision = 1;
	/** the type used to represent the values in the UI */
	public String type = FLOAT;
	
	/** type constants used as a type */
	public static String INT= "int";
	public static String FLOAT= "float";
	
	/**
	 * Constructor.
	 */
	public NumParam() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id the id
	 * @param info the description
	 */
	public NumParam(String id, String info) {
		super(id, info);
	}


	/**
	 * Creates a clone of this object.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		NumParam clonePar = new NumParam(this.id, this.info);

		clonePar.min = this.min;
		clonePar.max = this.max;
		clonePar.def = this.def;
		clonePar.current = this.current;
		clonePar.precision = this.precision;
		clonePar.level = this.level;
		clonePar.type = this.type;
		
		return clonePar;
	}
}
