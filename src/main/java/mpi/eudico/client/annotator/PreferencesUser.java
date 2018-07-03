package mpi.eudico.client.annotator;

/**
 * Extends the interface PreferencesListener with a method to set/change a
 * preference.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public interface PreferencesUser extends PreferencesListener {
    /**
     * Sets the preference with the specified key.
     *
     * @param key the key for the preference
     * @param value the value of the preference
     * @param document the document to set the preference for (a Transcription
     *        object)
     */
    public void setPreference(String key, Object value, Object document);
}
