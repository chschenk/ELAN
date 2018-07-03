/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.search.result.model;

import java.util.EventObject;



/**
 * $Id: ResultEvent.java 10187 2007-09-18 10:59:37Z klasal $
 */
public class ResultEvent extends EventObject {
	/** event type 'added'. */
    public static final int MATCH_ADDED = 0;
    /** event type 'status changed' (search complete etc.).  */
    public static final int STATUS_CHANGED = 1;
    /** event type 'new page'. */
    public static final int PAGE_COUNT_INCREASED = 2;
    /** event type 'data read' (from InputStream). */
    public static final int DATA_READ = 3;
    /** result type of this event. */
    private final int type;

    /**
     * Creates a new ResultEvent object.
     *
     * @param result DOCUMENT ME!
     * @param type DOCUMENT ME!
     */
    public ResultEvent(Result result,final  int type) {
        super(result);
        this.type = type;
    }

    /**
     * returns the type of the event
     *
     * @return type
     */
    public int getType() {
        return type;
    }
}
