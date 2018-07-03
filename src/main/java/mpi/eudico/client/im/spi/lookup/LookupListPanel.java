/*
 * derived from CityInputMethod.java
 */
package mpi.eudico.client.im.spi.lookup;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.TextHitInfo;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodContext;

import java.util.Locale;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;


/**
 * A re-implementation of a lookup list, with a JPanel as the basis and
 * a JList for rendering the candidates.
 *
 * @author $Author$
 * @version 2.0 Oct 2013
 */
public class LookupListPanel extends JPanel {
    /** the input method, a Lookup instance */
    InputMethod inputMethod;

    /** the input method context */
    InputMethodContext context;

    /** the lookup window, a JFrame */
    Window lookupWindow;

    /** the current array of candidates */
    String[] candidates;

    /** the array of locales */
    Locale[] locales;

    /** the number of candidates */ // same as the length of the array?
    int candidateCount;

    /** the array index of the element that is shown as first row in the list */
    int lookupCandidateIndex;

    /** default font size */
    final int FONT_SIZE = 20;

    /** a margin */
    final int INSIDE_INSET = 4; // even number!?

    /** Holds value of property DOCUMENT ME! */ // delete??
    final int LINE_SPACING = FONT_SIZE + (INSIDE_INSET / 2);
    /** the number of visible elements in the list */
    final int NUM_VISIBLE_ROWS = 10;
    int windowWidth = 80;
    
    /**
     * Currently the list is not in a scroll pane, scrolling is handled by this class itself. 
     */
    private JList luList;
    private DefaultListModel luModel;

    /**
     * Creates a new LookupList instance
     *
     * @param inputMethod the input method
     * @param context the input context
     * @param candidates an array of candidates
     * @param editorFont the font in use by the underlying text component 
     */
    public LookupListPanel(InputMethod inputMethod, InputMethodContext context,
        String[] candidates, Font editorFont) {
        if (context == null) {
            System.out.println(
                "assertion failed! LookupList.java context is null!");
            return;
        }

        this.inputMethod = inputMethod;
        this.context = context;
        this.candidates = candidates;
        this.candidateCount = candidates.length;
        lookupCandidateIndex = 0;
        lookupWindow = context.createInputMethodJFrame("Lookup list", true);
        //lookupWindow = context.createInputMethodWindow("Lookup list", true);
        
        setLayout(new BorderLayout(INSIDE_INSET / 2, INSIDE_INSET / 2));
        luModel = new DefaultListModel();
        for (int i = 0; i < candidates.length && i < NUM_VISIBLE_ROWS; i++) {
        	luModel.add(i, candidates[i]);
        }
        luList = new JList(luModel);
        luList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        luList.setCellRenderer(new CandidateRenderer());
        //luList.setLayoutOrientation(JList.HORIZONTAL_WRAP);// could support horizontal layout of the candidates
//        luList.setFixedCellHeight(height);
        add(luList);
        // would be good to be able to use the font from the text component
        if (editorFont != null) {
        	if (editorFont.getSize() != FONT_SIZE) {// not necessarily
        		setFont(editorFont.deriveFont( (float) FONT_SIZE));
        	} else {
        		setFont(editorFont);
        	}
        } else {
        	setFont(new Font("Arial Unicode MS", Font.PLAIN, FONT_SIZE));
    	}
        luList.setFont(getFont());
        Dimension prefSize = new Dimension(windowWidth, (NUM_VISIBLE_ROWS * LINE_SPACING) + (2 * INSIDE_INSET));
        setPreferredSize(prefSize);
        luList.setPreferredSize(prefSize);

        enableEvents(AWTEvent.KEY_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);

        ((JFrame) lookupWindow).getContentPane().add(this);
        //((JFrame) lookupWindow).setUndecorated(true);// this works on Mac to get the width less than 128
        //lookupWindow.add(this);
        lookupWindow.pack();        
        if (lookupWindow.getWidth() > windowWidth) {
        	lookupWindow.setSize(windowWidth, lookupWindow.getHeight());
        }
        updateWindowLocation();
        lookupWindow.setVisible(true);
        //System.out.println("Panel size: " + this.getSize());
        luList.setFixedCellHeight(luList.getHeight() / NUM_VISIBLE_ROWS);
    }

