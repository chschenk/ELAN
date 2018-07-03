package mpi.eudico.client.annotator.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.util.TimeInterval;

/**
 * Utility class to convert annotation values to tiers with the names of the values, 
 * each new tier containing only the annotations with that value of the original tier.<br>
 * E.g. given a tier T containing annotations with values A, B and C, will be converted to
 * the tiers A, B and C. Tier A will only contain annotations that had value A on tier T.
 * 
 * @author Han Sloetjes
 */
public class AnnotationsToTiersUtil {
	/**
	 * Constructor.
	 */
	public AnnotationsToTiersUtil() {
		super();
	}

	/**
	 * Converts annotation values to a map of tier names to segments mappings.
	 *  
	 * @param tier input tier
	 * @param splitDelimiter one or more characters to use for splitting values in multiple values
	 * 
	 * @return a map of tier name to segments mappings
	 */
	public Map<String, List<TimeInterval>> convertToTiers(TierImpl tier, String splitDelimiter) {
		if (tier == null) {
			return null;
		}
		if (tier.getNumberOfAnnotations() == 0) {
			return null;
		}
		
		Map<String, List<TimeInterval>> tierMap = new HashMap<String, List<TimeInterval>>();// default map size
		List<TimeInterval> curList;
		List<AbstractAnnotation> annotations = tier.getAnnotations();
		
		for (int i = 0; i < annotations.size(); i++) {
			Annotation ann = annotations.get(i);
			if (ann.getValue() != null && ann.getValue().length() > 0) {
				if (splitDelimiter == null) {
					curList = tierMap.get(ann.getValue());
					if (curList == null) {
						curList = new ArrayList<TimeInterval>();
						tierMap.put(ann.getValue(), curList);
					}
					curList.add(new TimeInterval(ann.getBeginTimeBoundary(), ann.getEndTimeBoundary()));
				} else {// split the value
					String[] vals = ann.getValue().split(splitDelimiter);
					for (String s : vals) {
						curList = tierMap.get(s);
						if (curList == null) {
							curList = new ArrayList<TimeInterval>();
							tierMap.put(s, curList);
						}
						curList.add(new TimeInterval(ann.getBeginTimeBoundary(), ann.getEndTimeBoundary()));
					}
				}
			}
		}
		
		return tierMap;
	}
}
