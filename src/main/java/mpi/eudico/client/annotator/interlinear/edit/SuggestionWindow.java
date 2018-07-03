package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;
import javax.swing.UIManager;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.interlinear.edit.SuggestionComponent.MouseAndMotionListener;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSelectionEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.SuggestionSelectionListener;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTSuggestionViewerModel;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A window class that can show suggestions in a kind of popup near the original annotation.
 * <p>
 * When a suggestion is chosen this is reported via  suggestionSelectionListener.suggestionSelected(...).
 * When the user clicks in a non-suggestion area, this is reported via ... .suggestionIgnored(...).
 * When the window is closed, this is reported via ... .suggestionClosed(...).
 * <p>
 * The usual listener is {@link InterlinearMediator}.
 * <pre>
 * +---------------------------------------------------------------+
 * | +-JPanel (tool bar)-----------------------------------------+ |
 * | |                                                           | |
 * | +-----------------------------------------------------------+ |
 * |                                                               |
 * | +-JScrollPane-----------------------------------------------+ |
 * | |SuggestionSetSelector                                      | |
 * | |                                                           | |
 * | +-----------------------------------------------------------+ |
 * +---------------------------------------------------------------+
 * </pre>
 * The window can also be dragged by clicking in the window.
 * <p>
 * There is some keyboard control for the selector:
 * <ul>
 * <li>The DIGIT keys 1-9 and 0 select suggestion number 1-10.
 * <li>The SHIFTTED DIGIT keys select suggestions 11-20.
 * <li>The SPACE and ENTER keys select the first suggestion.
 * <li>The TAB, BACKSPACE and DELETE keys ignore this suggestion and move to
 *     the next one (equivalent to clicking outside the suggestions).
 *  <li>The ESCAPE and W keys closes the window and stops the suggestions
 *     (equivalent to clicking the small "close" label in the corner).
 * <li>The ARROW keys scroll the scroll area.
 * <li>The PAGE UP and DOWN keys page through the scroll area.
 * </ul>
 * @author Han Sloetjes
 * @author Olaf Seibert
 */
