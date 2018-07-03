package mpi.eudico.client.im.spi.lookup;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.text.AttributedString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;



/**
 * This class is an implementation of java.awt.im.spi.InputMethod and a re-implementation of Lookup.java.
 * The input methods provided by this class share one property: all have over-the-spot
 * lookup windows which are composed by a word or a character. 
 * The lookup window contains a list of characters. The user selects a
 * character from the lookup window by pressing enter or space. The user
 * navigates through the lookup window with up, down, and PgUp and PgDn, Home and End.
 */
public class Lookup2 implements InputMethod {

    /**
     * The International Phonetic Association has standardised a phonetic
     * alphabet. These constants refer to the IPA alphabet in the revision as
     * of 1996. The default input method is RTR.
     * 
     * <p>
     * Here is the only place, where the IPA-96 locales should be defined. All
     * other Eudico classes should refer to this constant. Locales are using
     * lowercase letters only.
     * </p>
     */
    public static final Locale IPA96_RTR = new Locale("ipa-96", "", "rtr");

    /** Holds value of property DOCUMENT ME! */
    public static final Locale CHINESE_SIM = new Locale("chinese", "",
            "simplified");

    /** Holds value of property DOCUMENT ME! */
    public static final Locale CHINESE_TRA = new Locale("chinese", "",
            "traditional");

    /**
     * This array defines the locales for which input methods are implemented.
     */
    static Locale[] SUPPORTED_LOCALES = {
        Lookup2.IPA96_RTR, Lookup2.CHINESE_SIM, Lookup2.CHINESE_TRA
    };

    /** resources for the locales. */
    private static Map<Locale, String> hashedFilenames;

    static {
        hashedFilenames = new HashMap<Locale, String>();
        hashedFilenames.put(Lookup2.SUPPORTED_LOCALES[0], "ipa96.u8");
        hashedFilenames.put(Lookup2.SUPPORTED_LOCALES[1], "PinyinSC.u8");
        hashedFilenames.put(Lookup2.SUPPORTED_LOCALES[2], "PinyinTC.u8");
    }

    private static HashMap<String, List<String>> pinyinHash;

    /* lookup information */
    private String[] lookupCandidates;
    private LookupListPanel lookupList;
    private Font editorFont;

    /** the input method context */
    private InputMethodContext inputMethodContext;

    private Locale locale;
    private boolean converted;
    private StringBuilder rawText = new StringBuilder();
    private String convertedText;

    /**
     * No-arg constructor.
     * 
     * @see java.awt.im.spi.InputMethod
     */
    public Lookup2() {
    	//System.out.println("Lookup created");
    }


    /**
     * Loads the mappings of keyboard keys to IPA or Chinese characters.
     * The locale should not be null, should be one of the known and supported locales.
     *
     * @param locale the locale to load
     *
     * @throws IOException if the resource file for the locale is not found
     */
    private final void initializeHash(Locale locale) throws IOException {
        synchronized (getClass()) {
            Lookup2.pinyinHash = new HashMap<String, List<String>>();

            // Read UTF8 character stream
            BufferedReader datafile = new BufferedReader(new InputStreamReader(
                        getClass().getResourceAsStream((String) hashedFilenames.get(
                                locale)), "UTF8"));

            String buffer;

            while ((buffer = datafile.readLine()) != null) {
                int index = buffer.indexOf("\t");
                String pinyin = buffer.substring(0, index);
                ArrayList<String> newlist = new ArrayList<String>();
                int oldindex = index + 1;

                do {
                    index = buffer.indexOf(" ", oldindex);

                    if (index == -1) {
                        index = buffer.length();
                    }

                    String hanzi = buffer.substring(oldindex, index);

                    if (hanzi.length() > 0) {
                        newlist.add(hanzi);
                    }

                    oldindex = index + 1;
                } while (oldindex < buffer.length());

                Lookup2.pinyinHash.put(pinyin.intern(), newlist);
            }

            datafile.close();
        }
    }

    /**
     * Appends this character to the raw text and looks up the result in the hash.
     *
     * @param ch the character to append to the string to look up
     *
     * @return true if the word with the specified character appended is a key in the hash map
     */
    private final boolean wordResultsInHash(char ch) {
        return pinyinHash.containsKey(this.rawText +
            new Character(ch).toString());
    }

