package mpi.eudico.client.annotator.timeseries;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
//import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.Constants;


/**
 * Implementation of a track panel capable of displaying multiple time series tracks.
 */
public class TSTrackPanelImpl implements TSTrackPanel {
    /** a list of timeseries tracks */
    private List<AbstractTSTrack> tracks;

    /** a vertical ruler */
    private TSRulerImpl vertRuler;
    private int rulerWidth;
    private float zoomLevel;
    private float msPerPixel;
    private int height;
    private int width;
    private Insets margin;
    private Rectangle trackRect;
	private int[] tickYPos = new int[3];
	// a constant for invalid values
	private final int INVALID = (int) Math.pow(2, 30);

    /**
     *
     */
    public TSTrackPanelImpl() {
        super();

        tracks = new ArrayList<AbstractTSTrack>(4);
        vertRuler = new TSRulerImpl();
        vertRuler.setTrackPanel(this);
        margin = new Insets(3, 3, 3, 3);
        rulerWidth = 40;
        vertRuler.setWidth(rulerWidth);
		trackRect = new Rectangle();
    }

    /**
     * Sets the width for the vertical ruler.
     *
     * @param rulerWidth the width for the vertical ruler
     */
    public void setRulerWidth(int rulerWidth) {
        this.rulerWidth = rulerWidth;

        vertRuler.setWidth(rulerWidth);
        trackRect.x = margin.left + rulerWidth;
    }

    /**
     * Returns the current width of the vertical ruler.
     *
     * @return the width of the vertical ruler
     */
    public int getRulerWidth() {
        return rulerWidth;
    }

    /**
     * Replaces the vertical ruler.
     *
     * @param ruler the new vertical ruler
     */
    public void setRuler(TSRuler ruler) {
        vertRuler = (TSRulerImpl) ruler;
        vertRuler.setTrackPanel(this);
        vertRuler.setWidth(rulerWidth);
        vertRuler.setHeight(height - margin.top - margin.bottom);
		trackRect.x = margin.left + rulerWidth;
    }

    /**
     * Returns the vertical ruler.
     *
     * @return the vertical ruler
     */
    public TSRulerImpl getRuler() {
        return vertRuler;
    }

    /**
     * Paints the ruler on the specified graphics object.
     *
     * @param g2d the graphics object for rendering
     */
    public void paint(Graphics2D g2d, long intervalBeginTime) {
    	g2d.setColor(Color.WHITE);
    	g2d.fillRect(0, 0, width, height);
    	
    	g2d.setColor(Color.BLACK);
    	//g2d.drawRect(margin.left + rulerWidth, margin.top, 
    	//	width - margin.left - margin.right - rulerWidth, height - margin.top - margin.bottom);
		g2d.drawRect(trackRect.x, trackRect.y - 1, trackRect.width, trackRect.height + 2);
			
    	g2d.translate(margin.left, margin.top);
    	vertRuler.paint(g2d);
    	g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
    	tickYPos = vertRuler.getTickYPositions();
    	for (int i = 0; i < tickYPos.length; i++) {
    		g2d.drawLine(trackRect.x, tickYPos[i], trackRect.x + trackRect.width, tickYPos[i]);	
    	}
    	g2d.translate(trackRect.x, 0);
    	
    	g2d.setClip(0, 0, trackRect.width, trackRect.height);
    	for (int i = 0; i < tracks.size(); i++) {
    		paintTrack(g2d, tracks.get(i), intervalBeginTime, trackRect.width, trackRect.height);
    	}
    	g2d.setClip(null);
	g2d.translate(-margin.left - trackRect.x, -margin.top);
    }

	/**
	 * Renders track data to the specified Graphics context. Delegates rendering to one 
	 * of the data format specific methods.
	 * 
	 * @param g2d the Graphics context
	 * @param track the time series track to render.
	 * @param beginTime interval begin time
	 * @param w the width of the paint area
	 * @param h the height of the paint area
	 */
	public void paintTrack(Graphics2D g2d, AbstractTSTrack track, long beginTime, int w, 
		int h) {
		if (g2d == null || track == null || w <= 0 || h <= 0) {
			return;	
		}

		switch (track.getType()) {
			case TimeSeriesTrack.VALUES_INT_ARRAY:
				//paintIntArrayTrack(g2d, track, beginTime, width, height);
				break;
			case TimeSeriesTrack.VALUES_FLOAT_ARRAY:
				paintFloatArrayTrack(g2d, track, beginTime, w, h);
				break;
			case TimeSeriesTrack.VALUES_DOUBLE_ARRAY:
				//paintDoubleArrayTrack(g2d, track, beginTime, width, height);
				break;
			case TimeSeriesTrack.TIME_VALUE_LIST:
				paintTimeValueTrack(g2d, track, beginTime, w, h);
			default:
			    return;
		}
	}
	
