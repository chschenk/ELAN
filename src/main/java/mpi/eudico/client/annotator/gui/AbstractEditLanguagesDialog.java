package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.prefs.RecentLanguages;
import mpi.eudico.util.multilangcv.LangInfo;
import mpi.eudico.util.multilangcv.LanguageCollection;

/**
 * This class is for an editor of a list of languages.
 * 
 * It is abstract because it needs to access the relevant list of languages.
 * One subclass is for editing the languages in a Controlled Vocabulary,
 * another for the list of "recent" languages.
 * 
 * Another abstraction is for the combobox which offers the user the
 * languages to choose from.
 * By default this contains all languages from the LanguageCollection,
 * but it can be overridden to have different contents.
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public abstract class AbstractEditLanguagesDialog extends JDialog
												  implements ActionListener {

	/**
	 * These abstract access functions are for the manipulation of the to-be-edited
	 * list. They are not for the list of the available options.
	 * 
	 * Add a new language and return its ordinal position.
	 * There may be restrictions, such as uniqueness, so in case of failure it returns < 0.
	 */
	abstract int addLanguage(String s, String l, String lab);
	/**
	 * Remove the language at the given ordinal position.
	 */
	abstract void removeLanguage(int index);
	/**
	 * Change the language at a certain position.
	 * Returns whether this succeeded: there may be restrictions, such as uniqueness.
	 */
	abstract boolean setLanguageIds(int index, String s, String l, String lab);	
	/**
	 * Get the length of the to-be-edited list.
	 */
    abstract int getNumberOfLanguages();
	/**
	 * Get a single language from the to-be-edited list.
	 */
    abstract LangInfo getLangInfo(int index);
    
    protected JComboBox languageComboBox;		// contains "recent" languages
    protected JComboBox newLanguageComboBox;	// contains all available languages
    protected LangInfo selectedNewLanguage;
    protected ComboBoxLanguageEditor editor;
    protected JButton addButton;
    protected JButton changeButton;
    protected JButton deleteButton;
    protected JButton closeButton;
    protected String localePrefix;

    public AbstractEditLanguagesDialog(Dialog parent, String localePrefix) {
		super(parent, true);
		this.localePrefix = localePrefix;
		
		makeLayout();
	}
	
	void makeLayout() {
		
		String title = ElanLocale.getString(localePrefix + ".Title");
		setTitle(title);

		getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(6, 10, 6, 10);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = insets;
		GridBagConstraints gbc_center_x = (GridBagConstraints)gbc.clone();
        gbc_center_x.fill = GridBagConstraints.NONE;
		
		JLabel topLabel = new JLabel(title);
		topLabel.setFont(topLabel.getFont().deriveFont((float) 16));
		topLabel.setMaximumSize(topLabel.getPreferredSize());
        add(topLabel, gbc_center_x);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridBagLayout());
		contentPanel.setBorder(new TitledBorder(ElanLocale.getString("EditLanguagesDialog.Border")));
		
		JLabel label = new JLabel(ElanLocale.getString(localePrefix + ".Label.Available"));
		contentPanel.add(label, gbc);
        languageComboBox = new JComboBox();
        contentPanel.add(languageComboBox, gbc);

        label = new JLabel(ElanLocale.getString("EditLanguagesDialog.Label.Edit"));
        contentPanel.add(label, gbc);
        
        newLanguageComboBox = getNewLanguageComboBox();
        newLanguageComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        contentPanel.add(newLanguageComboBox, gbc);
        
        Box buttonBox = new Box(BoxLayout.X_AXIS);
        
        addButton = new JButton(ElanLocale.getString("EditLanguagesDialog.Button.Add"));
		buttonBox.add(addButton);

		changeButton = new JButton(ElanLocale.getString("EditLanguagesDialog.Button.Change"));
		buttonBox.add(changeButton);

		deleteButton = new JButton(ElanLocale.getString("EditLanguagesDialog.Button.Delete"));
		buttonBox.add(deleteButton);

		contentPanel.add(buttonBox, gbc_center_x);
		add(contentPanel, gbc);

		closeButton = new JButton(ElanLocale.getString("EditLanguagesDialog.Button.Close"));
		add(closeButton, gbc_center_x);
		
        languageComboBox.addActionListener(this);
        newLanguageComboBox.addActionListener(this);
		addButton.addActionListener(this);
		changeButton.addActionListener(this);
		deleteButton.addActionListener(this);
		closeButton.addActionListener(this);

        pack();        
        setLocationRelativeTo(getParent());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == addButton) {
			getSelectedNewLanguage();
			if (selectedNewLanguage == null) {
				showErrorDialog(ElanLocale.getString("EditLanguagesDialog.Error.NothingToAdd"));
				return;
			}
			String s = selectedNewLanguage.getId();
			String l = selectedNewLanguage.getLongId();
			String lab = selectedNewLanguage.getLabel();
			int newindex = addLanguage(s, l, lab);
			
			if (newindex >= 0) {
				updateLanguageComboBox();
				languageComboBox.setSelectedIndex(newindex);				
			} else {
				showErrorDialog(ElanLocale.getString(localePrefix + ".Error.AddFailed"));
			}
		} else if (source == deleteButton) {
			int index = languageComboBox.getSelectedIndex();
			if (index >= 0) {
				LangInfo item = (LangInfo)languageComboBox.getItemAt(index);
				String name = item.toString();
				String sure = String.format(ElanLocale.getString(localePrefix +  ".Confirm.Delete"),
						name); // the format may contain an instance of "%s".
				if (showConfirmDialog(sure)) {
	    			removeLanguage(index);
	    			updateLanguageComboBox();
				}
			}
		} else if (source == changeButton) {
			getSelectedNewLanguage();
			if (selectedNewLanguage == null) {
				showErrorDialog(ElanLocale.getString("EditLanguagesDialog.Error.NothingToChange"));
				return;
			}
			String s = selectedNewLanguage.getId();
			String l = selectedNewLanguage.getLongId();
			String lab = selectedNewLanguage.getLabel();
			int index = languageComboBox.getSelectedIndex();

			if (index >= 0 && setLanguageIds(index, s, l, lab)) {
				updateLanguageComboBox();
				languageComboBox.setSelectedIndex(index);				
			} else {
				showErrorDialog(ElanLocale.getString(localePrefix + ".Error.ChangeFailed"));
			}
		} else if (source == closeButton) {
			closeDialog();
		} else if (source == languageComboBox) {
			int index = languageComboBox.getSelectedIndex();
			if (index >= 0) {
				int index2 = getIdIndex(getLongLanguageId(index));
				if (index2 == -1 && newLanguageComboBox.isEditable()) {
					Object customItem = languageComboBox.getSelectedItem();
					newLanguageComboBox.setSelectedItem(customItem);
				} else {
					newLanguageComboBox.setSelectedIndex(index2);
				}
			}
		//} else if (source == newLanguageComboBox) {
		} else if (source == editor) {
			boolean valid = !e.getActionCommand().equals("invalid");
			addButton.setEnabled(valid);
			changeButton.setEnabled(valid);				
		}
	}
	
	protected String getLongLanguageId(int index) {
		return getLangInfo(index).getLongId();
	}
	
	/**
	 * Find out what language info has been selected or typed by the user.
	 * Don't await a call to actionPerformed(): that happens only when they hit Enter.
	 * 
	 * This method only returns languages that are valid and unique.
	 * That means that if a user invents a new language, both the long and short IDs
	 * must not already exist in the LanguageCollection.
	 * Later code may also check for further uniqueness, such as within the
	 * "recent languages" list.
	 */
	private LangInfo getSelectedNewLanguage() {
		Object selection;
		if (newLanguageComboBox.isEditable()) {
			ComboBoxEditor editor = newLanguageComboBox.getEditor();
			selection = editor.getItem();
		} else {
			selection = newLanguageComboBox.getSelectedItem();
		}
		
		if (selection instanceof LangInfo) {
			selectedNewLanguage = (LangInfo)selection;
		}
		return selectedNewLanguage;
	}
	
	/**
	 * Find the index in the newLanguageComboBox of the given id
	 * @return
	 */
	protected int getIdIndex(String id) {
		int size = newLanguageComboBox.getItemCount();
		for (int i = 0; i < size; i++) {
			Object o = newLanguageComboBox.getItemAt(i);
			if (o instanceof LangInfo) {
				LangInfo li  = (LangInfo) o;
				if (id.equals(li.getId()) || id.equals(li.getLongId())) {
					return i;
				}
			}
		}
		return -1;
	}
	
    /**
     * Closes the dialog
     */
    protected void closeDialog() {
        setVisible(false);
        dispose();
    }

    /*
	 * This method can only be called after the data source has been set;
	 * this typically happens in the constructor of the derived class:
	 * so this cannot be called yet in our constructor.
	 */
	protected void updateBoxes() {
        updateNewLanguageComboBox();
        updateLanguageComboBox();
        pack();
	}	
    
    /**
     * Extracts the languages from the CV and fills the cv language combobox.
     */
    protected void updateLanguageComboBox() {
        int nLangs = getNumberOfLanguages();
        
        languageComboBox.removeAllItems();
        
        for (int i = 0; i < nLangs; i++) {
        	languageComboBox.addItem(getLangInfo(i));
        }
        if (nLangs > 0) {
        	languageComboBox.setSelectedIndex(0);
        }
       	updateButtons();
    }
    
    /**
     * Create the combobox for the list of languages to choose from.
     * Can be overridden if you don't want a box with all languages
     * from the LanguageCollection.
     */
    protected JComboBox getNewLanguageComboBox() {
    	JComboBox box = new JComboBox();
        box.setEditable(true);
        return box;
    }
    
    /**
     * Update the "new language" combobox with a list of "all possible"
     * languages.
     * Since filling a combobox with a few thousand entries is rather slow,
     * and we have the entries in a List already anyway, just create a model
     * that adapts the List.
     */
    protected void updateNewLanguageComboBox() {

    	final List<LangInfo> languages = LanguageCollection.getLanguages();
    	
    	editor = new ComboBoxLanguageEditor(null);
    	ComboBoxModel m = new LanguagesListModel(languages);
    	 // speeds up initial display drastically:
    	newLanguageComboBox.setPrototypeDisplayValue(languages.get(0));
    	newLanguageComboBox.setModel(m);
    	newLanguageComboBox.setEditor(editor);
    	if (getNumberOfLanguages() > 0) {
    		newLanguageComboBox.setSelectedIndex(getIdIndex(getLongLanguageId(0)));
    	}
		editor.setActionListener(this);
    }

    /**
     * Enable/disable the buttons based on the current state.
     * By default, few limitations are present:
     * You can only delete or change a language if there is at least one.
     */
    protected void updateButtons() {
    	final boolean atLeastOneLanguage = getNumberOfLanguages() > 0;
		deleteButton.setEnabled(atLeastOneLanguage);
    	changeButton.setEnabled(atLeastOneLanguage);
   		addButton.setEnabled(true);
    }
    
    /**
     * Shows a confirm (yes/no) dialog with the specified message string.
     *
     * @param message the message to display
     *
     * @return true if the user clicked OK, false otherwise
     */
    protected boolean showConfirmDialog(String message) {
        int confirm = JOptionPane.showConfirmDialog(this, message,
        		ElanLocale.getString("Message.Warning"),
                JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_OPTION;
    }

    /**
     * Shows a error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
        		ElanLocale.getString("Message.Error"),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Use a custom model for the NewLanguageDialog to avoid duplicating the whole list
     * (about 7700 elements) element by element when adding it to the combo box.
     * We keep one extra element for a user-edited string.
     * 
     * @author olasei
     */
    private static class LanguagesListModel extends AbstractListModel
    										implements ComboBoxModel {
    	final List<LangInfo> languages;
    	final int size;
    	Object selectedItem;
    	
    	LanguagesListModel(List<LangInfo> languages) {
    		this.languages = languages;
    		this.size = languages.size();
    	}
    	
		@Override // AbstractListModel
		public int getSize() {
			return size;
		}

		@Override // AbstractListModel
		public Object getElementAt(int index) {
			if (index < 0) {
				return "";
			}
			if (index < size) {
				return languages.get(index);
			} else {
				return "";
			}
		}

		@Override // ComboBoxModel; copied from DefaultComboBoxModel
		public void setSelectedItem(Object anItem) {
	        if ((selectedItem != null && !selectedItem.equals(anItem)) ||
	                selectedItem == null && anItem != null) {
                selectedItem = anItem;
                fireContentsChanged(this, -1, -1);
	        }
		}

		@Override // ComboBoxModel
		public Object getSelectedItem() {
			return selectedItem;
		}
    }
    
    public static class ComboBoxLanguageEditor
    					implements ComboBoxEditor, DocumentListener {
    	LangInfoPanel panel;
		LangInfo origLangInfo;
		ActionListener validListener;
		boolean prevValid;
		private boolean updating;
			
    	public ComboBoxLanguageEditor(LangInfo defaultChoice) {
    		panel = new LangInfoPanel(defaultChoice);
    		panel.addDocumentListener(this);
    		prevValid = true;
    	}
    	
    	/**
    	 * We support a single ActionListener only.
    	 * The ActionCommand string will indicate either "valid" or "invalid".
    	 * Events are sent at least when the validity state changes.
    	 * @param validListener
    	 */
    	public void setActionListener(ActionListener validListener) {
    		this.validListener = validListener;
    	}

    	@Override
    	public void setItem(Object anObject) {
    		if (anObject instanceof LangInfo) {
    			LangInfo li = (LangInfo)anObject;
    			origLangInfo = li;
    			updating = true; // avoid invalid/valid actions during parts of update
    			panel.setLangInfo(li);
    			updating = false;
    			sendValidAction();
    		}
    	}

    	@Override
    	public Component getEditorComponent() {
    		return panel;
    	}

		/**
		 * Return a LangInfo from the text presently in the text fields.
		 * Checks it for uniqueness.
		 * Returns null if not good enough.
		 */

    	@Override
    	public Object getItem() {
    		return checkIfValid(panel.getLangInfo());
    	}
    	
    	/**
    	 * A short language id may consist of letters and digits but must start with a letter.
    	 * The pattern can be shared between instances but the matcher can't.
    	 */
    	private static final Pattern idPattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");
    	private final Matcher idMatcher = idPattern.matcher("");

    	/**
    	 * Checks if the given LangInfo is valid. 
    	 * Apart from uniqueness it also checks the text of the short id.
    	 * @param li
    	 * @return given LangInfo if ok, or null otherwise.
    	 */
    	private LangInfo checkIfValid(LangInfo li) {
    		String s = li.getId();
    		String l = li.getLongId();
    		boolean edited = !s.equals(origLangInfo.getId()) ||
    				         !l.equals(origLangInfo.getLongId());
    		if (edited) {
    			if (s.length() < 4) {
    				return null;
    			}
    			idMatcher.reset(s);
    			if (!idMatcher.matches()) { // pattern must match the entire string
    				return null;
    			}
    			if (LanguageCollection.validate(li) == null)
    			 	return null;
    			if (!RecentLanguages.getInstance().canAddLanguage(li))
    				return null;
    		}
    		return li;
    	}
    	
		private void sendValidAction() {
			if (updating)	
				return;
			// Make the text red if it is not valid.
			boolean valid = getItem() != null;
			panel.setValid(valid);
			if (valid != prevValid) {
	    		if (validListener != null) {
	    			validListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, valid ? "valid" : "invalid"));
	    		}
    			prevValid = valid;
    		}
		}

    	@Override // ComboBoxEditor
    	public void selectAll() {
    		panel.selectAll();
    	}

    	@Override // ComboBoxEditor
    	public void addActionListener(ActionListener l) {
    		panel.addActionListener(l);
    	}

    	@Override // ComboBoxEditor
    	public void removeActionListener(ActionListener l) {
    		panel.removeActionListener(l);
    	}


		@Override // DocumentListener
		public void insertUpdate(DocumentEvent e) {
			sendValidAction();
		}

		@Override // DocumentListener
		public void removeUpdate(DocumentEvent e) {
			sendValidAction();
		}

		@Override // DocumentListener
		public void changedUpdate(DocumentEvent e) {
			sendValidAction();
		}

		//  We create our own inner class to handle setting and
    	//  repainting the image and the text.
    	class LangInfoPanel extends JPanel  {
    		JTextField label;
    		JTextField shortId;
    		JTextField longId;

    		public LangInfoPanel(LangInfo initialEntry) {
    			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

    			label = new JTextField(initialEntry == null ? "" : initialEntry.getLabel());
    			label.setColumns(10);
    			label.setBorder(new BevelBorder(BevelBorder.LOWERED));

    			shortId = new JTextField(initialEntry == null ? "" : initialEntry.getId());
    			shortId.setColumns(6);
    			shortId.setBorder(new BevelBorder(BevelBorder.LOWERED));

    			longId = new JTextField(initialEntry == null ? "" : initialEntry.getLongId());
    			longId.setColumns(25);
    			longId.setBorder(new BevelBorder(BevelBorder.LOWERED));

    			add(label);
    			add(shortId);
    			add(longId);
    		}

    		public void setLangInfo(LangInfo li) {
    			label.setText(li.getLabel());
    			shortId.setText(li.getId());
    			longId.setText(li.getLongId());
    		}

    		public LangInfo getLangInfo() {
    			return new LangInfo(shortId.getText(), longId.getText(), label.getText());
    		}

    		public void selectAll() {
    			label.selectAll();
    			shortId.selectAll();
    			longId.selectAll();
    		}

    		public void setValid(boolean valid) {
    			Color c = valid ? Color.BLACK : Color.RED;
    			shortId.setForeground(c);
    			longId.setForeground(c);
    		}

    		public void addActionListener(ActionListener l) {
    			label.addActionListener(l);
    			shortId.addActionListener(l);
    			longId.addActionListener(l);
    		}

    		public void removeActionListener(ActionListener l) {
    			label.removeActionListener(l);
    			shortId.removeActionListener(l);
    			longId.removeActionListener(l);
    		}

    		/**
    		 * Adds document listeners to the id fields.
    		 * The label field requires no validation.
    		 */
    		public void addDocumentListener(DocumentListener l) {
    			shortId.getDocument().addDocumentListener(l);
    			longId.getDocument().addDocumentListener(l);
    		}

//    		public void removeDocumentListener(DocumentListener l) {
//    			shortId.getDocument().removeDocumentListener(l);
//    			longId.getDocument().removeDocumentListener(l);
//    		}
    	}
    }
}
