package mpi.eudico.client.annotator.viewer;

import mpi.eudico.util.TimeFormatter;

import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.SwingConstants;


/**
 * Paints a ruler denoting time units.
 * In verion 0.2 the paint method has been changed such that rounded values 
 * are used for the labels as much as possible. The old method of labels at 
 * a fixed interval has been moved to a new method paintAtFixedInterval.
 * In version 0.3 float precision for the msPerPixel has been introduced.
 * 
 * @author Han Sloetjes
 * @version 0.1 9/7/2003
 * @version 0.2 & 0.3 oct-nov 2005 
 */
public class TimeRuler {
    private Font font;

    /** Default distance between two small ticks */
    private int minorTickSpacing;

    /** Default distance between two large ticks */
    private int majorTickSpacing;

    /** Average width of a time label */
    private int avgLabelWidth;
    private String defLabel;
    private int height;
    private int extraHeight;

    /** floats for more accurate calculations */
    private float majorTick;
    private float minorTick;

    // store the last used label step distance
    private int step;

    // store the last used msPerPixels to avoid unnecessary calculations
    // in the paint method
    private float lastUsedMspp = -1;

    /** base values for round label vakues */
    private final int[] stepbase = new int[] { 10, 20, 50 };

    /** counter for the Math.pow loop over the stepbase values */
    private final int power = 4;
    
    private static final int minorTickHeight = 2;
    private static final int majorTickHeight = 5;

    /**
     * Constructs a TimeRuler using the specified Font and a default label.<br>
     * This label should reflect the format that will be used for the time
     * labels.
     *
     * @param font the font to use for the labels
     * @param defLabel the default time label, e.g. 00:00:000
     */
    public TimeRuler(Font font, String defLabel) {
        this.font = font;
        this.defLabel = defLabel;
        minorTickSpacing = 10; //every 10 pixels
        majorTickSpacing = 100; //every 100 pixels
        height = font.getSize() + 4 + 4;
        majorTick = majorTickSpacing;
        minorTick = minorTickSpacing;
    }
    
    public TimeRuler(Font font, String defLabel, int extraHeight) {
    	this(font, defLabel);
    	this.extraHeight = extraHeight;
    	height += extraHeight;
    }

    /**
     * Paints a time ruler to the specified Graphics2D object.<br>
     * A begintime (for an interval) and a width in pixels should be provided,
     * as well as the number of milliseconds per pixel (resolution). The
     * position can be top or bottom. Labels and ticks are always painted with
     * a fixed interval, defined by  <code>majorTickSpacing</code> and
     * <code>minorTickSpacing</code>, by  default 100 and 10 pixels.
     *
     * @param g2d the Graphics2D object
     * @param beginTime the (interval) begin time
     * @param width the width in pixels to use for painting
     * @param msPerPixel the resolution in milliseconds per pixel
     * @param position either SwingConstants.TOP or SwingConstants.BOTTOM
     */
    public void paintAtFixedInterval(Graphics2D g2d, long beginTime, int width,
        float msPerPixel, int position) {
        g2d.setFont(font);
        avgLabelWidth = g2d.getFontMetrics(font).stringWidth(defLabel);
        boolean alternateLabels = avgLabelWidth + 4 >= majorTickSpacing;

        // System.out.println("Ms pp: " + msPerPixel);
        int step = (int)(majorTickSpacing * msPerPixel);
        long firstMarker = step * (beginTime / step);
        int firstLine = (int) (firstMarker / msPerPixel);
        int extent = firstLine + width + majorTickSpacing;

        for (int i = firstLine; i <= extent; i += minorTickSpacing) {
            switch (position) {
            case SwingConstants.TOP:
                g2d.drawLine(i, 0, i, minorTickHeight);

                break;

            case SwingConstants.BOTTOM:
                g2d.drawLine(i, height - minorTickHeight, i, height);

                break;

            default:}
        }
        // to prevent time labels from being painted "on top of each other" if their
        // width is too large, skip the painting of every other label
        boolean paintThisLabel = alternateLabels ? (firstMarker % (2 * step) == 0) : true;
        // major ticks and labels
        for (int j = firstLine; j <= extent; j += majorTickSpacing) {
            switch (position) {
            case SwingConstants.TOP:
                g2d.drawLine(j, 0, j, majorTickHeight);
                if (paintThisLabel) {
	                g2d.drawString(TimeFormatter.toString(firstMarker),
	                    j - (avgLabelWidth / 2), height - 2);
                }

                break;

            case SwingConstants.BOTTOM:
            	if (paintThisLabel) {
	                g2d.drawString(TimeFormatter.toString(firstMarker),
	                    j - (avgLabelWidth / 2), font.getSize() + 2);
            	}
                g2d.drawLine(j, height - majorTickHeight, j, height);

                break;

            default:}

            //firstMarker += (majorTickSpacing * msPerPixel);
			firstMarker += step;
            //System.out.println("Next label: " + firstMarker);
        }
        if (alternateLabels) {
        	paintThisLabel = firstMarker % (2 * step) == 0;
        }
    }

