package mpi.search.model;

/**
 * Created on Jul 28, 2004
 * @author Alexander Klassmann
 * @version April, 2005
 */
public interface SearchListener{
	
	/**
	 * Handle exceptions from search thread
	 * @param e
	 */
	public void handleException(Exception e);
	
	/**
	 * notifies tool that search has started
	 *
	 */
	public void executionStarted();
	    
	/**
	 * notifies tool that search has stopped 
	 *
	 */
	public void executionStopped();
}
