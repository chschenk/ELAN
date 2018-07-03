package mpi.eudico.client.annotator.viewer;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;


/**
 * Stores the waveform geometry of an interval  of an audiofile. This class is
 * not threadsafe.
 *
 * @author Han Sloetjes
 * @version June 2003
 */
public class WavePart {
    /** use GeneralPath objects to store coordinates */
    public static final int GENERAL_PATH_MODE = 0;

    /** use int arrays to store y coordinates */
    public static final int INT_ARRAY_MODE = 1;

    /** The interval starttime of this WavePart */
    private long startTime;

    /** The interval stoptime of this WavePart */
    private long stopTime;
    private boolean useTwoChannels;
    
    private boolean drawExtremesContour = false;

    /**
     * The path of <br>
     * - the one channel of a mono audiofile<br>
     * - the first channel of a stereo audiofile<br>
     * - the two, merged channels of a stereo audiofile
     */
    private GeneralPath firstPath;

    /** The path of the second channel of a stereo audiofile */
    private GeneralPath secondPath;

    /** the index of the first sample to load */
    int startSample;

    /** the max number of pixels to paint in the current interval */
    int extent;

    /** four arrays to store top and bottom values for two channels */
    private int[] leftTops;
    private int[] leftBottoms;
    private int[] rightTops;
    private int[] rightBottoms;
    private int mode = GENERAL_PATH_MODE;

    /**
     * No arg constructor, initializes all fields.
     */
    public WavePart() {
        this(0L, 0L, true);
    }

    /**
     * Constructor, initializes this WavePart in the specified mode.
     *
     * @param mode DOCUMENT ME!
     */
    public WavePart(int mode) {
        this(0L, 0L, true, mode);
    }

    /**
     * Constructs a WavePart for the interval specified by the given
     * parameters. Assumes that two channels can be used.
     *
     * @param startTime the interval starttime
     * @param stopTime the interval stoptime
     */
    public WavePart(long startTime, long stopTime) {
        this(startTime, stopTime, true);
    }

