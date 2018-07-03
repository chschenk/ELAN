package mpi.eudico.client.annotator.multiplefilesedit.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * A class in which statistics for annotations, tiers, types, participants and annotators
 * for multiple files are collected.
 * 
 * @author Han Sloetjes
 *
 */
public class StatisticsCollectionMF {
	public final static String UNSPECIFIED = "unspecified";
	public static final int NUM_TIER_COL = 9;
	public static final int NUM_TYPE_COL = 10;
	public static final int NUM_PART_COL = 10;// reused for annotator and language
    /** formatter for average durations */
//    private DecimalFormat format = new DecimalFormat("#0.######",
//            new DecimalFormatSymbols(Locale.US));

    /** formatter for ss.ms values */
    private DecimalFormat format2 = new DecimalFormat("#0.0##",
            new DecimalFormatSymbols(Locale.US));
    
	private List<TierStats> tierStatsMF;
	private List<TierAttributeBasedStats> typeStatsMF;
	private List<TierAttributeBasedStats> partStatsMF;
	private List<TierAttributeBasedStats> annotatorStatsMF;
	private List<TierAttributeBasedStats> languageStatsMF;
	private Map<String, StatisticsAnnotationsMF> annotationsStatsMF;
	
	/**
	 * Constructor
	 */
	public StatisticsCollectionMF() {
		super();
		tierStatsMF = new ArrayList<TierStats>();
		typeStatsMF = new ArrayList<TierAttributeBasedStats>();
		partStatsMF = new ArrayList<TierAttributeBasedStats>();
		annotatorStatsMF = new ArrayList<TierAttributeBasedStats>();
		languageStatsMF = new ArrayList<TierAttributeBasedStats>();
		annotationsStatsMF = new TreeMap<String, StatisticsAnnotationsMF>();
	}
	
