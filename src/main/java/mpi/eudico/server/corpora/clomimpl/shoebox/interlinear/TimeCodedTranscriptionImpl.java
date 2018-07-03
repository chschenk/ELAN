/*
 * Created on Dec 17, 2004
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.util.TimeFormatter;


/**
 * This implementation of TimeCodedTranscription will delegate method calls to
 * a wrapped TranscriptionImpl, but overrides methods when necessary, to add
 * time code tiers without modifying the wrapped document.
 *
 * @author hennie
 * @version Aug 2005 Identity removed
 */
public class TimeCodedTranscriptionImpl implements TimeCodedTranscription {
    private TranscriptionImpl wrappedTranscription;
    private List<Tier> timeCodeTiers = null;
    private int tcTierCounter = 0;
    private LinguisticType tcLingType;
    private Map<TierImpl, TierImpl> rootTiers;
    private Map<Annotation, RefAnnotation> tcChildAnnots;

    /**
     * Creates a new TimeCodedTranscriptionImpl instance
     *
     * @param trImpl DOCUMENT ME!
     */
    public TimeCodedTranscriptionImpl(TranscriptionImpl trImpl) {
        wrappedTranscription = trImpl;

        timeCodeTiers = new ArrayList<Tier>();
        rootTiers = new HashMap<TierImpl, TierImpl>();
        tcChildAnnots = new HashMap<Annotation, RefAnnotation>();

        tcLingType = new LinguisticType(TC_LING_TYPE);
        tcLingType.setTimeAlignable(false);
        tcLingType.addConstraint(new SymbolicAssociation());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Transcription getTranscription() {
        return wrappedTranscription;
    }

    /**
     * Returns the list of Tiers that are accessible.
     *
     * @return the list of Tiers
     */
    @Override
	public List<Tier> getTiers() {
        List<TierImpl> tiers = wrappedTranscription.getTiers();
        List<Tier> allTiers = new ArrayList<Tier>(tiers);

        if (timeCodeTiers != null) {
            allTiers.addAll(timeCodeTiers);
        }

        return allTiers;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theAnnot DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<? extends Annotation> getChildAnnotationsOf(Annotation theAnnot) {
        List<Annotation> childAnnots = new ArrayList<Annotation>(wrappedTranscription.getChildAnnotationsOf(
                    theAnnot));

        Annotation child = tcChildAnnots.get(theAnnot);

        if (child != null) {
            childAnnots.add(child);
        }

        return childAnnots;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public TierImpl getParentTier(Tier theTier) {
        TierImpl parentTier = null;

        if (theTier != null) {
            if (timeCodeTiers.contains(theTier)) {
                parentTier = rootTiers.get(theTier);
            } else {
                parentTier = ((TierImpl) theTier).getParentTier();
            }
        }

        return parentTier;
    }

    /**
     * DOCUMENT ME!
     *
     * @param forTier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Tier getRootTier(Tier forTier) {
        Tier rootTier = null;

        if (forTier != null) {
            if (timeCodeTiers.contains(forTier)) {
                rootTier = rootTiers.get(forTier);
            } else {
                rootTier = ((TierImpl) forTier).getRootTier();
            }
        }

        return rootTier;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tier1 DOCUMENT ME!
     * @param tier2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isAncestorOf(Tier tier1, Tier tier2) {
        boolean ancestor = false;
        TierImpl parentTier = getParentTier(tier2);

        if (parentTier != null) { // has ancestor

            if (parentTier == tier1) {
                ancestor = true;
            } else {
                ancestor = isAncestorOf(tier1, parentTier);
            }
        }

        return ancestor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<TierImpl> getTierTree(TierImpl tier) {
        List<TierImpl> tierTree = new ArrayList<TierImpl>();

        List<TierImpl> children = tier.getChildTiers();

        tierTree.add(tier);

        for (int j = 0; j < children.size(); j++) {
            TierImpl child = children.get(j);
            tierTree.addAll(getTierTree(child));
        }

        // add potential tc tier
        for (int i = 0; i < timeCodeTiers.size(); i++) {
            TierImpl tcTier = (TierImpl) timeCodeTiers.get(i);

            if (rootTiers.get(tcTier) == tier) {
                tierTree.add(tcTier);
            }
        }

        return tierTree;
    }

    /**
     * DOCUMENT ME!
     *
     * @param timeCodeStyle DOCUMENT ME!
     */
    @Override
	public void prepareTimeCodeRendering(int timeCodeStyle, boolean correctAnnotationTimes) {
        cleanupTimeCodeTiers();
        addTimeCodeTiers(timeCodeStyle, correctAnnotationTimes);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void cleanupTimeCodeTiers() {
        timeCodeTiers.clear();
        rootTiers.clear();
        tcChildAnnots.clear();
        tcTierCounter = 0; // reset
    }

    /**
     * Time code is shown by adding a symbolic association tier to each root
     * tier, and a RefAnnotation with a time code as value for each root
     * annotation.
     *
     * @param timeCodeStyle DOCUMENT ME!
     * @param correctAnnotationTimes if true add the master media offset to the annotations' begin and end values
     */
    private void addTimeCodeTiers(int timeCodeStyle, boolean correctAnnotationTimes) {
        long offset = 0L;
        
        if (correctAnnotationTimes) {
            List<MediaDescriptor> mds = wrappedTranscription.getMediaDescriptors();
            if (mds != null && mds.size() > 0) {
                offset = mds.get(0).timeOrigin;
            }
        }
        
        List<TierImpl> topTiers = wrappedTranscription.getTopTiers();

        for (int i = 0; i < topTiers.size(); i++) {
            TierImpl topT = topTiers.get(i);
            timeCodeTiers.add(addTCTierFor(topT, timeCodeStyle, offset));
        }
    }

    private Tier addTCTierFor(TierImpl tier, int timeCodeStyle, long mediaOffset) {
        TierImpl newTier = null;
        String newTierName = TC_TIER_PREFIX + tcTierCounter++;

        // set parent tier and transcription to null, to prevent modification
        // of the wrappedTranscription.
        newTier = new TierImpl(null, newTierName, null, null, null);

        newTier.setLinguisticType(tcLingType);

        // add tc annotations for each annot on tier

        for (Annotation parentAnn : tier.getAnnotations()) {

            // manually set referred annotation, to prevent registration of
            // parent annotation listeners.
            // getChildAnnotationsOf should now return tc children
            RefAnnotation newAnnot = new RefAnnotation(null, newTier);
            newAnnot.getReferences().add(parentAnn);

            newTier.addAnnotation(newAnnot);

            long bl = -1;
            long el = -1;

            if (parentAnn instanceof AlignableAnnotation &&
                    ((AlignableAnnotation) parentAnn).getBegin().isTimeAligned()) {
                bl = parentAnn.getBeginTimeBoundary() + mediaOffset;
            }

            if (parentAnn instanceof AlignableAnnotation &&
                    ((AlignableAnnotation) parentAnn).getEnd().isTimeAligned()) {
                el = parentAnn.getEndTimeBoundary() + mediaOffset;
            }

            String value = "";

            if (timeCodeStyle == Interlinearizer.HHMMSSMS) {
                String beginStr = Interlinearizer.UNALIGNED_HHMMSSMS;

                if (bl != -1) {
                    beginStr = TimeFormatter.toString(bl);
                }

                String endStr = Interlinearizer.UNALIGNED_HHMMSSMS;

                if (el != -1) {
                    endStr = TimeFormatter.toString(el);
                }

                value = beginStr + " - " + endStr;
            } else {
                double bd = bl / 1000.0;
                double ed = el / 1000.0;

                //DecimalFormat ssmmm = new DecimalFormat("#0.000");
                // HS: 27 apr 05 formatting ('.' or ',')is locale dependent, make sure '.' is used
                DecimalFormat ssmmm = new DecimalFormat("#0.000",
                        new DecimalFormatSymbols(Locale.US));
                String bs = Interlinearizer.UNALIGNED_SSMS;

                if (bl != -1) {
                    bs = ssmmm.format(bd);
                }

                String es = Interlinearizer.UNALIGNED_SSMS;

                if (el != -1) {
                    es = ssmmm.format(ed);
                }

                value = bs + " - " + es;
            }

            newAnnot.setValue(value);

            // store parent and child
            tcChildAnnots.put(parentAnn, newAnnot);
        }

        rootTiers.put(newTier, tier);

        return newTier;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<Tier> getTimeCodeTiers() {
        return timeCodeTiers;
    }
}