    /**
     * Creates a new WavePart instance
     *
     * @param startTime DOCUMENT ME!
     * @param stopTime DOCUMENT ME!
     * @param useTwoChannels DOCUMENT ME!
     */
    public WavePart(long startTime, long stopTime, boolean useTwoChannels) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        firstPath = new GeneralPath();
        setUseTwoChannels(useTwoChannels);
    }

    /**
     * Creates a new WavePart instance
     *
     * @param startTime DOCUMENT ME!
     * @param stopTime DOCUMENT ME!
     * @param useTwoChannels DOCUMENT ME!
     * @param mode DOCUMENT ME!
     */
    public WavePart(long startTime, long stopTime, boolean useTwoChannels,
        int mode) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        setMode(mode);
        setUseTwoChannels(useTwoChannels);
    }

    /**
     * DOCUMENT ME!
     *
     * @return the path for the first channel
     */
    public GeneralPath getFirstPath() {
        return firstPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the path for the second channel
     */
    public GeneralPath getSecondPath() {
        return secondPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the current interval starttime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * DOCUMENT ME!
     *
     * @param startTime the new interval starttime
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the current interval stoptime
     */
    public long getStopTime() {
        return stopTime;
    }

    /**
     * DOCUMENT ME!
     *
     * @param stopTime the new interval stoptime
     */
    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    /**
     * The mode of a WavePart determines how the audio data should be stored.
     *
     * @return the mode of this WavePart
     */
    public int getMode() {
        return mode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param mode the new mode for this WavePart
     */
    public void setMode(int mode) {
        if (mode == INT_ARRAY_MODE) {
            this.mode = mode;
        } else {
            this.mode = GENERAL_PATH_MODE;

            if (firstPath == null) {
                firstPath = new GeneralPath();
            }
        }
    }

    /**
     * @see #setDrawExtremesContour(boolean)
	 * @return the drawExtremesContour
	 */
	public boolean isDrawExtremesContour() {
		return drawExtremesContour;
	}

	/**
	 * If set to true, a single line will be drawn connecting an extreme value on the x axis with
	 * the extreme value of x + 1. Otherwise per x-coordinate a vertical line will be drawn 
	 * connecting 2 values on the y axis. The latter option is the default. 
	 * The former mode is better suited in case of extreme zoom-in levels.
	 * 
	 * @param drawExtremesContour the drawExtremesContour to set
	 */
	public void setDrawExtremesContour(boolean drawExtremesContour) {
		this.drawExtremesContour = drawExtremesContour;
	}

	/**
     * DOCUMENT ME!
     *
     * @return true if two channels are in use, false otherwise
     */
    public boolean isUseTwoChannels() {
        return useTwoChannels;
    }

    /**
     * Sets whether or not two channels are to be used.
     *
     * @param useTwoChannels the new value for useTwoChannels
     */
    public void setUseTwoChannels(boolean useTwoChannels) {
        this.useTwoChannels = useTwoChannels;

        if (!useTwoChannels) {
            secondPath = null;
            rightTops = null;
            rightBottoms = null;
        } else if ((mode == GENERAL_PATH_MODE) && (secondPath == null)) {
            secondPath = new GeneralPath();
        }
    }

    /**
     * Sets the current interval time, the first sample (startTime /
     * msPerPixel) and  the size (in pixels) of the current interval.
     *
     * @param startTime the new start time
     * @param stopTime the new end time
     * @param startSample the first sample as a x coordinate value
     * @param size the number of pixels (the minimal size of the int array)
     * @param extent the max number of pixels to paint
     */
    public void setInterval(long startTime, long stopTime, int startSample,
        int size, int extent) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.startSample = startSample;
        this.extent = extent;

        if (mode == INT_ARRAY_MODE) {
            if ((leftTops == null) || (leftTops.length < size)) {
                leftTops = new int[size];
                leftBottoms = new int[size];
            }

            if (useTwoChannels &&
                    ((rightTops == null) || (rightTops.length < size))) {
                rightTops = new int[size];
                rightBottoms = new int[size];
            }
        }
    }

    /**
     * Add a line to the left channel.
     *
     * @param x the x coordinate
     * @param top the y coordinate of the max value
     * @param bottom the y coordinate of the min value
     */
    public void addLineToFirstChannel(int x, int top, int bottom) {
        if (mode == GENERAL_PATH_MODE) {
            firstPath.moveTo(x, top);
            firstPath.lineTo(x, bottom);
        } else {
            if (((x - startSample) >= 0) &&
                    ((x - startSample) < leftTops.length)) {
                leftTops[x - startSample] = top;
                leftBottoms[x - startSample] = bottom;
            }
        }
    }

    /**
     * Add a line to the right channel.
     *
     * @param x the x coordinate
     * @param top the y coordinate of the max value
     * @param bottom the y coordinate of the min value
     */
    public void addLineToRightChannel(int x, int top, int bottom) {
        if (mode == GENERAL_PATH_MODE) {
            secondPath.moveTo(x, top);
            secondPath.lineTo(x, bottom);
        } else {
            if (((x - startSample) >= 0) &&
                    ((x - startSample) < rightTops.length)) {
                rightTops[x - startSample] = top;
                rightBottoms[x - startSample] = bottom;
            }
        }
    }

    /**
     * Paint either the path or the data contained in the int array for the
     * left channel.
     *
     * @param g2d the g=Graphics context to render to
     * @param at the transformation to use for the channel
     */
    public void paintLeftChannel(Graphics2D g2d, AffineTransform at) {
        if (mode == GENERAL_PATH_MODE) {
        	if (!drawExtremesContour) {
        		g2d.draw(firstPath.createTransformedShape(at));
        	} else {
        		// for convenience
        		paintLeftChannelLimit(g2d, at, Integer.MAX_VALUE);
        	}
        } else {
            float scale = (float) at.getScaleY();
            
			if (leftTops != null) {	
				if (!drawExtremesContour) {
		            for (int i = 0; (i < leftTops.length) && (i < (extent - 1)); i++) {
		                g2d.drawLine(i + startSample, (int) (leftTops[i] * scale),
		                    i + startSample, (int) (leftBottoms[i] * scale));
		            }
				} else {
					float y1 = 0, y2;
		            for (int i = 0; (i < leftTops.length) && (i < (extent - 1)); i++) {
		            	y2 = (Math.abs(leftTops[i]) >= Math.abs(leftBottoms[i])) ? (leftTops[i] * scale) : 
		            		(leftBottoms[i] * scale);
		            	if (i == 0) {
		            		y1 = y2;
		            		continue;
		            	}
		            	g2d.drawLine(i + startSample -1, (int)y1, i + startSample, (int)y2);
		            	y1 = y2;
		            }
				}
			}
        }
    }

    /**
     * Paint the data contained in the int array for the
     * left channel, while restricting the "amplitude" to a specified min/max amplitude.
     *
     * @param g2d the g=Graphics context to render to
     * @param at the transformation to use for the channel
     * @param limit the maximum/minimum amplitude
     */
    public void paintLeftChannelLimit(Graphics2D g2d, AffineTransform at, int limit) {
        if (mode == INT_ARRAY_MODE) {
            float scale = (float) at.getScaleY();
            
			if (leftTops != null) {
				if (!drawExtremesContour) {
		            for (int i = 0; (i < leftTops.length) && (i < (extent - 1)); i++) {
		                g2d.drawLine(i + startSample, Math.max((int) (leftTops[i] * scale), -limit),
		                    i + startSample, Math.min((int) (leftBottoms[i] * scale), limit));
		                //System.out.println("T: " + leftTops[i] + " B: " + leftBottoms[i]);
		            }
				} else {
					float y1 = 0, y2;
		            for (int i = 0; (i < leftTops.length) && (i < (extent - 1)); i++) {
		            	y2 = (Math.abs(leftTops[i]) >= Math.abs(leftBottoms[i])) ? Math.max((leftTops[i] * scale), -limit) : 
		            		Math.min((leftBottoms[i] * scale), limit);
		            	if (i == 0) {//start drawing after the second point
		            		y1 = y2;
		            		continue;
		            	}
		            	g2d.drawLine(i + startSample -1, (int)y1, i + startSample, (int)y2);
		            	y1 = y2;
		            }
				}
			}
        } else { // GENERAL_PATH
        	float[] segm = new float[2];
        	float[] segm2 = new float[2];
        	PathIterator pathIt = firstPath.getPathIterator(at);
        	int i = 0;
        	
        	if (!drawExtremesContour) {
	        	// the first value of the array is the segment index, the second the value
	        	while (!pathIt.isDone() && i < extent) {     		
	        		pathIt.currentSegment(segm);
	        		
	        		if (!pathIt.isDone()) {
	        			pathIt.next();
	        			pathIt.currentSegment(segm2);

	        			g2d.drawLine(i + startSample, Math.max((int)segm[1], -limit), i + startSample, 
	        					Math.min((int)segm2[1], limit));
	        			
	            		pathIt.next();
	            		i++;
	        		}
	        	}
        	} else {
        		float y1 = 0, y2;
    			
	        	while (!pathIt.isDone() && i < extent) {     		
	        		pathIt.currentSegment(segm);
	        		
	        		if (!pathIt.isDone()) {
	        			pathIt.next();
	        			pathIt.currentSegment(segm2);
	        			
	        			y2 = Math.abs(segm[1]) >= Math.abs(segm2[1]) ? Math.max((int)segm[1], -limit) :
	        				Math.min((int)segm2[1], limit);
	        			
		            	if (i == 0) {//start drawing after the second point
		            		y1 = y2;
		            		i++;
		            		continue;
		            	}
		            	g2d.drawLine(i + startSample -1, (int)y1, i + startSample, (int)y2);
		            	y1 = y2;
	        			
	            		pathIt.next();
	            		i++;
	        		}
	        	}

        	}
        }
    }
    
    /**
     * Paint either the path or the data contained in the int array for the
     * right channel.
     *
     * @param g2d the g=Graphics context to render to
     * @param at the transformation to use for the channel
     */
    public void paintRightChannel(Graphics2D g2d, AffineTransform at) {
        if (mode == GENERAL_PATH_MODE) {
        	if (!drawExtremesContour) {
        		g2d.draw(secondPath.createTransformedShape(at));
        	} else {// for convenience
        		paintRightChannelLimit(g2d, at, Integer.MAX_VALUE);
        	}
        } else {
            float scale = (float) at.getScaleY();
			if (rightTops != null) {
				if (!drawExtremesContour) {
		            for (int i = 0; (i < rightTops.length) && (i < (extent - 1));
		                    i++) {
		                g2d.drawLine(i + startSample, (int) (rightTops[i] * scale),
		                    i + startSample, (int) (rightBottoms[i] * scale));
		            }
				} else {
					float y1 = 0, y2;
		            for (int i = 0; (i < rightTops.length) && (i < (extent - 1)); i++) {
		            	y2 = (Math.abs(rightTops[i]) >= Math.abs(rightBottoms[i])) ? (rightTops[i] * scale) : 
		            		(rightBottoms[i] * scale);
		            	if (i == 0) {//start drawing after the second point
		            		y1 = y2;
		            		continue;
		            	}
		            	g2d.drawLine(i + startSample -1, (int)y1, i + startSample, (int)y2);
		            	y1 = y2;
		            }
				}
			}
        }
    }
    
    /**
     * Paint the data contained in the int array for the
     * right channel, while restricting the "amplitude" to a specified min/max amplitude.
     *
     * @param g2d the g=Graphics context to render to
     * @param at the transformation to use for the channel
     * @param limit the maximum/minimum amplitude
     */
    public void paintRightChannelLimit(Graphics2D g2d, AffineTransform at, int limit) {
        if (mode == INT_ARRAY_MODE) {
            float scale = (float) at.getScaleY();
			if (rightTops != null) {
				if (!drawExtremesContour) {
		            for (int i = 0; (i < rightTops.length) && (i < (extent - 1));
		                    i++) {
		                g2d.drawLine(i + startSample, Math.max((int) (rightTops[i] * scale), -limit),
		                    i + startSample, Math.min((int) (rightBottoms[i] * scale), limit));
		            }
				} else {
					float y1 = 0, y2;
		            for (int i = 0; (i < rightTops.length) && (i < (extent - 1)); i++) {
		            	y2 = (Math.abs(rightTops[i]) >= Math.abs(rightBottoms[i])) ? Math.max((rightTops[i] * scale), -limit) : 
		            		Math.min((rightBottoms[i] * scale), limit);
		            	if (i == 0) {//start drawing after the second point
		            		y1 = y2;
		            		continue;
		            	}
		            	g2d.drawLine(i + startSample -1, (int)y1, i + startSample, (int)y2);
		            	y1 = y2;
		            }
				}
			}
        } else {
        	float[] segm = new float[2];
        	float[] segm2 = new float[2];
        	PathIterator pathIt = secondPath.getPathIterator(at);
        	int i = 0;
        	
        	if (!drawExtremesContour) {
	        	// the first value of the array is the segment index, the second the value
	        	while (!pathIt.isDone() && i < extent) {     		
	        		pathIt.currentSegment(segm);
	        		
	        		if (!pathIt.isDone()) {
	        			pathIt.next();
	        			pathIt.currentSegment(segm2);
	        			
	        			g2d.drawLine(i + startSample, Math.max((int)segm[1], -limit), i + startSample, 
	        					Math.min((int)segm2[1], limit));
	        			
	            		pathIt.next();
	            		i++;
	        		}
	        	}
        	} else {
        		float y1 = 0, y2;
    			
	        	while (!pathIt.isDone() && i < extent) {     		
	        		pathIt.currentSegment(segm);
	        		
	        		if (!pathIt.isDone()) {
	        			pathIt.next();
	        			pathIt.currentSegment(segm2);
	        			
	        			y2 = Math.abs(segm[1]) >= Math.abs(segm2[1]) ? Math.max((int)segm[1], -limit) :
	        				Math.min((int)segm2[1], limit);
	        			
		            	if (i == 0) {//start drawing after the second point
		            		y1 = y2;
		            		i++;
		            		continue;
		            	}
		            	g2d.drawLine(i + startSample -1, (int)y1, i + startSample, (int)y2);
		            	y1 = y2;
	        			
	            		pathIt.next();
	            		i++;
	        		}
	        	}

        	}
        }
    }

    /**
     * Checks whether the specified time is within the current  interval
     * starttime and stoptime, inclusive.
     *
     * @param time the time to check
     *
     * @return true if <code>time</code> is within the current interval,  false
     *         otherwise
     */
    public boolean contains(long time) {
        if ((time >= startTime) && (time <= stopTime)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether the specified time interval is within  the current
     * interval starttime and stoptime, inclusive.
     *
     * @param fromTime the starttime of the interval to check
     * @param toTime the stoptime of the interval to check
     *
     * @return true if the given time interval is within the current interval,
     *         false otherwise
     */
    public boolean contains(long fromTime, long toTime) {
        if ((fromTime >= startTime) && (toTime <= stopTime)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Returns the amount of overlap of the currently loaded interval 
     * with the specified interval, a value between 0 and 1.
     * 
     * @param nextFrom next begin time of interval	
     * @param nextTo next end time of interval, is assumed to be > nextFrom
     * @return a value between 0 and 1 (if there is no or only partial overlap)
     */
    public float amountOfOverlap(long nextFrom, long nextTo) {
    	if (nextFrom >= stopTime || nextTo <= startTime) {// no overlap
    		return 0f;
    	}
    	if (nextFrom < startTime) { // at least left overlap, maybe left and right (return > 1) 
    		return (nextTo - startTime) / (float) (stopTime - startTime);
    	}
    	// right overlap only
    	return (stopTime - nextFrom) / (float) (stopTime - startTime);
    }
    
    /**
     * Returns whether the new interval is of same length as the current
     * @param nextFrom new begin time
     * @param nextTo new end time
     * @return true if the intervals are same length
     */
    public boolean sameIntervalLength(long nextFrom, long nextTo) {
    	return stopTime - startTime == nextTo - nextFrom;
    }
    
    /**
     * Returns true if there is only an overlap on the left side.
     * @param nextFrom new begin time
     * @param nextTo new end time
     * @return true if there is only an overlap on the left side
     */
    public boolean leftOverlap(long nextFrom, long nextTo) {
    	return nextFrom <= startTime && nextTo > startTime && nextTo < stopTime; 
    }
    
    /**
     * Returns true if there is only an overlap on the right side.
     * @param nextFrom new begin time
     * @param nextTo new end time
     * @return true if there is only an overlap on the right side
     */
    public boolean rightOverlap(long nextFrom, long nextTo) {
    	return nextFrom > startTime && nextFrom < stopTime && nextTo >= stopTime; 
    }
    
    /**
     * Shifts part of the values in the arrays to the left or to the right.
     * If distance > 0 values will be shifted to the right (the rightmost values will be dropped)
     * else values will be shifted to the left (leftmost values will be dropped).
     * Note: only applicable in case of INT_ARRAY_MODE
     * 
     * @param distance the number of positions to shift
     */
    public void shiftInterval(int distance) {
    	if (mode == INT_ARRAY_MODE) {
    		if (distance > 0) {
    			if (leftTops != null) {
    				int copyLength = leftTops.length - distance;
    				System.arraycopy(leftTops, 0, leftTops, distance, copyLength);
    				System.arraycopy(leftBottoms, 0, leftBottoms, distance, copyLength);
    				if (rightTops != null) {
        				System.arraycopy(rightTops, 0, rightTops, distance, copyLength);
        				System.arraycopy(rightBottoms, 0, rightBottoms, distance, copyLength);
    				}
    			}   			 
    		} else if (distance < 0){
    			if (leftTops != null) {
    				int copyLength = leftTops.length + distance;
    				System.arraycopy(leftTops, -distance, leftTops, 0, copyLength);
    				System.arraycopy(leftBottoms, -distance, leftBottoms, 0, copyLength);
    				if (rightTops != null) {
        				System.arraycopy(rightTops, -distance, rightTops, 0, copyLength);
        				System.arraycopy(rightBottoms, -distance, rightBottoms, 0, copyLength);
    				}
    			}
    		}
    	}
    }

    /**
     * Clears the contents of both GeneralPath objects, if present.
     */
    public void reset() {
        if (mode == GENERAL_PATH_MODE) {
            if (firstPath != null) {
                firstPath.reset();
            }

            if (secondPath != null) {
                secondPath.reset();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("WavePart - ");
        b.append("start time: ");
        b.append(startTime);
        b.append(" stop time: ");
        b.append(stopTime);
        b.append(" ms.");

        return b.toString();
    }
}