	/**
	 * 
	 * @param file
	 * @param tier
	 * @param numAnnotations
	 * @param minDur
	 * @param maxDur
	 * @param totalDur
	 * @param latency i.e. the onset or first occurrence
	 */
	public void addTier(String file, TierImpl tier, int numAnnotations, long minDur, 
			long maxDur, long totalDur, long latency, List<Long> curDurations) {
		if (tier == null) {
			return;
		}
		// tier
		boolean tierFound = false;
		for (TierStats ts : tierStatsMF) {
			if (ts.getTierName().equals(tier.getName())) {
				tierFound = true;
				ts.numFiles++;
				ts.numAnnotations += numAnnotations;
				ts.durations.addAll(curDurations);
				ts.totalDur += totalDur;
				if (minDur < ts.minDur) {
					ts.minDur = minDur;
				}
				if (maxDur > ts.maxDur) {
					ts.maxDur = maxDur;
				}
				if (latency < ts.latency) {
					ts.latency = latency;
				}
				break;
			}
		}
		if (!tierFound) {
			TierStats ts = new TierStats(tier.getName());
			ts.numFiles = 1;
			ts.numAnnotations = numAnnotations;
			ts.durations.addAll(curDurations);
			ts.minDur = minDur;
			ts.maxDur = maxDur;
			ts.totalDur = totalDur;
			ts.latency = latency;
			tierStatsMF.add(ts);
		}
		// type
		boolean typeFound = false;
		String typeName = tier.getLinguisticType().getLinguisticTypeName();
		for (TierAttributeBasedStats tys : typeStatsMF) {
			if (tys.getAttributeValue().equals(typeName)) {
				typeFound = true;
				tys.numTiers++;
				tys.addFileName(file);
				tys.addTierName(tier.getName());
				tys.numAnnotations += numAnnotations;
				tys.durations.addAll(curDurations);
				tys.totalDur += totalDur;
				if (minDur < tys.minDur) {
					tys.minDur = minDur;
				}
				if (maxDur > tys.maxDur) {
					tys.maxDur = maxDur;
				}
				if (latency < tys.latency) {
					tys.latency = latency;
				}
				break;
			}
		}
		if (!typeFound) {
			TierAttributeBasedStats tys = new TierAttributeBasedStats(typeName);
			tys.numTiers = 1;
			tys.addFileName(file);
			tys.addTierName(tier.getName());
			tys.numAnnotations = numAnnotations;
			tys.durations.addAll(curDurations);
			tys.minDur = minDur;
			tys.maxDur = maxDur;
			tys.totalDur = totalDur;
			tys.latency = latency;
			typeStatsMF.add(tys);
		}
		// participant
		boolean partFound = false;
		String partName = tier.getParticipant();
		if (partName == null || partName.length() == 0) {
			partName = UNSPECIFIED;
		}
		for(TierAttributeBasedStats ps : partStatsMF) {
			if (ps.getAttributeValue().equals(partName)) {
				partFound = true;
				ps.numTiers++;
				ps.addFileName(file);
				ps.addTierName(tier.getName());
				ps.numAnnotations += numAnnotations;
				ps.durations.addAll(curDurations);
				ps.totalDur += totalDur;
				if (minDur < ps.minDur) {
					ps.minDur = minDur;
				}
				if (maxDur > ps.maxDur) {
					ps.maxDur = maxDur;
				}
				if (latency < ps.latency) {
					ps.latency = latency;
				}
				break;
			}
		}
		if (!partFound) {
			TierAttributeBasedStats ps = new TierAttributeBasedStats(partName);
			ps.numTiers = 1;
			ps.addFileName(file);
			ps.addTierName(tier.getName());
			ps.numAnnotations = numAnnotations;
			ps.durations.addAll(curDurations);
			ps.minDur = minDur;
			ps.maxDur = maxDur;
			ps.totalDur = totalDur;
			ps.latency = latency;
			partStatsMF.add(ps);
		}
		// annotator
		boolean annotFound = false;
		String annotName = tier.getAnnotator();
		if (annotName == null || annotName.length() == 0) {
			annotName = UNSPECIFIED;
		}
		for (TierAttributeBasedStats as : annotatorStatsMF) {
			if (as.getAttributeValue().equals(annotName)) {
				annotFound = true;
				as.numTiers++;
				as.addFileName(file);
				as.addTierName(tier.getName());
				as.numAnnotations += numAnnotations;
				as.durations.addAll(curDurations);
				as.totalDur += totalDur;
				if (minDur < as.minDur) {
					as.minDur = minDur;
				}
				if (maxDur > as.maxDur) {
					as.maxDur = maxDur;
				}
				if (latency < as.latency) {
					as.latency = latency;
				}
				break;
			}
		}
		if (!annotFound) {
			TierAttributeBasedStats as = new TierAttributeBasedStats(annotName);
			as.numTiers = 1;
			as.addFileName(file);
			as.addTierName(tier.getName());
			as.numAnnotations = numAnnotations;
			as.durations.addAll(curDurations);
			as.minDur = minDur;
			as.maxDur = maxDur;
			as.totalDur = totalDur;
			as.latency = latency;
			annotatorStatsMF.add(as);
		}
		// content language
		boolean langFound = false;
		String langName = tier.getLangRef();
		if (langName == null || langName.length() == 0) {
			langName = UNSPECIFIED;
		}
		for (TierAttributeBasedStats tas : languageStatsMF) {
			if (tas.getAttributeValue().equals(langName)) {
				langFound = true;
				tas.numTiers++;
				tas.addFileName(file);
				tas.addTierName(tier.getName());
				tas.numAnnotations += numAnnotations;
				tas.durations.addAll(curDurations);
				tas.totalDur += totalDur;
				if (minDur < tas.minDur) {
					tas.minDur = minDur;
				}
				if (maxDur > tas.maxDur) {
					tas.maxDur = maxDur;
				}
				if (latency < tas.latency) {
					tas.latency = latency;
				}
				break;
			}
		}
		if (!langFound) {
			TierAttributeBasedStats tas = new TierAttributeBasedStats(langName);
			tas.numTiers = 1;
			tas.addFileName(file);
			tas.addTierName(tier.getName());
			tas.numAnnotations = numAnnotations;
			tas.durations.addAll(curDurations);
			tas.minDur = minDur;
			tas.maxDur = maxDur;
			tas.totalDur = totalDur;
			tas.latency = latency;
			languageStatsMF.add(tas);
		}
	}
	
	/**
	 * If statistics of all tiers and types etc. need to be created, this is a 
	 * shortcut to add a linguistic type found in a file but not used by any
	 * tier.
	 * 
	 * @param file the file path
	 * @param typeName the name of the type
	 */
	public void addEmptyLinguisticType(String file, String typeName) {
		if (typeName == null) {
			return;
		}
		// type
		boolean typeFound = false;

		for (TierAttributeBasedStats tys : typeStatsMF) {
			if (tys.getAttributeValue().equals(typeName)) {
				typeFound = true;
				tys.addFileName(file);
				break;
			}
		}
		if (!typeFound) {
			TierAttributeBasedStats tys = new TierAttributeBasedStats(typeName);
			tys.numTiers = 0;
			tys.addFileName(file);
			tys.numAnnotations = 0;
			tys.minDur = 0;
			tys.maxDur = 0;
			tys.totalDur = 0;
			tys.latency = 0;
			typeStatsMF.add(tys);
		}
	}
	
