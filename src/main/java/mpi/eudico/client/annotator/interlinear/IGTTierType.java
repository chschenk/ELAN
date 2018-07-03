package mpi.eudico.client.annotator.interlinear;

/**
 * @author han
 *
 */
public enum IGTTierType {
	/**
	 * The top level tier or annotation.
	 * Has only one annotation per block (per definition of block).
	 */
	ROOT(null),
	/**
	 * In most cases the same as root level,
	 * but not if there is a paragraph level or similar above the phrase level.
	 * <em>(unused and unhandled so far!)</em>
	 * <p>
	 * Let's hope that there is only one annotation on this tier per block,
	 * and it's not in WordLevelBlock. Otherwise WORD_LEVEL_ROOT is not a word level root.
	 */
	PHRASE_LEVEL_ROOT(null),
	/**
	 * Simple undefined association type. <em>Seems to be unused and unhandled.</em>
	 */
	ASSOCIATION(null),
	/**
	 * Tier or annotation that is direct, one-to-one dependent on the root.
	 * Therefore also has only one annotation.
	 */
	FIRST_LEVEL_ASSOCIATION(null),
	/**
	 * Represents the word level of tier or annotation,
	 * the parent of morph, gloss and pos levels.
	 * The topmost tier in a WordLevelBlock;
	 * multiple annotations are possible in a block from this tier down.
	 */
	WORD_LEVEL_ROOT(null),
	/**
	 * A subdivision tier or annotation on such tier, e.g. morph levels.
	 */
	SUBDIVISION(null), 
	/**
	 *  Any association level not direct dependent on the absolute root.
	 *  If it has only *ASSOCIATION parents, also would have only one annotation
	 *  and be !inWordLevelBlock.
	 */
	DEEPER_LEVEL_ASSOCIATION(null),
	/**
	 * A time code tier/placeholder.
	 */
	TIME_CODE("TC"), // localize?
	/**
	 * A silence duration placeholder.
	 */
	SILENCE_DURATION("SD"), // localize?
	/**
	 * A speaker or participant placeholder.
	 */
	SPEAKER_LABEL("Speaker"), // localize?
	/**
	 * Null value or unknown; should not occur.
	 */
	NONE(null);
	
	private final String specialLabel;
	
	IGTTierType(String specialLabel) {
		this.specialLabel = specialLabel;
	}
	
	/**
	 * An IGTTier is "Special" if it does not contain user Annotations
	 * but is one of the administratively added tiers such as Speaker.
	 */
	public boolean isSpecial() {
		return specialLabel != null;
	}

	/**
	 * Return the "tier label" for a Special tier.
	 * On non-Special tiers the result is undefined.
	 */
	public String label() {
		return specialLabel;
	}
}
