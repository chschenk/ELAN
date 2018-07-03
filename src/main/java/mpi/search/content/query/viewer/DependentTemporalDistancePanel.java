package mpi.search.content.query.viewer;

import java.awt.Font;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mpi.search.SearchLocale;

import mpi.search.content.query.model.Constraint;


/**
 * $Id: DependentTemporalDistancePanel.java 8004 2007-01-30 15:36:46Z klasal $
 *
 * $Author$ $Version$
 */
public class DependentTemporalDistancePanel extends TemporalDistancePanel
    implements ItemListener {
    private final CardLayout distanceLayout = new CardLayout();
    private final JPanel distancePanel = new JPanel(distanceLayout);

    /**
     * Creates a new DependentTemporalDistancePanel object.
     */
    public DependentTemporalDistancePanel() {
        timeRelationComboBox = new JComboBox(Constraint.DEPENDENT_CONSTRAINT_TIME_RELATIONS);
        timeRelationComboBox.setRenderer(new LocalizeListCellRenderer());

        distancePanel.add(toTimeField, "interval width");
        distancePanel.add(new JLabel(""), "nothing");

        JLabel label = new JLabel(SearchLocale.getString("Search.And") + " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label);
        add(timeRelationComboBox);
        add(new JLabel(" "));
        add(distancePanel);
        timeRelationComboBox.addItemListener(this);
        timeRelationComboBox.setSelectedIndex(0);
        updateDistancePanel();
    }

    /**
     *
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            updateDistancePanel();
        }
    }

    /**
     * sets input field of seconds+milliseconds visible/invisible
     *
     */
    private void updateDistancePanel() {
        Object selectedItem = timeRelationComboBox.getSelectedItem();

        if (Constraint.WITHIN_OVERALL_DISTANCE.equals(selectedItem) ||
                Constraint.WITHIN_DISTANCE_TO_LEFT_BOUNDARY.equals(selectedItem) ||
                Constraint.WITHIN_DISTANCE_TO_RIGHT_BOUNDARY.equals(
                    selectedItem) ||
                Constraint.BEFORE_LEFT_DISTANCE.equals(selectedItem) ||
                Constraint.AFTER_RIGHT_DISTANCE.equals(selectedItem)) {
            distanceLayout.show(distancePanel, "interval width");
        } else {
            distanceLayout.show(distancePanel, "nothing");
        }
    }
}
