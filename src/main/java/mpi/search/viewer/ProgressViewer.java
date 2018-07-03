package mpi.search.viewer;

import mpi.search.SearchLocale;

import mpi.search.model.ProgressListener;

import mpi.search.result.model.Result;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JProgressBar;
import javax.swing.border.LineBorder;


/**
 * Created on Sep 23, 2004
 *
 * @author Alexander Klassmann
 * @version December, 2004
 */
public class ProgressViewer extends JProgressBar implements ProgressListener {
	/** label for status interrupted */
    private static final String INTERRUPTED = "Search.Interrupted";
    /** label for status complete */
    private static final String COMPLETE = "Search.Complete";
    /** label for status modified */
    private static final String MODIFIED = "Search.Modified";

    public ProgressViewer() {
        setBorder(new LineBorder(Color.lightGray));
        setStringPainted(true);
        setProgress(0);
        setVisible(false);
    }

    @Override
	public void setProgress(int procent) {
        setVisible(true);
        setValue(procent);

        if (procent == 0) {
            setString(" "); // empty string results in height change
        } else {
            setString((procent < 100) ? (procent + "%")
                                      : SearchLocale.getString(COMPLETE));
        }
    }

    @Override
	public void setStatus(int status) {
        setVisible(true);

        switch (status) {
        case Result.COMPLETE:
            setValue(100);
            setString(SearchLocale.getString(COMPLETE));

            break;

        case Result.INTERRUPTED:
            setValue(0);
            setString(SearchLocale.getString(INTERRUPTED));

            break;

        case Result.MODIFIED:
            setValue(0);
            setString(SearchLocale.getString(MODIFIED));
        }
    }

    // don't let shrink
    @Override
	public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}
