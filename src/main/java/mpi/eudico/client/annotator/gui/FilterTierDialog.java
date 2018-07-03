package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * A dialog that offers the user an interface to select a source and
 * destination tier  for a filter operation. The content of each annotation,
 * minus the strings specified  in the filterlist, of the source tier is
 * copied to a (new) annotation on the  destination tier.
 *
 * @author Han Sloetjes
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public class FilterTierDialog extends AbstractTwoTierOpDialog {
    private JPanel extraOptionsPanel;
    private JLabel filterLabel;
    private JTextField filterField;
    private JButton addFilterButton;
    private JButton removeFilterButton;
    private JList filterList;

    /**
     * Constructs a new FilterTier dialog.
     *
     * @param transcription the transcription
     * @param identity the identity
     */
    public FilterTierDialog(Transcription transcription) {
        super(transcription);

        //initComponents();
        initOptionsPanel();
        updateLocale();
        loadPreferences();
        
        //extractSourceTiers();
        postInit();
    }

    /**
     * Initialise the ui elements that are specific to this operation.
     */
    protected void initOptionsPanel() {
        GridBagConstraints gridBagConstraints;
        Insets insets = new Insets(2, 0, 2, 6);

        extraOptionsPanel = new JPanel(new GridBagLayout());
        filterLabel = new JLabel();
        filterField = new JTextField();
        addFilterButton = new JButton();
        removeFilterButton = new JButton();
        filterList = new JList(new DefaultListModel());

        JScrollPane listPane = new JScrollPane(filterList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(filterLabel, gridBagConstraints);

        filterField.setColumns(6);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(filterField, gridBagConstraints);

        addFilterButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        extraOptionsPanel.add(addFilterButton, gridBagConstraints);

        removeFilterButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        extraOptionsPanel.add(removeFilterButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = insets;
        extraOptionsPanel.add(listPane, gridBagConstraints);

        addOptionsPanel(extraOptionsPanel);

        // add an action to the action and input map
        RemoveFiltersAction rfa = new RemoveFiltersAction();
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        if (inputMap instanceof ComponentInputMap && (actionMap != null)) {
            inputMap.put((KeyStroke) rfa.getValue(Action.ACCELERATOR_KEY),
                rfa.getValue(Action.DEFAULT));
            actionMap.put(rfa.getValue(Action.DEFAULT), rfa);
        }

        if (filterField.getPreferredSize() != null) {
            filterField.setMinimumSize(filterField.getPreferredSize());
        }

        if (listPane.getPreferredSize() != null) {
            listPane.setMinimumSize(listPane.getPreferredSize());
        }
    }

    /**
     * Find the child tiers that are suitable to be the destination of this
     * operation.
     *
     * @see mpi.eudico.client.annotator.AbstractTwoTierOpDialog#extractDestinationTiers()
     */
    @Override
	protected void extractDestinationTiers() {
        destTierComboBox.removeAllItems();
        destTierComboBox.addItem(EMPTY);

        if ((sourceTierComboBox.getSelectedItem() != null) &&
                (sourceTierComboBox.getSelectedItem() != EMPTY)) {
            String name = (String) sourceTierComboBox.getSelectedItem();
            TierImpl source = transcription.getTierWithId(name);

            for (TierImpl dest : source.getDependentTiers()) {
            	LinguisticType lt = dest.getLinguisticType();

                if ((dest.getParentTier() == source) &&
                        (lt.getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)) {
                    destTierComboBox.addItem(dest.getName());
                }
            }
            if (destTierComboBox.getItemCount() > 1) {
            	destTierComboBox.removeItem(EMPTY);
            }
        }
    }

    /**
     * Start the filtering action.
     *
     * @see mpi.eudico.client.annotator.AbstractTwoTierOpDialog#startOperation()
     */
    @Override
	protected void startOperation() {
        // do some checks, spawn warning messages
        String sourceName = (String) sourceTierComboBox.getSelectedItem();
        String destName = (String) destTierComboBox.getSelectedItem();

        boolean preserveExisting = preserveRB.isSelected();
        boolean createEmptyAnnotations = emptyAnnCheckBox.isSelected();

        if ((sourceName == EMPTY) || (destName == EMPTY)) {
            //warn and return...
            showWarningDialog(ElanLocale.getString(
                    "TokenizeDialog.Message.InvalidTiers"));

            return;
        }

        String[] filters = null;
        Object[] listObj = ((DefaultListModel) filterList.getModel()).toArray();

        if ((listObj != null) && (listObj.length > 0)) {
            filters = new String[listObj.length];

            for (int i = 0; i < listObj.length; i++) {
                filters[i] = (String) listObj[i];
            }
        }
        storePreferences();
        //need a command because of undo / redo mechanism
        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.FILTER_TIER);
        Object[] args = new Object[5];
        args[0] = sourceName;
        args[1] = destName;
        args[2] = filters;
        args[3] = Boolean.valueOf(preserveExisting);
        args[4] = Boolean.valueOf(createEmptyAnnotations);
        com.execute(transcription, args);
    }

    /**
     * Add the current string in the filter textfield to the set of filters.
     */
    protected void addFilter() {
        String text = filterField.getText();

        if ((text != null) && (text.length() > 0)) {
            // don't add it twice
            int size = filterList.getModel().getSize();

            for (int i = 0; i < size; i++) {
                String item = (String) filterList.getModel().getElementAt(i);

                if (text.equals(item)) {
                    return;
                }
            }

            ((DefaultListModel) filterList.getModel()).addElement(text);
            filterField.setText("");
            filterField.requestFocus();
        }
    }

    /**
     * Removes the currently selected filters from the list of filters.
     */
    protected void removeFilter() {
        int[] selected = filterList.getSelectedIndices();

        if ((selected != null) && (selected.length > 0)) {
            for (int i = selected.length - 1; i >= 0; i--) {
                ((DefaultListModel) filterList.getModel()).removeElementAt(selected[i]);
            }
        }
    }

    /**
     * Applies localized strings to the ui elements.
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("FilterDialog.Title"));
        titleLabel.setText(ElanLocale.getString("FilterDialog.Title"));
        filterLabel.setText(ElanLocale.getString("FilterDialog.Label.Filter"));
        addFilterButton.setText(ElanLocale.getString("FilterDialog.Button.Add"));
        removeFilterButton.setText(ElanLocale.getString(
                "FilterDialog.Button.Remove"));
    }
    
    /**
     * Stores choices as preferences.
     */
    private void storePreferences() {
    	Preferences.set("FilterDialog.Overwrite", overwriteRB.isSelected(), null, false, false);
    	Preferences.set("FilterDialog.ProcessEmptyAnnotations", emptyAnnCheckBox.isSelected(), null, false, false);
    }
    
    /**
     * Restores choices as preferences.
     */
    private void loadPreferences() {
    	Boolean val = null;
    	
    	val = Preferences.getBool("FilterDialog.Overwrite", null);
    	if (val != null) {
    		boolean overwr = val.booleanValue();
    		if (overwr) {
    			overwriteRB.setSelected(true);
    		} else {
    			preserveRB.setSelected(true);
    		}
    	}
    	val = Preferences.getBool("FilterDialog.ProcessEmptyAnnotations", null);
    	if (val != null) {
    		emptyAnnCheckBox.setSelected(val);
    	}
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == addFilterButton) {
            addFilter();
        } else if (source == removeFilterButton) {
            removeFilter();
        } else {
            super.actionPerformed(ae);
        }
    }

    /**
     * An action to remove filters from the list.
     *
     * @author Han Sloetjes
     */
    class RemoveFiltersAction extends AbstractAction {
        /**
         * Constructor, sets the accelerator key to the VK_DELETE key.
         */
        public RemoveFiltersAction() {
            putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            putValue(Action.DEFAULT, "RemoveFilter");
        }

        /**
         * Removes selected filters from the list.
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
		public void actionPerformed(ActionEvent e) {
            if ((filterList != null) &&
                    (filterList.getSelectedIndices().length > 0)) {
                Object[] selObjects = filterList.getSelectedValues();

                for (Object selObject : selObjects) {
                    ((DefaultListModel) filterList.getModel()).removeElement(selObject);
                }
            }
        }
    }
}
