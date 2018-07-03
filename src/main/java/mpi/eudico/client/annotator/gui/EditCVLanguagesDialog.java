package mpi.eudico.client.annotator.gui;

import java.awt.Dialog;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import mpi.eudico.client.annotator.prefs.gui.RecentLanguagesBox;
import mpi.eudico.util.BasicControlledVocabulary;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.multilangcv.LangInfo;

/**
 * This dialog lets the user edit the languages in a Controlled Vocabulary.
 * 
 * @author olasei
 */

@SuppressWarnings("serial")
public class EditCVLanguagesDialog extends AbstractEditLanguagesDialog implements ActionListener {
	private ControlledVocabulary cv;

	public EditCVLanguagesDialog(Dialog parent, ControlledVocabulary cv) {
		super(parent, "EditCVLanguagesDialog");
		this.cv = cv;

		updateBoxes();
	}
	
    /**
     * Enable/disable the buttons based on the current state.
     * For instance, the last language cannot be deleted, and
     * the first language must be changed before you can add more.
     */
	@Override
    protected void updateButtons() {
    	super.deleteButton.setEnabled(getNumberOfLanguages() > 1);
    	boolean adding = getNumberOfLanguages() > 1 || 
    			         !getLongLanguageId(0).equals(BasicControlledVocabulary.DEFAULT_LANGUAGE_DEF);
   		super.addButton.setEnabled(adding);
    }
    
	/**
	 * In the editor for the languages to be used in the CV, we don't want to 
	 * have the whole official collection of languages available.
	 * Therefore we just allow to select from the "recent languages" list, with no
	 * editing or other option.
	 * 
	 * @see mpi.eudico.client.annotator.gui.AbstractEditLanguagesDialog#getNewLanguageComboBox()
	 */
	@Override
	protected JComboBox getNewLanguageComboBox() {
		return new RecentLanguagesBox("");
	}
	
	@Override
	protected void updateNewLanguageComboBox() {
		super.newLanguageComboBox.setSelectedIndex(getIdIndex(getLongLanguageId(0)));
		super.newLanguageComboBox.setEditable(false);
	}

	/**
	 * Implementations of access functions to the list to be edited.
	 */
	@Override
	int addLanguage(String s, String l, String lab) {
		return cv.addLanguage(s, l, lab);
	}

	@Override
	void removeLanguage(int index) {
		cv.removeLanguage(index);
	}

	@Override
	boolean setLanguageIds(int index, String s, String l, String lab) {
		return cv.setLanguageIds(index, s, l, lab);
	}

	@Override
	int getNumberOfLanguages() {
		return cv.getNumberOfLanguages();
	}

	@Override
	LangInfo getLangInfo(int index) {
		return cv.getLangInfo(index);
	}
}
