package mpi.eudico.client.annotator.util;

/**
 * A class holding constants for file extension relevant to Elan. 
 * Could be in the Constants class?
 *
 * @author Han Sloetjes
 */
public class FileExtension {
    /** Known extensions for MPEG-1/MPEG-2 files */
    public static final String[] MPEG_EXT = new String[] { "mpg", "mpeg" };
    // mpeg, mpg, m1s, m1v, m1a, m75, m15, mp2, mpm, mpv, mpa
    
	/** Known extensions for MPEG-4 files */
	public static final String[] MPEG4_EXT = new String[] { "mp4", "mpg4" };
    
	/** Known extensions for QuickTime movie files */
    public static final String[] QT_EXT = new String[] {"mov", "qt"};
    
    /** Known extensions for several audio file formats */
    public static final String[] MISC_AUDIO_EXT = new String[]{"au", "snd", "aiff", "aif",
    	"cdda", "cda", "mid", "midi", "mp3", "wma" };

	/** Known extensions for several video file formats */
	public static final String[] MISC_VIDEO_EXT = new String[] { "avi", "wmv", "wm", 
		"wmp", "asf", "asx", "mov", "qt", "mp4", "mpg4" };
		
    /** Known extensions for WAVE files */
    public static final String[] WAV_EXT = new String[] { "wav" };

    /** Known extensions for EAF files */
    public static final String[] EAF_EXT = new String[] { "eaf" };

    /** Known extensions for Elan Template files */
    public static final String[] TEMPLATE_EXT = new String[] { "etf" };

    /** Known extensions for Chat files */
    public static final String[] CHAT_EXT = new String[] { "cha" };
    
	/** Known extensions for wac files */
	public static final String[] WAC_EXT = new String[] { "wac" };
	
	/** Known extensions for (Shoebox) text files */
	public static final String[] TEXT_EXT = new String[] { "txt" };
	
	/** Knows extensions for Tiger-Syntax-Corpus files */
	public static final String[] TIGER_EXT = new String[]{ "tig" };

	/** Known extensions for Shoebox/Toolbox text files */
	public static final String[] SHOEBOX_TEXT_EXT = new String[] { "sht", "tbt", "txt" };
	
	/** Known extensions for Toolbox text files (emphasis on tbt) */
	public static final String[] TOOLBOX_TEXT_EXT = new String[] { "tbt", "sht", "txt" };
	
	/** Known extensions for Shoebox typ files */
	public static final String[] SHOEBOX_TYP_EXT = new String[] { "typ" };
	
	/** Known extensions for Shoebox marker files */
	public static final String[] SHOEBOX_MKR_EXT = new String[] { "mkr" };

	/** Known extensions for Transcriber files */
	public static final String[] TRANSCRIBER_EXT = new String[] { "trs" };
    
	/** Known extensions for image files */
	public static final String[] IMAGE_EXT = new String[] { "jpg", "jpeg", "png", "bmp"};

	/** Known extensions for image files as input media (media to be annotated) */
	public static final String[] IMAGE_MEDIA_EXT = new String[] { "jpg", "jpeg", "png", "bmp", "gif", "tiff"};
	
	/** Known extensions for smil files */
	public static final String[] SMIL_EXT = new String[] { "smil", "sml" };

	/** Known extensions for svg files */
	public static final String[] SVG_EXT = new String[] { "svg" };

	/** Known extensions for xml files */
	public static final String[] XML_EXT = new String[] { "xml" };
		
	/** Known extensions for (cyberglove) log files */
	public static final String[] LOG_EXT = new String[] { "log" };
	
	/** Known extensions for html files */
	public static final String[] HTML_EXT = new String[] { "html", "htm" };
	
	/** Known extensions for flex files */
	public static final String[] FLEX_EXT = new String[] { "flextext", "xml" };

	/** Known extensions for Praat TextGrid files */
	public static final String[] PRAAT_TEXTGRID_EXT = new String[] { "TextGrid" };
	
	/** The extension for an xml based ELAN preferences file */
	public static final String[] ELAN_XML_PREFS_EXT = new String[] { "pfsx" };
	
	/** Known extensions for csv and/or tab delimited files */
	public static final String[] CSV_EXT = new String[] { "csv", "txt" };
	
/** Known extension for subtitle files */
    public static final String[] SUBTITLE_EXT = new String[] { "srt", "stl", "lrc", "xml" };

	/** Known extension for IMDI metadata files */
	public static final String[] IMDI_EXT = new String[] { "imdi", "cmdi"};
	
	/** Known extension for External Controlled Vocabulary files */
	public static final String[] ECV_EXT = new String[] { "ecv"};
	
	/** Known extension for ELAN Annotation Query files */
	public static final String[] EAQ_EXT = new String[] {"eaq"};
	
	/** Known extension for CMDI metadata  files */
	public static final String[] CMDI_EXT = new String[] {"cmdi"};
	
    /** supported media files */
    public static final String[] MEDIA_EXT;
	
    static {
		MEDIA_EXT = new String[MPEG_EXT.length + WAV_EXT.length + MPEG4_EXT.length + QT_EXT.length];
		System.arraycopy(MPEG_EXT, 0, MEDIA_EXT, 0, MPEG_EXT.length);
		System.arraycopy(WAV_EXT, 0, MEDIA_EXT, MPEG_EXT.length, WAV_EXT.length);
		System.arraycopy(MPEG4_EXT, 0, MEDIA_EXT, (MPEG_EXT.length + WAV_EXT.length), MPEG4_EXT.length);
		System.arraycopy(QT_EXT, 0, MEDIA_EXT, (MPEG_EXT.length + WAV_EXT.length +MPEG4_EXT.length ), QT_EXT.length);
	}
}
