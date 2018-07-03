package mpi.eudico.client.annotator.prefs;

/**
 * This class manages the languages that were "recently seen" in CVs, so that a list of them can presented
 * in various situations where a language is to be chosen.
 * 
 * This has been extended to be the source list for choosing a "Language for multilingual content".
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.ShutdownListener;
import mpi.eudico.util.IoUtil;
import mpi.eudico.util.multilangcv.LangInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class RecentLanguages implements ShutdownListener {

	public static final String privatePreferencesFile = "RecentLanguages.xml";
	
	private List<LangInfo> recentLanguages;
	private List<WeakReference<RecentLanguageListener>> listeners;
	
	private static RecentLanguages instance;
	
	/**
	 * Get the singleton RecentLanguages instance.
	 * To be called from the GUI thread: this is not thread-safe.
	 * @return
	 */
	public static RecentLanguages getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new RecentLanguages();
		return instance;
	}

	/**
	 * Nobody else should be able to call the constructor directly.
	 */
	private RecentLanguages() {
	}
	
	/**
	 * Access function for use by the RecentLanguagesMenu and RecentLanguagesBox.
	 * @return
	 */
	public List<LangInfo> getRecentLanguages() {
		syncWithPrivatePreferences();
		return Collections.<LangInfo>unmodifiableList(recentLanguages);
	}

	/**
	 * Check if we can add a language: it must be unique in both its 
	 * long and short identifiers, OR it must be a duplicate of a
	 * language we already have.
	 * 
	 * @param newLI the LangInfo to test
	 * @return true if it seems ok
	 */
	public boolean canAddLanguage(LangInfo newLI) {
		return findConflictingLanguage(newLI, -1) < 0;
	}
	
	private final int UNIQUE = Integer.MIN_VALUE;

	/**
	 * Look though the languages and try to find if any of them conflict with
	 * a potential new language.
	 * 
	 * @param newLI
	 * @param skipIndex Ignore conflicts at this index
	 * @return UNIQUE if no conflict found, -index-1 if exact duplicate, index otherwise if conflicting.
	 */
	private int findConflictingLanguage(LangInfo newLI, int skipIndex) {
		syncWithPrivatePreferences();
				
		// See if we already have this language in our list.
		int size = recentLanguages.size();
		for (int i = 0; i < size; i++) {
			if (i != skipIndex) {
				LangInfo li = recentLanguages.get(i);
				if (li == newLI || li.getLongId().equals(newLI.getLongId())) {
					return li.getId().equals(newLI.getId()) ? -i - 1 : i;
				}				
				if (li.getId().equals(newLI.getId())) {
					// short id occurs in list but long id does not match
					return i;
				}
			}
		}
		return UNIQUE;
	}
	

	/**
	 * Keep the recent list up-to-date.
	 * This method is typically called from BasicControlledVocabulary, when anything 
	 * regarding a LangInfo changes.
	 * Entries must be unique but exact duplicates are absorbed.
	 * 
	 * @param li the changed LangInfo
	 * @return the index position of the newly added/changed language, or -1 when it failed.
	 */
	public int addRecentLanguage(LangInfo newLI) {
		syncWithPrivatePreferences();
				
		// See if we already have this language in our list.
		int index = findConflictingLanguage(newLI, -1);
		if (index == UNIQUE) {
			// Need to add it.
			index = recentLanguages.size();
			recentLanguages.add(newLI);
	
			notifyListenersAdded(index, newLI);

			return index;
		} else if (index >= 0) {
			// Conflict
			return -1;
		} else {
			// Duplicate. OK, but no need to do anything.
			return -index - 1;
		}
	}
	
	public void removeRecentLanguage(int index) {
		if (index >= 0 && index < recentLanguages.size()) {
			recentLanguages.remove(index);
			notifyListenersChanged(index, null);			
		}
	}

	public boolean changeRecentLanguage(int index, LangInfo newLI) {
		if (index >= 0 && index < recentLanguages.size()) {
			// Check uniqueness of the IDs first (disregarding the old entry)
			if (findConflictingLanguage(newLI, index) != UNIQUE) {
				return false;
			}
			recentLanguages.set(index, newLI);
			notifyListenersChanged(index, newLI);
			
			return true;
		}		
		return false;
	}
	
	/**
	 * Find a language. Identify it by either its long or short id.
	 * @param id
	 * @return the language info with the given id, if found, or null.
	 */

	public LangInfo getLanguageInfo(String id) {
		syncWithPrivatePreferences();
		
		// See if we have this language in our list.
		for (LangInfo li : recentLanguages) {
			if (li.getLongId().equals(id)) {
				return li;
			}				
			if (li.getId().equals(id)) {
				return li;
			}
		}
		return null;
	}

	/**
	 * Convert the Preferences values describing the list of recently used languages
	 * to a private form which is easier to process.
	 */
	
	private void syncWithPrivatePreferences() {
		if (recentLanguages != null) {
			// We've already done the work before...
			return;
		}
		recentLanguages = new ArrayList<LangInfo>();
	
		XMLReader reader;
		
        try {
            reader = XMLReaderFactory.createXMLReader(
                "org.apache.xerces.parsers.SAXParser");
            reader.setFeature("http://xml.org/sax/features/namespaces", false);
            reader.setFeature("http://xml.org/sax/features/validation", false);
            reader.setFeature("http://apache.org/xml/features/validation/schema", false);
            reader.setFeature("http://apache.org/xml/features/validation/dynamic", false);
            RecentLanguagesHandler rlh = new RecentLanguagesHandler();
            reader.setContentHandler(rlh);
            reader.setErrorHandler(rlh);
            // This works to make sure the schema isn't fetched from the web but from here:
            //reader.setEntityResolver(new MyResolver()); // see http://www.saxproject.org/apidoc/org/xml/sax/EntityResolver.html
            String fileName = Constants.ELAN_DATA_DIR + File.separator + privatePreferencesFile;
            reader.parse(fileName);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
        	// If the file is not there, that's not a problem.
        } catch (IOException e) {
            e.printStackTrace();
		}
        
        FrameManager.getInstance().addWindowCloseListener(this);
	}
	
	/**
	 * Save the recent languages to a private "preferences" file.
	 * Create DOM tree and then save it.
	 */
	private void savePrivatePreferences() {
	    DocumentBuilderFactory dbf;
	    DocumentBuilder db;

        dbf = DocumentBuilderFactory.newInstance();
        try {
			db = dbf.newDocumentBuilder();
			
			Document doc = db.newDocument();
			Element root = doc.createElement("RECENT_LANGUAGES");
			doc.appendChild(root);
			
			for (LangInfo li : recentLanguages) {
				Element l = doc.createElement("LANGUAGE");
				l.setAttribute("LANG_ID", li.getId());
				l.setAttribute("LANG_DEF", li.getLongId());
				l.setAttribute("LANG_LABEL", li.getLabel());
				
				root.appendChild(l);
			}
			
			// DOM tree finished; now write a file.
			
            String fileName = Constants.ELAN_DATA_DIR + File.separator + privatePreferencesFile;
			IoUtil.writeEncodedFile("UTF-8", fileName, doc.getDocumentElement());
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Handle listeners to our changes.
	 * Typically the RecentLanguagesMenuItem.
	 * Use WeakReference<RecentLanguageListener> so that we don't keep
	 * listener objects alive if they never could call
	 * removeRecentLanguageListener().
	 */

	public void addRecentLanguageListener(
			RecentLanguageListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<WeakReference<RecentLanguageListener>>(1);
		}
		listeners.add(new WeakReference<RecentLanguageListener>(listener));
	}

	public void removeRecentLanguageListener(
			RecentLanguageListener listener) {
		if (listeners != null) {

			Iterator<WeakReference<RecentLanguageListener>> iter = listeners.iterator();
			
			while (iter.hasNext()) {
				WeakReference<RecentLanguageListener> ref = iter.next();
				if (ref.get() == listener || ref.get() == null) {
					iter.remove();
				}
			}
		}
	}

	private void notifyListenersAdded(int freeIndex, LangInfo newLI) {
		if (listeners != null) {
			Iterator<WeakReference<RecentLanguageListener>> iter = listeners.iterator();
			
			while (iter.hasNext()) {
				WeakReference<RecentLanguageListener> ref = iter.next();
				RecentLanguageListener listener = ref.get();
				if (listener == null) {
					iter.remove();
				} else {
					listener.recentLanguageAdded(freeIndex, newLI);
				}
			}
		}
	}
	
	private void notifyListenersChanged(int freeIndex, LangInfo newLI) {
		if (listeners != null) {
			Iterator<WeakReference<RecentLanguageListener>> iter = listeners.iterator();
			
			while (iter.hasNext()) {
				WeakReference<RecentLanguageListener> ref = iter.next();
				RecentLanguageListener listener = ref.get();
				if (listener == null) {
					iter.remove();
				} else {
					listener.recentLanguageChanged(freeIndex, newLI);
				}
			}
		}
	}
	
	/**
	 * Handler for parsing the list of recent languages
	 * 
	 * @author olasei
	 */
	private class RecentLanguagesHandler extends DefaultHandler {
		public RecentLanguagesHandler() {
		}

        @Override
        public void startElement(String nameSpaceURI, String name, String rawName,
                Attributes attrs) throws SAXException {

        	if ("LANGUAGE".equals(rawName)) {
        		String id = attrs.getValue("LANG_ID");
        		String def = attrs.getValue("LANG_DEF");
        		String label = attrs.getValue("LANG_LABEL");
        		
        		LangInfo rli = new LangInfo(id, def, label);
       			recentLanguages.add(rli);
        	}
        }
	}

	/**
	 * Called when a window, or Elan, is closing.
	 */
	@Override
	public void somethingIsClosing(ShutdownListener.Event e) {
		if (e.getType() == Event.ELAN_EXITS_EARLY) {
			savePrivatePreferences();
		}
	}
}