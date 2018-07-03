package mpi.search.content.query.viewer;

import java.awt.Font;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import mpi.search.SearchLocale;
import mpi.search.gui.XNumericalJTextFieldFilter;



/**
 * $Id: StructuralDistancePanel.java 8004 2007-01-30 15:36:46Z klasal $
 *
 * $Author$
 * 
 */
public class StructuralDistancePanel extends AbstractDistancePanel {
    private static int unitComboBoxWidth = 160;

    /** Holds value of property DOCUMENT ME! */
    protected final JComboBox unitComboBox;

    /** Holds value of property DOCUMENT ME! */
    private final JTextField fromTextField = new JTextField(new XNumericalJTextFieldFilter(
                XNumericalJTextFieldFilter.INTEGER_WITH_NEG_INFINITY), "0", 3);

    /** Holds value of property DOCUMENT ME! */
    private final JTextField toTextField = new JTextField(new XNumericalJTextFieldFilter(
                XNumericalJTextFieldFilter.INTEGER_WITH_POS_INFINITY), "0", 3);

    /**
     * Creates a new StructuralDistancePanel object.
     */
    public StructuralDistancePanel() {
        fromTextField.setHorizontalAlignment(JTextField.CENTER);
        toTextField.setHorizontalAlignment(JTextField.CENTER);

        unitComboBox = new JComboBox() {
                    @Override
					public Dimension getPreferredSize() {
                        return new Dimension(unitComboBoxWidth,
                            super.getPreferredSize().height);
                    }
                };

        JLabel label = new JLabel(SearchLocale.getString("Search.Constraint.Distance") +
                " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label);
        add(fromTextField);
        label = new JLabel(" " + SearchLocale.getString("Search.To") + " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label);
        add(toTextField);
        add(new JLabel(" "));
        add(unitComboBox);
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    @Override
	public void setLowerBoundary(long l) {
        fromTextField.setText(getString(l));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getLowerBoundary() {
        return fromTextField.getText().trim().equals("") ? Long.MIN_VALUE
                                                         : getLong(fromTextField.getText());
    }

    /**
     *
     *
     * @param s DOCUMENT ME!
     */
    @Override
	public void setUnit(String s) {
        unitComboBox.setSelectedItem(s);
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getUnit() {
        return (unitComboBox.getSelectedIndex() != -1)
        ? (String) unitComboBox.getSelectedItem() : "";
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public JComboBox getUnitComboBox() {
        return unitComboBox;
    }

    /**
     *
     *
     * @param renderer DOCUMENT ME!
     */
    public void setUnitComboBoxRenderer(ListCellRenderer renderer) {
        unitComboBox.setRenderer(renderer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    @Override
	public void setUpperBoundary(long l) {
        toTextField.setText(getString(l));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public long getUpperBoundary() {
        return toTextField.getText().trim().equals("") ? Long.MAX_VALUE
                                                       : getLong(toTextField.getText());
    }
}
