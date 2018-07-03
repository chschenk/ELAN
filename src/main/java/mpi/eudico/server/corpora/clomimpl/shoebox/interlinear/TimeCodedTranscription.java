/*
 * Created on Dec 17, 2004
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * This interface is a sub-interface of Transcription. It contains only the
 * methods that are used in the interlinearizer package. Interlinearizer's
 * classes will use TimeCodedTranscription to retrieve their information about
 * the annotation document. The intended implementation will delegate method
 * calls to a wrapped Transcription, but allows to override methods when
 * necessary, for example to add time code tiers without modifying the wrapped
 * document.
 *
 * @author hennie
 * @version Aug 2005 Identity removed
 */
public interface TimeCodedTranscription {
    /** Holds value of property DOCUMENT ME! */
    public static final String TC_LING_TYPE = "12nov2004_temp$LING$type"; // unlikely type name

    /** Holds value of property DOCUMENT ME! */
    public static final String TC_TIER_PREFIX = "TC-";

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Transcription getTranscription();

    // for delegation and override
    public List<Tier> getTiers();

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<? extends Annotation> getChildAnnotationsOf(Annotation theAnnot);

    /**
     * DOCUMENT ME!
     *
     * @param theTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Tier getParentTier(Tier theTier);

    /**
     * DOCUMENT ME!
     *
     * @param forTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Tier getRootTier(Tier forTier);

    /**
     * DOCUMENT ME!
     *
     * @param tier1 DOCUMENT ME!
     * @param tier2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isAncestorOf(Tier tier1, Tier tier2);

    /**
     * DOCUMENT ME!
     *
     * @param tier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<TierImpl> getTierTree(TierImpl tier);

    // to manage time code tiers		
    public void prepareTimeCodeRendering(int timeCodeStyle, boolean correctAnnotationTimes);

    /**
     * DOCUMENT ME!
     */
    public void cleanupTimeCodeTiers();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<Tier> getTimeCodeTiers();
}
