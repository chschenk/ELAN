package mpi.eudico.client.annotator;

/**
 * Defines a preference change listener.
 *
 * @author Han Sloetjes
 */
public interface PreferencesListener {
    /**
     * Notifies the listener of a change in a single or in multiple
     * preferences. No parameters are passed, the listener is responsible  for
     * checking the preferences that apply.
     */
    public void preferencesChanged();
}
