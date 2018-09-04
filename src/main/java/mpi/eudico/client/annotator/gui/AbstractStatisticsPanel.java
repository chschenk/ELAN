package mpi.eudico.client.annotator.gui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;


/**
 * An abstract panel class for a statistics table and related gui elements.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public abstract class AbstractStatisticsPanel extends JPanel {
    /** the transcription */
    protected TranscriptionImpl transcription;

    /** the total (media) duration */
    protected long totalDuration;

    //Statistics table GUI 
    /** the scrollpane for the table */
    protected JScrollPane statPane;

    /** the panel for the table */
    protected JPanel statPanel;

    /** the statistics table */
    protected JTable statTable;

    /** no selection */
    protected final String EMPTY = "-";

	/** formatter for average durations */
    protected DecimalFormat format = new DecimalFormat("#0.0#####",
	            new DecimalFormatSymbols(Locale.US));

	/** formatter for ss.ms values */
	protected DecimalFormat format2 = new DecimalFormat("#0.0##",
	            new DecimalFormatSymbols(Locale.US));

    /**
     * Creates a new AbstractStatisticsPanel instance
     *
     * @param transcription the transcription
     */
    public AbstractStatisticsPanel(TranscriptionImpl transcription) {
        super();
        this.transcription = transcription;
        //initComponents();
    }

    /**
     * Creates a new AbstractStatisticsPanel instance
     *
     * @param transcription the transcription
     * @param totalDuration the duration
     */
    public AbstractStatisticsPanel(TranscriptionImpl transcription,
        long totalDuration) {
        super();
        this.transcription = transcription;
        this.totalDuration = totalDuration;
    }

    /**
     * initialise ui elements and create the initial table
     */
    abstract void initComponents();

    /**
     * Returns the current table.
     *
     * @return the current statistics table
     */
    public abstract JTable getStatisticsTable();
}
