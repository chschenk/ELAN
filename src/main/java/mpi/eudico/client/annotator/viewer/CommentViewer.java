package mpi.eudico.client.annotator.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.global.MenuAction;
import mpi.eudico.client.annotator.comments.CommentEnvelope;
import mpi.eudico.client.annotator.comments.CommentManager;
import mpi.eudico.client.annotator.comments.CommentSearchDialog;
import mpi.eudico.client.annotator.comments.CommentTable;
import mpi.eudico.client.annotator.comments.CommentTableModel;
import mpi.eudico.client.annotator.comments.CommentsFilterDialog;
import mpi.eudico.client.annotator.comments.CommentsSettingsDialog;
import mpi.eudico.client.annotator.comments.DefaultCommentTableModel;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.server.corpora.util.ACMEditableObject;

@SuppressWarnings("serial")
public class CommentViewer extends  AbstractViewer
	implements ACMEditListener,	ActionListener, ElanLocaleListener, ClientLogger,
			   ListDataListener, ListSelectionListener, ACMEditableObject, MultiTierViewer,
			   KeyListener, MouseListener {
	
	/** Potentially disable the use of the DWAN web service by removing it from the GUI. */
	public final static boolean USE_WEB_SERVICE = true;
	public final static boolean DEBUG = CommentManager.DEBUG;
	
	private static Color readonlyColor = Constants.LIGHT_YELLOW; // light yellow
	private static Color modifiableColor = Color.WHITE;
	
	private static Color filteredLightColor = new Color(208, 240, 255); // light blue
	private Color unfilteredLightColor;
	private static Color filteredDarkColor = new Color(0, 0, 255); // blue

	private Transcription transcription;
	private CommentManager commentManager;
	
	private Tier activeTier;
	private Annotation activeAnnotation;
	private JPanel rightPanel;
	private DefaultCommentTableModel tableModel;
	private CommentTable commentTable;
	private JSplitPane splitPane;
	/** The text from one of the comments, for editing */
	private JTextArea text;
	private JButton addCommentButton;
	private JButton deleteCommentButton;
	private JButton changeCommentButton;
	private JButton filterButton;
	private JButton logInOutButton;
	private JButton menuButton;
	private JPopupMenu popupMenu;
	private MenuAction addCommentMA;
	private MenuAction changeCommentMA;
	private MenuAction deleteCommentsMA;
	private MenuAction filterCommentsMA;
	private MenuAction toMailMA;
	private MenuAction toClipboardMA;
	private MenuAction fromClipboardMA;
	private MenuAction searchMA;
	private MenuAction settingsMA;
	private Set<String> visibleTiers;
	private int totalNumberOfTiers;

	private long fileWatcherInterval = 10L * 1000L * 60L;	// 10 minutes in msec
	private Thread fileWatcherThread;
	private long approximateNextWakeupTime;
    private Semaphore updateCheckCanBeEnqueued;	// Don't do two update scans at the same time
    
	private CommentEnvelope commentOfText;

	private RowFilter<CommentTableModel, Integer> stringFilter;
	private RowFilter<CommentTableModel, Integer> tierFilter;

	private String stringFilterString;
	private boolean stringFilterCaseSensitive = true;

	protected long rePopUpMillis;
	private DocumentListener documentListener;

	public CommentViewer(TranscriptionImpl transcription) {
		super();
		this.transcription = transcription;
		visibleTiers = new HashSet<String>();
		updateCheckCanBeEnqueued = new Semaphore(1);
		commentManager = new CommentManager(transcription, this);
		
		initComponents();
		updateList();
		updateGUI();
		
		startFileWatcherThread(transcription.getName());
	}

	private void initComponents() {
		// The Table goes left
		tableModel = new DefaultCommentTableModel(commentManager);
		commentTable = new CommentTable(tableModel);
		newTierFilter();
		unfilteredLightColor = commentTable.getBackground();
		applyPreferences();
		
//		// TODO: Some columns should truncate at the left if the column is too narrow.
//		// Code examples that I have found are more complicated than it is worth.

		JScrollPane scrollCommentList = new JScrollPane(commentTable);
		// Listen for mouse events both on the table and the empty space outside it.
		commentTable.setScrollPane(scrollCommentList);
		commentTable.addListeners(this);
		
		final int NUM_BUTTONS_X = 3;
				
		// The text area and the buttons go right
		rightPanel = new JPanel();
		rightPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(2, 2, 2, 2);
				
		JScrollPane scrollText = createScrollTextArea();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = NUM_BUTTONS_X;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		rightPanel.add(scrollText, gbc);
		
		// The menu you get when you click a menu button
		popupMenu = new JPopupMenu();
		
		toMailMA = new ToMailMenuAction("CommentViewer.ToMail");
		popupMenu.add(toMailMA);
		
		toClipboardMA = new ToClipboardMenuAction("CommentViewer.ToClipboard");
		popupMenu.add(toClipboardMA);
		
		fromClipboardMA = new FromClipboardMenuAction("CommentViewer.FromClipboard");
		popupMenu.add(fromClipboardMA);
		
		searchMA = new SearchMenuAction("CommentViewer.Search");
		popupMenu.add(searchMA);
		
		settingsMA = new SettingsMenuAction("CommentViewer.Settings");
		popupMenu.add(settingsMA);
		
		// Add the same menu as a popop menu
		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};
		rightPanel.addMouseListener(mouseListener); // active in between the buttons
		text.addMouseListener(mouseListener);
				
		// The block with the buttons
		addCommentMA = new AddCommentAction("CommentViewer.NewComment");
		addCommentButton = new JButton(addCommentMA);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		rightPanel.add(addCommentButton, gbc);

		changeCommentMA = new ChangeCommentAction("CommentViewer.ChangeComment");
		changeCommentButton = new JButton(changeCommentMA);
		gbc.gridx = 1;
		gbc.gridy = 1;
		rightPanel.add(changeCommentButton, gbc);

		deleteCommentsMA = new DeleteCommentsAction("CommentViewer.DeleteComment");
		deleteCommentButton = new JButton(deleteCommentsMA);
		gbc.gridx = 2;
		gbc.gridy = 1;
		rightPanel.add(deleteCommentButton, gbc);
				
		filterCommentsMA = new FilterCommentsAction("CommentViewer.Filter");
		filterButton = new JButton(filterCommentsMA);
		gbc.gridx = 0;
		gbc.gridy = 2;
		rightPanel.add(filterButton, gbc);
				
		if (USE_WEB_SERVICE) {
			logInOutButton = new JButton("Log In");
			logInOutButton.addActionListener(this);	
			gbc.gridx = 1;
			gbc.gridy = 2;
			rightPanel.add(logInOutButton, gbc);
		}
				
		// Pop up a menu if the user clicks on this button (not on release!)
		menuButton = new JButton("Other...");	
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		rightPanel.add(menuButton, gbc);
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				/*
				 * Clicking on the button a second time will first pop down the
				 * menu, then immediately call this mousePressed() method. To
				 * avoid popping the menu right up again, require a few ms of
				 * time to have passed first. When it is allowed is set in
				 * rePopUpMillis.
				 * (This is a bit of a hack...)
				 */
				if (System.currentTimeMillis() < rePopUpMillis) {
					popupMenu.setVisible(false);
					rePopUpMillis = 0;
				} else {
	            	Component c = e.getComponent();
	            	popupMenu.show(c, 0, c.getHeight());
				}
			}
		};

		menuButton.addMouseListener(ml);
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				rePopUpMillis = System.currentTimeMillis() + 3;
			}
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}});
		
		// A menu bar works better, but looks weird, in the middle of the GUI...
