package mpi.eudico.server.corpora.util;


/**
 * An ACMEditableObject is supposed to be a Transcription, Tier, Tag,
 * Annotation When it is modified it will arrange handling of the
 * modification, either by delegating it to another ACMEditableObject, or by
 * notifying listeners.
 */
public interface ACMEditableObject {
    /**
     * DOCUMENT ME!
     *
     * @param operation DOCUMENT ME!
     * @param modification DOCUMENT ME!
     */
    public void modified(int operation, Object modification);

    /**
     * DOCUMENT ME!
     *
     * @param source DOCUMENT ME!
     * @param operation DOCUMENT ME!
     * @param modification DOCUMENT ME!
     */
    public void handleModification(ACMEditableObject source, int operation,
        Object modification);
}
