package mpi.eudico.client.annotator;

import javax.swing.JLabel;

import mpi.eudico.util.TimeFormatter;


/**
 * A label to display a duration (or time) value, initially in hh:mm:ss.ms format
 * 
 * @version May 2017 Changed the font setting: used to be the small version of the
 * default font (Arial Unicode MS). Now only the size of the default font is used
 * (without changing the system/jre's default font for JLabels)
 */
@SuppressWarnings("serial")
public class DurationPanel extends JLabel {
    /**
     * Creates a new DurationPanel instance
     *
     * @param duration the initial duration value to display
     */
    public DurationPanel(long duration) {
        super(TimeFormatter.toString(duration)); //0 doesn't matter because player isn't set yet (see ViewerManager)
        setFont(Constants.deriveSmallFont(getFont()));
    }
}
