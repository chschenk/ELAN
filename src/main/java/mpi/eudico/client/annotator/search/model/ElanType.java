/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.search.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.BasicControlledVocabulary;
import mpi.eudico.util.CVEntry;
import mpi.search.SearchLocale;


/**
 * $Id: ElanType.java 43571 2015-03-23 15:28:01Z olasei $ 
 * 
 * This class describes Transcription-specific Types and Relations of
 * tiers and possible units of distance between them. It is meant for the
 * (one) Transcription open in ELAN. 
 * 
 * $Author$ $Version$
 */
public class ElanType extends EAFType {
    private Map<String, Locale> langHash = new HashMap<String, Locale>();

    /** Holds value of property DOCUMENT ME! */
    private final TranscriptionImpl transcription;

    /**
     * The Constructor builds a tree with the tiers of the transcription as
     * nodes The root node itself is an empty node.
     *
     * @param transcription DOCUMENT ME!
     */
    public ElanType(TranscriptionImpl transcription) {
        this.transcription = transcription;

        List<TierImpl> tierVector = transcription.getTiers();
        tierNames = new String[tierVector.size()];
        Locale loc;
        for (int i = 0; i < tierVector.size(); i++) {
            TierImpl tier = tierVector.get(i);
            tierNames[i] = tier.getName();

            loc = tier.getDefaultLocale();
            if (loc != null) {
                langHash.put(tierNames[i],
                    loc);
            }
        }
    }

    /**
     *
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public List<CVEntry> getClosedVoc(String tierName) {
        TierImpl tier = transcription.getTierWithId(tierName);
        String cvName = tier.getLinguisticType().getControlledVocabularyName();
        BasicControlledVocabulary cv = transcription.getControlledVocabulary(cvName);

        return (cv != null) ? Arrays.asList(cv.getEntries()) : null;
    }

    /**
     *
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public boolean isClosedVoc(String tierName) {
        TierImpl tier = transcription.getTierWithId(tierName);
        if(tier == null) {
			return false;
		}

        String cvName = tier.getLinguisticType().getControlledVocabularyName();
        return transcription.getControlledVocabulary(cvName) != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Locale getDefaultLocale(String tierName) {
        return langHash.get(tierName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName1 DOCUMENT ME!
     * @param tierName2 DOCUMENT ME!
     *
     * @return array of unit tier names
     */
    @Override
	public String[] getPossibleUnitsFor(String tierName1, String tierName2) {
        List<String> commonAncestors = new ArrayList<String>();
        String[] possibleUnits = new String[0];

        TierImpl tier1 = (transcription.getTierWithId(tierName1));
        TierImpl tier2 = (transcription.getTierWithId(tierName2));

        TierImpl loopTier = tier1;

        do {
            if (loopTier.equals(tier2) || tier2.hasAncestor(loopTier)) {
                commonAncestors.add(loopTier.getName() + " " +
                    SearchLocale.getString("Search.Annotation_PL"));
            }
        } while ((loopTier = loopTier.getParentTier()) != null);

        possibleUnits = commonAncestors.toArray(new String[0]);
        standardUnit = (possibleUnits.length > 0)
            ? (String) commonAncestors.get(0) : null;

        return possibleUnits;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tierName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String[] getRelatedTiers(String tierName) {
        String[] relatedTiers = new String[0];

        try {
            TierImpl tier = transcription.getTierWithId(tierName);
            TierImpl rootTier = tier.getRootTier();
            List<TierImpl> dependentTiers = rootTier.getDependentTiers();

            relatedTiers = new String[dependentTiers.size() + 1];
            relatedTiers[0] = rootTier.getName();

            for (int i = 0; i < dependentTiers.size(); i++) {
                relatedTiers[i + 1] = dependentTiers.get(i).getName();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return relatedTiers;
    }

    /**
     * Returns the transcription of this type object.
     * 
     * @return the transcription
     */
	public Transcription getTranscription() {
		return transcription;
	}
}
