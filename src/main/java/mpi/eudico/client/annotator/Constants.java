package mpi.eudico.client.annotator;

import java.awt.Color;
import java.awt.Font;

import javax.swing.plaf.FontUIResource;


/**
 * Some constants for use in ELAN
 */
public class Constants {
    /** The home directory of the user */
    public static String USERHOME = System.getProperty("user.home");

    /** The platform dependent file separator as a string */
    public static String FILESEPARATOR = System.getProperty("file.separator");

    /** An old configuration file, will be deleted by current versions of ELAN */
    public static String STRPROPERTIESFILE = USERHOME + FILESEPARATOR +
        ".elan.config";

    /** The (platform dependent) path of the folder where ELAN stores settings etc.*/
    public static String ELAN_DATA_DIR = USERHOME + FILESEPARATOR +
        ".elan_data";
	/**
	 * The directory (inside ELAN_DATA_DIR) where we search for
	 * lexicon-component (lexan) lexicons.
	 */
    public static final String LEXAN_LEXICON_DIR = "LexanLexicons";
	
    /** The name of a cache folder reserved for (text-based) analyzer extensions */
    public static final String ANALYZER_CACHE_FOLDER_NAME = "Analyzers";

    /** Holds value of property DOCUMENT ME! */
    public static Color DEFAULTBACKGROUNDCOLOR = new Color(230, 230, 230);

    /** Holds value of property DOCUMENT ME! */
    public static Color DEFAULTFOREGROUNDCOLOR = Color.black;

    /** Holds value of property DOCUMENT ME! */
    public static Color SELECTIONCOLOR = new Color(204, 204, 255);

    /** Holds value of property DOCUMENT ME! */
    public static Color CROSSHAIRCOLOR = Color.red;
    
    /** ALBERT */
    public static Color SEGMENTATIONCOLOR = Color.blue;

    /** Holds value of property DOCUMENT ME! */
    public static Color ACTIVEANNOTATIONCOLOR = Color.blue;

    /** Holds value of property DOCUMENT ME! */
    public static Color MEDIAPLAYERCONTROLSLIDERSELECTIONCOLOR = Color.gray;

    /** Holds value of property DOCUMENT ME! */
    public static Color MEDIAPLAYERCONTROLSLIDERCROSSHAIRCOLOR = Color.red.darker();

    /** Holds value of property DOCUMENT ME! */
    public static Color SIGNALSTEREOBLENDEDCOLOR1 = Color.green;

    /** Holds value of property DOCUMENT ME! */
    public static Color SIGNALSTEREOBLENDEDCOLOR2 = Color.blue;

    /** Holds value of property DOCUMENT ME! */
    public static Color SIGNALCHANNELCOLOR = new Color(224, 224, 224);

    /** Holds value of property DOCUMENT ME! */
    public static Color SHAREDCOLOR1 = Color.orange;

    /** Holds value of property DOCUMENT ME! */
    public static Color SHAREDCOLOR2 = Color.yellow;

    /** Holds value of property DOCUMENT ME! */
    public static Color SHAREDCOLOR3 = Color.gray;

    /** Holds value of property DOCUMENT ME! */
    public static Color SHAREDCOLOR4 = Color.white;
    
    public static Color SHAREDCOLOR5 = new Color(128, 0, 128);
    public static Color SHAREDCOLOR6 = Color.DARK_GRAY;

    /** Holds value of property DOCUMENT ME! */
    public static Color ACTIVETIERCOLOR = new Color(230, 210, 210);
    
    public static Color LIGHTBACKGROUNDCOLOR = new Color(240, 240, 240);
    
    /** the background color for the even rows in a table or list */
    public static final Color EVEN_ROW_BG = new Color(234, 245, 245);
    /** the background color for the selected row in a table or list */
    public static final Color SELECTED_ROW_BG = new Color(200,215,215);

    /** the Look & Feel default font for labels, not a constant, can be null */
    public static Font DEFAULT_LF_LABEL_FONT = null;
    
    /** the default font for tiers and annotations (if installed), no longer a constant */
    public static Font DEFAULTFONT = new Font("Arial Unicode MS", Font.PLAIN, 12);

    /** a slightly smaller version of the default font, no longer a constant */
    public static Font SMALLFONT = new Font("Arial Unicode MS", Font.PLAIN, 10);
    
    /** the size for a slightly smaller version of the default font, not a constant */
    public static float SMALLFONT_SIZE = 10f;
    
    /** the scale factor to use for a slightly smaller version of a font */
    public static final float SMALLFONT_SCALE_FACTOR = 0.84f;

    /** Holds value of property DOCUMENT ME! */
    public static final int SCROLLMIN = 0;

    /** Holds value of property DOCUMENT ME! */
    public static final int SCROLLINCREMENT = 1;
    
    public static final int COMBOBOX_VISIBLE_ROWS = 20;
    
    public static final int VISIBLE_MENUITEMS = 20;
    
    public static final int MAX_VISIBLE_PLAYERS = 4;

    // backup interval constants

    /** Holds value of property DOCUMENT ME! */
    public static final Integer BACKUP_NEVER = Integer.valueOf(0);
    
    public static final Integer BACKUP_1 = new Integer(60000);
    
