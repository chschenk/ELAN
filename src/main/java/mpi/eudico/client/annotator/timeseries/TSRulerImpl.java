package mpi.eudico.client.annotator.timeseries;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import mpi.eudico.client.annotator.Constants;


/**
 * Implementation of a vertical ruler for timeseries data. Paints ticks and
 * values of the extremes, the mid point and  the 'zero' point, in case min is
 * less than zero. And maybe some minor ticks as well.
 */
public class TSRulerImpl implements TSRuler {
    private int height;
    private int width;
    private float zoom;
    private float[] range;
    private String units;
    private Color foreground;
    private Font font;
    private int[] tickYPos = new int[3];

    /** Holds value of property DOCUMENT ME! */
    private final int majorTickWidth = 5;

    /** Holds value of property DOCUMENT ME! */
    private final int minorTickWidth = 3;

    /** the track panel this ruler is connected to */
    private TSTrackPanelImpl trackPanel;

    /**
     * Creates a new TSRulerImpl instance with default range, width and height.
     */
    public TSRulerImpl() {
        this(new float[]{0, 100});
    }

	/**
	 * Creates a new TSRulerImpl instance with default range, width and height.
	 */
	public TSRulerImpl(float[] range) {
		setRange(range);
	}
	
    /**
     * Creates a new TSRulerImpl instance
     *
     * @param range the initial range of the ruler
     * @param width the width of the ruler
     * @param height the height of the ruler
     */
    public TSRulerImpl(float[] range, int width, int height) {
        this.width = width;
        this.height = height;
        setRange(range);
    }

    /**
     * Returns the current range, min - max.
     *
     * @return the current min and max value of the ruler
     */
    @Override
	public float[] getRange() {
        return range;
    }

    /**
     * Sets the current range, min - max.
     *
     * @param range the new range
     */
    @Override
	public void setRange(float[] range) {
        if (range != null) {
            this.range = range;
        }
    }

    /**
     * Returns a String representation of the ruler's units, like m/s.
     *
     * @return a String representation of the ruler's units
     */
    @Override
	public String getUnitString() {
        return units;
    }

    /**
     * Sets the String representation of the ruler's units.
     *
     * @param unitString the String representation of the ruler's units
     */
    @Override
	public void setUnitString(String unitString) {
        units = unitString;
    }

    /**
     * Paints the ruler to the specified Graphics object, at the specified
     * location.
     *
     * @param g2d the Graphics object
     */
    public void paint(Graphics2D g2d) {
        if (range == null) {
            return;
        }

        if (foreground != null) {
            g2d.setColor(foreground);
        }

        if (font != null) {
            g2d.setFont(font);
        }
        float unitLabelRX = 0f;
        // how many labels maximally fit in the available vert. space
        int numLabels = (int) (height / (g2d.getFont().getSize2D() + 2));
        g2d.drawLine(width - majorTickWidth, 0, width, 0);
        int lw = width - majorTickWidth - 2;
        String label = getLabelString(range[1], g2d.getFontMetrics(), lw);
        if (numLabels > 0) {
	        g2d.drawString(label,
	            lw - g2d.getFontMetrics(g2d.getFont()).stringWidth(label),
	            g2d.getFont().getSize2D());
        }
        g2d.drawLine(width - majorTickWidth, height, width, height);
        if(numLabels > 1) {
	        label = getLabelString(range[0], g2d.getFontMetrics(), lw);
	        unitLabelRX = lw - g2d.getFontMetrics(g2d.getFont()).stringWidth(label);
	        g2d.drawString(label,
	        		unitLabelRX, height);
	        
        }

		float ym;
        if ((range[0] < 0) && (range[1] > 0)) {
            // find the pixel for 0
            ym = (height * range[1]) / (range[1] - range[0]);
            tickYPos[0] = (int) ym;
            g2d.drawLine(width - majorTickWidth, tickYPos[0], width, tickYPos[0]);
            if (numLabels > 2 && 
            		(ym > 1.5 * g2d.getFont().getSize2D() + 2 && 
            				ym < height - 1.5 * g2d.getFont().getSize2D() - 2)) {
	            label = "0";
	            g2d.drawString(label,
	                lw - g2d.getFontMetrics(g2d.getFont()).stringWidth(label),
	                ym + (g2d.getFont().getSize2D() / 2));
            }

            // minor tick halfway max and 0
			tickYPos[1] = (int) ym / 2;
            g2d.drawLine(width - minorTickWidth, tickYPos[1], width,
			tickYPos[1]);

            // minor tick halfway 0 and min
			tickYPos[2] = (int) (ym + ((height - ym) / 2));
            g2d.drawLine(width - minorTickWidth,
				tickYPos[2], width, tickYPos[2]);
        } else {
            // find pixel for mid value
            ym = height / 2f;
			tickYPos[0] = (int) ym;
			tickYPos[1] = height / 4;
			tickYPos[2] = (int) ((3 * height) / 4f);
			
            g2d.drawLine(width - minorTickWidth, tickYPos[1], width, tickYPos[1]);
            g2d.drawLine(width - majorTickWidth, tickYPos[0], width, tickYPos[0]);
            if (numLabels > 2 && 
            		(ym > 1.5 * g2d.getFont().getSize2D() + 2 && 
    				ym < height - 1.5 * g2d.getFont().getSize2D() - 2)) {
				label = getLabelString((range[0] + range[1]) / 2, g2d.getFontMetrics(), lw);
	            g2d.drawString(label,
	                lw - g2d.getFontMetrics(g2d.getFont()).stringWidth(label),
	                ym + g2d.getFont().getSize2D() / 2);
            }
            g2d.drawLine(width - minorTickWidth, tickYPos[2],
                width, tickYPos[2]);
        }

        // paint unit label
        if (units != null && units.length() > 0) {
        	// rotated
        	/*
            AffineTransform at = g2d.getTransform();
            g2d.translate(g2d.getFont().getSize2D() + 2, height);
            g2d.rotate((-90 * Math.PI) / 180); // 90 degrees in radians
            g2d.drawString(units, 2 * g2d.getFont().getSize2D(), 0);
            g2d.setTransform(at);
            */
        	if (numLabels > 1) {
        		String us = "(" + units + ")";
	        	int unitLX = g2d.getFontMetrics(g2d.getFont()).stringWidth(us);
	            g2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);          
	            g2d.drawString(us, unitLabelRX - 2 - unitLX, height);
            }
			
        }
        
