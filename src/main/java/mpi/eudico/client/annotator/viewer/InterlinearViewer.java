package mpi.eudico.client.annotator.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.EditTierDialog2;
import mpi.eudico.client.annotator.gui.InlineEditBox;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.annotator.util.Tier2D;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.AnnotationSize;
import mpi.eudico.server.corpora.clomimpl.shoebox.AnnotationSizeContainer;
import mpi.eudico.server.corpora.clomimpl.shoebox.SBLayout;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;


/**
 * This viewer shows annotations of multiple tiers. The annotations are not
 * abbreviated and they are not displayed relative to some kind of time scale.
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public class InterlinearViewer extends AbstractViewer
    implements ComponentListener, MouseMotionListener, MouseListener,
        MouseWheelListener, MultiTierViewer, AdjustmentListener, ActionListener,
        KeyListener, ACMEditListener {
    private TranscriptionImpl transcription = null;
    private SBLayout sbl = null;

    /** Holds value of property DOCUMENT ME! */
    protected int pixelsForTierHeight;

    /** Holds value of property DOCUMENT ME! */
    protected int pixelsForTierHeightMargin;
    private BufferedImage bi;
    private Graphics2D g2d;
    private FontMetrics currentMetrics;
    //private BasicStroke stroke;
    private JButton prevButton;
    private JButton nextButton;
    private JPopupMenu popup;
    private ButtonGroup fontSizeBG;
    private JMenu fontMenu;
    private Font font;
    private HashMap<String, Font> prefTierFonts;

    // menu items that can be enabled / disabled
    private JMenuItem newAnnoMI;
    private JMenuItem newAnnoBeforeMI;
    private JMenuItem newAnnoAfterMI;
    private JMenuItem modifyAnnoMI;

    //private JMenuItem modifyAnnoTimeMI;
    private JMenuItem deleteAnnoMI;
    private JMenuItem activeTierMI;
    private JMenuItem deleteTierMI;
    private JMenuItem changeTierMI;
    private long selSTime;
    private long selETime;
    private long curSTime = -1;
    //private long curETime;
    private SBTime curSBSelection = null;
    private SBTime cursorTag = null;
    private Annotation refAnnotation = null;
    private Annotation activeAnnotation = null;
    private int imageWidth = 1200;
    private int imageHeight = 380;

    /** Holds value of property DOCUMENT ME! */
    private final int HOR_TAG_GAP = 20;

    /** Holds value of property DOCUMENT ME! */
    private final int VER_MARGIN;

    /** Holds value of property DOCUMENT ME! */
    private final int HOR_TAG_SURPLUS = 5;

    /** Holds value of property DOCUMENT ME! */
    private final int MIN_TAG_SIZE = 15;

    // a vector of vectors each vector elem represents a tier
    // and stores SBTime objects
    private List<List<SBTime>> _vTiers = new ArrayList<List<SBTime>>();
    private ArrayList<Tier2D> allTiers;
    private int currentSegmentWidth;
    // treated as a constant, but not final for rare occasion where the width on screen 
    // is greater than the default (initial) value of 3000
    private int MAX_BUF_WIDTH = 3000;

    // ar
    private ArrayList<Tier2D> visibleTiers;
    private JPanel buttonPanel;
    private JScrollBar vScrollBar;
    private JScrollBar hScrollBar;

    /** Holds value of property DOCUMENT ME! */
    private final int defBarWidth;
    private int horizontalScrollOffset;
    private int verticalScrollOffset;
    private MultiTierControlPanel multiTierControlPanel;
    private int[] tierYPositions;
    private boolean showEmptySlots;
    //private long rightClickTime;
    //private int rightClickTierIndex;
    private Tier2D rightClickTier;
    private SBTime rightClickTag;

    // editing
    private InlineEditBox editBox;
    private boolean deselectCommits = true;

    /**
     * Constructs an InterLinearViewer using the specified user identity and
     * transcription.
     *
     * @param transcription the transcription containing the data for the
     *        viewer
     */
    public InterlinearViewer(TranscriptionImpl transcription) {
        this.transcription = transcription;
        font = Constants.DEFAULTFONT;
        setFont(font);
        currentMetrics = getFontMetrics(font);
        prefTierFonts = new HashMap<String, Font>();
        // pixelsForTierHeight = getFont().getSize() * 3;
        pixelsForTierHeight = getFont().getSize() + 24;
        pixelsForTierHeightMargin = 2;
        //stroke = new BasicStroke();
        initTiers();
        sbl = new SBLayout(transcription);
        sbl.getSegOrder();
        sbl.getPrevRef();

        //M_P Outcommented because of template files
        //				if (allTiers.size() > 0) {
        //					sbl.setWorkingSegmentsRange(1,0);
        //				}
        //enableEvents removed because of problems hiding a popup menu after a click on this component
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        addComponentListener(this);
        defBarWidth = getDefaultBarWidth();
        hScrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 10, 0, imageWidth);
        hScrollBar.setUnitIncrement(10);
        hScrollBar.setBlockIncrement(50);
        hScrollBar.addAdjustmentListener(this);
        vScrollBar = new JScrollBar(JScrollBar.VERTICAL, 0, 10, 0, imageHeight);
        vScrollBar.setUnitIncrement(pixelsForTierHeight / 2);
        vScrollBar.setBlockIncrement(pixelsForTierHeight);
        vScrollBar.addAdjustmentListener(this);

        //setLayout(new BorderLayout());
        //add(hScrollBar, BorderLayout.SOUTH);
        //add(vScrollBar, BorderLayout.EAST);
        setLayout(null);
        add(hScrollBar);
        add(vScrollBar);
        buttonPanel = createButtonPanel();

        //add(buttonPanel, BorderLayout.NORTH);
        add(buttonPanel);

        if (buttonPanel.getPreferredSize() != null) {
            VER_MARGIN = buttonPanel.getPreferredSize().height;
        } else {
            VER_MARGIN = 32;
        }

        horizontalScrollOffset = 0;
        verticalScrollOffset = 0;

        editBox = new InlineEditBox(true);
        editBox.setVisible(false);
        add(editBox);
        extractCurrentSegment();
    }
    
    /**
     * Overrides the super class method and
     * called when the viewer is destroyed 
     */
    @Override
	public void isClosing(){
    	if(editBox != null && editBox.isVisible()){
    		Boolean val = Preferences.getBool("InlineEdit.DeselectCommits", null);
			if (val != null && !val) {
				editBox.cancelEdit();
			} else {   				
				editBox.commitEdit();
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

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setFocusable(false);
        prevButton = new JButton();
        prevButton.setText(ElanLocale.getString(
                "InterlinearViewer.PrevButton.Text"));
        prevButton.setToolTipText(ElanLocale.getString(
                "InterlinearViewer.PrevButton.Tooltip"));
        prevButton.setActionCommand("prev");
        prevButton.addActionListener(this);
        prevButton.setFocusable(false);
        nextButton = new JButton();
        nextButton.setText(ElanLocale.getString(
                "InterlinearViewer.NextButton.Text"));
        nextButton.setToolTipText(ElanLocale.getString(
                "InterlinearViewer.NextButton.Tooltip"));
        nextButton.setActionCommand("next");
        nextButton.addActionListener(this);
        nextButton.setFocusable(false);

        Insets inset = new Insets(2, 6, 2, 6);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = inset;
        panel.add(prevButton, gbc);

        gbc.gridx = 1;
        panel.add(nextButton, gbc);

        JPanel spacer = new JPanel();
        spacer.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        spacer.setFocusable(false);
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(spacer, gbc);

        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
        panel.setPreferredSize(new Dimension(w,
                prevButton.getPreferredSize().height + 4));
        panel.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        panel.setBorder(BorderFactory.createLineBorder(Constants.SELECTIONCOLOR));

        return panel;
    }

    /**
     * Extract the Tiers from the annotation.<br>
     * Store Tier2D objects in the List of all Tiers and in the List
     * of visible tiers. There are other collections for storing all
     * information from the annotations.
     */
    private void initTiers() {
        Tier2D tier2d;
        allTiers = new ArrayList<Tier2D>(10);
        visibleTiers = new ArrayList<Tier2D>(10);

        Iterator<TierImpl> tierIt = transcription.getTiers().iterator();

        while (tierIt.hasNext()) {
            TierImpl tier = tierIt.next();

            tier2d = new Tier2D(tier);
            allTiers.add(tier2d);

            //visibleTiers.add(tier2d);
        }

        tierYPositions = new int[allTiers.size()];
    }

    /**
     * Processes the information for the current segment or annotation.<br>
     * A segment corresponds to the annotations of one (time) block. Shoebox
     * specific??
     */
    private void extractCurrentSegment() {
        dismissEditBox();
        cursorTag = null;
        _vTiers = new ArrayList<List<SBTime>>(20);

        if (!(allTiers.size() > 0) || !sbl.isRefAnnAvailable()) {
            buildLayout();

            return;
        }
        AnnotationSize as = null;
        if (prefTierFonts.size() > 0) {
        	Map<String, FontMetrics> metricsMap = new HashMap<String, FontMetrics>(prefTierFonts.size());
        	Iterator<String> keyIt = prefTierFonts.keySet().iterator();
        	while (keyIt.hasNext()) {
        		String key = keyIt.next();
        		Font tf = prefTierFonts.get(key);
        		if (tf != null) {
        			metricsMap.put(key, getFontMetrics(tf));
        		}
        	}
        	
            as = new AnnotationSize(transcription, sbl.getRefAnn(),
                    currentMetrics, metricsMap);
        } else {
            as = new AnnotationSize(transcription, sbl.getRefAnn(),
                    currentMetrics);
        }       

        List<TierImpl> segTiers = as.getTiers();

        int segWidth = 0;
        Tier tier;
        Iterator<Tier2D> tierIt = visibleTiers.iterator();
        Font tf = null;
        FontMetrics tfm = null;
        int yPos = VER_MARGIN;
        int xPos = 0;
        long time = -1;

        while (tierIt.hasNext()) {
            tier = tierIt.next().getTier();
            tf = getFontForTier(tier);
            tfm = getFontMetrics(tf);
            xPos = 0;

            boolean symbassflag = false; // have we counted freetranslation tiers - part of HACK ALERT
            int stereo = 0;

            if ((tier != null) && segTiers.contains(tier)) {
                List<AnnotationSizeContainer> tieranns = as.getTierLayoutInPixels(tier,
                        //currentMetrics);
                		tfm);

                if (tieranns == null) {
                    System.err.println("SB DRAWING ERROR!");

                    return;
                }

                List<SBTime> ht = new ArrayList<SBTime>(20);
                Rectangle rect = null;

                for (int itor = 0; itor < tieranns.size(); itor++) {
                    AnnotationSizeContainer asc = tieranns.get(itor);
                    Annotation a = asc.getAnnotation();

                    int tagWidth = asc.getSize() + HOR_TAG_SURPLUS;

                    if (tagWidth < MIN_TAG_SIZE) {
                        tagWidth = MIN_TAG_SIZE;
                    }

                    int tagHeight = (pixelsForTierHeight -
                        (2 * pixelsForTierHeightMargin));

                    if (((TierImpl) tier).getLinguisticType().getConstraints() != null) {
                        stereo = ((TierImpl) tier).getLinguisticType()
                                  .getConstraints().getStereoType();
                    } else {
                        stereo = -1;
                    }

                    if (a == null) {
                        // hack to stop "clickable" null SBTags from showing up
                        // BEGIN HACK ALERT
                        time = asc.getStartTime();

                        // gng: 24 Oct 2002; added more specific logic , to which tiers are
                        // not editable (freetranslation tier was not showing up as editable)
                        if ((((TierImpl) tier).getParentTier() == ((TierImpl) tier).getRootTier()) &&
                                ((stereo != Constraint.SYMBOLIC_ASSOCIATION) ||
                                symbassflag)) {
                            if (asc != tieranns.get(0)) {
                                time = -1;

                                //System.out.println("Time -1 added (" + tier + ", " + a + ")");
                            }
                        }

                        //System.out.println("ADD NULL TAG:"+xPos+" "+yPos+"time:"+time);
                        rect = new Rectangle();
                        rect.x = xPos;
                        rect.y = yPos + pixelsForTierHeightMargin;
                        rect.height = tagHeight;
                        rect.width = tagWidth;

                        // END HACK ALERT
                    } else {
                        rect = new Rectangle();
                        rect.x = xPos;
                        rect.y = yPos + pixelsForTierHeightMargin;
                        rect.height = tagHeight;
                        rect.width = tagWidth;
                    }

                    //asc.setRect(new Rectangle(xPos,yPos,tagWidth,tagHeight));
                    if (stereo == Constraint.SYMBOLIC_ASSOCIATION) {
                        symbassflag = true;
                    }

                    SBTime sbt = null;

                    if (null != a) {
                        sbt = new SBTime(a, a.getBeginTimeBoundary(),
                                a.getEndTimeBoundary(), rect, a.getValue());

                        //ht.add(new SBTime(a.getBeginTimeBoundary(), a.getEndTimeBoundary(), rect, a.getValue()));
                    } else {
                        sbt = new SBTime(null, time, time, rect, "");

                        //sbt = new SBTime(null, time, time + HOR_TAG_GAP, rect, "");
                        //ht.add(new SBTime(time, time + HOR_TAG_GAP, rect, ""));
                    }

                    ht.add(sbt);

                    if ((a != null) && (a == activeAnnotation)) {
                        refAnnotation = sbl.getRefAnn();
                        cursorTag = sbt;
                    }

                    if ((xPos + tagWidth) > segWidth) {
                        segWidth = xPos + tagWidth;
                    }

                    xPos += (tagWidth + HOR_TAG_GAP);
                }

                _vTiers.add(ht);
            } else {
                _vTiers.add(new ArrayList<SBTime>());
            }

            yPos += pixelsForTierHeight;
        }

        //end while loop
        currentSegmentWidth = segWidth + HOR_TAG_GAP;
        buildLayout();
    }

    /**
     * For each segment that is to be displayed a new canvas width is
     * calculated and the scrollbars have to be updated accordingly.
     */
    private void updateScrollBar() {
        // vertical first
        int vValue = vScrollBar.getValue();
        int max = VER_MARGIN + (visibleTiers.size() * pixelsForTierHeight);

        // before changing scrollbar values do a setValue(0), otherwise
        // setMaxiimum and/or setVisibleAmount will not be accurate
        vScrollBar.setValue(0);
        vScrollBar.setMaximum(max);
        vScrollBar.setVisibleAmount(getHeight() - defBarWidth);

        if (((vValue + getHeight()) - defBarWidth) > max) {
            vValue = max - getHeight() - defBarWidth;
        }

        vScrollBar.setValue(vValue);

        // horizontal next
        int hValue = hScrollBar.getValue();

        /* because in the BorderLayout the horizontal scrollbar
         * includes the space occupied by the vertical scrollbar */
        int hBarWidth = 2 * defBarWidth;
        hScrollBar.setValue(0);
        hScrollBar.setMaximum(currentSegmentWidth);
        hScrollBar.setVisibleAmount(getWidth() - hBarWidth);

        if (((hValue + getWidth()) - hBarWidth) > currentSegmentWidth) {
            hValue = currentSegmentWidth - getWidth() - hBarWidth;
        }

        hScrollBar.setValue(hValue);
        vScrollBar.revalidate();
        hScrollBar.revalidate();
    }

    /**
     * The tiers present in a document can be manipulated in a MultiTierViewer,
     * i.g. the ordering of the tiers and their visibility can be changed.
     * This viewer updates its state after such a change.
     *
     * @param tiers DOCUMENT ME!
     */
    @Override
	public void setVisibleTiers(List<TierImpl> tiers) {
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

        extractCurrentSegment();

        updateScrollBar();
        notifyMultiTierControlPanel();
    }

    /**
     * Sets the active tier for this viewer.<br>
     * The active tier is marked by a colored background bar.
     *
     * @param tier DOCUMENT ME!
     */
    @Override
	public void setActiveTier(Tier tier) {
        Iterator<Tier2D> it = allTiers.iterator();
        Tier2D t2d;
        String name = tier.getName();

        while (it.hasNext()) {
            t2d = it.next();

            if (t2d.getName().equals(name)) {
                t2d.setActive(true);
                
                int tierIndex = visibleTiers.indexOf(t2d);
                int cy = tierIndex * pixelsForTierHeight;
                if (cy < verticalScrollOffset) {
                    vScrollBar.setValue(cy);
                } else if (((cy + pixelsForTierHeight) - verticalScrollOffset) > (getHeight() -
                        VER_MARGIN - defBarWidth)) {
                    vScrollBar.setValue((cy + pixelsForTierHeight + VER_MARGIN + defBarWidth) -
                        getHeight());
                }
            } else {
                t2d.setActive(false);
            }
        }

        buildLayout();
    }

    /**
     * Sets the MultiTierControlPanel for this viewer.<br>
     * This controller gets notified of when the (vertical) positions of the
     * tiers have been changed (scrolling).
     *
     * @param controller DOCUMENT ME!
     */
    @Override
	public void setMultiTierControlPanel(MultiTierControlPanel controller) {
        multiTierControlPanel = controller;
        notifyMultiTierControlPanel();
    }

    /**
     * Calculates the y positions of the vertical middle of all visible tiers
     * and passes them to the MultiTierControlPanel.
     */
    private void notifyMultiTierControlPanel() {
        if (multiTierControlPanel == null) {
            return;
        }

        if (tierYPositions.length > 0) {
            tierYPositions[0] = (VER_MARGIN + (pixelsForTierHeight / 2)) -
                verticalScrollOffset;

            for (int i = 1; i < visibleTiers.size(); i++) {
                tierYPositions[i] = tierYPositions[0] +
                    (i * pixelsForTierHeight);
            }
        }

        multiTierControlPanel.setTierPositions(tierYPositions);
    }

    /**
     * If the selection has been changed in some other viewer or controller
     * this viewer is updated to reflect that change.<br>
     */
    @Override
	public void updateSelection() {
        selSTime = getSelectionBeginTime();
        selETime = getSelectionEndTime();
        repaint();
    }

    /**
     * AR heeft dit hier neergezet, zie abstract viewer voor get en set
     * methodes van ActiveAnnotation. Update method from ActiveAnnotationUser
     */
    @Override
	public void updateActiveAnnotation() {
        activeAnnotation = getActiveAnnotation();
        extractCurrentSegment();
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
                annotationChanged((Tier) e.getInvalidatedObject(),
                    (Annotation) e.getModification());

                // setActiveAnnotation((Annotation) e.getModification());
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
                annotationChanged((Tier) e.getInvalidatedObject(),
                    (Annotation) e.getModification());

                // setActiveAnnotation((Annotation) e.getModification());
                showEditBoxForAnnotation((Annotation) e.getModification());

                if (editBox.isVisible()) {
                    editBox.requestFocus();
                }
                
                if (multiTierControlPanel != null) {
                    multiTierControlPanel.annotationsChanged();
                }
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

            if (e.getModification() instanceof Annotation) {
                annotationRemoved((Annotation) e.getModification());
            } else if (e.getInvalidatedObject() instanceof Transcription) {
                //System.out.println("Operation == REMOVE_ANNOTATION");
                transcriptionChanged();
            }
            
            if (multiTierControlPanel != null) {
                multiTierControlPanel.annotationsChanged();
            }

            break;

        case ACMEditEvent.CHANGE_ANNOTATION_TIME:

        // fall through...
        //break;
        case ACMEditEvent.CHANGE_ANNOTATION_VALUE:

            if (e.getInvalidatedObject() instanceof Annotation) {
                Annotation a = (Annotation) e.getInvalidatedObject();

                if (a.getTier() instanceof TierImpl) {
                    annotationChanged(a.getTier(), a);
                }
            }

            break;

        default:
            break;
        }
    }

    /**
     * Paints the tiers to a Buffered Image.<br>
     * This is necessary when a new segment needs to be displayed, or after a
     * change in the visibility and/or ordering of the tiers.
     * Mar 2006: a maximum to the buffer's size is introduced to prevent OutOfMemoryErrors.
     * If the complete interlinear block does not fit in the buffer, the horizontal scroll offset 
     * is used when painting in the buffer. Consequently a new buffer image has to be painted with 
     * every horizontal scroll event.
     */
    protected void buildLayout() {
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }
        if (getWidth() > MAX_BUF_WIDTH) {
            MAX_BUF_WIDTH = getWidth();
        }
        /*
           int barWidth = vScrollBar.getWidth();
           if (barWidth <= 0) {
               if (UIManager.getDefaults().get("ScrollBar.width") != null) {
                   barWidth = ((Integer)(UIManager.getDefaults().get("ScrollBar.width"))).intValue();
               } else {
                   barWidth = 0;
               }
           }
         */
        int width = currentSegmentWidth + HOR_TAG_GAP;
        int height = VER_MARGIN + (visibleTiers.size() * pixelsForTierHeight);

        /* this only changes the image size when it is to small,
         * not when it is larger than needed */
        if (imageWidth < width && width < MAX_BUF_WIDTH) {
            imageWidth = width;
        }

        if (imageHeight < height) {
            imageHeight = height;
        }

        // Paint it off screen first
        if ((bi == null) || (bi.getWidth() < imageWidth) ||
                (bi.getHeight() < imageHeight)) {
            bi = new BufferedImage(imageWidth, imageHeight,
                    BufferedImage.TYPE_INT_RGB);
            g2d = bi.createGraphics();
        }

        if (SystemReporting.antiAliasedText) {
	        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        g2d.setFont(getFont());
        g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        g2d.fillRect(0, 0, imageWidth, bi.getHeight());

        Tier2D tier2d;
        // two painting strategies
        if (currentSegmentWidth > MAX_BUF_WIDTH) {
            // use the hor. scroll offset to paint in the buffer
            g2d.translate(-horizontalScrollOffset, 0);
            
	        for (int i = 0; i < visibleTiers.size(); i++) {
	            tier2d = visibleTiers.get(i);
	
	            if (tier2d.isActive()) {
	                g2d.setColor(Constants.ACTIVETIERCOLOR);
	                g2d.fillRect(horizontalScrollOffset, VER_MARGIN + (i * pixelsForTierHeight),
	                    imageWidth, pixelsForTierHeight);
	            }
	
	            if (i < _vTiers.size()) {
	                Iterator tagIt = _vTiers.get(i).iterator();
	
	                while (tagIt.hasNext()) {
	                    SBTime sbt = (SBTime) tagIt.next();
	                    if (sbt._rect.x + sbt._rect.width < horizontalScrollOffset) {
	                        continue;
	                    } else if (sbt._rect.x > horizontalScrollOffset + getWidth()) {
	                        break;
	                    }
	                    drawTag(g2d, sbt);
	                }
	            }
	        }
	        
            g2d.translate(horizontalScrollOffset, 0);
        } else {
            // paint all in the buffer, paintComponent will handle offsets
	        for (int i = 0; i < visibleTiers.size(); i++) {
	            tier2d = visibleTiers.get(i);
	
	            if (tier2d.isActive()) {
	                g2d.setColor(Constants.ACTIVETIERCOLOR);
	                g2d.fillRect(0, VER_MARGIN + (i * pixelsForTierHeight),
	                    imageWidth, pixelsForTierHeight);
	            }
	
	            if (i < _vTiers.size()) {
	                Iterator tagIt = _vTiers.get(i).iterator();
	
	                while (tagIt.hasNext()) {
	                    SBTime sbt = (SBTime) tagIt.next();
	                    drawTag(g2d, sbt);
	                }
	            }
	        }
        }
        repaint();
    }

    /**
     * Draws the value of a Tag to the specified Graphics object.
     *
     * @param g the Graphics object to render to
     * @param tag the SBTime (tag) to render
     */
    protected void drawTag(Graphics2D g, SBTime tag) {
        if (tag != null) {
            //g.drawString(tag.value, tag._rect.x, tag._rect.y +  2 * tag._rect.height / 3);
            // draw a marker when there is an annotation, but no text
            if ((tag.annotation != null) && (tag.value.length() == 0)) {
                g.setColor(Color.gray);
                g.draw(tag._rect);
            }

            //
            g.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
            if (tag.annotation != null) {
            	g.setFont(getFontForTier(tag.annotation.getTier()));
            }
            g.drawString(tag.value, tag._rect.x,
                tag._rect.y + (tag._rect.height / 2) +
                (getFont().getSize() / 2));
        }
    }

    /**
     * Overrides <code>JComponent</code>'s paintComponent to paint:<br>
     * - a Buffered Image with all the current tag values<br>
     * - a marker for the current selection - a "mouse over" marker
     *
     * Mar 2006: Added alternative painting strategy in case the complete interlinear image does not fit 
     * in the buffer; the buffer contains only part of the block and is painted at x = 0; 
     * 
     * @param g the Graphics context
     */
    @Override
	public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        
        if (SystemReporting.antiAliasedText) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        g2.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        if (currentSegmentWidth > MAX_BUF_WIDTH) {
            g2.translate(0, -verticalScrollOffset);// alternative paint strategy
        } else {
            g2.translate(-horizontalScrollOffset, -verticalScrollOffset);    
        }

        if (bi != null) {
            g2.drawImage(bi, 0, 0, this);
        } else {
            return;
        }

        if (currentSegmentWidth > MAX_BUF_WIDTH) {
            g2.translate(-horizontalScrollOffset, 0);
        }
        
        if (selSTime != selETime) {
            paintSelectionMarker(g2);
        }

        /*
           if (curSBSelection != null && !playerIsPlaying())
           {
               //AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.4f);
               //g2.setComposite(ac);
               Rectangle rect = curSBSelection._rect;
               g2.setColor(Constants.SHAREDCOLOR3);
               g2.fillRect(rect.x, rect.y, rect.width, rect.height);
                           g2.setColor(Constants.SHAREDCOLOR4);
                           g2.fillRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
                           drawTag(g2, curSBSelection);
                           //g2.setComposite(AlphaComposite.Src);
           }
         */

        //
        if (!playerIsPlaying()) {
            paintMediaTimeMarker(g2);

            if (showEmptySlots) {
                for (int i = 0; i < visibleTiers.size(); i++) {
                    TierImpl ti = visibleTiers.get(i).getTier();

                    if (ti.getParentTier() == null) {
                        continue;
                    } else {
                        if (!ti.isTimeAlignable()) {
                            paintEmptySlots(g2, ti, i);
                        }
                    }
                }
            }
        }

        if ((cursorTag != null) && (sbl.getRefAnn() == refAnnotation)) {
            g2.setColor(Constants.ACTIVEANNOTATIONCOLOR);
            g2.draw(cursorTag._rect);
        }

        g2.translate(horizontalScrollOffset, verticalScrollOffset);
    }
    
    /**
     * Paints the selected tags in a different color in order to visually
     * distinguish them from the other tags.<br>
     * <b>Note:</b> removed the Collections.binarySearch method. It fails when
     * the tier contains empty tags, tags without an annotation, or tags were
     * start time = -1. These tags ruin the "natural ordering" necessary for
     * the binary search algorithm.
     *
     * @param g2 the Graphics object to render to
     */
    private void paintSelectionMarker(Graphics2D g2) {

        for (List<SBTime> ht : _vTiers) {

            if (ht == null) {
                continue;
            }

            Iterator<SBTime> e = ht.iterator();
tagloop: 
            while (e.hasNext()) {
                SBTime sbt = e.next();

                if (sbt.startt == -1) {
                    continue;
                }

                //
                if (((sbt.startt <= selSTime) && (sbt.endt > selSTime)) ||
                        ((sbt.startt >= selSTime) && (sbt.endt < selETime)) ||
                        ((sbt.startt < selETime) && (sbt.endt >= selETime))) {
                    Rectangle rect = sbt._rect;

                    if (rect != null) {
                        g2.setColor(Constants.SELECTIONCOLOR);
                        g2.fill(rect);
                        drawTag(g2, sbt);
                    }

                    int pos = ht.indexOf(sbt);

                    for (int i = pos + 1; i < ht.size(); i++) {
                        sbt = ht.get(i);

                        if (sbt.startt >= selETime) {
                            break tagloop;
                        }

                        rect = sbt._rect;

                        if ((rect != null) && (sbt.startt != -1)) {
                            g2.setColor(Constants.SELECTIONCOLOR);
                            g2.fill(rect);
                            drawTag(g2, sbt);
                        }
                    }
                }

                //
            }
        }
    }

    /**
     * Determines whether to repaint or not.<br>
     * Sets alpha rect over ann's at current time.
     *
     * @return DOCUMENT ME!
     */
    public boolean shouldPaint() {
        if (getMediaTime() != curSTime) {
            return true;
        }

        return false;
    }

    /**
     * Paints the a marker to visually distinguish the tags at the current
     * media time. <b>Note:</b> removed the Collections.binarySearch method.
     * It fails when the tier contains empty tags, tags without an annotation,
     * or tags were start time = -1. These tags ruin the "natural ordering"
     * necessary for the binary search algorithm.
     *
     * @param g2 the Graphics object to render to
     */
    public void paintMediaTimeMarker(Graphics2D g2) {

    	for (List<SBTime> ht : _vTiers) {

            // This could be improved maybe by using the method the the TimeLineControllers use!.
            // ie: assume time moves forward - get current position and search from that point forward
            if (ht == null) {
                continue;
            }

            for (SBTime sbt : ht) {

                if (sbt.startt == -1) {
                    continue;
                }

                if ((sbt.startt <= getMediaTime()) &&
                        (sbt.endt > getMediaTime())) {
                    g2.setColor(Constants.CROSSHAIRCOLOR);

                    Rectangle rect = sbt._rect;
                    curSTime = sbt.startt;

                    if (rect != null) {
                        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
                    }

                    break;
                }
            }
        }
    }

    /**
     * Paint empty slots on this tier.<br>
     * Look for SBTime objects with annotation == null and start time == end
     * time.
     *
     * @param g2d the graphics context
     * @param tier the tier to check for empty slots
     * @param tierIndex the index of the tier
     */
    private void paintEmptySlots(Graphics2D g2d, TierImpl tier, int tierIndex) {
        if (tierIndex >= _vTiers.size()) {
            return;
        }

        //try {
        TierImpl parent = tier.getParentTier();

        for (SBTime sbt : _vTiers.get(tierIndex)) {

            if ((sbt.annotation == null) && (sbt.startt != -1)) {
                /* sbt start and end are the same
                   add 1ms to the start time to prevent finding the previous annotation on the
                   parent tier (prev annotation end == this annotation begin)
                 */
                if (parent.getAnnotationAtTime(sbt.startt + 1) != null) {
                    g2d.setColor(Constants.SHAREDCOLOR4);
                    g2d.fill(sbt._rect);
                    g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
                    g2d.draw(sbt._rect);
                }
            }
        }

        //} catch (RemoteException rex) {
        //rex.printStackTrace();
        //}
    }

    /**
     * Returns the Tier object at a certain point in the image.
     *
     * @param p the point to find the Tier for
     *
     * @return the Tier at Point p
     */
    private Tier2D getTierAtPoint(Point p) {
        Point pp = new Point(p);
        pp.y += verticalScrollOffset;

        int index = getTierIndexForPoint(pp);

        if ((index < 0) || (index >= visibleTiers.size())) {
            return null;
        }

        return visibleTiers.get(index);
    }

    /**
     * Calculate the index in the visible tiers array for the given y
     * coordinate. Vertical scroll offset is not taken into account in this
     * method!
     *
     * @param p DOCUMENT ME!
     *
     * @return the index of the tier  or -1 when not found
     */
    private int getTierIndexForPoint(Point p) {
        int y = (int) p.getY() - VER_MARGIN;

        if ((y < 0) || (y > (_vTiers.size() * pixelsForTierHeight))) {
            return -1;
        } else {
            return y / pixelsForTierHeight;
        }
    }

    /**
     * Returns the index of the specified tag in the tier's array of tags.
     *
     * @param tier the tier
     * @param tag the tag
     *
     * @return the index
     */
    private int getTagIndex(Tier2D tier, SBTime tag) {
        int j = visibleTiers.indexOf(tier);

        if (j > -1) {
            List<SBTime> v = _vTiers.get(j);

            return v.indexOf(tag);
        }

        return -1;
    }

    /**
     * Tries to find the tag / annotation at the specified point on the canvas.<br>
     * Collections.binarySearch seems to work properly here.
     *
     * @param p the Point to get the tag for
     *
     * @return the selected tag or null when there is no tag at Point p
     */
    private SBTime getTagAtPoint(Point p) {
        if (p.y <= VER_MARGIN) {
            return null;
        }

        p.x += horizontalScrollOffset;
        p.y += verticalScrollOffset;

        int size = _vTiers.size();

        int curtier = getTierIndexForPoint(p);

        //System.out.println("Curtier: " + curtier);
        if ((curtier >= size) || (curtier < 0)) {
            return null;
        }

        // get current tier
        List<SBTime> v = _vTiers.get(curtier);

        // iterate through the rects and find match
        SBTime sb = null;
        int pos = -1;

        if ((pos = Collections.binarySearch(v, p)) >= 0) {
            sb = v.get(pos);

            return sb;
        }

        return null;
    }

    /**
     * Create a popup menu to enable the manipulation of some settings for this
     * viewer.
     */
    private void createPopupMenu() {
        popup = new JPopupMenu("Interlinear Viewer");

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

        popup.addSeparator();

        //	tier menu items
        activeTierMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Tier.ActiveTier"));
        activeTierMI.setActionCommand("activeTier");
        activeTierMI.addActionListener(this);
        popup.add(activeTierMI);

        deleteTierMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Tier.DeleteTier"));
        deleteTierMI.setActionCommand("deleteTier");
        deleteTierMI.addActionListener(this);
        popup.add(deleteTierMI);

        changeTierMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Tier.ChangeTier"));

        //changeTierMI = new JMenuItem(ELANCommandFactory.CHANGE_TIER_ATTR);
        changeTierMI.setActionCommand("changeTier");
        changeTierMI.addActionListener(this);
        popup.add(changeTierMI);

        popup.addSeparator();

        // annotation menu items
        newAnnoMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Annotation.NewAnnotation"));
        newAnnoMI.setActionCommand("newAnn");
        newAnnoMI.addActionListener(this);
        popup.add(newAnnoMI);

        newAnnoBeforeMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Annotation.NewAnnotationBefore"));
        newAnnoBeforeMI.setActionCommand("annBefore");
        newAnnoBeforeMI.addActionListener(this);
        popup.add(newAnnoBeforeMI);

        newAnnoAfterMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Annotation.NewAnnotationAfter"));
        newAnnoAfterMI.setActionCommand("annAfter");
        newAnnoAfterMI.addActionListener(this);
        popup.add(newAnnoAfterMI);

        modifyAnnoMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Annotation.ModifyAnnotation"));
        modifyAnnoMI.setActionCommand("modifyAnn");
        modifyAnnoMI.addActionListener(this);
        popup.add(modifyAnnoMI);

        deleteAnnoMI = new JMenuItem(ElanLocale.getString(
                    "Menu.Annotation.DeleteAnnotation"));
        deleteAnnoMI.setActionCommand("deleteAnn");
        deleteAnnoMI.addActionListener(this);
        popup.add(deleteAnnoMI);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }

    /**
     * Enables / disables annotation and tier specific menuitems, depending on
     * the mouse click position.<br>
     * <b>Note: </b> this might need to be changed once usage of Action and
     * Command objects is implemented.
     *
     * @param p the position of the mouse click
     */
    private void updatePopup(Point p) {
        //disable all first
        newAnnoMI.setEnabled(false);
        newAnnoBeforeMI.setEnabled(false);
        newAnnoAfterMI.setEnabled(false);
        modifyAnnoMI.setEnabled(false);
        deleteAnnoMI.setEnabled(false);
        activeTierMI.setEnabled(false);
        deleteTierMI.setEnabled(false);
        changeTierMI.setEnabled(false);

        if (p.y < VER_MARGIN) {
            return;
        } else {
            Point pp = new Point(p);
            rightClickTier = getTierAtPoint(pp);

            if (rightClickTier == null) {
                return;
            }

            TierImpl tier = rightClickTier.getTier();

            if (tier == null) {
                return;
            }

            deleteTierMI.setEnabled(true);
            changeTierMI.setEnabled(true);

            if (!rightClickTier.isActive()) {
                activeTierMI.setEnabled(true);
            }

            Point tp = new Point(p);
            rightClickTag = getTagAtPoint(tp);

            if (rightClickTag == null) {
                // nothing to do when we are not on a tag
                return;
            }

            boolean supportsInsertion = false;

            try {
                LinguisticType lt = tier.getLinguisticType();
                Constraint c = null;

                if (lt != null) {
                    c = lt.getConstraints();
                }

                if (c != null) {
                    supportsInsertion = c.supportsInsertion();
                }

                if (rightClickTag == cursorTag) { //?? need this ??
                    modifyAnnoMI.setEnabled(true);
                    deleteAnnoMI.setEnabled(true);

                    if (supportsInsertion) {
                        newAnnoAfterMI.setEnabled(true);
                        newAnnoBeforeMI.setEnabled(true);
                    } else if (tier.isTimeAlignable() &&
                            (rightClickTag.annotation != null) /*&&
                           !(rightClickTag.annotation instanceof RefAnnotation)*/) {
                        // should be else if (tier.isTimeAlignable()){
                        newAnnoMI.setEnabled(true); //replace an existing annotation??
                    }
                } else if ((rightClickTag.startt == rightClickTag.endt) &&
                        (rightClickTag.startt != -1)) { //there is a tag but not an annotation?
                    newAnnoMI.setEnabled(true);
                }
            } catch (Exception rex) {
                rex.printStackTrace();
            }
        }
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

    /**
     * Display the edit box for the specified SBTime.
     *
     * @param sbt the tag to edit
     */
    private void showEditBoxForTag(SBTime sbt) {
        if (sbt.annotation == null) {
            return;
        }

        editBox.setAnnotation(sbt.annotation);
        editBox.setSize(new Dimension(sbt._rect.width, sbt._rect.height));
        Font f = getFontForTier(sbt.annotation.getTier());
        if (f != null) {
        	editBox.setFont(f);
        }
        editBox.setLocation(sbt._rect.x - horizontalScrollOffset,
            sbt._rect.y - verticalScrollOffset);
        editBox.configureEditor(JPanel.class, null,
            new Dimension(sbt._rect.width, sbt._rect.height));
        editBox.startEdit();
    }
    
    public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksLsit){
    	if(editBox != null){
    		editBox.setKeyStrokesNotToBeConsumed(ksLsit);
    	}
    }

    /**
     * Try to find the SBTime for the specified annotation and forward to
     * <code>showEditBoxForTag</code>.
     *
     * @param a the annotation to edit
     */
    public void showEditBoxForAnnotation(Annotation a) {
        if (a == null) {
            return;
        }

        for (List<SBTime> v  : _vTiers) {

            for (SBTime tag : v) {

                if (tag.annotation == a) {
                    showEditBoxForTag(tag);

                    return;
                }
            }
        }
    }

    //***** editing and data changed methods **************************************//

    /**
     * Add a new Tier to the existing list of tiers.<br>
     * This method is private because it does not check whether the specified
     * Tier already is present in the transcription.
     *
     * @param tier the new Tier
     */
    private void tierAdded(TierImpl tier) {
        Tier2D t2d = new Tier2D(tier);
        allTiers.add(t2d);
        sbl.getSegOrder();
        tierYPositions = new int[allTiers.size()];

        // wait for a call to setVisibleTiers to show the tier
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
                if ((cursorTag != null) && (cursorTag.annotation != null) &&
                        (cursorTag.annotation.getTier() == tier)) {
                    cursorTag = null;
                    setActiveAnnotation(null);
                }

                break;
            }
        }
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
        for (int i = 0; i < allTiers.size(); i++) {
            Tier2D tier2d = allTiers.get(i);

            if (tier2d.getTier() == tier) {
            	if (!tier2d.getName().equals(tier.getName())) {
            		Font opf = prefTierFonts.remove(tier2d.getName());
            		if (opf != null) {
            			prefTierFonts.put(tier.getName(), opf);
            		}
            	}
                tier2d.updateName();

                break;
            }
        }
    }

    /**
     * This method inserts a new annotation if there is an empty slot at the
     * specified point on the tier.<br>
     * <b>Note: </b> The current implementation relies on the annotation / text
     * layout, i.e. if there is an SBTime object with annotation == null and
     * start time == end time and start time != -1, it is assumed that we can
     * insert a new annotation.
     *
     * @param tier2d the Tier2D
     * @param p the location of a doubleclick in component space
     */
    private void autoInsertAnnotation(Tier2D tier2d, Point p) {
        dismissEditBox();

        TierImpl child = tier2d.getTier();

        if ((child == null) || child.isTimeAlignable() ||
                !child.hasParentTier()) {
            return;
        }

        SBTime sbt = getTagAtPoint(p);

        if ((sbt == null) || (sbt.annotation != null) || (sbt.startt == -1) ||
                (sbt.startt != sbt.endt)) {
            return;
        } else {
        	Command c = null;
            Boolean val = Preferences.getBool("CreateDependingAnnotations", null);                
            if (val != null) {
            	 if(val.booleanValue()){
            		 c = ELANCommandFactory.createCommand(transcription,
                             ELANCommandFactory.NEW_ANNOTATION_REC);  
            		 
            	 } else {
            		 c = ELANCommandFactory.createCommand(transcription,
                             ELANCommandFactory.NEW_ANNOTATION);                		 
            	 }
            } else {
       		 	c = ELANCommandFactory.createCommand(transcription,
                     ELANCommandFactory.NEW_ANNOTATION);                		 
            }
            Object[] args = new Object[] {
                    new Long(sbt.startt + 1),
                    new Long(sbt.startt + 1)
                };
            c.execute(child, args);

            Annotation aa = child.getAnnotationAtTime(sbt.startt + 1);

            if (aa != null) {
                sbt.annotation = aa;
                setActiveAnnotation(aa);
                showEditBoxForTag(sbt);

                if (editBox.isVisible()) {
                    editBox.requestFocus();
                }
            }
        }
    }

    /**
     * If the mofified annotation is within the current segment and the tier is
     * visible re-extract the current segment.<br>
     *
     * @param tier the Tier the annotation belongs to
     * @param annotation the new annotation
     */
    private void annotationChanged(Tier tier, Annotation annotation) {
        sbl.getRefTierOrder();
        extractCurrentSegment();

        /*
           if (activeAnnotation != null) {
               setSelection(activeAnnotation.getBeginTimeBoundary(), activeAnnotation.getEndTimeBoundary());
           }*/
        /*
           if (annotation.getBeginTimeBoundary() >= sbl.getCurrentRefStartTime() &&
                   annotation.getEndTimeBoundary() <= sbl.getCurrentRefEndTime()) {
               for (int i = 0; i < visibleTiers.size(); i++) {
                   if (((Tier2D)visibleTiers.get(i)).getTier() == tier) {
                       extractCurrentSegment();
                       return;
                   }
               }
           }
         */
    }

    /**
     * This is called when an ACMEditEvent is received with operation
     * REMOVE_ANNOTATION or CHANGE_ANNOTATIONS and the transcription as
     * invalidated object.<br>
     * It is undefined which tiers and annotations have been effected, so the
     * transcription is simply re-processed. Store state as much as possible.
     * Assume no tiers have been deleted or added.
     */
    private void transcriptionChanged() {
        sbl.getRefTierOrder();
        extractCurrentSegment();

        /*
           if (activeAnnotation != null) {
               setSelection(activeAnnotation.getBeginTimeBoundary(), activeAnnotation.getEndTimeBoundary());
           }*/
    }

    private void annotationRemoved(Annotation annotation) {
        if ((annotation.getBeginTimeBoundary() >= sbl.getCurrentRefStartTime()) &&
                (annotation.getEndTimeBoundary() <= sbl.getCurrentRefEndTime())) {
            sbl.getRefTierOrder();
            extractCurrentSegment();

            /*
               if (activeAnnotation != null) {
               setSelection(activeAnnotation.getBeginTimeBoundary(), activeAnnotation.getEndTimeBoundary());
               }*/
        }
    }

    /**
     * Set a new Transcription for this viewer.<br>
     * We should receive a setVisibleTiers() and a setActiveTier() call after
     * this. Faking it here.
     *
     * @param transcription the new transcription.
     */
    public void setTranscription(TranscriptionImpl transcription) {
        this.transcription = transcription;
        curSBSelection = null;
        activeAnnotation = null;

        if (transcription == null) {
        	allTiers.clear();
        	visibleTiers.clear();
        	tierYPositions = new int[0];
        	setVisibleTiers(new ArrayList<TierImpl>());
        	sbl = new SBLayout(null);
        	return;
        }
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
        sbl = new SBLayout(transcription);
        sbl.getSegOrder();
        sbl.getPrevRef();
        sbl.setWorkingSegmentsRange(1, 0);
        initTiers();

        if (sbl.setBlocksVisibleAtTime(getMediaTime() + 1)) {
            extractCurrentSegment();
            updateScrollBar();
        }

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

    /**
     * Implements ElanLocaleListener.<br> Update tooltips and label.
     */
    @Override
	public void updateLocale() {
        prevButton.setText(ElanLocale.getString(
                "InterlinearViewer.PrevButton.Text"));
        nextButton.setText(ElanLocale.getString(
                "InterlinearViewer.NextButton.Text"));
        prevButton.setToolTipText(ElanLocale.getString(
                "InterlinearViewer.PrevButton.Tooltip"));
        nextButton.setToolTipText(ElanLocale.getString(
                "InterlinearViewer.NextButton.Tooltip"));

        if (popup != null) {
            //createPopupMenu();
            fontMenu.setText(ElanLocale.getString("Menu.View.FontSize"));
            activeTierMI.setText(ElanLocale.getString("Menu.Tier.ActiveTier"));
            deleteTierMI.setText(ElanLocale.getString("Menu.Tier.DeleteTier"));
            changeTierMI.setText(ElanLocale.getString(
                    "Menu.Tier.ChangeTier"));
            newAnnoMI.setText(ElanLocale.getString("Menu.Annotation.NewAnnotation"));
            newAnnoBeforeMI.setText(ElanLocale.getString(
                    "Menu.Annotation.NewAnnotationBefore"));
            newAnnoAfterMI.setText(ElanLocale.getString(
                    "Menu.Annotation.NewAnnotationAfter"));
            modifyAnnoMI.setText(ElanLocale.getString(
                    "Menu.Annotation.ModifyAnnotation"));
            deleteAnnoMI.setText(ElanLocale.getString(
                    "Menu.Annotation.DeleteAnnotation"));
        }

        revalidate();

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
        int oldSize = getFont().getSize();
        setFont(f);
        currentMetrics = getFontMetrics(getFont());
        
        Iterator<String> keyIt = prefTierFonts.keySet().iterator();
        String key = null;
        Font prFont = null;
        while (keyIt.hasNext()) {
        	key = keyIt.next();
        	prFont = prefTierFonts.get(key);
        	if (prFont != null) {
        		prefTierFonts.put(key, new Font(prFont.getName(), prFont.getStyle(), 
        				f.getSize()));
        	}
        }
        
        //pixelsForTierHeight = getFont().getSize() * 3;
        pixelsForTierHeight = getFont().getSize() + 24;
        if (oldSize != f.getSize()) {
            if (multiTierControlPanel != null) {
            	multiTierControlPanel.setFont(getFont());
            }
            notifyMultiTierControlPanel();
            extractCurrentSegment();
            vScrollBar.setBlockIncrement(pixelsForTierHeight);
            updateScrollBar();
        } else {
            extractCurrentSegment();
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
            Enumeration en = fontSizeBG.getElements();
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
        return getFont().getSize();
    }
    
    /**
     * Returns the user defined preferred font for the tier, if there is one.
     *  
     * @param tier the tier the font is to be used for
     * @return the preferred font, if there is one, otherwise the default font
     */
    private Font getFontForTier(Tier tier) {
    	if (tier != null) {
    		Font fo = prefTierFonts.get(tier.getName());
    		if (fo != null) {
    			return fo;
    		}
    	}
    	
    	return getFont();
    }

    /**
     * DOCUMENT ME!
     *
     * @param ce DOCUMENT ME!
     */
    @Override
	public void controllerUpdate(ControllerEvent ce) {
        if (sbl.setBlocksVisibleAtTime(getMediaTime())) {
            extractCurrentSegment();
            updateScrollBar();

            //buildLayout();
        } else {
            if (shouldPaint() && !playerIsPlaying()) {
                repaint();
            }
        }
    }

    /**
     * Implements MouseMotionListener
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseDragged(MouseEvent e) {
    }

    /**
     * Implements MouseMotionListener
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseMoved(MouseEvent e) {
        /*
           if (e.isShiftDown()) {
               showEmptySlots = true;
           } else {
               showEmptySlots = false;
           }
           repaint();
         */
        /* //hoover behavior
           Point pt = e.getPoint();
           if (pt.y <= VER_MARGIN) {
                       curSBSelection = null;
                       return;
           }
           pt.x += horizontalScrollOffset;
           pt.y += verticalScrollOffset;
           int size = _vTiers.size();
           int curtier = ((pt.y + (pixelsForTierHeight/2))/(pixelsForTierHeight)) - 1 ;
               //System.out.println("Curtier: " + curtier);
           if (curtier >= size || curtier < 0)
               return;
           // get current tier
           List v = (List) _vTiers.elementAt(curtier);
           // itor thru the rects and find match
           SBTime sb = null;
           int pos = -1;
           if ((pos = Collections.binarySearch(v,pt))>= 0)
           {
               sb = (SBTime) v.elementAt(pos);
               String tt = "Tag: " + sb.value + " anno: " + sb.annotation + " st: " + sb.startt + " et: " + sb.endt;
               setToolTipText(tt);
               curSBSelection = sb;
               repaint();
               return;
           }
           curSBSelection = null;
         */
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseReleased(MouseEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mousePressed(MouseEvent e) {
        // HS nov 04: e.isPopupTrigger always returns false on my PC...
        // HS jun 2006: change to discriminate Ctrl-click and Command-click on Mac
        // isRightMouseButton() returns true for both, isMetaDown() returns true for Command only 
        // but on Windows isMetaDown() returns true when the right mouse button (BUTTON3) has been pressed
        if ((SwingUtilities.isRightMouseButton(e) && (e.getButton() == MouseEvent.BUTTON1 ^ e.isMetaDown())) 
                || e.isPopupTrigger()) {
            if (e.getPoint().y < VER_MARGIN) {
                return;
            }

            Point pp = e.getPoint();

            if (popup == null) {
                createPopupMenu();
            }

            updatePopup(pp);

            if ((popup.getWidth() == 0) || (popup.getHeight() == 0)) {
                popup.show(this, pp.x, pp.y);
            } else {
                popup.show(this, pp.x, pp.y);
                SwingUtilities.convertPointToScreen(pp, this);

                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                Window w = SwingUtilities.windowForComponent(this);

                if ((pp.x + popup.getWidth()) > d.width) {
                    pp.x -= popup.getWidth();
                }

                //this does not account for a desktop taskbar
                if ((pp.y + popup.getHeight()) > d.height) {
                    pp.y -= popup.getHeight();
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

        dismissEditBox();
        curSBSelection = getTagAtPoint(e.getPoint());

        if (curSBSelection != null) {
            cursorTag = curSBSelection;
            refAnnotation = sbl.getRefAnn();
            activeAnnotation = cursorTag.annotation;
        } else {
            cursorTag = null;
            refAnnotation = null;
            activeAnnotation = null;
        }

        setActiveAnnotation(activeAnnotation);
        repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseClicked(MouseEvent e) {
        // grab keyboard focus
        requestFocus();

        if (e.getPoint().y <= VER_MARGIN) {
            return;
        } else if (e.getClickCount() == 2) {
            dismissEditBox();

            if (cursorTag != null) {
                showEditBoxForTag(cursorTag);
            } else {
                Point dp = new Point(e.getPoint());
                Tier2D tier2d = getTierAtPoint(dp);

                if (tier2d != null) {
                    autoInsertAnnotation(tier2d, e.getPoint());
                }
            }
        }

        repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseEntered(MouseEvent e) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseExited(MouseEvent e) {
        showEmptySlots = false;
        repaint();
    }

    /**
     * The use of a mousewheel needs Java 1.4!<br>
     * The scroll amount of the mousewheel is the height of a tier.
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getUnitsToScroll() > 0) {
            vScrollBar.setValue(vScrollBar.getValue() + pixelsForTierHeight);
        } else {
            vScrollBar.setValue(vScrollBar.getValue() - pixelsForTierHeight);
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
    }

    //
    public boolean isValidNewChildTag(Tier tier, SBTime sbt) {
        Tier pt = ((TierImpl) tier).getParentTier();

        if (pt == null) {
            return false;
        }

        if (((TierImpl) pt).getAnnotationAtTime(sbt.startt + 1) == null) {
            return false;
        }

        //System.out.println(((TierImpl) pt).getAnnotationAtTime(beginTime+1).getValue());
        return true;
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
        return vScrollBar.getWidth();
    }

    //	*************************************************************************************//

    /* implement ComponentListener */
    /*
     * Calculate a new BufferedImage taken the new size of the Component
     */
    @Override
	public void componentResized(ComponentEvent e) {
        buildLayout();
        vScrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
            getHeight() - defBarWidth);
        vScrollBar.revalidate();
        hScrollBar.setBounds(0, getHeight() - defBarWidth, getWidth(),
            defBarWidth);
        hScrollBar.revalidate();
        buttonPanel.setBounds(0, 0, getWidth() - defBarWidth, VER_MARGIN);
        updateScrollBar();

        /*
           if (hScrollBar != null && vScrollBar != null) {
               updateScrollBar();
           }
         */
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

    /**
     * Implements AdjustmentListener. This scrolls the image horizontally or
     * vertically.
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
        int value = e.getValue();

        if (((JScrollBar) e.getSource()).getOrientation() == JScrollBar.HORIZONTAL) {
            if (editBox.isVisible()) {
                Point p = editBox.getLocation();
                p.x += (horizontalScrollOffset - value);
                editBox.setLocation(p);
            }

            horizontalScrollOffset = value;
            if (currentSegmentWidth > MAX_BUF_WIDTH) {
                buildLayout();
            } else {
                repaint();    
            }
            
        } else {
            if (editBox.isVisible()) {
                Point p = editBox.getLocation();
                p.y += (verticalScrollOffset - value);
                editBox.setLocation(p);
            }

            verticalScrollOffset = value;
            repaint();
            notifyMultiTierControlPanel();
        }
    }

    /**
     * Implements ActionListener. Handles a click on the "previous" or "next"
     * button.
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("prev")) {
            if (allTiers.size() == 0) {
                return;
            }

            sbl.setWorkingSegmentsRange(1, -1);
            extractCurrentSegment();
            updateScrollBar();

            Annotation an = sbl.getRefAnn();

            if (an != null) {
                setMediaTime(an.getBeginTimeBoundary());
            }
        } else if (e.getActionCommand().equals("next")) {
            if (allTiers.size() == 0) {
                return;
            }

            sbl.setWorkingSegmentsRange(1, 1);
            extractCurrentSegment();
            updateScrollBar();

            Annotation an = sbl.getRefAnn();

            if (an != null) {
                setMediaTime(an.getBeginTimeBoundary());
            }
        } else if (e.getActionCommand().indexOf("font") > -1) {
            String sizeString = e.getActionCommand();
            int index = sizeString.indexOf("font") + 4;
            int size = 12;

            try {
                size = Integer.parseInt(sizeString.substring(index));
                dismissEditBox();
                updateFont(getFont().deriveFont((float) size));
                setPreference("InterlinearViewer.FontSize", Integer.valueOf(size), transcription);
            } catch (NumberFormatException nfe) {
                System.err.println("Error parsing font size");
            }
        } else if (e.getActionCommand().equals("activeTier")) {
            doActiveTier();
        } else if (e.getActionCommand().equals("deleteTier")) {
            doDeleteTier();
        } else if (e.getActionCommand().equals("changeTier")) {
            doChangeTier();
        } else if (e.getActionCommand().equals("newAnn")) {
            doNewAnnotation();
        } else if (e.getActionCommand().equals("annBefore")) {
            doAnnotationBefore();
        } else if (e.getActionCommand().equals("annAfter")) {
            doAnnotationAfter();
        } else if (e.getActionCommand().equals("modifyAnn")) {
            doModifyAnnotation();
        } else if (e.getActionCommand().equals("deleteAnn")) {
            doDeleteAnnotation();
        }
    }

    /**
     * Set the font size.
     */
	@Override
	public void preferencesChanged() {
		Integer prefFontSize = Preferences.getInt("InterlinearViewer.FontSize", transcription);
		int fontSize;
		if (prefFontSize != null) {
			fontSize = prefFontSize;
			setFontSize(fontSize);
		} else {
			fontSize = getFontSize();			
		}
		// preferred fonts
		Map<String, Font> foMap = Preferences.getMapOfFont("TierFonts", transcription);
		if (foMap != null) {

			for (Map.Entry<String, Font> e : foMap.entrySet()) {
				String key = e.getKey();
				Font ft = e.getValue();
				
				if (key != null && ft != null) {
					for (Tier2D t2d : allTiers) {
						if (t2d.getName().equals(key)) {
							break;
						}
					}
					// use the size of the default font unless defined differently
					if (prefTierFonts.containsKey(key)) {
						Font oldF = prefTierFonts.get(key);
						if (!oldF.getName().equals(ft.getName()) && oldF.getStyle() != ft.getStyle()) {
							prefTierFonts.put(key, new Font(ft.getName(), 
									ft.getStyle(), fontSize));
							extractCurrentSegment();
						}
					} else {
						prefTierFonts.put(key, new Font(ft.getName(), ft.getStyle(), fontSize));
						extractCurrentSegment();
					}					
				}
			}	
			List<String> remKeys = new ArrayList<String>(4);
			for (String key : prefTierFonts.keySet()) {
				if (!foMap.containsKey(key)) {
					remKeys.add(key);
					prefTierFonts.remove(key);
				}
			}
			if (!remKeys.isEmpty()) {
				for (String key : remKeys) {
					prefTierFonts.remove(key);
				}
			}
		}
		
        Boolean val = Preferences.getBool("InlineEdit.EnterCommits", null);

        if (val != null) {
            editBox.setEnterCommits(val.booleanValue());
        }
        val = Preferences.getBool("InlineEdit.DeselectCommits", null);

        if (val != null) {
            deselectCommits = val.booleanValue();
        } 
        
		extractCurrentSegment();
	}
	
    //the edit actions from the popup menu, the Commands and Actions will come in here
    private void doActiveTier() {
        if ((rightClickTier == null) || (multiTierControlPanel == null)) {
            return;
        }

        multiTierControlPanel.setActiveTier(rightClickTier.getTier());
    }

    private void doDeleteTier() {
        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.EDIT_TIER);

        Object[] args = new Object[] {
        		Integer.valueOf(EditTierDialog2.DELETE), rightClickTier.getTier()
            };

        c.execute(transcription, args);
    }

    private void doChangeTier() {
        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.EDIT_TIER);

        Object[] args = new Object[] {
        		Integer.valueOf(EditTierDialog2.CHANGE), rightClickTier.getTier()
            };

        c.execute(transcription, args);
    }

    private void doNewAnnotation() {
        if (rightClickTier == null) {
            return;
        }

        TierImpl tier = rightClickTier.getTier();
        long begin = -1;
        long end = -1;

        if (tier.isTimeAlignable() && (cursorTag != null) &&
                (cursorTag.startt > -1)) {
            begin = cursorTag.startt;
            end = cursorTag.endt;
        } else {
            if ((rightClickTag != null) &&
                    (rightClickTag.startt == rightClickTag.endt)) {
                begin = rightClickTag.startt + 1;
                end = rightClickTag.startt + 1;
            }
        }

        if ((begin != -1) && (end != -1)) {
            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.NEW_ANNOTATION);
            Object[] args = new Object[] {
                    new Long(begin), new Long(end)
                };
            c.execute(tier, args);

            Annotation aa = tier.getAnnotationAtTime(begin);

            if (aa != null) {
                setActiveAnnotation(aa);
                showEditBoxForAnnotation(aa);

                if (editBox.isVisible()) {
                    editBox.requestFocus();
                }
            }
        }
    }

    /**
     * Annotation before is relative to the cursor tag (active annotation)
     */
    private void doAnnotationBefore() {
        if ((rightClickTier == null) || (cursorTag == null)) {
            return;
        }

        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.NEW_ANNOTATION_BEFORE);
        Object[] args = new Object[] { cursorTag.annotation };
        c.execute(rightClickTier.getTier(), args);

        Annotation aa = rightClickTier.getTier().getAnnotationBefore(cursorTag.annotation);

        if (aa != null) {
            setActiveAnnotation(aa);
            showEditBoxForAnnotation(aa);

            if (editBox.isVisible()) {
                editBox.requestFocus();
            }
        }
    }

    /**
     * Annotation after is relative to the cursor tag (active annotation)
     */
    private void doAnnotationAfter() {
        if ((rightClickTier == null) || (cursorTag == null)) {
            return;
        }

        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.NEW_ANNOTATION_AFTER);
        Object[] args = new Object[] { cursorTag.annotation };
        c.execute(rightClickTier.getTier(), args);

        Annotation aa = rightClickTier.getTier().getAnnotationAfter(cursorTag.annotation);

        if (aa != null) {
            setActiveAnnotation(aa);
            showEditBoxForAnnotation(aa);

            if (editBox.isVisible()) {
                editBox.requestFocus();
            }
        }
    }

    private void doModifyAnnotation() {
        if ((cursorTag != null) && (cursorTag.annotation != null)) {
            showEditBoxForTag(cursorTag);
        }
    }

    private void doDeleteAnnotation() {
        if ((cursorTag != null) && (cursorTag.annotation != null)) {
            Tier tier = rightClickTier.getTier();
            Annotation aa = cursorTag.annotation;

            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.DELETE_ANNOTATION);
            c.execute(tier, new Object[] { getViewerManager(), aa });
        }
    }

    /**
     * A private class that implements the Comparable interface used by a
     * vector in Collection.binarySearch. Needed to modify the compareTo
     * method used by Hashtable. This will implement the concept of begin/end
     * time to the comparison.<br>
     * <b>Note: </b>HS 07 aug 2003 added field for the value of the Tag<br>
     * <b>Note: </b>HS 25 sep 2003 added field for a reference to the
     * annotation; needed for a reliable way to handle the active annotation /
     * cursortag. The annotation can be null. The annotation reference is also
     * needed for editing purposes.
     */
    private class SBTime implements Comparable<Object> {
        /** Holds value of property DOCUMENT ME! */
        public Annotation annotation;

        /** Holds value of property DOCUMENT ME! */
        public long startt = 0;

        /** Holds value of property DOCUMENT ME! */
        public long endt = 0;

        /** Holds value of property DOCUMENT ME! */
        public Rectangle _rect;

        /** Holds value of property DOCUMENT ME! */
        public String value;

        /**
         * Creates a new SBTime instance
         *
         * @param a DOCUMENT ME!
         * @param st DOCUMENT ME!
         * @param et DOCUMENT ME!
         * @param rect DOCUMENT ME!
         * @param val DOCUMENT ME!
         */
        public SBTime(Annotation a, long st, long et, Rectangle rect, String val) {
            annotation = a;
            startt = st;
            endt = et;
            _rect = rect;
            value = val;
        }

        /**
         * DOCUMENT ME!
         *
         * @param o DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        @Override
		public int compareTo(Object o) {
            if (o instanceof Long) {
                long wl = 0;
                wl = ((Long) o).longValue();

                if ((wl >= startt) && (wl <= endt)) {
                    return 0;
                }

                if (startt == -1) {
                    return 1;
                }

                if (endt < wl) {
                    return -1;
                }
            }

            if (o instanceof Point) {
                Point pt = (Point) o;

                if (_rect.contains(pt.x, pt.y)) {
                    return 0;
                }

                if (_rect.x < pt.x) {
                    return -1;
                }
            }

            return 1;
        }
    }

}
