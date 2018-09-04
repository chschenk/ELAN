package mpi.eudico.client.annotator.prefs.gui;


import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import mpi.eudico.client.annotator.prefs.RecentLanguages;
import mpi.eudico.util.multilangcv.LangInfo;
import mpi.eudico.util.multilangcv.LanguageCollection;

/**
 * A pre-filled combobox which somehow knows what languages have been in use
 * recently, and fills itself with those.
 * <p>
 * The list is also stored in a private preferences file.<br/>
 * (Maybe a more compact serialization would be nice, but extending the Preferences storage
 * would be incompatible with older Elan versions, which cannot read it.)
 * 
 * @author olasei
 */

@SuppressWarnings("serial")
public class RecentLanguagesBox extends JComboBox {
	public RecentLanguagesBox(String initialValue) {
		super();
		
		//setEditor(new AbstractEditLanguagesDialog.ComboBoxLanguageEditor(null));
		initAndSetSelectedItem(initialValue);
	}
	
	private void initAndSetSelectedItem(String value) {
		boolean selected = false;	
		this.removeAllItems();
		for (LangInfo li: RecentLanguages.getInstance().getRecentLanguages()) {
			if (li != null) {
				addItem(li);
				if (li.getId().equals(value) ||
						li.getLongId().equals(value)) {
					super.setSelectedItem(li);
					selected = true;
				}
			}
		}			
		if (selected) {
			setEditable(false);
		} else {
			// The current preference wasn't in the list; add it so we don't lose it.
			//setEditable(true);
			super.setSelectedItem(value);
		}		
	}
	
	/**
	 * Sometimes, you want to be able to choose "no language".
	 * Add such an option to the list.
	 */
	public void addNoLanguageItem() {
		addItem(new LangInfo("", "", "None"));
	}
	
	/**
	 * Select a language with the given longId or shortId.
	 * If the given language is not in the list, <s>make the dropdown box editable</s>
	 * and put the string in the edit box.
	 * 
	 * @param selectId
	 */
	public void setSelectedItem(String selectId) {
		if (selectId == null) {
			selectId = "";
		}
		
		boolean selected = false;	
		ComboBoxModel m = getModel();
		int max = getItemCount();
		
		for (int i = 0; i < max; i++) {
			Object o = m.getElementAt(i);
			String lid = "";
			String sid = "";
			if (o instanceof LangInfo) {
				lid = ((LangInfo)o).getLongId();
				sid = ((LangInfo)o).getId();
			} else if (o instanceof String) {
				lid = (String)o;
			}
			if (lid.equals(selectId) || sid.equals(selectId)) {
				super.setSelectedIndex(i);
				selected = true;
				break;
			}
		}			
		if (selected) {
			setEditable(false);
		} else {
			// Should not happen
			//setEditable(true);
			super.setSelectedItem(selectId);
		}		
	}

	/**
	 * Override the "Object" version, because sometimes there is a callback
	 * from a table or something which uses Object for what is really a String
	 * or even LangInfo.
	 */
	@Override
	public void setSelectedItem(Object o) {
		if (o instanceof String) {
			setSelectedItem((String)o);
		} else {
			super.setSelectedItem(o);
		}
	}
	
	private LangInfo getLangInfo() {
		Object obj = getSelectedItem();
		if (obj instanceof LangInfo) {
			LangInfo li = (LangInfo) obj;
				return li;
		} else if (obj instanceof String) {
			// Should not happen
			String text = (String)obj;
			LangInfo li = LanguageCollection.tryParse(text);
			return li;
		}
		return null;
	}
	
	public String getId() {
		LangInfo li = getLangInfo();
		if (li != null) {
			return li.getId();
		}
		return "";
	}

	/**
	 * Retrieve the longId of the selected language.
	 * If necessary, parse the user input and extract the longId from it.
	 * If nothing proper can be found, returns the empty string.
	 */
	public String getLongId() {
		LangInfo li = getLangInfo();
		if (li != null) {
			return li.getLongId();
		}
		return "";
	}
}
