package mpi.search.query.model;

import java.util.ArrayList;
import java.util.List;



/**
 * $Id: QueryManager.java 8349 2007-03-09 10:12:33Z klasal $
 *
 * @author Alexander Klassmann
 * $Version$
 */
public class QueryManager {
    private final List<Query> queries = new ArrayList<Query>();
    private int currentQueryNr = 0;

    /**
     * get current query
     *
     * @return current query
     */
    public Query getCurrentQuery() {
        return getQuery(currentQueryNr);
    }

    /**
     * sets current query to query with specified number has no effect, if the
     * number isn't in the range
     *
     * @param i wanted query number;
     */
    public void setCurrentQueryNumber(int i) {
        if ((0 < i) && (i <= queries.size())) {
            currentQueryNr = i;
        }
    }

    /**
     * get number of current query
     *
     * @return current query number
     */
    public int getCurrentQueryNumber() {
        return currentQueryNr;
    }

    /**
     * get query with specified number
     *
     * @param nr query number (starts with 1!)
     *
     * @return query
     */
    public Query getQuery(int nr) {
        return (Query) (((0 < nr) && (nr <= queries.size()))
        ? queries.get(nr - 1) : null);
    }

    /**
     * add query
     *
     * @param query
     */
    public void addQuery(Query query) {
        queries.add(query);
        currentQueryNr = queries.size();
    }

    /**
     * checks if current query is last one
     *
     * @return true if current query is not last one
     */
    public boolean hasNextQuery() {
        return currentQueryNr < queries.size();
    }

    /**
     * checks if current query is first one
     *
     * @return true if current query is not first one
     */
    public boolean hasPreviousQuery() {
        return currentQueryNr > 1;
    }

    /**
     * checks if there is any query
     *
     * @return if there is at least one query
     */
    public boolean hasQuery() {
        return queries.size() > 0;
    }

    /**
     * count of queries
     *
     * @return number of queries
     */
    public int size() {
        return queries.size();
    }
}
