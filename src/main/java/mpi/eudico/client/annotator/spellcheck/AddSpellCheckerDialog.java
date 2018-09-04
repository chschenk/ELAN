package mpi.eudico.client.annotator.spellcheck;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.prefs.gui.RecentLanguagesBox;
import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.util.Pair;
import mpi.eudico.util.multilangcv.LangInfo;
import mpi.eudico.util.multilangcv.LanguageCollection;

/**
 * Dialog for creating a new spell checker.
 * 
 * When clicking OK a spell checker object is created by the SpellCheckerFactory
 * and the spell checker is initialized.
 * 
 * @author michahulsbosch
 *
 */
public class AddSpellCheckerDialog extends ClosableDialog implements ActionListener, ItemListener {
	private static final int DEFAULT_MINIMUM_HEIGHT = 260;
    private static final int DEFAULT_MINIMUM_WIDTH = 700;
   
    private JPanel titlePanel;
	private JLabel titleLabel;
	private JPanel buttonPanel;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel dataFieldPanel;
	private JComboBox checkerTypeBox;
	private JComboBox newLanguageComboBox;	// contains all available languages
	List<LangInfo> languageList;
	List<JTextField> textFields = new ArrayList<JTextField>();
	Map<String, JTextField> textFieldMap = new HashMap<String, JTextField>();
	private SpellChecker spellChecker;
	private LangInfo langInfo;
	private JLabel checkerTypeLabel;
	private JLabel languageLabel;
	
	private Transcription transcription = null;
	
	/**
	 * Constructor
	 * @param owner
	 * @param modal
	 */
	public AddSpellCheckerDialog(JDialog owner, boolean modal, Transcription transcription) {
		super(owner, modal);
		this.transcription = transcription;
		initComponents();
		postInit();
	}
	
	/**
	 * Initializes the GUI components
	 */
	private void initComponents() {
		dataFieldPanel = new JPanel(new BorderLayout());
		
		GridBagConstraints gridBagConstraints;
		Container dialogPane = this.getContentPane();
		
		dialogPane.setLayout(new GridBagLayout());

		Insets insets = new Insets(2, 6, 2, 6);
		
		titlePanel = new JPanel();
		titleLabel = new JLabel();
		titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
		titlePanel.add(titleLabel);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.insets = insets;
		dialogPane.add(titlePanel, gridBagConstraints);

		JPanel checkerInfoPanel = new JPanel();
		checkerInfoPanel.setLayout(new GridBagLayout());

		checkerTypeLabel = new JLabel();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		checkerInfoPanel.add(checkerTypeLabel, gridBagConstraints);

		checkerTypeBox = new JComboBox();
		checkerTypeBox.addItemListener(this);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = insets;
		checkerInfoPanel.add(checkerTypeBox, gridBagConstraints);

		String[] spellCheckerTypes = SpellCheckerFactory.getTypes();
		for(SpellCheckerType type : SpellCheckerType.values()) {
			checkerTypeBox.addItem(type);
		}
		
		// Languages
		languageLabel = new JLabel();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		checkerInfoPanel.add(languageLabel, gridBagConstraints);

		if(transcription != null) {
			newLanguageComboBox = new RecentLanguagesBox(null);
		} else {
			newLanguageComboBox = getNewLanguageComboBox();
		}
		newLanguageComboBox.addItemListener(this);
        newLanguageComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = insets;
		checkerInfoPanel.add(newLanguageComboBox, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		dialogPane.add(checkerInfoPanel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		dialogPane.add(dataFieldPanel, gridBagConstraints);
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 1, 0, 2));

		okButton = new JButton();
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		
		cancelButton = new JButton();
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = insets;
		dialogPane.add(buttonPanel, gridBagConstraints);

	    setLocationRelativeTo(getParent());
	}

	/**
	 * Stuff to do after initialization of GUI components
	 */
	private void postInit() {
		addCloseActions();
		updateLocale();
		setPosition();
		updateNewLanguageComboBox();
	}

	/**
	 * Update all labels according to the current locale
	 */
	private void updateLocale() {
		titleLabel.setText(ElanLocale.getString("AddSpellCheckerDialog.Label.Add"));
		checkerTypeLabel.setText(ElanLocale.getString("AddSpellCheckerDialog.Label.Type"));
		languageLabel.setText(ElanLocale.getString("Button.Language"));
		okButton.setText(ElanLocale.getString("Button.OK"));
		cancelButton.setText(ElanLocale.getString("Button.Cancel"));
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if(arg0.getSource() == checkerTypeBox) {
			updateFieldsForSelectedType();
		} else if(arg0.getSource() == newLanguageComboBox) {
			langInfo = (LangInfo) newLanguageComboBox.getSelectedItem();
		}
	}
	
