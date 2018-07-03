package mpi.eudico.server.corpora.event;

/**
 * An ACMEditListener reacts on changes of Transcriptions, Tiers and Tags
 *
 * @author Alexander Klassmann version 21-Jun-2001
 */
public interface ACMEditListener {
    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void ACMEdited(ACMEditEvent e);
}
