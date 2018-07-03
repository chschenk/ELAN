package mpi.search.result.model;

/**
 * $Id: ResultHandler.java 8348 2007-03-09 09:43:13Z klasal $
 */
public interface ResultHandler {
	/**
	 * Do something with a match
	 *
	 * @param match Match that should be handled
	 * @param parameter an optional parameter string that specifies what should be
	 *        done with the match.
	 */
	public void handleMatch(Match match, String parameter);

}
