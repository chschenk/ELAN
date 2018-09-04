package mpi.eudico.client.util;

import java.util.List;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.util.ControlledVocabulary;

/**
 * Utility class to help find a tier with a Controlled Vocabulary that can be used as a marker
 * vocabulary containing colors for the annotations on the calling parent tier. 
 *  
 * @author Han Sloetjes
 *
 */
public class TierAssociation {
	/**
	 * Tries to find a "marker" tier with a controlled vocabulary among the symbolically associated
	 * depending tiers. If there is only one child tier of type SYMBOLIC_ASSOCIATION
	 * and it has a CV, that tier is returned. If there are more depending tiers 
	 * of that type but there is only one tier with a CV that one is returned. If there 
	 * are more than one depending tiers of type SYMBOLIC_ASSOCIATION WITH a CV 
	 * <code>null</code> is returned.
	 * 
	 * @param trans the transcription containing the tiers and controlled vocabularies
	 * @param tier the tier to find a marker tier with CV for among the symbolically associated 
	 * child tiers.
	 * @return a tier with a controlled vocabulary or null
	 */
	public static TierImpl findMarkerTierFor(Transcription trans, TierImpl tier) {
		if (trans == null || tier == null) {
			return null;
		}
		
		List<TierImpl> chTiers = tier.getChildTiers();		
		TierImpl t;
		TierImpl targetTier = null;
		ControlledVocabulary cv;
		
		for (int i = 0; i < chTiers.size(); i++) {
			t = (TierImpl) chTiers.get(i);
			// Constraints must in fact always be non null, if it is null there is an inconsistency in the data
			if (t.getLinguisticType().getConstraints() != null && t.getLinguisticType().getConstraints().getStereoType() == 
				Constraint.SYMBOLIC_ASSOCIATION) {
				cv = ((TranscriptionImpl) trans).getControlledVocabulary(
						t.getLinguisticType().getControlledVocabularyName());
				if (cv != null) {
					if (targetTier == null) {
						targetTier = t;
					} else {
						// more than one potential marker vocabulary, return null
						return null;
					}
				}
				
			}
		}
		
		return targetTier;
	}

}
