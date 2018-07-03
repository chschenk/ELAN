package mpi.search.content.query.viewer;

import java.awt.FlowLayout;
import javax.swing.JPanel;

import mpi.search.SearchLocale;

/**
 * Created on May 26, 2004
 * 
 * @author Alexander Klassmann
 * @version May 26, 2004
 */
public abstract class AbstractDistancePanel extends JPanel {
	
	AbstractDistancePanel(){
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	}

    abstract public String getUnit();

    abstract public long getLowerBoundary();

    abstract public long getUpperBoundary();

    abstract public void setUnit(String s);

    abstract public void setLowerBoundary(long lowerBoundary);

    abstract public void setUpperBoundary(long upperBoundary);

    protected long getLong(String s) {
        long l = 0;
        if (s.toUpperCase().equals("-X")) {
            l = Long.MIN_VALUE;
        }
        else if (s.toUpperCase().equals("X") || s.toUpperCase().equals("X")) {
            l = Long.MAX_VALUE;
        }
        try {
            l = Long.parseLong(s);
        } catch (NumberFormatException e) {
            System.out.println(SearchLocale
                    .getString("Search.Exception.WrongNumberFormat")
                    + ": " + e.getMessage());
        }
        return l;
    }

    protected String getString(long l) {
        return (l != Long.MIN_VALUE && l != Long.MAX_VALUE) ? "" + l : "";
    }

}
