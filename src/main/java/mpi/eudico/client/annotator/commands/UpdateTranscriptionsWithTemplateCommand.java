package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.imports.MergeUtil;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.ConstraintImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.util.TranscriptionCompare;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.util.BasicControlledVocabulary;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;

/**
 * A command to update a corpus / multiple files with the changes made to the template
 * the files were based on. The files don't contain a link to the template, so it is 
 * up to the user to keep track of which files can be updated with which template.
 * 
 * This procedure adds new elements and updates modified elements but does not 
 * remove elements that are in the files but (no longer) in the template. 
 * 
 * @author Han Sloetjes
 *
 * July 2018
 */
public class UpdateTranscriptionsWithTemplateCommand implements
		ProcessReporter, Command {
	private String name;
	private ProgressListener listener;
	private ProcessReport reporter;
	// data objects
	private List<File> fileList;
	private TranscriptionImpl templateTrans;
	private Map<String, Object> templatePrefs;
	private Boolean dryRunFlag;
	private Boolean forceCVReplacement = false;
	
	private UpdateThread updateThread;
	private boolean cancelled = false;
	
	/**
	 * Constructor.
	 * 
	 * @param name name of the command
	 */
	public UpdateTranscriptionsWithTemplateCommand(String name) {
		this.name = name;
	}

	/**
	 * @param receiver null, ignored
	 * @param arguments arg[0] = files to update (List<File>), arg[1] = template 
	 * transcription (TranscriptionImpl or File), arg[2] = dry run flag (Boolean, optional),
	 * arg[3] = flag whether CV's should just be replaced, without checking
	 * for local changes in the files (Boolean, optional) 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void execute(Object receiver, Object[] arguments) {
		if (arguments[0] instanceof List<?>) {
			fileList = (List<File>) arguments[0];
		}
		if (fileList == null || fileList.isEmpty()) {
			report("The list of files to update is null or empty");
			processCancelled("The list of files to update is null or empty");
			return;
		}
		
		if (arguments[1] instanceof TranscriptionImpl) {
			templateTrans = (TranscriptionImpl) arguments[1];
		} else if (arguments[1] instanceof File) {
			File tf = (File) arguments[1];
			try {
				templateTrans = new TranscriptionImpl(tf.getAbsolutePath());
				//templateTrans.setNotifying(false);
			} catch (Throwable t){
				report(String.format("Unable to load the template: %s", t.getMessage()));
				processCancelled(String.format("Unable to load the template: %s", t.getMessage()));
				return;
			}
		}
		if (templateTrans == null) {
			report("The template is null, could not be loaded");
			return;
		}

		if (arguments.length >= 3) {
			if (arguments[2] instanceof Boolean) {
				dryRunFlag = (Boolean) arguments[2];
			}
		}
		if (arguments.length >= 4) {
			if (arguments[3] instanceof Boolean) {
				forceCVReplacement = (Boolean) arguments[3];
			}
		}
		
		templatePrefs = Preferences.loadPreferencesForFile(templateTrans.getFullPath());
		
		updateThread = new UpdateThread();
		updateThread.start();
	}

	/**
	 * @return the name of the command
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the Report object to write report messages to. This should be done
	 * before calling {@link #execute(Object, Object[])}
	 * 
	 *  @param report the target for report messages
	 */
	@Override
	public void setProcessReport(ProcessReport report) {
		this.reporter = report;
	}

	/**
	 * @return the Report object or null if none is set
	 */
	@Override
	public ProcessReport getProcessReport() {
		return reporter;
	}

	/**
	 * @param message the message to add to the report object, if the report object is not null
	 */
	@Override
	public void report(String message) {
		if (reporter != null) {
			reporter.append(message);
		} else {
			// log or send to System.out?
		}
	}

	/**
	 * Cancels the operation by breaking the loop over the files. 
	 * The file that is being processed will be finished.
	 * 
	 * @param cancelled if true, the process will be terminated
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * Pass <tt>null</tt> to unregister the current listener.
	 * 
	 * @param listener the listener to inform about the progress
	 */
	public void setProgressListener(ProgressListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Informs the listener, if any, of the progress. 0 is at start,
	 * 100 indicates completion.
	 *  
	 * @param percentage the progress as percentage (0 - 100)
	 * @param message a message to display
	 */
	private void setProgress(float percentage, String message) {
		if (listener != null) {
			if (percentage == 100) {
				listener.progressCompleted(this, message);
			} else {
				listener.progressUpdated(this, (int) percentage, message);
			}
		}
	}
	
	/**
	 * Informs the listener, if any, that the process was cancelled.
	 * 
	 * @param message a message to display
	 */
	private void processCancelled(String message) {
		if (listener != null) {
			listener.progressInterrupted(this, message);
		}
	}
	
	/**
	 * The actual work is performed in a separate thread.
	 * 
	 */
	private class UpdateThread extends Thread {
		private MergeUtil mergeUtil;
	
		private final String	NEW_TIERS = "nw_tiers";
		private final String	CH_TIERS = "cg_tiers";
		private final String	NEW_TYPES = "nw_types";
		private final String	CH_TYPES = "ch_types";
		private final String	NEW_CVS = "nw_cvs";
		private final String	CH_CVS = "ch_cvs";
		/* first comparisons are made, differences are stored in a map for use in the 
		 * actual updating */
		private Map<String, List<String>> changeMap = new HashMap<String, List<String>>(8);
		
		/**
		 * The actual process, looping over the files
		 */
		@Override
		public void run() {
			float curProg = 0.0f;
			float perFilePercentage = 1.0f;
			
			if (fileList.size() > 0) {
				perFilePercentage = 100.0f / fileList.size();
			}
			
			for (File f : fileList) {
				report("\nFILE:  " + f.getAbsolutePath());
				// load transcription for f
				TranscriptionImpl trans = null;
				try {
					trans = new TranscriptionImpl(f.getAbsolutePath());
					trans.setNotifying(false);
					trans.setUnchanged();
				} catch (Throwable t) {
					// catch any IO or Parse exception
					report("Unable to load: " + t.getMessage());
					curProg += perFilePercentage;
					setProgress(curProg, "Error - skipping file: " + f.getName());
					continue;
				}
				// clear the map for each file
				changeMap.clear();
				
				// check the differences with the template
				reportDifferences(templateTrans, trans);
				// report, update progress and continue if dry run flag is true
				if (dryRunFlag != null && dryRunFlag.booleanValue()) {
					curProg += perFilePercentage;
					setProgress(Math.min(99.0f, curProg), "Processed file: " + f.getName());
					if (cancelled) {
						report(String.format("The process was cancelled after %f%% of the files was processed", curProg));
						break;
					}
					continue;
				}
				
				// update transcription and save
				// update in reverse order: licenses, CV's, types, tiers, all including external references
				if (mergeUtil == null) {
					mergeUtil = new MergeUtil();
				}
				report("Updating file:");
				updateDifferences(templateTrans, trans, mergeUtil);
				
				// if the transcription changed, save it and update preferences
				if (trans.isChanged()) {
					try {
						ACMTranscriptionStore.getCurrentTranscriptionStore().storeTranscription(trans, 
								null, null, TranscriptionStore.EAF);
						mergeUtil.updatePreferences(trans, templatePrefs, 
								// the preferences to copy
								Preferences.CV_PREFS, "TierColors",
								"TierHighlightColors", "TierFonts",
								"MultiTierViewer.TierOrder", "MultiTierViewer.TierSortingMode",
								"MultiTierViewer.HiddenTiers"//??
								);
					} catch (IOException ioe) {
						report(String.format("Could not save the file: %s, cause: %s", f.getName(), 
								ioe.getMessage()));
					} catch (Throwable t) {
						report(String.format("Could not save the file: %s, cause: %s", f.getName(), 
								t.getMessage()));						
					}
				}
				
				// update progress
				curProg += perFilePercentage;
				setProgress(Math.min(99.0f, curProg), "Processed file: " + f.getName());
				if (cancelled) {
					report(String.format("The process was cancelled after %f%% of the files was processed", curProg));
					break;
				}
			}
			
			if (cancelled) {
				processCancelled("Process cancelled");				
			} else {
				setProgress(100, "Processed all files");
			}
		}
		
	    // #### private methods for reporting differences in tiers, types, ####
		// #### controlled vocabularies, languages, licenses and author    ####
		private void reportDifferences(TranscriptionImpl templTrans, TranscriptionImpl trans) {
			TranscriptionCompare trComp = new TranscriptionCompare();
			report("Comparing transcription (T1) with template (T2):");
			// tiers
			List<String> dfList = trComp.getTierNamesOnlyInFirst(trans, templTrans);
			reportList(String.format("Number of tiers only in T1: %d", dfList.size()), dfList);
			
			dfList = trComp.getTierNamesOnlyInFirst(templTrans, trans);
			reportList(String.format("Number of tiers only in T2: %d", dfList.size()), dfList);
			changeMap.put(NEW_TIERS, dfList);
			// check differences in tiers in both files
			dfList = trComp.getTierNamesInBoth(trans, templTrans);
			reportTierDifferences(String.format("Number of tiers both in T1 and T2: %d", dfList.size()),
					dfList, trComp, trans, templTrans);
			changeMap.put(CH_TIERS, dfList);
			// types
			dfList = trComp.getTypeNamesOnlyInFirst(trans, templTrans);
			reportList(String.format("Number of types only in T1: %d", dfList.size()), dfList);
			
			dfList = trComp.getTypeNamesOnlyInFirst(templTrans, trans);
			reportList(String.format("Number of types only in T2: %d", dfList.size()), dfList);
			changeMap.put(NEW_TYPES, dfList);
			// type differences
			dfList = trComp.getTypeNamesInBoth(trans, templTrans);
			reportTypeDifferences(String.format("Number of types both in T1 and T2: %d", dfList.size()),
					dfList, trComp, trans, templTrans);
			changeMap.put(CH_TYPES, dfList);
			
			// cv's
			dfList = trComp.getCVNamesOnlyInFirst(trans, templTrans);
			reportList(String.format("Number of CV's only in T1: %d", dfList.size()), dfList);
			
			dfList = trComp.getCVNamesOnlyInFirst(templTrans, trans);
			reportList(String.format("Number of CV's only in T2: %d", dfList.size()), dfList);
			changeMap.put(NEW_CVS, dfList);
			// compare internal and external CV's
			dfList = trComp.getCVNamesInBoth(trans, templTrans);
			reportCVDifferences(String.format("Number of CV's both in T1 and T2: %d", dfList.size()),
					dfList, trComp, trans, templTrans);
			changeMap.put(CH_CVS, dfList);
			
			// languages
			dfList = trComp.getLanguagesOnlyInFirst(trans, templTrans);
			reportList(String.format("Number of content languages only in T1: %d", dfList.size()), dfList);
			
			dfList = trComp.getLanguagesOnlyInFirst(templTrans, trans);
			reportList(String.format("Number of content languages only in T2: %d", dfList.size()), dfList);
			
			dfList = trComp.getLanguagesInBoth(trans, templTrans);
			reportList(String.format("Number of content languages both in T1 and T2: %d", dfList.size()), dfList);
			// licenses
			reportLicenseDifferences(trComp, trans, templTrans);
			// author
			reportAuthorDifferences(trans, templTrans);
		}
		
		/**
		 * Collects differences in tiers and tier structures and reports those. 
		 * 
		 * @param heading a string to output before the tier differences are reported
		 * @param dfList a list of tier names that are in both transcriptions
		 * @param trComp contains methods for the actual comparisons
		 * @param tr1 the first transcription  (the file to update)
		 * @param tr2 the second transcription (the template)
		 */
		private void reportTierDifferences(String heading, List<String> dfList, TranscriptionCompare trComp, 
				TranscriptionImpl tr1, TranscriptionImpl tr2) {
			report(heading);
			if (dfList.size() == 0) {
				return;
			}
			final String dTab = "\t\t";
			int numTiersWithDiffs  = 0;
			int numPropDifferences = 0;
			// an array of properties to compare
			TierImpl.ValueGetter[] propGetters = new TierImpl.ValueGetter[] {
					new TierImpl.ParentTierNameGetter(),
					new TierImpl.LinguisticTypeNameGetter(),
					new TierImpl.ParticipantGetter(),
					new TierImpl.AnnotatorGetter(),
					new TierImpl.LanguageGetter(),
					new TierImpl.LocaleGetter()
			};
			
			for (String s : dfList) {
				TierImpl t1 = tr1.getTierWithId(s);
				TierImpl t2 = tr2.getTierWithId(s);
				boolean anyDiff = false;
				boolean[] propDiffs = new boolean[propGetters.length];
				for (int i = 0; i < propDiffs.length; i++) {
					propDiffs[i] = !trComp.sameTierProperty(t1, t2, propGetters[i]);
					if (propDiffs[i]) {
						anyDiff = true;
					}
				}
				
				if (anyDiff) {
					numTiersWithDiffs++;
					report(String.format("\tDifference in: \"%s\"", s));
					for (int i = 0; i < propDiffs.length; i++) {
						if (propDiffs[i]) {
							numPropDifferences++;
							String p1 = propGetters[i].getSortValue(t1); 								
							String p2 = propGetters[i].getSortValue(t2);
							switch(i) {
							case 0: 
								report(String.format("%sParent tier T1: \"%s\", T2: \"%s\"", dTab, p1.isEmpty() ? "-" : 
									p1, p2.isEmpty() ? "-" : p2));
								break;
							case 1:
								report(String.format("%sTier type T1: \"%s\", T2: \"%s\"", dTab, p1, p2));
								break;
							case 2:
								report(String.format("%sParticipant T1: \"%s\", T2: \"%s\"", dTab, p1, p2));
								break;
							case 3:
								report(String.format("%sAnnotator T1: \"%s\", T2: \"%s\"", dTab, p1, p2));
								break;
							case 4:
								report(String.format("%sContent language T1: \"%s\", T2: \"%s\"", dTab, p1, p2));
								break;
							case 5:
								report(String.format("%sLocale (Input Method) T1: \"%s\", T2: \"%s\"", dTab, p1, p2));
							}
						}
					}
				}
			}
			// report the number of tiers with differences and the total number of differences
			if (numTiersWithDiffs > 0) {
				report(String.format("Found %d differences in %d of the %d tiers in T1 and T2.", 
						numPropDifferences, numTiersWithDiffs, dfList.size()));
			} else {
				report(String.format("Found no differences in the %d tiers both in T1 and T2.", dfList.size()));
			}
		}
		
		/**
		 * Reports differences in tier types in the two transcriptions
		 * 
		 * @param heading a string to output before the type differences are reported
		 * @param dfList a list of types that are in both transcriptions
		 * @param trComp contains methods for the actual comparisons
		 * @param tr1 the first transcription
		 * @param tr2 the second transcription
		 */
		private void reportTypeDifferences(String heading, List<String> dfList, TranscriptionCompare trComp, 
				TranscriptionImpl tr1, TranscriptionImpl tr2) {
			report(heading);
			if (dfList.size() == 0) {
				return;
			}
			final String dTab = "\t\t";
			int numTypesWithDiffs  = 0;
			int numPropDifferences = 0;
			LinguisticType.PropKey[] propKeys = new LinguisticType.PropKey[] {
					LinguisticType.PropKey.CONSTRAINT,
					LinguisticType.PropKey.CV_NAME,
					LinguisticType.PropKey.DC,
					LinguisticType.PropKey.LEX_BUNDLE
			};
			
			for (String s : dfList) {
				LinguisticType lt1 = tr1.getLinguisticTypeByName(s);
				LinguisticType lt2 = tr2.getLinguisticTypeByName(s);
				boolean anyDiff = false;
				boolean[] propDiffs = new boolean[propKeys.length];
				
				for (int i = 0; i < propDiffs.length; i++) {
					propDiffs[i] = !trComp.sameTypeProperty(lt1, lt2, propKeys[i]);
					if (propDiffs[i]) {
						anyDiff = true;
					}
				}
				
				if (anyDiff) {
					numTypesWithDiffs++;
					report(String.format("\tDifference in: \"%s\"", s));
					for (int i = 0; i < propDiffs.length; i++) {
						if (propDiffs[i]) {
							numPropDifferences++;
							
							switch (propKeys[i]) {
							case NAME:
							case ID:
								break;
							case CONSTRAINT:
								report(String.format("%sStereotype in T1: \"%s\", T2: \"%s\"", dTab, 
										ConstraintImpl.getStereoTypeName(
												lt1.getConstraints() == null ? -1 : lt1.getConstraints().getStereoType()),
										ConstraintImpl.getStereoTypeName(
												lt2.getConstraints() == null ? -1 : lt2.getConstraints().getStereoType()) ));
								break;
							case CV_NAME:
								String cvn1 = lt1.getControlledVocabularyName();
								String cvn2 = lt2.getControlledVocabularyName();
								report(String.format("%sControlled Vocabulary in T1: \"%s\", T2: \"%s\"", dTab, 
										cvn1 != null ? cvn1 : "-", cvn2 != null ? cvn2 : "-" ));
								break;
							case DC:
								String dcRef  = lt1.getDataCategory();
								String dcRef2 = lt2.getDataCategory();
								report(String.format("%sData Category in T1: \"%s\", T2: \"%s\"", dTab, 
										dcRef != null ? dcRef : "-", dcRef2 != null ? dcRef2 : "-" ));
								break;
							case LEX_BUNDLE:
								String ll1 = null;
								String ll2 = null;
								if (lt1.getLexiconQueryBundle() != null) {
									ll1 = String.format("%s:%s", lt1.getLexiconQueryBundle().getLink() != null ? lt1.getLexiconQueryBundle().getLink().getName() : "-", 
											lt1.getLexiconQueryBundle().getFldId() != null ? lt1.getLexiconQueryBundle().getFldId().getName() : "-");
								}
								if (lt2.getLexiconQueryBundle() != null) {
									ll2 = String.format("%s:%s", lt2.getLexiconQueryBundle().getLink() != null ? lt2.getLexiconQueryBundle().getLink().getName() : "-", 
											lt2.getLexiconQueryBundle().getFldId() != null ? lt2.getLexiconQueryBundle().getFldId().getName() : "-");
								}
								
								report(String.format("%sLexicon reference in T1: \"%s\", T2: \"%s\"", dTab, 
										ll1 != null ? ll1 : "-", ll2 != null ? ll2 : "-" ));
								break;
							case LEX_FIELD:
							case LEX_LINK:
								// part of the LEX_BUNDLE comparison
								break;
							}
						}
					}
				}
			}
			
			if (numTypesWithDiffs > 0) {
				report(String.format("Found %d differences in %d of the %d tier types in T1 and T2.", 
						numPropDifferences, numTypesWithDiffs, dfList.size()));
			} else {
				report(String.format("Found no differences in the %d tier types both in T1 and T2.", dfList.size()));
			}
		}
		
		/**
		 * Reports differences between Controlled Vocabularies of the same name in both transcriptions
		 * 
		 * @param heading a string to output before the CV differences are reported
		 * @param dfList list of CV names that are in both transcriptions
		 * @param trComp contains methods for the actual comparisons
		 * @param tr1 the first transcription
		 * @param tr2 the second transcription
		 */
		private void reportCVDifferences(String heading, List<String> dfList, TranscriptionCompare trComp, 
				TranscriptionImpl tr1, TranscriptionImpl tr2) {
			report(heading);
			if (dfList.size() == 0) {
				return;
			}
			final String dTab = "\t\t";
			int numCVsWithDiffs  = 0;
			int numPropDifferences = 0;
			
			BasicControlledVocabulary.PropKey[] propKeys = new BasicControlledVocabulary.PropKey[]{
					BasicControlledVocabulary.PropKey.DESCRIPTION,
					BasicControlledVocabulary.PropKey.NUM_ENTRIES,
					BasicControlledVocabulary.PropKey.NUM_LANGUAGES,
					BasicControlledVocabulary.PropKey.EXTERNAL_REF
			};
			
			for (String s : dfList) {
				ControlledVocabulary cv1 = tr1.getControlledVocabulary(s);
				ControlledVocabulary cv2 = tr2.getControlledVocabulary(s);
				boolean anyDiff = false;
				boolean[] propDiffs = new boolean[propKeys.length];
				
				for (int i = 0; i < propDiffs.length; i++) {
					propDiffs[i] = !trComp.sameCVProperty(cv1, cv2, propKeys[i]);
					if (propDiffs[i]) {
						anyDiff = true;
					}
				}
				
				if (anyDiff) {
					numCVsWithDiffs++;
					report(String.format("\tDifference in: \"%s\"", s));
					
					for (int i = 0; i < propDiffs.length; i++) {
						if (propDiffs[i]) {
							numPropDifferences++;
							
							switch (propKeys[i]) {
							case NAME:
								// precondition that the names are the same
								break;
							case DESCRIPTION:
								report(String.format("%sDifference in Description(s) in T1 and T2", dTab));
								break;
							case NUM_ENTRIES:
								report(String.format("%sNumber of entries in T1: %d, T2: %d", dTab, cv1.size(), cv2.size()));
								break;
							case NUM_LANGUAGES:
								String ls1 = "";
								String ls2 = "";
								for (int k = 0; k < cv1.getNumberOfLanguages(); k++) {
									ls1 = ls1 + cv1.getLanguageId(k) + " ";
								}
								for (int k = 0; k < cv2.getNumberOfLanguages(); k++) {
									ls2 = ls2 + cv2.getLanguageId(k) + " ";
								}
								
								report(String.format("%sNumber of languages in T1: %d (\"%s\"), T2: %d (\"%s\")", dTab, 
										cv1.getNumberOfLanguages(), ls1, 
										cv2.getNumberOfLanguages(), ls2));
								break;
							case EXTERNAL_REF:
								String extRef1 = null;
								if (cv1 instanceof ExternalCV) {
									extRef1 = ((ExternalCV) cv1).getExternalRef().getValue();
								}
								String extRef2 = null;
								if (cv2 instanceof ExternalCV) {
									extRef2 = ((ExternalCV) cv2).getExternalRef().getValue();
								}
								
								report(String.format("%sCV in T1: \"%s\", in T2: \"%s\"", dTab,
										extRef1 == null ? "internal" : "external:" + extRef1,
										extRef2 == null ? "internal" : "external:" + extRef2));
								break;
							}
						}
					}
				}
			}
			if (numCVsWithDiffs > 0) {
				report(String.format("Found %d differences in %d of the %d CV's in T1 and T2.", 
						numPropDifferences, numCVsWithDiffs, dfList.size()));
			} else {
				report(String.format("Found no differences (ignoring CV entry contents) in the %d CV's both in T1 and T2.", dfList.size()));
			}
		}
		
		/**
		 * Reports differences in licenses in the two transcriptions.
		 * 
		 * @param trComp contains methods for the actual comparisons
		 * @param tr1 the first transcription
		 * @param tr2 the second transcription
		 */
		private void reportLicenseDifferences(TranscriptionCompare trComp, 
				TranscriptionImpl tr1, TranscriptionImpl tr2) {
			List<LicenseRecord> lr1 = tr1.getLicenses();
			if (lr1 == null) {
				lr1 = new ArrayList<LicenseRecord>(0);
			}
			List<LicenseRecord> lr2 = tr2.getLicenses();
			if (lr2 == null) {
				lr2 = new ArrayList<LicenseRecord>(0);
			}
			if (lr1.isEmpty() && lr2.isEmpty()) {
				report("No License information in either T1 or T2");//
			} else {
				int numLr1 = lr1.size();
				int numLr2 = lr2.size();
				int numLr1Only = 0;
				int numLr2Only = 0;
				
				for (LicenseRecord rec1 : lr1) {
					boolean found = false;
					for (LicenseRecord rec2 : lr2) {
						if (trComp.sameLicense(rec1, rec2)) {
							found = true;
							break;
						}
					}
					if (!found) {
						numLr1Only++;
					}
				}
				for (LicenseRecord rec2 : lr2) {
					boolean found = false;
					for (LicenseRecord rec1 : lr1) {
						if (trComp.sameLicense(rec2, rec1)) {
							found = true;
							break;
						}
					}
					if (!found) {
						numLr2Only++;
					}
				}
				if (numLr1Only == 0 && numLr2Only == 0) {
					report(String.format("Found %d identical License Records in T1 and T2", numLr1));
				} else {
					report(String.format("Found %d License Records in T1 (%d unique), %d in T2 (%d unique)", 
							numLr1, numLr1Only, numLr2, numLr2Only));
				}
			}
			
		}
		
		/**
		 * Reports differences in the Author property of two transcriptions.
		 * 
		 * @param tr1 the first transcription
		 * @param tr2 the second transcription
		 */
		private void reportAuthorDifferences(TranscriptionImpl tr1, TranscriptionImpl tr2) {
			String au1 = tr1.getAuthor() == null ? "" : tr1.getAuthor();
			String au2 = tr2.getAuthor() == null ? "" : tr2.getAuthor();
			
			if (au1.equals(au2)) {
				if (au1.isEmpty()) {
					report("No Author information in either T1 or T2");
				} else {
					report(String.format("Same Author in T1 and T2: \"%s\"", au1));
				}
			} else {
				report(String.format("Different Author in T1 (\"%s\") and T2 (\"%s\")", au1, au2));
			}
		}
		
		/**
		 * Sends the elements of the list to the report object.
		 * 
		 * @param heading a header string to output before the list
		 * @param dfList the list to output
		 */
		private void reportList(String heading, List<String> dfList) {
			report(heading);
			for (String s : dfList) {
				if (s != null && !s.isEmpty()) {
					report("\t" + s);
				}
			}
		}
	    // #### end reporting methods ####
		
		// #### updating methods      ####
		/**
		 * A top level update method which sequentially calls all sub update methods.
		 * 
		 * @param templTrans the template or source transcription
		 * @param trans the target transcription, the transcription to be updated
		 * @param mergeUtil utility object with the actual implementation of update methods
		 */
		private void updateDifferences(TranscriptionImpl templTrans, TranscriptionImpl trans, MergeUtil mergeUtil) {
			updateAuthor(templTrans, trans);
			updateLicense(templTrans, trans);
			updateCVDifferences(mergeUtil, templTrans, trans);
			updateTypeDifferences(mergeUtil, templTrans, trans);
			updateTierDifferences(mergeUtil, templTrans, trans);
		}
		
		/**
		 * Updates the Author field of the transcription.
		 * If the source's Author is null or empty, the target is not updated.
		 * 
		 * @param srcTrans the source transcription, the template
		 * @param targetTrans the transcription to update
		 */
		private void updateAuthor(TranscriptionImpl srcTrans, TranscriptionImpl targetTrans) {
			if (srcTrans.getAuthor() != null && !srcTrans.getAuthor().isEmpty()) {
				if (!srcTrans.getAuthor().equals(targetTrans.getAuthor())) {
					targetTrans.setAuthor(srcTrans.getAuthor());
					targetTrans.setChanged();
					report(String.format("Updated Author of T1 to \"%s\"", srcTrans.getAuthor()));
				}
			}// don't remove Author information from the target 
		}
		
		/**
		 * Updates license information, unless the source's license is null or empty.
		 * Since a transcription can hold multiple licenses, ambiguities can arise.
		 * Current approach: if the target does not have a license element yet, copy
		 * all licenses from source. If both transcriptions have exactly 1 license and
		 * there is a difference, copy the one license to target.
		 * 
		 * @param srcTrans the template file, the source
		 * @param targetTrans the file to update, the target
		 */
		private void updateLicense(TranscriptionImpl srcTrans, TranscriptionImpl targetTrans) {
			if (srcTrans.getLicenses() != null && !srcTrans.getLicenses().isEmpty()) {
				if (targetTrans.getLicenses() == null || targetTrans.getLicenses().isEmpty()) {
					// add
					targetTrans.setLicenses(srcTrans.getLicenses());// copy the list?
					targetTrans.setChanged();
					report(String.format("\tAdded %d License Records to T1", targetTrans.getLicenses().size()));
				} else if (targetTrans.getLicenses().size() == 1 && srcTrans.getLicenses().size() == 1) {
					// replace
					targetTrans.getLicenses().set(0, srcTrans.getLicenses().get(0));
					targetTrans.setChanged();
					report("\tUpdated the single License Record of T1");
				}
			}// does not remove licenses if the source has no license(s)
		}
		
		/**
		 * Updates existing and adds new controlled vocabularies to the target 
		 * transcription. Elements (entries, CV's) that are in the target but not in 
		 * the source are not removed.
		 *   
		 * @param mergeUtil utility providing methods that perform the actual update
		 * @param srcTrans the source transcription (template)
		 * @param targetTrans the target transcription
		 */
		private void updateCVDifferences(MergeUtil mergeUtil,
				TranscriptionImpl srcTrans, TranscriptionImpl targetTrans) {
			// change existing CV's first
			List<String> changedCVNames = changeMap.get(CH_CVS);
			if (changedCVNames != null && !changedCVNames.isEmpty()) {
				for (String s : changedCVNames) {
					mergeUtil.updateControlledVocabulary(srcTrans, targetTrans, s, 
							forceCVReplacement, reporter);
				}
			}
			// then add new CV's
			List<String> newCVNames = changeMap.get(NEW_CVS);
			if (newCVNames != null && !newCVNames.isEmpty()) {
				for (String s : newCVNames) {
					mergeUtil.updateControlledVocabulary(srcTrans, targetTrans, s, 
							forceCVReplacement, reporter);
				}
			}
		}
		
		/**
		 * Updates existing tier types in the target transcription and adds new types.
		 * Types that are in the target transcription but not in the source are not
		 * removed. The Stereotype of a type cannot be updated because it could effect 
		 * (the consistency of) annotations on existing tiers.
		 * 
		 * @param mergeUtil utility providing methods that perform the actual update
		 * @param srcTrans the source transcription (template)
		 * @param targetTrans the target transcription
		 */
		private void updateTypeDifferences(MergeUtil mergeUtil,
				TranscriptionImpl srcTrans, TranscriptionImpl targetTrans) {
			// add new types
			List<String> newTypesNames = changeMap.get(NEW_TYPES);
			if (newTypesNames != null && !newTypesNames.isEmpty()) {
				for (String s : newTypesNames) {
					mergeUtil.updateTierType(srcTrans, targetTrans, s, 
							reporter);
				}
			}
			// change existing types, where possible
			List<String> changedTypesNames = changeMap.get(CH_TYPES);
			if (changedTypesNames != null && !changedTypesNames.isEmpty()) {
				for (String s : changedTypesNames) {
					mergeUtil.updateTierType(srcTrans, targetTrans, s, 
							reporter);
				}
			}
		}
		
		/**
		 * Updates existing tiers in the target transcription and adds new tiers.
		 * Tiers that are in the target transcription but not in the source are not
		 * removed. Certain tier properties cannot be updated (the tier (stereo)type, 
		 * the parent tier) because it could lead to data loss (deleted annotations).
		 * 
		 * @param mergeUtil utility providing methods that perform the actual update
		 * @param srcTrans the source transcription (template)
		 * @param targetTrans the target transcription
		 */
		private void updateTierDifferences(MergeUtil mergeUtil,
				TranscriptionImpl srcTrans, TranscriptionImpl targetTrans) {
			// change existing tiers
			List<String> changedTiersNames = changeMap.get(CH_TIERS);
			if (changedTiersNames != null && !changedTiersNames.isEmpty()) {
				for (String s : changedTiersNames) {
					mergeUtil.updateTier(srcTrans, targetTrans, s, reporter);
				}
			}
			// add new tiers
			List<String> newTiersNames = changeMap.get(NEW_TIERS);
			if (newTiersNames != null && !newTiersNames.isEmpty()) {
				mergeUtil.updateWithNewTiers(srcTrans, targetTrans, newTiersNames, reporter);
			}

		}
		
	}
	
}
