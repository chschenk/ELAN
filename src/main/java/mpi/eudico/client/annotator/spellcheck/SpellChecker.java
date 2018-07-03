package mpi.eudico.client.annotator.spellcheck;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import mpi.eudico.util.Pair;

/** 
 * 
 * @author michahulsbosch
 *
 */
public interface SpellChecker {
	public void initializeSpellChecker() throws SpellCheckerInitializationException;
	public void setType(SpellCheckerType type);
	public SpellCheckerType getType();
	public void setDescription(String description);
	public String getDescription();
	public String getInfo();
	public String getPreferencesString();
	public List<Pair<String, List<String>>> getSuggestions(String text);
	public Boolean isCorrect(String text);
	public void addUserDefinedWord(String word);
	public Set<String> getUserDefinedWords();
}
