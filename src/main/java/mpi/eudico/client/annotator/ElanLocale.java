package mpi.eudico.client.annotator;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * The class that handles everything with locales for Elan.
 */
public class ElanLocale {
	//private static Vector listeners = new Vector();
	private static Locale locale;
	private static ResourceBundle resourcebundle;
	
	private static Map<Object, List<ElanLocaleListener>> listenerGroups = new HashMap<Object, List<ElanLocaleListener>>();
	
	/** constant for a custom language */
	public static final Locale CUSTOM = new Locale("cu", "", "");
	
	/** constant for dutch */
	public static final Locale DUTCH = new Locale("nl", "NL");
	
	/** constant for english, the default language */
	public static final Locale ENGLISH = new Locale("", "");
	
	/** constant for catalan */
	public static final Locale CATALAN = new Locale("ca");
	
	/** constant for spanish */
	public static final Locale SPANISH = new Locale("es", "ES");
	
	/** constant for swedish */
	public static final Locale SWEDISH = new Locale("sv", "SE");
	
	/** constant for german */
	public static final Locale GERMAN = new Locale("de", "DE");
	
	/** constant for portuguese */
	public static final Locale PORTUGUESE = new Locale("pt");
	
	/** constant for french */
	public static final Locale FRENCH = new Locale("fr");
	
	/** constant for japanese */
	public static final Locale JAPANESE = new Locale("ja", "JP");
	
	/** constant for chinese simplified */
	public static final Locale CHINESE_SIMP = new Locale("zh", "CN"); 
	
	
	/** constant for russian */
	public static final Locale RUSSIAN = new Locale("ru", "RU");
	
	/** constant for korean */
	public static final Locale KOREAN = new Locale("ko");

	/**
	 * Constructor
	 */
	ElanLocale() {
		locale = Locale.getDefault();
		resourcebundle =
			ResourceBundle.getBundle(
				"mpi.eudico.client.annotator.resources.ElanLanguage",
				locale);
	}

	/**
	 * Gets the current locale
	 *
	 * @return The current locale
	 */
	public static Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the current locale
	 *
	 * @param locale_in The new locale
	 */
	public static void setLocale(Locale locale_in) {
		if (locale != null && locale.equals(locale_in)) {
			return;
		}
		//if (locale.getCountry().equals(locale_in.getCountry())) {
		//	return;
		//}

		locale = locale_in;
		if (locale.equals(CUSTOM)) {
			// try to read from a properties file from the user's ELAN directory
			try {
				File custFile = new File(Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + "ElanLanguage.properties");
				if (custFile.exists()) {
					FileInputStream stream = new FileInputStream(custFile);
					resourcebundle = new PropertyResourceBundle(stream);
					stream.close();
				} else {
					// log error
					ClientLogger.LOG.warning("No custom localisation file found.");
					resourcebundle =
						ResourceBundle.getBundle(
							"mpi.eudico.client.annotator.resources.ElanLanguage");	
				}
			} catch (Exception ex) {
				// log error
				ClientLogger.LOG.warning("Could not load custom localisation file: " + ex.getMessage());
				resourcebundle =
					ResourceBundle.getBundle(
						"mpi.eudico.client.annotator.resources.ElanLanguage");
			}
		} else {
		    resourcebundle =
			    ResourceBundle.getBundle(
				    "mpi.eudico.client.annotator.resources.ElanLanguage",
				    locale);
		}
		notifyListeners();
		
		try {
			if (!locale.equals(CUSTOM)) {
				mpi.search.SearchLocale.setLocale(locale);
			} else {
				File custFile = new File(Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + "SearchLanguage.properties");
				if (custFile.exists()) {
					FileInputStream stream = new FileInputStream(custFile);
					ResourceBundle resBundle = new PropertyResourceBundle(stream);
					stream.close();
					mpi.search.SearchLocale.setResourceBundle(resBundle);
				} else {
					// log error
					ClientLogger.LOG.warning("No custom search localisation file found.");
					mpi.search.SearchLocale.setLocale(locale);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the string in the right language from the right resource file
	 *
	 * @param str The string which has to be mapped to the right language
	 *
	 * @return The string in the right language
	 */
	public static String getString(String str) {
		if (locale == null) {
			locale = Locale.getDefault();
			resourcebundle =
				ResourceBundle.getBundle(
					"mpi.eudico.client.annotator.resources.ElanLanguage",
					locale);
		}

		try {
			return resourcebundle.getString(str);
		}
		catch (Exception ex) {
			return "";
		}
	}

	/**
	 * Adds an Elan Locale listener.
	 *
	 * @param listener A new Elan Locale listener
	 */
	public static void addElanLocaleListener(Object key, 
			ElanLocaleListener listener) {
		if (listenerGroups.containsKey(key)) {
			listenerGroups.get(key).add(listener);
			
			listener.updateLocale();
		} else {
			List<ElanLocaleListener> list = new ArrayList<ElanLocaleListener>();
			list.add(listener);
			
			listenerGroups.put(key, list);
			listener.updateLocale();
		}
		/*
		if (!listeners.contains(listener)) {
			listeners.add(listener);

			// make sure the listener is up to date
			listener.updateLocale();
		}
		*/
	}

	/**
	 * Removes an Elan Locale listener.
	 *
	 * @param listener The listener which has to be removed
	 */
	public static void removeElanLocaleListener(ElanLocaleListener listener) {
		//listeners.remove(listener);
		Iterator<Object> groupIt = listenerGroups.keySet().iterator();
		while (groupIt.hasNext()) {
			List<ElanLocaleListener> listeners = listenerGroups.get(groupIt.next());
			if (listeners.remove(listener)) {
				break;
			}
		}
	}

	/**
	 * Removes an Elan Locale listener group.
	 *
	 * @param key The key of the group which has to be removed
	 */
	public static void removeElanLocaleListener(Object key) {
		//listeners.remove(listener);
		listenerGroups.remove(key);
	}
	
	/**
	 * Notifies all listeners if the locale has been changed.
	 */
	private static void notifyListeners() {
		//for (int i = 0; i < listeners.size(); i++) {
		//	((ElanLocaleListener) listeners.elementAt(i)).updateLocale();
		//}
		Iterator<Object> groupIt = listenerGroups.keySet().iterator();
		while (groupIt.hasNext()) {
			List<ElanLocaleListener> listeners = listenerGroups.get(groupIt.next());
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).updateLocale();
			}
		}
	}
	
	/**
	 * Returns the resource bundle for use in (generic) classes that do not 
	 * directly access ElanLocale.
	 * 
	 * @return the resource bundle
	 */
	public static ResourceBundle getResourceBundle() {
		return resourcebundle;
	}

}
