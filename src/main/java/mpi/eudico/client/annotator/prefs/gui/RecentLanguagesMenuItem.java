package mpi.eudico.client.annotator.prefs.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesListener;
import mpi.eudico.client.annotator.gui.MenuScroller;
import mpi.eudico.client.annotator.prefs.RecentLanguageListener;
import mpi.eudico.client.annotator.prefs.RecentLanguages;
import mpi.eudico.util.multilangcv.LangInfo;

/**
 * A menu item with submenu containing the recently seen languages in CVs.
 * 
 * If a choice is made, the preference setting is updated.
 * Listens to preference changes and updates the checkmark.
 * <p>
 * Call obj.isClosing() when the menu item is about to go out of use,
 * so that it can do cleanup.
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public class RecentLanguagesMenuItem extends JMenu
                                     implements ActionListener, PreferencesListener, RecentLanguageListener, ElanLocaleListener {
	private String origLanguageValue;
	private ButtonGroup group;
	private boolean ignorePrefChanges = false;
	
	public RecentLanguagesMenuItem() {
		//super(ElanLocale.getString("PreferencesDialog.Edit.DefaultCVLanguage.Menu"));
		
        String stringPref = Preferences.getString(Preferences.PREF_ML_LANGUAGE, null);
        
        if (stringPref != null) {
        	origLanguageValue = stringPref;
        } else {
        	origLanguageValue = "";
        }
        
        // The menu could get very long; we need a scroller...
        MenuScroller.setScrollerFor(this, Constants.COMBOBOX_VISIBLE_ROWS, 100);
        
    	RecentLanguages recentLanguages = RecentLanguages.getInstance();
		
		// Create submenu items
		
		group = new ButtonGroup();
		for (LangInfo li : recentLanguages.getRecentLanguages()) {
			addLanguageMenuItem(li);
		}
		
        /*
         * Add ourselves as a preferences listener.
         * We want to know which language the user prefers, in order to
         * put the checkmark on the current item.
         */
		Preferences.addPreferencesListener(null, this);
		recentLanguages.addRecentLanguageListener(this);
		// add as LocaleListener
		ElanLocale.addElanLocaleListener(null, this);
	}

	/**
	 * Call this when the menu is about to go unreachable.
	 * It unhooks itself by unregistering its listeners.
	 */
	public void isClosing() {
		RecentLanguages.getInstance().removeRecentLanguageListener(this);
		Preferences.removePreferencesListener(null, this);
		ElanLocale.removeElanLocaleListener(this);
	}

	private void addLanguageMenuItem(LangInfo li) {
		String itemString = li.getLabel() + " - " + li.getId();
		JMenuItem item = new JRadioButtonMenuItem(itemString);
		String longId = li.getLongId();
		item.setActionCommand(longId);
		if (longId.equals(origLanguageValue)) {
			item.setSelected(true);
		}
		item.addActionListener(this);
		group.add(item);			
		this.add(item);
	}

	@Override // ActionListener
	public void actionPerformed(ActionEvent e) {
		String newLanguage = e.getActionCommand();
		if (newLanguage.equals(origLanguageValue)) {
			return;
		}
		
		// Change the preferences and notify everyone
		ignorePrefChanges = true;
		Preferences.set(Preferences.PREF_ML_LANGUAGE, newLanguage, null, true);
    	// Tell the Transcriptions about it
    	Preferences.updateAllCVLanguages(newLanguage, false);
    	origLanguageValue = newLanguage;		
		ignorePrefChanges = false;
	}	

	@Override // PreferencesListener
	public void preferencesChanged() {
        if (ignorePrefChanges) {
        	return;
        }

        String val = Preferences.getString(Preferences.PREF_ML_LANGUAGE, null);
        String languageValue;
        
        if (val != null) {
        	languageValue = val;
        } else {
        	languageValue = "";
        }
        // If it didn't change, no more work is needed.
		if (languageValue.equals(origLanguageValue)) {
			return;
		}
		origLanguageValue = languageValue;

		group.clearSelection();
		
		Enumeration<AbstractButton> e = group.getElements();
		while (e.hasMoreElements()) {
			AbstractButton ab = e.nextElement();
			if (languageValue.equals(ab.getActionCommand())) {
				ab.setSelected(true);
				break;
			}
		}
	}

	@Override // RecentLanguageListener
	public void recentLanguageAdded(int index, LangInfo langInfo) {
		if (index < this.getItemCount()) {
			// modify the menu item in the middle
			this.getItem(index).setText(langInfo.toString());
		} else {
			addLanguageMenuItem(langInfo);
		}
	}

	@Override // RecentLanguageListener
	public void recentLanguageChanged(int index, LangInfo langInfo) {
		if (index >= 0 && index < this.getItemCount()) {
			if (langInfo == null) {
				// Removed an item
				group.remove(this.getItem(index));
				this.remove(index);
			} else {
				// Changed an item
				this.getItem(index).setText(langInfo.toString());
			}
		}
	}

	@Override
	public void updateLocale() {
		setText(ElanLocale.getString("PreferencesDialog.Edit.DefaultCVLanguage.Menu"));
	}
}
