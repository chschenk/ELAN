package mpi.eudico.client.annotator.viewer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
//import mpi.eudico.client.annotator.ISOCATLanguageCodeFactory;
import mpi.eudico.client.annotator.InlineEditBoxListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShowInBrowserCommand;
import mpi.eudico.client.annotator.comments.CommentEnvelope;
import mpi.eudico.client.annotator.comments.CommentManager;
import mpi.eudico.client.annotator.dcr.ELANLocalDCRConnector;
import mpi.eudico.client.annotator.gui.InlineEditBox;
import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayer;
import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerFactory;
import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerHost;
import mpi.eudico.client.annotator.util.AnnotationTransfer;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.DragTag2D;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.annotator.util.Tag2D;
import mpi.eudico.client.annotator.util.Tier2D;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.client.util.TierAssociation;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.TimeFormatter;


/**
 * This viewer shows annotations of multiple tiers relative to a time scale.<br>
 * The value of each tag is truncated in such a way that it does not extent
 * beyond the available space at a given resolution.
 *
 * @author Han Sloetjes
 * @version 0.1 2/7/2003
 * @version Aug 2005 Identity removed
 * @version June 2008 msPerPixel changed to float, new zooming functions
 */
@SuppressWarnings("serial")
public class TimeLineViewer extends TimeScaleBasedViewer
    implements ComponentListener, MouseListener, MouseMotionListener,
        MouseWheelListener, KeyListener, AdjustmentListener, ActionListener, GesturesListener,
        MultiTierViewer, ACMEditListener, ChangeListener, InlineEditBoxListener,
        MediaDisplayerHost {
    /** default number of pixels that represents one second */
    static final int PIXELS_FOR_SECOND = 100;
    private TranscriptionImpl transcription;
    private boolean timeRulerVisible;
    private int rulerHeight;
    private TimeRuler ruler;
    private Font font;
    private Font tooltipFont;
    private FontMetrics metrics;
    private BufferedImage bi;
    private Graphics2D big2d;
    private AlphaComposite alpha04;
    private AlphaComposite alpha07;
    private BasicStroke stroke;
    private BasicStroke stroke2;
    private Color dropHighlightColor;
    //private Color selectionColorAlpha4;
    //private Color activeTierAlpha7;
    //private BasicStroke stroke3;
    private HashMap<String, Object> prefTierFonts;
    private Map<String,Color> highlightColors;
    private float msPerPixel;
    
    /** default value of milliseconds per pixel */
    public final int DEFAULT_MS_PER_PIXEL = 10;
    
    static final private Color COMMENT_INDICATOR_INTERIOR_COLOR =  new Color(128, 240, 128);	// light green
    static final private Color COMMENT_INDICATOR_EDGE_COLOR =  new Color(64, 128, 255);			// darker blue
   	static final private AlphaComposite COMMENT_INDICATOR_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 96f/255);

    /**
     * The resolution in number of pixels for a second. This is not a
     * percentage value. Historically resolution = PIXELS_FOR_SECOND  factor,
     * where factor = 100 / menu_resolution_percentage_value.
     */
    private int resolution;
    private int imageWidth;
    private int imageHeight;
    private long crossHairTime;
    private int crossHairPos;
    private long intervalBeginTime;
    private long intervalEndTime;
    private int verticalScrollOffset;
    private int horizontalScrollOffset;
    private long selectionBeginTime;
    private long selectionEndTime;
    private int selectionBeginPos;
    private int selectionEndPos;
    private long dragStartTime;
    private Point dragStartPoint;
    private Point dragEndPoint;
    private long splitTime;
    private Color symAnnColor = Constants.SHAREDCOLOR1;

    /** width of border area where auto scrolling starts */
    public final int SCROLL_OFFSET = 16;
    private DragScroller scroller;
    private JPopupMenu popup;
    private ButtonGroup zoomBG;
    private JMenu zoomMI;
    private JRadioButtonMenuItem customZoomMI;
    private ButtonGroup fontSizeBG;
    private JMenu fontMenu;
    private JCheckBoxMenuItem timeScaleConMI;
    private JCheckBoxMenuItem activeAnnStrokeBoldMI;
    private JCheckBoxMenuItem hScrollBarVisMI;
    private JCheckBoxMenuItem tickerModeMI;
    private JCheckBoxMenuItem timeRulerVisMI;
    private JCheckBoxMenuItem reducedTierHeightMI;

    private int staticMenuItems;
    // menu items that can be enabled / disabled
    private JMenuItem newAnnoMI;
    private JMenuItem newAnnoBeforeMI;
    private JMenuItem newAnnoAfterMI;
    private JMenuItem modifyAnnoMI;
    private JMenuItem modifyAnnoDCMI;
    private JMenuItem showInBrowserMI;
    private JMenuItem mergeAnnoNextMI;
    private JMenuItem mergeAnnoBeforeMI;
    private JMenuItem splitAnnotationMI;
    private JMenuItem modifyAnnoTimeMI;
    private JMenuItem modifyAnnoTimeDlgMI;
    private JMenuItem deleteAnnoValueMI;
    private JMenuItem deleteAnnoMI;
    private JMenuItem zoomSelectionMI;
    private JMenuItem zoomToEntireMediaMI;
    private JMenuItem deleteSelAnnosMI;
    // copy / paste menu items
    private JMenuItem copyAnnoMI;
    private JMenuItem copyAnnoTreeMI;
    private JMenuItem pasteAnnoHereMI;
    private JMenuItem pasteAnnoTreeHereMI;
    private JMenuItem shiftActiveAnnMI;
    private List <JMenuItem> contextSensitiveItems;
    
    private boolean timeScaleConnected;
    private boolean panMode;
    private boolean tickerMode = false;

    // do or don't show empty slots on a child tier
    private boolean showEmptySlots;
    private boolean aaStrokeBold;

    /** Holds value of property DOCUMENT ME! */
    protected int pixelsForTierHeight;

    /** Holds value of property DOCUMENT ME! */
    protected int pixelsForTierHeightMargin;

    //new storage fields
    private ArrayList<Tier2D> allTiers;
    private List<Tier2D> visibleTiers;
    private Tag2D hoverTag2D;
    private int hoverTierIndex;
    private List<Tag2D> selectedAnnotations;

    /** Holds value of property DOCUMENT ME! */
    protected Tag2D cursorTag2D;
    private int cursorTierIndex;
    private Tier2D rightClickTier;
    private long rightClickTime;

    // vertical scrolling
    private JScrollBar scrollBar;
    private JScrollBar hScrollBar;
    private boolean hScrollBarVisible = true;
    private JPanel zoomSliderPanel;
    private JSlider zoomSlider;
    private final int ZOOMSLIDER_WIDTH = 100;
    private JPanel corner;
    private int horScrollSpeed = 10;

    /** default scrollbar width */
    private final int defBarWidth;
    private int[] tierYPositions;
    private int tooltipFontSize;

    /** ar the control panel that receives the setTierPositions call */
    MultiTierControlPanel multiTierControlPanel;

    // editing
    private InlineEditBox editBox;
    private boolean deselectCommits = true;
    private boolean forceOpenControlledVocabulary = false;
    private DragTag2D dragEditTag2D;
    private boolean dragEditing;
    private Color dragEditColor = Color.green;

    /** Holds value of property DOCUMENT ME! */
    private final int DRAG_EDIT_MARGIN = 8;

    /** Holds value of property DOCUMENT ME! */
    private final int DRAG_EDIT_CENTER = 0;

    /** Holds value of property DOCUMENT ME! */
    private final int DRAG_EDIT_LEFT = 1;

    /** Holds value of property DOCUMENT ME! */
    private final int DRAG_EDIT_RIGHT = 2;
    private final int DRAG_EDIT_UP = 3;
    private final int DRAG_EDIT_DOWN = 4;
    private int dragEditMode = 0;
    
    private boolean clearSelOnSingleClick= true;

    // the parent's boundaries
    private long dragParentBegin = -1L;
    private long dragParentEnd = -1L;

    private boolean reducedTierHeight = false;
    /** How many pixels under the line */
    private int rhDist = 3;
    
    // a flag for the scroll thread
    /** Holds value of property DOCUMENT ME! */
    boolean stopScrolling = true;
    private Object tierLock = new Object();
    /** a flag to decide whether to use a BufferedImage or not. This is always advised but 
     * leads to strange painting artifacts on some systems (XP/Vista, jre version and graphics
     * hardware/driver may play a role) */
    private boolean useBufferedImage = false;
        
    private boolean centerAnnotation = true;
    private boolean delayedAnnotationActivation = false;
    private boolean activateNewAnnotation = true;
    private Annotation lastCreatedAnnotation;
	private Tag2D previousHoverTag2D = null;
	private MediaDisplayer mediaDisplayer;

    /**
     * Constructs a new TimeLineViewer.<br>
     * Takes care of some one time initialization and adds listeners.
     */
    public TimeLineViewer() {
        initViewer();
        initTiers();
        defBarWidth = getDefaultBarWidth() + 2;
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setOpaque(true);
        setDoubleBuffered(true);
//        String bufImg = System.getProperty("useBufferedImage");
//        if (bufImg != null && bufImg.toLowerCase().equals("true")) {
//        	useBufferedImage = true;
//        }
        useBufferedImage = SystemReporting.useBufferedPainting;
        //System.out.println("TL initial use buffered image: " + useBufferedImage);
    }

    /**
     * Constructs a new TimeLineViewer using the specified transcription.<br>
     * Calls the no-arg constructor first.
     *
     * @param transcription the transcription containing the data for the
     *        viewer
     */
    public TimeLineViewer(Transcription transcription) {
        this();
        this.transcription = (TranscriptionImpl) transcription;
        paintBuffer();
        initTiers(); 
    }

    /**
     * Overrides <code>JComponent</code>'s processKeyBinding by always
     * returning false. Necessary for the proper working of (menu) shortcuts
     * in Elan.
     *
     * @param ks DOCUMENT ME!
     * @param e DOCUMENT ME!
     * @param condition DOCUMENT ME!
     * @param pressed DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
        int condition, boolean pressed) {
        return false;
    }

    /**
     * Performs the initialization of fields and sets up the viewer.<br>
     */
    private void initViewer() {
        font = Constants.DEFAULTFONT;
        setFont(font);
        metrics = getFontMetrics(font);
        tooltipFontSize = getDefaultTooltipFontSize();
        tooltipFont = font.deriveFont((float) tooltipFontSize);
        prefTierFonts = new HashMap<String, Object>();
        selectedAnnotations = new ArrayList<Tag2D>(10);
        
        // Keep the tool tip showing
        int dismissDelay = Integer.MAX_VALUE;
        ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);

        timeRulerVisible = true;
        if (Constants.DEFAULT_LF_LABEL_FONT != null) {
        	ruler = new TimeRuler(Constants.deriveSmallFont(Constants.DEFAULT_LF_LABEL_FONT),
        			TimeFormatter.toString(0), 5);
        } else {
        	ruler = new TimeRuler(font, TimeFormatter.toString(0), 5);
        }        
        rulerHeight = ruler.getHeight();
        stroke = new BasicStroke();
        stroke2 = new BasicStroke(2.0f);
        //stroke3 = new BasicStroke(3.0f);
        dropHighlightColor = new Color(255, 255, 255, 192);
        //selectionColorAlpha4 = new Color(Constants.SELECTIONCOLOR.getRed(), Constants.SELECTIONCOLOR.getGreen(), 
        //		Constants.SELECTIONCOLOR.getBlue(), 178);
        //activeTierAlpha7 = new Color(Constants.ACTIVETIERCOLOR.getRed(), Constants.ACTIVETIERCOLOR.getGreen(),
        //		Constants.ACTIVETIERCOLOR.getBlue(), 200);
        msPerPixel = 10;
        resolution = PIXELS_FOR_SECOND;
        crossHairTime = 0L;
        crossHairPos = 0;
        intervalBeginTime = 0L;
        intervalEndTime = 0L;
        verticalScrollOffset = 0;

        selectionBeginTime = 0L;
        selectionEndTime = 0L;
        selectionBeginPos = 0;
        selectionEndPos = 0;
        dragStartTime = 0;
        timeScaleConnected = true;

        imageWidth = 0;
        imageHeight = 0;
        alpha04 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
        alpha07 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);

        //pixelsForTierHeight = font.getSize() * 3; //hardcoded for now
        //pixelsForTierHeight = font.getSize() + 24;
        pixelsForTierHeight = calcTierHeight();
        pixelsForTierHeightMargin = 2; // hardcoded for now

        scrollBar = new JScrollBar(JScrollBar.VERTICAL, 0, 50, 0, 200);
        scrollBar.setUnitIncrement(pixelsForTierHeight / 2);
        scrollBar.setBlockIncrement(pixelsForTierHeight);
        scrollBar.addAdjustmentListener(this);

        hScrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 50, 0, 400);
        hScrollBar.setUnitIncrement(10);
        hScrollBar.setBlockIncrement(40);
        hScrollBar.addAdjustmentListener(this);

        setLayout(null);
        //setLayout(new TLLayoutManager());
        add(scrollBar);
        add(hScrollBar);
        // the range is from 10% to 1000%, in order to have the button more in the center at 100% use a different max value
        zoomSlider = new JSlider(ZOOMLEVELS[0], 300, 100);
        zoomSlider.putClientProperty("JComponent.sizeVariant", "small");// On MacOS regular, small, mini
        zoomSliderPanel = new JPanel(null);
        zoomSliderPanel.add(zoomSlider);
        zoomSlider.addChangeListener(this);
        zoomSlider.setToolTipText(String.valueOf(zoomSlider.getValue()));
        add(zoomSliderPanel);
        corner = new JPanel();
        add(corner);

        editBox = new InlineEditBox(true);
        editBox.setFont(font);
        editBox.setVisible(false);
        add(editBox);
        editBox.addInlineEditBoxListener(this);
        editBox.setMediaDisplayerHost(this);
    }   
    
    /**
     * Called when the viewer will become invisible e.g when switching to an other working mode.
     */
    @Override
	public void isClosing(){
    	if(editBox != null && editBox.isVisible()){
    		Boolean boolPref = Preferences.getBool("InlineEdit.DeselectCommits", null);
    		if (boolPref != null && !boolPref) {
    			editBox.cancelEdit();   			
    		} else {
    			editBox.commitEdit();
    		}
        }    	
    	if (multiTierControlPanel != null) {
    		multiTierControlPanel.isClosing();
    	}
    }
    
    /**
     * Retrieves the default, platform specific width of a scrollbar.
     *
     * @return the default width, or 20 when not found
     */
    private int getDefaultBarWidth() {
        int width = 20;

        if (UIManager.getDefaults().get("ScrollBar.width") != null) {
            width = ((Integer) (UIManager.getDefaults().get("ScrollBar.width"))).intValue();
        }

        return width;
    }

    /**
     * Initialise tiers and tags.
     */
    private void initTiers() {
        allTiers = new ArrayList<Tier2D>(20);
        visibleTiers = new ArrayList<Tier2D>(allTiers.size());

        if (transcription == null) {
        	tierYPositions = new int[0];
            return;
        }

        extractTiers();
        tierYPositions = new int[allTiers.size()];

        //allTiers is filled, set all tiers visible
        // not neccessary anymore

        /*
           Iterator it = allTiers.iterator();
           while(it.hasNext()) {
               visibleTiers.add(it.next());
           }
         */
    }

    /**
     * Extract all Tiers from the Transcription. Store the information in
     * Tier2D and Tag2D objects.
     */
    private void extractTiers() {
        Tier2D tier2d;

        Iterator<TierImpl> tierIter = transcription.getTiers().iterator();

        while (tierIter.hasNext()) {
            TierImpl tier = tierIter.next();
            tier2d = createTier2D(tier);

            allTiers.add(tier2d);
        }
    }

    /**
     * Set the colour of a Tag2D based on the Controlled Vocabulary Entry
     * that is associated with the annotation (if any).
     */
	private void setColor(Tag2D tag2d, Annotation a, ControlledVocabulary cv) {
		String id = a.getCVEntryId();
		if (id != null) {
    		CVEntry cve = cv.getEntrybyId(id);
    		if (cve != null) {
    			tag2d.setColor(cve.getPrefColor());
    			return;
    		}
    	}
		// Remove colour and set to default.
		tag2d.setColor(null);
    }

    private Tier2D createTier2D(TierImpl tier) {
        Tier2D tier2d = new Tier2D(tier);
        Tag2D tag2d;
        int xPos;
        int tagWidth;
        TierImpl markTier = null;
        ControlledVocabulary cv = transcription.getControlledVocabulary(
        		tier.getLinguisticType().getControlledVocabularyName());
        if (cv == null) {
        	// if this tier has no Controlled Vocabulary associated with it, check whether
        	// there is a Symb. Association dependent tier that does have a CV and use the
        	// colors of these entries for the parent annotations on this tier
        	markTier = TierAssociation.findMarkerTierFor(transcription, tier);
        	if (markTier != null) {
        		cv = transcription.getControlledVocabulary(
        				markTier.getLinguisticType().getControlledVocabularyName());
        	}
        }

        List<Annotation> ch;

        for (Annotation a : tier.getAnnotations()) {
            //System.out.println("Annotation: " + a);
            tag2d = new Tag2D(a);
            xPos = timeToPixels(a.getBeginTimeBoundary());
            tag2d.setX(xPos);
            tagWidth = timeToPixels(a.getEndTimeBoundary()) - xPos;
            tag2d.setWidth(tagWidth);
            tag2d.setTruncatedValue(truncateString(a.getValue(), tagWidth,
                    metrics));
            if (cv != null) {
            	if (markTier == null) {
            		setColor(tag2d, a, cv);
            	} else {
            		ch = ((AbstractAnnotation)a).getChildrenOnTier(markTier);
            		if (ch.size() >= 1) {// should be 1 max
            			Annotation ma = ch.get(0);            			
                		setColor(tag2d, ma, cv);
            		}           		
            	}
            }
            
            tier2d.addTag(tag2d);

            if (a == getActiveAnnotation()) {
                cursorTag2D = tag2d;
            }
        }

        return tier2d;
    }

    /**
     * When the resolution or zoom level of the viewer has been changed the
     * Tag2D x position, width and truncated string value needs to be
     * recalculated.
     */
    private void recalculateTagSizes() {
        Tier2D tier2d;
        Tag2D tag2d;
        int xPos;
        int tagWidth;
        Font tierFont;
        FontMetrics tierMetrics;
        Iterator<Tier2D> tierIt = allTiers.iterator();

        while (tierIt.hasNext()) {
            tier2d = tierIt.next();
            tierFont = getFontForTier(tier2d.getTier());
            tierMetrics = getFontMetrics(tierFont);
            Iterator<Tag2D> tagIt = tier2d.getTags();

            while (tagIt.hasNext()) {
                tag2d = tagIt.next();
                xPos = timeToPixels(tag2d.getBeginTime());
                tag2d.setX(xPos);
                tagWidth = timeToPixels(tag2d.getEndTime()) - xPos;
                tag2d.setWidth(tagWidth);
                tag2d.setTruncatedValue(truncateString(tag2d.getValue(),
                        tagWidth, tierMetrics));
            }
        }
    }

    /**
     * Re-processes the annotations of a tier.<br>
     * Necessary after removal of an unknown number of annotations.
     *
     * @param tier2d the Tier2D
     */
    private void reextractTagsForTier(Tier2D tier2d) {
        if ((transcription == null) || (tier2d == null)) {
            return;
        }

        //int index = transcription.getTiers(userIdentity).indexOf(tier2d.getTier());
        //TierImpl tier = null;
        //if (index > -1) {
        //    tier = (TierImpl) transcription.getTiers(userIdentity).get(index);
        //}
        TierImpl tier = tier2d.getTier();

        if (tier == null) {
            return;
        }
        Font prefFont = getFontForTier(tier);
        FontMetrics tierMetrics = getFontMetrics(prefFont);
        tier2d.getTagsList().clear();

        TierImpl markTier = null;
        ControlledVocabulary cv = transcription.getControlledVocabulary(
        		tier.getLinguisticType().getControlledVocabularyName());
        if (cv == null) {
        	markTier = TierAssociation.findMarkerTierFor(transcription, tier);
        	if (markTier != null) {
        		cv = transcription.getControlledVocabulary(
        				markTier.getLinguisticType().getControlledVocabularyName());
        	}
        }
        
        Tag2D tag2d;
        int xPos;
        int tagWidth;
        List<Annotation> ch ;

        for (Annotation a : tier.getAnnotations()) {

            //System.out.println("Annotation: " + a);
            tag2d = new Tag2D(a);
            xPos = timeToPixels(a.getBeginTimeBoundary());
            tag2d.setX(xPos);
            tagWidth = timeToPixels(a.getEndTimeBoundary()) - xPos;
            tag2d.setWidth(tagWidth);
            tag2d.setTruncatedValue(truncateString(a.getValue(), tagWidth,
            		tierMetrics));
            if (cv != null) {
            	if (markTier == null) {
            		setColor(tag2d, a, cv);
            	} else {
            		ch = ((AbstractAnnotation)a).getChildrenOnTier(markTier);
            		if (ch.size() >= 1) {// should be 1 max
            			Annotation ma = ch.get(0);
                		setColor(tag2d, ma, cv);
            		}           		
            	}	
            }
            
            tier2d.addTag(tag2d);

            if (a == getActiveAnnotation()) {
                cursorTag2D = tag2d;
            }
        }
    }

    /**
     * Create a truncated String of a tag's value to display in the viewer.
     *
     * @param string the tag's value
     * @param width the available width for the String
     * @param fMetrics the font metrics
     *
     * @return the truncated String
     */
    private String truncateString(String string, int width, FontMetrics fMetrics) {
        String line = string.replace('\n', ' ');

        if (fMetrics != null) {
            int stringWidth = fMetrics.stringWidth(line);
            
            width -= 4;

            if (stringWidth > width) { // truncate            	            
            	// This method could work too, but it appends "..." if the string is clipped.
            	// And it calculates the string width by summing the character widths, which
            	// is documented as not necessarily being the same. And it is an internal
            	// and non-public API.
            	//return sun.swing.SwingUtilities2.clipStringIfNecessary(this, fMetrics, string, width);
            	
            	int lwb = 0;
            	int upb = line.length(); 
            	String s = line;

            	// Perform an initial estimation based on how much too wide the string was.
            	// Take a wide margin because character width may be very uneven.
            	double fraction = (double)width / stringWidth;
            	lwb = Math.max(lwb, (int)(0.8 * fraction * line.length() - 1));
            	upb = Math.min(     (int)(1.2 * fraction * line.length() + 1),  upb);
            	
            	// Then do a binary search in between those margins for the largest
            	// stringWidth that is <= width.
            	while (lwb < upb) {
            		int mid = (upb + lwb + 1) / 2;
            		
            		s = line.substring(0, mid);
            		stringWidth = fMetrics.stringWidth(s);
            		if (stringWidth > width) {
            			upb = mid - 1;
            		} else {
            			lwb = mid;
            			if (stringWidth == width) {
            				break;
            			}
            		}
            	}
           		line = line.substring(0, lwb);
            }
        }

        return line;
    }

    /**
     * Paint to a buffer.<br>
     * First paint the top ruler, next the current selection and finally paint
     * the tags of the visible tiers.
     */
    private void paintBuffer() {
    	if (!useBufferedImage /* && !playerIsPlaying() */) {
    		repaint();
    		return;
    	}
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }
        //long bpBegin = System.currentTimeMillis();
        if (((getWidth() - defBarWidth) != imageWidth) ||
                (imageHeight != ((visibleTiers.size() * pixelsForTierHeight) +
                rulerHeight))) {
            imageWidth = getWidth() - defBarWidth;
            imageHeight = (visibleTiers.size() * pixelsForTierHeight) +
                rulerHeight;

            if ((imageWidth <= 0) || (imageHeight <= 0)) {
                return;
            }

            intervalEndTime = intervalBeginTime +
                (int) (imageWidth * msPerPixel);

            if (timeScaleConnected) {
                setGlobalTimeScaleIntervalEndTime(intervalEndTime);
            }

        }

        if ((bi == null) || (bi.getWidth() < imageWidth) ||
                (bi.getHeight() < imageHeight)) {
            bi = new BufferedImage(imageWidth, imageHeight,
                    BufferedImage.TYPE_INT_RGB);
            big2d = bi.createGraphics();
        }
        
        if (bi.getHeight() > imageHeight) {
        	imageHeight = bi.getHeight();
        }
        if (SystemReporting.antiAliasedText) {
	        big2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        //big2d.setFont(font);
        big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        big2d.fillRect(0, 0, imageWidth, bi.getHeight());

        // mark the area beyond the media time
        long dur = getMediaDuration();
        int xx = xAt(dur);

        if (intervalEndTime > dur) {
        	if (xx >= 0 && xx <= imageWidth) {
        		big2d.setColor(Color.LIGHT_GRAY);         
        		big2d.drawLine(xx, 0, xx, bi.getHeight());
        	}
        	
        	if (xx <= imageWidth) {
        		xx = xx < 0 ? 0 : xx;
                if (!SystemReporting.isMacOS()) {
                	// this slows down Mac performance enormously, don't know why
                	big2d.setColor(UIManager.getColor("Panel.background"));
                } else {
                	//big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
                }
        		big2d.fillRect((xx < 0 ? 0 : xx + 1), 0, imageWidth - xx, bi.getHeight());
        	}
        }

        big2d.translate(-((int)(intervalBeginTime / msPerPixel)), 0);
        /*paint time ruler */
//        if (timeRulerVisible) {
//	        big2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
//	        //big2d.translate(-(intervalBeginTime / msPerPixel), 0.0);	
//	        ruler.paint(big2d, intervalBeginTime, imageWidth, msPerPixel,
//	            SwingConstants.TOP);
//	        big2d.setFont(font);
//        }
        ///end ruler
        // paint a slightly dif. background color for every other tier
        int y = rulerHeight;
        int ax = timeToPixels(intervalBeginTime); // == -translation.x

        big2d.setColor(Constants.LIGHTBACKGROUNDCOLOR);
        if (intervalBeginTime < dur) {
        	int numStripes = Math.max(visibleTiers.size(), imageHeight / pixelsForTierHeight);
	        for (int i = 0; i < numStripes; i++) {
	            if (i % 2 != 0) {
	                big2d.fillRect(ax, y + i * pixelsForTierHeight, xx, pixelsForTierHeight);
	            }
	        }
        }
        //paint selection
        if (selectionBeginPos != selectionEndPos) {
            int beginPos = timeToPixels(getSelectionBeginTime());
            int endPos = timeToPixels(getSelectionEndTime());
            big2d.setColor(Constants.SELECTIONCOLOR);
            big2d.setComposite(alpha04);
//            big2d.setColor(selectionColorAlpha4);
            big2d.fillRect(beginPos, 0, (endPos - beginPos), rulerHeight);
            big2d.setComposite(AlphaComposite.Src);
            big2d.fillRect(beginPos, rulerHeight, (endPos - beginPos),
                imageHeight - rulerHeight);
        }
        
        /* Mod by Mark */
        /* Overlay the tier highlight colors */
        if(highlightColors != null) {
	        big2d.setComposite(alpha04);
	        for(int i = 0; i < visibleTiers.size(); i++) {
	        	Color highlightColor = highlightColors.get(visibleTiers.get(i).getName());
	        	if(highlightColor != null) {
	        		big2d.setColor(highlightColor);
	        		big2d.fillRect(ax, y + (i * pixelsForTierHeight),
	                        imageWidth - (imageWidth - xx), pixelsForTierHeight);
	        	}
	        }
	        big2d.setComposite(AlphaComposite.Src);
        }
		/* --- END --- */
        // paint tags
        //int x;
        //int w;
        int h = pixelsForTierHeight - (2 * pixelsForTierHeightMargin);

        Tier2D tier2d;
        Tag2D tag2d;
        Font tf = null;

        synchronized (tierLock) {
            Iterator<Tier2D> visIt = visibleTiers.iterator();
            
            	while (visIt.hasNext()) {
                tier2d = visIt.next();
                tf = getFontForTier(tier2d.getTier());
                big2d.setFont(tf);
                
                if (tier2d.isActive()) {    
                    big2d.setColor(Constants.ACTIVETIERCOLOR);
                    big2d.setComposite(alpha07);
//                	big2d.setColor(activeTierAlpha7);
                    big2d.fillRect(ax, y, imageWidth, pixelsForTierHeight);
                    big2d.setComposite(AlphaComposite.Src);
                }

                Iterator<Tag2D> tagIt = tier2d.getTags();

                while (tagIt.hasNext()) {
                    tag2d = tagIt.next();

                    if (tag2d.getEndTime() < intervalBeginTime) {
                        continue; //don't paint
                    } else if (tag2d.getBeginTime() > intervalEndTime) {
                        break; // stop looping this tier
                    }

                    //paint tag at this position
                    paintTag(big2d, tag2d, tag2d.getX(),
                        y + pixelsForTierHeightMargin, tag2d.getWidth(), h);
                }

                y += pixelsForTierHeight;
            }
        }
        // end paint tags
        big2d.setTransform(new AffineTransform()); //reset transform
        big2d.setFont(font);
        /*paint time ruler */       
        if (timeRulerVisible) {
        	big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        	big2d.fillRect(0, 0, imageWidth, verticalScrollOffset + rulerHeight);
	        big2d.setColor(Constants.SELECTIONCOLOR);
	        big2d.drawLine(0, verticalScrollOffset + rulerHeight, imageWidth, verticalScrollOffset + rulerHeight);
	        big2d.translate(-((int)(intervalBeginTime / msPerPixel)), verticalScrollOffset);
	        //paint selection
	        if (selectionBeginPos != selectionEndPos) {
	            int beginPos = timeToPixels(getSelectionBeginTime());
	            int endPos = timeToPixels(getSelectionEndTime());
	            big2d.setColor(Constants.SELECTIONCOLOR);
	            big2d.setComposite(alpha04);
//	            big2d.setColor(selectionColorAlpha4);
	            big2d.fillRect(beginPos, 0, (endPos - beginPos), rulerHeight);
	            big2d.setComposite(AlphaComposite.Src);
	        }
	        big2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
	        ruler.paint(big2d, intervalBeginTime, imageWidth, msPerPixel,
	            SwingConstants.TOP);
	        //big2d.setFont(font);
	        big2d.setTransform(new AffineTransform()); //reset transform
        }
        ///end ruler
        
        // draw indications of comments
        paintCommentIndicators(big2d, true);
        //big2d.dispose(); // does not work properly in jdk 1.4
        //System.out.println("TL paint: " + (System.currentTimeMillis() - bpBegin));
        repaint();
        //??
//        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Check if the comment is to be visible.
     * <p>
     * If the comment isn't associated with any particular tier,
     * it is considered to be always visible.
     * <p>
     * Otherwise, it is visible if the tier associated with the comment is visible.
     * @param comment
     */
    private boolean commentIsVisible(CommentEnvelope comment) {
   		String tierName = comment.getTierName();
   		if (tierName != null && !tierName.isEmpty()) {
   			if (visibleTiers.isEmpty()) { // short circuit
   				return false;
   			}
   			if (visibleTiers.size() < allTiers.size()) {
   				for (Tier2D t2d : visibleTiers) {
   					if (t2d.getName().equals(tierName)) {
   						return true;
   					}
   				}
   	   			return false;
   			}   
   		}
   		return true;
    }

    /**
     * Paint some indication of the presence, location and time period of comments.
     * Called both from the buffered and unbuffered painting routines.
     * <p>
     * No "Comment2D"-like objects are constructed, the painting is simply
     * recalculated every time. There isn't really any reusable state to cache.
     * 
     * @param gr2d
     * @param buffered
     */
    private void paintCommentIndicators(Graphics2D gr2d, boolean buffered) {
    	if (!timeRulerVisible) {
    		return;
    	}
    	ViewerManager2 vm = getViewerManager();
    	if (vm == null) {
			return;
		}
    	CommentViewer cv = vm.getCommentViewer();
    	if (cv == null) {
			return;
		}
       	CommentManager cm = cv.getCommentManager();

       	int rulerY = buffered ? verticalScrollOffset
       			              : 0;
       	int rulerH = timeRulerVisible ? rulerHeight : 0;       	

       	Graphics2D ruler = null;
       	if (timeRulerVisible) {
       		ruler = (Graphics2D)gr2d.create();
	       	ruler.setColor(COMMENT_INDICATOR_INTERIOR_COLOR);
	       	ruler.setComposite(COMMENT_INDICATOR_ALPHA);
       	}

       	long lastEndTime = 0; // right-most side of overlapping rectangles
       	int vertOffset = 0;
       	final int barHeight = 5;
       	final int barStagger = 2;
       	boolean someMoreToTheLeft = false;
       	boolean someMoreToTheRight = false;
       	
       	for (CommentEnvelope comment : cm.getList()) {
       		// Is this comment inside the visible interval?
       		long commentEndTime = comment.getEndTime();
       		if (commentEndTime < intervalBeginTime) {
       			// do "expensive" visibility checking only if needed
       			if (timeRulerVisible) {
       				someMoreToTheLeft = someMoreToTheLeft || commentIsVisible(comment);
       			}
       			continue;	// skip this comment; it is too far to the left
       		}
       		
       		long commentStartTime = comment.getStartTime();
       		if (commentStartTime > intervalEndTime) {
       			if (timeRulerVisible) {
       				someMoreToTheRight = someMoreToTheRight || commentIsVisible(comment);
       			}
       			break;	// finished with painting: this is too far to the right
       		}
       		
       		// If the comment is not visible because of its tier, don't paint it.
			if (!commentIsVisible(comment)) {
				continue;
			}
       		
       		// See if we can start our indicator bars at the top again
       		if (commentStartTime >= lastEndTime) {
       			vertOffset = 0;
       		}
       		// Update how far they reach to the right
       		lastEndTime = Math.max(lastEndTime, commentEndTime);
       		
       		int leftpixel = xAt(commentStartTime);
       		int width = xAt(commentEndTime) - leftpixel;
       		width = Math.max(10, width); // make them at least some minimum wide

       		// Comment indicator bar, transparently covering the ruler.
       		// NOTE: for fillRect(), the width/height is exclusive and for drawRect() it is inclusive.
       		// So with the same width/height the former makes a smaller rectangle than the latter.
       		if (timeRulerVisible) {
       			ruler.setColor(COMMENT_INDICATOR_INTERIOR_COLOR);
       			ruler.fillRect(leftpixel+1, rulerY + vertOffset+1, width-2, barHeight-2);
       			ruler.setColor(COMMENT_INDICATOR_EDGE_COLOR);
       			ruler.drawRect(leftpixel, rulerY + vertOffset, width-1, barHeight-1);

       			// The next bar will be painted lower, if there is still room there.
       			vertOffset = Math.min(vertOffset + barStagger, rulerH - barHeight);
       		}
       	}
       	
       	if (someMoreToTheLeft) {
       		// Draw a left-pointing triangle
   			ruler.setColor(COMMENT_INDICATOR_EDGE_COLOR);
   			int left = 0;
   			int w = rulerH * 2 / 3; //    left                top     bottom
   			ruler.fillPolygon(new int[] {              left,  left+w,        left+w }, // xs
   					          new int[] { rulerY+rulerH / 2,  rulerY, rulerY+rulerH }, // ys
   					          3);
       	}
       	if (someMoreToTheRight) {
       		// Draw a right-pointing triangle
   			ruler.setColor(COMMENT_INDICATOR_EDGE_COLOR);
   			int right = xAt(intervalEndTime);
   			int w = rulerH * 2 / 3; //    right              top      bottom
   			ruler.fillPolygon(new int[] {             right, right-w,       right-w }, // xs
				              new int[] { rulerY+rulerH / 2,  rulerY, rulerY+rulerH }, // ys
   					          3);
       	}
		if (timeRulerVisible) {
			ruler.dispose();
		}
	}

    /**
     * Build some text to be used in a tooltip. Use the time and up to 20 comments.
     * Also take only a limited prefix from each comment.
     * @param pp The location of the tooltip.
     */
    private String getCommentTip(Point pp) {
		long time = timeAt(pp.x);
    	StringBuilder text = new StringBuilder();
    	text.append("<html>");
       	text.append(TimeFormatter.toString(time));	 // assume it needs no escaping

       	ViewerManager2 vm = getViewerManager();
       	if (vm != null) {
       		CommentViewer cv = vm.getCommentViewer();
       		if (cv != null) {
       			CommentManager cm = cv.getCommentManager();

       			int lines = 0;
       			for (CommentEnvelope comment : cm.getList()) {
       				long commentEndTime = comment.getEndTime();
       				if (commentEndTime < time) {
						continue;
					}

       				long commentStartTime = comment.getStartTime();
       				if (commentStartTime > time) {
						break;
					}

       				String message = comment.getMessage();
       				text.append("<br/>");
       				appendEscaped(text, message, 132);

       				// Don't grow the tooltip without bounds.
       				if (lines > 20) {
       					break;
       				}
       			}
       		}
    	}

    	text.append("</html>");
    	return text.toString();
    }
    
    private void appendEscaped(StringBuilder dest, String src, int maxLength) {
    	maxLength = Math.min(maxLength, src.length());
    	
    	for (int i = 0; i < maxLength; i++) {
    		char c = src.charAt(i);
    		
    		switch (c) {
    		case '<':
    			dest.append("&lt;");
    			break;
    		case '>':
    			dest.append("&gt;");
    			break;
    		case '&':
    			dest.append("&amp;");
    			break;
    		default:
    			dest.append(c);
    			break;
    		}
    	}
    }
    
	/**
     * Override <code>JComponent</code>'s paintComponent to paint:<br>
     * - a BufferedImage with a ruler, the selection and the tags<br>
     * - the current selection Tag<br>
     * - the "mouse over" Tag<br>
     * - the time ruler when timeRulerVisible is true<br>
     * - the cursor / crosshair - empty slots - the drag edit tag
     *
     * @param g the graphics object
     */
    @Override
	public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        if (SystemReporting.antiAliasedText) {
	        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        
        //synchronized (getTreeLock()) {
	        	
	        if (!useBufferedImage /*&& !playerIsPlaying()*/) {
	        	paintUnbuffered(g2d);
	        	return;
	        }
	        
	        int h = getHeight();
	        int ww = getWidth();
	        // scrolling related fill
	        g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
	        g2d.fillRect(0, 0, imageWidth, h);
	        
	        // mark the area beyond the media time
	        long dur = getMediaDuration();
	        int xx = xAt(dur);

	        if (intervalEndTime > dur) {
	        	if (xx >= 0 && xx <= ww) {
	        		g2d.setColor(Color.LIGHT_GRAY);         
	        		g2d.drawLine(xx, 0, xx, h);
	        	}
	        	
	        	if (xx <= ww) {
	        		xx = xx < 0 ? 0 : xx;
	                if (!SystemReporting.isMacOS()) {
	                	// this slows down Mac performance enormously, don't know why
	                	g2d.setColor(UIManager.getColor("Panel.background"));
	                } else {
	                	//big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
	                }
	        		g2d.fillRect((xx < 0 ? 0 : xx + 1), 0, ww - xx, h);
	        	}
	        }
	        
	        if (bi != null) {
	            g2d.translate(0, -verticalScrollOffset);
	            //paint selection in the part not occupied by the image
	            if (selectionBeginPos != selectionEndPos && bi.getHeight() < h) {
	                g2d.setColor(Constants.SELECTIONCOLOR);
	                g2d.fillRect(selectionBeginPos, 0, 
	                		(selectionEndPos - selectionBeginPos),
	                    h);
	            }
	            g2d.drawImage(bi, 0, 0, this);
	            g2d.translate(0, verticalScrollOffset);
	        }
	
	        g2d.setFont(font);
	
	        /* don't paint the hoverTag for now
	           if (hoverTag2D != null) {
	               //System.out.println("tag: " + hoverTag2D);
	               int x = xAt(hoverTag2D.getBeginTime());
	               int w = xAt(hoverTag2D.getEndTime()) - x;
	               int y = (rulerHeight + hoverTierIndex * pixelsForTierHeight + pixelsForTierHeightMargin) - verticalScrollOffset;
	               int he = pixelsForTierHeight - 2 * pixelsForTierHeightMargin;
	               paintHoverTag2D(g2d, hoverTag2D, x, y, w, he);
	           }
	         */
	        if ((cursorTag2D != null) &&
	                visibleTiers.contains(cursorTag2D.getTier2D())) {
	            //int x = xAt(cursorTag2D.getBeginTime());
	            //int w = xAt(cursorTag2D.getEndTime()) - x;
	            //int x = (int) ((cursorTag2D.getBeginTime() / msPerPixel) -
	            //    (intervalBeginTime / msPerPixel));
	        	int x =  cursorTag2D.getX() -
	        			(int) (intervalBeginTime / msPerPixel);
	            int w = cursorTag2D.getWidth();
	            int y = (rulerHeight + (cursorTierIndex * pixelsForTierHeight) +
	                pixelsForTierHeightMargin) - verticalScrollOffset;
	            int he = pixelsForTierHeight - (2 * pixelsForTierHeightMargin);
	            paintCursorTag2D(g2d, cursorTag2D, x, y, w, he);
	        }
	
	        //paint empty slots
	        if (showEmptySlots) {
	            for (int i = 0; i < visibleTiers.size(); i++) {
	                TierImpl ti = visibleTiers.get(i).getTier();
	
	                if (ti.getParentTier() == null) {
	                    continue;
	                } else {
	                    if (!ti.isTimeAlignable()) {
	                        int y = (rulerHeight + (i * pixelsForTierHeight) +
	                            pixelsForTierHeightMargin) - verticalScrollOffset;
	                        int he = pixelsForTierHeight -
	                            (2 * pixelsForTierHeightMargin);
	                        paintEmptySlots(g2d, ti, y, he);
	                    }
	                }
	            }
	        }
	
	        // paint the dragEdit annotation
	        if (dragEditTag2D != null) {
	            //long newTime = pixelToTime(dragEditTag2D.getX());
	            //int x = (int) ((dragEditTag2D.getBeginTime() / msPerPixel) -
	            //	(intervalBeginTime / msPerPixel));
	            int x = (int) (dragEditTag2D.getX() -
	                (intervalBeginTime / msPerPixel));
	
	            //int w = (int) ((dragEditTag2D.getEndTime() -
	            //dragEditTag2D.getBeginTime()) / msPerPixel);
	            int w = dragEditTag2D.getWidth();
	            int y = (rulerHeight + (cursorTierIndex * pixelsForTierHeight) +
	                
	                /*(getTierIndexForAnnotation(dragEditTag2D.getAnnotation()) * pixelsForTierHeight) +*/
	                pixelsForTierHeightMargin) - verticalScrollOffset + dragEditTag2D.getY();
	            int he = pixelsForTierHeight - (2 * pixelsForTierHeightMargin);
	            paintDragEditTag2D(g2d, x, y, w, he);
	        }
	
	        paintSelectedAnnotations(g2d);
	        /*
	        if (timeRulerVisible && (bi != null)) {
	            g2d.setClip(0, 0, imageWidth, rulerHeight);
	            g2d.drawImage(bi, 0, 0, this);
	            g2d.setClip(null);
	            g2d.setColor(Constants.SELECTIONCOLOR);
	            g2d.drawLine(0, rulerHeight, imageWidth, rulerHeight);
	        }
	        */
			
	        if ((crossHairPos >= 0) && (crossHairPos <= imageWidth)) {
	            // prevents drawing outside the component on Mac
	            g2d.setColor(Constants.CROSSHAIRCOLOR);
	            g2d.drawLine(crossHairPos, 0, crossHairPos, h);
	        }
        //}// treelock
        //Toolkit.getDefaultToolkit().sync();
    }

    private void paintUnbuffered(Graphics2D g2d) {
    	// from paintBuffer
    	int h = getHeight();
    	int w = getWidth();
        // scrolling related fill
        g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        g2d.fillRect(0, 0, w, h);
        // selection
        /*
        if (selectionBeginPos != selectionEndPos) {
            g2d.setColor(Constants.SELECTIONCOLOR);
            g2d.fillRect(selectionBeginPos, 0, 
            		(selectionEndPos - selectionBeginPos),
                h);
        }
        */
        // paint a slightly dif. background color for every other tier
        int y = rulerHeight - verticalScrollOffset;
        
        g2d.setColor(Constants.LIGHTBACKGROUNDCOLOR);
        for (int i = 0; i < visibleTiers.size(); i++) {
            if (i % 2 != 0) {
                g2d.fillRect(0, y + i * pixelsForTierHeight, w, pixelsForTierHeight);
            }
        }

        // mark the area beyond the media time
        /*
        if (intervalEndTime > getMediaDuration()) {
        	g2d.setColor(Color.LIGHT_GRAY);
            int xx = xAt(getMediaDuration());
            g2d.drawLine(xx, 0, xx, h);
            if (!SystemReporting.isMacOS()) {
            	g2d.setColor(UIManager.getColor("Panel.background"));
            	g2d.fillRect(xx + 1, 0, w - xx, h);
            }
        }
        */
        // mark the area beyond the media time
        long dur = getMediaDuration();
        int xx = xAt(dur);

        if (intervalEndTime > dur) {
        	if (xx >= 0 && xx <= w) {
        		g2d.setColor(Color.LIGHT_GRAY);         
        		g2d.drawLine(xx, 0, xx, h);
        	}
        	
        	if (xx <= w) {
        		xx = xx < 0 ? 0 : xx;
                if (!SystemReporting.isMacOS()) {
                	// this slows down Mac performance enormously, don't know why
                	g2d.setColor(UIManager.getColor("Panel.background"));
                } else {
                	//big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
                }
        		g2d.fillRect((xx < 0 ? 0 : xx + 1), 0, w - xx, h);
        	}
        }
        //paint selection
        if (selectionBeginPos != selectionEndPos) {
            g2d.setColor(Constants.SELECTIONCOLOR);
            g2d.setComposite(alpha04);
//            g2d.setColor(selectionColorAlpha4);
            g2d.fillRect(selectionBeginPos, 0, (selectionEndPos - selectionBeginPos), rulerHeight);
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.fillRect(selectionBeginPos, rulerHeight, (selectionEndPos - selectionBeginPos),
                w - rulerHeight);
        }
        /* Mod by Mark */
        /* Overlay the tier highlight colors */
        if(highlightColors != null) {
	        g2d.setComposite(alpha04);
	        for(int i = 0; i < visibleTiers.size(); i++) {
	        	Color highlightColor = highlightColors.get(visibleTiers.get(i).getName());
	        	if(highlightColor != null) {
	        		g2d.setColor(highlightColor);
	        		g2d.fillRect(0, y + (i * pixelsForTierHeight), w, pixelsForTierHeight);
	        	}
	        }
	        g2d.setComposite(AlphaComposite.SrcOver);
        }
        /* --- END --- */
        // translate horizontally
        int ax = timeToPixels(intervalBeginTime);
        g2d.translate(-ax, 0);
        int ht = pixelsForTierHeight - (2 * pixelsForTierHeightMargin);

        Tier2D tier2d;
        Tag2D tag2d;
        Font tf = null;

        synchronized (tierLock) {
        	Iterator<Tier2D> visIt = visibleTiers.iterator();

        	while (visIt.hasNext()) {
        		tier2d = visIt.next();
        		tf = getFontForTier(tier2d.getTier());
        		g2d.setFont(tf);               
        		if (tier2d.isActive()) {    
        			g2d.setColor(Constants.ACTIVETIERCOLOR);
        			g2d.setComposite(alpha07);
        			//                	big2d.setColor(activeTierAlpha7);
        			g2d.fillRect(ax, y, imageWidth, pixelsForTierHeight);
        			g2d.setComposite(AlphaComposite.SrcOver);
        		}

        		Iterator<Tag2D> tagIt = tier2d.getTags();

        		while (tagIt.hasNext()) {
        			tag2d = tagIt.next();

        			if (tag2d.getEndTime() < intervalBeginTime) {
        				continue; //don't paint
        			} else if (tag2d.getBeginTime() > intervalEndTime) {
        				break; // stop looping this tier
        			}

        			//x = timeToPixels(tag2d.getBeginTime());
        			//w = timeToPixels(tag2d.getEndTime()) - x;
        			//paint tag at this position
        			paintTag(g2d, tag2d, tag2d.getX(),
        					y + pixelsForTierHeightMargin, tag2d.getWidth(), ht);
        		}

        		y += pixelsForTierHeight;
        	}
        }

        // Before painting the time ruler
        //paintCommentIndicators(g2d);
        /*paint time ruler */
        if (timeRulerVisible) {
	        g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
	        g2d.fillRect(ax, 0, w, rulerHeight);
	        g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
	        ruler.paint(g2d, intervalBeginTime, w, msPerPixel,
	            SwingConstants.TOP);
        }
        
        g2d.setFont(font);
        // horizontally translate back
        g2d.translate(ax, 0);
        //paint selection over ruler
        if (selectionBeginPos != selectionEndPos && timeRulerVisible) {
            g2d.setColor(Constants.SELECTIONCOLOR);
            g2d.setComposite(alpha04);
//        	big2d.setColor(selectionColorAlpha4);
            g2d.fillRect(selectionBeginPos, 0, (selectionEndPos - selectionBeginPos), rulerHeight);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
        if (timeRulerVisible) {
	        g2d.setColor(Constants.SELECTIONCOLOR);
            g2d.drawLine(0, rulerHeight, w, rulerHeight);
        }
        // from paintComponent
        //g2d.setFont(font);
        if ((cursorTag2D != null) &&
                visibleTiers.contains(cursorTag2D.getTier2D())) {
        	int x =  cursorTag2D.getX() - (int) (intervalBeginTime / msPerPixel);
        	int ww = cursorTag2D.getWidth();
            int yy = (rulerHeight + (cursorTierIndex * pixelsForTierHeight) +
                pixelsForTierHeightMargin) - verticalScrollOffset;
            int he = pixelsForTierHeight - (2 * pixelsForTierHeightMargin);
            paintCursorTag2D(g2d, cursorTag2D, x, yy, ww, he);
        }

        //paint empty slots
        if (showEmptySlots) {
            for (int i = 0; i < visibleTiers.size(); i++) {
                TierImpl ti = visibleTiers.get(i).getTier();

                if (ti.getParentTier() == null) {
                    continue;
                } else {
                    if (!ti.isTimeAlignable()) {
                        int yy = (rulerHeight + (i * pixelsForTierHeight) +
                            pixelsForTierHeightMargin) - verticalScrollOffset;
                        int he = pixelsForTierHeight -
                            (2 * pixelsForTierHeightMargin);
                        paintEmptySlots(g2d, ti, yy, he);
                    }
                }
            }
        }

        // paint the dragEdit annotation
        if (dragEditTag2D != null) {
            //long newTime = pixelToTime(dragEditTag2D.getX());
            //int x = (int) ((dragEditTag2D.getBeginTime() / msPerPixel) -
            //	(intervalBeginTime / msPerPixel));
            int x = (int) (dragEditTag2D.getX() -
                (intervalBeginTime / msPerPixel));

            //int w = (int) ((dragEditTag2D.getEndTime() -
            //dragEditTag2D.getBeginTime()) / msPerPixel);
            int ww = dragEditTag2D.getWidth();
            int yy = (rulerHeight + (cursorTierIndex * pixelsForTierHeight) +
                
                /*(getTierIndexForAnnotation(dragEditTag2D.getAnnotation()) * pixelsForTierHeight) +*/
                pixelsForTierHeightMargin) - verticalScrollOffset + dragEditTag2D.getY();

            int he = pixelsForTierHeight - (2 * pixelsForTierHeightMargin);
            paintDragEditTag2D(g2d, x, yy, ww, he);
        }

        paintSelectedAnnotations(g2d);
        
        if ((crossHairPos >= 0) && (crossHairPos <= w)) {
            // prevents drawing outside the component on Mac
            g2d.setColor(Constants.CROSSHAIRCOLOR);
            g2d.drawLine(crossHairPos, 0, crossHairPos, h);
        }
        
        paintCommentIndicators(g2d, false);
    }
    
    /**
     * Paint the given Tag2D to the specified Graphics2D object using the
     * specified location and dimension.
     *
     * @param g2d the graphics object to paint to
     * @param tag2d the tag to paint
     * @param x the x postion of the tag
     * @param y the y position of the tag
     * @param width the width of the tag
     * @param height the height of the tag
     */
    private void paintTag(Graphics2D g2d, Tag2D tag2d, int x, int y, int width,
        int height) {
        // preferred background color
        // Normal, unreduced tier height uses the middle half strip in the available space
    	// for an unaligned annotation. Aligned annotations use the full height.
        // +---------------------------------
        // | |
        // | |_____________________|
        // | |                     | <- unaligned
        // | | <- aligned
        // +---------------------------------
        int topHeight = height / 4;
        int midHeight = height / 2;
		int botHeight = (height * 3) / 4;
		
		if (reducedTierHeight) {
			// Adapt for reduced tier height: have just rhDist pixels below the line.
			topHeight = height / 2 - rhDist;
			midHeight = height - rhDist;
			botHeight = height;
		} else {
	        topHeight = height / 4;
	        midHeight = height / 2;
			botHeight = (height * 3) / 4;
		}
        
		final boolean alignable = tag2d.getAnnotation() instanceof AlignableAnnotation;

		// Paint the background colour, if any.
		if (tag2d.getColor() != null) {
        	g2d.setColor(tag2d.getColor());
        	if (reducedTierHeight) {
        		// Fill the part above the line
        		g2d.fillRect(x, y, width, midHeight);
        	} else {
        		// Fill the part below the line. A bigger block for alignable annos.
        		int h = alignable ? midHeight : midHeight / 2;
        		
        		g2d.fillRect(x, y + midHeight, width, h);
        	}
        }

		if (alignable) {
            AlignableAnnotation a = (AlignableAnnotation) tag2d.getAnnotation();
            //skip check cursor
            int topB, botB, topE, botE;
            
            if (a.getBegin().isTimeAligned()) {
            	topB = 0;	// use full height for aligned times
            	botB = height;
            } else {
            	topB = topHeight;
            	botB = botHeight;
            }
            
            if (a.getEnd().isTimeAligned()) {
            	topE = 0;	// use full height for aligned times
            	botE = height;
            } else {
            	topE = topHeight;
            	botE = botHeight;
            }

            g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
            
            g2d.drawLine(x,         y + topB,       x,         y + botB);      // begin time
            g2d.drawLine(x,         y + midHeight,  x + width, y + midHeight); // ruler
            g2d.drawLine(x + width, y + topE,       x + width, y + botE);      // end time
        } else {
            //not alignable
            g2d.setColor(symAnnColor); // previously, this was a fixed color: Constants.SHAREDCOLOR1 

            g2d.drawLine(x,         y + topHeight, x,         y + botHeight); // begin time
            g2d.drawLine(x,         y + midHeight, x + width, y + midHeight); // ruler
            g2d.drawLine(x + width, y + topHeight, x + width, y + botHeight); // end time
        }

		// Draw small square to indicate that the text is truncated
        if (tag2d.isTruncated()) {
        	g2d.setColor(Constants.SHAREDCOLOR6);
        	g2d.fillRect(x + width - 2, y + midHeight - 2, 2, 2);
        }

        g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);

        int descent = g2d.getFontMetrics().getDescent();
        g2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
	            (float) (y + (midHeight - descent + 1)));
    }

    /**
     * Paint the mouseover highlight for a tag.
     *
     * @param g2d
     * @param tag2d
     * @param x
     * @param y
     * @param width
     * @param height
     */

    /*
       private void paintHoverTag2D(Graphics2D g2d, Tag2D tag2d, int x, int y,
           int width, int height) {
           g2d.setColor(Constants.SHAREDCOLOR3);
           g2d.drawRect(x, y, width, height);
           g2d.setColor(Constants.SHAREDCOLOR4);
           g2d.fillRect(x, y, width - 1, height - 1);
           g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
           g2d.drawString(tag2d.getTruncatedValue(), x + 4,
               (int) (y + ((height / 2) - 1)));
       }
     */

    /**
     * Paint the selected Tag.
     *
     * @param g2d
     * @param tag2d
     * @param x
     * @param y
     * @param width
     * @param height
     */
    private void paintCursorTag2D(Graphics2D g2d, Tag2D tag2d, int x, int y,
        int width, int height) {
        if (tag2d.getAnnotation() instanceof AlignableAnnotation) {
            AlignableAnnotation a = (AlignableAnnotation) tag2d.getAnnotation();
            TimeSlot b = a.getBegin();
            //TimeSlot e = a.getEnd();
            g2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);

            if (aaStrokeBold) {
                g2d.setStroke(stroke2);
            }

            int top = b.isTimeAligned() ? 0 : (height / 4);
            int bottom = b.isTimeAligned() ? height : ((height * 3) / 4);
            if (reducedTierHeight) {
            	top = (height / 2 - rhDist);
            	bottom = height;
            }
            g2d.drawLine(x, y + top, x, y + bottom);
            int mid = reducedTierHeight ? (height - rhDist) : height / 2;

            if (aaStrokeBold) {
                mid++;
            }

            //g2d.drawLine(x, y + top + 1, x + width, y + top + 1);
            g2d.drawLine(x, y + mid, x + width, y + mid);
            //top = e.isTimeAligned() ? 0 : (height / 4);
            //bottom = e.isTimeAligned() ? height : ((height * 3) / 4);
            g2d.drawLine(x + width, y + top, x + width, y + bottom);
            g2d.setStroke(stroke);
        } else {
            //not alignable
            g2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);

            if (aaStrokeBold) {
                g2d.setStroke(stroke2);
            }
            if (!reducedTierHeight) {
	            g2d.drawLine(x, y + (height / 4), x, y + ((height * 3) / 4));
	            g2d.drawLine(x, y + (height / 2), x + width, y + (height / 2));
	            g2d.drawLine(x + width, y + (height / 4), x + width,
	                y + ((height * 3) / 4));
            } else {
                g2d.drawLine(x, y + (height / 2 - rhDist), x, y + height);
                g2d.drawLine(x, y + (height - rhDist), x + width, y + (height - rhDist));
                g2d.drawLine(x + width, y + (height / 2 - rhDist), x + width,
                    y + height);
            }
            g2d.setStroke(stroke);
        }
    }

    /**
     * Paints the tag is edited by dragging its boundaries, or by dragging  the
     * whole tag.
     *
     * @param g2d
     * @param x
     * @param y
     * @param width
     * @param height
     */
    private void paintDragEditTag2D(Graphics2D g2d, int x, int y, int width,
        int height) {
        g2d.setColor(dragEditColor);

        if (aaStrokeBold) {
            g2d.setStroke(stroke2);
        }

        int top = 0;
        int bottom = height;
        g2d.drawLine(x, y + top, x, y + bottom);
        top = reducedTierHeight ? height / 2 - rhDist : height / 2;

        if (aaStrokeBold) {
            top++;
        }

        //g2d.drawLine(x, y + top + 1, x + width, y + top + 1);
        g2d.drawLine(x, y + top, x + width, y + top);

        g2d.drawLine(x + width, y, x + width, y + bottom);
        if (dragEditTag2D != null && dragEditTag2D.isOverTargetTier) {// shouldn't be null
        	g2d.drawRect(x, y, width, height);
        	g2d.setColor(dropHighlightColor);
        	g2d.fillRect(x, y, width, height);
        }
        g2d.setStroke(stroke);
    }

    /**
     * Paint empty slots on this tier.<br>
     * Iterate over the parent tags in the visible area and paint a tag when
     * it is not on the child.
     *
     * @param g2d the graphics context
     * @param ti the tier containing empty slots
     * @param y y coordinate for the tags
     * @param he height of the tags
     */
    private void paintEmptySlots(Graphics2D g2d, TierImpl ti, int y, int he) {
        try {
            TierImpl parent = ti.getParentTier();

            for (Annotation a : parent.getAnnotations()) {

                if (a.getEndTimeBoundary() < intervalBeginTime) {
                    continue;
                }

                if (a.getBeginTimeBoundary() > intervalEndTime) {
                    break;
                }

                if (a.getChildrenOnTier(ti).size() == 0) {
                    int x = (int) ((a.getBeginTimeBoundary() / msPerPixel) -
                        (intervalBeginTime / msPerPixel));
                    int wi = (int) ((a.getEndTimeBoundary() -
                        a.getBeginTimeBoundary()) / msPerPixel);
                    g2d.setColor(Constants.SHAREDCOLOR4);
                    g2d.fillRect(x, y, wi, he);
                    g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
                    g2d.drawRect(x, y, wi, he);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void paintSelectedAnnotations(Graphics2D g2d) {
    	if (selectedAnnotations.size() != 0) {
    		g2d.setColor(Constants.SHAREDCOLOR5);
    		int xOff = (int) (intervalBeginTime / msPerPixel);
    		for (Tag2D t2d : selectedAnnotations) {
    			if (visibleTiers.contains(t2d.getTier2D())) {
    				g2d.drawRect(t2d.getX() - xOff, 
    						rulerHeight + (getTierIndexForAnnotation(t2d.getAnnotation()) * pixelsForTierHeight) + 1 - verticalScrollOffset, 
    						t2d.getWidth(), pixelsForTierHeight - 2);
    			}
    		}
    	}
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
    	//return (int) ((t / msPerPixel) - (intervalBeginTime / msPerPixel));
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
        return intervalBeginTime + (int)(x * msPerPixel);
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
    private int timeToPixels(long theTime) {
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
    private long pixelToTime(int x) {
        return (long) (x * msPerPixel);
    }
    
    /**
     * Calculates the height for all tiers, based on the base font size.
     * 
     * @return the height in pixels
     */
    public int calcTierHeight() {
    	int th = (int)((font.getSize() * 2.5) + (36 / font.getSize()));
        if (reducedTierHeight) {
        	th = (th * 2) / 3;
        }
        return th;
    }

    /**
     * Implements updateTimeScale from TimeScaleBasedViewer to adjust the
     * TimeScale if needed and when in TimeScale connected mode.<br>
     * Checks the GlobalTimeScaleIntervalBeginTime and
     * GlobalTimeScaleMsPerPixel and adjusts the interval and resolution of
     * this viewer when they differ from the global values.<br>
     * For the time being assume that the viewer is notified only once when
     * the resolution or the interval begintime has changed.
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
     * Gets whether this viewer listens to time scale updates from other
     * viewers.
     *
     * @return true when connected to global time scale values, false otherwise
     */
    public boolean getTimeScaleConnected() {
        return timeScaleConnected;
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
     * Change the horizontal resolution or zoomlevel locally. The msPerPixel
     * denotes the number of milliseconds of which the sound samples should be
     * merged to one value. It corresponds to one pixel in image space (a
     * pixel is the smallest unit in image space).<br>
     * The position on the screen of crosshair cursor should change as little
     * as possible.<br>
     * This is calculated as follows:<br>
     * The absolute x coordinate in image space is the current media time
     * divided by the new msPerPixel.<br>
     * <pre>
     * |----------|----------|-------x--|-- <br>
     * |imagesize |                  | absolute x coordinate of media time<br>
     * |    1     |    2     |    3     |
     * </pre>
     * Calculate the number of screen images that fit within the absolute x
     * coordinate. The new position on the screen would then be the absolute x
     * coordinate minus the number of screen images multiplied by the image
     * width. The difference between the old x value and the new x value is
     * then used to calculate the new interval start time.<br>
     * The new start time = (number of screen images  image width -
     * difference)  msPerPixel.
     *
     * @param step the new horizontal zoomlevel
     */
    private void setLocalTimeScaleMsPerPixel(float step) {
        if (msPerPixel == step) {
            return;
        }

        if (step >= TimeScaleBasedViewer.MIN_MSPP) {
            msPerPixel = step;
        } else {
            msPerPixel = TimeScaleBasedViewer.MIN_MSPP;
        }

        resolution = (int) (1000f / msPerPixel);

        /*stop the player if necessary*/
        boolean playing = playerIsPlaying();

        if (playing) {
            stopPlayer();
        }

        long mediaTime = getMediaTime();
        int oldScreenPos = crossHairPos;
        int newMediaX = (int) (mediaTime / msPerPixel);
        int numScreens;

        if (imageWidth > 0) {
            numScreens = (int) (mediaTime / (imageWidth * msPerPixel));
        } else {
            numScreens = 0;
        }

        int newScreenPos = newMediaX - (numScreens * imageWidth);
        int diff = oldScreenPos - newScreenPos;

        //new values
        intervalBeginTime = (long) (((numScreens * imageWidth) - diff) * msPerPixel);

        if (intervalBeginTime < 0) {
            intervalBeginTime = 0;
        }

        intervalEndTime = intervalBeginTime + (long) (imageWidth * msPerPixel);
        recalculateTagSizes();
        crossHairPos = xAt(mediaTime);
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());
        updateHorScrollBar();
        paintBuffer();

        if (playing) {
            startPlayer();
        }
        float zoomFl = 100f * (10f / msPerPixel);
        int zoom = (int) zoomFl;
//        int zoom = (int) (100f * (10f / msPerPixel));

        updateZoomPopup(zoomFl);
        
        setPreference("TimeLineViewer.ZoomLevel", new Float(zoomFl), 
	        		transcription);

        if (zoomSlider != null) {
        	zoomSlider.removeChangeListener(this);
        	if (zoom <= 100) {
        		if (zoom < zoomSlider.getMinimum()) {
        			zoomSlider.setValue(zoomSlider.getMinimum());
        		} else {
        			zoomSlider.setValue(zoom);
        		}
        	} else {
        		// recalculate for > 100 values.
        		float factor = (zoom - 100) / (float) 900;
        		int zm = 100 + (int) ( factor * (zoomSlider.getMaximum() - 100) );
        		if (zm != zoomSlider.getValue()) {        			
        			zoomSlider.setValue(zm);
        		}
        	}
			zoomSlider.addChangeListener(this);
			zoomSlider.setToolTipText(String.valueOf(zoom));
        }
        //repaint();
    }

    /**
     * Returns the current msPerPixel.
     *
     * @return msPerPixel
     */
    public float getMsPerPixel() {
        return msPerPixel;
    }

    /**
     * Calls #setMsPerPixel with the appropriate value. In setMsPerPixel the
     * value of this.resolution is actually set. msPerPixel = 1000 / resolution<br>
     * resolution = 1000 / msPerPixel
     *
     * @param resolution the new resolution
     */
    public void setResolution(int resolution) {
        if (resolution < 1) {
            this.resolution = 1;
        } else {
            this.resolution = resolution;
        }

        float mspp = (1000f / resolution);
        setMsPerPixel(mspp);
    }

    /**
     * Sets the resolution by providing a factor the default PIXELS_FOR_SECOND
     * should be multiplied with.<br>
     * resolution = factor * PIXELS_FOR_SECOND.<br>
     * <b>Note:</b><br>
     * The factor = 100 / resolution_menu_percentage !
     *
     * @param factor the multiplication factor
     */
    public void setResolutionFactor(float factor) {
        int res = (int) (PIXELS_FOR_SECOND * factor);
        setResolution(res);
    }

    /**
     * Gets the current resolution
     *
     * @return the current resolution
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * Find the tag at the given location.
     *
     * @param p the location
     * @param tierIndex the tier the tag should be found in
     *
     * @return the tag
     */
    private Tag2D getTagAt(Point2D p, int tierIndex) {
        if ((tierIndex < 0) || (tierIndex > (visibleTiers.size() - 1))) {
            return null;
        }

        long pTime = pixelToTime((int) p.getX());
        Tag2D t2d;
        Iterator<Tag2D> it = visibleTiers.get(tierIndex).getTags();

        while (it.hasNext()) {
            t2d = it.next();

            if ((pTime >= t2d.getBeginTime()) && (pTime <= t2d.getEndTime())) {
                return t2d;
            }
        }

        return null;
    }

    /**
     * Calculate the index in the visible tiers array for the given y
     * coordinate.
     *
     * @param p DOCUMENT ME!
     *
     * @return the index of the Tier2D  or -1 when not found
     */
    private int getTierIndexForPoint(Point2D p) {
        int y = (int) p.getY() - rulerHeight;

        if ((y < 0) || (y > (visibleTiers.size() * pixelsForTierHeight))) {
            return -1;
        } else {
            return y / pixelsForTierHeight;
        }
    }

    /**
     * Calculate the index in the visible tiers array for the given annotation.
     *
     * @param annotation DOCUMENT ME!
     *
     * @return the index of the Tier2D or -1 when not found
     */
    private int getTierIndexForAnnotation(Annotation annotation) {
        Tier tier = annotation.getTier();
        int index = -1;

        for (int i = 0; i < visibleTiers.size(); i++) {
            if (visibleTiers.get(i).getTier() == tier) {
                index = i;

                break;
            }
        }

        return index;
    }

    /**
     * Inverts the point and finds the Tag2D at that point.
     *
     * @param p point in component space
     *
     * @return a tag2d or null
     */
    private Tag2D getHoverTag(Point p) {
        p.x += timeToPixels(intervalBeginTime);
        p.y += verticalScrollOffset;
        hoverTierIndex = getTierIndexForPoint(p);

        Tag2D hover = getTagAt(p, hoverTierIndex);

        return hover;
    }

    /**
     * Update the dragedit tag2d while dragging. <br>
     * Checks on the parent's boundaries (if any).
     *
     * @param dragEndPoint the position of the mouse pointer
     */
    private void updateDragEditTag(Point dragEndPoint) {
        if (dragEditTag2D == null) {
            return;
        }
        
        dragEditTag2D.move(dragEndPoint.x - dragStartPoint.x, dragEndPoint.y - dragStartPoint.y);
        int diff = dragEndPoint.x - dragStartPoint.x;
        int diffy = dragEndPoint.y - dragStartPoint.y;

        //if (Math.abs(dragEditTag2D.getDy()) > Math.abs(dragEditTag2D.getDx())) {
        if (dragEditMode == DRAG_EDIT_CENTER && (Math.abs(dragEditTag2D.getDy()) > Math.abs(dragEditTag2D.getDx()) || 
        		Math.abs(dragEditTag2D.getDy()) >= pixelsForTierHeight)) {
        	// vertical dragging
        	dragEditTag2D.setY(dragEditTag2D.getY() + diffy);
        	dragEditTag2D.resetX();
            dragStartPoint = dragEndPoint;
            int index = getTierIndexForPoint(new Point(dragEndPoint.x, dragEndPoint.y + verticalScrollOffset));
            if (index > -1 && index < visibleTiers.size()) {
            	Tier2D t2d = visibleTiers.get(index);
            	if (t2d != dragEditTag2D.getTier2D() && t2d.getTier().getParentTier() == null) {
            		dragEditTag2D.isOverTargetTier = true;
            	} else {
            		dragEditTag2D.isOverTargetTier = false;
            	}
            }
        } else {
        	// horizontal dragging
	        switch (dragEditMode) {
	        case DRAG_EDIT_CENTER:
	        	dragEditTag2D.resetY();
	            if (dragParentBegin == -1) {
	                dragEditTag2D.setX(dragEditTag2D.getX() + diff);
	                dragStartPoint = dragEndPoint;
	            } else {
	                long bt = pixelToTime(dragEditTag2D.getX() + diff);
	
	                if (diff < 0) {
	                    if (bt < dragParentBegin) {
	                        bt = dragParentBegin;
	
	                        int nx = timeToPixels(bt);
	                        dragEditTag2D.setX(nx);
	                    } else {
	                        dragEditTag2D.setX(dragEditTag2D.getX() + diff);
	                        dragStartPoint = dragEndPoint;
	                    }
	                } else {
	                    long et = pixelToTime(dragEditTag2D.getX() +
	                            dragEditTag2D.getWidth() + diff);
	
	                    if (et > dragParentEnd) {
	                        et = dragParentEnd;
	                        bt = et - pixelToTime(dragEditTag2D.getWidth());
	
	                        dragEditTag2D.setX(timeToPixels(bt));
	                    } else {
	                        dragEditTag2D.setX(dragEditTag2D.getX() + diff);
	                        dragStartPoint = dragEndPoint;
	                    }
	                }
	            }
	
	            setMediaTime(pixelToTime(dragEditTag2D.getX()));
	
	            break;
	
	        case DRAG_EDIT_LEFT:
	
	            if ((dragEditTag2D.getX() + diff) < ((dragEditTag2D.getX() +
	                    dragEditTag2D.getWidth()) - 1)) {
	                if ((dragParentBegin == -1) || (diff > 0)) {
	                    dragEditTag2D.setX(dragEditTag2D.getX() + diff);
	                    dragEditTag2D.setWidth(dragEditTag2D.getWidth() - diff);
	                    dragStartPoint = dragEndPoint;
	                } else if ((dragParentBegin > -1) && (diff < 0)) {
	                    long bt = pixelToTime(dragEditTag2D.getX() + diff);
	
	                    if (bt < dragParentBegin) {
	                        bt = dragParentBegin;
	
	                        int nx = timeToPixels(bt);
	                        dragEditTag2D.setX(nx);
	                        dragEditTag2D.setWidth(timeToPixels(dragEditTag2D.getEndTime() -
	                                bt));
	                    } else {
	                        dragEditTag2D.setX(dragEditTag2D.getX() + diff);
	                        dragEditTag2D.setWidth(dragEditTag2D.getWidth() - diff);
	                        dragStartPoint = dragEndPoint;
	                    }
	                }
	
	                setMediaTime(pixelToTime(dragEditTag2D.getX()));
	            }
	
	            break;
	
	        case DRAG_EDIT_RIGHT:
	
	            if ((dragEditTag2D.getWidth() + diff) > 1) {
	                if ((dragParentEnd == -1) || (diff < 0)) {
	                    dragEditTag2D.setWidth(dragEditTag2D.getWidth() + diff);
	                    dragStartPoint = dragEndPoint;
	                } else if ((dragParentEnd > -1) && (diff > 0)) {
	                    long et = pixelToTime(dragEditTag2D.getX() +
	                            dragEditTag2D.getWidth() + diff);
	
	                    if (et > dragParentEnd) {
	                        et = dragParentEnd;
	                        dragEditTag2D.setWidth(timeToPixels(et) -
	                            dragEditTag2D.getX());
	                    } else {
	                        dragEditTag2D.setWidth(dragEditTag2D.getWidth() + diff);
	                        dragStartPoint = dragEndPoint;
	                    }
	                }
	
	                setMediaTime(pixelToTime(dragEditTag2D.getX() +
	                        dragEditTag2D.getWidth()));
	            }
	
	            break;
	        }
        }

        repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @return the current interval begin time
     */
    public long getIntervalBeginTime() {
        return intervalBeginTime;
    }

    /**
     * DOCUMENT ME!
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
     * Calculates the new interval begin and/or end time.<br>
     * There are two special cases taken into account:<br>
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
     * 
     *
     * @param mediaTime
     */
    private void recalculateInterval(final long mediaTime) {
        long newBeginTime = intervalBeginTime;
        long newEndTime = intervalEndTime;

        if (playerIsPlaying()) {
            // we might be in a selection outside the new interval
            // shift the interval n * intervalsize to the left or right
            if (mediaTime > intervalEndTime) {
                newBeginTime = intervalEndTime;
                newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);

                while ((newEndTime += (imageWidth + msPerPixel)) < mediaTime) {
                    newBeginTime += (imageWidth * msPerPixel);
                }
            } else if (mediaTime < intervalBeginTime) {
                newEndTime = intervalBeginTime;
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                while ((newEndTime -= (imageWidth * msPerPixel)) > mediaTime) {
                    newBeginTime -= (imageWidth * msPerPixel);
                }

                if (newBeginTime < 0) {
                    newBeginTime = 0;
                    newEndTime = (long) (imageWidth * msPerPixel);
                }
            } else {
                // the new time appears to be in the current interval after all
                return;
            }
        } else { //player is not playing

            // is the new media time to the left or to the right of the current interval
            if (mediaTime < intervalBeginTime) {
                newBeginTime = mediaTime - (int) (SCROLL_OFFSET * msPerPixel);

                if (newBeginTime < 0) {
                    newBeginTime = 0;
                }

                newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
            } else if (mediaTime > intervalEndTime) {
                newEndTime = mediaTime + (int) (SCROLL_OFFSET * msPerPixel);
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                if (newBeginTime < 0) { // something would be wrong??
                    newBeginTime = 0;
                    newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
                }
            }

            if ((newBeginTime == getSelectionBeginTime()) &&
                    (newBeginTime > (SCROLL_OFFSET * msPerPixel))) {
                newBeginTime -= (SCROLL_OFFSET * msPerPixel);
                newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
            }

            if (newEndTime == getSelectionEndTime()) {
                newEndTime += (SCROLL_OFFSET * msPerPixel);
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                if (newBeginTime < 0) { // something would be wrong??
                    newBeginTime = 0;
                    newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
                }
            }

            // try to position the whole selection in the view
            if ((mediaTime == getSelectionBeginTime()) &&
                    (getSelectionEndTime() > (newEndTime -
                    (SCROLL_OFFSET * msPerPixel))) && !panMode) {
                newEndTime = getSelectionEndTime() +
                    (int) (SCROLL_OFFSET * msPerPixel);
                newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                if ((newBeginTime > mediaTime) &&
                        (mediaTime > (SCROLL_OFFSET * msPerPixel))) {
                    newBeginTime = mediaTime - (int) (SCROLL_OFFSET * msPerPixel);
                    newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
                } else if (newBeginTime > mediaTime) {
                    newBeginTime = 0;
                    newEndTime = (long) (imageWidth * msPerPixel);
                }
            }
        }

        if (timeScaleConnected) {
            //System.out.println("TLV new begin time: " + newBeginTime);
            //System.out.println("TLV new end time: " + newEndTime);
            setGlobalTimeScaleIntervalBeginTime(newBeginTime);
            setGlobalTimeScaleIntervalEndTime(newEndTime);
        } else {
            setLocalTimeScaleIntervalBeginTime(newBeginTime);
        }
    }

    /**
     * Changes the interval begin time locally.
     *
     * @param begin the new local interval begin time
     */
    private void setLocalTimeScaleIntervalBeginTime(long begin) {
        if (begin == intervalBeginTime) {
            return;
        }

        intervalBeginTime = begin;
        intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);

        //
        if (editBox.isVisible()) {
            if (getActiveAnnotation() != null) {
                int x = xAt(getActiveAnnotation().getBeginTimeBoundary());
                editBox.setLocation(x, editBox.getY());
            } else {
                dismissEditBox();
            }

            /*
               if (x < 0 || x > imageWidth) {
                   dismissEditBox();
               } else {
                   editBox.setLocation(x, editBox.getY());
               }
             */
        }

        //
        crossHairPos = xAt(crossHairTime);
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());
        updateHorScrollBar();
        paintBuffer();
    }

    /**
     * DOCUMENT ME!
     *
     * @return the vertical scroll offset
     */
    public int getVerticalScrollOffset() {
        return verticalScrollOffset;
    }

    /**
     * Sets the vertical scroll offset of the tags on this component. <b>Note:</b><br>
     * There should be some kind of synchronization with other viewers:
     * pending..
     *
     * @param offset the new vertical scroll offset
     */
    public void setVerticalScrollOffset(int offset) {
        verticalScrollOffset = offset;
//        repaint();
        paintBuffer();
    }

    /**
     * Scrolls the viewport vertically to ensure the cursorTag is visible.
     */
    private void ensureVerticalVisibilityOfActiveAnnotation() {
        if (cursorTag2D == null) {
            return;
        }

        int cy = cursorTierIndex * pixelsForTierHeight;

        if (cy < verticalScrollOffset) {
            scrollBar.setValue(cy);
        } else if (((cy + pixelsForTierHeight) - verticalScrollOffset) > (getHeight() -
                rulerHeight)) {
            scrollBar.setValue((cy + pixelsForTierHeight + rulerHeight) -
                getHeight());
        }
    }
  
    /**
     * 
     * @return the horizontal scroll offset
     */
    public int getHorizontalScrollOffset() {
    	return horizontalScrollOffset;
    }
    
    /**
     * Sets the vertical scroll offset
     * @param offset
     */
    public void setHorizontalScrollOffset(int offset) {
    	horizontalScrollOffset = offset;
    }

    /**
     * Update the values of the scrollbar.<br>
     * Called after a change in the number of visible tiers.
     */
    private void updateScrollBar() {
        int value = scrollBar.getValue();
        int max = (visibleTiers.size() * pixelsForTierHeight) + rulerHeight;

        // before changing scrollbar values do a setValue(0), otherwise
        // setMaximum and/or setVisibleAmount will not be accurate
        scrollBar.setValue(0);
        scrollBar.setMaximum(max);
        if (hScrollBarVisible) {
            scrollBar.setVisibleAmount(getHeight() - defBarWidth);
        } else {
        scrollBar.setVisibleAmount(getHeight());
        }

        if ((value + getHeight()) > max) {
            value = max - getHeight();
        }

        scrollBar.setValue(value);
        scrollBar.revalidate();
    }

    /**
     * Updates the values of the horizontal scrollbar. Called when the interval begin time, the 
     * resolution (msPerPixel), the viewer's width or the master media duration has changed. 
     */
    private void updateHorScrollBar() {
        if (!hScrollBarVisible) {
            return;
        }
        int value = hScrollBar.getValue();
        if (value != (int)(intervalBeginTime / msPerPixel)) {
            value = (int)(intervalBeginTime / msPerPixel);
        }
        int max = (int) (getMediaDuration() / msPerPixel + DEFAULT_MS_PER_PIXEL);
        hScrollBar.removeAdjustmentListener(this);
        
        //hScrollBar.setValue(0);
        //hScrollBar.setMaximum(max);
        //hScrollBar.setVisibleAmount(getWidth() - defBarWidth);
        
//        if (value != hScrollBar.getValue()) {
//            hScrollBar.setValue(value);
//        }
        // this should only be necessary after a resize
        hScrollBar.setBlockIncrement(getWidth() - defBarWidth);
        hScrollBar.setValues(value, getWidth() - defBarWidth, 0, max);
        hScrollBar.revalidate();
        hScrollBar.addAdjustmentListener(this);
    }

    /**
     * Calculate the y positions of the vertical middle of all visible tiers
     * and pass them to the MultiViewerController.
     */
    private void notifyMultiTierControlPanel() {
        if (multiTierControlPanel == null) {
            return;
        }

        if (tierYPositions.length != visibleTiers.size()) {
        	tierYPositions = new int[visibleTiers.size()];
        }
        if (tierYPositions.length > 0) {
        	int bh = timeRulerVisible ? rulerHeight : 0;
            tierYPositions[0] = (bh + (pixelsForTierHeight / 2)) -
                verticalScrollOffset;

            for (int i = 1; i < visibleTiers.size(); i++) {
                tierYPositions[i] = tierYPositions[0] +
                    (i * pixelsForTierHeight);
            }
        }

        multiTierControlPanel.setTierPositions(tierYPositions);
    }

    /**
     * Returns the actual size of the viewer (viewable area), i.e. the size of
     * the component minus the size of the scrollbar.<br>
     * Needed for the accurate alignment with other viewers.
     *
     * @return the actual size of the viewer
     */
    public Dimension getViewerSize() {
        return new Dimension(imageWidth, imageHeight);
    }

    /**
     * Layout information, gives the nr of pixels at the left of the viewer
     * panel that contains no time line information
     *
     * @return the nr of pixels at the left that contain no time line related
     *         data
     */
    public int getLeftMargin() {
        return 0;
    }

    /**
     * Layout information, gives the nr of pixels at the right of the viewer
     * panel that contains no time line information
     *
     * @return the nr of pixels at the right that contain no time line related
     *         data
     */
    public int getRightMargin() {
        return scrollBar.getWidth();
    }

    /**
     * Create a popup menu to enable the manipulation of some settings for this
     * viewer.
     */
    private void createPopupMenu() {
        popup = new JPopupMenu("TimeLine Viewer");
        zoomMI = new JMenu(ElanLocale.getString("TimeScaleBasedViewer.Zoom"));
        zoomBG = new ButtonGroup();
        zoomSelectionMI = new JMenuItem(
        		ElanLocale.getString("TimeScaleBasedViewer.Zoom.Selection"));
        zoomSelectionMI.addActionListener(this);
        zoomMI.add(zoomSelectionMI);
        // add zoom to show entire media file item
        zoomToEntireMediaMI = new JMenuItem(
        		ElanLocale.getString("TimeScaleBasedViewer.Zoom.EntireMedia"));
        zoomToEntireMediaMI.addActionListener(this);
        zoomMI.add(zoomToEntireMediaMI);
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

        // font size items		
        int fontSize = getFont().getSize();
        fontSizeBG = new ButtonGroup();
        fontMenu = new JMenu(ElanLocale.getString("Menu.View.FontSize"));
        
		JRadioButtonMenuItem fontRB;
		
		for (int element : Constants.FONT_SIZES) {
			fontRB = new JRadioButtonMenuItem(String.valueOf(element));
			fontRB.setActionCommand("font" + element);
			if (fontSize == element) {
				fontRB.setSelected(true);
			}
			fontRB.addActionListener(this);
			fontSizeBG.add(fontRB);
			fontMenu.add(fontRB);
		}

        popup.add(fontMenu);

        activeAnnStrokeBoldMI = new JCheckBoxMenuItem(ElanLocale.getString(
                    "TimeLineViewer.ActiveAnnotationBold"));
        activeAnnStrokeBoldMI.setSelected(aaStrokeBold);
        activeAnnStrokeBoldMI.setActionCommand("aastroke");
        activeAnnStrokeBoldMI.addActionListener(this);
        popup.add(activeAnnStrokeBoldMI);
        
        reducedTierHeightMI = new JCheckBoxMenuItem(ElanLocale.getString(
        		"TimeLineViewer.ReducedTierHeight"));
        reducedTierHeightMI.setSelected(reducedTierHeight);
        reducedTierHeightMI.setActionCommand("redTH");
        reducedTierHeightMI.addActionListener(this);
        popup.add(reducedTierHeightMI);

        hScrollBarVisMI = new JCheckBoxMenuItem(ElanLocale.getString(
                "TimeLineViewer.Menu.HScrollBar"));
        hScrollBarVisMI.setSelected(hScrollBarVisible);
        hScrollBarVisMI.setActionCommand("hsVis");
        hScrollBarVisMI.addActionListener(this);
        popup.add(hScrollBarVisMI);

        timeRulerVisMI = new JCheckBoxMenuItem(ElanLocale.getString(
        		"TimeScaleBasedViewer.TimeRuler.Visible"));
        timeRulerVisMI.setSelected(timeRulerVisible);
        timeRulerVisMI.addActionListener(this);
        popup.add(timeRulerVisMI);
        popup.addSeparator();

        timeScaleConMI = new JCheckBoxMenuItem(ElanLocale.getString(
                    "TimeScaleBasedViewer.Connected"), timeScaleConnected);
        timeScaleConMI.setActionCommand("connect");
        timeScaleConMI.addActionListener(this);
        popup.add(timeScaleConMI);
        
        tickerModeMI = new JCheckBoxMenuItem(ElanLocale.getString(
        		"TimeScaleBasedViewer.TickerMode"));
        tickerModeMI.setSelected(tickerMode);
        tickerModeMI.addActionListener(this);
        popup.add(tickerModeMI);

        // All static menu items must go before the context sensitive items.
        staticMenuItems = popup.getComponentCount();
        contextSensitiveItems = new ArrayList<JMenuItem>();

        popup.addSeparator();
        contextSensitiveItems.add(null);
        
        // annotation menu items
        newAnnoMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Annotation.NewAnnotation"));
        newAnnoMI.setActionCommand("newAnn");
        newAnnoMI.addActionListener(this);
        popup.add(newAnnoMI);
        contextSensitiveItems.add(newAnnoMI);

        newAnnoBeforeMI = new JMenuItem(ELANCommandFactory.getCommandAction(
        		transcription, ELANCommandFactory.NEW_ANNOTATION_BEFORE));
        popup.add(newAnnoBeforeMI);
        contextSensitiveItems.add(newAnnoBeforeMI);

        newAnnoAfterMI = new JMenuItem(ELANCommandFactory.getCommandAction(
        		transcription, ELANCommandFactory.NEW_ANNOTATION_AFTER));
        popup.add(newAnnoAfterMI);
        contextSensitiveItems.add(newAnnoAfterMI);

        modifyAnnoMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Annotation.ModifyAnnotation"));
        modifyAnnoMI.setActionCommand("modifyAnn");
        modifyAnnoMI.addActionListener(this);
        popup.add(modifyAnnoMI);
        contextSensitiveItems.add(modifyAnnoMI);

        modifyAnnoDCMI = new JMenuItem(ELANCommandFactory.getCommandAction(
        		transcription, ELANCommandFactory.MODIFY_ANNOTATION_DC_DLG));
        popup.add(modifyAnnoDCMI);
        contextSensitiveItems.add(modifyAnnoDCMI);
        
        showInBrowserMI = new JMenuItem(ELANCommandFactory.getCommandAction(
        		transcription, ELANCommandFactory.SHOW_IN_BROWSER));
        popup.add(showInBrowserMI);
        contextSensitiveItems.add(showInBrowserMI);
        
        mergeAnnoNextMI = new JMenuItem(ELANCommandFactory.getCommandAction(transcription, 
        		ELANCommandFactory.MERGE_ANNOTATION_WN));
        popup.add(mergeAnnoNextMI);
        contextSensitiveItems.add(mergeAnnoNextMI);
        
        mergeAnnoBeforeMI = new JMenuItem(ELANCommandFactory.getCommandAction(transcription, 
        		ELANCommandFactory.MERGE_ANNOTATION_WB));
        popup.add(mergeAnnoBeforeMI);
        contextSensitiveItems.add(mergeAnnoBeforeMI);

        splitAnnotationMI = new JMenuItem(ElanLocale.getString(ELANCommandFactory.SPLIT_ANNOTATION));
        splitAnnotationMI.setEnabled(false);
        splitAnnotationMI.addActionListener(this);
        popup.add(splitAnnotationMI);
        contextSensitiveItems.add(splitAnnotationMI);

        deleteAnnoValueMI = new JMenuItem(ELANCommandFactory.getCommandAction(
        		transcription, ELANCommandFactory.REMOVE_ANNOTATION_VALUE));
        popup.add(deleteAnnoValueMI);
        contextSensitiveItems.add(deleteAnnoValueMI);
        
        modifyAnnoTimeMI = new JMenuItem(ELANCommandFactory.getCommandAction(
                    transcription, ELANCommandFactory.MODIFY_ANNOTATION_TIME));
        popup.add(modifyAnnoTimeMI);
        contextSensitiveItems.add(modifyAnnoTimeMI);
        
        modifyAnnoTimeDlgMI = new JMenuItem(ELANCommandFactory.getCommandAction(
                transcription, ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG));
        popup.add(modifyAnnoTimeDlgMI);
        contextSensitiveItems.add(modifyAnnoTimeDlgMI);
        
        deleteAnnoMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Annotation.DeleteAnnotation"));
        deleteAnnoMI.setActionCommand("deleteAnn");
        deleteAnnoMI.addActionListener(this);
        popup.add(deleteAnnoMI);
        contextSensitiveItems.add(deleteAnnoMI);
        
        deleteSelAnnosMI = new JMenuItem(ElanLocale.getString("Menu.Annotation.DeleteSelectedAnnotations"));
        deleteSelAnnosMI.addActionListener(this);       
        popup.add(deleteSelAnnosMI);
        contextSensitiveItems.add(deleteSelAnnosMI);
        
        // copy and paste
        popup.addSeparator();
        contextSensitiveItems.add(null);
        
        copyAnnoMI = new JMenuItem(ElanLocale.getString(
        		"Menu.Annotation.CopyAnnotation"));
        copyAnnoMI.addActionListener(this);
        popup.add(copyAnnoMI);
        contextSensitiveItems.add(copyAnnoMI);

        copyAnnoTreeMI = new JMenuItem(ElanLocale.getString(
        		"Menu.Annotation.CopyAnnotationTree"));
        copyAnnoTreeMI.addActionListener(this);
        popup.add(copyAnnoTreeMI);
        contextSensitiveItems.add(copyAnnoTreeMI);

        pasteAnnoHereMI = new JMenuItem(ElanLocale.getString(
        		"Menu.Annotation.PasteAnnotationHere"));
        pasteAnnoHereMI.addActionListener(this);
        popup.add(pasteAnnoHereMI);
        contextSensitiveItems.add(pasteAnnoHereMI);

        pasteAnnoTreeHereMI = new JMenuItem(ElanLocale.getString(
        		"Menu.Annotation.PasteAnnotationTreeHere"));
        pasteAnnoTreeHereMI.addActionListener(this);
        popup.add(pasteAnnoTreeHereMI);
        contextSensitiveItems.add(pasteAnnoTreeHereMI);

        popup.addSeparator();
        contextSensitiveItems.add(null);

        shiftActiveAnnMI = new JMenuItem(ELANCommandFactory.getCommandAction(
        		transcription, ELANCommandFactory.SHIFT_ACTIVE_ANNOTATION));
        shiftActiveAnnMI.setText(ElanLocale.getString("Menu.Annotation.Shift") + " "
        		+ ElanLocale.getString("Menu.Annotation.ShiftActiveAnnotation"));
        popup.add(shiftActiveAnnMI);
        contextSensitiveItems.add(shiftActiveAnnMI);
        
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        float zoomFl = 100f * (10f / msPerPixel);
//        int zoom = (int) (100f * (10f / msPerPixel));
//
//        if (zoom <= 0) {
//            zoom = 100;
//        }

        updateZoomPopup(zoomFl);
    }

    /**
     * Updates the "zoom" menu item. Needed, when timeScaleConnected, after a
     * change of the zoomlevel in some other connected viewer.
     *
     * @param zoom the zoom level
     */
    private void updateZoomPopup(float zoom) {
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
     * Enables / Disables the split annotation option in the 
     * popup
     */
    
    private void checkForSplitAnnotation(){
    	splitAnnotationMI.setEnabled(false);
    	Annotation activeAnnotation = getActiveAnnotation();
    	if(activeAnnotation != null){
    		TierImpl tier = (TierImpl) activeAnnotation.getTier();	    	   
 	  	   	
 	  	   	if(tier.isTimeAlignable() && !tier.hasParentTier()){ 	
 	  	   		List<TierImpl> childTiers = tier.getChildTiers();
 	  	   		if(childTiers!= null){
 	  	   			boolean valid = false;
 	  	   			for(int i=0; i <childTiers.size(); i++){
 	  	   				TierImpl childTier = childTiers.get(i);
 	  	   				if(childTier.getLinguisticType().getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION){
 	  	   					valid = false;
 	  	   					break;
 	  	   				}
 	  	   				valid = true;
 	  	   			}
 	  	   			splitAnnotationMI.setEnabled(valid);
 	  	   			
 	  	   			if(!valid){
 	  	   				List<TierImpl> dependentTiers = tier.getDependentTiers();
// 	  	   				List<Annotation> dependingAnnotations = new List<Annotation>();
// 	  	   				for(int i=0;i < dependentTiers.size();i++){
// 	  	   					if( dependentTiers.get(i).getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
// 	  	   						continue; 
// 	  	   					}
// 	  	   					dependingAnnotations.addAll(activeAnnotation.getChildrenOnTier(dependentTiers.get(i)));
// 	  	   				}	
// 	  	   				if(dependingAnnotations.size() == 0){	  			 
// 	  	   					splitAnnotationMI.setEnabled(true);
// 	  	   				} 
 	  	   				boolean dependingAnnotationsExist = false;
 	  	   				for (TierImpl depTier : dependentTiers){
 	  	   					if (depTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
 	  	   						continue; 
 	  	   					}
 	  	   					if (activeAnnotation.getChildrenOnTier(depTier).size() > 0) {
	 	  	   					dependingAnnotationsExist = true;
	 	  	   					break;
 	  	   					};
 	  	   				}	
 	  	   				if (!dependingAnnotationsExist) {	  			 
 	  	   					splitAnnotationMI.setEnabled(true);
 	  	   				} 
 	  	   			}
 	  	   		}
 	  	   	}
    	}
	 }

    /**
     * Zooms in to the next level of predefined zoomlevels.
     * Note: has to be adapted once custom zoomlevels are implemented.
     * @version 08-2012 a temporary change in behaviour is now the new, standard behaviour
     */
    private void zoomIn() {
    	float zoom = 100 / (msPerPixel / 10);
    	// 08-2012 this temporary implementation is now the standard behaviour
    	float nz = zoom + 10;
    	float nm = ((100f / nz) * 10);
    	setMsPerPixel(nm);
    }
   
    /**
     * Zooms in to the next level of predefined zoomlevels.
     * Note: has to be adapted once custom zoomlevels are implemented.
     * @version 08-2012 a temporary change in behaviour is now the new, standard behaviour
     */
    private void zoomOut() {
    	float zoom = 100 / (msPerPixel / 10);
    	
    	// 08-2012 this temporary implementation is now the standard behaviour
    	float nz = zoom - 10;
    	if (nz < ZOOMLEVELS[0]) {
    		nz = ZOOMLEVELS[0];
    	}
    	float nm = ((100f / nz) * 10);
    	setMsPerPixel(nm);

    }
    
    private void zoomToSelection() {
    	long selInterval = getSelectionEndTime() - getSelectionBeginTime();
    	if (selInterval < 150) {
    		selInterval = 150;
    	}
    	int sw = imageWidth != 0 ? imageWidth - (2 * SCROLL_OFFSET) : getWidth() - defBarWidth - (2 * SCROLL_OFFSET);
    	float nextMsPP = selInterval / (float) sw;
    	//System.out.println("interval: " + selInterval + " mspp: " + nextMsPP);
    	// set a limit of zoom = 5% or mspp = 200
//    	if (nextMsPP > 200) {
//    		nextMsPP = 200;
//    	}
    	setMsPerPixel(nextMsPP);
    	//customZoomMI.setSelected(true);
    	//customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom") + " - " + (int)(100 / ((float) msPerPixel / 10)) + "%");
    	if (!playerIsPlaying()) {
    		long ibt = getSelectionBeginTime() - (long)(SCROLL_OFFSET * msPerPixel);
    		if (ibt < 0) {
    			ibt = 0;
    		}
    		setIntervalBeginTime(ibt);
    	}
    	setPreference("TimeLineViewer.ZoomLevel", new Float(100f * (10f / msPerPixel)), 
        		transcription);
    }
    
    /**
     * Zoom out (or in) to such a level that the entire media file is displayed in 
     * the viewer area (leaving a small margin on the right side of the viewer).
     */
    private void zoomToShowEntireMedia() {
    	int areaWidth = getWidth() - defBarWidth - SCROLL_OFFSET;
    	float nextMsPP = getMediaDuration() / (float) areaWidth;
    	if (nextMsPP != msPerPixel) {
	    	setMsPerPixel(nextMsPP);
	    	setIntervalBeginTime(0);
	    	if (getViewerManager() != null) {
	    		setPreference("SignalViewer.ZoomLevel", new Float(100f * (10f / msPerPixel)), 
	        		getViewerManager().getTranscription());
	    	}
    	}
    }
    
    /**
     * Zoomable interface, zooms in and out along predefined levels.
     */
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
    	
    	if (zoomMenuIndex > -1 && zoomMenuIndex < ZOOMLEVELS.length - 1) {
    		int nextZoom = ZOOMLEVELS[zoomMenuIndex + 1];
    		float nextMsPerPixel = ((100f / nextZoom) * 10);
    	    setMsPerPixel(nextMsPerPixel);
    		updateZoomPopup(nextZoom);
    	}
    	*/	
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

	/**
     * Shows / hides annotation and tier specific menuitems, depending on
     * the mouse click position.<br>
     * <b>Note: </b> this might need to be changed once usage of Action and
     * Command objects is implemented.
     *
     * @param p the position of the mouse click
     */
    private void updatePopup(Point p) {
    	enableAndDisablePopupItems(p);
        addAndRemovePopupItems();
    }
    
    /**
     * Enables / disables annotation and tier specific menuitems, depending on
     * the mouse click position.<br>
     * <b>Note: </b> this might need to be changed once usage of Action and
     * Command objects is implemented.
     *
     * @param p the position of the mouse click
     */
    private void enableAndDisablePopupItems(Point p) {
        //disable all first
        newAnnoMI.setEnabled(false);
        newAnnoBeforeMI.setEnabled(false);
        newAnnoAfterMI.setEnabled(false);
        modifyAnnoMI.setEnabled(false);
        modifyAnnoDCMI.setEnabled(false);
        showInBrowserMI.setEnabled(false);
        modifyAnnoTimeMI.setEnabled(false);
        deleteAnnoValueMI.setEnabled(false);
        deleteAnnoMI.setEnabled(false);
        deleteSelAnnosMI.setEnabled(false);
        copyAnnoMI.setEnabled(false);
        copyAnnoTreeMI.setEnabled(false);
        pasteAnnoHereMI.setEnabled(false);
        pasteAnnoTreeHereMI.setEnabled(false); 
//        mergeAnnoNextMI.setEnabled(false);	// enabled/disabled from its Action
//        mergeAnnoBeforeMI.setEnabled(false);	// enabled/disabled from its Action
        splitAnnotationMI.setEnabled(false);
        
        splitTime =  0;

        // update zoom to selection mi first, this has to be done independent of Point p.      
        zoomSelectionMI.setEnabled(getSelectionBeginTime() != getSelectionEndTime());
        if (timeRulerVisible && (p.y < rulerHeight)) {
            return; 
        } else {
            Point inverse = new Point(p);

            //compensate for the intervalBeginTime
            inverse.x += timeToPixels(intervalBeginTime);
            inverse.y += verticalScrollOffset;
            rightClickTime = pixelToTime(inverse.x);

            if (rightClickTime > getMediaDuration()) {
                return;
            }

            boolean supportsInsertion = false;

            TierImpl tier;

            Tag2D tag2d;
            int tierIndex = getTierIndexForPoint(inverse);

            if (tierIndex < 0) {
                return;
            }

            rightClickTier = visibleTiers.get(tierIndex);
            tier = rightClickTier.getTier();

            if (tier == null) {
                return;
            }

            try {
                LinguisticType lt = tier.getLinguisticType();
                Constraint c = null;

                if (lt != null) {
                    c = lt.getConstraints();
                }

                if (c != null) {
                    supportsInsertion = c.supportsInsertion();
                }
                
                checkForSplitAnnotation(); 
                splitTime = rightClickTime;

                if (selectedAnnotations.size() > 0) {
                	deleteSelAnnosMI.setEnabled(true);
                }

                tag2d = getTagAt(inverse, tierIndex);

                if ((tag2d != null) && (tag2d == cursorTag2D)) {
                    modifyAnnoMI.setEnabled(true);
                    modifyAnnoDCMI.setEnabled(true);
                    deleteAnnoValueMI.setEnabled(true);
                    deleteAnnoMI.setEnabled(true);
                    copyAnnoMI.setEnabled(true);
                    copyAnnoTreeMI.setEnabled(true);

                    if (supportsInsertion) {
                        newAnnoAfterMI.setEnabled(true);
                        newAnnoBeforeMI.setEnabled(true);
                    }

                    // removed else here...
                    if (tier.isTimeAlignable()) {
                        newAnnoMI.setEnabled(true); //replace an existing annotation??

                        if (getSelectionBeginTime() != getSelectionEndTime()) {
                            modifyAnnoTimeMI.setEnabled(true);
                        }
                    }
                    
                    if(ShowInBrowserCommand.hasBrowserLinkInECV(getViewerManager().getActiveAnnotation().getAnnotation())) {
                    	showInBrowserMI.setEnabled(true);
                    }
                } else {
                    // check this, much to complicated
                    // this should be based on the Constraints object..
                    if ((getSelectionBeginTime() != getSelectionEndTime()) &&
                            (getSelectionBeginTime() <= rightClickTime) &&
                            (getSelectionEndTime() >= rightClickTime)) {
                        if (tier.isTimeAlignable()) {
                            newAnnoMI.setEnabled(true);
                        } else {
                            if ((tier.getParentTier() != null) &&
                                    ((c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) ||
                                    (c.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION))) {
                                Annotation parentA = tier.getParentTier().getAnnotationAtTime(rightClickTime);
                                Annotation refA = tier.getAnnotationAtTime(rightClickTime);

                                if ((parentA != null) && (refA == null)) {
                                    newAnnoMI.setEnabled(true);
                                }
                            }
                        }
                    }
                    // paste items
                    if (AnnotationTransfer.validContentsOnClipboard()) {
                    	if (tier.getParentTier() == null) {
                    		pasteAnnoHereMI.setEnabled(true);
                    		pasteAnnoTreeHereMI.setEnabled(true);
                    	} else {
                            Annotation parentA = tier.getParentTier().getAnnotationAtTime(rightClickTime);
                            Annotation refA = tier.getAnnotationAtTime(rightClickTime);

                            if ((parentA != null) && (refA == null)) {
                            	pasteAnnoHereMI.setEnabled(true);
                        		pasteAnnoTreeHereMI.setEnabled(true);
                            }	
                    	}
                    }
                }
            } catch (Exception rex) {
                rex.printStackTrace();
            }
        }

    }

    /**
     * Based on which menu items are enabled and disabled, re-compose the menu
     * by inserting the enabled items, removing the disabled items, and
     * taking care there are not too many separators.
     * <p>
     * <code>contextSensitiveItems</code> contains the relevant items.
     * Separators are indicated with <code>null</code>. They are handled specially:
     * no two separators should appear consecutively, so they are removed and
     * re-created as necessary.
     */
	private void addAndRemovePopupItems() {
		// Remove and/or re-add items
        int pos = staticMenuItems; // the number of unchanged menu items at the top
        boolean prevIsASeparator = false;
        
        for (JMenuItem item : contextSensitiveItems) {
        	if (item == null) {
        		if (pos < popup.getComponentCount() &&
        				popup.getComponent(pos) instanceof JPopupMenu.Separator) {
        			// Separator is already present.
	        		if (prevIsASeparator) {
	        			// Two separators with no items in between; suppress one.
	        			popup.remove(pos);
	        		} else {
	        			// Use the existing separator
	        			pos++;
	        		}
        		} else {
	        		if (prevIsASeparator) {
	        			// The previous item is already a separator
	        		} else {
	        			// A new separator is needed
	        			popup.add(new JPopupMenu.Separator(), pos);
	        			pos++;
	        		}
        		}
    			prevIsASeparator = true;
        	} else {
        		if (item.isEnabled()/* || 
        				(item instanceof JCheckBoxMenuItem && ((JCheckBoxMenuItem)item).isSelected())*/) {
        			if (item.getParent() == null) {
        				popup.add(item, pos);
        			}
    				pos++;
        			prevIsASeparator = false;
        		} else {
        			if (item.getParent() != null) {
        				// Check if this item is where we expect; it always is.
        				if (popup.getComponent(pos) == item) {
            				popup.remove(pos);        				
        				} else { // should not happen
        					popup.remove(item);
        				}
        			}
        		}
        	}
        }
        // Remove a separator at the end
        if (prevIsASeparator) {
        	popup.remove(pos-1);
        }
        popup.validate();
	}

    /**
     * Set the inline edit box to invisible by canceling the edit.
     */
    private void dismissEditBox() {
        if (editBox.isVisible()) {
        	   if (deselectCommits) {
               editBox.commitEdit();
        	   } else {
               editBox.cancelEdit();
        	   }
        }
    }
    
    public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksList){
    	editBox.setKeyStrokesNotToBeConsumed(ksList);
    }
    
    /**
     * Display the edit box for the specified Tag2D
     *
     * @param tag2d the tag to edit
     */
    protected void showEditBoxForTag(Tag2D tag2d) {
        if (tag2d.getAnnotation() == null) {
            return;
        }

        editBox.setAnnotation(tag2d.getAnnotation(),
            forceOpenControlledVocabulary);

        int tierIndex = getTierIndexForAnnotation(tag2d.getAnnotation());
        int y = (rulerHeight + (tierIndex * pixelsForTierHeight)) -
            verticalScrollOffset;
        int x = xAt(tag2d.getBeginTime());

        /* //scroll to the beginning of the tag
           if (x < 0) {
               x = 0;
               setIntervalBeginTime(tag2d.getBeginTime());
           }
         */
        Font f = getFontForTier(tag2d.getTier2D().getTier());
        if (f != null) {
        	editBox.setFont(f);
        }
        editBox.configureEditor(JPanel.class, null,
            new Dimension(tag2d.getWidth() + 2, pixelsForTierHeight));
        updateEditBoxLocation(editBox, x, y);
        editBox.startEdit();

        forceOpenControlledVocabulary = false;
    }

    public void showEditBoxForAnnotation(Annotation ann) {
        if (ann == null) {
            return;
        }

        editBox.setAnnotation(ann);

        int tierIndex = getTierIndexForAnnotation(ann);
        int y = (rulerHeight + (tierIndex * pixelsForTierHeight)) -
            verticalScrollOffset;
        int x = xAt(ann.getBeginTimeBoundary());
        int w = xAt(ann.getEndTimeBoundary()) - x;

        /* //scroll to the beginning of the tag
           if (x < 0) {
               x = 0;
               setIntervalBeginTime(ann.getBeginTimeBoundary());
           }
         */
        // make sure the begin of the edit box is not outside the viewer's area
        if (x < 0) {
        	w = w - (-x) + 2;
        	x = 2;
        }
        //System.out.println("x: " + x + "w: " + w);
        Font f = getFontForTier(ann.getTier());
        if (f != null) {
        	editBox.setFont(f);
        }
        editBox.configureEditor(JPanel.class, null,
            new Dimension(w + 2, pixelsForTierHeight));
        updateEditBoxLocation(editBox, x, y);
        editBox.startEdit();
        
    }
    
    /*
     * Make sure the InlineEditBox remains inside the viewer's area as much as possible.
     */
    private void updateEditBoxLocation(InlineEditBox editBox, int x, int y) {
    	if (x + editBox.getWidth() > this.getWidth() - defBarWidth) {
    		x = Math.max(0, this.getWidth() - defBarWidth - editBox.getWidth());
    	}
    	if (editBox.getHeight() > getHeight() - defBarWidth) {
    		editBox.setSize(editBox.getWidth(), getHeight() - defBarWidth);
    	}
    	if (y + editBox.getHeight() > this.getHeight() - defBarWidth) {
    		y = Math.max(0, this.getHeight() - defBarWidth - editBox.getHeight());
    		//y = this.getHeight() - defBarWidth - editBox.getHeight();
    	}
    	editBox.setLocation(x, y);
    }

    // ***** editing and data changed methods **************************************//

    /**
     * Change a single annotation.
     *
     * @param tier the tier the annotation is part of
     * @param ann the annotation that has been changed
     */
    private void annotationChanged(TierImpl tier, Annotation ann) {
        Iterator<Tier2D> allTierIt = allTiers.iterator();
        dismissEditBox();
alltierloop: 
        while (allTierIt.hasNext()) {
            Tier2D t2d = allTierIt.next();

            if (t2d.getTier() == tier) {
                List<Tag2D> tagList = t2d.getTagsList();

                for (Tag2D tag2d : tagList) {
                    // check equality with ==
                    if (tag2d.getAnnotation() == ann) {
                        tag2d.setTruncatedValue(truncateString(ann.getValue(),
                                tag2d.getWidth(), metrics));
                        // check CV entry color
                        ControlledVocabulary cv = transcription.getControlledVocabulary(
                        		tier.getLinguisticType().getControlledVocabularyName());
                        if (cv != null) {
                        	String id = ann.getCVEntryId();
                        	CVEntry e = cv.getEntrybyId(id);
                        	if (e == null) {
                    			tag2d.setColor(null); // default colour                        		
                        	} else {
                    			tag2d.setColor(e.getPrefColor());
                    			
	                			// check a parent annotation for which this annotation could be a color marker
	                			TierImpl parTier = tier.getParentTier();
	                			if (parTier != null && tier == TierAssociation.findMarkerTierFor(transcription, parTier)) {
	           				parentTierLoop:
	                				for (Tier2D pt : allTiers) {
	                					if (pt.getTier() == parTier) {
	                						for (Tag2D pa2d : pt.getTagsList()) {
	                							if (pa2d.getBeginTime() == tag2d.getBeginTime() && 
	                									pa2d.getEndTime() == tag2d.getEndTime()) {
	                								pa2d.setColor(e.getPrefColor());
	                								break;
	                							}
	                							if (pa2d.getBeginTime() > tag2d.getBeginTime()) {
	                								break;
	                							}
	                						}
	                						break parentTierLoop;
	                					}
	                				}
	                			}
	                			break;
                    		}
                        }
                        
                        break alltierloop;
                    }
                }
            }
        }

        paintBuffer();
    }

    /**
     * Add a new Tier to the existing list of tiers.<br>
     * This method is private because it does not check whether the specified
     * Tier already is present in the transcription.
     *
     * @param tier the new Tier
     */
    private void tierAdded(TierImpl tier) {
        Tag2D tag2d;
        int xPos;
        int tagWidth;

        Tier2D tier2d = new Tier2D(tier);

        for (Annotation a : tier.getAnnotations()) {

            //System.out.println("Annotation: " + a);
            tag2d = new Tag2D(a);
            xPos = timeToPixels(a.getBeginTimeBoundary());
            tag2d.setX(xPos);
            tagWidth = timeToPixels(a.getEndTimeBoundary()) - xPos;
            tag2d.setWidth(tagWidth);
            tag2d.setTruncatedValue(truncateString(a.getValue(), tagWidth,
                    metrics));
            tier2d.addTag(tag2d);
        }

        allTiers.add(tier2d);
        tierYPositions = new int[allTiers.size()];

        //wait for a call to setVisibleTiers to show the tier
        //visibleTiers.add(tier2d);
        //paintBuffer();
        //System.out.println("new tier: " + tier2d.getName());
    }

    /**
     * Remove a Tier from the list of tiers.
     *
     * @param tier the Tier to remove
     */
    private void tierRemoved(TierImpl tier) {
        dismissEditBox();

        for (int i = 0; i < allTiers.size(); i++) {
            Tier2D tier2d = allTiers.get(i);

            if (tier2d.getTier() == tier) {
                allTiers.remove(i);
                prefTierFonts.remove(tier.getName());

                //wait for a call to setVisibleTiers
                if ((cursorTag2D != null) &&
                        (cursorTag2D.getTier2D() == tier2d)) {
                    cursorTag2D = null;
                    setActiveAnnotation(null);
                }

                break;
            }
        }

        //repaint();
    }

    /**
     * Check the name of the Tier2D object of the specified Tier.<br>
     * Other changes to the tier such as linguistic type or parent are
     * expected to become manifest through (a series of) changes in
     * annotations.
     *
     * @param tier the Tier that has been changed
     */
    private void tierChanged(TierImpl tier) {
        List<TierImpl> depTiers = new ArrayList<TierImpl>();

        if (tier != null) {
            depTiers = tier.getDependentTiers();
        }

        for (int i = 0; i < allTiers.size(); i++) {
            Tier2D tier2d = allTiers.get(i);

            if (tier2d.getTier() == tier) {
            	if (!tier2d.getName().equals(tier.getName())) {
            		Object opf = prefTierFonts.remove(tier2d.getName());
            		if (opf != null) {
            			prefTierFonts.put(tier.getName(), opf);
            		}
            	}
                tier2d.updateName();               
            }

            if ((tier2d.getTier() == tier) ||
                    depTiers.contains(tier2d.getTier())) {
                reextractTagsForTier(tier2d);
            }
        }
    }

    /**
     * Create a Tag2D for a new Annotation on the Tier2D of the specified TierImpl.<br>
     * If the Transcription is in Bulldozer mode, reextract the Tier2D.
     * Correction: transcription does not have a bulldozer mode (yet), just
     * reextract the tier...
     *
     * @param tiers the Tier the annotation belongs to
     */

    /*
       private void annotationAdded(TierImpl tier, Annotation annotation) {
           dismissEditBox();
    
               for (int i = 0; i < allTiers.size(); i++) {
                   Tier2D tier2d = (Tier2D) allTiers.get(i);
    
                   if (tier2d.getTier() == tier) {
                       reextractTagsForTier(tier2d);
    
                       paintBuffer();
    
                       break;
                   }
               }
           }
     */

    /**
     * Called when an annotation has been added before or after another
     * annotation, effecting more than one or two annotations.
     *
     * @param tiers a List of tiers that have been changed
     */
    private void annotationsAdded(List<TierImpl> tiers) {
        int mode = transcription.getTimeChangePropagationMode();

        if (mode != Transcription.SHIFT) {
            Tier2D tier2d;
            dismissEditBox();

            for (int i = 0; i < allTiers.size(); i++) {
                tier2d = allTiers.get(i);

                if (tiers.contains(tier2d.getTier())) {
                    reextractTagsForTier(tier2d);
                }
            }

            paintBuffer();
        } else {
            transcriptionChanged();
        }
    }

    /**
     * This method inserts a new annotation if there is an empty slot at the
     * specified point on the tier for the specified index.
     *
     * @param p the location of a doubleclick
     * @param tierIndex tier index
     */
    private void autoInsertAnnotation(Point p, int tierIndex) {
        if ((tierIndex < 0) || (tierIndex > (visibleTiers.size() - 1))) {
            return;
        }

        TierImpl child = visibleTiers.get(tierIndex).getTier();

        if (child == null) {
            return;
        }

        long clickTime = pixelToTime((int) p.getX());

        if (child.isTimeAlignable() || !child.hasParentTier()) {
            if ((clickTime >= getSelectionBeginTime()) &&
                    (clickTime <= getSelectionEndTime())) {
            	Command c = createNewAnnotationCommand();
                Object[] args = new Object[] {
                        new Long(getSelectionBeginTime()),
                        new Long(getSelectionEndTime())
                    };
                c.execute(child, args);
            }
        } else {
            TierImpl parent = child.getParentTier();

            Annotation ann = parent.getAnnotationAtTime(clickTime);

            if (ann != null) {
            	Command c = createNewAnnotationCommand();
                Object[] args = new Object[] {
                        new Long(clickTime),
                        new Long(clickTime)
                    };
                c.execute(child, args);
            }
        }
    }

	/**
	 * @return a command to create a new annotation, possibly recursively.
	 */
	private Command createNewAnnotationCommand() {
		Command c = null;
		Boolean val = Preferences.getBool("CreateDependingAnnotations", null);                
		if (val != null && val) {
			 c = ELANCommandFactory.createCommand(transcription,
	                 ELANCommandFactory.NEW_ANNOTATION_REC);  
		} else {
			 c = ELANCommandFactory.createCommand(transcription,
		             ELANCommandFactory.NEW_ANNOTATION); 
		}
		return c;
	}

    /**
     * Remove the Tag2D from the Tier2D corresponding to the respective
     * Annotation and TierImpl.
     */

    /*
       private void annotationRemoved(TierImpl tier, Annotation annotation) {
           Tier2D tier2d;
           Tag2D tag2d;
    
               for (int i = 0; i < allTiers.size(); i++) {
                   tier2d = (Tier2D) allTiers.get(i);
    
                   if (tier2d.getTier() == tier) {
                       Iterator tagIt = tier2d.getTags();
    
                       while (tagIt.hasNext()) {
                           tag2d = (Tag2D) tagIt.next();
    
                           if (tag2d.getAnnotation() == annotation) {
                               dismissEditBox();
                               tier2d.removeTag(tag2d);
    
                               return;
                           }
                       }
                   }
               }
           }
     */

    /**
     * This is called when an ACMEditEvent is received with operation
     * REMOVE_ANNOTATION and the transcription as invalidated object.<br>
     * It is undefined which tiers and annotations have been effected, so the
     * transcription is simply re-processed. Store state as much as possible.
     * Assume no tiers have been deleted or added.
     */
    private void annotationsRemoved() {
        transcriptionChanged();
    }

    /**
     * Called when begin and/or end time of an alignable annotation  has been
     * changed. In shift time propagation mode all tiers  are reextracted, in
     * other modes only the tiers that can be effected  are reextracted.
     *
     * @param tiers the vector of tiers that could be effected by the change
     */
    private void annotationTimeChanged(List<TierImpl> tiers) {
        int mode = transcription.getTimeChangePropagationMode();

        if (mode != Transcription.SHIFT) {
            Tier2D tier2d;

            for (int i = 0; i < allTiers.size(); i++) {
                tier2d = allTiers.get(i);

                if (tiers.contains(tier2d.getTier())) {
                    reextractTagsForTier(tier2d);
                }
            }

            paintBuffer();
        } else {
            transcriptionChanged();
        }
    }

    /**
     * This is called when an ACMEditEvent is received with an operation that
     * could influence all tiers in the transcription or with the
     * transcription as invalidated object.<br>
     * Examples are annotations_removed or annotation_added and
     * annotation_time_changed  in shift mode. The transcription is simply
     * re-processed. Store state as much as possible. Assume no tiers have
     * been deleted or added.
     */
    private void transcriptionChanged() {
        cursorTag2D = null;

        Tier2D tier2d;
        dismissEditBox();

        for (int i = 0; i < allTiers.size(); i++) {
            tier2d = allTiers.get(i);
            reextractTagsForTier(tier2d);
        }

        if (cursorTag2D != null) {
            cursorTierIndex = visibleTiers.indexOf(cursorTag2D.getTier2D());
        }

        paintBuffer();
    }

    /**
     * Tries to retrieve the default Font size for tooltips.<br>
     * This can then be used when the changing the Font the tooltip has to
     * use.
     *
     * @return the default font size or 12 when not found
     */
    private int getDefaultTooltipFontSize() {
        Object value = UIManager.getDefaults().get("ToolTip.font");

        if ((value != null) && value instanceof Font) {
            return ((Font) value).getSize();
        }

        return 12;
    }
    
    /**
     * Returns the user defined preferred font for the tier, if there is one.
     *  
     * @param tier the tier the font is to be used for
     * @return the preferred font, if there is one, otherwise the default font
     */
    private Font getFontForTier(Tier tier) {
    	if (tier != null) {
    		Font fo = (Font) prefTierFonts.get(tier.getName());
    		if (fo != null) {
    			return fo;
    		}
    	}
    	
    	return font;
    }

    /**
     * Override create tooltip to be able to set the (Unicode) Font for the
     * tip.
     *
     * @return DOCUMENT ME!
     */
    @Override
	public JToolTip createToolTip() {
        JToolTip tip = new JToolTip();
        tip.setFont(tooltipFont);

        //tip.setFont(tooltipFont != null ? tooltipFont : this.getFont().deriveFont((float)tooltipFontSize));
        tip.setComponent(this);

        return tip;
    }

    /**
     * Set a new Transcription for this viewer.<br>
     * We should receive a setVisibleTiers() and a setActiveTier() call after
     * this but are faking it now.
     *
     * @param transcription the new transcription.
     */
    public void setTranscription(Transcription transcription) {
        this.transcription = (TranscriptionImpl) transcription;
        hoverTag2D = null;
        hoverTierIndex = 0;
        cursorTag2D = null;
        cursorTierIndex = 0;

        //
        List<TierImpl> oldVisibles = new ArrayList<TierImpl>(visibleTiers.size());

        for (int i = 0; i < visibleTiers.size(); i++) {
            Tier2D tier2d = visibleTiers.get(i);
            oldVisibles.add(tier2d.getTier());
        }

        String activeTierName = "";

        for (int i = 0; i < allTiers.size(); i++) {
            Tier2D tier2d = allTiers.get(i);

            if (tier2d.isActive()) {
                activeTierName = tier2d.getName();

                break;
            }
        }

        //
        initTiers();

        //
        for (int i = 0; i < allTiers.size(); i++) {
            Tier2D tier2d = allTiers.get(i);

            if (tier2d.getName().equals(activeTierName)) {
                tier2d.setActive(true);

                break;
            }
        }

        setVisibleTiers(oldVisibles);
    }

    //***** end of initial editing and data changed methods **************************************//
    //**************************************************************************************//

    /* implement ControllerListener */
    /* (non-Javadoc)
     * @see mpi.eudico.client.annotator.ControllerListener#controllerUpdate(mpi.eudico.client.annotator.ControllerEvent)
     * 
     * Runs on a separate thread.
     */
    @Override
	public synchronized void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent || event instanceof StopEvent) {
            crossHairTime = getMediaTime();

            /*
               //System.out.println("TimeLineViewer time: " + crossHairTime);
               if (crossHairTime < intervalBeginTime || crossHairTime > intervalEndTime) {
                   if (playerIsPlaying()) {
                       // we might be in a selection outside the new interval
                       long newBeginTime = intervalEndTime;
                       long newEndTime = newBeginTime + (imageWidth * msPerPixel);
                       if (crossHairTime > newEndTime) {
                           while ((newEndTime += imageWidth + msPerPixel) < crossHairTime) {
                               newBeginTime += imageWidth * msPerPixel;
                           }
                       } else if (crossHairTime < newBeginTime) {
                           while ((newEndTime -= imageWidth * msPerPixel) > crossHairTime) {
                               newBeginTime -= imageWidth * msPerPixel;
                           }
                       }
                       setIntervalBeginTime(newBeginTime);
                   } else {
                       setIntervalBeginTime(crossHairTime);
                   }
                   crossHairPos = xAt(crossHairTime);
             */
            /*// replaced 07 apr 2005 better positioning of search result annotations
               if ((crossHairTime == intervalEndTime) && !playerIsPlaying()) {
                   recalculateInterval(crossHairTime);
               } else if ((crossHairTime < intervalBeginTime) ||
                       (crossHairTime > intervalEndTime)) {
                   //dismissEditBox();
                   recalculateInterval(crossHairTime);
               } */
            if (!playerIsPlaying()) {
                if (scroller == null) {
                    recalculateInterval(crossHairTime);
                    crossHairPos = xAt(crossHairTime);
                    repaint(); // safe outside of the EDT: a repaint only gets queued which seems safe.
                } else {
                    recalculateInterval(crossHairTime);
                }
            } else {
            	if (tickerMode) {
                    long intervalMidTime = (intervalBeginTime + intervalEndTime) / 2;

                    if (crossHairTime > (intervalMidTime + (1 * msPerPixel))) {
                        setIntervalBeginTime(intervalBeginTime +
                            (crossHairTime - intervalMidTime));
                    } else if (crossHairTime < intervalMidTime) {
                    	if (crossHairTime < intervalBeginTime) {
                    		setIntervalBeginTime(Math.max(0, crossHairTime - 
                    				(intervalMidTime - intervalBeginTime)));
                    	} else {
	                        int oldPos = crossHairPos;
	                        crossHairPos = xAt(crossHairTime);
	
	                        if (crossHairPos >= oldPos) {
	                            repaint(oldPos - 2, 0, crossHairPos - oldPos + 4,
	                                getHeight());
	                        } else {
	                            repaint(crossHairPos - 2, 0, oldPos - crossHairPos + 4,
	                                getHeight());
	                        }
                    	}
                    } else {
                        repaint();
                    }
            	} else if ((crossHairTime < intervalBeginTime) ||
                        (crossHairTime > intervalEndTime)) {
                    //dismissEditBox();
                    recalculateInterval(crossHairTime);
                } else {
                    // repaint a part of the viewer
                    int oldPos = crossHairPos;
                    crossHairPos = xAt(crossHairTime);

                    int newPos = crossHairPos;

                    if (newPos >= oldPos) {
                        repaint(oldPos - 2, 0, newPos - oldPos + 4, getHeight());

                        //repaint();
                    } else {
                        repaint(newPos - 2, 0, oldPos - newPos + 4, getHeight());

                        //repaint();
                    }
                }
            }
            if (event instanceof StopEvent) {
            	paintBuffer();
            }
        } else if (event instanceof StartEvent) {
        	if (!useBufferedImage) {
        		paintBuffer();	
        	}     	
        }
    }

    /**
     * Update method from ActiveAnnotationUser.<br>
     * The application wide active annotation corresponds to the cursorTag2D
     * in this viewer. If the Tier that the cursorTag2D belongs to is
     * invisible the cursorTag2D is <i>not</i> set to <code>null</code>.
     */
    @Override
	public void updateActiveAnnotation() {
        dismissEditBox();

        Annotation anno = getActiveAnnotation();

        if (anno != null) {
            //look for the annotation
            Tier2D tier2d;
            Tag2D tag2d;
            Iterator<Tier2D> allIter = allTiers.iterator();

            while (allIter.hasNext()) {
                tier2d = allIter.next();

                if (tier2d.getTier() == anno.getTier()) {
                    Iterator<Tag2D> tagIter = tier2d.getTags();

                    while (tagIter.hasNext()) {
                        tag2d = tagIter.next();

                        if (tag2d.getAnnotation() == anno) {
                            cursorTag2D = tag2d;
                            cursorTierIndex = visibleTiers.indexOf(cursorTag2D.getTier2D());
                            ensureVerticalVisibilityOfActiveAnnotation();

                            break;
                        }
                    }
                }
            }
            
            long beginTime = anno.getBeginTimeBoundary();
            long endTime = anno.getEndTimeBoundary();

            // update interval //
            if (!playerIsPlaying()) {
            	long newBeginTime = intervalBeginTime;
                long newEndTime = intervalEndTime;
                
                boolean updateInterval = false;
                
                if(centerAnnotation){
                	// always center the active annotation
                	long intMid = (long) ((imageWidth * msPerPixel) / 2);
                    long annMid = (endTime + beginTime) / 2;
                    newBeginTime = annMid - intMid;
                    newEndTime = annMid + intMid;                    
                    if (newBeginTime < 0) {
                        newBeginTime = 0;
                        newEndTime = (long) (imageWidth * msPerPixel);
                    }                    
                    updateInterval = true;
                } else if ((beginTime < intervalBeginTime) ||
                        (endTime > intervalEndTime)) {
                	// if the next annotation is not seen in the current interval, 
                	// update the interval
                        
                    if ((beginTime < newBeginTime) &&
                            (beginTime > (SCROLL_OFFSET * msPerPixel))) {
                        newBeginTime = beginTime -
                            (int) (SCROLL_OFFSET * msPerPixel);
                        newEndTime = newBeginTime + (int) (imageWidth * msPerPixel);
                    } else if (endTime > newEndTime) {
                        newEndTime = endTime +
                            (int) (SCROLL_OFFSET * msPerPixel);  
                        newBeginTime = newEndTime - (int) (imageWidth * msPerPixel);

                        if ((newBeginTime > beginTime) &&
                                (beginTime > (SCROLL_OFFSET * msPerPixel))) {
                            newBeginTime = beginTime -
                                (int) (SCROLL_OFFSET * msPerPixel);
                            newEndTime = newBeginTime +
                                (int) (imageWidth * msPerPixel);
                        } else if (newBeginTime > beginTime) {
                            newBeginTime = 0;
                            newEndTime = (long) (imageWidth * msPerPixel);
                        }                        
                        updateInterval = true;
                    }
                }                 
                if (timeScaleConnected && updateInterval) {                    	
                    setGlobalTimeScaleIntervalBeginTime(newBeginTime);
                    setGlobalTimeScaleIntervalEndTime(newEndTime);
                } else {
                    setLocalTimeScaleIntervalBeginTime(newBeginTime);
                }
            }

            // end update interval //
        } else {
            cursorTag2D = null;
        }
        repaint();
    }

    /**
     * Implements ACMEditListener.<br>
     * The ACMEditEvent that is received contains information about the kind
     * of modification and the objects effected by that modification.
     *
     * @param e the event object
     *
     * @see ACMEditEvent
     */
    @Override
	public void ACMEdited(ACMEditEvent e) {
        //System.out.println("ACMEdited:: operation: " + e.getOperation() + ", invalidated: " + e.getInvalidatedObject());
        //System.out.println("\tmodification: " + e.getModification() + ", source: " + e.getSource());
        switch (e.getOperation()) {
        case ACMEditEvent.ADD_TIER:

            if (e.getModification() instanceof TierImpl) {
                tierAdded((TierImpl) e.getModification());

                if (multiTierControlPanel != null) {
                    multiTierControlPanel.tierAdded((TierImpl) e.getModification());
                }
            }
            break;

        case ACMEditEvent.REMOVE_TIER:

            if (e.getModification() instanceof TierImpl) {
                tierRemoved((TierImpl) e.getModification());

                if (multiTierControlPanel != null) {
                    multiTierControlPanel.tierRemoved((TierImpl) e.getModification());
                }
            }

            break;

        case ACMEditEvent.CHANGE_TIER:

            if (e.getInvalidatedObject() instanceof TierImpl) {
                tierChanged((TierImpl) e.getInvalidatedObject());

                if (multiTierControlPanel != null) {
                    multiTierControlPanel.tierChanged((TierImpl) e.getInvalidatedObject());
                }
            }

            break;

        // if i'm right for the next three operations the event's
        // invalidated Object should be a tier
        // and the modification object the new annotation...
        case ACMEditEvent.ADD_ANNOTATION_HERE:

            if (e.getInvalidatedObject() instanceof TierImpl &&
                    e.getModification() instanceof Annotation) {
                //annotationAdded((TierImpl)e.getInvalidatedObject(), (Annotation)e.getModification());
                // to accomodate right updates in bulldozer mode stupidly reextract all dependant tiers
                TierImpl invTier = (TierImpl) e.getInvalidatedObject();
                List<TierImpl> depTiers = invTier.getDependentTiers();

                if (depTiers == null) {
                    depTiers = new ArrayList<TierImpl>();
                }

                depTiers.add(0, invTier);
                annotationsAdded(depTiers);
                // does this lead to exceptions in TextViewer??
                // this cannot be done in the new annotation command, then the inline edit box doesn't popup
                //setActiveAnnotation((Annotation) e.getModification());
                delayedAnnotationActivation = true;
                lastCreatedAnnotation = (Annotation) e.getModification();
                showEditBoxForAnnotation((Annotation) e.getModification());

                if (editBox.isVisible()) {
                    editBox.requestFocus();
                }
                
                if (multiTierControlPanel != null) {
                    multiTierControlPanel.annotationsChanged();
                }
            }

            break;

        case ACMEditEvent.ADD_ANNOTATION_BEFORE:

        // fall through
        //break;
        case ACMEditEvent.ADD_ANNOTATION_AFTER:

            if (e.getInvalidatedObject() instanceof TierImpl &&
                    e.getModification() instanceof Annotation) {
                /*
                   TierImpl invTier = (TierImpl) e.getInvalidatedObject();
                   List depTiers = invTier.getDependentTiers(userIdentity);
                
                                   if (depTiers == null) {
                                       depTiers = new List();
                                   }
                
                                   depTiers.add(0, invTier);
                                   annotationsAdded(depTiers);
                 */

                //setActiveAnnotation((Annotation) e.getModification());
                delayedAnnotationActivation = true;
                lastCreatedAnnotation = (Annotation) e.getModification();
                // jul 2004: redo all; we can not rely on the fact that only dependent
                // tiers will be effected by this operation...
                // (problem: unaligned annotations on time-subdivision tiers)
                List<TierImpl> tiers = transcription.getTiers();

                annotationsAdded(tiers);
                showEditBoxForAnnotation((Annotation) e.getModification());

                if (editBox.isVisible()) {
                    editBox.requestFocus();
                }
                
                if (multiTierControlPanel != null) {
                    multiTierControlPanel.annotationsChanged();
                }

                //maybe this could be more finegrained by re-extracting only all
                // RefAnnotations referring to the parent of the modified annotation...
            }

            break;

        case ACMEditEvent.CHANGE_ANNOTATIONS:

            if (e.getInvalidatedObject() instanceof Transcription) {
                transcriptionChanged();
                
                if (multiTierControlPanel != null) {
                    multiTierControlPanel.annotationsChanged();
                }
            }

            break;

        case ACMEditEvent.REMOVE_ANNOTATION:

            if (e.getInvalidatedObject() instanceof Transcription) {
                //System.out.println("Invalidated object: " + e.getInvalidatedObject());
                annotationsRemoved();
                
                if (multiTierControlPanel != null) {
                    multiTierControlPanel.annotationsChanged();
                }
            }

            break;

        case ACMEditEvent.CHANGE_ANNOTATION_TIME:

            if (e.getInvalidatedObject() instanceof AlignableAnnotation) {
                TierImpl invTier = (TierImpl) ((AlignableAnnotation) e.getInvalidatedObject()).getTier();
                List<TierImpl> depTiers = invTier.getDependentTiers();

                if (depTiers == null) {
                    depTiers = new ArrayList<TierImpl>();
                }

                depTiers.add(0, invTier);
                annotationTimeChanged(depTiers);
            }

            break;

        case ACMEditEvent.CHANGE_ANNOTATION_VALUE:

            if (e.getSource() instanceof Annotation) {
                Annotation a = (Annotation) e.getSource();

                if (a.getTier() instanceof TierImpl) {
                    annotationChanged((TierImpl) a.getTier(), a);

                    // The TimeLineViewer has a special responsibility in showing an
                    // edit box when an annotation has been created.
                    // When in an undo action a deleted annotation is recreated 
                    // and the value restored in a single pass, no component seems
                    // to actually have keyboard focus: no keyboard shortcur works
                    // (can't explain this)
                    // the following code ensures that after finishing the undo action
                    // a component receives the keyboard focus in a separate thread

                    /*
                       SwingUtilities.invokeLater(new Runnable(){
                           public void run() {
                               TimeLineViewer.this.getParent().requestFocus();
                           }
                       });
                     */
                }
            }

            break;

        case ACMEditEvent.REMOVE_COMMENT:
        case ACMEditEvent.ADD_COMMENT:
        case ACMEditEvent.CHANGE_COMMENT:
        	// Be simplistic for now and just redraw everything
        	paintBuffer();
        	break;

        default:
            break;
        }
        
        // simply remove all selected annotations
        selectedAnnotations.clear();
    }

    //**************************************************************************************//

    /* implement SelectionUser */
    /* (non-Javadoc)
     * @see mpi.eudico.client.annotator.SelectionUser#updateSelection()
     */
    @Override
	public void updateSelection() {
        //selectionBeginPos = (int) (getSelectionBeginTime() / msPerPixel);
        //selectionEndPos = (int) (getSelectionEndTime() / msPerPixel);
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());
        paintBuffer();

        //repaint();
    }

    //**************************************************************************************//

    /* implement ElanLocaleListener */

    /**
     * Update locale sensitive UI elements.
     */
    @Override
	public void updateLocale() {
        if (popup != null) {
            zoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom"));
            timeScaleConMI.setText(ElanLocale.getString(
                    "TimeScaleBasedViewer.Connected"));
            activeAnnStrokeBoldMI.setText(ElanLocale.getString(
                    "TimeLineViewer.ActiveAnnotationBold"));
            reducedTierHeightMI.setText(ElanLocale.getString("TimeLineViewer.ReducedTierHeight"));
            hScrollBarVisMI.setText(ElanLocale.getString("TimeLineViewer.Menu.HScrollBar"));
            fontMenu.setText(ElanLocale.getString("Menu.View.FontSize"));
            newAnnoMI.setText(ElanLocale.getString("Menu.Annotation.NewAnnotation"));
            newAnnoBeforeMI.setText(ElanLocale.getString(
                    "Menu.Annotation.NewAnnotationBefore"));
            newAnnoAfterMI.setText(ElanLocale.getString(
                    "Menu.Annotation.NewAnnotationAfter"));
            modifyAnnoMI.setText(ElanLocale.getString(
                    "Menu.Annotation.ModifyAnnotation"));
            //modifyAnnoDCMI.setText(ElanLocale.getString("Menu.Annotation.ModifyAnnotationDatCat"));
            showInBrowserMI.setText(ElanLocale.getString("Menu.Annotation.ShowInBrowser"));
            copyAnnoMI.setText(ElanLocale.getString(
    			"Menu.Annotation.CopyAnnotation"));
            copyAnnoTreeMI.setText(ElanLocale.getString(
    			"Menu.Annotation.CopyAnnotationTree"));
            pasteAnnoHereMI.setText(ElanLocale.getString(
    			"Menu.Annotation.PasteAnnotationHere"));
            pasteAnnoTreeHereMI.setText(ElanLocale.getString(
    			"Menu.Annotation.PasteAnnotationTreeHere"));

            //modifyAnnoTimeMI.setText(ElanLocale.getString(
            //       "Menu.Annotation.ModifyAnnotationTime"));
            deleteAnnoMI.setText(ElanLocale.getString(
                    "Menu.Annotation.DeleteAnnotation"));
            shiftActiveAnnMI.setText(ElanLocale.getString("Menu.Annotation.Shift") + " "
            		+ ElanLocale.getString("Menu.Annotation.ShiftActiveAnnotation"));
        }

        if (editBox != null) {
            editBox.updateLocale();
        }
    }

    /**
     * Updates the font that is used in the visualization of the annotations<br>
     * Does not change the font in the time ruler.
     *
     * @param f the new Font
     */
    public void updateFont(Font f) {
        int oldSize = font.getSize();
        font = f;
        setFont(font);
        tooltipFont = font.deriveFont((float) tooltipFontSize);
        metrics = getFontMetrics(font);
        
        Iterator<String> keyIt = prefTierFonts.keySet().iterator();
        String key = null;
        Font prFont = null;
        while (keyIt.hasNext()) {
        	key = keyIt.next();
        	prFont = (Font) prefTierFonts.get(key);
        	if (prFont != null) {
        		prefTierFonts.put(key, new Font(prFont.getName(), Font.PLAIN, 
        				font.getSize()));
        	}
        }
        
        recalculateTagSizes();
        //pixelsForTierHeight = font.getSize() * 3;
        //pixelsForTierHeight = font.getSize() + 24;
        pixelsForTierHeight = calcTierHeight();
        
        //pixelsForTierHeight = pixelsForTierHeight / 2 + 4;
        if (oldSize != f.getSize()) {
            if (multiTierControlPanel != null) {
            	multiTierControlPanel.setFont(font);
            }
            notifyMultiTierControlPanel();
            paintBuffer();
            scrollBar.setBlockIncrement(pixelsForTierHeight);
            updateScrollBar();
        } else {
            paintBuffer();
        }
    }

    /**
     * Sets the font size.
     *
     * @param fontSize the new font size
     */
    public void setFontSize(int fontSize) {
        updateFont(getFont().deriveFont((float) fontSize));

        if (popup != null) {
            Enumeration<AbstractButton> en = fontSizeBG.getElements();
            JMenuItem item;
            String value;

            while (en.hasMoreElements()) {
                item = (JMenuItem) en.nextElement();
                value = item.getText();

                try {
                    int v = Integer.parseInt(value);

                    if (v == fontSize) {
                        item.setSelected(true);

                        //updateFont(getFont().deriveFont((float) fontSize));
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    //// do nothing
                }
            }
        }
    }

    /**
     * Returns the current font size.
     *
     * @return the current font size
     */
    public int getFontSize() {
        return font.getSize();
    }

    /**
     * Handle scrolling of the viewer image.
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
        int value = e.getValue();

        if (e.getSource() == scrollBar) {	        
	        if (editBox.isVisible()) {
	            Point p = editBox.getLocation();
	            p.y += (verticalScrollOffset - value);
	            editBox.setLocation(p);
	        }
	
	        setVerticalScrollOffset(value);
	        notifyMultiTierControlPanel();
        } else if (e.getSource() == hScrollBar) {
            	// editbox is taken care of in setIntervalBeginTime
	        setIntervalBeginTime(pixelToTime(value));
	        setHorizontalScrollOffset(value);
        }
    }
    
	/**
	 * Zoom slider listener.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == zoomSlider) {
			//if (!zoomSlider.getValueIsAdjusting()) {
				int zoomValue = zoomSlider.getValue();
				if (zoomValue > 100) {
					float factor = (zoomValue - 100) / (float) (zoomSlider.getMaximum() - 100);
					zoomValue = 100 + (int) (factor * 900);
				}
				float nmspp = ((100f / zoomValue) * DEFAULT_MS_PER_PIXEL);
				if (nmspp != msPerPixel) {
					setMsPerPixel(nmspp);
				}
				zoomSlider.setToolTipText(String.valueOf(zoomValue));
			//}
		}		
	}

    //**************************************************************************************//

    /* implement MultiTierViewer */
    @Override
    public void setVisibleTiers(List<TierImpl> tiers) {
        //store some old values
        dismissEditBox();

        int oldNum = visibleTiers.size();

        synchronized (tierLock) {
            visibleTiers.clear();

            Tier2D t2d;
            Tier tier;

            Iterator<TierImpl> tierIter = tiers.iterator();

            while (tierIter.hasNext()) {
                tier = tierIter.next();

                Iterator<Tier2D> it = allTiers.iterator();

                while (it.hasNext()) {
                    t2d = it.next();

                    if (t2d.getTier() == tier) {
                        visibleTiers.add(t2d);

                        break;
                    }
                }
            }
        }

        if (cursorTag2D != null) {
            cursorTierIndex = visibleTiers.indexOf(cursorTag2D.getTier2D());
        }

        notifyMultiTierControlPanel();
        paintBuffer();

        if (oldNum != visibleTiers.size()) {
            updateScrollBar();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tier DOCUMENT ME!
     */
    @Override
	public void setActiveTier(Tier tier) {
        Iterator<Tier2D> it = allTiers.iterator(); //visibleTiers??
        Tier2D t2d;

        while (it.hasNext()) {
            t2d = it.next();

            if (t2d.getTier() == tier) {
                t2d.setActive(true);
                
                int tierIndex = visibleTiers.indexOf(t2d);
                int cy = tierIndex * pixelsForTierHeight;
                if (cy < verticalScrollOffset) {
                    scrollBar.setValue(cy);
                } else if (((cy + pixelsForTierHeight) - verticalScrollOffset) > (getHeight() -
                        rulerHeight - defBarWidth)) {
                    scrollBar.setValue((cy + pixelsForTierHeight + rulerHeight + defBarWidth) -
                        getHeight());
                }
            } else {
                t2d.setActive(false);
            }
        }

        paintBuffer();
    }

    /**
     * DOCUMENT ME!
     *
     * @param controller DOCUMENT ME!
     */
    @Override
	public void setMultiTierControlPanel(MultiTierControlPanel controller) {
        multiTierControlPanel = controller;

        //paintBuffer();
        notifyMultiTierControlPanel();
    }

    //*************************************************************************************//

    /* implement ComponentListener */
    /*
     * After a resize this is (currently) the order in which methods are called
     * componentResized
     * doLayout()
	 * layout()
	 * paint()
	 * paintComponent()
	 * paint()
	 * paintComponent()
     * Calculate a new BufferedImage taken the new size of the Component
     */
    @Override
	public void componentResized(ComponentEvent e) {
    	// in the case useBuffer == false calc the image width

    	if (!useBufferedImage) {
    		imageWidth = getWidth() - defBarWidth;
    		imageHeight = getHeight();
    		if(hScrollBarVisible) {
    			imageHeight -= defBarWidth;
    		}
            intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);
            if (timeScaleConnected) {
                setGlobalTimeScaleIntervalEndTime(intervalEndTime);
            }
    	}
        paintBuffer();
        if (hScrollBarVisible) {
        	if (getWidth() > 2 * ZOOMSLIDER_WIDTH) {
        		if (!zoomSliderPanel.isVisible()) {
        			zoomSliderPanel.setVisible(true);
        		}
	            hScrollBar.setBounds(0, getHeight() - defBarWidth, getWidth() - defBarWidth - ZOOMSLIDER_WIDTH, 
	            		defBarWidth);
	            //hScrollBar.revalidate();
	            updateHorScrollBar();
	            scrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
	                    getHeight() - defBarWidth);
		        zoomSliderPanel.setBounds(hScrollBar.getWidth(), getHeight() - defBarWidth, ZOOMSLIDER_WIDTH, defBarWidth);
		        zoomSlider.setBounds(0, 0, ZOOMSLIDER_WIDTH, defBarWidth);
		        corner.setBounds(getWidth() - defBarWidth, getHeight() - defBarWidth, 
		        		defBarWidth, defBarWidth);
        	} else {
        		zoomSliderPanel.setVisible(false);
	            hScrollBar.setBounds(0, getHeight() - defBarWidth, getWidth() - defBarWidth, defBarWidth);
	            //hScrollBar.revalidate();
	            updateHorScrollBar();
	            scrollBar.setBounds(getWidth() - defBarWidth - 1, 0, defBarWidth,
	                    getHeight() - defBarWidth);
		        corner.setBounds(getWidth() - defBarWidth, getHeight() - defBarWidth, 
		        		defBarWidth, defBarWidth);
        	}
        } else {
            scrollBar.setBounds(getWidth() - defBarWidth - 1, 0, defBarWidth,
            getHeight());
	        corner.setBounds(getWidth() - defBarWidth, getHeight() - defBarWidth, 
	        		0, 0);
        }
        //scrollBar.revalidate();
        updateScrollBar();
    	
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void componentMoved(ComponentEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void componentShown(ComponentEvent e) {
        componentResized(e);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void componentHidden(ComponentEvent e) {
    }

    //***********************************************************************************

    /* implement MouseListener and MouseMotionListener
       /*
     * A mouse click in the SignalViewer updates the media time
     * to the time corresponding to the x-position.
     */
    @Override
	public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
//        	checkForSplitAnnotation();
//        	Point p = e.getPoint();
//        	splitTime =  (int) (p.x * msPerPixel);
            return;
        }

        Annotation annotation = null; // new code by AR
        boolean shouldShowEditBox = false;

        // grab keyboard focus
        requestFocus();

        Point pp = e.getPoint();

        if ((e.getClickCount() == 1) && e.isAltDown()) {
        	Point inverse = new Point(pp);
            dismissEditBox();
            inverse.x += timeToPixels(intervalBeginTime);
            inverse.y += verticalScrollOffset;
            
            Tag2D selTag = getTagAt(inverse, getTierIndexForPoint(inverse));
            if (selTag != null) {
            	if (selectedAnnotations.contains(selTag)) {
            		selectedAnnotations.remove(selTag);
            	} else {
            		selectedAnnotations.add(selTag);
            	}
            } else {
            	selectedAnnotations.clear();
            }
            if (selectedAnnotations.size() != 0) {
            	long bt = Long.MAX_VALUE;
            	long et = Long.MIN_VALUE;
            	
            	for (Tag2D iterT2d : selectedAnnotations) {
            		if (iterT2d.getBeginTime() < bt) {
            			bt = iterT2d.getBeginTime();
            		}
            		if (iterT2d.getEndTime() > et) {
            			et = iterT2d.getEndTime();
            		}
            	}
            	if (bt < et) {
            		setSelection(bt, et);
            	}
            } else {
            	setSelection(0, 0);
            }
            repaint();
        } else 
        if ((e.getClickCount() == 1) && e.isShiftDown()) {
            // change the selection interval
            if (getSelectionBeginTime() != getSelectionEndTime()) {
                long clickTime = timeAt(pp.x);

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
            	long clickTime = timeAt(pp.x);
            	long medTime = getMediaTime();
            	if (clickTime > medTime) {
            		setSelection(medTime, clickTime);
            	} else if (clickTime < medTime) {
            		setSelection(clickTime, medTime);
            	}
            }
        } else {
            Point inverse = new Point(pp);
            dismissEditBox();

            if (timeRulerVisible && (pp.y < rulerHeight)) {
                //cursorTag2D = null;
            } else {
                //compensate for the intervalBeginTime
                inverse.x += timeToPixels(intervalBeginTime);
                inverse.y += verticalScrollOffset;
                cursorTierIndex = getTierIndexForPoint(inverse);
                cursorTag2D = getTagAt(inverse, cursorTierIndex);

                if (cursorTag2D != null) {
                    annotation = cursorTag2D.getAnnotation();
                    setActiveAnnotation(annotation);
                } else {
                    setActiveAnnotation(null);

                    //setMediaTime(timeAt(pp.x));
                }

                if ((e.getClickCount() == 2) && (cursorTag2D != null)) {
                    //showEditBoxForTag(cursorTag2D);
                    shouldShowEditBox = true;

                    if (e.isShiftDown()) {
                        forceOpenControlledVocabulary = true;
                    } else {
                        forceOpenControlledVocabulary = false;
                    }
                } else if ((e.getClickCount() >= 2) && (cursorTag2D == null)) {
                    autoInsertAnnotation(inverse, cursorTierIndex);
                }
            }

            //repaint();
            // disabled by AR
            //			setMediaTime(timeAt(pp.x));
            // new code by AR, takes care of setting selection in every mode to boundaries of active annotation
            if ((annotation == null) && !e.isAltDown()) {
                setMediaTime(timeAt(pp.x));
//            	System.out.println("isRightMouseButton: " + SwingUtilities.isRightMouseButton(e));
//            	System.out.println("isMeta: " + (e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown()));
//            	System.out.println("Pop up: " + e.isPopupTrigger());
                if ( !SwingUtilities.isRightMouseButton(e) && !(e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown()) 
                        && !e.isPopupTrigger()) {
//                	System.out.println("Clear");
//                	System.out.println("isRightMouseButton: " + SwingUtilities.isRightMouseButton(e));
//                	System.out.println("isMeta: " + (e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown()));
//                	System.out.println("Pop up: " + e.isPopupTrigger());
                	selectedAnnotations.clear();               	
                }
            }

            if (shouldShowEditBox && (cursorTag2D != null)) {
                showEditBoxForTag(cursorTag2D);
            }
        }
        
        
        // Previously in this method the handling of:
	    // a single click, and isAltdown() or isShiftdown() 
	    // or two or more clicks. Now check if we need to clear 
        // a selection.
	    // Skip clearing the selection if shift or alt is down. Clear on a 
	    // single click only.
        // TODO Note: the current implementation interferes with the creation of an annotation
        // by double clicking inside the selection 
        /*
	    if (clearSelOnSingleClick && e.getClickCount() == 1
		    && !e.isShiftDown() && !e.isAltDown() && annotation == null && 
		    !getViewerManager().getMediaPlayerController().getSelectionMode()) {
	    	setSelection(0, 0);
	    }
		*/
    }

    /*
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
	public void mousePressed(MouseEvent e) {
        Point pp = e.getPoint();

        // HS nov 04: e.isPopupTrigger always returns false on my PC...
        // HS jun 2006: change to discriminate Ctrl-click and Command-click on Mac
        // isRightMouseButton() returns true for both, isMetaDown() returns true for Command only 
        // but on Windows isMetaDown() returns true when the right mouse button (BUTTON3) has been pressed
        if ((SwingUtilities.isRightMouseButton(e) && (e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown())) 
                || e.isPopupTrigger()) {
            if (popup == null) {
                createPopupMenu();
            }

            updatePopup(pp);

            if (e.isShiftDown()) {
                forceOpenControlledVocabulary = true;
            } else {
                forceOpenControlledVocabulary = false;
            }

            if ((popup.getWidth() == 0) || (popup.getHeight() == 0)) {
                popup.show(this, pp.x, pp.y);
            } else {
                popup.show(this, pp.x, pp.y);
                SwingUtilities.convertPointToScreen(pp, this);

                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();                
                Window w = SwingUtilities.windowForComponent(this);
                Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(w.getGraphicsConfiguration());

                if ((pp.x + popup.getWidth()) > d.width - insets.right) {
                    pp.x -= (popup.getWidth() + insets.right);
                }

                //this does now account for a desktop taskbar
                if ((pp.y + popup.getHeight()) > d.height - insets.bottom) {
                	// HS Feb 2014 changed to move the popup up only as much as it falls off the screen
                	int diff = (d.height - insets.bottom) - (pp.y + popup.getHeight());
                    pp.y -= diff;
                    if (pp.y < insets.top) {// taskbar
                    	pp.y = insets.top;
                    }
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

        if (e.isAltDown() && timeRulerVisible &&
                (dragStartPoint.y < rulerHeight)) {
            panMode = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            //dismissEditBox();
        } else if (e.isAltDown()) {
            // drag-edit an annotation
            if ((cursorTag2D != null) && (dragEditTag2D == null)) {
                if (getHoverTag(new Point(e.getPoint())) == cursorTag2D) {
                    dragEditTag2D = new DragTag2D(cursorTag2D.getAnnotation());
                    dragEditTag2D.copyFrom(cursorTag2D);
                }
            }
            // drag an annotation that is not active
            if (dragEditTag2D == null) {
            	Point inverse = new Point(pp);
                dismissEditBox();
                //inverse.x += timeToPixels(intervalBeginTime);
                inverse.y += verticalScrollOffset;
            	Tag2D hover = getHoverTag(e.getPoint());
            	if (hover != null && (hover.getAnnotation() instanceof AlignableAnnotation)) {
//            		if (hover.getAnnotation() != getActiveAnnotation()) {
//            			setActiveAnnotation(hover.getAnnotation());
//            		}
            		
                    dragEditTag2D = new DragTag2D(hover.getAnnotation());
                    dragEditTag2D.copyFrom(hover);
            		cursorTag2D = hover;
            		cursorTierIndex = getTierIndexForPoint(inverse);
            	}
            }

            if (dragEditTag2D != null) {
                // before changing anything, create a copy
                DragTag2D copy = new DragTag2D(dragEditTag2D.getAnnotation());
                copy.copyFrom(dragEditTag2D);
                //selectedAnnotations.remove(dragEditTag2D);
                dragEditTag2D = copy;
                dragEditing = true;

                // find the parent annotation's boundaries, if this one is not on 
                // a root tier
                if (((TierImpl) dragEditTag2D.getAnnotation().getTier()).hasParentTier()) {
                    //TierImpl pt = (TierImpl) ((TierImpl)dragEditTag2D.getAnnotation().getTier()).getParentTier();
                    //AlignableAnnotation pa = (AlignableAnnotation) pt.getAnnotationAtTime(
                    //	dragEditTag2D.getAnnotation().getBeginTimeBoundary());
                    AlignableAnnotation pa = (AlignableAnnotation) dragEditTag2D.getAnnotation()
                                                                                .getParentAnnotation();

                    if (pa != null) {
                        dragParentBegin = pa.getBeginTimeBoundary();
                        dragParentEnd = pa.getEndTimeBoundary();
                    } else {
                        dragParentBegin = -1L;
                        dragParentEnd = -1L;
                    }
                } else {
                    dragParentBegin = -1L;
                    dragParentEnd = -1L;
                }

                int x = (int) ((dragEditTag2D.getBeginTime() / msPerPixel) -
                    (intervalBeginTime / msPerPixel));
                int x2 = x +
                    (int) ((dragEditTag2D.getEndTime() -
                    dragEditTag2D.getBeginTime()) / msPerPixel);

                if ((x2 - x) < (3 * DRAG_EDIT_MARGIN)) {
                    dragEditMode = DRAG_EDIT_CENTER;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else
                if (Math.abs(x - e.getX()) < DRAG_EDIT_MARGIN) {
                    dragEditMode = DRAG_EDIT_LEFT;
                    setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                } else if (Math.abs(x2 - e.getX()) < DRAG_EDIT_MARGIN) {
                    dragEditMode = DRAG_EDIT_RIGHT;
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else {
                    dragEditMode = DRAG_EDIT_CENTER;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

            }
        } else {
            panMode = false;

            /* just to be sure a running scroll thread can be stopped */
            stopScroll();
        }
    }

    /*
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseReleased(MouseEvent e) {
        //stop scrolling thread
        stopScroll();

        // changing the selection might have changed the intervalBeginTime
        if (timeScaleConnected) {
            setGlobalTimeScaleIntervalBeginTime(intervalBeginTime);
            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        }

        if (panMode) {
            panMode = false;
            setCursor(Cursor.getDefaultCursor());
        }

        if (dragEditing) {
            setCursor(Cursor.getDefaultCursor());
            if (dragEditTag2D != null) {
	            if (dragEditTag2D.getY() != dragEditTag2D.getOrigY()) {
	            	//System.out.println("Across tier translate!");
	            	Point inverse = new Point(e.getPoint());
	            	inverse.y += verticalScrollOffset;
	            	int tindex = getTierIndexForPoint(inverse);
	            	if (tindex > -1) {
	            		Tier2D t2d = visibleTiers.get(tindex);
	            		if (t2d.getTier() != dragEditTag2D.getAnnotation().getTier()) {
	            			if (t2d.getTier().getParentTier() != null) {
	            				// popup a message that the annotation can only be dragged to a
	            				// top level tier
	            			} else {
	            				doMoveAnnotation(dragEditTag2D.getAnnotation(), t2d.getTier());
	            			}
	            		} 
	            	}
	            } else {
	            	doDragModifyAnnotationTime();
	            }
            }
            dragEditing = false;
            dragEditTag2D = null;
            dragParentBegin = -1L;
            dragParentEnd = -1L;
            repaint();
        }
    }

    /*
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseEntered(MouseEvent e) {
    }

    /*
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseExited(MouseEvent e) {
        //stop scrolling thread
        stopScroll();
        hoverTag2D = null;
        showEmptySlots = false;
        repaint();
    }

    /*
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            return;
        }

        dragEndPoint = e.getPoint();

        //panning
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

        /*e.getPoint can be outside the image size*/
        if ((dragEndPoint.x <= 0) ||
                (dragEndPoint.x >= (getWidth() - defBarWidth))) {
            stopScroll();

            /*
               if (timeScaleConnected) {
                   setGlobalTimeScaleIntervalBeginTime(intervalBeginTime);
                   setGlobalTimeScaleIntervalEndTime(intervalEndTime);
               }
             */
            return;
        }

        //auto scroll first
        if ((dragEndPoint.x < SCROLL_OFFSET) && (dragEndPoint.x > 0)) {
            /*
               long begin = intervalBeginTime - SCROLL_OFFSET * msPerPixel;
               if (begin < 0) {
                   begin = 0;
               }
               setIntervalBeginTime(begin);
               //paintBuffer();
             */
            if (scroller == null) {
                // if the dragging starts close to the edge call setSelection
                if ((dragStartPoint.x < SCROLL_OFFSET) &&
                        (dragStartPoint.x > 0)) {
                    setSelection(dragStartTime, dragStartTime);
                }

                stopScrolling = false;
                scroller = new DragScroller(-SCROLL_OFFSET / 4, 30);
                scroller.start();
            }

            return;
        } else if ((dragEndPoint.x > (getWidth() - defBarWidth - SCROLL_OFFSET)) &&
                (dragEndPoint.x < (getWidth() - defBarWidth))) {
            /*
               long begin = intervalBeginTime + SCROLL_OFFSET * msPerPixel;
               setIntervalBeginTime(begin);
               //paintBuffer();
             */
            if (scroller == null) {
                // if the dragging starts close to the edge call setSelection
                if ((dragStartPoint.x > (getWidth() - defBarWidth -
                        SCROLL_OFFSET)) &&
                        (dragStartPoint.x < (getWidth() - defBarWidth))) {
                    setSelection(dragStartTime, dragStartTime);
                }

                stopScrolling = false;
                scroller = new DragScroller(SCROLL_OFFSET / 4, 30);
                scroller.start();
            }

            return;
        } else {
            stopScroll();

            if (dragEditing) {
                // don't change the selection
                updateDragEditTag(dragEndPoint);

                return;
            }

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
            repaint();
        }
    }

    /*
     * Note: if the alt key is released while the mouse is moving
     * isAltDown() still returns true.
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
	public void mouseMoved(MouseEvent e) {
        final Point pp = e.getPoint(); //pp is in component coordinates
        hoverTag2D = null;
        dragEditTag2D = null;

        if (timeRulerVisible && (pp.y < rulerHeight)) {
            //setToolTipText(null);
            //setToolTipText(TimeFormatter.toString(timeAt(e.getPoint().x)));
            setToolTipText(getCommentTip(pp));
            showEmptySlots = false;
            //repaint();

            return;
        }

        final Point inverse = new Point(pp);

        //compensate for the intervalBeginTime
        //inverse.x += timeToPixels(intervalBeginTime);
        //inverse.y += verticalScrollOffset;
        //hoverTierIndex = getTierIndexForPoint(inverse);
        //hoverTag2D = getTagAt(inverse, hoverTierIndex);
        hoverTag2D = getHoverTag(inverse);

        /*
           if (e.isShiftDown()) {
               showEmptySlots = true;
           } else {
               showEmptySlots = false;
           }
         */

        // repaint();
        if ((hoverTag2D != null) && (hoverTag2D == cursorTag2D) &&
                e.isAltDown()) {
            setToolTipText(null);

            if (cursorTag2D.getAnnotation() instanceof AlignableAnnotation) {
                // here it is for display only, don't copy 
                dragEditTag2D = new DragTag2D(cursorTag2D.getAnnotation());
                dragEditTag2D.copyFrom(cursorTag2D);
            }
        } else if(hoverTag2D != null && e.isControlDown()) {
        	if(hoverTag2D != previousHoverTag2D) {
	        	setToolTipText(null);
	        	
	        	if(mediaDisplayer != null) {
	        		mediaDisplayer.discard();
	        	}
	        	
	            previousHoverTag2D = hoverTag2D;
	        	
	            Annotation annot = hoverTag2D.getAnnotation();
	            
	            int tierIndex = getTierIndexForAnnotation(hoverTag2D.getAnnotation());
	            int y = (rulerHeight + (tierIndex * pixelsForTierHeight)) - verticalScrollOffset;
	            int xBegin =  xAt(hoverTag2D.getBeginTime());
	            int hoverTag2DWidth = hoverTag2D.getWidth();
	            int tierHeight = calcTierHeight();
	            
	            hostMediaDisplayer(new Object[] {annot}, new Rectangle(xBegin, y, hoverTag2DWidth, tierHeight));
        	}
    	} else if (hoverTag2D != null /**&& (cvVideoPlayer == null || cvVideoPlayer.isCleanedUp())*/) {
            StringBuilder sb = new StringBuilder();

            if (hoverTag2D.getAnnotation() instanceof AlignableAnnotation) {
                sb.append("BT: ");
                sb.append(TimeFormatter.toString(hoverTag2D.getBeginTime()));
                sb.append(", ET: ");
                sb.append(TimeFormatter.toString(hoverTag2D.getEndTime()));
                sb.append(" ");
            }

            sb.append(hoverTag2D.getValue());

            //make tooltip multiline
            final int MAXTOOLTIPLENGTH = 140;
            String strTemp = sb.toString();
            String strEnd = "<html>";

            while (strTemp.length() > MAXTOOLTIPLENGTH) {
                int index = strTemp.lastIndexOf(" ", MAXTOOLTIPLENGTH);
                if (index > 0 && index <= MAXTOOLTIPLENGTH) {
                	strEnd += (strTemp.substring(0, index) + "<br>");
                	strTemp = strTemp.substring(index);
                } else {
                	if (strTemp.length() > MAXTOOLTIPLENGTH) {
                		strEnd += strTemp.substring(0, MAXTOOLTIPLENGTH);
                		strTemp = "";
                		break;
                	}
                }
            }
            strEnd += strTemp;

            if (hoverTag2D.getAnnotation() instanceof AbstractAnnotation) {
            	AbstractAnnotation annot = (AbstractAnnotation) hoverTag2D.getAnnotation(); 
            	String cvName = ((TierImpl) annot.getTier()).getLinguisticType().getControlledVocabularyName(); 
            	ControlledVocabulary cv = null; 
               	int langIndex = 0;
               	String lang = Preferences.getString(Preferences.PREF_ML_LANGUAGE, null);
                if(cvName != null && !cvName.isEmpty()) { 
            		cv = transcription.getControlledVocabulary(cvName); 
            		if (cv != null) {
            			if (lang != null) {
            				int i = cv.getIndexOfLanguage(lang);
            				if (i >= 0) {
            					langIndex = i;
            				}
            			}
            			CVEntry entry = cv.getEntrybyId(annot.getCVEntryId()); 
            			String d;
            			if (entry != null && (d = entry.getDescription(langIndex)) != null && !d.isEmpty()) {
            				strEnd += "<br>" + d;
            			} 
            		}
            	}
            	

    			if (annot.getExtRef() != null) { 
            		
            		final String dataCategory = annot.getExtRefValue(ExternalReference.ISO12620_DC_ID);
					if (dataCategory != null && !dataCategory.isEmpty()) {
            			
            			String name = ELANLocalDCRConnector.getInstance().getNameForDC(dataCategory);
                		if(name != null){
            				strEnd += "<br>DCR: " + name; 
            			} else {
            				strEnd += "<br>DCR: " + dataCategory; 
            			}
            		} 
                } 
            }
            
            strEnd += ("</html>");
            setToolTipText(strEnd);

            //setToolTipText(t.toString().replace('\n', ' '));
            //System.out.println("Value: " + t.getValues() + "begin: " + t.getBeginTime() + " end: " + t.getEndTime());
        } else if (hoverTag2D != null) {
        	// Do nothing here
        } else {
            setToolTipText(null);
            
            if(mediaDisplayer != null) {
            	mediaDisplayer.discard();
            }
            previousHoverTag2D = null;
        }
        if (e.isAltDown()) {// hier temp copied from pressed
            // drag-edit an annotation
//            if ((cursorTag2D != null) && (dragEditTag2D == null)) {
//                if (getHoverTag(new Point(e.getPoint())) == cursorTag2D) {
//                    dragEditTag2D = cursorTag2D;
//                }
//            }
//        	if (hoverTag2D != null) {
//        		dragEditTag2D = hoverTag2D;
//        	}

            if (hoverTag2D != null && (hoverTag2D.getAnnotation() instanceof AlignableAnnotation)) {
                // before changing anything, create a copy
                Tag2D copy = new Tag2D(hoverTag2D.getAnnotation());
                copy.setX(hoverTag2D.getX());
                copy.setWidth(hoverTag2D.getWidth());
                //selectedAnnotations.remove(hoverTag2D);
                hoverTag2D = copy;
                dragEditing = true;

                // find the parent annotation's boundaries, if this one is not on 
                // a root tier
                if (((TierImpl) hoverTag2D.getAnnotation().getTier()).hasParentTier()) {
                    //TierImpl pt = (TierImpl) ((TierImpl)dragEditTag2D.getAnnotation().getTier()).getParentTier();
                    //AlignableAnnotation pa = (AlignableAnnotation) pt.getAnnotationAtTime(
                    //	dragEditTag2D.getAnnotation().getBeginTimeBoundary());
                    AlignableAnnotation pa = (AlignableAnnotation) hoverTag2D.getAnnotation()
                                                                                .getParentAnnotation();

                    if (pa != null) {
                        dragParentBegin = pa.getBeginTimeBoundary();
                        dragParentEnd = pa.getEndTimeBoundary();
                    } else {
                        dragParentBegin = -1L;
                        dragParentEnd = -1L;
                    }
                } else {
                    dragParentBegin = -1L;
                    dragParentEnd = -1L;
                }

                int x = (int) ((hoverTag2D.getBeginTime() / msPerPixel) -
                    (intervalBeginTime / msPerPixel));
                int x2 = x +
                    (int) ((hoverTag2D.getEndTime() -
                    		hoverTag2D.getBeginTime()) / msPerPixel);

                if (Math.abs(x - e.getX()) < DRAG_EDIT_MARGIN) {
                    dragEditMode = DRAG_EDIT_LEFT;
                    setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                } else if (Math.abs(x2 - e.getX()) < DRAG_EDIT_MARGIN) {
                    dragEditMode = DRAG_EDIT_RIGHT;
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else {
                    dragEditMode = DRAG_EDIT_CENTER;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }

                if ((x2 - x) < (3 * DRAG_EDIT_MARGIN)) {
                    dragEditMode = DRAG_EDIT_CENTER;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            } else {
            	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        } else {
        	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        //repaint();
    }

    /**
     * The use of a mousewheel needs Java 1.4!<br>
     * The scroll amount of the mousewheel is the height of a tier.
     *
     * @param e DOCUMENT ME!
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
        if (e.getUnitsToScroll() > 0) {// on Mac this is the same as ver. scroll with two fingers on the trackpad
            scrollBar.setValue(scrollBar.getValue() + pixelsForTierHeight);
        } else {
            scrollBar.setValue(scrollBar.getValue() - pixelsForTierHeight);
        }
    }
    
    /**
     * Performs zooming
     */
	@Override
	public void magnify(double zoom) {
		if (zoom > 0) {
			zoomIn();
		} else if (zoom < 0) {
			zoomOut();
		}
	}

	/**
	 * Scrolls left, right, up or down.
	 */
	@Override
	public void swipe(int x, int y) {
		//System.out.println("TL swipe: " + x + ", " + y);
		if (x != 0) {
			long newTime = intervalBeginTime + pixelToTime(x);
			if (newTime != intervalBeginTime && !(newTime < 0 && newTime < intervalBeginTime)) {
				setIntervalBeginTime(newTime);
			}
		} else if (y != 0) {
			if (y < 0 && verticalScrollOffset == 0) {
				return;
			}
			int newValue = scrollBar.getValue() + y;
			if (newValue >= 0 && newValue <= scrollBar.getMaximum() && newValue != scrollBar.getValue()) {
				scrollBar.setValue(scrollBar.getValue() + y);
			}
		}
	}

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void keyTyped(KeyEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void keyPressed(KeyEvent e) {
        if (e.getModifiers() == (KeyEvent.ALT_MASK +
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() +
                KeyEvent.SHIFT_MASK)) {
            if (!showEmptySlots) {
                showEmptySlots = true;
                repaint();
            }
        } else {
            if (showEmptySlots) {
                showEmptySlots = false;
                repaint();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void keyReleased(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_CONTROL) ||
                (e.getKeyCode() == KeyEvent.VK_ALT) ||
                (e.getKeyCode() == KeyEvent.VK_SHIFT)) {
            if (showEmptySlots) {
                showEmptySlots = false;
                repaint();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            if (!dragEditing) {
                dragEditTag2D = null;
                repaint();
            }
        }
    }

    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == copyAnnoMI) {
    		copyAnno();
    	} else if (e.getSource() == copyAnnoTreeMI) {
    		copyAnnoTree();
    	} else if (e.getSource() == pasteAnnoHereMI) {
    		pasteAnnoHere();
    	} else if (e.getSource() == pasteAnnoTreeHereMI) {
    		pasteAnnoTreeHere();
    	} else if (e.getSource() == deleteSelAnnosMI) {
    		deleteSelectedAnnotations();
    	} else if (e.getSource() == zoomSelectionMI) {
    		zoomToSelection();
    	} else if (e.getSource() == zoomToEntireMediaMI) {
        	zoomToShowEntireMedia();
        } else if (e.getSource() == splitAnnotationMI) {
    		Annotation ann = getActiveAnnotation();
    		Object[] arguments = new Object[2];
        	arguments[0] = ann;
        	if(ann.getBeginTimeBoundary() < splitTime && ann.getEndTimeBoundary() > splitTime){
        		arguments[1] = splitTime;
        	}
        	Command command = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.SPLIT_ANNOTATION);
        	command.execute(transcription, arguments);    	
        }     	else if (e.getSource() == tickerModeMI) {
    		tickerMode = tickerModeMI.isSelected();
            setPreference("TimeLineViewer.TickerMode", Boolean.valueOf(tickerMode), 
            		transcription);
    	} else if (e.getSource() == timeRulerVisMI) {
    		timeRulerVisible = timeRulerVisMI.isSelected();
    		if (timeRulerVisible) {
    			rulerHeight = ruler.getHeight();
    		} else {
    			rulerHeight = 0;
    		}
    		paintBuffer();
    		notifyMultiTierControlPanel();
    		setPreference("TimeLineViewer.TimeRulerVisible", Boolean.valueOf(timeRulerVisible), 
    				transcription);
    	}
    	else if (e.getActionCommand().equals("connect")) {
            boolean connected = ((JCheckBoxMenuItem) e.getSource()).getState();
            setTimeScaleConnected(connected);
            setPreference("TimeLineViewer.TimeScaleConnected", Boolean.valueOf(connected), 
            		transcription);
        } else if (e.getActionCommand().equals("aastroke")) {
            aaStrokeBold = ((JCheckBoxMenuItem) e.getSource()).getState();
            repaint();
            setPreference("TimeLineViewer.ActiveAnnotationBold", 
            		Boolean.valueOf(aaStrokeBold), transcription);
        } else if (e.getActionCommand().equals("redTH")) {
        	reducedTierHeight = ((JCheckBoxMenuItem) e.getSource()).getState();
            pixelsForTierHeight = calcTierHeight();
            notifyMultiTierControlPanel();
            paintBuffer();
            setPreference("TimeLineViewer.ReducedTierHeight", 
            		Boolean.valueOf(reducedTierHeight), transcription);
        } else if (e.getActionCommand().equals("hsVis")) {
            hScrollBarVisible = ((JCheckBoxMenuItem) e.getSource()).getState();
            if (hScrollBarVisible) {
                add(hScrollBar);
            } else {
                remove(hScrollBar);
            }
            componentResized(null);
            setPreference("TimeLineViewer.HorizontalScrollBarVisible", 
            		Boolean.valueOf(hScrollBarVisible), transcription);
        } else if (e.getActionCommand().equals("newAnn")) {
            doNewAnnotation();
        } else if (e.getActionCommand().equals("annBefore")) {// remove
            doAnnotationBefore();
        } else if (e.getActionCommand().equals("annAfter")) {// remove
            doAnnotationAfter();
        } else if (e.getActionCommand().equals("modifyAnn")) {
            doModifyAnnotation();
        } else if (e.getActionCommand().equals("deleteAnn")) {
            doDeleteAnnotation();
        } else if (e.getActionCommand().indexOf("font") > -1) {
            String sizeString = e.getActionCommand();
            int index = sizeString.indexOf("font") + 4;
            int size = 12;

            try {
                size = Integer.parseInt(sizeString.substring(index));
            } catch (NumberFormatException nfe) {
                System.err.println("Error parsing font size");
            }

            updateFont(getFont().deriveFont((float) size));
            setPreference("TimeLineViewer.FontSize", Integer.valueOf(size), 
            		transcription);
        } else {
            /* the rest are zoom menu items*/
            String zoomString = e.getActionCommand();
            int zoom = 100;

            try {
                zoom = Integer.parseInt(zoomString);
            } catch (NumberFormatException nfe) {
                System.err.println("Error parsing the zoom level");
            }

            float newMsPerPixel = ((100f / zoom) * 10);
            setMsPerPixel(newMsPerPixel); 
        }
    }
    
	@Override
	public void editingCancelled() {
		if (delayedAnnotationActivation) {
			if (activateNewAnnotation) {
				setActiveAnnotationA(lastCreatedAnnotation);
			}
			delayedAnnotationActivation = false;
		}
	}

	@Override
	public void editingCommitted() {
		if (delayedAnnotationActivation) {
			if (activateNewAnnotation) {
				setActiveAnnotationA(lastCreatedAnnotation);
			}
			delayedAnnotationActivation = false;
		}		
	}
	
	/** 
	 * An alternative way of activating the newly created annotation while not
	 * moving the crosshair to the beginning.
	 * @param aa the new annotation
	 */
	private void setActiveAnnotationA(Annotation aa) {
        Command c = ELANCommandFactory.createCommand(getViewerManager()
                .getTranscription(),
                ELANCommandFactory.ACTIVE_ANNOTATION);
        c.execute(getViewerManager(), new Object[] { aa, Boolean.FALSE});
	}

    /**
     * Set the font size, the "active annotation bold" flag etc.
     */
	@Override
	public void preferencesChanged() {
		Integer fontSize = Preferences.getInt("TimeLineViewer.FontSize", 
				transcription);
		if (fontSize != null) {
			setFontSize(fontSize.intValue());
		}
		Boolean aaBold = Preferences.getBool("TimeLineViewer.ActiveAnnotationBold",
				transcription);
		if (aaBold == null) {
			aaBold = Preferences.getBool("TimeLineViewer.ActiveAnnotationBold",
					null);// application default
		}
		if (aaBold != null) {
			if (activeAnnStrokeBoldMI != null) {
				// will this cause an event?
				activeAnnStrokeBoldMI.setSelected(aaBold.booleanValue());
			}
			aaStrokeBold = aaBold.booleanValue();
		}
		
		Boolean centerAnn = Preferences.getBool("EditingPanel.ActiveAnnotationInCenter", null);
    	if(centerAnn == null){
    		centerAnnotation = true;
    	} else {
    		centerAnnotation = centerAnn.booleanValue();
    	}
    	
    	Boolean clearSel = Preferences.getBool("ClearSelectionAfterCreation", null);
    	
    	if (clearSel != null) {
    		activateNewAnnotation = !clearSel;
    	}
		
		Boolean redTH = Preferences.getBool("TimeLineViewer.ReducedTierHeight", transcription);
		if (redTH == null) {
			redTH = Preferences.getBool("TimeLineViewer.ReducedTierHeight", null);
		}
		if (redTH != null) {
			boolean oldReduce = reducedTierHeight;
			reducedTierHeight = redTH.booleanValue();
			
			if (reducedTierHeightMI != null) {
				reducedTierHeightMI.setSelected(reducedTierHeight);
			}
			if (oldReduce != reducedTierHeight) {
		        pixelsForTierHeight = calcTierHeight(); 
		        notifyMultiTierControlPanel();
			}
		}			

		Boolean hsVis = Preferences.getBool("TimeLineViewer.HorizontalScrollBarVisible", 
				transcription);
		if (hsVis != null) {
			hScrollBarVisible = hsVis.booleanValue();
			if (hScrollBarVisMI != null) {
				// will this cause an event??
				hScrollBarVisMI.setSelected(hScrollBarVisible);
			}
			if (!hScrollBarVisible) {
	            remove(hScrollBar);
	        }
		}
		Boolean tsConnect = Preferences.getBool("TimeLineViewer.TimeScaleConnected", 
				transcription);
		if (tsConnect != null) {
			if (timeScaleConMI != null) {
				timeScaleConMI.setSelected(tsConnect.booleanValue());
			}
			setTimeScaleConnected(tsConnect.booleanValue());
		}
		Boolean ticMode = Preferences.getBool("TimeLineViewer.TickerMode", 
				transcription);
		if (ticMode != null) {
			tickerMode = ticMode.booleanValue();
			if (tickerModeMI != null) {				
				tickerModeMI.setSelected(tickerMode);				
			}
		}
		Boolean rulerVis = Preferences.getBool("TimeLineViewer.TimeRulerVisible", 
				transcription);
		if (rulerVis != null) {
			timeRulerVisible = rulerVis.booleanValue();
			if (timeRulerVisMI != null) {
				timeRulerVisMI.setSelected(timeRulerVisible);
			}
			if (timeRulerVisible) {
				rulerHeight = ruler.getHeight();
			} else {
				rulerHeight = 0;
			}
		}
		
		Float zoomLevel = Preferences.getFloat("TimeLineViewer.ZoomLevel", 
				transcription);
		if (zoomLevel != null) {
			float zl = zoomLevel.floatValue();
			if(zl > 5000.0){
				zl = 5000;
			}
            float newMsPerPixel =  (100f / zl) * 10;
            setMsPerPixel(newMsPerPixel);
			//updateZoomPopup((int)zl);
		}
		// preferred fonts
		Map<String, Font> foMap = Preferences.getMapOfFont("TierFonts", transcription);
		if (foMap != null) {
			
			for (Map.Entry<String, Font> e : foMap.entrySet()) {
				String key = e.getKey();
				Font ft = e.getValue();
				Tier2D t2d = null;
				
				if (key != null && ft != null) {
					for (int i = 0; i <allTiers.size(); i++) {
						t2d = allTiers.get(i);
						if (t2d.getName().equals(key)) {
							break;
						}
					}
					// use the size of the default font
					if (prefTierFonts.containsKey(key)) {
						Font oldF = (Font) prefTierFonts.get(key);
						if (!oldF.getName().equals(ft.getName())) {
							prefTierFonts.put(key, new Font(ft.getName(), 
									Font.PLAIN, font.getSize()));
							reextractTagsForTier(t2d);
						}
					} else {
						prefTierFonts.put(key, new Font(ft.getName(), Font.PLAIN, font.getSize()));
						reextractTagsForTier(t2d);
					}					
				}
			}	
			List<String> remKeys = new ArrayList<String>(4);
			
			for (String key : prefTierFonts.keySet()) {
				if (!foMap.containsKey(key)) {
					remKeys.add(key);
				}
			}

			if (!remKeys.isEmpty()) {
				for (String key : remKeys) {
					prefTierFonts.remove(key);
					
					for (Tier2D t2d : allTiers) {
						if (t2d.getName().equals(key)) {
							reextractTagsForTier(t2d);
							break;
						}
					}
				}
			}
		}
		
        Boolean boolPref = Preferences.getBool("InlineEdit.EnterCommits", null);

        if (boolPref != null) {
            editBox.setEnterCommits(boolPref.booleanValue());
        }

        boolPref = Preferences.getBool("InlineEdit.DeselectCommits", null);

        if (boolPref != null) {
            deselectCommits = boolPref.booleanValue();
        }
//        Map<String, Color> prefHLColors = (Map<String,Color>) Preferences.get(
//        		"TierHighlightColors", transcription);
//        if (prefHLColors != null) {
//        	highlightColors = new HashMap<String, Color>(prefHLColors.size());
//        	String key;
//        	Color colorVal;
//        	Iterator<String> keyIt = prefHLColors.keySet().iterator();
//        	while (keyIt.hasNext()) {
//        		key = keyIt.next();
//        		colorVal = prefHLColors.get(key);
//        		highlightColors.put(key, new Color(colorVal.getRed(), colorVal.getGreen(), colorVal.getBlue(), 102));
//        	}
//        }
        highlightColors =  Preferences.getMapOfColor(
        		"TierHighlightColors", transcription);
        
        // update CV entry colors, should be handled differently if possible
        ControlledVocabulary cv;
        for (Tier2D t2d : allTiers) {
        	
        	cv = transcription.getControlledVocabulary(t2d.getTier().getLinguisticType().getControlledVocabularyName());
        	if (cv != null) {
        		for (Tag2D tag2d : t2d.getTagsList()) {
        			Annotation ann = tag2d.getAnnotation();
        			
        			if (ann.getValue() != null && !ann.getValue().isEmpty()) {
        				setColor(tag2d, ann, cv);
        			}
        		}
        	} else {
        		TierImpl markTier = TierAssociation.findMarkerTierFor(transcription, t2d.getTier());
        		if (markTier != null) {
        			cv = transcription.getControlledVocabulary(markTier.getLinguisticType().getControlledVocabularyName());
        			if (cv != null) {
        				//entries = cv.getEntries();
                		for (Tag2D tag2d : t2d.getTagsList()) {
                			
                    		List<Annotation> ch = ((AbstractAnnotation)tag2d.getAnnotation()).getChildrenOnTier(markTier);
                    		if (ch.size() >= 1) {// should be 1 max
                    			Annotation ma = ch.get(0);
                				setColor(tag2d, ma, cv);
                    		}
                		}
        			}
        		}
        	}
        } 
        
	boolPref = Preferences.getBool("ClearSelectionOnSingleClick",null); 
	if (boolPref != null) {
	    clearSelOnSingleClick = boolPref.booleanValue();
	}
	
	Color colorPref = Preferences.getColor("Preferences.SymAnnColor",null); 
	if (colorPref != null) {
	    symAnnColor = new Color (colorPref.getRed(), colorPref.getGreen(), colorPref.getBlue());
	}
	
	Integer intPref = Preferences.getInt("Preferences.TimeLine.HorScrollSpeed", null);
	if (intPref instanceof Integer) {
		horScrollSpeed = intPref;
	}
	
	boolPref = Preferences.getBool("UI.UseBufferedPainting", null);
	if (!SystemReporting.isBufferedPaintingPropertySet && boolPref instanceof Boolean) {
		useBufferedImage = boolPref;
		//System.out.println("TL use buffered image preference: " + useBufferedImage);
	}
	
	paintBuffer();
}
	
    /**
     * Should we dispose of Graphics object?
     */
    @Override
	public void finalize() throws Throwable {
        System.out.println("Finalize TimeLineViewer...");
        if (bi != null) {
            bi.flush();
        }
        if (big2d != null) {
            big2d.dispose();
        }
        if (editBox != null) {
        	editBox.removeInlineEditBoxListener(this);
        }
        super.finalize();
    }
/*
	@Override
	public void paint(Graphics g) {
		System.out.println("Paint");		
		super.paint(g);
	}

	@Override
	public void layout() {
		// TODO Auto-generated method stub
		System.out.println("Layout");
		super.layout();
	}

	@Override
	public void doLayout() {
		// TODO Auto-generated method stub
		System.out.println("Do Layout");
		super.doLayout();
		//paintBuffer();
	}
*/
	/**
     * Scrolls the image while dragging to the left or right with the specified
     * number of pixels.<br>
     * This method is called from a separate Thread.
     *
     * @param numPixels the number of pixels to scroll the interval
     *
     * @see DragScroller
     */
    synchronized void scroll(int numPixels) {
        long begin = intervalBeginTime + (int) (numPixels * msPerPixel);

        if (numPixels > 0) {
            // left to right, change selection while scrolling
            setIntervalBeginTime(begin);
            selectionEndTime = getSelectionEndTime() +
                (int) (numPixels * msPerPixel);

            if (selectionEndTime > getMediaDuration()) {
                selectionEndTime = getMediaDuration();
            }

            setMediaTime(selectionEndTime);
            setSelection(getSelectionBeginTime(), selectionEndTime);
        } else {
            // right to left
            if (begin < 0) {
                begin = 0;
            }

            setIntervalBeginTime(begin);
            selectionBeginTime = getSelectionBeginTime() +
                (int) (numPixels * msPerPixel);

            if (selectionBeginTime < 0) {
                selectionBeginTime = 0;
            }

            setMediaTime(selectionBeginTime);
            setSelection(selectionBeginTime, getSelectionEndTime());
        }

        //repaint();
    }

    /**
     * DOCUMENT ME!
     */
    void stopScroll() {
        /*
           if (scroller != null) {
               try {
                   scroller.interrupt();
               } catch (SecurityException se) {
                   System.out.println("TimeLineViewer: could not stop scroll thread");
               } finally {
               }
               scroller = null;
           }
         */
        stopScrolling = true;
        scroller = null;
    }

    // the edit actions from the popup menu, the Commands and Actions will come in here

    private void doNewAnnotation() {
        if (rightClickTier == null) {
            return;
        }

        // use command
        TierImpl tier = rightClickTier.getTier();
        long begin = getSelectionBeginTime();
        long end = getSelectionEndTime();

        if (begin == end) {
            return;
        }

        if (!tier.isTimeAlignable()) {
            begin = rightClickTime;
            end = rightClickTime;
        }

        Command c = createNewAnnotationCommand();
        
        Object[] args = new Object[] {
                new Long(begin), new Long(end)
            };
        c.execute(tier, args);
    }

    /**
     * Annotation before is relative to the cursor tag (active annotation)
     * remove
     */
    private void doAnnotationBefore() {
        if ((rightClickTier == null) || (cursorTag2D == null)) {
            return;
        }

        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.NEW_ANNOTATION_BEFORE);
        Object[] args = new Object[] { cursorTag2D.getAnnotation() };
        c.execute(rightClickTier.getTier(), args);
    }

    /**
     * Annotation after is relative to the cursor tag (active annotation)
     * remove
     */
    private void doAnnotationAfter() {
        if ((rightClickTier == null) || (cursorTag2D == null)) {
            return;
        }

        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.NEW_ANNOTATION_AFTER);
        Object[] args = new Object[] { cursorTag2D.getAnnotation() };
        c.execute(rightClickTier.getTier(), args);
    }

    private void doModifyAnnotation() {
        if (cursorTag2D != null) {
            showEditBoxForTag(cursorTag2D);
        }
    }

    private void doDeleteAnnotation() {
        if (cursorTag2D != null) {
            Tier tier = cursorTag2D.getTier2D().getTier();
            Annotation aa = cursorTag2D.getAnnotation();

            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.DELETE_ANNOTATION);
            c.execute(tier, new Object[] { getViewerManager(), aa });
        }
    }
    
    private void deleteSelectedAnnotations() {
    	if (selectedAnnotations.size() == 0) {
    		return;
    	}
    	List<Annotation> selAnnos = new ArrayList<Annotation>(selectedAnnotations.size());
    	for (Tag2D t2d : selectedAnnotations) {
    		selAnnos.add(t2d.getAnnotation());
    	}
    	Command c = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.DELETE_MULTIPLE_ANNOS);
    	c.execute(transcription, new Object[]{selAnnos});
    	
    	selectedAnnotations.clear();
    	repaint();
    }
    
    private void copyAnno() {
    	if (cursorTag2D != null) {
            Annotation aa = cursorTag2D.getAnnotation();
            
            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.COPY_ANNOTATION);
            c.execute(null, new Object[]{aa});
    	}
    }
    
    private void copyAnnoTree() {
    	if (cursorTag2D != null) {
            Annotation aa = cursorTag2D.getAnnotation();
            
            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.COPY_ANNOTATION_TREE);
            c.execute(null, new Object[]{aa});
    	}
    }
    
    private void pasteAnnoHere() {
    	if (rightClickTier != null) {
    		TierImpl tier = rightClickTier.getTier();
    		
            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.PASTE_ANNOTATION_HERE);
            c.execute(transcription, new Object[]{tier.getName(), new Long(rightClickTime)});
    	}
    }
    
    private void pasteAnnoTreeHere() {
    	if (rightClickTier != null) {
    		TierImpl tier = rightClickTier.getTier();
    		
            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.PASTE_ANNOTATION_TREE_HERE);
            c.execute(transcription, new Object[]{tier.getName(), new Long(rightClickTime)});
    	}	
    }

    /**
     * Calculate the new begin and end time of the active annotation  and
     * create a command.
     */
    private void doDragModifyAnnotationTime() {
    	if (dragEditTag2D == null) {
    		return;
    	}
    	AlignableAnnotation editAnn = null;
    	if (dragEditTag2D != null && dragEditTag2D.getAnnotation() instanceof AlignableAnnotation) {
    		editAnn = (AlignableAnnotation) dragEditTag2D.getAnnotation();
    	}
    	if (editAnn == null) {
    		return;
    	}
//        if ((dragEditTag2D == null) ||
//                !(getActiveAnnotation() instanceof AlignableAnnotation)) {
//            return;
//        }

        long beginTime = 0L;
        long endTime = 0L;

        switch (dragEditMode) {
        case DRAG_EDIT_CENTER:
            beginTime = pixelToTime(dragEditTag2D.getX());
            endTime = pixelToTime(dragEditTag2D.getX() +
                    dragEditTag2D.getWidth());

            if (beginTime < 0) {
            	long corr = -beginTime;
            	beginTime += corr;
            	endTime += corr;
            }
            
            break;

        case DRAG_EDIT_LEFT:
            beginTime = pixelToTime(dragEditTag2D.getX());
            endTime = dragEditTag2D.getEndTime();

            break;

        case DRAG_EDIT_RIGHT:
            beginTime = dragEditTag2D.getBeginTime();
            endTime = pixelToTime(dragEditTag2D.getX() +
                    dragEditTag2D.getWidth());
        }

        if (endTime <= beginTime) {
            return;
        }
        // check if the change in begin and/or end time is >= than one pixel
        if (Math.abs(beginTime - dragEditTag2D.getBeginTime()) < msPerPixel &&
        		Math.abs(endTime - dragEditTag2D.getEndTime()) < msPerPixel) {
        	return;
        }
        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.MODIFY_ANNOTATION_TIME);