	/** Holds value of property DOCUMENT ME! */
	public static final Integer BACKUP_5 = new Integer(300000);

    /** Holds value of property DOCUMENT ME! */
    public static final Integer BACKUP_10 = new Integer(600000);

    /** Holds value of property DOCUMENT ME! */
    public static final Integer BACKUP_20 = new Integer(1200000);

    /** Holds value of property DOCUMENT ME! */
    public static final Integer BACKUP_30 = new Integer(1800000);
    
    /** font sizes available in several viewers */
    public static final int[] FONT_SIZES = new int[]{8, 9, 10, 12, 14, 16, 18, 24, 36, 42, 48, 60, 72};
    
    /** decimal digits*/
    public static final int ONE_DIGIT = 1;
    
    public static final int TWO_DIGIT = 2;
    
    public static final int THREE_DIGIT = 3;
    
    // time format constants
	/** the hour/minutes/seconds/milliseconds format */
	public static final int HHMMSSMS = 100;

	/** the seconds/milliseconds format */
	public static final int SSMS = 101;

	/** the pure milliseconds format */
	public static final int MS = 102;
	
	/** the frame number format */
	public static final int HHMMSSFF = 103;

	public static final String HHMMSSMS_STRING = "hh:mm:ss.ms";
	public static final String SSMS_STRING = "ss.ms";
	public static final String MS_STRING = "ms";
	public static final String HHMMSSFF_STRING = "hh:mm:ss:ff";
	public static final String PAL_STRING = "PAL";
	public static final String PAL_50_STRING = "PAL-50fps";
	public static final String NTSC_STRING = "NTSC";
	
	public static final String ELAN_BEGIN_LABEL = "ELANBegin";
	public static final String ELAN_END_LABEL = "ELANEnd";
	public static final String ELAN_PARTICIPANT_LABEL = "ELANParticipant";
	
	/** HS May 2008: new location of ELAN data folder on the Mac. */
	static {
		if (System.getProperty("os.name").indexOf("Mac OS") > -1) {
			ELAN_DATA_DIR = USERHOME + FILESEPARATOR + "Library" + FILESEPARATOR + "Preferences" + FILESEPARATOR + "ELAN";
		}
	}
	/** constants for copying annotations in different formats */
	public static final String TEXTANDTIME_STRING = "annotation + begintime + endtime";
	public static final String TEXT_STRING = "annotation only";
	public static final String URL_STRING = "filepath + tier name + begintime + endtime";
	public static final String CITE_STRING = "filename + tiername + begintime + endtime";
	
	/**
	 * Sets a scale factor for the default font for tiers and annotations. 
	 * Best be called after setting a preferred default font and/or size.
	 * 
	 * @param scaleFactor the scale factor
	 */
	public static void setFontScaling(float scaleFactor) {
		Constants.DEFAULTFONT = new Font(Constants.DEFAULTFONT.getFontName(), Constants.DEFAULTFONT.getStyle(), 
				(int) Math.ceil((Constants.DEFAULTFONT.getSize() * scaleFactor)));
		Constants.SMALLFONT_SIZE = scaleFactor * Constants.SMALLFONT_SIZE;
		Constants.SMALLFONT = new Font(Constants.SMALLFONT.getFontName(), Constants.SMALLFONT.getStyle(),
				(int) Math.ceil(Constants.SMALLFONT_SIZE));
	}
	
	/**
	 * Sets the default font to use for tiers and annotations. The application's default is "Arial Unicode MS",
	 * if that font is not installed it is up to the JRE which font is used.
	 * 
	 * If this method is called before setFontScaling the actual size will be different depending on the scale factor.
	 * 
	 * @param fontName the name of the font
	 * @param fontSize the size of the font
	 */
	public static void setDefaultFont(String fontName, int fontSize) {
		Constants.DEFAULTFONT = new Font(fontName, Font.PLAIN, fontSize);
		Constants.SMALLFONT_SIZE = fontSize * Constants.SMALLFONT_SCALE_FACTOR;
		Constants.SMALLFONT = DEFAULTFONT.deriveFont(Font.PLAIN, Constants.SMALLFONT_SIZE);
	}
	
	/**
	 * Sets the default Look&Feel font used for JLabels.
	 * 
	 * @param labelFont the font used for labels by the current L&F 
	 */
	public static void setLookAndFeelLabelFont(Font labelFont) {
		if (labelFont instanceof FontUIResource) {
			// could maybe use the FontUIResource, but create a Font now
			DEFAULT_LF_LABEL_FONT = new Font(labelFont.getFontName(), labelFont.getStyle(), labelFont.getSize());
		} else {
			DEFAULT_LF_LABEL_FONT = labelFont;
		}		
	}
	
	/**
	 * Returns a slightly smaller version of the font based on the 
	 * scale factor constant. 
	 * 
	 * @param sourceFont the Font to create a smaller version of
	 * @return a scaled version of the font
	 */
	public static Font deriveSmallFont(Font sourceFont) {
		if (sourceFont == null) {
			return null;
		}
		return sourceFont.deriveFont(sourceFont.getSize2D() * Constants.SMALLFONT_SCALE_FACTOR);
	}
}
