package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.Color;

/**
 * Some constants for the IGTViewer and related classes.
 * The pixel values for sizes etc. are default values. When applying these might need to
 * scale along with other components.
 * 
 * @author Han Sloetjes
 */
public interface IGTConstants {
	/**
	 * General text-to-boundary margins. The boundary can be a side of a 
	 * bounding box or the boundary of (a part of) the rendering area.
	 */
	public static final int TEXT_MARGIN_LEFT = 4;
	public static final int TEXT_MARGIN_RIGHT = 4;
	public static final int TEXT_MARGIN_TOP = 2;
	public static final int TEXT_MARGIN_BOTTOM = 2;
	/**
	 * Width of an indentation of a level in a tree like representation.  
	 */
	public static final int INDENTATION_SIZE = 10;
	
	/** Margin spaces between tier lines and rows within one block and between 
	 * annotations on the same row. */
	public static final int VERTICAL_ROW_MARGIN = 4;
	public static final int VERTICAL_LINE_MARGIN = 2;
	public static final int WHITESPACE_PIXEL_WIDTH = 8;
	/** space between suggestions, horizontal and vertical */
	public static final int SUGGESTION_MARGIN = 12;
	
	/** Some default colors for the IGT viewer */
	public static final Color ANNO_BORDER_COLOR = Color.LIGHT_GRAY;
	public static final Color ANNO_BACKGROUND_COLOR = new Color(255, 255, 245);
	public static final Color TABLE_BACKGROUND_COLOR1 = Color.WHITE;
	public static final Color TABLE_BACKGROUND_COLOR2 = new Color(230, 230, 230);
	
	public static final boolean SHOW_ANNOTATION_BORDER = true;
	public static final boolean SHOW_ANNOTATION_BACKGROUND = false;
	
	/* a number of keys for identifying properties and storing Preferences */
	// corresponds to TABLE_BACKGROUND_COLOR1
	public static final String KEY_BACKGROUND_COLOR_EVEN   = "InterlinearEditor.BackgroundColor.Even";
	// corresponds to TABLE_BACKGROUND_COLOR2
	public static final String KEY_BACKGROUND_COLOR_ODD    = "InterlinearEditor.BackgroundColor.Odd";
	public static final String KEY_ANN_BORDER_COLOR        = "InterlinearEditor.Annotation.BorderColor";
	public static final String KEY_ANN_BACKGROUND_COLOR    = "InterlinearEditor.Annotation.BackgroundColor";
	public static final String KEY_ANN_BORDER_VIS_FLAG     = "InterlinearEditor.PaintAnnotationBorders";
	public static final String KEY_ANN_BACKGROUND_VIS_FLAG = "InterlinearEditor.PaintAnnotationBackground";
	public static final String KEY_BBOX_LEFT_MARGIN        = "InterlinearEditor.Annotation.BB.LeftMargin";
	public static final String KEY_BBOX_TOP_MARGIN         = "InterlinearEditor.Annotation.BB.TopMargin";
	public static final String KEY_WHITESPACE_WIDTH        = "InterlinearEditor.WhitespaceWidth";
	
}
