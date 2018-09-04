package mpi.eudico.client.annotator.lexicon.api;

import mpi.eudico.client.annotator.lexicon.api.Param;

/**
 * Holds a parameter for the Lexicon Service CMDI parser
 * @author Micha Hulsbosch
 *
 */
public class Param {
	private String type;
	private String content;
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Creates a clone of this object.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		Param clonePar = new Param();
		clonePar.setType(type);
		clonePar.setContent(content);

		return clonePar;
	}
}