	/**
	 * Renders a track that stores its data in a float array.
	 * Null checking has been done in paintTrack().
	 * 
	 * @param g2d the Graphics context
	 * @param track the time series track to render.
	 * @param beginTime interval begin time
	 * @param w the width of the paint area
	 * @param h the height of the paint area
	 * @see #paintTrack(Graphics2D, AbstractTSTrack, long, int, int)
	 */
	private void paintFloatArrayTrack(Graphics2D g2d, AbstractTSTrack track, 
		long beginTime, int w, int h) {
		if (w <=0 || h <= 0) {
			return;
		}
		g2d .setColor(track.getColor());
		float[] data = (float[]) track.getData();
		float[] range = vertRuler.getRange();
		float scaleUnit = h / (range[1] - range[0]);
		long endTime = beginTime + (long)(w * msPerPixel);
		int xShift = (int)(beginTime / msPerPixel) + (int)(track.timeOffset / msPerPixel);
		int beginIndex = track.getIndexForTime(beginTime);
		int endIndex = track.getIndexForTime(endTime);
		float samplesPerPixel = msPerPixel * (track.getSampleRate() / 1000);

		int x1 = 0, x2 = -1, y1 = 0, y2 = 0;
		if (samplesPerPixel > 1) {
			// per pixel calculation
			int index1 = beginIndex;
			int index2 = index1;
			for (int i = 0; i <= w; i++) {
				float val = 0;
				boolean validPix = false;
				index1 = track.getIndexForTime(beginTime + (int)(i * msPerPixel));
				index2 = track.getIndexForTime(beginTime + (int)((i + 1) * msPerPixel));
				if (index1 < 0 || index2 < 0 || index1 == index2) {
					continue;
				}
				if (index2 >= data.length) {
					break;
				}
				for (int j = index1; j < index2; j++) {
					if (!Float.isNaN(data[j])) {
						val += data[j];
						validPix = true;
					}
				}
				// calculate average
				val /= (index2 - index1);

				if (x2 == -1) {
					// first point
					x2 = i;
					if (validPix) {
						y2 = (int) (scaleUnit * (range[1] - val));
					} else {
						y2 = INVALID;
					}
				} else {
					x1 = x2;
					y1 = y2;
					x2 = i;
					if (validPix) {
						y2 = (int) (scaleUnit * (range[1] - val));
					} else {
						y2 = INVALID;
					}
					if (y1 != INVALID && y2 != INVALID) {
						g2d.drawLine(x1, y1, x2, y2);
					}
				}
			}
		} else {
			// per sample calculation
			float pixelPerSample = 1 / samplesPerPixel;
			
			for (int i = beginIndex; i <= endIndex && i < data.length; i++) {
				if (i < 0) {
					continue;
				}
				float v = data[i];
				if (x2 == -1) {
					// first point
					x2 = (int)(i * pixelPerSample) - xShift;
					if (!Float.isNaN(v)) {
						y2 = (int) (scaleUnit * (range[1] - v));
					} else {
						y2 = INVALID;
					}
				} else {
					x1 = x2;
					y1 = y2;
					x2 = (int)(i * pixelPerSample) - xShift;
					if (!Float.isNaN(v)) {
						y2 = (int) (scaleUnit * (range[1] - v));
					} else {
						y2 = INVALID;
					}
					if (y1 != INVALID && y2 != INVALID) {
						g2d.drawLine(x1, y1, x2, y2);
					}
				}
			}
		}
	}

	/**
	 * Renders a track that stores its data in a TimeValue List, like a NonContinuousRateTSTrack.
	 * Null checking has been done in paintTrack().
	 * 
	 * @param g2d the Graphics context
	 * @param track the time series track to render.
	 * @param beginTime interval begin time
	 * @param w the width of the paint area
	 * @param h the height of the paint area
	 * @see #paintTrack(Graphics2D, AbstractTSTrack, long, int, int)
	 */
	private void paintTimeValueTrack(Graphics2D g2d, AbstractTSTrack track, long beginTime, int w, int h) {
		if (w <=0 || h <= 0) {
			return;
		}
	    TimeValue tv;
	    g2d .setColor(track.getColor());
		List<TimeValue> data = (List<TimeValue>)track.getData();
		float[] range = vertRuler.getRange();
		float scaleUnit = h / (range[1] - range[0]);
		long endTime = beginTime + (long)(w * msPerPixel);
		int xShift = (int)(beginTime / msPerPixel) + (int)(track.timeOffset / msPerPixel);
		
		int beginIndex = 0;
		if (beginTime >= 0) {
		    beginIndex = track.getIndexForTime(beginTime);
		} else {
		    beginIndex = track.getIndexForTime(0);
		}

		int endIndex = track.getIndexForTime(endTime);
		// if the time at end index is less than endtime take the next index, if it exists
		if (endIndex >= 0 && endIndex < data.size() - 1) {
		    tv = (TimeValue) data.get(endIndex);
		    if (tv.time < endTime) {
		        endIndex++;
		    }
		}
		
		int x1 = 0, x2 = -1, y1 = 0, y2 = 0;
		
		for (int i = beginIndex; i <= endIndex; i++) {
		    tv = (TimeValue) data.get(i);
		
			if (x2 == -1) {
				// first point
				x2 = (int)(tv.time / msPerPixel) - xShift;
				if (!Float.isNaN(tv.value)) {
					y2 = (int) (scaleUnit * (range[1] - tv.value));
				} else {
					y2 = INVALID;
				}
			} else {
				x1 = x2;
				y1 = y2;
				x2 = (int)(tv.time / msPerPixel) - xShift;
				if (!Float.isNaN(tv.value)) {
					y2 = (int) (scaleUnit * (range[1] - tv.value));
				} else {
					y2 = INVALID;
				}
				
				if (!(tv instanceof TimeValueStart)) {
					if (y1 != INVALID && y2 != INVALID) {
						g2d.drawLine(x1, y1, x2, y2);    
					}
				}
			}
		}
	}
	
