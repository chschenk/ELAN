package mpi.eudico.client.annotator.turnsandscenemode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaSSpecialMarkers {
	/** a combination to mark the start of a translation in the same annotation */
	public static final String TRANS_MARKER = "//";
	/** a String pattern marking different speakers. The general pattern is <code>X@Y:</code> 
	 * where X is optional and cannot contain white spaces and Y should be at least
	 * one character long and should also not contain white spaces. The whole pattern should be 
	 * terminated by a colon. This pattern can start anywhere on a line*/
	public static final String SPEAKER_MARKER = "[^@\\s]*@[^:\\s@]+:";
	/* Alternative patterns
	 * 1) after the @ a string with spaces is allowed if the name is terminated by a ":",
	 * otherwise the first space marks the end of the suffix and a ":" is not required
	 * "[^@\\s]*@(([^:]+:)|([^:\\s]+:*))"
	 * 2) white spaces allowed in the prefix (only useful if the marker is expected
	 * at the beginning of a new line), the suffix has to be terminated with ":", spaces allowed
	 * "[^@]*@[^:]+:"
	 * 3) no white spaces allowed in the optional prefix, no white spaces in the suffix,
	 * terminating colon is optional
	 * "[^@\\s]*@[^:\\s]+:*"
	 */
	
	/** a compiled Pattern object of the SPEAKER_MARKER */
	public static final Pattern SPEAKER_PATTERN = Pattern.compile(SPEAKER_MARKER);
	
	/**
	 * a flag to indicate whether speaker markers are expected and supported only
	 * at the start of a new line
	 */
	public static boolean speakerMarkerOnlyAtLineStart = false;
	
	
	/**
	 * Scans the text for occurrences of the marker that indicates that a translation
	 * starts (Default: "//"). Per speaker line/fragment only one marker is recognized, the first one.
	 * 
	 * @param text the input text, can consist of multiple lines
	 * @return an array of indices (character locations in the text) of the first 
	 * character of the translation marker string
	 */
	public static int[] getTranslationIndices(String text) {
		if (text == null || text.isEmpty()) {
			return null;
		}
		
		if (text.indexOf(TaSSpecialMarkers.TRANS_MARKER) > -1) {
			Matcher m = TaSSpecialMarkers.SPEAKER_PATTERN.matcher(text);
			List<Integer> indicesList = new ArrayList<Integer>(10);
			int index = text.indexOf(TaSSpecialMarkers.TRANS_MARKER);
			indicesList.add(index);
			
			while (index > -1) {
				// look for next tier name marker
				
				if (m.find(index)) {
					index = text.indexOf(TaSSpecialMarkers.TRANS_MARKER, m.end());
					if (index > -1) {
						indicesList.add(index);
					}
				} else {
					break;
				}
				// look for a new line after index
//				int nextNL = text.indexOf('\n', index);
//				if (nextNL < 0) {
//					break;
//				}
				// look for the marker after that new line
//				index = text.indexOf(TaSSpecialMarkers.TRANS_MARKER, nextNL);
//				if (index > -1) {
//					indicesList.add(index);
//				}
			}

			int[] indices = new int[indicesList.size()];
			for (int j = 0; j < indicesList.size(); j++) {
				indices[j] = indicesList.get(j);
			}

			indicesList.clear();		
			return indices;				
		}
			
		return null;
	}
	
	/**
	 * Performs a quick test on the existence of the Translation Marker
	 * in the text.
	 * 
	 * @param text the text to test
	 * @return true if the marker is found at least once
	 */
	public static boolean hasTranslationMarker(String text) {
		if (text != null && !text.isEmpty()) {
			return text.indexOf(TaSSpecialMarkers.TRANS_MARKER) > -1;
		}
		
		return false;
	}
	
	/**
	 * Scans all lines of the text to see if they start with or contain a pattern that
	 * is defined as a speaker (plus optionally tier) label.
	 * See SPEAKER_MARKER comments for the pattern, it is roughly "tier_label@speaker:"
	 * 
	 * @param text the input text, can contain multiple lines
	 * @return a two dimensional array of all detected speaker markers, 
	 * each marker identified by a begin and end index of the speaker label 
	 * (can be of variable length)
	 */
	public static int[][] getSpeakerIndices(String text) {
		if (text == null || text.isEmpty()) {
			return null;
		}
		Matcher m = TaSSpecialMarkers.SPEAKER_PATTERN.matcher(text);
		List<int[]> speakIndicesList = new ArrayList<int[]>(10);
		
		//System.out.println("IN: " + text);
		int fromIndex = 0;
		while (m.find(fromIndex)) {		
			if (speakerMarkerOnlyAtLineStart) {
				if (m.start() == fromIndex) {
					int[] match = new int[2];
					match[0] = m.start();
					match[1] = m.end();
					speakIndicesList.add(match);
				}
				// set the from index to the start of the next new line
				// so there is only one speaker per line
				fromIndex = text.indexOf('\n', fromIndex);
				if (fromIndex < 0) {
					break;
				}
				fromIndex++;
			} else {
				int[] match = new int[2];
				match[0] = m.start();
				match[1] = m.end();
				speakIndicesList.add(match);
				fromIndex = m.end() + 1;
			}
			if (fromIndex >= text.length() - 1) {
				break;
			} 
		}
		
		if (!speakIndicesList.isEmpty()) {
			int[][] matches = new int[speakIndicesList.size()][];
			for (int j = 0; j < speakIndicesList.size(); j++) {
				matches[j] = speakIndicesList.get(j);
			}
			speakIndicesList.clear();
			return matches;
		}
		
		return null;
	}
	
	/**
	 * A quick test to see if there is at least one speaker marker in the text
	 * given the pattern and the flag for "marker at line start".
	 * 
	 * @param text the text to query
	 * @return true if the pattern was found false otherwise
	 */
	public static boolean hasSpeakerMarker(String text) {
		if (text != null && !text.isEmpty()) {
			Matcher m = TaSSpecialMarkers.SPEAKER_PATTERN.matcher(text);
			
			int fromIndex = 0;
			while (m.find(fromIndex)) {
				if (speakerMarkerOnlyAtLineStart) {
					if (m.start() == fromIndex) {
						return true;
					} else {
						// set the from index to the start of the next new line
						fromIndex = text.indexOf('\n', fromIndex) + 1;
						if (fromIndex <= 0 || fromIndex >= text.length() - 1) {
							break;
						}
					}
				} else {
					// found anywhere 
					return true;
				}
			}

			return false;
		}
		
		return false;
	}
	
}
