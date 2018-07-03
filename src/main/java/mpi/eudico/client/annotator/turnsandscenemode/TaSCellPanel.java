package mpi.eudico.client.annotator.turnsandscenemode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.viewer.StyledHighlightPainter;
import mpi.eudico.util.TimeFormatter;

/**
 * A panel that serves as the basis for both a cell renderer and a cell editor.
 * Attempts are made to keep this a view object as much as possible.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TaSCellPanel extends JPanel {
	private JLabel timeLabel;
	private JLabel speakerLabel;
	private JLabel rowNumberLabel;
	private JTextArea textArea;
	private DecorationPanel indicatorPanel;
	private Border activeBorder = new LineBorder(Constants.ACTIVEANNOTATIONCOLOR, 1);
	private Border activeGapBorder = new LineBorder(new Color(128, 128, 255), 1);
	/* added assuming the panel would be in a JList. In a JTable this can be removed (there is a "showHorizontalLines" method) */
	private JPanel linePanel;
	private Color DEF_PANEL_BG;
	private Color DEF_TEXT_BG;
	public final Color NO_ANN_BG = new Color(230,230,250);
	private final String EMPTY_STR = "";
	// highlighting of special character combinations
	private final int NUMBER_OF_HIGHLIGHTERS = 4;
	private Highlighter tasHighlighter;
	private TextAreaDocListener docListener;
	private List<StyledHighlightPainter> transPainterList;
	private List<Object> transHighlightInfoList;

	private List<StyledHighlightPainter> speakPainterList;
	private List<Object> speakHighlightInfoList;
	
	private boolean timeLabelVisible;
	// speaker label visibility false for now
	private boolean speakerLabelVisible;
	
	private TaSAnno curAnnoSegment;
	
	private long currentMediaTime = -1L;
	
	private List<KeyStroke> keyStrokesNotToConsume;
	
	private Font baseFont = null;
	private Font mainTierFont = null;
	
	/**
	 * Constructor, creates and initializes the ui components.
	 */
	public TaSCellPanel() {
		super();
		initComponents();
	}
	
	/**
	 * Sets the annotation wrapper object to render.
	 * 
	 * @param slAnno the annotation wrapper object
	 */
	public void setTaSAnnotation(TaSAnno slAnno) {
		curAnnoSegment = slAnno;
		
		if (curAnnoSegment != null) {
			textArea.setText(curAnnoSegment.getText());
			if (slAnno.getAnnotation() == null) {
				textArea.setBackground(Constants.EVEN_ROW_BG);// NO_ANN_BG
				rowNumberLabel.setText(EMPTY_STR);
			} else {
				textArea.setBackground(DEF_TEXT_BG);	
				int index = slAnno.getAnnotation().getTier().getAnnotations().indexOf(slAnno.getAnnotation());
				if (index >= 0) {
					rowNumberLabel.setText(String.valueOf(index + 1));
				} else {
					rowNumberLabel.setText(EMPTY_STR);
				}
			}
			long et = curAnnoSegment.getEndTime();
			String etLabel = null;
			if (et == Long.MAX_VALUE) {
				etLabel = "?";
			} else {
				etLabel = TimeFormatter.toString(et);
			}
			timeLabel.setText(TimeFormatter.toString(curAnnoSegment.getBeginTime()) + " - " + 
					etLabel);
			if (curAnnoSegment.getParticipant() != null) {
				if (curAnnoSegment.getParticipant().length() > 3) {
					speakerLabel.setText(curAnnoSegment.getParticipant().substring(0, 3));
				} else {
					speakerLabel.setText(curAnnoSegment.getParticipant());
				}
			} else {
				speakerLabel.setText("");
			}
		} else {
			textArea.setText(EMPTY_STR);
			timeLabel.setText(EMPTY_STR);
			speakerLabel.setText(EMPTY_STR);
			rowNumberLabel.setText(EMPTY_STR);
			textArea.setBackground(DEF_TEXT_BG);
		}
	}
	
	private void initComponents() {
		setLayout(new GridBagLayout());
		timeLabelVisible = true;
		speakerLabelVisible = false;
		
		timeLabel = new JLabel("T");
		speakerLabel = new JLabel("S");
		rowNumberLabel = new JLabel("0");
		
		//indicatorPanel = new JPanel();// => custom panel to paint selection, active annotation and media time colors?
		indicatorPanel = new DecorationPanel();
		indicatorPanel.setPreferredSize(new Dimension(14, 20));
		DEF_PANEL_BG = indicatorPanel.getBackground();
		linePanel = new JPanel(null);
		linePanel.setPreferredSize(new Dimension(1, 1));
		
		//textArea = new JTextArea();
		textArea = new TaSTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(true);
		textArea.setMargin(new Insets(4, 6, 4, 4));
		DEF_TEXT_BG = textArea.getBackground();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridheight = 2;
		gbc.weighty = 1.0;
		this.add(indicatorPanel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(6, 3, 2, 4);
		this.add(rowNumberLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(6, 3, 2, 4);
		this.add(speakerLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(2, 3, 2, 2);
		this.add(textArea, gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(2, 3, 2, 2);
		this.add(timeLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridheight = 1;
		gbc.gridwidth = 4;
		gbc.weightx = 1.0;
		this.add(linePanel, gbc);
		updateLayout();
		
		initHighlighters();
	}
	
	private void initHighlighters() {
		docListener = new TextAreaDocListener();
		textArea.getDocument().addDocumentListener(docListener);
		tasHighlighter = textArea.getHighlighter();
		// translation highlighter
		transPainterList = new ArrayList<StyledHighlightPainter>(NUMBER_OF_HIGHLIGHTERS);
		transHighlightInfoList = new ArrayList<Object>(NUMBER_OF_HIGHLIGHTERS);
		
		for (int i = 0; i < NUMBER_OF_HIGHLIGHTERS; i++) {
			StyledHighlightPainter shp = new StyledHighlightPainter(
					Constants.SIGNALSTEREOBLENDEDCOLOR1, 0, StyledHighlightPainter.FILLED);
			shp.setVisible(false);
			try {
				transHighlightInfoList.add(tasHighlighter.addHighlight(0, 0, shp));
				transPainterList.add(shp);
			} catch (BadLocationException ble){
				/* ignore */
			}
		}
		// speaker highlighter
		speakPainterList = new ArrayList<StyledHighlightPainter>(NUMBER_OF_HIGHLIGHTERS);
		speakHighlightInfoList = new ArrayList<Object>(NUMBER_OF_HIGHLIGHTERS);
		
		for (int i = 0; i < NUMBER_OF_HIGHLIGHTERS; i++) {
			StyledHighlightPainter shp = new StyledHighlightPainter(
					Constants.SHAREDCOLOR1, 0, StyledHighlightPainter.FILLED);
			shp.setVisible(false);
			try {
				speakHighlightInfoList.add(tasHighlighter.addHighlight(0, 0, shp));
				speakPainterList.add(shp);
			} catch (BadLocationException ble){
				/* ignore */
			}
		}
	}
	
	private void updateLayout() {
		speakerLabel.setVisible(speakerLabelVisible);
		timeLabel.setVisible(timeLabelVisible);
	}
	
	public void startEditing() {
		textArea.requestFocus();
		textArea.setCaretPosition(0);
	}
	
	public void stopEditing() {
		textArea.transferFocusUpCycle();
	}
	
	public void setSelected(boolean selected) {
		if (selected) {
			indicatorPanel.setBackground(Constants.SELECTIONCOLOR);
		} else {
			indicatorPanel.setBackground(DEF_PANEL_BG);
		}
	}
	
	/**
	 * This sets the overall font for the panel, including the font for the annotations
	 * unless a preferred font for the tier is set.
	 * This method is also used when changing the size of the fonts.
	 * @param the font to use rendering (most) texts and labels
	 */
	@Override
	public void setFont(Font font) {
		if (font != null) {
			baseFont = font;
			if (textArea != null) {
				if (mainTierFont == null) {
					textArea.setFont(font);
				} else {
					textArea.setFont(mainTierFont.deriveFont((float) font.getSize())); 
				}
			}
			if (speakerLabel != null) {
				speakerLabel.setFont(font);
			}
			if (timeLabel != null) {
				int baseSize = font.getSize();
				float timeSize = 10 + 2 * (baseSize / 10f);
				timeLabel.setFont(font.deriveFont(timeSize));
			}	
			if (rowNumberLabel != null && timeLabel != null) {
				rowNumberLabel.setFont(timeLabel.getFont());
			}
		}
	}
	
	/**
	 * Sets the font to use for the annotations in the text area. This is the 
	 * user-defined preferred font for the (main) tier.
	 * 
	 * @param f the font to use for the annotations. If null the default font will be used
	 */
	public void setMainTierFont(Font f) {
		this.mainTierFont = f;
		
		if (textArea != null) {
			int baseSize = 16; 
			if (baseFont != null) {
				baseSize = baseFont.getSize();
			}
			if (mainTierFont != null) {
				if (mainTierFont.getSize() == baseSize) {
					textArea.setFont(mainTierFont);
				} else {
					textArea.setFont(mainTierFont.deriveFont((float) baseSize));
				}
			} else {
				textArea.setFont(baseFont);
			}
		}
	}

	public boolean isTimeLabelVisible() {
		return timeLabelVisible;
	}

	public void setTimeLabelVisible(boolean timeLabelVisible) {
		if (this.timeLabelVisible != timeLabelVisible) {
			this.timeLabelVisible = timeLabelVisible;
			updateLayout();
		}
	}

	public boolean isSpeakerLabelVisible() {
		return speakerLabelVisible;
	}

	public void setSpeakerLabelVisible(boolean speakerLabelVisible) {
		if (this.speakerLabelVisible != speakerLabelVisible) {
			this.speakerLabelVisible = speakerLabelVisible;
			updateLayout();
		}
	}

	/**
	 * Gives access to the text area so that actions and listeners can be added etc.
	 * @return the text area
	 */
	public JTextArea getTextArea() {
		return textArea;
	}

	// add method to set decoration flags for time selection (light blue), cross hair (red) and 
	// active annotation (dark blue)
	public void setDecorations(boolean inTimeSelection, boolean containsCrosshair, 
			boolean isActiveAnnotation, boolean isActiveGap) {
		indicatorPanel.timeSelected = inTimeSelection;
		indicatorPanel.crosshairInSegment = containsCrosshair;
		//indicatorPanel.activeAnnotation = isActiveAnnotation;
		if (isActiveAnnotation) {
			this.setBorder(activeBorder);
		} else {
			if (isActiveGap) {
				this.setBorder(activeGapBorder);
			} else {
				this.setBorder(null);
			}
		}
	}
	
	public void updatMediaTime(long mediaTime) {
		currentMediaTime = mediaTime;
		indicatorPanel.repaint();
	}
	
	/**
	 * Sets the list of key strokes the text editor should not consume so that the 
	 * associated action can be performed even while the text area has the focus.
	 * 
	 * @param keyStrokesNotToBeConsumed a list of key strokes
	 */
	public void setKeyStrokesNotToBeConsumed(List<KeyStroke> keyStrokesNotToBeConsumed) {
		keyStrokesNotToConsume = keyStrokesNotToBeConsumed;
	}
	
	@SuppressWarnings("serial")
	private class DecorationPanel extends JPanel {
		boolean timeSelected = true;
		boolean crosshairInSegment = true;
		//boolean activeAnnotation = true;
		
		int chHeight = 10;
		/**
		 * 
		 */
		public DecorationPanel() {
			super();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2d = (Graphics2D) g;
			int w = getWidth();
			int h = getHeight();
			if (!timeSelected) {
				g2d.setColor(DEF_PANEL_BG);
			} else {
				g2d.setColor(Constants.SELECTIONCOLOR);
			}
			g2d.fillRect(0, 0, w, h);
			
			if (crosshairInSegment) {
				if (currentMediaTime < 0) {
					g2d.setColor(Constants.CROSSHAIRCOLOR);
					g2d.fillRect(0, (h - chHeight) / 2, w, chHeight);
					g2d.drawLine(0, 0, w - 1, 0);
					g2d.drawLine(0, h - 1, w - 1, h - 1);
				} else {
					if (currentMediaTime >= curAnnoSegment.getBeginTime() && currentMediaTime <= curAnnoSegment.getEndTime()) {
						// proportionally move the crosshair cursor
						float relPos = (currentMediaTime - curAnnoSegment.getBeginTime()) / 
								(float)(curAnnoSegment.getEndTime() - curAnnoSegment.getBeginTime());
						int yPos = (int) (relPos * h);
						yPos = (yPos > h - chHeight ? h - chHeight : yPos);//limit yPos to max h - chHeight?
						g2d.setColor(Constants.CROSSHAIRCOLOR);
						g2d.fillRect(0, yPos , w, chHeight);
						g2d.drawLine(0, 0, w - 1, 0);
						g2d.drawLine(0, h - 1, w - 1, h - 1);
					}
				}
			}
			
//			if (activeAnnotation) {
//				g2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);
//				g2d.drawRect(0, 0, w - 1, h - 1);
//			}
		}
		
	}
	
	/**
	 * Fix for e.g. Shift+Space as the key stroke for play selection and similar combinations.
	 * 
	 * @version Nov 2015 added some additional code to change the behavior of "cut": commit the change after cutting text 
	 * but maybe without automatically activating another row in the table? 
	 * @version March 2016 Shift+Enter now adds a new line to the text area
	 * the list of key strokes not to consume is not used (yet)
	 */
	private class TaSTextArea extends JTextArea {
		int lastKeyCode;
		@Override
		protected void processKeyEvent(KeyEvent e) {
//			if (keyStrokesNotToConsume != null && keyStrokesNotToConsume.contains(KeyStroke.getKeyStrokeForEvent(e))) {
//				lastKeyCode = e.getKeyCode();
//				return;
//			}
			// maybe a general check for registered KeyStroke - Action combinations could be performed, whereupon that action
			// should be performed and the event consumed (while testing for the special case of VK_SPACE ?
			
			// combination with Ctrl either work anyway or have a more important role in text editing than
			// in media navigation? Combinations with arrow left/right also have a meaning in text editing
			int mask = e.getModifiersEx() & (InputEvent.SHIFT_DOWN_MASK | /*InputEvent.CTRL_DOWN_MASK |*/ InputEvent.ALT_DOWN_MASK);

			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				
				if (mask != 0) {
					if (e.getID() == KeyEvent.KEY_PRESSED) {
						// check if there is a registered KeyStroke - Action combination
						Object key = null;
						if ( (key = getInputMap().get(KeyStroke.getKeyStrokeForEvent(e))) != null) {
							Action act = getActionMap().get(key);
							if (act != null) {
								act.actionPerformed(null);
							}
						}
					}
					
					e.consume();
				}
			} else if (e.getKeyCode() == KeyEvent.VK_UNDEFINED) {
				// workaround to prevent a white space to be added to the document if space was combined
				// with shift or alt
				if (mask != 0 && lastKeyCode == KeyEvent.VK_SPACE && e.getID() == KeyEvent.KEY_TYPED) {
					e.consume();
				}
			} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				if (e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK) {// only shift down
					if (e.getID() == KeyEvent.KEY_PRESSED) {
						// add a new line character when the Shift+Enter combination was used
						int pos = this.getCaretPosition();
						try {
							this.insert("\n", pos);
						} catch (IllegalArgumentException iae) {
							this.append("\n");
						}
					}
					e.consume();
				}
			}
			
			lastKeyCode = e.getKeyCode();
			super.processKeyEvent(e);
		}
		
		/**
		 * Commit change after a cut action so that the cell's size can be updated
		 */
		@Override
		public void cut() {
			super.cut();
			// commit change
			Action cutAction = getActionMap().get("PostCut");
			if (cutAction != null) {
				cutAction.actionPerformed(null);
			}
			
		}
		
		/**
		 * Commit changes after a paste action so that the cell's size can be updated
		 */
		@Override
		public void paste() {
			super.paste();
			// commit change
			Action pasteAction = getActionMap().get("PostPaste");
			if (pasteAction != null) {
				pasteAction.actionPerformed(null);
			}
		}		
	}
	
	/**
	 * A listener that checks the contents of the text area in order 
	 * to set or update highlights for special markers.
	 * Supported are the marker to separate a translation (or other secondary annotation)
	 * from the primary transcription and the marker for speaker identification
	 */
	private class TextAreaDocListener implements DocumentListener {
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			checkHighlights(e.getDocument());
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			checkHighlights(e.getDocument());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			checkHighlights(e.getDocument());
		}
		
		private void checkHighlights(Document doc) {
			try {
				String t = doc.getText(0, doc.getLength());
				int[] transInd = TaSSpecialMarkers.getTranslationIndices(t);
				if (transInd == null || transInd.length == 0) {
					for (StyledHighlightPainter shp : transPainterList) {
						shp.setVisible(false);
					}
				} else {
					for (int i = 0; i < transInd.length && i < transPainterList.size(); i++) {
						transPainterList.get(i).setVisible(true);
						tasHighlighter.changeHighlight(transHighlightInfoList.get(i), transInd[i], transInd[i] + 2);
					}
					for (int j = transInd.length; j < transPainterList.size(); j++) {
						transPainterList.get(j).setVisible(false);
					}
				}
			} catch (BadLocationException ble) {
				//ble.printStackTrace();				
			}
			// speaker highlighting
			try {
				String t = doc.getText(0, doc.getLength());
				int[][] speakerInd = TaSSpecialMarkers.getSpeakerIndices(t);
				if (speakerInd == null || speakerInd.length == 0) {
					for (StyledHighlightPainter shp : speakPainterList) {
						shp.setVisible(false);
					}
				} else {
					for (int i = 0; i < speakerInd.length && i < speakPainterList.size(); i++) {
						speakPainterList.get(i).setVisible(true);
						tasHighlighter.changeHighlight(speakHighlightInfoList.get(i), speakerInd[i][0], speakerInd[i][1]);
					}
					for (int j = speakerInd.length; j < speakPainterList.size(); j++) {
						speakPainterList.get(j).setVisible(false);
					}
				}
			} catch (BadLocationException ble) {
				//ble.printStackTrace();				
			}
		}
	}
}
