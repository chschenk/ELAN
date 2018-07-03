package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.interannotator.CompareCombi;
import mpi.eudico.client.annotator.interannotator.CompareConstants;
import mpi.eudico.client.annotator.interannotator.CompareResultWriter;
import mpi.eudico.client.annotator.interannotator.CompareUnit;
import mpi.eudico.client.annotator.interannotator.TierAndFileMatcher;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AnnotationCoreImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Abstract class for inter-rater agreement calculation. In this abstract class the preparatory work
 * is implemented which is necessary for most(?) actual implementations. 
 */
public abstract class AbstractCompareCommand extends AbstractProgressCommand {
	protected Map<Object, Object> compareProperties;
	protected List<CompareCombi> compareSegments;
	protected TranscriptionImpl transcription; 
	protected TierAndFileMatcher tfMatcher;
	protected final String eafExt = ".eaf";
	protected int numFiles;
	protected int numSelTiers;
	
	/**
	 * Constructor.
	 * @param theName name of the command
	 */
	public AbstractCompareCommand(String theName) {
		super(theName);
	}
	
	/**
	 * The arguments parameter should contain the map with settings needed for the
	 * creation of combinations of segmentation units.
	 * 
	 * @param receiver the transcription in case of single ("current") document processing,
	 *  null otherwise
	 *  @param arguments the array of arguments. 
	 *  arguments[0]: the map containing the selections made by the user
	 *  
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		super.execute(receiver, arguments);
		transcription = (TranscriptionImpl) receiver;
		if (transcription != null) {
			numFiles = 1;
		}
		compareProperties = (Map<Object, Object>) arguments[0];
		
		if (compareProperties == null || compareProperties.size() == 0) {
			progressInterrupt("No input provided for calculations.");
			return;
		}
		
		compareSegments = new ArrayList<CompareCombi>();
		
		tfMatcher = new TierAndFileMatcher();
		
		CompareAnnotatorsThread cat = new CompareAnnotatorsThread();
		cat.start();
	}
	
	/**
	 * Returns the results.
	 * 
	 * @return the resulting comparison combinations, combi's that include the results of the agreement
	 * calculations.
	 */
	public List<CompareCombi> getCompareSegments() {
		return compareSegments;
	}

	protected void logErrorAndInterrupt(String message) {
		ClientLogger.LOG.warning(message);
		progressInterrupt(message);
	}
	
