package mpi.eudico.client.annotator.interlinear;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;


/**
 * A class to that provides ui elements to manipulate the font size of tiers.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TierFontSizeDlg extends ClosableDialog implements ActionListener,
    ItemListener {
    private Map<String, Integer> tierMap;

    // the order of the tier names
    private List<String> tierNames;

    /** possible fontsizes */
    private final int[] fontSizes = {
        8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72
    };

    // ui
    private JComboBox tierComboBox;
    private JComboBox fontSizeComboBox;
    private JButton applyToAllButton;

    /**
     * Creates a new TierFontSizeDlg instance. The map contains the current
     * tiername - fontsize mappings.
     *
     * @param parent the parent dialog
     * @param modal the modal state
     * @param tierMap the mapping of tier names to font sizes
     */
    public TierFontSizeDlg(Dialog parent, boolean modal, Map<String, Integer> tierMap) {
        super(parent, modal);
        this.tierMap = tierMap;
        initComponents();
        pack();
        setResizable(false);
    }

    /**
     * Creates a new TierFontSizeDlg instance.
     *
     * @param parent the parent dialog
     * @param modal the modal state
     * @param tierMap the mapping of tier names to font sizes
     * @param fontNames a list of the tiernames
     */
    public TierFontSizeDlg(Dialog parent, boolean modal, Map<String, Integer> tierMap,
        List<String> fontNames) {
        super(parent, modal);
        this.tierMap = tierMap;
        this.tierNames = fontNames;
        initComponents();
        pack();
        setResizable(false);
    }

    /**
     * Creates a new TierFontSizeDlg instance.
     *
     * @param parent the parent frame
     * @param modal the modal state
     * @param tierMap the mapping of tier names to font sizes
     */
    public TierFontSizeDlg(Frame parent, boolean modal, Map<String, Integer> tierMap) {
        super(parent, modal);
        this.tierMap = tierMap;
        initComponents();
        pack();
        setResizable(false);
    }

    /**
     * Creates a new TierFontSizeDlg instance.
     *
     * @param parent the parent frame
     * @param modal the modal state
     * @param tierMap the mapping of tier names to font sizes
     * @param tierNames a list of the tiernames
     */
    public TierFontSizeDlg(Frame parent, boolean modal, Map<String, Integer> tierMap,
        ArrayList<String> tierNames) {
        super(parent, modal);
        this.tierMap = tierMap;
        this.tierNames = tierNames;
        initComponents();
        pack();
        setResizable(false);
    }

    /**
     * Initialise ui components.
     */
    private void initComponents() {
        tierComboBox = new JComboBox();
        fontSizeComboBox = new JComboBox();
        applyToAllButton = new JButton();
        fillTierComboBox();
        fillFontSizeComboBox();

        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        Insets insets = new Insets(2, 2, 2, 2);
		
		tierComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        getContentPane().add(tierComboBox, c);

		fontSizeComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        getContentPane().add(fontSizeComboBox, c);

        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        getContentPane().add(applyToAllButton, c);

        setDefaultValues();
        updateForLocale();
        tierComboBox.addItemListener(this);
        fontSizeComboBox.addItemListener(this);
        applyToAllButton.addActionListener(this);
    }

    /**
     * Add the tier names to a combo box. If a separate list of tier names  is
     * provided then use this list. Otherwise add all keys from the tier -
     * font map.
     */
    private void fillTierComboBox() {
        if (tierNames != null) {
            for (int i = 0; i < tierNames.size(); i++) {
                tierComboBox.addItem(tierNames.get(i));
            }
        } else if (tierMap != null) {
            Set<String> tiers = tierMap.keySet();
            Iterator<String> tierIt = tiers.iterator();

            while (tierIt.hasNext()) {
                tierComboBox.addItem(tierIt.next());
            }
        }
    }

    /**
     * Adds the fontsizes to the combo box.
     */
    private void fillFontSizeComboBox() {
        for (int i = 0; i < fontSizes.length; i++) {
            fontSizeComboBox.addItem(Integer.valueOf(fontSizes[i]));
        }
    }

    /**
     * Updates the font size combobox for the first item in the tier list.
     */
    private void setDefaultValues() {
        String tierName = (String) tierComboBox.getSelectedItem();
        updateFontSizeComboBox(tierName);
    }

    /**
     * Updates the fontsize combobox for the selected tier.
     *
     * @param tierName the name of the tier.
     */
    private void updateFontSizeComboBox(String tierName) {
        int size = 12;

        if ((tierMap != null) && (tierName != null)) {
            Object fs = tierMap.get(tierName);

            if (fs != null) {
                fontSizeComboBox.setSelectedItem(fs);
            } else {
                fontSizeComboBox.setSelectedItem(Integer.valueOf(size));
            }
        } else {
            fontSizeComboBox.setSelectedItem(Integer.valueOf(size));
        }
    }

    /**
     * Apply a change to the tiername - fontsize map.
     *
     * @param newSize the new fontsize for the selected tier
     */
    private void changeFontSize(Integer newSize) {
        String tierName = (String) tierComboBox.getSelectedItem();

        if (tierName != null) {
            tierMap.put(tierName, newSize);
        }
    }

    /**
     * Update the UI elements according to the current Locale and the current
     * edit mode.
     */
    private void updateForLocale() {
        setTitle(ElanLocale.getString("InterlinearizerOptionsDlg.FontSizes"));

        // button labels
        applyToAllButton.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.ApplyToAll"));
    }

    /**
     * Sets the fontsize for all tiers to the currently selected fontsize.
     */
    private void doApplyToAll() {
        Object fontSize = fontSizeComboBox.getSelectedItem();

        if (fontSize instanceof Integer && (tierMap != null)) {
            Iterator<String> tierIt = tierMap.keySet().iterator();

            while (tierIt.hasNext()) {
                tierMap.put(tierIt.next(), (Integer)fontSize);
            }
        }
    }

    /**
     * Implements ActionListener (button).
     *
     * @param event the event
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (event.getSource() == applyToAllButton) {
            doApplyToAll();
        }
    }

    /**
     * Implements ItemListener (combo boxes)
     *
     * @param e the item event
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        if ((e.getSource() == tierComboBox) &&
                (e.getStateChange() == ItemEvent.SELECTED)) {
            String tierName = (String) tierComboBox.getSelectedItem();

            if (tierName != null) {
                updateFontSizeComboBox(tierName);
            }
        } else if ((e.getSource() == fontSizeComboBox) &&
                (e.getStateChange() == ItemEvent.SELECTED)) {
            Integer fontSize = (Integer) fontSizeComboBox.getSelectedItem();

            if (fontSize != null) {
                changeFontSize(fontSize);
            }
        }
    }
}
