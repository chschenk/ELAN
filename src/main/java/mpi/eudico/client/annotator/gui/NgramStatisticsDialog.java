package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.NgramStatsCommand;
import mpi.eudico.client.annotator.export.ExportNgramRawData;
import mpi.eudico.client.annotator.export.ExportNgramStatistics;
import mpi.eudico.client.annotator.ngramstats.NgramStatsResult;
import mpi.eudico.client.annotator.ngramstats.NgramStatsTableModel;
import mpi.eudico.client.annotator.tier.DisplayableContentTableModel;
import mpi.eudico.client.annotator.tier.SelectableContentTableModel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.client.annotator.util.SquelchOutput;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.util.EmptyStringComparator;
import mpi.eudico.util.FloatStringComparator;
import mpi.eudico.util.IntComparator;

/*
 * TODO sometimes this pops up on my box... I'm at a loss on how to investigate this
 * as it's somewhere deep in the swing guts :(
 * 
     [java] Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: 2 >= 1
     [java] 	at java.util.Vector.elementAt(Vector.java:470)
     [java] 	at javax.swing.table.DefaultTableColumnModel.getColumn(DefaultTableColumnModel.java:294)
     [java] 	at javax.swing.plaf.basic.BasicTableHeaderUI.paint(BasicTableHeaderUI.java:648)
     [java] 	at javax.swing.plaf.ComponentUI.update(ComponentUI.java:161)
     [java] 	at javax.swing.JComponent.paintComponent(JComponent.java:769)
     [java] 	at javax.swing.JComponent.paint(JComponent.java:1045)
     [java] 	at javax.swing.JComponent.paintChildren(JComponent.java:878)
     [java] 	at javax.swing.JComponent.paint(JComponent.java:1054)
     [java] 	at javax.swing.JViewport.paint(JViewport.java:731)
     [java] 	at javax.swing.JComponent.paintChildren(JComponent.java:878)
     [java] 	at javax.swing.JComponent.paint(JComponent.java:1054)
     [java] 	at javax.swing.JComponent.paintToOffscreen(JComponent.java:5212)
     [java] 	at javax.swing.BufferStrategyPaintManager.paint(BufferStrategyPaintManager.java:295)
     [java] 	at javax.swing.RepaintManager.paint(RepaintManager.java:1236)
     [java] 	at javax.swing.JComponent._paintImmediately(JComponent.java:5160)
     [java] 	at javax.swing.JComponent.paintImmediately(JComponent.java:4971)
     [java] 	at javax.swing.RepaintManager$3.run(RepaintManager.java:796)
     [java] 	at javax.swing.RepaintManager$3.run(RepaintManager.java:784)
     [java] 	at java.security.AccessController.doPrivileged(Native Method)
     [java] 	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:76)
     [java] 	at javax.swing.RepaintManager.paintDirtyRegions(RepaintManager.java:784)
     [java] 	at javax.swing.RepaintManager.paintDirtyRegions(RepaintManager.java:757)
     [java] 	at javax.swing.RepaintManager.prePaintDirtyRegions(RepaintManager.java:706)
     [java] 	at javax.swing.RepaintManager.access$1000(RepaintManager.java:62)
     [java] 	at javax.swing.RepaintManager$ProcessingRunnable.run(RepaintManager.java:1647)
     [java] 	at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:251)
     [java] 	at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:733)
     [java] 	at java.awt.EventQueue.access$200(EventQueue.java:103)
     [java] 	at java.awt.EventQueue$3.run(EventQueue.java:694)
     [java] 	at java.awt.EventQueue$3.run(EventQueue.java:692)
     [java] 	at java.security.AccessController.doPrivileged(Native Method)
     [java] 	at java.security.ProtectionDomain$1.doIntersectionPrivilege(ProtectionDomain.java:76)
     [java] 	at java.awt.EventQueue.dispatchEvent(EventQueue.java:703)
     [java] 	at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:242)
     [java] 	at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:161)
     [java] 	at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:150)
     [java] 	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:146)
     [java] 	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:138)
     [java] 	at java.awt.EventDispatchThread.run(EventDispatchThread.java:91)
 */

