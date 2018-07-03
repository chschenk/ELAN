package mpi.eudico.client.util;

import mpi.eudico.client.annotator.util.ClientLogger;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import mpi.eudico.util.TimeRelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

/**
 * Class that creates a sequence of data units for subtitles. The units consist
 * of a text value and a begin and end time, where the  end time can be either
 * the real end time or a calculated end time based on a minimum duration.<br>
 * Overlaps are handled by splitting into multiple units.
 *
 * @author Han Sloetjes
 */
public class SubtitleSequencer implements ClientLogger {
	/**
	 * If we're strict we avoid only overlap with a group of
	 * subtitles with the exact same end time. 
	 * But we allow a bit of fuzz.
	 */
    private static final int SAME_ENDTIME_FUZZ = 5;
    /**
     * We avoid overlap only when the overlap is
     * shorter than this number of milliseconds.
     * If it is longer it is handled by the normal mechanism.
     */
	private static final int MAX_OVERLAP = 200;

	/**
     * Creates a new SubtitleSequencer instance
     */
    public SubtitleSequencer() {
        super();
    }

    /**
     * Creates a list of subtitle objects, including all annotations of the
     * specified  tiers and applying a minimal duration.
     *
     * @param transcription the transcription document
     * @param tierNames the tiers to include
     * @param intervalBegin the selection begintime
     * @param intervalEnd the selection end time
     * @param minimalDuration the minimal duration per subtitle
     * @param offset the number of ms. to add to all time values
     * @param resolveOverlaps detects overlapping units and creates new,
     *        merging units for the overlaps
     *
     * @return a list of subtitle objects
     *
     * @throws NullPointerException if the {@link Transcription} or the list of
     *         tiernames is null
     * @throws IllegalArgumentException if the size of the list of tier names
     *         is 0
     */
    public List<SubtitleUnit> createSequence(Transcription transcription, List<String> tierNames,
        long intervalBegin, long intervalEnd, int minimalDuration, long offset,
        boolean resolveOverlaps) {
        if (transcription == null) {
            throw new NullPointerException("The transcription is null");
        }

        if (tierNames == null) {
            throw new NullPointerException("The list of tier names is null");
        }

        if (tierNames.size() == 0) {
            throw new IllegalArgumentException("No tiers have been specified");
        }

        Stack<SubtitleUnit> units = new Stack<SubtitleUnit>();

        for (int i = 0; i < tierNames.size(); i++) {
            String name = tierNames.get(i);
            TierImpl tier = (TierImpl) transcription.getTierWithId(name);

            if (tier == null) {
                LOG.warning("The tier does not exist: " + name);
                continue;
            }

            List<AbstractAnnotation> annotations = tier.getAnnotations();
            SubtitleUnit cursub = null;
            SubtitleUnit prevsub = null;

            for (int j = 0; j < annotations.size(); j++) {
            	AbstractAnnotation ann = annotations.get(j);

                if ((ann != null) &&
                        TimeRelation.overlaps(ann, intervalBegin, intervalEnd)) {
                    cursub = new SubtitleUnit(ann.getBeginTimeBoundary() +
                            offset, ann.getEndTimeBoundary() + offset, i, 
                            ann.getValue());

                    // If the preceding unit is below minimum length:
                    if ((prevsub != null) && ((prevsub.getCalcEnd() - 
                         prevsub.getBegin()) < minimalDuration)) {

                        if (resolveOverlaps && prevsub.getBegin() 
                        		+ minimalDuration > cursub.getBegin()) {
                        	// HS: check if there is overlap
                            // Merge the preceding annotation and this
                            // annotation into a single unit, extend the
                            // end time, then move on.
                            prevsub.setValue(prevsub.getValues()[0] + " " + 
                                cursub.getValues()[0]);
                            prevsub.setCalcEnd(cursub.getCalcEnd());
                            
                            continue;
                        } else {
                            // Increase the end time of the preceding annota-
                            // tion to the minimum duration (or as close as
                            //  we can get without merging with the next
                            // annotation on this tier).
                            prevsub.setCalcEnd(Math.min(cursub.getBegin(), 
                                prevsub.getBegin() + minimalDuration));
                        }
                    }

                    units.add(cursub);
                    prevsub = cursub;
                }
            }
            
            // Handle the last subtitle 
            if ((prevsub != null) && 
            		((prevsub.getCalcEnd() - prevsub.getBegin()) < minimalDuration)) {
                prevsub.setCalcEnd(Math.min(intervalEnd, 
                        prevsub.getBegin() + minimalDuration));            	
            }
        }

        if (!resolveOverlaps || units.size() < 2) {
            Collections.sort(units);
            return units;
        }

        // all units have been added, sort first
        Collections.sort(units, Collections.reverseOrder());

        ArrayList<SubtitleUnit> output = new ArrayList<SubtitleUnit>();
        ArrayList<SubtitleUnit> group = new ArrayList<SubtitleUnit>();

        do {
            // Read in a group of annotations that have the same start time.
            long first_start;
            
            /*
             * Get at least one subtitle unit into the group.
             * They will all have the same start time.
             * The first iteration:  agroup is empty but units isn't (checked above).
             * Later iterations: if agroup is empty then units isn't (checked in loop condition).
             */
            if (group.isEmpty()) {
            	SubtitleUnit sub = units.peek();
	            first_start = sub.getBegin();
	            group.add(units.pop());
            } else {
            	first_start = group.get(0).getBegin();
            }
            
            long second_start = Long.MAX_VALUE;
            /*
             * Get more units into the group, as long as they have the same start time.
             * For the first one that doesn't qualify, remember its time (second_start).
             */
            while (units.size() > 0) {
            	SubtitleUnit sub = units.peek();
                long next_start = sub.getBegin();
                
                if (next_start != first_start) {
                    second_start = next_start;
                    break;
                }
                group.add(units.pop());
            }
            
            // Calculate the earliest ending time of the current group.
            long first_end = Long.MAX_VALUE;
            long last_end = 0;
            for (SubtitleUnit sub : group) {
            	first_end = Math.min(first_end, sub.getCalcEnd());
            	last_end = Math.max(last_end, sub.getCalcEnd());
            }

            // Determine where the unit should end.
            long min_end = Math.min(first_end, second_start);
            long end_difference = last_end - first_end;

			/*
			 * If units get too short due to resolving overlaps, "do something".
			 * However, this is very complicated in general so handle only a
			 * simple situation.
			 * 
			 * Firstly we want that the overlaps we want to prevent are much
			 * shorter than the subtitles themselves. In other words, minimal
			 * overlap duration is much shorter than the minimal length of a
			 * subtitle in general, otherwise we can't shorten any times
			 * anywhere to remove the overlap.
			 * 
			 * Secondly we want that there is basically only one current
			 * subtitle[1], and the next one is starting just a bit early. In
			 * that case, we end the first subtitle a bit early, and begin the
			 * next one a bit late.
			 * 
             * |--------------------------|
             * |-------------------------|
             * ^                      |-------------------------|
             * |                      ^  ^^- last_end
             * +- first_start         |  +- first_end
             *                        +- second_start == min_end
             * 
             * [1] several ones with the exact same end time is fine too.
             * And in fact we can look at it fuzzily and pretend a few ms
             * difference doesn't matter.
             */
        	final long maxOverlapDuration = Math.min(MAX_OVERLAP, minimalDuration / 2);
            if (end_difference <= SAME_ENDTIME_FUZZ &&
            		second_start < last_end &&
            		first_end - second_start < maxOverlapDuration) {

        		long half_overlap = (first_end - second_start) / 2;
        		min_end += half_overlap;
        		decreaseEndTime(min_end, group);
        		increaseBeginTime(min_end, units);
            } else if (end_difference > 0 && end_difference <= SAME_ENDTIME_FUZZ) {
            	/*
            	 * This isn't strictly about overlap but about non-alignment.
            	 */
        		decreaseEndTime(first_end, group);
            }
            
            // Start preparing the contents for a new subtitle unit.
            // Sort the units according to the tier order.
            if (group.size() > 1) {
            	// Sort by line index, to get subtitles in the tier order
            	// as indicated.
            	Collections.sort(group, new Comparator<SubtitleUnit>() {
					@Override
					public int compare(SubtitleUnit lhs, SubtitleUnit rhs) {
						return lhs.getLineIndex() - rhs.getLineIndex();
					}});
            }
            
            // Get the string values
            ArrayList<String> values = new ArrayList<String>();

            for (SubtitleUnit sub : group) {
            	String[] vals = sub.getValues();
            	for (String s : vals) {
            		values.add(s);
            	}
            }

            SubtitleUnit out = new SubtitleUnit(first_start, min_end, null);
            out.setValues(values.toArray(new String[0]));
            output.add(out);

            // Now adjust the start times for each of the items just output,
            // removing any annotations that no longer have any run time.
            for (int i = group.size() - 1; i >= 0;  i--) {
            	SubtitleUnit sub = group.get(i);
                sub.setBegin(min_end);

                if (min_end >= sub.getCalcEnd()) {
                    group.remove(sub);
                }
            }
        } while (! (group.isEmpty() && units.isEmpty()));

        return output;
    }

