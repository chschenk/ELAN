package mpi.eudico.client.annotator.timeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a time series data track. Data are stored in a list
 * of TimeValue objects; there's no fixed sample rate.
 * There can be gaps in the track (one point is not connected with the previous one). A TimeValueStart 
 * object instead of a TimeValue object indicates the begin of a new segment.
 * By default a line is drawn to connect subsequent points. 
 */
public class NonContinuousRateTSTrack extends AbstractTSTrack {
    private List<TimeValue> data;
    
    /**
     * Creates a new NonContinuousRateTSTrack
     */
    public NonContinuousRateTSTrack() {
        super();
        setType(TimeSeriesTrack.TIME_VALUE_LIST);
    }
    
    /**
     * Creates a new NonContinuousRateTSTrack
     * 
     * @param name the name of the track
     * @param description the description of the track
     */
    public NonContinuousRateTSTrack(String name, String description) {
        super(name, description, TimeSeriesTrack.TIME_VALUE_LIST);
    }
    
    /**
     * If the time is between two times in the List return the lesser/lowest index.
     * 
     * @see mpi.eudico.client.annotator.timeseries.AbstractTSTrack#getIndexForTime(long)
     */
    @Override
	public int getIndexForTime(long time) {
        if (time < 0) {
            throw new IllegalArgumentException("Time should be greater than or equal to 0: " + time);
        }
        if (data == null || data.isEmpty()) {
            return -1;
        }
        int index = -1;
        if (time + timeOffset >= 0) {
            TimeValue key = new TimeValue(time + timeOffset, 0);
            index = Collections.binarySearch(data, key);
            // if the time is not in the list binarySearch returns -(insertion point) -1
            // insertion point is the first value greater than the key, we want the last element
            // smaller than the key
            if (index < 0) {
                // not in the list
                index = -(index + 1);
                // index is insertion point
                if (index > 0) {
                    // get the index of the element before the insertion point
                    index--;
                }
            }
        }
        
        return index;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.AbstractTSTrack#getTimeForIndex(int)
     */
    @Override
	public long getTimeForIndex(int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(
                "Index should be greater than or equal to 0");
        }

        long time = 0L;

        if (data != null) {
            if (index >= data.size()) {
                throw new ArrayIndexOutOfBoundsException("Index (" + index +
                    ") is greater than " + (data.size() - 1));
            }
            
            time = data.get(index).time;
        }
        // HS Jan 2011 return time - timeOffset instead of time + timeOffset?
        return time - timeOffset;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#setSampleRate(float)
     */
    @Override
	public void setSampleRate(float rate) {
        // ignore
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getSampleRate()
     */
    @Override
	public float getSampleRate() {
        return 0; // or -1??
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getSampleCount()
     */
    @Override
	public int getSampleCount() {
        if (data == null) {
            return 0;    
        }
        
        return data.size();
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getData()
     */
    @Override
	public List<TimeValue> getData() {
        return data;
    }

    /**
     * @param data should be an ordered List of TimeValue objects
     * 
     * @throws IllegalArgumentException if the data is not a List (of TimeValue objects, but this is assumed)
     *  
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#setData(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
	@Override
    public void setData(Object data) {
        if (!(data instanceof List)) {
            throw new IllegalArgumentException(
                "This track only accepts a List of TimeValue objects");
        }
        
        setData((List)data);
    }

    /**
     * @param data should be an ordered List of TimeValue objects
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#setData(java.lang.Object)
     */
    public void setData(List<TimeValue> data) {
        this.data = data;
    }

    /**
     * Returns the minimum in the range or Float.NaN in case there are no valid 
     * values in the range.
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMinimum(long, long)
     */
    @Override
	public float getMinimum(long begin, long end) {
        if (data == null || data.isEmpty()) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException(begin + " < 0");
        }
        
        //float maxTime = ((TimeValue) data.get(data.size() - 1)).time;
        
        //HS Jan 2011: try to calculate even if end > maxTime
        //if (end > maxTime) { 
            //throw new ArrayIndexOutOfBoundsException(end + " > " + (maxTime + timeOffset));
        //}

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi == ei) {
        	TimeValue tv = data.get(bi);
        	//System.out.println(" Time at Index: " + tv.time);
        	if (begin <= tv.time - timeOffset && tv.time - timeOffset <= end) {
        		return tv.value;// can be NaN
        	} else {
        		// if bi time < begin and bi = ei, NaN should be returned because no measure value is within the interval?
        		if (tv.time - timeOffset < begin) {
        			if (bi < data.size() - 1) {
        				TimeValue tv2 = data.get(bi + 1);
        				// hier: check whether the time is within the interval?
        				if ((!(tv2 instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv2.value < tv.value ? tv2.value : tv.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// tv.time > end + timeOffset
        			if (ei > 0) {
        				TimeValue tv2 = data.get(ei - 1);
        				if ((!(tv instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv2.value < tv.value ? tv2.value : tv.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        
        float min = Integer.MAX_VALUE;
        float val;
        TimeValue tv = null;
        
        for (int i = bi; (i <= ei) && (i < data.size()); i++) {
        	tv = data.get(i);
        	if (tv.time - timeOffset < begin) {
        		continue;
        	}
            val = tv.value;
            if (!Float.isNaN(val) && val < min) {
                min = val;
            }
        }

        if (min == Integer.MAX_VALUE) {
        	return Float.NaN;
        }
        
        return min;
    }

    /**
     * Returns the maximum in the range or Integer.MIN_VALUE in case there are no valid 
     * values in the range.
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMaximum(long, long)
     */
    @Override
	public float getMaximum(long begin, long end) {
        if (data == null || data.isEmpty()) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException(begin + " < 0");
        }

//        float maxTime = ((TimeValue) data.get(data.size() - 1)).time;
//        
//        if (end > maxTime - timeOffset) {
//            // TODO check whether time offset has been taken into account
//            throw new ArrayIndexOutOfBoundsException(end + " > " + (maxTime + timeOffset));
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi == ei) {
        	TimeValue tv = data.get(bi);
        	//System.out.println(" Time at Index: " + tv.time);
        	if (begin <= tv.time - timeOffset && tv.time - timeOffset <= end) {
        		return tv.value;// can be NaN
        	} else {
        		if (tv.time - timeOffset < begin) {
        			if (bi < data.size() - 1) {
        				TimeValue tv2 = data.get(bi + 1);
        				if ((!(tv2 instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv2.value > tv.value ? tv2.value : tv.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// tv.value > end + timeOffset
        			if (ei > 0) {
        				TimeValue tv2 = data.get(ei - 1);
        				if ((!(tv instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv2.value > tv.value ? tv2.value : tv.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        float max = Integer.MIN_VALUE;
        float val;
        TimeValue tv = null;

        for (int i = bi; (i <= ei) && (i < data.size()); i++) {
        	tv = data.get(i);
        	if (tv.time - timeOffset < begin) {
        		continue;
        	}
            val = tv.value;
            if (!Float.isNaN(val)  && val > max) {
                max = val;
            }
        }

        if (max == Integer.MIN_VALUE) {
        	return Float.NaN;
        }
        
        return max;       
    }

    /**
     * Returns the average value in the range or 0 in case there are no valid values 
     * in the range. 
     * Could return NaN?
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getAverage(long, long)
     */
    @Override
	public float getAverage(long begin, long end) {
        if (data == null || data.isEmpty()) {
            return 0; // throw an exception? return NaN?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException(begin + " < 0");
        }
        // time offset has been taken into account
//        float maxTime = ((TimeValue) data.get(data.size() - 1)).time;
//        
//        if (end > maxTime) {
//        	return Float.NaN;
//            //throw new ArrayIndexOutOfBoundsException(end + " > " + maxTime);
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        //System.out.println(getName() + ": BT " + begin + " BTI " + bi + "  ET " + end + " ETI " + ei);
        // check whether there are real values in the range
        if (bi == ei) {
        	TimeValue tv = data.get(bi);
        	//System.out.println(" Time at Index: " + tv.time);
        	if (begin <= tv.time - timeOffset && tv.time - timeOffset <= end) {
        		return tv.value;// can be NaN
        	} else {
        		if (tv.time - timeOffset < begin) {
        			if (bi < data.size() - 1) {
        				TimeValue tv2 = data.get(bi + 1);
        				if ((!(tv2 instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return (tv2.value + tv.value) / 2;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// tv.value > end - timeOffset
        			if (ei > 0) {
        				TimeValue tv2 = data.get(ei - 1);
        				if ((!(tv instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return (tv2.value + tv.value) / 2;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        int count = 0;
        float total = 0f;
        TimeValue tv;

        for (int i = bi; (i <= ei) && (i < data.size()); i++) {
        	tv = data.get(i);
        	if (tv.time - timeOffset < begin) {
        		continue;
        	}
        	
        	if (!Float.isNaN(tv.value)) {
        		total += tv.value;
        		count++;
        	}
        }
        
        if (count == 0) {
            return 0;// or return NaN?
        } else {
            return total / count;
        }
    }

    /**
     * Returns the sum of values in the range 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMaximum(long, long)
     */
    @Override
	public float getSum(long begin, long end) {
    	if (data == null || data.isEmpty()) {
            return 0; // throw an exception? return NaN?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException(begin + " < 0");
        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi == ei) {
        	TimeValue tv = data.get(bi);
        	if (begin <= tv.time - timeOffset && tv.time - timeOffset <= end) {
        		return tv.value;// can be NaN
        	} else {
        		if (tv.time - timeOffset < begin) {
        			if (bi < data.size() - 1) {
        				TimeValue tv2 = data.get(bi + 1);
        				if ((!(tv2 instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv2.value + tv.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// tv.value > end - timeOffset
        			if (ei > 0) {
        				TimeValue tv2 = data.get(ei - 1);
        				if ((!(tv instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv2.value + tv.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        int count = 0;
        float total = 0f;
        TimeValue tv;

        for (int i = bi; (i <= ei) && (i < data.size()); i++) {
        	tv = data.get(i);
        	if (tv.time - timeOffset < begin) {
        		continue;
        	}
        	
        	if (!Float.isNaN(tv.value)) {
        		total += tv.value;
        		count++;
        	}
        }
        
        if (count == 0) {
            return 0;// or return NaN?
        } else {
            return total;
        }
    }
    
    /**
     * Returns the sum of values in the range 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMaximum(long, long)
     */
    @Override
	public float getValueAtBegin(long begin, long end) {
    	if (data == null || data.isEmpty()) {
            return 0; // throw an exception? return NaN?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException(begin + " < 0");
        }
        
        // check whether bi is within the range, otherwise try bi + 1
        int bi = getIndexForTime(begin);
        
        TimeValue tvb = data.get(bi);
    	if (begin <= tvb.time - timeOffset && tvb.time - timeOffset <= end) {
    		return tvb.value;// can be NaN
    	} else if (tvb.time - timeOffset < begin) {
    		if (bi < data.size() - 1) {
    			TimeValue tv2 = data.get(bi + 1);
    			if (begin <= tv2.time - timeOffset && tv2.time - timeOffset <= end) {
    				return tv2.value;
    			} else {
    				return Float.NaN;
    			}
    		} else {
    			return Float.NaN;
    		}
    	} else {
    		return Float.NaN;
    	}
        /*
        int ei = getIndexForTime(end);// hier... is the end index relevant?
        
        if (bi == ei) {
        	TimeValue tv = (TimeValue) data.get(bi);
        	if (begin <= tv.time - timeOffset && tv.time - timeOffset <= end) {
        		return tv.value;// can be NaN
        	} else {
        		if (tv.time - timeOffset < begin) {
        			if (bi < data.size() - 1) {
        				TimeValue tv2 = (TimeValue) data.get(bi + 1);
        				if ((!(tv2 instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// tv.value > end - timeOffset
        			if (ei > 0) {
        				TimeValue tv2 = (TimeValue) data.get(ei - 1);
        				if ((!(tv instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        // if bi != ei, check whether bi is within the range, otherwise try bi + 1

        return ((TimeValue) data.get(bi)).value;
        */
    }
    
    /**
     * Returns the sum of values in the range 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMaximum(long, long)
     */
    @Override
	public float getValueAtEnd(long begin, long end) {
    	if (data == null || data.isEmpty()) {
            return 0; // throw an exception? return NaN?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException(begin + " < 0");
        }
        // check index at end, if that time is > end check index - 1
        int ei = getIndexForTime(end);
        
        TimeValue tvb = data.get(ei);
    	
        if (begin <= tvb.time - timeOffset && tvb.time - timeOffset <= end) {
    		return tvb.value;// can be NaN
    	} else if (tvb.time - timeOffset > end) {
    		// unlikely to happen because the value returned by getIndexForTime "rounds down"
    		if (ei >= 1) {
    			TimeValue tv2 = data.get(ei - 1);
    			if (begin <= tv2.time - timeOffset && tv2.time - timeOffset <= end) {
    				return tv2.value;
    			} else {
    				return Float.NaN;
    			}
    		} else {
    			return Float.NaN;
    		}
    	} else {
    		return Float.NaN;
    	}

        /*
        if (bi == ei) {
        	TimeValue tv = (TimeValue) data.get(bi);
        	if (begin <= tv.time - timeOffset && tv.time - timeOffset <= end) {
        		return tv.value;// can be NaN
        	} else {
        		if (tv.time - timeOffset < begin) {
        			if (bi < data.size() - 1) {
        				TimeValue tv2 = (TimeValue) data.get(bi + 1);
        				if ((!(tv2 instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv2.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// tv.value > end - timeOffset
        			if (ei > 0) {
        				TimeValue tv2 = (TimeValue) data.get(ei - 1);
        				if ((!(tv instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return tv2.value;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        return ((TimeValue) data.get(ei)).value;
        */
    }

    /**
     * Returns NaN in case there are no (valid) values in the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMedian(long,
     *      long)
     */
    @Override
	public float getMedian(long begin, long end) {
        if (data == null || data.isEmpty()) {
            return 0; // throw an exception? return NaN?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException(begin + " < 0");
        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        // check whether there are real values in the range
        // in case of one index for begin and end, the implementation is the same as for average
        if (bi == ei) {
        	TimeValue tv = data.get(bi);
        	//System.out.println(" Time at Index: " + tv.time);
        	if (begin <= tv.time - timeOffset && tv.time - timeOffset <= end) {
        		return tv.value;// can be NaN
        	} else {
        		if (tv.time - timeOffset < begin) {
        			if (bi < data.size() - 1) {
        				TimeValue tv2 = data.get(bi + 1);
        				if ((!(tv2 instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return (tv2.value + tv.value) / 2;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// tv.value > end - timeOffset
        			if (ei > 0) {
        				TimeValue tv2 = data.get(ei - 1);
        				if ((!(tv instanceof TimeValueStart)) && (!Float.isNaN(tv.value) && !Float.isNaN(tv2.value))){
        					return (tv2.value + tv.value) / 2;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }

        List<Float> valList = new ArrayList<Float>();

        for (int i = bi; (i <= ei) && (i < data.size()); i++) {
        	TimeValue tv = data.get(i);
        	if (tv.time - timeOffset < begin) {
        		continue;
        	}
        	
        	if (!Float.isNaN(tv.value)) {
        		valList.add(new Float(tv.value));//Float.valueOf(tv.value) ?
        	}
        }
        
        Collections.sort(valList);
        
        int count = valList.size();
        if (count == 0) {
            return Float.NaN;
        } else {
            if (count % 2 == 0) {
            	// even number of elements, need item c/2 and c/2 + 1, so in 0 based list c/2 - 1 and c/2
            	int mid = count / 2;
            	float f1 = valList.get(mid - 1);
            	float f2 = valList.get(mid);
            	return (f1 + f2) / 2;
            } else {
            	// odd number, middle is c/2 rounded down + 1, so in 0 based list c/2
            	int mid = count / 2;
            	return valList.get(mid);
            }
        }
	}

    /**
     * Returns NaN in case there are no (valid) values in the range.
     * Returns 0 if there is only one value in the range (maximum == minimum)
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getRange(long,
     *      long)
     */
	@Override
	public float getRange(long begin, long end) {
        if (data == null || data.isEmpty()) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException(begin + " < 0");
        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi == ei) {
        	TimeValue tv = data.get(bi);
        	//System.out.println(" Time at Index: " + tv.time);
        	if (begin <= tv.time - timeOffset && tv.time - timeOffset <= end) {
        		return tv.value;// within the interval, can be NaN
        	} else {
        		return Float.NaN;
        	}
        }
        
        float max = Integer.MIN_VALUE;
        float min = Integer.MAX_VALUE;

        for (int i = bi; (i <= ei) && (i < data.size()); i++) {
        	TimeValue tv = data.get(i);
        	if (tv.time - timeOffset < begin) {
        		continue;
        	}
            float val = tv.value;
            if (!Float.isNaN(val)) {
            	if (val > max) {
            		max = val;
            	}
            	if (val < min) {
            		min = val;
            	}
            }
        }

        if (max == Integer.MIN_VALUE) {
        	return Float.NaN;
        }
        
        return max - min;
	}

	/**
	 * Retrieves the last TimeValue object from the samples' list and returns the 
	 * time value of it.
	 * 
	 * @return the time value of the last sample in the list
	 */
	@Override
	public long getDataDuration() {
		if (data != null && data.size() > 0) {
			return data.get(data.size() - 1).time;
		}
		
		return super.getDataDuration();
	}
    
    
}