	/**
	 * Creates combination objects containing minimal information necessary for the 
	 * calculation. 
	 */
	protected void createSegments() {
		// retrieve files or transcription, prepare tier-combinations for the actual compare routine
		// maybe prepare a list of pairs of objects, each object containing file name, tier name, and a list of segments? 
		CompareConstants.FILE_MATCHING sourceMatch = (CompareConstants.FILE_MATCHING) compareProperties.get(CompareConstants.TIER_SOURCE_KEY);
		CompareConstants.MATCHING tierMatching = (CompareConstants.MATCHING) compareProperties.get(CompareConstants.TIER_MATCH_KEY);
		CompareConstants.MATCHING fileMatching = (CompareConstants.MATCHING) compareProperties.get(CompareConstants.FILE_MATCH_KEY);
		String tierNameSeparators = (String) compareProperties.get(CompareConstants.TIER_SEPARATOR_KEY);
		String fileNameSeparators = (String) compareProperties.get(CompareConstants.FILE_SEPARATOR_KEY);
		// either tiername 1 and 2 or selTierNames
		String tierName1 = (String) compareProperties.get(CompareConstants.TIER_NAME1_KEY);
		String tierName2 = (String) compareProperties.get(CompareConstants.TIER_NAME2_KEY);
		List<String> selTierNames = (List<String>) compareProperties.get(CompareConstants.TIER_NAMES_KEY);
		List<String> allTierNames = (List<String>) compareProperties.get(CompareConstants.ALL_TIER_NAMES_KEY);
		List<File> selFiles = (List<File>) compareProperties.get(CompareConstants.SEL_FILES_KEY);
		if (selFiles != null) {
			numFiles = selFiles.size();
		}
		
		if (selTierNames != null) {
			numSelTiers = selTierNames.size();// x 2?
		} else if (tierName1 != null && tierName2 != null) {
			numSelTiers = 2;
		}
		curProgress = 1f;
		progressUpdate((int) curProgress, "Starting to extract segments from the selected tiers.");
		
		if (sourceMatch == CompareConstants.FILE_MATCHING.CURRENT_DOC) {
			if (transcription == null) {
				// report error, this shouldn't happen here
				progressInterrupt("The transcription is null");
				
				return;
			}
			
			if (tierMatching == CompareConstants.MATCHING.MANUAL) {
				if (tierName1 == null) {
					progressInterrupt("The first manually selected tier is null");
				
					return;
				}
				if (tierName2 == null) {
					progressInterrupt("The second manually selected tier is null");
					
					return;
				}
				if (tierName1.equals(tierName2)) {
					progressInterrupt("The first and second selected tier have the same name (not allowed)");

					return;
				}
				
				extractSegments(transcription, tierName1, tierName2);
			} else if (tierMatching == CompareConstants.MATCHING.PREFIX || tierMatching == CompareConstants.MATCHING.SUFFIX) {
				if (selTierNames == null || selTierNames.size() == 0) {
					progressInterrupt("There are no tiers selected for comparing based on affix");
					
					return;
				}
				extractSegments(transcription, selTierNames, tierMatching, tierNameSeparators);
			} else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
				progressInterrupt("Cannot compare tiers with the same name in the same document");
				
				return;
			}
		} else if (sourceMatch == CompareConstants.FILE_MATCHING.IN_SAME_FILE) {
			if (selFiles == null || selFiles.size() == 0) {
				progressInterrupt("There are no files selected, cannot retrieve the tiers to compare.");

				return;
			}
			if (tierMatching == CompareConstants.MATCHING.MANUAL) {
				if (tierName1 == null) {
					progressInterrupt("The first manually selected tier is null");

					return;
				}
				if (tierName2 == null) {
					progressInterrupt("The second manually selected tier is null");

					return;
				}
				if (tierName1.equals(tierName2)) {
					progressInterrupt("The first and second selected tier have the same name (not allowed)");

					return;
				}
				
				extractSegments(selFiles, null, null, tierName1, tierName2);
			} else if (tierMatching == CompareConstants.MATCHING.PREFIX || tierMatching == CompareConstants.MATCHING.SUFFIX) {
				if (selTierNames == null || selTierNames.size() == 0) {
					progressInterrupt("There are no tiers selected for comparing based on affix");

					return;
				}
				
				extractSegments(selFiles, null, null, 
						selTierNames, allTierNames, tierMatching, tierNameSeparators);
			} else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
				// error condition: tiers of the same name should not be in the same file
				progressInterrupt("Tiers with the same name cannot be in the same file");

				return;
				
			}
		} else if (sourceMatch == CompareConstants.FILE_MATCHING.ACROSS_FILES) {
			if (selFiles == null || selFiles.size() < 2) {
				progressInterrupt("There are no files or too few files selected, cannot retrieve the tiers to compare.");

				return;
			}
			if (fileMatching != CompareConstants.MATCHING.PREFIX && fileMatching != CompareConstants.MATCHING.SUFFIX) {
				progressInterrupt("Cannot determine how to match files, e.g. based on prefix or suffix");

				return;
			}
			if (tierMatching == CompareConstants.MATCHING.MANUAL) {
				// this only makes sense if there are two tier names and exactly two files?? or how to know
				// which tier should be retrieved from which file? or try all possible combinations?
				if (tierName1 == null) {
					progressInterrupt("The first manually selected tier is null");

					return;
				}
				if (tierName2 == null) {
					progressInterrupt("The second manually selected tier is null");

					return;
				}
				if (tierName1.equals(tierName2)) {
					progressInterrupt("The first and second selected tier have the same name (not allowed)");

					return;
				}
				
				extractSegments(selFiles, fileMatching, fileNameSeparators, tierName1, tierName2);
			} else if (tierMatching == CompareConstants.MATCHING.PREFIX || tierMatching == CompareConstants.MATCHING.SUFFIX) {
				if (selTierNames == null || selTierNames.size() == 0) {
					progressInterrupt("There are no tiers selected for comparing based on affix");

					return;
				}
				
				extractSegments(selFiles, fileMatching, fileNameSeparators, 
						selTierNames, allTierNames, tierMatching, tierNameSeparators);
			} else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
				if (selTierNames == null || selTierNames.size() == 0) {
					progressInterrupt("There are no tiers selected for comparing based on same name");

					return;
				}
				
				extractSegments(selFiles, fileMatching, fileNameSeparators, 
						selTierNames, null, null, null);
			}	
		}
		if (errorOccurred) {
			return;
		}
		if (cancelled) {
			progressInterrupt("The process was cancelled while extracting segments from the tiers.");
		}
		curProgress = 30f;
		if(compareSegments.size() > 0) {
			progressUpdate((int) curProgress, String.format("Extracted the annotations of %d pairs of tiers...", 
					compareSegments.size()));
		} else {
			progressInterrupt("There are no segments for the agreement calculation, process stopped.");
		}
	}
	
	/**
	 * Extracts the segments from the two tiers in the specified transcription.
	 * Assumes necessary checks have been performed.
	 * 
	 * @param transcription the transcription, not null
	 * @param tierName1 first tier
	 * @param tierName2 second tier
	 * 
	 * @return a list of size 1 with the segments of the two tiers, or null if something is wrong
	 */
	private void extractSegments(TranscriptionImpl transcription, 
			String tierName1, String tierName2) {
		TierImpl t1 = transcription.getTierWithId(tierName1); 
		TierImpl t2 = transcription.getTierWithId(tierName2);
		if (t1 == null) {
			// log or report?
			String message = String.format("The tier \"%s\" is not found in the transcription.", tierName1);
			logErrorAndInterrupt(message);
			return;
		}
		if (t2 == null) {
			// log or report?
			String message = String.format("The tier \"%s\" is not found in the transcription.", tierName2);
			logErrorAndInterrupt(message);
			return;
		}
		// 
		List<AnnotationCore> segments1 = getAnnotationCores(t1);
		
		curProgress = 15f;
		progressUpdate((int) curProgress, "Extracted segments from tier " + tierName1);
		
		List<AnnotationCore> segments2 = getAnnotationCores(t2);
		
		curProgress = 25f;
		progressUpdate((int) curProgress, "Extracted segments from tier " + tierName2);
		
		if (segments1.isEmpty() && segments2.isEmpty()) {
			// log or report?
			return;
		}
		
		CompareUnit cu1 = new CompareUnit(transcription.getFullPath(), t1.getName(), t1.getAnnotator());
		cu1.annotations = segments1;
		CompareUnit cu2 = new CompareUnit(transcription.getFullPath(), t2.getName(), t2.getAnnotator());
		cu2.annotations = segments2;
		CompareCombi cc = new CompareCombi(cu1, cu2);
		compareSegments.add(cc);
	}
	
	/**
	 * Combines tiers within a transcription based on the tier matching (prefix or suffix).
	 * Assumes necessary checks have been performed.
	 * 
	 * @param transcription the transcription, not null
	 * @param selTierNames the selected tiers, not null
	 * @param tierMatching prefix or suffix matching
	 * @param tierNameSeparators, custom separators can be null
	 * 
	 * @return a list of compare combinations, one combination (or more?) per selected tier name
	 */
	private void extractSegments(TranscriptionImpl transcription, List<String> selTierNames, 
			CompareConstants.MATCHING tierMatching, String tierNameSeparators) {	
		// get a map of tier combinations
		List<String> allTierNames = new ArrayList<String>();
		List<TierImpl> tiers = transcription.getTiers();
		Tier t = null;
		for (int i = 0; i < tiers.size(); i++) {
			t = tiers.get(i);
			allTierNames.add(t.getName());
		}
		List<List<String>> tierMatches = tfMatcher.getMatchingTiers(allTierNames, selTierNames, 
				tierMatching, tierNameSeparators);
		
		if (tierMatches.size() == 0) {
			logErrorAndInterrupt("No matching tiers (same name, different affix) have been found.");
			return;
		}
		curProgress = 3;
		progressUpdate((int) curProgress, String.format("Found %d pairs of tiers.", tierMatches.size()));
		// create all possible tier combinations based on the tier matches in the list
		float perMatch = 25f / tierMatches.size();
		
		for (List<String> curMatches : tierMatches) {
			for (int i = 0; i < curMatches.size(); i++) {// first loop
				TierImpl t1 = transcription.getTierWithId(curMatches.get(i));
				List<AnnotationCore> segments1 = null;
				if (t1 != null) {
					segments1 = getAnnotationCores(t1);
					for (int j = i + 1; j < curMatches.size(); j++) {//second loop
						TierImpl t2 = transcription.getTierWithId(curMatches.get(j));
						if (t2 != null) {
							List<AnnotationCore> segments2 = getAnnotationCores(t2);
							if (! (segments1.isEmpty() && segments2.isEmpty()) ) {
								// add a combination
								CompareUnit cu1 = new CompareUnit(transcription.getFullPath(), t1.getName(), t1.getAnnotator());
								cu1.annotations = segments1;
								CompareUnit cu2 = new CompareUnit(transcription.getFullPath(), t2.getName(), t2.getAnnotator());
								cu2.annotations = segments2;
								CompareCombi cc = new CompareCombi(cu1, cu2);
								compareSegments.add(cc);
							}
						}// else log??
					}
				}// else log??
			}
			
			curProgress += perMatch;
			progressUpdate((int) curProgress, null);
			if (cancelled) {
				return;
			}
		}

	}
	
	/**
	 * Combines tiers in a set of files. Depending on the file matching style the tiers are in the same file
	 * or in different files that are matched based on their names with different affix. 
	 * 
	 * @param selFiles the selected files containing the tier to compare
	 * @param fileMatching null in case the tiers are in the same file, CompareConstants.MATCHING.PREFIX or
	 * CompareConstants.MATCHING.SUFFIX in case tiers are in different files
	 * 
	 * @param fileNameSeparators custom separator(s) for filename-affix separation  
	 * @param tierName1 the first selected tier
	 * @param tierName2 the second selected tier
	 * @return a list of compare combinations
	 */
	private void extractSegments(List<File> selFiles, CompareConstants.MATCHING fileMatching, 
			String fileNameSeparators, String tierName1, String tierName2) {

		if (fileMatching == null) {
			// each file in the list should contain both tier1 and tier2
			progressUpdate((int) curProgress, "Extracting segments from each file...");
			float perFile = 28f / selFiles.size();
			for (File f : selFiles) {
				if (f.isDirectory()) {
					curProgress += perFile;
					continue; // log
				}
				// create a Transcription of the file and check tiers
				TranscriptionImpl t1 = createTranscription(f);
				if (t1 != null) {
					TierImpl tier1 = t1.getTierWithId(tierName1);
					TierImpl tier2 = t1.getTierWithId(tierName2);
					
					CompareCombi cc= createCompareCombi(tier1, tier2);
					if (cc != null) {
						compareSegments.add(cc);
					}
				} //else log?
				curProgress += perFile;
				progressUpdate((int) curProgress, null);
			}
		} else {			
			progressUpdate((int) curProgress, "Extracting segments from file pairs...");
			List<List<File>> matchingFiles = tfMatcher.getMatchingFiles(selFiles, fileMatching, 
					fileNameSeparators, eafExt);
			if (matchingFiles.size() == 0) {
				logErrorAndInterrupt("No matching files found in the list of selected files");
				return;
			}
			curProgress = 4;
			progressUpdate((int) curProgress, String.format("Found %d pairs of matching files...", matchingFiles.size()) );
			// loop over matches, find right tiers in all combinations of files
			TranscriptionImpl t1 = null;
			TranscriptionImpl t2 = null;
			float perMatch = 25f / matchingFiles.size();
			for (List<File> matchList : matchingFiles) {
				// convert to list of transcriptions first (to avoid loading the same file more than once)? 
				// In most cases there will only be two files
				for (int i = 0; i < matchList.size(); i++) {
					t1 = createTranscription(matchList.get(i));
					if (t1 == null) {
						continue;
					}
					for (int j = i + 1; j < matchList.size(); j++) {
						t2 = createTranscription(matchList.get(j));
						if (t2 == null) {
							continue;
						}
						
						compareSegments.addAll(getCompareCombinations(t1, t2, tierName1, tierName2));
					}
				}
				curProgress += perMatch;
				progressUpdate((int) curProgress, null);
			}
		}
		progressUpdate((int) curProgress, String.format("Extracted the annotations of %1$d pairs of tiers from %2$d files...", 
				compareSegments.size(), selFiles.size()));
	}
	
	/**
	 * Combines tiers in a set of files. Depending on the file matching style the tiers are in the same file
	 * or in different files that are matched based on their names with different affix. Based on the tier matching
	 * style the tiers either the same name but in different files or they are matched based on tier name and affix. 
	 * It is an error if both file matching and tier matching parameters are null.
	 * 
	 * @param selFiles the selected files
	 * @param fileMatching null in case the tiers are in the same file, CompareConstants.MATCHING.PREFIX or
	 * CompareConstants.MATCHING.SUFFIX in case tiers are in different files
	 * @param fileNameSeparators custom separator(s) for filename-affix separation 
	 * @param selTierNames the selected tier names
	 * @param allTierNames all the tiers in the selected files
	 * @param tierMatching null in case the tiers are matched based on same-name-different-file, CompareConstants.MATCHING.PREFIX or
	 * CompareConstants.MATCHING.SUFFIX in case tiers are matched based on tiername-affix separation. In the latter case
	 * matching can be different-tiername-same file of different-tiername-different-file
	 * @param tierNameSeparators custom separator(s) for tiername-affix separation 
	 * @return a list of compare combinations
	 */
	private void extractSegments(List<File> selFiles, CompareConstants.MATCHING fileMatching, String fileNameSeparators, 
			List<String> selTierNames, List<String> allTierNames, CompareConstants.MATCHING tierMatching, String tierNameSeparators) {
		if (fileMatching == null && tierMatching == null) {
			// this would mean comparing tiers with the same name in the same file
			logErrorAndInterrupt("Cannot compare tiers with the same name in the same file.");
			return;
		}
		List<List<File>> matchingFiles = null;
		List<List<String>> matchingTiers = null;
		
		if (fileMatching == null) {
			// each file in the list should contain the combination of tiers

		} else { // file matching based on prefix or suffix
			matchingFiles = tfMatcher.getMatchingFiles(selFiles, fileMatching, fileNameSeparators, eafExt);
			if (matchingFiles.isEmpty()) {// can't compare
				// log or report...
				logErrorAndInterrupt("No matching files found in the list of selected files.");
				return;
			}
		}
		
		if (tierMatching == CompareConstants.MATCHING.PREFIX || tierMatching == CompareConstants.MATCHING.SUFFIX) {
			matchingTiers = tfMatcher.getMatchingTiers(allTierNames, selTierNames, 
					tierMatching, tierNameSeparators);
		}
		curProgress = 3;
		progressUpdate((int) curProgress, null);
		
		if (matchingFiles == null) {// tiers in the same file, matchingTiers cannot be null
			progressUpdate((int) curProgress, "Extracting segments from each file...");
			float perFile = 26f / selFiles.size();
			
			for (File f : selFiles) {
				if (f.isDirectory()) {
					curProgress += perFile;
					continue; // log
				}
				// create a Transcription of the file and check tiers
				TranscriptionImpl t1 = createTranscription(f);
				if (t1 != null) {
					// loop over all matched tiers (in most cases probably two tiers)
					for (List<String> tierMatch : matchingTiers) {
						for (int i = 0; i < tierMatch.size(); i++) {
							String tName1 = tierMatch.get(i);
							for (int j = i + 1; j < tierMatch.size(); j++) {
								String tName2 = tierMatch.get(j);
								
								TierImpl tier1 = t1.getTierWithId(tName1);
								TierImpl tier2 = t1.getTierWithId(tName2);
								
								CompareCombi cc= createCompareCombi(tier1, tier2);
								if (cc != null) {
									compareSegments.add(cc);
								}
							}
						}
					}					
				} //else log?
				curProgress += perFile;
				progressUpdate((int) curProgress, null);
			}
			
		} else {// tiers in different files
			progressUpdate((int) curProgress, "Extracting segments from file pairs...");
			float perMatch = 28f / matchingFiles.size();
			
			for (List<File> fileMatch : matchingFiles) {
				List<TranscriptionImpl> transMatch = createTranscriptions(fileMatch);
				if (transMatch.size() <= 1) {
					// log the files that cannot be processed
					curProgress += perMatch;
					continue;
				}

				TranscriptionImpl ti1 = null;
				TranscriptionImpl ti2 = null;
				
				for (int i = 0; i < transMatch.size() - 1; i++) {
					ti1 = transMatch.get(i);
					for (int j = i + 1; j < transMatch.size(); j++) {
						ti2 = transMatch.get(j);
						// we have two transcriptions now, loop over selected tiers or tier combinations
						
						if (matchingTiers == null) {// tiers of the same name in different files
							TierImpl tier1 = null;
							TierImpl tier2 = null;
							
							for (String tierName : selTierNames) {
								tier1 = ti1.getTierWithId(tierName);
								tier2 = ti2.getTierWithId(tierName);
								
								if (tier1 != null && tier2 != null) {
									CompareCombi cc = createCompareCombi(tier1, tier2);
									if (cc != null) {
										compareSegments.add(cc);
									}
								}
							}
						} else {// tiers matching based on affix, in different files based on affix
							for (List<String> tierMatch : matchingTiers) {
								String name1 = null;
								String name2 = null;
								for (int m = 0; m < tierMatch.size() - 1; m++) {
									for (int n = m + 1; n < tierMatch.size(); n++) {
										name1 = tierMatch.get(m);
										name2 = tierMatch.get(n);
										
										compareSegments.addAll(getCompareCombinations(ti1, ti2, name1, name2));
									}
								}
							}
						}
					}
				}
				curProgress += perMatch;
				progressUpdate((int) curProgress, null);
			}
		}
		progressUpdate((int) curProgress, String.format("Extracted the annotations of %1$d pairs of tiers from %2$d files...", 
				compareSegments.size(), selFiles.size()));
	}
	
	/**
	 * Creates a list of AnnotationCore objects from the annotations in the tier.
	 *  
	 * @param t the tier
	 * @return a list of annotation core objects
	 */
	private List<AnnotationCore> getAnnotationCores(TierImpl t) {
		List<AnnotationCore> acs = new ArrayList<AnnotationCore>();
		
		if (t != null) {
			List<AbstractAnnotation> anns = t.getAnnotations();

			if (anns != null) {
				for (int i = 0; i < anns.size(); i++) {
					AnnotationCore ac = anns.get(i);
					acs.add(new AnnotationCoreImpl(ac.getValue(), ac.getBeginTimeBoundary(), ac.getEndTimeBoundary()));
				}
			}
		}
		
		return acs;
	}
	
	private CompareCombi createCompareCombi(TierImpl t1, TierImpl t2) {
		if (t1 == null || t2 == null) {
			return null;
		}
		
		List<AnnotationCore> segments1 = getAnnotationCores(t1);
		List<AnnotationCore> segments2 = getAnnotationCores(t2);
		if (segments1.isEmpty() && segments2.isEmpty()) {
			// log?
			return null;
		}
		
		CompareUnit cu1 = new CompareUnit(t1.getTranscription().getFullPath(), t1.getName(), t1.getAnnotator());
		cu1.annotations = segments1;
		CompareUnit cu2 = new CompareUnit(t2.getTranscription().getFullPath(), t2.getName(), t2.getAnnotator());
		cu2.annotations = segments2;
		
		return new CompareCombi(cu1, cu2);
	}
	
	/**
	 * Returns a transcription object for a  file or null
	 * 
	 * @param f the file
	 * @return a transcription object or null
	 */
	private TranscriptionImpl createTranscription(File f) {
		if (f == null || f.isDirectory()) {
			return null;
		}
		try {
			return new TranscriptionImpl(f.getAbsolutePath());
		} catch (Throwable t) {// catch any
			// log
			ClientLogger.LOG.warning("Could not load a transcription from file: " + f.getName());
		}
		return null;
	}
	
	/**
	 * Creates a list of transcriptions based on the list of files.
	 * 
	 * @param files the list of files to load, not null
	 * @return a list of transcriptions
	 */
	private List<TranscriptionImpl> createTranscriptions(List<File> files) {
		List<TranscriptionImpl> transList = new ArrayList<TranscriptionImpl>(files.size());
		
		for (File f : files) {
			TranscriptionImpl ti = createTranscription(f);
			if (ti != null) {
				transList.add(ti);
			}
		}
		return transList;
	}
	
	/**
	 * Returns a list of tier combinations for comparing, the list contains 0, 1 or 2 elements.
	 * 
	 * @param t1 first transcription
	 * @param t2 second transcription
	 * @param tierName1 first tier name
	 * @param tierName2 second tier name
	 * 
	 * @return a list of CompareCombi objects
	 */
	private List<CompareCombi> getCompareCombinations(TranscriptionImpl t1, TranscriptionImpl t2, 
			String tierName1, String tierName2) {
		List<CompareCombi> combinations= new ArrayList<CompareCombi>();
		if (t1 == null || t2 == null) {
			// log error
			return combinations;
		}
		TierImpl tier1 = null;
		TierImpl tier2 = null;
		
		tier1 = t1.getTierWithId(tierName1);
		tier2 = t2.getTierWithId(tierName2);
		
		if (tier1 != null && tier2 != null) {
			CompareCombi cc = createCompareCombi(tier1, tier2);
			if (cc != null) {
				combinations.add(cc);
			}
		} //else {
			// log??
		//}
		tier1 = t1.getTierWithId(tierName2);
		tier2 = t2.getTierWithId(tierName1);
		
		if (tier1 != null && tier2 != null) {
			CompareCombi cc = createCompareCombi(tier1, tier2);
			if (cc != null) {
				combinations.add(cc);
			}
		}
		
		return combinations;
	}
	
	/**
	 * Applies the selected algorithm to the segments to calculate agreement values.
	 */
	protected void calculateAgreement() {
		
	}
	
	/**
	 * Saves the results to a file. The default is to write comparison combinations with 
	 * an agreement value to a text file.
	 * Note: maybe there is no need for a default implementation.  
	 * 
	 * @param toFile the file to write to
	 * @param encoding the encoding to use when saving as text file
	 * 
	 * @throws IOException any IO exception
	 */
	public void writeResultsAsText(File toFile, String encoding) throws IOException {
		CompareResultWriter crWriter = new CompareResultWriter();
		crWriter.writeResults(compareSegments, toFile, encoding);
	}

	/**
	 * Starts the calculations in a separate thread. 
	 * From this thread some methods are called that can be or have to be implemented 
	 * by the actual class. 
	 */
	class CompareAnnotatorsThread extends Thread {

		@Override
		public void run() {
			// pre-processing
			createSegments();
			// actual calculation
			if (!errorOccurred && !cancelled) {
				calculateAgreement();
			}
		}	
		
	}


}
