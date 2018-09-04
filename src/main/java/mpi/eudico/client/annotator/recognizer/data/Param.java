package mpi.eudico.client.annotator.recognizer.data;

/**
 * Lightweight base class for recognizer parameter. 
 * All members are public.
 * 
 * @author Han Sloetjes
 */
public abstract class Param {
	/** the identifier of the parameter */
	public String id;
	/** Information string for ui etc. */
	public String info;
	/** the level of the parameter */
	public String level = Param.BASIC ;	
	
	/** level constants*/
	public static String BASIC= "basic";
	public static String ADVANCED= "advanced";
	
	/**
	 * Constructor
	 */
	public Param() {
		super();
	}
	
	/**
	 * Constructor.
	 * @param id the id
	 * @param info the information string
	 */
	public Param(String id, String info) {
		this(id, info, Param.BASIC);
	}
	
	/**
	 * Constructor.
	 * @param id the id
	 * @param info the information string
	 * @param level the level of the param
	 */
	public Param(String id, String info, String level) {
		super();
		this.id = id;
		this.info = info;
		this.level = level;
	}
	
	@Override
	public abstract Object clone() throws CloneNotSupportedException;	
	
}
