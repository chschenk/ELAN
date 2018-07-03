package mpi.eudico.util;
/**
 * Class that performs basic xml escaping for "hand made" xml.
 * Temporary facility, probably.
 * 
 * @author Han Sloetjes
 */
public class XMLEscape {
    public final String LESS = "&lt;";
    public final String GREAT = "&gt;";
    public final String AMP = "&amp;";
    public final String QUOT = "&quot;";
    public final String APOS = "&apos;";
    
    public final String LESS_NUM = "&#60;";
    public final String GREAT_NUM = "&#62;";
    public final String AMP_NUM = "&#38;";
    public final String QUOT_NUM = "&#34;";
    public final String APOS_NUM = "&#39;";
    
    private final char CH_LESS = '<';
    private final char CH_GREAT = '>';
    private final char CH_AMP = '&';
    private final char CH_QUOT = '\"';
    private final char CH_APOS = '\'';
    private final char CH_SPACE = '\u0020';
    
    
	public XMLEscape() {
		super();
	}

    /**
     * Replaces reserved characters (e.g. ">" and "<" etc.) in the input string by standard 
     * xml entity strings (e.g. "&gt;" or "&lt;").
     * 
     * @param input the input text
     * @return the escaped string
     */
    public String escape(String input) {
    	if (input == null) {
    		return null;
    	}
    	
    	StringBuilder builder = new StringBuilder();
    	char c;
    	
    	for (int i = 0; i < input.length(); i++) {
    		c = input.charAt(i);
    		if (c < CH_SPACE) {
    			continue;
    		}
    		switch (c) {
    		case CH_LESS:
    			builder.append(LESS);
    			break;
    		case CH_GREAT:
    			builder.append(GREAT);
    			break;
    		case CH_AMP:
    			builder.append(AMP);
    			break;
    		case CH_APOS:
    			builder.append(APOS);
    			break;
    		case CH_QUOT:
    			builder.append(QUOT);
    			break;
    			default:
    				builder.append(c);
    		}
    	}
    	
    	if (builder.length() != input.length()) {
    		return builder.toString();
    	} else {
    		return input;
    	}
    } 

    /**
     * Replaces reserved characters (e.g. ">" and "<" etc.) in the input string by decimal representation of 
     * the character code points (e.g. "&gt;" or "&lt;").
     * 
     * @param input the input text
     * @return the escaped string
     */
    public String escapeNumeric(String input) {
    	if (input == null) {
    		return null;
    	}
    	
    	StringBuilder builder = new StringBuilder();
    	char c;
    	
    	for (int i = 0; i < input.length(); i++) {
    		c = input.charAt(i);
    		if (c < CH_SPACE) {
    			continue;
    		}
    		switch (c) {
    		case CH_LESS:
    			builder.append(LESS_NUM);
    			break;
    		case CH_GREAT:
    			builder.append(GREAT_NUM);
    			break;
    		case CH_AMP:
    			builder.append(AMP_NUM);
    			break;
    		case CH_APOS:
    			builder.append(APOS_NUM);
    			break;
    		case CH_QUOT:
    			builder.append(QUOT_NUM);
    			break;
    			default:
    				builder.append(c);
    		}
    	}
    	
    	if (builder.length() != input.length()) {
    		return builder.toString();
    	} else {
    		return input;
    	}
    } 
}
