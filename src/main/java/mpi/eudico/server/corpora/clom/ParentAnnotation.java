package mpi.eudico.server.corpora.clom;

import mpi.eudico.server.corpora.event.ParentAnnotationListener;

/**
 * A ParentAnnotation is supposed to notify it's listening child annotations
 * after some modification.
 */
public interface ParentAnnotation {
    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public void addParentAnnotationListener(ParentAnnotationListener l);

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public void removeParentAnnotationListener(ParentAnnotationListener l);

    /**
     * DOCUMENT ME!
     */
    public void notifyParentListeners();
}
