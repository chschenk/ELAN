package mpi.eudico.client.annotator.prefs;

import mpi.eudico.util.multilangcv.LangInfo;

/**
 * Interface for classes which wish to be kept informed if a new language
 * appears in the Recent Languages list.
 * 
 * @author olasei
 */
public interface RecentLanguageListener {
	/**
	 * Notification of addition of a new recent language.
	 * @param index
	 * @param langInfo
	 */
	public void recentLanguageAdded(int index, LangInfo langInfo);
	/**
	 * Notification of change of a recent language entry.
	 * If langInfo is null, the entry at the given position has been removed.
	 * @param index
	 * @param langInfo
	 */
	public void recentLanguageChanged(int index, LangInfo langInfo);
}
