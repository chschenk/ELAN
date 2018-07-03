package mpi.eudico.client.annotator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * Class to manage cached data.
 * @author michahulsbosch
 *
 */
public class CachedDataManager implements PreferencesListener {

	private static final CachedDataManager cachedDataManager = new CachedDataManager();
	private String cacheLocation;
	
	private static final Set<String> subdirectories = new HashSet<String>() {{
		add("lexica");
		//add("CVCACHE");
	}};
	
	private ArrayList<CacheSettingsChangeListener> cacheSettingsChangeListeners = new ArrayList<CacheSettingsChangeListener>(); 
	
	private CachedDataManager() {
		Preferences.addPreferencesListener(null, this);
		cacheLocation = getCacheLocationFromPreferences();
	}
	
	private String getCacheLocationFromPreferences() {
		String cacheLocationFromPreferences = Preferences.getString("CacheLocation", null);
		if(cacheLocationFromPreferences == null || cacheLocationFromPreferences.equals("") || cacheLocationFromPreferences.equals("-")) {
			cacheLocationFromPreferences = Constants.ELAN_DATA_DIR; 
		} else if(cacheLocationFromPreferences.startsWith("file:")) {
			cacheLocationFromPreferences = cacheLocationFromPreferences.substring(5);
		}
		return cacheLocationFromPreferences;
	}
	
	public static CachedDataManager getInstance() {
		return cachedDataManager;
	}
	
	public String getCacheLocation() {
		return cacheLocation;
	}

	@Override
	public void preferencesChanged() {
		String newCacheLocation = getCacheLocationFromPreferences();
		if (!cacheLocation.equals(newCacheLocation)) {
			moveCache(cacheLocation, newCacheLocation);
			cacheLocation = newCacheLocation;
			for(CacheSettingsChangeListener listener : cacheSettingsChangeListeners) {
				listener.cacheSettingsChanged();
			}
		}
	}
	
	private static void moveCache(String oldDirectory, String newDirectory) {
		ClientLogger.LOG.info("Dirs: " + oldDirectory + " " + newDirectory);
		try {
			for(String subdirectory : subdirectories) {
				File sourcePath = new File(oldDirectory + File.separator + subdirectory);
				File destinationPath = new File(newDirectory + File.separator + subdirectory);
				FileUtils.moveDirectory(sourcePath, destinationPath);
			}
		} catch (IOException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("The cache directory could not be moved (" + e.getMessage() + ")");
            }
		}
	}
	
	public void addCacheSettingsListener(CacheSettingsChangeListener listener) {
		cacheSettingsChangeListeners.add(listener);
	}
	
	public void removeCacheSettingsListener(CacheSettingsChangeListener listener) {
		cacheSettingsChangeListeners.remove(listener);
	}
	
	public static Boolean containsCacheSubdirs(File directory) {
		if(directory.isDirectory()) {
			String[] files = directory.list();
			Set<String> dirListing = new HashSet<String>(Arrays.asList(files));
			dirListing.retainAll(subdirectories);
			if(dirListing.size() > 0) {
				return true;
			}
		}
		return false;
	}
}
