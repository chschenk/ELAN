package mpi.eudico.client.annotator.gui;

import java.awt.Dialog;
import java.util.List;

import mpi.eudico.client.annotator.prefs.RecentLanguages;
import mpi.eudico.util.multilangcv.LangInfo;

/**
 * This dialog lets the user edit the "recently used languages" list
 * (although it isn't called that any more for the user).
 * This is the list of languages as recently seen in multi-lingual
 * CVs. From this list the user can choose a "Language for
 * multilingual content".
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public class EditRecentLanguagesDialog extends AbstractEditLanguagesDialog {
	RecentLanguages langs;
	List<LangInfo> list;
	
	public EditRecentLanguagesDialog(Dialog parent) {
		super(parent, "EditRecentLanguagesDialog");
		
		langs = RecentLanguages.getInstance();
		list = langs.getRecentLanguages();

		updateBoxes();
	}

	/**
	 * Implementations of access functions to the list to be edited.
	 */
	@Override
	int addLanguage(String s, String l, String lab) {
		LangInfo li = new LangInfo(s, l, lab);
		return langs.addRecentLanguage(li);
	}

	@Override
	void removeLanguage(int index) {
		langs.removeRecentLanguage(index);
	}

	@Override
	boolean setLanguageIds(int index, String s, String l, String lab) {
		LangInfo li = new LangInfo(s, l, lab);
		return langs.changeRecentLanguage(index, li);
	}

	@Override
	int getNumberOfLanguages() {
		return list.size();
	}

	@Override
	LangInfo getLangInfo(int index) {
		return list.get(index);
	}
}

