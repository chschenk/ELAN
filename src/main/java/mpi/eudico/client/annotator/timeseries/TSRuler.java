package mpi.eudico.client.annotator.timeseries;

import java.awt.Color;
import java.awt.Font;


/**
 * Interface describing a vertical ruler for time series data viewers.
 */
public interface TSRuler {
    /**
     * Returns the current range, min - max.
     *
     * @return the current min and max value of the ruler
     */
    public float[] getRange();

    /**
     * Sets the current range, min - max.
     *
     * @param range the new range
     */
    public void setRange(float[] range);

    /**
     * Returns a String representation of the ruler's units, like m/s.
     *
     * @return a String representation of the ruler's units
     */
    public String getUnitString();

    /**
     * Sets the String representation of the ruler's units.
     *
     * @param unitString the String representation of the ruler's units
     */
    public void setUnitString(String unitString);

    /**
     * Returns the height in pixels of the ruler.
     *
     * @return the height
     */
    public int getHeight();

    /**
     * Sets the height in pixels of the ruler.
     *
     * @param height the height in pixels of the ruler
     */
    public void setHeight(int height);

    /**
     * Returns the width of the ruler area.
     *
     * @return the width of the ruler
     */
    public int getWidth();

    /**
     * Sets the width of the ruler area.
     *
     * @param width the new width
     */
    public void setWidth(int width);

    /**
     * Sets the Color for tick markers and labels. Default is black.
     *
     * @param color the Color for tick markers and labels
     */
    public void setForegroundColor(Color color);

    /**
     * Returns the Color for tick markers and labels.
     *
     * @return the Color for tick markers and labels
     */
    public Color getForegroundColor();

    /**
     * Sets the font for labels.
     *
     * @param font the font for the labels
     */
    public void setFont(Font font);

    /**
     * Returns the font used for the lables.
     *
     * @return the font for the labels
     */
    public Font getFont();

    /**
     * Sets the size for the label font.
     *
     * @param size the size for the font
     */
    public void setFontSize(float size);

    /**
     * Returns the size of the font.
     *
     * @return the size of the font
     */
    public float getFontSize();
}
