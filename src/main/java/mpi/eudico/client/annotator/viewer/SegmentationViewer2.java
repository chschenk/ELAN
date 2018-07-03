package mpi.eudico.client.annotator.viewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.annotator.util.Tag2D;
import mpi.eudico.client.annotator.util.Tier2D;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.TimeInterval;

/**
 * A new implementation of the Segmentation viewer, designed for the new
 * Segmentation mode. The segmentation viewer is now part of the main window.
 * New annotations are immediately created (applied). Multiple tiers are now
 * shown, like in the Timeline viewer, be it that only toplevel tiers and 
 * "included in" tiers are displayed. Activating another tier is possible by
 * means of the arrow up and down keys.
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class SegmentationViewer2 extends DefaultTimeScaleBasedViewer implements 
MultiTierViewer, AdjustmentListener, ACMEditListener, ChangeListener {
    /**
     * every time the enter key is typed either a begin or an end time is added
     */
    public static final int TWO_TIMES_SEGMENTATION = 0;

    /** every time the enter key is typed an end and a begin are added */
    public static final int ONE_TIME_SEGMENTATION = 1;

    /** every time the enter key is typed an annotation of fixed duration is created,
     * the stroke time is either the begin or the end time */
    public static final int ONE_TIME_FIXED_SEGMENTATION = 2;
    
    /** every time the enter key is typed an annotation of fixed duration is created,
     * the stroke time is either the begin or the end time */
    public static final int TWO_TIMES_PRESS_RELEASE_SEGMENTATION = 3;
	
	private TranscriptionImpl transcription;
	
    private int pixelsForTierHeight;
    private int pixelsForTierHeightMargin;

    /** default scrollbar width */
    private int defBarWidth;
    private int[] tierYPositions;
    /** the control panel that receives the setTierPositions call */
    private SegmentationControlPanel segmentationControlPanel;
    private JScrollBar scrollBar;
    private JScrollBar hScrollBar;
    private JPanel zoomSliderPanel;
    private JSlider zoomSlider;
    private final int ZOOMSLIDER_WIDTH = 100;
    private JPanel corner;
    private int verticalScrollOffset;
    private int rhDist = 3;

    // data
    private Tier2D editTier2d;
    private Tag2D dragEditTag2D;
    private int editTierHeight = 25;
    private ArrayList<Tier2D> allTiers;
    private List<Tier2D> visibleTiers;

    // by default the single stroke of a fixed annotation marks the begin
    private boolean singleStrokeIsBegin = true;
    private long fixedDuration = 1000;
    private long delayDuration = 0L;
    private ButtonGroup fontSizeBG;
    private JMenu fontMenu;
    private JMenuItem deleteAnnoMI;
    private JMenuItem mergeNextAnnoMI;
    private JMenuItem mergeBeforeAnnoMI;
    private JMenuItem splitAnnoMI;
    private JMenuItem modifyAnnoTimeDlgMI;

    // administration
    //private ArrayList<TimeInterval> timeSegments;
    private String curTier;
	private InputMap mainInputMap;
	private InputMap cvInputMap;
	private ActionMap mainActionMap;
	private ActionMap cvActionMap;

    // default mode is the two-times-segmentation mode
    private int segmentMode = TWO_TIMES_SEGMENTATION;
    private long lastSegmentTime = -1;
    private int timeCount = 0;
    private long currentBeginTime = -1L;
    private AnnotationDataRecord lastRecord = null;
	/** an object to synchronize on */
    private final Object paintlock = new Object();

    private final int DRAG_EDIT_MARGIN = 8;
    private final int DRAG_EDIT_CENTER = 0;
    private final int DRAG_EDIT_LEFT = 1;
    private final int DRAG_EDIT_RIGHT = 2;
    private int dragEditMode = 0;
    private boolean dragEditing;
    // the parent's boundaries
    private long dragParentBegin = -1L;
    private long dragParentEnd = -1L;

	private long splitTime;
	private Map<String, Action> actionMap;
	private boolean pressReleaseKeyDown;
	/** declare a few actions here for easy enabling and disabling */
	private AbstractAction segmentTypeAction, segmentPressAction, segmentReleaseAction;
    
    /**
     * 
     */
	public SegmentationViewer2() {
		super();
		addMouseWheelListener(this);
        //setOpaque(true);
        //setDoubleBuffered(true);
        // Not using a buffered image makes repainting very slow under Java 1.6.0_22, Mac
         String bufImg = System.getProperty("useBufferedImage");
        if (bufImg != null && bufImg.toLowerCase().equals("true")) {
        	useBufferedImage = true;
        }
        pressReleaseKeyDown = false;
	}
	
	/**
	 * 
	 * @param transcription
	 */
	public SegmentationViewer2(Transcription transcription) {
		this();
		this.transcription = (TranscriptionImpl) transcription;
		initViewer2();		
	}

	private void initViewer2() {
		//super.initViewer();
		pixelsForTierHeight = calcTierHeight();
        pixelsForTierHeightMargin = 2;
        scrollBar = new JScrollBar(JScrollBar.VERTICAL, 0, 50, 0, 200);
        scrollBar.setUnitIncrement(pixelsForTierHeight / 2);
        scrollBar.setBlockIncrement(pixelsForTierHeight);
        scrollBar.addAdjustmentListener(this);

        hScrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 50, 0, 400);
        hScrollBar.setUnitIncrement(10);
        hScrollBar.setBlockIncrement(40);
        hScrollBar.addAdjustmentListener(this);
        
        add(hScrollBar);
        add(scrollBar);
        
        // the range is from 10% to 1000%, in order to have the button more in the center at 100% use a different max value
        zoomSlider = new JSlider(10, 300, 100);
        zoomSlider.putClientProperty("JComponent.sizeVariant", "small");// On MacOS regular, small, mini
        zoomSliderPanel = new JPanel(null);
        zoomSliderPanel.add(zoomSlider);
        zoomSlider.addChangeListener(this);
        zoomSlider.setToolTipText(String.valueOf(zoomSlider.getValue()));
        
        corner = new JPanel();
        add(corner);

        // this helps in disabling the mapping but still UP and DOWN are captured somehow 
        //zoomSlider.setInputMap(JComponent.WHEN_FOCUSED, new InputMap());
        //zoomSlider.setActionMap(new ActionMap());
        KeyStroke[] ks = zoomSlider.getRegisteredKeyStrokes();
        InputMap im;
        ActionMap am = zoomSlider.getActionMap();
        for (KeyStroke element : ks) {
        	if (element.getKeyCode() == KeyEvent.VK_LEFT || element.getKeyCode() == KeyEvent.VK_RIGHT) {
        		continue;
        	}
        	int condition = zoomSlider.getConditionForKeyStroke(element);
        	im = zoomSlider.getInputMap(condition);
        	if (im != null) {       		
        		Object obj = im.get(element);
        		am.remove(obj);
        		if (am.getParent() != null) {
        			am.getParent().remove(obj);
        		}
        		im.remove(element);
        	}
        }
        //zoomSlider.setInputMap(JComponent.WHEN_FOCUSED, new InputMap());
        //zoomSlider.setFocusable(false);
        add(zoomSliderPanel);
       
        
        actionMap = new  HashMap<String, Action>();
        mainInputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        mainActionMap = getActionMap();
       
        //timeSegments = new ArrayList<TimeInterval>(20);
        segmentTypeAction = new SegmentAction(); 
        actionMap.put(ELANCommandFactory.NEXT_ACTIVE_TIER, new ActiveTierAction(true));              
        actionMap.put(ELANCommandFactory.PREVIOUS_ACTIVE_TIER, new ActiveTierAction(false));        
        actionMap.put(ELANCommandFactory.DELETE_ANNOTATION, new DeleteAnnotationAction());   
        actionMap.put(ELANCommandFactory.MERGE_ANNOTATION_WN, new MergeAnnotationAction(true));   
        actionMap.put(ELANCommandFactory.MERGE_ANNOTATION_WB, new MergeAnnotationAction(false));   
        actionMap.put(ELANCommandFactory.SPLIT_ANNOTATION, new SplitAnnotationAction());   
        actionMap.put(ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG, new ModifyTimeDlgAction());
        
        DeleteAnnotationAction delac2 = new DeleteAnnotationAction(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        String key = "delact2";
        
        mainInputMap.put((KeyStroke) delac2.getValue(Action.ACCELERATOR_KEY), key);
        mainActionMap.put(key, delac2);
        getInputMap(WHEN_FOCUSED).put((KeyStroke) delac2.getValue(Action.ACCELERATOR_KEY), key);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put((KeyStroke) delac2.getValue(Action.ACCELERATOR_KEY), key);
        // initially add the default segmentation action
		key = ELANCommandFactory.SEGMENT;
        mainInputMap.put((KeyStroke) segmentTypeAction.getValue(Action.ACCELERATOR_KEY), key);
        mainActionMap.put(key, segmentTypeAction);
        getInputMap(WHEN_FOCUSED).put((KeyStroke) segmentTypeAction.getValue(Action.ACCELERATOR_KEY), key);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put((KeyStroke) segmentTypeAction.getValue(Action.ACCELERATOR_KEY), key);
        
        // ##### addition for the key press - key release segmentation ######
        // see also the switchActions method
        segmentPressAction = new PressReleaseSegmentAction(false);
        segmentReleaseAction = new PressReleaseSegmentAction(true);
        // #### end addition ####
        initTiers();
        updateShortcutMaps();
                
        /*
        ActiveTierAction dac2 = new ActiveTierAction(true);
        key = "nextact2";
        getInputMap(WHEN_FOCUSED).put((KeyStroke) dac2.getValue(Action.ACCELERATOR_KEY), key);
        mainActionMap.put(key, dac2);
        ActiveTierAction nac2 = new ActiveTierAction(false);
        key = "prevact2";
        getInputMap(WHEN_FOCUSED).put((KeyStroke) nac2.getValue(Action.ACCELERATOR_KEY), key);
        mainActionMap.put(key, nac2);
        
        ActiveTierAction dac3 = new ActiveTierAction(true);
        key = "nextact3";
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put((KeyStroke) dac3.getValue(Action.ACCELERATOR_KEY), key);
        mainActionMap.put(key, dac3);
        ActiveTierAction nac3 = new ActiveTierAction(false);
        key = "prevact3";
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put((KeyStroke) nac3.getValue(Action.ACCELERATOR_KEY), key);
        mainActionMap.put(key, nac3);
        */
        //setFocusable(true);
        paintBuffer();
	}
	
	private void updateShortcutMaps(){
		ShortcutsUtil scu = ShortcutsUtil.getInstance();		
		Iterator<Entry<String, Action>> it = actionMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Action> pair = it.next();
			String actionName = pair.getKey();
			Action ca = pair.getValue();
			if(ca != null){
				ca.putValue(Action.ACCELERATOR_KEY, scu.getKeyStrokeForAction(actionName, ELANCommandFactory.SEGMENTATION_MODE));
				 mainInputMap.put((KeyStroke) ca.getValue(Action.ACCELERATOR_KEY), actionName);
			     mainActionMap.put(actionName, ca);
			     getInputMap(WHEN_FOCUSED).put((KeyStroke) ca.getValue(Action.ACCELERATOR_KEY),actionName);
			     getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put((KeyStroke) ca.getValue(Action.ACCELERATOR_KEY), actionName);
			}			
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
     * Calculates the height for all tiers, based on the base font size.
     * 
     * @return the height in pixels
     */
    private int calcTierHeight() {
    	int th = (int)((font.getSize() * 2.5) + (36 / font.getSize()));
        th = (th * 2) / 3; // the reduced height of the normal annotation mode
        editTierHeight = (int) (2.5 * th);
        return th;
    }
	
    /**
     * Initialise tiers and tags.
     */
    private void initTiers() {
    	defBarWidth = getDefaultBarWidth() + 2;
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
            if (!tier.hasParentTier() || (tier.getLinguisticType().getConstraints() != null && 
            		tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN)){
	            tier2d = createTier2D(tier);   
	            if (tier2d != null) {
	            	allTiers.add(tier2d);
	            	visibleTiers.add(tier2d);
	            }
            }
        }
        
        if (allTiers.size() > 0) {
        	setActiveTier(allTiers.get(0).getTier());
//        	editTier2d = allTiers.get(0);
        }
    }

    
    /**
     * Creates a Tier2D with 2D tags for all annotations.
     *
     * @param tier the tier
     */
    private Tier2D createTier2D(TierImpl tier) {
        if (tier == null) {
            return null;
        }

        Tier2D tier2d = new Tier2D(tier);
        //segments2d = new Tier2D(tier);

        Tag2D tag2d;
        int xPos;
        int tagWidth;

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
        
        return tier2d;
    }
    
    private void reextractTier(Tier2D tier2d) {
    	if (tier2d == null) {
    		return;
    	}
    	tier2d.getTagsList().clear();
    	
        Tag2D tag2d;
        int xPos;
        int tagWidth;

        for (Annotation a : tier2d.getTier().getAnnotations()) {

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
        paintBuffer();
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
        //Font tierFont;
        //FontMetrics tierMetrics;
        Iterator<Tier2D> tierIt = allTiers.iterator();

        while (tierIt.hasNext()) {
            tier2d = tierIt.next();
            //tierFont = getFontForTier(tier2d.getTier());
            //tierMetrics = getFontMetrics(tierFont);
            Iterator<Tag2D> tagIt = tier2d.getTags();

            while (tagIt.hasNext()) {
                tag2d = tagIt.next();
                xPos = timeToPixels(tag2d.getBeginTime());
                tag2d.setX(xPos);
                tagWidth = timeToPixels(tag2d.getEndTime()) - xPos;
                tag2d.setWidth(tagWidth);
                tag2d.setTruncatedValue(truncateString(tag2d.getValue(),
                        tagWidth, metrics));
            }
        }
    }
    
	/**
     * Override <code>JComponent</code>'s paintComponent to paint:<br>
     * - a BufferedImage with a ruler and the tags<br>
     * - the cursor / crosshair
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
        if (!useBufferedImage) {
        	paintUnbuffered(g2d);
        	return;
        }
        
        int h = getHeight();

        if (bi != null) {
            g2d.drawImage(bi, 0, 0, this);
        }

        if (currentBeginTime > -1) {
        	int y = rulerHeight + (2 * pixelsForTierHeightMargin);        	
        	//int x = timeToPixels(currentBeginTime);
        	int x = xAt(currentBeginTime);
        	if (x >= 0 && x <= imageWidth) {
        		g2d.setColor(Constants.SHAREDCOLOR6);
        		g2d.drawLine(x, y, x, y + editTierHeight - (2 * pixelsForTierHeightMargin));
        	}
        }
        
        if (selectionBeginPos != selectionEndPos) {
        	g2d.setColor(Constants.SELECTIONCOLOR);
        	g2d.setComposite(alpha04);
        	g2d.fillRect(selectionBeginPos, 0, (selectionEndPos - selectionBeginPos), imageHeight);
        	g2d.setComposite(AlphaComposite.SrcOver);
        }
        
        if (dragEditTag2D != null) {
        	paintEditTag(g2d);
        }
        
        if ((crossHairPos >= 0) && (crossHairPos <= imageWidth)) {
            // prevents drawing outside the component on Mac
            g2d.setColor(Constants.CROSSHAIRCOLOR);
            g2d.drawLine(crossHairPos, 0, crossHairPos, h);
        }
        //Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Paint to a buffer.<br>
     * First paint the top ruler, next the annotations of the current tier.
     */
    private void paintBuffer() {
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }

        if (imageWidth != getWidth()) {
            imageWidth = getWidth();
        }

        if (imageHeight != getHeight()) {
            imageHeight = getHeight();
        }

        intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);
    	if (!useBufferedImage) {
    		repaint();
    		return;
    	}
    	
        synchronized (paintlock) {
	        if ((bi == null) || (bi.getWidth() < imageWidth) ||
	                (bi.getHeight() < imageHeight)) {
	            bi = new BufferedImage(imageWidth, imageHeight,
	                    BufferedImage.TYPE_INT_RGB);
	            big2d = bi.createGraphics();
	        }
	
	        if (SystemReporting.antiAliasedText) {
		        big2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	        }
	        
	        big2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
	        big2d.fillRect(0, 0, imageWidth, bi.getHeight());
	        /* paint the edit tier area */
	        big2d.setColor(Color.WHITE);
	        big2d.fillRect(0, rulerHeight, imageWidth, editTierHeight);
	        
	        // mark the area beyond the media time
	        long dur = getMediaDuration();
	        int xx = xAt(dur);
	        
	        if (intervalEndTime > dur) {
	            if (!SystemReporting.isMacOS()) {
	            	big2d.setColor(UIManager.getColor("Panel.background"));// problems on the mac
	            } else {
	            	big2d.setColor(Color.LIGHT_GRAY);
	            }
	            big2d.fillRect(xx, 0, imageWidth - xx, bi.getHeight());
	        }
	
	        big2d.translate(-((int)(intervalBeginTime / msPerPixel)), 0);
	        
	        /*paint time ruler */
	        if (timeRulerVisible) {
		        big2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);    
		        ruler.paint(big2d, intervalBeginTime, imageWidth, msPerPixel,
		            SwingConstants.TOP);
	        }
	        
	        big2d.setFont(font);
	
	        // paint a slightly dif. background color for every other tier
	        int y = rulerHeight + editTierHeight - verticalScrollOffset;
	        int ax = timeToPixels(intervalBeginTime); // == -translation.x
	
	        big2d.setColor(Constants.LIGHTBACKGROUNDCOLOR);
	        if (intervalBeginTime < getMediaDuration()) {
	        	int numStripes = Math.max(visibleTiers.size(), imageHeight / pixelsForTierHeight);
	        	int yy = y;
		        for (int i = 0; i < numStripes; i++) {
		            if (i % 2 != 0) {
		            	yy = y + i * pixelsForTierHeight;
		            	if (yy < rulerHeight + editTierHeight) {
		            		if (yy + pixelsForTierHeight > rulerHeight + editTierHeight) {
		            			big2d.fillRect(ax, rulerHeight + editTierHeight, xx, 
		            					pixelsForTierHeight - (rulerHeight + editTierHeight - yy));
		            		} else {
		            			continue;
		            		}
		            	} else {
		            		big2d.fillRect(ax, y + i * pixelsForTierHeight, xx, pixelsForTierHeight);
		            	}
		            }
		        }
	        }
	        big2d.setColor(Constants.SHAREDCOLOR6);
	        int height = pixelsForTierHeight - (2 * pixelsForTierHeightMargin);
	        y = rulerHeight + pixelsForTierHeightMargin + (pixelsForTierHeight / 2);
	        int x;
	        int width;
	        Tag2D tag2d;
	        Tier2D tier2d = null;
	        // hier, paint the active tier + annotations
	        if (editTier2d != null) {
	            Iterator<Tag2D> tagIt = editTier2d.getTags();
	        	
	            while (tagIt.hasNext()) {
	                tag2d = tagIt.next();
	
	                if (tag2d.getEndTime() < intervalBeginTime) {
	                    continue; //don't paint
	                } else if (tag2d.getBeginTime() > intervalEndTime) {
	                    break; // stop looping this tier
	                }
	
	                x = tag2d.getX();
	                width = tag2d.getWidth();
	                big2d.drawLine(x, y, x, y + height);
	                //big2d.drawLine(x, y + (height / 2), x + width, y +
	                //    (height / 2));
	                big2d.drawLine(x, y + (height - rhDist), x + width, y + (height - rhDist));
	                
	                big2d.drawLine(x + width, y, x + width, y + height);
	
	                int descent = big2d.getFontMetrics().getDescent();
	//                big2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
	//                    (float) (y + ((height / 2) - descent + 1)));
	                big2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
	        		        (float) (y + ((height - rhDist) - descent + 1)));	                
	            }    
	        }
	        
	        y = rulerHeight + editTierHeight + pixelsForTierHeightMargin - verticalScrollOffset;
	        // iterate over tiers
	        for (int i = 0; i < visibleTiers.size(); i++) {
	        	tier2d = visibleTiers.get(i);
	        	
		        if (tier2d != null) {
		        	
		        	if (y < rulerHeight + editTierHeight) {
			            y += pixelsForTierHeight;
			            continue;
		        	}
		        	
		            Iterator<Tag2D> tagIt = tier2d.getTags();
		
		            while (tagIt.hasNext()) {
		                tag2d = tagIt.next();
		
		                if (tag2d.getEndTime() < intervalBeginTime) {
		                    continue; //don't paint
		                } else if (tag2d.getBeginTime() > intervalEndTime) {
		                    break; // stop looping this tier
		                }
		
		                x = tag2d.getX();
		                width = tag2d.getWidth();
		                big2d.drawLine(x, y, x, y + height);
		                //big2d.drawLine(x, y + (height / 2), x + width, y +
		                //    (height / 2));
		                big2d.drawLine(x, y + (height - rhDist), x + width, y + (height - rhDist));
		                
		                big2d.drawLine(x + width, y, x + width, y + height);
		
		                int descent = big2d.getFontMetrics().getDescent();
	//	                big2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
	//	                    (float) (y + ((height / 2) - descent + 1)));
		                big2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
		        		        (float) (y + ((height - rhDist) - descent + 1)));	                
		            }
		            
	//	            if (i == 0) {
	//	            	y = rulerHeight + editTierHeight + pixelsForTierHeightMargin - verticalScrollOffset;
	//	            } else {
		            	y += pixelsForTierHeight;
		            //}
		        }
	        }
	        
	        big2d.setColor(Constants.SELECTIONCOLOR);
	        big2d.drawLine(ax, rulerHeight, ax + imageWidth, rulerHeight);
	        big2d.drawLine(ax, rulerHeight + editTierHeight, ax + imageWidth, rulerHeight + editTierHeight);
	        
	        y = rulerHeight + pixelsForTierHeightMargin;
	
	//        y = rulerHeight + pixelsForTierHeightMargin;
	//        if (currentBeginTime > -1) {
	//        	big2d.setColor(Constants.SHAREDCOLOR6);
	//        	x = timeToPixels(currentBeginTime);
	//        	big2d.drawLine(x, y, x, y + editTierHeight - pixelsForTierHeightMargin);
	//        }       
	        
	        // end paint tags
	        big2d.setTransform(new AffineTransform());
        } // end of synchronized block
        repaint();
    }
    
    /**
     * Painting the component without the use of a BufferedImage.
     *  
     * @param g2d the graphics context
     */
    private void paintUnbuffered(Graphics2D g2d) {
    	int h = getHeight();
    	int w = getWidth();
    	intervalEndTime = intervalBeginTime + (int) (w * msPerPixel);
        // scrolling related fill
        g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        g2d.fillRect(0, 0, w, h);
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, rulerHeight, w, editTierHeight);
        
        // mark the area beyond the media time
        long dur = getMediaDuration();
        int xx = xAt(dur);
        
        if (intervalEndTime > dur) {
            if (!SystemReporting.isMacOS()) {
            	g2d.setColor(UIManager.getColor("Panel.background"));// problems on the mac
            } else {
            	g2d.setColor(Color.LIGHT_GRAY);
            }
            g2d.fillRect(xx, 0, w - xx, h);
        }
        /*paint time ruler */
        g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
        g2d.translate(-((int)(intervalBeginTime / msPerPixel)), 0);
        ruler.paint(g2d, intervalBeginTime, w, msPerPixel,
            SwingConstants.TOP);
        
        g2d.setFont(font);
        ///###
        // paint a slightly dif. background color for every other tier
        int y = rulerHeight + editTierHeight - verticalScrollOffset;
        int ax = timeToPixels(intervalBeginTime); // == -translation.x

        g2d.setColor(Constants.LIGHTBACKGROUNDCOLOR);
        if (intervalBeginTime < getMediaDuration()) {
        	int numStripes = Math.max(visibleTiers.size(), h / pixelsForTierHeight);
        	int yy = y;
	        for (int i = 0; i < numStripes; i++) {
	            if (i % 2 != 0) {
	            	yy = y + i * pixelsForTierHeight;
	            	if (yy < rulerHeight + editTierHeight) {
	            		if (yy + pixelsForTierHeight > rulerHeight + editTierHeight) {
	            			g2d.fillRect(ax, rulerHeight + editTierHeight, xx, 
	            					pixelsForTierHeight - (rulerHeight + editTierHeight - yy));
	            		} else {
	            			continue;
	            		}
	            	} else {
	            		g2d.fillRect(ax, y + i * pixelsForTierHeight, xx, pixelsForTierHeight);
	            	}
	            }
	        }
        }
        // paint selection Oct 2015
        if (selectionBeginPos != selectionEndPos) {
        	g2d.translate(ax, 0.0);
        	g2d.setColor(Constants.SELECTIONCOLOR);
        	g2d.setComposite(alpha04);
        	g2d.fillRect(selectionBeginPos, 0, (selectionEndPos - selectionBeginPos), imageHeight);
        	g2d.setComposite(AlphaComposite.SrcOver);
        	g2d.translate(-ax, 0.0);
        }
        
        g2d.setColor(Constants.SHAREDCOLOR6);
        int height = pixelsForTierHeight - (2 * pixelsForTierHeightMargin);
        y = rulerHeight + pixelsForTierHeightMargin + (pixelsForTierHeight / 2);
        int x;
        int width;
        Tag2D tag2d;
        Tier2D tier2d = null;
        // paint the active tier + annotations
        if (editTier2d != null) {
            Iterator<Tag2D> tagIt = editTier2d.getTags();
        	
            while (tagIt.hasNext()) {
                tag2d = tagIt.next();

                if (tag2d.getEndTime() < intervalBeginTime) {
                    continue; //don't paint
                } else if (tag2d.getBeginTime() > intervalEndTime) {
                    break; // stop looping this tier
                }

                x = tag2d.getX();
                width = tag2d.getWidth();
                g2d.drawLine(x, y, x, y + height);
                //g2d.drawLine(x, y + (height / 2), x + width, y +
                //    (height / 2));
                g2d.drawLine(x, y + (height - rhDist), x + width, y + (height - rhDist));
                
                g2d.drawLine(x + width, y, x + width, y + height);

                int descent = g2d.getFontMetrics().getDescent();
//                g2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
//                    (float) (y + ((height / 2) - descent + 1)));
                g2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
        		        (float) (y + ((height - rhDist) - descent + 1)));	                
            }    
        }
        
        y = rulerHeight + editTierHeight + pixelsForTierHeightMargin - verticalScrollOffset;
        // iterate over tiers
        for (int i = 0; i < visibleTiers.size(); i++) {
        	tier2d = visibleTiers.get(i);
        	
	        if (tier2d != null) {
	        	
	        	if (y < rulerHeight + editTierHeight) {
		            y += pixelsForTierHeight;
		            continue;
	        	}
	        	
	            Iterator<Tag2D> tagIt = tier2d.getTags();
	
	            while (tagIt.hasNext()) {
	                tag2d = tagIt.next();
	
	                if (tag2d.getEndTime() < intervalBeginTime) {
	                    continue; //don't paint
	                } else if (tag2d.getBeginTime() > intervalEndTime) {
	                    break; // stop looping this tier
	                }
	
	                x = tag2d.getX();
	                width = tag2d.getWidth();
	                g2d.drawLine(x, y, x, y + height);
	                //g2d.drawLine(x, y + (height / 2), x + width, y +
	                //    (height / 2));
	                g2d.drawLine(x, y + (height - rhDist), x + width, y + (height - rhDist));
	                
	                g2d.drawLine(x + width, y, x + width, y + height);
	
	                int descent = g2d.getFontMetrics().getDescent();
//	                g2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
//	                    (float) (y + ((height / 2) - descent + 1)));
	                g2d.drawString(tag2d.getTruncatedValue(), (float) (x + 4),
	        		        (float) (y + ((height - rhDist) - descent + 1)));	                
	            }
	            
//	            if (i == 0) {
//	            	y = rulerHeight + editTierHeight + pixelsForTierHeightMargin - verticalScrollOffset;
//	            } else {
	            	y += pixelsForTierHeight;
	            //}
	        }
        }
        
        g2d.setColor(Constants.SELECTIONCOLOR);
        g2d.drawLine(ax, rulerHeight, ax + imageWidth, rulerHeight);
        g2d.drawLine(ax, rulerHeight + editTierHeight, ax + imageWidth, rulerHeight + editTierHeight);
        
        y = rulerHeight + pixelsForTierHeightMargin;

