package mpi.eudico.client.annotator.viewer;

import mpi.eudico.server.corpora.clom.Tier;


/**
 * Interface that gives the methods to be implemented by a viewer that can show
 * only one Tier.
 */
public interface SingleTierViewer {
    /**
     * DOCUMENT ME!
     *
     * @param tier DOCUMENT ME!
     */
    public void setTier(Tier tier);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Tier getTier();
}
