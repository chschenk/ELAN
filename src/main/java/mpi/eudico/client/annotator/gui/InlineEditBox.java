package mpi.eudico.client.annotator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.InlineEditBoxListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayer;
import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerHost;
import mpi.eudico.client.annotator.spellcheck.SpellChecker;
import mpi.eudico.client.annotator.spellcheck.SpellCheckerRegistry;
import mpi.eudico.client.annotator.viewer.StyledHighlightPainter;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.client.im.ImUtil;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.Pair;
import mpi.eudico.util.SimpleCVEntry;


/**
 * A class that provides and configures user interface components for  editing
 * annotation values. Elan Viewer components that offer the possibility of
 * editing annotation values can use this class to get a suitable editor
 * component. Depending on the language (locale) of the annotation it will
 * activate  special input method components.
 *
 * @author MPI
 * @version jun 2004 additions related to the use of controlled vocabularies
 * @version sep 2005 when an annotation's Locale is the system default and the 
 * the edit box's is also the system default, the language isn't set anymore.
 * This way it is possible to use other system specific im's / keyboards on 
 * MacOS as well.
 */
@SuppressWarnings("serial")
public class InlineEditBox extends JPanel implements ActionListener,
    MouseListener, MenuListener, KeyListener, ElanLocaleListener, DocumentListener {
    /** action command constant */
    private static final String EDIT_MENU_DET = "Detach Editor";

    /** action command constant */
    private static final String EDIT_MENU_ATT = "Attach Editor";

    /** action command constant */
    private static final String EDIT_MENU_CMT = "Commit Changes";

    /** action command constant */
    private static final String EDIT_MENU_CNL = "Cancel Changes";

    /** the textarea in use when the editor is used in attached mode */
    final private JTextArea textArea = new JTextArea("", 2, 1);

    /** the scrollpane for the attached-mode textarea */
    final private JScrollPane textAreaScrollPane = new JScrollPane(textArea);

    /** the textarea in use when the editor is used in detached mode */
    final private JTextArea exttextArea = new JTextArea("", 2, 1);

    /** the scrollpane for the detached-mode textarea */
    final private JScrollPane exttextAreaScrollPane = new JScrollPane(exttextArea); 

    /**
     * a focus listener that lets the attached-mode textarea request the
     * keyboard focus
     */
    final private FocusListener intFocusListener = new FocusAdapter() {
            @Override
			public void focusGained(FocusEvent e) {
                if (!isUsingControlledVocabulary) {
                    textArea.requestFocus();
                    textArea.getCaret().setVisible(true);
                } else {
                    if (cvEntryComp != null) {
                        cvEntryComp.grabFocus();
                    }
                }
            }
            
            @Override
			public void focusLost(FocusEvent e) {
				if (!isEditing) {
					transferFocusUpCycle();
				}
            }
        };

    /**
     * a focus listener that lets the detached-mode textarea request the
     * keyboard focus
     */
    final private FocusListener extFocusListener = new FocusAdapter() {
            @Override
			public void focusGained(FocusEvent e) {
                if (!isUsingControlledVocabulary) {
                    exttextArea.requestFocus();
                    exttextArea.getCaret().setVisible(true);
                } else {
                    if (cvEntryComp != null) {
                        cvEntryComp.grabFocus();
                    }
                }
            }
            
			@Override
			public void focusLost(FocusEvent e) {
				//transferFocusUpCycle();
			}
        };

    private JPopupMenu popupMenu = new JPopupMenu("Select Language");
    
    private final static int EDIT_COMMITTED = 0;
    private final static int EDIT_CANCELED = 1;
//    private final static int EDIT_INTERUPPTED = 2;
    
    private InlineEditBoxListener listener;
    
    private JDialog externalDialog = null;
    private Rectangle dialogBounds;
    private Locale[] allLocales;
    private int numberOfLocales;
    private String oldText;
    private boolean attached = true;
    private Annotation annotation;
    private Point position;   
    private Locale annotationLocale;
    private boolean attachable;
    private boolean isUsingControlledVocabulary = false;
    private  boolean showCVDescription = true;

    // fields for Locale changes
    private JMenu editMenu;
    private JMenu editorMenu;
    private JMenu selectLanguageMenu;
    private JMenuItem attachMI;
    private JMenuItem commitMI;
    private JMenuItem cancelMI;
    private JMenuItem closeMI;
    private JMenuItem detachPUMI;
    private JMenuItem commitPUMI;
    private JMenuItem cancelPUMI;
	private JMenuItem selectAllPUMI;
    private JMenuItem cutMI;
    private JMenuItem copyMI;
    private JMenuItem pasteMI;
    private JMenuItem cutPUMI;
    private JMenuItem copyPUMI;
    private JMenuItem pastePUMI;
    private JMenuItem selectAllMI;
    private JMenuBar menuBar;
    private JMenuItem toggleSuggestMI;

    /** a JList in a scrollpane */
    private CVEntryComponent cvEntryComp;
    private int minCVWidth = 300;
    private int minCVHeight = 120;
    private int inlineBoxWidth;
    private int firstColPercentage = 30;
    
    private List<KeyStroke> keyStrokesNotToBeConsumed = new ArrayList<KeyStroke>();
    private List<KeyStroke> defaultRegisteredKeyStrokes = new ArrayList<KeyStroke>();
    /**
     * this field can be either a JPanel (this), a JScrollPane, a JTextArea , a
     * JComboBox or any other component that can be added to the layout of a
     * viewer (component)
     */
    private JComponent editorComponent;

    //temp
    private Font uniFont = Constants.DEFAULTFONT;
    
    private boolean isEditing = false;
    private boolean enterCommits = true;// May 2013 changed (historic) default to true
    
    private String oriValue;
    private int cursorPos;
    
    private boolean restoreOriValue = false;

	private StyledHighlightPainter spellingErrorPainter;

	private ArrayList<Object> spellingErrorHighLightInfos;
	private MediaDisplayerHost mediaDisplayerHost;
    
    /**
     * When this editor is not created by a viewer, it will always be created
     * as a  "detached" dialog.
     *
     * @param attachable whether or not this editor can be attached to a
     *        viewer component.
     */
    public InlineEditBox(boolean attachable) {
        init();

        this.attachable = attachable;
    }

    /**
     * Creates a new InlineEditBox instance
     */
    public InlineEditBox() {
        init();
        attached = false;
    }    
 
    public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksList){    	
    	keyStrokesNotToBeConsumed.clear();
    	keyStrokesNotToBeConsumed.addAll(ksList);
    }
    
    /**
     * DOCUMENT ME!
     */
    public void init() {
        KeyStroke[] kss = textArea.getRegisteredKeyStrokes();
        for(KeyStroke ks : kss) {
        	ActionListener al = textArea.getActionForKeyStroke(ks);
        	if (al != null) {
        		defaultRegisteredKeyStrokes.add(ks);
        	}
        }
        
    	Boolean val = Preferences.getBool("InlineEdit.EnterCommits", null);

        if (val != null) {
            enterCommits = val.booleanValue();
        }
        
        attachable = true;
        setLayout(new BorderLayout());

        try {
            allLocales = ImUtil.getLanguages();
            numberOfLocales = (allLocales == null) ? 0 : allLocales.length;
        } catch (java.lang.NoSuchMethodError nsme) {
            // The SPI extensions have not been present at startup.
            //String msg = "Setup incomplete: you won't be able to set languages for editing.";
            String msg = ElanLocale.getString("InlineEditBox.Message.SPI") +
                "\n" + ElanLocale.getString("InlineEditBox.Message.SPI2");
            JOptionPane.showMessageDialog(null, msg, null,
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception exc) {
            LOG.warning("InlineEditBox::init::ParentIMBug::FIXME");
            LOG.warning(exc.getMessage());
        }

        textArea.addMouseListener(this);
        textArea.setLineWrap(false);
        textAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        add(textAreaScrollPane, BorderLayout.CENTER);
        textArea.getCaret().setVisible(true);
        textArea.addKeyListener(this);
        textArea.getDocument().addDocumentListener(this);
        textArea.addFocusListener(new FocusAdapter() {
                @Override
				public void focusGained(FocusEvent e) {                	
                	if (annotationLocale != null && !annotationLocale.equals(Locale.getDefault()) && 
                			!annotationLocale.equals(textArea.getLocale())) {
                		ImUtil.setLanguage(textArea, annotationLocale);
                		textArea.setFont(uniFont);
                	}
                }
				@Override
				public void focusLost(FocusEvent e) {				
					if (!isEditing) {
						transferFocusUpCycle();
					}
//					else{						
//						if(!popupMenu.isVisible() && attached && 
//							FrameManager.getInstance().getActiveFrame().isFocused() ){
//							
//							notifyListener(EDIT_INTERUPPTED);	
//						}
//					}			
				}
            });

        exttextArea.setLineWrap(true);
        exttextArea.setWrapStyleWord(true);
        exttextArea.addKeyListener(this);
        exttextArea.getDocument().addDocumentListener(this);
        exttextArea.addFocusListener(new FocusAdapter() {
                @Override
				public void focusGained(FocusEvent e) {
					if (annotationLocale != null && !annotationLocale.equals(Locale.getDefault()) && 
							!annotationLocale.equals(exttextArea.getLocale())) {
                    	ImUtil.setLanguage(exttextArea, annotationLocale);
                    	exttextArea.setFont(uniFont.deriveFont(20.0f));
					}
                }
				@Override
				public void focusLost(FocusEvent e) {
					if (!isEditing) {
						transferFocusUpCycle();
					}					
				}
            });

        createPopupMenu();

        exttextAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        exttextAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        textAreaScrollPane.addFocusListener(intFocusListener);
        addFocusListener(intFocusListener);
    }
    
    /**
     * Adda a inlineEditListener
     */
    public void addInlineEditBoxListener(InlineEditBoxListener inLineListener){
    	listener = inLineListener;
    }
    
    /**
     * Removes the inlineEditBoxListener
     */
    public void removeInlineEditBoxListener(InlineEditBoxListener inLineListener){
    	listener = null;
    }
    
    /**
     * Notifies the listener about the change
     */
    private void notifyListener(int edit_Type){
    	if(listener != null){
    		switch(edit_Type){
    		case EDIT_COMMITTED:
    			listener.editingCommitted();
    			break;
    		case EDIT_CANCELED:
    			listener.editingCancelled();
    			break;
//    		case EDIT_INTERUPPTED:
//    			listener.editingInterrupted();
//    			break;
    		}
    	}
    }

    /**
     * Creates a modal JDialog when editing is done in detached mode.
     */
    public void createExternalDialog() {
        try {
            externalDialog = new JDialog(ELANCommandFactory.getRootFrame(
                        annotation.getTier().getTranscription()),
                    ElanLocale.getString("InlineEditBox.Title"), true);
            externalDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            externalDialog.addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent event) {
                    cancelEdit();
                 }
            });
            
            if (menuBar == null) {
                createJMenuBar();
            }
            
    		if (isUsingControlledVocabulary) {
            	toggleSuggestMI.setVisible(true);
            } else {
            	toggleSuggestMI.setVisible(false);
            }
    		
            externalDialog.setJMenuBar(menuBar);
            externalDialog.addFocusListener(extFocusListener);
            externalDialog.setSize(300, 300);
        } catch (Exception ex) {
            LOG.warning("Could not create external dialog: " + ex.getMessage());
        }
    }

    /**
     * Creates a popup menu.
     */
    public void createPopupMenu() {
        detachPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Detach"));
        detachPUMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                ActionEvent.SHIFT_MASK));
        detachPUMI.setActionCommand(EDIT_MENU_DET);
        detachPUMI.addActionListener(this);
        popupMenu.add(detachPUMI);
        commitPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Commit"));
        commitPUMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        commitPUMI.setActionCommand(EDIT_MENU_CMT);
        commitPUMI.addActionListener(this);
        popupMenu.add(commitPUMI);
        cancelPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Cancel"));
        cancelPUMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        cancelPUMI.setActionCommand(EDIT_MENU_CNL);
        cancelPUMI.addActionListener(this);
        popupMenu.add(cancelPUMI);

        popupMenu.addSeparator();

        cutPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Cut"));
        cutPUMI.setActionCommand("cut");
        cutPUMI.addActionListener(this);
        popupMenu.add(cutPUMI);
        copyPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        copyPUMI.setActionCommand("copy");
        copyPUMI.addActionListener(this);
        popupMenu.add(copyPUMI);
        pastePUMI = new JMenuItem(ElanLocale.getString(
                    "InlineEditBox.Edit.Paste"));
        pastePUMI.setActionCommand("paste");
        pastePUMI.addActionListener(this);
        popupMenu.add(pastePUMI);
        selectAllPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.SelectAll"));
        selectAllPUMI.setActionCommand("selectAll");
        selectAllPUMI.addActionListener(this);
        popupMenu.add(selectAllPUMI);

        popupMenu.addSeparator();

        JMenuItem newItem;

        for (int i = 0; i < numberOfLocales; i++) {
            if (i == 0 && allLocales[i] == Locale.getDefault()) {
                newItem = new JMenuItem(allLocales[i].getDisplayName() + " (System default)");
                newItem.setActionCommand(allLocales[i].getDisplayName());
            } else {
                newItem = new JMenuItem(allLocales[i].getDisplayName());    
            }
            
            popupMenu.add(newItem);
            newItem.addActionListener(this);
        }
    }

    private JMenuBar createJMenuBar() {
        menuBar = new JMenuBar();
        editorMenu = new JMenu(ElanLocale.getString("InlineEditBox.Menu.Editor"));
        editMenu = new JMenu(ElanLocale.getString("Menu.Edit"));
        editMenu.addMenuListener(this);
        selectLanguageMenu = new JMenu(ElanLocale.getString(
                    "InlineEditBox.Menu.Select"));

        if (attachable == true) {
            attachMI = new JMenuItem(ElanLocale.getString(
                        "InlineEditBox.Attach"));
            attachMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                    ActionEvent.SHIFT_MASK));
            attachMI.setActionCommand(EDIT_MENU_ATT);
            attachMI.addActionListener(this);
            editorMenu.add(attachMI);
        }

        commitMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Commit"));
        commitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        commitMI.setActionCommand(EDIT_MENU_CMT);
        commitMI.addActionListener(this);
        editorMenu.add(commitMI);
        cancelMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Cancel"));
        cancelMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        cancelMI.setActionCommand(EDIT_MENU_CNL);
        cancelMI.addActionListener(this);
        editorMenu.add(cancelMI);

        closeMI = new JMenuItem(ElanLocale.getString("Button.Close"));
        closeMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
    			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        closeMI.setActionCommand("close");
        closeMI.addActionListener(this);
        editorMenu.add(closeMI);
        
		// Menu item for toggling the suggest panel
        editorMenu.add(new JSeparator());
        toggleSuggestMI = new JMenuItem(ElanLocale
        		.getString("InlineEditBox.ToggleSuggestPanel"));
        toggleSuggestMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        toggleSuggestMI.setActionCommand("toggleSuggest");
        toggleSuggestMI.addActionListener(this);
        editorMenu.add(toggleSuggestMI);
        
        cutMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Cut"));
        cutMI.setActionCommand("cut");
        cutMI.addActionListener(this);
        editMenu.add(cutMI);
        copyMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        copyMI.setActionCommand("copy");
        copyMI.addActionListener(this);
        editMenu.add(copyMI);
        pasteMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Paste"));
        pasteMI.setActionCommand("paste");
        pasteMI.addActionListener(this);
        editMenu.add(pasteMI);
        
        selectAllMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.SelectAll"));
        selectAllMI.setActionCommand("selectAll");
        selectAllMI.addActionListener(this);
        editMenu.add(selectAllMI);

        JMenuItem newItem;

        for (int i = 0; i < numberOfLocales; i++) {
            newItem = new JMenuItem(allLocales[i].getDisplayName());
            selectLanguageMenu.add(newItem);
            newItem.addActionListener(this);
        }

        menuBar.add(editorMenu);
        menuBar.add(editMenu);
        menuBar.add(selectLanguageMenu);

        return menuBar;
    }

    /**
     * Returns whether or not the current annotation's value is restricted by a
     * ControlledVocabulary.
     *
     * @return true if a CV has to be used, false otherwise
     */
    public boolean isUsingControlledVocabulary() {
        return isUsingControlledVocabulary;
    }

    /**
     * Overrides setVisible(boolean) in JComponent.
     *
     * @param vis the visibility value
     */
    @Override
	public void setVisible(boolean vis) {
        super.setVisible(vis);

        if (externalDialog != null) {
            externalDialog.setVisible(vis);
        }

        if (vis == false) {
            //closeIM();
            setLocation(-10, -10);
           
        }
    }

    /**
     * Overrides setSize(Dimension) in Component by performing a check to
     * guarantee a minimal  size.
     * 
     * @param d the requested size
     */
    @Override
	public void setSize(Dimension d) {
        // set useable sizes for edit region
        if (d.getWidth() < 60) {
            d = new Dimension(60, d.height);
        }

        if (d.getHeight() < 38) {
            d = new Dimension(d.width, 38);
        }

        //AK 16/08/2002 unfortunately all those setSizes are somewhere necessary
        super.setSize(d);
        setPreferredSize(d);
        textAreaScrollPane.setPreferredSize(d);
        textAreaScrollPane.setSize(d);
    }

    /**
     * Overrides setSize(Dimension) in Component by performing no check to
     * guarantee a minimal  size.
     * TODO check the other components whether they should use the size
     *  
     * @param d the requested size
     */
    public void setSizeIgnoreMinimum(Dimension d) {
        // set sizes for edit region

        super.setSize(d);
        setPreferredSize(d);
        textAreaScrollPane.setPreferredSize(d);
        textAreaScrollPane.setSize(d);
        //other editor components
    }
    /**
     * Overrides setFont(Font) in Component by also setting the font for the
     * textareas.
     *
     * @param font the Font to use
     */
    @Override
	public void setFont(Font font) {
        super.setFont(font);
        uniFont = font;

        // setFont() is used at intializing superclass - textarea not yet instantiated
        if (textArea != null) {
            textArea.setFont(font);
        }

        if (exttextArea != null) {
            exttextArea.setFont(font);
        }
    }
    
    private FontMetrics getFontMetrics() {
        if (textArea != null) {
            return textArea.getFontMetrics(textArea.getFont());
        }

        if (exttextArea != null) {
            return exttextArea.getFontMetrics(exttextArea.getFont());
        }
        
        return super.getFontMetrics(uniFont);
    }
    
    /**
     * Sets the margin insets for the text area and the detached text area.
     * TODO check whether the other editor components should use the margin as well
     * 
     * @param insets the margin insets
     */
    public void setMargin(Insets insets) {
        if (textArea != null) {
            textArea.setMargin(insets);
        }

        if (exttextArea != null) {
            exttextArea.setMargin(insets);
        }
    }
    
    /**
     * Sets the border (typically to null) for the text editing component and the scrollpane it is in.
     * 
     * @param border the new border for the text areas and the scrollpanes they are in
     */
    @Override
	public void setBorder(Border border) {
        if (textArea != null) {
            textArea.setBorder(border);
            textAreaScrollPane.setBorder(border);
        }

        if (exttextArea != null) {
            exttextArea.setBorder(border);
            exttextAreaScrollPane.setBorder(border);
        }	
    }
    
    /**
     * TEMP!!
     * A method to get the width of the current textual contents of the (attached) text area.
     * For testing "live" resizing while typing.   
     * 
     * @return the rendered width of the current text content
     */
    public int getCurrentTextWidth() {
    	if (attached) {
    		if (!isUsingControlledVocabulary) {
	    		if (textArea != null) {
	    			return textArea.getFontMetrics(textArea.getFont()).stringWidth(textArea.getText());
	    		}
    		}
    	}
    	
    	return 60;
    }
    
    @Override
	public synchronized void addKeyListener(KeyListener kl) {
        if (textArea != null) {
            textArea.addKeyListener(kl);
        }
        
        if (exttextArea != null) {
            exttextArea.addKeyListener(kl);
        }
        
		super.addKeyListener(kl);
	}

	@Override
	public synchronized void removeKeyListener(KeyListener kl) {
        if (textArea != null) {
            textArea.removeKeyListener(kl);
        }
        
        if (exttextArea != null) {
            exttextArea.removeKeyListener(kl);
        }
		super.removeKeyListener(kl);
	}

	/**
     * Sets the annotation that is to be edited. 
     * When <code>forceOpenCV</code> is true an 'open' text edit box will be used
     * even if the linguistic type has an associated ControlledVocabulery.
     * 
     * @param ann the annotation to be edited
     * @param forceOpenCV if true the associated CV will be ignored, 
     * editing will be open
     */
    public void setAnnotation (Annotation ann, boolean forceOpenCV) {
		annotation = ann;
		oldText = ann.getValue();
		//textArea.setText(oldText.trim());
		textArea.setText(oldText);// don't trim, otherwise it's difficult to remove spaces newlines etc.

		try {
			annotationLocale = ((TierImpl) annotation.getTier()).getDefaultLocale();
			if (forceOpenCV) {
				isUsingControlledVocabulary = false;
			} else {			
				isUsingControlledVocabulary = ((TierImpl) annotation.getTier()).getLinguisticType()
												   .isUsingControlledVocabulary();
			}
		} catch (Exception e) {
			LOG.warning(
				"Could not establish Default Language of Tier. Using System Default instead.");
			annotationLocale = null;//??
			isUsingControlledVocabulary = false;
		}

		if (attached) {
			if (!isUsingControlledVocabulary) {
				textArea.setEditable(true);
				textArea.setCaretPosition(textArea.getText().length());

				//textArea.requestFocus();
			} else {
				textArea.setEditable(false);

				if (cvEntryComp == null) {
					cvEntryComp = new CVEntryComponent(JScrollPane.class);
				}

				cvEntryComp.setAnnotation(annotation);
			}
		}
		
		paintSpellErrorUnderline(textArea);
    }

    /**
     * Sets the annotation that is to be edited.
     *
     * @param ann the annotation to be edited
     */
    public void setAnnotation(Annotation ann) {
		setAnnotation(ann, false);
    }

    /**
     * Checks whether the annotation's value has been edited.
     *
     * @return true if the annotation's value has been edited, false otherwise
     */
    public boolean annotationModified() {
        return attached ? (!oldText.equals(textArea.getText()))
                        : (!oldText.equals(exttextArea.getText()));
    }
    
    /**
     * Paints the squiggly line underneath spell errors in the text area
     * @param textAreaToCheck
     */
    private void paintSpellErrorUnderline(final JTextArea textAreaToCheck) {
    	SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
		    	String langRef = ((TierImpl) annotation.getTier()).getLangRef();
				SpellChecker checker = SpellCheckerRegistry.getInstance().getSpellChecker(langRef);
				String[] textElements = textAreaToCheck.getText().split("\\b");
				Highlighter highlighter = textAreaToCheck.getHighlighter();
				
				if(spellingErrorPainter == null) {
					spellingErrorPainter = new StyledHighlightPainter(Color.RED, 0, StyledHighlightPainter.SQUIGGLED);
			        spellingErrorPainter.setVisible(false);
				}
				
				if(spellingErrorHighLightInfos == null) {
					spellingErrorHighLightInfos =  new ArrayList<Object>();
				} else {
					// Clean up 
					for (Object obj : spellingErrorHighLightInfos) {
			        	highlighter.removeHighlight(obj);
			        }
			        spellingErrorHighLightInfos.clear();
				}
				
				// Underline individual words
		        int indexWordBegin = 0; // Indicates the begin index of the current word
				for(int elementIndex = 0; elementIndex < textElements.length; elementIndex++) {
					int elementLength = textElements[elementIndex].length();
					
					if(textElements[elementIndex].matches(".*\\p{L}.*")) { 
						if(!checker.isCorrect(textElements[elementIndex])) {
							int start = indexWordBegin;
				    		int end = start + elementLength;
				            Object hl = highlighter.addHighlight(start,	end, spellingErrorPainter);
				            spellingErrorHighLightInfos.add(hl);
						}
					}
					indexWordBegin += elementLength;
				}
				spellingErrorPainter.setVisible(true);
				return null;
			}
			
			@Override
			public void done() {
				textAreaToCheck.getParent().repaint();
				textAreaToCheck.getParent().validate();
			}
    	};
    	
    	worker.execute();
    }

    /**
     * Returns true if the internal TextArea is open
     *
     * @return true if the component is attached to a viewer's layout
     */
    public boolean isAttached() {
        return attached;
    }

    /**
     * Sets internal TextArea (resp. "this") visible(false) and opens external
     * TextArea (resp. "externalDialog")
     */
    public void detachEditor() {
        if (attachable && !attached) {
            return;
        }

        attached = false;
        position = getLocation();
        createExternalDialog();

        if (dialogBounds != null) {
            externalDialog.setBounds(dialogBounds);
        } else {
        	Rectangle boundsPref = Preferences.getRect("DetachedEditor.Bounds", getCurrentTranscription());
        	if (boundsPref != null) {
        		externalDialog.setBounds(boundsPref);
        	} else {
	            Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
	            Rectangle frameDim = externalDialog.getBounds();
	            externalDialog.setLocation((screenDim.width - frameDim.width) / 2,
	                (screenDim.height - frameDim.height) / 2);
        	}
        }

        ImUtil.setLanguage(textArea, Locale.getDefault());
        setVisible(false);
        
        if (!isUsingControlledVocabulary) {
            externalDialog.getContentPane().removeAll();
            externalDialog.getContentPane().add(exttextAreaScrollPane);
            exttextArea.setEditable(true);
            exttextArea.setText(textArea.getText());
            exttextArea.setCaretPosition(exttextArea.getText().length());
            exttextArea.setFont(textArea.getFont().deriveFont(20.0f));
        } else {
            if (cvEntryComp == null) {
                cvEntryComp = new CVEntryComponent(JScrollPane.class);
                cvEntryComp.setAnnotation(annotation);
            } else {
                cvEntryComp.setAnnotation(annotation);
                //cvEntryComp.setDelegate(JScrollPane.class);
            }

            exttextArea.setEditable(false);
            cvEntryComp.removePopupListener();
            cvEntryComp.setFont(getFont());
            externalDialog.getContentPane().removeAll();
            externalDialog.getContentPane().add(cvEntryComp.getEditorComponent());
        }

        paintSpellErrorUnderline(exttextArea);
        externalDialog.setVisible(true);
    }

    /**
     * Returns the editor the editor to the previous attached state and dispose
     * the external dialog.
     */
    protected void attachEditor() {
        attached = true;
        dialogBounds = externalDialog.getBounds();
        Preferences.set("DetachedEditor.Bounds", dialogBounds, getCurrentTranscription());
        externalDialog.dispose();
        externalDialog = null;
        
        setLocation(position);
        setVisible(true);

        if (!isUsingControlledVocabulary) {
            textArea.setText(exttextArea.getText());
            textArea.requestFocus();
        } else {
            cvEntryComp.addPopupListener();

            if (editorComponent == this) {
                removeAll();
                add(cvEntryComp.getEditorComponent(), BorderLayout.CENTER);
            }

            startEdit();
        }
    }

    /**
     * Resets elements and cleans up without applying any changes in the  value
     * of the annotation.  HB, 11 oct 01, changed from protected to public
     */
    public void cancelEdit() {
    	isEditing = false;
        closeIM();
        
        if(mediaDisplayerHost != null) {
        	mediaDisplayerHost.discardMediaDisplayer();
    	}

        if (attached) {
            setVisible(false);
        } else {
            if (externalDialog != null) {
                dialogBounds = externalDialog.getBounds();
                Preferences.set("DetachedEditor.Bounds", dialogBounds, getCurrentTranscription());
                setVisible(false);
                externalDialog.dispose();
                externalDialog = null;
            }

            attached = true;
        }
       
        notifyListener(EDIT_CANCELED);	
    }

    /**
     * Checks for modifications, applies the modification if any, resets and
     * cleans  up a bit.
     */
    public void commitEdit() {
		isEditing = false;
        closeIM();

        if(mediaDisplayerHost != null) {
        	mediaDisplayerHost.discardMediaDisplayer();
        }
        
        ExternalReference extRef = null;
        String cveId = null;
        
        if (isUsingControlledVocabulary && (cvEntryComp != null)) {
        	final SimpleCVEntry selectedEntry = cvEntryComp.getSelectedEntry();
			if (selectedEntry != null) {
	            if (attached) {
	                textArea.setText(cvEntryComp.getSelectedEntryValue());
	            } else {
	                exttextArea.setText(cvEntryComp.getSelectedEntryValue());
	            }
	            extRef = selectedEntry.getExternalRef();
	        	cveId = selectedEntry.getId();
	        	//System.out.printf("InlineEditBox.commitEdit: cveId = '%s' annotation = %s\n", cveId, annotation);
        	}
        } 

        // remove an ExternalCV reference by passing a null value.
        if (extRef == null && ((AbstractAnnotation) annotation).getExtRef() != null) {
        	extRef = new ExternalReferenceImpl(null, ExternalReference.CVE_ID);
        }
        
        String newText = "";
        boolean modified = annotationModified();

        if (attached) {
            if (modified) {
                newText = textArea.getText();
            }

            setVisible(false);
        } else {
            if (modified) {
                newText = exttextArea.getText();
            }

            dialogBounds = externalDialog.getBounds();
            Preferences.set("DetachedEditor.Bounds", dialogBounds, getCurrentTranscription());
            setVisible(false);
            externalDialog.dispose();
            externalDialog = null;
            attached = true;
        }

        if (modified) {
            /* 
             * Check for initial characters that should not be initial.
             * Those are the Mn, Mc, Me class characters.
             * This is not perfect, since Java 1.6 knows only about Unicode 4.0,
             * and more such characters have been added later.
             * It is also something that one expects that the text area
             * already would do correctly.
             */
    		if (newText.length() > 0) {
    			int codePoint = newText.codePointAt(0);
    			if (Character.isValidCodePoint(codePoint)) {
	    			int type = Character.getType(codePoint);
	    			boolean bad = false;
	    			if (type == Character.UNASSIGNED) {
	    				// This Java version doesn't know this character... oops!
	    				// Try to extract some useful information from the font.
	    				if (uniFont.canDisplay(codePoint)) {
		    				FontMetrics fm = getFontMetrics();
		    				int width = fm.charWidth(codePoint);
							// This trick would fail on some legitimate characters,
							// such as a zero-width-space, but it would not be used
							// for those, unless new ones are added to Unicode.
		    				bad = (width <= 0);
	    				} else {
	    					// If "the font" has no glyph for this code point,
	    					// we still don't know what category code point this is.
	    					// Fortunately, "the font" seems to be an amalgam of
	    					// various available fonts.
	    					bad = true;	// be pessimistic
	    				}
	    			} else {
	    				bad = (type == Character.NON_SPACING_MARK ||       // Mn
	    					   type == Character.COMBINING_SPACING_MARK || // Mc
	    				       type == Character.ENCLOSING_MARK);          // Me
	    			}
	    			if (bad) {
	    				// Perhaps we should let the user know...
	    				newText = " " + newText;
	    				JOptionPane.showMessageDialog(this,
	    						ElanLocale.getString("InlineEditBox.StartWithModifyingCharacter"),
	    						// "Text started with a character that is meant to modify the one before it.\n"
	    						// + "A space has been prepended.",
	    						null, JOptionPane.INFORMATION_MESSAGE);
	    			}
    			}
    		}

    		Command c = ELANCommandFactory.createCommand((annotation.getTier().getTranscription()),
                        ELANCommandFactory.MODIFY_ANNOTATION);
            Object[] args = new Object[] { oldText, newText, extRef, cveId };
            c.execute(annotation, args);
        }
       
        notifyListener(EDIT_COMMITTED);		
    }

    /**
     * @return the Transcription the current annotation is part of, or null
     */
    private Transcription getCurrentTranscription() {
    	if (annotation != null) {
    		return annotation.getTier().getTranscription();
    	}
    	return null;
    }

    /**
     * Restores the default locale (because of InputMethod stuff) of the
     * textareas, unless the language hasn't been changed when the annotation was set.
     */
    private void closeIM() {
    	if (annotationLocale != null) {
	        if (attached) {
	        	if (!textArea.getLocale().equals(Locale.getDefault())) {
					ImUtil.setLanguage(textArea, Locale.getDefault());
	        	}           
	        } else {
				if (!exttextArea.getLocale().equals(Locale.getDefault())) {
	            	ImUtil.setLanguage(exttextArea, Locale.getDefault());
				}
	        }
    	}
    }

    /**
     * Forwards the cut action to either the <code>textArea</code> or the
     * <code>exttextArea</code>, depending on the attached/detached state.
     */
    private void doCut() {
        if (attached) {
            textArea.cut();
        } else {
            exttextArea.cut();
        }
    }

    /**
     * Forwards the copy action to either the <code>textArea</code> or the
     * <code>exttextArea</code>, depending on the attached/detached state.
     */
    private void doCopy() {
        if (attached) {
            textArea.copy();
        } else {
            exttextArea.copy();
        }
    }

    /**
     * Forwards the paste action to either the <code>textArea</code> or the
     * <code>exttextArea</code>, depending on the attached/detached state.
     */
    private void doPaste() {
        if (attached) {
            textArea.paste();
        } else {
            exttextArea.paste();
        }
    }
    
	/**
	 * Forwards the select all action to either the <code>textArea</code> or the
	 * <code>exttextArea</code>, depending on the attached/detached state.
	 */
    private void doSelectAll() {
		if (attached) {
			textArea.selectAll();
		} else {
			exttextArea.selectAll();
		}
    }

    /**
     * Enables/disables edit menu items in the popup menu.<br>
     * Check the contents of the <code>textArea</code> component and the
     * system clipboard.
     */
    private void updatePopup() {   	
        if ((textArea.getSelectedText() == null) ||
                (textArea.getSelectedText().length() == 0)) {
            cutPUMI.setEnabled(false);
            copyPUMI.setEnabled(false);
        } else {
            cutPUMI.setEnabled(true);
            copyPUMI.setEnabled(true);
        }

        if (isTextOnClipboard()) {
            pastePUMI.setEnabled(true);
        } else {
            pastePUMI.setEnabled(false);
        }
        
		if (textArea.getText() == null || 
				textArea.getText().length() == 0) {
			selectAllPUMI.setEnabled(false);
		} else {
			selectAllPUMI.setEnabled(true);
		}
    }

    /**
     * Enables/disables edit menu items in the popup menu.<br>
     * Check the contents of the <code>textArea</code> component and the
     * system clipboard.
     */
    private void updateMenuBar() {
        if ((exttextArea.getSelectedText() == null) ||
                (exttextArea.getSelectedText().length() == 0)) {
            cutMI.setEnabled(false);
            copyMI.setEnabled(false);
        } else {
            cutMI.setEnabled(true);
            copyMI.setEnabled(true);
        }

        if (isTextOnClipboard()) {
            pasteMI.setEnabled(true);
        } else {
            pasteMI.setEnabled(false);
        }
        
		if (exttextArea.getText() == null || 
				exttextArea.getText().length() == 0) {
			selectAllMI.setEnabled(false);
		} else {
			selectAllMI.setEnabled(true);
		}
    }

    /**
     * Checks whether the contents of the system clipboard can be paste into  a
     * textcomponent.
     *
     * @return true if there is contents of type text, false otherwise
     */
    private boolean isTextOnClipboard() {
        Transferable contents = null;

        try {
            contents = Toolkit.getDefaultToolkit().getSystemClipboard()
                              .getContents(this);
        } catch (IllegalStateException ise) {
            LOG.warning("Could not access the system clipboard.");
        }

        if (contents != null) {
            DataFlavor[] flavors = contents.getTransferDataFlavors();
            DataFlavor best = DataFlavor.selectBestTextFlavor(flavors);

            if (best != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the (configured) editor component, ready to be used in a
     * viewer's  layout.
     *
     * @return the editor component
     *
     * @see #configureEditor(Class, Font, Dimension)
     * @see #startEdit()
     */
    public JComponent getEditorComponent() {
        if (editorComponent == null) {
            return this;
        }

        return editorComponent;
    }
    
    private Dimension getCVCompSize(Dimension size){
    	
    	minCVHeight = getFontMetrics(cvEntryComp.getEditorComponent().getFont()).getHeight();
        
    	// default CVWidth reveals up to 18 characters, but this depends on the font etc.
//        int l = cvEntryComp.getMaxEntryLength();
        
        int cvw = minCVWidth;
        
        // if a pref is set
        if(inlineBoxWidth > 0){
        	cvw = inlineBoxWidth;
        	
        	if(showCVDescription){
        		minCVWidth =  (cvw * firstColPercentage)/100;
        	} else {
        		minCVWidth = cvw;
        	}
        } else  {
//        	if (l > 18) {
//        		cvw = (int) ((18f / l) * minCVWidth);
//        	}
//        	cvw = (int) (cvw * 2.5);
//        	inlineBoxWidth = cvw;
        	
        	cvw = minCVWidth;
        	inlineBoxWidth = minCVWidth;
        }
        
        int w;
        int h;

        if (size == null) {
            w = cvw;
            h = minCVHeight;
        } else {
            w = size.width;
            h = size.height;

            if (w < cvw) {
                w = cvw;
            }

            if (h < minCVHeight) {
                h = minCVHeight;
            }
        }
        
        if(cvEntryComp.entries.length > 10){
        	h = 10 * minCVHeight;
        } else {
        	h = cvEntryComp.entries.length * minCVHeight;
        }
        
        return new Dimension(w , h);
    }

    /**
     * Sets up and configures a certain kind of editor component.<br>
     *
     * @param preferredComponent DOCUMENT ME!
     * @param font DOCUMENT ME!
     * @param size DOCUMENT ME!
     *
     * @see #getEditorComponent()
     * @see #startEdit()
     */
    public void configureEditor(Class preferredComponent, Font font,
        Dimension size) {
    	
    	if(preferredComponent == JComboBox.class){
    		showCVDescription = false;
    	} else {
    		Integer intPref = Preferences.getInt("InlineEditBoxWidth", null);
            if (intPref != null) {
            	inlineBoxWidth = intPref.intValue();
            }
            
            intPref = Preferences.getInt("InlineEditBoxCvWidthPercentage", null);
            if (intPref != null) {
            	firstColPercentage = intPref.intValue();
            }
            
            Boolean boolPref = Preferences.getBool("InlineEditBox.ShowCVDescription", null);
            if (boolPref != null) {
            	if (showCVDescription == boolPref.booleanValue()) {
            		// do nothing if the preference is not changed
            		
            	} else {
            		//if the preference is changed... reset the delegate
            		cvEntryComp = null;
                	showCVDescription = boolPref.booleanValue();
            	}
            }
    	}        
    	
        if (preferredComponent == JPanel.class) {
            // configures "this"
            editorComponent = this;

            if (isUsingControlledVocabulary) {
                if (cvEntryComp == null) {
                    cvEntryComp = new CVEntryComponent(JScrollPane.class);
                    cvEntryComp.setAnnotation(annotation);
                } else {
                    if (!(cvEntryComp.getEditorComponent() instanceof JScrollPane)) {
                        cvEntryComp.setDelegate(preferredComponent);
                    }
                }

                if (font != null) {
                    cvEntryComp.setFont(font);
                } else if (getFont() != null){
                	cvEntryComp.setFont(getFont());
                }

                cvEntryComp.addPopupListener();

                Dimension compSize = getCVCompSize(size);
                //cvEntryComp.getEditorComponent().setSize(calulatedSize);
                removeAll();
                add(cvEntryComp.getEditorComponent(), BorderLayout.CENTER);
                if(showCVDescription){
                	cvEntryComp.updateTableSize(compSize);
                }
                
                this.setSize(compSize.width, compSize.height+2*minCVHeight);
                
                validate();
            } else {
                if (font != null) {
                    setFont(font);
                }

                removeAll();
                add(textAreaScrollPane, BorderLayout.CENTER);

                if (size != null) {
                    setSize(size);
                }

                validate();
            }
        } else if (preferredComponent == JScrollPane.class) {
            // configure the scrollpane of either the attached-mode textfield
            // or the CVEntry list
            if (isUsingControlledVocabulary) {
                if (cvEntryComp == null) {
                    cvEntryComp = new CVEntryComponent(preferredComponent);
                    cvEntryComp.setAnnotation(annotation);
                } else {
                    if (!(cvEntryComp.getEditorComponent() instanceof JScrollPane)) {
                        cvEntryComp.setDelegate(preferredComponent);
                    }
                }

                if (font != null) {
                    cvEntryComp.setFont(font);
                } else if (getFont() != null){
                	cvEntryComp.setFont(getFont());
                }

                cvEntryComp.addPopupListener();
                
                Dimension calulatedSize = getCVCompSize(size);
                if (calulatedSize != null) {
                	if(showCVDescription){
                    	cvEntryComp.updateTableSize(calulatedSize);
                	}
                    cvEntryComp.getEditorComponent().setSize(calulatedSize);
                }

                editorComponent = cvEntryComp.getEditorComponent();
            } else {
                if (font != null) {
                    setFont(font);
                }

                if (size != null) {
                    setSize(size);
                }

                editorComponent = textAreaScrollPane;
            }
        } else if (preferredComponent == JComboBox.class) {
            if (isUsingControlledVocabulary) {
                if (cvEntryComp == null) {
                    cvEntryComp = new CVEntryComponent(preferredComponent);
                    cvEntryComp.setAnnotation(annotation);
                } else {
                    if (!(cvEntryComp.getEditorComponent() instanceof JComboBox)) {
                        cvEntryComp.setDelegate(preferredComponent);
                    }
                }

                if (font != null) {
                    cvEntryComp.setFont(font);
                } else if (getFont() != null){
                	cvEntryComp.setFont(getFont());
                }
                
                Dimension calulatedSize = getCVCompSize(size);
                if (calulatedSize != null) {
                	if(showCVDescription){
                    	cvEntryComp.updateTableSize(calulatedSize);
                    }
                    cvEntryComp.getEditorComponent().setSize(calulatedSize);
                }
                
                editorComponent = cvEntryComp.getEditorComponent();
            }
        }
    }

    /**
     * Makes the editorComponent visible and tries to grabFocus.<br>
     * This should be called after configuring and getting the editor
     * component
     *
     * @see #configureEditor(Class, Font, Dimension)
     * @see #getEditorComponent()
     */
    public void startEdit() {
		isEditing = true;
        if (editorComponent == this) {
            setVisible(true);
            requestFocus();
        } else {
            if (isUsingControlledVocabulary) {
                cvEntryComp.grabFocus();
            } else {
                editorComponent.requestFocus();
            }
        }		
    }

    /**
     * Sets the flag that determines that Enter commits without modifier.
     * 
     * @param enterCommits the Enter commits flag
     */
    public void setEnterCommits(boolean enterCommits) {
    	    this.enterCommits = enterCommits;
    }
    
    /**
     * Menu items' ActionPerformed handling.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals(EDIT_MENU_DET)) {
            detachEditor();
        } else if (command.equals(EDIT_MENU_ATT)) {
            if (attachable == true) {
                attachEditor();
            }
        } else if (command.equals(EDIT_MENU_CNL) || command.equals("close")) {
            cancelEdit();
        } else if (command.equals(EDIT_MENU_CMT)) {
            commitEdit();
        } else if (command.equals("cut")) {
            doCut();
        } else if (command.equals("copy")) {
            doCopy();
        } else if (command.equals("paste")) {
            doPaste();
        } else if (command.equals("selectAll")) {
			doSelectAll();
		} else if (command.equals("toggleSuggest")){
        	cvEntryComp.toggleSuggestPanel(externalDialog.getContentPane());
        } else {
            for (int i = 0; i < numberOfLocales; i++) {
                if (command.equals(allLocales[i].getDisplayName())) {
                    annotationLocale = allLocales[i];

                    if (attached) {
                        ImUtil.setLanguage(textArea, annotationLocale);
                        textArea.setFont(uniFont);
                    } else {
                        ImUtil.setLanguage(exttextArea, annotationLocale);
                        exttextArea.setFont(uniFont.deriveFont(20.0f));
                    }

                    break;
                }
            }
        }
    }

    /**
     * Mouse event handling for popping up the popup menu.
     *
     * @param e the mouse event
     */
    @Override
	public void mouseClicked(MouseEvent e) {
    }

    /**
     * Stub
     *
     * @param e the mouse event
     */
    @Override
	public void mouseEntered(MouseEvent e) {
    }

    /**
     * Stub
     *
     * @param e the mouse event
     */
    @Override
	public void mouseExited(MouseEvent e) {
    }

    /**
     * Stub
     *
     * @param e the mouse event
     */
    @Override
	public void mousePressed(MouseEvent e) {
		if (javax.swing.SwingUtilities.isRightMouseButton(e) ||
			 e.isPopupTrigger()) {
			updatePopup();
			popupMenu.show(textArea, e.getX(), e.getY());

			popupMenu.setVisible(true);
		}
    }

    /**
     * Stub
     *
     * @param e the mouse event
     */
    @Override
	public void mouseReleased(MouseEvent e) {
    }

    /**
     * M_P all e.consume() calls outcommented because the events are needed to
     * deselect a newly  made / edited annotation in EudicoAnnotationFrame
     * with the Escape key M_P 25 june 2003 Just the first outcommented. The
     * last when has to be consumed.
     * OS May 2016: not consuming ESC leads to undesired effects when this editbox
     * is embedded in a table (or maybe in a window that closes on ESC).
     * Whoever wants a notification that editing has ended
     * had better become an InlineEditBoxListener. EudicoAnnotationFrame does not
     * exist anymore so the previous must be rather out of date anyway.
     *
     * @param e the key event
     */
    @Override
	public void keyPressed(KeyEvent e) {    		
    	KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);   
    	
    	if(keyStrokesNotToBeConsumed.contains(ks)){    
    		oriValue = ((JTextArea)e.getSource()).getText();
    		cursorPos = ((JTextArea)e.getSource()).getCaretPosition();
    		restoreOriValue = false;
    		//temp
    		if (e.getKeyCode() == KeyEvent.VK_SPACE && (e.getModifiers() == KeyEvent.SHIFT_MASK
    				|| e.getModifiers() == KeyEvent.ALT_MASK ||e.getModifiers() == 0)) {
    			restoreOriValue = true;
    		} 
    		return;
    	}
    	
        // KB Cancel Changes
    	else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            //e.consume();
            cancelEdit();
        }
        // KB Detach
        else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && e.isShiftDown()) {
            if (attachable == true) {
                e.consume();

                // thread is necessary to avoid the dialog blocking events still in the eventqueue!
                if (attached) {
                    SwingUtilities.invokeLater(new Runnable() {
                            @Override
							public void run() {
                                detachEditor();
                            }
                        });
                } else {
                    attachEditor();
                }
            }
        }
        // KB Confirm
        else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && 
			(e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
            e.consume();
            commitEdit();
        } /*else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && e.isMetaDown() &&
                System.getProperty("os.name").startsWith("Mac OS")) {
            commitEdit(); // hack for osx metakey
        }*/
        else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && enterCommits) {
            e.consume();
            commitEdit();
        } else if (defaultRegisteredKeyStrokes.contains(ks)) {
        	// don't consume
        }
        /*else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || 
        		e.getKeyCode() == KeyEvent.VK_DELETE ||
        		e.getKeyCode() == KeyEvent.VK_LEFT ||
        		e.getKeyCode() == KeyEvent.VK_RIGHT ||
        		e.getKeyCode() == KeyEvent.VK_UP ||
        		e.getKeyCode() == KeyEvent.VK_DOWN ||
        		e.getKeyCode() == KeyEvent.VK_PAGE_DOWN ||
        		e.getKeyCode() == KeyEvent.VK_PAGE_UP ||
        		e.getKeyCode() == KeyEvent.VK_HOME ||
        		e.getKeyCode() == KeyEvent.VK_END ||
        		e.getKeyCode() == KeyEvent.VK_ENTER ) {
        } */
        // June 2010 capture the standard undo/redo events to prevent undo/redo being called in the enclosing program
        // while the edit box is active. Ideally an UndoManager should be installed for the edit box.
        // maybe better to use the user defined shortcuts for undo and redo