    void increaseBeginTime(long new_time, Collection<SubtitleUnit> units) {
    	for (SubtitleUnit sub: units) {
    		if (sub.getBegin() < new_time) {
    			sub.setBegin(new_time);
    		}
    	}
    }
    
    void decreaseEndTime(long new_time, Collection<SubtitleUnit> units) {
    	for (SubtitleUnit sub: units) {
    		if (sub.getCalcEnd() > new_time) {
    			sub.setCalcEnd(new_time);
    		}
    	}
    }
    
    /**
     * Creates a list of subtitle objects, including all annotations of the
     * specified  tiers and applying a minimal duration.
     *
     * @param transcription the transcription document
     * @param tierNames the tiers to include
     * @param intervalBegin the selection begintime
     * @param intervalEnd the selection end time
     * @param minimalDuration the minimal duration per subtitle
     *
     * @return a list of subtitle objects
     *
     * @see #createSequence(Transcription, List, long, long, int, boolean)
     */
    public List<SubtitleUnit> createSequence(Transcription transcription, List<String> tierNames,
        long intervalBegin, long intervalEnd, int minimalDuration) {
        return createSequence(transcription, tierNames, intervalBegin,
            intervalEnd, minimalDuration, 0L, false);
    }

    /**
     * Creates a list of subtitle objects, including all annotations of the
     * specified  tiers and applying a minimal duration.
     *
     * @param transcription the transcription document
     * @param tierNames the tiers to include
     * @param intervalBegin the selection begintime
     * @param intervalEnd the selection end time
     * @param minimalDuration the minimal duration per subtitle
     * @param offset the number of ms. to add to all time values
     * @param resolveOverlaps detects overlapping units and creates new,
     *        merging units for the overlaps
     *
     * @return a list of subtitle objects
     *
     * @see #createSequence(Transcription, List, long, long, int, boolean)
     */
    public List<SubtitleUnit> createSequence(Transcription transcription, String[] tierNames,
        long intervalBegin, long intervalEnd, int minimalDuration, long offset,
        boolean resolveOverlaps) {
        ArrayList<String> names = null;

        if (tierNames != null) {
            names = new ArrayList<String>(tierNames.length);

            for (int i = 0; i < tierNames.length; i++) {
                names.add(tierNames[i]);
            }
        }

        return createSequence(transcription, names, intervalBegin, intervalEnd,
            minimalDuration, offset, resolveOverlaps);
    }

    /**
     * Creates a list of subtitle objects, including all annotations of the
     * specified  tiers and applying a minimal duration.
     *
     * @param transcription the transcription document
     * @param tierNames the tiers to include
     * @param intervalBegin the selection begintime
     * @param intervalEnd the selection end time
     * @param minimalDuration the minimal duration per subtitle
     *
     * @return a list of subtitle objects
     *
     * @see #createSequence(Transcription, String[], long, long, int, boolean)
     */
    public List<SubtitleUnit> createSequence(Transcription transcription, String[] tierNames,
        long intervalBegin, long intervalEnd, int minimalDuration) {
        return createSequence(transcription, tierNames, intervalBegin,
            intervalEnd, minimalDuration, 0L, false);
    }
}