        if (trackPanel != null) {
        	if (trackPanel.getTracks().size() > 0) {
        		int ly = (int) (2 * g2d.getFont().getSize2D());
        		AbstractTSTrack track;
        		for (int i = 0; i < trackPanel.getTracks().size() && ly < height; i++) {
        			track = (AbstractTSTrack) trackPanel.getTracks().get(i);
        			g2d.setColor(track.getColor());
        			g2d.drawString(track.getName(), 2, ly);
        			ly += (g2d.getFont().getSize2D() + 2);
        		}
        	}       	
        }
    }

	public int[] getTickYPositions() {
		return tickYPos;
	}
	
    /**
     * Returns the vertical zoomlevel.
     *
     * @return the vertical zoomlevel
     */
    public float getVerticalZoom() {
        return zoom;
    }

    /**
     * Stes the vertical zoomlevel.
     *
     * @param vertZoom the vertical zoomlevel.
     */
    public void setVerticalZoom(float vertZoom) {
        zoom = vertZoom;
    }

    /**
     * Connects the track panel this ruler is part of.
     *
     * @param trackPanel the track panel
     */
    public void setTrackPanel(TSTrackPanelImpl trackPanel) {
        this.trackPanel = trackPanel;
    }

    /**
     * Returns the track panel this ruler is connected to.
     *
     * @return track panel this ruler is connected to
     */
    public TSTrackPanelImpl getTrackPanel() {
        return trackPanel;
    }

    /**
     * Removes the track panel this ruler is connected to.
     *
     * @param trackPanel
     */
    public void removeTrackPanel(TSTrackPanelImpl trackPanel) {
        if (this.trackPanel == trackPanel) {
            trackPanel = null;
        }
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSRuler#getHeight()
     */
    @Override
	public int getHeight() {
        return height;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSRuler#setHeight(int)
     */
    @Override
	public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the width of the ruler area.
     *
     * @return the width of the ruler
     */
    @Override
	public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the ruler area.
     *
     * @param width the new width
     */
    @Override
	public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSRuler#setForegroundColor(java.awt.Color)
     */
    @Override
	public void setForegroundColor(Color foreground) {
        this.foreground = foreground;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSRuler#getForegroundColor()
     */
    @Override
	public Color getForegroundColor() {
        return foreground;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSRuler#setFont(java.awt.Font)
     */
    @Override
	public void setFont(Font font) {
        this.font = font;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSRuler#getFont()
     */
    @Override
	public Font getFont() {
        return font;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSRuler#setFontSize(float)
     */
    @Override
	public void setFontSize(float size) {
        if (font != null) {
            font = font.deriveFont(size);
        }
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TSRuler#getFontSize()
     */
    @Override
	public float getFontSize() {
        if (font != null) {
            return font.getSize2D();
        }

        return 0f;
    }

    /**
     * Converts a float value to a String of certain a maximal length. 
     *
     * @param value the value to convert
     *
     * @return a String with a maximal length
     */
    private String getLabelString(float value, FontMetrics metrics, int maxWidth) {
        String lab = String.valueOf(value);
		if (metrics == null) {
			return lab;
		}
		int w = metrics.stringWidth(lab);
		if (w <= maxWidth) {
			return lab;
		}
		while (w > maxWidth && lab.length() > 0) {
			lab = lab.substring(0, lab.length() - 1);
			w = metrics.stringWidth(lab);
		}

        return lab;
    }

}
