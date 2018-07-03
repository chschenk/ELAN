package mpi.search.model;

import mpi.search.query.model.Query;

public interface SearchEngine {

	public void performSearch(Query query) throws Exception;
	
}
