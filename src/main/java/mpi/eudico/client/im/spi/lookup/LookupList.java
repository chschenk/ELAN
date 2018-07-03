/*
 * derived from CityInputMethod.java
       DONE
        - move LookupList into a central place for all pinyin input methods.
       TO DO
        - allow change of fontsize.
 */
package mpi.eudico.client.im.spi.lookup;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
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

import javax.swing.JFrame;


/**
 * DOCUMENT ME! $Id: LookupList.java 43915 2015-06-10 09:02:42Z olasei $
 *
 * @author $Author$
 * @version $Revision$
 */
public class LookupList extends Canvas {
    /** Holds value of property DOCUMENT ME! */
    InputMethod inputMethod;

    /** Holds value of property DOCUMENT ME! */
    InputMethodContext context;

    /** Holds value of property DOCUMENT ME! */
    Window lookupWindow;

    /** Holds value of property DOCUMENT ME! */
    String[] candidates;

    /** Holds value of property DOCUMENT ME! */
    Locale[] locales;

    /** Holds value of property DOCUMENT ME! */
    int candidateCount;

    /** Holds value of property DOCUMENT ME! */
    int selected;

    /** Holds value of property DOCUMENT ME! */
    int lookupCandidateIndex;

    /** Holds value of property DOCUMENT ME! */
    final int FONT_SIZE = 22;

    /** Holds value of property DOCUMENT ME! */
    final int INSIDE_INSET = 4; // even number!?

    /** Holds value of property DOCUMENT ME! */
    final int LINE_SPACING = FONT_SIZE + (INSIDE_INSET / 2);

    /**
     * Creates a new LookupList instance
     *
     * @param inputMethod DOCUMENT ME!
     * @param context DOCUMENT ME!
     * @param candidates DOCUMENT ME!
     * @param candidateCount DOCUMENT ME!
     */
    public LookupList(InputMethod inputMethod, InputMethodContext context,
        String[] candidates, int candidateCount) {
        if (context == null) {
            System.out.println(
                "assertion failed! LookupList.java context is null!");
        }

        this.inputMethod = inputMethod;
        this.context = context;
        this.candidates = candidates;
        this.candidateCount = candidateCount;
        lookupCandidateIndex = 0;
        lookupWindow = context.createInputMethodJFrame("Lookup list", true);

        // Find an appropriate Chinese font
        // note: Canvas is the only AWT component, where setFont() works!
        setFont(new Font("Arial Unicode MS", Font.PLAIN, FONT_SIZE));
        setSize(100, (10 * LINE_SPACING) + (2 * INSIDE_INSET));
        setForeground(Color.black);
        setBackground(Color.white);

        enableEvents(AWTEvent.KEY_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);

        ((JFrame) lookupWindow).getContentPane().add(this);
        lookupWindow.pack();
        updateWindowLocation();
        lookupWindow.setVisible(true);
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
     * DOCUMENT ME!
     *
     * @param candidateIndex DOCUMENT ME!
     */
    public void updateCandidates(int candidateIndex) {
        lookupCandidateIndex = candidateIndex;
        repaint();
    }

    // method of LookupList...
    public void selectCandidate(int candidate) {
        selected = candidate;
        repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param g DOCUMENT ME!
     */
    @Override
	public void paint(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        int descent = metrics.getDescent();
        int ascent = metrics.getAscent();
        int windowCount;

        if ((candidateCount - lookupCandidateIndex) < 10) {
            windowCount = (candidateCount - lookupCandidateIndex);
        } else {
            windowCount = 10;
        }

        for (int i = lookupCandidateIndex;
                i < (lookupCandidateIndex + windowCount) && i < candidates.length; i++) {
            int displayedNumber = ((i - lookupCandidateIndex) + 1);
            displayedNumber = (displayedNumber == 10) ? 0 : displayedNumber;

            // in the old days, the variable 
            //displayedNumber
            // was painted here too.
            g.drawString("   " + candidates[i], INSIDE_INSET,
                ((LINE_SPACING * ((i - lookupCandidateIndex) + 1)) +
                INSIDE_INSET) - descent);
        }

        Dimension size = getSize();
        g.drawRect(INSIDE_INSET / 2,
            ((LINE_SPACING * (selected + 1)) + INSIDE_INSET) -
            (descent + ascent + 1), size.width - INSIDE_INSET,
            descent + ascent + 2);
        g.drawRect(0, 0, size.width - 1, size.height - 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param visible DOCUMENT ME!
     */
    @Override
	public void setVisible(boolean visible) {
        if (!visible && (lookupWindow != null)) {
            lookupWindow.setVisible(false);
            lookupWindow.dispose();
            lookupWindow = null;
        } else if (lookupWindow != null) {
            lookupWindow.setVisible(true);
            lookupWindow.toFront();
        }

        super.setVisible(visible);
    }

    /**
     * DOCUMENT ME!
     *
     * @param event DOCUMENT ME!
     */
    @Override
	protected void processKeyEvent(KeyEvent event) {
        inputMethod.dispatchEvent(event);
    }

    // kriegt nur mouseentered und mouseleft mit
    // clicks f"uhren zu hide window
    // noch bevor the funktion gerufen wird
    @Override
	protected void processMouseEvent(MouseEvent event) {
        // HS feb-2004 MOUSE_PRESSED now seem to come through normally (jdk 1.4.x)
        // dispatch the event
        //int y = event.getY();
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            inputMethod.dispatchEvent(event);

            //if (y >= INSIDE_INSET && y < INSIDE_INSET + candidateCount * LINE_SPACING) {
            //inputMethod.selectCandidate((y - INSIDE_INSET) / LINE_SPACING);
            //inputMethod.hideWindows();
            //}
        }
    }
}
