package mpi.eudico.client.annotator.recognizer.data;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for textual parameters.
 * 
 * @author Han Sloetjes
 */
public class TextParam extends Param {
	/** the default value */
	public String defValue;
	/** a controlled vocabulary */
	public List<String> conVoc;
	/** the current or stored value */
	public String curValue;
	
	/**
	 * Constructor.
	 */
	public TextParam() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id the id
	 * @param info the description
	 */
	public TextParam(String id, String info) {
		super(id, info);
	}


	/**
	 * Creates a clone of this object.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		TextParam clonePar = new TextParam(this.id, this.info);
		
		clonePar.defValue = this.defValue;
		clonePar.curValue = this.curValue;
		clonePar.level = this.level;
		if (this.conVoc != null) {
			clonePar.conVoc = new ArrayList<String>(this.conVoc);
		}
		
		return clonePar;
	}
}