//		JMenuBar menuBar = new JMenuBar();
//		JMenu menu = new JMenu("Other...");
//		menuBar.add(menu);
//		menu.add(toMailMA);
//		menu.add(toClipboardMA);
//		menu.add(fromClipboardMA);
//		menu.add(searchMA);
//		menu.add(settingsMA);		
//		rightPanel.add(menuBar, gbc);
				
		// The split goes in the middle - HORIZONTAL_SPLIT is counter-intuitive!
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollCommentList, rightPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setBorder(null);
		splitPane.setDividerSize(6);
    	splitPane.setContinuousLayout(true);
		
		// Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(100, 50);
		scrollCommentList.setMinimumSize(minimumSize);
		rightPanel.setMinimumSize(minimumSize);
		
		setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		add(splitPane, gbc);
		
		updateLocale();
	}

	/**
	 * Create a scroll pane with a text area, with all modifications we like
	 * for editing the text of a comment.
	 * <p>
	 * These include a tooltip and a shortcut to commit the changes.
	 * 
	 * @return the scroll pane
	 */
	private JScrollPane createScrollTextArea() {
		text = new JTextArea();
		text.setLineWrap(true);
		text.setToolTipText(ElanLocale.getString("CommentViewer.TextToolTip"));
		documentListener = new RedTextDocumentListener();
		// Change the colour of the text when it has been changed
		text.getDocument().addDocumentListener(documentListener);
		// Create a key (Command-Enter) to change or add the comment
		InputMap inputMap = text.getInputMap();
		ActionMap actionMap = text.getActionMap();
		// Command+Enter or Control+Enter
		KeyStroke key = KeyStroke.getKeyStroke(
				KeyEvent.VK_ENTER,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		// inputMap.put(key, new ChangeOrAddCommentAction()); // shortcut of the below
		inputMap.put(key, "change-or-add-comment");
		actionMap.put("change-or-add-comment", new ChangeOrAddCommentAction());
			
		JScrollPane scrollText = new JScrollPane(text);
		return scrollText;
	}
	
	/**
	 * Set the preferred column widths based on the Preferences.
	 * Also store the order that the user dragged them into.
	 * <p>
	 * <b>Note: This uses the GUI names of the columns in the preferences,
	 * which depend on the language.</b> 
	 */
	private void applyPreferences() {
		commentTable.applyPreferences("CommentViewer", transcription);
	}
	
	/**
	 * Save the preferred column widths and order to preferences.
	 */
	private void savePreferences() {
		commentTable.savePreferences("CommentViewer", transcription);
	}
	
	public CommentManager getCommentManager() {
		return commentManager;
	}
	
	/**
	 * Some buttons and menu items work via Actions, some go through here.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == null) {
			// should not happen;
			// guard against logInOutButton being null.
		} else if (source == logInOutButton) {
			if (commentManager.webClientIsLoggedIn()) {
				commentManager.logoutWebClient();
			} else {
				loginWebClient();
			}
			updateGUI();
		}
	}
	
	/**
	 * Perform a login of the DWAN Web client.
	 * <p>
	 * We need mutual exclusion with the update thread, because logging in
	 * implies a reload from the server, and that should not interfere with
	 * reloads from the update thread.
	 * <p>
	 * Otherwise this is a possible scenario:
	 * <ul>
	 * <li>the user hits the login button
	 * <li>a modified comment is detected on the server and the user is asked
	 * for a decision via a modal dialog
	 * <li>the dialog sits in a (modal) event loop while the user ponders the
	 * situation
	 * <li>the update thread's time expires and it puts an update check on the
	 * event queue (invokeLater())
	 * <li>the GUI thread can ~immediately perform the action because it's in an
	 * event loop
	 * </ul>
	 * This is a case where processing can become interleaved just because there
	 * is a modal dialog (where one would not expect anything else to be
	 * happening simultaneously on the same thread). Without the modal dialog this
	 * could not have happened.
	 */
	private void loginWebClient() {
		boolean success = false;
		try {
			updateCheckCanBeEnqueued.acquire();
			success = commentManager.loginWebClient();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			updateCheckCanBeEnqueued.release();
		}
		if (!success) {
			JOptionPane.showMessageDialog(logInOutButton,
					ElanLocale.getString("CommentViewer.LoginFailed"));
		}
	}
	
	/**
	 * Set the selected comment.
	 * Converts from model to view index.
	 * The selection in the time line viewer will also be updated
	 * (that happens only on a double click or space/return on the keyboard).
	 * @param modelRow
	 */
	protected int setSelectedIndex(int modelRow) {
		int viewRow = commentTable.convertRowIndexToView(modelRow);
		commentTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
		return viewRow;
	}
	
	/**
	 * Scroll a specified cell of a table into view.
	 * @param table
	 * @param viewRowIndex
	 * @param colIndex
	 */
	public static void scrollToVisible(JTable table, int viewRowIndex, int viewColIndex) {
		table.scrollRectToVisible(table.getCellRect(viewRowIndex, viewColIndex, true));
	}

	/**
	 * Select a given row (model coordinate) and scroll it into view.
	 * 
	 * @param modelRow
	 */
	public void showTableRow(int modelRow) {
		int viewRow = setSelectedIndex(modelRow);
		// Try to scroll it into view
		scrollToVisible(commentTable, viewRow, 0);
	
	}

	/**
	 * Returns the selected rows in the comment table.
	 * All indices are converted from view index to model index.
	 */
	protected int[] getSelectedRows() {
		int selected[] = commentTable.getSelectedRows();
		for (int i = 0; i < selected.length; i++) {
			selected[i] = commentTable.convertRowIndexToModel(selected[i]);
		}
		return selected;
	}
	
	/*
	 * React to a new set of visible tiers (the ones that are chosen via the MultiTierControlPanel
	 * on the left of the timeline).
	 * We will only show comments for tiers that are shown (and comments that are not
	 * associated with any particular tier).
	 */
	private void newTierFilter() {
		/*
		 * If there are no invisible tiers, don't bother to set a real filter.
		 * It's all visible anyway.
		 */
		if (visibleTiers.size() >= totalNumberOfTiers) {
			tierFilter = null;
			setRowFilter();
			return;
		}

		RowFilter<CommentTableModel, Integer> filter = new RowFilter<CommentTableModel, Integer>() {

			@Override
			public boolean include(
					javax.swing.RowFilter.Entry<? extends CommentTableModel, ? extends Integer> entry) {
				int index = entry.getIdentifier();
				CommentEnvelope e = entry.getModel().getComment(index);
				String tierName = e.getTierName();
				
				if (tierName == null || tierName.isEmpty()) {
					return true;
				}
					
				return visibleTiers.contains(tierName);
			}
		};
		tierFilter = filter;
		setRowFilter();
	}
	
	/**
	 * The user can set a filter based on a String.
	 * It is considered as a Regex, and it is applied to all columns.
	 * 
	 * If the string is not a valid regex, use it as a literal string.
	 * 
	 * @param match
	 */
	public void setRegexFilter(String match, boolean caseSensitive) {
		RowFilter<CommentTableModel, Integer> rf = null;
		stringFilterString = match;
		stringFilterCaseSensitive = caseSensitive;
		
		if (match != null && !match.isEmpty()) {
			// Unfortunately, we can't make a regex RowFilter from a Pattern, only from a String.
			// Therefore we need to prepend the string with the equivalent of some flags.
			// i => CASE_INSENSITIVE, u => UNICODE_CASE.
			// Unfortunately, there is no such letter for Pattern.LITERAL.
			// Using Pattern.quote() comes close but is less efficient.
			String flags = caseSensitive ? "" : "(?iu)";
			try {
			    rf = RowFilter.regexFilter(flags + match);
			} catch (PatternSyntaxException e) {
				try {
					//If current expression doesn't parse, just use it as a literal string.
					rf = RowFilter.regexFilter(flags + Pattern.quote(match));
				} catch (PatternSyntaxException e2) {
					rf = null;
				}
			}
		}
		
		if (rf != null) {
			commentTable.setBackground(filteredLightColor);
			filterButton.setForeground(filteredDarkColor);
		} else {
			commentTable.setBackground(unfilteredLightColor);
			filterButton.setForeground(null);
		}
		
		stringFilter = rf;
		setRowFilter();
	}
	
	/**
	 * To be called every time one of the filters changes.
	 * This method combines them and applies the result to the table.
	 */
	private void setRowFilter() {
		List<RowFilter<CommentTableModel, Integer>> filters =
				new ArrayList<RowFilter<CommentTableModel, Integer>>(2);
		
		if (tierFilter != null) {
			filters.add(tierFilter);
		}
		if (stringFilter != null) {
			filters.add(stringFilter);
		}
		if (filters.isEmpty()) {
			commentTable.setRowFilter(null);
		} else if (filters.size() == 1) {
			commentTable.setRowFilter(filters.get(0));
		} else {
			commentTable.setRowFilter(RowFilter.andFilter(filters));
		}
	}
	
	private void deActivateComment() {
        commentOfText = null;
    	text.setText("");
    	text.setBackground(modifiableColor);	        	
    	text.setEditable(true);
    }
    
	private void activateCommentHalfway() {
		final ListSelectionModel selectionModel = commentTable.getSelectionModel();
		int index = selectionModel.getLeadSelectionIndex();

		if (index >= 0 && index < commentTable.getRowCount() &&
				selectionModel.isSelectedIndex(index)) {
			index = commentTable.convertRowIndexToModel(index);
			activateCommentHalfway(index);
			return;
		}
		// The lead selection index may be deselected if it was just command-clicked.
		// That will deselect the row but not change the lead selection index.
		// If there is no selection, it is difficult to keep track of which comment the
		// text belongs to. An index may get out of date, if there are comments inserted
		// or deleted. Even a pointer may point to a comment that was already removed.
		// So try to be safe by simply erasing the text from the field.
		deActivateComment();
	}
	
	/**
	 * This is somewhat similar to activateComment(), but less forceful.
	 * Intended to be used when a comment is merely selected. <br/>
	 * It copies the text to the edit box, but not the other activation
	 * things.
	 * <p>
	 * If the comment is read-only, it will get a distinctive background color
	 * and the text box is made uneditable.
	 * 
	 * @param index in terms of our comment list (i.e. the model).
	 */
	private boolean activateCommentHalfway(int index) {
		if (index >= 0) {
			index = Math.min(index, commentTable.getModel().getRowCount() - 1);

	        CommentEnvelope comment = commentManager.get(index);
	        if (comment.isReadOnly()) {
	        	text.setBackground(readonlyColor);
	        	text.setEditable(false);
	        } else {
	        	text.setBackground(modifiableColor);	        	
	        	text.setEditable(true);
	        }
	        commentOfText = comment;
	        text.setText(comment.getMessage());
	        
	        return true;
		}
		
		return false;
	}

	/**
	 * Activate the currently selected comment, if any.
	 */
	private void activateComment() {		
		final ListSelectionModel selectionModel = commentTable.getSelectionModel();
		int index = selectionModel.getLeadSelectionIndex();
		
		if (index >= 0 && selectionModel.isSelectedIndex(index)) {
			index = commentTable.convertRowIndexToModel(index);
			activateComment(index);
			return;
		}
		deActivateComment();
	}

	/**
	 * Activate a comment upon double click on its entry in the comment list,
	 * or hitting the space or enter key.
	 * This entails copying its text to the text box;
	 * additionally the selection is set to the period of the comment.
	 * If an annotation is given, select the annotation.
	 * If a tier is given, select it.
	 * 
	 * (to show the selection in the comment list, call showTableRow()).
	 * @param index model coordinate
	 */
	public void activateComment(int index) {
		index = Math.min(index, commentTable.getModel().getRowCount() - 1);

        CommentEnvelope comment = commentManager.get(index);        
        commentOfText = comment;
        
        text.setText(comment.getMessage());
        
        ViewerManager2 vm = getViewerManager();
        
        // Set as much as possible that we can, given the URI.
        // One can have conflicting annotations, tiers, and time periods active,
        // (as long as the time is selected after the annotation),
        // so there is no reason not to just select everything here. 
        String id;
//        Annotation a = null;
//        if ((id = comment.getAnnotationID()) != null) { 
//        	a = transcription.getAnnotationById(id);
//        }
//    	vm.getTimeLineViewer().setActiveAnnotation(a);

    	// Onno first thought this bit was too forceful:
    	if (true) {
	        if ((id = comment.getTierName()) != null) {
	        	Tier tier = transcription.getTierWithId(id);
	        	if (tier != null) {
	        		vm.getMultiTierControlPanel().setActiveTier(tier);
	        	}
	        }		    
    	}

    	vm.getSelection().setSelection(comment.getStartTime(), comment.getEndTime());
    	
        // that doesn't scroll the selection into view...
    	// but this will:
    	vm.getMasterMediaPlayer().setMediaTime(comment.getStartTime());
	}
	
	@Override // ACMEditListener interface
	public void ACMEdited(ACMEditEvent e) {
		//LOG.info(e.toString());

		// If an annotation has been removed, also remove the comment about it.
		if (e.getOperation() == ACMEditEvent.REMOVE_ANNOTATION) {
//			Object o = e.getModification();
//			if (o instanceof Annotation) {
//				Annotation a = (Annotation)o;
//				String aid = a.getId();
//				
//				if (aid != null) {				
//					int len = commentManager.getList().size();
//					for (int i = 0; i < len; i++) {
//						CommentEnvelope c = commentManager.get(i);
//						String cid = c.getAnnotationID();
//						
//						if (aid.equals(cid)) {
//							// Found a comment that refers to the annotation.
//							// Ask if it should be removed.
//							setSelectedIndex(i);
//							int reply = JOptionPane.showConfirmDialog(null,
//									//"This comment refers to the removed annotation.\nRemove it?",
//									ElanLocale.getString("CommentViewer.RemovedAnnotation"),
//									ElanLocale.getString("Message.Warning"),
//				                    JOptionPane.YES_NO_OPTION);
//							if (reply == JOptionPane.YES_OPTION) {
//								commentManager.undoableRemove(i);
//								i--; // compensate i++
//								len--;
//							}
//						}
//					}
//				}
//			}			
		}
	}

	@SuppressWarnings("unused")
	private void logURI()
	{
        long selectionBeginPos = getSelectionBeginTime();
        long selectionEndPos = getSelectionEndTime();
        LOG.info("selection begin = " + Long.toString(selectionBeginPos) + ", end = " + Long.toString(selectionEndPos));
        ViewerManager2 vm = getViewerManager();
        if (vm != null) {
	        Transcription transcription = vm.getTranscription();
	        long crossHairTime = vm.getMasterMediaPlayer().getMediaTime();
	        //LOG.info("URI for crosshair: " + transcription.getTimeBasedURI(crossHairTime));
	        LOG.info("URI for crosshair: " + CommentEnvelope.getTimeBasedURI(transcription.getURN(), crossHairTime));

            Tier tier = activeTier;
	        URI uri;
	        if (tier == null) {
	        	//uri = transcription.getTimeBasedURI(selectionBeginPos, selectionEndPos);
	        	uri = CommentEnvelope.getTimeBasedURI(transcription.getURN(), selectionBeginPos, selectionEndPos);
	        } else {
	        	uri = CommentEnvelope.getTierTimeBasedURI(transcription.getURN(),
	        			tier.getName(), selectionBeginPos, selectionEndPos);
	        }
	        LOG.info("URI for active selection: " + uri.toString());
        }	
	}
	
	/**
	 * Called when the crosshair time changes (TimeEvent), either because the user
	 * clicked or because of playing.
	 * Also called when playing starts (StartEvent) or stops (StopEvent). 
	 */
	@Override
	public void controllerUpdate(ControllerEvent event) {
		//LOG.info(event.toString());
		//logURI();
	}

	/**
	 * Called when the selection in the time line viewer changes
	 * @see mpi.eudico.client.annotator.viewer.AbstractViewer#updateSelection()
	 */
	@Override
	public void updateSelection() {
		//logURI();
	}

	@Override
	public void updateActiveAnnotation() {
        activeAnnotation = getActiveAnnotation();
        if (activeAnnotation == null) {
        	//LOG.info("no active annotation.");
        } else {
	        //URI uri = transcription.getAnnotationBasedURI(activeAnnotation);
	        //LOG.info("URI for active annotation: " + uri.toString());
	    }
	}

	@Override
	public void updateLocale() {
		if (logInOutButton != null) {
			String messageKey = 
					commentManager.webClientIsLoggedIn() ? "CommentViewer.LogOut"
                            				             : "CommentViewer.LogIn";
			logInOutButton.setText(ElanLocale.getString(messageKey));
			logInOutButton.setToolTipText(ElanLocale.getString("CommentViewer.LogInToolTip"));
		}
		menuButton.setText(ElanLocale.getString("CommentViewer.Other"));
		menuButton.setToolTipText(ElanLocale.getString("CommentViewer.OtherToolTip"));
		
		addCommentMA.updateLocale();
		changeCommentMA.updateLocale();
		deleteCommentsMA.updateLocale();
		filterCommentsMA.updateLocale();
		toMailMA.updateLocale();
		toClipboardMA.updateLocale();
		fromClipboardMA.updateLocale();
		searchMA.updateLocale();
		settingsMA.updateLocale();
	}

	@Override
	public void preferencesChanged() {
		commentManager.preferencesChanged();
		//applyPreferences();
		
		Integer pref = Preferences.getInt(CommentManager.UPDATE_CHECK_TIME, null);
		if (pref != null) {
			long oldInterval = fileWatcherInterval;
			fileWatcherInterval = pref * 60 * 1000L; // convert minutes to msec.
			if (fileWatcherInterval < oldInterval) {
				maybeRestartFileWatcher();
			}
		}
	}

	@Override
	public void isClosing() {
		stopFileWatcherThread();
		savePreferences();
		commentManager.isClosing();
	}
	
	private void updateList() {
		if (commentManager != null) {	// when commentManager is constructed, the pointer isn't assigned yet!
			List<CommentEnvelope> list = commentManager.getList();
			tableModel.setComments(list);
		}
	}
	
	/**
	 * ListDataListener interface.
	 */
	@Override
	public void contentsChanged(ListDataEvent event) {
		if (commentManager != null) {	// when commentManager is constructed, the pointer isn't assigned yet!
			List<CommentEnvelope> list = commentManager.getList();
			int lwb = event.getIndex0();
			int upb = event.getIndex1();
			
			if (upb >= list.size()) {
				LOG.info("upb > list.size()");
				upb = list.size() - 1;
			}
			if (lwb >= list.size()) {
				LOG.info("lwb > list.size()");
				lwb = list.size() - 1;
			}
			
			// Try to detect if we just had a load(), which replaced all
			// the data, or not.
			// fireTableRowsUpdated() doesn't like indices that are larger
			// than what it previously thought of as the number of rows.
			if (lwb == 0 && upb == list.size() - 1) {
				tableModel.fireTableDataChanged();			
			} else {
				tableModel.fireTableRowsUpdated(lwb, upb);
			}
			modified(ACMEditEvent.CHANGE_COMMENT, null);
			
			// Need to update the text field?
			activateCommentHalfway();
		}
	}

	/**
	 * ListDataListener interface.
	 */
	@Override
	public void intervalAdded(ListDataEvent event) {
		List<CommentEnvelope> list = commentManager.getList();
		int lwb = event.getIndex0();
		int upb = event.getIndex1();
		
		for (; lwb <= upb; lwb++) {
			CommentEnvelope cm = list.get(lwb);
			modified(ACMEditEvent.ADD_COMMENT, cm);
		}
		tableModel.fireTableRowsInserted(event.getIndex0(), upb);
	}

	/**
	 * ListDataListener interface.
	 */
	@Override
	public void intervalRemoved(ListDataEvent event) {
		int lwb = event.getIndex0();
		int upb = event.getIndex1();
		
		tableModel.fireTableRowsDeleted(lwb, upb);
		modified(ACMEditEvent.REMOVE_COMMENT, null);
	}

	/**
	 * ListSelectionListener interface. <br/>
	 * Gets called when the selection of the table changes.
	 * <p>
	 * Since we do row-wise selection, looking at commentTable.getSelectionModel() is
	 * sufficient; no need to check for commentTable.getColumnModel().getSelectionModel().
	 * <p>
	 * While we're in the middle of a change, sometimes parts of the private JTable data is updated
	 * and others aren't. Especially with the array that backs the model this can happen
	 * easily. As a stopgap, add a try/catch.
	 */
	@Override // ListSelectionListener
	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel expectedSource = commentTable.getSelectionModel();
		
		if (e.getSource() == expectedSource) {
			try {
				activateCommentHalfway();
			} catch (ArrayIndexOutOfBoundsException ex) {
				deActivateComment();
			}
			updateGUI();
		}
	}

	/**
	 * Update which buttons are enabled etc.
	 * This looks at the selected comments, how many they are,
	 * and sees if they are readonly.
	 * 
	 * If the current comment is not readonly, the Change button is enabled.
	 * If there is at least one deletable comment, the Delete button is enabled.
	 * If at least one comment is selected, the toClipboard and toMail buttons are enabled.
	 * The login/logout button gets the appropriate text: login if not logged in,
	 * logout if logged in.
	 */
	private void updateGUI() {
		int num = commentTable.getSelectedRowCount();
		boolean changable = false;
		if (num == 1 &&
				commentOfText != null && !commentOfText.isReadOnly()) {
			changable = true;
		}
		boolean deletable = false;
		if (num >= 1) {
			int[] selected = getSelectedRows();
			for (int s : selected) {
				CommentEnvelope e = commentManager.get(s);
				if (!e.isReadOnly()) {
					deletable = true;
					break;
				}
			}
		}
		
		changeCommentMA.setEnabled(changable);
		deleteCommentsMA.setEnabled(deletable);
		toClipboardMA.setEnabled(num >= 1);
		fromClipboardMA.setEnabled(true);
		toMailMA.setEnabled(num >= 1);
		if (logInOutButton != null) {
			logInOutButton.setText(ElanLocale.getString(
				commentManager.webClientIsLoggedIn() ? "CommentViewer.LogOut"
						                             : "CommentViewer.LogIn"));
		}
	}
	
	/**
	 * Add some comment, if it isn't a duplicate.
	 * This is to be used when the user has done some explicit action
	 * (such as pasting from the clipboard)
	 * and the comment can be assumed to be new for both the server and save file.
	 * @param ce
	 */
	public void addComment(CommentEnvelope ce) {
		ce.setToBeSaved(true);

		int existingIndex = commentManager.undoableAddComment(ce);
		if (existingIndex >= 0) {
			// Show the duplicate comment
			showTableRow(existingIndex);
			// See if the comment is really identical
			String changed;
			CommentEnvelope existing = commentManager.undoableGet(existingIndex);
			if (ce.valueEquals(existing)) {
				// "apparently identical";
				changed = ElanLocale.getString("CommentViewer.WarnDeleted.Identical");
			} else {
				// "but different";
				changed = ElanLocale.getString("CommentViewer.WarnDeleted.Different");
			}
			if (ce.isNewerThan(existing)) {
				// ", more recently edited";
				changed += ElanLocale.getString("CommentViewer.WarnDeleted.MoreRecent");
			}
			
			// "Duplicate comment (%s).\n\nReplace it?"
			String fmt = ElanLocale.getString("CommentViewer.WarnDeleted");
			String msg = String.format(fmt, changed);
			int reply = showYesNoKeepasnewDialog(msg);

			if (reply == ACCEPT_OPTION) {
				commentManager.undoableReplace(existingIndex, ce);
			} else if (reply == REJECT_OPTION) {
				// This function is not called when reloading from the server...
//				if (reloadFromWhere == CommentManager.RELOAD_FROM_SERVER) {
//					// Force the original comment to be saved back to undo this change
//					commentManager.undoableRelease(existingIndex, existing);
//				}
			} else if (reply == IMPORT_AS_NEW_OPTION) { // unused
				// Change its identity
				commentManager.updateAsNewComment(ce, true);
				commentManager.undoableAddComment(ce);	
				// Force the original comment to be saved back, too
				commentManager.undoableRelease(existingIndex, existing);
				// TODO: there were 2 undo-able steps here. Combine them into a sequence?
			} else if (reply == KEEP_LOCAL_AS_NEW_OPTION) {
				// Change the identity of our existing comment
				commentManager.updateAsNewComment(existing, false);
				commentManager.undoableRelease(existingIndex, existing);
				commentManager.undoableAddComment(ce);
				// TODO: there were 2 undo-able steps here. Combine them into a sequence?
			} else if (reply == POSTPONE_OPTION) {
				// do nothing.
			}
		} 
	}

	/*
	 * Constants for the sequence of responses to this dialog.
	 */
	public static final int POSTPONE_OPTION = 0;
	public static final int KEEP_LOCAL_AS_NEW_OPTION = 1;
	public static final int REJECT_OPTION = 2;
	public static final int ACCEPT_OPTION = 3;

	public static final int IMPORT_AS_NEW_OPTION = -99; // not recommended it seems...
	
	public int showYesNoKeepasnewDialog(String msg) {
		String[] options = {
					//"Postpone",
					ElanLocale.getString("CommentViewer.PostponeOption"),
					//"Keep local comment as new",
					ElanLocale.getString("CommentViewer.KeepLocalAsNewOption"),
					//"Import external comment as new",
					//ElanLocale.getString("CommentViewer.ImportAsNewOption"),
					//"Reject (and reverse)",
					ElanLocale.getString("CommentViewer.RejectOption"),
					//"Accept",
					ElanLocale.getString("CommentViewer.AcceptOption"),
				};
		String title = ElanLocale.getString("CommentViewer.WarnConflicting");
		int n = JOptionPane.showOptionDialog(
				null,					// Component parentComponent
				msg,					// String message
			    title,					// String title
			    JOptionPane.YES_NO_CANCEL_OPTION,   // optionType
			    JOptionPane.QUESTION_MESSAGE,	    // messageType
			    null,					// Icon icon
			    options,				// Object[] options
			    options[ACCEPT_OPTION]);// Object initialValue
		
		return n;	
	}
	
	/**
	 * Ask the user what to do if we already know it is a modified version of an existing comment.
	 * The source is not some user action, so either some changed save file or the server.
	 * <p>
	 * Options are: <ul>
	 * <li>do nothing; postpone the decision until next time
	 * <li>reject the new comment and delete it from the server
	 * <li>remove the existing comment, insert the new one instead
	 * <li>insert the new comment as if it were completely new
	 * <li>turn the old comment into a completely new one, and insert the new comment.
	 * </ul>
	 * 
	 * @param existingIndex
	 * @param newCE
	 * @param reloadFromWhere RELOAD_FROM_SERVER or RELOAD_FROM_FILE
	 */
	public void modifyComment(int existingIndex, CommentEnvelope newCE, int reloadFromWhere) {
		assert (existingIndex >= 0);
			
		// Show the duplicate comment
		showTableRow(existingIndex);
		
		String newtext = newCE.getMessage();
		if (newtext.length() > 200) {
			newtext = newtext.substring(0, 200) + "...";
		}
		String initials = newCE.getInitials();
		if (!initials.isEmpty()) {
			newtext = '[' + initials + "] " + newtext;
		}
		
		StringBuilder msg = new StringBuilder();
		msg.append(ElanLocale.getString("CommentViewer.CommentChangedExternally"));
		msg.append("\n\"");
		msg.append(newtext);
		msg.append("\"\n\n");
		msg.append(ElanLocale.getString("CommentViewer.ReplaceIt"));
//		if (ce.getToBeSavedToFile()) {
//			// msg += "\nYou seem to have modified the comment recently.";
//			msg.append('\n');
//			msg.append(ElanLocale.getString("CommentViewer.ModifiedRecently"));
//		}
		
		int reply = showYesNoKeepasnewDialog(msg.toString());

		CommentEnvelope existing = commentManager.undoableGet(existingIndex);

		if (reply == ACCEPT_OPTION) {
			commentManager.undoableReplace(existingIndex, newCE);
		} else if (reply == REJECT_OPTION) {
			if (reloadFromWhere == CommentManager.RELOAD_FROM_SERVER) { // TODO is this condition necessary?
				// Force the original comment to be saved back to undo this change
				commentManager.undoableRelease(existingIndex, existing);
			}
		} else if (reply == IMPORT_AS_NEW_OPTION) { // unused
			if (DEBUG) {
				System.out.printf("modifyComment: IMPORT_AS_NEW_OPTION. updateAsNewComment(newCE, true)\n");
			}
			commentManager.updateAsNewComment(newCE, true);
			// Force the original comment to be saved back, too.
			// Release before modifying the list: otherwise index becomes invalid.
			commentManager.undoableRelease(existingIndex, existing);
			commentManager.undoableAddComment(newCE);			
			// TODO: there were 2 undo-able steps here. Combine them into a sequence?
		} else if (reply == KEEP_LOCAL_AS_NEW_OPTION) {
			// Change the identity of our existing comment
			commentManager.updateAsNewComment(existing, false);
			commentManager.undoableRelease(existingIndex, existing);
			commentManager.undoableAddComment(newCE);							
			// TODO: there were 2 undo-able steps here. Combine them into a sequence?
		} else if (reply == POSTPONE_OPTION) {
			// do nothing.
		}
	}

	private static final int MAYBE_REMOVE_POSTPONE = 0;
	private static final int MAYBE_REMOVE_ACCEPT = 1;
	private static final int MAYBE_REMOVE_REJECT = 2;
	
	/**
	 * Ask the user what to do if we can't find a comment in the newly read collection.
	 * 
	 * @param existingIndex
	 * @param ce
	 */
	public void maybeRemoveComment(int existingIndex, CommentEnvelope ce, int reloadFromWhere) {
		// Show the to-be-removed comment
		showTableRow(existingIndex);
		String message = ce.getMessage();
		if (message.length() > 64) {
			message = message.substring(0, 64);
		}
		
		StringBuilder msg = new StringBuilder();
		//	Comment seems to be deleted or unknown externally.
		msg.append(ElanLocale.getString("CommentViewer.CommentDeletedExternally"));
		msg.append("\n\"");
		msg.append(message);
		msg.append("\"\n\n");
		//  Remove it?
		msg.append(ElanLocale.getString("CommentViewer.RemoveIt"));

		if (ce.getToBeSavedToFile()) {
			// msg += "\nYou seem to have modified the comment recently.";
			msg.append('\n');
			msg.append(ElanLocale.getString("CommentViewer.ModifiedRecently"));
		}
		
		String[] options = {
			//"Postpone",
			ElanLocale.getString("CommentViewer.PostponeOption"),
			//"Remove local version",
			ElanLocale.getString("CommentViewer.RemoveLocal"),
			//"Keep local version (and export it)",
			ElanLocale.getString("CommentViewer.DontRemoveLocal"),
		};

		int reply = JOptionPane.showOptionDialog(
				null,					// Component parentComponent
				msg.toString(),			// String message
				ElanLocale.getString("Message.Warning"),	// String title
			    JOptionPane.YES_NO_CANCEL_OPTION,   // optionType
			    JOptionPane.QUESTION_MESSAGE,	    // messageType
			    null,					// Icon icon
			    options,				// Object[] options
			    options[MAYBE_REMOVE_REJECT]);	// Object initialValue
		
		if (reply == MAYBE_REMOVE_POSTPONE) {
			// Do nothing
		} else if (reply == MAYBE_REMOVE_ACCEPT) {
			commentManager.undoableRemove(existingIndex);
		} else if (reply == MAYBE_REMOVE_REJECT) {
			// Force the non-deleted comment to be saved back
			// but by now its URL has apparently become invalid.
			// (If the comment was removed from a saved file, we don't know about the URL).
			if (reloadFromWhere == CommentManager.RELOAD_FROM_SERVER) {
				ce.setMessageURL("");
			}
			commentManager.undoableRelease(existingIndex, ce); // maybe also only if RELOAD_FROM_SERVER? 
		}
	}
	
	//                //
	// File watching. //
	//                //
	
	private void startFileWatcherThread(String name) {
		fileWatcherThread = new Thread(null, null, "ElanSavedFileWatcher " + name) {
			@Override
			public void run() {
				try {
					for (;;) {
						approximateNextWakeupTime = System.currentTimeMillis() + fileWatcherInterval;
						Thread.sleep(fileWatcherInterval);
						
						/*
						 * Try to acquire the semaphore. We don't want to queue multiple
						 * update scans at the same time, that is simply not needed.
						 * It doesn't hurt to skip an update check if the previous one isn't finished.
						 * The enqueued Runnable will release the semaphore when it has
						 * finished its work.
						 * 
						 * The actual update check runs on the GUI thread, since it may
						 * access the GUI, and it provides exclusion with user-triggered
						 * modification actions.
						 */
						if (updateCheckCanBeEnqueued.tryAcquire()) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									try {
										commentManager.checkForFileModifications(false);
									} finally {
										updateCheckCanBeEnqueued.release();
									}
								}
							});
						} else {
							if (DEBUG) {
								System.out.println("Won't trigger an unpdate check (previous one not finished). Will try again later.");
							}
						}
					}
				} catch (InterruptedException e) {
					// Ah! Somebody wanted to stop us!
					return;
				}
			}
		};
		fileWatcherThread.setDaemon(true);
		fileWatcherThread.start();
	}
	
	private void stopFileWatcherThread() {
		try {
			updateCheckCanBeEnqueued.acquire();
			if (fileWatcherThread != null && fileWatcherThread.isAlive()) {
				fileWatcherThread.interrupt();
				fileWatcherThread.join();
			}
		} catch (InterruptedException e1) {
			// Can't happen - we only interrupt the file watcher thread.
			e1.printStackTrace();
		} finally {
			fileWatcherThread = null;
			updateCheckCanBeEnqueued.release();
		}
	}

	/**
	 * 	If the new next wakeup is much sooner than the one already scheduled,
	 *  then restart the thread.
	 *  
	 *  The margin is taken as 30 seconds.
	 */
	private void maybeRestartFileWatcher() {
		if (System.currentTimeMillis() + fileWatcherInterval + 30000 < approximateNextWakeupTime) {
			stopFileWatcherThread();
			startFileWatcherThread(transcription.getName());
		}
	}
		
	/**
	 * Handle ACMListeners.
	 * Borrow the Transcription's functionality and let it
	 * notify everyone. 
	 * That saves duplication on listener administration.
	 */
	public void notifyListeners(ACMEditableObject source, int operation, Object modification) {
		transcription.handleModification(source, operation, modification);
	}

   	@Override
	public void modified(int operation, Object modification) {
   		handleModification(this, operation, modification);
   	}

	@Override
    public void handleModification(ACMEditableObject commentViewer, int operation, Object modification) {
		notifyListeners(commentViewer, operation, modification);
	}

	/**
	 * MultiTierViewer interface
	 */
	@Override
	public void setVisibleTiers(List/*<Tier>*/ tiers0) {
		List<Tier> tiers = tiers0;
		
		visibleTiers.clear();
		for (Tier t : tiers) {
			visibleTiers.add(t.getName());
		}
		totalNumberOfTiers = transcription.getTiers().size();
		newTierFilter();
	}

	/**
	 * MultiTierViewer interface
	 */
	@Override
	public void setActiveTier(Tier tier) {
		activeTier = tier;
		// Can also be found with vm.getMultiTierControlPanel().getActiveTier();
		//logURI();
	}

	/**
	 * MultiTierViewer interface
	 */
	@Override
	public void setMultiTierControlPanel(MultiTierControlPanel controller) {
	}

	/**
	 * KeyListener interface
	 */
	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	/**
	 * Activate a comment whenever a space or enter key is hit on the comment list.
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getSource() == commentTable) {
			if (e.getKeyChar() == ' ' || e.getKeyChar() == '\n') {
				activateComment();
			}
		}
	}

	/**
	 * MouseListener interface.
	 * 
	 * When double-clicking in the list, we want to activate the comment.
	 * Use the fact that the first click will already have selected it.
	 */
    @Override
	public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
			activateComment();
        }
    }

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
	
	/**
	 * A document listener that turns the text red if it is
	 * different than the text from the comment it came from.
	 * Otherwise it turns the text black (which is hopefully the default).
	 * 
	 * This is for showing the user that they have a modification which is not saved.
	 * 
	 * @author olasei
	 */
	private final class RedTextDocumentListener implements
			DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			if (commentOfText == null || !commentOfText.getMessage().equals(text.getText())) {
				text.setForeground(Color.RED);
			} else {
				text.setForeground(Color.BLACK);
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			insertUpdate(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}
	}

	/**
	 * Extend MenuAction with setting the tool tip from the beginning.
	 * It is already updated in MenuAction if it was non-empty.
	 * 
	 * @author olasei
	 */
	private class ButtonMenuAction extends MenuAction {
		public ButtonMenuAction(String name) {
			super(name);
            putValue(Action.SHORT_DESCRIPTION,
                    ElanLocale.getString(name + "ToolTip"));
		}
	}
	
	/**
	 * A local MenuAction for handling the button "Add Comment".
	 *
	 * @author olasei
	 */
	private final class AddCommentAction extends ButtonMenuAction {
		public AddCommentAction(String name) {
			super(name);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// When creating the fragment identifier for the comment, always set the time,
			// set the Annotation id when available, and
			// set the tier when available.
			long beginTime = getSelectionBeginTime();
			long endTime = getSelectionEndTime();
			if (beginTime == 0 && endTime == 0) {
				// There is no selection, use the media time instead
				beginTime = endTime = getMediaTime();
			}
			CommentEnvelope comment = commentManager.createComment(text.getText(),
					beginTime, endTime);
//			if (activeAnnotation != null) {
//				comment.setAnnotationID(activeAnnotation.getId());
//			} 
			if (activeTier != null) {
				comment.setTierName(activeTier.getName());
			}
			//int where = commentManager.insert(comment);
			int where = commentManager.undoableInsert(comment);
			showTableRow(where);
		}
	}
	
	/**
	 * A local MenuAction for handling the button "Change Comment".
	 *
	 * @author olasei
	 */
	private final class ChangeCommentAction extends ButtonMenuAction {
		public ChangeCommentAction(String name) {
			super(name);
		}
		
		@Override
		public void actionPerformed(ActionEvent ev) {
			int selected[] = getSelectedRows();
			
			if (selected.length == 1) {
				CommentEnvelope e = commentManager.undoableGet(selected[0]);
				if (!e.isReadOnly()) {
					e.setMessage(text.getText());
					e.setModificationDate();
					int newpos = commentManager.undoableRelease(selected[0], e);
					// re-activate the comment
					setSelectedIndex(newpos);
					text.setForeground(Color.BLACK);
				}
			}
		}
	}

	/**
	 * Change a comment, if that action is enabled, otherwise add one.
	 *
	 * @author olasei
	 */
	private final class ChangeOrAddCommentAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (changeCommentMA.isEnabled()) {
				changeCommentMA.actionPerformed(e);
			} else {
				addCommentMA.actionPerformed(e);
			}
		}
	}
	
	/**
	 * A local MenuAction for handling the button "Delete Comment".
	 *
	 * @author olasei
	 */
	private final class DeleteCommentsAction extends ButtonMenuAction {
		public DeleteCommentsAction(String name) {
			super(name);
		}
		
		@Override
		public void actionPerformed(ActionEvent ev) {
			int selected[] = getSelectedRows();	

			Arrays.sort(selected);
			
			 // remove bottom-to-top, to keep indices valid
			for (int i = selected.length - 1; i >= 0; i-- ) {
				CommentEnvelope e = commentManager.get(selected[i]);
				if (!e.isReadOnly()) {
					commentManager.undoableRemove(selected[i]);
				}
			}
		}
	}

	/**
	 * A local MenuAction for handling the button "Filter...".
	 *
	 * @author olasei
	 */
	private final class FilterCommentsAction extends ButtonMenuAction {
		public FilterCommentsAction(String name) {
			super(name);
		}
		
		@Override
		public void actionPerformed(ActionEvent ev) {
			if ((ev.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
		        // with shift pressed, cancel the filtering
				setRegexFilter(null, stringFilterCaseSensitive);
			} else {
		        // create a filter dialog
				CommentsFilterDialog dialog = new CommentsFilterDialog(
						CommentViewer.this,
						stringFilterString,
						stringFilterCaseSensitive);
		        dialog.setVisible(true);
			}
		}
	}

	/**
	 * A local MenuAction for handling the menu item "To Mail".
	 *
	 * @author olasei
	 */
	private final class ToMailMenuAction extends ButtonMenuAction {
		public ToMailMenuAction(String name) {
			super(name);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int selected[] = getSelectedRows();
			
			commentManager.commentsToMail(selected);
		}
	}
	
	/**
	 * A local MenuAction for handling the menu item "To Clipboard".
	 *
	 * @author olasei
	 */
	private final class ToClipboardMenuAction extends ButtonMenuAction {
		public ToClipboardMenuAction(String name) {
			super(name);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int selected[] = getSelectedRows();
			
			commentManager.commentsToClipboard(selected);
		}
	}
	
	/**
	 * A local MenuAction for handling the menu item "From Clipboard".
	 *
	 * @author olasei
	 */
	private final class FromClipboardMenuAction extends ButtonMenuAction {
		public FromClipboardMenuAction(String name) {
			super(name);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			CommentManager.commentsFromClipboard();
		}
	}

	/**
	 * A local MenuAction for handling the menu item "Search...".
	 *
	 * @author olasei
	 */
	private final class SearchMenuAction extends ButtonMenuAction {
		public SearchMenuAction(String name) {
			super(name);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			CommentSearchDialog dialog = new CommentSearchDialog(stringFilterString, stringFilterCaseSensitive);
	        dialog.setVisible(true);
		}
	}

	/**
	 * A local MenuAction for handling the menu item "Settings...".
	 *
	 * @author olasei
	 */
	private final class SettingsMenuAction extends ButtonMenuAction {
		public SettingsMenuAction(String name) {
			super(name);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
	        // create a settings dialog (modal).
	        CommentsSettingsDialog dialog = new CommentsSettingsDialog(transcription);
	        dialog.setVisible(true);
		}
	}
}