@SuppressWarnings("serial")
public class SuggestionWindow extends JFrame /*JWindow*/ implements MouseListener, 
MouseMotionListener, ComponentListener, KeyListener,
MouseAndMotionListener, ActionListener, ItemListener {
	private static final int MAX_INITIAL_WIDTH = 1538;// not sure what this is based on?
	private static final int MAX_INITIAL_HEIGHT = 768;
	private JScrollPane selectorScrollPane; 
	private SuggestionSetSelector selector;
	private SuggestionSelectionListener suggestionSelectionListener;
	private JToggleButton reverseButton;
	//private JToggleButton incrementalSelectButton;// maybe use this if there are suitable icons
	private JCheckBox incrementalSelectCB;
	private JLabel incrementalLevelLabel;
	private JCheckBox reuseWindowBoundsCB;
	private JPanel toolBarPanel;
	private SpringLayout springLayout;
	private WindowHandler windowListener;
	
    private Point dragStartPoint;
    private Point dragEndPoint;
    private final static int DRAG_WINDOW = 0;
    private int dragCorner = DRAG_WINDOW;
    private Insets windowMargins;
    private final int MARGIN_TO_SCROLLBAR = 5;
    private int scrollBarWidth;
    
	/* by default all suggestion sets are shown, 
	 * in incremental mode fragments can be selected from left to right */
	private boolean incrementalMode = false;
	private int incrementLevel = -1; // when switching to incremental mode the level will be set to 0
	private final int MAX_INCREMENT_LEVEL = Integer.MAX_VALUE;
	private Map<Integer, List<SuggestionComponent>> incrementMap = null;// lists of SuggestionComponents per level	
	private boolean reversedOrderMode = false;
	private Rectangle prefWindowBounds = null;
    // State
	// pref keys
	private final String PREF_INCREMENTAL = "SuggestionWindow.Incremental";
	private final String PREF_REVERSED = "SuggestionWindow.ReversedOrder";
	private final String PREF_REUSE_WINDOW_BOUNDS = "SuggestionWindow.ReuseWindowBounds";
	private final String PREF_WINDOW_BOUNDS = "SuggestionWindow.WindowBounds";
	
	/**
	 * Constructor with a frame as the owner.
	 * 
	 * @param frame the owner
	 * @param selector the suggestion set selector component
	 */
	public SuggestionWindow(Frame frame, SuggestionSetSelector selector) {
		//super(frame);
		super();		
		setIconImage(frame.getIconImage());
		
		this.selector = selector;
		initComponents();
	}

	/**
	 * Constructor with a window as the owner.
	 * 
	 * @param window the owner
	 * @param selector the suggestion set selector component
	 */
	public SuggestionWindow(Window window, SuggestionSetSelector selector) {
		//super(window);
		super();
		
		this.selector = selector;
		initComponents();
	}

	/**
	 * Initializes the ui components, adds the listeners.
	 */
	private void initComponents() {
		// Make JFrame a window without title bar, but hopefully with resizer in the south-east corner.
		//setUndecorated(true);
		//setResizable(true);
		
		selectorScrollPane = new JScrollPane(selector);
		
		getContentPane().setLayout(new BorderLayout(2, 2));
		toolBarPanel = createToolPanel();
		getContentPane().add(toolBarPanel, BorderLayout.PAGE_START);
		getContentPane().add(selectorScrollPane, BorderLayout.CENTER);
//		if (isAlwaysOnTopSupported()) {
//			setAlwaysOnTop(true);
//		}
		windowMargins = new Insets(0, 0, 0, 0);// can be removed now?
		
		Object sbWidth = UIManager.getDefaults().get("ScrollBar.width");
        if (sbWidth instanceof Integer) {
            scrollBarWidth = (Integer) sbWidth;
        }
		selectorScrollPane.addMouseListener(this);
		selectorScrollPane.addMouseMotionListener(this);
		
		getContentPane().addMouseListener(this);
		getContentPane().addMouseMotionListener(this);
		addComponentListener(this);

		setFocusable(true);	// we want to see keys
		setFocusTraversalKeysEnabled(false); // we want to see the TAB key
		selectorScrollPane.requestFocusInWindow();
		addKeyListener(this);
		windowListener = new WindowHandler();
		addWindowListener(windowListener);
		readPrefs();
	}
	
	/**
	 * Creates a toolbar-like panel containing some widgets to customize the 
	 * behavior and the presentation of the suggestion sets.
	 *   
	 * @return a panel with buttons etc.
	 */
	private JPanel createToolPanel() {
		springLayout = new SpringLayout();
		JPanel p = new JPanel(springLayout);
		int gap = 4;
		int w = gap;
		int h = gap;
		Icon ascendIcon = null;
		Icon descendIcon = null;
		try {
			ascendIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
			descendIcon =  new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));
		} catch (Throwable t) {}
		
		reverseButton = new JToggleButton(descendIcon);// use icon + tooltip
		reverseButton.setSelectedIcon(ascendIcon);
		reverseButton.setToolTipText(ElanLocale.getString("InterlinearEditor.Suggestions.ReverseToolTip"));
		reverseButton.addActionListener(this);
		p.add(reverseButton);
		w += (reverseButton.getPreferredSize().width + gap);
		
		incrementalSelectCB = new JCheckBox(ElanLocale.getString("InterlinearEditor.Suggestions.IncrementalSelection"));
		incrementalSelectCB.setToolTipText(ElanLocale.getString("InterlinearEditor.Suggestions.IncrementalSelectionToolTip"));
		incrementalSelectCB.addItemListener(this);
		p.add(incrementalSelectCB);
		w += (incrementalSelectCB.getPreferredSize().width + gap);
		h += Math.max(reverseButton.getPreferredSize().height, incrementalSelectCB.getPreferredSize().height);
		h += gap;
		
		incrementalLevelLabel = new JLabel();
		incrementalLevelLabel.setForeground(Color.BLUE);
		p.add(incrementalLevelLabel);
		
		springLayout.putConstraint(SpringLayout.WEST, reverseButton, gap, 
				SpringLayout.WEST, p);
		springLayout.putConstraint(SpringLayout.VERTICAL_CENTER, reverseButton, 
				0, SpringLayout.VERTICAL_CENTER, p);
		springLayout.putConstraint(SpringLayout.WEST, incrementalSelectCB, gap, 
				SpringLayout.EAST, reverseButton);
		springLayout.putConstraint(SpringLayout.VERTICAL_CENTER, incrementalSelectCB, 
				0, SpringLayout.VERTICAL_CENTER, p);
		springLayout.putConstraint(SpringLayout.WEST, incrementalLevelLabel, 2 * gap, 
				SpringLayout.EAST, incrementalSelectCB);
		springLayout.putConstraint(SpringLayout.VERTICAL_CENTER, incrementalLevelLabel, 
				0, SpringLayout.VERTICAL_CENTER, p);
		
		reuseWindowBoundsCB = new JCheckBox(ElanLocale.getString("InterlinearEditor.Suggestions.RestoreWindowBounds"));
		p.add(reuseWindowBoundsCB);
		
		springLayout.putConstraint(SpringLayout.EAST, reuseWindowBoundsCB, -gap, 
				SpringLayout.EAST, p);
		springLayout.putConstraint(SpringLayout.VERTICAL_CENTER, reuseWindowBoundsCB, 
				0, SpringLayout.VERTICAL_CENTER, p);
		w += (reuseWindowBoundsCB.getPreferredSize().width + 2 * gap);
		
		p.setPreferredSize(new Dimension(w, h));
		return p;
	}

	/**
	 * Removes the listeners from this window.
	 */
	private void removeListeners() {
		getContentPane().removeMouseListener(this);
		getContentPane().removeMouseMotionListener(this);
		selectorScrollPane.removeMouseListener(this);
		selectorScrollPane.removeMouseMotionListener(this);
		selector.removeWorkAroundToolTipBug(this);
		removeKeyListener(this);
		removeWindowListener(windowListener);
		suggestionSelectionListener = null;
	}
	
	/**
	 * Removes listeners and closes the window.
	 */
	private void closeSelectionWindow() {
		storePrefs();
		removeListeners();
		setVisible(false);
		dispose();
	}
	
	/**
	 * Restore some settings.
	 */
	private void readPrefs() {
		Boolean reversedPref = Preferences.getBool(PREF_REVERSED, null);
		if (reversedPref != null && reversedPref.booleanValue()) {
			reversedOrderMode = true;
			reverseButton.setSelected(reversedOrderMode);
		}
		
		Boolean incrementalPref = Preferences.getBool(PREF_INCREMENTAL, null);
		if (incrementalPref != null && incrementalPref.booleanValue()) {
			incrementalMode = true;
			incrementalSelectCB.setSelected(true);
		}
		
		Boolean reuseWinBounds = Preferences.getBool(PREF_REUSE_WINDOW_BOUNDS, null);
		if (reuseWinBounds != null) {
			reuseWindowBoundsCB.setSelected(reuseWinBounds.booleanValue());
			if (reuseWinBounds) {
				prefWindowBounds = Preferences.getRect(PREF_WINDOW_BOUNDS, null);
			}
		}
	}
	
	/**
	 * Store some settings.
	 */
	private void storePrefs() {
		Preferences.set(PREF_REVERSED, Boolean.valueOf(reversedOrderMode), null);
		Preferences.set(PREF_INCREMENTAL, Boolean.valueOf(incrementalMode), null);
		boolean reuseWindowBounds = reuseWindowBoundsCB.isSelected();
		Preferences.set(PREF_REUSE_WINDOW_BOUNDS, Boolean.valueOf(reuseWindowBounds), null);
		if (reuseWindowBounds) {
			Preferences.set(PREF_WINDOW_BOUNDS, this.getBounds(), null);
		}
	}
	
	/**
	 * Sets the listener to be notified of a suggestion selection event.
	 * Currently there can only be one such listener.
	 * 
	 * @param listener the new suggestion selection listener
	 */
	public void setSuggestionSelectionListener(SuggestionSelectionListener listener) {
		suggestionSelectionListener = listener;
	}

	/**
	 * If there is only one suggestion, generate an event to select that one selection.
	 * TODO this shouldn't be done here actually but before this window is created.
	 * But the auto-interlinearization mode currently depends on this window becoming visible
	 * (should be changed). (This solution still doesn't work with Analyze/Interlinearize mode).
	 * 
	 * @see java.awt.Window#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		
		if (suggestionSelectionListener != null) {
			if (selector != null && selector.getModel().getRowCount() == 1) {
				// emulate a key typed VK_1 or mouse clicked event 
				KeyEvent ke = new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 
						0, KeyEvent.VK_1, '1');
				keyPressed(ke);
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if (selector != null) { // always
			Dimension d = selector.getPreferredSize();
			Dimension toolDim = toolBarPanel.getPreferredSize();
			Dimension maxDim = getMaximumSize();
			if (maxDim == null || maxDim.width == Integer.MAX_VALUE || 
					maxDim.height == Integer.MAX_VALUE) {
				// no maximum size set by the parent component, the creator of this window
				if (d.width > MAX_INITIAL_WIDTH) {
					d.width = MAX_INITIAL_WIDTH;
				}
				if (d.height > MAX_INITIAL_HEIGHT) {
					d.height = MAX_INITIAL_HEIGHT;
				}
			} else {// a maximum area has been set
				if (d.height > maxDim.height + windowMargins.top + windowMargins.bottom + scrollBarWidth) {
					int origPrefHeight = d.height;
					d.height = maxDim.height - windowMargins.top - windowMargins.bottom - scrollBarWidth;
					// if the height is (much) more than the available height check if the width can be increased
					// it is difficult to estimate the ideal size (this would require detailed information from the selector).
					// roughly estimate how many units do not fit
					int numMissing = (int) Math.ceil((origPrefHeight - d.height) / (double)selector.getUnitHeight());
					int numInCurrentViewHeight = (int) Math.ceil(d.height / (double)selector.getUnitHeight());
					int numExtraColumns = (int) Math.ceil(numMissing / (double) numInCurrentViewHeight);
					// assume the average width of a unit is twice the height
					int extraWidth = numExtraColumns * 2 * selector.getUnitHeight();
					d.width += extraWidth;
				}
				if (d.width < toolDim.width) {
					// take the preferred width of the toolbar into account
					d.width = toolDim.width;
				}
				if (d.width > maxDim.width + windowMargins.left + windowMargins.right + scrollBarWidth + MARGIN_TO_SCROLLBAR) {
					d.width = maxDim.width - windowMargins.left - windowMargins.right - scrollBarWidth - MARGIN_TO_SCROLLBAR;
					// the height could probably be adjusted (decreased) now
				}
			}
			return new Dimension(d.width  + windowMargins.left + windowMargins.right  + scrollBarWidth + MARGIN_TO_SCROLLBAR,
					             d.height + windowMargins.top  + windowMargins.bottom + scrollBarWidth);
		} else {
			return super.getPreferredSize();
		}
	}
	
	// ######## mouse motion listener for dragging/resizing the window
	
	@Override // MouseMotionListener
	public void mouseDragged(MouseEvent e) {
		dragEndPoint = e.getPoint();
		int dx = dragEndPoint.x - dragStartPoint.x;
		int dy = dragEndPoint.y - dragStartPoint.y;
		Rectangle bounds = getBounds(); // a copy
		
		// Use relative movements, because the coordinates could be in different spaces.
		if (dragCorner == DRAG_WINDOW) {
			bounds.x += dx;
			bounds.y += dy;
			// Don't set dragStartPoint to dragEndPoint, because we have effectively moved
			// the window under the cursor back to dragStartPoint.
			setBounds(bounds);
		}
		
	}

	@Override // MouseMotionListener
	public void mouseMoved(MouseEvent e) {
	}

	// ####### mouse listener
	@Override // MouseListener
	public void mouseClicked(MouseEvent e) {
		final Component source = (JComponent) e.getSource();
		
		if (source == selectorScrollPane) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("SuggestionWindow.mouseClicked: Scrollpane %s",
						String.valueOf(e.getPoint())));
			}
			if (e.isShiftDown() || e.isAltDown()) {
				if (!incrementalMode) {
					increaseChoiceOfSuggestions();
				}
			} else {
				// There isn't really any need to check.
				// If the source is the scroll pane but but not a SuggestionComponent,
				// the user clicked outside of them and selectedIndex will be -1.
				int xp = e.getPoint().x + selectorScrollPane.getHorizontalScrollBar().getValue();
				int xy = e.getPoint().y + selectorScrollPane.getVerticalScrollBar().getValue();
				
				int selectedIndex = selector.getSuggestionIndexAtPoint(xp, xy);
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("Selected index: " + selectedIndex));
				}
				if (!incrementalMode || incrementLevel == MAX_INCREMENT_LEVEL) {
					applySelectedSuggestionAndClose(selectedIndex);
				} else if (selectedIndex > -1) {
					int visIndex = selector.convertModelIndexToVisible(selectedIndex);
					if (visIndex > -1) {
						SuggestionComponent sc = selector.getSuggestionAtVisibleIndex(visIndex);
						int hashValue = sc.getHashOfFrag(incrementLevel);
						incrementLevel++;
						updateIncrementLevel(incrementLevel, hashValue);
					}
				}
//				if (suggestionSelectionListener != null) {
//					SuggestionSelectionEvent event = new SuggestionSelectionEvent(selector.getModel(), selectedIndex);
//					if (selectedIndex > -1) {
//						suggestionSelectionListener.suggestionSelected(event);
//					} else {
//						suggestionSelectionListener.suggestionIgnored(event);
//					}
//				}
//				closeSelectionWindow();
			}
		} else if (source instanceof SuggestionComponent) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("SuggestionWindow.mouseClicked: SuggestionComponent %s",
						String.valueOf(e.getPoint())));
			}
			
			if (e.isShiftDown() || e.isAltDown()) {
				if (!incrementalMode) {
					reduceChoiceOfSuggestions((SuggestionComponent)source);
				}
			} else {
				if (!incrementalMode || incrementLevel == MAX_INCREMENT_LEVEL) {
					applySelectedSuggestionAndClose(((SuggestionComponent)source).getIndex());
				} else {
					int selHash = ((SuggestionComponent)source).getHashOfFrag(incrementLevel);
					incrementLevel++;
					updateIncrementLevel(incrementLevel, selHash);
				}
//				if (suggestionSelectionListener != null) {
//					int selectedIndex = ((SuggestionComponent)source).getIndex();
//					SuggestionSelectionEvent event = new SuggestionSelectionEvent(selector.getModel(), selectedIndex);
//					suggestionSelectionListener.suggestionSelected(event);
//				}
//				closeSelectionWindow();
			}
		} else if (source == getContentPane()) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("SuggestionWindow.mouseClicked: contentPane %s",
						String.valueOf(e.getPoint())));
			}
		} else {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("SuggestionWindow.mouseClicked: other source %s",
						String.valueOf(e)));
			}
		}
	}
	
	/**
	 * Creates an event and notifies the listener which suggestion has been 
	 * selected or that the suggestions are ignored (no selection).
	 *  
	 * @param selectedIndexInModel the index of the suggestion in the model
	 */
	private void applySelectedSuggestionAndClose(int selectedIndexInModel) {
		if (suggestionSelectionListener != null) {
			SuggestionSelectionEvent event = new SuggestionSelectionEvent(selector.getModel(), selectedIndexInModel);
			if (selectedIndexInModel > -1) {
				suggestionSelectionListener.suggestionSelected(event);
			} else {
				suggestionSelectionListener.suggestionIgnored(event);
			}
		}
		closeSelectionWindow();
	}

	/**
	 * The user has made a part-choice.
	 * <p>
	 * Keep all the suggestions that have the same (hash) value for the current
	 * fragment. Discard all others. This is simply done by making them invisible.
	 * 
	 * @param selectedIndex
	 * @param selected
	 */
	private void reduceChoiceOfSuggestions(SuggestionComponent selected) {
		int fragNr = selected.getFragNr();
		int hash = selected.getHashOfFrag(fragNr);
		int nextFragNr = fragNr >= 0 ? fragNr + 1
				                     : fragNr - 1;
		
		Component[] components = selector.getComponents();
		int nVisible = 0;
		
		for (int i = components.length - 1; i >= 0; i--) {
			Component c = components[i];
			if (c.isVisible() && c instanceof SuggestionComponent) {
				SuggestionComponent sc = (SuggestionComponent)c;
				if (hash == sc.getHashOfFrag(fragNr)) {
					sc.setFragNr(nextFragNr);
					nVisible += 1;
				} else {
					sc.setVisible(false);
				}
			}
		}
		
		if (nVisible > 1) {
		} else if (nVisible == 1) {
			applySelectedSuggestionAndClose(selected.getIndex());
		} else { // nVisible == 0
			// This can't happen. At least component #selectedIndex should still be there.
		}
		
		selector.revalidate();
		selectorScrollPane.revalidate();
		selector.revalidate();
		invalidate();
		repaint();
	}

	/**
	 * Increase the choice of suggestions by making them all visible again.
	 */
	private void increaseChoiceOfSuggestions() {
		Component[] components = selector.getComponents();
		
		for (int i = components.length - 1; i >= 0; i--) {
			Component c = components[i];
			if (c instanceof SuggestionComponent) {
				((SuggestionComponent) c).setFragNr(SuggestionComponent.NO_FRAG);
				((SuggestionComponent) c).setIncrementalModeLevel(-1);
				c.setVisible(true);
			}
		}
		selector.doLayout();
	}

	/**
	 * In incremental suggestion selection mode this updates the set of visible
	 * suggestions. 
	 * 
	 * @param level the increment level, the number of 'parts' of the suggestions
	 * that have already been selected
	 * @param selectedFragHash the selected value of the current level
	 */
	private void updateIncrementLevel(int level, int selectedFragHash) {
		if (level == MAX_INCREMENT_LEVEL) {
			return;
		}
		Component[] components = selector.getComponents();
		// store current state
		List<SuggestionComponent> curCompList = new ArrayList<SuggestionComponent>();
		if (level == 0) {// store all
			for (Component c : components) {
				if (c instanceof SuggestionComponent) {
					curCompList.add((SuggestionComponent)c);		
				}
			}
			incrementMap.put(level, curCompList);
		} else {// > 0
			List<SuggestionComponent> prevCompList = incrementMap.get(level - 1);
			
			for (SuggestionComponent sc : prevCompList) {				
				int hash = sc.getHashOfFrag(level - 1);
				if (hash == selectedFragHash) {
						curCompList.add(sc);
				}
			}
			
			incrementMap.put(level, curCompList);
		}
		// check if there is only one left, apply
		if (curCompList.size() == 1) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("Incremental selection, one suggestion left: %s", 
						selector.getModel().getRowData(curCompList.get(0).getIndex())));
			}
			// apply this selection
			applySelectedSuggestionAndClose(curCompList.get(0).getIndex());
		}	

		// update the visible components for next fragment
		Map<Integer, List<SuggestionComponent>> uniqueHashMap = new HashMap<Integer, List<SuggestionComponent>>();		
		List<SuggestionComponent> firstPerUnique = new ArrayList<SuggestionComponent>();
		
		for (SuggestionComponent sc : curCompList) {
			int hash = sc.getHashOfFrag(level);
			if (!uniqueHashMap.containsKey(hash)) {
				List<SuggestionComponent> compPerHash = new ArrayList<SuggestionComponent>();
				uniqueHashMap.put(hash, compPerHash);
				compPerHash.add(sc);
				firstPerUnique.add(sc);
			} else {
				uniqueHashMap.get(hash).add(sc);
			}
		}
		
		// test if more than one suggestions remain
		// if one hash value left -> 
		//     if != 0 increment the level with this hash
		//     if == 0 last level reached, then 
		//         if size of list of suggestions == 1, select this suggestion
		//         if > 1, show all suggestions in the list for the final selection
		if (uniqueHashMap.size() == 1) {
			int onlyHash = uniqueHashMap.keySet().iterator().next();
			List<SuggestionComponent> onlyLeftList = uniqueHashMap.get(onlyHash);
			int numSuggestions = onlyLeftList.size();
			
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("Incremental, last hash value left: %d, number of suggestions: %d, is last level: %b", 
						onlyHash, numSuggestions, (onlyHash == 0)));
			}

			if (onlyHash == 0) {
				if (numSuggestions > 1) {// show the last set of alternatives
					incrementLevel = MAX_INCREMENT_LEVEL;
					firstPerUnique = onlyLeftList;
				} else { // select this one
					applySelectedSuggestionAndClose(onlyLeftList.get(0).getIndex());
					return;
				}
			} else {
				if (numSuggestions == 1) {// select this one
					applySelectedSuggestionAndClose(onlyLeftList.get(0).getIndex());
				} else {// > 1
					incrementLevel++;
					updateIncrementLevel(incrementLevel, onlyHash);
				}
				return;
			}
		}	
		
		for (int i = components.length - 1; i >= 0; i--) {
			Component c = components[i];
			if (!firstPerUnique.contains(c)) {
				if (c instanceof SuggestionComponent) {
					c.setVisible(false);
					((SuggestionComponent) c).setIncrementalModeLevel(-1);
				}
			} else {
				((SuggestionComponent) c).setIncrementalModeLevel(
						incrementLevel != MAX_INCREMENT_LEVEL ? incrementLevel : -1);
				c.setVisible(true);
			}
		}
		// update the label
		if (incrementLevel != MAX_INCREMENT_LEVEL) {
			incrementalLevelLabel.setText(String.format("%s: -%d-",  
					ElanLocale.getString("InterlinearEditor.Suggestions.SelectNextFragment"), (incrementLevel + 1)));
		} else {
			incrementalLevelLabel.setText(ElanLocale.getString("InterlinearEditor.Suggestions.SelectFinal"));
		}
		springLayout.layoutContainer(toolBarPanel);
		selector.doLayout();
	}

	@Override // MouseListener
	public void mouseEntered(MouseEvent e) {
	}

	@Override // MouseListener
	public void mouseExited(MouseEvent e) {
	}

	@Override // MouseListener
	public void mousePressed(MouseEvent e) {
		final Container contentPane = getContentPane();
		// We get notifications from the content pane and from the scroll pane within it,
		// which may have slightly offset coordinates.
		// The dragging coordinates will be in the same space, and they are used relatively,
		// so there is no need to translate them.
		dragStartPoint = e.getPoint();
		final Object source = e.getSource();
		
		// However the resize corner is outside the scroll pane anyway.
		if (source == contentPane) {
			dragCorner = DRAG_WINDOW;
		} else if (e.getSource() == selectorScrollPane) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("SuggestionWindow.mousePressed: Scrollpane %s", e.getPoint().toString()));
			}
		} else {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer(String.format("SuggestionWindow.mousePressed: other source %s", e.toString()));
			}
		}
	}

	@Override // MouseListener
	public void mouseReleased(MouseEvent e) {
		dragCorner = DRAG_WINDOW;
	}

	@Override // ComponentListener
	public void componentHidden(ComponentEvent e) {
	}

	@Override // ComponentListener
	public void componentMoved(ComponentEvent e) {
	}

	@Override // ComponentListener
	public void componentResized(ComponentEvent e) {
		checkToolPanelLabels() ;
	}

	@Override // ComponentListener
	public void componentShown(ComponentEvent e) {
		selector.init(); // causes it to know its size and contents
		selector.workAroundTooltipBug(this);
		
		final int unitHeight = selector.getUnitHeight();
		int sideMargin = (this.getWidth() - getContentPane().getWidth()) / 2;
		int topMargin = this.getHeight() - getContentPane().getHeight() - sideMargin;
//		System.out.println("Top: " + topMargin + " Side: " + sideMargin);
		windowMargins.set(Math.max(topMargin, 0) + toolBarPanel.getHeight(), sideMargin, sideMargin, sideMargin);
		selectorScrollPane.getVerticalScrollBar().setUnitIncrement(unitHeight);
		selectorScrollPane.getHorizontalScrollBar().setUnitIncrement(unitHeight);
		selectorScrollPane.revalidate();
		if (prefWindowBounds != null) {
			// check if bounds are still on screen
			setBoundsChecked(prefWindowBounds);
		} else {
			this.setSize(getPreferredSize());
		}
		// settings from preferences
		if (incrementalMode) {
			incrementMap = new LinkedHashMap<Integer, List<SuggestionComponent>>(8);
			incrementLevel = 0;
			updateIncrementLevel(incrementLevel, -1);
		}
		if (reversedOrderMode) {
			selector.reverseOrderOfSuggestions();
		}
	}

	/**
	 * Use a KeyListener instead of InputMap and ActionMap because there is no
	 * access to the actually typed key (especially shift-0...9), without making
	 * a separate Action for each key.
	 */
	@Override // KeyListener
	public void keyTyped(KeyEvent e) {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer(String.format("SuggestionWindow.keyTyped %s", String.valueOf(e)));
		}
	}

	@Override // KeyListener
	public void keyPressed(KeyEvent e) {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer(String.format("SuggestionWindow.keyPressed %s", String.valueOf(e)));
		}
		
		if (suggestionSelectionListener != null) {
			final IGTSuggestionViewerModel model = selector.getModel();

			int selectedIndex = 1; // default; find out which key below.

			//SuggestionSelectionEvent event;
			JScrollBar sb;
			int code = e.getKeyCode();
			
			switch (code) {
			case KeyEvent.VK_SPACE:
			case KeyEvent.VK_ENTER:
				code = KeyEvent.VK_1;
				// fall through; pretend space was 1.
			case KeyEvent.VK_0:	case KeyEvent.VK_1:	case KeyEvent.VK_2:
			case KeyEvent.VK_3:	case KeyEvent.VK_4:	case KeyEvent.VK_5:
			case KeyEvent.VK_6:	case KeyEvent.VK_7:	case KeyEvent.VK_8:
			case KeyEvent.VK_9:
				selectedIndex = code - KeyEvent.VK_0;
				if (selectedIndex == 0) {
					selectedIndex = 10;
				}
				if (e.isShiftDown()) {
					selectedIndex += 10;
				}
				// to return the selected index from the model is a bit strange after reducing
				// the choices (via shift or alt click). E.g. with 3 suggestions visible, pressing
				// an 8 could still select a set that is currently not visible and pressing 3 might
				// not select the third visible suggestion
				// same for reversed order mode
				/*  old implementation
				if (selectedIndex <= model.getRowCount()) {
					event = new SuggestionSelectionEvent(model, selectedIndex - 1);
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer(String.format("suggestionSelected %d\n", selectedIndex - 1));
					}
					suggestionSelectionListener.suggestionSelected(event);
					closeSelectionWindow();
				}*/
				if (selectedIndex <= selector.getVisibleSuggestionCount()) {
					if (!incrementalMode || incrementLevel == MAX_INCREMENT_LEVEL) {// apply
						int modelIndex = selector.convertVisibleIndexToModel(selectedIndex - 1);// selected index 1-based
						if (modelIndex > -1 && modelIndex < model.getRowCount()) {// model index 0-based
							if (LOG.isLoggable(Level.FINER)) {
								LOG.finer(String.format("visibleSuggestionSelected %d\n", modelIndex));
							}
							applySelectedSuggestionAndClose(modelIndex);
//							event = new SuggestionSelectionEvent(model, modelIndex);
//
//							suggestionSelectionListener.suggestionSelected(event);
//							closeSelectionWindow();
						}
					} else {
						SuggestionComponent sugComp = selector.getSuggestionAtVisibleIndex(selectedIndex - 1);
						int hash = sugComp.getHashOfFrag(incrementLevel);
						incrementLevel++;
						updateIncrementLevel(incrementLevel, hash);
					}
				}
				break;
				
			case KeyEvent.VK_TAB: // only gets reported when setFocusTraversalKeysEnabled(false)
			case KeyEvent.VK_BACK_SPACE:
			case KeyEvent.VK_DELETE:
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer("suggestionIgnored");
				}
				applySelectedSuggestionAndClose(-1);
				break;
				
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_W: // also matches Command-W
				SuggestionSelectionEvent event = new SuggestionSelectionEvent(model, -1);
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer("suggestionClosed");
				}
				suggestionSelectionListener.suggestionClosed(event);
				closeSelectionWindow();
				break;

				// This should not be needed for keyboard control of the
				// scroll area, but I didn't get it to work otherwise.
			case KeyEvent.VK_PAGE_UP:
				sb = selectorScrollPane.getVerticalScrollBar();
				sb.setValue(sb.getValue() - sb.getBlockIncrement(-1));
				break;
			
			case KeyEvent.VK_PAGE_DOWN:
				sb = selectorScrollPane.getVerticalScrollBar();
				sb.setValue(sb.getValue() + sb.getBlockIncrement(1));
				break;

			case KeyEvent.VK_UP:
				sb = selectorScrollPane.getVerticalScrollBar();
				sb.setValue(sb.getValue() - sb.getUnitIncrement(-1));
				break;
			
			case KeyEvent.VK_DOWN:
				sb = selectorScrollPane.getVerticalScrollBar();
				sb.setValue(sb.getValue() + sb.getUnitIncrement(1));
				break;

			case KeyEvent.VK_LEFT:
				sb = selectorScrollPane.getHorizontalScrollBar();
				sb.setValue(sb.getValue() - sb.getUnitIncrement(-1));
				break;
			
			case KeyEvent.VK_RIGHT:
				sb = selectorScrollPane.getHorizontalScrollBar();
				sb.setValue(sb.getValue() + sb.getUnitIncrement(1));
				break;
				
			case KeyEvent.VK_R:
				reverseButton.doClick();
				break;
				
			case KeyEvent.VK_I:
				incrementalSelectCB.doClick();
				break;
			}
		}
	}

	@Override // KeyListener
	public void keyReleased(KeyEvent e) {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer(String.format("SuggestionWindow.keyReleased %s", String.valueOf(e)));
		}
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reverseButton) {
			if (selector != null) {
				selector.reverseOrderOfSuggestions();
			}
			// the button might have been clicked and therefore have the focus
			reverseButton.transferFocusUpCycle();
			reversedOrderMode = !reversedOrderMode;
		}		
	}

	/**
	 * Check box(es) listener. (De-)activates the incremental mode of the
	 * suggestion selector.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == incrementalSelectCB) {
			if (selector != null) {				
				incrementalMode = e.getStateChange() == ItemEvent.SELECTED;
				if (incrementalMode) {
					incrementMap = new LinkedHashMap<Integer, List<SuggestionComponent>>(8);
					incrementLevel = 0;
					updateIncrementLevel(incrementLevel, -1);
				} else {
					incrementLevel = -1;
					increaseChoiceOfSuggestions();
					incrementalLevelLabel.setText("");
				}
			}
			checkToolPanelLabels();
			// the check box might have been clicked and therefore have the focus
			incrementalSelectCB.transferFocusUpCycle();
		}
	}

	/**
	 * If there is too little space for the "Remember windows position" checkbox label and the 
	 * increment level label, hide the former label.
	 */
	private void checkToolPanelLabels() {
		if (reuseWindowBoundsCB == null) {
			return;
		}
		// in incremental mode, check if there is enough space for the (re)store window checkbox
		if (incrementalMode) {
			if (reuseWindowBoundsCB.getX() > 0 && reuseWindowBoundsCB.getY() > 0) {
				Rectangle leftComp = incrementalLevelLabel.getBounds();
				if (leftComp.x + leftComp.width >= reuseWindowBoundsCB.getX()) {
					reuseWindowBoundsCB.setToolTipText(reuseWindowBoundsCB.getText());
					reuseWindowBoundsCB.setText("");
				} else {
					reuseWindowBoundsCB.setToolTipText(null);
					reuseWindowBoundsCB.setText(ElanLocale.getString(
							"InterlinearEditor.Suggestions.RestoreWindowBounds"));
				}
			}
		} else {
			if (reuseWindowBoundsCB.getText().isEmpty()) {
				reuseWindowBoundsCB.setToolTipText(null);
				reuseWindowBoundsCB.setText(ElanLocale.getString(
						"InterlinearEditor.Suggestions.RestoreWindowBounds"));
			}
		}
	}
	
	/**
	 * Set the bounds for this window after checking the bounds against the current screen
	 * and insets.
	 * 
	 * @param restBounds the bounds as (re)stored from preferences
	 */
	private void setBoundsChecked(Rectangle restBounds) {
		try {
			Dimension screenSizeDim = Toolkit.getDefaultToolkit().getScreenSize();
			Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
			
			if (restBounds.x < screenInsets.left) {
				restBounds.x = screenInsets.left;
			}		
			if (restBounds.x + restBounds.width > screenSizeDim.width - screenInsets.right) {
				restBounds.x = screenSizeDim.width - screenInsets.right - restBounds.width;
			}
			if (restBounds.y < screenInsets.top) {
				restBounds.y = screenInsets.top;
			}
			if (restBounds.y + restBounds.height > screenSizeDim.height - screenInsets.bottom) {
				restBounds.y = screenSizeDim.height - screenInsets.bottom - restBounds.height;
			}
			this.setBounds(restBounds);
		} catch (Throwable t) {}
		
	}

	//###################################	
	class WindowHandler extends WindowAdapter {
		/**
		 * Notifies listeners that the suggestions window was closed without
		 * making a selection.
		 */
		@Override
		public void windowClosing(WindowEvent e) {
			if (selector != null) {
				if (suggestionSelectionListener != null) {
					SuggestionSelectionEvent event = new SuggestionSelectionEvent(selector.getModel(), -1);
					suggestionSelectionListener.suggestionClosed(event);
				}
				closeSelectionWindow();
			}
		}		 
	}
	
	/** for setting a breakpoint */
//	@Override
//	public void update(Graphics g) {
//		super.update(g);
//	}
}
