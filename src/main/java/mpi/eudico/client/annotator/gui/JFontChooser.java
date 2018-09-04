package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * A simple font chooser displaying two lists, one with the font family names
 * of the system and one with the font varieties per family. Only one font can
 * be selected. There are no options to specify size or weight.
 * Dec. 2008: option to choose a font size is added. There are now different 
 * modes in which to use the chooser.
 *  
 * @author Han Sloetjes
 * @version 2.0, Dec 2008
 */
public class JFontChooser extends JPanel implements ListSelectionListener,
    ActionListener, ChangeListener {
    /** selection of font family and font style */
    public static int FONT_STYLE = 1;

    /** selection of font family and font size*/
    public static int FONT_SIZE = 2;

    /** selection of font family, font style and font size*/
    public static int FONT_STYLE_AND_SIZE = 3;
    private JList familyList;
    private JList fontList;
    private JLabel selFontLabel;
    private JLabel fontSizeLabel;
    private JSpinner fontSizeSpinner;
    private JList fontSizeList;
    private JButton okButton;
    private JButton cancelButton;
    private GraphicsEnvironment ge;
    private String[] families;
    private Font[] allFonts;
    private Font selFont;
    private JDialog dialog;
    private int mode = FONT_STYLE;
    private int fontSize = 12;

    /**
     * Creates a new JFontChooser instance
     */
    public JFontChooser() {
        super();
        ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        families = ge.getAvailableFontFamilyNames();
        allFonts = ge.getAllFonts();
        initComponents();
    }

    /**
     * Constructor with a selection mode as argument, for a customized
     * font chooser.
     *
     * @param selectionMode determines what the user can select, one of
     *        FONT_STYLE, FONT_SIZE or FONT_STYLE_AND_SIZE.
     */
    public JFontChooser(int selectionMode) {
        super();
        ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        families = ge.getAvailableFontFamilyNames();
        allFonts = ge.getAllFonts();

        if ((selectionMode >= FONT_STYLE) &&
                (selectionMode <= FONT_STYLE_AND_SIZE)) {
            mode = selectionMode;
        }

        initComponents();
    }

    /**
     * Initializes the ui components.
     */
    protected void initComponents() {
        familyList = new JList(families);
        familyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontList = new JList(new DefaultListModel());
        fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selFontLabel = new JLabel("-");
        fontSizeLabel = new JLabel(ElanLocale.getString("Menu.View.FontSize"));

        SpinnerNumberModel model = new SpinnerNumberModel(12, 4, 60, 2);
        fontSizeSpinner = new JSpinner(model);
        fontSizeList = new JList(new Object[] {
	        		Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10),
	        		Integer.valueOf(11), Integer.valueOf(12), Integer.valueOf(14),
	        		Integer.valueOf(16), Integer.valueOf(18), Integer.valueOf(24),
	        		Integer.valueOf(28), Integer.valueOf(36), Integer.valueOf(48),
	        		Integer.valueOf(60)
                });
        fontSizeList.setSelectedValue(Integer.valueOf(fontSize), false);
        fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        okButton = new JButton(ElanLocale.getString("Button.Apply"));
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));

        setLayout(new GridBagLayout());

        JScrollPane familySP = new JScrollPane(familyList);
        JScrollPane fontSP = new JScrollPane(fontList);
        JPanel sizePanel = new JPanel(new GridBagLayout());
        Dimension dim = new Dimension(200, 160);
        familySP.setPreferredSize(dim);
        fontSP.setPreferredSize(dim);

        if ((mode == FONT_SIZE) || (mode == FONT_STYLE_AND_SIZE)) {
            sizePanel.setPreferredSize(dim);

            GridBagConstraints fgbc = new GridBagConstraints();
            fgbc.anchor = GridBagConstraints.NORTHWEST;
            fgbc.fill = GridBagConstraints.HORIZONTAL;
            fgbc.weightx = 1.0;
            fgbc.insets = new Insets(0, 0, 2, 0);
            sizePanel.add(fontSizeLabel, fgbc);

            fgbc.gridy = 1;
            fgbc.insets = new Insets(2, 0, 2, 0);
            sizePanel.add(fontSizeSpinner, fgbc);

            fgbc.gridy = 2;
            fgbc.fill = GridBagConstraints.BOTH;
            fgbc.weighty = 1.0;
            fgbc.insets = new Insets(2, 0, 0, 0);
            sizePanel.add(new JScrollPane(fontSizeList), fgbc);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 10, 2, 6);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        add(familySP, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(10, 6, 2, 10);
        add(fontSP, gbc);
        gbc.gridx = 2;
        add(sizePanel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 10, 2, 10);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 0;
        add(selFontLabel, gbc);
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.insets = new Insets(10, 10, 2, 10);
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(buttonPanel, gbc);

        familyList.addListSelectionListener(this);
        fontList.addListSelectionListener(this);
        fontSizeSpinner.addChangeListener(this);
        fontSizeList.addListSelectionListener(this);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);

        if (mode == FONT_STYLE) {
            sizePanel.setVisible(false);
        }

        if (mode == FONT_SIZE) {
            fontSP.setVisible(false);
        }
    }

    private void updateFontLabel() {
        String ff = "";

        if (familyList.getSelectedValue() != null) {
            ff = (String) familyList.getSelectedValue();  
        }

        if (fontList.getSelectedValue() != null && 
        		(mode == FONT_STYLE || mode == FONT_STYLE_AND_SIZE)) {
            ff += (" - " + (String) fontList.getSelectedValue());
            selFontLabel.setFont((new Font((String) fontList.getSelectedValue(),0,fontSize)));
        }

        if ((mode == FONT_SIZE) || (mode == FONT_STYLE_AND_SIZE)) {
            ff += (" (" + fontSize + ")");            
        }

        selFontLabel.setText(ff);        
    }

    private void updateFontList() {
        if (familyList.getSelectedValue() != null) {
            String ff = (String) familyList.getSelectedValue();

            ((DefaultListModel) fontList.getModel()).clear();

            for (int i = 0; i < allFonts.length; i++) {
                if (allFonts[i].getFamily().equals(ff)) {
                    ((DefaultListModel) fontList.getModel()).addElement(allFonts[i].getName());
                }
            }

            if (((DefaultListModel) fontList.getModel()).getSize() > 0) {
                fontList.setSelectedIndex(0);
            }

            updateFont();
            updateFontLabel();
        }
    }

    private void updateFont() {
        String name = (String) fontList.getSelectedValue();

        if ((name == null) || (mode == FONT_SIZE)) {
            if (familyList.getSelectedValue() != null) {
                String ff = (String) familyList.getSelectedValue();

                for (int i = 0; i < allFonts.length; i++) {
                    if (allFonts[i].getFamily().equals(ff)) {
                        selFont = allFonts[i];

                        break;
                    }
                }
            } else {
                selFont = null;
            }
        } else {        	
            for (int i = 0; i < allFonts.length; i++) {
                if (allFonts[i].getName().equals(name)) {
                    selFont = allFonts[i];

                    break;
                }
            }
        }

        if ((mode == FONT_SIZE) || (mode == FONT_STYLE_AND_SIZE)) {
            if (selFont != null) {
                selFont = selFont.deriveFont((float) fontSize);
            }
        }
    }

    private void doClose() {
        selFont = null;

        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
        }
    }

    private void doApply() {
        updateFont();

        if ((dialog != null) && dialog.isVisible()) {
            dialog.setVisible(false);
            dialog.dispose();
        }
    }

    /**
     * Creates a dialog (that blocks) and returns the selected font or null if
     * the dialog has been canceled.
     *
     * @param parent the parent dialog
     * @param modal the modal flag
     * @param curFont the current selected font, used to update the lists
     *
     * @return the selected font
     */
    public Font showDialog(JDialog parent, boolean modal, Font curFont) {
        dialog = new JDialog(parent, modal);
        dialog.setTitle(ElanLocale.getString("FontDialog.Title"));
        if (curFont != null) {
        	familyList.setSelectedValue(curFont.getFamily(), true);
        	if (mode != FONT_SIZE) {
        		fontList.setSelectedValue(curFont.getName(), true);
        	}
        	if (mode != FONT_STYLE) {
        		fontSizeSpinner.setValue(Integer.valueOf(curFont.getSize()));
        		fontSizeList.setSelectedValue(Integer.valueOf(curFont.getSize()), true);
        	}
        }
        dialog.setContentPane(this);
        dialog.addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent we) {
                    doClose();
                }
            });
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return getValue();
    }

    /**
     * Creates a dialog (that blocks) and returns the selected font or null if
     * the dialog has been canceled.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param curFont the current selected font, used to update the lists
     *
     * @return the selected font
     */
    public Font showDialog(JFrame parent, boolean modal, Font curFont) {
        dialog = new JDialog(parent, modal);
        dialog.setTitle(ElanLocale.getString("FontDialog.Title"));
        if (curFont != null) {
        	familyList.setSelectedValue(curFont.getFamily(), true);
        	if (mode != FONT_SIZE) {
        		fontList.setSelectedValue(curFont.getName(), true);
        	}
        	if (mode != FONT_STYLE) {
        		fontSizeSpinner.setValue(Integer.valueOf(curFont.getSize()));
        		fontSizeList.setSelectedValue(Integer.valueOf(curFont.getSize()), true);
        	}
        }
        dialog.setContentPane(this);
        dialog.addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent we) {
                    doClose();
                }
            });
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return getValue();
    }

    /**
     * Returns the selected font, can be null;
     *
     * @return the elected font
     */
    public Font getValue() {
        return selFont;
    }

    /**
     * List selection event handling.
     *
     * @param e the event
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == familyList) {
            updateFontList();
            updateFontLabel();
        } else if (e.getSource() == fontList) {
            updateFont();
            updateFontLabel();
        } else if (e.getSource() == fontSizeList) {
            Integer selSize = (Integer) fontSizeList.getSelectedValue();

            if (selSize != null) {
                fontSize = selSize.intValue();
                fontSizeSpinner.setValue(selSize);
            }
        }
    }

    /**
     * The button actions.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            doApply();
        } else if (e.getSource() == cancelButton) {
            doClose();
        }
    }

    /**
     * Handles changes in the font size spinner.
     *
     * @param e the event
     */
    @Override
	public void stateChanged(ChangeEvent e) {
        if (e.getSource() == fontSizeSpinner) {
            fontSize = ((Integer) fontSizeSpinner.getValue());

            Integer value;

            for (int i = 0; i < fontSizeList.getModel().getSize(); i++) {
                value = (Integer) fontSizeList.getModel().getElementAt(i);

                if (value.intValue() == fontSize) {
                    fontSizeList.setSelectedValue(value, true);

                    break;
                }

                if (i == (fontSizeList.getModel().getSize() - 1)) {
                    fontSizeList.clearSelection();
                }
            }

            updateFont();
            updateFontLabel();
        }
    }
}
