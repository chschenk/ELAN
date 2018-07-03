package mpi.eudico.client.annotator.spellcheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.Pair;

import com.atlascopco.hunspell.Hunspell;

/**
 * A wrapper for the Hunspell BridJ implementation from https://github.com/thomas-joiner/HunspellBridJ.
 * It needs a locale and a local file path to the .dic and .aff files to create a dictionary
 * 
 * @author michahulsbosch
 *
 */
public class HunspellChecker implements SpellChecker {
	private String description = "A spellchecker that uses localy installed Hunspell dictionaries. See also http://hunspell.github.io/";
	
	private SpellCheckerType type = SpellCheckerType.HUNSPELL;
	private String filePath;
	
	private Hunspell dict;
	
	private Set<String> newWords = new HashSet<String>();
	
	public HunspellChecker(String filePath) {
		if(filePath.endsWith(".dic")) {
			this.filePath = filePath.substring(0, filePath.length() - 4); 
		} else {
			this.filePath = filePath;
		}
		
	}
	
	/**
	 * Creates a HunspellChecker with args if they are correct
	 * @param args
	 * @return 
	 */
	public static HunspellChecker create(HashMap<String, String> args) {
		// Assume args contains the following keys:
		// * path 
		if(args.containsKey("path")) {
			return new HunspellChecker(args.get("path"));
		}
		return null;
	}
	
	/**
	 * Gives the necessary data fields for creating an instance, and their locale reference
	 * @return
	 */
	public static ArrayList<Pair<String, String>> getDataFields() {
		ArrayList<Pair<String, String>> fields = new ArrayList<Pair<String, String>>();
//		fields.add(new Pair<String, String>("language", ""Button.Language""));
//		fields.add(new Pair<String, String>("region", "HunspellChecker.DataField.Region"));
		fields.add(new Pair<String, String>("path", "HunspellChecker.DataField.Path"));
		return fields;
	}
	
	/**
	 * Initializes this spellchecker by creating a Hunspell dictionary
	 */
	@Override
	public void initializeSpellChecker() throws SpellCheckerInitializationException {
		//String path = filePath + locale.toString();
		try {
			dict = new Hunspell(filePath + ".dic", filePath + ".aff");
		} catch (UnsatisfiedLinkError e) {
			throw new SpellCheckerInitializationException("No Hunspell dictionary could be opened from " + filePath + " - " + e.getMessage(), e);
		} catch (UnsupportedOperationException e) {
			throw new SpellCheckerInitializationException("No Hunspell dictionary could be opened from " + filePath + " - " + e.getMessage(), e);
		}
	}
	
	@Override
	public void setType(SpellCheckerType type) {
		this.type = type;
	}

	@Override
	public SpellCheckerType getType() {
		return type;
	}

	@Override
	public String getInfo() {
		return filePath;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public String getPreferencesString() {
		return type + "," + filePath;
	}
	
	@Override
	public String toString() {
		return "Hunspell: " + getInfo();
	}

	@Override
	public List<Pair<String, List<String>>> getSuggestions(String text) {
		List<Pair<String, List<String>>> suggestions = new ArrayList<Pair<String, List<String>>>();
		
		// Simple split on one or more spaces
		String[] words = text.split("\\b");
		
		for(int i = 0; i < words.length; i++) {
			String word = words[i];
			if (word.matches(".*\\p{L}.*")) {
				List<String> wordSuggestions;
				if (!isCorrect(word)) {
					wordSuggestions = dict.suggest(word);
				} else {
					wordSuggestions = new ArrayList<String>();
				}
				suggestions.add(new Pair<String, List<String>>(word, wordSuggestions));
			}
		}
		
		return suggestions;
	}

	@Override
	public Boolean isCorrect(String text) {
		// Simple split on one or more spaces
		String[] words = text.split("\\s+");
		
		for(int i = 0; i < words.length; i++) {
			String word = words[i];
			if(!dict.spell(word)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void addUserDefinedWord(String word) {
		newWords.add(word);
	}

	@Override
	public Set<String> getUserDefinedWords() {
		return newWords;
	}

}
