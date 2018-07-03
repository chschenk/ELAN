package mpi.eudico.client.annotator.spellcheck;

import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.apache.xerces.impl.dv.util.Base64;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ShutdownListener;
import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.IoUtil;
import mpi.eudico.util.multilangcv.LangInfo;

/**
 * Singleton class that keeps all existing spell checkers in one place
 * @author michahulsbosch
 *
 */
public class SpellCheckerRegistry implements ShutdownListener {
	/** The map of all existing spell checkers, identified by language reference (ISO 639-2) */
	private HashMap<String, SpellChecker> spellCheckers;// = new HashMap<String, SpellChecker>();
	
	/** The set of default spell checkers as found in ElanDefault.properties. */
	Set<String> defaultCheckers = new HashSet<String>();
	
	/** The singleton instance */
	private static SpellCheckerRegistry instance = null;
	
	/** File name of the spell check preferences */
	public static final String privatePreferencesFile = "SpellCheckers.xml";
	
	/** Sources of spell checker settings. */
	public enum SettingSource {
		PROPERTIES,
		PREFERENCES
	}
	
	/**
	 * 
	 */
	private SpellCheckerRegistry() {
		FrameManager.getInstance().addWindowCloseListener(this);
		spellCheckers = new HashMap<String, SpellChecker>();
	}
	
	/**
	 * 
	 * @return
	 */
	public static synchronized SpellCheckerRegistry getInstance() {
		if(instance == null) {
			instance = new SpellCheckerRegistry();
			// Create and register spell checkers from preferences
			instance.loadFrom(SettingSource.PREFERENCES);
			//instance.loadFrom(SettingSource.PROPERTIES);
		}
		return instance;
	}
	
	public SpellChecker getSpellChecker(String languageRef) {
		return spellCheckers.get(languageRef);
	}
	
	public Boolean hasSpellCheckerLoaded(String languageRef) {
		return spellCheckers.containsKey(languageRef);
	}
	
	public void putSpellChecker(String languageRef, SpellChecker checker) {
		putSpellChecker(languageRef, checker, false);
	}
	
	public void putSpellChecker(String languageRef, SpellChecker checker, Boolean isDefault) {
		spellCheckers.put(languageRef, checker);
		if(isDefault) {
			defaultCheckers.add(languageRef);
		} else if(defaultCheckers.contains(languageRef)) {
			defaultCheckers.remove(languageRef);
		}
		updatePreferences();
		
	}
	
	public void delete(String languageRef) {
		spellCheckers.remove(languageRef);
		if(defaultCheckers.contains(languageRef)) {
			defaultCheckers.remove(languageRef);
		}
		updatePreferences();
	}
	
	private void updatePreferences() {
		ArrayList<String> list = new ArrayList<String>();
		for(Map.Entry<String, SpellChecker> entry : spellCheckers.entrySet()) {
			SpellChecker checker = entry.getValue();
			list.add(entry.getKey() + "," + checker.getPreferencesString());
		}
		Preferences.set("SpellCheckerRegistry", list, null, true, true);
	}

	public HashMap<String, SpellChecker> getSpellCheckers() {
		return spellCheckers;
	}
	
	public synchronized void loadFrom(SettingSource source) {
		if(source.equals(SettingSource.PROPERTIES)) {
			loadFromProperties();
		} else if(source.equals(SettingSource.PREFERENCES)) {
			loadFromPreferences();
		}
	}
	