//        y = rulerHeight + pixelsForTierHeightMargin;
//        if (currentBeginTime > -1) {
//        	big2d.setColor(Constants.SHAREDCOLOR6);
//        	x = timeToPixels(currentBeginTime);
//        	big2d.drawLine(x, y, x, y + editTierHeight - pixelsForTierHeightMargin);
//        }       
        
        // end paint tags
        //g2d.setTransform(new AffineTransform());
        g2d.translate(ax, 0.0);
        if (currentBeginTime > -1) {
        	y = rulerHeight + (2 * pixelsForTierHeightMargin);        	
        	//int x = timeToPixels(currentBeginTime);
        	x = xAt(currentBeginTime);
        	if (x >= 0 && x <= w) {
        		g2d.setColor(Constants.SHAREDCOLOR6);
        		g2d.drawLine(x, y, x, y + editTierHeight - (2 * pixelsForTierHeightMargin));
        	}
        }
        
        if (dragEditTag2D != null) {
        	paintEditTag(g2d);
        }
        
        if ((crossHairPos >= 0) && (crossHairPos <= imageWidth)) {
            // prevents drawing outside the component on Mac
            g2d.setColor(Constants.CROSSHAIRCOLOR);
            g2d.drawLine(crossHairPos, 0, crossHairPos, h);
        }

        //Toolkit.getDefaultToolkit().sync();
    }
    
    private void paintEditTag(Graphics2D g2d) {
    	if (dragEditTag2D != null) {
    		int x = (int) (dragEditTag2D.getX() -
	                (intervalBeginTime / msPerPixel));
    		int y = timeRulerVisible ? rulerHeight +  2 * pixelsForTierHeightMargin : 2 * pixelsForTierHeightMargin;
    		int h = editTierHeight - (4 * pixelsForTierHeightMargin);
    		g2d.setColor(Color.GREEN);
    		g2d.fillRect(x, y, 2, h);
    		g2d.fillRect(x + dragEditTag2D.getWidth(), y, 2, h);
    		g2d.fillRect(x, y + (h / 2), dragEditTag2D.getWidth() , 2);
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

            if (stringWidth > (width - 4)) { // truncate

                int i = 0;
                String s = "";
                int size = line.length();

                while (i < size) {
                    if (fMetrics.stringWidth(s) > (width - 4)) {
                        break;
                    } else {
                        s = s + line.charAt(i++);
                    }
                }

                if (!s.equals("")) {
                    line = s.substring(0, s.length() - 1);
                } else {
                    line = s;
                }
            }
        }

        return line;
    }
    
	@Override
	public int getRightMargin() {
		return defBarWidth;
	}
	
	/**
	 * Overrides super's implementation by adding a font size menu.
	 */
	@Override
	protected void createPopupMenu() {
		super.createPopupMenu();
		
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

		if (popup.getComponentCount() >= 2) {
			popup.add(fontMenu, 1);	
		} else {
			popup.add(fontMenu);
		}
		
		popup.addSeparator();
		
        deleteAnnoMI = new JMenuItem(actionMap.get(ELANCommandFactory.DELETE_ANNOTATION));  
        deleteAnnoMI.setEnabled(false);     
        popup.add(deleteAnnoMI);  
        
        mergeNextAnnoMI = new JMenuItem(actionMap.get(ELANCommandFactory.MERGE_ANNOTATION_WN)); 
        mergeNextAnnoMI.setEnabled(false);  
        popup.add(mergeNextAnnoMI);
        
        mergeBeforeAnnoMI = new JMenuItem(actionMap.get(ELANCommandFactory.MERGE_ANNOTATION_WB));        
        mergeBeforeAnnoMI.setEnabled(false);       
        popup.add(mergeBeforeAnnoMI);
       
        splitAnnoMI = new JMenuItem(ElanLocale.getString(ELANCommandFactory.SPLIT_ANNOTATION));
        splitAnnoMI.setAction(actionMap.get(ELANCommandFactory.SPLIT_ANNOTATION));
        splitAnnoMI.setEnabled(false);        
        popup.add(splitAnnoMI);
        
        modifyAnnoTimeDlgMI = new JMenuItem(actionMap.get(ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG));  
        modifyAnnoTimeDlgMI.setEnabled(false);     
        popup.add(modifyAnnoTimeDlgMI);
	}
	
	/** 
	 * Check whether the delete annotation item should be enabled.
	 */
	@Override
	protected void updatePopup(Point p) {
		if (popup == null) {
			return;
		}
		boolean isEditAnnotation = (dragEditTag2D != null);
		deleteAnnoMI.setEnabled(isEditAnnotation);
		 modifyAnnoTimeDlgMI.setEnabled(isEditAnnotation);
		checkForSplitAnnotation();
		if(isEditAnnotation){
			Annotation aa = dragEditTag2D.getAnnotation();
			TierImpl tier = (TierImpl) aa.getTier();     
			mergeBeforeAnnoMI.setEnabled(tier.getAnnotationBefore(aa) != null);
			mergeNextAnnoMI.setEnabled(tier.getAnnotationAfter(aa) != null);
			
		} else{
			mergeBeforeAnnoMI.setEnabled(false);
			mergeNextAnnoMI.setEnabled(false);
		}
		
		 Point inverse = new Point(p);

         //compensate for the intervalBeginTime
         inverse.x += timeToPixels(intervalBeginTime);
         inverse.y += verticalScrollOffset;
         splitTime = pixelToTime(inverse.x);
	}
	
	 /**
     * Enables / Disables the split annotation option in the 
     * popup
     */
    
    private void checkForSplitAnnotation(){
    	splitAnnoMI.setEnabled(false);
    	if(dragEditTag2D != null){
    		Annotation aa = dragEditTag2D.getAnnotation();
			TierImpl tier = (TierImpl) aa.getTier();         		   
 	  	   	
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
 	  	   			splitAnnoMI.setEnabled(valid);
 	  	   			
 	  	   			if(!valid){
 	  	   				List<TierImpl> dependentTiers = tier.getDependentTiers();
 	  	   				List<Annotation> dependingAnnotations = new ArrayList<Annotation>();
 	  	   				for(int i=0;i < dependentTiers.size();i++){
 	  	   					if( dependentTiers.get(i).getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
 	  	   						continue; 
 	  	   					}
 	  	   					dependingAnnotations.addAll(aa.getChildrenOnTier(dependentTiers.get(i)));
 	  	   				}	
 	  	   				if(dependingAnnotations.size() == 0){	  			 
 	  	   					splitAnnoMI.setEnabled(true);
 	  	   				} 
 	  	   			}
 	  	   		}
 	  	   	}
    	}
	 }

	/**
     * DOCUMENT ME!
     *
     * @param controller DOCUMENT ME!
     */
    @Override
	public void setMultiTierControlPanel(MultiTierControlPanel controller) {
    	// stub this viewer has a specialized control panel
    }
    
    public void setSegmentationControlPanel(SegmentationControlPanel segment) {
    	segmentationControlPanel = segment;
    	notifySegmentationControlPanel();
    }
    
    // methods to set segmentation mode and fixed time
    /**
     * Sets the segmentation mode to one of the constants.
     * @param nextMode the new mode for segmentation
     */
    public void setSegmentationMode(int nextMode) {
    	if (nextMode == segmentMode) {
    		return;
    	}
    	
    	// TODO stop player??
    	if (nextMode >= TWO_TIMES_SEGMENTATION && nextMode <= TWO_TIMES_PRESS_RELEASE_SEGMENTATION) {
    		int oldMode = segmentMode;
    		segmentMode = nextMode;
            timeCount = 0;
            lastSegmentTime = -1;
            
            if (segmentMode == TWO_TIMES_PRESS_RELEASE_SEGMENTATION || 
            		(segmentMode != TWO_TIMES_PRESS_RELEASE_SEGMENTATION && 
            		oldMode == TWO_TIMES_PRESS_RELEASE_SEGMENTATION)) {
            	// remove and add actions
            	switchActions();
            }
                     
    	}
    }
    
    /**
     * Removes one type of segmentation action(s) from the input and action maps and adds 
     * the alternative one(s).
     * Disabling and enabling didn't seem to work (when the two press/release actions had to be 
     * enabled again only one would be enabled.)
     */
    private void switchActions() {
    	if (segmentMode == TWO_TIMES_PRESS_RELEASE_SEGMENTATION) {
    		mainInputMap.remove((KeyStroke) segmentTypeAction.getValue(Action.ACCELERATOR_KEY));
    		mainActionMap.remove(ELANCommandFactory.SEGMENT);
    		getInputMap(WHEN_FOCUSED).remove((KeyStroke) segmentTypeAction.getValue(Action.ACCELERATOR_KEY));
    		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove((KeyStroke) segmentTypeAction.getValue(Action.ACCELERATOR_KEY));
    		
    		segmentPressAction = new PressReleaseSegmentAction(false);
            String key = "SegDown";

            mainInputMap.put((KeyStroke) segmentPressAction.getValue(Action.ACCELERATOR_KEY), key);
            mainActionMap.put(key, segmentPressAction);
            getInputMap(WHEN_FOCUSED).put((KeyStroke) segmentPressAction.getValue(Action.ACCELERATOR_KEY), key);
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put((KeyStroke) segmentPressAction.getValue(Action.ACCELERATOR_KEY), key);
            
            segmentReleaseAction = new PressReleaseSegmentAction(true);
            key = "SegUp";

            mainInputMap.put((KeyStroke) segmentReleaseAction.getValue(Action.ACCELERATOR_KEY), key);
            mainActionMap.put(key, segmentReleaseAction);
            getInputMap(WHEN_FOCUSED).put((KeyStroke) segmentReleaseAction.getValue(Action.ACCELERATOR_KEY), key);
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put((KeyStroke) segmentReleaseAction.getValue(Action.ACCELERATOR_KEY), key);                       
    	} else {
    		String key = "SegDown";
    		mainInputMap.remove((KeyStroke) segmentPressAction.getValue(Action.ACCELERATOR_KEY));
    		mainActionMap.remove(key);
    		getInputMap(WHEN_FOCUSED).remove((KeyStroke) segmentPressAction.getValue(Action.ACCELERATOR_KEY));
    		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove((KeyStroke) segmentPressAction.getValue(Action.ACCELERATOR_KEY));
    		
    		key = "SegUp";
    		mainInputMap.remove((KeyStroke) segmentReleaseAction.getValue(Action.ACCELERATOR_KEY));
    		mainActionMap.remove(key);
    		getInputMap(WHEN_FOCUSED).remove((KeyStroke) segmentReleaseAction.getValue(Action.ACCELERATOR_KEY));
    		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove((KeyStroke) segmentReleaseAction.getValue(Action.ACCELERATOR_KEY));
    		
    		// add the default segmentation action again
    		key = ELANCommandFactory.SEGMENT;
            mainInputMap.put((KeyStroke) segmentTypeAction.getValue(Action.ACCELERATOR_KEY), key);
            mainActionMap.put(key, segmentTypeAction);
            getInputMap(WHEN_FOCUSED).put((KeyStroke) segmentTypeAction.getValue(Action.ACCELERATOR_KEY), key);
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put((KeyStroke) segmentTypeAction.getValue(Action.ACCELERATOR_KEY), key);
    	}
    }
    /**
     * Sets the duration of new segments/annotations. Only used in combination 
     * with the fixed segmentation mode.
     * @param duration the duration for segmentations
     */
    public void setFixedSegmentDuration(long duration) {
    	fixedDuration = duration;
    }
    
    /**
     * Returns whether the keystroke in fixed duration segmentation mode marks 
     * the begin or the end time
     * 
     * @return whether the key stroke marks the begin or end of an annotation
     */
    public boolean isSingleStrokeIsBegin() {
		return singleStrokeIsBegin;
	}

    /**
     * Sets whether the keystroke in fixed duration segmentation mode marks 
     * the begin or the end time
     * 
     * @param singleStrokeIsBegin whether the key stroke marks the begin or end of an annotation
     */
	public void setSingleStrokeIsBegin(boolean singleStrokeIsBegin) {
		this.singleStrokeIsBegin = singleStrokeIsBegin;
	}
	
	/**
	 * Sets the number of ms. the annotation has to be created before the time the 
	 * segmentation key has been pressed.
	 *  
	 * @param delayDuration the delay value
	 */
	public void setDelayDuration(long delayDuration) {
		this.delayDuration = delayDuration;
		if (delayDuration < 0) {
			delayDuration = -delayDuration;
		}
	}

	/**
     * Add the begin or end boundary of an annotation and the value of the CV entry.
     */
    public void addSegmentTime(CVEntry cve) {

        long cur = getMediaTime() - delayDuration;
        if (cur < 0) {
        	cur = 0;
        }

        if (segmentMode == TWO_TIMES_SEGMENTATION) {
            if (cur != lastSegmentTime) {
                timeCount++;

                if ((timeCount % 2) != 0) {
                    lastSegmentTime = cur;
                    setCurrentBeginTime(lastSegmentTime);
                } else {
                	if (cve != null) {
                		AnnotationDataRecord adr;
                		if (cur > lastSegmentTime) {
                			adr = new AnnotationDataRecord(curTier, cve.getValue(), lastSegmentTime, cur);
                		} else {
                			adr = new AnnotationDataRecord(curTier, cve.getValue(), cur, lastSegmentTime);
                		}
                		adr.setCvEntry(cve);
                		
                		if (adr.getEndTime() - adr.getBeginTime() > 0) {
                			//timeSegments.add(adr);
                			//previewer.addSegment(adr);
                			createAnnotation(adr);
                		}
                	} else {
	                    TimeInterval ti;
	                    if (cur > lastSegmentTime) {
	                    	ti = new TimeInterval(lastSegmentTime, cur);
	                    } else {
	                        ti = new TimeInterval(cur, lastSegmentTime);
	                    }
	
	                    if (ti.getDuration() > 0) {
	                        //timeSegments.add(ti);
	                        //previewer.addSegment(ti);
	                        createAnnotation(ti);
	                    }
                	}

                    lastSegmentTime = cur;
                    setCurrentBeginTime(-1);
                }
            } else {
            	timeCount++;

                if ((timeCount % 2) != 0) {
                    lastSegmentTime = cur;
                    setCurrentBeginTime(lastSegmentTime);
                } else {
                	// cannot create an annotation with duration 0, but make sure the next time
                	// at another position is detected as the end time
                	timeCount++;
                }
            }
        } else if (segmentMode == ONE_TIME_SEGMENTATION) {
            if (lastSegmentTime == -1) {
                timeCount++;
                lastSegmentTime = cur;
                setCurrentBeginTime(lastSegmentTime);
            } else {
                if (cur != lastSegmentTime) {
                    timeCount++;
                    if (cve != null) {
                		AnnotationDataRecord adr;
                		if (cur > lastSegmentTime) {
                			adr = new AnnotationDataRecord(curTier, cve.getValue(), lastSegmentTime, cur);
                		} else {
                			adr = new AnnotationDataRecord(curTier, cve.getValue(), cur, lastSegmentTime);
                		}
                		adr.setCvEntry(cve);
                		
                		if (adr.getEndTime() - adr.getBeginTime() > 0) {
                			//timeSegments.add(adr);
                			//previewer.addSegment(adr);
                			createAnnotation(adr);
                		}
                    } else {
	                    TimeInterval ti;
	                    if (cur > lastSegmentTime) {
	                    	ti = new TimeInterval(lastSegmentTime, cur);
	                    } else {
	                        ti = new TimeInterval(cur, lastSegmentTime);
	                    }
	
	                    if (ti.getDuration() > 0) {
	                        //timeSegments.add(ti);
	                        //previewer.addSegment(ti);
	                        createAnnotation(ti);
	                    }
                    }
                    setCurrentBeginTime(-1);
                    lastSegmentTime = cur;
                }
            }
        } else {
            // ONE_TIME_FIXED_SEGMENTTATION mode
        	if (cve != null) {
        		AnnotationDataRecord adr;
        		
                if (singleStrokeIsBegin) {
                    adr = new AnnotationDataRecord(curTier, cve.getValue(), cur, cur + fixedDuration);
                } else {
                    long bb = cur - fixedDuration;

                    if (bb < 0) {
                        bb = 0;
                    }

                    adr = new AnnotationDataRecord(curTier, cve.getValue(), bb, cur);
                }
        		adr.setCvEntry(cve);
                
                if (adr.getEndTime() - adr.getBeginTime() > 0) {
                    //timeSegments.add(adr);
                    //previewer.addSegment(adr);
                    createAnnotation(adr);
                }
        	} else {
	            TimeInterval ti = null;
	
	            if (singleStrokeIsBegin) {
	                ti = new TimeInterval(cur, cur + fixedDuration);
	            } else {
	                long bb = cur - fixedDuration;
	
	                if (bb < 0) {
	                    bb = 0;
	                }
	
	                ti = new TimeInterval(bb, cur);
	            }
	
	            if (ti.getDuration() > 0) {
	                //timeSegments.add(ti);
	                //previewer.addSegment(ti);
	                createAnnotation(ti);
	            }
        	}

            lastSegmentTime = cur;
        }	
    }
    //####  addition for key press - key release segmentation ####
    /**
     * Implementation that is similar to the TWO_TIMES_SEGMENTATION mode but this
     * behavior is independent of the selected mode.
     * Might need revision? 
     * 
     * @param release if true the action for a key release is performed (finish a segment),
     * otherwise the begin time of a new segment is marked.
     */
    private void addPressReleaseTime(boolean release) {
        long cur = getMediaTime() - delayDuration;
        if (cur < 0) {
        	cur = 0;
        }
        
        if (!release) {// key down event, mark begin
        	lastSegmentTime = cur;
            setCurrentBeginTime(lastSegmentTime);
        } else {//key up, create annotation
            TimeInterval ti;
            if (cur > lastSegmentTime) {// should always be true when called in the KEY_PRESS_KEY_RELEASE mode
            	ti = new TimeInterval(lastSegmentTime, cur);
            } else {
                ti = new TimeInterval(cur, lastSegmentTime);
            }

            if (ti.getDuration() > 0) {
                createAnnotation(ti);
            }
            
            lastSegmentTime = cur;
            setCurrentBeginTime(-1);
        }

    }
    // #### end addition ####
    
    /**
     * Calculate the y positions of the vertical middle of all visible tiers
     * and pass them to the MultiViewerController.
     */
    private void notifySegmentationControlPanel() {
        if (segmentationControlPanel == null) {
            return;
        }

        segmentationControlPanel.setEditTierHeight(rulerHeight + editTierHeight);
        segmentationControlPanel.setTierHeight(pixelsForTierHeight);
        if (segmentationControlPanel.getFont().getSize() != font.getSize()) {
        	segmentationControlPanel.setFont(segmentationControlPanel.getFont().deriveFont((float) font.getSize()));
        }
        
        if (tierYPositions.length != visibleTiers.size() + 1) {
        	tierYPositions = new int[visibleTiers.size() + 1];
        }
        if (tierYPositions.length > 0) {
            tierYPositions[0] = (rulerHeight + (editTierHeight / 2));

            for (int i = 0; i < visibleTiers.size(); i++) {
//                tierYPositions[i] = rulerHeight + editTierHeight + pixelsForTierHeight - pixelsForTierHeightMargin - rhDist +
//                    ((i - 1) * pixelsForTierHeight) - verticalScrollOffset;
            	tierYPositions[i + 1] = rulerHeight + editTierHeight + ((i + 1) * pixelsForTierHeight) - verticalScrollOffset;
            }
        }

        segmentationControlPanel.setTierPositions(tierYPositions);
    }
    
    /**
     * Update the values of the scrollbar.<br>
     * Called after a change in the number of visible tiers.
     */
    private void updateScrollBar() {
        int value = scrollBar.getValue();
        int max = (visibleTiers.size() * pixelsForTierHeight) + rulerHeight + editTierHeight;
        scrollBar.removeAdjustmentListener(this);
        // before changing scrollbar values do a setValue(0), otherwise
        // setMaximum and/or setVisibleAmount will not be accurate
        scrollBar.setValue(0);
        scrollBar.setMaximum(max);
        scrollBar.setVisibleAmount(getHeight() - defBarWidth);

        if ((value + getHeight()) > max) {
            value = max - getHeight();
        }

        scrollBar.setValue(value);
        scrollBar.revalidate();
        scrollBar.addAdjustmentListener(this);
        verticalScrollOffset = scrollBar.getValue();
        notifySegmentationControlPanel();
    }
    
    /**
     * Updates the values of the horizontal scrollbar. Called when the interval begin time, the 
     * resolution (msPerPixel), the viewer's width or the master media duration has changed. 
     */
    private void updateHorScrollBar() {
        int value = hScrollBar.getValue();
        if (value != (int)(intervalBeginTime / msPerPixel)) {
            value = (int)(intervalBeginTime / msPerPixel);
        }
        int max = (int) (getMediaDuration() / msPerPixel + DEFAULT_MS_PER_PIXEL);
        hScrollBar.removeAdjustmentListener(this);
        
//        hScrollBar.setValue(0);
//        hScrollBar.setMaximum(max);
//        hScrollBar.setVisibleAmount(getWidth() - defBarWidth);
//        if (value != hScrollBar.getValue()) {
//            hScrollBar.setValue(value);
//        }
        // this should only be necessary after a resize
        hScrollBar.setBlockIncrement(getWidth() - defBarWidth);
        hScrollBar.setValues(value, getWidth() - defBarWidth, 0, max);
        hScrollBar.revalidate();
        hScrollBar.addAdjustmentListener(this);
    }
    
	@Override
	protected void setLocalTimeScaleIntervalBeginTime(long begin) {
        if (begin == intervalBeginTime) {
            return;
        }

        //if (playerIsPlaying()) {
            intervalBeginTime = begin;
            intervalEndTime = (long) (intervalBeginTime + (imageWidth * msPerPixel));

            crossHairPos = xAt(crossHairTime);
            selectionBeginPos = xAt(getSelectionBeginTime());
            selectionEndPos = xAt(getSelectionEndTime());
       /* } else {
            if (!panMode) {
                //intervalBeginTime = (long) (begin - (SCROLL_OFFSET * msPerPixel));
            	intervalBeginTime = begin;
            } else {
                intervalBeginTime = begin;
            }

            if (intervalBeginTime < 0) {
                intervalBeginTime = 0;
            }

            intervalEndTime = (long) (intervalBeginTime + (imageWidth * msPerPixel));

            crossHairPos = xAt(crossHairTime);
        }*/
        updateHorScrollBar();
        paintBuffer();
	}

	@Override
	protected void setLocalTimeScaleMsPerPixel(float step) {
		if (step == msPerPixel) {
			return;
		}
		if (step >= TimeScaleBasedViewer.MIN_MSPP) {
            msPerPixel = step;
        } else {
            msPerPixel = TimeScaleBasedViewer.MIN_MSPP;
        }

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
        int zoom = (int) (100f * (10f / msPerPixel));

        if (zoom <= 0) {
            zoom = 100;
        }

        updateZoomPopup(zoom);
        
        setPreference("SegmentationViewer.ZoomLevel", new Float(zoom), 
        		transcription);
        
        if (zoomSlider != null) {
        	if (zoom <= 100) {
        		zoomSlider.setValue(zoom);
        	} else {
        		// recalculate for > 100 values.
        		float factor = (zoom - 100) / (float) 900;
        		int zm = 100 + (int) ( factor * (zoomSlider.getMaximum() - 100) );
        		if (zm != zoomSlider.getValue()) {
        			zoomSlider.removeChangeListener(this);
        			zoomSlider.setValue(zm);
        			zoomSlider.addChangeListener(this);
        			zoomSlider.setToolTipText(String.valueOf(zoom));
        		}
        	}
        }
	}

	/**
	 * Note that this method may running on a user thread, not on the Event
	 * Dispatching Thread.
	 * 
     * @see mpi.eudico.client.annotator.AbstractViewer#controllerUpdate(mpi.eudico.client.annotator.ControllerEvent)
     */
    @Override
	public synchronized void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent || event instanceof StopEvent) {
            crossHairTime = getMediaTime();

//            System.out.println("SV time: " + crossHairTime);
//            System.out.println("IB time: " + intervalBeginTime);
//            System.out.println("IE time: " + intervalEndTime);
            
            if (!playerIsPlaying()) {
                recalculateInterval(crossHairTime);
                crossHairPos = xAt(crossHairTime);
                repaint();
            } else {
                long intervalMidTime = (intervalBeginTime + intervalEndTime) / 2;

                if (crossHairTime > (intervalMidTime + (1 * msPerPixel))) {
                    setIntervalBeginTime(intervalBeginTime +
                        (crossHairTime - intervalMidTime));
                } else if (!useBufferedImage) {
                	crossHairPos = xAt(crossHairTime);
                	repaint();
                }
                else if (crossHairTime < intervalMidTime) {
                    int oldPos = crossHairPos;
                    crossHairPos = xAt(crossHairTime);
                    if (useBufferedImage) {
	                    if (crossHairPos >= oldPos) {
	                        repaint(oldPos - 2, 0, crossHairPos - oldPos + 4,
	                            getHeight());
	                    } else {
	                        repaint(crossHairPos - 2, 0, oldPos - crossHairPos + 4,
	                            getHeight());
	                    }
                    } else {
                    	repaint();
                    }
                } else {
                    repaint();
                }
            }
            
        }
    }

    /**
     * Calculates the x-coordinates of begin and end of the selection.
     *
     * @see mpi.eudico.client.annotator.AbstractViewer#updateSelection()
     */
    @Override
	public void updateSelection() {
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());
        paintBuffer();
    }

    /**
     * Stub.
     *
     * @see mpi.eudico.client.annotator.AbstractViewer#updateLocale()
     */
    @Override
	public void updateLocale() {
    	if (segmentationControlPanel != null) {
    		segmentationControlPanel.updateLocale();
    	}
    	super.updateLocale();
    }
    
    /**
     * Edit events in the document.
     */
	@Override
	public void ACMEdited(ACMEditEvent e) {
        //System.out.println("ACMEdited:: operation: " + e.getOperation() + ", invalidated: " + e.getInvalidatedObject());
        //System.out.println("\tmodification: " + e.getModification() + ", source: " + e.getSource());
        switch (e.getOperation()) {
        case ACMEditEvent.ADD_TIER:

            if (e.getModification() instanceof TierImpl) {
                tierAdded((TierImpl) e.getModification());

                if (segmentationControlPanel != null) {
                    segmentationControlPanel.addTier((TierImpl) e.getModification());
                }
            }

            break;

        case ACMEditEvent.REMOVE_TIER:

            if (e.getModification() instanceof TierImpl) {
                tierRemoved((Tier) e.getModification());

                if (segmentationControlPanel != null) {
                    segmentationControlPanel.removeTier((TierImpl) e.getModification());
                }
            }

            break;
            
        case ACMEditEvent.CHANGE_TIER:

            if (e.getInvalidatedObject() instanceof TierImpl) {
                tierChanged((TierImpl) e.getInvalidatedObject());

                if (segmentationControlPanel != null) {
                	segmentationControlPanel.changeTier((TierImpl) e.getInvalidatedObject());
                }
            }

            break;
        case ACMEditEvent.ADD_ANNOTATION_HERE:

            if (e.getInvalidatedObject() instanceof TierImpl &&
                    e.getModification() instanceof Annotation) {
                TierImpl invTier = (TierImpl) e.getInvalidatedObject();
                List<TierImpl> depTiers = invTier.getDependentTiers();

                if (depTiers == null) {
                    depTiers = new ArrayList<TierImpl>();
                }

                depTiers.add(0, invTier);
                //annotationsAdded(depTiers);
            	annotationAdded(invTier, (Annotation) e.getModification());
            	// hier check if a cv entry value needs to be added to the new annotation?
            	if (lastRecord != null) {
            		Command c = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MODIFY_ANNOTATION);
            		Object[] args = new Object[] { "", lastRecord.getValue(), lastRecord.getExtRef(), lastRecord.getCvEntryId() };
            		c.execute(e.getModification(), args);
            		lastRecord = null;
            	}
            }
            
            break;
        case ACMEditEvent.CHANGE_ANNOTATION_VALUE:

            if (e.getSource() instanceof Annotation) {
                Annotation a = (Annotation) e.getSource();
                annotationChanged(a);
            }
            
            break;
        case ACMEditEvent.CHANGE_ANNOTATION_TIME:
        	// fall through, other annotations may have been modified by this change
        case ACMEditEvent.REMOVE_ANNOTATION:
        	// fall through
        case ACMEditEvent.CHANGE_ANNOTATIONS:
        	// modification is null, source is transcription
        	transcriptionChanged();
            
            break;
        }
		
	}

	/**
	 * Update the current tier after an annotation has been added.
	 * 
	 * @param tier the changed tier
	 * @param ann the new annotation
	 */
	private void annotationAdded(Tier tier, Annotation ann) {
		Tier2D tier2d;
		for (int i = 0; i < allTiers.size(); i++) {
			tier2d = allTiers.get(i);
			
			if (tier2d.getTier() == tier) {
				long bt = ann.getBeginTimeBoundary();
				long et = ann.getEndTimeBoundary();
				int bx = timeToPixels(bt);
				int ex = timeToPixels(et);
				
				List<Tag2D> annos = tier2d.getTagsList();
				int size = annos.size();
				Tag2D tag2d;
				boolean overlap = false;
				//start at the end
				for (int j = size - 1; j >= 0; j--) {
					tag2d = annos.get(j);
					// compare on the basis of coordinates instead of times					
					if (tag2d.getX() + tag2d.getWidth() <= bx) {
						break;
					}
					if (tag2d.getX() >= ex) {
						continue;
					}
					
					if (tag2d.getX() >= bx && tag2d.getX() + tag2d.getWidth() <= ex) {
						tier2d.removeTag(tag2d);
						overlap = true;
					} else {
						// some kind of overlap, update
						tag2d.setX(timeToPixels(tag2d.getBeginTime()));
						tag2d.setWidth(timeToPixels(tag2d.getEndTime()) - tag2d.getX());
						tag2d.setTruncatedValue(truncateString(tag2d.getValue(), tag2d.getWidth(),
		                    metrics));
						overlap = true;
					}

				}
				
				tag2d = new Tag2D(ann); 
	            int xPos = timeToPixels(bt);
	            tag2d.setX(xPos);
	            int tagWidth = timeToPixels(et) - xPos;
	            tag2d.setWidth(tagWidth);
	            tag2d.setTruncatedValue(truncateString(ann.getValue(), tagWidth,
	                    metrics));
	            tier2d.insertTag(tag2d);
		            
				if (overlap) {
					// check child tiers in the list, or not necessary? If child annotations have been removed
					// all tiers are re-extracted?
				}
				break;
			}
		}
		paintBuffer();
	}
	
	/**
	 * Updates the label of an annotation
	 * @param ann
	 */
	private void annotationChanged(Annotation ann) {
		if (ann == null) {
			return;
		}
		Tier2D tier2d = null;
		
		if (editTier2d.getTier() == ann.getTier()) {
			tier2d = editTier2d;
		} else {
			Tier2D t2d = null;
			for (int i = 0; i < allTiers.size(); i++) {
				t2d = allTiers.get(i);
					
				if (t2d.getTier() == ann.getTier()) {
					tier2d = t2d;
					break;
				}
			}
		}
		
		if (tier2d != null) {
			List<Tag2D> annos = tier2d.getTagsList();
			int size = annos.size();
			Tag2D tag2d;
			//start at the end
			for (int j = size - 1; j >= 0; j--) {
				tag2d = annos.get(j);
				if (tag2d.getAnnotation() == ann) {
					tag2d.setTruncatedValue(truncateString(ann.getValue(), tag2d.getWidth(), metrics));
					break;
				}
			}
		}
	}
	
	/**
	 * Creates a Tier2D, updates the tierYpositions array and waits for a call to setVisibleTiers
	 * @param tier
	 */
	private void tierAdded(TierImpl tier) {
		// check if it is already there
        if (tier.hasParentTier() && (tier.getLinguisticType().getConstraints() != null && 
        		tier.getLinguisticType().getConstraints().getStereoType() != Constraint.INCLUDED_IN)){
        	return;
        }
        
		boolean found = false;
		for (Tier2D t2d : allTiers) {
			if (t2d.getTier() == tier || t2d.getTier().getName().equals(tier.getName())) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			Tier2D tier2d = createTier2D(tier);
			if (tier2d != null) {
				allTiers.add(tier2d);
				tierYPositions = new int[allTiers.size()];
			}
		}
	}

	/**
	 * Removes a tier, resets the y positions and waits for a call to setVisibleTiers.
	 * @param tier
	 */
	private void tierRemoved(Tier tier) {
		Tier2D tier2d;
		for(int i = 0; i < allTiers.size(); i++) {
			tier2d = allTiers.get(i);
			
			if (tier2d.getTier() == tier) {
				allTiers.remove(i);
				visibleTiers.remove(tier2d);
				
				if (tier2d == editTier2d) {
					if (visibleTiers.size() > 0) {
						setActiveTier(visibleTiers.get(0).getTier());
					} else if (allTiers.size() > 0) {
						setActiveTier(allTiers.get(0).getTier());
					}
				}
				tierYPositions = new int[allTiers.size()];
				break;
			}
		}
	}
	
    /**
     * Check the name of the Tier2D object of the specified Tier.
     *
     * @param tier the Tier that has been changed
     */
    private void tierChanged(TierImpl tier) {
		Tier2D tier2d;
		for(int i = 0; i < allTiers.size(); i++) {
			tier2d = allTiers.get(i);
			
			if (tier2d.getTier() == tier) {
	            if (!tier.hasParentTier() || (tier.getLinguisticType().getConstraints() != null && 
	            		tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN)){
	            	tier2d.updateName();
	            } else {
					allTiers.remove(i);
					visibleTiers.remove(tier2d);
					
					if (tier2d == editTier2d) {
						if (visibleTiers.size() > 0) {
							setActiveTier(visibleTiers.get(0).getTier());
						} else if (allTiers.size() > 0) {
							setActiveTier(allTiers.get(0).getTier());
						}
					}
					tierYPositions = new int[allTiers.size()];
					if (segmentationControlPanel != null) {
						segmentationControlPanel.removeTier(tier);
					}
	            }
				break;
			}
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
        Tier2D tier2d;

        for (int i = 0; i < allTiers.size(); i++) {
            tier2d = allTiers.get(i);
            reextractTier(tier2d);
        }

        paintBuffer();
    }
	
    /**
     * Restore position etc.
     */
	@Override
	public void preferencesChanged() {
		Integer fontSize = Preferences.getInt("SegmentationViewer.FontSize", 
				transcription);
		if (fontSize != null) {
			setFontSize(fontSize.intValue());
		}
		
		Float zoomLevel = Preferences.getFloat("SegmentationViewer.ZoomLevel", 
				transcription);
		if (zoomLevel != null) {
			float zl = zoomLevel.floatValue();
			if(zl > 5000.0){
				zl = 5000;
			}
            float newMsPerPixel =  ((100f / zl) * 10);
            setMsPerPixel(newMsPerPixel);
			updateZoomPopup((int)zl);
		}
		
		Boolean rulerVisible = Preferences.getBool("SegmentationViewer.TimeRulerVisible", transcription);
		if (rulerVisible != null) {
			setTimeRulerVisible(rulerVisible);
			if (timeRulerVisMI != null && timeRulerVisible != timeRulerVisMI.isSelected()) {
				timeRulerVisMI.setSelected(timeRulerVisible);
			}			
		}
		
		super.preferencesChanged();
	}
	
	public void shortcutsChanged(){
		this.updateShortcutMaps();
	}
	
	private void setPreference(String key, Object value, Transcription trans) {
		// re-use zoomlevel, time ruler visibility, connected flag??
		Preferences.set(key, value, trans, false, false);
	}
	
	private void createAnnotation(TimeInterval ti) {
		if (ti != null && editTier2d != null) {
			if (ti instanceof AnnotationDataRecord) {
				lastRecord = (AnnotationDataRecord) ti;
			} else {
				lastRecord = null;
			}

			Command c = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.NEW_ANNOTATION);
	        Object[] args = new Object[] {
	                new Long(ti.getBeginTime()), new Long(ti.getEndTime())
	            };
	        c.execute(editTier2d.getTier(), args);
		}
	}

    /**
     * Sets the current begin time for painting an indicator of the position 
     * of the first stroke of a segment.
     * 
     * @param time the current begin time
     */
    public void setCurrentBeginTime(long time) {
    	currentBeginTime = time;
    	paintBuffer();// hier in paint component?
    }
    
	@Override
	public void componentResized(ComponentEvent e) {
		//if (!useBufferedImage) {
			imageWidth = getWidth() - defBarWidth;
			intervalWidth = imageWidth;
	        intervalEndTime = intervalBeginTime + (int) (imageWidth * msPerPixel);
	        if (timeScaleConnected) {
	            setGlobalTimeScaleIntervalEndTime(intervalEndTime);
	        }
		//}
    	
        //paintBuffer();

        if (getWidth() > 2 * ZOOMSLIDER_WIDTH) {
	        hScrollBar.setBounds(0, getHeight() - defBarWidth, getWidth() - defBarWidth - ZOOMSLIDER_WIDTH, defBarWidth);
	        updateHorScrollBar();
	        scrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
	                    getHeight() - defBarWidth);
	        updateScrollBar();
	        if (!zoomSliderPanel.isVisible()) {
	        	zoomSliderPanel.setVisible(true);
	        }
	        zoomSliderPanel.setBounds(hScrollBar.getWidth(), getHeight() - defBarWidth, ZOOMSLIDER_WIDTH, defBarWidth);
	        zoomSlider.setBounds(0, 0, ZOOMSLIDER_WIDTH, defBarWidth);
	        corner.setBounds(getWidth() - defBarWidth, getHeight() - defBarWidth, 
	        		defBarWidth, defBarWidth);
        } else {
        	zoomSliderPanel.setVisible(false);
	        hScrollBar.setBounds(0, getHeight() - defBarWidth, getWidth() - defBarWidth, defBarWidth);
	        updateHorScrollBar();
	        scrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
	                    getHeight() - defBarWidth);
	        updateScrollBar();
	        corner.setBounds(getWidth() - defBarWidth, getHeight() - defBarWidth, 
	        		defBarWidth, defBarWidth);
        }
        paintBuffer();
        //repaint();
	}
	
	@Override
	public void componentShown(ComponentEvent e) {
		super.componentResized(e);
	}

	/**
	 * Sets the tier that receives new segments.
	 * 
	 * @param tier the new active tier
	 */
	@Override
	public void setActiveTier(Tier tier) {
		if (tier != null) {
			if (editTier2d != null && editTier2d.getTier() == tier) {
				return;
			}
			dragEditTag2D = null;
            Iterator<Tier2D> it = allTiers.iterator();
            Tier2D t2d;
            
            while (it.hasNext()) {
                t2d = it.next();

                if (t2d.getTier() == tier) {
                    //visibleTiers.add(t2d);
                	editTier2d = t2d;
                	if (segmentationControlPanel != null) {
                		segmentationControlPanel.setActiveTier(tier);
                	}

                	setActionsForTier((TierImpl) tier);
                	paintBuffer();
                    break;
                }
            }
		} else {
			dragEditTag2D = null;
			editTier2d = null;
        	if (segmentationControlPanel != null) {
        		segmentationControlPanel.setActiveTier(null);
        	}
			setActionsForTier(null);
			paintBuffer();
		}
	}
	
	/**
	 * If a tier is associated with a controlled vocabulary this adds possible shortcuts
	 * to the input and action map.
	 * 
	 * @param tier the active tier
	 */
	private void setActionsForTier(TierImpl tier) {
		if (cvInputMap != null) {
    		cvInputMap.clear();
    	}
    	if (cvActionMap != null) {
    		cvActionMap.clear();
    	}

        if (tier != null) {
            String cvname = tier.getLinguisticType().getControlledVocabularyName();
            if (cvname != null) {
            	ControlledVocabulary cv = transcription.getControlledVocabulary(cvname);
            	if (cv != null) {
            		// extract keys? add actions
            		if (cvInputMap == null) { 
            			cvInputMap = new ComponentInputMap(this);
            			mainInputMap.setParent(cvInputMap);
            		}
            		if (cvActionMap == null) {
            			cvActionMap = new ActionMap();
            			mainActionMap.setParent(cvActionMap);
            		}

            		String cveId = "cve-";            		
            		int i = 0;
            		
            		for (CVEntry e : cv) {
            			if (e.getShortcutKeyCode() <= 0) {
            				continue;
            			}
            			String nextId = cveId + i;
            			i++;
            			SegmentAction sa = new SegmentAction(e);
            			cvInputMap.put((KeyStroke) sa.getValue(Action.ACCELERATOR_KEY), nextId);
            			cvActionMap.put(nextId, sa);
            		}
            	}
            }
        }
	}
	
	/**
	 * 
	 * @param down if true activate the next one in the list, otherwise the previous
	 */
	public void setNextTierActive(boolean down) {
		Tier2D nt2d;

		for (int i = 0; i < visibleTiers.size(); i++) {
			nt2d = visibleTiers.get(i);
			
			if (nt2d == editTier2d) {
				if (down) {
					if (i < visibleTiers.size() - 1) {
						setActiveTier(visibleTiers.get(i + 1).getTier());
					} else {
						setActiveTier(visibleTiers.get(0).getTier());
					}
				} else {
					if (i > 0) {
						setActiveTier(visibleTiers.get(i - 1).getTier());
					} else {
						setActiveTier(visibleTiers.get(visibleTiers.size() - 1).getTier());
					}
				}
				break;
			}
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
        metrics = getFontMetrics(font);
        
        recalculateTagSizes();
        pixelsForTierHeight = calcTierHeight();
        
        if (oldSize != f.getSize()) {
            notifySegmentationControlPanel();
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

                        break;
                    }
                } catch (NumberFormatException nfe) {
                    //// do nothing
                }
            }
        }
    }
    
	/**
	 * Updates the visible tiers
	 */
	@Override
	public void setVisibleTiers(List<TierImpl> tiers) {
		if (tiers != null) {
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
            
            notifySegmentationControlPanel();
            paintBuffer();
		}	
	}
	
	/**
	 * Handles changes in visibility of the time ruler.
	 */
	@Override
	protected void setTimeRulerVisible(boolean visible) {
		if (visible == timeRulerVisible) {
			return;
		}
		super.setTimeRulerVisible(visible);
		paintBuffer();
		notifySegmentationControlPanel();
		setPreference("SegmentationViewer.TimeRulerVisible", Boolean.valueOf(timeRulerVisible), 
				transcription);
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		int value = e.getValue();
		
		if (e.getSource() == scrollBar) {
	        verticalScrollOffset = value;
	        notifySegmentationControlPanel();
		} else if (e.getSource() == hScrollBar) {
	        setIntervalBeginTime(pixelToTime(value));
		}
		paintBuffer();
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().indexOf("font") > -1) {
            String sizeString = e.getActionCommand();
            int index = sizeString.indexOf("font") + 4;
            int size = 12;

            try {
                size = Integer.parseInt(sizeString.substring(index));
            } catch (NumberFormatException nfe) {
                System.err.println("Error parsing font size");
            }
            
            updateFont(getFont().deriveFont((float) size));
            
            // set preference once when leaving the mode
            setPreference("SegmentationViewer.FontSize", Integer.valueOf(size), 
            		transcription);
            return;
        } 
