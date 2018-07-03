/*
 * Created on Jul 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.io.Serializable;

/**
 * @author hennie
 */
public class MarkerRecord implements Serializable {
	// character encoding
	public static final int UTF8 = 0;
	public static final int ISOLATIN = 1;
	public static final int SILIPA = 2;

	public static final String ISOLATINSTRING = "ISO-Latin-1";
	public static final String UNICODESTRING = "Unicode (UTF-8)";
	public static final String SILIPASTRING = "SIL IPA";
	
	public static final String[] charsetStrings = {ISOLATINSTRING, UNICODESTRING, SILIPASTRING};
	
	private String marker;
	private String parentMarker;
	private String stereoType;
	private String charsetString;
	private boolean participantMarker = false;
	private boolean exclude = false;
		
	/**
	 * @return
	 */
	public String getCharsetString() {
		return charsetString;
	}
	
	public int getCharset() {
		int charset = -1;
		if (charsetString.equals(ISOLATINSTRING)) {
			charset = ISOLATIN;
		}
		else if (charsetString.equals(UNICODESTRING)) {
			charset = UTF8;
		}
		else if (charsetString.equals(SILIPASTRING)) {
			charset = SILIPA;
		}
		
		return charset;
	}

	/**
	 * @return
	 */
	public String getMarker() {
		return marker;
	}

	/**
	 * @return
	 */
	public String getParentMarker() {
		return parentMarker;
	}

	/**
	 * @return
	 */
	public String getStereoType() {
		return stereoType;
	}

	public boolean getParticipantMarker() {
		return participantMarker;
	}
	
	public boolean isExcluded() {
		return exclude;
	}
	
	/**
	 * @param string
	 */
	public void setCharset(String charset) {
		this.charsetString = charset;
	}
	

	/**
	 * @param string
	 */
	public void setMarker(String string) {
		marker = string;
	}

	/**
	 * @param string
	 */
	public void setParentMarker(String string) {
		parentMarker = string;
	}

	/**
	 * @param string
	 */
	public void setStereoType(String string) {
		stereoType = string;
	}
	
	public void setParticipantMarker(boolean bool) {
		participantMarker = bool;
	}
	
	public void setExcluded(boolean bool) {
		exclude = bool;
	}
	
	@Override
	public String toString() {
		return  "marker:      " + marker + "\n" +
				"parent:      " + parentMarker + "\n" +
				"stereotype:  " + stereoType + "\n" +
				"charset:     " + charsetString + "\n" +
				"exclude:     " + exclude + "\n" +
				"participant: " + participantMarker + "\n";
	}
}