    /**
     * Positions the lookup window near (usually below) the insertion point in
     * the component where composition occurs.
     */
    private void updateWindowLocation() {
        Point windowLocation = new Point();
        Rectangle caretRect = context.getTextLocation(TextHitInfo.leading(0));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = lookupWindow.getSize();
        final int SPACING = 2;

        if ((caretRect.x + windowSize.width) > screenSize.width) {
            windowLocation.x = screenSize.width - windowSize.width;
        } else {
            windowLocation.x = caretRect.x;
        }

        if ((caretRect.y + caretRect.height + SPACING + windowSize.height) > screenSize.height) {
            windowLocation.y = caretRect.y - SPACING - windowSize.height;
        } else {
            windowLocation.y = caretRect.y + caretRect.height + SPACING;
        }

        lookupWindow.setLocation(windowLocation);
    }

    /**
     * Selects the candidate at the specified index in the candidates array.
     * 
     * @param candidate the index in the candidates array (zero based)
     */
    void selectCandidate(int candidate) {
    	//System.out.println("Select: " + candidate);
    	
    	if (candidate < 0) {
    		candidate = 0;
    	}
    	if (candidate > candidateCount - 1) {
    		candidate = candidateCount - 1;
    	}
    	
    	int targetRowIndex = candidate - lookupCandidateIndex;
    	if (targetRowIndex < 0) {// make the new candidate the first in the list
    		// renew the list, could check for diff > NUM_VISIBLE_ROWS
    		luModel.removeAllElements();
    		for (int i = candidate, j = 0; i < candidates.length && j < NUM_VISIBLE_ROWS; i++, j++) {
    			luModel.addElement(candidates[i]);
    		}
    		lookupCandidateIndex = candidate;
    		// select first row
    		luList.setSelectedIndex(0);
    	} else if (targetRowIndex > NUM_VISIBLE_ROWS) { // make the new candidate the last in the list
    		int firstEl = candidate - NUM_VISIBLE_ROWS;
    		
    		luModel.removeAllElements();
    		
    		for (int i = firstEl, j = 0; i < candidateCount && j < NUM_VISIBLE_ROWS; i++, j++) {
    			luModel.addElement(candidates[i]);
    		}
    		lookupCandidateIndex = firstEl;
    		luList.setSelectedIndex(luModel.getSize() - 1);
    	} else {
    		luList.setSelectedIndex(targetRowIndex);
    	}
    }
    
    /**
     * Select the next item in the array, if it exists.
     * 
     * Updates the elements in the list if necessary.
     */
    void selectNext() {
    	int curSelRow = luList.getSelectedIndex();
    	if (curSelRow + lookupCandidateIndex == candidateCount - 1) {
    		return;
    	}
    	
    	if (curSelRow == NUM_VISIBLE_ROWS - 1) {
    		// don't change the selected row, scroll the candidates
    		lookupCandidateIndex++;
    		luModel.remove(0);
    		luModel.addElement(candidates[NUM_VISIBLE_ROWS - 1 + lookupCandidateIndex]);
    		luList.setSelectedIndex(curSelRow);
    	} else {
    		curSelRow++;
    		luList.setSelectedIndex(curSelRow);
    	}
    }
    
    /**
     * Select the previous item in the array, if it exists.
     * 
     * Updates the elements in the list if necessary.
     */
    void selectPrevious() {
    	int curSelRow = luList.getSelectedIndex();
    	if (curSelRow == 0 && lookupCandidateIndex == 0) {
    		return;
    	}
    	
    	if (curSelRow == 0) {
    		// scroll the model
    		luModel.remove(luModel.size() - 1);
    		lookupCandidateIndex--;
    		luModel.add(0, candidates[lookupCandidateIndex]);
    		luList.setSelectedIndex(curSelRow);
    	} else {
    		curSelRow--;
    		luList.setSelectedIndex(curSelRow);
    	}
    }
    
