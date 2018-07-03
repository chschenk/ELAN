package mpi.eudico.client.annotator.multiplefilesedit.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

public class StatisticsAnnotationsMF {
	public static final int NUM_ANN_COL = 9;
    /** formatter for average durations */
//    private DecimalFormat format = new DecimalFormat("#0.######",
//            new DecimalFormatSymbols(Locale.US));

    /** formatter for ss.ms values */
    private DecimalFormat format2 = new DecimalFormat("#0.0##",
            new DecimalFormatSymbols(Locale.US));
	public static final String EMPTY = "";
	
	private Map<String, AnStats> annotationStats;
	private String tierName;
	//public int numFiles;
	public int numAnnotations;
	public long minDur;
	public long maxDur;
	public long totalDur;
	public long latency;
	
	/**
	 * 
	 */
	public StatisticsAnnotationsMF(TierImpl tier) {
		super();
		tierName = tier.getName();
		annotationStats = new HashMap<String, AnStats>();
		addTier(tier);
	}
	
	public String getTierName() {
		return tierName;
	}
	
	public void addTier(TierImpl tier) {
		if (tier != null) {
			if (tierName == null) {
				tierName = tier.getName();
			} else if (!tierName.equals(tier.getName())) {
				return;
			}
			//numFiles++;
			numAnnotations += tier.getNumberOfAnnotations();
			extractAnnotations(tier);
		}
	}

	/**
	 * Extracts information of all annotations and creates or updates annotation statistics 
	 * 
	 * @param tier
	 */
	private void extractAnnotations(TierImpl tier) {
		if (tier == null) {
			return;
		}
		List<AbstractAnnotation> annotations = tier.getAnnotations();
		if (annotations.size() == 0) {
			return;
		}
		
        if ((annotations != null) && !annotations.isEmpty()) {

            for (AbstractAnnotation ann : annotations) {
                long bt = ann.getBeginTimeBoundary();
                long et = ann.getEndTimeBoundary();
                long dur = et - bt;
        		AnStats stats;
                
                if ((ann.getValue() != null) && (ann.getValue().length() > 0)) {
                    stats = annotationStats.get(ann.getValue());
                    if (stats == null) {
                    	stats = new AnStats(ann.getValue());
                    	annotationStats.put(ann.getValue(), stats);
                    	stats.minDur = dur;
                    	stats.maxDur = dur;
                    	stats.latency = bt;
                    }
                    
                } else {
                    stats = annotationStats.get(EMPTY);
                    if (stats == null) {
                    	stats = new AnStats(EMPTY);
                    	annotationStats.put(EMPTY, stats);
                    	stats.minDur = dur;
                    	stats.maxDur = dur;
                    	stats.latency = bt;
                    }                	
                }
                
                stats.numOccur++;
                stats.durations.add(dur);
                stats.totalDur += dur;
                if (dur < stats.minDur) {
                	stats.minDur = dur;
                }
                if (dur > stats.maxDur) {
                	stats.maxDur = dur;
                }
                if (bt < stats.latency) {
                	stats.latency = bt;
                }
            }
        }
		
	}
	
	/**
	 * Returns the statistics of all unique values in this particular tier.
	 * The columns are:<br>
	 * tier name ; annotation value ; num occurrences ; min duration ; max duration ; average duration ; median duration ; total duration ; latency
	 * @return
	 */
	public List<String[]> getAnnotationStastitics() {
		if (annotationStats.size() == 0) {
			String[] row0 = new String[NUM_ANN_COL];
			for (int i = 0; i < NUM_ANN_COL; i++) {
				if (i == 0) {
					row0[i] = tierName;
				} else {
					row0[i] = "-"; // or "0"?
				}
			}
			List<String[]> asList = new ArrayList<String[]>(1);
			asList.add(row0);
			return asList;
		}
		List<String[]> asList = new ArrayList<String[]>(annotationStats.size());
		
		List<String> keyList = new ArrayList<String>(annotationStats.keySet());
		Collections.sort(keyList);
		Iterator<String> keyIt = keyList.iterator();
		String key;
		AnStats stats;
		float mill = 1000f;
		
		while (keyIt.hasNext()) {
			key = keyIt.next();
			stats = annotationStats.get(key);
			String[] row = new String[NUM_ANN_COL];
			
			for (int i = 0; i < NUM_ANN_COL; i++) {
				switch(i) {
				case 0:
					row[i] = tierName;
					break;
				case 1:
					row[i] = key;// annotation value
					break;
				case 2:
					row[i] = String.valueOf(stats.numOccur);
					break;
				case 3:
					row[i] = format2.format(stats.minDur / mill);
					break;
				case 4:
					row[i] = format2.format(stats.maxDur / mill);
					break;
				case 5: // average
					float avg = stats.totalDur / (float) stats.numOccur;// num occur guaranteed non 0
					row[i] = format2.format(avg / mill);
					break;
				case 6: // median
					float median = 0f;
					int numDurs = stats.durations.size();
					if (numDurs == 1) {
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
					row[i] = format2.format(stats.latency / mill);
					break;
				default:
					row[i] = "-";
					break;
				}
			}
			/*
			row[0] = tierName;
			row[1] = key;
			row[2] = String.valueOf(stats.numOccur);
			row[3] = format2.format(stats.minDur / mill);
			row[4] = format2.format(stats.maxDur / mill);
			// average
			float avg = stats.totalDur / (float) stats.numOccur;
			row[5] = format2.format(avg / mill);
			// median
			float median = 0f;
			int numDurs = stats.durations.size();
			if (numDurs == 1) {
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
			row[6] = format2.format(median / mill); 
			row[7] = format2.format(stats.totalDur / mill);
			row[8] = format2.format(stats.latency / mill);
			*/
			asList.add(row);
		}
		
		return asList;
	}
	
	class AnStats {
		// average duration and median duration at end of processing
		private String value;
		//public Set<Long> durations;
		public List<Long> durations;// should every individual duration be in the list or only the unique durations?
		//public int numFiles;//??
		public int numOccur;
		public long minDur;
		public long maxDur;
		public long totalDur;
		public long latency;

		/**
		 * @param value
		 */
		public AnStats(String value) {
			super();
			this.value = value;
			//durations = new TreeSet<Long>();//unique values
			durations = new ArrayList<Long>();
		}
		
		public String getValue() {
			return value;
		}
	}
}
