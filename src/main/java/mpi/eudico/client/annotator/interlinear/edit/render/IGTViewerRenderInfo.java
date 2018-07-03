package mpi.eudico.client.annotator.interlinear.edit.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.FontSizer;
import mpi.eudico.client.annotator.interlinear.edit.IGTConstants;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTTier;

public class IGTViewerRenderInfo extends IGTRenderInfo implements FontSizer {

	public int vertRowMargin = IGTConstants.VERTICAL_ROW_MARGIN; // the empty space between two rows
							  //  (== root annotation + depending annotations)
	public int vertLineMargin = IGTConstants.VERTICAL_LINE_MARGIN; // the empty space between two lines of text
							   // (between two tiers)
	public int headerWidth; // width of tier label area
	public Insets annBBoxInsets;// margins for the bounding boxes around IGT annotations
	public boolean showAnnoBorders = true;
	public boolean showAnnoBackground = false;

	public int whitespaceWidth = IGTConstants.WHITESPACE_PIXEL_WIDTH;
	public Font defaultFont = Constants.DEFAULTFONT;

	public Color foregroundColor = Constants.DEFAULTFOREGROUNDCOLOR;
	// the following colors might become user definable, but initially use default colors
	public Color backgroundColor = IGTConstants.TABLE_BACKGROUND_COLOR1;
	public Color backgroundColor2 = IGTConstants.TABLE_BACKGROUND_COLOR2;
	public Color annoBorderColor = IGTConstants.ANNO_BORDER_COLOR;
	public Color annoBackgroundColor = IGTConstants.ANNO_BACKGROUND_COLOR;

	// font information
	private Map<String, Font> tierFontMap;
	private Map<String, Color> tierColorMap;
	private Map<String, Color> prefTierColorMap;

	/**
	 * No-arg constructor.
	 */
	public IGTViewerRenderInfo() {
		super();
		tierFontMap = new HashMap<String, Font>();
		tierColorMap = new HashMap<String, Color>();
		prefTierColorMap = new HashMap<String, Color>();
		annBBoxInsets = new Insets(
				IGTConstants.TEXT_MARGIN_TOP, 
				IGTConstants.TEXT_MARGIN_LEFT, 
				IGTConstants.TEXT_MARGIN_BOTTOM, 
				IGTConstants.TEXT_MARGIN_RIGHT);
	}

	/**
	 * Set the per tier color map
	 * 
	 * @param colorMap
	 */
	public void setTierColorMap(Map<String, Color> colorMap) {
		if (colorMap != null) {
			prefTierColorMap.clear();
			prefTierColorMap.putAll(colorMap);
			tierColorMap.clear();
			tierColorMap.putAll(prefTierColorMap);
		}
	}

	/**
	 * Set the per tier font map
	 * 
	 * @param fontMap
	 */
	public void setTierFontMap(Map<String, Font> fontMap) {
		if (fontMap != null) {
			tierFontMap.clear();
			tierFontMap.putAll(fontMap);

			for (Map.Entry<String, Font> e : fontMap.entrySet()) {
				String key = e.getKey();
				Font ft = e.getValue();

				if (key != null && ft != null) {
					// use the size of the default font
					tierFontMap.put(key, new Font(ft.getName(), ft.getStyle(),
							defaultFont.getSize()));
				}
			}
		}
	}

	/**
	 * Returns the Color for the particular tier.
	 * 
	 * @param tierName
	 *            the name of the tier
	 * @return the Color
	 */
	public Color getColorForTier(IGTTier tier) {
		Color c = null;
		if (tier.isSpecial()) {
			c = tierColorMap.get(tier.getRootTier().getTierName());
		} else {
			c = tierColorMap.get(tier.getTierName());
			if (c == null) {
				c = prefTierColorMap.get(tier.getTierName());
				if (c == null) {
					if (tier.getParentTier() != null) {
						c = tierColorMap.get(tier.getRootTier().getTierName());
						if (c != null) {
							tierColorMap.put(tier.getTierName(), c);
						}
					}
				} else {
					tierColorMap.put(tier.getTierName(), c);
				}
			}
		}

		if (c != null) {
			return c;
		} else {
			return this.foregroundColor;
		}

	}

