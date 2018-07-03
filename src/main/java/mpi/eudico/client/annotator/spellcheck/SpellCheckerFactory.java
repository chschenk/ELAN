package mpi.eudico.client.annotator.spellcheck;

import java.util.ArrayList;
import java.util.HashMap;

import mpi.eudico.util.Pair;

/**
 * Creates spell checkers from a list of data 
 * @author micha
 *
 */
public class SpellCheckerFactory {
	/**
	 * List of all spell checker type
	 * Each entry should correspond with a class implementing the SpellChecker interface 
	 * @author micha
	 *
	 */
	enum SpellCheckerType {
		HUNSPELL,
		GECCO;
		
		@Override
		public String toString() {
			return this.name().substring(0, 1) + this.name().substring(1).toLowerCase();
		}
	}
	
	/**
	 * Asks the corresponding class for the apropriate data fields and return them
	 * @param type
	 * @return
	 */
	public static ArrayList<Pair<String, String>> getDataFields(SpellCheckerType type) {
		switch(type) {
		case HUNSPELL: return HunspellChecker.getDataFields();
		case GECCO: return GeccoClient.getDataFields();
		}
		return null;
	}
	
	/**
	 * Creates a spell checker for the type, using the args
	 * @param type
	 * @param args
	 * @return
	 */
	public static SpellChecker create(SpellCheckerType type, HashMap<String, String> args) {
		switch(type) {
		case HUNSPELL: return HunspellChecker.create(args);
		case GECCO: return GeccoClient.create(args);
		}
		return null;
	}
	
	public static String[] getTypes() {
		String[] types = new String[SpellCheckerType.values().length];
		int i = 0;
		for(SpellCheckerType type : SpellCheckerType.values()) {
			types[i] = type.name();
			i++;
		}
		return types;
	}
}