    /**
     * Paints a time ruler to the specified Graphics2D object.<br>
     * A begintime (for an interval) and a width in pixels should be provided,
     * as well as the number of milliseconds per pixel (resolution). The
     * position can be top or bottom. Labels and ticks are <b>not</b> always
     * painted with a fixed interval.  This method tries to paint round time
     * values (whole seconds, hundreds/thousands of  milliseconds etc.
     * depending on the resolution) where the <code>majorTickSpacing</code>
     * value serves as the minimum label distance.
     *
     * @param g2d the Graphics2D object
     * @param beginTime the (interval) begin time
     * @param width the width in pixels to use for painting
     * @param msPerPixel the resolution in milliseconds per pixel
     * @param position either SwingConstants.TOP or SwingConstants.BOTTOM
     */
    public void paint(Graphics2D g2d, long beginTime, int width,
        float msPerPixel, int position) {
        g2d.setFont(font);
        avgLabelWidth = g2d.getFontMetrics(font).stringWidth(defLabel);

        // try to paint labels for some rounded values, depending on 
        // the amount of msPerPixel, ignoring the defaults for minor- and
        // majortickspacing
        // recalculate majorTick, minorTick and label step
        if (msPerPixel != lastUsedMspp) {
            int oldStep = (int)(majorTickSpacing * msPerPixel);
            step = getRoundedStep(oldStep);
            if (step == 0) {
            	step = 1;
            }
            majorTick = majorTickSpacing * ((float) step / oldStep);
            minorTick = majorTick / ((float) majorTickSpacing / minorTickSpacing);
            lastUsedMspp = msPerPixel;
        }
        // if the necessary width for the label is almost as large or larger as the available 
        // width, skip the painting of every other label
        boolean alternateLabels = avgLabelWidth + 4 >= majorTick;
        // begin with a marker that is smaller than the begintime
        long firstMarker = step * (beginTime / step);
        int firstLine = (int) (firstMarker / msPerPixel);
        int extent = firstLine + width + (int) majorTick;
        int x;

        for (int i = firstLine, j = 0; i <= extent; j++) {
            //x = firstLine + (int) Math.round(j * minorTick);
        	x = firstLine + (int) (j * minorTick);

            switch (position) {
            case SwingConstants.TOP:
                g2d.drawLine(x, 0, x, minorTickHeight);

                break;

            case SwingConstants.BOTTOM:
                g2d.drawLine(x, height - minorTickHeight, x, height);

                break;

            default:}

            i = x;
        }
        // to prevent time labels from being painted "on top of each other" if their
        // width is too large, skip the painting of every other label
        boolean paintThisLabel = alternateLabels ? (firstMarker % (2 * step) == 0) : true;
        for (int j = firstLine, i = 0; j <= extent; i++) {
            //x = firstLine + (int) Math.round(i * majorTick);
        	x = firstLine + (int) (i * majorTick);

            switch (position) {
            case SwingConstants.TOP:
                g2d.drawLine(x, 0, x, majorTickHeight);
                if (paintThisLabel) {
	                g2d.drawString(TimeFormatter.toString(firstMarker),
	                    x - (avgLabelWidth / 2), height - 2);
                }

                break;

            case SwingConstants.BOTTOM:
            	if (paintThisLabel) {
	                g2d.drawString(TimeFormatter.toString(firstMarker),
	                    x - (avgLabelWidth / 2), font.getSize() + 2);
            	}
                g2d.drawLine(x, height - majorTickHeight, x, height);

                break;

            default:}

            //firstMarker += Math.round(majorTick * msPerPixel);
			firstMarker += step;
            j = x;

            //System.out.println("Next label: " + firstMarker);
            if (alternateLabels) {
            	paintThisLabel = firstMarker % (2 * step) == 0;
            }
        }
    }

