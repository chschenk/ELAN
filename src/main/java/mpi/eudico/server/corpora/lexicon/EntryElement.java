package mpi.eudico.server.corpora.lexicon;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//import mpi.eudico.client.util.BrowserLaunch;

/**
 * Represents an element in a Lexicon structure
 * @author Micha Hulsbosch
 *
 */
public class EntryElement {
	protected boolean isField;
	protected  String name;
	protected  String value;
	protected  List<EntryElement> elements;
	protected  EntryElement parent;
	
	public EntryElement(String name, EntryElement parent) {
		this.parent = parent;
		isField = true;
		this.name = name;
		this.value = "";
		elements = new ArrayList<EntryElement>();
	}
	
	public void addElement(EntryElement element) {
		elements.add(element);
		isField = false;
	}

	/**
	 * @return the isField
	 */
	public boolean isField() {
		return isField;
	}

	/**
	 * @param isField the isField to set
	 */
	public void setField(boolean isField) {
		this.isField = isField;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the elements
	 */
	public List<EntryElement> getElements() {
		return elements;
	}

	/**
	 * @param elements the elements to set
	 */
	public void setElements(List<EntryElement> elements) {
		this.elements = elements;
	}

	/**
	 * @return the parent
	 */
	public EntryElement getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(EntryElement parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		String str = "<html><b>" + name + "</b>";
		if(value != null && !value.equals("")) {
			try {
				URL url = new URL(value);
				str += ": <i><a href=\"" + value + "\">" + value + "</a></i>";
			} catch (MalformedURLException mue) {
				// Apparently the value was not a URL; Do nothing
				str += ": <i>" + value + "</i>";
			}
		}
		str += "</html>";
		return str;
	}
}
