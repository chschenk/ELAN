package mpi.search.model;

import mpi.search.query.model.Query;
import mpi.search.result.model.Result;


/**
 * $Id: SearchController.java 8348 2007-03-09 09:43:13Z klasal $
 *
 * @author Alexander Klassmann
 * @version July 2004
 */
public interface SearchController {
    static public int INIT = 0;
    static public int ACTIVE = 1;
    static public int FINISHED = 2;
    static public int INTERRUPTED = 3;
    
    /**
     * Execute a query tree on behalf of forQManager.
     * @param query
     */
    public void execute(Query query);
    
    /**
     * checks if search is running
     * @return true if in process
     */
    public boolean isExecuting();

    /**
     * Stop searching.
     */
    public void stopExecution();

	/**
	 * Returns result. Contains all matches found until stopped.
	 * @return Result
	 */
    public Result getResult();
    
    /**
     * set a GUI for showing progress of search
     * @param o
     */
    public void setProgressListener(ProgressListener o);
    
    /**
     * get current search duration
     * @return search duration in ms
     */
    public long getSearchDuration();

}
