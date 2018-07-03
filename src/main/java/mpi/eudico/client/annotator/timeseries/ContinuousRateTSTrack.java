package mpi.eudico.client.annotator.timeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a time series data track. Data are stored in a flat list
 * or array of values; in combination with the  (fixed) sample rate it is
 * possible to find a time-value pair.
 */
public class ContinuousRateTSTrack extends AbstractTSTrack {
    private float sampleRate;
    private float msPerSample;
    private float[] data;

    /**
     * Constructor.
     */
    public ContinuousRateTSTrack() {
        super();
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#setSampleRate(int)
     */
    @Override
	public void setSampleRate(float rate) {
        sampleRate = rate;
        msPerSample = 1000 / sampleRate;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getSampleRate()
     */
    @Override
	public float getSampleRate() {
        return sampleRate;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getSampleCount()
     */
    @Override
	public int getSampleCount() {
        if (data == null) {
            return 0;
        }

        return data.length;
    }

    /**
     * Returns an array of floats.
     *
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getData()
     */
    @Override
	public float[] getData() {
        return data;
    }

    /**
     * Sets the data of this tracks. Currently this method only accepts an
     * array of floats; in any other case  an IllegalArgumentException will be
     * thrown.
     *
     * @param data DOCUMENT ME!
     *
     * @throws IllegalArgumentException when the data is provided in anything
     *         else but an array of floats
     *
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#setData(java.lang.Object)
     */
    @Override
	public void setData(Object data) {
        if (!(data instanceof float[])) {
            throw new IllegalArgumentException(
                "This track only accepts an array of floats");
        }

        setData((float[]) data);
    }

    /**
     * Sets the data of this tracks.
     *
     * @param data DOCUMENT ME!
     *
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#setData(java.lang.Object)
     */
	public void setData(float[] data) {
        this.data = (float[]) data;
    }

    /**
     * Returns a calculated index based on the time, time offset and the sample
     * rate. There is no guarantee that the index is within the data array's
     * bounds.
     *
     * @see mpi.eudico.client.annotator.timeseries.AbstractTSTrack#getIndexForTime(long)
     */
    @Override
	public int getIndexForTime(long time) {
        int index = -1;

        if ((time + timeOffset) >= 0) {
            //index = (int) ((time + timeOffset) / msPerSample);	
            /* this might be more accurate and more consistent */
            index = (msPerSample >= 1)
                ? (int) ((time + timeOffset) / msPerSample)
                : (int) Math.ceil((time + timeOffset) / msPerSample);
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
            if (index >= data.length) {
                throw new ArrayIndexOutOfBoundsException("Index (" + index +
                    ") is greater than " + (data.length - 1));
            }

            time = (long) (index * msPerSample);
        }
        // HS Jan 2011 return time - timeOffset instead of time + timeOffset?
        return time - timeOffset;
    }

    /**
     * Returns Float.NaN if there are no (valid) values within the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getAverage(long,
     *      long)
     */
    @Override
	public float getAverage(long begin, long end) {
        if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

        // try to calculate an average anyway, for the part that does exist in the interval
//        if (end > (msPerSample * data.length)) {
//            // in fact: end + timeOffset > msPerSample * data.length - timeOffset
//            throw new ArrayIndexOutOfBoundsException("End time greater than track duration: " + end + " > " +
//                (msPerSample * data.length));
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return (data[bi] + data[bi + 1]) / 2;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return (data[bi] + data[bi - 1]) / 2;
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

        for (int i = bi; (i <= ei) && (i < data.length); i++) {
        	if (!Float.isNaN(data[i])) {
        		total += data[i];
        		count++;
        	}
        }

        if (count == 0) {
            return Float.NaN;
        } else {
            return total / count;
        }
    }

    /**
     * Returns NaN in case no valid values are in the range.
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMaximum(long,
     *      long)
     */
    @Override
	public float getMaximum(long begin, long end) {
        if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

//        if (end > (msPerSample * data.length)) {
//            // in fact: end + timeOffset > msPerSample * data.length - timeOffset
//            throw new ArrayIndexOutOfBoundsException("End time greater than track duration: " + end + " > " +
//                (msPerSample * data.length));
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi] > data[bi + 1] ? data[bi] : data[bi + 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi] > data[bi - 1] ? data[bi] : data[bi - 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        float max = Integer.MIN_VALUE; //problem with Float.MIN_VALUE
        int count = 0;

        for (int i = bi; (i <= ei) && (i < data.length); i++) {
            if (!Float.isNaN(data[i])) {
            	if (data[i] > max) {
            		max = data[i];
            	}
                count++;
            }
        }
        
        if (count > 0) {
        	return max;
        } else {
        	return Float.NaN;
        }
    }

    /**
     * Returns NaN in case there are no (valid) values in the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMinimum(long,
     *      long)
     */
    @Override
	public float getMinimum(long begin, long end) {
        if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

//        if (end > (msPerSample * data.length)) {
//            // in fact: end + timeOffset > msPerSample * data.length - timeOffset
//            throw new ArrayIndexOutOfBoundsException("End time greater than track duration: " + end + " > " +
//                (msPerSample * data.length));
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi] < data[bi + 1] ? data[bi] : data[bi + 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi] < data[bi - 1] ? data[bi] : data[bi - 1];
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
        int count = 0;

        for (int i = bi; (i <= ei) && (i < data.length); i++) {
            if (!Float.isNaN(data[i])) {
            	if (data[i] < min) {
            		min = data[i];
            	}
                count++;
            }
        }
        
        if (count > 0) {
        	return min;
        } else {
        	return Float.NaN;
        }
    }
    
    /**
     * Returns NaN in case there are no (valid) values in the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getSum(long,
     *      long)
     */
    @Override
	public float getSum(long begin, long end) {
    	if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi] + data[bi + 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi] + data[bi - 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        float sum = 0;
        int count = 0;
        
        for (int i = bi; (i <= ei) && (i < data.length); i++) {
            if (!Float.isNaN(data[i])) {
            	sum += data[i];
                count++;
            }
        }
        
        if (count > 0) {
        	return sum;
        } else {
        	return Float.NaN;
        }
    }

    /**
     * Returns NaN in case there are no (valid) values in the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getValueAtBegin(long,
     *      long)
     */
	@Override
	public float getValueAtBegin(long begin, long end) {
		if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi - 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        return data[bi];
	}

    /**
     * Returns NaN in case there are no (valid) values in the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getValueAtEnd(long,
     *      long)
     */
	@Override
	public float getValueAtEnd(long begin, long end) {
		if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi + 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        return data[ei];
	}

    /**
     * Returns NaN in case there are no (valid) values in the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMedian(long,
     *      long)
     */
	@Override
	public float getMedian(long begin, long end) {
        if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

        // try to calculate the median anyway, for the part that does exist in the interval
//        if (end > (msPerSample * data.length)) {
//            // in fact: end + timeOffset > msPerSample * data.length - timeOffset
//            throw new ArrayIndexOutOfBoundsException("End time greater than track duration: " + end + " > " +
//                (msPerSample * data.length));
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        // in case of one index for begin and end, the implementation is the same as for average
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return (data[bi] + data[bi + 1]) / 2;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return (data[bi] + data[bi - 1]) / 2;
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
        for (int i = bi; (i <= ei) && (i < data.length); i++) {
        	// only store real values (or?)
        	if (!Float.isNaN(data[i])) {
        		valList.add(new Float(data[i]));// Float.valueOf(data[i]) ?
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
        if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

//        if (end > (msPerSample * data.length)) {
//            // in fact: end + timeOffset > msPerSample * data.length - timeOffset
//            throw new ArrayIndexOutOfBoundsException("End time greater than track duration: " + end + " > " +
//                (msPerSample * data.length));
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return 0;// within the interval
        	} else {
        		return Float.NaN;
        	}
        }
        
        float max = Integer.MIN_VALUE; //problem with Float.MIN_VALUE
        float min = Integer.MAX_VALUE;
        int count = 0;

        for (int i = bi; (i <= ei) && (i < data.length); i++) {
            if (!Float.isNaN(data[i])) {
            	if (data[i] > max) {
            		max = data[i];
            	}
            	if (data[i] < min) {
            		min = data[i];
            	}
                count++;
            }
        }
        
        if (count > 0) {
        	return max - min;
        } else {
        	return Float.NaN;
        }
	}

	/**
	 * Calculates the duration based on the number of samples in the array.
	 * 
	 * @return the time value corresponding to the last sample in the array
	 */
	@Override
	public long getDataDuration() {
		if (data != null && data.length > 0) {
			return (long) ((data.length - 1) * msPerSample);
		}
		
		return super.getDataDuration();
	}
}
