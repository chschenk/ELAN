package mpi.eudico.server.corpora.util;

import mpi.eudico.server.corpora.event.ACMEditListener;

/**
 * An ACMEditableDocument is supposed to be implemented by a Transcription. It
 * represents the Observable for edit modifications.
 */
public interface ACMEditableDocument {
    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public void addACMEditListener(ACMEditListener l);

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public void removeACMEditListener(ACMEditListener l);

    /**
     * DOCUMENT ME!
     *
     * @param source DOCUMENT ME!
     * @param operation DOCUMENT ME!
     * @param modification DOCUMENT ME!
     */
    public void notifyListeners(ACMEditableObject source, int operation,
        Object modification);
}
