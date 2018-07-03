package mpi.eudico.client.annotator.spellcheck;

import java.util.List;

import mpi.eudico.util.Pair;

public class SpellCheckerUtil {

	public static final String serializeSuggestions(List<Pair<String, List<String>>> allSuggestions) {
		StringBuilder stringBldr = new StringBuilder();
		for(Pair<String, List<String>> suggestions : allSuggestions) {
			stringBldr.append(suggestions.getFirst() + "\n");
			for(String suggestion : suggestions.getSecond()) {
				stringBldr.append("  " + suggestion + "\n");
			}
		}
		return stringBldr.toString();
	}
	
	/**
	 * Determines whether a suggestions data structure actually contains suggestions
	 * for one or more words. 
	 * @param allSuggestions
	 * @return
	 */
	public static final Boolean hasSuggestions(List<Pair<String, List<String>>> allSuggestions) {
		for(Pair<String, List<String>> suggestions : allSuggestions) {
			if(!suggestions.getSecond().isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	public static final Boolean isSuggestion(Pair<String, List<String>> suggestions) {
		if(!suggestions.getSecond().isEmpty()) {
			return true;
		}
		return false;
	}
} 