//		else if (e.getSource() == deleteAnnoMI) {
//        	deleteEditAnnotation();
//        	if (dragEditTag2D != null) {
//                //TierImpl tier = (TierImpl) dragEditTag2D.getAnnotation().getTier();
//                Annotation aa = dragEditTag2D.getAnnotation();
//                TierImpl tier = (TierImpl) aa.getTier();
//
//                Command c = ELANCommandFactory.createCommand(transcription,
//                        ELANCommandFactory.DELETE_ANNOTATION);
//                c.execute(tier, new Object[] { getViewerManager(), aa });
//                dragEditTag2D = null;
//        	}
//        	return;
//        } else if(e.getSource() == mergeBeforeAnnoMI){
//        	if (dragEditTag2D != null) {
//                //TierImpl tier = (TierImpl) dragEditTag2D.getAnnotation().getTier();
//                Annotation aa = dragEditTag2D.getAnnotation();
//                TierImpl tier = (TierImpl) aa.getTier();
//
//                Command c = ELANCommandFactory.createCommand(transcription,
//                        ELANCommandFactory.MERGE_ANNOTATION_WB);
//                c.execute(transcription, new Object[] { aa, false });
//        	}
//        } else if(e.getSource() == mergeNextAnnoMI){
//        	if (dragEditTag2D != null) {
//                //TierImpl tier = (TierImpl) dragEditTag2D.getAnnotation().getTier();
//                Annotation aa = dragEditTag2D.getAnnotation();
//                TierImpl tier = (TierImpl) aa.getTier();
//
//                Command c = ELANCommandFactory.createCommand(transcription,
//                        ELANCommandFactory.MERGE_ANNOTATION_WN);
//                c.execute(transcription, new Object[] { aa, true });
//        	}
//        } else if (e.getSource() == splitAnnoMI) {
//        	if (dragEditTag2D != null) {               
//                Annotation ann = dragEditTag2D.getAnnotation();
//                Object[] arguments = new Object[2];
//                arguments[0] = ann;
//                if(ann.getBeginTimeBoundary() < splitTime && ann.getEndTimeBoundary() > splitTime){
//                	arguments[1] = splitTime;
//                }
//                Command command = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.SPLIT_ANNOTATION);
//                command.execute(transcription, arguments);    	
//        	}
//        } 
		super.actionPerformed(e);
		
	}

	/**
	 * Finds an annotation on the edit tier at the specified x coordinate.
	 * @param x the x coordinate
	 * @return the tag2d object (annotation) or null
	 */
	private Tag2D getAnnotationAtX(int x) {
		if (editTier2d == null) {
			return null;
		}
		
		long t = timeAt(x);
		Tag2D t2d;
		for (int i = 0; i < editTier2d.getTagsList().size(); i++) {
			t2d = editTier2d.getTagsList().get(i);
			if (t >= t2d.getBeginTime() && t <= t2d.getEndTime()) {
				return t2d;
			}
			if (t2d.getEndTime() > t) {
				break;
			}
		}
		
		return null;
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

        int diff = dragEndPoint.x - dragStartPoint.x;

        switch (dragEditMode) {
        case DRAG_EDIT_CENTER:

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

        repaint();
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
    	if (dragEditTag2D.getAnnotation() instanceof AlignableAnnotation) {
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
        c.execute(editAnn,
                new Object[] { new Long(beginTime), new Long(endTime) });
    }
    
    /**
     * Deletes the "hover" annotation, the annotation the mouse is over 
     * (for the time being. Maybe the active annotation mechanism needs to be used).
     */
    protected void deleteEditAnnotation() {
    	if (dragEditTag2D == null) {
    		return;
    	}
    	
    	AlignableAnnotation editAnn = null;
    	if (dragEditTag2D.getAnnotation() instanceof AlignableAnnotation) {
    		editAnn = (AlignableAnnotation) dragEditTag2D.getAnnotation();
    	}
    	if (editAnn == null) {
    		return;
    	}
    	Tier tier = editAnn.getTier();
    	dragEditTag2D = null;
    	
    	Command c = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.DELETE_ANNOTATION);
    	c.execute(tier, new Object[]{getViewerManager(), editAnn});
   	
    }
    
    protected void mergeAnnotation(boolean next) {
		if (dragEditTag2D != null) {
            Annotation aa = dragEditTag2D.getAnnotation();
        	Command c;
            if(next){
            c = ELANCommandFactory.createCommand(transcription,
                        ELANCommandFactory.MERGE_ANNOTATION_WN);               
            } else{
            c = ELANCommandFactory.createCommand(transcription,
                         ELANCommandFactory.MERGE_ANNOTATION_WB);
            }            
            c.execute(transcription, new Object[] { aa, next });
    	}
    }	
	
    protected void splitAnnotation() {
		if (dragEditTag2D != null) {               
            Annotation ann = dragEditTag2D.getAnnotation();
            Object[] arguments = new Object[2];
            arguments[0] = ann;
            if(ann.getBeginTimeBoundary() < splitTime && ann.getEndTimeBoundary() > splitTime){
            	arguments[1] = splitTime;
            }
            Command command = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.SPLIT_ANNOTATION);
            command.execute(transcription, arguments);    	
    	}
    }
	
    protected void modifyAnnotationTimeDlg() {
		if (dragEditTag2D != null) {               
            Annotation ann = dragEditTag2D.getAnnotation();
            Object[] arguments = new Object[1];
            arguments[0] = ann;
            
            Command command = ELANCommandFactory.createCommand(transcription, 
            		ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG);
            command.execute(getViewerManager(), arguments);    	
    	}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            return;
        }

        dragEndPoint = e.getPoint();

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
        if (dragEditing) {
            // don't change the selection
            updateDragEditTag(dragEndPoint);
        }
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Object oldEdit = dragEditTag2D;
		int y = timeRulerVisible ? rulerHeight : 0;
		if (e.getY() > y && e.getY() < y + editTierHeight) {			
			// resets to null if there is no annotation at this spot
			dragEditTag2D = getAnnotationAtX(e.getX());
			
			if (dragEditTag2D != null) {
	            int x = (int) ((dragEditTag2D.getBeginTime() / msPerPixel) -
	                    (intervalBeginTime / msPerPixel));
	            int x2 = x +
	                    (int) ((dragEditTag2D.getEndTime() -
	                    dragEditTag2D.getBeginTime()) / msPerPixel);
	
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
				setCursor(Cursor.getDefaultCursor());
			}
                
			repaint();
		} else {
			dragEditTag2D = null;
			if (oldEdit != null) {
				repaint();
			}
			setCursor(Cursor.getDefaultCursor());
		}
	}
	
	/**
	 * Clicking on an annotation sets the selection (this viewer does not paint the selection yet).
	 * @since Oct 2015
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		
		if (dragEditTag2D != null) {
			setSelection(dragEditTag2D.getAnnotation().getBeginTimeBoundary(), dragEditTag2D.getAnnotation().getEndTimeBoundary());
		} else {
			Tag2D tag2d = getAnnotationAtX(e.getX());
			if (tag2d != null) {
				setSelection(tag2d.getAnnotation().getBeginTimeBoundary(), tag2d.getAnnotation().getEndTimeBoundary());
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		
		if (dragEditTag2D != null) {
			// create a copy for dragging
			Tag2D copy = new Tag2D(dragEditTag2D.getAnnotation());
            copy.setX(dragEditTag2D.getX());
            copy.setWidth(dragEditTag2D.getWidth());
            
            dragEditTag2D = copy;
            dragStartPoint = e.getPoint();
            dragStartTime = timeAt(e.getX());
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
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (popup != null && popup.isVisible()) {
			return;
		}
        if (dragEditing) {
            setCursor(Cursor.getDefaultCursor());
            doDragModifyAnnotationTime();
            dragEditing = false;
            dragEditTag2D = null;
            dragParentBegin = -1L;
            dragParentEnd = -1L;
            repaint();
        } else {
        	super.mouseReleased(e);
        }
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
    	if (e.getWheelRotation() == 0) {
    		return;
    	}
		if (e.isControlDown()) {
			super.mouseWheelMoved(e);
		} else if (e.isShiftDown()) {
			super.mouseWheelMoved(e);
		}
		else {
	        if (e.getUnitsToScroll() > 0) {
	            scrollBar.setValue(scrollBar.getValue() + pixelsForTierHeight);
	        } else {
	            scrollBar.setValue(scrollBar.getValue() - pixelsForTierHeight);
	        }
		}
	}
	//#### addition for key press key release segmentation ####
	public class PressReleaseSegmentAction extends AbstractAction {
		private boolean up;
		
		/**
		 * 
		 * @param up if true it triggers key released events, otherwise key pressed events
		 */
		public PressReleaseSegmentAction(boolean up) {
			this.up = up;
			KeyStroke ks = ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.SEGMENT, ELANCommandFactory.SEGMENTATION_MODE);
			if (ks == null) {
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, up));
			} else {
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(ks.getKeyCode(), 0, up));
			}
			putValue(Action.DEFAULT, null);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
