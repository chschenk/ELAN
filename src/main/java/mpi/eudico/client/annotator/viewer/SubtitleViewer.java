package mpi.eudico.client.annotator.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.InlineEditBoxListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.InlineEditBox;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;

/**
 * Viewer for a subtitle
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public class SubtitleViewer extends AbstractViewer implements SingleTierViewer,
    ACMEditListener, ActionListener, InlineEditBoxListener {
    private JMenu fontMenu;
    private ButtonGroup fontSizeBG;
    private int fontSize;
    private JPopupMenu popup;
    private Dimension minDimension = new Dimension(400, 50);
    private JTextArea taSubtitle;
    private JScrollPane jspSubtitle;
    private TierImpl tier;
    private List<? extends Annotation> annotations = new ArrayList<Annotation>();
    private long begintime = 0;
    private long endtime = 0;
    private long[] arrTags;
    private Highlighter highlighter;
    private Highlighter.HighlightPainter selectionPainter;
    private InlineEditBox inlineEditBox;
    private int viewerIndex = 0;
    
    /**
     * Constructor
     *
     */
    public SubtitleViewer() {

        try {
            setLayout(new BorderLayout());

            taSubtitle = new JTextArea() { //don't eat up any key events
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

            taSubtitle.setFont(Constants.DEFAULTFONT);
            taSubtitle.setLineWrap(true);
            taSubtitle.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            taSubtitle.setForeground(Constants.DEFAULTFOREGROUNDCOLOR);
            taSubtitle.setEditable(false);
            taSubtitle.addMouseListener(new SubtitleViewerMouseListener(this));

            highlighter = taSubtitle.getHighlighter();
            selectionPainter = new DefaultHighlighter.DefaultHighlightPainter(Constants.SELECTIONCOLOR);

            jspSubtitle = new JScrollPane(taSubtitle);
            add(jspSubtitle, BorderLayout.CENTER);

            fontSize = 12;
  
            setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sets minimum for the subtitle viewer See also getPreferredSize
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Dimension getMinimumSize() {
        return minDimension;
    }

    /**
     * Sets minimum for the subtitle viewer See also getMinimumSize
     *
     * @return DOCUMENT ME!
     */
    @Override
	public Dimension getPreferredSize() {
        return minDimension;
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
    }

    /**
     * AR heeft dit hier neergezet Er moet nog gepraat worden over wat hier te
     * doen valt
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void ACMEdited(ACMEditEvent e) {
        switch (e.getOperation()) {
        case ACMEditEvent.ADD_ANNOTATION_HERE:
        case ACMEditEvent.ADD_ANNOTATION_BEFORE:
        case ACMEditEvent.ADD_ANNOTATION_AFTER:
        case ACMEditEvent.CHANGE_ANNOTATIONS:
        case ACMEditEvent.REMOVE_ANNOTATION:
        case ACMEditEvent.CHANGE_ANNOTATION_TIME:
        case ACMEditEvent.CHANGE_ANNOTATION_VALUE: {
            setTier(getTier());
            doUpdate();
        }
        }
    }

    /**
     * Applies (stored) preferences.
     */
	@Override
	public void preferencesChanged() {
		Integer fontSi = Preferences.getInt("SubTitleViewer.FontSize-" + 
				viewerIndex, getViewerManager().getTranscription());
		if (fontSi != null) {
			setFontSize(fontSi.intValue());
		}
		if (tier != null) {
			Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", getViewerManager().getTranscription());
			if (fonts != null) {
				Font tf = fonts.get(tier.getName());
				if (tf != null) {
					taSubtitle.setFont(new Font(tf.getName(), Font.PLAIN, fontSize));
				} else {
					taSubtitle.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
				}
			}
		} else {
			taSubtitle.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
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
                repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            taSubtitle.setFont(taSubtitle.getFont().deriveFont((float) fontSize));
            setPreference("SubTitleViewer.FontSize-" + viewerIndex, Integer.valueOf(fontSize), 
            		getViewerManager().getTranscription());
        }
    }

    /**
     * AR notification that some media related event happened method from
     * ControllerListener not implemented in AbstractViewer
     *
     * @param event DOCUMENT ME!
     */
    @Override
	public void controllerUpdate(ControllerEvent event) {
        doUpdate();
    }

    /**
     * Update the complete subtitle viewer Determines whether current time is
     * in a selection Sets the correct value in the subtitle viewer
     */
    public void doUpdate() {
        String strTagValue = "";
        boolean bFound = false;
        long mediatime = getMediaTime();
        int annotations_size = annotations.size();

        if (arrTags == null) {
            return;
        }

        //use fast search to determine approximate index from array
        int index = Math.abs(Arrays.binarySearch(arrTags, mediatime));

        //make it an index which can be used with the vector
        index = index / 2;

        //determine first and last index in vector to use in loop
        int beginindex = index - 2;
        int endindex = index + 2;

        if (beginindex < 0) {
            beginindex = 0;
        }

        if (endindex > (annotations_size - 1)) {
            endindex = annotations_size - 1;
        }
        
		long selectionBeginTime = getSelectionBeginTime();
		long selectionEndTime = getSelectionEndTime();
		
        //now there is a maximum of only 5 indexes to loop through
        for (int i = beginindex; i <= endindex; i++) {
        	Annotation ann = annotations.get(i);
            begintime = ann.getBeginTimeBoundary();
            endtime = ann.getEndTimeBoundary();
            // special case: if a selection (active annotation) is played, 
            // at the end mediatime == end time of active annotation. Include endtime
            // in comparison in such case
            if ( (ann == getActiveAnnotation() && endtime == mediatime) ||
            	(begintime == selectionBeginTime && endtime == selectionEndTime && endtime == mediatime) ) {
				bFound = true;
				strTagValue = ann.getValue();
				strTagValue = strTagValue.replace('\n', ' ');

				break;
            }

            if ((mediatime >= begintime) && (mediatime < endtime)) {
                bFound = true;
                strTagValue = ann.getValue();
				/*
				if (ann == getActiveAnnotation()) {
					taSubtitle.setBorder(BorderFactory.createLineBorder(
						Constants.ACTIVEANNOTATIONCOLOR));
				} else {
					taSubtitle.setBorder(null);
				}
				*/
                //next line for JDK1.4 only
                //strTagValue = strTagValue.replaceAll("\n", "");
                strTagValue = strTagValue.replace('\n', ' ');

                break;
            }
        }

        //set the appropriate text
        if (bFound) {
            try {
                taSubtitle.setText(" " + strTagValue); //+ '\u0332')
            } catch (Exception ex) {
                taSubtitle.setText("");
            }
        } else {
            taSubtitle.setText("");
			//taSubtitle.setBorder(null);
        }

        //handle colors
        if ((tier != null) && (selectionBeginTime != selectionEndTime) &&
                (mediatime >= begintime) && ((mediatime < endtime) || 
                (mediatime == endtime && selectionBeginTime == begintime)) &&
                (((selectionBeginTime >= begintime) &&
                (selectionBeginTime < endtime)) ||
                ((selectionEndTime >= begintime) &&
                (selectionEndTime < endtime)))) {
            try {
                highlighter.addHighlight(1, taSubtitle.getText().length(),
                    selectionPainter);
            } catch (Exception ex) {
                //ex.printStackTrace();
                ClientLogger.LOG.warning("Cannot add a highlight: " + ex.getMessage());
            }
        } else {
            taSubtitle.getHighlighter().removeAllHighlights();
        }

        repaint();
    }

    /**
     * Sets the tier which is shown in the subtitle viewer
     *
     * @param tier The tier which should become visible
     */
    @Override
	public void setTier(Tier tier) {
        // added by AR
		putOriginalComponentBack();
        if (tier == null) {
            this.tier = null;
            annotations = new ArrayList<Annotation>();
            doUpdate();
            setPreference("SubTitleViewer.TierName-" + viewerIndex, tier, 
            		getViewerManager().getTranscription());
            taSubtitle.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
        } else {
            this.tier = (TierImpl) tier;

            try {
                annotations = this.tier.getAnnotations();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setPreference("SubTitleViewer.TierName-" + viewerIndex, tier.getName(), 
            		getViewerManager().getTranscription());           
            
//    			Object fonts = Preferences.get("TierFonts", getViewerManager().getTranscription());
//    			if (fonts instanceof HashMap) {
//    				Font tf = (Font) ((HashMap) fonts).get(tier.getName());
//    				if (tf != null) {
//    					taSubtitle.setFont(new Font(tf.getName(), Font.PLAIN, fontSize));
//    				} else {
//    					taSubtitle.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
//    				}
//    			}
            preferencesChanged();
    		
        }

        buildArray();
        doUpdate();
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
						taSubtitle.setFont(taSubtitle.getFont().deriveFont((float) fontSize));
						break;
					}
				} catch (NumberFormatException nfe) {
					//// do nothing
				}
			}
		} else {		
			createPopup();
			taSubtitle.setFont(taSubtitle.getFont().deriveFont((float) fontSize));
		}
	}


	/**
	 * The index of this subtitle viewer in the list of subtitle viewers, 
	 * first index is 1.
	 * 
	 * @return the viewerIndex
	 */
	public int getViewerIndex() {
		return viewerIndex;
	}

	/**
	 * Sets the index of this subtitle viewer in the list of subtitle viewers.
	 * 
	 * @param viewerIndex the viewerIndex to set
	 */
	public void setViewerIndex(int viewerIndex) {
		this.viewerIndex = viewerIndex;
	}
	
    /**
     * Builds an array with all begintimes and endtimes from a tag Used for
     * searching (quickly) a particular tag
     */
    private void buildArray() {
        int annotations_size = annotations.size();
        arrTags = new long[2 * annotations_size];

        int arrIndex = 0;
        for (int i = 0; i < annotations_size; i++) {
        	Annotation ann = annotations.get(i);

            begintime = ann.getBeginTimeBoundary();
            endtime = ann.getEndTimeBoundary();

            arrTags[arrIndex++] = begintime;
            arrTags[arrIndex++] = endtime;
        }
    }

    //when inline edit box disappears, it calls this method
    public void putOriginalComponentBack() {
        inlineEditBox = null;
        removeAll();
        add(jspSubtitle, BorderLayout.CENTER);
        validate();
        repaint();
    }
    
    @Override
	public void isClosing(){
        if(inlineEditBox != null && inlineEditBox.isVisible()){    		
    		Boolean boolPref = Preferences.getBool("InlineEdit.DeselectCommits", null);
			if (boolPref != null && !boolPref) {
				inlineEditBox.cancelEdit();
			} else {   				
				inlineEditBox.commitEdit();
			}
    	}
        validate();
        repaint();
    }

    /**
     * Hhandles mouse actions on the subtitle viewer
     */
    private class SubtitleViewerMouseListener extends MouseAdapter {
        private SubtitleViewer subtitleViewer;

        /**
         * Creates a new SubtitleViewerMouseListener instance
         *
         * @param sv DOCUMENT ME!
         */
        SubtitleViewerMouseListener(SubtitleViewer sv) {
            subtitleViewer = sv;
        }
        
        /**
         * The mouse pressed event handler.
         * @param e the mouse event
         */
		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
				Point p = e.getPoint();
				SwingUtilities.convertPointToScreen(p, subtitleViewer);

				int x = e.getPoint().x;
				int y = e.getPoint().y;

				popup.show(subtitleViewer, x, y);

				return;
			}
		}

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        @Override
		public void mouseClicked(MouseEvent e) {
            //bring up edit dialog
            if (e.getClickCount() == 2) {
                inlineEditBox = new InlineEditBox(true);

                Annotation annotation = getAnnotation();

                if (annotation != null) {
					if (e.isShiftDown()) {
						// open CV
						inlineEditBox.setAnnotation(annotation, true);
					} else {
						inlineEditBox.setAnnotation(annotation);
					}
					inlineEditBox.setFont(taSubtitle.getFont());
					inlineEditBox.configureEditor(JPanel.class, null, getSize());
					inlineEditBox.addInlineEditBoxListener(subtitleViewer);
                    removeAll();
                    add(inlineEditBox, BorderLayout.CENTER);
                    validate();
                    //repaint();
                   	inlineEditBox.startEdit();
                }

                return;
            }

            if (!taSubtitle.getText().equals("")) {
                stopPlayer();
                setActiveAnnotation(getAnnotation());
            }
        }

        private Annotation getAnnotation() {
            int annotations_size = annotations.size();

            for (int i = 0; i < annotations_size; i++) {
                Annotation annotation = annotations.get(i);

                //exact time always exists in this case
                //if (getSelectionBeginTime() == tag.getBeginTime())
                // HS 4 dec 03: the subtitle viewer always seems to show the tag
                // at the media time, if any
                if ((getMediaTime() >= annotation.getBeginTimeBoundary()) &&
                        (getMediaTime() < annotation.getEndTimeBoundary())) {

                    return annotation;
                }
            }

            return null;
        }
    }
     //end of SubtitleViewerMouseListener
	
	@Override
	public void editingCommitted() {
		putOriginalComponentBack();		
	}
	
	@Override
	public void editingCancelled() {
		putOriginalComponentBack();			
	}

	public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksList) {
		if(inlineEditBox != null){
			inlineEditBox.setKeyStrokesNotToBeConsumed(ksList);
		}		
	}	
	
//	public void editingInterrupted() {
//		isClosing();		
//	}
}
