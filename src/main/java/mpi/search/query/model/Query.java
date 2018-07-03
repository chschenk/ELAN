/*
 * Created on 19.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mpi.search.query.model;

import java.util.Date;

import mpi.search.result.model.Result;


/**
 * $Id: Query.java 8348 2007-03-09 09:43:13Z klasal $  
 * $Author$ $Version$
 */
public abstract class Query {
    private final Date creationDate;

    /**
     * Creates a new Query object.
     */
    public Query() {
        creationDate = new Date();
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public abstract Result getResult();

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public Date getCreationDate() {
        return creationDate;
    }
}
