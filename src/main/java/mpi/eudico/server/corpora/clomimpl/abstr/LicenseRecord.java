package mpi.eudico.server.corpora.clomimpl.abstr;

/**
 * A class to store the information from a single &lt;LICENSE>
 * element in an EAF file.
 *  
 * @author olasei
 */

public class LicenseRecord {
	private String url;
	private String text;
	
	/**
	 * @return the url. Not null.
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url The url to of the license set. Not null.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return The text. Not null.
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text The text of the license to set. Not null.
	 */
	public void setText(String text) {
		this.text = text;
	}	
}
