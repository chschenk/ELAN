package mpi.search.content.query.viewer;

import java.awt.*;
import javax.swing.JComboBox;

/**
 * Created on May 19, 2004
 * 
 * @author Alexander Klassmann
 * @version May 19, 2004
 */
public class TemporalDistancePanel extends AbstractDistancePanel {
    protected JComboBox timeRelationComboBox;

    /** Holds value of property DOCUMENT ME! */
    final protected TimeField fromTimeField = new TimeField(false);

    /** Holds value of property DOCUMENT ME! */
    final protected TimeField toTimeField = new TimeField(true);

    @Override
	public long getLowerBoundary() {
        return fromTimeField.getTime();
    }

    @Override
	public long getUpperBoundary() {
        return toTimeField.getTime();
    }

    public TemporalDistancePanel() {
        setLayout(new GridBagLayout());
    }

    @Override
	public String getUnit() {
        return timeRelationComboBox.getSelectedIndex() != -1 ? (String) timeRelationComboBox
                .getSelectedItem()
                : "";
    }

    @Override
	public void setLowerBoundary(long milliSeconds) {
        fromTimeField.setTime(milliSeconds);
    }

    @Override
	public void setUpperBoundary(long milliSeconds) {
        toTimeField.setTime(milliSeconds);
    }

    @Override
	public void setUnit(String unit) {
        timeRelationComboBox.setSelectedItem(unit);
    }
}
