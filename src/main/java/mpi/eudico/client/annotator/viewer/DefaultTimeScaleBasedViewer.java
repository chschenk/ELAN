package mpi.eudico.client.annotator.viewer;

import java.awt.AlphaComposite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.util.TimeFormatter;


/**
 * 'Empty' base class for TimeScaleBased viewers in ELAN.  Functionality shared
 * by (most of) these classes is implemented here. This class could be
 * abstract, it is itself not a useful viewer. NB: 12-2005 If this works
 * TimeLineViewer and SignalViewer could extend  this class.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class DefaultTimeScaleBasedViewer extends TimeScaleBasedViewer
    implements ComponentListener, ActionListener, MouseListener,
        MouseMotionListener, MouseWheelListener {
    /** default value of milliseconds per pixel */
    public final int DEFAULT_MS_PER_PIXEL = 10;

    /** distance to left and right viewer boundary for auto-scrolling */
    public final int SCROLL_OFFSET = 16;

    /** the buffered image of the viewer */
    protected BufferedImage bi;

    /** the graphics object of the buffered image */
    protected Graphics2D big2d;

    /** the buf. image height */
    protected int imageWidth;

    /** the buf. image width */
    protected int imageHeight;

    /** the width in pixels of the interval */
    protected int intervalWidth;

    /** the font for the viewer */
    protected Font font;

    /** the fontmetrics for the viewer */
    protected FontMetrics metrics;

    /** height of the horizontal, time ruler */
    protected int rulerHeight;

    /** the hor. ruler object */
    protected TimeRuler ruler;

    /** the width of a vertical, amplitude ruler */
    protected int vertRulerWidth;

    /** a Transform object for (drawing in) the graphics object */
    protected AffineTransform identity;

    /** the 'current' time */
    protected long crossHairTime;

    /** the pixel (x-)position representing the current time */
    protected int crossHairPos;

    /** the time corresponding to the begin of the current interval */
    protected long intervalBeginTime;

    /** the time corresponding to the end of the current interval */
    protected long intervalEndTime;

    /** the begintime of the currently selected interval */
    protected long selectionBeginTime;

    /** the endtime of the currently selected interval */
    protected long selectionEndTime;

    /** the x-pos of the begin of the currently selected interval */
    protected int selectionBeginPos;

    /** the x-pos of the end of the currently selected interval */
    protected int selectionEndPos;
    
    /** a flag indicating if a selection is cleared by a single mouseclick */
    
    protected boolean clearSelOnSingleClick = true;

    /** the time value corresponding to the (x-)position where dragging started */
    protected long dragStartTime;

    /** the point where dragging started */
    protected Point dragStartPoint;

    /** the point where dragging ended */
    protected Point dragEndPoint;

    /** a transparency value */
    protected AlphaComposite alpha04;

	/** another transparency value */
	protected AlphaComposite alpha05;
	
    /** another transparency value */
    protected AlphaComposite alpha07;

    /** the current ms-per-pixel value (resolution) */
    protected float msPerPixel;

    /** a flag whether the TimeScale of this viewer is connected to another viewer */
    protected boolean timeScaleConnected;

    /** a flag whether horizontal panning with the mouse is going on */
    protected boolean panMode;
    /** a flag for the visibility of the time ruler */
    protected boolean timeRulerVisible;
    /** a flag for the attached/detached state. Attached means in the main 
     * application window */
    protected boolean attached;
    //popup

    /** the right mouse button popup menu */
    protected JPopupMenu popup;

    /** the zoom submenu group */
    protected ButtonGroup zoomBG;

    /** the zoom submenu */
    protected JMenu zoomMI;
    
    /** the time scale connected item */
    protected JCheckBoxMenuItem timeScaleConMI;
    /** the custom zoom level label item */
    protected JRadioButtonMenuItem customZoomMI;
    /** the zoom to selection item */
    protected JMenuItem zoomSelectionMI;
    /** the time ruler visibility item */
    protected JCheckBoxMenuItem timeRulerVisMI;
    
    /**
     * an offset in milliseconds into the media/data file where the new media
     * begin point (0 point) is situated
     */
    protected long mediaTimeOffset;
    protected int horScrollSpeed = 10;
    // buffered or unbuffered painting
    protected boolean useBufferedImage = false;

    /**
     * Creates a new DefaultTimeScaleBasedViewer instance.
     */
    public DefaultTimeScaleBasedViewer() {
        initViewer();
        useBufferedImage = SystemReporting.useBufferedPainting;
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        setDoubleBuffered(true);
        setOpaque(true);
    }

    /**
     * Performs the initialization of fields and sets up the viewer.
     */
    protected void initViewer() {
        setLayout(null);
        font = Constants.DEFAULTFONT;
        setFont(font);
        metrics = getFontMetrics(font);
        if (Constants.DEFAULT_LF_LABEL_FONT != null) {
        	ruler = new TimeRuler(Constants.deriveSmallFont(Constants.DEFAULT_LF_LABEL_FONT), 
        			TimeFormatter.toString(0));
        } else {
        	ruler = new TimeRuler(font, TimeFormatter.toString(0));
        }
        rulerHeight = ruler.getHeight();
        timeRulerVisible = true;
        vertRulerWidth = 43;
        msPerPixel = 10f;
        crossHairTime = 0L;
        crossHairPos = 0;
        selectionBeginTime = 0L;
        selectionEndTime = 0L;
        selectionBeginPos = 0;
        selectionEndPos = 0;
        dragStartTime = 0;
        alpha04 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
		alpha05 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        alpha07 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);

        //dragStartTime = 0;
        imageWidth = 0;
        imageHeight = 0;
        intervalWidth = 0;
        mediaTimeOffset = 0;
        identity = new AffineTransform();
    }

    /**
     * Layout information, gives the nr of pixels at the left of the viewer
     * panel that contains no time line information. Space used for a control/
     * info panel, vertical ruler etc.
     *
     * @return the nr of pixels at the left that contain no time line related
     *         data
     */
    public int getLeftMargin() {
        return 0;
    }

    /**
     * Layout information, gives the nr of pixels at the right of the viewer
     * panel that contains no time line information. Space for e.g. a
     * scrollbar.
     *
     * @return the nr of pixels at the right that contain no time line related
     *         data
     */
    public int getRightMargin() {
        return 0;
    }

    /**
     * Create a popup menu to enable the manipulation of some settings for this
     * viewer.
     */
    protected void createPopupMenu() {
        popup = new JPopupMenu("TimeScaleBasedViewer");
        zoomMI = new JMenu(ElanLocale.getString("TimeScaleBasedViewer.Zoom"));
        zoomBG = new ButtonGroup();
        zoomSelectionMI = new JMenuItem(
        		ElanLocale.getString("TimeScaleBasedViewer.Zoom.Selection"));
        zoomSelectionMI.addActionListener(this);
        zoomSelectionMI.setActionCommand("zoomSel");
        zoomMI.add(zoomSelectionMI);
        customZoomMI = new JRadioButtonMenuItem(
        		ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom"));
        customZoomMI.setEnabled(false);
        zoomBG.add(customZoomMI);
        zoomMI.add(customZoomMI);
        zoomMI.addSeparator();
        //
        JRadioButtonMenuItem zoomRB;

        for (int element : ZOOMLEVELS) {
            zoomRB = new JRadioButtonMenuItem(element + "%");
            zoomRB.setActionCommand(String.valueOf(element));
            zoomRB.addActionListener(this);
            zoomBG.add(zoomRB);
            zoomMI.add(zoomRB);

            if (element == 100) {
                zoomRB.setSelected(true);
            }
        }

        popup.add(zoomMI);

        timeRulerVisMI = new JCheckBoxMenuItem(ElanLocale.getString(
			"TimeScaleBasedViewer.TimeRuler.Visible"));
        timeRulerVisMI.setSelected(timeRulerVisible);
        timeRulerVisMI.addActionListener(this);
        popup.add(timeRulerVisMI);
        //popup.addSeparator();

		timeScaleConMI = new JCheckBoxMenuItem(ElanLocale.getString(
					"TimeScaleBasedViewer.Connected"), timeScaleConnected);
		timeScaleConMI.setActionCommand("connect");
		timeScaleConMI.addActionListener(this);
		popup.add(timeScaleConMI);
		
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        int zoom = (int) (100f * (10f / msPerPixel));

        if (zoom <= 0) {
            zoom = 100;
        }

        updateZoomPopup(zoom);
    }

    /**
     * The viewer can update (e.g. enable / disable items) the popup.
     *
     * @param p the point where the mouse is clicked/pressed
     */
    protected void updatePopup(Point p) {
    }

    /**
     * Updates the "zoom" menu item. Needed, when timeScaleConnected, after a
     * change of the zoomlevel in some other connected viewer.
     *
     * @param zoom the zoom level
     */
    protected void updateZoomPopup(float zoom) {
    	if (popup == null) {
    		return;
    	}
    	// rounding issues 74.999 == 75%, 149.99 == 150%
        if (zoom > 74.99f && zoom < 75f) {
        	zoom = 75f;
        } else if (zoom > 149.99 && zoom < 150f) {
        	zoom = 150f;
        }

        int zoomMenuIndex = -1;
        for (int i = 0; i < ZOOMLEVELS.length; i++) {
        	if (zoom == ZOOMLEVELS[i]) {
        		zoomMenuIndex = i;
        		break;
        	}
        }
        
        Enumeration<AbstractButton> en = zoomBG.getElements();
        int counter = 0;

        while (en.hasMoreElements()) {
            JRadioButtonMenuItem rbmi = (JRadioButtonMenuItem) en.nextElement();
            // +1 cause of the "custom" menu item
            if (counter == zoomMenuIndex + 1) { //rbmi.getActionCommand().equals(zoomLevel)
                rbmi.setSelected(true);
                
                break;
            } else {
                rbmi.setSelected(false);
            }

            counter++;
        }
        if (zoomMenuIndex == -1) {
        	customZoomMI.setSelected(true);
        	customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom") + " - " + zoom + "%");
        } else {
        	customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom"));
        }
        
    }

    /**
     * Zooms in to the next level of predefined zoomlevels.
     * Note: has to be adapted once custom zoomlevels are implemented.
     */
    protected void zoomIn() {
    	float zoom = 100 / (msPerPixel / 10);
    	// HS 08-2012 updated to match the implementation of the TimeLineViewer
    	float nz = zoom + 10;
    	float nm = ((100f / nz) * 10);
    	setMsPerPixel(nm);
    }
   
    /**
     * Zooms in to the next level of predefined zoomlevels.
     * Note: has to be adapted once custom zoomlevels are implemented.
     */
    protected void zoomOut() {
    	float zoom = 100 / (msPerPixel / 10);
    	// HS 08-2012 updated to match the implementation in the TimeLineViewer
    	float nz = zoom - 10;
    	if (nz < 10) {
    		nz = 10;
    	}
    	float nm = ((100f / nz) * 10);
    	setMsPerPixel(nm);
    	
    	/*
        // first find the closest match (there can be rounding issues)
        int zoomMenuIndex = -1;
        int diff = Integer.MAX_VALUE;

        for (int i = 0; i < ZOOMLEVELS.length; i++) {
            int d = Math.abs(ZOOMLEVELS[i] - (int) zoom);

            if (d < diff) {
                diff = d;
                zoomMenuIndex = i;
            }
        }
    	
    	if (zoomMenuIndex > 0) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex - 1];
    		float nextMsPerPixel = ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}
    	*/
    }
    
    protected void zoomToSelection() {
    	
    }
    
    /**
     * Returns the x-ccordinate for a specific time. The coordinate is in the
     * component's coordinate system.
     *
     * @param t time
     *
     * @return int the x-coordinate for the specified time
     */
    public int xAt(long t) {
        return (int) ((t - intervalBeginTime) / msPerPixel);
    }

    /**
     * Returns the time in ms at a given position in the current image. The
     * given x coordinate is in the component's ("this") coordinate system.
     * The interval begin time is included in the calculation of the time at
     * the given coordinate.
     *
     * @param x x-coordinate
     *
     * @return the mediatime corresponding to the specified position
     */
    public long timeAt(int x) {
        return intervalBeginTime + (int) (x * msPerPixel);
    }

    /**
     * Calculates the x coordinate in virtual image space.<br>
     * This virtual image would be an image of width <br>
     * media duration in ms / ms per pixel. Therefore the return value does
     * not correct for interval begin time and is not necessarily within the
     * bounds of this component.
     *
     * @param theTime the media time
     *
     * @return the x coordinate in the virtual image space
     */
    protected int timeToPixels(long theTime) {
        return (int) (theTime / msPerPixel);
    }

    /**
     * Calculates the time corresponding to a pixel location in the virtual
     * image space.
     *
     * @param x the x coordinate in virtual image space
     *
     * @return the media time at the specified point
     */
    protected long pixelToTime(int x) {
        return (long) (x * msPerPixel);
    }

    /**
     * Returns the current media offset, in ms.
     *
     * @return the current media offset
     */
    public long getMediaTimeOffset() {
        return mediaTimeOffset;
    }

    /**
     * Sets new media offset.
     *
     * @param offset the new media offset in ms
     */
    public void setMediaTimeOffset(long offset) {
        mediaTimeOffset = offset;
    }

    /**
     * Returns whether y coordinate (of the mouse) is in the horizontal ruler's
     * area.
     *
     * @param yPos y-coordinate of the mouse pointer
     *
     * @return true if the mouse pointer is in the hor. ruler, false otherwise
     */
    protected boolean pointInHorizontalRuler(int yPos) {
        return yPos < rulerHeight;
    }

    /**
     * @see mpi.eudico.client.annotator.viewer.TimeScaleBasedViewer#updateTimeScale()
     */
    @Override
	public void updateTimeScale() {
        if (timeScaleConnected) {
            //if the resolution is changed recalculate the begin time
            if (getGlobalTimeScaleMsPerPixel() != msPerPixel) {
                setLocalTimeScaleMsPerPixel(getGlobalTimeScaleMsPerPixel());
            } else if (getGlobalTimeScaleIntervalBeginTime() != intervalBeginTime) {
                //assume the resolution has not been changed
                setLocalTimeScaleIntervalBeginTime(getGlobalTimeScaleIntervalBeginTime());

                //System.out.println("update begin time in TimeLineViewer called");
            }
        }
    }

	/**
	 * Sets whether or not this viewer listens to global time scale updates.
	 *
	 * @param connected the new timescale connected value
	 */
	public void setTimeScaleConnected(boolean connected) {
		timeScaleConnected = connected;

		if (timeScaleConnected) {
			if (msPerPixel != getGlobalTimeScaleMsPerPixel()) {
				setLocalTimeScaleMsPerPixel(getGlobalTimeScaleMsPerPixel());
			}

			if (intervalBeginTime != getGlobalTimeScaleIntervalBeginTime()) {
				setLocalTimeScaleIntervalBeginTime(getGlobalTimeScaleIntervalBeginTime());
			}
		}
	}


	/**
	 * Returns whether this viewer listens to time scale updates from other
	 * viewers.
	 *
	 * @return true when connected to global time scale values, false otherwise
	 */
	public boolean getTimeScaleConnected() {
		return timeScaleConnected;
	}
	
	/**
	 * Returns the attached state.
	 * 
	 * @return true if the viewer is attached, i.e. part of the main window
	 */
    public boolean isAttached() {
		return attached;
	}

    /**
     * Sets the attached flag. In the detached state the viewer can not be time scale
     * connected.
     * 
     * @param attached if true, the viewer is part of the main window, otherwise 
     * it will reside in its own window
     */
	public void setAttached(boolean attached) {
		this.attached = attached;
		if (timeScaleConMI != null) {
			timeScaleConMI.setEnabled(attached);
			timeScaleConMI.setSelected(attached);
		}
		setTimeScaleConnected(attached);
	}

	/**
     * Returns the current interval begin time
     *
     * @return the current interval begin time
     */
    public long getIntervalBeginTime() {
        return intervalBeginTime;
    }

    /**
     * Returns the current interval end time
     *
     * @return the current interval end time
     */
    public long getIntervalEndTime() {
        return intervalEndTime;
    }

    /**
     * Checks whether this viewer is TimeScale connected and changes the
     * interval begin time globally or locally.
     *
     * @param begin the new interval begin time
     */
    public void setIntervalBeginTime(long begin) {
        if (timeScaleConnected) {
            setGlobalTimeScaleIntervalBeginTime(begin);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        } else {
            setLocalTimeScaleIntervalBeginTime(begin);
        }
    }

    /**
     * Calculates the new interval begin and/or end time.<p>
     * There are two special cases taken into account:<p>
     * 
     * <ul>
     * <li>
     * when the player is playing attempts are made to shift the interval
     * <i>n</i> times the interval size to the left or to the right, until the
     * new interval contains the new mediatime.
     * </li>
     * <li>
     * when the player is not playing and the new interval begin time coincides
     * with the selection begin time, the interval is shifted a certain offset
     * away from the image edge. Same thing when the interval end time
     * coincides with the selection end time.
     * </li>
     * </ul>
     * Note that this method is called from controllerUpdate() which is running
     * on a user thread, not on the Event Dispatching Thread.
     *
     * @param mediaTime
     */
    protected void recalculateInterval(final long mediaTime) {
        long newBeginTime = intervalBeginTime;
        long newEndTime = intervalEndTime;

    	if (intervalWidth == 0) {
    		intervalWidth = SCROLL_OFFSET;	// arbitrary non-zero value
    	}
        final long intervalMS = (long) (intervalWidth * msPerPixel);
		if (playerIsPlaying()) {
            // We might be in a selection outside the new interval.
            // Shift the interval n * intervalsize to the left or right
			// until newBeginTime <= mediaTime <= newEndTime.
            if (intervalEndTime < mediaTime) {
            	// Shift the interval right
                newBeginTime = intervalEndTime;
                newEndTime = newBeginTime + intervalMS;

                while (newEndTime < mediaTime) {
                    newBeginTime += intervalMS;
                    newEndTime += intervalMS;
                }
            } else if (mediaTime < intervalBeginTime) {
            	// Shift the interval left 
                newEndTime = intervalBeginTime;
                newBeginTime = newEndTime - intervalMS;

                while (mediaTime < newBeginTime) {
                    newBeginTime -= intervalMS;
                    newEndTime -= intervalMS;
                }

                if (newBeginTime < 0) {
                    newBeginTime = 0;
                    newEndTime = intervalMS;
                }
            } else {
                // the new time appears to be in the current interval after all
                return;
            }
        } else { //player is not playing

            // is the new media time to the left or to the right of the current interval
            if (mediaTime <= intervalBeginTime) {
                newBeginTime = mediaTime - (int) (SCROLL_OFFSET * msPerPixel);

                if (newBeginTime < 0) {
                    newBeginTime = 0;
                }

                newEndTime = newBeginTime + intervalMS;
            } else if (mediaTime >= intervalEndTime) {
                newEndTime = mediaTime + (int) (SCROLL_OFFSET * msPerPixel);
                newBeginTime = newEndTime - intervalMS;

                if (newBeginTime < 0) { // something would be wrong??
                    newBeginTime = 0;
                    newEndTime = newBeginTime + intervalMS;
                }
            }

            if ((newBeginTime == getSelectionBeginTime()) &&
                    (newBeginTime > (SCROLL_OFFSET * msPerPixel))) {
                newBeginTime -= (SCROLL_OFFSET * msPerPixel);
                newEndTime = newBeginTime + intervalMS;
            }

            if (newEndTime == getSelectionEndTime()) {
                newEndTime += (SCROLL_OFFSET * msPerPixel);
                newBeginTime = newEndTime - intervalMS;

                if (newBeginTime < 0) { // something would be wrong??
                    newBeginTime = 0;
                    newEndTime = newBeginTime + intervalMS;
                }
            }
        }

        if (timeScaleConnected) {
            //System.out.println("SV new begin time: " + newBeginTime);
            //System.out.println("SV new end time: " + newEndTime);
            setGlobalTimeScaleIntervalBeginTime(newBeginTime);
            setGlobalTimeScaleIntervalEndTime(newEndTime);
        } else {
            setLocalTimeScaleIntervalBeginTime(newBeginTime);
        }
    }

    /**
     * To be implemented by each extending class.
     *
     * @param begin the interval begintime
     */
    protected void setLocalTimeScaleIntervalBeginTime(long begin) {
    }

    /**
     * To be implemented by each extending class.
     *
     * @param step new msPerPixel value
     */
    protected void setLocalTimeScaleMsPerPixel(float step) {
    }
    
    /**
     * Changes the visibility of the time ruler.
     * 
     * @param visible
     */
    protected void setTimeRulerVisible(boolean visible) {
    	timeRulerVisible = visible;
		if (timeRulerVisible) {
			rulerHeight = ruler.getHeight();
		} else {
			rulerHeight = 0;
		}
    }

    /**
     * Checks whether this viewer is TimeScale connected and changes the
     * milliseconds per pixel value globally or locally.
     *
     * @param mspp the new milliseconds per pixel value
     */
    public void setMsPerPixel(float mspp) {
        if (timeScaleConnected) {
            setGlobalTimeScaleMsPerPixel(mspp);
            setGlobalTimeScaleIntervalBeginTime(intervalBeginTime);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        } else {
            setLocalTimeScaleMsPerPixel(mspp);
        }
    }

    /**
	 * Note that this method may running on a user thread, not on the Event
	 * Dispatching Thread.
	 * 
	 * @see mpi.eudico.client.annotator.viewer.AbstractViewer#controllerUpdate(mpi.eudico.client.mediacontrol.ControllerEvent)
	 */
    @Override
	public synchronized void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent || event instanceof StopEvent) {
            crossHairTime = getMediaTime();

            if (!playerIsPlaying()) {
                //if (scroller == null) { to do: drag scrolling
                recalculateInterval(crossHairTime);
                crossHairPos = xAt(crossHairTime);
                repaint();

                //} else {
                //	recalculateInterval(crossHairTime);
                //}
            } else {
                if ((crossHairTime < intervalBeginTime) ||
                        (crossHairTime > intervalEndTime)) {
                    recalculateInterval(crossHairTime);
                } else {
                    // repaint a part of the viewer
                    int oldPos = crossHairPos;
                    crossHairPos = xAt(crossHairTime);

                    int newPos = crossHairPos;

                    if (useBufferedImage) {
	                    if (newPos >= oldPos) {
	                        repaint(oldPos - 2, 0, newPos - oldPos + 4, getHeight());
	
	                        //repaint();
	                    } else {
	                        repaint(newPos - 2, 0, oldPos - newPos + 4, getHeight());
	
	                        //repaint();
	                    }
                    } else {
                    	repaint();
                    }
                }
            }
        }
    }

    /**
     * Gets the selection begintime and endtime and calculates pixel positions.
     *
     * @see mpi.eudico.client.annotator.viewer.AbstractViewer#updateSelection()
     */
    @Override
	public void updateSelection() {
        selectionBeginPos = (int) (getSelectionBeginTime() / msPerPixel);
        selectionEndPos = (int) (getSelectionEndTime() / msPerPixel);
    }

    /**
     * @see mpi.eudico.client.annotator.viewer.AbstractViewer#updateActiveAnnotation()
     */
    @Override
	public void updateActiveAnnotation() {
        // stub		
    }

    /**
     * Update menu item labels; here only the zoom menu is updated.
     *
     * @see mpi.eudico.client.annotator.viewer.AbstractViewer#updateLocale()
     */
    @Override
	public void updateLocale() {
        if (popup != null) {
            zoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom"));
            timeScaleConMI.setText(ElanLocale.getString(
				"TimeScaleBasedViewer.Connected"));
        }
    }

    /**
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    @Override
	public void componentHidden(ComponentEvent e) {
        // stub		
    }

    /**
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    @Override
	public void componentMoved(ComponentEvent e) {
        //  stub		
    }

    /**
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    @Override
	public void componentResized(ComponentEvent e) {
        // stub		
    }

    /**
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    @Override
	public void componentShown(ComponentEvent e) {
        // stub		
    }

    /**
     * Handle zoom menu events.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == zoomSelectionMI) {
    		zoomToSelection();
    	} else if (e.getActionCommand().equals("connect")) {
			boolean connected = ((JCheckBoxMenuItem) e.getSource()).getState();
			setTimeScaleConnected(connected);
		} else if (e.getSource() == timeRulerVisMI) {
			setTimeRulerVisible(timeRulerVisMI.isSelected());
    	} else {		
	        /* the rest are zoom menu items*/
	        String zoomString = e.getActionCommand();
	        int zoom = 100;
	
	        try {
	            zoom = Integer.parseInt(zoomString);
	        } catch (NumberFormatException nfe) {
	            System.err.println("Error parsing the zoom level");
	
	            return;
	        }
	
	        float newMsPerPixel = ((100f / zoom) * 10);
	        setMsPerPixel(newMsPerPixel);
		}
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            return;
        }
        
        if ((e.getClickCount() == 1) && e.isShiftDown()) {
            // change the selection interval
            if (getSelectionBeginTime() != getSelectionEndTime()) {
                long clickTime = timeAt(e.getPoint().x);

                if (clickTime > getSelectionEndTime()) {
                    // expand to the right
                    setSelection(getSelectionBeginTime(), clickTime);
                } else if (clickTime < getSelectionBeginTime()) {
                    // expand to the left
                    setSelection(clickTime, getSelectionEndTime());
                } else {
                    // reduce from left or right, whichever boundary is closest
                    // to the click time
                    if ((clickTime - getSelectionBeginTime()) < (getSelectionEndTime() -
                            clickTime)) {
                        setSelection(clickTime, getSelectionEndTime());
                    } else {
                        setSelection(getSelectionBeginTime(), clickTime);
                    }
                }
            } else {
            	// create a selection from media time to click time
            	long clickTime = timeAt(e.getPoint().x);
            	long medTime = getMediaTime();
            	if (clickTime > medTime) {
            		setSelection(medTime, clickTime);
            	} else if (clickTime < medTime) {
            		setSelection(clickTime, medTime);
            	}
            }
        } else {
        	setMediaTime(timeAt(e.getPoint().x));
        } 
        
	
	    // Selection clearing	    
	    boolean signalSel = getSelectionBeginTime() != 0
		    && getSelectionEndTime() != 0;
	    // signalSel: true if and only if an audio signal is selected

	    // Skip clearing the selection if shift is down, i.e. if the selection 
	    // is being changed, or if a popup has been created by clicking right.

	    if (clearSelOnSingleClick && signalSel && e.getClickCount() == 1
		    && !e.isShiftDown() && !getViewerManager().getMediaPlayerController().getSelectionMode()) {
	    	setSelection(0, 0);
		
	    	// For clicks that close the popup not popup.isShowing()
	    	// therefore, we cannot avoid these clicks clearing the 
	    	// selection as well.
	    }
        
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseEntered(MouseEvent e) {
        // stub		
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseExited(MouseEvent e) {
        // stub		
    }

    /**
     * Handles the popmenu positioning.
     *
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
	public void mousePressed(MouseEvent e) {
        Point pp = e.getPoint();

        // HS nov 04: e.isPopupTrigger always returns false on my PC...
        if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            if (popup == null) {
                createPopupMenu();
            }

            updatePopup(pp);

            if ((popup.getWidth() == 0) || (popup.getHeight() == 0)) {
                popup.show(this, pp.x, pp.y);
            } else {
                popup.show(this, pp.x, pp.y);
                SwingUtilities.convertPointToScreen(pp, this);

                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                Window w = SwingUtilities.windowForComponent(this);

                if ((pp.x + popup.getWidth()) > d.width) {
                    pp.x -= popup.getWidth();
                }

                //this does not account for a desktop taskbar
                if ((pp.y + popup.getHeight()) > d.height) {
                    pp.y -= popup.getHeight();
                }

                //keep it in the window then
                if ((pp.y + popup.getHeight()) > (w.getLocationOnScreen().y +
                        w.getHeight())) {
                    pp.y -= popup.getHeight();
                }

                popup.setLocation(pp);
            }

            return;
        }

        if (playerIsPlaying()) {
            stopPlayer();
        }

        dragStartPoint = e.getPoint();
        dragStartTime = timeAt(dragStartPoint.x);

        if (e.isAltDown() && pointInHorizontalRuler(dragStartPoint.y)) {
            dragStartTime = timeAt(dragStartPoint.x);
            panMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else {
            panMode = false;

            /* just to be sure a running scroll thread can be stopped */

            //stopScroll();
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseReleased(MouseEvent e) {
        // changing the selection might have changed the intervalBeginTime
        if (timeScaleConnected) {
            setGlobalTimeScaleIntervalBeginTime(intervalBeginTime);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        }

        if (panMode) {
            panMode = false;
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            return;
        }

        dragEndPoint = e.getPoint();

        if (panMode) {
            int scrolldiff = dragEndPoint.x - dragStartPoint.x;

            // some other viewer may have a media offset...
            long newTime = intervalBeginTime - (int) (scrolldiff * msPerPixel);

            if ((intervalBeginTime < 0) && (newTime < intervalBeginTime)) {
                newTime = intervalBeginTime;
            }

            setIntervalBeginTime(newTime);
            dragStartPoint = dragEndPoint;

            return;
        }

        // only the 'normal' drag-selection implemented here
        if (timeAt(dragEndPoint.x) > dragStartTime) { //left to right
            selectionEndTime = timeAt(dragEndPoint.x);

            if (selectionEndTime > getMediaDuration()) {
                selectionEndTime = getMediaDuration();
            }

            selectionBeginTime = dragStartTime;

            if (selectionBeginTime < 0) {
                selectionBeginTime = 0L;
            }

            if (selectionEndTime < 0) {
                selectionEndTime = 0L;
            }

            setMediaTime(selectionEndTime);
        } else { //right to left
            selectionBeginTime = timeAt(dragEndPoint.x);

            if (selectionBeginTime > getMediaDuration()) {
                selectionBeginTime = getMediaDuration();
            }

            selectionEndTime = dragStartTime;

            if (selectionEndTime > getMediaDuration()) {
                selectionEndTime = getMediaDuration();
            }

            if (selectionBeginTime < 0) {
                selectionBeginTime = 0L;
            }

            if (selectionEndTime < 0) {
                selectionEndTime = 0L;
            }

            setMediaTime(selectionBeginTime);
        }

        setSelection(selectionBeginTime, selectionEndTime);

        updateSelection();
        repaint();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseMoved(MouseEvent e) {
        // stub
    }
    
	/**
	 * The use of a mousewheel needs Java 1.4!<br>
	 * Typically scroll vertically.
	 * With Ctrl down zoom in or out.
	 * 
	 * @param e the event
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
    	if (e.getWheelRotation() == 0) {
    		return;
    	}
    	if (e.isControlDown()) {
    		if (e.getUnitsToScroll() > 0) {
    			zoomOut();
    		} else {
    			zoomIn();
    		}
    		return;
    	} else if (e.isShiftDown()) {// on Mac this is the same as hor. scroll with two fingers on the trackpad
	        if (e.getWheelRotation() != 0) {
	        	int timeDiff = (int) (horScrollSpeed * e.getWheelRotation() * msPerPixel);// 2: arbitrary acceleration of the gesture
	        	long newTime = intervalBeginTime + timeDiff;
	        	if (newTime != intervalBeginTime && !(intervalBeginTime < 0 && newTime < intervalBeginTime)) {
	        		setIntervalBeginTime(newTime);
	        	}
	        }
	        return;
        }
	}

	@Override
	public void preferencesChanged() {
		// method stub
	    
		Boolean boolPref = Preferences.getBool("ClearSelectionOnSingleClick",null);
		if (boolPref instanceof Boolean){
		    clearSelOnSingleClick = boolPref.booleanValue();
		}
		
		Integer intPref = Preferences.getInt("Preferences.TimeLine.HorScrollSpeed", null);
		if (intPref instanceof Integer) {
			horScrollSpeed = intPref;
		}
		
		boolPref = Preferences.getBool("UI.UseBufferedPainting", null);
		if (!SystemReporting.isBufferedPaintingPropertySet && boolPref != null) {
			useBufferedImage = boolPref;
		}
	}
	// Zoomable interface
	@Override
	public void zoomInStep() {
		float zoom = 100 / (msPerPixel / 10);
    	// if zoom is already larger than the max predefined level either
    	// return or add a fixed percentage?
    	if (zoom >= ZOOMLEVELS[ZOOMLEVELS.length - 1]) {
    		return;
    	}
        int zoomMenuIndex = -1;
    	// find between which levels the current value is, go to the larger one
    	for (int i = 0; i < ZOOMLEVELS.length; i++) {
    		if (zoom < ZOOMLEVELS[i]) {
    			zoomMenuIndex = i;
    			break;
    		} else if (zoom == ZOOMLEVELS[i]) {
    			zoomMenuIndex = i + 1;
    		}
    	}
    	if (zoomMenuIndex >= 0 && zoomMenuIndex < ZOOMLEVELS.length) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex];
    		float nextMsPerPixel = ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}
		
	}

	@Override
	public void zoomOutStep() {
		float zoom = 100 / (msPerPixel / 10);
    	// if zoom is already smaller than the min predefined level return
    	if (zoom <= ZOOMLEVELS[0]) {
    		return;
    	}
        int zoomMenuIndex = -1;
    	// find between which levels the current value is, go to the larger one
    	for (int i = ZOOMLEVELS.length -1; i >= 0 ; i--) {
    		if (zoom > ZOOMLEVELS[i]) {
    			zoomMenuIndex = i;
    			break;
    		} else if (zoom == ZOOMLEVELS[i]) {
    			zoomMenuIndex = i - 1;
    		}
    	}
    	if (zoomMenuIndex >= 0 && zoomMenuIndex < ZOOMLEVELS.length) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex];
    		float nextMsPerPixel = ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}		
	}

	/**
	 * Zoomable interface, zooms to 10 ms per pixel.
	 */
	@Override
	public void zoomToDefault() {
		if ((int) msPerPixel != DEFAULT_MS_PER_PIXEL) {
    		int nextZoom = 100; // 100%
    		float nextMsPerPixel = DEFAULT_MS_PER_PIXEL;
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
		}		
	}

}
