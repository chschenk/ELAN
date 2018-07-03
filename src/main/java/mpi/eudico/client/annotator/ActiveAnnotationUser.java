package mpi.eudico.client.annotator;

import mpi.eudico.server.corpora.clom.Annotation;


/**
 * The interface that defines methods from an ActiveAnnotation user
 */
public interface ActiveAnnotationUser extends ActiveAnnotationListener {
    /**
     * DOCUMENT ME!
     *
     * @param activeAnnotation DOCUMENT ME!
     */
    public void setActiveAnnotationObject(ActiveAnnotation activeAnnotation);

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateActiveAnnotation();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Annotation getActiveAnnotation();

    /**
     * DOCUMENT ME!
     *
     * @param annotation DOCUMENT ME!
     */
    public void setActiveAnnotation(Annotation annotation);
}
