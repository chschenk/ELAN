package mpi.eudico.client.annotator.prefs;

import java.util.Map;


/**
 * Defines methods that must be implemented by ui elements that provide the means to change user preferences.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public interface PreferenceEditor {
    /**
     * Returns whether or not any preference option has been changed.
     *
     * @return whether or not any preference option has been changed
     */
    public boolean isChanged();

    /**
     * Returns a map of preference key and new value pairs.
     *
     * @return a map of preference key and new value pairs
     */
    public Map<String, Object> getChangedPreferences();
}