    /**
     * Looks up the specified character and adds it to the "raw text" if there is a mapping
     * for the new "key".
     *
     * @param ch the character to look up
     *
     * @return true if the character is, once appended, in the hash
     */
    private final boolean lookupCharacter(char ch) {
        if (wordResultsInHash(ch)) {
            /*
               The user starts a pinyin word.
               A lookup list will be opened.
             */
            rawText.append(ch);
            sendText(false);

            return true;
        }

        return false;
    }

 

    /**
     * Handle non-character keys, such as arrows
     *
     * @param e the key event of type key pressed
     */
    private final void handlePressedKey(KeyEvent e) {
        if (lookupList == null) {
            return;
        }

        /*
           There is a lookup list.
           The user continues a pinyin word
         */

        // Two _KP_ (keypad) constants added for Linux support
        switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
            //break;
            // fall through
        case KeyEvent.VK_KP_UP:
            lookupList.selectPrevious();

            break; // Linux?

        case KeyEvent.VK_DOWN:

        	// fall through
        case KeyEvent.VK_KP_DOWN:
            lookupList.selectNext();

            break; // Linux?

        case KeyEvent.VK_PAGE_UP:
        	//System.out.println("Page Up!");
            lookupList.selectPageUp();

            break;

        case KeyEvent.VK_PAGE_DOWN:
        	//System.out.println("Page Down!");
        	lookupList.selectPageDown();

            break;

            // handle escape here, check key code (27)
        case KeyEvent.VK_ESCAPE:
			cancelEdit();
			
        	break;
        	
        case KeyEvent.VK_HOME:
        	//System.out.println("Home!");
        	lookupList.selectHome();
			
        	break;
        	
        case KeyEvent.VK_END:
        	//System.out.println("End!");
        	lookupList.selectEnd();
			
        	break;
        }
        