//			System.out.println("Segment " + (up ? "Up" : "Down") + "   Is key down: " + pressReleaseKeyDown);
			if (!up && pressReleaseKeyDown) {
				// on some systems holding down a key for a longer period of time results in multiple
				// key pressed events. If the key is already down don't add a new segmentation time.
				return;
			}
			pressReleaseKeyDown = !up;	
			SegmentationViewer2.this.addPressReleaseTime(up);
		}
		
	}
	//#### end addition ####

	/**
     * An action class to handle the enter-key-typed event.
     *
     * @author Han Sloetjes
     */
    public class SegmentAction extends AbstractAction {
        /**
         * Constructor, sets the accelerator key to the VK_ENTER key.
         */
        public SegmentAction() {        	
            putValue(Action.ACCELERATOR_KEY, ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.SEGMENT, ELANCommandFactory.SEGMENTATION_MODE));              
            putValue(Action.DEFAULT, null);
        }
        
        /**
         * Constructor with key code and (annotation) value as parameters.
         * 
         * @param keyCode the key code
         * @param value the value for the segment
         */
        public SegmentAction(CVEntry cve) {        	
        	putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(cve.getShortcutKeyCode(), 0));      
        	putValue(Action.DEFAULT, cve);
        }

        /**
         * Forwards the action to the enclosing class.
         *
         * @param e the action event
         */
        @Override
		public void actionPerformed(ActionEvent e) {
//        	System.out.println("Segment action");
        	Object val = getValue(Action.DEFAULT);
        	if (val instanceof CVEntry) {
        		SegmentationViewer2.this.addSegmentTime((CVEntry) val);
        	} else {
        		SegmentationViewer2.this.addSegmentTime(null);
        	}
        }
    }

    /**
     * Action to activate the next or the previous tier.
     * @author Han Sloetjes
     *
     */
	public class ActiveTierAction extends AbstractAction {
		private boolean down = true;
		
		/**
		 * Constructor.
		 * @param down the direction of activation, down or up
		 */
		public ActiveTierAction(boolean down) {
			super();
			this.down = down;
			if (down) {
				putValue(Action.NAME, ElanLocale.getString(ELANCommandFactory.NEXT_ACTIVE_TIER)); 	            	
	            putValue(Action.ACCELERATOR_KEY, 
	            		ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.NEXT_ACTIVE_TIER, ELANCommandFactory.SEGMENTATION_MODE));    
	            putValue(Action.DEFAULT, null);
			} else {
				putValue(Action.NAME, ElanLocale.getString(ELANCommandFactory.PREVIOUS_ACTIVE_TIER)); 	            	
				putValue(Action.ACCELERATOR_KEY, 
		            		ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PREVIOUS_ACTIVE_TIER, ELANCommandFactory.SEGMENTATION_MODE));   
	            putValue(Action.DEFAULT, null);				
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SegmentationViewer2.this.setNextTierActive(down);
		}
		
	}
	
	/**
	 * An action to delete the "active"/"hover" annotation.
	 * 
	 * @author Han Sloetjes
	 *
	 */
	public class DeleteAnnotationAction extends AbstractAction {

		/**
		 * Constructor
		 */
		public DeleteAnnotationAction() {
			super(ElanLocale.getString(ELANCommandFactory.DELETE_ANNOTATION));			
			putValue(Action.ACCELERATOR_KEY, 
					ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.DELETE_ANNOTATION, 
							ELANCommandFactory.SEGMENTATION_MODE));   
            putValue(Action.DEFAULT, null);
		}
		
		public DeleteAnnotationAction(KeyStroke keyStroke) {
			super();			
			putValue(Action.ACCELERATOR_KEY, keyStroke);   
            putValue(Action.DEFAULT, null);
		}
		
		/**
		 * Calls the deleteEditAnnotation() method.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			SegmentationViewer2.this.deleteEditAnnotation();			
		}
		
	}
	
	/**
	 * An action to merge the "active"/"hover" annotation.
	 * 
	 * @author Han Sloetjes
	 *
	 */
	public class MergeAnnotationAction extends AbstractAction {
		
		private boolean next = true;
		
		/**
		 * Constructor.
		 * @param next  if true merge with next annotation,
		 * 				else merge with annotaiton before
		 */
		public MergeAnnotationAction(boolean next) {
			super();
			this.next = next;
			if (next) {
				putValue(Action.NAME, ElanLocale.getString(ELANCommandFactory.MERGE_ANNOTATION_WN)); 	            		 
	            putValue(Action.ACCELERATOR_KEY, 
	            		ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MERGE_ANNOTATION_WN, ELANCommandFactory.SEGMENTATION_MODE));    
	            putValue(Action.DEFAULT, null);
			} else {
				putValue(Action.NAME, ElanLocale.getString(ELANCommandFactory.MERGE_ANNOTATION_WB)); 	            		 
	  			putValue(Action.ACCELERATOR_KEY, 
		            		ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MERGE_ANNOTATION_WB, ELANCommandFactory.SEGMENTATION_MODE));   
	            putValue(Action.DEFAULT, null);				
			}
		}
	

		@Override
		public void actionPerformed(ActionEvent e) {
			SegmentationViewer2.this.mergeAnnotation(next);
		}

	}
	
	/**
	 * An action to split the "active"/"hover" annotation in two exact halves.
	 * 
	 * @author Han Sloetjes
	 *
	 */
	public class SplitAnnotationAction extends AbstractAction {		
	
		/**
		 * Constructor.		
		 */
		public SplitAnnotationAction() {
			super(ElanLocale.getString(ELANCommandFactory.SPLIT_ANNOTATION));			
			putValue(Action.ACCELERATOR_KEY, 
		            		ShortcutsUtil.getInstance().getKeyStrokeForAction(
		            				ELANCommandFactory.SPLIT_ANNOTATION, ELANCommandFactory.SEGMENTATION_MODE));   
			putValue(Action.DEFAULT, null);	
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SegmentationViewer2.this.splitAnnotation();
		}

	}
	
	public class ModifyTimeDlgAction extends AbstractAction {

		public ModifyTimeDlgAction() {
			super(ElanLocale.getString(ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG));
			putValue(Action.ACCELERATOR_KEY, ShortcutsUtil.getInstance().getKeyStrokeForAction(
					ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG,  ELANCommandFactory.SEGMENTATION_MODE));
			putValue(Action.DEFAULT, null);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SegmentationViewer2.this.modifyAnnotationTimeDlg();		
		}
		
	}
}