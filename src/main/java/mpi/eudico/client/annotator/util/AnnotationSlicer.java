package mpi.eudico.client.annotator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.util.TimeInterval;
import mpi.eudico.util.TimeIntervalComparator;

/**
 * A utility class that 'slices' the media time line based on the start and
 * end times of the annotations in a provided list of tiers. Implicitly or
 * explicitly creates new intervals based on the intervals in the tiers. 
 * 
 * @author aarsom
 */
public class AnnotationSlicer {
	
	/**
	 * Returns a sorted list of begin time and end time values
	 * from the list of given tiers
	 * 
	 * @param tierList, list of tiers from which the time values
	 * 					to be extracted
	 * @return ArrayList<Long>, null if the tier list is null
	 */
	public static List<Long> getTimeValues(List<TierImpl> tierList){
		if(tierList == null){
			return null;
		}
		
		List<Long> timeSlotList = new ArrayList<Long>();
		long time;
		for(int i= 0; i < tierList.size(); i++){
			Tier tier = tierList.get(i);
			List<? extends Annotation> annotations  = tier.getAnnotations();
			for(int a=0; a < annotations.size(); a++){
				time = annotations.get(a).getBeginTimeBoundary();
				if(!timeSlotList.contains(time)){
					timeSlotList.add(time);
				}
				
				time = annotations.get(a).getEndTimeBoundary();
				if(!timeSlotList.contains(time)){
					timeSlotList.add(time);
				}
			}
		}
		
		Collections.sort(timeSlotList);	
		return timeSlotList;
	}
	
    /**
     * Returns a map <timeValue, List<annotations at this time value>>
     * 
     * @param timeValuesList, list of time values for which the annotations
     * 						 have to be extracted
     * @param tierList, tier list from which the annotations
     * 					have to extracted
     * 
     * @return HashMap<Long, ArrayList<Annotation>>
     */
	public static Map<Long, List<Annotation>> getAnnotationMap(List<Long> timeValuesList, List<TierImpl> tierList){
		Map<Long, List<Annotation>> map = new HashMap<Long,List<Annotation>>();
		List<Annotation> annList;
		Annotation ann;
		long currentTimeValue;
		for(int i = 0; i < timeValuesList.size(); i++){
			currentTimeValue = timeValuesList.get(i);
			annList  = new ArrayList<Annotation>();
			
			for(int t=0; t < tierList.size(); t++){				
				ann = tierList.get(t).getAnnotationAtTime(currentTimeValue);
				if(ann != null){	
					annList.add(ann);
				}
			}	
			
			map.put(currentTimeValue, annList);
		}		
		return map;
	}
	
	/**
	 * 
	 * @param timeValuesList the list of time values, if obtained via {@link #getTimeValues(List)}
	 * representing all start and end times of the annotations on the specified tiers
	 * @param tierList the tiers to process
	 * 
	 * @return a Map containing time intervals as keys and lists of annotations as values. 
	 * Each list of annotations is of the same size as the list of tiers,
	 * containing <code>null</code> values for the tiers where there is no annotation in that interval.
	 * Returns null if any of the parameters is null.
	 * 
	 * @see #getAnnotationMap(List, List) instead of returning a map of Long to a List, this
	 * variant returns a map of interval keys to lists of annotations 
	 */
	public static SortedMap<TimeInterval, List<Annotation>> getIntervalAnnotationMap (
			List<Long> timeValuesList, List<TierImpl> tierList) {
		if (timeValuesList == null || timeValuesList.isEmpty()) {
			return null;
		}
		if (tierList == null || tierList.isEmpty()) {
			return null;
		}
		
		SortedMap<TimeInterval, List<Annotation>> map = 
				new TreeMap<TimeInterval, List<Annotation>>(new TimeIntervalComparator());
		
		for (int i = 0; i < timeValuesList.size() - 1; i++) {
			Long time1 = timeValuesList.get(i);
			Long time2 = timeValuesList.get(i + 1);
			long mid = (time1 + time2) / 2;
			List<Annotation> annList = new ArrayList<Annotation>();
			boolean anyNonNull = false;
			
			for (TierImpl t : tierList) {
				Annotation a = t.getAnnotationAtTime(mid);
				if (a != null) {
					anyNonNull = true;
				}
				annList.add(a);// adds null if there is no annotation
			}
			// only add rows that contain at least one annotation
			if (anyNonNull) {
				map.put(new TimeInterval(time1, time2), annList);
			}		
		}
		
		return map;
	}
}
