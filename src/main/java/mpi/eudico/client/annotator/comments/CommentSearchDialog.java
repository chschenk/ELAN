package mpi.eudico.client.annotator.comments;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.viewer.CommentViewer;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.Pair;

/**
 * A dialog for the user to specify a search string.
 * 
 * The dialog is not connected to the CommentViewer,
 * because it will list comments from various other files,
 * and must be agnostic of which viewer belongs to them
 * (and in fact, they don't belong to any viewer at all). 
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public class CommentSearchDialog extends ClosableDialog
                                 implements ActionListener, MouseListener, PropertyChangeListener {
	private JLabel searchLabel;
	private JTextField searchTextField;
	private String searchString;
	private JLabel caseLabel;
	private JCheckBox caseCheckBox;
	private boolean caseSensitive;
	private JButton searchButton;
	private JButton closeButton;
	private DefaultCommentTableModel tableModel;
	private TableModelExtender<String> tableModelEx;
	private CommentTable commentTable;
	private List<CommentEnvelope> searchResults;
	private List<String> resultFiles;
	private JComboBox columnComboBox;
	private JLabel columnLabel;
	/**
	 * Column number from the CommentTableModel indicating in which column to search.
	 * Use < 0 (such as -1) to search in all columns.
	 */
	private int searchColumn;
	private JProgressBar progressBar;
	private SwingWorker<Void, Void> searchTask;
	
	public CommentSearchDialog(String initialFilter, boolean caseSensitive) {
        super((Frame)null, false);   // Make it a non-modal dialog
		this.searchString = initialFilter;
		this.caseSensitive = caseSensitive;
		this.searchResults = Collections.emptyList();
		initComponents();
		postInit();
	}

	private void initComponents() {
		String title = ElanLocale.getString("CommentSearchDialog.Title");
        setTitle(title);
        
        setLayout(new GridBagLayout());

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints settingsgbc = new GridBagConstraints();
        settingsgbc.insets = new Insets(10, 10, 10, 10);
        settingsgbc.fill = GridBagConstraints.HORIZONTAL;
        settingsgbc.weightx = 1.0;
        settingsgbc.gridx = 0;
        settingsgbc.gridy = 0;

        // Create the block with the text fields
        JPanel textfieldsPanel = new JPanel(new GridBagLayout());

        // Create the filter label and textfield
        // "Search for regular expression"
        searchLabel = new JLabel(ElanLocale.getString("CommentSearchDialog.SearchForRegex"));
        searchTextField = new JTextField(searchString, 30);
        searchTextField.addActionListener(this);
        
        // "Search In Column"
        columnLabel = new JLabel(ElanLocale.getString("CommentSearchDialog.SearchInColumn"));
        columnComboBox = new JComboBox();
        // "All Columns"
        columnComboBox.addItem(ElanLocale.getString("CommentSearchDialog.AllColumns"));

        tableModel = new DefaultCommentTableModel(null);
		int ncols = tableModel.getColumnCount();
		for (int i = 0; i < ncols; i++) {
			columnComboBox.addItem(tableModel.getColumnName(i));
		}
		columnComboBox.addActionListener(this);
		searchColumn = -1; // all columns

        // "Case sensitive"
        caseLabel = new JLabel(ElanLocale.getString("CommentSearchDialog.CaseSensitive"));
        caseCheckBox = new JCheckBox("", caseSensitive);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        textfieldsPanel.add(searchLabel, gbc);
        gbc.gridy++;
        textfieldsPanel.add(columnLabel, gbc);
        gbc.gridy++;
        textfieldsPanel.add(caseLabel, gbc);
        gbc.gridy++;

        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        textfieldsPanel.add(searchTextField, gbc);
        gbc.gridy++;
        textfieldsPanel.add(columnComboBox, gbc);
        gbc.gridy++;
        textfieldsPanel.add(caseCheckBox, gbc);
        gbc.gridy++;

        settingsgbc.gridy++;
        settingsPanel.add(textfieldsPanel, settingsgbc);
        
		tableModel.setComments(searchResults);
		tableModelEx = new TableModelExtender<String>(tableModel,
				ElanLocale.getString("CommentSearchDialog.File"), // "File"
				String.class);
		commentTable = new CommentTable(tableModelEx);
		JScrollPane scrollCommentList = new JScrollPane(commentTable);
		commentTable.setScrollPane(scrollCommentList);
		commentTable.addListeners(this);
		
        settingsgbc.gridy++;
        settingsgbc.weighty = 1.0;
        settingsgbc.fill = GridBagConstraints.BOTH;
        settingsPanel.add(scrollCommentList, settingsgbc);
        
        progressBar = new JProgressBar(0,1);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        settingsgbc.gridy++;
        settingsgbc.weighty = 0.0;
        settingsgbc.fill = GridBagConstraints.HORIZONTAL;
        settingsPanel.add(progressBar, settingsgbc);

        // maingbc is used twice: once for the top part which is the border-enclosed part,
        // and once for the two buttons below that.
        GridBagConstraints maingbc = new GridBagConstraints();
        maingbc.gridx = 0;
        maingbc.weightx = 1.0;
        maingbc.weighty = 1.0;
        maingbc.fill = GridBagConstraints.BOTH;
        maingbc.insets = new Insets(20, 20, 10, 20);
        add(settingsPanel, maingbc);

        // Create a panel with APPLY and CANCEL buttons below the bordered area.
        JPanel searchClosePanel = new JPanel(new GridBagLayout());

        // Create the "Apply" button
        searchButton = new JButton(ElanLocale.getString("CommentSearchDialog.Search"));
        searchButton.addActionListener(this);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        searchClosePanel.add(searchButton, gbc);

        // Create the "Close" button
        closeButton = new JButton(ElanLocale.getString("CommentSearchDialog.Close"));
        closeButton.addActionListener(this);
        searchClosePanel.add(closeButton, gbc);

        maingbc.weighty = 0;
        maingbc.fill = GridBagConstraints.HORIZONTAL;
        maingbc.insets = new Insets(0, 20, 20, 20);
        add(searchClosePanel, maingbc);
	}

	/**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        setLocationRelativeTo(getParent());
        setPreferences();
    }

    /** 
     * The user has done something.
     * 
     * If they want to search, interpret the text as a regular expression.
     * If it turns out to be invalid as such, use it as a literal search string.
     */
	@Override
	public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
    	
	    if (source == searchButton || source == searchTextField) {	    	
	    	String match = searchTextField.getText();
	    	if (!match.isEmpty()) {
		    	boolean caseSensitive = caseCheckBox.isSelected();
				int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
				Pattern p;
				try {
				    p = Pattern.compile(match, flags);
				} catch (PatternSyntaxException ex) {
					try {
						// If current expression doesn't parse, just use it as a literal string.
					    p = Pattern.compile(match, flags | Pattern.LITERAL);
					} catch (PatternSyntaxException ex2) {
						// This is probably impossible.
						p = null;
					}
				}
				
				if (p != null) {
					doSearch(p);
				}
	    	}
	    } else if (source == columnComboBox) {
	    	// Subtract 1 from the column number for the "All Columns" item
	    	searchColumn = columnComboBox.getSelectedIndex() - 1;
	    } else if (source == closeButton) {
	    	if (searchTask != null && !searchTask.isDone()) {
	    		searchTask.cancel(false);
	    	} else {
		    	savePreferences();
		        dispose();
	    	}
	    }
	}
	
	/**
	 * Do the actual search.
	 * Determines in which directory to search.
	 * Clears the previous search results.
	 * Calls the recursing method.
	 */
	private void doSearch(Pattern p) {
		String dir;
		String pref = Preferences.getString(CommentManager.SEARCH_COMMENTS_DIRECTORY, null);
		if (pref != null) {
			dir = FileUtility.urlToAbsPath(pref);
		} else {
			dir = Constants.USERHOME;
		}
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		searchButton.setEnabled(false);
		progressBar.setIndeterminate(true);
		
		// Clear previous search results (also needed due to r/o empty list)
		resultFiles = new ArrayList<String>();
		resultFiles = Collections.synchronizedList(resultFiles);
		tableModelEx.setColumn(resultFiles);
		searchResults = new ArrayList<CommentEnvelope>();
		searchResults = Collections.synchronizedList(searchResults);
		tableModel.setComments(searchResults);
		
		doSearchTask(p, new File(dir));
	}

	/**
	 * Start a separate thread to do the comment search work in.
	 * <p>
	 * When there are results to report, or
	 * when a new directory is entered, it sends a property change event.
	 * In this way, all GUI updates are done on the GUI thread.
	 * 
	 * @param p Pattern to search for
	 * @param dir File directory to search in
	 */
	private void doSearchTask(final Pattern p, final File dir) {
		searchTask = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				doSearch(p, dir);
				return null;
			}
			@Override
			public void done() {
				progressBar.setIndeterminate(false);
				progressBar.setValue(progressBar.getMaximum());
				progressBar.setString( // Done
						ElanLocale.getString("CommentSearchDialog.Done")
						);
				setCursor(null);
				searchButton.setEnabled(true);
			}
		};
		searchTask.addPropertyChangeListener(this);
		searchTask.execute();
	}

	/**
	 * Listen to firePropertyChange()s.
	 * The resultFiles and searchResults have already been changed in the 
	 * worker thread, but the table has not been formally notified yet.
	 * <p>
	 * When entering a new directory to search, the text in the progress
	 * bar is updated.
	 * The Mac L&F doesn't show that, but the default L&F does.
	 * 
	 * @param evt
	 */
	@Override // PropertyChangeListener
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if ("results".equals(propertyName)) {
			synchronized(resultFiles) {
				tableModelEx.setColumn(resultFiles);
			}
			synchronized(searchResults) {
				tableModel.setComments(searchResults);
			}
		} else if ("directory".equals(propertyName)) {
			progressBar.setString((String)evt.getNewValue());
		}
	}
	
	/**
	 * Recursing method to search comments.
	 * It searches in files which have the correct file name suffix.
	 * 
	 * @param p the Pattern to match
	 * @param dir which directory to search in
	 * @return true when cancelled
	 */
	private boolean doSearch(Pattern p, File dir) {
		searchTask.firePropertyChange("directory", null, dir.toString());
		if (searchTask.isCancelled()) {
			return true;
		}
		
        File files[] = dir.listFiles();

        if (files != null) {
	        for (File f : files) {
	            if (f.isDirectory()) {
	            	if (doSearch(p, f)) {
	            		return true;
	            	}
	        		searchTask.firePropertyChange("directory", null, dir.toString());
	            } else if (f.canRead()) {
	        		if (searchTask.isCancelled()) {
	        			return true;
	        		}
	                String name = f.getName();
	                if (name.endsWith(CommentManager.COMMENT_FILENAME_SUFFIX)) {
	                    // Ok, look into this file.
	                    scanForComment(p, f);
	                }
	            }
	        }
        }
		return false;
	}
	
	/**
	 * Search in a single File for one or more comments that match the Pattern.
	 * Any matches are added to the search results table.
	 * @param p the Pattern
	 * @param f the File
	 */
	private void scanForComment(final Pattern p, File f) {
    	final Matcher m = p.matcher(""); // re-usable

    	/*
		 * Create a predicate which does the actual searching for the user's
		 * text. If searchColumn < 0, create a filter to look in all fields that
		 * are normally visible in the table. Otherwise create one to look in
		 * the indicated column only.
		 */
    	Predicate<CommentEnvelope> filter;
    	if (searchColumn < 0) {
	        filter = new Predicate<CommentEnvelope>() {
	            @Override
				public boolean test(CommentEnvelope ce) {
            		int ncols = tableModel.getColumnCount();
            		
            		for (int i = 0; i < ncols; i++) {
    					m.reset(DefaultCommentTableModel.getValueAtColumn(ce, i));
    					if (m.find())
    						return true;
            		}
	            	
	            	return false;
	            }
	        };
    	} else {
	        filter = new Predicate<CommentEnvelope>() {
	            @Override
				public boolean test(CommentEnvelope ce) {
					m.reset(DefaultCommentTableModel.getValueAtColumn(ce, searchColumn));
					if (m.find())
						return true;
	            	
	            	return false;
	            }
	        };
    	}

        // Read the file, filtered by our criteria.
        String fileName = f.getAbsolutePath();
		List<CommentEnvelope> list = CommentManager.read(fileName, filter);
        
        // and if we got anything, show it to the user.
        if (!list.isEmpty()) {
        	// set the file name column first
    		fileName = FileUtility.dropExtension(fileName);
    		for (int i = list.size(); i > 0; i--) {
    			resultFiles.add(fileName);
    		}
    		// then the actual result table
        	searchResults.addAll(list);
        	
    		//tableModelEx.setColumn(resultFiles);    		
    		//tableModel.setComments(searchResults);
    		searchTask.firePropertyChange("results", null, null);
        }
	}

	/**
	 * Activate the comment that is currently selected in the search results list, if any.
	 * Activating happens in the context of its Transcription, which must therefore
	 * be found and opened.
	 */
	private void activateComment() {
		int index = commentTable.getSelectionModel().getLeadSelectionIndex();
		if (index >= 0) {
			index = commentTable.convertRowIndexToModel(index);
			CommentEnvelope ce = searchResults.get(index);
			String commentFileName = resultFiles.get(index);
			commentFileName = FileUtility.dropExtension(commentFileName) + ".eaf";
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			activateComment(ce, commentFileName);
			setCursor(null);
		}
	}

	/**
	 * We need to try to find the transcription that belongs to this comment,
	 * and open it.
	 * We try to activate the comment in its viewer, but there is no guarantee
	 * that that really works. In particular if the file needs to be loaded,
	 * that process may prevent it.
	 * 
	 * @param ce the Comment to find and activate so the user sees it properly
	 */
	private void activateComment(CommentEnvelope ce, String suggestedFileName) {
		final String searchString = ce.getMessageID();
        
        Predicate<TranscriptionImpl> pred = new Predicate<TranscriptionImpl>() {
			@Override
			public boolean test(TranscriptionImpl t) {
                Pair<ElanFrame2, Boolean> pair = CommentManager.getOrOpenFrameFor(t);
                ElanFrame2 frame = pair.getFirst();
                boolean opened = pair.getSecond();

		        if (frame != null) {
		        	// Make sure that the comment is actually present in this file.
		        	// Use *its* viewer and manager for that, not ours.
		        	// This is needed because there could be multiple copies, such as
		        	// backups or old versions, each with the same URN.
		        	CommentViewer cv =  frame.getViewerManager().getCommentViewer();
		        	if (cv != null) {
		        		CommentManager cm = cv.getCommentManager();
		        		int index = cm.findCommentById(searchString);
		        		if (index >= 0) {
		                    cv.activateComment(index);
		                    cv.showTableRow(index);
		                    frame.toFront();
		                    return true;
		        		}
		        	}
		        	// Could not find the comment.
			        if (opened) {
			        	frame.doClose(true);
			        }
		        }
		        
		        return false;
			}        
        };

        URI uri = ce.getAnnotationURIBase();
        
        // Shortcut: try a file name based on the comment file name first.
        // If that succeeds, don't go and search at all.
    	File f = new File(suggestedFileName);
        if (f.canRead()) {
        	List<File> candidate = new ArrayList<File>(1);
        	candidate.add(f);
            Transcription t = CommentManager.findTranscriptionFromURN(uri, pred, candidate);
            if (t != null) {
            	return;
            }

        }
        /*TranscriptionImpl t =*/ CommentManager.findTranscriptionFromURNwithDialog(uri, pred);
        // if t == null, nothing was found.
	}

	/**
	 * Listen to clicks on the search results table.
	 * A double-click activates a search result.
	 */
	@Override // MouseListener
	public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
			activateComment();
        }
	}

	@Override // MouseListener
	public void mousePressed(MouseEvent e) {
	}

	@Override // MouseListener
	public void mouseReleased(MouseEvent e) {
	}

	@Override // MouseListener
	public void mouseEntered(MouseEvent e) {
	}

	@Override // MouseListener
	public void mouseExited(MouseEvent e) {
	}
	/**
	 * Set the preferred column widths based on the Preferences.
	 * Also restore their order.
	 * These preferences are global.
	 * <p>
	 * <b>Note: This uses the GUI names of the columns in the preferences,
	 * which depend on the language.</b> 
	 */
	private void setPreferences() {
		commentTable.applyPreferences("CommentSearchDlg", null);
	}
	
	/**
	 * Save the preferred column widths and order to preferences.
	 */
	private void savePreferences() {
		commentTable.savePreferences("CommentSearchDlg", null);
	}
}
