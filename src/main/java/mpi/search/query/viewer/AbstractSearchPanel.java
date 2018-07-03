package mpi.search.query.viewer;

import javax.swing.JPanel;

import mpi.search.model.SearchController;
import mpi.search.model.SearchListener;
import mpi.search.query.model.Query;
import mpi.search.result.viewer.MatchCounter;
import mpi.search.result.viewer.ResultViewer;
import mpi.search.viewer.ProgressViewer;


/**
 * Created on Apr 14, 2005 $Id: AbstractSearchPanel.java,v 1.5 2005/11/01
 * 16:07:16 klasal Exp $ $Author$ $Version$
 */
public abstract class AbstractSearchPanel extends JPanel
    implements SearchListener {
    /** progress viewer */
    protected final ProgressViewer progressViewer;

    /** show current number of result and amount of matches */
    protected MatchCounter matchCounter;

    /** Holds value of property DOCUMENT ME! */
    protected ResultViewer resultViewer;

    /** search engine */
    protected SearchController searchEngine;

    /**
     * Creates a new AbstractSearchPanel object.
     */
    public AbstractSearchPanel() {
        progressViewer = new ProgressViewer();
        matchCounter = new MatchCounter();
    }

    /**
     *
     */
    @Override
	public void executionStarted() {
        matchCounter.setVisible(true);
        progressViewer.setVisible(true);
    }

    /**
     *
     */
    public void stopSearch() {
        if (searchEngine != null) {
            searchEngine.stopExecution();
        }
    }

    protected abstract Query getQuery();

    protected void startSearch() {
        if (searchEngine.getResult() != null) {
            searchEngine.getResult().removeListeners();
        }

        Query query = getQuery();

        if (query != null) {
            query.getResult().reset();

            query.getResult().addResultChangeListener(matchCounter);
            query.getResult().addResultChangeListener(resultViewer);

            searchEngine.execute(query);
        }
    }
}