//        c.execute(getActiveAnnotation(),
//            new Object[] { new Long(beginTime), new Long(endTime) });
        c.execute(editAnn,
                new Object[] { new Long(beginTime), new Long(endTime) });
    }
    
    /**
     * Moves an (alignable) annotation from one tier to another (top level tier) . 
     * @param ann
     * @param t
     */
    private void doMoveAnnotation(Annotation ann, Tier t) {
        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.MOVE_ANNOTATION_TO_TIER);

        c.execute(t, new Object[] { ann });
    }

    /**
     * A Thread for scrolling near the left and right border of the viewer.
     *
     * @author HS
     * @version 1.0
     */
    class DragScroller extends Thread {
        /** Holds value of property DOCUMENT ME! */
        int numPixels;

        /** Holds value of property DOCUMENT ME! */
        long sleepTime;

        /**
         * Creates a new DragScroller instance
         *
         * @param numPixels DOCUMENT ME!
         * @param sleepTime DOCUMENT ME!
         */
        DragScroller(int numPixels, long sleepTime) {
            this.numPixels = numPixels;
            this.sleepTime = sleepTime;
        }

        /**
         * Periodically scrolls the view.
         */
        @Override
		public void run() {
            while (!stopScrolling) {
                TimeLineViewer.this.scroll(numPixels);

                try {
                    sleep(sleepTime);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    //##############################################
/*
	public void doLayout() {
		// TODO Auto-generated method stub
		super.doLayout();
		System.out.println("doLayout....");
    	if (!useBufferedImage) {
    		imageWidth = getWidth() - defBarWidth;
            intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);
            if (timeScaleConnected) {
                setGlobalTimeScaleIntervalEndTime(intervalEndTime);
            }
    	}
        //paintBuffer();
        if (hScrollBarVisible) {
            hScrollBar.setBounds(0, getHeight() - defBarWidth, getWidth() - defBarWidth, defBarWidth);
            //hScrollBar.revalidate();
            updateHorScrollBar();
        scrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
                    getHeight() - defBarWidth);
        } else {
            scrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
            getHeight());
        }
        //scrollBar.revalidate();
        updateScrollBar();
        paintBuffer();
        //hScrollBar.setVisible(true);
	}
*/
	class TLLayoutManager implements LayoutManager2 {

		@Override
		public void addLayoutComponent(Component comp, Object constraints) {
			// TODO Auto-generated method stub
			System.out.println("add...");
		}

		@Override
		public float getLayoutAlignmentX(Container target) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public float getLayoutAlignmentY(Container target) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void invalidateLayout(Container target) {
			// TODO Auto-generated method stub
			System.out.println("invalidate...");
			//hScrollBar.setVisible(false);
		}

		@Override
		public Dimension maximumLayoutSize(Container target) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void layoutContainer(Container parent) {
			// TODO Auto-generated method stub
			System.out.println("layout container...");
			// in the case useBuffer == false calc the image width
			/*
	    	if (!useBufferedImage) {
	    		imageWidth = getWidth() - defBarWidth;
	            intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);
	            if (timeScaleConnected) {
	                setGlobalTimeScaleIntervalEndTime(intervalEndTime);
	            }
	    	}
	        //paintBuffer();
	        if (hScrollBarVisible) {
	            hScrollBar.setBounds(0, getHeight() - defBarWidth, getWidth() - defBarWidth, defBarWidth);
	            //hScrollBar.revalidate();
	            updateHorScrollBar();
	        scrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
	                    getHeight() - defBarWidth);
	        } else {
	            scrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
	            getHeight());
	        }
	        //scrollBar.revalidate();
	        updateScrollBar();
	        paintBuffer();
	        */
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return null;
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return null;
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}
    	
    }

	@Override
	public void hostMediaDisplayer(Object[] arguments, Rectangle sourceBounds) {
		mediaDisplayer = MediaDisplayerFactory.getMediaDisplayer(arguments);
        if(mediaDisplayer != null) {

            // TODO Have the videoWidth and videoHeight determined
            // dynamically from the actual video, and calculate
            // position and size accordingly.
            int videoWidth = 320;
            int videoHeight = 180;

            MediaDisplayerFactory.MEDIA_ORIENTATION horizontalOrientation = MediaDisplayerFactory.MEDIA_ORIENTATION.WEST;
            MediaDisplayerFactory.MEDIA_ORIENTATION verticalOrientation = MediaDisplayerFactory.MEDIA_ORIENTATION.NORTH;

           // Determine whether the tag2d is above or below the center
            int hoverTag2DCenterY = (int) sourceBounds.y + sourceBounds.height/2;
            int viewportCenterY = imageHeight/2;
            int videoYCoordinate = sourceBounds.y;
            if(hoverTag2DCenterY > viewportCenterY) {
            	videoYCoordinate = sourceBounds.y + sourceBounds.height;
            	verticalOrientation = MediaDisplayerFactory.MEDIA_ORIENTATION.SOUTH;
            }

            // Determine whether the tag2d is left or right of the center
            int hoverTag2DCenterX = (int) sourceBounds.x + sourceBounds.width/2;
            int viewportCenterX = imageWidth/2;
            int videoXCoordinate = sourceBounds.x + sourceBounds.width + 2;
            if(hoverTag2DCenterX > viewportCenterX) {
            	videoXCoordinate = sourceBounds.x - 2;
            	horizontalOrientation = MediaDisplayerFactory.MEDIA_ORIENTATION.EAST;
            }

            // Determine the bounding box for the video
            Rectangle bounds = new Rectangle(videoXCoordinate, videoYCoordinate, videoWidth, videoHeight);
        	
        	mediaDisplayer.displayMedia(this, bounds, 500, horizontalOrientation, verticalOrientation);
        } else {
        	if (ClientLogger.LOG.isLoggable(Level.FINE)) {
        		ClientLogger.LOG.fine("mediaDisplayer == null");
        	}
        }
	}

	@Override
	public void discardMediaDisplayer() {
		if(mediaDisplayer != null) {
	    	mediaDisplayer.discard();
	    }
	}
}
