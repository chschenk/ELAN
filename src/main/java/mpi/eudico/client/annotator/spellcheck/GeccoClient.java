package mpi.eudico.client.annotator.spellcheck;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.Pair;

/**
 * Uses a Gecco Spellchecker webservice (https://github.com/proycon/gecco) through CLAM (http://proycon.github.io/clam/) 
 * @author michahulsbosch
 *
 */
public class GeccoClient extends ClamClient implements SpellChecker {
	SpellCheckerType type = SpellCheckerType.GECCO;
	
	private String description = "A spellchecker that uses a Gecco Spellchecker webservice through CLAM. See also https://github.com/proycon/gecco";
	
	/** Contains cached suggestions that were analyzed before */
	HashMap<String, List<Pair<String, List<String>>>> cachedSuggestions = new HashMap<String, List<Pair<String,List<String>>>>();
	
	public GeccoClient(String host, String protocol, String path, String username, String password) {
		super(host, protocol, path, username, password);
	}
	
	/**
	 * Creates a GeccoClient with args if they are correct
	 * @param args
	 * @return
	 */
	public static GeccoClient create(HashMap<String, String> args) {
		if(args.containsKey("url")
				&& args.containsKey("username")
				&& args.containsKey("password")) {
			try {
				URL url = new URL(args.get("url"));
				return new GeccoClient(url.getHost(), url.getProtocol(), url.getPath(), args.get("username"), args.get("password"));
			} catch (MalformedURLException e) {
				if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
	            	ClientLogger.LOG.warning("The url " + args.get("url") + " is malformed (" + e.getMessage() + ")");
	            }
			}
		}
		return null;
	}
	
	@Override
	public void initializeSpellChecker() throws SpellCheckerInitializationException {
		super.initialize();
	}
	
	/**
	 * Gives the necessary data fields for creating an instance, and their locale reference
	 * @return
	 */
	public static ArrayList<Pair<String, String>> getDataFields() {
		ArrayList<Pair<String, String>> fields = new ArrayList<Pair<String, String>>();
		fields.add(new Pair<String, String>("url", "GeccoClient.DataField.Url"));
		fields.add(new Pair<String, String>("username", "GeccoClient.DataField.Username"));
		fields.add(new Pair<String, String>("password", "GeccoClient.DataField.Password"));
		return fields;
	}
		
	/**
	 * Processes a single sentence without creating a CLAM project first.
	 * Note that only tokens that get suggestions end up in the returned 
	 * data structure. This is due to the workings of this part of Gecco. 
	 * @param sentence
	 */
	public List<Pair<String, List<String>>> processSentence(String sentence) {
		if (ClientLogger.LOG.isLoggable(Level.FINE)) {
			ClientLogger.LOG.fine(sentence);	
		}
		sentence = sentence.replaceAll("^\\s+", ""); // strip spaces from the front
		sentence = sentence.replaceAll("\\s+$", ""); // strip spaces from the ends
		
		// If the sentence is empty, don't bother calling Gecco
		if(sentence.equals("")) {
			return new ArrayList<Pair<String, List<String>>>();
		}

		String[] words = sentence.split("\\s+");
		
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(makeNameValuePair("sentence", sentence));
		String body = request(Method.GET, basePath + "/actions/process_sentence", data);
		ClientLogger.LOG.info(body);
		
		// Initialize the complete suggestions list with empty suggestions
		List<Pair<String, List<String>>> allSuggestionsList = new ArrayList<Pair<String, List<String>>>();
			
		try {
			JSONArray json = new JSONArray(body);
			for(int w = 0; w < words.length; w++) {
				allSuggestionsList.add(new Pair<String, List<String>>(words[w], new ArrayList<String>()));
			}
			for (int i = 0; i < json.length(); i++) {
				JSONObject annotation = (JSONObject) json.get(i);
				String token = (String) annotation.get("text");
				Integer index = (Integer) annotation.get("index");
				JSONArray suggestions = (JSONArray) annotation.get("suggestions");
				
				ArrayList<String> suggestionsList = new ArrayList<String>();
				for (int j = 0; j < suggestions.length(); j++) {
					JSONObject suggestion = (JSONObject) suggestions.get(j);
					suggestionsList.add((String) suggestion.get("suggestion"));
				} 
				//allSuggestionsList.add(new Pair<String, List<String>>(token,suggestionsList));
				allSuggestionsList.set(index, new Pair<String, List<String>>(token,suggestionsList));
			}
		} catch(JSONException je) {
			if (ClientLogger.LOG.isLoggable(Level.SEVERE)) {
				ClientLogger.LOG.severe("An error occurred while processing '" + sentence 
						+ "' by " + basePath + ". " + je.getMessage());
			}
		}
		
		return allSuggestionsList;
	}
	
	@Override
	public void setType(SpellCheckerType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SpellCheckerType getType() {
		return type;
	}

	@Override
	public String getInfo() {
		return getHost() + getPath();
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
		return type + "," + getAuthenticationProtocol() + "," + getHost() + "," + getPath() + "," + getUsername() + "," + getPassword();
	}
	
	@Override
	public String toString() {
		return "Gecco webservice: " + getInfo();
	}

	@Override
	/** 
	 * Gets the suggestions for a text from either the cached suggestions
	 * or the Gecco webservice. In latter case the suggestions are put
	 * in the cached suggestions.
	 */
	public List<Pair<String, List<String>>> getSuggestions(String text) {
		// Check to see if the text has already been analyzed.
		if(cachedSuggestions.containsKey(text)) {
			return cachedSuggestions.get(text);
		}
		
		// If the text has not been analyzed, do it now and 
		// put it in the cached suggestions.
		List<Pair<String, List<String>>> suggestions = processSentence(text);
		cachedSuggestions.put(text, suggestions);
		return suggestions;
	}

	@Override
	public Boolean isCorrect(String text) {
		if(cachedSuggestions.containsKey(text)) {
			return !SpellCheckerUtil.hasSuggestions(cachedSuggestions.get(text));
		} else {
			return !SpellCheckerUtil.hasSuggestions(getSuggestions(text));
		}
	}

	/**
	 * Used for testing from the command line
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 7) {
			System.err.println("Specify the following:");
			System.err.println("  protocol (http or https)");
			System.err.println("  hostname"); // e.g. webservices-lst.science.ru.nl
			System.err.println("  path"); // e.g. /valkuil
			System.err.println("  username");
			System.err.println("  password");
			System.err.println("  command (createProject, deleteProject, ...)");
			System.err.println("  data (projectName ...)");
			System.exit(0);
		}
		String protocol = args[0];
		String host = args[1];
		String path = args[2];
		String username = args[3];
		String password = args[4];
		String command = args[5];
		String[] data = Arrays.copyOfRange(args, 6, args.length);
		
		GeccoClient client = new GeccoClient(host, protocol, path, username, password);
		
		if(command.equals("createProject")) {
			client.createProject(data[0]);
		} else if(command.equals("deleteProject")) {
			client.deleteProject(data[0]);
		} else if(command.equals("uploadText")) {
			client.uploadText(data[0], data[1], data[2]); 
		} else if(command.equals("startProject")) {
			client.startProject(data[0], data[1]);
		} else if(command.equals("pollProject")) {
			client.pollProject(data[0]);
		} else if(command.equals("retrieveOutput")) {
			client.retrieveOutput(data[0], data[1]);
		} else if(command.equals("processSentence")) {
			for(int i = 0; i < data.length; i++) {
				List<Pair<String, List<String>>> suggestions = client.getSuggestions(data[i]);
			}
		}
	}

	@Override
	public void addUserDefinedWord(String word) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getUserDefinedWords() {
		return new HashSet(); //TODO stub
	}
}