	/**
	 * Loads spell checker settings from the ElanDefault.properties file containing
	 * default settings.
	 */
	public void loadFromProperties() {
		// Read the properties file
		ResourceBundle resourcebundle = ResourceBundle.getBundle( "mpi.eudico.client.annotator.resources.ElanDefault");
		
		// Add a spell checker for every language encountered.
		String geccoClientLanguages = resourcebundle.getString("GeccoClient.Languages");
		if (geccoClientLanguages != null) {
			String[] languages = geccoClientLanguages.split(",");
			for (String language : languages) {
				// Add the spell checker if there is none yet for this language.
				if (!SpellCheckerRegistry.getInstance().hasSpellCheckerLoaded(language)) {
					String url = resourcebundle.getString("GeccoClient.Url." + language);
					String username = resourcebundle.getString("GeccoClient.Username." + language);
					String passwordEncoded = resourcebundle.getString("GeccoClient.Password." + language);
					String password = new String(Base64.decode(passwordEncoded));

					HashMap<String, String> spellCheckerSettings = new HashMap<String, String>();
					spellCheckerSettings.put("url", url);
					spellCheckerSettings.put("username", username);
					spellCheckerSettings.put("password", password);
					SpellChecker spellChecker = SpellCheckerFactory.create(SpellCheckerType.GECCO,
							spellCheckerSettings);

					if (spellChecker != null) {
						try {
							spellChecker.initializeSpellChecker();
							putSpellChecker(language, spellChecker, true);
						} catch (SpellCheckerInitializationException e) {
							if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
								ClientLogger.LOG.warning("The spell checker '" + spellChecker.getInfo() + "'could not be initialized (" + e.getMessage() + ")");
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Loads spell checker settings from preferences, creates spell checkers from that settings
	 * and registers the spell checkers in the SpellCheckerRegistry.
	 * This method is usually called only once when ELAN is started.
	 */
	public void loadFromPreferences() {
		syncWithPrivatePreferences();		
	}

	@Override
	public void somethingIsClosing(ShutdownListener.Event e) {
		if (e.getType() == Event.ELAN_EXITS_EARLY) {
			savePrivatePreferences();
		}
	}

	private void savePrivatePreferences() {
		DocumentBuilderFactory dbf;
	    DocumentBuilder db;

        dbf = DocumentBuilderFactory.newInstance();
        try {
			db = dbf.newDocumentBuilder();
			
			Document doc = db.newDocument();
			Element root = doc.createElement("SPELL_CHECKERS");
			doc.appendChild(root);
			
			for (String languageRef : spellCheckers.keySet()) {
				// Only save non default spell checkers
				if(!defaultCheckers.contains(languageRef)) {
					SpellChecker checker = spellCheckers.get(languageRef);
					Element l = doc.createElement("SPELL_CHECKER");
					l.setAttribute("LANG_ID", languageRef);
					l.setAttribute("PREFS", checker.getPreferencesString());
					
					for (String newWord : checker.getUserDefinedWords()) {
						Element w = doc.createElement("USER_DEFINED_WORD");
						w.setTextContent(newWord);
						l.appendChild(w);
					}
					
					root.appendChild(l);
				}
			}
			
			// DOM tree finished; now write a file.
			
            String fileName = Constants.ELAN_DATA_DIR + File.separator + privatePreferencesFile;
			IoUtil.writeEncodedFile("UTF-8", fileName, doc.getDocumentElement());
			
		} catch (ParserConfigurationException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Error in XML parser configuration (" + e.getMessage() + ")");
            }
		} catch (Exception e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Error when writing private preferences for spell checkers (" + e.getMessage() + ")");
            }
		}
	}
	
	private void syncWithPrivatePreferences() {
		XMLReader reader;
		
        try {
            reader = XMLReaderFactory.createXMLReader(
                "org.apache.xerces.parsers.SAXParser");
            reader.setFeature("http://xml.org/sax/features/namespaces", false);
            reader.setFeature("http://xml.org/sax/features/validation", false);
            reader.setFeature("http://apache.org/xml/features/validation/schema", false);
            reader.setFeature("http://apache.org/xml/features/validation/dynamic", false);
            SpellCheckersHandler sch = new SpellCheckersHandler();
            reader.setContentHandler(sch);
            reader.setErrorHandler(sch);
            String fileName = Constants.ELAN_DATA_DIR + File.separator + privatePreferencesFile;
            reader.parse(fileName);
        } catch (SAXException e) {
        	if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("The XML of the spell checking private preferences could not be read (" + e.getMessage() + ")");
            }
        } catch (FileNotFoundException e) {
        	// If the file is not there, that's not a problem.
        } catch (IOException e) {
        	if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("The file of the spell checking private preferences could not be read (" + e.getMessage() + ")");
            }
		}
	}
	
	/**
	 * Handler for parsing the list of spell checkers
	 * 
	 * @author michahulsbosch
	 */
	private class SpellCheckersHandler extends DefaultHandler {
		SpellChecker currentChecker;
		private StringBuffer curCharValue = new StringBuffer(1024);
		
		public SpellCheckersHandler() {
		}

        @Override
        public void startElement(String nameSpaceURI, String name, String rawName,
                Attributes attrs) throws SAXException {

        	if ("SPELL_CHECKER".equals(rawName)) {
            	String id = attrs.getValue("LANG_ID");
        		String checkerStr = attrs.getValue("PREFS");
        		
        		SpellChecker checker = null;
    			String[] checkerSettings = checkerStr.split(",");
    			if(checkerSettings[0].equals(SpellCheckerType.GECCO.toString())) {
    				HashMap<String, String> args = new HashMap<String,String>();
    				args.put("url", checkerSettings[2] + checkerSettings[3]);
    				args.put("username", checkerSettings[4]);
    				args.put("password", checkerSettings[5]);
    				checker = SpellCheckerFactory.create(SpellCheckerType.GECCO, args);
    			} else if(checkerSettings[0].equals(SpellCheckerType.HUNSPELL.toString())) {
    				HashMap<String, String> args = new HashMap<String,String>();
    				args.put("path", checkerSettings[1]);
    				checker = SpellCheckerFactory.create(SpellCheckerType.HUNSPELL, args);
    			}
    			
    			if(checker != null) {
    				try {
    					checker.initializeSpellChecker();
    					putSpellChecker(id, checker);
    					currentChecker = checker;
    				} catch (SpellCheckerInitializationException e) {
    					if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
    		            	ClientLogger.LOG.warning("The spell checker '" + checker.getInfo() + " could not be initialized (" + e.getMessage() + ")");
    		            }
    				}
    			}
    			
    			
        	} else if ("USER_DEFINED_WORD".equals(rawName)) {
        		curCharValue.setLength(0);
        	}
        }
        
        @Override
        public void endElement(String nameSpaceURI, String name, String rawName) {
        	if ("USER_DEFINED_WORD".equals(rawName)) {
        		String word = curCharValue.toString();
        		if(currentChecker != null) {
        			currentChecker.addUserDefinedWord(word);
        		}
        		curCharValue.setLength(0);
        	} else if ("SPELL_CHECKER".equals(rawName)) {
        		currentChecker = null;
        	}
        }
        
        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
        	curCharValue.append(ch, start, length);
        }
	}
}
