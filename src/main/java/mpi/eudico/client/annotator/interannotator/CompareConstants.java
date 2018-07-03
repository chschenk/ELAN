package mpi.eudico.client.annotator.interannotator;
/**
 * An interface defining constants for usage as step and preferences keys for 
 * inter-annotator agreement calculation wizard.
 *  
 * @author Han Sloetjes
 *
 */
public interface CompareConstants {

	public static enum METHOD {
		CLASSIC ("Classic"),
		MOD_KAPPA ("Modified Kappa"),
		STACCATO ("Staccato");
		
		public String value;
		
		private METHOD(String val) {
			this.value = val;
		}

		@Override
		public String toString() {
			return value;
		}
	}
	
	/** 
	 * An enumeration of File and Tier matching constants
	 * 
	 * @author Han Sloetjes
	 */
	public static enum MATCHING {
		MANUAL ("Manual"),
		AFFIX ("Affix based"),
		SUFFIX ("Suffix"),
		PREFIX ("Prefix"),
		SAME_NAME ("Same name");
		
		public String value;
		
		private MATCHING(String val) {
			this.value = val;
		}

		@Override
		public String toString() {
			return value;
		}	
	}
	
	/** 
	 * An enumeration of File matching constants
	 * 
	 * @author Han Sloetjes
	 */
	public static enum FILE_MATCHING {
		CURRENT_DOC ("In current document"),
		IN_SAME_FILE ("In same file"),
		ACROSS_FILES ("Across files");
		
		public String value;
		
		private FILE_MATCHING(String val) {
			this.value = val;
		}

		@Override
		public String toString() {
			return value;
		}	
	}

	// preferences keys
	public static final String METHOD_KEY = "Compare.CompareMethod";
	public static final String FILE_MATCH_KEY = "Compare.FileMatching";
	public static final String TIER_MATCH_KEY = "Compare.TierMatching";
	public static final String FILE_SEPARATOR_KEY = "Compare.FileSeparator";
	public static final String TIER_SEPARATOR_KEY = "Compare.TierSeparator";
	public static final String TIER_SOURCE_KEY = "Compare.TierSource";
	public static final String SEL_FILES_KEY = "Compare.SelectedFiles";
	public static final String TIER_NAME1_KEY = "Compare.TierName1";
	public static final String TIER_NAME2_KEY = "Compare.TierName2";
	public static final String TIER_NAMES_KEY = "Compare.SelectedTierNames";
	public static final String ALL_TIER_NAMES_KEY = "Compare.AllTierNames";
	public static final String OVERLAP_PERCENTAGE = "Compare.OverlapPercentage";
	public static final String OUTPUT_PER_FILE = "Compare.Output.PerFile";
	public static final String OUTPUT_PER_TIER_PAIR = "Compare.Output.PerTierPair";
	public static final String MONTE_CARLO_SIM = "Compare.MonteCarloSimulations";
	public static final String NUM_NOMINATIONS = "Compare.NumberOfNominations";
	public static final String NULL_HYPOTHESIS = "Compare.NullHypothesis.Value";
	
	// constant for unmatched or unlinked annotations
	public static final String UNMATCHED = "Unmatched";
}
