package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * A dialog for the tokenization of annotations on one (parent) tier  on
 * another (dependent) subdivision tier.<br>
 * Tokenization means that for  every single token (word) in one parent
 * annotation, a new annotation on a  a dependent tier is being created.
 * e.g.<br>
 * <pre>
 * |this is a test|<br>
 * |this|is|a|test|
 * </pre>
 * The default delimiters are space, tab, newline, (carriagereturn and
 * formfeed). The user can specify other delimiters.
 *
 * @author Han Sloetjes
 * @version jul 2004
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public class TokenizeDialog extends AbstractTwoTierOpDialog
    implements ActionListener, ItemListener, ChangeListener {
    private JRadioButton customDelimRB;
    private JLabel tokenDelimLabel;
    private JLabel customTokenLabel;
    private JPanel extraOptionsPanel;
    private JRadioButton defaultDelimRB;
    private JTextField customDelimField;
	private JTextField customTokenField;
    private ButtonGroup delimButtonGroup;
	private JLabel warningLabel;

    /** Holds value of property DOCUMENT ME! */
    private final char[] DEF_DELIMS = new char[] { '\t', '\n', '\r', '\f' };

    /**
     * Creates a new tokenizer dialog.
     *
     * @param transcription the transcription
     */
    public TokenizeDialog(Transcription transcription) {
        super(transcription);

        //initComponents();
        initOptionsPanel();
        updateLocale();
        loadPreferences();
        //extractSourceTiers();
        postInit();
    }

    /**
     * Extracts the candidate destination tiers for the currently selected
     * source tier.<br>
     * The destination tier must be a direct child of the source and must be
     * of type included-in, time-subdivision or symbolic-subdivision,
     * or must be a root tier which is not a parent of the source.
     */
    @Override
	protected void extractDestinationTiers() {
        destTierComboBox.removeAllItems();
        destTierComboBox.addItem(EMPTY);

        if ((sourceTierComboBox.getSelectedItem() != null) &&
                (sourceTierComboBox.getSelectedItem() != EMPTY)) {
            String name = (String) sourceTierComboBox.getSelectedItem();
            TierImpl source = transcription.getTierWithId(name);

            List<TierImpl> tiers = transcription.getTiers();

            /*
             * Choose potential destinations:
             * - direct children of the source, except in case of SYMBOLIC_ASSOCIATION
             * - any top-level tier, except if it is a parent of the source
             */
            for (TierImpl dest : tiers) {
                LinguisticType lt = dest.getLinguisticType();

                if ((!dest.hasParentTier() && !isParentOf(dest, source)) ||
                    (dest.getParentTier() == source &&
                        lt.getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION)) {
                    destTierComboBox.addItem(dest.getName());
                }
            }
            if (destTierComboBox.getItemCount() > 1) {
            	destTierComboBox.removeItem(EMPTY);
            }
        }
    }
    
    /**
     * Determine if {@code parent} is a parent tier of {@code child}.
     * @param parent potential parent
     * @param child potential child
     * @return whether that is so.
     */
    private boolean isParentOf(Tier parent, TierImpl child) {
    	while ((child = child.getParentTier()) != null) {
    		if (child == parent) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Determine if the tier has any children
     * 
     * @param parentName potential parent
     * @return whether that is so.
     */
    private boolean hasChildren(String parentName) {
    	if (parentName == null) {
    		return false;
    	}
    	
    	TierImpl parent = transcription.getTierWithId(parentName);
    	if (parent == null) {
    		return false;
    	}
    			
		for (TierImpl t : transcription.getTiers()) {
			if (t.getParentTier() == parent) {
				return true;
			}
		}

		return false;
    }

    /**
     * Performs some checks and starts the tokenization process.
     */
    @Override
	protected void startOperation() {
        // do some checks, spawn warning messages
        String sourceName = (String) sourceTierComboBox.getSelectedItem();
        String destName = (String) destTierComboBox.getSelectedItem();
        String delimsText = null;
        boolean preserveExisting = preserveRB.isSelected();
        boolean createEmptyAnnotations = emptyAnnCheckBox.isSelected();

        if ((sourceName == EMPTY) || (destName == EMPTY)) {
            //warn and return...
            showWarningDialog(ElanLocale.getString(
                    "TokenizeDialog.Message.InvalidTiers"));

            return;
        }

        if (customDelimRB.isSelected()) {
            // check if there is a valid tokenizer
            delimsText = customDelimField.getText();

            if ((delimsText == null) || (delimsText.length() == 0)) {
                showWarningDialog(ElanLocale.getString(
                        "TokenizeDialog.Message.NoDelimiter"));

                return;
            }

            // be sure tab and newline characters are part of the delimiter
            delimsText = checkDelimiters(delimsText);
        }
        
        String customTokens = customTokenField.getText();
        if (customTokens.isEmpty()) {
        	customTokens = null;
        }
        
        storePreferences();
        // if we get here we can start working...
        //need a command because of undo / redo mechanism
        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.TOKENIZE_TIER);
        Object[] args = new Object[6];
        args[0] = sourceName;
        args[1] = destName;
        args[2] = delimsText;
        args[3] = Boolean.valueOf(preserveExisting);
        args[4] = Boolean.valueOf(createEmptyAnnotations);
        args[5] = customTokens;
        com.execute(transcription, args);
    }

    /**
     * Ensures that some default characters are part of the delimiter string.
     *
     * @param delim the string to check
     *
     * @return new delimiter string
     */
    private String checkDelimiters(String delim) {
        StringBuilder buffer = new StringBuilder(delim);

        for (char element : DEF_DELIMS) {
            if (delim.indexOf(element) < 0) {
                buffer.append(element);
            }
        }

        return buffer.toString();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == destTierComboBox) {
    		Object selectedItem = destTierComboBox.getSelectedItem();
    		boolean warn = hasChildren((String)selectedItem);
    		warningLabel.setVisible(warn);
    	}
    	super.actionPerformed(e);
    };
    
    /**
     * Modifies the superclass UI elements.
     * The purpose is to show a warning if a destination tier with children
     * is chosen.
     */
    @Override
    protected void initComponents() {
    	super.initComponents();
    	
    	destTierComboBox.addActionListener(this);
    	
    	warningLabel = new JLabel();
    	warningLabel.setForeground(Color.RED);
    	warningLabel.setVisible(false);
    	
        Insets insets = new Insets(2, 6, 2, 6);
    	GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;

        tierSelectionPanel.add(warningLabel, gridBagConstraints);
    }

    /**
     * Initializes UI elements.
     */
    protected void initOptionsPanel() {
        GridBagConstraints gridBagConstraints;

        extraOptionsPanel = new JPanel();
        delimButtonGroup = new ButtonGroup();
        tokenDelimLabel = new JLabel();
        customTokenLabel = new JLabel();
        defaultDelimRB = new JRadioButton();
        customDelimRB = new JRadioButton();
        customDelimField = new JTextField();
        customTokenField = new JTextField();

        Insets insets = new Insets(2, 0, 2, 6);

        extraOptionsPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(tokenDelimLabel, gridBagConstraints);

        defaultDelimRB.setSelected(true);
        defaultDelimRB.addChangeListener(this);
        delimButtonGroup.add(defaultDelimRB);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(defaultDelimRB, gridBagConstraints);

        customDelimRB.addChangeListener(this);
        delimButtonGroup.add(customDelimRB);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(customDelimRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(customTokenLabel, gridBagConstraints);

        customDelimField.setEnabled(false);
        customDelimField.setColumns(6);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(customDelimField, gridBagConstraints);

        customTokenField.setEnabled(true);
        customTokenField.setColumns(6);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(customTokenField, gridBagConstraints);

        addOptionsPanel(extraOptionsPanel);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("TokenizeDialog.Title"));
        titleLabel.setText(ElanLocale.getString("TokenizeDialog.Title"));

        //explanatoryTA.setText(ElanLocale.getString("TokenizeDialog.Explanation"));
        tokenDelimLabel.setText(ElanLocale.getString(
                "TokenizeDialog.Label.TokenDelimiter"));
        defaultDelimRB.setText(ElanLocale.getString(
                "TokenizeDialog.RadioButton.Default"));
        customDelimRB.setText(ElanLocale.getString(
                "TokenizeDialog.RadioButton.Custom"));
        customTokenLabel.setText(ElanLocale.getString(
                "TokenizeDialog.Label.CustomTokens"));
        // "Warning: this erases all annotations on depending tiers"
        warningLabel.setText(ElanLocale.getString(
                "TokenizeDialog.Label.WarnEraseChildren"));
    }
    
    /**
     * Stores choices as preferences.
     */
    private void storePreferences() {
    	Preferences.set("TokenizeDialog.SourceTier", sourceTierComboBox.getSelectedItem(), transcription, false, false);
    	Preferences.set("TokenizeDialog.DestTier", destTierComboBox.getSelectedItem(), transcription, false, false);
		Preferences.set("TokenizeDialog.DefaultDelimiter", defaultDelimRB.isSelected(), null, false, false);
		Preferences.set("TokenizeDialog.CustomDelimiter", customDelimField.getText(), null, false, false);
		Preferences.set("TokenizeDialog.CustomTokens", customTokenField.getText(), null, false, false);
    	Preferences.set("TokenizeDialog.Overwrite", overwriteRB.isSelected(), null, false, false);
    	Preferences.set("TokenizeDialog.ProcessEmptyAnnotations", emptyAnnCheckBox.isSelected(), null, false, false);
    }
    
    /**
     * Restores choices as preferences.
     */
    private void loadPreferences() {
    	String stringPref;
    	Boolean boolPref;
    	
    	stringPref = Preferences.getString("TokenizeDialog.SourceTier", transcription);
		if (stringPref != null) {
			sourceTierComboBox.setSelectedItem(stringPref);
		}
    	stringPref = Preferences.getString("TokenizeDialog.DestTier", transcription);
		if (stringPref != null) {
			destTierComboBox.setSelectedItem(stringPref);
		}
    	boolPref = Preferences.getBool("TokenizeDialog.DefaultDelimiter", null);
    	if (boolPref != null) {
    		boolean defde = boolPref.booleanValue();
    		if (!defde) {
    			customDelimRB.setSelected(true);
    			stringPref = Preferences.getString("TokenizeDialog.CustomDelimiter", null);
    			if (stringPref != null) {
    				customDelimField.setText(stringPref);
    			}
    		}
    	}
    	stringPref = Preferences.getString("TokenizeDialog.CustomTokens", null);
		if (stringPref != null) {
			customTokenField.setText(stringPref);
		}
		boolPref = Preferences.getBool("TokenizeDialog.Overwrite", null);
    	if (boolPref != null) {
    		boolean overwr = boolPref.booleanValue();
    		if (overwr) {
    			overwriteRB.setSelected(true);
    		} else {
    			preserveRB.setSelected(true);
    		}
    	}
    	boolPref = Preferences.getBool("TokenizeDialog.ProcessEmptyAnnotations", null);
    	if (boolPref != null) {
    		emptyAnnCheckBox.setSelected(boolPref);
    	}
    }

    /**
     * The state changed event handling.
     *
     * @param ce the change event
     */
    @Override
	public void stateChanged(ChangeEvent ce) {
        if (defaultDelimRB.isSelected()) {
            customDelimField.setEnabled(false);
        } else {
            customDelimField.setEnabled(true);
            customDelimField.requestFocus();
        }
    }

    /**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    @Override
	protected void closeDialog(WindowEvent evt) {
        storePreferences();
        super.closeDialog(evt);
    }
}
