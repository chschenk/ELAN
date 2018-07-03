package mpi.eudico.server.corpora.event;

import java.util.EventObject;


/**
 * A ParentAnnotationListener reacts on changes of ParentAnnotations
 *
 * @author Hennie Brugman version 1-Jul-2002
 */
public interface ParentAnnotationListener {
    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void parentAnnotationChanged(EventObject e);
}
