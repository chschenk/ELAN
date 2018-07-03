package mpi.eudico.client.annotator.interannotator;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.SelectableContentTableModel;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.util.UneditableTableModel;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.EmptyStringComparator;

/**
 * A AnnotatorCompare object creates both a wizard object and the objects that
 * are the steps the wizard presents. This class, the second or the third step
 * in the wizard dialog, helps in making a choice between the classical way of
 * comparing and comparing by calculating the value of kappa for the matrices
 * generated from two annotated tiers. For additional comments, refer to the
 * first step.
 * 
 * @author keeloo
 * @author Han Sloetjes 
 */
public class TiersSelectionStep extends StepPane implements
	ListSelectionListener, TableModelListener {

    /*
     * The current document, can be null.
     */
    private TranscriptionImpl transcription;
    
    /**
     * ide generated
     */
    private static final long serialVersionUID = 1L;

    // two tables
    JTable table1;
    JTable table2;
   
    // two labels
    private JLabel hint1;
    private JLabel hint2;
    
    // retrieve settings from previous steps globally
    private CompareConstants.METHOD method;
    private CompareConstants.FILE_MATCHING tierSource;
    private CompareConstants.MATCHING tierMatching;
    private List<File> selFiles;
    // have a list available for caching the files when going to the previous step
    private List<File> oldSelFiles;
    private List<String> allTierNames;
    private String tierCustomSeparator;

	private JPanel tierPanel;

	private JScrollPane table1ScrollPane;
	private JScrollPane table2ScrollPane;
	// in case of affix based matching, highlight matching tiers in second, non-editable table?
	private TierAndFileMatcher tierMatcher;
    // for debugging purposes introduce a synchronous mode
	private boolean synchronousMode = true;
	
    /**
     * @param wizard
     *            organizing the steps
     * @param transcription
     *            to select the tiers from, can be null
     */
    public TiersSelectionStep(MultiStepPane wizard,
	    TranscriptionImpl transcription) {
	
		super(wizard);
	
		this.transcription = transcription;;
		
		// enable the wizard to jump to a specific step by looking at step names
		this.setName("CompareAnnotatorsDialog.TierSelectionStep");
	
		allTierNames = new ArrayList<String>();
		tierMatcher = new TierAndFileMatcher();
		// create the dialog panel
		createPanel();
    }

    /**
     * Adjust the pane's component properties when in the preceding pane
     * 'forward' was pushed
     * @param enable the enabled flag for the finish button 
     */
    private void setStateFinish(boolean enable) {
	    multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, enable);
    	//multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, enable);
    }

    private void checkConditions() {
    	// store "current" (or previous) values in order to determine whether tiers have to be (re)loaded
    	// how best to check the list of files?
    	CompareConstants.FILE_MATCHING oldTierSource = tierSource;
    	CompareConstants.MATCHING oldTierMatching = tierMatching;
    	if (selFiles != null) {
    		oldSelFiles = new ArrayList<File>(selFiles);
    	}
    	
    	// load the new values
    	method = (CompareConstants.METHOD) multiPane.getStepProperty(CompareConstants.METHOD_KEY);//"compareMethod"
    	tierSource = (CompareConstants.FILE_MATCHING) multiPane.getStepProperty(CompareConstants.TIER_SOURCE_KEY);//"tierSource", current, single file, across files
    	tierMatching = (CompareConstants.MATCHING) multiPane.getStepProperty(CompareConstants.TIER_MATCH_KEY);// "tierMatching", manual, prefix, suffix or sameName
    	selFiles = (List<File>) multiPane.getStepProperty(CompareConstants.SEL_FILES_KEY);//"selectedFiles"
    	tierCustomSeparator = (String) multiPane.getStepProperty(CompareConstants.TIER_SEPARATOR_KEY);
    	
    	if (transcription == null && (selFiles == null || selFiles.isEmpty())) {
    		setStateFinish(false);
    		// return; //return here?
    	}
    	// if we have to switch from one table to two or vice versa, clear the layout and rebuild it
    	boolean rebuildLayout = false;
    	boolean reloadTiers = false;
    	// rebuild layout if it is not the same as the previous 
    	if (oldTierSource != tierSource) {
    		rebuildLayout = true;
    		if ( (oldTierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC && tierSource != CompareConstants.FILE_MATCHING.CURRENT_DOC) ||
    				(oldTierSource != CompareConstants.FILE_MATCHING.CURRENT_DOC && tierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC)) {
    			reloadTiers = true;
    		}
    	}
    	// if only the set of files changed the ui doesn't need to be updated (?), only
    	// the tiers need to be reloaded
    	if (oldSelFiles == null && selFiles != null) {
    		reloadTiers = true;
    	} else if (oldSelFiles != null && selFiles == null) {
    		reloadTiers = true;
    	} else if (oldSelFiles != null && selFiles != null) {
    		if (oldSelFiles.size() != selFiles.size()) {
    			reloadTiers = true;
    		} else {
    			// the equals method returns true of all elements are in the same order which is not necessary for this check
    			for (File f : oldSelFiles) {
    				if (!selFiles.contains(f)) {
    					reloadTiers = true;
    					break;
    				}
    			}
    		}
    	}// end of testing if tiers have to be reloaded
    	
    	if (oldTierMatching != tierMatching) {
    		rebuildLayout = true;
    	}
    	
    	if (rebuildLayout) {
    		if (tierPanel != null) {
    			tierPanel.removeAll();
    		}
    		
	    	updatePanel(tierMatching);
    	}
    	
    	if (reloadTiers) {
    		reloadTiers();
    		fillTables();
    	} else {
    		if (rebuildLayout) {
    			// if the layout of tables has changed but the tiers haven't, 
    			// the tables still need to be updated
    			fillTables();
    		}    		
    	}
    	updateButtonState();
    }

    /**
     * Change component associated text on a change of language preference
     */
    public void updateLocale() {	
		// update label texts
		hint1.setText(ElanLocale
			.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint1"));
		hint2.setText(ElanLocale
			.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint2"));
    }

    /*
     * Creates all ui elements. The layout is made when entering this step.
     */
    private void createPanel() {
	
		// create labels showing a hint
		hint1 = new JLabel();
		hint2 = new JLabel();
	
		/*
		 * With the labels, all language sensitive components have been created,
		 * so a text can be added to them now.
		 */
		updateLocale();
	
		UneditableTableModel model1 = new UneditableTableModel(0, 0);
		UneditableTableModel model2 = new UneditableTableModel(0, 0);
	
		// create two tables, and associate these with the models and a listener
		table1 = new JTable(model1);
		table1.getSelectionModel().setSelectionMode(
			ListSelectionModel.SINGLE_SELECTION);
		table2 = new JTable(model2);
		table2.getSelectionModel().setSelectionMode(
			ListSelectionModel.SINGLE_SELECTION);
		table1.getSelectionModel().addListSelectionListener(this);
		table2.getSelectionModel().addListSelectionListener(this);
	
		// add scrolling
		Dimension prdim = new Dimension(400, 80);
		table1ScrollPane = new JScrollPane(table1);
		table1ScrollPane.setPreferredSize(prdim);
		table2ScrollPane = new JScrollPane(table2);
		table2ScrollPane.setPreferredSize(prdim);
	
		// create the panel
		tierPanel = new JPanel();
		tierPanel.setLayout(new GridBagLayout());
	
		// prepare to add components to the table
		Insets insets = new Insets(6, 6, 6, 6);
	
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = insets;
		gbc.weightx = 1.0;
	
		gbc.gridx = 0;
		gbc.gridy = 0;
	
		// add the hints and tables to the panel
		tierPanel.add(hint1, gbc);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		tierPanel.add(table1ScrollPane, gbc);
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.0;
		tierPanel.add(hint2, gbc);
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		tierPanel.add(table2ScrollPane, gbc);
	
		// add the panel to the wizard pane
		setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(tierPanel, gbc);
    }
    
    /**
     * Update the user interface based on the way tiers are coupled for comparison  
     * 
     * @param mode
     */
    private void updatePanel(CompareConstants.MATCHING mode) {
    	table2.getSelectionModel().removeListSelectionListener(this);
    	if (mode == CompareConstants.MATCHING.MANUAL) {
    		// pair one by one, two tables, valid for current document and in same file
    		// prepare to add components to the table
    		Insets insets = new Insets(6, 6, 6, 6);
    	
    		GridBagConstraints gbc = new GridBagConstraints();
    		gbc.anchor = GridBagConstraints.NORTHWEST;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.insets = insets;
    		gbc.weightx = 1.0;
    	
    		gbc.gridx = 0;
    		gbc.gridy = 0;
    	
    		// add the hints and tables to the panel
    		tierPanel.add(hint1, gbc);
    		gbc.gridy = 1;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weighty = 1.0;
    		tierPanel.add(table1ScrollPane, gbc);
    		gbc.gridy = 2;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.weighty = 0.0;
    		tierPanel.add(hint2, gbc);
    		gbc.gridy = 3;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weighty = 1.0;
    		tierPanel.add(table2ScrollPane, gbc);
    		//
    		table2.setEnabled(true);
    		table2.getSelectionModel().addListSelectionListener(this);
    		hint1.setText(ElanLocale
    				.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint1"));
    		hint2.setText(ElanLocale
    				.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint2"));
    	} else if (mode == CompareConstants.MATCHING.AFFIX || mode == CompareConstants.MATCHING.SUFFIX ||
    			mode == CompareConstants.MATCHING.PREFIX) {
    		// or have disabled second table, highlighting the automatically detected counterpart
    		// prepare to add components to the table

    		Insets insets = new Insets(6, 6, 6, 6);
    	
    		GridBagConstraints gbc = new GridBagConstraints();
    		gbc.anchor = GridBagConstraints.NORTHWEST;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.insets = insets;
    		gbc.weightx = 1.0;
    	
    		gbc.gridx = 0;
    		gbc.gridy = 0;
    	
    		// add the hints and tables to the panel
    		tierPanel.add(hint1, gbc);
    		gbc.gridy = 1;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weighty = 1.0;
    		tierPanel.add(table1ScrollPane, gbc);
    		gbc.gridy = 2;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.weighty = 0.0;
    		tierPanel.add(hint2, gbc);
    		gbc.gridy = 3;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weighty = 1.0;
    		tierPanel.add(table2ScrollPane, gbc);
    		//
    		table2.setEnabled(false);
    		hint1.setText(ElanLocale
    				.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint3"));
    		hint2.setText(ElanLocale
    				.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint4"));
    	} else if (mode == CompareConstants.MATCHING.SAME_NAME) {
    		// sameName, only one table, multiple selection
    		Insets insets = new Insets(6, 6, 6, 6);
        	
    		GridBagConstraints gbc = new GridBagConstraints();
    		gbc.anchor = GridBagConstraints.NORTHWEST;
    		gbc.fill = GridBagConstraints.HORIZONTAL;
    		gbc.insets = insets;
    		gbc.weightx = 1.0;
    	
    		gbc.gridx = 0;
    		gbc.gridy = 0;
    	
    		// add the hint label and table to the panel
    		tierPanel.add(hint1, gbc);
    		gbc.gridy = 1;
    		gbc.fill = GridBagConstraints.BOTH;
    		gbc.weighty = 1.0;
    		tierPanel.add(table1ScrollPane, gbc);
    		//
    		hint1.setText(ElanLocale
    				.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint5"));
    	}
    }
    
    /**
     * (Re)loads the tiers from a set of files or from a transcription and fills one or two table models, depending 
     * on the method of matching of tiers to compare. 
     */
    private void reloadTiers() {
    	setStateFinish(false);
    	List<String> tierNames = null;
    	
    	if (transcription != null && tierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC) {
    		tierNames = new ArrayList<String>();
    		if (transcription != null) {
    			TierImpl ti;
    		
    			for (int i = 0; i < transcription.getTiers().size(); i++) {
    			    ti = (TierImpl) transcription.getTiers().get(i);
    			    
    			    tierNames.add(ti.getName());
    			}
    		}
    	} else if (selFiles != null){   	
	    	TierLoader tierLoader = new TierLoader(selFiles);
	    	if (!synchronousMode) {
		    	tierLoader.start();
		    	//long startTime = System.currentTimeMillis();
		    	// && System.currentTimeMillis() - startTime < 10000
		    	while (tierLoader.isAlive()) {
		    		System.out.println("Number of files  processed: " + tierLoader.getNumProccessed());
		    		try {
		    			Thread.sleep(200);
		    		} catch (InterruptedException ie) {
		    			
		    		}
		    	}
	    	} else {
	    		tierLoader.run();// call the run method on the current thread
	    	}
	    	System.out.println("Final number of files  processed: " + tierLoader.getNumProccessed());
	    	
	    	// check the type of table (model) needed
	    	tierNames = tierLoader.getTierNames();
    	}
    	allTierNames.clear();
    	allTierNames.addAll(tierNames);
    	multiPane.putStepProperty(CompareConstants.ALL_TIER_NAMES_KEY, tierNames);
    }
    
    /**
     * Creates and populates one or two table models and adds them to the tables.
     * Assumes tiers have been loaded and tables have been created and are already in the gui layout. 
     */
    private void fillTables() {
    	
    	if (tierMatching == CompareConstants.MATCHING.MANUAL) {
    		// two tables, both single selection
    		UneditableTableModel tierModel1 = new UneditableTableModel(0, 1);
    		UneditableTableModel tierModel2 = new UneditableTableModel(0, 1);
        	for (String s : allTierNames) {
        		String[] rowData = new String[]{s};
        		tierModel1.addRow(rowData);
        		tierModel2.addRow(rowData);
        	}
        	table1.setModel(tierModel1);
        	table2.setModel(tierModel2);
        	table1.getTableHeader().setReorderingAllowed(false);
        	table2.getTableHeader().setReorderingAllowed(false);
        	tierModel1.setColumnIdentifiers(new Object[]{ElanLocale.getString("EditTierDialog.Label.TierName")});
        	tierModel2.setColumnIdentifiers(new Object[]{ElanLocale.getString("EditTierDialog.Label.TierName")});
        	
        	makeRowsSortable(table1, tierModel1, new int[] {0} );
        	makeRowsSortable(table2, tierModel2, new int[] {0} );
        	
        	table1.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        	table2.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        	table1.getSelectionModel().addListSelectionListener(this);
        	table2.getSelectionModel().addListSelectionListener(this);
    	} else if (tierMatching == CompareConstants.MATCHING.AFFIX || tierMatching == CompareConstants.MATCHING.PREFIX ||
    			tierMatching == CompareConstants.MATCHING.SUFFIX) {
    		// two tables, the second one disabled, or one table, with a third column
    		Set<String> tierColumnData = new TreeSet<String>();
        	for (String s : allTierNames) {
        		tierColumnData.add(s);
        	}
        	SelectableContentTableModel tierModel1 = new SelectableContentTableModel(tierColumnData);
        	//SelectableContentTableModel tierModel2 = new SelectableContentTableModel(tierColumnData);
        	UneditableTableModel tierModel2 = new UneditableTableModel(0, 1);
        	tierModel2.setRowCount(tierColumnData.size());
        	Iterator<String> tierIter = tierColumnData.iterator();
        	int row = 0;
        	while (tierIter.hasNext()) {
        		tierModel2.setValueAt(tierIter.next(), row, 0);
        		row++;
        	}

        	table1.setModel(tierModel1);
        	table2.setModel(tierModel2);
        	table1.getTableHeader().setReorderingAllowed(false);
        	table2.getTableHeader().setReorderingAllowed(false);

        	makeRowsSortable(table1, tierModel1, new int[] {1} );
        	makeRowsSortable(table2, tierModel2, new int[] {0} );
        	
			table1.getColumnModel().getColumn(0).setHeaderValue(null);
			table1.getColumnModel().getColumn(1).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName") );
			table1.getColumnModel().getColumn(0).setMaxWidth(30);

			table2.getColumnModel().getColumn(0).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName") );
        	table1.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        	table2.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        	//table1.getSelectionModel().addListSelectionListener(this);
        	table2.setEnabled(false);
        	tierModel1.addTableModelListener(this);
    	} else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
    		// one table, multi selection
    		Set<String> tierColumnData = new TreeSet<String>();
        	for (String s : allTierNames) {
        		tierColumnData.add(s);
        	}
        	SelectableContentTableModel tierModel1 = new SelectableContentTableModel(tierColumnData);
        	table1.setModel(tierModel1);
        	table1.getTableHeader().setReorderingAllowed(false);
			
        	makeRowsSortable(table1, tierModel1, new int[] {1} );
        	
			table1.getColumnModel().getColumn(0).setHeaderValue(null);
			table1.getColumnModel().getColumn(1).setHeaderValue( ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName") );
			table1.getColumnModel().getColumn(0).setMaxWidth(30);
			table1.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			//table1.getSelectionModel().addListSelectionListener(this);
			tierModel1.addTableModelListener(this);
    	}
    }
    
    /**
     * Performs checks on table(s) to see if the next/finish button should
     * be enabled or not.
     */
    private void updateButtonState() {
    	if (tierMatching == null) {
    		setStateFinish(false);
    		return;
    	}
    	
		if (tierMatching == CompareConstants.MATCHING.MANUAL) {
			// two tables visible, both should have one selected
			if (table1.getSelectedRow() != -1 && table2.getSelectedRow() != -1) {
				// in case of same document check if it is not the same
				if (tierSource == CompareConstants.FILE_MATCHING.ACROSS_FILES) {
					setStateFinish(true);
				} else {// current document or in single file
					setStateFinish(table1.getSelectedRow() != table2.getSelectedRow());
				}
			} else {
				setStateFinish(false);
			}
		/*} else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
			// single table, should have at least one tier
			setStateFinish(table1.getSelectedRow() != -1);*/
		} else { // tierMatching is based on suffix/prefix, at least one row , or tierMatching is same name
			TableModel tierModel = table1.getModel();
			if (tierModel instanceof SelectableContentTableModel) {
				SelectableContentTableModel tm = (SelectableContentTableModel) tierModel;
				setStateFinish( !tm.nothingSelected() );
			} else {
				setStateFinish(table1.getSelectedRow() != -1);
			}
		}
    }
    
    /**
     * Updates the second table to highlight tiers that match (affix based) the tiers
     * selected in the first table.
     */
    private void updateMatchingTiersTable() {
    	TableModel tierModel = table1.getModel();
		if (tierModel instanceof SelectableContentTableModel) {
			SelectableContentTableModel tm = (SelectableContentTableModel) tierModel;
			List<Object> selectedTiers1 = tm.getSelectedValues();
			List<String> selTierNames = new ArrayList<String>(selectedTiers1.size());
			for (Object oo : selectedTiers1) {
				selTierNames.add((String) oo);
			}
			List<List<String>> matchedTiers = tierMatcher.getMatchingTiers(allTierNames, selTierNames, 
					tierMatching, tierCustomSeparator);
			List<String> allMatches = new ArrayList<String>();
			for (List<String> mt : matchedTiers) {
				for (String name : mt) {
					if (!selTierNames.contains(name)) {
						allMatches.add(name);
					}
				}		
			}

			table2.getSelectionModel().clearSelection();
			int numRows = table2.getRowCount();
			String rowValue = null;
			for (int i = 0; i < numRows; i++) {
				rowValue = (String) table2.getValueAt(i, 0);
				if (allMatches.contains(rowValue)) {
					table2.addRowSelectionInterval(i, i);
				}
			}
		}	
    }
    
    /**
     * Listen to both the first and second table in the pane in case the table(s)
     * does/do not contain check boxes (?). 
     * 
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {			
		boolean isAdjusting = e.getValueIsAdjusting();
		if (!isAdjusting) {
			updateButtonState();
		}	
    }
    

    /**
     * Table model events are only received from {@link SelectableContentTableModel} models,
     * there (table row) selection events are not always equivalent to content selection events.  
     */
	@Override
	public void tableChanged(TableModelEvent e) {
		TableModel tierModel = table1.getModel();
		if (tierModel instanceof SelectableContentTableModel) {
			if (tierMatching == CompareConstants.MATCHING.AFFIX || tierMatching == CompareConstants.MATCHING.PREFIX ||
    			tierMatching == CompareConstants.MATCHING.SUFFIX) {
				updateMatchingTiersTable();
			}
			updateButtonState();
		}		
	}

    /**
     * Reply to the wizard's question for the title of this step.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
		return (ElanLocale.getString("CompareAnnotatorsDialog.TierSelectionStep.Title"));
    }
    
    /**
     * Act on the message send when entering this step after choosing 'next' in
     * the preceding step.
     * 
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepForward() {
    	setStateFinish(false);
    	checkConditions();
    }
    
    /**
     * Have come back after canceling an ongoing calculation.
     * Enable buttons. 
     */
	@Override
	public void enterStepBackward() {
		setStateFinish(true);
	}
	
    /**
     * Cache the current list of selected files in order to be able to check whether tiers have to be reloaded. 
     */
    @Override
	public boolean leaveStepBackward() {
		if (selFiles != null) {
			if (oldSelFiles == null) {
				oldSelFiles = new ArrayList<File>();
			}
			oldSelFiles.clear();
			oldSelFiles.addAll(selFiles);
		}
		
		return true;
	}

	@Override
	public boolean leaveStepForward() {
		Object matchingProp = multiPane.getStepProperty (CompareConstants.TIER_MATCH_KEY);
		if (matchingProp == CompareConstants.MATCHING.MANUAL) {
			// two tables, one tier per table selected
			if (table1.getSelectedRow() < 0) {
				// warning message? These error conditions should never occur 
				ClientLogger.LOG.warning("For manual matching a tier should be selected in the first table.");
				return false;
			}
			if (table2.getSelectedRow() < 0) {
				// warning
				ClientLogger.LOG.warning("For manual matching a tier should be selected in the second table.");
				return false;
			}
			multiPane.putStepProperty(CompareConstants.TIER_NAME1_KEY, table1.getValueAt(table1.getSelectedRow(), 0));
			multiPane.putStepProperty(CompareConstants.TIER_NAME2_KEY, table2.getValueAt(table2.getSelectedRow(), 0));
			
			return true;
		} else {// matching is SAME_NAME or AFFIX based
			// one table, at least one tier selected. Maybe check here if there is at least one matching tier?
			TableModel tierModel = table1.getModel();
			if (tierModel instanceof SelectableContentTableModel) {
				SelectableContentTableModel tm = (SelectableContentTableModel) tierModel;
				List<Object> selectedValues = tm.getSelectedValues();
				
				if (selectedValues.size() == 0) {
					ClientLogger.LOG.warning("For same name or affix based matching at least one tier should be selected in the (first) table.");
					return false;
				}
				
				List<String> selectedTierNames = new ArrayList<String>(selectedValues.size());
				for (Object selected : selectedValues) {
					selectedTierNames.add((String) selected);
				}
				
				multiPane.putStepProperty(CompareConstants.TIER_NAMES_KEY, selectedTierNames);
			} else {
				// something is wrong?
				ClientLogger.LOG.warning("The type of tier selection and tier matching is unclear.");
				return false;
			}
			
		}
		
		return true;
	}

	/**
     * Answer the wizard when it asks for the preferred previous step.
     * 
     * @return the identifier of the preferred previous step; when null the
     *         wizard will follow the steps in the order of declaration.
     */
    @Override
	public String getPreferredPreviousStep() {
    	return super.getPreferredPreviousStep();
    	/*
		if (multiPane.getStepProperty(CompareConstants.METHOD_KEY) == CompareConstants.METHOD.CLASSIC) {
		     // In case of the classic method, the document selection step will
		     // be skipped when moving to the first step.
		    return "CompareAnnotatorsDialog.MethodSelectionStep";
		} else {
		    // otherwise, stick to the predefined step order
		    return null;
		}
		*/
    }
    
    /**
     * Delegates to {@link #leaveStepForward()}; Next and Finish both move to the 
     * Progress monitoring step in which the real work is done and monitored.  
     * 
     * @return {@link #leaveStepForward()}
     */
    @Override
	public boolean doFinish() {
    	
    	if (leaveStepForward()) {
    		multiPane.nextStep();
    		return false;
    	}
    	return false;
    }

    public static void makeRowsSortable(JTable table, TableModel model, int[] sortableColumnIndices) {
    	if(sortableColumnIndices.length == 0) {
    		return;
    	}
    	TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
        EmptyStringComparator emptyComp = new EmptyStringComparator();
        for(int i = 0; i < sortableColumnIndices.length; i++) {
        	rowSorter.setComparator(sortableColumnIndices[i], emptyComp);
        }
        table.setRowSorter(rowSorter);
    }
}
