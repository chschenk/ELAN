package mpi.eudico.client.annotator.interlinear;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Main object for options for interlinearisation.  Creates a BlockMetrics
 * object that performs calculations etc. for interlinear layout.
 *
 * @author MPI
 */
public class Interlinear {
    // constants
    // units for page width and height

    /** constant for centimeter layout unit */
    public static final int CM = 0;

    /** constant for inch layout unit */
    public static final int INCH = 1;

    /** constant for pixel layout unit */
    public static final int PIXEL = 2;

    /** constant for character layout unit */
    public static final int NUM_CHARACTERS = 3;

    // wrap styles for blocks and lines

    /** block wrap style where each block starts at a new 'block line',
     * but where within a block wrapping is applied dependent on the width */
    public static final int EACH_BLOCK = 0;

    /**
     * block wrap style where wrapping to a new line occurs when the next
     * block does not fit completely on the current 'block line'
     */
    public static final int BLOCK_BOUNDARY = 1;

    /**
     * block wrap style where block wrapping only occurs when the next
     * annotations do not fit on the current 'block line'!
     */
    public static final int WITHIN_BLOCKS = 2;

    /**
     * block and line wrap style where no wrapping is applied; all annotations
     * are positions on a single 'block line'
     */
    public static final int NO_WRAP = 3;
    
    // wrap styles for lines

    /**
     * line wrap style where a line that does not fit on the current line is
     * wrapped to the next
     */
    public static final int NEXT_LINE = 4;
    
    /**
     * wrap lines to new lines at the end of the block
     */
    public static final int END_OF_BLOCK = 5;

    // time code types

    /** the hour/minutes/seconds/milliseconds format */
    public static final int HHMMSSMS = 0;

    /** the seconds/milliseconds format */
    public static final int SSMS = 1;

    /** the pure milliseconds format */
    public static final int MS = 2;
    
//    //number of digits after decimal for min silence duration
//    public static final int ONE_DIGITS = 1;
//
//    public static final int TWO_DIGITS = 2; 
//    
//    public static final int THREE_DIGITS = 3; 
    
    // unit for text alignment

    /** constant for pixel based text alignment */
    public static final int PIXELS = 0;

    /** constant for bytes based text alignment */
    public static final int BYTES = 1;

    /** constant for character based alignment */
    public static final int CHARACTERS = 2;

    // font
    //	public static Font DEFAULTFONT = new Font("SansSerif", Font.PLAIN, 12);

    /** constant for the default font size for annotations */
    public static final int DEFAULT_FONT_SIZE = 12;

    /** the default font for printing */
    public static Font DEFAULTFONT = new Font("MS Arial Unicode", Font.PLAIN,
            DEFAULT_FONT_SIZE);