    /**
     * Sets the font for the time label values.
     *
     * @param font the font for the time label values
     */
    public void setFont(Font font) {
        this.font = font;
        height = font.getSize() + 4 + 4 + extraHeight;
    }

    /**
     * Returns the font in use for the time labels.
     *
     * @return the font in use for the time labels
     */
    public Font getFont() {
        return font;
    }

    /**
     * Returns the next, rounded up step value that will assure rounded time
     * values and non overlapping time labels.
     *
     * @param inStep the calculated step size, the basis for the new step
     *
     * @return the new step value
     */
    private int getRoundedStep(int inStep) {
        int step = inStep;
        int one;
        int two;
        int five;

        for (int i = 1; i <= power; i++) {
            one = (int) (stepbase[0] * Math.pow(10, i));
            two = (int) (stepbase[1] * Math.pow(10, i));
            five = (int) (stepbase[2] * Math.pow(10, i));

            if ((step == one) || (step == two) || (step == five)) {
                break;
            }

            if (step < one) {
                if (i == 0) {
                    step = one;

                    break;
                } else {
                    continue;
                }
            } else if (step > five) {
                if (i == power) {
                    step = five;

                    break;
                } else {
                    if (step < (int) (stepbase[0] * Math.pow(10, i + 1))) {
                        step = (int) (stepbase[0] * Math.pow(10, i + 1));

                        break;
                    }
                }
            } else if ((step > one) && (step < two)) {
                step = two;

                break;
            } else if ((step > two) && (step < five)) {
                step = five;

                break;
            }
        }

        return step;
    }

    /**
     * Returns the distance in pixels between consecutive time labels or major
     * tick marks.  100 pixels by default. It depends on the value of the
     * 'milliseconds per pixel'  parameter passed to the paint method how many
     * milliseconds a 'major tick'  represents.
     *
     * @return distance in pixels between major ticks
     */
    public int getMajorTickSpacing() {
        return majorTickSpacing;
    }

    /**
     * Returns the distance in pixels between minor tick marks, a subdivision
     * of the  major ticks. 10 pixels by default.
     *
     * @return distance in pixels between minor ticks
     */
    public int getMinorTickSpacing() {
        return minorTickSpacing;
    }

    /**
     * Sets the distance in pixels between consecutive time labels or major
     * tick  marks.
     *
     * @param spacing the new distance in pixels between major ticks
     */
    public void setMajorTickSpacing(int spacing) {
        majorTickSpacing = spacing;

        // force recalculations
        lastUsedMspp = -1;
    }

    /**
     * Sets the distance in pixels between minor tick marks.
     *
     * @param spacing the new space in pixels between minor ticks
     */
    public void setMinorTickSpacing(int spacing) {
        minorTickSpacing = spacing;

        // force recalculations
        lastUsedMspp = -1;
    }

    /**
     * Returns the height of the ruler, which is based on the size of the font.
     *
     * @return the height of the ruler
     */
    public int getHeight() {
        return height;
    }
}