	/**
	 * 
	 * @param file currently ignored, can be used at some time if per file statistics is needed 
	 * @param tierName the name of the tiers, a key in a map  
	 * @param annotationStatistics an object containing (intermediate) statistics of annotations 
	 * on tiers with the same name in multiple files
	 */
	public void addAnnotations(String file, String tierName, StatisticsAnnotationsMF annotationStatistics) {
		if (annotationsStatsMF.containsKey(tierName)) {
			// error? a tierName - stats mapping should be added only once
		} else {
			annotationsStatsMF.put(tierName, annotationStatistics);
		}
	}
	
	/**
	 * Returns the annotation statistics object for that tier or null
	 * 
	 * @param tierName the name of the tiers
	 * @return a statistics object or null
	 */
	public StatisticsAnnotationsMF getAnnotationStats(String tierName) {
		return annotationsStatsMF.get(tierName);
	}
	
	public List<String[]> getAllAnnotationStatistics() {
		if (annotationsStatsMF.size() == 0) {
			String[] row = new String[StatisticsAnnotationsMF.NUM_ANN_COL];
			for (int i = 0; i < row.length; i++) {
				row[i] = "-"; // or "0"?
			}
			List<String[]> ansList = new ArrayList<String[]>(1);
			ansList.add(row);
			return ansList;
		}
		List<String[]> annsList = new ArrayList<String[]>();
		StatisticsAnnotationsMF stats;
		Iterator<String> keyIt = annotationsStatsMF.keySet().iterator();
		while (keyIt.hasNext()) {
			stats = annotationsStatsMF.get(keyIt.next());
			annsList.addAll(stats.getAnnotationStastitics());
		}
		
		return annsList;
	}
	
	/**
	 * Returns the statistics per tier, in the order the tiers have been added.
	 * (Sorting could be added). <br>
	 * The columns contain the following:<br>
	 * tier name ; number of files the tier is in ; number of annotations ; min duration ; max duration ; average duration ; median duration ; total duration ; latency 
	 * @return a list of tier table rows
	 */
	public List<String[]> getTierStatistics() {
		if (tierStatsMF.size() == 0) {
			String[] row = new String[NUM_TIER_COL];
			for (int i = 0; i < NUM_TIER_COL; i++) {
				row[i] = "-"; // or "0"?
			}
			List<String[]> tsList = new ArrayList<String[]>(1);
			tsList.add(row);
			return tsList;
		}
		List<String[]> tierList = new ArrayList<String[]>(tierStatsMF.size());
		// could sort the list tier name alphabetically here
		
		float mill = 1000f;
		
		for (TierStats stats : tierStatsMF) {
			String[] row = new String[NUM_TIER_COL];
			
			for (int i = 0; i < NUM_TIER_COL; i++) {
				switch(i) {
				case 0: 
					row[i] = stats.getTierName();
					break;
				case 1: 
					row[i] = String.valueOf(stats.numFiles);
					break;
				case 2:
					row[i] = String.valueOf(stats.numAnnotations);
					break;
				case 3:
					//row[i] = format2.format(stats.minDur / mill);
					if (stats.minDur == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.minDur / mill);
					}
					break;
				case 4:
					row[i] = format2.format(stats.maxDur / mill);
					break;
				case 5: // average
					if (stats.numAnnotations == 0) {
						row[i] = format2.format(0);
					} else {
						float avg = stats.totalDur / (float) stats.numAnnotations;
						row[i] = format2.format(avg / mill);
					}
					break;
				case 6: //median
					float median = 0f;
					int numDurs = stats.durations.size();
					if (numDurs == 0) {
						median = 0;
					} else if (numDurs == 1) {
						median = stats.durations.get(0);
					} else {
						Collections.sort(stats.durations);
						if ((numDurs % 2) != 0) {
							// in case of an odd number, take the middle value
							median = stats.durations.get(numDurs / 2);
						} else {
			                // in case of an even number, calculate the average of the 
			                // two middle values
			                long h = stats.durations.get(numDurs / 2);
			                long l = stats.durations.get((numDurs / 2) - 1);
			                median = (h + l) / 2;
						}
					}
					row[i] = format2.format(median / mill);
					break;
				case 7: 
					row[i] = format2.format(stats.totalDur / mill);
					break;
				case 8:
					//row[i] = format2.format(stats.latency / mill);
					if (stats.latency == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.latency / mill);
					}
					break;
				default:
					row[i] = "-";
					break;
				}
			}
			
