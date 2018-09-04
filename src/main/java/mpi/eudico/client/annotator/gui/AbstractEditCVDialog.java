package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;


@SuppressWarnings("serial")
public abstract class AbstractEditCVDialog extends JDialog
    implements ActionListener, ItemListener {
	private static final int DEFAULT_MINIMUM_HEIGHT = 500;
    private static final int DEFAULT_MINIMUM_WIDTH = 550;
    
    protected EditCVPanel cvEditorPanel;
    protected JButton addCVButton;
    protected JButton changeCVButton;
    protected JButton closeDialogButton;
    protected JButton deleteCVButton;
    protected JComboBox cvComboBox;
    protected JLabel currentCVLabel;
    protected JComboBox cvLanguageComboBox;
    protected JLabel currentCVLanguageLabel;
    protected JLabel cvDescLabel;
    protected JLabel cvNameLabel;
    protected JLabel titleLabel;
    protected JPanel cvButtonPanel;
    protected JPanel cvPanel;
    protected JTextArea cvDescArea;
    protected JTextField cvNameTextField;
    protected String cvContainsEntriesMessage = "contains entries.";
    protected String cvInvalidNameMessage = "Invalid name.";
    protected String cvNameExistsMessage = "Name exists already.";
    protected String deleteQuestion = "delete anyway?";
    protected String oldCVDesc;

    // internal caching fields
    protected String oldCVName;
    protected int minimumHeight;
    protected int minimumWidth;
    private final boolean multipleCVs;
	private int editLanguagesNumber;
	private Color defTextFieldBgColor;
	//private Color defLabelFgColor;

    /**
     * Constructor with standard EditCVPanel
     * @param parent parent window
     * @param modal modality
     * @param multipleCVs if true, user can edit more than one CV
     */
    public AbstractEditCVDialog(Frame parent, boolean modal, boolean multipleCVs) {
        this(parent, modal, multipleCVs, new EditCVPanel());
    }

    /**
     *
     * @param parent parent window
     * @param modal modality
     * @param multipleCVs if true, user can edit more than one CV
     * @param cvEditorPanel panel which might have already controlled vocabulary
     */
    public AbstractEditCVDialog(Frame parent, boolean modal,
        boolean multipleCVs, EditCVPanel cvEditorPanel) {
        super(parent, modal);
        this.cvEditorPanel = cvEditorPanel;
        this.multipleCVs = multipleCVs;
        minimumHeight = DEFAULT_MINIMUM_HEIGHT;
        minimumWidth = DEFAULT_MINIMUM_WIDTH;
        makeLayout();
    }

    /**
     * The button actions.
     *
     * @param actionEvent the actionEvent
     */
    @Override
	public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        // check source equality
        if (source == closeDialogButton) {
            closeDialog();
        } else if (source == addCVButton) {
            addCV();
        } else if (source == changeCVButton) {
            changeCV();
        } else if (source == deleteCVButton) {
            deleteCV();
        }
    }

    /**
     * Handles a change in the cv selection.
     * Implements ItemListener.
     *
     * @param ie the item event
     */
    @Override
	public void itemStateChanged(ItemEvent ie) {
    	ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();
    	
        if (ie.getSource() == cvComboBox) {
            if (ie.getStateChange() == ItemEvent.SELECTED) {
                cvEditorPanel.setControlledVocabulary(cv);
            }

            updateLanguageComboBox();
            updateCVButtons();
        } else
        if (ie.getSource() == cvLanguageComboBox) {
            if (ie.getStateChange() == ItemEvent.SELECTED) {
            	int languageIndex = cvLanguageComboBox.getSelectedIndex();
            	if (cv != null) {
            		// Add a language?
            		if (languageIndex == editLanguagesNumber) {
            			new EditCVLanguagesDialog(this, cv).setVisible(true);
            			updateLanguageComboBox();
                        cvEditorPanel.setControlledVocabulary(cv);
            		} else {
                  		// Select a language
            			updateCVButtons();
            		}
            	}
            }
        }
    }

    /**
     * test method with an cv list of size 1
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        AbstractEditCVDialog dialog = new AbstractEditCVDialog(frame, false,
                true) {
        		private List<ControlledVocabulary> cvList;
                @Override
				protected List<ControlledVocabulary> getCVList() {
                	if (cvList == null) {
                		cvList = new ArrayList<ControlledVocabulary>();
                		cvList.add(new ControlledVocabulary("name"));
                	}
                    return cvList;
                }
            };

        dialog.updateComboBox();
        dialog.pack();
        dialog.setVisible(true);
    }

    protected abstract List<ControlledVocabulary> getCVList();

    /**
     * Pack, size and set location.
     */
    protected void setPosition() {
        pack();
        setSize(Math.max(getSize().width, DEFAULT_MINIMUM_WIDTH),
            Math.max(getSize().height, DEFAULT_MINIMUM_HEIGHT));
        setLocationRelativeTo(getParent());
    }

    /**
     * check if name is valid
     *
     */
    protected void addCV() {
        String name = cvNameTextField.getText();

        name = name.trim();

        if (name.length() == 0) {
            showWarningDialog(cvInvalidNameMessage);

            return;
        }

        if (cvExists(name)) {
            // cv with that name already exists, warn
            showWarningDialog(cvNameExistsMessage);

            return;
        }

        addCV(name);
    }

    /**
     * Creates a new ControlledVocabulary when there isn't already one with the
     * same name and adds it to the List.
     *
     * @param name name of new CV
     */
    protected void addCV(String name) {
        ControlledVocabulary cv = new ControlledVocabulary(name, "");
        cvComboBox.addItem(cv);
        cvEditorPanel.setControlledVocabulary(cv);
    }

    /**
     * Checks whether name is valid and unique.
     */
    protected void changeCV() {
        ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();

        if (cv == null) {
            return;
        }

        String name = cvNameTextField.getText();
        String desc = cvDescArea.getText();

        if (name != null) {
            name = name.trim();

            if (name.length() < 1) {
                showWarningDialog(cvInvalidNameMessage);
                cvNameTextField.setText(oldCVName);

                return;
            }
        }

        if ((oldCVName != null) && !oldCVName.equals(name)) {
            // check if there is already a cv with the new name
            if (cvExists(name)) {
                // cv with that name already exists, warn
                showWarningDialog(cvNameExistsMessage);

                return;
            }

            changeCV(cv, name, desc);
        } else if (((oldCVDesc == null) && (desc != null) &&
                (desc.length() > 0)) ||
                ((oldCVDesc != null) &&
                ((desc == null) || (desc.length() == 0))) ||
                ((oldCVDesc != null) && !oldCVDesc.equals(desc))) {
            changeCV(cv, null, desc);
        }
    }

    /**
     * changes name and description in specified ControlledVocabulary
     * @param cv ControlledVocabulary to be changed
     * @param name new name (may be null -> no change of name!)
     * @param description new description
     */
    protected void changeCV(ControlledVocabulary cv, String name,
        String description) {
    	int languageIndex = cvLanguageComboBox.getSelectedIndex();
        cv.setDescription(languageIndex, description);

        if (name != null) {
            cv.setName(name);
            cvEditorPanel.setControlledVocabulary(cv);
            cvLanguageComboBox.setSelectedIndex(languageIndex);
        }
    }

    /**
    * Closes the dialog
    */
    protected void closeDialog() {
    	for (ControlledVocabulary cv : getCVList()) {
    		if (cv.isChanged()) {
    	    	// get all Transcriptions to update their CVE-linked Annotations.
				String lang = Preferences.getString(Preferences.PREF_ML_LANGUAGE, null);
				if (lang != null) {
					Preferences.updateAllCVLanguages(lang, true);
				}
    	    	break;
    		}
    	}
    	setVisible(false);
        dispose();
    }

    /**
     *
     * @param name
     * @return true if ControlledVocabulary with specified name is in the list
     */
    protected boolean cvExists(String name) {
        boolean nameExists = false;

        for (int i = 0; i < cvComboBox.getItemCount(); i++) {
            if (((ControlledVocabulary) cvComboBox.getItemAt(i)).getName()
                     .equals(name)) {
                nameExists = true;

                break;
            }
        }

        return nameExists;
    }

    /**
     * If cv not empty, ask the user for confirmation.
     */
    protected void deleteCV() {
        ControlledVocabulary conVoc = (ControlledVocabulary) cvComboBox.getSelectedItem();

        if (!conVoc.isEmpty()) {
            String mes = cvContainsEntriesMessage + " " + deleteQuestion;

            if (!showConfirmDialog(mes)) {
                return;
            }
        }

        deleteCV(conVoc);
    }

    /**
     * Deletes controlled vocabulary from the list
     * @param cv ControlledVocabulary to be deleted
     */
    protected void deleteCV(ControlledVocabulary cv) {
        cvComboBox.removeItem(cv);

        if (cvComboBox.getItemCount() > 0) {
            cvComboBox.setSelectedIndex(0);
        } else {
            cvEditorPanel.setControlledVocabulary(null);
        }
    }

    /**
    * makes layout
    */
    protected void makeLayout() {
        JPanel closeButtonPanel;
        JPanel titlePanel;

        GridBagConstraints gridBagConstraints;

        cvPanel = new JPanel();
        currentCVLabel = new JLabel();
        cvComboBox = new JComboBox();
        currentCVLanguageLabel = new JLabel();
        cvLanguageComboBox = new JComboBox();
        cvNameLabel = new JLabel();
        cvNameTextField = new JTextField();
        cvDescLabel = new JLabel();
        cvDescArea = new JTextArea();
        cvButtonPanel = new JPanel();
        addCVButton = new JButton();
        changeCVButton = new JButton();
        changeCVButton.setEnabled(false);
        deleteCVButton = new JButton();
        deleteCVButton.setEnabled(false);

        closeButtonPanel = new JPanel();
        closeDialogButton = new JButton();
        titlePanel = new JPanel();
        titleLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                    closeDialog();
                }
            });

        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        getContentPane().add(titlePanel, gridBagConstraints);

        cvPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(currentCVLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvComboBox, gridBagConstraints);

        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(currentCVLanguageLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvLanguageComboBox, gridBagConstraints);
        
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvNameLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        cvPanel.add(cvNameTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;

        cvPanel.add(cvDescLabel, gridBagConstraints);
        cvDescArea.setLineWrap(true);
        cvDescArea.setWrapStyleWord(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;

        cvPanel.add(new JScrollPane(cvDescArea), gridBagConstraints);

        cvButtonPanel.setLayout(new GridLayout(0, 1, 6, 6));

        addCVButton.addActionListener(this);
        cvButtonPanel.add(addCVButton);

        changeCVButton.addActionListener(this);
        cvButtonPanel.add(changeCVButton);

        deleteCVButton.addActionListener(this);
        cvButtonPanel.add(deleteCVButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvButtonPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;

        if (multipleCVs) {
            getContentPane().add(cvPanel, gridBagConstraints);
        }

        //
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(cvEditorPanel, gridBagConstraints);

        closeButtonPanel.setLayout(new GridLayout(1, 1, 0, 2));

        closeDialogButton.addActionListener(this);
        closeButtonPanel.add(closeDialogButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        getContentPane().add(closeButtonPanel, gridBagConstraints);

        InputMap iMap = ((JComponent) getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap aMap = ((JComponent) getContentPane()).getActionMap();

        if ((iMap != null) && (aMap != null)) {
            final String esc = "Esc";
            final String enter = "Enter";
            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), esc);
            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter);
            aMap.put(esc, new EscapeAction());
            aMap.put(enter, new EnterAction());
        }
        cvComboBox.setRenderer(new CVListRenderer());
        defTextFieldBgColor = cvDescArea.getBackground();
        //defLabelFgColor = cvComboBox.getForeground();
    }

    /**
     * Shows a confirm (yes/no) dialog with the specified message string.
     *
     * @param message the message to display
     *
     * @return true if the user clicked OK, false otherwise
     */
    protected boolean showConfirmDialog(String message) {
        int confirm = JOptionPane.showConfirmDialog(this, message, "Warning",
                JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_OPTION;
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning",
            JOptionPane.WARNING_MESSAGE);
    }

    protected void updateCVButtons() {
        ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();
        boolean isExternal = cv instanceof ExternalCV;
        int lang = Math.max(0, cvLanguageComboBox.getSelectedIndex());
        changeCVButton.setEnabled(cv != null);
        deleteCVButton.setEnabled(cv != null);
        cvNameTextField.setText((cv != null) ? cv.getName() : "");
        cvDescArea.setText((cv != null) ? cv.getDescription(lang) : "");
        oldCVName = (cv != null) ? cv.getName() : null;
        oldCVDesc = (cv != null) ? cv.getDescription(lang) : null;
        if (isExternal) {
        	//cvComboBox.setForeground(Constants.ACTIVEANNOTATIONCOLOR);
        	cvNameTextField.setBackground(Constants.LIGHT_YELLOW);
        	cvDescArea.setBackground(Constants.LIGHT_YELLOW);
        } else {
        	//cvComboBox.setForeground(defLabelFgColor);
        	cvNameTextField.setBackground(defTextFieldBgColor);
        	cvDescArea.setBackground(defTextFieldBgColor);
        }
    }

    /**
     * Extracts the CVs from the transcription and fills the cv combobox.
     */
    protected void updateComboBox() {
        cvComboBox.removeItemListener(this);

        // extract
        List<ControlledVocabulary> v = getCVList();
        cvComboBox.removeAllItems();

        for (int i = 0; i < v.size(); i++) {
            cvComboBox.addItem(v.get(i));
        }

        if (v.size() > 0) {
            cvComboBox.setSelectedIndex(0);
            cvEditorPanel.setControlledVocabulary((ControlledVocabulary) cvComboBox.getItemAt(
                    0));
        }

        updateCVButtons();

        cvComboBox.addItemListener(this);
        
        updateLanguageComboBox();
    }

    /**
     * Extracts the languages from the CV and fills the cv language combobox.
     */
    protected void updateLanguageComboBox() {
        cvLanguageComboBox.removeItemListener(this);

        ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();
        if (cv == null) {
        	return;
        }
        int nLangs = cv.getNumberOfLanguages();
        
        cvLanguageComboBox.removeAllItems();
        
        for (int i = 0; i < nLangs; i++) {
        	String id = cv.getLanguageId(i);
        	String longId = cv.getLongLanguageId(i);
        	String label = cv.getLanguageLabel(i);
        	String item = id + " - " + label;
        	cvLanguageComboBox.addItem(item);
        }
        // The BasicControlledVocabulary has some magic where it starts constructed with
        // one default language, but calling addLanguage() for the first time modifies
        // that language instead of really adding a new one.
        cvLanguageComboBox.addItem(ElanLocale.getString("EditCVDialog.Label.EditLanguages"));
        editLanguagesNumber = nLangs;
        
       	cvLanguageComboBox.setSelectedIndex(0);

        cvLanguageComboBox.addItemListener(this);
    }
    /**
     * Since this dialog is meant to be modal a Locale change while this dialog
     * is open  is not supposed to happen. This will set the labels etc. using
     * the current locale  strings.
     */
    protected void updateLabels() {
        closeDialogButton.setText("Close");
        deleteCVButton.setText("Delete");
        changeCVButton.setText("Change");
        addCVButton.setText("Add");
        cvNameLabel.setText("Name");
        cvDescLabel.setText("Description");
        currentCVLabel.setText("Current");
    }

    /**
     * An action to put in the dialog's action map and that is being performed
     * when the enter key has been hit.
     *
     * @author Han Sloetjes
     */
    protected class EnterAction extends AbstractAction {
        /**
         * The action that is performed when the enter key has been hit.
         *
         * @param ae the action event
         */
        @Override
		public void actionPerformed(ActionEvent ae) {
            Component com = AbstractEditCVDialog.this.getFocusOwner();

            if (com instanceof JButton) {
                ((JButton) com).doClick();
            }
        }
    }

    ////////////
    // action classes for handling escape and enter key.
    ////////////

    /**
     * An action to put in the dialog's action map and that is being performed
     * when the escape key has been hit.
     *
     * @author Han Sloetjes
     */
    protected class EscapeAction extends AbstractAction {
        /**
         * The action that is performed when the escape key has been hit.
         *
         * @param ae the action event
         */
        @Override
		public void actionPerformed(ActionEvent ae) {
            AbstractEditCVDialog.this.closeDialog();
        }
    }
    
    /**
     * A renderer that marks External CV's in the combo box with a different 
     * background color. 
     */
    protected class CVListRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			if (value instanceof ExternalCV) {
				if (!isSelected) {
					c.setBackground(Constants.LIGHT_YELLOW);
				}
			}
			return c;
		}    	
    }
}