    /** the default preview font for character based alignment! */
    public static Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN,
            DEFAULT_FONT_SIZE);

    /** the default font size for time codes */
    public static final int TIMECODE_FONT_SIZE = 10;

    // empty line style

    /** constant for template style empty line style */
    public static final int TEMPLATE = 0;

    /** constant for an empty line style where empty lines are hidden */
    public static final int HIDE_EMPTY_LINES = 1;

    // sorting style

    /** sorting as read from file */
    public static final int EXTERNALLY_SPECIFIED = 0;

    /** sorting according t otier dependencies */
    public static final int TIER_HIERARCHY = 1;

    /** sorting by tier name */
    public static final int BY_NAME = 2;

    /** sorting by linguistic type */
    public static final int BY_LINGUISTIC_TYPE = 3;

    /** sorting by participant */
    public static final int BY_PARTICIPANT = 4;

    /** sorting by participant */
    public static final int BY_ANNOTATOR = 5;

    /** sorting by participant */
    public static final int BY_LANGUAGE = 6;

    /** random user defined sorting */
    public static final int USER_DEFINED = 7;

    /** constant for the mode where all timecodes are on a single line */
    public static final int SINGLE_TIMECODE = 0;

    /** constant for a timecode per root tier */
    public static final int MULTIPLE_TIMECODE = 1;
    
    /** default minimal silence duration  */
    public static final int MIN_SILENCE = 20;

    // character encoding

    /** output text encoding in utf8 */
    public static final int UTF8 = 0;

    /** iso-latin encoding */
    public static final int ISOLATIN = 1;

    /** sil-ipa encoding */
    public static final int SIL = 2;

    // output modes

    /** preview/output mode for printing */
    public static final int PRINT = 100;

    /** preview output mode for interlinear text */
    public static final int INTERLINEAR_TEXT = 101;

    /** output mode for shoebox/toolbox */
    public static final int SHOEBOX_TEXT = 102;
    
    /** output mode for interlinear html */
    public static final int HTML = 103;

    // constants for text out

    /** default line width */
    public static final int DEFAULT_NUM_CHARS = 80;

    /** space between label and contents */
    public static final int LABEL_VALUE_MARGIN = 3;

    /** default number of newlines between blocks */
    public static final int DEFAULT_TEXT_BLOCK_SPACING = 2;

    /** the transcription */
    private final TranscriptionImpl transcription;
    private int width;
    private int height;
    private List<Tier> visibleTiers;
    private boolean tierLabelsShown;
    private long[] visibleTimeInterval;
    private int blockWrapStyle;
    private int lineWrapStyle;
    private boolean timeCodeShown;
    private int timeCodeType;
    private boolean playSoundSel;			// CC 26/11/2010
    private String mediaURL;				// CC 03/12/2010
    private Map<String, Font> fonts;
    private Map<String, Integer> fontSizes;
    private boolean emptySlotsShown;
    private int lineSpacing;
    private int blockSpacing = -1;
    private int emptySpace; // the space between words/annotations 
    private Annotation activeAnnotation;
    private long[] selection;
    private long mediaTime;
    private int alignmentUnit;
    private int emptyLineStyle; // show full 'template' for block, or hide empty lines
    private int sortingStyle;
    private Map<String, Integer> charEncodings;
    private int outputMode;
    private int timeCodeMultiplicity;
    private BlockMetrics metrics;
    private int pageHeight;
    private boolean selectionOnly = false;
    private long timeOffset = 0L;
    //only applicable for text export, insert a space and a tab char between annotations 
    // for easier postprocessing in texteditors
    private boolean insertTabs = false;
    private boolean tabsReplaceSpaces = false;
    private boolean showPageNumber = true;
    public int pageNumberAreaHeight = DEFAULT_FONT_SIZE + 2;
    private final String EMPTY = "";	//CC
    
    private boolean showSilenceDuration = false;
    private int minSilenceDuration;
    
    private int noOfDecimalDigits;
    private int cachedLineWrapWidth = -1;

    // preferences keys

    /** pref key */
    final String prefSelectionOnly = "Interlinear.SelectionOnly";
    
    /** pref key */
    final String prefSilenceDurationShown = "Interlinear.SilenceDurationShown";
    
    /** pref key */
    final String prefMinSilenceDurationValue = "Interlinear.MinSilenceDurationValue";
    
    /** pref key */
    final String prefNoOfDecimalDigits = "NumberOfDecimalDigits";

    /** pref key */
    final String prefLabelsShown = "Interlinear.LabelsShown";

    /** pref key */
    final String prefTimeCodeShown = "Interlinear.TimeCodeShown";
    
    /** pref key */
    final String prefEmptyLineStyle = "Interlinear.EmptyLineStyle";
    
    /** pref key */
    final String prefTimeCodeType = "Interlinear.TimeCodeType";
    
    //CC
    /** pref key */
    final String prefPlaySoundSel = "Interlinear.PlayMediaSel";

    /** pref key */
    final String prefBlockWrapStyle = "Interlinear.BlockWrapStyle";

    /** pref key */
    final String prefLineWrapStyle = "Interlinear.LineWrapStyle";

    /** pref key */
    final String prefBlockSpacing = "Interlinear.BlockSpacing";

    /** pref key */
    final String prefBlockSpacingTextOut = "Interlinear.BlockSpacing.Text";

    /** pref key */
    final String prefLineSpacing = "Interlinear.LineSpacing";

    /** pref key */
    final String prefTierSortingStyle = "Interlinear.TierSortingStyle";

    /** pref key */
    final String prefVisibleTiers = "Interlinear.VisibleTiers";

    /** pref key */
    final String prefTierOrder = "Interlinear.TierOrder";

    /** pref key */
    final String prefFontSizes = "Interlinear.FontSizes";

    /** pref key */
    final String prefNumCharPerLine = "Interlinear.NumCharPerLine";
    
    /** pref key */
    final String prefHTMLPixWidth = "Interlinear.HTMLPixWidth";
    
    /** pref key */
    final String prefInsertTab = "Interlinear.InsertSpace";//should be InsertTab
    
    /** pref key */
    final String prefTabsInsteadOfSpaces = "Interlinear.TabsInsteadOfSpaces";
    
    /** pref key */
    final String prefPageNumber = "Interlinear.PageNumber";
    
    /** pref key */
    final String prefHiddenTiers = "Interlinear.HiddenTiers";
    
    /** pref key */
    final String prefTierSelectionMode = "Interlinear.SelectTiersMode";

    /**
     * Creates a new Interlinear instance. Default mode is Print mode
     *
     * @param tr the transcription holding tiers and annotations.
     */
    public Interlinear(TranscriptionImpl tr) {
        this(tr, PRINT);
    }

    /**
     * Creates a new Interlinear instance.
     *
     * @param tr the transcription holding tiers and annotations.
     * @param mode one of <code>PRINT</code>, <code>INTERLINEAR_TEXT</code>,
     *        <code>SHOEBOX_TEXT</code> or <code>HTML</code>
     */
    public Interlinear(TranscriptionImpl tr, int mode) {
        transcription = tr;

        if ((mode < PRINT) || (mode > HTML)) {
            outputMode = PRINT;
        } else {
            outputMode = mode;
        }

        if (mode == SHOEBOX_TEXT) {
        	metrics = new ToolboxMetrics(this);
        } else {
        	metrics = new BlockMetrics(this);
        }
        
        setDefaultValues();
    }

    /**
     * Resets the Metrics object.
     */
    private void resetMetrics() {
        metrics.reset();
    }

    /**
     * Initialises default values.
     */
    private void setDefaultValues() {
        if (outputMode == PRINT) {
            width = 550;
            pageHeight = 600;
            height = pageHeight;
            alignmentUnit = PIXELS;
            blockSpacing = 0;
            emptySpace = 10;
            emptySlotsShown = false;
        } else if (outputMode == INTERLINEAR_TEXT) {
            width = DEFAULT_NUM_CHARS;
            height = 800;
            pageHeight = height;
            alignmentUnit = CHARACTERS;
            emptySpace = 1;
            emptySlotsShown = false;
        } else if (outputMode == SHOEBOX_TEXT){
            width = DEFAULT_NUM_CHARS;
            height = 800;
            pageHeight = height;
            alignmentUnit = CHARACTERS;
            emptySpace = 1;
            emptySlotsShown = false;
        } else {
            // html
            width = 800;
            pageHeight = 800;
            height = 800;
            alignmentUnit = PIXELS;
            emptySpace = 10;
            emptySlotsShown = true;
        }

        tierLabelsShown = true;
        blockWrapStyle = BLOCK_BOUNDARY;
        lineWrapStyle = NEXT_LINE;
        timeCodeShown = false;
        timeCodeType = HHMMSSMS;
        
        minSilenceDuration = MIN_SILENCE;
        
        noOfDecimalDigits = Constants.TWO_DIGIT;
                
        emptyLineStyle = HIDE_EMPTY_LINES;
        sortingStyle = EXTERNALLY_SPECIFIED;

        timeCodeMultiplicity = MULTIPLE_TIMECODE;
        visibleTiers = new ArrayList<Tier>();

        // defaults for font and fontsizes
        fonts = new HashMap<String, Font>();
        fontSizes = new HashMap<String, Integer>();
        charEncodings = new HashMap<String, Integer>();

        // set default visible tiers to all tier names
        if (transcription != null) {
            visibleTiers.addAll(transcription.getTiers());

            Iterator<Tier> tierIt = visibleTiers.iterator();
            TierImpl t;

            while (tierIt.hasNext()) {
                t = (TierImpl) tierIt.next();

                if (outputMode == PRINT || outputMode == HTML) {
                    setFont(t.getName(), DEFAULTFONT);
                } else {
                    setFont(t.getName(), MONOSPACED_FONT);
                }

                setFontSize(t.getName(), DEFAULT_FONT_SIZE);
            }
        }

        loadPreferences();
    }

    /**
     * Since Interlinear can be used with or withou a ui, this method should be
     * called  by other objects: the ui/dialog it is in (on closing/disposing
     * of the ui) or by the CommandAction that created this Interlinear
     * object.
     */
    public void savePreferences() {
        Preferences.set(prefSelectionOnly, Boolean.valueOf(selectionOnly),
            transcription);
        Preferences.set(prefBlockWrapStyle, blockWrapStyle, null);
        Preferences.set(prefLabelsShown, Boolean.valueOf(tierLabelsShown), null);
        Preferences.set(prefLineSpacing, lineSpacing, null);
        Preferences.set(prefLineWrapStyle, lineWrapStyle, null);
        Preferences.set(prefTierSortingStyle, sortingStyle, null);
        Preferences.set(prefTimeCodeType, timeCodeType, null);
        Preferences.set(prefTimeCodeShown, Boolean.valueOf(timeCodeShown), null);
        Preferences.set(prefEmptyLineStyle, emptyLineStyle, null);
        
        // CC
        Preferences.set(prefPlaySoundSel, Boolean.valueOf(playSoundSel), null);     
        
        if(showSilenceDuration){
        	Preferences.set(prefSilenceDurationShown, Boolean.valueOf(showSilenceDuration), null);
        	Preferences.set(prefMinSilenceDurationValue, minSilenceDuration, null);
        	Preferences.set(prefNoOfDecimalDigits, noOfDecimalDigits , null);
        } else {
        	Preferences.set(prefSilenceDurationShown, Boolean.valueOf(showSilenceDuration), null);
        }

        //  ordered, visible tiers
        String[] tierNames = new String[visibleTiers.size()];
        List<String> tierNameList = new ArrayList<String>(visibleTiers.size());

        for (int i = 0; i < visibleTiers.size(); i++) {
            tierNames[i] = visibleTiers.get(i).getName();
            tierNameList.add(visibleTiers.get(i).getName());
        }

        Preferences.set(prefVisibleTiers, tierNameList, transcription);

        if (outputMode == PRINT) {
            Preferences.set(prefFontSizes, fontSizes, transcription);
            Preferences.set(prefBlockSpacing, blockSpacing, null);
            Preferences.set(prefPageNumber, Boolean.valueOf(showPageNumber), null);
        } else if (outputMode == HTML) {
            Preferences.set(prefFontSizes, fontSizes, transcription);
            Preferences.set(prefHTMLPixWidth, getWidth(), null);
            Preferences.set(prefBlockSpacingTextOut, blockSpacing, null);
            if (cachedLineWrapWidth > -1) {
            	// HS April 2016 store the value for number of chars in case of line wrap
            	Preferences.set(prefNumCharPerLine, cachedLineWrapWidth, null);
            }
        } else {
            Preferences.set(prefNumCharPerLine, width, null);
            Preferences.set(prefBlockSpacingTextOut, blockSpacing, null);
            if (outputMode == INTERLINEAR_TEXT) {
                Preferences.set(prefInsertTab, Boolean.valueOf(insertTabs), null);
                Preferences.set(prefTabsInsteadOfSpaces, Boolean.valueOf(tabsReplaceSpaces), null);
                if (cachedLineWrapWidth > -1) {
                	// HS April 2016 store the value for number of chars in case of line wrap
                	Preferences.set(prefNumCharPerLine, cachedLineWrapWidth, null);
                }
            }
        }
    }

    /**
     * Load stored preferences.
     */
    void loadPreferences() {
        Boolean boolPref;
        String stringPref;
        Integer intPref;
        
        boolPref = Preferences.getBool(prefSelectionOnly, transcription);

        if (boolPref != null) {
            setSelectionOnly(boolPref.booleanValue());
        }
        
        intPref = Preferences.getInt(prefEmptyLineStyle, null);

        if (intPref != null) {
            setEmptyLineStyle(intPref.intValue());
        }

        intPref = Preferences.getInt(prefBlockWrapStyle, null);

        if (intPref != null) {
            setBlockWrapStyle(intPref.intValue());
        }

        intPref = Preferences.getInt(prefLineWrapStyle, null);

        if (intPref != null) {
            setLineWrapStyle(intPref.intValue());
        }

        boolPref = Preferences.getBool(prefLabelsShown, null);

        if (boolPref != null) {
            setTierLabelsShown(boolPref.booleanValue());
        }

        boolPref = Preferences.getBool(prefTimeCodeShown, null);

        if (boolPref != null) {
            setTimeCodeShown(boolPref.booleanValue());
        }

        intPref = Preferences.getInt(prefTimeCodeType, null);

        if (intPref != null) {
            setTimeCodeType(intPref.intValue());
        }
        
        // CC 26/11/2010
        boolPref = Preferences.getBool(prefPlaySoundSel, null);

        if (boolPref != null) {
            setPlaySoundSel(boolPref.booleanValue());
        }
        
        boolPref = Preferences.getBool(prefSilenceDurationShown, null);

        if (boolPref != null) {
        	setShowSilenceDuration((boolPref));
        }
        
        intPref = Preferences.getInt(prefMinSilenceDurationValue, null);

        if (intPref != null) {
            setMinSilenceDuration(intPref.intValue());
        }
        
        intPref = Preferences.getInt(prefNoOfDecimalDigits, null);
        if (intPref != null) {
        	setNumOfDecimalDigits(intPref.intValue());
        	Preferences.set("Interlinear.prefNoOfDecimalDigits", null, null);
        } else {
        	intPref = Preferences.getInt("Interlinear.prefNoOfDecimalDigits", null);
            if (intPref != null) {
            	Preferences.set(prefNoOfDecimalDigits, intPref, null);
            	Preferences.set("Interlinear.prefNoOfDecimalDigits", null, null);
            	setNumOfDecimalDigits(intPref.intValue());
            }
        }      
        
        List<String> visList = Preferences.getListOfString(prefVisibleTiers, transcription);

//        	if (curPref instanceof String[]) {
//	            String[] vis = (String[]) curPref;
//
//	            for (String vi : vis) {
//	                name = vi;
//	
//	                t = transcription.getTierWithId(name);
//	
//	                if (t != null) {
//	                    allVis.add(t);
//	                }
//	            }
//        	} else {
    	List<Tier> allVis = new ArrayList<Tier>();
        if (visList != null) {
            
	            for (String name : visList) {
	                TierImpl t = transcription.getTierWithId(name);
	
	                if (t != null) {
	                    allVis.add(t);
	                }
	            }
        	}
        	
        	List<String> allOrdered = Preferences.getListOfString(prefTierOrder, transcription);
            List<String> at;

            if (allOrdered != null) {
                at = allOrdered; // saved ordered list of tiers
            } else {
                at = new ArrayList<String>(0);
            }

            // add new tiers to the list of visible tiers
            List<TierImpl> v = transcription.getTiers();

            for (TierImpl t : v) {
                String name = t.getName();

                if (!allVis.contains(t) && !at.contains(name)) {
                    allVis.add(t);
                }
            }

            setVisibleTiers(allVis);

        if (outputMode == PRINT || outputMode == HTML) {
            Map<String, Integer> pref = Preferences.getMapOfInt(prefFontSizes, transcription);

            if (pref != null) {
                fontSizes = pref;

                for (Map.Entry<String, Integer> e : fontSizes.entrySet()) {
	                String tierName = e.getKey();
	                Integer size = e.getValue();

	                setFontSize(tierName, size.intValue());
                }
            }
            
            // preferred fonts
            Map<String, Font> foMap = Preferences.getMapOfFont("TierFonts", transcription);
            
    		if (foMap != null) {
    			for (Map.Entry<String, Font> e : foMap.entrySet()) {
    				String key = e.getKey();
    				Font ft = e.getValue();
    				
    				if (key != null && ft != null) {
    					setFont(key, ft);
    				}
    			}
    		}

    		intPref = Preferences.getInt(prefLineSpacing, null);

            if (intPref != null) {
                setLineSpacing(intPref.intValue());
            }

            intPref = Preferences.getInt(prefBlockSpacing, null);

            if (intPref != null) {
                setBlockSpacing(intPref.intValue());
            }
            
            if (outputMode == PRINT) {
	            boolPref = Preferences.getBool(prefPageNumber, null);
	            
	            if (boolPref != null) {
	            	setShowPageNumber(boolPref.booleanValue());
	            }
            }
        }

        if (outputMode == HTML) {
            intPref = Preferences.getInt(prefHTMLPixWidth, null);
            
            if (intPref != null) {
                setWidth(intPref.intValue());
            }
            
            intPref = Preferences.getInt(prefBlockSpacingTextOut, null);

            if (intPref != null) {
                setBlockSpacing(intPref.intValue());
            }
        } else if (outputMode != PRINT) {
            intPref = Preferences.getInt(prefNumCharPerLine, null);

            if (intPref != null) {
                setWidth(intPref.intValue());
                cachedLineWrapWidth = intPref;
            }

            intPref = Preferences.getInt(prefBlockSpacingTextOut, null);

            if (intPref != null) {
                setBlockSpacing(intPref.intValue());
            }
        }
        
        if (outputMode == INTERLINEAR_TEXT) {
            boolPref = Preferences.getBool(prefInsertTab, null);
            if (boolPref != null) {
                setInsertTabs(boolPref.booleanValue());
            }
            boolPref = Preferences.getBool(prefTabsInsteadOfSpaces, null);
            if (boolPref != null) {
                setTabsReplaceSpaces(boolPref.booleanValue());
            }
        }
    }

    /**
     * Initialises a reset and recalculation of the annotation blocks and
     * pages, using the graphics of the specified BufferedImage.
     *
     * @param bi the BufferedImage object, provided by a preview renderer
     */
    public void renderView(BufferedImage bi) {
        calculateMetrics(bi.getGraphics());
    }

    /**
     * Initialises a reset and recalculation of the annotation blocks,  based
     * on character alignment.
     */
    public void renderView() {
        calculateMetrics();
    }

    /**
     * Calls a suitable renderer to render the contents of the specified page
     * to the specified Graphics object.
     *
     * @param g the Graphics object
     * @param pageIndex the page to render
     *
     * @return true if the page was successfully rendered
     */
    public boolean renderPage(Graphics g, int pageIndex) {
        if (alignmentUnit == PIXELS) { // call to renderView should be consistent with params
            PixelRenderer.render(this, g, pageIndex);

            return true;
        }

        return false;
    }

    /**
     * Calculate annotation widths in blocks (including dependent annotations),
     * calculate print blocks, a combination of annotation blocks that fit
     * (partially) on a block line and calculate pages / page breaks.
     *
     * @param graphics the graphics object to use for the calculations
     */
    protected void calculateMetrics(Graphics graphics) {
        resetMetrics();

        metrics.calculateAnnotationBlocks(graphics);

        metrics.calculatePrintBlocks();

        metrics.calculatePageBreaks();

        setHeight(pageHeight * metrics.getPageBreaks().size());
    }

    /**
     * Calculates metrics based on byte-wise or character wise alignment.
     */
    protected void calculateMetrics() {
        resetMetrics();

        metrics.calculateAnnotationBlocks(null);

        metrics.calculatePrintBlocks();

        // this creates a single page breaks object
        metrics.calculatePageBreaks();
    }

    /**
     * Renders the view to the specifeid BufferedImage using the specied
     * offsets. In case of a character based interlinearization the
     * <code>PixelRenderer</code> converts the character positions to pixel
     * based  positions in order to create a preview of the final text output.
     *
     * @param bi the BufferedImage object to render to
     * @param offset the hor. and vert. offset
     */
    public void drawViewOnImage(BufferedImage bi, int[] offset) {
        if (alignmentUnit == PIXELS) { // call to renderView should be consistent with params
            PixelRenderer.render(this, bi, offset);
        } else {
            PixelRenderer.renderCharacterPreview(this, bi, offset);
        }
    }

    // getters and setters

    /**
     * Returns the active annotation
     *
     * @return the active annotation
     */
    public Annotation getActiveAnnotation() {
        return activeAnnotation;
    }

    /**
     * Returns the alignment unit type.
     *
     * @return the alignment unit type, one of <code>PIXELS</code>,
     *         <code>BYTES</code> or <code>CHARACTERS</code>
     */
    public int getAlignmentUnit() {
        return alignmentUnit;
    }

    /**
     * Returns the block wrap style type.
     *
     * @return the block wrap style type, one of <code>NO_WRAP</code>,
     *         <code>EACH_BLOCK</code>,  <code>BLOCK_BOUNDARIES</code> or
     *         <code>WITHIN_BLOCKS</code>
     */
    public int getBlockWrapStyle() {
        return blockWrapStyle;
    }

    /**
     * Returns whether empty slots are shown.
     *
     * @return true when empty slot are painted, false otherwise
     */
    public boolean isEmptySlotsShown() {
        return emptySlotsShown;
    }

    /**
     * Returns the Font to use for the specified tier.
     *
     * @param tierName the name of the tier
     *
     * @return the font for printing or preview
     */
    public Font getFont(String tierName) {
        Font f = fonts.get(tierName);

        if (f == null) { // use default font
            f = DEFAULTFONT;
        }

        return f;
    }

    /**
     * Returns the font size used for the specified tier
     *
     * @param tierName the name of the tier
     *
     * @return the size of the font for this tier
     */
    public int getFontSize(String tierName) {
        int size = 0;
        Integer sizeInt = fontSizes.get(tierName);

        if (sizeInt != null) {
            size = sizeInt.intValue();
        } else {
            size = DEFAULT_FONT_SIZE;
        }

        return size;
    }

    /**
     * Returns the total height necessary for rendering
     *
     * @return the total height
     */
    public int getHeight() {
        if (height > 0) {
            return height;
        } else {
            return 0;
        }
    }

    /**
     * Returns the number of pixels to add between lines.
     *
     * @return the number of pixels to add between lines
     */
    public int getLineSpacing() {
        return lineSpacing;
    }

    /**
     * Returns the number of pixels to add between blocks.
     *
     * @return the number of pixels to add between blocks.
     */
    public int getBlockSpacing() {
        if (blockSpacing < 0) { // default: derived from line spacing

            if (outputMode == PRINT) {
                return 20 + (3 * getLineSpacing());
            } else {
                return DEFAULT_TEXT_BLOCK_SPACING;
            }
        } else {
            return blockSpacing;
        }
    }

    /**
     * Set the number of pixels to add between blocks.
     *
     * @param blockSpacing the number of pixels to add between blocks.
     */
    public void setBlockSpacing(int blockSpacing) {
        this.blockSpacing = blockSpacing;
    }
    
    /**
     * Returns the line wrap style.
     *
     * @return either <code>NO_WRAP</code> or <code>NEXT_LINE</code>
     */
    public int getLineWrapStyle() {
        return lineWrapStyle;
    }

    /** Return the main media file .
     *  CC 03/12/2010
     * @param  : the  of the media file
     */
    public String getMediaURL() throws IOException {
            List<MediaDescriptor> mds = transcription.getMediaDescriptors();
            if ((mds != null) && (mds.size() > 0)) {
            	MediaDescriptor md = mds.get(0);
            	if ((md.mediaURL != null) && !md.mediaURL.equals(EMPTY)) {
            		mediaURL = md.mediaURL;
                }
            }
            else { mediaURL=""; }
            return mediaURL;
    }
    
    /**
     * Used in combination with {@link #isPlaySoundSel()} and {@link #getMediaURL()}.
     * Determines what type of html 5 element is used for a media player.
     * @return true if the main media file is video, false otherwise 
     */
    public boolean isMediaVideo() {
        List<MediaDescriptor> mds = transcription.getMediaDescriptors();
        if ((mds != null) && (mds.size() > 0)) {
        	MediaDescriptor md = mds.get(0);
        	if ((md.mimeType != null) && md.mimeType.startsWith("video")) {
        		return true;
            }
        }
    	return false;
    }
    
    /**
     * Returns the current media time.
     *
     * @return the current media time
     */
    public long getMediaTime() {
        return mediaTime;
    }

    /**
     * Returns the selection begin and end time.
     *
     * @return the selection begin and end time
     */
    public long[] getSelection() {
        return selection;
    }

    /**
     * Returns whether or not the tier labels should be included in the output.
     *
     * @return true if the tier labels are included in the left margin, false
     *         otherwise
     */
    public boolean isTierLabelsShown() {
        return tierLabelsShown;
    }
    
    // CC
    public boolean isPlaySoundSel() {
        return playSoundSel;
    }

    /**
     * Returns whether or not time information of each block should be included
     * in the  output.
     *
     * @return true if time information is included in the output, false
     *         otherwise
     */
    public boolean isTimeCodeShown() {
        return timeCodeShown;
    }

    /**
     * Returns the type of time codes to use.
     *
     * @return the timecode type, one of <code>HHMMSSMS</code>, or
     *         <code>SSMS</code>
     */
    public int getTimeCodeType() {
        return timeCodeType;
    }

    /**
     * Returns a list of the currently visible tiers.
     *
     * @return a sorted list of the visible tier objects
     */
    public List<Tier> getVisibleTiers() {
        return visibleTiers;
    }

    /**
     * Returns the currently visible time interval.
     *
     * @return the currently visible time interval, begin and end time
     */
    public long[] getVisibleTimeInterval() {
        return visibleTimeInterval;
    }

    /**
     * Returns the width of the output area, either in pixels or in number of
     * bytes or characters
     *
     * @return the output width
     */
    public int getWidth() {
        if (width > 0) {
            return width;
        } else {
            // find width from max horizontally used space
            //return metrics.getMaxHorizontallyUsedWidth();
            return 0;
        }
    }

    /**
     * Sets the active annotation.
     *
     * @param annotation the active annotation
     */
    public void setActiveAnnotation(Annotation annotation) {
        activeAnnotation = annotation;
    }

    /**
     * Sets the alignment unit.
     *
     * @param i the new alignment unit
     *
     * @see #getAlignmentUnit()
     */
    public void setAlignmentUnit(int i) {
        alignmentUnit = i;
    }

    /**
     * Sets the block wrap style type.
     *
     * @param i the new block wrap style type
     *
     * @see #getBlockWrapStyle()
     */
    public void setBlockWrapStyle(int i) {
        blockWrapStyle = i;
    }

    /**
     * Sets the empty slots shown value.
     *
     * @param b the new empty slots shown value
     *
     * @see #getEmptyLineStyle()
     */
    public void setEmptySlotsShown(boolean b) {
        emptySlotsShown = b;
    }

    /**
     * Sets the (total) height necessary for the output.
     *
     * @param i the new height
     */
    public void setHeight(int i) {
        height = i;
    }

    /**
     * Sets the number of pixels to insert between lines.
     *
     * @param i the space between two lines
     */
    public void setLineSpacing(int i) {
        lineSpacing = i;
    }

    /**
     * Sets the line wrap style.
     *
     * @param i the line wrap style
     *
     * @see #getLineWrapStyle()
     */
    public void setLineWrapStyle(int i) {
        lineWrapStyle = i;
    }

    /**
     * Sets the current media time.
     *
     * @param l the time in ms
     */
    public void setMediaTime(long l) {
        mediaTime = l;
    }

    /**
     * Sets the selction begin and end time
     *
     * @param ls array of length 2, begin and end time
     */
    public void setSelection(long[] ls) {
        selection = ls;
    }

    /**
     * Sets the visibility of the tier labels.
     *
     * @param show if true include the labels
     */
    public void setTierLabelsShown(boolean show) {
        tierLabelsShown = show;
    }

    /**
     * Sets whether time code information should be included in the output.
     *
     * @param b when true time codes are included in the output
     */
    public void setTimeCodeShown(boolean b) {
        timeCodeShown = b;
    }
    
    /**
     * Sets whether silence duration should be included in the output.
     *
     * @param b when true silence duration are included in the output
     */
    public void setShowSilenceDuration(boolean b) {
        showSilenceDuration = b;
    }
    
    /**
     * Returns whether or not the silence duration should be included in the output.
     *
     * @return true if the silence duration are included in the output, false
     *         otherwise
     */
    public boolean isShowSilenceDuration() {
       return showSilenceDuration;
    } 
    
    /**
     * Sets the minimum silence duration value .
     *
     * @param i the min silence duration value in ms
     */
    public void setMinSilenceDuration(int i) {
        minSilenceDuration = i;
    }
    
    /**
     * Gets the minimum silence duration value .
     *
     * @return  the min silence duration value in ms
     */
    public int getMinSilenceDuration() {
        return minSilenceDuration ;
    }

    /**
     * Sets the format for the time codes.
     *
     * @param i the format type
     *
     * @see #getTimeCodeType()
     */
    public void setTimeCodeType(int i) {
        timeCodeType = i;
    }
    
    public void setPlaySoundSel(boolean b) {  // CC 26/11/2010
        playSoundSel = b;
    }
    
    /**
     * Returns the number of digits after decimal point.
     *
     * @return the number of digits.
     */
    public int getNumOfDecimalDigits() {
        return noOfDecimalDigits;
    }

   /**Set the number of digits after the decimal point
    * 
    * @param digits the number of digits after the decimal point
    */
    public void setNumOfDecimalDigits(int digits) {
        this.noOfDecimalDigits = digits;
    }

    /**
     * Sets the visible tiers.
     *
     * @param strings array of the names of the visible tiers
     */
    public void setVisibleTiers(String[] strings) {
        visibleTiers.clear();

        if (strings != null) {
            for (String string : strings) {
                Tier t = transcription.getTierWithId(string);

                if (t != null) {
                    visibleTiers.add(t);
                }
            }
        }
    }

    /**
     * Sets the visible tiers.
     *
     * @param visTiers a list of tier objects
     */
    public void setVisibleTiers(List<Tier> visTiers) {
        if (visTiers != null) {
            visibleTiers = visTiers;
        } else {
            visibleTiers.clear();
        }
    }

    /**
     * Sets the visible time interval.
     *
     * @param ls the begin and end time of the visible time interval, in ms
     */
    public void setVisibleTimeInterval(long[] ls) {
        visibleTimeInterval = ls;
    }

    /**
     * Sets the width of the document, in pixels, bytes or characters
     *
     * @param i the new width
     *
     * @see #getWidth()
     */
    public void setWidth(int i) {
        width = i;
        if (i != Integer.MAX_VALUE && 
        		(getOutputMode() == Interlinear.INTERLINEAR_TEXT || getOutputMode() == Interlinear.HTML)) {
        	cachedLineWrapWidth = i;
        }
    }

    /**
     * Creates a font object to use for the specified tier, using the
     * preferred, user defined font size.
     *
     * @param tierName the name of the tier
     * @param f the base font object for the tier
     */
    public void setFont(String tierName, Font f) {
        int fontSize = getFontSize(tierName);
        f = f.deriveFont((float) fontSize);

        fonts.put(tierName, f);
    }

    /**
     * Sets the font size for the specified tier.
     *
     * @param tierName the namew of the tier
     * @param size the size for the font
     */
    public void setFontSize(String tierName, int size) {
        fontSizes.put(tierName, Integer.valueOf(size));
        fonts.put(tierName, getFont(tierName).deriveFont((float) size));
    }

    /**
     * Returns the empty line style.
     *
     * @return the empty line style, either <code>TEMPLATE</code> or
     *         <code>HIDE_EMPTY_LINES</code>
     */
    public int getEmptyLineStyle() {
        return emptyLineStyle;
    }

    /**
     * Sets the empty line style.
     *
     * @param i empty line style, either<code>TEMPLATE</code> or
     *        <code>HIDE_EMPTY_LINES</code>
     */
    public void setEmptyLineStyle(int i) {
        emptyLineStyle = i;
    }

    /**
     * Returns the sorting style (sorting of the tiers).
     *
     * @return the sorting style, either <code>EXTERNALLY_SPECIFIED</code>,
     *         <code>TIER_HIERARCHY</code>, <code>BY_LINGUISTIC_TYPE</code> or
     *         <code>BY_PARTICIPANT</code>
     */
    public int getSortingStyle() {
        return sortingStyle;
    }

    /**
     * Sets the sorting style for the tiers.
     *
     * @param i the new sorting style
     *
     * @see #getSortingStyle()
     */
    public void setSortingStyle(int i) {
        sortingStyle = i;
    }

    /**
     * Returns the Transcription object.
     *
     * @return the Transcription object
     */
    public TranscriptionImpl getTranscription() {
        return transcription;
    }

    /**
     * Returns the output mode.
     *
     * @return the output mode, either <code>PRINT</code>,
     *         <code>INTERLINEAR_TEXT</code>  or <code>SHOEBOX_TEXT</code>
     */
    public int getOutputMode() {
        return outputMode;
    }

    /**
     * Sets the output mode.
     *
     * @param mode the output mode
     *
     * @see #getOutputMode()
     */
    public void setOutputMode(int mode) {
        outputMode = mode;
    }

    /**
     * Returns the page height, in case of <code>PRINT</code> mode.
     *
     * @return the page height in pixels
     */
    public int getPageHeight() {
        return pageHeight;
    }

    /**
     * Sets the page heigth.
     *
     * @param height page height in pixels
     */
    public void setPageHeight(int height) {
        pageHeight = height;
    }

    /**
     * Returns the character encoding for a particular tier.
     *
     * @param tierName the tier to find the encoding for
     *
     * @return the character encoding
     */
    public int getCharEncoding(String tierName) {
        int encoding = UTF8;

        if (tierName == null) {
            return encoding; // UTF8 is always the default
        }

        if (!charEncodings.containsKey(tierName)) {
            return encoding;
        }

        Integer encodingInt = charEncodings.get(tierName);

        if (encodingInt != null) {
            encoding = encodingInt.intValue();
        }

        return encoding;
    }

    /**
     * Sets the character encoding for a tier.
     *
     * @param tierName the tier to specify the encoding for
     * @param charEncoding the new encoding, either <code>UTF8</code>
     *        (default),  <code>ISOLATIN</code>, <code>SIL</code>
     */
    public void setCharEncoding(String tierName, int charEncoding) {
        charEncodings.put(tierName, Integer.valueOf(charEncoding));
    }

    /**
     * Returns the empty horizontal space between neightbouring annotations.
     *
     * @return the empty horizontal space between neightbouring annotations, in
     *         pixels or in number of characters
     */
    public int getEmptySpace() {
        return emptySpace;
    }

    /**
     * Returns the BlockMetrics object containing the formatted annotation
     * layout  for output.
     *
     * @return the object holding the formatted annotation output layout
     */
    public BlockMetrics getMetrics() {
        return metrics;
    }

    /**
     * Returns whether the output is restricted to the selected time interval.
     *
     * @return whether the output is restricted to the selected time interval
     */
    public boolean isSelectionOnly() {
        return selectionOnly;
    }

    /**
     * Sets whether the output is restricted to the selected time interval.
     *
     * @param b if true only the annotations in the selected interval are
     *        included in the output
     */
    public void setSelectionOnly(boolean b) {
        selectionOnly = b;
    }

    /**
     * Sets whether a single time code line should be used or rather a time
     * code line  per root tier. (Not used yet)
     *
     * @param style single Time code line or multiple time code lines, one of
     *        <code>SINGLE_TIMECODE</code>, or <code>MULTIPLE_TIMECODE</code>.
     */
    public void setTimeCodeMultiplicity(int style) {
        timeCodeMultiplicity = style;
    }

    /**
     * Returns whether a single time code line should be used or rather a time
     * code line  per root tier.
     *
     * @return one of <code>SINGLE_TIMECODE</code>, or
     *         <code>MULTIPLE_TIMECODE</code>.
     */
    public int getTimeCodeMultiplicity() {
        return timeCodeMultiplicity;
    }
    
    /**
     * @return Returns whether a tab should be inserted between annotations
     */
    public boolean isInsertTabs() {
        return insertTabs;
    }
    
    /**
     * Sets whether a tab should be inserted between annotations
     * 
     * @param insertTabs if true a tab will be inserted (together with a space char
     */
    public void setInsertTabs(boolean insertTabs) {
        this.insertTabs = insertTabs;
    }
    
    /**
     * 
     * @return whether a tab should replace white spaces between annotations
     */
    public boolean isTabsReplaceSpaces() {
    	return tabsReplaceSpaces;
    }
    
    /**
     * Sets whether a tab should replace whitespaces between annotations
     * @param tabsReplaceSpaces if true a tab is used to separate two annotations
     */
    public void setTabsReplaceSpaces(boolean tabsReplaceSpaces) {
    	this.tabsReplaceSpaces = tabsReplaceSpaces;
    }

    /**
     * Returns the (master media) time offset.
     * 
     * @return the (master media) time offset
     */
	public long getTimeOffset() {
		return timeOffset;
	}

	/**
	 * Sets the (master media) time offset.
	 * @param timeOffset the time offset
	 */
	public void setTimeOffset(long timeOffset) {
		this.timeOffset = timeOffset;
	}
	
	/**
	 * Returns whether a page number should be printed (print mode only).
	 * 
	 * @return the include page number flag
	 */
	public boolean isShowPageNumber() {
		return showPageNumber;
	}

	/**
	 * Sets whether a page number should be printed (print mode only).
	 * 
	 * @param showPageNumber if true a page number will be printed
	 */
	public void setShowPageNumber(boolean showPageNumber) {
		this.showPageNumber = showPageNumber;
	}
}
