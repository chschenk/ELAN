package mpi.eudico.client.annotator.search.model;

import mpi.search.content.query.model.ContentQuery;

/** 
 * The guts of the FAST engine
 * @author Larwan Berke, DePaul
 * @version 1.0
 * @since June 2013
 */
class FASTSearchHandler extends EAFMultipleFileSearchHandler {
	final private ContentQuery q;

	// we just need to actually store the query for our threadpool implementation
    public FASTSearchHandler(ContentQuery query) {
    	super(query);
    	q = query;
    }
    
    public ContentQuery getQuery() {
    	return q;
    }
}