	/**
	 * Returns the font for the particular tier.
	 * 
	 * @param tierName
	 *            the name of the tier
	 * @return the font or null
	 */
	public Font getFontForTier(String tierName) {
		Font f = tierFontMap.get(tierName);
		if (f != null) {
			return f;
		}
		return defaultFont;
	}

	/**
	 * Sets the font for the specified tier.
	 * 
	 * @param tierName
	 *            the name of the tier
	 * @param f
	 *            the font
	 */
	public void setFontForTier(String tierName, Font f) {
		tierFontMap.put(tierName, new Font(f.getName(), f.getStyle(),
				defaultFont.getSize()));
	}

	/**
	 * Set the size of the default, base font
	 * 
	 * @param int, the size of font
	 */
	@Override
	public void setFontSize(int size) {
		defaultFont = new Font(defaultFont.getName(), defaultFont.getStyle(),
				size);

		// update font size on all the font prefs
		Iterator<String> keyIt = tierFontMap.keySet().iterator();
		String key = null;
		Font ft = null;

		while (keyIt.hasNext()) {
			key = keyIt.next();
			ft = tierFontMap.get(key);

			if (key != null && ft != null) {
				// use the size of the default font
				tierFontMap.put(key, new Font(ft.getName(), ft.getStyle(),
						defaultFont.getSize()));
			}
		}
	}

	/**
	 * @return the current size of the base font
	 */
	@Override
	public int getFontSize() {
		return defaultFont.getSize();
	}

	/**
	 * Returns the size of the font for this tier as an int.
	 * 
	 * @param tierName
	 *            the tier name
	 * @return the size of font as an int, defaults to 12 if there is no mapping
	 */
	public int getFontSizeForTier(String tierName) {
		Font f = tierFontMap.get(tierName);

		if (f != null) {
			return f.getSize();
		}

		// return 12;
		return defaultFont.getSize();
	}

	/**
	 * Returns the height for a tier "line". This height is based on the size of
	 * the font for the tier. If a bounding box is visible around annotations,
	 * insets for that bounding box are taken into account 
	 * 
	 * @param tierName
	 *            the name of the tier
	 * @return the line height for a tier, the font size plus some extra space,
	 *         defaults to 14
	 */
	public int getHeightForTier(Graphics g2d, String tierName) {
		Font f = tierFontMap.get(tierName);
		if (f == null) {
			f = defaultFont; // TODO implement something more sensible
		}

		return g2d.getFontMetrics(f).getHeight() + getVerticalBBoxInsets();
	}

	public int getBaselineForTier(Graphics g2d, String tierName) {
		Font f = tierFontMap.get(tierName);
		if (f == null) {
			f = defaultFont; // TODO implement something more sensible
		}

		return g2d.getFontMetrics(f).getDescent();
	}
	
	/**
	 * @return the sum of the left and the right inset values for annotation bounding boxes
	 */
	public int getHorizontalBBoxInsets() {
		return annBBoxInsets.left + annBBoxInsets.right;
	}

	/**
	 * @return the sum of the top and bottom inset values for annotation bounding boxes
	 */
	public int getVerticalBBoxInsets() {
		return annBBoxInsets.top + annBBoxInsets.bottom;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		// buf.append("IGTViewerRenderInfo:[");
		buf.append(super.toString());
		buf.append(" vertRowMargin=");
		buf.append(String.valueOf(vertRowMargin));
		buf.append(" vertLineMargin=");
		buf.append(String.valueOf(vertLineMargin));
		buf.append(" headerWidth=");
		buf.append(String.valueOf(headerWidth));
		buf.append(" whitespaceWidth=");
		buf.append(String.valueOf(whitespaceWidth));
		if (annBBoxInsets != null) {
			buf.append(" boundingBoxMargins=");
			buf.append(annBBoxInsets.toString());
		}
		// buf.append("]");

		return buf.toString();
	}
}