//        else if ( (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_Y) && 
//    			(e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
//        	// do nothing unless UndoManager is active 
//        	e.consume();
//        }
        else {
        	// consume the event to prevent actions with keyboard shortcuts to be triggered?
        	// no, that disables e.g. the backspace key on a mac        	
        	if(ks.getModifiers() == KeyEvent.CTRL_DOWN_MASK || 
        			ks.getModifiers() == KeyEvent.ALT_DOWN_MASK ||
        			ks.getModifiers() == KeyEvent.META_DOWN_MASK) {
				e.consume();
			}
        }
    }

    /**
     * Stub
     *
     * @param e the key event
     */
    @Override
	public void keyReleased(KeyEvent e) {    
    	if(restoreOriValue){    		
    		((JTextArea)e.getSource()).setText(oriValue);
    		((JTextArea)e.getSource()).setCaretPosition(cursorPos);
    		restoreOriValue = false;
    	}
    }

    /**
     * Stub
     *
     * @param e the key event
     */
    @Override
	public void keyTyped(KeyEvent e) {      
    }

    /**
     * Updates menu items of the menubar by checking the system clipboard.
     *
     * @param e the menu event
     */
    @Override
	public void menuSelected(MenuEvent e) {
        updateMenuBar();
    }

    /**
     * Stub
     *
     * @param e the menu event
     */
    @Override
	public void menuDeselected(MenuEvent e) {
    }

    /**
     * Stub
     *
     * @param e the menu event
     */
    @Override
	public void menuCanceled(MenuEvent e) {
    }

    /**
     * Updates UI elements after a change in the selected Locale.
     */
    @Override
	public void updateLocale() {
        detachPUMI.setText(ElanLocale.getString("InlineEditBox.Detach"));
        commitPUMI.setText(ElanLocale.getString("InlineEditBox.Commit"));
        cancelPUMI.setText(ElanLocale.getString("InlineEditBox.Cancel"));
        cutPUMI.setText(ElanLocale.getString("InlineEditBox.Edit.Cut"));
        copyPUMI.setText(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        pastePUMI.setText(ElanLocale.getString("InlineEditBox.Edit.Paste"));

        if (menuBar != null) {
            editorMenu.setText(ElanLocale.getString("InlineEditBox.Menu.Editor"));
            editMenu.setText(ElanLocale.getString("Menu.Edit"));
            selectLanguageMenu.setText(ElanLocale.getString(
                    "InlineEditBox.Menu.Select"));
            attachMI.setText(ElanLocale.getString("InlineEditBox.Attach"));
            commitMI.setText(ElanLocale.getString("InlineEditBox.Commit"));
            cancelMI.setText(ElanLocale.getString("InlineEditBox.Cancel"));
            closeMI.setText(ElanLocale.getString("Button.Close"));
            cutMI.setText(ElanLocale.getString("InlineEditBox.Edit.Cut"));
            copyMI.setText(ElanLocale.getString("InlineEditBox.Edit.Copy"));
            pasteMI.setText(ElanLocale.getString("InlineEditBox.Edit.Paste"));
        }
    }

    //////////////////////////
    // inner class: a component for selecting an entry from a list of entries of a cv
    //////////////////////////

    /**
     * A class that provides a component for the selection of an entry from  a ControlledVocabulary.<br>
     * The current possible delegate components are a JScrollPane containing
     * a JList or a JComboBox
     *
     * @author Han Sloetjes
     */
    class CVEntryComponent implements KeyListener, ActionListener, DocumentListener {
        /** the list containing the cv entries */
        private JList entryList;

        /** the model for the list */
        private DefaultListModel entryListModel;

        /** the scrollpane for the list */
        private JScrollPane scrollPane;
        
        /** the table containing the cv entries */
        private JTable entryTable;
        
        /** the model for the table */
        private DefaultTableModel entryTableModel;
        
        /** the scrollPane for the table */
        private JScrollPane tableScrollPane;
        
        /** moudelistener for bringing up the popup menu */
        private MouseListener tablePopupListener;

        /** mouse listener that handles a double click on a list entry */
        private MouseListener tableDoubleClickListener;

        /** popup menu for detach, commit and cancel */
        private JPopupMenu popup;

        /** moudelistener for bringing up the popup menu */
        private MouseListener popupListener;

        /** mouse listener that handles a double click on a list entry */
        private MouseListener doubleClickListener;

        /** menu items for detaching, cancelling and committing */
        private JMenuItem detachMI;

        /** menu items for detaching, cancelling and committing */
        private JMenuItem cancelMI;

        /** menu items for detaching, cancelling and committing */
        private JMenuItem commitMI;
        
		/** menu item to toggle the suggest panel */
        private JMenuItem toggleSuggestMI;

        /** a combo box editor component */
        private JComboBox box;

        /** a model for the combo box */
        private DefaultComboBoxModel entryBoxModel;

        /**
         * the component to use for editing, either a scrollpane containing a
         * JList  or a JComboBox
         */
        private JComponent delegate;

        /**
         * the array of CV entries from the ControlledVocabulary referenced by
         * the  LinguisticType in use by the Tier containing the current
         * annotation
         */
        private SimpleCVEntry[] entries;

        /** the annotation to edit */
        private Annotation annotation;
        
        /** the length in numbers of characters of the longest entry **/
        private int maxEntryLength = 0;
        // By Micha:
        /** the textfield for the suggestion (by Micha) */
		private JTextField textField;

		private Document textFieldDoc;

		/** the panel for the suggestion (by Micha) */
		private JPanel suggestPanel;

		private volatile Thread t;
        
        private String oldPartial = new String();

		private JList suggestEntryList;

		private DefaultListModel suggestEntryListModel;

		private JScrollPane suggestScrollPane;

		private MouseAdapter suggestPanelPopupListener;

		private MouseAdapter suggestPanelDoubleClickListener;

		// Holds previous language index for keeping track of
		// changes in the language index
		private int oldLangIndex = -1;

       /**
         * Creates a new entrylist and initializes components.<br>
         * Components are being initialized depending on the type of the
         * argument.
         *
         * @param componentClass the type of component to use for edit
         *        operations
         */
        public CVEntryComponent(Class componentClass) {
            initComponents(componentClass);
        }

        /**
         * Returns the current delegate component.
         *
         * @return the delegate component for editing actions
         */
        public JComponent getEditorComponent() {
            return delegate;
        }

        /**
         * Sets which type of component should be used for editing. Can depend
         * on the kind of viewer that created the InlineEditBox  and of the
         * attached / detached state.
         *
         * @param compClass the type of component to use for editing
         */
        void setDelegate(Class compClass) {
        	
        	if(showCVDescription){
        		if(compClass == JPanel.class){
        			if (delegate == suggestPanel) {
                        return;
                    }
        		} 
        		else if(delegate == tableScrollPane){
        			return;
        		} 
        	} else if (delegate.getClass() == compClass) {
                return;
            }
            
            // if show cv description is true always return 
            // a scrollpane with a Table as a delegate
            // regardless of the compClass
            if(showCVDescription){
            	if (entryTable == null) {
            		initComponents(compClass);
        		}
            	
            	if(compClass == JPanel.class){
            		if(suggestPanel == null){
            			initComponents(compClass);
            		} else {
            			suggestScrollPane.getViewport().add(entryTable);
            			entryTable.removeKeyListener(this);
            		}
            		
            		delegate = suggestPanel;
            	} else {
            		entryTable.addKeyListener(this);
            		tableScrollPane.getViewport().add(entryTable);
            		delegate = tableScrollPane;            		
            	}
            	
            	while(entryTableModel.getRowCount() > 0){
            		entryTableModel.removeRow(entryTable.getRowCount()-1);
            	}
                fillModel(true);

                // TO DO: check
                String cveId = null;

                if (annotation != null) {
                    cveId = annotation.getCVEntryId();
                    for (int i = 0; i < entryTable.getRowCount(); i++) {
        				SimpleCVEntry entry = (SimpleCVEntry) entryTableModel.getValueAt(i, 0);
	                	String id = entry.getId();
        				if (cveId.equals(id)) {
        					entryTable.setRowSelectionInterval(i, i);
        					scrollIfNeededAutomatically();
        					break;
        				}
        			}
                }
            } else {
            	if (compClass == JComboBox.class) {
                    if (box == null) {
                        initComponents(compClass);
                    }

                    delegate = box;
                    
                    // make sure it is filled with the current entries
                    entryBoxModel.removeAllElements();
                    fillModel(true);

                    if (entryList != null) {
                        box.setSelectedItem(entryList.getSelectedValue());
                    }
                } else if (compClass == JScrollPane.class) {
                    if (entryList == null) {
                        initComponents(compClass);
                    }

                    delegate = scrollPane;
                    entryListModel.clear();
                    fillModel(true);

                    if (box != null) {
                        entryList.setSelectedValue(box.getSelectedItem(), true);
                    }
                } else if (compClass == JPanel.class) {
        			if (suggestEntryList == null) {
        				initComponents(compClass);
        			}
        			
        			delegate = suggestPanel;
        			
        			suggestEntryListModel.clear();
        			fillModel(true);

        			if (box != null) {
        				suggestEntryList.setSelectedValue(box.getSelectedItem(), true);
        			}
        		}
            }
        }

        /**
         * Tries to ensure that the selected item is visible in the
         * scrollpane's viewport. Applies only to the JList component.
         */
        public void ensureSelectionIsVisible() {
            if (delegate instanceof JScrollPane && entryList != null) {
                entryList.ensureIndexIsVisible(entryList.getSelectedIndex());
            } else if (delegate instanceof JPanel && suggestEntryList != null) {
    			suggestEntryList.ensureIndexIsVisible(suggestEntryList.getSelectedIndex());
     	    }
        }

        /**
         * When this list is in a detached dialog it doesn't need the popup
         * because all options are in the menu bar. Applies only to the JList
         * component.
         */
        public void removePopupListener() {
        	if (entryTable != null) {
                entryTable.removeMouseListener(tablePopupListener);
                if(delegate == suggestPanel){
                	textField.removeMouseListener(tablePopupListener);
                }
            }
        	
            if (entryList != null) {
                entryList.removeMouseListener(popupListener);
            }
            if (suggestEntryList != null) {
    			suggestEntryList.removeMouseListener(suggestPanelPopupListener);
    			textField.removeMouseListener(suggestPanelPopupListener);
    		}
        }

        /**
         * When this list is not in a dialog menu items for detaching,
         * committing  and cancelling need to be provided.  Applies only to
         * the JList component.
         */
        public void addPopupListener() {
        	if (entryTable != null) {
                MouseListener[] listeners = entryTable.getMouseListeners();

                for (MouseListener listener2 : listeners) {
                    if (listener2 == tablePopupListener) {
                        return;
                    }
                }

                entryTable.addMouseListener(tablePopupListener);
                if(delegate == suggestPanel){
                	textField.addMouseListener(tablePopupListener);
                }
            }
            if (entryList != null) {
                MouseListener[] listeners = entryList.getMouseListeners();

                for (MouseListener listener2 : listeners) {
                    if (listener2 == popupListener) {
                        return;
                    }
                }

                entryList.addMouseListener(popupListener);
            }
    		if (suggestEntryList != null) {
    			MouseListener[] listeners = suggestEntryList.getMouseListeners();

    			for (MouseListener listener2 : listeners) {
    				if (listener2 == suggestPanelPopupListener) {
    					return;
    				}
    			}

    			suggestEntryList.addMouseListener(suggestPanelPopupListener);
    			textField.addMouseListener(suggestPanelPopupListener);
    		}
    		if (entryTable != null) {
    			MouseListener[] listeners = entryTable.getMouseListeners();

    			for (MouseListener listener2 : listeners) {
    				if (listener2 == tablePopupListener) {
    					return;
    				}
    			}

    			entryTable.addMouseListener(tablePopupListener);
    		}
        }
        
        /**
         * 
         */
        private void updateTableSize(Dimension size){
        	entryTable.getColumn("value").setMinWidth((size.width * firstColPercentage)/100);
        	entryTable.getColumn("value").setMaxWidth((size.width * firstColPercentage)/100);
        	entryTable.setRowHeight(minCVHeight);
        }
        
        /**
    	 * Scrolls the current editing row to
    	 * the center of the table if needed
    	 * 
    	 */
    	public void scrollIfNeededAutomatically(){
    		JViewport viewport = (JViewport) entryTable.getParent();	
    		Rectangle rect = entryTable.getCellRect(entryTable.getSelectedRow(),
    				entryTable.getSelectedColumn(), true);		   
    		Rectangle viewRect = viewport.getViewRect();
    		rect.setLocation(rect.x-viewRect.x, rect.y-viewRect.y);

    		rect.translate(0, 0);
    	
    		viewport.scrollRectToVisible(rect);
    	}   		

        /**
         * Initializes either a list in a scrollpane (with a popup menu etc) or
         * a combo box.  Adds listeners.
         *
         * @param component the type of component to use for editing
         */
        private void initComponents(Class component) {
        	if(showCVDescription){
        		if (entryTable == null) {
        			entryTableModel = new DefaultTableModel(){
        				@Override
						public boolean isCellEditable(int row, int column){
        					return false;
        				}
        			};
        			entryTableModel.setColumnIdentifiers(new Object[]{"value", "desc"});
        			
        			entryTable = new JTable(entryTableModel);
        			entryTable.setTableHeader(null);
                    entryTable.setFont(InlineEditBox.this.getFont());
                    entryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    entryTable.addMouseMotionListener(new MouseMotionListener() {
						
                    	int lastRow = -1;
                    	//int lastCol = -1;
                    	
						@Override
						public void mouseMoved(MouseEvent e) {
							Point p = e.getPoint();
							int row = entryTable.rowAtPoint(p);
			                int col = entryTable.columnAtPoint(p);
			                
			                if ((row > -1 && row < entryTable.getRowCount()) && (col > -1 && col < entryTable.getColumnCount())) {
			                	if(row != lastRow) { // The user changed rows
			                		lastRow = row;
			                		
			                		String cvEntryId = ((SimpleCVEntry) entryTableModel.getValueAt(row, 0)).getId();
			                		InlineEditBox.this.showVideoForEntry(cvEntryId);
			                	}
			                }

						}
						
						@Override
						public void mouseDragged(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}
					});
                    entryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
        				@Override
						public Component getTableCellRendererComponent(JTable table,
                                Object value, boolean isSelected, boolean hasFocus,
                                int row, int column){
        					if (value == null) {
        						this.setText("");
        					} else if(value instanceof SimpleCVEntry){
        						this.setText( ((SimpleCVEntry)value).getValue());
        					} else {
        						this.setText(value.toString());
        					}
        					this.setToolTipText(this.getText());
        					
        					if (isSelected) {
        		                setBackground(table.getSelectionBackground());
        		                setForeground(table.getSelectionForeground());
        		            } else {
        		                setBackground(table.getBackground());
        		                setForeground(table.getForeground());
        		            }
        					
        		            setOpaque(true);
        					
        					return this;
        				}
        			});
                    
                    tablePopupListener = new MouseAdapter() {
                    	@Override
						public void mousePressed(MouseEvent e) {
                    		if (SwingUtilities.isRightMouseButton(e) || 
                    				e.isPopupTrigger()) {
                    			CVEntryComponent.this.popup.show(CVEntryComponent.this.entryTable,
                    					e.getX(), e.getY());
                    			CVEntryComponent.this.popup.setVisible(true);
                    		}
                    	}
                    };
                    
                    if (popup == null) {
            			createPopupMenu();
            		}

                    tableDoubleClickListener = new MouseAdapter() {
                    	@Override
						public void mouseClicked(MouseEvent e) {
                    		if (e.getClickCount() > 1) {
                    			InlineEditBox.this.commitEdit();
                    		}
                    	}
                    };
                    
                    entryTable.addMouseListener(tablePopupListener);
                    entryTable.addMouseListener(tableDoubleClickListener);
        		}
        		
        		if(component == JPanel.class){
    				suggestScrollPane = new JScrollPane(entryTable);

    				textField = new JTextField();
    				textField.addKeyListener(this);
    				textFieldDoc = textField.getDocument();
    				textFieldDoc.addDocumentListener(this);
    				suggestPanel = new JPanel(new BorderLayout());
    				suggestPanel.add(textField, BorderLayout.NORTH);
    				suggestPanel.add(suggestScrollPane, BorderLayout.CENTER);
    				
    				textField.addMouseListener(tablePopupListener);
    				
    				delegate = suggestPanel;
    				
    				entryTable.removeKeyListener(this);
                } else {
                	entryTable.addKeyListener(this);
                	
                	tableScrollPane = new JScrollPane(entryTable);
                	tableScrollPane.addMouseListener(new MouseAdapter() {
                		@Override
                		public void mouseExited(MouseEvent e) {
                			System.out.println("MOUSE EXITED " + e.getPoint().toString());
                			showVideoForEntry("");
                		}
					});
                	JScrollBar verticalScrollbar = tableScrollPane.getVerticalScrollBar();
                	if(verticalScrollbar != null) {
                		verticalScrollbar.addAdjustmentListener(new AdjustmentListener() {
						
	                    	int lastRow = -1;
	                    	//int lastCol = -1;
	                    	
							@Override
							public void adjustmentValueChanged(AdjustmentEvent e) {
								Point p = tableScrollPane.getMousePosition();
								if(p == null) {
									return;
								}
	
								Point vpp = tableScrollPane.getViewport().getViewPosition();
								
								p.translate(vpp.x, vpp.y);
								
								int row = entryTable.rowAtPoint(p);
				                int col = entryTable.columnAtPoint(p);
				                
				                if ((row > -1 && row < entryTable.getRowCount()) && (col > -1 && col < entryTable.getColumnCount())) {
				                	if(row != lastRow) { // The user changed rows
				                		lastRow = row;
				                		
				                		String cvEntryId = ((SimpleCVEntry) entryTableModel.getValueAt(row, 0)).getId();
				                		InlineEditBox.this.showVideoForEntry(cvEntryId);
				                	}
				                }
							}
						});
                	}
                	delegate = tableScrollPane;
                }
        	} else {
        		entryTable = null;
        		if (component == JScrollPane.class) {
                    if (entryList == null) {
                        entryListModel = new DefaultListModel();
                        entryList = new JList(entryListModel);
                        entryList.setFont(InlineEditBox.this.getFont());
                        entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        scrollPane = new JScrollPane(entryList);
                        
                        if (popup == null) {
                			createPopupMenu();
                		}
                        
                        popupListener = new MouseAdapter() {
                                    @Override
									public void mousePressed(MouseEvent e) {
                                        if (SwingUtilities.isRightMouseButton(e) || 
                                        	e.isPopupTrigger()) {
                                            CVEntryComponent.this.popup.show(CVEntryComponent.this.entryList,
                                                e.getX(), e.getY());
                                            CVEntryComponent.this.popup.setVisible(true);
                                        }
                                    }
                                };

                        doubleClickListener = new MouseAdapter() {
                                    @Override
									public void mouseClicked(MouseEvent e) {
                                        if (e.getClickCount() > 1) {
                                            InlineEditBox.this.commitEdit();
                                        }
                                    }
                                };

                        entryList.addMouseListener(popupListener);
                        entryList.addMouseListener(doubleClickListener);

                        entryList.addKeyListener(this);
                        entryList.addListSelectionListener(new ListSelectionListener(){
                        	@Override
							public void valueChanged(ListSelectionEvent lse) {
    							CVEntryComponent.this.ensureSelectionIsVisible();
                        	}
                        });
                        
                        entryList.addMouseMotionListener(new MouseMotionListener() {
    						
                        	int lastRow = -1;
                        	//int lastCol = -1;
                        	
    						@Override
    						public void mouseMoved(MouseEvent e) {
    							Point p = e.getPoint();
    							int row = entryList.locationToIndex(p);
    			                
    			                if ((row > -1 && row < entryListModel.size())) {
    			                	if(row != lastRow) { // The user changed rows
    			                		lastRow = row;
    			                		
    			                		String cvEntryId = ((SimpleCVEntry) entryListModel.getElementAt(row)).getId();
    			                		InlineEditBox.this.showVideoForEntry(cvEntryId);
    			                	}
    			                }

    						}
    						
    						@Override
    						public void mouseDragged(MouseEvent e) {
    							// TODO Auto-generated method stub
    							
    						}
    					});
                        
                        JScrollBar verticalScrollbar = scrollPane.getVerticalScrollBar();
                    	if(verticalScrollbar != null) {
                    		verticalScrollbar.addAdjustmentListener(new AdjustmentListener() {
    						
    	                    	int lastRow = -1;
    	                    	//int lastCol = -1;
    	                    	
    							@Override
    							public void adjustmentValueChanged(AdjustmentEvent e) {
    								Point p = scrollPane.getMousePosition();
    								if(p == null) {
    									return;
    								}
    	
    								Point vpp = scrollPane.getViewport().getViewPosition();
    								
    								p.translate(vpp.x, vpp.y);
    								
    								int row = entryList.locationToIndex(p);
    				                
    				                if ((row > -1 && row < entryListModel.size())) {
    				                	if(row != lastRow) { // The user changed rows
    				                		lastRow = row;
    				                		
    				                		String cvEntryId = ((SimpleCVEntry) entryListModel.getElementAt(row)).getId();
        			                		InlineEditBox.this.showVideoForEntry(cvEntryId);
    				                	}
    				                }
    							}
    						});
                    	}
                        delegate = scrollPane;
                    }
                } else if (component == JComboBox.class) {
                    if (box == null) {
                        entryBoxModel = new DefaultComboBoxModel();
                        box = new JComboBox(entryBoxModel);
                        
                        box.addActionListener(this);
                        box.addKeyListener(this);
                        delegate = box;
                    }
                } else if (component == JPanel.class) {
        			if (suggestEntryList == null) {
        				suggestEntryListModel = new DefaultListModel();
        				suggestEntryList = new JList(suggestEntryListModel);
        				suggestEntryList.setFont(InlineEditBox.this.getFont());
        				suggestEntryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        				//suggestEntryList.setFocusable(false);
        				suggestScrollPane = new JScrollPane(suggestEntryList);

        				textField = new JTextField();
        				textField.addKeyListener(this);
        				textFieldDoc = textField.getDocument();
        				textFieldDoc.addDocumentListener(this);
        				suggestPanel = new JPanel(new BorderLayout());
        				suggestPanel.add(textField, BorderLayout.NORTH);
        				suggestPanel.add(suggestScrollPane, BorderLayout.CENTER);
        				
        				if (popup == null) {
        					createPopupMenu();
        				}
        				
        				suggestPanelPopupListener = new MouseAdapter() {
        					@Override
							public void mousePressed(MouseEvent e) {
        						if (SwingUtilities.isRightMouseButton(e) || 
        							e.isPopupTrigger()) {
        							CVEntryComponent.this.popup.show(CVEntryComponent.this.suggestEntryList,
        								e.getX(), e.getY());
        							CVEntryComponent.this.popup.setVisible(true);
        						}
        					}
        				};
        				
        				suggestPanelDoubleClickListener = new MouseAdapter() {
        							@Override
									public void mouseClicked(MouseEvent e) {
        								if (e.getClickCount() > 1) {
        									InlineEditBox.this.commitEdit();
        								}
        							}
        						};

        				suggestEntryList.addMouseListener(suggestPanelPopupListener);
        				textField.addMouseListener(suggestPanelPopupListener);
        				suggestEntryList.addMouseListener(suggestPanelDoubleClickListener);

        				suggestEntryList.addListSelectionListener(new ListSelectionListener(){
        					@Override
							public void valueChanged(ListSelectionEvent lse) {
        						CVEntryComponent.this.ensureSelectionIsVisible();
        					}
        				});

        				delegate = suggestPanel;
        			}
        		}
        	}
        	
            
        }

        /**
         * Sets the font for the entry list component.
         *
         * @param f the font
         */
        public void setFont(Font f) {
            if (delegate == box) {
                box.setFont(f);
            } else if (delegate == scrollPane) {
                entryList.setFont(f);
            } else if (delegate == suggestPanel) {
            	textField.setFont(f);
            	if(showCVDescription){
            		entryTable.setFont(f);
            	} else {
            		suggestEntryList.setFont(f);
            	}
            } else if (delegate == tableScrollPane){
            	entryTable.setFont(f);
            }
        }

        /**
         * Gets the entry array with the entries in the cv referenced by the
         * linguistic type of the tier.
         * HS Jan 2011: try to re-use the (potentially long) list of cv entries
         *
         * @param annotation the active annotation
         */
        public void setAnnotation(Annotation annotation) {
        	ControlledVocabulary oldCV = null;
        	
        	if (this.annotation != null) {
        		TierImpl tier = (TierImpl) this.annotation.getTier();
                TranscriptionImpl trans = tier.getTranscription();
                oldCV = trans.getControlledVocabulary(tier.getLinguisticType()
                                  .getControlledVocabularyName());
        	}
        	
            this.annotation = annotation;
            ControlledVocabulary cv = null;
            int langIndex = -1;
            
            if (annotation != null) {
            	TierImpl tier = (TierImpl) annotation.getTier();
                Pair<ControlledVocabulary, Integer> pair = tier.getEffectiveLanguage();
        		if (pair != null) {
            		cv = pair.getFirst();
            		langIndex = pair.getSecond();
        		}
            }
            
            if (cv != null) {
            	// Check which language to use for this editor.
        		if (langIndex < 0) {
        			langIndex = cv.getDefaultLanguageIndex();
        		}
                // reload local CVs anyway
            	if (cv != oldCV || !(cv instanceof ExternalCV)) {
            		entries = cv.getSimpleEntries(langIndex);
        			//Arrays.sort(entries);
                    if (entryListModel != null) {
                        entryListModel.clear();
                    }
                    
                    if(entryTableModel != null){
                    	while(entryTableModel.getRowCount() > 0){
                    		entryTableModel.removeRow(entryTableModel.getRowCount()-1);
                    	}
                    }

                    if (entryBoxModel != null) {
                        entryBoxModel.removeAllElements();
                    }
                    fillModel(true);
            	} else {
            		if(langIndex != oldLangIndex) {
            			entries = cv.getSimpleEntries(langIndex);
            			//Arrays.sort(entries);
            			fillModel(true);
            		} else {
            			// else reuse existing list, currently only for external CV because there is 
                		// no notification of changes in local CVs yet
                		fillModel(false);
            		}
            	}       
            	oldLangIndex = langIndex;            
            } else { // cv == null
            	entries = new SimpleCVEntry[]{};
                if (entryListModel != null) {
                    entryListModel.clear();
                }
                
                if(entryTableModel != null){
                	while(entryTableModel.getRowCount() > 0){
                		entryTableModel.removeRow(entryTableModel.getRowCount()-1);
                	}
                }

                if (entryBoxModel != null) {
                    entryBoxModel.removeAllElements();
                }
                fillModel(false);
            }
        }

        /**
         * Fills the model of either the combo box or the list with the entries
         * of the current Controlled Vocabulary.
         */
        private void fillModel(boolean reload) {
            String cveId = null;

            if (annotation != null) {
                cveId = annotation.getCVEntryId();
            }

            if (delegate == scrollPane) {
				boolean selected = false;

            	if (reload) {
            		int nAdded = 0;
	                for (SimpleCVEntry entry : entries) {
                		String v = entry.getValue();
	                	if (!v.isEmpty()) {
		                    entryListModel.addElement(entry);
	
		                    if (cveId != null && cveId.equals(entry.getId())) {
		                        entryList.setSelectedIndex(nAdded);
            					selected = true;
		                    }
		                    
		                    if (v.length() > maxEntryLength) {
		                    	maxEntryLength = v.length();
		                    }
		                    
		                    int width = getFontMetrics(this.getEditorComponent().getFont()).stringWidth(v);
		                    if(minCVWidth < width){
		                    	minCVWidth = width;
		                    }
		                    nAdded++;
	                	}
	                }
            	} else {
            		// select the current value
            		if (cveId != null) {
            			for (int i = 0; i < entryListModel.size(); i++) {
            				SimpleCVEntry entry = (SimpleCVEntry) entryListModel.getElementAt(i);
    	                	String id = entry.getId();
            				if (cveId.equals(id)) {
            					entryList.setSelectedIndex(i);
								selected = true;
            					break;
            				}
            			}
            		}
            	}   
                
				if (!selected) {
					// Avoid a default selection, if there is no matching SimpleCVEntry.
					// This is good for example when editing an empty annotation,
					// or a new one while recursive annotations are created.
					entryList.setSelectedIndex(-1);
				}
            	
            } else if (delegate == box) {
				boolean selected = false;

            	if (reload) {
	                for (SimpleCVEntry entry : entries) {
                		String v = entry.getValue();
	                    if (!v.isEmpty()) {
		                    entryBoxModel.addElement(entry);
		
		                    if (cveId != null && cveId.equals(entry.getId())) {
		                        entryBoxModel.setSelectedItem(entry);
            					selected = true;
		                    }
		                    
		                    if (v.length() > maxEntryLength) {
		                    	maxEntryLength = v.length();
		                    }
		                    
		                    int width = getFontMetrics(this.getEditorComponent().getFont()).stringWidth(v);
		                    if(minCVWidth < width){
		                    	minCVWidth = width;
		                    }
							selected = true;
	                    }
	                }
            	} else {
            		if (cveId != null) {
            			for (int i = 0; i < entryBoxModel.getSize(); i++) {
            				SimpleCVEntry entry = (SimpleCVEntry) entryBoxModel.getElementAt(i);
            				if (cveId.equals(entry.getId())) {
            					box.setSelectedIndex(i);
								selected = true;
            					break;
            				}
            			}
            		}
            	}
            	
				if (!selected) {
					// (see above)
            		box.setSelectedIndex(-1);  
            	}
                
            } else if (delegate == suggestPanel) {
            	String value = null;
            	if (annotation != null) {
            		value = annotation.getValue();
            	}
    			textField.setText(value); // This triggers a
    			// insertUpdate of the Document, which triggers
    			// the filling of the suggestEntryList
    		} if (delegate == tableScrollPane) {
				boolean selected = false;

            	if (reload) {
            		// Remove all rows
            		entryTableModel.setRowCount(0);
            		int nAdded = 0;
	                for (SimpleCVEntry entry : entries) {
                		String v = entry.getValue();
	                	if (!v.isEmpty()) {
		                    entryTableModel.addRow(new Object[]{entry, entry.getDescription()});
	
		                    if (cveId != null && cveId.equals(entry.getId())) {
		                    	entryTable.setRowSelectionInterval(nAdded, nAdded);
            					selected = true;
		                    }
		                    
		                    if (v.length() > maxEntryLength) {
		                    	maxEntryLength = v.length();
		                    }
		                    
		                    int width = getFontMetrics(this.getEditorComponent().getFont()).stringWidth(v);
		                    if(minCVWidth < width){
		                    	minCVWidth = width;
		                    }
		                    nAdded++;
	                	}
	                }
	                
	                scrollIfNeededAutomatically();
            	} else {
            		// select the current value
            		if (cveId != null) {
            			for (int i = 0; i < entryTable.getRowCount(); i++) {
            				SimpleCVEntry entry = (SimpleCVEntry) entryTableModel.getValueAt(i, 0);
    	                	String id = entry.getId();
            				if (cveId.equals(id)) {
            					entryTable.setRowSelectionInterval(i, i);
            					scrollIfNeededAutomatically();
								selected = true;
            					break;
            				}
            			}
            		}
            	}   
                
				if (!selected) {
					// Avoid a default selection, if there is no matching SimpleCVEntry.
					// This is good for example when editing an empty annotation,
					// or a new one while recursive annotations are created.
					//entryTable.setRowSelectionInterval(-1, -1);//IllegalArgument
					entryTable.clearSelection();
				}
            	
            } 
        }

        /**
         * Tries to grant the focus to the delegate component.
         */
        public void grabFocus() {
            if (delegate == box) {
                box.requestFocus();
            } else if (delegate == scrollPane) {
                entryList.requestFocus();
                entryList.ensureIndexIsVisible(entryList.getSelectedIndex());
            } else if (delegate == suggestPanel) {
    			textField.requestFocus();
    		} else if(delegate == tableScrollPane){
    			entryTable.requestFocus();
    			//scrollIfNeeded();
    		}
        }

        /**
         * Returns the currently selected entry value.
         *
         * @return the currently selected entry value or null
         */
        public String getSelectedEntryValue() {
            SimpleCVEntry e = getSelectedEntry();
            if (e != null) {
            	return e.getValue();
            }
            return null;
        }
        
        /**
         * Returns the currently selected entry.
         *
         * @return the currently selected entry or null
         */
        public SimpleCVEntry getSelectedEntry() {
        	SimpleCVEntry value = null;

            if (delegate == scrollPane) {
                if (entryList.getSelectedValue() != null) {
                    value = (SimpleCVEntry) entryList.getSelectedValue();
                }
            } else if (delegate == box) {
                if (box.getSelectedItem() != null) {
                    value = (SimpleCVEntry) box.getSelectedItem();
                }
            } else if (delegate == suggestPanel) {
            	if(showCVDescription){
            		if(entryTable.getSelectedRow() > -1){
            			value = (SimpleCVEntry)entryTable.getValueAt(entryTable.getSelectedRow() , 0);
            		}
            	} else if (suggestEntryList.getSelectedValue() != null) {
    				value = (SimpleCVEntry) suggestEntryList.getSelectedValue();
    			} 
    		} else if (delegate == tableScrollPane){
    			if(entryTable.getSelectedRow() > -1){
        			value = (SimpleCVEntry)entryTable.getValueAt(entryTable.getSelectedRow() , 0);
        		}
    		} 
            return value;
        }
        
        /**
         * Returns the number of characters of the longest entry value.
         * 
         * @return the length of the longest entry
         */
        public int getMaxEntryLength() {
        	return maxEntryLength;
        }

        /**
         * KeyPressed handling.
         *
         * @param e the key event
         */
        @Override
		public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				if (delegate != box) {
					if (e.isShiftDown()) {
						// There is no detached combobox
						InlineEditBox.this.detachEditor();
					} else {
						// The combo box doesn't think anything is selected here;
						// and ENTER will cause its normal actionPerformed call.
						// In all other cases commit when the Enter key is typed.
						if (getSelectedEntry() != null) {
							InlineEditBox.this.commitEdit();
						} else {
							InlineEditBox.this.cancelEdit();
						}
					}
				}
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                InlineEditBox.this.cancelEdit();
            } else if(e.getKeyCode() == KeyEvent.VK_DOWN && e.getSource() == textField) {
            	if(showCVDescription){
            		if(entryTable.getSelectedRow() == entryTable.getRowCount()-1) {
    					entryTable.clearSelection();
        			} else {
        				entryTable.setRowSelectionInterval(entryTable.getSelectedRow()+1, entryTable.getSelectedRow()+1);
                		scrollIfNeededAutomatically();
        			}
            	} else {
            		suggestEntryList.setSelectedIndex(suggestEntryList.getSelectedIndex() + 1);
        			suggestEntryList.ensureIndexIsVisible(suggestEntryList.getSelectedIndex());
            	}
    			
    		} else if(e.getKeyCode() == KeyEvent.VK_UP && e.getSource() == textField) {
    			if(showCVDescription){
    				if(entryTable.getSelectedRow() <= 0) {
    					entryTable.clearSelection();
        			} else {
        				entryTable.setRowSelectionInterval(entryTable.getSelectedRow()-1, entryTable.getSelectedRow()-1);
                		scrollIfNeededAutomatically();
        			}
    			} else {
    				if(suggestEntryList.getSelectedIndex() <= 0) {
        				suggestEntryList.clearSelection();
        			} else {
        				suggestEntryList.setSelectedIndex(suggestEntryList.getSelectedIndex() - 1);
        				suggestEntryList.ensureIndexIsVisible(suggestEntryList.getSelectedIndex());
        			}
    			}
    		} else if(e.getKeyCode() == KeyEvent.VK_U && 
    				(e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
    			if(InlineEditBox.this.isAttached()) {
    				toggleSuggestPanel(InlineEditBox.this);
    			}
    		} else {
            	if (entries != null) {
            		int code = e.getKeyCode();
            		for (SimpleCVEntry cve : entries) {
            			if (cve.getShortcutKeyCode() == code) {
            				if (delegate == scrollPane) {
            					entryList.setSelectedValue(cve, false);
            				} else if (delegate == box) {
            					box.setSelectedItem(cve);
            				} else if(delegate == tableScrollPane){
            					for(int i=0; i < entryTable.getRowCount(); i++){
            						if(cve == entryTable.getValueAt(i, 0)){
            							entryTable.setRowSelectionInterval(i, i);
            							break;
            						}
            					}
            				}
            				InlineEditBox.this.commitEdit();
            				break;
            			}
            		}
            	}
            }
        }

        /**
         * Key released handling: do nothing.
         *
         * @param e the key event
         */
        @Override
		public void keyReleased(KeyEvent e) {
        }

        /**
         * Key typed handling: do nothing.
         *
         * @param e the key event
         */
        @Override
		public void keyTyped(KeyEvent e) {
        }

        /**
         * Action handling.
         *
         * @param ae the action event
         */
        @Override
		public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == detachMI) {
                if (attachable) {
                    if (attached) {
                        InlineEditBox.this.detachEditor();
                    } else {
                        InlineEditBox.this.attachEditor();
                    }
                }
            } else if (ae.getSource() == commitMI) {
                InlineEditBox.this.commitEdit();
            } else if (ae.getSource() == cancelMI) {
                InlineEditBox.this.cancelEdit();
            } else if (ae.getSource() == box) {	    		
                if ((ae.getID() == ActionEvent.ACTION_PERFORMED) &&
                        (ae.getModifiers() == InputEvent.BUTTON1_MASK)) {
                    // prevent that the first click / doubleclick on the combo box
                    // causes a commit
                    if (box.isPopupVisible()) {
                        InlineEditBox.this.commitEdit();
                    }
                }
            }
            else if (ae.getSource() == toggleSuggestMI){
    			toggleSuggestPanel(InlineEditBox.this);
    		}
        }
        
        /**
    	 * Toggles between the scrollPane and the suggestPanel
    	 * 
    	 * @param container (the container of the scrollPane or suggestPanel)
    	 */
    	private void toggleSuggestPanel(Container container) {
    		container.removeAll();
    		
			if (delegate == scrollPane || delegate == tableScrollPane) {
				//System.err.println("DEBUG: delegate == scrollPane");
				setDelegate(JPanel.class);
				container.add(getEditorComponent(), BorderLayout.CENTER);
			} else if (delegate == suggestPanel) {
				//System.err.println("DEBUG: delegate == suggestPanel");
				setDelegate(JScrollPane.class);
				container.add(getEditorComponent(), BorderLayout.CENTER);
			}
			
			grabFocus();

			container.repaint();
			container.validate();	
		}
    	
    	/**
    	 * Creates the popup menu for the scrollPane and suggestPanel
    	 */
    	private void createPopupMenu() {
    		popup = new JPopupMenu();
            detachMI = new JMenuItem(ElanLocale.getString(
                        "InlineEditBox.Detach"));
            detachMI.addActionListener(this);
            detachMI.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, ActionEvent.SHIFT_MASK));
            popup.add(detachMI);
            commitMI = new JMenuItem(ElanLocale.getString(
                        "InlineEditBox.Commit"));
            commitMI.addActionListener(this);
            commitMI.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            popup.add(commitMI);
            cancelMI = new JMenuItem(ElanLocale.getString(
                        "InlineEditBox.Cancel"));
            cancelMI.addActionListener(this);
            cancelMI.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ESCAPE, 0));
            popup.add(cancelMI);
            
            popup.add(new JSeparator());
            toggleSuggestMI = new JMenuItem(ElanLocale.getString(
            	"InlineEditBox.ToggleSuggestPanel"));
            toggleSuggestMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
            		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            toggleSuggestMI.addActionListener(this);
            popup.add(toggleSuggestMI);
    	}
    	
    	/* (non-Javadoc)
    	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
    	 */
    	@Override
		public void insertUpdate(DocumentEvent e) {
    		if(e.getDocument() == textFieldDoc && !oldPartial.equals(textField.getText())) {
    			findSuggestions();
    		}
        }
        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        @Override
		public void removeUpdate(DocumentEvent e) {
        	if(e.getDocument() == textFieldDoc && !oldPartial.equals(textField.getText())) {
    			findSuggestions();
    		}
        }
        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        @Override
		public void changedUpdate(DocumentEvent e) {
        }
        
        /**
         * Starts a thread that finds entry suggestions
         * 
         * @param
         */
        private void findSuggestions() {
           	oldPartial = textField.getText();
			if(t != null && t.isAlive()) {
				Thread tmpT = t;
				t = null;
				if(tmpT != null) {
					tmpT.interrupt();
				}
				try {
					tmpT.join();
				} catch(Exception ie) {
					System.out.println(ie);
				}
			}
			t = new Thread(new SuggestionsFinder(oldPartial));
			t.start();
        }
        
        /**
		 * Finds entries that (partially) match contents of the text field of the
		 * suggest panel.
		 * Reason for extending Runnable: if the number of entries is very large,
		 * searching would interfere typing. 
		 * 
		 * NOT THREAD SAVE it reads the entries array from class CVEntryComponent
		 * 
		 * @author Micha Hulsbosch
		 *
		 */
		private class SuggestionsFinder implements Runnable {
    		
    		private String part;
    		
    		public SuggestionsFinder(String part) {
    			this.part = part;
    		}
    		
    		@Override
			public void run() {
				ArrayList<SimpleCVEntry> suggestions = new ArrayList<SimpleCVEntry>();
				Boolean suggestSearchMethodFlag = false;
				Boolean suggestSearchInDescFlag = false;
				Boolean suggestIgnoreCaseFlag = false;
				Boolean boolPref = Preferences
						.getBool("SuggestPanel.EntryContains", null);
				if (boolPref != null) {
					suggestSearchMethodFlag = boolPref.booleanValue();
				}
				boolPref = Preferences.getBool("SuggestPanel.SearchDescription", null);
				if (boolPref != null) {
					suggestSearchInDescFlag = boolPref.booleanValue();
				}
				boolPref = Preferences.getBool("SuggestPanel.IgnoreCase", null);

				if (boolPref != null) {
					suggestIgnoreCaseFlag = boolPref.booleanValue();
				}
				if (part != null && !part.equals("")) {
					int entriesIndex = 0;
					if (suggestIgnoreCaseFlag) {
						part = part.toLowerCase();
					}
					while (entriesIndex < entries.length
							&& !Thread.currentThread().isInterrupted()) {
						SimpleCVEntry next = entries[entriesIndex];
						String entryValue = next.getValue();
						if (suggestIgnoreCaseFlag) {
							entryValue = entryValue.toLowerCase();
						}
						if (suggestSearchMethodFlag) {
							if (entryValue.contains(part)) {
								suggestions.add(next);
							} else if (suggestSearchInDescFlag
									&& getDescription(next).contains(part)) {
								suggestions.add(next);
							}
						} else {
							if (entryValue.startsWith(part)) {
								suggestions.add(next);
							} else if (suggestSearchInDescFlag
									&& getDescription(next).contains(part)) {
								suggestions.add(next);
							}
						}
						entriesIndex++;
					}
					if (!Thread.currentThread().isInterrupted()) {
						SwingUtilities.invokeLater(new SuggestionsDisplayer(
								suggestions));
					}
				} else {
					if (!Thread.currentThread().isInterrupted()) {
						SwingUtilities.invokeLater(new SuggestionsDisplayer(
								new ArrayList<SimpleCVEntry>()));
					}
				}
			} 
    	}
		
		private String getDescription(SimpleCVEntry next) {
			String d = next.getDescription();
			if (d == null) {
				return "";
			} else {
				return d.toLowerCase();
			}
		}
		
		/**
		 * Displays the suggestions in the suggestEntryList
		 * 
		 * Should be invoked in the Event dispatching thread
		 * 
		 * @author Micha Hulsbosch
		 * 
		 */
    	private class SuggestionsDisplayer implements Runnable {
    		ArrayList<SimpleCVEntry> suggestions;
    		
    		public SuggestionsDisplayer(ArrayList<SimpleCVEntry> suggestions) {
    			this.suggestions = suggestions;
    			
    			if(showCVDescription){
    				while(entryTableModel.getRowCount()> 0){
    					entryTableModel.removeRow(entryTableModel.getRowCount()-1);
    				}
    			} else {
    				suggestEntryListModel.clear();
    			}
    			
    		}
    		
    		@Override
			public void run() {
    			Iterator<SimpleCVEntry> suggestionIterator = suggestions.iterator();
    			int suggestionIndex = 0;
    			while (suggestionIterator.hasNext()) {
    				SimpleCVEntry nextSuggestion = suggestionIterator.next();
    				if(showCVDescription){
    					entryTableModel.addRow(new Object[]{nextSuggestion, nextSuggestion.getDescription()});
        				if (nextSuggestion.getValue().equals(oldPartial)) {
        					entryTable.setRowSelectionInterval(suggestionIndex, suggestionIndex);
        				}
    				} else {
    					suggestEntryListModel.addElement(nextSuggestion);
        				if (nextSuggestion.getValue().equals(oldPartial)) {
        					suggestEntryList.setSelectedIndex(suggestionIndex);
        					suggestEntryList.ensureIndexIsVisible(suggestionIndex);
        				}
    				}
    				suggestionIndex++;
    				//System.out.println("Added to list: " + nextSuggestion);
    			}
    			
    			if(showCVDescription){
    				scrollIfNeededAutomatically();
    			}
    		}
    	}
    }
    // end of CVEntryComponent   

    /**
     * @author Micha Hulsbosch
     * @param cvEntryId
     */
	protected void showVideoForEntry(String cvEntryId) {
		if(mediaDisplayerHost != null) {
    		mediaDisplayerHost.discardMediaDisplayer();
    	}
		
		if(mediaDisplayerHost != null) {
			Rectangle inlineEditBoxBounds = getBounds();
			mediaDisplayerHost.hostMediaDisplayer(new Object[] {cvEntryId, annotation}, inlineEditBoxBounds);
		}   
	}

	@Override
	public void changedUpdate(DocumentEvent de) {
		if(attached) {
			paintSpellErrorUnderline(textArea);
		} else {
			paintSpellErrorUnderline(exttextArea);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent de) {
		if(attached) {
			paintSpellErrorUnderline(textArea);
		} else {
			paintSpellErrorUnderline(exttextArea);
		}
	}

	@Override
	public void removeUpdate(DocumentEvent de) {
		if(attached) {
			paintSpellErrorUnderline(textArea);
		} else {
			paintSpellErrorUnderline(exttextArea);
		}
	}
	
	public void setMediaDisplayerHost(MediaDisplayerHost mediaDisplayerHost) {
		this.mediaDisplayerHost = mediaDisplayerHost; 
	}
}