/**
 * A dialog for creating N-gram analysis on a specific tier in multiple files
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class NgramStatisticsDialog extends ClosableDialog
	implements ListSelectionListener, ActionListener, ProgressListener {
	private static final long serialVersionUID = 6308995887627969093L;
	
	private JButton rawButton, saveButton, closeButton;
	private JFrame parent;
	private JLabel titleLabel;
	private String selectedDomain = null;
	
	private JPanel ngramTablePane;
	private JTable ngramTable;
	private NgramStatsTableModel ngramModel;
	
	private JPanel tierSelectPanel, buttonPanel, titlePanel;
	private JTable tierTable;
	private JButton selectDomainButton, updateStatisticsButton;
	private JTextField ngramSize;

	private JProgressBar progressBar;
	
	private List<String> fileNames;
	
	private NgramStatsCommand curCommand;
	private NgramStatsResult curCommandResult;
	private Insets defaultInsets;
		
	public NgramStatisticsDialog(JFrame parentFrame){
		super(parentFrame);
		parent = parentFrame;
		
		setTitle(ElanLocale.getString("Menu.File.MultiFileNgramStats"));
		
		defaultInsets = new Insets(2, 6, 2, 6);
		
		initComponents();
		
		//resize and relocate window
		pack();
		setLocationRelativeTo(parent);
	}

	/**
	 * Initializes the GUI components
	 */
	private void initComponents(){
		//Setting title
        titleLabel = new JLabel(ElanLocale.getString("Menu.File.MultiFileNgramStats"));
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel = new JPanel();
        titlePanel.add(titleLabel);
        
        initTierSelectionPanel();
        initNgramsTable();
        initButtonPanel();        
        
        // init the progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        // init the gbc add our components!
        GridBagConstraints gbc = new GridBagConstraints();
        getContentPane().setLayout(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = defaultInsets;
        getContentPane().add(titlePanel, gbc);
        
        // add tier panel
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        getContentPane().add(tierSelectPanel, gbc);
        
        // add ngrams table
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        getContentPane().add(ngramTablePane, gbc);
        
        // add progress bar
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        getContentPane().add(progressBar, gbc);

        // add the save/close button panel
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        getContentPane().add(buttonPanel, gbc);

        getRootPane().setDefaultButton(closeButton);
	}

	/**
	 * Initialize the button panel (save/close) for display
	 */
	private void initButtonPanel() {
		//Button components
		rawButton = new JButton(ElanLocale.getString("Statistics.Multi.SaveRawData"));
        saveButton = new JButton(ElanLocale.getString("Button.Save"));
        closeButton = new JButton(ElanLocale.getString("Button.Close"));
        
        //Setting buttons
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 6, 2));
        buttonPanel.add(rawButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        
        //add event handlers
        rawButton.addActionListener(this);
        saveButton.addActionListener(this);
        closeButton.addActionListener(this);
        
        // disable save button until we have a stat to export!
        rawButton.setEnabled(false);
        saveButton.setEnabled(false);
	}

	/**
	 * Initialize the tier selection panel for display
	 */
	private void initTierSelectionPanel() {
		tierSelectPanel = new JPanel( new GridBagLayout() );
		tierSelectPanel.setPreferredSize(new Dimension(300, 150));
		tierSelectPanel.setBorder(new TitledBorder(ElanLocale.getString("Statistics.Multi.TierSelection")));
		
		tierTable = new JTable( new DisplayableContentTableModel(ElanLocale.getString("Statistics.Multi.NoShow")) );
		tierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tierTable.setShowVerticalLines(false);
		tierTable.getTableHeader().setReorderingAllowed(false);
		tierTable.setModel(new DisplayableContentTableModel(ElanLocale.getString("Statistics.Multi.NoShow")));
		tierTable.getColumnModel().getColumn(0).setHeaderValue(ElanLocale.getString("FileAndTierSelectionStepPane.Column.Header.Message"));
		
		JScrollPane tierTableScrollPane = new JScrollPane(tierTable);
		tierTableScrollPane.setColumnHeaderView(null);
		
		selectDomainButton = new JButton(ElanLocale.getString("MFE.DomainDefKey"));
		updateStatisticsButton = new JButton(ElanLocale.getString("Statistics.Multi.Update"));
        updateStatisticsButton.setEnabled(false);
        
        //add event handlers
        selectDomainButton.addActionListener(this);
        updateStatisticsButton.addActionListener(this);
        
        ngramSize = new JTextField();
        ngramSize.setText("2");
		
		//layout components
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = defaultInsets;
        tierSelectPanel.add(tierTableScrollPane, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        tierSelectPanel.add(selectDomainButton, gbc);
        
        gbc.gridy = 1;
        tierSelectPanel.add(new JLabel(ElanLocale.getString("Statistics.Multi.NgramSize")), gbc);
        
        gbc.gridy = 2;
        tierSelectPanel.add(ngramSize, gbc);
        
        gbc.gridy = 3;
        tierSelectPanel.add(updateStatisticsButton, gbc);
	}

	/**
	 * Initializes the N-gram JTable for display
	 */
	private void initNgramsTable() {
		ngramTablePane = new JPanel(new GridBagLayout());
//		ngramTablePane.setBorder(
//				BorderFactory.createCompoundBorder(
//						BorderFactory.createRaisedBevelBorder(),
//						BorderFactory.createLoweredBevelBorder()
//				)
//		);
		ngramTablePane.setPreferredSize(new Dimension(760,300));

		ngramModel = new NgramStatsTableModel(null);
		// Set up the ngram table
		ngramTable = new JTable(ngramModel);
		//ngramsTable = new JTable(new DefaultTableModel(new NgramStatsCollection("a", "a", 1).ngramsTableColumns, 1));
		//ngramsTable.setEnabled(false);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = defaultInsets;
		ngramTablePane.add(new JScrollPane(ngramTable), gbc);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton sender = (JButton) e.getSource();
		
		if ( sender == saveButton ) {
			// are we executing a command?
	    	if (curCommand != null) {
	    		return;
	    	}
	    	
	    	// actually export!
	    	progressBar.setString("Exporting Statistics...");
	    	new ExportNgramStatistics((JFrame)getParent(), true, null, ((NgramStatsTableModel)ngramTable.getModel()).getResult());
	    	progressBar.setString("Export Completed.");
	    	return;
		}
		
		if ( sender == rawButton ) {
			// are we executing a command?
	    	if (curCommand != null) {
	    		return;
	    	}
	    	
	    	// actually export!
	    	progressBar.setString("Exporting Raw Data...");
	    	new ExportNgramRawData((JFrame)getParent(), true, null, ((NgramStatsTableModel)ngramTable.getModel()).getResult());
	    	progressBar.setString("Export Completed.");
	    	return;
		}
		
		if ( sender == closeButton ) {
			setVisible(false);
			dispose();
			return;
		}

	    if (sender == selectDomainButton) {
	    	// are we executing a command?
	    	if (curCommand == null) {
	    		loadTiersFromDomain();
	    	}
		} 
	    
	    if ( sender == updateStatisticsButton ) {
	    	// are we executing a command?
	    	if (curCommand != null) {
	    		return;
	    	}
	    	
	    	// only allow integers for numNgrams
	    	try {
	    		int num = Integer.parseInt(ngramSize.getText());
	    		if ( num < 1 ) {
	    			throw new NumberFormatException("invalid");
	    		}
	    	} catch (NumberFormatException ex) {
	    		JOptionPane.showMessageDialog(this, ElanLocale.getString("Statistics.Multi.NgramSizeWarning"),
	    				ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
	    		return;
	    	}
	    	
	    	// Check to see if we have a tier selected?
	    	String selectedTier = null;
	    	for (int i = 0; i < tierTable.getModel().getRowCount(); i++) {
				if ( (Boolean)tierTable.getValueAt(i, 0) == true ) {
					selectedTier = (String)tierTable.getValueAt(i, 1);
					break;
				}
			}
	    	
	    	if ( selectedTier == null ) {
	    		JOptionPane.showMessageDialog(this, ElanLocale.getString("TranscriptionManager.SelectTierDlg.Empty_Tier"),
	    				ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
	    	} else {
	    		// Disable any buttons while we are processing
	    		updateStatisticsButton.setEnabled(false);
	    		selectDomainButton.setEnabled(false);
	    		saveButton.setEnabled(false);
	    		rawButton.setEnabled(false);
	    		
	    		// destroy the old result so we conserve memory
	    		ngramTable.setModel(new NgramStatsTableModel(null));
	    		
	    		// actually begin the calculation!
	    		curCommand = new NgramStatsCommand("NgramStats");
	    		curCommand.addProgressListener(this);
	    		curCommandResult = new NgramStatsResult(selectedDomain, selectedTier, Integer.parseInt(ngramSize.getText()));
	    		curCommand.execute(null, new Object[]{ fileNames, curCommandResult });
	    	}
	    }
	}

	/**
	 * Loads tier information from the selected domain
	 */
	private void loadTiersFromDomain () {
		// cache the previous domain so we can check if it was changed
		// Note: the domain name doesn't guarantee much, the domain
		// can be changed in the MFDomainDialog
		// String lastDomain = selectedDomain;
		
		// create domain dialog
		MFDomainDialog domainDialog = new MFDomainDialog(this, true);
		domainDialog.setVisible(true);

		// when domain is selected, get the search paths
    	List<String> searchPaths = domainDialog.getSearchPaths();
    	List<String> searchDirs = domainDialog.getSearchDirs();
    	String val = domainDialog.getDomainName();
    	// did we change domains or what? Only way is to compare all selected files
    	
		if (searchPaths.size() == 0 && searchDirs.size() == 0) {
			return;
		} else {
			selectedDomain = val;
		}
    	
   		tierSelectPanel.setBorder( new TitledBorder( ElanLocale.getString("Statistics.Multi.TierSelection") +
    				" (" + ElanLocale.getString("MFE.Domain") + ": " + val + ")" ));

    	// extract the filenames and add it to our list
    	fileNames = searchPaths;
		if (searchDirs.size() > 0) {
			String name;
			File f;
			for (int i = 0; i < searchDirs.size(); i++) {
				name = searchDirs.get(i);
				f = new File(name);
				if (f.isFile() && f.canRead()) {
					fileNames.add(f.getAbsolutePath());// should not occur
				} else if (f.isDirectory() && f.canRead()) {
					addFilesFromDirectory(f, fileNames);
				}
			}
		}
		
		// do we even have a file to search against?
		if ( fileNames.isEmpty() ) {
			tierTable.getSelectionModel().removeListSelectionListener(this);
			tierTable.setModel(new DisplayableContentTableModel(ElanLocale.getString("Statistics.Multi.NoShow")));
			updateStatisticsButton.setEnabled(false);
			return;
		}
		
		List<TierImpl> tiers = null;
		int count = 0;
		while (tiers == null && count < fileNames.size()) {		
			// Open the first file, and load the tiers from it, this assumes all files contain the same tiers
			EAFSkeletonParser parser = new EAFSkeletonParser(fileNames.get(count++));
			// also, squelch any output...
			SquelchOutput s = new SquelchOutput();
			try {
				s.squelchOutput();
			} catch (IOException ioe){}
			
			try {
				parser.parse();
			} catch (Exception e) {
				// ignore?
			}
			
			try {
				s.restoreOutput();
			} catch (IOException ioe){}
			
	        tiers = parser.getTiers();
		}
		
        Set<String> uniqueTierNames = new TreeSet<String>();

        if (tiers != null) {      
	        for (int i = 0; i < tiers.size(); i++) {
	        	TierImpl tier = tiers.get(i);
	            if (tier != null) {
	            	uniqueTierNames.add(tier.getName());
	            }
	        }
        }
        //update table
        if ( uniqueTierNames.isEmpty() ) {
        	tierTable.getSelectionModel().removeListSelectionListener(this);
        	tierTable.setModel(new DisplayableContentTableModel(ElanLocale.getString("Statistics.Multi.NoShow")));
        	updateStatisticsButton.setEnabled(false);
		} else {
			tierTable.getSelectionModel().removeListSelectionListener(this);
			tierTable.setModel(new SelectableContentTableModel(uniqueTierNames));
			tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
			tierTable.getColumnModel().getColumn(0).setHeaderValue("");
			tierTable.getColumnModel().getColumn(1).setHeaderValue(ElanLocale.getString("EditTierDialog.Label.TierName"));

			TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(tierTable.getModel());
	        EmptyStringComparator emptyComp = new EmptyStringComparator();
	        for (int i = 1; i < tierTable.getColumnCount(); i++) {
	    		rowSorter.setComparator(i, emptyComp);
	        }
	        tierTable.setRowSorter(rowSorter);
	        
			tierTable.getSelectionModel().addListSelectionListener(this);
			updateStatisticsButton.setEnabled(true);
			tierTable.repaint();
		}
	}
	
	/**
     * Scans the folders for eaf files and adds their paths to the files list, recursively.
     * <p>Adaptation from DomainPane, List of String instead of List of Files
     * @param dir the directory or folder
     * @param files the list to add the file paths to
     */
    private void addFilesFromDirectory(File dir, List<String> files) {
        if ((dir == null) && (files == null)) {
            return;
        }

        File[] allSubs = dir.listFiles();

        for (int i = 0; i < allSubs.length; i++) {
            if (allSubs[i].isDirectory() && allSubs[i].canRead()) {
            	addFilesFromDirectory(allSubs[i], files);
            } else {
                if (allSubs[i].canRead() && allSubs[i].getName().toLowerCase().endsWith(FileExtension.EAF_EXT[0])) {
                	// don't add duplicate files!
                    if ( ! files.contains(allSubs[i].getAbsolutePath()) ) {                    	
                    	files.add(allSubs[i].getAbsolutePath());
                    }
                }
            }
        }
    }
	
	/**
	 * Handler for the case if a user selects a tier in the table.
	 * <p>It then sets the value in the table accordingly
	 * @param e the event that fired
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {		
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		
		// are we processing a command?
		if (curCommand != null) {
			return;
		}

		if ( e.getValueIsAdjusting() ) {
			// we only modify the table if we have tiers to show...
			if ( fileNames != null && ! fileNames.isEmpty() ) {
				int selectedRow = lsm.getMinSelectionIndex();
				
				if(selectedRow >= 0) { // Necessary for unselecting a row and table sorting 
					// TODO this analytics package can only analyze one tier at a time...
					for (int i = 0; i < tierTable.getModel().getRowCount(); i++) {
						if ( (Boolean)tierTable.getValueAt(i, 0) == true ) {
							tierTable.setValueAt( !(Boolean)tierTable.getValueAt(i, 0), i, 0);
						}
					}
					tierTable.setValueAt( !(Boolean)tierTable.getValueAt(selectedRow, 0), selectedRow, 0);
				}
			}
		}
	}

	@Override
	public void progressCompleted(Object source, String message) {
        progressBar.setValue(100);
        progressBar.setString(message);
        
        // display report
        ProcessReport report = curCommand.getProcessReport();
        if (report != null) {
        	new ReportDialog(this, report).setVisible(true);
        }
        
        // update the table if we didn't encounter errors
        if (!message.equals("ERROR")) {
        	// create the model that will fetch values from the collection on demand
    		NgramStatsTableModel model = new NgramStatsTableModel(curCommandResult);
    		ngramTable.setModel(model);

            TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);

            IntComparator<Integer> intComp = new IntComparator<Integer>();
            rowSorter.setComparator(1, intComp);
            int numCols = ngramModel.getColumnCount();
            FloatStringComparator<String> fsComp = new FloatStringComparator<String>();
            for (int i = 2; i < numCols; i++) {
            	rowSorter.setComparator(i, fsComp);
            }

            ngramTable.setRowSorter(rowSorter);
        }
        
        // Enable buttons now!
		updateStatisticsButton.setEnabled(true);
		selectDomainButton.setEnabled(true);
		saveButton.setEnabled(true);
		rawButton.setEnabled(true);
		curCommand = null;
		curCommandResult = null;
	}

	@Override
	public void progressUpdated(Object source, int percent, String message) {
        if (percent == progressBar.getMaximum()) {
            progressCompleted(source, message);
        } else {
            progressBar.setValue(percent);
            progressBar.setString(message);
        }
	}

	@Override
	public void progressInterrupted(Object source, String message) {
		// do nothing...		
	}
}
