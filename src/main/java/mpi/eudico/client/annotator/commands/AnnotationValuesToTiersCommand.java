package mpi.eudico.client.annotator.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.util.AnnotationsToTiersUtil;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.TimeInterval;

/**
 * A command that takes the annotation values of a tier and creates a tier for each value
 * and adds (empty) annotations to those tiers according to the annotation values on
 * the input tier.
 * 
 * @author Han Sloetjes	
 */
public class AnnotationValuesToTiersCommand implements UndoableCommand {
	private String name;
	private TranscriptionImpl transcription;
	private String sourceTierName;
	private Map<String, List<TimeInterval>> segmentsPerTier;
	// used to store which tiers have been created, for undo/redo, and to map the 
	// new tier name to the key in the map of segments (they can be different)
	private Map<String, String> tierNameMap; 
	private int maxNumTiers;
	private TierImpl sourceTier;
	private final String lingTypeName = "split-type";
	private LinguisticType splitType;
	
	public AnnotationValuesToTiersCommand(String name) {
		this.name = name;
	}

	/**
	 * @param receiver the transcription
	 * @param arguments the arguments for this command<br>
	 * arguments[0] = the name of the input tier (String)<br>
	 * arguments[1] = (optional) the maximum number of tiers to create (Integer)<br>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		transcription = (TranscriptionImpl) receiver;
		sourceTierName = (String) arguments[0];
		sourceTier = (TierImpl) transcription.getTierWithId(sourceTierName);
		if (sourceTier == null) {
			ClientLogger.LOG.warning("There is no tier with that name: " + sourceTierName);
			return;
		}
		maxNumTiers = -1;
		if (arguments.length > 1) {
			maxNumTiers = (Integer) arguments[1];
		}
		// call utility method to extract tier name to segments mappings
		AnnotationsToTiersUtil aToTUtil = new AnnotationsToTiersUtil();
		segmentsPerTier = aToTUtil.convertToTiers(sourceTier, "\\+");
		
		if (segmentsPerTier == null) {
			ClientLogger.LOG.warning("No new tiers have been extracted from the input tier: " + sourceTierName);
			return;
		}
		tierNameMap = new HashMap<String, String>();
		
		createTiers();
	}

	/**
	 * Creates the tiers based on the extracted segments.
	 */
	private void createTiers() {
		if(segmentsPerTier == null) {
			ClientLogger.LOG.warning("No tiers to create.");
		}
		
		int count = 0;
		String tierName;
		List<TimeInterval> curSegments;
		LinguisticType nextType = null;
		TierImpl nextTier = null;
		Iterator<String> tierIter = segmentsPerTier.keySet().iterator();
		
		while (tierIter.hasNext()) {
			if (count == 0) {
				nextType = createType();
				if (nextType == null) {
					ClientLogger.LOG.warning("Unable to create a linguistic type, cannot create new tiers.");
					break;
				}
			}
			
			if (maxNumTiers > 0 && count >= maxNumTiers) {
				ClientLogger.LOG.warning("The maximum number of tiers has been reached, stopping here.");
				break;
			}
			
			tierName = tierIter.next();		
			curSegments = segmentsPerTier.get(tierName);
			nextTier = createTier(tierName, nextType);
			
			if (nextTier == null) {
				ClientLogger.LOG.warning("Could not create a tier for: " + tierName);
				continue;
			}
			tierNameMap.put(nextTier.getName(), tierName);
			
			// when tiers are created the notifying flag should be on currently, switch it off for annotations
			transcription.setNotifying(false);
			// now create annotations
			AbstractAnnotation aa;
			for (TimeInterval ti : curSegments) {
				aa = (AbstractAnnotation) nextTier.createAnnotation(ti.getBeginTime(), ti.getEndTime());
				// if need be the original annotation value can be copied into the new annotations?
			}
			transcription.setNotifying(true);
			// when tiers are created the notifying flag should be on, currently
			
			count++;
		}
	}
	
	/**
	 * Creates a type to use for all new tiers.
	 * 
	 * @return a linguistic type
	 */
	private LinguisticType createType() {
		if (sourceTier != null) {
			LinguisticType sourceType = sourceTier.getLinguisticType();
			// if the source tier is a top level tier "re-use" the linguistic type
			if (sourceType.getConstraints() == null) {
				return sourceType;
			}
		}
		
		
		LinguisticType lt = transcription.getLinguisticTypeByName(lingTypeName);
		if (lt != null) {
			if (lt.getConstraints() == null) {
				return lt;
			}
		}
		// either not there or wrong supertype
		String id;
		for (int i = 0; i < 20; i++) {
			if (i == 0) {
				id = lingTypeName;
			} else {
				id = lingTypeName + "-" + i;
			}
			if (transcription.getLinguisticTypeByName(id) == null) {
				LinguisticType nextLt = new LinguisticType(id);
				nextLt.setTimeAlignable(true);
				transcription.addLinguisticType(nextLt);
				splitType = nextLt;
				return nextLt;
			}
		}
		
		return null;
	}
	
	/**
	 * Creates a tier to use for new annotations. Checks for unique names.
	 * 
	 * @param name the intended tier name
	 * @param type the linguistic type to use
	 * @return the created tier or null
	 */
	private TierImpl createTier(String name, LinguisticType type) {
		if (transcription.getTierWithId(name) == null) {
			TierImpl t = new TierImpl(name, "", transcription, type);
			t.setLangRef(sourceTier.getLangRef());
			transcription.addTier(t);
			return t;
		} else {
			String id;
			for (int i = 1; i < 20; i++) {
				id = name + "-" + i;
				if (transcription.getTierWithId(id) == null) {
					TierImpl t = new TierImpl(id, "", transcription, type);
					t.setLangRef(sourceTier.getLangRef());
					transcription.addTier(t);
					return t;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Removes the tiers that have been created.
	 */
	@Override
	public void undo() {
		if (transcription != null && tierNameMap != null) {
			Iterator<String> tierIter = tierNameMap.keySet().iterator();
			String tierName;
			TierImpl tier;
			
			while (tierIter.hasNext()) {
				tierName = tierIter.next();
				tier = (TierImpl) transcription.getTierWithId(tierName);
				if (tier != null) {
					transcription.removeTier(tier);
				} else {
					ClientLogger.LOG.warning("The  tier " + tierName + " does not exist and can therefore not be deleted.");
				}
			}
			
			if (splitType != null) {
				transcription.removeLinguisticType(splitType);
			}
		}

	}

	/**
	 * Recreates the tiers based on the earlier extracted segments.
	 */
	@Override
	public void redo() {
		if (transcription != null && tierNameMap != null && segmentsPerTier != null) {
			LinguisticType curTypeToUse;
			if (splitType == null) {// an existing type was used
				curTypeToUse = transcription.getLinguisticTypeByName(lingTypeName);
			} else {
				curTypeToUse = splitType;
				transcription.addLinguisticType(splitType);
			}

			Iterator<String> tierIter = tierNameMap.keySet().iterator();
			String tierName;
			TierImpl tier;
			
			while (tierIter.hasNext()) {
				tierName = tierIter.next();
				tier = new TierImpl(tierName, "", transcription, curTypeToUse);
				tier.setLangRef(sourceTier.getLangRef());
				transcription.addTier(tier);
				
				List<TimeInterval> segments = segmentsPerTier.get(tierNameMap.get(tierName));
				for (TimeInterval ti : segments) {
					tier.createAnnotation(ti.getBeginTime(), ti.getEndTime());
				}
			}
		}
	}
	
	/**
	 * Returns the name of the command
	 */
	@Override
	public String getName() {
		return name;
	}

}