    /**
     * Sets the vertical zoom level.
     *
     * @param vertZoom the vertical zoom level
     */
    public void setVerticalZoom(float vertZoom) {
        zoomLevel = vertZoom;
    }

    /**
     * Returns the current vertical zoomlevel.
     *
     * @return the current vertical zoomlevel
     */
    public float getVerticalZoom() {
        return zoomLevel;
    }

    /**
     * Adds a track to the panel.
     *
     * @param track the track to add to the panel
     *
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#addTrack(mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack)
     */
    @Override
	public void addTrack(AbstractTSTrack track) {
        tracks.add(track);
    }

    /**
     * Removes a track from the panel.
     *
     * @param track the track to remove from the panel
     *
     * @return true if the track was in the list of tracks and has been
     *         removed, false otherwise
     *
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#removeTrack(mpi.eudico.client.annotator.timeseries.AbstractTSTrack)
     */
    @Override
	public boolean removeTrack(AbstractTSTrack track) {
        return tracks.remove(track);
    }

    /**
     * Removes a track identified by trackID from the panel.
     *
     * @param trackID the name or id of the track to remove from the panel
     *
     * @return true if the track was in the list of tracks and has been
     *         removed, false otherwise
     *
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#removeTrack(java.lang.String)
     */
    @Override
	public boolean removeTrack(String trackID) {
        if (trackID == null) {
            return false;
        }

        AbstractTSTrack track = getTrack(trackID);

        if (track != null) {
            return tracks.remove(track);
        }

        return false;
    }

    /**
     * Returns the track identified by trackID.
     *
     * @param trackID the name or id of the track
     *
     * @return the track, or null if not present in the list
     *
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#getTrack(java.lang.String)
     */
    @Override
	public AbstractTSTrack getTrack(String trackID) {
        AbstractTSTrack track = null;
        AbstractTSTrack tr = null;

        for (int i = 0; i < tracks.size(); i++) {
            tr = tracks.get(i);

            if (tr.getName().equals(trackID)) {
                track = tr;

                break;
            }
        }

        return track;
    }

    /**
     * Returns the list of tracks in this panel.
     *
     * @return the list of tracks
     *
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#getTracks()
     */
    @Override
	public List<AbstractTSTrack> getTracks() {
        return tracks;
    }

    /**
     * Sets the display height for this panel.
     *
     * @param height the new height
     *
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#setHeight(int)
     */
    @Override
	public void setHeight(int height) {
        this.height = height;
        vertRuler.setHeight(height - margin.top - margin.bottom);
        trackRect.height = height - margin.top - margin.bottom;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#getHeight()
     */
    @Override
	public int getHeight() {
        return height;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#setWidth(int)
     */
    @Override
	public void setWidth(int width) {
        this.width = width;
        trackRect.width = width - rulerWidth - margin.left - margin.right;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#getWidth()
     */
    @Override
	public int getWidth() {
        return width;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#setMargin(int[])
     */
    @Override
	public void setMargin(Insets margin) {
        this.margin = margin;
        vertRuler.setHeight(height - margin.top - margin.bottom);
        trackRect.x = margin.left;
        trackRect.y = margin.top;
		trackRect.width = width - rulerWidth - margin.left - margin.right;
		trackRect.height = height - margin.top - margin.bottom;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSTrackPanel#getMargin()
     */
    @Override
	public Insets getMargin() {
        return margin;
    }
    
	/**
	 * Returns the current resolution, or number of milliseconds per pixel.
	 * @return number of milliseconds per pixel
	 */
	public float getMsPerPixel() {
		return msPerPixel;
	}

	/**
	 * Sets the resolution or number of milliseconds per pixel.
	 * @param msPerPixel the number of ms that each pixel represents
	 */
	public void setMsPerPixel(float msPerPixel) {
		this.msPerPixel = msPerPixel;
	}

}