	/**
	 * Changes the dataFieldPanel according to the currently selected spell checker type
	 */
	private void updateFieldsForSelectedType() {
		SpellCheckerType type = (SpellCheckerType) checkerTypeBox.getSelectedItem();
		dataFieldPanel.removeAll();
		dataFieldPanel.add(createPanelForType(type), BorderLayout.CENTER);
		dataFieldPanel.invalidate();
		dataFieldPanel.revalidate();
		dataFieldPanel.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == okButton) {
			createSpellChecker();
			closeDialog();
		} else if(ae.getSource() == cancelButton) {
			closeDialog();
		}
	}
	
	/**
	 * Creates a spell checker with the settings in this dialog.
	 * Typically called when user is done editing the setting, 
	 * i.e. when clicking OK.
	 */
	private void createSpellChecker() {
		HashMap<String, String> spellCheckerSettings = new HashMap<String, String>();
		for(Map.Entry<String, JTextField> entry : textFieldMap.entrySet()) {
			spellCheckerSettings.put(entry.getKey(), entry.getValue().getText());
		}
		try {
			spellChecker = SpellCheckerFactory.create((SpellCheckerType) checkerTypeBox.getSelectedItem(), spellCheckerSettings);
			spellChecker.initializeSpellChecker();
		} catch (SpellCheckerInitializationException e) {
			if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
            	ClientLogger.LOG.warning("Could not create a spell checker (" + e.getMessage() + ")");
            }
		}
	}
	
	/**
	 * Closes the dialog
	 */
	protected void closeDialog() {
		setVisible(false);
		dispose();
	}
	
	/**
	 * 
	 * @return the created spell checker
	 */
	public SpellChecker getSpellChecker() {
		return spellChecker;
	}
	
	/**
	 * 
	 * @return the chosen LangInfo 
	 */
	public LangInfo getLangInfo() {
		return langInfo;
	}
	
	/**
	 * Creates a custom form panel for the selected spell checker type 
	 * @param type
	 * @return
	 */
	private JPanel createPanelForType(SpellCheckerType type) {
		textFieldMap.clear();
		ArrayList<Pair<String, String>> fields = SpellCheckerFactory.getDataFields(type);
		//int numberOfFields = fields.size();
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gridBagConstraints;
		Insets insets = new Insets(2, 6, 2, 6);
		int gridy = 0;
	
		for(Pair<String, String> field : fields) {
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = gridy;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints.insets = insets;
			JLabel label = new JLabel(ElanLocale.getString(field.getSecond()));
			panel.add(label, gridBagConstraints);
			
			
			final JTextField textField;
			if(field.getFirst().equals("password")) {
				textField = new JPasswordField();
			} else if(field.getFirst().equals("path")) {
				textField = new JTextField();
				textField.setEditable(false);
				gridBagConstraints = new GridBagConstraints();
				gridBagConstraints.gridx = 2;
				gridBagConstraints.gridy = gridy;
				gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
				gridBagConstraints.insets = insets;
				JButton browseButton = new JButton();
				browseButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						getFile(textField);
					}
				});
				browseButton.setText(ElanLocale.getString("Button.Browse"));
				panel.add(browseButton, gridBagConstraints);
			} else {
				textField = new JTextField();
			}
			textFieldMap.put(field.getFirst(), textField);
			label.setLabelFor(textField);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = gridy;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints.insets = insets;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			panel.add(textField, gridBagConstraints);
			gridy++;
		}
		panel.validate();
		return panel;
	}
	
	/**
     * Prompts the user to select a file.
     * @param 
     */
    private void getFile(JTextComponent textComponent) {
    	
    	ArrayList<String[]> extensions = new ArrayList<String[]>();
        extensions.add(new String[] {"dic"});
        
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("HunspellChecker.DataField.Browse.Title"), 
        		FileChooser.OPEN_DIALOG, new String[] { "dic" }, null);
        File impFile = chooser.getSelectedFile();
        if(impFile != null) {
        	textComponent.setText(impFile.getAbsolutePath());
        }
    }

	/**
	 * Pack, size and set location.
	 */
	protected void setPosition() {
	    pack();
	    setSize(Math.max(getSize().width, DEFAULT_MINIMUM_WIDTH),
	        Math.max(getSize().height, DEFAULT_MINIMUM_HEIGHT));
//	    setSize(700,260);
	    setLocationRelativeTo(getParent());
	}

	/**
	 * Create the combobox for the list of languages to choose from.
	 * Can be overridden if you don't want a box with all languages
	 * from the LanguageCollection.
	 */
	protected JComboBox getNewLanguageComboBox() {
		JComboBox box = new JComboBox();
	    box.setEditable(false);
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
		if(!(newLanguageComboBox instanceof RecentLanguagesBox)) {
			final List<LangInfo> languages = LanguageCollection.getLanguages();
			
			ComboBoxModel m = new LanguagesListModel(languages);
			 // speeds up initial display drastically:
			newLanguageComboBox.setPrototypeDisplayValue(languages.get(0));
			newLanguageComboBox.setModel(m);
		}
	}
	
	/**
	 * Returns the number of language in the list of languages.
	 * @return
	 */
	int getNumberOfLanguages() {
		return languageList.size();
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
}
