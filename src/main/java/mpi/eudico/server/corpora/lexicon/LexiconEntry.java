package mpi.eudico.server.corpora.lexicon;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Represents an entry in a Lexicon structure
 * @author Micha Hulsbosch
 *
 */
public class LexiconEntry  extends EntryElement {
	private String id;
	private String fieldOfFocus;
	private ArrayList<String> focusFieldValues;

	public LexiconEntry(String name) {
		super(name, null);
		id = null;
		fieldOfFocus = null;
		focusFieldValues = new ArrayList<String>();
	}
	
	/**
	 * @return the isField
	 */
	@Override
	public boolean isField() {
		return false;
	}

	/**
	 * @param isField the isField to set
	 */
	@Override
	public void setField(boolean isField) {
		this.isField = false;
	}
	
	@Override
	public String getName() {
		return "Entry";
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		String str = "<html><b>" + getName() + "</b> (" + fieldOfFocus + ": ";
		Iterator iter = focusFieldValues.iterator();
		StringBuilder buffer = new StringBuilder();
		if (iter.hasNext()) {
		    buffer.append("<i>" + iter.next() + "</i>");
		    while (iter.hasNext()) {
			buffer.append(" or ");
			buffer.append("<i>" + iter.next() + "</i>");
		    }
		}
		str += buffer.toString();
		str += ")</html>";
		return str;
	}

	public void setFieldOfFocus(String fieldOfFocus) {
		this.fieldOfFocus = fieldOfFocus;
	}

	/**
	 * @return the fieldOfFocus
	 */
	public String getFieldOfFocus() {
		return fieldOfFocus;
	}

	public void addFocusFieldValue(String value) {
		focusFieldValues.add(value);
	}

	/**
	 * @return the focusFieldValues
	 */
	public ArrayList<String> getFocusFieldValues() {
		return focusFieldValues;
	}
}
