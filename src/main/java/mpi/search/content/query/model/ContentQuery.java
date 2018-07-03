package mpi.search.content.query.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import mpi.search.content.model.CorpusType;
import mpi.search.content.result.model.ContentResult;
import mpi.search.query.model.Query;
import mpi.search.result.model.Result;


/**
 * $Id: ContentQuery.java 8348 2007-03-09 09:43:13Z klasal $
 *
 * @author $Author$
 * @version $Revision$
 */
public class ContentQuery extends Query {
    private final ContentResult result = new ContentResult();
    private AnchorConstraint anchorConstraint;
    private final CorpusType type;
    private final File[] files;

    /**
     * Creates a new ContentQuery object.
     *
     * @param rootConstraint DOCUMENT ME!
     * @param type DOCUMENT ME!
     */
    public ContentQuery(AnchorConstraint rootConstraint, CorpusType type) {
        this(rootConstraint, type, null);
    }

    /**
     * Creates a new Query instance
     *
     * @param rootConstraint DOCUMENT ME!
     * @param type DOCUMENT ME!
     * @param files DOCUMENT ME!
     */
    public ContentQuery(AnchorConstraint rootConstraint, CorpusType type,
        File[] files) {
        this.anchorConstraint = rootConstraint;
        this.type = type;
        this.files = files;
    }

    /**
     *
     *
     * @param rootConstraint DOCUMENT ME!
     */
    public final void setAnchorConstraint(AnchorConstraint rootConstraint) {
        this.anchorConstraint = rootConstraint;
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public final AnchorConstraint getAnchorConstraint() {
        return anchorConstraint;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final List<Constraint> getConstraints() {
        List<Constraint> constraintList = new ArrayList<Constraint>();
        addChildren(constraintList, anchorConstraint);

        return constraintList;
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public File[] getFiles() {
        return files;
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public final boolean isRestricted() {
        return anchorConstraint instanceof RestrictedAnchorConstraint;
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Result getResult() {
        return result;
    }

    /**
     *
     *
     * @return DOCUMENT ME!
     */
    public final CorpusType getType() {
        return type;
    }

    /**
     * Returns false, if there is one constraint with an empty search
     * expression
     *
     * @return boolean
     */
    public final boolean isWellSpecified() {
        boolean wellSpecified = true;

        List<Constraint> constraintList = getConstraints();

        for (int i = 0; i < constraintList.size(); i++) {
            if (constraintList.get(i) != null) {
                if (constraintList.get(i).isRegEx() &&
                        constraintList.get(i).getPattern()
                             .equals("")) {
                    wellSpecified = false;
                }
            }
        }

        return wellSpecified;
    }

    /**
     *
     *
     * @param object DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean equals(Object object) {
        if (!(object instanceof ContentQuery)) {
            return false;
        }

        return getConstraints().equals(((ContentQuery) object).getConstraints());
    }

    /**
     * Translates query to human readable text. (Debugging)
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        List<Constraint> constraintList = getConstraints();

        for (int i = 0; i < constraintList.size(); i++) {
            sb.append(constraintList.get(i).toString());
        }

        return sb.toString();
    }

    private final void addChildren(List<Constraint> list, Constraint node) {
        list.add(node);

        for (Enumeration<Constraint> e = node.children(); e.hasMoreElements();) {
            addChildren(list, e.nextElement());
        }
    }
}