			tierList.add(row);
		}
		
		return tierList;
	}
	
	/**
	 * Returns the statistics per linguistic type, in the order the types have been added.
	 * (Sorting could be added). <br>
	 * The columns contain the following:<br>
	 * type name ; number of files ; number of tiers using the type ; number of annotations ; min duration ; max duration ; average duration ; median duration ; total duration ; latency 
	 * @return a list of type table rows
	 */
	public List<String[]> getTypeStatistics() {
		return getTierAttributeBasedStatistics(typeStatsMF);
		/*
		if (typeStatsMF.size() == 0) {
			String[] row = new String[NUM_TYPE_COL];
			for (int i = 0; i < NUM_TYPE_COL; i++) {
				row[i] = "-"; // or "0"?
			}
			List<String[]> typeList = new ArrayList<String[]>(1);
			typeList.add(row);
			return typeList;
		}
		List<String[]> typeList = new ArrayList<String[]>(typeStatsMF.size());
		// could sort the list type name alphabetically here
		
		float mill = 1000f;
		
		for (TypeStats stats : typeStatsMF) {
			String[] row = new String[NUM_TYPE_COL];
			
			for (int i = 0; i < NUM_TYPE_COL; i++) {
				switch(i) {
				case 0: 
					row[i] = stats.getTypeName();
					break;
				case 1: 
					row[i] = String.valueOf(stats.getNumFiles());
					break;
				case 2: 
					//row[i] = String.valueOf(stats.getNumUniqueTiers());
					row[i] = String.valueOf(stats.numTiers);
					break;
				case 3:
					row[i] = String.valueOf(stats.numAnnotations);
					break;
				case 4:
					//row[i] = format2.format(stats.minDur / mill);
					if (stats.minDur == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.minDur / mill);
					}
					break;
				case 5:
					row[i] = format2.format(stats.maxDur / mill);
					break;
				case 6: // average
					if (stats.numAnnotations == 0) {
						row[i] = format2.format(0);
					} else {
						float avg = stats.totalDur / (float) stats.numAnnotations;
						row[i] = format2.format(avg / mill);
					}
					break;
				case 7: // median
					float median = 0f;
					int numDurs = stats.durations.size();
					if (numDurs == 0) {
						median = 0;
					} else if (numDurs == 1) {
						median = stats.durations.get(0);
					} else {
						Collections.sort(stats.durations);
						if ((numDurs % 2) != 0) {
							// in case of an odd number, take the middle value
							median = stats.durations.get(numDurs / 2);
						} else {
			                // in case of an even number, calculate the average of the 
			                // two middle values
			                long h = stats.durations.get(numDurs / 2);
			                long l = stats.durations.get((numDurs / 2) - 1);
			                median = (h + l) / 2;
						}
					}
					row[i] = format2.format(median / mill);
					break;
				case 8: 
					row[i] = format2.format(stats.totalDur / mill);
					break;
				case 9:
					//row[i] = format2.format(stats.latency / mill);
					if (stats.latency == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.latency / mill);
					}
					break;
				default:
					row[i] = "-";
					break;
				}
			}
			
			typeList.add(row);
		}
		
		return typeList;
		*/
	}
	
	/**
	 * Returns the statistics per participant, in the order the participants have been added.
	 * NOTE: only the participants of the selected/added tiers are in the list
	 * (Sorting could be added). <br>
	 * The columns contain the following:<br>
	 * participant name ; number of files ; number of tiers for participant ; number of annotations ; min duration ; max duration ; average duration ; median duration ; total duration ; latency 
	 * @return a list of participant table rows
	 */
	public List<String[]> getPartStatistics() {
		return getTierAttributeBasedStatistics(partStatsMF);
		/*
		if (partStatsMF.size() == 0) {
			String[] row = new String[NUM_PART_COL];
			for (int i = 0; i < NUM_PART_COL; i++) {
				row[i] = "-"; // or "0"?
			}
			List<String[]> partList = new ArrayList<String[]>(1);
			partList.add(row);
			return partList;
		}
		
		List<String[]> partList = new ArrayList<String[]>(partStatsMF.size());
		
		float mill = 1000f;
		
		for (ParticipantStats stats : partStatsMF) {
			String[] row = new String[NUM_PART_COL];
			
			for (int i = 0; i < NUM_PART_COL; i++) {
				switch(i) {
				case 0: 
					row[i] = stats.getPartName();
					break;
				case 1: 
					row[i] = String.valueOf(stats.getNumFiles());
					break;
				case 2: 
					//row[i] = String.valueOf(stats.getNumUniqueTiers());
					row[i] = String.valueOf(stats.numTiers);
					break;
				case 3:
					row[i] = String.valueOf(stats.numAnnotations);
					break;
				case 4:
					if (stats.minDur == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.minDur / mill);
					}
					break;
				case 5:
					row[i] = format2.format(stats.maxDur / mill);
					break;
				case 6: // average
					if (stats.numAnnotations == 0) {
						row[i] = format2.format(0);
					} else {
						float avg = stats.totalDur / (float) stats.numAnnotations;
						row[i] = format2.format(avg / mill);
					}
					break;
				case 7: // median
					float median = 0f;
					int numDurs = stats.durations.size();
					
			        if (numDurs == 0) {
	                    //median = 0;
	                } else if (numDurs == 1) {
						median = stats.durations.get(0);
					} else {
						Collections.sort(stats.durations);
						if ((numDurs % 2) != 0) {
							// in case of an odd number, take the middle value
							median = stats.durations.get(numDurs / 2);
						} else {
			                // in case of an even number, calculate the average of the 
			                // two middle values
			                long h = stats.durations.get(numDurs / 2);
			                long l = stats.durations.get((numDurs / 2) - 1);
			                median = (h + l) / 2;
						}
					}
					row[i] = format2.format(median / mill);
					break;
				case 8: 
					row[i] = format2.format(stats.totalDur / mill);
					break;
				case 9:
					if (stats.latency == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.latency / mill);
					}
					break;
				default:
					row[i] = "-";
					break;
				}
			}
			
			partList.add(row);
		}
		
		return partList;
		*/
	}
	
	/**
	 * Returns the statistics per annotator, in the order the annotators have been added.
	 * NOTE: only the annotators of the selected/added tiers are in the list
	 * (Sorting could be added). <br>
	 * The columns contain the following:<br>
	 * annotator name ; number of files ; number of tiers for annotator ; number of annotations ; min duration ; max duration ; average duration ; median duration ; total duration ; latency 
	 * @return a list of annotator table rows
	 */
	public List<String[]> getAnnotatorStatistics() {
		return getTierAttributeBasedStatistics(annotatorStatsMF);
		/*
		if (annotatorStatsMF.size() == 0) {
			String[] row = new String[NUM_PART_COL];
			for (int i = 0; i < NUM_PART_COL; i++) {
				row[i] = "-"; // or "0"?
			}
			List<String[]> annotList = new ArrayList<String[]>(1);
			annotList.add(row);
			return annotList;
		}
		
		List<String[]> annotList = new ArrayList<String[]>(annotatorStatsMF.size());
		
		float mill = 1000f;
		
		for (TierAttributeBasedStats stats : annotatorStatsMF) {
			String[] row = new String[NUM_PART_COL];
			
			for (int i = 0; i < NUM_PART_COL; i++) {
				switch(i) {
				case 0: 
					row[i] = stats.getAttributeValue();
					break;
				case 1: 
					row[i] = String.valueOf(stats.getNumFiles());
					break;
				case 2: 
					//row[i] = String.valueOf(stats.getNumUniqueTiers());
					row[i] = String.valueOf(stats.numTiers);
					break;
				case 3:
					row[i] = String.valueOf(stats.numAnnotations);
					break;
				case 4:
					if (stats.minDur == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.minDur / mill);
					}
					break;
				case 5:
					row[i] = format2.format(stats.maxDur / mill);
					break;
				case 6: // average
					if (stats.numAnnotations == 0) {
						row[i] = format2.format(0);
					} else {
						float avg = stats.totalDur / (float) stats.numAnnotations;
						row[i] = format2.format(avg / mill);
					}
					break;
				case 7: // median
					float median = 0f;
					int numDurs = stats.durations.size();
					if (numDurs == 0) {
						median = 0;
					} else if (numDurs == 1) {
						median = stats.durations.get(0);
					} else {
						Collections.sort(stats.durations);
						if ((numDurs % 2) != 0) {
							// in case of an odd number, take the middle value
							median = stats.durations.get(numDurs / 2);
						} else {
			                // in case of an even number, calculate the average of the 
			                // two middle values
			                long h = stats.durations.get(numDurs / 2);
			                long l = stats.durations.get((numDurs / 2) - 1);
			                median = (h + l) / 2;
						}
					}
					row[i] = format2.format(median / mill);
					break;
				case 8: 
					row[i] = format2.format(stats.totalDur / mill);
					break;
				case 9:
					if (stats.latency == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.latency / mill);
					}
					break;
				default:
					row[i] = "-";
					break;
				}
			}
			
			annotList.add(row);
		}
		
		return annotList;
		*/
	}
	
	/**
	 * Returns the language statistics
	 * 
	 * @return the table rows based on the content language attribute of the tiers.
	 */
	public List<String[]> getLanguageStatistics() {
		return getTierAttributeBasedStatistics(languageStatsMF);
	}
	
	/**
	 * Returns the statistics per attribute value, in the order they have been added.
	 * NOTE: only the attributes of the selected/added tiers are in the list
	 * (Sorting could be added to the table). <br>
	 * The columns contain the following:<br>
	 * attribute name (or value actually) ; number of files ; number of tiers for the attribute; number of annotations ; min duration ; max duration ; average duration ; median duration ; total duration ; latency 

	 * @param attribStatsMF the statistics for a specific tier attribute (annotator, participant, type, language)
	 * @return a list of table rows for the specified attribute  
	 */
	private List<String[]> getTierAttributeBasedStatistics(List<TierAttributeBasedStats> attribStatsMF) {
		List<String[]> attribList;
		if (attribStatsMF == null || attribStatsMF.size() == 0) {
			attribList = new ArrayList<String[]>(1);
			String[] row = new String[NUM_PART_COL];
			for (int i = 0; i < NUM_PART_COL; i++) {
				row[i] = "-"; // or "0"?
			}
			attribList.add(row);
			return attribList;
		} else {
			attribList = new ArrayList<String[]>(attribStatsMF.size());
		}

		float mill = 1000f;
		
		for (TierAttributeBasedStats stats : attribStatsMF) {
			String[] row = new String[NUM_PART_COL];
			
			for (int i = 0; i < NUM_PART_COL; i++) {
				switch(i) {
				case 0: 
					row[i] = stats.getAttributeValue();
					break;
				case 1: 
					row[i] = String.valueOf(stats.getNumFiles());
					break;
				case 2: 
					//row[i] = String.valueOf(stats.getNumUniqueTiers());
					row[i] = String.valueOf(stats.numTiers);
					break;
				case 3:
					row[i] = String.valueOf(stats.numAnnotations);
					break;
				case 4:
					if (stats.minDur == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.minDur / mill);
					}
					break;
				case 5:
					row[i] = format2.format(stats.maxDur / mill);
					break;
				case 6: // average
					if (stats.numAnnotations == 0) {
						row[i] = format2.format(0);
					} else {
						float avg = stats.totalDur / (float) stats.numAnnotations;
						row[i] = format2.format(avg / mill);
					}
					break;
				case 7: // median
					float median = 0f;
					int numDurs = stats.durations.size();
					if (numDurs == 0) {
						median = 0;
					} else if (numDurs == 1) {
						median = stats.durations.get(0);
					} else {
						Collections.sort(stats.durations);
						if ((numDurs % 2) != 0) {
							// in case of an odd number, take the middle value
							median = stats.durations.get(numDurs / 2);
						} else {
			                // in case of an even number, calculate the average of the 
			                // two middle values
			                long h = stats.durations.get(numDurs / 2);
			                long l = stats.durations.get((numDurs / 2) - 1);
			                median = (h + l) / 2;
						}
					}
					row[i] = format2.format(median / mill);
					break;
				case 8: 
					row[i] = format2.format(stats.totalDur / mill);
					break;
				case 9:
					if (stats.latency == Long.MAX_VALUE) {
						row[i] = format2.format(0);
					} else {
						row[i] = format2.format(stats.latency / mill);
					}
					break;
				default:
					row[i] = "-";
					break;
				}
			}
			
			attribList.add(row);
		}
		
		return attribList;	
	}
	
}