    /**
     * Scrolls the elements in the list page up (one scroll view) and selects the new element
     * at the row currently selected.
     */
    void selectPageUp() {
    	if (luModel.getSize() < NUM_VISIBLE_ROWS) {
    		// select the last item?
    		luList.setSelectedIndex(0);
    		return;
    	}
    	
    	int curSelRow = luList.getSelectedIndex();
    	int targetBeginIndex = lookupCandidateIndex - NUM_VISIBLE_ROWS;
    	if (targetBeginIndex < 0) {
    		targetBeginIndex = 0;    		
    	}
    	if (targetBeginIndex != lookupCandidateIndex) {
    		lookupCandidateIndex = targetBeginIndex;
    		luModel.clear();
    		for (int i = lookupCandidateIndex, j = 0; i < candidateCount && j < NUM_VISIBLE_ROWS; i++, j++) {
    			luModel.addElement(candidates[i]);
    		}
    		
    		luList.setSelectedIndex(curSelRow);
    	}
    }
    
    /**
     * Scrolls the elements in the list page down (one scroll view down) and selects the new element 
     * at the row currently selected.
     */
    void selectPageDown() {
    	if (luModel.getSize() < NUM_VISIBLE_ROWS) {
    		// select the last item?
    		luList.setSelectedIndex(luModel.getSize() - 1);
    		return;
    	}
    	
    	int curSelRow = luList.getSelectedIndex();
    	int targetBeginIndex = lookupCandidateIndex + NUM_VISIBLE_ROWS;
    	if (targetBeginIndex + NUM_VISIBLE_ROWS > candidateCount) {
    		targetBeginIndex = candidateCount - NUM_VISIBLE_ROWS;    		
    	}
    	if (targetBeginIndex != lookupCandidateIndex) {
    		lookupCandidateIndex = targetBeginIndex;
    		luModel.clear();
    		for (int i = lookupCandidateIndex, j = 0; i < candidateCount && j < NUM_VISIBLE_ROWS; i++, j++) {
    			luModel.addElement(candidates[i]);
    		}
    		if (curSelRow < luModel.getSize()) { 
    			luList.setSelectedIndex(curSelRow);
    		} else {
    			luList.setSelectedIndex(luModel.getSize() - 1);
    		}
    	}
    }
    
    /**
     * Selects the first element in the array of candidates, if necessary updates the visible list.
     */
    void selectHome() {
    	if (lookupCandidateIndex > 0) {
    		luModel.removeAllElements();
    		
    		for (int i = 0; i < candidateCount && i < NUM_VISIBLE_ROWS; i++) {
    			luModel.addElement(candidates[i]);
    		}
    		lookupCandidateIndex = 0;
    	}
		luList.setSelectedIndex(0);
    }
    
    /**
     * Selects the very last element in the candidate array, updates the list of
     * visible elements if necessary.
     */
    void selectEnd() {
    	if (lookupCandidateIndex < candidateCount - luModel.getSize()) {
    		luModel.removeAllElements();
    		
    		int firstEl = candidateCount - NUM_VISIBLE_ROWS;
    		if (firstEl < 0) {
    			firstEl = 0;
    		}
    		
    		for (int i = firstEl, j = 0; i < candidateCount && j < NUM_VISIBLE_ROWS; i++, j++) {
    			luModel.addElement(candidates[i]);
    		}
    		lookupCandidateIndex = firstEl;
    	}
    	luList.setSelectedIndex(luModel.getSize() - 1);
    }
    
    /**
     * Returns the index in the array of candidates of the clicked element.
     * 
     * @param mouseLocation the y coordinate of the click location 
     * @return the index in the candidates array of the clicked candidate
     */
    int getSelectedCandidateIndex(int mouseLocation) {
    	//System.out.println("Y: " + mouseLocation);
    	int curSelRow = mouseLocation / luList.getFixedCellHeight();
    	//System.out.println("Row: " + curSelRow);
    	if (curSelRow + lookupCandidateIndex >= candidateCount) {
    		return -1;
    	}
    	
    	return curSelRow + lookupCandidateIndex;
    }
    
    /**
     * Returns the index of the current selected candidate, which is the sum
     * of the current selected row and the scroll index of the first visible row
     * 
     * @return the index of the selected row
     */
    int getSelectedCandidateIndex() {
    	int curSelRow = luList.getSelectedIndex();
    	
    	return curSelRow + lookupCandidateIndex;
    }
    
