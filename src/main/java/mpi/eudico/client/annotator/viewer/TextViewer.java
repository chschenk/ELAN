package mpi.eudico.client.annotator.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
//import javax.swing.text.DefaultHighlighter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.InlineEditBox;
import mpi.eudico.client.annotator.spellcheck.SpellChecker;
import mpi.eudico.client.annotator.spellcheck.SpellCheckerRegistry;
import mpi.eudico.client.annotator.spellcheck.SpellCheckerUtil;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.Pair;


/**
 * Viewer for text of a selected tier with highligting where the cursor is.
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public class TextViewer extends AbstractViewer implements SingleTierViewer,
    ACMEditListener, ActionListener {
    private JMenu fontMenu;
    private ButtonGroup fontSizeBG;
    private int fontSize;
    private JPopupMenu popup;
    private JMenuItem centerMI;
    private JTextArea taText;
    private JScrollPane jspText;
    private TierImpl tier;
    private List<? extends Annotation> annotations = new ArrayList<Annotation>();
    //private int index = 0;
    private long begintime = 0;
    private long endtime = 0;
    private long[] arrTagTimes;
    private int[] arrTagPositions;
    private String tierText = "";
    private Highlighter highlighter;
	private StyledHighlightPainter selectionPainter;
	private StyledHighlightPainter currentPainter;
	private StyledHighlightPainter activeAnnotationPainter;
	private StyledHighlightPainter spellingErrorPainter;
	private ValueHighlightPainter valuePainter;
	private Object selectionHighLightInfo;
	private Object currentHighLightInfo;
	private List<Object> spellingErrorHighLightInfos;
	private Object activeHighLightInfo;
	private List<Object> valueHighLightInfos;
    private int indexActiveAnnotationBegin = 0;
    private int indexActiveAnnotationEnd = 0;
    private int indexSelectionBegin = 0;
    private int indexSelectionEnd = 0;
    private int indexMediaTime = 0;
    private boolean bVisDotted; //used for visualization with dots
    private int extraLength; //used for visualization with dots
    
    private boolean centerVertically = true;
    private final Color transparent;
    private boolean enterCommits = true;
    private final String DOTS = "\u0020\u0020\u00B7\u0020\u0020";
    
    /** The language reference (ISO 639-2) for the current tier */
	private String tierLanguageRef;
	private SwingWorker<Void, Void> worker = null;

    /**
     * Constructor
     */
    public TextViewer() {
        bVisDotted = true;
        extraLength = 4;
		transparent = new Color(
			Constants.SELECTIONCOLOR.getRed(),
			Constants.SELECTIONCOLOR.getGreen(),
			Constants.SELECTIONCOLOR.getBlue(),
			0);
		
        try {
            setLayout(new BorderLayout());

            taText = new JTextArea(4, 10) { //don't eat up any key events
                        @Override
						protected boolean processKeyBinding(KeyStroke ks,
                            KeyEvent e, int condition, boolean pressed) {
                            return false;
                        }
                        
                        /**
                         * Override to set rendering hints.
                         */
                        /* only for J 1.4, new solutions for 1.5 and 1.6
                        public void paintComponent(Graphics g) {
                        	if (g instanceof Graphics2D) {
                                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        	}
                        	super.paintComponent(g);
                        }
                        */
                    };

            taText.setFont(Constants.DEFAULTFONT);
            fontSize = 12;
            taText.setLineWrap(true);
            taText.setWrapStyleWord(true);
            taText.setForeground(Constants.DEFAULTFOREGROUNDCOLOR);
            taText.setEditable(false);
            taText.addMouseListener(new TextViewerMouseListener(taText));
            taText.addMouseMotionListener(new TextViewerMouseMotionListener());
            taText.getCaret().setSelectionVisible(false);
            taText.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            taText.setSelectionColor(taText.getBackground());
            highlighter = taText.getHighlighter();
            selectionPainter = new StyledHighlightPainter(Constants.SELECTIONCOLOR, 1, StyledHighlightPainter.FILLED);
			selectionPainter.setVisible(false);
            currentPainter = new StyledHighlightPainter(Constants.CROSSHAIRCOLOR, 0);
            currentPainter.setVisible(false);
            spellingErrorPainter = new StyledHighlightPainter(Color.RED, 0, StyledHighlightPainter.SQUIGGLED);
            spellingErrorPainter.setVisible(false);
            activeAnnotationPainter = new StyledHighlightPainter(Constants.ACTIVEANNOTATIONCOLOR, 1);
            activeAnnotationPainter.setVisible(false);
            valuePainter = new ValueHighlightPainter(null, 0);
            valuePainter.setVisible(false);
            valueHighLightInfos = new ArrayList<Object>();
            
			currentHighLightInfo =  highlighter.addHighlight(0, 0, currentPainter);
            spellingErrorHighLightInfos =  new ArrayList<Object>();
			activeHighLightInfo = highlighter.addHighlight(0, 0, activeAnnotationPainter);
			selectionHighLightInfo = highlighter.addHighlight(0, 0, selectionPainter);
			highlighter.addHighlight(0, 0, valuePainter);

            jspText = new JScrollPane(taText);
            jspText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jspText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            //            jspText.setViewportView(taText);
            add(jspText, BorderLayout.CENTER);

            ///////////////////////////////////////////////////////////////////////
            //
            // Temporary code
            // highlighter.addHighlight in doUpdate gives an error when tabpane is
            // not visible. When not visible (in sync mode) the size of tabpane is
            // set to 0. So check size in doUpdate and if 0 return.
            //
            ///////////////////////////////////////////////////////////////////////
            addComponentListener(new TextViewerComponentListener());

            setVisible(true);
        } catch (Exception ex) {
//            ex.printStackTrace();
            ClientLogger.LOG.warning("Exception while setting up the TextViewer: " + ex.getMessage());
        }
    }

    /**
     * AR notification that the selection has changed method from SelectionUser
     * not implemented in AbstractViewer
     */
    @Override
	public void updateSelection() {
        doUpdate();
    }

    /**
     * AR heeft dit hier neergezet, zie abstract viewer voor get en set
     * methodes van ActiveAnnotation. Update method from ActiveAnnotationUser
     */
    @Override
	public void updateActiveAnnotation() {
        doUpdate();
    }

    /**
     * AR heeft dit hier neergezet Er moet nog gepraat worden over wat hier te
     * doen valt
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void ACMEdited(ACMEditEvent e) {
    	if (tier == null) {
    		return;
    	}
        switch (e.getOperation()) {
        case ACMEditEvent.ADD_ANNOTATION_HERE:
        case ACMEditEvent.ADD_ANNOTATION_BEFORE:
        case ACMEditEvent.ADD_ANNOTATION_AFTER: {
        	// check on which tier, from this tier to root tier
        	// update what is necessary
        	if (e.getInvalidatedObject() instanceof TierImpl) {
        		Tier t = (Tier) e.getInvalidatedObject();
        		if (t == tier || tier.hasAncestor(t)) {
        			updateAnnotations();
        		}
        	}
        	
        	break;
        }
        /* do nothing
        case ACMEditEvent.CHANGE_ANNOTATION_TIME: {
        	if (e.getInvalidatedObject() instanceof AlignableAnnotation) {
                TierImpl invTier = (TierImpl) ((AlignableAnnotation) e.getInvalidatedObject()).getTier();
        	}
        	break;
        }
        */
        case ACMEditEvent.CHANGE_ANNOTATION_VALUE: {
        	// check for this tier only
        	if (e.getInvalidatedObject() instanceof Annotation) {
                Tier invTier = ((Annotation) e.getInvalidatedObject()).getTier();
                if (invTier == tier) {
                	updateAnnotations();
                }
        	}
        	break;
        }
        case ACMEditEvent.CHANGE_ANNOTATION_TIME:	
        case ACMEditEvent.CHANGE_ANNOTATIONS:
        case ACMEditEvent.REMOVE_ANNOTATION: {
        	// always update everything
            setTier(getTier());// expensive reads and sets preferences
            if (tier != null) {
            	doUpdate();
            }
        	break;
        }
        }
    }

    /**
     * method from ElanLocaleListener not implemented in AbstractViewer
     */
    @Override
	public void updateLocale() {
        createPopup();
    }

    private void createPopup() {
        popup = new JPopupMenu("");
		
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

        //add visualization toggle
        JMenuItem menuItem = new JMenuItem(ElanLocale.getString(
                    "TextViewer.ToggleVisualization"));
        menuItem.setActionCommand("TOGGLEVISUALIZATION");
        menuItem.addActionListener(this);
		popup.add(menuItem);
		
		centerMI = new JCheckBoxMenuItem(ElanLocale.getString(
			"TextViewer.CenterVertical"));
		centerMI.setSelected(centerVertically);
		centerMI.setActionCommand("centerVert");
		centerMI.addActionListener(this);
        
        popup.add(centerMI);
        popup.addSeparator();
        JMenuItem copyItem = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        copyItem.setActionCommand("copy");
        copyItem.addActionListener(this);
        popup.add(copyItem);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        String strAction = e.getActionCommand();

        if (strAction.indexOf("font") != -1) {
            int index = strAction.indexOf("font") + 4;

            try {
                fontSize = Integer.parseInt(strAction.substring(index));
                //repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //taText.setFont(getFont().deriveFont((float) fontSize));
			taText.setFont(taText.getFont().deriveFont((float) fontSize));
            setPreference("TextViewer.FontSize", Integer.valueOf(fontSize), 
            		getViewerManager().getTranscription());
        } else if (strAction.equals("TOGGLEVISUALIZATION")) {
            bVisDotted = !bVisDotted;
            extraLength = bVisDotted ? 4 : 0;
            
            setPreference("TextViewer.DotSeparated", Boolean.valueOf(bVisDotted), 
            		getViewerManager().getTranscription());
            
            setTier(getTier());
        } else if (strAction == "centerVert") {
        	setCenteredVertically(centerMI.isSelected());
            setPreference("TextViewer.CenterVertical", Boolean.valueOf(centerVertically), 
            		getViewerManager().getTranscription());
        } else if (strAction.equals("copy")) {
        	taText.copy();
        }
    }

    /**
     * AR notification that some media related event happened method from
     * ControllerListener not implemented in AbstractViewer
     * <p>
     * This is called from a separate thread. However Swing calls are only
     * allowed from the Event Dispatch Thread.
     */
    @Override
	public void controllerUpdate(ControllerEvent event) {
    	SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
		        doUpdate();
			}});
    }
    
    /**
     * Change font size and dot-separation.
     */
	@Override
	public void preferencesChanged() {
		Integer fontSi = Preferences.getInt("TextViewer.FontSize", 
				getViewerManager().getTranscription());
		if (fontSi != null) {
			setFontSize(fontSi.intValue());
		}
		Boolean dotSep = Preferences.getBool("TextViewer.DotSeparated", 
				getViewerManager().getTranscription());
		if (dotSep != null) {
			setDotSeparated(dotSep.booleanValue());
		}
		Boolean vertCent = Preferences.getBool("TextViewer.CenterVertical", 
				getViewerManager().getTranscription());
		if (vertCent != null) {
			setCenteredVertically(vertCent.booleanValue());
		}
		
		loadFont(tier);
		
        Boolean boolPref = Preferences.getBool("InlineEdit.EnterCommits", null);

        if (boolPref != null) {
            enterCommits = boolPref.booleanValue();
        }
        // make sure the colors for cv entries are updated
        if (this.tier != null) {
        	Object cvPrefs = Preferences.get(Preferences.CV_PREFS, tier.getTranscription());
        	if (cvPrefs != null && tier.getLinguisticType().getControlledVocabularyName() != null) {
        		setTier(tier);
        	}
        }
        
        paintSpellErrorUnderline(); // TODO check if the changed preferences are actually spell checking related 
	}
	
	/**
	 * Loads the font that has been set for the specified tier.
	 * 
	 * @param tier the selected tier
	 */
	private void loadFont(Tier tier) {
		if (tier != null) {
			Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", getViewerManager().getTranscription());
			if (fonts != null) {
				Font tf = fonts.get(tier.getName());
				if (tf != null) {
					taText.setFont(new Font(tf.getName(), Font.PLAIN, fontSize));
				} else {
					taText.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
				}
			}
		} else {
			taText.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
		}
	}
	
	/**
	 * Calculates the position and size of the crosshair painter and requests 
	 * the text area to scroll the resulting rectangle to be visible in the view.
	 * In this calculation the <code>centerVertically</code> value is taken into
	 * account. 
	 */
    private void scrollIfNeeded() {
        try {
            Highlighter.Highlight[] h_arr = highlighter.getHighlights();

            for (Highlight element : h_arr) {
                if (element.getPainter() == currentPainter) {
                    int idx = element.getStartOffset();
					int ide = element.getEndOffset();
					
					Rectangle rect = taText.modelToView(idx);
					Rectangle endRect = taText.modelToView(ide);
					if (rect == null || endRect == null) {
						return;
					}
					Rectangle union = rect.union(endRect);
					
					if (centerVertically) {
						int restY = jspText.getViewport().getHeight() - union.height;
						union.y = union.y - restY / 2;
						if (union.y < 0) {
							union.y = 0;
						}
						// the +1 for some reason results in a smoother scrolling...
						union.height = union.height + restY + 1;
						
						taText.scrollRectToVisible(union);
					} else {					
	                    //taText.scrollRectToVisible(taText.modelToView(idx));
						taText.scrollRectToVisible(union);
					}
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getIndexBegin(int index, long beginTime) {
        int indexj;
        int retIndex = -1;

        for (int j = -2; j <= 2; j++) {
            indexj = index + j;

            if ((indexj >= 0) && (indexj < arrTagTimes.length)) {
                if (arrTagTimes[indexj] <= beginTime) {
                    retIndex = indexj;
                }
            }
        }

        if (retIndex == -1) {
            return index;
        } else {
            return retIndex;
        }
    }

    private int getIndexEnd(int index, long endTime) {
        int indexj;
        int retIndex = -1;

        for (int j = 2; j >= -2; j--) {
            indexj = index + j;

            if ((indexj >= 0) && (indexj < arrTagTimes.length)) {
                if (arrTagTimes[indexj] >= endTime) {
                    retIndex = indexj;
                }
            }
        }

        if (retIndex == -1) {
            return index;
        } else {
            return retIndex;
        }
    }

    /**
     * Update the complete text viewer Determines whether current time is in a
     * selection Sets the correct value in the text viewer
     */
    public void doUpdate() {
        ///////////////////////////////////////////////////////////////////////
        //
        // Temporary code
        // highlighter.addHighlight in doUpdate gives an error when tabpane is
        // not visible. When not visible (in sync mode) the size of tabpane is
        // set to 0. So check size in doUpdate and if <= 0 return.
        //
        ///////////////////////////////////////////////////////////////////////
        Dimension dim = getSize();

        if ((dim.height <= 0) || (dim.width <= 0)) {
            return;
        }

        ///////////////////////////////////////////////////////////////////////
        //
        // End temporary code
        //
        ///////////////////////////////////////////////////////////////////////
        if (arrTagTimes == null) {
            return;
        } else if (arrTagTimes.length == 0) {
        	repaint();
        	return;
        }

        //boolean bFound = false;
        long mediatime = getMediaTime();
        //int annotations_size = annotations.size();
        int indexj = 0;
        int index = 0;
        long activeAnnotationBeginTime = 0;
        long activeAnnotationEndTime = 0;
        indexActiveAnnotationBegin = 0;
        indexActiveAnnotationEnd = 0;

        long selectionBeginTime = getSelectionBeginTime();
        long selectionEndTime = getSelectionEndTime();
        boolean selBTBetween = false;
        boolean selETBetween = false;

        Annotation activeAnnotation = getActiveAnnotation();

        if ((activeAnnotation != null) && (activeAnnotation.getTier() == tier)) {
            activeAnnotationBeginTime = activeAnnotation.getBeginTimeBoundary();
            activeAnnotationEndTime = activeAnnotation.getEndTimeBoundary();
        }

        //select the appropriate text
        try {
            if ((activeAnnotation != null) &&
                    (activeAnnotation.getTier() == tier)) {
                //VALUES FOR HIGHLIGHTING ACTIVE ANNOTATION
                //use fast search to determine approximate index from array
                index = Math.abs(Arrays.binarySearch(arrTagTimes,
                            activeAnnotationBeginTime));

                //look for the 2 surrounding indexes
                //index from active annotation begin
                indexActiveAnnotationBegin = getIndexBegin(index,
                        activeAnnotationBeginTime);

                index = Math.abs(Arrays.binarySearch(arrTagTimes,
                            activeAnnotationEndTime));

                //index from active annotation end
                indexActiveAnnotationEnd = getIndexEnd(index,
                        activeAnnotationEndTime);
            }

            if ((selectionBeginTime == 0) && (selectionEndTime == 0)) {
                //selection is cleared
                indexSelectionBegin = 0;
                indexSelectionEnd = 0;
            } else {
                //VALUES FOR HIGHLIGHTING SELECTION
                //use fast search to determine approximate index from array
            	int bindex = Arrays.binarySearch(arrTagTimes, selectionBeginTime);
            	selBTBetween = bindex < 0 && bindex % 2 != 0;
                index = bindex == -1? 0 : Math.abs(bindex);

                //look for the 2 surrounding indexes
                //index from selection begin
                indexSelectionBegin = getIndexBegin(index, selectionBeginTime);
                int eindex = Arrays.binarySearch(arrTagTimes, selectionEndTime);
                selETBetween = eindex < 0 && eindex % 2 != 0;
                index = Math.abs(eindex);

                //index from selection end
                indexSelectionEnd = getIndexEnd(index, selectionEndTime);

                if (indexSelectionEnd >= arrTagPositions.length) {
                    indexSelectionEnd = arrTagPositions.length - 1;
                }
            }

            //VALUES FOR HIGHLIGHTING CURRENT MEDIATIME
            int insIndex = Arrays.binarySearch(arrTagTimes, mediatime);
            // a positive value means exactly a begin or end time of an annotation
            // uneven negative value = a time value within an annotation
            // even negative value = a time value between annotations (the "dots" in dotted visualization)
            boolean cursBetweenAnn = insIndex < 0 && insIndex % 2 != 0;
            index = Math.abs(insIndex);

            for (int j = -2; j <= 2; j++) {
                indexj = index + j;

                //needed to check whether no 'crosshair' should be drawn at left of first annotation
                if (indexj < 0 && index != 0) {
                    indexMediaTime = -1;

                    break;
                }

                if ((indexj >= 0) && (indexj < arrTagTimes.length)) {
                    if (arrTagTimes[indexj] <= mediatime) {
                        indexMediaTime = indexj;
                    }
                }
            }
            
            // adjust painters
            try {
				if ((indexMediaTime >= 0) &&
						((indexMediaTime + 1) < arrTagPositions.length)) {
					currentPainter.setVisible(true);
					if (!bVisDotted) {// highlight an annotation or a space between annotations
						highlighter.changeHighlight(currentHighLightInfo, arrTagPositions[indexMediaTime],
								arrTagPositions[indexMediaTime + 1]);
					} else if (!cursBetweenAnn && indexMediaTime % 2 == 0) {// highlight an annotation
						highlighter.changeHighlight(currentHighLightInfo, arrTagPositions[indexMediaTime],
								arrTagPositions[indexMediaTime + 1] - extraLength);
					} else {// highlight a "dot" between annotations. if media time == end time of an annotation it is "outside"
						highlighter.changeHighlight(currentHighLightInfo, arrTagPositions[indexMediaTime] - (extraLength / 2 + 1),
								arrTagPositions[indexMediaTime + 1] - 2);
					}
				} else if (indexMediaTime == arrTagPositions.length - 1 && bVisDotted) {
					// highlight last dot
					currentPainter.setVisible(true);
					highlighter.changeHighlight(currentHighLightInfo, arrTagPositions[indexMediaTime] - (extraLength / 2 + 1),
							arrTagPositions[indexMediaTime] - 1);
				} else {
					currentPainter.setVisible(false);
					// position the crosshair painter even if it is before the first annotation
					// or after the last; this way the painter's position can be used 
					// to scroll to begin or end of the document
					if (indexMediaTime < 0 && arrTagPositions.length > 1) {
						highlighter.changeHighlight(currentHighLightInfo, arrTagPositions[0], 
							arrTagPositions[1]);
					} else if (indexMediaTime  + 1 >= arrTagPositions.length && 
						arrTagPositions.length > 1) {
						highlighter.changeHighlight(currentHighLightInfo, 
							arrTagPositions[arrTagPositions.length - 2], 
							arrTagPositions[arrTagPositions.length - 1]);
					}				
				}
            } catch (BadLocationException ble) {
            	ClientLogger.LOG.warning("Error media position highlight: " + ble.getMessage());
            	currentPainter.setVisible(false);
            }

            try {
				if ((indexActiveAnnotationBegin >= 0) &&
					(indexActiveAnnotationBegin < arrTagPositions.length) &&
					(indexActiveAnnotationEnd >= 0) &&
					(indexActiveAnnotationEnd < arrTagPositions.length)) {
					if (activeAnnotation != null && activeAnnotation.getTier() == tier) {
						activeAnnotationPainter.setVisible(true);
						highlighter.changeHighlight(activeHighLightInfo,
								arrTagPositions[indexActiveAnnotationBegin],
							arrTagPositions[indexActiveAnnotationEnd] - extraLength);
					} else {
						activeAnnotationPainter.setVisible(false);
					}
				} else {
					activeAnnotationPainter.setVisible(false);
				}
            } catch (BadLocationException ble) {
            	ClientLogger.LOG.warning("Error active annotation highlight: " + ble.getMessage());
            	activeAnnotationPainter.setVisible(false);
            }
            
            try {
				if ((indexSelectionBegin >= 0) &&
						(indexSelectionBegin < arrTagPositions.length) &&
						(indexSelectionEnd > 0) &&
						(indexSelectionEnd < arrTagPositions.length)) {
						selectionPainter.setVisible(true);
						if (bVisDotted) {
							// correct indices based on the flags selBTBetween and selETBetween
							int btPos = arrTagPositions[indexSelectionBegin];
							if (selBTBetween && btPos > extraLength / 2) {
								// include the previous dot
								btPos = btPos - (extraLength / 2) - 1;
							}
							
							int etPos = arrTagPositions[indexSelectionEnd];
							if (selETBetween) {
								// include the next dot
								etPos = etPos - (extraLength / 2) + 1;
							} else {
								etPos -= extraLength;
							}
							etPos = etPos > btPos ? etPos : btPos + 1;
							
							highlighter.changeHighlight(selectionHighLightInfo,
									btPos, etPos);
						} else {
							highlighter.changeHighlight(selectionHighLightInfo,
								arrTagPositions[indexSelectionBegin],
								Math.max(arrTagPositions[indexSelectionBegin] + 1, 
										arrTagPositions[indexSelectionEnd]));
						}
				} else {
					selectionPainter.setVisible(false);
				}
            } catch (BadLocationException ble) {
            	ClientLogger.LOG.warning("Error interval selection highlight: " + ble.getMessage());
            	selectionPainter.setVisible(false);
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
        	ClientLogger.LOG.warning("Update error: " + ex.getMessage());
        }

        //repaint();

        scrollIfNeeded();
        
        repaint();
    }
    
    private void updateAnnotations() {
    	annotations = this.tier.getAnnotations();
        for (Object obj : valueHighLightInfos) {
        	highlighter.removeHighlight(obj);
        }
        valueHighLightInfos.clear();
        
        buildArrayAndText();
        taText.setText(tierText);
        
        // now that the text has been set, the indices of the cv value highlights have
        // to be repositioned. Get the begin positions from the painter. Do it here instead of in doUpdate
        if (valueHighLightInfos.size() > 0) {
        	Map<Integer, Color> cm = valuePainter.getColors();
        	if (cm != null) {
        		ArrayList<Integer> kl =new ArrayList<Integer>(cm.keySet());
        		Collections.sort(kl);
        		if (kl.size() == valueHighLightInfos.size()) {
        			int j = 0;
        			int pos = 0;
        			for (int i = 0; i < kl.size(); i++) {
        				pos = kl.get(i);
        				while (j < arrTagPositions.length - 1) {
        					if (arrTagPositions[j] == pos) {
        						try {
        							highlighter.changeHighlight(valueHighLightInfos.get(i), pos, arrTagPositions[j + 1] - extraLength);
        							j++;
        						} catch (BadLocationException ble) {
        							ClientLogger.LOG.warning("Error CV values highlight: " + ble.getMessage());
        						}
        						break;
        					}
        					j++;
        				}
        			}
        		}
        	}
        }
        
        paintSpellErrorUnderline();
        
		// call doUpdate after the TextArea has finished updating itself
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				doUpdate();
			}
		});
    }

    /**
     * Puts squiggly underline under incorrect words in annotations! 
     */
	private void paintSpellErrorUnderline() {
		if(worker != null && !worker.isDone()) {
			return;
		}
		
		worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				// Clean up 
				for (Object obj : spellingErrorHighLightInfos) {
		        	highlighter.removeHighlight(obj);
		        }
		        spellingErrorHighLightInfos.clear();
		        
		        // If there is no spell checker for the current tier's language, don't do anything
		        if(!SpellCheckerRegistry.getInstance().hasSpellCheckerLoaded(tierLanguageRef)) {
		        	return null;
		        }
		        
		        SpellChecker checker = SpellCheckerRegistry.getInstance().getSpellChecker(tierLanguageRef);
		        
		        // Paint squiggly line under incorrect words in annotation values
	        	for(int annIndex = 0; annIndex < annotations.size(); annIndex++) {
	            	Annotation ann = annotations.get(annIndex);
	            	String annValue = ann.getValue();
	            	
	            	// Get suggestions for the whole annotation, i.e. all words together 
	            	// (spell checker may use context)
	            	List<Pair<String, List<String>>> suggestions = checker.getSuggestions(ann.getValue());

	            	if(SpellCheckerUtil.hasSuggestions(suggestions)) { // Is there any mistake?
		            	String[] annElements = annValue.split("\\b"); // Split on word boundary to include spaces
		            	
		        		// Get the corresponding tag begin
		    			long annotationBeginTime = ann.getBeginTimeBoundary();
		    			int indexAnnBegin = Math.abs(Arrays.binarySearch(arrTagTimes,
		    					annotationBeginTime));
		    			if(arrTagTimes[indexAnnBegin + 1] == arrTagTimes[indexAnnBegin]) {
		    				indexAnnBegin++;
		    			}
		    			
		                // Underline individual words
		                int indexWordBegin = 0; // Indicates the begin index of the current word
		                int wordIndex = 0; // Index for actual words in the annotation
		    			for(int elementIndex = 0; elementIndex < annElements.length; elementIndex++) {
		    				int elementLength = annElements[elementIndex].length();
		    				
							if(annElements[elementIndex].matches(".*\\p{L}.*")) {
		    					if(SpellCheckerUtil.isSuggestion(suggestions.get(wordIndex))) {
		    						int start = arrTagPositions[indexAnnBegin] + indexWordBegin;
		                    		int end = start + elementLength;
				                    Object hl = highlighter.addHighlight(start,	end, spellingErrorPainter);
				                    spellingErrorHighLightInfos.add(hl);
		    					}
		    					wordIndex++; // Update this index when there was a word 
		    				}
							indexWordBegin += elementLength;
		    			}
	            	}
	        	}
	    		
	            spellingErrorPainter.setVisible(true);
				return null;
			}
			
			@Override
			public void done() {
				repaint();
				validate();
			}
			
		};
		
		worker.execute();
	}

    /**
     * Sets the tier which is shown in the text viewer
     *
     * @param tier The tier which should become visible
     */
    @Override
	public void setTier(Tier tier) {
        // added by AR
        if (tier == null) {
            this.tier = null;
            annotations = new ArrayList<Annotation>();
            setPreference("TextViewer.TierName", tier, 
            		getViewerManager().getTranscription());
            taText.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
        } else {        	
            this.tier = (TierImpl) tier;

            try {
                annotations = this.tier.getAnnotations();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setPreference("TextViewer.TierName", tier.getName(), 
            		getViewerManager().getTranscription());
            
            //preferencesChanged();// leads to eternal loop in case of CV colors!
            loadFont(this.tier);
            
            // Set spellchecker for tier language
            tierLanguageRef = tier.getLangRef();
        }

        for (Object obj : valueHighLightInfos) {
        	highlighter.removeHighlight(obj);
        }
        valueHighLightInfos.clear();
        
        buildArrayAndText();
        taText.setText(tierText);
        
        // now that the text has been set, the indices of the cv value highlights have
        // to be repositioned. Get the begin positions from the painter. Do it here instead of in doUpdate
        if (valueHighLightInfos.size() > 0) {
        	Map<Integer, Color> cm = valuePainter.getColors();
        	if (cm != null) {
        		ArrayList<Integer> kl =new ArrayList<Integer>(cm.keySet());
        		Collections.sort(kl);
        		if (kl.size() == valueHighLightInfos.size()) {
        			int j = 0;
        			int pos = 0;
        			for (int i = 0; i < kl.size(); i++) {
        				pos = kl.get(i);
        				while (j < arrTagPositions.length - 1) {
        					if (arrTagPositions[j] == pos) {
        						try {
        							highlighter.changeHighlight(valueHighLightInfos.get(i), pos, arrTagPositions[j + 1] - extraLength);
        							j++;
        						} catch (BadLocationException ble) {
        							ClientLogger.LOG.warning("Error CV values highlight after changing the tier: " + ble.getMessage());
        						}
        						break;
        					}
        					j++;
        				}
        			}
        		}
        	}
        }

        paintSpellErrorUnderline();
        
		// call doUpdate after the TextArea has finished updating itself
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				doUpdate();
			}
		});
				
        //doUpdate();
    }

    /**
     * Gets the tier which is shown in the subtitle viewer
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Tier getTier() {
        return tier;
    }
    
	/**
	 * Returns the current font size.
	 * 
	 * @return the current font size
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * Sets the font size.
	 * 
	 * @param size the new font size
	 */
	public void setFontSize(int size) {
		fontSize = size;
		if (fontSizeBG != null) {
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
						taText.setFont(taText.getFont().deriveFont((float) fontSize));
						break;
					}
				} catch (NumberFormatException nfe) {
					//// do nothing
				}
			}
		} else {		
			createPopup();
			taText.setFont(taText.getFont().deriveFont((float) fontSize));
		}
	}
	
	/**
	 * Returns whether the annotation values are visualized separated by a dot.
	 * 
	 * @return true if the annotation values are visualized separated by a dot, 
	 * false otherwise 
	 */
	public boolean isDotSeparated() {
		return bVisDotted;
	}
	
	/**
	 * Sets the visualization of the annotation values.
	 * 
	 * @param dotted when true the annotations are separated by dots
	 */
	public void setDotSeparated(boolean dotted) {
		if (dotted != bVisDotted) {
			bVisDotted = dotted;
			extraLength = bVisDotted ? 4 : 0;
			
			setTier(getTier());
		}
	}
	
	/**
	 * Sets whether the crosshair should be centered vertically in  
	 * the view. When this value is <code>false</code> the crosshair 
	 * will most of the time be drawn at he bottom of the view.
 	 *   
	 * @param centered the center vertically value
	 */
	public void setCenteredVertically(boolean centered) {
		centerVertically = centered;
		
		if (centerMI.isSelected() != centerVertically) {
			centerMI.setSelected(centerVertically);
		}
		
		scrollIfNeeded();
		repaint();
	}
	
	/**
	 * Returns whether the crosshair is vertically centered .
	 * 
	 * @return the center vertically value
	 */
	public boolean isCenteredVertically() {
		return centerVertically;
	}

    /**
     * Builds an array with all begintimes and endtimes from a tag Used for
     * searching (quickly) a particular tag
     */
    private void buildArrayAndText() {
        tierText = "";
        StringBuilder builder = new StringBuilder(256);
        
        int annotations_size = annotations.size();
        arrTagTimes = new long[2 * annotations_size];
        arrTagPositions = new int[2 * annotations_size];

        int arrIndexTimes = 0;
        int arrIndexPositions = 0;
        // add highlights for CVEntry values with colours
        Color c = null;
    	ControlledVocabulary cv = null;
        HashMap<Integer, Color> colors = null;
        if (tier != null) {
	        String cvName = tier.getLinguisticType().getControlledVocabularyName();
	        if (cvName != null) {
	        	cv = tier.getTranscription().getControlledVocabulary(cvName);
	        	if (cv != null) {
	        		colors = new HashMap<Integer, Color>();
	        	}
	        }
        }

        try {
            for (Annotation ann : annotations) {
                String strTagValue = ann.getValue();
                c = null;
                if (cv != null) {
	                String id = ann.getCVEntryId();
	                CVEntry cve = cv.getEntrybyId(id);
	                if (cve != null) {
	                	c = cve.getPrefColor();
	                }
                }
                
                //next line for JDK1.4 only
                //strTagValue = strTagValue.replaceAll("\n", "");
                strTagValue = strTagValue.replace('\n', ' ');

                //building text
                if (bVisDotted) {
                    //tierText += (strTagValue + DOTS);
                    builder.append(strTagValue).append(DOTS);
                    //extraLength = 4;
                } else {
                    //tierText += (strTagValue + " ");
                    builder.append(strTagValue).append(' ');
                    //extraLength = 0;
                }

                begintime = ann.getBeginTimeBoundary();
                endtime = ann.getEndTimeBoundary();

                arrTagTimes[arrIndexTimes++] = begintime;
                arrTagTimes[arrIndexTimes++] = endtime;

                int taglength = (strTagValue).length() +
                    extraLength;

                if ((arrIndexPositions == 0) || (arrIndexPositions == 1)) {
                    arrTagPositions[arrIndexPositions++] = 0;
                    arrTagPositions[arrIndexPositions++] = taglength;
                } else {
                    arrTagPositions[arrIndexPositions] = arrTagPositions[arrIndexPositions -
                        1] + 1; // 1 space
                    arrIndexPositions++;
                    arrTagPositions[arrIndexPositions] = arrTagPositions[arrIndexPositions -
                        1] + taglength;
                    arrIndexPositions++;
                }
                
                if (c != null) {
                	colors.put(arrTagPositions[arrIndexPositions - 2], c);
                	Object hl = highlighter.addHighlight(arrTagPositions[arrIndexPositions - 2], 
                			arrTagPositions[arrIndexPositions - 1] - extraLength, valuePainter);
                	valueHighLightInfos.add(hl);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        valuePainter.setColors(colors);// when null it removes previous colors
        valuePainter.setVisible(colors != null && colors.size() > 0);
        
        tierText = builder.toString();
        //used for testing purposes
//        for (int tel=0; tel < arrTagTimes.length; tel++)
//        {
//            System.out.println("arrTagTimes[" + tel + "]: " + arrTagTimes[tel] + " --- " + "arrTagPositions[" + tel + "]: " + arrTagPositions[tel]);
//        }
    }

    /**
     * Handles mouse actions on the text viewer
     */
    private class TextViewerMouseListener extends MouseAdapter {
        private InlineEditBox inlineEditBox = null;
		private JComponent comp;
        /**
         * Creates a new TextViewerMouseListener instance
         *
         * @param c the parent component
         */
		public TextViewerMouseListener(JComponent c) {
			comp = c;
		}
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        @Override
		public void mousePressed(MouseEvent e) {
            stopPlayer();

            if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
                Point p = e.getPoint();
                SwingUtilities.convertPointToScreen(p, comp);

                int x = e.getPoint().x;
                int y = e.getPoint().y;

                popup.show(comp, x, y);
            }
        }

		/**
		 * Finds the index of the active annotation in the array of tags.
		 * 
		 * @return the index
		 */
        private int getArrayIndex() {
            int index = -1;

            int annotations_size = annotations.size();

            for (int i = 0; i < annotations_size; i++) {
            	Annotation ann = annotations.get(i);

                // 4 dec 2003: there isn't always a selection to rely on
                //exact time always exists in this case
                /*
                if (getSelectionBeginTime() == tag.getBeginTime()) {
                    index = i;

                    break;
                }
                
                // temp: use the active annotation
                else */
                // HS jul 2004 only use the active annotation to find the particular index
                if ((getActiveAnnotation() != null) &&
                        (getActiveAnnotation().getBeginTimeBoundary() == ann.getBeginTimeBoundary())) {
                    index = i;

                    break;
                }
            }

            return index;
        }

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        @Override
		public void mouseReleased(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
				return;
			}
			
            int annotations_size = annotations.size();

            //bring up edit dialog
            if (e.getClickCount() == 2) {
                if (inlineEditBox == null) {
                    inlineEditBox = new InlineEditBox(false);
                    inlineEditBox.setLocale(ElanLocale.getLocale());
                } else {
                	   inlineEditBox.setEnterCommits(enterCommits);
                }

                int index = getArrayIndex();

                if (index >= 0 && index < annotations_size) {
                	Annotation annotation = annotations.get(index);
                    if (e.isShiftDown()) {
                    	// open CV
						inlineEditBox.setAnnotation(annotation, true);
                    } else {
						inlineEditBox.setAnnotation(annotation);
                    }
                    inlineEditBox.setFont(taText.getFont());
                    
                    inlineEditBox.detachEditor();
                }

                return;
            }

            //setting selection color to background because of strange behaviour from JTextArea
            //JTextArea's own selection system gets in the way with our selection.
            //For example, a fast three-click will select a complete line.
            //A workaround: set the selection color to the background color.
            //So the JTextArea still shows its own selection , you just don't see it.
            //One problem left: making a selection in the JTextArea by dragging a mouse.
            //You can't see your selection while you're making it because of the color.
            //To avoid this the selection color of the JTextArea is set to our
            //selection color in the mouseDragged method of TextViewerMouseMotionListener.
            //And here is the place to set it back again.
            //taText.setSelectionColor(taText.getBackground());
			taText.setSelectionColor(transparent);

            if (!taText.getText().equals("")) {
                int selectionStartPosition = taText.getSelectionStart();
                int selectionEndPosition = taText.getSelectionEnd();

                int indexSelectionStart = 0;
                int indexSelectionEnd = 0;
                int indexj = 0;

                //index from selection begin
                int index = Math.abs(Arrays.binarySearch(arrTagPositions,
                            selectionStartPosition));

                for (int j = -2; j <= 2; j++) {
                    indexj = index + j;

                    if ((indexj >= 0) && (indexj < arrTagPositions.length)) {
                        if (arrTagPositions[indexj] <= selectionStartPosition) {
                            indexSelectionStart = indexj;
                        }
                    }
                }

                //if clicked
                if ((selectionStartPosition == selectionEndPosition) &&
                        ((indexSelectionStart + 1) < arrTagTimes.length)) {
                    //don't change order of setting things!
                    // HS 4 dec 2003: changed in order to use application wide implementation of
                    // setActiveAnnotation (i.e. don't set the selection when a RefAnnotation is made active)
                    //setSelection(arrTagTimes[indexSelectionStart], arrTagTimes[indexSelectionStart + 1]);
                    index = (int) Math.ceil(indexSelectionStart / 2);

                    if (index < annotations_size) {
						Annotation ann = annotations.get(index);
                        setActiveAnnotation(ann);
                    } else {
                        setMediaTime(arrTagTimes[indexSelectionStart]);
                    }

                    return;
                }

                //if dragged
                index = Math.abs(Arrays.binarySearch(arrTagPositions,
                            selectionEndPosition));

                for (int j = 2; j >= -2; j--) {
                    indexj = index + j;

                    if ((indexj >= 0) && (indexj < arrTagTimes.length)) {
                        if (arrTagPositions[indexj] >= selectionEndPosition) {
                            indexSelectionEnd = indexj;
                        }
                    }
                }

                //if dragged outside right border, then determine the new indexSelectionEnd
                int indexNewJ;

                if ((indexj + 2) < arrTagTimes.length) {
                    indexNewJ = indexj + 2;
                } else if ((indexj + 1) < arrTagTimes.length) {
                    indexNewJ = indexj + 1;
                } else {
                    indexNewJ = indexj;
                }

                if (indexSelectionEnd == 0) {
                    indexSelectionEnd = indexNewJ;
                }

                if ((indexSelectionStart < indexSelectionEnd) &&
                        (indexSelectionEnd < arrTagTimes.length)) {
                    setSelection(arrTagTimes[indexSelectionStart],
                        arrTagTimes[indexSelectionEnd]);
                    setMediaTime(arrTagTimes[indexSelectionStart]);
                }
            }
        }
    }
     //end of TextViewerMouseListener

    /**
     * Handles mouse motion actions on the text viewer
     */
    private class TextViewerMouseMotionListener extends MouseMotionAdapter {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        @Override
		public void mouseDragged(MouseEvent e) {
            //see comment in mouseReleased from TextViewerMouseListener
            taText.setSelectionColor(Constants.SELECTIONCOLOR);
        }
    }
     //end of TextViewerMouseMotionListener

    /**
     * DOCUMENT ME!
     * $Id: TextViewer.java 46043 2017-04-11 15:42:35Z hasloe $
     * @author $Author$
     * @version $Revision$
     */
    private class TextViewerComponentListener extends ComponentAdapter {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        @Override
		public void componentResized(ComponentEvent e) {
            doUpdate();
        }
    }

}
