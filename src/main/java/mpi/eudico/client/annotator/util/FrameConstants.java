package mpi.eudico.client.annotator.util;

/**
 * Specifies a number of constants for ELAN frames.
 *
 * @author Han Sloetjes, MPI
 */
public interface FrameConstants {
    // menu constants
    /** the file menu */
    public static final int FILE = 0;

    /** the edit menu */
    public static final int EDIT = 100;

    /** the annotation menu */
    public static final int ANNOTATION = 200;

    /** the tier menu */
    public static final int TIER = 300;

    /** the type menu */
    public static final int TYPE = 400;

    /** the search menu */
    public static final int SEARCH = 500;

    /** the view menu */
    public static final int VIEW = 600;

    /** the options menu */
    public static final int OPTION = 700;

    /** the window menu */
    public static final int WINDOW = 800;

    /** the help menu */
    public static final int HELP = 900;

    /** the recent files menu */
    public static final int RECENT = 11;

    /** the export menu */
    public static final int EXPORT = 12;

    /** the import menu */
    public static final int IMPORT = 13;
    
    /** the media player menu */
    public static final int MEDIA_PLAYER = 610;

    /** the viewers menu */
    public static final int VIEWER = 620;
    
    /** the wave form viewer menu */
    public static final int WAVE_FORM_VIEWER = 630;
    
    /** the propagation menu */
    public static final int PROPAGATION = 701;

    /** the language menu */
    public static final int LANG = 760;
    
    /** the video standard menu, or frame length */
    public static final int FRAME_LENGTH = 740;

    /** the annotation mode menu */
    public static final int ANNOTATION_MODE = 710;
    
    /** the media synchronisation mode menu */
    public static final int SYNC_MODE = 711;
    
    /** the kiosk mode menu */
    public static final int KIOSK_MODE = 712;
    
    /** the play around selection dialog menu */
    public static final int PLAY_AROUND_SEL = 713;
    
    /** the rate volume toggle value menu */
    public static final int RATE_VOL_TOGGLE = 714;
    
    /** the maximum size of the recent files list */
    public final int MAX_NUM_RECENTS = 5;
    
    /** the maximum size of the recent files list */
    public final int MAX_NUM_STORED_RECENTS = 30;
}