    /**
     * Returns the index of the candidate that has the given number shortcut key 
     * displayed next to it.
     * 
     * @param numberKey a number between 0 and 9 
     * @return the index in the candidate array of the item that is currently listed next to the given number
     */
    int getCandidateIndexForNumberKeyShortcut (int numberKey) {
    	if (numberKey < 0 || numberKey > 9 || numberKey > NUM_VISIBLE_ROWS) {
    		return -1;
    	}
    	
    	int firstRowNumber = lookupCandidateIndex % NUM_VISIBLE_ROWS + 1;
    	if (numberKey == firstRowNumber) {
    		return lookupCandidateIndex;
    	} else if (numberKey < firstRowNumber) {
    		int targetRow = NUM_VISIBLE_ROWS - (firstRowNumber - numberKey);
    		if (targetRow < luModel.getSize()) {// this should already mean that targetIndex should be < candidateCount ??
    			int targetIndex = lookupCandidateIndex + targetRow;
    			if (targetIndex < candidateCount) {
    				return targetIndex;
    			}
    		} 
    	} else {
			int distance = numberKey - firstRowNumber;
			int targetIndex = lookupCandidateIndex + distance;
			//System.out.println("Target index in array: " + targetIndex);
			if (targetIndex < candidateCount) {
				return targetIndex;
			}
		}
    	
    	return -1;
    }

    /**
     * Makes the lookup window visible/invisible.
     *
     * @param visible the visibility flag
     */
    @Override
	public void setVisible(boolean visible) {
        if (!visible && (lookupWindow != null)) {
            lookupWindow.setVisible(false);
            lookupWindow.dispose();
            lookupWindow = null;
        } else if (visible && lookupWindow != null) {
            lookupWindow.setVisible(true);
            lookupWindow.toFront();
        }

        super.setVisible(visible);
    }

    /**
     * Process a key event.
     *
     * @param event the key event
     */
    @Override
	protected void processKeyEvent(KeyEvent event) {
    	//System.out.println("Process Key: " + event.getKeyChar());
        inputMethod.dispatchEvent(event);
    }

    /**
     * Delegates the event to the input method. 
     * 
     * @param event the mouse event
     */
    @Override
	protected void processMouseEvent(MouseEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            inputMethod.dispatchEvent(event);
        }
    }
    
    /**
     * A renderer for characters/strings that are candidates in the list that shows
     * the entries together with a digit representing the keyboard shortcut for that
     * candidate.
     * TODO implement a horizontal and a vertical list. Currently only vertical.
     * 
     * @author Han Sloetjes
     */
    class CandidateRenderer extends JPanel implements ListCellRenderer {
    	private JLabel candidateLabel;
    	private JLabel numberLabel;
    	
    	/**
    	 * Constructor.
    	 */
		public CandidateRenderer() {
			super(new GridBagLayout());
			setOpaque(true);
			candidateLabel = new JLabel();
			numberLabel = new JLabel();
			numberLabel.setHorizontalAlignment(SwingConstants.TRAILING);
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, 4, 0, 0);
			add(candidateLabel, gbc);
			gbc.gridx = 1;
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets(0, 0, 0, 4);
			add(numberLabel, gbc);
		}

		/**
		 * Renders the candidates together with a number "hot key". The number is based on the position
		 * in the array of candidates, not merely the row number (although this might be an alternative to consider).
		 * 
		 *  @param list the JList rendering the data
		 *  @param value the candidate character
		 *  @param index the row number, or the index in the model
		 *  @param isSelected whether the cell to render is selected
		 *  @param cellHasFocus whether the cell has the focus
		 */
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			if (value instanceof String) {
				candidateLabel.setText((String) value);
			} else {// Character
				candidateLabel.setText(value.toString());
			}
			numberLabel.setText(String.valueOf( (index + lookupCandidateIndex + 1) % NUM_VISIBLE_ROWS));
			
			candidateLabel.setFont(list.getFont());
			numberLabel.setFont(list.getFont());
			
			if (isSelected) {
				this.setBackground(list.getSelectionBackground());
				this.setForeground(list.getSelectionForeground());
				candidateLabel.setBackground(list.getSelectionBackground());
				candidateLabel.setForeground(list.getSelectionForeground());
				numberLabel.setBackground(list.getSelectionBackground());
				numberLabel.setForeground(list.getSelectionForeground());
			} else {
				this.setBackground(list.getBackground());
				this.setForeground(list.getForeground());
				candidateLabel.setBackground(list.getBackground());
				candidateLabel.setForeground(list.getForeground());
				numberLabel.setBackground(list.getBackground());
				numberLabel.setForeground(list.getForeground());
			}

			return this;
		}

    }
}
