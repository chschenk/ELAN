package mpi.eudico.client.annotator.transcriptionMode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.Pair;
import mpi.eudico.util.SimpleCVEntry;


/** A class that provides and configures components for  editing
 * annotation values, specially made for the transcription table 
 * in Transcription mode.
 *  
 * @author aarsom
 *
 */
@SuppressWarnings("serial")
public class TranscriptionTableEditBox extends JPanel implements ActionListener,
    MouseListener, KeyListener, ElanLocaleListener, DocumentListener {
	
    /** A logger to replace System.out calls. */
    private static final Logger LOG = Logger.getLogger(TranscriptionTableEditBox.class.getName());

    private JTextArea textArea = new JTextArea();    
    private JPopupMenu popupMenu;   
    private String oldText;
    private Annotation annotation;
    private TranscriptionTable table = null;
    private TranscriptionViewer viewer; 
    private boolean isUsingControlledVocabulary = false;
   
    private JMenuItem commitMI;
    private JMenuItem cancelMI;
    private JMenuItem mergeBeforeMI;
    private JMenuItem mergeNextMI;
    private JMenuItem deleteAnnotationMI;
    private JMenuItem editInAnnModeMI;
	private JMenuItem selectAllMI;
    private JMenuItem cutMI;
    private JMenuItem copyMI;
    private JMenuItem pasteMI;
    private JMenuItem changeColorMI;
    private JMenuItem nonEditableTierMI;
	private JMenuItem hideAllTiersMI;
	private JMenuItem showHideMoreMI;
    
    private CVEntryComponent cvEntryComp;
    
    private List<KeyStroke> keyStrokesList;
        
    private int caretPosition = -1;    
   
    private int maxHtForOtherColumn = 0;
    private boolean heightComputed = false;
    private Graphics g;
	private FontMetrics fm;
	private int fontHeight;
	
	private int currentRow;
    private int minRowHeight;
	private int columnWidth;
	
	private boolean deselectCommit = true;// global value - if this default value is changed- also change in EditingPanel  
    
    /**
     * this field can be either a JScrollPane(with JTextArea)
     *  or a JComboBox 
     */
    private JComponent editorComponent;
    //temp
    private Font uniFont = Constants.DEFAULTFONT;
    
    private boolean isEditing = false;
    
    private boolean commitChanges = false;
  
    /**
     * a focus listener that lets the textarea request the
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
     * Creates a instance of TranscriptionTableEditBox
     * 
     * @param viewer
     * @param table
     */
    public TranscriptionTableEditBox(TranscriptionViewer viewer, TranscriptionTable table) {
        this.table = table;
        this.viewer = viewer;    
        keyStrokesList = viewer.getKeyStrokeList();
        init();
    }
    
    /**
     * Initializes the components  
     */
    public void init() {    	  
        setLayout(new BorderLayout());        

        textArea.addMouseListener(this);
        textArea.setLineWrap(true);
        textArea.setMargin(new Insets(0,3,0,3));
        textArea.setWrapStyleWord(true);
        textArea.setBorder(new CompoundBorder(new LineBorder(Color.black, 1), new LineBorder(Color.white, 2)));
        
        add(textArea, BorderLayout.CENTER);
        textArea.getCaret().setVisible(true);    
        textArea.addKeyListener(this);
        textArea.addFocusListener(intFocusListener);
        addFocusListener(intFocusListener);
    
        textArea.getDocument().addDocumentListener(this);
        
		// remove the default behavior of TAB 
		Action doNothing = new AbstractAction() {
		    @Override
			public void actionPerformed(ActionEvent e) {
		        //do nothing
		    }
		};	
		textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
		                            "doNothing");
    }

    /**
     * Creates a popup menu.
     */
    public void createPopupMenu() {      
    	popupMenu = new JPopupMenu();   
    	   	
    	commitMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Commit"));     
        commitMI.addActionListener(this);
        popupMenu.add(commitMI);
        
        cancelMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Cancel"));       
        cancelMI.addActionListener(this);
        popupMenu.add(cancelMI); 
       
        editInAnnModeMI = new JMenuItem(ElanLocale.getString(ELANCommandFactory.EDIT_IN_ANN_MODE));      
        editInAnnModeMI.addActionListener(this); 
        popupMenu.add(editInAnnModeMI); 
        
        mergeBeforeMI = new JMenuItem(ElanLocale.getString(ELANCommandFactory.MERGE_ANNOTATION_WB));       
        mergeBeforeMI.addActionListener(this); 
        popupMenu.add(mergeBeforeMI);        
        
        mergeNextMI = new JMenuItem(ElanLocale.getString(ELANCommandFactory.MERGE_ANNOTATION_WN));      
        mergeNextMI.addActionListener(this); 
        popupMenu.add(mergeNextMI);
        
        deleteAnnotationMI = new JMenuItem(ElanLocale.getString(ELANCommandFactory.DELETE_ANNOTATION));      
        deleteAnnotationMI.addActionListener(this); 
        popupMenu.add(deleteAnnotationMI);        
        popupMenu.addSeparator();
        
        cutMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Cut"));
        cutMI.setActionCommand("cut");
        cutMI.addActionListener(this);
        popupMenu.add(cutMI);
        
        copyMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        copyMI.setActionCommand("copy");
        copyMI.addActionListener(this);
        popupMenu.add(copyMI);
        
        pasteMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Paste"));
        pasteMI.setActionCommand("paste");
        pasteMI.addActionListener(this);
        popupMenu.add(pasteMI);
        
        selectAllMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.SelectAll"));
        selectAllMI.setActionCommand("selectAll");
        selectAllMI.addActionListener(this);
        popupMenu.add(selectAllMI);
        
        popupMenu.addSeparator();   
        changeColorMI = new JMenuItem(ElanLocale.getString("TranscriptionTable.Label.ChangeColorForThisTier"));
		changeColorMI.addActionListener(this);
		
        nonEditableTierMI = new JMenuItem(ElanLocale.getString("TranscriptionTable.Label.NonEditableTier"));
		nonEditableTierMI.addActionListener(this);
		
		hideAllTiersMI = new JMenuItem(ElanLocale.getString("TranscriptionTable.Label.HideLinkedTiers"));		
		hideAllTiersMI.addActionListener(this);
		
		showHideMoreMI = new JMenuItem(ElanLocale.getString("TranscriptionTable.Label.ShoworHideTiers"));
		showHideMoreMI.addActionListener(this);
			
		popupMenu.add(changeColorMI);	
		popupMenu.add(nonEditableTierMI);	
		popupMenu.addSeparator();
		popupMenu.add(hideAllTiersMI);
		popupMenu.add(showHideMoreMI);	
		
		updatePopMenuShortcuts();
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
    
    public String getEditingCellValue() {
    	if(textArea != null){
    		return textArea.getText();
    	}
        return null;
    }

    /**
     * Overrides setFont(Font) in Component by also setting the font for the
     * textareas.
     *
     * @param font the Font to use
     */
    @Override
	public void setFont(Font font) {
     //   super.setFont(font);
        uniFont = font;        
      
        // setFont() is used at initializing superclass - textarea not yet instantiated
        if (textArea != null) {
        	 textArea.setFont(font);
        	if(heightComputed){        		
        		 heightComputed = false;
        		 recalculateRowHeight();
        		 textArea.repaint();
        	}
        }
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
		textArea.setText(oldText);// don't trim, otherwise it's difficult to remove spaces newlines etc.
	
		try {			
			if (forceOpenCV) {
				isUsingControlledVocabulary = false;
			} else {			
				isUsingControlledVocabulary = ((TierImpl) annotation.getTier()).getLinguisticType()
												   .isUsingControlledVocabulary();
			}			
		} catch (Exception e) {			
			isUsingControlledVocabulary = false;
		}
		
		if (!isUsingControlledVocabulary) {
			textArea.setEditable(true);
			
			if(caretPosition > 0 && caretPosition <= textArea.getText().length()){
				textArea.setCaretPosition(caretPosition);		
			} else {
				textArea.setCaretPosition(textArea.getText().length());
				caretPosition = -1;
			}
			//textArea.requestFocus();
		} else {
			textArea.setEditable(false);
			if (cvEntryComp == null) {
				cvEntryComp = new CVEntryComponent(JScrollPane.class);
			}
			cvEntryComp.setAnnotation(annotation);
		}
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
        return !oldText.equals(textArea.getText());
    }    

    /**
     * Resets elements and cleans up without applying any changes in the  value
     * of the annotation.  HB, 11 oct 01, changed from protected to public
     */
    public void cancelEdit() {
    	viewer.stopPlayer();
    	isEditing = false;
    	heightComputed = false;
        setVisible(false); 
        if (table != null) {
            table.editingCanceled(new ChangeEvent(this));            
        }
    }

   /**
    * Checks for modifications, applies the modification if any, resets ,
    * cleans  up a bit and decide whether to exit the edit mode 
    * 
    * @param exit, if true exit the edit mode
    */
    public void commitChanges() {     	
    	if (!isEditing || annotation == null) {
			return;
		}   
    	
    	commitChanges = true;
       
        Object extRef = null;
        String cveId = null;

        if (isUsingControlledVocabulary && (cvEntryComp != null)) {
        	final SimpleCVEntry selectedEntry = cvEntryComp.getSelectedEntry();
			if (selectedEntry != null) {
        		textArea.setText(cvEntryComp.getSelectedEntryValue());
	            
	            extRef = selectedEntry.getExternalRef();
	        	cveId = selectedEntry.getId();
        	}
        }
      
        // remove an ExternalCV reference by passing a null value.
        if (extRef == null && ((AbstractAnnotation) annotation).getExtRef() != null) {
        	extRef = new ExternalReferenceImpl(null, ExternalReference.CVE_ID);
        }
        
        String newText =  textArea.getText() ;  
        boolean modified = annotationModified();

        if (modified) {    
        	setVisible(false);      
        	
            Command c = ELANCommandFactory.createCommand((annotation.getTier() .getTranscription()),
                            ELANCommandFactory.MODIFY_ANNOTATION);
            Object[] args = new Object[] { oldText, newText, extRef, cveId };
            c.execute(annotation, args);           
        }  
        
        isEditing = false;
		heightComputed = false;	 
		commitChanges = false;

        if ( table != null) {  
//        	if( oldText.length() != newText.length()){        	
//    			recalculateRowHeight();
//    		} else if(newText.trim().length() == 0 && isUsingControlledVocabulary){
//    			// if no cv is set
//    			recalculateRowHeight();
//    		}
        	if( getEditorComponent() instanceof JScrollPane || oldText.length() != newText.length()){        	
    			recalculateRowHeight();
    		} 
            table.editingStopped(new ChangeEvent(this));              
        }
    }  
    
    /**
     * Checks the global preference whether de-selecting
     * edit box commits the changes
     * 
     * @return
     */
    public boolean isDeselectCommitChanges(){    	
    	Boolean val = Preferences.getBool("InlineEdit.DeselectCommits", null);
    	
    	if (val != null) {
    		deselectCommit = val.booleanValue();
        }
    	
    	return deselectCommit;
    }

    /**
     * Forwards the cut action to the <code>textArea</code> 
     */
    private void doCut() {      		
    	textArea.cut();     	
    	recalculateRowHeight();
    }

    /**
     * Forwards the copy action to the <code>textArea</code> 
     */
    private void doCopy() {    	
    	textArea.copy();    
    }

    /**
     * Forwards the paste action to the <code>textArea</code> 
     */
    private void doPaste() {  
    	textArea.paste();   
    	recalculateRowHeight();
    }
    
	/**
	 * Forwards the select all action to the <code>textArea</code> 
	 */
    private void doSelectAll() {
    	textArea.selectAll();
    }
    
    /**
      * Merges the given annotation with the annotation before it     
      */
    private void mergeWithAnnBefore() {  
    	Annotation ann = (Annotation) table.getValueAt(table.getCurrentRow()-1, table.getCurrentColumn());
    	caretPosition = ann.getValue().length() + textArea.getCaretPosition();        	
    	commitChanges();
    	if(((TierImpl)annotation.getTier()).hasParentTier()){    			
    		viewer.mergeBeforeAnn(annotation.getParentAnnotation());
    	}else {
    		viewer.mergeBeforeAnn(annotation);
    	}   
    }
    
    /**
     * Merges the given annotation with the annotation next to it
     */
    private void mergeWithNextAnn() {    	
   		caretPosition = textArea.getCaretPosition();
   		commitChanges();    		
   		if(((TierImpl)annotation.getTier()).hasParentTier()){    			
   			viewer.mergeNextAnn(annotation.getParentAnnotation());
   		}else {
   			viewer.mergeNextAnn(annotation);
   		}
    }
    
    private void commit(){
    	commitChanges();
        table.goToNextEditableCell();
    }
    
    /**
     * Deletes the annotation
     */
    private void deleteAnnotation() {
    	commitChanges();
    	viewer.deleteAnnotation(annotation);		
	}
    
    private void editInAnnotationMode(){
    	commitChanges();
    	if(annotation != null){    
    		viewer.getViewerManager().getMasterMediaPlayer().stop();
    		viewer.editInAnnotationMode();
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
        
		if (textArea.getText() == null || 
				textArea.getText().length() == 0) {
			selectAllMI.setEnabled(false);
		} else {
			selectAllMI.setEnabled(true);
		}
		
		if(annotation == null){
			editInAnnModeMI.setEnabled(false);
		} else {
			editInAnnModeMI.setEnabled(true);
		}			
		
		if(viewer.getMerge()){
			int leadColumn = table.getCurrentColumn();
			int leadRow = table.getCurrentRow();
			Object obj ;
			if(leadRow != 0){
				obj = table.getValueAt(leadRow-1, leadColumn);
				if(obj instanceof Annotation){
					if( annotation.getTier().equals(((Annotation)obj).getTier())){
						mergeBeforeMI.setEnabled(true);
					}else{
						mergeBeforeMI.setEnabled(false);
					}
				} else {
					mergeBeforeMI.setEnabled(false);
				}
			} else {
				mergeBeforeMI.setEnabled(false);
			}			
			
			if(leadRow < (table.getRowCount()-1)){
				obj = table.getValueAt(leadRow+1, leadColumn);
				if(obj instanceof Annotation){
					if( annotation.getTier().equals(((Annotation)obj).getTier())){
						mergeNextMI.setEnabled(true);
					}else{
						mergeNextMI.setEnabled(false);
					}
				} else {
					mergeNextMI.setEnabled(false);
				}
			} else {
				mergeNextMI.setEnabled(false);
			}
		} else {
			mergeBeforeMI.setEnabled(false);
			mergeNextMI.setEnabled(false);
		}
    }    
    
    private boolean checkForMergeBefore(){
    	if(viewer.getMerge()){
			int leadColumn = table.getCurrentColumn();
			int leadRow = table.getCurrentRow();
			Object obj ;
			if(leadRow != 0){
				obj = table.getValueAt(leadRow-1, leadColumn);
				if(obj instanceof Annotation){
					if( annotation.getTier().equals(((Annotation)obj).getTier())){
						return true;
					}else{
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}	
		} else {
			return false;
		}
    }
    
    private boolean checkForMergeAfter(){
    	if(viewer.getMerge()){
			int leadColumn = table.getCurrentColumn();
			int leadRow = table.getCurrentRow();
			Object obj ;
			if(leadRow < (table.getRowCount()-1)){
				obj = table.getValueAt(leadRow+1, leadColumn);
				if(obj instanceof Annotation){
					if( annotation.getTier().equals(((Annotation)obj).getTier())){
						return true;
					}else{
						return false;
					}
				} else {
						return false;
				}
			} else {
					return false;
			}
		} else {
			return false;
		}
    }
    
    public void showPopUp(java.awt.Component obj , int x, int y) {
    	if(popupMenu == null){
    		createPopupMenu();
    	}
		updatePopup();
		popupMenu.show(obj, x, y);
		popupMenu.setVisible(true);
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
        if (preferredComponent == JTextArea.class) {        	
                if (font != null) {
                    setFont(font);
                }
                if (size != null) {
                    setSize(size);
                }    
               editorComponent = textArea;         
        } else if (preferredComponent == JScrollPane.class) {
            if (isUsingControlledVocabulary) {
                if (cvEntryComp == null) {
                    cvEntryComp = new CVEntryComponent(preferredComponent);
                    cvEntryComp.setAnnotation(annotation);
                }                 
                if (font != null) {
                    cvEntryComp.setFont(font);
                }
                if (size != null) {
                    cvEntryComp.getEditorComponent().setSize(size);
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
    	if(isEditing){
    		return;
    	}
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
    
    @Override
	public void grabFocus(){
    	if (isUsingControlledVocabulary) {  
            cvEntryComp.grabFocus();                
        } else {
            editorComponent.requestFocus();
        }
    }
    
    private void updatePopMenuShortcuts(){
    	if(popupMenu == null){
    		return;
    	}
    	
    	ShortcutsUtil scu = ShortcutsUtil.getInstance();
    	
    	commitMI.setAccelerator(scu.getKeyStrokeForAction(ELANCommandFactory.COMMIT_CHANGES, ELANCommandFactory.TRANSCRIPTION_MODE)); 
        cancelMI.setAccelerator(scu.getKeyStrokeForAction(ELANCommandFactory.CANCEL_CHANGES, ELANCommandFactory.TRANSCRIPTION_MODE)); 
        editInAnnModeMI.setAccelerator(scu.getKeyStrokeForAction(ELANCommandFactory.EDIT_IN_ANN_MODE, ELANCommandFactory.TRANSCRIPTION_MODE));  
        mergeBeforeMI.setAccelerator(scu.getKeyStrokeForAction(ELANCommandFactory.MERGE_ANNOTATION_WB, ELANCommandFactory.TRANSCRIPTION_MODE ));   
        mergeNextMI.setAccelerator(scu.getKeyStrokeForAction(ELANCommandFactory.MERGE_ANNOTATION_WN, ELANCommandFactory.TRANSCRIPTION_MODE));  
        deleteAnnotationMI.setAccelerator(scu.getKeyStrokeForAction(ELANCommandFactory.DELETE_ANNOTATION, ELANCommandFactory.TRANSCRIPTION_MODE)); 
        nonEditableTierMI.setAccelerator(scu.getKeyStrokeForAction(ELANCommandFactory.FREEZE_TIER, ELANCommandFactory.TRANSCRIPTION_MODE));    
		hideAllTiersMI.setAccelerator(scu.getKeyStrokeForAction(ELANCommandFactory.HIDE_TIER, ELANCommandFactory.TRANSCRIPTION_MODE));   
    }
    
    public void updateShortCuts() {
    	keyStrokesList = viewer.getKeyStrokeList();
        updatePopMenuShortcuts();
	}
    
    /**
     * Menu items' ActionPerformed handling.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (e.getSource() == cancelMI) {
            cancelEdit();
        } else if (e.getSource() == commitMI) {
        	commitChanges();
        } else if (e.getSource() == editInAnnModeMI) {        	
        	editInAnnotationMode();
        } else if (command.equals("cut")) {
            doCut();
        } else if (command.equals("copy")) {
            doCopy();
        } else if (command.equals("paste")) {
            doPaste();
        } else if (command.equals("selectAll")) {
			doSelectAll();
		} else if (e.getSource() == mergeBeforeMI) {
			mergeWithAnnBefore();
		}else if (e.getSource() == mergeNextMI) {
			mergeWithNextAnn();
		} else if (e.getSource() == deleteAnnotationMI){
			deleteAnnotation();
		} else if(e.getSource() == changeColorMI){	
			//commitChanges();
			viewer.showChangeColorDialog(annotation.getTier().getName());
		} else if(e.getSource() == nonEditableTierMI){	
			commitChanges();			
			viewer.editOrNoneditableTier(annotation.getTier().getName());
		} else if(e.getSource() == hideAllTiersMI){		
			commitChanges();
			viewer.hideTiersLinkedWith(annotation.getTier().getName());				
		} else if( e.getSource() == showHideMoreMI) {	
			commitChanges();
			viewer.showHideMoreTiers();			
		} 
    }
    
	public void cvChanged(ControlledVocabulary cv) {
		if(cvEntryComp != null){
			cvEntryComp.cvChanged(cv);
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
			showPopUp(textArea, e.getX(), e.getY());
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

    @Override
	public void keyPressed(KeyEvent e) {
    	
    	KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);   
    	
    	if(ks == null || !keyStrokesList.contains(ks)){  
    		return;
    	}  
    	        
    	//e.consume();
    	// cancel changes
    	if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.CANCEL_CHANGES, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
            cancelEdit();
        }  		
		// move up
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MOVE_UP, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	e.consume();
        	commitChanges();
            table.goToEditableCellUp();
            //table.startEdit(e);           
        }  
		// move down
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MOVE_DOWN, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	e.consume();
        	commitChanges();
            table.goToEditableCellDown();
            //table.startEdit(e);
        } 
		//move left
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MOVE_LEFT, ELANCommandFactory.TRANSCRIPTION_MODE)) {        
        	e.consume();
        	commitChanges();
            table.goToEditableCellLeft();
            //table.startEdit(e);           
        } 		
		//move right
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MOVE_RIGHT, ELANCommandFactory.TRANSCRIPTION_MODE)) {        
        	e.consume();
        	commitChanges();
            table.goToEditableCellRight();
            //table.startEdit(e);           
        } 
		// play from start
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PLAY_FROM_START, ELANCommandFactory.TRANSCRIPTION_MODE)) {        
        	e.consume();
        	viewer.playIntervalFromBeginTime(annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary());
        } 
    	
    	// edit in annotation mode
        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.EDIT_IN_ANN_MODE, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	e.consume();
        	editInAnnotationMode();
        } 
       	// play the media for the time interval
        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PLAY_PAUSE, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	e.consume();
        	viewer.playInterval(annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary());
        } 
    	
    	//play Selection
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PLAY_SELECTION, ELANCommandFactory.TRANSCRIPTION_MODE)){
        	e.consume();
        	viewer.playSelection();
        }
    	
    	//play Selection
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.CLEAR_SELECTION, ELANCommandFactory.TRANSCRIPTION_MODE)){
        	e.consume();
        	viewer.clearSelection();
        }
    	
    	//play around Selection
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PLAY_AROUND_SELECTION, ELANCommandFactory.TRANSCRIPTION_MODE)){
        	e.consume();
        	viewer.playAroundSelection(annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary());
        }
    	
    	//go to one pixel backward
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PIXEL_LEFT, ELANCommandFactory.TRANSCRIPTION_MODE)){
        	e.consume();
        	viewer.goToOnepixelForwardOrBackward(ELANCommandFactory.PIXEL_LEFT, annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary());
        }
    	
    	//go to one pixel forward
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PIXEL_RIGHT, ELANCommandFactory.TRANSCRIPTION_MODE)){
        	e.consume();
        	viewer.goToOnepixelForwardOrBackward(ELANCommandFactory.PIXEL_RIGHT, annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary());
        }
    	
    	//go to one previous frame
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PREVIOUS_FRAME, ELANCommandFactory.TRANSCRIPTION_MODE)){
        	e.consume();
        	viewer.goToPreviousOrNextFrame(ELANCommandFactory.PREVIOUS_FRAME, annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary());
        }
    	
    	//go to one next frame
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.NEXT_FRAME, ELANCommandFactory.TRANSCRIPTION_MODE)){
        	e.consume();
        	viewer.goToPreviousOrNextFrame(ELANCommandFactory.NEXT_FRAME, annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary());
        }
    	
    	//go to one sec backward
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.SECOND_LEFT, ELANCommandFactory.TRANSCRIPTION_MODE)){
        	e.consume();
        	viewer.goToOneSecondForwardOrBackward(ELANCommandFactory.SECOND_LEFT, annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary());
        }
    	
    	//go to one sec forward
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.SECOND_RIGHT, ELANCommandFactory.TRANSCRIPTION_MODE)){
        	e.consume();
        	viewer.goToOneSecondForwardOrBackward(ELANCommandFactory.SECOND_RIGHT, annotation.getBeginTimeBoundary(), annotation.getEndTimeBoundary());
        }
    	
    	// delete annotation
        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.DELETE_ANNOTATION, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	e.consume();
        	deleteAnnotation();
        } 
        // merge with before
        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MERGE_ANNOTATION_WB, ELANCommandFactory.TRANSCRIPTION_MODE)) { 
    		e.consume();
    		if(checkForMergeBefore()){
    			mergeWithAnnBefore();
    		}    
    	} 
        // merge with Next
        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MERGE_ANNOTATION_WN, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	e.consume();
			if(checkForMergeAfter()){
				mergeWithNextAnn();
			}    
        } 
    	 // make tier editable/ non- editable
        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.FREEZE_TIER, ELANCommandFactory.TRANSCRIPTION_MODE)) { 
    		e.consume();
    		commitChanges();
    		viewer.editOrNoneditableTier(annotation.getTier().getName());
    	} 
    	
        //hide tiers
        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.HIDE_TIER, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	commitChanges();
    		viewer.hideTiersLinkedWith(annotation.getTier().getName());	
        } 
    	// copy
        else if (e.getKeyCode() == KeyEvent.VK_C && (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0){
        	e.consume();
        	doCopy();
        }
    	// paste
        else if (e.getKeyCode() == KeyEvent.VK_V && (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0){
        	e.consume();
        	doPaste();
        }
    	//cut
        else if (e.getKeyCode() == KeyEvent.VK_X && (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0){
        	e.consume();
        	doCut();
        }     
        // KB Confirm
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.COMMIT_CHANGES, ELANCommandFactory.TRANSCRIPTION_MODE)) { 
            e.consume();
            commitChanges();
            table.goToNextEditableCell();
        }  
    	
        // loop mode
        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.LOOP_MODE, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	e.consume();
        	viewer.toggleLoopMode();
        } 

//        // June 2010 capture the standard undo/redo events to prevent undo/redo being called in the enclosing program
//        // while the edit box is active. Ideally an UndoManager should be installed for the edit box.
//        // maybe better to use the user defined shortcuts for undo and redo
        else if (ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.UNDO, ELANCommandFactory.TRANSCRIPTION_MODE) || 
        		ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.REDO, ELANCommandFactory.TRANSCRIPTION_MODE) ) {  
        	// do nothing unless UndoManager is active 
        	e.consume();
        }    	
//        else {        
//        	// consume the event to prevent actions with keyboard shortcuts to be triggered?
//        	// no, that disables e.g. the backspace key on a mac
//        	//e.consume();        	
//        }	    	
    }

	/**
     * Stub
     *
     * @param e the key event
     */
    @Override
	public void keyReleased(KeyEvent e) {
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
     * Updates UI elements after a change in the selected Locale.
     */
    @Override
	public void updateLocale() {      
        commitMI.setText(ElanLocale.getString("InlineEditBox.Commit"));
        cancelMI.setText(ElanLocale.getString("InlineEditBox.Cancel"));
        cutMI.setText(ElanLocale.getString("InlineEditBox.Edit.Cut"));
        copyMI.setText(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        pasteMI.setText(ElanLocale.getString("InlineEditBox.Edit.Paste"));
    }
    
    /**
     * Initialize some values required 
     * calculating the height of the JTextArea
     */
    private void initializeHeightParameterSettings(){
    	g = textArea.getGraphics();
		fm = textArea.getFontMetrics(uniFont);
		fontHeight = fm.getAscent()+ fm.getDescent() + 3;
		currentRow = table.getEditingRow();
    	minRowHeight = table.getDefaultRowHeight()+10;	
    	maxHtForOtherColumn = 0;
    	
    	columnWidth = table.getCellRect(currentRow, table.getEditingColumn(), false).width - 6;
    }
    
    /**
     * Calculates the maximum height required for 
     * other cells in this row
     */
    private void calculateMaxHieghtForOtherCells(){    	
    	int longestTextInOtherColumn = 0;    	
		for(int i = 1; i< table.getColumnCount(); i++){
			if(i == table.getCurrentColumn()){
				continue;
			}			 			 
			Object c  = table.getValueAt(currentRow, i);
			if(c instanceof Annotation){
				Annotation ann = (Annotation)c;
				String value = ann.getValue();	
				if(value != null) {
					int length = value.length();
					if(length > longestTextInOtherColumn){
						longestTextInOtherColumn = length;						
						maxHtForOtherColumn = getHeightForthisCell(ann, i);							
					}
				 }
			 }
		}
    }
    
    /**
	 * Returns the height required for the
	 * given cell
	 * 
	 * @param row, rowIndex of the row which is to be
	 * 			   recalculated
	 */
	private int getHeightForthisCell(Annotation ann, int column){
		int fontHeight = 0;
		int rowHeightValue = 0;
		FontMetrics fm = null;
		
	 	Graphics g = getGraphics();
	 	 
		String value = ann.getValue();
		Font f = table.getFontForTier(ann.getTier().getName());
		if(f != null){
			f = new Font(f.getFontName(), f.getStyle(), table.getFontSize());
			fm = getFontMetrics(f);
		} else {		
			f = new Font(table.getFont().getFontName(), table.getFont().getStyle(), table.getFontSize());
			fm = getFontMetrics(f);
		}
		int height = fm.getAscent()+ fm.getDescent() + 3;
		if(height > fontHeight){
			 fontHeight = height;
		}		
		double width = fm.getStringBounds(value, g).getWidth() + 10; 
		
		int newRowHeight = 0;
		if(width > columnWidth){      
			 int y = (int)(width /columnWidth);						 				 
			 newRowHeight = (1 + y) * fontHeight;						 
		} else {
			newRowHeight = fontHeight;
		}	
		
		if(newRowHeight > rowHeightValue){
			 rowHeightValue = newRowHeight;
		}	
		if(rowHeightValue > minRowHeight){
			return rowHeightValue+10; 	
		 }else {
			 return minRowHeight;  
		 }
	}
	
	/**
	 * Calculates the height for the textArea
	 * 
	 * @return
	 */
	private int calculateNewHeight(){	
		double width = fm.getStringBounds(textArea.getText(), g).getWidth() + 10; 		
		int newRowHeight = 0;
		if(width > columnWidth){      
			int y = (int)(width /columnWidth);						 				 
			newRowHeight = (1 + y) * fontHeight;						 
		} else {
			newRowHeight = fontHeight;
		}
		
		return newRowHeight;
	}
	    
    /**
     * Updates the size of the editbox
     */
    private void recalculateRowHeight(){   
    	if(table == null){
    		return;
    	}
    	if(table.isEditing()){     	
    		if(!heightComputed){
    			initializeHeightParameterSettings();
    			calculateMaxHieghtForOtherCells();
        		heightComputed = true;    
    		}	    		
    		int currentHeight = table.getRowHeight(currentRow);
    		int height = calculateNewHeight();
    		
    		if(maxHtForOtherColumn > height){
    			if(currentHeight > maxHtForOtherColumn) {
    				table.setRowHeight( currentRow, maxHtForOtherColumn);
    			}
        	} else if( minRowHeight <  height){        		
    			table.setRowHeight( currentRow, height+10); 
    		} else if( height < minRowHeight){    
    			if(currentHeight > minRowHeight){
    				table.setRowHeight( currentRow, minRowHeight);
    			}
    		} 
    	}
	}
    
	@Override
	public void insertUpdate(DocumentEvent e) {	
		recalculateRowHeight();
	}
	
	@Override
	public void removeUpdate(DocumentEvent e) {		
		recalculateRowHeight();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {	
	}
	
	/**
     * A class that provides a component for the selection of an entry from  a ControlledVocabulary.<br>
     * The current possible delegate components are a JScrollPane containing
     * a JList
     *
     * @author Han Sloetjes
     */
    class CVEntryComponent {
        /** the list containing the cv entries */
        private JList entryList;

        /** the model for the list */
        private DefaultListModel entryListModel;

        /** the scrollpane for the list */
        private JScrollPane scrollPane;

        /** moudelistener for bringing up the popup menu */
        private MouseListener popupListener;

        /** mouse listener that handles a double click on a list entry */
        private MouseListener doubleClickListener;
        
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
            if (delegate.getClass() == compClass) {
                return;
            }
       
            if (entryList == null) {
                   initComponents(compClass);
            }

            delegate = scrollPane;
            entryListModel.clear();
            fillModel(true);
        }

        /**
         * Tries to ensure that the selected item is visible in the
         * scrollpane's viewport. Applies only to the JList component.
         */
        public void ensureSelectionIsVisible() {
            if (delegate instanceof JScrollPane && entryList != null) {
                entryList.ensureIndexIsVisible(entryList.getSelectedIndex());
            }
        }
        
        public ControlledVocabulary cvChanged( ControlledVocabulary cv) {
        	ControlledVocabulary currentCV = null;
    	
        	if (this.annotation != null) {
        		TierImpl tier = (TierImpl) this.annotation.getTier();
        		TranscriptionImpl trans = tier.getTranscription();
        		currentCV = trans.getControlledVocabulary(tier.getLinguisticType()
                              .getControlledVocabularyName());                
        	}
        	if(currentCV != null && currentCV.equals(cv)){
        		fillModel(true);
        		commitChanges();
        		table.startEdit(null);
        	}
        	return null;
        }

        /**
         * Initializes either a list in a scrollpane (with a popup menu etc) or
         * a combo box.  Adds listeners.
         *
         * @param component the type of component to use for editing
         */
        private void initComponents(Class component) {           
                if (entryList == null) {
                    entryListModel = new DefaultListModel();
                    entryList = new JList(entryListModel);
                    entryList.setFont(TranscriptionTableEditBox.this.getFont());
                    entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    scrollPane = new JScrollPane(entryList);
                    
                    if (popupMenu == null) {
            			createPopupMenu();
            		}
                    
                    popupListener = new MouseAdapter() {
                    	@Override
						public void mousePressed(MouseEvent e) {
                    		if (SwingUtilities.isRightMouseButton(e) || 
                                    	e.isPopupTrigger()) {
                    			showPopUp(entryList, e.getX(), e.getY());
                                    }
                                }
                            };

                    doubleClickListener = new MouseAdapter() {
                                @Override
								public void mouseClicked(MouseEvent e) {
                                    if (e.getClickCount() > 1) {
                                        commit();
                                    }
                                }
                            };

                    entryList.addMouseListener(popupListener);
                    entryList.addMouseListener(doubleClickListener);

                    entryList.addKeyListener(new KeyAdapter(){
                        @Override
						public void keyPressed(KeyEvent e) {
                           	if (entries != null) {
                           		int code = e.getKeyCode();
                           		for (SimpleCVEntry cve : entries) {
                           			if (cve.getShortcutKeyCode() == code) {
                           				if (delegate == scrollPane) {
                           					entryList.setSelectedValue(cve, false);
                           				} 
                           				commit();
                           				e.consume();
                           				break;
                           			}
                           		}
                           	}
                           	
                           	
                           	if(!e.isConsumed()){
                           		TranscriptionTableEditBox.this.keyPressed(e);
                           	}
                           	
                           	
                        }
                    });
                    entryList.addListSelectionListener(new ListSelectionListener(){
                    	@Override
						public void valueChanged(ListSelectionEvent lse) {
							CVEntryComponent.this.ensureSelectionIsVisible();
                    	}
                    });
                    delegate = scrollPane;
                }
    		}
        

        /**
         * Sets the font for the entry list component.
         *
         * @param f the font
         */
        public void setFont(Font f) {
             if (delegate == scrollPane) {
                entryList.setFont(f);
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
        		if (langIndex < 0) {
        			langIndex = cv.getDefaultLanguageIndex();
        		}
            	// reload local CV's anyway
            	if (cv != oldCV || !(cv instanceof ExternalCV)) {
            		entries = cv.getSimpleEntries(langIndex);
                    if (entryListModel != null) {
                        entryListModel.clear();
                    }                  
                    fillModel(true);
            	} else {
            		// else reuse existing list, currently only for external CV because there is 
            		// no notification of changes in local CV's yet
                    fillModel(false);
            	}               
            } else { // cv == null
            	entries = new SimpleCVEntry[]{};
                if (entryListModel != null) {
                    entryListModel.clear();
                }
                fillModel(false);
            }
        }

        /**
         * Fills the model of either the combo box or the list with the entries
         * of the current Controlled Vocabulary.
         */
        private void fillModel(boolean reload) {
            String value = null;

            if (annotation != null) {
                value = annotation.getValue();
            }

            if (delegate == scrollPane) {
            	if (reload) {
	                for (int i = 0; i < entries.length; i++) {
	                    entryListModel.addElement(entries[i]);
	
	                    if ((value != null) && value.equals(entries[i].getValue())) {
	                        entryList.setSelectedIndex(i);
	                    }
	                }
            	} else {
            		// select the current value
            		if (value != null) {
            			SimpleCVEntry entry;
            			for (int i = 0; i < entryListModel.size(); i++) {
            				entry = (SimpleCVEntry) entryListModel.getElementAt(i);
            				if (value.equals(entry.getValue())) {
            					entryList.setSelectedIndex(i);
            					break;
            				}
            			}
            		}
            	}   
                
            	/* FIX ME : temporary fix to avoid committing the default selected value
                            to the annotation while recursive annotations are created */
            	
//            	if ((entries.length > 0) && (entryList.getSelectedIndex() < 0)) {
//            		entryList.setSelectedIndex(0);
//            	}            	
            	entryList.setSelectedIndex(-1);     
            	
            } 
        }

        /**
         * Tries to grant the focus to the delegate component.
         */
        public void grabFocus() {
            if (delegate == scrollPane) {
                entryList.requestFocus();
                entryList.ensureIndexIsVisible(entryList.getSelectedIndex());
            } 
        }

        /**
         * Returns the currently selected entry value.
         *
         * @return the currently selected entry value or null
         */
        public String getSelectedEntryValue() {
            String value = null;

            if (delegate == scrollPane) {
                if (entryList.getSelectedValue() != null) {
                    value = ((SimpleCVEntry) entryList.getSelectedValue()).getValue();
                }
            } 

            return value;
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
            } 

            return value;
        }
    }
}