        e.consume(); //any other non-character keys
    }

    /**
     * Handle a typed character key.
     *
     * @param e the key event
     */
    private final void handleTypedKey(KeyEvent e) {
        char ch = e.getKeyChar();
        
        if ((lookupList != null) && lookupList.isVisible()) {
            /* There is a lookup list.
               The user continues a pinyin word or commits a single character
             */
            if ((KeyEvent.VK_SPACE == ch) || (KeyEvent.VK_ENTER == ch)) {
                selectCandidate(lookupList.getSelectedCandidateIndex());
                commit();
                closeLookupWindow();
                e.consume();

                return;
            }

            if (ch == '\b') {
                if (rawText.length() != 0) {
                    rawText.setLength(rawText.length() - 1);
                    sendText(false);
                }

                e.consume();

                return;
            }
            
            if (ch >= '0' && ch <= '9') {
    			int intValue = Character.getNumericValue(ch);
    			//System.out.println("Digit: " + intValue);
    			int indexInArray = lookupList.getCandidateIndexForNumberKeyShortcut(intValue);
    			//System.out.println("Index: " + indexInArray);
    			if (indexInArray < 0) {
    				// do nothing
    			} else {
                    selectCandidate(indexInArray);
                    commit();
                    closeLookupWindow();
                    e.consume();
                    
                    return;
    			}
            }

            lookupCharacter(ch);

            /*
               The user typed a character, but the resulting (pinyin) word does not exist.
               Such word will not be written.
               If there is a lookup list: always consume, no matter if the word
               results in the hash or not
             */
            e.consume();

            return;
        } else {
            // There is no lookup list.		
            // There should be no rawText, otherwise, there is something wrong.
            if (rawText.length() != 0) {
                System.out.println("There is Raw Text but no lookup list");
            }

            // We may have to open a lookup list.
            if (lookupCharacter(ch)) {
                /*
                   The user starts a pinyin word or ipa character.
                   A lookup list will be opened.
                 */
                e.consume();
            } else {
                /*
                   The character does not exist in the hash, not even as the beginning of a
                   word.
                   Pass through the underlying editor unchanged, and without consuming.
                 */
            }
        }
    }

    /**
     * Commits the raw text to the text component, resets flags and buffers. 
     */
    private final void commit() {
        sendText(true);
        rawText.setLength(0);
        convertedText = null;
        converted = false;
        closeLookupWindow();
    }

    /**
     * Dispatches a more or less empty InputMethodEvent, resulting in
     * cancellation of the edit operation. Resets object values and closes the
     * LookupList  (when necessary).
     */
    private void cancelEdit() {
        inputMethodContext.dispatchInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
            null, 0, TextHitInfo.leading(0), null);
        rawText.setLength(0);
        convertedText = null;
        converted = false;
        closeLookupWindow();
    }

    /**
     * Sends the (composed) text to the component
     *
     * @param committed if true the text is send as committed text
     */
    private final void sendText(boolean committed) {
        String text;
        InputMethodHighlight highlight;
        int committedCharacterCount = 0;

        if (converted) {
            text = convertedText;
            highlight = InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
        } else if (rawText.length() > 0) {
            text = new String(rawText);
            highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;

            // Redo list of characters in look up window based on latest pinyin string
            String lookupName;
            lookupName = rawText.toString().toLowerCase();

            ArrayList<String> templist = (ArrayList<String>) (pinyinHash.get(lookupName.intern()));

            if (templist == null) {
            	return;// throw exception?
            }

            //System.out.println(lookupName + " " + templist.size()) ;
            lookupCandidates = new String[templist.size()];

            for (int k = 0; k < lookupCandidates.length; k++) {
                lookupCandidates[k] = (String) templist.get(k);
            }

            if (lookupCandidates != null) {

                if (lookupList != null) {
                    lookupList.setVisible(false);
                    lookupList = null;
                }

                openLookupWindow();
            }
        } else {
            text = "";
            highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
            closeLookupWindow();
        }

        AttributedString as = new AttributedString(text);

        if (committed) {
            committedCharacterCount = text.length();
        } else if (text.length() > 0) {
            as.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, highlight);
        }

        inputMethodContext.dispatchInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
            as.getIterator(), committedCharacterCount,
            TextHitInfo.leading(text.length()), null);
    }

    /**
     * Looks up and selects the candidate.
     *
     * @param candidate 
     */
    private final void selectCandidate(int candidate) {
    	if (candidate < 0 || candidate >= lookupCandidates.length) {
    		return;// throw exception
    	}
        convertedText = lookupCandidates[candidate];
        converted = true;
        sendText(false);
    }

    /**
     * Creates and opens the lookup window.
     */
    private final void openLookupWindow() {
    	lookupList = new LookupListPanel(this, inputMethodContext, lookupCandidates, editorFont);
        lookupList.selectCandidate(0);// TODO select the most frequently used (at least for the single stroke input methods)
    }

    /**
     * Closes the lookup list.
     */
    private final void closeLookupWindow() {
        if (lookupList != null) {
            lookupList.setVisible(false);
            lookupList = null;
            editorFont = null;
        }
    }

    /**
     * Makes the lookup window visible.
     */
    private void showLookupWindow() {
        if (lookupList != null) {
            lookupList.setVisible(true);
        }
    }

    /**
     * @see java.awt.im.spi.InputMethod#activate
     */
    @Override
	public void activate() {
        showLookupWindow();
    }

    /**
     * HS 05-feb-2004 On deactivation the editing is canceled. This prevents
     * all kinds of exceptions that occur when the user switches to another
     * application while the LookupList is open.  The argument
     * <code>isTemporary</code> is ignored; the editing is always canceled.
     *
     * @param isTemporary ignored
     *
     * @see java.awt.im.spi.InputMethod#deactivate
     * @see cancelEdit
     */
    @Override
	public void deactivate(boolean isTemporary) {
        cancelEdit();
    }

    /**
     * @see java.awt.im.spi.InputMethod#dispatchEvent
     */
    @Override
	public void dispatchEvent(AWTEvent event) {
    	// start with a check on the locale: on Mac OS X the window is shown again (dispatchEvent is called)
    	// after changing the locale back to the default and even if the main application window
    	// has the focus
    	if (locale == null) {
    		return;
    	}
        if (event instanceof KeyEvent) {
            //System.out.println("KE id: " + ((KeyEvent) event).getID() + " char: " + ((KeyEvent) event).getKeyChar() 
            //		+ " code: " + ((KeyEvent) event).getKeyCode());
        	if (editorFont == null) {
        		if (event.getSource() instanceof Component)
        		editorFont = ((Component) event.getSource()).getFont();
        	}
            
            switch (((KeyEvent) event).getID()) {
            case KeyEvent.KEY_TYPED:
                this.handleTypedKey((KeyEvent) event);

                break;

            case KeyEvent.KEY_PRESSED:
                this.handlePressedKey((KeyEvent) event);

                break;
            }

        }

        if (event instanceof MouseEvent) {
            // MOUSE_PRESSED results in hiding the window...
            // I don't see MOUSE_PRESSED here...
            // a lot of work to do.
            //HS 03-feb-2004 they come in, when the inline edit box is "attached"
            MouseEvent mevent = (MouseEvent) event;

            if (mevent.getID() == MouseEvent.MOUSE_PRESSED) {
            	if (lookupList != null) {
            		int y = mevent.getY();
            		int index = lookupList.getSelectedCandidateIndex(y);
            		if (index >= 0 && index < lookupCandidates.length) {
	                    convertedText = lookupCandidates[index];
	                    converted = true;
	                    sendText(false);
	                    mevent.consume();
	                    commit();
            		} else {
            			mevent.consume();
            		}
            	}
            }
        }
    }

    /**
     * @see java.awt.im.spi.InputMethod#dispose
     */
    @Override
	public void dispose() {
        hideWindows();
    }

    /**
     * @see java.awt.im.spi.InputMethod#endComposition
     */
    @Override
	public void endComposition() {
        if (this.rawText.length() != 0) {
            this.commit(); // TODO do we want that?
        }

        hideWindows();
    }

    /**
     * @return null
     * @see java.awt.im.spi.InputMethod#getControlObject
     */
    @Override
	public Object getControlObject() {
        return null;
    }

    /**
     * @see java.awt.im.spi.InputMethod#getLocale
     */
    @Override
	public Locale getLocale() {
        return this.locale;
    }

    /**
     * @see java.awt.im.spi.InputMethod#hideWindows
     */
    @Override
	public void hideWindows() {
        this.closeLookupWindow();
    }

    /**
     * @see java.awt.im.spi.InputMethod#isCompositionEnabled
     */
    @Override
	public boolean isCompositionEnabled() {
        return true;
    }

    /**
     * @see java.awt.im.spi.InputMethod#notifyClientWindowChange
     */
    @Override
	public void notifyClientWindowChange(Rectangle location) {
    	//System.out.println("Window change: " + location);
    }

    /**
     * @see java.awt.im.spi.InputMethod#reconvert
     */
    @Override
	public void reconvert() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.awt.im.spi.InputMethod#removeNotify
     */
    @Override
	public void removeNotify() {
    }

    /**
     * @see java.awt.im.spi.InputMethod#setCharacterSubsets
     */
    @Override
	public void setCharacterSubsets(Character.Subset[] subsets) {
    }

    /**
     * @see java.awt.im.spi.InputMethod#setCompositionEnabled
     */
    @Override
	public void setCompositionEnabled(boolean enable) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.awt.im.spi.InputMethod#setInputMethodContext
     */
    @Override
	public void setInputMethodContext(InputMethodContext context) {
    	//System.out.println("Sets the context...");
        this.inputMethodContext = context;
    }

    /**
     * @see java.awt.im.spi.InputMethod#setLocale
     */
    @Override
	public boolean setLocale(Locale locale) {
        //System.out.println("Lookup.java: request for " + locale);
        //System.out.println("Lookup.java: was  " + this.locale);
        if (locale == null) {
        	this.locale = null;
            return false;
        }

        if (locale == this.locale) {
            return true;
        }

        if (!hashedFilenames.containsKey(locale)) {
        	this.locale = null;
            return false;
        }

        try {
            initializeHash(locale);
            this.closeLookupWindow();
            this.locale = locale;

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
